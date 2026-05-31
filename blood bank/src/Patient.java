

public class Patient {
    private int patientID;
    private String fullName;
    private String bloodGroup;
    private int hospitalID;
    
    public Patient() {}
    
    public Patient(int patientID, String fullName, String bloodGroup, int hospitalID) {
        this.patientID = patientID;
        this.fullName = fullName;
        this.bloodGroup = bloodGroup;
        this.hospitalID = hospitalID;
    }
    
    // Getters and Setters
    public int getPatientID() { return patientID; }
    public void setPatientID(int patientID) { this.patientID = patientID; }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }
    
    public int getHospitalID() { return hospitalID; }
    public void setHospitalID(int hospitalID) { this.hospitalID = hospitalID; }
    
    @Override
    public String toString() {
        return "Patient{ID=" + patientID + ", Name='" + fullName + "', BloodGroup='" + bloodGroup + "'}";
    }
}