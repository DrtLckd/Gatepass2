/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.leninsystem;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;
/**
 *
 * @author predator 300
 */

import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.util.Properties;

public class EmailUtil {
    public static boolean sendEmail(String toEmail, String fromEmail, String subject, String body, String host, final String username, final String password, File attachment) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(fromEmail));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject(subject);

        // Create the message part
        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setText(body);

        // Create a multipart message
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);

        // Part two is attachment
        if (attachment != null) {
            MimeBodyPart attachPart = new MimeBodyPart();
            try {
                attachPart.attachFile(attachment);
            } catch (Exception e) {
                e.printStackTrace();
                throw new MessagingException("Failed to attach file", e);
            }
            multipart.addBodyPart(attachPart);
        }

        // Send the complete message parts
        message.setContent(multipart);

        // Send message
        Transport.send(message);
        
        return true;
    }
}