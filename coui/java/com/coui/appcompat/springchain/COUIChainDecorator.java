package com.coui.appcompat.springchain;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import com.coui.appcompat.springchain.COUIGridSpringChain;
import com.coui.appcompat.springchain.api.ICheckViewEdge;
import com.coui.appcompat.uiutil.UIUtil;

import java.util.Objects;


public final class COUIChainDecorator {
    private static final boolean DEBUG = true;
    private static final int DEFAULT_MARGIN_VERTICAL = 24;
    private static final float FACTOR = 0.3f;
    private static final float FACTOR_RATIO = 0.2f;
    private static final float FACTOR_RATIO_1 = 0.6f;
    private static final float FACTOR_RATIO_2 = 0.75f;
    private static final float FACTOR_RATIO_3 = 0.8f;
    private static final int INDEX_0 = 0;
    private static final int INDEX_1 = 1;
    private static final int INDEX_2 = 2;
    private static final int INDEX_3 = 3;
    private static final int INDEX_4 = 4;
    private static final int INDEX_5 = 5;
    private static final float MAX_DISTANCE_FACTOR = 0.25f;
    private static final float MAX_DISTANCE_RATIO = 0.5f;
    private static final float OVER_SCROLL_FACTOR = 0.3f;
    private static final String TAG = "ChainDecorator";
    private final COUIGridSpringChain.TransCalculator backToTop;
    private ICheckViewEdge checkEdge;
    private float curDistance;
    private int currentMarginVertical;
    private float downY;
    private float factor;
    private int hasReleaseSpring;
    private float inheritDistance;
    private final COUIGridSpringChain scrollGridChain;
    private boolean shouldUpdateDownY;

    public COUIChainDecorator(COUIGridSpringChain scrollGridChain) {
        this.scrollGridChain = Objects.requireNonNull(scrollGridChain, "scrollGridChain");
        this.factor = FACTOR;
        this.currentMarginVertical = DEFAULT_MARGIN_VERTICAL;
        this.backToTop = new COUIGridSpringChain.TransCalculator() {
            @Override
            public float getTrans(int index, float distance, int direction) {
                float translation;
                if (index != INDEX_0) {
                    float adjustedDistance;
                    if (index == INDEX_1) {
                        adjustedDistance = distance * COUIChainDecorator.this.factor
                                * FACTOR_RATIO * FACTOR_RATIO_1;
                    } else if (index == INDEX_2) {
                        adjustedDistance = distance * COUIChainDecorator.this.factor
                                * FACTOR_RATIO;
                    } else if (index == INDEX_3) {
                        adjustedDistance = distance * COUIChainDecorator.this.factor
                                * FACTOR_RATIO * FACTOR_RATIO_1 * FACTOR_RATIO_3;
                    } else if (index == INDEX_4) {
                        adjustedDistance = distance * COUIChainDecorator.this.factor
                                * FACTOR_RATIO * FACTOR_RATIO_2;
                    } else if (index != INDEX_5) {
                        adjustedDistance = distance * COUIChainDecorator.this.factor
                                * FACTOR_RATIO
                                * ((float) Math.pow(FACTOR_RATIO_2, index - INDEX_3));
                    } else {
                        adjustedDistance = distance * COUIChainDecorator.this.factor
                                * FACTOR_RATIO * FACTOR_RATIO_2 * FACTOR_RATIO_2;
                    }
                    translation = adjustedDistance - COUIChainDecorator.this.currentMarginVertical;
                } else {
                    translation = distance * COUIChainDecorator.this.factor;
                }
                return direction != COUIGridSpringChain.BACK_TO_TOP
                        ? direction != COUIGridSpringChain.BACK_TO_BOTTOM
                                ? translation
                                : Math.min(translation, 0.0f)
                        : Math.max(translation, 0.0f);
            }
        };
        scrollGridChain.setTranCalculator(COUIGridSpringChain.BACK_TO_TOP);
        scrollGridChain.setTranCalculator(COUIGridSpringChain.BACK_TO_BOTTOM);
    }

