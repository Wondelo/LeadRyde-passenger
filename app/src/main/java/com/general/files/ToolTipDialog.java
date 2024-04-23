package com.general.files;

import android.content.Context;
import android.view.View;
import android.webkit.WebView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.adapter.files.UberXCategoryAdapter;
import com.leadryde.userapp.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.utils.Utils;
import com.view.MTextView;

public class ToolTipDialog {

    BottomSheetDialog tipDialog;
    Context context;
    GeneralFunctions generalFunctions;
    String Title;
    String Msg;

    public ToolTipDialog(Context context,GeneralFunctions generalFunctions,String Title,String Msg)
    {
        this.context=context;
        this.generalFunctions=generalFunctions;
        this.Title=Title;
        this.Msg=Msg;

        this.generalFunctions=generalFunctions;
        createView();
    }




    public void createView()
    {
        if (tipDialog != null && tipDialog.isShowing()) {
            return;
        }
        tipDialog = new BottomSheetDialog(context);


        View contentView = View.inflate(context, R.layout.desgin_tooltip, null);

        tipDialog.setContentView(contentView);
        tipDialog.setCancelable(true);
        BottomSheetBehavior mBehavior = BottomSheetBehavior.from((View) contentView.getParent());
        mBehavior.setPeekHeight(Utils.dipToPixels(context, 340));
        mBehavior.setHideable(true);
        MTextView titleTxt = contentView.findViewById(R.id.titleTxt);
        MTextView okTxt = contentView.findViewById(R.id.okTxt);
        WebView msgTxt = contentView.findViewById(R.id.msgTxt);

        titleTxt.setText(Title);
        okTxt.setText(generalFunctions.retrieveLangLBl("", "LBL_OK_THANKS"));
        okTxt.setOnClickListener(v -> tipDialog.dismiss());


        msgTxt.loadDataWithBaseURL(null, generalFunctions.wrapHtml(msgTxt.getContext(), Msg), "text/html", "UTF-8", null);







        tipDialog.show();
    }
}
