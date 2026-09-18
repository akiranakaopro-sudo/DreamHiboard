package com.coui.appcompat.input;

import android.graphics.BlendMode;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.view.View;

import androidx.dynamicanimation.animation.FloatValueHolder;

import com.coui.appcompat.animation.dynamicanimation.COUIDynamicAnimation;
import com.coui.appcompat.animation.dynamicanimation.COUISpringAnimation;
import com.coui.appcompat.animation.dynamicanimation.COUISpringForce;

class LightEffectHelper {
    private static final float APPEAR_SPRING_RESPONSE = 0.3f;
    private static final float DEFAULT_ALPHA_VALUE = 255.0f;
    private static final float DEFAULT_DELAY_PERCENT = 0.6f;
    private static final float DEFAULT_SPRING_RESPONSE = 0.3f;
    private static final float DISAPPEAR_SPRING_RESPONSE = 0.6f;

    private LightEffectHelperCallback mCallback;
    private float mInnerLightAlpha;
    private COUISpringAnimation mInnerLightAnimator;
    private float mInnerLightRadius;
    private RadialGradient mInnerLightShader;
    private float mOuterLightAlpha;
    private COUISpringAnimation mOuterLightAnimator;
    private float mOuterLightRadiusEnd;
    private float mOuterLightRadiusStart;
    private RadialGradient mOuterLightShader;
    private final Matrix mRadialMatrix;
    private final View mTargetView;

    public interface LightEffectHelperCallback {
        default void onInnerLightUpdate(float alpha) {
        }
    }

    public LightEffectHelper(View view) {
        this(view, 0.0f, 0.0f, null, null);
    }

    public LightEffectHelper(View view, float innerRadius, float outerRadius, RadialGradient innerShader,
            RadialGradient outerShader) {
        mTargetView = view;
        mRadialMatrix = new Matrix();
        mInnerLightRadius = innerRadius;
        mOuterLightRadiusStart = DEFAULT_DELAY_PERCENT * outerRadius;
        mOuterLightRadiusEnd = outerRadius;
        mInnerLightShader = innerShader;
        mOuterLightShader = outerShader;
    }

    private void ensureLightEffectAnimator() {
        if (mInnerLightAnimator == null) {
            COUISpringForce springForce = new COUISpringForce();
            springForce.setBounce(0.0f);
            springForce.setResponse(DEFAULT_SPRING_RESPONSE);
            mInnerLightAnimator = new COUISpringAnimation(new FloatValueHolder(mInnerLightAlpha));
            mInnerLightAnimator.setSpring(springForce);
            mInnerLightAnimator.addUpdateListener(new COUIDynamicAnimation.OnAnimationUpdateListener() {
                @Override
                public void onAnimationUpdate(COUIDynamicAnimation animation, float value, float velocity) {
                    mInnerLightAlpha = value;
                    if (mCallback != null) {
                        mCallback.onInnerLightUpdate(value);
                    }
                    if (mTargetView != null) {
                        mTargetView.invalidate();
                    }
                }
            });
        }
        if (mOuterLightAnimator == null) {
            COUISpringForce springForce = new COUISpringForce();
            springForce.setBounce(0.0f);
            springForce.setResponse(DEFAULT_SPRING_RESPONSE);
            mOuterLightAnimator = new COUISpringAnimation(new FloatValueHolder(mOuterLightAlpha));
            mOuterLightAnimator.setSpring(springForce);
            mOuterLightAnimator.addUpdateListener(new COUIDynamicAnimation.OnAnimationUpdateListener() {
                @Override
                public void onAnimationUpdate(COUIDynamicAnimation animation, float value, float velocity) {
                    mOuterLightAlpha = value;
                    if (mTargetView != null) {
                        mTargetView.invalidate();
                    }
                }
            });
        }
    }

    public void drawLightEffect(Canvas canvas, float scale, Path path, Paint paint, float centerX, float centerY) {
        if (paint == null || mInnerLightShader == null || mOuterLightShader == null) {
            return;
        }
        float outerScale = (((mOuterLightAlpha / DEFAULT_ALPHA_VALUE)
                * (mOuterLightRadiusEnd - mOuterLightRadiusStart)) + mOuterLightRadiusStart) / mOuterLightRadiusEnd;
        mRadialMatrix.reset();
        mRadialMatrix.setScale(outerScale, outerScale, centerX, centerY);
        mOuterLightShader.setLocalMatrix(mRadialMatrix);
        paint.setShader(mOuterLightShader);
        paint.setAlpha((int) mOuterLightAlpha);
        paint.setBlendMode(BlendMode.LIGHTEN);
        canvas.drawPath(path, paint);

        mRadialMatrix.reset();
        mRadialMatrix.setScale(scale, scale, centerX, centerY);
        mInnerLightShader.setLocalMatrix(mRadialMatrix);
        paint.setShader(mInnerLightShader);
        paint.setAlpha((int) mInnerLightAlpha);
        canvas.drawCircle(centerX, centerY, mInnerLightRadius * scale, paint);
    }

    public void executeLightEffectAnimator(boolean appear) {
        ensureLightEffectAnimator();
        mInnerLightAnimator.animateToFinalPosition(appear ? DEFAULT_ALPHA_VALUE : 0.0f);
        mOuterLightAnimator.getSpring().setResponse(appear ? APPEAR_SPRING_RESPONSE : DISAPPEAR_SPRING_RESPONSE);
        mOuterLightAnimator.animateToFinalPosition(appear ? DEFAULT_ALPHA_VALUE : 0.0f);
    }

    public void setCallback(LightEffectHelperCallback callback) {
        mCallback = callback;
    }

    public void updateLightShaderConfig(float innerRadius, float outerRadius, RadialGradient innerShader,
            RadialGradient outerShader) {
        mInnerLightRadius = innerRadius;
        mOuterLightRadiusStart = DEFAULT_DELAY_PERCENT * outerRadius;
        mOuterLightRadiusEnd = outerRadius;
        mInnerLightShader = innerShader;
        mOuterLightShader = outerShader;
    }
}
