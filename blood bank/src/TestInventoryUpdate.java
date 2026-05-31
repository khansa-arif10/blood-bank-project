
import java.sql.*;
import java.util.Map;

/**
 * Test program to verify donation recording updates inventory
 */
public class TestInventoryUpdate {
    
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║   BLOOD INVENTORY UPDATE TEST - DONATION RECORDING      ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝\n");
        
        // Show current inventory
        System.out.println("📊 CURRENT INVENTORY:");
        System.out.println("─────────────────────────────────────────────────────────");
        displayInventory();
        
        System.out.println("\n\n🩸 TESTING DONATION RECORDING...\n");
        System.out.println("This will:");
        System.out.println("  1. Record a donation in Donations table");
        System.out.println("  2. Add blood to BloodInventory table");
        System.out.println("  3. Update donor's last donation date");
        System.out.println("\nPress Enter to continue or Ctrl+C to cancel...");
        
        try {
            System.in.read();
        } catch (Exception e) {
            // Ignore
        }
        
        // Test donation recording
        testDonationRecording();
        
        System.out.println("\n\n📊 UPDATED INVENTORY:");
        System.out.println("─────────────────────────────────────────────────────────");
        displayInventory();
        
        System.out.println("\n\n✓ TEST COMPLETE!");
        System.out.println("\nVerification Steps:");
        System.out.println("  1. Check if inventory quantities increased");
        System.out.println("  2. Run: SELECT * FROM BloodInventory ORDER BY AddedDate DESC LIMIT 5");
        System.out.println("  3. Verify DonationID is linked correctly\n");
    }
    
    private static void displayInventory() {
        BloodInventoryService inventoryService = new BloodInventoryService();
        Map<String, Integer> inventory = inventoryService.getInventorySummary();
        
        String[] bloodGroups = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        
        System.out.printf("%-12s | %-15s%n", "Blood Group", "Quantity (ml)");
        System.out.println("─────────────┼─────────────────");
        
        int total = 0;
        for (String bg : bloodGroups) {
            int qty = inventory.getOrDefault(bg, 0);
            total += qty;
            
            String status = qty == 0 ? "❌ EMPTY" : qty < 450 ? "⚠️  LOW" : "✓ OK";
            System.out.printf("%-12s | %6d ml   %s%n", bg, qty, status);
        }
        
        System.out.println("─────────────┼─────────────────");
        System.out.printf("%-12s | %6d ml%n", "TOTAL", total);
    }
    
    private static void testDonationRecording() {
        // Get first available donor
        DonorDAO donorDAO = new DonorDAO();
        var donors = donorDAO.getAllDonors();
        
        if (donors.isEmpty()) {
            System.out.println("❌ No donors found! Please register a donor first.");
            return;
        }
        
        Donor testDonor = donors.get(0);
        System.out.println("📝 Recording test donation:");
        System.out.println("   Donor: " + testDonor.getFullName() + " (ID: " + testDonor.getDonorID() + ")");
        System.out.println("   Blood Group: " + testDonor.getBloodGroup());
        System.out.println("   Quantity: 450 ml (standard donation)");
        
        // Record donation
        Date today = new Date(System.currentTimeMillis());
        BloodDonation donation = new BloodDonation();
        donation.setDonorID(testDonor.getDonorID());
        donation.setDonationDate(today);
        donation.setCollectedBy(1); // System user
        
        DonationDAO donationDAO = new DonationDAO();
        boolean donated = donationDAO.addDonation(donation);
        
        if (!donated) {
            System.out.println("❌ Failed to record donation!");
            return;
        }
        
        System.out.println("   ✓ Donation recorded (ID: " + donation.getDonationID() + ")");
        
        // Add to inventory
        BloodInventoryService inventoryService = new BloodInventoryService();
        boolean inventoryAdded = inventoryService.addToInventory(
            donation.getDonationID(), 
            testDonor.getBloodGroup(), 
            450
        );
        
        if (inventoryAdded) {
            System.out.println("   ✓ Added to inventory successfully!");
            
            // Update donor
            testDonor.setLastDonationDate(today);
            donorDAO.updateDonor(testDonor);
            System.out.println("   ✓ Updated donor's last donation date");
            
            System.out.println("\n✓ Complete donation workflow executed successfully!");
        } else {
            System.out.println("   ❌ Failed to add to inventory!");
        }
    }
    
    private static void showRecentInventoryEntries() {
        System.out.println("\n📋 RECENT INVENTORY ENTRIES:");
        System.out.println("─────────────────────────────────────────────────────────");
        
        String query = "SELECT i.InventoryID, i.DonationID, i.BloodGroup, i.Quantity, " +
                      "i.AddedDate, d.DonorID, dn.FullName " +
                      "FROM BloodInventory i " +
                      "JOIN Donations d ON i.DonationID = d.DonationID " +
                      "JOIN Donors dn ON d.DonorID = dn.DonorID " +
                      "ORDER BY i.AddedDate DESC LIMIT 5";
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            System.out.printf("%-8s | %-10s | %-6s | %-8s | %-12s | %s%n", 
                "Inv ID", "Donation", "Blood", "Quantity", "Date", "Donor");
            System.out.println("─────────┼────────────┼────────┼──────────┼──────────────┼─────────");
            
            while (rs.next()) {
                System.out.printf("%-8d | %-10d | %-6s | %6d ml | %-12s | %s%n",
                    rs.getInt("InventoryID"),
                    rs.getInt("DonationID"),
                    rs.getString("BloodGroup"),
                    rs.getInt("Quantity"),
                    rs.getDate("AddedDate"),
                    rs.getString("FullName"));
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
