package com.assignment.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.assignment.R;
import com.assignment.data.model.StopUiModel;
import com.assignment.databinding.ActivityMainBinding;
import com.assignment.worker.FetchStopsWorker;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnCameraIdleListener {

    private static final int PERMISSION_REQUEST_CODE = 1001;
    private static final String WORK_NAME = "fetch_stops";

    private GoogleMap googleMap;

    private StopsViewModel stopsViewModel;

    private StopsAdapter stopsAdapter;

    private FusedLocationProviderClient fusedLocationProviderClient;

    private ActivityMainBinding binding;

    private final Map<String, Marker> stopMarkers = new HashMap<>();
    private String selectedStopId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        stopsViewModel = new ViewModelProvider(this).get(StopsViewModel.class);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        binding.rvStops.setLayoutManager(new LinearLayoutManager(this));
        stopsAdapter = new StopsAdapter(stopUiModel -> {
            selectedStopId = stopUiModel.stop.getId();
            if (googleMap != null) {
                if (stopUiModel.stop.getLat() != null && stopUiModel.stop.getLng() != null) {
                    LatLng pos = new LatLng(stopUiModel.stop.getLat(), stopUiModel.stop.getLng());
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 16));

                    // show info window
                    Marker marker = stopMarkers.get(stopUiModel.stop.getId());
                    if (marker != null) {
                        marker.showInfoWindow();
                    }
                }
            }
        });

        binding.rvStops.setAdapter(stopsAdapter);

        binding.swipeRefresh.setOnRefreshListener(() -> stopsViewModel.refreshData());

        binding.btnRefresh.setOnClickListener(v -> stopsViewModel.refreshData());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        observeViewModel();
        scheduleWork();

        stopsViewModel.refreshData();
    }

    private void observeViewModel() {

        stopsViewModel.getIsLoading().observe(this, isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.swipeRefresh.setRefreshing(isLoading);
        });

        stopsViewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        stopsViewModel.getVisibleStops().observe(this, stops -> {
            stopsAdapter.submitList(stops);
            updateMapMarkers(stops);
        });
    }

    private void updateMapMarkers(List<StopUiModel> stops) {
        if (googleMap == null) return;

        googleMap.clear();
        stopMarkers.clear();

        for (StopUiModel model : stops) {
            if (model.stop.getLat() != null && model.stop.getLng() != null) {
                LatLng pos = new LatLng(model.stop.getLat(), model.stop.getLng());
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(pos)
                        .title(model.stop.getName())
                        .snippet(model.stop.getType());
                if ("metro".equalsIgnoreCase(model.stop.getType())) {
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                } else {
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                }
                Marker marker = googleMap.addMarker(markerOptions);
                if (marker != null) {
                    marker.setTag(model.stop.getId());
                    stopMarkers.put(model.stop.getId(), marker);
                }
            }
        }

        if (selectedStopId != null) {
            Marker marker = stopMarkers.get(selectedStopId);
            if (marker != null) {
                marker.showInfoWindow();
            }
        }
    }

    private void scheduleWork() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(FetchStopsWorker.class, 3, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.setOnCameraIdleListener(this);

        googleMap.setOnMarkerClickListener(marker -> {
            String stopId = (String) marker.getTag();
            selectedStopId = stopId;
            if (stopId != null) {
                scrollToStop(stopId);
            }
            return false;
        });

        googleMap.setOnMapClickListener(latLng -> selectedStopId = null);

        LatLng delhi = new LatLng(28.6139, 77.2090);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(delhi, 12));

        checkLocationPermission();

    }

    private void scrollToStop(String stopId) {
        List<StopUiModel> stops = stopsViewModel.getVisibleStops().getValue();
        if (stops != null) {
            for (int i = 0; i < stops.size(); i++) {
                if (stops.get(i).stop.getId().equals(stopId)) {
                    binding.rvStops.smoothScrollToPosition(i);
                    break;
                }
            }
        }

    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_CODE);
        } else {
            enableMyLocation();
        }
    }

    private void enableMyLocation() {
        if (googleMap != null) {
            try {
                googleMap.setMyLocationEnabled(true);
                fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
                    if (location != null) {
                        stopsViewModel.updateUserLocation(location);
                    }
                });
            } catch (SecurityException e) {
                Log.e("Stacktrace :", Objects.requireNonNull(e.getLocalizedMessage()));
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            } else {
                Toast.makeText(this, "Location permission denied. Distance will not be available.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onCameraIdle() {
        if (googleMap != null) {
            CameraPosition position = googleMap.getCameraPosition();
            LatLngBounds bounds = googleMap.getProjection().getVisibleRegion().latLngBounds;
            stopsViewModel.updateMapState(position.target, position.zoom, bounds);
        }
    }
}