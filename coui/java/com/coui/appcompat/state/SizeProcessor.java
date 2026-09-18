package com.coui.appcompat.state;

import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;


public class SizeProcessor extends Processor<Object, View> {
    public static final int KEY_HEIGHT = 1;
    public static final int KEY_WEIGHT = 2;
    public static final int KEY_WIDTH = 0;

    public static class Builder {
        SparseArray<Object> mSparseArray = new SparseArray<>();
        int mState;
        View mView;

        public Builder(int state) {
            this.mState = state;
        }

        public SizeProcessor create() {
            return new SizeProcessor(this.mView, this.mState, this.mSparseArray);
        }

        public Builder setHeight(int height) {
            this.mSparseArray.put(KEY_HEIGHT, Integer.valueOf(height));
            return this;
        }

        public Builder setWeight(float weight) {
            this.mSparseArray.put(KEY_WEIGHT, Float.valueOf(weight));
            return this;
        }

        public Builder setWidth(int width) {
            this.mSparseArray.put(KEY_WIDTH, Integer.valueOf(width));
            return this;
        }

        public Builder with(View view) {
            this.mView = view;
            return this;
        }
    }

    @Override
    public void onProcess(View view, int state, SparseArray<Object> sparseArray) {
        if (view == null || view.getLayoutParams() == null) {
            return;
        }
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (sparseArray.get(KEY_WIDTH) != null) {
            layoutParams.width = ((Integer) sparseArray.get(KEY_WIDTH)).intValue();
        }
        if (sparseArray.get(KEY_HEIGHT) != null) {
            layoutParams.height = ((Integer) sparseArray.get(KEY_HEIGHT)).intValue();
        }
        if (sparseArray.get(KEY_WEIGHT) != null && (layoutParams instanceof LinearLayout.LayoutParams)) {
            ((LinearLayout.LayoutParams) layoutParams).weight = ((Float) sparseArray.get(KEY_WEIGHT)).floatValue();
        }
        view.setLayoutParams(layoutParams);
    }

    private SizeProcessor(View view, int state, SparseArray<Object> sparseArray) {
        super(view, state, sparseArray);
    }
}
