package com.coui.appcompat.grid;

import com.coui.appcompat.R;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;


public class COUIPercentWidthRelativeLayout extends RelativeLayout {
    private static final int CARD_LIST_FLAG = 2;
    private static final int DEFAULT_FLAG = 0;
    private static final int LARGE_PADDING = 0;
    private static final int LIST_FLAG = 1;
    private static final int PADDING_MODE = 0;
    private static final int REMEASURE_MODE = 1;
    private static final int SMALL_PADDING = 1;
    private int mGridNumber;
    private int mGridNumberResourceId;
    private int mInitPaddingEnd;
    private int mInitPaddingStart;
    private boolean mIsActivityEmbedded;
    private boolean mIsParentChildHierarchy;
    public int mMode;
    private int mPaddingSize;
    private int mPaddingType;
    private boolean mPercentEnabled;
    private int mScreenPhysicalWidth;

    public COUIPercentWidthRelativeLayout(Context context) {
        this(context, null);
    }

    private void initAttr(AttributeSet attributeSet) {
        if (getContext() != null) {
            TypedArray typedArrayObtainStyledAttributes = getContext().obtainStyledAttributes(attributeSet, R.styleable.COUIPercentWidthRelativeLayout);
            int gridNumberStyleable = R.styleable.COUIPercentWidthRelativeLayout_gridNumber;
            this.mGridNumberResourceId = typedArrayObtainStyledAttributes.getResourceId(gridNumberStyleable, 0);
            this.mGridNumber = typedArrayObtainStyledAttributes.getInteger(gridNumberStyleable, getContext().getResources().getInteger(R.integer.grid_guide_column_preference));
            this.mPaddingType = typedArrayObtainStyledAttributes.getInteger(R.styleable.COUIPercentWidthRelativeLayout_paddingType, DEFAULT_FLAG);
            this.mPaddingSize = typedArrayObtainStyledAttributes.getInteger(R.styleable.COUIPercentWidthRelativeLayout_paddingSize, LARGE_PADDING);
            this.mPercentEnabled = typedArrayObtainStyledAttributes.getBoolean(R.styleable.COUIPercentWidthRelativeLayout_percentIndentEnabled, true);
            this.mMode = typedArrayObtainStyledAttributes.getInt(R.styleable.COUIPercentWidthRelativeLayout_percentMode, PADDING_MODE);
            this.mIsParentChildHierarchy = typedArrayObtainStyledAttributes.getBoolean(R.styleable.COUIPercentWidthRelativeLayout_isParentChildHierarchy, false);
            this.mInitPaddingStart = getPaddingStart();
            this.mInitPaddingEnd = getPaddingEnd();
            typedArrayObtainStyledAttributes.recycle();
        }
    }

    private void prepareForMeasure() {
        Context context = getContext();
        if (context != null) {
            this.mIsActivityEmbedded = COUIResponsiveUtils.isActivityEmbedded(getContext());
            if (context instanceof Activity) {
                this.mScreenPhysicalWidth = COUIResponsiveUtils.getScreenPhysicalWidth((Activity) context);
            } else {
                this.mScreenPhysicalWidth = -1;
            }
        }
    }

    @Override
    public boolean checkLayoutParams(ViewGroup.LayoutParams layoutParams) {
        return layoutParams instanceof LayoutParams;
    }

    @Override
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        if (getContext() != null && this.mGridNumberResourceId != 0) {
            this.mGridNumber = getContext().getResources().getInteger(this.mGridNumberResourceId);
        }
        prepareForMeasure();
        requestLayout();
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measureWidthSpec;
        if (this.mPercentEnabled) {
            measureWidthSpec = COUIResponsiveUtils.measureLayout(this, widthMeasureSpec, this.mGridNumber, this.mPaddingType, this.mPaddingSize, this.mMode, this.mInitPaddingStart, this.mInitPaddingEnd, this.mScreenPhysicalWidth, this.mIsParentChildHierarchy, this.mIsActivityEmbedded);
            for (int index = 0; index < getChildCount(); index++) {
                LayoutParams layoutParams = (LayoutParams) getChildAt(index).getLayoutParams();
                COUIResponsiveUtils.measureChildWithPercent(getContext(), getChildAt(index), measureWidthSpec, this.mPaddingType, this.mPaddingSize, layoutParams.mGridNumber, layoutParams.mPercentMode);
            }
        } else {
            measureWidthSpec = widthMeasureSpec;
        }
        super.onMeasure(measureWidthSpec, heightMeasureSpec);
    }

    public void setIsParentChildHierarchy(boolean isParentChildHierarchy) {
        this.mIsParentChildHierarchy = isParentChildHierarchy;
        requestLayout();
    }

    public void setPercentIndentEnabled(boolean percentEnabled) {
        this.mPercentEnabled = percentEnabled;
        requestLayout();
    }

    public static class LayoutParams extends RelativeLayout.LayoutParams {
        public int mGridNumber;
        public int mPercentMode;

        public LayoutParams(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
            init(context, attributeSet);
        }

        private void init(Context context, AttributeSet attributeSet) {
            TypedArray typedArrayObtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.COUIPercentWidthRelativeLayout_Layout);
            this.mGridNumber = typedArrayObtainStyledAttributes.getInt(R.styleable.COUIPercentWidthRelativeLayout_Layout_layout_gridNumber, 0);
            this.mPercentMode = typedArrayObtainStyledAttributes.getInt(R.styleable.COUIPercentWidthRelativeLayout_Layout_layout_percentMode, 0);
            typedArrayObtainStyledAttributes.recycle();
        }

        public void setGridNumber(int gridNumber) {
            this.mGridNumber = gridNumber;
        }

        public void setPercentMode(int percentMode) {
            this.mPercentMode = percentMode;
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.LayoutParams layoutParams) {
            super(layoutParams);
        }

        public LayoutParams(ViewGroup.MarginLayoutParams marginLayoutParams) {
            super(marginLayoutParams);
        }

        public LayoutParams(LinearLayout.LayoutParams layoutParams) {
            super((ViewGroup.MarginLayoutParams) layoutParams);
        }
    }

    public COUIPercentWidthRelativeLayout(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    @Override
    public LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(-1, -1);
    }

    public COUIPercentWidthRelativeLayout(Context context, AttributeSet attributeSet, int defStyleAttr) {
        this(context, attributeSet, defStyleAttr, 0);
    }

    public COUIPercentWidthRelativeLayout(Context context, AttributeSet attributeSet, int defStyleAttr, int defStyleRes) {
        super(context, attributeSet, defStyleAttr, defStyleRes);
        this.mMode = 0;
        this.mPercentEnabled = true;
        this.mIsActivityEmbedded = false;
        this.mScreenPhysicalWidth = 0;
        initAttr(attributeSet);
        prepareForMeasure();
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attributeSet) {
        return new LayoutParams(getContext(), attributeSet);
    }

    @Override
    public LayoutParams generateLayoutParams(ViewGroup.LayoutParams layoutParams) {
        return new LayoutParams(layoutParams);
    }
}
