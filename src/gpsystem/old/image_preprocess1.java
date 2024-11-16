package gpsystem.old;

import java.util.ArrayList;
import java.util.List;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class image_preprocess1 {
    
    static {
        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            System.out.println("OpenCV library loaded successfully.");
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Failed to load OpenCV library: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Function to load an image
    public static Mat loadImage(String filePath) {
        return Imgcodecs.imread(filePath);
    }
    
    // Grayscale Conversion
    public static Mat convertToGrayscale(Mat image) {
        Mat grayImage = new Mat();
        Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
        return grayImage;
    }

//    public static Mat applyHistogramEqualization(Mat image) {
//        Mat equalizedImage = new Mat();
//        Imgproc.equalizeHist(image, equalizedImage);
//        return equalizedImage;
//    }
    
    // Contrast Adjustment (Optional)
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

    // Edge Detection (Canny)
    public static Mat applyCannyEdgeDetection(Mat image) {
        Mat edges = new Mat();
        Imgproc.Canny(image, edges, 50, 180);
        return edges;
    }
    
    public static Mat applyMorphologicalClose(Mat image) {
        Mat closedImage = new Mat();
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5));
        Imgproc.morphologyEx(image, closedImage, Imgproc.MORPH_CLOSE, kernel);
        return closedImage;
    }

    // Contour Detection
    public static Mat findContours(Mat image) {
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(image, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        Mat contourImage = Mat.zeros(image.size(), CvType.CV_8UC3);
        for (int i = 0; i < contours.size(); i++) {
            Imgproc.drawContours(contourImage, contours, i, new Scalar(0, 255, 0), 2);
        }
        return contourImage;
    }
    
    // Perspective Correction (assuming predefined points)
    public static Mat applyPerspectiveCorrection(Mat image) {
        Mat correctedImage = new Mat();
        Mat transformMatrix = Mat.eye(3, 3, CvType.CV_32F); // Dummy matrix for now
        Imgproc.warpPerspective(image, correctedImage, transformMatrix, image.size());
        return correctedImage;
    }
    
    // Thresholding / Binarization with Adaptive Threshold
    public static Mat applyThreshold(Mat image) {
        Mat grayImage = image.channels() > 1 ? convertToGrayscale(image) : image;
        Mat thresholdImage = new Mat();
        Imgproc.adaptiveThreshold(grayImage, thresholdImage, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 3);
        return thresholdImage;
    }

    
    // Morphological Transformations (Dilation followed by Erosion)
    public static Mat applyMorphologicalTransformations(Mat image) {
        Mat morphImage = new Mat();
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        Imgproc.dilate(image, morphImage, kernel);
        Imgproc.erode(morphImage, morphImage, kernel);
        return morphImage;
    }

    public static Mat applySharpening(Mat image) {
        Mat sharpened = new Mat();
        Mat kernel = new MatOfFloat(
            -1, -1, -1, 
            -1,  9, -1, 
            -1, -1, -1
        );
        Imgproc.filter2D(image, sharpened, image.depth(), kernel);
        return sharpened;
    }

    public static void preprocessAndSave(String inputPath, String outputPath) {
        Mat image = loadImage(inputPath);
        if (image.empty()) {
            System.out.println("Failed to load image.");
            return;
        }

        // Apply Grayscale
        image = convertToGrayscale(image);
        Imgcodecs.imwrite("D:\\Downloads\\Thesis\\javaTest\\grayscale.jpg", image);

//        // Apply Histogram Equalization
//        image = applyHistogramEqualization(image);
//        Imgcodecs.imwrite("D:\\Downloads\\Thesis\\javaTest\\histogram_equalized.jpg", image);

        // Contrast Adjustment
        image = adjustContrast(image, 1.3, 20);
        Imgcodecs.imwrite("D:\\Downloads\\Thesis\\javaTest\\contrast_adjusted.jpg", image);

        // Noise Reduction
        image = reduceNoise(image);
        Imgcodecs.imwrite("D:\\Downloads\\Thesis\\javaTest\\noise_reduced.jpg", image);

        // Canny Edge Detection
        image = applyCannyEdgeDetection(image);
        Imgcodecs.imwrite("D:\\Downloads\\Thesis\\javaTest\\canny_edges.jpg", image);

        // Morphological Close before Contour Detection
        image = applyMorphologicalClose(image);
        Imgcodecs.imwrite("D:\\Downloads\\Thesis\\javaTest\\morphological_close.jpg", image);

        // Contour Detection
        Mat contourImage = findContours(image);
        Imgcodecs.imwrite("D:\\Downloads\\Thesis\\javaTest\\contours.jpg", contourImage);

        // Perspective Correction
        Mat correctedImage = applyPerspectiveCorrection(contourImage);
        Imgcodecs.imwrite("D:\\Downloads\\Thesis\\javaTest\\perspective_corrected.jpg", correctedImage);

        // Thresholding
        correctedImage = applyThreshold(correctedImage);
        Imgcodecs.imwrite("D:\\Downloads\\Thesis\\javaTest\\thresholded.jpg", correctedImage);

        // Morphological Transformations
        correctedImage = applyMorphologicalTransformations(correctedImage);
        Imgcodecs.imwrite("D:\\Downloads\\Thesis\\javaTest\\morphological_transformed.jpg", correctedImage);

        // Final Sharpening
        correctedImage = applySharpening(correctedImage);
        Imgcodecs.imwrite(outputPath, correctedImage);
        System.out.println("Image processed and saved to: " + outputPath);
    }


    public static void main(String[] args) {
        String inputPath = "D:\\Downloads\\Thesis\\task3\\IMG_0480.jpeg";
        String outputPath = "D:\\Downloads\\Thesis\\javaTest\\processed_image.jpg";

        preprocessAndSave(inputPath, outputPath);
    }
    
}
