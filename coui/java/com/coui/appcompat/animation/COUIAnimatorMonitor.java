package com.coui.appcompat.animation;

import com.oplus.wrapper.os.Debug;
import com.oplus.wrapper.os.SystemProperties;
import com.oplus.wrapper.os.Trace;

public class COUIAnimatorMonitor {
    private static final int CALLERS_DEPTH = 10;
    private boolean mDebug;

    public COUIAnimatorMonitor(Object target) {
        this.mDebug = false;
        try {
            this.mDebug = SystemProperties.getBoolean("debug.sys.animtrace.enable", false);
        } catch (Error | Exception unused) {
        }
    }

    public void hookAfterUpdateProperty(long durationMillis) {
    }

    public void hookAnimationEnd() {
    }

    public void hookAnimationStart() {
        if (this.mDebug) {
            try {
                long traceTag = Trace.TRACE_TAG_VIEW;
                Trace.traceBegin(traceTag, "AnimatorStart " + Debug.getCallers(CALLERS_DEPTH));
                Trace.traceEnd(traceTag);
            } catch (Error | Exception unused) {
            }
        }
    }

    public void hookAnimator() {
    }

    public void hookSpringAnimationEnd(int cookie) {
        if (this.mDebug) {
            try {
                Trace.asyncTraceEnd(Trace.TRACE_TAG_VIEW, "spring_animator", cookie);
            } catch (Error | Exception unused) {
            }
            hookAnimationEnd();
        }
    }

    public void hookSpringAnimationStart(int cookie) {
        if (this.mDebug) {
            try {
                Trace.asyncTraceBegin(Trace.TRACE_TAG_VIEW, "spring_animator", cookie);
            } catch (Error | Exception unused) {
            }
            hookAnimationStart();
        }
    }
}
