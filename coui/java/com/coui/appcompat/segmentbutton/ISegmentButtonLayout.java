package com.coui.appcompat.segmentbutton;

/** ViewPager-style callback contract used by {@link COUISegmentButtonLayout}. */
public interface ISegmentButtonLayout {
    void onPageScrollStateChanged(int state);
    void onPageScrolled(int position, float positionOffset, int positionOffsetPixels);
    void onPageSelected(int position);
}
