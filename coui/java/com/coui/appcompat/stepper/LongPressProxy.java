package com.coui.appcompat.stepper;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.view.GestureDetectorCompat;

class LongPressProxy {
    public static final int CLICK_EVENT = 1;
    public static final int LONG_CLICK_EVENT = 2;

    private GestureDetectorCompat mGestureDetectorCompat;
    private Handler mHandler = new MyHandler(Looper.getMainLooper());
    private GestureDetector.OnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public void onLongPress(MotionEvent event) {
            super.onLongPress(event);
            mHandler.sendEmptyMessage(LONG_CLICK_EVENT);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            mHandler.sendEmptyMessage(CLICK_EVENT);
            return true;
        }
    };
    private View.OnTouchListener mOnTouchListener;
    private Runnable mRunnable;
    private View mView;

    public LongPressProxy(View view, Runnable runnable) {
        mView = view;
        mRunnable = runnable;
        mGestureDetectorCompat = new GestureDetectorCompat(mView.getContext(), mGestureListener);
        init();
    }

    @SuppressLint({"ClickableViewAccessibility"})
    private void init() {
        mView.setOnTouchListener((view, event) -> {
            View.OnTouchListener onTouchListener = mOnTouchListener;
            if (onTouchListener != null) {
                onTouchListener.onTouch(view, event);
            }
            mGestureDetectorCompat.onTouchEvent(event);
            if (event.getActionMasked() == MotionEvent.ACTION_CANCEL
                    || event.getActionMasked() == MotionEvent.ACTION_UP) {
                mHandler.removeMessages(LONG_CLICK_EVENT);
            }
            return true;
        });
    }

    public void release() {
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
        View view = mView;
        if (view != null) {
            view.setOnTouchListener(null);
            mView.removeCallbacks(mRunnable);
            mView = null;
        }
        mRunnable = null;
        mOnTouchListener = null;
    }

    public void setOnTouchListener(View.OnTouchListener listener) {
        mOnTouchListener = listener;
    }

    public class MyHandler extends Handler {
        public MyHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message message) {
            super.handleMessage(message);
            int what = message.what;
            if (what == CLICK_EVENT) {
                mRunnable.run();
            } else if (what == LONG_CLICK_EVENT && mView.isEnabled()) {
                mRunnable.run();
                sendEmptyMessageDelayed(LONG_CLICK_EVENT, 100L);
            }
        }
    }
}
