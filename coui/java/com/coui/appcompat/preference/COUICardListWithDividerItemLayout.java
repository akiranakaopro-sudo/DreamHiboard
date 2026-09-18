package com.coui.appcompat.preference;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.coui.appcompat.cardlist.COUICardListSelectedItemLayout;

public class COUICardListWithDividerItemLayout extends COUICardListSelectedItemLayout {
    public COUICardListWithDividerItemLayout(Context context) {
        super(context);
    }

    public COUICardListWithDividerItemLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public COUICardListWithDividerItemLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }
}
