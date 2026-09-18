package com.oplus.physicsengine.common;

import android.util.Log;

public abstract class Debug {
    public static boolean sDebug = false;
    public static boolean sDebugFrame = false;

    public static void logD(String message) {
        Log.d("PhysicsWorld", message);
    }

    public static void setDebugMode(boolean debug) {
        sDebug = debug;
        sDebugFrame = debug;
    }
}
