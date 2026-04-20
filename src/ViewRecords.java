import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class ViewRecords extends JFrame {

    private JTable recordsTable;
    private DefaultTableModel tableModel;

    public ViewRecords() {
        // Basic frame settings for View Records window.
        setTitle("Issue Records");
        setSize(950, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Table setup to display joined issue records.
        tableModel = new DefaultTableModel(new String[] {
                "Issue ID", "Member Name", "Book Title", "Issue Date", "Return Date", "Status"
        }, 0);

        recordsTable = new JTable(tableModel);
        add(new JScrollPane(recordsTable), BorderLayout.CENTER);

        // Bottom action area for refreshing records.
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshButton = new JButton("Refresh Records");
        refreshButton.addActionListener(e -> loadIssueRecords());
        buttonPanel.add(refreshButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Initial load when frame opens.
        loadIssueRecords();
    }

    // Loads all issue records with member and book details using SQL joins.
    private void loadIssueRecords() {
        tableModel.setRowCount(0);

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DBConnection.getConnection();

            String selectSql = "SELECT ir.issue_id, m.name AS member_name, b.title AS book_title, "
                    + "ir.issue_date, ir.return_date, ir.status "
                    + "FROM issue_records ir "
                    + "JOIN members m ON ir.member_id = m.member_id "
                    + "JOIN books b ON ir.book_id = b.book_id "
                    + "ORDER BY ir.issue_id";

            statement = connection.prepareStatement(selectSql);
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Object[] rowData = {
                        resultSet.getInt("issue_id"),
                        resultSet.getString("member_name"),
                        resultSet.getString("book_title"),
                        resultSet.getDate("issue_date"),
                        resultSet.getDate("return_date"),
                        resultSet.getString("status")
                };
                tableModel.addRow(rowData);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Database error while loading records: " + ex.getMessage(),
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
