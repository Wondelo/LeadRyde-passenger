package com.dialogs;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.general.call.CommunicationManager;
import com.general.call.MediaDataProvider;
import com.general.files.GeneralFunctions;
import com.general.files.MyApp;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.leadryde.userapp.R;
import com.utils.Utils;
import com.utils.VectorUtils;
import com.view.GenerateAlertBox;
import com.view.MTextView;

import java.util.ArrayList;

public class CommunicationCallTypeDialog {

    @Nullable
    private BottomSheetDialog sheetDialog;
    private GeneralFunctions generalFunc;
    private CommunicationCallTypeList listener;

    private GenerateAlertBox currentAlertBox;
    private boolean isOpenAllowAllDialog = false;
    private final ArrayList<String> requestPermissions = new ArrayList<>();

    public void setListener(CommunicationCallTypeList listener) {
        this.listener = listener;
    }

    public void showPreferenceDialog(Context mContext, MediaDataProvider dataProvider) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        generalFunc = MyApp.getInstance().getGeneralFun(mContext);

        View dialogView = View.inflate(mContext, R.layout.design_communication_call_type, null);
        builder.setView(dialogView);

        ImageView imgVideo = dialogView.findViewById(R.id.imgVideo);
        ImageView imgAudio = dialogView.findViewById(R.id.imgAudio);
        VectorUtils.manageVectorImage(mContext, imgVideo, R.drawable.ic_video, R.drawable.ic_video_compat);
        VectorUtils.manageVectorImage(mContext, imgAudio, R.drawable.ic_audio, R.drawable.ic_audio_compat);


        ImageView closeImg = dialogView.findViewById(R.id.closeImg);
        closeImg.setOnClickListener(v -> {
            if (sheetDialog != null) {
                sheetDialog.dismiss();
            }
        });

        MTextView txtTitle = dialogView.findViewById(R.id.txtTitle);
        MTextView txtAudio = dialogView.findViewById(R.id.txtAudio);
        MTextView txtVideo = dialogView.findViewById(R.id.txtVideo);
        txtTitle.setText(generalFunc.retrieveLangLBl("", "LBL_CHOOSE_CALL_TYPE_TXT"));
        txtAudio.setText(generalFunc.retrieveLangLBl("", "LBL_AUDIO_CALL"));
        txtVideo.setText(generalFunc.retrieveLangLBl("", "LBL_VIDEO_CALL"));

        LinearLayout llVideoArea = dialogView.findViewById(R.id.llVideoArea);
        llVideoArea.setOnClickListener(view -> {
            // Video Call Click
            checkPermissions(mContext, CommunicationManager.TYPE.VIDEO_CALL, dataProvider);
        });

        LinearLayout llAudioArea = dialogView.findViewById(R.id.llAudioArea);
        llAudioArea.setOnClickListener(view -> {
            // Audio Call Click
            checkPermissions(mContext, CommunicationManager.TYPE.VOIP_CALL, dataProvider);
        });

        sheetDialog = new BottomSheetDialog(mContext);
        sheetDialog.setContentView(dialogView);
        View bottomSheetView = sheetDialog.getWindow().getDecorView().findViewById(R.id.design_bottom_sheet);
        bottomSheetView.setBackgroundColor(mContext.getResources().getColor(android.R.color.transparent));

        (BottomSheetBehavior.from((View) dialogView.getParent())).setPeekHeight(Utils.dpToPx(600, mContext));

        sheetDialog.setCancelable(false);
        if (generalFunc.isRTLmode()) {
            generalFunc.forceRTLIfSupported(sheetDialog);
        }
        Animation a = AnimationUtils.loadAnimation(mContext, R.anim.bottom_up);
        a.reset();
        bottomSheetView.clearAnimation();
        bottomSheetView.startAnimation(a);
        sheetDialog.show();
    }

    public void showNoPermission() {
        currentAlertBox = generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("Application requires some permission to be granted to work. Please allow it.", "LBL_ALLOW_PERMISSIONS_APP"),
                generalFunc.retrieveLangLBl("Cancel", "LBL_CANCEL_TXT"), generalFunc.retrieveLangLBl("Allow All", "LBL_ALLOW_ALL_TXT"),
                buttonId -> {
                    if (buttonId == 0) {
                        currentAlertBox.closeAlertBox();
                        if (sheetDialog != null) {
                            sheetDialog.dismiss();
                        }
                    } else {
                        generalFunc.openSettings();
                    }
                });
    }

    public void checkPermissions(Context mContext, CommunicationManager.TYPE commType, MediaDataProvider dataProvider) {
        generalFunc = MyApp.getInstance().getGeneralFun(mContext);
        if (commType == CommunicationManager.TYPE.VIDEO_CALL) {
            requestPermissions.add(android.Manifest.permission.CAMERA);
        }
        requestPermissions.add(android.Manifest.permission.RECORD_AUDIO);
        if (generalFunc.isAllPermissionGranted(true, requestPermissions)) {
            listener.onCallTypeSelected(mContext, commType, dataProvider);
            if (sheetDialog != null) {
                sheetDialog.dismiss();
            }
        } else {
            if (isOpenAllowAllDialog) {
                showNoPermission();
            }
            isOpenAllowAllDialog = true;
        }
    }

    public interface CommunicationCallTypeList {
        void onCallTypeSelected(Context mContext, CommunicationManager.TYPE type, MediaDataProvider dataProvider);
    }


}