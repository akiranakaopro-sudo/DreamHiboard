package com.coui.appcompat.uiutil;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

public abstract class COUIWorkHandler {
    public static final int TYPE_AUDIO = 1;
    public static final int TYPE_DEFAULT = 0;

    private static COUIWorkHandler sAudioInstance;
    private static COUIWorkHandler sDefaultInstance;

    private Handler mHandler;
    private final HandlerThread mHandlerThread;

    public static class COUIAudioWorkHandler extends COUIWorkHandler {
        private static final String TAG = "COUIAudioWorkHandler";

        private COUIAudioWorkHandler() {
            super();
        }

        @Override
        public HandlerThread newHandlerInstance() {
            return new HandlerThread(TAG, -16);
        }
    }

    public static class COUIDefaultWorkHandler extends COUIWorkHandler {
        private static final String TAG = "COUIDefaultWorkHandler";

        private COUIDefaultWorkHandler() {
            super();
        }

        @Override
        public HandlerThread newHandlerInstance() {
            return new HandlerThread(TAG, 0);
        }
    }

    private COUIWorkHandler() {
        HandlerThread handlerThread = newHandlerInstance();
        mHandlerThread = handlerThread;
        handlerThread.start();
    }

    private void checkMainThread() {
        if (Looper.myLooper() != null && Looper.myLooper() != Looper.getMainLooper()) {
            throw new RuntimeException("Current thread is not origin thread!");
        }
    }

    private void ensureHandler() {
        if (getHandler() != null || getHandlerThread().getLooper() == null) {
            return;
        }
        setHandler(new Handler(getHandlerThread().getLooper()));
    }

    public static COUIWorkHandler getInstance() {
        return getInstance(TYPE_DEFAULT);
    }

    public static COUIWorkHandler getInstance(int type) {
        if (type == TYPE_AUDIO) {
            if (sAudioInstance == null) {
                sAudioInstance = new COUIAudioWorkHandler();
            }
            return sAudioInstance;
        }
        if (sDefaultInstance == null) {
            sDefaultInstance = new COUIDefaultWorkHandler();
        }
        return sDefaultInstance;
    }

    public Handler getHandler() {
        return mHandler;
    }

    public HandlerThread getHandlerThread() {
        return mHandlerThread;
    }

    public abstract HandlerThread newHandlerInstance();

    public void setHandler(Handler handler) {
        mHandler = handler;
    }

    public void start(Runnable runnable) {
        checkMainThread();
        ensureHandler();
        getHandler().post(runnable);
    }

    public void startDelay(Runnable runnable, Long delay) {
        checkMainThread();
        ensureHandler();
        getHandler().postDelayed(runnable, delay);
    }

    public void startDelay(Runnable runnable, Object token, Long delay) {
        checkMainThread();
        ensureHandler();
        getHandler().postDelayed(runnable, token, delay);
    }
}
