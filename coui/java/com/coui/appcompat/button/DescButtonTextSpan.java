package com.coui.appcompat.button;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ReplacementSpan;
import com.coui.appcompat.R;
import com.coui.appcompat.textutil.COUIChangeTextUtil;


public class DescButtonTextSpan extends ReplacementSpan {
    private Context mContext;
    private TextPaint mDescPaint;
    private String mDescText;
    private int mDescTextWidth;
    private int mHeight;
    private boolean mIsRtl;
    private int mMaxWidth;
    private String mText;
    private int mTextColor;
    private TextPaint mTextPaint;
    private int mTextWidth;
    private int mTopMargin;
    private int mWidth;

    public DescButtonTextSpan(Context context, String text, String descText, int maxWidth, int width, int height, int textColor, Paint paint, boolean isRtl) {
        this.mText = text;
        this.mDescText = descText;
        this.mMaxWidth = maxWidth;
        this.mWidth = width < 0 ? 0 : width;
        this.mContext = context;
        this.mHeight = height;
        this.mTextColor = textColor;
        this.mIsRtl = isRtl;
        this.mTextPaint = new TextPaint(paint);
        initPaint();
        handleSubString();
    }

    private int getEmptyWidth() {
        return ((int) this.mTextPaint.measureText(" ")) / 2;
    }

    private int getStartText() {
        return Math.abs(this.mTextWidth - this.mDescTextWidth) / 2;
    }

    private String getSubStringByWidth(String text, int maxWidth, TextPaint textPaint) {
        return (TextUtils.isEmpty(text) || maxWidth < 0) ? "" : StaticLayout.Builder.obtain(text, 0, text.length(), textPaint, maxWidth).setMaxLines(1).setEllipsize(TextUtils.TruncateAt.END).build().getText().toString();
    }

    private int getWidth() {
        if (TextUtils.isEmpty(this.mDescText) || TextUtils.isEmpty(this.mText)) {
            return 0;
        }
        return Math.max(this.mDescTextWidth, this.mTextWidth);
    }

    private void handleSubString() {
        int descTextWidth = this.mDescTextWidth;
        int maxWidth = this.mMaxWidth;
        if (descTextWidth > maxWidth) {
            String subStringByWidth = getSubStringByWidth(this.mDescText, maxWidth, this.mDescPaint);
            this.mDescText = subStringByWidth;
            this.mDescTextWidth = (int) this.mDescPaint.measureText(subStringByWidth);
        }
        int textWidth = this.mTextWidth;
        int maxTextWidth = this.mMaxWidth;
        if (textWidth > maxTextWidth) {
            String subStringByWidth2 = getSubStringByWidth(this.mText, maxTextWidth, this.mTextPaint);
            this.mText = subStringByWidth2;
            this.mTextWidth = (int) this.mTextPaint.measureText(subStringByWidth2);
        }
    }

    private void initPaint() {
        float fontScale = this.mContext.getResources().getConfiguration().fontScale;
        int textSize = this.mContext.getResources().getDimensionPixelSize(R.dimen.coui_btn_desc_text_size);
        int descTextSize = this.mContext.getResources().getDimensionPixelSize(R.dimen.coui_btn_desc_sub_text_size);
        int suitableFontSize = (int) COUIChangeTextUtil.getSuitableFontSize(textSize, fontScale, 2);
        int suitableDescFontSize = (int) COUIChangeTextUtil.getSuitableFontSize(descTextSize, fontScale, 2);
        this.mTextPaint.setTextSize(suitableFontSize);
        this.mTextPaint.setColor(this.mTextColor);
        TextPaint textPaint = new TextPaint(this.mTextPaint);
        this.mDescPaint = textPaint;
        textPaint.setTextSize(suitableDescFontSize);
        this.mDescPaint.setColor(this.mTextColor);
        this.mDescTextWidth = (int) this.mDescPaint.measureText(this.mDescText);
        this.mTextWidth = (int) this.mTextPaint.measureText(this.mText);
        this.mTopMargin = this.mContext.getResources().getDimensionPixelSize(R.dimen.coui_btn_desc_top_margin);
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        Paint.FontMetricsInt descFontMetrics = this.mDescPaint.getFontMetricsInt();
        Paint.FontMetricsInt textFontMetrics = this.mTextPaint.getFontMetricsInt();
        int descDescent = descFontMetrics.descent;
        int descAscent = descFontMetrics.ascent;
        int descLeading = descFontMetrics.leading;
        int textBaseline = y - ((((descDescent - descAscent) + descLeading) + this.mTopMargin) / 2);
        int descBaseline = textFontMetrics.bottom + textBaseline + descLeading + Math.abs(descAscent) + this.mTopMargin;
        int emptyWidth = getEmptyWidth();
        int startText = getStartText();
        if (this.mIsRtl) {
            emptyWidth = -emptyWidth;
        }
        float drawX = x - emptyWidth;
        if (this.mTextWidth > this.mDescTextWidth) {
            canvas.drawText(this.mText, drawX, textBaseline, this.mTextPaint);
            canvas.drawText(this.mDescText, drawX + startText, descBaseline, this.mDescPaint);
        } else {
            canvas.drawText(this.mText, startText + drawX, textBaseline, this.mTextPaint);
            canvas.drawText(this.mDescText, drawX, descBaseline, this.mDescPaint);
        }
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fontMetricsInt) {
        return getWidth();
    }
}
