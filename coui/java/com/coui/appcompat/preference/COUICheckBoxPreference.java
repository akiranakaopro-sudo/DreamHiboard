package com.coui.appcompat.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.CheckBoxPreference;
import androidx.preference.PreferenceViewHolder;
import androidx.recyclerview.widget.COUIRecyclerView;

import com.coui.appcompat.R;
import com.coui.appcompat.cardlist.COUICardListHelper;
import com.coui.appcompat.cardlist.COUICardListSelectedItemLayout;
import com.coui.appcompat.checkbox.COUICheckBox;
import com.coui.appcompat.hapticfeedback.COUIHapticFeedbackConstants;

public final class COUICheckBoxPreference extends CheckBoxPreference implements COUIRecyclerView.ICOUIDividerDecorationInterface {

    private static final int CHECKBOX_STATE_UNSELECTED = 0;
    private static final int CHECKBOX_STATE_PART_SELECTED = 1;
    private static final int CHECKBOX_STATE_ALL_SELECTED = 2;

    @Nullable
    private CharSequence mAssignment;

    private int mAssignmentColor;
    private int mDividerDefaultHorizontalPadding;

    @Nullable
    private COUICheckBox mCheckBox;

    @Nullable
    private View mItemView;

    @Nullable
    private TextView mTitleView;

    private final View.AccessibilityDelegate mAccessibilityDelegate =
            new View.AccessibilityDelegate() {
                @Override
                public void onInitializeAccessibilityNodeInfo(
                        @NonNull View host,
                        @NonNull AccessibilityNodeInfo info
                ) {
                    super.onInitializeAccessibilityNodeInfo(host, info);
                    bindAccessibilityAction(info);
                }
            };

    public COUICheckBoxPreference(@NonNull Context context) {
        this(context, null);
    }

    public COUICheckBoxPreference(
            @NonNull Context context,
            @Nullable AttributeSet attrs
    ) {
        this(context, attrs, R.attr.couiCheckBoxPreferenceStyle);
    }

    public COUICheckBoxPreference(
            @NonNull Context context,
            @Nullable AttributeSet attrs,
            int defStyleAttr
    ) {
        this(context, attrs, defStyleAttr, R.style.Preference_COUI_COUICheckBoxPreference);
    }

    public COUICheckBoxPreference(
            @NonNull Context context,
            @Nullable AttributeSet attrs,
            int defStyleAttr,
            int defStyleRes
    ) {
        super(context, attrs, defStyleAttr, defStyleRes);

        readDimensions(context);
        readPreferenceAttrs(context, attrs, defStyleAttr, defStyleRes);
        readCheckBoxPreferenceAttrs(context, attrs, defStyleAttr, defStyleRes);
    }

    private void readDimensions(@NonNull Context context) {
        mDividerDefaultHorizontalPadding = context.getResources()
                .getDimensionPixelSize(R.dimen.coui_preference_divider_default_horizontal_padding);
    }

    private void readPreferenceAttrs(
            @NonNull Context context,
            @Nullable AttributeSet attrs,
            int defStyleAttr,
            int defStyleRes
    ) {
        try (TypedArray array = context.obtainStyledAttributes(
                attrs,
                R.styleable.COUIPreference,
                defStyleAttr,
                defStyleRes
        )) {
            mAssignmentColor = array.getColor(
                    R.styleable.COUIPreference_couiAssignmentColor,
                    0
            );
        }
    }

    private void readCheckBoxPreferenceAttrs(
            @NonNull Context context,
            @Nullable AttributeSet attrs,
            int defStyleAttr,
            int defStyleRes
    ) {
        try (TypedArray array = context.obtainStyledAttributes(
                attrs,
                R.styleable.COUICheckBoxPreference,
                defStyleAttr,
                defStyleRes
        )) {
            mAssignment = array.getText(
                    R.styleable.COUICheckBoxPreference_couiCheckBoxAssignment
            );
        }
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        mItemView = holder.itemView;

        bindItemView(holder);
        bindTitle(holder);
        bindCheckBox(holder);
        bindIconLayout(holder);
        bindAssignment(holder);
        bindCardBackground(holder);
    }

    private void bindItemView(@NonNull PreferenceViewHolder holder) {
        View itemView = holder.itemView;
        itemView.setAccessibilityDelegate(mAccessibilityDelegate);
        itemView.setOnTouchListener(COUICheckBoxPreference::onItemTouched);
    }

    private static boolean onItemTouched(@NonNull View view, @NonNull MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            view.performHapticFeedback(COUIHapticFeedbackConstants.GRANULAR_SHORT_VIBRATE);
        }
        return false;
    }

    private void bindTitle(@NonNull PreferenceViewHolder holder) {
        View titleView = holder.findViewById(android.R.id.title);
        mTitleView = titleView instanceof TextView ? (TextView) titleView : null;
    }

    private void bindCheckBox(@NonNull PreferenceViewHolder holder) {
        View checkBoxView = holder.findViewById(android.R.id.checkbox);
        mCheckBox = checkBoxView instanceof COUICheckBox ? (COUICheckBox) checkBoxView : null;

        if (mCheckBox != null) {
            mCheckBox.setState(isChecked()
                    ? CHECKBOX_STATE_ALL_SELECTED
                    : CHECKBOX_STATE_UNSELECTED);
        }
    }

    private void bindIconLayout(@NonNull PreferenceViewHolder holder) {
        View iconLayout = holder.findViewById(R.id.img_layout);
        if (iconLayout == null) {
            return;
        }

        View iconView = holder.findViewById(android.R.id.icon);
        iconLayout.setVisibility(iconView != null ? iconView.getVisibility() : View.GONE);
    }

    private void bindAssignment(@NonNull PreferenceViewHolder holder) {
        COUIPreferenceUtils.bindAssignmentView(holder, mAssignment, mAssignmentColor);
    }

    private void bindCardBackground(@NonNull PreferenceViewHolder holder) {
        int positionInGroup = COUICardListHelper.getPositionInGroup(this);
        COUICardListHelper.setItemCardBackground(holder.itemView, positionInGroup);
    }

    private void bindAccessibilityAction(@NonNull AccessibilityNodeInfo info) {
        Context context = getContext();

        CharSequence actionLabel = context.getString(R.string.coui_accessibility_switch);

        if (mCheckBox != null && mCheckBox.getState() == CHECKBOX_STATE_PART_SELECTED) {
            actionLabel = context.getString(R.string.coui_accessibility_select_all);
        }

        info.addAction(new AccessibilityNodeInfo.AccessibilityAction(
                AccessibilityNodeInfo.ACTION_CLICK,
                actionLabel
        ));
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
}
