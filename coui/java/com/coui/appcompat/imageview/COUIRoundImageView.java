package com.coui.appcompat.imageview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatImageView;

import com.coui.appcompat.R;
import com.coui.appcompat.roundRect.COUIRoundRectUtil;

public class COUIRoundImageView extends AppCompatImageView {
    private static final int CIRCLE = 0;
    private static final int ROUND = 1;
    private static final int SHADOW = 2;
    public static final int ICON_SMALL = 1;
    public static final int ICON_MEDIUM = 2;
    public static final int ICON_LARGE = 3;

    private Bitmap mBitmap;
    private BitmapShader mBitmapShader;
    private int mBitmapWidth;
    private int mBitmapHeight;
    private final Paint mBitmapPaint;
    private Paint mBorderPaint;
    private int mBorderRadius;
    private int mBorderWidth = 2;
    private final RectF mBorderRect;
    private final Context mContext;
    private Drawable mDrawable;
    private boolean mHasDefaultPic;
    private int mOutCircleColor;
    private final Paint mOutCircle;
    private RectF mOutBorderRect;
    private int mRefreshStyle;
    private float mRadius;
    private RectF mRoundRect;
    private float mScale;
    private Bitmap mShadowBitmap;
    private BitmapShader mShadowBitmapShader;
    private int mShadowBorderWidth;
    private Drawable mShadowDrawable;
    private int mShadowDrawableHeight;
    private int mShadowDrawableWidth;
    private final RectF mShadowInsideRect;
    private int mSourceDrawableHeight;
    private int mSourceDrawableWidth;
    private final Matrix mMatrix;
    private int mType;
    private boolean mHasBorder;
    private int mWidth;

    public COUIRoundImageView(Context context) {
        this(context, null);
    }

