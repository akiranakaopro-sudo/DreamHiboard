package com.coui.appcompat.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CompoundButton;

import androidx.preference.PreferenceViewHolder;
import androidx.preference.SwitchPreferenceCompat;
import androidx.recyclerview.widget.COUIRecyclerView;

import com.coui.appcompat.R;
import com.coui.appcompat.cardlist.COUICardListHelper;
import com.coui.appcompat.cardlist.COUICardListSelectedItemLayout;
import com.coui.appcompat.couiswitch.COUISwitch;

public class COUISwitchPreferenceCompat extends SwitchPreferenceCompat implements COUIRecyclerView.ICOUIDividerDecorationInterface {
    private CharSequence mAssignment;
    private int mAssignmentColor;
    private int mDividerDefaultHorizontalPadding;
    private boolean mIsEnableClickSpan;
    private View mItemView;
    private final Listener mListener;
    private COUISwitch mSwitchView;

    public COUISwitchPreferenceCompat(Context context) {
        this(context, null);
    }

    public COUISwitchPreferenceCompat(Context context, AttributeSet attrs) {
        this(context, attrs, androidx.preference.R.attr.switchPreferenceCompatStyle);
    }

    public COUISwitchPreferenceCompat(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public COUISwitchPreferenceCompat(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
        mListener = new Listener();
        mAssignmentColor = 0;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.COUIPreference, defStyleAttr, 0);
        mIsEnableClickSpan = typedArray.getBoolean(R.styleable.COUIPreference_couiEnalbeClickSpan, false);
        mAssignment = typedArray.getText(R.styleable.COUIPreference_couiAssignment);
        mAssignmentColor = typedArray.getInt(R.styleable.COUIPreference_couiAssignmentColor, 0);
        typedArray.recycle();
        mDividerDefaultHorizontalPadding = getContext().getResources()
                .getDimensionPixelSize(R.dimen.coui_preference_divider_default_horizontal_padding);
    }

    private boolean callCustomChangeListener(Object value) {
        return callChangeListener(value);
    }

    @Override
    public boolean drawDivider() {
        if (!(mItemView instanceof COUICardListSelectedItemLayout)) {
            return false;
        }
        int positionInGroup = COUICardListHelper.getPositionInGroup(this);
        return positionInGroup == 1 || positionInGroup == 2;
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
        return mItemView;
    }

    @Override
    public int getDividerStartInset() {
        return mDividerDefaultHorizontalPadding;
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        mItemView = holder.itemView;
        View preferenceView = holder.findViewById(R.id.coui_preference);
        if (preferenceView != null) {
            preferenceView.setSoundEffectsEnabled(false);
        }
        View switchWidget = holder.findViewById(R.id.switchWidget);
        boolean isCOUISwitch = switchWidget instanceof COUISwitch;
        if (isCOUISwitch) {
            COUISwitch couiSwitch = (COUISwitch) switchWidget;
            couiSwitch.setOnCheckedChangeListener(null);
            couiSwitch.setVerticalScrollBarEnabled(false);
            mSwitchView = couiSwitch;
        }
        super.onBindViewHolder(holder);
        if (isCOUISwitch) {
            ((COUISwitch) switchWidget).setOnCheckedChangeListener(mListener);
        }
        if (mIsEnableClickSpan) {
            COUIPreferenceUtils.setSummaryView(getContext(), holder);
        }
        COUIPreferenceUtils.bindAssignmentView(holder, mAssignment, mAssignmentColor);
        COUICardListHelper.setItemCardBackground(holder.itemView, COUICardListHelper.getPositionInGroup(this));
    }

    @Override
    public void onClick() {
        COUISwitch couiSwitch = mSwitchView;
        if (couiSwitch != null) {
            couiSwitch.setShouldPlaySound(true);
        }
        super.onClick();
    }

    public void setAssignment(CharSequence assignment) {
        if (TextUtils.equals(mAssignment, assignment)) {
            return;
        }
        mAssignment = assignment;
        notifyChanged();
    }

    public void setAssignmentColor(int color) {
        if (mAssignmentColor != color) {
            mAssignmentColor = color;
            notifyChanged();
        }
    }

    public void setIsEnableClickSpan(boolean enabled) {
        mIsEnableClickSpan = enabled;
    }

    public class Listener implements CompoundButton.OnCheckedChangeListener {
        private Listener() {
        }

        @Override
        public void onCheckedChanged(CompoundButton button, boolean checked) {
            if (COUISwitchPreferenceCompat.this.isChecked() == checked) {
                return;
            }
            if (COUISwitchPreferenceCompat.this.callCustomChangeListener(Boolean.valueOf(checked))) {
                COUISwitchPreferenceCompat.this.setChecked(checked);
            } else {
                button.setChecked(!checked);
            }
        }
    }
}
