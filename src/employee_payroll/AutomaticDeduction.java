package employee_payroll;

import java.sql.*;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;

public class AutomaticDeduction extends javax.swing.JFrame {
    
    private static final double ABSENT_DEDUCTION = 500.00; // Default deduction amount per absence
    
    public AutomaticDeduction() {
        initComponents();
    }
    
    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Automatic Deduction Calculator");
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create input panel
        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        
        JLabel empIdLabel = new JLabel("Employee ID:");
        JTextField empIdField = new JTextField();
        
        JLabel absencesLabel = new JLabel("Number of Absences:");
        JTextField absencesField = new JTextField();
        
        JLabel deductionLabel = new JLabel("Deduction per Absence:");
        JTextField deductionField = new JTextField(String.valueOf(ABSENT_DEDUCTION));
        
        inputPanel.add(empIdLabel);
        inputPanel.add(empIdField);
        inputPanel.add(absencesLabel);
        inputPanel.add(absencesField);
        inputPanel.add(deductionLabel);
        inputPanel.add(deductionField);
        
        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton calculateButton = new JButton("Calculate & Apply Deduction");
        buttonPanel.add(calculateButton);
        
        mainPanel.add(inputPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Add action listener
        calculateButton.addActionListener(e -> {
            try {
                int empId = Integer.parseInt(empIdField.getText().trim());
                int absences = Integer.parseInt(absencesField.getText().trim());
                double deductionAmount = Double.parseDouble(deductionField.getText().trim());
                
                applyDeduction(empId, absences, deductionAmount);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid numbers");
            }
        });
        
        add(mainPanel);
        pack();
        setLocationRelativeTo(null);
    }
    
    private void applyDeduction(int empId, int absences, double deductionPerAbsence) {
        try {
            Connection conn = db.java_db();
            
            // First check if employee exists and get their details
            String empQuery = "SELECT f_name, l_name, salary FROM employee WHERE emp_id = ?";
            PreparedStatement empStmt = conn.prepareStatement(empQuery);
            empStmt.setInt(1, empId);
            
            ResultSet empRs = empStmt.executeQuery();
            
            if (empRs.next()) {
                String firstName = empRs.getString("f_name");
                String lastName = empRs.getString("l_name");
                double salary = empRs.getDouble("salary");
                
                // Calculate total deduction
                double totalDeduction = absences * deductionPerAbsence;
                
                // Insert into deduction table
                String deductQuery = "INSERT INTO deduction (emp_id, f_name, l_name, salary, deduct_amount, deduct_reason, made_by) " +
                                   "VALUES (?, ?, ?, ?, ?, ?, ?)";
                
                PreparedStatement deductStmt = conn.prepareStatement(deductQuery);
                deductStmt.setInt(1, empId);
                deductStmt.setString(2, firstName);
                deductStmt.setString(3, lastName);
                deductStmt.setDouble(4, salary);
                deductStmt.setDouble(5, totalDeduction);
                deductStmt.setString(6, String.format("Automatic deduction for %d absences", absences));
                deductStmt.setString(7, "System");
                
                deductStmt.executeUpdate();
                
                JOptionPane.showMessageDialog(this, String.format(
                    "Deduction applied successfully!\n" +
                    "Employee: %s %s\n" +
                    "Total Deduction: $%.2f",
                    firstName, lastName, totalDeduction
                ));
                
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Employee ID not found!");
            }
            
            conn.close();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error applying deduction: " + e.getMessage());
        }
    }
    
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            new AutomaticDeduction().setVisible(true);
        });
    }
}
