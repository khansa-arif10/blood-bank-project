
import java.sql.*;

public class SchemaFixer {
    public static void fixFailedBloodTable() {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return;
            
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet rs = meta.getColumns(null, null, "FailedBlood", "BloodGroup");
            
            if (!rs.next()) {
                System.out.println("Fixing schema: Adding BloodGroup column to FailedBlood table...");
                try (Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("ALTER TABLE FailedBlood ADD COLUMN BloodGroup VARCHAR(10) AFTER DonationID");
                    System.out.println("Schema fixed successfully.");
                }
            }
            
            // Check Donations table for QuantityML
            rs = meta.getColumns(null, null, "Donations", "QuantityML");
            if (!rs.next()) {
                System.out.println("Fixing schema: Adding QuantityML column to Donations table...");
                try (Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("ALTER TABLE Donations ADD COLUMN QuantityML INT DEFAULT 450 AFTER DonationDate");
                    System.out.println("Schema fixed successfully (Donations).");
                }
            }
        } catch (SQLException e) {
            System.out.println("Schema check failed: " + e.getMessage());
        }
    }
}
