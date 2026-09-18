package com.coui.appcompat.cardlist;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
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
import com.coui.appcompat.preference.ListSelectedItemLayout;
import com.coui.appcompat.roundRect.COUIShapePath;
import com.coui.appcompat.roundcorner.RoundCornerUtil;
import com.coui.appcompat.state.COUIStateEffectDrawable;
import com.coui.appcompat.version.COUIVersionUtil;
import com.oplus.graphics.OplusPathAdapter;

public class COUICardListSelectedItemLayout extends ListSelectedItemLayout {
    private static final float OS_16_1_WEIGHT = 3.0f;
    public static final int T = 32;

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
    private View mMainLayoutToSetExtraPadding;
    private int mMinimumHeight;
    private OplusPathAdapter mOplusPathAdapter;
    private final ViewOutlineProvider mOutlineProvider;
    private final Paint mPaint;
    private Path mPath;
    private float[] mRadii;
    private float mRadius;
    private int mRadius17dpForOS16_1;
    private boolean mTopRounded;

    public COUICardListSelectedItemLayout(Context context) {
        this(context, null);
    }

    public COUICardListSelectedItemLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public COUICardListSelectedItemLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public COUICardListSelectedItemLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mHeadOrTailPadding = getResources().getDimensionPixelOffset(R.dimen.coui_list_card_head_or_tail_padding);
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

            @Override public int getOpacity() { return android.graphics.PixelFormat.TRANSLUCENT; }
            @Override public void setAlpha(int alpha) { }
            @Override public void setColorFilter(ColorFilter colorFilter) { }
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
        mRadii = new float[8];
        COUIDarkModeUtil.setForceDarkAllow(this, false);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.COUICardListSelectedItemLayout, defStyleAttr, defStyleRes);
        boolean tiny = a.getBoolean(R.styleable.COUICardListSelectedItemLayout_listIsTiny, false);
        mRadius = a.getDimensionPixelOffset(
                R.styleable.COUICardListSelectedItemLayout_couiCardRadius,
                COUIContextUtil.getAttrDimens(context, R.attr.couiRoundCornerM));
        init(context, tiny);
        mHorizontalMargin = a.getDimensionPixelOffset(
                R.styleable.COUICardListSelectedItemLayout_couiCardListHorizontalMargin,
                mHorizontalMargin);
        mRadius17dpForOS16_1 = getResources().getDimensionPixelSize(R.dimen.coui_card_list_os_16_1_radius_17_dp);
        a.recycle();
        if (getId() != -1) {
            try {
                if ("single_card".equals(getContext().getResources().getResourceEntryName(getId()))) {
                    consumeDispatchingEventForState(true);
                }
            } catch (Resources.NotFoundException e) {
                com.coui.appcompat.log.COUILog.e("COUICardListSelectedItemLayout", e.getMessage());
            }
        }
    }

    private void init(Context context, boolean tiny) {
        mHorizontalMargin = context.getResources().getDimensionPixelOffset(
                tiny ? R.dimen.coui_preference_card_margin_horizontal_tiny
                        : R.dimen.coui_preference_card_margin_horizontal);
        mCardBackgroundColor = COUIContextUtil.getAttrColor(context, R.attr.couiColorCardBackground);
        mMinimumHeight = getMinimumHeight();
        mInitPaddingTop = getPaddingTop();
        mInitPaddingBottom = getPaddingBottom();
        setBackground(mCardBackgroundDrawable);
    }

    private void setCardRadiusStyle(int position) {
        if (position == COUICardListHelper.FULL) {
            mTopRounded = true;
            mBottomRounded = true;
        } else if (position == COUICardListHelper.HEAD) {
            mTopRounded = true;
            mBottomRounded = false;
        } else if (position == COUICardListHelper.TAIL) {
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
        if (position == COUICardListHelper.HEAD) {
            top = mHeadOrTailPadding;
            bottom = 0;
        } else if (position == COUICardListHelper.TAIL) {
            top = 0;
            bottom = mHeadOrTailPadding;
        } else if (position == COUICardListHelper.FULL) {
            top = mHeadOrTailPadding;
            bottom = mHeadOrTailPadding;
        } else {
            top = 0;
            bottom = 0;
        }
        setMinimumHeight(mMinimumHeight + top + bottom);
        View view = mMainLayoutToSetExtraPadding;
        if (view != null) {
            view.setPaddingRelative(view.getPaddingStart(), getPaddingTop() + top, view.getPaddingEnd(), getPaddingBottom() + bottom);
        } else {
            setPaddingRelative(getPaddingStart(), mInitPaddingTop + top, getPaddingEnd(), mInitPaddingBottom + bottom);
        }
    }

    private void updatePath() {
        getLayoutPath().reset();
        mCardRect.set(mHorizontalMargin, 0.0f, getWidth() - mHorizontalMargin, getHeight());
        if (RoundCornerUtil.getSmoothStyleType() != 1) {
            COUIShapePath.getRoundRectPath(getLayoutPath(), mCardRect, mRadius, mTopRounded, mTopRounded, mBottomRounded, mBottomRounded);
            return;
        }
        if (mOplusPathAdapter == null) {
            mOplusPathAdapter = new OplusPathAdapter(getLayoutPath(), OplusPathAdapter.NEW_PATH_SMOOTH);
        }
        if (COUIVersionUtil.getOSVersionCode() > COUIVersionUtil.OPLUS_OUTLINE_MAJOR_VERSION_37) {
            mRadius = mRadius17dpForOS16_1;
        }
        mRadii[0] = mTopRounded ? mRadius : 0.0f;
        mRadii[1] = mTopRounded ? mRadius : 0.0f;
        mRadii[2] = mTopRounded ? mRadius : 0.0f;
        mRadii[3] = mTopRounded ? mRadius : 0.0f;
        mRadii[4] = mBottomRounded ? mRadius : 0.0f;
        mRadii[5] = mBottomRounded ? mRadius : 0.0f;
        mRadii[6] = mBottomRounded ? mRadius : 0.0f;
        mRadii[7] = mBottomRounded ? mRadius : 0.0f;
        if (COUIVersionUtil.getOSVersionCode() > COUIVersionUtil.OPLUS_OUTLINE_MAJOR_VERSION_37) {
            mOplusPathAdapter.addSmoothRoundRect(mCardRect, mRadii, Path.Direction.CCW, OS_16_1_WEIGHT);
        } else {
            mOplusPathAdapter.addSmoothRoundRect(mCardRect, mRadii, Path.Direction.CCW);
        }
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

    public float getRadius() {
        return mRadius;
    }

    @Override
    public boolean isCardType() {
        return true;
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mConfigurationChangeListener != null) {
            mConfigurationChangeListener.configurationChanged(newConfig);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
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

    public void setMainLayoutToSetExtraPadding(View view) {
        mMainLayoutToSetExtraPadding = view;
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
