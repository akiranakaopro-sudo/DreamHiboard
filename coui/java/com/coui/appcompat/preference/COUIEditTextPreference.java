package com.coui.appcompat.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceViewHolder;
import androidx.recyclerview.widget.COUIRecyclerView;

import com.coui.appcompat.R;
import com.coui.appcompat.cardlist.COUICardListHelper;
import com.coui.appcompat.cardlist.COUICardListSelectedItemLayout;
import com.coui.appcompat.uiutil.AnimLevel;
import com.coui.appcompat.uiutil.UIUtil;

public class COUIEditTextPreference extends EditTextPreference implements COUICardSupportInterface, COUIRecyclerView.ICOUIDividerDecorationInterface {
    private CharSequence mAssignment;
    private boolean mBlurBackground;
    private AnimLevel mBlurMinAnimLevel;
    private final Context mContext;
    private int mDividerDefaultHorizontalPadding;
    private boolean mIsSupportCardUse;
    private View mItemView;
    private Drawable mJumpRes;
    private final Point mLastTouchPoint = new Point();
    private View mPreferenceView;
    private CharSequence mStatusText1;
    private boolean mSupportEmptyInput;
    private TextView mTitleView;

    public COUIEditTextPreference(Context context) {
        this(context, null);
    }

    public COUIEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init(context, attrs);
    }

    public COUIEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public COUIEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray preferenceAttrs = context.obtainStyledAttributes(
                attrs, R.styleable.COUIPreference, 0, R.style.Preference_COUI_COUIWithPopupIcon);
        mAssignment = preferenceAttrs.getText(R.styleable.COUIPreference_couiAssignment);
        mJumpRes = preferenceAttrs.getDrawable(R.styleable.COUIPreference_coui_jump_mark);
        mStatusText1 = preferenceAttrs.getText(R.styleable.COUIPreference_coui_jump_status1);
        mIsSupportCardUse = preferenceAttrs.getBoolean(R.styleable.COUIPreference_isSupportCardUse, true);
        mBlurBackground = preferenceAttrs.getBoolean(R.styleable.COUIPreference_couiDialogBlurBackground, false);
        mBlurMinAnimLevel = AnimLevel.valueOf(preferenceAttrs.getInt(
                R.styleable.COUIPreference_couiBlurAnimLevel,
                UIUtil.ANIM_LEVEL_SUPPORT_BLUR_MIN.getIntValue()));
        preferenceAttrs.recycle();

        TypedArray editTextAttrs = context.obtainStyledAttributes(
                attrs, R.styleable.couiEditTextPreference, 0, 0);
        mSupportEmptyInput = editTextAttrs.getBoolean(
                R.styleable.couiEditTextPreference_couiSupportEmptyInput, false);
        editTextAttrs.recycle();

        mDividerDefaultHorizontalPadding = getContext().getResources()
                .getDimensionPixelSize(R.dimen.coui_preference_divider_default_horizontal_padding);
    }

    @Override
    public boolean drawDivider() {
        if (!(mItemView instanceof COUICardListSelectedItemLayout)) {
            return false;
        }
        int positionInGroup = COUICardListHelper.getPositionInGroup(this);
        return positionInGroup == COUICardListHelper.HEAD || positionInGroup == COUICardListHelper.MIDDLE;
    }

    public CharSequence getAssignment() {
        return mAssignment;
    }

    public AnimLevel getBlurMinAnimLevel() {
        return mBlurMinAnimLevel;
    }

    public View getDividerEndAlignView() {
        return null;
    }

    public int getDividerEndInset() {
        return mDividerDefaultHorizontalPadding;
    }

    public View getDividerStartAlignView() {
        return mTitleView;
    }

    public int getDividerStartInset() {
        return mDividerDefaultHorizontalPadding;
    }

    public Drawable getJump() {
        return mJumpRes;
    }

    public Point getLastTouchPoint() {
        return mLastTouchPoint;
    }

    public View getPreferenceView() {
        return mPreferenceView;
    }

    public CharSequence getStatusText1() {
        return mStatusText1;
    }

    public boolean isBlurBackground() {
        return mBlurBackground;
    }

    @Override
    public boolean isSupportCardUse() {
        return mIsSupportCardUse;
    }

    public boolean isSupportEmptyInput() {
        return mSupportEmptyInput;
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        mItemView = holder.itemView;
        COUIPreferenceUtils.bindView(holder, mJumpRes, mStatusText1, getAssignment());
        COUICardListHelper.setItemCardBackground(holder.itemView, COUICardListHelper.getPositionInGroup(this));
        mTitleView = (TextView) holder.findViewById(android.R.id.title);
        mPreferenceView = holder.itemView;
        holder.itemView.setOnTouchListener((view, event) -> {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                mLastTouchPoint.set((int) event.getX(), (int) event.getY());
            }
            return false;
        });
    }

    public void setAssignment(CharSequence assignment) {
        if (!TextUtils.equals(mAssignment, assignment)) {
            mAssignment = assignment;
            notifyChanged();
        }
    }

    public void setBlurBackground(boolean blurBackground) {
        mBlurBackground = blurBackground;
    }

    public void setBlurMinAnimLevel(AnimLevel blurMinAnimLevel) {
        mBlurMinAnimLevel = blurMinAnimLevel;
    }

    @Override
    public void setIsSupportCardUse(boolean isSupportCardUse) {
        mIsSupportCardUse = isSupportCardUse;
    }

    public void setJump(Drawable drawable) {
        if (mJumpRes != drawable) {
            mJumpRes = drawable;
            notifyChanged();
        }
    }

    public void setJump(int resId) {
        setJump(mContext.getResources().getDrawable(resId));
    }

    public void setStatusText1(CharSequence statusText1) {
        if (!TextUtils.equals(mStatusText1, statusText1)) {
            mStatusText1 = statusText1;
            notifyChanged();
        }
    }
}
