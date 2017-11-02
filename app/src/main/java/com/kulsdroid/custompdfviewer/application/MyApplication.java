package com.kulsdroid.custompdfviewer.application;

import android.app.Application;
import android.content.Context;

/**
 * Created by KulsDroid on 11/2/2017.
 */

public class MyApplication extends Application {

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
    }

    public static Context getContext(){
        return mContext;
    }
}
