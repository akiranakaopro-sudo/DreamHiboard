package com.coui.appcompat.toolbar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.appcompat.widget.TooltipCompat;
import com.coui.appcompat.R;
import com.coui.appcompat.accessibilityutil.COUIAccessibilityUtil;
import com.coui.appcompat.darkmode.COUIDarkModeUtil;
import com.coui.appcompat.state.COUIMaskRippleDrawable;
import com.coui.appcompat.textviewcompatutil.COUITextViewCompatUtil;
import java.lang.reflect.Field;


@SuppressLint({"RestrictedApi"})
public class COUIActionMenuItemView extends ActionMenuItemView {
    private static final String TAG = "COUIActionMenuItemView";
    private int mIconMenuMinWidth;
    private boolean mIsText;
    private int mMarginEnd;
    private COUIMaskRippleDrawable mMaskRippleDrawable;
    private int mPaddingHorizontal;
    private int mPaddingVertical;
    private int mTextMenuItemMaxWidth;
    private int mTextPaddingHorizontal;
    private int mTextPaddingVertical;

    public COUIActionMenuItemView(Context context) {
        this(context, null);
    }

    private void configMenuIconBackground() {
        COUIMaskRippleDrawable cOUIMaskRippleDrawable = new COUIMaskRippleDrawable(getContext());
        this.mMaskRippleDrawable = cOUIMaskRippleDrawable;
        cOUIMaskRippleDrawable.setCircleRippleMask(COUIMaskRippleDrawable.getMaskRippleRadiusByType(getContext(), 0));
        setBackground(this.mMaskRippleDrawable);
        COUIDarkModeUtil.setForceDarkAllow(this, false);
    }

    private void setReflectField(Class cls, Object obj, String str, Object reflectField) {
        try {
            Field declaredField = cls.getDeclaredField(str);
            declaredField.setAccessible(true);
            declaredField.set(obj, reflectField);
        } catch (Exception e) {
            Log.e(TAG, "setReflectField error: " + e.getMessage());
        }
    }

    @Override
    public CharSequence getAccessibilityClassName() {
        return COUIAccessibilityUtil.BUTTON_CLASS_NAME;
    }

    @Override
    public void initialize(MenuItemImpl menuItemImpl, int index) {
        super.initialize(menuItemImpl, index);
        this.mIsText = menuItemImpl.getIcon() == null;
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        if (this.mIsText) {
            COUITextViewCompatUtil.setPressRippleDrawable(this);
            setMaxWidth(this.mTextMenuItemMaxWidth);
        } else {
            setReflectField(ActionMenuItemView.class, this, "mMinWidth", Integer.valueOf(this.mIconMenuMinWidth));
            configMenuIconBackground();
            int index_2 = this.mPaddingHorizontal;
            int index_3 = this.mPaddingVertical;
            setPadding(index_2, index_3, index_2, index_3);
        }
        boolean flag = this.mIsText;
        layoutParams.height = flag ? -2 : -1;
        if (!flag && (layoutParams instanceof ViewGroup.MarginLayoutParams)) {
            ((ViewGroup.MarginLayoutParams) layoutParams).setMarginEnd(this.mMarginEnd);
        }
        setLayoutParams(layoutParams);
    }

    public boolean isTextMenuItem() {
        return this.mIsText;
    }

    @Override
    public void jumpDrawablesToCurrentState() {
    }

    @Override
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        MenuItemImpl itemData = getItemData();
        if (itemData == null || itemData.getIcon() == null) {
            return;
        }
        this.mMarginEnd = getContext().createConfigurationContext(configuration).getResources().getDimensionPixelSize(R.dimen.coui_action_menu_item_view_margin_end);
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
            ((ViewGroup.MarginLayoutParams) layoutParams).setMarginEnd(this.mMarginEnd);
        }
        TooltipCompat.setTooltipText(this, null);
    }

    public void refresh() {
        COUIMaskRippleDrawable cOUIMaskRippleDrawable = this.mMaskRippleDrawable;
        if (cOUIMaskRippleDrawable != null) {
            cOUIMaskRippleDrawable.refresh(getContext());
        }
    }

    @Override
    public void setIcon(Drawable drawable) {
        super.setIcon(drawable);
        TooltipCompat.setTooltipText(this, null);
    }

    public void setItemWithGap(boolean itemWithGap) {
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        if (!this.mIsText && (layoutParams instanceof ViewGroup.MarginLayoutParams)) {
            if (itemWithGap) {
                ((ViewGroup.MarginLayoutParams) layoutParams).setMarginEnd(this.mMarginEnd);
            } else {
                ((ViewGroup.MarginLayoutParams) layoutParams).setMarginEnd(0);
            }
        }
        setLayoutParams(layoutParams);
    }

    @Override
    public void setTitle(CharSequence charSequence) {
        super.setTitle(charSequence);
        TooltipCompat.setTooltipText(this, null);
    }

    public COUIActionMenuItemView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public COUIActionMenuItemView(Context context, AttributeSet attributeSet, int index) {
        super(context, attributeSet, index);
        this.mPaddingHorizontal = context.getResources().getDimensionPixelSize(R.dimen.coui_toolbar_menu_bg_padding_horizontal);
        this.mPaddingVertical = context.getResources().getDimensionPixelSize(R.dimen.coui_toolbar_menu_bg_padding_vertical);
        this.mTextPaddingHorizontal = context.getResources().getDimensionPixelSize(R.dimen.coui_toolbar_text_menu_bg_padding_horizontal);
        this.mTextPaddingVertical = context.getResources().getDimensionPixelSize(R.dimen.coui_toolbar_text_menu_bg_padding_vertical);
        this.mMarginEnd = context.getResources().getDimensionPixelSize(R.dimen.coui_action_menu_item_view_margin_end);
        this.mIconMenuMinWidth = context.getResources().getDimensionPixelSize(R.dimen.coui_action_menu_item_min_width);
        this.mTextMenuItemMaxWidth = context.getResources().getDimensionPixelSize(R.dimen.coui_action_bar_text_menu_item_max_width);
    }
}
