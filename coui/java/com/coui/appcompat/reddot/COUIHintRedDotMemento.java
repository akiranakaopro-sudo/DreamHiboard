package com.coui.appcompat.reddot;

public class COUIHintRedDotMemento {
    private int mPointMode;
    private int mPointNumber;
    private String mPointText;

    public void applyTo(COUIHintRedDot redDot) {
        redDot.setPointMode(mPointMode);
        redDot.setPointNumber(mPointNumber);
        redDot.setPointText(mPointText);
    }

    public int getPointMode() {
        return mPointMode;
    }

    public int getPointNumber() {
        return mPointNumber;
    }

    public String getPointText() {
        return mPointText;
    }

    public void setPointMode(int pointMode) {
        mPointMode = pointMode;
    }

    public void setPointNumber(int pointNumber) {
        mPointNumber = pointNumber;
    }

    public void setPointText(String pointText) {
        mPointText = pointText;
    }
}
