

public class User {
    private int userID;
    private String username;
    private String password;
    private String fullName;
    private String role; // Admin, Technician, Staff
    
    public User() {}
    
    public User(int userID, String username, String password, String fullName, String role) {
        this.userID = userID;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
    }
    
    // Getters and Setters
    public int getUserID() { return userID; }
    public void setUserID(int userID) { this.userID = userID; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    @Override
    public String toString() {
        return "User{ID=" + userID + ", Username='" + username + "', Name='" + fullName + "', Role='" + role + "'}";
    }
}