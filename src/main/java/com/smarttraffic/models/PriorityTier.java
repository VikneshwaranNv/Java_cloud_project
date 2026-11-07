package com.smarttraffic.models;

/**
 * Priority tiers assigned to emergency vehicles based on calculated scores
 * and override operational status.
 */
public enum PriorityTier {
    GOLD_CORRIDOR("Gold Corridor", 90.0, 100.0),
    STANDARD_CLEARANCE("Standard Clearance", 75.0, 89.99),
    MINOR_TRAFFIC_DELAY("Minor Traffic Delay", 60.0, 74.99),
    ROUTE_BLOCKED("Route Blocked", 0.0, 59.99);

    private final String label;
    private final double minScore;
    private final double maxScore;

    PriorityTier(String label, double minScore, double maxScore) {
        this.label = label;
        this.minScore = minScore;
        this.maxScore = maxScore;
    }

    public String getLabel() {
        return label;
    }

    public double getMinScore() {
        return minScore;
    }

    public double getMaxScore() {
        return maxScore;
    }

    /**
     * Determines priority tier according to STAGE 7 specification:
     * - Gold Corridor: 90-100
     * - Standard Clearance: 75-89
     * - Minor Traffic Delay: 60-74
     * - Route Blocked: below 60 OR failed override
     */
    public static PriorityTier evaluate(double score, boolean overrideSuccess) {
        if (!overrideSuccess || score < 60.0) {
            return ROUTE_BLOCKED;
        } else if (score >= 90.0) {
            return GOLD_CORRIDOR;
        } else if (score >= 75.0) {
            return STANDARD_CLEARANCE;
        } else {
            return MINOR_TRAFFIC_DELAY;
        }
    }
}
