package com.coui.appcompat.state;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;

import com.coui.appcompat.R;
import com.coui.appcompat.contextutil.COUIContextUtil;
import androidx.core.graphics.ColorUtils;
import com.coui.appcompat.log.COUILog;

public class COUIMaskEffectDrawable extends StatefulDrawable {
    public static final int MASK_EFFECT_TYPE_WIDGET_WITH_BACKGROUND = 0;
    public static final int MASK_EFFECT_TYPE_CONTAINER_WIDGET = 1;
    private static final float DEFAULT_MIN_PROGRESS_FOR_TOUCH_ENTER_ANIMATION = 0.7f;

    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final StateEffectAnimator mHoverAnimator;
    private final StateEffectAnimator mFocusAnimator;
    private final StateEffectAnimator mPressAnimator;
    private boolean mAnimateEnabled = true;
    private boolean mEnableFocusedState = true;
    private boolean mEnableSelectedState = true;
    private boolean mRoundStyle = true;
    private float mMinProgressForTouchEnterAnimation = DEFAULT_MIN_PROGRESS_FOR_TOUCH_ENTER_ANIMATION;
    private int mMaskType;
    private Path mMaskPath;
    private RectF mMaskRect;
    private float mMaskRadiusX;
    private float mMaskRadiusY;
    private StatefulDrawableListener mListener;

    public COUIMaskEffectDrawable(Context context, int maskType) {
        super("COUIMaskEffectDrawable");
        mMaskType = maskType;
        mHoverAnimator = new StateEffectAnimator(this, "hover", 0, COUIContextUtil.getAttrColor(context, R.attr.couiColorHover));
        mFocusAnimator = new StateEffectAnimator(this, "focus", 0, COUIContextUtil.getAttrColor(context, R.attr.couiColorFocus));
        mPressAnimator = new StateEffectAnimator(this, "press", 0, COUIContextUtil.getAttrColor(context, R.attr.couiColorPress));
        mHoverAnimator.setSpringResponse(0.3f);
        mHoverAnimator.setSpringBounce(0.0f);
        mFocusAnimator.setSpringResponse(0.3f);
        mFocusAnimator.setSpringBounce(0.0f);
        mPressAnimator.setSpringResponse(0.3f);
        mPressAnimator.setSpringBounce(0.0f);
    }

    @Override
    public void draw(Canvas canvas) {
        if (!isDrawableEnabled()) {
            return;
        }
        drawIfNeeded(canvas, mHoverAnimator.getCurrentMaskColor());
        if (mMaskType == MASK_EFFECT_TYPE_CONTAINER_WIDGET) {
            drawIfNeeded(canvas, mFocusAnimator.getCurrentMaskColor());
        }
        drawIfNeeded(canvas, mPressAnimator.getCurrentMaskColor());
    }

    private void drawIfNeeded(Canvas canvas, int color) {
        if (color == 0) {
            return;
        }
        mPaint.setColor(color);
        drawPathOrRect(canvas);
    }

    private void drawPathOrRect(Canvas canvas) {
        if (mMaskPath != null) {
            canvas.drawPath(mMaskPath, mPaint);
            return;
        }
        if (mMaskRect != null) {
            canvas.drawRoundRect(mMaskRect, mMaskRadiusX, mMaskRadiusY, mPaint);
            return;
        }
        Rect bounds = getBounds();
        float radius = mRoundStyle ? Math.max(0, Math.min(bounds.width(), bounds.height())) / 2.0f : 0.0f;
        canvas.drawRoundRect(bounds.left, bounds.top, bounds.right, bounds.bottom, radius, radius, mPaint);
    }

    public void enableFocusedState(boolean enabled) {
        mEnableFocusedState = enabled;
    }

    public void enableSelectedState(boolean enabled) {
        mEnableSelectedState = enabled;
    }

