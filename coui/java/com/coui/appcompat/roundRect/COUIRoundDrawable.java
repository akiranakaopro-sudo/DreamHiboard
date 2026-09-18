package com.coui.appcompat.roundRect;

import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
public class COUIRoundDrawable extends Drawable {

    private COUIRoundDrawableState drawableState;
    private final Paint fillPaint;
    private Path fillPath;
    private boolean pathDirty;
    private final RectF rectF;
    private final Paint strokePaint;
    private Path strokePath;
    private PorterDuffColorFilter strokeTintFilter;
    private PorterDuffColorFilter tintFilter;

    public COUIRoundDrawable() {
        this(new COUIRoundDrawableState());
    }

    public COUIRoundDrawable(COUIRoundDrawableState drawableState) {
        this.fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.rectF = new RectF();
        this.fillPath = new Path();
        this.strokePath = new Path();
        this.drawableState = drawableState;
        this.fillPaint.setStyle(Paint.Style.FILL);
        this.strokePaint.setStyle(Paint.Style.STROKE);
    }

    private void calculatePath() {
        this.fillPath = COUIShapePath.getRoundRectPath(
                this.fillPath,
                getBoundsAsRectF(),
                this.drawableState.radius
        );
    }

    private void calculateStrokePath() {
        this.strokePath = COUIShapePath.getRoundRectPath(
                this.strokePath,
                getBoundsAsRectF(),
                this.drawableState.radius
        );
    }

    private PorterDuffColorFilter calculateTintFilter(ColorStateList tintList, PorterDuff.Mode tintMode) {
        if (tintList == null || tintMode == null) {
            return null;
        }
        return new PorterDuffColorFilter(
                tintList.getColorForState(getState(), 0),
                tintMode
        );
    }

    private boolean hasFill() {
        return !((this.fillPaint == null || this.fillPaint.getColor() == 0)
                && this.tintFilter == null);
    }

    private boolean hasStroke() {
        return !((this.strokePaint == null
                || this.strokePaint.getStrokeWidth() <= 0.0f
                || this.strokePaint.getColor() == 0)
                && this.strokeTintFilter == null);
    }

    private static int modulateAlpha(int paintAlpha, int drawableAlpha) {
        return (paintAlpha * (drawableAlpha + (drawableAlpha >>> 7))) >>> 8;
    }

    private boolean updateColorsForState(int[] stateSet) {
        boolean changed = false;

        if (this.drawableState.fillColor != null) {
            int currentFillColor = this.fillPaint.getColor();
            int newFillColor = this.drawableState.fillColor.getColorForState(
                    stateSet,
                    currentFillColor
            );

            if (currentFillColor != newFillColor) {
                this.fillPaint.setColor(newFillColor);
                changed = true;
            }
        }

        if (this.drawableState.strokeColor != null) {
            int currentStrokeColor = this.strokePaint.getColor();
            int newStrokeColor = this.drawableState.strokeColor.getColorForState(
                    stateSet,
                    currentStrokeColor
            );

            if (currentStrokeColor != newStrokeColor) {
                this.strokePaint.setColor(newStrokeColor);
                changed = true;
            }
        }

        return changed;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        this.fillPaint.setColorFilter(this.tintFilter);

        int originalFillAlpha = this.fillPaint.getAlpha();
        this.fillPaint.setAlpha(modulateAlpha(originalFillAlpha, this.drawableState.alpha));

        this.strokePaint.setStrokeWidth(this.drawableState.strokeWidth);
        this.strokePaint.setColorFilter(this.strokeTintFilter);

        int originalStrokeAlpha = this.strokePaint.getAlpha();
        this.strokePaint.setAlpha(modulateAlpha(originalStrokeAlpha, this.drawableState.alpha));

        if (this.pathDirty) {
            calculateStrokePath();
            calculatePath();
            this.pathDirty = false;
        }

        if (hasFill()) {
            canvas.drawPath(this.fillPath, this.fillPaint);
        }

        if (hasStroke()) {
            canvas.drawPath(this.strokePath, this.strokePaint);
        }

        this.fillPaint.setAlpha(originalFillAlpha);
        this.strokePaint.setAlpha(originalStrokeAlpha);
    }

    public RectF getBoundsAsRectF() {
        this.rectF.set(getBounds());
        return this.rectF;
    }

    @Override
    public Drawable.ConstantState getConstantState() {
        return this.drawableState;
    }

