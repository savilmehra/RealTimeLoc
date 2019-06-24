package in.railyatri.rylocation;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationRequest;

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
import in.railyatri.rylocation.BusinessLayer.BusinessEvent.EventLocationFound;
import in.railyatri.rylocation.BusinessLayer.BusinessEvent.EventSetLocationConfigs;
import in.railyatri.rylocation.Persistent.TinyDB;
import in.railyatri.rylocation.ResourcesAccessLayer.Geofencing.RYGeofenceProvider;
import in.railyatri.rylocation.ResourcesAccessLayer.Location.LocationFilter;
import in.railyatri.rylocation.ResourcesAccessLayer.Location.RYFusedProvider;
import in.railyatri.rylocation.ResourcesAccessLayer.Location.RYGPSProvider;
import in.railyatri.rylocation.ResourcesAccessLayer.Location.RYLocationProperties;
import in.railyatri.rylocation.ResourcesAccessLayer.NetworkLocation.NetworkLocationConstants;
import in.railyatri.rylocation.libUtils.Utils;

import static android.content.Context.ACTIVITY_SERVICE;

public class RYLocationRequest implements SensorEventListener {
    private Context mContext;
    private static RYLocationRequest RYLocationInstance;
    private RYLocationProperties mLocationProperties;
    private RYFusedProvider ryFusedProvider;
    private RYGPSProvider ryGPSProvider;
    private RYGeofenceProvider ryGeofenceProvider;
    private boolean isListening = false;
    private float[] accelerometerData;
    private float[] geomagneticData;
    private float[] orientationData;
    private SensorManager sensorManager;

    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    private static final int SHAKE_THRESHOLD = 10;
    private float currentSpeed = 0.0f;

    /**
     * Private constructor
     */
    private RYLocationRequest() {
    }

