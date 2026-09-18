package com.coui.appcompat.poplist;

import android.view.MotionEvent;
import android.view.View;

import com.coui.appcompat.talkbackutil.COUIAccessibilityUtil;

public class PreciseClickHelper {
    private final float[] mLastTouchDownXY = new float[2];
    private OnPreciseClickListener mOnPreciseClickListener;
    private View mTarget;
    private final View.OnClickListener mOnClickListener = view -> {
        if (COUIAccessibilityUtil.isTalkbackEnabled(view.getContext())
                || (mLastTouchDownXY[0] == 0.0f && mLastTouchDownXY[1] == 0.0f)) {
            mOnPreciseClickListener.onClick(view, view.getWidth() / 2, view.getHeight() / 2);
        } else {
            mOnPreciseClickListener.onClick(view, Math.round(mLastTouchDownXY[0]), Math.round(mLastTouchDownXY[1]));
        }
    };
    private final View.OnTouchListener mOnTouchListener = (view, event) -> {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            mLastTouchDownXY[0] = event.getX();
            mLastTouchDownXY[1] = event.getY();
        }
        return false;
    };

    public interface OnPreciseClickListener {
        void onClick(View view, int x, int y);
    }

    public PreciseClickHelper(View view, OnPreciseClickListener listener) {
        mTarget = view;
        mOnPreciseClickListener = listener;
    }

    public View getTargetView() {
        return mTarget;
    }

    public void setup() {
        mTarget.setOnTouchListener(mOnTouchListener);
        mTarget.setOnClickListener(mOnClickListener);
    }

    public void unSet() {
        mTarget.setOnClickListener(null);
        mTarget.setOnTouchListener(null);
    }
}
