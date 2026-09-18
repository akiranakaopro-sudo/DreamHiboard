package com.coui.appcompat.springchain;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import com.coui.appcompat.springchain.api.IChainItem;

import java.util.Objects;


public class COUIGridSpringChainItem extends FrameLayout implements IChainItem {
    private int itemHeight;
    private int itemWidth;
    private int itemX;
    private int itemY;
    private View proxyView;
    private boolean skipSpringChainCalc;

    public COUIGridSpringChainItem(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        Objects.requireNonNull(context, "context");
        itemWidth = 1;
        itemHeight = 1;
        proxyView = this;
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
        setTranslationX(translationX);
    }

    @Override
    public void updateSpringY(float translationY) {
        setTranslationY(translationY);
    }

    public COUIGridSpringChainItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public COUIGridSpringChainItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public COUIGridSpringChainItem(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }
}
