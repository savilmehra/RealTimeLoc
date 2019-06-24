package in.railyatri.rylocation.BusinessLayer.BusinessEvent;

import com.google.android.gms.location.Geofence;

import java.util.List;

/**
 * Created by garvit on 13/9/16.
 */
public class EventGeofenceFound {

    int eventType;
    List<Geofence> geofenceList;

    public int getEventType() {
        return eventType;
    }

    public void setEventType(int eventType) {
        this.eventType = eventType;
    }

    public List<Geofence> getGeofenceList() {
        return geofenceList;
    }

    public void setGeofenceList(List<Geofence> geofenceList) {
        this.geofenceList = geofenceList;
    }


}
