package com.leadryde.userapp;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.adapter.files.EmergencyContactRecycleAdapter;
import com.bumptech.glide.Glide;
import com.countryview.view.CountryPicker;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.MyApp;
import com.utils.Utils;
import com.view.ErrorView;
import com.view.GenerateAlertBox;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;
import com.view.editBox.MaterialEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class EmergencyContactActivity extends AppCompatActivity implements EmergencyContactRecycleAdapter.OnItemClickList {

    int PICK_CONTACT = 2121;

    MTextView titleTxt;
    ImageView backImgView;

    GeneralFunctions generalFunc;

    MButton btn_type2;

    ProgressBar loading;
    RelativeLayout dataContainer;
    LinearLayout noContactArea;
    androidx.appcompat.app.AlertDialog alertDialog;

    RecyclerView emeContactRecyclerView;
    EmergencyContactRecycleAdapter adapter;
    ErrorView errorView;
    ImageView bannerImg;
    LinearLayout mainArea;

    ArrayList<HashMap<String, String>> list;

    private CountryPicker countryPicker;
    Locale locale;
    private String vSImage = "", vCountryCode = "", vPhoneCode = "";
    public JSONObject obj_userProfile;
    String userProfileJson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contact);

        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());
        ImageView backImgView = findViewById(R.id.backImgView);
        locale = new Locale(generalFunc.retrieveValue(Utils.LANGUAGE_CODE_KEY));
        userProfileJson = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);
        obj_userProfile = generalFunc.getJsonObject(userProfileJson);

        mainArea = findViewById(R.id.mainArea);
        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        loading = (ProgressBar) findViewById(R.id.loading);
        btn_type2 = ((MaterialRippleLayout) findViewById(R.id.btn_type2)).getChildView();
        emeContactRecyclerView = (RecyclerView) findViewById(R.id.emeContactRecyclerView);
        errorView = (ErrorView) findViewById(R.id.errorView);
        dataContainer = (RelativeLayout) findViewById(R.id.dataContainer);
        noContactArea = (LinearLayout) findViewById(R.id.noContactArea);
        noContactArea = (LinearLayout) findViewById(R.id.noContactArea);
        bannerImg = (ImageView) findViewById(R.id.bannerImg);


        list = new ArrayList<>();
        adapter = new EmergencyContactRecycleAdapter(getActContext(), list);
        emeContactRecyclerView.setAdapter(adapter);

        setLabels();

        btn_type2.setId(Utils.generateViewId());
        btn_type2.setOnClickListener(new setOnClickList());
        backImgView.setOnClickListener(new setOnClickList());

        getContacts();

        adapter.setOnItemClickList(this);
    }

    @Override
    public void onItemClick(int position) {
        buildWarningMessage(list.get(position).get("iEmergencyId"));
    }

    public void setLabels() {
        titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_EMERGENCY_CONTACT"));

        String userprofilejson = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);
        if (generalFunc.getJsonValue("APP_TYPE", userprofilejson).equalsIgnoreCase(Utils.CabGeneralTypeRide_Delivery_UberX)) {
            ((MTextView) findViewById(R.id.emeTitleTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_FOR_SAFETY"));
        } else {
            ((MTextView) findViewById(R.id.emeTitleTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_EMERGENCY_CONTACT_TITLE"));
        }
        ((MTextView) findViewById(R.id.emeSubTitleTxt1)).setText(generalFunc.retrieveLangLBl("", "LBL_EMERGENCY_CONTACT_SUB_TITLE1"));
        ((MTextView) findViewById(R.id.emeSubTitleTxt2)).setText(generalFunc.retrieveLangLBl("", "LBL_EMERGENCY_CONTACT_SUB_TITLE2"));
        ((MTextView) findViewById(R.id.notifyTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_ADD_EMERGENCY_UP_TO_COUNT"));
        btn_type2.setText(generalFunc.retrieveLangLBl("", "LBL_ADD_CONTACTS"));


    }

    public void closeLoader() {
        if (loading.getVisibility() == View.VISIBLE) {
            loading.setVisibility(View.GONE);
        }
    }

    public void getContacts() {
        mainArea.setBackgroundColor(Color.parseColor("#EBEBEB"));
        (findViewById(R.id.btn_type2)).setVisibility(View.GONE);
        (findViewById(R.id.notifyTxt)).setVisibility(View.GONE);
        dataContainer.setVisibility(View.VISIBLE);
        noContactArea.setVisibility(View.GONE);

        if (errorView.getVisibility() == View.VISIBLE) {
            errorView.setVisibility(View.GONE);
        }
        if (loading.getVisibility() != View.VISIBLE) {
            loading.setVisibility(View.VISIBLE);
        }

        if (list.size() > 0) {
            list.clear();
        }

        final HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "loadEmergencyContacts");
        parameters.put("iUserId", generalFunc.getMemberId());
        parameters.put("UserType", Utils.userType);

        final ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {

                noContactArea.setVisibility(View.GONE);
                JSONObject responseObj = generalFunc.getJsonObject(responseString);

                if (responseObj != null && !responseObj.equals("")) {

                    closeLoader();

                    if (generalFunc.checkDataAvail(Utils.action_str, responseObj) == true) {

                        JSONArray obj_arr = generalFunc.getJsonArray(Utils.message_str, responseObj);

                        for (int i = 0; i < obj_arr.length(); i++) {
                            JSONObject obj_temp = generalFunc.getJsonObject(obj_arr, i);

                            HashMap<String, String> map = new HashMap<String, String>();

                            map.put("ContactName", generalFunc.getJsonValueStr("vName", obj_temp));
                            map.put("ContactPhone", generalFunc.getJsonValueStr("vPhone", obj_temp));
                            map.put("iEmergencyId", generalFunc.getJsonValueStr("iEmergencyId", obj_temp));

                            list.add(map);
                        }

                        adapter.notifyDataSetChanged();

                        if (obj_arr.length() >= 5) {
                            (findViewById(R.id.notifyTxt)).setVisibility(View.GONE);
                            (findViewById(R.id.btn_type2)).setVisibility(View.GONE);
                        } else {
                            (findViewById(R.id.notifyTxt)).setVisibility(View.VISIBLE);
                            (findViewById(R.id.btn_type2)).setVisibility(View.VISIBLE);
                        }

                    } else {
                        mainArea.setBackgroundColor(Color.parseColor("#FFFFFF"));
                        noContactArea.setVisibility(View.VISIBLE);
                        dataContainer.setVisibility(View.GONE);

                        (findViewById(R.id.notifyTxt)).setVisibility(View.VISIBLE);
                        (findViewById(R.id.btn_type2)).setVisibility(View.VISIBLE);
                    }
                } else {
                    generateErrorView();
                }
            }
        });
        exeWebServer.execute();
    }

    public void addContact(String contactName, String contactPhone) {
        final HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "addEmergencyContacts");
        parameters.put("iUserId", generalFunc.getMemberId());
        parameters.put("vName", contactName);
        parameters.put("Phone", contactPhone);
        parameters.put("UserType", Utils.userType);

        final ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {

                if (responseString != null && !responseString.equals("")) {

                    if (generalFunc.checkDataAvail(Utils.action_str, responseString) == true) {
                        getContacts();

                        generalFunc.showMessage(generalFunc.getCurrentView(EmergencyContactActivity.this),
                                generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)));
                    } else {
                        generalFunc.showGeneralMessage("",
                                generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)));
                    }
                } else {
                    generalFunc.showError();
                }
            }
        });
        exeWebServer.execute();
    }

    public void buildWarningMessage(final String iEmergencyId) {
        final GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
        generateAlert.setCancelable(false);
        generateAlert.setBtnClickList(new GenerateAlertBox.HandleAlertBtnClick() {
            @Override
            public void handleBtnClick(int btn_id) {
                generateAlert.closeAlertBox();

                if (btn_id == 1) {
                    deleteContact(iEmergencyId);
                }
            }
        });
        generateAlert.setContentMessage("", generalFunc.retrieveLangLBl("", "LBL_CONFIRM_MSG_DELETE_EME_CONTACT"));
        generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));

        generateAlert.setNegativeBtn(generalFunc.retrieveLangLBl("", "LBL_CANCEL_TXT"));


        generateAlert.showAlertBox();
    }

    public void deleteContact(String iEmergencyId) {
        final HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "deleteEmergencyContacts");
        parameters.put("iUserId", generalFunc.getMemberId());
        parameters.put("iEmergencyId", iEmergencyId);
        parameters.put("UserType", Utils.userType);

        final ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {

                if (responseString != null && !responseString.equals("")) {

                    if (generalFunc.checkDataAvail(Utils.action_str, responseString) == true) {
                        getContacts();

                        generalFunc.showMessage(generalFunc.getCurrentView(EmergencyContactActivity.this),
                                generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)));
                    } else {
                        generalFunc.showGeneralMessage("",
                                generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)));
                    }
                } else {
                    generalFunc.showError();
                }
            }
        });
        exeWebServer.execute();
    }

    public void generateErrorView() {

        closeLoader();

        generalFunc.generateErrorView(errorView, "LBL_ERROR_TXT", "LBL_NO_INTERNET_TXT");

        if (errorView.getVisibility() != View.VISIBLE) {
            errorView.setVisibility(View.VISIBLE);
        }
        errorView.setOnRetryListener(new ErrorView.RetryListener() {
            @Override
            public void onRetry() {
                getContacts();
            }
        });
    }

    public Context getActContext() {
        return EmergencyContactActivity.this;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Utils.SELECT_COUNTRY_REQ_CODE && resultCode == RESULT_OK && data != null) {
            vCountryCode = data.getStringExtra("vCountryCode");
            vPhoneCode = data.getStringExtra("vPhoneCode");
            vSImage = data.getStringExtra("vSImage");
        }
        // READ_CONTACTS  permission removed and add new popup for add contacts 05/09/2022
        /*else if (requestCode == PICK_CONTACT && data != null) {
            Uri uri = data.getData();

            if (uri != null) {
                Cursor c = null;

                try {
                    c = getContentResolver().query(uri, new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                            ContactsContract.CommonDataKinds.Phone.TYPE}, null, null, null);

                    if (c != null && c.moveToFirst()) {
                        String number = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        String name = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

                        addContact(name, number);
                    }
                } finally {
                    if (c != null) {
                        c.close();
                    }
                }
            }
        } */
    }

    public void AddEmergencyContacts() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActContext());

        LayoutInflater inflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.emergency_contaxct_layout, null);

        final String required_str = generalFunc.retrieveLangLBl("", "LBL_FEILD_REQUIRD");

        final ImageView contactimg = dialogView.findViewById(R.id.img);
        ImageView countryimage = dialogView.findViewById(R.id.countryimage);
        MaterialEditText countryBox = dialogView.findViewById(R.id.countryBox);
        int paddingValStart = (int) getResources().getDimension(R.dimen._35sdp);
        int paddingValEnd = (int) getResources().getDimension(R.dimen._12sdp);
        if (generalFunc.isRTLmode()) {
            countryBox.setPaddings(paddingValEnd, 0, paddingValStart, 0);
        } else {
            countryBox.setPaddings(paddingValStart, 0, paddingValEnd, 0);
        }

        contactimg.setImageResource(R.drawable.ic_contact_card);

        MaterialEditText nameBox = dialogView.findViewById(R.id.nameBox);

        MTextView submitTxt = dialogView.findViewById(R.id.submitTxt);
        MTextView cancelTxt = dialogView.findViewById(R.id.cancelTxt);
        MTextView subTitleTxt = dialogView.findViewById(R.id.subTitleTxt);
        MaterialEditText phoneBox = dialogView.findViewById(R.id.phoneBox);

        subTitleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_CONTACT_DETAILS_TXT"));
        submitTxt.setText(generalFunc.retrieveLangLBl("", "LBL_SUBMIT_BUTTON_TXT"));
        cancelTxt.setText(generalFunc.retrieveLangLBl("", "LBL_CANCEL_TXT"));

        nameBox.setFloatingLabelText(generalFunc.retrieveLangLBl("", "LBL_FULL_NAME"));
        nameBox.setHint(generalFunc.retrieveLangLBl("", "LBL_FULL_NAME"));
        nameBox.setInputType(InputType.TYPE_CLASS_TEXT);

        phoneBox.setFloatingLabelText(generalFunc.retrieveLangLBl("", "LBL_MOBILE_NUMBER_HEADER_TXT"));
        phoneBox.setHint(generalFunc.retrieveLangLBl("", "LBL_MOBILE_NUMBER_HEADER_TXT"));
        phoneBox.setInputType(InputType.TYPE_CLASS_PHONE);
        phoneBox.setImeOptions(EditorInfo.IME_ACTION_DONE);
        //phoneBox.setHelperText(generalFunc.retrieveLangLBl("", "LBL_SIGN_IN_MOBILE_EMAIL_HELPER"));

        vSImage = generalFunc.retrieveValue(Utils.DefaultCountryImage);
        int imagewidth = (int) getResources().getDimension(R.dimen._30sdp);
        int imageheight = (int) getResources().getDimension(R.dimen._20sdp);
        String imgUrl = Utils.getResizeImgURL(getActContext(), vSImage, imagewidth, imageheight);
        Glide.with(getActContext()).load(/*GeneralFunctions.parseIntegerValue(0,*/ imgUrl/*)*/).into(countryimage);
        //new LoadImage.builder(LoadImage.bind(imgUrl), countryimage).build();


        countryBox.setOnClickListener(v -> {
            if (countryPicker != null) {
                countryPicker = null;
            }
            countryPicker = new CountryPicker.Builder(getActContext()).showingDialCode(true)
                    .setLocale(locale).showingFlag(true)
                    .enablingSearch(true)
                    .setCountrySelectionListener(country ->
                            setData(country.getCode(), country.getDialCode(),
                                    country.getFlagName(), countryimage, countryBox))
                    .build();
            countryPicker.show(getActContext());
        });

        countryBox.setOnTouchListener(new setOnTouchList());

        if (!generalFunc.getJsonValue("vPhoneCode", obj_userProfile).equals("")) {
            vPhoneCode = generalFunc.getJsonValueStr("vPhoneCode", obj_userProfile);
            vCountryCode = generalFunc.getJsonValueStr("vCountry", obj_userProfile);
            countryBox.setText("+" + generalFunc.convertNumberWithRTL(vPhoneCode));
        }

        if (generalFunc.getJsonValue("vSCountryImage", obj_userProfile) != null && !generalFunc.getJsonValueStr("vSCountryImage", obj_userProfile).equalsIgnoreCase("")) {
            vSImage = generalFunc.getJsonValueStr("vSCountryImage", obj_userProfile);
            imgUrl = Utils.getResizeImgURL(getActContext(), vSImage, imagewidth, imageheight);
            Glide.with(getActContext()).load(/*GeneralFunctions.parseIntegerValue(0,*/ imgUrl/*)*/).into(countryimage);
            //new LoadImage.builder(LoadImage.bind(imgUrl), countryimage).build();
        }

        builder.setView(dialogView);

        cancelTxt.setOnClickListener(v -> alertDialog.dismiss());
        submitTxt.setOnClickListener(v -> {

            boolean mobileEntered = Utils.checkText(phoneBox) ? true : Utils.setErrorFields(phoneBox, required_str);
            boolean NameEntered = Utils.checkText(nameBox) ? true : Utils.setErrorFields(nameBox, required_str);

            if (mobileEntered) {
                mobileEntered = phoneBox.length() >= 3 ? true : Utils.setErrorFields(phoneBox, generalFunc.retrieveLangLBl("", "LBL_INVALID_MOBILE_NO"));
            }

            if (!NameEntered || !mobileEntered) {
                return;
            } else {
                alertDialog.dismiss();
                addContact(Utils.getText(nameBox), "+" + vPhoneCode + " " + Utils.getText(phoneBox));
            }

        });

        builder.setView(dialogView);
        alertDialog = builder.create();


        if (generalFunc.isRTLmode()) {
            generalFunc.forceRTLIfSupported(alertDialog);
        }

        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.getWindow().setBackgroundDrawable(getActContext().getResources().getDrawable(R.drawable.all_roundcurve_card_contact));
        alertDialog.show();
    }


    public void setData(String vCountryCode, String vPhoneCode, String vSImage, ImageView countryimage
            , MaterialEditText countryBox) {
        this.vCountryCode = vCountryCode;
        this.vPhoneCode = vPhoneCode;
        this.vSImage = vSImage;

        runOnUiThread(() -> {
            //new LoadImage.builder(LoadImage.bind(vSImage), countryimage).build();
            Glide.with(getActContext()).load(vSImage/*GeneralFunctions.parseIntegerValue(0, vSImage)*/).into(countryimage);
            countryBox.setText("+" + generalFunc.convertNumberWithRTL(vPhoneCode));
        });

    }

    public class setOnTouchList implements View.OnTouchListener {

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP && !view.hasFocus()) {
                view.performClick();
            }
            return true;
        }
    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            int i = view.getId();
            Utils.hideKeyboard(getActContext());
            if (i == R.id.backImgView) {
                EmergencyContactActivity.super.onBackPressed();
            } else if (i == btn_type2.getId()) {
                //checkPermission();
                AddEmergencyContacts();
            }
        }
    }

}
