
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FailedBloodDAO {
    
    public boolean addFailedBlood(int donationID, String bloodGroup, String reason) {
        String query = "INSERT INTO FailedBlood (DonationID, BloodGroup, Reason, FailedDate) VALUES (?, ?, ?, CURDATE())";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, donationID);
            stmt.setString(2, bloodGroup);
            stmt.setString(3, reason);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Failed to add failed blood record: " + e.getMessage());
            return false;
        }
    }
    
    public List<FailedBlood> getAllFailedBlood() {
        List<FailedBlood> list = new ArrayList<>();
        String query = "SELECT * FROM FailedBlood ORDER BY FailedDate DESC";
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                FailedBlood fb = new FailedBlood(
                    rs.getInt("FailedID"),
                    rs.getInt("DonationID"),
                    rs.getString("BloodGroup"),
                    rs.getString("Reason"),
                    rs.getDate("FailedDate")
                );
                list.add(fb);
            }
        } catch (SQLException e) {
            Logger.error("Failed to retrieve failed blood records: " + e.getMessage());
        }
        return list;
    }
}
