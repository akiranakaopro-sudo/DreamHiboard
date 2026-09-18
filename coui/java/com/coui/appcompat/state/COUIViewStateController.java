package com.coui.appcompat.state;

import android.content.ComponentCallbacks;
import android.content.res.Configuration;
import android.util.SparseArray;
import android.view.View;
import java.util.LinkedList;
import java.util.List;


public class COUIViewStateController implements IViewStateController, ComponentCallbacks {
    private SparseArray<List<Processor>> mSparseArray = new SparseArray<>();

    public void addViewStateProcessor(Processor... processorArr) {
        if (processorArr == null) {
            return;
        }
        for (Processor processor : processorArr) {
            if (this.mSparseArray.get(processor.getState()) == null) {
                this.mSparseArray.put(processor.getState(), new LinkedList());
            }
            this.mSparseArray.get(processor.getState()).add(processor);
        }
    }

    public View getProcessView() {
        return null;
    }

    public SparseArray<List<Processor>> getProcessorMap() {
        return this.mSparseArray;
    }

    public void onConfigurationChanged(Configuration configuration) {
        for (int index = 0; index < this.mSparseArray.size(); index++) {
            for (Processor processor : this.mSparseArray.valueAt(index)) {
                if (processor != null) {
                    processor.onConfigurationChanged(configuration);
                }
            }
        }
    }

    @Override
    public void onLowMemory() {
    }

    @Override
    public void onViewStateChanged(int state) {
        List<Processor> processors = this.mSparseArray.get(state);
        if (processors == null) {
            return;
        }
        for (Processor processor : processors) {
            if (processor.isLoadedView()) {
                processor.process();
            } else {
                processor.process(getProcessView());
            }
        }
    }

    @Override
    public void release() {
        for (int index = 0; index < this.mSparseArray.size(); index++) {
            for (Processor processor : this.mSparseArray.valueAt(index)) {
                if (processor != null) {
                    processor.release();
                }
            }
        }
        this.mSparseArray.clear();
    }

    public void addViewStateProcessor(List<Processor> list) {
        if (list == null) {
            return;
        }
        addViewStateProcessor((Processor[]) list.toArray(new Processor[0]));
    }
}
