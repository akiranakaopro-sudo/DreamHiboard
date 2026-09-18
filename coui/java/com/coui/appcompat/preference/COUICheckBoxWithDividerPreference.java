package com.coui.appcompat.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.preference.CheckBoxPreference;
import androidx.preference.PreferenceViewHolder;
import androidx.recyclerview.widget.COUIRecyclerView;

import com.coui.appcompat.R;
import com.coui.appcompat.cardlist.COUICardListHelper;
import com.coui.appcompat.cardlist.COUICardListSelectedItemLayout;
import com.coui.appcompat.checkbox.COUICheckBox;
import com.coui.appcompat.poplist.PreciseClickHelper;

public class COUICheckBoxWithDividerPreference extends CheckBoxPreference implements COUICardSupportInterface, COUIRecyclerView.ICOUIDividerDecorationInterface {
    private CharSequence mAssignment;
    private int mAssignmentColor;
    private LinearLayout mCheckBoxLayout;
    private OnMainLayoutClickListener mClickListener;
    private int mDividerDefaultHorizontalPadding;
    private boolean mIsSupportCardUse;
    private View mItemView;
    private LinearLayout mMainLayout;
    private PreciseClickHelper mPreciseHelper;
    private PreciseClickHelper.OnPreciseClickListener mPreciseListener;
    private TextView mTitleView;

    public interface OnMainLayoutClickListener {
        void onMainLayoutClick();
    }

    public COUICheckBoxWithDividerPreference(Context context) {
        this(context, null);
    }

    public COUICheckBoxWithDividerPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.couiCheckBoxWithDividerPreferenceStyle);
    }

    public COUICheckBoxWithDividerPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Preference_COUI_COUICheckBoxWithDividerPreference);
    }

    public COUICheckBoxWithDividerPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mAssignmentColor = 0;
        TypedArray checkBoxAttrs = context.obtainStyledAttributes(attrs, R.styleable.COUICheckBoxPreference, defStyleAttr, defStyleRes);
        mAssignment = checkBoxAttrs.getText(R.styleable.COUICheckBoxPreference_couiCheckBoxAssignment);
        checkBoxAttrs.recycle();
        TypedArray preferenceAttrs = context.obtainStyledAttributes(attrs, R.styleable.COUIPreference, defStyleAttr, defStyleRes);
        mIsSupportCardUse = preferenceAttrs.getBoolean(R.styleable.COUIPreference_isSupportCardUse, true);
        mAssignmentColor = preferenceAttrs.getInt(R.styleable.COUIPreference_couiAssignmentColor, 0);
        preferenceAttrs.recycle();
        mDividerDefaultHorizontalPadding = getContext().getResources()
                .getDimensionPixelSize(R.dimen.coui_preference_divider_default_horizontal_padding);
    }

    private void initPreciseHelper() {
        if (mMainLayout == null || mPreciseListener == null) {
            return;
        }
        removePreciseClickListener();
        mPreciseHelper = new PreciseClickHelper(mMainLayout, new PreciseClickHelper.OnPreciseClickListener() {
            @Override
            public void onClick(View view, int x, int y) {
                mPreciseListener.onClick(view, x, y);
                if (mMainLayout != null && mClickListener != null) {
                    mClickListener.onMainLayoutClick();
                }
            }
        });
        mPreciseHelper.setup();
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

    public OnMainLayoutClickListener getOnMainLayoutClickListener() {
        return mClickListener;
    }

    @Override
    public boolean isSupportCardUse() {
        return mIsSupportCardUse;
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        mMainLayout = (LinearLayout) holder.itemView.findViewById(R.id.main_layout);
        View itemView = holder.itemView;
        if (itemView instanceof COUICardListSelectedItemLayout && mMainLayout != null) {
            ((COUICardListSelectedItemLayout) itemView).setMainLayoutToSetExtraPadding(mMainLayout);
        }
        mItemView = holder.itemView;
        View checkbox = holder.findViewById(android.R.id.checkbox);
        View icon = holder.findViewById(android.R.id.icon);
        View iconLayout = holder.findViewById(R.id.img_layout);
        if (iconLayout != null) {
            iconLayout.setVisibility(icon != null ? icon.getVisibility() : View.GONE);
        }
        if (checkbox instanceof COUICheckBox) {
            ((COUICheckBox) checkbox).setState(isChecked() ? 2 : 0);
        }
        mTitleView = (TextView) holder.findViewById(android.R.id.title);
        initPreciseHelper();
        if (mMainLayout != null) {
            if (mPreciseListener == null) {
                mMainLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mClickListener != null) {
                            mClickListener.onMainLayoutClick();
                        }
                    }
                });
            }
            mMainLayout.setClickable(isSelectable());
        }
        // Leapy fixed 2026-08-27: Layout uses check_box_layout; checkbox_layout is a
        // different id (privacy checkbox). Wrong lookup left mCheckBoxLayout null so
        // Messages DND rows never toggled from the checkbox strip.
        mCheckBoxLayout = (LinearLayout) holder.itemView.findViewById(R.id.check_box_layout);
        if (mCheckBoxLayout != null) {
            mCheckBoxLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    COUICheckBoxWithDividerPreference.super.onClick();
                }
            });
            mCheckBoxLayout.setClickable(isSelectable());
        }
        COUIPreferenceUtils.bindAssignmentView(holder, mAssignment, mAssignmentColor);
        COUICardListHelper.setItemCardBackground(holder.itemView, COUICardListHelper.getPositionInGroup(this));
    }

    public void removePreciseClickListener() {
        if (mPreciseHelper != null) {
            mPreciseHelper.unSet();
            mPreciseHelper = null;
        }
        if (mMainLayout != null) {
            mMainLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mClickListener != null) {
                        mClickListener.onMainLayoutClick();
                    }
                }
            });
        }
    }

    public void setAssignment(CharSequence assignment) {
        if (!TextUtils.equals(mAssignment, assignment)) {
            mAssignment = assignment;
            notifyChanged();
        }
    }

    public void setAssignmentColor(int color) {
        if (mAssignmentColor != color) {
            mAssignmentColor = color;
            notifyChanged();
        }
    }

    @Override
    public void setIsSupportCardUse(boolean supportCardUse) {
        mIsSupportCardUse = supportCardUse;
    }

    public void setOnMainLayoutListener(OnMainLayoutClickListener listener) {
        mClickListener = listener;
    }

    public void setOnPreciseClickListener(PreciseClickHelper.OnPreciseClickListener listener) {
        mPreciseListener = listener;
        initPreciseHelper();
    }
}
