package com.MM.notChatApp.classes;


public class User {
    private String UserName;
    private String phone;
    private String UserPhotoUrl;
    private String UserBio;

    public User(){

    }
    public User(String userName, String userPhotoUrl, String phone, String userBio) {
        UserName = userName;
        UserPhotoUrl = userPhotoUrl;
        this.phone = phone;
        UserBio = userBio;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public String getUserPhotoUrl() {
        return UserPhotoUrl;
    }

    public void setUserPhotoUrl(String userPhotoUrl) {
        UserPhotoUrl = userPhotoUrl;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUserBio() {
        return UserBio;
    }

    public void setUserBio(String userBio) {
        UserBio = userBio;
    }

}
