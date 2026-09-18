package com.coui.appcompat.indicator;


public interface COUIIPagerIndicator {
    void onPageScrollStateChanged(int state);

    void onPageScrolled(int position, float positionOffset, int positionOffsetPixels);

    void onPageSelected(int position);
}
