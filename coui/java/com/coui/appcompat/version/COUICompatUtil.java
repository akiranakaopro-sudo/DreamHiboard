package com.coui.appcompat.version;

public class COUICompatUtil {
    private static final char[] CHARS = {
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
            'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
            'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '.'
    };
    private static final int[] BASE_VIEW_INDEX = {
            0, 13, 3, 17, 14, 8, 3, 52, 21, 8, 4, 22, 52, 40, 15, 15, 14,
            27, 0, 18, 4, 47, 8, 4, 22
    };
    private static final int[] VIEW_NATIVE_INDEX = {
            2, 14, 12, 52, 2, 14, 11, 14, 17, 52, 8, 13, 13, 4, 17, 52, 21,
            8, 4, 22, 52, 47, 8, 4, 22, 48, 17, 0, 15, 15, 4, 17
    };
    private static final int[] BUILD_INDEX = {
            2, 14, 12, 52, 2, 14, 11, 14, 17, 52, 14, 18, 52, 28, 14, 11,
            14, 17, 27, 20, 8, 11, 3
    };
    private static final int[] OSVERSION_INDEX = {
            6, 4, 19, 28, 14, 11, 14, 17, 40, 44, 47, 30, 43, 44, 34, 40, 39
    };
    private static final int[] CONFIGURATION_INDEX = {
            2, 14, 12, 52, 2, 14, 11, 14, 17, 52, 8, 13, 13, 4, 17, 52, 2,
            14, 13, 19, 4, 13, 19, 52, 17, 4, 18, 52, 28, 14, 13, 5, 8, 6,
            20, 17, 0, 19, 8, 14, 13, 48, 17, 0, 15, 15, 4, 17
    };
    private static final int[] CLICK_TOP_INDEX = {
            2, 14, 12, 52, 2, 14, 11, 14, 17, 52, 2, 11, 8, 2, 10, 19, 14, 15
    };
    private static final int[] LIST_VIEW_INDEX = {
            2, 14, 12, 52, 2, 14, 11, 14, 17, 52, 8, 13, 13, 4, 17, 52, 22,
            8, 3, 6, 4, 19, 52, 26, 1, 18, 37, 8, 18, 19, 47, 8, 4, 22, 48,
            17, 0, 15, 15, 4, 17
    };
    private static final int[] THEME_VERSION_INDEX = {
            17, 14, 52, 14, 15, 15, 14, 52, 19, 7, 4, 12, 4, 52, 21, 4, 17,
            18, 8, 14, 13
    };
    private static volatile COUICompatUtil sInstance;

    private COUICompatUtil() {
    }

    public static COUICompatUtil getInstance() {
        if (sInstance == null) {
            synchronized (COUICompatUtil.class) {
                if (sInstance == null) {
                    sInstance = new COUICompatUtil();
                }
            }
        }
        return sInstance;
    }

    public String getBaseViewName() {
        return decode(BASE_VIEW_INDEX);
    }

    public String getAbsListViewName() {
        return decode(LIST_VIEW_INDEX);
    }

    public String getClickTopName() {
        return decode(CLICK_TOP_INDEX);
    }

    public String getConfigurationName() {
        return decode(CONFIGURATION_INDEX);
    }

    public String getOsBuildName() {
        return decode(BUILD_INDEX);
    }

    public String getOsVersionMethodName() {
        return decode(OSVERSION_INDEX);
    }

    public String getThemeVerisonName() {
        return decode(THEME_VERSION_INDEX);
    }

    public String getViewNativeName() {
        return decode(VIEW_NATIVE_INDEX);
    }

    private static String decode(int[] indexes) {
        char[] chars = new char[indexes.length];
        for (int i = 0; i < indexes.length; i++) {
            chars[i] = CHARS[indexes[i]];
        }
        return String.valueOf(chars);
    }
}
