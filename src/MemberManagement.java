import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class MemberManagement extends JFrame {

    private JTextField nameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTable memberTable;
    private DefaultTableModel tableModel;

    public MemberManagement() {
        // Basic frame settings for the Member Management window.
        setTitle("Member Management");
        setSize(850, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Top form panel for adding new members.
        JPanel formPanel = new JPanel(new GridLayout(2, 4, 10, 10));

        formPanel.add(new JLabel("Name:"));
        nameField = new JTextField();
        formPanel.add(nameField);

        formPanel.add(new JLabel("Email:"));
        emailField = new JTextField();
        formPanel.add(emailField);

        formPanel.add(new JLabel("Phone:"));
        phoneField = new JTextField();
        formPanel.add(phoneField);

        JButton addMemberButton = new JButton("Add Member");
        addMemberButton.addActionListener(e -> addMember());
        formPanel.add(addMemberButton);

        JButton refreshButton = new JButton("Refresh Members");
        refreshButton.addActionListener(e -> loadMembers());
        formPanel.add(refreshButton);

        add(formPanel, BorderLayout.NORTH);

        // Center table section to display all member records.
        tableModel = new DefaultTableModel(new String[] {
                "Member ID", "Name", "Email", "Phone", "Join Date"
        }, 0);

        memberTable = new JTable(tableModel);
        add(new JScrollPane(memberTable), BorderLayout.CENTER);

        // Footer action area for spacing/alignment consistency.
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        add(footerPanel, BorderLayout.SOUTH);

        // Initial load of all members when frame opens.
        loadMembers();
    }

    // Adds a new member with join_date set to today's date.
    private void addMember() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please fill all fields before adding a member.",
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = DBConnection.getConnection();

            String insertSql = "INSERT INTO members (name, email, phone, join_date) VALUES (?, ?, ?, ?)";
            statement = connection.prepareStatement(insertSql);
            statement.setString(1, name);
            statement.setString(2, email);
            statement.setString(3, phone);
            statement.setDate(4, Date.valueOf(LocalDate.now()));

            int rowsInserted = statement.executeUpdate();

            if (rowsInserted > 0) {
                JOptionPane.showMessageDialog(this,
                        "Member added successfully.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

                // Reset input fields after successful insert.
                nameField.setText("");
                emailField.setText("");
                phoneField.setText("");

                loadMembers();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Member could not be added.",
                        "Insert Failed",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Database error while adding member: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        } finally {
            // Closing all opened DB resources in finally block.
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this,
                            "Error closing statement: " + ex.getMessage(),
                            "Resource Close Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }

            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this,
                            "Error closing connection: " + ex.getMessage(),
                            "Resource Close Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    // Fetches all members from database and loads them into JTable.
    private void loadMembers() {
        tableModel.setRowCount(0);

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DBConnection.getConnection();

            String selectSql = "SELECT member_id, name, email, phone, join_date FROM members ORDER BY member_id";
            statement = connection.prepareStatement(selectSql);
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Object[] rowData = {
                        resultSet.getInt("member_id"),
                        resultSet.getString("name"),
                        resultSet.getString("email"),
                        resultSet.getString("phone"),
                        resultSet.getDate("join_date")
                };
                tableModel.addRow(rowData);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Database error while loading members: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        } finally {
            // Closing all opened DB resources in finally block.
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this,
                            "Error closing result set: " + ex.getMessage(),
                            "Resource Close Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }

            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this,
                            "Error closing statement: " + ex.getMessage(),
                            "Resource Close Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }

            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this,
                            "Error closing connection: " + ex.getMessage(),
                            "Resource Close Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}
