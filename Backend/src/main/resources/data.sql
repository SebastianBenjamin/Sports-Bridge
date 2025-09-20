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

-- Additional dummy legacy users (IDs 100+), varied roles and regions, edge cases
INSERT INTO users (id, first_name, last_name, email, password, role, phone, date_of_birth, gender, aadhar_number, address, city, state, pincode, country, created_at, updated_at) VALUES
(100, 'Ava', 'Athlete', 'ava.athlete@sports.com', '$2a$10$N.zmdr9k7uOLTACIxHKHhOKrnyGSpP5/CKWq7/FxvhgLQvPZaHYlu', 'ATHELETE', '+919900000100', '2000-01-10', 'FEMALE', '111122223333', '12 River Rd', 'Mumbai', 'Maharashtra', '400001', 'India', NOW() - INTERVAL '1200 days', NOW()),
(101, 'Zed', 'Zero', 'zed.zero@sports.com', '$2a$10$N.zmdr9k7uOLTACIxHKHhOKrnyGSpP5/CKWq7/FxvhgLQvPZaHYlu', 'ATHELETE', '+919900000101', '2003-05-20', 'MALE', '111122223334', '404 Null St', 'Nowhere', 'Nowhere', '000000', 'Noland', NOW() - INTERVAL '1 days', NOW()),
(102, 'Max', 'Images', 'max.images@sports.com', '$2a$10$N.zmdr9k7uOLTACIxHKHhOKrnyGSpP5/CKWq7/FxvhgLQvPZaHYlu', 'ATHELETE', '+919900000102', '1998-07-07', 'MALE', '111122223335', '77 Gallery Ave', 'Delhi', 'Delhi', '110001', 'India', NOW() - INTERVAL '800 days', NOW()),
(103, 'Mini', 'Caption', 'mini.caption@sports.com', '$2a$10$N.zmdr9k7uOLTACIxHKHhOKrnyGSpP5/CKWq7/FxvhgLQvPZaHYlu', 'ATHELETE', '+919900000103', '1999-09-09', 'FEMALE', '111122223336', '1 Short Ln', 'Delhi', 'Delhi', '110001', 'India', NOW() - INTERVAL '500 days', NOW()),
(104, 'Coach', 'Core', 'coach.core@sports.com', '$2a$10$N.zmdr9k7uOLTACIxHKHhOKrnyGSpP5/CKWq7/FxvhgLQvPZaHYlu', 'COACH', '+919900000104', '1980-03-03', 'MALE', '111122223337', '90 Mentor Blvd', 'Bangalore', 'Karnataka', '560001', 'India', NOW() - INTERVAL '2000 days', NOW()),
(105, 'Sally', 'Sponsor', 'sally.sponsor@bigfunds.com', '$2a$10$N.zmdr9k7uOLTACIxHKHhOKrnyGSpP5/CKWq7/FxvhgLQvPZaHYlu', 'SPONSOR', '+919900000105', '1988-02-02', 'FEMALE', '111122223338', '22 Finance Park', 'Chennai', 'Tamil Nadu', '600001', 'India', NOW() - INTERVAL '3000 days', NOW()),
(106, 'Tiny', 'Budget', 'tiny.budget@microfund.org', '$2a$10$N.zmdr9k7uOLTACIxHKHhOKrnyGSpP5/CKWq7/FxvhgLQvPZaHYlu', 'SPONSOR', '+919900000106', '1992-11-11', 'MALE', '111122223339', '3 Lean St', 'Pune', 'Maharashtra', '411001', 'India', NOW() - INTERVAL '200 days', NOW()),
(107, 'No', 'Budget', 'no.budget@unknown.org', '$2a$10$N.zmdr9k7uOLTACIxHKHhOKrnyGSpP5/CKWq7/FxvhgLQvPZaHYlu', 'SPONSOR', '+919900000107', '1991-01-21', 'MALE', '111122223340', '9 Missing Ave', 'Hyderabad', 'Telangana', '500001', 'India', NOW() - INTERVAL '50 days', NOW()),
(108, 'Clara', 'Citymatch', 'clara.city@sports.com', '$2a$10$N.zmdr9k7uOLTACIxHKHhOKrnyGSpP5/CKWq7/FxvhgLQvPZaHYlu', 'ATHELETE', '+919900000108', '1997-05-05', 'FEMALE', '111122223341', '45 Local Rd', 'Mumbai', 'Maharashtra', '400002', 'India', NOW() - INTERVAL '900 days', NOW()),
(109, 'Diego', 'DiffCountry', 'diego.diff@intl.com', '$2a$10$N.zmdr9k7uOLTACIxHKHhOKrnyGSpP5/CKWq7/FxvhgLQvPZaHYlu', 'ATHELETE', '+521234567890', '1996-06-06', 'MALE', '111122223342', '10 Abroad St', 'Mexico City', 'CDMX', '01000', 'Mexico', NOW() - INTERVAL '700 days', NOW()),
(110, 'Una', 'Unverified', 'una.unverified@sports.com', '$2a$10$N.zmdr9k7uOLTACIxHKHhOKrnyGSpP5/CKWq7/FxvhgLQvPZaHYlu', 'ATHELETE', '+919900000110', '2001-01-01', 'FEMALE', '111122223343', '1 Pending Dr', 'Kolkata', 'West Bengal', '700001', 'India', NOW() - INTERVAL '100 days', NOW());

