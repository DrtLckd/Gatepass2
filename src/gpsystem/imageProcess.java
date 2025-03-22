package gpsystem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;

public class imageProcess {

    static {
        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            System.out.println("OpenCV library loaded successfully.");
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Failed to load OpenCV library: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Load image
    public static Mat loadImage(String filePath) {
        Mat image = Imgcodecs.imread(filePath);
        if (image.empty()) {
            System.err.println("Failed to load image at path: " + filePath);
        }
        return image;
    }

//    public static Mat resizeImage(Mat image, int newWidth, int newHeight) {
//        Mat resizedImage = new Mat();
//        Imgproc.resize(image, resizedImage, new Size(newWidth, newHeight));
//        return resizedImage;
//}

    // Grayscale Conversion
    public static Mat convertToGrayscale(Mat image) {
        Mat grayImage = new Mat();
        Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
        return grayImage;
    }

    // Contrast Adjustment (Adaptive Histogram Equalization)
    public static Mat applyCLAHE(Mat image) {
        Mat enhanced = new Mat();
        CLAHE clahe = Imgproc.createCLAHE(2.5, new Size(8, 8));
        clahe.apply(image, enhanced);
        return enhanced;
    }

    // Noise Reduction
    public static Mat reduceNoise(Mat image) {
        Mat denoisedImage = new Mat();
        Imgproc.GaussianBlur(image, denoisedImage, new Size(5, 5), 0);
        return denoisedImage;
    }

//    // Binary Thresholding (Strict Threshold)
//    public static Mat applyStrictThreshold(Mat image) {
//        Mat binaryImage = new Mat();
//        Imgproc.threshold(image, binaryImage, 50, 255, Imgproc.THRESH_BINARY);
//        return binaryImage;
//    }
      // Binary Thresholding (Otsu's Adaptive Threshold)
    public static Mat applyAdaptiveThreshold(Mat image) {
        Mat binaryImage = new Mat();
        Imgproc.adaptiveThreshold(image, binaryImage, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 15, 5);
        return binaryImage;
    }
        
//    public static Mat applyPerspectiveCorrection(Mat croppedImage) {
//        // Detect corners of the cropped image (assuming a rectangular ROI)
//        int width = croppedImage.width();
//        int height = croppedImage.height();
//
//        // Define the source points (corners of the cropped ROI)
//        Point[] srcPoints = new Point[]{
//            new Point(0, 0),             // Top-left
//            new Point(width - 1, 0),     // Top-right
//            new Point(width - 1, height - 1), // Bottom-right
//            new Point(0, height - 1)     // Bottom-left
//        };
//
//        // Define the destination points (aligned rectangle)
//        Point[] dstPoints = new Point[]{
//            new Point(0, 0),             // Top-left
//            new Point(width - 1, 0),     // Top-right
//            new Point(width - 1, height - 1), // Bottom-right
//            new Point(0, height - 1)     // Bottom-left
//        };
//
//        // Create Mat objects for source and destination points
//        MatOfPoint2f srcMat = new MatOfPoint2f(srcPoints);
//        MatOfPoint2f dstMat = new MatOfPoint2f(dstPoints);
//
//        // Compute the perspective transformation matrix
//        Mat perspectiveTransform = Imgproc.getPerspectiveTransform(srcMat, dstMat);
//
//        // Apply the perspective transformation
//        Mat correctedImage = new Mat();
//        Imgproc.warpPerspective(croppedImage, correctedImage, perspectiveTransform, new Size(width, height));
//
//        return correctedImage;
//    }
    // Perspective Correction (Automatic Detection of Corners)
    public static Mat applyPerspectiveCorrection(Mat image) {
        int width = image.width();
        int height = image.height();

        if (width <= 0 || height <= 0) {
            System.err.println("Invalid image dimensions for perspective correction.");
            return image;
        }

        // Apply Canny edge detection to find edges
        Mat edges = new Mat();
        Imgproc.Canny(image, edges, 100, 200); // Adjust thresholds if needed

        // Visualize the edges (for debugging purposes)
        Imgcodecs.imwrite("edges_detected.jpg", edges);  // Save edge detection output for visualization
    
        // Find contours
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(edges, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // Visualize contours (for debugging purposes)
        Mat contourImage = new Mat(image.size(), CvType.CV_8UC3, new Scalar(0, 0, 0)); // Black image for drawing
        Imgproc.drawContours(contourImage, contours, -1, new Scalar(255, 0, 0), 2);  // Draw all contours in blue
        Imgcodecs.imwrite("contours_detected.jpg", contourImage);  // Save contour image for visualization

        // Find the largest contour (this is likely the object of interest, such as a license plate)
        MatOfPoint largestContour = null;
        for (MatOfPoint contour : contours) {
            double area = Imgproc.contourArea(contour);
            if (area > 500) {  // Set a threshold for contour area to ignore small contours
                Rect boundingRect = Imgproc.boundingRect(contour);
                double aspectRatio = (double)boundingRect.width / boundingRect.height;
                // Only consider contours that are roughly rectangular
                if (aspectRatio > 1.0 && aspectRatio < 3.0) { // Aspect ratio of a license plate
                    largestContour = contour;
                }
            }
        }

        // Approximate the largest contour to a quadrilateral (the license plate)
        if (largestContour != null) {
            MatOfPoint2f approx = new MatOfPoint2f();
            MatOfPoint2f contour2f = new MatOfPoint2f(largestContour.toArray());
            double peri = Imgproc.arcLength(contour2f, true);
            Imgproc.approxPolyDP(contour2f, approx, 0.02 * peri, true); // Approximate to 4 corners

            // If we find 4 corners, we can perform perspective correction
            if (approx.rows() == 4) {
                // Define source points from the detected corners
                MatOfPoint2f srcMat = approx;

                // Define destination points (fixed rectangle for perspective correction)
                MatOfPoint2f dstMat = new MatOfPoint2f(
                    new Point(0, 0),               // Top-left corner
                    new Point(width - 1, 0),       // Top-right corner
                    new Point(width - 1, height - 1),  // Bottom-right corner
                    new Point(0, height - 1)       // Bottom-left corner
                );

                // Calculate the perspective transformation matrix
                Mat perspectiveTransform = Imgproc.getPerspectiveTransform(srcMat, dstMat);

                // Apply the perspective transformation
                Mat correctedImage = new Mat();
                Imgproc.warpPerspective(image, correctedImage, perspectiveTransform, new Size(width, height));

                return correctedImage;
            } else {
                System.err.println("Failed to detect a quadrilateral.");
                return image; // Return the original image if no quadrilateral is found
            }
        } else {
            System.err.println("No contours found.");
            return image; // Return the original image if no contours are found
        }
    }



    
    public static boolean preprocessAndSave(String inputPath, String finalPath, int xmin, int ymin, int xmax, int ymax) {
        Mat image = loadImage(inputPath);
        if (image.empty()) {
            System.err.println("Failed to load image: " + inputPath);
            return false; // Preprocessing failed
        }

        try {           
            // Ensure ROI is within valid bounds
            xmin = Math.max(0, Math.min(xmin, image.cols() - 1));
            ymin = Math.max(0, Math.min(ymin, image.rows() - 1));
            xmax = Math.max(xmin + 1, Math.min(xmax, image.cols()));
            ymax = Math.max(ymin + 1, Math.min(ymax, image.rows()));
        
            if (xmax <= xmin || ymax <= ymin) {
                System.err.println("Invalid crop dimensions! [xmin=" + xmin + ", xmax=" + xmax + ", ymin=" + ymin + ", ymax=" + ymax + "]");
                return false;
            }
            
            // ✅ Extract folder from finalPath (plate_X directory)
            File outputFolder = new File(finalPath).getParentFile();
            if (!outputFolder.exists()) outputFolder.mkdirs();
            System.out.println("Saving processed images in: " + outputFolder.getAbsolutePath());
        
            // ✅ Save Cropped Image
            Mat croppedImage = new Mat(image, new Rect(xmin, ymin, xmax - xmin, ymax - ymin));
            String croppedPath = outputFolder + File.separator + "1cropped.jpg";
            Imgcodecs.imwrite(croppedPath, croppedImage);
            if (!new File(croppedPath).exists()) System.err.println("Failed to save: " + croppedPath);
            System.out.println("Cropped saved: " + croppedPath);

            // ✅ Convert to Grayscale
            Mat grayImage = convertToGrayscale(croppedImage);
            String grayPath = outputFolder + File.separator + "2grayscale.jpg";
            Imgcodecs.imwrite(grayPath, grayImage);
            if (!new File(grayPath).exists()) System.err.println("Failed to save: " + grayPath);
            System.out.println("Grayscale saved: " + grayPath);

            // ✅ Noise Reduction
            Mat denoisedImage = reduceNoise(grayImage);
            String denoisedPath = outputFolder + File.separator + "3denoised.jpg";
            Imgcodecs.imwrite(denoisedPath, denoisedImage);
            if (!new File(denoisedPath).exists()) System.err.println("Failed to save: " + denoisedPath);
            System.out.println("Noise reduced saved: " + denoisedPath);
            
            // ✅ Perspective Correction
            Mat correctedImage = applyPerspectiveCorrection(denoisedImage);
            String correctedPath = outputFolder + File.separator + "4corrected.jpg";
            Imgcodecs.imwrite(correctedPath, correctedImage);
            if (!new File(correctedPath).exists()) System.err.println("Failed to save: " + correctedPath);
            System.out.println("Perspective correction saved: " + correctedPath);

            // ✅ Contrast Adjustment
            Mat contrastImage = applyCLAHE(correctedImage);
            String contrastPath = outputFolder + File.separator + "5contrast.jpg";
            Imgcodecs.imwrite(contrastPath, contrastImage);
            if (!new File(contrastPath).exists()) System.err.println("Failed to save: " + contrastPath);
            System.out.println("Contrast saved: " + contrastPath);

            // ✅ Adaptive Thresholding
            Mat binaryImage = applyAdaptiveThreshold(contrastImage);
            String binaryPath = outputFolder + File.separator + "6binary.jpg";
            Imgcodecs.imwrite(binaryPath, binaryImage);
            if (!new File(binaryPath).exists()) System.err.println("Failed to save: " + binaryPath);
            System.out.println("Binary saved: " + binaryPath);

            // ✅ Final Processed Image
            String finalProcessedPath = outputFolder + File.separator + "final.jpg";
            Imgcodecs.imwrite(finalProcessedPath, binaryImage);
            if (!new File(finalProcessedPath).exists()) System.err.println("Failed to save: " + finalProcessedPath);
            System.out.println("Final processed image saved: " + finalProcessedPath);

            return true;
//            // Binary Thresholding (Optional)
//            Mat binaryImage = new Mat();
//            Imgproc.threshold(contrastImage, binaryImage, 0, 255, 
//                              Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
//            if (binaryImage.empty()) {
//                System.err.println("❌ Thresholding failed.");
//                return false;
//            }
//            Imgcodecs.imwrite(finalPath, binaryImage);
//            System.out.println("✅ Final processed image saved: " + finalPath);

        } catch (Exception e) {
            System.err.println("Error during image preprocessing: " + e.getMessage());
            return false;
        }
    }



    public static void main(String[] args) {
        // Ensure necessary directories exist
        new File("captures").mkdirs();
        new File("processed").mkdirs();

        // Static test values
        String inputPath = "processed/cropped_image.jpg";
        String outputPath = "processed/final_image.jpg";
        int xmin = 0, ymin = 0, xmax = 251, ymax = 137;

        // Check if input file exists
        File inputFile = new File(inputPath);
        if (!inputFile.exists()) {
            System.err.println("Error: Input file does not exist at path: " + inputPath);
            return;
        }

        // Log progress
        System.out.println("Starting preprocessing for image: " + inputPath);
        System.out.println("Output will be saved in: " + outputPath);
        System.out.println("Region of Interest: xmin=" + xmin + ", ymin=" + ymin + ", xmax=" + xmax + ", ymax=" + ymax);

        // Perform preprocessing
        preprocessAndSave(inputPath, outputPath, xmin, ymin, xmax, ymax);
        System.out.println("Preprocessing complete.");

        // Call OCR after preprocessing
        System.out.println("Starting OCR...");
        String ocrResult = ocr_python.runOCR(outputPath);
        System.out.println("Detected Text: " + ocrResult);
    }
}