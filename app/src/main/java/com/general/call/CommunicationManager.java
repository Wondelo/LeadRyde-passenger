package com.general.call;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.dialogs.CommunicationCallTypeDialog;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.MyApp;
import com.general.files.StartActProcess;
import com.sinch.android.rtc.calling.Call;
import com.leadryde.userapp.ChatActivity;
import com.utils.Utils;

import org.json.JSONObject;

import java.util.HashMap;

public class CommunicationManager {

    private static CommunicationManager instance;
    private final CommunicationCallTypeDialog mCommunicationCallTypeDialog;

    public static String IS_INCOMING_VIEW = "IS_INCOMING_VIEW";
    public static String MY_DATA = "MY_DATA";

    public static TYPE COMM_TYPE = TYPE.NONE;
    public static MEDIA MEDIA_TYPE = MEDIA.DEFAULT;
    private GeneralFunctions mGeneralFunc;
    public static boolean isOnlyVideo = false;

    public enum TYPE {
        PHONE_CALL,
        CHAT,
        VIDEO_CALL,
        VOIP_CALL,
        BOTH_CALL,
        NONE,
        OTHER
    }

    public enum MEDIA {
        SINCH,
        TWILIO,
        LOCAL,
        DEFAULT
    }

    public static CommunicationManager getInstance() {
        if (instance == null) {
            instance = new CommunicationManager();
        }
        return instance;
    }

    public CommunicationManager() {
        this.mCommunicationCallTypeDialog = new CommunicationCallTypeDialog();
        this.mCommunicationCallTypeDialog.setListener(this::continueCallAction);
    }

    public void initiateService(@NonNull GeneralFunctions generalFunc, @NonNull String jsonValue) {
        this.mGeneralFunc = generalFunc;

        // Set Audio/Video Calling Method for Apps
        final String audioCallingMethod = generalFunc.getJsonValue("AUDIO_CALLING_METHOD", jsonValue);
        switch (audioCallingMethod.toUpperCase()) {
            case "SINCH":
                MEDIA_TYPE = MEDIA.SINCH;
                break;
            case "TWILIO":
                MEDIA_TYPE = MEDIA.TWILIO;
                break;
            case "LOCAL":
                MEDIA_TYPE = MEDIA.LOCAL;
                break;
        }

        // Calling Method
        final String callingMethod = generalFunc.getJsonValue("RIDE_DRIVER_CALLING_METHOD", jsonValue);
        switch (callingMethod.toUpperCase()) {
            case "VOIP":
                COMM_TYPE = TYPE.VOIP_CALL;
                break;
            case "VIDEOCALL":
                COMM_TYPE = TYPE.VIDEO_CALL;
                break;
            case "VOIP-VIDEOCALL":
                COMM_TYPE = TYPE.BOTH_CALL;
                break;
            case "NORMAL":
                COMM_TYPE = TYPE.NONE;
                MEDIA_TYPE = MEDIA.DEFAULT;
                break;
        }

        String mName = generalFunc.getJsonValue("vName", jsonValue);
        String mImage = generalFunc.getJsonValue("vImgName", jsonValue);


        switch (MEDIA_TYPE) {
            case SINCH:
                SinchHandler.getInstance().initiateService();
                break;
            case TWILIO:
                TwilioHandler.getInstance().initiateService();
                break;
            case LOCAL:
            //    LocalHandler.getInstance().initiateService(mName, mImage);
                break;
            case DEFAULT:
                DefaultCommunicationHandler.getInstance().initiateService();
                break;
        }
    }

    public void communicate(Context mContext, MediaDataProvider dataProvider, TYPE type) {
        if (type == TYPE.CHAT) {
            openChat(mContext, dataProvider);
        } else {
            toCall(mContext, dataProvider);
        }
    }

    public void communicateVideo(Context mContext, MediaDataProvider dataProvider, TYPE type) {
        mCommunicationCallTypeDialog.checkPermissions(mContext, type, dataProvider);
    }

    private void toCall(Context mContext, MediaDataProvider dataProvider) {
        switch (dataProvider.media) {
            case SINCH:
            case TWILIO:
            case LOCAL:
                if (COMM_TYPE == TYPE.BOTH_CALL) {
                    mCommunicationCallTypeDialog.showPreferenceDialog(mContext, dataProvider);
                } else {
                    mCommunicationCallTypeDialog.checkPermissions(mContext, COMM_TYPE, dataProvider);
                }
                break;
            case DEFAULT:
                DefaultCommunicationHandler.getInstance().executeAction(mContext, TYPE.PHONE_CALL, dataProvider);
                break;
        }
    }

    private void continueCallAction(Context mContext, TYPE communication_type, MediaDataProvider dataProvider) {
        if (communication_type == TYPE.VIDEO_CALL) {
            dataProvider.isVideoCall = true;
        }
        switch (dataProvider.media) {
            case SINCH:
                SinchHandler.getInstance().executeAction(mContext, communication_type, dataProvider);
                break;
            case TWILIO:
                TwilioHandler.getInstance().executeAction(mContext, communication_type, dataProvider);
                break;
            case LOCAL:
            //    LocalHandler.getInstance().executeAction(mContext, communication_type, dataProvider);
                break;
            case DEFAULT:
                DefaultCommunicationHandler.getInstance().executeAction(mContext, TYPE.PHONE_CALL, dataProvider);
                return;
        }
        openCallScreen(mContext, dataProvider);
        sendNotification(mContext, dataProvider, "No");
    }

