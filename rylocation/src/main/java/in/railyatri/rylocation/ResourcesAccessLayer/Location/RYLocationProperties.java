package in.railyatri.rylocation.ResourcesAccessLayer.Location;

import android.support.annotation.FloatRange;

import com.google.android.gms.location.LocationRequest;


/**
 * This class must be used for setting up {@link RYFusedProvider}
 * <p/>
 * We provide default values as below
 * <p/>
 * Default minimum time between updates as 15 minutes {@link #DEFAULT_MIN_TIME}
 * <p/>
 * Default minimum distance between updates as 100 meters  {@link #DEFAULT_MIN_METERS}
 * <p/>
 * Created by Saldi on 28/4/16.
 * for Railyatri
 * you may contact me at : sourabh.saldi@railyatri.in
 */
public class RYLocationProperties {


    public static final RYLocationProperties DEFAULT = new RYLocationProperties();

    public static final long DEFAULT_MIN_TIME = 15 * 60 * 1000;

    public static final float DEFAULT_MIN_METERS = 100;


    private long mRegularUpdateTime = 0;

    private float mRegularUpdateDistance = 0;

    private int mPriority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;


    public int getPriority() {
        return mPriority;
    }

    public void setPriority(int mPriority) {
        this.mPriority = mPriority;
    }


    public void setRegularUpdateTime(@FloatRange(from = 1) long regularUpdateTime) {
        if (regularUpdateTime > 0) {
            mRegularUpdateTime = regularUpdateTime;
        }
    }

    public long getRegularUpdateTime() {
        return mRegularUpdateTime <= 0 ? DEFAULT_MIN_TIME : mRegularUpdateTime;
    }

    public void setMetersBetweenUpdates(@FloatRange(from = 0) float mRegularUpdateDistance) {
        if (mRegularUpdateDistance > 0) {
            this.mRegularUpdateDistance = mRegularUpdateDistance;
        }
    }

    public float getMetersBetweenUpdates() {
        return mRegularUpdateDistance <= 0 ? DEFAULT_MIN_METERS : mRegularUpdateDistance;
    }
}