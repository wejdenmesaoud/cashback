-- Create database if not exists
CREATE DATABASE IF NOT EXISTS cashcase;

-- Use the database
USE cashcase;

-- Create roles table
CREATE TABLE IF NOT EXISTS roles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(20) NOT NULL
);

-- Insert default roles
INSERT INTO roles (name) VALUES ('ROLE_USER');
INSERT INTO roles (name) VALUES ('ROLE_MODERATOR');
INSERT INTO roles (name) VALUES ('ROLE_ADMIN');

-- Create teams table
CREATE TABLE IF NOT EXISTS teams (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(255)
);

-- Insert default team
INSERT INTO teams (name, description) VALUES ('Default Team', 'Default team for all users');

-- Create users table with team reference
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(120) NOT NULL,
    team_id INT,
    FOREIGN KEY (team_id) REFERENCES teams(id)
);

-- Create user_roles junction table
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id INT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- Create settings table
CREATE TABLE IF NOT EXISTS settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    setting_key VARCHAR(100) NOT NULL,
    case_coefficient DOUBLE NOT NULL,
    chat_coefficient DOUBLE NOT NULL,
    user_id BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Create default settings
INSERT INTO settings (setting_key, case_coefficient, chat_coefficient) 
VALUES ('DEFAULT', 1.0, 1.0);

-- Create refresh_token table
CREATE TABLE IF NOT EXISTS refresh_token (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    token VARCHAR(255) NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Grant privileges
GRANT ALL PRIVILEGES ON cashcase.* TO 'root'@'%';
FLUSH PRIVILEGES;
