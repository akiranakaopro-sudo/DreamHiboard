package com.coui.appcompat.lockview;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.PathInterpolator;
import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.customview.widget.ExploreByTouchHelper;
import com.coui.appcompat.animation.COUIEaseInterpolator;
import com.coui.appcompat.animation.COUILinearInterpolator;
import com.coui.appcompat.contextutil.COUIContextUtil;
import com.coui.appcompat.darkmode.COUIDarkModeUtil;
import com.coui.appcompat.vibrateutil.VibrateUtils;
import com.coui.appcompat.R;
import java.util.LinkedList;
import java.util.List;

public class COUISimpleLock extends View {
    public static final int DEFAULTTYPE = 0;
    private static final int FOURCIRCLE = 4;
    private static final int FOURINTERVAL = 3;
    private static final int SIXCIRCLE = 6;
    public static final int SIXCIRCLETYPE = 1;
    private static final int SIXINTERVAL = 5;
    private static final String TAG = "COUISimpleLock";
    private final int ADD_ANIMATION;
    private final int CLEAR_ALL_ANIMATION;
    private final float[] DELAY_FOUR;
    private final float[] DELAY_SIX;
    private final int DELETE_ANIMATION;
    private final int DRAW_ALL_ANIMATION;
    private final int FAILED_ANIMATION;
    private final int FALL_HEIGHT;
    private final int FALL_SPEED;
    private final int MORPHING_FILLED_TO_OUTLINED_TIME;
    private final int MORPHING_OUTLINED_TO_FILLED_TIME;
    private final int SHAKE_AND_FALL_TIME;
    private final float[] SHAKE_X_POINT;
    private int animationMode;
    private boolean fail_lastDraw;
    private boolean fto_lastDraw;
    private boolean isFingerprintMode;
    private ValueAnimator mAddAnimator;
    private int mCodeImageStart;
    public int mCodeNumber;
    private int mContentHeight;
    private int mContentWidth;
    private Context mContext;
    private String mDecription;
    private ValueAnimator mDeleteAnimator;
    private PathInterpolator mDeleteAnimatorInterpolator;
    private boolean mDrawFailedAnimation;
    private Drawable mDrawable;
    private int mDrawableHeight;
    private int mDrawableWidth;
    private Animator mFailedAnimator;
    private Drawable mFilledRectangleDrawable;
    private boolean mIsLinearMotorVersion;
    private boolean mIsVibrator;
    private LinkedList<String> mNumberStrList;
    private int mOpacity;
    private Drawable mOutlinedRectangleDrawable;
    private int mRectangleNum;
    private int mRectanglePadding;
    private int mRectangleType;
    private int mRectanglesNumber;
    private int mRectanglesWidth;
    private float mScaleX;
    private float mScaleY;
    private int mStyle;
    private SimpleLockTouchHelper mTouchHelper;
    private float mTransitionX;
    private float mTransitionY;
    private boolean otf_lastDraw;

    public final class SimpleLockTouchHelper extends ExploreByTouchHelper {
        private Rect mTempRect;

        public SimpleLockTouchHelper(View view) {
            super(view);
            this.mTempRect = new Rect();
        }

        public CharSequence getItemDescription(int virtualViewId) {
            if (COUISimpleLock.this.mDecription == null || COUISimpleLock.this.mNumberStrList == null) {
                return SimpleLockTouchHelper.class.getSimpleName();
            }
            COUISimpleLock cOUISimpleLock = COUISimpleLock.this;
            cOUISimpleLock.mDecription = cOUISimpleLock.mDecription.replace('y', String.valueOf(COUISimpleLock.this.mRectangleNum).charAt(0));
            return COUISimpleLock.this.mDecription.replace('x', String.valueOf(COUISimpleLock.this.mNumberStrList.size()).charAt(0));
        }

        @Override
        public int getVirtualViewAt(float x, float y) {
            return (x < 0.0f || x > ((float) COUISimpleLock.this.mContentWidth) || y < 0.0f || y > ((float) COUISimpleLock.this.mDrawableHeight)) ? -2 : 0;
        }

        @Override
        public void getVisibleVirtualViews(List<Integer> list) {
            list.add(0);
        }

        public boolean onItemClicked(int virtualViewId) {
            sendEventForVirtualView(virtualViewId, 1);
            return false;
        }

        @Override
        public boolean onPerformActionForVirtualView(int virtualViewId, int action, Bundle bundle) {
            if (action != 16) {
                return false;
            }
            return onItemClicked(virtualViewId);
        }

