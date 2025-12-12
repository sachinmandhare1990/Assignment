package com.assignment.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.assignment.AssignmentApp;
import com.assignment.data.repository.StopsRepository;

public class FetchStopsWorker extends Worker {
    public FetchStopsWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context applicationContext = getApplicationContext();
        StopsRepository repository = ((AssignmentApp) applicationContext).stopsRepository;

        boolean success = repository.fetchAndSaveStopsSync();
        if (success) {
            return Result.success();
        } else {
            return Result.retry();
        }
    }
}
