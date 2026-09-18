package com.coui.appcompat.reddot;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.coui.appcompat.R;

public class COUIRedDotFrameLayout extends FrameLayout {
    public static final int CIRCLE_TYPE = 1;
    public static final int RECTANGLE_TYPE = 0;

    String TAG = "COUIRedDotFrameLayout";
    private View mAnchorView;
    private int mAnchorViewShapeType = RECTANGLE_TYPE;
    private int mAnchorViewSize;
    private int mCouiDotDiameter;
    private int mCouiDotViewHeight;
    private int mMarginTopAndLeftOfAnchorView;
    private int mMarginTopAndLeftOfReddot;
    private int mRedDotMode;
    private String mRedDotText;
    private COUIHintRedDot mRedDotView;
    private final Runnable mRequestLayoutRunnable = new Runnable() {
        @Override
        public void run() {
            requestLayout();
        }
    };

    public COUIRedDotFrameLayout(Context context) {
        this(context, null);
    }

    public COUIRedDotFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public COUIRedDotFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mCouiDotViewHeight = getResources().getDimensionPixelSize(R.dimen.coui_height);
        init(attrs, defStyleAttr);
        addRedDot();
    }

    private void addRedDot() {
        if (mRedDotMode == COUIHintRedDot.NO_POINT_MODE) {
            return;
        }
        final COUIHintRedDot redDot = new COUIHintRedDot(getContext());
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        redDot.setLayoutParams(layoutParams);
        layoutParams.gravity = 8388661;
        redDot.setPointMode(mRedDotMode);
        if (mRedDotMode == COUIHintRedDot.POINT_WITH_NUM_MODE
                || mRedDotMode == COUIHintRedDot.POINT_NUM_MODE_STROKE) {
            redDot.setViewHeight(mCouiDotViewHeight);
            redDot.setPointText(mRedDotText);
        } else {
            redDot.setDotDiameter(mCouiDotDiameter);
        }
        post(new Runnable() {
            @Override
            public void run() {
                addView(redDot);
            }
        });
        refresh();
    }

    private void init(AttributeSet attrs, int defStyleAttr) {
        int mediumIconSize = getResources().getDimensionPixelSize(
                R.dimen.coui_hint_red_dot_medium_icon_size);
        int largeIconSize = getResources().getDimensionPixelSize(
                R.dimen.coui_hint_red_dot_large_icon_size);
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(
                    attrs, R.styleable.COUIRedDotFrameLayout, defStyleAttr, 0);
            mRedDotMode = a.getInt(
                    R.styleable.COUIRedDotFrameLayout_couiHintRedPointMode,
                    COUIHintRedDot.NO_POINT_MODE);
            mRedDotText = a.getString(R.styleable.COUIRedDotFrameLayout_couiHintRedPointText);
            mAnchorViewShapeType = a.getInt(
                    R.styleable.COUIRedDotFrameLayout_anchorViewShapeType,
                    RECTANGLE_TYPE);
            mAnchorViewSize = a.getDimensionPixelSize(
                    R.styleable.COUIRedDotFrameLayout_anchorViewDpSize,
                    mediumIconSize);
            a.recycle();
        }
        if (mRedDotMode == COUIHintRedDot.NO_POINT_MODE) {
            return;
        }
        if (mAnchorViewSize < mediumIconSize) {
            applySmallAnchorConfig();
        } else if (mAnchorViewSize >= largeIconSize) {
            applyLargeAnchorConfig();
        } else {
            applyMediumAnchorConfig();
        }
        if (mRedDotMode == COUIHintRedDot.POINT_ONLY_MODE_STROKE) {
            mCouiDotDiameter += getResources().getDimensionPixelSize(
                    R.dimen.coui_hint_red_dot_mode_stroke_extra_diameter);
        }
        if (mRedDotMode == COUIHintRedDot.POINT_NUM_MODE_STROKE) {
            mCouiDotViewHeight += getResources().getDimensionPixelSize(
                    R.dimen.coui_hint_red_dot_mode_stroke_extra_diameter);
        }
    }

    private void applySmallAnchorConfig() {
        if (mRedDotMode == COUIHintRedDot.POINT_ONLY_MODE
                || mRedDotMode == COUIHintRedDot.POINT_ONLY_MODE_STROKE) {
            mCouiDotDiameter = getResources().getDimensionPixelSize(
                    R.dimen.coui_hint_red_dot_small_reddot_size);
        }
        if (mAnchorViewShapeType == RECTANGLE_TYPE) {
            mMarginTopAndLeftOfAnchorView = getResources().getDimensionPixelSize(
                    mRedDotMode == COUIHintRedDot.POINT_WITH_NUM_MODE
                            || mRedDotMode == COUIHintRedDot.POINT_NUM_MODE_STROKE
                            ? R.dimen.coui_hint_red_dot_small_number_topend_margin_rectangle
                            : R.dimen.coui_hint_red_dot_small_icon_topend_margin_rectangle);
        } else if (mRedDotMode == COUIHintRedDot.POINT_ONLY_MODE
                || mRedDotMode == COUIHintRedDot.POINT_ONLY_MODE_STROKE) {
            mMarginTopAndLeftOfReddot = getResources().getDimensionPixelSize(
                    R.dimen.coui_hint_red_dot_small_icon_topend_margin_circle);
        }
    }

    private void applyMediumAnchorConfig() {
        if (mRedDotMode == COUIHintRedDot.POINT_ONLY_MODE
                || mRedDotMode == COUIHintRedDot.POINT_ONLY_MODE_STROKE) {
            mCouiDotDiameter = getResources().getDimensionPixelSize(
                    R.dimen.coui_hint_red_dot_medium_reddot_size);
        }
        if (mAnchorViewShapeType == RECTANGLE_TYPE) {
            mMarginTopAndLeftOfAnchorView = getResources().getDimensionPixelSize(
                    mRedDotMode == COUIHintRedDot.POINT_WITH_NUM_MODE
                            || mRedDotMode == COUIHintRedDot.POINT_NUM_MODE_STROKE
                            ? R.dimen.coui_hint_red_dot_medium_number_topend_margin_rectangle
                            : R.dimen.coui_hint_red_dot_medium_icon_topend_margin_rectangle);
        } else if (mRedDotMode == COUIHintRedDot.POINT_ONLY_MODE
                || mRedDotMode == COUIHintRedDot.POINT_ONLY_MODE_STROKE) {
            mMarginTopAndLeftOfReddot = getResources().getDimensionPixelSize(
                    R.dimen.coui_hint_red_dot_medium_icon_topend_margin_circle);
        }
    }

    private void applyLargeAnchorConfig() {
        if (mRedDotMode == COUIHintRedDot.POINT_WITH_NUM_MODE
                || mRedDotMode == COUIHintRedDot.POINT_NUM_MODE_STROKE) {
            mCouiDotViewHeight = getResources().getDimensionPixelSize(R.dimen.coui_height_large);
        } else {
            mCouiDotDiameter = getResources().getDimensionPixelSize(
                    R.dimen.coui_hint_red_dot_large_reddot_size);
        }
        if (mAnchorViewShapeType == RECTANGLE_TYPE) {
            mMarginTopAndLeftOfAnchorView = getResources().getDimensionPixelSize(
                    mRedDotMode == COUIHintRedDot.POINT_WITH_NUM_MODE
                            || mRedDotMode == COUIHintRedDot.POINT_NUM_MODE_STROKE
                            ? R.dimen.coui_hint_red_dot_large_number_topend_margin_rectangle
                            : R.dimen.coui_hint_red_dot_large_icon_topend_margin_rectangle);
        } else if (mRedDotMode == COUIHintRedDot.POINT_ONLY_MODE
                || mRedDotMode == COUIHintRedDot.POINT_ONLY_MODE_STROKE) {
            mMarginTopAndLeftOfReddot = getResources().getDimensionPixelSize(
                    R.dimen.coui_hint_red_dot_large_icon_topend_margin_circle);
        }
    }

    private boolean isRtlMode() {
        return getLayoutDirection() == LAYOUT_DIRECTION_RTL;
    }

    private void refresh() {
        removeCallbacks(mRequestLayoutRunnable);
        post(mRequestLayoutRunnable);
    }

    private void setChildView() {
        if (mRedDotView != null && mAnchorView != null) {
            return;
        }
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child instanceof COUIHintRedDot) {
                mRedDotView = (COUIHintRedDot) child;
            } else {
                mAnchorView = child;
            }
        }
    }

    public COUIHintRedDot getRedDotView() {
        return mRedDotView;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mRedDotMode == COUIHintRedDot.NO_POINT_MODE) {
            return;
        }
        if (mAnchorView == null || mRedDotView == null) {
            if (mAnchorView != null) {
                mAnchorView.layout(0, 0, mAnchorView.getMeasuredWidth(), mAnchorView.getMeasuredHeight());
            }
            return;
        }
        if (isRtlMode()) {
            int anchorOffset = mMarginTopAndLeftOfAnchorView;
            mAnchorView.layout(anchorOffset, anchorOffset,
                    mAnchorView.getMeasuredWidth() + anchorOffset,
                    anchorOffset + mAnchorView.getMeasuredHeight());
            int redDotOffset = mMarginTopAndLeftOfReddot;
            mRedDotView.layout(redDotOffset, redDotOffset,
                    mRedDotView.getWidth() + redDotOffset,
                    redDotOffset + mRedDotView.getHeight());
        } else {
            mAnchorView.layout(0, mMarginTopAndLeftOfAnchorView,
                    mAnchorView.getMeasuredWidth(),
                    mMarginTopAndLeftOfAnchorView + mAnchorView.getMeasuredHeight());
            int redDotOffset = mMarginTopAndLeftOfReddot;
            mRedDotView.layout(getWidth() - mRedDotView.getWidth() - redDotOffset,
                    redDotOffset,
                    getWidth() - redDotOffset,
                    redDotOffset + mRedDotView.getHeight());
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mRedDotMode == COUIHintRedDot.NO_POINT_MODE) {
            return;
        }
        setChildView();
        if (mAnchorView != null && mRedDotView != null) {
            setMeasuredDimension(
                    getMeasuredWidth() + mMarginTopAndLeftOfAnchorView,
                    getMeasuredHeight() + mMarginTopAndLeftOfAnchorView);
        } else if (mAnchorView != null) {
            setMeasuredDimension(mAnchorView.getWidth(), mAnchorView.getHeight());
        }
    }

    public void removeRedDot() {
        if (mRedDotView != null) {
            removeView(mRedDotView);
            mRedDotView = null;
            refresh();
        }
    }

    public void showReddot(int pointMode, String text) {
        showReddot(pointMode, text,
                getResources().getDimensionPixelSize(R.dimen.coui_hint_red_dot_medium_icon_size),
                RECTANGLE_TYPE);
    }

    public void showReddot(int pointMode, String text, int anchorViewSize, int anchorViewShapeType) {
        mAnchorViewShapeType = anchorViewShapeType;
        mAnchorViewSize = anchorViewSize;
        mRedDotMode = pointMode;
        mRedDotText = text;
        init(null, 0);
        addRedDot();
    }
}
