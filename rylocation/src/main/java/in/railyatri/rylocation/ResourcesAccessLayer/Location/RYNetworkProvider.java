package in.railyatri.rylocation.ResourcesAccessLayer.Location;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;
import android.util.Log;


public abstract class RYNetworkProvider implements LocationListener {

    private static final String TAG = "RYLocationProvider";
    private LocationManager mLocationManager;
    private boolean mIsListening = false;
    private Context mContext;
    private RYLocationProperties mRYLocationProperties;

    /**
     * Constructor initializes  with default properties
     *
     * @param context
     */
    public RYNetworkProvider(@NonNull Context context) {
        this(context, RYLocationProperties.DEFAULT);
    }

    /**
     * Constructor initializes  based on properties given by Client
     *
     * @param context
     * @param RYLocationProperties
     */
    public RYNetworkProvider(@NonNull Context context, @NonNull RYLocationProperties RYLocationProperties) {
        this.mContext = context;
        this.mRYLocationProperties = RYLocationProperties;
        mLocationManager = (LocationManager) mContext
                .getSystemService(Context.LOCATION_SERVICE);

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
        }

    }

    /**
     * Start Listen for location updates
     */
    @RequiresPermission(anyOf = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION})
    public final void startListen() throws InterruptedException {
        if (!this.mIsListening) {
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
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, mRYLocationProperties.getRegularUpdateTime(), 0, this);
            mIsListening = true;
            Log.i(TAG, "started location updates");
        } else {
            Log.i(TAG, "Relax, Already listening for location updates");
        }
    }


    /**
     * This method can be used for stopping Location updates
     */
    public final void stopListen() {
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
        try {
            mLocationManager.removeUpdates(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mIsListening = false;
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
    @Override
    public final void onLocationChanged(@NonNull Location location) {
        Log.i(TAG, "Location has changed, new location is " + location);
        onLocationFound(location);
    }

    /**
     * Called when the RYLocationProvider finds a location
     *
     * @param location the found location
     */
    public abstract void onLocationFound(@NonNull Location location);

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}