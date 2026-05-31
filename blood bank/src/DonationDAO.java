

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DonationDAO {
    
    public boolean addDonation(BloodDonation donation) {
        String query = "INSERT INTO Donations (DonorID, DonationDate, QuantityML, CollectedBy) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, donation.getDonorID());
            stmt.setDate(2, donation.getDonationDate());
            stmt.setInt(3, donation.getQuantityML());
            stmt.setInt(4, donation.getCollectedBy());
            
            int rows = stmt.executeUpdate();
            
            if (rows > 0) {
                // Get the generated DonationID
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int donationID = generatedKeys.getInt(1);
                    donation.setDonationID(donationID);
                    
                    // Manually insert into BloodTest to ensure record exists (in case triggers are missing)
                    createBloodTestRecord(conn, donationID);
                }
                Logger.info("Donation recorded for DonorID: " + donation.getDonorID() + " (DonationID: " + donation.getDonationID() + ")");
                return true;
            }
            return false;
        } catch (SQLException e) {
            Logger.error("Failed to record donation: " + e.getMessage());
            return false;
        }
    }
    
    private void createBloodTestRecord(Connection conn, int donationID) {
        String query = "INSERT INTO BloodTest (DonationID, FinalStatus) VALUES (?, 'Pending')";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, donationID);
            stmt.executeUpdate();
        } catch (SQLException e) {
            // Ignore if already exists (e.g. if trigger worked)
            Logger.warning("Could not create BloodTest record (might already exist): " + e.getMessage());
        }
    }
    
    public List<BloodDonation> getAllDonations() {
        List<BloodDonation> donations = new ArrayList<>();
        String query = "SELECT * FROM Donations";
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                donations.add(new BloodDonation(
                    rs.getInt("DonationID"),
                    rs.getInt("DonorID"),
                    rs.getDate("DonationDate"),
                    rs.getInt("QuantityML"),
                    rs.getInt("CollectedBy")
                ));
            }
        } catch (SQLException e) {
            Logger.error("Failed to retrieve donations: " + e.getMessage());
        }
        return donations;
    }
    
    public BloodDonation getDonationByID(int donationID) {
        String query = "SELECT * FROM Donations WHERE DonationID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, donationID);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return new BloodDonation(
                    rs.getInt("DonationID"),
                    rs.getInt("DonorID"),
                    rs.getDate("DonationDate"),
                    rs.getInt("QuantityML"),
                    rs.getInt("CollectedBy")
                );
            }
        } catch (SQLException e) {
            Logger.error("Failed to retrieve donation: " + e.getMessage());
        }
        return null;
    }
    
    public List<BloodDonation> getDonationsByDonor(int donorID) {
        List<BloodDonation> donations = new ArrayList<>();
        String query = "SELECT * FROM Donations WHERE DonorID = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, donorID);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                donations.add(new BloodDonation(
                    rs.getInt("DonationID"),
                    rs.getInt("DonorID"),
                    rs.getDate("DonationDate"),
                    rs.getInt("QuantityML"),
                    rs.getInt("CollectedBy")
                ));
            }
        } catch (SQLException e) {
            Logger.error("Failed to retrieve donations by donor: " + e.getMessage());
        }
        return donations;
    }
}