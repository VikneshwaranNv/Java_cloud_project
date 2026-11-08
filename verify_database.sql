-- ============================================================================
-- SMART CITY TRAFFIC SIGNAL & EMERGENCY VEHICLE GREEN CORRIDOR ROUTER
-- Verification Script: verify_database.sql
-- Inspects tables, row counts, user grants, FK constraints, and telemetry records
-- ============================================================================

USE smart_traffic;

SELECT '=== TABLE RECORD COUNTS ===' AS section;
SELECT 
    (SELECT COUNT(*) FROM emergency_vehicles) AS vehicles_ingested,
    (SELECT COUNT(*) FROM intersections)      AS intersections_active,
    (SELECT COUNT(*) FROM preemption_logs)    AS preemption_events,
    (SELECT COUNT(*) FROM corridor_reports)   AS corridor_reports_saved;

SELECT '=== RECENT EMERGENCY VEHICLES ===' AS section;
SELECT vehicle_id, beacon_id, category, severity_code, destination_code, speed_kmh, updated_at
FROM emergency_vehicles
ORDER BY updated_at DESC
LIMIT 5;

SELECT '=== INTERSECTIONS STATE ===' AS section;
SELECT intersection_id, name, current_signal_state, camera_density_count, is_blocked, clearance_delay_sec
FROM intersections
ORDER BY intersection_id ASC;

SELECT '=== RECENT PREEMPTION LOGS ===' AS section;
SELECT log_id, vehicle_id, intersection_id, eta_minutes, override_triggered, override_status, previous_signal, new_signal, thread_name, logged_at
FROM preemption_logs
ORDER BY log_id DESC
LIMIT 10;

SELECT '=== RECENT CORRIDOR PERFORMANCE REPORTS ===' AS section;
SELECT report_id, vehicle_id, category, priority_score, priority_tier, total_intersections, overridden_intersections, corridor_status, generated_at
FROM corridor_reports
ORDER BY generated_at DESC
LIMIT 5;

SELECT '=== USER PRIVILEGES VERIFICATION ===' AS section;
SHOW GRANTS FOR 'traffic_app'@'%';
