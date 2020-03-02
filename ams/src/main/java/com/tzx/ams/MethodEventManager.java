package com.tzx.ams;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Tanzhenxing
 * Date: 2020-01-15 16:31
 * Description:
 */
public class MethodEventManager {

    private static volatile MethodEventManager INSTANCE;
    private Map<String, List<MethodObserver>> mObserverMap = new HashMap<>();

    private MethodEventManager() {
    }

    public static MethodEventManager getInstance() {
        if (INSTANCE == null) {
            synchronized (MethodEventManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new MethodEventManager();
                }
            }
        }
        return INSTANCE;
    }

    public void registerMethodObserver(String tag, MethodObserver listener) {
        if (listener == null) {
            return;
        }

        List<MethodObserver> listeners = mObserverMap.get(tag);
        if (listeners == null) {
            listeners = new ArrayList<>();
        }
        listeners.add(listener);
        mObserverMap.put(tag, listeners);
    }

    public void notifyMethodEnter(String tag, String methodName) {
        List<MethodObserver> listeners = mObserverMap.get(tag);
        if (listeners == null) {
            return;
        }
        for (MethodObserver listener : listeners) {
            listener.onMethodEnter(tag, methodName);
        }
    }

    public void notifyMethodExit(String tag, String methodName) {
        List<MethodObserver> listeners = mObserverMap.get(tag);
        if (listeners == null) {
            return;
        }
        for (MethodObserver listener : listeners) {
            listener.onMethodExit(tag, methodName);
        }
    }
}
