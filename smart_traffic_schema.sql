-- ============================================================================
-- SMART CITY TRAFFIC SIGNAL & EMERGENCY VEHICLE GREEN CORRIDOR ROUTER
-- Database Schema: smart_traffic_schema.sql
-- Compatible with: MySQL Community Server 8.4 LTS / Oracle Cloud (OCI) MySQL
-- Engine: InnoDB | Character Set: utf8mb4 | Collation: utf8mb4_unicode_ci
-- ============================================================================

-- Create database if it does not exist (can be executed once granted permission)
CREATE DATABASE IF NOT EXISTS smart_traffic
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE smart_traffic;

-- Drop tables in reverse order of foreign key dependency if re-creating
DROP TABLE IF EXISTS corridor_reports;
DROP TABLE IF EXISTS preemption_logs;
DROP TABLE IF EXISTS intersections;
DROP TABLE IF EXISTS emergency_vehicles;

-- ----------------------------------------------------------------------------
-- Table 1: emergency_vehicles
-- Stores registered emergency vehicles and their live phase parameters
-- ----------------------------------------------------------------------------
CREATE TABLE emergency_vehicles (
    vehicle_id VARCHAR(32) NOT NULL,
    beacon_id VARCHAR(32) NOT NULL,
    category ENUM('AMBULANCE', 'FIRE_ENGINE', 'POLICE_VIP') NOT NULL,
    severity_code VARCHAR(32) NOT NULL,
    destination_code VARCHAR(64) NOT NULL,
    speed_kmh DOUBLE NOT NULL DEFAULT 0.0,
    latitude DECIMAL(10, 7) NOT NULL,
    longitude DECIMAL(10, 7) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (vehicle_id),
    UNIQUE KEY uq_beacon_id (beacon_id),
    INDEX idx_category (category),
    INDEX idx_severity (severity_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- Table 2: intersections
-- Represents physical signalized intersections along city road corridors
-- ----------------------------------------------------------------------------
CREATE TABLE intersections (
    intersection_id VARCHAR(32) NOT NULL,
    name VARCHAR(128) NOT NULL,
    latitude DECIMAL(10, 7) NOT NULL,
    longitude DECIMAL(10, 7) NOT NULL,
    current_signal_state ENUM('RED', 'YELLOW', 'GREEN') NOT NULL DEFAULT 'RED',
    camera_density_count INT NOT NULL DEFAULT 0,
    is_blocked BOOLEAN NOT NULL DEFAULT FALSE,
    green_duration_sec INT NOT NULL DEFAULT 45,
    clearance_delay_sec INT NOT NULL DEFAULT 5,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (intersection_id),
    INDEX idx_intersection_status (is_blocked, current_signal_state)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- Table 3: preemption_logs
-- Records real-time preemption actions (green waves, signal overrides)
-- ----------------------------------------------------------------------------
CREATE TABLE preemption_logs (
    log_id BIGINT AUTO_INCREMENT NOT NULL,
    vehicle_id VARCHAR(32) NOT NULL,
    intersection_id VARCHAR(32) NOT NULL,
    eta_minutes DOUBLE NOT NULL,
    override_triggered BOOLEAN NOT NULL,
    override_status ENUM('SUCCESS', 'FAILED', 'BYPASSED') NOT NULL,
    previous_signal ENUM('RED', 'YELLOW', 'GREEN') NOT NULL,
    new_signal ENUM('RED', 'YELLOW', 'GREEN') NOT NULL,
    clearance_delay_sec INT NOT NULL,
    transit_duration_sec INT NOT NULL,
    thread_name VARCHAR(64) NOT NULL,
    log_message TEXT NOT NULL,
    logged_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (log_id),
    CONSTRAINT fk_preempt_vehicle FOREIGN KEY (vehicle_id)
        REFERENCES emergency_vehicles (vehicle_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_preempt_intersection FOREIGN KEY (intersection_id)
        REFERENCES intersections (intersection_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    INDEX idx_preempt_vehicle (vehicle_id),
    INDEX idx_preempt_intersection (intersection_id),
    INDEX idx_override_status (override_status),
    INDEX idx_logged_at (logged_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- Table 4: corridor_reports
-- Summarizes overall transit routing, priority score, tier, and clearance
-- ----------------------------------------------------------------------------
CREATE TABLE corridor_reports (
    report_id VARCHAR(64) NOT NULL,
    vehicle_id VARCHAR(32) NOT NULL,
    category ENUM('AMBULANCE', 'FIRE_ENGINE', 'POLICE_VIP') NOT NULL,
    priority_score DOUBLE NOT NULL,
    priority_tier ENUM('GOLD_CORRIDOR', 'STANDARD_CLEARANCE', 'MINOR_TRAFFIC_DELAY', 'ROUTE_BLOCKED') NOT NULL,
    total_intersections INT NOT NULL,
    overridden_intersections INT NOT NULL,
    total_transit_seconds INT NOT NULL,
    corridor_status ENUM('ACTIVE', 'COMPLETED', 'ABORTED') NOT NULL DEFAULT 'COMPLETED',
    summary_text TEXT NOT NULL,
    generated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (report_id),
    CONSTRAINT fk_report_vehicle FOREIGN KEY (vehicle_id)
        REFERENCES emergency_vehicles (vehicle_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    INDEX idx_report_tier (priority_tier),
    INDEX idx_report_vehicle (vehicle_id),
    INDEX idx_generated_at (generated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
