
package com.easy_locater.realtimeloc;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class NotificationBody {

    @SerializedName("notification")
    @Expose
    private Notification notification;
    @SerializedName("priority")
    @Expose
    private String priority;
    @SerializedName("registration_ids")
    @Expose
    private List<String> registrationIds = null;

    public Notification getNotification() {
        return notification;
    }

    public void setNotification(Notification notification) {
        this.notification = notification;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public List<String> getRegistrationIds() {
        return registrationIds;
    }

    public void setRegistrationIds(List<String> registrationIds) {
        this.registrationIds = registrationIds;
    }

}
