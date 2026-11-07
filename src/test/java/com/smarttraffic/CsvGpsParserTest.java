package com.smarttraffic;

import com.smarttraffic.models.EmergencyVehicle;
import com.smarttraffic.models.VehicleCategory;
import com.smarttraffic.services.CsvGpsParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for STAGE 9 CSV parsing and validation.
 */
public class CsvGpsParserTest {

    @Test
    @DisplayName("Should successfully parse realistic emergency_gps.csv")
    void testParseEmergencyGpsCsv() throws IOException {
        CsvGpsParser parser = new CsvGpsParser();
        File file = new File("emergency_gps.csv");

        assertTrue(file.exists(), "emergency_gps.csv must exist in project root");

        List<EmergencyVehicle> vehicles = parser.parseCsv(file);
        assertNotNull(vehicles);
        assertFalse(vehicles.isEmpty(), "Should have parsed at least one vehicle");

        // Check categories represented
        boolean hasAmbulance = vehicles.stream().anyMatch(v -> v.getCategory() == VehicleCategory.AMBULANCE);
        boolean hasFireEngine = vehicles.stream().anyMatch(v -> v.getCategory() == VehicleCategory.FIRE_ENGINE);
        boolean hasPoliceVip = vehicles.stream().anyMatch(v -> v.getCategory() == VehicleCategory.POLICE_VIP);

        assertTrue(hasAmbulance, "CSV should contain ambulance records");
        assertTrue(hasFireEngine, "CSV should contain fire engine records");
        assertTrue(hasPoliceVip, "CSV should contain police VIP convoy records");

        for (EmergencyVehicle v : vehicles) {
            assertTrue(v.getSpeedKmh() > 0, "Speed must be positive");
            assertTrue(v.getEtaMinutes() > 0, "ETA must be positive");
            assertNotNull(v.getDestinationCode());
        }
    }

    @Test
    @DisplayName("Should skip invalid Beacon ID rows during parsing")
    void testSkipInvalidBeaconRows() throws IOException {
        File tempFile = File.createTempFile("test_invalid_beacon", ".csv");
        tempFile.deleteOnExit();

        try (FileWriter fw = new FileWriter(tempFile)) {
            fw.write("vehicle_id,beacon_id,category,severity_code,destination_code,latitude,longitude,speed_kmh,intersection_id,camera_density,eta_minutes,param1,param2,param3\n");
            // Valid row
            fw.write("EV-1,EMG-AMB-1001,AMBULANCE,CRITICAL,HOSP-1,12.97,77.59,60.0,INT-1,50,1.5,90,80,70\n");
            // Invalid beacon row (lowercase)
            fw.write("EV-2,emg-amb-1002,AMBULANCE,URGENT,HOSP-2,12.97,77.59,55.0,INT-2,40,2.1,80,70,60\n");
        }

        CsvGpsParser parser = new CsvGpsParser();
        List<EmergencyVehicle> list = parser.parseCsv(tempFile);

        assertEquals(1, list.size(), "Only the valid row should be accepted");
        assertEquals("EV-1", list.get(0).getVehicleId());
    }
}
