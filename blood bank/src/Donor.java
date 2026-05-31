

import java.sql.Date;

public class Donor {
    private int donorID;
    private String fullName;
    private String gender;
    private int age;
    private String bloodGroup;
    private String phone;
    private Date lastDonationDate;
    
    public Donor() {}
    
    public Donor(int donorID, String fullName, String gender, int age, String bloodGroup, String phone, Date lastDonationDate) {
        this.donorID = donorID;
        this.fullName = fullName;
        this.gender = gender;
        this.age = age;
        this.bloodGroup = bloodGroup;
        this.phone = phone;
        this.lastDonationDate = lastDonationDate;
    }
    
    // Getters and Setters
    public int getDonorID() { return donorID; }
    public void setDonorID(int donorID) { this.donorID = donorID; }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    
    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public Date getLastDonationDate() { return lastDonationDate; }
    public void setLastDonationDate(Date lastDonationDate) { this.lastDonationDate = lastDonationDate; }
    
    @Override
    public String toString() {
        return "Donor{ID=" + donorID + ", Name='" + fullName + "', BloodGroup='" + bloodGroup + "', Phone='" + phone + "'}";
    }
}