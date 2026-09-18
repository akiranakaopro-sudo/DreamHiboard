package com.oplus.physicsengine.common;

public final class Mat22 {
    public final Vector ex = new Vector();
    public final Vector ey = new Vector();

    public Mat22 invertLocal() {
        float a = this.ex.mX;
        float b = this.ey.mX;
        float c = this.ex.mY;
        float d = this.ey.mY;
        float det = (a * d) - (b * c);
        if (det != 0.0f) {
            det = 1.0f / det;
        }
        this.ex.mX = d * det;
        this.ey.mX = -b * det;
        this.ex.mY = -c * det;
        this.ey.mY = a * det;
        return this;
    }

    public static void mulToOut(Mat22 matrix, Vector vector, Vector out) {
        float x = vector.mX;
        float y = vector.mY;
        out.mX = (matrix.ex.mX * x) + (matrix.ey.mX * y);
        out.mY = (matrix.ex.mY * x) + (matrix.ey.mY * y);
    }
}
