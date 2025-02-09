package gpsystem;

import java.io.*;

public class ocr_python {

    public static String runOCR(String finalImagePath) {
        StringBuilder extractedText = new StringBuilder();
        try {
            String pythonScriptPath = "ocr_script.py";  // Make sure this path is correct!

            // Create Process to Run Python OCR Script
            ProcessBuilder processBuilder = new ProcessBuilder("python", pythonScriptPath, finalImagePath);
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                extractedText.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.err.println("Python OCR script exited with an error. Code: " + exitCode);
            }
        } catch (Exception e) {
            System.err.println("Error during OCR execution: " + e.getMessage());
            e.printStackTrace();
        }
        return extractedText.toString().trim();  // Return extracted text
    }
}
