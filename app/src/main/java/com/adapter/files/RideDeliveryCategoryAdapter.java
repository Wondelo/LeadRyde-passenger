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
public class RideDeliveryCategoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {



    public GeneralFunctions generalFunc;
    ArrayList<HashMap<String, String>> list_item;
    Context mContext;
    OnItemClickList onItemClickList;

    boolean isForceToGrid = false;
    int positionOfSeperatorView = -1;
    String userProfileJson;

    int dimension;
    int width;
    int margin;
    int grid;
    boolean click = false;
    //added for manage Listing
    boolean isFirst = true;
    int isFirstPos=0;

    public RideDeliveryCategoryAdapter(Context mContext, ArrayList<HashMap<String, String>> list_item, GeneralFunctions generalFunc) {
        this.mContext = mContext;
        this.list_item = list_item;
        this.generalFunc = generalFunc;
        userProfileJson = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);


       // dimension = mContext.getResources().getDimensionPixelSize(R.dimen.category_grid_size);
        margin = mContext.getResources().getDimensionPixelSize(R.dimen.category_banner_left_right_margin);
       // grid = mContext.getResources().getDimensionPixelSize(R.dimen.category_grid_size);
      //  width = margin * 2;


    }

    public RideDeliveryCategoryAdapter(Context mContext, ArrayList<HashMap<String, String>> list_item, GeneralFunctions generalFunc, boolean isForceToGrid) {
        this.mContext = mContext;
        this.list_item = list_item;
        this.generalFunc = generalFunc;
        this.isForceToGrid = isForceToGrid;
        userProfileJson = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {



            View view = LayoutInflater.from(parent.getContext()).inflate( R.layout.item_ridedelivery_cat_grid_design , parent, false);
            ViewHolder viewHolder = new ViewHolder(view);
            return viewHolder;



    }



    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder parentViewHolder, final int position) {

        HashMap<String, String> item = list_item.get(position);

        String vCategory = item.get("vCategory");
        String vLogo_image = item.get("vImage");



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

           // String imageURL = Utils.getResizeImgURL(mContext, vLogo_image, grid, grid);



            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.mipmap.ic_no_icon);
//            requestOptions.error(R.drawable.ic_error);

            Glide.with(mContext).load(vLogo_image).apply(requestOptions).listener(new RequestListener<Drawable>() {
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

                        if (onItemClickList != null) {
                            onItemClickList.onItemClick(position);
                        }


            });




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

                    break;
                case MotionEvent.ACTION_UP:
                        if (list_item.size()>item_position && (list_item.get(item_position).get("LAST_CLICK_TIME") == null || (System.currentTimeMillis() - GeneralFunctions.parseLongValue(0, list_item.get(item_position).get("LAST_CLICK_TIME"))) > 1000)) {
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
                if (motionEvent == MotionEvent.ACTION_UP && list_item.size()>item_position) {
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




    public void setOnItemClickList(OnItemClickList onItemClickList) {
        this.onItemClickList = onItemClickList;
    }

    public interface OnItemClickList {
        void onItemClick(int position);

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




}
