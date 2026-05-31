

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SystemLog {
    
    public static void logAction(Integer userID, String actionType, String description) {
        String query = "INSERT INTO SystemLog (UserID, ActionType, Description) VALUES (?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            if (userID != null) {
                stmt.setInt(1, userID);
            } else {
                stmt.setNull(1, java.sql.Types.INTEGER);
            }
            stmt.setString(2, actionType);
            stmt.setString(3, description);
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            Logger.error("Failed to log system action: " + e.getMessage());
        }
    }
}