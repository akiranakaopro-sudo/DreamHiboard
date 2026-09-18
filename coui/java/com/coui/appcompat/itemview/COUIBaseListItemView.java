package com.coui.appcompat.itemview;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.ICOUIBaseListItemView;

import com.coui.appcompat.R;
import com.coui.appcompat.imageview.COUIRoundImageView;
import com.coui.appcompat.preference.COUICustomListSelectedLinearLayout;
import com.coui.appcompat.reddot.COUIHintRedDot;

// Leapy modified 2026-07-22: Implement OPPO's exact RecyclerView item contract.
public class COUIBaseListItemView extends RelativeLayout implements ICOUIBaseListItemView {
    public static final int CIRCLE = 0;
    private static final int DEFAULT_RADIUS = 14;
    private static final int DELAY_TIME = 70;
    public static final int FORCE_CLICKABLE = 1;
    public static final int FORCE_UNCLICKABLE = 2;
    private static final int PER_HEIGHT = 6;
    public static final int ROUND = 1;

    private COUIRoundImageView mAssignIconView;
    private COUIHintRedDot mAssignRedDotView;
    private TextView mAssignView;
    private Context mContext;
    private COUIHintRedDot mEndRedDotView;
    private boolean mHasBorder;
    private COUIHintRedDot mIconRedDotView;
    private int mIconStyle;
    private COUIRoundImageView mIconView;
    private boolean mIsCustom;
    private boolean mItemEnabled;
    private int mRadius;
    private View mRootItemView;
    private TextView mSummaryView;
    private TextView mTitleView;
    private ViewGroup mWidgetFrame;

    public COUIBaseListItemView(Context context) {
        this(context, null);
    }

