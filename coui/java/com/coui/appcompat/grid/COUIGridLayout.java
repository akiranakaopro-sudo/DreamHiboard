package com.coui.appcompat.grid;

import com.coui.appcompat.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import com.coui.appcompat.uiutil.UIUtil;
import com.coui.component.responsiveui.ResponsiveUIModel;
import com.coui.component.responsiveui.layoutgrid.MarginType;


public class COUIGridLayout extends GridLayout {
    public static final int GRID_MODE = 0;
    private static final int LARGE_MARGIN = 0;
    private static final int SMALL_MARGIN = 1;
    public static final int SPECIFIC_GAP_MODE = 1;
    public static final int SPECIFIC_SIZE_MODE = 2;
    private int[] mBottomMargin;
    private int mChildGridNumber;
    private float mChildHeight;
    private float mChildMinHeight;
    private float mChildMinWidth;
    private float mChildWidth;
    private int mColumn;
    private int[] mEndMargin;
    private int mGridMargin;
    private int mGridMarginType;
    private float[] mGridModeColumnWidth;
    private float mHorizontalGap;
    private boolean mIsIgnoreChildMargin;
    private int[] mMaxHorizontalMargin;
    private int[] mMaxVerticalMargin;
    private float mMinHorizontalGap;
    private ResponsiveUIModel mResponsiveUIModel;
    private int[] mStartMargin;
    private int[] mTopMargin;
    private int mType;
    private float mVerticalGap;

    public COUIGridLayout(Context context) {
        this(context, null);
    }

    private int adjustHorizontalMargin() {
        if (this.mIsIgnoreChildMargin) {
            return 0;
        }
        this.mMaxHorizontalMargin = new int[this.mColumn + 1];
        int totalHorizontalMargin = 0;
        for (int columnIndex = 0; columnIndex <= this.mColumn; columnIndex++) {
            int childIndex = columnIndex;
            while (childIndex < this.mStartMargin.length) {
                int[] startMargins = this.mStartMargin;
                int columnCount = this.mColumn;
                if (columnIndex < columnCount) {
                    int[] maxHorizontalMargins = this.mMaxHorizontalMargin;
                    int currentMaxMargin = maxHorizontalMargins[columnIndex];
                    int startMargin = startMargins[childIndex];
                    if (currentMaxMargin < startMargin) {
                        maxHorizontalMargins[columnIndex] = startMargin;
                    }
                }
                if (columnIndex > 0 && childIndex > 0) {
                    int[] endMargins = this.mEndMargin;
                    if (childIndex <= endMargins.length) {
                        int[] maxHorizontalMargins = this.mMaxHorizontalMargin;
                        int currentMaxMargin = maxHorizontalMargins[columnIndex];
                        int previousEndMargin = endMargins[childIndex - 1];
                        if (currentMaxMargin < previousEndMargin) {
                            maxHorizontalMargins[columnIndex] = previousEndMargin;
                        }
                    }
                }
                childIndex += columnCount;
            }
            totalHorizontalMargin += this.mMaxHorizontalMargin[columnIndex];
        }
        return totalHorizontalMargin;
    }

    private float calculateChildHeight() {
        float childHeight = this.mChildHeight;
        if (childHeight != 0.0f) {
            return childHeight;
        }
        float childMinHeight = this.mChildMinHeight;
        if (childMinHeight == 0.0f) {
            return 0.0f;
        }
        return (childMinHeight / this.mChildMinWidth) * this.mChildWidth;
    }

    private int calculateHorizontalMargin() {
        int previousEndMargin;
        int startMargin;
        if (this.mIsIgnoreChildMargin) {
            return 0;
        }
        int totalHorizontalMargin = 0;
        for (int columnIndex = 0; columnIndex <= this.mColumn; columnIndex++) {
            int childIndex = columnIndex;
            int maxMargin = 0;
            while (childIndex < this.mStartMargin.length) {
                int[] startMargins = this.mStartMargin;
                int columnCount = this.mColumn;
                if (columnIndex < columnCount && maxMargin < (startMargin = startMargins[childIndex])) {
                    maxMargin = startMargin;
                }
                if (columnIndex > 0 && childIndex > 0) {
                    int[] endMargins = this.mEndMargin;
                    if (childIndex <= endMargins.length && maxMargin < (previousEndMargin = endMargins[childIndex - 1])) {
                        maxMargin = previousEndMargin;
                    }
                }
                childIndex += columnCount;
            }
            totalHorizontalMargin += maxMargin;
        }
        return totalHorizontalMargin;
    }

