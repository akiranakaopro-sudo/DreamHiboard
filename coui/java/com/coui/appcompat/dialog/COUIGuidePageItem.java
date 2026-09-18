package com.coui.appcompat.dialog;

public class COUIGuidePageItem {
    private final CharSequence mDescription;
    private final int mImageResId;
    private final CharSequence mTitle;

    public COUIGuidePageItem(int imageResId, CharSequence title, CharSequence description) {
        mImageResId = imageResId;
        mTitle = title;
        mDescription = description;
    }

    public CharSequence getDescription() {
        return mDescription;
    }

    public int getImageResId() {
        return mImageResId;
    }

    public CharSequence getTitle() {
        return mTitle;
    }
}
