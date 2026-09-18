package androidx.recyclerview.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.animation.PathInterpolator;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.coui.appcompat.animation.COUIEaseInterpolator;
import com.coui.appcompat.R;
import com.coui.appcompat.contextutil.COUIContextUtil;
import com.coui.appcompat.vibrateutil.VibrateUtils;




/**
 * Leapy added 2026-08-01: COUI fast scroller recovered from the decoded
 * ColorOS 16 TrafficMonitor COUI implementation. Resource symbols and the
 * vibrator type are mapped to this source tree; the animation/state behavior
 * remains the decoded implementation.
 */
public class COUIFastScroller extends RecyclerView.ItemDecoration implements RecyclerView.OnItemTouchListener {
    private static final int ANIMATION_STATE_FADING_IN = 1;
    private static final int ANIMATION_STATE_FADING_OUT = 3;
    private static final int ANIMATION_STATE_IN = 2;
    private static final int ANIMATION_STATE_OUT = 0;
    private static final int DIRECTION = 1;
    private static final int DRAG_NONE = 0;
    private static final int DRAG_X = 1;
    private static final int DRAG_Y = 2;
    private static final String HEIGHT_ANIM_HOLDER = "HEIGHT_ANIM_HOLDER";
    private static final int HIDE_DELAY_AFTER_DRAGGING_MS = 2000;
    private static final int HIDE_DELAY_AFTER_VISIBLE_MS = 2000;
    private static final int HIDE_DURATION_MS = 160;
    private static final String MEDIUM_FONT = "sans-serif-medium";
    private static final int MIN_VELOCITY_WEAK = 250;
    private static final int MIN_VELOCITY_WEAKEST = 70;
    public static final int MIN_VIBRATOR_TIME = 100;
    private static final int SCROLLBAR_FULL_OPAQUE = 255;
    private static final int SHOW_DURATION_MS = 160;
    private static final int STATE_DRAGGING = 2;
    private static final int STATE_HIDDEN = 0;
    private static final int STATE_VISIBLE = 1;
    private static final String THUMB_TRANSLATE_X_HOLDER = "THUMB_TRANSLATE_X_HOLDER";
    private static final float TOUCH_SCALE_FACTOR = 2.5f;
    private static final String WIDTH_ANIM_HOLDER = "WIDTH_ANIM_HOLDER";
    private float mCurrentThumbShadowX;
    private float mCurrentThumbShadowY;
    private final int mDefaultVerticalMarginEnd;
    private final int mDefaultVerticalThumbHeight;
    private final int mDefaultVerticalThumbWidth;
    private final String mDots;
    private boolean mHasMotorVibrator;
    private final float mHeightEndScale;
    private PropertyValuesHolder mHeightScaleHolder;
    private ValueAnimator mMessageAlphaAnimator;
    private f mMessageAnimatorUpdateListener;
    private final Drawable mMessageBackgroundDrawable;
    private int mMessageBackgroundHeight;
    private final int mMessageBackgroundInternalPadding;
    private int mMessageBackgroundShadowPaddingEnd;
    private int mMessageBackgroundShadowPaddingTop;
    private final int mMessageBackgroundTopOffset;
    private final int mMessageMarginEnd;
    private final int mMessageMaximumWidth;
    private final int mMessageMinimumWidth;
    private TextPaint mMessagePaint;
    private final int mMessageTextPadding;
    private float mMessageTextShadowPaddingY;
    private float mMessageWidth;
    private boolean mNeedShowMessage;
    private g mPressAnimatorListener;
    private RecyclerView mRecyclerView;
    private final int mScaleEndThumbTranslateX;
    private float mScaleEndThumbTranslateY;
    private final int mScaleEndVerticalThumbHeight;
    private final int mScaleEndVerticalThumbWidth;
    private final int mScrollbarMinimumRange;
    private float mTextWidth;
    private float mTextX;
    private float mTextY;
    private int mThumbBackgroundShadowPaddingEnd;
    private int mThumbBackgroundShadowPaddingTop;
    private int mThumbBottomMargin;
    private final int mThumbDrawableBackgroundScaleCenterX;
    private final int mThumbDrawableBackgroundScaleCenterY;
    private ValueAnimator mThumbScaleAnimator;
    private h mThumbScaleAnimatorUpdateListener;
    private int mThumbTopMargin;
    private PropertyValuesHolder mThumbTranslateXHolder;
    private VelocityTracker mVelocityTracker;
    private float mVerticalDragY;
    private int mVerticalThumbCenterY;
    private final Drawable mVerticalThumbDrawable;
    private final Drawable mVerticalThumbDrawableBackground;
    private int mVibrateLevel;
    private final float mWidthEndScale;
    private PropertyValuesHolder mWidthScaleHolder;
    private float mCurrentWidthScale = 1.0f;
    private float mCurrentHeightScale = 1.0f;
    private float mCurrentThumbTranslateX = 0.0f;
    private float mCurrentThumbTranslateScaleX = 0.0f;
    private float mCurrentThumbTranslateY = 0.0f;
    private final PathInterpolator mCommonInterpolator = new COUIEaseInterpolator();
    private AnimatorSet mPressAnimators = new AnimatorSet();
    private int mPressAnimatorState = 0;
    private float mMessageAlphaAnimatedValue = 0.0f;
    private String mMessage = "";
    private String mRealShowMessage = "";
    private boolean mIsThumbAlwaysShow = false;
    private int mRecyclerViewWidth = 0;
    private int mRecyclerViewHeight = 0;
    private int mTrackerMaxVelocity = 8000;
    private int mLowVelocityThreshold = 3000;
    private int mMidVelocityThreshold = 6000;
    private int mTrackerPeriod = 1000;
    private long lastVibratorTime = -1;
    private Object mLinearMotorVibrator = null;
    private boolean mEnableAdaptiveVibrator = true;
    private boolean mEnabled = true;
    private boolean mNeedVerticalScrollbar = false;
    private int mState = STATE_HIDDEN;
    private int mDragState = DRAG_NONE;
    private float mVibrateIntensity = 1.0f;
    private final int[] mVerticalRange = new int[2];
    final ValueAnimator mShowHideAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
    int mAnimationState = ANIMATION_STATE_OUT;
    private final Runnable mHideRunnable = new a();
    private final RecyclerView.OnScrollListener mOnScrollListener = new b();

