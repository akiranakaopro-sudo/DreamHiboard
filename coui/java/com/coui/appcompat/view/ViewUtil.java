package com.coui.appcompat.view;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOverlay;

public class ViewUtil {
    private ViewUtil() {
    }

    public static ViewGroup getContentView(View view) {
        if (view == null) {
            return null;
        }
        View rootView = view.getRootView();
        ViewGroup content = rootView.findViewById(android.R.id.content);
        if (content != null) {
            return content;
        }
        if (rootView == view || !(rootView instanceof ViewGroup)) {
            return null;
        }
        return (ViewGroup) rootView;
    }

    public static ViewOverlay getContentViewOverlay(View view) {
        return getOverlay(getContentView(view));
    }

    private static ViewOverlay getOverlay(View view) {
        if (view == null) {
            return null;
        }
        return view.getOverlay();
    }
}
