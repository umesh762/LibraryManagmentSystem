import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public class MainDashboard extends JFrame {

    public MainDashboard() {
        // Main window setup.
        setTitle("Library Book Issue Management System");
        setSize(700, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Title section.
        JLabel titleLabel = new JLabel("Library Dashboard", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        add(titleLabel, BorderLayout.NORTH);

        // Center panel with action buttons.
        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JButton bookButton = createMenuButton("Book Management");
        JButton memberButton = createMenuButton("Member Management");
        JButton issueButton = createMenuButton("Issue Book");
        JButton returnButton = createMenuButton("Return Book");
        JButton recordsButton = createMenuButton("View Records");

        // Button actions to open each feature window.
        bookButton.addActionListener((ActionEvent e) -> openModule("BookManagement"));
        memberButton.addActionListener((ActionEvent e) -> openModule("MemberManagement"));
        issueButton.addActionListener((ActionEvent e) -> openModule("IssueBook"));
        returnButton.addActionListener((ActionEvent e) -> openModule("ReturnBook"));
        recordsButton.addActionListener((ActionEvent e) -> openModule("ViewRecords"));

        gbc.gridx = 0;

        gbc.gridy = 0;
        centerPanel.add(bookButton, gbc);

        gbc.gridy = 1;
        centerPanel.add(memberButton, gbc);

        gbc.gridy = 2;
        centerPanel.add(issueButton, gbc);

        gbc.gridy = 3;
        centerPanel.add(returnButton, gbc);

        gbc.gridy = 4;
        centerPanel.add(recordsButton, gbc);

        add(centerPanel, BorderLayout.CENTER);
    }

    // Helper method to keep button style consistent.
    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(260, 42));
        button.setFont(new Font("SansSerif", Font.PLAIN, 16));
        return button;
    }

    // Opens a JFrame module dynamically by class name.
    private void openModule(String className) {
        try {
            Class<?> moduleClass = Class.forName(className);
            Object moduleObject = moduleClass.getDeclaredConstructor().newInstance();

            if (moduleObject instanceof JFrame) {
                ((JFrame) moduleObject).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this,
                        className + " is not a JFrame module.",
                        "Module Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Unable to open " + className + ".\nPlease make sure the class exists.",
                    "Open Module Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Entry point of the application.
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainDashboard dashboard = new MainDashboard();
            dashboard.setVisible(true);
        });
    }
}
