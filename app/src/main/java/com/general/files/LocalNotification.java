package com.general.files;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import com.leadryde.userapp.BookingActivity;
import com.leadryde.userapp.BuildConfig;
import com.leadryde.userapp.HistoryDetailActivity;
import com.leadryde.userapp.OnGoingTripDetailsActivity;
import com.leadryde.userapp.R;
import com.utils.IntentAction;
import com.utils.Logger;
import com.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by Admin on 20/03/18.
 */

public class LocalNotification {
    static Context mContext;

    private static String CHANNEL_ID = BuildConfig.APPLICATION_ID + "";
    private static NotificationManager mNotificationManager = null;

    public static void dispatchLocalNotification(Context context, String message, boolean onlyInBackground, boolean isOnlySoundPlay) {
        mContext = context;

        if (MyApp.getInstance().getCurrentAct() == null && mContext == null) {
            return;
        }

        continueDispatchNotification(message, onlyInBackground, isOnlySoundPlay);
    }

    private static void continueDispatchNotification(String message, boolean onlyInBackground, boolean isOnlySoundPlay) {
        Intent intent = null;

        if (Utils.getPreviousIntent(mContext) != null) {
            intent = Utils.getPreviousIntent(mContext);
        } else {
            intent = mContext
                    .getPackageManager()
                    .getLaunchIntentForPackage(mContext.getPackageName());

            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT |
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        }

        PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, intent, IntentAction.getPendingIntentFlag());

        GeneralFunctions generalFunctions = MyApp.getInstance().getGeneralFun(mContext);
        String userProfileJson = generalFunctions.retrieveValue(Utils.USER_PROFILE_JSON);

        Uri soundUri = Settings.System.DEFAULT_NOTIFICATION_URI;

