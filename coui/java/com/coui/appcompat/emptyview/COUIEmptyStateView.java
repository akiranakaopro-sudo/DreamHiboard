package com.coui.appcompat.emptyview;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Size;
import android.util.SparseArray;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.coui.appcompat.R;
import com.coui.appcompat.accessibilityutil.COUIAccessibilityUtil;
import com.coui.appcompat.contextutil.COUIContextUtil;
import com.coui.appcompat.textviewcompatutil.COUITextViewCompatUtil;

public final class COUIEmptyStateView extends LinearLayout {
    public static final int EMPTY_VIEW_SIZE_TYPE_AUTO = 0;
    public static final int EMPTY_VIEW_SIZE_TYPE_SMALL = 1;
    public static final int EMPTY_VIEW_SIZE_TYPE_MEDIUM = 2;
    public static final int EMPTY_VIEW_SIZE_TYPE_LARGE = 3;

    private static final float ANIM_SIZE_SCALE_VALUE_NORMAL = 1.0f;
    private static final float ANIM_SIZE_SCALE_VALUE_SMALL = 0.6f;
    private static final float HEIGHT_RATIO_NORMAL = 0.45f;
    private static final float HEIGHT_RATIO_SMALL = 0.5f;
    private static final int INVALID_VALUE = -1;

    private String mActionText = "";
    private String mAnimFileName = "";
    private int mAnimHeight;
    private int mAnimWidth;
    private boolean mAutoPlay;
    private final int mDefaultAnimHeight;
    private final int mDefaultAnimWidth;
    private final LinearLayout mEmptyStateGroup;
    private int mEmptyViewSizeType;
    private final int mHeightThresholdMedium;
    private final int mHeightThresholdSmall;
    private int mImageRes = INVALID_VALUE;
    private int mRawAnimRes = INVALID_VALUE;
    private String mSubtitleText = "";
    private String mTitleText = "";
    private final int mWidthThresholdMedium;

    public COUIEmptyStateView(Context context) {
        this(context, null);
    }

    public COUIEmptyStateView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public COUIEmptyStateView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public COUIEmptyStateView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mDefaultAnimHeight = getDimenPx(R.dimen.coui_component_empty_anim_view_height_normal);
        mDefaultAnimWidth = getDimenPx(R.dimen.coui_component_empty_anim_view_width_normal);
        mWidthThresholdMedium = getDimenPx(R.dimen.coui_component_width_threshold_medium);
        mHeightThresholdMedium = getDimenPx(R.dimen.coui_component_height_threshold_medium);
        mHeightThresholdSmall = getDimenPx(R.dimen.coui_component_height_threshold_small);

        setOverScrollMode(OVER_SCROLL_ALWAYS);
        setVerticalScrollBarEnabled(false);
        setHorizontalScrollBarEnabled(false);

