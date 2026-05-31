

public class AuthService {
    private UserDAO userDAO;
    
    public AuthService() {
        this.userDAO = new UserDAO();
    }
    
    public User login(String username, String password) {
        User user = userDAO.authenticateUser(username, password);
        if (user != null) {
            Logger.info("User logged in: " + username);
            SystemLog.logAction(user.getUserID(), "Login", "User " + username + " logged in successfully");
        } else {
            Logger.warning("Failed login attempt for: " + username);
        }
        return user;
    }
    
    public boolean registerUser(String username, String password, String fullName, String role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setFullName(fullName);
        user.setRole(role);
        
        boolean success = userDAO.addUser(user);
        if (success) {
            Logger.info("New user registered: " + username);
        }
        return success;
    }
}