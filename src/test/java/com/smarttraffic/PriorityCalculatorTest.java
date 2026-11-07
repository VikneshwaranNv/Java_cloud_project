package com.smarttraffic;

import com.smarttraffic.models.EmergencyVehicle;
import com.smarttraffic.models.PriorityTier;
import com.smarttraffic.models.VehicleCategory;
import com.smarttraffic.services.PriorityCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for STAGE 4 Priority Scoring formulas and STAGE 7 Priority Tiers.
 */
public class PriorityCalculatorTest {

    private PriorityCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new PriorityCalculator();
    }

    @Test
    @DisplayName("Ambulance: Score = (ETA Precision * 0.50) + (Queue Density * 0.30) + (Patient Criticality * 0.20)")
    void testAmbulanceFormula() {
        // ETA Precision = 95, Queue Density = 85, Patient Criticality = 90
        // Expected: (95 * 0.50) + (85 * 0.30) + (90 * 0.20) = 47.5 + 25.5 + 18.0 = 91.0
        double score = calculator.calculateAmbulanceScore(95.0, 85.0, 90.0);
        assertEquals(91.0, score, 0.001);

        EmergencyVehicle ev = new EmergencyVehicle();
        ev.setCategory(VehicleCategory.AMBULANCE);
        ev.setParam1(95.0);
        ev.setParam2(85.0);
        ev.setParam3(90.0);

        double calcScore = calculator.calculateScore(ev);
        assertEquals(91.0, calcScore, 0.001);
    }

    @Test
    @DisplayName("Fire Engine: Score = (Queue Density * 0.40) + (Weight Clearance * 0.30) + (ETA Precision * 0.30)")
    void testFireEngineFormula() {
        // Queue Density = 75, Weight Clearance = 92, ETA Precision = 88
        // Expected: (75 * 0.40) + (92 * 0.30) + (88 * 0.30) = 30.0 + 27.6 + 26.4 = 84.0
        double score = calculator.calculateFireEngineScore(75.0, 92.0, 88.0);
        assertEquals(84.0, score, 0.001);

        EmergencyVehicle ev = new EmergencyVehicle();
        ev.setCategory(VehicleCategory.FIRE_ENGINE);
        ev.setParam1(75.0);
        ev.setParam2(92.0);
        ev.setParam3(88.0);

        double calcScore = calculator.calculateScore(ev);
        assertEquals(84.0, calcScore, 0.001);
    }

    @Test
    @DisplayName("Police VIP Convoy: Score = (Signal Synchronization * 0.50) + (Security Clearance * 0.30) + (Speed Maintenance * 0.20)")
    void testPoliceVipFormula() {
        // Synchronization = 90, Clearance = 95, Speed = 85
        // Expected: (90 * 0.50) + (95 * 0.30) + (85 * 0.20) = 45.0 + 28.5 + 17.0 = 90.5
        double score = calculator.calculatePoliceVipScore(90.0, 95.0, 85.0);
        assertEquals(90.5, score, 0.001);

        EmergencyVehicle ev = new EmergencyVehicle();
        ev.setCategory(VehicleCategory.POLICE_VIP);
        ev.setParam1(90.0);
        ev.setParam2(95.0);
        ev.setParam3(85.0);

        double calcScore = calculator.calculateScore(ev);
        assertEquals(90.5, calcScore, 0.001);
    }

    @Test
    @DisplayName("STAGE 7: Tier evaluation boundaries")
    void testPriorityTiers() {
        // Gold Corridor: 90-100
        assertEquals(PriorityTier.GOLD_CORRIDOR, PriorityTier.evaluate(90.0, true));
        assertEquals(PriorityTier.GOLD_CORRIDOR, PriorityTier.evaluate(95.5, true));
        assertEquals(PriorityTier.GOLD_CORRIDOR, PriorityTier.evaluate(100.0, true));

        // Standard Clearance: 75-89
        assertEquals(PriorityTier.STANDARD_CLEARANCE, PriorityTier.evaluate(75.0, true));
        assertEquals(PriorityTier.STANDARD_CLEARANCE, PriorityTier.evaluate(84.0, true));
        assertEquals(PriorityTier.STANDARD_CLEARANCE, PriorityTier.evaluate(89.9, true));

        // Minor Traffic Delay: 60-74
        assertEquals(PriorityTier.MINOR_TRAFFIC_DELAY, PriorityTier.evaluate(60.0, true));
        assertEquals(PriorityTier.MINOR_TRAFFIC_DELAY, PriorityTier.evaluate(68.5, true));
        assertEquals(PriorityTier.MINOR_TRAFFIC_DELAY, PriorityTier.evaluate(74.9, true));

        // Route Blocked: below 60
        assertEquals(PriorityTier.ROUTE_BLOCKED, PriorityTier.evaluate(59.9, true));
        assertEquals(PriorityTier.ROUTE_BLOCKED, PriorityTier.evaluate(30.0, true));

        // Route Blocked on failed override even if score is high
        assertEquals(PriorityTier.ROUTE_BLOCKED, PriorityTier.evaluate(95.0, false));
    }

    @Test
    @DisplayName("Section 4: Emergency Transit Efficiency (ETE) formula")
    void testEmergencyTransitEfficiency() {
        // ETE = (UncongestedEstimatedTime / ActualEmergencyTransitTime) * 100
        // e.g. Uncongested = 120s, Actual = 150s -> (120 / 150) * 100 = 80.0%
        double ete = calculator.calculateEmergencyTransitEfficiency(120.0, 150.0);
        assertEquals(80.0, ete, 0.001);

        // Guard test: actual time <= 0
        assertEquals(0.0, calculator.calculateEmergencyTransitEfficiency(120.0, 0.0), 0.001);
    }

    @Test
    @DisplayName("Section 4: Congestion Mitigation Index (CMI) formula")
    void testCongestionMitigationIndex() {
        // CMI = 100 - (IntersectionDelayTime / StandardSignalCycle) * 100
        // e.g. Delay = 15s, Standard Cycle = 100s -> 100 - (15 / 100) * 100 = 85.0%
        double cmi = calculator.calculateCongestionMitigationIndex(15.0, 100.0);
        assertEquals(85.0, cmi, 0.001);

        // Zero delay: CMI = 100.0%
        assertEquals(100.0, calculator.calculateCongestionMitigationIndex(0.0, 90.0), 0.001);
    }
}
