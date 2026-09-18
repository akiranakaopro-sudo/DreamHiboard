package com.coui.appcompat.button;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewParent;
import android.widget.TextView;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.ColorUtils;
import androidx.core.view.ViewCompat;
import com.coui.appcompat.button.listener.OnSizeChangeListener;
import com.coui.appcompat.button.listener.OnTextChangeListener;
import com.coui.appcompat.contextutil.COUIContextUtil;
import com.coui.appcompat.darkmode.COUIDarkModeUtil;
import com.coui.appcompat.hapticfeedback.COUIHapticFeedbackConstants;
import com.coui.appcompat.log.COUILog;
import com.coui.appcompat.roundRect.COUIRoundRectUtil;
import com.coui.appcompat.roundRect.COUIShapePath;
import com.coui.appcompat.roundcorner.RoundCornerUtil;
import com.coui.appcompat.state.COUIMaskEffectDrawable;
import com.coui.appcompat.state.COUIMaskRippleDrawable;
import com.coui.appcompat.state.COUIStateEffectDrawable;
import com.coui.appcompat.state.COUIStrokeDrawable;
import com.coui.appcompat.textutil.COUIChangeTextUtil;
import com.coui.appcompat.uiutil.ShadowUtils;
import com.oplus.graphics.OplusOutlineAdapter;
import com.coui.appcompat.R;


public class COUIButton extends AppCompatButton {
    public static final int BORDERLESS_BUTTON_ANIM = 0;
    public static final int COMMON_ROUND = 1;
    private static final float DEFAULT_BRIGHTNESS_MAX_VALUE = 0.8f;
    public static final float DEFAULT_RADIUS = -1.0f;
    public static final int DIALOG_BORDERLESS_BUTTON_ANIM = 2;
    public static final int FILL_BUTTON_ANIM = 1;
    private static final float HALF_HEIGHT = 2.0f;
    private static final int MAX_COLOR_VALUE = 255;
    public static final int RADIUS_HALF_HEIGHT = -1;
    public static final int SMOOTH_ROUND = 0;
    private static final String TAG = "COUIButton";
    private boolean isLimitHeight;
    private boolean mAnimEnable;
    private int mAnimType;
    private final Path mBackgroundPath;
    private int mButtonHeight;
    private int mButtonMaxHeight;
    private int mButtonMaxWidth;
    private int mButtonWidth;
    private float[] mColorHsl;
    private float mCurrentBrightness;
    private float mCurrentScale;
    private String mDescText;
    private int mDisabledColor;
    private int mDrawableColor;
    private final Paint mFillPaint;
    private float mFocusedStrokeRadius;
    private boolean mIsDescType;
    private boolean mIsNeedVibrate;
    private COUIMaskEffectDrawable mMaskDrawable;
    private float mMaxBrightness;
    private int mMaxSingleLargeWidth;
    private boolean mNeedLimitMaxWidth;
    private OnSizeChangeListener mOnSizeChangeListener;
    private OnTextChangeListener mOnTextChangeListener;
    private Rect mOplusOutLineRect;
    private OplusOutlineAdapter mOplusOutline;
    private int mPressColor;
    private float mRadius;
    private float mRadiusOffset;
    private COUIMaskRippleDrawable mRippleBackgroundDrawable;
    private int mRoundType;
    private boolean mScaleEnable;
    private COUIStateEffectDrawable mStateEffectBackground;
    private int mStrokeColor;
    private COUIStrokeDrawable mStrokeDrawable;
    private Path mStrokePath;
    private RectF mStrokeRectF;
    private float mStrokeWidth;
    private int mStyle;
    private String mText;
    private Rect mTmpRect;
    private RectF mTmpRectF;
    private float mWeight;

    public COUIButton(Context context) {
        this(context, null);
    }

    private void adapterDisableColor(TypedArray typedArray) {
        Context context = getContext();
        int disableColorAttr = R.attr.couiColorDisable;
        int attrId = COUIContextUtil.getAttrId(context, disableColorAttr, 0);
        int disabledColorStyleable = R.styleable.COUIButton_disabledColor;
        int resourceId = typedArray.getResourceId(disabledColorStyleable, 0);
        if (attrId == 0 || attrId != resourceId) {
            this.mDisabledColor = typedArray.getColor(disabledColorStyleable, 0);
        } else {
            this.mDisabledColor = COUIContextUtil.getAttrColor(getContext(), disableColorAttr);
        }
    }

