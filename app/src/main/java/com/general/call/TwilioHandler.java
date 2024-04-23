package com.general.call;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;

import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.MyApp;
import com.leadryde.userapp.R;
import com.twilio.video.AudioCodec;
import com.twilio.video.ConnectOptions;
import com.twilio.video.EncodingParameters;
import com.twilio.video.G722Codec;
import com.twilio.video.H264Codec;
import com.twilio.video.IsacCodec;
import com.twilio.video.LocalAudioTrack;
import com.twilio.video.LocalParticipant;
import com.twilio.video.LocalVideoTrack;
import com.twilio.video.OpusCodec;
import com.twilio.video.PcmaCodec;
import com.twilio.video.PcmuCodec;
import com.twilio.video.RemoteAudioTrack;
import com.twilio.video.RemoteAudioTrackPublication;
import com.twilio.video.RemoteDataTrack;
import com.twilio.video.RemoteDataTrackPublication;
import com.twilio.video.RemoteParticipant;
import com.twilio.video.RemoteVideoTrack;
import com.twilio.video.RemoteVideoTrackPublication;
import com.twilio.video.Room;
import com.twilio.video.TwilioException;
import com.twilio.video.Video;
import com.twilio.video.VideoCodec;
import com.twilio.video.VideoTrack;
import com.twilio.video.VideoView;
import com.twilio.video.Vp8Codec;
import com.twilio.video.Vp9Codec;
import com.utils.Logger;
import com.utils.Utils;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;

import tvi.webrtc.VideoSink;

public class TwilioHandler {

    private static final String TAG = TwilioHandler.class.getSimpleName();
    private static TwilioHandler instance;
    private GeneralFunctions generalFunc;
    private Context mContext;
    private V3CallScreen mV3CallScreen;
    private V3CallListener mListener;
    private MediaDataProvider mDataProvider;

    private static final String LOCAL_AUDIO_TRACK_NAME = "mic", LOCAL_VIDEO_TRACK_NAME = "camera";

    private AudioCodec audioCodec;
    private VideoCodec videoCodec;
    private SharedPreferences preferences;

    private CameraCapturerCompat cameraCapturerCompat;
    private LocalAudioTrack localAudioTrack;
    private LocalVideoTrack localVideoTrack;
    private LocalParticipant localParticipant;
    private EncodingParameters encodingParameters;

    private VideoSink localVideoView;
    private boolean isVideoCall;

    private Room room;
    private ProgressBar reconnectingProgressBar;
    private VideoView primaryVideoView, thumbnailVideoView;
    private String remoteParticipantIdentity;
    private AppRTCAudioManager audioManager;

    public static TwilioHandler getInstance() {
        if (instance == null) {
            instance = new TwilioHandler();
        }
        return instance;
    }

