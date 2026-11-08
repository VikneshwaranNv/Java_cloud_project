package com.smarttraffic.database;

import com.smarttraffic.models.CorridorReport;
import com.smarttraffic.models.EmergencyVehicle;
import com.smarttraffic.models.Intersection;
import com.smarttraffic.models.OverrideStatus;
import com.smarttraffic.models.PreemptionLog;
import com.smarttraffic.models.PriorityTier;
import com.smarttraffic.models.SignalState;
import com.smarttraffic.models.VehicleCategory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) providing CRUD operations for the smart_traffic database.
 * Implements STAGE 3 and STAGE 11 requirements using PreparedStatement and clean exception handling.
 */
public class TrafficDAO {

    // -------------------------------------------------------------------------
    // 1. EMERGENCY VEHICLES
    // -------------------------------------------------------------------------

    public void insertEmergencyVehicle(EmergencyVehicle v) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            insertEmergencyVehicle(conn, v);
        }
    }

    public void insertEmergencyVehicle(Connection conn, EmergencyVehicle v) throws SQLException {
        String sql = "INSERT INTO emergency_vehicles " +
                     "(vehicle_id, beacon_id, category, severity_code, destination_code, speed_kmh, latitude, longitude) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE speed_kmh = VALUES(speed_kmh), latitude = VALUES(latitude), longitude = VALUES(longitude)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, v.getVehicleId());
            stmt.setString(2, v.getBeaconId());
            stmt.setString(3, v.getCategory().name());
            stmt.setString(4, v.getSeverityCode());
            stmt.setString(5, v.getDestinationCode());
            stmt.setDouble(6, v.getSpeedKmh());
            stmt.setDouble(7, v.getLatitude());
            stmt.setDouble(8, v.getLongitude());

            stmt.executeUpdate();
        }
    }

    public List<EmergencyVehicle> getAllEmergencyVehicles() throws SQLException {
        List<EmergencyVehicle> list = new ArrayList<>();
        String sql = "SELECT vehicle_id, beacon_id, category, severity_code, destination_code, speed_kmh, latitude, longitude FROM emergency_vehicles ORDER BY created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                EmergencyVehicle v = new EmergencyVehicle();
                v.setVehicleId(rs.getString("vehicle_id"));
                v.setBeaconId(rs.getString("beacon_id"));
                v.setCategory(VehicleCategory.valueOf(rs.getString("category")));
                v.setSeverityCode(rs.getString("severity_code"));
                v.setDestinationCode(rs.getString("destination_code"));
                v.setSpeedKmh(rs.getDouble("speed_kmh"));
                v.setLatitude(rs.getDouble("latitude"));
                v.setLongitude(rs.getDouble("longitude"));
                list.add(v);
            }
        }
        return list;
    }

    // -------------------------------------------------------------------------
    // 2. INTERSECTIONS
    // -------------------------------------------------------------------------

    public void insertIntersection(Intersection i) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            insertIntersection(conn, i);
        }
    }

    public void insertIntersection(Connection conn, Intersection i) throws SQLException {
        String sql = "INSERT INTO intersections " +
                     "(intersection_id, name, latitude, longitude, current_signal_state, camera_density_count, is_blocked, green_duration_sec, clearance_delay_sec) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE current_signal_state = VALUES(current_signal_state), camera_density_count = VALUES(camera_density_count), is_blocked = VALUES(is_blocked)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, i.getIntersectionId());
            stmt.setString(2, i.getName());
            stmt.setDouble(3, i.getLatitude());
            stmt.setDouble(4, i.getLongitude());
            stmt.setString(5, i.getCurrentSignal().name());
            stmt.setInt(6, i.getCameraDensity());
            stmt.setBoolean(7, i.isBlocked());
            stmt.setInt(8, i.getGreenDurationSec());
            stmt.setInt(9, i.getClearanceDelaySec());

            stmt.executeUpdate();
        }
    }

    public List<Intersection> getAllIntersections() throws SQLException {
        List<Intersection> list = new ArrayList<>();
        String sql = "SELECT intersection_id, name, latitude, longitude, current_signal_state, camera_density_count, is_blocked, green_duration_sec, clearance_delay_sec FROM intersections ORDER BY intersection_id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Intersection i = new Intersection(
                    rs.getString("intersection_id"),
                    rs.getString("name"),
                    rs.getDouble("latitude"),
                    rs.getDouble("longitude"),
                    SignalState.valueOf(rs.getString("current_signal_state")),
                    rs.getInt("camera_density_count"),
                    rs.getBoolean("is_blocked"),
                    rs.getInt("green_duration_sec"),
                    rs.getInt("clearance_delay_sec")
                );
                list.add(i);
            }
        }
        return list;
    }

    // -------------------------------------------------------------------------
    // 3. PREEMPTION LOGS
    // -------------------------------------------------------------------------

    public long insertPreemptionLog(PreemptionLog log) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            return insertPreemptionLog(conn, log);
        }
    }

    public long insertPreemptionLog(Connection conn, PreemptionLog log) throws SQLException {
        String sql = "INSERT INTO preemption_logs " +
                     "(vehicle_id, intersection_id, eta_minutes, override_triggered, override_status, previous_signal, new_signal, clearance_delay_sec, transit_duration_sec, thread_name, log_message) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, log.getVehicleId());
            stmt.setString(2, log.getIntersectionId());
            stmt.setDouble(3, log.getEtaMinutes());
            stmt.setBoolean(4, log.isOverrideTriggered());
            stmt.setString(5, log.getOverrideStatus().name());
            stmt.setString(6, log.getPreviousSignal() != null ? log.getPreviousSignal().name() : "RED");
            stmt.setString(7, log.getNewSignal() != null ? log.getNewSignal().name() : "GREEN");
            stmt.setInt(8, log.getClearanceDelaySec());
            stmt.setInt(9, log.getTransitDurationSec());
            stmt.setString(10, log.getThreadName());
            stmt.setString(11, log.getLogMessage());

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long id = generatedKeys.getLong(1);
                    log.setLogId(id);
                    return id;
                }
            }
        }
        return 0L;
    }

    public List<PreemptionLog> getPreemptionLogsForVehicle(String vehicleId) throws SQLException {
        List<PreemptionLog> list = new ArrayList<>();
        String sql = "SELECT log_id, vehicle_id, intersection_id, eta_minutes, override_triggered, override_status, previous_signal, new_signal, clearance_delay_sec, transit_duration_sec, thread_name, log_message, logged_at " +
                     "FROM preemption_logs WHERE vehicle_id = ? ORDER BY log_id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, vehicleId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    PreemptionLog l = new PreemptionLog(
                        rs.getString("vehicle_id"),
                        rs.getString("intersection_id"),
                        rs.getDouble("eta_minutes"),
                        rs.getBoolean("override_triggered"),
                        OverrideStatus.valueOf(rs.getString("override_status")),
                        SignalState.valueOf(rs.getString("previous_signal")),
                        SignalState.valueOf(rs.getString("new_signal")),
                        rs.getInt("clearance_delay_sec"),
                        rs.getInt("transit_duration_sec"),
                        rs.getString("thread_name"),
                        rs.getString("log_message")
                    );
                    l.setLogId(rs.getLong("log_id"));
                    Timestamp ts = rs.getTimestamp("logged_at");
                    if (ts != null) {
                        l.setLoggedAt(ts.toLocalDateTime());
                    }
                    list.add(l);
                }
            }
        }
        return list;
    }

    // -------------------------------------------------------------------------
    // 4. CORRIDOR REPORTS
    // -------------------------------------------------------------------------

    public void insertCorridorReport(CorridorReport report) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            insertCorridorReport(conn, report);
        }
    }

    public void insertCorridorReport(Connection conn, CorridorReport report) throws SQLException {
        String sql = "INSERT INTO corridor_reports " +
                     "(report_id, vehicle_id, category, priority_score, priority_tier, total_intersections, overridden_intersections, total_transit_seconds, corridor_status, summary_text) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE priority_score = VALUES(priority_score), corridor_status = VALUES(corridor_status), summary_text = VALUES(summary_text)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, report.getReportId());
            stmt.setString(2, report.getVehicleId());
            stmt.setString(3, report.getCategory().name());
            stmt.setDouble(4, report.getPriorityScore());
            stmt.setString(5, report.getPriorityTier().name());
            stmt.setInt(6, report.getTotalIntersections());
            stmt.setInt(7, report.getOverriddenIntersections());
            stmt.setInt(8, report.getTotalTransitSeconds());
            stmt.setString(9, report.getCorridorStatus());
            stmt.setString(10, report.getSummaryText());

            stmt.executeUpdate();
        }
    }

    public CorridorReport getCorridorReport(String reportId) throws SQLException {
        String sql = "SELECT report_id, vehicle_id, category, priority_score, priority_tier, total_intersections, overridden_intersections, total_transit_seconds, corridor_status, summary_text, generated_at " +
                     "FROM corridor_reports WHERE report_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, reportId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    CorridorReport r = new CorridorReport(
                        rs.getString("report_id"),
                        rs.getString("vehicle_id"),
                        VehicleCategory.valueOf(rs.getString("category")),
                        rs.getDouble("priority_score"),
                        PriorityTier.valueOf(rs.getString("priority_tier")),
                        rs.getInt("total_intersections"),
                        rs.getInt("overridden_intersections"),
                        rs.getInt("total_transit_seconds"),
                        rs.getString("corridor_status"),
                        rs.getString("summary_text")
                    );
                    Timestamp ts = rs.getTimestamp("generated_at");
                    if (ts != null) {
                        r.setGeneratedAt(ts.toLocalDateTime());
                    }
                    return r;
                }
            }
        }
        return null;
    }
}
