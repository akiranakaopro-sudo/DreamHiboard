package com.coui.appcompat.poplist;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

public class PopupListItem {
    public static final int MENU_GROUP_ITEM_ACTIVATED_IN_MAIN = 1;
    public static final int MENU_GROUP_ITEM_ACTIVATED_IN_SUB = 2;
    public static final int MENU_GROUP_ITEM_INACTIVE = 0;
    public static final int MENU_HINT_TYPE_CUSTOM = 1;
    public static final int MENU_HINT_TYPE_NONE = -1;
    public static final int MENU_HINT_TYPE_RED_DOT = 0;
    public static final int MENU_ITEM_FORCE_TINT_ALL = 7;
    public static final int MENU_ITEM_FORCE_TINT_ICON = 1;
    public static final int MENU_ITEM_FORCE_TINT_NONE = 0;
    public static final int MENU_ITEM_FORCE_TINT_STATE_ICON = 4;
    public static final int MENU_ITEM_FORCE_TINT_TITLE = 2;
    public static final int MENU_ITEM_TYPE_ALERT = 1;
    public static final int MENU_ITEM_TYPE_CUSTOM = 2;
    public static final int MENU_ITEM_TYPE_DEFAULT = 0;
    public static final int MENU_ITEM_TYPE_HEADER = 3;

    private View mCustomHintView;
    private View mCustomItemView;
    private String mDescription;
    private String mDescriptionContentDescription;
    private int mForceTint;
    private int mGroupId;
    private int mGroupState;
    private int mHintType;
    private Drawable mIcon;
    private int mIconId;
    private int mId;
    private boolean mIsChecked;
    private boolean mIsEnable;
    private int mItemType;
    private int mRedDotAmount;
    private String mRedDotText;
    private Drawable mStateIcon;
    private int mStateIconId;
    private ArrayList<PopupListItem> mSubMenuItemList;
    private String mTitle;
    private ColorStateList mTitleColor;
    private String mTitleContentDescription;

    public static class Builder {
        private String mDescription;
        private String mDescriptionContentDescription;
        private String mRedDotText;
        private String mTitle;
        private String mTitleContentDescription;
        private int mId = -1;
        private int mIconId = 0;
        private int mGroupId = 0;
        private int mStateIconId = 0;
        private int mRedDotAmount = -1;
        private int mForceTint = MENU_ITEM_FORCE_TINT_ALL;
        private int mItemType = MENU_ITEM_TYPE_DEFAULT;
        private int mHintType = -1;
        private boolean mIsEnable = true;
        private boolean mIsChecked = false;
        private Drawable mIcon = null;
        private Drawable mStateIcon = null;
        private ColorStateList mTitleColor = null;
        private ArrayList<PopupListItem> mSubMenuItemList = null;
        private View mCustomHintView = null;
        private View mCustomItemView = null;

        public PopupListItem build() {
            PopupListItem item = new PopupListItem();
            item.apply(this);
            return item;
        }

        public Builder reset() {
            mId = -1;
            mIconId = 0;
            mIcon = null;
            mIsEnable = true;
            mTitle = null;
            mDescription = null;
            mItemType = MENU_ITEM_TYPE_DEFAULT;
            mTitleColor = null;
            mIsChecked = false;
            mStateIconId = 0;
            mStateIcon = null;
            mHintType = -1;
            mRedDotText = null;
            mRedDotAmount = -1;
            mForceTint = MENU_ITEM_FORCE_TINT_ALL;
            mCustomHintView = null;
            mGroupId = 0;
            mSubMenuItemList = null;
            mCustomItemView = null;
            return this;
        }

        public Builder setCustomHintView(View view) { mCustomHintView = view; return this; }
        public Builder setCustomItemView(View view) { mCustomItemView = view; return this; }
        public Builder setDescription(String description) { mDescription = description; return this; }
        public Builder setDescriptionContentDescription(String description) { mDescriptionContentDescription = description; return this; }
        public Builder setForceTint(int forceTint) { mForceTint = forceTint; return this; }
        public Builder setGroupId(int groupId) { mGroupId = groupId; return this; }
        @Deprecated public Builder setHasSubArray(boolean hasSubArray) { return this; }
        public Builder setHintType(int hintType) { mHintType = hintType; return this; }
        public Builder setIcon(Drawable icon) { mIcon = icon; return this; }
        public Builder setIconId(int iconId) { mIconId = iconId; return this; }
        public Builder setId(int id) { mId = id; return this; }
        @Deprecated public Builder setIsCheckable(boolean isCheckable) { return this; }
        public Builder setIsChecked(boolean isChecked) { mIsChecked = isChecked; return this; }
        @Deprecated public Builder setIsClickSubArray(boolean isClickSubArray) { return this; }
        public Builder setIsEnable(boolean isEnable) { mIsEnable = isEnable; return this; }
        @Deprecated public Builder setItemColorSpecial(int itemColorSpecial) { return this; }
        public Builder setItemType(int itemType) { mItemType = itemType; return this; }
        @Deprecated public Builder setOperateIcon(Drawable icon) { mStateIcon = icon; return this; }
        public Builder setRedDotAmount(int amount) { mRedDotAmount = amount; return this; }
        public Builder setRedDotText(String text) { mRedDotText = text; return this; }
        public Builder setStateIcon(Drawable icon) { mStateIcon = icon; return this; }
        public Builder setStateIconId(int stateIconId) { mStateIconId = stateIconId; return this; }
        @Deprecated public Builder setSubArray(ArrayList<PopupListItem> items) { mSubMenuItemList = items; return this; }
        public Builder setSubMenuItemList(ArrayList<PopupListItem> items) { mSubMenuItemList = items; return this; }
        public Builder setTitle(String title) { mTitle = title; return this; }
        @Deprecated public Builder setTitleColorInt(int color) { return this; }
        public Builder setTitleColorList(ColorStateList color) { mTitleColor = color; return this; }
        public Builder setTitleContentDescription(String description) { mTitleContentDescription = description; return this; }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface PopupMenuGroupState {}

