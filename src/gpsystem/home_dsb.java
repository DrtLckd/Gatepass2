package gpsystem;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import java.util.LinkedList;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;

/**
 *
 * @author acer
 */
public class home_dsb extends javax.swing.JFrame {

    private final EmbeddedMediaPlayerComponent mediaPlayerComponent;
    private MediaPlayer mediaPlayer;
    private LinkedList<String> recentIPs = new LinkedList<>(); // Store recent IPs
    private final int maxRecent = 5; // Maximum number of recent IPs to remember
    private rtsp_capture rtsp_capture;
    private detection_server serverInstance;
    private Thread serverThread;

    public home_dsb() {
        initComponents();
        rtspIPComboBox.requestFocusInWindow();

        // Set layout for the main frame
        setLayout(new BorderLayout());
    
        // Initialize VLCJ media player component and add it to the streamPanel
        mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
        streamPanel.setLayout(new BorderLayout());
        streamPanel.add(mediaPlayerComponent, BorderLayout.CENTER);

        // Initialize the mediaPlayer instance
        mediaPlayer = mediaPlayerComponent.mediaPlayer();
        
        // Initialize StreamCaptureHandler with the mediaPlayer
        rtsp_capture = new rtsp_capture(mediaPlayer);
        
        // Default message and background color for the stream panel
        JLabel initialLabel = new JLabel("Please enter IP and Port, then press Submit", SwingConstants.CENTER);
        streamPanel.setBackground(java.awt.Color.DARK_GRAY);
        initialLabel.setForeground(java.awt.Color.WHITE);
        streamPanel.add(initialLabel, BorderLayout.CENTER);
        
        // Add a listener to resize the media player component within the streamPanel
        streamPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {
                mediaPlayerComponent.setSize(streamPanel.getSize()); // Adjust size dynamically
            }
        });

        updateRecentIPs(); // Load into JComboBox

        // Add an event listener for error handling
        mediaPlayer.events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
            @Override
            public void playing(MediaPlayer mediaPlayer) {
                System.out.println("Streaming started...");
            }

            @Override
            public void finished(MediaPlayer mediaPlayer) {
                System.out.println("Streaming ended.");
                JOptionPane.showMessageDialog(null, "Stream ended.", "Info", JOptionPane.INFORMATION_MESSAGE);
                showUnavailableMessage();
            }

            @Override
            public void error(MediaPlayer mediaPlayer) {
                System.out.println("An error occurred during streaming.");
                JOptionPane.showMessageDialog(null, "An error occurred. Check IP and Port.", "Error", JOptionPane.ERROR_MESSAGE);
                showUnavailableMessage();
            }
        });
        setLocationRelativeTo(null); // Center the window on the screen
    }
        
   private void startStream(String rtspUrl) {
    System.out.println("Starting stream with URL: " + rtspUrl);

    // Show "Connecting..." message immediately
    showConnectingMessage();

    // Add the media component to the stream panel on the Event Dispatch Thread
    SwingUtilities.invokeLater(() -> {
        // Clear the panel and add the media component
        streamPanel.removeAll();
        streamPanel.add(mediaPlayerComponent, BorderLayout.CENTER);
        streamPanel.revalidate();
        streamPanel.repaint();

        // Ensure the component is displayable before starting the stream
        if (!mediaPlayerComponent.isDisplayable()) {
            System.out.println("MediaPlayerComponent is not displayable. Aborting stream start.");
            JOptionPane.showMessageDialog(this, "Video surface is not initialized properly.", "Error", JOptionPane.ERROR_MESSAGE);
            showUnavailableMessage();
            return;
        }

        // Start the stream only after ensuring the media player component is ready
        mediaPlayer.media().play(rtspUrl);

        // Check after a short delay to confirm the stream is playing
        Timer statusCheckTimer = new Timer(2000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ((Timer) e.getSource()).stop(); // Stop the status check timer

                if (!mediaPlayer.status().isPlaying()) {
                    System.out.println("Failed to start stream.");
                    showUnavailableMessage();
                } else {
                    System.out.println("Stream started successfully.");
                }
            }
        });
        statusCheckTimer.setRepeats(false);
        statusCheckTimer.start();
        });
    }


    
    // Show "Connecting..." message
    private void showConnectingMessage() {
        streamPanel.removeAll();
        streamPanel.setBackground(java.awt.Color.DARK_GRAY);
        JLabel connectingLabel = new JLabel("Connecting...", SwingConstants.CENTER);
        connectingLabel.setFont(connectingLabel.getFont().deriveFont(16.0f));
        connectingLabel.setForeground(java.awt.Color.WHITE);
        streamPanel.add(connectingLabel, BorderLayout.CENTER);
        streamPanel.revalidate();
        streamPanel.repaint();
    }
    
    // Show "Stream Unavailable" message
    private void showUnavailableMessage() {
        streamPanel.removeAll();
        streamPanel.setBackground(java.awt.Color.DARK_GRAY);
        JLabel unavailableLabel = new JLabel("Stream Unavailable", SwingConstants.CENTER);
        unavailableLabel.setFont(unavailableLabel.getFont().deriveFont(16.0f));
        unavailableLabel.setForeground(java.awt.Color.RED);
        streamPanel.add(unavailableLabel, BorderLayout.CENTER);
        streamPanel.revalidate();
        streamPanel.repaint();
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMenu1 = new javax.swing.JMenu();
        enterRTSP = new javax.swing.JButton();
        rtspIPLabel = new javax.swing.JLabel();
        streamPanel = new javax.swing.JPanel();
        rtspIPComboBox = new javax.swing.JComboBox<>();
        portComboBox = new javax.swing.JComboBox<>();
        portLabel = new javax.swing.JLabel();
        sidebar_dsb1 = new gpsystem.sidebar_dsb();
        captureChkBox = new javax.swing.JCheckBox();
        serverChkBox = new javax.swing.JCheckBox();

        jMenu1.setText("jMenu1");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        enterRTSP.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        enterRTSP.setText("Submit");
        enterRTSP.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        enterRTSP.setDefaultCapable(false);
        enterRTSP.setMaximumSize(new java.awt.Dimension(72, 23));
        enterRTSP.setMinimumSize(new java.awt.Dimension(72, 23));
        enterRTSP.setPreferredSize(new java.awt.Dimension(72, 23));
        enterRTSP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enterRTSPActionPerformed(evt);
            }
        });

        rtspIPLabel.setText("RTSP IP:");

        streamPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 51, 102), 2));
        streamPanel.setMinimumSize(new java.awt.Dimension(1280, 720));
        streamPanel.setPreferredSize(new java.awt.Dimension(0, 0));

        javax.swing.GroupLayout streamPanelLayout = new javax.swing.GroupLayout(streamPanel);
        streamPanel.setLayout(streamPanelLayout);
        streamPanelLayout.setHorizontalGroup(
            streamPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        streamPanelLayout.setVerticalGroup(
            streamPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 716, Short.MAX_VALUE)
        );

        rtspIPComboBox.setEditable(true);
        rtspIPComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "192.168.0.26" }));
        rtspIPComboBox.setToolTipText("Please enter a valid IP address.");
        rtspIPComboBox.setName(""); // NOI18N
        rtspIPComboBox.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                rtspIPComboBoxKeyPressed(evt);
            }
        });

        portComboBox.setEditable(true);
        portComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "554" }));
        portComboBox.setToolTipText("Please enter a valid port number.");
        portComboBox.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                portComboBoxKeyPressed(evt);
            }
        });

        portLabel.setText("Port:");

        captureChkBox.setText("Capture");
        captureChkBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                captureChkBoxActionPerformed(evt);
            }
        });

        serverChkBox.setText("Server");
        serverChkBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                serverChkBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(sidebar_dsb1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(streamPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 1298, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(rtspIPLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rtspIPComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(portLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(portComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(enterRTSP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 794, Short.MAX_VALUE)
                        .addComponent(serverChkBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(captureChkBox)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(streamPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 720, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rtspIPLabel)
                    .addComponent(rtspIPComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(portLabel)
                    .addComponent(portComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(enterRTSP, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(captureChkBox)
                    .addComponent(serverChkBox))
                .addContainerGap())
            .addComponent(sidebar_dsb1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void enterRTSPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enterRTSPActionPerformed
        String ip = (String) rtspIPComboBox.getSelectedItem();
        String port = (String) portComboBox.getSelectedItem();

        if (ip == null || ip.trim().isEmpty() || port == null || port.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both IP and port.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Construct the full RTSP URL dynamically
        String rtspUrl = "rtsp://" + ip + ":" + port;

        addToRecentIPs(rtspUrl); // Add the full URL to recent IPs
        startStream(rtspUrl); // Start streaming only on Submit
    }//GEN-LAST:event_enterRTSPActionPerformed

    private void rtspIPComboBoxKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_rtspIPComboBoxKeyPressed
        if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
            portComboBox.requestFocusInWindow();
        }                                       
    }//GEN-LAST:event_rtspIPComboBoxKeyPressed

    private void portComboBoxKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_portComboBoxKeyPressed
        if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
            enterRTSP.doClick();
        }
    }//GEN-LAST:event_portComboBoxKeyPressed

    private void serverChkBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_serverChkBoxActionPerformed
        if (serverChkBox.isSelected()) {
            if (serverInstance == null) {
                serverThread = new Thread(() -> {
                    try {
                        serverInstance = new detection_server();
                        System.out.println("Detection Server started.");
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(this, "Failed to start Detection Server.", "Error", JOptionPane.ERROR_MESSAGE);
                        e.printStackTrace();
                        serverChkBox.setSelected(false); // Reset checkbox
                    }
                });
                serverThread.start();
            }
        } else {
            // Stop the detection server
            if (serverInstance != null) {
                serverInstance.stop();
                serverInstance = null;
                System.out.println("Detection Server stopped.");
            }
        }
    }//GEN-LAST:event_serverChkBoxActionPerformed

    private void captureChkBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_captureChkBoxActionPerformed
        //        if (captureChkBox.isSelected()) {
            //            // Enable RTSP capture detection only if the stream is active
            //            if (rtsp_capture != null && mediaPlayer.status().isPlaying()) {
                //                rtsp_capture.enableDetection();
                //                System.out.println("RTSP Capture detection enabled.");
                //            } else {
                //                System.out.println("Cannot enable detection. Ensure the stream is active.");
                //                captureChkBox.setSelected(false); // Reset checkbox
                //            }
            //        } else {
            //            // Disable RTSP capture detection
            //            if (rtsp_capture != null) {
                //                rtsp_capture.disableDetection();
                //                System.out.println("RTSP Capture detection disabled.");
                //            }
            //        }
        if (captureChkBox.isSelected()) {
            // Enable RTSP capture detection
            if (rtsp_capture != null) {
                rtsp_capture.enableDetection();
                System.out.println("RTSP Capture detection enabled.");
            }
        } else {
            // Disable RTSP capture detection
            if (rtsp_capture != null) {
                rtsp_capture.disableDetection();
                System.out.println("RTSP Capture detection disabled.");
            }
        }
    }//GEN-LAST:event_captureChkBoxActionPerformed

    private void addToRecentIPs(String rtspUrl) {
        if (recentIPs.contains(rtspUrl)) {
            recentIPs.remove(rtspUrl); // Move existing IP to the top
        }
        recentIPs.addFirst(rtspUrl); // Add the full RTSP URL to recent IPs
        if (recentIPs.size() > maxRecent) {
            recentIPs.removeLast(); // Remove oldest if we exceed maxRecent
        }
        updateRecentIPs(); // Refresh JComboBox with updated IP list only
    }
    
    private void updateRecentIPs() {
        rtspIPComboBox.removeAllItems();
        portComboBox.removeAllItems();

        LinkedList<String> uniqueIPs = new LinkedList<>();
        LinkedList<String> uniquePorts = new LinkedList<>();

        for (String url : recentIPs) {
            // Extract IP and Port from the full URL
            String[] parts = url.replace("rtsp://", "").split(":");
            if (parts.length == 2) {
                String ip = parts[0];
                String port = parts[1];

                // Add IP to ComboBox only if it's unique
                if (!uniqueIPs.contains(ip)) {
                    uniqueIPs.add(ip);
                    rtspIPComboBox.addItem(ip);
                }

                // Add port to ComboBox only if it's unique
                if (!uniquePorts.contains(port)) {
                    uniquePorts.add(port);
                    portComboBox.addItem(port);
                }
            }
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(home_dsb.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(home_dsb.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(home_dsb.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(home_dsb.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new home_dsb().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox captureChkBox;
    private javax.swing.JButton enterRTSP;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JComboBox<String> portComboBox;
    private javax.swing.JLabel portLabel;
    private javax.swing.JComboBox<String> rtspIPComboBox;
    private javax.swing.JLabel rtspIPLabel;
    private javax.swing.JCheckBox serverChkBox;
    private gpsystem.sidebar_dsb sidebar_dsb1;
    private javax.swing.JPanel streamPanel;
    // End of variables declaration//GEN-END:variables
}