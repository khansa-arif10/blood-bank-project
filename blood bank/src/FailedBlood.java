
import java.sql.Date;

public class FailedBlood {
    private int failedID;
    private int donationID;
    private String bloodGroup;
    private String reason; // "Test Failed" or "Expired"
    private Date failedDate;
    
    public FailedBlood() {}
    
    public FailedBlood(int failedID, int donationID, String bloodGroup, String reason, Date failedDate) {
        this.failedID = failedID;
        this.donationID = donationID;
        this.bloodGroup = bloodGroup;
        this.reason = reason;
        this.failedDate = failedDate;
    }
    
    public int getFailedID() { return failedID; }
    public void setFailedID(int failedID) { this.failedID = failedID; }
    
    public int getDonationID() { return donationID; }
    public void setDonationID(int donationID) { this.donationID = donationID; }
    
    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    
    public Date getFailedDate() { return failedDate; }
    public void setFailedDate(Date failedDate) { this.failedDate = failedDate; }
    
    @Override
    public String toString() {
        return "FailedBlood{ID=" + failedID + ", DonationID=" + donationID + ", Reason='" + reason + "'}";
    }
}