    private void calculateInGridMode() {
        if (getContext() == null) {
            return;
        }
        this.mResponsiveUIModel.rebuild(getMeasuredWidth(), UIUtil.getScreenHeightMetrics(getContext())).chooseMargin(this.mGridMarginType == SMALL_MARGIN ? MarginType.MARGIN_SMALL : MarginType.MARGIN_LARGE);
        this.mGridMargin = this.mResponsiveUIModel.margin();
        this.mHorizontalGap = this.mResponsiveUIModel.gutter();
        this.mColumn = this.mResponsiveUIModel.columnCount() / this.mChildGridNumber;
        int columnIndex = 0;
        this.mChildWidth = this.mResponsiveUIModel.width(0, this.mChildGridNumber - 1);
        this.mGridModeColumnWidth = new float[this.mChildGridNumber];
        while (true) {
            int columnCount = this.mColumn;
            if (columnIndex >= columnCount) {
                this.mMaxHorizontalMargin = new int[columnCount + 1];
                return;
            }
            float[] gridModeColumnWidth = this.mGridModeColumnWidth;
            ResponsiveUIModel responsiveUIModel = this.mResponsiveUIModel;
            int childGridNumber = this.mChildGridNumber;
            gridModeColumnWidth[columnIndex] = responsiveUIModel.width(columnIndex * childGridNumber, ((columnIndex + 1) * childGridNumber) - 1);
            columnIndex++;
        }
    }

    private void calculateInSpecificGapMode() {
        float widthWithoutPadding = getWidthWithoutPadding();
        float horizontalGap = this.mHorizontalGap;
        this.mColumn = Math.max(1, (int) ((widthWithoutPadding + horizontalGap) / (horizontalGap + this.mChildMinWidth)));
        float widthWithoutPadding2 = getWidthWithoutPadding() - calculateHorizontalMargin();
        float horizontalGap2 = this.mHorizontalGap;
        this.mColumn = Math.max(1, (int) ((widthWithoutPadding2 + horizontalGap2) / (horizontalGap2 + this.mChildMinWidth)));
        float widthWithoutPadding3 = getWidthWithoutPadding() - adjustHorizontalMargin();
        float horizontalGap3 = this.mHorizontalGap;
        this.mChildWidth = Math.max(0.0f, (widthWithoutPadding3 - (horizontalGap3 * (this.mColumn - 1))) / this.mColumn);
        this.mChildHeight = calculateChildHeight();
    }

    private void calculateInSpecificSizeMode() {
        float widthWithoutPadding = getWidthWithoutPadding();
        float minHorizontalGap = this.mMinHorizontalGap;
        this.mColumn = Math.max(1, (int) ((widthWithoutPadding + minHorizontalGap) / (minHorizontalGap + this.mChildWidth)));
        float widthWithoutPadding2 = getWidthWithoutPadding() - calculateHorizontalMargin();
        float minHorizontalGap2 = this.mMinHorizontalGap;
        this.mColumn = Math.max(1, (int) ((widthWithoutPadding2 + minHorizontalGap2) / (minHorizontalGap2 + this.mChildWidth)));
        this.mHorizontalGap = Math.max(0.0f, ((getWidthWithoutPadding() - adjustHorizontalMargin()) - (this.mChildWidth * this.mColumn)) / (this.mColumn - 1));
    }

