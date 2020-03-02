package com.tzx.ams;

/**
 * Created by Tanzhenxing
 * Date: 2020-01-15 16:34
 * Description:
 */
public interface MethodObserver {
    default void onMethodEnter(String tag, String methodName) {}
    default void onMethodExit(String tag, String methodName) {}
}
