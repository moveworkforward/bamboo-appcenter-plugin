package com.rivannikov.bamboo.appcenter.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.google.gson.annotations.SerializedName;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AppCenterReleasesRequest {

    private String id;
    @SerializedName("mandatory_update")
    private boolean mandatoryUpdate = false;
    @SerializedName("notify_testers")
    private boolean notifyTesters = true;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isMandatoryUpdate() {
        return mandatoryUpdate;
    }

    public void setMandatoryUpdate(boolean mandatoryUpdate) {
        this.mandatoryUpdate = mandatoryUpdate;
    }

    public boolean isNotifyTesters() {
        return notifyTesters;
    }

    public void setNotifyTesters(boolean notifyTesters) {
        this.notifyTesters = notifyTesters;
    }

}
