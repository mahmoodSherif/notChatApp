package com.MM.notChatApp;

public interface DBvars {
    interface USER{
        String UserStatues = "UserStatues";
        String phone = "phone";
        String userBio = "userBio";
        String userName = "userName";
        String userPhotoUrl = "userPhotoUrl";
    }
    interface GROUP{
        String groupMembers = "groupMembers";
        String groupName = "groupName";
        String photoUrl = "photoUrl";
        String id = "id";
        String isGroup = "isGroup";
    }
    interface MESSAGE{
        String id = "id";
        String text = "text" ;
        String time = "time";
        String photoUrl = "photoUrl" ;
        String audioUrl = "audioUrl" ;
        String docUrl = "docUrl";
        String sentby = "sentby";
        String  statues = "statues";
    }
    String typring = "typring";

}