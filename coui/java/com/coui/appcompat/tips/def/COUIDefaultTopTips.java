package com.coui.appcompat.tips.def;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.coui.appcompat.R;
import com.coui.appcompat.contextutil.COUIContextUtil;
import com.coui.appcompat.darkmode.COUIDarkModeUtil;
import com.coui.appcompat.roundcorner.RoundCornerUtil;
import com.coui.appcompat.tips.COUICustomTopTips;

public class COUIDefaultTopTips extends COUICustomTopTips implements IDefaultTopTips {
    private IDefaultTopTips mDefaultTopTips;

    public COUIDefaultTopTips(Context context) {
        this(context, null);
    }

    public COUIDefaultTopTips(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public COUIDefaultTopTips(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public IDefaultTopTips generateView() {
        COUIDefaultTopTipsView view = new COUIDefaultTopTipsView(getContext());
        view.setOnLinesChangedListener(lines -> { });
        view.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        setContentView(view);
        return view;
    }

    @Override
    public int getContentViewId() {
        return 0;
    }

    @Override
    public void init() {
        COUIDarkModeUtil.setForceDarkAllow(this, false);
        mDefaultTopTips = generateView();
        if (RoundCornerUtil.isVersionSupport()) {
            setRadius(COUIContextUtil.getAttrDimens(getContext(), R.attr.couiRoundCornerMRadius));
            setWeight(COUIContextUtil.getAttrFloat(getContext(), R.attr.couiRoundCornerMWeight));
        } else {
            setRadius(COUIContextUtil.getAttrDimens(getContext(), R.attr.couiRoundCornerM));
        }
        setCardBackgroundColor(ColorStateList.valueOf(
                COUIContextUtil.getAttrColor(getContext(), R.attr.couiColorContainer4)));
    }

    @Override public void setCloseBtnListener(View.OnClickListener listener) { mDefaultTopTips.setCloseBtnListener(listener); }
    @Override public void setCloseDrawable(Drawable drawable) { mDefaultTopTips.setCloseDrawable(drawable); }
    @Override public void setNegativeButton(CharSequence text) { mDefaultTopTips.setNegativeButton(text); }
    @Override public void setNegativeButtonColor(int color) { mDefaultTopTips.setNegativeButtonColor(color); }
    @Override public void setNegativeButtonListener(View.OnClickListener listener) { mDefaultTopTips.setNegativeButtonListener(listener); }
    @Override public void setPositiveButton(CharSequence text) { mDefaultTopTips.setPositiveButton(text); }
    @Override public void setPositiveButtonColor(int color) { mDefaultTopTips.setPositiveButtonColor(color); }
    @Override public void setPositiveButtonListener(View.OnClickListener listener) { mDefaultTopTips.setPositiveButtonListener(listener); }
    @Override public void setStartIcon(Drawable drawable) { mDefaultTopTips.setStartIcon(drawable); }
    @Override public void setTipsText(CharSequence text) { mDefaultTopTips.setTipsText(text); }
    @Override public void setTipsTextColor(int color) { mDefaultTopTips.setTipsTextColor(color); }
}
