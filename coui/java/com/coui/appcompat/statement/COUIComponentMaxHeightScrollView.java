package com.coui.appcompat.statement;

import com.coui.appcompat.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;


public final class COUIComponentMaxHeightScrollView extends COUIMaxHeightScrollView {
    private boolean isProtocolFixed;

    public COUIComponentMaxHeightScrollView(Context context, AttributeSet attributeSet, int index, DefaultConstructorMarker defaultConstructorMarker) {
        this(context, (index & 2) != 0 ? null : attributeSet);
    }

    public final boolean isProtocolFixed() {
        return this.isProtocolFixed;
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (getMaxHeight() > 0) {
            heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(Math.min(getMaxHeight(), View.MeasureSpec.getSize(heightMeasureSpec)), Integer.MIN_VALUE);
        }
        if (this.isProtocolFixed && getChildCount() > 0) {
            measureChild(getChildAt(0), widthMeasureSpec, heightMeasureSpec);
            setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), getChildAt(0).getMeasuredHeight() > View.MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop() ? getContext().getResources().getDimensionPixelOffset(R.dimen.coui_component_bottom_sheet_margin) : 0);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public final void setProtocolFixed(boolean protocolFixed) {
        this.isProtocolFixed = protocolFixed;
    }


    public COUIComponentMaxHeightScrollView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        Intrinsics.checkNotNullParameter(context, "context");
    }
}
