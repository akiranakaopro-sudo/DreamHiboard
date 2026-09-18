package com.coui.appcompat.seekbar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;

import com.coui.appcompat.R;
import com.coui.appcompat.contextutil.COUIContextUtil;
import com.coui.appcompat.textutil.COUIChangeTextUtil;

public class TextDrawable extends ShapeDrawable {
    private static final int WIDTH = 144;

    private final Context mContext;
    private final int mFontSize;
    private final int mTextHeight;
    private final int mMarginBottom;
    private final int mPaddingEnd;
    private final Paint mTextPaint;

    private String mText = "";

    public TextDrawable(Context context) {
        super(new RectShape());

        mContext = context;

        mFontSize = context.getResources().getDimensionPixelOffset(
                R.dimen.coui_seekbar_popup_text_size_small
        );
        mTextHeight = context.getResources().getDimensionPixelOffset(
                R.dimen.coui_seekbar_popup_text_height
        );
        mMarginBottom = context.getResources().getDimensionPixelOffset(
                R.dimen.coui_seekbar_popup_text_margin_bottom
        );
        mPaddingEnd = context.getResources().getDimensionPixelOffset(
                R.dimen.coui_seekbar_popup_text_padding_end
        );

        mTextPaint = new Paint();
        mTextPaint.setColor(COUIContextUtil.getAttrColor(context, R.attr.couiColorPrimaryNeutral));
        mTextPaint.setAntiAlias(true);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTypeface(Typeface.create(COUIChangeTextUtil.MEDIUM_FONT, 0));
        mTextPaint.setTextAlign(isLayoutRtl() ? Paint.Align.LEFT : Paint.Align.RIGHT);
        mTextPaint.setStrokeWidth(0.0f);

        getPaint().setColor(0);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        Rect bounds = getBounds();
        int saveCount = canvas.save();

        canvas.translate(bounds.left, bounds.top);
        mTextPaint.setTextSize(mFontSize);

        float x = isLayoutRtl() ? mPaddingEnd : WIDTH - mPaddingEnd;
        canvas.drawText(mText, x, mFontSize, mTextPaint);

        canvas.restoreToCount(saveCount);
    }

    @Override
    public int getIntrinsicHeight() {
        return mTextHeight + mMarginBottom;
    }

    @Override
    public int getIntrinsicWidth() {
        return WIDTH;
    }

    public boolean isLayoutRtl() {
        return mContext.getResources().getConfiguration().getLayoutDirection() == 1;
    }

    public void setText(String text) {
        mText = text;
    }
}