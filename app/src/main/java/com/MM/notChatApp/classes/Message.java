package com.MM.notChatApp.classes;


public class Message {
    private String text;
    private String time;
    private String photoUrl;
    private String sentby;
    private String have; // "both" for both users have the message "{user phone}"for the one who has the massage
    /*
    * 0 for unSend
    * 1 for send
    * 2 for riceve
    * 3 for read
    * */
    private int statues;

    public Message(String text, String time, String photoUrl, int statues , String user , String have) {
        this.text = text;
        this.time = time;
        this.photoUrl = photoUrl;
        this.statues = statues;
        sentby = user;
        this.have = have;
    }
    public Message() {

    }

    public String getHave() {
        return have;
    }

    public void setHave(String have) {
        this.have = have;
    }

    public String getSentby() {
        return sentby;
    }

    public void setSentby(String sentby) {
        this.sentby = sentby;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public int getStatues() {
        return statues;
    }

    public void setStatues(int statues) {
        this.statues = statues;
    }
}
