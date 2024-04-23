package com.general.call;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

import com.general.files.AudioPlayer;
import com.general.files.GeneralFunctions;
import com.general.files.MyApp;
import com.leadryde.userapp.BaseActivity;
import com.leadryde.userapp.R;
import com.squareup.picasso.Picasso;
import com.utils.CommonUtilities;
import com.utils.Utils;
import com.view.CreateRoundedView;
import com.view.GenerateAlertBox;
import com.view.MButton;
import com.view.MTextView;
import com.view.SelectableRoundedImageView;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class V3CallScreen extends BaseActivity implements V3CallListener {

    private AudioPlayer mAudioPlayer;
    private SelectableRoundedImageView ivAvatar;
    private MTextView txtCallState;
    private MTextView txtCallTime;
    private ImageView imgBtnMute, imgBtnSpeaker, imgBtnSwitchCamera;
    private MButton btnAnswer, btnEndCall;
    private long mCallStart = 0;
    private final long mIncomingCallStart = System.currentTimeMillis();
    private boolean isMute = false, isSpeaker = false, isFront = true;
    boolean isCallStart = false, isIncomingView;
    private View viewData;
    private MediaDataProvider dataProvider;
    private Timer mTimer;
    private UpdateCallDurationTask mDurationTask;
    private int btnRadius, backColor, strokeColor, filterColor;
    private RelativeLayout rlTwilioView, rlLocalView;
    private GenerateAlertBox currentAlertBox;
    private boolean isOpenAllowAllDialog = false, isListener = false;
    ArrayList<String> requestPermissions = new ArrayList<>();
    //GeneralFunctions generalFunc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.v3_call_screen);

        //generalFunc = MyApp.getInstance().getGeneralFun(this);
        isIncomingView = getIntent().getBooleanExtra(CommunicationManager.IS_INCOMING_VIEW, false);

        dataProvider = (MediaDataProvider) getIntent().getSerializableExtra(CommunicationManager.MY_DATA);

        inti();
        if (isIncomingView) {
            intiIncomingView();
        } else {
            intiCallingView();
        }

        CommunicationManager.getInstance().setListener(V3CallScreen.this, this);

        if (dataProvider.isVideoCall) {
            requestPermissions.add(android.Manifest.permission.CAMERA);
        }
        if (CommunicationManager.MEDIA_TYPE == CommunicationManager.MEDIA.TWILIO) {
            requestPermissions.add(android.Manifest.permission.READ_PHONE_STATE);
        }
        requestPermissions.add(android.Manifest.permission.RECORD_AUDIO);
        checkPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (requestPermissions.size() > 0) {

            //if (generalFunc == null) generalFunc = MyApp.getInstance().getGeneralFun(this);

            if (generalFunc.isAllPermissionGranted(false, requestPermissions)) {
                if (!isListener) {
                    CommunicationManager.getInstance().setUIListener(dataProvider, V3CallScreen.this, this);
                    isListener = true;
                }
            }
        }
    }

    public void checkPermission() {
        generalFunc.isAllPermissionGranted(!isOpenAllowAllDialog, requestPermissions);
        if (isOpenAllowAllDialog) {
            showNoPermission();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (generalFunc.isAllPermissionGranted(false, requestPermissions)) {
            if (!isListener) {
                CommunicationManager.getInstance().setUIListener(dataProvider, V3CallScreen.this, this);
                isListener = true;
            }
        } else {
            isOpenAllowAllDialog = true;
        }
    }

    public void showNoPermission() {
        currentAlertBox = generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("Application requires some permission to be granted to work. Please allow it.", "LBL_ALLOW_PERMISSIONS_APP"),
                generalFunc.retrieveLangLBl("Cancel", "LBL_CANCEL_TXT"), generalFunc.retrieveLangLBl("Allow All", "LBL_ALLOW_ALL_TXT"),
                buttonId -> {
                    if (buttonId == 0) {
                        currentAlertBox.closeAlertBox();
                        finish();
                    } else {
                        generalFunc.openSettings();
                    }
                });
    }

    @Override
    public void onStop() {
        super.onStop();
        btnEndCall.performClick();
        mDurationTask.cancel();
        mTimer.cancel();
    }

    @Override
    public void onStart() {
        super.onStart();
        mTimer = new Timer();
        mDurationTask = new UpdateCallDurationTask();
        mTimer.schedule(mDurationTask, 0, 500);
    }

    private void inti() {
        mAudioPlayer = new AudioPlayer(this);

        rlTwilioView = findViewById(R.id.rlTwilioView);
        rlLocalView = findViewById(R.id.rlLocalView);

        String toMemberImage = dataProvider.toMemberType;
        String toMemberId = dataProvider.callId;
        ivAvatar = findViewById(R.id.ivAvatar);
        if (isIncomingView) {
            if (dataProvider.fromMemberType != null && dataProvider.fromMemberId != null) {
                toMemberImage = dataProvider.fromMemberType;
                toMemberId = dataProvider.fromMemberId;
            }
        }
        if (Utils.checkText(dataProvider.toMemberImage)) {
            if (toMemberImage.equalsIgnoreCase(Utils.CALLTODRIVER)) {
                toMemberImage = CommonUtilities.PROVIDER_PHOTO_PATH + toMemberId + "/" + dataProvider.toMemberImage;
            } else if (toMemberImage.equalsIgnoreCase(Utils.CALLTOSTORE)) {
                toMemberImage = CommonUtilities.STORE_PHOTO_PATH + toMemberId + "/" + dataProvider.toMemberImage;
            }
        }
        ivAvatar.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.mipmap.ic_no_pic_user, null));
        if (Utils.checkText(toMemberImage)) {
            Picasso.get().load(toMemberImage).error(R.mipmap.ic_no_pic_user).into(ivAvatar);
        }

        MTextView txtToMemberName = findViewById(R.id.txtToMemberName);
        txtToMemberName.setText(dataProvider.toMemberName);
        txtCallState = findViewById(R.id.txtCallState);
        txtCallTime = findViewById(R.id.txtCallTime);

        imgBtnMute = findViewById(R.id.imgBtnMute);
        imgBtnMute.setOnClickListener(new setOnClickList());
        imgBtnSpeaker = findViewById(R.id.imgBtnSpeaker);
        imgBtnSpeaker.setOnClickListener(new setOnClickList());
        imgBtnSwitchCamera = findViewById(R.id.imgBtnSwitchCamera);
        imgBtnSwitchCamera.setOnClickListener(new setOnClickList());

        btnAnswer = findViewById(R.id.btnAnswer);
        btnAnswer.setText(generalFunc.retrieveLangLBl("", "LBL_ANSWER"));
        btnAnswer.setOnClickListener(new setOnClickList());

        viewData = findViewById(R.id.viewData);

        btnEndCall = findViewById(R.id.btnEndCall);
        btnEndCall.setText(generalFunc.retrieveLangLBl("", "LBL_END_CALL"));
        btnEndCall.setOnClickListener(new setOnClickList());

        btnRadius = Utils.dipToPixels(this, 35);
        backColor = getResources().getColor(android.R.color.transparent);
        strokeColor = getResources().getColor(R.color.white);
        filterColor = getResources().getColor(R.color.black);

        new CreateRoundedView(backColor, btnRadius, 2, strokeColor, imgBtnMute);
        new CreateRoundedView(backColor, btnRadius, 2, strokeColor, imgBtnSpeaker);
        new CreateRoundedView(strokeColor, btnRadius, 2, strokeColor, imgBtnSwitchCamera);
        imgBtnSwitchCamera.setColorFilter(filterColor);
    }

    private void intiIncomingView() {
        txtCallState.setVisibility(View.VISIBLE);
        txtCallTime.setVisibility(View.GONE);
        imgBtnMute.setVisibility(View.GONE);
        imgBtnSpeaker.setVisibility(View.GONE);
        imgBtnSwitchCamera.setVisibility(View.GONE);
        btnAnswer.setVisibility(View.VISIBLE);
        viewData.setVisibility(View.VISIBLE);
    }

    private void intiCallingView() {
        if (isCallStart) {
            txtCallState.setVisibility(View.GONE);
            txtCallTime.setVisibility(View.VISIBLE);
        } else {
            txtCallState.setVisibility(View.VISIBLE);
            txtCallTime.setVisibility(View.GONE);
        }
        imgBtnMute.setVisibility(View.VISIBLE);
        imgBtnSpeaker.setVisibility(View.VISIBLE);
        if (dataProvider.isVideoCall) {
            ivAvatar.setVisibility(View.GONE);
            imgBtnSwitchCamera.setVisibility(View.VISIBLE);
        } else {
            imgBtnSwitchCamera.setVisibility(View.GONE);
        }
        btnAnswer.setVisibility(View.GONE);
        viewData.setVisibility(View.GONE);
    }

    private void stopRinging() {
        if (mAudioPlayer != null) {
            if (isIncomingView) {
                mAudioPlayer.stopRingtone();
            } else {
                mAudioPlayer.stopProgressTone();
            }
        }
    }

    private class setOnClickList implements View.OnClickListener {

        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.imgBtnMute:
                    CommunicationManager.getInstance().setMuteButtonAction(isMute);
                    break;

                case R.id.imgBtnSpeaker:
                    CommunicationManager.getInstance().setSpeakerButtonAction(isSpeaker);
                    break;

                case R.id.imgBtnSwitchCamera:
                    CommunicationManager.getInstance().setSwitchCameraButtonAction();
                    break;

                case R.id.btnAnswer:
                    checkPermission();
                    if (isListener) {
                        stopRinging();
                        CommunicationManager.getInstance().setAnsButtonAction();
                    }
                    break;

                case R.id.btnEndCall:
                    CommunicationManager.getInstance().onCallEnded(V3CallScreen.this, dataProvider);
                    break;
            }
        }
    }

    @Override
    public void onMuteView(boolean mIsMute) {
        if (mIsMute) {
            new CreateRoundedView(backColor, btnRadius, 2, strokeColor, imgBtnMute);
            imgBtnMute.setColorFilter(strokeColor);
        } else {
            new CreateRoundedView(strokeColor, btnRadius, 2, strokeColor, imgBtnMute);
            imgBtnMute.setColorFilter(filterColor);
        }
        isMute = !mIsMute;
    }

    @Override
    public void onSpeakerView(boolean mIsSpeaker) {
        if (mIsSpeaker) {
            new CreateRoundedView(strokeColor, btnRadius, 2, strokeColor, imgBtnSpeaker);
            imgBtnSpeaker.setColorFilter(filterColor);
        } else {
            new CreateRoundedView(backColor, btnRadius, 2, strokeColor, imgBtnSpeaker);
            imgBtnSpeaker.setColorFilter(strokeColor);
        }
        isSpeaker = !mIsSpeaker;
    }

    @Override
    public void onCameraView(String data, boolean mIsFront) {
        if (data == null) {
            mIsFront = isFront;
        }
        if (mIsFront) {
            new CreateRoundedView(backColor, btnRadius, 2, strokeColor, imgBtnSwitchCamera);
            imgBtnSwitchCamera.setColorFilter(strokeColor);
        } else {
            new CreateRoundedView(strokeColor, btnRadius, 2, strokeColor, imgBtnSwitchCamera);
            imgBtnSwitchCamera.setColorFilter(filterColor);
        }
        isFront = !mIsFront;
    }

    @Override
    public void onEstablishedAfterUI() {
        imgBtnSwitchCamera.setVisibility(View.VISIBLE);
        imgBtnMute.setVisibility(View.VISIBLE);
        imgBtnSpeaker.setVisibility(View.VISIBLE);

        if (!dataProvider.isVideoCall) {
            imgBtnSwitchCamera.setVisibility(View.GONE);
            if (CommunicationManager.MEDIA_TYPE == CommunicationManager.MEDIA.TWILIO) {
                rlTwilioView.setVisibility(View.GONE);
            } else if (CommunicationManager.MEDIA_TYPE == CommunicationManager.MEDIA.LOCAL) {
                rlLocalView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onCallEnded() {
        stopRinging();
        isCallStart = false;
        if (mDurationTask != null) {
            mDurationTask.cancel();
            mTimer.cancel();
        }
        finish();
    }

    @Override
    public void onCallEstablished() {
        stopRinging();
        mCallStart = System.currentTimeMillis();
        isCallStart = true;
        intiCallingView();
        CommunicationManager.getInstance().setEstablishedAfterUI(isMute, isSpeaker, false, isFront);
    }

    @Override
    public void onCallProgressing() {
        txtCallState.setText(generalFunc.retrieveLangLBl("", "LBL_CALLING"));
        if (mAudioPlayer != null) {
            if (isIncomingView) {
                mAudioPlayer.playRingtone();
            } else {
                mAudioPlayer.playProgressTone();
            }
        }
    }

    @Override
    public void onUIChanges() {
        switch (CommunicationManager.MEDIA_TYPE) {
            case SINCH:
                if (dataProvider.isVideoCall) {
                    ivAvatar.setVisibility(View.GONE);
                } else {
                    ivAvatar.setVisibility(View.VISIBLE);
                }
                break;
            case TWILIO:
                if (dataProvider.isVideoCall) {
                    rlTwilioView.setVisibility(View.VISIBLE);
                    ivAvatar.setVisibility(View.GONE);
                } else {
                    rlTwilioView.setVisibility(View.GONE);
                    ivAvatar.setVisibility(View.VISIBLE);
                }
                break;
            case LOCAL:
                if (dataProvider.isVideoCall) {
                    rlLocalView.setVisibility(View.VISIBLE);
                    ivAvatar.setVisibility(View.GONE);
                } else {
                    rlLocalView.setVisibility(View.GONE);
                    ivAvatar.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    private class UpdateCallDurationTask extends TimerTask {

        @Override
        public void run() {
            runOnUiThread(() -> {
                if (mCallStart > 0) {
                    if (isCallStart) {
                        txtCallTime.setText(formatTimeSpan(System.currentTimeMillis() - mCallStart));
                    }
                } else {
                    if (CommunicationManager.MEDIA_TYPE == CommunicationManager.MEDIA.TWILIO || CommunicationManager.MEDIA_TYPE == CommunicationManager.MEDIA.LOCAL) {
                        if (isIncomingView) {
                            long totalSeconds = (System.currentTimeMillis() - mIncomingCallStart) / 1000;
                            long seconds = totalSeconds % 60;
                            if (seconds > 18) {
                                btnEndCall.performClick();
                            }
                        }
                    }
                }
            });
        }

        private String formatTimeSpan(long timeSpan) {
            long totalSeconds = timeSpan / 1000;
            long minutes = totalSeconds / 60;
            long seconds = totalSeconds % 60;
            return String.format(Locale.US, "%02d:%02d", minutes, seconds);
        }
    }
}