package com.coui.appcompat.card;

import android.content.Context;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.preference.PreferenceViewHolder;

import com.coui.appcompat.R;
import com.coui.appcompat.darkmode.COUIDarkModeUtil;

public final class COUICardButtonPreference extends COUIPressFeedbackJumpPreference {
    public static final Companion Companion = new Companion();
    public static final float CARD_BUTTON_TEXT_SIZE_NORMAL = 12.0f;
    public static final float CARD_BUTTON_TEXT_SIZE_SMALL = 10.0f;

    public COUICardButtonPreference(Context context) {
        this(context, null);
    }

    public COUICardButtonPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.couiJumpPreferenceStyle);
    }

    public COUICardButtonPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public COUICardButtonPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setLayoutResource(R.layout.coui_component_card_button_preference);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        COUIDarkModeUtil.setForceDarkAllow(holder.itemView, false);
        TextView title = (TextView) holder.findViewById(android.R.id.title);
        title.post(() -> title.setTextSize(
                isOverFlowed(title) ? CARD_BUTTON_TEXT_SIZE_SMALL : CARD_BUTTON_TEXT_SIZE_NORMAL));
    }

    private int getAvailableWidth(TextView textView) {
        return textView.getWidth() - textView.getPaddingLeft() - textView.getPaddingRight();
    }

    private boolean isOverFlowed(TextView textView) {
        TextPaint paint = textView.getPaint();
        return paint.measureText(textView.getText().toString()) > getAvailableWidth(textView);
    }

    public static final class Companion {
        private Companion() {
        }
    }
}
