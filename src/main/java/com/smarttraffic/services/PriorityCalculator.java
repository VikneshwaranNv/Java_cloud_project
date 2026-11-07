package com.smarttraffic.services;

import com.smarttraffic.models.EmergencyVehicle;
import com.smarttraffic.models.PriorityTier;
import com.smarttraffic.models.VehicleCategory;
import com.smarttraffic.utils.ValidationUtils;

/**
 * Service for calculating vehicle priority scores and assigning priority tiers.
 * Implements the formulas specified in STAGE 4 and tiers in STAGE 7.
 */
public class PriorityCalculator {

    /**
     * Calculates the priority score for an emergency vehicle based on its category
     * and normalized 0-100 parameters.
     *
     * @param vehicle the emergency vehicle with category and parameters
     * @return priority score strictly between 0.0 and 100.0
     */
    public double calculateScore(EmergencyVehicle vehicle) {
        if (vehicle == null || vehicle.getCategory() == null) {
            throw new IllegalArgumentException("Vehicle and vehicle category must not be null");
        }

        double score;
        VehicleCategory category = vehicle.getCategory();

        switch (category) {
            case AMBULANCE:
                score = calculateAmbulanceScore(vehicle.getParam1(), vehicle.getParam2(), vehicle.getParam3());
                break;
            case FIRE_ENGINE:
                score = calculateFireEngineScore(vehicle.getParam1(), vehicle.getParam2(), vehicle.getParam3());
                break;
            case POLICE_VIP:
                score = calculatePoliceVipScore(vehicle.getParam1(), vehicle.getParam2(), vehicle.getParam3());
                break;
            default:
                throw new IllegalArgumentException("Unsupported category: " + category);
        }

        // Clamp final score to 0.0 - 100.0
        score = ValidationUtils.clampTo100(score);
        vehicle.setPriorityScore(score);
        return score;
    }

    /**
     * STAGE 4: Ambulance Priority Score
     * Formula: Score = (ETA Precision × 0.50) + (Queue Density × 0.30) + (Patient Criticality × 0.20)
     * Max delay: 3 minutes
     */
    public double calculateAmbulanceScore(double etaPrecision, double queueDensity, double patientCriticality) {
        double p1 = ValidationUtils.clampTo100(etaPrecision);
        double p2 = ValidationUtils.clampTo100(queueDensity);
        double p3 = ValidationUtils.clampTo100(patientCriticality);
        return (p1 * 0.50) + (p2 * 0.30) + (p3 * 0.20);
    }

    /**
     * STAGE 4: Fire Engine Priority Score
     * Formula: Score = (Queue Density × 0.40) + (Weight Clearance × 0.30) + (ETA Precision × 0.30)
     * Max delay: 5 minutes
     */
    public double calculateFireEngineScore(double queueDensity, double weightClearance, double etaPrecision) {
        double p1 = ValidationUtils.clampTo100(queueDensity);
        double p2 = ValidationUtils.clampTo100(weightClearance);
        double p3 = ValidationUtils.clampTo100(etaPrecision);
        return (p1 * 0.40) + (p2 * 0.30) + (p3 * 0.30);
    }

    /**
     * STAGE 4: Police VIP Convoy Priority Score
     * Formula: Score = (Signal Synchronization × 0.50) + (Security Clearance × 0.30) + (Speed Maintenance × 0.20)
     * Max delay: 2 minutes
     */
    public double calculatePoliceVipScore(double signalSynchronization, double securityClearance, double speedMaintenance) {
        double p1 = ValidationUtils.clampTo100(signalSynchronization);
        double p2 = ValidationUtils.clampTo100(securityClearance);
        double p3 = ValidationUtils.clampTo100(speedMaintenance);
        return (p1 * 0.50) + (p2 * 0.30) + (p3 * 0.20);
    }

    /**
     * Assigns priority tier according to STAGE 7 specification:
     * - Gold Corridor: 90–100
     * - Standard Clearance: 75–89
     * - Minor Traffic Delay: 60–74
     * - Route Blocked: below 60 or failed override
     *
     * @param vehicle the vehicle with computed priority score
     * @param overrideSuccess whether the green corridor override succeeded
     * @return the assigned PriorityTier
     */
    public PriorityTier assignTier(EmergencyVehicle vehicle, boolean overrideSuccess) {
        double score = vehicle.getPriorityScore();
        PriorityTier tier = PriorityTier.evaluate(score, overrideSuccess);
        vehicle.setPriorityTier(tier);
        return tier;
    }

    /**
     * Section 4 Mathematical Formula:
     * Emergency Transit Efficiency (ETE) = (UncongestedEstimatedTime / ActualEmergencyTransitTime) * 100
     *
     * @param uncongestedEstimatedTimeSec uncongested estimated transit duration in seconds
     * @param actualEmergencyTransitTimeSec actual emergency transit duration in seconds
     * @return ETE percentage value
     */
    public double calculateEmergencyTransitEfficiency(double uncongestedEstimatedTimeSec, double actualEmergencyTransitTimeSec) {
        if (actualEmergencyTransitTimeSec <= 0) {
            return 0.0;
        }
        return (uncongestedEstimatedTimeSec / actualEmergencyTransitTimeSec) * 100.0;
    }

    /**
     * Section 4 Mathematical Formula:
     * Congestion Mitigation Index (CMI) = 100 - (IntersectionDelayTime / StandardSignalCycle) * 100
     *
     * @param intersectionDelayTimeSec total delay experienced at intersection in seconds
     * @param standardSignalCycleSec standard signal cycle length in seconds (e.g. 90s or 120s)
     * @return CMI percentage index
     */
    public double calculateCongestionMitigationIndex(double intersectionDelayTimeSec, double standardSignalCycleSec) {
        if (standardSignalCycleSec <= 0) {
            return 100.0;
        }
        double ratio = (intersectionDelayTimeSec / standardSignalCycleSec) * 100.0;
        return 100.0 - ratio;
    }
}
