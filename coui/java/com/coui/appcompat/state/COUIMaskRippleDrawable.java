package com.coui.appcompat.state;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;

import com.coui.appcompat.R;
import com.coui.appcompat.contextutil.COUIContextUtil;
import com.coui.appcompat.log.COUILog;

public class COUIMaskRippleDrawable extends RippleStatefulDrawable {
    private static final float DEFAULT_SPRING_BOUNCE = 0.0f;
    private static final float DEFAULT_SPRING_RESPONSE = 0.3f;
    private static final int[] PRESS_ENTERED_STATE_SET = {android.R.attr.state_enabled, android.R.attr.state_pressed};
    private static final int[] PRESS_EXITED_STATE_SET = {android.R.attr.state_enabled};
    public static final int RIPPLE_MASK_TYPE_CIRCLE = 0;
    public static final int RIPPLE_MASK_TYPE_CUSTOM_PATH = 1;
    public static final int RIPPLE_TYPE_CHECKBOX_RADIUS = 1;
    public static final int RIPPLE_TYPE_ICON_RADIUS = 0;
    private static final int RIPPLE_RADIUS_AUTO = -1;
    private static final String TAG = "COUIMaskRippleDrawable";
    private static final int U = 34;

    private boolean mAnimateEnabled = true;
    private final Path mClipPath = new Path();
    private final StateEffectAnimator mFocusAnimator;
    private final Rect mHostBounds;
    private final StateEffectAnimator mHoverAnimator;
    private Path mMaskPath;
    private float mMaskRadiusX;
    private float mMaskRadiusY;
    private RectF mMaskRect;
    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int mRadius;
    private int mRippleMaskType;

    public COUIMaskRippleDrawable(Context context) {
        super(TAG);
        mHostBounds = getBounds();
        setColor(ColorStateList.valueOf(resolvePressColor(context)));
        setCircleRippleMask(0);
        mHoverAnimator = new StateEffectAnimator(this, "hover", 0,
                COUIContextUtil.getAttrColor(context, R.attr.couiColorHover));
        mFocusAnimator = new StateEffectAnimator(this, "focus", 0,
                COUIContextUtil.getAttrColor(context, R.attr.couiColorFocus));
        mHoverAnimator.setSpringBounce(DEFAULT_SPRING_BOUNCE);
        mHoverAnimator.setSpringResponse(DEFAULT_SPRING_RESPONSE);
        mFocusAnimator.setSpringBounce(DEFAULT_SPRING_BOUNCE);
        mFocusAnimator.setSpringResponse(DEFAULT_SPRING_RESPONSE);
    }

    private static int resolvePressColor(Context context) {
        int color = COUIContextUtil.getAttrColor(context, R.attr.couiColorPress);
        if (Build.VERSION.SDK_INT == U) {
            color = COUIContextUtil.getAttrColor(context, R.attr.couiColorPressBackground);
        } else if (Build.VERSION.SDK_INT < U) {
            color = COUIContextUtil.getAttrColor(context, R.attr.couiColorRipplePressBackground);
        }
        return color;
    }

    private void clipPathOrRect(Canvas canvas) {
        if (mMaskPath != null) {
            canvas.clipPath(mMaskPath);
            return;
        }
        if (mMaskRect != null) {
            mClipPath.reset();
            mClipPath.addRoundRect(mMaskRect, mMaskRadiusX, mMaskRadiusY, Path.Direction.CCW);
            canvas.clipPath(mClipPath);
            return;
        }
        Rect bounds = getBounds();
        float radius = Math.max(0, Math.min(bounds.width(), bounds.height())) / 2.0f;
        mClipPath.reset();
        mClipPath.addRoundRect(bounds.left, bounds.top, bounds.right, bounds.bottom, radius, radius, Path.Direction.CCW);
        canvas.clipPath(mClipPath);
    }

    private void drawPathOrRect(Canvas canvas) {
        if (mRippleMaskType == RIPPLE_MASK_TYPE_CIRCLE) {
            canvas.drawCircle(mHostBounds.centerX(), mHostBounds.centerY(), mRadius, mPaint);
        } else if (mRippleMaskType == RIPPLE_MASK_TYPE_CUSTOM_PATH) {
            if (mMaskPath != null) {
                canvas.drawPath(mMaskPath, mPaint);
            } else if (mMaskRect != null) {
                canvas.drawRoundRect(mMaskRect, mMaskRadiusX, mMaskRadiusY, mPaint);
            } else {
                Rect bounds = getBounds();
                float radius = Math.max(0, Math.min(bounds.width(), bounds.height())) / 2.0f;
                canvas.drawRoundRect(bounds.left, bounds.top, bounds.right, bounds.bottom, radius, radius, mPaint);
            }
        }
    }

    public static int getMaskRippleRadiusByType(Context context, int type) {
        if (type == RIPPLE_TYPE_ICON_RADIUS) {
            return context.getResources().getDimensionPixelOffset(R.dimen.icon_ripple_bg_radius);
        }
        if (type == RIPPLE_TYPE_CHECKBOX_RADIUS) {
            return context.getResources().getDimensionPixelOffset(R.dimen.checkbox_ripple_bg_radius);
        }
        COUILog.e(TAG, "wrong mask type!");
        return 0;
    }

