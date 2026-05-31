

import java.util.List;

public class HospitalService {
    private HospitalDAO hospitalDAO;
    private PatientDAO patientDAO;
    
    public HospitalService() {
        this.hospitalDAO = new HospitalDAO();
        this.patientDAO = new PatientDAO();
    }
    
    public boolean registerHospital(String hospitalName, String address) {
        Hospital hospital = new Hospital();
        hospital.setHospitalName(hospitalName);
        hospital.setAddress(address);
        hospital.setStatus("Pending Approval");
        
        boolean success = hospitalDAO.addHospital(hospital);
        if (success) {
            Logger.info("Hospital registered: " + hospitalName);
            SystemLog.logAction(null, "Hospital Registration", "Hospital " + hospitalName + " registered");
        }
        return success;
    }
    
    public boolean approveHospital(int hospitalID, int adminUserID) {
        boolean success = hospitalDAO.updateHospitalStatus(hospitalID, "Approved");
        if (success) {
            Hospital hospital = hospitalDAO.getHospitalByID(hospitalID);
            Logger.info("Hospital approved: " + hospital.getHospitalName());
            SystemLog.logAction(adminUserID, "Hospital Approval", "Hospital ID " + hospitalID + " approved");
        }
        return success;
    }
    
    public boolean rejectHospital(int hospitalID, int adminUserID) {
        boolean success = hospitalDAO.updateHospitalStatus(hospitalID, "Unapproved");
        if (success) {
            Hospital hospital = hospitalDAO.getHospitalByID(hospitalID);
            Logger.info("Hospital rejected: " + hospital.getHospitalName());
            SystemLog.logAction(adminUserID, "Hospital Rejection", "Hospital ID " + hospitalID + " rejected");
        }
        return success;
    }
    
    public List<Hospital> getAllHospitals() {
        return hospitalDAO.getAllHospitals();
    }
    
    public List<Hospital> getPendingHospitals() {
        return hospitalDAO.getHospitalsByStatus("Pending Approval");
    }
    
    public List<Hospital> getApprovedHospitals() {
        return hospitalDAO.getHospitalsByStatus("Approved");
    }
    
    public boolean addPatient(String fullName, String bloodGroup, int hospitalID) {
        Patient patient = new Patient();
        patient.setFullName(fullName);
        patient.setBloodGroup(bloodGroup);
        patient.setHospitalID(hospitalID);
        
        return patientDAO.addPatient(patient);
    }
    
    public List<Patient> getAllPatients() {
        return patientDAO.getAllPatients();
    }
    
    public List<Patient> getPatientsByHospital(int hospitalID) {
        return patientDAO.getPatientsByHospital(hospitalID);
    }
}