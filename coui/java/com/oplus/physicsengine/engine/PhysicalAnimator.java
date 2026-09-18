package com.oplus.physicsengine.engine;

import android.content.Context;
import android.view.Display;
import android.view.WindowManager;

import androidx.collection.ArraySet;

import com.oplus.physicsengine.common.Compat;
import com.oplus.physicsengine.common.Debug;
import com.oplus.physicsengine.common.Vector;
import com.oplus.physicsengine.dynamics.Body;
import com.oplus.physicsengine.dynamics.World;
import com.oplus.physicsengine.dynamics.spring.Spring;
import com.oplus.physicsengine.dynamics.spring.SpringDef;

import java.util.ArrayList;
import java.util.HashMap;

public final class PhysicalAnimator implements ChoreographerCompat.AnimationFrameCallback {
    public final ArraySet<BaseBehavior> mAllBehaviors = new ArraySet<>(1);
    public HashMap<BaseBehavior, AnimationListener> mAnimationListeners;
    public final ChoreographerCompat mChoreographer;
    public final ArraySet<BaseBehavior> mCurrentRunningBehaviors = new ArraySet<>(1);
    public final Body mGround;
    public boolean mIsAnimatorRunning = false;
    public boolean mIsCancel = false;
    public boolean mIsSteady = true;
    public HashMap<BaseBehavior, AnimationUpdateListener> mUpdateListeners;
    public final World mWorld;

    public PhysicalAnimator(Context context) {
        this.mChoreographer = new ChoreographerCompat();
        this.mChoreographer.setFrameCallback(this);
        if (context != null) {
            float density = context.getResources().getDisplayMetrics().density;
            Compat.sPhysicalSizeToPixelsRatio = (density * 55.0f) + 0.5f;
            Compat.sSteadyAccuracy = 0.1f / Compat.sPhysicalSizeToPixelsRatio;
            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            if (windowManager != null) {
                Display display = windowManager.getDefaultDisplay();
                if (display != null && display.getRefreshRate() > 0.0f) {
                    Compat.sRefreshRate = 1.0f / display.getRefreshRate();
                }
            }
        }
        this.mWorld = new World();
        this.mGround = createBody(new Vector(), 0, 5, 0.0f, 0.0f, "Ground");
    }

    public static PhysicalAnimator create(Context context) {
        return new PhysicalAnimator(context);
    }

    public static void updateValue(BaseBehavior behavior) {
        if (behavior == null || behavior.mPropertyMap == null || behavior.mActiveUIItem == null) {
            return;
        }
        for (FloatPropertyHolder propertyHolder : behavior.mPropertyMap.values()) {
            if (propertyHolder instanceof FloatValueHolder) {
                ((FloatValueHolder) propertyHolder).mValue =
                        behavior.mActiveUIItem.mTransform.x * propertyHolder.mValueThreshold;
            }
        }
    }

    public void addAnimationListener(AnimationListener listener, BaseBehavior... behaviors) {
        for (BaseBehavior behavior : behaviors) {
            addAnimationListener(behavior, listener);
        }
    }

    public final void addAnimationListener(BaseBehavior behavior, AnimationListener listener) {
        if (this.mAnimationListeners == null) {
            this.mAnimationListeners = new HashMap<>(1);
        }
        this.mAnimationListeners.put(behavior, listener);
    }

    public void addAnimationUpdateListener(AnimationUpdateListener listener, BaseBehavior... behaviors) {
        for (BaseBehavior behavior : behaviors) {
            addAnimationUpdateListener(behavior, listener);
        }
    }

    public void addAnimationUpdateListener(BaseBehavior behavior, AnimationUpdateListener listener) {
        if (this.mUpdateListeners == null) {
            this.mUpdateListeners = new HashMap<>(1);
        }
        this.mUpdateListeners.put(behavior, listener);
    }

    public void addBehavior(BaseBehavior behavior) {
        behavior.mAnimator = this;
        behavior.verifyBodyProperty();
        behavior.linkGroundToSpring(getGround());
        for (int i = 0; i < this.mAllBehaviors.size(); i++) {
            BaseBehavior oldBehavior = this.mAllBehaviors.valueAt(i);
            if (oldBehavior != null && oldBehavior.mTarget != null && behavior.mTarget != null
                    && oldBehavior.mTarget == behavior.mTarget
                    && oldBehavior.getType() == behavior.getType()
                    && removeBehavior(oldBehavior)) {
                i--;
            }
        }
        this.mAllBehaviors.add(behavior);
    }

