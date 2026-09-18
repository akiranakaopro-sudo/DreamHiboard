package com.coui.appcompat.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Checkable;
import android.widget.TextView;

import androidx.preference.CheckBoxPreference;
import androidx.preference.PreferenceViewHolder;
import androidx.recyclerview.widget.COUIRecyclerView;

import com.coui.appcompat.R;
import com.coui.appcompat.cardlist.COUICardListHelper;
import com.coui.appcompat.cardlist.COUICardListSelectedItemLayout;

public class COUIMarkPreference extends CheckBoxPreference implements COUICardSupportInterface,
        COUIRecyclerView.ICOUIDividerDecorationInterface {
    public static final int CIRCLE = 0;
    static final int DEFAULT_RADIUS = 14;
    public static final int HEAD_MARK = 1;
    public static final int ROUND = 1;
    public static final int TAIL_MARK = 0;

    private CharSequence mAssignment;
    private int mAssignmentColor;
    private int mDividerDefaultHorizontalPadding;
    private boolean mHasBorder;
    private int mIconStyle;
    private boolean mIsCustom;
    private boolean mIsEnableClickSpan;
    private boolean mIsSupportCardUse;
    private View mItemView;
    int mMarkStyle;
    private int mRadius;
    private boolean mShowDivider;
    private TextView mTitleView;

    public COUIMarkPreference(Context context) {
        this(context, null);
    }

    public COUIMarkPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.couiMarkPreferenceStyle);
    }

    public COUIMarkPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Preference_COUI_COUIMarkPreference);
    }

    public COUIMarkPreference(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mMarkStyle = TAIL_MARK;
        mShowDivider = true;
        mAssignmentColor = 0;
        mIsCustom = false;
        TypedArray markAttrs = context.obtainStyledAttributes(attrs, R.styleable.COUIMarkPreference,
                defStyleAttr, defStyleRes);
        mMarkStyle = markAttrs.getInt(R.styleable.COUIMarkPreference_couiMarkStyle, TAIL_MARK);
        mAssignment = markAttrs.getText(R.styleable.COUIMarkPreference_couiMarkAssignment);
        markAttrs.recycle();

        TypedArray preferenceAttrs = context.obtainStyledAttributes(attrs, R.styleable.COUIPreference,
                defStyleAttr, defStyleRes);
        mShowDivider = preferenceAttrs.getBoolean(R.styleable.COUIPreference_couiShowDivider,
                mShowDivider);
        mIsEnableClickSpan = preferenceAttrs.getBoolean(
                R.styleable.COUIPreference_couiEnalbeClickSpan, false);
        mIsSupportCardUse = preferenceAttrs.getBoolean(
                R.styleable.COUIPreference_isSupportCardUse, true);
        mIconStyle = preferenceAttrs.getInt(R.styleable.COUIPreference_couiIconStyle, ROUND);
        mHasBorder = preferenceAttrs.getBoolean(R.styleable.COUIPreference_hasBorder, false);
        mRadius = preferenceAttrs.getDimensionPixelSize(
                R.styleable.COUIPreference_preference_icon_radius, DEFAULT_RADIUS);
        mAssignmentColor = preferenceAttrs.getInt(
                R.styleable.COUIPreference_couiAssignmentColor, 0);
        preferenceAttrs.recycle();

        mDividerDefaultHorizontalPadding = getContext().getResources().getDimensionPixelSize(
                R.dimen.coui_preference_divider_default_horizontal_padding);
        setChecked(true);
    }

    @Override
    public boolean drawDivider() {
        if (!(mItemView instanceof COUICardListSelectedItemLayout)) {
            return false;
        }
        int positionInGroup = COUICardListHelper.getPositionInGroup(this);
        return positionInGroup == COUICardListHelper.HEAD
                || positionInGroup == COUICardListHelper.MIDDLE;
    }

    public CharSequence getAssignment() {
        return mAssignment;
    }

    public int getBorderRectRadius(int position) {
        return position == 3 ? 16 : 14;
    }

    @Override
    public View getDividerEndAlignView() {
        return null;
    }

    @Override
    public int getDividerEndInset() {
        return mDividerDefaultHorizontalPadding;
    }

    @Override
    public View getDividerStartAlignView() {
        return mTitleView;
    }

    @Override
    public int getDividerStartInset() {
        return mDividerDefaultHorizontalPadding;
    }

    public int getMarkStyle() {
        return mMarkStyle;
    }

    @Override
    public boolean isSupportCardUse() {
        return mIsSupportCardUse;
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        mItemView = holder.itemView;
        final View checkableView = holder.findViewById(R.id.coui_tail_mark);
        mItemView.setAccessibilityDelegate(new View.AccessibilityDelegate() {
            @Override
            public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
                super.onInitializeAccessibilityNodeInfo(host, info);
                info.addAction(new AccessibilityNodeInfo.AccessibilityAction(
                        AccessibilityNodeInfo.ACTION_CLICK,
                        COUIMarkPreference.this.getContext().getResources()
                                .getString(R.string.coui_accessibility_select)));
                if (checkableView instanceof Checkable
                        && ((Checkable) checkableView).isChecked()) {
                    info.setClickable(false);
                    info.removeAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_CLICK);
                }
            }
        });

        if (checkableView instanceof Checkable) {
            if (mMarkStyle == TAIL_MARK) {
                checkableView.setVisibility(View.VISIBLE);
                ((Checkable) checkableView).setChecked(isChecked());
            } else {
                checkableView.setVisibility(View.GONE);
            }
        }

        View headMark = holder.findViewById(R.id.coui_head_mark);
        if (headMark instanceof Checkable) {
            if (mMarkStyle == HEAD_MARK) {
                headMark.setVisibility(View.VISIBLE);
                ((Checkable) headMark).setChecked(isChecked());
            } else {
                headMark.setVisibility(View.GONE);
            }
        }

        COUIPreferenceUtils.setIconStyle(holder, getContext(), mRadius, mHasBorder, mIconStyle,
                mIsCustom);
        mTitleView = (TextView) holder.findViewById(android.R.id.title);
        View imgLayout = holder.findViewById(R.id.img_layout);
        View icon = holder.findViewById(android.R.id.icon);
        if (imgLayout != null) {
            if (icon != null) {
                imgLayout.setVisibility(icon.getVisibility());
            } else {
                imgLayout.setVisibility(View.GONE);
            }
        }
        if (mIsEnableClickSpan) {
            COUIPreferenceUtils.setSummaryView(getContext(), holder);
        }
        COUIPreferenceUtils.bindAssignmentView(holder, mAssignment, mAssignmentColor);
        COUICardListHelper.setItemCardBackground(holder.itemView,
                COUICardListHelper.getPositionInGroup(this));
    }

    public void setAssignment(CharSequence assignment) {
        if (!TextUtils.equals(mAssignment, assignment)) {
            mAssignment = assignment;
            notifyChanged();
        }
    }

    public void setAssignmentColor(int assignmentColor) {
        if (mAssignmentColor != assignmentColor) {
            mAssignmentColor = assignmentColor;
            notifyChanged();
        }
    }

    public void setBorderRectRadius(int radius) {
        if (mRadius != radius) {
            mRadius = radius;
            notifyChanged();
        }
    }

    public void setIsCustomIconRadius(boolean isCustom) {
        mIsCustom = isCustom;
    }

    public void setIsEnableClickSpan(boolean enableClickSpan) {
        mIsEnableClickSpan = enableClickSpan;
    }

    @Override
    public void setIsSupportCardUse(boolean supportCardUse) {
        mIsSupportCardUse = supportCardUse;
    }

    public void setMarkStyle(int markStyle) {
        mMarkStyle = markStyle;
    }
}
