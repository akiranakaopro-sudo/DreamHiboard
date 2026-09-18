package com.coui.appcompat.poplist;

import com.coui.appcompat.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;
import com.coui.appcompat.contextutil.COUIContextUtil;
import com.coui.appcompat.log.COUILog;
import com.coui.appcompat.roundcorner.RoundCornerUtil;
import com.coui.appcompat.uiutil.AnimLevel;
import com.coui.appcompat.uiutil.COUIBackgroundBlurBuilder;
import com.coui.appcompat.uiutil.ShadowUtils;
import com.coui.appcompat.uiutil.UIUtil;
import com.coui.appcompat.version.COUIVersionUtil;
import com.oplus.graphics.OplusOutline;
import com.oplus.graphics.OplusOutlineAdapter;
import com.oplus.graphics.OplusPath;
import com.oplus.graphics.OplusPathAdapter;


public class RoundFrameLayout extends FrameLayout {
    public static final int CANVAS_CLIP = 0;
    private static final int MAX_COLOR = 255;
    private static final float OS_16_1_WEIGHT = 3.0f;
    public static final int OUTLINE_CLIP = 1;
    private static final String TAG = "RoundFrameLayout";
    private boolean mAllowDispatchEvent;
    private COUIBackgroundBlurBuilder mBackgroundBlurBuilder;
    private int mClipMode;
    private OplusPathAdapter mOplusPathAdapter;
    private float mOutlineAlpha;
    private final Rect mOutlineRect;
    private final Rect mOverrideRect;
    private Paint mPaint;
    private Path mPath;
    private float mRadius;
    private int mRadius16dpForOS16_1;
    private int mRadius9dpForOS16_1;
    private RectF mRectF;
    private float mRoundCornerRadius;
    private float mRoundCornerWeight;

    public RoundFrameLayout(Context context) {
        this(context, null);
    }

    private void dispatchDraw27(Canvas canvas) {
        canvas.saveLayer(this.mRectF, null, 31);
        super.dispatchDraw(canvas);
        canvas.drawPath(genPath(), this.mPaint);
        canvas.restore();
    }

    private void dispatchDraw28(Canvas canvas) {
        canvas.save();
        canvas.clipPath(genPath());
        super.dispatchDraw(canvas);
        canvas.restore();
    }


    public boolean execute15SRC() {
        return (RoundCornerUtil.getSmoothStyleType() == 0 && this.mRoundCornerWeight > 0.0f) || this.mBackgroundBlurBuilder.useBackgroundBlur();
    }


    public boolean execute16SRC() {
        return RoundCornerUtil.getSmoothStyleType() == 1;
    }

    private Path genPath() {
        this.mPath.reset();
        float value = this.mRoundCornerRadius;
        if (value == 0.0f) {
            value = this.mRadius;
        }
        float value_2 = value;
        if (!RoundCornerUtil.supportSRCCompatibleBlur(this.mBackgroundBlurBuilder.useBackgroundBlur())) {
            this.mPath.addRoundRect(this.mRectF, value_2, value_2, Path.Direction.CW);
        } else if (execute15SRC()) {
            OplusPath oplusPath = new OplusPath(this.mPath);
            if (COUIVersionUtil.getOSVersionCode() > 37) {
                oplusPath.addSmoothRoundRect(this.mRectF, this.mRadius9dpForOS16_1, this.mRoundCornerWeight, Path.Direction.CW);
            } else {
                oplusPath.addSmoothRoundRect(this.mRectF, value_2, this.mRoundCornerWeight, Path.Direction.CW);
            }
        } else if (execute16SRC()) {
            if (this.mOplusPathAdapter == null) {
                this.mOplusPathAdapter = new OplusPathAdapter(this.mPath, 1);
            }
            if (COUIVersionUtil.getOSVersionCode() > 37) {
                this.mOplusPathAdapter.addSmoothRoundRect(this.mRectF, value_2, this.mRadius16dpForOS16_1, OS_16_1_WEIGHT, Path.Direction.CCW);
            } else {
                this.mOplusPathAdapter.addSmoothRoundRect(this.mRectF, value_2, value_2, Path.Direction.CCW);
            }
        } else {
            this.mPath.addRoundRect(this.mRectF, value_2, value_2, Path.Direction.CW);
        }
        return this.mPath;
    }

    public void clearOverrideOutline() {
        this.mOverrideRect.setEmpty();
        this.mOutlineAlpha = 1.0f;
        invalidateOutline();
    }

