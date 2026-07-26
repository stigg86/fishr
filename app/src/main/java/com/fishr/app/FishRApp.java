package com.fishr.app;

import android.app.Application;
import android.content.Context;

public class FishRApp extends Application {
    private static FishRApp instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static Context getContext() {
        return instance;
    }
}
