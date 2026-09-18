package com.coui.appcompat.emptyview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Size;
import android.view.View;

import com.airbnb.lottie.LottieAnimationView;
import com.coui.appcompat.darkmode.COUIDarkModeUtil;

// Leapy modified 2026-07-26: Extend upstream Lottie directly after removing
// the non-OPPO EffectiveAnimationView placeholder.
public final class EmptyStateAnimView extends LottieAnimationView {
    private Size mAnimSize = new Size(0, 0);

    public EmptyStateAnimView(Context context, AttributeSet attrs) {
        super(context, attrs);
        COUIDarkModeUtil.setForceDarkAllow(this, false);
    }

    public Size getAnimSize() {
        return mAnimSize;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(
                View.MeasureSpec.makeMeasureSpec(mAnimSize.getWidth(), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(mAnimSize.getHeight(), View.MeasureSpec.EXACTLY));
    }

    public void setAnimSize(Size value) {
        mAnimSize = value;
        setVisibility(value.getWidth() == 0 || value.getHeight() == 0 ? INVISIBLE : VISIBLE);
    }
}
