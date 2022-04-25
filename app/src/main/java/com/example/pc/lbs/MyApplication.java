package com.example.pc.lbs;

import android.app.Application;
import android.content.Context;

/**
 * 主Application
 */
public class MyApplication extends Application {

    //全局获取context
    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();

        sContext = getApplicationContext();
    }

    //全局获取context
    public static Context getContext() {
        return sContext;
    }

}
