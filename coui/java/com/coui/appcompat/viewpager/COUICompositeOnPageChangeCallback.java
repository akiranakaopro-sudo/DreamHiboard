package com.coui.appcompat.viewpager;

import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;

public class COUICompositeOnPageChangeCallback extends ViewPager2.OnPageChangeCallback {
    private final List<ViewPager2.OnPageChangeCallback> mCallbacks;

    public COUICompositeOnPageChangeCallback(int initialCapacity) {
        mCallbacks = new ArrayList<>(initialCapacity);
    }

    private void throwCallbackListModifiedWhileInUse(ConcurrentModificationException exception) {
        throw new IllegalStateException(
                "Adding and removing callbacks during dispatch to callbacks is not supported",
                exception);
    }

    public void addOnPageChangeCallback(ViewPager2.OnPageChangeCallback callback) {
        mCallbacks.add(callback);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        try {
            Iterator<ViewPager2.OnPageChangeCallback> iterator = mCallbacks.iterator();
            while (iterator.hasNext()) {
                iterator.next().onPageScrollStateChanged(state);
            }
        } catch (ConcurrentModificationException e) {
            throwCallbackListModifiedWhileInUse(e);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        try {
            Iterator<ViewPager2.OnPageChangeCallback> iterator = mCallbacks.iterator();
            while (iterator.hasNext()) {
                iterator.next().onPageScrolled(position, positionOffset, positionOffsetPixels);
            }
        } catch (ConcurrentModificationException e) {
            throwCallbackListModifiedWhileInUse(e);
        }
    }

    @Override
    public void onPageSelected(int position) {
        try {
            Iterator<ViewPager2.OnPageChangeCallback> iterator = mCallbacks.iterator();
            while (iterator.hasNext()) {
                iterator.next().onPageSelected(position);
            }
        } catch (ConcurrentModificationException e) {
            throwCallbackListModifiedWhileInUse(e);
        }
    }

    public void removeOnPageChangeCallback(ViewPager2.OnPageChangeCallback callback) {
        mCallbacks.remove(callback);
    }
}
