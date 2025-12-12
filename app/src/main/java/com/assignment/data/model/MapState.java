package com.assignment.data.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

public class MapState {
    public final LatLng target;
    public final float zoom;

    public final LatLngBounds bounds;

    public MapState(LatLng target, float zoom, LatLngBounds bounds) {
        this.target = target;
        this.zoom = zoom;
        this.bounds = bounds;
    }
}
