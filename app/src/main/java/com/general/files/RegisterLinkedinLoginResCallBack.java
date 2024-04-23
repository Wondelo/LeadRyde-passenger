package com.general.files;

import android.content.Context;


import com.leadryde.userapp.AppLoignRegisterActivity;
import com.view.MyProgressDialog;

/**
 * Created by Admin on 29-06-2016.
 */
public class RegisterLinkedinLoginResCallBack {
    Context mContext;
    GeneralFunctions generalFunc;


    MyProgressDialog myPDialog;

    AppLoignRegisterActivity appMainLoginAct;
    public boolean isrestart;

    public RegisterLinkedinLoginResCallBack(Context mContext) {
        this.mContext = mContext;

        generalFunc = MyApp.getInstance().getGeneralFun(mContext);
        appMainLoginAct = (AppLoignRegisterActivity) mContext;

    }

    public RegisterLinkedinLoginResCallBack(Context mContext, boolean isrestart) {
        this.mContext = mContext;
        this.isrestart = isrestart;

        generalFunc = MyApp.getInstance().getGeneralFun(mContext);
    }

    public void continueLogin() {
        if (appMainLoginAct==null)
        {
            OpenLinkedinDialog openLinkedinDialog = new OpenLinkedinDialog(mContext, generalFunc,isrestart);
        }else
        {
            OpenLinkedinDialog openLinkedinDialog = new OpenLinkedinDialog(mContext, generalFunc);
        }


    }


}
