package com.example.android.customview;

import android.app.Application;
import android.content.Context;

public  class App extends Application {
    private static Context mContext;
    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static Context getContext() {
        return mContext;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        mContext=base;

    }
}
