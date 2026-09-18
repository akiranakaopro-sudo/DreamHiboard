package com.coui.appcompat.poplist;

import com.coui.appcompat.R;

import android.animation.Animator;
import android.view.View;
import com.coui.appcompat.animation.dynamicanimation.COUISpringAnimation;


interface AnimationExecutor {
    boolean isAsynchronous();

    void runOnMainThread(View view, Runnable runnable);

    void setAlpha(View view, float alpha);

    void setScaleX(View view, float scaleX);

    void setScaleY(View view, float scaleY);

    Animator startAnimation(COUISpringAnimation cOUISpringAnimation, View view, float forward);
}
