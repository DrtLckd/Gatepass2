package gpsystem;

import java.io.File;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.CLAHE;
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

    public static Mat resizeImage(Mat image, int newWidth, int newHeight) {
        Mat resizedImage = new Mat();
        Imgproc.resize(image, resizedImage, new Size(newWidth, newHeight));
        return resizedImage;
}

    // Grayscale Conversion
    public static Mat convertToGrayscale(Mat image) {
        Mat grayImage = new Mat();
        Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
        return grayImage;
    }

    // Contrast Adjustment (Adaptive Histogram Equalization)
    public static Mat applyCLAHE(Mat image) {
        Mat enhanced = new Mat();
        CLAHE clahe = Imgproc.createCLAHE(2.0, new Size(8, 8));
        clahe.apply(image, enhanced);
        return enhanced;
    }

    // Noise Reduction (Median Blur)
    public static Mat reduceNoise(Mat image) {
        Mat denoisedImage = new Mat();
        Imgproc.medianBlur(image, denoisedImage, 3);
        return denoisedImage;
    }

    // Binary Thresholding (Strict Threshold)
    public static Mat applyStrictThreshold(Mat image) {
        Mat binaryImage = new Mat();
        Imgproc.threshold(image, binaryImage, 50, 255, Imgproc.THRESH_BINARY);
        return binaryImage;
    }

    public static Mat applyPerspectiveCorrection(Mat croppedImage) {
        // Detect corners of the cropped image (assuming a rectangular ROI)
        int width = croppedImage.width();
        int height = croppedImage.height();

        // Define the source points (corners of the cropped ROI)
        Point[] srcPoints = new Point[]{
            new Point(0, 0),             // Top-left
            new Point(width - 1, 0),     // Top-right
            new Point(width - 1, height - 1), // Bottom-right
            new Point(0, height - 1)     // Bottom-left
        };

        // Define the destination points (aligned rectangle)
        Point[] dstPoints = new Point[]{
            new Point(0, 0),             // Top-left
            new Point(width - 1, 0),     // Top-right
            new Point(width - 1, height - 1), // Bottom-right
            new Point(0, height - 1)     // Bottom-left
        };

        // Create Mat objects for source and destination points
        MatOfPoint2f srcMat = new MatOfPoint2f(srcPoints);
        MatOfPoint2f dstMat = new MatOfPoint2f(dstPoints);

        // Compute the perspective transformation matrix
        Mat perspectiveTransform = Imgproc.getPerspectiveTransform(srcMat, dstMat);

        // Apply the perspective transformation
        Mat correctedImage = new Mat();
        Imgproc.warpPerspective(croppedImage, correctedImage, perspectiveTransform, new Size(width, height));

        return correctedImage;
    }
        
    public static void preprocessAndSave(String inputPath, String finalPath, int xmin, int ymin, int xmax, int ymax) {
        Mat image = loadImage(inputPath);
        if (image.empty()) {
            System.err.println("Failed to load image for preprocessing: " + inputPath);
            return;
        }

        try {        
            // Resize the image before processing (place this after loading)
            image = resizeImage(image, 640, 480);
            
            // Crop the image to the region of interest (ROI)
            Rect roi = new Rect(xmin, ymin, xmax - xmin, ymax - ymin);
            Mat croppedImage = new Mat(image, roi);
            String croppedPath = finalPath.replace("final_image", "cropped_image");
            Imgcodecs.imwrite(finalPath.replace("final_image", "cropped_image"), croppedImage);
            System.out.println("Cropped image saved to: " + croppedPath);

            // Step 3: Apply perspective correction on the cropped ROI
            Mat correctedImage = applyPerspectiveCorrection(croppedImage);
            String correctedPath = finalPath.replace("final_image", "perspective_corrected");
            Imgcodecs.imwrite(correctedPath, correctedImage);
            System.out.println("Perspective-corrected image saved to: " + correctedPath);

            // Grayscale Conversion
            Mat grayImage = convertToGrayscale(correctedImage);
            String grayPath = finalPath.replace("final_image", "grayscale");
            Imgcodecs.imwrite(grayPath, grayImage);
            System.out.println("Grayscale image saved to: " + grayPath);

            // Noise Reduction
            Mat denoisedImage = reduceNoise(grayImage);
            String denoisedPath = finalPath.replace("final_image", "noise_reduced");
            Imgcodecs.imwrite(denoisedPath, denoisedImage);
            System.out.println("Denoised image saved to: " + denoisedPath);

            // Contrast Adjustment
            Mat contrastImage = applyCLAHE(denoisedImage);
            String contrastPath = finalPath.replace("final_image", "contrast_adjusted");
            Imgcodecs.imwrite(contrastPath, contrastImage);
            System.out.println("Contrast-adjusted image saved to: " + contrastPath);
            
            //Binary Thresholding (Temporarily Disabled)
            Mat binaryImage = applyStrictThreshold(contrastImage);
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
        String inputPath = "captures/1731890353265.jpg";
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
        String ocrResult = ocr_python.runOCR(outputPath);
        System.out.println("Detected Text: " + ocrResult);
    }
}