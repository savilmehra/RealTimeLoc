package in.railyatri.rylocation;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import in.railyatri.rylocation.BusinessLayer.BusinessEntity.RYLocation;
import in.railyatri.rylocation.BusinessLayer.BusinessEvent.EventLocationNetworkFound;
import in.railyatri.rylocation.Persistent.TinyDB;
import in.railyatri.rylocation.ResourcesAccessLayer.Location.LocationFilter;
import in.railyatri.rylocation.ResourcesAccessLayer.Location.RYLocationProperties;
import in.railyatri.rylocation.ResourcesAccessLayer.Location.RYNetworkProvider;
import in.railyatri.rylocation.ResourcesAccessLayer.NetworkLocation.NetworkLocationConstants;
import in.railyatri.rylocation.libUtils.Utils;

public class RYLocationNetworkRequest {
    private Context mContext;
    private static RYLocationNetworkRequest RYLocationInstance;
    private RYLocationProperties mLocationProperties;
    private RYNetworkProvider ryNetworkProvider;
    private boolean isListening = false;
    public static final String TAG = "RYLocationGPSProvider";


    /**
     * Private constructor
     */
    private RYLocationNetworkRequest() {
    }

    /**
     * A static method to get instance.
     */
    public static RYLocationNetworkRequest getInstance() {
        if (RYLocationInstance == null) {
            RYLocationInstance = new RYLocationNetworkRequest();
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
                startNetworkLocation(context);
                break;
            case NetworkLocationConstants.RequestType.FOREGROUND:
                tinyDB.putLong("updateInterval", updateInterval);
                tinyDB.putInt("rylocation_request_type", requestType);
                Log.i(TAG, "REQUEST FOR FOREGROUND");
                startNetworkLocation(context);
                break;
            case NetworkLocationConstants.RequestType.ON_TRIP:
                tinyDB.putLong("updateInterval", updateInterval);
                tinyDB.putInt("rylocation_request_type", requestType);
                Log.i(TAG, "REQUEST FOR ON TRIP");
                startNetworkLocation(context);
                break;
            case NetworkLocationConstants.RequestType.IDLE:
                tinyDB.putLong("updateInterval", updateInterval);
                tinyDB.putInt("rylocation_request_type", requestType);
                Log.i(TAG, "REQUEST FOR IDLE");
                startNetworkLocation(context);
                break;
            case NetworkLocationConstants.RequestType.MAPLOCATION:
                Log.e("Garvit", "MapLocation");
                tinyDB.putLong("updateInterval", updateInterval);
                tinyDB.putInt("rylocation_request_type", requestType);
                Log.i(TAG, "REQUEST FOR MAPLOCATION");
                startNetworkLocation(context);
                break;

            case NetworkLocationConstants.RequestType.GPS:
                Log.e("Garvit", "GPS");
                tinyDB.putLong("updateInterval", 5000);
                tinyDB.putInt("rylocation_request_type", requestType);
                Log.i(TAG, "REQUEST FOR GPS");
                startNetworkLocation(context);
                break;

            case NetworkLocationConstants.RequestType.LTS:
                Log.e("Garvit", "LTS");
                tinyDB.putLong("updateInterval", 5000);
                tinyDB.putInt("rylocation_request_type", requestType);
                Log.i(TAG, "REQUEST FOR LTS");
                startNetworkLocation(context);
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
        if (ryNetworkProvider != null)
            ryNetworkProvider.stopListen();
    }


    /**
     * This method will let app stop listening to fused location and will start only GPS locations
     *
     * @param context
     */
    private void startNetworkLocation(final Context context) {
        if (ryNetworkProvider != null)
            ryNetworkProvider.stopListen();
        ryNetworkProvider = new RYNetworkProvider(context, mLocationProperties) {

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
                EventBus.getDefault().post(new EventLocationNetworkFound(ryLocation));
            }
        };
        isListening = true;
    }


    public boolean isNetworkListening() {
        return ryNetworkProvider.isListening();
    }

    public boolean isListening() {
        return isListening;
    }

}
