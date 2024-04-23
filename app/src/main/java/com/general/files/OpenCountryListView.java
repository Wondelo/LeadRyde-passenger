package com.general.files;

import android.content.Context;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.adapter.files.PinnedSectionListAdapter;
import com.leadryde.userapp.R;
import com.leadryde.userapp.SelectCountryActivity;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.utils.Utils;
import com.view.ErrorView;
import com.view.MTextView;
import com.view.pinnedListView.CountryListItem;
import com.view.pinnedListView.PinnedSectionListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class OpenCountryListView implements PinnedSectionListAdapter.CountryClick, OnScrollTouchDelegate {

    Context mContext;

    ArrayList<CountryListItem> items_list;
    MTextView titleTxt;
    ImageView backImgView;
    GeneralFunctions generalFunc;
    ProgressBar loading;
    ErrorView errorView;
    MTextView noResTxt;
    PinnedSectionListView country_list;
    PinnedSectionListAdapter pinnedSectionListAdapter;
    private CountryListItem[] sections;
    ImageView searchImgView;
    LinearLayout searcharea;
    View toolbarArea;
    MTextView cancelTxt;
    EditText searchTxt;
    ImageView imageCancel;

    JSONArray countryArr;
    MTextView noData;

    public OpenCountryListView(Context mContext) {
        this.mContext = mContext;
        initView();
    }

    public void initView() {
        final BottomSheetDialog optionDailog = new BottomSheetDialog(mContext);

        View contentView = View.inflate(mContext, R.layout.activity_select_country, null);
        optionDailog.setContentView(contentView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                Utils.dpToPx(700, mContext)));
        BottomSheetBehavior mBehavior = BottomSheetBehavior.from((View) contentView.getParent());

        mBehavior.setPeekHeight(Utils.dpToPx(700, mContext));
        optionDailog.setCancelable(true);

        View bottomSheetView = optionDailog.getWindow().getDecorView().findViewById(R.id.design_bottom_sheet);
        bottomSheetView.setBackgroundColor(mContext.getResources().getColor(android.R.color.transparent));



        generalFunc = MyApp.getInstance().getGeneralFun(mContext);


        titleTxt = (MTextView) bottomSheetView.findViewById(R.id.titleTxt);
        backImgView = (ImageView) bottomSheetView.findViewById(R.id.backImgView);
        noResTxt = (MTextView) bottomSheetView.findViewById(R.id.noResTxt);
        noData = (MTextView)bottomSheetView. findViewById(R.id.noData);
        loading = (ProgressBar) bottomSheetView.findViewById(R.id.loading);
        errorView = (ErrorView) bottomSheetView.findViewById(R.id.errorView);
        country_list = (PinnedSectionListView) bottomSheetView.findViewById(R.id.country_list);
        country_list.onTouchDegateListener(this);
        searchImgView = (ImageView) bottomSheetView.findViewById(R.id.searchImgView);
        searcharea = (LinearLayout) bottomSheetView.findViewById(R.id.searcharea);
        toolbarArea = (View) bottomSheetView.findViewById(R.id.toolbarArea);
        cancelTxt = (MTextView) bottomSheetView.findViewById(R.id.cancelTxt);
        searchTxt = (EditText)bottomSheetView. findViewById(R.id.searchTxt);
        imageCancel = (ImageView) bottomSheetView.findViewById(R.id.imageCancel);
        searchImgView.setVisibility(View.GONE);
        searchImgView.setOnClickListener(new setOnClickList());
        cancelTxt.setOnClickListener(new setOnClickList());
        imageCancel.setOnClickListener(new setOnClickList());


        country_list.setShadowVisible(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            country_list.setFastScrollEnabled(true);
            country_list.setFastScrollAlwaysVisible(true);
        }
        items_list = new ArrayList<>();


        searchTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                filterCountries(charSequence.toString());

            }

            @Override
            public void afterTextChanged(Editable editable) {

                if (searchTxt.length() == 0) {
                    imageCancel.setVisibility(View.INVISIBLE);
                } else {
                    imageCancel.setVisibility(View.VISIBLE);
                }
            }
        });


        setLabels();

        backImgView.setOnClickListener(new setOnClickList());
        getCountryList();
        imageCancel.setVisibility(View.INVISIBLE);
        searcharea.setVisibility(View.VISIBLE);
        toolbarArea.setVisibility(View.GONE);








        try {
            if (!optionDailog.isShowing()) {
                optionDailog.show();
            }
        } catch (Exception e) {

        }

    }

    @Override
    public void countryClickList(CountryListItem countryListItem) {

    }

    @Override
    public void onTouchDelegate() {

    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            Utils.hideKeyboard(mContext);
            switch (view.getId()) {

                case R.id.cancelTxt:
                    toolbarArea.setVisibility(View.VISIBLE);
                    searcharea.setVisibility(View.GONE);
                    searchTxt.setText("");
//                    filterCountries("");
                    Utils.hideKeyboard(mContext);
                    break;
                case R.id.searchImgView:
                    if (countryArr != null) {
                        imageCancel.setVisibility(View.INVISIBLE);
                        searcharea.setVisibility(View.VISIBLE);
                        toolbarArea.setVisibility(View.GONE);

                        searchTxt.requestFocus();
                        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm != null) {
                            imm.showSoftInput(searchTxt, InputMethodManager.SHOW_IMPLICIT);
                        }
                    }
                    break;
                case R.id.imageCancel:
                    searchTxt.setText("");
                    break;
            }
        }
    }
    private void filterCountries(String searchText) {
        int totalSection = 0;
        ArrayList<CountryListItem> items_list_tmp = new ArrayList<>();


        int sectionPosition = 0, listPosition = 0;

        HashMap<Integer, CountryListItem> sectionMapData = new HashMap<>();
        int imagewidth = (int) mContext.getResources().getDimension(R.dimen._30sdp);

        if (countryArr == null) {
            return;
        }

        for (int i = 0; i < countryArr.length(); i++) {
            JSONObject tempJson = generalFunc.getJsonObject(countryArr, i);

            String key_str = generalFunc.getJsonValueStr("key", tempJson);
            String count_str = generalFunc.getJsonValueStr("TotalCount", tempJson);

            JSONArray subListArr = generalFunc.getJsonArray("List", tempJson);


            ArrayList<CountryListItem> items_list_sub_tmp = new ArrayList<>();

            for (int j = 0; j < subListArr.length(); j++) {
                JSONObject subTempJson = generalFunc.getJsonObject(subListArr, j);
                String vCountryCode = generalFunc.getJsonValueStr("vCountryCode", subTempJson);
                String vPhoneCode = generalFunc.getJsonValueStr("vPhoneCode", subTempJson);
                String iCountryId = generalFunc.getJsonValueStr("iCountryId", subTempJson);
                String vCountry = generalFunc.getJsonValueStr("vCountry", subTempJson);
                String vRImage = generalFunc.getJsonValueStr("vRImage", subTempJson);
                String vSImage = generalFunc.getJsonValueStr("vSImage", subTempJson);

                String searchTxt = searchText.trim().toLowerCase(Locale.US);
                String countryTxt = vCountry.toLowerCase(Locale.US);

                if (searchText.trim().equals("") || countryTxt.startsWith(searchTxt)) {
                    CountryListItem countryListItem = new CountryListItem(CountryListItem.ITEM, vCountry);
                    countryListItem.sectionPosition = sectionPosition;
                    countryListItem.listPosition = listPosition++;
                    countryListItem.setvCountryCode(vCountryCode);
                    countryListItem.setvPhoneCode(vPhoneCode);
                    countryListItem.setiCountryId(iCountryId);
                    countryListItem.setvRImage(Utils.getResizeImgURL(mContext, vRImage, imagewidth, imagewidth));
                    countryListItem.setvSImage(vSImage);
                    items_list_sub_tmp.add(countryListItem);
                }


            }

            if (items_list_sub_tmp.size() > 0) {
                CountryListItem section = new CountryListItem(CountryListItem.SECTION, key_str);
                section.sectionPosition = sectionPosition;
                section.listPosition = listPosition++;
//                section.CountSubItems = generalFunc.parseIntegerValue(0, count_str);
                section.CountSubItems = items_list_sub_tmp.size();
//                onSectionAdded(section, sectionPosition);
                sectionMapData.put(sectionPosition, section);

                items_list_tmp.add(section);

                totalSection = totalSection + 1;

                items_list_tmp.addAll(items_list_sub_tmp);


                sectionPosition++;
            }
        }

        sections = new CountryListItem[totalSection];

        for (Integer currentKey : sectionMapData.keySet()) {
            onSectionAdded(sectionMapData.get(currentKey), currentKey);
        }

        items_list.clear();

        items_list.addAll(items_list_tmp);

        pinnedSectionListAdapter = new PinnedSectionListAdapter(mContext, items_list, sections);
        country_list.setAdapter(pinnedSectionListAdapter);

        pinnedSectionListAdapter.setCountryClickListener(this);

        pinnedSectionListAdapter.notifyDataSetChanged();
    }
    public void setLabels() {
        titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_SELECT_CONTRY"));
        searchTxt.setHint(generalFunc.retrieveLangLBl("Search Country", "LBL_SEARCH_COUNTRY"));
        cancelTxt.setText(generalFunc.retrieveLangLBl("", "LBL_CANCEL_TXT"));
    }
    public void getCountryList() {
        if (errorView.getVisibility() == View.VISIBLE) {
            errorView.setVisibility(View.GONE);
        }
        if (loading.getVisibility() != View.VISIBLE) {
            loading.setVisibility(View.VISIBLE);
        }

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "countryList");

        noResTxt.setVisibility(View.GONE);

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(mContext, parameters);
        exeWebServer.setDataResponseListener(responseString -> {

            noResTxt.setVisibility(View.GONE);

            if (responseString != null && !responseString.equals("")) {

                closeLoader();

                if (generalFunc.checkDataAvail(Utils.action_str, responseString) == true) {



                    JSONArray countryArr = generalFunc.getJsonArray("CountryList", responseString);

                    this.countryArr = countryArr;

                    items_list.clear();

                    sections = new CountryListItem[generalFunc.parseIntegerValue(0, generalFunc.getJsonValue("totalValues", responseString))];
                    pinnedSectionListAdapter = new PinnedSectionListAdapter(mContext, items_list, sections);
                    country_list.setAdapter(pinnedSectionListAdapter);

                    pinnedSectionListAdapter.setCountryClickListener(this);
                    items_list.clear();
                    pinnedSectionListAdapter.notifyDataSetChanged();

                    int sectionPosition = 0, listPosition = 0;
                    int imagewidth = (int) mContext.getResources().getDimension(R.dimen._30sdp);

                    for (int i = 0; i < countryArr.length(); i++) {
                        JSONObject tempJson = generalFunc.getJsonObject(countryArr, i);

                        String key_str = generalFunc.getJsonValueStr("key", tempJson);
                        String count_str = generalFunc.getJsonValueStr("TotalCount", tempJson);


                        CountryListItem section = new CountryListItem(CountryListItem.SECTION, key_str);
                        section.sectionPosition = sectionPosition;
                        section.listPosition = listPosition++;
                        section.CountSubItems = generalFunc.parseIntegerValue(0, count_str);
                        onSectionAdded(section, sectionPosition);
                        items_list.add(section);

                        JSONArray subListArr = generalFunc.getJsonArray("List", tempJson);

                        for (int j = 0; j < subListArr.length(); j++) {
                            JSONObject subTempJson = generalFunc.getJsonObject(subListArr, j);
                            String vCountryCode = generalFunc.getJsonValueStr("vCountryCode", subTempJson);
                            String vPhoneCode = generalFunc.getJsonValueStr("vPhoneCode", subTempJson);
                            String iCountryId = generalFunc.getJsonValueStr("iCountryId", subTempJson);
                            String vCountry = generalFunc.getJsonValueStr("vCountry", subTempJson);
                            String vRImage = generalFunc.getJsonValueStr("vRImage", subTempJson);
                            String vSImage = generalFunc.getJsonValueStr("vSImage", subTempJson);

                            CountryListItem countryListItem = new CountryListItem(CountryListItem.ITEM, vCountry);
                            countryListItem.sectionPosition = sectionPosition;
                            countryListItem.listPosition = listPosition++;
                            countryListItem.setvCountryCode(vCountryCode);
                            countryListItem.setvPhoneCode(vPhoneCode);
                            countryListItem.setiCountryId(iCountryId);
                            countryListItem.setvRImage(Utils.getResizeImgURL(mContext, vRImage, imagewidth, imagewidth));
                            countryListItem.setvSImage(vSImage);
                            items_list.add(countryListItem);
                        }

                        sectionPosition++;
                    }
                    searchImgView.setEnabled(true);
                    if (countryArr == null || countryArr.length() == 0) {
                        searchImgView.setEnabled(false);
                        noData.setText(generalFunc.retrieveLangLBl("", "LBL_NO_COUNTRY_AVAIL"));
                        noData.setVisibility(View.VISIBLE);
                    } else {
                        noData.setVisibility(View.GONE);
                    }
                    pinnedSectionListAdapter.notifyDataSetChanged();
                } else {
                    noResTxt.setText(generalFunc.retrieveLangLBl("", "LBL_ERROR_TXT"));
                    noResTxt.setVisibility(View.VISIBLE);
                }
            } else {
                generateErrorView();
            }
        });
        exeWebServer.execute();
    }
    public void closeLoader() {
        if (loading.getVisibility() == View.VISIBLE) {
            loading.setVisibility(View.GONE);
        }
    }
    protected void onSectionAdded(CountryListItem section, int sectionPosition) {
        sections[sectionPosition] = section;
    }
    public void generateErrorView() {

        closeLoader();

        generalFunc.generateErrorView(errorView, "LBL_ERROR_TXT", "LBL_NO_INTERNET_TXT");

        if (errorView.getVisibility() != View.VISIBLE) {
            errorView.setVisibility(View.VISIBLE);
        }
        errorView.setOnRetryListener(() -> getCountryList());
    }
}