

import java.sql.Date;

public class BloodRequest {
    private int requestID;
    private int patientID;
    private String requestedBloodGroup;
    private int quantity;
    private Date requestDate;
    private String status; // Pending, Fulfilled, Rejected
    
    public BloodRequest() {}
    
    public BloodRequest(int requestID, int patientID, String requestedBloodGroup, int quantity, Date requestDate, String status) {
        this.requestID = requestID;
        this.patientID = patientID;
        this.requestedBloodGroup = requestedBloodGroup;
        this.quantity = quantity;
        this.requestDate = requestDate;
        this.status = status;
    }
    
    // Getters and Setters
    public int getRequestID() { return requestID; }
    public void setRequestID(int requestID) { this.requestID = requestID; }
    
    public int getPatientID() { return patientID; }
    public void setPatientID(int patientID) { this.patientID = patientID; }
    
    public String getRequestedBloodGroup() { return requestedBloodGroup; }
    public void setRequestedBloodGroup(String requestedBloodGroup) { this.requestedBloodGroup = requestedBloodGroup; }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    
    public Date getRequestDate() { return requestDate; }
    public void setRequestDate(Date requestDate) { this.requestDate = requestDate; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    @Override
    public String toString() {
        return "Request{ID=" + requestID + ", PatientID=" + patientID + ", BloodGroup='" + requestedBloodGroup + "', Status='" + status + "'}";
    }
}