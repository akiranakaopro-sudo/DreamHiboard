package com.oplus.wrapper.os;

public class Trace {
    public static final long TRACE_TAG_GRAPHICS = 2L;
    public static final long TRACE_TAG_HAL = 2048L;
    public static final long TRACE_TAG_ACTIVITY_MANAGER = 64L;
    public static final long TRACE_TAG_VIEW = 8L;

    public static void traceBegin(long traceTag, String methodName) {
        try {
            android.os.Trace.class.getMethod("traceBegin", long.class, String.class)
                    .invoke(null, traceTag, methodName);
        } catch (Throwable ignored) {
            android.os.Trace.beginSection(methodName);
        }
    }

    public static void traceEnd(long traceTag) {
        try {
            android.os.Trace.class.getMethod("traceEnd", long.class).invoke(null, traceTag);
        } catch (Throwable ignored) {
            android.os.Trace.endSection();
        }
    }

    public static void asyncTraceBegin(long traceTag, String methodName, int cookie) {
        try {
            android.os.Trace.class.getMethod("asyncTraceBegin", long.class, String.class, int.class)
                    .invoke(null, traceTag, methodName, cookie);
        } catch (Throwable ignored) {
            android.os.Trace.beginAsyncSection(methodName, cookie);
        }
    }

    public static void asyncTraceEnd(long traceTag, String methodName, int cookie) {
        try {
            android.os.Trace.class.getMethod("asyncTraceEnd", long.class, String.class, int.class)
                    .invoke(null, traceTag, methodName, cookie);
        } catch (Throwable ignored) {
            android.os.Trace.endAsyncSection(methodName, cookie);
        }
    }

    public static void traceCounter(long traceTag, String counterName, int counterValue) {
        try {
            android.os.Trace.class.getMethod("traceCounter", long.class, String.class, int.class)
                    .invoke(null, traceTag, counterName, counterValue);
        } catch (Throwable ignored) {
            android.os.Trace.setCounter(counterName, counterValue);
        }
    }
}
