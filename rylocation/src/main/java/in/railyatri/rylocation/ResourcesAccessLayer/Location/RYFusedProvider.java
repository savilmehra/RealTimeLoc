package in.railyatri.rylocation.ResourcesAccessLayer.Location;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

;


/**
 * Created by Saldi on 28/4/16.
 * Updated by Akshay on 24/10/2018.
 */
public abstract class RYFusedProvider /*implements LocationListener, GoogleApiClient.ConnectionCallbacks*/ {

    private static final String TAG = "RYLocationProvider";
    private LocationRequest mLocationRequest;
    private boolean mIsListening = false;
    private Context mContext;
    private RYLocationProperties mRYLocationProperties;
    //private GoogleApiClient mGoogleApiClient;
    private LocationCallback mLocationCallback;
    private FusedLocationProviderClient mFusedLocationClient;

    /**
     * Constructor initializes  with default properties
     *
     * @param context
     */
    public RYFusedProvider(@NonNull Context context) {
        this(context, RYLocationProperties.DEFAULT);
    }

    /**
     * Constructor initializes  based on properties given by Client
     *
     * @param context
     * @param RYLocationProperties
     */
    public RYFusedProvider(@NonNull Context context, @NonNull RYLocationProperties RYLocationProperties) {
        this.mContext = context;
        this.mRYLocationProperties = RYLocationProperties;

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(mRYLocationProperties.getRegularUpdateTime());
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setSmallestDisplacement(0);
        mLocationRequest.setPriority(mRYLocationProperties.getPriority());


        getLastLocation();
        setLocationCallback();
        startListen();

    }

    private void setLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    Log.i(TAG, "location update found");
                    onLocationFound(location);
                }
            }

            ;
        };
    }

    private void getLastLocation() {
        Log.i(TAG, "getting last known location");

        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            Log.i(TAG, "last known location found");
                            onLocationFound(location);
                        }
                    }
                });
    }

    /**
     * Start Listen for location updates
     */
    public final void startListen() {
        if (mLocationRequest == null) {
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(mRYLocationProperties.getRegularUpdateTime());
            mLocationRequest.setFastestInterval(0);
            mLocationRequest.setSmallestDisplacement(0);
            mLocationRequest.setPriority(mRYLocationProperties.getPriority());
        }
        /*if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                    .addConnectionCallbacks(this)
                    .addApi(LocationServices.API)
                    .build();
            if (!mGoogleApiClient.isConnected() || !mGoogleApiClient.isConnecting())
                mGoogleApiClient.connect();
        }*/
        if (!this.mIsListening) {
            //if (mGoogleApiClient.isConnected()) {
            Log.i(TAG, "listening for location updates");
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            try {
                LocationServices.getFusedLocationProviderClient(mContext).requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.getMainLooper());
            } catch (Exception e) {
                e.printStackTrace();
            }
            mIsListening = true;
            /*} else {
                Log.i(TAG, "api client not connected");
                mIsListening = false;
                startListen();
            }*/

        } else {
            Log.i(TAG, "Relax, Already listening for location updates");
        }
    }

    /**
     * This method can be used for stopping Location updates
     */
    public final void stopListen() {
        if (this.mIsListening) {
            Log.i(TAG, "Stopped listening for location updates");
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //Check self permission required for newer Android versions
                return;
            }
            try {
                LocationServices.getFusedLocationProviderClient(mContext).removeLocationUpdates(mLocationCallback);
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.mIsListening = false;
        } else {
            Log.i(TAG, "we were not listening for location updates anyway");
        }
    }

    /**
     * Check if we are currently listening location updates (can be useful in client library)
     *
     * @return
     */
    public final boolean isListening() {
        return mIsListening;
    }

    /**
     * Called when we find location
     *
     * @param location
     */
    //@Override
    public final void onLocationChanged(@NonNull Location location) {
        onLocationFound(location);
    }

    /**
     * Called when the RYLocationProvider finds a location
     *
     * @param location the found location
     */
    public abstract void onLocationFound(@NonNull Location location);

    //@Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "api client connected");
        try {
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            startListen();
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "Failed to connect to location services ");
            mIsListening = false;

        }
    }

    //@Override
    public void onConnectionSuspended(int i) {
        //mGoogleApiClient.connect();
    }

    public void writeToFile(String data, Context context) {
        long yourmilliseconds = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS",Locale.ENGLISH);
        Date resultdate = new Date(yourmilliseconds);
        data = sdf.format(resultdate) + "  " + data;
        File path = Environment.getExternalStorageDirectory();
        File file = new File(path, "logs.txt");

        try {
            BufferedWriter buf = new BufferedWriter(new FileWriter(file, true));
            buf.append(data);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            Log.e("RYLOCATION", "File write failed: " + e.toString());
        }
    }

}