-- Legacy coaches, athletes, sponsors referencing users
INSERT INTO coaches (id, user_id, authority, specialization, experience_years, state, district) VALUES
(200, 104, 'National Board', 'Strength & Conditioning', 15, 'Karnataka', 'Bangalore Urban');

INSERT INTO athletes (id, user_id, height, weight, is_disabled, disability_type, emergency_contact_name, emergency_contact_phone, state, district) VALUES
(300, 100, 1.70, 60.5, false, NULL, 'Mom Ava', '+911234567890', 'Maharashtra', 'Mumbai'),
(301, 101, NULL, NULL, false, NULL, 'Friend Zed', '+911234567891', 'Nowhere', 'Nowhere'),
(302, 102, 1.85, 80.0, false, NULL, 'Coach Max', '+911234567892', 'Delhi', 'New Delhi'),
(303, 108, 1.68, 55.0, false, NULL, 'Dad Clara', '+911234567893', 'Maharashtra', 'Mumbai'),
(304, 109, 1.80, 78.0, false, NULL, 'Sister Diego', '+5211111111', 'CDMX', 'Benito Juarez');

INSERT INTO sponsors (id, user_id, company_name, industry, website, budget_range) VALUES
(400, 105, 'BigFunds Ltd', 'Finance', 'https://bigfunds.example.com', '1000000-5000000'),
(401, 106, 'MicroFund', 'Non-profit', NULL, '100-500'),
(402, 107, 'Unknown Inc', 'Unknown', NULL, NULL);

-- Achievements for players (many vs none)
INSERT INTO achievements (id, athlete_id, title, description, competition_name, certificate_url, achievement_date, rank_position, created_at) VALUES
(500, 300, 'State Gold', 'Won gold at state meet', 'Maharashtra State Meet', NULL, '2023-01-15', 1, NOW() - INTERVAL '500 days'),
(501, 300, 'National Silver', 'Runner-up', 'National Games', NULL, '2024-02-18', 2, NOW() - INTERVAL '300 days'),
(502, 302, 'City Bronze', 'Third place in city trials', 'Delhi Trials', NULL, '2022-11-10', 3, NOW() - INTERVAL '700 days');

-- Invitations seeds (legacy users): pending/accepted/rejected
INSERT INTO invitations (id, coach_id, player_id, status, created_at) VALUES
(600, 104, 100, 'PENDING', NOW() - INTERVAL '1 day'),
(601, 104, 102, 'ACCEPTED', NOW() - INTERVAL '2 days'),
(602, 104, 101, 'REJECTED', NOW() - INTERVAL '3 days');

-- Chat rooms/messages seeds for accepted invitation (coach 104, player 102)
INSERT INTO chat_rooms (id, coach_id, player_id, created_at) VALUES
(700, 104, 102, NOW() - INTERVAL '2 days')
ON CONFLICT DO NOTHING;

INSERT INTO chat_messages (id, room_id, sender_id, content, created_at) VALUES
(800, 700, 104, 'Welcome to the program!', NOW() - INTERVAL '2 days'),
(801, 700, 102, 'Thank you, coach!', NOW() - INTERVAL '2 days' + INTERVAL '10 minutes');
