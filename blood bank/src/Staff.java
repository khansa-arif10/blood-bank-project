

import java.sql.Date;

public class Staff extends User {
    private int staffId;
    private String designation;
    private String department;
    private Date joinDate;

    // Default constructor
    public Staff() {
        super();
    }

    // Full constructor - matches the User class structure
    public Staff(int userID, String username, String password, String fullName, String role,
                 int staffId, String designation, String department, Date joinDate) {
        super(userID, username, password, fullName, role);
        this.staffId = staffId;
        this.designation = designation;
        this.department = department;
        this.joinDate = joinDate;
    }

    // Getters
    public int getStaffId() { return staffId; }
    public String getDesignation() { return designation; }
    public String getDepartment() { return department; }
    public Date getJoinDate() { return joinDate; }

    // Setters
    public void setStaffId(int staffId) { this.staffId = staffId; }
    public void setDesignation(String designation) { this.designation = designation; }
    public void setDepartment(String department) { this.department = department; }
    public void setJoinDate(Date joinDate) { this.joinDate = joinDate; }

    @Override
    public String toString() {
        return "Staff{ID=" + staffId + ", Name='" + getFullName() + 
               "', Designation='" + designation + "', Department='" + department + 
               "', Role='" + getRole() + "'}";
    }
}