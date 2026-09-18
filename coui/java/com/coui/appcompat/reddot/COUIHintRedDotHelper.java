package com.coui.appcompat.reddot;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;

import androidx.core.content.ContextCompat;

import com.coui.appcompat.R;
import com.coui.appcompat.roundRect.COUIRoundRectUtil;
import com.coui.appcompat.textutil.COUIChangeTextUtil;

public class COUIHintRedDotHelper {
    private int mBgColor;
    private final Paint mBgPaint;
    private int mCornerRadius;
    private int mDotDiameter;
    private int mEllipsisDiameter;
    private int mEllipsisSpacing;
    private int mLargeWidth;
    private int mMediumWidth;
    private int mNaviSmallWidth;
    private int mSmallWidth;
    private int mStrokeColor;
    private final Paint mStrokePaint;
    private int mStrokeWidth;
    private int mTextColor;
    private final TextPaint mTextPaint;
    private int mTextSize;
    private int mViewHeight;

    public COUIHintRedDotHelper(Context context, AttributeSet attrs, int[] styleable, int defStyleAttr, int defStyleRes) {
        TypedArray a = context.obtainStyledAttributes(attrs, styleable, defStyleAttr, defStyleRes);
        mBgColor = a.getColor(R.styleable.COUIHintRedDot_couiHintRedDotColor, 0);
        mTextColor = a.getColor(R.styleable.COUIHintRedDot_couiHintRedDotTextColor, 0);
        mTextSize = a.getDimensionPixelSize(R.styleable.COUIHintRedDot_couiHintTextSize, 0);
        mSmallWidth = a.getDimensionPixelSize(R.styleable.COUIHintRedDot_couiSmallWidth, 0);
        mMediumWidth = a.getDimensionPixelSize(R.styleable.COUIHintRedDot_couiMediumWidth, 0);
        mLargeWidth = a.getDimensionPixelSize(R.styleable.COUIHintRedDot_couiLargeWidth, 0);
        mViewHeight = a.getDimensionPixelSize(R.styleable.COUIHintRedDot_couiHeight, 0);
        mCornerRadius = a.getDimensionPixelSize(R.styleable.COUIHintRedDot_couiCornerRadius, 0);
        mDotDiameter = a.getDimensionPixelSize(R.styleable.COUIHintRedDot_couiDotDiameter, 0);
        mEllipsisDiameter = a.getDimensionPixelSize(R.styleable.COUIHintRedDot_couiEllipsisDiameter, 0);
        a.recycle();
        mNaviSmallWidth = context.getResources().getDimensionPixelSize(R.dimen.coui_hint_red_dot_navi_small_width);
        mEllipsisSpacing = context.getResources().getDimensionPixelSize(R.dimen.coui_hint_red_dot_ellipsis_spacing);
        mStrokeWidth = context.getResources().getDimensionPixelSize(R.dimen.coui_dot_stroke_width);
        mStrokeColor = ContextCompat.getColor(context, R.color.coui_color_white);
        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setTypeface(Typeface.create(COUIChangeTextUtil.MEDIUM_FONT, Typeface.NORMAL));
        mBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBgPaint.setColor(mBgColor);
        mBgPaint.setStyle(Paint.Style.FILL);
        mStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mStrokePaint.setColor(mStrokeColor);
        mStrokePaint.setStyle(Paint.Style.FILL);
    }

    private void drawNumber(Canvas canvas, int number, int alpha, RectF rect, boolean fade) {
        if (number <= 0) {
            return;
        }
        if (fade) {
            mTextPaint.setAlpha(Math.max(0, Math.min(255, alpha)));
        }
        if (number < 1000) {
            String text = String.valueOf(number);
            Paint.FontMetricsInt metrics = mTextPaint.getFontMetricsInt();
            int textWidth = (int) mTextPaint.measureText(text);
            canvas.drawText(text, rect.left + (((rect.right - rect.left) - textWidth) / 2.0f),
                    (((rect.top + rect.bottom) - metrics.ascent) - metrics.descent) / 2.0f, mTextPaint);
        } else {
            drawEllipsis(canvas, rect);
        }
        mTextPaint.setColor(mTextColor);
        mTextPaint.setAlpha(255);
    }

    private void drawEllipsis(Canvas canvas, RectF rect) {
        float centerX = (rect.left + rect.right) / 2.0f;
        float centerY = (rect.top + rect.bottom) / 2.0f;
        for (int i = -1; i <= 1; i++) {
            canvas.drawCircle(((mEllipsisSpacing + mEllipsisDiameter) * i) + centerX,
                    centerY, mEllipsisDiameter / 2.0f, mTextPaint);
        }
    }

