package com.google.firebase.iid;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessaging;

public class FirebaseInstanceId {

    public static FirebaseInstanceId instance;

    String vDeviceToken = "";

    public static FirebaseInstanceId getInstance() {
        if (instance == null) {
            instance = new FirebaseInstanceId();
        }
        return instance;
    }

    public String getToken(String senderId, String scope) {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                vDeviceToken = task.getResult();
            }
        });


        while(vDeviceToken.trim().equalsIgnoreCase("")){
        }

        return vDeviceToken;
    }
}
