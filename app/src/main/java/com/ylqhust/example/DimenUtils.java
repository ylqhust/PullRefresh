package com.ylqhust.example;

import android.content.res.Resources;

/**
 * Created by apple on 15/12/11.
 */
public class DimenUtils {
    public static final float density = Resources.getSystem().getDisplayMetrics().density;

    public static int Dp2Px(float dp){
        return (int) (dp*density+0.5f);
    }

    public static int Px2Dp(float px){
        return (int) (px/density + 0.5f);
    }
}
