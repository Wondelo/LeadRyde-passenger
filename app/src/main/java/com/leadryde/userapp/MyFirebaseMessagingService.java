package com.leadryde.userapp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.general.call.SinchHandler;
import com.general.files.FireTripStatusMsg;
import com.general.files.GeneralFunctions;
import com.general.files.MyApp;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.sinch.android.rtc.NotificationResult;
import com.sinch.android.rtc.SinchHelpers;
import com.utils.Logger;
import com.utils.Utils;

import java.util.Map;


public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private static final String TAG1 = "MyFirebaseIIDService";


    String authorizedEntity; // Project id from Google Developer Console
    String scope = "GCM"; // e.g. communicating using GCM, but you can use any
    // URL-safe characters up to a maximum of 1000, or
    // you can also leave it blank.

    @Override
    public void onNewToken(String s) {
        // depricated
//        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        if (!Utils.checkText(authorizedEntity)) {
            authorizedEntity = MyApp.getInstance().getGeneralFun(this).retrieveValue(Utils.APP_GCM_SENDER_ID_KEY);
        }
        super.onNewToken(s);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Map data = remoteMessage.getData();
        Logger.d("RemoteMessage", "::" + data.toString());
        if (SinchHelpers.isSinchPushPayload(remoteMessage.getData())) {

            GeneralFunctions generalFunctions = MyApp.getInstance().getGeneralFun(getApplicationContext());
            if (generalFunctions.getMemberId().equals("")) {
                return;
            }


            new ServiceConnection() {
                private Map payload;

                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    if (payload != null) {
                        SinchHandler.getInstance().relayRemotePushNotificationPayload(payload);
                    }
                    payload = null;
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                }

                public void relayMessageData(Map<String, String> data) {
                    payload = data;
                    SinchHandler.getInstance().initiateService();
                }
            }.relayMessageData(data);
            return;

        }


        if (!Utils.checkText(authorizedEntity)) {
            authorizedEntity = MyApp.getInstance().getGeneralFun(this).retrieveValue(Utils.APP_GCM_SENDER_ID_KEY);
        }

        if (remoteMessage == null || remoteMessage.getData() == null/* || remoteMessage.getNotification().getBody() == null*/)
            return;


        String message = remoteMessage.getData().get("message");
        Logger.e("MyFirebaseMessagingService", "::MsgReceived::" + message);

        try {
            PowerManager powerManager = (PowerManager) MyApp.getInstance().getCurrentAct().getSystemService(Context.POWER_SERVICE);
            boolean isScreenOn = powerManager.isScreenOn();
            if (isScreenOn) {
                new FireTripStatusMsg(MyApp.getInstance() != null ? MyApp.getInstance().getCurrentAct() : getApplicationContext()).fireTripMsg(message);
            } else {
                new FireTripStatusMsg(MyApp.getInstance() != null ? MyApp.getInstance().getCurrentAct() : getApplicationContext()).fireTripMsg(message);
            }
        } catch (Exception e) {
            new FireTripStatusMsg(MyApp.getInstance() != null ? MyApp.getInstance().getCurrentAct() : getApplicationContext()).fireTripMsg(message);
        }

    }

}
