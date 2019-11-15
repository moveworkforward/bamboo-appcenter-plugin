package com.rivannikov.bamboo.appcenter.model;

import com.google.gson.annotations.SerializedName;

public class AppCenterReleaseUploadsResponse {

    @SerializedName("upload_id")
    private String uploadId;
    @SerializedName("upload_url")
    private String uploadUrl;

    public String getUploadId() {
        return uploadId;
    }

    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }

    public String getUploadUrl() {
        return uploadUrl;
    }

    public void setUploadUrl(String uploadUrl) {
        this.uploadUrl = uploadUrl;
    }
}
