package com.coui.appcompat.textviewcompatutil;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import com.coui.appcompat.R;
import com.coui.appcompat.contextutil.COUIContextUtil;
import com.coui.appcompat.statelistutil.COUIStateListUtil;

public class COUITextPressRippleDrawable extends RippleDrawable {
    private static final int TRANSPARENT = Color.parseColor("#00000000");
    public static final int U = 34;

    public COUITextPressRippleDrawable(Context context) {
        this(context, false);
    }

    private void initPadding(Context context) {
        int dimensionPixelOffset = context.getResources().getDimensionPixelOffset(R.dimen.text_ripple_bg_padding_horizontal);
        int dimensionPixelOffset2 = context.getResources().getDimensionPixelOffset(R.dimen.text_ripple_bg_padding_vertical);
        setPadding(dimensionPixelOffset, dimensionPixelOffset2, dimensionPixelOffset, dimensionPixelOffset2);
    }

    private static int parseThemeColor() {
        return Build.VERSION.SDK_INT >= U ? R.attr.couiColorPressBackground : R.attr.couiColorRipplePressBackground;
    }

    public COUITextPressRippleDrawable(Context context, boolean skipPadding) {
        super(COUIStateListUtil.createColorStateList(COUIContextUtil.getAttrColor(context, parseThemeColor()), TRANSPARENT), new ColorDrawable(TRANSPARENT), new COUITextPressMaskDrawable());
        if (skipPadding) {
            return;
        }
        initPadding(context);
    }
}
