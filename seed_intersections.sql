-- ============================================================================
-- SMART CITY TRAFFIC SIGNAL & EMERGENCY VEHICLE GREEN CORRIDOR ROUTER
-- Seed Script: seed_intersections.sql
-- Pre-populates corridor intersections INT-01 through INT-06
-- ============================================================================

USE smart_traffic;

INSERT INTO intersections (intersection_id, name, latitude, longitude, current_signal_state, camera_density_count, is_blocked, green_duration_sec, clearance_delay_sec)
VALUES
('INT-01', 'Central Boulevard & 1st Ave', 13.0827000, 80.2707000, 'RED', 45, TRUE, 60, 8),
('INT-02', 'Grand Trunk Road Junction', 13.0845000, 80.2725000, 'RED', 62, TRUE, 55, 12),
('INT-03', 'Metro Central Ring Road', 13.0862000, 80.2741000, 'YELLOW', 28, FALSE, 50, 4),
('INT-04', 'City General Hospital Cross', 13.0881000, 80.2763000, 'RED', 75, TRUE, 70, 15),
('INT-05', 'Tech Corridor North Express', 13.0905000, 80.2789000, 'RED', 35, FALSE, 45, 6),
('INT-06', 'Eastern Highway Terminal', 13.0928000, 80.2812000, 'GREEN', 15, FALSE, 40, 2)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    latitude = VALUES(latitude),
    longitude = VALUES(longitude),
    current_signal_state = VALUES(current_signal_state),
    camera_density_count = VALUES(camera_density_count),
    is_blocked = VALUES(is_blocked),
    green_duration_sec = VALUES(green_duration_sec),
    clearance_delay_sec = VALUES(clearance_delay_sec);

SELECT 'Intersections INT-01 through INT-06 seeded successfully.' AS status;
