

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class BloodInventoryService {
    
    public Map<String, Integer> getInventorySummary() {
        Map<String, Integer> summary = new HashMap<>();
        String query = "SELECT BloodGroup, SUM(Quantity) as Total FROM BloodInventory GROUP BY BloodGroup";
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                summary.put(rs.getString("BloodGroup"), rs.getInt("Total"));
            }
        } catch (SQLException e) {
            Logger.error("Failed to get inventory summary: " + e.getMessage());
        }
        
        return summary;
    }
    
    public boolean checkAvailability(String bloodGroup, int quantity) {
        String query = "SELECT SUM(Quantity) as Available FROM BloodInventory WHERE BloodGroup = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, bloodGroup);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                int available = rs.getInt("Available");
                return available >= quantity;
            }
        } catch (SQLException e) {
            Logger.error("Failed to check availability: " + e.getMessage());
        }
        
        return false;
    }
    
    public int getAvailableQuantity(String bloodGroup) {
        String query = "SELECT SUM(Quantity) as Available FROM BloodInventory WHERE BloodGroup = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, bloodGroup);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("Available");
            }
        } catch (SQLException e) {
            Logger.error("Failed to get available quantity: " + e.getMessage());
        }
        
        return 0;
    }
    
    /**
     * Add blood to inventory after donation
     * @param donationID - The ID of the donation record
     * @param bloodGroup - The blood group (e.g., "A+", "O-")
     * @param quantity - Quantity in ml (typically 450-500ml per donation)
     * @return true if successfully added to inventory
     */
    public boolean addToInventory(int donationID, String bloodGroup, int quantity) {
        String query = "INSERT INTO BloodInventory (DonationID, BloodGroup, Quantity, AddedDate) VALUES (?, ?, ?, CURDATE())";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, donationID);
            stmt.setString(2, bloodGroup);
            stmt.setInt(3, quantity);
            
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                Logger.info("Added " + quantity + "ml of " + bloodGroup + " blood to inventory (DonationID: " + donationID + ")");
                return true;
            }
        } catch (SQLException e) {
            Logger.error("Failed to add to inventory: " + e.getMessage());
        }
        
        return false;
    }
    
    public void checkExpiration() {
        String query = "SELECT InventoryID, DonationID, BloodGroup, AddedDate FROM BloodInventory WHERE DATEDIFF(CURDATE(), AddedDate) > 42";
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            FailedBloodDAO failedDAO = new FailedBloodDAO();
            
            while (rs.next()) {
                int inventoryID = rs.getInt("InventoryID");
                int donationID = rs.getInt("DonationID");
                String bloodGroup = rs.getString("BloodGroup");
                
                // Add to FailedBlood
                failedDAO.addFailedBlood(donationID, bloodGroup, "Expired");
                
                // Remove from Inventory
                deleteFromInventory(inventoryID);
                
                Logger.info("Moved expired blood (InventoryID: " + inventoryID + ") to FailedBlood");
            }
        } catch (SQLException e) {
            Logger.error("Failed to check expiration: " + e.getMessage());
        }
    }
    
    private void deleteFromInventory(int inventoryID) {
        String query = "DELETE FROM BloodInventory WHERE InventoryID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, inventoryID);
            stmt.executeUpdate();
        } catch (SQLException e) {
            Logger.error("Failed to delete from inventory: " + e.getMessage());
        }
    }
    
    /**
     * Reduce blood from inventory (when fulfilling a request)
     * @param bloodGroup - The blood group
     * @param quantity - Quantity to reduce
     * @return true if successfully reduced
     */
    public boolean reduceFromInventory(String bloodGroup, int quantity) {
        if (!checkAvailability(bloodGroup, quantity)) {
            Logger.warning("Insufficient inventory for " + bloodGroup + ". Required: " + quantity);
            return false;
        }
        
        String query = "UPDATE BloodInventory SET Quantity = Quantity - ? " +
                      "WHERE BloodGroup = ? AND Quantity > 0 " +
                      "ORDER BY AddedDate ASC LIMIT 1";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, quantity);
            stmt.setString(2, bloodGroup);
            
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                Logger.info("Reduced " + quantity + "ml of " + bloodGroup + " from inventory");
                return true;
            }
        } catch (SQLException e) {
            Logger.error("Failed to reduce inventory: " + e.getMessage());
        }
        
        return false;
    }
}