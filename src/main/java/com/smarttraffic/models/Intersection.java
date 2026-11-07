package com.smarttraffic.models;

/**
 * Model class representing a traffic intersection in the corridor.
 */
public class Intersection {
    private String intersectionId;
    private String name;
    private double latitude;
    private double longitude;
    private SignalState currentSignal;
    private int cameraDensity;
    private boolean blocked;
    private int greenDurationSec;
    private int clearanceDelaySec;

    public Intersection() {
        this.currentSignal = SignalState.RED;
        this.blocked = true;
        this.greenDurationSec = 45;
        this.clearanceDelaySec = 5;
    }

    public Intersection(String intersectionId, String name, double latitude, double longitude,
                        SignalState currentSignal, int cameraDensity, boolean blocked,
                        int greenDurationSec, int clearanceDelaySec) {
        this.intersectionId = intersectionId;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.currentSignal = currentSignal;
        this.cameraDensity = cameraDensity;
        this.blocked = blocked;
        this.greenDurationSec = greenDurationSec;
        this.clearanceDelaySec = clearanceDelaySec;
    }

    public String getIntersectionId() {
        return intersectionId;
    }

    public void setIntersectionId(String intersectionId) {
        this.intersectionId = intersectionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public SignalState getCurrentSignal() {
        return currentSignal;
    }

    public synchronized void setCurrentSignal(SignalState currentSignal) {
        this.currentSignal = currentSignal;
    }

    public int getCameraDensity() {
        return cameraDensity;
    }

    public void setCameraDensity(int cameraDensity) {
        this.cameraDensity = cameraDensity;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public synchronized void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public int getGreenDurationSec() {
        return greenDurationSec;
    }

    public void setGreenDurationSec(int greenDurationSec) {
        this.greenDurationSec = greenDurationSec;
    }

    public int getClearanceDelaySec() {
        return clearanceDelaySec;
    }

    public void setClearanceDelaySec(int clearanceDelaySec) {
        this.clearanceDelaySec = clearanceDelaySec;
    }

    @Override
    public String toString() {
        return String.format(
            "Intersection[id=%s, name='%s', signal=%s, blocked=%b, density=%d, green=%ds, clearance=%ds]",
            intersectionId, name, currentSignal, blocked, cameraDensity, greenDurationSec, clearanceDelaySec
        );
    }
}
