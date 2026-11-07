package com.smarttraffic.models;

import java.time.LocalDateTime;

/**
 * Model class summarizing the overall green corridor routing for an emergency vehicle.
 */
public class CorridorReport {
    private String reportId;
    private String vehicleId;
    private VehicleCategory category;
    private double priorityScore;
    private PriorityTier priorityTier;
    private int totalIntersections;
    private int overriddenIntersections;
    private int totalTransitSeconds;
    private String corridorStatus;
    private String summaryText;
    private LocalDateTime generatedAt;

    public CorridorReport() {
        this.corridorStatus = "COMPLETED";
        this.generatedAt = LocalDateTime.now();
    }

    public CorridorReport(String reportId, String vehicleId, VehicleCategory category,
                          double priorityScore, PriorityTier priorityTier,
                          int totalIntersections, int overriddenIntersections,
                          int totalTransitSeconds, String corridorStatus, String summaryText) {
        this.reportId = reportId;
        this.vehicleId = vehicleId;
        this.category = category;
        this.priorityScore = priorityScore;
        this.priorityTier = priorityTier;
        this.totalIntersections = totalIntersections;
        this.overriddenIntersections = overriddenIntersections;
        this.totalTransitSeconds = totalTransitSeconds;
        this.corridorStatus = corridorStatus;
        this.summaryText = summaryText;
        this.generatedAt = LocalDateTime.now();
    }

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public VehicleCategory getCategory() {
        return category;
    }

    public void setCategory(VehicleCategory category) {
        this.category = category;
    }

    public double getPriorityScore() {
        return priorityScore;
    }

    public void setPriorityScore(double priorityScore) {
        this.priorityScore = priorityScore;
    }

    public PriorityTier getPriorityTier() {
        return priorityTier;
    }

    public void setPriorityTier(PriorityTier priorityTier) {
        this.priorityTier = priorityTier;
    }

    public int getTotalIntersections() {
        return totalIntersections;
    }

    public void setTotalIntersections(int totalIntersections) {
        this.totalIntersections = totalIntersections;
    }

    public int getOverriddenIntersections() {
        return overriddenIntersections;
    }

    public void setOverriddenIntersections(int overriddenIntersections) {
        this.overriddenIntersections = overriddenIntersections;
    }

    public int getTotalTransitSeconds() {
        return totalTransitSeconds;
    }

    public void setTotalTransitSeconds(int totalTransitSeconds) {
        this.totalTransitSeconds = totalTransitSeconds;
    }

    public String getCorridorStatus() {
        return corridorStatus;
    }

    public void setCorridorStatus(String corridorStatus) {
        this.corridorStatus = corridorStatus;
    }

    public String getSummaryText() {
        return summaryText;
    }

    public void setSummaryText(String summaryText) {
        this.summaryText = summaryText;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    @Override
    public String toString() {
        return String.format(
            "CorridorReport[reportId=%s, vehicleId=%s, score=%.2f, tier=%s, overridden=%d/%d, transit=%ds, status=%s]",
            reportId, vehicleId, priorityScore, priorityTier != null ? priorityTier.getLabel() : "N/A",
            overriddenIntersections, totalIntersections, totalTransitSeconds, corridorStatus
        );
    }
}
