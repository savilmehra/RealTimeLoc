package in.railyatri.rylocation.BusinessLayer.BusinessEntity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Saldi on 5/5/16.
 * for Railyatri
 * you may contact me at : sourabh.saldi@railyatri.in
 */
public class NetworkLocationResponse {

    @SerializedName("success")
    @Expose
    private Boolean success;
    @SerializedName("lat")
    @Expose
    private Double lat;
    @SerializedName("lng")
    @Expose
    private Double lng;
    @SerializedName("accuracy")
    @Expose
    private Integer accuracy;

    /**
     * @return The success
     */
    public Boolean getSuccess() {
        return success;
    }

    /**
     * @param success The success
     */
    public void setSuccess(Boolean success) {
        this.success = success;
    }

    /**
     * @return The lat
     */
    public Double getLat() {
        return lat;
    }

    /**
     * @param lat The lat
     */
    public void setLat(Double lat) {
        this.lat = lat;
    }

    /**
     * @return The lng
     */
    public Double getLng() {
        return lng;
    }

    /**
     * @param lng The lng
     */
    public void setLng(Double lng) {
        this.lng = lng;
    }

    /**
     * @return The accuracy
     */
    public Integer getAccuracy() {
        return accuracy;
    }

    /**
     * @param accuracy The accuracy
     */
    public void setAccuracy(Integer accuracy) {
        this.accuracy = accuracy;
    }

}