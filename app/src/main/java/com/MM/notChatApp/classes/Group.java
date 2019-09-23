package com.MM.notChatApp.classes;
import com.MM.notChatApp.classes.User;
import java.util.ArrayList;
public class Group {
    private String id;
    private String groupName;
    private String photoUrl;
    private ArrayList<String> groupMembers;

    public Group(String id, String groupName, String photoUrl, ArrayList<String> groupMembers) {
        this.id = id;
        this.groupName = groupName;
        this.photoUrl = photoUrl;
        this.groupMembers = groupMembers;
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

    public ArrayList<String> getGroupMembers() {
        return groupMembers;
    }

    public void setGroupMembers(ArrayList<String> groupMembers) {
        this.groupMembers = groupMembers;
    }
}
