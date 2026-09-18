package com.coui.appcompat.rippleutil;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.view.View;

import com.coui.appcompat.darkmode.COUIDarkModeUtil;
import com.coui.appcompat.state.COUIMaskRippleDrawable;
import com.coui.appcompat.state.COUIStateEffectDrawable;

public class COUIRippleDrawableUtil {
    private COUIRippleDrawableUtil() {
    }

    public static Drawable getRippleDrawable(Context context, int resId, int radius) {
        return context.getDrawable(resId);
    }

    public static void setIconPressRippleDrawable(View view, int radius) {
        if (view == null) {
            return;
        }
        COUIMaskRippleDrawable mask = new COUIMaskRippleDrawable(view.getContext());
        mask.setCircleRippleMask(radius);
        view.setBackground(new COUIStateEffectDrawable(new Drawable[]{mask}));
        COUIDarkModeUtil.setForceDarkAllow(view, false);
    }

    public static void setPressRippleDrawable(View view, int radius) {
        if (view == null) {
            return;
        }
        view.setBackground(new COUIPressRippleDrawable(view.getContext(), radius));
    }

    public static void setPressRippleDrawable(View view, int radius, boolean bounded) {
        if (view == null) {
            return;
        }
        view.setBackground(new COUIPressRippleDrawable(view.getContext(), radius, bounded));
    }

    public static void setRadiusAdaptation(RippleDrawable rippleDrawable, int radius) {
        rippleDrawable.setRadius(radius);
    }
}