    /**
     * A static method to get instance.
     */
    public static RYLocationRequest getInstance() {
        if (RYLocationInstance == null) {
            RYLocationInstance = new RYLocationRequest();
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
        this.mContext = context;

        //Deferred from release 3.6.7(261) due to anr's
//        try {
//            if (new TinyDB(context, TinyDB.PERSISTENT_TYPE.LOCATION).getBoolean("isVibrationSensor")
//                    && Build.VERSION.SDK_INT > 16 && isMemoryFree()) {
//
//                accelerometerData = new float[3];
//                geomagneticData = new float[3];
//                orientationData = new float[3];
//
//                sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
//                sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
//                sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL);
//            }
//        } catch (Exception e) {
//            //DeBug.showLog("vibrate", "exception " + e.getMessage());
//            //ignore
//        }

        EventBus.getDefault().post(new EventSetLocationConfigs(requestType));

        Log.i(Utils.TAG, Utils.LOCATION_REQUEST_STARTED);
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

        switch (requestType) {
            case NetworkLocationConstants.RequestType.FAST:         // 1 second
                stopSensor(updateInterval - 100);
                tinyDB.putInt("rylocation_request_type", requestType);
                Log.i(Utils.TAG, "REQUEST FOR FAST");
                mLocationProperties.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                startGPSLocation(context);
//                startFusedLocation(context);
                break;
            case NetworkLocationConstants.RequestType.FOREGROUND:   // 40 second
                stopSensor(updateInterval - 100);
                tinyDB.putInt("rylocation_request_type", requestType);
                Log.i(Utils.TAG, "REQUEST FOR FOREGROUND");
                mLocationProperties.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                startFusedLocation(context);
                break;
            case NetworkLocationConstants.RequestType.ON_TRIP:      // 5 minutes
                stopAndReregisterSensor(updateInterval - 100);
                tinyDB.putInt("rylocation_request_type", requestType);
                Log.i(Utils.TAG, "REQUEST FOR ON TRIP");
                mLocationProperties.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                startFusedLocation(context);
                break;
            case NetworkLocationConstants.RequestType.IDLE:         // 15 minutes
                stopAndReregisterSensor(updateInterval - 100);
                tinyDB.putInt("rylocation_request_type", requestType);
                Log.i(Utils.TAG, "REQUEST FOR IDLE");
                mLocationProperties.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                startFusedLocation(context);
                break;
            case NetworkLocationConstants.RequestType.MAPLOCATION:      // 1 second
                stopSensor(updateInterval - 100);
                tinyDB.putInt("rylocation_request_type", requestType);
                Log.i(Utils.TAG, "REQUEST FOR MAPLOCATION");
                mLocationProperties.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                startFusedLocation(context);
                break;

            case NetworkLocationConstants.RequestType.GPS:      // 5 second
                stopSensor(updateInterval - 100);
                tinyDB.putInt("rylocation_request_type", 5000);
                Log.i(Utils.TAG, "REQUEST FOR LTS");
                mLocationProperties.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                startFusedLocation(context);
                break;

            case NetworkLocationConstants.RequestType.LTS:      // 5 second
                stopSensor(updateInterval - 100);
                tinyDB.putInt("rylocation_request_type", 5000);
                Log.i(Utils.TAG, "REQUEST FOR LTS");
                mLocationProperties.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                startFusedLocation(context);
                break;
        }

    }

    private boolean isMemoryFree() {
        try {
            ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
            ActivityManager activityManager = (ActivityManager) mContext.getSystemService(ACTIVITY_SERVICE);
            activityManager.getMemoryInfo(mi);
//            double availableMegs = mi.availMem / 0x100000L;
            double percentAvail = mi.availMem / (double) mi.totalMem * 100.0;
            if (percentAvail > 50)
                return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }


    /**
     * This method must be used to stop location requests
     *
     * @param context
     */
    public void stopLocationRequest(Context context) {

        if (sensorManager != null)
            sensorManager.unregisterListener(this);

        Log.i(Utils.TAG, Utils.LOCATION_REQUEST_STOPPED);
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
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (ryGPSProvider != null)
                    ryGPSProvider.stopListen();
                if (ryFusedProvider != null)
                    ryFusedProvider.stopListen();
                ryFusedProvider = new RYFusedProvider(context, mLocationProperties) {

                    @Override
                    public void onLocationFound(Location location) {
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
                        ryLocation.setGPSLocation(false);
                        ryLocation.setAccurate(locationFilter.isLocation(ryLocation));
                        ryLocation.setAccelerometerData(accelerometerData);
                        ryLocation.setOrientationData(orientationData);
                        ryLocation.setGeomagneticData(geomagneticData);
                        ryLocation.setMockLocation(isMockLocation(location, context));
                        EventBus.getDefault().post(new EventLocationFound(ryLocation));
                    }
                };
                isListening = true;

            }
        }).start();

    }

    /**
     * This method will let app stop listening to fused location and will start only GPS locations
     *
     * @param context
     */
    private void startGPSLocation(final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                if (ryFusedProvider != null)
                    ryFusedProvider.stopListen();
                if (ryGPSProvider != null)
                    ryGPSProvider.stopListen();
                ryGPSProvider = new RYGPSProvider(context, mLocationProperties) {

                    @Override
                    public void onLocationFound(Location location) {
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
                        ryLocation.setAccelerometerData(accelerometerData);
                        ryLocation.setOrientationData(orientationData);
                        ryLocation.setGeomagneticData(geomagneticData);
                        ryLocation.setMockLocation(isMockLocation(location, context));
                        EventBus.getDefault().post(new EventLocationFound(ryLocation));
                    }
                };
                isListening = true;

            }
        }).start();
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
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }

        ryGeofenceProvider = new RYGeofenceProvider(context);
        return ryGeofenceProvider.addGeofences(mGeofenceList);
    }

    public boolean stopGeofences(Context context) {
        Log.d("RYGeofence", "Stop Geofence ");
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        ryGeofenceProvider = new RYGeofenceProvider(context);
        return ryGeofenceProvider.removeGeofences();
    }

    @Override
    public void onSensorChanged(final SensorEvent event) {

        try {
            new Thread(new Runnable() {
                @Override
                public void run() {

                    //DeBug.showLog("vibrate", "yes");

                    if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                        accelerometerData = event.values;

                        //DeBug.showLog("vibrate", "accelerometer");
                        //mTinyDB.putString("accelerometer", Arrays.toString(accelerometerData));

                    }
                    if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                        //DeBug.showLog("vibrate", "geomagnetic");
                        geomagneticData = event.values;
                        //mTinyDB.putString("geomagnetic", Arrays.toString(geomagneticData));
                    }
                    if (accelerometerData != null && geomagneticData != null) {

                        float R[] = new float[9];
                        float I[] = new float[9];
                        boolean success = SensorManager.getRotationMatrix(R, I, accelerometerData, geomagneticData);
                        if (success) {
                            //DeBug.showLog("vibrate", "orientation");
                            orientationData = SensorManager.getOrientation(R, orientationData);

                            // mTinyDB.putString("orientation", Arrays.toString(orientationData));
                        }
                    }

                }
            }).start();

        } catch (Exception e) {
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    private void stopAndReregisterSensor(long restartTime) {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    sensorManager.registerListener(RYLocationRequest.this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
                    sensorManager.registerListener(RYLocationRequest.this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL);
                }
            }, restartTime);
        }
    }

    private void stopSensor(long restartTime) {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }


    private boolean isMockLocation(Location location, Context context) {
        try {
            boolean isMock;
            if (android.os.Build.VERSION.SDK_INT >= 18) {
                isMock = location.isFromMockProvider();
            } else {
                isMock = !Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION).equals("0");
            }
            return isMock;
        } catch (Exception e) {
        }
        return false;
    }
}
