package in.railyatri.rylocation.ResourcesAccessLayer.Location;

import android.content.Context;
import android.location.LocationManager;
import android.support.annotation.NonNull;

/**
 * Created by Saldi on 28/4/16.
 * for Railyatri
 * you may contact me at : sourabh.saldi@railyatri.in
 */
public  class RYLocationUtils {

    /**
     * @param context
     * @return
     */
    public static boolean isGpsProviderEnabled(@NonNull Context context) {
        return isProviderEnabled(context, LocationManager.GPS_PROVIDER);

    }

    /**
     * @param context
     * @return
     */
    public static boolean isNetworkProviderEnabled(@NonNull Context context) {
        return isProviderEnabled(context, LocationManager.NETWORK_PROVIDER);
    }

    /**
     * @param context
     * @return
     */
    public static boolean isPassiveProviderEnabled(@NonNull Context context) {
        return isProviderEnabled(context, LocationManager.PASSIVE_PROVIDER);
    }

    /**
     * This method checks whether or not given provider is enabled
     *
     * @param context
     * @param provider
     * @return true if enabled, false otherwise
     */
    private static boolean isProviderEnabled(@NonNull Context context, @NonNull String provider) {
        final LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return manager.isProviderEnabled(provider);
    }

}