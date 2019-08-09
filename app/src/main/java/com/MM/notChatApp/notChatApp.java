package com.MM.notChatApp;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class notChatApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