    public void cancel(String reason) {
        if (this.mIsCancel) {
            return;
        }
        this.mIsCancel = true;
        for (BaseBehavior behavior : new ArrayList<>(this.mCurrentRunningBehaviors)) {
            AnimationListener listener = this.mAnimationListeners == null ? null : this.mAnimationListeners.get(behavior);
            if (listener != null) {
                listener.onAnimationCancel(behavior);
            }
            behavior.stopBehavior();
        }
        this.mCurrentRunningBehaviors.clear();
        pause();
        this.mIsCancel = false;
    }

    public Body createBody(Vector position, int type, int property, float width, float height, String tag) {
        return this.mWorld.createBody(position, type, property, width, height, tag);
    }

    public Spring createSpring(SpringDef springDef) {
        return this.mWorld.createSpring(springDef);
    }

    public boolean destroyBody(Body body) {
        if (body == null) {
            return false;
        }
        this.mWorld.destroyBody(body);
        return true;
    }

    public Body getGround() {
        return this.mGround;
    }

    public boolean isFrameScheduled() {
        return this.mChoreographer.mFrameScheduled;
    }

    public void pause() {
        if (!this.mIsAnimatorRunning) {
            return;
        }
        if (this.mChoreographer.mFrameScheduled) {
            this.mChoreographer.mChoreographer.removeFrameCallback(this.mChoreographer.mChoreographerFrameCallback);
            this.mChoreographer.mFrameScheduled = false;
        }
        this.mIsAnimatorRunning = false;
    }

    public boolean removeBehavior(BaseBehavior behavior) {
        if (behavior == null) {
            return false;
        }
        boolean removed = this.mAllBehaviors.remove(behavior);
        this.mCurrentRunningBehaviors.remove(behavior);
        if (removed) {
            behavior.onRemove();
        }
        return removed;
    }

    public void setDebugMode(Boolean debug) {
        Debug.setDebugMode(debug.booleanValue());
    }

    void dispatchAnimationUpdate(BaseBehavior behavior) {
        AnimationUpdateListener listener = this.mUpdateListeners == null ? null : this.mUpdateListeners.get(behavior);
        if (listener != null) {
            listener.onAnimationUpdate(behavior);
        }
    }

    void doFrame() {
        if (this.mIsCancel) {
            return;
        }
        this.mWorld.step(Compat.sRefreshRate);
        for (BaseBehavior behavior : new ArrayList<>(this.mCurrentRunningBehaviors)) {
            if (behavior == null) {
                continue;
            }
            behavior.dispatchChanging();
            updateValue(behavior);
            dispatchAnimationUpdate(behavior);
            if (behavior.isSteady()) {
                behavior.stopBehavior();
            }
        }
        this.mIsSteady = this.mCurrentRunningBehaviors.isEmpty();
        if (this.mIsSteady) {
            pause();
        } else {
            this.mChoreographer.scheduleNextFrame();
        }
    }

    void finishBehavior(BaseBehavior behavior, boolean cancelled) {
        this.mCurrentRunningBehaviors.remove(behavior);
        AnimationListener listener = this.mAnimationListeners == null ? null : this.mAnimationListeners.get(behavior);
        if (listener != null) {
            if (cancelled) {
                listener.onAnimationCancel(behavior);
            } else {
                listener.onAnimationEnd(behavior);
            }
        }
    }

    void scheduleNextFrame() {
        if (this.mIsAnimatorRunning) {
            this.mChoreographer.scheduleNextFrame();
        }
    }

    void startBehavior(BaseBehavior behavior) {
        if (!this.mAllBehaviors.contains(behavior)) {
            addBehavior(behavior);
        }
        this.mCurrentRunningBehaviors.add(behavior);
        behavior.mIsStarted = true;
        if (behavior.mPropertyBody != null) {
            behavior.mPropertyBody.setAwake(true);
        }
        AnimationListener listener = this.mAnimationListeners == null ? null : this.mAnimationListeners.get(behavior);
        if (listener != null) {
            listener.onAnimationStart(behavior);
        }
        this.mIsSteady = false;
        this.mIsAnimatorRunning = true;
        this.mChoreographer.scheduleNextFrame();
    }
}
