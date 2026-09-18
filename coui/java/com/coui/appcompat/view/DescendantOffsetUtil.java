package com.coui.appcompat.view;

import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

public class DescendantOffsetUtil {
    private static final ThreadLocal<Matrix> MATRIX = new ThreadLocal<>();
    private static final ThreadLocal<RectF> RECT_F = new ThreadLocal<>();
    private static final float ROUNDING_OFFSET = 0.5f;

    private DescendantOffsetUtil() {
    }

    public static void getDescendantRect(ViewGroup parent, View descendant, Rect out) {
        out.set(0, 0, descendant.getWidth(), descendant.getHeight());
        offsetDescendantRect(parent, descendant, out);
    }

    public static void offsetDescendantRect(ViewGroup parent, View descendant, Rect rect) {
        Matrix matrix = MATRIX.get();
        if (matrix == null) {
            matrix = new Matrix();
            MATRIX.set(matrix);
        } else {
            matrix.reset();
        }
        offsetDescendantMatrix(parent, descendant, matrix);

        RectF rectF = RECT_F.get();
        if (rectF == null) {
            rectF = new RectF();
            RECT_F.set(rectF);
        }
        rectF.set(rect);
        matrix.mapRect(rectF);
        rect.set((int) (rectF.left + ROUNDING_OFFSET),
                (int) (rectF.top + ROUNDING_OFFSET),
                (int) (rectF.right + ROUNDING_OFFSET),
                (int) (rectF.bottom + ROUNDING_OFFSET));
    }

    private static void offsetDescendantMatrix(ViewParent targetParent, View view, Matrix matrix) {
        ViewParent parent = view.getParent();
        if (parent instanceof View && parent != targetParent) {
            View parentView = (View) parent;
            offsetDescendantMatrix(targetParent, parentView, matrix);
            matrix.preTranslate(-parentView.getScrollX(), -parentView.getScrollY());
        }
        matrix.preTranslate(view.getLeft(), view.getTop());
        if (!view.getMatrix().isIdentity()) {
            matrix.preConcat(view.getMatrix());
        }
    }
}
