package com.coui.appcompat.tintimageview;

import android.R;
import android.content.Context;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.TintTypedArray;

public class COUITintImageView extends AppCompatImageView {
    private static final int[] TINT_ATTRS = {R.attr.background, R.attr.src};
    private final COUITintManager mTintManager;

    public COUITintImageView(Context context) {
        this(context, null);
    }

    @Override
    public void setImageResource(int resId) {
        setImageDrawable(this.mTintManager.getDrawable(resId));
    }

    public COUITintImageView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public COUITintImageView(Context context, AttributeSet attributeSet, int defStyleAttr) {
        super(context, attributeSet, defStyleAttr);
        TintTypedArray tintTypedArray = TintTypedArray.obtainStyledAttributes(getContext(), attributeSet, TINT_ATTRS, defStyleAttr, 0);
        if (tintTypedArray.length() > 0) {
            if (tintTypedArray.hasValue(0)) {
                setBackgroundDrawable(tintTypedArray.getDrawable(0));
            }
            if (tintTypedArray.hasValue(1)) {
                setImageDrawable(tintTypedArray.getDrawable(1));
            }
        }
        tintTypedArray.recycle();
        this.mTintManager = COUITintManager.get(context);
    }
}