    @Override
    public void dispatchDraw(Canvas canvas) {
        if (!this.mOverrideRect.isEmpty()) {
            getBackground().setBounds(this.mOverrideRect);
        }
        dispatchDraw28(canvas);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        if (!this.mAllowDispatchEvent) {
            return false;
        }
        if (this.mOverrideRect.isEmpty() || this.mOverrideRect.contains((int) motionEvent.getX(), (int) motionEvent.getY())) {
            return super.dispatchTouchEvent(motionEvent);
        }
        return false;
    }

    @Override
    public void draw(Canvas canvas) {
        if (!this.mOverrideRect.isEmpty()) {
            getBackground().setBounds(this.mOverrideRect);
        }
        super.draw(canvas);
    }

    public boolean getUseBackgroundBlur() {
        return this.mBackgroundBlurBuilder.useBackgroundBlur();
    }

    public void initUseBackgroundBlur(boolean flag) {
        initUseBackgroundBlur(flag, UIUtil.ANIM_LEVEL_SUPPORT_BLUR_MIN);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isHardwareAccelerated()) {
            COUILog.e(TAG, "Hardware accelerate is disabled! Set background blur failed.");
            return;
        }
        if (this.mBackgroundBlurBuilder.useBackgroundBlur()) {
            this.mBackgroundBlurBuilder.setTargetView(this);
            float value = this.mRoundCornerRadius;
            if (value == 0.0f) {
                value = this.mRadius;
            }
            if (RoundCornerUtil.isSupportRoundCornerWhenBlur()) {
                COUILog.i(TAG, "current version support roundCorner when use blur");
                this.mBackgroundBlurBuilder.setSmoothWeight(COUIContextUtil.getAttrFloat(getContext(), R.attr.couiRoundCornerMWeight));
                if (COUIVersionUtil.getOSVersionCode() > 37) {
                    value = this.mRadius9dpForOS16_1;
                }
            }
            this.mBackgroundBlurBuilder.setCornerRadius(value);
            this.mBackgroundBlurBuilder.applyBlurBackground();
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mBackgroundBlurBuilder.release();
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.mRectF.set(getPaddingLeft(), getPaddingTop(), w - getPaddingRight(), h - getPaddingBottom());
    }

    public void setAllowDispatchEvent(boolean allowDispatchEvent) {
        this.mAllowDispatchEvent = allowDispatchEvent;
    }

    @Override
    public void setAlpha(float alpha) {
        super.setAlpha(alpha);
        if (!this.mBackgroundBlurBuilder.useBackgroundBlur() || getBackground() == null) {
            return;
        }
        getBackground().setAlpha((int) (alpha * 255.0f));
    }

    public void setClipMode(int clipMode) {
        this.mClipMode = clipMode;
        if (clipMode == CANVAS_CLIP) {
            setClipToOutline(false);
            setElevation(0.0f);
            setBackgroundColor(0);
        } else if (clipMode == OUTLINE_CLIP) {
            setClipToOutline(true);
            if (ShadowUtils.checkOPlusViewElevationSDK()) {
                ShadowUtils.setElevationToView(this, 3);
            } else {
                setElevation(getContext().getResources().getDimensionPixelSize(R.dimen.support_shadow_size_level_five));
                setOutlineSpotShadowColor(androidx.core.content.ContextCompat.getColor(getContext(), R.color.coui_popup_outline_spot_shadow_color));
            }
            setBackgroundColor(-1);
        }
    }

    public void setOverrideOutline(int index, int index_2, int index_3, int index_4, float value) {
        this.mOutlineAlpha = value;
        this.mOverrideRect.set(index, index_2, index_3, index_4);
        if (getBackground() != null) {
            getBackground().setBounds(this.mOverrideRect);
        }
        invalidateOutline();
    }

    public void setRadius(float radius) {
        this.mRadius = radius;
        postInvalidate();
    }

    public void setRoundCornerRadius(float roundCornerRadius) {
        this.mRoundCornerRadius = roundCornerRadius;
        postInvalidate();
    }

