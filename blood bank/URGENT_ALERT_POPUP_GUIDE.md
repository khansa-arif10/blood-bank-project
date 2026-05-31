# 🚨 Urgent Alert Popup Notification System

## Overview
The Blood Bank Management System now includes an automatic urgent alert notification system that alerts users when blood inventory levels are critically low (below 3 units).

---

## ✨ Features

### 1. **Automatic Popup on Login** 🔔
- When you login, the system automatically checks blood inventory levels
- If any blood group has **less than 3 units**, an urgent alert popup appears
- The popup displays all critical blood groups with their current quantities
- Appears after a 300ms delay to allow UI to render smoothly

### 2. **Persistent Red Indicator** 🔴
- Located in the **top navigation bar** (top-right corner)
- Shows: `🔴 X URGENT` where X is the number of critical blood groups
- **Always visible** when there are critical alerts, even after dismissing the popup
- Clicking the indicator opens the detailed alert view
- Tooltip shows which blood groups are critical

### 3. **Smart Behavior**
- **First Login**: Popup appears automatically if there are critical alerts
- **After Dismissing**: Popup won't show again until you logout and login
- **Red Indicator**: Remains visible as a reminder until inventory is replenished
- **Real-time Updates**: Indicator updates automatically after donations are recorded
- **Per Session**: Alert state resets when you logout

---

## 🎯 How It Works

### When You Login:
1. System checks inventory for all 8 blood groups (A+, A-, B+, B-, AB+, AB-, O+, O-)
2. Identifies blood groups with **quantity < 3 units**
3. If critical groups found:
   - Shows popup with details
   - Displays red indicator in navbar

### The Popup Shows:
```
🚨 CRITICAL BLOOD SHORTAGE
Immediate action required

The following blood groups are critically low (<3 units):

🔴 O+ Blood Group        1 units
🔴 AB- Blood Group       0 units
🔴 B+ Blood Group        2 units

Please contact eligible donors or organize an urgent donation camp.

[View Alert Details]  [Dismiss]
```

