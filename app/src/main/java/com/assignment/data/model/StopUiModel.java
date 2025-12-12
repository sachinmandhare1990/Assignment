package com.assignment.data.model;

import java.util.Locale;

public class StopUiModel {
    public Stop stop;
    public float distanceFromUser = -1;
    public float distanceFromMapCenter = -1;

    public StopUiModel(Stop stop) {
        this.stop = stop;
    }

    public String getDistanceText() {
        if (distanceFromUser < 0) return "Location N/A";
        if (distanceFromUser < 1000) {
            return String.format(Locale.getDefault(), "%.0f m", distanceFromUser);
        } else {
            return String.format(Locale.getDefault(), "%.1f km", distanceFromUser / 1000);
        }
    }

}
