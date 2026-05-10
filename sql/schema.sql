CREATE DATABASE IF NOT EXISTS gulfcars CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE gulfcars;

-- Drop in dependency-safe order
SET FOREIGN_KEY_CHECKS=0;
DROP TABLE IF EXISTS License, Fine, AccidentReport, InsurancePolicy, FuelLog,
    VehicleMaintenance, Review, OnlinePayment, CardPayment, CashPayment, Payment,
    RentalContract, Booking, VehicleBranchAssignment, Vehicle, Mechanic, RentalAgent,
    Employee, Branch, Supplier, CustomerPromotion, Promotion, Customer;
SET FOREIGN_KEY_CHECKS=1;

CREATE TABLE Customer (
    CustomerID INT PRIMARY KEY AUTO_INCREMENT,
    FirstName VARCHAR(50) NOT NULL DEFAULT 'Unknown',
    LastName VARCHAR(50) NOT NULL DEFAULT 'Unknown',
    PhoneNumber VARCHAR(20) NOT NULL DEFAULT 'N/A',
    Email VARCHAR(100) UNIQUE DEFAULT NULL,
    Address TEXT,
    DateOfBirth DATE DEFAULT NULL,
    LoyaltyPoints INT NOT NULL DEFAULT 0 CHECK (LoyaltyPoints >= 0)
);

CREATE TABLE Promotion (
    PromotionID INT PRIMARY KEY AUTO_INCREMENT,
    Description TEXT,
    DiscountPercentage DECIMAL(5,2) NOT NULL DEFAULT 0.00 CHECK (DiscountPercentage BETWEEN 0 AND 100),
    StartDate DATE NOT NULL DEFAULT (CURRENT_DATE),
    EndDate DATE DEFAULT NULL,
    CHECK (EndDate IS NULL OR EndDate >= StartDate)
);

