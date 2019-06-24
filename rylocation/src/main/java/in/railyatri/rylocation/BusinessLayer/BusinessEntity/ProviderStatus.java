package in.railyatri.rylocation.BusinessLayer.BusinessEntity;

/**
 * Created by Saldi on 5/5/16.
 * for Railyatri
 * you may contact me at : sourabh.saldi@railyatri.in
 */
public class ProviderStatus {

    private String mProviderName;
    private boolean mEnabled;

    public String getProviderName() {
        return mProviderName;
    }

    public void setProviderName(String providerName) {
        mProviderName = providerName;
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    public void setEnabled(boolean enabled) {
        this.mEnabled = enabled;
    }
}