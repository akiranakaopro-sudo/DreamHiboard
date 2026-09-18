package com.coui.component.responsiveui;

import android.util.Log;

public final class ResponsiveUILog {
    public static final ResponsiveUILog INSTANCE = new ResponsiveUILog();
    private static final boolean LOG_DEBUG = false;

    private ResponsiveUILog() {
    }

    public boolean getLOG_DEBUG() {
        return LOG_DEBUG;
    }

    public boolean isLoggable(String tag, int level) {
        return Log.isLoggable(tag, level);
    }
}
