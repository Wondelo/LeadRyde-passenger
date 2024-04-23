package com.leadryde.userapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.ViewPagerCards.RideDeliveryCardPagerAdapter;
import com.adapter.files.RideDeliveryCategoryAdapter;
import com.ethanhua.skeleton.Skeleton;
import com.ethanhua.skeleton.SkeletonScreen;
import com.fragments.MyBookingFragment;
import com.fragments.MyProfileFragment;
import com.fragments.MyWalletFragment;
import com.general.files.AddBottomBar;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.GetAddressFromLocation;
import com.general.files.GetLocationUpdates;
import com.general.files.LoadAvailableCab;
import com.general.files.MyApp;
import com.general.files.OpenCatType;
import com.general.files.StartActProcess;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.model.Stop_Over_Points_Data;
import com.utils.Utils;
import com.view.GenerateAlertBox;
import com.view.LoopingCirclePageIndicator;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;
import com.view.SelectableRoundedImageView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class RideDeliveryActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener, RideDeliveryCardPagerAdapter.OnItemClickList, RideDeliveryCategoryAdapter.OnItemClickList, OnMapReadyCallback
        , GetLocationUpdates.LocationUpdates, GetAddressFromLocation.AddressFound, GoogleMap.OnCameraMoveStartedListener, GoogleMap.OnCameraIdleListener {


    GeneralFunctions generalFunc;
    ViewPager bannerViewPager;
    LoopingCirclePageIndicator bannerCirclePageIndicator;
    // private ShadowTransformer mCardShadowTransformer;
    private RideDeliveryCardPagerAdapter mCardAdapter;
    ArrayList<HashMap<String, String>> imagesList;
    ArrayList<HashMap<String, String>> itemList;
    RecyclerView dataListRecyclerView;
    RideDeliveryCategoryAdapter rideDeliveryCategoryAdapter;


    AddBottomBar addBottomBar;
    FrameLayout container;
    String userProfileJson = "";
    MyProfileFragment myProfileFragment;
    MyWalletFragment myWalletFragment;
    public MyBookingFragment myBookingFragment;

    private static final int SEL_CARD = 004;
    public static final int TRANSFER_MONEY = 87;
    public boolean iswallet = false;
    MTextView whereTxt, aroundTxt;
    MTextView homePlaceTxt, homePlaceHTxt;
    MTextView workPlaceTxt, workPlaceHTxt;
    LinearLayout homeLocArea, workLocArea;
    ImageView homeActionImgView, workActionImgView;
    private static final int RC_SIGN_IN_UP = 007;
    static LinearLayout MainLayout, bottomMenuArea, homeWorkArea;
    ProgressBar mProgressBar;

    boolean isRide = false;
    SupportMapFragment map;
    GoogleMap gMap;
    GetLocationUpdates getLastLocation;
    boolean isFirstLocation = true;
    boolean isFirstLocationUpdate = true;
    public Location userLocation;
    LoadAvailableCab loadAvailCabs;
    private SkeletonScreen skeletonScreen;
    public ArrayList<HashMap<String, String>> listOfDrivers;
    ArrayList<Marker> driverMarkerList = new ArrayList<>();
    ImageView backImgView;
    ImageView prefBtnImageView, userLocBtnImgView;
    ImageView headerLogo;
    Toolbar toolbar;
    int currentPage = 0;
    Timer timer;
    final long DELAY_MS = 500;//delay in milliseconds before task is to be executed
    final long PERIOD_MS = 7500; // time in milliseconds between successive task executions.

    ImageView pinImgView;
    LinearLayout locArea;
    MTextView placeTxtView, placeTxtViewTitle;
    boolean isPlaceSelected = false;
    LatLng placeLocation;
    String pickUpLocationAddress = "";
    Marker placeMarker;
    private RideDeliveryActivity listener;
    GetAddressFromLocation getAddressFromLocation;
    public ArrayList<Stop_Over_Points_Data> stopOverPointsList = new ArrayList<>();

    androidx.appcompat.app.AlertDialog pref_dialog;
    public boolean ishandicap = false;
    public boolean isChildSeat = false;
    public boolean isWheelChair = false;
    public boolean isfemale = false;
    Handler handler = new Handler();
    Runnable runnable;
    int delay = 60*1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ride_delivery);


        generalFunc = new GeneralFunctions(getActContext());
        userProfileJson = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);
        initView();
        getDetails();
        addBottomBar = new AddBottomBar(getActContext(), generalFunc.getJsonObject(userProfileJson));
    }


    public void initView() {
        map = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapV2);
        getAddressFromLocation = new GetAddressFromLocation(getActContext(), generalFunc);
        getAddressFromLocation.setAddressList(this);
        map.getMapAsync(RideDeliveryActivity.this);

        pinImgView = (ImageView) findViewById(R.id.pinImgView);
        placeTxtView = (MTextView) findViewById(R.id.placeTxtView);
        placeTxtViewTitle = (MTextView) findViewById(R.id.placeTxtViewTitle);
        locArea = (LinearLayout) findViewById(R.id.loc_area);

        backImgView = (ImageView) findViewById(R.id.backImgView);
        headerLogo = (ImageView) findViewById(R.id.headerLogo);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        backImgView.setVisibility(View.GONE);
        headerLogo.setPadding(0, 0, 0, 0);
        headerLogo.setVisibility(View.VISIBLE);
        container = (FrameLayout) findViewById(R.id.container);
        whereTxt = (MTextView) findViewById(R.id.whereTxt);
        aroundTxt = (MTextView) findViewById(R.id.aroundTxt);
        homePlaceTxt = (MTextView) findViewById(R.id.homePlaceTxt);
        homePlaceHTxt = (MTextView) findViewById(R.id.homePlaceHTxt);
        workPlaceTxt = (MTextView) findViewById(R.id.workPlaceTxt);
        workPlaceHTxt = (MTextView) findViewById(R.id.workPlaceHTxt);
        homeLocArea = (LinearLayout) findViewById(R.id.homeLocArea);
        workLocArea = (LinearLayout) findViewById(R.id.workLocArea);
        MainLayout = (LinearLayout) findViewById(R.id.MainLayout);
        bottomMenuArea = (LinearLayout) findViewById(R.id.bottomMenuArea);
        homeWorkArea = (LinearLayout) findViewById(R.id.homeWorkArea);
        bannerViewPager = (ViewPager) findViewById(R.id.bannerViewPager);
        bannerCirclePageIndicator = findViewById(R.id.bannerCirclePageIndicator);
        homeActionImgView = (ImageView) findViewById(R.id.homeActionImgView);
        workActionImgView = (ImageView) findViewById(R.id.workActionImgView);
        mProgressBar = (ProgressBar) findViewById(R.id.mProgressBar);
        homeLocArea.setOnClickListener(new setOnClickList());
        workLocArea.setOnClickListener(new setOnClickList());
        //placeTxtView.setOnClickListener(new setOnClickList());
        locArea.setOnClickListener(new setOnClickList());
        whereTxt.setOnClickListener(new setOnClickList());
        homeActionImgView.setOnClickListener(new setOnClickList());
        workActionImgView.setOnClickListener(new setOnClickList());

        bannerViewPager.addOnPageChangeListener(this);
        dataListRecyclerView = (RecyclerView) findViewById(R.id.dataListRecyclerView);
        //  dataListRecyclerView.setHasFixedSize(true);
        prefBtnImageView = (ImageView) findViewById(R.id.prefBtnImageView);
        userLocBtnImgView = (ImageView) findViewById(R.id.userLocBtnImgView);
        prefBtnImageView.setOnClickListener(new setOnClickList());
        userLocBtnImgView.setOnClickListener(new setOnClickList());

        whereTxt.setText(generalFunc.retrieveLangLBl("", "LBL_PICKUP_FROM_TXT"));
        aroundTxt.setText(generalFunc.retrieveLangLBl("Around You", "LBL_AROUND_YOU"));
        //placeTxtViewTitle.setText(generalFunc.retrieveLangLBl("Pick Up", "LBL_PICKUP_TXT") + ":" + generalFunc.retrieveLangLBl("Your Location", "LBL_YOUR_LOCATION_TXT"));
        placeTxtViewTitle.setText(generalFunc.retrieveLangLBl("Edit Your Pickup", "LBL_EDIT_YOUR_PICKUP"));
        //placeTxtView.setText(generalFunc.retrieveLangLBl("", "LBL_SEARCH_LOC"));
        checkPlaces();
        map.getMapAsync(RideDeliveryActivity.this);


        skeletonScreen = Skeleton.bind(MainLayout)
                .load(R.layout.ridedlivery_shimmer_view)
                .duration(1000)
                .color(R.color.shimmer_color)
                .angle(0)
                .show();

        MyHandler myHandler = new MyHandler(this);
        myHandler.sendEmptyMessageDelayed(1, 2000);

    }

    static WeakReference<RideDeliveryActivity> activityWeakReference;

    @Override
    public void onAddressFound(String address, double latitude, double longitude, String geocodeobject) {
        placeTxtView.setText(address);
        pickUpLocationAddress = address;
        //locArea.setVisibility(View.VISIBLE);
        isPlaceSelected = true;
        this.placeLocation = new LatLng(latitude, longitude);

        CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(this.placeLocation, 14.0f);

        if (gMap != null) {
            gMap.clear();
            if (isFirstLocation) {
                gMap.moveCamera(cu);
            }
            isFirstLocation = false;

            //this.gMap.setOnCameraMoveStartedListener(this);
            //this.gMap.setOnCameraIdleListener(this);
            setGoogleMapCameraListener(this);
        }

        filterDrivers(false);
        getOnlineDriversRideDelivery("Ride");
    }

    public void setGoogleMapCameraListener(RideDeliveryActivity act) {
        listener = act;
        this.gMap.setOnCameraMoveStartedListener(act);
        this.gMap.setOnCameraIdleListener(act);

    }

    @Override
    public void onCameraMoveStarted(int i) {
        //if (pinImgView.getVisibility() == View.VISIBLE) {
        //if (!isAddressEnable) {
        locArea.setVisibility(View.GONE);
        //placeTxtView.setText(generalFunc.retrieveLangLBl("", "LBL_SELECTING_LOCATION_TXT"));
        //}
        //}

    }

    @Override
    public void onCameraIdle() {
        if (getAddressFromLocation == null) {
            return;
        }

        /*if (pinImgView.getVisibility() == View.GONE) {
            return;
        }*/

        LatLng center = null;
        if (gMap != null && gMap.getCameraPosition() != null) {
            center = gMap.getCameraPosition().target;
        }

        if (center == null) {
            return;
        }
        setGoogleMapCameraListener(null);
        if (isFirstLocationUpdate) {
            isFirstLocationUpdate = false;
            getAddressFromLocation.setLocation(center.latitude, center.longitude);
            getAddressFromLocation.setLoaderEnable(true);
            getAddressFromLocation.execute();
        }
    }

    public static class MyHandler extends android.os.Handler {


        MyHandler(RideDeliveryActivity activity) {
            activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (activityWeakReference.get() != null) {
                activityWeakReference.get().skeletonScreen.hide();

            }
        }
    }

    private void setCameraPosition(LatLng location) {
        CameraPosition cameraPosition = new CameraPosition.Builder().target(
                        new LatLng(location.latitude,
                                location.longitude))
                .zoom(Utils.defaultZomLevel).build();
        gMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

    }

    private LatLng getLocationLatLng(boolean setText) {
        LatLng placeLocation = null;


        String isPickUpLoc = getIntent().getStringExtra("isPickUpLoc");

        if (isPickUpLoc != null && isPickUpLoc.equals("true")) {
            if (getIntent().hasExtra("PickUpLatitude") && getIntent().hasExtra("PickUpLongitude")) {

                //isAddressEnable = true;
                placeLocation = new LatLng(generalFunc.parseDoubleValue(0.0, getIntent().getStringExtra("PickUpLatitude")),
                        generalFunc.parseDoubleValue(0.0, getIntent().getStringExtra("PickUpLongitude")));

            }

            if (setText && getIntent().hasExtra("PickUpAddress")) {
                //pinImgView.setVisibility(View.VISIBLE);
                isPlaceSelected = true;
                placeTxtView.setText("" + getIntent().getStringExtra("PickUpAddress"));
            }

        } else if (getIntent().getStringExtra("isDestLoc") != null && getIntent().hasExtra("DestLatitude") && getIntent().hasExtra("DestLongitude")) {

            //isAddressEnable = true;
            placeLocation = new LatLng(generalFunc.parseDoubleValue(0.0, getIntent().getStringExtra("DestLatitude")),
                    generalFunc.parseDoubleValue(0.0, getIntent().getStringExtra("DestLongitude")));

            if (setText && getIntent().hasExtra("DestAddress")) {
                //pinImgView.setVisibility(View.VISIBLE);
                isPlaceSelected = true;
                placeTxtView.setText("" + getIntent().getStringExtra("DestAddress"));
            }

        } else if (userLocation != null) {
            placeLocation = new LatLng(generalFunc.parseDoubleValue(0.0, "" + userLocation.getLatitude()),
                    generalFunc.parseDoubleValue(0.0, "" + userLocation.getLongitude()));

        } else {


            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            locationManager = (LocationManager) getSystemService
                    (Context.LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return placeLocation;
            }
            Location getLastLocation = locationManager.getLastKnownLocation
                    (LocationManager.PASSIVE_PROVIDER);
            if (getLastLocation != null) {
                LatLng UserLocation = new LatLng(generalFunc.parseDoubleValue(0.0, "" + getLastLocation.getLatitude()),
                        generalFunc.parseDoubleValue(0.0, "" + getLastLocation.getLongitude()));
                if (UserLocation != null) {
                    placeLocation = UserLocation;
                }
            }
        }
        return placeLocation;
    }


    public GoogleMap getMap() {
        return this.gMap;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        this.gMap = googleMap;
        setGoogleMapCameraListener(null);

        gMap.getUiSettings().setAllGesturesEnabled(true);
        if (generalFunc.checkLocationPermission(true)) {
            getMap().setMyLocationEnabled(true);
            //  getMap().setPadding(0, 0, 0, Utils.dipToPixels(getActContext(), 50));
            getMap().getUiSettings().setTiltGesturesEnabled(false);
            getMap().getUiSettings().setZoomControlsEnabled(false);
            getMap().getUiSettings().setCompassEnabled(false);
            getMap().getUiSettings().setMyLocationButtonEnabled(true);
            View view = map.getView().findViewWithTag("GoogleMapMyLocationButton");
            view.setVisibility(View.INVISIBLE);

            /*View view = map.getView().findViewWithTag("GoogleMapMyLocationButton");
            ImageView locationButton = (ImageView) map.getView().findViewWithTag("GoogleMapMyLocationButton");
            locationButton.setImageResource(R.drawable.ic_cordinate);
            locationButton.setBackgroundResource(R.drawable.circle_shadow_big);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(Utils.dipToPixels(getActContext(), 45), Utils.dipToPixels(getActContext(), 45));

            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE);
            layoutParams.addRule(RelativeLayout.ALIGN_BOTTOM);
            layoutParams.setMargins(Utils.dipToPixels(getActContext(), 40),Utils.dipToPixels(getActContext(), 40),Utils.dipToPixels(getActContext(), 55),Utils.dipToPixels(getActContext(), 10 ));

            view.setLayoutParams(layoutParams);*/
        }


        getMap().setOnMarkerClickListener(marker -> {
            marker.hideInfoWindow();
            return true;
        });

        if (getLastLocation != null) {
            getLastLocation.stopLocationUpdates();
            getLastLocation = null;
        }

        GetLocationUpdates.locationResolutionAsked = false;
        getLastLocation = new GetLocationUpdates(getActContext(), Utils.LOCATION_UPDATE_MIN_DISTANCE_IN_MITERS, true, this);


        gMap.getUiSettings().setCompassEnabled(false);
    }

    @Override
    public void onLocationUpdate(Location location) {
        if (location == null) {
            return;
        }

        this.userLocation = location;


        if (isFirstLocation == true) {
            placeLocation = getLocationLatLng(true);
            if (/*isAddressEnable &&*/ listener == null) {
                setGoogleMapCameraListener(this);
            }
            if (placeLocation != null) {
                setCameraPosition(new LatLng(placeLocation.latitude, placeLocation.longitude));
            } else {
                setCameraPosition(new LatLng(location.getLatitude(), location.getLongitude()));
            }

            //pinImgView.setVisibility(View.VISIBLE);
            isFirstLocation = false;

            double currentZoomLevel = Utils.defaultZomLevel;

            CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(this.userLocation.getLatitude(), this.userLocation.getLongitude()))
                    .zoom((float) currentZoomLevel).build();

            if (cameraPosition != null && getMap() != null) {
                getMap().moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }


            isFirstLocation = false;
            if (itemList != null) {
                getOnlineDriversRideDelivery(itemList.get(0).get("eCatType"));
            } else {
                getOnlineDriversRideDelivery("Ride");
            }
        }

    }


    public void initializeLoadCab() {


        loadAvailCabs = new LoadAvailableCab(getActContext(), generalFunc, "1", userLocation,
                getMap(), userProfileJson);

        // loadAvailCabs.pickUpAddress = pickUpLocationAddress;
        // loadAvailCabs.currentGeoCodeResult = currentGeoCodeObject;
        loadAvailCabs.checkAvailableCabs();
    }

    public class setOnClickList implements View.OnClickListener {

        @SuppressLint("MissingPermission")
        @Override
        public void onClick(View view) {
            int i = view.getId();

            Bundle bndl = new Bundle();

            if (i == R.id.homeLocArea) {


                HashMap<String, String> data = new HashMap<>();
                data.put("userHomeLocationAddress", "");
                data.put("userHomeLocationLatitude", "");
                data.put("userHomeLocationLongitude", "");
                data = GeneralFunctions.retrieveValue(data, getActContext());

                final String home_address_str = data.get("userHomeLocationAddress");
                final String home_addr_latitude = data.get("userHomeLocationLatitude");
                final String home_addr_longitude = data.get("userHomeLocationLongitude");

                if (home_address_str != null && !home_address_str.equalsIgnoreCase("")) {
                    HashMap<String, String> hasmpObj = itemList.get(0);
//                    hasmpObj.put("latitude", home_addr_latitude);
//                    hasmpObj.put("longitude", home_addr_longitude);
//                    hasmpObj.put("address", home_address_str);
                    hasmpObj.put("isHome", "Yes");


                    (new OpenCatType(getActContext(), hasmpObj)).execute();

                }


            } else if (i == R.id.workLocArea) {

                HashMap<String, String> data = new HashMap<>();
                data.put("userWorkLocationAddress", "");
                data.put("userWorkLocationLatitude", "");
                data.put("userWorkLocationLongitude", "");
                data = generalFunc.retrieveValue(data);


                String work_address_str = data.get("userWorkLocationAddress");
                String work_addr_latitude = data.get("userWorkLocationLatitude");
                String work_addr_longitude = data.get("userWorkLocationLongitude");

                if (work_address_str != null && !work_address_str.equalsIgnoreCase("")) {
                    HashMap<String, String> hasmpObj = itemList.get(0);
                    hasmpObj.put("isWork", "Yes");
                    (new OpenCatType(getActContext(), hasmpObj)).execute();

                }
            } else if (i == R.id.homeActionImgView) {
                if (getIntent().hasExtra("eSystem") && !Utils.checkText(generalFunc.getMemberId())) {
                    Bundle bn = new Bundle();
                    bn.putBoolean("isRestart", false);
                    new StartActProcess(getActContext()).startActForResult(RideDeliveryActivity.class, bn, RC_SIGN_IN_UP);
                    return;
                }

                Bundle bn = new Bundle();
                bn.putString("isHome", "true");


                new StartActProcess(getActContext()).startActForResult(SearchPickupLocationActivity.class,
                        bn, Utils.ADD_HOME_LOC_REQ_CODE);

            } else if (i == R.id.workActionImgView) {
                if (getIntent().hasExtra("eSystem") && !Utils.checkText(generalFunc.getMemberId())) {
                    Bundle bn = new Bundle();
                    bn.putBoolean("isRestart", false);
                    new StartActProcess(getActContext()).startActForResult(RideDeliveryActivity.class, bn, RC_SIGN_IN_UP);
                    return;
                }

                Bundle bn = new Bundle();
                bn.putString("isWork", "true");


                new StartActProcess(getActContext()).startActForResult(SearchPickupLocationActivity.class,
                        bn, Utils.ADD_WORK_LOC_REQ_CODE);

            } /*else if (i == R.id.loc_area || i == R.id.whereTxt) {
                Bundle bn = new Bundle();
                bn.putString("selType", Utils.CabGeneralType_Ride);
                bn.putBoolean("isRestart", false);
                bn.putBoolean("isAddressAdded", true);
                bn.putDouble("pic_lat", placeLocation.latitude);
                bn.putDouble("pic_long", placeLocation.longitude);
                bn.putString("pic_address",  pickUpLocationAddress);
                bn.putBoolean("isFromHome", true);
                new StartActProcess(getActContext()).startActWithData(MainActivity.class, bn);

            } */ else if (i == R.id.placeTxtView) {
                Bundle bn = new Bundle();

                bn.putString("selType", Utils.CabGeneralType_Ride);
                bn.putBoolean("isRestart", false);
                bn.putBoolean("isAddressAdded", true);

                bn.putDouble("pic_lat", placeLocation.latitude);
                bn.putDouble("pic_long", placeLocation.longitude);
                bn.putString("pic_address", pickUpLocationAddress);
                bn.putBoolean("isFromHome", true);
                new StartActProcess(getActContext()).startActWithData(MainActivity.class, bn);

                /*bn.putString("locationArea", "dest");
                bn.putBoolean("isDriverAssigned", false);
                bn.putDouble("lat", placeLocation.latitude);
                bn.putDouble("long", placeLocation.longitude);
                bn.putString("address", pickUpLocationAddress);
                bn.putString("type", Utils.CabGeneralType_Ride);
                Stop_Over_Points_Data stop_over_points_data = new Stop_Over_Points_Data();
                stop_over_points_data.setDestAddress(pickUpLocationAddress);
                stop_over_points_data.setDestLat(placeLocation.latitude);
                stop_over_points_data.setDestLong(placeLocation.longitude);
                stop_over_points_data.setDestLatLong(new LatLng(placeLocation.latitude, placeLocation.longitude));
                stop_over_points_data.setHintLable(generalFunc.retrieveLangLBl("", "LBL_PICK_UP_FROM"));
                stop_over_points_data.setAddressAdded(true);
                stop_over_points_data.setDestination(true);
                stop_over_points_data.setRemovable(true);
                stopOverPointsList.clear();
                stopOverPointsList.add(stop_over_points_data);
                //if (isMultiStopOverEnabled()) {
                Gson gson = new Gson();
                String json = gson.toJson(stopOverPointsList);
                bn.putString("stopOverPointsList", json);
                bn.putBoolean("isFromHome", true);
                    //bn.putString("iscubejekRental", "" + iscubejekRental);
                    //bn.putString("isRental", "" + isRental);
                //}
                new StartActProcess(getActContext()).startActForResult(SearchLocationActivity.class, bn, Utils.SEARCH_DEST_LOC_REQ_CODE);*/

/*                LatLng placeLocation = getLocationLatLng(false);
                Bundle bn = new Bundle();
                if (getIntent().hasExtra("locationArea")) {
                    bn.putString("locationArea", getIntent().getStringExtra("locationArea"));
                } else {
                    bn.putString("locationArea", "");
                }
                bn.putString("hideSetMapLoc", "");
                if (placeLocation != null) {
                    bn.putDouble("lat", placeLocation.latitude);
                    bn.putDouble("long", placeLocation.longitude);
                } else {
                    bn.putDouble("lat", 0.0);
                    bn.putDouble("long", 0.0);
                    bn.putString("address", "");
                }
                bn.putBoolean("isPlaceAreaShow", false);
                if (getIntent().hasExtra("isFromMulti")) {
                    bn.putBoolean("isFromMulti", true);
                    bn.putInt("pos", getIntent().getIntExtra("pos", -1));
                }
                if (getIntent().hasExtra("isFromStopOver")) {
                    bn.putInt("pos", getIntent().getIntExtra("pos", -1));
                }
                if (getIntent().hasExtra("stopOverPointsList")) {
                    bn.putSerializable("stopOverPointsList", getIntent().getSerializableExtra("stopOverPointsList"));
                }
                new StartActProcess(getActContext()).startActForResult(SearchLocationActivity.class, bn, Utils.PLACE_CUSTOME_LOC_REQUEST_CODE);*/
            } else if (i == R.id.prefBtnImageView) {
                getUserProfileJson();
                openPrefrancedailog();
            } else if (i == R.id.userLocBtnImgView) {

                //getMap().setMyLocationEnabled(true);
                //getMap().getUiSettings().setMyLocationButtonEnabled(true);
                View views = map.getView().findViewWithTag("GoogleMapMyLocationButton");
                views.setVisibility(View.INVISIBLE);
                views.callOnClick();
                //moveToCurrentLoc();
            }


        }
    }


    private void getUserProfileJson() {
        userProfileJson = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);
        //obj_userProfile = generalFunc.getJsonObject(userProfileJson);
    }

    public void openPrefrancedailog() {

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getActContext());
        LayoutInflater inflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.activity_prefrance, null);
        final MTextView TitleTxt = (MTextView) dialogView.findViewById(R.id.TitleTxt);
        final MTextView noteText = (MTextView) dialogView.findViewById(R.id.noteText);
        noteText.setText(generalFunc.retrieveLangLBl("", "LBL_NOTE") + ": " + generalFunc.retrieveLangLBl("", "LBL_OPTION_FOR_FEMALE_USERS"));
        final CheckBox checkboxHandicap = (CheckBox) dialogView.findViewById(R.id.checkboxHandicap);
        final CheckBox checkboxChildseat = (CheckBox) dialogView.findViewById(R.id.checkboxChildseat);
        final CheckBox checkboxWheelChair = (CheckBox) dialogView.findViewById(R.id.checkboxWheelChair);
        final CheckBox checkboxFemale = (CheckBox) dialogView.findViewById(R.id.checkboxFemale);
        final ImageView cancelImg = (ImageView) dialogView.findViewById(R.id.cancelImg);
        checkboxFemale.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (generalFunc.retrieveValue(Utils.FEMALE_RIDE_REQ_ENABLE).equalsIgnoreCase("Yes") && generalFunc.getJsonValue("eGender", userProfileJson).equals("") && generalFunc.retrieveValue("IS_RIDE_MODULE_AVAIL").equalsIgnoreCase("yes")) {
                    checkboxFemale.setChecked(false);
                    genderDailog();
                    return;
                }
            }
        });

        cancelImg.setOnClickListener(v -> pref_dialog.dismiss());

        if (generalFunc.retrieveValue(Utils.HANDICAP_ACCESSIBILITY_OPTION).equalsIgnoreCase("yes")) {
            checkboxHandicap.setVisibility(View.VISIBLE);
        } else {
            checkboxHandicap.setVisibility(View.GONE);
        }
        if (generalFunc.retrieveValue(Utils.CHILD_SEAT_ACCESSIBILITY_OPTION).equalsIgnoreCase("yes")) {
            checkboxChildseat.setVisibility(View.VISIBLE);
        } else {
            checkboxChildseat.setVisibility(View.GONE);
        }
        if (generalFunc.retrieveValue(Utils.WHEEL_CHAIR_ACCESSIBILITY_OPTION).equalsIgnoreCase("yes")) {
            checkboxWheelChair.setVisibility(View.VISIBLE);
        } else {
            checkboxWheelChair.setVisibility(View.GONE);
        }

        if (generalFunc.retrieveValue(Utils.FEMALE_RIDE_REQ_ENABLE).equalsIgnoreCase("yes") && generalFunc.retrieveValue("IS_RIDE_MODULE_AVAIL").equalsIgnoreCase("yes")) {
            if (!generalFunc.getJsonValue("eGender", userProfileJson).equalsIgnoreCase("") && !generalFunc.getJsonValue("eGender", userProfileJson).equalsIgnoreCase("Male")) {
                checkboxFemale.setVisibility(View.VISIBLE);
                noteText.setVisibility(View.GONE);
            } else if (generalFunc.getJsonValue("eGender", userProfileJson).equalsIgnoreCase("")) {
                checkboxFemale.setVisibility(View.VISIBLE);
            } else {
                checkboxFemale.setVisibility(View.GONE);
                noteText.setVisibility(View.GONE);
            }
        } else {
            checkboxFemale.setVisibility(View.GONE);
            noteText.setVisibility(View.GONE);
        }
        if (isfemale) {
            checkboxFemale.setChecked(true);
        }

        if (ishandicap) {
            checkboxHandicap.setChecked(true);
        }
        if (isChildSeat) {
            checkboxChildseat.setChecked(true);
        }
        if (isWheelChair) {
            checkboxWheelChair.setChecked(true);
        }
        MButton btn_type2 = btn_type2 = ((MaterialRippleLayout) dialogView.findViewById(R.id.btn_type2)).getChildView();
        int submitBtnId = Utils.generateViewId();
        btn_type2.setId(submitBtnId);
        btn_type2.setText(generalFunc.retrieveLangLBl("Update", "LBL_UPDATE"));
        btn_type2.setOnClickListener(v -> {
            pref_dialog.dismiss();
            if (checkboxFemale.isChecked()) {
                isfemale = true;
            } else {
                isfemale = false;
            }
            if (checkboxHandicap.isChecked()) {
                ishandicap = true;
            } else {
                ishandicap = false;
            }
            if (checkboxChildseat.isChecked()) {
                isChildSeat = true;
            } else {
                isChildSeat = false;
            }
            if (checkboxWheelChair.isChecked()) {
                isWheelChair = true;
            } else {
                isWheelChair = false;
            }
            if (loadAvailCabs != null) {
                loadAvailCabs.changeCabs();
            }
        });

        builder.setView(dialogView);
        TitleTxt.setText(generalFunc.retrieveLangLBl("Prefrance", "LBL_PREFRANCE_TXT"));
        checkboxHandicap.setText(generalFunc.retrieveLangLBl("Filter handicap accessibility drivers only", "LBL_MUST_HAVE_HANDICAP_ASS_CAR"));
        checkboxFemale.setText(generalFunc.retrieveLangLBl("Accept Female Only trip request", "LBL_ACCEPT_FEMALE_REQ_ONLY_PASSENGER"));
        checkboxChildseat.setText(generalFunc.retrieveLangLBl("", "LBL_MUST_HAVE_CHILD_SEAT_ASS_CAR"));
        checkboxWheelChair.setText(generalFunc.retrieveLangLBl("", "LBL_MUST_HAVE_WHEEL_CHAIR_ASS_CAR"));

        pref_dialog = builder.create();
        pref_dialog.setCancelable(false);
        if (generalFunc.isRTLmode()) {
            generalFunc.forceRTLIfSupported(pref_dialog);
        }
        pref_dialog.getWindow().setBackgroundDrawable(getActContext().getResources().getDrawable(R.drawable.all_roundcurve_card));
        pref_dialog.show();
    }

    public void genderDailog() {
        final Dialog builder = new Dialog(getActContext(), R.style.Theme_Dialog);
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        builder.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        builder.setContentView(R.layout.gender_view);
        builder.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

        final MTextView genderTitleTxt = (MTextView) builder.findViewById(R.id.genderTitleTxt);
        final MTextView maleTxt = (MTextView) builder.findViewById(R.id.maleTxt);
        final MTextView femaleTxt = (MTextView) builder.findViewById(R.id.femaleTxt);
        final ImageView gendercancel = (ImageView) builder.findViewById(R.id.gendercancel);
        final ImageView gendermale = (ImageView) builder.findViewById(R.id.gendermale);
        final ImageView genderfemale = (ImageView) builder.findViewById(R.id.genderfemale);
        final LinearLayout male_area = (LinearLayout) builder.findViewById(R.id.male_area);
        final LinearLayout female_area = (LinearLayout) builder.findViewById(R.id.female_area);

        if (generalFunc.isRTLmode()) {
            //            ((LinearLayout)builder.findViewById(R.id.llCancelButton)).setRotation(180);
                                /*RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params1.addRule(RelativeLayout.ALIGN_PARENT_START);
            gendercancel.setLayoutParams(params1);*/
            LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params1.gravity = Gravity.START;
            gendercancel.setLayoutParams(params1);
        }

        genderTitleTxt.setText(generalFunc.retrieveLangLBl("Select your gender to continue", "LBL_SELECT_GENDER"));
        maleTxt.setText(generalFunc.retrieveLangLBl("Male", "LBL_MALE_TXT"));
        femaleTxt.setText(generalFunc.retrieveLangLBl("FeMale", "LBL_FEMALE_TXT"));
        gendercancel.setOnClickListener(v -> builder.dismiss());

        male_area.setOnClickListener(v -> {
            if (pref_dialog != null) {
                pref_dialog.dismiss();
            }

            callgederApi("Male");
            builder.dismiss();
        });
        female_area.setOnClickListener(v -> {
            if (pref_dialog != null) {
                pref_dialog.dismiss();
            }
            callgederApi("Female");
            builder.dismiss();
        });

        builder.show();
    }

    public void callgederApi(String egender) {
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("type", "updateUserGender");
        parameters.put("UserType", Utils.userType);
        parameters.put("iMemberId", generalFunc.getMemberId());
        parameters.put("eGender", egender);

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {
            boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);
            String message = generalFunc.getJsonValue(Utils.message_str, responseString);
            if (isDataAvail) {
                generalFunc.storeData(Utils.USER_PROFILE_JSON, message);
                getUserProfileJson();
                prefBtnImageView.performClick();
            }
        });
        exeWebServer.execute();
    }

    public void checkPlaces() {

        String home_address_str = generalFunc.retrieveValue("userHomeLocationAddress");
//        if(home_address_str.equalsIgnoreCase("")){
//            home_address_str = "----";
//        }
        String work_address_str = generalFunc.retrieveValue("userWorkLocationAddress");
//        if(work_address_str.equalsIgnoreCase("")){
//            work_address_str = "----";
//        }

        if (home_address_str != null && !home_address_str.equalsIgnoreCase("")) {

            homePlaceTxt.setText(generalFunc.retrieveLangLBl("", "LBL_HOME_PLACE"));
//            homePlaceHTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
//            homePlaceTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
//            homePlaceTxt.setTextColor(Color.parseColor("#909090"));
            homePlaceHTxt.setText("" + home_address_str);
            homePlaceHTxt.setVisibility(View.VISIBLE);
            //homePlaceHTxt.setTextColor(getResources().getColor(R.color.black));
            homeActionImgView.setImageResource(R.mipmap.ic_edit);

        } else {
            homePlaceHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_HOME_PLACE"));
//            homePlaceHTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
//            homePlaceTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            homePlaceTxt.setText("" + generalFunc.retrieveLangLBl("", "LBL_ADD_HOME_PLACE_TXT"));
            //      homePlaceTxt.setTextColor(Color.parseColor("#909090"));
            homeActionImgView.setImageResource(R.mipmap.ic_pluse);
        }

        if (work_address_str != null && !work_address_str.equalsIgnoreCase("")) {

            workPlaceTxt.setText(generalFunc.retrieveLangLBl("", "LBL_WORK_PLACE"));
            workPlaceHTxt.setText("" + work_address_str);
            //   workPlaceHTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            //  workPlaceTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            //  workPlaceTxt.setTextColor(getResources().getColor(R.color.gray));
            workPlaceHTxt.setVisibility(View.VISIBLE);
//            workPlaceTxt.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, img_edit, null);
            //  workPlaceHTxt.setTextColor(getResources().getColor(R.color.black));
            workActionImgView.setImageResource(R.mipmap.ic_edit);

        } else {
            workPlaceHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_WORK_PLACE"));
            // workPlaceHTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            //workPlaceTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            workPlaceTxt.setText("" + generalFunc.retrieveLangLBl("", "LBL_ADD_WORK_PLACE_TXT"));
            // workPlaceTxt.setTextColor(Color.parseColor("#909090"));
            workActionImgView.setImageResource(R.mipmap.ic_pluse);
        }

        if (home_address_str != null && home_address_str.equalsIgnoreCase("")) {
            homePlaceHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_ADD_HOME_PLACE_TXT"));
            //homePlaceTxt.setText("----");
            homePlaceTxt.setVisibility(View.GONE);

            //  homePlaceHTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            //  homePlaceHTxt.setTextColor(getResources().getColor(R.color.black));

            //  homePlaceTxt.setTextColor(Color.parseColor("#909090"));
            //  homePlaceTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);

            homePlaceHTxt.setVisibility(View.VISIBLE);
            homeActionImgView.setImageResource(R.mipmap.ic_pluse);
        }

        if (work_address_str != null && work_address_str.equalsIgnoreCase("")) {
            workPlaceHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_ADD_WORK_PLACE_TXT"));
            workPlaceTxt.setText("----");
            workPlaceTxt.setVisibility(View.GONE);

            //  workPlaceHTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            // workPlaceHTxt.setTextColor(getResources().getColor(R.color.black));

            //  workPlaceTxt.setTextColor(Color.parseColor("#909090"));
            //   workPlaceTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);

            workPlaceHTxt.setVisibility(View.VISIBLE);
            workActionImgView.setImageResource(R.mipmap.ic_pluse);
        }
    }

    public Activity getActContext() {
        return RideDeliveryActivity.this;
    }

    @Override
    protected void onResume() {
        super.onResume();


        if (myProfileFragment != null && isProfilefrg) {
            myProfileFragment.onResume();
        }

        if (myWalletFragment != null && isWalletfrg) {
            myWalletFragment.onResume();
        }

        if (myBookingFragment != null && isBookingfrg) {
            myBookingFragment.onResume();
        }

        if (generalFunc.retrieveValue(Utils.ISWALLETBALNCECHANGE).equalsIgnoreCase("Yes")) {
            // getWalletBalDetails();
        }

        userProfileJson = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);

        //  setUserInfo();


        if (iswallet) {

            iswallet = false;
        }

        handler.postDelayed( runnable = () -> {
            getOnlineDriversRideDelivery("Ride");
            handler.postDelayed(runnable, delay);
        }, 0);

    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(runnable);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (getLastLocation != null) {
            getLastLocation.stopLocationUpdates();
        }
        releaseResources();
        super.onDestroy();
    }

    public void releaseResources() {
        setGoogleMapCameraListener(null);
        this.gMap = null;
        getAddressFromLocation.setAddressList(null);
        getAddressFromLocation = null;
    }

    public void openHistoryFragment() {
        this.getWindow().setStatusBarColor(getResources().getColor(R.color.appThemeColor_1));
        if (activityWeakReference.get() != null) {
            activityWeakReference.get().skeletonScreen.hide();

        }

        isProfilefrg = false;
        isWalletfrg = false;
        isBookingfrg = true;
        container.setVisibility(View.VISIBLE);
        if (myBookingFragment == null) {
            myBookingFragment = new MyBookingFragment();
        } else {
            myBookingFragment.onDestroy();
            myBookingFragment = new MyBookingFragment();
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, myBookingFragment).commit();
    }

    public void manageHome() {
        isProfilefrg = false;
        isWalletfrg = false;
        isBookingfrg = false;
        container.setVisibility(View.GONE);

        if (imagesList != null && imagesList.size() > 0 && imagesList.get(0).get("vStatusBarColor") != null) {
            bannerViewPager.setCurrentItem(0);
            toolbar.setBackgroundColor(Color.parseColor(imagesList.get(0).get("vStatusBarColor")));
            this.getWindow().setStatusBarColor(Color.parseColor(imagesList.get(0).get("vStatusBarColor")));
        }
    }

    public void openProfileFragment() {
        this.getWindow().setStatusBarColor(getResources().getColor(R.color.appThemeColor_1));
        if (activityWeakReference.get() != null) {
            activityWeakReference.get().skeletonScreen.hide();

        }
        isProfilefrg = true;
        isWalletfrg = false;
        isBookingfrg = false;
//        if (myProfileFragment != null) {
//            myProfileFragment = null;
//            Utils.runGC();
//        }


        container.setVisibility(View.VISIBLE);
        if (myProfileFragment == null) {
            myProfileFragment = new MyProfileFragment();
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, myProfileFragment).commit();


    }

    boolean isProfilefrg = false;
    boolean isWalletfrg = false;
    boolean isBookingfrg = false;

    public void openWalletFragment() {
        this.getWindow().setStatusBarColor(getResources().getColor(R.color.appThemeColor_1));
        if (activityWeakReference.get() != null) {
            activityWeakReference.get().skeletonScreen.hide();

        }
        isProfilefrg = false;
        isWalletfrg = true;
        isBookingfrg = false;

//        if (myProfileFragment != null) {
//            myProfileFragment = null;
//            Utils.runGC();
//        }


        container.setVisibility(View.VISIBLE);
        if (myWalletFragment == null) {
            myWalletFragment = new MyWalletFragment();
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, myWalletFragment).commit();


    }

    public void getDetails() {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "getServiceCategoryDetails");
        parameters.put("UserType", Utils.app_type);
        parameters.put("iMemberId", generalFunc.getMemberId());
        parameters.put("iVehicleCategoryId", "0");


        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), false, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {
            JSONObject responseObj = generalFunc.getJsonObject(responseString);

            if (responseObj != null && !responseObj.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseObj);

                if (isDataAvail) {
                    mProgressBar.setVisibility(View.GONE);
                    MainLayout.setVisibility(View.VISIBLE);
                    // bottomMenuArea.setVisibility(View.VISIBLE);

                    itemList = new ArrayList<>();
                    JSONArray itemarr = generalFunc.getJsonArray(Utils.message_str, responseObj);
                    for (int i = 0; i < itemarr.length(); i++) {
                        JSONObject obj_temp = generalFunc.getJsonObject(itemarr, i);
                        HashMap<String, String> itemObj = new HashMap<>();
                        itemObj.put("vCategory", generalFunc.getJsonValueStr("vCategory", obj_temp));
                        itemObj.put("vImage", generalFunc.getJsonValueStr("vImage", obj_temp));
                        itemObj.put("eCatType", generalFunc.getJsonValueStr("eCatType", obj_temp));
                        itemObj.put("iVehicleCategoryId", generalFunc.getJsonValueStr("iVehicleCategoryId", obj_temp));

                        itemList.add(itemObj);
                        if (generalFunc.getJsonValueStr("eCatType", obj_temp).equalsIgnoreCase("Ride")) {
                            isRide = true;
                        }

                    }


                    JSONArray bannerarr = generalFunc.getJsonArray("bannerArray", responseObj);
                    //  ArrayList<String> imagesList = new ArrayList<String>();
                    imagesList = new ArrayList<>();
                    mCardAdapter = new RideDeliveryCardPagerAdapter();


                    for (int i = 0; i < bannerarr.length(); i++) {
                        JSONObject obj_temp = generalFunc.getJsonObject(bannerarr, i);

                        String vImage = generalFunc.getJsonValueStr("vImage", obj_temp);
                        //  String imageURL = Utils.getResizeImgURL(getActContext(), vImage, Utils.getWidthOfBanner(getActContext(), 0), Utils.getHeightOfBanner(getActContext(), 0, "16:9"));

                        // String imageURL = vImage;

                        HashMap<String, String> bannerObj = new HashMap<>();
                        bannerObj.put("vTitle", generalFunc.getJsonValueStr("vTitle", obj_temp));
                        bannerObj.put("vSubtitle", generalFunc.getJsonValueStr("vSubtitle", obj_temp));
                        bannerObj.put("vBtnTtitle", generalFunc.getJsonValueStr("vBtnTtitle", obj_temp));
                        bannerObj.put("vTextColor", generalFunc.getJsonValueStr("vTextColor", obj_temp));
                        bannerObj.put("vBtnBgColor", generalFunc.getJsonValueStr("vBtnBgColor", obj_temp));
                        bannerObj.put("vBtnTextColor", generalFunc.getJsonValueStr("vBtnTextColor", obj_temp));
                        bannerObj.put("eCatType", generalFunc.getJsonValueStr("eCatType", obj_temp));
                        bannerObj.put("vCategory", generalFunc.getJsonValueStr("vCategory", obj_temp));
                        bannerObj.put("iVehicleCategoryId", generalFunc.getJsonValueStr("iVehicleCategoryId", obj_temp));
                        bannerObj.put("vImage", vImage);
                        bannerObj.put("vStatusBarColor", generalFunc.getJsonValueStr("vStatusBarColor", obj_temp));


                        imagesList.add(bannerObj);
                        mCardAdapter.addCardItem(bannerObj, getActContext(), this);
                    }
                    if (imagesList != null && imagesList.size() > 0) {
                        toolbar.setBackgroundColor(Color.parseColor(imagesList.get(0).get("vStatusBarColor")));
                        this.getWindow().setStatusBarColor(Color.parseColor(imagesList.get(0).get("vStatusBarColor")));

                        int imageListSize = imagesList.size();
                        if (imageListSize > 2) {
                            bannerViewPager.setOffscreenPageLimit(3);
                        } else if (imageListSize > 1) {
                            bannerViewPager.setOffscreenPageLimit(2);
                        }
                    }

                    GridLayoutManager gridLay = new GridLayoutManager(getActContext(), 3);
                    dataListRecyclerView.setLayoutManager(gridLay);


                    // mCardShadowTransformer = new ShadowTransformer(bannerViewPager, mCardAdapter);
                    //mFragmentCardShadowTransformer = new ShadowTransformer(bannerViewPager, mFragmentCardAdapter);

                    bannerViewPager.setAdapter(mCardAdapter);
                    //  bannerViewPager.setPageTransformer(false, mCardShadowTransformer);
                    bannerViewPager.setOffscreenPageLimit(3);

                    rideDeliveryCategoryAdapter = new RideDeliveryCategoryAdapter(getActContext(), itemList, generalFunc);
                    rideDeliveryCategoryAdapter.setOnItemClickList(this);
                    dataListRecyclerView.setAdapter(rideDeliveryCategoryAdapter);

                    if (isRide) {
                        homeWorkArea.setVisibility(View.GONE);
                        whereTxt.setVisibility(View.VISIBLE);

                    } else {
                        homeWorkArea.setVisibility(View.GONE);
                        whereTxt.setVisibility(View.GONE);
                    }


                    getOnlineDriversRideDelivery(itemList.get(0).get("eCatType"));
                    bannerCirclePageIndicator.setDataSize(imagesList.size());
                    bannerCirclePageIndicator.setViewPager(bannerViewPager);
                    manageAutoScroll();

                } else {
                    final GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
                    generateAlert.setCancelable(false);
                    generateAlert.setBtnClickList(btn_id -> {
                        generateAlert.closeAlertBox();
                        if (btn_id == 1) {
                            finish();

                        }
                    });
                    generateAlert.setContentMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseObj)));
                    generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("Ok", "LBL_BTN_OK_TXT"));
                    generateAlert.showAlertBox();

                }
            } else {
                generalFunc.showError();
            }
        });
        exeWebServer.execute();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        try {


            if (imagesList.get(position).get("vStatusBarColor") != null) {
                toolbar.setBackgroundColor(Color.parseColor(imagesList.get(position).get("vStatusBarColor")));
                this.getWindow().setStatusBarColor(Color.parseColor(imagesList.get(position).get("vStatusBarColor")));
            }
        } catch (Exception e) {

        }


    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }


    @Override
    public void onItemClick(int position) {
        if (placeLocation != null) {
            HashMap<String, String> locationArray = itemList.get(position);
            locationArray.put("pic_lat", String.valueOf(placeLocation.latitude));
            locationArray.put("pic_long", String.valueOf(placeLocation.longitude));
            locationArray.put("pic_address", pickUpLocationAddress);
            locationArray.put("vCategory", itemList.get(position).get("vCategory"));
            locationArray.put("vImage", itemList.get(position).get("vImage"));
            locationArray.put("eCatType", itemList.get(position).get("eCatType"));
            locationArray.put("iVehicleCategoryId", itemList.get(position).get("iVehicleCategoryId"));
            (new OpenCatType(getActContext(), locationArray)).execute();
        }
        //(new OpenCatType(getActContext(), itemList.get(position))).execute();

    }

    @Override
    public void onBannerItemClick(int position) {
        (new OpenCatType(getActContext(), imagesList.get(position))).execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN_UP && resultCode == RESULT_OK) {

        } else if (requestCode == Utils.ADD_HOME_LOC_REQ_CODE && resultCode == RESULT_OK && data != null) {

            String Latitude = data.getStringExtra("Latitude");
            String Longitude = data.getStringExtra("Longitude");
            String Address = data.getStringExtra("Address");

            generalFunc.storeData("userHomeLocationLatitude", "" + Latitude);
            generalFunc.storeData("userHomeLocationLongitude", "" + Longitude);
            generalFunc.storeData("userHomeLocationAddress", "" + Address);

            homePlaceTxt.setText(Address);
            checkPlaces();


            double lati = generalFunc.parseDoubleValue(0.0, Latitude);
            double longi = generalFunc.parseDoubleValue(0.0, Longitude);
            //  resetOrAddDest(data.getIntExtra("pos", -1), Address, lati, longi, "" + false);


        } else if (requestCode == Utils.ADD_MAP_LOC_REQ_CODE && resultCode == RESULT_OK && data != null) {

            String Latitude = data.getStringExtra("Latitude");
            String Longitude = data.getStringExtra("Longitude");
            String Address = data.getStringExtra("Address");

            double lati = generalFunc.parseDoubleValue(0.0, Latitude);
            double longi = generalFunc.parseDoubleValue(0.0, Longitude);

            //  resetOrAddDest(data.getIntExtra("pos", -1), Address, lati, longi, "" + false);

        } else if (requestCode == Utils.ADD_WORK_LOC_REQ_CODE && resultCode == RESULT_OK && data != null) {
            String Latitude = data.getStringExtra("Latitude");
            String Longitude = data.getStringExtra("Longitude");
            String Address = data.getStringExtra("Address");


            generalFunc.storeData("userWorkLocationLatitude", "" + Latitude);
            generalFunc.storeData("userWorkLocationLongitude", "" + Longitude);
            generalFunc.storeData("userWorkLocationAddress", "" + Address);

            workPlaceTxt.setText(Address);
            checkPlaces();

            double lati = generalFunc.parseDoubleValue(0.0, Latitude);
            double longi = generalFunc.parseDoubleValue(0.0, Longitude);
            // resetOrAddDest(data.getIntExtra("pos", -1), Address, lati, longi, "" + false);

        } else if (requestCode == Utils.PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                //pinImgView.setVisibility(View.VISIBLE);
                Place place = PlaceAutocomplete.getPlace(this, data);
                placeTxtView.setText(place.getAddress());
                isPlaceSelected = true;
                LatLng placeLocation = place.getLatLng();
                this.placeLocation = placeLocation;
                CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(placeLocation, Utils.defaultZomLevel);
                if (gMap != null) {
                    gMap.clear();
                    placeMarker = gMap.addMarker(new MarkerOptions().position(placeLocation).title("" + place.getAddress()));
                    filterDrivers(false);
                    gMap.moveCamera(cu);
                }
                getOnlineDriversRideDelivery("Ride");
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                generalFunc.showMessage(generalFunc.getCurrentView(RideDeliveryActivity.this), status.getStatusMessage());
            } else if (requestCode == RESULT_CANCELED) {
            }
        } else if (requestCode == Utils.PLACE_CUSTOME_LOC_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                //pinImgView.setVisibility(View.VISIBLE);
                placeTxtView.setText(data.getStringExtra("Address"));
                isPlaceSelected = true;
                //isAddressEnable = true;
                LatLng placeLocation = new LatLng(generalFunc.parseDoubleValue(0.0, data.getStringExtra("Latitude")), generalFunc.parseDoubleValue(0.0, data.getStringExtra("Longitude")));
                this.placeLocation = placeLocation;
                CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(placeLocation, Utils.defaultZomLevel);
                if (gMap != null && placeLocation != null) {
                    gMap.clear();
                    gMap.moveCamera(cu);
                }
                filterDrivers(false);
                getOnlineDriversRideDelivery("Ride");
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                generalFunc.showMessage(generalFunc.getCurrentView(RideDeliveryActivity.this), status.getStatusMessage());
            } else if (requestCode == RESULT_CANCELED) {
            }
        }
    }

    public void manageAutoScroll() {
        final Handler handler = new Handler();
        final Runnable Update = new Runnable() {
            public void run() {
                if (!isProfilefrg && !iswallet && !isBookingfrg) {
                    if (currentPage == imagesList.size()) {
                        currentPage = 0;
                    }
                    bannerViewPager.setCurrentItem(currentPage++, true);
                }
            }
        };

        timer = new Timer(); // This will create a new Thread
        timer.schedule(new TimerTask() { // task to be scheduled
            @Override
            public void run() {
                handler.post(Update);
            }
        }, DELAY_MS, PERIOD_MS);
    }

    public void getOnlineDriversRideDelivery(String etype) {
        if (userLocation == null) {
            return;
        }


        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "getOnlineDriversRideDelivery");
        parameters.put("PickUpLatitude", "" + userLocation.getLatitude());
        parameters.put("PickUpLongitude", "" + userLocation.getLongitude());
        parameters.put("iUserId", generalFunc.getMemberId());
        parameters.put("PickUpAddress", "");
        parameters.put("eType", etype);


        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);

        exeWebServer.setDataResponseListener(responseString -> {

            if (responseString != null && !responseString.equals("")) {

                JSONArray availCabArr = generalFunc.getJsonArray("DriverList", responseString);

                if (availCabArr != null) {
                    removeDriversFromMap(true);
                    listOfDrivers = new ArrayList<>();
                    for (int i = 0; i < availCabArr.length(); i++) {
                        JSONObject obj_temp = generalFunc.getJsonObject(availCabArr, i);

                        JSONObject carDetailsJson = generalFunc.getJsonObject("DriverCarDetails", obj_temp);
                        HashMap<String, String> driverDataMap = new HashMap<String, String>();
                        driverDataMap.put("driver_id", generalFunc.getJsonValueStr("iDriverId", obj_temp));
                        driverDataMap.put("Name", generalFunc.getJsonValueStr("vName", obj_temp));
                        driverDataMap.put("eIsFeatured", generalFunc.getJsonValueStr("eIsFeatured", obj_temp));
                        driverDataMap.put("LastName", generalFunc.getJsonValueStr("vLastName", obj_temp));
                        driverDataMap.put("Latitude", generalFunc.getJsonValueStr("vLatitude", obj_temp));
                        driverDataMap.put("Longitude", generalFunc.getJsonValueStr("vLongitude", obj_temp));
                        driverDataMap.put("GCMID", generalFunc.getJsonValueStr("iGcmRegId", obj_temp));
                        driverDataMap.put("iAppVersion", generalFunc.getJsonValueStr("iAppVersion", obj_temp));
                        driverDataMap.put("driver_img", generalFunc.getJsonValueStr("vImage", obj_temp));
                        driverDataMap.put("average_rating", generalFunc.getJsonValueStr("vAvgRating", obj_temp));
                        driverDataMap.put("DIST_TO_PICKUP_INT", generalFunc.getJsonValueStr("distance", obj_temp));
                        driverDataMap.put("vPhone_driver", generalFunc.getJsonValueStr("vPhone", obj_temp));
                        driverDataMap.put("vPhoneCode_driver", generalFunc.getJsonValueStr("vCode", obj_temp));
                        driverDataMap.put("tProfileDescription", generalFunc.getJsonValueStr("tProfileDescription", obj_temp));
                        driverDataMap.put("ACCEPT_CASH_TRIPS", generalFunc.getJsonValueStr("ACCEPT_CASH_TRIPS", obj_temp));
                        driverDataMap.put("vWorkLocationRadius", generalFunc.getJsonValueStr("vWorkLocationRadius", obj_temp));
                        driverDataMap.put("PROVIDER_RADIUS", generalFunc.getJsonValueStr("vWorkLocationRadius", obj_temp));
                        driverDataMap.put("iGcmRegId", generalFunc.getJsonValueStr("iGcmRegId", obj_temp));

                        driverDataMap.put("DriverGender", generalFunc.getJsonValueStr("eGender", obj_temp));
                        driverDataMap.put("eFemaleOnlyReqAccept", generalFunc.getJsonValueStr("eFemaleOnlyReqAccept", obj_temp));

                        driverDataMap.put("eHandiCapAccessibility", generalFunc.getJsonValueStr("eHandiCapAccessibility", obj_temp));
                        driverDataMap.put("eChildSeatAvailable", generalFunc.getJsonValueStr("eChildSeatAvailable", obj_temp));
                        driverDataMap.put("eWheelChairAvailable", generalFunc.getJsonValueStr("eWheelChairAvailable", obj_temp));
                        driverDataMap.put("vCarType", generalFunc.getJsonValueStr("vCarType", obj_temp));
                        driverDataMap.put("vColour", generalFunc.getJsonValueStr("vColour", obj_temp));
                        driverDataMap.put("vLicencePlate", generalFunc.getJsonValueStr("vLicencePlate", obj_temp));
                        driverDataMap.put("make_title", generalFunc.getJsonValueStr("make_title", obj_temp));
                        driverDataMap.put("model_title", generalFunc.getJsonValueStr("model_title", obj_temp));
                        driverDataMap.put("fAmount", generalFunc.getJsonValueStr("fAmount", obj_temp));
                        driverDataMap.put("eRental", generalFunc.getJsonValueStr("vRentalCarType", obj_temp));
                        /*End of the day feature - driver is in destination Mode*/
                        driverDataMap.put("eDestinationMode", generalFunc.getJsonValueStr("eDestinationMode", obj_temp));


                        driverDataMap.put("vCurrencySymbol", generalFunc.getJsonValueStr("vCurrencySymbol", obj_temp));

                        driverDataMap.put("PROVIDER_RATING_COUNT", generalFunc.getJsonValueStr("PROVIDER_RATING_COUNT", obj_temp));

                        driverDataMap.put("eFareType", generalFunc.getJsonValueStr("eFareType", obj_temp));
                        driverDataMap.put("ePoolRide", generalFunc.getJsonValueStr("ePoolRide", obj_temp));
                        driverDataMap.put("fMinHour", generalFunc.getJsonValueStr("fMinHour", obj_temp));
                        driverDataMap.put("eTripStatusActive", generalFunc.getJsonValueStr("eTripStatusActive", obj_temp));
                        driverDataMap.put("eFavDriver", generalFunc.getJsonValueStr("eFavDriver", obj_temp));
                        driverDataMap.put("iStopId", generalFunc.getJsonValueStr("iStopId", obj_temp));
                        driverDataMap.put("eIconType", generalFunc.getJsonValueStr("eIconType", obj_temp));

                        driverDataMap.put("IS_PROVIDER_ONLINE", generalFunc.getJsonValueStr("IS_PROVIDER_ONLINE", obj_temp));
                        listOfDrivers.add(driverDataMap);
                    }

                } else {
                    removeDriversFromMap(true);
                }


                filterDrivers(false);


            } else {
                filterDrivers(false);
                removeDriversFromMap(true);

            }

        });
        exeWebServer.execute();
    }

    public void removeDriversFromMap(boolean isUnSubscribeAll) {
        if (driverMarkerList.size() > 0) {
            ArrayList<Marker> tempDriverMarkerList = new ArrayList<>();
            tempDriverMarkerList.addAll(driverMarkerList);
            for (int i = 0; i < tempDriverMarkerList.size(); i++) {
                Marker marker_temp = driverMarkerList.get(0);
                marker_temp.remove();
                driverMarkerList.remove(0);

            }
        }


    }

    public void filterDrivers(boolean isCheckAgain) {
        ArrayList<Marker> driverMarkerList_temp = new ArrayList<>();
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        if (listOfDrivers != null) {
            for (int i = 0; i < listOfDrivers.size(); i++) {
                HashMap<String, String> driverData = listOfDrivers.get(i);
                double driverLocLatitude = GeneralFunctions.parseDoubleValue(0.0, driverData.get("Latitude"));
                double driverLocLongitude = GeneralFunctions.parseDoubleValue(0.0, driverData.get("Longitude"));
                builder.include(new LatLng(driverLocLatitude, driverLocLongitude));
                Marker driverMarker = drawMarker(new LatLng(driverLocLatitude, driverLocLongitude), "", driverData);
                driverMarkerList_temp.add(driverMarker);


            }
            removeDriversFromMap(true);
            driverMarkerList.addAll(driverMarkerList_temp);
        }
    }

    public Marker drawMarker(LatLng point, String Name, HashMap<String, String> driverData) {

        MarkerOptions markerOptions = new MarkerOptions();
        //String eIconType = generalFunc.getSelectedCarTypeData(selectedCabTypeId, cabTypesArrList, "eIconType");
        String eIconType = driverData.get("eIconType");

        int iconId = R.mipmap.car_driver;
        if (eIconType.equalsIgnoreCase("Bike")) {
            iconId = R.mipmap.car_driver_1;
        } else if (eIconType.equalsIgnoreCase("Cycle")) {
            iconId = R.mipmap.car_driver_2;
        } else if (eIconType.equalsIgnoreCase("Truck")) {
            iconId = R.mipmap.car_driver_4;
        } else if (eIconType.equalsIgnoreCase("Fly")) {
            iconId = R.mipmap.ic_fly_icon;
        }

        SelectableRoundedImageView providerImgView = null;
        View marker_view = null;

        markerOptions.position(point).title("DriverId" + driverData.get("driver_id")).icon(BitmapDescriptorFactory.fromResource(iconId))
                .anchor(0.5f, 0.5f).flat(true);


        // Adding marker on the Google Map
        final Marker marker = gMap.addMarker(markerOptions);
        marker.setRotation(0);
        marker.setVisible(true);


        return marker;
    }

    public static Bitmap createDrawableFromView(Context context, View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }

    public void pubNubMsgArrived(String message) {

        String driverMsg = generalFunc.getJsonValue("Message", message);
        String eType = generalFunc.getJsonValue("eType", message);
        //   String app_type = APP_TYPE;

        if (driverMsg.equals("CabRequestAccepted")) {
            String eSystem = generalFunc.getJsonValue("eSystem", userProfileJson);
            if (eSystem != null && eSystem.equalsIgnoreCase("DeliverAll")) {
                generalFunc.showGeneralMessage("", generalFunc.getJsonValue("vTitle", message));
                return;
            }


            if (eType.equalsIgnoreCase(Utils.eType_Multi_Delivery)) {

                return;
            }

            if (generalFunc.isJSONkeyAvail("iCabBookingId", message) && !generalFunc.getJsonValue("iCabBookingId", message).trim().equals("")) {
                MyApp.getInstance().restartWithGetDataApp();
            } else {
                if (eType.equalsIgnoreCase(Utils.CabGeneralType_UberX) || eType.equalsIgnoreCase(Utils.eType_Multi_Delivery)) {
                    return;
                } else {
                    MyApp.getInstance().restartWithGetDataApp();
                }
            }

        }

    }

}