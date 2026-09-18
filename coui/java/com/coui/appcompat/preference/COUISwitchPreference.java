package com.coui.appcompat.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceViewHolder;
import androidx.preference.SwitchPreference;
import androidx.recyclerview.widget.COUIRecyclerView;

import com.coui.appcompat.R;
import com.coui.appcompat.cardlist.COUICardListHelper;
import com.coui.appcompat.cardlist.COUICardListSelectedItemLayout;
import com.coui.appcompat.couiswitch.COUISwitch;
import com.coui.appcompat.reddot.COUIHintRedDot;

public class COUISwitchPreference extends SwitchPreference
        implements COUICardSupportInterface, COUIRecyclerView.ICOUIDividerDecorationInterface {

    public static final int CIRCLE = 0;
    public static final int ROUND = 1;

    private static final int DEFAULT_RADIUS = 14;
    private static final int BOTTOM_RADIUS = 16;
    private static final int INVALID_COLOR = -1;

    private static final int CARD_POSITION_MIDDLE = 1;
    private static final int CARD_POSITION_BOTTOM = 2;
    private static final int CARD_POSITION_SINGLE = 3;

    private static final int RED_DOT_POINT_ONLY = 1;

    @Nullable
    private CharSequence mAssignment;

    private int mAssignmentColor;
    private int mDividerDefaultHorizontalPadding;
    private boolean mHasBorder;
    private boolean mHasRedDot;
    private int mIconStyle;
    private boolean mIsCustom;
    private boolean mIsEnableClickSpan;
    private boolean mIsSupportCardUse;

    @Nullable
    private View mItemView;

    private final CompoundButton.OnCheckedChangeListener mListener;

    private int mRadius;
    private int mSwitchBarCheckedColor;

    @Nullable
    private COUISwitch mSwitchView;

    @Nullable
    private CharSequence mTitle;

    @Nullable
    private TextView mTitleView;

    public COUISwitchPreference(@NonNull Context context) {
        this(context, null);
    }

    public COUISwitchPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, androidx.preference.R.attr.switchPreferenceStyle);
    }

    public COUISwitchPreference(
            @NonNull Context context,
            @Nullable AttributeSet attrs,
            int defStyleAttr
    ) {
        this(context, attrs, defStyleAttr, 0);
    }

    public COUISwitchPreference(
            @NonNull Context context,
            @Nullable AttributeSet attrs,
            int defStyleAttr,
            int defStyleRes
    ) {
        super(context, attrs, defStyleAttr, defStyleRes);

        mListener = new CheckedChangeListener();
        mAssignmentColor = 0;
        mIsCustom = false;
        mSwitchBarCheckedColor = INVALID_COLOR;

        readPreferenceAttrs(context, attrs, defStyleAttr, defStyleRes);
        readSwitchPreferenceAttrs(context, attrs, defStyleAttr, defStyleRes);
        readDimensions(context);

        mTitle = getTitle();
    }

    private void readPreferenceAttrs(
            @NonNull Context context,
            @Nullable AttributeSet attrs,
            int defStyleAttr,
            int defStyleRes
    ) {
        TypedArray array = context.obtainStyledAttributes(
                attrs,
                R.styleable.COUIPreference,
                defStyleAttr,
                defStyleRes
        );

        try {
            mIsEnableClickSpan = array.getBoolean(
                    R.styleable.COUIPreference_couiEnalbeClickSpan,
                    false
            );

            mAssignment = array.getText(R.styleable.COUIPreference_couiAssignment);

            mAssignmentColor = array.getColor(
                    R.styleable.COUIPreference_couiAssignmentColor,
                    0
            );

            mIsSupportCardUse = array.getBoolean(
                    R.styleable.COUIPreference_isSupportCardUse,
                    true
            );

            mIconStyle = array.getInt(
                    R.styleable.COUIPreference_couiIconStyle,
                    ROUND
            );

            mHasBorder = array.getBoolean(
                    R.styleable.COUIPreference_hasBorder,
                    false
            );

            mRadius = array.getDimensionPixelSize(
                    R.styleable.COUIPreference_preference_icon_radius,
                    DEFAULT_RADIUS
            );
        } finally {
            array.recycle();
        }
    }

    private void readSwitchPreferenceAttrs(
            @NonNull Context context,
            @Nullable AttributeSet attrs,
            int defStyleAttr,
            int defStyleRes
    ) {
        TypedArray array = context.obtainStyledAttributes(
                attrs,
                R.styleable.COUISwitchPreference,
                defStyleAttr,
                defStyleRes
        );

        try {
            mHasRedDot = array.getBoolean(
                    R.styleable.COUISwitchPreference_hasTitleRedDot,
                    false
            );
        } finally {
            array.recycle();
        }
    }

    private void readDimensions(@NonNull Context context) {
        mDividerDefaultHorizontalPadding = context.getResources()
                .getDimensionPixelSize(R.dimen.coui_preference_divider_default_horizontal_padding);
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        mItemView = holder.itemView;

        bindPreferenceContainer(holder);
        bindSwitchBeforeSuper(holder);

        super.onBindViewHolder(holder);

        bindSummary(holder);
        bindIcon(holder);
        bindTitle(holder);
        bindRedDot(holder);
        bindAssignment(holder);
        bindCardBackground(holder);
    }

    private void bindPreferenceContainer(@NonNull PreferenceViewHolder holder) {
        View preferenceView = holder.findViewById(R.id.coui_preference);
        if (preferenceView == null) {
            return;
        }

        preferenceView.setSoundEffectsEnabled(false);
        preferenceView.setHapticFeedbackEnabled(false);
    }

    private void bindSwitchBeforeSuper(@NonNull PreferenceViewHolder holder) {
        View switchWidget = holder.findViewById(android.R.id.switch_widget);

        if (!(switchWidget instanceof COUISwitch)) {
            mSwitchView = null;
            return;
        }

        COUISwitch switchView = (COUISwitch) switchWidget;
        mSwitchView = switchView;

        switchView.setOnCheckedChangeListener(mListener);
        switchView.setVerticalScrollBarEnabled(false);

        if (mSwitchBarCheckedColor != INVALID_COLOR) {
            switchView.setBarCheckedColor(mSwitchBarCheckedColor);
        }
    }

    private void bindSummary(@NonNull PreferenceViewHolder holder) {
        if (mIsEnableClickSpan) {
            COUIPreferenceUtils.setSummaryView(getContext(), holder);
        }
    }

    private void bindIcon(@NonNull PreferenceViewHolder holder) {
        COUIPreferenceUtils.setIconStyle(
                holder,
                getContext(),
                mRadius,
                mHasBorder,
                mIconStyle,
                mIsCustom
        );

        View imageLayout = holder.findViewById(R.id.img_layout);
        if (imageLayout == null) {
            return;
        }

        View iconView = holder.itemView.findViewById(android.R.id.icon);
        imageLayout.setVisibility(iconView != null ? iconView.getVisibility() : View.GONE);
    }

    private void bindTitle(@NonNull PreferenceViewHolder holder) {
        TextView titleView = (TextView) holder.findViewById(android.R.id.title);
        mTitleView = titleView;

        if (titleView != null) {
            titleView.setText(mTitle);
        }
    }

    private void bindRedDot(@NonNull PreferenceViewHolder holder) {
        View redDotView = holder.findViewById(R.id.jump_icon_red_dot);

        if (!(redDotView instanceof COUIHintRedDot)) {
            if (redDotView != null) {
                redDotView.setVisibility(View.GONE);
            }
            return;
        }

        COUIHintRedDot redDot = (COUIHintRedDot) redDotView;

        if (mHasRedDot) {
            redDot.setLaidOut();
            redDot.setVisibility(View.VISIBLE);
            redDot.setPointMode(RED_DOT_POINT_ONLY);
        } else {
            redDot.setVisibility(View.GONE);
        }

        redDot.invalidate();
    }

    private void bindAssignment(@NonNull PreferenceViewHolder holder) {
        COUIPreferenceUtils.bindAssignmentView(holder, mAssignment, mAssignmentColor);
    }

    private void bindCardBackground(@NonNull PreferenceViewHolder holder) {
        int positionInGroup = COUICardListHelper.getPositionInGroup(this);
        COUICardListHelper.setItemCardBackground(holder.itemView, positionInGroup);
    }

    @Override
    public boolean drawDivider() {
        if (!(mItemView instanceof COUICardListSelectedItemLayout)) {
            return false;
        }

        int positionInGroup = COUICardListHelper.getPositionInGroup(this);
        return positionInGroup == CARD_POSITION_MIDDLE
                || positionInGroup == CARD_POSITION_BOTTOM;
    }

    @Override
    public boolean isSupportCardUse() {
        return mIsSupportCardUse;
    }

    @Override
    public void setIsSupportCardUse(boolean supportCardUse) {
        if (mIsSupportCardUse == supportCardUse) {
            return;
        }

        mIsSupportCardUse = supportCardUse;
        notifyChanged();
    }

    @Nullable
    @Override
    public View getDividerStartAlignView() {
        return mTitleView;
    }

    @Override
    public int getDividerStartInset() {
        return mDividerDefaultHorizontalPadding;
    }

    @Nullable
    @Override
    public View getDividerEndAlignView() {
        return null;
    }

    @Override
    public int getDividerEndInset() {
        return mDividerDefaultHorizontalPadding;
    }

    public int getBorderRectRadius(int position) {
        return position == CARD_POSITION_SINGLE ? BOTTOM_RADIUS : DEFAULT_RADIUS;
    }

    @Nullable
    public CharSequence getAssignment() {
        return mAssignment;
    }

    public void setAssignment(@Nullable CharSequence assignment) {
        if (TextUtils.equals(mAssignment, assignment)) {
            return;
        }

        mAssignment = assignment;
        notifyChanged();
    }

    public void setAssignmentColor(int color) {
        if (mAssignmentColor == color) {
            return;
        }

        mAssignmentColor = color;
        notifyChanged();
    }

    public void setBorderRectRadius(int radius) {
        if (mRadius == radius) {
            return;
        }

        mRadius = radius;
        notifyChanged();
    }

    public void setHasRedDot(boolean hasRedDot) {
        if (mHasRedDot == hasRedDot) {
            return;
        }

        mHasRedDot = hasRedDot;
        notifyChanged();
    }

    public void setIsCustomIconRadius(boolean custom) {
        if (mIsCustom == custom) {
            return;
        }

        mIsCustom = custom;
        notifyChanged();
    }

    public void setIsEnableClickSpan(boolean enabled) {
        if (mIsEnableClickSpan == enabled) {
            return;
        }

        mIsEnableClickSpan = enabled;
        notifyChanged();
    }

    public final void setSwitchBarCheckedColor(int color) {
        if (mSwitchBarCheckedColor == color) {
            return;
        }

        mSwitchBarCheckedColor = color;

        if (mSwitchView != null) {
            mSwitchView.setBarCheckedColor(color);
        }
    }

    @Override
    public void setTitle(@Nullable CharSequence title) {
        super.setTitle(title);
        mTitle = getTitle();

        if (mTitleView != null) {
            mTitleView.setText(mTitle);
        }
    }

    @Override
    public void onClick() {
        setPlaySound(true);
        setPerformFeedBack(true);
        super.onClick();
    }

    public void setPlaySound(boolean enabled) {
        if (mSwitchView != null) {
            mSwitchView.setShouldPlaySound(enabled);
        }
    }

    public void setPerformFeedBack(boolean enabled) {
        if (mSwitchView != null) {
            mSwitchView.setTactileFeedbackEnabled(enabled);
        }
    }

    public void refresh() {
        if (mSwitchView != null) {
            mSwitchView.refresh();
        }
    }

    private final class CheckedChangeListener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(@NonNull CompoundButton buttonView, boolean checked) {
            if (isChecked() == checked) {
                return;
            }

            if (callChangeListener(Boolean.valueOf(checked))) {
                setChecked(checked);
            } else {
                buttonView.setChecked(!checked);
            }
        }
    }
}
