package com.coui.appcompat.preference;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;
import androidx.recyclerview.widget.COUIRecyclerView;

import com.coui.appcompat.R;
import com.coui.appcompat.cardlist.COUICardListHelper;
import com.coui.appcompat.cardlist.COUICardListSelectedItemLayout;
import com.coui.appcompat.imageview.COUIRoundImageView;
import com.coui.appcompat.poplist.PreciseClickHelper;
import com.coui.appcompat.reddot.COUIHintRedDot;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class COUIPreference extends Preference implements COUICardSupportInterface, COUIRecyclerView.ICOUIDividerDecorationInterface {
    public static final int CIRCLE = 0;
    static final int DEFAULT_RADIUS = 14;
    static final int DEFAULT_SCALE = 3;
    public static final int FORCE_CLICK = 1;
    public static final int FORCE_UNCLICK = 2;
    static final int MAX_RADIUS = 36;
    static final int MIN_RADIUS = 14;
    public static final int NORMAL = 0;
    static final int NO_ICON_HEIGHT = 0;
    public static final int ROUND = 1;
    public static final int SUMMARY_LINE_DEFAULT = 0;
    public static final int SUMMARY_LINE_ONE = 1;
    public static final int SUMMARY_LINE_TWO = 2;
    private static final String TAG = "COUIPreference-";
    static final int ratio = 6;

    private COUIHintRedDot assignRedDot;
    private COUIRoundImageView assignmentIcon;
    private COUIHintRedDot endRedDot;
    private View iconRedDot;
    private Drawable mAssignIconRes;
    private int mAssignRedDotMode;
    private CharSequence mAssignment;
    private int mAssignmentColor;
    private int mClickStyle;
    private final Context mContext;
    private boolean mCouiSetDefaultColor;
    private int mCouiSummaryLineLimit;
    private final int mDividerDefaultHorizontalPadding;
    private int mEndRedDotMode;
    private int mEndRedDotNum;
    private boolean mHasBorder;
    private int mIconRedDotMode;
    private int mIconStyle;
    private boolean mIsBackgroundAnimationEnabled;
    private boolean mIsCustom;
    private boolean mIsEnableClickSpan;
    private boolean mIsSelected;
    private boolean mIsSupportCardUse;
    private View mItemView;
    Drawable mJumpRes;
    private PreciseClickHelper mPreciseHelper;
    protected PreciseClickHelper.OnPreciseClickListener mPreciseListener;
    private int mRadius;
    private boolean mShowDivider;
    CharSequence mStatusText1;
    private ColorStateList mSummaryTextColor;
    private TextView mSummaryView;
    private ColorStateList mTitleTextColor;
    private TextView mTitleView;

    @Retention(RetentionPolicy.SOURCE)
    public @interface SummaryLineType {
    }

    public COUIPreference(Context context) {
        this(context, null);
    }

    public COUIPreference(Context context, AttributeSet attrs) {
        this(context, attrs, androidx.preference.R.attr.preferenceStyle);
    }

    public COUIPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public COUIPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mShowDivider = true;
        mClickStyle = NORMAL;
        mIsSelected = false;
        mIsBackgroundAnimationEnabled = true;
        mIsCustom = false;
        mTitleTextColor = null;
        mSummaryTextColor = null;
        mCouiSetDefaultColor = false;
        mCouiSummaryLineLimit = SUMMARY_LINE_DEFAULT;
        mContext = context;
        mDividerDefaultHorizontalPadding = context.getResources().getDimensionPixelSize(R.dimen.coui_preference_divider_default_horizontal_padding);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.COUIPreference, defStyleAttr, defStyleRes);
        mShowDivider = a.getBoolean(R.styleable.COUIPreference_couiShowDivider, mShowDivider);
        mIsEnableClickSpan = a.getBoolean(R.styleable.COUIPreference_couiEnalbeClickSpan, false);
        mJumpRes = a.getDrawable(R.styleable.COUIPreference_coui_jump_mark);
        mAssignIconRes = a.getDrawable(R.styleable.COUIPreference_coui_assign_icon);
        mStatusText1 = a.getText(R.styleable.COUIPreference_coui_jump_status1);
        mClickStyle = a.getInt(R.styleable.COUIPreference_couiClickStyle, NORMAL);
        mAssignment = a.getText(R.styleable.COUIPreference_couiAssignment);
        mAssignmentColor = a.getInt(R.styleable.COUIPreference_couiAssignmentColor, 0);
        mIconStyle = a.getInt(R.styleable.COUIPreference_couiIconStyle, ROUND);
        mHasBorder = a.getBoolean(R.styleable.COUIPreference_hasBorder, false);
        mRadius = a.getDimensionPixelSize(R.styleable.COUIPreference_preference_icon_radius, DEFAULT_RADIUS);
        mIconRedDotMode = a.getInt(R.styleable.COUIPreference_iconRedDotMode, 0);
        mEndRedDotMode = a.getInt(R.styleable.COUIPreference_endRedDotMode, 0);
        mAssignRedDotMode = a.getInt(R.styleable.COUIPreference_assignRedDotMode, 0);
        mEndRedDotNum = a.getInt(R.styleable.COUIPreference_endRedDotNum, 0);
        mIsBackgroundAnimationEnabled = a.getBoolean(R.styleable.COUIPreference_isBackgroundAnimationEnabled, true);
        mIsSupportCardUse = a.getBoolean(R.styleable.COUIPreference_isSupportCardUse, true);
        mCouiSetDefaultColor = a.getBoolean(R.styleable.COUIPreference_couiSetDefaultColor, false);
        if (mCouiSetDefaultColor) {
            mTitleTextColor = a.getColorStateList(R.styleable.COUIPreference_titleTextColor);
            mSummaryTextColor = a.getColorStateList(R.styleable.COUIPreference_couiSummaryColor);
        }
        mIsCustom = a.getBoolean(R.styleable.COUIPreference_couiIsCustomIcon, false);
        mCouiSummaryLineLimit = a.getInt(R.styleable.COUIPreference_couiSummaryLineLimit, SUMMARY_LINE_DEFAULT);
        a.recycle();
    }

    private void initPreciseHelper() {
        if (mItemView == null || mPreciseListener == null) {
            return;
        }
        removePreciseClickListener();
        mPreciseHelper = new PreciseClickHelper(mItemView, (view, x, y) -> mPreciseListener.onClick(view, x, y));
        mPreciseHelper.setup();
    }

    public void changeEndRedDotNumberWithAnim(int number) {
        if (endRedDot != null) {
            mEndRedDotNum = number;
            endRedDot.changePointNumber(number);
            if (number > 0) {
                endRedDot.setPointNumber(number);
            }
        }
    }

    public void dismissAssignRedDot() {
        if (assignRedDot != null && assignRedDot.getVisibility() == View.VISIBLE) {
            assignRedDot.executeScaleAnim(false);
            notifyChanged();
        }
    }

    public void dismissEndRedDot() {
        if (endRedDot != null && endRedDot.getVisibility() == View.VISIBLE) {
            endRedDot.executeScaleAnim(false);
            notifyChanged();
        }
    }

    public void dismissIconRedDot() {
        if (iconRedDot instanceof COUIHintRedDot && iconRedDot.getVisibility() == View.VISIBLE) {
            ((COUIHintRedDot) iconRedDot).executeScaleAnim(false);
            notifyChanged();
        }
    }

    public boolean drawDivider() {
        if (!mShowDivider || !(mItemView instanceof COUICardListSelectedItemLayout)) {
            return false;
        }
        int positionInGroup = COUICardListHelper.getPositionInGroup(this);
        return positionInGroup == COUICardListHelper.HEAD || positionInGroup == COUICardListHelper.MIDDLE;
    }

    public int getAssignRedDotMode() { return mAssignRedDotMode; }
    public CharSequence getAssignment() { return mAssignment; }
    public int getBorderRectRadius(int position) { return (position == 1 || position == 2 || position != 3) ? MIN_RADIUS : 16; }
    public int getClickStyle() { return mClickStyle; }
    @Override public View getDividerEndAlignView() { return null; }
    @Override public int getDividerEndInset() { return mDividerDefaultHorizontalPadding; }
    @Override public View getDividerStartAlignView() { return mTitleView; }
    @Override public int getDividerStartInset() { return mDividerDefaultHorizontalPadding; }
    public int getEndRedDotMode() { return mEndRedDotMode; }
    public int getEndRedDotNum() { return mEndRedDotNum; }
    public int getIconRedDotMode() { return mIconRedDotMode; }
    public int getIconStyle() { return mIconStyle; }
    public boolean getIsSelected() { return mIsSelected; }
    public CharSequence getStatusText1() { return mStatusText1; }
    public boolean isShowDivider() { return mShowDivider; }
    @Override public boolean isSupportCardUse() { return mIsSupportCardUse; }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        COUICardListHelper.setItemCardBackground(holder.itemView, COUICardListHelper.getPositionInGroup(this));
        if (holder.itemView instanceof COUICardListSelectedItemLayout) {
            ((COUICardListSelectedItemLayout) holder.itemView).consumeDispatchingEventForState(false);
        }
        View preferenceView = holder.findViewById(R.id.coui_preference);
        if (preferenceView != null) {
            if (mClickStyle == FORCE_CLICK) {
                preferenceView.setClickable(false);
            } else if (mClickStyle == FORCE_UNCLICK) {
                preferenceView.setClickable(true);
            }
        }
        mItemView = holder.itemView;
        initPreciseHelper();
        if (mItemView instanceof ListSelectedItemLayout) {
            ((ListSelectedItemLayout) mItemView).setBackgroundAnimationEnabled(mIsBackgroundAnimationEnabled);
        }
        if (mItemView instanceof COUICardListSelectedItemLayout) {
            ((COUICardListSelectedItemLayout) mItemView).setIsSelected(mIsSelected);
        }
        if (mAssignmentColor == 0) {
            COUIPreferenceUtils.bindView(holder, mJumpRes, mStatusText1, getAssignment());
        } else {
            COUIPreferenceUtils.bindView(holder, mJumpRes, mStatusText1, getAssignment(), mAssignmentColor);
        }
        COUIPreferenceUtils.setTitleViewColor(getContext(), holder, mTitleTextColor);
        COUIPreferenceUtils.setIconStyle(holder, getContext(), mRadius, mHasBorder, mIconStyle, mIsCustom);
        COUIPreferenceUtils.setSummaryViewColor(holder, mSummaryTextColor);
        if (mIsEnableClickSpan) {
            COUIPreferenceUtils.setSummaryView(getContext(), holder);
        }
        mTitleView = (TextView) holder.findViewById(android.R.id.title);
        mSummaryView = (TextView) holder.findViewById(android.R.id.summary);
        if (mSummaryView != null) {
            if (mCouiSummaryLineLimit == SUMMARY_LINE_DEFAULT) {
                mSummaryView.setMaxLines(Integer.MAX_VALUE);
                mSummaryView.setEllipsize(null);
            } else {
                mSummaryView.setMaxLines(mCouiSummaryLineLimit);
                mSummaryView.setEllipsize(TextUtils.TruncateAt.END);
            }
        }
        bindRedDots(holder);
    }

    private void bindRedDots(PreferenceViewHolder holder) {
        boolean hasEndWidget = true;
        iconRedDot = holder.findViewById(R.id.img_red_dot);
        endRedDot = (COUIHintRedDot) holder.findViewById(R.id.jump_icon_red_dot);
        assignRedDot = (COUIHintRedDot) holder.findViewById(R.id.assignment_red_dot);
        assignmentIcon = (COUIRoundImageView) holder.findViewById(R.id.assignment_icon);
        if (iconRedDot instanceof COUIHintRedDot) {
            if (mIconRedDotMode != 0) {
                ((COUIHintRedDot) iconRedDot).setLaidOut();
                iconRedDot.setVisibility(View.VISIBLE);
                ((COUIHintRedDot) iconRedDot).setPointMode(mIconRedDotMode);
                iconRedDot.invalidate();
            } else {
                iconRedDot.setVisibility(View.GONE);
            }
        }
        if (assignmentIcon == null) {
            hasEndWidget = false;
        } else if (mAssignIconRes != null) {
            assignmentIcon.setImageDrawable(mAssignIconRes);
            assignmentIcon.setVisibility(View.VISIBLE);
        } else {
            assignmentIcon.setVisibility(View.GONE);
            hasEndWidget = false;
        }
        if (endRedDot != null) {
            if (mEndRedDotMode != 0) {
                endRedDot.setLaidOut();
                endRedDot.setVisibility(View.VISIBLE);
                endRedDot.setPointMode(mEndRedDotMode);
                endRedDot.setPointNumber(mEndRedDotNum);
                endRedDot.invalidate();
                hasEndWidget = true;
            } else {
                endRedDot.setVisibility(View.GONE);
            }
        }
        if (assignRedDot != null) {
            if (mAssignRedDotMode != 0) {
                assignRedDot.setLaidOut();
                assignRedDot.setVisibility(View.VISIBLE);
                assignRedDot.setPointMode(mAssignRedDotMode);
                assignRedDot.invalidate();
            } else {
                assignRedDot.setVisibility(View.GONE);
            }
        }
        if (assignRedDot != null) {
            ((ViewGroup) assignRedDot.getParent()).setVisibility(hasEndWidget || mAssignRedDotMode != 0 ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onDetached() {
        removePreciseClickListener();
        super.onDetached();
    }

    public void removePreciseClickListener() {
        if (mPreciseHelper != null) {
            mPreciseHelper.unSet();
            mPreciseHelper = null;
        }
    }

    public void setAssignIconRes(Drawable drawable) { if (mAssignIconRes != drawable) { mAssignIconRes = drawable; notifyChanged(); } }
    public void setAssignRedDotMode(int mode) { if (mAssignRedDotMode != mode) { mAssignRedDotMode = mode; notifyChanged(); } }
    public void setAssignment(CharSequence assignment) { if (!TextUtils.equals(mAssignment, assignment)) { mAssignment = assignment; notifyChanged(); } }
    public void setAssignmentColor(int color) { if (mAssignmentColor != color) { mAssignmentColor = color; notifyChanged(); } }
    public void setBackgroundAnimationEnabled(boolean enabled) { if (mIsBackgroundAnimationEnabled != enabled) { mIsBackgroundAnimationEnabled = enabled; notifyChanged(); } }
    public void setBorderRectRadius(int radius) { if (mRadius != radius) { mRadius = radius; notifyChanged(); } }
    public void setClickStyle(int style) { mClickStyle = style; }
    public void setCouiSummaryLine(int lineLimit) { if (mCouiSummaryLineLimit != lineLimit) { mCouiSummaryLineLimit = lineLimit; notifyChanged(); } }
    public void setEndRedDotMode(int mode) { if (mEndRedDotMode != mode) { mEndRedDotMode = mode; notifyChanged(); } }
    public void setEndRedDotNum(int number) { if (mEndRedDotNum != number) { mEndRedDotNum = number; notifyChanged(); } }
    public void setIconRedDotMode(int mode) { if (mIconRedDotMode != mode) { mIconRedDotMode = mode; notifyChanged(); } }
    public void setIconStyle(int style) { if (style == CIRCLE || style == ROUND) { mIconStyle = style; notifyChanged(); } }
    public void setIsCustomIconRadius(boolean custom) { mIsCustom = custom; }
    public void setIsEnableClickSpan(boolean enable) { mIsEnableClickSpan = enable; }
    @Override public void setIsSupportCardUse(boolean support) { mIsSupportCardUse = support; }
    public void setJump(Drawable drawable) { if (mJumpRes != drawable) { mJumpRes = drawable; notifyChanged(); } }
    public void setJump(int resId) { setJump(mContext.getResources().getDrawable(resId)); }
    public void setOnPreciseClickListener(PreciseClickHelper.OnPreciseClickListener listener) { mPreciseListener = listener; initPreciseHelper(); }
    public void setSelected(boolean selected) { if (mIsSelected != selected) { mIsSelected = selected; notifyChanged(); } }
    public void setSelectedState(boolean selected) { mIsSelected = selected; }
    public void setShowDivider(boolean show) { if (mShowDivider != show) { mShowDivider = show; notifyChanged(); } }
    public void setStatusText1(CharSequence statusText) { if (!TextUtils.equals(mStatusText1, statusText)) { mStatusText1 = statusText; notifyChanged(); } }
    public void setSummaryTextColor(ColorStateList color) { mSummaryTextColor = color; notifyChanged(); }
    public void setTitleColor(ColorStateList color) { if (mTitleTextColor != color) { mTitleTextColor = color; notifyChanged(); } }
    public void showAssignRedDot() { if (assignRedDot != null) { assignRedDot.executeScaleAnim(true); notifyChanged(); } }
    public void showEndRedDot() { if (endRedDot != null) { endRedDot.executeScaleAnim(true); notifyChanged(); } }
    public void showIconRedDot() { if (iconRedDot instanceof COUIHintRedDot) { ((COUIHintRedDot) iconRedDot).executeScaleAnim(true); notifyChanged(); } }
}
