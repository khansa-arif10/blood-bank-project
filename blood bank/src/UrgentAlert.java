

import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UrgentAlert {
    
    private static final int MINIMUM_UNITS_THRESHOLD = 3;
    private static final int MINIMUM_DAYS_BETWEEN_DONATIONS = 56;
    
    /**
     * Comprehensive alert system that checks inventory and notifies eligible donors
     */
    public static void checkLowInventoryAndAlertDonors() {
        BloodInventoryService inventoryService = new BloodInventoryService();
        DonorDAO donorDAO = new DonorDAO();
        Map<String, Integer> inventory = inventoryService.getInventorySummary();
        
        String[] bloodGroups = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        
        System.out.println("\n========== URGENT ALERT SYSTEM ==========");
        System.out.println("Checking inventory levels and donor eligibility...");
        System.out.println("==========================================\n");
        
        List<AlertNotification> notifications = new ArrayList<>();
        boolean hasAlert = false;
        
        for (String bloodGroup : bloodGroups) {
            int quantity = inventory.getOrDefault(bloodGroup, 0);
            
            if (quantity < MINIMUM_UNITS_THRESHOLD) {
                hasAlert = true;
                String severity = quantity == 0 ? "CRITICAL" : "WARNING";
                
                System.out.println("─────────────────────────────────────────");
                System.out.println("🚨 " + severity + ": " + bloodGroup + " Blood Group");
                System.out.println("   Current Stock: " + quantity + " units");
                System.out.println("   Required: " + MINIMUM_UNITS_THRESHOLD + " units minimum");
                System.out.println("─────────────────────────────────────────");
                
                Logger.warning(severity + " inventory alert: " + bloodGroup + " - " + quantity + " units");
                
                // Find eligible donors
                List<Donor> eligibleDonors = donorDAO.getEligibleDonorsForAlert(
                    bloodGroup, MINIMUM_DAYS_BETWEEN_DONATIONS
                );
                
                System.out.println("\n📞 Eligible Donors for " + bloodGroup + ": " + eligibleDonors.size());
                System.out.println("   (Haven't donated in last " + MINIMUM_DAYS_BETWEEN_DONATIONS + " days)\n");
                
                if (eligibleDonors.isEmpty()) {
                    System.out.println("   ⚠️  No eligible donors available at this time.");
                    Logger.warning("No eligible donors found for " + bloodGroup);
                } else {
                    System.out.println("   Sending alerts to eligible donors:\n");
                    
                    for (Donor donor : eligibleDonors) {
                        AlertNotification notification = sendAlertToDonor(donor, bloodGroup, quantity);
                        notifications.add(notification);
                        
                        // Display donor alert info
                        System.out.println("   ✓ " + donor.getFullName());
                        System.out.println("     Phone: " + donor.getPhone());
                        
                        Date lastDonation = donor.getLastDonationDate();
                        if (lastDonation != null) {
                            long daysSince = ChronoUnit.DAYS.between(
                                lastDonation.toLocalDate(), 
                                LocalDate.now()
                            );
                            System.out.println("     Last Donation: " + daysSince + " days ago");
                        } else {
                            System.out.println("     Last Donation: Never donated");
                        }
                        System.out.println("     Message: " + notification.getMessage());
                        System.out.println();
                    }
                    
                    Logger.info("Sent " + eligibleDonors.size() + " alerts for " + bloodGroup);
                }
                
                System.out.println("─────────────────────────────────────────\n");
            }
        }
        
        if (!hasAlert) {
            System.out.println("✓ All blood groups have adequate stock.");
            System.out.println("  No alerts needed at this time.\n");
        } else {
            System.out.println("\n📊 ALERT SUMMARY");
            System.out.println("==========================================");
            System.out.println("Total Alerts Sent: " + notifications.size());
            System.out.println("==========================================\n");
            
            // Log all notifications
            logAlertNotifications(notifications);
        }
    }
    
    /**
     * Legacy method for simple inventory checking
     */
    public static void checkLowInventory() {
        BloodInventoryService inventoryService = new BloodInventoryService();
        var inventory = inventoryService.getInventorySummary();
        
        String[] bloodGroups = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        
        System.out.println("\n========== URGENT ALERTS ==========");
        boolean hasAlert = false;
        
        for (String bloodGroup : bloodGroups) {
            int quantity = inventory.getOrDefault(bloodGroup, 0);
            
            if (quantity == 0) {
                System.out.println("🚨 CRITICAL: " + bloodGroup + " - OUT OF STOCK!");
                Logger.warning("Critical inventory alert: " + bloodGroup + " out of stock");
                hasAlert = true;
            } else if (quantity < 3) {
                System.out.println("⚠️  WARNING: " + bloodGroup + " - Low stock (" + quantity + " units)");
                Logger.warning("Low inventory alert: " + bloodGroup + " - " + quantity + " units");
                hasAlert = true;
            }
        }
        
        if (!hasAlert) {
            System.out.println("✓ All blood groups have adequate stock.");
        }
        
        System.out.println("===================================\n");
    }
    
    /**
     * Send alert notification to a donor
     */
    private static AlertNotification sendAlertToDonor(Donor donor, String bloodGroup, int currentStock) {
        String message = String.format(
            "URGENT: Dear %s, we critically need %s blood. Current stock: %d units. " +
            "Your donation can save lives! Please visit our blood bank at your earliest convenience. " +
            "Contact: Blood Bank Management System. Thank you for being a life saver!",
            donor.getFullName(), bloodGroup, currentStock
        );
        
        AlertNotification notification = new AlertNotification(
            donor.getDonorID(),
            donor.getFullName(),
            donor.getPhone(),
            bloodGroup,
            message,
            new java.util.Date()
        );
        
        // In a real system, this would send SMS/Email
        // For now, we log it
        Logger.info("Alert sent to " + donor.getFullName() + " (" + donor.getPhone() + ") for " + bloodGroup);
        
        return notification;
    }
    
    /**
     * Log all alert notifications to the system log
     */
    private static void logAlertNotifications(List<AlertNotification> notifications) {
        for (AlertNotification notification : notifications) {
            Logger.info(String.format(
                "Alert: Donor=%s, Phone=%s, BloodGroup=%s, Time=%s",
                notification.getDonorName(),
                notification.getPhone(),
                notification.getBloodGroup(),
                notification.getTimestamp()
            ));
        }
    }
    
    /**
     * Get detailed alert report
     */
    public static Map<String, List<Donor>> getAlertReport() {
        BloodInventoryService inventoryService = new BloodInventoryService();
        DonorDAO donorDAO = new DonorDAO();
        Map<String, Integer> inventory = inventoryService.getInventorySummary();
        Map<String, List<Donor>> alertReport = new HashMap<>();
        
        String[] bloodGroups = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        
        for (String bloodGroup : bloodGroups) {
            int quantity = inventory.getOrDefault(bloodGroup, 0);
            
            if (quantity < MINIMUM_UNITS_THRESHOLD) {
                List<Donor> eligibleDonors = donorDAO.getEligibleDonorsForAlert(
                    bloodGroup, MINIMUM_DAYS_BETWEEN_DONATIONS
                );
                alertReport.put(bloodGroup, eligibleDonors);
            }
        }
        
        return alertReport;
    }
}

/**
 * Class to represent an alert notification
 */
class AlertNotification {
    private int donorID;
    private String donorName;
    private String phone;
    private String bloodGroup;
    private String message;
    private java.util.Date timestamp;
    
    public AlertNotification(int donorID, String donorName, String phone, 
                           String bloodGroup, String message, java.util.Date timestamp) {
        this.donorID = donorID;
        this.donorName = donorName;
        this.phone = phone;
        this.bloodGroup = bloodGroup;
        this.message = message;
        this.timestamp = timestamp;
    }
    
    public int getDonorID() { return donorID; }
    public String getDonorName() { return donorName; }
    public String getPhone() { return phone; }
    public String getBloodGroup() { return bloodGroup; }
    public String getMessage() { return message; }
    public java.util.Date getTimestamp() { return timestamp; }
}