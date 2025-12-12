package com.assignment.data.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;

import com.assignment.data.api.ApiResponse;
import com.assignment.data.api.ApiService;
import com.assignment.data.db.StopDao;
import com.assignment.data.model.Stop;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Response;

public class StopsRepository {

    private final StopDao stopDao;
    private final ApiService apiService;

    public StopsRepository(StopDao stopDao, ApiService apiService) {
        this.stopDao = stopDao;
        this.apiService = apiService;
    }

    public LiveData<List<Stop>> getAllStops() {
        return stopDao.getAllStops();
    }

    public boolean fetchAndSaveStopsSync() {
        try {
            Response<ApiResponse> busResponse = apiService.getBusStops().execute();
            Response<ApiResponse> metroResponse = apiService.getMetroStops().execute();

            if (busResponse.isSuccessful() && metroResponse.isSuccessful()) {
                List<Stop> allStops = new ArrayList<>();
                if (busResponse.body() != null) {
                    allStops.addAll(busResponse.body().getStops());
                }
                if (metroResponse.body() != null) {
                    allStops.addAll(metroResponse.body().getStops());
                }
                if (!allStops.isEmpty()) {
                    stopDao.insertStops(allStops);
                    return true;
                }
            }
        } catch (IOException e) {
            Log.e("Stacktrace", Objects.requireNonNull(e.getLocalizedMessage()));
        }
        return false;
    }

}