    public ColorStateList getFillColor() {
        return this.drawableState.fillColor;
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    public void invalidateIgnoreCalculate() {
        this.pathDirty = false;
        super.invalidateSelf();
    }

    @Override
    public void invalidateSelf() {
        this.pathDirty = true;
        super.invalidateSelf();
    }

    @Override
    public boolean isStateful() {
        ColorStateList tintList = this.drawableState.tintList;
        ColorStateList strokeTintList = this.drawableState.strokeTintList;
        ColorStateList strokeColor = this.drawableState.strokeColor;
        ColorStateList fillColor = this.drawableState.fillColor;

        return super.isStateful()
                || (tintList != null && tintList.isStateful())
                || (strokeTintList != null && strokeTintList.isStateful())
                || (strokeColor != null && strokeColor.isStateful())
                || (fillColor != null && fillColor.isStateful());
    }

    @NonNull
    @Override
    public Drawable mutate() {
        this.drawableState = new COUIRoundDrawableState(this.drawableState);
        return this;
    }

    @Override
    public void onBoundsChange(@NonNull Rect bounds) {
        this.pathDirty = true;
        super.onBoundsChange(bounds);
    }

    @Override
    public boolean onStateChange(@NonNull int[] stateSet) {
        boolean changed = updateColorsForState(stateSet);
        if (changed) {
            invalidateSelf();
        }
        return changed;
    }

    @Override
    public void setAlpha(int alpha) {
        if (this.drawableState.alpha != alpha) {
            this.drawableState.alpha = alpha;
            invalidateSelf();
        }
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        if (this.drawableState.colorFilter != colorFilter) {
            this.drawableState.colorFilter = colorFilter;
            invalidateSelf();
        }
    }

    public void setFillColor(ColorStateList fillColor) {
        if (this.drawableState.fillColor != fillColor) {
            this.drawableState.fillColor = fillColor;
            onStateChange(getState());
        }
    }

    public void setFillColor(int fillColor) {
        setFillColor(ColorStateList.valueOf(fillColor));
    }

    public void setRadius(float radius) {
        this.drawableState.radius = radius;
    }

    public void setStroke(float strokeWidth, int strokeColor) {
        setStroke(strokeWidth, ColorStateList.valueOf(strokeColor));
    }

    public void setStroke(float strokeWidth, ColorStateList strokeColor) {
        if (this.drawableState.strokeWidth == strokeWidth
                && this.drawableState.strokeColor == strokeColor) {
            return;
        }

        this.drawableState.strokeWidth = strokeWidth;
        this.drawableState.strokeColor = strokeColor;

        if (!onStateChange(getState())) {
            invalidateSelf();
        }
    }

    @Override
    public void setTint(int tintColor) {
        setTintList(ColorStateList.valueOf(tintColor));
    }

    @Override
    public void setTintList(ColorStateList tintList) {
        this.drawableState.tintList = tintList;

        PorterDuffColorFilter tintColorFilter = calculateTintFilter(
                tintList,
                this.drawableState.tintMode
        );

        this.strokeTintFilter = tintColorFilter;
        this.tintFilter = tintColorFilter;

        invalidateIgnoreCalculate();
    }

    @Override
    public void setTintMode(PorterDuff.Mode tintMode) {
        this.drawableState.tintMode = tintMode;

        PorterDuffColorFilter tintColorFilter = calculateTintFilter(
                this.drawableState.tintList,
                tintMode
        );

        this.strokeTintFilter = tintColorFilter;
        this.tintFilter = tintColorFilter;

        invalidateIgnoreCalculate();
    }

    public static final class COUIRoundDrawableState extends Drawable.ConstantState {
        public int alpha;
        public ColorFilter colorFilter;
        public ColorStateList fillColor;
        public float radius;
        public ColorStateList strokeColor;
        public ColorStateList strokeTintList;
        public float strokeWidth;
        public ColorStateList tintList;
        public PorterDuff.Mode tintMode;

        public COUIRoundDrawableState() {
            this.colorFilter = null;
            this.fillColor = null;
            this.strokeColor = null;
            this.strokeTintList = null;
            this.tintList = null;
            this.tintMode = PorterDuff.Mode.SRC_IN;
            this.alpha = 255;
        }

        public COUIRoundDrawableState(COUIRoundDrawableState drawableState) {
            this.colorFilter = null;
            this.fillColor = null;
            this.strokeColor = null;
            this.strokeTintList = null;
            this.tintList = null;
            this.tintMode = PorterDuff.Mode.SRC_IN;
            this.alpha = 255;

            this.colorFilter = drawableState.colorFilter;
            this.fillColor = drawableState.fillColor;
            this.strokeColor = drawableState.strokeColor;
            this.strokeTintList = drawableState.strokeTintList;
            this.tintList = drawableState.tintList;
            this.strokeWidth = drawableState.strokeWidth;
            this.radius = drawableState.radius;
        }

        @Override
        public int getChangingConfigurations() {
            return 0;
        }

        @NonNull
        @Override
        public Drawable newDrawable() {
            COUIRoundDrawable drawable = new COUIRoundDrawable(this);
            drawable.pathDirty = true;
            return drawable;
        }
    }
}
