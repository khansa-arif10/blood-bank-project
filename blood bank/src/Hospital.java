

public class Hospital {
    private int hospitalID;
    private String hospitalName;
    private String address;
    private String status; // Approved, Unapproved, Pending Approval
    
    public Hospital() {}
    
    public Hospital(int hospitalID, String hospitalName, String address, String status) {
        this.hospitalID = hospitalID;
        this.hospitalName = hospitalName;
        this.address = address;
        this.status = status;
    }
    
    // Getters and Setters
    public int getHospitalID() { return hospitalID; }
    public void setHospitalID(int hospitalID) { this.hospitalID = hospitalID; }
    
    public String getHospitalName() { return hospitalName; }
    public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    @Override
    public String toString() {
        return "Hospital{ID=" + hospitalID + ", Name='" + hospitalName + "', Status='" + status + "'}";
    }
}