        @Override
        public void onPopulateAccessibilityEvent(View view, AccessibilityEvent accessibilityEvent) {
            super.onPopulateAccessibilityEvent(view, accessibilityEvent);
        }

        @Override
        public void onPopulateEventForVirtualView(int virtualViewId, AccessibilityEvent accessibilityEvent) {
            accessibilityEvent.getText().add(getItemDescription(virtualViewId));
        }

        @Override
        public void onPopulateNodeForVirtualView(int virtualViewId, @NonNull AccessibilityNodeInfoCompat accessibilityNodeInfoCompat) {
            accessibilityNodeInfoCompat.setContentDescription(getItemDescription(virtualViewId));
            accessibilityNodeInfoCompat.addAction(16);
            setRectBounds(virtualViewId, this.mTempRect);
            accessibilityNodeInfoCompat.setBoundsInParent(this.mTempRect);
        }

        public void setRectBounds(int virtualViewId, Rect rect) {
            if (virtualViewId < 0 || virtualViewId >= 1) {
                return;
            }
            rect.set(0, 0, COUISimpleLock.this.mContentWidth, COUISimpleLock.this.mDrawableHeight);
        }
    }

    public COUISimpleLock(Context context) {
        this(context, null);
    }

    private ValueAnimator createMorphingAnimationFilledToOutLined() {
        ValueAnimator valueAnimator = this.mDeleteAnimator;
        if (valueAnimator != null) {
            return valueAnimator;
        }
        ValueAnimator valueAnimatorOfInt = ValueAnimator.ofInt(255, 0);
        this.mDeleteAnimator = valueAnimatorOfInt;
        valueAnimatorOfInt.setInterpolator(this.mDeleteAnimatorInterpolator);
        this.mDeleteAnimator.setDuration(230L);
        this.mDeleteAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                COUISimpleLock.this.setOpacity(((Integer) animation.getAnimatedValue()).intValue());
                COUISimpleLock.this.invalidate();
            }
        });
        this.mDeleteAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                COUISimpleLock.this.fto_lastDraw = true;
                COUISimpleLock.this.invalidate();
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }

            @Override
            public void onAnimationStart(Animator animator) {
                COUISimpleLock.this.fto_lastDraw = false;
            }
        });
        return this.mDeleteAnimator;
    }

    private ValueAnimator createMorphingAnimationOutLinedToFilled() {
        ValueAnimator valueAnimator = this.mAddAnimator;
        if (valueAnimator != null) {
            return valueAnimator;
        }
        ValueAnimator valueAnimatorOfInt = ValueAnimator.ofInt(0, 255);
        this.mAddAnimator = valueAnimatorOfInt;
        valueAnimatorOfInt.setDuration(230L);
        this.mAddAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                COUISimpleLock.this.setOpacity(((Integer) animation.getAnimatedValue()).intValue());
                COUISimpleLock.this.invalidate();
            }
        });
        this.mAddAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                COUISimpleLock.this.otf_lastDraw = true;
                COUISimpleLock.this.invalidate();
                if (COUISimpleLock.this.mDrawFailedAnimation) {
                    if (COUISimpleLock.this.mFailedAnimator != null && COUISimpleLock.this.mFailedAnimator.isRunning()) {
                        COUISimpleLock.this.otf_lastDraw = false;
                        return;
                    }
                    COUISimpleLock.this.animationMode = 5;
                    COUISimpleLock cOUISimpleLock = COUISimpleLock.this;
                    cOUISimpleLock.mFailedAnimator = cOUISimpleLock.createFailedAnimator();
                    COUISimpleLock.this.mFailedAnimator.start();
                    COUISimpleLock.this.mIsVibrator = true;
                }
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }

            @Override
            public void onAnimationStart(Animator animator) {
                COUISimpleLock.this.otf_lastDraw = false;
            }
        });
        return this.mAddAnimator;
    }

    private void drawAllCodeAnimation(Canvas canvas, int codeIndex) {
        int startX = this.mCodeImageStart;
        int height = this.mDrawableHeight;
        if (this.otf_lastDraw) {
            drawPreviousState(canvas, this.mCodeNumber);
            this.animationMode = 0;
            return;
        }
        int circleCount = judgeType();
        int left = startX;
        for (int index = 0; index < circleCount; index++) {
            int right = left + this.mDrawableWidth;
            drawOutLinedRectangle(canvas, left, 0, right, height);
            if (index <= codeIndex) {
                drawFilledRectangle(canvas, left, 0, right, height);
            }
            if (index > codeIndex) {
                drawFilledRectangle(canvas, 0, left, right, height, this.mOpacity);
            }
            left = right + this.mRectanglePadding;
        }
    }

    private void drawClearAllAnimation(Canvas canvas, int codeIndex) {
        int startX = this.mCodeImageStart;
        int height = this.mDrawableHeight;
        if (this.fto_lastDraw) {
            drawPreviousState(canvas, this.mCodeNumber);
            this.animationMode = 0;
            return;
        }
        int circleCount = judgeType();
        int left = startX;
        for (int index = 0; index < circleCount; index++) {
            int right = left + this.mDrawableWidth;
            drawOutLinedRectangle(canvas, left, 0, right, height);
            if (index <= codeIndex) {
                drawFilledRectangleWithAlphaChange(canvas, 0, left, right, height, this.mOpacity);
            }
            left = right + this.mRectanglePadding;
        }
    }

    private void drawFailedAnimation(Canvas canvas, int codeIndex) {
        int startX = this.mCodeImageStart;
        int height = this.mDrawableHeight;
        if (this.fail_lastDraw) {
            this.animationMode = 0;
            this.mDrawFailedAnimation = false;
            this.mCodeNumber = -1;
            drawPreviousState(canvas, -1);
            return;
        }
        int circleCount = judgeType();
        int left = startX;
        for (int index = 0; index < circleCount; index++) {
            int right = left + this.mDrawableWidth;
            drawOutLinedRectangleShake(canvas, 0, left, right, height, 0.0f, 0.0f);
            if (index <= codeIndex) {
                drawFilledRectangleShakeAndFall(canvas, 0, left, right, height, 0.0f, 0.0f, index);
            }
            left = left + this.mDrawableWidth + this.mRectanglePadding;
        }
    }

    private void drawFilledRectangle(Canvas canvas, int left, int top, int right, int bottom) {
        Drawable drawableNewDrawable = this.mFilledRectangleDrawable.getConstantState().newDrawable();
        this.mDrawable = drawableNewDrawable;
        float transitionX = this.mTransitionX;
        drawableNewDrawable.setBounds((int) (left + transitionX), top, (int) (right + transitionX), bottom);
        this.mDrawable.draw(canvas);
    }

    private void drawFilledRectangleShakeAndFall(Canvas canvas, int top, int left, int right, int bottom, float unusedOffsetX, float unusedOffsetY, int index) {
        this.mDrawable = this.mFilledRectangleDrawable.getConstantState().newDrawable();
        float transitionX = this.mTransitionX;
        this.mDrawable.setBounds((int) (left + transitionX), (int) (top + getDelayFallHeight(index, this.mTransitionY)), (int) (right + transitionX), (int) (bottom + getDelayFallHeight(index, this.mTransitionY)));
        int delayFallHeight = (int) ((1.0f - (getDelayFallHeight(index, this.mTransitionY) / 150.0f)) * 140.0f);
        Drawable drawable = this.mDrawable;
        if (delayFallHeight <= 0) {
            delayFallHeight = 0;
        }
        drawable.setAlpha(delayFallHeight);
        this.mDrawable.draw(canvas);
    }

    private void drawFilledRectangleWithAlphaChange(Canvas canvas, int top, int left, int right, int bottom, int alpha) {
        Drawable drawableNewDrawable = this.mFilledRectangleDrawable.getConstantState().newDrawable();
        this.mDrawable = drawableNewDrawable;
        float transitionX = this.mTransitionX;
        drawableNewDrawable.setBounds((int) (left + transitionX), top, (int) (right + transitionX), bottom);
        this.mDrawable.setAlpha(alpha);
        this.mDrawable.draw(canvas);
    }

    private void drawFilledToOutLined(Canvas canvas, int codeIndex) {
        int startX = this.mCodeImageStart;
        int height = this.mDrawableHeight;
        if (this.fto_lastDraw) {
            this.animationMode = 0;
            drawPreviousState(canvas, this.mCodeNumber);
            return;
        }
        int circleCount = judgeType();
        int left = startX;
        for (int index = 0; index < circleCount; index++) {
            int right = left + this.mDrawableWidth;
            drawOutLinedRectangle(canvas, left, 0, right, height);
            if (index < codeIndex) {
                drawFilledRectangle(canvas, left, 0, right, height);
            }
            if (index == codeIndex) {
                drawFilledRectangleWithAlphaChange(canvas, 0, left, right, height, this.mOpacity);
            }
            left = right + this.mRectanglePadding;
        }
    }

    private void drawOutLinedRectangle(Canvas canvas, int left, int top, int right, int bottom) {
        Drawable drawableNewDrawable = this.mOutlinedRectangleDrawable.getConstantState().newDrawable();
        this.mDrawable = drawableNewDrawable;
        float transitionX = this.mTransitionX;
        drawableNewDrawable.setBounds((int) (left + transitionX), top, (int) (right + transitionX), bottom);
        this.mDrawable.draw(canvas);
    }

    private void drawOutLinedRectangleShake(Canvas canvas, int top, int left, int right, int bottom, float unusedOffsetX, float unusedOffsetY) {
        Drawable drawableNewDrawable = this.mOutlinedRectangleDrawable.getConstantState().newDrawable();
        this.mDrawable = drawableNewDrawable;
        float transitionX = this.mTransitionX;
        drawableNewDrawable.setBounds((int) (left + transitionX), top, (int) (right + transitionX), bottom);
        this.mDrawable.draw(canvas);
    }

    private void drawOutLinedToFilled(Canvas canvas, int codeIndex) {
        int startX = this.mCodeImageStart;
        int height = this.mDrawableHeight;
        if (this.otf_lastDraw) {
            this.animationMode = 0;
            drawPreviousState(canvas, this.mCodeNumber);
            return;
        }
        int circleCount = judgeType();
        int left = startX;
        for (int index = 0; index < circleCount; index++) {
            int right = left + this.mDrawableWidth;
            drawOutLinedRectangle(canvas, left, 0, right, height);
            if (index < codeIndex) {
                drawFilledRectangle(canvas, left, 0, right, height);
            }
            if (index == codeIndex) {
                drawFilledRectangle(canvas, 0, left, right, height, this.mOpacity);
            }
            if (this.mDrawFailedAnimation) {
                drawFilledRectangleShakeAndFall(canvas, 0, left, right, height, 0.0f, 0.0f, index);
            }
            left = left + this.mDrawableWidth + this.mRectanglePadding;
        }
    }

    private void drawPreviousState(Canvas canvas, int codeIndex) {
        int startX = this.mCodeImageStart;
        int height = this.mDrawableHeight;
        int circleCount = judgeType();
        int left = startX;
        for (int index = 0; index < circleCount; index++) {
            int right = left + this.mDrawableWidth;
            if (index <= codeIndex) {
                drawFilledRectangle(canvas, left, 0, right, height);
            }
            if (index > codeIndex) {
                drawOutLinedRectangle(canvas, left, 0, right, height);
            }
            left = right + this.mRectanglePadding;
        }
    }

    private float getDelayFallHeight(int index, float transitionY) {
        int rectangleNum = this.mRectangleNum;
        if (rectangleNum == 4) {
            float delayedHeight = transitionY - this.DELAY_FOUR[index];
            if (delayedHeight >= 0.0f) {
                return delayedHeight;
            }
            return 0.0f;
        }
        if (rectangleNum != 6) {
            return transitionY;
        }
        float delayedHeight = transitionY - this.DELAY_SIX[index];
        if (delayedHeight >= 0.0f) {
            return delayedHeight;
        }
        return 0.0f;
    }

    private int judgeType() {
        int rectangleNum = this.mRectangleNum;
        if (rectangleNum == 4) {
            return 4;
        }
        return rectangleNum == 6 ? 6 : -1;
    }

    public void performFeedback() {
        if (this.mIsLinearMotorVersion) {
            performHapticFeedback(304);
        } else {
            performHapticFeedback(300);
        }
    }

    public Animator createFailedAnimator() {
        Animator animator = this.mFailedAnimator;
        if (animator != null) {
            return animator;
        }
        ValueAnimator shakeAnimator = ValueAnimator.ofFloat(0.0f, 30.0f, -28.0f, 14.0f, -8.0f, 4.0f, -3.0f, 0.0f);
        shakeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                COUISimpleLock.this.setInternalTranslationX(((Float) animation.getAnimatedValue()).floatValue());
                COUISimpleLock.this.invalidate();
            }
        });
        final ValueAnimator fallAnimator = ValueAnimator.ofFloat(0.0f, 250.0f);
        fallAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                COUISimpleLock.this.setInternalTranslationY(((Float) animation.getAnimatedValue()).floatValue());
            }
        });
        shakeAnimator.setInterpolator(new COUILinearInterpolator());
        fallAnimator.setInterpolator(new COUILinearInterpolator());
        shakeAnimator.setDuration(800L);
        fallAnimator.setDuration(800L);
        shakeAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                COUISimpleLock.this.setInternalTranslationX(0.0f);
                COUISimpleLock.this.fail_lastDraw = true;
                COUISimpleLock.this.mDrawFailedAnimation = false;
                COUISimpleLock.this.invalidate();
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

            @Override
            public void onAnimationStart(Animator animation) {
                COUISimpleLock.this.animationMode = 5;
                COUISimpleLock.this.setInternalTranslationX(0.0f);
                COUISimpleLock.this.fail_lastDraw = false;
                COUISimpleLock.this.mDrawFailedAnimation = true;
                fallAnimator.start();
                if (COUISimpleLock.this.isFingerprintMode) {
                    COUISimpleLock.this.isFingerprintMode = false;
                } else if (COUISimpleLock.this.mIsVibrator) {
                    COUISimpleLock.this.performFeedback();
                    COUISimpleLock.this.mIsVibrator = false;
                }
            }
        });
        this.mFailedAnimator = shakeAnimator;
        return shakeAnimator;
    }

    @Override
    public boolean dispatchHoverEvent(MotionEvent motionEvent) {
        SimpleLockTouchHelper simpleLockTouchHelper = this.mTouchHelper;
        if (simpleLockTouchHelper == null || !simpleLockTouchHelper.dispatchHoverEvent(motionEvent)) {
            return super.dispatchHoverEvent(motionEvent);
        }
        return true;
    }

    public Animator getAddAnimator() {
        return createMorphingAnimationOutLinedToFilled();
    }

    public Animator getDeleteAnimator() {
        return createMorphingAnimationFilledToOutLined();
    }

    public Animator getFailedAnimator() {
        this.mIsVibrator = true;
        return createFailedAnimator();
    }

    public LinkedList<String> getNumberStrList() {
        return this.mNumberStrList;
    }

    @Override
    public void onDraw(Canvas canvas) {
        int mode = this.animationMode;
        if (mode == 1) {
            drawFilledToOutLined(canvas, this.mCodeNumber + 1);
            return;
        }
        if (mode == 2) {
            drawOutLinedToFilled(canvas, this.mCodeNumber);
            return;
        }
        if (mode == 3) {
            drawClearAllAnimation(canvas, this.mRectanglesNumber);
            return;
        }
        if (mode == 4) {
            drawAllCodeAnimation(canvas, this.mRectanglesNumber);
        } else if (mode != 5) {
            drawPreviousState(canvas, this.mCodeNumber);
        } else {
            drawFailedAnimation(canvas, this.mCodeNumber);
        }
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int size = View.MeasureSpec.getSize(widthMeasureSpec);
        this.mContentWidth = size;
        this.mCodeImageStart = (size - this.mRectanglesWidth) / 2;
        setMeasuredDimension(size, this.mDrawableHeight + 150);
    }

    public void refresh() {
        String resourceTypeName = getResources().getResourceTypeName(this.mStyle);
        TypedArray typedArrayObtainStyledAttributes = null;
        if ("attr".equals(resourceTypeName)) {
            typedArrayObtainStyledAttributes = this.mContext.obtainStyledAttributes(null, R.styleable.COUISimpleLock, this.mStyle, 0);
        } else if ("style".equals(resourceTypeName)) {
            typedArrayObtainStyledAttributes = this.mContext.obtainStyledAttributes(null, R.styleable.COUISimpleLock, 0, this.mStyle);
        }
        if (typedArrayObtainStyledAttributes != null) {
            this.mOutlinedRectangleDrawable = typedArrayObtainStyledAttributes.getDrawable(R.styleable.COUISimpleLock_couiOutLinedRectangleIconDrawable);
            this.mFilledRectangleDrawable = typedArrayObtainStyledAttributes.getDrawable(R.styleable.COUISimpleLock_couiFilledRectangleIconDrawable);
            typedArrayObtainStyledAttributes.recycle();
        }
    }

    public void reset() {
        ValueAnimator deleteAnimator = this.mDeleteAnimator;
        if (deleteAnimator != null && deleteAnimator.isRunning()) {
            this.mDeleteAnimator.cancel();
        }
        ValueAnimator addAnimator = this.mAddAnimator;
        if (addAnimator != null && addAnimator.isRunning()) {
            this.mAddAnimator.cancel();
        }
        Animator failedAnimator = this.mFailedAnimator;
        if (failedAnimator != null && failedAnimator.isRunning()) {
            this.mFailedAnimator.cancel();
        }
        this.mCodeNumber = -1;
        this.animationMode = 0;
        this.mNumberStrList.clear();
        this.mDrawFailedAnimation = false;
        invalidate();
    }

    public void setAllCode(boolean animate) {
        int rectangleNum = this.mRectangleNum;
        if (rectangleNum == 4) {
            if (this.mDrawFailedAnimation || this.mCodeNumber >= 3) {
                return;
            }
            Animator failedAnimator = this.mFailedAnimator;
            if (failedAnimator != null && failedAnimator.isRunning()) {
                return;
            }
        } else if (rectangleNum == 6) {
            if (this.mDrawFailedAnimation || this.mCodeNumber >= 5) {
                return;
            }
            Animator failedAnimator = this.mFailedAnimator;
            if (failedAnimator != null && failedAnimator.isRunning()) {
                return;
            }
        }
        if (animate) {
            ValueAnimator deleteAnimator = this.mDeleteAnimator;
            if (deleteAnimator != null && deleteAnimator.isRunning()) {
                this.mDeleteAnimator.end();
            }
            ValueAnimator addAnimator = this.mAddAnimator;
            if (addAnimator != null && addAnimator.isRunning()) {
                this.mAddAnimator.end();
            }
            this.animationMode = 4;
            this.mRectanglesNumber = this.mCodeNumber;
            int num = this.mRectangleNum;
            if (num == 4) {
                this.mCodeNumber = 3;
            } else if (num == 6) {
                this.mCodeNumber = 5;
            }
            ValueAnimator valueAnimatorCreateMorphingAnimationOutLinedToFilled = createMorphingAnimationOutLinedToFilled();
            this.mAddAnimator = valueAnimatorCreateMorphingAnimationOutLinedToFilled;
            valueAnimatorCreateMorphingAnimationOutLinedToFilled.start();
        }
    }

    public void setClearAll(boolean clear) {
        int rectangleNum = this.mRectangleNum;
        if (rectangleNum == 4) {
            int codeNumber = this.mCodeNumber;
            if (codeNumber == -1 || this.mDrawFailedAnimation || codeNumber > 3 || !clear) {
                return;
            }
            Animator failedAnimator = this.mFailedAnimator;
            if (failedAnimator != null && failedAnimator.isRunning()) {
                return;
            }
        } else if (rectangleNum == 6) {
            int codeNumber = this.mCodeNumber;
            if (codeNumber == -1 || this.mDrawFailedAnimation || codeNumber > 5 || !clear) {
                return;
            }
            Animator failedAnimator = this.mFailedAnimator;
            if (failedAnimator != null && failedAnimator.isRunning()) {
                return;
            }
        }
        ValueAnimator deleteAnimator = this.mDeleteAnimator;
        if (deleteAnimator != null && deleteAnimator.isRunning()) {
            this.mDeleteAnimator.end();
        }
        ValueAnimator addAnimator = this.mAddAnimator;
        if (addAnimator != null && addAnimator.isRunning()) {
            this.mAddAnimator.end();
        }
        LinkedList<String> linkedList = this.mNumberStrList;
        if (linkedList != null) {
            linkedList.clear();
        }
        this.animationMode = 3;
        this.mRectanglesNumber = this.mCodeNumber;
        this.mCodeNumber = -1;
        ValueAnimator valueAnimatorCreateMorphingAnimationFilledToOutLined = createMorphingAnimationFilledToOutLined();
        this.mDeleteAnimator = valueAnimatorCreateMorphingAnimationFilledToOutLined;
        valueAnimatorCreateMorphingAnimationFilledToOutLined.start();
    }

    public void setDeleteLast(boolean delete) {
        int codeNumber;
        int rectangleNum = this.mRectangleNum;
        if ((rectangleNum == 4 || rectangleNum == 6) && ((codeNumber = this.mCodeNumber) == -1 || !delete || codeNumber >= rectangleNum)) {
            return;
        }
        LinkedList<String> linkedList = this.mNumberStrList;
        if (linkedList != null && !linkedList.isEmpty()) {
            this.mNumberStrList.removeFirst();
            String description = this.mDecription;
            if (description != null && this.mNumberStrList != null) {
                this.mDecription = description.replace('y', String.valueOf(this.mRectangleNum).charAt(0));
                announceForAccessibility(this.mDecription.replace('x', String.valueOf(this.mNumberStrList.size()).charAt(0)));
            }
        }
        this.mCodeNumber--;
        if (this.mDrawFailedAnimation) {
            return;
        }
        Animator failedAnimator = this.mFailedAnimator;
        if (failedAnimator == null || !failedAnimator.isRunning()) {
            if (this.mCodeNumber < -1) {
                this.mCodeNumber = -1;
                return;
            }
            ValueAnimator deleteAnimator = this.mDeleteAnimator;
            if (deleteAnimator != null && deleteAnimator.isRunning()) {
                this.mDeleteAnimator.end();
            }
            ValueAnimator addAnimator = this.mAddAnimator;
            if (addAnimator != null && addAnimator.isRunning()) {
                this.mAddAnimator.end();
            }
            this.animationMode = 1;
            ValueAnimator valueAnimatorCreateMorphingAnimationFilledToOutLined = createMorphingAnimationFilledToOutLined();
            this.mDeleteAnimator = valueAnimatorCreateMorphingAnimationFilledToOutLined;
            valueAnimatorCreateMorphingAnimationFilledToOutLined.start();
        }
    }

    public void setFailed(boolean failed) {
        Animator failedAnimator = this.mFailedAnimator;
        if (failedAnimator != null && failedAnimator.isRunning()) {
            this.mFailedAnimator.end();
        }
        this.mDrawFailedAnimation = failed;
    }

    public void setFilledRectangleDrawable(Drawable drawable) {
        this.mFilledRectangleDrawable = drawable;
    }

    public void setFingerprintRecognition(boolean enabled) {
        this.isFingerprintMode = enabled;
    }

    public void setInternalTranslationX(float translationX) {
        this.mTransitionX = translationX;
    }

    public void setInternalTranslationY(float translationY) {
        this.mTransitionY = translationY;
    }

    public void setOneCode(int code) {
        int rectangleNum = this.mRectangleNum;
        if (rectangleNum == 4) {
            if (this.mCodeNumber > 3) {
                return;
            }
        } else if (rectangleNum == 6 && this.mCodeNumber > 5) {
            return;
        }
        if (rectangleNum == 4) {
            if (this.mCodeNumber == 3) {
                this.mCodeNumber = -1;
            }
        } else if (rectangleNum == 6 && this.mCodeNumber == 5) {
            this.mCodeNumber = -1;
        }
        ValueAnimator deleteAnimator = this.mDeleteAnimator;
        if (deleteAnimator != null && deleteAnimator.isRunning()) {
            this.mDeleteAnimator.end();
        }
        ValueAnimator addAnimator = this.mAddAnimator;
        if (addAnimator != null && addAnimator.isRunning()) {
            this.mAddAnimator.end();
        }
        this.animationMode = 2;
        this.mCodeNumber++;
        ValueAnimator valueAnimatorCreateMorphingAnimationOutLinedToFilled = createMorphingAnimationOutLinedToFilled();
        this.mAddAnimator = valueAnimatorCreateMorphingAnimationOutLinedToFilled;
        valueAnimatorCreateMorphingAnimationOutLinedToFilled.start();
        if (this.mNumberStrList != null) {
            String codeStr = String.valueOf(code);
            if (this.mCodeNumber != this.mRectangleNum - 1) {
                this.mNumberStrList.addFirst(codeStr);
            } else {
                this.mNumberStrList.clear();
            }
        }
    }

    public void setOpacity(int opacity) {
        this.mOpacity = opacity;
    }

    public void setOutlinedRectangleDrawable(Drawable drawable) {
        this.mOutlinedRectangleDrawable = drawable;
    }

    public void setRectanglePadding(int padding) {
        this.mRectanglePadding = padding;
    }

    public void setRectangleType(int type) {
        this.mRectangleType = type;
    }

    @Override
    public void setScaleX(float scaleX) {
        this.mScaleX = scaleX;
    }

    @Override
    public void setScaleY(float scaleY) {
        this.mScaleY = scaleY;
    }

    public void setSimpleLockType(int type) {
        if (type == 0) {
            this.mRectangleNum = 4;
            this.mRectanglesWidth = (this.mDrawableWidth * 4) + (this.mRectanglePadding * 3);
        } else if (type == 1) {
            this.mRectangleNum = 6;
            this.mRectanglesWidth = (this.mDrawableWidth * 6) + (this.mRectanglePadding * 5);
        }
        this.mCodeImageStart = (this.mContentWidth - this.mRectanglesWidth) / 2;
        invalidate();
    }

    public COUISimpleLock(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, R.attr.couiSimpleLockStyle);
    }

    public COUISimpleLock(Context context, AttributeSet attributeSet, int defStyleAttr) {
        this(context, attributeSet, defStyleAttr, COUIContextUtil.isCOUIDarkTheme(context) ? R.style.Widget_COUI_COUISimpleLock_Dark : R.style.Widget_COUI_COUISimpleLock);
    }

    public COUISimpleLock(Context context, AttributeSet attributeSet, int defStyleAttr, int defStyleRes) {
        super(context, attributeSet, defStyleAttr, defStyleRes);
        this.mCodeNumber = -1;
        this.DELETE_ANIMATION = 1;
        this.ADD_ANIMATION = 2;
        this.CLEAR_ALL_ANIMATION = 3;
        this.DRAW_ALL_ANIMATION = 4;
        this.FAILED_ANIMATION = 5;
        this.MORPHING_OUTLINED_TO_FILLED_TIME = 230;
        this.MORPHING_FILLED_TO_OUTLINED_TIME = 230;
        this.SHAKE_AND_FALL_TIME = 800;
        this.FALL_SPEED = 250;
        this.FALL_HEIGHT = 150;
        this.SHAKE_X_POINT = new float[]{0.0f, 30.0f, -28.0f, 14.0f, -8.0f, 4.0f, -3.0f, 0.0f};
        this.DELAY_FOUR = new float[]{0.0f, 38.5f, 91.0f, 63.0f};
        this.DELAY_SIX = new float[]{0.0f, 38.5f, 91.0f, 63.0f, 38.5f, 70.0f};
        this.mRectanglesWidth = 0;
        this.mDrawable = null;
        this.fto_lastDraw = false;
        this.otf_lastDraw = false;
        this.fail_lastDraw = false;
        this.animationMode = 0;
        this.mDrawFailedAnimation = false;
        this.mAddAnimator = null;
        this.mDeleteAnimator = null;
        this.mFailedAnimator = null;
        this.mScaleX = 0.0f;
        this.mScaleY = 0.0f;
        this.mOpacity = 0;
        this.mTransitionX = 0.0f;
        this.mTransitionY = 0.0f;
        this.isFingerprintMode = false;
        this.mRectangleType = -1;
        this.mRectangleNum = -1;
        this.mNumberStrList = null;
        this.mTouchHelper = null;
        this.mDecription = null;
        this.mIsVibrator = true;
        this.mDeleteAnimatorInterpolator = new COUIEaseInterpolator();
        if (attributeSet != null && attributeSet.getStyleAttribute() != 0) {
            this.mStyle = attributeSet.getStyleAttribute();
        } else {
            this.mStyle = defStyleAttr;
        }
        this.mContext = context;
        COUIDarkModeUtil.setForceDarkAllow(this, false);
        TypedArray typedArrayObtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.COUISimpleLock, defStyleAttr, defStyleRes);
        this.mRectanglePadding = typedArrayObtainStyledAttributes.getDimensionPixelSize(R.styleable.COUISimpleLock_couiRectanglePadding, 0);
        this.mOutlinedRectangleDrawable = typedArrayObtainStyledAttributes.getDrawable(R.styleable.COUISimpleLock_couiOutLinedRectangleIconDrawable);
        this.mFilledRectangleDrawable = typedArrayObtainStyledAttributes.getDrawable(R.styleable.COUISimpleLock_couiFilledRectangleIconDrawable);
        this.mRectangleType = typedArrayObtainStyledAttributes.getInteger(R.styleable.COUISimpleLock_couiCircleNum, 0);
        typedArrayObtainStyledAttributes.recycle();
        Drawable drawable = this.mFilledRectangleDrawable;
        if (drawable != null) {
            this.mDrawable = drawable;
            this.mDrawableWidth = drawable.getIntrinsicWidth();
            this.mDrawableHeight = this.mDrawable.getIntrinsicHeight();
            int rectangleType = this.mRectangleType;
            if (rectangleType == 0) {
                this.mRectangleNum = 4;
                this.mRectanglesWidth = (this.mDrawableWidth * 4) + (this.mRectanglePadding * 3);
            } else if (rectangleType == 1) {
                this.mRectangleNum = 6;
                this.mRectanglesWidth = (this.mDrawableWidth * 6) + (this.mRectanglePadding * 5);
            }
        }
        SimpleLockTouchHelper simpleLockTouchHelper = new SimpleLockTouchHelper(this);
        this.mTouchHelper = simpleLockTouchHelper;
        ViewCompat.setAccessibilityDelegate(this, simpleLockTouchHelper);
        LinkedList<String> linkedList = new LinkedList<>();
        this.mNumberStrList = linkedList;
        linkedList.clear();
        this.mDecription = context.getResources().getString(R.string.coui_simple_lock_access_description);
        setImportantForAccessibility(1);
        this.mIsLinearMotorVersion = VibrateUtils.isLinearMotorVersion(context);
    }

    private void drawFilledRectangle(Canvas canvas, int top, int left, int right, int bottom, int alpha) {
        Drawable drawableNewDrawable = this.mFilledRectangleDrawable.getConstantState().newDrawable();
        this.mDrawable = drawableNewDrawable;
        float transitionX = this.mTransitionX;
        drawableNewDrawable.setBounds((int) (left + transitionX), top, (int) (right + transitionX), bottom);
        this.mDrawable.setAlpha(alpha > 0 ? 255 : 0);
        this.mDrawable.draw(canvas);
    }
}
