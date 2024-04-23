package com.general.files;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.widget.AppCompatCheckBox;

import com.leadryde.userapp.R;
import com.utils.CommonUtilities;
import com.view.MTextView;

public class showTermsDialog {
    GeneralFunctions generalFunc;
    int pos;
    String service;
    Context mContext;
    boolean click;
    androidx.appcompat.app.AlertDialog alertDialog;
    private OnClickList clickListener = null;
    boolean hideCloseView = false;

    public showTermsDialog(Context mContext, GeneralFunctions generalFunc, int position, String service, boolean click, OnClickList clickListener) {
        this.generalFunc = generalFunc;
        this.pos = position;
        this.service = service;
        this.click = click;
        this.mContext = mContext;
        this.clickListener = clickListener;
        showDialog();
    }

    public showTermsDialog(Context mContext, GeneralFunctions generalFunc, int position, String service, boolean click, OnClickList clickListener, boolean hideCloseView) {
        this.generalFunc = generalFunc;
        this.pos = position;
        this.service = service;
        this.click = click;
        this.mContext = mContext;
        this.clickListener = clickListener;
        this.hideCloseView = hideCloseView;
        showDialog();
    }

    private void showDialog() {
        //


        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(mContext);
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.age_confirm_dialog, null);

        ImageView cancelImg = (ImageView) dialogView.findViewById(R.id.cancelImg);
        LinearLayout confirmlayout = (LinearLayout) dialogView.findViewById(R.id.confirmlayout);
        MTextView submitTxt = (MTextView) dialogView.findViewById(R.id.submitTxt);
        MTextView subTitleTxt = (MTextView) dialogView.findViewById(R.id.subTitleTxt);
        MTextView confirmTxt = (MTextView) dialogView.findViewById(R.id.confirmTxt);
        AppCompatCheckBox confirmChkBox = (AppCompatCheckBox) dialogView.findViewById(R.id.confirmChkBox);
        subTitleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_AGE_CONFIRMATION"));
        confirmTxt.setText(generalFunc.retrieveLangLBl("", "LBL_AGE_NOTE"));
        submitTxt.setText(generalFunc.retrieveLangLBl("", "LBL_CONFIRM_TXT"));
        confirmTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(confirmChkBox.isChecked())
                {
                    confirmChkBox.setChecked(false);
                }
                else
                {
                    confirmChkBox.setChecked(true);
                }

            }
        });

        cancelImg.setVisibility(hideCloseView ? View.GONE : View.VISIBLE);

        builder.setView(dialogView);
        cancelImg.setOnClickListener(v -> alertDialog.dismiss());
        confirmlayout.setOnClickListener(v -> {
            if (click) {
                CommonUtilities.ageRestrictServices.add(service);
                alertDialog.dismiss();

                if (clickListener != null) {
                    clickListener.onClick();
                }
            }

        });

        confirmChkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    click = true;
                    confirmlayout.setAlpha(1);
                } else {
                    click = false;
                    confirmlayout.setAlpha((float) 0.4);
                }
            }
        });


        builder.setView(dialogView);
        alertDialog = builder.create();
        if (generalFunc.isRTLmode()) {
            generalFunc.forceRTLIfSupported(alertDialog);
        }
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.getWindow().setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.all_roundcurve_card));
        alertDialog.show();

    }

    public interface OnClickList {
        void onClick();
    }
}
