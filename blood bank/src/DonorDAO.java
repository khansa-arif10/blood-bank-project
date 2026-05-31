

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DonorDAO {
    
    public boolean addDonor(Donor donor) {
        String query = "INSERT INTO Donors (FullName, Gender, Age, BloodGroup, Phone, LastDonationDate) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, donor.getFullName());
            stmt.setString(2, donor.getGender());
            stmt.setInt(3, donor.getAge());
            stmt.setString(4, donor.getBloodGroup());
            stmt.setString(5, donor.getPhone());
            stmt.setDate(6, donor.getLastDonationDate());
            
            int rows = stmt.executeUpdate();
            Logger.info("Donor added: " + donor.getFullName());
            return rows > 0;
        } catch (SQLException e) {
            Logger.error("Failed to add donor: " + e.getMessage());
            return false;
        }
    }
    
    public List<Donor> getAllDonors() {
        List<Donor> donors = new ArrayList<>();
        String query = "SELECT * FROM Donors";
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                donors.add(new Donor(
                    rs.getInt("DonorID"),
                    rs.getString("FullName"),
                    rs.getString("Gender"),
                    rs.getInt("Age"),
                    rs.getString("BloodGroup"),
                    rs.getString("Phone"),
                    rs.getDate("LastDonationDate")
                ));
            }
        } catch (SQLException e) {
            Logger.error("Failed to retrieve donors: " + e.getMessage());
        }
        return donors;
    }
    
    public Donor getDonorByID(int donorID) {
        String query = "SELECT * FROM Donors WHERE DonorID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, donorID);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return new Donor(
                    rs.getInt("DonorID"),
                    rs.getString("FullName"),
                    rs.getString("Gender"),
                    rs.getInt("Age"),
                    rs.getString("BloodGroup"),
                    rs.getString("Phone"),
                    rs.getDate("LastDonationDate")
                );
            }
        } catch (SQLException e) {
            Logger.error("Failed to retrieve donor: " + e.getMessage());
        }
        return null;
    }
    
    public boolean updateDonor(Donor donor) {
        String query = "UPDATE Donors SET FullName = ?, Gender = ?, Age = ?, BloodGroup = ?, Phone = ?, LastDonationDate = ? WHERE DonorID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, donor.getFullName());
            stmt.setString(2, donor.getGender());
            stmt.setInt(3, donor.getAge());
            stmt.setString(4, donor.getBloodGroup());
            stmt.setString(5, donor.getPhone());
            stmt.setDate(6, donor.getLastDonationDate());
            stmt.setInt(7, donor.getDonorID());
            
            int rows = stmt.executeUpdate();
            Logger.info("Donor updated: " + donor.getFullName());
            return rows > 0;
        } catch (SQLException e) {
            Logger.error("Failed to update donor: " + e.getMessage());
            return false;
        }
    }
    
    public boolean deleteDonor(int donorID) {
        String query = "DELETE FROM Donors WHERE DonorID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, donorID);
            int rows = stmt.executeUpdate();
            Logger.info("Donor deleted: ID " + donorID);
            return rows > 0;
        } catch (SQLException e) {
            Logger.error("Failed to delete donor: " + e.getMessage());
            return false;
        }
    }
    
    public List<Donor> searchDonorsByBloodGroup(String bloodGroup) {
        List<Donor> donors = new ArrayList<>();
        String query = "SELECT * FROM Donors WHERE BloodGroup = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, bloodGroup);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                donors.add(new Donor(
                    rs.getInt("DonorID"),
                    rs.getString("FullName"),
                    rs.getString("Gender"),
                    rs.getInt("Age"),
                    rs.getString("BloodGroup"),
                    rs.getString("Phone"),
                    rs.getDate("LastDonationDate")
                ));
            }
        } catch (SQLException e) {
            Logger.error("Failed to search donors: " + e.getMessage());
        }
        return donors;
    }
    
    /**
     * Get eligible donors for urgent alerts
     * Returns donors who haven't donated in the last 56 days for a specific blood group
     */
    public List<Donor> getEligibleDonorsForAlert(String bloodGroup, int daysSinceLastDonation) {
        List<Donor> eligibleDonors = new ArrayList<>();
        String query = "SELECT * FROM Donors WHERE BloodGroup = ? AND " +
                      "(LastDonationDate IS NULL OR DATEDIFF(CURDATE(), LastDonationDate) >= ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, bloodGroup);
            stmt.setInt(2, daysSinceLastDonation);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                eligibleDonors.add(new Donor(
                    rs.getInt("DonorID"),
                    rs.getString("FullName"),
                    rs.getString("Gender"),
                    rs.getInt("Age"),
                    rs.getString("BloodGroup"),
                    rs.getString("Phone"),
                    rs.getDate("LastDonationDate")
                ));
            }
            Logger.info("Found " + eligibleDonors.size() + " eligible donors for " + bloodGroup);
        } catch (SQLException e) {
            Logger.error("Failed to get eligible donors: " + e.getMessage());
        }
        return eligibleDonors;
    }
}