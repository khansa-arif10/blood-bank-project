

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BloodRequestService {
    
    public boolean createRequest(int patientID, String bloodGroup, int quantity, Date requestDate) {
        String query = "INSERT INTO BloodRequests (PatientID, RequestedBloodGroup, Quantity, RequestDate) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, patientID);
            stmt.setString(2, bloodGroup);
            stmt.setInt(3, quantity);
            stmt.setDate(4, requestDate);
            
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                Logger.info("Blood request created for Patient ID: " + patientID);
                SystemLog.logAction(null, "Blood Request", "Request created for Patient ID " + patientID);
                return true;
            }
        } catch (SQLException e) {
            Logger.error("Failed to create blood request: " + e.getMessage());
            if (e.getMessage().contains("Hospital is not approved")) {
                System.out.println("ERROR: Cannot create request - Hospital is not approved!");
            }
        }
        return false;
    }
    
    public List<BloodRequest> getAllRequests() {
        List<BloodRequest> requests = new ArrayList<>();
        String query = "SELECT * FROM BloodRequests ORDER BY RequestDate DESC";
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                requests.add(new BloodRequest(
                    rs.getInt("RequestID"),
                    rs.getInt("PatientID"),
                    rs.getString("RequestedBloodGroup"),
                    rs.getInt("Quantity"),
                    rs.getDate("RequestDate"),
                    rs.getString("Status")
                ));
            }
        } catch (SQLException e) {
            Logger.error("Failed to retrieve blood requests: " + e.getMessage());
        }
        return requests;
    }
    
    public List<BloodRequest> getPendingRequests() {
        List<BloodRequest> requests = new ArrayList<>();
        String query = "SELECT * FROM BloodRequests WHERE Status = 'Pending' ORDER BY RequestDate";
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                requests.add(new BloodRequest(
                    rs.getInt("RequestID"),
                    rs.getInt("PatientID"),
                    rs.getString("RequestedBloodGroup"),
                    rs.getInt("Quantity"),
                    rs.getDate("RequestDate"),
                    rs.getString("Status")
                ));
            }
        } catch (SQLException e) {
            Logger.error("Failed to retrieve pending requests: " + e.getMessage());
        }
        return requests;
    }
    
    public boolean fulfillRequest(int requestID, int userID) {
        String query = "UPDATE BloodRequests SET Status = 'Fulfilled' WHERE RequestID = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, requestID);
            int rows = stmt.executeUpdate();
            
            if (rows > 0) {
                Logger.info("Blood request fulfilled: Request ID " + requestID);
                SystemLog.logAction(userID, "Request Fulfilled", "Request ID " + requestID + " fulfilled");
                return true;
            }
        } catch (SQLException e) {
            Logger.error("Failed to fulfill request: " + e.getMessage());
        }
        return false;
    }
    
    public boolean rejectRequest(int requestID, int userID) {
        String query = "UPDATE BloodRequests SET Status = 'Rejected' WHERE RequestID = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, requestID);
            int rows = stmt.executeUpdate();
            
            if (rows > 0) {
                Logger.info("Blood request rejected: Request ID " + requestID);
                SystemLog.logAction(userID, "Request Rejected", "Request ID " + requestID + " rejected");
                return true;
            }
        } catch (SQLException e) {
            Logger.error("Failed to reject request: " + e.getMessage());
        }
        return false;
    }
}