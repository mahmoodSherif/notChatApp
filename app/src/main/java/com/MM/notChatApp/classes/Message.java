package com.MM.notChatApp.classes;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Message {
    private String id;
    private String text;
    private String time;
    private String photoUrl;
    private String audioUrl;
    private String docUrl;
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
    public Message(String time, String audioUrl, int statues , String user ) {
        this.time = time;
        this.audioUrl = audioUrl;
        this.statues = statues;
        sentby = user;
    }
    public Message(int statues,String time, String docUrl , String user ) {
        this.time = time;
        this.docUrl = docUrl;
        this.statues = statues;
        sentby = user;
    }
    public Message() {

    }

    public String getDocUrl() {
        return docUrl;
    }

    public void setDocUrl(String docUrl) {
        this.docUrl = docUrl;
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

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public Map<String,Object>toMap(ArrayList<String> users){
        HashMap<String, Object> result = new HashMap<>();
        result.put("id",id);
        result.put("text",text);
        result.put("time",time);
        result.put("photoUrl",photoUrl);
        result.put("sentby",sentby);
        result.put("audioUrl",audioUrl);
        result.put("docUrl",docUrl);
        for(String phone : users){
            result.put(phone,true);
        }
        return result;
    }
}
