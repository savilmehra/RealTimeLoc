package in.railyatri.rylocation.BusinessLayer.BusinessEntity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LocationLogs {

    @SerializedName("data")
    @Expose
    private Data data;

    /**
     * @return The data
     */
    public Data getData() {
        return data;
    }

    /**
     * @param data The data
     */
    public void setData(Data data) {
        this.data = data;
    }

}