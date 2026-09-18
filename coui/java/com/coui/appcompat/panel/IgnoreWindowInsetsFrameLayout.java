package com.coui.appcompat.panel;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.WindowInsets;
import android.widget.FrameLayout;

import com.coui.appcompat.R;

public class IgnoreWindowInsetsFrameLayout extends FrameLayout {
    private boolean mCouiPanelEdgeToEdgeEnable;
    private boolean mIsIgnoreWindowInsetsBottom;
    private boolean mIsIgnoreWindowInsetsLeft;
    private boolean mIsIgnoreWindowInsetsRight;
    private boolean mIsIgnoreWindowInsetsTop;
    private int mWindowInsetsBottomOffset;
    private int mWindowInsetsLeftOffset;
    private int mWindowInsetsRightOffset;
    private int mWindowInsetsTopOffset;

    public IgnoreWindowInsetsFrameLayout(Context context) {
        super(context);
        this.mIsIgnoreWindowInsetsLeft = true;
        this.mIsIgnoreWindowInsetsTop = true;
        this.mIsIgnoreWindowInsetsRight = true;
        this.mIsIgnoreWindowInsetsBottom = true;
    }

    private void initAttr(AttributeSet attributeSet) {
        if (getContext() != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attributeSet, R.styleable.IgnoreWindowInsetsFrameLayout);
            this.mIsIgnoreWindowInsetsLeft = typedArray.getBoolean(R.styleable.IgnoreWindowInsetsFrameLayout_ignoreWindowInsetsLeft, true);
            this.mIsIgnoreWindowInsetsTop = typedArray.getBoolean(R.styleable.IgnoreWindowInsetsFrameLayout_ignoreWindowInsetsTop, true);
            this.mIsIgnoreWindowInsetsRight = typedArray.getBoolean(R.styleable.IgnoreWindowInsetsFrameLayout_ignoreWindowInsetsRight, true);
            this.mIsIgnoreWindowInsetsBottom = typedArray.getBoolean(R.styleable.IgnoreWindowInsetsFrameLayout_ignoreWindowInsetsBottom, true);
            typedArray.recycle();
            if (COUINavigationBarUtil.isGestureNavigation(getContext())) {
                return;
            }
            this.mIsIgnoreWindowInsetsBottom = false;
            setFitsSystemWindows(false);
            setClipToPadding(true);
        }
    }

    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets windowInsets) {
        int bottomPadding;
        int navigationBarBottomInset = windowInsets.getInsets(WindowInsets.Type.navigationBars()).bottom;
        int leftPadding = this.mIsIgnoreWindowInsetsLeft ? 0 : Math.max(0, windowInsets.getSystemWindowInsetLeft() + this.mWindowInsetsLeftOffset);
        int topPadding = this.mIsIgnoreWindowInsetsTop ? 0 : Math.max(0, windowInsets.getSystemWindowInsetTop() + this.mWindowInsetsTopOffset);
        int rightPadding = this.mIsIgnoreWindowInsetsRight ? 0 : Math.max(0, windowInsets.getSystemWindowInsetRight() + this.mWindowInsetsRightOffset);
        if (this.mIsIgnoreWindowInsetsBottom) {
            bottomPadding = 0;
        } else {
            if (this.mCouiPanelEdgeToEdgeEnable) {
                navigationBarBottomInset = 0;
            }
            bottomPadding = Math.max(0, navigationBarBottomInset + this.mWindowInsetsBottomOffset);
        }
        setPadding(leftPadding, topPadding, rightPadding, bottomPadding);
        this.mWindowInsetsLeftOffset = 0;
        this.mWindowInsetsTopOffset = 0;
        this.mWindowInsetsRightOffset = 0;
        this.mWindowInsetsBottomOffset = 0;
        return windowInsets.consumeSystemWindowInsets();
    }

    public void setCouiPanelEdgeToEdgeEnable(boolean enabled) {
        this.mCouiPanelEdgeToEdgeEnable = enabled;
    }

    public void setIgnoreWindowInsetsBottom(boolean ignoreWindowInsetsBottom) {
        this.mIsIgnoreWindowInsetsBottom = ignoreWindowInsetsBottom;
    }

    public void setIgnoreWindowInsetsLeft(boolean ignoreWindowInsetsLeft) {
        this.mIsIgnoreWindowInsetsLeft = ignoreWindowInsetsLeft;
    }

    public void setIgnoreWindowInsetsRight(boolean ignoreWindowInsetsRight) {
        this.mIsIgnoreWindowInsetsRight = ignoreWindowInsetsRight;
    }

    public void setIgnoreWindowInsetsTop(boolean ignoreWindowInsetsTop) {
        this.mIsIgnoreWindowInsetsTop = ignoreWindowInsetsTop;
    }

    public void setWindowInsetsBottomOffset(int bottomOffset) {
        this.mWindowInsetsBottomOffset = bottomOffset;
    }

    public void setWindowInsetsLeftOffset(int leftOffset) {
        this.mWindowInsetsLeftOffset = leftOffset;
    }

    public void setWindowInsetsRightOffset(int rightOffset) {
        this.mWindowInsetsRightOffset = rightOffset;
    }

    public void setWindowInsetsTopOffset(int topOffset) {
        this.mWindowInsetsTopOffset = topOffset;
    }

    public IgnoreWindowInsetsFrameLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mIsIgnoreWindowInsetsLeft = true;
        this.mIsIgnoreWindowInsetsTop = true;
        this.mIsIgnoreWindowInsetsRight = true;
        this.mIsIgnoreWindowInsetsBottom = true;
        initAttr(attributeSet);
    }

    public IgnoreWindowInsetsFrameLayout(Context context, AttributeSet attributeSet, int defStyleAttr) {
        super(context, attributeSet, defStyleAttr);
        this.mIsIgnoreWindowInsetsLeft = true;
        this.mIsIgnoreWindowInsetsTop = true;
        this.mIsIgnoreWindowInsetsRight = true;
        this.mIsIgnoreWindowInsetsBottom = true;
        initAttr(attributeSet);
    }
}
