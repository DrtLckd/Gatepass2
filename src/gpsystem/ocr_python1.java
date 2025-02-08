package gpsystem;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ocr_python1 {

    public static String runOCR(String finalImagePath) {
        StringBuilder extractedText = new StringBuilder();
        try {
            String pythonScriptPath = "ocr_script.py"; // Update with the actual path to your Python OCR script

            // Build the process to run the Python OCR script with the final image
            ProcessBuilder processBuilder = new ProcessBuilder("python", pythonScriptPath, finalImagePath);
            processBuilder.redirectErrorStream(true);

            // Start the process
            Process process = processBuilder.start();

            // Read the output from the Python script
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                extractedText.append(line).append("\n");
            }

            // Wait for the process to finish
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.err.println("Python OCR script exited with an error. Code: " + exitCode);
            }
        } catch (Exception e) {
            System.err.println("Error during OCR execution: " + e.getMessage());
            e.printStackTrace();
        }
        return extractedText.toString().trim();
    }

    public static void main(String[] args) {
        // For testing, provide the path to the final processed image
        if (args.length < 1) {
            System.err.println("Usage: java ocr_python <finalImagePath>");
            return;
        }
        String finalImagePath = args[0];
        runOCR(finalImagePath);
    }
}
