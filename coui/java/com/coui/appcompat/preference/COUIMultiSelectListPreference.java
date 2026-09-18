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

import androidx.preference.MultiSelectListPreference;
import androidx.preference.PreferenceViewHolder;
import androidx.recyclerview.widget.COUIRecyclerView;

import com.coui.appcompat.R;
import com.coui.appcompat.cardlist.COUICardListHelper;
import com.coui.appcompat.cardlist.COUICardListSelectedItemLayout;
import com.coui.appcompat.uiutil.AnimLevel;
import com.coui.appcompat.uiutil.UIUtil;

public class COUIMultiSelectListPreference extends MultiSelectListPreference
        implements COUICardSupportInterface, COUIRecyclerView.ICOUIDividerDecorationInterface {
    private CharSequence mAssignment;
    private boolean mBlurBackground;
    private AnimLevel mBlurMinAnimLevel;
    Context mContext;
    private int mDividerDefaultHorizontalPadding;
    private boolean mIfFollowHand;
    private boolean mIsSupportCardUse;
    private View mItemView;
    Drawable mJumpRes;
    private Point mLastTouchPoint;
    private View mPreferenceView;
    CharSequence mStatusText1;
    private CharSequence[] mSummaries;
    private TextView mTitleView;

    public COUIMultiSelectListPreference(Context context) {
        super(context, null);
        mLastTouchPoint = new Point();
        mIfFollowHand = true;
        mBlurBackground = false;
    }

    public COUIMultiSelectListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mLastTouchPoint = new Point();
        mIfFollowHand = true;
        mBlurBackground = false;
        mContext = context;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.COUIPreference,
                0, R.style.Preference_COUI_COUIWithPopupIcon);
        mIsSupportCardUse = a.getBoolean(R.styleable.COUIPreference_isSupportCardUse, true);
        mAssignment = a.getText(R.styleable.COUIPreference_couiAssignment);
        mJumpRes = a.getDrawable(R.styleable.COUIPreference_coui_jump_mark);
        mStatusText1 = a.getText(R.styleable.COUIPreference_coui_jump_status1);
        mIfFollowHand = a.getBoolean(R.styleable.COUIPreference_couiIfFollowHand, true);
        mBlurBackground = a.getBoolean(R.styleable.COUIPreference_couiDialogBlurBackground, false);
        mBlurMinAnimLevel = AnimLevel.valueOf(a.getInt(R.styleable.COUIPreference_couiBlurAnimLevel,
                UIUtil.ANIM_LEVEL_SUPPORT_BLUR_MIN.getIntValue()));
        a.recycle();
        mDividerDefaultHorizontalPadding = getContext().getResources().getDimensionPixelSize(
                R.dimen.coui_preference_divider_default_horizontal_padding);
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

    public CharSequence[] getSummaries() {
        return mSummaries;
    }

    public boolean isBlurBackground() {
        return mBlurBackground;
    }

    public boolean isIfFollowHand() {
        return mIfFollowHand;
    }

    @Override
    public boolean isSupportCardUse() {
        return mIsSupportCardUse;
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        mItemView = holder.itemView;
        COUIPreferenceUtils.bindView(holder, mJumpRes, mStatusText1, getAssignment());
        COUICardListHelper.setItemCardBackground(holder.itemView, COUICardListHelper.getPositionInGroup(this));
        mTitleView = (TextView) holder.findViewById(android.R.id.title);
        mPreferenceView = holder.itemView;
        mPreferenceView.setOnTouchListener((view, event) -> {
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

    public void setIfFollowHand(boolean ifFollowHand) {
        mIfFollowHand = ifFollowHand;
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

    public void setStatusText1(CharSequence statusText) {
        if ((statusText != null || mStatusText1 == null)
                && (statusText == null || statusText.equals(mStatusText1))) {
            return;
        }
        mStatusText1 = statusText;
        notifyChanged();
    }

    public void setSummaries(CharSequence[] summaries) {
        mSummaries = summaries;
    }
}