    private void drawButtonBackground(Canvas canvas) {
        if (this.mAnimEnable) {
            int iSave = canvas.save();
            canvas.translate(getScrollX(), getScrollY());
            this.mFillPaint.setStyle(Paint.Style.FILL);
            this.mFillPaint.setAntiAlias(true);
            if (this.mAnimType == 1) {
                this.mFillPaint.setColor(isEnabled() ? this.mDrawableColor : this.mDisabledColor);
            } else {
                this.mFillPaint.setColor(getStrokeButtonAnimatorColor(this.mDrawableColor));
            }
            if (this.mRoundType == COMMON_ROUND) {
                float drawableRadius = getDrawableRadius();
                canvas.drawRoundRect(this.mTmpRectF, drawableRadius, drawableRadius, this.mFillPaint);
                if (this.mAnimType != 1) {
                    float drawableRadius2 = (getDrawableRadius(this.mStrokeRectF) + this.mRadiusOffset) - this.mStrokeWidth;
                    this.mFillPaint.setColor(isEnabled() ? this.mStrokeColor : this.mDisabledColor);
                    this.mFillPaint.setStrokeWidth(this.mStrokeWidth);
                    this.mFillPaint.setStyle(Paint.Style.STROKE);
                    canvas.drawRoundRect(this.mStrokeRectF, drawableRadius2, drawableRadius2, this.mFillPaint);
                }
            } else if (isOs16()) {
                canvas.drawRect(this.mTmpRect, this.mFillPaint);
                if (this.mAnimType == 0) {
                    canvas.save();
                    Path path = this.mStrokePath;
                    RectF rectF = this.mStrokeRectF;
                    COUIShapePath.getSmoothRoundRectPath(path, rectF, getDrawableRadius(rectF), this.mWeight);
                    canvas.clipOutPath(this.mStrokePath);
                    canvas.drawColor(isEnabled() ? this.mStrokeColor : this.mDisabledColor);
                    canvas.restore();
                }
            } else {
                canvas.drawPath(this.mBackgroundPath, this.mFillPaint);
                if (this.mAnimType != 1) {
                    this.mFillPaint.setColor(isEnabled() ? this.mStrokeColor : this.mDisabledColor);
                    this.mFillPaint.setStrokeWidth(this.mStrokeWidth);
                    this.mFillPaint.setStyle(Paint.Style.STROKE);
                    COUIRoundRectUtil roundRectUtil = COUIRoundRectUtil.getInstance();
                    RectF rectF2 = this.mStrokeRectF;
                    canvas.drawPath(roundRectUtil.getPath(rectF2, (getDrawableRadius(rectF2) + this.mRadiusOffset) - this.mStrokeWidth), this.mFillPaint);
                }
            }
            canvas.restoreToCount(iSave);
        }
    }

    private void drawMaskAndStroke(Canvas canvas) {
        int iSave = canvas.save();
        canvas.translate(getScrollX(), getScrollY());
        this.mMaskDrawable.draw(canvas);
        this.mStrokeDrawable.draw(canvas);
        canvas.restoreToCount(iSave);
    }

    private int getAnimatorColor() {
        return !isEnabled() ? this.mDisabledColor : ColorUtils.compositeColors(this.mMaskDrawable.getCompositeMaskColor(), this.mDrawableColor);
    }

    private SpannableString getDescTypeText(String text) {
        DescButtonTextSpan descButtonTextSpan = new DescButtonTextSpan(getContext(), text, this.mDescText, (this.mButtonMaxWidth - getPaddingStart()) - getPaddingRight(), (this.mButtonWidth - getPaddingStart()) - getPaddingRight(), (this.mButtonHeight - getPaddingBottom()) - getPaddingTop(), getCurrentTextColor(), getPaint(), isLayoutRTL());
        SpannableString spannableString = new SpannableString("  ");
        spannableString.setSpan(descButtonTextSpan, spannableString.length() - 1, spannableString.length(), 33);
        return spannableString;
    }

    private int getStrokeButtonAnimatorColor(int color) {
        return 0;
    }

