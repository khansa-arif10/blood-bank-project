

import java.sql.*;

public class RequestFulfillment {
    
    public static boolean fulfillRequest(int requestID, int inventoryID, int fulfilledBy, int quantity) {
        String query = "INSERT INTO BloodFulfillment (RequestID, InventoryID, FulfilledBy, FulfillmentDate, QuantityUsed) " +
                      "VALUES (?, ?, ?, CURDATE(), ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, requestID);
            stmt.setInt(2, inventoryID);
            stmt.setInt(3, fulfilledBy);
            stmt.setInt(4, quantity);
            
            int rows = stmt.executeUpdate();
            
            if (rows > 0) {
                // Update inventory quantity
                updateInventoryQuantity(inventoryID, quantity);
                
                // Update request status
                updateRequestStatus(requestID, "Fulfilled");
                
                Logger.info("Blood request fulfilled: Request ID " + requestID);
                SystemLog.logAction(fulfilledBy, "Request Fulfillment", 
                    "Request " + requestID + " fulfilled with " + quantity + " units");
                return true;
            }
        } catch (SQLException e) {
            Logger.error("Failed to fulfill request: " + e.getMessage());
        }
        return false;
    }
    
    private static void updateInventoryQuantity(int inventoryID, int quantityUsed) {
        String query = "UPDATE BloodInventory SET Quantity = Quantity - ? WHERE InventoryID = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, quantityUsed);
            stmt.setInt(2, inventoryID);
            stmt.executeUpdate();
        } catch (SQLException e) {
            Logger.error("Failed to update inventory: " + e.getMessage());
        }
    }
    
    private static void updateRequestStatus(int requestID, String status) {
        String query = "UPDATE BloodRequests SET Status = ? WHERE RequestID = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, status);
            stmt.setInt(2, requestID);
            stmt.executeUpdate();
        } catch (SQLException e) {
            Logger.error("Failed to update request status: " + e.getMessage());
        }
    }
    
    public static ResultSet getFulfillmentHistory(int requestID) {
        String query = "SELECT bf.*, u.FullName as FulfilledByName, bi.BloodGroup " +
                      "FROM BloodFulfillment bf " +
                      "JOIN Users u ON bf.FulfilledBy = u.UserID " +
                      "JOIN BloodInventory bi ON bf.InventoryID = bi.InventoryID " +
                      "WHERE bf.RequestID = ?";
        
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, requestID);
            return stmt.executeQuery();
        } catch (SQLException e) {
            Logger.error("Failed to get fulfillment history: " + e.getMessage());
        }
        return null;
    }
}