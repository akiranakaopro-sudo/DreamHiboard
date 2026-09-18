package com.oplus.flexiblewindow;

import android.app.Activity;

public class FlexibleWindowManager {
    private static final FlexibleWindowManager INSTANCE = new FlexibleWindowManager();

    public static FlexibleWindowManager getInstance() {
        return INSTANCE;
    }

    public int getFlexibleWindowState(Activity activity) {
        return 0;
    }
}
