package db_connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class db_config {
    
    private static final String DATABASE_URL = "jdbc:mysql://localhost:3306/LeninSystem"; // Replace with your database URL
    private static final String DATABASE_USER = "your_username"; // Replace with your MySQL username
    private static final String DATABASE_PASSWORD = "your_password"; // Replace with your MySQL password
    
    private Connection connection;

    // Constructor to initialize the connection
    public db_config() {
        try {
            // Load MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);
            System.out.println("Connected to MySQL database successfully!");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Close the connection
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to insert a request into the database
    public void insertRequest(String clientName, String contactNo, String projAddress, String clientEmail, String requestFrom, String sendTo, String stockAvailability, String dateIssued) {
        String sql = "INSERT INTO RFQ (client_name, proj_location, contact_no, client_email, request_from, send_to, stock_availability, dateIssued, request_app) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, clientName);
            statement.setString(2, projAddress);
            statement.setString(3, contactNo);
            statement.setString(4, clientEmail);
            statement.setString(5, requestFrom);
            statement.setString(6, sendTo);
            statement.setString(7, stockAvailability);
            statement.setString(8, dateIssued);
            statement.setString(9, "pending");
            
            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                JOptionPane.showMessageDialog(null, "Request submitted successfully!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Method to insert a user into the database
    public void insertUser(String username, String password, String email, String emailPassword) {
        String sql = "INSERT INTO Users (username, password, email, emailPassword) VALUES (?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, password); // Encrypt password if necessary
            statement.setString(3, email);
            statement.setString(4, emailPassword);

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                JOptionPane.showMessageDialog(null, "User inserted successfully!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Method to fetch RFQ data from the database
    public void getRFQData() {
        String sql = "SELECT * FROM RFQ ORDER BY dateIssued DESC";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                System.out.println("Client Name: " + resultSet.getString("client_name"));
                // Fetch other details similarly
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error fetching data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Method to fetch user credentials
    public String[] getUserCredentials(String username) {
        String sql = "SELECT username, password, email FROM Users WHERE username = ?";
        String[] credentials = new String[3];
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    credentials[0] = resultSet.getString("username");
                    credentials[1] = resultSet.getString("password");
                    credentials[2] = resultSet.getString("email");
                } else {
                    JOptionPane.showMessageDialog(null, "User not found in database.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error fetching user credentials: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return credentials;
    }
}
