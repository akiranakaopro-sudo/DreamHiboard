package com.coui.appcompat.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import androidx.core.view.ViewCompat;
import com.coui.appcompat.R;
import com.coui.appcompat.contextutil.COUIContextUtil;
import com.coui.component.responsiveui.ResponsiveUIModel;
import com.coui.component.responsiveui.layoutgrid.MarginType;
import java.util.Arrays;

public class COUIResponsiveGridMaskView extends View {
    private static final boolean DEBUG = true;
    private static final String TAG = "COUIResponsiveGridMaskView";
    private int mColumnCount;
    private final Paint mColumnPaint;
    private final Rect mColumnRect;
    private int[] mColumnWidth;
    private Context mContext;
    private int mGutter;
    private int mMargin;
    private final Paint mMarginPaint;
    private final Rect mMarginRect;
    private MarginType mMarginType;
    private ResponsiveUIModel mResponsiveUIModel;

    public COUIResponsiveGridMaskView(Context context) {
        super(context);
        this.mColumnCount = 0;
        this.mGutter = 0;
        this.mMargin = 0;
        this.mMarginType = MarginType.MARGIN_SMALL;
        this.mMarginRect = new Rect();
        this.mColumnRect = new Rect();
        this.mMarginPaint = new Paint();
        this.mColumnPaint = new Paint();
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;
        this.mResponsiveUIModel = new ResponsiveUIModel(context, 0, 0);
        requestLatestGridParams();
        this.mMarginPaint.setColor(COUIContextUtil.getColor(context, R.color.responsive_ui_column_hint_margin));
        this.mColumnPaint.setColor(COUIContextUtil.getColor(context, R.color.responsive_ui_column_hint_column));
    }

    private void requestLatestGridParams() {
        this.mResponsiveUIModel.chooseMargin(this.mMarginType);
        this.mColumnCount = this.mResponsiveUIModel.columnCount();
        this.mColumnWidth = this.mResponsiveUIModel.columnWidth();
        this.mGutter = this.mResponsiveUIModel.gutter();
        this.mMargin = this.mResponsiveUIModel.margin();
        int columnWidthSum = 0;
        for (int columnWidth : this.mColumnWidth) {
            Log.d(TAG, "requestLatestGridParams: " + columnWidth);
            columnWidthSum += columnWidth;
        }
        Log.d(TAG, "requestLatestGridParams: \ngetMeasureWidth() = " + getMeasuredWidth() + "\nmMargin = " + this.mMargin + "\nmGutter = " + this.mGutter + "\nmColumnWidth = " + Arrays.toString(this.mColumnWidth) + "\nmColumnCount = " + this.mColumnCount + "\nsum(columnWidth) = " + columnWidthSum + "\ntotal = (mMargin * 2) + (mColumnWidth * mColumnCount) + (mGutter * (mColumnCount - 1)) = " + ((this.mMargin * 2) + columnWidthSum + (this.mGutter * (this.mColumnCount - 1))));
    }

