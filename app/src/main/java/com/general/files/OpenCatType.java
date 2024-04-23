package com.general.files;

import android.content.Context;
import android.os.Bundle;

import com.leadryde.userapp.CommonDeliveryTypeSelectionActivity;
import com.leadryde.userapp.DonationActivity;
import com.leadryde.userapp.MainActivity;
import com.utils.CommonUtilities;
import com.utils.Utils;
import com.view.GenerateAlertBox;

import java.util.HashMap;
import java.util.Locale;

public class OpenCatType {
    Context mContext;
    HashMap<String, String> mapData;
    GeneralFunctions generalFunc;
    String userProfileJson="";

    public OpenCatType(Context mContext, HashMap<String, String> mapData) {
        this.mContext = mContext;
        this.mapData = mapData;

        generalFunc = new GeneralFunctions(mContext);
        userProfileJson = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);

    }

    public void execute() {
        if (mapData.get("eCatType") != null) {
            Bundle bn = new Bundle();
            String s = mapData.get("eCatType").toUpperCase(Locale.US);
            generalFunc.storeData(Utils.iServiceId_KEY, "");

            if (!generalFunc.getJsonValue("APP_TYPE", userProfileJson).equalsIgnoreCase("Delivery")) {
                String latitude=mapData.get("latitude");
                String longitude=mapData.get("longitude");
                String address=mapData.get("address");

                if (latitude == null || latitude.equalsIgnoreCase("0.0") ) {
                    bn.putString("latitude", "");
                } else {
                    bn.putString("latitude", latitude);
                }
                if (longitude == null || longitude.equalsIgnoreCase("0.0") ) {
                    bn.putString("longitude", "");
                } else {
                    bn.putString("longitude", longitude);
                }

                if (address == null || address.equalsIgnoreCase("")) {
                    bn.putString("address", "");
                } else {
                    bn.putString("address", address);
                }

            }


            if ("RIDE".equals(s)) {
                bn.putString("selType", Utils.CabGeneralType_Ride);
                bn.putBoolean("isRestart", false);
                if(mapData.containsKey("isHome"))
                {
                    bn.putBoolean("isHome", true);
                }
                else if(mapData.containsKey("isWork"))
                {
                    bn.putBoolean("isWork", true);
                }
                bn.putBoolean("isAddressAdded", false);
                bn.putBoolean("isBlankAddressAdded", true);

                bn.putDouble("pic_lat", Double.parseDouble(mapData.get("pic_lat")));
                bn.putDouble("pic_long", Double.parseDouble(mapData.get("pic_long")));
                bn.putString("pic_address", mapData.get("pic_address"));
                bn.putBoolean("isFromHome", true);
                new StartActProcess(mContext).startActWithData(MainActivity.class, bn);
                return;
            } else if ("FLY".equals(s)) {
                bn.putString("selType", Utils.CabGeneralType_Ride);
                bn.putBoolean("eFly", true);
                bn.putBoolean("isRestart", false);
                new StartActProcess(mContext).startActWithData(MainActivity.class, bn);
                return;
            } else if ("MOTORIDE".equals(s)) {

                bn.putString("selType", Utils.CabGeneralType_Ride);
                bn.putBoolean("isRestart", false);
                bn.putBoolean("emoto", true);
                new StartActProcess(mContext).startActWithData(MainActivity.class, bn);
                return;
            } else if ("DELIVERY".equals(s)) {
                boolean isMulti=mapData.get("eDeliveryType") != null && mapData.get("eDeliveryType").equalsIgnoreCase("Multi");
                if (isMulti) {
                    bn.putBoolean("fromMulti", true);
                }
                /*Single Delivery UI as Multi Delivery - SdUiAsMd*/
                else if(generalFunc.retrieveValue("ENABLE_MULTI_VIEW_IN_SINGLE_DELIVERY").equalsIgnoreCase("Yes") && !isMulti)
                {
                    //chaange for testing
                    bn.putBoolean("fromMulti", false);
                    bn.putString("maxDestination", "1");
                }
                bn.putString("selType", Utils.CabGeneralType_Deliver);
                bn.putBoolean("isRestart", false);
                new StartActProcess(mContext).startActWithData(MainActivity.class, bn);
                return;
            } else if ("MOTODELIVERY".equals(s)) {

                boolean isMulti=mapData.get("eDeliveryType") != null && mapData.get("eDeliveryType").equalsIgnoreCase("Multi");
                if (isMulti) {
                    bn.putBoolean("fromMulti", true);
                }
                /*Single Delivery UI as Multi Delivery - SdUiAsMd*/
                else if(generalFunc.retrieveValue("ENABLE_MULTI_VIEW_IN_SINGLE_DELIVERY").equalsIgnoreCase("Yes") && !isMulti)
                {
                    bn.putBoolean("fromMulti", true);
                    bn.putString("maxDestination", "1");
                }
                bn.putString("selType", Utils.CabGeneralType_Deliver);
                bn.putBoolean("isRestart", false);
                bn.putBoolean("emoto", true);
                new StartActProcess(mContext).startActWithData(MainActivity.class, bn);
                return;
            } else if ("RENTAL".equals(s)) {
                bn.putString("selType", "rental");
                bn.putBoolean("isRestart", false);

                bn.putBoolean("isAddressAdded", false);
                bn.putBoolean("isBlankAddressAdded", true);
                bn.putDouble("pic_lat", Double.parseDouble(mapData.get("pic_lat")));
                bn.putDouble("pic_long", Double.parseDouble(mapData.get("pic_long")));
                bn.putString("pic_address", mapData.get("pic_address"));
                bn.putBoolean("isFromHome", true);

                new StartActProcess(mContext).startActWithData(MainActivity.class, bn);
                return;
            } else if ("MOTORENTAL".equals(s)) {
                bn.putString("selType", "rental");
                bn.putBoolean("isRestart", false);
                bn.putBoolean("emoto", true);
                new StartActProcess(mContext).startActWithData(MainActivity.class, bn);
                return;
            }
            else if("Genie".equalsIgnoreCase(s))
            {
                bn.putString("vCategory", mapData.get("vCategory"));
                bn.putString("eCatType", mapData.get("eCatType"));
                bn.putString("iVehicleCategoryId", mapData.get("iVehicleCategoryId"));

                return;

            }
            else if("Runner".equalsIgnoreCase(s))
            {
                bn.putString("vCategory", mapData.get("vCategory"));
                bn.putString("eCatType", mapData.get("eCatType"));
                bn.putString("iVehicleCategoryId", mapData.get("iVehicleCategoryId"));
                return;
            }
            else if("Anywhere".equalsIgnoreCase(s))
            {
                bn.putString("vCategory", mapData.get("vCategory"));
                bn.putString("eCatType", mapData.get("eCatType"));
                bn.putString("iVehicleCategoryId", mapData.get("iVehicleCategoryId"));
                return;
            }
            else if ("DELIVERALL".equals(s)) {
                CommonUtilities.ageRestrictServices.clear();
                goToDeliverAllScreen(mapData.get("iServiceId"));
                return;
            } else if ("MOREDELIVERY".equals(s)) {
                bn.putString("iVehicleCategoryId", mapData.get("iVehicleCategoryId"));
                bn.putString("vCategory", mapData.get("vCategory"));
                new StartActProcess(mContext).startActWithData(CommonDeliveryTypeSelectionActivity.class, bn);
                return;
            } else if ("Donation".equalsIgnoreCase(s)) {
                new StartActProcess(mContext).startActWithData(DonationActivity.class, bn);
                return;
            }
        }
    }

    private void goToDeliverAllScreen(String iServiceId) {
        if (generalFunc.prefHasKey(Utils.iServiceId_KEY) && generalFunc != null) {
            generalFunc.removeValue(Utils.iServiceId_KEY);
        }
        HashMap<String, String> storeData = new HashMap<>();
        storeData.put(Utils.iServiceId_KEY, iServiceId);
        storeData.put("DEFAULT_SERVICE_CATEGORY_ID", iServiceId);
        generalFunc.storeData(storeData);
        getLanguageLabelServiceWise();
    }

    private void getLanguageLabelServiceWise() {

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "getUserLanguagesAsPerServiceType");
        parameters.put("LanguageCode", generalFunc.retrieveValue(Utils.LANGUAGE_CODE_KEY));
        parameters.put("eSystem", Utils.eSystem_Type);

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(mContext, parameters);
        exeWebServer.setLoaderConfig(mContext, true, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {
            if (responseString != null && !responseString.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);

                if (isDataAvail) {
                    generalFunc.storeData(Utils.languageLabelsKey, generalFunc.getJsonValue(Utils.message_str, responseString));
                    generalFunc.storeData(Utils.LANGUAGE_CODE_KEY, generalFunc.getJsonValue("vLanguageCode", responseString));
                    generalFunc.storeData(Utils.LANGUAGE_IS_RTL_KEY, generalFunc.getJsonValue("langType", responseString));

                    generalFunc.storeData(Utils.GOOGLE_MAP_LANGUAGE_CODE_KEY, generalFunc.getJsonValue("vGMapLangCode", responseString));
                    GeneralFunctions.clearAndResetLanguageLabelsData(MyApp.getInstance().getApplicationContext());
                    Utils.setAppLocal(mContext);

                    Bundle bn = new Bundle();
                    String latitude=mapData.get("latitude");
                    String longitude=mapData.get("longitude");
                    String address=mapData.get("address");
                    if (latitude == null || latitude.equalsIgnoreCase("0.0")) {
                        bn.putString("latitude", "");
                        bn.putString("lat", "");
                    } else {
                        bn.putString("latitude", latitude);
                        bn.putString("lat", latitude);
                    }
                    if (longitude == null || longitude.equalsIgnoreCase("0.0")) {
                        bn.putString("longitude", "");
                        bn.putString("long", "");
                    } else {
                        bn.putString("longitude", longitude.toString());
                        bn.putString("long", longitude.toString());
                    }

                    if (address == null || address.equalsIgnoreCase("")) {
                        bn.putString("address", "");
                    } else {
                        bn.putString("address", address);
                    }
                    bn.putBoolean("isback", true);

                  //  new StartActProcess(mContext).startActWithData(FoodDeliveryHomeActivity.class, bn);

                    if (generalFunc.retrieveValue("CHECK_SYSTEM_STORE_SELECTION").equalsIgnoreCase("Yes")) {
                        bn.putString("iCompanyId", generalFunc.getJsonValue("STORE_ID",responseString));
                        bn.putString("ispriceshow", generalFunc.getJsonValue("ispriceshow",responseString));

                        bn.putString("eAvailable",generalFunc.getJsonValue("eAvailable",responseString));
                        bn.putString("timeslotavailable",generalFunc.getJsonValue("timeslotavailable",responseString));
                    }
                    else {
                   //     new StartActProcess(mContext).startActWithData(GenieDeliveryHomeActivity.class, bn);
                    }
                } else {
                    errorCallApi();
                }
            } else {
                errorCallApi();
            }

        });
        exeWebServer.execute();
    }

    private void errorCallApi() {
        final GenerateAlertBox generateAlert = new GenerateAlertBox(mContext);
        generateAlert.setCancelable(false);
        generateAlert.setBtnClickList(btn_id -> {
            if (btn_id == 0) {
                generateAlert.closeAlertBox();
            } else {
                getLanguageLabelServiceWise();
                generateAlert.closeAlertBox();
            }
        });
        generateAlert.setContentMessage("", generalFunc.retrieveLangLBl("", "LBL_ERROR_TXT"));
        generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_RETRY_TXT"));
        generateAlert.setNegativeBtn(generalFunc.retrieveLangLBl("", "LBL_CANCEL_TXT"));
        generateAlert.showAlertBox();
    }
}
