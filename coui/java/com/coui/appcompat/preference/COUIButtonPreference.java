package com.coui.appcompat.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import androidx.preference.PreferenceViewHolder;

import com.coui.appcompat.R;
import com.coui.appcompat.button.COUIButton;

public class COUIButtonPreference extends COUIPreference {
    private int mDrawableColor;
    private View.OnClickListener mListener;
    private OnButtonClickListener mOnButtonClickListener;
    private CharSequence mText;
    private int mTextColor;
    private int mTextSize;

    public interface OnButtonClickListener {
        void onButtonClick();
    }

    public COUIButtonPreference(Context context) {
        this(context, null);
    }

    public COUIButtonPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.couiButtonPreferenceStyle);
    }

    public COUIButtonPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Preference_COUI_COUIButtonPreference);
    }

    public COUIButtonPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mListener = view -> {
            if (mOnButtonClickListener != null) {
                mOnButtonClickListener.onButtonClick();
            }
        };
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.COUIButtonPreference, defStyleAttr, defStyleRes);
        mText = a.getText(R.styleable.COUIButtonPreference_btnText);
        mTextSize = a.getInt(R.styleable.COUIButtonPreference_btnTextSize, 14);
        mTextColor = a.getColor(R.styleable.COUIButtonPreference_btnTextColor, 0);
        mDrawableColor = a.getColor(R.styleable.COUIButtonPreference_btnDrawableColor, 0);
        a.recycle();
    }

    public CharSequence getBtnText() {
        return mText;
    }

    public int getDrawableColor() {
        return mDrawableColor;
    }

    public int getTextColor() {
        return mTextColor;
    }

    public int getTextSize() {
        return mTextSize;
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        COUIButton button = (COUIButton) holder.findViewById(R.id.coui_btn);
        if (button != null) {
            button.setText(getBtnText());
            button.setTextSize(getTextSize());
            if (getTextColor() != 0) {
                button.setTextColor(getTextColor());
            }
            if (getDrawableColor() != 0) {
                button.setDrawableColor(getDrawableColor());
            }
            button.setOnClickListener(mListener);
        }
    }

    public void setBtnText(CharSequence text) {
        if (!TextUtils.equals(text, mText)) {
            mText = text;
            notifyChanged();
        }
    }

    public void setDrawableColor(int color) {
        if (mDrawableColor != color) {
            mDrawableColor = color;
            notifyChanged();
        }
    }

    public void setOnButtonClickListener(OnButtonClickListener listener) {
        mOnButtonClickListener = listener;
    }

    public void setTextColor(int color) {
        if (mTextColor != color) {
            mTextColor = color;
            notifyChanged();
        }
    }

    public void setTextSize(int textSize) {
        if (mTextSize != textSize) {
            mTextSize = textSize;
            notifyChanged();
        }
    }
}
