package com.coui.appcompat.viewpager;

import android.view.View;

import androidx.viewpager2.widget.ViewPager2;

public class COUIMarginPageTransformer implements ViewPager2.PageTransformer {
    private final int mMarginPx;

    public COUIMarginPageTransformer(int marginPx) {
        if (marginPx < 0) {
            throw new IllegalArgumentException("Margin must be non-negative");
        }
        mMarginPx = marginPx;
    }

    @Override
    public void transformPage(View page, float position) {
        COUIViewPager2 viewPager = COUIViewPager2.findOwner(page);
        float offset = mMarginPx * position;
        if (viewPager.getOrientation() != ViewPager2.ORIENTATION_HORIZONTAL) {
            page.setTranslationY(offset);
            return;
        }
        if (viewPager.isRtl()) {
            offset = -offset;
        }
        page.setTranslationX(offset);
    }
}
