# Urgent Alert System - Implementation Documentation

## Overview
The Urgent Alert System automatically monitors blood inventory levels and notifies eligible donors when stock falls below the threshold of 3 units.

## Features Implemented

### 1. **Automatic Inventory Monitoring**
- Checks all 8 blood groups (A+, A-, B+, B-, AB+, AB-, O+, O-)
- Triggers alerts when units drop below 3
- Critical alert for 0 units, Warning for 1-2 units

### 2. **Intelligent Donor Selection**
- Identifies donors with matching blood group
- Filters donors who haven't donated in last 56 days
- Includes new donors who have never donated

### 3. **Personalized Alert Messages**
- Generates custom messages with donor name
- Includes current stock level
- Provides urgency context

### 4. **Comprehensive Logging**
- All alerts logged to system logs
- Tracks timestamp, donor info, blood group
- Maintains audit trail

## Code Structure

### New Methods in DonorDAO.java
```java
public List<Donor> getEligibleDonorsForAlert(String bloodGroup, int daysSinceLastDonation)
```
- **Purpose**: Query database for eligible donors
- **SQL**: Uses DATEDIFF to check last donation date
- **Returns**: List of eligible donors for specific blood group

### Enhanced UrgentAlert.java

#### Main Method:
```java
public static void checkLowInventoryAndAlertDonors()
```
- **Purpose**: Complete alert workflow
- **Process**:
  1. Check inventory levels
  2. Find eligible donors
  3. Generate personalized messages
  4. Log all notifications
  5. Display summary report

#### Helper Methods:
```java
private static AlertNotification sendAlertToDonor(Donor, String, int)
```
- Creates alert notification object
- Formats personalized message
- Logs the alert

```java
public static Map<String, List<Donor>> getAlertReport()
```
- Returns detailed report of all pending alerts
- Useful for GUI integration

### New Class: AlertNotification
```java
class AlertNotification
```
- Stores alert details
- Fields: donorID, donorName, phone, bloodGroup, message, timestamp
- Immutable data structure for logging

## Database Query

### SQL Used:
```sql
SELECT * FROM Donors 
WHERE BloodGroup = ? 
AND (LastDonationDate IS NULL OR DATEDIFF(CURDATE(), LastDonationDate) >= ?)
```

**Explanation**:
- Selects donors with matching blood group
- Includes donors with NULL last donation (new donors)
- Includes donors who donated 56+ days ago
- Uses MySQL DATEDIFF function

## Integration Guide

### 1. Command Line Usage
```java
// Simple check
UrgentAlert.checkLowInventory();

// Full alert system
UrgentAlert.checkLowInventoryAndAlertDonors();

// Get report for processing
Map<String, List<Donor>> report = UrgentAlert.getAlertReport();
```

### 2. GUI Integration (BloodBankGUI.java)

#### Add Button to Admin Dashboard:
```java
JButton alertButton = createStyledButton("🔔 Check Urgent Alerts");
alertButton.addActionListener(e -> showUrgentAlerts());
```

#### Implementation Method:
```java
private void showUrgentAlerts() {
    JDialog dialog = new JDialog(this, "Urgent Alerts", true);
    dialog.setSize(900, 700);
    
    JTextArea alertArea = new JTextArea();
    alertArea.setEditable(false);
    alertArea.setFont(new Font("Consolas", Font.PLAIN, 12));
    
    // Redirect System.out to capture alert output
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(baos);
    PrintStream old = System.out;
    System.setOut(ps);
    
    UrgentAlert.checkLowInventoryAndAlertDonors();
    
    System.out.flush();
    System.setOut(old);
    alertArea.setText(baos.toString());
    
    dialog.add(new JScrollPane(alertArea));
    dialog.setLocationRelativeTo(this);
    dialog.setVisible(true);
}
```

