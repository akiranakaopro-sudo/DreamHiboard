package com.coui.appcompat.state;

import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

public class MarginProcessor extends Processor<Integer, View> {
    public static final int KEY_TOP_MARGIN = 0;
    public static final int KEY_START_MARGIN = 1;
    public static final int KEY_BOTTOM_MARGIN = 2;
    public static final int KEY_END_MARGIN = 3;

    public static class Builder {
        SparseArray<Integer> mSparseArray = new SparseArray<>();
        int mState;
        View mView;

        public Builder(int state) {
            mState = state;
        }

        public MarginProcessor create() {
            return new MarginProcessor(mView, mState, mSparseArray);
        }

        public Builder setMarginBottom(int margin) {
            mSparseArray.put(KEY_BOTTOM_MARGIN, margin);
            return this;
        }

        public Builder setMarginEnd(int margin) {
            mSparseArray.put(KEY_END_MARGIN, margin);
            return this;
        }

        public Builder setMarginStart(int margin) {
            mSparseArray.put(KEY_START_MARGIN, margin);
            return this;
        }

        public Builder setMarginTop(int margin) {
            mSparseArray.put(KEY_TOP_MARGIN, margin);
            return this;
        }

        public Builder with(View view) {
            mView = view;
            return this;
        }
    }

    @Override
    public void onProcess(View view, int state, SparseArray<Integer> sparseArray) {
        ViewGroup.MarginLayoutParams layoutParams =
                (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        if (sparseArray.get(KEY_START_MARGIN) != null) {
            layoutParams.setMarginStart(sparseArray.get(KEY_START_MARGIN));
        }
        if (sparseArray.get(KEY_END_MARGIN) != null) {
            layoutParams.setMarginEnd(sparseArray.get(KEY_END_MARGIN));
        }
        if (sparseArray.get(KEY_TOP_MARGIN) != null) {
            layoutParams.topMargin = sparseArray.get(KEY_TOP_MARGIN);
        }
        if (sparseArray.get(KEY_BOTTOM_MARGIN) != null) {
            layoutParams.bottomMargin = sparseArray.get(KEY_BOTTOM_MARGIN);
        }
        view.setLayoutParams(layoutParams);
    }

    @Override
    public void process(View view) {
        if (view == null || !(view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams)) {
            throw new IllegalArgumentException("mView == null, or layoutParams is wrong");
        }
        super.process(view);
    }

    private MarginProcessor(View view, int state, SparseArray<Integer> sparseArray) {
        super(view, state, sparseArray);
    }
}
