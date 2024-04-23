package com.adapter.files;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.leadryde.userapp.R;
import com.general.files.GeneralFunctions;
import com.squareup.picasso.Picasso;
import com.utils.Utils;
import com.view.MTextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by tarwindersingh on 06/01/18.
 */

public class DaynamicCategoryAdapter extends RecyclerView.Adapter<DaynamicCategoryAdapter.ViewHolder> {

    GeneralFunctions generalFunc;
    ArrayList<HashMap<String, String>> listData;
    Context mContext;
    OnCategorySelectListener onCategorySelectListener;
    int isPosselected = 0;


    public DaynamicCategoryAdapter(GeneralFunctions generalFunc, ArrayList<HashMap<String, String>> listData, Context mContext) {
        this.generalFunc = generalFunc;
        this.listData = listData;
        this.mContext = mContext;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_daynamic_category_item, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }


    public void setCategorySelectListener(OnCategorySelectListener onCategorySelectListener) {
        this.onCategorySelectListener = onCategorySelectListener;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {


        holder.categoryTxt.setText(listData.get(position).get("vCategoryName"));
        holder.categoryTxt.setTextColor(Color.parseColor(listData.get(position).get("vTextColor")));

        Picasso.get().load(listData.get(position).get("vBgImage")).into(holder.categoryBG);

        holder.categoryTxt.setText(listData.get(position).get("vCategoryName"));
        if (position == isPosselected) {
            holder.categoryArea.setBackgroundResource(R.drawable.bg_services_selector);
        } else {
            holder.categoryArea.setBackgroundResource(0);

        }

        if (generalFunc.isRTLmode()) {
            Picasso.get().load(listData.get(position).get("vIconImage")).into(holder.categoryRTLImgView);
            holder.categoryRTLImgView.setVisibility(View.VISIBLE);
            holder.categoryImgView.setVisibility(View.GONE);
            holder.categoryImgView.setRotationY(180);

        } else {
            Picasso.get().load(listData.get(position).get("vIconImage")).into(holder.categoryImgView);
            holder.categoryImgView.setVisibility(View.VISIBLE);
            holder.categoryRTLImgView.setVisibility(View.GONE);
        }


        holder.categoryArea.setOnClickListener(view -> {

            if (onCategorySelectListener != null) {
                isPosselected = position;
                onCategorySelectListener.onCategorySelect(position);
                notifyDataSetChanged();
            }
        });

    }

    // Return the size of your itemsData (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return listData.size();
    }


    public interface OnCategorySelectListener {
        void onCategorySelect(int pos);
    }

    // inner class to hold a reference to each item of RecyclerView
    public class ViewHolder extends RecyclerView.ViewHolder {


        private MTextView categoryTxt;
        private ImageView categoryImgView, categoryRTLImgView;
        private ImageView categoryBG;
        private CardView cardview;
        private View categoryArea;

        public ViewHolder(View view) {
            super(view);


            categoryTxt = (MTextView) view.findViewById(R.id.categoryTxt);
            categoryImgView = (ImageView) view.findViewById(R.id.categoryImgView);
            categoryRTLImgView = (ImageView) view.findViewById(R.id.categoryRTLImgView);
            categoryBG = (ImageView) view.findViewById(R.id.categoryBG);
            categoryArea = (View) view.findViewById(R.id.categoryArea);


        }
    }
}
