package com.adapter.files;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.ViewPagerCards.RoundCornerDrawable;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.leadryde.userapp.R;
import com.general.files.GeneralFunctions;
import com.general.files.showTermsDialog;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.utils.CommonUtilities;
import com.utils.Logger;
import com.utils.Utils;
import com.view.CreateRoundedView;
import com.view.MTextView;
import com.view.SelectableRoundedImageView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Admin on 02-03-2017.
 */
public class UberXCategoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int TYPE_GRID = 0;
    private final int TYPE_BANNER = 1;
    private final int TYPE_LIST = 2;
    private final int NO_TYPE = -1;

    public GeneralFunctions generalFunc;
    ArrayList<HashMap<String, String>> list_item;
    Context mContext;
    OnItemClickList onItemClickList;

    String CAT_TYPE_MODE = "0";

    boolean isForceToGrid = false;
    int positionOfSeperatorView = -1;
    boolean ismultiSelect = false;
    String userProfileJson;

    int dimension;
    int bannerHeight;
    int bannerWidth;
    int width;
    int margin;
    int grid;
    boolean click = false;
    //added for manage Listing
    String APP_HOME_PAGE_LIST_VIEW_ENABLED;
    String LBL_DELIVERY_SERVICES;
    boolean isFirst = true;
    int isFirstPos = 0;
    int daynamic_height, daynamic_width;
    boolean isDaynamic = false;

    public UberXCategoryAdapter(Context mContext, ArrayList<HashMap<String, String>> list_item, GeneralFunctions generalFunc) {
        this.mContext = mContext;
        this.list_item = list_item;
        this.generalFunc = generalFunc;
        userProfileJson = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);
        APP_HOME_PAGE_LIST_VIEW_ENABLED = generalFunc.getJsonValue("APP_HOME_PAGE_LIST_VIEW_ENABLED", userProfileJson);
        LBL_DELIVERY_SERVICES = generalFunc.retrieveLangLBl("", "LBL_DELIVERY_SERVICES");
        if (generalFunc.getJsonValue("SERVICE_PROVIDER_FLOW", userProfileJson).equalsIgnoreCase("PROVIDER")) {
            ismultiSelect = true;
        }
        dimension = mContext.getResources().getDimensionPixelSize(R.dimen.category_grid_size);
        margin = mContext.getResources().getDimensionPixelSize(R.dimen.category_banner_left_right_margin);
        grid = mContext.getResources().getDimensionPixelSize(R.dimen.category_grid_size);
        width = margin * 2;
        bannerWidth = Utils.getWidthOfBanner(mContext, width);
        bannerHeight = Utils.getHeightOfBanner(mContext, width * 4, "16:9");
        daynamic_height = mContext.getResources().getDimensionPixelSize(R.dimen._100sdp);
        daynamic_width = mContext.getResources().getDimensionPixelSize(R.dimen._150sdp);


    }

    public UberXCategoryAdapter(Context mContext, ArrayList<HashMap<String, String>> list_item, GeneralFunctions generalFunc, boolean isForceToGrid) {
        this.mContext = mContext;
        this.list_item = list_item;
        this.generalFunc = generalFunc;
        this.isForceToGrid = isForceToGrid;
        userProfileJson = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);
        APP_HOME_PAGE_LIST_VIEW_ENABLED = generalFunc.getJsonValue("APP_HOME_PAGE_LIST_VIEW_ENABLED", userProfileJson);
        LBL_DELIVERY_SERVICES = generalFunc.retrieveLangLBl("", "LBL_DELIVERY_SERVICES");

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {


        if (viewType == TYPE_BANNER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rdu_banner_design, parent, false);
            BannerViewHolder bannerViewHolder = new BannerViewHolder(view);
            return bannerViewHolder;
        } else if (viewType == TYPE_GRID) {
            View view = LayoutInflater.from(parent.getContext()).inflate(CAT_TYPE_MODE.equals("0") ? R.layout.item_uberx_cat_grid_design : R.layout.item_uberx_cat_list_design, parent, false);
            ViewHolder viewHolder = new ViewHolder(view);
            return viewHolder;
        } else if (viewType == TYPE_LIST) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_uberx_list_design, parent, false);
            if (generalFunc.getJsonValue("ENABLE_NEW_HOME_SCREEN_LAYOUT_APP", userProfileJson) != null && generalFunc.getJsonValue("ENABLE_NEW_HOME_SCREEN_LAYOUT_APP", userProfileJson).equalsIgnoreCase("Yes")) {
                isDaynamic = true;
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_uberx_daynamiclist_design, parent, false);
            }
            ListViewHolder viewHolder = new ListViewHolder(view);
            return viewHolder;
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(CAT_TYPE_MODE.equals("0") ? R.layout.item_uberx_cat_grid_design : R.layout.item_uberx_cat_list_design, parent, false);
            ViewHolder viewHolder = new ViewHolder(view);
            return viewHolder;

        }


    }

    public void setCategoryMode(String CAT_TYPE_MODE) {
        this.CAT_TYPE_MODE = CAT_TYPE_MODE;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder parentViewHolder, final int position) {

        HashMap<String, String> item = list_item.get(position);

        String vCategory = item.get("vCategory");
        String vLogo_image = item.get("vLogo_image");
        String vBannerImage = item.get("vBannerImage");
        String eShowTermsStr = item.get("eShowTerms");
        boolean eShowTerms = Utils.checkText(eShowTermsStr) && eShowTermsStr.equalsIgnoreCase("yes") ? true : false;

        if (parentViewHolder instanceof ListViewHolder) {

            String vListLogo = item.get("vListLogo");
            ListViewHolder viewHolder = (ListViewHolder) parentViewHolder;


            if (isFirstPos == position) {
                isFirst = true;
            }
            if (isFirst) {

                isFirst = false;
                isFirstPos = position;
                viewHolder.selectServiceTxt.setText(LBL_DELIVERY_SERVICES);
                viewHolder.selectServiceTxt.setVisibility(View.VISIBLE);
                if (isDaynamic) {
                    viewHolder.selectServiceTxt.setVisibility(View.GONE);
                }
            } else {
                viewHolder.selectServiceTxt.setVisibility(View.GONE);
            }
            viewHolder.catDescTxt.setText(item.get("tListDescription"));
            if (item.get("tListDescription") != null && item.get("tListDescription").equalsIgnoreCase("")) {
                if(isDaynamic) {
                    viewHolder.catDescTxt.setVisibility(View.GONE);
                }
            } else {
                viewHolder.catDescTxt.setVisibility(View.VISIBLE);
            }
            if (vCategory.matches("\\w*")) {
                viewHolder.catNameTxt.setMaxLines(1);

                viewHolder.catNameTxt.setText(vCategory);
            } else {
                viewHolder.catNameTxt.setMaxLines(2);

                viewHolder.catNameTxt.setText(vCategory);
            }

            String imageURL = Utils.getResizeImgURL(mContext, vListLogo, grid, grid);
            if (isDaynamic) {
                imageURL = Utils.getResizeImgURL(mContext, vListLogo, daynamic_width, daynamic_height);

                if (generalFunc.isRTLmode()) {
                    viewHolder.catImgView.setVisibility(View.GONE);
                    viewHolder.rtlcatImgView.setVisibility(View.VISIBLE);
                    viewHolder.imageAreaBg.setBackground(mContext.getResources().getDrawable(R.drawable.left_curve_card));
                }
            }


            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.mipmap.ic_no_icon);
//            requestOptions.error(R.drawable.ic_error);

            Glide.with(mContext).load(imageURL).apply(requestOptions).listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {

                    if (!vListLogo.contains("http") && !vListLogo.equals("")) {
                        Handler handler = new Handler();
                        handler.post(() -> Glide.with(mContext).load(GeneralFunctions.parseIntegerValue(0, vListLogo)).into(viewHolder.catImgView));
                    }
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    return false;
                }
            }).into(  isDaynamic?((generalFunc.isRTLmode()?viewHolder.rtlcatImgView:viewHolder.catImgView)):(viewHolder.catImgView));



            viewHolder.contentArea.setOnClickListener(view -> {
                if (CAT_TYPE_MODE.equalsIgnoreCase("0")) {
                    if (onItemClickList != null) {

                        if (eShowTerms && !CommonUtilities.ageRestrictServices.contains(vCategory)) {
                            new showTermsDialog(getActContext(), generalFunc, position, vCategory, click, new showTermsDialog.OnClickList() {
                                @Override
                                public void onClick() {
                                    onItemClickList.onItemClick(position);
                                }
                            });
                        } else {
                            onItemClickList.onItemClick(position);
                        }


                    }
                } else {

                    if (onItemClickList != null) {
                        onItemClickList.onItemClick(position);
                    }

                }
            });


        } else if (parentViewHolder instanceof ViewHolder) {
            ViewHolder viewHolder = (ViewHolder) parentViewHolder;


            try {
                if (generalFunc.isRTLmode()) {
                    if (viewHolder.arrowImageView != null) {
                        viewHolder.arrowImageView.setRotation(180);
                    }
                }
            } catch (Exception e) {
            }
            if (vCategory.matches("\\w*")) {
                viewHolder.uberXCatNameTxtView.setMaxLines(1);

                viewHolder.uberXCatNameTxtView.setText(vCategory);
            } else {
                viewHolder.uberXCatNameTxtView.setMaxLines(2);

                viewHolder.uberXCatNameTxtView.setText(vCategory);
            }

            String imageURL = Utils.getResizeImgURL(mContext, vLogo_image, grid, grid);

          /*  Picasso.with(mContext).load(vLogo_image.equals("") ? CommonUtilities.SERVER : imageURL).placeholder(R.mipmap.ic_no_icon).into(viewHolder.catImgView, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    if (!vLogo_image.contains("http") && !vLogo_image.equals("")) {
                        Picasso.with(mContext).load(GeneralFunctions.parseIntegerValue(0, vLogo_image)).placeholder(R.mipmap.ic_no_icon).error(R.mipmap.ic_no_icon).into(viewHolder.catImgView);
                    }
                }
            });*/

            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.mipmap.ic_no_icon);