    @Override
    public void onDetachedFromWindow() {
        this.mContext = null;
        super.onDetachedFromWindow();
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL) {
            int measuredWidth = getMeasuredWidth();
            Log.d(TAG, "onDraw: total" + getMeasuredWidth());
            this.mMarginRect.set(measuredWidth, 0, measuredWidth - ((int) (((float) this.mMargin) + 0.0f)), getHeight());
            canvas.drawRect(this.mMarginRect, this.mMarginPaint);
            Log.d(TAG, "onDraw: right margin:0.0 - " + (this.mMargin + 0.0f));
            float offset = ((float) this.mMargin) + 0.0f;
            int columnIndex = 0;
            while (columnIndex < this.mColumnCount) {
                this.mColumnRect.set(measuredWidth - ((int) offset), 0, measuredWidth - ((int) (this.mColumnWidth[columnIndex] + offset)), getHeight());
                canvas.drawRect(this.mColumnRect, this.mColumnPaint);
                Log.d(TAG, "onDraw: column:" + offset + " - " + (this.mColumnWidth[columnIndex] + offset));
                if (columnIndex != this.mColumnCount - 1) {
                    Log.d(TAG, "onDraw: gap:" + (this.mColumnWidth[columnIndex] + offset) + " - " + (this.mColumnWidth[columnIndex] + offset + this.mGutter));
                }
                offset += this.mColumnWidth[columnIndex] + (columnIndex == this.mColumnCount + (-1) ? 0 : this.mGutter);
                columnIndex++;
            }
            this.mMarginRect.set(measuredWidth - ((int) offset), 0, measuredWidth - ((int) (this.mMargin + offset)), getHeight());
            canvas.drawRect(this.mMarginRect, this.mMarginPaint);
            Log.d(TAG, "onDraw: left margin:" + offset + " - " + (this.mMargin + offset));
            return;
        }
        Log.d(TAG, "onDraw: total width: " + getMeasuredWidth());
        this.mMarginRect.set(0, 0, (int) (((float) this.mMargin) + 0.0f), getHeight());
        canvas.drawRect(this.mMarginRect, this.mMarginPaint);
        Log.d(TAG, "onDraw: left margin: 0.0 - " + (this.mMargin + 0.0f) + " width: " + this.mMargin);
        float offset = ((float) this.mMargin) + 0.0f;
        int columnIndex = 0;
        while (columnIndex < this.mColumnCount) {
            this.mColumnRect.set((int) offset, 0, (int) (this.mColumnWidth[columnIndex] + offset), getHeight());
            canvas.drawRect(this.mColumnRect, this.mColumnPaint);
            Log.d(TAG, "onDraw: column " + columnIndex + " :" + offset + " - " + (this.mColumnWidth[columnIndex] + offset) + " width: " + this.mColumnWidth[columnIndex]);
            if (columnIndex != this.mColumnCount - 1) {
                Log.d(TAG, "onDraw: gap " + columnIndex + " :" + (this.mColumnWidth[columnIndex] + offset) + " - " + (this.mColumnWidth[columnIndex] + offset + this.mGutter) + " width: " + this.mGutter);
            }
            offset += this.mColumnWidth[columnIndex] + (columnIndex == this.mColumnCount + (-1) ? 0 : this.mGutter);
            columnIndex++;
        }
        this.mMarginRect.set((int) offset, 0, (int) (this.mMargin + offset), getHeight());
        canvas.drawRect(this.mMarginRect, this.mMarginPaint);
        Log.d(TAG, "onDraw: right margin:" + offset + " - " + (this.mMargin + offset) + " width:" + this.mMargin);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        this.mResponsiveUIModel.rebuild(getMeasuredWidth(), getMeasuredHeight());
        requestLatestGridParams();
    }

    public void setMarginType(MarginType marginType) {
        this.mMarginType = marginType;
    }

    public COUIResponsiveGridMaskView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mColumnCount = 0;
        this.mGutter = 0;
        this.mMargin = 0;
        this.mMarginType = MarginType.MARGIN_SMALL;
        this.mMarginRect = new Rect();
        this.mColumnRect = new Rect();
        this.mMarginPaint = new Paint();
        this.mColumnPaint = new Paint();
        init(context);
    }

    public COUIResponsiveGridMaskView(Context context, AttributeSet attributeSet, int defStyleAttr) {
        super(context, attributeSet, defStyleAttr);
        this.mColumnCount = 0;
        this.mGutter = 0;
        this.mMargin = 0;
        this.mMarginType = MarginType.MARGIN_SMALL;
        this.mMarginRect = new Rect();
        this.mColumnRect = new Rect();
        this.mMarginPaint = new Paint();
        this.mColumnPaint = new Paint();
        init(context);
    }

    public COUIResponsiveGridMaskView(Context context, AttributeSet attributeSet, int defStyleAttr, int defStyleRes) {
        super(context, attributeSet, defStyleAttr, defStyleRes);
        this.mColumnCount = 0;
        this.mGutter = 0;
        this.mMargin = 0;
        this.mMarginType = MarginType.MARGIN_SMALL;
        this.mMarginRect = new Rect();
        this.mColumnRect = new Rect();
        this.mMarginPaint = new Paint();
        this.mColumnPaint = new Paint();
        init(context);
    }
}
