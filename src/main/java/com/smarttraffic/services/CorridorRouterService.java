package com.smarttraffic.services;

import com.smarttraffic.models.CorridorReport;
import com.smarttraffic.models.EmergencyVehicle;
import com.smarttraffic.models.Intersection;
import com.smarttraffic.models.OverrideStatus;
import com.smarttraffic.models.PreemptionLog;
import com.smarttraffic.models.PriorityTier;
import com.smarttraffic.workers.CorridorWorker;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Core orchestration service for evaluating override conditions, coordinating
 * multithreaded corridor workers, and generating final transit reports.
 * Implements STAGE 6, STAGE 7, and STAGE 10 requirements.
 */
public class CorridorRouterService {

    private final PriorityCalculator priorityCalculator;

    public CorridorRouterService() {
        this.priorityCalculator = new PriorityCalculator();
    }

    public CorridorRouterService(PriorityCalculator priorityCalculator) {
        this.priorityCalculator = priorityCalculator;
    }

    /**
     * STAGE 6 Condition:
     * If ETA is strictly less than 2 minutes at a blocked intersection:
     * -> Trigger GREEN_CORRIDOR_OVERRIDE.
     * Note: ETA exactly equal to 2.0 minutes must NOT trigger the override.
     *
     * @param etaMinutes estimated time of arrival in minutes
     * @param isBlocked whether the intersection is currently blocked by cross-traffic
     * @return true only if etaMinutes < 2.0 and isBlocked is true
     */
    public boolean shouldTriggerGreenCorridorOverride(double etaMinutes, boolean isBlocked) {
        // Strictly less than 2 minutes and blocked
        return (etaMinutes < 2.0) && isBlocked;
    }

    /**
     * Executes the multithreaded green corridor simulation across consecutive intersections.
     * Implements STAGE 10:
     * - Uses Java ExecutorService for multithreading
     * - Launches separate background worker threads for consecutive intersections
     * - Demonstrates parallel signal switching
     * - Waits for worker completion before building reports
     *
     * @param vehicle emergency vehicle requesting passage
     * @param intersections consecutive intersections along the route
     * @return CorridorSimulationResult containing preemption logs and final report
     */
    public CorridorSimulationResult simulateCorridor(EmergencyVehicle vehicle, List<Intersection> intersections) {
        if (vehicle == null || intersections == null || intersections.isEmpty()) {
            throw new IllegalArgumentException("Vehicle and intersections list must not be empty");
        }

        // 1. Calculate Priority Score
        double score = priorityCalculator.calculateScore(vehicle);

        // 2. Setup thread pool for parallel worker execution
        int poolSize = Math.min(intersections.size(), 8);
        ExecutorService executor = Executors.newFixedThreadPool(poolSize, r -> {
            Thread t = new Thread(r);
            t.setName(String.format("CorridorWorker-%02d", t.threadId()));
            t.setDaemon(true);
            return t;
        });

        List<Future<PreemptionLog>> futures = new ArrayList<>();
        List<PreemptionLog> collectedLogs = new ArrayList<>();

        int seq = 1;
        for (Intersection intersection : intersections) {
            // Check override condition for each intersection along corridor
            boolean triggerOverride = shouldTriggerGreenCorridorOverride(vehicle.getEtaMinutes(), intersection.isBlocked());
            CorridorWorker worker = new CorridorWorker(vehicle, intersection, triggerOverride, seq++);
            futures.add(executor.submit(worker));
        }

        // Wait for all workers to finish
        int overriddenCount = 0;
        int totalTransitSec = 0;
        boolean allOverridesSucceeded = true;

        for (Future<PreemptionLog> future : futures) {
            try {
                PreemptionLog log = future.get(10, TimeUnit.SECONDS);
                collectedLogs.add(log);

                if (log.isOverrideTriggered()) {
                    if (log.getOverrideStatus() == OverrideStatus.SUCCESS) {
                        overriddenCount++;
                    } else {
                        allOverridesSucceeded = false;
                    }
                }
                totalTransitSec += log.getTransitDurationSec();

            } catch (Exception e) {
                allOverridesSucceeded = false;
                PreemptionLog errLog = new PreemptionLog(
                    vehicle.getVehicleId(),
                    "UNKNOWN",
                    vehicle.getEtaMinutes(),
                    false,
                    OverrideStatus.FAILED,
                    null,
                    null,
                    0,
                    0,
                    Thread.currentThread().getName(),
                    "Execution exception in worker: " + e.getMessage()
                );
                collectedLogs.add(errLog);
            }
        }

        // Shutdown executor cleanly
        executor.shutdown();
        try {
            if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        // 3. Assign Tier according to STAGE 7
        boolean overallOverrideSuccess = (overriddenCount > 0) && allOverridesSucceeded;
        if (overriddenCount == 0 && !intersections.get(0).isBlocked()) {
            overallOverrideSuccess = true; // No override needed because path was already clear
        }
        PriorityTier tier = priorityCalculator.assignTier(vehicle, overallOverrideSuccess);

        // 4. Calculate Mathematical Evaluation Formulas: ETE & CMI
        double speedMs = Math.max(20.0, vehicle.getSpeedKmh()) * (1000.0 / 3600.0);
        double totalDistanceMeters = intersections.size() * 500.0;
        double uncongestedTimeSec = Math.max(30.0, totalDistanceMeters / speedMs);
        double ete = priorityCalculator.calculateEmergencyTransitEfficiency(uncongestedTimeSec, totalTransitSec);

        int totalIntersectionDelay = 0;
        for (Intersection inter : intersections) {
            totalIntersectionDelay += inter.getClearanceDelaySec();
        }
        double standardCycleSec = intersections.size() * 60.0;
        double cmi = priorityCalculator.calculateCongestionMitigationIndex(totalIntersectionDelay, standardCycleSec);

        // 5. Build summary text
        String summary = String.format(
            "Corridor completed for %s (%s). Score: %.2f | Tier: %s | Transit: %ds | ETE: %.1f%% | CMI: %.1f%% | Overridden: %d/%d.",
            vehicle.getVehicleId(), vehicle.getCategory().getDisplayName(), score, tier.getLabel(),
            totalTransitSec, ete, cmi, overriddenCount, intersections.size()
        );

        String reportId = "REP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        CorridorReport report = new CorridorReport(
            reportId,
            vehicle.getVehicleId(),
            vehicle.getCategory(),
            score,
            tier,
            intersections.size(),
            overriddenCount,
            totalTransitSec,
            ete,
            cmi,
            overallOverrideSuccess ? "COMPLETED" : "ABORTED",
            summary
        );

        return new CorridorSimulationResult(report, collectedLogs);
    }

    /**
     * Container holding the execution artifacts of a corridor run.
     */
    public static class CorridorSimulationResult {
        private final CorridorReport report;
        private final List<PreemptionLog> logs;

        public CorridorSimulationResult(CorridorReport report, List<PreemptionLog> logs) {
            this.report = report;
            this.logs = logs;
        }

        public CorridorReport getReport() {
            return report;
        }

        public List<PreemptionLog> getLogs() {
            return logs;
        }
    }
}