//            requestOptions.error(R.drawable.ic_error);

            Glide.with(mContext).load(imageURL).apply(requestOptions).listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {

                    if (!vLogo_image.contains("http") && !vLogo_image.equals("")) {
                        Handler handler = new Handler();
                        handler.post(() -> Glide.with(mContext).load(GeneralFunctions.parseIntegerValue(0, vLogo_image)).into(viewHolder.catImgView));
                    }
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    return false;
                }
            }).into(viewHolder.catImgView);


            viewHolder.contentArea.setOnClickListener(view -> {
                if (CAT_TYPE_MODE.equalsIgnoreCase("0")) {
                    if (onItemClickList != null) {

                        if (eShowTerms && !CommonUtilities.ageRestrictServices.contains(vCategory)) {
                            new showTermsDialog(getActContext(), generalFunc, position, vCategory, click, new showTermsDialog.OnClickList() {
                                @Override
                                public void onClick() {
                                    onItemClickList.onItemClick(position);
                                }
                            });
                        } else {
                            onItemClickList.onItemClick(position);
                        }


                    }
                } else {
                    if (ismultiSelect) {
                        if (viewHolder.serviceCheckbox != null) {
                            viewHolder.serviceCheckbox.setChecked(!viewHolder.serviceCheckbox.isChecked());
                        }
                    } else {
                        if (onItemClickList != null) {
                            onItemClickList.onItemClick(position);
                        }
                    }
                }
            });


            if (CAT_TYPE_MODE.equals("0")) {
                viewHolder.contentArea.setOnTouchListener(new SetOnTouchList(TYPE_GRID, position, false));
                /*viewHolder.contentArea.setOnTouchListener((view, motionEvent) -> {
                    Logger.e("CONTENT_AREA", "TOUCH");
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            scaleView(view, 0.85f, 0.85f, motionEvent.getAction());
                            break;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            scaleView(view, (float) 1.0, (float) 1.0, motionEvent.getAction());
                            break;
                    }
                    return true;
                });*/
            } else {
                viewHolder.contentArea.setOnTouchListener(null);
                if (ismultiSelect) {
                    viewHolder.serviceCheckbox.setVisibility(View.VISIBLE);
                    viewHolder.arrowImageView.setVisibility(View.GONE);
                    viewHolder.serviceCheckbox.setOnCheckedChangeListener((compoundButton, b) -> {

                                if (b) {
                                    viewHolder.uberXCatNameTxtView.setTextColor(mContext.getResources().getColor(R.color.appThemeColor_1));
                                } else {
                                    viewHolder.uberXCatNameTxtView.setTextColor(mContext.getResources().getColor(R.color.black));
                                }
                                onItemClickList.onMultiItem(item.get("iVehicleCategoryId"), b);
                            }
                    );

                    String isCheck = item.get("isCheck");
                    if (isCheck != null && isCheck.equals("Yes")) {
                        viewHolder.serviceCheckbox.setChecked(true);


                    } else if (isCheck != null && isCheck.equals("No")) {
                        viewHolder.serviceCheckbox.setChecked(false);
                    }

                } else {
                    viewHolder.serviceCheckbox.setVisibility(View.GONE);
                    viewHolder.arrowImageView.setVisibility(View.VISIBLE);
                }
            }

        } else if (parentViewHolder instanceof BannerViewHolder) {
            BannerViewHolder holder = (BannerViewHolder) parentViewHolder;


            holder.seperatorView.setVisibility(View.GONE);
            if (!generalFunc.getJsonValue("RDU_HOME_PAGE_LAYOUT_DESIGN", userProfileJson).equalsIgnoreCase("Banner/Icon")) {
                if (positionOfSeperatorView == -1 || positionOfSeperatorView == position) {
                    holder.seperatorView.setVisibility(View.GONE);
                    positionOfSeperatorView = position;
                }
            }

            if(isDaynamic)
            {
                holder.containerView.setContentPadding(0,0,0,0);
                holder.containerView.setUseCompatPadding(false);
            }

            if (generalFunc.isRTLmode()) {
                holder.bookNowImg.setScaleX(-1);
            }

            String vCategoryBanner = item.get("vCategoryBanner");
            holder.serviceNameTxt.setText(vCategoryBanner.equalsIgnoreCase("") ? vCategory : vCategoryBanner);

            holder.bookNowTxt.setText(item.get("tBannerButtonText"));

            CardView.LayoutParams layoutParams = (CardView.LayoutParams) holder.bannerAreaContainerView.getLayoutParams();

            layoutParams.height = bannerHeight;

            int btnRadius = Utils.dipToPixels(mContext, 8);
            int strokeWidth = Utils.dipToPixels(mContext, 0);
            int color1 = mContext.getResources().getColor(R.color.appThemeColor_1);
            int color2 = mContext.getResources().getColor(R.color.appThemeColor_2);

            new CreateRoundedView(color1, btnRadius, strokeWidth, color1, holder.bookNowTxt);
            new CreateRoundedView(color2, btnRadius, strokeWidth, color1, holder.serviceNameTxt);


            String imageURL = Utils.getResizeImgURL(mContext, vBannerImage, bannerWidth, bannerHeight);

            Picasso.get()
                    .load(vBannerImage.equals("") ? CommonUtilities.SERVER : imageURL)
                    .placeholder(R.mipmap.ic_no_icon)
                    .into(holder.bannerImgView, new Callback() {
                        @Override
                        public void onSuccess() {
                            try {
                                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                                    holder.bannerImgView.invalidate();
                                    BitmapDrawable drawable = (BitmapDrawable) holder.bannerImgView.getDrawable();
                                    Bitmap bitmap = drawable.getBitmap();

                                    holder.containerView.setPreventCornerOverlap(false);

                                    RoundCornerDrawable round = new RoundCornerDrawable(bitmap, mContext.getResources().getDimension(R.dimen._5sdp), 0);
                                    holder.bannerImgView.setVisibility(View.GONE);
                                    holder.containerView.setBackground(round);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(Exception e) {

                            if (!vBannerImage.contains("http") && !vBannerImage.equals("")) {
                                Picasso.get()
                                        .load(GeneralFunctions.parseIntegerValue(0, vBannerImage))
                                        .placeholder(R.mipmap.ic_no_icon)
                                        .error(R.mipmap.ic_no_icon)
                                        .into(holder.bannerImgView);
                            }
                        }
                    });

            holder.containerView.setOnClickListener(view -> {
                if (onItemClickList != null) {

                    if (eShowTerms && !CommonUtilities.ageRestrictServices.contains(vCategory)) {
                        new showTermsDialog(getActContext(), generalFunc, position, vCategoryBanner, click, new showTermsDialog.OnClickList() {
                            @Override
                            public void onClick() {
                                onItemClickList.onItemClick(position);
                            }
                        });
                    } else {
                        onItemClickList.onItemClick(position);
                    }

                }
            });

            if (CAT_TYPE_MODE.equals("0")) {

                holder.containerView.setOnTouchListener(new SetOnTouchList(TYPE_BANNER, position, false));

                /*holder.containerView.setOnTouchListener((view, motionEvent) -> {
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            scaleView(view, 0.97f, 0.97f, motionEvent.getAction());
                            break;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            scaleView(view, (float) 1.0, (float) 1.0, motionEvent.getAction());
                            break;
                    }
                    return true;
                });*/
            } else {
                Logger.d("TouchOnBanner", "Removed");
                holder.containerView.setOnTouchListener(null);
            }
        }
    }

    private Context getActContext() {

        return mContext;
    }

    private class SetOnTouchList implements View.OnTouchListener {
        int viewType;
        int item_position;
        boolean isBlockDownEvent = false;

        public SetOnTouchList(int viewType, int item_position, boolean isBlockDownEvent) {
            this.item_position = item_position;
            this.viewType = viewType;
            this.isBlockDownEvent = isBlockDownEvent;
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {

            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (!isBlockDownEvent) {
                        scaleView(view, viewType == TYPE_BANNER ? 0.97f : 0.85f, viewType == TYPE_BANNER ? 0.97f : 0.85f, motionEvent.getAction(), viewType, item_position);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (list_item.size() > item_position && (list_item.get(item_position).get("LAST_CLICK_TIME") == null || (System.currentTimeMillis() - GeneralFunctions.parseLongValue(0, list_item.get(item_position).get("LAST_CLICK_TIME"))) > 1000)) {
                        scaleView(view, (float) 1.0, (float) 1.0, motionEvent.getAction(), viewType, item_position);
                    } else {
                        scaleView(view, (float) 1.0, (float) 1.0, MotionEvent.ACTION_CANCEL, viewType, item_position);
                    }

                    break;
                case MotionEvent.ACTION_CANCEL:
                    scaleView(view, (float) 1.0, (float) 1.0, motionEvent.getAction(), viewType, item_position);
                    break;
            }
            return true;
        }
    }

    public void scaleView(View v, float startScale, float endScale, int motionEvent, int viewType, int item_position) {

        v.setOnTouchListener(new SetOnTouchList(viewType, item_position, true));

        Animation anim = new ScaleAnimation(
                startScale, endScale, // Start and end values for the X axis scaling
                startScale, endScale, // Start and end values for the Y axis scaling
                Animation.RELATIVE_TO_SELF, 0.5f, // Pivot point of X scaling
                Animation.RELATIVE_TO_SELF, 0.5f); // Pivot point of Y scaling
        anim.setFillAfter(true); // Needed to keep the result of the animation
        anim.setDuration(100);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (motionEvent == MotionEvent.ACTION_UP && list_item.size() > item_position) {
                    list_item.get(item_position).put("LAST_CLICK_TIME", "" + System.currentTimeMillis());
                    v.performClick();
                }
                v.setOnTouchListener(new SetOnTouchList(viewType, item_position, false));
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        v.startAnimation(anim);
    }

    @Override
    public int getItemCount() {
        return list_item.size();
    }


    @Override
    public int getItemViewType(int position) {
        if (CAT_TYPE_MODE.equals("0")) {

            HashMap<String, String> dataMap = list_item.get(position);
            String eShowType = dataMap.get("eShowType");
            if (eShowType != null && !eShowType.equals("") && eShowType.equalsIgnoreCase("Banner") && isForceToGrid == false) {
                return TYPE_BANNER;
            } else if (eShowType != null && !eShowType.equals("") && eShowType.equalsIgnoreCase("ICON")) {
                return TYPE_GRID;
            } else if (eShowType != null && !eShowType.equals("") && eShowType.equalsIgnoreCase("List") && isForceToGrid == false) {
                return TYPE_LIST;
            }
        }
        return NO_TYPE;
    }

    public void setOnItemClickList(OnItemClickList onItemClickList) {
        this.onItemClickList = onItemClickList;
    }

    public interface OnItemClickList {
        void onItemClick(int position);

        void onMultiItem(String id, boolean b);
    }

    public class ListViewHolder extends RecyclerView.ViewHolder {

        public MTextView catNameTxt;
        public MTextView catDescTxt;
        public MTextView selectServiceTxt;

        public View contentArea,imageAreaBg;
        public SelectableRoundedImageView catImgView, rtlcatImgView;


        public ListViewHolder(View view) {
            super(view);

            catNameTxt = (MTextView) view.findViewById(R.id.catNameTxt);
            selectServiceTxt = (MTextView) view.findViewById(R.id.selectServiceTxt);
            catDescTxt = (MTextView) view.findViewById(R.id.catDescTxt);
            contentArea = view.findViewById(R.id.contentArea);
            catImgView = (SelectableRoundedImageView) view.findViewById(R.id.catImgView);

            if (isDaynamic) {
                rtlcatImgView = (SelectableRoundedImageView) view.findViewById(R.id.rtlcatImgView);
                imageAreaBg = (View) view.findViewById(R.id.imageAreaBg);
            }

        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public MTextView uberXCatNameTxtView;

        public View contentArea;
        public SelectableRoundedImageView catImgView;
        public ImageView arrowImageView;
        public AppCompatCheckBox serviceCheckbox;

        public ViewHolder(View view) {
            super(view);

            uberXCatNameTxtView = (MTextView) view.findViewById(R.id.uberXCatNameTxtView);
            contentArea = view.findViewById(R.id.contentArea);
            catImgView = (SelectableRoundedImageView) view.findViewById(R.id.catImgView);
            arrowImageView = (ImageView) view.findViewById(R.id.arrowImageView);
            serviceCheckbox = (AppCompatCheckBox) view.findViewById(R.id.serviceCheckbox);

        }
    }

    public class BannerViewHolder extends RecyclerView.ViewHolder {


        public CardView containerView;
        public View bannerAreaContainerView;
        public View seperatorView;
        public ImageView bannerImgView;
        public MTextView bookNowTxt, serviceNameTxt;
        ImageView bookNowImg;

        public BannerViewHolder(View view) {
            super(view);
            containerView = view.findViewById(R.id.containerView);
            bannerImgView = view.findViewById(R.id.bannerImgView);
            bookNowTxt = (MTextView) view.findViewById(R.id.bookNowTxt);
            serviceNameTxt = (MTextView) view.findViewById(R.id.serviceNameTxt);
            bannerAreaContainerView = view.findViewById(R.id.bannerAreaContainerView);
            seperatorView = view.findViewById(R.id.seperatorView);
            bookNowImg = view.findViewById(R.id.bookNowImg);
        }
    }


}