    private void initStatefulBackground(Context context) {
        this.mFocusedStrokeRadius = context.getResources().getDimension(R.dimen.default_focus_stroke_radius);
        Drawable background = getBackground();
        COUIMaskEffectDrawable maskDrawable = new COUIMaskEffectDrawable(context, 0);
        this.mMaskDrawable = maskDrawable;
        maskDrawable.setMaskPath(this.mBackgroundPath);
        this.mMaskDrawable.setCallback(this);
        COUIStrokeDrawable strokeDrawable = new COUIStrokeDrawable(context);
        this.mStrokeDrawable = strokeDrawable;
        strokeDrawable.setStrokePath(this.mBackgroundPath);
        this.mStrokeDrawable.setCallback(this);
        COUIMaskRippleDrawable rippleBackgroundDrawable = new COUIMaskRippleDrawable(context);
        this.mRippleBackgroundDrawable = rippleBackgroundDrawable;
        rippleBackgroundDrawable.setCustomRippleMask();
        this.mRippleBackgroundDrawable.setMaskPath(this.mBackgroundPath);
        Drawable[] drawableArr = new Drawable[2];
        if (background == null) {
            background = new ColorDrawable(0);
        }
        drawableArr[0] = background;
        drawableArr[1] = this.mRippleBackgroundDrawable;
        this.mStateEffectBackground = new COUIStateEffectDrawable(drawableArr);
        setScaleEnable(this.mScaleEnable);
        super.setBackground(this.mStateEffectBackground);
        setAnimType(this.mAnimType);
    }

    private boolean isLayoutRTL() {
        return ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL;
    }

    private boolean isOs16() {
        return RoundCornerUtil.getSmoothStyleType() == ViewCompat.LAYOUT_DIRECTION_RTL;
    }

    public void lambda$setText$0(CharSequence text, TextView.BufferType bufferType) {
        super.setText(getDescTypeText(text.toString()), bufferType);
    }

    private int limitLargeButtonMaxWidth(int widthMode) {
        if (!this.mNeedLimitMaxWidth || widthMode == 0 || getLayoutParams() == null) {
            return 0;
        }
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        int buttonMaxWidth = this.mButtonMaxWidth;
        int maxSingleLargeWidth = this.mMaxSingleLargeWidth;
        if (buttonMaxWidth <= maxSingleLargeWidth) {
            return 0;
        }
        layoutParams.width = maxSingleLargeWidth;
        return View.MeasureSpec.makeMeasureSpec(maxSingleLargeWidth, MeasureSpec.EXACTLY);
    }

    private void performHapticFeedback() {
        if (this.mIsNeedVibrate) {
            performHapticFeedback(COUIHapticFeedbackConstants.GRANULAR_SHORT_VIBRATE);
        }
    }

    private void setIsDescTypeStyle() {
        int dimensionPixelSize = getResources().getDimensionPixelSize(R.dimen.coui_btn_desc_padding_horizontal);
        int dimensionPixelSize2 = getResources().getDimensionPixelSize(R.dimen.coui_btn_desc_padding_vertical);
        setPaddingRelative(dimensionPixelSize, dimensionPixelSize2, dimensionPixelSize, dimensionPixelSize2);
        setGravity(17);
        int dpG2Size = (int) COUIChangeTextUtil.getDpG2Size(getResources().getDimensionPixelSize(R.dimen.coui_btn_desc_height_min), getResources().getConfiguration().fontScale);
        setMinHeight(dpG2Size);
        setMinimumHeight(dpG2Size);
        setMinWidth(0);
        setMinimumWidth(0);
        requestLayout();
    }

    private void startAnimMode() {
        if (this.mAnimType == 1) {
            setBackgroundDrawable(null);
        }
    }

    private boolean supportAddOnSmoothRound() {
        return isOs16() && this.mRoundType == SMOOTH_ROUND;
    }

    private void updateRoundRectPath() {
        COUIShapePath.getRoundRectPath(this.mBackgroundPath, this.mTmpRectF, getDrawableRadius());
    }

    @Override
    public boolean dispatchHoverEvent(MotionEvent motionEvent) {
        if (isEnabled() && motionEvent.getActionMasked() == 9) {
            this.mMaskDrawable.setHoverEntered();
        }
        if (motionEvent.getActionMasked() == 10 && isHovered()) {
            this.mMaskDrawable.setHoverExited();
        }
        return super.dispatchHoverEvent(motionEvent);
    }

    @Override
    public void draw(Canvas canvas) {
        updateRoundRectPath();
        super.draw(canvas);
    }

