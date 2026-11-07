package com.smarttraffic.utils;

import java.util.regex.Pattern;

/**
 * Utility class for string processing, beacon regex validation, and input sanitization.
 * Implements Stage 8 requirements:
 * - Beacon ID validation using exact regex: EMG-[A-Z]{3}-\\d{4}
 * - Camera data parsing using split(",")
 * - Location tags using trim()
 */
public final class ValidationUtils {

    /**
     * Exact regex specification required by STAGE 8:
     * EMG-[A-Z]{3}-\\d{4}
     */
    public static final String BEACON_ID_REGEX = "^EMG-[A-Z]{3}-\\d{4}$";
    private static final Pattern BEACON_PATTERN = Pattern.compile(BEACON_ID_REGEX);

    private ValidationUtils() {
        // Prevent instantiation
    }

    /**
     * Validates whether a Beacon ID matches the required pattern: EMG-[A-Z]{3}-\\d{4}
     * Examples of valid: EMG-AMB-1001, EMG-FIR-2042, EMG-POL-3099
     * Examples of invalid: EMG-amb-1001, EMG-AM-1001, EMG-AMB-12, AMB-1001
     *
     * @param beaconId the beacon identifier to validate
     * @return true if matches, false otherwise
     */
    public static boolean isValidBeaconId(String beaconId) {
        if (beaconId == null) {
            return false;
        }
        return BEACON_PATTERN.matcher(beaconId.trim()).matches();
    }

    /**
     * Parses camera data stream using split(",") and sanitizes whitespace using trim().
     * Example input: " INT-01 , 84 , BLOCKED "
     *
     * @param cameraDataRaw comma-separated raw camera data
     * @return array of trimmed string tokens
     */
    public static String[] parseCameraData(String cameraDataRaw) {
        if (cameraDataRaw == null || cameraDataRaw.isEmpty()) {
            return new String[0];
        }
        String[] tokens = cameraDataRaw.split(",");
        String[] cleaned = new String[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            cleaned[i] = sanitizeLocationTag(tokens[i]);
        }
        return cleaned;
    }

    /**
     * Sanitizes location tags and text identifiers using trim().
     *
     * @param tag raw location tag or identifier
     * @return trimmed string or empty string if null
     */
    public static String sanitizeLocationTag(String tag) {
        return (tag == null) ? "" : tag.trim();
    }

    /**
     * Clamps a numeric value to the 0-100 range.
     *
     * @param value raw numeric input
     * @return clamped value between 0.0 and 100.0
     */
    public static double clampTo100(double value) {
        return Math.max(0.0, Math.min(100.0, value));
    }
}
