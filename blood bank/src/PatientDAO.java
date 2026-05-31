

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PatientDAO {
    
    public boolean addPatient(Patient patient) {
        String query = "INSERT INTO Patients (FullName, BloodGroup, HospitalID) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, patient.getFullName());
            stmt.setString(2, patient.getBloodGroup());
            stmt.setInt(3, patient.getHospitalID());
            
            int rows = stmt.executeUpdate();
            Logger.info("Patient added: " + patient.getFullName());
            return rows > 0;
        } catch (SQLException e) {
            Logger.error("Failed to add patient: " + e.getMessage());
            return false;
        }
    }
    
    public List<Patient> getAllPatients() {
        List<Patient> patients = new ArrayList<>();
        String query = "SELECT * FROM Patients";
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                patients.add(new Patient(
                    rs.getInt("PatientID"),
                    rs.getString("FullName"),
                    rs.getString("BloodGroup"),
                    rs.getInt("HospitalID")
                ));
            }
        } catch (SQLException e) {
            Logger.error("Failed to retrieve patients: " + e.getMessage());
        }
        return patients;
    }
    
    public Patient getPatientByID(int patientID) {
        String query = "SELECT * FROM Patients WHERE PatientID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, patientID);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return new Patient(
                    rs.getInt("PatientID"),
                    rs.getString("FullName"),
                    rs.getString("BloodGroup"),
                    rs.getInt("HospitalID")
                );
            }
        } catch (SQLException e) {
            Logger.error("Failed to retrieve patient: " + e.getMessage());
        }
        return null;
    }
    
    public List<Patient> getPatientsByHospital(int hospitalID) {
        List<Patient> patients = new ArrayList<>();
        String query = "SELECT * FROM Patients WHERE HospitalID = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, hospitalID);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                patients.add(new Patient(
                    rs.getInt("PatientID"),
                    rs.getString("FullName"),
                    rs.getString("BloodGroup"),
                    rs.getInt("HospitalID")
                ));
            }
        } catch (SQLException e) {
            Logger.error("Failed to retrieve patients by hospital: " + e.getMessage());
        }
        return patients;
    }
}