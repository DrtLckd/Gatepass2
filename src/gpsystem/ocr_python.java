package gpsystem;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ocr_python {
    
    public static void main(String[] args) {
        try {
            // Reference the existing processed image
            String processedImagePath = "D:\\Downloads\\Thesis\\javaTest\\processed_image.jpg";

            // Path to your Python OCR script
            String pythonScriptPath = "D:\\Downloads\\Thesis\\ocr_script.py"; // Update with the actual path to ocr_script.py

            // Step 2: Run the Python OCR script on the processed image
            ProcessBuilder processBuilder = new ProcessBuilder("python", pythonScriptPath, processedImagePath);
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line); // Output OCR results
            }

            int exitCode = process.waitFor();
            System.out.println("Python OCR script exited with code: " + exitCode);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}