package com.coui.appcompat.progressbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.airbnb.lottie.LottieAnimationView;
import com.coui.appcompat.R;
import com.coui.appcompat.contextutil.COUIContextUtil;

public class COUICompProgressIndicator extends LinearLayout {
    public static final int LARGE_ANIMATION = 0;
    public static final int SMALL_ANIMATION = 1;
    public static final int SMALL_ANIMATION_WITH_TEXT_HORIZONTAL = 2;
    public static final int LARGE_ANIMATION_WITH_TEXT_VERTICAL = 3;
    public static final int SMALL_ANIMATION_WITH_TEXT_VERTICAL = 4;

    private final boolean mAutoPlay;
    private final int mCouiLargeLoadingTextviewTopMargin;
    private final int mCouiLoadingTextviewBottomMargin;
    private final int mCouiLoadingTextviewLeftMargin;
    private int mCouiLoadingType;
    private String mCouiLottieLoadingJsonName;
    private final int mCouiLottieLoadingViewHeight;
    private final int mCouiLottieLoadingViewWidth;
    private final int mCouiSmallLoadingTextviewTopMargin;
    private final int mCouiSmallLottieLoadingViewHeight;
    private final int mCouiSmallLottieLoadingViewWidth;
    private final int mCouiLottieLoadingRawRes;
    private final int mRepeatCount;
    private final boolean mTextFix;
    // Leapy modified 2026-07-26: Match the decoded OPPO implementation with
    // a direct Lottie view, including raw/asset selection and repeat behavior.
    private LottieAnimationView mLoadingView;
    private String mLoadingTips;
    private boolean mNeedRePlay;
    private TextView mTipsTextView;

    public COUICompProgressIndicator(Context context) {
        this(context, null);
    }

    public COUICompProgressIndicator(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public COUICompProgressIndicator(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.COUICompProgressIndicator,
                defStyleAttr, 0);
        mCouiLoadingType = a.getInt(R.styleable.COUICompProgressIndicator_couiLoadingType,
                LARGE_ANIMATION);
        mLoadingTips = a.getString(R.styleable.COUICompProgressIndicator_loadingTips);
        mCouiLottieLoadingJsonName = a.getString(
                R.styleable.COUICompProgressIndicator_couiLottieLoadingJsonName);
        mCouiLottieLoadingRawRes = a.getResourceId(
                R.styleable.COUICompProgressIndicator_couiLottieLoadingRawRes, -1);
        mRepeatCount = a.getInt(
                R.styleable.COUICompProgressIndicator_couiRepeatCount, -1);
        mAutoPlay = a.getBoolean(R.styleable.COUICompProgressIndicator_couiAutoPlay, true);
        int maxWidth = getResources().getDimensionPixelSize(R.dimen.coui_loading_max_large_width);
        int maxHeight = getResources().getDimensionPixelSize(R.dimen.coui_loading_max_large_height);
        mCouiLottieLoadingViewWidth = Math.min(maxWidth, a.getDimensionPixelSize(
                R.styleable.COUICompProgressIndicator_couiLottieLoadingViewWidth,
                getResources().getDimensionPixelOffset(R.dimen.coui_loading_large_width)));
        mCouiLottieLoadingViewHeight = Math.min(maxHeight, a.getDimensionPixelSize(
                R.styleable.COUICompProgressIndicator_couiLottieLoadingViewHeight,
                getResources().getDimensionPixelOffset(R.dimen.coui_loading_large_height)));
        mCouiSmallLottieLoadingViewWidth = Math.min(maxWidth, a.getDimensionPixelSize(
                R.styleable.COUICompProgressIndicator_couiSmallLottieLoadingViewWidth,
                getResources().getDimensionPixelOffset(R.dimen.coui_loading_small_width)));
        mCouiSmallLottieLoadingViewHeight = Math.min(maxHeight, a.getDimensionPixelSize(
                R.styleable.COUICompProgressIndicator_couiSmallLottieLoadingViewHeight,
                getResources().getDimensionPixelOffset(R.dimen.coui_loading_small_height)));
        mTextFix = a.getBoolean(R.styleable.COUICompProgressIndicator_couiTextFix, false);
        a.recycle();
        mCouiLoadingTextviewLeftMargin = context.getResources().getDimensionPixelSize(
                R.dimen.coui_loading_textview_left_margin);
        mCouiLargeLoadingTextviewTopMargin = context.getResources().getDimensionPixelSize(
                R.dimen.coui_loading_textview_top_margin);
        mCouiSmallLoadingTextviewTopMargin = context.getResources().getDimensionPixelSize(
                R.dimen.coui_loading_textview_top_margin_small);
        mCouiLoadingTextviewBottomMargin = context.getResources().getDimensionPixelSize(
                R.dimen.coui_loading_textview_bottom_margin);
        if (TextUtils.isEmpty(mCouiLottieLoadingJsonName)) {
            mCouiLottieLoadingJsonName = COUIContextUtil.getAttrString(
                    context, R.attr.couiRotatingSpinnerJsonName);
        }
        setGravity(Gravity.CENTER);
        setOrientation(VERTICAL);
    }

