package com.coui.appcompat.poplist;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Outline;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.PopupWindow;

import androidx.core.content.ContextCompat;

import com.coui.appcompat.R;
import com.coui.appcompat.contextutil.COUIContextUtil;
import com.coui.appcompat.uiutil.ShadowUtils;

public class COUIPopupWindow extends PopupWindow {
    private Context mContext;
    protected boolean mIsOutLineBackgroundInPopupWindow = true;
    private boolean mSetElevationInPopupwindow;
    protected WindowSpacingControlHelper mWindowSpacingControlHelper = new WindowSpacingControlHelper();

    public COUIPopupWindow(Context context) {
        this(context, null);
    }

    public COUIPopupWindow(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.popupWindowStyle);
    }

    public COUIPopupWindow(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Widget_COUI_PopupWindow);
    }

    public COUIPopupWindow(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initPopupWindow(context);
    }

    public COUIPopupWindow(View contentView, int width, int height) {
        this(contentView, width, height, false);
    }

    public COUIPopupWindow(View contentView, int width, int height, boolean focusable) {
        super(contentView, width, height, focusable);
        initPopupWindow(contentView.getContext());
    }

    private void initPopupWindow(Context context) {
        mContext = context;
        TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{R.attr.couiPopupWindowBackground});
        initPopupWindowBackground(context, a);
        a.recycle();
        setClippingEnabled(false);
        setElevation(0f);
        setExitTransition(null);
        setEnterTransition(null);
        setAnimationStyle(R.style.Animation_COUI_PopupListWindow);
    }

    public void addSpacingControlUtil(Context context, int resId, WindowSpacingControlHelper.AnchorViewTypeEnum anchorViewTypeEnum) {
        if (context != null) {
            addSpacingControlUtil(context.getResources().getDimensionPixelSize(resId), anchorViewTypeEnum);
        }
    }

    public void addSpacingControlUtil(int spacing, WindowSpacingControlHelper.AnchorViewTypeEnum anchorViewTypeEnum) {
        mWindowSpacingControlHelper.addAnchorViewSpacingMap(spacing, anchorViewTypeEnum);
    }

    public int getAnchorViewSpacing(WindowSpacingControlHelper.AnchorViewTypeEnum anchorViewTypeEnum) {
        return mWindowSpacingControlHelper.isUtilMapInit()
                ? mWindowSpacingControlHelper.getAnchorViewSpacing(anchorViewTypeEnum)
                : 0;
    }

    public int getAnchorViewSpacing(View view, WindowSpacingControlHelper.AnchorViewTypeEnum anchorViewTypeEnum) {
        return mWindowSpacingControlHelper.isUtilMapInit()
                ? mWindowSpacingControlHelper.getAnchorViewSpacing(view, anchorViewTypeEnum)
                : 0;
    }

    public void initElevationInPopupwindow() {
        if (!mSetElevationInPopupwindow || getContentView() == null) {
            return;
        }
        setBackgroundDrawable(null);
        if (ShadowUtils.checkOPlusViewElevationSDK()) {
            ShadowUtils.setElevationToView(getContentView(), 3);
        } else {
            setElevation(mContext.getResources().getDimensionPixelSize(R.dimen.support_shadow_size_level_five));
            getContentView().setOutlineSpotShadowColor(ContextCompat.getColor(mContext, R.color.coui_popup_outline_spot_shadow_color));
        }
    }

    public void initOutlineRoundRectBackground() {
        if (!mIsOutLineBackgroundInPopupWindow || getContentView() == null) {
            return;
        }
        getContentView().setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(),
                        COUIContextUtil.getAttrDimens(view.getContext(), R.attr.couiRoundCornerM));
            }
        });
        getContentView().setClipToOutline(true);
    }

    public void initPopupWindowBackground(Context context, TypedArray typedArray) {
        setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.coui_free_bottom_alert_poplist_background));
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        initOutlineRoundRectBackground();
        initElevationInPopupwindow();
    }

    public void setDismissTouchOutside(boolean dismissTouchOutside) {
        if (dismissTouchOutside) {
            setTouchable(true);
            setFocusable(true);
            setOutsideTouchable(true);
        } else {
            setFocusable(false);
            setOutsideTouchable(false);
        }
        update();
    }

    public void setElevationInPopupwindow(boolean elevationInPopupwindow) {
        mSetElevationInPopupwindow = elevationInPopupwindow;
    }
}