    /*public void communicateOnlyVideo(Context mContext, MediaDataProvider dataProvider) {
        final String audioCallingMethod = mGeneralFunc.getJsonValue("AUDIO_CALLING_METHOD", mGeneralFunc.retrieveValue(Utils.USER_PROFILE_JSON));
        switch (audioCallingMethod.toUpperCase()) {
            case "SINCH":
                MEDIA_TYPE = MEDIA.SINCH;
                break;
            case "TWILIO":
                MEDIA_TYPE = MEDIA.TWILIO;
                break;
            case "LOCAL":
                MEDIA_TYPE = MEDIA.LOCAL;
                break;
        }
        dataProvider.media = MEDIA_TYPE;
        //mCommunicationCallTypeDialog.btnClick = true;
        mCommunicationCallTypeDialog.checkPermissions(mContext, TYPE.VIDEO_CALL, dataProvider);
    }*/

    private void sendNotification(Context mContext, MediaDataProvider dataProvider, String isCallEnded) {
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("type", "SendTripMessageNotification");
        parameters.put("UserType", Utils.userType);
        parameters.put("eSystem", dataProvider.toMemberType.equalsIgnoreCase(Utils.CALLTOSTORE) ? "DeliverAll" : "");

        parameters.put("iFromMemberId", mGeneralFunc.getMemberId());
        parameters.put("iToMemberId", dataProvider.toMemberType + "_" + dataProvider.callId);

        parameters.put("isForVoip", "Yes");
        parameters.put("isCallEnded", isCallEnded);

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(mContext, parameters);
        exeWebServer.setDataResponseListener(responseString -> {
        });
        exeWebServer.execute();
    }

    private void openCallScreen(Context mContext, MediaDataProvider dataProvider) {
        Intent callScreen = new Intent(mContext, V3CallScreen.class);
        callScreen.putExtra(MY_DATA, dataProvider);
        callScreen.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        mContext.startActivity(callScreen);
    }

    private void openChat(Context mContext, MediaDataProvider dataProvider) {
        if (dataProvider.media == MEDIA.DEFAULT) {
            DefaultCommunicationHandler.getInstance().executeAction(mContext, TYPE.CHAT, dataProvider);
        } else {
            Bundle bnChat = new Bundle();
            bnChat.putString("iFromMemberId", dataProvider.callId);
            bnChat.putString("FromMemberImageName", dataProvider.toMemberImage);
            bnChat.putString("iTripId", dataProvider.iTripId);
            bnChat.putString("FromMemberName", dataProvider.toMemberName);
            bnChat.putString("vBookingNo", dataProvider.vBookingNo);

            if (dataProvider.isBid) {
                bnChat.putString("iBiddingPostId", dataProvider.iTripId);
                bnChat.putString("iDriverId",  dataProvider.callId);
            }
            new StartActProcess(mContext).startActWithData(ChatActivity.class, bnChat);
        }
    }

    public void incomingCommunicate(Context mContext, GeneralFunctions generalFunc, Call mCall, JSONObject obj_msg) {
        MediaDataProvider mDataProvider = null;
        switch (MEDIA_TYPE) {
            case SINCH:
                mDataProvider = new MediaDataProvider.Builder()
                        .setCallId(mCall.getHeaders().get("Id"))
                        .setToMemberType(mCall.getHeaders().get("type"))
                        .setToMemberName(mCall.getHeaders().get("Name"))
                        .setToMemberImage(mCall.getHeaders().get("PImage"))
                        .setVideoCall(mCall.getDetails().isVideoOffered())
                        .build();
                break;
            case TWILIO:
                if (generalFunc.getJsonValueStr("isDecline", obj_msg) != null && generalFunc.getJsonValueStr("isDecline", obj_msg).equalsIgnoreCase("Yes")) {
                    if (MyApp.getInstance().getCurrentAct() instanceof V3CallScreen) {
                        V3CallScreen incomingCallScreenActivity = (V3CallScreen) MyApp.getInstance().getCurrentAct();
                        incomingCallScreenActivity.onCallEnded();
                    }
                    return;
                }
                mDataProvider = new MediaDataProvider.Builder()
                        .setFromMemberId(generalFunc.getJsonValueStr("fromMemberId", obj_msg))
                        .setFromMemberType(generalFunc.getJsonValueStr("fromMemberType", obj_msg))
                        .setToMemberName(generalFunc.getJsonValueStr("Name", obj_msg))
                        .setToMemberImage(generalFunc.getJsonValueStr("PImage", obj_msg))
                        .setToMemberType(generalFunc.getJsonValueStr("toMemberType", obj_msg))
                        .setToMemberId(generalFunc.getJsonValueStr("toMemberId", obj_msg))
                        .setVideoCall(generalFunc.getJsonValueStr("isVideoCall", obj_msg).equalsIgnoreCase("Yes"))
                        .setRoomName(generalFunc.getJsonValueStr("roomName", obj_msg))
                        .build();
                TwilioHandler.getInstance().setDataProvider(mDataProvider);
                break;
            case LOCAL:
                mDataProvider = new MediaDataProvider.Builder()
                        .setCallId(generalFunc.getJsonValueStr("Id", obj_msg))
                        .setToMemberType(generalFunc.getJsonValueStr("type", obj_msg))
                        .setToMemberName(generalFunc.getJsonValueStr("Name", obj_msg))
                        .setToMemberImage(generalFunc.getJsonValueStr("PImage", obj_msg))
                        .setVideoCall(generalFunc.getJsonValueStr("isVideoCall", obj_msg).equalsIgnoreCase("Yes"))
                        .build();
                break;
        }
        if (MyApp.getInstance().getCurrentAct() != null) {
            if (MyApp.getInstance().getCurrentAct() instanceof V3CallScreen) {
                return;
            }
        }
        Intent callScreen = new Intent(mContext, V3CallScreen.class);
        callScreen.putExtra(CommunicationManager.IS_INCOMING_VIEW, true);
        callScreen.putExtra(CommunicationManager.MY_DATA, mDataProvider);
        callScreen.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        mContext.startActivity(callScreen);
    }

