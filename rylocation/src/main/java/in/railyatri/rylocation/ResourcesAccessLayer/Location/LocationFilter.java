package in.railyatri.rylocation.ResourcesAccessLayer.Location;

import android.content.Context;
import android.location.Location;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import in.railyatri.rylocation.BusinessLayer.BusinessEntity.RYLocation;
import in.railyatri.rylocation.Persistent.TinyDB;

/**
 * Created by garvit on 6/6/17.
 */
public class LocationFilter {
    Context mContext;
    private static final int MINUTES_IN_HOUR = 60;
    private static final int SECONDS_IN_MINUTE = 60;
    private static final int METRES_IN_KMS = 1000;
    LinkedHashMap<Integer, Location> queue;


    public LocationFilter(Context context) {
        this.mContext = context;
        queue = new LinkedHashMap<Integer, Location>() {
            @Override
            protected boolean removeEldestEntry(Entry<Integer, Location> eldest) {
                return this.size() > 5;
            }
        };
    }

    public boolean isLocation(RYLocation ryLocation) {
        if (!new TinyDB(mContext).getBoolean("isActiveTrip"))
            return false;

        Location currentLocation = new Location("currentLocation");
        currentLocation.setLatitude(ryLocation.getLatitude());
        currentLocation.setLongitude(ryLocation.getLongitude());
        currentLocation.setTime(ryLocation.getTime());
        currentLocation.setAccuracy(ryLocation.getAccuracy());
        currentLocation.setSpeed(ryLocation.getSpeed());
        currentLocation.setProvider(ryLocation.getProvider());
        currentLocation.setAltitude(ryLocation.getAltitude());
        currentLocation.setBearing(ryLocation.getBearing());

        boolean isAccurate = true;
        Location lastLocation = (Location) new TinyDB(mContext,TinyDB.PERSISTENT_TYPE.LOCATION).getObject("ryLastLocation", Location.class);
        if (lastLocation == null) {
            new TinyDB(mContext,TinyDB.PERSISTENT_TYPE.LOCATION).putObject("ryLastLocation", currentLocation);
            return true;
        }

        isAccurate = timeDistanceCalculations(currentLocation, lastLocation);
        new TinyDB(mContext,TinyDB.PERSISTENT_TYPE.LOCATION).putObject("ryLastLocation", currentLocation);
        return stationCalculations(currentLocation, isAccurate);
    }

    private boolean stationCalculations(Location currentLocation, boolean isAccurate) {
        if (!isAccurate)
            return false;
        if (new TinyDB(mContext,TinyDB.PERSISTENT_TYPE.RY_LOCATION).getObject("lastStationLocation", RYLocation.class) == null)
            return isAccurate;

        RYLocation stationLocation = (RYLocation) new TinyDB(mContext,TinyDB.PERSISTENT_TYPE.RY_LOCATION).getObject("lastStationLocation", RYLocation.class);
        Location location = new Location("stationLocation");
        location.setLatitude(stationLocation.getLatitude());
        location.setLongitude(stationLocation.getLongitude());
        location.setTime(stationLocation.getTime());
        location.setAccuracy(stationLocation.getAccuracy());
        location.setSpeed(stationLocation.getSpeed());
        location.setProvider(stationLocation.getProvider());
        location.setAltitude(stationLocation.getAltitude());
        location.setBearing(stationLocation.getBearing());

        return timeDistanceCalculations(currentLocation, location);
    }

    int g = 1;

    private void addLocation(Location currentLocation) {
        queue.put(g++, currentLocation);
    }

    private int getTime(Location currentLocation, Location lastLocation) {
        long time = (currentLocation.getTime() - lastLocation.getTime()) / 1000;
        time = (time >= 0) && (time <= 3 * SECONDS_IN_MINUTE) ? 1 : time;
        time = (time >= 3 * SECONDS_IN_MINUTE) && (time <= 15 * SECONDS_IN_MINUTE) ? 2 : time;
        time = (time >= 15 * SECONDS_IN_MINUTE) && (time <= 30 * SECONDS_IN_MINUTE) ? 3 : time;
        time = (time >= 30 * SECONDS_IN_MINUTE) && (time <= MINUTES_IN_HOUR * SECONDS_IN_MINUTE) ? 4 : time;
        return (int) time;
    }

    private boolean checkForAccurate(Location currentLocation) {
        int accurateLocation = 0;
        if (queue.size() < 5) {
            return false;
        } else {
            for (int i = 0; i < queue.size(); i++) {
                int distance = calculateDistance(currentLocation, (new ArrayList<Location>(queue.values())).get(i));
                int time = getTime(currentLocation, (new ArrayList<Location>(queue.values())).get(i));
                if (time <= 4) {
                    if (distance >= 10 * METRES_IN_KMS)
                        switch (time) {
                            case 1:
                                break;
                            case 2:
                                if (distance <= 50 * METRES_IN_KMS) {
                                    accurateLocation++;
                                }
                                break;

                            case 3:
                                if (distance <= 100 * METRES_IN_KMS) {
                                    accurateLocation++;
                                }
                                break;

                            case 4:
                                if (distance <= 200 * METRES_IN_KMS) {
                                    accurateLocation++;
                                }
                                break;
                        }
                    else if (distance <= 10 * METRES_IN_KMS) {
                        accurateLocation++;
                    }
                }
            }
            if (accurateLocation > 2)
                return true;
        }
        return false;
    }


    private int calculateDistance(Location currentLocation, Location lastLocation) {
        Location locationCurrent = new Location("currentLocation");
        locationCurrent.setLatitude(currentLocation.getLatitude());
        locationCurrent.setLongitude(currentLocation.getLongitude());
        Location locationStation = new Location("lastLocation");
        locationStation.setLatitude(lastLocation.getLatitude());
        locationStation.setLongitude(lastLocation.getLongitude());
        return (int) locationCurrent.distanceTo(locationStation);
    }

    private boolean timeDistanceCalculations(Location currentLocation, Location lastLocation) {
        boolean isAccurate;
        int distance = calculateDistance(currentLocation, lastLocation);
        int time = getTime(currentLocation, lastLocation);
        int currentSpeed = (int) currentLocation.getSpeed();

        if (distance >= 10 * METRES_IN_KMS)
            switch (time) {
                case 1:
                    isAccurate = checkForAccurate(currentLocation);
                    break;
                case 2:
                    if (distance <= 50 * METRES_IN_KMS) {
                        isAccurate = true;
                        addLocation(currentLocation);
                    } else
                        isAccurate = checkForAccurate(currentLocation);
                    break;

                case 3:
                    if (distance <= 100 * METRES_IN_KMS) {
                        isAccurate = true;
                        addLocation(currentLocation);
                    } else
                        isAccurate = checkForAccurate(currentLocation);
                    break;

                case 4:
                    if (distance <= 200 * METRES_IN_KMS) {
                        isAccurate = true;
                        addLocation(currentLocation);
                    } else
                        isAccurate = checkForAccurate(currentLocation);
                    break;

                default:
                    isAccurate = false;
                    break;
            }
        else if (currentSpeed > 200)
            isAccurate = false;
        else if (distance <= 10 * METRES_IN_KMS) {
            isAccurate = true;
            addLocation(currentLocation);
        } else
            isAccurate = true;
        return isAccurate;
    }


}
