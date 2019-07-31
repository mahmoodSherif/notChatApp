package com.MM.notChatApp;

public class User {
    private String UserName;
    private String UserPhotoUrl;
    private  String UserId;
    private String UserBio;
    private Message UserLastMessage;

    public User(String userName, String userPhotoUrl, String userId, String userBio, Message userLastMessage) {
        UserName = userName;
        UserPhotoUrl = userPhotoUrl;
        UserId = userId;
        UserBio = userBio;
        UserLastMessage = userLastMessage;
    }

    public Message getUserLastMessage() {
        return UserLastMessage;
    }

    public void setUserLastMessage(Message userLastMessage) {
        UserLastMessage = userLastMessage;
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

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public String getUserBio() {
        return UserBio;
    }

    public void setUserBio(String userBio) {
        UserBio = userBio;
    }
}
