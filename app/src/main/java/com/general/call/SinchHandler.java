package com.general.call;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.general.files.GeneralFunctions;
import com.general.files.GetDeviceToken;
import com.general.files.MyApp;
import com.leadryde.userapp.R;
import com.sinch.android.rtc.ClientRegistration;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.SinchClientListener;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallListener;
import com.sinch.android.rtc.video.VideoController;
import com.sinch.android.rtc.video.VideoScalingType;
import com.utils.Logger;
import com.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SinchHandler {

    private static final String TAG = SinchHandler.class.getSimpleName();
    private static SinchHandler instance;
    private GeneralFunctions generalFunc;
    private Context mContext;
    private V3CallListener mListener;

    private final HashMap<String, String> mCallHashMap = new HashMap<>();
    private SinchClient mSinchClient;
    private final String mUserName, SINCH_APP_KEY, SINCH_APP_SECRET_KEY, SINCH_APP_ENVIRONMENT_HOST;
    private ViewGroup localVideo, remoteView;

    @Nullable
    private Call mCall = null;

    public static SinchHandler getInstance() {
        if (instance == null) {
            instance = new SinchHandler();
        }
        return instance;
    }

    public void setUIListener(MediaDataProvider dataProvider, V3CallScreen mActivity, V3CallListener V3CallListener) {
        this.mContext = mActivity;
        this.generalFunc = MyApp.getInstance().getGeneralFun(mContext);
        this.mListener = V3CallListener;
        mListener.onUIChanges();
        mListener.onCallProgressing();
        if (dataProvider.isVideoCall) {
            localVideo = mActivity.findViewById(R.id.localVideo);
            remoteView = mActivity.findViewById(R.id.remoteVideo);
            try {
                final VideoController vc = mSinchClient.getVideoController();
                if (vc != null) {
                    mActivity.runOnUiThread(() -> {
                        remoteView.addView(vc.getLocalView());
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public SinchHandler() {
        this.mContext = MyApp.getInstance().getCurrentAct();
        this.generalFunc = MyApp.getInstance().getGeneralFun(mContext);
        String mUserProfileJson = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);
        this.mUserName = Utils.userType + "_" + generalFunc.getMemberId();

        this.SINCH_APP_KEY = this.generalFunc.retrieveValue(Utils.SINCH_APP_KEY);
        this.SINCH_APP_SECRET_KEY = this.generalFunc.retrieveValue(Utils.SINCH_APP_SECRET_KEY);
        this.SINCH_APP_ENVIRONMENT_HOST = this.generalFunc.retrieveValue(Utils.SINCH_APP_ENVIRONMENT_HOST);

        mCallHashMap.put("Id", generalFunc.getMemberId());
        mCallHashMap.put("Name", generalFunc.getJsonValue("vName", mUserProfileJson));
        mCallHashMap.put("PImage", generalFunc.getJsonValue("vImgName", mUserProfileJson));
        mCallHashMap.put("type", Utils.userType);
    }

    public void removeInitiateService() {
        if (mSinchClient != null) {
            mSinchClient.setSupportManagedPush(false);
            mSinchClient.unregisterPushNotificationData();
            mSinchClient.setSupportPushNotifications(false);
            mSinchClient.unregisterManagedPush();
        }
    }

    public void initiateService() {
        if (mSinchClient == null) {
            if (SINCH_APP_KEY == null || SINCH_APP_KEY.equalsIgnoreCase("")) {
                return;
            }
            mSinchClient = Sinch.getSinchClientBuilder().context(mContext).userId(mUserName)
                    .applicationKey(this.SINCH_APP_KEY)
                    .applicationSecret(this.SINCH_APP_SECRET_KEY)
                    .environmentHost(this.SINCH_APP_ENVIRONMENT_HOST).build();

            mSinchClient.setSupportCalling(true);
            mSinchClient.setSupportActiveConnectionInBackground(true);
            mSinchClient.setSupportPushNotifications(true);
            mSinchClient.startListeningOnActiveConnection();

            mSinchClient.getCallClient().setRespectNativeCalls(false);
            mSinchClient.setSupportManagedPush(true);

            mSinchClient.addSinchClientListener(new SinchClientListener() {
                @Override
                public void onClientStarted(SinchClient sinchClient) {
                    Logger.d(TAG, "SinchClient started");
                    /*GetDeviceToken GenerateDeviceToken = new GetDeviceToken(generalFunc);
                    GenerateDeviceToken.setDataResponseListener(vDeviceToken -> {
                        if (!vDeviceToken.equals("")) {
                            try {
                                mSinchClient.registerPushNotificationData(vDeviceToken.getBytes());
                            } catch (Exception ignored) {

                            }
                        }
                    });
                    GenerateDeviceToken.execute();*/
                }

                @Override
                public void onClientStopped(SinchClient sinchClient) {
                    Logger.d(TAG, "onClient Stopped");
                }

                @Override
                public void onClientFailed(SinchClient sinchClient, SinchError sinchError) {
                    Logger.d(TAG, "SinchClient Failed");
                    mSinchClient.terminate();
                    mSinchClient = null;
                }

                @Override
                public void onRegistrationCredentialsRequired(SinchClient sinchClient, ClientRegistration clientRegistration) {
                    Logger.d(TAG, "onRegistration Credentials Required");
                }

                @Override
                public void onLogMessage(int i, String s, String s1) {
                    Logger.d(TAG, "SinchClient started");
                }
            });
            mSinchClient.getCallClient().addCallClientListener((callClient, call) -> {
                mCall = call;
                call.addCallListener(new SinchCallListener());
                CommunicationManager.getInstance().incomingCommunicate(mContext, generalFunc, call, null);
            });
            mSinchClient.start();
        }
    }

    public void relayRemotePushNotificationPayload(final Map payload) {
        mSinchClient.relayRemotePushNotificationPayload(payload);
    }

    public void executeAction(Context mContext, CommunicationManager.TYPE communication_type, MediaDataProvider dataProvider) {
        switch (communication_type) {
            case PHONE_CALL:
                mCall = mSinchClient.getCallClient().callPhoneNumber(dataProvider.phoneNumber, mCallHashMap);
                break;
            case CHAT:
                break;
            case VIDEO_CALL:
                mCallHashMap.put("isVideoCall", "Yes");
                mCall = mSinchClient.getCallClient().callUserVideo(dataProvider.toMemberType + "_" + dataProvider.callId, mCallHashMap);
                if (mCall != null) {
                    mCall.addCallListener(new SinchCallListener());
                    mSinchClient.getVideoController().setResizeBehaviour(VideoScalingType.ASPECT_FILL);
                    // TODO: 12-12-2021 / Viral | Local Calling Disable
                    //mSinchClient.getVideoController().setLocalVideoResizeBehaviour(VideoScalingType.ASPECT_FILL);
                }
                break;
            case VOIP_CALL:
                mCallHashMap.put("isVideoCall", "No");
                mCall = mSinchClient.getCallClient().callUser(dataProvider.toMemberType + "_" + dataProvider.callId, mCallHashMap);
                if (mCall != null) {
                    mCall.addCallListener(new SinchCallListener());
                    mSinchClient.getAudioController().disableSpeaker();
                }
                break;
        }
    }

    public void answerClicked() {
        if (mCall != null) {
            mCall.answer();
            mCall.addCallListener(new SinchCallListener());
            if (mCall.getDetails().isVideoOffered()) {
                mSinchClient.getVideoController().setResizeBehaviour(VideoScalingType.ASPECT_FILL);
                // TODO: 12-12-2021 / Viral | Local Calling Disable
                //mSinchClient.getVideoController().setLocalVideoResizeBehaviour(VideoScalingType.ASPECT_FILL);
            } else {
                mSinchClient.getAudioController().disableSpeaker();
            }
        }
    }

    public void muteBtnClicked(boolean isMute) {
        if (isMute) {
            mSinchClient.getAudioController().unmute();
        } else {
            mSinchClient.getAudioController().mute();
        }
        mListener.onMuteView(isMute);
    }

    public void speakerBtnClicked(boolean isSpeaker) {
        if (isSpeaker) {
            mSinchClient.getAudioController().enableSpeaker();
        } else {
            mSinchClient.getAudioController().disableSpeaker();
        }
        mListener.onSpeakerView(isSpeaker);
    }

    public void switchCameraBtnClicked() {
        mSinchClient.getVideoController().toggleCaptureDevicePosition();
        mListener.onCameraView(null, true);
    }

    public void callEnded() {
        if (CommunicationManager.MEDIA_TYPE == CommunicationManager.MEDIA.SINCH) {
            MyApp.getInstance().getCurrentAct().setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
        }
        mSinchClient.setPushNotificationDisplayName(generalFunc.retrieveLangLBl("", "LBL_CALL_ENDED"));
        if (mCall != null) {
            mCall.hangup();
        }
        removeVideoViews();
        if (mListener != null) {
            mListener.onCallEnded();
        }
    }

    public void setEstablishedAfterUI() {
        Activity mActivity = MyApp.getInstance().getCurrentAct();
        mActivity.setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);

        if (mCall != null && mCall.getDetails().isVideoOffered()) {
            try {
                final VideoController vc = mSinchClient.getVideoController();
                if (vc != null) {
                    mActivity.runOnUiThread(() -> {
                        remoteView.removeView(vc.getLocalView());

                        localVideo.addView(vc.getLocalView());

                        remoteView.addView(vc.getRemoteView());
                        remoteView.setOnClickListener((View v) -> {

                        });
                        //vc.setLocalVideoZOrder(!mToggleVideoViewPositions);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void removeVideoViews() {
        try {
            if (mSinchClient == null) {
                return; // early
            }
            final VideoController vc = mSinchClient.getVideoController();
            if (vc != null) {
                MyApp.getInstance().getCurrentAct().runOnUiThread(() -> {
                    remoteView.removeView(vc.getLocalView());
                    remoteView.removeView(vc.getRemoteView());
                    localVideo.removeView(vc.getLocalView());
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class SinchCallListener implements CallListener {

        @Override
        public void onCallEnded(Call call) {
            callEnded();
        }

        @Override
        public void onCallEstablished(Call call) {
            if (call.getDetails().isVideoOffered()) {
                speakerBtnClicked(true);
            }
            mListener.onCallEstablished();
        }

        @Override
        public void onCallProgressing(Call call) {
            mListener.onCallProgressing();
        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> pushPairs) {
        }
    }
}