package com.oplus.physicsengine.engine;

import androidx.annotation.NonNull;

public final class Transform {
    public float scaleX;
    public float scaleY;
    public float x;
    public float y;

    @NonNull
    public String toString() {
        return "Transform{x=" + this.x + ", y=" + this.y + ", scaleX=" + this.scaleX
                + ", scaleY=" + this.scaleY + '}';
    }
}
