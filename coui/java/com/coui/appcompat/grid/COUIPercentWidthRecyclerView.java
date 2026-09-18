package com.coui.appcompat.grid;

import com.coui.appcompat.R;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import androidx.recyclerview.widget.COUIRecyclerView;

public class COUIPercentWidthRecyclerView extends COUIRecyclerView {
    private static final int CARD_LIST_FLAG = 2;
    private static final int DEFAULT_FLAG = 0;
    private static final int LARGE_PADDING = 0;
    private static final int LIST_FLAG = 1;
    private static final int SMALL_PADDING = 1;
    private static final String TAG = "COUIPercentWidthRecyclerView";
    private int mGridNumber;
    private int mGridNumberResourceId;
    private int mInitPaddingEnd;
    private int mInitPaddingStart;
    private boolean mIsActivityEmbedded;
    private boolean mIsParentChildHierarchy;
    private int mPaddingSize;
    private int mPaddingType;
    private boolean mPercentEnabled;
    private int mScreenPhysicalWidth;

    public COUIPercentWidthRecyclerView(Context context) {
        this(context, null);
    }

    private void initAttr(AttributeSet attributeSet) {
        if (getContext() != null) {
            TypedArray typedArrayObtainStyledAttributes = getContext().obtainStyledAttributes(attributeSet, R.styleable.COUIPercentWidthRecyclerView);
            int gridNumberStyleable = R.styleable.COUIPercentWidthRecyclerView_couiRecyclerGridNumber;
            this.mGridNumberResourceId = typedArrayObtainStyledAttributes.getResourceId(gridNumberStyleable, 0);
            this.mGridNumber = typedArrayObtainStyledAttributes.getInteger(gridNumberStyleable, getContext().getResources().getInteger(R.integer.grid_guide_column_preference));
            this.mPaddingType = typedArrayObtainStyledAttributes.getInteger(R.styleable.COUIPercentWidthRecyclerView_paddingType, LIST_FLAG);
            this.mPaddingSize = typedArrayObtainStyledAttributes.getInteger(R.styleable.COUIPercentWidthRecyclerView_paddingSize, LARGE_PADDING);
            this.mIsParentChildHierarchy = typedArrayObtainStyledAttributes.getBoolean(R.styleable.COUIPercentWidthRecyclerView_isParentChildHierarchy, false);
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
        if (this.mPercentEnabled) {
            widthMeasureSpec = COUIResponsiveUtils.measureLayout(this, widthMeasureSpec, this.mGridNumber, this.mPaddingType, this.mPaddingSize, 0, this.mInitPaddingStart, this.mInitPaddingEnd, this.mScreenPhysicalWidth, this.mIsParentChildHierarchy, this.mIsActivityEmbedded);
        } else if (getPaddingStart() != this.mInitPaddingStart || getPaddingEnd() != this.mInitPaddingEnd) {
            setPaddingRelative(this.mInitPaddingStart, getPaddingTop(), this.mInitPaddingEnd, getPaddingBottom());
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setIsParentChildHierarchy(boolean isParentChildHierarchy) {
        this.mIsParentChildHierarchy = isParentChildHierarchy;
        requestLayout();
    }

    public void setPercentIndentEnabled(boolean percentEnabled) {
        this.mPercentEnabled = percentEnabled;
        requestLayout();
    }

    public COUIPercentWidthRecyclerView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public COUIPercentWidthRecyclerView(Context context, AttributeSet attributeSet, int defStyleAttr) {
        super(context, attributeSet, defStyleAttr);
        this.mPercentEnabled = true;
        this.mIsActivityEmbedded = false;
        this.mScreenPhysicalWidth = 0;
        initAttr(attributeSet);
        this.mInitPaddingStart = getPaddingStart();
        this.mInitPaddingEnd = getPaddingEnd();
        setScrollBarStyle(33554432);
        prepareForMeasure();
    }
}
