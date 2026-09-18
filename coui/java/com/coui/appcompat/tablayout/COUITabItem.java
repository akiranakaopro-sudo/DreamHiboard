package com.coui.appcompat.tablayout;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import androidx.appcompat.widget.TintTypedArray;
import com.coui.appcompat.R;


public final class COUITabItem extends View {
    final int mCustomLayout;
    final Drawable mIcon;
    final CharSequence mText;

    public COUITabItem(Context context) {
        this(context, null);
    }

    public COUITabItem(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        TintTypedArray array = TintTypedArray.obtainStyledAttributes(context, attributeSet, R.styleable.COUITabItem);
        this.mText = array.getText(R.styleable.COUITabItem_android_text);
        this.mIcon = array.getDrawable(R.styleable.COUITabItem_android_icon);
        this.mCustomLayout = array.getResourceId(R.styleable.COUITabItem_android_layout, 0);
        array.recycle();
    }
}






