package com.coui.appcompat.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.preference.PreferenceViewHolder;
import androidx.preference.SwitchPreferenceCompat;
import androidx.recyclerview.widget.COUIRecyclerView;

import com.coui.appcompat.R;
import com.coui.appcompat.cardlist.COUICardListHelper;
import com.coui.appcompat.cardlist.COUICardListSelectedItemLayout;
import com.coui.appcompat.couiswitch.COUISwitch;

public class COUISwitchLoadingPreference extends SwitchPreferenceCompat implements COUICardSupportInterface, COUIRecyclerView.ICOUIDividerDecorationInterface {
    private CharSequence mAssignment;
    private int mAssignmentColor;
    View mCheckableView;
    private int mDividerDefaultHorizontalPadding;
    private boolean mIsEnableClickSpan;
    private boolean mIsSupportCardUse;
    private View mItemView;
    private final Listener mListener;
    private COUISwitch.OnLoadingStateChangedListener mOnLoadingStateChangedListener;
    private COUISwitch mSwitchView;
    private TextView mTitleView;

    public COUISwitchLoadingPreference(Context context) {
        this(context, null);
    }

    public COUISwitchLoadingPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.couiSwitchLoadPreferenceStyle);
    }

    public COUISwitchLoadingPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Preference_COUI_SwitchPreference_Loading);
    }

    public COUISwitchLoadingPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mListener = new Listener();
        mAssignmentColor = 0;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.COUIPreference, defStyleAttr, 0);
        mIsEnableClickSpan = typedArray.getBoolean(R.styleable.COUIPreference_couiEnalbeClickSpan, false);
        mIsSupportCardUse = typedArray.getBoolean(R.styleable.COUIPreference_isSupportCardUse, true);
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
        return mTitleView;
    }

    @Override
    public int getDividerStartInset() {
        return mDividerDefaultHorizontalPadding;
    }

    public View getSwitch() {
        return mCheckableView;
    }

    @Override
    public boolean isSupportCardUse() {
        return mIsSupportCardUse;
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        mItemView = holder.itemView;
        View preferenceView = holder.findViewById(R.id.coui_preference);
        if (preferenceView != null) {
            preferenceView.setSoundEffectsEnabled(false);
            preferenceView.setHapticFeedbackEnabled(false);
        }
        View switchWidget = holder.findViewById(R.id.switchWidget);
        mCheckableView = switchWidget;
        if (switchWidget instanceof COUISwitch) {
            COUISwitch couiSwitch = (COUISwitch) switchWidget;
            couiSwitch.setOnCheckedChangeListener(null);
            couiSwitch.setVerticalScrollBarEnabled(false);
            mSwitchView = couiSwitch;
        }
        super.onBindViewHolder(holder);
        View checkableView = mCheckableView;
        if (checkableView instanceof COUISwitch) {
            COUISwitch couiSwitch = (COUISwitch) checkableView;
            couiSwitch.setLoadingStyle(true);
            couiSwitch.setOnLoadingStateChangedListener(mOnLoadingStateChangedListener);
            couiSwitch.setOnCheckedChangeListener(mListener);
        }
        if (mIsEnableClickSpan) {
            COUIPreferenceUtils.setSummaryView(getContext(), holder);
        }
        mTitleView = (TextView) holder.findViewById(android.R.id.title);
        View iconView = holder.itemView.findViewById(android.R.id.icon);
        View imageLayout = holder.findViewById(R.id.img_layout);
        if (imageLayout != null) {
            if (iconView != null) {
                imageLayout.setVisibility(iconView.getVisibility());
            } else {
                imageLayout.setVisibility(View.GONE);
            }
        }
        COUIPreferenceUtils.bindAssignmentView(holder, mAssignment, mAssignmentColor);
        COUICardListHelper.setItemCardBackground(holder.itemView, COUICardListHelper.getPositionInGroup(this));
    }

    @Override
    public void onClick() {
        COUISwitch couiSwitch = mSwitchView;
        if (couiSwitch != null) {
            couiSwitch.setShouldPlaySound(true);
            mSwitchView.setTactileFeedbackEnabled(true);
            mSwitchView.startLoading();
        }
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

    @Override
    public void setIsSupportCardUse(boolean supportCardUse) {
        mIsSupportCardUse = supportCardUse;
    }

    public void setOnLoadingStateChangedListener(COUISwitch.OnLoadingStateChangedListener listener) {
        mOnLoadingStateChangedListener = listener;
        View view = mCheckableView;
        if (view instanceof COUISwitch) {
            ((COUISwitch) view).setOnLoadingStateChangedListener(listener);
        }
    }

    public void startLoading() {
        View view = mCheckableView;
        if (view == null || !(view instanceof COUISwitch)) {
            return;
        }
        ((COUISwitch) view).startLoading();
    }

    public void stopLoading() {
        View view = mCheckableView;
        if (view == null || !(view instanceof COUISwitch)) {
            return;
        }
        ((COUISwitch) view).stopLoading();
    }

    public class Listener implements CompoundButton.OnCheckedChangeListener {
        private Listener() {
        }

        @Override
        public void onCheckedChanged(CompoundButton button, boolean checked) {
            if (COUISwitchLoadingPreference.this.callCustomChangeListener(Boolean.valueOf(checked))) {
                COUISwitchLoadingPreference.this.setChecked(checked);
            } else {
                button.setChecked(!checked);
            }
        }
    }
}