    @Override
    public void drawableStateChanged() {
        super.drawableStateChanged();
        COUIMaskEffectDrawable maskDrawable = this.mMaskDrawable;
        if (maskDrawable != null) {
            maskDrawable.setState(getDrawableState());
        }
        COUIStrokeDrawable strokeDrawable = this.mStrokeDrawable;
        if (strokeDrawable != null) {
            strokeDrawable.setState(getDrawableState());
        }
    }

    public String getDescText() {
        return this.mDescText;
    }

    public int getDrawableColor() {
        return this.mDrawableColor;
    }

    public float getDrawableRadius() {
        return getDrawableRadius(this.mTmpRect);
    }

    public int getMeasureMaxHeight() {
        return this.mButtonMaxHeight;
    }

    public int getMeasureMaxWidth() {
        return this.mButtonMaxWidth;
    }

    public int getRoundType() {
        return this.mRoundType;
    }

    @Override
    public int getSolidColor() {
        return (this.mAnimEnable && this.mAnimType == 1) ? getAnimatorColor() : super.getSolidColor();
    }

    public float getStrokeWidth() {
        return this.mStrokeWidth;
    }

    @Override
    public CharSequence getText() {
        return isDescType() ? this.mText : super.getText();
    }

    @Override
    public void invalidateDrawable(Drawable drawable) {
        super.invalidateDrawable(drawable);
        invalidate();
    }

    public boolean isDescType() {
        return (!this.mIsDescType || TextUtils.isEmpty(this.mText) || TextUtils.isEmpty(this.mDescText)) ? false : true;
    }

    public boolean isLimitHeight() {
        return this.isLimitHeight;
    }

    @Override
    public void onDraw(Canvas canvas) {
        drawButtonBackground(canvas);
        drawMaskAndStroke(canvas);
        super.onDraw(canvas);
    }

    @Override
    public void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (focused) {
            this.mStrokeDrawable.setFocusEntered();
            this.mMaskDrawable.setFocusEntered();
        } else {
            this.mStrokeDrawable.setFocusExited();
            this.mMaskDrawable.setFocusExited();
        }
        ViewParent parent = getParent();
        if (this.mAnimType == 1 && (parent instanceof ViewGroup) && !((ViewGroup) parent).getClipChildren()) {
            COUILog.w(TAG, "Button parent view should set clip children false to make drawing focused stroke effect works.");
        }
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        this.mTmpRect.right = getWidth();
        this.mTmpRect.bottom = getHeight();
        this.mTmpRectF.set(this.mTmpRect);
        if (supportAddOnSmoothRound()) {
            RectF rectF = this.mStrokeRectF;
            float rectTop = this.mTmpRect.top;
            float strokeWidth = this.mStrokeWidth;
            rectF.top = rectTop + strokeWidth;
            rectF.left = this.mTmpRect.left + strokeWidth;
            rectF.right = this.mTmpRect.right - strokeWidth;
            rectF.bottom = this.mTmpRect.bottom - strokeWidth;
            return;
        }
        RectF rectF2 = this.mStrokeRectF;
        float rectTop = this.mTmpRect.top;
        float strokeWidth = this.mStrokeWidth;
        rectF2.top = rectTop + (strokeWidth / HALF_HEIGHT);
        rectF2.left = this.mTmpRect.left + (strokeWidth / HALF_HEIGHT);
        rectF2.right = this.mTmpRect.right - (strokeWidth / HALF_HEIGHT);
        rectF2.bottom = this.mTmpRect.bottom - (strokeWidth / HALF_HEIGHT);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        this.mButtonMaxWidth = View.MeasureSpec.getSize(widthMeasureSpec);
        this.mButtonMaxHeight = View.MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = View.MeasureSpec.getMode(heightMeasureSpec);
        int widthMode = View.MeasureSpec.getMode(widthMeasureSpec);
        if (widthMode == MeasureSpec.EXACTLY) {
            this.mButtonWidth = this.mButtonMaxWidth;
        } else {
            this.mButtonWidth = 0;
        }
        if (heightMode == MeasureSpec.EXACTLY) {
            this.mButtonHeight = this.mButtonMaxHeight;
        } else {
            this.mButtonHeight = 0;
        }
        int largeButtonMaxWidthMeasureSpec = limitLargeButtonMaxWidth(widthMode);
        if (largeButtonMaxWidthMeasureSpec != 0) {
            widthMeasureSpec = largeButtonMaxWidthMeasureSpec;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        OnSizeChangeListener onSizeChangeListener = this.mOnSizeChangeListener;
        if (onSizeChangeListener != null) {
            onSizeChangeListener.onSizeChanged(this, width, height, oldWidth, oldHeight);
        }
        if (isDescType()) {
            setText(this.mText);
        }
    }

