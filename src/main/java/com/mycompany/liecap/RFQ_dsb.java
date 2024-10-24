/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.liecap;

import panels.sidebar_dsb;
import db_connection.db_config;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author predator 300
 */
public class RFQ_dsb extends javax.swing.JFrame {

    private sidebar_dsb sidebar;
    /**
     * Creates new form RFQ_dsb
     */
    public RFQ_dsb() {
        initComponents();
        fetchAndDisplayRFQData();
    }

        // Constructor with sidebar_dsb parameter
    public RFQ_dsb(sidebar_dsb sidebar) {
        this.sidebar = sidebar;
        initComponents();
        fetchAndDisplayRFQData();
    }
    
        /**
     * Method to fetch data from MongoDB and populate the table
     */
    private void fetchAndDisplayRFQData() {
        DefaultTableModel model = (DefaultTableModel) RFQView_Table.getModel();
        model.setRowCount(0); // Clear existing data

        new SwingWorker<List<Document>, Void>() {
            @Override
            protected List<Document> doInBackground() throws Exception {
                db_config dbConfig = new db_config();
                return dbConfig.getRFQData();
            }

            @Override
            protected void done() {
                try {
                    List<Document> documents = get();
                    for (Document doc : documents) {
                        String clientName = doc.getString("client_name");
                        String projLocation = doc.getString("proj_location");
                        String stockAvailability = doc.getString("stock_availability");
                        String requestApp = doc.getString("request_app");

                        model.addRow(new Object[]{clientName, projLocation, stockAvailability, requestApp});
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(RFQ_dsb.this, "Error fetching data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        body_login1 = new panels.body_login();
        create_request_btn = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        RFQView_Table = new javax.swing.JTable();
        sidebar_dsb1 = new panels.sidebar_dsb();
        Refresh = new javax.swing.JToggleButton();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        body_login1.setBackground(new java.awt.Color(255, 255, 255));

        create_request_btn.setText("Create Request");
        create_request_btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                create_request_btnActionPerformed(evt);
            }
        });

        RFQView_Table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Client Name", "Location", "Stock Availability", "Project Status", "Action"
            }
        ));
        jScrollPane1.setViewportView(RFQView_Table);

        Refresh.setText("Refresh");
        Refresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RefreshActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("QUOTATION");

        javax.swing.GroupLayout body_login1Layout = new javax.swing.GroupLayout(body_login1);
        body_login1.setLayout(body_login1Layout);
        body_login1Layout.setHorizontalGroup(
            body_login1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(body_login1Layout.createSequentialGroup()
                .addComponent(sidebar_dsb1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(body_login1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(body_login1Layout.createSequentialGroup()
                        .addGap(46, 46, 46)
                        .addGroup(body_login1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 627, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(body_login1Layout.createSequentialGroup()
                                .addComponent(create_request_btn)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(Refresh)))
                        .addGap(38, 38, 38))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, body_login1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel1)
                        .addGap(279, 279, 279))))
        );
        body_login1Layout.setVerticalGroup(
            body_login1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(body_login1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(body_login1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(create_request_btn)
                    .addComponent(Refresh, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 489, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(25, 25, 25))
            .addGroup(body_login1Layout.createSequentialGroup()
                .addComponent(sidebar_dsb1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 4, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(body_login1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(body_login1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void create_request_btnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_create_request_btnActionPerformed
        // TODO add your handling code here:
        RFQ_createRequest rfq_cr = new RFQ_createRequest();
        rfq_cr.setVisible(true);
    }//GEN-LAST:event_create_request_btnActionPerformed

    private void RefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RefreshActionPerformed
        fetchAndDisplayRFQData();
    }//GEN-LAST:event_RefreshActionPerformed

    /**
     * @param args the command line arguments
     */
//    public static void main(String args[]) {
//        /* Set the Nimbus look and feel */
//        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
//        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
//         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
//         */
//        try {
//            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
//                if ("Nimbus".equals(info.getName())) {
//                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
//                    break;
//                }
//            }
//        } catch (ClassNotFoundException ex) {
//            java.util.logging.Logger.getLogger(RFQ_dsb.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (InstantiationException ex) {
//            java.util.logging.Logger.getLogger(RFQ_dsb.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (IllegalAccessException ex) {
//            java.util.logging.Logger.getLogger(RFQ_dsb.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
//            java.util.logging.Logger.getLogger(RFQ_dsb.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        }
//        //</editor-fold>
//
//        /* Create and display the form */
//        java.awt.EventQueue.invokeLater(new Runnable() {
//            public void run() {
//                new RFQ_dsb().setVisible(true);
//            }
//        });
//    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable RFQView_Table;
    private javax.swing.JToggleButton Refresh;
    private panels.body_login body_login1;
    private javax.swing.JButton create_request_btn;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private panels.sidebar_dsb sidebar_dsb1;
    // End of variables declaration//GEN-END:variables
}