    public int getCompositeMaskColor() {
        return ColorUtils.compositeColors(
                mPressAnimator.getCurrentMaskColor(),
                ColorUtils.compositeColors(mFocusAnimator.getCurrentMaskColor(), mHoverAnimator.getCurrentMaskColor()));
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

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
            reset();
            return;
        }
        if (!isEnabled()) {
            return;
        }
        if (state == STATE_TOUCH_ENTERED && !isStateLocked(STATE_TOUCH_ENTERED)) {
            int touchType = getTouchType();
            if (touchType != TOUCH_TYPE_PRESSED) {
                if (touchType == TOUCH_TYPE_SELECTED) {
                    mPressAnimator.animateToProgress(
                            isTouchEntered() ? StateEffectAnimator.DEFAULT_ANIMATE_FACTOR : 0.0f,
                            false);
                }
                return;
            }
            if (isTouchEntered()) {
                mPressAnimator.animateToProgress(StateEffectAnimator.DEFAULT_ANIMATE_FACTOR, true);
                return;
            }
            mPressAnimator.animateToProgressUntil(0.0f,
                    mMinProgressForTouchEnterAnimation * StateEffectAnimator.DEFAULT_ANIMATE_FACTOR);
            return;
        }
        if (state == STATE_HOVERED && !isStateLocked(STATE_HOVERED)) {
            mHoverAnimator.animateToProgress(isHovered() ? StateEffectAnimator.DEFAULT_ANIMATE_FACTOR : 0.0f, mAnimateEnabled);
            return;
        }
        if (mEnableFocusedState && state == STATE_FOCUSED && !isStateLocked(STATE_FOCUSED)) {
            if (mMaskType == MASK_EFFECT_TYPE_CONTAINER_WIDGET) {
                mFocusAnimator.animateToProgress(isFocused() ? StateEffectAnimator.DEFAULT_ANIMATE_FACTOR : 0.0f, mAnimateEnabled);
            }
        } else if (mEnableSelectedState && state == STATE_SELECTED && !isStateLocked(STATE_SELECTED)
                && mMaskType == MASK_EFFECT_TYPE_CONTAINER_WIDGET) {
            mFocusAnimator.animateToProgress(isSelected() ? StateEffectAnimator.DEFAULT_ANIMATE_FACTOR : 0.0f, mAnimateEnabled);
        }
    }

    @Override
    public void refresh(Context context) {
        mHoverAnimator.setEndMaskColor(COUIContextUtil.getAttrColor(context, R.attr.couiColorHover));
        mFocusAnimator.setEndMaskColor(COUIContextUtil.getAttrColor(context, R.attr.couiColorFocus));
        mPressAnimator.setEndMaskColor(COUIContextUtil.getAttrColor(context, R.attr.couiColorPress));
    }

    @Override
    public void reset() {
        mHoverAnimator.animateToProgress(0.0f, false);
        mFocusAnimator.animateToProgress(0.0f, false);
        mPressAnimator.animateToProgress(0.0f, false);
    }

    @Override
    public void setAlpha(int alpha) {
    }

    @Override
    public void setAnimateEnabled(boolean enabled) {
        mAnimateEnabled = enabled;
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
    }

    @Override
    public void setDrawableEnabled(boolean enabled) {
        super.setDrawableEnabled(enabled);
        if (!enabled) {
            reset();
        }
    }

    public void setIsRoundStyle(boolean roundStyle) {
        mRoundStyle = roundStyle;
    }

    public void setFocusStateLocked(boolean locked, boolean entered, boolean animated) {
        setStateLocked(STATE_FOCUSED, locked, entered, animated);
    }

    public void setHoverStateLocked(boolean locked, boolean entered, boolean animated) {
        setStateLocked(STATE_HOVERED, locked, entered, animated);
    }

    public void setMaskPath(Path path) {
        mMaskPath = path;
    }

    public void setMaskRect(RectF rect, float radiusX, float radiusY) {
        mMaskRect = rect;
        mMaskRadiusX = radiusX;
        mMaskRadiusY = radiusY;
    }

    public void setMaskType(int maskType) {
        mMaskType = maskType;
    }

    public void setMinProgressForTouchEnterAnimation(float minProgress) {
        if (minProgress >= 0.0f && minProgress <= 1.0f) {
            mMinProgressForTouchEnterAnimation = minProgress;
        } else {
            COUILog.e("COUIMaskEffectDrawable", "Touch enter min progress should be within range [0, 1]");
        }
    }

    @Override
    public void setStateLocked(int state, boolean locked, boolean entered, boolean animated) {
        super.setStateLocked(state, locked, entered, animated);
        if (state == STATE_TOUCH_ENTERED) {
            mPressAnimator.animateToProgress(entered ? StateEffectAnimator.DEFAULT_ANIMATE_FACTOR : 0.0f, animated);
        } else if (state == STATE_HOVERED) {
            mHoverAnimator.animateToProgress(entered ? StateEffectAnimator.DEFAULT_ANIMATE_FACTOR : 0.0f, animated);
        } else if (state == STATE_FOCUSED) {
            mFocusAnimator.animateToProgress(entered ? StateEffectAnimator.DEFAULT_ANIMATE_FACTOR : 0.0f, animated);
        }
    }

    public void setStatefulDrawableListener(StatefulDrawableListener listener) {
        mListener = listener;
    }

    public void setTouchEnterStateLocked(boolean locked, boolean entered, boolean animated) {
        setStateLocked(STATE_TOUCH_ENTERED, locked, entered, animated);
    }
}
