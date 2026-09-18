package com.coui.appcompat.progressbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.view.ViewCompat;
import androidx.dynamicanimation.animation.FloatPropertyCompat;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

import com.coui.appcompat.R;

public class COUILoadProgress extends AppCompatButton {
    public static final int DEFAULT_UP_OR_DOWN = 0;
    public static final int UPING_OR_DOWNING = 1;
    public static final int UP_OR_DOWN_WAIT = 2;
    public static final int UP_OR_DOWN_FAIL = 3;
    public static final int INSTALL_HAVE_GIFT = 4;

    public static final int DEFAULT = DEFAULT_UP_OR_DOWN;
    public static final int ING = UPING_OR_DOWNING;
    public static final int WAIT = UP_OR_DOWN_WAIT;
    public static final int FAIL = UP_OR_DOWN_FAIL;
    public static final int COMPLETE = INSTALL_HAVE_GIFT;

    private static final float ONE_MILLION = 1.0E-7f;
    private static final int TIMEOUT_SEND_ACCESSIBILITY_EVENT = 10;
    private static final int[] DEFAULT_SET = {R.attr.coui_state_default};
    private static final int[] WAIT_SET = {R.attr.coui_state_wait};
    private static final int[] FAIL_SET = {R.attr.coui_state_fail};
    private static final int[] ING_SET = {R.attr.coui_state_ing};

    private AccessibilityEventSender mAccessibilityEventSender;
    private boolean mBroadcasting;
    protected Drawable mButtonDrawable;
    protected Drawable mButtonDrawableReverseColor;
    private int mButtonResource;
    protected boolean mIsUpdateWithAnimation;
    private final AccessibilityManager mManager;
    public int mMax;
    private OnStateChangeListener mOnStateChangeListener;
    private OnStateChangeListener mOnStateChangeWidgetListener;
    public int mProgress;
    public int mState;
    protected float mVisualProgress;
    private OnProgressAnimationUpdateListener mVisualProgressAnimationListener;
    private final FloatPropertyCompat<COUILoadProgress> mVisualProgressProperty =
            new FloatPropertyCompat<COUILoadProgress>("VisualProgressProperty") {
                @Override
                public float getValue(COUILoadProgress object) {
                    return object.mVisualProgress;
                }

                @Override
                public void setValue(COUILoadProgress object, float value) {
                    object.mVisualProgress = value;
                    if (object.mVisualProgressAnimationListener != null) {
                        object.mVisualProgressAnimationListener.onAnimationUpdate(
                                object.mVisualProgress, object.mState);
                    }
                    object.invalidate();
                }
            };
    private SpringAnimation mVisualProgressSpringAnimation;

    public class AccessibilityEventSender implements Runnable {
        @Override
        public void run() {
            sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
        }
    }

    public interface OnProgressAnimationUpdateListener {
        void onAnimationUpdate(float visualProgress, int state);
    }

    public interface OnStateChangeListener {
        void onStateChanged(COUILoadProgress view, int state);
    }

