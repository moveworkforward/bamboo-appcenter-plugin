package com.rivannikov.bamboo.appcenter.model;

import com.google.gson.annotations.SerializedName;

public class AppCenterReleaseUploadsCommitResponse {

    @SerializedName("release_id")
    private String releaseId;
    @SerializedName("release_url")
    private String releaseUrl;

    public String getReleaseId() {
        return releaseId;
    }

    public void setReleaseId(String releaseId) {
        this.releaseId = releaseId;
    }

    public String getReleaseUrl() {
        return releaseUrl;
    }

    public void setReleaseUrl(String releaseUrl) {
        this.releaseUrl = releaseUrl;
    }

}
