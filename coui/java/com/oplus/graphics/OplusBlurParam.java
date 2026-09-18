package com.oplus.graphics;

import android.util.Log;

import java.util.Arrays;
import java.util.Objects;

public class OplusBlurParam {
    public static final int BLUR_BLEND_MODE_COLORDODGE = 3;
    public static final int BLUR_BLEND_MODE_COLORMIX = 1;
    public static final int BLUR_BLEND_MODE_DEFAULT = 0;
    public static final int BLUR_BLEND_MODE_GLOW_COLORDODGE = 5;
    public static final int BLUR_BLEND_MODE_GLOW_OVERLAY = 4;
    public static final int BLUR_BLEND_MODE_OVERLAY = 2;
    public static final int BLUR_TILE_MODE_CLAMP = 3;
    public static final int BLUR_TILE_MODE_DECAL = 4;
    public static final int BLUR_TILE_MODE_DEFAULT = 0;
    public static final int BLUR_TILE_MODE_MIRROR = 2;
    public static final int BLUR_TILE_MODE_REPEAT = 1;
    public static final int BLUR_TYPE_DEFAULT = 0;
    public static final int BLUR_TYPE_FAST_KAWASE = 2;
    public static final int BLUR_TYPE_GAUSSIAN = 4;
    public static final int BLUR_TYPE_ORIGINAL = 1;
    public static final int BLUR_TYPE_QUALITY_KAWASE = 3;
    public static final float DEFAULT_SMOOTH_CORNER_WEIGHT = 2.0f;
    private static final String TAG = "OplusBlurParam";
    private static final int PARAMS_SIZE = 17;
    private int mBlurType = BLUR_TYPE_DEFAULT;
    private int mTileMode = BLUR_TILE_MODE_DEFAULT;
    private float mZoomFactor = 1.0f;
    private int mBlendMode = BLUR_BLEND_MODE_DEFAULT;
    private final float[] mBlendColor = {0.0f, 0.0f, 0.0f, 0.0f};
    private final float[] mMixColor = {0.0f, 0.0f, 0.0f, 0.0f};
    private final float[] mBlurColor = {0.0f, 0.0f, 0.0f, 0.0f};
    private float mSmoothCornerWeight = DEFAULT_SMOOTH_CORNER_WEIGHT;

    public void setBlurType(int type) {
        mBlurType = type;
    }

    public void setMirrorParams(int mode, float factor) {
        mTileMode = mode;
        mZoomFactor = factor;
    }

    public void setMaterialParams(int mode, float[] blendColor, float[] mixColor) {
        mBlendMode = mode;
        if (blendColor != null && blendColor.length >= 4) {
            System.arraycopy(blendColor, 0, mBlendColor, 0, 4);
        }
        if (mixColor != null && mixColor.length >= 4) {
            System.arraycopy(mixColor, 0, mMixColor, 0, 4);
        }
    }

    public void setArcylicParams(float[] arcylicColor) {
        if (arcylicColor != null && arcylicColor.length >= 4) {
            System.arraycopy(arcylicColor, 0, mBlurColor, 0, 4);
        }
    }

    public void setSmoothCornerWeight(float weight) {
        if (weight <= 0.0f) {
            Log.e(TAG, "IllegalArgument for setSmoothCornerWeight " + weight);
            return;
        }
        mSmoothCornerWeight = weight;
    }

    public float getSmoothCornerWeight() {
        return mSmoothCornerWeight;
    }

    public int getParamsSize() {
        return PARAMS_SIZE;
    }

    public float[] toFloatArray() {
        return new float[]{mBlurType, mTileMode, mZoomFactor, mBlendMode,
                mBlendColor[0], mBlendColor[1], mBlendColor[2], mBlendColor[3],
                mMixColor[0], mMixColor[1], mMixColor[2], mMixColor[3],
                mBlurColor[0], mBlurColor[1], mBlurColor[2], mBlurColor[3],
                mSmoothCornerWeight};
    }

    public void fromFloatArray(float[] values) {
        if (values == null || values.length != PARAMS_SIZE) {
            Log.w(TAG, "float array size is not equal params num in fromFloatArray");
            return;
        }
        mBlurType = Math.round(values[0]);
        mTileMode = Math.round(values[1]);
        mZoomFactor = values[2];
        mBlendMode = Math.round(values[3]);
        System.arraycopy(values, 4, mBlendColor, 0, 4);
        System.arraycopy(values, 8, mMixColor, 0, 4);
        System.arraycopy(values, 12, mBlurColor, 0, 4);
        mSmoothCornerWeight = values[16];
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        return obj instanceof OplusBlurParam
                && Arrays.equals(toFloatArray(), ((OplusBlurParam) obj).toFloatArray());
    }

    @Override
    public int hashCode() {
        return Objects.hash(mBlurType, mTileMode, mZoomFactor, mBlendMode,
                Arrays.hashCode(mBlendColor), Arrays.hashCode(mMixColor), Arrays.hashCode(mBlurColor),
                mSmoothCornerWeight);
    }
}
