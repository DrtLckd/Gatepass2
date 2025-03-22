package gpsystem;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fi.iki.elonen.NanoHTTPD;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.awt.TextArea;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import org.opencv.core.Rect;
import java.net.*;
import java.io.*;

public class detectCaptureServer extends NanoHTTPD {
    private MediaPlayer mediaPlayer;
    private TextArea detectedTextPane;  // For detection details
    private TextArea extractedTextPane; // For OCR results
    private static final String CAPTURE_DIR = "captures"; // Directory for saving images
    private static final String PROCESSED_DIR = "processed"; // Directory for processed images
    private final int MAX_DETECTION_LINES = 15;  // ✅ Keep only the last 15 detections
    private final List<String> detectionHistory = new LinkedList<>();
    private final int MAX_OCR_LINES = 10;  // ✅ Keep last 10 OCR results
    private final List<String> ocrHistory = new LinkedList<>();

    private static final String AMB82_URL = "http://192.168.0.27:5000/plate";  // Update with your AMB82 IP
    
    public detectCaptureServer(MediaPlayer mediaPlayer, TextArea detectedTextPane, TextArea extractedTextPane) throws Exception {
        super("0.0.0.0", 5000);  // ✅ NEW: Listens on ALL network interfaces (Wi-Fi & Ethernet)
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
        System.out.println("Incoming request: " + session.getMethod() + " from " + session.getRemoteIpAddress());

        if (Method.POST.equals(session.getMethod())) {
            try {
                Map<String, String> files = new HashMap<>();
                session.parseBody(files);
                String receivedData = files.get("postData");

                if (receivedData == null || receivedData.isEmpty()) {
                    System.err.println("Received empty request payload.");
                    return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json", "{\"error\":\"Empty request payload\"}");
                }

                System.out.println("Received data: " + receivedData);

                try {
                    com.google.gson.JsonObject jsonObject = com.google.gson.JsonParser.parseString(receivedData).getAsJsonObject();
                    if (!jsonObject.has("detections")) {
                        return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json", "{\"error\":\"Missing detections array\"}");
                    }

                    com.google.gson.JsonArray detections = jsonObject.getAsJsonArray("detections");
                    if (detections.size() == 0) {
                        return newFixedLengthResponse(Response.Status.OK, "application/json", "{\"status\":\"no detections found\"}");
                    }

                    // ✅ Get current timestamp
                    String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());

                    // ✅ Store detected objects
                    boolean carDetected = false;
                    boolean motorDetected = false;
                    boolean plateDetected = false;
                    List<Rect> detectedPlates = new ArrayList<>();
                    List<String> detectionLogs = new ArrayList<>();

                    int plateIndex = 0;  // NEW: Track plate numbers separately

                    for (int i = 0; i < detections.size(); i++) {
                        com.google.gson.JsonObject detection = detections.get(i).getAsJsonObject();
                        if (!detection.has("label") || !detection.has("bbox")) {
                            System.err.println("Invalid detection format, skipping.");
                            continue;
                        }

                        String type = detection.get("label").getAsString();
                        float confidence = detection.has("conf") ? detection.get("conf").getAsFloat() : 1.0f;

                        if (confidence < 0.5) {
                            System.out.println("Ignoring low-confidence detection: " + type + " (" + confidence + ")");
                            continue;
                        }

                        com.google.gson.JsonArray bbox = detection.getAsJsonArray("bbox");
                        if (bbox.size() < 4) {
                            System.err.println("Invalid bounding box format, skipping.");
                            continue;
                        }

                        int xmin = bbox.get(0).getAsInt();
                        int ymin = bbox.get(1).getAsInt();
                        int xmax = bbox.get(2).getAsInt();
                        int ymax = bbox.get(3).getAsInt();

                        // ✅ Store in formatted logs
                        detectionLogs.add(String.format("[%s] %s - Confidence: %.2f, BBox: [%d, %d, %d, %d]", 
                            timestamp, type, confidence, xmin, ymin, xmax, ymax));

                        if (type.equals("car")) {
                            carDetected = true;
                        } else if (type.equals("motor")) {
                            motorDetected = true;
                        } else if (type.equals("plate")) {
                            plateDetected = true;
                            detectedPlates.add(new Rect(xmin, ymin, xmax - xmin, ymax - ymin));
                            System.out.println("Detected Plate " + plateIndex + ": BBox [" + xmin + ", " + ymin + ", " + xmax + ", " + ymax + "]");
                            plateIndex++;
                        }
                    }


                    // ✅ Update UI with structured logs
                    updateDetectedTextPane(String.join("\n", detectionLogs));

                    // ✅ Separate Car and Plate Detection
                    if (carDetected && plateDetected) {
                        System.out.println("Car & Plate detected! Capturing snapshot...");

                        // More readable timestamped filename
                        String formattedTime = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
                        String snapshotPath = CAPTURE_DIR + File.separator + formattedTime + ".jpg";

                        boolean snapshotResult = mediaPlayer.snapshots().save(new File(snapshotPath));
                        if (!snapshotResult) {
                            System.err.println("Snapshot failed for car + plate, cannot process frame.");
                            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json", "{\"error\":\"Snapshot capture failed for car\"}");
                        }

                        System.out.println("Snapshot saved: " + snapshotPath);
                        captureAndProcessFrame(snapshotPath, detectedPlates);
                    }

                    // ✅ Separate Motorcycle and Plate Detection
                    else if (motorDetected && plateDetected) {
                        System.out.println("Motor & Plate detected! Capturing snapshot...");

                        // More readable timestamped filename for motorcycle
                        String formattedTime = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
                        String snapshotPath = CAPTURE_DIR + File.separator + formattedTime + ".jpg";

                        boolean snapshotResult = mediaPlayer.snapshots().save(new File(snapshotPath));
                        if (!snapshotResult) {
                            System.err.println("Snapshot failed for motor + plate, cannot process frame.");
                            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json", "{\"error\":\"Snapshot capture failed for motor\"}");
                        }

                        System.out.println("Snapshot saved: " + snapshotPath);
                        captureAndProcessFrame(snapshotPath, detectedPlates);
                    }

                    // ✅ If no valid combination of vehicle and plate is detected, log and ignore
                    else {
                        System.out.println("Vehicle or plate missing. Ignoring data.");
                        return newFixedLengthResponse(Response.Status.OK, "application/json", "{\"status\":\"vehicle or plate missing\"}");
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

    // For Detected Text
    private void updateDetectedTextPane(String detections) {
        SwingUtilities.invokeLater(() -> {
            if (detectedTextPane != null) {
                if (!detections.trim().isEmpty()) {
                    detectionHistory.add(detections);

                    // Keep only the last MAX_DETECTION_LINES
                    while (detectionHistory.size() > MAX_DETECTION_LINES) {
                        detectionHistory.remove(0);
                    }

                    // Format the logs as requested
                    String formattedLogs = String.join("\n\n", detectionHistory);
                    detectedTextPane.setText(formattedLogs);
                }
            } else {
                System.err.println("DetectedTextPane is null!");
            }
        });
    }

    // For Extracted Text
    private void updateExtractedTextPane(String extractedText) {
        SwingUtilities.invokeLater(() -> {
            if (extractedTextPane != null) {
                if (!extractedText.trim().isEmpty()) {
                    // Format extracted text as requested and append it
                    String formattedText = extractedText + "\n\n"; 
                    extractedTextPane.append(formattedText);
                }
            } else {
                System.err.println("ExtractedTextPane is null!");
            }
        });
    }

    private void captureAndProcessFrame(String snapshotPath, List<Rect> detectedPlates) {
        if (!mediaPlayer.status().isPlaying()) {
            System.out.println("Skipping snapshot: No active stream.");
            return;
        }

        if (detectedPlates.isEmpty()) {
            System.out.println("Skipping processing: No plates detected.");
            return;
        }

        String folderName = snapshotPath.substring(snapshotPath.lastIndexOf(File.separator) + 1, snapshotPath.lastIndexOf("."));
        String folderPath = "processed" + File.separator + folderName;
        File folder = new File(folderPath);
        if (!folder.exists()) folder.mkdirs(); // Create folder if it doesn't exist

        for (int plateIndex = 0; plateIndex < detectedPlates.size(); plateIndex++) {
            Rect plate = detectedPlates.get(plateIndex);

            // ✅ Validate Region of Interest (ROI)
            if (plate.x < 0 || plate.y < 0 || plate.width <= 0 || plate.height <= 0) {
                System.err.println("Invalid plate region: " + plate);
                continue;
            }

            String finalImagePath = folderPath + File.separator + "final.jpg";

            try {
                boolean preprocessSuccess = imageProcess.preprocessAndSave(snapshotPath, finalImagePath, plate.x, plate.y, plate.x + plate.width, plate.y + plate.height);

                if (!preprocessSuccess) {
                    System.err.println("Preprocessing failed for plate " + plateIndex + ", skipping OCR.");
                    continue;
                }

                // ✅ Ensure OCR Image Exists
                File ocrImageFile = new File(finalImagePath);
                if (!ocrImageFile.exists() || ocrImageFile.length() == 0) {
                    System.err.println("OCR Skipped: Image file does not exist or is empty - " + finalImagePath);
                    continue;
                }

                // ✅ Run OCR on the Processed Image & Extract Best Results
                String[] plateResults = extractBestOCRResult(ocr_python.runOCR(finalImagePath));
                String extractedText = plateResults[0];  // Original OCR output
                String normalizedText = plateResults[1]; // Alternative corrected form

                if (extractedText.isEmpty()) {
                    System.err.println("OCR did not detect any text for plate " + plateIndex);
                    continue;
                }

                System.out.println("OCR Extracted Text for plate " + plateIndex + ": " + extractedText);
                System.out.println("Normalized OCR Plate: " + normalizedText);

                // ✅ Validate the License Plate in Database using both versions
                boolean isPlateValid = dbConn.validateLicensePlate(extractedText) || dbConn.validateLicensePlate(normalizedText);
                String plateDetails = dbConn.getPlateDetails(extractedText);

                if (isPlateValid) {
                    System.out.println("Plate is REGISTERED.\n" + plateDetails);
                    updateExtractedTextPane("Plate " + plateIndex + ": " + extractedText + "\n" + plateDetails);
                    sendPlateToAMB82(extractedText);
                } else {
                    System.out.println("Plate is NOT REGISTERED.");
                    updateExtractedTextPane("Plate " + plateIndex + ": " + extractedText + "\n❌ Not Registered.");
                }

            } catch (Exception e) {
                System.err.println("Error processing plate " + plateIndex + ": " + e.getMessage());
            }
        }
    }

    // Method to send the license plate to AMB82
    public void sendPlateToAMB82(String licensePlate) {
        // Prepare JSON data to send to AMB82
        JsonObject jsonPayload = new JsonObject();
        jsonPayload.addProperty("plate", licensePlate);

        // Send HTTP POST request to AMB82 (assuming AMB82 is listening on its own server)
        try {
            URL url = new URL(AMB82_URL);  // AMB82 URL
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");

            // Send JSON data
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonPayload.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Check response from AMB82
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                String response = br.readLine();
                System.out.println("AMB82 Response: " + response);
            }

        } catch (Exception e) {
            System.err.println("Error sending plate to AMB82: " + e.getMessage());
        }
    }


    private String[] extractBestOCRResult(String ocrOutput) {
        try {
            JsonArray ocrResults = JsonParser.parseString(ocrOutput).getAsJsonArray();
            String bestPlate = "";
            String normalizedPlate = "";
            double highestConfidence = 0.0;

            for (JsonElement element : ocrResults) {
                JsonObject obj = element.getAsJsonObject();
                String originalText = obj.get("text").getAsString().replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
                double confidence = obj.get("confidence").getAsDouble();

                // Normalize for misreads
                String tempNormalized = originalText
                    .replace('I', '1')
                    .replace('L', '1')
                    .replace('O', '0')
                    .replace('B', '8')
                    .replace('S', '5');

                if (confidence > highestConfidence) {
                    highestConfidence = confidence;
                    bestPlate = originalText;  // Keep the original extracted text
                    normalizedPlate = tempNormalized;  // Store the alternative form
                }
            }

            // Return both original and normalized plates
            return new String[]{bestPlate, normalizedPlate};

        } catch (Exception e) {
            System.err.println("Error parsing OCR output: " + e.getMessage());
            return new String[]{"", ""}; // Return empty values to prevent crashes
        }
    }
}
