package gpsystem;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import javax.swing.border.EmptyBorder;

public class gallery_dsb extends JFrame {

    private JPanel filePanel;
    private JTextArea textArea;
    private JButton backButton;
    private static final String IMAGE_DIR = "captures";         
    private static final int THUMBNAIL_WIDTH = 150; // Width of each thumbnail
    private static final int THUMBNAIL_HEIGHT = 100; // Height of each thumbnail
    private File currentDirectory; // Keep track of the current directory
    private Map<File, ImageIcon> thumbnailCache = new HashMap<>(); // Cache for thumbnails

  
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
        setLocationRelativeTo(null);
        
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
        filePanel.setLayout(new GridBagLayout());
        JScrollPane scrollPane = new JScrollPane(filePanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        getContentPane().add(scrollPane, BorderLayout.CENTER);
    }

    private void createTextArea() {
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setText("Click on a picture to see its name...");
        JScrollPane textScrollPane = new JScrollPane(textArea);
        textScrollPane.setPreferredSize(new Dimension(getWidth() / 4, getHeight() / 6));
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

    private int calculateColumns() {
        int availableWidth = filePanel.getWidth(); // Dynamically calculate available width
        return Math.max(1, availableWidth / (THUMBNAIL_WIDTH + 20)); // Ensure at least 1 column
    }

    private ImageIcon getThumbnail(File file) {
        return thumbnailCache.computeIfAbsent(file, f -> {
            try {
                BufferedImage img = ImageIO.read(f);
                if (img != null) {
                    Image scaledImg = img.getScaledInstance(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, Image.SCALE_SMOOTH);
                    return new ImageIcon(scaledImg);
                } else {
                    return new ImageIcon(); // Return empty icon instead of crashing
                }
            } catch (Exception e) {
                System.err.println("Error loading thumbnail: " + f.getName());
            }
            return null;
        });
    }

    
    private void loadFiles(File directory) {
        if (!directory.exists() || !directory.isDirectory()) {
            JOptionPane.showMessageDialog(this, "Invalid directory: " + directory.getAbsolutePath(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        currentDirectory = directory;
        updateBreadcrumbs();

        filePanel.removeAll();
        File[] files = directory.listFiles(file -> file.isDirectory() || file.getName().matches(".*\\.(jpg|png|jpeg)$"));

        if (files == null || files.length == 0) {
            filePanel.add(new JLabel("No images or folders to display."));
        } else {
            int columns = calculateColumns();
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            gbc.gridx = 0;
            gbc.gridy = 0;

            for (File file : files) {
                if (file.isDirectory()) {
                    addFolderPreview(file, gbc);
                } else if (file.isFile()) {
                    addFilePreview(file, gbc);
                }

                gbc.gridx++;
                if (gbc.gridx >= columns) {
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
        folderLabel.setFont(new Font("Arial", Font.BOLD, 12));
        folderLabel.setToolTipText("Folder: " + folder.getName());
        folderLabel.setHorizontalAlignment(SwingConstants.LEFT);
        folderLabel.setBorder(BorderFactory.createLineBorder(Color.BLUE));
        folderLabel.setPreferredSize(new Dimension(THUMBNAIL_WIDTH, 30));
        folderLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                loadFiles(folder);
            }
        });

        filePanel.add(folderLabel, gbc);
    }

    private void addFilePreview(File file, GridBagConstraints gbc) {
        JLabel imageLabel = new JLabel("Loading...");
        filePanel.add(imageLabel, gbc);

        SwingWorker<ImageIcon, Void> worker = new SwingWorker<>() {
            @Override
            protected ImageIcon doInBackground() {
                return getThumbnail(file);
            }

            @Override
            protected void done() {
                try {
                    imageLabel.setIcon(get());
                    imageLabel.setText(null); // Remove "Loading..."
                    imageLabel.setToolTipText(file.getName());
                    imageLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                    imageLabel.addMouseListener(new java.awt.event.MouseAdapter() {
                        @Override
                        public void mouseClicked(java.awt.event.MouseEvent e) {
                            textArea.setText(file.getName());
                        }
                    });
                } catch (Exception e) {
                    imageLabel.setText("Failed to load");
                    System.err.println("Error loading file: " + file.getName());
                }
            }
        };

        worker.execute();
    }

    private void updateBreadcrumbs() {
        StringBuilder breadcrumbs = new StringBuilder();
        File parent = currentDirectory;
        while (parent != null) {
            if (breadcrumbs.length() > 0) {
                breadcrumbs.insert(0, " > ");
            }
            breadcrumbs.insert(0, parent.getName());
            parent = parent.getParentFile();
        }
        textArea.setText(breadcrumbs.toString());
    }
        
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        sidebar_dsb1 = new gpsystem.sidebar_dsb();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(sidebar_dsb1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 584, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(sidebar_dsb1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            gallery_dsb gallery = new gallery_dsb();
            gallery.setVisible(true);

            SwingUtilities.invokeLater(() -> {
                gallery.getContentPane().revalidate();
                gallery.getContentPane().repaint();
            });
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private gpsystem.sidebar_dsb sidebar_dsb1;
    // End of variables declaration//GEN-END:variables
}
