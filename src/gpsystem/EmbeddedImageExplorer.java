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
        setTitle("Embedded File Explorer");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create the scrollable file panel
        createScrollableFilePanel();

        // Load files from the default directory
        loadFiles(new File("C:\\Users\\predator 300\\Pictures\\Camera Roll")); // Replace with your directory path

        setVisible(true);
    }

    private void createScrollableFilePanel() {
        filePanel = new JPanel();
        
        // Use GridLayout for 5 items per row
        filePanel.setLayout(new GridLayout(0, 5, 10, 10)); // 0 rows (dynamic), 5 columns, with spacing

        // Add the file panel to a scrollable view
        JScrollPane scrollPane = new JScrollPane(filePanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // Add the scroll pane to the main frame
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadFiles(File directory) {
        if (!directory.exists() || !directory.isDirectory()) {
            JOptionPane.showMessageDialog(this, "Invalid directory: " + directory.getAbsolutePath(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        filePanel.removeAll(); // Clear existing content

        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    addFolderPreview(file); // Add folders
                } else if (file.isFile()) {
                    addFilePreview(file); // Add files
                }
            }
        }

        filePanel.revalidate();
        filePanel.repaint();
    }

    private void addFolderPreview(File folder) {
        JLabel folderLabel = new JLabel("📁 " + folder.getName());
        folderLabel.setToolTipText("Folder: " + folder.getName());
        folderLabel.setHorizontalAlignment(SwingConstants.CENTER);
        folderLabel.setBorder(BorderFactory.createLineBorder(Color.BLUE));

        folderLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                loadFiles(folder); // Open folder when clicked
            }
        });

        filePanel.add(folderLabel);
    }

    private void addFilePreview(File file) {
        try {
            BufferedImage img = ImageIO.read(file);
            if (img != null) {
                Image scaledImg = img.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                JLabel imageLabel = new JLabel(new ImageIcon(scaledImg));
                imageLabel.setToolTipText(file.getName());
                imageLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

                filePanel.add(imageLabel);
            }
        } catch (Exception e) {
            System.err.println("Error loading file: " + file.getName());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(EmbeddedImageExplorer::new);
    }
}


