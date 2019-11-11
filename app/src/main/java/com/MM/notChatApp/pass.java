package com.MM.notChatApp;

import com.MM.notChatApp.classes.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class pass {
    static public ArrayList<User> list = new ArrayList<>();
    static public ArrayList<String> members = new ArrayList<>();

    static public String userPhone = "";

    static public FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
    static public DatabaseReference chatRef = mFirebaseDatabase.getReference().child("chats");
    static public DatabaseReference userRef = mFirebaseDatabase.getReference().child("users").child(userPhone);
    static public DatabaseReference chatListRef = mFirebaseDatabase.getReference().child("chatList");
    static public DatabaseReference groupRef = mFirebaseDatabase.getReference().child("group");

    static public FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    static public StorageReference docRef = firebaseStorage.getReference().child("chat_docs");
    static public StorageReference audioRef = firebaseStorage.getReference().child("chats_audio");
}
