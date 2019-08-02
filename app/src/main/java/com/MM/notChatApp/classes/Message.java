package com.MM.notChatApp.classes;


public class Message {
    private String text;
    private String time;
    private String photoUrl;
    /*
    * 0 for unSend
    * 1 for send
    * 2 for riceve
    * 3 for read
    * */
    private int statues;

    public Message(String text, String time, String photoUrl, int statues) {
        this.text = text;
        this.time = time;
        this.photoUrl = photoUrl;
        this.statues = statues;
    }
    public Message()
    {

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
