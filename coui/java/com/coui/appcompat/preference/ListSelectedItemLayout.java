package com.coui.appcompat.preference;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;

import com.coui.appcompat.animation.COUILinearInterpolator;
import com.coui.appcompat.list.ConfigurationChangedListener;
import com.coui.appcompat.list.ICardListSelectedItem;
import com.coui.appcompat.list.IListSelectedItem;
import com.coui.appcompat.state.DrawableStateProxy;
import com.coui.appcompat.state.COUIMaskEffectDrawable;
import com.coui.appcompat.state.COUIStateEffectDrawable;

public class ListSelectedItemLayout extends COUICheckedLinearLayout implements IListSelectedItem, ICardListSelectedItem {
    @Deprecated protected static final int APPEAR_DURATION = 150;
    @Deprecated protected static final int DISAPPEAR_DURATION = 367;
    @Deprecated protected static final int STATE_BACKGROUND_APPEAR = 1;
    @Deprecated protected static final int STATE_BACKGROUND_DISAPPEAR = 2;
    @Deprecated protected Interpolator mAppearInterpolator;
    @Deprecated protected ValueAnimator mBackgroundAppearAnimator;
    @Deprecated protected ValueAnimator mBackgroundDisappearAnimator;
    @Deprecated protected Interpolator mDisappearInterpolator;
    @Deprecated protected boolean mNeedAutoStartDisAppear;
    @Deprecated protected int mState;

    private boolean mBackgroundAnimationEnabled;
    private boolean mConsumeDispatchingEventForState;
    private final RectF mLayoutRect;
    private COUIMaskEffectDrawable mMaskDrawable;
    private COUIStateEffectDrawable mStateEffectBackground;

    public ListSelectedItemLayout(Context context) {
        this(context, null);
    }

    public ListSelectedItemLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ListSelectedItemLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ListSelectedItemLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mLayoutRect = new RectF();
        mBackgroundAnimationEnabled = true;
        mConsumeDispatchingEventForState = false;
        mNeedAutoStartDisAppear = false;
        mState = STATE_BACKGROUND_DISAPPEAR;
        mDisappearInterpolator = new PathInterpolator(0.17f, 0.17f, 0.67f, 1.0f);
        mAppearInterpolator = new COUILinearInterpolator();
        initStateEffectBackground();
        setDefaultFocusHighlightEnabled(false);
    }

    private void initStateEffectBackground() {
        mMaskDrawable = new COUIMaskEffectDrawable(getContext(), COUIMaskEffectDrawable.MASK_EFFECT_TYPE_CONTAINER_WIDGET);
        Path layoutPath = getLayoutPath();
        if (layoutPath != null) {
            mMaskDrawable.setMaskPath(layoutPath);
        } else {
            mMaskDrawable.setMaskRect(mLayoutRect, 0.0f, 0.0f);
        }
        Drawable[] layers = new Drawable[2];
        layers[0] = getBackground() == null ? new ColorDrawable(0) : getBackground();
        layers[1] = mMaskDrawable;
        mStateEffectBackground = new COUIStateEffectDrawable(layers);
        mStateEffectBackground.setAnimateEnabled(mBackgroundAnimationEnabled);
        super.setBackground(mStateEffectBackground);
    }

    private void handlePressAnimationInternal(MotionEvent event) {
        if (isEnabled() && isClickable() && mBackgroundAnimationEnabled) {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_DOWN) {
                startAppearAnimation();
            } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                startDisAppearAnimationOrNot();
            }
        }
    }

    public void consumeDispatchingEventForState(boolean consume) {
        mConsumeDispatchingEventForState = consume;
    }

    @Override
    public boolean dispatchHoverEvent(MotionEvent event) {
        if (mConsumeDispatchingEventForState) {
            if (isEnabled() && event.getActionMasked() == MotionEvent.ACTION_HOVER_ENTER) {
                mMaskDrawable.setStateLocked(DrawableStateProxy.STATE_HOVERED, true, true, true);
            }
            if (event.getActionMasked() == MotionEvent.ACTION_HOVER_EXIT) {
                mMaskDrawable.setStateLocked(DrawableStateProxy.STATE_HOVERED, false, false, true);
            }
        }
        return super.dispatchHoverEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (mConsumeDispatchingEventForState) {
            handlePressAnimationInternal(event);
        }
        return super.dispatchTouchEvent(event);
    }

    public Path getLayoutPath() {
        return null;
    }

    @Deprecated
    public void initAnimation(Context context) {
    }

    @Override
    public boolean isCardType() {
        return false;
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mLayoutRect.set(0.0f, 0.0f, w, h);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mConsumeDispatchingEventForState) {
            handlePressAnimationInternal(event);
        }
        return super.onTouchEvent(event);
    }

    public void refresh() {
        if (mStateEffectBackground != null) {
            mStateEffectBackground.refresh(getContext());
        }
        if (mMaskDrawable != null) {
            mMaskDrawable.refresh(getContext());
        }
    }

    @Override
    public void refreshCardBg(int color) {
    }

    public void resetBackgroundAnimation() {
        if (mStateEffectBackground != null) {
            mStateEffectBackground.reset();
        }
    }

    @Override
    public void setBackground(Drawable background) {
        if (mStateEffectBackground == null) {
            super.setBackground(background);
        } else if (background == null) {
            mStateEffectBackground.setViewBackground(new ColorDrawable(0));
        } else {
            mStateEffectBackground.setViewBackground(background);
        }
    }

    @Deprecated
    public void setBackgroundAnimationDrawable(Drawable drawable) {
    }

    public void setBackgroundAnimationEnabled(boolean enabled) {
        mBackgroundAnimationEnabled = enabled;
        if (mStateEffectBackground != null) {
            mStateEffectBackground.setAnimateEnabled(enabled);
        }
    }

    @Override
    public void setConfigurationChangeListener(ConfigurationChangedListener listener) {
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (!enabled && isEnabled()) {
            startDisAppearAnimationOrNot();
        }
        super.setEnabled(enabled);
    }

    @Override
    public void setPositionInGroup(int position) {
    }

    public void setPressScaleEffectEnable(boolean enabled) {
        if (mStateEffectBackground == null) {
            return;
        }
        if (enabled) {
            mStateEffectBackground.enableScaleEffect(this);
        } else {
            mStateEffectBackground.disableScaleEffect();
        }
    }

    public void startAppearAnimation() {
        startAppearAnimation(true);
    }

    public void startAppearAnimation(boolean animated) {
        if (mStateEffectBackground == null) {
            return;
        }
        if (!animated) {
            mStateEffectBackground.setAnimateEnabled(false);
        }
        mStateEffectBackground.setTouched(true);
        if (!animated) {
            mStateEffectBackground.setAnimateEnabled(true);
        }
    }

    public void startDisAppearAnimationOrNot() {
        startDisAppearAnimationOrNot(true);
    }

    public void startDisAppearAnimationOrNot(boolean animated) {
        if (mStateEffectBackground == null) {
            return;
        }
        if (!animated) {
            mStateEffectBackground.setAnimateEnabled(false);
        }
        mStateEffectBackground.setTouched(false);
        if (!animated) {
            mStateEffectBackground.setAnimateEnabled(true);
        }
    }
}
