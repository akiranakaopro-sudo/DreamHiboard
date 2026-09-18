package com.coui.appcompat.dialog;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.core.view.ViewCompat;

import com.coui.appcompat.R;
import com.coui.appcompat.contextutil.COUIContextUtil;
import com.coui.appcompat.dialog.widget.COUIAlertDialogMaxLinearLayout;
import com.coui.appcompat.uiutil.FollowHandManager;
import com.coui.appcompat.uiutil.ShadowUtils;
import com.coui.appcompat.uiutil.UIUtil;

public class COUIBottomAlertDialogAdjustUtil {
    public interface OnFirstLayoutListener {
        void onFirstLayout();
    }

    public static void adjustToFree(Window window, View anchorView) {
        adjustToFree(window, anchorView, null);
    }

    private static int dpToPx(Context context, float value) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                context.getResources().getDisplayMetrics()));
    }

    private static int getDimensionPixel(Window window, int resId, int defValue) {
        Resources resources = window.getDecorView().getResources();
        return resources == null || resId == 0 ? defValue : resources.getDimensionPixelOffset(resId);
    }

    private static Drawable getDrawable(Window window, int resId) {
        Context context = window.getDecorView().getContext();
        return context == null || resId == 0 ? null : context.getDrawable(resId);
    }

    public static Rect getLocationRectInScreen(View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        return new Rect(location[0], location[1], view.getMeasuredWidth() + location[0],
                location[1] + view.getMeasuredHeight());
    }

    private static void offsetWindowTo(Window window, int x, int y) {
        WindowManager.LayoutParams attrs = window.getAttributes();
        attrs.x = x;
        attrs.y = y;
        window.setAttributes(attrs);
    }

    private static void setFirstLayoutListener(final Window window,
            final OnFirstLayoutListener listener) {
        if (listener == null) {
            return;
        }
        window.getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        window.getDecorView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        listener.onFirstLayout();
                    }
                });
    }

    private static void setWindowWidth(Window window, int width) {
        WindowManager.LayoutParams attrs = window.getAttributes();
        attrs.width = width;
        window.setAttributes(attrs);
    }

    private static void updateParentPanel(Window window, boolean followHand) {
        View parentPanel = window.findViewById(R.id.parentPanel);
        if (!(parentPanel instanceof COUIAlertDialogMaxLinearLayout)) {
            return;
        }
        if (followHand) {
            int shadowSize = getDimensionPixel(window, R.dimen.support_shadow_size_level_four, 0);
            LinearLayout wrapper = new LinearLayout(window.getContext());
            Rect decorRect = FollowHandManager.getDecorViewRectInWindow();
            int wrapperHeight = decorRect != null ? decorRect.height() : ViewGroup.LayoutParams.MATCH_PARENT;
            wrapper.setLayoutParams(new ViewGroup.LayoutParams(
                    UIUtil.dip2px(window.getContext(),
                            window.getContext().getResources().getConfiguration().screenWidthDp),
                    wrapperHeight));
            if (FollowHandManager.ifWidthDpIsFullScreen(window.getContext())
                    && FollowHandManager.getWindowLocationOnScreen()[0] > 0) {
                WindowManager.LayoutParams attrs = new WindowManager.LayoutParams();
                attrs.copyFrom(window.getAttributes());
                attrs.gravity = Gravity.TOP | Gravity.START;
                attrs.x = FollowHandManager.getWindowLocationOnScreen()[0];
                attrs.y = FollowHandManager.getWindowLocationOnScreen()[1];
                window.setAttributes(attrs);
            }
            UIUtil.safeForceHasOverlappingRendering(wrapper, false);
            wrapper.setClipToOutline(false);
            wrapper.setClipChildren(false);
            parentPanel.setClipToOutline(false);
            ((COUIAlertDialogMaxLinearLayout) parentPanel).setClipChildren(false);
            ViewGroup parent = (ViewGroup) parentPanel.getParent();
            parent.removeView(parentPanel);
            parent.addView(wrapper);
            wrapper.addView(parentPanel);
            ShadowUtils.setElevationToView(parentPanel, ShadowUtils.SHADOW_LV4, shadowSize,
                    COUIContextUtil.getColor(window.getContext(),
                            R.color.coui_dialog_follow_hand_spot_shadow_color));
        } else {
            ((COUIAlertDialogMaxLinearLayout) parentPanel).setMaxWidth(
                    getDimensionPixel(window, R.dimen.coui_dialog_max_width, 0));
        }
        parentPanel.setBackground(getDrawable(window, R.drawable.coui_alert_dialog_builder_background));
        parentPanel.requestLayout();
    }

    private static void updateWindowLocation(Window window, View anchorView, Point point,
            Point extraOffset) {
        Point position;
        if (anchorView == null && point != null) {
            offsetWindowTo(window, point.x, point.y);
            return;
        }
        int anchorPaddingTop = getDimensionPixel(window,
                R.dimen.coui_alert_dialog_layout_anchor_view_padding_top, 0);
        View parentPanel = window.findViewById(R.id.parentPanel);
        if (parentPanel == null || anchorView == null) {
            return;
        }
        position = FollowHandManager.calculatePosition(anchorView.getContext(),
                parentPanel.getMeasuredWidth(), parentPanel.getMeasuredHeight() + anchorPaddingTop,
                false);
        if (position == null) {
            return;
        }
        if (point == null && position.y < FollowHandManager.getClickPositionYInWindow()) {
            position.y += dpToPx(anchorView.getContext(), 8.0f);
        }
        Rect decorRect = FollowHandManager.getDecorViewRectInWindow();
        int y = position.y - (decorRect != null ? decorRect.top : 0);
        position.y = y;
        if (extraOffset != null) {
            position.x += extraOffset.x;
            position.y = y + extraOffset.y;
        }
        if (parentPanel instanceof LinearLayout) {
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) parentPanel.getLayoutParams();
            lp.topMargin = position.y + anchorPaddingTop;
            Rect visibleFrame = new Rect();
            if (FollowHandManager.getWindowLocationOnScreen()[0] == 0) {
                anchorView.getWindowVisibleDisplayFrame(visibleFrame);
            }
            if (ViewCompat.getLayoutDirection(anchorView) == ViewCompat.LAYOUT_DIRECTION_RTL) {
                lp.setMarginStart(((FollowHandManager.getDecorViewRectInWindow().width()
                        - position.x) - parentPanel.getMeasuredWidth()) + visibleFrame.left);
            } else {
                lp.setMarginStart(position.x - visibleFrame.left);
            }
            parentPanel.setLayoutParams(lp);
        }
    }

    public static void adjustToFree(Window window, View anchorView, Point point) {
        adjustToFree(window, anchorView, point, null);
    }

    public static void adjustToFree(final Window window, final View anchorView, final Point point,
            final Point extraOffset) {
        if (window == null) {
            return;
        }
        setWindowWidth(window, WindowManager.LayoutParams.WRAP_CONTENT);
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setGravity(Gravity.TOP | Gravity.START);
        window.setWindowAnimations(R.style.Animation_COUI_DialogListWindow);
        setFirstLayoutListener(window, new OnFirstLayoutListener() {
            @Override
            public void onFirstLayout() {
                final View parentPanel = window.findViewById(R.id.parentPanel);
                if (parentPanel == null) {
                    return;
                }
                if (point == null) {
                    FollowHandManager.init(anchorView);
                } else {
                    FollowHandManager.init(anchorView, point.x, point.y);
                }
                updateParentPanel(window, true);
                parentPanel.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                    @Override
                    public void onLayoutChange(View view, int left, int top, int right, int bottom,
                            int oldLeft, int oldTop, int oldRight, int oldBottom) {
                        updateWindowLocation(window, anchorView, point, extraOffset);
                        parentPanel.removeOnLayoutChangeListener(this);
                        window.getDecorView().setVisibility(View.VISIBLE);
                    }
                });
            }
        });
    }
}
