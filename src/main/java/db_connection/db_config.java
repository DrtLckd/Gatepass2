/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package db_connection;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Sorts;
import com.mycompany.leninsystem.AESUtil;
import org.bson.Document;
import javax.swing.JOptionPane;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

/**
 *
 * @author predator 300
 */
public class db_config {
    
    private static final String URI = "mongodb+srv://LeninSystem:LeninSystem@leninsystem.mta9tsj.mongodb.net/?retryWrites=true&w=majority&appName=LeninSystem";
    private static final String DATABASE_NAME = "LeninSystem";
    
    private MongoClient mongoClient;
    private MongoDatabase database;
    
    public db_config() {
        ConnectionString connectionString = new ConnectionString(URI);
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
        this.mongoClient = MongoClients.create(settings);
        this.database = mongoClient.getDatabase(DATABASE_NAME);
    }
    
     public MongoDatabase getDatabase() {
        return this.database;
    }
    
    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
    
    public void insertRequest(String clientName, String contactNo, String projAddress, String clientEmail, String requestFrom, String sendTo, String stockAvailability, byte[] fileData, String dateIssued) {
        String requestApp = "pending";

        Document document = new Document("client_name", clientName)
                .append("proj_location", projAddress)
                .append("contact_no", contactNo)
                .append("client_email", clientEmail)
                .append("request_from", requestFrom)
                .append("send_to", sendTo)
                .append("dateIssued", dateIssued)
                .append("request_app", requestApp)
                .append("stock_availability", stockAvailability);
                

        try {
            MongoDatabase database = getDatabase();
            MongoCollection<Document> collection = database.getCollection("RFQ");
            collection.insertOne(document);
            JOptionPane.showMessageDialog(null, "Request and email submitted successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            close();
        }

//    try {
//        MongoDatabase database = getDatabase();
//        MongoCollection<Document> collection = database.getCollection("RFQ");
//        collection.insertOne(document);
//        JOptionPane.showMessageDialog(null, "Request submitted successfully!");
//
//        // Fetch username and password based on requestFrom
//        String username = null;
//        String emailPassword = null;
//
//        MongoCollection<Document> usersCollection = database.getCollection("Users");
//        Document query = new Document("username", requestFrom); // Assuming requestFrom is the username
//        Document userDocument = usersCollection.find(query).first();
//        
//        if (userDocument != null) {
//            // Decrypt username and password
//            String encryptedUsername = userDocument.getString("username");
//            String encryptedEmailPassword = userDocument.getString("emailPassword");
//            
//            // Decrypt using AESUtil
//            SecretKey key = AESUtil.getDecryptionKey(userDocument.getString("key"), userDocument.getString("iv"));
//            username = AESUtil.decrypt(encryptedUsername, key);
//            emailPassword = AESUtil.decrypt(encryptedEmailPassword, key);
//        } else {
//            JOptionPane.showMessageDialog(null, "User not found in database.");
//            return;
//        }
//
//        // Now you can use 'username' and 'emailPassword' for further processing
//
//    } catch (Exception e) {
//        e.printStackTrace();
//        JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
//    } finally {
//        close();
//    }
    }
    
//    public void insertUser(String username, String password, String email, String emailPassword) {
//        try {
//             // Generate AES key and IV (Initialization Vector)
//            SecretKey key = AESUtil.generateKey();
//            IvParameterSpec iv = AESUtil.generateIV();
//
//            // Encrypt password and email
//            String encryptedPassword = AESUtil.encrypt(password, key, iv);
//            String encryptedEmail = AESUtil.encrypt(email, key, iv);
//
//             // Convert key and IV to Base64 for storage
//            String keyBase64 = Base64.getEncoder().encodeToString(key.getEncoded());
//            String ivBase64 = Base64.getEncoder().encodeToString(iv.getIV());
//            
//            // Create document to insert into MongoDB
//            Document document = new Document("username", username)
//                    .append("password", encryptedPassword)
//                    .append("c_password", encryptedPassword)
//                    .append("email", encryptedEmail)
////                    .append("emailPassword", emailPassword); // Assuming email password is not encrypted
//                    .append("emailPassword", encryptedPassword)
//                    .append("key", keyBase64)
//                    .append("iv", ivBase64);
//
//            // Insert document into "Users" collection
//            MongoCollection<Document> collection = database.getCollection("Users");
//            collection.insertOne(document);
//
//            JOptionPane.showMessageDialog(null, "User inserted successfully!");
//        } catch (Exception e) {
//            e.printStackTrace();
//            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
//        } finally {
//            close();
//        }
//    }
    
public void insertUser(String username, String password, String email, String emailPassword) {
    try {
        // Generate AES key and IV (Initialization Vector)
        SecretKey key = AESUtil.generateKey();
        IvParameterSpec iv = AESUtil.generateIV();

        // Encrypt password and email
        String encryptedPassword = AESUtil.encrypt(password, key, iv);
        String encryptedEmail = AESUtil.encrypt(email, key, iv);

        // Convert key and IV to Base64 for storage
        String keyBase64 = Base64.getEncoder().encodeToString(key.getEncoded());
        String ivBase64 = Base64.getEncoder().encodeToString(iv.getIV());

        // Log encrypted values for debugging
        System.out.println("Encrypted Password: " + encryptedPassword);
        System.out.println("Encrypted Email: " + encryptedEmail);
        System.out.println("Key (Base64): " + keyBase64);
        System.out.println("IV (Base64): " + ivBase64);

        // Create document to insert into MongoDB
        Document document = new Document("username", username)
                .append("password", encryptedPassword)
                .append("c_password", encryptedPassword)
                .append("email", encryptedEmail)
                .append("emailPassword", encryptedPassword)
                .append("key", keyBase64)
                .append("iv", ivBase64);

        // Insert document into "Users" collection
        MongoCollection<Document> collection = database.getCollection("Users");
        collection.insertOne(document);

        JOptionPane.showMessageDialog(null, "User inserted successfully!");
    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    } finally {
        close();
    }
}


    
        // Method to fetch RFQ data from the database
    public List<Document> getRFQData() {
        List<Document> rfqList = new ArrayList<>();
        try {
            MongoCollection<Document> collection = database.getCollection("RFQ");
            // Sort by dateIssued in descending order
            try (MongoCursor<Document> cursor = collection.find().sort(Sorts.descending("dateIssued")).iterator()) {
                while (cursor.hasNext()) {
                    rfqList.add(cursor.next());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error fetching data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return rfqList;
    }
    
    public List<Document> getUsersData() {
        List<Document> usersList = new ArrayList<>();
        try {
            MongoCollection<Document> collection = database.getCollection("Users");
            try (MongoCursor<Document> cursor = collection.find().iterator()) {
                while (cursor.hasNext()) {
                    usersList.add(cursor.next());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error fetching data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return usersList;
    }
    
//    public String[] getUserCredentials(String requestFrom) {
//        String[] credentials = new String[2];
//        try {
//            MongoCollection<Document> usersCollection = database.getCollection("Users");
//            Document query = new Document("username", requestFrom);
//            Document userDocument = usersCollection.find(query).first();
//
//            if (userDocument != null) {
//                String encryptedEmail = userDocument.getString("email");
//                String encryptedEmailPassword = userDocument.getString("emailPassword");
//
//                SecretKey key = AESUtil.getDecryptionKey(userDocument.getString("key"), userDocument.getString("iv"));
//                String email = AESUtil.decrypt(encryptedEmail, key);
//                String emailPassword = AESUtil.decrypt(encryptedEmailPassword, key);
//
//                credentials[0] = email;
//                credentials[1] = emailPassword;
//            } else {
//                JOptionPane.showMessageDialog(null, "User not found in database.");
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            JOptionPane.showMessageDialog(null, "Error fetching user credentials: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
//        } finally {
//            close();
//        }
//        return credentials;
//    }
    

//    public String[] getUserCredentials(String requestFrom) {
//    String[] credentials = new String[2];
//    MongoClient mongoClient = null;
//    try {
//        ConnectionString connectionString = new ConnectionString(URI);
//        MongoClientSettings settings = MongoClientSettings.builder()
//                .applyConnectionString(connectionString)
//                .build();
//        mongoClient = MongoClients.create(settings);
//        MongoDatabase database = mongoClient.getDatabase("LeninSystem");
//
//        MongoCollection<Document> usersCollection = database.getCollection("Users");
//        Document query = new Document("email", requestFrom);
//        System.out.println("Querying database with: " + query.toJson());
//        Document userDocument = usersCollection.find(query).first();
//
//        if (userDocument != null) {
//            System.out.println("User document found: " + userDocument.toJson());
//            String encryptedEmail = userDocument.getString("email");
//            String encryptedEmailPassword = userDocument.getString("emailPassword");
//
//            String keyBase64 = userDocument.getString("key");
//            String ivBase64 = userDocument.getString("iv");
//            SecretKey key = AESUtil.getDecryptionKey(keyBase64);
//            IvParameterSpec iv = AESUtil.getDecryptionIV(ivBase64);
//
//            String email = AESUtil.decrypt(encryptedEmail, key, iv);
//            String emailPassword = AESUtil.decrypt(encryptedEmailPassword, key, iv);
//
//            credentials[0] = email;
//            credentials[1] = emailPassword;
//        } else {
//            JOptionPane.showMessageDialog(null, "User not found in database.");
//        }
//    } catch (Exception e) {
//        e.printStackTrace();
//        JOptionPane.showMessageDialog(null, "Error fetching user credentials: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
//    } finally {
//        if (mongoClient != null) {
//            mongoClient.close();
//        }
//    }
//    return credentials;
//}
    
public String[] getUserCredentials(String requestFrom) {
    String[] credentials = new String[2];
    MongoClient mongoClient = null;
    try {
        ConnectionString connectionString = new ConnectionString(URI);
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
        mongoClient = MongoClients.create(settings);
        MongoDatabase database = mongoClient.getDatabase("LeninSystem");

        MongoCollection<Document> usersCollection = database.getCollection("Users");

        // Fetch encryption keys
        Document knownUserDoc = usersCollection.find().first(); // Get a known user document to fetch the key and IV
        String keyBase64 = knownUserDoc.getString("key");
        String ivBase64 = knownUserDoc.getString("iv");

        if (keyBase64 == null || ivBase64 == null) {
            JOptionPane.showMessageDialog(null, "Error fetching encryption keys.", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        // Encrypt the requestFrom email before querying
        SecretKey key = AESUtil.getDecryptionKey(keyBase64);
        IvParameterSpec iv = AESUtil.getDecryptionIV(ivBase64);
        String encryptedRequestFrom = AESUtil.encrypt(requestFrom, key, iv);

        // Query the database with the encrypted email
        Document query = new Document("email", encryptedRequestFrom);
        System.out.println("Querying database with: " + query.toJson());
        Document userDocument = usersCollection.find(query).first();

        if (userDocument != null) {
            System.out.println("User document found: " + userDocument.toJson());
            String encryptedEmail = userDocument.getString("email");
            String encryptedEmailPassword = userDocument.getString("emailPassword");

            String userKeyBase64 = userDocument.getString("key");
            String userIvBase64 = userDocument.getString("iv");
            SecretKey userKey = AESUtil.getDecryptionKey(userKeyBase64);
            IvParameterSpec userIv = AESUtil.getDecryptionIV(userIvBase64);

            // Decrypt the email and email password
            String email = AESUtil.decrypt(encryptedEmail, userKey, userIv);
            String emailPassword = AESUtil.decrypt(encryptedEmailPassword, userKey, userIv);

            System.out.println("Decrypted Email: " + email);
            System.out.println("Decrypted Email Password: " + emailPassword);

            credentials[0] = email;
            credentials[1] = emailPassword;
        } else {
            System.out.println("User not found in database.");
            JOptionPane.showMessageDialog(null, "User not found in database.");
        }
    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "Error fetching user credentials: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    } finally {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
    return credentials;
}


public String getUserKey(String email) {
    // Fetch the user's key based on the email
    try {
        MongoCollection<Document> usersCollection = database.getCollection("Users");
        Document query = new Document("email", email);
        Document userDocument = usersCollection.find(query).first();

        if (userDocument != null) {
            return userDocument.getString("key");
        } else {
            JOptionPane.showMessageDialog(null, "User not found in database.");
        }
    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "Error fetching user key: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
    return null;
}

public String getUserIV(String email) {
    // Fetch the user's IV based on the email
    try {
        MongoCollection<Document> usersCollection = database.getCollection("Users");
        Document query = new Document("email", email);
        Document userDocument = usersCollection.find(query).first();

        if (userDocument != null) {
            return userDocument.getString("iv");
        } else {
            JOptionPane.showMessageDialog(null, "User not found in database.");
        }
    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "Error fetching user IV: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
    return null;
}

}
    
    

