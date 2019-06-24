package in.railyatri.rylocation.BusinessLayer.BusinessEvent;

import in.railyatri.rylocation.BusinessLayer.BusinessEntity.ProviderStatus;
import in.railyatri.rylocation.BusinessLayer.BusinessEntity.RYLocation;

/**
 * Created by Saldi on 28/4/16.
 * for Railyatri
 * you may contact me at : sourabh.saldi@railyatri.in
 */
public class EventProviderStatus {

    private ProviderStatus mProvider;

    public EventProviderStatus(ProviderStatus providerStatus) {
        this.mProvider = providerStatus;
    }

    public ProviderStatus getProviderStatus() {
        return mProvider;
    }
}
