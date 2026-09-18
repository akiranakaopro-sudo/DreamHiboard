package com.coui.appcompat.tips.def;

import android.graphics.drawable.Drawable;
import android.view.View;

public interface IDefaultTopTips {
    void setCloseBtnListener(View.OnClickListener listener);
    void setCloseDrawable(Drawable drawable);
    void setNegativeButton(CharSequence text);
    void setNegativeButtonColor(int color);
    void setNegativeButtonListener(View.OnClickListener listener);
    void setPositiveButton(CharSequence text);
    void setPositiveButtonColor(int color);
    void setPositiveButtonListener(View.OnClickListener listener);
    void setStartIcon(Drawable drawable);
    void setTipsText(CharSequence text);
    void setTipsTextColor(int color);
}
