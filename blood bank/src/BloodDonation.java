

import java.sql.Date;

public class BloodDonation {
    private int donationID;
    private int donorID;
    private Date donationDate;
    private int quantityML;
    private int collectedBy;
    
    public BloodDonation() {}
    
    public BloodDonation(int donationID, int donorID, Date donationDate, int quantityML, int collectedBy) {
        this.donationID = donationID;
        this.donorID = donorID;
        this.donationDate = donationDate;
        this.quantityML = quantityML;
        this.collectedBy = collectedBy;
    }
    
    // Getters and Setters
    public int getDonationID() { return donationID; }
    public void setDonationID(int donationID) { this.donationID = donationID; }
    
    public int getDonorID() { return donorID; }
    public void setDonorID(int donorID) { this.donorID = donorID; }
    
    public Date getDonationDate() { return donationDate; }
    public void setDonationDate(Date donationDate) { this.donationDate = donationDate; }
    
    public int getQuantityML() { return quantityML; }
    public void setQuantityML(int quantityML) { this.quantityML = quantityML; }
    
    public int getCollectedBy() { return collectedBy; }
    public void setCollectedBy(int collectedBy) { this.collectedBy = collectedBy; }
    
    @Override
    public String toString() {
        return "Donation{ID=" + donationID + ", DonorID=" + donorID + ", Date=" + donationDate + "}";
    }
}