package com.oplus.physicsengine.common;

import androidx.annotation.NonNull;

public final class Vector {
    public float mX;
    public float mY;

    public Vector() {
        this(0.0f, 0.0f);
    }

    public Vector(float x, float y) {
        mX = x;
        mY = y;
    }

    public void addLocal(Vector vector) {
        mX += vector.mX;
        mY += vector.mY;
    }

    public Vector add(Vector vector) {
        addLocal(vector);
        return this;
    }

    public void subLocal(Vector vector) {
        mX -= vector.mX;
        mY -= vector.mY;
    }

    public Vector sub(Vector vector) {
        subLocal(vector);
        return this;
    }

    public void mulLocal(float scale) {
        mX *= scale;
        mY *= scale;
    }

    public Vector mul(float scale) {
        mulLocal(scale);
        return this;
    }

    public Vector negate() {
        mX = -mX;
        mY = -mY;
        return this;
    }

    public Vector set(float value) {
        mX = value;
        mY = value;
        return this;
    }

    public Vector set(Vector vector) {
        mX = vector.mX;
        mY = vector.mY;
        return this;
    }

    public void setZero() {
        mX = 0.0f;
        mY = 0.0f;
    }

    @NonNull
    public String toString() {
        return "(" + mX + "," + mY + ")";
    }
}
