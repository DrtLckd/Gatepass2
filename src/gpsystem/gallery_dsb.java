package gpsystem;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import javax.swing.border.EmptyBorder;

public class gallery_dsb extends JFrame {

    private JPanel filePanel;
    private JTextArea textArea;
    private JButton backButton;
    private static final String IMAGE_DIR = "C:\\Users\\predator 300\\Pictures"; // Path to the directory with images
    private static final int THUMBNAIL_WIDTH = 150; // Width of each thumbnail
    private static final int THUMBNAIL_HEIGHT = 100; // Height of each thumbnail
    private File currentDirectory; // Keep track of the current directory

  
  public gallery_dsb() {
        setTitle("Gallery - Embedded File Explorer");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create a JPanel as the main content container
        JPanel contentPane = new JPanel(new BorderLayout(10, 10));
        contentPane.setBorder(new EmptyBorder(30, 30, 30, 30)); // Add 5% margins
        setContentPane(contentPane); // Set this panel as the content pane

        // Initialize components
        initializeComponents();

        // Load files from the default directory
        currentDirectory = new File(IMAGE_DIR); // Replace with your directory path
        loadFiles(currentDirectory);
    }

    private void initializeComponents() {
        createScrollableFilePanel();
        createTextArea();
        createBackButton();
    }

    private void createScrollableFilePanel() {
        filePanel = new JPanel();
        filePanel.setLayout(new GridBagLayout()); // Use GridBagLayout for flexible grid
        filePanel.setAlignmentY(Component.TOP_ALIGNMENT); // Force top alignment

        JScrollPane scrollPane = new JScrollPane(filePanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        getContentPane().add(scrollPane, BorderLayout.CENTER);
    }

    private void createTextArea() {
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setText("Click on a picture to see its name...");
        JScrollPane textScrollPane = new JScrollPane(textArea);
        textScrollPane.setPreferredSize(new Dimension(getWidth() / 4, getHeight() / 4));
        getContentPane().add(textScrollPane, BorderLayout.SOUTH);
    }

    private void createBackButton() {
        backButton = new JButton("Back");
        backButton.addActionListener(e -> goBack());
        JPanel topRightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topRightPanel.add(backButton);
        getContentPane().add(topRightPanel, BorderLayout.NORTH);
    }

    private void goBack() {
        if (currentDirectory.getParentFile() != null) {
            currentDirectory = currentDirectory.getParentFile();
            loadFiles(currentDirectory);
        } else {
            textArea.setText("Already at the root directory.");
        }
    }

    private void loadFiles(File directory) {
    if (!directory.exists() || !directory.isDirectory()) {
        JOptionPane.showMessageDialog(this, "Invalid directory: " + directory.getAbsolutePath(), "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    currentDirectory = directory; // Update the current directory
    filePanel.removeAll(); // Clear existing content

    File[] files = directory.listFiles(file -> file.isDirectory() || file.getName().endsWith(".jpg") || file.getName().endsWith(".png") || file.getName().endsWith(".jpeg"));

    if (files == null || files.length == 0) {
        filePanel.add(new JLabel("No images or folders to display."));
    } else {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Spacing between items
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0; // No horizontal stretching
        gbc.weighty = 0; // No vertical stretching
        gbc.anchor = GridBagConstraints.NORTHWEST; // Align items to the top-left
        gbc.fill = GridBagConstraints.NONE; // Prevent resizing of items

        for (File file : files) {
            if (file.isDirectory()) {
                addFolderPreview(file, gbc);
            } else if (file.isFile()) {
                addFilePreview(file, gbc);
            }
            gbc.gridx++;
            if (gbc.gridx >= 5) { // Move to the next row after 5 items
                gbc.gridx = 0;
                gbc.gridy++;
            }
        }
    }

    filePanel.revalidate();
    filePanel.repaint();
}


    private void addFolderPreview(File folder, GridBagConstraints gbc) {
        JLabel folderLabel = new JLabel("📁 " + folder.getName());
        folderLabel.setToolTipText("Folder: " + folder.getName());
        folderLabel.setHorizontalAlignment(SwingConstants.LEFT);
        folderLabel.setVerticalAlignment(SwingConstants.CENTER);
        folderLabel.setBorder(BorderFactory.createLineBorder(Color.BLUE));
        folderLabel.setPreferredSize(new Dimension(120, 60));

        folderLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                loadFiles(folder);
            }
        });

        filePanel.add(folderLabel, gbc);
    }

    private void addFilePreview(File file, GridBagConstraints gbc) {
        try {
            BufferedImage img = ImageIO.read(file);
            if (img != null) {
                Image scaledImg = img.getScaledInstance(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, Image.SCALE_SMOOTH);
                JLabel imageLabel = new JLabel(new ImageIcon(scaledImg));
                imageLabel.setToolTipText(file.getName());
                imageLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                imageLabel.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        textArea.setText(file.getName());
                    }
                });
                filePanel.add(imageLabel, gbc);
            }
        } catch (Exception e) {
            System.err.println("Error loading file: " + file.getName());
        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        JFrame frame = new JFrame("Image Gallery");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);

        // Add the gallery component to the frame
        frame.add(new JScrollPane(new gallery_dsb())); // Add scrollable functionality
        frame.setVisible(true);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
