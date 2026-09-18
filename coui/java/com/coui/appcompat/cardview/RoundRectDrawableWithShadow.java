package com.coui.appcompat.cardview;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

import com.coui.appcompat.R;

class RoundRectDrawableWithShadow extends Drawable {
    private static final double COS_45 = Math.cos(Math.toRadians(45.0d));
    private static final float SHADOW_MULTIPLIER = 1.5f;
    private static RoundRectHelper sRoundRectHelper;

    private boolean mAddPaddingForCorners = true;
    private ColorStateList mBackground;
    private final RectF mCardBounds;
    private float mCornerRadius;
    private Paint mCornerShadowPaint;
    private Path mCornerShadowPath;
    private boolean mDirty = true;
    private Paint mEdgeShadowPaint;
    private final int mInsetShadow;
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
    private boolean mPrintedShadowClipWarning;
    private float mRawMaxShadowSize;
    private float mRawShadowSize;
    private final int mShadowEndColor;
    private float mShadowSize;
    private final int mShadowStartColor;

    public interface RoundRectHelper {
        void drawRoundRect(Canvas canvas, RectF bounds, float cornerRadius, Paint paint);
    }

    RoundRectDrawableWithShadow(Resources resources, ColorStateList colorStateList, float radius,
            float shadowSize, float maxShadowSize) {
        mShadowStartColor = resources.getColor(R.color.cardview_shadow_start_color, null);
        mShadowEndColor = resources.getColor(R.color.cardview_shadow_end_color, null);
        mInsetShadow = resources.getDimensionPixelSize(R.dimen.cardview_compat_inset_shadow);
        setBackground(colorStateList);
        mCornerShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mCornerShadowPaint.setStyle(Paint.Style.FILL);
        mCornerRadius = (int) (radius + 0.5f);
        mCardBounds = new RectF();
        mEdgeShadowPaint = new Paint(mCornerShadowPaint);
        mEdgeShadowPaint.setAntiAlias(false);
        setShadowSize(shadowSize, maxShadowSize);
    }

    public static float calculateHorizontalPadding(float maxShadowSize, float cornerRadius,
            boolean addPaddingForCorners) {
        return addPaddingForCorners
                ? (float) (maxShadowSize + ((1.0d - COS_45) * cornerRadius))
                : maxShadowSize;
    }

    public static float calculateVerticalPadding(float maxShadowSize, float cornerRadius,
            boolean addPaddingForCorners) {
        return addPaddingForCorners
                ? (float) ((maxShadowSize * SHADOW_MULTIPLIER)
                + ((1.0d - COS_45) * cornerRadius))
                : maxShadowSize * SHADOW_MULTIPLIER;
    }

    public static void setRoundRectHelper(RoundRectHelper helper) {
        sRoundRectHelper = helper;
    }

    private void buildComponents(Rect bounds) {
        float verticalOffset = SHADOW_MULTIPLIER * mRawMaxShadowSize;
        mCardBounds.set(bounds.left + mRawMaxShadowSize, bounds.top + verticalOffset,
                bounds.right - mRawMaxShadowSize, bounds.bottom - verticalOffset);
        buildShadowCorners();
    }

    private void buildShadowCorners() {
        RectF innerBounds = new RectF(-mCornerRadius, -mCornerRadius, mCornerRadius, mCornerRadius);
        RectF outerBounds = new RectF(innerBounds);
        outerBounds.inset(-mShadowSize, -mShadowSize);
        if (mCornerShadowPath == null) {
            mCornerShadowPath = new Path();
        } else {
            mCornerShadowPath.reset();
        }
        mCornerShadowPath.setFillType(Path.FillType.EVEN_ODD);
        mCornerShadowPath.moveTo(-mCornerRadius, 0.0f);
        mCornerShadowPath.rLineTo(-mShadowSize, 0.0f);
        mCornerShadowPath.arcTo(outerBounds, 180.0f, 90.0f, false);
        mCornerShadowPath.arcTo(innerBounds, 270.0f, -90.0f, false);
        mCornerShadowPath.close();
        float startRatio = mCornerRadius / (mShadowSize + mCornerRadius);
        mCornerShadowPaint.setShader(new RadialGradient(0.0f, 0.0f,
                mCornerRadius + mShadowSize,
                new int[]{mShadowStartColor, mShadowStartColor, mShadowEndColor},
                new float[]{0.0f, startRatio, 1.0f}, Shader.TileMode.CLAMP));
        mEdgeShadowPaint.setShader(new LinearGradient(0.0f, -mCornerRadius + mShadowSize,
                0.0f, -mCornerRadius - mShadowSize,
                new int[]{mShadowStartColor, mShadowStartColor, mShadowEndColor},
                new float[]{0.0f, 0.5f, 1.0f}, Shader.TileMode.CLAMP));
        mEdgeShadowPaint.setAntiAlias(false);
    }

