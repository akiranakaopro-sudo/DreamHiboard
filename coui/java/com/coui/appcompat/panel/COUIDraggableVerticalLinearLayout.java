package com.coui.appcompat.panel;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.appcompat.content.res.AppCompatResources;

import com.coui.appcompat.R;
import com.coui.appcompat.contextutil.COUIContextUtil;
import com.coui.appcompat.darkmode.COUIDarkModeUtil;

public class COUIDraggableVerticalLinearLayout extends LinearLayout {
    private ImageView mAnimDragView;
    private Drawable mDragViewDrawable;
    private int mDragViewDrawableTintColor;
    private float mElevation;
    private boolean mHasShadowNinePatchDrawable;
    private int mPaddingBottom;
    private int mPaddingLeft;
    private int mPaddingRight;
    private int mPaddingTop;
    private int mStyle;

    public COUIDraggableVerticalLinearLayout(Context context) {
        this(context, null);
    }

    private void initDragView(AttributeSet attributeSet, int defStyleAttr, int defStyleRes) {
        setOrientation(LinearLayout.VERTICAL);
        this.mAnimDragView = new ImageView(getContext());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams((int) getResources().getDimension(R.dimen.coui_panel_drag_view_width), (int) getResources().getDimension(R.dimen.coui_panel_drag_view_height));
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        this.mAnimDragView.setLayoutParams(layoutParams);
        COUIDarkModeUtil.setForceDarkAllow(this.mAnimDragView, false);
        setDragViewByTypeArray(getContext().obtainStyledAttributes(attributeSet, R.styleable.COUIDraggableVerticalLinearLayout, defStyleAttr, defStyleRes));
        if (attributeSet != null) {
            int styleAttribute = attributeSet.getStyleAttribute();
            this.mStyle = styleAttribute;
            if (styleAttribute == 0) {
                this.mStyle = defStyleAttr;
            }
        } else {
            this.mStyle = defStyleAttr;
        }
        recordPaddingAndElevation();
        addView(this.mAnimDragView);
    }

    private void recordPaddingAndElevation() {
        this.mElevation = getElevation();
        this.mPaddingLeft = getPaddingLeft();
        this.mPaddingTop = getPaddingTop();
        this.mPaddingRight = getPaddingRight();
        this.mPaddingBottom = getPaddingBottom();
    }

    private void setDragViewByTypeArray(TypedArray typedArray) {
        if (typedArray != null) {
            this.mHasShadowNinePatchDrawable = typedArray.getBoolean(R.styleable.COUIDraggableVerticalLinearLayout_hasShadowNinePatchDrawable, false);
            int resourceId = typedArray.getResourceId(R.styleable.COUIDraggableVerticalLinearLayout_dragViewIcon, R.drawable.coui_panel_drag_view);
            int color = typedArray.getColor(R.styleable.COUIDraggableVerticalLinearLayout_dragViewTintColor, COUIContextUtil.getAttrColor(getContext(), R.attr.couiColorControls));
            typedArray.recycle();
            Drawable dragViewDrawable = AppCompatResources.getDrawable(getContext(), resourceId);
            if (dragViewDrawable != null) {
                dragViewDrawable.setTint(color);
                this.mAnimDragView.setImageDrawable(dragViewDrawable);
            }
            if (this.mHasShadowNinePatchDrawable) {
                setBackground(AppCompatResources.getDrawable(getContext(), R.drawable.coui_panel_bg_with_shadow));
            } else {
                setBackground(AppCompatResources.getDrawable(getContext(), R.drawable.coui_default_panel_bg_without_shadow));
            }
        }
    }

    public ImageView getDragView() {
        return this.mAnimDragView;
    }

    @Deprecated
    public boolean isHasShadowNinePatchDrawable() {
        return this.mHasShadowNinePatchDrawable;
    }

    public void refresh() {
        TypedArray typedArray = null;
        if (this.mStyle != 0) {
            String resourceTypeName = getResources().getResourceTypeName(this.mStyle);
            if (TextUtils.equals(resourceTypeName, "attr")) {
                typedArray = getContext().getTheme().obtainStyledAttributes(null, R.styleable.COUIDraggableVerticalLinearLayout, this.mStyle, 0);
            } else if (TextUtils.equals(resourceTypeName, "style")) {
                typedArray = getContext().getTheme().obtainStyledAttributes(null, R.styleable.COUIDraggableVerticalLinearLayout, 0, this.mStyle);
            }
        }
        setDragViewByTypeArray(typedArray);
        setBackground(AppCompatResources.getDrawable(getContext(), R.drawable.coui_panel_bg_with_shadow));
    }

    public void setDragViewDrawable(Drawable drawable) {
        if (drawable != null) {
            this.mDragViewDrawable = drawable;
            this.mAnimDragView.setImageDrawable(drawable);
        }
    }

    public void setDragViewDrawableTintColor(int tintColor) {
        Drawable drawable = this.mDragViewDrawable;
        if (drawable == null || this.mDragViewDrawableTintColor == tintColor) {
            return;
        }
        this.mDragViewDrawableTintColor = tintColor;
        drawable.setTint(tintColor);
        this.mAnimDragView.setImageDrawable(this.mDragViewDrawable);
    }

    @Deprecated
    public void setHasShadowNinePatchDrawable(boolean hasShadowNinePatchDrawable) {
        this.mHasShadowNinePatchDrawable = hasShadowNinePatchDrawable;
        if (hasShadowNinePatchDrawable) {
            setBackground(AppCompatResources.getDrawable(getContext(), R.drawable.coui_panel_bg_with_shadow));
            setElevation(0.0f);
        } else {
            setBackground(AppCompatResources.getDrawable(getContext(), R.drawable.coui_default_panel_bg_without_shadow));
            setPadding(this.mPaddingLeft, this.mPaddingTop, this.mPaddingRight, this.mPaddingBottom);
            setElevation(this.mElevation);
        }
        invalidate();
    }

    @Override
    public void setOrientation(int orientation) {
        super.setOrientation(LinearLayout.VERTICAL);
    }

    public COUIDraggableVerticalLinearLayout(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, R.attr.couiDraggableVerticalLinearLayoutStyle);
    }

    public COUIDraggableVerticalLinearLayout(Context context, AttributeSet attributeSet, int defStyleAttr) {
        this(context, attributeSet, defStyleAttr, COUIContextUtil.isCOUIDarkTheme(context) ? R.style.COUIDraggableVerticalLinearLayout_Dark : R.style.COUIDraggableVerticalLinearLayout);
    }

    public COUIDraggableVerticalLinearLayout(Context context, AttributeSet attributeSet, int defStyleAttr, int defStyleRes) {
        super(context, attributeSet, defStyleAttr, defStyleRes);
        this.mHasShadowNinePatchDrawable = false;
        this.mElevation = 0;
        this.mPaddingLeft = 0;
        this.mPaddingTop = 0;
        this.mPaddingRight = 0;
        this.mPaddingBottom = 0;
        initDragView(attributeSet, defStyleAttr, defStyleRes);
    }
}
