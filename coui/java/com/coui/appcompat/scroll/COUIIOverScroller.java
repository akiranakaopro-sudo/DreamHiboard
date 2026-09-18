package com.coui.appcompat.scroll;

import android.view.animation.Interpolator;

public interface COUIIOverScroller {
    void abortAnimation();

    boolean computeScrollOffset();

    void fling(int startX, int startY, int velocityX, int velocityY);

    void fling(int startX, int startY, int velocityX, int velocityY,
            int minX, int maxX, int minY, int maxY);

    void fling(int startX, int startY, int velocityX, int velocityY,
            int minX, int maxX, int minY, int maxY, int overX, int overY);

    int getCOUICurrX();

    int getCOUICurrY();

    int getCOUIFinalX();

    int getCOUIFinalY();

    float getCurrVelocity();

    float getCurrVelocityX();

    float getCurrVelocityY();

    boolean isCOUIFinished();

    boolean isScrollingInDirection(float xvel, float yvel);

    void notifyHorizontalEdgeReached(int startX, int finalX, int overX);

    void notifyVerticalEdgeReached(int startY, int finalY, int overY);

    void setCOUIFriction(float friction);

    void setCurrVelocityX(float velocityX);

    void setCurrVelocityY(float velocityY);

    default void setDurationRatio(float ratio) {
    }

    void setFinalX(int newX);

    void setFinalY(int newY);

    void setFlingFriction(float friction);

    void setInterpolator(Interpolator interpolator);

    void setIsScrollView(boolean isScrollView);

    default void setVelocityXRatio(float ratio) {
    }

    default void setVelocityYRatio(float ratio) {
    }

    boolean springBack(int startX, int startY, int minX, int maxX, int minY, int maxY);

    void startScroll(int startX, int startY, int dx, int dy);

    void startScroll(int startX, int startY, int dx, int dy, int duration);
}
