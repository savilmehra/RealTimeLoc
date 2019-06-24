package in.railyatri.rylocation.BusinessLayer.BusinessEvent;


public class EventSetLocationConfigs {
    int locationConfig;

    public EventSetLocationConfigs(int requestType) {
        this.locationConfig = requestType;
    }

    public int getLocationConfig() {
        return locationConfig;
    }
}
