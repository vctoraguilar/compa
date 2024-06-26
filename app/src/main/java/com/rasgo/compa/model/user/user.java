package com.rasgo.compa.model.user;

public class user {
    private String userId;
    private String displayName;
    private String email;
    private String photoUrl;
    private String coverUrl;
    private String businessName;
    private String businessEmail;
    private String businessDescription;
    private String fcmToken;



    public user() {
    }

    public user(String userId, String displayName, String email, String photoUrl, String coverUrl, String businessName, String businessEmail, String businessDescription) {
        this.userId = userId;
        this.displayName = displayName;
        this.email = email;
        this.photoUrl = photoUrl;
        this.coverUrl = coverUrl;
        this.businessName = businessName;
        this.businessEmail = businessEmail;
        this.businessDescription = businessDescription;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getBusinessEmail() {
        return businessEmail;
    }

    public void setBusinessEmail(String businessEmail) {
        this.businessEmail = businessEmail;
    }

    public String getBusinessDescription() {
        return businessDescription;
    }

    public void setBusinessDescription(String businessDescription) {
        this.businessDescription = businessDescription;
    }
}
