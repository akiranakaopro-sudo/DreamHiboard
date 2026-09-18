package com.coui.appcompat.cardview;

import com.coui.appcompat.roundRect.COUIRoundRectUtil;

class CardViewApi17Impl extends CardViewBaseImpl {
    @Override
    public void initStatic() {
        RoundRectDrawableWithShadow.setRoundRectHelper((canvas, rect, radius, paint) ->
                canvas.drawPath(COUIRoundRectUtil.getInstance().getPath(rect, radius), paint));
    }
}
