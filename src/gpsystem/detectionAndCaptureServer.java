package gpsystem;

import fi.iki.elonen.NanoHTTPD;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.awt.TextArea;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;

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

                if (receivedData != null && !receivedData.isEmpty()) {
                    System.out.println("Received data: " + receivedData);

                    // Parse JSON to extract detection details
                    com.google.gson.JsonObject jsonObject = com.google.gson.JsonParser.parseString(receivedData).getAsJsonObject();
                    com.google.gson.JsonArray detections = jsonObject.getAsJsonArray("detections");

                    StringBuilder detectionDetails = new StringBuilder("Detections:\n");
                    boolean vehicleDetected = false; // Either "car" or "motorcycle"
                    boolean plateDetected = false;
                    int xmin = 0, ymin = 0, xmax = 0, ymax = 0;

                    for (int i = 0; i < detections.size(); i++) {
                        com.google.gson.JsonObject detection = detections.get(i).getAsJsonObject();
                        String type = detection.get("type").getAsString();
                        com.google.gson.JsonArray coords = detection.getAsJsonArray("coords");

                        detectionDetails.append(type)
                            .append(" - Coords: [")
                            .append(coords.get(0).getAsInt()).append(", ")
                            .append(coords.get(1).getAsInt()).append(", ")
                            .append(coords.get(2).getAsInt()).append(", ")
                            .append(coords.get(3).getAsInt()).append("]\n");

                        if ("car".equals(type) || "motorcycle".equals(type)) {
                            vehicleDetected = true;
                        } else if ("plate".equals(type)) {
                            plateDetected = true;
                            xmin = coords.get(0).getAsInt();
                            ymin = coords.get(1).getAsInt();
                            xmax = coords.get(2).getAsInt();
                            ymax = coords.get(3).getAsInt();
                        }
                    }

                    // Update detectedTextPane with detection details
                    updateDetectedTextPane(detectionDetails.toString());

                    // If conditions met, capture and process snapshot
                    if (vehicleDetected && plateDetected) {
                        System.out.println("Conditions met. Capturing snapshot...");
                        String snapshotPath = CAPTURE_DIR + File.separator + System.currentTimeMillis() + ".jpg";

                        boolean snapshotResult = mediaPlayer.snapshots().save(new File(snapshotPath));
                        if (snapshotResult) {
                            System.out.println("Snapshot saved: " + snapshotPath);
                            captureAndProcessFrame(snapshotPath, xmin, ymin, xmax, ymax);
                        } else {
                            System.out.println("Snapshot failed.");
                        }
                    }
                }
                return newFixedLengthResponse(Response.Status.OK, "application/json", "{\"status\":\"snapshot triggered\"}");
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
                extractedTextPane.setText(extractedText);
            } else {
                System.err.println("ExtractedTextPane is null!");
            }
        });
    }

    private void captureAndProcessFrame(String snapshotPath, int xmin, int ymin, int xmax, int ymax) {
        if (!mediaPlayer.status().isPlaying()) {
            System.out.println("Skipping snapshot: No active stream.");
            return; // Prevents capturing an invalid frame
        }

        // Ensure xmin, ymin, xmax, ymax are valid
        if (xmin == xmax || ymin == ymax) {
            System.out.println("Skipping processing: No plate detected.");
            return; // Prevents trying to process an empty region
        }

        String folderName = snapshotPath.substring(snapshotPath.lastIndexOf(File.separator) + 1, snapshotPath.lastIndexOf(".")); // Unique folder name
        String folderPath = "processed" + File.separator + folderName;

        File folder = new File(folderPath);
        if (!folder.exists()) folder.mkdirs(); // Create folder if it doesn't exist

        String finalImagePath = folderPath + File.separator + "final_image.jpg";

        try {
            // Step 1: Perform Image Preprocessing
            boolean preprocessSuccess = image_preprocess.preprocessAndSave(snapshotPath, finalImagePath, xmin, ymin, xmax, ymax);

            if (!preprocessSuccess) {
                System.err.println("Preprocessing failed, skipping OCR.");
                return; // Prevents OCR from running on an invalid image
            }

            // Step 2: Perform OCR on the Final Processed Image
            String extractedText = ocr_python.runOCR(finalImagePath);

            // Step 3: Update the Extracted Text Pane in home_dsb
            updateExtractedTextPane(extractedText);

        } catch (Exception e) {
            System.err.println("Error during image preprocessing or OCR: " + e.getMessage());
        }
    }

}
