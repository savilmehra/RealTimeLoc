package in.railyatri.rylocation.BusinessLayer.BusinessEntity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Data {

    @SerializedName("lat")
    @Expose
    private Double lat;
    @SerializedName("lon")
    @Expose
    private Double lon;
    @SerializedName("lac")
    @Expose
    private Integer lac;
    @SerializedName("mnc")
    @Expose
    private Integer mnc;
    @SerializedName("cellId")
    @Expose
    private Integer cellId;
    @SerializedName("networkType")
    @Expose
    private String networkType;
    @SerializedName("vendor")
    @Expose
    private String vendor;
    @SerializedName("osVersion")
    @Expose
    private String osVersion;
    @SerializedName("device")
    @Expose
    private String device;
    @SerializedName("speed")
    @Expose
    private String speed;
    @SerializedName("timeStamp")
    @Expose
    private String timeStamp;
    @SerializedName("signalStrength")
    @Expose
    private String signalStrength;

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
     * @return The lon
     */
    public Double getLon() {
        return lon;
    }

    /**
     * @param lon The lon
     */
    public void setLon(Double lon) {
        this.lon = lon;
    }

    /**
     * @return The lac
     */
    public Integer getLac() {
        return lac;
    }

    /**
     * @param lac The lac
     */
    public void setLac(Integer lac) {
        this.lac = lac;
    }

    /**
     * @return The mnc
     */
    public Integer getMnc() {
        return mnc;
    }

    /**
     * @param mnc The mnc
     */
    public void setMnc(Integer mnc) {
        this.mnc = mnc;
    }

    /**
     * @return The cellId
     */
    public Integer getCellId() {
        return cellId;
    }

    /**
     * @param cellId The cellId
     */
    public void setCellId(Integer cellId) {
        this.cellId = cellId;
    }

    /**
     * @return The networkType
     */
    public String getNetworkType() {
        return networkType;
    }

    /**
     * @param networkType The networkType
     */
    public void setNetworkType(String networkType) {
        this.networkType = networkType;
    }

    /**
     * @return The vendor
     */
    public String getVendor() {
        return vendor;
    }

    /**
     * @param vendor The vendor
     */
    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    /**
     * @return The osVersion
     */
    public String getOsVersion() {
        return osVersion;
    }

    /**
     * @param osVersion The osVersion
     */
    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    /**
     * @return The device
     */
    public String getDevice() {
        return device;
    }

    /**
     * @param device The device
     */
    public void setDevice(String device) {
        this.device = device;
    }

    /**
     * @return The speed
     */
    public String getSpeed() {
        return speed;
    }

    /**
     * @param speed The speed
     */
    public void setSpeed(String speed) {
        this.speed = speed;
    }

    /**
     * @return The timeStamp
     */
    public String getTimeStamp() {
        return timeStamp;
    }

    /**
     * @param timeStamp The timeStamp
     */
    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getSignalStrength() {
        return signalStrength;
    }

    public void setSignalStrength(String signalStrength) {
        this.signalStrength = signalStrength;
    }
}