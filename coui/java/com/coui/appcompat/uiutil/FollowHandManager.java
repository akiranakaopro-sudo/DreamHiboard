package com.coui.appcompat.uiutil;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;
import android.view.DisplayCutout;
import android.view.View;

import com.coui.appcompat.log.COUILog;

public class FollowHandManager {
    private static final String TAG = "FollowHandManager";
    private static Rect sAnchorRectInWindow;
    private static Rect sDecorViewRectInWindow;
    private static Rect sLimitRectInWindow;
    private static final int[] sWindowLocationOnScreen = new int[2];
    private static final int[] sTouchPosition = new int[2];
    private static final Point sClickLocationInWindow = new Point();
    private static Rect sPaddingRect = new Rect();
    private static Rect sMarginRect = new Rect();
    private static final int[] sAnchorLocationInWindow = new int[2];

    public static Point calculatePosition(Context context, int width, int height, boolean addWindowOffset) {
        int windowX;
        if (sAnchorRectInWindow == null) {
            Log.e(TAG, "The AnchorRectInWindow is null");
            return null;
        }
        Point point = new Point();
        int x = sClickLocationInWindow.x - (width / 2);
        int y = ifSetOffset() ? sClickLocationInWindow.y : sAnchorRectInWindow.bottom;
        int anchorTop = ifSetOffset() ? sClickLocationInWindow.y : sAnchorRectInWindow.top;
        int spaceBelow = getBoundaryBottomInWindow() - y;
        if (spaceBelow < height + sMarginRect.top + sMarginRect.bottom) {
            y = (anchorTop - height) - sMarginRect.bottom;
        } else if (sMarginRect.top + y + height < getBoundaryBottomInWindow()) {
            y += sMarginRect.top;
        }
        int boundedX = Math.max(getBoundaryLeftInWindow() + sMarginRect.left,
                Math.min(x, (getBoundaryRightInWindow() - sMarginRect.right) - width));
        if (addWindowOffset && ifWidthDpIsFullScreen(context) && (windowX = sWindowLocationOnScreen[0]) > 0) {
            boundedX += windowX;
        }
        point.set(boundedX, Math.max(getBoundaryTopInWindow() + sMarginRect.top, y));
        return point;
    }

    public static Rect getAnchorRectInWindow() {
        return sAnchorRectInWindow;
    }

    public static int getBoundaryBottomInWindow() {
        if (sDecorViewRectInWindow == null) {
            COUILog.e(TAG, "The sDecorViewRectInWindow is null, must calling init() first");
            return 0;
        }
        return (sLimitRectInWindow != null ? sLimitRectInWindow.bottom : sDecorViewRectInWindow.bottom)
                - sPaddingRect.bottom;
    }

    public static int getBoundaryLeftInWindow() {
        if (sDecorViewRectInWindow == null) {
            COUILog.e(TAG, "The sDecorViewRectInWindow is null, must calling init() first");
            return 0;
        }
        return (sLimitRectInWindow != null ? sLimitRectInWindow.left : sDecorViewRectInWindow.left)
                + sPaddingRect.left;
    }

    public static int getBoundaryRightInWindow() {
        if (sDecorViewRectInWindow == null) {
            COUILog.e(TAG, "The sDecorViewRectInWindow is null, must calling init() first");
            return 0;
        }
        return (sLimitRectInWindow != null ? sLimitRectInWindow.right : sDecorViewRectInWindow.right)
                - sPaddingRect.right;
    }

    public static int getBoundaryTopInWindow() {
        if (sDecorViewRectInWindow == null) {
            COUILog.e(TAG, "The sDecorViewRectInWindow is null, must calling init() first");
            return 0;
        }
        return (sLimitRectInWindow != null ? sLimitRectInWindow.top : sDecorViewRectInWindow.top)
                + sPaddingRect.top;
    }

    public static int getClickPositionXInWindow() {
        if (ifSetOffset()) {
            return sTouchPosition[0] + sAnchorLocationInWindow[0];
        }
        if (sAnchorRectInWindow != null) {
            return sAnchorRectInWindow.centerX();
        }
        COUILog.e(TAG, "The AnchorRectInWindow is null, must calling init() first");
        return 0;
    }

    public static int getClickPositionYInWindow() {
        if (ifSetOffset()) {
            return sTouchPosition[1] + sAnchorLocationInWindow[1];
        }
        if (sAnchorRectInWindow != null) {
            return sAnchorRectInWindow.centerY();
        }
        COUILog.e(TAG, "The AnchorRectInWindow is null, must calling init() first");
        return 0;
    }

