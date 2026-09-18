package com.coui.appcompat.view;

import android.util.Log;
import android.view.View;

import com.coui.appcompat.version.COUICompatUtil;

import java.lang.reflect.Field;

public class ViewNative {
    private static final String TAG = "ViewNative";
    private static final String VIEW_WRAPPER_PATH_NEW = "com.oplus.inner.view.ViewWrapper";
    private static String sViewNativeWrapperName;

    private static boolean canReachFrameworkWrapper() {
        try {
            Class.forName(VIEW_WRAPPER_PATH_NEW);
            return true;
        } catch (Exception e) {
            // The wrapper is optional outside an OPPO framework.
            return false;
        }
    }

    public static void setScrollX(View view, int scrollX) {
        String viewNativeName = canReachFrameworkWrapper()
                ? VIEW_WRAPPER_PATH_NEW
                : COUICompatUtil.getInstance().getViewNativeName();
        sViewNativeWrapperName = viewNativeName;
        try {
            Class.forName(viewNativeName)
                    .getDeclaredMethod("setScrollXForColor", View.class, Integer.TYPE)
                    .invoke(null, view, scrollX);
        } catch (Exception e) {
            // Leapy modified 2026-07-30: MTK does not provide OPPO's
            // com.oplus/com.color ViewWrapper classes. COUIRecyclerView relies
            // on this write for every overscroll frame, so the old no-op catch
            // prevented the spring offset from accumulating. Use the direct
            // field path already present in decoded OPPO ViewNative, with the
            // public Android writer as the final compatibility fallback.
            try {
                Field field = View.class.getDeclaredField("mScrollX");
                field.setAccessible(true);
                field.setInt(view, scrollX);
            } catch (Exception fieldError) {
                Log.d(TAG, fieldError.toString());
                view.scrollTo(scrollX, view.getScrollY());
            }
            // Leapy end
        }
    }

    public static void setScrollY(View view, int scrollY) {
        String viewNativeName = canReachFrameworkWrapper()
                ? VIEW_WRAPPER_PATH_NEW
                : COUICompatUtil.getInstance().getViewNativeName();
        sViewNativeWrapperName = viewNativeName;
        try {
            Class.forName(viewNativeName)
                    .getDeclaredMethod("setScrollYForColor", View.class, Integer.TYPE)
                    .invoke(null, view, scrollY);
        } catch (Exception e) {
            // Leapy modified 2026-07-30: Mirror the decoded OPPO direct-field
            // path when its vendor wrapper is absent on MTK.
            try {
                Field field = View.class.getDeclaredField("mScrollY");
                field.setAccessible(true);
                field.setInt(view, scrollY);
            } catch (Exception fieldError) {
                Log.d(TAG, fieldError.toString());
                view.scrollTo(view.getScrollX(), scrollY);
            }
            // Leapy end
        }
    }
}
