package gpsystem;

import java.sql.*;
import java.util.*;

public class dbConn {
    // Database connection details
    private static final String DB_URL = "jdbc:mysql://localhost:3306/registeredvehicles";  
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root";

    // Validate license plate by checking OCR variations first
    public static boolean validateLicensePlate(String plateNumber) {
        Set<String> possiblePlates = generateOCRVariations(plateNumber); // Check OCR variations first
        
        System.out.println("\nSearching for OCR variants of: " + plateNumber);
        
        for (String plate : possiblePlates) {
            System.out.println("Checking: " + plate);
            String query = "SELECT plate_number FROM record " +
                           "WHERE UPPER(TRIM(plate_number)) = ?";

            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setString(1, plate.toUpperCase());

                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String foundPlate = rs.getString("plate_number");

                    if (foundPlate.equalsIgnoreCase(plate)) {
                        System.out.println("Exact Match: " + foundPlate);
                        return true;  // Exact match → No user input needed
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        System.out.println("No Match Found. User must input plate manually.");
        return false; // No match → User must manually confirm or register the plate
    }

    // Get details for a plate number, checking against all variations
    public static String getPlateDetails(String plateNumber) {
        Set<String> possiblePlates = generateOCRVariations(plateNumber);

        String query = "SELECT plate_number, full_name, vehicle, model FROM record " +
                       "WHERE UPPER(TRIM(plate_number)) = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            for (String plate : possiblePlates) {
                stmt.setString(1, plate.toUpperCase());

                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    String foundPlate = rs.getString("plate_number");
                    String owner = rs.getString("full_name");
                    String vehicle = rs.getString("vehicle");
                    String model = rs.getString("model") != null ? rs.getString("model") : "Unknown Model";

                    if (foundPlate.equalsIgnoreCase(plateNumber)) {
                        return String.format("Owner: %s\nVehicle: %s\nModel: %s", owner, vehicle, model);
                    } else {
                        return String.format("Partial Match: %s\nOwner: %s\nVehicle: %s\nModel: %s\nConfirm?", 
                            foundPlate, owner, vehicle, model);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Plate Not Found. Please input manually.";
    }

    // Normalize the plate to account for common OCR mistakes and reverse replacements
    public static String normalizePlate(String plate) {
        if (plate == null || plate.isEmpty()) {
            return ""; // Return empty string if plate is null
        }

        // Normalize characters that are commonly misread by OCR (both ways)
        return plate.replaceAll("[^a-zA-Z0-9]", "")   // Remove non-alphanumeric characters
                    .replace('I', '1')               // Convert I → 1
                    .replace('1', 'I')               // Convert 1 → I (reverse case)
                    .replace('O', '0')               // Convert O → 0
                    .replace('0', 'O')               // Convert 0 → O (reverse case)
                    .replace('B', '8')               // Convert B → 8
                    .replace('8', 'B')               // Convert 8 → B (reverse case)
                    .replace('S', '5')               // Convert S → 5
                    .replace('5', 'S')               // Convert 5 → S (reverse case)
//                    .replace('Z', '2')               // Convert Z → 2
//                    .replace('2', 'Z')               // Convert 2 → Z (reverse case)
//                    .replace('G', '6')               // Convert G → 6
//                    .replace('6', 'G')               // Convert 6 → G (reverse case)
//                    .replace('Q', 'O')               // Convert Q → O
//                    .replace('0', 'O')               // Convert 0 → O
//                    .replace('O', 'Q')               // Convert O → Q
//                    .replace('0', 'Q')               // Convert 0 → Q
                    .toUpperCase()                   // Ensure uppercase for consistent comparison
                    .trim();                         // Trim any leading/trailing spaces
    }

    // Generate all possible variations of the license plate (OCR variants only)
    public static Set<String> generateOCRVariations(String plateNumber) {
        Set<String> variations = new HashSet<>();
        variations.add(plateNumber); // Add the original plate as a variation

        // Create a HashMap to handle bidirectional OCR replacements (e.g., I ↔ 1, O ↔ 0)
        Map<Character, Character> replacements = new HashMap<>();
        replacements.put('I', '1'); // I ↔ 1
        replacements.put('1', 'I'); // 1 ↔ I
        replacements.put('O', '0'); // O ↔ 0
        replacements.put('0', 'O'); // 0 ↔ O
        replacements.put('B', '8'); // B ↔ 8
        replacements.put('8', 'B'); // 8 ↔ B
        replacements.put('S', '5'); // S ↔ 5
        replacements.put('5', 'S'); // 5 ↔ S
//        replacements.put('Z', '2'); // Z ↔ 2
//        replacements.put('2', 'Z'); // 2 ↔ Z
//        replacements.put('G', '6'); // G ↔ 6
//        replacements.put('6', 'G'); // 6 ↔ G
//        replacements.put('Q', 'O'); // Q ↔ O
//        replacements.put('0', 'O'); // 0 ↔ O
//        replacements.put('O', 'Q'); // O → Q
//        replacements.put('0', 'Q'); // 0 → Q

        // Loop over the map and generate all possible variations
        for (Map.Entry<Character, Character> entry : replacements.entrySet()) {
            char original = entry.getKey();
            char replacement = entry.getValue();

            // Replace the character with its counterpart and add both variations
            variations.add(plateNumber.replace(original, replacement));
            variations.add(plateNumber.replace(replacement, original));
        }

        return variations;
    }

    // Test the database connection and plate search.
    public static void main(String[] args) {
        testConnection();

        String testPlate = "5B3JX1";
        System.out.println("Generated Plate Variations for: " + testPlate);
        Set<String> variations = generateOCRVariations(testPlate);
        for (String variation : variations) {
            System.out.println("Checking: " + variation);
            if (validateLicensePlate(variation)) {
                System.out.println("Plate Found: " + variation);
                break; // Exit once a valid plate is found
            }
        }
    }

    public static void testConnection() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            System.out.println("Connected to database successfully!");
        } catch (SQLException e) {
            System.out.println("Database connection failed!");
            e.printStackTrace();
        }
    }
}