    private float getFormalDistance(float distance, float previousDistance) {
        if (previousDistance * distance <= 0.0f) {
            return distance;
        }
        float maxDistance = Math.min(
                Math.max(((getHeight() * MAX_DISTANCE_RATIO)
                        - Math.abs(scrollGridChain.getLastTranslationY()))
                        / MAX_DISTANCE_FACTOR, 0.0f),
                (getHeight() * MAX_DISTANCE_RATIO) / MAX_DISTANCE_FACTOR);
        float factor = Math.min(Math.max(1 - Math.abs(previousDistance / maxDistance), 0.0f),
                1.0f);
        return (factor * (distance - previousDistance)) + previousDistance;
    }

    private boolean isReachBottomEdge() {
        ICheckViewEdge iCheckViewEdge = this.checkEdge;
        return iCheckViewEdge != null && iCheckViewEdge.isReachBottomEdge();
    }

    private boolean isReachTopEdge() {
        ICheckViewEdge iCheckViewEdge = this.checkEdge;
        return iCheckViewEdge != null && iCheckViewEdge.isReachTopEdge();
    }

    public final COUIGridSpringChain.TransCalculator getBackToTop() {
        return this.backToTop;
    }

    public final ICheckViewEdge getCheckEdge() {
        return this.checkEdge;
    }

    public final int getHeight() {
        ICheckViewEdge iCheckViewEdge = this.checkEdge;
        if (iCheckViewEdge != null) {
            return iCheckViewEdge.getHeight();
        }
        return 0;
    }

    public final COUIGridSpringChain getScrollGridChain() {
        return this.scrollGridChain;
    }

    @SuppressLint({"ClickableViewAccessibility"})
    public final void initEdgeCheck(final ViewGroup scrollView, final ViewGroup container, boolean flag) {
        Objects.requireNonNull(scrollView, "scrollView");
        Objects.requireNonNull(container, "container");
        this.checkEdge = new ICheckViewEdge() {
            @Override
            public int getHeight() {
                return scrollView.getHeight();
            }

            @Override
            public boolean isReachBottomEdge() {
                return scrollView.getScrollY() + scrollView.getHeight() >= container.getMeasuredHeight();
            }

            @Override
            public boolean isReachTopEdge() {
                return scrollView.getScrollY() <= 0;
            }
        };
        if (flag) {
            scrollView.setOnTouchListener((view, event) -> {
                onEdgeSpringEvent(Objects.requireNonNull(event, "event"));
                return false;
            });
        }
    }

    public final boolean onEdgeSpringEvent(MotionEvent event) {
        Objects.requireNonNull(event, "event");
        int pointerIndex = UIUtil.getAdjustmentPointerIndex(event, event.getActionIndex());
        float rawY = event.getRawY(pointerIndex);
        int action = event.getAction() & 255;
        if (action == MotionEvent.ACTION_DOWN) {
            this.downY = rawY;
            this.curDistance = 0.0f;
            this.inheritDistance = 0.0f;
            int runningDirection = this.scrollGridChain.isSpringSystemRunning();
            this.hasReleaseSpring = runningDirection;
            if (runningDirection != 0) {
                this.scrollGridChain.releaseSpring();
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
                this.scrollGridChain.updateMoveTranslation(this.curDistance, COUIGridSpringChain.BACK_TO_TOP);
                return true;
            }
            if (this.curDistance < 0.0f && isReachBottomEdge()) {
                this.scrollGridChain.updateMoveTranslation(this.curDistance,
                        COUIGridSpringChain.BACK_TO_BOTTOM);
                return true;
            }
            if (this.hasReleaseSpring != 0) {
                this.downY = rawY;
                this.scrollGridChain.updateMoveTranslation(0.0f, 0);
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
                this.scrollGridChain.startRebound(COUIGridSpringChain.BACK_TO_TOP);
                return true;
            }
            if (this.curDistance < 0.0f && isReachBottomEdge()) {
                this.scrollGridChain.startRebound(COUIGridSpringChain.BACK_TO_BOTTOM);
                return true;
            }
            if (this.hasReleaseSpring != 0) {
                this.downY = rawY;
                this.scrollGridChain.startRebound(0);
            } else {
                this.downY = rawY;
            }
        }
        return false;
    }

    public final void setCheckEdge(ICheckViewEdge iCheckViewEdge) {
        this.checkEdge = iCheckViewEdge;
    }
}
