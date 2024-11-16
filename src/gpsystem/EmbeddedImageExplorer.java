/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gpsystem;

/**
 *
 * @author predator 300
 */
import javax.swing.*;
import java.awt.*;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class EmbeddedImageExplorer extends JFrame {

    private JPanel filePanel;

    public EmbeddedImageExplorer() {
        setTitle("Embedded Image Explorer");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set layout for the main frame
        setLayout(new BorderLayout());

        // Create a scrollable panel to display files
        filePanel = new JPanel();
        filePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JScrollPane scrollPane = new JScrollPane(filePanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // Load files from the specified directory
        loadFiles(new File("C:\\Users\\YourUsername\\Pictures")); // Replace with your directory

        add(scrollPane, BorderLayout.CENTER);
        setVisible(true);
    }

    private void loadFiles(File directory) {
        if (!directory.exists() || !directory.isDirectory()) {
            JOptionPane.showMessageDialog(this, "Invalid directory: " + directory.getAbsolutePath(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Clear existing content
        filePanel.removeAll();

        // List files in the directory
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    // Create a preview for the file
                    addFilePreview(file);
                }
            }
        }

        // Refresh the panel
        filePanel.revalidate();
        filePanel.repaint();
    }

    private void addFilePreview(File file) {
        try {
            // Check if the file is an image
            BufferedImage img = ImageIO.read(file);
            if (img != null) {
                // Create a scaled preview image
                Image scaledImg = img.getScaledInstance(100, 100, Image.SCALE_SMOOTH);

                // Create a label with the image
                JLabel imageLabel = new JLabel(new ImageIcon(scaledImg));
                imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
                imageLabel.setVerticalAlignment(SwingConstants.CENTER);
                imageLabel.setToolTipText(file.getName());

                // Add a border for better visibility
                imageLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

                // Add click listener (optional)
                imageLabel.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseClicked(java.awt.event.MouseEvent evt) {
                        JOptionPane.showMessageDialog(null, "You clicked: " + file.getName());
                    }
                });

                // Add the image preview to the panel
                filePanel.add(imageLabel);
            }
        } catch (Exception e) {
            System.err.println("Error loading file: " + file.getAbsolutePath());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(EmbeddedImageExplorer::new);
    }
}

