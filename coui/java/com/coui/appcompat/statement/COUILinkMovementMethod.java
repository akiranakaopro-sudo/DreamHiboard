package com.coui.appcompat.statement;

import android.text.Selection;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.MotionEvent;
import android.widget.TextView;
import kotlin.jvm.internal.Intrinsics;


public final class COUILinkMovementMethod extends LinkMovementMethod {
    public static final COUILinkMovementMethod INSTANCE = new COUILinkMovementMethod();
    private static COUIStatementClickableSpan clickableSpan;

    private COUILinkMovementMethod() {
    }

    @Override
    public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
        COUIStatementClickableSpan cOUIStatementClickableSpan;
        Intrinsics.checkNotNullParameter(widget, "widget");
        Intrinsics.checkNotNullParameter(buffer, "buffer");
        Intrinsics.checkNotNullParameter(event, "event");
        if (event.getAction() == 0) {
            int touchX = (int) event.getX();
            int offsetForHorizontal = widget.getLayout().getOffsetForHorizontal(widget.getLayout().getLineForVertical((((int) event.getY()) - widget.getTotalPaddingTop()) + widget.getScrollY()), (touchX - widget.getTotalPaddingLeft()) + widget.getScrollX());
            COUIStatementClickableSpan[] links = (COUIStatementClickableSpan[]) buffer.getSpans(offsetForHorizontal, offsetForHorizontal, COUIStatementClickableSpan.class);
            if (clickableSpan == null) {
                Intrinsics.checkNotNullExpressionValue(links, "links");
                if (!(links.length == 0)) {
                    COUIStatementClickableSpan cOUIStatementClickableSpan2 = links[0];
                    cOUIStatementClickableSpan2.setPressed(true);
                    widget.invalidate();
                    clickableSpan = cOUIStatementClickableSpan2;
                }
            }
        } else if ((event.getAction() == 1 || event.getAction() == 3 || event.getAction() == 4) && (cOUIStatementClickableSpan = clickableSpan) != null) {
            cOUIStatementClickableSpan.setPressed(false);
            clickableSpan = null;
            widget.invalidate();
        }
        if (event.getAction() == 1) {
            int touchX = (int) event.getX();
            int touchY = (int) event.getY();
            int totalPaddingLeft = touchX - widget.getTotalPaddingLeft();
            int totalPaddingTop = touchY - widget.getTotalPaddingTop();
            int scrollX = totalPaddingLeft + widget.getScrollX();
            int scrollY = totalPaddingTop + widget.getScrollY();
            int offsetForHorizontal2 = widget.getLayout().getOffsetForHorizontal(widget.getLayout().getLineForVertical(scrollY), scrollX);
            ClickableSpan[] links2 = (ClickableSpan[]) buffer.getSpans(offsetForHorizontal2, offsetForHorizontal2, ClickableSpan.class);
            Intrinsics.checkNotNullExpressionValue(links2, "links");
            if (!(links2.length == 0)) {
                ClickableSpan clickableSpan2 = links2[0];
                int spanStart = buffer.getSpanStart(clickableSpan2);
                int spanEnd = buffer.getSpanEnd(clickableSpan2);
                if (offsetForHorizontal2 < spanStart || offsetForHorizontal2 > spanEnd || scrollY > widget.getMeasuredHeight() || scrollY < 0) {
                    Selection.removeSelection(buffer);
                } else {
                    clickableSpan2.onClick(widget);
                }
                return true;
            }
            Selection.removeSelection(buffer);
        }
        return super.onTouchEvent(widget, buffer, event);
    }
}
