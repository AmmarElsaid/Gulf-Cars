USE gulfcars;

INSERT INTO Customer VALUES
(1,'Siphesihle','Ngwane','+27601311223','Ngwane@gmail.com','Westville, Durban','1990-04-12',120),
(2,'Ammar','Elsaid','+2777223344','ammar@gmail.com','Westville, Durban','1995-09-23',45),
(3,'Kwanele','Mzila','+27150334455','kmzila13@gmail.com','KwaMashu, Durban','1988-01-30',300);

INSERT INTO Promotion VALUES
(1,'Summer Splash 15% off',15.00,'2025-06-01','2025-08-31'),
(2,'Loyalty Bonus 10%',10.00,'2025-01-01','2025-12-31');

INSERT INTO Supplier VALUES
(1,'Gulf Auto Imports','Faisal Rahman','+27642223344','contact@gulfauto.ae','Jebel Ali Free Zone'),
(2,'Desert Motors','Layla Saeed','+2743334455','sales@desertmotors.ae','Sheikh Zayed Rd');

INSERT INTO Branch (BranchID,BranchName,Location,PhoneNumber) VALUES
(1,'GulfCars Downtown','Dubai Downtown','+2714440001'),
(2,'GulfCars Airport','DXB Terminal 3','+2714440002'),
(3,'GulfCars Abu Dhabi','Yas Island','+2712440003');

INSERT INTO Employee (Name,PhoneNumber,Email,HireDate, Salary,BranchID,EmployeeType) VALUES
('Mariam Yusuf','+27151110001','mariam@gulfcars.ae','2022-03-15',9500.00,1,'RentalAgent'),
('Khalid Noor','+27155110002','khalid@gulfcars.ae','2021-07-01',12000.00,2,'RentalAgent'),
('Rashid Ali','+271551110003','rashid@gulfcars.ae','2020-02-10',11000.00,1,'Mechanic'),
('Yousef Tariq','+27155111004','yousef@gulfcars.ae','2023-05-20',8500.00,3,'Mechanic');

-- AgentLevel must be one of: Junior, Intermediate, Senior
INSERT INTO RentalAgent (EmployeeID, AgentLevel, CustomerServiceRating) VALUES
(1,'Senior',4.80),
(2,'Senior',4.95);

-- CertificationLevel must be one of: Basic, Intermediate, Advanced, Master
INSERT INTO Mechanic (EmployeeID, Specialisation, CertificationLevel) VALUES
(3,'Engine & Transmission','Master'),
(4,'Bodywork','Advanced');

UPDATE Branch SET ManagerID=2 WHERE BranchID=2;
UPDATE Branch SET ManagerID=1 WHERE BranchID=1;

-- CategoryType must be one of: Economy, Compact, SUV, Luxury, Van, Truck
INSERT INTO Vehicle (SupplierID, Make, Model, Year, LicensePlate, VIN, Status, Mileage, CategoryType) VALUES
(1,'Toyota','Land Cruiser',2023,'DXB-A-1001','VIN0001LC23','Available',15400,'SUV'),
(1,'Nissan','Patrol',2024,'DXB-B-1002','VIN0002NP24','Rented',8200,'SUV'),
(2,'BMW','5 Series',2023,'AUH-C-1003','VIN0003BM23','Available',22100,'Luxury'),
(2,'Mercedes','G-Wagon',2024,'DXB-D-1004','VIN0004MG24','Maintenance',5400,'Luxury'),
(1,'Hyundai','Elantra',2022,'SHJ-E-1005','VIN0005HE22','Available',41000,'Economy');

-- BookingStatus must be one of: Pending, Confirmed, Cancelled, Completed
INSERT INTO Booking (CustomerID, VehicleID, PickupDate, ReturnDate, BookingStatus) VALUES
(1,2,'2025-04-20','2025-04-25','Confirmed'),
(2,3,'2025-05-10','2025-05-15','Confirmed');

INSERT INTO RentalContract (CustomerID, VehicleID, EmployeeID, StartDate, EndDate, TotalRentFee, DepositAmount, ContractStatus) VALUES
(1,2,1,'2025-04-20','2025-04-25',1750.00,500.00,'Active');

INSERT INTO Payment (ContractID, Amount, PaymentDate, PaymentType) VALUES
(1,500.00,'2025-04-20','Card'),
(1,1750.00,'2025-04-25','Online');

INSERT INTO CardPayment VALUES (1,'Visa');
INSERT INTO OnlinePayment VALUES (2,'Stripe');

INSERT INTO Review (CustomerID, VehicleID, Rating) VALUES
(1,2,5),
(2,3,4);

INSERT INTO VehicleMaintenance (VehicleID, MechanicID, MaintenanceDate, ServiceType, Cost, Notes) VALUES
(4,3,'2025-04-15','Brake replacement',1200.00,'Front pads + rotors');

INSERT INTO InsurancePolicy (VehicleID, ProviderName, CoverageAmount, ExpiryDate) VALUES
(1,'Oman Insurance',150000.00,'2026-01-01'),
(2,'AXA Gulf',180000.00,'2026-03-15');

INSERT INTO License VALUES
('DL-AE-0001','2027-05-10',1),
('DL-AE-0002','2026-12-01',2);
