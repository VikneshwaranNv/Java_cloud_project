package com.smarttraffic.models;

import java.time.LocalDateTime;

/**
 * Model class for recording signal preemption actions on an intersection.
 */
public class PreemptionLog {
    private long logId;
    private String vehicleId;
    private String intersectionId;
    private double etaMinutes;
    private boolean overrideTriggered;
    private OverrideStatus overrideStatus;
    private SignalState previousSignal;
    private SignalState newSignal;
    private int clearanceDelaySec;
    private int transitDurationSec;
    private String threadName;
    private String logMessage;
    private LocalDateTime loggedAt;

    public PreemptionLog() {
        this.loggedAt = LocalDateTime.now();
    }

    public PreemptionLog(String vehicleId, String intersectionId, double etaMinutes,
                         boolean overrideTriggered, OverrideStatus overrideStatus,
                         SignalState previousSignal, SignalState newSignal,
                         int clearanceDelaySec, int transitDurationSec,
                         String threadName, String logMessage) {
        this.vehicleId = vehicleId;
        this.intersectionId = intersectionId;
        this.etaMinutes = etaMinutes;
        this.overrideTriggered = overrideTriggered;
        this.overrideStatus = overrideStatus;
        this.previousSignal = previousSignal;
        this.newSignal = newSignal;
        this.clearanceDelaySec = clearanceDelaySec;
        this.transitDurationSec = transitDurationSec;
        this.threadName = threadName;
        this.logMessage = logMessage;
        this.loggedAt = LocalDateTime.now();
    }

    public long getLogId() {
        return logId;
    }

    public void setLogId(long logId) {
        this.logId = logId;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getIntersectionId() {
        return intersectionId;
    }

    public void setIntersectionId(String intersectionId) {
        this.intersectionId = intersectionId;
    }

    public double getEtaMinutes() {
        return etaMinutes;
    }

    public void setEtaMinutes(double etaMinutes) {
        this.etaMinutes = etaMinutes;
    }

    public boolean isOverrideTriggered() {
        return overrideTriggered;
    }

    public void setOverrideTriggered(boolean overrideTriggered) {
        this.overrideTriggered = overrideTriggered;
    }

    public OverrideStatus getOverrideStatus() {
        return overrideStatus;
    }

    public void setOverrideStatus(OverrideStatus overrideStatus) {
        this.overrideStatus = overrideStatus;
    }

    public SignalState getPreviousSignal() {
        return previousSignal;
    }

    public void setPreviousSignal(SignalState previousSignal) {
        this.previousSignal = previousSignal;
    }

    public SignalState getNewSignal() {
        return newSignal;
    }

    public void setNewSignal(SignalState newSignal) {
        this.newSignal = newSignal;
    }

    public int getClearanceDelaySec() {
        return clearanceDelaySec;
    }

    public void setClearanceDelaySec(int clearanceDelaySec) {
        this.clearanceDelaySec = clearanceDelaySec;
    }

    public int getTransitDurationSec() {
        return transitDurationSec;
    }

    public void setTransitDurationSec(int transitDurationSec) {
        this.transitDurationSec = transitDurationSec;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public String getLogMessage() {
        return logMessage;
    }

    public void setLogMessage(String logMessage) {
        this.logMessage = logMessage;
    }

    public LocalDateTime getLoggedAt() {
        return loggedAt;
    }

    public void setLoggedAt(LocalDateTime loggedAt) {
        this.loggedAt = loggedAt;
    }

    @Override
    public String toString() {
        return String.format(
            "PreemptionLog[vId=%s, intId=%s, eta=%.2f min, override=%b, status=%s, %s->%s, transit=%ds, thread='%s']",
            vehicleId, intersectionId, etaMinutes, overrideTriggered, overrideStatus,
            previousSignal, newSignal, transitDurationSec, threadName
        );
    }
}
