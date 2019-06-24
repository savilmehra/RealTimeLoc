package com.easy_locater.realtimeloc;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.gcm.*;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;


public class NewsCheckingService extends GcmTaskService  implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    GoogleApiClient mLocationClient;
    LocationRequest mLocationRequest = new LocationRequest();
    private static final String TAG = NewsCheckingService.class.getSimpleName();

    public static final String GCM_ONEOFF_TAG = "oneoff|[0,0]";
    public static final String GCM_REPEAT_TAG = "repeat|[7200,1800]";

    @Override
    public void onInitializeTasks() {
        //called when app is updated to a new version, reinstalled etc.
        //you have to schedule your repeating tasks again
        super.onInitializeTasks();
    }


    @Override
    public int onRunTask(TaskParams taskParams) {
        //do some stuff (mostly network) - executed in background thread (async)
        mLocationClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        //obtain your data
        Bundle extras = taskParams.getExtras();

        Handler h = new Handler(getMainLooper());
        Log.v(TAG, "onRunTask");
        if(taskParams.getTag().equals(GCM_ONEOFF_TAG)) {
            h.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(NewsCheckingService.this, "ONEOFF executed", Toast.LENGTH_SHORT).show();
                }
            });
        } else if(taskParams.getTag().equals(GCM_REPEAT_TAG)) {
            h.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(NewsCheckingService.this, "ONEOFF executed", Toast.LENGTH_SHORT).show();
                    int priority = LocationRequest.PRIORITY_HIGH_ACCURACY; //by default
                    mLocationRequest.setPriority(priority);
                    mLocationClient.connect();
                }
            });
        }
        return GcmNetworkManager.RESULT_SUCCESS;
    }

    public static void scheduleOneOff(Context context) {
        //in this method, single OneOff task is scheduled (the target service that will be called is MyTaskService.class)
        Bundle data = new Bundle();
        data.putString("some key", "some budle data");
        try {
            OneoffTask oneoff = new OneoffTask.Builder()
                    //specify target service - must extend GcmTaskService
                    .setService(NewsCheckingService.class)
                    //tag that is unique to this task (can be used to cancel task)
                    .setTag(GCM_ONEOFF_TAG)
                    //executed between 0 - 10s from now
                    .setExecutionWindow(10, 10)
                    //set required network state, this line is optional
                    .setRequiredNetwork(Task.NETWORK_STATE_ANY)
                    //request that charging must be connected, this line is optional
                    .setRequiresCharging(false)
                    //set some data we want to pass to our task
                    .setExtras(data)
                    //if another task with same tag is already scheduled, replace it with this task
                    .setUpdateCurrent(true)
                    .build();
            GcmNetworkManager.getInstance(context).schedule(oneoff);
            Log.v(TAG, "oneoff task scheduled");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void scheduleRepeat(Context context) {
        //in this method, single Repeating task is scheduled (the target service that will be called is MyTaskService.class)
        try {
            PeriodicTask periodic = new PeriodicTask.Builder()
                    //specify target service - must extend GcmTaskService
                    .setService(NewsCheckingService.class)
                    //repeat every 60 seconds
                    .setPeriod(5)
                    //specify how much earlier the task can be executed (in seconds)
                    .setFlex(4)
                    //tag that is unique to this task (can be used to cancel task)
                    .setTag(GCM_REPEAT_TAG)
                    //whether the task persists after device reboot
                    .setPersisted(true)
                    //if another task with same tag is already scheduled, replace it with this task
                    .setUpdateCurrent(true)
                    //set required network state, this line is optional
                    .setRequiredNetwork(Task.NETWORK_STATE_ANY)
                    //request that charging must be connected, this line is optional
                    .setRequiresCharging(false)
                    .build();
            GcmNetworkManager.getInstance(context).schedule(periodic);
            Log.v(TAG, "repeating task scheduled");
        } catch (Exception e) {
            Log.e(TAG, "scheduling failed");
            e.printStackTrace();
        }
    }

    public static void cancelOneOff(Context context) {
        GcmNetworkManager
                .getInstance(context)
                .cancelTask(GCM_ONEOFF_TAG, NewsCheckingService.class);
    }

    public static void cancelRepeat(Context context) {
        GcmNetworkManager
                .getInstance(context)
                .cancelTask(GCM_REPEAT_TAG, NewsCheckingService.class);
    }

    public static void cancelAll(Context context) {
        GcmNetworkManager
                .getInstance(context)
                .cancelAllTasks(NewsCheckingService.class);
    }
    /*
     * LOCATION CALLBACKS
     */
    @Override
    public void onConnected(Bundle dataBundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mLocationClient, mLocationRequest, this);

        Log.d(TAG, "Connected to Google API");
    }

    /*
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Connection suspended");
    }

    //to get the location change
    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Location changed");


        if (location != null) {
            Log.d(TAG, "== location != null");
            DeBug.showLog("Service Started  new", "Got it"
            );
            //Send result to activities
            //  sendMessageToUI(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));
        }
    }



    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Failed to connect to Google API");

    }
}