    class a implements Runnable {
        a() {
        }

        @Override
        public void run() {
            if (COUIFastScroller.this.mIsThumbAlwaysShow) {
                return;
            }
            COUIFastScroller.this.hide(HIDE_DURATION_MS);
        }
    }

    class b extends RecyclerView.OnScrollListener {
        b() {
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int index, int index_2) {
            if (COUIFastScroller.this.mEnabled) {
                COUIFastScroller.this.performFeedback();
                COUIFastScroller.this.updateScrollPosition(recyclerView.computeHorizontalScrollOffset(), recyclerView.computeVerticalScrollOffset());
            }
        }
    }

    class c implements View.OnAttachStateChangeListener {
        c() {
        }

        @Override
        public void onViewAttachedToWindow(View view) {
            VibrateUtils.registerHapticObserver(COUIFastScroller.this.mRecyclerView.getContext());
        }

        @Override
        public void onViewDetachedFromWindow(View view) {
            VibrateUtils.unRegisterHapticObserver();
            COUIFastScroller.this.mRecyclerView.removeOnAttachStateChangeListener(this);
            COUIFastScroller.this.cancelHide();
        }
    }

    private class d extends AnimatorListenerAdapter {


        private boolean mCanceled = false;

        d() {
        }

        @Override
        public void onAnimationCancel(Animator animator) {
            this.mCanceled = true;
        }

        @Override
        public void onAnimationEnd(Animator animator) {
            if (this.mCanceled) {
                this.mCanceled = false;
                return;
            }
            if (((Float) COUIFastScroller.this.mShowHideAnimator.getAnimatedValue()).floatValue() == 0.0f) {
                COUIFastScroller cOUIFastScroller = COUIFastScroller.this;
                cOUIFastScroller.mAnimationState = ANIMATION_STATE_OUT;
                cOUIFastScroller.setState(0);
            } else {
                COUIFastScroller cOUIFastScroller2 = COUIFastScroller.this;
                cOUIFastScroller2.mAnimationState = ANIMATION_STATE_IN;
                cOUIFastScroller2.requestRedraw();
            }
        }
    }

