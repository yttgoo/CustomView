package com.example.android.customview.utils;

import com.example.android.customview.App;

public class Utils {
    public static int px2dp(int pxValue) {
        float scale = App.getContext().getResources().getDisplayMetrics().density;
        return (int) ((pxValue / scale + 0.5f));
    }

    public static int dp2px(int dpValue) {
        final float scale = App.getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
