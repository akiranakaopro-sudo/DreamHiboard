package com.coui.appcompat.viewpager;

import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.ViewPager2;

import java.util.Locale;

public class COUIPageTransformerAdapter extends ViewPager2.OnPageChangeCallback {
    private final LinearLayoutManager mLayoutManager;
    private ViewPager2.PageTransformer mPageTransformer;

    public COUIPageTransformerAdapter(LinearLayoutManager layoutManager) {
        mLayoutManager = layoutManager;
    }

    public ViewPager2.PageTransformer getPageTransformer() {
        return mPageTransformer;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (mPageTransformer == null) {
            return;
        }
        float transformOffset = -positionOffset;
        for (int i = 0; i < mLayoutManager.getChildCount(); i++) {
            View child = mLayoutManager.getChildAt(i);
            if (child == null) {
                throw new IllegalStateException(String.format(Locale.US,
                        "LayoutManager returned a null child at pos %d/%d while transforming pages",
                        i, mLayoutManager.getChildCount()));
            }
            mPageTransformer.transformPage(child, (mLayoutManager.getPosition(child) - position) + transformOffset);
        }
    }

    public void setPageTransformer(ViewPager2.PageTransformer transformer) {
        mPageTransformer = transformer;
    }
}
