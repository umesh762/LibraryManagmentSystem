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

public class IssueBook extends JFrame {

    private JTextField memberIdField;
    private JTextField bookIdField;

    public IssueBook() {
        // Basic frame settings for Issue Book window.
        setTitle("Issue Book");
        setSize(450, 220);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Input section for member ID and book ID.
        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 10, 10));

        inputPanel.add(new JLabel("Member ID:"));
        memberIdField = new JTextField();
        inputPanel.add(memberIdField);

        inputPanel.add(new JLabel("Book ID:"));
        bookIdField = new JTextField();
        inputPanel.add(bookIdField);

        add(inputPanel, BorderLayout.CENTER);

        // Action section with Issue button.
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton issueButton = new JButton("Issue Book");
        issueButton.addActionListener(e -> issueBookToMember());
        buttonPanel.add(issueButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    // Issues a book only when valid member/book exist and copies are available.
    private void issueBookToMember() {
        String memberIdText = memberIdField.getText().trim();
        String bookIdText = bookIdField.getText().trim();

        if (memberIdText.isEmpty() || bookIdText.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter both Member ID and Book ID.",
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int memberId;
        int bookId;

        try {
            memberId = Integer.parseInt(memberIdText);
            bookId = Integer.parseInt(bookIdText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Member ID and Book ID must be valid numbers.",
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        Connection connection = null;
        PreparedStatement checkMemberStatement = null;
        PreparedStatement checkBookStatement = null;
        PreparedStatement insertIssueStatement = null;
        PreparedStatement updateBookStatement = null;
        ResultSet memberResultSet = null;
        ResultSet bookResultSet = null;

        try {
            connection = DBConnection.getConnection();
            connection.setAutoCommit(false);

            // Validate whether the member exists.
            String checkMemberSql = "SELECT member_id FROM members WHERE member_id = ?";
            checkMemberStatement = connection.prepareStatement(checkMemberSql);
            checkMemberStatement.setInt(1, memberId);
            memberResultSet = checkMemberStatement.executeQuery();

            if (!memberResultSet.next()) {
                JOptionPane.showMessageDialog(this,
                        "Member ID not found.",
                        "Issue Error",
                        JOptionPane.ERROR_MESSAGE);
                connection.rollback();
                return;
            }

            // Validate book existence and availability.
            String checkBookSql = "SELECT available_copies, title FROM books WHERE book_id = ?";
            checkBookStatement = connection.prepareStatement(checkBookSql);
            checkBookStatement.setInt(1, bookId);
            bookResultSet = checkBookStatement.executeQuery();

            if (!bookResultSet.next()) {
                JOptionPane.showMessageDialog(this,
                        "Book ID not found.",
                        "Issue Error",
                        JOptionPane.ERROR_MESSAGE);
                connection.rollback();
                return;
            }

            int availableCopies = bookResultSet.getInt("available_copies");
            String bookTitle = bookResultSet.getString("title");

            if (availableCopies <= 0) {
                JOptionPane.showMessageDialog(this,
                        "No available copies for the selected book.",
                        "Issue Error",
                        JOptionPane.ERROR_MESSAGE);
                connection.rollback();
                return;
            }

            // Insert issue record with today's date and issued status.
            String insertIssueSql = "INSERT INTO issue_records (book_id, member_id, issue_date, status) VALUES (?, ?, ?, ?)";
            insertIssueStatement = connection.prepareStatement(insertIssueSql);
            insertIssueStatement.setInt(1, bookId);
            insertIssueStatement.setInt(2, memberId);
            insertIssueStatement.setDate(3, Date.valueOf(LocalDate.now()));
            insertIssueStatement.setString(4, "issued");

            int issueRows = insertIssueStatement.executeUpdate();

            // Decrease available copies by 1 in books table.
            String updateBookSql = "UPDATE books SET available_copies = available_copies - 1 WHERE book_id = ?";
            updateBookStatement = connection.prepareStatement(updateBookSql);
            updateBookStatement.setInt(1, bookId);

            int updateRows = updateBookStatement.executeUpdate();

            if (issueRows > 0 && updateRows > 0) {
                connection.commit();
                JOptionPane.showMessageDialog(this,
                        "Book issued successfully.\nBook: " + bookTitle,
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

                // Clear fields after successful issue operation.
                memberIdField.setText("");
                bookIdField.setText("");
            } else {
                connection.rollback();
                JOptionPane.showMessageDialog(this,
                        "Book issue failed. Please try again.",
                        "Issue Failed",
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
                    "Database error while issuing book: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        } finally {
            // Closing all opened DB resources in finally block.
            if (bookResultSet != null) {
                try {
                    bookResultSet.close();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this,
                            "Error closing result set: " + ex.getMessage(),
                            "Resource Close Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }

            if (memberResultSet != null) {
                try {
                    memberResultSet.close();
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

            if (insertIssueStatement != null) {
                try {
                    insertIssueStatement.close();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this,
                            "Error closing statement: " + ex.getMessage(),
                            "Resource Close Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }

            if (checkBookStatement != null) {
                try {
                    checkBookStatement.close();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this,
                            "Error closing statement: " + ex.getMessage(),
                            "Resource Close Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }

            if (checkMemberStatement != null) {
                try {
                    checkMemberStatement.close();
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
