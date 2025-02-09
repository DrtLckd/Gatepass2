package gpsystem;

import java.io.*;

public class ocr_python {

    private Process pythonProcess;
    private BufferedReader reader;
    private BufferedWriter writer;

    public ocr_python() {
        try {
            String pythonScriptPath = "ocr_script.py"; // Ensure correct path
            ProcessBuilder processBuilder = new ProcessBuilder("python", pythonScriptPath);
            processBuilder.redirectErrorStream(true);
            pythonProcess = processBuilder.start();

            // Setup streams for reading & writing
            reader = new BufferedReader(new InputStreamReader(pythonProcess.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(pythonProcess.getOutputStream()));

            System.out.println("OCR Python script initialized successfully.");

        } catch (IOException e) {
            System.err.println("Error initializing Python OCR script: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public synchronized String runOCR(String finalImagePath) {
        StringBuilder extractedText = new StringBuilder();
        try {
            if (pythonProcess == null) {
                System.err.println("OCR process is not running.");
                return "";
            }

            // Send the image path to Python
            writer.write(finalImagePath + "\n");
            writer.flush();

            // Read OCR output
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().equals("END")) break; // Marker to stop reading
                extractedText.append(line).append("\n");
            }

        } catch (IOException e) {
            System.err.println("Error communicating with Python OCR script: " + e.getMessage());
            e.printStackTrace();
        }

        return extractedText.toString().trim();
    }

    public void stopOCR() {
        try {
            if (pythonProcess != null) {
                writer.write("exit\n");
                writer.flush();
                pythonProcess.waitFor();
                pythonProcess.destroy();
                pythonProcess = null;
                System.out.println("OCR process terminated.");
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error stopping OCR process: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java ocr_python <finalImagePath>");
            return;
        }

        ocr_python ocr = new ocr_python();
        String finalImagePath = args[0];

        System.out.println("Extracted Text: " + ocr.runOCR(finalImagePath));

        ocr.stopOCR();
    }
}
