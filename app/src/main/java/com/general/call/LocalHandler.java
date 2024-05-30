//package com.general.call;
//
//import android.annotation.SuppressLint;
//import android.content.Context;
//import android.os.AsyncTask;
//import android.os.Handler;
//import android.os.Looper;
//import android.util.DisplayMetrics;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.RelativeLayout;
//
//import com.general.files.GeneralFunctions;
//import com.general.files.MyApp;
//import com.leadryde.userapp.R;
//import com.neovisionaries.ws.client.WebSocketException;
//import com.neovisionaries.ws.client.WebSocketFrame;
//import com.utils.Logger;
//import com.utils.Utils;
//
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//import org.webrtc.sinch.EglBase;
//import org.webrtc.sinch.SurfaceViewRenderer;
//import org.webrtc.sinch.VideoCapturer;
//
//import java.security.cert.X509Certificate;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
//import javax.net.ssl.SSLContext;
//import javax.net.ssl.TrustManager;
//import javax.net.ssl.X509TrustManager;
//
//import io.github.sac.BasicListener;
//import io.github.sac.Emitter;
//import io.github.sac.Socket;
//import tvi.webrtc.AudioTrack;
//import tvi.webrtc.IceCandidate;
//import tvi.webrtc.MediaStream;
//import tvi.webrtc.PeerConnection;
//import tvi.webrtc.PeerConnectionFactory;
//import tvi.webrtc.VideoTrack;
//
//public class LocalHandler {
//
//    private static final String TAG = LocalHandler.class.getSimpleName();
//    @SuppressLint("StaticFieldLeak")
//    private static LocalHandler instance;
//    private GeneralFunctions generalFunc;
//    private Context mContext;
//    private final String myCallId = "*";
//    private V3CallScreen mV3CallScreen;
//    private V3CallListener mListener;
//    private MediaDataProvider dataProvider;
//
//    private JSONObject mCallHashMap, mArgs, mTheirArgs;
//
//    private boolean isVideoCall = false, isCalled = false;
//    private Socket mSocket;
//    private SurfaceViewRenderer localVideoView, remoteVideoView;
//    private PeerConnection localPeer;
//    private PeerConnectionFactory peerConnectionFactory;
//    private final List<PeerConnection.IceServer> peerIceServers = new ArrayList<>();
//    private final ArrayList<IceCandidate> iceCandidateList = new ArrayList<>();
//
//    private VideoCapturer videoCapturerAndroid;
//    private VideoTrack localVideoTrack;
//    private AudioTrack localAudioTrack;
//    private AppRTCAudioManager audioManager = null;
//
//    private MediaStream mMediaStream;
//    private String roomId = "", mUserName;
//    private Handler pingHandler = null;
//    private boolean isFrontCamera = true;
//
//    public static LocalHandler getInstance() {
//        if (instance == null) {
//            instance = new LocalHandler();
//        }
//        return instance;
//    }
//
//    public LocalHandler() {
//        this.mContext = MyApp.getInstance().getCurrentAct();
//        this.generalFunc = MyApp.getInstance().getGeneralFun(mContext);
//        this.mUserName = Utils.userType + "_" + generalFunc.getMemberId();
//        myPing();
//    }
//
//    public void setListener(V3CallScreen callScreen, V3CallListener V3CallListener) {
//        this.mContext = callScreen;
//        this.generalFunc = MyApp.getInstance().getGeneralFun(mContext);
//        this.mListener = V3CallListener;
//        this.mV3CallScreen = callScreen;
//    }
//
//
//    public void setUIListener(MediaDataProvider dataProvider, V3CallScreen v3CallScreen, V3CallListener V3CallListener) {
//        this.mContext = v3CallScreen;
//        this.generalFunc = MyApp.getInstance().getGeneralFun(mContext);
//        this.mV3CallScreen = v3CallScreen;
//        this.mListener = V3CallListener;
//        this.dataProvider = dataProvider;
//        mListener.onUIChanges();
//        mListener.onCallProgressing();
//        this.audioManager = AppRTCAudioManager.create(mContext);
//        initiate();
//    }
//
//    private void initiate() {
//        localVideoView = mV3CallScreen.findViewById(R.id.local_gl_surface_view);
//        remoteVideoView = mV3CallScreen.findViewById(R.id.remote_gl_surface_view);
//        EglBase rootEglBase = EglBase.create();
//        localVideoView.init(rootEglBase.getEglBaseContext(), null);
//        remoteVideoView.init(rootEglBase.getEglBaseContext(), null);
//        localVideoView.setZOrderMediaOverlay(true);
//
//        JSONArray webrtcArray = generalFunc.getJsonArray("WEBRTC_ICE_SERVER_LIST", generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON)));
//        if (webrtcArray != null) {
//
//            for (int i = 0; i < webrtcArray.length(); i++) {
//                JSONObject serverObj = generalFunc.getJsonObject(webrtcArray, i);
//                PeerConnection.IceServer peerIceServer = PeerConnection.IceServer.builder(generalFunc.getJsonValueStr("TURN_URL", serverObj)).setUsername(generalFunc.getJsonValueStr("USER_NAME", serverObj)).setPassword(generalFunc.getJsonValueStr("Password", serverObj)).createIceServer();
//                PeerConnection.IceServer peerIceServer1 = PeerConnection.IceServer.builder(generalFunc.getJsonValueStr("STUN_URL", serverObj)).createIceServer();
//
//                peerIceServers.add(peerIceServer);
//                peerIceServers.add(peerIceServer1);
//
//            }
//        }
//
//        PeerConnectionFactory.InitializationOptions initializationOptions = PeerConnectionFactory.InitializationOptions.builder(mV3CallScreen).createInitializationOptions();
//        PeerConnectionFactory.initialize(initializationOptions);
//
//        //Create a new PeerConnectionFactory instance - using Hardware encoder and decoder.
//        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
//        peerConnectionFactory = PeerConnectionFactory.builder().setOptions(options)
//                .setVideoEncoderFactory(new DefaultVideoEncoderFactory(rootEglBase.getEglBaseContext(), true, true))
//                .setVideoDecoderFactory(new DefaultVideoDecoderFactory(rootEglBase.getEglBaseContext())).createPeerConnectionFactory();
//
//        //Now create a VideoCapturer instance.
//        videoCapturerAndroid = createCameraCapturer(new Camera1Enumerator(false));
//
//        //Create a VideoSource instance
//        VideoSource videoSource = null;
//        if (videoCapturerAndroid != null) {
//            SurfaceTextureHelper surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", rootEglBase.getEglBaseContext());
//            videoSource = peerConnectionFactory.createVideoSource(videoCapturerAndroid.isScreencast());
//            videoCapturerAndroid.initialize(surfaceTextureHelper, mV3CallScreen, videoSource.getCapturerObserver());
//            videoCapturerAndroid.startCapture(1024, 720, 30);
//        }
//        localVideoTrack = peerConnectionFactory.createVideoTrack("100", videoSource);
//        localVideoTrack.setEnabled(dataProvider.isVideoCall);
//
//        //create an AudioSource instance
//        MediaConstraints audioConstraints = new MediaConstraints();
//        /*audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googEchoCancellation", "true"));
//        audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googAutoGainControl", "true"));
//        audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googHighpassFilter", "true"));
//        audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googNoiseSuppression", "true"));*/
//        AudioSource audioSource = peerConnectionFactory.createAudioSource(audioConstraints);
//
//        localAudioTrack = peerConnectionFactory.createAudioTrack("101", audioSource);
//
//        localVideoView.setVisibility(View.VISIBLE);
//        localVideoTrack.addSink(localVideoView);
//
//        localVideoView.setMirror(true);
//        remoteVideoView.setMirror(true);
//
//        if (!mV3CallScreen.isIncomingView) {
//            createPeerConnection();
//            doCall();
//        }
//    }
//
//    private VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
//        final String[] deviceNames = enumerator.getDeviceNames();
//        for (String deviceName : deviceNames) {
//            if (isFrontCamera) {
//                if (enumerator.isFrontFacing(deviceName)) {
//                    VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);
//                    if (videoCapturer != null) {
//                        return videoCapturer;
//                    }
//                }
//            } else {
//                if (enumerator.isBackFacing(deviceName)) {
//                    VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);
//                    if (videoCapturer != null) {
//                        return videoCapturer;
//                    }
//                }
//            }
//        }
//        for (String deviceName : deviceNames) {
//            if (!enumerator.isFrontFacing(deviceName)) {
//                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);
//                if (videoCapturer != null) {
//                    return videoCapturer;
//                }
//            }
//        }
//        return null;
//    }
//
//    public void initiateService(String mName, String mImage) {
//        /*SinchClient mSinchClient = Sinch.getSinchClientBuilder().context(mContext).userId(myCallId)
//                .applicationKey(generalFunc.retrieveValue(Utils.SINCH_APP_KEY))
//                .applicationSecret(generalFunc.retrieveValue(Utils.SINCH_APP_SECRET_KEY))
//                .environmentHost(generalFunc.retrieveValue(Utils.SINCH_APP_ENVIRONMENT_HOST)).build();
//        mSinchClient.start();*/
//        mCallHashMap = new JSONObject();
//        try {
//            mCallHashMap.put("Id", generalFunc.getMemberId());
//            mCallHashMap.put("Name", mName);
//            mCallHashMap.put("PImage", mImage);
//            mCallHashMap.put("type", Utils.userType);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        switch (CommunicationManager.COMM_TYPE) {
//            case VIDEO_CALL:
//                isVideoCall = true;
//                break;
//            case VOIP_CALL:
//                isVideoCall = false;
//                break;
//        }
//        initiateSocket();
//    }
//
//    private void initiateSocket() {
//        new AsyncTask<Object, Object, Object>() {
//            @Override
//            protected Object doInBackground(Object[] objects) {
//                try {
//                    SSLContext sslcontext = SSLContext.getInstance("TLS");
//                    sslcontext.init(null, new TrustManager[]{new X509TrustManager() {
//                        public X509Certificate[] getAcceptedIssuers() {
//                            return new X509Certificate[]{};
//                        }
//
//                        @SuppressLint("TrustAllX509TrustManager")
//                        public void checkClientTrusted(X509Certificate[] chain, String authType) {
//                        }
//
//                        @SuppressLint("TrustAllX509TrustManager")
//                        public void checkServerTrusted(X509Certificate[] chain, String authType) {
//                        }
//                    }}, null);
//
//                    String url = generalFunc.getJsonValueStr("WEBRTC_SOCKET_URL", generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON)));
//                    Logger.e(TAG, "SocketApp | " + url);
//                    mSocket = new Socket(url);
//                    mSocket.setAuthToken(Utils.app_type + "_" + MyApp.getInstance().getAppLevelGeneralFunc().getMemberId());
//                    mSocket.setListener(new BasicListener() {
//                        public void onConnected(Socket socket, Map<String, List<String>> headers) {
//                            Logger.d(TAG, "SocketApp | onConnected");
//                        }
//
//                        public void onDisconnected(Socket socket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) {
//                            Logger.d(TAG, "SocketApp | onDisconnected");
//                            isCalled = false;
//                            if (instance != null) {
//                                initiateSocket();
//                            }
//                            if (mListener != null) {
//                                mListener.onCallEnded();
//                            }
//                        }
//
//                        public void onConnectError(Socket socket, WebSocketException exception) {
//                            Logger.d(TAG, "SocketApp | onConnectError::" + exception);
//                            //initiateSocket();
//                        }
//
//                        public void onSetAuthToken(String token, Socket socket) {
//                            Logger.d(TAG, "SocketApp | onSetAuthToken::" + token);
//                        }
//
//                        public void onAuthentication(Socket socket, Boolean status) {
//                            if (status) {
//                                Logger.d(TAG, "SocketApp | socket is authenticated");
//                            } else {
//                                Logger.d(TAG, "SocketApp | Authentication is required (optional)");
//                            }
//                        }
//                    });
//                    setCallerId();
//                    mSocket.connect();
//                } catch (Exception e) {
//                    Logger.d(TAG, "SocketApp | Exception::" + e.getMessage());
//                }
//                return null;
//            }
//        }.execute("");
//    }
//
//    public void executeAction(Context mContext, CommunicationManager.TYPE communication_type, MediaDataProvider dataProvider) {
//        roomId = dataProvider.toMemberType + "_" + dataProvider.callId;
//
//        //setCallerId(callerId);
//        switch (communication_type) {
//            case VIDEO_CALL:
//                isVideoCall = true;
//                try {
//                    mCallHashMap.put("isVideoCall", "Yes");
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                break;
//            case VOIP_CALL:
//                isVideoCall = false;
//                try {
//                    mCallHashMap.put("isVideoCall", "No");
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                break;
//        }
//    }
//
//    private void setCallerId() {
//        if (mSocket != null) {
//            mSocket.removeAllCallbacks();
//            mSocket.on(myCallId, onMessage);
//        }
//        Logger.d(TAG, "SocketApp ::setCallerId::" + myCallId);
//    }
//
//    private void createPeerConnection() {
//        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(peerIceServers);
//        rtcConfig.tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED;
//        rtcConfig.bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE;
//        rtcConfig.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE;
//        rtcConfig.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY;
//        rtcConfig.keyType = PeerConnection.KeyType.ECDSA;
//
//        localPeer = peerConnectionFactory.createPeerConnection(rtcConfig, new PeerConnection.Observer() {
//
//            @Override
//            public void onIceCandidate(IceCandidate iceCandidate) {
//                Logger.d(TAG, "onIceCandidate ::" + iceCandidate.toString());
//                iceCandidateList.add(iceCandidate);
//                emitIceCandidate(iceCandidate);
//            }
//
//            @Override
//            public void onAddStream(MediaStream mediaStream) {
//                Logger.d(TAG, "onAddStream ::" + mediaStream.getId());
//                mV3CallScreen.runOnUiThread(() -> mListener.onCallEstablished());
//                mMediaStream = mediaStream;
//                final VideoTrack videoTrack = mediaStream.videoTracks.get(0);
//                final AudioTrack remoteAudioTrack = mediaStream.audioTracks.get(0);
//                remoteAudioTrack.setEnabled(true);
//                mV3CallScreen.runOnUiThread(() -> {
//                    try {
//                        if (mV3CallScreen.isIncomingView) {
//                            updateVideoViews(true);
//                        }
//
//                        audioManager.init();
//                        if (isVideoCall) {
//                            videoTrack.addSink(remoteVideoView);
//                            videoTrack.setEnabled(true);
//                            remoteVideoView.setVisibility(View.VISIBLE);
//                        } else {
//                            remoteVideoView.setVisibility(View.GONE);
//                            videoTrack.setEnabled(false);
//                        }
//                    } catch (Exception e) {
//                        Logger.d(TAG, "gotRemoteStream ::" + e.toString());
//                    }
//                });
//            }
//
//            @Override
//            public void onSignalingChange(PeerConnection.SignalingState signalingState) {
//
//            }
//
//            @Override
//            public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
//
//            }
//
//            @Override
//            public void onIceConnectionReceivingChange(boolean b) {
//
//            }
//
//            @Override
//            public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
//
//            }
//
//            @Override
//            public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
//
//            }
//
//            @Override
//            public void onRemoveStream(MediaStream mediaStream) {
//
//            }
//
//            @Override
//            public void onDataChannel(DataChannel dataChannel) {
//
//            }
//
//            @Override
//            public void onRenegotiationNeeded() {
//
//            }
//
//            @Override
//            public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {
//
//            }
//        });
//
//        startStreamingVideo();
//    }
//
//    private void startStreamingVideo() {
//        Logger.d(TAG, "SocketApp ::addStreamToLocalPeer");
//        MediaStream stream = peerConnectionFactory.createLocalMediaStream("102");
//        stream.addTrack(localAudioTrack);
//        stream.addTrack(localVideoTrack);
//        localPeer.addStream(stream);
//    }
//
//    private void updateVideoViews(final boolean remoteVisible) {
//        MyApp.getInstance().getCurrentAct().runOnUiThread(() -> {
//            if (localVideoView != null) {
//                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) localVideoView.getLayoutParams();
//                if (remoteVisible) {
//                    params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//                    params.height = dpToPx(150);
//                    params.width = dpToPx(120);
//                    params.topMargin = dpToPx(100);
//                    params.rightMargin = dpToPx(20);
//                } else {
//                    params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//                }
//                localVideoView.setLayoutParams(params);
//            }
//        });
//    }
//
//    public int dpToPx(int dp) {
//        DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
//        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
//    }
//
//    public void disconnect() {
//        if (mSocket != null) {
//            instance = null;
//            mSocket.disconnect();
//        }
//    }
//
//    public void hangUpCall() {
//        if (!isFrontCamera) {
//            switchCameraBtnClicked();
//        }
//        if (localPeer != null) {
//            localPeer.close();
//            localPeer = null;
//        }
//
//        String oppD = null;
//        try {
//            oppD = mV3CallScreen.isIncomingView ? (mTheirArgs.getString("type") + "_" + mTheirArgs.getString("Id")) : roomId;
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        mSocket.emit(myCallId, oppD + "@@" + roomId + "@@CallEnd" + "@@" + mCallHashMap.toString());
//        roomId = "";
//        isCalled = false;
//        iceCandidateList.clear();
//        updateVideoViews(false);
//        if (audioManager != null) {
//            audioManager.close();
//            audioManager = null;
//        }
//        if (mListener != null) {
//            mListener.onCallEnded();
//            mListener = null;
//        }
//    }
//
//    private void emitMessage(SessionDescription message) {
//        try {
//            JSONObject obj = new JSONObject();
//            obj.put("type", message.type.canonicalForm());
//            obj.put("description", message.description);
//            Logger.d(TAG, "emitIceCandidate " + obj.toString());
//
//            String oppD = mV3CallScreen.isIncomingView ? (mTheirArgs.getString("type") + "_" + mTheirArgs.getString("Id")) : roomId;
//            mSocket.emit(myCallId, oppD + "@@" + roomId + "@@" + obj.toString() + "@@" + mCallHashMap.toString());
//
//        } catch (JSONException e) {
//            Logger.e(TAG, "emitIceCandidate | JSONException" + e.getMessage());
//        }
//    }
//
//    public void emitIceCandidate(IceCandidate iceCandidate) {
//        try {
//            JSONObject object = new JSONObject();
//            object.put("type", "candidate");
//            object.put("sdpMLineIndex", iceCandidate.sdpMLineIndex);
//            object.put("sdpMid", iceCandidate.sdpMid);
//            object.put("sdp", iceCandidate.sdp);
//            String oppD = mV3CallScreen.isIncomingView ? (mTheirArgs.getString("type") + "_" + mTheirArgs.getString("Id")) : roomId;
//            mSocket.emit(myCallId, oppD + "@@" + roomId + "@@" + object.toString() + "@@" + mCallHashMap.toString());
//        } catch (Exception e) {
//            Logger.e(TAG, "emitIceCandidate | JSONException" + e.getMessage());
//        }
//    }
//
//    private void doCall() {
//        MediaConstraints sdpConstraints = new MediaConstraints();
//        sdpConstraints.optional.add(new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"));
//        sdpConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
//        sdpConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
//        localPeer.createOffer(new SimpleSdpObserver() {
//            @Override
//            public void onCreateSuccess(SessionDescription sessionDescription) {
//                Logger.d(TAG, "onCreateSuccess | SignallingClient emit ");
//                localPeer.setLocalDescription(new SimpleSdpObserver(), sessionDescription);
//                emitMessage(sessionDescription);
//            }
//        }, sdpConstraints);
//    }
//
//    public void doAnswer() {
//        if (mArgs != null) {
//            try {
//                createPeerConnection();
//                localPeer.setRemoteDescription(new SimpleSdpObserver(), new SessionDescription(OFFER, mArgs.getString("description")));
//                localPeer.createAnswer(new SimpleSdpObserver() {
//                    @Override
//                    public void onCreateSuccess(SessionDescription sessionDescription) {
//                        localPeer.setLocalDescription(new SimpleSdpObserver(), sessionDescription);
//                        emitMessage(sessionDescription);
//                        if (iceCandidateList.size() > 0) {
//                            for (int i = 0; i < iceCandidateList.size(); i++) {
//                                localPeer.addIceCandidate(iceCandidateList.get(i));
//                            }
//                        }
//                    }
//                }, new MediaConstraints());
//            } catch (JSONException e) {
//                Logger.e(TAG, "SocketApp | JSONException:: answer-> " + e.getMessage());
//            }
//        }
//    }
//
//    public void speakerBtnClicked(boolean isSpeaker) {
//        try {
//            if (isSpeaker) {
//                if (mMediaStream != null && mMediaStream.audioTracks != null) {
//                    mMediaStream.audioTracks.get(0).setEnabled(isSpeaker);
//                }
//            }
//            audioManager.setSpeakerphoneOn(isSpeaker);
//            mListener.onSpeakerView(isSpeaker);
//        } catch (Exception e) {
//
//        }
//    }
//
//    public void muteBtnClicked(boolean isMute) {
//        localAudioTrack.setEnabled(!isMute);
//        mListener.onMuteView(isMute);
//    }
//
//    public void switchCameraBtnClicked() {
//        CameraVideoCapturer cameraVideoCapturer = (CameraVideoCapturer) videoCapturerAndroid;
//
//        if (cameraVideoCapturer != null) {
//            cameraVideoCapturer.switchCamera(new CameraVideoCapturer.CameraSwitchHandler() {
//                @Override
//                public void onCameraSwitchDone(boolean b) {
//                    isFrontCamera = b;
//
//                    localVideoView.setMirror(b);
//                    String oppD = null;
//                    try {
//                        oppD = mV3CallScreen.isIncomingView ? (mTheirArgs.getString("type") + "_" + mTheirArgs.getString("Id")) : roomId;
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//
//                    mSocket.emit(myCallId, oppD + "@@" + roomId + "@@CameraSwitched" + "@@" + (b ? "Yes" : "No"));
//                }
//
//                @Override
//                public void onCameraSwitchError(String s) {
//
//                }
//            });
//        }
//        if (mListener != null) {
//            mListener.onCameraView(null, true);
//        }
//    }
//
//    private Emitter.Listener onMessage = (name, data) -> {
//        String[] split = data.toString().split("@@");
//        String firstSubString = split[1];
//        String secondSubString = split[2];
//        if (secondSubString.equalsIgnoreCase("ping")) {
//            return;
//        }
//        String ThirdSubString = "";
//        if (split.length > 3) {
//            ThirdSubString = split[3];
//        }
//        Logger.d(TAG, "SocketApp | onMessage::1st::" + firstSubString + "::2nd::" + secondSubString + "::3rd::" + ThirdSubString);
//
//        if (secondSubString.equalsIgnoreCase("CameraSwitched")) {
//            if (ThirdSubString.equalsIgnoreCase("Yes")) {
//                remoteVideoView.setMirror(true);
//            } else if (ThirdSubString.equalsIgnoreCase("No")) {
//                remoteVideoView.setMirror(false);
//            }
//            return;
//        }
//
//        JSONObject args = generalFunc.getJsonObject(secondSubString);
//        String type = generalFunc.getJsonValueStr("type", args);
//
//        if (type.equalsIgnoreCase("offer")) {
//            if (firstSubString.equalsIgnoreCase(mUserName)) {
//                JSONObject detailsArgs = generalFunc.getJsonObject(ThirdSubString);
//                if (isCalled) {
//                    mSocket.emit(myCallId, generalFunc.getJsonValueStr("type", detailsArgs) + "_" + generalFunc.getJsonValueStr("Id", detailsArgs) + "@@CallBusy");
//                    return;
//                }
//
//                if (mListener == null) {
//                    roomId = firstSubString;
//                    isCalled = true;
//                    this.mArgs = args;
//                    this.mTheirArgs = detailsArgs;
//                    try {
//                        isVideoCall = generalFunc.getJsonValue("isVideoCall", detailsArgs) != null && generalFunc.getJsonValue("isVideoCall", detailsArgs).equals("Yes");
//                        mCallHashMap.put("isVideoCall", generalFunc.getJsonValue("isVideoCall", detailsArgs));
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                    CommunicationManager.getInstance().incomingCommunicate(mContext, generalFunc, null, detailsArgs);
//                    return;
//                }
//            }
//        }
//        if (firstSubString.equalsIgnoreCase(mUserName)) {
//            if (secondSubString.equalsIgnoreCase("CallBusy")) {
//                isCalled = false;
//                if (mListener != null) {
//                    mListener.onCallEnded();
//                }
//                return;
//            }
//        }
//        if (!firstSubString.equalsIgnoreCase(roomId)) {
//            Logger.d(TAG, "SocketApp | IDNotMatch:: " + firstSubString + " :: roomId-> " + roomId);
//            return;
//        }
//        if (secondSubString.equalsIgnoreCase("CallEnd")) {
//            isCalled = false;
//            if (mListener != null) {
//                mListener.onCallEnded();
//            }
//            return;
//        }
//        if (type.equalsIgnoreCase("answer")) {
//            try {
//                localPeer.setRemoteDescription(new SimpleSdpObserver(), new SessionDescription(SessionDescription.Type.fromCanonicalForm(args.getString("type").toLowerCase()), args.getString("description")));
//                updateVideoViews(true);
//            } catch (JSONException e) {
//                Logger.e(TAG, "SocketApp | JSONException:: answer-> " + e.getMessage());
//            }
//        } else if (type.equalsIgnoreCase("candidate")) {
//            if (args != null) {
//                try {
//                    if (mV3CallScreen.isIncomingView) {
//                        iceCandidateList.add(new IceCandidate(args.getString("sdpMid"), args.getInt("sdpMLineIndex"), args.getString("sdp")));
//                    } else {
//                        localPeer.addIceCandidate(new IceCandidate(args.getString("sdpMid"), args.getInt("sdpMLineIndex"), args.getString("sdp")));
//                    }
//                } catch (JSONException e) {
//                    Logger.e(TAG, "SocketApp | JSONException:: candidate-> " + e.getMessage());
//                }
//            }
//        }
//    };
//
//    static class SimpleSdpObserver implements SdpObserver {
//
//        @Override
//        public void onCreateSuccess(SessionDescription sessionDescription) {
//        }
//
//        @Override
//        public void onSetSuccess() {
//        }
//
//        @Override
//        public void onCreateFailure(String s) {
//        }
//
//        @Override
//        public void onSetFailure(String s) {
//        }
//
//    }
//
//    private void myPing() {
//        if (pingHandler == null) {
//            pingHandler = new Handler(Looper.myLooper());
//            pingHandler.postDelayed(new Runnable() {
//
//                @Override
//                public void run() {
//                    if (instance != null) {
//                        if (mSocket != null && mSocket.isconnected()) {
//                            // mSocket.emit(toCallId, toCallId + "@@ping");
//                        } else {
//                            if (mSocket != null && !mSocket.isconnected()) {
//                                mSocket.connectAsync();
//                            }
//                        }
//                        pingHandler.postDelayed(this, 10000);
//                    }
//                }
//            }, 10000);
//        }
//    }
//
//    public void setEstablishedAfterUI(boolean isMute, boolean isSpeaker, boolean isSpeakerClick, boolean isFront) {
//        muteBtnClicked(isMute);
//        if (isSpeakerClick) {
//            speakerBtnClicked(isSpeaker);
//        } else {
//            speakerBtnClicked(isVideoCall ? true : isSpeaker);
//        }
//        mListener.onEstablishedAfterUI();
//    }
//}