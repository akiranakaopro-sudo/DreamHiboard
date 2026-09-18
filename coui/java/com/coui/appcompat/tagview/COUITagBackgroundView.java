package com.coui.appcompat.tagview;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.LinearLayout;

import com.coui.appcompat.R;
import com.coui.appcompat.theme.COUIThemeUtils;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.shape.ShapeAppearancePathProvider;

public class COUITagBackgroundView extends LinearLayout {
    private boolean isChangedShapeModel;
    private int mCardBLCornerRadius;
    private int mCardBRCornerRadius;
    private int mCardCornerRadius;
    private int mCardTLCornerRadius;
    private int mCardTRCornerRadius;
    private final Path mClipPath;
    private final RectF mClipRectF;
    private ColorStateList mColorStateList;
    private MaterialShapeDrawable mMaterialShapeDrawable;
    private Paint mPaint;
    private ShapeAppearanceModel mShapeAppearanceModel;
    private int mStrokeColor;
    private ColorStateList mStrokeStateColor;
    private float mStrokeWidth;

    public COUITagBackgroundView(Context context) {
        this(context, null);
    }

    public COUITagBackgroundView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public COUITagBackgroundView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mStrokeWidth = 0.0f;
        this.mStrokeColor = 0;
        this.mStrokeStateColor = ColorStateList.valueOf(0);
        this.mClipPath = new Path();
        this.mClipRectF = new RectF();
        this.isChangedShapeModel = true;

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.COUITagBackgroundView);
        int cornerRadius = typedArray.getDimensionPixelSize(R.styleable.COUITagBackgroundView_couiTagCornerRadius, 0);
        this.mCardCornerRadius = cornerRadius;
        this.mCardTLCornerRadius = typedArray.getDimensionPixelSize(R.styleable.COUITagBackgroundView_couiTagTLCornerRadius, cornerRadius);
        this.mCardTRCornerRadius = typedArray.getDimensionPixelSize(R.styleable.COUITagBackgroundView_couiTagTRCornerRadius, this.mCardCornerRadius);
        this.mCardBLCornerRadius = typedArray.getDimensionPixelSize(R.styleable.COUITagBackgroundView_couiTagBLCornerRadius, this.mCardCornerRadius);
        this.mCardBRCornerRadius = typedArray.getDimensionPixelSize(R.styleable.COUITagBackgroundView_couiTagBRCornerRadius, this.mCardCornerRadius);
        this.mColorStateList = typedArray.getColorStateList(R.styleable.COUITagBackgroundView_couiTagBackgroundColor);
        if (this.mColorStateList == null) {
            this.mColorStateList = ColorStateList.valueOf(COUIThemeUtils.getThemeAttrColor(context, R.attr.couiColorBackgroundWithTag));
        }
        this.mStrokeStateColor = typedArray.getColorStateList(R.styleable.COUITagBackgroundView_couiTagStrokeColor);
        if (this.mStrokeStateColor == null) {
            this.mStrokeStateColor = ColorStateList.valueOf(0);
        }
        Paint paint = new Paint(1);
        this.mPaint = paint;
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        this.mStrokeWidth = typedArray.getDimensionPixelSize(R.styleable.COUITagBackgroundView_couiTagStrokeWidth, 0);
        generateModel();
        initDrawable();
        setupDrawable();
        typedArray.recycle();
    }

    private void dispatchDrawInner(Canvas canvas) {
        canvas.save();
        canvas.clipPath(this.mClipPath);
        super.dispatchDraw(canvas);
        canvas.restore();
    }

    private void generateModel() {
        this.mShapeAppearanceModel = new ShapeAppearanceModel.Builder()
                .setTopRightCorner(0, this.mCardTRCornerRadius)
                .setBottomRightCorner(0, this.mCardBRCornerRadius)
                .setTopLeftCorner(0, this.mCardTLCornerRadius)
                .setBottomLeftCorner(0, this.mCardBLCornerRadius)
                .build();
        this.isChangedShapeModel = true;
    }

    private void initDrawable() {
        MaterialShapeDrawable materialShapeDrawable = this.mMaterialShapeDrawable;
        if (materialShapeDrawable == null) {
            this.mMaterialShapeDrawable = new MaterialShapeDrawable(this.mShapeAppearanceModel);
        } else {
            materialShapeDrawable.setShapeAppearanceModel(this.mShapeAppearanceModel);
        }
        this.mMaterialShapeDrawable.setShadowCompatibilityMode(2);
        this.mMaterialShapeDrawable.initializeElevationOverlay(getContext());
        this.mMaterialShapeDrawable.setFillColor(this.mColorStateList);
        this.mMaterialShapeDrawable.setStroke(this.mStrokeWidth, this.mStrokeStateColor);
    }

    private void setupDrawable() {
        setBackground(this.mMaterialShapeDrawable);
    }

    @Override
    public void dispatchDraw(Canvas canvas) {
        if (this.isChangedShapeModel) {
            this.mClipRectF.set(getBackground().getBounds());
            ShapeAppearancePathProvider.getInstance().calculatePath(this.mShapeAppearanceModel, 1.0f, this.mClipRectF, this.mClipPath);
            this.isChangedShapeModel = false;
        }
        dispatchDrawInner(canvas);
    }

    public int getCardBLCornerRadius() {
        return this.mCardBLCornerRadius;
    }

    public int getCardBRCornerRadius() {
        return this.mCardBRCornerRadius;
    }

    public int getCardCornerRadius() {
        return this.mCardCornerRadius;
    }

    public int getCardTLCornerRadius() {
        return this.mCardTLCornerRadius;
    }

    public int getCardTRCornerRadius() {
        return this.mCardTRCornerRadius;
    }

    public ColorStateList getColorStateList() {
        return this.mColorStateList;
    }

    public MaterialShapeDrawable getMaterialShapeDrawable() {
        return this.mMaterialShapeDrawable;
    }

    public int getStrokeColor() {
        return this.mStrokeColor;
    }

    public ColorStateList getStrokeStateColor() {
        return this.mStrokeStateColor;
    }

    public float getStrokeWidth() {
        return this.mStrokeWidth;
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        ViewParent parent = getParent();
        if (parent != null) {
            ((ViewGroup) parent).setClipChildren(false);
        }
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        this.isChangedShapeModel = true;
    }

    public void setCardBLCornerRadius(int radius) {
        this.mCardBLCornerRadius = radius;
        generateModel();
        initDrawable();
        setupDrawable();
    }

    public void setCardBRCornerRadius(int radius) {
        this.mCardBRCornerRadius = radius;
        generateModel();
        initDrawable();
        setupDrawable();
    }

    public void setCardCornerRadius(int radius) {
        this.mCardCornerRadius = radius;
        this.mCardBLCornerRadius = radius;
        this.mCardBRCornerRadius = radius;
        this.mCardTLCornerRadius = radius;
        this.mCardTRCornerRadius = radius;
        generateModel();
        initDrawable();
        setupDrawable();
    }

    public void setCardTLCornerRadius(int radius) {
        this.mCardTLCornerRadius = radius;
        generateModel();
        initDrawable();
        setupDrawable();
    }

    public void setCardTRCornerRadius(int radius) {
        this.mCardTRCornerRadius = radius;
        generateModel();
        initDrawable();
        setupDrawable();
    }

    public void setColorStateList(ColorStateList colorStateList) {
        this.mColorStateList = colorStateList;
        generateModel();
        initDrawable();
        setupDrawable();
    }

    public void setStrokeColor(int color) {
        this.mStrokeColor = color;
        setStrokeStateColor(ColorStateList.valueOf(color));
    }

    public void setStrokeStateColor(ColorStateList colorStateList) {
        this.mStrokeStateColor = colorStateList;
        generateModel();
        initDrawable();
        setupDrawable();
    }

    public void setStrokeWidth(float strokeWidth) {
        this.mStrokeWidth = strokeWidth;
        generateModel();
        initDrawable();
        setupDrawable();
    }
}