    public COUIBaseListItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public COUIBaseListItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public COUIBaseListItemView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mItemEnabled = true;
        mRadius = DEFAULT_RADIUS;
        mIconStyle = ROUND;
        mContext = context;

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.COUIBaseListItemView, defStyleAttr, defStyleRes);
        boolean assignInRightAsMainLayout = a.getBoolean(
                R.styleable.COUIBaseListItemView_assignInRightAsMainLayout, true);
        boolean iconMarginDependOnImageView = a.getBoolean(
                R.styleable.COUIBaseListItemView_iconMarginDependOnImageView, false);
        mItemEnabled = a.getBoolean(R.styleable.COUIBaseListItemView_itemEnabled, true);
        CharSequence title = a.getText(R.styleable.COUIBaseListItemView_title);
        CharSequence summary = a.getText(R.styleable.COUIBaseListItemView_summary);
        Drawable icon = a.getDrawable(R.styleable.COUIBaseListItemView_icon);
        CharSequence assignment = a.getText(R.styleable.COUIBaseListItemView_assignment);
        Drawable assignmentIcon = a.getDrawable(R.styleable.COUIBaseListItemView_assignmentIcon);
        int widgetLayout = a.getResourceId(R.styleable.COUIBaseListItemView_widgetLayout, 0);
        a.recycle();

        View inflated = View.inflate(context,
                assignInRightAsMainLayout ? R.layout.coui_preference_assignment_in_right : R.layout.coui_preference,
                this);
        mRootItemView = inflated.findViewById(R.id.coui_preference);
        View iconLayout = inflated.findViewById(R.id.img_layout);
        mIconView = (COUIRoundImageView) inflated.findViewById(android.R.id.icon);
        mIconRedDotView = (COUIHintRedDot) inflated.findViewById(R.id.img_red_dot);
        mTitleView = (TextView) inflated.findViewById(android.R.id.title);
        mSummaryView = (TextView) inflated.findViewById(android.R.id.summary);
        mEndRedDotView = (COUIHintRedDot) inflated.findViewById(R.id.jump_icon_red_dot);
        mAssignRedDotView = (COUIHintRedDot) inflated.findViewById(R.id.assignment_red_dot);
        mAssignIconView = (COUIRoundImageView) inflated.findViewById(R.id.assignment_icon);
        mAssignView = (TextView) inflated.findViewById(R.id.assignment);
        mWidgetFrame = (ViewGroup) inflated.findViewById(android.R.id.widget_frame);
        mRootItemView.setClickable(true);
        setIconMarginDependOnImageView(iconMarginDependOnImageView);
        iconLayout.setVisibility(VISIBLE);
        setTitle(title);
        setSummary(summary);
        setIcon(icon);
        setAssignment(assignment);
        setAssignIcon(assignmentIcon);
        setWidgetView(widgetLayout);
        setIconStyle(mRadius, mHasBorder, mIconStyle, mIsCustom);
        setEnableState(this, mItemEnabled);
    }

    private void setEnableState(View view, boolean enabled) {
        view.setEnabled(enabled);
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = viewGroup.getChildCount() - 1; i >= 0; i--) {
                setEnableState(viewGroup.getChildAt(i), enabled);
            }
        }
    }

    private void setIconMarginDependOnImageView(boolean dependOnImageView) {
        if (mRootItemView instanceof COUICustomListSelectedLinearLayout) {
            ((COUICustomListSelectedLinearLayout) mRootItemView)
                    .setIconMarginDependOnImageView(dependOnImageView);
        }
    }

    public ImageView getAssignIconView() {
        return mAssignIconView;
    }

    public ImageView getIconView() {
        return mIconView;
    }

    @Override
    public boolean getItemEnabled() {
        return mItemEnabled;
    }

    public final View getRootItemView() {
        return mRootItemView;
    }

    public final void setAssignIcon(Drawable drawable) {
        if (mAssignIconView != null) {
            if (drawable == null) {
                mAssignIconView.setVisibility(GONE);
            } else {
                mAssignIconView.setImageDrawable(drawable);
                mAssignIconView.setVisibility(VISIBLE);
            }
        }
    }

    public void setAssignRedDotMode(int mode) {
        if (mAssignRedDotView != null) {
            if (mode == 0) {
                mAssignRedDotView.setVisibility(GONE);
                return;
            }
            mAssignRedDotView.setLaidOut();
            mAssignRedDotView.setVisibility(VISIBLE);
            mAssignRedDotView.setPointMode(mode);
            mAssignRedDotView.invalidate();
        }
    }

    public final void setAssignment(CharSequence assignment) {
        if (TextUtils.isEmpty(assignment)) {
            mAssignView.setVisibility(GONE);
        } else {
            mAssignView.setText(assignment);
            mAssignView.setVisibility(VISIBLE);
        }
    }

    public void setAssignmentColor(int color) {
        if (color != 0) {
            mAssignView.setTextColor(color);
        }
    }

    public void setClickableStyle(int style) {
        if (style == FORCE_CLICKABLE) {
            mRootItemView.setClickable(false);
        } else if (style == FORCE_UNCLICKABLE) {
            mRootItemView.setClickable(true);
        }
    }

    public void setCustomIconRadius(boolean custom) {
        mIsCustom = custom;
        setIconStyle(mRadius, mHasBorder, mIconStyle, custom);
    }

    @Deprecated
    public final void setEnable(boolean enabled) {
        setEnableState(this, enabled);
    }

    public void setEndRedDotMode(int mode, int number) {
        if (mode == 0) {
            mEndRedDotView.setVisibility(GONE);
            return;
        }
        mEndRedDotView.setLaidOut();
        mEndRedDotView.setVisibility(VISIBLE);
        mEndRedDotView.setPointMode(mode);
        mEndRedDotView.setPointNumber(number);
        mEndRedDotView.invalidate();
    }

    public final void setIcon(Drawable drawable) {
        if (drawable == null) {
            mIconView.setVisibility(GONE);
        } else {
            mIconView.setImageDrawable(drawable);
            mIconView.setVisibility(VISIBLE);
        }
    }

    public void setIconBorderRadius(int radius) {
        mRadius = radius;
        setIconStyle(radius, mHasBorder, mIconStyle, mIsCustom);
    }

    public void setIconHasBorder(boolean hasBorder) {
        mHasBorder = hasBorder;
        setIconStyle(mRadius, hasBorder, mIconStyle, mIsCustom);
    }

    public void setIconRedDotMode(int mode) {
        if (mode == 0) {
            mIconRedDotView.setVisibility(GONE);
            return;
        }
        mIconRedDotView.setLaidOut();
        mIconRedDotView.setVisibility(VISIBLE);
        mIconRedDotView.setPointMode(mode);
        mIconRedDotView.invalidate();
    }

    public void setIconStyle(int style) {
        if (style == CIRCLE || style == ROUND) {
            mIconStyle = style;
            setIconStyle(mRadius, mHasBorder, style, mIsCustom);
        }
    }

    public final void setItemBackground(Drawable drawable) {
        mRootItemView.setBackground(drawable);
    }

    @Override
    public void setItemEnabled(boolean enabled) {
        if (mItemEnabled != enabled) {
            mItemEnabled = enabled;
            setEnableState(this, enabled);
        }
    }

    @Override
    public void setOnClickListener(OnClickListener listener) {
        mRootItemView.setOnClickListener(listener);
    }

    public void setPaddingEnd(int paddingEnd) {
        mRootItemView.setPaddingRelative(mRootItemView.getPaddingStart(), mRootItemView.getPaddingTop(),
                paddingEnd, mRootItemView.getPaddingBottom());
    }

    public void setPaddingStart(int paddingStart) {
        mRootItemView.setPaddingRelative(paddingStart, mRootItemView.getPaddingTop(),
                mRootItemView.getPaddingEnd(), mRootItemView.getPaddingBottom());
    }

    public void setPaddingStartAndEnd(int paddingStart, int paddingEnd) {
        mRootItemView.setPaddingRelative(paddingStart, mRootItemView.getPaddingTop(),
                paddingEnd, mRootItemView.getPaddingBottom());
    }

    public final void setSummary(CharSequence summary) {
        if (TextUtils.isEmpty(summary)) {
            mSummaryView.setVisibility(GONE);
        } else {
            mSummaryView.setText(summary);
            mSummaryView.setVisibility(VISIBLE);
        }
    }

    public void setSummaryClickSpan() {
        mSummaryView.setHighlightColor(getContext().getResources().getColor(android.R.color.transparent));
        mSummaryView.setMovementMethod(LinkMovementMethod.getInstance());
        mSummaryView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                int actionMasked = event.getActionMasked();
                int selectionStart = mSummaryView.getSelectionStart();
                int selectionEnd = mSummaryView.getSelectionEnd();
                int offsetForPosition = mSummaryView.getOffsetForPosition(event.getX(), event.getY());
                boolean outOfSelection = selectionStart == selectionEnd
                        || offsetForPosition <= selectionStart
                        || offsetForPosition >= selectionEnd;
                if (actionMasked != MotionEvent.ACTION_DOWN) {
                    if (actionMasked == MotionEvent.ACTION_UP || actionMasked == MotionEvent.ACTION_CANCEL) {
                        mSummaryView.setPressed(false);
                        mSummaryView.postInvalidateDelayed(DELAY_TIME);
                    }
                } else {
                    if (outOfSelection) {
                        return false;
                    }
                    mSummaryView.setPressed(true);
                    mSummaryView.invalidate();
                }
                return false;
            }
        });
    }

    public void setSummaryColor(ColorStateList colorStateList) {
        if (colorStateList != null) {
            mSummaryView.setTextColor(colorStateList);
        }
    }

    public final void setTitle(CharSequence title) {
        if (TextUtils.isEmpty(title)) {
            mTitleView.setVisibility(GONE);
        } else {
            mTitleView.setText(title);
            mTitleView.setVisibility(VISIBLE);
        }
    }

    public void setTitleColor(ColorStateList colorStateList) {
        if (colorStateList != null) {
            mTitleView.setTextColor(colorStateList);
        }
    }

    public final void setWidgetView(int layoutResId) {
        if (mWidgetFrame != null) {
            if (layoutResId == 0) {
                mWidgetFrame.setVisibility(GONE);
                return;
            }
            mWidgetFrame.setVisibility(VISIBLE);
            mWidgetFrame.removeAllViews();
            View.inflate(mContext, layoutResId, mWidgetFrame);
        }
    }

    public final void setIconStyle(int radius, boolean hasBorder, int iconStyle, boolean custom) {
        if (custom) {
            mIconView.setHasBorder(hasBorder);
            mIconView.setBorderRectRadius(0);
            mIconView.setType(iconStyle);
            return;
        }
        Drawable drawable = mIconView.getDrawable();
        if (drawable != null && radius == DEFAULT_RADIUS) {
            radius = drawable.getIntrinsicHeight() / PER_HEIGHT;
            Resources resources = getContext().getResources();
            int minRadius = R.dimen.coui_preference_icon_min_radius;
            if (radius < resources.getDimensionPixelOffset(minRadius)) {
                radius = getContext().getResources().getDimensionPixelOffset(minRadius);
            } else {
                int maxRadius = R.dimen.coui_preference_icon_max_radius;
                if (radius > resources.getDimensionPixelOffset(maxRadius)) {
                    radius = getContext().getResources().getDimensionPixelOffset(maxRadius);
                }
            }
        }
        mIconView.setHasBorder(hasBorder);
        mIconView.setBorderRectRadius(radius);
        mIconView.setType(iconStyle);
    }

    public void setWidgetView(View view) {
        if (mWidgetFrame != null) {
            if (view != null) {
                mWidgetFrame.setVisibility(VISIBLE);
                mWidgetFrame.removeAllViews();
                mWidgetFrame.addView(view);
                return;
            }
            mWidgetFrame.setVisibility(GONE);
        }
    }
}
