/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.leninsystem;

/**
 *
 * @author predator 300
 */
class ComboBoxItem {
    private String username;
    private String decryptedEmail;

    public ComboBoxItem(String username, String decryptedEmail) {
        this.username = username;
        this.decryptedEmail = decryptedEmail;
    }

    public String getUsername() {
        return username;
    }

    public String getDecryptedEmail() {
        return decryptedEmail;
    }

    @Override
    public String toString() {
        return username; // Display username in ComboBox
    }
}