#### Alternative: Display in Table Format:
```java
private void openAlertManagement() {
    JDialog dialog = new JDialog(this, "Alert Management", true);
    dialog.setSize(1000, 700);
    
    Map<String, List<Donor>> alertReport = UrgentAlert.getAlertReport();
    
    String[] columns = {"Blood Group", "Stock", "Eligible Donors", "Action"};
    DefaultTableModel model = new DefaultTableModel(columns, 0);
    
    BloodInventoryService invService = new BloodInventoryService();
    Map<String, Integer> inventory = invService.getInventorySummary();
    
    for (Map.Entry<String, List<Donor>> entry : alertReport.entrySet()) {
        String bg = entry.getKey();
        List<Donor> donors = entry.getValue();
        int stock = inventory.getOrDefault(bg, 0);
        
        model.addRow(new Object[]{
            bg,
            stock + " units",
            donors.size() + " donors",
            "View Details"
        });
    }
    
    JTable table = new JTable(model);
    dialog.add(new JScrollPane(table), BorderLayout.CENTER);
    
    JButton sendAlertsBtn = new JButton("Send All Alerts");
    sendAlertsBtn.addActionListener(e -> {
        UrgentAlert.checkLowInventoryAndAlertDonors();
        JOptionPane.showMessageDialog(dialog, "Alerts sent successfully!");
        dialog.dispose();
    });
    
    JPanel buttonPanel = new JPanel();
    buttonPanel.add(sendAlertsBtn);
    dialog.add(buttonPanel, BorderLayout.SOUTH);
    
    dialog.setLocationRelativeTo(this);
    dialog.setVisible(true);
}
```

### 3. Automatic Scheduling

#### Option A: After Donation
```java
// In record donation method
if (donationRecorded) {
    // Update inventory
    inventoryService.updateInventory(...);
    
    // Check and send alerts
    new Thread(() -> {
        UrgentAlert.checkLowInventoryAndAlertDonors();
    }).start();
}
```

#### Option B: Scheduled Task
```java
// Run every day at 9 AM
Timer timer = new Timer(true);
timer.scheduleAtFixedRate(new TimerTask() {
    @Override
    public void run() {
        UrgentAlert.checkLowInventoryAndAlertDonors();
    }
}, getScheduledTime(), 24 * 60 * 60 * 1000); // Daily
```

## Configuration

### Constants in UrgentAlert.java:
```java
private static final int MINIMUM_UNITS_THRESHOLD = 3;
private static final int MINIMUM_DAYS_BETWEEN_DONATIONS = 56;
```

**Customization**:
- Change `MINIMUM_UNITS_THRESHOLD` to adjust alert trigger
- Change `MINIMUM_DAYS_BETWEEN_DONATIONS` based on medical guidelines
- Default 56 days = 8 weeks (standard donation interval)

## Testing

### Test Program: TestUrgentAlerts.java
```bash
javac -encoding UTF-8 -cp "libs\*" -d bin src\*.java
java -cp "bin;libs\*" TestUrgentAlerts
```

### Test Scenarios:
1. **Low Stock**: Add donors, reduce inventory to < 3 units
2. **Recent Donors**: Add donation within 56 days - should be excluded
3. **Eligible Donors**: Add donors with old/no donations - should be included
4. **No Donors**: Test blood groups with no registered donors

## Output Example

```
========== URGENT ALERT SYSTEM ==========
─────────────────────────────────────────
🚨 CRITICAL: A+ Blood Group
   Current Stock: 0 units
   Required: 3 units minimum
─────────────────────────────────────────

📞 Eligible Donors for A+: 1
   (Haven't donated in last 56 days)

   Sending alerts to eligible donors:

   ✓ Liam Carter
     Phone: 0300-1001001
     Last Donation: 588 days ago
     Message: URGENT: Dear Liam Carter, we critically need A+ blood...

📊 ALERT SUMMARY
==========================================
Total Alerts Sent: 4
==========================================
```

## Future Enhancements

### 1. SMS Integration
```java
// Add SMS service
private static void sendSMS(String phone, String message) {
    // Integrate with SMS API (Twilio, etc.)
    TwilioAPI.sendMessage(phone, message);
}
```

### 2. Email Notifications
```java
// Add email service
private static void sendEmail(String email, String subject, String body) {
    // Use JavaMail API
    EmailService.send(email, subject, body);
}
```

### 3. Notification History Table
```sql
CREATE TABLE AlertNotifications (
    NotificationID INT PRIMARY KEY AUTO_INCREMENT,
    DonorID INT,
    BloodGroup VARCHAR(5),
    Message TEXT,
    SentDate DATETIME,
    Status VARCHAR(20),
    FOREIGN KEY (DonorID) REFERENCES Donors(DonorID)
);
```

### 4. Response Tracking
- Track donor responses
- Mark alerts as acknowledged
- Schedule follow-up reminders

## Maintenance

### Log File Location:
Check `resources/logs/system.log` for:
- Alert triggers
- Donor notifications
- System errors

### Performance Considerations:
- Database indexes on `BloodGroup` and `LastDonationDate`
- Connection pooling for multiple alerts
- Async notification sending

## Support

For issues or enhancements:
1. Check system logs
2. Verify database connectivity
3. Ensure donors have valid phone numbers
4. Test SQL query independently

---
**Version**: 1.0  
**Date**: December 10, 2025  
**Author**: Blood Bank Management System Team
