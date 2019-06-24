package in.railyatri.rylocation.libUtils;

import android.app.ActivityManager;
import android.content.Context;

public class Utils {

    public static final String TAG = "RYLocationProvider";
    public static final String ENDPOINT = "http://api.railyatri.in";
    public static final String LOCATION_REQUEST_STARTED = "location request started";
    public static final String LOCATION_REQUEST_STOPPED = "location request stopped";
    /**
     * Checks if service is running
     *
     * @param serviceClass
     * @return
     */
    public static  boolean isServiceRunning(Class<?> serviceClass,Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