    private class e implements ValueAnimator.AnimatorUpdateListener {
        e() {
        }

        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            int iFloatValue = (int) (((Float) valueAnimator.getAnimatedValue()).floatValue() * 255.0f);
            COUIFastScroller.this.mVerticalThumbDrawableBackground.setAlpha(iFloatValue);
            COUIFastScroller.this.mVerticalThumbDrawable.setAlpha(iFloatValue);
            COUIFastScroller.this.requestRedraw();
        }
    }

    private class f implements ValueAnimator.AnimatorUpdateListener {
        private f() {
        }

        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            COUIFastScroller.this.mMessageAlphaAnimatedValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
            int index = (int) (COUIFastScroller.this.mMessageAlphaAnimatedValue * 255.0f);
            COUIFastScroller.this.mMessageBackgroundDrawable.setAlpha(index);
            COUIFastScroller.this.mMessagePaint.setAlpha(index);
            COUIFastScroller.this.requestRedraw();
        }

 f(COUIFastScroller cOUIFastScroller, a aVar) {
            this();
        }
    }

    private class g extends AnimatorListenerAdapter {


        private boolean mCanceled = false;

        g() {
        }

        @Override
        public void onAnimationCancel(Animator animator) {
            this.mCanceled = true;
        }

        @Override
        public void onAnimationEnd(Animator animator) {
            if (this.mCanceled) {
                this.mCanceled = false;
            } else if (COUIFastScroller.this.mCurrentWidthScale != 1.0f) {
                COUIFastScroller.this.mPressAnimatorState = 2;
            } else {
                COUIFastScroller.this.mPressAnimatorState = 0;
            }
        }
    }

    private class h implements ValueAnimator.AnimatorUpdateListener {
        private h() {
        }

        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            COUIFastScroller.this.mCurrentHeightScale = ((Float) valueAnimator.getAnimatedValue(COUIFastScroller.HEIGHT_ANIM_HOLDER)).floatValue();
            COUIFastScroller.this.mCurrentWidthScale = ((Float) valueAnimator.getAnimatedValue(COUIFastScroller.WIDTH_ANIM_HOLDER)).floatValue();
            COUIFastScroller.this.mCurrentThumbTranslateScaleX = ((Float) valueAnimator.getAnimatedValue(COUIFastScroller.THUMB_TRANSLATE_X_HOLDER)).floatValue();
            COUIFastScroller cOUIFastScroller = COUIFastScroller.this;
            cOUIFastScroller.mCurrentThumbTranslateX = cOUIFastScroller.mCurrentThumbTranslateScaleX * COUIFastScroller.this.mScaleEndThumbTranslateX;
            COUIFastScroller cOUIFastScroller2 = COUIFastScroller.this;
            cOUIFastScroller2.mCurrentThumbTranslateY = cOUIFastScroller2.mCurrentThumbTranslateScaleX * COUIFastScroller.this.mScaleEndThumbTranslateY;
            COUIFastScroller.this.requestRedraw();
        }

 h(COUIFastScroller cOUIFastScroller, a aVar) {
            this();
        }
    }

    public COUIFastScroller(RecyclerView recyclerView, Context context) {
        this.mScaleEndThumbTranslateY = 0.0f;
        this.mHasMotorVibrator = true;
        this.mMessageBackgroundHeight = 0;
        this.mMessageBackgroundShadowPaddingEnd = 0;
        this.mThumbBackgroundShadowPaddingEnd = 0;
        this.mMessageBackgroundShadowPaddingTop = 0;
        this.mThumbBackgroundShadowPaddingTop = 0;
        this.mCurrentThumbShadowY = 0.0f;
        this.mCurrentThumbShadowX = 0.0f;
        this.mMessageTextShadowPaddingY = 0.0f;
        int dimensionPixelOffset = context.getResources().getDimensionPixelOffset(R.dimen.coui_fast_scroller_default_width);
        this.mDefaultVerticalThumbWidth = dimensionPixelOffset;
        int dimensionPixelOffset2 = context.getResources().getDimensionPixelOffset(R.dimen.coui_fast_scroller_default_height);
        this.mDefaultVerticalThumbHeight = dimensionPixelOffset2;
        this.mDefaultVerticalMarginEnd = context.getResources().getDimensionPixelOffset(R.dimen.coui_fast_scroller_default_vertical_margin_end);
        int dimensionPixelOffset3 = context.getResources().getDimensionPixelOffset(R.dimen.coui_fast_scroller_scale_end_width);
        this.mScaleEndVerticalThumbWidth = dimensionPixelOffset3;
        int dimensionPixelOffset4 = context.getResources().getDimensionPixelOffset(R.dimen.coui_fast_scroller_scale_end_height);
        this.mScaleEndVerticalThumbHeight = dimensionPixelOffset4;
        this.mThumbDrawableBackgroundScaleCenterX = context.getResources().getDimensionPixelOffset(R.dimen.coui_fast_scroller_bar_background_scale_x_offset);
        this.mThumbBackgroundShadowPaddingEnd = context.getResources().getDimensionPixelOffset(R.dimen.coui_fast_scroller_scale_shadow_padding_end);
        this.mThumbBackgroundShadowPaddingTop = context.getResources().getDimensionPixelOffset(R.dimen.coui_fast_scroller_scale_shadow_padding_top);
        this.mThumbDrawableBackgroundScaleCenterY = dimensionPixelOffset2 / 2;
        // Leapy modified 2026-08-01: The decoded bytecode performs
        // int-to-float on both operands before div-float. Keeping Java integer
        // division here collapses the OPPO press scale to 1.0 and removes the
        // fast-scroller expansion animation.
        this.mWidthEndScale = (float) dimensionPixelOffset3 / dimensionPixelOffset;
        this.mHeightEndScale = (float) dimensionPixelOffset4 / dimensionPixelOffset2;
        // Leapy end
        Drawable drawable = context.getDrawable(R.drawable.coui_fast_scroller_slide_bar_background);
        this.mVerticalThumbDrawableBackground = drawable;
        drawable.setBounds(0, 0, dimensionPixelOffset, dimensionPixelOffset2);
        drawable.setAlpha(SCROLLBAR_FULL_OPAQUE);
        Drawable verticalThumbDrawable = context.getDrawable(R.drawable.coui_fast_scroller_union);
        this.mVerticalThumbDrawable = verticalThumbDrawable;
        this.mScaleEndThumbTranslateX = context.getResources().getDimensionPixelOffset(R.dimen.coui_fast_scroller_bar_thumb_translate_x);
        this.mScaleEndThumbTranslateY = context.getResources().getDimensionPixelOffset(R.dimen.coui_fast_scroller_bar_thumb_translate_y);
        this.mCurrentThumbShadowY = context.getResources().getDimensionPixelOffset(R.dimen.coui_fast_scroller_bar_thumb_shadow_padding_y);
        this.mCurrentThumbShadowX = context.getResources().getDimensionPixelOffset(R.dimen.coui_fast_scroller_bar_thumb_shadow_padding_x);
        int dimensionPixelOffset5 = context.getResources().getDimensionPixelOffset(R.dimen.coui_fast_scroller_union_width);
        int dimensionPixelOffset6 = context.getResources().getDimensionPixelOffset(R.dimen.coui_fast_scroller_union_height);
        int index = (dimensionPixelOffset - dimensionPixelOffset5) / 2;
        int index_2 = (dimensionPixelOffset2 - dimensionPixelOffset6) / 2;
        verticalThumbDrawable.setBounds(index, index_2, dimensionPixelOffset5 + index, dimensionPixelOffset6 + index_2);
        verticalThumbDrawable.setAlpha(SCROLLBAR_FULL_OPAQUE);
        Drawable messageBackgroundDrawable = context.getDrawable(R.drawable.coui_fast_scroller_message_background);
        this.mMessageBackgroundDrawable = messageBackgroundDrawable;
        messageBackgroundDrawable.setAlpha(0);
        this.mMessageTextPadding = context.getResources().getDimensionPixelOffset(R.dimen.coui_fast_scroller_message_text_padding);
        this.mMessageBackgroundInternalPadding = context.getResources().getDimensionPixelOffset(R.dimen.coui_fast_scroller_message_background_internal_padding);
        this.mMessageBackgroundTopOffset = context.getResources().getDimensionPixelOffset(R.dimen.coui_fast_scroller_message_background_top_offset);
        this.mMessageBackgroundHeight = context.getResources().getDimensionPixelOffset(R.dimen.coui_fast_scroller_message_minimum_height);
        this.mMessageBackgroundShadowPaddingEnd = context.getResources().getDimensionPixelOffset(R.dimen.coui_fast_scroller_message_shadow_padding_end);
        this.mMessageBackgroundShadowPaddingTop = context.getResources().getDimensionPixelOffset(R.dimen.coui_fast_scroller_message_shadow_padding_top);
        this.mMessageTextShadowPaddingY = context.getResources().getDimensionPixelOffset(R.dimen.coui_fast_scroller_message_text_shadow_padding_top);
        this.mMessageMaximumWidth = context.getResources().getDimensionPixelOffset(R.dimen.coui_fast_scroller_max_message_width);
        this.mMessageMinimumWidth = context.getResources().getDimensionPixelOffset(R.dimen.coui_fast_scroller_message_minimum_width);
        this.mMessageMarginEnd = context.getResources().getDimensionPixelOffset(R.dimen.coui_fast_scroller_message_margin_end);
        this.mDots = context.getString(R.string.fast_scroller_dots);
        this.mScrollbarMinimumRange = context.getResources().getDimensionPixelOffset(R.dimen.coui_fast_scroller_minimum_range);
        this.mThumbTopMargin = context.getResources().getDimensionPixelOffset(R.dimen.coui_fast_scroller_thumb_top_margin);
        this.mThumbBottomMargin = context.getResources().getDimensionPixelOffset(R.dimen.coui_fast_scroller_thumb_bottom_margin);
        this.mHasMotorVibrator = VibrateUtils.isLinearMotorVersion(context);
        initMessagePaint(context);
        initAnimators();
        attachToRecyclerView(recyclerView);
    }


    public void cancelHide() {
        this.mRecyclerView.removeCallbacks(this.mHideRunnable);
    }

    private void computeVelocityWithTouchEvent(MotionEvent motionEvent) {
        int action = motionEvent.getAction();
        if (action == 0) {
            initOrResetVelocityTracker();
            this.mVelocityTracker.addMovement(motionEvent);
            return;
        }
        if (action != 1) {
            if (action == 2) {
                initVelocityTrackerIfNotExists();
                this.mVelocityTracker.addMovement(motionEvent);
                return;
            } else if (action != 3) {
                return;
            }
        }
        recycleVelocityTracker();
    }

    private void destroyCallbacks() {
        this.mRecyclerView.removeItemDecoration(this);
        this.mRecyclerView.removeOnItemTouchListener(this);
        this.mRecyclerView.removeOnScrollListener(this.mOnScrollListener);
        cancelHide();
    }

    private void drawVerticalScrollbar(Canvas canvas) {
        int index_7;
        int index_8;
        float value_9;
        float value_10;
        float value_11;
        this.mVerticalThumbDrawableBackground.mutate();
        this.mVerticalThumbDrawable.mutate();
        int index = this.mRecyclerViewWidth;
        int index_2 = this.mVerticalThumbCenterY;
        int index_3 = (index_2 - (this.mDefaultVerticalThumbHeight / 2)) + this.mThumbBackgroundShadowPaddingTop;
        float value = ((index_2 - (this.mScaleEndVerticalThumbHeight / 2.0f)) - this.mMessageBackgroundTopOffset) + this.mMessageBackgroundShadowPaddingTop;
        float value_2 = -this.mCurrentThumbTranslateY;
        float value_3 = -this.mCurrentThumbShadowY;
        float value_4 = -this.mMessageTextShadowPaddingY;
        if (isLayoutRTL()) {
            int index_4 = this.mDefaultVerticalMarginEnd;
            index_7 = index_4 - this.mThumbBackgroundShadowPaddingEnd;
            value_9 = ((index_4 + this.mScaleEndVerticalThumbWidth) - this.mMessageMarginEnd) - this.mMessageBackgroundShadowPaddingEnd;
            value_10 = this.mCurrentThumbTranslateX;
            value_11 = -this.mCurrentThumbShadowX;
            index_8 = this.mThumbDrawableBackgroundScaleCenterX - index_7;
        } else {
            int index_5 = index - this.mDefaultVerticalThumbWidth;
            int index_6 = this.mDefaultVerticalMarginEnd;
            index_7 = (index_5 - index_6) + this.mThumbBackgroundShadowPaddingEnd;
            float value_5 = this.mMessageBackgroundShadowPaddingEnd + ((((index - this.mMessageWidth) - this.mScaleEndVerticalThumbWidth) - index_6) - this.mMessageMarginEnd);
            float value_6 = -this.mCurrentThumbTranslateX;
            float value_7 = this.mCurrentThumbShadowX;
            index_8 = (index - index_7) - this.mThumbDrawableBackgroundScaleCenterX;
            value_9 = value_5;
            value_10 = value_6;
            value_11 = value_7;
        }
        int iSave = canvas.save();
        canvas.translate(index_7, index_3);
        int iSave2 = canvas.save();
        float value_8 = index_8;
        canvas.scale(this.mCurrentWidthScale, this.mCurrentHeightScale, value_8, this.mThumbDrawableBackgroundScaleCenterY);
        this.mVerticalThumbDrawableBackground.draw(canvas);
        canvas.restoreToCount(iSave2);
        canvas.translate(value_11, value_3);
        canvas.translate(value_10, value_2);
        canvas.scale(this.mCurrentWidthScale, this.mCurrentHeightScale, value_8, this.mThumbDrawableBackgroundScaleCenterY);
        this.mVerticalThumbDrawable.draw(canvas);
        canvas.restoreToCount(iSave);
        if (!this.mNeedShowMessage || this.mMessageAlphaAnimatedValue == 0.0f) {
            return;
        }
        int iSave3 = canvas.save();
        canvas.translate(value_9, value);
        this.mMessageBackgroundDrawable.draw(canvas);
        canvas.translate(0.0f, value_4);
        canvas.drawText(this.mRealShowMessage, this.mTextX, this.mTextY, this.mMessagePaint);
        canvas.restoreToCount(iSave3);
    }

    private void executePressAnimator(boolean flag) {
        this.mWidthScaleHolder.setFloatValues(this.mCurrentWidthScale, flag ? this.mWidthEndScale : 1.0f);
        this.mHeightScaleHolder.setFloatValues(this.mCurrentHeightScale, flag ? this.mHeightEndScale : 1.0f);
        this.mThumbTranslateXHolder.setFloatValues(this.mCurrentThumbTranslateScaleX, flag ? 1.0f : 0.0f);
        if (this.mNeedShowMessage) {
            this.mMessageAlphaAnimator.setFloatValues(this.mMessageAlphaAnimatedValue, flag ? 1.0f : 0.0f);
        }
        this.mPressAnimators.start();
    }

    private boolean filterVibrator() {
        if (this.lastVibratorTime == -1) {
            this.lastVibratorTime = System.currentTimeMillis();
            return false;
        }
        if (System.currentTimeMillis() - this.lastVibratorTime < MIN_VIBRATOR_TIME) {
            return true;
        }
        this.lastVibratorTime = System.currentTimeMillis();
        return false;
    }

    private int[] getVerticalRange() {
        int[] iArr = this.mVerticalRange;
        iArr[0] = this.mThumbTopMargin;
        iArr[1] = this.mRecyclerViewHeight - this.mThumbBottomMargin;
        return iArr;
    }


    public void hide(int index) {
        int index_2 = this.mAnimationState;
        if (index_2 == ANIMATION_STATE_FADING_IN) {
            this.mShowHideAnimator.cancel();
        } else if (index_2 != ANIMATION_STATE_IN) {
            return;
        }
        this.mAnimationState = ANIMATION_STATE_FADING_OUT;
        ValueAnimator valueAnimator = this.mShowHideAnimator;
        valueAnimator.setFloatValues(((Float) valueAnimator.getAnimatedValue()).floatValue(), 0.0f);
        this.mShowHideAnimator.setDuration(index);
        this.mShowHideAnimator.start();
    }

    private void initAnimators() {
        this.mShowHideAnimator.addListener(new d());
        this.mShowHideAnimator.addUpdateListener(new e());
        this.mShowHideAnimator.setInterpolator(this.mCommonInterpolator);
        a aVar = null;
        this.mThumbScaleAnimatorUpdateListener = new h(this, aVar);
        this.mPressAnimatorListener = new g();
        this.mMessageAnimatorUpdateListener = new f(this, aVar);
        this.mWidthScaleHolder = PropertyValuesHolder.ofFloat(WIDTH_ANIM_HOLDER, 0.0f, 0.0f);
        this.mHeightScaleHolder = PropertyValuesHolder.ofFloat(HEIGHT_ANIM_HOLDER, 0.0f, 0.0f);
        PropertyValuesHolder propertyValuesHolderOfFloat = PropertyValuesHolder.ofFloat(THUMB_TRANSLATE_X_HOLDER, 0.0f, 0.0f);
        this.mThumbTranslateXHolder = propertyValuesHolderOfFloat;
        ValueAnimator valueAnimatorOfPropertyValuesHolder = ValueAnimator.ofPropertyValuesHolder(this.mWidthScaleHolder, this.mHeightScaleHolder, propertyValuesHolderOfFloat);
        this.mThumbScaleAnimator = valueAnimatorOfPropertyValuesHolder;
        valueAnimatorOfPropertyValuesHolder.setDuration(200L);
        this.mThumbScaleAnimator.setInterpolator(this.mCommonInterpolator);
        this.mThumbScaleAnimator.addUpdateListener(this.mThumbScaleAnimatorUpdateListener);
        ValueAnimator valueAnimatorOfFloat = ValueAnimator.ofFloat(new float[0]);
        this.mMessageAlphaAnimator = valueAnimatorOfFloat;
        valueAnimatorOfFloat.addUpdateListener(this.mMessageAnimatorUpdateListener);
        this.mMessageAlphaAnimator.setDuration(SHOW_DURATION_MS);
        this.mMessageAlphaAnimator.setInterpolator(this.mCommonInterpolator);
        resetPressAnimator(false);
    }

    private void initMessagePaint(Context context) {
        TextPaint textPaint = new TextPaint();
        this.mMessagePaint = textPaint;
        textPaint.setAntiAlias(true);
        this.mMessagePaint.setTextSize(context.getResources().getDimensionPixelSize(R.dimen.coui_fast_scroller_message_text_size));
        this.mMessagePaint.setTypeface(Typeface.create(MEDIUM_FONT, 0));
        this.mMessagePaint.setColor(COUIContextUtil.getAttrColor(context, R.attr.couiColorLabelPrimary));
        this.mMessagePaint.setAlpha(0);
        Paint.FontMetrics fontMetrics = this.mMessagePaint.getFontMetrics();
        float value = fontMetrics.bottom;
        this.mTextY = ((this.mScaleEndVerticalThumbHeight + (value - fontMetrics.top)) / 2.0f) - value;
    }

    private void initOrResetVelocityTracker() {
        VelocityTracker velocityTracker = this.mVelocityTracker;
        if (velocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        } else {
            velocityTracker.clear();
        }
    }

    private void initVelocityTrackerIfNotExists() {
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
    }

    private boolean isLayoutRTL() {
        return ViewCompat.getLayoutDirection(this.mRecyclerView) == 1;
    }

    private void letGo() {
        int index = this.mPressAnimatorState;
        if (index == 1) {
            this.mPressAnimators.cancel();
        } else if (index != 2) {
            return;
        }
        this.mPressAnimatorState = 3;
        executePressAnimator(false);
    }

    private boolean performAdaptiveFeedback() {
        VelocityTracker velocityTracker;
        if (this.mLinearMotorVibrator == null) {
            Object linearMotorVibrator = VibrateUtils.getLinearMotorVibrator(this.mRecyclerView.getContext());
            this.mLinearMotorVibrator = linearMotorVibrator;
            this.mHasMotorVibrator = linearMotorVibrator != null;
        }
        if (this.mLinearMotorVibrator == null || (velocityTracker = this.mVelocityTracker) == null) {
            return false;
        }
        velocityTracker.computeCurrentVelocity(this.mTrackerPeriod, this.mTrackerMaxVelocity);
        int iAbs = (int) Math.abs(this.mVelocityTracker.getYVelocity());
        int index = iAbs > this.mMidVelocityThreshold ? 0 : 1;
        if ((iAbs > MIN_VELOCITY_WEAKEST && iAbs < MIN_VELOCITY_WEAK && filterVibrator()) || iAbs < MIN_VELOCITY_WEAKEST) {
            return true;
        }
        VibrateUtils.setLinearMotorVibratorStrength(this.mLinearMotorVibrator, index, iAbs, this.mTrackerMaxVelocity, 1200, VibrateUtils.STRENGTH_MAX_GRANULAR, this.mVibrateLevel, this.mVibrateIntensity);
        return true;
    }


    public void performFeedback() {
        if (this.mHasMotorVibrator && this.mEnableAdaptiveVibrator) {
            performAdaptiveFeedback();
        }
    }

    private void press() {
        int index = this.mPressAnimatorState;
        if (index != 0) {
            if (index != 3) {
                return;
            } else {
                this.mPressAnimators.cancel();
            }
        }
        this.mPressAnimatorState = 1;
        executePressAnimator(true);
    }

    private void recycleVelocityTracker() {
        VelocityTracker velocityTracker = this.mVelocityTracker;
        if (velocityTracker != null) {
            velocityTracker.recycle();
            this.mVelocityTracker = null;
        }
    }

    private void resetHideDelay(int index) {
        cancelHide();
        if (this.mIsThumbAlwaysShow) {
            return;
        }
        this.mRecyclerView.postDelayed(this.mHideRunnable, index);
    }

    private void resetPressAnimator(boolean flag) {
        AnimatorSet animatorSet = new AnimatorSet();
        this.mPressAnimators = animatorSet;
        animatorSet.play(this.mThumbScaleAnimator);
        this.mPressAnimators.addListener(this.mPressAnimatorListener);
        if (flag) {
            this.mPressAnimators.playTogether(this.mMessageAlphaAnimator);
        }
    }

    private int scrollTo(float value, float value_2, int[] iArr, int index) {
        int index_2 = iArr[1] - iArr[0];
        if (index_2 == 0) {
            return 0;
        }
        return (int) (((value_2 - value) / index_2) * (index - this.mRecyclerViewHeight));
    }

    private void setupCallbacks() {
        this.mRecyclerView.addItemDecoration(this);
        this.mRecyclerView.addOnItemTouchListener(this);
        this.mRecyclerView.addOnScrollListener(this.mOnScrollListener);
        this.mRecyclerView.addOnAttachStateChangeListener(new c());
    }

    private void show() {
        int index = this.mAnimationState;
        if (index != ANIMATION_STATE_OUT) {
            if (index != ANIMATION_STATE_FADING_OUT) {
                return;
            } else {
                this.mShowHideAnimator.cancel();
            }
        }
        this.mAnimationState = ANIMATION_STATE_FADING_IN;
        ValueAnimator valueAnimator = this.mShowHideAnimator;
        valueAnimator.setFloatValues(((Float) valueAnimator.getAnimatedValue()).floatValue(), 1.0f);
        this.mShowHideAnimator.setDuration(SHOW_DURATION_MS);
        this.mShowHideAnimator.start();
    }

    private void verticalScrollTo(float value) {
        int iScrollTo;
        int[] verticalRange = getVerticalRange();
        if (((value <= verticalRange[0] || value >= verticalRange[1]) && !this.mRecyclerView.canScrollVertically(1)) || Math.abs(this.mVerticalThumbCenterY - value) < 2.0f || (iScrollTo = scrollTo(this.mVerticalDragY, value, verticalRange, this.mRecyclerView.computeVerticalScrollRange())) == 0) {
            return;
        }
        this.mRecyclerView.scrollBy(0, iScrollTo);
        this.mVerticalDragY = value;
    }

    public void attachToRecyclerView(RecyclerView recyclerView) {
        RecyclerView recyclerView2 = this.mRecyclerView;
        if (recyclerView2 == recyclerView) {
            return;
        }
        if (recyclerView2 != null) {
            destroyCallbacks();
        }
        this.mRecyclerView = recyclerView;
        if (recyclerView != null) {
            setupCallbacks();
        }
    }

    public boolean getEnable() {
        return this.mEnabled;
    }

    public String getMessage() {
        return this.mMessage;
    }

    public boolean getNeedShowMessage() {
        return this.mNeedShowMessage;
    }

    public String getRealShowMessage() {
        return this.mRealShowMessage;
    }

    public boolean getThumbAlwaysShow() {
        return this.mIsThumbAlwaysShow;
    }

    public int getThumbBottomMargin() {
        return this.mThumbBottomMargin;
    }

    public int getThumbTopMargin() {
        return this.mThumbTopMargin;
    }

    Drawable getVerticalThumbDrawable() {
        return this.mVerticalThumbDrawableBackground;
    }

    public boolean isDragging() {
        return this.mState == STATE_DRAGGING;
    }

    /**
     * Returns whether the active gesture is dragging the vertical thumb.
     *
     * <p>Leapy added 2026-08-01: expose the decoded {@code DRAG_Y} state to
     * clients without reflecting the private {@code mDragState} field. D8 may
     * inline and remove the private static-final constant, so reflective access
     * is not a stable library contract.</p>
     */
    public boolean isDraggingVertically() {
        return isDragging() && this.mDragState == DRAG_Y;
    }

    boolean isPointInsideVerticalThumb(float value, float value_2) {
        int index = this.mDefaultVerticalThumbWidth;
        int index_2 = this.mDefaultVerticalMarginEnd;
        int index_3 = this.mThumbBackgroundShadowPaddingEnd;
        float value_3 = (index + index_2) - (index_3 * TOUCH_SCALE_FACTOR);
        float value_4 = ((this.mRecyclerViewWidth - index) - index_2) + (index_3 * TOUCH_SCALE_FACTOR);
        int index_4 = this.mVerticalThumbCenterY;
        int index_5 = this.mDefaultVerticalThumbHeight;
        int index_6 = this.mThumbBackgroundShadowPaddingTop;
        float value_5 = (index_4 - (index_5 / 2.0f)) + (index_6 * TOUCH_SCALE_FACTOR);
        float value_6 = (index_4 + (index_5 / 2.0f)) - (index_6 * TOUCH_SCALE_FACTOR);
        if (!isLayoutRTL() ? value >= value_4 : value <= value_3) {
            if (value_2 >= value_5 && value_2 <= value_6) {
                return true;
            }
        }
        return false;
    }

    public boolean isVisible() {
        return this.mState == STATE_VISIBLE;
    }

    @Override
    public void onDrawOver(Canvas canvas, RecyclerView recyclerView, RecyclerView.State state) {
        if (this.mRecyclerViewWidth != this.mRecyclerView.getWidth() || this.mRecyclerViewHeight != this.mRecyclerView.getHeight()) {
            this.mRecyclerViewWidth = this.mRecyclerView.getWidth();
            this.mRecyclerViewHeight = this.mRecyclerView.getHeight();
            setState(0);
        } else {
            if (this.mAnimationState == ANIMATION_STATE_OUT || !this.mNeedVerticalScrollbar) {
                return;
            }
            drawVerticalScrollbar(canvas);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {
        int index = this.mState;
        if (index == 1) {
            boolean zIsPointInsideVerticalThumb = isPointInsideVerticalThumb(motionEvent.getX(), motionEvent.getY());
            if (motionEvent.getAction() != 0 || !zIsPointInsideVerticalThumb) {
                return false;
            }
            this.mDragState = DRAG_Y;
            this.mVerticalDragY = (int) motionEvent.getY();
            setState(2);
        } else if (index != 2) {
            return false;
        }
        return true;
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean flag) {
    }

    @Override
    public void onTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {
        if (this.mState == STATE_HIDDEN) {
            return;
        }
        if (this.mEnableAdaptiveVibrator) {
            computeVelocityWithTouchEvent(motionEvent);
        }
        int action = motionEvent.getAction();
        if (action == 0) {
            if (isPointInsideVerticalThumb(motionEvent.getX(), motionEvent.getY())) {
                this.mDragState = DRAG_Y;
                this.mVerticalDragY = (int) motionEvent.getY();
                setState(2);
                return;
            }
            return;
        }
        if (action != 1) {
            if (action == 2) {
                if (this.mState == STATE_DRAGGING) {
                    show();
                    if (this.mDragState == DRAG_Y) {
                        verticalScrollTo(motionEvent.getY());
                        return;
                    }
                    return;
                }
                return;
            }
            if (action != 3) {
                return;
            }
        }
        if (this.mState == STATE_DRAGGING) {
            this.mVerticalDragY = 0.0f;
            setState(1);
            this.mDragState = DRAG_NONE;
        }
    }

    void requestRedraw() {
        this.mRecyclerView.invalidate();
    }

    public void setEnable(boolean enable) {
        this.mEnabled = enable;
        if (enable || this.mState == STATE_HIDDEN) {
            return;
        }
        hide(HIDE_DURATION_MS);
    }

    public void setEnableAdaptiveVibrator(boolean enableAdaptiveVibrator) {
        this.mEnableAdaptiveVibrator = enableAdaptiveVibrator;
    }

    public void setMessage(String str) {
        if (str == null || str.equals(this.mMessage) || str.trim().equals("")) {
            return;
        }
        this.mMessage = str;
        this.mRealShowMessage = str;
        float fMeasureText = this.mMessagePaint.measureText(str);
        this.mTextWidth = fMeasureText;
        float messageWidth = fMeasureText + this.mMessageTextPadding + this.mMessageBackgroundInternalPadding;
        this.mMessageWidth = messageWidth;
        if (messageWidth > this.mMessageMaximumWidth) {
            for (int index = 1; index < str.length(); index++) {
                String realShowMessage = str.substring(0, str.length() - index) + this.mDots;
                this.mRealShowMessage = realShowMessage;
                float fMeasureText2 = this.mMessagePaint.measureText(realShowMessage);
                this.mTextWidth = fMeasureText2;
                float messageWidth_2 = fMeasureText2 + this.mMessageTextPadding + this.mMessageBackgroundInternalPadding;
                this.mMessageWidth = messageWidth_2;
                if (messageWidth_2 <= this.mMessageMaximumWidth) {
                    break;
                }
            }
        } else {
            int messageWidth_3 = this.mMessageMinimumWidth;
            if (messageWidth < messageWidth_3) {
                this.mMessageWidth = messageWidth_3;
            }
        }
        this.mMessageBackgroundDrawable.setBounds(0, 0, (int) this.mMessageWidth, this.mMessageBackgroundHeight);
        this.mTextX = (this.mMessageWidth - this.mTextWidth) / 2.0f;
        requestRedraw();
    }

    public void setNeedShowMessage(boolean needShowMessage) {
        if (this.mNeedShowMessage != needShowMessage) {
            resetPressAnimator(needShowMessage);
            this.mNeedShowMessage = needShowMessage;
            requestRedraw();
        }
    }

    void setState(int state) {
        if (state == 2 && this.mState != STATE_DRAGGING) {
            press();
            cancelHide();
        }
        if (state == 0) {
            requestRedraw();
        } else {
            show();
        }
        if (this.mState == STATE_DRAGGING && state != 2) {
            resetHideDelay(HIDE_DELAY_AFTER_VISIBLE_MS);
            letGo();
        } else if (state == 1) {
            resetHideDelay(HIDE_DELAY_AFTER_VISIBLE_MS);
        }
        this.mState = state;
    }

    public void setThumbAlwaysShow(boolean thumbAlwaysShow) {
        if (thumbAlwaysShow != this.mIsThumbAlwaysShow) {
            this.mIsThumbAlwaysShow = thumbAlwaysShow;
            if (thumbAlwaysShow) {
                cancelHide();
            } else if (this.mState == STATE_VISIBLE) {
                resetHideDelay(HIDE_DELAY_AFTER_VISIBLE_MS);
            }
        }
    }

    public void setThumbBottomMargin(int thumbBottomMargin) {
        this.mThumbBottomMargin = thumbBottomMargin;
    }

    public void setThumbTopMargin(int thumbTopMargin) {
        this.mThumbTopMargin = thumbTopMargin;
    }

    public void setVibrateIntensity(float vibrateIntensity) {
        this.mVibrateIntensity = vibrateIntensity;
    }

    public void setVibrateLevel(int vibrateLevel) {
        this.mVibrateLevel = vibrateLevel;
    }

    void updateScrollPosition(int index, int index_2) {
        int[] verticalRange = getVerticalRange();
        int iComputeVerticalScrollRange = this.mRecyclerView.computeVerticalScrollRange();
        int index_3 = verticalRange[1];
        int index_4 = verticalRange[0];
        int verticalThumbCenterY = index_3 - index_4;
        boolean needVerticalScrollbar = iComputeVerticalScrollRange - verticalThumbCenterY > 0 && this.mRecyclerViewHeight >= this.mScrollbarMinimumRange;
        this.mNeedVerticalScrollbar = needVerticalScrollbar;
        if (!needVerticalScrollbar) {
            if (this.mState != 0) {
                setState(0);
                return;
            }
            return;
        }
        // Leapy modified 2026-08-01: Match decoded int-to-float/div-float.
        // Integer division pins the thumb to the start until the final scroll
        // position, which is why the recovered scroller appeared unchanged.
        float value = (float) index_2
                / (iComputeVerticalScrollRange - this.mRecyclerViewHeight);
        // Leapy end
        if (value > 1.0f) {
            this.mVerticalThumbCenterY = verticalThumbCenterY + index_4;
        } else {
            this.mVerticalThumbCenterY = (int) ((value * verticalThumbCenterY) + index_4);
        }
        int index_5 = this.mState;
        if (index_5 == 0 || index_5 == 1) {
            setState(1);
        }
    }
}
// Leapy end