    private void calculateMargins() {
        int childCount = getChildCount();
        this.mTopMargin = new int[childCount];
        this.mBottomMargin = new int[childCount];
        this.mStartMargin = new int[childCount];
        this.mEndMargin = new int[childCount];
        if (this.mIsIgnoreChildMargin) {
            return;
        }
        int visibleChildIndex = 0;
        for (int childIndex = 0; childIndex < childCount; childIndex++) {
            View childAt = getChildAt(childIndex);
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) childAt.getLayoutParams();
            if (childAt.getVisibility() != View.GONE) {
                this.mTopMargin[visibleChildIndex] = marginLayoutParams.topMargin;
                this.mBottomMargin[visibleChildIndex] = marginLayoutParams.bottomMargin;
                this.mStartMargin[visibleChildIndex] = marginLayoutParams.getMarginStart();
                this.mEndMargin[visibleChildIndex] = marginLayoutParams.getMarginEnd();
                visibleChildIndex++;
            }
        }
    }

    private int calculateVerticalMargin(int rowCount) {
        int nextRowIndex;
        int rowIndex = 0;
        if (this.mIsIgnoreChildMargin) {
            return 0;
        }
        this.mMaxVerticalMargin = new int[rowCount + 1];
        int totalVerticalMargin = 0;
        while (rowIndex <= rowCount) {
            int childIndex = this.mColumn * rowIndex;
            nextRowIndex = rowIndex + 1;
            int columnCount = this.mColumn;
            while (childIndex < nextRowIndex * columnCount) {
                int[] topMargins = this.mTopMargin;
                if (childIndex < topMargins.length) {
                    int[] maxVerticalMargins = this.mMaxVerticalMargin;
                    int currentMaxMargin = maxVerticalMargins[rowIndex];
                    int topMargin = topMargins[childIndex];
                    if (currentMaxMargin < topMargin) {
                        maxVerticalMargins[rowIndex] = topMargin;
                    }
                }
                if (rowIndex > 0 && childIndex > 0) {
                    int previousRowChildIndex = childIndex - columnCount;
                    int[] bottomMargins = this.mBottomMargin;
                    if (previousRowChildIndex < bottomMargins.length) {
                        int[] maxVerticalMargins = this.mMaxVerticalMargin;
                        if (maxVerticalMargins[rowIndex] < bottomMargins[childIndex - columnCount]) {
                            maxVerticalMargins[rowIndex] = bottomMargins[childIndex - columnCount];
                        }
                    }
                }
                childIndex++;
            }
            totalVerticalMargin += this.mMaxVerticalMargin[rowIndex];
            rowIndex = nextRowIndex;
        }
        return totalVerticalMargin;
    }

    private int getVisibleChildCount() {
        int visibleChildCount = 0;
        for (int childIndex = 0; childIndex < getChildCount(); childIndex++) {
            if (getChildAt(childIndex).getVisibility() != View.GONE) {
                visibleChildCount++;
            }
        }
        return visibleChildCount;
    }

    private int getWidthWithoutPadding() {
        return (getMeasuredWidth() - getPaddingStart()) - getPaddingEnd();
    }

    private void initAttr(AttributeSet attributeSet) {
        if (getContext() != null) {
            TypedArray typedArrayObtainStyledAttributes = getContext().obtainStyledAttributes(attributeSet, R.styleable.COUIGridLayout);
            this.mHorizontalGap = typedArrayObtainStyledAttributes.getDimension(R.styleable.COUIGridLayout_couiHorizontalGap, 0.0f);
            this.mMinHorizontalGap = typedArrayObtainStyledAttributes.getDimension(R.styleable.COUIGridLayout_minHorizontalGap, 0.0f);
            this.mVerticalGap = typedArrayObtainStyledAttributes.getDimension(R.styleable.COUIGridLayout_couiVerticalGap, 0.0f);
            this.mChildMinWidth = typedArrayObtainStyledAttributes.getDimension(R.styleable.COUIGridLayout_childMinWidth, 0.0f);
            this.mChildMinHeight = typedArrayObtainStyledAttributes.getDimension(R.styleable.COUIGridLayout_childMinHeight, 0.0f);
            this.mChildHeight = typedArrayObtainStyledAttributes.getDimension(R.styleable.COUIGridLayout_childHeight, 0.0f);
            this.mChildWidth = typedArrayObtainStyledAttributes.getDimension(R.styleable.COUIGridLayout_childWidth, 0.0f);
            this.mChildGridNumber = typedArrayObtainStyledAttributes.getInteger(R.styleable.COUIGridLayout_childGridNumber, 0);
            this.mGridMarginType = typedArrayObtainStyledAttributes.getInteger(R.styleable.COUIGridLayout_gridMarginType, SMALL_MARGIN);
            this.mType = typedArrayObtainStyledAttributes.getInteger(R.styleable.COUIGridLayout_specificType, 0);
            typedArrayObtainStyledAttributes.recycle();
        }
    }

    private void initUIManager() {
        if (getContext() != null) {
            this.mResponsiveUIModel = new ResponsiveUIModel(getContext(), 0, 0);
        }
    }

    private boolean isLayoutRTL() {
        return getLayoutDirection() == 1;
    }

    private int measureHeight(int heightMeasureSpec, double rowCount) {
        int verticalMargin = calculateVerticalMargin((int) rowCount);
        int mode = View.MeasureSpec.getMode(heightMeasureSpec);
        int size = View.MeasureSpec.getSize(heightMeasureSpec);
        if (mode == View.MeasureSpec.AT_MOST) {
            return Math.min(size, (int) ((((double) this.mChildHeight) * rowCount) + ((rowCount - 1.0d) * ((double) this.mVerticalGap)) + ((double) verticalMargin)));
        }
        if (mode == View.MeasureSpec.UNSPECIFIED) {
            return (int) ((((double) this.mChildHeight) * rowCount) + ((rowCount - 1.0d) * ((double) this.mVerticalGap)) + ((double) verticalMargin));
        }
        if (mode != View.MeasureSpec.EXACTLY) {
            return 0;
        }
        return size;
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int childLeft;
        int childRight;
        super.onLayout(changed, left, top, right, bottom);
        int paddingStart = getPaddingStart() + this.mGridMargin;
        int paddingTop = getPaddingTop();
        int visibleChildIndex = 0;
        for (int childIndex = 0; childIndex < getChildCount(); childIndex++) {
            View childAt = getChildAt(childIndex);
            float childWidth = this.mType == GRID_MODE ? this.mGridModeColumnWidth[childIndex % this.mColumn] : this.mChildWidth;
            int horizontalMargin = this.mIsIgnoreChildMargin ? 0 : Math.max(0, this.mMaxHorizontalMargin[visibleChildIndex % this.mColumn]);
            int verticalMargin = this.mIsIgnoreChildMargin ? 0 : Math.max(0, this.mMaxVerticalMargin[visibleChildIndex / this.mColumn]);
            if (childAt.getVisibility() != View.GONE) {
                if (isLayoutRTL()) {
                    childRight = (getWidth() - paddingStart) - horizontalMargin;
                    childLeft = (int) (childRight - childWidth);
                } else {
                    childLeft = paddingStart + horizontalMargin;
                    childRight = (int) (childLeft + childWidth);
                }
                int childTop = paddingTop + verticalMargin;
                childAt.layout(childLeft, childTop, childRight, (int) (childTop + this.mChildHeight));
                visibleChildIndex++;
                if (visibleChildIndex % this.mColumn == 0) {
                    paddingStart = getPaddingStart() + this.mGridMargin;
                    paddingTop = (int) (paddingTop + this.mChildHeight + this.mVerticalGap + verticalMargin);
                } else {
                    paddingStart = (int) (paddingStart + this.mHorizontalGap + childWidth + horizontalMargin);
                }
            }
        }
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        calculateMargins();
        int type = this.mType;
        if (type == GRID_MODE) {
            calculateInGridMode();
        } else if (type == SPECIFIC_GAP_MODE) {
            calculateInSpecificGapMode();
        } else if (type == SPECIFIC_SIZE_MODE) {
            calculateInSpecificSizeMode();
        }
        for (int childIndex = 0; childIndex < getChildCount(); childIndex++) {
            View childAt = getChildAt(childIndex);
            if (this.mChildHeight == 0.0f) {
                this.mChildHeight = childAt.getMeasuredHeight();
            }
            measureChild(childAt, ViewGroup.getChildMeasureSpec(widthMeasureSpec, 0, (int) this.mChildWidth), ViewGroup.getChildMeasureSpec(heightMeasureSpec, 0, (int) this.mChildHeight));
        }
        setMeasuredDimension(View.resolveSizeAndState(View.MeasureSpec.getSize(widthMeasureSpec), widthMeasureSpec, 0), measureHeight(heightMeasureSpec, Math.ceil(getVisibleChildCount() / this.mColumn)));
    }

    public void setChildGridNumber(int childGridNumber) {
        this.mChildGridNumber = childGridNumber;
        requestLayout();
    }

    public void setChildHeight(float childHeight) {
        this.mChildHeight = childHeight;
        requestLayout();
    }

    public void setChildMinHeight(float childMinHeight) {
        this.mChildMinHeight = childMinHeight;
        requestLayout();
    }

    public void setChildMinWidth(float childMinWidth) {
        this.mChildMinWidth = childMinWidth;
        requestLayout();
    }

    public void setChildWidth(float childWidth) {
        this.mChildWidth = childWidth;
        requestLayout();
    }

    public void setGridMarginType(int gridMarginType) {
        this.mGridMarginType = gridMarginType;
        requestLayout();
    }

    public void setHorizontalGap(float horizontalGap) {
        this.mHorizontalGap = horizontalGap;
        requestLayout();
    }

    public void setIsIgnoreChildMargin(boolean ignoreChildMargin) {
        this.mIsIgnoreChildMargin = ignoreChildMargin;
    }

    public void setMinHorizontalGap(float minHorizontalGap) {
        this.mMinHorizontalGap = minHorizontalGap;
        requestLayout();
    }

    public void setType(int type) {
        this.mType = type;
        requestLayout();
    }

    public void setVerticalGap(float verticalGap) {
        this.mVerticalGap = verticalGap;
        requestLayout();
    }

    public COUIGridLayout(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public COUIGridLayout(Context context, AttributeSet attributeSet, int defStyleAttr) {
        this(context, attributeSet, defStyleAttr, 0);
    }

    public COUIGridLayout(Context context, AttributeSet attributeSet, int defStyleAttr, int defStyleRes) {
        super(context, attributeSet, defStyleAttr, defStyleRes);
        this.mIsIgnoreChildMargin = true;
        initUIManager();
        initAttr(attributeSet);
    }
}
