package com.coui.appcompat.log;

import android.util.Log;

public final class COUILog {
    public static final boolean LOG_DEBUG = false;

    private COUILog() {
    }

    public static void d(String tag, String message) {
        Log.d(tag, message);
    }

    public static void d(boolean debug, String tag, String message) {
        if (debug) {
            Log.d(tag, message);
        }
    }

    public static void e(String tag, String message) {
        Log.e(tag, message);
    }

    public static void i(String tag, String message) {
        Log.i(tag, message);
    }

    public static void v(String tag, String message) {
        Log.v(tag, message);
    }

    public static void w(String tag, String message) {
        Log.w(tag, message);
    }

    public static boolean isLoggable(String tag, int level) {
        return Log.isLoggable(tag, level);
    }
}
