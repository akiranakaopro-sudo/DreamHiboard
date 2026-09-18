package com.coui.appcompat.dialog;

import android.app.Dialog;
import android.content.ComponentCallbacks;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.coui.appcompat.R;
import com.coui.appcompat.buttonBar.COUIButtonBarLayout;
import com.coui.appcompat.contextutil.COUIContextUtil;
import com.coui.appcompat.dialog.adapter.COUIListDialogAdapter;
import com.coui.appcompat.dialog.adapter.ChoiceListAdapter;
import com.coui.appcompat.dialog.adapter.SummaryAdapter;
import com.coui.appcompat.dialog.widget.COUIAlertDialogMaxLinearLayout;
import com.coui.appcompat.dialog.widget.COUIAlertDialogMaxScrollView;
import com.coui.appcompat.dialog.widget.COUIMaxHeightNestedScrollView;
import com.coui.appcompat.grid.COUIResponsiveUtils;
import com.coui.appcompat.imageview.COUIRoundImageView;
import com.coui.appcompat.log.COUILog;
import com.coui.appcompat.roundcorner.RoundCornerUtil;
import com.coui.appcompat.statement.COUIMaxHeightScrollView;
import com.coui.appcompat.uiutil.AnimLevel;
import com.coui.appcompat.uiutil.COUIBackgroundBlurBuilder;
import com.coui.appcompat.uiutil.UIUtil;

public class COUIAlertDialogBuilder extends AlertDialog.Builder {
    private static final int DEF_WINDOW_ANIM = R.style.Animation_COUI_Dialog_Alpha;
    private static final int DEF_WINDOW_GRAVITY = Gravity.CENTER;
    private static final String TAG = "COUIAlertDialogBuilder";
    private static final int UNSET_WIDTH = -1;

    private boolean mHasAdapter;
    private boolean mHasMessage;
    private boolean mHasSetButton;
    private boolean mHasSetView;
    private boolean mHasTitle;
    private boolean mIsAssignMentLayout;
    private boolean mAlwaysFollowHand;
    private View mAnchorView;
    private Point mAnchorViewTouchPoint;
    private COUIBackgroundBlurBuilder mBackgroundBlurBuilder;
    private boolean mButtonLayoutDynamicLayout = true;
    private COUIListDialogAdapter mCOUIListDialogAdapter;
    private ChoiceListAdapter mChoiceListAdapter;
    private ComponentCallbacks mComponentCallbacks;
    private Configuration mConfiguration;
    private AlertDialog mDialog;
    private int mContentMaxWidth;
    private int mContentMaxHeight;
    private View mContentView;
    private int mCustomContentLayoutRes;
    private int mCustomDialogPaddingBottom;
    private Drawable mCustomDrawable;
    private CharSequence mCustomMessage;
    private String mCustomTitle;
    private int mDialogStyle;
    private int mDialogWindowType;
    private Point mExtraOffsetPoint;
    private boolean mForcePhysicalDimensions;
    private int mGravity = DEF_WINDOW_GRAVITY;
    private boolean mHasLoading;
    private boolean mHasMessageMerge;
    private boolean mIsCustomStyle;
    private boolean mIsForceCenterInLargeScreen;
    private boolean mIsForceCenterStyleStatus;
    private boolean mIsNeedToAdaptMessageAndList;
    private boolean mIsTinyStyle;
    private DialogInterface.OnClickListener mItemClickListener;
    private CharSequence[] mItems;
    private int mOriginWidth = UNSET_WIDTH;
    private int mOldConfigurationHeightDP;
    private int mOldConfigurationWidthDP;
    private int mRecommendButtonId = -1;
    private boolean mRegisterConfigurationChangeCallBack = true;
    private CharSequence[] mSummaryItems;
    private boolean mSupportDynamicMarginTop;
    public int[] mTextColor;
    private int mWindowAnimStyleRes = DEF_WINDOW_ANIM;

    public static class OutsideTouchListener implements View.OnTouchListener {
        private final Dialog mDialog;
        private final int mPrePieSlop;

        public OutsideTouchListener(Dialog dialog) {
            mDialog = dialog;
            mPrePieSlop = ViewConfiguration.get(dialog.getContext()).getScaledWindowTouchSlop();
        }

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            View parentPanel = view.findViewById(R.id.parentPanel);
            if (parentPanel == null) {
                COUILog.e(TAG, "parentPanel is null; Need to check whether the application has a layout that covers the coui's");
                return mDialog.onTouchEvent(event);
            }
            RectF panelBounds = new RectF(
                    parentPanel.getLeft() + parentPanel.getPaddingLeft(),
                    parentPanel.getTop() + parentPanel.getPaddingTop(),
                    parentPanel.getRight() - parentPanel.getPaddingRight(),
                    parentPanel.getBottom() - parentPanel.getPaddingBottom());
            if (panelBounds.contains(event.getX(), event.getY())) {
                return false;
            }
            MotionEvent copy = MotionEvent.obtain(event);
            if (event.getAction() == MotionEvent.ACTION_UP) {
                copy.setAction(MotionEvent.ACTION_OUTSIDE);
            }
            view.performClick();
            boolean handled = mDialog.onTouchEvent(copy);
            copy.recycle();
            return handled;
        }

