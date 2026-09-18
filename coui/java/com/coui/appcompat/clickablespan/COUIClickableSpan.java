package com.coui.appcompat.clickablespan;

import android.content.Context;
import android.content.res.ColorStateList;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.coui.appcompat.R;

public class COUIClickableSpan extends ClickableSpan {
    private SpannableStrClickListener mClickReference;
    private final Context mContext;
    private ColorStateList mTextColor;

    public interface SpannableStrClickListener {
        void onClick();
    }

    public COUIClickableSpan(Context context) {
        mContext = context;
    }

    @Override
    public void onClick(View view) {
        if (mClickReference != null) {
            mClickReference.onClick();
        }
    }

    public void setClickTextColor(ColorStateList colorStateList) {
        if (colorStateList != null) {
            mTextColor = colorStateList;
        }
    }

    public void setStatusBarClickListener(SpannableStrClickListener listener) {
        mClickReference = listener;
    }

    @Override
    public void updateDrawState(TextPaint textPaint) {
        mTextColor = ContextCompat.getColorStateList(mContext, R.color.coui_clickable_text_color);
        if (mTextColor != null) {
            textPaint.setColor(mTextColor.getColorForState(textPaint.drawableState, 0));
        }
    }
}
