package com.coui.appcompat.state;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;

import com.coui.appcompat.R;
import com.coui.appcompat.contextutil.COUIContextUtil;

public class COUIStrokeDrawable extends StatefulDrawable {
    public static final int TYPE_OUTER = 0;
    public static final int TYPE_INNER = 1;

    private final Paint mStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path mClipPath = new Path();
    private final StateEffectAnimator mFocusAnimator;
    private boolean mAnimateEnabled = true;
    private Path mStrokePath;
    private RectF mStrokeRect;
    private float mRadiusX;
    private float mRadiusY;
    private int mStrokeType = TYPE_OUTER;
    private StatefulDrawableListener mListener;

    public COUIStrokeDrawable(Context context) {
        super("COUIStrokeDrawable");
        mStrokePaint.setStyle(Paint.Style.STROKE);
        mStrokePaint.setStrokeWidth(context.getResources().getDimensionPixelOffset(R.dimen.default_focus_stroke_radius) * 2.0f);
        mFocusAnimator = new StateEffectAnimator(this, "focus", 0, COUIContextUtil.getAttrColor(context, R.attr.couiColorFocusOutline));
        mFocusAnimator.setSpringBounce(0.0f);
        mFocusAnimator.setSpringResponse(0.3f);
    }

    @Override
    public void draw(Canvas canvas) {
        int color = mFocusAnimator.getCurrentMaskColor();
        if (!isDrawableEnabled() || color == 0) {
            return;
        }
        mStrokePaint.setColor(color);
        canvas.save();
        if (mStrokePath != null) {
            canvas.clipPath(mStrokePath, mStrokeType == TYPE_INNER ? Region.Op.INTERSECT : Region.Op.DIFFERENCE);
            canvas.drawPath(mStrokePath, mStrokePaint);
        } else {
            Path path = mClipPath;
            path.reset();
            if (mStrokeRect != null) {
                path.addRoundRect(mStrokeRect, mRadiusX, mRadiusY, Path.Direction.CCW);
            } else {
                Rect bounds = getBounds();
                float radius = Math.max(0, Math.min(bounds.width(), bounds.height())) / 2.0f;
                path.addRoundRect(bounds.left, bounds.top, bounds.right, bounds.bottom, radius, radius, Path.Direction.CCW);
            }
            canvas.clipPath(path, mStrokeType == TYPE_INNER ? Region.Op.INTERSECT : Region.Op.DIFFERENCE);
            canvas.drawPath(path, mStrokePaint);
        }
        canvas.restore();
    }

    @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
    @Override public void setAlpha(int alpha) { }
    @Override public void setColorFilter(ColorFilter colorFilter) { }

    @Override
    public void invalidateSelf() {
        super.invalidateSelf();
        if (mListener != null) {
            mListener.onDrawableUpdate();
        }
    }

    @Override
    public void onViewStateChanged(int state) {
        if (state == STATE_ENABLED && !isEnabled()) {
            mFocusAnimator.animateToProgress(0.0f, false);
        } else if (isEnabled() && state == STATE_FOCUSED) {
            mFocusAnimator.animateToProgress(isFocused() ? StateEffectAnimator.DEFAULT_ANIMATE_FACTOR : 0.0f, mAnimateEnabled);
        }
    }

    @Override
    public void refresh(Context context) {
        mFocusAnimator.setEndMaskColor(COUIContextUtil.getAttrColor(context, R.attr.couiColorFocusOutline));
    }

    @Override
    public void reset() {
        mFocusAnimator.animateToProgress(0.0f, false);
    }

    @Override
    public void setAnimateEnabled(boolean enabled) {
        mAnimateEnabled = enabled;
    }

    @Override
    public void setDrawableEnabled(boolean enabled) {
        super.setDrawableEnabled(enabled);
        if (!enabled) {
            reset();
        }
    }

    @Override
    public void setStateLocked(int state, boolean locked, boolean entered, boolean animated) {
        super.setStateLocked(state, locked, entered, animated);
        if (state == STATE_FOCUSED) {
            mFocusAnimator.animateToProgress(entered ? StateEffectAnimator.DEFAULT_ANIMATE_FACTOR : 0.0f, animated);
        }
    }

    public void setFocusStateLocked(boolean locked, boolean entered, boolean animated) {
        setStateLocked(STATE_FOCUSED, locked, entered, animated);
    }

    public void setStatefulDrawableListener(StatefulDrawableListener listener) {
        mListener = listener;
    }

    public void setStrokePath(Path path) {
        mStrokePath = path;
    }

    public void setStrokeRect(RectF rect, float radiusX, float radiusY) {
        mStrokeRect = rect;
        mRadiusX = radiusX;
        mRadiusY = radiusY;
    }

    public void setStrokeType(int strokeType) {
        mStrokeType = strokeType;
    }
}
