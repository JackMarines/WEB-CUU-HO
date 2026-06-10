CREATE DATABASE IF NOT EXISTS emergency_response;
USE emergency_response;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(15),
    password VARCHAR(255),
    provider VARCHAR(50) DEFAULT 'local',
    provider_id VARCHAR(255),
    avatar_url VARCHAR(500),
    role ENUM('user', 'admin', 'superadmin') NOT NULL DEFAULT 'user',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS disaster_types (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    slug VARCHAR(50) NOT NULL UNIQUE,
    icon VARCHAR(100),
    base_urgency_score INT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS distress_calls (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    caller_name VARCHAR(100),
    caller_phone VARCHAR(15),
    disaster_type_id BIGINT NOT NULL,
    lat DECIMAL(10,7) NOT NULL,
    lng DECIMAL(10,7) NOT NULL,
    location_name VARCHAR(255),
    description TEXT NOT NULL,
    status ENUM('active', 'in_progress', 'resolved', 'dismissed') NOT NULL DEFAULT 'active',
    urgency_score INT NOT NULL,
    person_count INT NOT NULL DEFAULT 1,
    image_url VARCHAR(500),
    suggested_supplies JSON,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at DATETIME,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (disaster_type_id) REFERENCES disaster_types(id)
);

CREATE TABLE IF NOT EXISTS rescue_centers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    type ENUM('shelter', 'supply_distribution', 'rescue_team') NOT NULL,
    lat DECIMAL(10,7) NOT NULL,
    lng DECIMAL(10,7) NOT NULL,
    address VARCHAR(500),
    phone VARCHAR(15),
    supplies JSON,
    capacity INT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS responses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    distress_call_id BIGINT NOT NULL,
    rescue_center_id BIGINT NOT NULL,
    assigned_by BIGINT NOT NULL,
    status ENUM('assigned', 'in_progress', 'delivered') NOT NULL DEFAULT 'assigned',
    note TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (distress_call_id) REFERENCES distress_calls(id),
    FOREIGN KEY (rescue_center_id) REFERENCES rescue_centers(id),
    FOREIGN KEY (assigned_by) REFERENCES users(id)
);
