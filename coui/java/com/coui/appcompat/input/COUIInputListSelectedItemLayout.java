package com.coui.appcompat.input;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;

import com.coui.appcompat.R;
import com.coui.appcompat.contextutil.COUIContextUtil;
import com.coui.appcompat.darkmode.COUIDarkModeUtil;
import com.coui.appcompat.list.ConfigurationChangedListener;
import com.coui.appcompat.log.COUILog;
import com.coui.appcompat.preference.ListSelectedItemLayout;
import com.coui.appcompat.roundRect.COUIShapePath;
import com.coui.appcompat.state.COUIStateEffectDrawable;

public class COUIInputListSelectedItemLayout extends ListSelectedItemLayout {
    public static final int NONE = 0;
    public static final int HEAD = 1;
    public static final int MIDDLE = 2;
    public static final int TAIL = 3;
    public static final int FULL = 4;
    public static final int T = 32;

    private static final String TAG = "COUICardListSelectedItemLayout";

    @Deprecated
    private ValueAnimator mRestoreBackgroundAppearAnimator;
    @Deprecated
    private ValueAnimator mRestoreBackgroundDisappearAnimator;
    private final int mHeadOrTailPadding;
    private boolean mApplyOutline;
    private boolean mBottomRounded;
    private int mCardBackgroundColor;
    private final Drawable mCardBackgroundDrawable;
    private final RectF mCardRect;
    private ConfigurationChangedListener mConfigurationChangeListener;
    private int mHorizontalMargin;
    private int mInitPaddingBottom;
    private int mInitPaddingTop;
    private boolean mIsDrawPathType;
    private boolean mIsSelected;
    private int mMinimumHeight;
    private final ViewOutlineProvider mOutlineProvider;
    private final Paint mPaint;
    private Path mPath;
    private float mRadius;
    private boolean mTopRounded;

    @Deprecated
    public static class AnimatorHelper {
        public int mAppearAnimatorCurrentPlayTime;
        public int mDisappearAnimatorCurrentPlayTime;

        public AnimatorHelper(int appearAnimatorCurrentPlayTime, int disappearAnimatorCurrentPlayTime) {
            mAppearAnimatorCurrentPlayTime = appearAnimatorCurrentPlayTime;
            mDisappearAnimatorCurrentPlayTime = disappearAnimatorCurrentPlayTime;
        }
    }

    public COUIInputListSelectedItemLayout(Context context) {
        this(context, null);
    }

    public COUIInputListSelectedItemLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public COUIInputListSelectedItemLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public COUIInputListSelectedItemLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mHeadOrTailPadding = getResources().getDimensionPixelOffset(R.dimen.coui_list_input_head_or_tail_padding);
        mCardRect = new RectF();
        mPaint = new Paint();
        mCardBackgroundDrawable = new Drawable() {
            @Override
            public void draw(Canvas canvas) {
                if (!mIsDrawPathType) {
                    canvas.drawColor(mCardBackgroundColor);
                } else {
                    mPaint.setColor(mCardBackgroundColor);
                    canvas.drawPath(getLayoutPath(), mPaint);
                }
            }

            @Override
            public int getOpacity() {
                return PixelFormat.TRANSLUCENT;
            }

            @Override
            public void setAlpha(int alpha) {
            }

            @Override
            public void setColorFilter(ColorFilter colorFilter) {
            }
        };
        mOutlineProvider = new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                if (Build.VERSION.SDK_INT >= T) {
                    outline.setPath(getLayoutPath());
                    mApplyOutline = true;
                }
            }
        };
        mTopRounded = true;
        mBottomRounded = true;
        mApplyOutline = false;
        COUIDarkModeUtil.setForceDarkAllow(this, false);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.COUIInputListSelectedItemLayout, defStyleAttr, defStyleRes);
        boolean tiny = a.getBoolean(R.styleable.COUIInputListSelectedItemLayout_listIsTiny, false);
        mRadius = a.getDimensionPixelOffset(
                R.styleable.COUIInputListSelectedItemLayout_couiInputRadius,
                COUIContextUtil.getAttrDimens(context, R.attr.couiRoundCornerM));
        init(context, tiny);
        mHorizontalMargin = a.getDimensionPixelOffset(
                R.styleable.COUIInputListSelectedItemLayout_couiInputListHorizontalMargin,
                mHorizontalMargin);
        a.recycle();

        if (getId() != -1) {
            try {
                if ("single_card".equals(getContext().getResources().getResourceEntryName(getId()))) {
                    consumeDispatchingEventForState(true);
                }
            } catch (Resources.NotFoundException e) {
                COUILog.e(TAG, e.getMessage());
            }
        }
    }

    private void init(Context context, boolean tiny) {
        mHorizontalMargin = context.getResources().getDimensionPixelOffset(
                tiny ? R.dimen.coui_preference_input_margin_horizontal_tiny
                        : R.dimen.coui_preference_input_margin_horizontal);
        mCardBackgroundColor = COUIContextUtil.getAttrColor(context, R.attr.couiColorCardBackground);
        mMinimumHeight = getMinimumHeight();
        mInitPaddingTop = getPaddingTop();
        mInitPaddingBottom = getPaddingBottom();
        setBackground(mCardBackgroundDrawable);
    }

    private void setCardRadiusStyle(int position) {
        if (position == FULL) {
            mTopRounded = true;
            mBottomRounded = true;
        } else if (position == HEAD) {
            mTopRounded = true;
            mBottomRounded = false;
        } else if (position == TAIL) {
            mTopRounded = false;
            mBottomRounded = true;
        } else {
            mTopRounded = false;
            mBottomRounded = false;
        }
    }

    private void setExtraPadding(int position) {
        int top;
        int bottom;
        if (position == HEAD) {
            top = mHeadOrTailPadding;
            bottom = 0;
        } else if (position == TAIL) {
            top = 0;
            bottom = mHeadOrTailPadding;
        } else if (position == FULL) {
            top = mHeadOrTailPadding;
            bottom = mHeadOrTailPadding;
        } else {
            top = 0;
            bottom = 0;
        }
        setMinimumHeight(mMinimumHeight + top + bottom);
        setPaddingRelative(getPaddingStart(), mInitPaddingTop + top, getPaddingEnd(), mInitPaddingBottom + bottom);
    }

    private void updatePath() {
        getLayoutPath().reset();
        mCardRect.set(mHorizontalMargin, 0.0f, getWidth() - mHorizontalMargin, getHeight());
        COUIShapePath.getRoundRectPath(
                getLayoutPath(),
                mCardRect,
                mRadius,
                mTopRounded,
                mTopRounded,
                mBottomRounded,
                mBottomRounded);
    }

    public void changeDrawCanvasType(boolean drawPathType) {
        if (mIsDrawPathType != drawPathType) {
            mIsDrawPathType = drawPathType;
        }
    }

    @Override
    public void draw(Canvas canvas) {
        if (mIsDrawPathType || (Build.VERSION.SDK_INT >= T && mApplyOutline)) {
            updatePath();
            super.draw(canvas);
        } else {
            canvas.save();
            canvas.clipPath(getLayoutPath());
            super.draw(canvas);
            canvas.restore();
        }
    }

    public boolean getIsSelected() {
        return mIsSelected;
    }

    @Override
    public Path getLayoutPath() {
        if (mPath == null) {
            mPath = new Path();
        }
        return mPath;
    }

    public int getMarginHorizontal() {
        return mHorizontalMargin;
    }

    @Override
    public boolean isCardType() {
        return false;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mConfigurationChangeListener != null) {
            mConfigurationChangeListener.configurationChanged(newConfig);
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updatePath();
        if (mIsDrawPathType || Build.VERSION.SDK_INT < T) {
            mApplyOutline = false;
            setClipToOutline(false);
        } else {
            setOutlineProvider(mOutlineProvider);
            setClipToOutline(true);
        }
    }

    @Override
    public void refreshCardBg(int color) {
        mCardBackgroundColor = color;
        invalidate();
    }

    @Deprecated
    public synchronized void restoreAnimator(AnimatorHelper animatorHelper) {
    }

    @Deprecated
    public AnimatorHelper saveAndEndAnimator() {
        return null;
    }

    @Override
    public void setConfigurationChangeListener(ConfigurationChangedListener listener) {
        mConfigurationChangeListener = listener;
    }

    public void setIsSelected(boolean selected) {
        setIsSelected(selected, false);
    }

    public void setIsSelected(boolean selected, boolean animated) {
        if (mIsSelected != selected) {
            mIsSelected = selected;
            Drawable background = getBackground();
            if (background instanceof COUIStateEffectDrawable) {
                ((COUIStateEffectDrawable) background).setStateLocked(1, selected, selected, animated);
            }
        }
    }

    public void setMarginHorizontal(int margin) {
        mHorizontalMargin = margin;
        requestLayout();
    }

    @Override
    public void setPositionInGroup(int position) {
        if (position > 0) {
            setExtraPadding(position);
            setCardRadiusStyle(position);
            updatePath();
        }
    }

    public void setRadius(float radius) {
        mRadius = radius;
        updatePath();
        invalidate();
    }
}