    public static Rect getDecorViewRectInWindow() {
        return sDecorViewRectInWindow;
    }

    public static Rect getLimitRectInWindow() {
        return sLimitRectInWindow;
    }

    public static Rect getMarginRect() {
        return sMarginRect;
    }

    public static Rect getPaddingRect() {
        return sPaddingRect;
    }

    public static int[] getWindowLocationOnScreen() {
        return sWindowLocationOnScreen;
    }

    public static boolean ifSetOffset() {
        return sTouchPosition[0] != 0 || sTouchPosition[1] != 0;
    }

    public static boolean ifWidthDpIsFullScreen(Context context) {
        double screenWidthDp = context.getResources().getConfiguration().screenWidthDp;
        double realWidthDp = UIUtil.getScreenWidthRealSize(context)
                / context.getResources().getDisplayMetrics().density;
        return screenWidthDp == Math.floor(realWidthDp) || screenWidthDp == Math.ceil(realWidthDp);
    }

    public static void init(View view) {
        init(view, 0, 0);
    }

    private static void reset() {
        setOffset(0, 0);
        setLimitRectInWindow(null);
        sPaddingRect.set(0, 0, 0, 0);
        sMarginRect.set(0, 0, 0, 0);
    }

    public static void setLimitRectInWindow(Rect rect) {
        sLimitRectInWindow = rect;
    }

    public static void setMargin(Rect rect) {
        sMarginRect = rect;
    }

    public static void setOffset(int x, int y) {
        sTouchPosition[0] = x;
        sTouchPosition[1] = y;
    }

    public static void setPadding(Rect rect) {
        sPaddingRect = rect;
    }

    public static void init(View view, int x, int y) {
        reset();
        if (x != 0 || y != 0) {
            setOffset(x, y);
        }
        int[] location = new int[2];
        sDecorViewRectInWindow = new Rect();
        sAnchorRectInWindow = new Rect();
        view.getWindowVisibleDisplayFrame(sDecorViewRectInWindow);
        view.getGlobalVisibleRect(sAnchorRectInWindow);
        Rect rootRect = new Rect();
        view.getRootView().getGlobalVisibleRect(rootRect);
        view.getRootView().getLocationOnScreen(location);
        rootRect.offset(location[0], location[1]);
        sDecorViewRectInWindow.left = Math.max(sDecorViewRectInWindow.left, rootRect.left);
        sDecorViewRectInWindow.top = Math.max(sDecorViewRectInWindow.top, rootRect.top);
        sDecorViewRectInWindow.right = Math.min(sDecorViewRectInWindow.right, rootRect.right);
        sDecorViewRectInWindow.bottom = Math.min(sDecorViewRectInWindow.bottom, rootRect.bottom);
        view.getRootView().getLocationOnScreen(location);
        int screenX = location[0];
        int screenY = location[1];
        view.getRootView().getLocationInWindow(location);
        int windowX = location[0];
        int windowY = location[1];
        sWindowLocationOnScreen[0] = screenX - windowX;
        sWindowLocationOnScreen[1] = screenY - windowY;
        sDecorViewRectInWindow.offset(-sWindowLocationOnScreen[0], -sWindowLocationOnScreen[1]);
        view.getLocationInWindow(sAnchorLocationInWindow);
        sClickLocationInWindow.x = getClickPositionXInWindow();
        sClickLocationInWindow.y = getClickPositionYInWindow();
        if (view.getRootWindowInsets() == null) {
            return;
        }
        DisplayCutout displayCutout = view.getRootWindowInsets().getDisplayCutout();
        if (displayCutout == null) {
            return;
        }
        for (Rect cutoutRect : displayCutout.getBoundingRects()) {
            if (cutoutRect.top == 0) {
                sDecorViewRectInWindow.top = Math.max(sDecorViewRectInWindow.top, cutoutRect.bottom);
            } else if (cutoutRect.bottom == sDecorViewRectInWindow.bottom) {
                sDecorViewRectInWindow.bottom = Math.min(sDecorViewRectInWindow.bottom, cutoutRect.top);
            } else if (cutoutRect.left == 0) {
                sDecorViewRectInWindow.left = Math.max(sDecorViewRectInWindow.left, cutoutRect.right);
            } else if (cutoutRect.right == sDecorViewRectInWindow.right) {
                sDecorViewRectInWindow.right = Math.min(sDecorViewRectInWindow.right, cutoutRect.left);
            }
        }
    }
}
