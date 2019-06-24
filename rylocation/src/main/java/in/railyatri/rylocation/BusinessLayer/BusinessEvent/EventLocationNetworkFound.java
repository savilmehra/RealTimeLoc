package in.railyatri.rylocation.BusinessLayer.BusinessEvent;

import in.railyatri.rylocation.BusinessLayer.BusinessEntity.RYLocation;

public class EventLocationNetworkFound {

    public EventLocationNetworkFound(RYLocation location) {
        this.mLocation = location;
    }

    public RYLocation getLocation() {
        return mLocation;
    }

    private RYLocation mLocation;
}
