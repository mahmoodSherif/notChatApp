package com.MM.notChatApp.classes;
import com.MM.notChatApp.classes.User;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;

public class Group {
    private String id;
    private String groupName;
    private String photoUrl;
    private HashMap<String , Boolean> groupMembers;

    public Group(String id, String groupName, String photoUrl, ArrayList<String> groupMembers) {
        this.id = id;
        this.groupName = groupName;
        this.photoUrl = photoUrl;
        this.groupMembers = new HashMap<>();
        for(int i = 0;i<groupMembers.size();i++){
            this.groupMembers.put(groupMembers.get(i) , true);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public HashMap<String, Boolean> getGroupMembers() {
        return groupMembers;
    }

    public void setGroupMembers(HashMap<String, Boolean> groupMembers) {
        this.groupMembers = groupMembers;
    }
}
