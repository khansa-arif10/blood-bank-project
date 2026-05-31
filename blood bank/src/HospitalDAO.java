

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HospitalDAO {
    
    public boolean addHospital(Hospital hospital) {
        String query = "INSERT INTO Hospitals (HospitalName, Address, Status) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, hospital.getHospitalName());
            stmt.setString(2, hospital.getAddress());
            stmt.setString(3, hospital.getStatus());
            
            int rows = stmt.executeUpdate();
            Logger.info("Hospital added: " + hospital.getHospitalName());
            return rows > 0;
        } catch (SQLException e) {
            Logger.error("Failed to add hospital: " + e.getMessage());
            return false;
        }
    }
    
    public List<Hospital> getAllHospitals() {
        List<Hospital> hospitals = new ArrayList<>();
        String query = "SELECT * FROM Hospitals";
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                hospitals.add(new Hospital(
                    rs.getInt("HospitalID"),
                    rs.getString("HospitalName"),
                    rs.getString("Address"),
                    rs.getString("Status")
                ));
            }
        } catch (SQLException e) {
            Logger.error("Failed to retrieve hospitals: " + e.getMessage());
        }
        return hospitals;
    }
    
    public Hospital getHospitalByID(int hospitalID) {
        String query = "SELECT * FROM Hospitals WHERE HospitalID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, hospitalID);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return new Hospital(
                    rs.getInt("HospitalID"),
                    rs.getString("HospitalName"),
                    rs.getString("Address"),
                    rs.getString("Status")
                );
            }
        } catch (SQLException e) {
            Logger.error("Failed to retrieve hospital: " + e.getMessage());
        }
        return null;
    }
    
    public boolean updateHospitalStatus(int hospitalID, String status) {
        String query = "UPDATE Hospitals SET Status = ? WHERE HospitalID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, status);
            stmt.setInt(2, hospitalID);
            
            int rows = stmt.executeUpdate();
            Logger.info("Hospital status updated: ID " + hospitalID + " to " + status);
            return rows > 0;
        } catch (SQLException e) {
            Logger.error("Failed to update hospital status: " + e.getMessage());
            return false;
        }
    }
    
    public List<Hospital> getHospitalsByStatus(String status) {
        List<Hospital> hospitals = new ArrayList<>();
        String query = "SELECT * FROM Hospitals WHERE Status = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                hospitals.add(new Hospital(
                    rs.getInt("HospitalID"),
                    rs.getString("HospitalName"),
                    rs.getString("Address"),
                    rs.getString("Status")
                ));
            }
        } catch (SQLException e) {
            Logger.error("Failed to retrieve hospitals by status: " + e.getMessage());
        }
        return hospitals;
    }
}