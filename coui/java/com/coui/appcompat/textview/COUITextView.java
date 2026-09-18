package com.coui.appcompat.textview;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;

import androidx.appcompat.widget.AppCompatTextView;

public class COUITextView extends AppCompatTextView {
    private static final String TAG = "COUITextViewDebug";

    private final Context mContext;
    private boolean mDebug;

    public COUITextView(Context context) {
        this(context, null);
    }

    public COUITextView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle);
    }

    public COUITextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mDebug = false;
        mContext = context;
        if ("debug".equals(getTag())) {
            setDebug(Boolean.TRUE);
        }
        int appearance = findViewAppearanceResourceId(context.getTheme(), attrs, defStyleAttr, -1);
        if (!viewAttrsHasAddLineSpace(context.getTheme(), attrs, defStyleAttr, -1) && appearance != -1) {
            applyLineSpaceFromViewAppearance(context.getTheme(), appearance);
        }
        if (mDebug) {
            int fontHeight = getPaint().getFontMetricsInt(null);
            Log.i(TAG, "textSize: " + getTextSize() + ", lineHeight: " + getLineHeight()
                    + ", fontHeight: " + fontHeight + ", Multiplier: " + getLineSpacingMultiplier());
        }
    }

    private void applyLineSpaceFromViewAppearance(Resources.Theme theme, int resId) {
        TypedArray a = theme.obtainStyledAttributes(resId, new int[]{android.R.attr.textAppearance, android.R.attr.lineSpacingMultiplier});
        float multiplier = a.getFloat(1, 1.0f);
        if (multiplier >= 1.0f) {
            setLineSpacing(0.0f, multiplier);
        }
        a.recycle();
    }

    private static int findViewAppearanceResourceId(Resources.Theme theme, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray a = theme.obtainStyledAttributes(attrs, new int[]{android.R.attr.textAppearance, android.R.attr.lineSpacingMultiplier}, defStyleAttr, defStyleRes);
        int resId = a.getResourceId(0, -1);
        a.recycle();
        return resId;
    }

    private static boolean viewAttrsHasAddLineSpace(Resources.Theme theme, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray a = theme.obtainStyledAttributes(attrs, new int[]{android.R.attr.textAppearance, android.R.attr.lineSpacingMultiplier}, defStyleAttr, defStyleRes);
        float multiplier = a.getFloat(1, 1.0f);
        a.recycle();
        return multiplier != 1.0f;
    }

    public void setDebug(Boolean debug) {
        mDebug = debug.booleanValue();
    }

    @Override
    public void setTextAppearance(int resId) {
        super.setTextAppearance(resId);
        applyLineSpaceFromViewAppearance(mContext.getTheme(), resId);
    }
}