    public static class SavedState extends BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    @Override
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    @Override
                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
        int mProgress;
        int mState;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            mState = (Integer) in.readValue(null);
            mProgress = (Integer) in.readValue(null);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeValue(mState);
            out.writeValue(mProgress);
        }

        @Override
        public String toString() {
            return "CompoundButton.SavedState{"
                    + Integer.toHexString(System.identityHashCode(this))
                    + " mState = " + mState + " mProgress = " + mProgress + "}";
        }
    }

    public COUILoadProgress(Context context) {
        this(context, null);
    }

    public COUILoadProgress(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.couiLoadProgressStyle);
    }

    public COUILoadProgress(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Widget_COUI_COUILoadProgress);
    }

    public COUILoadProgress(Context context, @Nullable AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr);
        mManager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        init();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.COUILoadProgress,
                defStyleAttr, defStyleRes);
        int state = a.getInteger(R.styleable.COUILoadProgress_couiState, DEFAULT_UP_OR_DOWN);
        Drawable drawable = a.getDrawable(R.styleable.COUILoadProgress_couiDefaultDrawable);
        if (drawable != null) {
            setButtonDrawable(drawable);
        }
        setProgress(a.getInt(R.styleable.COUILoadProgress_couiProgress, mProgress), false);
        setState(state);
        a.recycle();
        if (ViewCompat.getImportantForAccessibility(this) == IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
            ViewCompat.setImportantForAccessibility(this, IMPORTANT_FOR_ACCESSIBILITY_YES);
        }
    }

    private void init() {
        mProgress = 0;
        mMax = 100;
    }

    private void refreshProgressWithAnim(int fromProgress) {
        if (mVisualProgressSpringAnimation == null) {
            SpringForce force = new SpringForce();
            force.setDampingRatio(SpringForce.DAMPING_RATIO_NO_BOUNCY);
            force.setStiffness(50f);
            mVisualProgressSpringAnimation = new SpringAnimation(this, mVisualProgressProperty);
            mVisualProgressSpringAnimation.setSpring(force);
            mVisualProgressSpringAnimation.addEndListener(
                    (animation, canceled, value, velocity) -> mIsUpdateWithAnimation = false);
        }
        if (mVisualProgressSpringAnimation.isRunning()) {
            mIsUpdateWithAnimation = true;
            mVisualProgressSpringAnimation.animateToFinalPosition(mProgress);
            return;
        }
        mVisualProgress = fromProgress;
        if (Math.abs(mVisualProgress - mProgress) <= ONE_MILLION) {
            mIsUpdateWithAnimation = false;
            invalidate();
            return;
        }
        mVisualProgressSpringAnimation.setStartValue(mVisualProgress);
        mVisualProgressSpringAnimation.animateToFinalPosition(mProgress);
        mVisualProgressSpringAnimation.setStartVelocity(0f);
        mIsUpdateWithAnimation = true;
    }

    private void scheduleAccessibilityEventSender() {
        if (mAccessibilityEventSender == null) {
            mAccessibilityEventSender = new AccessibilityEventSender();
        } else {
            removeCallbacks(mAccessibilityEventSender);
        }
        postDelayed(mAccessibilityEventSender, TIMEOUT_SEND_ACCESSIBILITY_EVENT);
    }

    private void skipAnimation() {
        if (mVisualProgressSpringAnimation != null && mVisualProgressSpringAnimation.isRunning()) {
            mIsUpdateWithAnimation = false;
            if (mVisualProgressSpringAnimation.canSkipToEnd()) {
                mVisualProgressSpringAnimation.skipToEnd();
            } else {
                mVisualProgressSpringAnimation.cancel();
            }
        }
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (mButtonDrawable != null) {
            mButtonDrawable.setState(getDrawableState());
            invalidate();
        }
    }

    public int getMax() {
        return mMax;
    }

    public int getMax(int ignored) {
        return mMax;
    }

    public int getProgress() {
        return mProgress;
    }

    public int getState() {
        return mState;
    }

    @Override
    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        if (mButtonDrawable != null) {
            mButtonDrawable.jumpToCurrentState();
        }
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (getState() == DEFAULT_UP_OR_DOWN) {
            mergeDrawableStates(drawableState, DEFAULT_SET);
        }
        if (getState() == UPING_OR_DOWNING) {
            mergeDrawableStates(drawableState, ING_SET);
        }
        if (getState() == UP_OR_DOWN_WAIT) {
            mergeDrawableStates(drawableState, WAIT_SET);
        }
        if (getState() == UP_OR_DOWN_FAIL) {
            mergeDrawableStates(drawableState, FAIL_SET);
        }
        return drawableState;
    }

    @Override
    protected void onDetachedFromWindow() {
        if (mAccessibilityEventSender != null) {
            removeCallbacks(mAccessibilityEventSender);
        }
        skipAnimation();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    public void onProgressRefresh(int progress) {
        if (mManager != null && mManager.isEnabled() && mManager.isTouchExplorationEnabled()) {
            scheduleAccessibilityEventSender();
        }
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        setState(savedState.mState);
        setProgress(savedState.mProgress);
        requestLayout();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        setFreezesText(true);
        SavedState savedState = new SavedState(super.onSaveInstanceState());
        savedState.mState = getState();
        savedState.mProgress = mProgress;
        return savedState;
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == GONE || visibility == INVISIBLE) {
            skipAnimation();
            invalidate();
        }
    }

    @Override
    public boolean performClick() {
        toggle();
        return super.performClick();
    }

    public void setButtonDrawable(int resId) {
        if (resId == 0 || resId != mButtonResource) {
            mButtonResource = resId;
            setButtonDrawable(resId != 0 ? getResources().getDrawable(mButtonResource) : null);
        }
    }

    public void setButtonDrawable(Drawable drawable) {
        if (drawable == null) {
            mButtonDrawable = null;
            mButtonDrawableReverseColor = null;
            mButtonResource = 0;
            return;
        }
        if (mButtonDrawable != null) {
            mButtonDrawable.setCallback(null);
            unscheduleDrawable(mButtonDrawable);
        }
        drawable.setCallback(this);
        drawable.setState(getDrawableState());
        drawable.setVisible(getVisibility() == VISIBLE, false);
        mButtonDrawable = drawable;
        Drawable.ConstantState state = drawable.getConstantState();
        mButtonDrawableReverseColor = state == null ? drawable.mutate() : state.newDrawable();
        mButtonDrawable.setState(null);
        setMinHeight(mButtonDrawable.getIntrinsicHeight());
        refreshDrawableState();
    }

    public void setMax(int max) {
        if (max < 0) {
            max = 0;
        }
        if (max != mMax) {
            mMax = max;
            if (mProgress > max) {
                mProgress = max;
            }
            invalidate();
        }
    }

    public void setOnStateChangeListener(OnStateChangeListener listener) {
        mOnStateChangeListener = listener;
    }

    public void setOnStateChangeWidgetListener(OnStateChangeListener listener) {
        mOnStateChangeWidgetListener = listener;
    }

    public void setProgress(int progress) {
        setProgress(progress, true);
    }

    public void setProgress(int progress, boolean animate) {
        if (progress < 0) {
            progress = 0;
        }
        if (progress > mMax) {
            progress = mMax;
        }
        if (animate) {
            int oldProgress = mProgress;
            if (progress != oldProgress) {
                mProgress = progress;
            }
            refreshProgressWithAnim(oldProgress);
            return;
        }
        if (progress != mProgress) {
            mProgress = progress;
        }
        if (mIsUpdateWithAnimation) {
            mIsUpdateWithAnimation = false;
        }
        mVisualProgress = mProgress;
        invalidate();
        onProgressRefresh(progress);
    }

    public void setState(int state) {
        if (mState != state) {
            mState = state;
            refreshDrawableState();
            if (mBroadcasting) {
                return;
            }
            mBroadcasting = true;
            if (mOnStateChangeListener != null) {
                mOnStateChangeListener.onStateChanged(this, mState);
            }
            if (mOnStateChangeWidgetListener != null) {
                mOnStateChangeWidgetListener.onStateChanged(this, mState);
            }
            mBroadcasting = false;
        }
    }

    public void setVisualProgressAnimationListener(OnProgressAnimationUpdateListener listener) {
        mVisualProgressAnimationListener = listener;
    }

    public void setOnProgressAnimationUpdateListener(OnProgressAnimationUpdateListener listener) {
        setVisualProgressAnimationListener(listener);
    }

    public void toggle() {
        int state = mState;
        if (state == DEFAULT_UP_OR_DOWN) {
            setState(UPING_OR_DOWNING);
        } else if (state == UPING_OR_DOWNING) {
            setState(UP_OR_DOWN_WAIT);
        } else if (state == UP_OR_DOWN_WAIT || state == UP_OR_DOWN_FAIL) {
            setState(UPING_OR_DOWNING);
        }
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        return super.verifyDrawable(who) || who == mButtonDrawable;
    }
}
