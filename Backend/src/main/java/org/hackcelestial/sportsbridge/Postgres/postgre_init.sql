-- Step 1: Create database
CREATE DATABASE sportsbridge;

-- Step 2: Create user
CREATE USER admin WITH PASSWORD 'admin';

-- Step 3: Grant privileges
GRANT ALL PRIVILEGES ON DATABASE sportsbridge TO admin;

-- Step 4: Connect to the new database and set schema privileges
\connect sportsbridge
GRANT ALL ON SCHEMA public TO admin;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO admin;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO admin;