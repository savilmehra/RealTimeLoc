package in.railyatri.rylocation.BusinessLayer.BusinessEntity;

import android.location.Location;

import java.io.Serializable;

/**
 * Created by Saldi on 5/5/16.
 * for Railyatri
 * you may contact me at : sourabh.saldi@railyatri.in
 */
public class RYLocation implements Serializable {

    private Location mLocation;

    private double mLatitude;

    private double mLongitude;

    private double mAltitude;

    private float mAccuracy;

    private float mSpeed;

    private float mBearing;

    private boolean isGPSLocation;

    private long mTime;

    private String mProvider;

    private boolean isAccurate;

    private float[] accelerometerData;

    private float[] geomagneticData;

    private float[] orientationData;

    private boolean isMockLocation;


    public boolean isMockLocation() {
        return isMockLocation;
    }

    public void setMockLocation(boolean mockLocation) {
        isMockLocation = mockLocation;
    }

    public Boolean getAccurate() {
        return isAccurate;
    }

    public void setAccurate(boolean accurate) {
        isAccurate = accurate;
    }

    public double getAltitude() {
        return mAltitude;
    }

    public void setAltitude(double mAltitude) {
        this.mAltitude = mAltitude;
    }

    public float getBearing() {
        return mBearing;
    }

    public void setBearing(float mBearing) {
        this.mBearing = mBearing;
    }

    public String getProvider() {
        return mProvider;
    }

    public void setProvider(String mProvider) {
        this.mProvider = mProvider;
    }

    public long getTime() {
        return mTime;
    }

    public void setTime(long mTime) {
        this.mTime = mTime;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double latitude) {
        mLatitude = latitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(double longitude) {
        mLongitude = longitude;
    }

    public float getAccuracy() {
        return mAccuracy;
    }

    public void setAccuracy(float mAccuracy) {
        this.mAccuracy = mAccuracy;
    }

    public float getSpeed() {
        return mSpeed;
    }

    public void setSpeed(float speed) {
        this.mSpeed = speed;
    }

    public boolean isGPSLocation() {
        return isGPSLocation;
    }

    public void setGPSLocation(boolean GPSLocation) {
        isGPSLocation = GPSLocation;
    }

    public float[] getAccelerometerData() {
        return accelerometerData;
    }

    public void setAccelerometerData(float[] accelerometerData) {
        this.accelerometerData = accelerometerData;
    }

    public float[] getGeomagneticData() {
        return geomagneticData;
    }

    public void setGeomagneticData(float[] geomagneticData) {
        this.geomagneticData = geomagneticData;
    }

    public float[] getOrientationData() {
        return orientationData;
    }

    public void setOrientationData(float[] orientationData) {
        this.orientationData = orientationData;
    }

//    public Location getGPSLocation() {
//        return mLocation;
//    }
//
//    public void setGPSLocation(Location mLocation) {
//        this.mLocation = mLocation;
//    }
}
