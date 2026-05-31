

import java.sql.*;

public class StaffService {
    
    public String processBloodTest(int donationID, int testedBy, String hepatitisA, String hepatitisB, 
                                   String hepatitisC, String hiv, String syphilis) {
        String query = "UPDATE BloodTest SET TestedBy = ?, TestDate = CURDATE(), " +
                      "HepatitisA = ?, HepatitisB = ?, HepatitisC = ?, HIV = ?, Syphilis = ? " +
                      "WHERE DonationID = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, testedBy);
            stmt.setString(2, hepatitisA);
            stmt.setString(3, hepatitisB);
            stmt.setString(4, hepatitisC);
            stmt.setString(5, hiv);
            stmt.setString(6, syphilis);
            stmt.setInt(7, donationID);
            
            int rows = stmt.executeUpdate();
            
            if (rows == 0) {
                // Record might not exist (if trigger failed), so insert it
                return insertBloodTest(donationID, testedBy, hepatitisA, hepatitisB, hepatitisC, hiv, syphilis);
            }
            
            if (rows > 0) {
                // Determine if test passed
                boolean passed = "Negative".equals(hepatitisA) && 
                               "Negative".equals(hepatitisB) && 
                               "Negative".equals(hepatitisC) && 
                               "Negative".equals(hiv) && 
                               "Negative".equals(syphilis);
                
                if (passed) {
                    markTestPassed(donationID);
                } else {
                    markTestFailed(donationID);
                }
                
                Logger.info("Blood test processed for Donation ID: " + donationID);
                SystemLog.logAction(testedBy, "Blood Test", "Test processed for Donation " + donationID);
                return null; // Success
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Logger.error("Failed to process blood test: " + e.getMessage());
            return "Database Error: " + e.getMessage();
        }
        return "Unknown error occurred";
    }
    
    private String insertBloodTest(int donationID, int testedBy, String hepatitisA, String hepatitisB, 
                                   String hepatitisC, String hiv, String syphilis) {
        String query = "INSERT INTO BloodTest (DonationID, TestedBy, TestDate, HepatitisA, HepatitisB, HepatitisC, HIV, Syphilis, FinalStatus) " +
                      "VALUES (?, ?, CURDATE(), ?, ?, ?, ?, ?, 'Pending')";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, donationID);
            stmt.setInt(2, testedBy);
            stmt.setString(3, hepatitisA);
            stmt.setString(4, hepatitisB);
            stmt.setString(5, hepatitisC);
            stmt.setString(6, hiv);
            stmt.setString(7, syphilis);
            
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                boolean passed = "Negative".equals(hepatitisA) && 
                               "Negative".equals(hepatitisB) && 
                               "Negative".equals(hepatitisC) && 
                               "Negative".equals(hiv) && 
                               "Negative".equals(syphilis);
                
                if (passed) {
                    markTestPassed(donationID);
                } else {
                    markTestFailed(donationID);
                }
                return null; // Success
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Logger.error("Failed to insert blood test: " + e.getMessage());
            return "Insert Error: " + e.getMessage();
        }
        return "Failed to insert test record";
    }
    
    private void markTestPassed(int donationID) {
        String query = "UPDATE BloodTest SET FinalStatus = 'Passed' WHERE DonationID = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, donationID);
            stmt.executeUpdate();

            // Add to Inventory
            String getDonationDetails = "SELECT d.BloodGroup, dn.QuantityML FROM Donations dn JOIN Donors d ON dn.DonorID = d.DonorID WHERE dn.DonationID = ?";
            try (PreparedStatement detailStmt = conn.prepareStatement(getDonationDetails)) {
                detailStmt.setInt(1, donationID);
                ResultSet rs = detailStmt.executeQuery();
                if (rs.next()) {
                    String bloodGroup = rs.getString("BloodGroup");
                    int quantity = rs.getInt("QuantityML");
                    
                    BloodInventoryService inventoryService = new BloodInventoryService();
                    inventoryService.addToInventory(donationID, bloodGroup, quantity);
                }
            }

        } catch (SQLException e) {
            Logger.error("Failed to mark test as passed: " + e.getMessage());
        }
    }

    private void markTestFailed(int donationID) {
        String query = "UPDATE BloodTest SET FinalStatus = 'Failed' WHERE DonationID = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, donationID);
            stmt.executeUpdate();
            
            // Add to FailedBlood
            String getBloodGroupQuery = "SELECT d.BloodGroup FROM Donations dn JOIN Donors d ON dn.DonorID = d.DonorID WHERE dn.DonationID = ?";
            try (PreparedStatement bgStmt = conn.prepareStatement(getBloodGroupQuery)) {
                bgStmt.setInt(1, donationID);
                ResultSet rs = bgStmt.executeQuery();
                if (rs.next()) {
                    String bloodGroup = rs.getString("BloodGroup");
                    FailedBloodDAO failedDAO = new FailedBloodDAO();
                    failedDAO.addFailedBlood(donationID, bloodGroup, "Test Failed");
                }
            }
            
        } catch (SQLException e) {
            Logger.error("Failed to mark test as failed: " + e.getMessage());
        }
    }
    
    public String getTestStatus(int donationID) {
        String query = "SELECT FinalStatus FROM BloodTest WHERE DonationID = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, donationID);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("FinalStatus");
            }
        } catch (SQLException e) {
            Logger.error("Failed to get test status: " + e.getMessage());
        }
        return "Unknown";
    }
    
    public ResultSet getPendingTests() {
        String query = "SELECT bt.TestID, bt.DonationID, d.DonorID, dn.FullName, bt.FinalStatus " +
                      "FROM BloodTest bt " +
                      "JOIN Donations d ON bt.DonationID = d.DonationID " +
                      "JOIN Donors dn ON d.DonorID = dn.DonorID " +
                      "WHERE bt.FinalStatus = 'Pending'";
        
        try {
            Connection conn = DBConnection.getConnection();
            Statement stmt = conn.createStatement();
            return stmt.executeQuery(query);
        } catch (SQLException e) {
            Logger.error("Failed to get pending tests: " + e.getMessage());
        }
        return null;
    }
}