CREATE TABLE CustomerPromotion (
    CustomerID INT,
    PromotionID INT,
    DateApplied DATE NOT NULL DEFAULT (CURRENT_DATE),
    ValueSaved DECIMAL(10,2) NOT NULL DEFAULT 0.00 CHECK (ValueSaved >= 0),
    PRIMARY KEY (CustomerID, PromotionID),
    FOREIGN KEY (CustomerID) REFERENCES Customer(CustomerID) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (PromotionID) REFERENCES Promotion(PromotionID) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE Supplier (
    SupplierID INT PRIMARY KEY AUTO_INCREMENT,
    CompanyName VARCHAR(100) NOT NULL DEFAULT 'Unknown Supplier',
    ContactPerson VARCHAR(100) NOT NULL DEFAULT 'Unknown',
    PhoneNumber VARCHAR(20) NOT NULL DEFAULT 'N/A',
    Email VARCHAR(100) UNIQUE DEFAULT NULL,
    Address TEXT
);

CREATE TABLE Branch (
    BranchID INT PRIMARY KEY AUTO_INCREMENT,
    BranchName VARCHAR(100) NOT NULL DEFAULT 'Unnamed Branch',
    Location VARCHAR(100) NOT NULL DEFAULT 'Unknown',
    PhoneNumber VARCHAR(20) NOT NULL DEFAULT 'N/A',
    ManagerID INT NULL DEFAULT NULL
);

CREATE TABLE Employee (
    EmployeeID INT PRIMARY KEY AUTO_INCREMENT,
    Name VARCHAR(100) NOT NULL DEFAULT 'Unknown Employee',
    PhoneNumber VARCHAR(20) NOT NULL DEFAULT 'N/A',
    Email VARCHAR(100) UNIQUE DEFAULT NULL,
    HireDate DATE NOT NULL DEFAULT (CURRENT_DATE),
    Salary DECIMAL(10,2) NOT NULL DEFAULT 0.00 CHECK (Salary >= 0),
    BranchID INT NULL DEFAULT NULL,
    EmployeeType VARCHAR(20) NOT NULL DEFAULT 'RentalAgent' CHECK (EmployeeType IN ('RentalAgent','Mechanic')),
    FOREIGN KEY (BranchID) REFERENCES Branch(BranchID) ON DELETE SET NULL ON UPDATE CASCADE
);

CREATE TABLE RentalAgent (
    EmployeeID INT PRIMARY KEY,
    AgentLevel VARCHAR(20) NOT NULL DEFAULT 'Junior' CHECK (AgentLevel IN ('Junior','Intermediate','Senior')),
    CustomerServiceRating DECIMAL(3,2) NOT NULL DEFAULT 0.00 CHECK (CustomerServiceRating BETWEEN 0 AND 5),
    FOREIGN KEY (EmployeeID) REFERENCES Employee(EmployeeID) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE Mechanic (
    EmployeeID INT PRIMARY KEY,
    Specialisation VARCHAR(100) NOT NULL DEFAULT 'General',
    CertificationLevel VARCHAR(50) NOT NULL DEFAULT 'Basic' CHECK (CertificationLevel IN ('Basic','Intermediate','Advanced','Master')),
    FOREIGN KEY (EmployeeID) REFERENCES Employee(EmployeeID) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE Vehicle (
    VehicleID INT PRIMARY KEY AUTO_INCREMENT,
    SupplierID INT NULL DEFAULT NULL,
    Make VARCHAR(50) NOT NULL DEFAULT 'Unknown',
    Model VARCHAR(50) NOT NULL DEFAULT 'Unknown',
    Year INT NOT NULL DEFAULT 2026 CHECK (Year BETWEEN 1990 AND 2035),
    LicensePlate VARCHAR(20) UNIQUE DEFAULT NULL,
    VIN VARCHAR(50) UNIQUE DEFAULT NULL,
    Status VARCHAR(30) NOT NULL DEFAULT 'Available' CHECK (Status IN ('Available','Booked','Rented','Maintenance','Retired')),
    Mileage INT NOT NULL DEFAULT 0 CHECK (Mileage >= 0),
    CategoryType VARCHAR(50) NOT NULL DEFAULT 'Economy' CHECK (CategoryType IN ('Economy','Compact','SUV','Luxury','Van','Truck')),
    FOREIGN KEY (SupplierID) REFERENCES Supplier(SupplierID) ON DELETE SET NULL ON UPDATE CASCADE
);

CREATE TABLE VehicleBranchAssignment (
    VehicleID INT,
    BranchID INT,
    DateAssigned DATE NOT NULL DEFAULT (CURRENT_DATE),
    ReturnDate DATE NULL DEFAULT NULL,
    ReturnBranchID INT NULL DEFAULT NULL,
    PRIMARY KEY (VehicleID, BranchID, DateAssigned),
    FOREIGN KEY (VehicleID) REFERENCES Vehicle(VehicleID) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (BranchID) REFERENCES Branch(BranchID) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (ReturnBranchID) REFERENCES Branch(BranchID) ON DELETE SET NULL ON UPDATE CASCADE,
    CHECK (ReturnDate IS NULL OR ReturnDate >= DateAssigned)
);

CREATE TABLE Booking (
    BookingID INT PRIMARY KEY AUTO_INCREMENT,
    CustomerID INT NOT NULL,
    VehicleID INT NOT NULL,
    PickupDate DATE NOT NULL DEFAULT (CURRENT_DATE),
    ReturnDate DATE NOT NULL,
    BookingStatus VARCHAR(30) NOT NULL DEFAULT 'Pending' CHECK (BookingStatus IN ('Pending','Confirmed','Cancelled','Completed')),
    FOREIGN KEY (CustomerID) REFERENCES Customer(CustomerID) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (VehicleID) REFERENCES Vehicle(VehicleID) ON DELETE RESTRICT ON UPDATE CASCADE,
    CHECK (ReturnDate >= PickupDate)
);

CREATE TABLE RentalContract (
    ContractID INT PRIMARY KEY AUTO_INCREMENT,
    CustomerID INT NOT NULL,
    VehicleID INT NOT NULL,
    EmployeeID INT NULL DEFAULT NULL,
    StartDate DATE NOT NULL DEFAULT (CURRENT_DATE),
    EndDate DATE NOT NULL,
    TotalRentFee DECIMAL(10,2) NOT NULL DEFAULT 0.00 CHECK (TotalRentFee >= 0),
    DepositAmount DECIMAL(10,2) NOT NULL DEFAULT 0.00 CHECK (DepositAmount >= 0),
    ContractStatus VARCHAR(30) NOT NULL DEFAULT 'Active' CHECK (ContractStatus IN ('Active','Completed','Cancelled')),
    FOREIGN KEY (CustomerID) REFERENCES Customer(CustomerID) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (VehicleID) REFERENCES Vehicle(VehicleID) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (EmployeeID) REFERENCES Employee(EmployeeID) ON DELETE SET NULL ON UPDATE CASCADE,
    CHECK (EndDate >= StartDate)
);

CREATE TABLE Payment (
    PaymentID INT PRIMARY KEY AUTO_INCREMENT,
    ContractID INT NOT NULL,
    Amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 CHECK (Amount >= 0),
    PaymentDate DATE NOT NULL DEFAULT (CURRENT_DATE),
    PaymentType VARCHAR(20) NOT NULL DEFAULT 'Cash' CHECK (PaymentType IN ('Online','Card','Cash')),
    FOREIGN KEY (ContractID) REFERENCES RentalContract(ContractID) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE OnlinePayment (
    PaymentID INT PRIMARY KEY,
    PaymentGateway VARCHAR(50) NOT NULL DEFAULT 'Unknown Gateway',
    FOREIGN KEY (PaymentID) REFERENCES Payment(PaymentID) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE CardPayment (
    PaymentID INT PRIMARY KEY,
    CardType VARCHAR(50) NOT NULL DEFAULT 'Unknown' CHECK (CardType IN ('Visa','Mastercard','Amex','Unknown')),
    FOREIGN KEY (PaymentID) REFERENCES Payment(PaymentID) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE CashPayment (
    PaymentID INT PRIMARY KEY,
    ReceiptNumber VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    FOREIGN KEY (PaymentID) REFERENCES Payment(PaymentID) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE Review (
    ReviewID INT PRIMARY KEY AUTO_INCREMENT,
    CustomerID INT NOT NULL,
    VehicleID INT NOT NULL,
    Rating INT NOT NULL DEFAULT 3 CHECK (Rating BETWEEN 1 AND 5),
    FOREIGN KEY (CustomerID) REFERENCES Customer(CustomerID) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (VehicleID) REFERENCES Vehicle(VehicleID) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE VehicleMaintenance (
    MaintenanceID INT PRIMARY KEY AUTO_INCREMENT,
    VehicleID INT NOT NULL,
    MechanicID INT NULL DEFAULT NULL,
    MaintenanceDate DATE NOT NULL DEFAULT (CURRENT_DATE),
    ServiceType VARCHAR(100) NOT NULL DEFAULT 'General Service',
    Cost DECIMAL(10,2) NOT NULL DEFAULT 0.00 CHECK (Cost >= 0),
    Notes TEXT,
    FOREIGN KEY (VehicleID) REFERENCES Vehicle(VehicleID) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (MechanicID) REFERENCES Mechanic(EmployeeID) ON DELETE SET NULL ON UPDATE CASCADE
);

CREATE TABLE FuelLog (
    FuelLogID INT PRIMARY KEY AUTO_INCREMENT,
    VehicleID INT NOT NULL,
    FuelLevelBefore DECIMAL(5,2) NOT NULL DEFAULT 0.00 CHECK (FuelLevelBefore BETWEEN 0 AND 100),
    FuelLevelAfter DECIMAL(5,2) NOT NULL DEFAULT 0.00 CHECK (FuelLevelAfter BETWEEN 0 AND 100),
    RefuelingCost DECIMAL(10,2) NOT NULL DEFAULT 0.00 CHECK (RefuelingCost >= 0),
    FOREIGN KEY (VehicleID) REFERENCES Vehicle(VehicleID) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE InsurancePolicy (
    PolicyID INT PRIMARY KEY AUTO_INCREMENT,
    VehicleID INT NOT NULL,
    ProviderName VARCHAR(100) NOT NULL DEFAULT 'Unknown Provider',
    CoverageAmount DECIMAL(12,2) NOT NULL DEFAULT 0.00 CHECK (CoverageAmount >= 0),
    ExpiryDate DATE NOT NULL,
    FOREIGN KEY (VehicleID) REFERENCES Vehicle(VehicleID) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE AccidentReport (
    ReportID INT PRIMARY KEY AUTO_INCREMENT,
    VehicleID INT NOT NULL,
    CustomerID INT NOT NULL,
    DateOfAccident DATE NOT NULL DEFAULT (CURRENT_DATE),
    Location VARCHAR(100) NOT NULL DEFAULT 'Unknown',
    DamageDescription TEXT,
    RepairCostEstimate DECIMAL(10,2) NOT NULL DEFAULT 0.00 CHECK (RepairCostEstimate >= 0),
    FOREIGN KEY (VehicleID) REFERENCES Vehicle(VehicleID) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (CustomerID) REFERENCES Customer(CustomerID) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE Fine (
    FineID INT PRIMARY KEY AUTO_INCREMENT,
    CustomerID INT NOT NULL,
    VehicleID INT NOT NULL,
    Amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 CHECK (Amount >= 0),
    Reason TEXT,
    FOREIGN KEY (CustomerID) REFERENCES Customer(CustomerID) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (VehicleID) REFERENCES Vehicle(VehicleID) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE License (
    DriverLicenseNumber VARCHAR(50) PRIMARY KEY,
    LicenseExpiryDate DATE NOT NULL,
    CustomerID INT UNIQUE NOT NULL,
    FOREIGN KEY (CustomerID) REFERENCES Customer(CustomerID) ON DELETE CASCADE ON UPDATE CASCADE
);

ALTER TABLE Branch
ADD CONSTRAINT fk_branch_manager
FOREIGN KEY (ManagerID) REFERENCES Employee(EmployeeID)
ON DELETE SET NULL ON UPDATE CASCADE;

DELIMITER //

-- Stored procedure 1: creates a booking after checking that the vehicle is available.
CREATE PROCEDURE CreateBooking (
    IN p_CustomerID INT,
    IN p_VehicleID INT,
    IN p_PickupDate DATE,
    IN p_ReturnDate DATE
)
BEGIN
    DECLARE v_status VARCHAR(30);

    SELECT Status INTO v_status
    FROM Vehicle
    WHERE VehicleID = p_VehicleID;

    IF v_status IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Vehicle does not exist';
    ELSEIF v_status <> 'Available' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Vehicle is not available for booking';
    ELSEIF p_ReturnDate < p_PickupDate THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Return date cannot be before pickup date';
    ELSE
        INSERT INTO Booking (CustomerID, VehicleID, PickupDate, ReturnDate, BookingStatus)
        VALUES (p_CustomerID, p_VehicleID, p_PickupDate, p_ReturnDate, 'Confirmed');

        UPDATE Vehicle
        SET Status = 'Booked'
        WHERE VehicleID = p_VehicleID;
    END IF;
END//

-- Stored procedure 2: records a payment and automatically inserts the subtype row.
CREATE PROCEDURE RecordPayment (
    IN p_ContractID INT,
    IN p_Amount DECIMAL(10,2),
    IN p_PaymentType VARCHAR(20),
    IN p_PaymentGateway VARCHAR(50),
    IN p_CardType VARCHAR(50),
    IN p_ReceiptNumber VARCHAR(50)
)
BEGIN
    DECLARE v_payment_id INT;

    IF p_Amount < 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Payment amount cannot be negative';
    ELSEIF p_PaymentType NOT IN ('Online','Card','Cash') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Invalid payment type';
    ELSE
        INSERT INTO Payment (ContractID, Amount, PaymentDate, PaymentType)
        VALUES (p_ContractID, p_Amount, CURRENT_DATE, p_PaymentType);

        SET v_payment_id = LAST_INSERT_ID();

        IF p_PaymentType = 'Online' THEN
            INSERT INTO OnlinePayment (PaymentID, PaymentGateway)
            VALUES (v_payment_id, COALESCE(p_PaymentGateway, 'Unknown Gateway'));
        ELSEIF p_PaymentType = 'Card' THEN
            INSERT INTO CardPayment (PaymentID, CardType)
            VALUES (v_payment_id, COALESCE(p_CardType, 'Unknown'));
        ELSE
            INSERT INTO CashPayment (PaymentID, ReceiptNumber)
            VALUES (v_payment_id, COALESCE(p_ReceiptNumber, CONCAT('CASH-', v_payment_id)));
        END IF;
    END IF;
END//

-- Trigger 1: prevents invalid promotion dates before a promotion is inserted.
CREATE TRIGGER trg_promotion_date_check
BEFORE INSERT ON Promotion
FOR EACH ROW
BEGIN
    IF NEW.EndDate IS NOT NULL AND NEW.EndDate < NEW.StartDate THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Promotion end date cannot be before start date';
    END IF;
END//

-- Trigger 2: when a rental contract is opened, the linked vehicle becomes rented.
CREATE TRIGGER trg_contract_sets_vehicle_rented
AFTER INSERT ON RentalContract
FOR EACH ROW
BEGIN
    IF NEW.ContractStatus = 'Active' THEN
        UPDATE Vehicle
        SET Status = 'Rented'
        WHERE VehicleID = NEW.VehicleID;
    END IF;
END//

DELIMITER ;
