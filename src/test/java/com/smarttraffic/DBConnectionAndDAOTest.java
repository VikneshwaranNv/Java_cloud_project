package com.smarttraffic;

import com.smarttraffic.database.DBConnection;
import com.smarttraffic.database.TrafficDAO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests verifying STAGE 3 & STAGE 11 DBConnection error handling and DAO readiness.
 */
public class DBConnectionAndDAOTest {

    @Test
    @DisplayName("DBConnection should provide non-null JDBC configuration without hardcoding")
    void testJdbcConfigurationResolution() {
        assertNotNull(DBConnection.getJdbcUrl());
        assertNotNull(DBConnection.getDbUser());
        assertNotNull(DBConnection.getDbPassword());

        assertTrue(DBConnection.getJdbcUrl().contains("smart_traffic") || DBConnection.getJdbcUrl().contains("mysql"));
    }

    @Test
    @DisplayName("DBConnection should handle unreachable database gracefully with clear diagnostics")
    void testConnectionGracefulFailureWhenOffline() {
        boolean isConnected = DBConnection.testConnection();
        // Regardless of whether MySQL is online or offline, it must not throw an unhandled crash
        if (!isConnected) {
            SQLException ex = assertThrows(SQLException.class, DBConnection::getConnection);
            assertNotNull(ex.getMessage());
            assertTrue(ex.getMessage().contains("DB CONNECTION FAILURE") || ex.getMessage().contains("MySQL"),
                "Error message should provide clear diagnostics");
        }
    }

    @Test
    @DisplayName("TrafficDAO instance initialization")
    void testTrafficDaoInitialization() {
        TrafficDAO dao = new TrafficDAO();
        assertNotNull(dao);
    }

    @Test
    @DisplayName("DBConnection should support dynamic Wi-Fi / Hotspot custom connection configuration")
    void testDynamicWifiHotspotConnectionConfiguration() {
        // Save current state
        assertFalse(DBConnection.isCustomConnectionActive());

        // Configure custom peer connection for Student 2 over Wi-Fi
        DBConnection.setCustomConnection("192.168.43.100", 3306, "smart_traffic", "traffic_app", "SmartCity2026!");
        assertTrue(DBConnection.isCustomConnectionActive());
        assertTrue(DBConnection.getJdbcUrl().contains("192.168.43.100:3306/smart_traffic"));
        assertEquals("traffic_app", DBConnection.getDbUser());
        assertEquals("SmartCity2026!", DBConnection.getDbPassword());

        // Reset to default
        DBConnection.resetCustomConnection();
        assertFalse(DBConnection.isCustomConnectionActive());
    }
}