    private void drawPointOnly(Canvas canvas, RectF rect) {
        float radius = (rect.bottom - rect.top) / 2.0f;
        canvas.drawCircle(rect.left + radius, rect.top + radius, radius, mBgPaint);
    }

    private void drawPointStroke(Canvas canvas, RectF rect) {
        float radius = (rect.bottom - rect.top) / 2.0f;
        canvas.drawCircle(rect.left + radius, rect.top + radius, radius - mStrokeWidth, mBgPaint);
    }

    private void drawPointWithNumber(Canvas canvas, Object number, RectF rect) {
        boolean string = number instanceof String;
        if (string) {
            if (TextUtils.isEmpty((CharSequence) number)) {
                return;
            }
        } else {
            if (!(number instanceof Integer)) {
                throw new IllegalArgumentException("params 'number' must be String or Integer!");
            }
            if (((Integer) number) <= 0) {
                return;
            }
        }
        Path path = Math.min(rect.right - rect.left, rect.bottom - rect.top) < mCornerRadius * 2
                ? COUIRoundRectUtil.getInstance().getPath(rect, ((int) Math.min(rect.right - rect.left, rect.bottom - rect.top)) / 2)
                : COUIRoundRectUtil.getInstance().getPath(rect, mCornerRadius);
        canvas.drawPath(path, mBgPaint);
        if (string) {
            drawText(canvas, (String) number, rect);
        } else {
            drawNumber(canvas, (Integer) number, 255, rect, false);
        }
    }

    private void drawPointWithStroke(Canvas canvas, Object number, RectF rect) {
        boolean string = number instanceof String;
        if (string) {
            if (TextUtils.isEmpty((CharSequence) number)) {
                return;
            }
        } else {
            if (!(number instanceof Integer)) {
                throw new IllegalArgumentException("params 'number' must be String or Integer!");
            }
            if (((Integer) number) <= 0) {
                return;
            }
        }
        RectF inner = new RectF(0.0f, 0.0f, rect.right - (mStrokeWidth * 2), rect.bottom - (mStrokeWidth * 2));
        int radius = ((int) Math.min(inner.right, inner.bottom)) / 2;
        canvas.drawPath(COUIRoundRectUtil.getInstance().getPath(rect, mCornerRadius), mStrokePaint);
        canvas.save();
        canvas.translate(mStrokeWidth, mStrokeWidth);
        canvas.drawPath(COUIRoundRectUtil.getInstance().getPath(inner, radius), mBgPaint);
        canvas.restore();
        if (string) {
            drawText(canvas, (String) number, rect);
        } else {
            drawNumber(canvas, (Integer) number, 255, rect, false);
        }
    }

