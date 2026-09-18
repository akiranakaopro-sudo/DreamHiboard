package com.coui.appcompat.cardview;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.coui.appcompat.roundcorner.RoundCornerUtil;
import com.coui.appcompat.version.COUIVersionUtil;
import com.oplus.graphics.OplusOutline;
import com.oplus.graphics.OplusOutlineAdapter;

class RoundRectDrawable extends Drawable {
    private ColorStateList mBackground;
    private final RectF mBoundsF;
    private final Rect mBoundsI;
    private float mCardRoundCornerRadius;
    private boolean mInsetForPadding;
    private boolean mInsetForRadius = true;
    private boolean mIsSupportSmoothRoundCorner;
    private float mPadding;
    private final Paint mPaint;
    private float mRadius;
    private ColorStateList mTint;
    private PorterDuffColorFilter mTintFilter;
    private PorterDuff.Mode mTintMode = PorterDuff.Mode.SRC_IN;
    private float mWeight;

    RoundRectDrawable(ColorStateList colorStateList, float radius, float cardRoundCornerRadius) {
        this(colorStateList, radius, 0.0f, cardRoundCornerRadius);
    }

    RoundRectDrawable(ColorStateList colorStateList, float radius, float weight,
            float cardRoundCornerRadius) {
        mRadius = radius;
        mWeight = weight;
        mCardRoundCornerRadius = cardRoundCornerRadius;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        setBackground(colorStateList);
        mBoundsF = new RectF();
        mBoundsI = new Rect();
        mIsSupportSmoothRoundCorner = RoundCornerUtil.isVersionSupport();
    }

    private PorterDuffColorFilter createTintFilter(ColorStateList tint, PorterDuff.Mode tintMode) {
        if (tint == null || tintMode == null) {
            return null;
        }
        return new PorterDuffColorFilter(tint.getColorForState(getState(), 0), tintMode);
    }

    private boolean execute15SRC() {
        return isAdaptedOn15() || isAdaptedOn16();
    }

    private boolean execute16SRC() {
        return RoundCornerUtil.getSmoothStyleType() >= 1 && mCardRoundCornerRadius != 0.0f;
    }

    private Context getContextFromCallback() {
        Callback callback = getCallback();
        if (callback instanceof View) {
            return ((View) callback).getContext();
        }
        return null;
    }

    private boolean isAdaptedOn15() {
        return RoundCornerUtil.getSmoothStyleType() == 0
                && mRadius != 0.0f && mWeight != 0.0f;
    }

    private boolean isAdaptedOn16() {
        return RoundCornerUtil.getSmoothStyleType() == 1
                && mCardRoundCornerRadius == 0.0f && mRadius != 0.0f && mWeight != 0.0f;
    }

    private void setBackground(ColorStateList colorStateList) {
        if (colorStateList == null) {
            colorStateList = ColorStateList.valueOf(0);
        }
        mBackground = colorStateList;
        mPaint.setColor(colorStateList.getColorForState(getState(), mBackground.getDefaultColor()));
    }

    private void updateBounds(Rect bounds) {
        if (bounds == null) {
            bounds = getBounds();
        }
        mBoundsF.set(bounds.left, bounds.top, bounds.right, bounds.bottom);
        mBoundsI.set(bounds);
        if (mInsetForPadding) {
            mBoundsI.inset(
                    (int) Math.ceil(RoundRectDrawableWithShadow.calculateHorizontalPadding(
                            mPadding, mRadius, mInsetForRadius)),
                    (int) Math.ceil(RoundRectDrawableWithShadow.calculateVerticalPadding(
                            mPadding, mRadius, mInsetForRadius)));
            mBoundsF.set(mBoundsI);
        }
    }

    @Override
    public void draw(Canvas canvas) {
        boolean clearColorFilter = false;
        if (mTintFilter != null && mPaint.getColorFilter() == null) {
            mPaint.setColorFilter(mTintFilter);
            clearColorFilter = true;
        }
        canvas.drawColor(mPaint.getColor());
        if (clearColorFilter) {
            mPaint.setColorFilter(null);
        }
    }

    public float getCardRoundCornerRadius() {
        return mCardRoundCornerRadius;
    }

    public ColorStateList getColor() {
        return mBackground;
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public void getOutline(Outline outline) {
        if (execute16SRC()) {
            OplusOutlineAdapter oplusOutline = new OplusOutlineAdapter(outline, 1);
            if (COUIVersionUtil.getOSVersionCode() > 37) {
                Context context = getContextFromCallback();
                if (context != null) {
                    float radius = RoundCornerUtil.getRoundCornerForOS17(
                            context, (int) mCardRoundCornerRadius);
                    oplusOutline.setSmoothRoundRect(mBoundsI, radius, 3.0f);
                } else {
                    oplusOutline.setSmoothRoundRect(mBoundsI, mCardRoundCornerRadius);
                }
            } else {
                oplusOutline.setSmoothRoundRect(mBoundsI, mCardRoundCornerRadius);
            }
            return;
        }
        if (execute15SRC()) {
            new OplusOutline(outline).setSmoothRoundRect(mBoundsI, mRadius, mWeight);
            return;
        }
        float radius = mCardRoundCornerRadius != 0.0f ? mCardRoundCornerRadius : mRadius;
        outline.setRoundRect(mBoundsI, radius);
    }

    public float getPadding() {
        return mPadding;
    }

    public float getRadius() {
        return mRadius;
    }

    public float getWeight() {
        return mWeight;
    }

    @Override
    public boolean isStateful() {
        return (mTint != null && mTint.isStateful())
                || (mBackground != null && mBackground.isStateful())
                || super.isStateful();
    }

    @Override
    public void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        updateBounds(bounds);
    }

    @Override
    public boolean onStateChange(int[] state) {
        int color = mBackground.getColorForState(state, mBackground.getDefaultColor());
        boolean changed = color != mPaint.getColor();
        if (changed) {
            mPaint.setColor(color);
        }
        if (mTint == null || mTintMode == null) {
            return changed;
        }
        mTintFilter = createTintFilter(mTint, mTintMode);
        return true;
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    public void setCardRoundCornerRadius(float radius) {
        if (radius == mCardRoundCornerRadius) {
            return;
        }
        mCardRoundCornerRadius = radius;
        updateBounds(null);
        invalidateSelf();
    }

    public void setColor(ColorStateList colorStateList) {
        setBackground(colorStateList);
        invalidateSelf();
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        mPaint.setColorFilter(colorFilter);
    }

    public void setPadding(float padding, boolean insetForPadding, boolean insetForRadius) {
        if (padding == mPadding && mInsetForPadding == insetForPadding
                && mInsetForRadius == insetForRadius) {
            return;
        }
        mPadding = padding;
        mInsetForPadding = insetForPadding;
        mInsetForRadius = insetForRadius;
        updateBounds(null);
        invalidateSelf();
    }

    public void setRadius(float radius) {
        if (radius == mRadius) {
            return;
        }
        mRadius = radius;
        updateBounds(null);
        invalidateSelf();
    }

    @Override
    public void setTintList(ColorStateList tint) {
        mTint = tint;
        mTintFilter = createTintFilter(tint, mTintMode);
        invalidateSelf();
    }

    @Override
    public void setTintMode(PorterDuff.Mode tintMode) {
        mTintMode = tintMode;
        mTintFilter = createTintFilter(mTint, tintMode);
        invalidateSelf();
    }

    public void setWeight(float weight) {
        if (weight == mWeight) {
            return;
        }
        mWeight = weight;
        updateBounds(null);
        invalidateSelf();
    }
}