    @Retention(RetentionPolicy.SOURCE)
    public @interface PopupMenuItemHintType {}

    @Retention(RetentionPolicy.SOURCE)
    public @interface PopupMenuItemType {}

    public PopupListItem() {
        mId = -1;
        mGroupId = 0;
        mItemType = MENU_ITEM_TYPE_DEFAULT;
        mHintType = -1;
        mGroupState = 0;
        mForceTint = MENU_ITEM_FORCE_TINT_ALL;
        mRedDotAmount = -1;
        mIconId = 0;
        mStateIconId = 0;
    }

    private void apply(Builder builder) {
        mId = builder.mId;
        mIconId = builder.mIconId;
        mIcon = builder.mIcon;
        mIsEnable = builder.mIsEnable;
        mTitle = builder.mTitle;
        mTitleContentDescription = builder.mTitleContentDescription;
        mDescriptionContentDescription = builder.mDescriptionContentDescription;
        mDescription = builder.mDescription;
        mItemType = builder.mItemType;
        mIsChecked = builder.mIsChecked;
        mStateIconId = builder.mStateIconId;
        mStateIcon = builder.mStateIcon;
        mHintType = builder.mHintType;
        mRedDotText = builder.mRedDotText;
        mRedDotAmount = builder.mRedDotAmount;
        mForceTint = builder.mForceTint;
        mTitleColor = builder.mTitleColor;
        if (mTitleColor != null) {
            mForceTint &= -3;
        }
        if (mHintType == 1) {
            mCustomHintView = builder.mCustomHintView;
            builder.mCustomHintView = null;
        }
        mGroupId = builder.mGroupId;
        if (builder.mSubMenuItemList != null) {
            mSubMenuItemList = builder.mSubMenuItemList;
            builder.mSubMenuItemList = null;
        }
        mCustomItemView = builder.mCustomItemView;
    }

    public View getCustomHintView() { return mCustomHintView; }
    public View getCustomItemView() { return mCustomItemView; }
    public String getDescription() { return mDescription; }
    public String getDescriptionContentDescription() { return mDescriptionContentDescription; }
    public int getForceTint() { return mForceTint; }
    public int getGroupId() { return mGroupId; }
    public int getGroupState() { return mGroupState; }
    public int getHintType() { return mHintType; }
    public Drawable getIcon() { return mIcon; }
    public int getIconId() { return mIconId; }
    public int getId() { return mId; }
    @Deprecated public int getItemColorSpecial() { return -1; }
    public int getItemType() { return mItemType; }
    @Deprecated public Drawable getOperateIcon() { return mStateIcon; }
    public int getRedDotAmount() { return mRedDotAmount; }
    public String getRedDotText() { return mRedDotText; }
    public Drawable getStateIcon() { return mStateIcon; }
    public int getStateIconId() { return mStateIconId; }
    @Deprecated public ArrayList<PopupListItem> getSubArray() { return mSubMenuItemList; }
    public ArrayList<PopupListItem> getSubMenuItemList() { return mSubMenuItemList; }
    public String getTitle() { return mTitle; }
    @Deprecated public int getTitleColorInt() { return -1; }
    public ColorStateList getTitleColorList() { return mTitleColor; }
    public String getTitleContentDescription() { return mTitleContentDescription; }
    @Deprecated public boolean hasSubArray() { return hasSubMenu(); }
    public boolean hasSubMenu() { return mSubMenuItemList != null && !mSubMenuItemList.isEmpty(); }
    @Deprecated public boolean isCheckable() { return true; }
    public boolean isChecked() { return mIsChecked; }
    public boolean isEnable() { return mIsEnable; }
    @Deprecated public void setCheckable(boolean checkable) {}
    public void setChecked(boolean checked) { mIsChecked = checked; }
    public void setCustomHintView(View view) { mCustomHintView = view; }
    public void setCustomItemView(View view) { mCustomItemView = view; }
    public void setDescription(String description) { mDescription = description; }
    public void setDescriptionContentDescription(String description) { mTitleContentDescription = description; }
    public void setEnable(boolean enable) { mIsEnable = enable; }
    public void setForceTint(int forceTint) { mForceTint = forceTint; }
    public void setGroupId(int groupId) { mGroupId = groupId; }
    public void setGroupState(int groupState) { mGroupState = groupState; }
    @Deprecated public void setHasSubArray(boolean hasSubArray) {}
    public void setHintType(int hintType) { mHintType = hintType; }
    public void setIcon(Drawable icon) { mIcon = icon; }
    public void setIconId(int iconId) { mIconId = iconId; }
    public void setId(int id) { mId = id; }
    @Deprecated public void setItemColorSpecial(int color) {}
    public void setItemType(int itemType) { mItemType = itemType; }
    @Deprecated public void setOperateIcon(Drawable icon) { mStateIcon = icon; }
    public void setRedDotAmount(int amount) { mRedDotAmount = amount; }
    public void setRedDotText(String text) { mRedDotText = text; }
    public void setStateIcon(Drawable icon) { mStateIcon = icon; }
    public void setStateIconId(int stateIconId) { mStateIconId = stateIconId; }
    @Deprecated public void setSubArray(ArrayList<PopupListItem> items) { mSubMenuItemList = items; }
    public void setSubMenuItemList(ArrayList<PopupListItem> items) { mSubMenuItemList = items; }
    public void setTitle(String title) { mTitle = title; }
    @Deprecated public void setTitleColorInt(int color) {}
    public void setTitleColorList(ColorStateList color) { mTitleColor = color; }
    public void setTitleContentDescription(String description) { mTitleContentDescription = description; }

