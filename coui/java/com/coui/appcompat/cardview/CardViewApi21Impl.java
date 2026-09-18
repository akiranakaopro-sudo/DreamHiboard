package com.coui.appcompat.cardview;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.View;

class CardViewApi21Impl implements CardViewImpl {
    private RoundRectDrawable getCardBackground(CardViewDelegate delegate) {
        return (RoundRectDrawable) delegate.getCardBackground();
    }

    @Override
    public ColorStateList getBackgroundColor(CardViewDelegate delegate) {
        return getCardBackground(delegate).getColor();
    }

    @Override
    public float getCardRoundCornerRadius(CardViewDelegate delegate) {
        return getCardBackground(delegate).getCardRoundCornerRadius();
    }

    @Override
    public float getElevation(CardViewDelegate delegate) {
        return delegate.getCardView().getElevation();
    }

    @Override
    public float getMaxElevation(CardViewDelegate delegate) {
        return getCardBackground(delegate).getPadding();
    }

    @Override
    public float getMinHeight(CardViewDelegate delegate) {
        return getRadius(delegate) * 2.0f;
    }

    @Override
    public float getMinWidth(CardViewDelegate delegate) {
        return getRadius(delegate) * 2.0f;
    }

    @Override
    public float getRadius(CardViewDelegate delegate) {
        return getCardBackground(delegate).getRadius();
    }

    @Override
    public float getWeight(CardViewDelegate delegate) {
        return getCardBackground(delegate).getWeight();
    }

    @Override
    public void initStatic() {
    }

    @Override
    public void initialize(CardViewDelegate delegate, Context context, ColorStateList colorStateList,
            float radius, float elevation, float maxElevation, float weight,
            float cardRoundCornerRadius) {
        delegate.setCardBackground(new RoundRectDrawable(
                colorStateList, radius, weight, cardRoundCornerRadius));
        View cardView = delegate.getCardView();
        cardView.setClipToOutline(true);
        cardView.setElevation(elevation);
        setMaxElevation(delegate, maxElevation);
    }

    @Override
    public void onCompatPaddingChanged(CardViewDelegate delegate) {
        setMaxElevation(delegate, getMaxElevation(delegate));
    }

    @Override
    public void onPreventCornerOverlapChanged(CardViewDelegate delegate) {
        setMaxElevation(delegate, getMaxElevation(delegate));
    }

    @Override
    public void setBackgroundColor(CardViewDelegate delegate, ColorStateList color) {
        getCardBackground(delegate).setColor(color);
    }

    @Override
    public void setCardRoundCornerRadius(CardViewDelegate delegate, float radius) {
        getCardBackground(delegate).setCardRoundCornerRadius(radius);
    }

    @Override
    public void setElevation(CardViewDelegate delegate, float elevation) {
        delegate.getCardView().setElevation(elevation);
    }

    @Override
    public void setMaxElevation(CardViewDelegate delegate, float maxElevation) {
        getCardBackground(delegate).setPadding(
                maxElevation, delegate.getUseCompatPadding(), delegate.getPreventCornerOverlap());
        updatePadding(delegate);
    }

    @Override
    public void setRadius(CardViewDelegate delegate, float radius) {
        getCardBackground(delegate).setRadius(radius);
    }

    @Override
    public void setWeight(CardViewDelegate delegate, float weight) {
        getCardBackground(delegate).setWeight(weight);
    }

    @Override
    public void updatePadding(CardViewDelegate delegate) {
        if (!delegate.getUseCompatPadding()) {
            delegate.setShadowPadding(0, 0, 0, 0);
            return;
        }
        float maxElevation = getMaxElevation(delegate);
        float radius = getRadius(delegate);
        int horizontal = (int) Math.ceil(RoundRectDrawableWithShadow.calculateHorizontalPadding(
                maxElevation, radius, delegate.getPreventCornerOverlap()));
        int vertical = (int) Math.ceil(RoundRectDrawableWithShadow.calculateVerticalPadding(
                maxElevation, radius, delegate.getPreventCornerOverlap()));
        delegate.setShadowPadding(horizontal, vertical, horizontal, vertical);
    }
}
