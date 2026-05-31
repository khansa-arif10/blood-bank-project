

import java.sql.*;
import java.util.List;
import java.util.Scanner;

public class CodeMain {
    private static Scanner scanner = new Scanner(System.in);
    private static User currentUser = null;
    private static AuthService authService = new AuthService();
    private static BloodBankService bloodBankService = new BloodBankService();
    private static HospitalService hospitalService = new HospitalService();
    private static BloodInventoryService inventoryService = new BloodInventoryService();
    private static BloodRequestService requestService = new BloodRequestService();
    private static AdminService adminService = new AdminService();
    private static StaffService staffService = new StaffService();
    
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║    BLOOD BANK MANAGEMENT SYSTEM        ║");
        System.out.println("╚════════════════════════════════════════╝\n");
        
        // Test database connection
        if (DBConnection.getConnection() == null) {
            System.out.println("Failed to connect to database. Exiting...");
            return;
        }
        
        // Fix schema if needed
        SchemaFixer.fixFailedBloodTable();
        TriggerFixer.fixTriggers();
        
        loginMenu();
    }
    
    private static void loginMenu() {
        while (true) {
            System.out.println("\n=== LOGIN ===");
            System.out.println("1. Login");
            System.out.println("2. Exit");
            System.out.print("Choice: ");
            
            int choice = getIntInput();
            
            switch (choice) {
                case 1:
                    login();
                    break;
                case 2:
                    System.out.println("Thank you for using Blood Bank Management System!");
                    DBConnection.closeConnection();
                    System.exit(0);
                default:
                    System.out.println("Invalid choice!");
            }
        }
    }
    
    private static void login() {
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        
        currentUser = authService.login(username, password);
        
        if (currentUser != null) {
            System.out.println("\n✓ Login successful! Welcome, " + currentUser.getFullName());
            redirectToRoleMenu();
        } else {
            System.out.println("\n✗ Invalid credentials!");
        }
    }
    
    private static void redirectToRoleMenu() {
        String role = currentUser.getRole();
        
        switch (role) {
            case "Admin":
                adminMenu();
                break;
            case "Technician":
                technicianMenu();
                break;
            case "Staff":
                staffMenu();
                break;
            default:
                System.out.println("Unknown role!");
        }
    }
    
    private static void adminMenu() {
        while (true) {
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║          ADMIN DASHBOARD               ║");
            System.out.println("╚════════════════════════════════════════╝");
            System.out.println("1. Manage Users");
            System.out.println("2. Manage Hospitals");
            System.out.println("3. View Blood Inventory");
            System.out.println("4. View Blood Requests");
            System.out.println("5. View System Logs");
            System.out.println("6. Generate Report");
            System.out.println("7. Check Urgent Alerts");
            System.out.println("8. Logout");
            System.out.print("Choice: ");
            
            int choice = getIntInput();
            
            switch (choice) {
                case 1:
                    manageUsers();
                    break;
                case 2:
                    manageHospitals();
                    break;
                case 3:
                    viewInventory();
                    break;
                case 4:
                    viewBloodRequests();
                    break;
                case 5:
                    viewSystemLogs();
                    break;
                case 6:
                    adminService.generateReport();
                    break;
                case 7:
                    UrgentAlert.checkLowInventory();
                    break;
                case 8:
                    logout();
                    return;
                default:
                    System.out.println("Invalid choice!");
            }
        }
    }
    
    private static void technicianMenu() {
        while (true) {
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║        TECHNICIAN DASHBOARD            ║");
            System.out.println("╚════════════════════════════════════════╝");
            System.out.println("1. Register Donor");
            System.out.println("2. Record Donation");
            System.out.println("3. View All Donors");
            System.out.println("4. Search Donors by Blood Group");
            System.out.println("5. View Blood Inventory");
            System.out.println("6. Logout");
            System.out.print("Choice: ");
            
            int choice = getIntInput();
            
            switch (choice) {
                case 1:
                    registerDonor();
                    break;
                case 2:
                    recordDonation();
                    break;
                case 3:
                    viewAllDonors();
                    break;
                case 4:
                    searchDonorsByBloodGroup();
                    break;
                case 5:
                    viewInventory();
                    break;
                case 6:
                    logout();
                    return;
                default:
                    System.out.println("Invalid choice!");
            }
        }
    }
    
    private static void staffMenu() {
        while (true) {
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║           STAFF DASHBOARD              ║");
            System.out.println("╚════════════════════════════════════════╝");
            System.out.println("1. Process Blood Tests");
            System.out.println("2. View Pending Tests");
            System.out.println("3. Manage Blood Requests");
            System.out.println("4. View Blood Inventory");
            System.out.println("5. Register Hospital");
            System.out.println("6. Add Patient");
            System.out.println("7. Logout");
            System.out.print("Choice: ");
            
            int choice = getIntInput();
            
            switch (choice) {
                case 1:
                    processBloodTest();
                    break;
                case 2:
                    viewPendingTests();
                    break;
                case 3:
                    manageBloodRequests();
                    break;
                case 4:
                    viewInventory();
                    break;
                case 5:
                    registerHospital();
                    break;
                case 6:
                    addPatient();
                    break;
                case 7:
                    logout();
                    return;
                default:
                    System.out.println("Invalid choice!");
            }
        }
    }
    
    // ==================== USER MANAGEMENT ====================
    
    private static void manageUsers() {
        System.out.println("\n=== Manage Users ===");
        System.out.println("1. Add User");
        System.out.println("2. View All Users");
        System.out.println("3. Delete User");
        System.out.println("4. Back");
        System.out.print("Choice: ");
        
        int choice = getIntInput();
        
        switch (choice) {
            case 1:
                addUser();
                break;
            case 2:
                viewAllUsers();
                break;
            case 3:
                deleteUser();
                break;
            case 4:
                return;
        }
    }
    
    private static void addUser() {
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        System.out.print("Full Name: ");
        String fullName = scanner.nextLine();
        System.out.print("Role (Admin/Technician/Staff): ");
        String role = scanner.nextLine();
        
        if (adminService.createUser(username, password, fullName, role)) {
            System.out.println("✓ User added successfully!");
        } else {
            System.out.println("✗ Failed to add user!");
        }
    }
    
    private static void viewAllUsers() {
        List<User> users = adminService.getAllUsers();
        System.out.println("\n=== All Users ===");
        for (User user : users) {
            System.out.println(user);
        }
    }
    
    private static void deleteUser() {
        System.out.print("Enter User ID to delete: ");
        int userID = getIntInput();
        
        if (adminService.deleteUser(userID)) {
            System.out.println("✓ User deleted successfully!");
        } else {
            System.out.println("✗ Failed to delete user!");
        }
    }
    
    // ==================== HOSPITAL MANAGEMENT ====================
    
    private static void manageHospitals() {
        System.out.println("\n=== Manage Hospitals ===");
        System.out.println("1. View Pending Approvals");
        System.out.println("2. View All Hospitals");
        System.out.println("3. Approve Hospital");
        System.out.println("4. Reject Hospital");
        System.out.println("5. Back");
        System.out.print("Choice: ");
        
        int choice = getIntInput();
        
        switch (choice) {
            case 1:
                viewPendingHospitals();
                break;
            case 2:
                viewAllHospitals();
                break;
            case 3:
                approveHospital();
                break;
            case 4:
                rejectHospital();
                break;
            case 5:
                return;
        }
    }
    
    private static void viewPendingHospitals() {
        List<Hospital> hospitals = adminService.getPendingHospitals();
        System.out.println("\n=== Pending Hospital Approvals ===");
        if (hospitals.isEmpty()) {
            System.out.println("No pending hospitals.");
        } else {
            for (Hospital hospital : hospitals) {
                System.out.println(hospital);
            }
        }
    }
    
    private static void viewAllHospitals() {
        List<Hospital> hospitals = hospitalService.getAllHospitals();
        System.out.println("\n=== All Hospitals ===");
        for (Hospital hospital : hospitals) {
            System.out.println(hospital);
        }
    }
    
    private static void approveHospital() {
        System.out.print("Enter Hospital ID to approve: ");
        int hospitalID = getIntInput();
        
        if (adminService.approveHospital(hospitalID, currentUser.getUserID())) {
            System.out.println("✓ Hospital approved successfully!");
        } else {
            System.out.println("✗ Failed to approve hospital!");
        }
    }
    
    private static void rejectHospital() {
        System.out.print("Enter Hospital ID to reject: ");
        int hospitalID = getIntInput();
        
        if (adminService.rejectHospital(hospitalID, currentUser.getUserID())) {
            System.out.println("✓ Hospital rejected!");
        } else {
            System.out.println("✗ Failed to reject hospital!");
        }
    }
    
    private static void registerHospital() {
        System.out.print("Hospital Name: ");
        String name = scanner.nextLine();
        System.out.print("Address: ");
        String address = scanner.nextLine();
        
        if (hospitalService.registerHospital(name, address)) {
            System.out.println("✓ Hospital registered! Awaiting approval.");
        } else {
            System.out.println("✗ Failed to register hospital!");
        }
    }
    
    // ==================== DONOR MANAGEMENT ====================
    
    private static void registerDonor() {
        System.out.print("Full Name: ");
        String name = scanner.nextLine();
        System.out.print("Gender (Male/Female/Other): ");
        String gender = scanner.nextLine();
        System.out.print("Age: ");
        int age = getIntInput();
        System.out.print("Blood Group (A+/A-/B+/B-/AB+/AB-/O+/O-): ");
        String bloodGroup = scanner.nextLine();
        System.out.print("Phone: ");
        String phone = scanner.nextLine();
        
        if (bloodBankService.registerDonor(name, gender, age, bloodGroup, phone)) {
            System.out.println("✓ Donor registered successfully!");
        } else {
            System.out.println("✗ Failed to register donor!");
        }
    }
    
    private static void recordDonation() {
        System.out.print("Donor ID: ");
        int donorID = getIntInput();
        
        // Check if donor exists
        Donor donor = bloodBankService.getDonorByID(donorID);
        if (donor == null) {
            System.out.println("✗ Donor not found!");
            return;
        }
        
        System.out.println("Recording donation for: " + donor.getFullName());
        Date donationDate = new Date(System.currentTimeMillis());
        
        if (bloodBankService.recordDonation(donorID, donationDate, currentUser.getUserID())) {
            System.out.println("✓ Donation recorded successfully!");
            System.out.println("A blood test entry has been created automatically.");
        } else {
            System.out.println("✗ Failed to record donation!");
        }
    }
    
    private static void viewAllDonors() {
        List<Donor> donors = bloodBankService.getAllDonors();
        System.out.println("\n=== All Donors ===");
        for (Donor donor : donors) {
            System.out.println(donor);
        }
    }
    
    private static void searchDonorsByBloodGroup() {
        System.out.print("Blood Group: ");
        String bloodGroup = scanner.nextLine();
        
        List<Donor> donors = bloodBankService.searchDonorsByBloodGroup(bloodGroup);
        System.out.println("\n=== Donors with Blood Group " + bloodGroup + " ===");
        if (donors.isEmpty()) {
            System.out.println("No donors found.");
        } else {
            for (Donor donor : donors) {
                System.out.println(donor);
            }
        }
    }
    
    // ==================== BLOOD TEST MANAGEMENT ====================
    
    private static void processBloodTest() {
        System.out.print("Donation ID: ");
        int donationID = getIntInput();
        
        System.out.println("Enter test results (Positive/Negative):");
        System.out.print("Hepatitis A: ");
        String hepA = scanner.nextLine();
        System.out.print("Hepatitis B: ");
        String hepB = scanner.nextLine();
        System.out.print("Hepatitis C: ");
        String hepC = scanner.nextLine();
        System.out.print("HIV: ");
        String hiv = scanner.nextLine();
        System.out.print("Syphilis: ");
        String syphilis = scanner.nextLine();
        
        String result = staffService.processBloodTest(donationID, currentUser.getUserID(), hepA, hepB, hepC, hiv, syphilis);
        
        if (result == null) {
            String status = staffService.getTestStatus(donationID);
            System.out.println("✓ Blood test processed!");
            System.out.println("Status: " + status);
            
            if ("Passed".equals(status)) {
                System.out.println("Blood has been added to inventory!");
            } else if ("Failed".equals(status)) {
                System.out.println("Blood failed testing and has been marked as failed.");
            }
        } else {
            System.out.println("✗ Failed to process blood test! Error: " + result);
        }
    }
    
    private static void viewPendingTests() {
        System.out.println("\n=== Pending Blood Tests ===");
        ResultSet rs = staffService.getPendingTests();
        
        if (rs != null) {
            try {
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    System.out.println("Test ID: " + rs.getInt("TestID") + 
                                     " | Donation ID: " + rs.getInt("DonationID") +
                                     " | Donor: " + rs.getString("FullName") +
                                     " | Status: " + rs.getString("FinalStatus"));
                }
                if (!found) {
                    System.out.println("No pending tests.");
                }
            } catch (SQLException e) {
                Logger.error("Error displaying pending tests: " + e.getMessage());
            }
        }
    }
    
    // ==================== INVENTORY MANAGEMENT ====================
    
    private static void viewInventory() {
        System.out.println("\n=== Blood Inventory ===");
        var inventory = inventoryService.getInventorySummary();
        
        if (inventory.isEmpty()) {
            System.out.println("No blood in inventory.");
        } else {
            String[] bloodGroups = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
            for (String bg : bloodGroups) {
                int quantity = inventory.getOrDefault(bg, 0);
                System.out.println(bg + ": " + quantity + " units");
            }
        }
    }
    
    // ==================== BLOOD REQUEST MANAGEMENT ====================
    
    private static void addPatient() {
        System.out.print("Patient Name: ");
        String name = scanner.nextLine();
        System.out.print("Blood Group: ");
        String bloodGroup = scanner.nextLine();
        System.out.print("Hospital ID: ");
        int hospitalID = getIntInput();
        
        if (hospitalService.addPatient(name, bloodGroup, hospitalID)) {
            System.out.println("✓ Patient added successfully!");
        } else {
            System.out.println("✗ Failed to add patient!");
        }
    }
    
    private static void manageBloodRequests() {
        System.out.println("\n=== Manage Blood Requests ===");
        System.out.println("1. Create Request");
        System.out.println("2. View Pending Requests");
        System.out.println("3. Fulfill Request");
        System.out.println("4. Reject Request");
        System.out.println("5. Back");
        System.out.print("Choice: ");
        
        int choice = getIntInput();
        
        switch (choice) {
            case 1:
                createBloodRequest();
                break;
            case 2:
                viewPendingRequests();
                break;
            case 3:
                fulfillRequest();
                break;
            case 4:
                rejectRequest();
                break;
            case 5:
                return;
        }
    }
    
    private static void createBloodRequest() {
        System.out.print("Patient ID: ");
        int patientID = getIntInput();
        System.out.print("Blood Group: ");
        String bloodGroup = scanner.nextLine();
        System.out.print("Quantity: ");
        int quantity = getIntInput();
        
        Date requestDate = new Date(System.currentTimeMillis());
        
        if (requestService.createRequest(patientID, bloodGroup, quantity, requestDate)) {
            System.out.println("✓ Blood request created successfully!");
        } else {
            System.out.println("✗ Failed to create request! Check if hospital is approved.");
        }
    }
    
    private static void viewPendingRequests() {
        List<BloodRequest> requests = requestService.getPendingRequests();
        System.out.println("\n=== Pending Blood Requests ===");
        
        if (requests.isEmpty()) {
            System.out.println("No pending requests.");
        } else {
            for (BloodRequest request : requests) {
                System.out.println(request);
            }
        }
    }
    
    private static void fulfillRequest() {
        System.out.print("Request ID to fulfill: ");
        int requestID = getIntInput();
        
        if (requestService.fulfillRequest(requestID, currentUser.getUserID())) {
            System.out.println("✓ Request fulfilled successfully!");
        } else {
            System.out.println("✗ Failed to fulfill request!");
        }
    }
    
    private static void rejectRequest() {
        System.out.print("Request ID to reject: ");
        int requestID = getIntInput();
        
        if (requestService.rejectRequest(requestID, currentUser.getUserID())) {
            System.out.println("✓ Request rejected!");
        } else {
            System.out.println("✗ Failed to reject request!");
        }
    }
    
    private static void viewBloodRequests() {
        List<BloodRequest> requests = requestService.getAllRequests();
        System.out.println("\n=== All Blood Requests ===");
        
        if (requests.isEmpty()) {
            System.out.println("No requests found.");
        } else {
            for (BloodRequest request : requests) {
                System.out.println(request);
            }
        }
    }
    
    // ==================== SYSTEM LOGS ====================
    
    private static void viewSystemLogs() {
        System.out.println("\n=== System Logs (Last 50) ===");
        ResultSet rs = adminService.getSystemLogs();
        
        if (rs != null) {
            try {
                while (rs.next()) {
                    System.out.println("[" + rs.getTimestamp("ActionTime") + "] " +
                                     rs.getString("ActionType") + " by " + 
                                     (rs.getString("FullName") != null ? rs.getString("FullName") : "System") +
                                     ": " + rs.getString("Description"));
                }
            } catch (SQLException e) {
                Logger.error("Error displaying logs: " + e.getMessage());
            }
        }
    }
    
    // ==================== UTILITY METHODS ====================
    
    private static void logout() {
        System.out.println("Logging out...");
        SystemLog.logAction(currentUser.getUserID(), "Logout", "User " + currentUser.getUsername() + " logged out");
        currentUser = null;
    }
    
    private static int getIntInput() {
        while (true) {
            try {
                String input = scanner.nextLine();
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.print("Invalid input. Please enter a number: ");
            }
        }
    }
}