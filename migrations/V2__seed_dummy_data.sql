INSERT INTO users (id, first_name, last_name, email, password, role, phone, date_of_birth, gender, aadhar_number, address, city, state, pincode, country, created_at, updated_at)
VALUES
(120, 'Priya', 'Pro', 'priya.pro@sports.com', '$2a$10$N.zmdr9k7uOLTACIxHKHhOKrnyGSpP5/CKWq7/FxvhgLQvPZaHYlu', 'ATHELETE', '+919900000120', '1999-12-12', 'FEMALE', '111122229999', '9 Fit St', 'Mumbai', 'Maharashtra', '400003', 'India', NOW() - INTERVAL '1400 days', NOW()),
(121, 'Rahul', 'Rookie', 'rahul.rookie@sports.com', '$2a$10$N.zmdr9k7uOLTACIxHKHhOKrnyGSpP5/CKWq7/FxvhgLQvPZaHYlu', 'ATHELETE', '+919900000121', '2005-08-08', 'MALE', '111122229998', '1 Newbie Ln', 'Delhi', 'Delhi', '110002', 'India', NOW() - INTERVAL '30 days', NOW()),
(122, 'Coach', 'Champ', 'coach.champ@sports.com', '$2a$10$N.zmdr9k7uOLTACIxHKHhOKrnyGSpP5/CKWq7/FxvhgLQvPZaHYlu', 'COACH', '+919900000122', '1982-04-04', 'MALE', '111122229997', '33 Coach Ave', 'Pune', 'Maharashtra', '411002', 'India', NOW() - INTERVAL '3000 days', NOW()),
(123, 'Mega', 'Sponsor', 'mega.sponsor@mega.com', '$2a$10$N.zmdr9k7uOLTACIxHKHhOKrnyGSpP5/CKWq7/FxvhgLQvPZaHYlu', 'SPONSOR', '+919900000123', '1975-07-07', 'FEMALE', '111122229996', '88 Corp Park', 'Mumbai', 'Maharashtra', '400004', 'India', NOW() - INTERVAL '5000 days', NOW())
ON CONFLICT (id) DO NOTHING;

-- Set profile images for some users to help AI marketability and performance proxies
UPDATE users SET profile_image_url = '/uploads/sample1.jpg' WHERE id IN (100, 102, 108, 120) AND (profile_image_url IS NULL OR profile_image_url = '');
UPDATE users SET profile_image_url = NULL WHERE id IN (101, 110, 121);

-- Coaches
INSERT INTO coaches (id, user_id, authority, specialization, experience_years, state, district) VALUES
(220, 122, 'National Board', 'Sprint', 18, 'Maharashtra', 'Pune')
ON CONFLICT (id) DO NOTHING;

-- Athletes
INSERT INTO athletes (id, user_id, height, weight, is_disabled, disability_type, emergency_contact_name, emergency_contact_phone, state, district) VALUES
(320, 120, 1.72, 58.0, false, NULL, 'Mom Priya', '+919999999120', 'Maharashtra', 'Mumbai'),
(321, 121, NULL, NULL, false, NULL, 'Dad Rahul', '+919999999121', 'Delhi', 'New Delhi')
ON CONFLICT (id) DO NOTHING;

-- Sponsors
INSERT INTO sponsors (id, user_id, company_name, industry, website, budget_range) VALUES
(420, 123, 'MegaCorp', 'Tech', 'https://mega.example.com', '5000000-20000000')
ON CONFLICT (id) DO NOTHING;

-- Achievements
INSERT INTO achievements (id, athlete_id, title, description, competition_name, certificate_url, achievement_date, rank_position, created_at) VALUES
(520, 320, 'District Gold', 'Top of the podium', 'Mumbai District', NULL, '2024-07-10', 1, NOW() - INTERVAL '60 days'),
(521, 320, 'Intercity Silver', 'Strong finish', 'Intercity Cup', NULL, '2025-01-15', 2, NOW() - INTERVAL '30 days')
ON CONFLICT (id) DO NOTHING;

-- Invitations and chat
INSERT INTO invitations (id, coach_id, player_id, status, created_at) VALUES
(620, 122, 120, 'PENDING', NOW() - INTERVAL '2 hours'),
(621, 122, 121, 'ACCEPTED', NOW() - INTERVAL '1 days')
ON CONFLICT (id) DO NOTHING;

INSERT INTO chat_rooms (id, coach_id, player_id, created_at) VALUES
(720, 122, 121, NOW() - INTERVAL '1 days')
ON CONFLICT (id) DO NOTHING;

INSERT INTO chat_messages (id, room_id, sender_id, content, created_at) VALUES
(820, 720, 122, 'Hi Rahul, let''s begin with basics.', NOW() - INTERVAL '1 days'),
(821, 720, 121, 'Yes coach!', NOW() - INTERVAL '1 days' + INTERVAL '5 minutes')
ON CONFLICT (id) DO NOTHING;