    private void drawText(Canvas canvas, String text, RectF rect) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        float textWidth = mTextPaint.measureText(text);
        if (textWidth < mTextPaint.measureText(String.valueOf(1000))) {
            Paint.FontMetricsInt metrics = mTextPaint.getFontMetricsInt();
            canvas.drawText(text, rect.left + (((rect.right - rect.left) - textWidth) / 2.0f),
                    (((rect.top + rect.bottom) - metrics.ascent) - metrics.descent) / 2.0f, mTextPaint);
        } else {
            drawEllipsis(canvas, rect);
        }
    }

    private int getBgHeight() {
        return mViewHeight;
    }

    private int getBgWidth(int number) {
        if (number < 10) {
            return Math.max(mSmallWidth, mViewHeight);
        }
        if (number < 100) {
            return Math.max(mMediumWidth, mViewHeight);
        }
        if (number < 1000) {
            return Math.max(mLargeWidth, mViewHeight);
        }
        return Math.max(mMediumWidth, mViewHeight);
    }

    private int getNaviBgWidth(int number) {
        return number < 10 ? mNaviSmallWidth : number < 100 ? mSmallWidth : mMediumWidth;
    }

    private boolean isNumeric(String text) {
        if (TextUtils.isEmpty(text)) {
            return false;
        }
        for (int i = text.length() - 1; i >= 0; i--) {
            if (!Character.isDigit(text.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public void drawPointWithFadeNumber(Canvas canvas, int oldNumber, int oldAlpha, int newNumber, int newAlpha, RectF rect) {
        canvas.drawPath(COUIRoundRectUtil.getInstance().getPath(rect, mCornerRadius), mBgPaint);
        if (oldAlpha > newAlpha) {
            drawNumber(canvas, oldNumber, oldAlpha, rect, true);
            drawNumber(canvas, newNumber, newAlpha, rect, true);
        } else {
            drawNumber(canvas, newNumber, newAlpha, rect, true);
            drawNumber(canvas, oldNumber, oldAlpha, rect, true);
        }
    }

    public void drawRedPoint(Canvas canvas, int mode, Object value, RectF rect) {
        if (mode == COUIHintRedDot.POINT_ONLY_MODE) {
            drawPointOnly(canvas, rect);
        } else if (mode == COUIHintRedDot.POINT_WITH_NUM_MODE || mode == COUIHintRedDot.POINT_NAVI_WITH_NUM) {
            drawPointWithNumber(canvas, value, rect);
        } else if (mode == COUIHintRedDot.POINT_ONLY_MODE_STROKE) {
            drawPointStroke(canvas, rect);
        } else if (mode == COUIHintRedDot.POINT_NUM_MODE_STROKE) {
            drawPointWithStroke(canvas, value, rect);
        }
    }

    public int getViewHeight(int mode) {
        if (mode == COUIHintRedDot.POINT_ONLY_MODE) {
            return mDotDiameter;
        }
        if (mode == COUIHintRedDot.POINT_WITH_NUM_MODE || mode == COUIHintRedDot.POINT_NAVI_WITH_NUM
                || mode == COUIHintRedDot.POINT_ONLY_MODE_STROKE || mode == COUIHintRedDot.POINT_NUM_MODE_STROKE) {
            return getBgHeight();
        }
        return 0;
    }

    public int getViewWidth(int mode, int number) {
        if (mode == COUIHintRedDot.POINT_ONLY_MODE) {
            return mDotDiameter;
        }
        if (mode == COUIHintRedDot.POINT_NAVI_WITH_NUM) {
            return getNaviBgWidth(number);
        }
        if (mode == COUIHintRedDot.POINT_WITH_NUM_MODE || mode == COUIHintRedDot.POINT_ONLY_MODE_STROKE
                || mode == COUIHintRedDot.POINT_NUM_MODE_STROKE) {
            return getBgWidth(number);
        }
        return 0;
    }

    public int getViewWidth(int mode, String text) {
        if (mode == COUIHintRedDot.POINT_ONLY_MODE) {
            return mDotDiameter;
        }
        if (mode == COUIHintRedDot.POINT_NAVI_WITH_NUM) {
            return getNaviBgWidth(text);
        }
        if (mode == COUIHintRedDot.POINT_WITH_NUM_MODE || mode == COUIHintRedDot.POINT_ONLY_MODE_STROKE
                || mode == COUIHintRedDot.POINT_NUM_MODE_STROKE) {
            return getBgWidth(text);
        }
        return 0;
    }

    private int getNaviBgWidth(String text) {
        float textWidth = (int) mTextPaint.measureText(text);
        if (textWidth < mTextPaint.measureText(String.valueOf(10))) {
            return mNaviSmallWidth;
        }
        if (textWidth >= mTextPaint.measureText(String.valueOf(100)) && textWidth < mTextPaint.measureText(String.valueOf(1000))) {
            return mLargeWidth;
        }
        return mMediumWidth;
    }

    private int getBgWidth(String text) {
        if (TextUtils.isEmpty(text)) {
            return mSmallWidth;
        }
        if (isNumeric(text)) {
            return getBgWidth(Integer.parseInt(text));
        }
        float textWidth = (int) mTextPaint.measureText(text);
        if (textWidth < mTextPaint.measureText(String.valueOf(10))) {
            return Math.max(mSmallWidth, mViewHeight);
        }
        if (textWidth < mTextPaint.measureText(String.valueOf(100))) {
            return Math.max(mMediumWidth, mViewHeight);
        }
        if (textWidth < mTextPaint.measureText(String.valueOf(1000))) {
            return Math.max(mLargeWidth, mViewHeight);
        }
        return Math.max(mMediumWidth, mViewHeight);
    }

    public void setBgColor(int color) { mBgColor = color; mBgPaint.setColor(color); }
    public void setCornerRadius(int radius) { mCornerRadius = radius; }
    public void setDotDiameter(int diameter) { mDotDiameter = diameter; }
    public void setEllipsisDiameter(int diameter) { mEllipsisDiameter = diameter; }
    public void setLargeWidth(int width) { mLargeWidth = width; }
    public void setMediumWidth(int width) { mMediumWidth = width; }
    public void setSmallWidth(int width) { mSmallWidth = width; }
    public void setTextColor(int color) { mTextColor = color; mTextPaint.setColor(color); }
    public void setTextSize(int size) { mTextSize = size; mTextPaint.setTextSize(size); }
    public void setViewHeight(int height) { mViewHeight = height; setCornerRadius(height / 2); }
}
