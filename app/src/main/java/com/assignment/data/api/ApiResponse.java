package com.assignment.data.api;

import com.assignment.data.model.Stop;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ApiResponse {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("stops")
    private List<Stop> stops;


    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public List<Stop> getStops() {
        return stops;
    }
}
