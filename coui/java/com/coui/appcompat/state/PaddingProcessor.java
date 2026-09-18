package com.coui.appcompat.state;

import android.util.SparseArray;
import android.view.View;
import androidx.core.view.ViewCompat;


public class PaddingProcessor extends Processor<Integer, View> {
    public static final int KEY_BOTTOM_PADDING = 2;
    public static final int KEY_END_PADDING = 3;
    public static final int KEY_START_PADDING = 1;
    public static final int KEY_TOP_PADDING = 0;

    public static class Builder {
        SparseArray<Integer> mSparseArray = new SparseArray<>();
        int mState;
        View mView;

        public Builder(int state) {
            this.mState = state;
        }

        public PaddingProcessor create() {
            return new PaddingProcessor(this.mView, this.mState, this.mSparseArray);
        }

        public Builder setPaddingBottom(int paddingBottom) {
            this.mSparseArray.put(KEY_BOTTOM_PADDING, Integer.valueOf(paddingBottom));
            return this;
        }

        public Builder setPaddingEnd(int paddingEnd) {
            this.mSparseArray.put(KEY_END_PADDING, Integer.valueOf(paddingEnd));
            return this;
        }

        public Builder setPaddingStart(int paddingStart) {
            this.mSparseArray.put(KEY_START_PADDING, Integer.valueOf(paddingStart));
            return this;
        }

        public Builder setPaddingTop(int paddingTop) {
            this.mSparseArray.put(KEY_TOP_PADDING, Integer.valueOf(paddingTop));
            return this;
        }

        public Builder with(View view) {
            this.mView = view;
            return this;
        }
    }

    @Override
    public void onProcess(View view, int state, SparseArray<Integer> sparseArray) {
        ViewCompat.setPaddingRelative(view, sparseArray.get(KEY_START_PADDING) != null ? sparseArray.get(KEY_START_PADDING).intValue() : view.getPaddingStart(), sparseArray.get(KEY_TOP_PADDING) != null ? sparseArray.get(KEY_TOP_PADDING).intValue() : view.getPaddingTop(), sparseArray.get(KEY_END_PADDING) != null ? sparseArray.get(KEY_END_PADDING).intValue() : view.getPaddingEnd(), sparseArray.get(KEY_BOTTOM_PADDING) != null ? sparseArray.get(KEY_BOTTOM_PADDING).intValue() : view.getPaddingBottom());
    }

    private PaddingProcessor(View view, int state, SparseArray<Integer> sparseArray) {
        super(view, state, sparseArray);
    }
}
