package com.general.files;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.leadryde.userapp.R;
import com.utils.Logger;
import com.utils.Utils;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;
import com.view.anim.loader.AVLoadingIndicatorView;

//import ru.noties.scrollable.CanScrollVerticallyDelegate;
//import ru.noties.scrollable.ScrollableLayout;

public class CovidDialog extends AppCompatActivity {
    private Context mContext;
    String LBL_CURRENT_PERSON_LIMIT="";

//    public CovidDialog(Context mContext) {
//        this.mContext = mContext;
//    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.covid_info);
        mContext=this;

        ImageView backArrowImgView = (ImageView) findViewById(R.id.backArrowImgView);

        backArrowImgView.setOnClickListener(v ->
        {

            finish();
        });
        RelativeLayout container = (RelativeLayout) findViewById(R.id.container);
        WebView mWebView = (WebView) findViewById(R.id.webView);
        AVLoadingIndicatorView loaderView = (AVLoadingIndicatorView) findViewById(R.id.loaderView);

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress > 90) {
                    loaderView.setVisibility(View.GONE);
                }
            }
        });

        mWebView.getSettings().setJavaScriptEnabled(true);


        String url = getIntent().getStringExtra("URL");
       // mWebView.loadUrl(url + "&fromapp=yes");



        mWebView.requestFocus();
        mWebView.getSettings().setLightTouchEnabled(true);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setGeolocationEnabled(true);
        mWebView.setSoundEffectsEnabled(true);
        mWebView.loadDataWithBaseURL(null,url,
                "text/html", "UTF-8",null);

        loaderView.setVisibility(View.VISIBLE);
        GeneralFunctions generalFunctions=new GeneralFunctions(mContext);
        MTextView capacityTxt = (MTextView) findViewById(R.id.capacityTxt);
        MButton btn;
        btn = ((MaterialRippleLayout) findViewById(R.id.btn)).getChildView();
        btn.setText(generalFunctions.retrieveLangLBl("Agree and ride","LBL_AGREE"));
        btn.setId(Utils.generateViewId());
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("isRideNow",getIntent().getBooleanExtra("isRideNow",false));
                returnIntent.putExtra("isRental", getIntent().getBooleanExtra("isRental", false));
                returnIntent.putExtra("isDeliverNow", getIntent().getBooleanExtra("isDeliverNow", false));
                returnIntent.putExtra("isDeliverLater", getIntent().getBooleanExtra("isDeliverLater", false));

                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });
        LBL_CURRENT_PERSON_LIMIT=getIntent().getStringExtra("LBL_CURRENT_PERSON_LIMIT");
        if (Utils.checkText(LBL_CURRENT_PERSON_LIMIT)) {
            capacityTxt.setText(LBL_CURRENT_PERSON_LIMIT);
        }
    }

    @SuppressLint({"ClickableViewAccessibility", "SetJavaScriptEnabled"})
    public void open(String url, String imageURL) {
        try {
            final Dialog addAddressDailog = new Dialog(mContext);
            View contentView = View.inflate(mContext, R.layout.covid_info, null);

            addAddressDailog.setContentView(contentView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    Utils.dpToPx(ViewGroup.LayoutParams.MATCH_PARENT, mContext)));
            addAddressDailog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

            addAddressDailog.setCancelable(true);

            int screenHeight = ((int) Utils.getScreenPixelHeight(mContext));
            int peekHeight = 0;
            int bottomMarginForLoader = Utils.dpToPx(50, mContext);
            GeneralFunctions generalFunctions = new GeneralFunctions(mContext);

            ImageView backArrowImgView = (ImageView) addAddressDailog.findViewById(R.id.backArrowImgView);

            backArrowImgView.setOnClickListener(v -> addAddressDailog.dismiss());
            RelativeLayout container = (RelativeLayout) addAddressDailog.findViewById(R.id.container);
            WebView mWebView = (WebView) addAddressDailog.findViewById(R.id.webView);
            MTextView capacityTxt = (MTextView) addAddressDailog.findViewById(R.id.capacityTxt);
            MButton btn;
            btn = ((MaterialRippleLayout) addAddressDailog.findViewById(R.id.btn)).getChildView();
            btn.setText(generalFunctions.retrieveLangLBl("Agree and ride", "LBL_AGREE_AND_RIDE"));
            btn.setId(Utils.generateViewId());
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("isRideNow", getIntent().getBooleanExtra("isRideNow", false));
                    returnIntent.putExtra("isRental", getIntent().getBooleanExtra("isRental", false));
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                }
            });
            capacityTxt.setText(LBL_CURRENT_PERSON_LIMIT);
            AVLoadingIndicatorView loaderView = (AVLoadingIndicatorView) addAddressDailog.findViewById(R.id.loaderView);

            RelativeLayout.LayoutParams loaderView_ly_params = (RelativeLayout.LayoutParams) loaderView.getLayoutParams();
            loaderView_ly_params.bottomMargin = screenHeight - peekHeight + bottomMarginForLoader;
            loaderView.setLayoutParams(loaderView_ly_params);

            mWebView.setWebChromeClient(new WebChromeClient() {
                @Override
                public void onProgressChanged(WebView view, int newProgress) {
                    if (newProgress > 90) {
                        loaderView.setVisibility(View.GONE);
                    }
                }
            });

            mWebView.getSettings().setJavaScriptEnabled(true);
            //  mWebView.loadUrl(url + "&fromapp=yes");
            //  loaderView.setVisibility(View.VISIBLE);

            mWebView.loadDataWithBaseURL(null,url,
                    "text/html", "UTF-8",null);

            addAddressDailog.show();
        }
        catch (Exception e)

        {
            Logger.d("covidEx","::"+e.toString());
        }
    }


}
