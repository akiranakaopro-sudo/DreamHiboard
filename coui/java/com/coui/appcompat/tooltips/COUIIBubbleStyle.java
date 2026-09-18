package com.coui.appcompat.tooltips;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public interface COUIIBubbleStyle {
    void dismissWindow();

    TextView getContentView();

    int[] getDefStyleParams();

    default ImageView getDismissIv() {
        return null;
    }

    int getLayoutId();

    int getMaxWidth();

    int getRealWidth(int width, ViewGroup viewGroup);

    default void hideDismissView() {
    }

    void initBubbleStyle(ViewGroup viewGroup);

    void refreshBubbleStyle(ColorStateList colorStateList);

    default void refreshTextResources() {
    }

    void setContentText(CharSequence charSequence);

    void setContentTextColor(ColorStateList colorStateList);

    default void setContentTextRes(int resId) {
    }

    void setContentView(View view);

    default void setDismissTextRes(int resId) {
    }

    default void setTitleRes(int resId) {
    }

    void setToolTipsAction(IToolTipsAction action, Context context, int mode);

    void sizeBubbleStyle(ViewGroup viewGroup, int width);
}
