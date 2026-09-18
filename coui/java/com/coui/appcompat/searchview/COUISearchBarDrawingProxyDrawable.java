package com.coui.appcompat.searchview;


import com.coui.appcompat.R;
import android.animation.ArgbEvaluator;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import com.coui.appcompat.roundRect.COUIShapePath;
import com.coui.appcompat.roundcorner.RoundCornerUtil;
import com.oplus.graphics.OplusPathAdapter;


class COUISearchBarDrawingProxyDrawable extends Drawable {
    private static final ArgbEvaluator DEFAULT_EVALUATOR = new ArgbEvaluator();
    private int mButtonDividerColor;
    private int mCurrentBackgroundColor;
    private float mDividerAlpha;
    private final Rect mDividerRect = new Rect();
    private final Path mNormalBackgroundPath;
    private final Paint mNormalPaint;
    private OplusPathAdapter mOplusPathAdapter;
    private final Paint mPressFeedbackPaint;
    private RectF mSmoothRoundRect;

    public COUISearchBarDrawingProxyDrawable() {
        Path path = new Path();
        this.mNormalBackgroundPath = path;
        this.mPressFeedbackPaint = new Paint(1);
        this.mNormalPaint = new Paint(1);
        this.mDividerAlpha = 0.0f;
        if (isOs16()) {
            this.mOplusPathAdapter = new OplusPathAdapter(path, 1);
            this.mSmoothRoundRect = new RectF();
        }
    }

    private boolean isOs16() {
        return RoundCornerUtil.getSmoothStyleType() == 1;
    }

    @Override
    public void draw(Canvas canvas) {
        if (isOs16()) {
            canvas.save();
            canvas.clipPath(this.mNormalBackgroundPath);
            this.mPressFeedbackPaint.setStyle(Paint.Style.FILL);
            this.mPressFeedbackPaint.setColor(this.mCurrentBackgroundColor);
            canvas.drawColor(this.mPressFeedbackPaint.getColor());
            canvas.restore();
        } else {
            this.mPressFeedbackPaint.setStyle(Paint.Style.FILL);
            this.mPressFeedbackPaint.setColor(this.mCurrentBackgroundColor);
            canvas.drawPath(this.mNormalBackgroundPath, this.mPressFeedbackPaint);
        }
        this.mNormalPaint.setColor(((Integer) DEFAULT_EVALUATOR.evaluate(this.mDividerAlpha, 0, Integer.valueOf(this.mButtonDividerColor))).intValue());
        canvas.drawRect(this.mDividerRect, this.mNormalPaint);
    }

    public int getCurrentBackgroundColor() {
        return this.mCurrentBackgroundColor;
    }

    public Path getNormalBackgroundPath() {
        return this.mNormalBackgroundPath;
    }

    @Override
    public int getOpacity() {
        return -3;
    }

    @Override
    public void setAlpha(int alpha) {
    }

    public void setBackgroundRect(Rect rect) {
        if (isOs16()) {
            this.mSmoothRoundRect.set(rect);
            this.mNormalBackgroundPath.reset();
            OplusPathAdapter oplusPathAdapter = this.mOplusPathAdapter;
            RectF rectF = this.mSmoothRoundRect;
            oplusPathAdapter.addSmoothRoundRect(rectF, rectF.height() / 2.0f, this.mSmoothRoundRect.height() / 2.0f, Path.Direction.CCW);
        } else {
            COUIShapePath.getRoundRectPath(this.mNormalBackgroundPath, new RectF(rect), (rect.bottom - rect.top) / 2.0f, true, true, true, true);
        }
        invalidateSelf();
    }

    public void setButtonDividerColor(int color) {
        this.mButtonDividerColor = color;
        invalidateSelf();
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        invalidateSelf();
    }

    public void setCurrentBackgroundColor(int color) {
        this.mCurrentBackgroundColor = color;
        invalidateSelf();
    }

    public void setDividerAlpha(float alpha) {
        this.mDividerAlpha = alpha;
        invalidateSelf();
    }

    public void setDividerRect(Rect rect) {
        this.mDividerRect.set(rect);
        invalidateSelf();
    }
}
