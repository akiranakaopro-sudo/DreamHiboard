package com.coui.appcompat.dialog.widget;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Insets;
import android.graphics.Outline;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewParent;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.LinearLayoutCompat;

import com.coui.appcompat.R;
import com.coui.appcompat.buttonBar.COUIButtonBarLayout;
import com.coui.appcompat.contextutil.COUIContextUtil;
import com.coui.appcompat.log.COUILog;
import com.coui.appcompat.roundcorner.RoundCornerUtil;
import com.coui.appcompat.statement.COUIMaxHeightScrollView;
import com.coui.appcompat.uiutil.UIUtil;
import com.coui.appcompat.version.COUIVersionUtil;
import com.oplus.graphics.OplusOutlineAdapter;

public class COUIAlertDialogMaxLinearLayout extends LinearLayout {
    private boolean mHasLoading;
    private boolean mHasMessageMerge;
    private boolean mIsTiny;
    private boolean mSupportDynamicMarginTop;
    private int mCustomDialogPaddingBottom;
    private int mMaxHeight;
    private int mMaxWidth;
    private int mNeedMinHeight;
    private int mNeedReMeasureLayoutId = -1;
    private int mNeedSetPaddingLayoutId = -1;
    private int mRadius = -1;
    private boolean mBlurBackgroundWindow;
    private boolean mIsSupportSmoothRoundCorner;
    private boolean mNeedSetMarginTop;
    private int mDialogLayoutMarginBottom;
    private int mDialogLayoutMarginHorizontal;
    private int mDialogLayoutMarginVerticalTotal;
    private int mMessagePaddingTopWhenDialogIsTallDialog;
    private int mMessagePaddingBottomWhenDialogTallDialog;
    private int mDialogContentPanelLayoutMinHeight;
    private int mDialogCustomViewMinHeight;
    private int mMessagePaddingStart;
    private int mMessagePaddingEnd;
    private int mRadius33dpForOS16_1;
    private int mRadius16dpForOS16_1;
    private int mCouiBottomAlertDialogButtonbarMargintop;
    private View mTopPanelLayout;
    private FrameLayout mCustomPanelLayout;
    private FrameLayout mCustomView;
    private View mContentPanelLayout;
    private COUIDialogTitle mDialogTitle;
    private COUIAlertDialogMessageView mDialogMessage;
    private COUIMaxHeightNestedScrollView mScrollViewMessage;
    private COUIMaxHeightScrollView mScrollViewTitle;
    private COUIButtonBarLayout mButtonPanel;
    private View mTitleTemplate;
    private int mOriginalScrollViewTitleMaxHeight = -1;
    private int mOriginalScrollViewTitleMinHeight = -1;
    private int mOriginalTitleTemplateMarginTop = -1;
    private int mOriginalTitleTemplateMarginBottom = -1;
    private int mMergeAndSplitCriticalHeight;
    private LinearLayout mLinearLayoutTitle;
    private View mSpacingViewForMessage;
    private View mSpacingViewForCustomView;
    private boolean mNeedResetButtomBarTopMargin;
    private boolean mIsMergedToTitle;
    private int mCustomMarginExtra = 5;
    private final Rect mTempRect = new Rect();
    private final View.OnApplyWindowInsetsListener mApplyWindowInsetsListener =
            new View.OnApplyWindowInsetsListener() {
                @Override
                public WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                    setMarginTop();
                    return windowInsets;
                }
            };

    public COUIAlertDialogMaxLinearLayout(Context context) {
        this(context, null);
    }

    public COUIAlertDialogMaxLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.COUIAlertDialogMaxLinearLayout);
        mMaxHeight = a.getDimensionPixelSize(R.styleable.COUIAlertDialogMaxLinearLayout_maxHeight, 0);
        mMaxWidth = a.getDimensionPixelSize(R.styleable.COUIAlertDialogMaxLinearLayout_maxWidth, 0);
        mRadius = a.getDimensionPixelSize(R.styleable.COUIAlertDialogMaxLinearLayout_clip_radius_root, -1);
        a.recycle();
        mMessagePaddingTopWhenDialogIsTallDialog = getResources().getDimensionPixelSize(
                R.dimen.coui_alert_dialog_scroll_padding_top_message);
        mMessagePaddingBottomWhenDialogTallDialog = getResources().getDimensionPixelSize(
                R.dimen.coui_alert_dialog_scroll_padding_bottom_message);
        mDialogLayoutMarginBottom = getResources().getDimensionPixelSize(R.dimen.coui_dialog_layout_margin_vertical);
        mDialogLayoutMarginVerticalTotal = mDialogLayoutMarginBottom;
        mDialogLayoutMarginHorizontal = getResources().getDimensionPixelSize(R.dimen.coui_dialog_layout_margin_horizontal);
        mDialogContentPanelLayoutMinHeight = getResources().getDimensionPixelSize(
                R.dimen.coui_dialog_layout_content_panel_layout_min_height);
        mDialogCustomViewMinHeight = getResources().getDimensionPixelSize(
                R.dimen.coui_dialog_layout_customview_min_height);
        mMessagePaddingStart = getResources().getDimensionPixelSize(R.dimen.coui_alert_dialog_message_padding_left);
        mMessagePaddingEnd = getResources().getDimensionPixelSize(R.dimen.coui_alert_dialog_message_padding_left);
        mRadius33dpForOS16_1 = getResources().getDimensionPixelSize(R.dimen.coui_dialog_os_16_1_radius_33_dp);
        mRadius16dpForOS16_1 = getResources().getDimensionPixelSize(R.dimen.coui_dialog_os_16_1_radius_16_dp);
        mCouiBottomAlertDialogButtonbarMargintop = getResources().getDimensionPixelSize(
                R.dimen.coui_bottom_alert_dialog_buttonbar_margintop);
        mIsSupportSmoothRoundCorner = RoundCornerUtil.isVersionSupport() && RoundCornerUtil.isSmoothRoundRectOn();
        setOrientation(VERTICAL);
    }

    @Override
    protected void onAttachedToWindow() {
        if (mSupportDynamicMarginTop) {
            getRootView().setOnApplyWindowInsetsListener(mApplyWindowInsetsListener);
        } else {
            setMarginTop();
        }
        Drawable background = getContext().getDrawable(R.drawable.coui_alert_dialog_background);
        if (!(((background instanceof NinePatchDrawable) || (background instanceof BitmapDrawable)) && !mIsTiny)) {
            setOutLineProvider();
        }
        setMarginBottom();
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        getRootView().setOnApplyWindowInsetsListener(null);
        super.onDetachedFromWindow();
    }

    private void setMarginBottom() {
        if (mCustomDialogPaddingBottom > 0 && getBackground() instanceof InsetDrawable) {
            InsetDrawable insetDrawable = (InsetDrawable) getBackground();
            insetDrawable.getPadding(mTempRect);
            mTempRect.bottom = mCustomDialogPaddingBottom;
            mMaxHeight += mCustomDialogPaddingBottom - mDialogLayoutMarginBottom;
            Drawable drawable = insetDrawable.getDrawable();
            setBackground(new InsetDrawable(drawable, mTempRect.left, mTempRect.top,
                    mTempRect.right, mTempRect.bottom));
            mDialogLayoutMarginVerticalTotal = mTempRect.top + mTempRect.bottom;
        }
    }

    public void setMarginTop() {
        boolean needTopMargin = true;
        Activity activity = UIUtil.contextToActivity(getContext());
        if (activity != null) {
            boolean statusBarHiddenByInsets = false;
            if (Build.VERSION.SDK_INT >= 30
                    && activity.getWindow().getDecorView().getRootWindowInsets() != null) {
                Insets insets = activity.getWindow().getDecorView().getRootWindowInsets()
                        .getInsets(WindowInsets.Type.statusBars());
                statusBarHiddenByInsets = insets != null && insets.top == 0;
            }
            boolean fullScreenFlag = (activity.getWindow().getAttributes().flags
                    & WindowManager.LayoutParams.FLAG_FULLSCREEN)
                    == WindowManager.LayoutParams.FLAG_FULLSCREEN;
            boolean lowProfile = (activity.getWindow().getDecorView().getSystemUiVisibility()
                    & View.SYSTEM_UI_FLAG_FULLSCREEN) == View.SYSTEM_UI_FLAG_FULLSCREEN;
            if (!fullScreenFlag && !lowProfile) {
                needTopMargin = statusBarHiddenByInsets;
            }
        } else if (Build.VERSION.SDK_INT >= 30 && getRootView().getRootWindowInsets() != null) {
            needTopMargin = !getRootView().getRootWindowInsets().isVisible(WindowInsets.Type.statusBars());
        } else {
            needTopMargin = false;
        }
        if (mNeedSetMarginTop != needTopMargin && getBackground() instanceof InsetDrawable) {
            InsetDrawable insetDrawable = (InsetDrawable) getBackground();
            insetDrawable.getPadding(mTempRect);
            if (needTopMargin) {
                mTempRect.top = mTempRect.bottom;
                mMaxHeight += mTempRect.bottom;
            } else {
                mTempRect.top = 0;
                mMaxHeight -= mTempRect.bottom;
            }
            Drawable drawable = insetDrawable.getDrawable();
            setBackground(new InsetDrawable(drawable, mTempRect.left, mTempRect.top,
                    mTempRect.right, mTempRect.bottom));
            mDialogLayoutMarginVerticalTotal = mTempRect.top + mTempRect.bottom;
        }
        mNeedSetMarginTop = needTopMargin;
    }

    private void setOutLineProvider() {
        if (mRadius == -1) {
            int radiusAttr;
            if (mHasLoading) {
                radiusAttr = R.attr.couiRoundCornerM;
            } else {
                radiusAttr = R.attr.couiRoundCornerXXL;
            }
            if (mIsSupportSmoothRoundCorner && RoundCornerUtil.getSmoothStyleType()
                    == RoundCornerUtil.SMOOTH_ROUND_CORNER_TYPE_UNSUPPORTED) {
                mRadius = 0;
            } else {
                mRadius = COUIContextUtil.getAttrDimens(getContext(), radiusAttr);
            }
        }
        if (mRadius > 0) {
            setClipToOutline(true);
            setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    setOutLineProviderInternal(outline);
                }
            });
        }
    }

    private void setOutLineProviderInternal(Outline outline) {
        int measuredWidth;
        int measuredHeight;
        int left;
        int top;
        if (mHasLoading || mIsTiny) {
            measuredWidth = getMeasuredWidth();
            measuredHeight = getMeasuredHeight();
            if (mIsTiny) {
                measuredHeight += mRadius;
            }
            left = 0;
            top = 0;
        } else {
            left = mDialogLayoutMarginHorizontal;
            top = mCustomDialogPaddingBottom > 0
                    ? mDialogLayoutMarginVerticalTotal - mCustomDialogPaddingBottom
                    : mDialogLayoutMarginVerticalTotal - mDialogLayoutMarginBottom;
            measuredWidth = getMeasuredWidth() - (mDialogLayoutMarginHorizontal * 2);
            measuredHeight = getMeasuredHeight() - mDialogLayoutMarginVerticalTotal;
        }
        int right = left + measuredWidth;
        int bottom = top + measuredHeight;
        if (!RoundCornerUtil.supportSRCCompatibleBlur(mBlurBackgroundWindow)) {
            outline.setRoundRect(left, top, right, bottom, mRadius);
        } else if (useOs15RoundCorner()) {
            new OplusOutlineAdapter(outline, OplusOutlineAdapter.OLD_OUTLINE_SMOOTH)
                    .setSmoothRoundRect(left, top, right, bottom, mRadius,
                            mHasLoading
                                    ? COUIContextUtil.getFloat(getContext(), R.dimen.coui_round_corner_m_weight)
                                    : COUIContextUtil.getFloat(getContext(), R.dimen.coui_round_corner_xxl_weight));
        } else if (RoundCornerUtil.getSmoothStyleType() == RoundCornerUtil.SMOOTH_ROUND_CORNER_TYPE_OS16) {
            OplusOutlineAdapter adapter = new OplusOutlineAdapter(outline,
                    OplusOutlineAdapter.NEW_OUTLINE_SMOOTH);
            if (COUIVersionUtil.getOSVersionCode() <= 37 || !mIsSupportSmoothRoundCorner) {
                adapter.setSmoothRoundRect(left, top, right, bottom, mRadius);
            } else {
                adapter.setSmoothRoundRect(left, top, right, bottom,
                        mHasLoading ? mRadius16dpForOS16_1 : mRadius33dpForOS16_1, 3.0f);
            }
        } else {
            outline.setRoundRect(left, top, right, bottom, mRadius);
        }
        COUILog.i("DialogMaxLinearLayout", "getOutline: mBlurBackgroundWindow = "
                + mBlurBackgroundWindow + " isSupportRoundCornerWhenBlur="
                + RoundCornerUtil.isSupportRoundCornerWhenBlur() + " mIsSupportSmoothRoundCorner="
                + mIsSupportSmoothRoundCorner + " mRadius=" + mRadius);
    }

    private boolean useOs15RoundCorner() {
        return RoundCornerUtil.getSmoothStyleType() == RoundCornerUtil.SMOOTH_ROUND_CORNER_TYPE_OS15
                || mBlurBackgroundWindow;
    }

    private int maxMeasureSpec(int measureSpec, int maxSize) {
        return MeasureSpec.makeMeasureSpec(Math.min(MeasureSpec.getSize(measureSpec), maxSize),
                MeasureSpec.getMode(measureSpec));
    }

    private boolean isSpecialMachine() {
        return Build.VERSION.SDK_INT <= 31;
    }

    private int getHeightWithVerticalMargin(View view) {
        if (view == null) {
            return 0;
        }
        int measuredHeight = view.getMeasuredHeight();
        if (!(view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams)) {
            return measuredHeight;
        }
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        return measuredHeight + lp.topMargin + lp.bottomMargin;
    }

    private void clearTitleTemplateMargin() {
        if (mTitleTemplate == null || !(mTitleTemplate.getLayoutParams() instanceof LinearLayout.LayoutParams)) {
            return;
        }
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mTitleTemplate.getLayoutParams();
        lp.topMargin = 0;
        lp.bottomMargin = 0;
        mTitleTemplate.setLayoutParams(lp);
    }

    private void restoreTitleTemplateMargin() {
        if (mTitleTemplate == null || !(mTitleTemplate.getLayoutParams() instanceof LinearLayout.LayoutParams)) {
            return;
        }
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mTitleTemplate.getLayoutParams();
        if (mOriginalTitleTemplateMarginTop >= 0) {
            lp.topMargin = mOriginalTitleTemplateMarginTop;
        }
        if (mOriginalTitleTemplateMarginBottom >= 0) {
            lp.bottomMargin = mOriginalTitleTemplateMarginBottom;
        }
        mTitleTemplate.setLayoutParams(lp);
    }

    @Override
    @SuppressLint({"LongLogTag"})
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mMaxWidth > 0) {
            widthMeasureSpec = maxMeasureSpec(widthMeasureSpec, mMaxWidth);
        }
        if (mMaxHeight > 0) {
            heightMeasureSpec = maxMeasureSpec(heightMeasureSpec, mMaxHeight);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measuredHeight = getMeasuredHeight();
        if (mScrollViewTitle == null) {
            try {
                mTopPanelLayout = findViewById(R.id.topPanel);
                mCustomPanelLayout = findViewById(R.id.customPanel);
                mCustomView = findViewById(R.id.custom);
                mContentPanelLayout = findViewById(R.id.contentPanel);
                mDialogTitle = findViewById(R.id.alertTitle);
                mDialogMessage = findViewById(android.R.id.message);
                mScrollViewMessage = findViewById(R.id.scrollView);
                mScrollViewTitle = findViewById(R.id.alert_title_scroll_view);
                if (mScrollViewTitle != null && mOriginalScrollViewTitleMaxHeight < 0) {
                    mOriginalScrollViewTitleMaxHeight = mScrollViewTitle.getMaxHeight();
                }
                if (mScrollViewTitle != null && mOriginalScrollViewTitleMinHeight < 0) {
                    mOriginalScrollViewTitleMinHeight = mScrollViewTitle.getMinHeight();
                }
                mButtonPanel = findViewById(R.id.buttonPanel);
                mTitleTemplate = findViewById(R.id.title_template);
                if (mTitleTemplate != null
                        && mTitleTemplate.getLayoutParams() instanceof LinearLayout.LayoutParams) {
                    LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mTitleTemplate.getLayoutParams();
                    mOriginalTitleTemplateMarginTop = lp.topMargin;
                    mOriginalTitleTemplateMarginBottom = lp.bottomMargin;
                }
            } catch (Exception e) {
                Log.e("DialogMaxLinearLayout", "Failed to get type conversion. message e:" + e.getMessage());
                mHasMessageMerge = false;
                return;
            }
        }
        int messageLineCount;
        int titleLineCount;
        if (mDialogMessage instanceof TextView && mDialogTitle != null) {
            messageLineCount = mDialogMessage.getLineCount();
            titleLineCount = mDialogTitle.getLineCount();
        } else {
            messageLineCount = 0;
            titleLineCount = 0;
        }
        getWindowVisibleDisplayFrame(mTempRect);
        int availableHeight = mTempRect.height() - mDialogLayoutMarginVerticalTotal;
        int measuredContentHeight = measuredHeight - mDialogLayoutMarginVerticalTotal;
        if (measuredHeight <= 0 || measuredContentHeight >= mNeedMinHeight || availableHeight < mNeedMinHeight) {
            if (mNeedSetPaddingLayoutId != -1) {
                boolean titleMultiLine = titleLineCount > 1;
                boolean messageMultiLine = messageLineCount > 1;
                boolean multiButtonVertical = mButtonPanel != null && mButtonPanel.getButtonCount() > 1
                        && mButtonPanel.getOrientation() == VERTICAL;
                boolean customTall = mCustomView != null
                        && mCustomView.getMeasuredHeight() > mDialogCustomViewMinHeight;
                View paddingTarget;
                if ((titleMultiLine || messageMultiLine || multiButtonVertical || customTall)
                        && (paddingTarget = findViewById(mNeedSetPaddingLayoutId)) != null
                        && paddingTarget.getPaddingTop() != mMessagePaddingTopWhenDialogIsTallDialog) {
                    paddingTarget.setPadding(paddingTarget.getPaddingStart(),
                            mMessagePaddingTopWhenDialogIsTallDialog, paddingTarget.getPaddingEnd(),
                            mMessagePaddingBottomWhenDialogTallDialog);
                    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                }
            }
        } else if (mNeedReMeasureLayoutId != -1 && !isSpecialMachine()) {
            COUIMaxHeightScrollView remeasureView = findViewById(mNeedReMeasureLayoutId);
            int targetMinHeight = remeasureView != null
                    ? remeasureView.getMeasuredHeight() + (mNeedMinHeight - measuredContentHeight)
                    : 0;
            if (remeasureView != null && remeasureView.getMinHeight() != targetMinHeight) {
                remeasureView.setMinHeight(targetMinHeight);
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }
        }
        boolean hasMessage = mDialogMessage != null && !TextUtils.isEmpty(mDialogMessage.getText());
        boolean hasCustomView = mCustomView != null && mCustomView.getChildCount() > 0;
        if (mDialogTitle != null && !TextUtils.isEmpty(mDialogTitle.getText())
                && (hasMessage || hasCustomView) && mHasMessageMerge
                && mButtonPanel != null && mButtonPanel.getParent() instanceof COUIMaxHeightNestedScrollView
                && mTopPanelLayout != null && mScrollViewMessage != null) {
            if (mMergeAndSplitCriticalHeight == 0) {
                mMergeAndSplitCriticalHeight =
                        ((COUIMaxHeightNestedScrollView) mButtonPanel.getParent()).getMeasuredHeight()
                                + mTopPanelLayout.getMeasuredHeight()
                                + mDialogContentPanelLayoutMinHeight
                                + mScrollViewMessage.getPaddingTop()
                                + mScrollViewMessage.getPaddingBottom();
            }
            boolean shouldMerge = availableHeight < mMergeAndSplitCriticalHeight;
            boolean shouldSplit = !shouldMerge;
            if (mLinearLayoutTitle != null
                    && (((mDialogMessage != null && mDialogMessage.getParent() == mLinearLayoutTitle)
                    || (mCustomView != null && mCustomView.getParent() == mLinearLayoutTitle))
                    && shouldSplit)) {
                restoreTitleTemplateMargin();
                mLinearLayoutTitle.setPadding(0, 0, 0, 0);
                mScrollViewMessage.setVisibility(VISIBLE);
                if (mScrollViewTitle != null) {
                    if (mOriginalScrollViewTitleMaxHeight > 0
                            && mScrollViewTitle.getMaxHeight() != mOriginalScrollViewTitleMaxHeight) {
                        mScrollViewTitle.setMaxHeight(mOriginalScrollViewTitleMaxHeight);
                    }
                    if (mOriginalScrollViewTitleMinHeight >= 0
                            && mScrollViewTitle.getMinHeight() != mOriginalScrollViewTitleMinHeight) {
                        mScrollViewTitle.setMinHeight(mOriginalScrollViewTitleMinHeight);
                    }
                    mScrollViewTitle.setVisibility(VISIBLE);
                }
                if (mDialogMessage != null && mDialogMessage.getParent() == mLinearLayoutTitle) {
                    mLinearLayoutTitle.removeView(mDialogMessage);
                    if (mSpacingViewForMessage != null) {
                        mLinearLayoutTitle.removeView(mSpacingViewForMessage);
                    }
                    mDialogMessage.setPaddingRelative(mMessagePaddingStart, mDialogMessage.getPaddingTop(),
                            mMessagePaddingEnd, mDialogMessage.getPaddingBottom());
                    mScrollViewMessage.addView(mDialogMessage);
                }
                if (mCustomView != null && mCustomView.getParent() == mLinearLayoutTitle) {
                    mLinearLayoutTitle.removeView(mCustomView);
                    if (mSpacingViewForCustomView != null) {
                        mLinearLayoutTitle.removeView(mSpacingViewForCustomView);
                    }
                    ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) mCustomView.getLayoutParams();
                    lp.setMarginStart(lp.getMarginStart() + (mMessagePaddingStart - mCustomMarginExtra));
                    mCustomPanelLayout.addView(mCustomView);
                }
                if (mNeedResetButtomBarTopMargin
                        && mButtonPanel.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                    mButtonPanel.setTopMarginFlag(true);
                }
                mIsMergedToTitle = false;
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }
            if (shouldMerge && !mIsMergedToTitle && mScrollViewTitle != null) {
                if (mLinearLayoutTitle == null) {
                    mLinearLayoutTitle = new LinearLayout(getContext());
                    LinearLayout.LayoutParams titleLayoutParams = new LinearLayout.LayoutParams(
                            LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                    titleLayoutParams.gravity = android.view.Gravity.TOP;
                    mLinearLayoutTitle.setLayoutParams(titleLayoutParams);
                    mLinearLayoutTitle.setOrientation(VERTICAL);
                    mScrollViewTitle.removeAllViews();
                    mScrollViewTitle.addView(mLinearLayoutTitle);
                    mLinearLayoutTitle.addView(mDialogTitle);
                    if (hasMessage) {
                        mSpacingViewForMessage = new View(getContext());
                        mSpacingViewForMessage.setLayoutParams(new LinearLayout.LayoutParams(
                                LayoutParams.MATCH_PARENT, mMessagePaddingTopWhenDialogIsTallDialog));
                    }
                    if (hasCustomView) {
                        mSpacingViewForCustomView = new View(getContext());
                        mSpacingViewForCustomView.setLayoutParams(new LinearLayout.LayoutParams(
                                LayoutParams.MATCH_PARENT, mMessagePaddingTopWhenDialogIsTallDialog));
                    }
                }
                clearTitleTemplateMargin();
                mLinearLayoutTitle.setPadding(0, mOriginalTitleTemplateMarginTop, 0,
                        mOriginalTitleTemplateMarginBottom);
                mScrollViewMessage.setVisibility(GONE);
                if (hasMessage && mDialogMessage.getParent() != mLinearLayoutTitle) {
                    mDialogMessage.setPaddingRelative(0, mDialogMessage.getPaddingTop(), 0,
                            mDialogMessage.getPaddingBottom());
                    mScrollViewMessage.removeView(mDialogMessage);
                    mLinearLayoutTitle.addView(mSpacingViewForMessage);
                    mLinearLayoutTitle.addView(mDialogMessage);
                }
                if (hasCustomView && mCustomView.getParent() != mLinearLayoutTitle) {
                    mCustomPanelLayout.removeView(mCustomView);
                    LinearLayoutCompat.LayoutParams lp = new LinearLayoutCompat.LayoutParams(
                            LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                    lp.setMarginStart(lp.getMarginStart() - (mMessagePaddingStart - mCustomMarginExtra));
                    mLinearLayoutTitle.addView(mSpacingViewForCustomView);
                    mLinearLayoutTitle.addView(mCustomView, lp);
                }
                if (mButtonPanel.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                    ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) mButtonPanel.getLayoutParams();
                    if (lp.topMargin == mCouiBottomAlertDialogButtonbarMargintop) {
                        lp.topMargin = 0;
                        mButtonPanel.setLayoutParams(lp);
                        mNeedResetButtomBarTopMargin = true;
                        mButtonPanel.setTopMarginFlag(false);
                    }
                }
                mIsMergedToTitle = true;
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }
        }
        if (mButtonPanel != null && mButtonPanel.getParent() instanceof COUIMaxHeightNestedScrollView
                && mTopPanelLayout != null && mContentPanelLayout != null && mCustomPanelLayout != null) {
            COUIMaxHeightNestedScrollView buttonScroll =
                    (COUIMaxHeightNestedScrollView) mButtonPanel.getParent();
            int buttonHeightWithMargin = getHeightWithVerticalMargin(mButtonPanel);
            if (!mIsMergedToTitle) {
                buttonScroll.setMaxHeight(availableHeight - mTopPanelLayout.getMeasuredHeight());
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                return;
            }
            int titleSpace = ((availableHeight - buttonHeightWithMargin)
                    - mContentPanelLayout.getMeasuredHeight()) - mCustomPanelLayout.getMeasuredHeight();
            if (mScrollViewTitle != null) {
                if (titleSpace > 0) {
                    mScrollViewTitle.setVisibility(VISIBLE);
                    mScrollViewTitle.setMaxHeight(titleSpace);
                    if (titleSpace < mOriginalScrollViewTitleMinHeight) {
                        mScrollViewTitle.setMinHeight(titleSpace);
                    }
                } else {
                    mScrollViewTitle.setVisibility(GONE);
                    mScrollViewTitle.setMinHeight(0);
                }
                int buttonMaxHeight = ((availableHeight - Math.max(titleSpace, 0))
                        - mContentPanelLayout.getMeasuredHeight()) - mCustomPanelLayout.getMeasuredHeight();
                if (buttonMaxHeight > 0) {
                    buttonScroll.setMaxHeight(buttonMaxHeight);
                }
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }
        }
    }

    public int getMaxWidth() {
        return mMaxWidth;
    }

    public void setCustomDialogPaddingBottom(int paddingBottom) {
        if (paddingBottom >= mDialogLayoutMarginBottom) {
            mCustomDialogPaddingBottom = paddingBottom;
        } else {
            COUILog.e("DialogMaxLinearLayout", "setCustomDialogPaddingBottom can't be less than 24dp");
        }
    }

    public void setHasLoading(boolean hasLoading) {
        mHasLoading = hasLoading;
    }

    public void setHasMessageMerge(boolean hasMessageMerge) {
        mHasMessageMerge = hasMessageMerge;
    }

    public void setBlurBackgroundWindow(boolean blurBackgroundWindow) {
        mBlurBackgroundWindow = blurBackgroundWindow;
    }

    public void setIsTiny(boolean tiny) {
        mIsTiny = tiny;
    }

    public void setMaxHeight(int maxHeight) {
        mMaxHeight = maxHeight;
        requestLayout();
    }

    public void setMaxWidth(int maxWidth) {
        mMaxWidth = maxWidth;
        requestLayout();
    }

    public void setNeedMinHeight(int needMinHeight) {
        mNeedMinHeight = needMinHeight;
        requestLayout();
    }

    public void setNeedReMeasureLayoutId(int layoutId) {
        mNeedReMeasureLayoutId = layoutId;
    }

    public void setNeedSetPaddingLayoutId(int layoutId) {
        mNeedSetPaddingLayoutId = layoutId;
    }

    public void setSupportDynamicMarginTop(boolean supportDynamicMarginTop) {
        mSupportDynamicMarginTop = supportDynamicMarginTop;
    }
}
