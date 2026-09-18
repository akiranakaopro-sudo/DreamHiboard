package com.coui.appcompat.cardview;

import android.R.attr;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.coui.appcompat.R;

public class COUICardView extends FrameLayout {
    private static final int[] COLOR_BACKGROUND_ATTR = {attr.colorBackground};
    private static final CardViewImpl IMPL;

    static {
        CardViewApi21Impl impl = new CardViewApi21Impl();
        IMPL = impl;
        impl.initStatic();
    }

    private final CardViewDelegate mCardViewDelegate;
    private boolean mCompatPadding;
    final Rect mContentPadding = new Rect();
    private boolean mPreventCornerOverlap;
    final Rect mShadowBounds = new Rect();
    int mUserSetMinHeight;
    int mUserSetMinWidth;

    public COUICardView(Context context) {
        this(context, null);
    }

    public COUICardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public COUICardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mCardViewDelegate = new CardViewDelegate() {
            private Drawable mCardBackground;

            @Override
            public Drawable getCardBackground() {
                return mCardBackground;
            }

            @Override
            public View getCardView() {
                return COUICardView.this;
            }

            @Override
            public boolean getPreventCornerOverlap() {
                return COUICardView.this.getPreventCornerOverlap();
            }

            @Override
            public boolean getUseCompatPadding() {
                return COUICardView.this.getUseCompatPadding();
            }

            @Override
            public void setCardBackground(Drawable drawable) {
                mCardBackground = drawable;
                COUICardView.this.setBackgroundDrawable(drawable);
            }

            @Override
            public void setMinWidthHeightInternal(int width, int height) {
                if (width > mUserSetMinWidth) {
                    COUICardView.super.setMinimumWidth(width);
                }
                if (height > mUserSetMinHeight) {
                    COUICardView.super.setMinimumHeight(height);
                }
            }

            @Override
            public void setShadowPadding(int left, int top, int right, int bottom) {
                mShadowBounds.set(left, top, right, bottom);
                COUICardView.super.setPadding(left + mContentPadding.left,
                        top + mContentPadding.top, right + mContentPadding.right,
                        bottom + mContentPadding.bottom);
            }
        };
        initialize(context, attrs, defStyleAttr);
    }

    private void initialize(Context context, AttributeSet attrs, int defStyleAttr) {
        ColorStateList backgroundColor;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.COUICardView, defStyleAttr, 0);
        if (a.hasValue(R.styleable.COUICardView_cardBackgroundColor)) {
            backgroundColor = a.getColorStateList(R.styleable.COUICardView_cardBackgroundColor);
        } else {
            TypedArray aa = getContext().obtainStyledAttributes(COLOR_BACKGROUND_ATTR);
            int color = aa.getColor(0, 0);
            aa.recycle();
            float[] hsv = new float[3];
            Color.colorToHSV(color, hsv);
            backgroundColor = ColorStateList.valueOf(hsv[2] > 0.5f
                    ? getResources().getColor(R.color.cardview_light_background, null)
                    : getResources().getColor(R.color.cardview_dark_background, null));
        }
        float radius = a.getDimension(R.styleable.COUICardView_cardCornerRadius, 0.0f);
        float weight = a.getFloat(R.styleable.COUICardView_couiCardCornerWeight, 0.0f);
        float roundCornerRadius = a.getDimension(
                R.styleable.COUICardView_couiCardRoundCornerRadius, 0.0f);
        float elevation = a.getDimension(R.styleable.COUICardView_cardElevation, 0.0f);
        float maxElevation = a.getDimension(R.styleable.COUICardView_cardMaxElevation, 0.0f);
        mCompatPadding = a.getBoolean(R.styleable.COUICardView_cardUseCompatPadding, false);
        mPreventCornerOverlap = a.getBoolean(
                R.styleable.COUICardView_cardPreventCornerOverlap, true);
        int defaultPadding = a.getDimensionPixelSize(R.styleable.COUICardView_contentPadding, 0);
        mContentPadding.left = a.getDimensionPixelSize(
                R.styleable.COUICardView_contentPaddingLeft, defaultPadding);
        mContentPadding.top = a.getDimensionPixelSize(
                R.styleable.COUICardView_contentPaddingTop, defaultPadding);
        mContentPadding.right = a.getDimensionPixelSize(
                R.styleable.COUICardView_contentPaddingRight, defaultPadding);
        mContentPadding.bottom = a.getDimensionPixelSize(
                R.styleable.COUICardView_contentPaddingBottom, defaultPadding);
        float finalMaxElevation = Math.max(elevation, maxElevation);
        mUserSetMinWidth = a.getDimensionPixelSize(R.styleable.COUICardView_android_minWidth, 0);
        mUserSetMinHeight = a.getDimensionPixelSize(R.styleable.COUICardView_android_minHeight, 0);
        a.recycle();
        IMPL.initialize(mCardViewDelegate, context, backgroundColor, radius, elevation,
                finalMaxElevation, weight, roundCornerRadius);
    }

    public ColorStateList getCardBackgroundColor() {
        return IMPL.getBackgroundColor(mCardViewDelegate);
    }

    public float getCardElevation() {
        return IMPL.getElevation(mCardViewDelegate);
    }

    public float getCardRoundCornerRadius() {
        return IMPL.getCardRoundCornerRadius(mCardViewDelegate);
    }

    public int getContentPaddingBottom() {
        return mContentPadding.bottom;
    }

    public int getContentPaddingLeft() {
        return mContentPadding.left;
    }

    public int getContentPaddingRight() {
        return mContentPadding.right;
    }

    public int getContentPaddingTop() {
        return mContentPadding.top;
    }

    public float getMaxCardElevation() {
        return IMPL.getMaxElevation(mCardViewDelegate);
    }

    public boolean getPreventCornerOverlap() {
        return mPreventCornerOverlap;
    }

    public float getRadius() {
        return IMPL.getRadius(mCardViewDelegate);
    }

    public boolean getUseCompatPadding() {
        return mCompatPadding;
    }

    public float getWeight() {
        return IMPL.getWeight(mCardViewDelegate);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (IMPL instanceof CardViewApi21Impl) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        if (widthMode == MeasureSpec.AT_MOST || widthMode == MeasureSpec.EXACTLY) {
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(Math.max(
                    (int) Math.ceil(IMPL.getMinWidth(mCardViewDelegate)),
                    MeasureSpec.getSize(widthMeasureSpec)), widthMode);
        }
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.EXACTLY) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(Math.max(
                    (int) Math.ceil(IMPL.getMinHeight(mCardViewDelegate)),
                    MeasureSpec.getSize(heightMeasureSpec)), heightMode);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setCardBackgroundColor(int color) {
        IMPL.setBackgroundColor(mCardViewDelegate, ColorStateList.valueOf(color));
    }

    public void setCardBackgroundColor(ColorStateList color) {
        IMPL.setBackgroundColor(mCardViewDelegate, color);
    }

    public void setCardElevation(float elevation) {
        IMPL.setElevation(mCardViewDelegate, elevation);
    }

    public void setCardRoundCornerRadius(float radius) {
        IMPL.setCardRoundCornerRadius(mCardViewDelegate, radius);
    }

    public void setContentPadding(int left, int top, int right, int bottom) {
        mContentPadding.set(left, top, right, bottom);
        IMPL.updatePadding(mCardViewDelegate);
    }

    public void setMaxCardElevation(float maxElevation) {
        IMPL.setMaxElevation(mCardViewDelegate, maxElevation);
    }

    @Override
    public void setMinimumHeight(int minHeight) {
        mUserSetMinHeight = minHeight;
        super.setMinimumHeight(minHeight);
    }

    @Override
    public void setMinimumWidth(int minWidth) {
        mUserSetMinWidth = minWidth;
        super.setMinimumWidth(minWidth);
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
    }

    @Override
    public void setPaddingRelative(int start, int top, int end, int bottom) {
    }

    public void setPreventCornerOverlap(boolean preventCornerOverlap) {
        if (preventCornerOverlap != mPreventCornerOverlap) {
            mPreventCornerOverlap = preventCornerOverlap;
            IMPL.onPreventCornerOverlapChanged(mCardViewDelegate);
        }
    }

    public void setRadius(float radius) {
        IMPL.setRadius(mCardViewDelegate, radius);
    }

    public void setUseCompatPadding(boolean useCompatPadding) {
        if (mCompatPadding != useCompatPadding) {
            mCompatPadding = useCompatPadding;
            IMPL.onCompatPaddingChanged(mCardViewDelegate);
        }
    }

    public void setWeight(float weight) {
        IMPL.setWeight(mCardViewDelegate, weight);
    }
}