        public int getPrePieSlop() {
            return mPrePieSlop;
        }
    }

    public COUIAlertDialogBuilder(Context context) {
        this(context, R.style.COUIAlertDialog_BottomWarning);
    }

    public COUIAlertDialogBuilder(Context context, int themeResId) {
        super(new ContextThemeWrapper(context, themeResId));
        initDefaults(themeResId);
        initAttrs();
        initBlurBuilder(context);
    }

    public COUIAlertDialogBuilder(Context context, int colorThemeResId, int themeResId) {
        super(wrapColorContext(context, colorThemeResId, themeResId));
        initDefaults(themeResId);
        initAttrs();
        initBlurBuilder(context);
    }

    private void initDefaults(int dialogStyle) {
        mDialogStyle = dialogStyle;
        Configuration configuration = getContext().getResources().getConfiguration();
        mOldConfigurationWidthDP = configuration.screenWidthDp;
        mOldConfigurationHeightDP = configuration.screenHeightDp;
        mComponentCallbacks = new ComponentCallbacks() {
            @Override
            public void onConfigurationChanged(Configuration configuration) {
                if (mRegisterConfigurationChangeCallBack) {
                    mConfiguration = configuration;
                    updateGravityWhileConfigChange(configuration);
                }
            }

            @Override
            public void onLowMemory() {
            }
        };
    }

    private void disabledTitleScroll(AlertDialog dialog) {
        View view = dialog.findViewById(R.id.alert_title_scroll_view);
        if (!(view instanceof COUIMaxHeightScrollView)) {
            COUILog.e(TAG, "alert_title_scroll_view is error; Need to check whether the application has a layout that covers the coui's");
            return;
        }
        COUIMaxHeightScrollView scrollView = (COUIMaxHeightScrollView) view;
        scrollView.setOnTouchListener((v, event) -> scrollView.getHeight() < scrollView.getMaxHeight());
    }

    private void initAttrs() {
        TypedArray a = getContext().obtainStyledAttributes(null, R.styleable.COUIAlertDialogBuilder,
                androidx.appcompat.R.attr.alertDialogStyle, R.style.AlertDialogBuildStyle);
        mGravity = a.getInt(R.styleable.COUIAlertDialogBuilder_android_gravity, DEF_WINDOW_GRAVITY);
        mWindowAnimStyleRes = a.getResourceId(R.styleable.COUIAlertDialogBuilder_windowAnimStyle,
                DEF_WINDOW_ANIM);
        mContentMaxWidth = a.getDimensionPixelOffset(
                R.styleable.COUIAlertDialogBuilder_contentMaxWidth, 0);
        mContentMaxHeight = a.getDimensionPixelOffset(
                R.styleable.COUIAlertDialogBuilder_contentMaxHeight, 0);
        mCustomContentLayoutRes = a.getResourceId(
                R.styleable.COUIAlertDialogBuilder_customContentLayout, 0);
        mIsNeedToAdaptMessageAndList = a.getBoolean(
                R.styleable.COUIAlertDialogBuilder_isNeedToAdaptMessageAndList, false);
        mIsTinyStyle = a.getBoolean(R.styleable.COUIAlertDialogBuilder_isTinyDialog, false);
        mHasLoading = a.getBoolean(R.styleable.COUIAlertDialogBuilder_hasLoading, false);
        mIsAssignMentLayout = a.getBoolean(R.styleable.COUIAlertDialogBuilder_isAssignMentLayout, false);
        mIsForceCenterInLargeScreen = a.getBoolean(
                R.styleable.COUIAlertDialogBuilder_isForceCenterStyleInLargeScreen, false);
        mIsCustomStyle = a.getBoolean(R.styleable.COUIAlertDialogBuilder_isCustomStyle, false);
        a.recycle();
    }

    private void initBlurBuilder(Context context) {
        mBackgroundBlurBuilder = new COUIBackgroundBlurBuilder(context);
        mBackgroundBlurBuilder.setMixColorLight(UIUtil.colorToFloats(
                COUIContextUtil.getColor(getContext(), R.color.coui_dialog_list_mix_blur_light)));
        mBackgroundBlurBuilder.setMixColorDark(UIUtil.colorToFloats(
                COUIContextUtil.getColor(getContext(), R.color.coui_dialog_list_mix_blur_dark)));
        mBackgroundBlurBuilder.setBlendColorLight(UIUtil.colorToFloats(
                COUIContextUtil.getColor(getContext(), R.color.coui_dialog_list_blend_blur_light)));
        mBackgroundBlurBuilder.setBlendColorDark(UIUtil.colorToFloats(
                COUIContextUtil.getColor(getContext(), R.color.coui_dialog_list_blend_blur_dark)));
    }

    private void initBlurListener() {
        if (mDialog == null || mDialog.getWindow() == null) {
            return;
        }
        mDialog.getWindow().getDecorView().addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View view) {
                registerApplicationConfigChangeListener();
                try {
                    operateBlur(view);
                } catch (Exception e) {
                    Log.e(TAG, "operateBlur error message:" + e.getMessage());
                }
            }

            @Override
            public void onViewDetachedFromWindow(View view) {
                releaseApplicationConfigChangeListener();
                mBackgroundBlurBuilder.release();
                view.removeOnAttachStateChangeListener(this);
            }
        });
    }

    private void initCOUIDialogTitle(View view) {
        if (view == null) {
            return;
        }
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        view.setLayoutParams(params);
    }

    private void initContentMaxHeight(Window window) {
        if (mContentMaxHeight <= 0) {
            return;
        }
        View parentPanel = window.findViewById(R.id.parentPanel);
        if (parentPanel instanceof COUIAlertDialogMaxLinearLayout) {
            ((COUIAlertDialogMaxLinearLayout) parentPanel).setMaxHeight(mContentMaxHeight);
        } else if (parentPanel instanceof COUIAlertDialogMaxScrollView) {
            ((COUIAlertDialogMaxScrollView) parentPanel).setMaxHeight(mContentMaxHeight);
        } else {
            COUILog.e(TAG, "parentPanel is error; Need to check whether the application has a layout that covers the coui's");
        }
    }

    private void initContentMaxWidth(Window window) {
        if (mContentMaxWidth <= 0) {
            return;
        }
        View parentPanel = window.findViewById(R.id.parentPanel);
        if (parentPanel instanceof COUIAlertDialogMaxLinearLayout) {
            ((COUIAlertDialogMaxLinearLayout) parentPanel).setMaxWidth(mContentMaxWidth);
        } else if (parentPanel instanceof COUIAlertDialogMaxScrollView) {
            ((COUIAlertDialogMaxScrollView) parentPanel).setMaxWidth(mContentMaxWidth);
        } else {
            COUILog.e(TAG, "parentPanel is error; Need to check whether the application has a layout that covers the coui's");
        }
    }

    private void initCustomPanel() {
        if (!mHasSetView && mCustomContentLayoutRes != 0) {
            setView(mCustomContentLayoutRes);
        }
    }

    private void initCustomPanelVisibility(Window window) {
        if (!mHasSetView) {
            return;
        }
        View customPanel = window.findViewById(R.id.customPanel);
        if (customPanel != null) {
            customPanel.setVisibility(View.VISIBLE);
        }
        View custom = window.findViewById(R.id.custom);
        if (custom == null) {
            return;
        }
        custom.setVisibility(View.VISIBLE);
        if (mHasLoading || mHasMessage) {
            return;
        }
        int top = !mHasTitle
                ? getContext().getResources().getDimensionPixelOffset(
                R.dimen.coui_alert_dialog_builder_customstyle_padding_top_withouttitle)
                : !mIsAssignMentLayout
                ? getContext().getResources().getDimensionPixelOffset(
                R.dimen.coui_alert_dialog_customer_layout_imageview_margin_top)
                : 0;
        int bottom = mIsAssignMentLayout
                ? getContext().getResources().getDimensionPixelOffset(
                R.dimen.coui_alert_dialog_customer_layout_imageview_margin_bottom)
                : 0;
        custom.setPaddingRelative(custom.getPaddingStart(), top, custom.getPaddingEnd(), bottom);
    }

    private void initListPanel(Window window) {
        View listPanel = window.findViewById(R.id.listPanel);
        if (!(listPanel instanceof ViewGroup)) {
            COUILog.e(TAG, "listPanel is error; Need to check whether the application has a layout that covers the coui's");
            return;
        }
        ViewGroup listPanelGroup = (ViewGroup) listPanel;
        ListView listView = mDialog != null ? mDialog.getListView() : null;
        boolean hasList = listView != null;
        if (listView != null) {
            listView.setScrollIndicators(0);
            if (listView.getParent() instanceof ViewGroup) {
                ((ViewGroup) listView.getParent()).removeView(listView);
            }
            listPanelGroup.addView(listView, new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
        View scrollView = window.findViewById(R.id.scrollView);
        if (scrollView instanceof ViewGroup) {
            scrollView.setScrollIndicators(0);
            if (mIsNeedToAdaptMessageAndList && hasList) {
                setViewHorizontalWeight(scrollView, 1);
                setViewHorizontalWeight(listPanelGroup, 1);
            }
            if (scrollView instanceof COUIMaxHeightNestedScrollView && mHasAdapter
                    && !AppFeatureUtil.isSecondaryScreen(getContext())) {
                ((COUIMaxHeightNestedScrollView) scrollView).setMaxHeight(
                        getContext().getResources().getDimensionPixelOffset(
                                R.dimen.coui_alert_dialog_builder_content_max_height_with_adapter));
            }
        }
    }

    private void initMessagePadding() {
        if (mDialog == null || mDialog.getWindow() == null) {
            return;
        }
        View scrollView = mDialog.findViewById(R.id.scrollView);
        View parentPanel = mDialog.getWindow().findViewById(R.id.parentPanel);
        if (!(parentPanel instanceof COUIAlertDialogMaxLinearLayout)) {
            COUILog.e(TAG, "parentPanel is error; Need to check whether the application has a layout that covers the coui's");
            return;
        }
        COUIAlertDialogMaxLinearLayout layout = (COUIAlertDialogMaxLinearLayout) parentPanel;
        layout.setHasLoading(mHasLoading);
        layout.setIsTiny(mIsTinyStyle);
        layout.setSupportDynamicMarginTop(mSupportDynamicMarginTop);
        if (mAnchorView != null) {
            mCustomDialogPaddingBottom = 0;
        }
        layout.setCustomDialogPaddingBottom(mCustomDialogPaddingBottom);
        if (!mIsTinyStyle && !mHasLoading && mHasMessage && scrollView != null) {
            if (mHasTitle && mIsAssignMentLayout) {
                scrollView.setPadding(scrollView.getPaddingLeft(), 0, scrollView.getPaddingRight(),
                        getContext().getResources().getDimensionPixelOffset(
                                R.dimen.coui_alert_dialog_scroll_padding_bottom_message_has_title_in_assignment));
            }
            if (!mIsAssignMentLayout) {
                layout.setNeedSetPaddingLayoutId(R.id.scrollView);
            }
        }
        layout.setHasMessageMerge(mHasMessageMerge);
    }

    private void initSingleContentPadding(Window window) {
        View buttonPanel = window.findViewById(R.id.buttonPanel);
        boolean hasContent = mHasTitle || mHasMessage || mHasSetView || mHasAdapter
                || (mItems != null && mItems.length > 0);
        if (mIsTinyStyle) {
            if (buttonPanel != null && !hasContent) {
                buttonPanel.setPadding(buttonPanel.getPaddingLeft(),
                        getContext().getResources().getDimensionPixelOffset(
                                R.dimen.coui_tiny_dialog_btn_bar_padding_vertical),
                        buttonPanel.getPaddingRight(), buttonPanel.getPaddingBottom());
            }
            return;
        }
        if (!(buttonPanel instanceof COUIButtonBarLayout)) {
            COUILog.e(TAG, "buttonPanel is error; Need to check whether the application has a layout that covers the coui's");
            return;
        }
        COUIButtonBarLayout buttonBarLayout = (COUIButtonBarLayout) buttonPanel;
        buttonBarLayout.setRecommendButtonId(mRecommendButtonId);
        buttonBarLayout.setDynamicLayout(mButtonLayoutDynamicLayout);
        buttonBarLayout.setShowDividerWhenHasItems(mItems != null);
    }

    private void initTitle(Window window) {
        if (mIsTinyStyle || mHasLoading) {
            return;
        }
        View titleTemplate = window.findViewById(R.id.title_template);
        if (!(titleTemplate instanceof LinearLayout)) {
            COUILog.e(TAG, "title_template is error; Need to check whether the application has a layout that covers the coui's");
            return;
        }
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) titleTemplate.getLayoutParams();
        params.topMargin = getContext().getResources().getDimensionPixelOffset(
                R.dimen.coui_no_message_alert_dialog_title_margin_top);
        params.bottomMargin = getContext().getResources().getDimensionPixelOffset(
                R.dimen.coui_no_message_alert_dialog_title_margin_bottom);
        titleTemplate.setLayoutParams(params);
        initTitleScrollView(window, window.findViewById(R.id.alert_title_scroll_view));
        initCOUIDialogTitle(window.findViewById(R.id.alertTitle));
    }

    private void initTitleScrollView(Window window, View view) {
        if (!(view instanceof COUIMaxHeightScrollView)) {
            COUILog.e(TAG, "alert_title_scroll_view is error; Need to check whether the application has a layout that covers the coui's");
            return;
        }
        COUIMaxHeightScrollView scrollView = (COUIMaxHeightScrollView) view;
        scrollView.setMinHeight((window.getContext().getResources().getDimensionPixelOffset(
                R.dimen.coui_alert_dialog_builder_title_scroll_min_height)
                - getContext().getResources().getDimensionPixelOffset(
                R.dimen.coui_no_message_alert_dialog_title_margin_top))
                - getContext().getResources().getDimensionPixelOffset(
                R.dimen.coui_no_message_alert_dialog_title_margin_bottom));
        scrollView.setFillViewport(true);
        View parentPanel = window.findViewById(R.id.parentPanel);
        if (!(parentPanel instanceof COUIAlertDialogMaxLinearLayout)) {
            COUILog.e(TAG, "parentPanelView is error; Need to check whether the application has a layout that covers the coui's");
            return;
        }
        COUIAlertDialogMaxLinearLayout layout = (COUIAlertDialogMaxLinearLayout) parentPanel;
        if (!mHasMessage) {
            layout.setNeedMinHeight(window.getContext().getResources().getDimensionPixelOffset(
                    R.dimen.coui_alert_dialog_builder_parent_panel_min_height_normal));
        }
        layout.setNeedReMeasureLayoutId(scrollView.getId());
    }

    private void initWindow(Window window) {
        if (window == null) {
            return;
        }
        if (isFollowHandMode()) {
            COUIBottomAlertDialogAdjustUtil.adjustToFree(window, mAnchorView, mAnchorViewTouchPoint,
                    mExtraOffsetPoint);
            window.getDecorView().setVisibility(View.INVISIBLE);
        } else {
            Configuration configuration = mConfiguration;
            if (configuration == null) {
                configuration = window.getContext().getResources().getConfiguration();
            }
            updateGravityAndAnimation(configuration);
        }
        window.getDecorView().setOnTouchListener(new OutsideTouchListener(mDialog));
        setWindowType(window);
        setWindowWidth(window);
    }

    private boolean isFollowHandMode() {
        return mAnchorView != null || mAnchorViewTouchPoint != null;
    }

    private boolean isForceCenterStyleInLargeScreen(Configuration configuration) {
        return isLargeScreen(configuration) && mIsForceCenterInLargeScreen;
    }

    private boolean isLargeScreen(Configuration configuration) {
        int widthDp = configuration.screenWidthDp;
        int heightDp = configuration.screenHeightDp;
        if (mForcePhysicalDimensions) {
            widthDp = UIUtil.px2dip(getContext(), UIUtil.getScreenWidthRealSize(getContext()));
            heightDp = UIUtil.px2dip(getContext(), UIUtil.getScreenHeightRealSize(getContext()));
        }
        return COUIResponsiveUtils.isLargePadWindow(getContext(), widthDp, heightDp);
    }

    private boolean isMiddleAndLargeScreen(Configuration configuration) {
        return mAlwaysFollowHand || !COUIResponsiveUtils.isSmallScreenDp(configuration.screenWidthDp);
    }

    public void operateBlur(View view) {
        if (!view.isHardwareAccelerated()) {
            COUILog.e(TAG, "Hardware accelerate is disabled! Set background blur failed.");
            return;
        }
        if (!mBackgroundBlurBuilder.useBackgroundBlur() || mDialog == null || mDialog.getWindow() == null) {
            return;
        }
        Window window = mDialog.getWindow();
        View rootView = window.findViewById(R.id.rootView);
        View parentPanel = window.findViewById(R.id.parentPanel);
        mBackgroundBlurBuilder.setTargetView(rootView);
        mBackgroundBlurBuilder.setRootView(view);
        if (parentPanel instanceof COUIAlertDialogMaxLinearLayout) {
            ((COUIAlertDialogMaxLinearLayout) parentPanel).setBlurBackgroundWindow(
                    mBackgroundBlurBuilder.useBackgroundBlur());
        } else {
            COUILog.e(TAG, "operateBlur: parentPanel is not COUIAlertDialogMaxLinearLayout");
        }
        int weightAttr = mHasLoading ? R.attr.couiRoundCornerMWeight : R.attr.couiRoundCornerXXLWeight;
        if (RoundCornerUtil.isSupportRoundCornerWhenBlur()) {
            mBackgroundBlurBuilder.setSmoothWeight(COUIContextUtil.getAttrFloat(getContext(), weightAttr));
        }
        int radiusAttr = mHasLoading ? R.attr.couiRoundCornerM : R.attr.couiRoundCornerXXL;
        float radius = COUIContextUtil.getAttrDimens(getContext(), radiusAttr);
        if (mIsTinyStyle) {
            mBackgroundBlurBuilder.setCornerRadius(radius, radius, 0.0f, 0.0f);
        } else {
            mBackgroundBlurBuilder.setCornerRadius(radius);
        }
        mBackgroundBlurBuilder.applyBlurBackground();
    }

    public void registerApplicationConfigChangeListener() {
        getContext().registerComponentCallbacks(mComponentCallbacks);
    }

    public void releaseApplicationConfigChangeListener() {
        if (mComponentCallbacks != null) {
            getContext().unregisterComponentCallbacks(mComponentCallbacks);
        }
    }

    private void setCustomLayout() {
        if (!mIsCustomStyle || mDialog == null) {
            return;
        }
        if (mCustomDrawable != null) {
            View customImage = mDialog.findViewById(R.id.customImageview);
            if (customImage instanceof COUIRoundImageView) {
                ((COUIRoundImageView) customImage).setImageDrawable(mCustomDrawable);
                customImage.setVisibility(View.VISIBLE);
            } else {
                COUILog.e(TAG, "customImageview is error; Need to check whether the application has a layout that covers the coui's");
            }
        }
        if (mCustomTitle != null) {
            View customTitle = mDialog.findViewById(R.id.customTitle);
            if (customTitle instanceof TextView) {
                ((TextView) customTitle).setText(mCustomTitle);
                customTitle.setVisibility(View.VISIBLE);
            } else {
                COUILog.e(TAG, "customTitle is error; Need to check whether the application has a layout that covers the coui's");
            }
        }
        if (mCustomMessage != null) {
            View customMessage = mDialog.findViewById(R.id.customMessage);
            if (customMessage instanceof TextView) {
                ((TextView) customMessage).setText(mCustomMessage);
                customMessage.setVisibility(View.VISIBLE);
            } else {
                COUILog.e(TAG, "customMessage is error; Need to check whether the application has a layout that covers the coui's");
            }
        }
    }

    private void setViewHorizontalWeight(View view, int weight) {
        if (view == null) {
            return;
        }
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (layoutParams instanceof LinearLayout.LayoutParams) {
            layoutParams.height = 0;
            ((LinearLayout.LayoutParams) layoutParams).weight = weight;
            view.setLayoutParams(layoutParams);
        }
    }

    private void setWindowWidth(Window window) {
        WindowManager.LayoutParams attrs = window.getAttributes();
        Configuration configuration = mConfiguration;
        if (configuration == null) {
            configuration = window.getContext().getResources().getConfiguration();
        }
        int width = COUIResponsiveUtils.isSmallScreenDp(configuration.screenWidthDp)
                && (configuration.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK)
                == Configuration.SCREENLAYOUT_SIZE_NORMAL
                ? WindowManager.LayoutParams.MATCH_PARENT
                : Math.min(UIUtil.getScreenWidthMetrics(getContext()), mContentMaxWidth);
        if (isFollowHandMode()) {
            width = WindowManager.LayoutParams.WRAP_CONTENT;
        }
        attrs.width = width;
        window.setAttributes(attrs);
    }

    private void updateGravityAndAnimation(Configuration configuration) {
        if (mDialog == null || mDialog.getWindow() == null) {
            return;
        }
        if (isForceCenterStyleInLargeScreen(configuration)) {
            mIsForceCenterStyleStatus = true;
            mDialog.getWindow().setGravity(Gravity.CENTER);
            mDialog.getWindow().setWindowAnimations(DEF_WINDOW_ANIM);
        } else {
            mIsForceCenterStyleStatus = false;
            mDialog.getWindow().setGravity(mGravity);
            mDialog.getWindow().setWindowAnimations(mWindowAnimStyleRes);
        }
    }

    public static Context wrapColorContext(Context context, int colorThemeResId, int themeResId) {
        return new ContextThemeWrapper(new ContextThemeWrapper(context, colorThemeResId), themeResId);
    }

    @Override
    public AlertDialog create() {
        initCustomPanel();
        initAdapter();
        AlertDialog dialog = super.create();
        mDialog = dialog;
        initWindow(dialog.getWindow());
        return mDialog;
    }

    public AlertDialog create(View anchorView) {
        if (!isMiddleAndLargeScreen(getContext().getResources().getConfiguration())) {
            anchorView = null;
        }
        mAnchorView = anchorView;
        return create();
    }

    public AlertDialog create(View anchorView, Point point) {
        return create(anchorView, point.x, point.y);
    }

    public AlertDialog create(View anchorView, int x, int y) {
        return createWithExtraOffset(anchorView, x, y, 0, 0);
    }

    public AlertDialog createWithExtraOffset(View anchorView, int offsetX, int offsetY) {
        return createWithExtraOffset(anchorView, 0, 0, offsetX, offsetY);
    }

    public AlertDialog createWithExtraOffset(View anchorView, int x, int y, int offsetX, int offsetY) {
        if (isMiddleAndLargeScreen(getContext().getResources().getConfiguration())) {
            mAnchorView = anchorView;
            if (x != 0 || y != 0) {
                mAnchorViewTouchPoint = new Point(x, y);
            }
            if (offsetX != 0 || offsetY != 0) {
                mExtraOffsetPoint = new Point(offsetX, offsetY);
            }
        }
        return create();
    }

    public void enforceChangeScreenWidth(int preferWidth) {
        if (preferWidth < 0) {
            Log.d(TAG, "enforceChangeScreenWidth : given value not satisfy : preferWidth =" + preferWidth);
            return;
        }
        try {
            Resources resources = getContext().getResources();
            Configuration configuration = resources.getConfiguration();
            mOriginWidth = configuration.screenWidthDp;
            configuration.screenWidthDp = preferWidth;
            resources.updateConfiguration(configuration, resources.getDisplayMetrics());
            Log.d(TAG, "enforceChangeScreenWidth : OriginWidth=" + mOriginWidth
                    + " ,PreferWidth:" + preferWidth);
        } catch (Exception e) {
            Log.d(TAG, "enforceChangeScreenWidth : failed to updateConfiguration");
        }
    }

    public View getAnchorView() {
        return mAnchorView;
    }

    public int getBottomAlertDialogWindowAnimStyle(Context context) {
        return isMiddleAndLargeScreen(context.getResources().getConfiguration()) && isFollowHandMode()
                ? R.style.Animation_COUI_DialogListWindow : mWindowAnimStyleRes;
    }

    public int getBottomAlertDialogWindowGravity(Context context) {
        if (isMiddleAndLargeScreen(context.getResources().getConfiguration()) && isFollowHandMode()) {
            return Gravity.TOP | Gravity.START;
        }
        return mGravity;
    }

    public void initAdapter() {
        if (mCOUIListDialogAdapter != null) {
            mCOUIListDialogAdapter.setIsTop(!mHasTitle && !mHasMessage);
            mCOUIListDialogAdapter.setIsBottom(!mHasSetView && !mHasSetButton);
        }
        if (mChoiceListAdapter != null) {
            mChoiceListAdapter.setIsTop(!mHasTitle && !mHasMessage);
            mChoiceListAdapter.setIsBottom(!mHasSetView && !mHasSetButton);
        }
        if (!mHasAdapter && mItems != null && mItems.length > 0) {
            setAdapter(new SummaryAdapter(getContext(), !mHasTitle && !mHasMessage,
                    !mHasSetView && !mHasSetButton, mItems, mSummaryItems, mTextColor),
                    mItemClickListener);
        }
    }

    public void restoreScreenWidth() {
        if (mOriginWidth == UNSET_WIDTH) {
            return;
        }
        try {
            Resources resources = getContext().getResources();
            Configuration configuration = resources.getConfiguration();
            configuration.screenWidthDp = mOriginWidth;
            resources.updateConfiguration(configuration, resources.getDisplayMetrics());
            Log.d(TAG, "restoreScreenWidth : OriginWidth=" + mOriginWidth);
            mOriginWidth = UNSET_WIDTH;
        } catch (Exception e) {
            Log.d(TAG, "restoreScreenWidth : failed to updateConfiguration");
        }
    }

    private void setWindowType(Window window) {
        WindowManager.LayoutParams attrs = window.getAttributes();
        if (mDialogWindowType > 0) {
            attrs.type = mDialogWindowType;
        }
        window.setAttributes(attrs);
    }

    public COUIAlertDialogBuilder setAlwaysFollowHand(boolean alwaysFollowHand) {
        mAlwaysFollowHand = alwaysFollowHand;
        return this;
    }

    public COUIAlertDialogBuilder setAnchorView(View anchorView) {
        mAnchorView = anchorView;
        return this;
    }

    public COUIAlertDialogBuilder setAnchorViewTouchPoint(Point point) {
        mAnchorViewTouchPoint = point;
        return this;
    }

    public COUIAlertDialogBuilder setBlurBackgroundDrawable(boolean useBlur) {
        setBlurBackgroundDrawable(useBlur, UIUtil.ANIM_LEVEL_SUPPORT_BLUR_MIN);
        return this;
    }

    public COUIAlertDialogBuilder setBlurBackgroundDrawable(boolean useBlur, AnimLevel animLevel) {
        mBackgroundBlurBuilder.setUseBackgroundBlur(useBlur, animLevel);
        return this;
    }

    public COUIAlertDialogBuilder setButtonLayoutDynamicLayout(boolean dynamicLayout) {
        mButtonLayoutDynamicLayout = dynamicLayout;
        return this;
    }

    public COUIAlertDialogBuilder setCustomDialogPaddingBottom(int paddingBottom) {
        mCustomDialogPaddingBottom = paddingBottom;
        return this;
    }

    public COUIAlertDialogBuilder setCustomDrawable(Drawable drawable) {
        mCustomDrawable = drawable;
        return this;
    }

    public COUIAlertDialogBuilder setCustomMessage(CharSequence message) {
        mCustomMessage = message;
        return this;
    }

    public COUIAlertDialogBuilder setCustomTitle(String title) {
        mCustomTitle = title;
        return this;
    }

    public COUIAlertDialogBuilder setExtraOffsetPoint(Point point) {
        mExtraOffsetPoint = point;
        return this;
    }

    public COUIAlertDialogBuilder setForcePhysicalDimensions(boolean forcePhysicalDimensions) {
        mForcePhysicalDimensions = forcePhysicalDimensions;
        return this;
    }

    public COUIAlertDialogBuilder setHasMessageMerge(boolean hasMessageMerge) {
        mHasMessageMerge = hasMessageMerge;
        return this;
    }

    public void setHasSetButton(boolean hasSetButton) {
        mHasSetButton = hasSetButton;
    }

    public COUIAlertDialogBuilder setIsForceCenterInLargeScreen(boolean forceCenter) {
        mIsForceCenterInLargeScreen = forceCenter;
        return this;
    }

    public COUIAlertDialogBuilder setNeedToAdaptMessageAndList(boolean needToAdapt) {
        mIsNeedToAdaptMessageAndList = needToAdapt;
        return this;
    }

    public COUIAlertDialogBuilder setRegisterConfigurationChangeCallBack(boolean register) {
        mRegisterConfigurationChangeCallBack = register;
        return this;
    }

    public COUIAlertDialogBuilder setSummaryItems(CharSequence[] summaryItems) {
        mSummaryItems = summaryItems;
        return this;
    }

    public COUIAlertDialogBuilder setSummaryItems(int summaryItemsResId) {
        mSummaryItems = getContext().getResources().getTextArray(summaryItemsResId);
        return this;
    }

    public void setSupportDynamicMarginTop(boolean supportDynamicMarginTop) {
        mSupportDynamicMarginTop = supportDynamicMarginTop;
    }

    public COUIAlertDialogBuilder setWindowAnimStyle(int styleResId) {
        mWindowAnimStyleRes = styleResId;
        return this;
    }

    public COUIAlertDialogBuilder setWindowGravity(int gravity) {
        mGravity = gravity;
        return this;
    }

    public COUIAlertDialogBuilder setWindowType(int type) {
        mDialogWindowType = type;
        return this;
    }

    @Override
    public AlertDialog show() {
        AlertDialog dialog = super.show();
        mDialog = dialog;
        disabledTitleScroll(dialog);
        updateViewAfterShown();
        return dialog;
    }

    public AlertDialog show(View anchorView) {
        if (!isMiddleAndLargeScreen(getContext().getResources().getConfiguration())) {
            anchorView = null;
        }
        mAnchorView = anchorView;
        return show();
    }

    public void updateGravityWhileConfigChange(Configuration configuration) {
        if (mDialog == null || mDialog.getWindow() == null) {
            return;
        }
        Configuration dialogConfiguration = mDialog.getContext().getResources().getConfiguration();
        boolean shouldResetScrollHeights = false;
        if (mOldConfigurationWidthDP != configuration.screenWidthDp) {
            shouldResetScrollHeights = configuration.screenWidthDp == dialogConfiguration.screenWidthDp;
        }
        if (!shouldResetScrollHeights
                && mOldConfigurationHeightDP != configuration.screenHeightDp
                && configuration.screenHeightDp == dialogConfiguration.screenHeightDp) {
            shouldResetScrollHeights = true;
        }
        if (shouldResetScrollHeights) {
            if (mHasTitle) {
                View titleScroll = mDialog.findViewById(R.id.alert_title_scroll_view);
                if (titleScroll instanceof COUIMaxHeightScrollView) {
                    ((COUIMaxHeightScrollView) titleScroll).setMaxHeight(
                            getContext().getResources().getDimensionPixelSize(
                                    R.dimen.coui_alert_dialog_builder_title_scroll_max_height));
                } else {
                    COUILog.e(TAG, "alert_title_scroll_view is error; Need to check whether the application has a layout that covers the coui's");
                }
            }
            if (mHasMessage) {
                View scrollView = mDialog.findViewById(R.id.scrollView);
                if (scrollView instanceof COUIMaxHeightNestedScrollView) {
                    ((COUIMaxHeightNestedScrollView) scrollView).setMaxHeight(
                            getContext().getResources().getDimensionPixelSize(
                                    R.dimen.coui_alert_dialog_builder_content_max_height));
                } else {
                    COUILog.e(TAG, "scrollView is error; Need to check whether the application has a layout that covers the coui's");
                }
            }
        }
        mOldConfigurationWidthDP = configuration.screenWidthDp;
        mOldConfigurationHeightDP = configuration.screenHeightDp;
        if (isFollowHandMode()) {
            mAnchorViewTouchPoint = null;
            mAnchorView = null;
            if (mContentView != null) {
                View custom = mDialog.getWindow().findViewById(R.id.custom);
                if (custom instanceof FrameLayout) {
                    ((FrameLayout) custom).removeView(mContentView);
                } else {
                    COUILog.e(TAG, "custom is error; Need to check whether the application has a layout that covers the coui's");
                }
            }
            mDialog.dismiss();
            show();
            return;
        }
        if (mIsForceCenterStyleStatus != isForceCenterStyleInLargeScreen(configuration)) {
            updateGravityAndAnimation(configuration);
        }
        setWindowWidth(mDialog.getWindow());
    }

    public void updateViewAfterShown() {
        if (mDialog == null || mDialog.getWindow() == null) {
            return;
        }
        Window window = mDialog.getWindow();
        initTitle(window);
        initMessagePadding();
        initCustomPanelVisibility(window);
        initListPanel(window);
        initContentMaxWidth(window);
        initContentMaxHeight(window);
        initSingleContentPadding(window);
        setCustomLayout();
        initBlurListener();
        setWindowWidth(window);
    }

    @Override
    public COUIAlertDialogBuilder setAdapter(ListAdapter adapter, DialogInterface.OnClickListener listener) {
        mHasAdapter = adapter != null;
        if (adapter instanceof COUIListDialogAdapter) {
            mCOUIListDialogAdapter = (COUIListDialogAdapter) adapter;
        }
        super.setAdapter(adapter, listener);
        return this;
    }

    @Override
    public COUIAlertDialogBuilder setSingleChoiceItems(ListAdapter adapter, int checkedItem,
            DialogInterface.OnClickListener listener) {
        mHasAdapter = adapter != null;
        if (adapter instanceof ChoiceListAdapter) {
            mChoiceListAdapter = (ChoiceListAdapter) adapter;
        }
        super.setSingleChoiceItems(adapter, checkedItem, listener);
        return this;
    }

    @Override
    public COUIAlertDialogBuilder setView(int layoutResId) {
        mHasSetView = true;
        super.setView(layoutResId);
        return this;
    }

    @Override
    public COUIAlertDialogBuilder setView(View view) {
        mHasSetView = true;
        mContentView = view;
        super.setView(view);
        return this;
    }

    @Override
    public COUIAlertDialogBuilder setItems(int itemsId, DialogInterface.OnClickListener listener) {
        mItems = getContext().getResources().getTextArray(itemsId);
        mItemClickListener = listener;
        super.setItems(itemsId, listener);
        return this;
    }

    public COUIAlertDialogBuilder setItems(int itemsId, DialogInterface.OnClickListener listener,
            int[] textColor) {
        mItems = getContext().getResources().getTextArray(itemsId);
        mItemClickListener = listener;
        mTextColor = textColor;
        super.setItems(itemsId, listener);
        return this;
    }

    @Override
    public COUIAlertDialogBuilder setItems(CharSequence[] items, DialogInterface.OnClickListener listener) {
        mItems = items;
        mItemClickListener = listener;
        super.setItems(items, listener);
        return this;
    }

    @Override
    public COUIAlertDialogBuilder setMessage(int messageId) {
        mHasMessage = !TextUtils.isEmpty(getContext().getString(messageId));
        super.setMessage(messageId);
        return this;
    }

    @Override
    public COUIAlertDialogBuilder setMessage(CharSequence message) {
        mHasMessage = !TextUtils.isEmpty(message);
        super.setMessage(message);
        return this;
    }

    @Override
    public COUIAlertDialogBuilder setTitle(int titleId) {
        mHasTitle = !TextUtils.isEmpty(getContext().getString(titleId));
        super.setTitle(titleId);
        return this;
    }

    @Override
    public COUIAlertDialogBuilder setTitle(CharSequence title) {
        mHasTitle = !TextUtils.isEmpty(title);
        super.setTitle(title);
        return this;
    }

    @Override
    public COUIAlertDialogBuilder setNegativeButton(int textId, DialogInterface.OnClickListener listener) {
        super.setNegativeButton(textId, listener);
        setHasSetButton(true);
        return this;
    }

    @Override
    public COUIAlertDialogBuilder setNeutralButton(int textId, DialogInterface.OnClickListener listener) {
        super.setNeutralButton(textId, listener);
        setHasSetButton(true);
        return this;
    }

    @Override
    public COUIAlertDialogBuilder setPositiveButton(int textId, DialogInterface.OnClickListener listener) {
        super.setPositiveButton(textId, listener);
        setHasSetButton(true);
        return this;
    }

    @Override
    public COUIAlertDialogBuilder setNegativeButton(CharSequence text,
            DialogInterface.OnClickListener listener) {
        super.setNegativeButton(text, listener);
        setHasSetButton(true);
        return this;
    }

    @Override
    public COUIAlertDialogBuilder setNeutralButton(CharSequence text,
            DialogInterface.OnClickListener listener) {
        super.setNeutralButton(text, listener);
        setHasSetButton(true);
        return this;
    }

    @Override
    public COUIAlertDialogBuilder setPositiveButton(CharSequence text,
            DialogInterface.OnClickListener listener) {
        super.setPositiveButton(text, listener);
        setHasSetButton(true);
        return this;
    }

    public COUIAlertDialogBuilder setNegativeButton(int textId,
            DialogInterface.OnClickListener listener, boolean recommend) {
        super.setNegativeButton(textId, listener);
        setHasSetButton(true);
        if (recommend) {
            mRecommendButtonId = android.R.id.button2;
        }
        return this;
    }

    public COUIAlertDialogBuilder setNeutralButton(int textId,
            DialogInterface.OnClickListener listener, boolean recommend) {
        super.setNeutralButton(textId, listener);
        setHasSetButton(true);
        if (recommend) {
            mRecommendButtonId = android.R.id.button3;
        }
        return this;
    }

    public COUIAlertDialogBuilder setPositiveButton(int textId,
            DialogInterface.OnClickListener listener, boolean recommend) {
        super.setPositiveButton(textId, listener);
        setHasSetButton(true);
        if (recommend) {
            mRecommendButtonId = android.R.id.button1;
        }
        return this;
    }

    public COUIAlertDialogBuilder setNegativeButton(CharSequence text,
            DialogInterface.OnClickListener listener, boolean recommend) {
        super.setNegativeButton(text, listener);
        setHasSetButton(true);
        if (recommend) {
            mRecommendButtonId = android.R.id.button2;
        }
        return this;
    }

    public COUIAlertDialogBuilder setNeutralButton(CharSequence text,
            DialogInterface.OnClickListener listener, boolean recommend) {
        super.setNeutralButton(text, listener);
        setHasSetButton(true);
        if (recommend) {
            mRecommendButtonId = android.R.id.button3;
        }
        return this;
    }

    public COUIAlertDialogBuilder setPositiveButton(CharSequence text,
            DialogInterface.OnClickListener listener, boolean recommend) {
        super.setPositiveButton(text, listener);
        setHasSetButton(true);
        if (recommend) {
            mRecommendButtonId = android.R.id.button1;
        }
        return this;
    }
}
