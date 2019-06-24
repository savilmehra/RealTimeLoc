package in.railyatri.rylocation.BusinessLayer.BusinessEvent;

import android.location.Location;

import in.railyatri.rylocation.BusinessLayer.BusinessEntity.RYLocation;

/**
 * Created by Saldi on 28/4/16.
 * for Railyatri
 * you may contact me at : sourabh.saldi@railyatri.in
 */
public class EventLocationFound {

    private RYLocation mLocation;

    public EventLocationFound(RYLocation location) {
        this.mLocation = location;
    }

    public RYLocation getLocation() {
        return mLocation;
    }
}
