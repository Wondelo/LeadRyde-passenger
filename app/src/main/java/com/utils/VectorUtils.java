package com.utils;

import android.content.Context;
import android.os.Build;
import android.view.View;

import androidx.core.content.ContextCompat;

public class VectorUtils {
    public static void manageVectorImage(Context context, View view, int orgResId, int compactResId) {
        final int sdk = Build.VERSION.SDK_INT;
        if (sdk < Build.VERSION_CODES.M) {
            view.setBackgroundDrawable(ContextCompat.getDrawable(context, compactResId));
        } else {
            view.setBackground(ContextCompat.getDrawable(context, orgResId));
        }
    }
}
