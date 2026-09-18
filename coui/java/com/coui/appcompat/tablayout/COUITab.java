package com.coui.appcompat.tablayout;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.coui.appcompat.reddot.COUIHintRedDot;
import androidx.core.content.res.ResourcesCompat;


public class COUITab {
    public static final int INVALID_POSITION = -1;
    private CharSequence mContentDesc;
    private View mCustomView;
    private Drawable mIcon;
    COUITabLayout mParent;
    private Object mTag;
    private CharSequence mText;
    COUITabView mView;
    private int mPosition = INVALID_POSITION;
    private boolean mIsBold = true;

    public CharSequence getContentDescription() {
        return this.mContentDesc;
    }

    public View getCustomView() {
        return this.mCustomView;
    }

    public Drawable getIcon() {
        return this.mIcon;
    }

    @Deprecated
    public int getPointMode() {
        COUITabView cOUITabView = this.mView;
        if (cOUITabView == null || cOUITabView.getHintRedDot() == null) {
            return 0;
        }
        return this.mView.getHintRedDot().getPointMode();
    }

    @Deprecated
    public int getPointNumber() {
        COUITabView cOUITabView = this.mView;
        if (cOUITabView == null || cOUITabView.getHintRedDot() == null) {
            return 0;
        }
        return this.mView.getHintRedDot().getPointNumber();
    }

    public int getPosition() {
        return this.mPosition;
    }

    public COUIHintRedDot getRedPoint() {
        COUITabView cOUITabView = this.mView;
        if (cOUITabView != null) {
            return cOUITabView.getHintRedDot();
        }
        return null;
    }

    public boolean getSelectedByClick() {
        COUITabView cOUITabView = this.mView;
        if (cOUITabView != null) {
            return cOUITabView.getSelectedByClick();
        }
        return false;
    }

    public Object getTag() {
        return this.mTag;
    }

    public CharSequence getText() {
        return this.mText;
    }

    public COUITabView getView() {
        return this.mView;
    }

    public boolean isBold() {
        return this.mIsBold;
    }

    public boolean isSelected() {
        COUITabLayout cOUITabLayout = this.mParent;
        if (cOUITabLayout != null) {
            return cOUITabLayout.getSelectedTabPosition() == this.mPosition;
        }
        throw new IllegalArgumentException("Tab not attached to a COUITabLayout");
    }

    public void reset() {
        this.mParent = null;
        this.mView = null;
        this.mTag = null;
        this.mIcon = null;
        this.mText = null;
        this.mContentDesc = null;
        this.mPosition = INVALID_POSITION;
        this.mCustomView = null;
    }

    public void select() {
        COUITabLayout cOUITabLayout = this.mParent;
        if (cOUITabLayout == null) {
            throw new IllegalArgumentException("Tab not attached to a COUITabLayout");
        }
        cOUITabLayout.selectTab(this);
    }

    public void setBold(boolean bold) {
        this.mIsBold = bold;
    }

    public COUITab setContentDescription(int contentDescription) {
        COUITabLayout cOUITabLayout = this.mParent;
        if (cOUITabLayout != null) {
            return setContentDescription(cOUITabLayout.getResources().getText(contentDescription));
        }
        throw new IllegalArgumentException("Tab not attached to a COUITabLayout");
    }

    public COUITab setCustomView(View view) {
        this.mCustomView = view;
        return this;
    }

    public COUITab setIcon(Drawable drawable) {
        this.mIcon = drawable;
        updateView();
        return this;
    }

    @Deprecated
    public COUITab setPointMode(int pointMode) {
        COUITabView cOUITabView = this.mView;
        if (cOUITabView != null && cOUITabView.getHintRedDot() != null && pointMode != this.mView.getHintRedDot().getPointMode()) {
            this.mView.getHintRedDot().setPointMode(pointMode);
        }
        return this;
    }

    @Deprecated
    public COUITab setPointNumber(int pointNumber) {
        COUITabView cOUITabView = this.mView;
        if (cOUITabView != null && cOUITabView.getHintRedDot() != null && pointNumber != this.mView.getHintRedDot().getPointNumber()) {
            this.mView.getHintRedDot().setPointNumber(pointNumber);
        }
        return this;
    }

    public void setPosition(int position) {
        this.mPosition = position;
    }

    public COUITab setTag(Object obj) {
        this.mTag = obj;
        return this;
    }

    public COUITab setText(CharSequence charSequence) {
        this.mText = charSequence;
        updateView();
        return this;
    }

    public COUITab updateTabView() {
        updateView();
        return this;
    }

    public void updateView() {
        COUITabView cOUITabView = this.mView;
        if (cOUITabView != null) {
            cOUITabView.update();
        }
    }

    public COUITab setCustomView(int customView) {
        COUITabLayout cOUITabLayout = this.mParent;
        if (cOUITabLayout == null) {
            throw new IllegalArgumentException("Tab not attached to a COUITabLayout");
        }
        this.mCustomView = LayoutInflater.from(cOUITabLayout.getContext()).inflate(customView, (ViewGroup) this.mParent, false);
        return this;
    }

    public COUITab setIcon(int icon) {
        COUITabLayout cOUITabLayout = this.mParent;
        if (cOUITabLayout != null) {
            return setIcon(ResourcesCompat.getDrawable(cOUITabLayout.getResources(), icon, null));
        }
        throw new IllegalArgumentException("Tab not attached to a TabLayout");
    }

    public COUITab setText(int text) {
        COUITabLayout cOUITabLayout = this.mParent;
        if (cOUITabLayout != null) {
            return setText(cOUITabLayout.getResources().getText(text));
        }
        throw new IllegalArgumentException("Tab not attached to a TabLayout");
    }

    public COUITab setContentDescription(CharSequence charSequence) {
        this.mContentDesc = charSequence;
        updateView();
        return this;
    }
}






