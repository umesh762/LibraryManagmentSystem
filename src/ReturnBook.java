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
import javax.swing.JTextField;

public class ReturnBook extends JFrame {

    private JTextField issueIdField;

    public ReturnBook() {
        // Basic frame settings for Return Book window.
        setTitle("Return Book");
        setSize(420, 200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Input section for issue ID.
        JPanel inputPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        inputPanel.add(new JLabel("Issue ID:"));
        issueIdField = new JTextField();
        inputPanel.add(issueIdField);
        add(inputPanel, BorderLayout.CENTER);

        // Action section with Return button.
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton returnButton = new JButton("Return Book");
        returnButton.addActionListener(e -> returnIssuedBook());
        buttonPanel.add(returnButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    // Returns an issued book by issue_id and updates copies safely.
    private void returnIssuedBook() {
        String issueIdText = issueIdField.getText().trim();

        if (issueIdText.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter Issue ID.",
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int issueId;
        try {
            issueId = Integer.parseInt(issueIdText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Issue ID must be a valid number.",
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        Connection connection = null;
        PreparedStatement fetchIssueStatement = null;
        PreparedStatement updateIssueStatement = null;
        PreparedStatement updateBookStatement = null;
        ResultSet issueResultSet = null;

        try {
            connection = DBConnection.getConnection();
            connection.setAutoCommit(false);

            // Fetch issue record and validate current status.
            String fetchIssueSql = "SELECT book_id, status FROM issue_records WHERE issue_id = ?";
            fetchIssueStatement = connection.prepareStatement(fetchIssueSql);
            fetchIssueStatement.setInt(1, issueId);
            issueResultSet = fetchIssueStatement.executeQuery();

            if (!issueResultSet.next()) {
                JOptionPane.showMessageDialog(this,
                        "Issue ID not found.",
                        "Return Error",
                        JOptionPane.ERROR_MESSAGE);
                connection.rollback();
                return;
            }

            int bookId = issueResultSet.getInt("book_id");
            String currentStatus = issueResultSet.getString("status");

            if ("returned".equalsIgnoreCase(currentStatus)) {
                JOptionPane.showMessageDialog(this,
                        "This book is already marked as returned.",
                        "Return Error",
                        JOptionPane.ERROR_MESSAGE);
                connection.rollback();
                return;
            }

            // Update issue record with return date and returned status.
            String updateIssueSql = "UPDATE issue_records SET return_date = ?, status = ? WHERE issue_id = ?";
            updateIssueStatement = connection.prepareStatement(updateIssueSql);
            updateIssueStatement.setDate(1, Date.valueOf(LocalDate.now()));
            updateIssueStatement.setString(2, "returned");
            updateIssueStatement.setInt(3, issueId);

            int issueUpdatedRows = updateIssueStatement.executeUpdate();

            // Increase available copies in books table.
            String updateBookSql = "UPDATE books SET available_copies = available_copies + 1 WHERE book_id = ?";
            updateBookStatement = connection.prepareStatement(updateBookSql);
            updateBookStatement.setInt(1, bookId);

            int bookUpdatedRows = updateBookStatement.executeUpdate();

            if (issueUpdatedRows > 0 && bookUpdatedRows > 0) {
                connection.commit();
                JOptionPane.showMessageDialog(this,
                        "Book returned successfully.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

                // Clear input field after successful return.
                issueIdField.setText("");
            } else {
                connection.rollback();
                JOptionPane.showMessageDialog(this,
                        "Return operation failed. Please try again.",
                        "Return Failed",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    JOptionPane.showMessageDialog(this,
                            "Error during rollback: " + rollbackEx.getMessage(),
                            "Rollback Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }

            JOptionPane.showMessageDialog(this,
                    "Database error while returning book: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        } finally {
            // Closing all opened DB resources in finally block.
            if (issueResultSet != null) {
                try {
                    issueResultSet.close();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this,
                            "Error closing result set: " + ex.getMessage(),
                            "Resource Close Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }

            if (updateBookStatement != null) {
                try {
                    updateBookStatement.close();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this,
                            "Error closing statement: " + ex.getMessage(),
                            "Resource Close Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }

            if (updateIssueStatement != null) {
                try {
                    updateIssueStatement.close();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this,
                            "Error closing statement: " + ex.getMessage(),
                            "Resource Close Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }

            if (fetchIssueStatement != null) {
                try {
                    fetchIssueStatement.close();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this,
                            "Error closing statement: " + ex.getMessage(),
                            "Resource Close Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }

            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
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
