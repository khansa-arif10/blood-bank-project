# 🎯 Quick Start Guide - Urgent Alert Popup

## What You'll See When Blood Is Low

### 1️⃣ Login Screen
```
┌─────────────────────────────────────────┐
│  💉 One Drop, One Life                  │
│                                         │
│  Username: [admin]                      │
│  Password: [•••••]                      │
│                                         │
│         [Login]                         │
└─────────────────────────────────────────┘
```

### 2️⃣ Popup Appears (if blood < 3 units)
```
┌────────────────────────────────────────────────┐
│ ┌────────────────────────────────────────────┐ │
│ │  🚨 CRITICAL BLOOD SHORTAGE                │ │
│ │  Immediate action required                 │ │
│ └────────────────────────────────────────────┘ │
│                                                │
│  The following blood groups are critically     │
│  low (<3 units):                              │
│                                                │
│  ╔═══════════════════════════════════════╗   │
│  ║ 🔴 O+ Blood Group      |    1 units   ║   │
│  ╚═══════════════════════════════════════╝   │
│                                                │
│  ╔═══════════════════════════════════════╗   │
│  ║ 🔴 AB- Blood Group     |    0 units   ║   │
│  ╚═══════════════════════════════════════╝   │
│                                                │
│  ╔═══════════════════════════════════════╗   │
│  ║ 🔴 B+ Blood Group      |    2 units   ║   │
│  ╚═══════════════════════════════════════╝   │
│                                                │
│  Please contact eligible donors or organize   │
│  an urgent donation camp.                     │
│                                                │
│  ────────────────────────────────────────────  │
│         [View Alert Details]  [Dismiss]       │
└────────────────────────────────────────────────┘
```

### 3️⃣ Dashboard with Red Indicator
```
┌─────────────────────────────────────────────────────────────┐
│  💉 One Drop, One Life    [🔴 3 URGENT] John Doe (Admin) [Logout] │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  Admin Dashboard                                            │
│                                                             │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐     │
│  │   👥     │ │   🏥     │ │   🩸     │ │   📋     │     │
│  │  Users   │ │Hospitals │ │Inventory │ │ Requests │     │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘     │
│                                                             │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐     │
│  │   📊     │ │   📈     │ │   ⚠️     │ │   ⚙️     │     │
│  │   Logs   │ │ Reports  │ │  Alerts  │ │ Settings │     │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘     │
│                                                             │
└─────────────────────────────────────────────────────────────┘
        ↑
   Red badge is always visible when there are critical alerts
```

---

## 🔴 Red Indicator Badge Details

**Location**: Top-right corner of navbar, before your name

**Appearance**:
```
┌──────────────┐
│ 🔴 3 URGENT  │  ← Red background, white text, bold
└──────────────┘
```

**What the number means**:
- `🔴 1 URGENT` = 1 blood group is critical (< 3 units)
- `🔴 3 URGENT` = 3 blood groups are critical
- `🔴 8 URGENT` = All blood groups are critical (emergency!)

**Hover tooltip**:
```
┌─────────────────────────────────────┐
│ Critical blood shortage: O+, AB-, B+ │
└─────────────────────────────────────┘
```

---

## 🖱️ What Happens When You Click

### Click "View Alert Details":
Opens the full alert system with detailed inventory:

```
┌────────────────────────────────────────────────┐
│  ⚠️ System Alerts & Warnings                   │
├────────────────────────────────────────────────┤
│                                                │
│  🔴 CRITICAL: O+ Blood Group                   │
│  Only 1 units available. Immediate action!     │
│                                                │
│  🔴 CRITICAL: AB- Blood Group                  │
│  Only 0 units available. Immediate action!     │
│                                                │
│  🟠 WARNING: A+ Blood Group                    │
│  Low stock: 7 units. Consider donation camp.   │
│                                                │
│  ✅ All other blood groups have sufficient stock│
│                                                │
│  ────────────────────────────────────────────  │
│  Critical: 2  |  Warnings: 1                   │
└────────────────────────────────────────────────┘
```

### Click "Dismiss":
- Popup closes
- Red badge stays visible
- You can continue working
- Popup won't appear again until you logout

### Click Red Badge (🔴 3 URGENT):
- Opens the same detailed alert view as above
- Available anytime you need to check
- Badge is always clickable

