import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import javax.swing.*;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import javax.swing.border.*;

@SuppressWarnings("unused")
public class BloodBankGUI extends JFrame {
    private static User currentUser = null;
    private static AuthService authService = new AuthService();
    private static BloodInventoryService inventoryService = new BloodInventoryService();
    private static AdminService adminService = new AdminService();
    private static HospitalService hospitalService = new HospitalService();
    
    // Color Scheme
    private static final Color PRIMARY_COLOR = new Color(220, 53, 69);      // Medical Red
    private static final Color SECONDARY_COLOR = new Color(40, 44, 52);     // Dark Gray
    private static final Color BACKGROUND_COLOR = new Color(248, 249, 250); // Light Gray
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color TEXT_PRIMARY = new Color(33, 37, 41);
    private static final Color TEXT_SECONDARY = new Color(108, 117, 125);
    
    private JPanel mainPanel;
    private CardLayout cardLayout;
    
    // Urgent Alert System
    private boolean alertDismissed = false;
    private JLabel urgentAlertIndicator = null;
    private java.util.List<String> criticalBloodGroups = new java.util.ArrayList<>();
    
    public BloodBankGUI() {
        setTitle("One Drop, One Life - Blood Bank Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 750);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Set system look and feel with fallback
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        // Test database connection - redirect System.err to capture error details
        java.io.ByteArrayOutputStream errContent = new java.io.ByteArrayOutputStream();
        java.io.PrintStream originalErr = System.err;
        System.setErr(new java.io.PrintStream(errContent));
        
        Connection testConnection = DBConnection.getConnection();
        
        System.setErr(originalErr);
        
        if (testConnection == null) {
            String errorDetails = errContent.toString();
            if (errorDetails.isEmpty()) {
                errorDetails = "Unknown error. Please check:\n" +
                              "1. MySQL server is running\n" +
                              "2. Database 'bloodbankdb' exists\n" +
                              "3. Username and password are correct\n" +
                              "4. MySQL JDBC driver is in classpath";
            }
            
            JTextArea textArea = new JTextArea(errorDetails);
            textArea.setEditable(false);
            textArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(600, 300));
            
            JOptionPane.showMessageDialog(this, 
                scrollPane,
                "Database Connection Error", 
                JOptionPane.ERROR_MESSAGE);
            
            System.err.println("Database connection failed. Application will close.");
            System.exit(1);
        } else {
            System.out.println("GUI: Database connected successfully!");
            // Fix schema if needed
            SchemaFixer.fixFailedBloodTable();
            TriggerFixer.fixTriggers();
        }
        
        mainPanel.add(createLoginPanel(), "LOGIN");
        
        add(mainPanel);
        cardLayout.show(mainPanel, "LOGIN");
    }
    
    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        
        // Left side - Branding
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(PRIMARY_COLOR);
        leftPanel.setPreferredSize(new Dimension(500, 750));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(80, 50, 80, 50));
        
        JLabel logoLabel = new JLabel("💉");
        logoLabel.setFont(new java.awt.Font("Segoe UI Emoji", java.awt.Font.PLAIN, 120));
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel titleLabel = new JLabel("One Drop, One Life");
        titleLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 38));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel sloganLabel = new JLabel("From our hearts to your veins");
        sloganLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.ITALIC, 18));
        sloganLabel.setForeground(new Color(255, 255, 255, 200));
        sloganLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        leftPanel.add(Box.createVerticalGlue());
        leftPanel.add(logoLabel);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        leftPanel.add(titleLabel);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        leftPanel.add(sloganLabel);
        leftPanel.add(Box.createVerticalGlue());
        
        // Right side - Login Form
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new GridBagLayout());
        rightPanel.setBackground(BACKGROUND_COLOR);
        
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(CARD_COLOR);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(222, 226, 230), 1, true),
            BorderFactory.createEmptyBorder(40, 40, 40, 40)
        ));
        formPanel.setPreferredSize(new Dimension(400, 450));
        
        JLabel welcomeLabel = new JLabel("Welcome Back");
        welcomeLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 28));
        welcomeLabel.setForeground(TEXT_PRIMARY);
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel subtitleLabel = new JLabel("Sign in to continue");
        subtitleLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        subtitleLabel.setForeground(TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JTextField usernameField = createStyledTextField("Username");
        JPasswordField passwordField = createStyledPasswordField("Password");
        
        JButton loginButton = createPrimaryButton("Sign In");
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        loginButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            
            if (username.isEmpty() || password.isEmpty()) {
                showError(this, "Please enter both username and password");
                return;
            }
            
            currentUser = authService.login(username, password);
            
            if (currentUser != null) {
                showSuccess(this, "Welcome, " + currentUser.getFullName() + "!");
                openDashboard();
            } else {
                showError(this, "Invalid username or password");
                passwordField.setText("");
            }
        });
        
        // Enter key support
        ActionListener loginAction = e -> loginButton.doClick();
        usernameField.addActionListener(loginAction);
        passwordField.addActionListener(loginAction);
        
        formPanel.add(welcomeLabel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        formPanel.add(subtitleLabel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 40)));
        formPanel.add(usernameField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        formPanel.add(passwordField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        formPanel.add(loginButton);
        
        rightPanel.add(formPanel);
        
        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(rightPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    // ==================== DASHBOARD ====================
    
    private void openDashboard() {
        JPanel dashboardPanel = createDashboardPanel();
        mainPanel.add(dashboardPanel, "DASHBOARD");
        cardLayout.show(mainPanel, "DASHBOARD");
        
        // Check for urgent alerts after a short delay (let UI render first)
        SwingUtilities.invokeLater(() -> {
            try {
                Thread.sleep(300);
                checkAndShowUrgentAlertPopup();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }
    
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        
        // Top Navigation Bar
        JPanel navbar = createNavbar();
        panel.add(navbar, BorderLayout.NORTH);
        
        // Content Area
        JPanel contentArea = new JPanel(new BorderLayout());
        contentArea.setBackground(BACKGROUND_COLOR);
        contentArea.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        String role = currentUser.getRole();
        
        if ("Admin".equals(role)) {
            contentArea.add(createAdminDashboard(), BorderLayout.CENTER);
        } else if ("Technician".equals(role)) {
            contentArea.add(createTechnicianDashboard(), BorderLayout.CENTER);
        } else if ("Staff".equals(role)) {
            contentArea.add(createStaffDashboard(), BorderLayout.CENTER);
        }
        
        panel.add(contentArea, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createNavbar() {
        JPanel navbar = new JPanel(new BorderLayout());
        navbar.setBackground(SECONDARY_COLOR);
        navbar.setPreferredSize(new Dimension(1200, 70));
        navbar.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 30));
        
        JPanel leftSection = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        leftSection.setOpaque(false);
        
        JLabel logoText = new JLabel("💉 One Drop, One Life");
        logoText.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 20));
        logoText.setForeground(Color.WHITE);
        logoText.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        leftSection.add(logoText);
        
        JPanel rightSection = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightSection.setOpaque(false);
        
        JLabel userLabel = new JLabel(currentUser.getFullName() + " (" + currentUser.getRole() + ")");
        userLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        userLabel.setForeground(Color.WHITE);
        userLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        // Urgent Alert Indicator
        urgentAlertIndicator = new JLabel();
        urgentAlertIndicator.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        urgentAlertIndicator.setForeground(Color.WHITE);
        urgentAlertIndicator.setBackground(new Color(220, 53, 69));
        urgentAlertIndicator.setOpaque(true);
        urgentAlertIndicator.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 33, 49), 2),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        urgentAlertIndicator.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        urgentAlertIndicator.setVisible(false);
        urgentAlertIndicator.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                checkAlerts();
            }
        });
        updateAlertIndicator();
        
        JButton logoutButton = createNavButton("Logout");
        logoutButton.addActionListener(e -> logout());
        
        rightSection.add(urgentAlertIndicator);
        rightSection.add(userLabel);
        rightSection.add(logoutButton);
        
        navbar.add(leftSection, BorderLayout.WEST);
        navbar.add(rightSection, BorderLayout.EAST);
        
        return navbar;
    }
    
    // ==================== ADMIN DASHBOARD ====================
    
    private JPanel createAdminDashboard() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("Admin Dashboard");
        titleLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 32));
        titleLabel.setForeground(TEXT_PRIMARY);
        
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Stats and Quick Actions
        JPanel contentPanel = new JPanel(new GridLayout(0, 4, 20, 20));
        contentPanel.setOpaque(false);
        
        contentPanel.add(createDashboardCard("Users", "Manage System Users", "👥", e -> openUserManagement()));
        contentPanel.add(createDashboardCard("Hospitals", "Hospital Approvals", "🏥", e -> openHospitalManagement()));
        contentPanel.add(createDashboardCard("Inventory", "Blood Stock Levels", "🩸", e -> openInventoryView()));
        contentPanel.add(createDashboardCard("Requests", "Blood Requests", "📋", e -> openRequestsView()));
        contentPanel.add(createDashboardCard("System Logs", "Activity Logs", "📊", e -> openSystemLogs()));
        contentPanel.add(createDashboardCard("Reports", "Generate Reports", "📈", e -> generateReport()));
        contentPanel.add(createDashboardCard("Failed Blood", "View Failed/Expired", "🚫", e -> openFailedBloodView()));
        contentPanel.add(createDashboardCard("Alerts", "Urgent Alerts", "⚠️", e -> checkAlerts()));
        contentPanel.add(createDashboardCard("Settings", "System Settings", "🛠️", e -> openSettings()));
        
        panel.add(contentPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    // ==================== TECHNICIAN DASHBOARD ====================
    
    private JPanel createTechnicianDashboard() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("Technician Dashboard");
        titleLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 32));
        titleLabel.setForeground(TEXT_PRIMARY);
        
        panel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel contentPanel = new JPanel(new GridLayout(2, 3, 20, 20));
        contentPanel.setOpaque(false);
        
        contentPanel.add(createDashboardCard("Register Donor", "Add New Donor", "➕", e -> openDonorRegistration()));
        contentPanel.add(createDashboardCard("Record Donation", "Log Blood Donation", "💉", e -> openDonationRecord()));
        contentPanel.add(createDashboardCard("View Donors", "All Registered Donors", "👥", e -> openDonorsList()));
        contentPanel.add(createDashboardCard("Search Donors", "By Blood Group", "🔍", e -> openDonorSearch()));
        contentPanel.add(createDashboardCard("Inventory", "Check Blood Stock", "🩸", e -> openInventoryView()));
        contentPanel.add(createDashboardCard("Reports", "Donation Reports", "📊", e -> generateReport()));
        
        panel.add(contentPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    // ==================== STAFF DASHBOARD ====================
    
    private JPanel createStaffDashboard() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("Staff Dashboard");
        titleLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 32));
        titleLabel.setForeground(TEXT_PRIMARY);
        
        panel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel contentPanel = new JPanel(new GridLayout(2, 3, 20, 20));
        contentPanel.setOpaque(false);
        
        contentPanel.add(createDashboardCard("Process Tests", "Blood Test Results", "🔬", e -> openTestProcessing()));
        contentPanel.add(createDashboardCard("Pending Tests", "View Pending", "⏳", e -> openPendingTests()));
        contentPanel.add(createDashboardCard("Blood Requests", "Manage Requests", "📋", e -> openRequestManagement()));
        contentPanel.add(createDashboardCard("Inventory", "Check Stock", "🩸", e -> openInventoryView()));
        contentPanel.add(createDashboardCard("Register Hospital", "Add Hospital", "🏥", e -> openHospitalRegistration()));
        contentPanel.add(createDashboardCard("Add Patient", "Register Patient", "🧑‍⚕️", e -> openPatientRegistration()));
        
        panel.add(contentPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    // ==================== DASHBOARD CARD ====================
    
    private JPanel createDashboardCard(String title, String subtitle, String icon, ActionListener action) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_COLOR);
        // Reduced vertical padding to prevent cutting off content
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(222, 226, 230), 1, true),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.setPreferredSize(new Dimension(250, 180));
        card.setMinimumSize(new Dimension(250, 180));
        card.setMaximumSize(new Dimension(250, 180));
        
        // Hover effect
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(new Color(248, 249, 250));
                card.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(PRIMARY_COLOR, 2, true),
                    BorderFactory.createEmptyBorder(19, 19, 19, 19)
                ));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(CARD_COLOR);
                card.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(222, 226, 230), 1, true),
                    BorderFactory.createEmptyBorder(20, 20, 20, 20)
                ));
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                if (action != null) {
                    action.actionPerformed(new ActionEvent(card, ActionEvent.ACTION_PERFORMED, null));
                }
            }
        });
        
        // Icon - Reduced font size to prevent clipping
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new java.awt.Font("Segoe UI Emoji", java.awt.Font.PLAIN, 40));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        // Add padding to the icon label itself to prevent top clipping
        iconLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        // Ensure the label has enough height
        iconLabel.setPreferredSize(new Dimension(60, 60));
        iconLabel.setMaximumSize(new Dimension(60, 60));
        
        // Title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 18));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Subtitle
        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        subtitleLabel.setForeground(TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Add spacing to center content vertically
        card.add(Box.createVerticalGlue());
        card.add(iconLabel);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(titleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 5)));
        card.add(subtitleLabel);
        card.add(Box.createVerticalGlue());
        
        return card;
    }
    
    // ==================== COMMON UI ELEMENTS ====================
    
    private JTextField createStyledTextField(String placeholder) {
        JTextField field = new JTextField();
        field.setPreferredSize(new Dimension(320, 45));
        field.setMaximumSize(new Dimension(320, 45));
        field.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(206, 212, 218), 1, true),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        field.setForeground(TEXT_PRIMARY);
        
        // Placeholder effect
        field.setText(placeholder);
        field.setForeground(TEXT_SECONDARY);
        
        field.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(TEXT_PRIMARY);
                }
            }
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(TEXT_SECONDARY);
                }
            }
        });
        
        return field;
    }
    
    private JPasswordField createStyledPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField();
        field.setPreferredSize(new Dimension(320, 45));
        field.setMaximumSize(new Dimension(320, 45));
        field.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(206, 212, 218), 1, true),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        field.setEchoChar((char) 0);
        field.setForeground(TEXT_SECONDARY);
        field.setText(placeholder);
        
        field.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (String.valueOf(field.getPassword()).equals(placeholder)) {
                    field.setText("");
                    field.setEchoChar('•');
                    field.setForeground(TEXT_PRIMARY);
                }
            }
            public void focusLost(FocusEvent e) {
                if (field.getPassword().length == 0) {
                    field.setEchoChar((char) 0);
                    field.setText(placeholder);
                    field.setForeground(TEXT_SECONDARY);
                }
            }
        });
        
        return field;
    }
    
    private JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(320, 45));
        button.setMaximumSize(new Dimension(320, 45));
        button.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(PRIMARY_COLOR);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setVerticalAlignment(SwingConstants.CENTER);
        
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(200, 43, 59));
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(PRIMARY_COLOR);
            }
        });
        
        return button;
    }
    
    
    private JButton createNavButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(60, 64, 72));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setVerticalAlignment(SwingConstants.CENTER);
        
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(80, 84, 92));
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(60, 64, 72));
            }
        });
        
        return button;
    }
    
    // ==================== DIALOG METHODS ====================
    
    private void openUserManagement() {
        JDialog dialog = new JDialog(this, "User Management", true);
        dialog.setSize(1000, 650);
        dialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(BACKGROUND_COLOR);
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(245, 247, 250));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY_COLOR),
            BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));
        
        JLabel titleLabel = new JLabel("👥 User Management");
        titleLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 26));
        titleLabel.setForeground(PRIMARY_COLOR);
        
        JButton addUserBtn = createStyledActionButton("➕ Add User", new Color(46, 204, 113));
        addUserBtn.addActionListener(e -> showAddUserDialog(dialog));
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(addUserBtn, BorderLayout.EAST);
        
        // Users Table
        JPanel contentPanel = new JPanel(new BorderLayout(15, 15));
        contentPanel.setBackground(BACKGROUND_COLOR);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        String[] columns = {"ID", "Username", "Full Name", "Role", "Actions"};
        List<User> users = adminService.getAllUsers();
        Object[][] data = new Object[users.size()][5];
        
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            data[i][0] = user.getUserID();
            data[i][1] = user.getUsername();
            data[i][2] = user.getFullName();
            data[i][3] = user.getRole();
            data[i][4] = "Delete";
        }
        
        JTable table = new JTable(data, columns) {
            public boolean isCellEditable(int row, int column) {
                return column == 4;
            }
        };
        
        table.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        table.setRowHeight(45);
        table.setShowGrid(true);
        table.setGridColor(new Color(230, 230, 230));
        styleTableHeader(table);
        table.setSelectionBackground(new Color(220, 53, 69, 30));
        table.setSelectionForeground(Color.BLACK);

        // Center alignment for all columns
        javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            if (i != 4) { // Skip actions column as it gets custom renderer
                table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }
        
        // Add delete button renderer
        table.getColumn("Actions").setCellRenderer((tbl, value, isSelected, hasFocus, row, column) -> {
            JButton btn = new JButton("🗑 Delete");
            btn.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
            btn.setForeground(Color.WHITE);
            btn.setBackground(new Color(231, 76, 60));
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            return btn;
        });
        
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (col == 4 && row >= 0) {
                    int userId = (int) table.getValueAt(row, 0);
                    String userName = (String) table.getValueAt(row, 2);
                    
                    int confirm = JOptionPane.showConfirmDialog(dialog,
                        "Are you sure you want to delete user: " + userName + "?",
                        "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    
                    if (confirm == JOptionPane.YES_OPTION) {
                        if (adminService.deleteUser(userId)) {
                            showSuccess(dialog, "User deleted successfully!");
                            dialog.dispose();
                            openUserManagement();
                        } else {
                            showError(dialog, "Failed to delete user!");
                        }
                    }
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        
        JLabel infoLabel = new JLabel("Total Users: " + users.size());
        infoLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        infoLabel.setForeground(TEXT_SECONDARY);
        
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.add(infoLabel, BorderLayout.SOUTH);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    private void showAddUserDialog(JDialog parent) {
        JDialog dialog = new JDialog(parent, "Add New User", true);
        dialog.setSize(500, 450);
        dialog.setLocationRelativeTo(parent);
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        panel.setBackground(CARD_COLOR);
        
        JLabel titleLabel = new JLabel("Create New User Account");
        titleLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 20));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(25));
        
        JTextField usernameField = createStyledTextField("Username");
        JPasswordField passwordField = createStyledPasswordField("Password");
        JTextField fullNameField = createStyledTextField("Full Name");
        
        JPanel rolePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rolePanel.setOpaque(false);
        JLabel roleLabel = new JLabel("Role:");
        roleLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        String[] roles = {"Admin", "Technician", "Staff"};
        JComboBox<String> roleCombo = new JComboBox<>(roles);
        roleCombo.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        roleCombo.setPreferredSize(new Dimension(200, 35));
        rolePanel.add(roleLabel);
        rolePanel.add(Box.createHorizontalStrut(10));
        rolePanel.add(roleCombo);
        
        panel.add(usernameField);
        panel.add(Box.createVerticalStrut(15));
        panel.add(passwordField);
        panel.add(Box.createVerticalStrut(15));
        panel.add(fullNameField);
        panel.add(Box.createVerticalStrut(15));
        panel.add(rolePanel);
        panel.add(Box.createVerticalStrut(25));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setOpaque(false);
        
        JButton createBtn = createStyledActionButton("✓ Create User", new Color(46, 204, 113));
        createBtn.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String fullName = fullNameField.getText().trim();
            String role = (String) roleCombo.getSelectedItem();
            
            if (username.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
                showError(dialog, "All fields are required!");
                return;
            }
            
            if (adminService.createUser(username, password, fullName, role)) {
                showSuccess(dialog, "User created successfully!");
                dialog.dispose();
                parent.dispose();
                openUserManagement();
            } else {
                showError(dialog, "Failed to create user. Username may already exist.");
            }
        });
        
        JButton cancelBtn = createStyledActionButton("✕ Cancel", new Color(149, 165, 166));
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(createBtn);
        buttonPanel.add(cancelBtn);
        panel.add(buttonPanel);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private void openHospitalManagement() {
        JDialog dialog = new JDialog(this, "Hospital Management", true);
        dialog.setSize(1100, 700);
        dialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(245, 247, 250));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY_COLOR),
            BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));
        
        JLabel titleLabel = new JLabel("🏥 Hospital Management");
        titleLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 26));
        titleLabel.setForeground(PRIMARY_COLOR);
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Tabs for Pending and All Hospitals
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        tabbedPane.setBackground(BACKGROUND_COLOR);
        
        tabbedPane.addTab("⏳ Pending Approval", createHospitalTablePanel(adminService.getPendingHospitals(), true, dialog));
        tabbedPane.addTab("✓ All Hospitals", createHospitalTablePanel(hospitalService.getAllHospitals(), false, dialog));
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    private JPanel createHospitalTablePanel(List<Hospital> hospitals, boolean showActions, JDialog parentDialog) {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        String[] columns = showActions ? 
            new String[]{"ID", "Hospital Name", "Address", "Status", "Actions"} :
            new String[]{"ID", "Hospital Name", "Address", "Status"};
        
        Object[][] data = new Object[hospitals.size()][columns.length];
        
        for (int i = 0; i < hospitals.size(); i++) {
            Hospital hospital = hospitals.get(i);
            data[i][0] = hospital.getHospitalID();
            data[i][1] = hospital.getHospitalName();
            data[i][2] = hospital.getAddress();
            data[i][3] = hospital.getStatus();
            if (showActions) {
                data[i][4] = "Approve / Reject";
            }
        }
        
        JTable table = new JTable(data, columns) {
            public boolean isCellEditable(int row, int column) {
                return showActions && column == 4;
            }
        };
        
        table.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        table.setRowHeight(50);
        table.setShowGrid(true);
        table.setGridColor(new Color(230, 230, 230));
        styleTableHeader(table);
        table.setSelectionBackground(new Color(220, 53, 69, 30));
        table.setSelectionForeground(Color.BLACK);

        // Center alignment
        javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            if (!showActions || i != 4) {
                table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }
        
        if (showActions) {
            table.getColumn("Actions").setCellRenderer((tbl, value, isSelected, hasFocus, row, column) -> {
                JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
                btnPanel.setOpaque(false);
                
                JButton approveBtn = new JButton("✓");
                approveBtn.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
                approveBtn.setForeground(Color.WHITE);
                approveBtn.setBackground(new Color(46, 204, 113));
                approveBtn.setPreferredSize(new Dimension(50, 30));
                approveBtn.setBorderPainted(false);
                
                JButton rejectBtn = new JButton("✕");
                rejectBtn.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
                rejectBtn.setForeground(Color.WHITE);
                rejectBtn.setBackground(new Color(231, 76, 60));
                rejectBtn.setPreferredSize(new Dimension(50, 30));
                rejectBtn.setBorderPainted(false);
                
                btnPanel.add(approveBtn);
                btnPanel.add(rejectBtn);
                return btnPanel;
            });
            
            table.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    int row = table.rowAtPoint(e.getPoint());
                    int col = table.columnAtPoint(e.getPoint());
                    if (col == 4 && row >= 0) {
                        int hospitalId = (int) table.getValueAt(row, 0);
                        String hospitalName = (String) table.getValueAt(row, 1);
                        
                        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
                        JButton approveBtn = new JButton("✓ Approve");
                        approveBtn.setBackground(new Color(46, 204, 113));
                        approveBtn.setForeground(Color.WHITE);
                        approveBtn.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
                        approveBtn.setBorderPainted(false);
                        
                        JButton rejectBtn = new JButton("✕ Reject");
                        rejectBtn.setBackground(new Color(231, 76, 60));
                        rejectBtn.setForeground(Color.WHITE);
                        rejectBtn.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
                        rejectBtn.setBorderPainted(false);
                        
                        actionPanel.add(approveBtn);
                        actionPanel.add(rejectBtn);
                        
                        int choice = JOptionPane.showConfirmDialog(parentDialog, actionPanel,
                            "Action for: " + hospitalName, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                        
                        if (choice == JOptionPane.OK_OPTION) {
                            // Determine which button was conceptually "clicked"
                            int action = JOptionPane.showOptionDialog(parentDialog,
                                "Choose action for " + hospitalName,
                                "Hospital Action",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE,
                                null,
                                new String[]{"Approve", "Reject"},
                                "Approve");
                            
                            if (action == 0) {
                                if (adminService.approveHospital(hospitalId, currentUser.getUserID())) {
                                    showSuccess(parentDialog, "Hospital approved successfully!");
                                    parentDialog.dispose();
                                    openHospitalManagement();
                                }
                            } else if (action == 1) {
                                if (adminService.rejectHospital(hospitalId, currentUser.getUserID())) {
                                    showSuccess(parentDialog, "Hospital rejected!");
                                    parentDialog.dispose();
                                    openHospitalManagement();
                                }
                            }
                        }
                    }
                }
            });
        }
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        
        JLabel infoLabel = new JLabel("Total: " + hospitals.size() + " hospitals");
        infoLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        infoLabel.setForeground(TEXT_SECONDARY);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(infoLabel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void openInventoryView() {
        JDialog dialog = new JDialog(this, "Blood Inventory", true);
        dialog.setSize(1000, 650);
        dialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(245, 247, 250));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY_COLOR),
            BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));
        
        JLabel titleLabel = new JLabel("🩸 Blood Inventory Dashboard");
        titleLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 26));
        titleLabel.setForeground(PRIMARY_COLOR);
        
        JButton refreshBtn = createStyledActionButton("🔄 Refresh", new Color(52, 152, 219));
        refreshBtn.addActionListener(e -> {
            dialog.dispose();
            openInventoryView();
        });
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(refreshBtn, BorderLayout.EAST);
        
        // Content
        JPanel contentPanel = new JPanel(new BorderLayout(15, 15));
        contentPanel.setBackground(BACKGROUND_COLOR);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        var inventory = inventoryService.getInventorySummary();
        String[] columns = {"Blood Group", "Available Quantity", "Status", "Level"};
        String[][] data = new String[8][4];
        String[] bloodGroups = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        
        double totalUnits = 0;
        for (int i = 0; i < bloodGroups.length; i++) {
            int quantityML = inventory.getOrDefault(bloodGroups[i], 0);
            double units = quantityML / (double) ML_PER_UNIT;
            totalUnits += units;
            
            data[i][0] = bloodGroups[i];
            data[i][1] = String.format("%.1f units (%d ml)", units, quantityML);
            
            // === INVENTORY STATUS & LEVEL LOGIC ===
            // Thresholds (in units):
            // - CRITICAL_THRESHOLD = 3 units (Below this is Critical)
            // - LOW_THRESHOLD = 10 units (Below this is Status: Low)
            // - HIGH_THRESHOLD = 20 units (Above this is Level: High)
            
            double criticalThreshold = CRITICAL_UNITS_THRESHOLD * ML_PER_UNIT; // 3 units
            double lowThreshold = 10 * ML_PER_UNIT; // 10 units
            double highThreshold = 20 * ML_PER_UNIT; // 20 units
            
            // 1. Determine Status (Only "Available" or "Low")
            if (quantityML < lowThreshold) {
                data[i][2] = "Low";
                
                // 2. Determine Level for Low Status
                if (quantityML < criticalThreshold) {
                    data[i][3] = "Critical"; // Below 3 units
                } else {
                    data[i][3] = "Low";      // Between 3 and 10 units
                }
            } else {
                data[i][2] = "Available";
                
                // 2. Determine Level for Available Status
                if (quantityML >= highThreshold) {
                    data[i][3] = "High";     // Above 20 units
                } else {
                    data[i][3] = "Moderate"; // Between 10 and 20 units
                }
            }
        }
        
        JTable table = new JTable(data, columns);
        table.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 15));
        table.setRowHeight(50);
        table.setShowGrid(true);
        table.setGridColor(new Color(230, 230, 230));
        
        // Table header styling
        styleTableHeader(table);
        table.setSelectionBackground(new Color(220, 53, 69, 30));
        table.setSelectionForeground(TEXT_PRIMARY);
        
        // Center align cells
        javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        // Color rows by LEVEL (Column 3) - override center renderer
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                
                String level = (String) table.getValueAt(row, 3); // Check Level column
                
                if (!isSelected) {
                    if ("Critical".equals(level)) {
                        c.setBackground(new Color(248, 215, 218)); // Red
                    } else if ("Low".equals(level)) {
                        c.setBackground(new Color(255, 243, 205)); // Orange/Yellow
                    } else if ("Moderate".equals(level)) {
                        c.setBackground(new Color(255, 249, 230)); // Light Yellow
                    } else { // High
                        c.setBackground(new Color(212, 237, 218)); // Green
                    }
                }
                return c;
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        
        // Stats Summary
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        statsPanel.setOpaque(false);
        statsPanel.setPreferredSize(new Dimension(0, 90));
        
        int critical = 0, low = 0, moderate = 0, high = 0;
        for (String[] row : data) {
            switch (row[3]) { // Count based on Level
                case "Critical": critical++; break;
                case "Low": low++; break;
                case "Moderate": moderate++; break;
                case "High": high++; break;
            }
        }
        
        statsPanel.add(createMiniStatCard("Total Units", String.format("%.1f", totalUnits), new Color(52, 152, 219)));
        statsPanel.add(createMiniStatCard("High", String.valueOf(high), new Color(40, 167, 69)));
        statsPanel.add(createMiniStatCard("Moderate", String.valueOf(moderate), new Color(255, 193, 7)));
        statsPanel.add(createMiniStatCard("Low/Critical", String.valueOf(low + critical), new Color(220, 53, 69)));
        
        contentPanel.add(statsPanel, BorderLayout.NORTH);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    private void openRequestsView() {
        JDialog dialog = new JDialog(this, "Blood Requests", true);
        dialog.setSize(1150, 700);
        dialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(245, 247, 250));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY_COLOR),
            BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));
        
        JLabel titleLabel = new JLabel("📋 Blood Requests Management");
        titleLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 26));
        titleLabel.setForeground(PRIMARY_COLOR);
        
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        // Content
        JPanel contentPanel = new JPanel(new BorderLayout(15, 15));
        contentPanel.setBackground(BACKGROUND_COLOR);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        BloodRequestService requestService = new BloodRequestService();
        List<BloodRequest> requests = requestService.getAllRequests();
        
        String[] columns = {"Request ID", "Patient ID", "Blood Group", "Quantity", "Request Date", "Status"};
        Object[][] data = new Object[requests.size()][6];
        
        for (int i = 0; i < requests.size(); i++) {
            BloodRequest req = requests.get(i);
            data[i][0] = req.getRequestID();
            data[i][1] = req.getPatientID();
            data[i][2] = req.getRequestedBloodGroup();
            data[i][3] = req.getQuantity() + " units";
            data[i][4] = req.getRequestDate();
            data[i][5] = req.getStatus();
        }
        
        JTable table = new JTable(data, columns);
        table.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        table.setRowHeight(45);
        table.setShowGrid(true);
        table.setGridColor(new Color(230, 230, 230));
        styleTableHeader(table);
        table.setSelectionBackground(new Color(220, 53, 69, 30));
        table.setSelectionForeground(Color.BLACK);
        
        // Color code rows by status
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                String status = (String) table.getValueAt(row, 5);
                
                if (!isSelected) {
                    if ("Pending".equals(status)) {
                        c.setBackground(new Color(255, 243, 205));
                    } else if ("Fulfilled".equals(status)) {
                        c.setBackground(new Color(212, 237, 218));
                    } else if ("Rejected".equals(status)) {
                        c.setBackground(new Color(248, 215, 218));
                    } else {
                        c.setBackground(Color.WHITE);
                    }
                }
                return c;
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        
        // Stats Panel
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        statsPanel.setOpaque(false);
        statsPanel.setPreferredSize(new Dimension(0, 80));
        
        int pending = 0, fulfilled = 0, rejected = 0;
        for (BloodRequest req : requests) {
            switch (req.getStatus()) {
                case "Pending": pending++; break;
                case "Fulfilled": fulfilled++; break;
                case "Rejected": rejected++; break;
            }
        }
        
        statsPanel.add(createMiniStatCard("Pending", String.valueOf(pending), new Color(255, 193, 7)));
        statsPanel.add(createMiniStatCard("Fulfilled", String.valueOf(fulfilled), new Color(40, 167, 69)));
        statsPanel.add(createMiniStatCard("Rejected", String.valueOf(rejected), new Color(220, 53, 69)));
        
        contentPanel.add(statsPanel, BorderLayout.NORTH);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    private void openSystemLogs() {
        JDialog dialog = new JDialog(this, "System Logs", true);
        dialog.setSize(1200, 700);
        dialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(245, 247, 250));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY_COLOR),
            BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));
        
        JLabel titleLabel = new JLabel("📊 System Activity Logs");
        titleLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 26));
        titleLabel.setForeground(PRIMARY_COLOR);
        
        JButton refreshBtn = createStyledActionButton("🔄 Refresh", new Color(52, 152, 219));
        refreshBtn.addActionListener(e -> {
            dialog.dispose();
            openSystemLogs();
        });
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(refreshBtn, BorderLayout.EAST);
        
        // Content
        JPanel contentPanel = new JPanel(new BorderLayout(15, 15));
        contentPanel.setBackground(BACKGROUND_COLOR);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        String[] columns = {"Log ID", "Action Type", "Description", "User", "Timestamp"};
        java.util.List<String[]> logList = new java.util.ArrayList<>();
        
        try {
            ResultSet rs = adminService.getSystemLogs();
            while (rs != null && rs.next()) {
                logList.add(new String[]{
                    String.valueOf(rs.getInt("LogID")),
                    rs.getString("ActionType"),
                    rs.getString("Description"),
                    rs.getString("FullName") != null ? rs.getString("FullName") : "System",
                    rs.getTimestamp("ActionTime").toString()
                });
            }
        } catch (SQLException e) {
            showError(dialog, "Failed to load logs: " + e.getMessage());
        }
        
        Object[][] data = logList.toArray(new Object[0][]);
        
        JTable table = new JTable(data, columns);
        table.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        table.setRowHeight(40);
        table.setShowGrid(true);
        table.setGridColor(new Color(230, 230, 230));
        styleTableHeader(table);
        table.setSelectionBackground(new Color(220, 53, 69, 30));
        table.setSelectionForeground(Color.BLACK);

        // Center alignment
        javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        // Adjust column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(60);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(300);
        table.getColumnModel().getColumn(3).setPreferredWidth(150);
        table.getColumnModel().getColumn(4).setPreferredWidth(180);
        
        // Alternate row colors
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 249, 250));
                }
                return c;
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        
        JLabel infoLabel = new JLabel("Showing last " + logList.size() + " activities");
        infoLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        infoLabel.setForeground(TEXT_SECONDARY);
        
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.add(infoLabel, BorderLayout.SOUTH);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    private void openFailedBloodView() {
        JDialog dialog = new JDialog(this, "Failed & Expired Blood", true);
        dialog.setSize(1000, 600);
        dialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(245, 247, 250));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY_COLOR),
            BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));
        
        JLabel titleLabel = new JLabel("🚫 Failed & Expired Blood Records");
        titleLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 26));
        titleLabel.setForeground(PRIMARY_COLOR);
        
        JButton refreshBtn = createStyledActionButton("🔄 Refresh", new Color(52, 152, 219));
        refreshBtn.addActionListener(e -> {
            dialog.dispose();
            openFailedBloodView();
        });
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(refreshBtn, BorderLayout.EAST);
        
        // Content
        JPanel contentPanel = new JPanel(new BorderLayout(15, 15));
        contentPanel.setBackground(BACKGROUND_COLOR);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        String[] columns = {"Failed ID", "Donation ID", "Blood Group", "Reason", "Date"};
        java.util.List<String[]> failedList = new java.util.ArrayList<>();
        
        FailedBloodDAO failedDAO = new FailedBloodDAO();
        java.util.List<FailedBlood> list = failedDAO.getAllFailedBlood();
        
        for (FailedBlood fb : list) {
            failedList.add(new String[]{
                String.valueOf(fb.getFailedID()),
                String.valueOf(fb.getDonationID()),
                fb.getBloodGroup(),
                fb.getReason(),
                fb.getFailedDate().toString()
            });
        }
        
        Object[][] data = failedList.toArray(new Object[0][]);
        
        JTable table = new JTable(data, columns);
        table.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        table.setRowHeight(40);
        table.setShowGrid(true);
        table.setGridColor(new Color(230, 230, 230));
        styleTableHeader(table);
        table.setSelectionBackground(new Color(220, 53, 69, 30));
        table.setSelectionForeground(Color.BLACK);

        // Center alignment
        javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        // Color code rows based on reason
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                if (!isSelected) {
                    String reason = (String) table.getValueAt(row, 3);
                    if ("Test Failed".equals(reason)) {
                        c.setBackground(new Color(255, 235, 238)); // Light Red
                    } else if ("Expired".equals(reason)) {
                        c.setBackground(new Color(255, 243, 205)); // Light Yellow
                    } else {
                        c.setBackground(Color.WHITE);
                    }
                }
                return c;
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    private void generateReport() {
        JDialog dialog = new JDialog(this, "System Report", true);
        dialog.setSize(1100, 750);
        dialog.setLocationRelativeTo(this);
        
        JPanel outerPanel = new JPanel(new BorderLayout());
        outerPanel.setBackground(BACKGROUND_COLOR);
        
        // Header Section with Gradient Effect
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(new Color(245, 247, 250));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(220, 53, 69)),
            BorderFactory.createEmptyBorder(25, 30, 25, 30)
        ));
        
        JLabel titleLabel = new JLabel("📊 Blood Bank System Report");
        titleLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 32));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JPanel metaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        metaPanel.setOpaque(false);
        
        JLabel dateLabel = new JLabel("Generated: " + new java.text.SimpleDateFormat("MMM dd, yyyy 'at' HH:mm:ss").format(new java.util.Date()));
        dateLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        dateLabel.setForeground(TEXT_SECONDARY);
        
        JLabel userLabel = new JLabel("By: " + currentUser.getFullName() + " (" + currentUser.getRole() + ")");
        userLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        userLabel.setForeground(TEXT_SECONDARY);
        
        metaPanel.add(dateLabel);
        metaPanel.add(new JLabel("•"));
        metaPanel.add(userLabel);
        
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(8));
        headerPanel.add(metaPanel);
        
        // Main Content Area
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(BACKGROUND_COLOR);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Section 1: Blood Inventory
        contentPanel.add(createReportSection("🩸 Blood Inventory Overview", createInventoryReportContent()));
        contentPanel.add(Box.createVerticalStrut(20));
        
        // Section 2: Statistics Grid
        JPanel statsGrid = new JPanel(new GridLayout(1, 3, 15, 0));
        statsGrid.setOpaque(false);
        statsGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        
        statsGrid.add(createStatCard("Blood Requests", createRequestsStatsContent(), new Color(52, 152, 219)));
        statsGrid.add(createStatCard("Hospitals", createHospitalsStatsContent(), new Color(46, 204, 113)));
        statsGrid.add(createStatCard("Donors", createDonorsStatsContent(), new Color(155, 89, 182)));
        
        contentPanel.add(statsGrid);
        contentPanel.add(Box.createVerticalStrut(20));
        
        // Section 3: Detailed Tables
        contentPanel.add(createReportSection("📋 Detailed Statistics", createDetailedStatsContent()));
        
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        // Action Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 20));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        buttonPanel.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, new Color(230, 230, 230)));
        
        JButton exportButton = createStyledReportButton("💾 Export PDF", new Color(52, 152, 219));
        exportButton.addActionListener(e -> exportReportToPDF(dialog, contentPanel));
        
        JButton printButton = createStyledReportButton("🖨 Print", new Color(46, 204, 113));
        printButton.addActionListener(e -> printReport(dialog, contentPanel));
        
        JButton closeButton = createStyledReportButton("✕ Close", new Color(149, 165, 166));
        closeButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(exportButton);
        buttonPanel.add(printButton);
        buttonPanel.add(closeButton);
        
        outerPanel.add(headerPanel, BorderLayout.NORTH);
        outerPanel.add(scrollPane, BorderLayout.CENTER);
        outerPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(outerPanel);
        dialog.setVisible(true);
    }
    
    private JPanel createReportSection(String title, JPanel content) {
        JPanel section = new JPanel(new BorderLayout(0, 15));
        section.setBackground(CARD_COLOR);
        section.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
            BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 500));
        
        JLabel sectionTitle = new JLabel(title);
        sectionTitle.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 20));
        sectionTitle.setForeground(TEXT_PRIMARY);
        
        section.add(sectionTitle, BorderLayout.NORTH);
        section.add(content, BorderLayout.CENTER);
        
        return section;
    }
    
    private JPanel createStatCard(String title, String content, Color accentColor) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        JPanel headerBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        headerBar.setOpaque(false);
        headerBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 16));
        titleLabel.setForeground(accentColor);
        
        headerBar.add(titleLabel);
        
        JTextArea contentArea = new JTextArea(content);
        contentArea.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        contentArea.setForeground(TEXT_PRIMARY);
        contentArea.setBackground(CARD_COLOR);
        contentArea.setEditable(false);
        contentArea.setLineWrap(false);
        contentArea.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        card.add(headerBar);
        card.add(Box.createVerticalStrut(5));
        card.add(contentArea);
        
        return card;
    }
    
    private JButton createStyledReportButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(140, 38));
        
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.darker());
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
            }
        });
        
        return button;
    }
    
    private JPanel createInventoryReportContent() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        
        var inventory = inventoryService.getInventorySummary();
        String[] columns = {"Blood Group", "Available Quantity", "Status", "Indicator"};
        String[][] data = new String[8][4];
        String[] bloodGroups = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        
        double totalUnits = 0;
        for (int i = 0; i < bloodGroups.length; i++) {
            int quantityML = inventory.getOrDefault(bloodGroups[i], 0);
            double units = quantityML / (double) ML_PER_UNIT;
            totalUnits += units;
            
            data[i][0] = bloodGroups[i];
            data[i][1] = String.format("%.1f units (%d ml)", units, quantityML);
            
            // Updated thresholds based on units
            if (quantityML < CRITICAL_UNITS_THRESHOLD * ML_PER_UNIT) { // < 3 units
                data[i][2] = "Critical";
                data[i][3] = "🔴";
            } else if (quantityML < 10 * ML_PER_UNIT) { // < 10 units
                data[i][2] = "Low";
                data[i][3] = "🟠";
            } else if (quantityML < 20 * ML_PER_UNIT) { // < 20 units
                data[i][2] = "Moderate";
                data[i][3] = "🟡";
            } else {
                data[i][2] = "Good";
                data[i][3] = "🟢";
            }
        }
        
        JTable table = new JTable(data, columns);
        table.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        table.setRowHeight(40);
        table.setShowGrid(true);
        table.setGridColor(new Color(230, 230, 230));
        styleTableHeader(table);
        table.setSelectionBackground(new Color(220, 53, 69, 30));
        table.setSelectionForeground(Color.BLACK);

        // Center alignment
        javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        
        JLabel summaryLabel = new JLabel(String.format("Total Blood Units: %.1f", totalUnits));
        summaryLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 15));
        summaryLabel.setForeground(PRIMARY_COLOR);
        summaryLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        panel.add(tableScroll, BorderLayout.CENTER);
        panel.add(summaryLabel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private String createRequestsStatsContent() {
        StringBuilder sb = new StringBuilder();
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT Status, COUNT(*) as Count FROM BloodRequests GROUP BY Status";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            int total = 0;
            while (rs.next()) {
                String status = rs.getString("Status");
                int count = rs.getInt("Count");
                total += count;
                sb.append(String.format("%-12s : %d\n", status, count));
            }
            sb.append("─────────────────\n");
            sb.append(String.format("%-12s : %d", "Total", total));
        } catch (SQLException e) {
            sb.append("Error loading data");
        }
        return sb.toString();
    }
    
    private String createHospitalsStatsContent() {
        StringBuilder sb = new StringBuilder();
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT Status, COUNT(*) as Count FROM Hospitals GROUP BY Status";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            int total = 0;
            while (rs.next()) {
                String status = rs.getString("Status");
                int count = rs.getInt("Count");
                total += count;
                sb.append(String.format("%-12s : %d\n", status, count));
            }
            sb.append("─────────────────\n");
            sb.append(String.format("%-12s : %d", "Total", total));
        } catch (SQLException e) {
            sb.append("Error loading data");
        }
        return sb.toString();
    }
    
    private String createDonorsStatsContent() {
        StringBuilder sb = new StringBuilder();
        try (Connection conn = DBConnection.getConnection()) {
            Statement stmt1 = conn.createStatement();
            ResultSet rs1 = stmt1.executeQuery("SELECT COUNT(*) as Total FROM Donors");
            int donors = rs1.next() ? rs1.getInt("Total") : 0;
            
            Statement stmt2 = conn.createStatement();
            ResultSet rs2 = stmt2.executeQuery("SELECT COUNT(*) as Total FROM Donations");
            int donations = rs2.next() ? rs2.getInt("Total") : 0;
            
            sb.append(String.format("%-12s : %d\n", "Donors", donors));
            sb.append(String.format("%-12s : %d\n", "Donations", donations));
            sb.append("─────────────────\n");
            sb.append(String.format("%-12s : %.1f", "Avg/Donor", donors > 0 ? (double)donations/donors : 0.0));
        } catch (SQLException e) {
            sb.append("Error loading data");
        }
        return sb.toString();
    }
    
    private JPanel createDetailedStatsContent() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT BloodGroup, COUNT(*) as Count FROM Donors GROUP BY BloodGroup ORDER BY Count DESC";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            java.util.List<String[]> rows = new java.util.ArrayList<>();
            while (rs.next()) {
                rows.add(new String[]{rs.getString("BloodGroup"), String.valueOf(rs.getInt("Count"))});
            }
            
            String[] columns = {"Blood Group", "Number of Donors"};
            String[][] data = rows.toArray(new String[0][]);
            
            JTable table = new JTable(data, columns);
            table.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
            table.setRowHeight(35);
            table.setShowGrid(true);
            table.setGridColor(new Color(230, 230, 230));
            styleTableHeader(table);
            table.setSelectionBackground(new Color(220, 53, 69, 30));
            table.setSelectionForeground(Color.BLACK);

            // Center alignment
            javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer();
            centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
            for (int i = 0; i < table.getColumnCount(); i++) {
                table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
            
            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
            scrollPane.setPreferredSize(new Dimension(500, 200));
            
            panel.add(scrollPane, BorderLayout.CENTER);
        } catch (SQLException e) {
            JLabel errorLabel = new JLabel("Error loading detailed statistics");
            errorLabel.setForeground(Color.RED);
            panel.add(errorLabel, BorderLayout.CENTER);
        }
        
        return panel;
    }
    
    private void exportReportToPDF(JDialog dialog, JPanel content) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new java.io.File("BloodBankReport_" + 
            new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date()) + ".pdf"));
        
        if (fileChooser.showSaveDialog(dialog) == JFileChooser.APPROVE_OPTION) {
            try {
                java.io.File file = fileChooser.getSelectedFile();
                if (!file.getName().toLowerCase().endsWith(".pdf")) {
                    file = new java.io.File(file.getAbsolutePath() + ".pdf");
                }
                
                // Generate professional PDF using iText
                generateProfessionalPDF(file);
                showSuccess(dialog, "PDF report exported successfully!\n" + file.getAbsolutePath());
                
            } catch (Exception ex) {
                showError(dialog, "Failed to export: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }
    
    private void generateProfessionalPDF(java.io.File file) throws Exception {
        com.itextpdf.text.Document document = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4);
        com.itextpdf.text.pdf.PdfWriter.getInstance(document, new java.io.FileOutputStream(file));
        document.open();
        
        // Define fonts
        com.itextpdf.text.Font titleFont = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 20, com.itextpdf.text.BaseColor.RED);
        com.itextpdf.text.Font headerFont = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 14, com.itextpdf.text.BaseColor.DARK_GRAY);
        com.itextpdf.text.Font subHeaderFont = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 12, com.itextpdf.text.BaseColor.BLACK);
        com.itextpdf.text.Font normalFont = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA, 10, com.itextpdf.text.BaseColor.BLACK);
        com.itextpdf.text.Font smallFont = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA, 8, com.itextpdf.text.BaseColor.GRAY);
        
        // Title
        com.itextpdf.text.Paragraph title = new com.itextpdf.text.Paragraph("BLOOD BANK MANAGEMENT SYSTEM", titleFont);
        title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        title.setSpacingAfter(5);
        document.add(title);
        
        com.itextpdf.text.Paragraph subtitle = new com.itextpdf.text.Paragraph("Comprehensive Report", headerFont);
        subtitle.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(20);
        document.add(subtitle);
        
        // Report Info
        com.itextpdf.text.Paragraph info = new com.itextpdf.text.Paragraph();
        info.add(new com.itextpdf.text.Phrase("Generated: " + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()) + "\n", smallFont));
        info.add(new com.itextpdf.text.Phrase("By: " + currentUser.getFullName() + " (" + currentUser.getRole() + ")\n\n", smallFont));
        document.add(info);
        
        // Blood Inventory Section
        com.itextpdf.text.Paragraph invTitle = new com.itextpdf.text.Paragraph("BLOOD INVENTORY", subHeaderFont);
        invTitle.setSpacingBefore(10);
        invTitle.setSpacingAfter(10);
        document.add(invTitle);
        
        com.itextpdf.text.pdf.PdfPTable invTable = new com.itextpdf.text.pdf.PdfPTable(3);
        invTable.setWidthPercentage(100);
        invTable.setWidths(new int[]{2, 2, 2});
        
        // Header row
        com.itextpdf.text.pdf.PdfPCell headerCell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Blood Group", com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 10, com.itextpdf.text.BaseColor.WHITE)));
        headerCell.setBackgroundColor(new com.itextpdf.text.BaseColor(220, 53, 69));
        headerCell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        headerCell.setPadding(8);
        invTable.addCell(headerCell);
        
        headerCell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Quantity (units)", com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 10, com.itextpdf.text.BaseColor.WHITE)));
        headerCell.setBackgroundColor(new com.itextpdf.text.BaseColor(220, 53, 69));
        headerCell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        headerCell.setPadding(8);
        invTable.addCell(headerCell);
        
        headerCell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Status", com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 10, com.itextpdf.text.BaseColor.WHITE)));
        headerCell.setBackgroundColor(new com.itextpdf.text.BaseColor(220, 53, 69));
        headerCell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        headerCell.setPadding(8);
        invTable.addCell(headerCell);
        
        // Data rows
        var inventory = inventoryService.getInventorySummary();
        String[] bloodGroups = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        int totalUnits = 0;
        
        for (String bg : bloodGroups) {
            int qty = inventory.getOrDefault(bg, 0);
            totalUnits += qty;
            String status = qty < 5 ? "CRITICAL" : qty < 10 ? "LOW" : "OK";
            com.itextpdf.text.BaseColor statusColor = qty < 5 ? com.itextpdf.text.BaseColor.RED : qty < 10 ? com.itextpdf.text.BaseColor.ORANGE : com.itextpdf.text.BaseColor.GREEN;
            
            com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(bg, normalFont));
            cell.setPadding(5);
            cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            invTable.addCell(cell);
            
            cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(String.valueOf(qty), normalFont));
            cell.setPadding(5);
            cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            invTable.addCell(cell);
            
            cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(status, com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 9, statusColor)));
            cell.setPadding(5);
            cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            invTable.addCell(cell);
        }
        
        // Total row
        com.itextpdf.text.pdf.PdfPCell totalCell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("TOTAL", com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 10)));
        totalCell.setPadding(5);
        totalCell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
        totalCell.setBackgroundColor(com.itextpdf.text.BaseColor.LIGHT_GRAY);
        invTable.addCell(totalCell);
        
        totalCell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(String.valueOf(totalUnits), com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 10)));
        totalCell.setPadding(5);
        totalCell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        totalCell.setBackgroundColor(com.itextpdf.text.BaseColor.LIGHT_GRAY);
        invTable.addCell(totalCell);
        
        totalCell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("", normalFont));
        totalCell.setBackgroundColor(com.itextpdf.text.BaseColor.LIGHT_GRAY);
        invTable.addCell(totalCell);
        
        document.add(invTable);
        
        // Hospitals Section
        com.itextpdf.text.Paragraph hospTitle = new com.itextpdf.text.Paragraph("REGISTERED HOSPITALS", subHeaderFont);
        hospTitle.setSpacingBefore(20);
        hospTitle.setSpacingAfter(10);
        document.add(hospTitle);
        
        List<Hospital> hospitals = hospitalService.getAllHospitals();
        com.itextpdf.text.pdf.PdfPTable hospTable = new com.itextpdf.text.pdf.PdfPTable(4);
        hospTable.setWidthPercentage(100);
        hospTable.setWidths(new int[]{1, 3, 3, 2});
        
        String[] hospHeaders = {"ID", "Name", "Address", "Status"};
        for (String header : hospHeaders) {
            headerCell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(header, com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 9, com.itextpdf.text.BaseColor.WHITE)));
            headerCell.setBackgroundColor(new com.itextpdf.text.BaseColor(52, 152, 219));
            headerCell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            headerCell.setPadding(6);
            hospTable.addCell(headerCell);
        }
        
        int approvedCount = 0;
        for (Hospital h : hospitals) {
            if ("Approved".equals(h.getStatus())) approvedCount++;
            
            hospTable.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(String.valueOf(h.getHospitalID()), normalFont)));
            hospTable.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(h.getHospitalName(), normalFont)));
            hospTable.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(h.getAddress(), normalFont)));
            
            com.itextpdf.text.pdf.PdfPCell statusCell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(h.getStatus(), 
                com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 9, 
                "Approved".equals(h.getStatus()) ? com.itextpdf.text.BaseColor.GREEN : com.itextpdf.text.BaseColor.ORANGE)));
            statusCell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            hospTable.addCell(statusCell);
        }
        
        document.add(hospTable);
        
        com.itextpdf.text.Paragraph hospStats = new com.itextpdf.text.Paragraph(String.format("Total: %d | Approved: %d | Pending: %d", 
            hospitals.size(), approvedCount, hospitals.size() - approvedCount), smallFont);
        hospStats.setSpacingBefore(5);
        document.add(hospStats);
        
        // Donors Section
        com.itextpdf.text.Paragraph donorTitle = new com.itextpdf.text.Paragraph("DONOR STATISTICS", subHeaderFont);
        donorTitle.setSpacingBefore(20);
        donorTitle.setSpacingAfter(10);
        document.add(donorTitle);
        
        DonorDAO donorDAO = new DonorDAO();
        List<Donor> donors = donorDAO.getAllDonors();
        
        com.itextpdf.text.pdf.PdfPTable donorTable = new com.itextpdf.text.pdf.PdfPTable(2);
        donorTable.setWidthPercentage(60);
        donorTable.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_LEFT);
        
        for (String bg : bloodGroups) {
            long count = donors.stream().filter(d -> bg.equals(d.getBloodGroup())).count();
            donorTable.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(bg + " Donors", normalFont)));
            donorTable.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(String.valueOf(count), normalFont)));
        }
        
        com.itextpdf.text.pdf.PdfPCell totalDonorCell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Total Donors", com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 10)));
        totalDonorCell.setBackgroundColor(com.itextpdf.text.BaseColor.LIGHT_GRAY);
        donorTable.addCell(totalDonorCell);
        
        totalDonorCell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase(String.valueOf(donors.size()), com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 10)));
        totalDonorCell.setBackgroundColor(com.itextpdf.text.BaseColor.LIGHT_GRAY);
        donorTable.addCell(totalDonorCell);
        
        document.add(donorTable);
        
        // Requests Section
        com.itextpdf.text.Paragraph reqTitle = new com.itextpdf.text.Paragraph("BLOOD REQUESTS", subHeaderFont);
        reqTitle.setSpacingBefore(20);
        reqTitle.setSpacingAfter(10);
        document.add(reqTitle);
        
        BloodRequestService requestService = new BloodRequestService();
        List<BloodRequest> allRequests = requestService.getAllRequests();
        List<BloodRequest> pending = requestService.getPendingRequests();
        long fulfilled = allRequests.stream().filter(r -> "Fulfilled".equals(r.getStatus())).count();
        long rejected = allRequests.stream().filter(r -> "Rejected".equals(r.getStatus())).count();
        
        com.itextpdf.text.Paragraph reqStats = new com.itextpdf.text.Paragraph();
        reqStats.add(new com.itextpdf.text.Phrase("Total Requests: " + allRequests.size() + "\n", normalFont));
        reqStats.add(new com.itextpdf.text.Phrase("Pending: " + pending.size() + " | ", com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA, 10, com.itextpdf.text.BaseColor.ORANGE)));
        reqStats.add(new com.itextpdf.text.Phrase("Fulfilled: " + fulfilled + " | ", com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA, 10, com.itextpdf.text.BaseColor.GREEN)));
        reqStats.add(new com.itextpdf.text.Phrase("Rejected: " + rejected, com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA, 10, com.itextpdf.text.BaseColor.RED)));
        document.add(reqStats);
        
        // Footer
        com.itextpdf.text.Paragraph footer = new com.itextpdf.text.Paragraph("\n\n" + new String(new char[80]).replace("\0", "_"), smallFont);
        footer.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        document.add(footer);
        
        com.itextpdf.text.Paragraph endNote = new com.itextpdf.text.Paragraph("END OF REPORT", com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 12, com.itextpdf.text.BaseColor.GRAY));
        endNote.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        endNote.setSpacingBefore(10);
        document.add(endNote);
        
        document.close();
    }
    
    private void printReport(JDialog dialog, JPanel content) {
        try {
            java.awt.print.PrinterJob job = java.awt.print.PrinterJob.getPrinterJob();
            job.setPrintable((graphics, pageFormat, pageIndex) -> {
                if (pageIndex > 0) return java.awt.print.Printable.NO_SUCH_PAGE;
                
                graphics.translate((int)pageFormat.getImageableX(), (int)pageFormat.getImageableY());
                content.print(graphics);
                return java.awt.print.Printable.PAGE_EXISTS;
            });
            
            if (job.printDialog()) {
                job.print();
                showSuccess(dialog, "Report sent to printer!");
            }
        } catch (Exception ex) {
            showError(dialog, "Failed to print: " + ex.getMessage());
        }
    }
    
    @SuppressWarnings("unused")
    private String generateTextReport() {
        StringBuilder report = new StringBuilder();
        report.append("═══════════════════════════════════════════════════════════════\n");
        report.append("          BLOOD BANK MANAGEMENT SYSTEM - COMPREHENSIVE REPORT\n");
        report.append("═══════════════════════════════════════════════════════════════\n\n");
        report.append("Generated: ").append(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date())).append("\n");
        report.append("By: ").append(currentUser.getFullName()).append(" (").append(currentUser.getRole()).append(")\n\n");
        
        // Blood Inventory
        var inventory = inventoryService.getInventorySummary();
        report.append("═══════════════════════════════════════════════════════════════\n");
        report.append("                        BLOOD INVENTORY\n");
        report.append("═══════════════════════════════════════════════════════════════\n");
        String[] bloodGroups = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        int totalUnits = 0;
        for (String bg : bloodGroups) {
            int qty = inventory.getOrDefault(bg, 0);
            totalUnits += qty;
            String status = qty < 5 ? "[CRITICAL]" : qty < 10 ? "[LOW]" : "[OK]";
            report.append(String.format("  %-6s : %4d units  %s\n", bg, qty, status));
        }
        report.append("  ──────────────────────────────────\n");
        report.append(String.format("  TOTAL  : %4d units\n\n", totalUnits));
        
        // Hospitals
        report.append("═══════════════════════════════════════════════════════════════\n");
        report.append("                     REGISTERED HOSPITALS\n");
        report.append("═══════════════════════════════════════════════════════════════\n");
        List<Hospital> hospitals = hospitalService.getAllHospitals();
        int approvedCount = 0;
        int pendingCount = 0;
        for (Hospital h : hospitals) {
            String statusText = "Approved".equals(h.getStatus()) ? "✓" : "⏳";
            report.append(String.format("  [%s] %-30s | %-20s | ID: %d\n", 
                statusText, h.getHospitalName(), h.getAddress(), h.getHospitalID()));
            if ("Approved".equals(h.getStatus())) approvedCount++;
            else pendingCount++;
        }
        report.append(String.format("\n  Total: %d  |  Approved: %d  |  Pending: %d\n\n", 
            hospitals.size(), approvedCount, pendingCount));
        
        // Donors
        report.append("═══════════════════════════════════════════════════════════════\n");
        report.append("                      REGISTERED DONORS\n");
        report.append("═══════════════════════════════════════════════════════════════\n");
        DonorDAO donorDAO = new DonorDAO();
        List<Donor> donors = donorDAO.getAllDonors();
        report.append(String.format("  Total Registered Donors: %d\n\n", donors.size()));
        report.append("  Blood Group Distribution:\n");
        java.util.Map<String, Long> bgCount = new java.util.HashMap<>();
        for (String bg : bloodGroups) {
            long count = donors.stream().filter(d -> bg.equals(d.getBloodGroup())).count();
            bgCount.put(bg, count);
            report.append(String.format("    %-6s : %3d donors\n", bg, count));
        }
        report.append("\n");
        
        // Blood Requests
        report.append("═══════════════════════════════════════════════════════════════\n");
        report.append("                       BLOOD REQUESTS\n");
        report.append("═══════════════════════════════════════════════════════════════\n");
        BloodRequestService requestService = new BloodRequestService();
        List<BloodRequest> allRequests = requestService.getAllRequests();
        List<BloodRequest> pending = requestService.getPendingRequests();
        long fulfilled = allRequests.stream().filter(r -> "Fulfilled".equals(r.getStatus())).count();
        long rejected = allRequests.stream().filter(r -> "Rejected".equals(r.getStatus())).count();
        
        report.append(String.format("  Total Requests: %d\n", allRequests.size()));
        report.append(String.format("  Pending: %d  |  Fulfilled: %d  |  Rejected: %d\n\n", 
            pending.size(), fulfilled, rejected));
        
        if (!pending.isEmpty()) {
            report.append("  Recent Pending Requests:\n");
            for (int i = 0; i < Math.min(5, pending.size()); i++) {
                BloodRequest req = pending.get(i);
                report.append(String.format("    [%d] %s - %d units for Patient ID: %d (Requested: %s)\n",
                    req.getRequestID(), req.getRequestedBloodGroup(), req.getQuantity(),
                    req.getPatientID(), req.getRequestDate()));
            }
        }
        report.append("\n");
        
        // Donations
        report.append("═══════════════════════════════════════════════════════════════\n");
        report.append("                      BLOOD DONATIONS\n");
        report.append("═══════════════════════════════════════════════════════════════\n");
        DonationDAO donationDAO = new DonationDAO();
        List<BloodDonation> donations = donationDAO.getAllDonations();
        report.append(String.format("  Total Donations Recorded: %d\n", donations.size()));
        
        // Recent donations
        if (!donations.isEmpty()) {
            report.append("\n  Recent Donations (Last 5):\n");
            for (int i = Math.max(0, donations.size() - 5); i < donations.size(); i++) {
                BloodDonation don = donations.get(i);
                Donor donor = donorDAO.getDonorByID(don.getDonorID());
                String donorName = donor != null ? donor.getFullName() : "Unknown";
                report.append(String.format("    [%d] %s - Donor: %s (ID: %d) on %s\n",
                    don.getDonationID(), donorName, donorName, don.getDonorID(), 
                    don.getDonationDate()));
            }
        }
        
        report.append("\n═══════════════════════════════════════════════════════════════\n");
        report.append("                        END OF REPORT\n");
        report.append("═══════════════════════════════════════════════════════════════\n");
        
        return report.toString();
    }
    
    private void checkAlerts() {
        JDialog dialog = new JDialog(this, "System Alerts", true);
        dialog.setSize(900, 600);
        dialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(245, 247, 250));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(231, 76, 60)),
            BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));
        
        JLabel titleLabel = new JLabel("⚠️ System Alerts & Warnings");
        titleLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 26));
        titleLabel.setForeground(new Color(231, 76, 60));
        
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        // Content
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(BACKGROUND_COLOR);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        var inventory = inventoryService.getInventorySummary();
        String[] bloodGroups = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        
        int criticalCount = 0;
        int lowCount = 0;
        
        for (String bg : bloodGroups) {
            int quantityML = inventory.getOrDefault(bg, 0);
            double units = quantityML / (double) ML_PER_UNIT;
            String displayText = String.format("%.1f units (%d ml)", units, quantityML);
            
            if (quantityML < CRITICAL_UNITS_THRESHOLD * ML_PER_UNIT) { // < 3 units (1350ml)
                contentPanel.add(createAlertCard(
                    "🔴 CRITICAL: " + bg + " Blood Group",
                    "Only " + displayText + " available. Immediate action required!",
                    new Color(220, 53, 69)
                ));
                contentPanel.add(Box.createVerticalStrut(10));
                criticalCount++;
            } else if (quantityML < 10 * ML_PER_UNIT) { // < 10 units (4500ml)
                contentPanel.add(createAlertCard(
                    "🟠 WARNING: " + bg + " Blood Group",
                    "Low stock: " + displayText + ". Consider organizing donation camp.",
                    new Color(255, 193, 7)
                ));
                contentPanel.add(Box.createVerticalStrut(10));
                lowCount++;
            }
        }
        
        if (criticalCount == 0 && lowCount == 0) {
            contentPanel.add(createAlertCard(
                "✅ All Systems Normal",
                "No critical alerts. All blood groups have sufficient stock.",
                new Color(40, 167, 69)
            ));
        }
        
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        // Summary Footer
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 15));
        footerPanel.setBackground(new Color(248, 249, 250));
        footerPanel.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, new Color(220, 220, 220)));
        
        JLabel summaryLabel = new JLabel(String.format("Critical: %d  |  Warnings: %d", criticalCount, lowCount));
        summaryLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 16));
        summaryLabel.setForeground(TEXT_PRIMARY);
        
        footerPanel.add(summaryLabel);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    private JPanel createAlertCard(String title, String message, Color accentColor) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(accentColor, 2),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 16));
        titleLabel.setForeground(accentColor);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel messageLabel = new JLabel(message);
        messageLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        messageLabel.setForeground(TEXT_PRIMARY);
        messageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(messageLabel);
        
        return card;
    }
    
    // ==================== URGENT ALERT POPUP NOTIFICATION ====================
    
    private static final int ML_PER_UNIT = 450;
    private static final int CRITICAL_UNITS_THRESHOLD = 3; // Less than 3 units = critical
    
    private void checkAndShowUrgentAlertPopup() {
        criticalBloodGroups.clear();
        var inventory = inventoryService.getInventorySummary();
        String[] bloodGroups = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        
        // Find critical blood groups (below 3 units = 1350ml)
        int criticalThresholdML = CRITICAL_UNITS_THRESHOLD * ML_PER_UNIT; // 3 * 450 = 1350ml
        for (String bg : bloodGroups) {
            int quantityML = inventory.getOrDefault(bg, 0);
            if (quantityML < criticalThresholdML) {
                criticalBloodGroups.add(bg);
            }
        }
        
        // Update the alert indicator
        updateAlertIndicator();
        
        // Show popup only if not dismissed and there are critical alerts
        if (!alertDismissed && !criticalBloodGroups.isEmpty()) {
            showUrgentAlertPopup();
        }
    }
    
    private void updateAlertIndicator() {
        if (urgentAlertIndicator == null) return;
        
        if (criticalBloodGroups.isEmpty()) {
            urgentAlertIndicator.setVisible(false);
        } else {
            urgentAlertIndicator.setText("🔴 " + criticalBloodGroups.size() + " URGENT");
            urgentAlertIndicator.setVisible(true);
            urgentAlertIndicator.setToolTipText("Critical blood shortage: " + String.join(", ", criticalBloodGroups));
        }
    }
    
    private void showUrgentAlertPopup() {
        // Create custom dialog
        JDialog alertDialog = new JDialog(this, "🚨 URGENT BLOOD SHORTAGE ALERT", true);
        alertDialog.setSize(550, 400);
        alertDialog.setLocationRelativeTo(this);
        alertDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        
        // Header with red background
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(220, 53, 69));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        
        JLabel headerLabel = new JLabel("🚨 CRITICAL BLOOD SHORTAGE");
        headerLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 24));
        headerLabel.setForeground(Color.WHITE);
        
        JLabel subHeaderLabel = new JLabel("Immediate action required");
        subHeaderLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        subHeaderLabel.setForeground(new Color(255, 255, 255, 200));
        
        JPanel headerTextPanel = new JPanel();
        headerTextPanel.setLayout(new BoxLayout(headerTextPanel, BoxLayout.Y_AXIS));
        headerTextPanel.setOpaque(false);
        headerTextPanel.add(headerLabel);
        headerTextPanel.add(Box.createVerticalStrut(5));
        headerTextPanel.add(subHeaderLabel);
        
        headerPanel.add(headerTextPanel, BorderLayout.CENTER);
        
        // Content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        
        JLabel messageLabel = new JLabel("<html><b>The following blood groups are critically low (&lt;3 units = 1350ml):</b></html>");
        messageLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        messageLabel.setForeground(TEXT_PRIMARY);
        messageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        contentPanel.add(messageLabel);
        contentPanel.add(Box.createVerticalStrut(15));
        
        // Blood groups list
        for (String bg : criticalBloodGroups) {
            int quantityML = inventoryService.getInventorySummary().getOrDefault(bg, 0);
            double units = quantityML / (double) ML_PER_UNIT;
            String unitsDisplay = String.format("%.1f units (%d ml)", units, quantityML);
            
            JPanel bgPanel = new JPanel(new BorderLayout());
            bgPanel.setBackground(new Color(255, 240, 240));
            bgPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 53, 69), 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
            ));
            bgPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
            
            JLabel bgLabel = new JLabel("🔴 " + bg + " Blood Group");
            bgLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
            bgLabel.setForeground(new Color(220, 53, 69));
            
            JLabel qtyLabel = new JLabel(unitsDisplay);
            qtyLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
            qtyLabel.setForeground(TEXT_SECONDARY);
            
            bgPanel.add(bgLabel, BorderLayout.WEST);
            bgPanel.add(qtyLabel, BorderLayout.EAST);
            
            contentPanel.add(bgPanel);
            contentPanel.add(Box.createVerticalStrut(8));
        }
        
        contentPanel.add(Box.createVerticalStrut(10));
        
        JLabel actionLabel = new JLabel("<html><i>Please contact eligible donors or organize an urgent donation camp.</i></html>");
        actionLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
        actionLabel.setForeground(TEXT_SECONDARY);
        actionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(actionLabel);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)));
        
        JButton viewAlertsBtn = new JButton("View Alert Details");
        viewAlertsBtn.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        viewAlertsBtn.setForeground(new Color(220, 53, 69));
        viewAlertsBtn.setBackground(Color.WHITE);
        viewAlertsBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 53, 69), 1),
            BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));
        viewAlertsBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        viewAlertsBtn.addActionListener(e -> {
            alertDialog.dispose();
            alertDismissed = true;
            checkAlerts();
        });
        
        JButton dismissBtn = new JButton("Dismiss");
        dismissBtn.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        dismissBtn.setForeground(Color.WHITE);
        dismissBtn.setBackground(new Color(220, 53, 69));
        dismissBtn.setBorderPainted(false);
        dismissBtn.setFocusPainted(false);
        dismissBtn.setOpaque(true);
        dismissBtn.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        dismissBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        dismissBtn.setHorizontalAlignment(SwingConstants.CENTER);
        dismissBtn.setVerticalAlignment(SwingConstants.CENTER);
        dismissBtn.addActionListener(e -> {
            alertDismissed = true;
            alertDialog.dispose();
        });
        
        buttonPanel.add(viewAlertsBtn);
        buttonPanel.add(dismissBtn);
        
        // Assemble dialog
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        alertDialog.add(mainPanel);
        alertDialog.setVisible(true);
    }
    
    private JPanel createMiniStatCard(String label, String value, Color color) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 2),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        // Ensure minimum size to prevent clipping
        card.setMinimumSize(new Dimension(150, 100));
        card.setPreferredSize(new Dimension(150, 100));
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 32));
        valueLabel.setForeground(color);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel labelText = new JLabel(label);
        labelText.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        labelText.setForeground(TEXT_SECONDARY);
        labelText.setAlignmentX(Component.CENTER_ALIGNMENT);
        labelText.setHorizontalAlignment(SwingConstants.CENTER);
        
        card.add(Box.createVerticalGlue());
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(labelText);
        card.add(Box.createVerticalGlue());
        
        return card;
    }
    
    private JButton createStyledActionButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(140, 40));
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setVerticalAlignment(SwingConstants.CENTER);
        
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.darker());
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
            }
        });
        
        return button;
    }

    private void styleTableHeader(JTable table) {
        javax.swing.table.DefaultTableCellRenderer headerRenderer = new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setBackground(PRIMARY_COLOR);
                c.setForeground(Color.WHITE);
                c.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
                ((javax.swing.JLabel)c).setHorizontalAlignment(SwingConstants.CENTER);
                ((javax.swing.JComponent)c).setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(200, 33, 49)));
                return c;
            }
        };
        
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }
        table.getTableHeader().setPreferredSize(new Dimension(0, 45));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setOpaque(true);
        table.getTableHeader().setBackground(PRIMARY_COLOR);
        table.getTableHeader().setForeground(Color.WHITE);
    }
    
    private void openSettings() {
        JDialog dialog = new JDialog(this, "System Settings", true);
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(245, 247, 250));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY_COLOR),
            BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));
        
        JLabel titleLabel = new JLabel("🛠️ System Settings & Configuration");
        titleLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 26));
        titleLabel.setForeground(PRIMARY_COLOR);
        
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        // Settings Content
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(BACKGROUND_COLOR);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));
        
        // System Info Section
        contentPanel.add(createSettingsSection("📊 System Information", new String[][]{
            {"System Name", "Blood Bank Management System"},
            {"Version", "1.0.0"},
            {"Database", "MySQL - bloodbankdb"},
            {"Current User", currentUser.getFullName() + " (" + currentUser.getRole() + ")"}
        }));
        
        contentPanel.add(Box.createVerticalStrut(20));
        
        // Blood Inventory Thresholds
        contentPanel.add(createSettingsSection("🩸 Inventory Thresholds", new String[][]{
            {"Critical Level", "< 5 units (Red Alert)"},
            {"Low Level", "< 10 units (Warning)"},
            {"Moderate Level", "10-20 units (Caution)"},
            {"Good Level", "> 20 units (Healthy)"}
        }));
        
        contentPanel.add(Box.createVerticalStrut(20));
        
        // System Features
        contentPanel.add(createSettingsSection("✨ Active Features", new String[][]{
            {"User Management", "✓ Enabled"},
            {"Hospital Management", "✓ Enabled"},
            {"Blood Request Processing", "✓ Enabled"},
            {"System Logging", "✓ Enabled"},
            {"Alert System", "✓ Enabled"},
            {"Report Generation", "✓ Enabled"}
        }));
        
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        // Footer
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        footerPanel.setBackground(new Color(248, 249, 250));
        footerPanel.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, new Color(220, 220, 220)));
        
        JLabel infoLabel = new JLabel("Contact Administrator to modify system settings");
        infoLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.ITALIC, 13));
        infoLabel.setForeground(TEXT_SECONDARY);
        
        footerPanel.add(infoLabel);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    private JPanel createSettingsSection(String title, String[][] items) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBackground(CARD_COLOR);
        section.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 500));
        
        JLabel sectionTitle = new JLabel(title);
        sectionTitle.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 18));
        sectionTitle.setForeground(TEXT_PRIMARY);
        sectionTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        section.add(sectionTitle);
        section.add(Box.createVerticalStrut(15));
        
        for (String[] item : items) {
            JPanel itemPanel = new JPanel(new BorderLayout());
            itemPanel.setOpaque(false);
            itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
            
            JLabel keyLabel = new JLabel(item[0]);
            keyLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
            keyLabel.setForeground(TEXT_SECONDARY);
            
            JLabel valueLabel = new JLabel(item[1]);
            valueLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
            valueLabel.setForeground(TEXT_PRIMARY);
            
            itemPanel.add(keyLabel, BorderLayout.WEST);
            itemPanel.add(valueLabel, BorderLayout.EAST);
            
            section.add(itemPanel);
            section.add(Box.createVerticalStrut(8));
        }
        
        return section;
    }
    
    private void openDonorRegistration() {
        JDialog dialog = new JDialog(this, "Register New Donor", true);
        dialog.setSize(600, 700);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        panel.setBackground(CARD_COLOR);
        
        JLabel titleLabel = new JLabel("🩸 Register New Donor");
        titleLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 24));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(30));
        
        // Full Name
        JPanel namePanel = new JPanel(new BorderLayout(10, 0));
        namePanel.setOpaque(false);
        namePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        JLabel nameLabel = new JLabel("Full Name:");
        nameLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        nameLabel.setPreferredSize(new Dimension(120, 35));
        JTextField nameField = new JTextField();
        nameField.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        namePanel.add(nameLabel, BorderLayout.WEST);
        namePanel.add(nameField, BorderLayout.CENTER);
        panel.add(namePanel);
        panel.add(Box.createVerticalStrut(15));
        
        // Gender
        JPanel genderPanel = new JPanel(new BorderLayout(10, 0));
        genderPanel.setOpaque(false);
        genderPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        JLabel genderLabel = new JLabel("Gender:");
        genderLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        genderLabel.setPreferredSize(new Dimension(120, 35));
        JComboBox<String> genderCombo = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        genderCombo.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        genderPanel.add(genderLabel, BorderLayout.WEST);
        genderPanel.add(genderCombo, BorderLayout.CENTER);
        panel.add(genderPanel);
        panel.add(Box.createVerticalStrut(15));
        
        // Age
        JPanel agePanel = new JPanel(new BorderLayout(10, 0));
        agePanel.setOpaque(false);
        agePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        JLabel ageLabel = new JLabel("Age:");
        ageLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        ageLabel.setPreferredSize(new Dimension(120, 35));
        JSpinner ageSpinner = new JSpinner(new SpinnerNumberModel(25, 18, 65, 1));
        ageSpinner.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        agePanel.add(ageLabel, BorderLayout.WEST);
        agePanel.add(ageSpinner, BorderLayout.CENTER);
        panel.add(agePanel);
        panel.add(Box.createVerticalStrut(15));
        
        // Blood Group
        JPanel bloodPanel = new JPanel(new BorderLayout(10, 0));
        bloodPanel.setOpaque(false);
        bloodPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        JLabel bloodLabel = new JLabel("Blood Group:");
        bloodLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        bloodLabel.setPreferredSize(new Dimension(120, 35));
        JComboBox<String> bloodCombo = new JComboBox<>(new String[]{"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"});
        bloodCombo.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        bloodPanel.add(bloodLabel, BorderLayout.WEST);
        bloodPanel.add(bloodCombo, BorderLayout.CENTER);
        panel.add(bloodPanel);
        panel.add(Box.createVerticalStrut(15));
        
        // Phone
        JPanel phonePanel = new JPanel(new BorderLayout(10, 0));
        phonePanel.setOpaque(false);
        phonePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        JLabel phoneLabel = new JLabel("Phone:");
        phoneLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        phoneLabel.setPreferredSize(new Dimension(120, 35));
        JTextField phoneField = new JTextField();
        phoneField.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        phonePanel.add(phoneLabel, BorderLayout.WEST);
        phonePanel.add(phoneField, BorderLayout.CENTER);
        panel.add(phonePanel);
        panel.add(Box.createVerticalStrut(15));
        
        // Last Donation Date
        JPanel datePanel = new JPanel(new BorderLayout(10, 0));
        datePanel.setOpaque(false);
        datePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        JLabel dateLabel = new JLabel("Last Donation:");
        dateLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        dateLabel.setPreferredSize(new Dimension(120, 35));
        JTextField dateField = new JTextField("YYYY-MM-DD (Optional)");
        dateField.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        dateField.setForeground(Color.GRAY);
        dateField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (dateField.getText().equals("YYYY-MM-DD (Optional)")) {
                    dateField.setText("");
                    dateField.setForeground(Color.BLACK);
                }
            }
            public void focusLost(FocusEvent e) {
                if (dateField.getText().isEmpty()) {
                    dateField.setText("YYYY-MM-DD (Optional)");
                    dateField.setForeground(Color.GRAY);
                }
            }
        });
        datePanel.add(dateLabel, BorderLayout.WEST);
        datePanel.add(dateField, BorderLayout.CENTER);
        panel.add(datePanel);
        panel.add(Box.createVerticalStrut(30));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setOpaque(false);
        
        JButton registerBtn = createStyledActionButton("✓ Register Donor", new Color(46, 204, 113));
        registerBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String gender = (String) genderCombo.getSelectedItem();
            int age = (Integer) ageSpinner.getValue();
            String bloodGroup = (String) bloodCombo.getSelectedItem();
            String phone = phoneField.getText().trim();
            String lastDonation = dateField.getText().trim();
            
            if (name.isEmpty() || phone.isEmpty()) {
                showError(dialog, "Please fill all required fields!");
                return;
            }
            
            if (lastDonation.equals("YYYY-MM-DD (Optional)")) {
                lastDonation = null;
            }
            
            DonorDAO donorDAO = new DonorDAO();
            java.sql.Date sqlDate = null;
            if (lastDonation != null && !lastDonation.isEmpty()) {
                try {
                    sqlDate = java.sql.Date.valueOf(lastDonation);
                } catch (IllegalArgumentException ex) {
                    showError(dialog, "Invalid date format! Use YYYY-MM-DD");
                    return;
                }
            }
            Donor donor = new Donor(0, name, gender, age, bloodGroup, phone, sqlDate);
            
            if (donorDAO.addDonor(donor)) {
                showSuccess(dialog, "✓ Donor registered successfully!\nName: " + name + "\nBlood Group: " + bloodGroup);
                dialog.dispose();
            } else {
                showError(dialog, "Failed to register donor!");
            }
        });
        
        JButton cancelBtn = createStyledActionButton("✕ Cancel", new Color(149, 165, 166));
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(registerBtn);
        buttonPanel.add(cancelBtn);
        panel.add(buttonPanel);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private void openDonationRecord() {
        JDialog dialog = new JDialog(this, "Record Blood Donation", true);
        dialog.setSize(650, 500);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        panel.setBackground(CARD_COLOR);
        
        JLabel titleLabel = new JLabel("💉 Record Blood Donation");
        titleLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 24));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(30));
        
        // Donor ID
        JPanel donorPanel = new JPanel(new BorderLayout(10, 0));
        donorPanel.setOpaque(false);
        donorPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        JLabel donorLabel = new JLabel("Donor ID:");
        donorLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        donorLabel.setPreferredSize(new Dimension(140, 35));
        JSpinner donorSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10000, 1));
        donorSpinner.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        JButton verifyBtn = createStyledActionButton("🔍 Verify", new Color(52, 152, 219));
        JLabel verifyLabel = new JLabel("");
        verifyLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.ITALIC, 12));
        
        verifyBtn.addActionListener(e -> {
            int donorId = (Integer) donorSpinner.getValue();
            DonorDAO donorDAO = new DonorDAO();
            Donor donor = donorDAO.getDonorByID(donorId);
            if (donor != null) {
                verifyLabel.setText("✓ " + donor.getFullName() + " (" + donor.getBloodGroup() + ")");
                verifyLabel.setForeground(new Color(46, 204, 113));
            } else {
                verifyLabel.setText("✗ Donor not found");
                verifyLabel.setForeground(PRIMARY_COLOR);
            }
        });
        
        JPanel donorInputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        donorInputPanel.setOpaque(false);
        donorInputPanel.add(donorSpinner);
        donorInputPanel.add(verifyBtn);
        
        donorPanel.add(donorLabel, BorderLayout.WEST);
        donorPanel.add(donorInputPanel, BorderLayout.CENTER);
        panel.add(donorPanel);
        panel.add(Box.createVerticalStrut(10));
        
        JPanel verifyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 150, 0));
        verifyPanel.setOpaque(false);
        verifyPanel.add(verifyLabel);
        panel.add(verifyPanel);
        panel.add(Box.createVerticalStrut(15));
        
        // Donation Date
        JPanel datePanel = new JPanel(new BorderLayout(10, 0));
        datePanel.setOpaque(false);
        datePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        JLabel dateLabel = new JLabel("Donation Date:");
        dateLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        dateLabel.setPreferredSize(new Dimension(140, 35));
        
        // Set default to current date
        JTextField dateField = new JTextField(java.time.LocalDate.now().toString());
        dateField.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        dateField.setForeground(Color.BLACK);
        
        datePanel.add(dateLabel, BorderLayout.WEST);
        datePanel.add(dateField, BorderLayout.CENTER);
        panel.add(datePanel);
        panel.add(Box.createVerticalStrut(15));
        
        // Blood Group
        JPanel bloodPanel = new JPanel(new BorderLayout(10, 0));
        bloodPanel.setOpaque(false);
        bloodPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        JLabel bloodLabel = new JLabel("Blood Group:");
        bloodLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        bloodLabel.setPreferredSize(new Dimension(140, 35));
        
        // Changed to JTextField and made read-only
        JTextField bloodField = new JTextField();
        bloodField.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        bloodField.setEditable(false);
        bloodField.setBackground(new Color(240, 240, 240));
        bloodField.setText("Verify Donor First");
        
        bloodPanel.add(bloodLabel, BorderLayout.WEST);
        bloodPanel.add(bloodField, BorderLayout.CENTER);
        panel.add(bloodPanel);
        panel.add(Box.createVerticalStrut(15));
        
        // Quantity
        JPanel quantityPanel = new JPanel(new BorderLayout(10, 0));
        quantityPanel.setOpaque(false);
        quantityPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        JLabel quantityLabel = new JLabel("Quantity (ml):");
        quantityLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        quantityLabel.setPreferredSize(new Dimension(140, 35));
        // Updated limits: Min 450ml, Max 1000ml (1 Litre), Step 50ml
        JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(450, 450, 1000, 50));
        quantitySpinner.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        quantityPanel.add(quantityLabel, BorderLayout.WEST);
        quantityPanel.add(quantitySpinner, BorderLayout.CENTER);
        panel.add(quantityPanel);
        panel.add(Box.createVerticalStrut(30));
        
        // Update Verify Button Logic
        verifyBtn.addActionListener(e -> {
            int donorId = (Integer) donorSpinner.getValue();
            DonorDAO donorDAO = new DonorDAO();
            Donor donor = donorDAO.getDonorByID(donorId);
            if (donor != null) {
                verifyLabel.setText("✓ " + donor.getFullName());
                verifyLabel.setForeground(new Color(46, 204, 113));
                bloodField.setText(donor.getBloodGroup()); // Auto-populate blood group
                bloodField.setBackground(Color.WHITE);
            } else {
                verifyLabel.setText("✗ Donor not found");
                verifyLabel.setForeground(PRIMARY_COLOR);
                bloodField.setText("Verify Donor First");
                bloodField.setBackground(new Color(240, 240, 240));
            }
        });
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setOpaque(false);
        
        JButton recordBtn = createStyledActionButton("✓ Record Donation", new Color(46, 204, 113));
        recordBtn.addActionListener(e -> {
            int donorId = (Integer) donorSpinner.getValue();
            String date = dateField.getText().trim();
            String bloodGroup = bloodField.getText();
            int quantity = (Integer) quantitySpinner.getValue();
            
            if (date.equals("YYYY-MM-DD") || date.isEmpty()) {
                showError(dialog, "Please enter a valid date!");
                return;
            }
            
            if (bloodGroup.equals("Verify Donor First") || bloodGroup.isEmpty()) {
                showError(dialog, "Please verify donor to get blood group!");
                return;
            }
            
            DonorDAO donorDAO = new DonorDAO();
            Donor donor = donorDAO.getDonorByID(donorId);
            if (donor == null) {
                showError(dialog, "Invalid Donor ID! Please verify donor first.");
                return;
            }
            
            java.sql.Date sqlDate;
            try {
                sqlDate = java.sql.Date.valueOf(date);
            } catch (IllegalArgumentException ex) {
                showError(dialog, "Invalid date format! Use YYYY-MM-DD");
                return;
            }
            
            DonationDAO donationDAO = new DonationDAO();
            BloodDonation donation = new BloodDonation(0, donorId, sqlDate, quantity, currentUser.getUserID());
            
            if (donationDAO.addDonation(donation)) {
                // Add blood to inventory logic is handled by StaffService after testing
                showSuccess(dialog, "Donation recorded successfully!\nBlood Test created (Pending).");
                dialog.dispose();
            } else {
                showError(dialog, "Failed to record donation!");
            }
        });
        
        JButton cancelBtn = createStyledActionButton("✕ Cancel", new Color(149, 165, 166));
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(recordBtn);
        buttonPanel.add(cancelBtn);
        panel.add(buttonPanel);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private void openDonorsList() {
        JDialog dialog = new JDialog(this, "All Donors", true);
        dialog.setSize(1200, 700);
        dialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(245, 247, 250));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY_COLOR),
            BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));
        
        JLabel titleLabel = new JLabel("👥 All Registered Donors");
        titleLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 26));
        titleLabel.setForeground(PRIMARY_COLOR);
        
        JButton refreshBtn = createStyledActionButton("🔄 Refresh", new Color(52, 152, 219));
        refreshBtn.addActionListener(e -> {
            dialog.dispose();
            openDonorsList();
        });
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(refreshBtn, BorderLayout.EAST);
        
        // Content
        JPanel contentPanel = new JPanel(new BorderLayout(15, 15));
        contentPanel.setBackground(BACKGROUND_COLOR);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        DonorDAO donorDAO = new DonorDAO();
        List<Donor> donors = donorDAO.getAllDonors();
        
        String[] columns = {"Donor ID", "Full Name", "Age", "Gender", "Blood Group", "Phone", "Last Donation"};
        Object[][] data = new Object[donors.size()][7];
        
        for (int i = 0; i < donors.size(); i++) {
            Donor d = donors.get(i);
            data[i] = new Object[]{
                d.getDonorID(),
                d.getFullName(),
                d.getAge(),
                d.getGender(),
                d.getBloodGroup(),
                d.getPhone(),
                d.getLastDonationDate() != null ? d.getLastDonationDate() : "Never"
            };
        }
        
        JTable table = new JTable(data, columns);
        table.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        table.setRowHeight(45);
        table.setShowGrid(true);
        table.setGridColor(new Color(230, 230, 230));
        
        styleTableHeader(table);
        table.setSelectionBackground(new Color(220, 53, 69, 30));
        table.setSelectionForeground(TEXT_PRIMARY);
        
        // Center align all cells
        javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        // Color code by blood group - override center renderer
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                
                if (!isSelected && column == 4) { // Blood Group column
                    String bloodGroup = (String) table.getValueAt(row, 4);
                    if (bloodGroup.contains("+")) {
                        c.setBackground(new Color(255, 240, 245));
                    } else {
                        c.setBackground(new Color(240, 248, 255));
                    }
                } else if (!isSelected) {
                    c.setBackground(Color.WHITE);
                } else {
                    c.setBackground(new Color(220, 53, 69, 30));
                }
                c.setForeground(TEXT_PRIMARY);
                return c;
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        
        // Stats Panel
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        statsPanel.setOpaque(false);
        statsPanel.setPreferredSize(new Dimension(0, 80));
        
        long recentDonors = donors.stream().filter(d -> d.getLastDonationDate() != null).count();
        
        statsPanel.add(createMiniStatCard("Total Donors", String.valueOf(donors.size()), PRIMARY_COLOR));
        statsPanel.add(createMiniStatCard("Active Donors", String.valueOf(recentDonors), new Color(46, 204, 113)));
        statsPanel.add(createMiniStatCard("New This Month", "N/A", new Color(52, 152, 219)));
        
        contentPanel.add(statsPanel, BorderLayout.NORTH);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    private void openDonorSearch() {
        JDialog dialog = new JDialog(this, "Search Donors by Blood Group", true);
        dialog.setSize(1100, 650);
        dialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(245, 247, 250));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY_COLOR),
            BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));
        
        JLabel titleLabel = new JLabel("🔍 Search Donors by Blood Group");
        titleLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 26));
        titleLabel.setForeground(PRIMARY_COLOR);
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 20));
        searchPanel.setBackground(CARD_COLOR);
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
        JLabel searchLabel = new JLabel("Select Blood Group:");
        searchLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 15));
        
        JComboBox<String> bloodCombo = new JComboBox<>(new String[]{"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"});
        bloodCombo.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 15));
        bloodCombo.setPreferredSize(new Dimension(100, 35));
        
        JButton searchBtn = createStyledActionButton("🔍 Search", new Color(52, 152, 219));
        
        searchPanel.add(searchLabel);
        searchPanel.add(bloodCombo);
        searchPanel.add(searchBtn);
        
        // Results Panel
        JPanel resultsPanel = new JPanel(new BorderLayout(15, 15));
        resultsPanel.setBackground(BACKGROUND_COLOR);
        resultsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        String[] columns = {"Donor ID", "Full Name", "Age", "Gender", "Blood Group", "Phone", "Last Donation"};
        JTable resultsTable = new JTable(new Object[0][7], columns);
        resultsTable.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        resultsTable.setRowHeight(45);
        resultsTable.setShowGrid(true);
        resultsTable.setGridColor(new Color(230, 230, 230));
        
        styleTableHeader(resultsTable);
        resultsTable.setSelectionBackground(new Color(220, 53, 69, 30));
        resultsTable.setSelectionForeground(Color.BLACK);

        // Center alignment
        javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < resultsTable.getColumnCount(); i++) {
            resultsTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        JScrollPane scrollPane = new JScrollPane(resultsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        
        JLabel resultLabel = new JLabel("Select a blood group and click Search");
        resultLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.ITALIC, 14));
        resultLabel.setForeground(TEXT_SECONDARY);
        
        resultsPanel.add(scrollPane, BorderLayout.CENTER);
        resultsPanel.add(resultLabel, BorderLayout.SOUTH);
        
        // Search Action
        searchBtn.addActionListener(e -> {
            String selectedBloodGroup = (String) bloodCombo.getSelectedItem();
            DonorDAO donorDAO = new DonorDAO();
            List<Donor> donors = donorDAO.searchDonorsByBloodGroup(selectedBloodGroup);
            
            Object[][] data = new Object[donors.size()][7];
            for (int i = 0; i < donors.size(); i++) {
                Donor d = donors.get(i);
                data[i] = new Object[]{
                    d.getDonorID(),
                    d.getFullName(),
                    d.getAge(),
                    d.getGender(),
                    d.getBloodGroup(),
                    d.getPhone(),
                    d.getLastDonationDate() != null ? d.getLastDonationDate() : "Never"
                };
            }
            
            resultsTable.setModel(new javax.swing.table.DefaultTableModel(data, columns));
            styleTableHeader(resultsTable);
            
            // Re-apply center alignment
            javax.swing.table.DefaultTableCellRenderer searchCenterRenderer = new javax.swing.table.DefaultTableCellRenderer();
            searchCenterRenderer.setHorizontalAlignment(SwingConstants.CENTER);
            for (int i = 0; i < resultsTable.getColumnCount(); i++) {
                resultsTable.getColumnModel().getColumn(i).setCellRenderer(searchCenterRenderer);
            }
            
            resultLabel.setText("Found " + donors.size() + " donor(s) with blood group " + selectedBloodGroup);
            resultLabel.setForeground(donors.size() > 0 ? new Color(46, 204, 113) : PRIMARY_COLOR);
        });
        
        // Layout
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(headerPanel, BorderLayout.NORTH);
        topPanel.add(searchPanel, BorderLayout.CENTER);
        
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(resultsPanel, BorderLayout.CENTER);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    private void openTestProcessing() {
        JDialog dialog = new JDialog(this, "Blood Test Processing", true);
        dialog.setSize(1100, 700);
        dialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(245, 247, 250));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY_COLOR),
            BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));
        
        JLabel titleLabel = new JLabel("🔬 Blood Test Processing");
        titleLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 26));
        titleLabel.setForeground(PRIMARY_COLOR);
        
        JButton refreshBtn = createStyledActionButton("🔄 Refresh", new Color(52, 152, 219));
        refreshBtn.addActionListener(e -> {
            dialog.dispose();
            openTestProcessing();
        });
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(refreshBtn, BorderLayout.EAST);
        
        // Content
        JPanel contentPanel = new JPanel(new BorderLayout(15, 15));
        contentPanel.setBackground(BACKGROUND_COLOR);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        StaffService staffService = new StaffService();
        String[] columns = {"Test ID", "Donation ID", "Donor ID", "Donor Name", "Status", "Actions"};
        java.util.List<String[]> testList = new java.util.ArrayList<>();
        
        try {
            ResultSet rs = staffService.getPendingTests();
            while (rs != null && rs.next()) {
                testList.add(new String[]{
                    String.valueOf(rs.getInt("TestID")),
                    String.valueOf(rs.getInt("DonationID")),
                    String.valueOf(rs.getInt("DonorID")),
                    rs.getString("FullName"),
                    rs.getString("FinalStatus"),
                    "Process Test"
                });
            }
        } catch (SQLException e) {
            showError(dialog, "Failed to load tests: " + e.getMessage());
        }
        
        Object[][] data = testList.toArray(new Object[0][]);
        
        JTable table = new JTable(data, columns) {
            public boolean isCellEditable(int row, int column) {
                return column == 5;
            }
        };
        
        table.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        table.setRowHeight(45);
        table.setShowGrid(true);
        table.setGridColor(new Color(230, 230, 230));
        
        styleTableHeader(table);
        table.setSelectionBackground(new Color(220, 53, 69, 30));
        table.setSelectionForeground(Color.BLACK);

        // Center alignment
        javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            if (i != 5) { // Skip actions column
                table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }
        
        table.getColumn("Actions").setCellRenderer((tbl, value, isSelected, hasFocus, row, column) -> {
            JButton btn = new JButton("🔬 Process");
            btn.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
            btn.setForeground(Color.WHITE);
            btn.setBackground(new Color(52, 152, 219));
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            return btn;
        });
        
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (col == 5 && row >= 0) {
                    int donationId = Integer.parseInt((String) table.getValueAt(row, 1));
                    String donorName = (String) table.getValueAt(row, 3);
                    
                    showTestProcessingDialog(dialog, donationId, donorName);
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        
        JLabel infoLabel = new JLabel("Pending Tests: " + testList.size());
        infoLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        infoLabel.setForeground(TEXT_SECONDARY);
        
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.add(infoLabel, BorderLayout.SOUTH);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    private void showTestProcessingDialog(JDialog parent, int donationId, String donorName) {
        JDialog dialog = new JDialog(parent, "Process Blood Test", true);
        dialog.setSize(650, 600);
        dialog.setLocationRelativeTo(parent);
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        panel.setBackground(CARD_COLOR);
        
        JLabel titleLabel = new JLabel("🔬 Blood Test Results");
        titleLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 24));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel infoLabel = new JLabel("Donation ID: " + donationId + " | Donor: " + donorName);
        infoLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        infoLabel.setForeground(TEXT_SECONDARY);
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(infoLabel);
        panel.add(Box.createVerticalStrut(30));
        
        // Test Results
        String[] tests = {"Hepatitis A", "Hepatitis B", "Hepatitis C", "HIV", "Syphilis"};
        JComboBox<?>[] combos = new JComboBox[5];
        String[] results = {"Negative", "Positive"};
        
        for (int i = 0; i < tests.length; i++) {
            JPanel testPanel = new JPanel(new BorderLayout(10, 0));
            testPanel.setOpaque(false);
            testPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            
            JLabel testLabel = new JLabel(tests[i] + ":");
            testLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
            testLabel.setPreferredSize(new Dimension(120, 35));
            
            combos[i] = new JComboBox<>(results);
            combos[i].setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
            combos[i].setPreferredSize(new Dimension(150, 35));
            
            testPanel.add(testLabel, BorderLayout.WEST);
            testPanel.add(combos[i], BorderLayout.CENTER);
            
            panel.add(testPanel);
            panel.add(Box.createVerticalStrut(12));
        }
        
        panel.add(Box.createVerticalStrut(20));
        
        // Warning Label
        JLabel warningLabel = new JLabel("<html><center>⚠️ If any test is Positive,<br>the blood will be marked as Failed</center></html>");
        warningLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.ITALIC, 13));
        warningLabel.setForeground(new Color(220, 53, 69));
        warningLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panel.add(warningLabel);
        panel.add(Box.createVerticalStrut(25));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setOpaque(false);
        
        JButton submitBtn = createStyledActionButton("✓ Submit Results", new Color(46, 204, 113));
        submitBtn.addActionListener(e -> {
            String hepatitisA = (String) combos[0].getSelectedItem();
            String hepatitisB = (String) combos[1].getSelectedItem();
            String hepatitisC = (String) combos[2].getSelectedItem();
            String hiv = (String) combos[3].getSelectedItem();
            String syphilis = (String) combos[4].getSelectedItem();
            
            StaffService service = new StaffService();
            String result = service.processBloodTest(donationId, currentUser.getUserID(), 
                    hepatitisA, hepatitisB, hepatitisC, hiv, syphilis);
            
            if (result == null) {
                
                boolean allNegative = "Negative".equals(hepatitisA) && 
                                    "Negative".equals(hepatitisB) && 
                                    "Negative".equals(hepatitisC) && 
                                    "Negative".equals(hiv) && 
                                    "Negative".equals(syphilis);
                
                String message = allNegative ? 
                    "✓ Test results submitted!\nAll tests PASSED - Blood is safe for use." :
                    "⚠ Test results submitted!\nBlood FAILED testing - Will not be used.";
                
                showSuccess(dialog, message);
                dialog.dispose();
                parent.dispose();
                openTestProcessing();
            } else {
                showError(dialog, "Failed to submit test results!\nError: " + result);
            }
        });
        
        JButton cancelBtn = createStyledActionButton("✕ Cancel", new Color(149, 165, 166));
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(submitBtn);
        buttonPanel.add(cancelBtn);
        panel.add(buttonPanel);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private void openPendingTests() {
        JDialog dialog = new JDialog(this, "Pending Blood Tests", true);
        dialog.setSize(1100, 650);
        dialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(245, 247, 250));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY_COLOR),
            BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));
        
        JLabel titleLabel = new JLabel("⏳ Pending Blood Tests");
        titleLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 26));
        titleLabel.setForeground(PRIMARY_COLOR);
        
        JButton refreshBtn = createStyledActionButton("🔄 Refresh", new Color(52, 152, 219));
        refreshBtn.addActionListener(e -> {
            dialog.dispose();
            openPendingTests();
        });
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(refreshBtn, BorderLayout.EAST);
        
        // Content
        JPanel contentPanel = new JPanel(new BorderLayout(15, 15));
        contentPanel.setBackground(BACKGROUND_COLOR);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        StaffService staffService = new StaffService();
        String[] columns = {"Test ID", "Donation ID", "Donor ID", "Donor Name", "Status", "Date Collected"};
        java.util.List<String[]> testList = new java.util.ArrayList<>();
        
        try {
            ResultSet rs = staffService.getPendingTests();
            while (rs != null && rs.next()) {
                testList.add(new String[]{
                    String.valueOf(rs.getInt("TestID")),
                    String.valueOf(rs.getInt("DonationID")),
                    String.valueOf(rs.getInt("DonorID")),
                    rs.getString("FullName"),
                    "⏳ " + rs.getString("FinalStatus"),
                    "Awaiting Test"
                });
            }
        } catch (SQLException e) {
            showError(dialog, "Failed to load pending tests: " + e.getMessage());
        }
        
        Object[][] data = testList.toArray(new Object[0][]);
        
        JTable table = new JTable(data, columns);
        table.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        table.setRowHeight(50);
        table.setShowGrid(true);
        table.setGridColor(new Color(230, 230, 230));
        
        styleTableHeader(table);
        table.setSelectionBackground(new Color(220, 53, 69, 30));
        table.setSelectionForeground(Color.BLACK);
        
        // Color all rows with pending status
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                if (!isSelected) {
                    c.setBackground(new Color(255, 243, 205));
                }
                return c;
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        
        // Stats Panel
        JPanel statsPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        statsPanel.setOpaque(false);
        statsPanel.setPreferredSize(new Dimension(0, 80));
        
        statsPanel.add(createMiniStatCard("Pending Tests", String.valueOf(testList.size()), new Color(255, 193, 7)));
        statsPanel.add(createMiniStatCard("Action Required", "Process Now", new Color(220, 53, 69)));
        
        contentPanel.add(statsPanel, BorderLayout.NORTH);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        
        JLabel infoLabel = new JLabel("💡 Tip: Use 'Process Tests' to submit test results");
        infoLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.ITALIC, 13));
        infoLabel.setForeground(TEXT_SECONDARY);
        
        contentPanel.add(infoLabel, BorderLayout.SOUTH);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    private void openRequestManagement() {
        JDialog dialog = new JDialog(this, "Blood Request Management", true);
        dialog.setSize(1200, 700);
        dialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(245, 247, 250));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY_COLOR),
            BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));
        
        JLabel titleLabel = new JLabel("📋 Blood Request Management");
        titleLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 26));
        titleLabel.setForeground(PRIMARY_COLOR);
        
        JButton createRequestBtn = createStyledActionButton("➕ Create Request", new Color(46, 204, 113));
        createRequestBtn.addActionListener(e -> showCreateRequestDialog(dialog));
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(createRequestBtn, BorderLayout.EAST);
        
        // Tabbed Content
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        tabbedPane.setBackground(BACKGROUND_COLOR);
        
        BloodRequestService requestService = new BloodRequestService();
        
        tabbedPane.addTab("⏳ Pending Requests", createRequestTablePanel(requestService.getPendingRequests(), true, dialog));
        tabbedPane.addTab("📊 All Requests", createRequestTablePanel(requestService.getAllRequests(), false, dialog));
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    private JPanel createRequestTablePanel(List<BloodRequest> requests, boolean showActions, JDialog parentDialog) {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        String[] columns = showActions ? 
            new String[]{"Request ID", "Patient ID", "Blood Group", "Quantity", "Date", "Status", "Actions"} :
            new String[]{"Request ID", "Patient ID", "Blood Group", "Quantity", "Date", "Status"};
        
        Object[][] data = new Object[requests.size()][columns.length];
        
        for (int i = 0; i < requests.size(); i++) {
            BloodRequest req = requests.get(i);
            data[i][0] = req.getRequestID();
            data[i][1] = req.getPatientID();
            data[i][2] = req.getRequestedBloodGroup();
            data[i][3] = req.getQuantity() + " units";
            data[i][4] = req.getRequestDate();
            data[i][5] = req.getStatus();
            if (showActions) {
                data[i][6] = "Fulfill / Reject";
            }
        }
        
        JTable table = new JTable(data, columns);
        table.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        table.setRowHeight(45);
        table.setShowGrid(true);
        table.setGridColor(new Color(240, 240, 240));
        
        styleTableHeader(table);
        // Override background color if distinct color was intended, but ensure opaque is true
        // table.getTableHeader().setBackground(new Color(142, 68, 173)); 
        
        if (showActions) {
            table.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    int row = table.rowAtPoint(e.getPoint());
                    int col = table.columnAtPoint(e.getPoint());
                    if (col == 6 && row >= 0) {
                        int requestId = (int) table.getValueAt(row, 0);
                        String bloodGroup = (String) table.getValueAt(row, 2);
                        
                        int action = JOptionPane.showOptionDialog(parentDialog,
                            "Choose action for Request #" + requestId + " (" + bloodGroup + ")",
                            "Request Action",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            new String[]{"✓ Fulfill", "✕ Reject"},
                            "Fulfill");
                        
                        BloodRequestService service = new BloodRequestService();
                        if (action == 0) {
                            if (service.fulfillRequest(requestId, currentUser.getUserID())) {
                                showSuccess(parentDialog, "Request fulfilled successfully!");
                                parentDialog.dispose();
                                openRequestManagement();
                            } else {
                                showError(parentDialog, "Failed to fulfill request. Check blood availability.");
                            }
                        } else if (action == 1) {
                            if (service.rejectRequest(requestId, currentUser.getUserID())) {
                                showSuccess(parentDialog, "Request rejected!");
                                parentDialog.dispose();
                                openRequestManagement();
                            }
                        }
                    }
                }
            });
        }
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        
        JLabel infoLabel = new JLabel("Total: " + requests.size() + " requests");
        infoLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        infoLabel.setForeground(TEXT_SECONDARY);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(infoLabel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void showCreateRequestDialog(JDialog parent) {
        JDialog dialog = new JDialog(parent, "Create Blood Request", true);
        dialog.setSize(550, 500);
        dialog.setLocationRelativeTo(parent);
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        panel.setBackground(CARD_COLOR);
        
        JLabel titleLabel = new JLabel("New Blood Request");
        titleLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 22));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(25));
        
        JTextField patientIdField = createStyledTextField("Patient ID");
        
        JPanel bloodGroupPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bloodGroupPanel.setOpaque(false);
        JLabel bgLabel = new JLabel("Blood Group:");
        bgLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        String[] bloodGroups = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        JComboBox<String> bloodGroupCombo = new JComboBox<>(bloodGroups);
        bloodGroupCombo.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        bloodGroupCombo.setPreferredSize(new Dimension(150, 35));
        bloodGroupPanel.add(bgLabel);
        bloodGroupPanel.add(Box.createHorizontalStrut(10));
        bloodGroupPanel.add(bloodGroupCombo);
        
        JTextField quantityField = createStyledTextField("Quantity (units)");
        
        panel.add(patientIdField);
        panel.add(Box.createVerticalStrut(15));
        panel.add(bloodGroupPanel);
        panel.add(Box.createVerticalStrut(15));
        panel.add(quantityField);
        panel.add(Box.createVerticalStrut(30));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setOpaque(false);
        
        JButton createBtn = createStyledActionButton("✓ Create Request", new Color(46, 204, 113));
        createBtn.addActionListener(e -> {
            try {
                int patientId = Integer.parseInt(patientIdField.getText().trim());
                String bloodGroup = (String) bloodGroupCombo.getSelectedItem();
                int quantity = Integer.parseInt(quantityField.getText().trim());
                
                BloodRequestService service = new BloodRequestService();
                if (service.createRequest(patientId, bloodGroup, quantity, new java.sql.Date(System.currentTimeMillis()))) {
                    showSuccess(dialog, "Blood request created successfully!");
                    dialog.dispose();
                    parent.dispose();
                    openRequestManagement();
                } else {
                    showError(dialog, "Failed to create request. Check if hospital is approved.");
                }
            } catch (NumberFormatException ex) {
                showError(dialog, "Please enter valid numbers for Patient ID and Quantity!");
            }
        });
        
        JButton cancelBtn = createStyledActionButton("✕ Cancel", new Color(149, 165, 166));
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(createBtn);
        buttonPanel.add(cancelBtn);
        panel.add(buttonPanel);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private void openHospitalRegistration() {
        JDialog dialog = new JDialog(this, "Hospital Registration", true);
        dialog.setSize(600, 450);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        panel.setBackground(CARD_COLOR);
        
        JLabel titleLabel = new JLabel("🏥 Register New Hospital");
        titleLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 24));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(30));
        
        JLabel infoLabel = new JLabel("<html><center>Register a new hospital to the blood bank system.<br>Hospital will be pending approval by admin.</center></html>");
        infoLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        infoLabel.setForeground(TEXT_SECONDARY);
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panel.add(infoLabel);
        panel.add(Box.createVerticalStrut(25));
        
        JTextField hospitalNameField = createStyledTextField("Hospital Name");
        JTextField addressField = createStyledTextField("Hospital Address");
        
        panel.add(hospitalNameField);
        panel.add(Box.createVerticalStrut(15));
        panel.add(addressField);
        panel.add(Box.createVerticalStrut(30));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setOpaque(false);
        
        JButton registerBtn = createStyledActionButton("✓ Register Hospital", new Color(46, 204, 113));
        registerBtn.addActionListener(e -> {
            String hospitalName = hospitalNameField.getText().trim();
            String address = addressField.getText().trim();
            
            if (hospitalName.isEmpty() || address.isEmpty()) {
                showError(dialog, "All fields are required!");
                return;
            }
            
            if (hospitalService.registerHospital(hospitalName, address)) {
                showSuccess(dialog, "Hospital registered successfully!\nPending admin approval.");
                dialog.dispose();
            } else {
                showError(dialog, "Failed to register hospital!");
            }
        });
        
        JButton cancelBtn = createStyledActionButton("✕ Cancel", new Color(149, 165, 166));
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(registerBtn);
        buttonPanel.add(cancelBtn);
        panel.add(buttonPanel);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private void openPatientRegistration() {
        JDialog dialog = new JDialog(this, "Patient Registration", true);
        dialog.setSize(600, 550);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        panel.setBackground(CARD_COLOR);
        
        JLabel titleLabel = new JLabel("🧑‍⚕️ Register New Patient");
        titleLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 24));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(25));
        
        JTextField fullNameField = createStyledTextField("Patient Full Name");
        
        JPanel bloodGroupPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bloodGroupPanel.setOpaque(false);
        JLabel bgLabel = new JLabel("Blood Group:");
        bgLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        String[] bloodGroups = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        JComboBox<String> bloodGroupCombo = new JComboBox<>(bloodGroups);
        bloodGroupCombo.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        bloodGroupCombo.setPreferredSize(new Dimension(150, 35));
        bloodGroupPanel.add(bgLabel);
        bloodGroupPanel.add(Box.createHorizontalStrut(10));
        bloodGroupPanel.add(bloodGroupCombo);
        
        JTextField hospitalIdField = createStyledTextField("Hospital ID");
        
        JLabel noteLabel = new JLabel("<html><i>Note: Hospital must be approved before adding patients</i></html>");
        noteLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.ITALIC, 12));
        noteLabel.setForeground(TEXT_SECONDARY);
        noteLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        panel.add(fullNameField);
        panel.add(Box.createVerticalStrut(15));
        panel.add(bloodGroupPanel);
        panel.add(Box.createVerticalStrut(15));
        panel.add(hospitalIdField);
        panel.add(Box.createVerticalStrut(10));
        panel.add(noteLabel);
        panel.add(Box.createVerticalStrut(30));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setOpaque(false);
        
        JButton registerBtn = createStyledActionButton("✓ Register Patient", new Color(46, 204, 113));
        registerBtn.addActionListener(e -> {
            String fullName = fullNameField.getText().trim();
            String bloodGroup = (String) bloodGroupCombo.getSelectedItem();
            String hospitalIdStr = hospitalIdField.getText().trim();
            
            if (fullName.isEmpty() || hospitalIdStr.isEmpty()) {
                showError(dialog, "All fields are required!");
                return;
            }
            
            try {
                int hospitalId = Integer.parseInt(hospitalIdStr);
                
                if (hospitalService.addPatient(fullName, bloodGroup, hospitalId)) {
                    showSuccess(dialog, "Patient registered successfully!");
                    dialog.dispose();
                } else {
                    showError(dialog, "Failed to register patient!\nCheck if hospital ID is valid and approved.");
                }
            } catch (NumberFormatException ex) {
                showError(dialog, "Hospital ID must be a valid number!");
            }
        });
        
        JButton cancelBtn = createStyledActionButton("✕ Cancel", new Color(149, 165, 166));
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(registerBtn);
        buttonPanel.add(cancelBtn);
        panel.add(buttonPanel);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    // ==================== UTILITY METHODS ====================
    
    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to logout?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            SystemLog.logAction(currentUser.getUserID(), "Logout", 
                "User " + currentUser.getUsername() + " logged out");
            currentUser = null;
            
            // Reset alert state on logout
            alertDismissed = false;
            criticalBloodGroups.clear();
            urgentAlertIndicator = null;
            
            mainPanel.removeAll();
            mainPanel.add(createLoginPanel(), "LOGIN");
            cardLayout.show(mainPanel, "LOGIN");
        }
    }
    
    private void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    private void showSuccess(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
    
    @SuppressWarnings("unused")
    private void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BloodBankGUI gui = new BloodBankGUI();
            gui.setVisible(true);
        });
    }
}
