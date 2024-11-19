package gpsystem;

import java.io.File;
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
        Imgproc.threshold(image, binaryImage, 50, 255, Imgproc.THRESH_BINARY);
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

    public static void preprocessAndSave(String inputPath, String finalPath, int xmin, int ymin, int xmax, int ymax) {
        Mat image = loadImage(inputPath);
        if (image.empty()) {
            System.err.println("Failed to load image for preprocessing: " + inputPath);
            return;
        }

        try {
            // Crop the image to the region of interest (ROI)
            Rect roi = new Rect(xmin, ymin, xmax - xmin, ymax - ymin);
            Mat croppedImage = new Mat(image, roi);
            String croppedPath = finalPath.replace("final_image", "cropped_image");
            Imgcodecs.imwrite(croppedPath, croppedImage);
            System.out.println("Cropped image saved to: " + croppedPath);

            // Proceed with grayscale, contrast adjustment, etc.
            // Grayscale Conversion
            Mat grayImage = convertToGrayscale(croppedImage);
            String grayPath = finalPath.replace("final_image", "grayscale");
            Imgcodecs.imwrite(grayPath, grayImage);
            System.out.println("Grayscale image saved to: " + grayPath);

            // Contrast Adjustment
            Mat contrastImage = adjustContrast(grayImage, 1.3, 20);
            String contrastPath = finalPath.replace("final_image", "contrast_adjusted");
            Imgcodecs.imwrite(contrastPath, contrastImage);
            System.out.println("Contrast-adjusted image saved to: " + contrastPath);

            // Noise Reduction
            Mat denoisedImage = reduceNoise(contrastImage);
            String denoisedPath = finalPath.replace("final_image", "noise_reduced");
            Imgcodecs.imwrite(denoisedPath, denoisedImage);
            System.out.println("Denoised image saved to: " + denoisedPath);

            //Binary Thresholding (Temporarily Disabled)
            Mat binaryImage = applyStrictThreshold(denoisedImage);
            Imgcodecs.imwrite(finalPath, binaryImage);
            System.out.println("Final processed image saved to: " + finalPath);

            // Save the denoised image as the final image (without thresholding)
            Imgcodecs.imwrite(finalPath, denoisedImage);
            System.out.println("Final processed image saved to: " + finalPath);
        } catch (Exception e) {
            System.err.println("Error during image preprocessing: " + e.getMessage());
        }
    }



    public static void main(String[] args) {
        // Ensure necessary directories exist
        new File("captures").mkdirs();
        new File("processed").mkdirs();

        // Static test values
        String inputPath = "captures/screengrab.jpg";
        String outputPath = "processed/final_image.jpg";
        int xmin = 463, ymin = 378, xmax = 1200, ymax = 815;

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
        ocr_python.runOCR(outputPath);
    }
}