-- ============================================================
-- DROP DATABASE AND CREATE FRESH
-- ============================================================
DROP DATABASE IF EXISTS bloodbankdb;
CREATE DATABASE bloodbankdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE bloodbankdb;

-- ============================================================
-- 1) USERS
-- ============================================================
CREATE TABLE Users (
    UserID INT AUTO_INCREMENT PRIMARY KEY,
    Username VARCHAR(50) UNIQUE NOT NULL,
    Password VARCHAR(255) NOT NULL,
    FullName VARCHAR(100) NOT NULL,
    Role ENUM('Admin','Technician','Staff') NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 2) DONORS
-- ============================================================
CREATE TABLE Donors (
    DonorID INT AUTO_INCREMENT PRIMARY KEY,
    FullName VARCHAR(100) NOT NULL,
    Gender ENUM('Male','Female','Other'),
    Age INT,
    BloodGroup ENUM('A+','A-','B+','B-','AB+','O+','O-'),
    Phone VARCHAR(20),
    LastDonationDate DATE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 3) DONATIONS
-- ============================================================
CREATE TABLE Donations (
    DonationID INT AUTO_INCREMENT PRIMARY KEY,
    DonorID INT NOT NULL,
    DonationDate DATE NOT NULL,
    CollectedBy INT NOT NULL,
    FOREIGN KEY (DonorID) REFERENCES Donors(DonorID) ON DELETE CASCADE,
    FOREIGN KEY (CollectedBy) REFERENCES Users(UserID) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 4) HOSPITALS
-- ============================================================
CREATE TABLE Hospitals (
    HospitalID INT AUTO_INCREMENT PRIMARY KEY,
    HospitalName VARCHAR(100) NOT NULL,
    Address VARCHAR(255),
    Status ENUM('Approved','Unapproved','Pending Approval') DEFAULT 'Pending Approval'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 5) PATIENTS
-- ============================================================
CREATE TABLE Patients (
    PatientID INT AUTO_INCREMENT PRIMARY KEY,
    FullName VARCHAR(100),
    BloodGroup ENUM('A+','A-','B+','B-','AB+','O+','O-'),
    HospitalID INT,
    FOREIGN KEY (HospitalID) REFERENCES Hospitals(HospitalID) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 6) BLOOD REQUESTS
-- ============================================================
CREATE TABLE BloodRequests (
    RequestID INT AUTO_INCREMENT PRIMARY KEY,
    PatientID INT NOT NULL,
    RequestedBloodGroup ENUM('A+','A-','B+','B-','AB+','O+','O-'),
    Quantity INT NOT NULL,
    RequestDate DATE NOT NULL,
    Status ENUM('Pending','Fulfilled','Rejected') DEFAULT 'Pending',
    FOREIGN KEY (PatientID) REFERENCES Patients(PatientID) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 7) BLOOD INVENTORY
-- ============================================================
CREATE TABLE BloodInventory (
    InventoryID INT AUTO_INCREMENT PRIMARY KEY,
    DonationID INT NOT NULL UNIQUE,
    BloodGroup ENUM('A+','A-','B+','B-','AB+','O+','O-'),
    Quantity INT DEFAULT 1,
    AddedDate DATE NOT NULL,
    FOREIGN KEY (DonationID) REFERENCES Donations(DonationID) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 8) FAILED BLOOD
-- ============================================================
CREATE TABLE FailedBlood (
    FailedID INT AUTO_INCREMENT PRIMARY KEY,
    DonationID INT NOT NULL,
    BloodGroup VARCHAR(10),
    Reason TEXT NOT NULL,
    FailedDate DATE NOT NULL,
    FOREIGN KEY (DonationID) REFERENCES Donations(DonationID) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 9) SYSTEM LOG
-- ============================================================
CREATE TABLE SystemLog (
    LogID INT AUTO_INCREMENT PRIMARY KEY,
    UserID INT,
    ActionType VARCHAR(100),
    Description TEXT,
    ActionTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (UserID) REFERENCES Users(UserID) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 10) BLOOD TEST
-- ============================================================
CREATE TABLE BloodTest (
    TestID INT AUTO_INCREMENT PRIMARY KEY,
    DonationID INT NOT NULL,
    TestedBy INT NULL,
    TestDate DATE,
    HepatitisA ENUM('Positive','Negative') DEFAULT NULL,
    HepatitisB ENUM('Positive','Negative') DEFAULT NULL,
    HepatitisC ENUM('Positive','Negative') DEFAULT NULL,
    HIV ENUM('Positive','Negative') DEFAULT NULL,
    Syphilis ENUM('Positive','Negative') DEFAULT NULL,
    FinalStatus ENUM('Pending','Passed','Failed') DEFAULT 'Pending',
    FOREIGN KEY (DonationID) REFERENCES Donations(DonationID) ON DELETE CASCADE,
    FOREIGN KEY (TestedBy) REFERENCES Users(UserID) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 11) BLOOD FULFILLMENT
