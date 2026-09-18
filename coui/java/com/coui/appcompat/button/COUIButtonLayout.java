package com.coui.appcompat.button;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import com.coui.appcompat.R;


public class COUIButtonLayout extends LinearLayout {
    private boolean isForceSmallScreenWidth;
    private boolean isLimitHeight;
    private int mHorizontalLayoutPadding;
    private int mLimitMaxWidth;
    private int mMaxHeight;
    private int mMaxWidth;
    private OnButtonLayoutVisibilityChangedListener mOnButtonLayoutVisibilityChangedListener;
    private int mOrientation;
    private int mVerticalLayoutPadding;

    public interface OnButtonLayoutVisibilityChangedListener {
        void onButtonLayoutVisibilityChanged(int visibility);
    }

    public COUIButtonLayout(Context context) {
        super(context);
        this.isLimitHeight = false;
        this.isForceSmallScreenWidth = false;
    }

    private void initResource() {
        this.mOrientation = getOrientation();
        this.mHorizontalLayoutPadding = getResources().getDimensionPixelSize(R.dimen.coui_horizontal_btn_margin);
        this.mVerticalLayoutPadding = getResources().getDimensionPixelSize(R.dimen.coui_horizontal_single_btn_margin);
    }

    private void setPaddingHorizontal(int paddingHorizontal) {
        if (paddingHorizontal == 0) {
            paddingHorizontal = getOrientation() == LinearLayout.HORIZONTAL ? this.mHorizontalLayoutPadding : this.mVerticalLayoutPadding;
        }
        setPaddingRelative(paddingHorizontal, getPaddingTop(), paddingHorizontal, getPaddingBottom());
    }

    public int getMaxHeight() {
        return this.mMaxHeight;
    }

    public int getMaxWidth() {
        return this.mMaxWidth;
    }

    public boolean isForceSmallScreenWidth() {
        return this.isForceSmallScreenWidth;
    }

    public boolean isLimitHeight() {
        return this.isLimitHeight;
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = View.MeasureSpec.getMode(widthMeasureSpec);
        int limitMaxWidth = this.mLimitMaxWidth;
        if (limitMaxWidth <= 0 || !(widthMode == MeasureSpec.AT_MOST || widthMode == MeasureSpec.EXACTLY)) {
            this.mMaxWidth = View.MeasureSpec.getSize(widthMeasureSpec);
        } else {
            int limitedWidth = Math.min(limitMaxWidth, View.MeasureSpec.getSize(widthMeasureSpec));
            this.mMaxWidth = limitedWidth;
            widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(limitedWidth, MeasureSpec.EXACTLY);
        }
        this.mMaxHeight = View.MeasureSpec.getSize(heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void onVisibilityChanged(View view, int visibility) {
        OnButtonLayoutVisibilityChangedListener onButtonLayoutVisibilityChangedListener;
        super.onVisibilityChanged(view, visibility);
        if (view != this || (onButtonLayoutVisibilityChangedListener = this.mOnButtonLayoutVisibilityChangedListener) == null) {
            return;
        }
        onButtonLayoutVisibilityChangedListener.onButtonLayoutVisibilityChanged(visibility);
    }

    public boolean setForceSmallScreenWidth(boolean forceSmallScreenWidth) {
        this.isForceSmallScreenWidth = forceSmallScreenWidth;
        return forceSmallScreenWidth;
    }

    public void setHorizontalLayoutPadding(int horizontalLayoutPadding) {
        this.mHorizontalLayoutPadding = horizontalLayoutPadding;
        if (getOrientation() == LinearLayout.HORIZONTAL) {
            setPaddingHorizontal(this.mHorizontalLayoutPadding);
        }
    }

    public void setLimitHeight(boolean limitHeight) {
        this.isLimitHeight = limitHeight;
    }

    public void setOnButtonLayoutVisibilityChangedListener(OnButtonLayoutVisibilityChangedListener onButtonLayoutVisibilityChangedListener) {
        this.mOnButtonLayoutVisibilityChangedListener = onButtonLayoutVisibilityChangedListener;
    }

    @Override
    public void setOrientation(int orientation) {
        super.setOrientation(orientation);
        if (this.mOrientation != orientation) {
            setPaddingHorizontal(0);
            this.mOrientation = orientation;
        }
    }

    public void setVerticalLayoutPadding(int verticalLayoutPadding) {
        this.mVerticalLayoutPadding = verticalLayoutPadding;
        if (getOrientation() == LinearLayout.VERTICAL) {
            setPaddingHorizontal(this.mVerticalLayoutPadding);
        }
    }

    public COUIButtonLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.isLimitHeight = false;
        this.isForceSmallScreenWidth = false;
        initResource();
        if (getContext() != null) {
            TypedArray typedArrayObtainStyledAttributes = getContext().obtainStyledAttributes(attributeSet, R.styleable.COUIButtonLayout);
            this.mHorizontalLayoutPadding = (int) typedArrayObtainStyledAttributes.getDimension(R.styleable.COUIButtonLayout_horizontalLayoutPadding, this.mHorizontalLayoutPadding);
            this.mVerticalLayoutPadding = (int) typedArrayObtainStyledAttributes.getDimension(R.styleable.COUIButtonLayout_verticalLayoutPadding, this.mVerticalLayoutPadding);
            this.mLimitMaxWidth = (int) typedArrayObtainStyledAttributes.getDimension(R.styleable.COUIButtonLayout_couiLimitMaxWidth, this.mLimitMaxWidth);
            typedArrayObtainStyledAttributes.recycle();
        }
        setPaddingHorizontal(0);
    }
}
