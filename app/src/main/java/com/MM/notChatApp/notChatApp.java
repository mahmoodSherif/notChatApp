package com.MM.notChatApp;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class notChatApp extends Application implements Application.ActivityLifecycleCallbacks {
    private void status(String status) {
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("UserStatues",status);
        FirebaseDatabase.getInstance().getReference().child("users").child(
                FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()
        ).updateChildren(hashMap);
    }
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {

    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        status("online");
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        status("offline");
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {

    }
}
