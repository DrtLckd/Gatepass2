package gpsystem;

import fi.iki.elonen.NanoHTTPD;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.awt.TextArea;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import org.opencv.core.Rect;

public class detectionAndCaptureServer extends NanoHTTPD {
    private MediaPlayer mediaPlayer;
    private TextArea detectedTextPane;  // For detection details
    private TextArea extractedTextPane; // For OCR results
    
    private static final String CAPTURE_DIR = "captures"; // Directory for saving images
    private static final String PROCESSED_DIR = "processed"; // Directory for processed images

    public detectionAndCaptureServer(MediaPlayer mediaPlayer, TextArea detectedTextPane, TextArea extractedTextPane) throws Exception {
        super(5000); // Start HTTP server on port 5000
        this.mediaPlayer = mediaPlayer;
        this.detectedTextPane = detectedTextPane;
        this.extractedTextPane = extractedTextPane;
        setupDirectories();
        setupMediaPlayerListener();
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        System.out.println("Server is running on http://localhost:5000/");
    }

    private void setupDirectories() {
        File captureDir = new File(CAPTURE_DIR);
        if (!captureDir.exists()) captureDir.mkdir();

        File processedDir = new File(PROCESSED_DIR);
        if (!processedDir.exists()) processedDir.mkdir();
    }

