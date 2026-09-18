package com.coui.appcompat.rippleutil;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;

import com.coui.appcompat.R;
import com.coui.appcompat.contextutil.COUIContextUtil;
import com.coui.appcompat.statelistutil.COUIStateListUtil;

public class COUIPressRippleDrawable extends RippleDrawable {
    private static final int TRANSPARENT = Color.parseColor("#00000000");
    public static final int U = 34;

    public COUIPressRippleDrawable(Context context, int radius) {
        this(context, radius, false);
    }

    public COUIPressRippleDrawable(Context context, int radius, boolean needPadding) {
        super(
                COUIStateListUtil.createColorStateList(
                        COUIContextUtil.getAttrColor(context, parseThemeColor()),
                        TRANSPARENT
                ),
                new ColorDrawable(TRANSPARENT),
                new COUIPressMaskDrawable(radius)
        );
        if (needPadding) {
            initPadding(context);
        }
    }

    private void initPadding(Context context) {
        int horizontal = context.getResources().getDimensionPixelOffset(R.dimen.text_ripple_bg_padding_horizontal);
        int vertical = context.getResources().getDimensionPixelOffset(R.dimen.text_ripple_bg_padding_vertical);
        setPadding(horizontal, vertical, horizontal, vertical);
    }

    private static int parseThemeColor() {
        return Build.VERSION.SDK_INT >= U ? R.attr.couiColorPressBackground : R.attr.couiColorRipplePressBackground;
    }
}
