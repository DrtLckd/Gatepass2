package gpsystem;

import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class rtsp_capture {
    private MediaPlayer mediaPlayer;
    private Timer detectionTimer;
    private static final String CAPTURE_DIR = "captures"; // Directory for saving images
    private static final String PROCESSED_DIR = "processed"; // Directory for processed images
    private static final String DETECTION_SERVER_URL = "http://localhost:5000"; // Detection server URL

    public rtsp_capture(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
        setupDirectories();
        setupDetectionListener();
    }

    public void enableDetection() {
        if (detectionTimer != null && !detectionTimer.isRunning()) {
            detectionTimer.start();
        }
    }

    public void disableDetection() {
        if (detectionTimer != null && detectionTimer.isRunning()) {
            detectionTimer.stop();
        }
    }

    // Ensure necessary directories exist
    private void setupDirectories() {
        File captureDir = new File(CAPTURE_DIR);
        if (!captureDir.exists()) captureDir.mkdir();

        File processedDir = new File(PROCESSED_DIR);
        if (!processedDir.exists()) processedDir.mkdir();
    }

    private void setupDetectionListener() {
        detectionTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Query the detection server for detection data
                String detectionData = queryDetectionServer();
                if (detectionData != null) {
                    handleDetectionData(detectionData);
                }
            }
        });

        mediaPlayer.events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
            @Override
            public void playing(MediaPlayer mediaPlayer) {
                System.out.println("Stream playing.");
            }

//            @Override
//            public void finished(MediaPlayer mediaPlayer) {
//                System.out.println("Stream finished.");
//                disableDetection(); // Ensure timer is stopped
//            }
//
//            @Override
//            public void error(MediaPlayer mediaPlayer) {
//                System.out.println("Stream error.");
//                disableDetection(); // Ensure timer is stopped
//            }
            @Override
            public void finished(MediaPlayer mediaPlayer) {
                System.out.println("Stream finished.");
                detectionTimer.stop();
            }

            @Override
            public void error(MediaPlayer mediaPlayer) {
                System.out.println("Stream error.");
                detectionTimer.stop();
            }
        });
    }

    private String queryDetectionServer() {
        try {
            URL url = new URL(DETECTION_SERVER_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            if (connection.getResponseCode() == 200) {
                InputStreamReader reader = new InputStreamReader(connection.getInputStream());
                StringBuilder response = new StringBuilder();
                int c;
                while ((c = reader.read()) != -1) {
                    response.append((char) c);
                }
                reader.close();
                return response.toString();
            } else {
                System.out.println("Detection server returned: " + connection.getResponseCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
            disableDetection(); // Disable detection on error
            JOptionPane.showMessageDialog(null, "Unable to connect to Detection Server.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }

    private void handleDetectionData(String detectionData) {
        System.out.println("Raw Detection Data: " + detectionData); // Log the received data

        try {
            File screengrabFile = new File("captures/screengrab.jpg");
            if (!screengrabFile.exists()) {
                System.out.println("No screengrab available. Skipping detection processing.");
                return;
            }

            com.google.gson.JsonObject jsonObject = com.google.gson.JsonParser.parseString(detectionData).getAsJsonObject();
            com.google.gson.JsonObject coordinates = jsonObject.getAsJsonObject("coordinates");

            int xmin = coordinates.get("xmin").getAsInt();
            int ymin = coordinates.get("ymin").getAsInt();
            int xmax = coordinates.get("xmax").getAsInt();
            int ymax = coordinates.get("ymax").getAsInt();

            String type = jsonObject.get("type").getAsString();

            System.out.println("Detection received: Type=" + type + ", Coordinates=" + xmin + "," + ymin + "," + xmax + "," + ymax);

            captureAndProcessFrame(xmin, ymin, xmax, ymax);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void captureAndProcessFrame(int xmin, int ymin, int xmax, int ymax) {
        String capturePath = CAPTURE_DIR + File.separator + "capture_" + System.currentTimeMillis() + ".jpg";
        File captureFile = new File(capturePath);

        boolean result = mediaPlayer.snapshots().save(captureFile);

        if (result) {
            System.out.println("Screengrab saved: " + capturePath);

            // Pass image for preprocessing
            String processedPath = PROCESSED_DIR + File.separator + "processed_" + System.currentTimeMillis() + ".jpg";
            image_preprocess.preprocessAndSave(capturePath, processedPath, xmin, ymin, xmax, ymax);

            System.out.println("Processed image saved: " + processedPath);
        } else {
            System.out.println("Failed to save screengrab.");
        }
    }
}