package com.coui.appcompat.cardview;

import android.content.Context;
import android.content.res.ColorStateList;

interface CardViewImpl {
    ColorStateList getBackgroundColor(CardViewDelegate delegate);

    default float getCardRoundCornerRadius(CardViewDelegate delegate) {
        return 0.0f;
    }

    float getElevation(CardViewDelegate delegate);

    float getMaxElevation(CardViewDelegate delegate);

    float getMinHeight(CardViewDelegate delegate);

    float getMinWidth(CardViewDelegate delegate);

    float getRadius(CardViewDelegate delegate);

    float getWeight(CardViewDelegate delegate);

    void initStatic();

    void initialize(CardViewDelegate delegate, Context context, ColorStateList backgroundColor,
            float radius, float elevation, float maxElevation, float weight,
            float cardRoundCornerRadius);

    void onCompatPaddingChanged(CardViewDelegate delegate);

    void onPreventCornerOverlapChanged(CardViewDelegate delegate);

    void setBackgroundColor(CardViewDelegate delegate, ColorStateList color);

    default void setCardRoundCornerRadius(CardViewDelegate delegate, float radius) {
    }

    void setElevation(CardViewDelegate delegate, float elevation);

    void setMaxElevation(CardViewDelegate delegate, float maxElevation);

    void setRadius(CardViewDelegate delegate, float radius);

    void setWeight(CardViewDelegate delegate, float weight);

    void updatePadding(CardViewDelegate delegate);
}
