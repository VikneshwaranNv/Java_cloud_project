package com.smarttraffic;

import com.smarttraffic.utils.ValidationUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for STAGE 8 String Processing and Beacon Regex validation.
 */
public class ValidationUtilsTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "EMG-AMB-1001",
        "EMG-FIR-2042",
        "EMG-POL-3099",
        "EMG-XYZ-0000",
        "EMG-ZZZ-9999"
    })
    @DisplayName("Should accept valid Beacon IDs matching EMG-[A-Z]{3}-\\d{4}")
    void testValidBeaconIds(String validId) {
        assertTrue(ValidationUtils.isValidBeaconId(validId), "Expected valid beacon ID: " + validId);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "EMG-amb-1001",       // lowercase letters
        "EMG-AM-1001",        // only 2 letters
        "EMG-AMBA-1001",      // 4 letters
        "EMG-AMB-123",        // only 3 digits
        "EMG-AMB-12345",      // 5 digits
        "AMB-1001",           // missing EMG prefix
        "EMG_AMB_1001",       // underscores instead of dashes
        "",                   // empty string
        "   "                 // whitespace
    })
    @DisplayName("Should reject invalid Beacon IDs")
    void testInvalidBeaconIds(String invalidId) {
        assertFalse(ValidationUtils.isValidBeaconId(invalidId), "Expected invalid beacon ID: " + invalidId);
    }

    @Test
    @DisplayName("Should reject null Beacon ID safely")
    void testNullBeaconId() {
        assertFalse(ValidationUtils.isValidBeaconId(null));
    }

    @Test
    @DisplayName("Should parse camera data using split(',') and trim whitespace")
    void testParseCameraData() {
        String raw = " INT-01 ,  84 , BLOCKED ";
        String[] parsed = ValidationUtils.parseCameraData(raw);

        assertEquals(3, parsed.length);
        assertEquals("INT-01", parsed[0]);
        assertEquals("84", parsed[1]);
        assertEquals("BLOCKED", parsed[2]);
    }

    @Test
    @DisplayName("Should sanitize location tags using trim()")
    void testSanitizeLocationTag() {
        assertEquals("Central Boulevard", ValidationUtils.sanitizeLocationTag("   Central Boulevard   \t"));
        assertEquals("", ValidationUtils.sanitizeLocationTag(null));
    }
}
