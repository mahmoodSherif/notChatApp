package com.MM.notChatApp;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class notChatApp extends Application implements Application.ActivityLifecycleCallbacks {
    private static void  status(String status) {
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("UserStatues",status);
        if(FirebaseAuth.getInstance().getCurrentUser() == null)
            return;
        FirebaseDatabase.getInstance().getReference().child("users").child(
                FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()
        ).updateChildren(hashMap);
    }

    private static final AtomicBoolean applicationBackgrounded = new AtomicBoolean(true);
    private static final long INTERVAL_BACKGROUND_STATE_CHANGE = 750L;
    private static WeakReference<Activity> currentActivityReference;

    @Override
    public void onCreate() {
        super.onCreate();
        this.registerActivityLifecycleCallbacks(this);
    }

    private void determineForegroundStatus() {
        if (applicationBackgrounded.get()) {
            onEnterForeground();
            applicationBackgrounded.set(false);
        }
    }

    private void determineBackgroundStatus() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!applicationBackgrounded.get() && currentActivityReference == null) {
                    applicationBackgrounded.set(true);
                    onEnterBackground();
                }
            }
        }, INTERVAL_BACKGROUND_STATE_CHANGE);
    }

    public static void onEnterForeground() {
        status("online");
        //This is where you'll handle logic you want to execute when your application enters the foreground
    }

    public static void onEnterBackground() {
        status("offline");
        //This is where you'll handle logic you want to execute when your application enters the background
    }

    @Override
    public void onActivityResumed(Activity activity) {
        notChatApp.currentActivityReference = new WeakReference<>(activity);
        determineForegroundStatus();
    }

    @Override
    public void onActivityPaused(Activity activity) {
        notChatApp.currentActivityReference = null;
        determineBackgroundStatus();
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        // if you want to do something when every activity is created, do it here
    }

    @Override
    public void onActivityStarted(Activity activity) {
        // if you want to do something when every activity is started, do it here
    }

    @Override
    public void onActivityStopped(Activity activity) {
        // if you want to do something when every activity is stopped, do it here
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        // if you want to do something when an activity saves its instance state, do it here
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        // if you want to do something when every activity is destroyed, do it here
    }
}

