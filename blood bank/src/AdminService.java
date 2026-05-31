import java.sql.*;
import java.util.List;
public class AdminService {
    private UserDAO userDAO;
    private HospitalService hospitalService;
    public AdminService() {
        this.userDAO = new UserDAO();
        this.hospitalService = new HospitalService();
    }
    public boolean createUser(String username, String password, String fullName, String role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setFullName(fullName);
        user.setRole(role);
        boolean success = userDAO.addUser(user);
        if (success) {
            Logger.info("Admin created new user: " + username);
        }
        return success;
    }
    public List<User> getAllUsers() {
        return userDAO.getAllUsers();
    }
    public boolean deleteUser(int userID) {
        return userDAO.deleteUser(userID);
    }
    
    public boolean approveHospital(int hospitalID, int adminUserID) {
        return hospitalService.approveHospital(hospitalID, adminUserID);
    }
    
    public boolean rejectHospital(int hospitalID, int adminUserID) {
        return hospitalService.rejectHospital(hospitalID, adminUserID);
    }
    
    public List<Hospital> getPendingHospitals() {
        return hospitalService.getPendingHospitals();
    }
    
    public ResultSet getSystemLogs() {
        String query = "SELECT sl.LogID, sl.ActionType, sl.Description, sl.ActionTime, u.FullName " +
                      "FROM SystemLog sl " +
                      "LEFT JOIN Users u ON sl.UserID = u.UserID " +
                      "ORDER BY sl.ActionTime DESC LIMIT 50";
        
        try {
            Connection conn = DBConnection.getConnection();
            Statement stmt = conn.createStatement();
            return stmt.executeQuery(query);
        } catch (SQLException e) {
            Logger.error("Failed to retrieve system logs: " + e.getMessage());
        }
        return null;
    }
    
    public void generateReport() {
        System.out.println("\n========== BLOOD BANK REPORT ==========");
        
        // Inventory Summary
        BloodInventoryService inventoryService = new BloodInventoryService();
        System.out.println("\n--- Blood Inventory ---");
        var inventory = inventoryService.getInventorySummary();
        if (inventory.isEmpty()) {
            System.out.println("No blood in inventory.");
        } else {
            for (var entry : inventory.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue() + " units");
            }
        }
        
        // Request Summary
        System.out.println("\n--- Blood Requests ---");
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT Status, COUNT(*) as Count FROM BloodRequests GROUP BY Status";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                System.out.println(rs.getString("Status") + ": " + rs.getInt("Count"));
            }
        } catch (SQLException e) {
            Logger.error("Failed to generate request summary: " + e.getMessage());
        }
        
        // Hospital Summary
        System.out.println("\n--- Hospitals ---");
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT Status, COUNT(*) as Count FROM Hospitals GROUP BY Status";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                System.out.println(rs.getString("Status") + ": " + rs.getInt("Count"));
            }
        } catch (SQLException e) {
            Logger.error("Failed to generate hospital summary: " + e.getMessage());
        }
        
        System.out.println("\n=======================================\n");
    }
}