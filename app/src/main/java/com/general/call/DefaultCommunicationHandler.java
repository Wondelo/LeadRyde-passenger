package com.general.call;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class DefaultCommunicationHandler {

    private static DefaultCommunicationHandler instance;

    public static DefaultCommunicationHandler getInstance() {
        if (instance == null) {
            instance = new DefaultCommunicationHandler();
        }
        return instance;
    }

    public void initiateService() {
    }

    public void executeAction(Context mContext, CommunicationManager.TYPE communication_type, MediaDataProvider dataProvider) {
        switch (communication_type) {
            case PHONE_CALL:
            case VIDEO_CALL:
            case VOIP_CALL:
                makeCall(mContext, dataProvider);
                break;
            case CHAT:
                chat(mContext, dataProvider);
                break;
        }
    }

    private void makeCall(Context mContext, MediaDataProvider dataProvider) {
        try {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + dataProvider.phoneNumber));
            mContext.startActivity(callIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void chat(Context mContext, MediaDataProvider dataProvider) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("smsto:" + Uri.encode(dataProvider.phoneNumber)));
        mContext.startActivity(intent);
    }
}