    public COUIRoundImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public COUIRoundImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        if (attrs != null) {
            mRefreshStyle = attrs.getStyleAttribute();
        }
        mShadowInsideRect = new RectF();
        mBorderRect = new RectF();
        mMatrix = new Matrix();
        mBitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBitmapPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
        initBorderPaint();
        mOutCircle = new Paint(Paint.ANTI_ALIAS_FLAG);
        mOutCircle.setStrokeWidth(mBorderWidth);
        mOutCircle.setStyle(Paint.Style.STROKE);
        mShadowDrawable = context.getResources().getDrawable(R.drawable.coui_round_image_view_shadow, context.getTheme());
        mShadowDrawableWidth = mShadowDrawable.getIntrinsicWidth();
        mShadowDrawableHeight = mShadowDrawable.getIntrinsicHeight();
        int srcWidth = (int) context.getResources().getDimension(R.dimen.coui_roundimageView_src_width);
        mSourceDrawableWidth = srcWidth;
        mSourceDrawableHeight = srcWidth;
        mWidth = getResources().getDimensionPixelSize(R.dimen.coui_roundimageview_default_radius);
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.COUIRoundImageView);
            mBorderRadius = a.getDimensionPixelSize(R.styleable.COUIRoundImageView_couiBorderRadius,
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.0f, getResources().getDisplayMetrics()));
            mType = a.getInt(R.styleable.COUIRoundImageView_couiType, 0);
            mHasBorder = a.getBoolean(R.styleable.COUIRoundImageView_couiHasBorder, false);
            mHasDefaultPic = a.getBoolean(R.styleable.COUIRoundImageView_couiHasDefaultPic, true);
            mOutCircleColor = a.getColor(R.styleable.COUIRoundImageView_couiRoundImageViewOutCircleColor,
                    getResources().getColor(R.color.coui_roundimageview_outcircle_color_dark, context.getTheme()));
            mOutCircle.setColor(mOutCircleColor);
            a.recycle();
        } else {
            mOutCircleColor = getResources().getColor(R.color.coui_roundimageview_outcircle_color, context.getTheme());
            mOutCircle.setColor(mOutCircleColor);
        }
        initShadow();
        setupShader(getDrawable());
    }

    private void initBorderPaint() {
        mBorderPaint = new Paint();
        mBorderPaint.setStrokeWidth(mBorderWidth);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setAntiAlias(true);
        mBorderPaint.setColor(getResources().getColor(R.color.coui_border, getContext().getTheme()));
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        int height = Math.max(1, drawable.getIntrinsicHeight());
        int width = Math.max(1, drawable.getIntrinsicWidth());
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        return bitmap;
    }

    private void setupShader(Drawable drawable) {
        if (drawable == null) {
            return;
        }
        Drawable currentDrawable = getDrawable();
        mDrawable = currentDrawable;
        if (currentDrawable == null || drawable == null) {
            return;
        }
        if (currentDrawable != drawable) {
            mDrawable = drawable;
        }
        mBitmapWidth = mDrawable.getIntrinsicWidth();
        mBitmapHeight = mDrawable.getIntrinsicHeight();
        mBitmap = drawableToBitmap(mDrawable);
        if (mType == SHADOW) {
            mShadowBitmap = createBitmapWithShadow();
            Shader.TileMode tileMode = Shader.TileMode.CLAMP;
            mShadowBitmapShader = new BitmapShader(mShadowBitmap, tileMode, tileMode);
        }
        if (mBitmap == null || mBitmap.isRecycled()) {
            return;
        }
        Shader.TileMode tileMode = Shader.TileMode.CLAMP;
        mBitmapShader = new BitmapShader(mBitmap, tileMode, tileMode);
    }

    private void updateShaderMatrix() {
        mMatrix.reset();
        float scaleX = (mSourceDrawableWidth * 1.0f) / mBitmapWidth;
        float scaleY = (mSourceDrawableHeight * 1.0f) / mBitmapHeight;
        if (scaleX <= 1.0f) {
            scaleX = 1.0f;
        }
        float scale = Math.max(scaleX, scaleY > 1.0f ? scaleY : 1.0f);
        float dx = (mSourceDrawableWidth - (mBitmapWidth * scale)) * 0.5f;
        float dy = (mSourceDrawableHeight - (mBitmapHeight * scale)) * 0.5f;
        mMatrix.setScale(scale, scale);
        mMatrix.postTranslate(((int) (dx + 0.5f)) + (mShadowBorderWidth / 2.0f),
                ((int) (dy + 0.5f)) + (mShadowBorderWidth / 2.0f));
    }

    public Bitmap createBitmapWithShadow() {
        updateShaderMatrix();
        Shader.TileMode tileMode = Shader.TileMode.CLAMP;
        BitmapShader shader = new BitmapShader(mBitmap, tileMode, tileMode);
        mShadowBitmapShader = shader;
        shader.setLocalMatrix(mMatrix);
        mBitmapPaint.setShader(mShadowBitmapShader);
        Bitmap bitmap = Bitmap.createBitmap(mShadowDrawableWidth, mShadowDrawableHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        mBorderRadius = mSourceDrawableWidth / 2;
        canvas.drawPath(COUIRoundRectUtil.getInstance().getPath(mShadowInsideRect, mBorderRadius), mBitmapPaint);
        mShadowDrawable.setBounds(0, 0, mShadowDrawableWidth, mShadowDrawableHeight);
        mShadowDrawable.draw(canvas);
        return bitmap;
    }

    public void initShadow() {
        mBorderRect.set(0.0f, 0.0f, mShadowDrawableWidth, mShadowDrawableHeight);
        mShadowBorderWidth = mShadowDrawableWidth - mSourceDrawableWidth;
        mShadowInsideRect.set(mBorderRect);
        mShadowInsideRect.inset(mShadowBorderWidth / 2, mShadowBorderWidth / 2);
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (mDrawable != null) {
            mDrawable.setState(getDrawableState());
            setupShader(mDrawable);
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mScale = 1.0f;
        if (mBitmap != null && !mBitmap.isRecycled()) {
            if (mType == CIRCLE) {
                int bitmapSize = Math.min(mBitmap.getWidth(), mBitmap.getHeight());
                mScale = (mWidth * 1.0f) / bitmapSize;
            } else if (mType == ROUND) {
                mScale = Math.max((getWidth() * 1.0f) / mBitmap.getWidth(), (getHeight() * 1.0f) / mBitmap.getHeight());
            } else if (mType == SHADOW) {
                mScale = Math.max((getWidth() * 1.0f) / mShadowDrawableWidth, (getHeight() * 1.0f) / mShadowDrawableHeight);
                mMatrix.reset();
                mMatrix.setScale(mScale, mScale);
                mShadowBitmapShader.setLocalMatrix(mMatrix);
                mBitmapPaint.setShader(mShadowBitmapShader);
                canvas.drawRect(mRoundRect, mBitmapPaint);
                return;
            }
            float dx = getWidth() < mBitmap.getWidth() * mScale ? (getWidth() - (mBitmap.getWidth() * mScale)) / 2.0f : 0.0f;
            float dy = getHeight() < mBitmap.getHeight() * mScale ? (getHeight() - (mBitmap.getHeight() * mScale)) / 2.0f : 0.0f;
            mMatrix.setScale(mScale, mScale);
            mMatrix.postTranslate(dx, dy);
            if (mBitmapShader != null) {
                mBitmapShader.setLocalMatrix(mMatrix);
                mBitmapPaint.setShader(mBitmapShader);
            }
        }
        if (mType == CIRCLE) {
            canvas.drawCircle(mRadius, mRadius, mRadius, mBitmapPaint);
            if (mHasBorder) {
                canvas.drawCircle(mRadius, mRadius, mRadius - 0.5f, mOutCircle);
            }
        } else if (mType == ROUND) {
            if (mRoundRect == null) {
                mRoundRect = new RectF(0.0f, 0.0f, getWidth(), getHeight());
            }
            if (mOutBorderRect == null) {
                mOutBorderRect = new RectF(mBorderWidth / 2.0f, mBorderWidth / 2.0f,
                        getWidth() - (mBorderWidth / 2.0f), getHeight() - (mBorderWidth / 2.0f));
            }
            canvas.drawPath(COUIRoundRectUtil.getInstance().getPath(mRoundRect, mBorderRadius), mBitmapPaint);
            if (mHasBorder) {
                canvas.drawPath(COUIRoundRectUtil.getInstance().getPath(mOutBorderRect, mBorderRadius - (mBorderWidth / 2.0f)), mOutCircle);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mType == CIRCLE) {
            int min = Math.min(getMeasuredHeight(), getMeasuredWidth());
            if (min == 0) {
                min = mWidth;
            }
            mWidth = min;
            mRadius = min / 2.0f;
            setMeasuredDimension(min, min);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mType == ROUND || mType == SHADOW) {
            mRoundRect = new RectF(0.0f, 0.0f, getWidth(), getHeight());
            mOutBorderRect = new RectF(mBorderWidth / 2.0f, mBorderWidth / 2.0f,
                    getWidth() - (mBorderWidth / 2.0f), getHeight() - (mBorderWidth / 2.0f));
        }
    }

    public void refresh() {
        TypedArray a = null;
        if (mRefreshStyle == 0) {
            a = getContext().getTheme().obtainStyledAttributes(null, R.styleable.COUIRoundImageView, 0, 0);
        } else {
            String typeName = getResources().getResourceTypeName(mRefreshStyle);
            if ("attr".equals(typeName)) {
                a = getContext().getTheme().obtainStyledAttributes(null, R.styleable.COUIRoundImageView, mRefreshStyle, 0);
            } else if ("style".equals(typeName)) {
                a = getContext().getTheme().obtainStyledAttributes(null, R.styleable.COUIRoundImageView, 0, mRefreshStyle);
            }
        }
        if (a != null) {
            mOutCircleColor = a.getColor(R.styleable.COUIRoundImageView_couiRoundImageViewOutCircleColor,
                    getResources().getColor(R.color.coui_roundimageview_outcircle_color_dark, getContext().getTheme()));
            mOutCircle.setColor(mOutCircleColor);
            a.recycle();
        }
        invalidate();
    }

    public void setBorderRectRadius(int radius) {
        mBorderRadius = radius;
        invalidate();
    }

    public void setHasBorder(boolean hasBorder) {
        mHasBorder = hasBorder;
    }

    public void setHasDefaultPic(boolean hasDefaultPic) {
        mHasDefaultPic = hasDefaultPic;
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        setupShader(drawable);
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        setupShader(AppCompatResources.getDrawable(mContext, resId));
    }

    public void setOutCircleColor(int color) {
        mOutCircle.setColor(color);
        invalidate();
    }

    public void setType(int type) {
        if (mType != type) {
            mType = type;
            if (type == CIRCLE) {
                int min = Math.min(getMeasuredHeight(), getMeasuredWidth());
                if (min == 0) {
                    min = mWidth;
                }
                mWidth = min;
                mRadius = min / 2.0f;
            }
            invalidate();
        }
    }
}
