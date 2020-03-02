package com.tzx.amsdemo;

import android.app.Application;

import com.tzx.ams.MethodEventManager;

/**
 * Created by Tanzhenxing
 * Date: 2020-02-27 18:53
 * Description:
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        MethodEventManager.getInstance().registerMethodObserver("TEST", new TimeObserver());
    }
}
