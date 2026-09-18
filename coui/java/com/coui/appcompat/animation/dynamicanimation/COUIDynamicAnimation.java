package com.coui.appcompat.animation.dynamicanimation;

import android.os.Looper;
import android.util.AndroidRuntimeException;
import android.view.View;

import androidx.core.view.ViewCompat;
import androidx.dynamicanimation.animation.FloatPropertyCompat;
import androidx.dynamicanimation.animation.FloatValueHolder;

import java.util.ArrayList;

public abstract class COUIDynamicAnimation<T extends COUIDynamicAnimation<T>>
        implements COUIAnimationHandler.AnimationFrameCallback {
    public static final float MIN_VISIBLE_CHANGE_ALPHA = 0.00390625f;
    public static final float MIN_VISIBLE_CHANGE_PIXELS = 1.0f;
    public static final float MIN_VISIBLE_CHANGE_ROTATION_DEGREES = 0.1f;
    public static final float MIN_VISIBLE_CHANGE_SCALE = 0.002f;
    private static final float THRESHOLD_MULTIPLIER = 0.75f;
    private static final float UNSET = Float.MAX_VALUE;

    private COUIAnimationHandler mAnimationHandler;
    private long mElapsedTime;
    boolean mEnableNonMainThread;
    private final ArrayList<OnAnimationEndListener> mEndListeners = new ArrayList<>();
    private boolean mIsLogicallyEnd;
    private long mLastFrameTime;
    private final ArrayList<OnLogicallyCompleteListener> mLogicallyCompleteListeners =
            new ArrayList<>();
    float mMaxValue = Float.MAX_VALUE;
    float mMinValue = -Float.MAX_VALUE;
    private float mMinVisibleChange;
    final FloatPropertyCompat mProperty;
    boolean mRunning;
    boolean mStartValueIsSet;
    final Object mTarget;
    private final ArrayList<OnAnimationUpdateListener> mUpdateListeners = new ArrayList<>();
    protected float mValue = UNSET;
    float mVelocity;

    public static final ViewProperty TRANSLATION_X = new ViewProperty("translationX") {
        @Override
        public float getValue(View view) {
            return view.getTranslationX();
        }

        @Override
        public void setValue(View view, float value) {
            view.setTranslationX(value);
        }
    };
    public static final ViewProperty TRANSLATION_Y = new ViewProperty("translationY") {
        @Override
        public float getValue(View view) {
            return view.getTranslationY();
        }

        @Override
        public void setValue(View view, float value) {
            view.setTranslationY(value);
        }
    };
    public static final ViewProperty TRANSLATION_Z = new ViewProperty("translationZ") {
        @Override
        public float getValue(View view) {
            return ViewCompat.getTranslationZ(view);
        }

        @Override
        public void setValue(View view, float value) {
            ViewCompat.setTranslationZ(view, value);
        }
    };
    public static final ViewProperty SCALE_X = new ViewProperty("scaleX") {
        @Override
        public float getValue(View view) {
            return view.getScaleX();
        }

        @Override
        public void setValue(View view, float value) {
            view.setScaleX(value);
        }
    };
    public static final ViewProperty SCALE_Y = new ViewProperty("scaleY") {
        @Override
        public float getValue(View view) {
            return view.getScaleY();
        }

        @Override
        public void setValue(View view, float value) {
            view.setScaleY(value);
        }
    };
    public static final ViewProperty ROTATION = new ViewProperty("rotation") {
        @Override
        public float getValue(View view) {
            return view.getRotation();
        }

        @Override
        public void setValue(View view, float value) {
            view.setRotation(value);
        }
    };
    public static final ViewProperty ROTATION_X = new ViewProperty("rotationX") {
        @Override
        public float getValue(View view) {
            return view.getRotationX();
        }

        @Override
        public void setValue(View view, float value) {
            view.setRotationX(value);
        }
    };
    public static final ViewProperty ROTATION_Y = new ViewProperty("rotationY") {
        @Override
        public float getValue(View view) {
            return view.getRotationY();
        }

        @Override
        public void setValue(View view, float value) {
            view.setRotationY(value);
        }
    };
    public static final ViewProperty X = new ViewProperty("x") {
        @Override
        public float getValue(View view) {
            return view.getX();
        }

        @Override
        public void setValue(View view, float value) {
            view.setX(value);
        }
    };
    public static final ViewProperty Y = new ViewProperty("y") {
        @Override
        public float getValue(View view) {
            return view.getY();
        }

        @Override
        public void setValue(View view, float value) {
            view.setY(value);
        }
    };
    public static final ViewProperty Z = new ViewProperty("z") {
        @Override
        public float getValue(View view) {
            return ViewCompat.getZ(view);
        }

        @Override
        public void setValue(View view, float value) {
            ViewCompat.setZ(view, value);
        }
    };
    public static final ViewProperty ALPHA = new ViewProperty("alpha") {
        @Override
        public float getValue(View view) {
            return view.getAlpha();
        }

        @Override
        public void setValue(View view, float value) {
            view.setAlpha(value);
        }
    };
    public static final ViewProperty SCROLL_X = new ViewProperty("scrollX") {
        @Override
        public float getValue(View view) {
            return view.getScrollX();
        }

        @Override
        public void setValue(View view, float value) {
            view.setScrollX((int) value);
        }
    };
    public static final ViewProperty SCROLL_Y = new ViewProperty("scrollY") {
        @Override
        public float getValue(View view) {
            return view.getScrollY();
        }

        @Override
        public void setValue(View view, float value) {
            view.setScrollY((int) value);
        }
    };

    public static class MassState {
        public float mValue;
        public float mVelocity;
    }

    public interface OnAnimationEndListener {
        void onAnimationEnd(COUIDynamicAnimation animation, boolean canceled, float value,
                float velocity);
    }

    public interface OnAnimationUpdateListener {
        void onAnimationUpdate(COUIDynamicAnimation animation, float value, float velocity);
    }

    public interface OnLogicallyCompleteListener {
        void onLogicallyComplete(COUIDynamicAnimation animation, boolean canceled, float value,
                float velocity);
    }

    public static abstract class ViewProperty extends FloatPropertyCompat<View> {
        private ViewProperty(String name) {
            super(name);
        }
    }

    public COUIDynamicAnimation(final FloatValueHolder valueHolder) {
        mTarget = null;
        mProperty = new FloatPropertyCompat<Object>("FloatValueHolder") {
            @Override
            public float getValue(Object object) {
                return valueHolder.getValue();
            }

            @Override
            public void setValue(Object object, float value) {
                valueHolder.setValue(value);
            }
        };
        mMinVisibleChange = MIN_VISIBLE_CHANGE_PIXELS;
    }

    public <K> COUIDynamicAnimation(K target, FloatPropertyCompat<K> property) {
        mTarget = target;
        mProperty = property;
        if (property == ROTATION || property == ROTATION_X || property == ROTATION_Y) {
            mMinVisibleChange = MIN_VISIBLE_CHANGE_ROTATION_DEGREES;
        } else if (property == ALPHA || property == SCALE_X || property == SCALE_Y) {
            mMinVisibleChange = MIN_VISIBLE_CHANGE_ALPHA;
        } else {
            mMinVisibleChange = MIN_VISIBLE_CHANGE_PIXELS;
        }
    }

    private COUIAnimationHandler getAnimationHandler() {
        return mAnimationHandler == null ? COUIAnimationHandler.getInstance() : mAnimationHandler;
    }

    private float getPropertyValue() {
        return mProperty.getValue(mTarget);
    }

    private void startAnimationInternal() {
        if (mRunning) {
            return;
        }
        mRunning = true;
        if (!mStartValueIsSet) {
            mValue = getPropertyValue();
        }
        if (mValue > mMaxValue || mValue < mMinValue) {
            throw new IllegalArgumentException(
                    "Starting value need to be in between min value and max value");
        }
        getAnimationHandler().addAnimationFrameCallback(this, 0L);
        mElapsedTime = 0L;
        mIsLogicallyEnd = false;
    }

    private void endAnimationInternal(boolean canceled) {
        mRunning = false;
        getAnimationHandler().removeCallback(this);
        mLastFrameTime = 0L;
        mStartValueIsSet = false;
        onLogicallyComplete(canceled);
        for (int i = 0; i < mEndListeners.size(); i++) {
            OnAnimationEndListener listener = mEndListeners.get(i);
            if (listener != null) {
                listener.onAnimationEnd(this, canceled, mValue, mVelocity);
            }
        }
        removeNullEntries(mEndListeners);
    }

    private void onLogicallyComplete(boolean canceled) {
        if (mIsLogicallyEnd) {
            return;
        }
        for (int i = 0; i < mLogicallyCompleteListeners.size(); i++) {
            OnLogicallyCompleteListener listener = mLogicallyCompleteListeners.get(i);
            if (listener != null) {
                listener.onLogicallyComplete(this, canceled, mValue, mVelocity);
            }
        }
        removeNullEntries(mLogicallyCompleteListeners);
        mIsLogicallyEnd = true;
    }

    private static <T> void removeEntry(ArrayList<T> list, T entry) {
        int index = list.indexOf(entry);
        if (index >= 0) {
            list.set(index, null);
        }
    }

    private static <T> void removeNullEntries(ArrayList<T> list) {
        for (int i = list.size() - 1; i >= 0; i--) {
            if (list.get(i) == null) {
                list.remove(i);
            }
        }
    }

    public T addEndListener(OnAnimationEndListener listener) {
        if (!mEndListeners.contains(listener)) {
            mEndListeners.add(listener);
        }
        return (T) this;
    }

    public T addLogicallyCompleteListener(OnLogicallyCompleteListener listener) {
        if (!mLogicallyCompleteListeners.contains(listener)) {
            mLogicallyCompleteListeners.add(listener);
        }
        return (T) this;
    }

    public T addUpdateListener(OnAnimationUpdateListener listener) {
        if (isRunning()) {
            throw new UnsupportedOperationException(
                    "Error: Update listeners must be added beforethe animation.");
        }
        if (!mUpdateListeners.contains(listener)) {
            mUpdateListeners.add(listener);
        }
        return (T) this;
    }

    public void cancel() {
        if (!mEnableNonMainThread && Looper.myLooper() != Looper.getMainLooper()) {
            throw new AndroidRuntimeException("Animations may only be canceled on the main thread");
        }
        if (mRunning) {
            endAnimationInternal(true);
        }
    }

    @Override
    public boolean doAnimationFrame(long frameTime) {
        if (mLastFrameTime == 0L) {
            mLastFrameTime = frameTime;
            setPropertyValue(mValue);
            return false;
        }
        long delta = frameTime - mLastFrameTime;
        onAnimate(delta);
        mLastFrameTime = frameTime;
        boolean finished = updateValueAndVelocity(delta);
        mValue = Math.max(Math.min(mValue, mMaxValue), mMinValue);
        setPropertyValue(mValue);
        if (isLogicEnd(mElapsedTime)) {
            onLogicallyComplete(false);
        }
        if (finished) {
            endAnimationInternal(false);
        }
        return finished;
    }

    public abstract float getAcceleration(float value, float velocity);

    public float getMinimumVisibleChange() {
        return mMinVisibleChange;
    }

    public float getTargetPropertyValue() {
        return getPropertyValue();
    }

    public float getValueThreshold() {
        return mMinVisibleChange * THRESHOLD_MULTIPLIER;
    }

    public abstract boolean isAtEquilibrium(float value, float velocity);

    public boolean isLogicEnd(long elapsedMillis) {
        return false;
    }

    public boolean isRunning() {
        return mRunning;
    }

    public boolean isStartValueSet() {
        return mStartValueIsSet;
    }

    public void onAnimate(long deltaMillis) {
        mElapsedTime += deltaMillis;
    }

    public void removeEndListener(OnAnimationEndListener listener) {
        removeEntry(mEndListeners, listener);
    }

    public void removeLogicallyCompleteListener(OnLogicallyCompleteListener listener) {
        removeEntry(mLogicallyCompleteListeners, listener);
    }

    public void removeUpdateListener(OnAnimationUpdateListener listener) {
        removeEntry(mUpdateListeners, listener);
    }

    public void setAnimationHandler(COUIAnimationHandler handler) {
        mAnimationHandler = handler;
    }

    public T setEnableNonMainThread(boolean enableNonMainThread) {
        mEnableNonMainThread = enableNonMainThread;
        return (T) this;
    }

    public T setMaxValue(float maxValue) {
        mMaxValue = maxValue;
        return (T) this;
    }

    public T setMinValue(float minValue) {
        mMinValue = minValue;
        return (T) this;
    }

    public T setMinimumVisibleChange(float minimumVisibleChange) {
        if (minimumVisibleChange <= 0.0f) {
            throw new IllegalArgumentException("Minimum visible change must be positive.");
        }
        mMinVisibleChange = minimumVisibleChange;
        setValueThreshold(minimumVisibleChange * THRESHOLD_MULTIPLIER);
        return (T) this;
    }

    public void setPropertyValue(float value) {
        mProperty.setValue(mTarget, value);
        for (int i = 0; i < mUpdateListeners.size(); i++) {
            OnAnimationUpdateListener listener = mUpdateListeners.get(i);
            if (listener != null) {
                listener.onAnimationUpdate(this, mValue, mVelocity);
            }
        }
        removeNullEntries(mUpdateListeners);
    }

    public T setStartValue(float value) {
        mValue = value;
        mStartValueIsSet = true;
        return (T) this;
    }

    public T setStartVelocity(float velocity) {
        mVelocity = velocity;
        return (T) this;
    }

    public abstract void setValueThreshold(float threshold);

    public void start() {
        if (!mEnableNonMainThread && Looper.myLooper() != Looper.getMainLooper()) {
            throw new AndroidRuntimeException("Animations may only be started on the main thread");
        }
        if (!mRunning) {
            startAnimationInternal();
        }
    }

    public abstract boolean updateValueAndVelocity(long deltaMillis);
}
