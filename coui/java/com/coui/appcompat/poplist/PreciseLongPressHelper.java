package com.coui.appcompat.poplist;

import android.view.MotionEvent;
import android.view.View;

public class PreciseLongPressHelper {
    private final float[] mLastTouchDownXY = new float[2];
    private OnPreciseLongClickListener mOnPreciseLongClickListener;
    private View mTarget;
    private View.OnTouchListener mTouchListenerTransfer;
    private final View.OnTouchListener mOnTouchListener = (view, event) -> {
        if (mTouchListenerTransfer != null) {
            mTouchListenerTransfer.onTouch(view, event);
        }
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            mLastTouchDownXY[0] = event.getX();
            mLastTouchDownXY[1] = event.getY();
        }
        return false;
    };
    private final View.OnLongClickListener mOnLongClickListener = view -> {
        mOnPreciseLongClickListener.onLongClick(view, Math.round(mLastTouchDownXY[0]), Math.round(mLastTouchDownXY[1]));
        return true;
    };

    public interface OnPreciseLongClickListener {
        void onLongClick(View view, int x, int y);
    }

    public PreciseLongPressHelper(View view, OnPreciseLongClickListener listener) {
        mTarget = view;
        mOnPreciseLongClickListener = listener;
    }

    public void setTouchListenerTransfer(View.OnTouchListener listener) {
        mTouchListenerTransfer = listener;
    }

    public void setup() {
        mTarget.setOnTouchListener(mOnTouchListener);
        mTarget.setOnLongClickListener(mOnLongClickListener);
    }
}
