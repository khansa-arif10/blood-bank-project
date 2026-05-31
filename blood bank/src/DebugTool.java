
import java.sql.*;

public class DebugTool {
    public static void main(String[] args) {
        System.out.println("=== DEBUGGING TOOL ===");
        
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                System.out.println("Connection failed.");
                return;
            }
            
            System.out.println("Connected to database.");
            
            // 1. Check BloodTest Schema
            System.out.println("\n--- Checking BloodTest Schema ---");
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet columns = meta.getColumns(null, null, "BloodTest", null);
            while (columns.next()) {
                System.out.println("Column: " + columns.getString("COLUMN_NAME") + 
                                 " Type: " + columns.getString("TYPE_NAME") + 
                                 " Size: " + columns.getInt("COLUMN_SIZE"));
            }
            
            // 2. Check FailedBlood Schema
            System.out.println("\n--- Checking FailedBlood Schema ---");
            columns = meta.getColumns(null, null, "FailedBlood", null);
            while (columns.next()) {
                System.out.println("Column: " + columns.getString("COLUMN_NAME") + 
                                 " Type: " + columns.getString("TYPE_NAME"));
            }
            
            // 3. Check Pending Tests
            System.out.println("\n--- Checking Pending Tests ---");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM BloodTest WHERE FinalStatus = 'Pending'");
            while (rs.next()) {
                System.out.println("Found Pending Test: TestID=" + rs.getInt("TestID") + 
                                 " DonationID=" + rs.getInt("DonationID"));
            }
            
            // 4. Check Users
            System.out.println("\n--- Checking Users ---");
            rs = stmt.executeQuery("SELECT UserID, Username, Role FROM Users");
            while (rs.next()) {
                System.out.println("User: " + rs.getInt("UserID") + " - " + rs.getString("Username") + " (" + rs.getString("Role") + ")");
            }
            
            // 5. Try Update
            System.out.println("\n--- Trying Update on DonationID 2 ---");
            String query = "UPDATE BloodTest SET TestedBy = ?, TestDate = CURDATE(), " +
                          "HepatitisA = ?, HepatitisB = ?, HepatitisC = ?, HIV = ?, Syphilis = ? " +
                          "WHERE DonationID = ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, 2); // User tech1
            ps.setString(2, "Negative");
            ps.setString(3, "Negative");
            ps.setString(4, "Negative");
            ps.setString(5, "Negative");
            ps.setString(6, "Negative");
            ps.setInt(7, 2); // DonationID 2
            
            int rows = ps.executeUpdate();
            System.out.println("Update rows: " + rows);
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
