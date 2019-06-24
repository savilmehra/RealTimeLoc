package in.railyatri.rylocation;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.location.Geofence;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import in.railyatri.rylocation.BusinessLayer.BusinessEntity.RYLocation;
import in.railyatri.rylocation.BusinessLayer.BusinessEvent.EventLocationGPSFound;
import in.railyatri.rylocation.Persistent.TinyDB;
import in.railyatri.rylocation.ResourcesAccessLayer.Geofencing.RYGeofenceProvider;
import in.railyatri.rylocation.ResourcesAccessLayer.Location.LocationFilter;
import in.railyatri.rylocation.ResourcesAccessLayer.Location.RYFusedProvider;
import in.railyatri.rylocation.ResourcesAccessLayer.Location.RYGPSProvider;
import in.railyatri.rylocation.ResourcesAccessLayer.Location.RYLocationProperties;
import in.railyatri.rylocation.ResourcesAccessLayer.NetworkLocation.NetworkLocationConstants;
import in.railyatri.rylocation.libUtils.Utils;

/**
 * Created by garvit on 2/6/17.
 */
public class RYLocationGPSRequest {
    private Context mContext;
    private static RYLocationGPSRequest RYLocationInstance;
    private RYLocationProperties mLocationProperties;
    private RYFusedProvider ryFusedProvider;
    private RYGPSProvider ryGPSProvider;
    private RYGeofenceProvider ryGeofenceProvider;
    private boolean isListening = false;
    public static final String TAG = "RYLocationGPSProvider";


    /**
     * Private constructor
     */
    private RYLocationGPSRequest() {
    }

    /**
     * A static method to get instance.
     */
    public static RYLocationGPSRequest getInstance() {
        if (RYLocationInstance == null) {
            RYLocationInstance = new RYLocationGPSRequest();
        }
        return RYLocationInstance;
    }

    /**
     * This method must be used to start location updates
     *
     * @param context
     * @param requestType
     */
    public void startLocationRequest(final Context context, int requestType) {
        Log.i(TAG, Utils.LOCATION_REQUEST_STARTED);
        int minimumDistance = new TinyDB(context).getInt("minimumDistance");
        Long updateInterval = new TinyDB(context).getLong("updateInterval", 1000L);
        boolean isUserOnTrip = new TinyDB(context).getBoolean("isActiveTrip");
        mLocationProperties = new RYLocationProperties();
        if (isUserOnTrip)
            mLocationProperties.setMetersBetweenUpdates(0);
        else
            mLocationProperties.setMetersBetweenUpdates(minimumDistance);
        mLocationProperties.setRegularUpdateTime(updateInterval);

        TinyDB tinyDB = new TinyDB(context);
        this.mContext = context;
        switch (requestType) {
            case NetworkLocationConstants.RequestType.FAST:
                tinyDB.putLong("updateInterval", updateInterval);
                tinyDB.putInt("rylocation_request_type", requestType);
                Log.i(TAG, "REQUEST FOR FAST");
                startGPSLocation(context);
                break;
            case NetworkLocationConstants.RequestType.FOREGROUND:
                tinyDB.putLong("updateInterval", updateInterval);
                tinyDB.putInt("rylocation_request_type", requestType);
                Log.i(TAG, "REQUEST FOR FOREGROUND");
                startGPSLocation(context);
                break;
            case NetworkLocationConstants.RequestType.ON_TRIP:
                tinyDB.putLong("updateInterval", updateInterval);
                tinyDB.putInt("rylocation_request_type", requestType);
                Log.i(TAG, "REQUEST FOR ON TRIP");
                startGPSLocation(context);
                break;
            case NetworkLocationConstants.RequestType.IDLE:
                tinyDB.putLong("updateInterval", updateInterval);
                tinyDB.putInt("rylocation_request_type", requestType);
                Log.i(TAG, "REQUEST FOR IDLE");
                startGPSLocation(context);
                break;
            case NetworkLocationConstants.RequestType.MAPLOCATION:
                Log.e("Garvit", "MapLocation");
                tinyDB.putLong("updateInterval", updateInterval);
                tinyDB.putInt("rylocation_request_type", requestType);
                Log.i(TAG, "REQUEST FOR MAPLOCATION");
                startGPSLocation(context);
                break;

            case NetworkLocationConstants.RequestType.GPS:
                Log.e("Garvit", "GPS");
                tinyDB.putLong("updateInterval", 5000);
                tinyDB.putInt("rylocation_request_type", requestType);
                Log.i(TAG, "REQUEST FOR GPS");
                startGPSLocation(context);
                break;

            case NetworkLocationConstants.RequestType.LTS:
                Log.e("Garvit", "LTS");
                tinyDB.putLong("updateInterval", 5000);
                tinyDB.putInt("rylocation_request_type", requestType);
                Log.i(TAG, "REQUEST FOR LTS");
                startGPSLocation(context);
                break;
        }

    }

