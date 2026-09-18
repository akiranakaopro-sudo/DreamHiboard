package com.coui.appcompat.springchain;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import com.coui.appcompat.scrollview.COUIScrollView;
import com.coui.appcompat.uiutil.UIUtil;

import java.util.Objects;


@SuppressLint({"LongLogTag"})
public final class COUIGridSpringChainScrollView extends COUIScrollView {
    private static final boolean DEBUG = true;
    private static final float MAX_DISTANCE_FACTOR = 0.25f;
    private static final float MAX_DISTANCE_RATIO = 0.5f;
    private static final float OVER_SCROLL_FACTOR = 0.3f;
    private static final String TAG = "EdgeSpringChainScrollView";
    private float curDistance;
    private float downY;
    private ICOUIGridSpringChainViewGroup edgeSpringChainViewGroup;
    private int hasReleaseSpring;
    private float inheritDistance;
    private boolean shouldUpdateDownY;

    public COUIGridSpringChainScrollView(Context context) {
        this(context, null);
    }

    private float getFormalDistance(float distance, float previousDistance) {
        if (previousDistance * distance <= 0.0f) {
            return distance;
        }
        ICOUIGridSpringChainViewGroup viewGroup = edgeSpringChainViewGroup;
        float lastTranslationY = viewGroup != null ? viewGroup.getLastTranslationY() : 0.0f;
        float maxDistance = Math.min(
                Math.max(((getHeight() * MAX_DISTANCE_RATIO) - Math.abs(lastTranslationY))
                        / MAX_DISTANCE_FACTOR, 0.0f),
                (getHeight() * MAX_DISTANCE_RATIO) / MAX_DISTANCE_FACTOR);
        float factor = Math.min(Math.max(1 - Math.abs(previousDistance / maxDistance), 0.0f),
                1.0f);
        return (factor * (distance - previousDistance)) + previousDistance;
    }

    private boolean isReachBottomEdge() {
        View childAt = getChildAt(0);
        return childAt != null && getScrollY() + getHeight() >= childAt.getMeasuredHeight();
    }

    private boolean isReachTopEdge() {
        return getScrollY() <= 0;
    }

    private boolean onEdgeSpringEvent(MotionEvent event) {
        int pointerIndex = UIUtil.getAdjustmentPointerIndex(event, event.getActionIndex());
        float rawY = event.getRawY(pointerIndex);
        int action = event.getAction() & 255;
        if (action == MotionEvent.ACTION_DOWN) {
            this.downY = rawY;
            this.curDistance = 0.0f;
            this.inheritDistance = 0.0f;
            Integer runningDirection = null;
            ICOUIGridSpringChainViewGroup viewGroup = this.edgeSpringChainViewGroup;
            if (viewGroup != null) {
                runningDirection = Integer.valueOf(viewGroup.isSpringSystemRunning());
            }
            this.hasReleaseSpring = runningDirection != null ? runningDirection.intValue() : 0;
            if (this.hasReleaseSpring != 0) {
                ICOUIGridSpringChainViewGroup releaseViewGroup = this.edgeSpringChainViewGroup;
                if (releaseViewGroup != null) {
                    releaseViewGroup.releaseSpring();
                }
            }
            Log.d(TAG, "onEdgeSpringEvent : ACTION_DOWN : hasReleaseSpring=:" + this.hasReleaseSpring);
            return false;
        }
        if (action == MotionEvent.ACTION_POINTER_DOWN || action == MotionEvent.ACTION_POINTER_UP) {
            this.inheritDistance = this.curDistance;
            this.shouldUpdateDownY = true;
            Log.d(TAG, "onEdgeSpringEvent : ACTION_POINTER_DOWN/UP : inheritDistance=:"
                    + this.inheritDistance);
            return false;
        }
        if (action == MotionEvent.ACTION_MOVE) {
            if (this.shouldUpdateDownY) {
                this.downY = rawY;
                this.shouldUpdateDownY = false;
            }
            this.curDistance = getFormalDistance((rawY - this.downY) + this.inheritDistance,
                    this.curDistance);
            if (this.curDistance > 0.0f && isReachTopEdge()) {
                ICOUIGridSpringChainViewGroup viewGroup = this.edgeSpringChainViewGroup;
                if (viewGroup != null) {
                    viewGroup.updateMoveTranslation(this.curDistance, COUIGridSpringChain.BACK_TO_TOP);
                }
                return true;
            }
            if (this.curDistance < 0.0f && isReachBottomEdge()) {
                ICOUIGridSpringChainViewGroup viewGroup = this.edgeSpringChainViewGroup;
                if (viewGroup != null) {
                    viewGroup.updateMoveTranslation(this.curDistance, COUIGridSpringChain.BACK_TO_BOTTOM);
                }
                return true;
            }
            if (this.hasReleaseSpring != 0) {
                this.downY = rawY;
                ICOUIGridSpringChainViewGroup viewGroup = this.edgeSpringChainViewGroup;
                if (viewGroup != null) {
                    viewGroup.updateMoveTranslation(0.0f, 0);
                }
            } else {
                this.downY = rawY;
            }
            return false;
        }
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            this.curDistance = getFormalDistance((rawY - this.downY) + this.inheritDistance,
                    this.curDistance);
            Log.d(TAG, "onEdgeSpringEvent : ACTION_UP/CANCEL : curDistance=:" + this.curDistance
                    + " ,isReachTopEdge()=:" + isReachTopEdge()
                    + " ,isReachBottomEdge()=:" + isReachBottomEdge()
                    + " ,hasReleaseSpring=:" + this.hasReleaseSpring);
            if (this.curDistance > 0.0f && isReachTopEdge()) {
                ICOUIGridSpringChainViewGroup viewGroup = this.edgeSpringChainViewGroup;
                if (viewGroup != null) {
                    viewGroup.startRebound(COUIGridSpringChain.BACK_TO_TOP);
                }
                return true;
            }
            if (this.curDistance < 0.0f && isReachBottomEdge()) {
                ICOUIGridSpringChainViewGroup viewGroup = this.edgeSpringChainViewGroup;
                if (viewGroup != null) {
                    viewGroup.startRebound(COUIGridSpringChain.BACK_TO_BOTTOM);
                }
                return true;
            }
            if (this.hasReleaseSpring != 0) {
                this.downY = rawY;
                ICOUIGridSpringChainViewGroup viewGroup = this.edgeSpringChainViewGroup;
                if (viewGroup != null) {
                    viewGroup.startRebound(0);
                }
            } else {
                this.downY = rawY;
            }
        }
        return false;
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() <= 0 || !(getChildAt(0) instanceof ICOUIGridSpringChainViewGroup)) {
            return;
        }
        edgeSpringChainViewGroup = (ICOUIGridSpringChainViewGroup) getChildAt(0);
    }

    @Override
    @SuppressLint({"ClickableViewAccessibility"})
    public boolean onTouchEvent(MotionEvent ev) {
        Objects.requireNonNull(ev, "ev");
        onEdgeSpringEvent(ev);
        return super.onTouchEvent(ev);
    }

    public COUIGridSpringChainScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Objects.requireNonNull(context, "context");
        setEnableVibrator(false);
        setCustomOverScrollDistFactor(OVER_SCROLL_FACTOR);
        setOverScrollMode(OVER_SCROLL_ALWAYS);
    }
}
