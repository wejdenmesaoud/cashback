-- Test database initialization script
-- Create tables in the correct order to avoid foreign key constraint issues

-- First, create tables without foreign key constraints
CREATE TABLE IF NOT EXISTS roles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(120) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id INT NOT NULL,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE IF NOT EXISTS teams (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    user_id BIGINT
);

CREATE TABLE IF NOT EXISTS engineers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20),
    email VARCHAR(50),
    gender VARCHAR(10),
    manager VARCHAR(50) NOT NULL,
    team_id BIGINT
);

CREATE TABLE IF NOT EXISTS reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    chat VARCHAR(1000),
    total INT,
    engineer_name VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS cases (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    case_description VARCHAR(1000) NOT NULL,
    date TIMESTAMP NOT NULL,
    ces_rating INT,
    survey_source VARCHAR(50),
    sap_case_id VARCHAR(50),
    top_contract_type VARCHAR(50),
    ces_driver_correct_solution INT,
    ces_driver_timely_updates INT,
    ces_driver_timely_solution INT,
    ces_driver_professionalism INT,
    ces_driver_expertise INT,
    chat_session_id VARCHAR(100),
    survey_feedback VARCHAR(2000),
    engineer_id BIGINT,
    report_id BIGINT
);

CREATE TABLE IF NOT EXISTS bonuses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    amount DECIMAL(19,2) NOT NULL,
    calculation_date DATE NOT NULL,
    start_period DATE NOT NULL,
    end_period DATE NOT NULL,
    engineer_id BIGINT
);

CREATE TABLE IF NOT EXISTS settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    setting_key VARCHAR(100) NOT NULL,
    case_coefficient DOUBLE NOT NULL,
    chat_coefficient DOUBLE NOT NULL,
    user_id BIGINT
);

-- Insert default roles
INSERT INTO roles (name) VALUES ('ROLE_USER'), ('ROLE_MODERATOR'), ('ROLE_ADMIN') ON DUPLICATE KEY UPDATE name=name;