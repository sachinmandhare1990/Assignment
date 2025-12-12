package com.assignment.ui;

import android.app.Application;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.assignment.AssignmentApp;
import com.assignment.data.model.MapState;
import com.assignment.data.model.Stop;
import com.assignment.data.model.StopUiModel;
import com.assignment.data.repository.StopsRepository;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StopsViewModel extends AndroidViewModel {
    private final StopsRepository repository;
    private final LiveData<List<Stop>> allStops;

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>(null);

    private final MutableLiveData<MapState> mapState = new MutableLiveData<>();

    private final MediatorLiveData<List<StopUiModel>> visibleStops = new MediatorLiveData<>();

    private Location userLocation;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();


    public StopsViewModel(@NonNull Application application) {
        super(application);
        this.repository = ((AssignmentApp) application).stopsRepository;
        this.allStops = repository.getAllStops();

        visibleStops.addSource(allStops, stops -> {
            updateVisibleStops();
        });

        visibleStops.addSource(mapState, mapState -> {
            updateVisibleStops();
        });
    }

    public LiveData<List<StopUiModel>> getVisibleStops() {
        return visibleStops;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void refreshData() {
        isLoading.setValue(true);
        errorMessage.setValue(null);
        executorService.execute(() -> {
            boolean success = repository.fetchAndSaveStopsSync();
            isLoading.postValue(false);
            if (!success) {
                errorMessage.postValue("Failed to fetech data. Please try again.");
            }
        });
    }

    public void updateUserLocation(Location location) {
        this.userLocation = location;
        updateVisibleStops();
    }

    public void updateMapState(LatLng target, float zoom, LatLngBounds bounds) {
        mapState.setValue(new MapState(target, zoom, bounds));
    }

    private void updateVisibleStops() {
        List<Stop> stops = allStops.getValue();
        MapState state = mapState.getValue();

        if (stops == null || state == null) return;

        List<StopUiModel> filltered = new ArrayList<>();
        float zoom = state.zoom;
        LatLng target = state.target;
        LatLngBounds bounds = state.bounds;


        for (Stop stop : stops) {
            if (stop.getLat() == null || stop.getLng() == null) continue;

            boolean shouldAdd = false;
            if (zoom >= 15) {
                if (bounds.contains(new LatLng(stop.getLat(), stop.getLng()))) {
                    shouldAdd = true;
                }
            } else if (zoom >= 12) {
                float[] results = new float[1];
                Location.distanceBetween(target.latitude, target.longitude, stop.getLat(), stop.getLng(), results);
                if (results[0] < 5000) {
                    shouldAdd = true;
                }
            } else {
                shouldAdd = true;
            }

            if (shouldAdd) {
                filltered.add(createStopUiModel(stop, target));
            }
        }

        if (zoom < 12) {
            filltered.sort((o1, o2) -> Float.compare(o1.distanceFromMapCenter, o2.distanceFromMapCenter));
            if (filltered.size() > 30) {
                filltered = new ArrayList<>(filltered.subList(0, 30));
            }
        } else if (zoom < 15) {
            filltered.sort((o1, o2) -> Float.compare(o1.distanceFromMapCenter, o2.distanceFromMapCenter));
            if (filltered.size() > 100) {
                filltered = new ArrayList<>(filltered.subList(0, 100));
            }
        }

        if (userLocation != null) {
            for (StopUiModel model : filltered) {
                float[] results = new float[1];
                Location.distanceBetween(userLocation.getLatitude(), userLocation.getLongitude(), model.stop.getLat(), model.stop.getLng(), results);
                model.distanceFromUser = results[0];
            }
            filltered.sort((o1, o2) -> Float.compare(o1.distanceFromUser, o2.distanceFromUser));
        }
        visibleStops.setValue(filltered);
    }

    private StopUiModel createStopUiModel(Stop stop, LatLng target) {
        StopUiModel model = new StopUiModel(stop);

        if (userLocation != null && stop.getLat() != null && stop.getLng() != null) {
            float[] results = new float[1];
            Location.distanceBetween(userLocation.getLatitude(), userLocation.getLongitude(), stop.getLat(), stop.getLng(), results);
            model.distanceFromUser = results[0];
        } else {
            model.distanceFromUser = -1;
        }

        if (target != null && stop.getLat() != null && stop.getLng() != null) {
            float[] results = new float[1];
            Location.distanceBetween(target.latitude, target.longitude, stop.getLat(), stop.getLng(), results);
            model.distanceFromMapCenter = results[0];
        }
        return model;
    }

}

