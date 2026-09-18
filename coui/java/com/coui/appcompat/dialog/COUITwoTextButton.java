package com.coui.appcompat.dialog;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.coui.appcompat.button.COUIButton;

class COUITwoTextButton extends COUIButton {
    private static final int MAX_ALPHA = 255;

    private String mStartText = "";
    private String mNextText = "";
    private float mStartTextAlpha;
    private float mNextTextAlpha;
    private TextPaint mStartTextPaint;
    private TextPaint mNextTextPaint;

    public COUITwoTextButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void drawText(Canvas canvas, String text, TextPaint textPaint, float alpha) {
        int width = getWidth();
        String ellipsized = (String) TextUtils.ellipsize(text, textPaint,
                (width - getPaddingStart()) - getPaddingEnd(), TextUtils.TruncateAt.END);
        float x = ((width - textPaint.measureText(ellipsized)) / 2.0f)
                - ((getPaddingEnd() - getPaddingStart()) / 2.0f);
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        float y = (((getMeasuredHeight() + (metrics.bottom - metrics.top)) / 2.0f) - metrics.bottom)
                - ((getPaddingBottom() - getPaddingTop()) / 2.0f);
        textPaint.setAlpha((int) (alpha * MAX_ALPHA));
        canvas.drawText(ellipsized, x, y, textPaint);
    }

    public float getStartTextAlpha() {
        return mStartTextAlpha;
    }

    public float getNextTextAlpha() {
        return mNextTextAlpha;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mStartTextPaint == null) {
            mStartTextPaint = new TextPaint(getPaint());
        }
        if (mNextTextPaint == null) {
            mNextTextPaint = new TextPaint(getPaint());
        }
        int save = canvas.save();
        canvas.translate(getScrollX(), getScrollY());
        drawText(canvas, mStartText, mStartTextPaint, mStartTextAlpha);
        drawText(canvas, mNextText, mNextTextPaint, mNextTextAlpha);
        canvas.restoreToCount(save);
    }

    public void setStartText(CharSequence text) {
        mStartText = text == null ? "" : text.toString();
    }

    public void setNextText(CharSequence text) {
        mNextText = text == null ? "" : text.toString();
    }

    public void setStartTextAlpha(float alpha) {
        mStartTextAlpha = alpha;
    }

    public void setNextTextAlpha(float alpha) {
        mNextTextAlpha = alpha;
    }
}
