package com.oplus.wrapper.os;

public class SystemProperties {
    private SystemProperties() {
    }

    public static String get(String key) {
        return get(key, "");
    }

    public static String get(String key, String def) {
        try {
            Class<?> clazz = Class.forName("android.os.SystemProperties");
            return (String) clazz.getMethod("get", String.class, String.class).invoke(null, key, def);
        } catch (Throwable ignored) {
            return def;
        }
    }

    public static boolean getBoolean(String key, boolean def) {
        try {
            Class<?> clazz = Class.forName("android.os.SystemProperties");
            Object result = clazz.getMethod("getBoolean", String.class, boolean.class).invoke(null, key, def);
            return result instanceof Boolean ? (Boolean) result : def;
        } catch (Throwable ignored) {
            return def;
        }
    }

    public static int getInt(String key, int def) {
        try {
            Class<?> clazz = Class.forName("android.os.SystemProperties");
            Object result = clazz.getMethod("getInt", String.class, int.class).invoke(null, key, def);
            return result instanceof Integer ? (Integer) result : def;
        } catch (Throwable ignored) {
            return def;
        }
    }

    public static long getLong(String key, long def) {
        try {
            Class<?> clazz = Class.forName("android.os.SystemProperties");
            Object result = clazz.getMethod("getLong", String.class, long.class).invoke(null, key, def);
            return result instanceof Long ? (Long) result : def;
        } catch (Throwable ignored) {
            return def;
        }
    }

    public static void set(String key, String value) {
        try {
            Class<?> clazz = Class.forName("android.os.SystemProperties");
            clazz.getMethod("set", String.class, String.class).invoke(null, key, value);
        } catch (Throwable ignored) {
        }
    }
}