    public void setListener(V3CallScreen callScreen, V3CallListener V3CallListener) {
        switch (MEDIA_TYPE) {
            case SINCH:
                //SinchHandler.getInstance().setListener(callScreen, V3CallListener);
                break;
            case TWILIO:
                TwilioHandler.getInstance().setListener(callScreen, V3CallListener);
                break;
            case LOCAL:
             //   LocalHandler.getInstance().setListener(callScreen, V3CallListener);
                break;
        }
    }

    public void setUIListener(MediaDataProvider dataProvider, V3CallScreen callScreen, V3CallScreen v3CallScreen) {
        switch (MEDIA_TYPE) {
            case SINCH:
                SinchHandler.getInstance().setUIListener(dataProvider, callScreen, v3CallScreen);
                break;
            case TWILIO:
                TwilioHandler.getInstance().setUIListener(dataProvider, callScreen, v3CallScreen);
                break;
            case LOCAL:
           //     LocalHandler.getInstance().setUIListener(dataProvider,callScreen, v3CallScreen);
                break;
        }
    }

    public void setMuteButtonAction(boolean isMute) {
        switch (MEDIA_TYPE) {
            case SINCH:
                SinchHandler.getInstance().muteBtnClicked(isMute);
                break;
            case TWILIO:
                TwilioHandler.getInstance().muteBtnClicked();
                break;
            case LOCAL:
            //    LocalHandler.getInstance().muteBtnClicked(isMute);
                break;
        }
    }

    public void setSpeakerButtonAction(boolean isSpeaker) {
        switch (MEDIA_TYPE) {
            case SINCH:
                SinchHandler.getInstance().speakerBtnClicked(isSpeaker);
                break;
            case TWILIO:
                TwilioHandler.getInstance().speakerBtnClicked(isSpeaker);
                break;
            case LOCAL:
           //     LocalHandler.getInstance().speakerBtnClicked(isSpeaker);
                break;
        }
    }

    public void setSwitchCameraButtonAction() {
        switch (MEDIA_TYPE) {
            case SINCH:
                SinchHandler.getInstance().switchCameraBtnClicked();
                break;
            case TWILIO:
                TwilioHandler.getInstance().switchCameraBtnClicked();
                break;
            case LOCAL:
            //    LocalHandler.getInstance().switchCameraBtnClicked();
                break;
        }
    }

    public void setAnsButtonAction() {
        switch (MEDIA_TYPE) {
            case SINCH:
                SinchHandler.getInstance().answerClicked();
                break;
            case TWILIO:
                TwilioHandler.getInstance().getTwilioAccessToken(true);
                break;
            case LOCAL:
             //   LocalHandler.getInstance().doAnswer();
                break;
        }
    }

    public void onCallEnded(Context mContext, MediaDataProvider dataProvider) {
        sendNotification(mContext, dataProvider, "Yes");
        switch (MEDIA_TYPE) {
            case SINCH:
                SinchHandler.getInstance().callEnded();
                break;
            case TWILIO:
                TwilioHandler.getInstance().sendNotification(true);
                break;
            case LOCAL:
           //     LocalHandler.getInstance().hangUpCall();
                break;
        }
    }

    public void setEstablishedAfterUI(boolean isMute, boolean isSpeaker, boolean isSpeakerClick, boolean isFront) {
        switch (MEDIA_TYPE) {
            case SINCH:
                SinchHandler.getInstance().setEstablishedAfterUI();
                break;
            case TWILIO:
                TwilioHandler.getInstance().setEstablishedAfterUI();
                break;
            case LOCAL:
            //    LocalHandler.getInstance().setEstablishedAfterUI(isMute, isSpeaker, isSpeakerClick, isFront);
                break;
        }
    }
}