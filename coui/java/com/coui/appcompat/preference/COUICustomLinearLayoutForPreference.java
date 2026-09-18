package com.coui.appcompat.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.coui.appcompat.R;

public class COUICustomLinearLayoutForPreference extends LinearLayout {
    private String TAG;
    private boolean mAHavePriority;
    private boolean mBStickToC;
    private int mDefaultMarginEnd;
    private int mMessageLayoutMarginEndInRight;
    private int mReddotMarginEndInRightHasAssignment;
    private int mReddotMarginEndInRightNoAssignment;
    private boolean mSetMessageLayoutMarginEnd;
    private int mViewLowPriorityMinWidth;
    private View viewA;
    private View viewB;
    private View viewC;

    public COUICustomLinearLayoutForPreference(Context context) {
        this(context, null);
    }

    public COUICustomLinearLayoutForPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public COUICustomLinearLayoutForPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TAG = "COUICustomLinearLayout";
        mBStickToC = true;
        mAHavePriority = true;
        mSetMessageLayoutMarginEnd = true;
        init(context, attrs, defStyleAttr);
    }

    private void customMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int maxHeight;
        int realWidthWithMargin;
        int marginWidth;
        int widthC;
        int widthA;
        int childWidthSpec = View.MeasureSpec.makeMeasureSpec(
                (View.MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft()) - getPaddingRight(),
                View.MeasureSpec.getMode(widthMeasureSpec)
        );
        int childHeightSpec = View.MeasureSpec.makeMeasureSpec(
                (View.MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop()) - getPaddingBottom(),
                View.MeasureSpec.getMode(heightMeasureSpec)
        );
        if (viewA.getVisibility() != GONE) {
            measureChildWithMargins(viewA, childWidthSpec, 0, childHeightSpec, 0);
            maxHeight = Math.max(viewA.getMeasuredHeight(), 0);
        } else {
            measureChild(viewA, View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.EXACTLY));
            maxHeight = 0;
        }
        if (viewB.getVisibility() != GONE) {
            measureChildWithMargins(viewB, childWidthSpec, 0, childHeightSpec, 0);
            maxHeight = Math.max(viewB.getMeasuredHeight(), maxHeight);
        } else {
            measureChild(viewB, View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.EXACTLY));
        }
        if (viewC.getVisibility() != GONE) {
            measureChildWithMargins(viewC, childWidthSpec, 0, childHeightSpec, 0);
            maxHeight = Math.max(viewC.getMeasuredHeight(), maxHeight);
        } else {
            measureChild(viewC, View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.EXACTLY));
        }
        int paddingLeft = getPaddingLeft();
        int measuredWidth = getMeasuredWidth() - getPaddingRight();
        int availableWidth = measuredWidth - paddingLeft;
        if (getRealWidthWithMargin(viewA) + getRealWidthWithMargin(viewB) + getRealWidthWithMargin(viewC) > availableWidth) {
            if (mAHavePriority) {
                realWidthWithMargin = getRealWidthWithMargin(viewC);
                marginWidth = getMarginWidth(viewC);
            } else {
                realWidthWithMargin = getRealWidthWithMargin(viewA);
                marginWidth = getMarginWidth(viewA);
            }
            int lowPriorityContentWidth = realWidthWithMargin - marginWidth;
            if (lowPriorityContentWidth >= mViewLowPriorityMinWidth) {
                lowPriorityContentWidth = mViewLowPriorityMinWidth;
            }
            if (mAHavePriority) {
                widthA = Math.min(
                        getRealWidthWithMargin(viewA),
                        (availableWidth - (lowPriorityContentWidth + getMarginWidth(viewC))) - getRealWidthWithMargin(viewB)
                );
                widthC = measuredWidth - Math.max(
                        measuredWidth - getRealWidthWithMargin(viewC),
                        (paddingLeft + widthA) + getRealWidthWithMargin(viewB)
                );
            } else {
                widthC = Math.min(
                        getRealWidthWithMargin(viewC),
                        (availableWidth - (lowPriorityContentWidth + getMarginWidth(viewA))) - getRealWidthWithMargin(viewB)
                );
                widthA = Math.min(getRealWidthWithMargin(viewA), (availableWidth - widthC) - getRealWidthWithMargin(viewB));
            }
            int widthB = getRealWidthWithMargin(viewB);
            if (viewA.getVisibility() != GONE) {
                View view = viewA;
                setChildSize(view, widthA - getMarginWidth(view));
                maxHeight = Math.max(viewA.getMeasuredHeight(), maxHeight);
            }
            if (viewB.getVisibility() != GONE) {
                View view = viewB;
                setChildSize(view, widthB - getMarginWidth(view));
                maxHeight = Math.max(viewB.getMeasuredHeight(), maxHeight);
            }
            if (viewC.getVisibility() != GONE) {
                View view = viewC;
                setChildSize(view, widthC - getMarginWidth(view));
                maxHeight = Math.max(viewC.getMeasuredHeight(), maxHeight);
            }
            // Leapy fixed 2026-08-25: Decoded OPPO passes makeMeasureSpec(...) as the
            // height to setMeasuredDimension. That stores a MeasureSpec (EXACTLY/
            // AT_MOST bits) as a pixel height, so Screen timeout rows inflate to a
            // huge empty gap with no visible card when the title+assignment path
            // overflows. Use the resolved pixel height instead.
            setMeasuredDimension(
                    View.MeasureSpec.getSize(widthMeasureSpec),
                    maxHeight + getPaddingTop() + getPaddingBottom());
            // Leapy end
        }
    }

    private int getMarginHeight(View view) {
        if (view.getVisibility() != GONE) {
            return ((ViewGroup.MarginLayoutParams) view.getLayoutParams()).topMargin
                    + ((ViewGroup.MarginLayoutParams) view.getLayoutParams()).bottomMargin;
        }
        return 0;
    }

    private int getMarginLeft(View view) {
        if (view.getVisibility() != GONE) {
            return ((ViewGroup.MarginLayoutParams) view.getLayoutParams()).leftMargin;
        }
        return 0;
    }

    private int getMarginTop(View view) {
        if (view.getVisibility() != GONE) {
            return ((ViewGroup.MarginLayoutParams) view.getLayoutParams()).topMargin;
        }
        return 0;
    }

    private int getMarginWidth(View view) {
        if (view.getVisibility() != GONE) {
            return ((ViewGroup.MarginLayoutParams) view.getLayoutParams()).leftMargin
                    + ((ViewGroup.MarginLayoutParams) view.getLayoutParams()).rightMargin;
        }
        return 0;
    }

    private int getRealHeight(View view) {
        if (view.getVisibility() != GONE) {
            return view.getMeasuredHeight()
                    + ((ViewGroup.MarginLayoutParams) view.getLayoutParams()).topMargin
                    + ((ViewGroup.MarginLayoutParams) view.getLayoutParams()).bottomMargin;
        }
        return 0;
    }

    private int getRealWidthWithMargin(View view) {
        if (view.getVisibility() != GONE) {
            return view.getMeasuredWidth()
                    + ((ViewGroup.MarginLayoutParams) view.getLayoutParams()).leftMargin
                    + ((ViewGroup.MarginLayoutParams) view.getLayoutParams()).rightMargin;
        }
        return 0;
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        setOrientation(HORIZONTAL);
        mReddotMarginEndInRightNoAssignment = getContext().getResources()
                .getDimensionPixelSize(R.dimen.support_preference_reddot_margin_end_in_right_noassignment);
        mReddotMarginEndInRightHasAssignment = getContext().getResources()
                .getDimensionPixelSize(R.dimen.support_preference_reddot_margin_end_in_right_hasassignment);
        mMessageLayoutMarginEndInRight = getContext().getResources()
                .getDimensionPixelSize(R.dimen.support_preference_title_margin_end_in_right);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.COUICustomLinearLayoutForPreference, defStyleAttr, 0);
        mDefaultMarginEnd = a.getDimensionPixelOffset(
                R.styleable.COUICustomLinearLayoutForPreference_couiMessageLayoutMarginEnd, 0);
        mBStickToC = a.getBoolean(R.styleable.COUICustomLinearLayoutForPreference_couiBStickToC, mBStickToC);
        mAHavePriority = a.getBoolean(R.styleable.COUICustomLinearLayoutForPreference_couiAHavePriority, mAHavePriority);
        mSetMessageLayoutMarginEnd = a.getBoolean(
                R.styleable.COUICustomLinearLayoutForPreference_couiMarginEndOfA,
                mSetMessageLayoutMarginEnd
        );
        a.recycle();
        mViewLowPriorityMinWidth = context.getResources()
                .getDimensionPixelSize(R.dimen.assignment_in_right_low_priority_min_width);
    }

    private boolean isRtlMode() {
        return getLayoutDirection() == LAYOUT_DIRECTION_RTL;
    }

    private void setChildSize(View view, int width) {
        view.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
    }

    private boolean setReddotAndMessageLayoutMarginEnd() {
        boolean changed;
        View view;
        View view_3;
        if (viewB == null || viewB.getVisibility() != VISIBLE) {
            changed = false;
        } else {
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) viewB.getLayoutParams();
            if (viewC == null || viewC.getVisibility() != VISIBLE) {
                int marginEnd = layoutParams.getMarginEnd();
                int target = mReddotMarginEndInRightNoAssignment;
                if (marginEnd != target) {
                    layoutParams.setMarginEnd(target);
                    viewB.setLayoutParams(layoutParams);
                    changed = true;
                } else {
                    changed = false;
                }
            } else {
                int marginEnd = layoutParams.getMarginEnd();
                int target = mReddotMarginEndInRightHasAssignment;
                if (marginEnd != target) {
                    layoutParams.setMarginEnd(target);
                    viewB.setLayoutParams(layoutParams);
                    changed = true;
                } else {
                    changed = false;
                }
            }
        }
        if (mSetMessageLayoutMarginEnd && (view = viewA) != null && view.getVisibility() == VISIBLE) {
            View view_2 = viewB;
            if ((view_2 == null || view_2.getVisibility() != VISIBLE)
                    && ((view_3 = viewC) == null || view_3.getVisibility() != VISIBLE)) {
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) viewA.getLayoutParams();
                int marginEnd = layoutParams.getMarginEnd();
                int target = mDefaultMarginEnd;
                if (marginEnd != target) {
                    layoutParams.setMarginEnd(target);
                    viewA.setLayoutParams(layoutParams);
                    return true;
                }
            } else {
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) viewA.getLayoutParams();
                int marginEnd = layoutParams.getMarginEnd();
                int target = mMessageLayoutMarginEndInRight;
                if (marginEnd != target) {
                    layoutParams.setMarginEnd(target);
                    viewA.setLayoutParams(layoutParams);
                    return true;
                }
            }
        }
        return changed;
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int realWidthWithMargin;
        int realWidthWithMargin2;
        int paddingLeft = getPaddingLeft();
        int measuredWidth = getMeasuredWidth() - getPaddingRight();
        int paddingTop = getPaddingTop();
        int measuredHeight = (getMeasuredHeight() - getPaddingBottom()) - paddingTop;
        int realHeight = ((measuredHeight - getRealHeight(viewA)) / 2) + paddingTop;
        int realHeight2 = ((measuredHeight - getRealHeight(viewC)) / 2) + paddingTop;
        int realHeight3 = paddingTop + ((measuredHeight - getRealHeight(viewB)) / 2);
        if (isRtlMode()) {
            int realWidthWithMargin3 = measuredWidth - getRealWidthWithMargin(viewA);
            realWidthWithMargin = mBStickToC
                    ? getRealWidthWithMargin(viewC) + paddingLeft
                    : realWidthWithMargin3 - getRealWidthWithMargin(viewB);
            realWidthWithMargin2 = paddingLeft;
            paddingLeft = realWidthWithMargin3;
        } else {
            realWidthWithMargin2 = measuredWidth - getRealWidthWithMargin(viewC);
            realWidthWithMargin = mBStickToC
                    ? realWidthWithMargin2 - getRealWidthWithMargin(viewB)
                    : getRealWidthWithMargin(viewA) + paddingLeft;
        }
        View view = viewA;
        view.layout(
                getMarginLeft(view) + paddingLeft,
                getMarginTop(viewA) + realHeight,
                ((paddingLeft + getMarginLeft(viewA)) + getRealWidthWithMargin(viewA)) - getMarginWidth(viewA),
                ((realHeight + getMarginTop(viewA)) + getRealHeight(viewA)) - getMarginHeight(viewA)
        );
        View view_2 = viewC;
        view_2.layout(
                getMarginLeft(view_2) + realWidthWithMargin2,
                getMarginTop(viewC) + realHeight2,
                ((realWidthWithMargin2 + getMarginLeft(viewC)) + getRealWidthWithMargin(viewC)) - getMarginWidth(viewC),
                ((realHeight2 + getMarginTop(viewC)) + getRealHeight(viewC)) - getMarginHeight(viewC)
        );
        View view_3 = viewB;
        view_3.layout(
                getMarginLeft(view_3) + realWidthWithMargin,
                getMarginTop(viewB) + realHeight3,
                ((realWidthWithMargin + getMarginLeft(viewB)) + getRealWidthWithMargin(viewB)) - getMarginWidth(viewB),
                ((realHeight3 + getMarginTop(viewB)) + getRealHeight(viewB)) - getMarginHeight(viewB)
        );
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        viewA = getChildAt(0);
        viewB = getChildAt(1);
        viewC = getChildAt(2);
        if (setReddotAndMessageLayoutMarginEnd()) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
        customMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