    private void addLoadingView(Context context, boolean large) {
        mLoadingView = new LottieAnimationView(context);
        LayoutParams lp = large
                ? new LayoutParams(mCouiLottieLoadingViewWidth, mCouiLottieLoadingViewHeight)
                : new LayoutParams(mCouiSmallLottieLoadingViewWidth, mCouiSmallLottieLoadingViewHeight);
        lp.gravity = Gravity.CENTER;
        mLoadingView.setRepeatCount(mRepeatCount);
        if (!TextUtils.isEmpty(mCouiLottieLoadingJsonName)) {
            mLoadingView.setAnimation(mCouiLottieLoadingJsonName);
        }
        if (mCouiLottieLoadingRawRes != -1) {
            mLoadingView.setAnimation(mCouiLottieLoadingRawRes);
        }
        addView(mLoadingView, lp);
        if (mAutoPlay) {
            mLoadingView.playAnimation();
        }
    }

    private void addSubView(Context context) {
        if (mCouiLoadingType == LARGE_ANIMATION) {
            addLoadingView(context, true);
        } else if (mCouiLoadingType == SMALL_ANIMATION) {
            addLoadingView(context, false);
        } else if (mCouiLoadingType == SMALL_ANIMATION_WITH_TEXT_HORIZONTAL) {
            setOrientation(HORIZONTAL);
            addLoadingView(context, false);
            addTipsTextView(context, false);
        } else if (mCouiLoadingType == LARGE_ANIMATION_WITH_TEXT_VERTICAL) {
            addLoadingView(context, true);
            addTipsTextView(context, true);
        } else if (mCouiLoadingType == SMALL_ANIMATION_WITH_TEXT_VERTICAL) {
            addLoadingView(context, false);
            addTipsTextView(context, true);
        }
    }

    private void addTipsTextView(Context context, boolean vertical) {
        mTipsTextView = new TextView(new ContextThemeWrapper(context,
                android.R.style.Widget_TextView));
        mTipsTextView.setText(mLoadingTips);
        mTipsTextView.setTextColor(COUIContextUtil.getAttrColor(context,
                R.attr.couiColorLabelSecondary, 0xff666666));
        LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        if (mCouiLoadingType == SMALL_ANIMATION_WITH_TEXT_HORIZONTAL) {
            lp.setMarginStart(mCouiLoadingTextviewLeftMargin);
        } else if (mCouiLoadingType == LARGE_ANIMATION_WITH_TEXT_VERTICAL) {
            lp.setMargins(0, mCouiLargeLoadingTextviewTopMargin, 0,
                    mCouiLoadingTextviewBottomMargin);
        } else if (mCouiLoadingType == SMALL_ANIMATION_WITH_TEXT_VERTICAL) {
            lp.setMargins(0, mCouiSmallLoadingTextviewTopMargin, 0,
                    mCouiLoadingTextviewBottomMargin);
        }
        if (mTextFix) {
            mTipsTextView.setTextSize(12f);
        }
        mTipsTextView.setVisibility(TextUtils.isEmpty(mLoadingTips) ? GONE : VISIBLE);
        addView(mTipsTextView, lp);
    }

    public LottieAnimationView getAnimationView() {
        return mLoadingView;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        // Leapy modified 2026-07-26: Create the renderer on attachment like
        // OPPO so detached indicators do not parse or start animations.
        if (mLoadingView == null) {
            addSubView(getContext());
        }
        if (mLoadingView != null && mNeedRePlay) {
            mLoadingView.resumeAnimation();
            mNeedRePlay = false;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (mLoadingView != null && mLoadingView.isAnimating()) {
            mNeedRePlay = true;
            mLoadingView.pauseAnimation();
        }
        super.onDetachedFromWindow();
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (mLoadingView == null) {
            return;
        }
        if (visibility == VISIBLE && mNeedRePlay) {
            mLoadingView.resumeAnimation();
            mNeedRePlay = false;
        } else if (visibility != VISIBLE && mLoadingView.isAnimating()) {
            mNeedRePlay = true;
            mLoadingView.pauseAnimation();
        }
    }

    public void refresh() {
        if (!TextUtils.isEmpty(mCouiLottieLoadingJsonName)) {
            mLoadingView.setAnimation(mCouiLottieLoadingJsonName);
        }
        if (mCouiLottieLoadingRawRes != -1) {
            mLoadingView.setAnimation(mCouiLottieLoadingRawRes);
        }
        if (mLoadingView != null && mAutoPlay) {
            mLoadingView.playAnimation();
        }
        if (mTipsTextView != null) {
            mTipsTextView.setTextColor(COUIContextUtil.getAttrColor(getContext(),
                    R.attr.couiColorLabelSecondary, 0xff666666));
        }
    }

    public void setLoadingTips(String tips) {
        mLoadingTips = tips;
        if (mTipsTextView != null) {
            mTipsTextView.setText(tips);
            mTipsTextView.setVisibility(TextUtils.isEmpty(tips) ? GONE : VISIBLE);
        }
    }

    public void setLoadingTips(int resId) {
        setLoadingTips(getContext().getString(resId));
    }

    public void setLoadingType(int loadingType) {
        mCouiLoadingType = loadingType;
    }

    public void setTipsText(CharSequence text) {
        setLoadingTips(text == null ? null : text.toString());
    }

    public void startLoading() {
        if (mLoadingView != null) {
            mLoadingView.playAnimation();
        }
    }

    public void stopLoading() {
        if (mLoadingView != null) {
            mLoadingView.cancelAnimation();
        }
    }
}