    private void setupMediaPlayerListener() {
        mediaPlayer.events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
            @Override
            public void playing(MediaPlayer mediaPlayer) {
                System.out.println("Stream playing.");
            }

            @Override
            public void finished(MediaPlayer mediaPlayer) {
                System.out.println("Stream finished.");
            }

            @Override
            public void error(MediaPlayer mediaPlayer) {
                System.out.println("Stream error.");
            }
        });
    }

    @Override
    public Response serve(IHTTPSession session) {
        if (Method.POST.equals(session.getMethod())) {
            try {
                Map<String, String> files = new HashMap<>();
                session.parseBody(files);
                String receivedData = files.get("postData");

                if (receivedData == null || receivedData.isEmpty()) {
                    System.err.println("❌ Received empty request payload.");
                    return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json", "{\"error\":\"Empty request payload\"}");
                }

                System.out.println("✅ Received data: " + receivedData);

                // **Try parsing JSON safely**
                try {
                    com.google.gson.JsonObject jsonObject = com.google.gson.JsonParser.parseString(receivedData).getAsJsonObject();
                    if (!jsonObject.has("detections")) {
                        return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json", "{\"error\":\"Missing detections array\"}");
                    }

                    com.google.gson.JsonArray detections = jsonObject.getAsJsonArray("detections");
                    if (detections.size() == 0) {
                        return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json", "{\"error\":\"No detections found\"}");
                    }

                    StringBuilder detectionDetails = new StringBuilder("Detections:\n");
                    boolean vehicleDetected = false;
                    List<Rect> detectedPlates = new ArrayList<>();

                    for (int i = 0; i < detections.size(); i++) {
                        com.google.gson.JsonObject detection = detections.get(i).getAsJsonObject();
                        if (!detection.has("type") || !detection.has("coords")) {
                            System.err.println("❌ Invalid detection format, skipping.");
                            continue;
                        }

                        String type = detection.get("type").getAsString();
                        com.google.gson.JsonArray coords = detection.getAsJsonArray("coords");

                        if (coords.size() < 4) {
                            System.err.println("❌ Invalid bounding box format, skipping.");
                            continue;
                        }

                        detectionDetails.append(type).append(" - Coords: [")
                                .append(coords.get(0).getAsInt()).append(", ")
                                .append(coords.get(1).getAsInt()).append(", ")
                                .append(coords.get(2).getAsInt()).append(", ")
                                .append(coords.get(3).getAsInt()).append("]\n");

                        if ("car".equals(type) || "motorcycle".equals(type)) {
                            vehicleDetected = true;
                        } else if ("plate".equals(type)) {
                            int xmin = coords.get(0).getAsInt();
                            int ymin = coords.get(1).getAsInt();
                            int xmax = coords.get(2).getAsInt();
                            int ymax = coords.get(3).getAsInt();
                            detectedPlates.add(new Rect(xmin, ymin, xmax - xmin, ymax - ymin));
                        }
                    }

                    updateDetectedTextPane(detectionDetails.toString());

                    // **Only capture frame if both vehicle & plate detected**
                    if (vehicleDetected && !detectedPlates.isEmpty()) {
                        System.out.println("🚗🔍 Conditions met! Capturing snapshot...");
                        String snapshotPath = CAPTURE_DIR + File.separator + System.currentTimeMillis() + ".jpg";

                        boolean snapshotResult = mediaPlayer.snapshots().save(new File(snapshotPath));
                        if (snapshotResult) {
                            System.out.println("📸 Snapshot saved: " + snapshotPath);
                            captureAndProcessFrame(snapshotPath, detectedPlates);
                        } else {
                            System.out.println("❌ Snapshot failed.");
                        }
                    }

                    return newFixedLengthResponse(Response.Status.OK, "application/json", "{\"status\":\"snapshot triggered\"}");
                } catch (Exception e) {
                    e.printStackTrace();
                    return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json", "{\"error\":\"Invalid JSON format\"}");
                }
            } catch (Exception e) {
                e.printStackTrace();
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Error processing request.");
            }
        }
        return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Not Found");
    }



    private void updateDetectedTextPane(String detections) {
        SwingUtilities.invokeLater(() -> {
            if (detectedTextPane != null) {
                detectedTextPane.setText(detections);
            } else {
                System.err.println("DetectedTextPane is null!");
            }
        });
    }

    private void updateExtractedTextPane(String extractedText) {
        SwingUtilities.invokeLater(() -> {
            if (extractedTextPane != null) {
                String existingText = extractedTextPane.getText();
                extractedTextPane.setText(existingText + "\n" + extractedText);
            } else {
                System.err.println("ExtractedTextPane is null!");
            }
        });
    }


    private void captureAndProcessFrame(String snapshotPath, List<Rect> detectedPlates) {
        if (!mediaPlayer.status().isPlaying()) {
            System.out.println("Skipping snapshot: No active stream.");
            return; // Prevents capturing an invalid frame
        }

        if (detectedPlates.isEmpty()) {
            System.out.println("Skipping processing: No plates detected.");
            return;
        }

        String folderName = snapshotPath.substring(snapshotPath.lastIndexOf(File.separator) + 1, snapshotPath.lastIndexOf("."));
        String folderPath = "processed" + File.separator + folderName;

        File folder = new File(folderPath);
        if (!folder.exists()) folder.mkdirs(); // Create folder if it doesn't exist

        for (int i = 0; i < detectedPlates.size(); i++) {
            Rect plate = detectedPlates.get(i);
            
            // ✅ Validate ROI before processing
            if (plate.x < 0 || plate.y < 0 || plate.width <= 0 || plate.height <= 0) {
                System.err.println("❌ Invalid plate region: " + plate);
                continue;
            }
        
            String finalImagePath = folderPath + File.separator + "plate_" + i + "_final.jpg";

            try {
                boolean preprocessSuccess = image_preprocess.preprocessAndSave(snapshotPath, finalImagePath, plate.x, plate.y, plate.x + plate.width, plate.y + plate.height);

                if (!preprocessSuccess) {
                    System.err.println("Preprocessing failed for plate " + i + ", skipping OCR.");
                    continue;
                }

                String extractedText = ocr_python.runOCR(finalImagePath);

                if (extractedText == null || extractedText.trim().isEmpty()) {
                    System.err.println("❌ OCR did not detect any text for plate " + i);
                } else {
                    System.out.println("✅ OCR Extracted Text for plate " + i + ": " + extractedText);
                }

                // ✅ Append results instead of overwriting
                updateExtractedTextPane("Plate " + i + ": " + extractedText);

            } catch (Exception e) {
                System.err.println("Error processing plate " + i + ": " + e.getMessage());
            }
        }
    }
}
