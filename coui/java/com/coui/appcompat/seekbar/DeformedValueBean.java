package com.coui.appcompat.seekbar;

public class DeformedValueBean {
    private float mDrawProgressScale;
    private float mHeightBottomDeformedDownValue;
    private float mHeightBottomDeformedUpValue;
    private float mHeightTopDeformedDownValue;
    private float mHeightTopDeformedUpValue;
    private int mProgress;
    private float mScale;
    private float mWidthDeformedValue;

    public DeformedValueBean(float heightBottomUp, float heightTopUp, float width,
            float heightBottomDown, float heightTopDown, int progress) {
        mHeightBottomDeformedUpValue = heightBottomUp;
        mHeightTopDeformedUpValue = heightTopUp;
        mWidthDeformedValue = width;
        mHeightBottomDeformedDownValue = heightBottomDown;
        mHeightTopDeformedDownValue = heightTopDown;
        mProgress = progress;
    }

    public float getDrawProgressScale() { return mDrawProgressScale; }
    public float getHeightBottomDeformedDownValue() { return mHeightBottomDeformedDownValue; }
    public float getHeightBottomDeformedUpValue() { return mHeightBottomDeformedUpValue; }
    public float getHeightTopDeformedDownValue() { return mHeightTopDeformedDownValue; }
    public float getHeightTopDeformedUpValue() { return mHeightTopDeformedUpValue; }
    public int getProgress() { return mProgress; }
    public float getScale() { return mScale; }
    public float getWidthDeformedValue() { return mWidthDeformedValue; }
    public void setDrawProgressScale(float value) { mDrawProgressScale = value; }
    public void setHeightBottomDeformedDownValue(float value) { mHeightBottomDeformedDownValue = value; }
    public void setHeightBottomDeformedUpValue(float value) { mHeightBottomDeformedUpValue = value; }
    public void setHeightTopDeformedDownValue(float value) { mHeightTopDeformedDownValue = value; }
    public void setHeightTopDeformedUpValue(float value) { mHeightTopDeformedUpValue = value; }
    public void setProgress(int progress) { mProgress = progress; }
    public void setScale(float scale) { mScale = scale; }
    public void setWidthDeformedValue(float value) { mWidthDeformedValue = value; }
}