    private void drawShadow(Canvas canvas) {
        float edgeShadowTop = -mCornerRadius - mShadowSize;
        float inset = mCornerRadius + mInsetShadow + (mRawShadowSize / 2.0f);
        float doubleInset = inset * 2.0f;
        boolean drawHorizontalEdges = mCardBounds.width() - doubleInset > 0.0f;
        boolean drawVerticalEdges = mCardBounds.height() - doubleInset > 0.0f;

        int saved = canvas.save();
        canvas.translate(mCardBounds.left + inset, mCardBounds.top + inset);
        canvas.drawPath(mCornerShadowPath, mCornerShadowPaint);
        if (drawHorizontalEdges) {
            canvas.drawRect(0.0f, edgeShadowTop, mCardBounds.width() - doubleInset,
                    -mCornerRadius, mEdgeShadowPaint);
        }
        canvas.restoreToCount(saved);

        saved = canvas.save();
        canvas.translate(mCardBounds.right - inset, mCardBounds.bottom - inset);
        canvas.rotate(180.0f);
        canvas.drawPath(mCornerShadowPath, mCornerShadowPaint);
        if (drawHorizontalEdges) {
            canvas.drawRect(0.0f, edgeShadowTop, mCardBounds.width() - doubleInset,
                    -mCornerRadius + mShadowSize, mEdgeShadowPaint);
        }
        canvas.restoreToCount(saved);

        saved = canvas.save();
        canvas.translate(mCardBounds.left + inset, mCardBounds.bottom - inset);
        canvas.rotate(270.0f);
        canvas.drawPath(mCornerShadowPath, mCornerShadowPaint);
        if (drawVerticalEdges) {
            canvas.drawRect(0.0f, edgeShadowTop, mCardBounds.height() - doubleInset,
                    -mCornerRadius, mEdgeShadowPaint);
        }
        canvas.restoreToCount(saved);

        saved = canvas.save();
        canvas.translate(mCardBounds.right - inset, mCardBounds.top + inset);
        canvas.rotate(90.0f);
        canvas.drawPath(mCornerShadowPath, mCornerShadowPaint);
        if (drawVerticalEdges) {
            canvas.drawRect(0.0f, edgeShadowTop, mCardBounds.height() - doubleInset,
                    -mCornerRadius, mEdgeShadowPaint);
        }
        canvas.restoreToCount(saved);
    }

    private void setBackground(ColorStateList colorStateList) {
        if (colorStateList == null) {
            colorStateList = ColorStateList.valueOf(0);
        }
        mBackground = colorStateList;
        mPaint.setColor(colorStateList.getColorForState(getState(), mBackground.getDefaultColor()));
    }

    private void setShadowSize(float shadowSize, float maxShadowSize) {
        if (shadowSize < 0.0f) {
            throw new IllegalArgumentException("Invalid shadow size " + shadowSize + ". Must be >= 0");
        }
        if (maxShadowSize < 0.0f) {
            throw new IllegalArgumentException("Invalid max shadow size " + maxShadowSize + ". Must be >= 0");
        }
        float evenShadowSize = toEven(shadowSize);
        float evenMaxShadowSize = toEven(maxShadowSize);
        if (evenShadowSize > evenMaxShadowSize) {
            if (!mPrintedShadowClipWarning) {
                mPrintedShadowClipWarning = true;
            }
            evenShadowSize = evenMaxShadowSize;
        }
        if (mRawShadowSize == evenShadowSize && mRawMaxShadowSize == evenMaxShadowSize) {
            return;
        }
        mRawShadowSize = evenShadowSize;
        mRawMaxShadowSize = evenMaxShadowSize;
        mShadowSize = (int) ((evenShadowSize * SHADOW_MULTIPLIER) + mInsetShadow + 0.5f);
        mDirty = true;
        invalidateSelf();
    }

