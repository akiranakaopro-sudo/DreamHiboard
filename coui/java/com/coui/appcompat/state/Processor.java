package com.coui.appcompat.state;

import android.content.ComponentCallbacks;
import android.content.res.Configuration;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;


public abstract class Processor<T, V extends View> implements ComponentCallbacks {
    private SparseArray<T> mSparseArray;
    private final int mState;
    protected V mView;

    public Processor(int state, SparseArray<T> sparseArray) {
        this(null, state, sparseArray);
    }

    public SparseArray<T> getSparseArray() {
        return this.mSparseArray;
    }

    public int getState() {
        return this.mState;
    }

    public boolean isLoadedView() {
        return this.mView != null;
    }

    @Override
    public void onConfigurationChanged(Configuration configuration) {
    }

    @Override
    public void onLowMemory() {
    }

    public abstract void onProcess(V view, int state, SparseArray<T> sparseArray);

    public void process() {
        process(this.mView);
    }

    public void release() {
        this.mView = null;
        this.mSparseArray.clear();
        this.mSparseArray = null;
    }

    public void setView(V view) {
        if (view == null) {
            throw new IllegalArgumentException("Processor: setView() params cannot be null!");
        }
        this.mView = view;
    }

    public Processor(V view, int state, SparseArray<T> sparseArray) {
        if (sparseArray == null) {
            throw new IllegalArgumentException("Processor: the params cannot be null!");
        }
        this.mView = view;
        this.mState = state;
        this.mSparseArray = sparseArray;
    }

    public void process(V view) {
        if (view != null) {
            onProcess(view, this.mState, this.mSparseArray);
        } else {
            Log.e(getClass().getSimpleName(), "Processor: the parameter mView == null");
        }
    }
}
