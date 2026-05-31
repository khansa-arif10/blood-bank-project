

import java.sql.*;
import java.util.List;

public class BloodBankService {
    private DonorDAO donorDAO;
    private DonationDAO donationDAO;
    
    public BloodBankService() {
        this.donorDAO = new DonorDAO();
        this.donationDAO = new DonationDAO();
    }
    
    public boolean registerDonor(String fullName, String gender, int age, String bloodGroup, String phone) {
        Donor donor = new Donor();
        donor.setFullName(fullName);
        donor.setGender(gender);
        donor.setAge(age);
        donor.setBloodGroup(bloodGroup);
        donor.setPhone(phone);
        donor.setLastDonationDate(null);
        
        return donorDAO.addDonor(donor);
    }
    
    public boolean recordDonation(int donorID, Date donationDate, int collectedBy) {
        BloodDonation donation = new BloodDonation();
        donation.setDonorID(donorID);
        donation.setDonationDate(donationDate);
        donation.setCollectedBy(collectedBy);
        
        boolean success = donationDAO.addDonation(donation);
        
        if (success) {
            // Get donor info
            Donor donor = donorDAO.getDonorByID(donorID);
            if (donor != null) {
                // Update donor's last donation date
                donor.setLastDonationDate(donationDate);
                donorDAO.updateDonor(donor);
                
                // Add blood to inventory (standard donation is 450ml)
                BloodInventoryService inventoryService = new BloodInventoryService();
                int donationID = donation.getDonationID();
                boolean inventoryAdded = inventoryService.addToInventory(donationID, donor.getBloodGroup(), 450);
                
                if (inventoryAdded) {
                    Logger.info("Added 450ml of " + donor.getBloodGroup() + " to inventory");
                } else {
                    Logger.warning("Donation recorded but inventory update failed");
                }
            }
            SystemLog.logAction(collectedBy, "Donation Recorded", "Donation from Donor ID: " + donorID);
        }
        
        return success;
    }
    
    public List<Donor> getAllDonors() {
        return donorDAO.getAllDonors();
    }
    
    public List<Donor> searchDonorsByBloodGroup(String bloodGroup) {
        return donorDAO.searchDonorsByBloodGroup(bloodGroup);
    }
    
    public List<BloodDonation> getAllDonations() {
        return donationDAO.getAllDonations();
    }
    
    public List<BloodDonation> getDonationsByDonor(int donorID) {
        return donationDAO.getDonationsByDonor(donorID);
    }
    
    public Donor getDonorByID(int donorID) {
        return donorDAO.getDonorByID(donorID);
    }
}