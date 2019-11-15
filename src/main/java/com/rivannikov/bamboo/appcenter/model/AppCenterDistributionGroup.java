package com.rivannikov.bamboo.appcenter.model;

import com.google.gson.annotations.SerializedName;

public class AppCenterDistributionGroup {

    private String id;
    private String name;
    private String origin;
    @SerializedName("display_name")
    private boolean displayName;
    @SerializedName("is_public")
    private boolean isPublic;

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public boolean isDisplayName() {
        return displayName;
    }

    public void setDisplayName(boolean displayName) {
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