        View view = View.inflate(context, R.layout.coui_component_empty_state, null);
        mEmptyStateGroup = (LinearLayout) view;
        addView(mEmptyStateGroup, new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        COUITextViewCompatUtil.setPressRippleDrawable(getActionBt());
        getActionBt().setAccessibilityDelegate(new View.AccessibilityDelegate() {
            @Override
            public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
                super.onInitializeAccessibilityNodeInfo(host, info);
                info.setClassName(COUIAccessibilityUtil.BUTTON_CLASS_NAME);
            }
        });

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.COUIEmptyStateView,
                defStyleAttr, defStyleRes);
        mAutoPlay = a.getBoolean(R.styleable.COUIEmptyStateView_anim_autoPlay, false);
        setActionText(getStringOrEmpty(a, R.styleable.COUIEmptyStateView_actionText));
        setTitleText(getStringOrEmpty(a, R.styleable.COUIEmptyStateView_titleText));
        setSubtitleText(getStringOrEmpty(a, R.styleable.COUIEmptyStateView_subtitleText));
        setRawAnimRes(a.getResourceId(R.styleable.COUIEmptyStateView_anim_rawRes, INVALID_VALUE));
        setAnimFileName(getStringOrEmpty(a, R.styleable.COUIEmptyStateView_anim_fileName));
        setImageRes(a.getResourceId(R.styleable.COUIEmptyStateView_android_src, INVALID_VALUE));
        mAnimHeight = a.getDimensionPixelSize(R.styleable.COUIEmptyStateView_animHeight,
                mDefaultAnimHeight);
        mAnimWidth = a.getDimensionPixelSize(R.styleable.COUIEmptyStateView_animWidth,
                mDefaultAnimWidth);
        setEmptyViewSizeType(a.getInteger(R.styleable.COUIEmptyStateView_emptyViewSizeType,
                EMPTY_VIEW_SIZE_TYPE_AUTO));
        a.recycle();
    }

    private int calculateTopMargin(int groupHeight) {
        return Math.max(Math.round((getMeasuredHeight() - groupHeight)
                * getMarginTopHeightRatio(getEmptyStateGroupSizeType())), 0);
    }

    private TextView getActionBt() {
        return findViewById(R.id.empty_view_action);
    }

    private Size getAnimTargetSize(int sizeType) {
        float scale = sizeType == EMPTY_VIEW_SIZE_TYPE_SMALL ? 0.0f
                : sizeType == EMPTY_VIEW_SIZE_TYPE_MEDIUM ? ANIM_SIZE_SCALE_VALUE_SMALL
                : ANIM_SIZE_SCALE_VALUE_NORMAL;
        return new Size((int) (mAnimWidth * scale), (int) (mAnimHeight * scale));
    }

    private EmptyStateAnimView getAnimView() {
        return findViewById(R.id.empty_view_anim);
    }

    private int getDimenPx(int resId) {
        return getContext().getResources().getDimensionPixelSize(resId);
    }

    private int getEmptyStateGroupSizeType() {
        return getEmptyStateGroupSizeType(getMeasuredWidth(), getMeasuredHeight());
    }

    private int getEmptyStateGroupSizeType(int width, int height) {
        if (mEmptyViewSizeType != EMPTY_VIEW_SIZE_TYPE_AUTO) {
            return mEmptyViewSizeType;
        }
        if (height < mHeightThresholdSmall) {
            return EMPTY_VIEW_SIZE_TYPE_SMALL;
        }
        return width < mWidthThresholdMedium || height < mHeightThresholdMedium
                ? EMPTY_VIEW_SIZE_TYPE_MEDIUM : EMPTY_VIEW_SIZE_TYPE_LARGE;
    }

    private float getMarginTopHeightRatio(int sizeType) {
        return sizeType == EMPTY_VIEW_SIZE_TYPE_SMALL ? HEIGHT_RATIO_SMALL : HEIGHT_RATIO_NORMAL;
    }

    private String getStringOrEmpty(TypedArray a, int index) {
        String value = a.getString(index);
        return value == null ? "" : value;
    }

    private TextView getSubTitle() {
        return findViewById(R.id.empty_view_subtitle);
    }

    private TextView getTitle() {
        return findViewById(R.id.empty_view_title);
    }

    // Leapy modified 2026-07-26: Keep the decoded OPPO empty-state resource
    // routing while rendering it with upstream Lottie.
    private void updateAnimRes(LottieAnimationView animView, int resId) {
        if (resId > 0) {
            animView.setAnimation(resId);
        }
    }

    private void updateAnimRes(LottieAnimationView animView, String fileName) {
        if (!TextUtils.isEmpty(fileName)) {
            animView.setAnimation(fileName);
        }
    }

    private void updateContentOrVisibility(TextView textView, String text) {
        textView.setText(text);
        textView.setVisibility(TextUtils.isEmpty(text) ? GONE : VISIBLE);
    }

    private void updateImageRes(LottieAnimationView animView, int resId) {
        if (resId != 0) {
            animView.setImageResource(resId);
        }
    }

    public void cancelAnimation() {
        getAnimView().cancelAnimation();
    }

    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
    }

    public String getActionText() {
        return mActionText;
    }

    public String getAnimFileName() {
        return mAnimFileName;
    }

    public int getAnimHeight() {
        return mAnimHeight;
    }

    public int getAnimWidth() {
        return mAnimWidth;
    }

    public boolean getAutoPlay() {
        return mAutoPlay;
    }

    public int getEmptyViewSizeType() {
        return mEmptyViewSizeType;
    }

    public int getImageRes() {
        return mImageRes;
    }

    public int getRawAnimRes() {
        return mRawAnimRes;
    }

    public String getSubtitleText() {
        return mSubtitleText;
    }

    public String getTitleText() {
        return mTitleText;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mAutoPlay && getAnimView().getVisibility() != INVISIBLE) {
            getAnimView().playAnimation();
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int paddingTop = getPaddingTop() + calculateTopMargin(mEmptyStateGroup.getMeasuredHeight());
        int measuredHeight = mEmptyStateGroup.getMeasuredHeight() + paddingTop;
        int measuredWidth = (getMeasuredWidth() - mEmptyStateGroup.getMeasuredWidth()) / 2;
        mEmptyStateGroup.layout(measuredWidth, paddingTop,
                mEmptyStateGroup.getMeasuredWidth() + measuredWidth, measuredHeight);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int mode = MeasureSpec.getMode(heightMeasureSpec);
        getAnimView().setAnimSize(getAnimTargetSize(getEmptyStateGroupSizeType(
                MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec))));
        measureChild(mEmptyStateGroup, widthMeasureSpec, heightMeasureSpec);
        if (mode != MeasureSpec.EXACTLY) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(mEmptyStateGroup.getMeasuredHeight(), mode);
        }
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    public void playAnimation() {
        getAnimView().playAnimation();
    }

    public void refresh() {
        getTitle().setTextColor(COUIContextUtil.getAttrColor(getContext(), R.attr.couiColorLabelPrimary));
        getSubTitle().setTextColor(COUIContextUtil.getAttrColor(getContext(), R.attr.couiColorLabelSecondary));
        getActionBt().setTextColor(COUIContextUtil.getAttrColor(getContext(), R.attr.couiColorPrimaryText));
    }

    public void setActionText(String value) {
        updateContentOrVisibility(getActionBt(), value);
        mActionText = value;
    }

    public void setActionText(int resId) {
        getActionBt().setText(resId);
    }

    public void setAnimFileName(String value) {
        if (TextUtils.equals(value, mAnimFileName)) {
            return;
        }
        updateAnimRes(getAnimView(), value);
        mAnimFileName = value;
    }

    public void setAnimHeight(int animHeight) {
        mAnimHeight = animHeight;
    }

    public void setAnimRes(int resId) {
        updateAnimRes(getAnimView(), resId);
    }

    public void setAnimWidth(int animWidth) {
        mAnimWidth = animWidth;
    }

    public void setAutoPlay(boolean autoPlay) {
        mAutoPlay = autoPlay;
    }

    public void setEmptyViewSizeType(int sizeType) {
        if (sizeType != mEmptyViewSizeType) {
            getAnimView().requestLayout();
            mEmptyViewSizeType = sizeType;
        }
    }

    public void setImageRes(int resId) {
        if (resId != mImageRes) {
            updateImageRes(getAnimView(), resId);
            mImageRes = resId;
        }
    }

    public void setOnButtonClickListener(View.OnClickListener listener) {
        getActionBt().setOnClickListener(listener);
    }

    public void setRawAnimRes(int resId) {
        if (resId != mRawAnimRes) {
            updateAnimRes(getAnimView(), resId);
            mRawAnimRes = resId;
        }
    }

    public void setSubtitle(int resId) {
        TextView subTitle = getSubTitle();
        subTitle.setText(resId);
        CharSequence text = subTitle.getText();
        subTitle.setVisibility(TextUtils.isEmpty(text) ? GONE : VISIBLE);
    }

    public void setSubtitleText(String value) {
        updateContentOrVisibility(getSubTitle(), value);
        mSubtitleText = value;
    }

    public void setTitleText(String value) {
        updateContentOrVisibility(getTitle(), value);
        mTitleText = value;
    }

    public void setTitleText(int resId) {
        TextView title = getTitle();
        title.setText(resId);
        CharSequence text = title.getText();
        title.setVisibility(TextUtils.isEmpty(text) ? GONE : VISIBLE);
    }
}
