package com.coui.appcompat.roundRect;

import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;

public class COUIRoundRectUtil {
    private final Path mPath;

    public static final class SInstanceHolder {
        static final COUIRoundRectUtil sInstance = new COUIRoundRectUtil();

        private SInstanceHolder() {
        }
    }
    private COUIRoundRectUtil() {
        this.mPath = new Path();
    }

    public static COUIRoundRectUtil getInstance() {
        return SInstanceHolder.sInstance;
    }

    public Path getPath(Rect rect, float radius) {
        return getPath(new RectF(rect), radius);
    }

    public Path getPath(RectF rectF, float radius) {
        return COUIShapePath.getRoundRectPath(this.mPath, rectF, radius);
    }

    public Path getPath(
            float left,
            float top,
            float right,
            float bottom,
            float radius
    ) {
        return getPath(new RectF(left, top, right, bottom), radius);
    }

    public Path getPath(
            float left,
            float top,
            float right,
            float bottom,
            float radius,
            boolean topLeft,
            boolean topRight,
            boolean bottomRight,
            boolean bottomLeft
    ) {
        return COUIShapePath.getRoundRectPath(
                this.mPath,
                new RectF(left, top, right, bottom),
                radius,
                topLeft,
                topRight,
                bottomRight,
                bottomLeft
        );
    }
}
