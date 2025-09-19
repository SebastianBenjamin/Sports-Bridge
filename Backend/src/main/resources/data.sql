-- Insert demo users for testing
INSERT INTO users (id, first_name, last_name, email, password, role, phone, date_of_birth, gender, aadhar_number, address, city, state, pincode, country, created_at, updated_at) VALUES
(1, 'Admin', 'User', 'admin@sportsbridge.com', '$2a$10$N.zmdr9k7uOLTACIxHKHhOKrnyGSpP5/CKWq7/FxvhgLQvPZaHYlu', 'ADMIN', '9999999999', '1990-01-01', 'MALE', '123412341234', '123 Admin Street', 'Mumbai', 'Maharashtra', '400001', 'India', NOW(), NOW()),
(2, 'John', 'Athlete', 'athlete@sportsbridge.com', '$2a$10$N.zmdr9k7uOLTACIxHKHhOKrnyGSpP5/CKWq7/FxvhgLQvPZaHYlu', 'ATHELETE', '9876543210', '1995-06-15', 'MALE', '234523452345', '456 Sports Avenue', 'Delhi', 'Delhi', '110001', 'India', NOW(), NOW()),
(3, 'Jane', 'Coach', 'coach@sportsbridge.com', '$2a$10$N.zmdr9k7uOLTACIxHKHhOKrnyGSpP5/CKWq7/FxvhgLQvPZaHYlu', 'COACH', '9876543211', '1985-03-20', 'FEMALE', '345634563456', '789 Training Ground', 'Bangalore', 'Karnataka', '560001', 'India', NOW(), NOW()),
(4, 'Bob', 'Sponsor', 'sponsor@sportsbridge.com', '$2a$10$N.zmdr9k7uOLTACIxHKHhOKrnyGSpP5/CKWq7/FxvhgLQvPZaHYlu', 'SPONSOR', '9876543212', '1980-12-10', 'MALE', '456745674567', '321 Business Park', 'Chennai', 'Tamil Nadu', '600001', 'India', NOW(), NOW());

-- Insert some sports
INSERT INTO sports (id, name, description, created_at, updated_at) VALUES
(1, 'Football', 'The beautiful game', NOW(), NOW()),
(2, 'Cricket', 'Gentleman''s game', NOW(), NOW()),
(3, 'Basketball', 'Shoot hoops', NOW(), NOW()),
(4, 'Tennis', 'Racket sport', NOW(), NOW()),
(5, 'Swimming', 'Water sport', NOW(), NOW());
