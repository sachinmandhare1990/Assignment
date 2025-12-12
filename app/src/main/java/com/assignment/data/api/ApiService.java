package com.assignment.data.api;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {
    @GET("bus_stops.json")
    Call<ApiResponse> getBusStops();

    @GET("metro_stops.json")
    Call<ApiResponse> getMetroStops();

}
