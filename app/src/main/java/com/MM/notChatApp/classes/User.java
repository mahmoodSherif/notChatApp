package com.MM.notChatApp.classes;


import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class User {
    private String UserName;
    private String phone;
    private String UserPhotoUrl;
    private String UserBio;
    private String UserStatues;


    public User(){

    }
    public User(String userName, String userPhotoUrl, String phone, String userBio,String status) {
        UserName = userName;
        UserPhotoUrl = userPhotoUrl;
        this.phone = phone;
        UserBio = userBio;
        UserStatues = status;
    }
    public User(String phone){
        this.phone = phone;
    }

    public String getUserStatues() {
        return UserStatues;
    }

    public void setUserStatues(String userStatues) {
        UserStatues = userStatues;
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

    public Task<Void>   addTODatabae(){
        DatabaseReference users = FirebaseDatabase.getInstance().getReference().child("users").child(phone);
        return users.setValue(new User(UserName , UserPhotoUrl , phone , UserBio,UserStatues));
    }
    public DatabaseReference getFormDataBase(String phone){
        return FirebaseDatabase.getInstance().getReference().child("users").child(phone);
    }

}
