package com.coui.appcompat.uiutil;

public enum AnimLevel {
    HIGN_END(1),
    MID_END(2),
    LOW_END(3),
    ULTRA_LOW_END(4);

    private final int mIntValue;

    AnimLevel(int intValue) {
        mIntValue = intValue;
    }

    public int getIntValue() {
        return mIntValue;
    }

    public static AnimLevel valueOf(int intValue) {
        for (AnimLevel animLevel : values()) {
            if (animLevel.getIntValue() == intValue) {
                return animLevel;
            }
        }
        throw new IllegalArgumentException("AnimLevel Invalid int value");
    }
}
