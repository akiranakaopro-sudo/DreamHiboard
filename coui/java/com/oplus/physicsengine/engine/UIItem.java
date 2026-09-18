package com.oplus.physicsengine.engine;

import androidx.annotation.NonNull;

import com.oplus.physicsengine.common.Vector;

public final class UIItem {
    public float mHeight;
    public final Vector mMoveTarget = new Vector();
    public final Vector mStartPosition = new Vector();
    public final Vector mStartScale = new Vector();
    public final Vector mStartVelocity = new Vector();
    public final Object mTarget;
    public final Transform mTransform = new Transform();
    public float mWidth;

    public UIItem(Object target) {
        this.mTarget = target;
        this.mStartScale.set(1.0f);
        this.mTransform.scaleX = 1.0f;
        this.mTransform.scaleY = 1.0f;
    }

    public Transform getTransform() {
        return this.mTransform;
    }

    public void setTransformScale(float scale) {
        this.mTransform.scaleX = scale;
        this.mTransform.scaleY = scale;
    }

    @NonNull
    public String toString() {
        return "UIItem{mTarget=" + this.mTarget + ", mTransform=" + this.mTransform
                + ", mWidth=" + this.mWidth + ", mHeight=" + this.mHeight + "}@" + hashCode();
    }
}
