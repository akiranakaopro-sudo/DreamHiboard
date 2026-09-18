package com.coui.appcompat.grid;

import com.coui.appcompat.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import com.coui.appcompat.list.COUIListView;

public class COUIPercentWidthListView extends COUIListView {
    private static final int CARD_LIST_FLAG = 2;
    private static final int DEFAULT_FLAG = 0;
    private static final int LARGE_PADDING = 0;
    private static final int LIST_FLAG = 1;
    private static final int PADDING_MODE = 0;
    private static final int REMEASURE_MODE = 1;
    private static final int SMALL_PADDING = 1;
    private static final String TAG = "COUIPercentWidthListView";
    private int mGridNumber;
    private int mGridNumberResourceId;
    private int mInitPaddingEnd;
    private int mInitPaddingStart;
    private boolean mIsActivityEmbedded;
    private boolean mIsParentChildHierarchy;
    private boolean mMeasureEnabled;
    public int mMode;
    private int mPaddingSize;
    private int mPaddingType;
    private boolean mPercentEnabled;
    private int mScreenPhysicalWidth;

    public COUIPercentWidthListView(Context context) {
        this(context, null);
    }

    private void initAttr(AttributeSet attributeSet) {
        if (getContext() != null) {
            TypedArray typedArrayObtainStyledAttributes = getContext().obtainStyledAttributes(attributeSet, R.styleable.COUIPercentWidthListView);
            int gridNumberStyleable = R.styleable.COUIPercentWidthListView_couiListGridNumber;
            this.mGridNumberResourceId = typedArrayObtainStyledAttributes.getResourceId(gridNumberStyleable, 0);
            this.mGridNumber = typedArrayObtainStyledAttributes.getInteger(gridNumberStyleable, getContext().getResources().getInteger(R.integer.grid_guide_column_preference));
            this.mMode = typedArrayObtainStyledAttributes.getInt(R.styleable.COUIPercentWidthListView_percentMode, PADDING_MODE);
            this.mPaddingType = typedArrayObtainStyledAttributes.getInteger(R.styleable.COUIPercentWidthListView_paddingType, LIST_FLAG);
            this.mPaddingSize = typedArrayObtainStyledAttributes.getInteger(R.styleable.COUIPercentWidthListView_paddingSize, LARGE_PADDING);
            this.mIsParentChildHierarchy = typedArrayObtainStyledAttributes.getBoolean(R.styleable.COUIPercentWidthRecyclerView_isParentChildHierarchy, false);
            this.mPercentEnabled = typedArrayObtainStyledAttributes.getBoolean(R.styleable.COUIPercentWidthLinearLayout_percentIndentEnabled, true);
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
        if (this.mMeasureEnabled) {
            if (this.mPercentEnabled) {
                widthMeasureSpec = COUIResponsiveUtils.measureLayout(this, widthMeasureSpec, this.mGridNumber, this.mPaddingType, this.mPaddingSize, this.mMode, this.mInitPaddingStart, this.mInitPaddingEnd, this.mScreenPhysicalWidth, this.mIsParentChildHierarchy, this.mIsActivityEmbedded);
            } else if (getPaddingStart() != this.mInitPaddingStart || getPaddingEnd() != this.mInitPaddingEnd) {
                setPaddingRelative(this.mInitPaddingStart, getPaddingTop(), this.mInitPaddingEnd, getPaddingBottom());
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setIsParentChildHierarchy(boolean isParentChildHierarchy) {
        this.mIsParentChildHierarchy = isParentChildHierarchy;
        requestLayout();
    }

    @SuppressLint({"LongLogTag"})
    public void setMeasureEnable(boolean measureEnabled) {
        Log.d(TAG, "setMeasureEnable = " + measureEnabled + " " + Log.getStackTraceString(new Throwable()));
        this.mMeasureEnabled = measureEnabled;
    }

    public void setPercentIndentEnabled(boolean percentEnabled) {
        this.mPercentEnabled = percentEnabled;
        requestLayout();
    }

    public COUIPercentWidthListView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mPercentEnabled = true;
        this.mMode = 0;
        this.mIsActivityEmbedded = false;
        this.mScreenPhysicalWidth = 0;
        this.mMeasureEnabled = true;
        initAttr(attributeSet);
        prepareForMeasure();
    }

    public COUIPercentWidthListView(Context context, AttributeSet attributeSet, int defStyleAttr) {
        super(context, attributeSet, defStyleAttr);
        this.mPercentEnabled = true;
        this.mMode = 0;
        this.mIsActivityEmbedded = false;
        this.mScreenPhysicalWidth = 0;
        this.mMeasureEnabled = true;
        initAttr(attributeSet);
        this.mInitPaddingStart = getPaddingStart();
        this.mInitPaddingEnd = getPaddingEnd();
        setScrollBarStyle(33554432);
    }
}
