package com.rivannikov.bamboo.appcenter.model;

import com.google.gson.annotations.SerializedName;

public class AppCenterReleasesResponse {

    private String id;
    @SerializedName("mandatory_update")
    private boolean mandatoryUpdate;
    @SerializedName("provisioning_status_url")
    private String provisioningStatusUrl;

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

    public String getProvisioningStatusUrl() {
        return provisioningStatusUrl;
    }

    public void setProvisioningStatusUrl(String provisioningStatusUrl) {
        this.provisioningStatusUrl = provisioningStatusUrl;
    }
}
