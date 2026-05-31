

import java.sql.Date;

public class BloodInventory {
    private int inventoryID;
    private int donationID;
    private String bloodGroup;
    private int quantity;
    private Date addedDate;
    
    public BloodInventory() {}
    
    public BloodInventory(int inventoryID, int donationID, String bloodGroup, int quantity, Date addedDate) {
        this.inventoryID = inventoryID;
        this.donationID = donationID;
        this.bloodGroup = bloodGroup;
        this.quantity = quantity;
        this.addedDate = addedDate;
    }
    
    // Getters and Setters
    public int getInventoryID() { return inventoryID; }
    public void setInventoryID(int inventoryID) { this.inventoryID = inventoryID; }
    
    public int getDonationID() { return donationID; }
    public void setDonationID(int donationID) { this.donationID = donationID; }
    
    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    
    public Date getAddedDate() { return addedDate; }
    public void setAddedDate(Date addedDate) { this.addedDate = addedDate; }
    
    @Override
    public String toString() {
        return "Inventory{ID=" + inventoryID + ", BloodGroup='" + bloodGroup + "', Quantity=" + quantity + "}";
    }
}