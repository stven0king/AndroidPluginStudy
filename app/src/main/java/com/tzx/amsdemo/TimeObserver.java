package com.tzx.amsdemo;

import android.util.Log;

import com.tzx.ams.MethodObserver;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Tanzhenxing
 * Date: 2020-01-15 19:51
 * Description:
 */
public class TimeObserver implements MethodObserver {
    private static final String TAG_METHOD_TIME = "MethodCost";
    private Map<String, Long> enterTimeMap = new HashMap<>();

    @Override
    public void onMethodEnter(String tag, String methodName) {
        String key = generateKey(tag, methodName);
        Long time = System.currentTimeMillis();
        enterTimeMap.put(key, time);
    }

    @Override
    public void onMethodExit(String tag, String methodName) {
        String key = generateKey(tag, methodName);
        Long enterTime = enterTimeMap.get(key);
        if (enterTime == null) {
            throw new IllegalStateException("method exit without enter");
        }
        long cost = System.currentTimeMillis() - enterTime;
        Log.d(TAG_METHOD_TIME, "method " + methodName + " cost "
                + (double)cost + "ms" + " in thread " + Thread.currentThread().getName());
        enterTimeMap.remove(key);
    }

    private String generateKey(String tag, String methodName) {
        return tag + methodName + Thread.currentThread().getName();
    }
}
