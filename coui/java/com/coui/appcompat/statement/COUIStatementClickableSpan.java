package com.coui.appcompat.statement;

import com.coui.appcompat.R;

import android.content.Context;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;
import androidx.core.graphics.ColorUtils;
import com.coui.appcompat.contextutil.COUIContextUtil;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;


public class COUIStatementClickableSpan extends ClickableSpan {
    public static final int ALPHA_PRESSED = 77;
    public static final Companion Companion = new Companion(null);
    private boolean isPressed;
    private final Context mContext;

    public static final class Companion {
        private Companion() {
        }

        public Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }
    }

    public COUIStatementClickableSpan(Context context) {
        Intrinsics.checkNotNullParameter(context, "context");
        this.mContext = context;
    }

    public final Context getMContext() {
        return this.mContext;
    }

    public final boolean isPressed() {
        return this.isPressed;
    }

    @Override
    public void onClick(View widget) {
        Intrinsics.checkNotNullParameter(widget, "widget");
        if (widget instanceof TextView) {
            ((TextView) widget).setHighlightColor(0);
        }
    }

    public final void setPressed(boolean pressed) {
        this.isPressed = pressed;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        Intrinsics.checkNotNullParameter(ds, "ds");
        int attrColor = COUIContextUtil.getAttrColor(this.mContext, R.attr.couiColorLink);
        if (this.isPressed) {
            attrColor = ColorUtils.setAlphaComponent(attrColor, ALPHA_PRESSED);
        }
        ds.setColor(attrColor);
    }
}
