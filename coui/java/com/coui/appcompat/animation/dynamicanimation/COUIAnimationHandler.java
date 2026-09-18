package com.coui.appcompat.animation.dynamicanimation;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.Choreographer;

import java.util.ArrayList;
import java.util.HashMap;

class COUIAnimationHandler {
    private static final long FRAME_DELAY_MS = 10L;
    private static final ThreadLocal<COUIAnimationHandler> sAnimatorHandler = new ThreadLocal<>();

    private final HashMap<AnimationFrameCallback, Long> mDelayedCallbackStartTime = new HashMap<>();
    final ArrayList<AnimationFrameCallback> mAnimationCallbacks = new ArrayList<>();
    private final AnimationCallbackDispatcher mCallbackDispatcher = new AnimationCallbackDispatcher();
    private AnimationFrameCallbackProvider mProvider;
    long mCurrentFrameTime;
    private boolean mListDirty;

    class AnimationCallbackDispatcher {
        void dispatchAnimationFrame() {
            mCurrentFrameTime = SystemClock.uptimeMillis();
            doAnimationFrame(mCurrentFrameTime);
            if (mAnimationCallbacks.size() > 0) {
                getProvider().postFrameCallback();
            }
        }
    }

    interface AnimationFrameCallback {
        boolean doAnimationFrame(long frameTime);
    }

    abstract static class AnimationFrameCallbackProvider {
        final AnimationCallbackDispatcher mDispatcher;

        AnimationFrameCallbackProvider(AnimationCallbackDispatcher dispatcher) {
            mDispatcher = dispatcher;
        }

        abstract void postFrameCallback();
    }

    static class FrameCallbackProvider14 extends AnimationFrameCallbackProvider {
        private final Handler mHandler;
        private final Runnable mRunnable;
        long mLastFrameTime = -1L;

        FrameCallbackProvider14(AnimationCallbackDispatcher dispatcher) {
            super(dispatcher);
            mRunnable = () -> {
                mLastFrameTime = SystemClock.uptimeMillis();
                mDispatcher.dispatchAnimationFrame();
            };
            mHandler = new Handler(Looper.myLooper());
        }

        @Override
        void postFrameCallback() {
            long delay = Math.max(FRAME_DELAY_MS - (SystemClock.uptimeMillis() - mLastFrameTime), 0L);
            mHandler.postDelayed(mRunnable, delay);
        }
    }

    static class FrameCallbackProvider16 extends AnimationFrameCallbackProvider {
        private final Choreographer mChoreographer;
        private final Choreographer.FrameCallback mChoreographerCallback;

        FrameCallbackProvider16(AnimationCallbackDispatcher dispatcher) {
            super(dispatcher);
            mChoreographer = Choreographer.getInstance();
            mChoreographerCallback = frameTimeNanos -> mDispatcher.dispatchAnimationFrame();
        }

        @Override
        void postFrameCallback() {
            mChoreographer.postFrameCallback(mChoreographerCallback);
        }
    }

    static long getFrameTime() {
        COUIAnimationHandler handler = sAnimatorHandler.get();
        return handler == null ? 0L : handler.mCurrentFrameTime;
    }

    static COUIAnimationHandler getInstance() {
        COUIAnimationHandler handler = sAnimatorHandler.get();
        if (handler == null) {
            handler = new COUIAnimationHandler();
            sAnimatorHandler.set(handler);
        }
        return handler;
    }

    private boolean isCallbackDue(AnimationFrameCallback callback, long currentTime) {
        Long startTime = mDelayedCallbackStartTime.get(callback);
        if (startTime == null) {
            return true;
        }
        if (startTime >= currentTime) {
            return false;
        }
        mDelayedCallbackStartTime.remove(callback);
        return true;
    }

    void addAnimationFrameCallback(AnimationFrameCallback callback, long delay) {
        if (mAnimationCallbacks.size() == 0) {
            getProvider().postFrameCallback();
        }
        if (!mAnimationCallbacks.contains(callback)) {
            mAnimationCallbacks.add(callback);
        }
        if (delay > 0L) {
            mDelayedCallbackStartTime.put(callback, SystemClock.uptimeMillis() + delay);
        }
    }

    void doAnimationFrame(long frameTime) {
        long currentTime = SystemClock.uptimeMillis();
        for (int i = 0; i < mAnimationCallbacks.size(); i++) {
            AnimationFrameCallback callback = mAnimationCallbacks.get(i);
            if (callback != null && isCallbackDue(callback, currentTime)) {
                callback.doAnimationFrame(frameTime);
            }
        }
        cleanUpList();
    }

    AnimationFrameCallbackProvider getProvider() {
        if (mProvider == null) {
            mProvider = new FrameCallbackProvider16(mCallbackDispatcher);
        }
        return mProvider;
    }

    void removeCallback(AnimationFrameCallback callback) {
        mDelayedCallbackStartTime.remove(callback);
        int index = mAnimationCallbacks.indexOf(callback);
        if (index >= 0) {
            mAnimationCallbacks.set(index, null);
            mListDirty = true;
        }
    }

    void setProvider(AnimationFrameCallbackProvider provider) {
        mProvider = provider;
    }

    private void cleanUpList() {
        if (!mListDirty) {
            return;
        }
        for (int i = mAnimationCallbacks.size() - 1; i >= 0; i--) {
            if (mAnimationCallbacks.get(i) == null) {
                mAnimationCallbacks.remove(i);
            }
        }
        mListDirty = false;
    }
}
