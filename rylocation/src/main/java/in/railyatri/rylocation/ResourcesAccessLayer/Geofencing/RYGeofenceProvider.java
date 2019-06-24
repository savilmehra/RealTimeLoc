package in.railyatri.rylocation.ResourcesAccessLayer.Geofencing;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.RequiresPermission;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;

import in.railyatri.rylocation.Persistent.TinyDB;

/**
 * Created by garvit on 8/9/16.
 */
public class RYGeofenceProvider implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {

    Context mContext;
    private GoogleApiClient mGoogleApiClient;
    private PendingIntent mGeofencePendingIntent = null;
    private static final String TAG = "RYGeofenceProvider";

    public RYGeofenceProvider(Context context) {
        this.mContext = context;
        buildApiClient();
    }

    private void buildApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            if (!mGoogleApiClient.isConnected() || !mGoogleApiClient.isConnecting())
                mGoogleApiClient.connect();
        }
    }

    /**
     * Adding geofences
     *
     * @param mGeofenceList
     */
    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    public boolean addGeofences(ArrayList<Geofence> mGeofenceList) {
        Log.d(TAG, "Add Geofence");
        if (!mGoogleApiClient.isConnected()) {
            buildApiClient();
            return false;
        }

        if (mGeofenceList == null || mGeofenceList.size() == 0)
            return false;

        try {
            LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, getGeofencingRequest(mGeofenceList), getGeofencePendingIntent()).setResultCallback(this);
        } catch (SecurityException securityException) {
            logSecurityException(securityException);
            return false;
        } catch (Exception exception) {
            Log.e(TAG, "Adding Geofence Exception: " + exception);
            return false;
        }
        return true;
    }


    /**
     * Removing geofences
     */
    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    public boolean removeGeofences() {
        Log.d(TAG, "Remove Geofence");
        if (!mGoogleApiClient.isConnected()) {
            buildApiClient();
            return false;
        }

        try {
            LocationServices.GeofencingApi.removeGeofences(
                    mGoogleApiClient,
                    getGeofencePendingIntent()
            ).setResultCallback(this);
        } catch (SecurityException securityException) {
            logSecurityException(securityException);
            return false;
        } catch (Exception exception) {
            Log.e(TAG, "Remove Geofence Exception: " + exception);
            return false;
        }
        return true;
    }

    private void logSecurityException(SecurityException securityException) {
        Log.e(TAG, "Invalid location permission. " +
                "You need to use ACCESS_FINE_LOCATION with geofences", securityException);
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(mContext, RYGeofenceTransitionsIntentService.class);
        return PendingIntent.getService(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private GeofencingRequest getGeofencingRequest(ArrayList<Geofence> mGeofenceList) {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL);

        // Add the geofences to be monitored by geofencing service.
        builder.addGeofences(mGeofenceList);

        // Return a GeofencingRequest.
        return builder.build();
    }


    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "Connected to GoogleApiClient");
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "Connection suspended");
    }

    /**
     * @param status The Status returned through a PendingIntent when addGeofences() or
     *               removeGeofences() get called.
     */
    public void onResult(Status status) {
        if (status.isSuccess()) {
            if (!new TinyDB(mContext).getBoolean("isGeofenceAdded"))
                new TinyDB(mContext).putBoolean("isGeofenceAdded", true);
            else
                new TinyDB(mContext).putBoolean("isGeofenceAdded", false);
        } else {
            String errorMessage = RYGeofenceErrorMessages.getErrorString(mContext,
                    status.getStatusCode());
            new TinyDB(mContext).putBoolean("isGeofenceAdded", false);
            Log.e(TAG, errorMessage);
        }
    }


}
