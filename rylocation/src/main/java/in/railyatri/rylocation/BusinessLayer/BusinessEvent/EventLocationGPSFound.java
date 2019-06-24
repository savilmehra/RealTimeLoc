package in.railyatri.rylocation.BusinessLayer.BusinessEvent;

import in.railyatri.rylocation.BusinessLayer.BusinessEntity.RYLocation;

/**
 * Created by garvit on 2/6/17.
 */
public class EventLocationGPSFound {

    public EventLocationGPSFound(RYLocation location) {
        this.mLocation = location;
    }

    public RYLocation getLocation() {
        return mLocation;
    }

    private RYLocation mLocation;
}
