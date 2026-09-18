package com.coui.appcompat.springchain.api;

import android.view.View;


public interface IChainItem {
    int getItemHeight();

    int getItemWidth();

    int getItemX();

    int getItemY();

    boolean getSkipSpringChainCalc();

    void setItemHeight(int height);

    void setItemSize(int width, int height);

    void setItemWidth(int width);

    void setItemX(int x);

    void setItemXY(int x, int y);

    void setItemY(int y);

    void setProxyView(View view);

    void setSkipSpringChainCalc(boolean skip);

    void updateSpringX(float x);

    void updateSpringY(float y);
}
