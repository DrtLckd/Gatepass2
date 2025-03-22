package gpsystem;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class ocr_python {

    // This method runs the OCR process on the given image
    public static String runOCR(String finalImagePath) {
        StringBuilder extractedText = new StringBuilder();
        try {
            String pythonScriptPath = "ocr_script.py";  // Ensure the path to your Python script is correct

            // Log the start of OCR execution
            System.out.println("Starting OCR process for image: " + finalImagePath);

            // Create Process to run the Python OCR script with the image path passed as an argument
            ProcessBuilder processBuilder = new ProcessBuilder("python", pythonScriptPath, finalImagePath);
            processBuilder.redirectErrorStream(true); // Merge error stream with output stream for easier logging

            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                // Capture only the OCR results
                if (line.startsWith("[") && line.endsWith("]")) {
                    extractedText.append(line).append("\n");
                }
            }

            // Wait for the process to complete, with a timeout
            boolean finished = process.waitFor(10, java.util.concurrent.TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();  // Kill the process if it takes too long
                process.waitFor();
                System.err.println("OCR process timeout!");
                return "";
            }
        } catch (Exception e) {
            System.err.println("Error during OCR execution: " + e.getMessage());
            e.printStackTrace();
        }
        return extractedText.toString().trim();  // Return the extracted text
    }

    // This method formats the OCR output for display in the UI (Optional)
    public static String formatOCRResults(String extractedText) {
        // Get current timestamp
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        // Simply return the OCR result with the timestamp
        return String.format("[%s]\n%s", timestamp, extractedText);
    }

    public static void processImage(String finalImagePath) {
        // Run OCR and extract the license plate text
        String extractedText = runOCR(finalImagePath).trim();

        if (extractedText.isEmpty()) {
            System.out.println("❌ No text detected.");
            return;
        }

        System.out.println("OCR Extracted Plate: " + extractedText);

        // Validate the plate using the database connection
        boolean isPlateValid = dbConn.validateLicensePlate(extractedText);

        if (isPlateValid) {
            System.out.println("✅ License Plate is REGISTERED!");
            System.out.println("Vehicle Details: \n" + dbConn.getPlateDetails(extractedText));
        } else {
            System.out.println("❌ License Plate is NOT REGISTERED.");
        }
    }
}