    private int toEven(float value) {
        int rounded = (int) (value + 0.5f);
        return rounded % 2 == 1 ? rounded - 1 : rounded;
    }

    @Override
    public void draw(Canvas canvas) {
        if (mDirty) {
            buildComponents(getBounds());
            mDirty = false;
        }
        canvas.translate(0.0f, mRawShadowSize / 2.0f);
        drawShadow(canvas);
        canvas.translate(0.0f, -mRawShadowSize / 2.0f);
        sRoundRectHelper.drawRoundRect(canvas, mCardBounds, mCornerRadius, mPaint);
    }

    public ColorStateList getColor() {
        return mBackground;
    }

    public float getCornerRadius() {
        return mCornerRadius;
    }

    public void getMaxShadowAndCornerPadding(Rect rect) {
        getPadding(rect);
    }

    public float getMaxShadowSize() {
        return mRawMaxShadowSize;
    }

    public float getMinHeight() {
        float maxShadowSize = mRawMaxShadowSize;
        return (Math.max(maxShadowSize,
                mCornerRadius + mInsetShadow + ((maxShadowSize * SHADOW_MULTIPLIER) / 2.0f)) * 2.0f)
                + (((mRawMaxShadowSize * SHADOW_MULTIPLIER) + mInsetShadow) * 2.0f);
    }

    public float getMinWidth() {
        float maxShadowSize = mRawMaxShadowSize;
        return (Math.max(maxShadowSize, mCornerRadius + mInsetShadow + (maxShadowSize / 2.0f)) * 2.0f)
                + ((mRawMaxShadowSize + mInsetShadow) * 2.0f);
    }

    @Override
    public int getOpacity() {
        return android.graphics.PixelFormat.TRANSLUCENT;
    }

    @Override
    public boolean getPadding(Rect padding) {
        int vertical = (int) Math.ceil(calculateVerticalPadding(
                mRawMaxShadowSize, mCornerRadius, mAddPaddingForCorners));
        int horizontal = (int) Math.ceil(calculateHorizontalPadding(
                mRawMaxShadowSize, mCornerRadius, mAddPaddingForCorners));
        padding.set(horizontal, vertical, horizontal, vertical);
        return true;
    }

    public float getShadowSize() {
        return mRawShadowSize;
    }

    @Override
    public boolean isStateful() {
        return (mBackground != null && mBackground.isStateful()) || super.isStateful();
    }

    @Override
    public void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        mDirty = true;
    }

    @Override
    public boolean onStateChange(int[] state) {
        int newColor = mBackground.getColorForState(state, mBackground.getDefaultColor());
        if (mPaint.getColor() == newColor) {
            return false;
        }
        mPaint.setColor(newColor);
        mDirty = true;
        invalidateSelf();
        return true;
    }

    public void setAddPaddingForCorners(boolean addPaddingForCorners) {
        mAddPaddingForCorners = addPaddingForCorners;
        invalidateSelf();
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
        mCornerShadowPaint.setAlpha(alpha);
        mEdgeShadowPaint.setAlpha(alpha);
    }

    public void setColor(ColorStateList colorStateList) {
        setBackground(colorStateList);
        invalidateSelf();
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        mPaint.setColorFilter(colorFilter);
    }

    public void setCornerRadius(float radius) {
        if (radius < 0.0f) {
            throw new IllegalArgumentException("Invalid radius " + radius + ". Must be >= 0");
        }
        float rounded = (int) (radius + 0.5f);
        if (mCornerRadius == rounded) {
            return;
        }
        mCornerRadius = rounded;
        mDirty = true;
        invalidateSelf();
    }

    public void setMaxShadowSize(float size) {
        setShadowSize(mRawShadowSize, size);
    }

    public void setShadowSize(float size) {
        setShadowSize(size, mRawMaxShadowSize);
    }
}
