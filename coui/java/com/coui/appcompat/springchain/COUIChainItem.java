package com.coui.appcompat.springchain;

import android.view.View;
import com.coui.appcompat.springchain.api.IChainItem;

import java.util.Objects;


public final class COUIChainItem implements IChainItem {
    private int itemHeight;
    private int itemWidth;
    private int itemX;
    private int itemY;
    private View proxyView;
    private boolean skipSpringChainCalc;

    public COUIChainItem(View proxyView) {
        this(proxyView, 0, 0, 1, 1, false);
    }

    public COUIChainItem(View proxyView, int itemX, int itemY) {
        this(proxyView, itemX, itemY, 1, 1, false);
    }

    public COUIChainItem(View proxyView, int itemX, int itemY, int itemWidth, int itemHeight) {
        this(proxyView, itemX, itemY, itemWidth, itemHeight, false);
    }

    public COUIChainItem(View proxyView, int itemX, int itemY, int itemWidth, int itemHeight,
            boolean skipSpringChainCalc) {
        this.proxyView = proxyView;
        this.itemX = itemX;
        this.itemY = itemY;
        this.itemWidth = itemWidth;
        this.itemHeight = itemHeight;
        this.skipSpringChainCalc = skipSpringChainCalc;
    }

    @Override
    public int getItemHeight() {
        return this.itemHeight;
    }

    @Override
    public int getItemWidth() {
        return this.itemWidth;
    }

    @Override
    public int getItemX() {
        return this.itemX;
    }

    @Override
    public int getItemY() {
        return this.itemY;
    }

    @Override
    public boolean getSkipSpringChainCalc() {
        return this.skipSpringChainCalc;
    }

    @Override
    public void setItemHeight(int itemHeight) {
        this.itemHeight = itemHeight;
    }

    @Override
    public void setItemSize(int itemWidth, int itemHeight) {
        setItemWidth(itemWidth);
        setItemHeight(itemHeight);
    }

    @Override
    public void setItemWidth(int itemWidth) {
        this.itemWidth = itemWidth;
    }

    @Override
    public void setItemX(int itemX) {
        this.itemX = itemX;
    }

    @Override
    public void setItemXY(int itemX, int itemY) {
        setItemX(itemX);
        setItemY(itemY);
    }

    @Override
    public void setItemY(int itemY) {
        this.itemY = itemY;
    }

    @Override
    public void setProxyView(View proxyView) {
        this.proxyView = Objects.requireNonNull(proxyView, "proxyView");
    }

    @Override
    public void setSkipSpringChainCalc(boolean skipSpringChainCalc) {
        this.skipSpringChainCalc = skipSpringChainCalc;
    }

    @Override
    public void updateSpringX(float translationX) {
        View view = this.proxyView;
        if (view == null) {
            return;
        }
        view.setTranslationX(translationX);
    }

    @Override
    public void updateSpringY(float translationY) {
        View view = this.proxyView;
        if (view == null) {
            return;
        }
        view.setTranslationY(translationY);
    }
}
