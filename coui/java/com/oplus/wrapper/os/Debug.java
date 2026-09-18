package com.oplus.wrapper.os;

public final class Debug {
    private Debug() {
    }

    public static String getCallers(int depth) {
        try {
            Object result = android.os.Debug.class.getMethod("getCallers", int.class)
                    .invoke(null, depth);
            if (result instanceof String) {
                return (String) result;
            }
        } catch (Throwable ignored) {
        }
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StringBuilder callers = new StringBuilder();
        int start = 3;
        int end = Math.min(stackTrace.length, start + depth);
        for (int i = start; i < end; i++) {
            if (callers.length() > 0) {
                callers.append(" <- ");
            }
            callers.append(stackTrace[i].toString());
        }
        return callers.toString();
    }
}