    @Override
    public void onTextChanged(CharSequence text, int start, int before, int count) {
        super.onTextChanged(text, start, before, count);
        OnTextChangeListener onTextChangeListener = this.mOnTextChangeListener;
        if (onTextChangeListener != null) {
            onTextChangeListener.onTextChanged(this, text, start, before, count);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (isEnabled() && this.mAnimEnable) {
            int action = motionEvent.getAction();
            if (action == 0) {
                performHapticFeedback();
                this.mMaskDrawable.setTouchEntered();
                this.mStateEffectBackground.setTouched(true);
            } else if (action == 1 || action == 3) {
                performHapticFeedback();
                this.mMaskDrawable.setTouchExited();
                this.mStateEffectBackground.setTouched(false);
            }
        }
        return super.onTouchEvent(motionEvent);
    }

    public void refresh() {
        String resourceTypeName = getResources().getResourceTypeName(this.mStyle);
        TypedArray typedArrayObtainStyledAttributes = null;
        if ("attr".equals(resourceTypeName)) {
            typedArrayObtainStyledAttributes = getContext().obtainStyledAttributes(null, R.styleable.COUIButton, this.mStyle, 0);
        } else if ("style".equals(resourceTypeName)) {
            typedArrayObtainStyledAttributes = getContext().obtainStyledAttributes(null, R.styleable.COUIButton, 0, this.mStyle);
        }
        if (typedArrayObtainStyledAttributes != null) {
            adapterDisableColor(typedArrayObtainStyledAttributes);
            this.mDrawableColor = typedArrayObtainStyledAttributes.getColor(R.styleable.COUIButton_drawableColor, 0);
            this.mStrokeColor = typedArrayObtainStyledAttributes.getColor(R.styleable.COUIButton_strokeColor, 0);
            setTextColor(typedArrayObtainStyledAttributes.getColorStateList(R.styleable.COUIButton_android_textColor));
            typedArrayObtainStyledAttributes.recycle();
        }
        COUIStateEffectDrawable stateEffectBackground = this.mStateEffectBackground;
        if (stateEffectBackground != null) {
            stateEffectBackground.refresh(getContext());
        }
    }

    public void setAnimEnable(boolean animEnable) {
        this.mAnimEnable = animEnable;
    }

    public void setAnimType(int animType) {
        this.mAnimType = animType;
        if (animType == FILL_BUTTON_ANIM || animType == BORDERLESS_BUTTON_ANIM) {
            this.mMaskDrawable.setDrawableEnabled(true);
            this.mMaskDrawable.setMaskType(0);
            this.mStrokeDrawable.setDrawableEnabled(true);
            this.mRippleBackgroundDrawable.setDrawableEnabled(false);
        } else if (animType == DIALOG_BORDERLESS_BUTTON_ANIM) {
            this.mMaskDrawable.setDrawableEnabled(true);
            this.mMaskDrawable.setMaskType(1);
            this.mStrokeDrawable.setDrawableEnabled(false);
            this.mRippleBackgroundDrawable.setDrawableEnabled(false);
        }
        updateRoundRectPath();
    }

    @Override
    public void setBackground(Drawable drawable) {
        COUIStateEffectDrawable stateEffectBackground = this.mStateEffectBackground;
        if (stateEffectBackground == null) {
            super.setBackground(drawable);
        } else if (drawable == null) {
            stateEffectBackground.setViewBackground(new ColorDrawable(0));
        } else {
            stateEffectBackground.setViewBackground(drawable);
        }
    }

    public void setDescText(String descText) {
        this.mDescText = descText;
        if (isDescType()) {
            setText(getText());
        }
    }

    public void setDescType(boolean descType, String descText) {
        if (!descType || TextUtils.isEmpty(getText()) || TextUtils.isEmpty(descText)) {
            return;
        }
        this.mIsDescType = true;
        this.mDescText = descText;
        setIsDescTypeStyle();
        setText(getText());
    }

    public void setDisabledColor(int disabledColor) {
        this.mDisabledColor = disabledColor;
    }

    public void setDrawableColor(int drawableColor) {
        this.mDrawableColor = drawableColor;
    }

    public void setDrawableRadius(int drawableRadius) {
        this.mRadius = drawableRadius;
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (enabled != isEnabled() && isDescType()) {
            setText(this.mText);
        }
        super.setEnabled(enabled);
    }

    public void setIsNeedVibrate(boolean isNeedVibrate) {
        this.mIsNeedVibrate = isNeedVibrate;
    }

    public void setLimitHeight(boolean limitHeight) {
        this.isLimitHeight = limitHeight;
    }

    public void setMaxBrightness(int maxBrightness) {
        this.mMaxBrightness = maxBrightness;
    }

    @Override
    public void setMinHeight(int minHeight) {
        int dimensionPixelSize;
        if (isDescType() && minHeight < (dimensionPixelSize = getResources().getDimensionPixelSize(R.dimen.coui_btn_large_height_min))) {
            minHeight = dimensionPixelSize;
        }
        super.setMinHeight(minHeight);
    }

    public void setNeedLimitMaxWidth(boolean needLimitMaxWidth) {
        this.mNeedLimitMaxWidth = needLimitMaxWidth;
    }

    public void setOnSizeChangeListener(OnSizeChangeListener onSizeChangeListener) {
        this.mOnSizeChangeListener = onSizeChangeListener;
    }

    public void setOnTextChangeListener(OnTextChangeListener onTextChangeListener) {
        this.mOnTextChangeListener = onTextChangeListener;
    }

    public void setRoundType(int roundType) {
        if (roundType != SMOOTH_ROUND && roundType != COMMON_ROUND) {
            throw new IllegalArgumentException("Invalid roundType" + roundType);
        }
        if (this.mRoundType != roundType) {
            this.mRoundType = roundType;
            invalidate();
        }
    }

    public void setScaleEnable(boolean scaleEnable) {
        this.mScaleEnable = scaleEnable;
        COUIStateEffectDrawable stateEffectBackground = this.mStateEffectBackground;
        if (stateEffectBackground != null) {
            if (scaleEnable) {
                stateEffectBackground.enableScaleEffect(this, 2);
            } else {
                stateEffectBackground.disableScaleEffect();
            }
        }
    }

    public void setStrokeColor(int strokeColor) {
        this.mStrokeColor = strokeColor;
    }

    public void setStrokeWidth(float strokeWidth) {
        this.mStrokeWidth = strokeWidth;
    }

    @Override
    public void setText(final CharSequence text, final TextView.BufferType bufferType) {
        if (!this.mIsDescType || TextUtils.isEmpty(text) || TextUtils.isEmpty(this.mDescText)) {
            super.setText(text, bufferType);
        } else {
            post(new Runnable() {
                @Override
                public final void run() {
                    COUIButton.this.lambda$setText$0(text, bufferType);
                }
            });
        }
        this.mText = text == null ? null : text.toString();
    }

    public COUIButton(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, androidx.appcompat.R.attr.buttonStyle);
    }

