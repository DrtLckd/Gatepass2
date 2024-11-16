/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gpsystem.old;

import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;


public class rtsp_capture1 {
    private MediaPlayer mediaPlayer;
    private Timer detectionTimer;
    private static final String CAPTURE_DIR = "captures"; // nandito yung mga nakuha niyang imahe


    public rtsp_capture1(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
        setupCaptureDirectory();
        setupDetectionListener();
    }

    // Ensure the capture directory exists
    private void setupCaptureDirectory() {
        File dir = new File(CAPTURE_DIR);
        if (!dir.exists()) {
            dir.mkdir(); // Create the directory if it doesn't exist
        }
    }
    
    private void setupDetectionListener() {
        // Set up a timer to periodically check for bounding boxes in the stream
        detectionTimer = new Timer(1000, new ActionListener() { // Check every second
            @Override
            public void actionPerformed(ActionEvent e) {
                captureFrameIfDetected();
            }
        });
        detectionTimer.start();

        mediaPlayer.events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
            @Override
            public void playing(MediaPlayer mediaPlayer) {
                System.out.println("Stream playing, starting detection.");
                detectionTimer.start();
            }

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

    private void captureFrameIfDetected() {
        if (detectVehicleOrPlate()) {
            String filename = CAPTURE_DIR + File.separator + "screengrab_" + System.currentTimeMillis() + ".jpg";
            File file = new File(filename);
            boolean result = mediaPlayer.snapshots().save(file);

            if (result) {
                System.out.println("Screengrab saved: " + filename);
            } else {
                System.out.println("Failed to save screengrab.");
            }
        }
    }

    private boolean detectVehicleOrPlate() {
        // Implement your detection logic here for bounding boxes
        // Return true if a vehicle or plate is detected
        return true; // Placeholder, replace with actual detection condition
    }
}