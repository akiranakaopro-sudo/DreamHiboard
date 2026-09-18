package com.coui.appcompat.cardview;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Rect;

import com.coui.appcompat.roundRect.COUIRoundRectUtil;

class CardViewBaseImpl implements CardViewImpl {
    private RoundRectDrawableWithShadow createBackground(Context context,
            ColorStateList colorStateList, float radius, float elevation, float maxElevation) {
        return new RoundRectDrawableWithShadow(
                context.getResources(), colorStateList, radius, elevation, maxElevation);
    }

    private RoundRectDrawableWithShadow getShadowBackground(CardViewDelegate delegate) {
        return (RoundRectDrawableWithShadow) delegate.getCardBackground();
    }

    @Override
    public ColorStateList getBackgroundColor(CardViewDelegate delegate) {
        return getShadowBackground(delegate).getColor();
    }

    @Override
    public float getElevation(CardViewDelegate delegate) {
        return getShadowBackground(delegate).getShadowSize();
    }

    @Override
    public float getMaxElevation(CardViewDelegate delegate) {
        return getShadowBackground(delegate).getMaxShadowSize();
    }

    @Override
    public float getMinHeight(CardViewDelegate delegate) {
        return getShadowBackground(delegate).getMinHeight();
    }

    @Override
    public float getMinWidth(CardViewDelegate delegate) {
        return getShadowBackground(delegate).getMinWidth();
    }

    @Override
    public float getRadius(CardViewDelegate delegate) {
        return getShadowBackground(delegate).getCornerRadius();
    }

    @Override
    public float getWeight(CardViewDelegate delegate) {
        return 0.0f;
    }

    @Override
    public void initStatic() {
        RoundRectDrawableWithShadow.setRoundRectHelper((canvas, rect, radius, paint) ->
                canvas.drawPath(COUIRoundRectUtil.getInstance().getPath(rect, radius), paint));
    }

    @Override
    public void initialize(CardViewDelegate delegate, Context context, ColorStateList colorStateList,
            float radius, float elevation, float maxElevation, float weight,
            float cardRoundCornerRadius) {
        RoundRectDrawableWithShadow background =
                createBackground(context, colorStateList, radius, elevation, maxElevation);
        background.setAddPaddingForCorners(delegate.getPreventCornerOverlap());
        delegate.setCardBackground(background);
        updatePadding(delegate);
    }

    @Override
    public void onCompatPaddingChanged(CardViewDelegate delegate) {
    }

    @Override
    public void onPreventCornerOverlapChanged(CardViewDelegate delegate) {
        getShadowBackground(delegate).setAddPaddingForCorners(delegate.getPreventCornerOverlap());
        updatePadding(delegate);
    }

    @Override
    public void setBackgroundColor(CardViewDelegate delegate, ColorStateList color) {
        getShadowBackground(delegate).setColor(color);
    }

    @Override
    public void setElevation(CardViewDelegate delegate, float elevation) {
        getShadowBackground(delegate).setShadowSize(elevation);
    }

    @Override
    public void setMaxElevation(CardViewDelegate delegate, float maxElevation) {
        getShadowBackground(delegate).setMaxShadowSize(maxElevation);
        updatePadding(delegate);
    }

    @Override
    public void setRadius(CardViewDelegate delegate, float radius) {
        getShadowBackground(delegate).setCornerRadius(radius);
        updatePadding(delegate);
    }

    @Override
    public void setWeight(CardViewDelegate delegate, float weight) {
    }

    @Override
    public void updatePadding(CardViewDelegate delegate) {
        Rect shadowPadding = new Rect();
        getShadowBackground(delegate).getMaxShadowAndCornerPadding(shadowPadding);
        delegate.setMinWidthHeightInternal(
                (int) Math.ceil(getMinWidth(delegate)),
                (int) Math.ceil(getMinHeight(delegate)));
        delegate.setShadowPadding(
                shadowPadding.left, shadowPadding.top, shadowPadding.right, shadowPadding.bottom);
    }
}
