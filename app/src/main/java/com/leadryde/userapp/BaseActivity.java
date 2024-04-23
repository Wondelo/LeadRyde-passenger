package com.leadryde.userapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.general.call.CommunicationManager;
import com.general.files.GeneralFunctions;
import com.general.files.MyApp;


public abstract class BaseActivity extends AppCompatActivity {

    public GeneralFunctions generalFunc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        generalFunc = MyApp.getInstance().getGeneralFun(this);
    }

    public void manageSinchClient(String jsonValue) {
        CommunicationManager.getInstance().initiateService(generalFunc, jsonValue);
    }
}