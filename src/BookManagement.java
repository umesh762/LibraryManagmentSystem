import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class BookManagement extends JFrame {

    private JTextField titleField;
    private JTextField authorField;
    private JTextField totalCopiesField;
    private JTable bookTable;
    private DefaultTableModel tableModel;

    public BookManagement() {
        // Basic frame settings for the Book Management window.
        setTitle("Book Management");
        setSize(850, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Top form panel for adding new books.
        JPanel formPanel = new JPanel(new GridLayout(2, 4, 10, 10));

        formPanel.add(new JLabel("Title:"));
        titleField = new JTextField();
        formPanel.add(titleField);

        formPanel.add(new JLabel("Author:"));
        authorField = new JTextField();
        formPanel.add(authorField);

        formPanel.add(new JLabel("Total Copies:"));
        totalCopiesField = new JTextField();
        formPanel.add(totalCopiesField);

        JButton addBookButton = new JButton("Add Book");
        addBookButton.addActionListener(e -> addBook());
        formPanel.add(addBookButton);

        JButton refreshButton = new JButton("Refresh Books");
        refreshButton.addActionListener(e -> loadBooks());
        formPanel.add(refreshButton);

        add(formPanel, BorderLayout.NORTH);

        // Center table section to display all book records.
        tableModel = new DefaultTableModel(new String[] {
                "Book ID", "Title", "Author", "Total Copies", "Available Copies"
        }, 0);

        bookTable = new JTable(tableModel);
        add(new JScrollPane(bookTable), BorderLayout.CENTER);

        // Footer action area (optional spacing/alignment).
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        add(footerPanel, BorderLayout.SOUTH);

        // Initial load of all books when frame opens.
        loadBooks();
    }

    // Adds a new book and sets available_copies equal to total_copies.
    private void addBook() {
        String title = titleField.getText().trim();
        String author = authorField.getText().trim();
        String totalCopiesText = totalCopiesField.getText().trim();

        if (title.isEmpty() || author.isEmpty() || totalCopiesText.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please fill all fields before adding a book.",
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int totalCopies;
        try {
            totalCopies = Integer.parseInt(totalCopiesText);
            if (totalCopies <= 0) {
                JOptionPane.showMessageDialog(this,
                        "Total copies must be greater than 0.",
                        "Input Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Total copies must be a valid number.",
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = DBConnection.getConnection();

            String insertSql = "INSERT INTO books (title, author, total_copies, available_copies) VALUES (?, ?, ?, ?)";
            statement = connection.prepareStatement(insertSql);
            statement.setString(1, title);
            statement.setString(2, author);
            statement.setInt(3, totalCopies);
            statement.setInt(4, totalCopies);

            int rowsInserted = statement.executeUpdate();

            if (rowsInserted > 0) {
                JOptionPane.showMessageDialog(this,
                        "Book added successfully.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

                // Reset input fields after successful insert.
                titleField.setText("");
                authorField.setText("");
                totalCopiesField.setText("");

                loadBooks();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Book could not be added.",
                        "Insert Failed",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Database error while adding book: " + ex.getMessage(),
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

    // Fetches all books from database and loads them into JTable.
    private void loadBooks() {
        tableModel.setRowCount(0);

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DBConnection.getConnection();

            String selectSql = "SELECT book_id, title, author, total_copies, available_copies FROM books ORDER BY book_id";
            statement = connection.prepareStatement(selectSql);
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Object[] rowData = {
                        resultSet.getInt("book_id"),
                        resultSet.getString("title"),
                        resultSet.getString("author"),
                        resultSet.getInt("total_copies"),
                        resultSet.getInt("available_copies")
                };
                tableModel.addRow(rowData);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Database error while loading books: " + ex.getMessage(),
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
