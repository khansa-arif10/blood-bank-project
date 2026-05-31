

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    
    public User authenticateUser(String username, String password) {
        String query = "SELECT * FROM Users WHERE Username = ? AND Password = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return new User(
                    rs.getInt("UserID"),
                    rs.getString("Username"),
                    rs.getString("Password"),
                    rs.getString("FullName"),
                    rs.getString("Role")
                );
            }
        } catch (SQLException e) {
            Logger.error("Authentication failed: " + e.getMessage());
        }
        return null;
    }
    
    public boolean addUser(User user) {
        String query = "INSERT INTO Users (Username, Password, FullName, Role) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getFullName());
            stmt.setString(4, user.getRole());
            
            int rows = stmt.executeUpdate();
            Logger.info("User added: " + user.getUsername());
            return rows > 0;
        } catch (SQLException e) {
            Logger.error("Failed to add user: " + e.getMessage());
            return false;
        }
    }
    
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM Users";
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                users.add(new User(
                    rs.getInt("UserID"),
                    rs.getString("Username"),
                    rs.getString("Password"),
                    rs.getString("FullName"),
                    rs.getString("Role")
                ));
            }
        } catch (SQLException e) {
            Logger.error("Failed to retrieve users: " + e.getMessage());
        }
        return users;
    }
    
    public boolean updateUser(User user) {
        String query = "UPDATE Users SET Password = ?, FullName = ?, Role = ? WHERE UserID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, user.getPassword());
            stmt.setString(2, user.getFullName());
            stmt.setString(3, user.getRole());
            stmt.setInt(4, user.getUserID());
            
            int rows = stmt.executeUpdate();
            Logger.info("User updated: " + user.getUsername());
            return rows > 0;
        } catch (SQLException e) {
            Logger.error("Failed to update user: " + e.getMessage());
            return false;
        }
    }
    
    public boolean deleteUser(int userID) {
        String query = "DELETE FROM Users WHERE UserID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, userID);
            int rows = stmt.executeUpdate();
            Logger.info("User deleted: ID " + userID);
            return rows > 0;
        } catch (SQLException e) {
            Logger.error("Failed to delete user: " + e.getMessage());
            return false;
        }
    }
}