    public TwilioHandler() {
        this.mContext = MyApp.getInstance().getCurrentAct();
        this.generalFunc = MyApp.getInstance().getGeneralFun(mContext);
        this.preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    public void setListener(V3CallScreen callScreen, V3CallListener V3CallListener) {
        this.mContext = callScreen;
        this.generalFunc = MyApp.getInstance().getGeneralFun(mContext);
        this.mListener = V3CallListener;
    }

    public void setUIListener(MediaDataProvider dataProvider, V3CallScreen v3CallScreen, V3CallListener V3CallListener) {
        this.mContext = v3CallScreen;
        this.generalFunc = MyApp.getInstance().getGeneralFun(mContext);
        this.preferences = PreferenceManager.getDefaultSharedPreferences(mContext);

        this.mDataProvider = dataProvider;
        this.mV3CallScreen = v3CallScreen;
        this.mListener = V3CallListener;

        isVideoCall = dataProvider.isVideoCall;
        this.audioManager = AppRTCAudioManager.create(mContext);

        reconnectingProgressBar = mV3CallScreen.findViewById(R.id.reconnecting_progress_bar);
        primaryVideoView = mV3CallScreen.findViewById(R.id.primary_video_view);
        thumbnailVideoView = mV3CallScreen.findViewById(R.id.thumbnail_video_view);
        thumbnailVideoView.setVisibility(View.GONE);

        createAudioAndVideoTracks();
        onResume();
        mListener.onUIChanges();
        mListener.onCallProgressing();
    }

    public void setDataProvider(MediaDataProvider mDataProvider) {
        this.mDataProvider = mDataProvider;
    }

    public void initiateService() {
        switch (CommunicationManager.COMM_TYPE) {
            case VIDEO_CALL:
                isVideoCall = true;
                break;
            case VOIP_CALL:
                isVideoCall = false;
                break;
        }
    }

    public void executeAction(Context mContext, CommunicationManager.TYPE communication_type, MediaDataProvider dataProvider) {
        switch (communication_type) {
            case VIDEO_CALL:
                isVideoCall = true;
                break;
            case VOIP_CALL:
                isVideoCall = false;
                break;
        }
        getTwilioAccessToken(false);
    }

    private void createAudioAndVideoTracks() {
        localAudioTrack = LocalAudioTrack.create(mContext, true, LOCAL_AUDIO_TRACK_NAME);
        if (isVideoCall) {
            cameraCapturerCompat = new CameraCapturerCompat(mContext, CameraCapturerCompat.Source.FRONT_CAMERA);
            localVideoTrack = LocalVideoTrack.create(mContext, true, cameraCapturerCompat, LOCAL_VIDEO_TRACK_NAME);
            primaryVideoView.setMirror(true);
            localVideoTrack.addSink(primaryVideoView);
            localVideoView = primaryVideoView;
        }
    }

    private void onResume() {
        audioCodec = getAudioCodecPreference();
        videoCodec = getVideoCodecPreference();
        final EncodingParameters newEncodingParameters = getEncodingParameters();
        if (isVideoCall && localVideoTrack == null) {
            localVideoTrack = LocalVideoTrack.create(mContext, true, cameraCapturerCompat, LOCAL_VIDEO_TRACK_NAME);
            if (localVideoTrack != null) {
                localVideoTrack.addSink(localVideoView);
                if (localParticipant != null) {
                    localParticipant.publishTrack(localVideoTrack);
                    if (!newEncodingParameters.equals(encodingParameters)) {
                        localParticipant.setEncodingParameters(newEncodingParameters);
                    }
                }
            }
        }
        encodingParameters = newEncodingParameters;
        if (room != null) {
            reconnectingProgressBar.setVisibility((room.getState() != Room.State.RECONNECTING) ? View.GONE : View.VISIBLE);
        }
    }

    public void getTwilioAccessToken(boolean isIncoming) {
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("type", "getTwilioAccessToken");
        parameters.put("UserType", Utils.userType);
        parameters.put("iMemberId", generalFunc.getMemberId());
        if (isIncoming) {
            parameters.put("roomName", mDataProvider.roomName);
        }

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(mContext, parameters);
        exeWebServer.setLoaderConfig(mContext, true, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {
            JSONObject responseStringObject = generalFunc.getJsonObject(responseString);

            if (GeneralFunctions.checkDataAvail(Utils.action_str, responseStringObject)) {
                String message = generalFunc.getJsonValueStr(Utils.message_str, responseStringObject);

                mDataProvider.roomName = generalFunc.getJsonValue("roomName", message);

                if (mDataProvider.roomName != null) {
                    connectToRoom(generalFunc.getJsonValue("token", message), mDataProvider.roomName);
                    sendNotification(false);
                }
            }
        });
        exeWebServer.execute();
    }

    private void connectToRoom(@NonNull String accessToken, @NonNull String roomName) {
        ConnectOptions.Builder connectOptionsBuilder = new ConnectOptions.Builder(accessToken).roomName(roomName);
        if (localAudioTrack != null) {
            connectOptionsBuilder.audioTracks(Collections.singletonList(localAudioTrack));
            connectOptionsBuilder.preferAudioCodecs(Collections.singletonList(audioCodec));
        }
        if (isVideoCall && localVideoTrack != null) {
            connectOptionsBuilder.videoTracks(Collections.singletonList(localVideoTrack));
            connectOptionsBuilder.preferVideoCodecs(Collections.singletonList(videoCodec));
        }
        connectOptionsBuilder.encodingParameters(encodingParameters);
        connectOptionsBuilder.enableAutomaticSubscription(preferences.getBoolean("enable_automatic_subscription", true));
        room = Video.connect(mContext, connectOptionsBuilder.build(), roomListener());
    }

    @SuppressLint("SetTextI18n")
    private Room.Listener roomListener() {
        return new Room.Listener() {
            @Override
            public void onConnected(@NotNull Room room) {
                localParticipant = room.getLocalParticipant();
                mV3CallScreen.setTitle(room.getName());
                for (RemoteParticipant remoteParticipant : room.getRemoteParticipants()) {
                    addRemoteParticipant(remoteParticipant);
                    break;
                }
            }

            @Override
            public void onReconnecting(@NonNull Room room, @NonNull TwilioException twilioException) {
                reconnectingProgressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onReconnected(@NonNull Room room) {
                reconnectingProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onConnectFailure(@NotNull Room room, @NotNull TwilioException e) {
                Logger.d(TAG, "::onConnectFailure" + e.toString());
            }

            @Override
            public void onDisconnected(@NotNull Room room, TwilioException e) {
                localParticipant = null;
                reconnectingProgressBar.setVisibility(View.GONE);
                TwilioHandler.this.room = null;
                moveLocalVideoToPrimaryView();
                disconnect();
            }

            @Override
            public void onParticipantConnected(@NotNull Room room, @NotNull RemoteParticipant remoteParticipant) {
                Logger.d(TAG, "onParticipantConnected::" + room.getName() + "::" + remoteParticipant.getIdentity());
                addRemoteParticipant(remoteParticipant);
            }

            @Override
            public void onParticipantDisconnected(@NotNull Room room, @NotNull RemoteParticipant remoteParticipant) {
                removeRemoteParticipant(remoteParticipant);
                disconnect();
            }

            @Override
            public void onRecordingStarted(@NotNull Room room) {
                Logger.d(TAG, "onRecordingStarted");
            }

            @Override
            public void onRecordingStopped(@NotNull Room room) {
                Logger.d(TAG, "onRecordingStopped");
            }
        };
    }

    public void disconnect() {
        removeVideoViews();
        if (mListener != null) {
            mListener.onCallEnded();
        }
    }

    private void addRemoteParticipant(RemoteParticipant remoteParticipant) {
        if (thumbnailVideoView.getVisibility() == View.VISIBLE) {
            generalFunc.showMessage(primaryVideoView, "Multiple participants are not currently support in this UI");
            return;
        }
        remoteParticipantIdentity = remoteParticipant.getIdentity();
        callEstablishedView(remoteParticipant);

        remoteParticipant.setListener(new RemoteParticipant.Listener() {
            @Override
            public void onAudioTrackPublished(@NotNull RemoteParticipant remoteParticipant, @NotNull RemoteAudioTrackPublication remoteAudioTrackPublication) {
                Logger.d(TAG, String.format("onAudioTrackPublished: " + "[RemoteParticipant: identity=%s], "
                                + "[RemoteAudioTrackPublication: sid=%s, enabled=%b, "
                                + "subscribed=%b, name=%s]", remoteParticipant.getIdentity(),
                        remoteAudioTrackPublication.getTrackSid(), remoteAudioTrackPublication.isTrackEnabled(),
                        remoteAudioTrackPublication.isTrackSubscribed(), remoteAudioTrackPublication.getTrackName()));
            }

            @Override
            public void onAudioTrackUnpublished(@NotNull RemoteParticipant remoteParticipant, @NotNull RemoteAudioTrackPublication remoteAudioTrackPublication) {
                Logger.d(TAG, String.format("onAudioTrackUnpublished: " + "[RemoteParticipant: identity=%s], "
                                + "[RemoteAudioTrackPublication: sid=%s, enabled=%b, "
                                + "subscribed=%b, name=%s]", remoteParticipant.getIdentity(),
                        remoteAudioTrackPublication.getTrackSid(), remoteAudioTrackPublication.isTrackEnabled(),
                        remoteAudioTrackPublication.isTrackSubscribed(), remoteAudioTrackPublication.getTrackName()));
            }

            @Override
            public void onDataTrackPublished(@NotNull RemoteParticipant remoteParticipant, @NotNull RemoteDataTrackPublication remoteDataTrackPublication) {
                Logger.d(TAG, String.format("onDataTrackPublished: " + "[RemoteParticipant: identity=%s], "
                                + "[RemoteDataTrackPublication: sid=%s, enabled=%b, "
                                + "subscribed=%b, name=%s]", remoteParticipant.getIdentity(),
                        remoteDataTrackPublication.getTrackSid(), remoteDataTrackPublication.isTrackEnabled(),
                        remoteDataTrackPublication.isTrackSubscribed(), remoteDataTrackPublication.getTrackName()));
            }

            @Override
            public void onDataTrackUnpublished(@NotNull RemoteParticipant remoteParticipant, @NotNull RemoteDataTrackPublication remoteDataTrackPublication) {
                Logger.d(TAG, String.format("onDataTrackUnpublished: " + "[RemoteParticipant: identity=%s], "
                                + "[RemoteDataTrackPublication: sid=%s, enabled=%b, "
                                + "subscribed=%b, name=%s]", remoteParticipant.getIdentity(),
                        remoteDataTrackPublication.getTrackSid(), remoteDataTrackPublication.isTrackEnabled(),
                        remoteDataTrackPublication.isTrackSubscribed(), remoteDataTrackPublication.getTrackName()));
            }

            @Override
            public void onVideoTrackPublished(@NotNull RemoteParticipant remoteParticipant, @NotNull RemoteVideoTrackPublication remoteVideoTrackPublication) {
                Logger.d(TAG, String.format("onVideoTrackPublished: " + "[RemoteParticipant: identity=%s], "
                                + "[RemoteVideoTrackPublication: sid=%s, enabled=%b, "
                                + "subscribed=%b, name=%s]", remoteParticipant.getIdentity(),
                        remoteVideoTrackPublication.getTrackSid(), remoteVideoTrackPublication.isTrackEnabled(),
                        remoteVideoTrackPublication.isTrackSubscribed(), remoteVideoTrackPublication.getTrackName()));
            }

            @Override
            public void onVideoTrackUnpublished(@NotNull RemoteParticipant remoteParticipant, @NotNull RemoteVideoTrackPublication remoteVideoTrackPublication) {
                Logger.d(TAG, String.format("onVideoTrackUnpublished: " + "[RemoteParticipant: identity=%s], "
                                + "[RemoteVideoTrackPublication: sid=%s, enabled=%b, "
                                + "subscribed=%b, name=%s]", remoteParticipant.getIdentity(),
                        remoteVideoTrackPublication.getTrackSid(), remoteVideoTrackPublication.isTrackEnabled(),
                        remoteVideoTrackPublication.isTrackSubscribed(), remoteVideoTrackPublication.getTrackName()));
            }

            @Override
            public void onAudioTrackSubscribed(@NotNull RemoteParticipant remoteParticipant, @NotNull RemoteAudioTrackPublication remoteAudioTrackPublication, @NotNull RemoteAudioTrack remoteAudioTrack) {
                Logger.d(TAG, String.format("onAudioTrackSubscribed: " + "[RemoteParticipant: identity=%s], "
                                + "[RemoteAudioTrack: enabled=%b, playbackEnabled=%b, name=%s]",
                        remoteParticipant.getIdentity(), remoteAudioTrack.isEnabled(),
                        remoteAudioTrack.isPlaybackEnabled(), remoteAudioTrack.getName()));
            }

            @Override
            public void onAudioTrackUnsubscribed(@NotNull RemoteParticipant remoteParticipant, @NotNull RemoteAudioTrackPublication remoteAudioTrackPublication, @NotNull RemoteAudioTrack remoteAudioTrack) {
                Logger.d(TAG, String.format("onAudioTrackUnsubscribed: " + "[RemoteParticipant: identity=%s], "
                                + "[RemoteAudioTrack: enabled=%b, playbackEnabled=%b, name=%s]",
                        remoteParticipant.getIdentity(), remoteAudioTrack.isEnabled(),
                        remoteAudioTrack.isPlaybackEnabled(), remoteAudioTrack.getName()));
            }

            @SuppressLint("DefaultLocale")
            @Override
            public void onAudioTrackSubscriptionFailed(@NotNull RemoteParticipant remoteParticipant, @NotNull RemoteAudioTrackPublication remoteAudioTrackPublication, @NotNull TwilioException twilioException) {
                Logger.d(TAG, String.format("onAudioTrackSubscriptionFailed: " + "[RemoteParticipant: identity=%s], "
                                + "[RemoteAudioTrackPublication: sid=%b, name=%s]"
                                + "[TwilioException: code=%d, message=%s]", remoteParticipant.getIdentity(),
                        remoteAudioTrackPublication.getTrackSid(), remoteAudioTrackPublication.getTrackName(),
                        twilioException.getCode(), twilioException.getMessage()));
            }

            @Override
            public void onDataTrackSubscribed(@NotNull RemoteParticipant remoteParticipant, @NotNull RemoteDataTrackPublication remoteDataTrackPublication, @NotNull RemoteDataTrack remoteDataTrack) {
                Logger.d(TAG, String.format("onDataTrackSubscribed: " + "[RemoteParticipant: identity=%s], "
                                + "[RemoteDataTrack: enabled=%b, name=%s]", remoteParticipant.getIdentity(),
                        remoteDataTrack.isEnabled(), remoteDataTrack.getName()));
            }

            @Override
            public void onDataTrackUnsubscribed(@NotNull RemoteParticipant remoteParticipant, @NotNull RemoteDataTrackPublication remoteDataTrackPublication, @NotNull RemoteDataTrack remoteDataTrack) {
                Logger.d(TAG, String.format("onDataTrackUnsubscribed: " + "[RemoteParticipant: identity=%s], "
                                + "[RemoteDataTrack: enabled=%b, name=%s]", remoteParticipant.getIdentity(),
                        remoteDataTrack.isEnabled(), remoteDataTrack.getName()));
            }

            @SuppressLint("DefaultLocale")
            @Override
            public void onDataTrackSubscriptionFailed(@NotNull RemoteParticipant remoteParticipant, @NotNull RemoteDataTrackPublication remoteDataTrackPublication, @NotNull TwilioException twilioException) {
                Logger.d(TAG, String.format("onDataTrackSubscriptionFailed: " + "[RemoteParticipant: identity=%s], "
                                + "[RemoteDataTrackPublication: sid=%b, name=%s]"
                                + "[TwilioException: code=%d, message=%s]", remoteParticipant.getIdentity(),
                        remoteDataTrackPublication.getTrackSid(), remoteDataTrackPublication.getTrackName(),
                        twilioException.getCode(), twilioException.getMessage()));
            }

            @Override
            public void onVideoTrackSubscribed(@NotNull RemoteParticipant remoteParticipant, @NotNull RemoteVideoTrackPublication remoteVideoTrackPublication, @NotNull RemoteVideoTrack remoteVideoTrack) {
                Logger.d(TAG, String.format("onVideoTrackSubscribed: "
                                + "[RemoteParticipant: identity=%s], " + "[RemoteVideoTrack: enabled=%b, name=%s]",
                        remoteParticipant.getIdentity(), remoteVideoTrack.isEnabled(), remoteVideoTrack.getName()));
                callEstablishedView(remoteParticipant);
            }

            @Override
            public void onVideoTrackUnsubscribed(@NotNull RemoteParticipant remoteParticipant, @NotNull RemoteVideoTrackPublication remoteVideoTrackPublication, @NotNull RemoteVideoTrack remoteVideoTrack) {
                Logger.d(TAG, String.format("onVideoTrackUnsubscribed: "
                                + "[RemoteParticipant: identity=%s], " + "[RemoteVideoTrack: enabled=%b, name=%s]",
                        remoteParticipant.getIdentity(), remoteVideoTrack.isEnabled(), remoteVideoTrack.getName()));
                removeParticipantVideo(remoteVideoTrack);
            }

            @SuppressLint("DefaultLocale")
            @Override
            public void onVideoTrackSubscriptionFailed(@NotNull RemoteParticipant remoteParticipant, @NotNull RemoteVideoTrackPublication remoteVideoTrackPublication, @NotNull TwilioException twilioException) {
                Logger.d(TAG, String.format("onVideoTrackSubscriptionFailed: "
                                + "[RemoteParticipant: identity=%s], " + "[RemoteVideoTrackPublication: sid=%b, name=%s]"
                                + "[TwilioException: code=%d, message=%s]",
                        remoteParticipant.getIdentity(), remoteVideoTrackPublication.getTrackSid(),
                        remoteVideoTrackPublication.getTrackName(), twilioException.getCode(), twilioException.getMessage()));
                generalFunc.showMessage(primaryVideoView, String.format("Failed to subscribe to %s video track", remoteParticipant.getIdentity()));
            }

            @Override
            public void onAudioTrackEnabled(@NotNull RemoteParticipant remoteParticipant, @NotNull RemoteAudioTrackPublication remoteAudioTrackPublication) {
            }

            @Override
            public void onAudioTrackDisabled(@NotNull RemoteParticipant remoteParticipant, @NotNull RemoteAudioTrackPublication remoteAudioTrackPublication) {
            }

            @Override
            public void onVideoTrackEnabled(@NotNull RemoteParticipant remoteParticipant, @NotNull RemoteVideoTrackPublication remoteVideoTrackPublication) {
            }

            @Override
            public void onVideoTrackDisabled(@NotNull RemoteParticipant remoteParticipant, @NotNull RemoteVideoTrackPublication remoteVideoTrackPublication) {
            }
        });
    }

    private void removeParticipantVideo(VideoTrack videoTrack) {
        videoTrack.removeSink(primaryVideoView);
    }

    private void moveLocalVideoToThumbnailView() {
        try {
            if (thumbnailVideoView.getVisibility() == View.GONE) {
                thumbnailVideoView.setVisibility(View.VISIBLE);
                localVideoTrack.removeSink(primaryVideoView);
                localVideoTrack.addSink(thumbnailVideoView);
                localVideoView = thumbnailVideoView;
                thumbnailVideoView.setMirror(cameraCapturerCompat.getCameraSource() == CameraCapturerCompat.Source.FRONT_CAMERA);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("SetTextI18n")
    private void removeRemoteParticipant(RemoteParticipant remoteParticipant) {
        if (!remoteParticipant.getIdentity().equals(remoteParticipantIdentity)) {
            return;
        }
        if (!remoteParticipant.getRemoteVideoTracks().isEmpty()) {
            RemoteVideoTrackPublication remoteVideoTrackPublication = remoteParticipant.getRemoteVideoTracks().get(0);
            if (remoteVideoTrackPublication.isTrackSubscribed()) {
                removeParticipantVideo(Objects.requireNonNull(remoteVideoTrackPublication.getRemoteVideoTrack()));
            }
        }
        moveLocalVideoToPrimaryView();
    }

    private void moveLocalVideoToPrimaryView() {
        if (thumbnailVideoView.getVisibility() == View.VISIBLE) {
            thumbnailVideoView.setVisibility(View.GONE);
            if (localVideoTrack != null) {
                localVideoTrack.removeSink(thumbnailVideoView);
                localVideoTrack.addSink(primaryVideoView);
            }
            if (isVideoCall) {
                if (thumbnailVideoView.getVisibility() == View.VISIBLE) {
                    thumbnailVideoView.setVisibility(View.GONE);
                    if (localVideoTrack != null) {
                        localVideoTrack.removeSink(thumbnailVideoView);
                        localVideoTrack.addSink(primaryVideoView);
                    }
                    localVideoView = primaryVideoView;
                    primaryVideoView.setMirror(cameraCapturerCompat.getCameraSource() == CameraCapturerCompat.Source.FRONT_CAMERA);
                }
            }
        }
    }

    public void sendNotification(boolean isDecline) {
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("type", "sendNotificationForTwilio");

        parameters.put("fromMemberType", Utils.userType);
        parameters.put("fromMemberId", generalFunc.getMemberId());

        if (isDecline) {
            if (mDataProvider.fromMemberType != null) {
                parameters.put("toMemberType", mDataProvider.fromMemberType);
                parameters.put("toMemberId", mDataProvider.fromMemberId);
            } else {
                parameters.put("toMemberType", mDataProvider.toMemberType);
                parameters.put("toMemberId", mDataProvider.callId);
            }
            parameters.put("isDecline", "Yes");
        } else {
            parameters.put("toMemberType", mDataProvider.toMemberType);
            parameters.put("toMemberId", mDataProvider.callId);

            if (mDataProvider.isVideoCall) {
                parameters.put("isVideoCall", "Yes");
            } else {
                parameters.put("isVideoCall", "No");
            }
            parameters.put("roomName", mDataProvider.roomName);
        }

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(mContext, parameters);
        exeWebServer.setLoaderConfig(mContext, true, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {
            if (isDecline) {
                disconnect();
            }
        });
        exeWebServer.execute();
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    private EncodingParameters getEncodingParameters() {
        final int maxAudioBitrate = Integer.parseInt(preferences.getString("ender_max_audio_bitrate", "0"));
        final int maxVideoBitrate = Integer.parseInt(preferences.getString("sender_max_video_bitrate", "0"));
        return new EncodingParameters(maxAudioBitrate, maxVideoBitrate);
    }

    private AudioCodec getAudioCodecPreference() {
        final String audioCodecName = preferences.getString("audio_codec", OpusCodec.NAME);
        switch (audioCodecName) {
            case IsacCodec.NAME:
                return new IsacCodec();
            case PcmaCodec.NAME:
                return new PcmaCodec();
            case PcmuCodec.NAME:
                return new PcmuCodec();
            case G722Codec.NAME:
                return new G722Codec();
            default:
                return new OpusCodec();
        }
    }

    private VideoCodec getVideoCodecPreference() {
        final String videoCodecName = preferences.getString("video_codec", Vp8Codec.NAME);
        switch (videoCodecName) {
            case Vp8Codec.NAME:
                boolean simulcast = preferences.getBoolean("vp8_simulcast", false);
                return new Vp8Codec(simulcast);
            case H264Codec.NAME:
                return new H264Codec();
            case Vp9Codec.NAME:
                return new Vp9Codec();
            default:
                return new Vp8Codec();
        }
    }

    private void callEstablishedView(RemoteParticipant remoteParticipant) {
        audioManager.init();
        if (remoteParticipant.getRemoteVideoTracks().size() > 0) {
            RemoteVideoTrackPublication remoteVideoTrackPublication = remoteParticipant.getRemoteVideoTracks().get(0);
            if (remoteVideoTrackPublication.isTrackSubscribed()) {

                moveLocalVideoToThumbnailView();
                primaryVideoView.setMirror(false);
                Objects.requireNonNull(remoteVideoTrackPublication.getRemoteVideoTrack()).addSink(primaryVideoView);
                speakerBtnClicked(true);

            }
        } else if (remoteParticipant.getRemoteAudioTracks().size() > 0) {
            if (isVideoCall && localVideoTrack != null) {
                localVideoTrack.enable(false);
            }
            speakerBtnClicked(false);
        }
        mListener.onCallEstablished();
    }

    public void switchCameraBtnClicked() {
        if (cameraCapturerCompat != null) {
            CameraCapturerCompat.Source cameraSource = cameraCapturerCompat.getCameraSource();
            cameraCapturerCompat.switchCamera();
            if (thumbnailVideoView.getVisibility() == View.VISIBLE) {
                thumbnailVideoView.setMirror(cameraSource == CameraCapturerCompat.Source.BACK_CAMERA);
            } else {
                primaryVideoView.setMirror(cameraSource == CameraCapturerCompat.Source.BACK_CAMERA);
            }
            mListener.onCameraView("", cameraSource != CameraCapturerCompat.Source.BACK_CAMERA);
        }
    }

    public void speakerBtnClicked(boolean isSpeaker) {
        audioManager.setSpeakerphoneOn(isSpeaker);
        mListener.onSpeakerView(isSpeaker);
    }

    public void muteBtnClicked() {
        if (localAudioTrack != null) {
            boolean enable = !localAudioTrack.isEnabled();
            localAudioTrack.enable(enable);
            mListener.onMuteView(enable);
        }
    }

    public void removeVideoViews() {
        try {
            if (room != null && room.getState() != Room.State.DISCONNECTED) {
                room.disconnect();
            }
            if (localAudioTrack != null) {
                localAudioTrack.release();
                localAudioTrack = null;
            }
            if (localVideoTrack != null) {
                localVideoTrack.release();
                localVideoTrack = null;
            }
            if (audioManager != null) {
                audioManager.close();
                audioManager = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setEstablishedAfterUI() {
        mListener.onEstablishedAfterUI();
    }
}