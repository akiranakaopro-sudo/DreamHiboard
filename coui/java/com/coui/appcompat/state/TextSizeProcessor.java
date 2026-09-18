package com.coui.appcompat.state;

import android.util.SparseArray;
import android.widget.TextView;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


public class TextSizeProcessor extends Processor<Float, TextView> {
    public static final int KEY_SIZE_UNIT = 2;
    public static final int KEY_TEXT_SIZE = 1;

    public static class Builder {
        SparseArray<Float> mSparseArray = new SparseArray<>();
        int mState;
        TextView mView;

        public Builder(int state) {
            this.mState = state;
        }

        public TextSizeProcessor create() {
            return new TextSizeProcessor(this.mView, this.mState, this.mSparseArray);
        }

        public Builder setSizeType(float sizeType) {
            this.mSparseArray.put(KEY_SIZE_UNIT, Float.valueOf(sizeType));
            return this;
        }

        public Builder setTextSize(float textSize) {
            this.mSparseArray.put(KEY_TEXT_SIZE, Float.valueOf(textSize));
            return this;
        }

        public Builder with(TextView textView) {
            this.mView = textView;
            return this;
        }
    }

    @Target({ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TypedValue {
        public static final float DP = 1.0f;
        public static final float PX = 2.0f;
        public static final float SP = 0.0f;
    }

    private int getTypedValueByKeySizeUnit(float sizeUnit) {
        if (sizeUnit == TypedValue.DP) {
            return 1;
        }
        return sizeUnit == TypedValue.PX ? 0 : 2;
    }

    private TextSizeProcessor(TextView textView, int state, SparseArray<Float> sparseArray) {
        super(textView, state, sparseArray);
    }

    @Override
    public void onProcess(TextView textView, int state, SparseArray<Float> sparseArray) {
        int typedValueByKeySizeUnit = sparseArray.get(KEY_SIZE_UNIT) != null ? getTypedValueByKeySizeUnit(sparseArray.get(KEY_SIZE_UNIT).floatValue()) : 2;
        if (sparseArray.get(KEY_TEXT_SIZE) != null) {
            textView.setTextSize(typedValueByKeySizeUnit, sparseArray.get(KEY_TEXT_SIZE).floatValue());
        }
    }
}