    /**
     * This method must be used to stop location requests
     *
     * @param context
     */
    public void stopLocationRequest(Context context) {
        Log.i(TAG, Utils.LOCATION_REQUEST_STOPPED);
        this.mContext = context;
        if (ryGPSProvider != null)
            ryGPSProvider.stopListen();
    }

    /**
     * This method must eb used to stop GPS location and start Fused locations
     *
     * @param context
     */
    private void startFusedLocation(final Context context) {
        if (ryGPSProvider != null)
            ryGPSProvider.stopListen();
        if (ryFusedProvider != null)
            ryFusedProvider.stopListen();
        ryFusedProvider = new RYFusedProvider(context, mLocationProperties) {

            @Override
            public void onLocationFound(Location location) {
                if (location == null)
                    return;
                RYLocation ryLocation = new RYLocation();
                ryLocation.setLatitude(location.getLatitude());
                ryLocation.setLongitude(location.getLongitude());
                ryLocation.setAccuracy(location.getAccuracy());
                ryLocation.setTime(location.getTime());
                ryLocation.setSpeed(Math.round(location.getSpeed() * 3.6));
                ryLocation.setProvider(location.getProvider());
                ryLocation.setAltitude(location.getAltitude());
                ryLocation.setBearing(location.getBearing());
                ryLocation.setGPSLocation(false);
                EventBus.getDefault().post(new EventLocationGPSFound(ryLocation));
            }
        };
        isListening = true;
    }

    /**
     * This method will let app stop listening to fused location and will start only GPS locations
     *
     * @param context
     */
    private void startGPSLocation(final Context context) {
//        if (!new TinyDB(context).getBoolean("isGPS")) {
//            stopLocationRequest(context);
//            return;
//        }

        if (ryFusedProvider != null)
            ryFusedProvider.stopListen();
        if (ryGPSProvider != null)
            ryGPSProvider.stopListen();
        ryGPSProvider = new RYGPSProvider(context, mLocationProperties) {

            @Override
            public void onLocationFound(Location location) {
                if (location == null)
                    return;
                RYLocation ryLocation = new RYLocation();
                LocationFilter locationFilter = new LocationFilter(mContext);
                ryLocation.setLatitude(location.getLatitude());
                ryLocation.setLongitude(location.getLongitude());
                ryLocation.setAccuracy(location.getAccuracy());
                ryLocation.setTime(location.getTime());
                ryLocation.setSpeed(Math.round(location.getSpeed() * 3.6));
                ryLocation.setProvider(location.getProvider());
                ryLocation.setAltitude(location.getAltitude());
                ryLocation.setBearing(location.getBearing());
                ryLocation.setGPSLocation(true);
                ryLocation.setAccurate(locationFilter.isLocation(ryLocation));
                EventBus.getDefault().post(new EventLocationGPSFound(ryLocation));
            }
        };
        isListening = true;
    }

    public boolean isFusedListening() {
        return ryFusedProvider.isListening();
    }

    public boolean isGPSListening() {
        return ryGPSProvider.isListening();
    }

    public boolean isListening() {
        return isListening;
    }

    /**
     * Below is only debug method used to write file not deleting this as we might need the same in future
     *
     * @param data
     * @param context
     */
    public void writeToFile(String data, Context context) {
        long yourmilliseconds = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ENGLISH);
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
            e.printStackTrace();
        }
    }

    public boolean startGeofences(Context context, ArrayList<Geofence> mGeofenceList) {
        Log.d("RYGeofence", "Start Geofence ");
        if (mGeofenceList == null)
            return false;
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }

        ryGeofenceProvider = new RYGeofenceProvider(context);
        return ryGeofenceProvider.addGeofences(mGeofenceList);
    }

    public boolean stopGeofences(Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        ryGeofenceProvider = new RYGeofenceProvider(context);
        return ryGeofenceProvider.removeGeofences();
    }
}
