package com.assignment.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.assignment.data.model.Stop;

import java.util.List;

@Dao
public interface StopDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertStops(List<Stop> stops);

    @Query("SELECT * FROM stops")
    LiveData<List<Stop>> getAllStops();
}