        if (generalFunctions.getJsonValue("USER_NOTIFICATION", userProfileJson).equalsIgnoreCase("notification_1.mp3")) {
            soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + mContext.getPackageName() + "/" + R.raw.notification_1);
        } else if (generalFunctions.getJsonValue("USER_NOTIFICATION", userProfileJson).equalsIgnoreCase("notification_2.mp3")) {
            soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + mContext.getPackageName() + "/" + R.raw.notification_2);
        } else if (generalFunctions.getJsonValue("USER_NOTIFICATION", userProfileJson).equalsIgnoreCase("notification_3.mp3")) {
            soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + mContext.getPackageName() + "/" + R.raw.notification_3);
        } else if (generalFunctions.getJsonValue("USER_NOTIFICATION", userProfileJson).equalsIgnoreCase("notification_4.mp3")) {
            soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + mContext.getPackageName() + "/" + R.raw.notification_4);
        } else if (generalFunctions.getJsonValue("USER_NOTIFICATION", userProfileJson).equalsIgnoreCase("notification_5.mp3")) {
            soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + mContext.getPackageName() + "/" + R.raw.notification_5);
        } else if (generalFunctions.getJsonValue("USER_NOTIFICATION", userProfileJson).equalsIgnoreCase("notification_6.mp3")) {
            soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + mContext.getPackageName() + "/" + R.raw.notification_6);
        } else if (generalFunctions.getJsonValue("USER_NOTIFICATION", userProfileJson).equalsIgnoreCase("notification_7.mp3")) {
            soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + mContext.getPackageName() + "/" + R.raw.notification_7);
        } else if (generalFunctions.getJsonValue("USER_NOTIFICATION", userProfileJson).equalsIgnoreCase("notification_8.mp3")) {
            soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + mContext.getPackageName() + "/" + R.raw.notification_8);
        } else if (generalFunctions.getJsonValue("USER_NOTIFICATION", userProfileJson).equalsIgnoreCase("notification_9.mp3")) {
            soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + mContext.getPackageName() + "/" + R.raw.notification_9);
        } else if (generalFunctions.getJsonValue("USER_NOTIFICATION", userProfileJson).equalsIgnoreCase("notification_10.mp3")) {
            soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + mContext.getPackageName() + "/" + R.raw.notification_10);
        }


        if (mNotificationManager != null) {
            mNotificationManager.cancelAll();
            mNotificationManager = null;
        }

        // Receive Notifications in >26 version devices
        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // mBuilder.setChannelId(BuildConfig.APPLICATION_ID);
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    mContext.getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_HIGH
            );
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            // channel.setSound(soundUri, audioAttributes);

            if (mNotificationManager != null) {
                mNotificationManager.createNotificationChannel(channel);
            }
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_rider_logo)
                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher))
                .setContentTitle(mContext.getString(R.string.app_name))
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(contentIntent)
                //  .setSound(soundUri)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH);


        if (onlyInBackground && MyApp.getInstance().isMyAppInBackGround()) {
//            mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(Utils.NOTIFICATION_ID, mBuilder.build());
            playNotificationSound(soundUri);
        } else if (!onlyInBackground) {
//            mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(Utils.NOTIFICATION_ID, mBuilder.build());
            playNotificationSound(soundUri);
        } /*else if (!MyApp.getInstance().isMyAppInBackGround() && isOnlySoundPlay){
            playNotificationSound(soundUri);
        }*/
    }

    private RemoteViews NormalView;
    private RemoteViews expandView;
    AppWidgetManager appWidgetManager;
    private Notification notification;

    GeneralFunctions generalFunc;
    JSONObject jsonObject;
    static String iOrderId;
    static String iTripId;
    static String iCabBookingId;
    static String eType;
    static String eFinished;

    public void customNotification(Context context, String Data) {
        Logger.d("customNotification", "::called::" + Data);
        mContext = context;
        generalFunc = new GeneralFunctions(mContext);

        jsonObject = generalFunc.getJsonObject(Data);
        iOrderId = generalFunc.getJsonValueStr("iOrderId", jsonObject);
        iTripId = generalFunc.getJsonValueStr("iTripId", jsonObject);

        initNotification();

    }

    private void initNotification() {


        if (mNotificationManager != null) {
            mNotificationManager.cancelAll();
            mNotificationManager = null;
        }

        // Receive Notifications in >26 version devices
        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, (int) System.currentTimeMillis(), new Intent(IntentAction.NOTIFICATION_CLICK), IntentAction.getPendingIntentFlag());
        PendingIntent viewOrderIntent = PendingIntent.getBroadcast(mContext, (int) System.currentTimeMillis(), new Intent(IntentAction.NOTIFICATION_VIEW_ORDER), IntentAction.getPendingIntentFlag());
        PendingIntent trackOrderIntent = PendingIntent.getBroadcast(mContext, (int) System.currentTimeMillis(), new Intent(IntentAction.NOTIFICATION_TRACK_ORDER), IntentAction.getPendingIntentFlag());

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, CHANNEL_ID);
        builder.setSmallIcon(R.drawable.ic_stat_rider_logo)
                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher))
                .setTicker(mContext.getString(R.string.app_name))
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);


        NormalView = new RemoteViews(mContext.getPackageName(), R.layout.notification_view);
        builder.setCustomContentView(NormalView);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            expandView = new RemoteViews(mContext.getPackageName(), generalFunc.isRTLmode() ? R.layout.notification_big_view_rtl : R.layout.notification_big_view);
            builder.setCustomBigContentView(expandView);
            builder.setPriority(Notification.PRIORITY_MAX);
            expandView.setOnClickPendingIntent(R.id.viewdetailsTxt, viewOrderIntent);
            expandView.setOnClickPendingIntent(R.id.trackorderTxt, trackOrderIntent);
            expandView.setTextViewText(R.id.titleTxt, generalFunc.getJsonValueStr("CustomSubTitle", jsonObject));
            NormalView.setTextViewText(R.id.titleTxt, generalFunc.getJsonValueStr("CustomSubTitle", jsonObject));
            NormalView.setTextViewText(R.id.statusTxt, generalFunc.getJsonValueStr("CustomTitle", jsonObject));
            expandView.setTextViewText(R.id.statusTxt, generalFunc.getJsonValueStr("CustomTitle", jsonObject));


            JSONArray obj_arr = generalFunc.getJsonArray("CustomMessage", jsonObject);

            if (obj_arr != null) {
                int j = obj_arr.length();
                for (int i = 0; i < obj_arr.length(); i++) {
                    JSONObject obj_temp = generalFunc.getJsonObject(obj_arr, i);
                    if (generalFunc.isRTLmode()) {
                        j--;
                        obj_temp = generalFunc.getJsonObject(obj_arr, j);

                    }
                    RemoteViews newView = new RemoteViews(mContext.getPackageName(), R.layout.item_custom_notification);
                    //newView.setTextViewText(R.id.carTypeTitle, generalFunc.getJsonValueStr("vStatus", obj_temp));
                   String status= generalFunc.getJsonValueStr("vStatus", obj_temp);
                    if (i == 0) {
                        eType = generalFunc.getJsonValueStr("eType", obj_temp);
                        eFinished = generalFunc.getJsonValueStr("eFinished", obj_temp);
                        iCabBookingId = generalFunc.getJsonValueStr("iCabBookingId", obj_temp);
                    }
                    // if (generalFunc.getJsonValueStr("iStatusCode", obj_temp).equalsIgnoreCase(generalFunc.getJsonValueStr("OrderCurrentStatusCode", obj_temp))) {
                    if (generalFunc.getJsonValueStr("isCurrentStatus", obj_temp).equalsIgnoreCase("Yes")) {


                        manageIcon(generalFunc.getJsonValueStr("icon", obj_temp), newView, true);
                        newView.setViewVisibility(R.id.imagareaselcted, View.VISIBLE);
                        newView.setViewVisibility(R.id.imagarea, View.GONE);
                        newView.setTextViewText(R.id.carTypeTitle, generalFunc.getJsonValueStr("vStatus", obj_temp));
                        SpannableString s = new SpannableString(status);
                        s.setSpan(new StyleSpan(Typeface.BOLD), 0, s.length(), 0);
                        newView.setTextViewText(R.id.carTypeTitle,s);



                    } else {
                        manageIcon(generalFunc.getJsonValueStr("icon", obj_temp), newView, false);
                        newView.setViewVisibility(R.id.imagareaselcted, View.GONE);
                        newView.setViewVisibility(R.id.imagarea, View.VISIBLE);

                        SpannableString s = new SpannableString(status);
                        s.setSpan(new StyleSpan(Typeface.NORMAL), 0, s.length(), 0);
                        newView.setTextViewText(R.id.carTypeTitle,s);
                    }
                    if(MyApp.getInstance().isDrakModeOn())
                    {
                        newView.setTextColor(R.id.carTypeTitle, Color.parseColor("#ffffff"));
                        NormalView.setTextColor(R.id.statusTxt, Color.parseColor("#ffffff"));
                        NormalView.setTextColor(R.id.titleTxt, Color.parseColor("#ffffff"));
                    }

                    if (generalFunc.getJsonValueStr("CustomViewBtn", jsonObject) != null && generalFunc.getJsonValueStr("CustomViewBtn", jsonObject).equalsIgnoreCase("Yes")) {
                        expandView.setViewVisibility(R.id.viewdetailsTxt, View.VISIBLE);
                        expandView.setTextViewText(R.id.viewdetailsTxt, generalFunc.retrieveLangLBl("View Details", "LBL_VIEW_DETAILS"));
                    } else {
                        expandView.setViewVisibility(R.id.viewdetailsTxt, View.GONE);
                    }

                    if (generalFunc.getJsonValueStr("CustomTrackDetails", jsonObject) != null && generalFunc.getJsonValueStr("CustomTrackDetails", jsonObject).equalsIgnoreCase("Yes")) {
                        expandView.setViewVisibility(R.id.trackorderTxt, View.VISIBLE);
                        expandView.setTextViewText(R.id.trackorderTxt, generalFunc.retrieveLangLBl("Track Order", "LBL_TRACK_ORDER"));
                    } else {
                        expandView.setViewVisibility(R.id.trackorderTxt, View.GONE);
                    }


                    expandView.addView(R.id.trackview, newView);
                    NormalView.addView(R.id.trackview, newView);


                }
            }
        }

        mNotificationManager.notify(75, builder.build());


    }

    public void manageIcon(String iconName, RemoteViews view, boolean isSelected) {
        int selectedimgId = 0;
        int normalimgId = 0;

        if (generalFunc.isRTLmode()) {
            selectedimgId = R.id.carTypeImgViewselctedrtl;
            normalimgId = R.id.carTypeImgViewrtl;

            view.setViewVisibility(R.id.carTypeImgViewrtl, View.VISIBLE);
            view.setViewVisibility(R.id.carTypeImgViewselctedrtl, View.VISIBLE);
            view.setViewVisibility(R.id.carTypeImgViewselcted, View.GONE);
            view.setViewVisibility(R.id.carTypeImgView, View.GONE);

        } else {
            selectedimgId = R.id.carTypeImgViewselcted;
            normalimgId = R.id.carTypeImgView;
            view.setViewVisibility(R.id.carTypeImgViewrtl, View.GONE);
            view.setViewVisibility(R.id.carTypeImgViewselctedrtl, View.GONE);
            view.setViewVisibility(R.id.carTypeImgViewselcted, View.VISIBLE);
            view.setViewVisibility(R.id.carTypeImgView, View.VISIBLE);
        }


        if (iconName.equalsIgnoreCase("Active")) {
            view.setImageViewResource(isSelected ? selectedimgId : normalimgId, R.drawable.ic_cust_active);
        } else if (iconName.equalsIgnoreCase("Arrived")) {
            view.setImageViewResource(isSelected ? selectedimgId : normalimgId, R.drawable.ic_cust_arrived);
        } else if (iconName.equalsIgnoreCase("Arriving")) {
            view.setImageViewResource(isSelected ? selectedimgId : normalimgId, R.drawable.ic_cust_arriving);
        } else if (iconName.equalsIgnoreCase("cancelled")&& (eType.equalsIgnoreCase(Utils.CabGeneralType_Ride) || eType.equalsIgnoreCase(Utils.CabGeneralType_UberX) ||
                eType.equals("Deliver") || eType.equalsIgnoreCase(Utils.eType_Multi_Delivery)) ) {
            view.setImageViewResource(isSelected ? selectedimgId : normalimgId, R.drawable.ic_cust_cancelled);
        } else if (iconName.equalsIgnoreCase("finished")) {
            view.setImageViewResource(isSelected ? selectedimgId : normalimgId, R.drawable.ic_cust_finished);
        } else if (iconName.equalsIgnoreCase("OnGoingTrip")) {
            view.setImageViewResource(isSelected ? selectedimgId : normalimgId, R.drawable.ic_cust_ongoingtrip);
        } else if (iconName.equalsIgnoreCase("Placed")) {
            view.setImageViewResource(isSelected ? selectedimgId : normalimgId, R.drawable.ic_cust_order_place);
        } else if (iconName.equalsIgnoreCase("AcceptedStore")) {
            view.setImageViewResource(isSelected ? selectedimgId : normalimgId, R.drawable.ic_cust_acceptby_rest);
        } else if (iconName.equalsIgnoreCase("Pickedup")) {
            view.setImageViewResource(isSelected ? selectedimgId : normalimgId, R.drawable.ic_cust_pickup);
        } else if (iconName.equalsIgnoreCase("Delivered")) {
            view.setImageViewResource(isSelected ? selectedimgId : normalimgId, R.drawable.ic_cust_deliverd);
        } else if (iconName.equalsIgnoreCase("Cancelled") || iconName.equalsIgnoreCase("Declined")) {
            view.setImageViewResource(isSelected ? selectedimgId : normalimgId, R.drawable.ic_cust_declineby_rest);
        } else if (iconName.equalsIgnoreCase("Review")) {
            view.setImageViewResource(isSelected ? selectedimgId : normalimgId, R.drawable.ic_cust_confirm_item);
        } else if (iconName.equalsIgnoreCase("Billing") || iconName.equalsIgnoreCase("Billing-Cash")) {
            view.setImageViewResource(isSelected ? selectedimgId : normalimgId, R.drawable.ic_cust_billing_payment);
        }
        else if ( iconName.equalsIgnoreCase("Billing-Card")) {
            view.setImageViewResource(isSelected ? selectedimgId : normalimgId, R.drawable.ic__cust_billing_card);
        }


    }

    public static void mangeNotificationclick() {
        Intent intent = null;

        if (Utils.getPreviousIntent(mContext) != null) {
            intent = Utils.getPreviousIntent(mContext);
        } else {
            intent = mContext
                    .getPackageManager()
                    .getLaunchIntentForPackage(mContext.getPackageName());

            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT |
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        }
        MyApp.getInstance().getApplicationContext().startActivity(intent);
        clearAllNotifications();
    }

    static class ActionReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent data) {

            Logger.d("ActionReceiver", "::called");

            if (data == null || data.getAction() == null) {
                return;
            }
            switch (data.getAction()) {
                case IntentAction.NOTIFICATION_CLICK:
                    mangeNotificationclick();

                    break;
                case IntentAction.NOTIFICATION_VIEW_ORDER:

//                    Bundle bn = new Bundle();
//                    bn.putBoolean("isRestart", true);
//                    bn.putBoolean("fromNoti", true);
//                    bn.putBoolean("isCustmNoti", true);
//                    bn.putString("iOrderId", iOrderId);

                    Logger.d("ActionReceiver", "::" + eType);
                    Intent intentrd = null;
                    // new StartActProcess(MyApp.getInstance().getApplicationContext()).startActWithData(OrderDetailsActivity.class, bn);
                    if (eType.equalsIgnoreCase("Ride") || eType.equalsIgnoreCase("Deliver")) {

                        if (Utils.getPreviousIntent(mContext) != null) {
                            intentrd = Utils.getPreviousIntent(mContext);
                        } else {
                            intentrd = mContext
                                    .getPackageManager()
                                    .getLaunchIntentForPackage(mContext.getPackageName());

                            intentrd.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT |
                                    Intent.FLAG_ACTIVITY_NEW_TASK |
                                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                        }
                        MyApp.getInstance().getApplicationContext().startActivity(intentrd);
                        clearAllNotifications();
//                        Intent intent = new Intent(MyApp.getInstance().getApplicationContext(), LauncherActivity.class);
//
//                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        MyApp.getInstance().getApplicationContext().startActivity(intent);
//                        clearAllNotifications();
                        return;

                    } else if (eType.equalsIgnoreCase("UberX") || eType.equalsIgnoreCase("Multi-Delivery")) {
                        if (eFinished != null && eFinished.equalsIgnoreCase("Yes")) {
                            Intent intent = new Intent(MyApp.getInstance().getApplicationContext(), HistoryDetailActivity.class);
                            intent.putExtra("isRestart", true);
                            intent.putExtra("eType", eType);
                            intent.putExtra("iTripId", iTripId);
                            intent.putExtra("iCabBookingId", iCabBookingId);

                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            MyApp.getInstance().getApplicationContext().startActivity(intent);
                            clearAllNotifications();
                        } else {
                            Intent intent = new Intent(MyApp.getInstance().getApplicationContext(), OnGoingTripDetailsActivity.class);
                            intent.putExtra("isRestart", true);
                            intent.putExtra("eType", eType);
                            intent.putExtra("iTripId", iTripId);

                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            MyApp.getInstance().getApplicationContext().startActivity(intent);
                            clearAllNotifications();
                        }
                        return;

                    }



                    clearAllNotifications();


                    break;
                case IntentAction.NOTIFICATION_TRACK_ORDER:

//                    Bundle bn1 = new Bundle();
//                    bn1.putBoolean("isRestart", true);
//                    bn1.putString("iOrderId", iOrderId);
//                    bn1.putBoolean("fromNoti", true);
//                    bn1.putBoolean("isOrder", true);
//                    bn1.putBoolean("isCustmNoti", true);
//                    new StartActProcess(MyApp.getInstance().getApplicationContext()).startActWithData(BookingActivity.class, bn1);


                    Intent intent1 = new Intent(MyApp.getInstance().getApplicationContext(), BookingActivity.class);
                    intent1.putExtra("isRestart", true);
                    intent1.putExtra("fromNoti", true);
                    intent1.putExtra("isCustmNoti", true);
                    intent1.putExtra("iOrderId", iOrderId);
                    intent1.putExtra("isOrder", true);

                    intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    MyApp.getInstance().getApplicationContext().startActivity(intent1);
                    clearAllNotifications();


                    break;
                case IntentAction.NOTIFICATION_CLOSE:
                    clearAllNotifications();
                    break;
            }

        }

    }

    public static void playNotificationSound(Uri nofifyUrl) {
        try {

            Ringtone r = RingtoneManager.getRingtone(mContext, nofifyUrl);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void clearAllNotifications() {
        if (mNotificationManager != null) {
            mNotificationManager.cancelAll();
            mNotificationManager = null;
        }
    }
}
