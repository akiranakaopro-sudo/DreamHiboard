package com.oplus.physicsengine.engine;

import android.util.Log;
import android.view.Choreographer;

import com.oplus.physicsengine.common.Debug;

public final class ChoreographerCompat {
    public Choreographer mChoreographer;
    public Choreographer.FrameCallback mChoreographerFrameCallback;
    public AnimationFrameCallback mFrameCallback;
    public boolean mFrameScheduled;

    public ChoreographerCompat() {
        this.mChoreographerFrameCallback = frameTimeNanos -> {
            this.mFrameScheduled = false;
            if (this.mFrameCallback != null) {
                if (Debug.sDebugFrame) {
                    Log.d("PhysicsWorld-Frame", "doFrame ----------------------- frameTime =:" + frameTimeNanos);
                }
                if (this.mFrameCallback instanceof PhysicalAnimator) {
                    ((PhysicalAnimator) this.mFrameCallback).doFrame();
                }
            }
        };
        this.mFrameScheduled = false;
        this.mChoreographer = Choreographer.getInstance();
    }

    public void scheduleNextFrame() {
        if (!this.mFrameScheduled && this.mFrameCallback != null) {
            this.mChoreographer.postFrameCallback(this.mChoreographerFrameCallback);
            if (Debug.sDebugFrame) {
                Log.d("PhysicsWorld-Frame", "scheduleNextFrame ----------------------- ");
            }
            this.mFrameScheduled = true;
        }
    }

    public void setFrameCallback(AnimationFrameCallback frameCallback) {
        this.mFrameCallback = frameCallback;
    }

    public interface AnimationFrameCallback {
    }
}