### Red Indicator:
- **Text**: `🔴 2 URGENT` (example for 2 critical blood groups)
- **Color**: Red background (#DC3545)
- **Border**: Dark red outline
- **Location**: Top-right navbar, before username
- **Clickable**: Opens the full alert details dialog

---

## 🖱️ User Actions

### Option 1: View Alert Details
- Click **"View Alert Details"** button in popup
- Opens the comprehensive alert system dialog
- Shows all inventory levels, warnings, and critical alerts
- Marks popup as dismissed for this session

### Option 2: Dismiss
- Click **"Dismiss"** button
- Closes the popup
- **Red indicator remains visible** as a reminder
- Popup won't appear again until you logout and login

### Option 3: Click Red Indicator
- Click the red `🔴 X URGENT` badge in navbar
- Opens the full alert details dialog
- Available anytime after dismissing popup

---

## 📊 Alert Thresholds

| Level | Quantity | Indicator | Action |
|-------|----------|-----------|--------|
| **Critical** | < 3 units | 🔴 Red popup & badge | Immediate action required |
| **Low** | 3-9 units | 🟠 Warning in alert dialog | Consider organizing donation camp |
| **Normal** | ≥ 10 units | ✅ Green status | No action needed |

---

## 🔄 Real-Time Updates

The system updates the alert indicator automatically when:
- ✅ **New donation recorded**: Inventory increases, critical alerts may be resolved
- ✅ **Blood request fulfilled**: Inventory decreases, new alerts may appear
- ✅ **Manual inventory changes**: System detects changes immediately

### After Recording a Donation:
1. Donation is saved to database
2. Blood is added to inventory
3. System rechecks all blood groups
4. Red indicator updates automatically:
   - If blood group is now above 3 units, it's removed from alert list
   - Badge count decreases (e.g., `🔴 2 URGENT` → `🔴 1 URGENT`)
   - If no critical groups remain, badge disappears ✅

---

## 🧪 Testing the Feature

### Test 1: Low Inventory Alert
```sql
-- Set some blood groups to critical levels
UPDATE BloodInventory SET Quantity = 1 WHERE BloodGroup = 'O+';
UPDATE BloodInventory SET Quantity = 0 WHERE BloodGroup = 'AB-';
UPDATE BloodInventory SET Quantity = 2 WHERE BloodGroup = 'B+';
```

**Expected Result:**
- Login → Popup appears showing 3 critical blood groups
- Navbar shows: `🔴 3 URGENT`

### Test 2: Dismiss Behavior
1. Login (popup appears)
2. Click **"Dismiss"**
3. Popup closes
4. Red indicator still visible: `🔴 3 URGENT`
5. Navigate around application → indicator persists
6. Logout and login again → popup appears again

### Test 3: Recording Donation Updates Alert
1. Login with critical alerts present
2. Dismiss the popup
3. Record a donation for O+ blood (450ml)
4. After success message:
   - Red indicator updates automatically
   - Count decreases if O+ is now ≥ 3 units
   - `🔴 3 URGENT` → `🔴 2 URGENT`

### Test 4: No Critical Alerts
```sql
-- Set all blood groups to normal levels
UPDATE BloodInventory SET Quantity = 500;
```

**Expected Result:**
- Login → No popup appears
- No red indicator in navbar
- System is in normal state ✅

---

## 🎨 Visual Design

### Popup Dialog:
- **Size**: 550x400 pixels
- **Header**: Red background (#DC3545) with white text
- **Title**: "🚨 CRITICAL BLOOD SHORTAGE"
- **Subtitle**: "Immediate action required"
- **Content**: White background with light red cards for each blood group
- **Buttons**: 
  - "View Alert Details" (outlined red)
  - "Dismiss" (solid red)

### Red Indicator Badge:
- **Background**: Red (#DC3545)
- **Text**: White, bold, 12pt
- **Border**: Dark red (#C82131), 2px
- **Padding**: 8px vertical, 15px horizontal
- **Cursor**: Hand pointer (clickable)
- **Animation**: None (static, professional)

---

## 🔧 Technical Details

### Class Variables Added:
```java
private boolean alertDismissed = false;
private JLabel urgentAlertIndicator = null;
private java.util.List<String> criticalBloodGroups = new java.util.ArrayList<>();
```

### Methods Added:
1. **`checkAndShowUrgentAlertPopup()`** - Main method called on dashboard load
2. **`updateAlertIndicator()`** - Updates the red badge visibility and text
3. **`showUrgentAlertPopup()`** - Creates and displays the popup dialog

### Integration Points:
- **openDashboard()**: Calls `checkAndShowUrgentAlertPopup()` after 300ms delay
- **createNavbar()**: Creates and adds `urgentAlertIndicator` to navbar
- **logout()**: Resets alert state (`alertDismissed = false`, clears list)
- **Donation recording**: Updates alert indicator after inventory change

---

## 💡 Usage Tips

### For Administrators:
- Check the alert immediately after login
- Click "View Alert Details" to see full inventory status
- Use the alert system to identify which donors to contact
- Organize urgent donation camps for critical blood groups

### For Staff/Technicians:
- Record donations promptly to resolve critical alerts
- Monitor the red indicator throughout your shift
- Click the badge anytime to check current status
- Alert will automatically update after donations

### For All Users:
- **Don't ignore the red badge** - it means urgent action is needed
- If you dismiss the popup, you can always click the badge to see details
- The alert reappears on every login until inventory is replenished
- System provides real-time feedback on inventory changes

---

## 🚀 Benefits

1. ✅ **Immediate Awareness**: Users are instantly notified of critical shortages
2. ✅ **Persistent Reminder**: Red badge ensures alerts aren't forgotten
3. ✅ **Non-Intrusive**: Popup can be dismissed, badge remains visible
4. ✅ **Real-Time Updates**: Automatically reflects inventory changes
5. ✅ **Professional Design**: Clear, urgent visual communication
6. ✅ **Action-Oriented**: Direct link to detailed alert management

---

## 📝 System Requirements

- Java 17+ (JDK 24 recommended)
- MySQL database with BloodInventory table
- iText PDF library (for reports)
- Windows PowerShell (for running)

---

## 🔄 Version History

**Version 1.0** (Current)
- ✅ Automatic popup on login with critical alerts
- ✅ Persistent red indicator badge in navbar
- ✅ Real-time updates after donations
- ✅ Session-based dismissal tracking
- ✅ Click-to-view detailed alerts
- ✅ Clean, professional UI design

---

## 📞 Support

If the alert system is not working:

1. **Check Database Connection**: Ensure MySQL server is running
2. **Verify Inventory Data**: Run `SELECT * FROM BloodInventory`
3. **Check Console**: Look for error messages in terminal
4. **Review Logs**: Check `resources/logs/system.log`
5. **Restart Application**: Logout and login again

---

**✅ Feature Ready for Production Use!**

The urgent alert notification system is fully integrated and tested. Login to see it in action! 🚨