    private float getDrawableRadius(RectF rectF) {
        if (this.mRadius < 0.0f && supportAddOnSmoothRound()) {
            return rectF.height() / HALF_HEIGHT;
        }
        float radius = this.mRadius;
        return radius < 0.0f ? (rectF.height() / HALF_HEIGHT) - this.mRadiusOffset : radius;
    }

    public COUIButton(Context context, AttributeSet attributeSet, int defStyleAttr) {
        super(context, attributeSet, defStyleAttr);
        boolean closeLimitTextSize;
        this.mBackgroundPath = new Path();
        this.mStrokePath = new Path();
        this.mScaleEnable = true;
        this.mFillPaint = new Paint(1);
        this.mRadius = 21.0f;
        this.mCurrentBrightness = 1.0f;
        this.mCurrentScale = 1.0f;
        this.mPressColor = 0;
        this.mTmpRect = new Rect();
        this.mTmpRectF = new RectF();
        this.mStrokeRectF = new RectF();
        this.mColorHsl = new float[3];
        this.isLimitHeight = true;
        this.mNeedLimitMaxWidth = false;
        this.mOplusOutLineRect = new Rect();
        if (attributeSet == null || attributeSet.getStyleAttribute() == 0) {
            this.mStyle = defStyleAttr;
        } else {
            this.mStyle = attributeSet.getStyleAttribute();
        }
        COUIDarkModeUtil.setForceDarkAllow(this, false);
        TypedArray typedArrayObtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.COUIButton, defStyleAttr, 0);
        this.mAnimEnable = typedArrayObtainStyledAttributes.getBoolean(R.styleable.COUIButton_animEnable, false);
        this.mAnimType = typedArrayObtainStyledAttributes.getInteger(R.styleable.COUIButton_animType, FILL_BUTTON_ANIM);
        this.mRoundType = typedArrayObtainStyledAttributes.getInteger(R.styleable.COUIButton_couiRoundType, SMOOTH_ROUND);
        this.mIsNeedVibrate = typedArrayObtainStyledAttributes.getBoolean(R.styleable.COUIButton_needVibrate, true);
        this.mScaleEnable = typedArrayObtainStyledAttributes.getBoolean(R.styleable.COUIButton_scaleEnable, this.mScaleEnable);
        if (this.mAnimEnable) {
            this.mMaxBrightness = typedArrayObtainStyledAttributes.getFloat(R.styleable.COUIButton_brightness, DEFAULT_BRIGHTNESS_MAX_VALUE);
            this.mRadius = typedArrayObtainStyledAttributes.getDimension(R.styleable.COUIButton_drawableRadius, DEFAULT_RADIUS);
            adapterDisableColor(typedArrayObtainStyledAttributes);
            this.mDrawableColor = typedArrayObtainStyledAttributes.getColor(R.styleable.COUIButton_drawableColor, 0);
            this.mStrokeColor = typedArrayObtainStyledAttributes.getColor(R.styleable.COUIButton_strokeColor, 0);
            this.mPressColor = typedArrayObtainStyledAttributes.getInteger(R.styleable.COUIButton_pressColor, 0);
            closeLimitTextSize = typedArrayObtainStyledAttributes.getBoolean(R.styleable.COUIButton_closeLimitTextSize, false);
            startAnimMode();
        } else {
            closeLimitTextSize = false;
        }
        this.mStrokeWidth = typedArrayObtainStyledAttributes.getDimension(R.styleable.COUIButton_strokeWidth, context.getResources().getDimension(R.dimen.coui_bordless_btn_stroke_width));
        this.mMaxSingleLargeWidth = getResources().getDimensionPixelSize(R.dimen.coui_single_larger_btn_width);
        boolean isDescType = typedArrayObtainStyledAttributes.getBoolean(R.styleable.COUIButton_isDescType, false);
        this.mIsDescType = isDescType;
        if (isDescType && !TextUtils.isEmpty(getText())) {
            this.mDescText = typedArrayObtainStyledAttributes.getString(R.styleable.COUIButton_descText);
            this.mText = getText().toString();
            if (isDescType()) {
                setDescType(this.mIsDescType, this.mDescText);
            }
        }
        typedArrayObtainStyledAttributes.recycle();
        this.mRadiusOffset = getResources().getDimension(R.dimen.coui_button_radius_offset);
        if (!closeLimitTextSize) {
            COUIChangeTextUtil.adaptFontSize(this, 4);
        }
        initStatefulBackground(context);
        if (supportAddOnSmoothRound()) {
            setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    COUIButton.this.mOplusOutline = new OplusOutlineAdapter(outline, 1);
                    COUIButton.this.mOplusOutLineRect.left = (int) COUIButton.this.mTmpRectF.left;
                    COUIButton.this.mOplusOutLineRect.top = (int) COUIButton.this.mTmpRectF.top;
                    COUIButton.this.mOplusOutLineRect.right = (int) COUIButton.this.mTmpRectF.right;
                    COUIButton.this.mOplusOutLineRect.bottom = (int) COUIButton.this.mTmpRectF.bottom;
                    COUIButton.this.mOplusOutline.setSmoothRoundRect(COUIButton.this.mOplusOutLineRect, COUIButton.this.getDrawableRadius());
                }
            });
            setClipToOutline(true);
            ShadowUtils.clearShadow(this);
            this.mWeight = COUIContextUtil.getAttrFloat(getContext(), R.attr.couiRoundCornerXXLWeight);
        }
    }

    private float getDrawableRadius(Rect rect) {
        if (this.mRadius < 0.0f && supportAddOnSmoothRound()) {
            return rect.height() / HALF_HEIGHT;
        }
        float radius = this.mRadius;
        return radius < 0.0f ? (rect.height() / HALF_HEIGHT) - this.mRadiusOffset : radius;
    }
}
