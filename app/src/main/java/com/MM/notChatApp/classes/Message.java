package com.MM.notChatApp.classes;


import java.util.HashMap;
import java.util.Map;

public class Message {
    private String id;
    private String text;
    private String time;
    private String photoUrl;
    private String sentby;
    private boolean haveByMe;
    private boolean haveByFriend;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /*
    * 0 for unSend
    * 1 for send
    * 2 for riceve
    * 3 for read
    * */
    private int statues;

    public Message(String text, String time, String photoUrl, int statues , String user) {
        this.text = text;
        this.time = time;
        this.photoUrl = photoUrl;
        this.statues = statues;
        sentby = user;
    }
    public Message() {

    }

    public boolean isHaveByMe() {
        return haveByMe;
    }

    public void setHaveByMe(boolean haveByMe) {
        this.haveByMe = haveByMe;
    }

    public boolean isHaveByFriend() {
        return haveByFriend;
    }

    public void setHaveByFriend(boolean haveByFriend) {
        this.haveByFriend = haveByFriend;
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

    public Map<String,Object>toMap(String phone , String fPhone){
        HashMap<String, Object> result = new HashMap<>();
        result.put("id",id);
        result.put("text",text);
        result.put("time",time);
        result.put("photoUrl",photoUrl);
        result.put("sentby",sentby);
        result.put(phone,true);
        result.put(fPhone,true);
        return result;
    }
}
