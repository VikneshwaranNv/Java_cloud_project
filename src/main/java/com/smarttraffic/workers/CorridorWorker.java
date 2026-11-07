package com.smarttraffic.workers;

import com.smarttraffic.models.EmergencyVehicle;
import com.smarttraffic.models.Intersection;
import com.smarttraffic.models.OverrideStatus;
import com.smarttraffic.models.PreemptionLog;
import com.smarttraffic.models.SignalState;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Callable;

/**
 * Worker thread simulating green corridor preemption and signal switching
 * across consecutive intersections.
 * Implements STAGE 3, STAGE 6, and STAGE 10 requirements.
 */
public class CorridorWorker implements Callable<PreemptionLog> {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private final EmergencyVehicle vehicle;
    private final Intersection intersection;
    private final boolean shouldOverride;
    private final int sequenceOrder;

    public CorridorWorker(EmergencyVehicle vehicle, Intersection intersection, boolean shouldOverride, int sequenceOrder) {
        this.vehicle = vehicle;
        this.intersection = intersection;
        this.shouldOverride = shouldOverride;
        this.sequenceOrder = sequenceOrder;
    }

    @Override
    public PreemptionLog call() {
        String threadName = Thread.currentThread().getName();
        String timestamp = LocalTime.now().format(TIME_FORMATTER);

        SignalState prevSignal = intersection.getCurrentSignal();
        SignalState newSignal;
        OverrideStatus status;
        int clearanceDelay = intersection.getClearanceDelaySec();
        int transitSec = 0;
        StringBuilder logBuilder = new StringBuilder();

        try {
            logBuilder.append(String.format(
                "[%s] [%s] Starting Corridor Worker for Intersection %s (Sequence #%d) | Vehicle: %s (ETA: %.2f min)\n",
                timestamp, threadName, intersection.getIntersectionId(), sequenceOrder, vehicle.getVehicleId(), vehicle.getEtaMinutes()
            ));

            if (shouldOverride) {
                // STAGE 6 Simulation Steps:
                // 1. Conflicting signals turning RED
                logBuilder.append(String.format(
                    "[%s] [%s] -> STEP 1: Conflicting cross-traffic signals switched to RED at %s\n",
                    LocalTime.now().format(TIME_FORMATTER), threadName, intersection.getIntersectionId()
                ));

                // Simulate clearance delay before opening green wave
                Thread.sleep(Math.max(50, clearanceDelay * 15L));

                // 2. Emergency vehicle receiving a GREEN signal
                logBuilder.append(String.format(
                    "[%s] [%s] -> STEP 2: Emergency preemption activated! Signal turned GREEN for corridor at %s\n",
                    LocalTime.now().format(TIME_FORMATTER), threadName, intersection.getIntersectionId()
                ));
                intersection.setCurrentSignal(SignalState.GREEN);
                intersection.setBlocked(false);

                // 3. Consecutive intersections receiving green wave
                logBuilder.append(String.format(
                    "[%s] [%s] -> STEP 3: Green wave synchronized with adjacent corridor segment (Duration: %ds)\n",
                    LocalTime.now().format(TIME_FORMATTER), threadName, intersection.getGreenDurationSec()
                ));

                // 4. Calculate simulated transit duration
                double speed = Math.max(20.0, vehicle.getSpeedKmh());
                transitSec = (int) Math.round((500.0 / (speed * (1000.0 / 3600.0)))); // 500m segment transit
                transitSec += clearanceDelay;

                // 5. Successful override status
                newSignal = SignalState.GREEN;
                status = OverrideStatus.SUCCESS;

                logBuilder.append(String.format(
                    "[%s] [%s] -> STEP 4 & 5: Transit cleared in %ds. Status: %s\n",
                    LocalTime.now().format(TIME_FORMATTER), threadName, transitSec, status
                ));
            } else {
                // Override bypassed (e.g. ETA >= 2.0 or intersection not blocked)
                logBuilder.append(String.format(
                    "[%s] [%s] -> Override BYPASSED. Standard signal cycle maintained (ETA: %.2f min, Blocked: %b)\n",
                    LocalTime.now().format(TIME_FORMATTER), threadName, vehicle.getEtaMinutes(), intersection.isBlocked()
                ));
                newSignal = prevSignal;
                status = OverrideStatus.BYPASSED;
                transitSec = (int) Math.round(vehicle.getEtaMinutes() * 60.0);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            newSignal = prevSignal;
            status = OverrideStatus.FAILED;
            logBuilder.append(String.format(
                "[%s] [%s] -> ERROR: Worker interrupted at %s: %s\n",
                LocalTime.now().format(TIME_FORMATTER), threadName, intersection.getIntersectionId(), e.getMessage()
            ));
        } catch (Exception ex) {
            newSignal = prevSignal;
            status = OverrideStatus.FAILED;
            logBuilder.append(String.format(
                "[%s] [%s] -> ERROR: Worker encountered exception at %s: %s\n",
                LocalTime.now().format(TIME_FORMATTER), threadName, intersection.getIntersectionId(), ex.getMessage()
            ));
        }

        PreemptionLog log = new PreemptionLog(
            vehicle.getVehicleId(),
            intersection.getIntersectionId(),
            vehicle.getEtaMinutes(),
            shouldOverride,
            status,
            prevSignal,
            newSignal,
            clearanceDelay,
            transitSec,
            threadName,
            logBuilder.toString()
        );

        return log;
    }

    public Intersection getIntersection() {
        return intersection;
    }

    public EmergencyVehicle getVehicle() {
        return vehicle;
    }
}
