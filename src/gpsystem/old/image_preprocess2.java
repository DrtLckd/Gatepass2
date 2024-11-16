package gpsystem.old;

import java.util.ArrayList;
import java.util.List;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class image_preprocess2 {

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
        return Imgcodecs.imread(filePath);
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

    // Apply ROI Cropping
    public static Mat cropToROI(Mat image) {
        // Define the ROI based on estimated license plate location in the image
        int x = (int)(image.width() * 0.05);  // Adjust ROI based on your observations
        int y = (int)(image.height() * 0.3);
        int width = (int)(image.width() * 0.86);
        int height = (int)(image.height() * 0.6);

        // Ensure ROI is within the image bounds
        Rect roiRect = new Rect(x, y, width, height);
        return new Mat(image, roiRect);
    }

    // Binary Thresholding (Strict Threshold)
    public static Mat applyStrictThreshold(Mat image) {
        Mat binaryImage = new Mat();
        Imgproc.threshold(image, binaryImage, 127, 255, Imgproc.THRESH_BINARY);
        return binaryImage;
    }

    // Fill in bounded areas to make text solid
    public static Mat fillBoundedAreas(Mat image) {
        Mat filledImage = image.clone();

        // Find contours
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(filledImage, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // Fill each contour to make letters/numbers solid
        for (MatOfPoint contour : contours) {
            Imgproc.drawContours(filledImage, List.of(contour), -1, new Scalar(255), Core.FILLED);
        }

        return filledImage;
    }
    
    // Main pre-processing pipeline
    public static void preprocessAndSave(String inputPath, String outputPath) {
        Mat image = loadImage(inputPath);
        if (image.empty()) {
            System.out.println("Failed to load image.");
            return;
        }

        // Step 1: Apply Grayscale
        image = convertToGrayscale(image);
        Imgcodecs.imwrite("D:\\Downloads\\Thesis\\javaTest\\grayscale.jpg", image);

        // Step 2: Apply Contrast Adjustment
        image = adjustContrast(image, 1.3, 20);
        Imgcodecs.imwrite("D:\\Downloads\\Thesis\\javaTest\\contrast_adjusted.jpg", image);

        // Step 3: Apply Noise Reduction
        image = reduceNoise(image);
        Imgcodecs.imwrite("D:\\Downloads\\Thesis\\javaTest\\noise_reduced.jpg", image);

        // Step 4: Crop to predefined ROI
        Mat croppedImage = cropToROI(image);
        Imgcodecs.imwrite("D:\\Downloads\\Thesis\\javaTest\\cropped_plate.jpg", croppedImage);

        // Step 5: Apply strict binary threshold for black and white
        Mat binaryImage = applyStrictThreshold(croppedImage);
        Imgcodecs.imwrite("D:\\Downloads\\Thesis\\javaTest\\thresholded.jpg", binaryImage);

        // Step 6: Fill bounded areas to make the text solid
//        Mat finalImage = fillBoundedAreas(binaryImage);
        Mat finalImage = binaryImage;
        Imgcodecs.imwrite(outputPath, finalImage);
        System.out.println("Image processed and saved to: " + outputPath);
    }


    public static void main(String[] args) {
        String inputPath = "D:\\Downloads\\Thesis\\task3\\t3_ (36).jpg";
        String outputPath = "D:\\Downloads\\Thesis\\javaTest\\processed_image.jpg";

        preprocessAndSave(inputPath, outputPath);
    }
}