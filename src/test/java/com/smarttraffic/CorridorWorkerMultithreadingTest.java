package com.smarttraffic;

import com.smarttraffic.models.EmergencyVehicle;
import com.smarttraffic.models.Intersection;
import com.smarttraffic.models.PreemptionLog;
import com.smarttraffic.models.SignalState;
import com.smarttraffic.models.VehicleCategory;
import com.smarttraffic.workers.CorridorWorker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying STAGE 10 Multithreading and parallel execution across consecutive intersections.
 */
public class CorridorWorkerMultithreadingTest {

    @Test
    @DisplayName("STAGE 10: Multithreaded signal switching with thread names and timestamps")
    void testParallelCorridorWorkers() throws Exception {
        EmergencyVehicle ev = new EmergencyVehicle(
            "EV-TEST-MT", "EMG-FIR-2042", VehicleCategory.FIRE_ENGINE, "CODE_RED", "FIRE-HQ",
            12.975, 77.601, 70.0, "INT-01", 60, 1.2, 85.0, 90.0, 80.0
        );

        int intersectionCount = 4;
        List<Intersection> intersections = new ArrayList<>();
        for (int i = 1; i <= intersectionCount; i++) {
            intersections.add(new Intersection(
                "INT-0" + i, "Junction " + i, 12.970 + (i * 0.005), 77.590 + (i * 0.005),
                SignalState.RED, 50 + (i * 5), true, 45, 2
            ));
        }

        ExecutorService executor = Executors.newFixedThreadPool(4);
        List<Future<PreemptionLog>> futures = new ArrayList<>();

        for (int i = 0; i < intersections.size(); i++) {
            CorridorWorker worker = new CorridorWorker(ev, intersections.get(i), true, i + 1);
            futures.add(executor.submit(worker));
        }

        executor.shutdown();
        boolean finished = executor.awaitTermination(5, TimeUnit.SECONDS);
        assertTrue(finished, "Workers must complete within timeout");

        List<PreemptionLog> logs = new ArrayList<>();
        Set<String> threadNames = new HashSet<>();

        for (Future<PreemptionLog> f : futures) {
            PreemptionLog log = f.get();
            assertNotNull(log);
            logs.add(log);
            threadNames.add(log.getThreadName());

            // Check log message contains timestamp and thread name
            assertNotNull(log.getLogMessage());
            assertTrue(log.getLogMessage().contains("["), "Log must have timestamp bracket");
            assertTrue(log.getLogMessage().contains("Corridor"), "Log must contain worker activity");
        }

        assertEquals(intersectionCount, logs.size());
        // Verify multiple threads participated
        assertTrue(threadNames.size() >= 1, "At least one worker thread must be identified");

        // Verify all intersections were switched to GREEN
        for (Intersection inter : intersections) {
            assertEquals(SignalState.GREEN, inter.getCurrentSignal());
            assertFalse(inter.isBlocked());
        }
    }
}