---

## ✅ What Happens After Recording a Donation

**Before Donation**:
```
Navbar: [🔴 3 URGENT] John Doe (Admin)
Critical: O+ (1), AB- (0), B+ (2)
```

**Record 450ml O+ donation** ✓

**After Donation**:
```
Navbar: [🔴 2 URGENT] John Doe (Admin)  ← Count decreased!
Critical: AB- (0), B+ (2)  ← O+ removed (now has 451ml)
```

**Record another 450ml O+ donation** ✓

```
Navbar: [🔴 2 URGENT] John Doe (Admin)  ← Same (O+ already removed)
Critical: AB- (0), B+ (2)
```

**Record 2000ml AB- donations** ✓

```
Navbar: [🔴 1 URGENT] John Doe (Admin)  ← Count decreased again!
Critical: B+ (2)  ← Only B+ remains
```

**Record 1000ml B+ donations** ✓

```
Navbar: No badge (disappeared!)  ← All resolved! ✅
Critical: None
```

---

## 📊 Quick Reference - Alert States

| Inventory Level | What You See | What To Do |
|----------------|--------------|------------|
| All blood ≥ 10 units | No popup, no badge ✅ | Normal operations |
| Some 3-9 units | No popup, warning in alert dialog 🟠 | Monitor, plan donation camp |
| Any < 3 units | Popup + Red badge 🔴 | **URGENT ACTION NEEDED** |
| Multiple < 3 units | Popup + `🔴 X URGENT` badge | **CRITICAL - Immediate action** |

---

## 🎬 Typical Workflow

### Morning Routine:
1. **Login** → Popup appears (if critical)
2. **Read critical blood groups** → Note which ones need urgent attention
3. **Click "View Alert Details"** → See full inventory status
4. **Take action**:
   - Contact donors for critical blood groups
   - Organize urgent donation camp
   - Check pending requests for those blood groups

### Throughout the Day:
5. **Monitor red badge** → Always visible as reminder
6. **Record donations** → Badge updates automatically
7. **Click badge anytime** → Check current status
8. **Watch badge count decrease** → Progress indicator

### End of Shift:
9. **Check if badge still visible** → Update next shift
10. **Logout** → Alert state resets

### Next Login:
11. **Popup appears again if still critical** → Ongoing reminder
12. **Continue working to resolve** → Until all blood ≥ 3 units

---

## 🚦 Color Guide

| Color | Icon | Meaning | Urgency |
|-------|------|---------|---------|
| 🔴 Red | 🔴 | Critical (< 3 units) | **URGENT** - Act immediately |
| 🟠 Orange | 🟠 | Warning (3-9 units) | **SOON** - Plan donation camp |
| ✅ Green | ✅ | Normal (≥ 10 units) | **OK** - No action needed |

---

## 💡 Pro Tips

1. **Don't dismiss immediately** - Read which blood groups are critical
2. **Use the badge as a todo list** - Badge count = items to resolve
3. **Click badge frequently** - Check progress throughout the day
4. **Record donations promptly** - Watch badge update in real-time
5. **Aim for badge to disappear** - That means all blood groups are safe!

---

## ❓ Common Questions

**Q: Why does the popup appear every time I login?**
A: It's a reminder that critical action is still needed. It will stop appearing once all blood groups have ≥ 3 units.

**Q: Can I turn off the popup?**
A: Yes, click "Dismiss". The red badge will remain as a visual reminder.

**Q: What if I want to see the alerts again after dismissing?**
A: Click the red `🔴 X URGENT` badge in the navbar, or click the "Alerts" card on the dashboard.

**Q: When does the badge disappear?**
A: When all blood groups have at least 3 units in inventory.

**Q: Does the badge update automatically?**
A: Yes! After recording donations, the badge updates immediately to reflect new inventory levels.

**Q: What happens when I logout?**
A: The dismissal state resets. Next login, the popup will appear again if there are still critical alerts.

---

## 🎯 Success Indicators

You're doing great when:
- ✅ Red badge count decreases over time
- ✅ Badge disappears completely
- ✅ Popup shows fewer critical blood groups
- ✅ Alert dialog shows more green checkmarks

**Goal**: No red badge = All blood groups safe! 🎉

---

**Ready to use? Login and see it in action!** 🚀
