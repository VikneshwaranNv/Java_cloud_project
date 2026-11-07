package com.smarttraffic.models;

/**
 * Enumeration representing the emergency vehicle categories.
 */
public enum VehicleCategory {
    AMBULANCE("Ambulance", 3.0),
    FIRE_ENGINE("Fire Engine", 5.0),
    POLICE_VIP("Police VIP Convoy", 2.0);

    private final String displayName;
    private final double maxDelayMinutes;

    VehicleCategory(String displayName, double maxDelayMinutes) {
        this.displayName = displayName;
        this.maxDelayMinutes = maxDelayMinutes;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getMaxDelayMinutes() {
        return maxDelayMinutes;
    }

    public static VehicleCategory fromString(String categoryStr) {
        if (categoryStr == null) {
            throw new IllegalArgumentException("Category string cannot be null");
        }
        String clean = categoryStr.trim().toUpperCase().replace(" ", "_");
        for (VehicleCategory cat : values()) {
            if (cat.name().equals(clean) || cat.displayName.equalsIgnoreCase(categoryStr.trim())) {
                return cat;
            }
        }
        throw new IllegalArgumentException("Unknown vehicle category: " + categoryStr);
    }
}
