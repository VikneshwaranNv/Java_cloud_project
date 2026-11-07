package com.smarttraffic;

import com.smarttraffic.models.EmergencyVehicle;
import com.smarttraffic.models.Intersection;
import com.smarttraffic.models.OverrideStatus;
import com.smarttraffic.models.PreemptionLog;
import com.smarttraffic.models.SignalState;
import com.smarttraffic.models.VehicleCategory;
import com.smarttraffic.services.CorridorRouterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for STAGE 6 Green Corridor Override condition and simulation service.
 */
public class CorridorRouterServiceTest {

    private CorridorRouterService routerService;

    @BeforeEach
    void setUp() {
        routerService = new CorridorRouterService();
    }

    @Test
    @DisplayName("STAGE 6: ETA < 2.0 at blocked intersection MUST trigger override")
    void testOverrideTriggeredWhenEtaLessThanTwoMinutes() {
        // ETA strictly less than 2.0 (e.g. 1.99, 1.4, 0.5)
        assertTrue(routerService.shouldTriggerGreenCorridorOverride(1.99, true));
        assertTrue(routerService.shouldTriggerGreenCorridorOverride(1.4, true));
        assertTrue(routerService.shouldTriggerGreenCorridorOverride(0.5, true));
    }

    @Test
    @DisplayName("STAGE 6: ETA exactly equal to 2.0 minutes must NOT trigger override")
    void testOverrideNotTriggeredWhenEtaEqualsTwoMinutes() {
        // Critical requirement: ETA == 2.0 must NOT trigger override
        assertFalse(routerService.shouldTriggerGreenCorridorOverride(2.0, true),
            "ETA exactly 2.0 minutes must NOT trigger GREEN_CORRIDOR_OVERRIDE");
    }

    @Test
    @DisplayName("STAGE 6: ETA > 2.0 minutes must NOT trigger override")
    void testOverrideNotTriggeredWhenEtaGreaterThanTwoMinutes() {
        assertFalse(routerService.shouldTriggerGreenCorridorOverride(2.01, true));
        assertFalse(routerService.shouldTriggerGreenCorridorOverride(3.5, true));
        assertFalse(routerService.shouldTriggerGreenCorridorOverride(5.0, true));
    }

    @Test
    @DisplayName("STAGE 6: Intersection not blocked must NOT trigger override")
    void testOverrideNotTriggeredWhenNotBlocked() {
        assertFalse(routerService.shouldTriggerGreenCorridorOverride(1.2, false));
    }

    @Test
    @DisplayName("Simulate corridor for emergency vehicle across multiple intersections")
    void testCorridorSimulation() {
        EmergencyVehicle ev = new EmergencyVehicle(
            "EV-TEST-1", "EMG-AMB-1001", VehicleCategory.AMBULANCE, "CRITICAL", "HOSP-01",
            12.97, 77.59, 65.0, "INT-01", 80, 1.5, 95.0, 85.0, 90.0
        );

        List<Intersection> corridor = new ArrayList<>();
        corridor.add(new Intersection("INT-01", "Boulevard 1", 12.971, 77.591, SignalState.RED, 80, true, 45, 5));
        corridor.add(new Intersection("INT-02", "Boulevard 2", 12.972, 77.592, SignalState.RED, 60, true, 45, 5));

        CorridorRouterService.CorridorSimulationResult result = routerService.simulateCorridor(ev, corridor);

        assertNotNull(result);
        assertNotNull(result.getReport());
        assertEquals(2, result.getLogs().size());
        assertEquals(2, result.getReport().getTotalIntersections());
        assertEquals(2, result.getReport().getOverriddenIntersections());

        for (PreemptionLog log : result.getLogs()) {
            assertTrue(log.isOverrideTriggered());
            assertEquals(OverrideStatus.SUCCESS, log.getOverrideStatus());
            assertEquals(SignalState.GREEN, log.getNewSignal());
            assertNotNull(log.getThreadName());
        }
    }
}
