package gpsystem;

import java.util.ArrayList;
import java.util.List;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class image_preprocess {

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
    
    // Grayscale Conversion
    public static Mat convertToGrayscale(Mat image) {
        Mat grayImage = new Mat();
        Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
        return grayImage;
    }

    // Contrast Adjustment
    public static Mat adjustContrast(Mat image, double alpha, double beta) {
        Mat contrastImage = new Mat();
        image.convertTo(contrastImage, -1, alpha, beta); // alpha: contrast, beta: brightness
        return contrastImage;
    }

    // Noise Reduction
    public static Mat reduceNoise(Mat image) {
        Mat denoisedImage = new Mat();
        Imgproc.GaussianBlur(image, denoisedImage, new Size(5, 5), 0);
        return denoisedImage;
    }

//    public static Mat cropToROI(Mat image, int xmin, int ymin, int xmax, int ymax) {
//        try {
//            Rect roi = new Rect(xmin, ymin, xmax - xmin, ymax - ymin);
//            return new Mat(image, roi);
//        } catch (CvException e) {
//            System.err.println("Invalid ROI for cropping: " + e.getMessage());
//            return new Mat(); // Return an empty Mat for invalid ROI
//        }
//    }


    // Binary Thresholding (Strict Threshold)
    public static Mat applyStrictThreshold(Mat image) {
        Mat binaryImage = new Mat();
        Imgproc.threshold(image, binaryImage, 127, 255, Imgproc.THRESH_BINARY);
        return binaryImage;
    }
//
//    // Fill in bounded areas to make text solid
//    public static Mat fillBoundedAreas(Mat image) {
//        Mat filledImage = image.clone();
//
//        // Find contours
//        List<MatOfPoint> contours = new ArrayList<>();
//        Mat hierarchy = new Mat();
//        Imgproc.findContours(filledImage, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
//
//        // Fill each contour to make letters/numbers solid
//        for (MatOfPoint contour : contours) {
//            Imgproc.drawContours(filledImage, List.of(contour), -1, new Scalar(255), Core.FILLED);
//        }
//
//        return filledImage;
//    }

    // Preprocess and Save Image
    public static void preprocessAndSave(String inputPath, String outputPath, int xmin, int ymin, int xmax, int ymax) {
        Mat image = loadImage(inputPath);
        if (image.empty()) {
            return;
        }

        try {
            // Grayscale Conversion
            Mat grayImage = convertToGrayscale(image);
            Imgcodecs.imwrite("processed/grayscale.jpg", grayImage);

            // Contrast Adjustment
            Mat contrastImage = adjustContrast(grayImage, 1.3, 20);
            Imgcodecs.imwrite("processed/contrast_adjusted.jpg", contrastImage);

            // Noise Reduction
            Mat denoisedImage = reduceNoise(contrastImage);
            Imgcodecs.imwrite("processed/noise_reduced.jpg", denoisedImage);

//            // Cropping Using ROI
//            Mat croppedImage = cropToROI(denoisedImage, xmin, ymin, xmax, ymax);
//            Imgcodecs.imwrite("processed/cropped_image.jpg", croppedImage);

            // Binary Thresholding
            Mat binaryImage = applyStrictThreshold(contrastImage); //croppedImage
            Imgcodecs.imwrite(outputPath, binaryImage);

            System.out.println("Image processed and saved to: " + outputPath);
        } catch (Exception e) {
            System.err.println("Error during image preprocessing: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // For testing: Adjust paths and coordinates as necessary
        String inputPath = "captures/screengrab.jpg";
        String outputPath = "processed/final_image.jpg";
        int xmin = 463, ymin = 378, xmax = 1200, ymax = 815;

        preprocessAndSave(inputPath, outputPath, xmin, ymin, xmax, ymax);
    }
}