-- ============================================================
CREATE TABLE BloodFulfillment (
    FulfillmentID INT AUTO_INCREMENT PRIMARY KEY,
    RequestID INT NOT NULL,
    InventoryID INT NOT NULL,
    FulfilledBy INT NOT NULL,
    FulfillmentDate DATE NOT NULL,
    QuantityUsed INT NOT NULL,
    FOREIGN KEY (RequestID) REFERENCES BloodRequests(RequestID),
    FOREIGN KEY (InventoryID) REFERENCES BloodInventory(InventoryID),
    FOREIGN KEY (FulfilledBy) REFERENCES Users(UserID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TRIGGERS
-- ============================================================
DELIMITER $$

-- 1) Create BloodTest automatically after Donation
CREATE TRIGGER trg_create_blood_test
AFTER INSERT ON Donations
FOR EACH ROW
BEGIN
    INSERT INTO BloodTest (DonationID, FinalStatus)
    VALUES (NEW.DonationID, 'Pending');

    INSERT INTO SystemLog (UserID, ActionType, Description)
    VALUES (NEW.CollectedBy, 'Donation Created',
           CONCAT('Donation ', NEW.DonationID, ' recorded.'));
END$$

-- 2) Process BloodTest after update
CREATE TRIGGER trg_process_blood_test
AFTER UPDATE ON BloodTest
FOR EACH ROW
BEGIN
    IF (NEW.HepatitisA = 'Positive'
       OR NEW.HepatitisB = 'Positive'
       OR NEW.HepatitisC = 'Positive'
       OR NEW.HIV = 'Positive'
       OR NEW.Syphilis = 'Positive') THEN

        IF NEW.FinalStatus <> 'Failed' THEN
            UPDATE BloodTest
            SET FinalStatus = 'Failed'
            WHERE TestID = NEW.TestID;
        END IF;

        IF NOT EXISTS (SELECT 1 FROM FailedBlood WHERE DonationID = NEW.DonationID) THEN
            INSERT INTO FailedBlood (DonationID, Reason, FailedDate)
            VALUES (NEW.DonationID, 'Positive disease marker', CURDATE());
        END IF;

        INSERT INTO SystemLog (UserID, ActionType, Description)
        VALUES (NEW.TestedBy, 'Blood Test Failed',
                CONCAT('Donation ', NEW.DonationID, ' failed testing.'));

    ELSEIF NEW.FinalStatus = 'Passed' THEN
        IF NOT EXISTS (SELECT 1 FROM BloodInventory WHERE DonationID = NEW.DonationID) THEN
            INSERT INTO BloodInventory (DonationID, BloodGroup, AddedDate)
            SELECT d.DonationID, dn.BloodGroup, CURDATE()
            FROM Donations d
            JOIN Donors dn ON dn.DonorID = d.DonorID
            WHERE d.DonationID = NEW.DonationID;
        END IF;

        INSERT INTO SystemLog (UserID, ActionType, Description)
        VALUES (NEW.TestedBy, 'Blood Test Passed',
                CONCAT('Donation ', NEW.DonationID, ' added to inventory.'));
    END IF;
END$$

-- 3) Ensure only approved hospitals can make blood requests
CREATE TRIGGER trg_approved_hospital_request
BEFORE INSERT ON BloodRequests
FOR EACH ROW
BEGIN
    DECLARE hosp_status ENUM('Approved','Unapproved','Pending Approval');

    SELECT h.Status INTO hosp_status
    FROM Patients p
    JOIN Hospitals h ON p.HospitalID = h.HospitalID
    WHERE p.PatientID = NEW.PatientID;

    IF hosp_status <> 'Approved' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Blood request denied: Hospital is not approved';
    END IF;
END$$

DELIMITER ;

-- ============================================================
-- 10) FAILED BLOOD
-- ============================================================
CREATE TABLE FailedBlood (
    FailedID INT AUTO_INCREMENT PRIMARY KEY,
    DonationID INT NOT NULL,
    BloodGroup VARCHAR(5) NOT NULL,
    Reason VARCHAR(50) NOT NULL, -- 'Test Failed' or 'Expired'
    FailedDate DATE NOT NULL,
    FOREIGN KEY (DonationID) REFERENCES Donations(DonationID) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- SAMPLE DATA (10–12 entries)
-- ============================================================

-- Users
INSERT INTO Users (Username, Password, FullName, Role) VALUES
('admin1','pass','Aisha Ahmed','Admin'),
('tech1','pass','Bilal Ali','Technician'),
('staff1','pass','Sami Shah','Staff'),
('tech2','pass','Hina Mir','Technician'),
('staff2','pass','Zoya Iqbal','Staff');

-- Donors
INSERT INTO Donors (FullName, Gender, Age, BloodGroup, Phone, LastDonationDate) VALUES
('Liam Carter','Male',32,'A+','0300-1001001','2024-05-01'),
('Noah Brown','Male',28,'O+','0300-1001002','2024-04-20'),
('Oliver Smith','Male',35,'B+','0300-1001003','2024-03-15');

-- Hospitals
INSERT INTO Hospitals (HospitalName, Address, Status) VALUES
('City Hospital','Downtown, Block A','Approved'),
('Green Valley Clinic','West End, Lane 3','Pending Approval'),
('Metro Medical Center','Central Ave','Approved');

-- Patients
INSERT INTO Patients (FullName, BloodGroup, HospitalID) VALUES
('Tom Hanks','A+',1),
('Mia Wong','O+',2),
('John Clark','B+',3);

-- Donations
INSERT INTO Donations (DonorID, DonationDate, CollectedBy) VALUES
(1,'2025-01-01',2),
(2,'2025-01-02',2),
(3,'2025-01-03',3);

-- Blood Requests (will fail for non-approved hospitals)
INSERT INTO BloodRequests (PatientID, RequestedBloodGroup, Quantity, RequestDate) VALUES
(1,'A+',2,'2025-01-05'); -- This works (hospital 1 is Approved)
-- INSERT INTO BloodRequests (PatientID, RequestedBloodGroup, Quantity, RequestDate) VALUES (2,'O+',1,'2025-01-06'); -- Will fail