    @Deprecated public PopupListItem(String title, boolean enable) { this((Drawable) null, title, enable); }
    @Deprecated public PopupListItem(int iconId, String title, boolean enable) {
        this();
        mIconId = iconId;
        mTitle = title;
        mIsEnable = enable;
    }
    @Deprecated public PopupListItem(String title, boolean enable, int groupId) {
        this((Drawable) null, title, enable);
        mGroupId = groupId;
    }
    @Deprecated public PopupListItem(Drawable icon, String title, boolean enable) {
        this(icon, title, enable, -1);
    }
    @Deprecated public PopupListItem(Drawable icon, String title, boolean enable, int redDotAmount) {
        this(icon, title, false, false, redDotAmount, enable);
    }
    @Deprecated public PopupListItem(Drawable icon, String title, boolean checked, boolean hasSub) {
        this(icon, title, checked, false, hasSub);
    }
    @Deprecated public PopupListItem(Drawable icon, String title, boolean checked, boolean sub, boolean enable) {
        this(icon, title, checked, sub, -1, enable);
    }
    @Deprecated public PopupListItem(Drawable icon, String title, boolean checked, boolean sub,
            int redDotAmount, boolean enable) {
        this(icon, title, checked, sub, redDotAmount, enable, null);
    }
    @Deprecated public PopupListItem(Drawable icon, String title, boolean checked, boolean sub,
            int redDotAmount, boolean enable, ArrayList<PopupListItem> items) {
        this(icon, title, checked, sub, redDotAmount, enable, items, null);
    }
    @Deprecated public PopupListItem(Drawable icon, String title, boolean checked, boolean sub,
            int redDotAmount, boolean enable, ArrayList<PopupListItem> items, String redDotText) {
        this(icon, title, checked, sub, redDotAmount, enable, items, redDotText, null);
    }
    @Deprecated public PopupListItem(Drawable icon, String title, boolean checked, boolean sub,
            int redDotAmount, boolean enable, ArrayList<PopupListItem> items, String redDotText,
            Drawable stateIcon) {
        this(icon, title, checked, sub, redDotAmount, enable, items, redDotText, stateIcon, -1);
    }
    @Deprecated public PopupListItem(Drawable icon, String title, boolean checked, boolean sub,
            int redDotAmount, boolean enable, ArrayList<PopupListItem> items, String redDotText,
            Drawable stateIcon, int iconId) {
        this(icon, title, checked, sub, redDotAmount, enable, items, redDotText, stateIcon, iconId, -1);
    }
    @Deprecated public PopupListItem(Drawable icon, String title, boolean checked, boolean sub,
            int redDotAmount, boolean enable, ArrayList<PopupListItem> items, String redDotText,
            Drawable stateIcon, int iconId, int groupId) {
        this();
        mIcon = icon;
        mTitle = title;
        mIsChecked = sub;
        mIsEnable = enable;
        mRedDotAmount = redDotAmount;
        mSubMenuItemList = items;
        mRedDotText = redDotText;
        mStateIcon = stateIcon;
        mGroupId = groupId;
    }
}
