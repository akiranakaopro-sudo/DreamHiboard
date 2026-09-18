package com.coui.appcompat.state;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.view.View;

import com.coui.appcompat.log.COUILog;
import com.coui.appcompat.pressfeedback.COUIPressFeedbackHelper;

public class COUIStateEffectDrawable extends LayerDrawable implements IStateEffect {
    private static final String TAG = "StateEffectDrawable";
    private boolean mEnabled = true;
    private boolean mEnableScaleEffect;
    private COUIPressFeedbackHelper mScaleHelper;

    public COUIStateEffectDrawable(Drawable[] layers) {
        super(layers);
    }

    public void disableScaleEffect() {
        mEnableScaleEffect = false;
        if (mScaleHelper != null) {
            mScaleHelper.setTargetView(null);
        }
    }

    public void enableScaleEffect(View view) {
        enableScaleEffect(view, 0);
    }

    public void enableScaleEffect(View view, int viewType) {
        mEnableScaleEffect = true;
        if (mScaleHelper == null) {
            mScaleHelper = new COUIPressFeedbackHelper(view, viewType);
        } else {
            mScaleHelper.setTargetView(view);
            mScaleHelper.setViewType(viewType);
        }
    }

    @Override
    public int getOpacity() {
        return android.graphics.PixelFormat.TRANSLUCENT;
    }

    @Override
    public boolean isStateful() {
        return true;
    }

    @Override
    protected boolean onStateChange(int[] stateSet) {
        boolean enabled = false;
        for (int state : stateSet) {
            if (state == android.R.attr.state_enabled) {
                enabled = true;
                break;
            }
        }
        if (enabled != mEnabled) {
            mEnabled = enabled;
            if (!enabled) {
                if (mScaleHelper != null) {
                    mScaleHelper.executeFeedbackAnimator(false);
                }
                reset();
            }
        }
        return super.onStateChange(stateSet);
    }

    public void setTouched(boolean touched) {
        for (int i = 0; i < getNumberOfLayers(); i++) {
            Drawable drawable = getDrawable(i);
            // Leapy modified: OPPO dispatches state through DrawableStateProxy.
            if (drawable instanceof DrawableStateProxy) {
                if (touched) {
                    ((DrawableStateProxy) drawable).setTouchEntered();
                } else {
                    ((DrawableStateProxy) drawable).setTouchExited();
                }
            }
        }
        if (mEnabled && mEnableScaleEffect && mScaleHelper != null) {
            mScaleHelper.executeFeedbackAnimator(touched);
        }
    }

    public void setHovered(boolean hovered) {
        for (int i = 0; i < getNumberOfLayers(); i++) {
            Drawable drawable = getDrawable(i);
            if (drawable instanceof DrawableStateProxy) {
                if (hovered) {
                    ((DrawableStateProxy) drawable).setHoverEntered();
                } else {
                    ((DrawableStateProxy) drawable).setHoverExited();
                }
            }
        }
    }

    public void setStateLocked(int state, boolean locked, boolean entered, boolean animated) {
        for (int i = 0; i < getNumberOfLayers(); i++) {
            Drawable drawable = getDrawable(i);
            if (drawable instanceof DrawableStateProxy) {
                ((DrawableStateProxy) drawable).setStateLocked(state, locked, entered, animated);
            }
        }
    }

    public void setFocused(boolean focused) {
        for (int i = 0; i < getNumberOfLayers(); i++) {
            Drawable drawable = getDrawable(i);
            if (drawable instanceof DrawableStateProxy) {
                if (focused) {
                    ((DrawableStateProxy) drawable).setFocusEntered();
                } else {
                    ((DrawableStateProxy) drawable).setFocusExited();
                }
            }
        }
    }

    // Leapy added: Match OPPO's separate selected-touch state dispatch.
    public void setTouchSelected(boolean selected) {
        for (int i = 0; i < getNumberOfLayers(); i++) {
            Drawable drawable = getDrawable(i);
            if (drawable instanceof DrawableStateProxy) {
                if (selected) {
                    ((DrawableStateProxy) drawable).setTouchSelectEntered();
                } else {
                    ((DrawableStateProxy) drawable).setTouchSelectExited();
                }
            }
        }
    }

    public void setViewBackground(Drawable drawable) {
        if (drawable == this) {
            COUILog.e(TAG, "Set view background failed! Should not set LayerDrawable itself as its child recursively!");
        } else {
            // Leapy modified: Preserve OPPO's layer-id replacement behavior.
            setDrawableByLayerId(getId(0), drawable);
        }
    }

    @Override
    public void refresh(Context context) {
        for (int i = 0; i < getNumberOfLayers(); i++) {
            Drawable drawable = getDrawable(i);
            if (drawable instanceof IStateEffect) {
                ((IStateEffect) drawable).refresh(context);
            }
        }
    }

    @Override
    public void reset() {
        for (int i = 0; i < getNumberOfLayers(); i++) {
            Drawable drawable = getDrawable(i);
            if (drawable instanceof IStateEffect) {
                ((IStateEffect) drawable).reset();
            }
        }
    }

    @Override
    public void setAnimateEnabled(boolean enabled) {
        for (int i = 0; i < getNumberOfLayers(); i++) {
            Drawable drawable = getDrawable(i);
            if (drawable instanceof IStateEffect) {
                ((IStateEffect) drawable).setAnimateEnabled(enabled);
            }
        }
    }
}
