package com.general.files;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;

import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.drawable.DrawableCompat;

import com.leadryde.userapp.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.utils.CommonUtilities;
import com.utils.Logger;
import com.utils.Utils;
import com.view.SelectableRoundedImageView;

import java.math.RoundingMode;
import java.text.NumberFormat;

import static android.text.TextUtils.isEmpty;

public class AppFunctions {
    Context mContext;
    GeneralFunctions generalFunc;

    public AppFunctions(Context mContext) {
        this.mContext = mContext;
        generalFunc = new GeneralFunctions(mContext);
    }

    public void checkProfileImage(SelectableRoundedImageView userProfileImgView, String userProfileJson, String imageKey) {
        String vImgName_str = generalFunc.getJsonValue(imageKey, userProfileJson);

        Picasso.get().load(CommonUtilities.USER_PHOTO_PATH + generalFunc.getMemberId() + "/" + vImgName_str).placeholder(R.mipmap.ic_no_pic_user).error(R.mipmap.ic_no_pic_user).into(userProfileImgView);
    }

    public void setOverflowButtonColor(final Toolbar toolbar, final int color) {
        Drawable drawable = toolbar.getOverflowIcon();
        if (drawable != null) {
            drawable = DrawableCompat.wrap(drawable);
            DrawableCompat.setTint(drawable.mutate(), color);
            toolbar.setOverflowIcon(drawable);
        }
    }
    public static String substringAfterLast(String str, String separator) {
        if (isEmpty(str)) {
            return str;
        }
        if (isEmpty(separator)) {
            return "";
        }
        int pos = str.lastIndexOf(separator);
        if (pos == -1 || pos == (str.length() - separator.length())) {
            return "";
        }
        return str.substring(pos + separator.length());
    }

    public void checkProfileImage(SelectableRoundedImageView userProfileImgView, String userProfileJson, String imageKey, ImageView profilebackimage) {
        String vImgName_str = generalFunc.getJsonValue(imageKey, userProfileJson);

        Picasso.get().load(CommonUtilities.USER_PHOTO_PATH + generalFunc.getMemberId() + "/" + vImgName_str).placeholder(R.mipmap.ic_no_pic_user).error(R.mipmap.ic_no_pic_user).into(userProfileImgView);

        Picasso.get().load(CommonUtilities.USER_PHOTO_PATH + generalFunc.getMemberId() + "/" + vImgName_str).placeholder(R.mipmap.ic_no_pic_user).error(R.mipmap.ic_no_pic_user).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                // Utils.setBlurImage(bitmap, profilebackimage);
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });
    }

    public static void runGAC() {
        System.gc();
        System.runFinalization();
        Runtime.getRuntime().gc();
        System.gc();
    }

    public int getViewHeightWidth(View view, boolean getHeight) {
        WindowManager wm = (WindowManager) view.getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        int deviceWidth;

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            Point size = new Point();
            display.getSize(size);
            deviceWidth = size.x;
        } else {
            deviceWidth = display.getWidth();
        }

        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(deviceWidth, View.MeasureSpec.AT_MOST);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(widthMeasureSpec, heightMeasureSpec);
        return !getHeight ? view.getMeasuredWidth() : view.getMeasuredHeight();
    }

    public void slideAnimView(View view, int currentHeight, int newHeight, long duration) {
        ValueAnimator slideAnimator = ValueAnimator.ofInt(currentHeight, newHeight).setDuration(duration);
        slideAnimator.addUpdateListener(animation1 -> {
            Integer value = (Integer) animation1.getAnimatedValue();
            view.getLayoutParams().height = value.intValue();
            view.getLayoutParams().width = value.intValue();
            view.requestLayout();
        });
        AnimatorSet animationSet = new AnimatorSet();
        animationSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animationSet.play(slideAnimator);
        animationSet.start();
    }

    public void slideAnimView(View view, View view1, int currentHeight, int newHeight, long duration) {
        ValueAnimator slideAnimator = ValueAnimator.ofInt(currentHeight, newHeight).setDuration(duration);
        slideAnimator.addUpdateListener(animation1 -> {
            Integer value = (Integer) animation1.getAnimatedValue();
            view.getLayoutParams().height = value.intValue();
            view.requestLayout();
            view1.getLayoutParams().height = value.intValue();
            view1.requestLayout();
        });
        AnimatorSet animationSet = new AnimatorSet();
        animationSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animationSet.play(slideAnimator);
        animationSet.start();
    }

    public static Spanned fromHtml(String html) {
        if (!Utils.checkText(html)) {
            // return an empty spannable if the html is null
            return new SpannableString("");
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // FROM_HTML_MODE_LEGACY is the behaviour that was used for versions below android N
            // we are using this flag to give a consistent behaviour
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(html);
        }
    }

    public boolean isEmailBlankAndOptional(GeneralFunctions generalFunc, String email) {
        return generalFunc.retrieveValue("ENABLE_EMAIL_OPTIONAL").equalsIgnoreCase("Yes") && !Utils.checkText(email);
    }

    public String formatNumAsPerCurrency(GeneralFunctions generalFunc, String formatString, String currencySymbol, boolean addCurrency) {

        if (generalFunc.retrieveValue("eReverseformattingEnable").equalsIgnoreCase("Yes")) {
            NumberFormat myFormat = NumberFormat.getInstance();
            myFormat.setGroupingUsed(true); // this will also round numbers, 3
            myFormat.setMinimumFractionDigits(2);
//          myFormat.setRoundingMode(RoundingMode.HALF_UP);
            // decimal places
            Logger.d("FORMATE", "formatString Initial>> " + "" + formatString);
            double data = generalFunc.parseDoubleValue(0, formatString);
            Logger.d("FORMATE", "formatString data>> " + "" + data);
            System.out.println("adding commas to number in Java using NumberFormat class");
            // https://www.java67.com/2015/06/how-to-format-numbers-in-java.html

            formatString = myFormat.format(data);
            System.out.println(myFormat.format(data));


            Logger.d("FORMATE", "formatString >> " + "" + formatString);
            String newStr = formatString.replace(",", "_").replace(".", ",").replace("_", ".");
            Logger.d("FORMATE", "formatString After >> " + "" + formatString);
            formatString = newStr;


        }

        if (addCurrency) {
            if (generalFunc.retrieveValue("eReverseSymbolEnable").equalsIgnoreCase("Yes")) {
                formatString = formatString + " " + currencySymbol;
            } else {
                formatString = currencySymbol + " " + formatString;
            }
        }

        return formatString;
    }

}
