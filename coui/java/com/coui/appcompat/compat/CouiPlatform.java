package com.coui.appcompat.compat;

/**
 * Cached detection of a real ColorOS/Oplus framework (not in-tree COUI stubs).
 * Stub classes such as {@code android.content.res.OplusBaseConfiguration} live in
 * this library for compilation; {@link Class#forName} on those names must not be
 * treated as proof that Oplus framework APIs exist on the device.
 */
public final class CouiPlatform {
    private static final String OPLUS_BUILD = "com.oplus.os.OplusBuild";

    private static Boolean sColorOsRuntime;

    private CouiPlatform() {
    }

    /** True only when {@code com.oplus.os.OplusBuild} is present on the device. */
    public static boolean isColorOsRuntime() {
        if (sColorOsRuntime != null) {
            return sColorOsRuntime;
        }
        try {
            Class.forName(OPLUS_BUILD);
            sColorOsRuntime = true;
        } catch (Throwable ignored) {
            sColorOsRuntime = false;
        }
        return sColorOsRuntime;
    }
}