    private void setRadiusCompat(int radius) {
        setRadius(radius);
    }

    @Override
    public void draw(Canvas canvas) {
        if (!isDrawableEnabled()) {
            return;
        }
        if (mRippleMaskType == RIPPLE_MASK_TYPE_CUSTOM_PATH) {
            canvas.save();
            clipPathOrRect(canvas);
        }
        if (mHoverAnimator.getCurrentMaskColor() != 0) {
            mPaint.setColor(mHoverAnimator.getCurrentMaskColor());
            drawPathOrRect(canvas);
        }
        if (mFocusAnimator.getCurrentMaskColor() != 0) {
            mPaint.setColor(mFocusAnimator.getCurrentMaskColor());
            drawPathOrRect(canvas);
        }
        super.draw(canvas);
        if (mRippleMaskType == RIPPLE_MASK_TYPE_CUSTOM_PATH) {
            canvas.restore();
        }
    }

    @Override
    protected boolean onStateChange(int[] stateSet) {
        mDrawableStateManager.onStateChange(stateSet);
        return false;
    }

    @Override
    public void onViewStateChanged(int state) {
        if (!isEnabled()) {
            return;
        }
        if (state == android.R.attr.state_focused && !isStateLocked(android.R.attr.state_focused)) {
            mFocusAnimator.animateToProgress(isFocused() ? StateEffectAnimator.DEFAULT_ANIMATE_FACTOR : 0.0f, mAnimateEnabled);
        } else if (state == android.R.attr.state_hovered && !isStateLocked(android.R.attr.state_hovered)) {
            mHoverAnimator.animateToProgress(isHovered() ? StateEffectAnimator.DEFAULT_ANIMATE_FACTOR : 0.0f, mAnimateEnabled);
        } else if (state == android.R.attr.state_pressed) {
            if (isPressed()) {
                PRESS_ENTERED_STATE_SET[0] = isEnabled() ? android.R.attr.state_enabled : -android.R.attr.state_enabled;
                super.onStateChange(PRESS_ENTERED_STATE_SET);
            } else {
                PRESS_EXITED_STATE_SET[0] = isEnabled() ? android.R.attr.state_enabled : -android.R.attr.state_enabled;
                super.onStateChange(PRESS_EXITED_STATE_SET);
            }
            invalidateSelf();
        }
    }

    @Override
    public void refresh(Context context) {
        mHoverAnimator.setEndMaskColor(COUIContextUtil.getAttrColor(context, R.attr.couiColorHover));
        mFocusAnimator.setEndMaskColor(COUIContextUtil.getAttrColor(context, R.attr.couiColorFocus));
        setColor(ColorStateList.valueOf(resolvePressColor(context)));
    }

    @Override
    public void reset() {
        mHoverAnimator.animateToProgress(0.0f, false);
        mFocusAnimator.animateToProgress(0.0f, false);
    }

    @Override
    public void setAnimateEnabled(boolean enabled) {
        mAnimateEnabled = enabled;
    }

    public void setCircleRippleMask(int radius) {
        if (radius < 0) {
            COUILog.e(TAG, "radius should larger than 0!");
            return;
        }
        mRippleMaskType = RIPPLE_MASK_TYPE_CIRCLE;
        setRadiusCompat(radius);
        mRadius = radius;
    }

    public void setCustomRippleMask() {
        mRippleMaskType = RIPPLE_MASK_TYPE_CUSTOM_PATH;
        setRadiusCompat(RIPPLE_RADIUS_AUTO);
    }

    public void setFocusStateLocked(boolean locked, boolean entered, boolean animated) {
        setStateLocked(android.R.attr.state_focused, locked, entered, animated);
    }

    public void setHoverStateLocked(boolean locked, boolean entered, boolean animated) {
        setStateLocked(android.R.attr.state_hovered, locked, entered, animated);
    }

    public void setMaskPath(Path path) {
        mMaskPath = path;
    }

    public void setMaskRect(RectF rect, float radiusX, float radiusY) {
        mMaskRect = rect;
        mMaskRadiusX = radiusX;
        mMaskRadiusY = radiusY;
    }

    @Override
    public void setStateLocked(int state, boolean locked, boolean entered, boolean animated) {
        super.setStateLocked(state, locked, entered, animated);
        if (state == android.R.attr.state_pressed) {
            COUILog.w(TAG, "Lock state press in COUIMaskRippleDrawable is not allowed!");
        }
        if (state == android.R.attr.state_hovered) {
            mHoverAnimator.animateToProgress(entered ? StateEffectAnimator.DEFAULT_ANIMATE_FACTOR : 0.0f, animated);
        }
        if (state == android.R.attr.state_focused) {
            mFocusAnimator.animateToProgress(entered ? StateEffectAnimator.DEFAULT_ANIMATE_FACTOR : 0.0f, animated);
        }
    }
}