    public RoundFrameLayout(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public void initUseBackgroundBlur(boolean flag, AnimLevel animLevel) {
        this.mBackgroundBlurBuilder.setUseBackgroundBlur(flag, animLevel);
    }

    public RoundFrameLayout(Context context, AttributeSet attributeSet, int index) {
        super(context, attributeSet, index);
        this.mOutlineRect = new Rect();
        this.mOverrideRect = new Rect();
        this.mOutlineAlpha = 1.0f;
        this.mAllowDispatchEvent = true;
        TypedArray typedArrayObtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.RoundFrameLayout);
        this.mRoundCornerRadius = typedArrayObtainStyledAttributes.getDimension(R.styleable.RoundFrameLayout_couiRoundCornerRadius, 0.0f);
        this.mRadius = typedArrayObtainStyledAttributes.getDimension(R.styleable.RoundFrameLayout_rfRadius, 0.0f);
        this.mClipMode = typedArrayObtainStyledAttributes.getInt(R.styleable.RoundFrameLayout_couiClipType, CANVAS_CLIP);
        this.mRoundCornerWeight = typedArrayObtainStyledAttributes.getFloat(R.styleable.RoundFrameLayout_couirfRoundCornerWeight, 0.0f);
        typedArrayObtainStyledAttributes.recycle();
        this.mRadius16dpForOS16_1 = getResources().getDimensionPixelSize(R.dimen.coui_popup_list_window_os_16_1_radius_16_dp);
        this.mRadius9dpForOS16_1 = getResources().getDimensionPixelSize(R.dimen.coui_popup_list_window_os_16_1_radius_9_dp);
        this.mPath = new Path();
        this.mPaint = new Paint(1);
        this.mRectF = new RectF();
        this.mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                if (RoundFrameLayout.this.mOverrideRect.isEmpty()) {
                    RoundFrameLayout.this.mOutlineRect.set((int) RoundFrameLayout.this.mRectF.left, (int) RoundFrameLayout.this.mRectF.top, (int) RoundFrameLayout.this.mRectF.right, (int) RoundFrameLayout.this.mRectF.bottom);
                } else {
                    outline.setAlpha(RoundFrameLayout.this.mOutlineAlpha);
                    RoundFrameLayout.this.mOutlineRect.set(RoundFrameLayout.this.mOverrideRect);
                }
                float value = RoundFrameLayout.this.mRoundCornerRadius != 0.0f ? RoundFrameLayout.this.mRoundCornerRadius : RoundFrameLayout.this.mRadius;
                if (!RoundCornerUtil.supportSRCCompatibleBlur(RoundFrameLayout.this.mBackgroundBlurBuilder.useBackgroundBlur())) {
                    outline.setRoundRect(RoundFrameLayout.this.mOutlineRect, value);
                    return;
                }
                if (RoundFrameLayout.this.execute15SRC()) {
                    OplusOutline oplusOutline = new OplusOutline(outline);
                    if (COUIVersionUtil.getOSVersionCode() > 37) {
                        oplusOutline.setSmoothRoundRect(RoundFrameLayout.this.mOutlineRect, RoundFrameLayout.this.mRadius9dpForOS16_1, RoundFrameLayout.this.mRoundCornerWeight);
                        return;
                    } else {
                        oplusOutline.setSmoothRoundRect(RoundFrameLayout.this.mOutlineRect, value, RoundFrameLayout.this.mRoundCornerWeight);
                        return;
                    }
                }
                if (!RoundFrameLayout.this.execute16SRC()) {
                    outline.setRoundRect(RoundFrameLayout.this.mOutlineRect, value);
                    return;
                }
                OplusOutlineAdapter oplusOutlineAdapter = new OplusOutlineAdapter(outline, 1);
                if (COUIVersionUtil.getOSVersionCode() > 37) {
                    oplusOutlineAdapter.setSmoothRoundRect(RoundFrameLayout.this.mOutlineRect, RoundFrameLayout.this.mRadius16dpForOS16_1, RoundFrameLayout.OS_16_1_WEIGHT);
                } else {
                    oplusOutlineAdapter.setSmoothRoundRect(RoundFrameLayout.this.mOutlineRect, value);
                }
            }
        });
        setClipMode(this.mClipMode);
        setDefaultFocusHighlightEnabled(false);
        COUIBackgroundBlurBuilder cOUIBackgroundBlurBuilder = new COUIBackgroundBlurBuilder(getContext());
        this.mBackgroundBlurBuilder = cOUIBackgroundBlurBuilder;
        cOUIBackgroundBlurBuilder.setMixColorLight(UIUtil.colorToFloats(COUIContextUtil.getColor(getContext(), R.color.coui_popup_list_mix_blur_light)));
        this.mBackgroundBlurBuilder.setMixColorDark(UIUtil.colorToFloats(COUIContextUtil.getColor(getContext(), R.color.coui_popup_list_mix_blur_dark)));
        this.mBackgroundBlurBuilder.setBlendColorLight(UIUtil.colorToFloats(COUIContextUtil.getColor(getContext(), R.color.coui_popup_list_blend_blur_light)));
        this.mBackgroundBlurBuilder.setBlendColorDark(UIUtil.colorToFloats(COUIContextUtil.getColor(getContext(), R.color.coui_popup_list_blend_blur_dark)));
    }
}
