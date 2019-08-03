package com.MM.notChatApp.classes;

import android.widget.Toast;

import androidx.annotation.NonNull;

import com.MM.notChatApp.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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

    //fireBase functions
    public void addUserToDatabase(){
        FirebaseUser cur = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference users = FirebaseDatabase.getInstance().getReference().child("users")
                .child(cur.getUid());
        users.setValue(new User(cur.getDisplayName(),cur.getPhotoUrl().toString(), null , null , null))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                    }
                });
    }
}
