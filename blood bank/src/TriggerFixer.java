
import java.sql.*;

public class TriggerFixer {
    public static void fixTriggers() {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            System.out.println("Dropping potentially conflicting trigger 'trg_process_blood_test'...");
            try {
                stmt.executeUpdate("DROP TRIGGER IF EXISTS trg_process_blood_test");
                System.out.println("Trigger dropped successfully.");
            } catch (SQLException e) {
                System.out.println("Failed to drop trigger: " + e.getMessage());
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
