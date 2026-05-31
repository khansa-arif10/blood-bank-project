
import java.util.List;
import java.util.Map;

/**
 * Test program to demonstrate the Urgent Alert System
 * This program checks blood inventory and alerts eligible donors
 */
public class TestUrgentAlerts {
    
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║    BLOOD BANK URGENT ALERT SYSTEM - TEST PROGRAM          ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝\n");
        
        // Test the urgent alert system
        System.out.println("Testing Automatic Alert System...\n");
        UrgentAlert.checkLowInventoryAndAlertDonors();
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("DETAILED ALERT REPORT");
        System.out.println("=".repeat(60) + "\n");
        
        // Get detailed report
        Map<String, List<Donor>> alertReport = UrgentAlert.getAlertReport();
        
        if (alertReport.isEmpty()) {
            System.out.println("✓ No blood groups require urgent alerts at this time.");
        } else {
            System.out.println("Blood groups requiring urgent attention:\n");
            
            for (Map.Entry<String, List<Donor>> entry : alertReport.entrySet()) {
                String bloodGroup = entry.getKey();
                List<Donor> donors = entry.getValue();
                
                System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                System.out.println("Blood Group: " + bloodGroup);
                System.out.println("Eligible Donors: " + donors.size());
                System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                
                if (donors.isEmpty()) {
                    System.out.println("  No eligible donors found.\n");
                } else {
                    System.out.println("  Donor Details:");
                    int count = 1;
                    for (Donor donor : donors) {
                        System.out.println("\n  " + count + ". " + donor.getFullName());
                        System.out.println("     ID: " + donor.getDonorID());
                        System.out.println("     Phone: " + donor.getPhone());
                        System.out.println("     Age: " + donor.getAge() + " | Gender: " + donor.getGender());
                        
                        if (donor.getLastDonationDate() != null) {
                            System.out.println("     Last Donation: " + donor.getLastDonationDate());
                        } else {
                            System.out.println("     Last Donation: Never donated (New donor)");
                        }
                        count++;
                    }
                    System.out.println();
                }
            }
        }
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("END OF ALERT REPORT");
        System.out.println("=".repeat(60) + "\n");
        
        // Display usage information
        displayUsageInfo();
    }
    
    private static void displayUsageInfo() {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║                 ALERT SYSTEM INFORMATION                   ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝\n");
        
        System.out.println("📋 SYSTEM CONFIGURATION:");
        System.out.println("   • Minimum Stock Threshold: 3 units");
        System.out.println("   • Minimum Days Between Donations: 56 days");
        System.out.println("   • Automatic Alert Trigger: Stock < 3 units\n");
        
        System.out.println("🔍 HOW IT WORKS:");
        System.out.println("   1. System checks blood inventory for all blood groups");
        System.out.println("   2. Identifies blood groups with stock below 3 units");
        System.out.println("   3. Queries database for eligible donors:");
        System.out.println("      - Same blood group as the shortage");
        System.out.println("      - Haven't donated in the last 56 days");
        System.out.println("      - OR never donated before (new donors)");
        System.out.println("   4. Generates personalized alert messages");
        System.out.println("   5. Logs all alerts to system logs\n");
        
        System.out.println("📞 ALERT MESSAGE FORMAT:");
        System.out.println("   'URGENT: Dear [Name], we critically need [Blood Group]");
        System.out.println("   blood. Current stock: [X] units. Your donation can save");
        System.out.println("   lives! Please visit our blood bank at your earliest");
        System.out.println("   convenience.'\n");
        
        System.out.println("💡 INTEGRATION METHODS:");
        System.out.println("   • UrgentAlert.checkLowInventoryAndAlertDonors()");
        System.out.println("     - Full alert system with donor notifications");
        System.out.println("   • UrgentAlert.getAlertReport()");
        System.out.println("     - Get Map<BloodGroup, List<Donor>> of alerts");
        System.out.println("   • UrgentAlert.checkLowInventory()");
        System.out.println("     - Simple inventory check (legacy method)\n");
        
        System.out.println("🔄 SUGGESTED AUTOMATION:");
        System.out.println("   • Run alert check after each donation");
        System.out.println("   • Schedule periodic checks (e.g., daily at 9 AM)");
        System.out.println("   • Trigger on inventory update");
        System.out.println("   • Add to GUI 'Check Alerts' button\n");
        
        System.out.println("📊 DATABASE QUERY:");
        System.out.println("   SELECT * FROM Donors WHERE BloodGroup = ?");
        System.out.println("   AND (LastDonationDate IS NULL OR");
        System.out.println("        DATEDIFF(CURDATE(), LastDonationDate) >= 56)\n");
    }
}
