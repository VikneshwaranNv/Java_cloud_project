package com.smarttraffic.services;

import com.smarttraffic.models.EmergencyVehicle;
import com.smarttraffic.models.VehicleCategory;
import com.smarttraffic.utils.ValidationUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for parsing and validating emergency vehicle GPS data from CSV files.
 * Implements STAGE 8 and STAGE 9 requirements.
 */
public class CsvGpsParser {

    /**
     * Parses the CSV file containing emergency vehicle live telemetry.
     * Validates Beacon ID against the required regex pattern and ensures data integrity.
     *
     * @param csvFile file pointer to the CSV
     * @return List of parsed and validated EmergencyVehicle instances
     * @throws IOException if file cannot be read
     */
    public List<EmergencyVehicle> parseCsv(File csvFile) throws IOException {
        List<EmergencyVehicle> vehicles = new ArrayList<>();

        if (!csvFile.exists()) {
            throw new IOException("CSV file does not exist at: " + csvFile.getAbsolutePath());
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
            String line = reader.readLine(); // Header line
            if (line == null) {
                return vehicles;
            }

            int lineNum = 1;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                // Stage 8: Split camera/telemetry tokens
                String[] tokens = line.split(",");
                if (tokens.length < 14) {
                    System.err.printf("[CSV WARN] Line %d has insufficient columns (%d < 14). Skipping row.\n", lineNum, tokens.length);
                    continue;
                }

                try {
                    String vehicleId = ValidationUtils.sanitizeLocationTag(tokens[0]);
                    String beaconId = ValidationUtils.sanitizeLocationTag(tokens[1]);
                    String categoryStr = ValidationUtils.sanitizeLocationTag(tokens[2]);
                    String severity = ValidationUtils.sanitizeLocationTag(tokens[3]);
                    String destination = ValidationUtils.sanitizeLocationTag(tokens[4]);
                    double lat = Double.parseDouble(tokens[5].trim());
                    double lon = Double.parseDouble(tokens[6].trim());
                    double speed = Double.parseDouble(tokens[7].trim());
                    String intersectionId = ValidationUtils.sanitizeLocationTag(tokens[8]);
                    int cameraDensity = Integer.parseInt(tokens[9].trim());
                    double etaMinutes = Double.parseDouble(tokens[10].trim());
                    double p1 = Double.parseDouble(tokens[11].trim());
                    double p2 = Double.parseDouble(tokens[12].trim());
                    double p3 = Double.parseDouble(tokens[13].trim());

                    // Validate Beacon ID with exact regex: EMG-[A-Z]{3}-\\d{4}
                    if (!ValidationUtils.isValidBeaconId(beaconId)) {
                        System.err.printf("[CSV WARN] Line %d: Invalid Beacon ID '%s'. Must match 'EMG-[A-Z]{3}-\\d{4}'. Skipping.\n",
                            lineNum, beaconId);
                        continue;
                    }

                    VehicleCategory category = VehicleCategory.fromString(categoryStr);

                    EmergencyVehicle ev = new EmergencyVehicle(
                        vehicleId, beaconId, category, severity, destination,
                        lat, lon, speed, intersectionId, cameraDensity, etaMinutes,
                        p1, p2, p3
                    );

                    vehicles.add(ev);
                } catch (Exception ex) {
                    System.err.printf("[CSV ERROR] Line %d: Parsing failed: %s. Skipping row.\n", lineNum, ex.getMessage());
                }
            }
        }

        return vehicles;
    }
}
