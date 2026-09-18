package com.coui.appcompat.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.coui.appcompat.R;

public class COUIHalfHeightHorizontalPaddingLinearLayout extends LinearLayout {
    private static final int SPECIAL_COUNT = 2;

    private View mEndView;
    private int mFixPaddingEnd;
    private View mStartView;

    public COUIHalfHeightHorizontalPaddingLinearLayout(Context context) {
        super(context);
        mFixPaddingEnd = 0;
    }

    public COUIHalfHeightHorizontalPaddingLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mFixPaddingEnd = 0;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.COUIHalfHeightHorizontalPaddingLinearLayout);
        mFixPaddingEnd = a.getDimensionPixelSize(
                R.styleable.COUIHalfHeightHorizontalPaddingLinearLayout_fixPaddingEnd,
                mFixPaddingEnd
        );
        a.recycle();
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (getChildCount() == SPECIAL_COUNT) {
            mStartView = getChildAt(0);
            mEndView = getChildAt(1);
            if (mStartView.getMeasuredHeight() < mEndView.getMeasuredHeight()) {
                setPadding(getPaddingStart(), 0, getPaddingEnd(), 0);
            }
            int halfHeight = getMeasuredHeight() / 2;
            if (halfHeight >= mFixPaddingEnd) {
                return;
            }
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) getLayoutParams();
            int marginEndLarge = getResources().getDimensionPixelSize(
                    R.dimen.support_preference_category_layout_title_margin_end_large
            );
            if (halfHeight != getPaddingStart()
                    || halfHeight != getPaddingEnd()
                    || layoutParams.getMarginEnd() == marginEndLarge
                    || layoutParams.getMarginEnd() == 0) {
                setPadding(halfHeight, getPaddingTop(), halfHeight, getPaddingBottom());
                if (halfHeight < mFixPaddingEnd) {
                    layoutParams.setMarginEnd((layoutParams.getMarginEnd() + mFixPaddingEnd) - halfHeight);
                    setLayoutParams(layoutParams);
                }
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }
        }
    }
}
