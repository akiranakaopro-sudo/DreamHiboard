package com.coui.appcompat.tips.marquee;

import android.content.Context;
import android.util.AttributeSet;

import com.coui.appcompat.tips.def.COUIDefaultTopTips;
import com.coui.appcompat.tips.def.COUIDefaultTopTipsView;
import com.coui.appcompat.tips.def.IDefaultTopTips;

public class COUIMarqueeTopTips extends COUIDefaultTopTips {
    private COUIDefaultTopTipsView mCOUIDefaultTopTipsView;

    public COUIMarqueeTopTips(Context context) {
        this(context, null);
    }

    public COUIMarqueeTopTips(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public COUIMarqueeTopTips(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public IDefaultTopTips generateView() {
        COUIDefaultTopTipsView view = (COUIDefaultTopTipsView) super.generateView();
        mCOUIDefaultTopTipsView = view;
        return view;
    }

    public void startRoll() {
        mCOUIDefaultTopTipsView.startRoll();
    }

    public void stopRoll() {
        mCOUIDefaultTopTipsView.stopRoll();
    }
}
