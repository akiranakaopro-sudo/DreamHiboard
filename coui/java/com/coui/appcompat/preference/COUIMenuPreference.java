package com.coui.appcompat.preference;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.coui.appcompat.R;
import com.coui.appcompat.log.COUILog;
import com.coui.appcompat.poplist.COUIClickSelectMenu;
import com.coui.appcompat.poplist.PopupListItem;
import com.coui.appcompat.poplist.PreciseClickHelper;
import com.coui.appcompat.uiutil.AnimLevel;
import com.coui.appcompat.uiutil.UIUtil;

import java.util.ArrayList;

public class COUIMenuPreference extends COUIPreference {
    private static final String TAG = "COUIMenuPreference";
    private AnimLevel mBlurMinAnimLevel;
    private COUIClickSelectMenu mCouiClickSelectMenu;
    private boolean mEnableAddExtraWdith;
    private CharSequence[] mEntries;
    private CharSequence[] mEntryValues;
    private int[] mGroupIds;
    private boolean mHelperEnabled;
    private final AdapterView.OnItemClickListener mItemClickListener;
    private PreciseClickHelper.OnPreciseClickListener mListener;
    private int mMaxShowItemCount;
    private int mPopInputMethod;
    private final ArrayList<PopupListItem> mPopupListItems;
    private boolean mReuseMenuWhenOffsetChanged;
    private ColorStateList mSelectItemColor;
    private String mSummary;
    private boolean mUseBackgroundBlur;
    private String mValue;
    private boolean mValueSet;

    public COUIMenuPreference(Context context) {
        this(context, null);
    }

    public COUIMenuPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.couiMenuPreferenceStyle);
    }

    public COUIMenuPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public COUIMenuPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
        mItemClickListener = (adapterView, view, position, id) -> {
            if (mEntryValues != null && position < mEntryValues.length && position >= 0) {
                if (callChangeListener(mEntryValues[position].toString())) {
                    setValue(mEntryValues[position].toString());
                }
            } else if (mEntryValues == null) {
                COUILog.e(TAG, "OnItemClick, mEntryValues is null");
            } else {
                COUILog.e(TAG, "OnItemClick, position is error:" + position + ",length:" + mEntryValues.length);
            }
            mCouiClickSelectMenu.dismiss();
        };
        mPopupListItems = new ArrayList<>();
        mHelperEnabled = true;
        mEnableAddExtraWdith = true;
        mMaxShowItemCount = -1;
        mUseBackgroundBlur = false;
        mBlurMinAnimLevel = UIUtil.ANIM_LEVEL_SUPPORT_BLUR_MIN;
        mReuseMenuWhenOffsetChanged = false;

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.COUIMenuPreference, defStyleAttr, 0);
        mEntryValues = a.getTextArray(R.styleable.COUIMenuPreference_android_entryValues);
        mEntries = a.getTextArray(R.styleable.COUIMenuPreference_android_entries);
        mMaxShowItemCount = a.getInteger(R.styleable.COUIMenuPreference_maxShowItemCount, -1);
        mPopInputMethod = a.getInt(R.styleable.COUIMenuPreference_popInputMethod, 0);
        int groupIdsRes = a.getResourceId(R.styleable.COUIMenuPreference_groupIds, 0);
        if (groupIdsRes != 0) {
            mGroupIds = context.getResources().getIntArray(groupIdsRes);
        }
        mValue = a.getString(R.styleable.COUIMenuPreference_android_value);
        a.recycle();
        setEntryValues(mEntryValues);
        setEntries(mEntries);
        setValue(mValue);
    }

    private int getValueIndex() {
        return findIndexOfValue(mValue);
    }

    public int findIndexOfValue(String value) {
        if (value == null || mEntryValues == null) {
            return 0;
        }
        for (int i = mEntryValues.length - 1; i >= 0; i--) {
            if (!TextUtils.isEmpty(mEntryValues[i]) && mEntryValues[i].equals(value)) {
                return i;
            }
        }
        return 0;
    }

    public CharSequence[] getEntries() {
        return mEntries;
    }

    public CharSequence[] getEntryValues() {
        return mEntryValues;
    }

    @Override
    public CharSequence getSummary() {
        if (getSummaryProvider() != null) {
            return super.getSummary();
        }
        String value = getValue();
        CharSequence summary = super.getSummary();
        if (mSummary == null) {
            return summary;
        }
        String formatted = String.format(mSummary, value == null ? "" : value);
        if (TextUtils.equals(formatted, summary)) {
            return summary;
        }
        Log.w(TAG, "Setting a summary with a String formatting marker is no longer supported. You should use a SummaryProvider instead.");
        return formatted;
    }

    public String getValue() {
        return mValue;
    }

    @Override
    public void onBindViewHolder(final PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        if (mCouiClickSelectMenu == null) {
            mCouiClickSelectMenu = new COUIClickSelectMenu(getContext(), holder.itemView);
        }
        setReusePopup(mReuseMenuWhenOffsetChanged);
        mCouiClickSelectMenu.getPopup().setUseBackgroundBlur(mUseBackgroundBlur, mBlurMinAnimLevel);
        mCouiClickSelectMenu.getPopup().setInputMethodMode(mPopInputMethod);
        if (mSelectItemColor != null) {
            mCouiClickSelectMenu.registerForClickSelectItems(holder.itemView, mPopupListItems,
                    mSelectItemColor.getDefaultColor());
        } else {
            mCouiClickSelectMenu.registerForClickSelectItems(holder.itemView, mPopupListItems);
        }
        mCouiClickSelectMenu.setEnableAddExtraWidth(mEnableAddExtraWdith);
        mCouiClickSelectMenu.setHelperEnabled(mHelperEnabled);
        if (mListener != null) {
            mCouiClickSelectMenu.setOnPreciseClickListener(mListener);
        }
        mCouiClickSelectMenu.setOnItemClickListener(mItemClickListener);
        mCouiClickSelectMenu.setMaxShowItemCount(mMaxShowItemCount);
        holder.itemView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override public void onViewAttachedToWindow(View view) {}

            @Override
            public void onViewDetachedFromWindow(View view) {
                holder.itemView.removeOnAttachStateChangeListener(this);
                if (mCouiClickSelectMenu != null && mCouiClickSelectMenu.getPopup().isShowing()) {
                    mCouiClickSelectMenu.getPopup().dismiss();
                }
            }
        });
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        setValue(getPersistedString((String) defaultValue));
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            return superState;
        }
        SavedState state = new SavedState(superState);
        state.mValue = getValue();
        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        if (!mValueSet) {
            setValue(savedState.mValue);
        }
    }

    public void setBlurMinAnimLevel(AnimLevel animLevel) {
        mBlurMinAnimLevel = animLevel;
        if (mCouiClickSelectMenu != null) {
            mCouiClickSelectMenu.getPopup().setUseBackgroundBlur(mUseBackgroundBlur, mBlurMinAnimLevel);
        }
    }

    public void setEnableAddExtraWidth(boolean enable) {
        mEnableAddExtraWdith = enable;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        setMenuShowEnabled(enabled);
    }

    public void setEntries(CharSequence[] entries) {
        mEntries = entries;
        mValueSet = false;
        if (entries == null || entries.length <= 0) {
            return;
        }
        mPopupListItems.clear();
        PopupListItem.Builder builder = new PopupListItem.Builder();
        for (int i = 0; i < entries.length; i++) {
            builder.reset().setTitle((String) entries[i]).setGroupId(mGroupIds != null ? mGroupIds[i] : -1);
            mPopupListItems.add(builder.build());
        }
    }

    public void setEntries(int entriesResId) {
        setEntries(getContext().getResources().getTextArray(entriesResId));
    }

    public void setEntryValues(CharSequence[] entryValues) {
        mEntryValues = entryValues;
        mValueSet = false;
        if (mEntries != null || entryValues == null || entryValues.length <= 0) {
            return;
        }
        mPopupListItems.clear();
        PopupListItem.Builder builder = new PopupListItem.Builder();
        for (int i = 0; i < entryValues.length; i++) {
            builder.reset().setTitle((String) entryValues[i]).setGroupId(mGroupIds != null ? mGroupIds[i] : -1);
            mPopupListItems.add(builder.build());
        }
    }

    public void setEntryValues(int entryValuesResId) {
        setEntryValues(getContext().getResources().getTextArray(entryValuesResId));
    }

    public void setMaxShowItemCount(int maxShowItemCount) {
        if (mMaxShowItemCount != maxShowItemCount) {
            mMaxShowItemCount = maxShowItemCount;
            notifyChanged();
        }
    }

    public void setMenuItemSelectColor(ColorStateList color) {
        mSelectItemColor = color;
    }

    public void setMenuShow(boolean show) {
        if (mCouiClickSelectMenu != null) {
            mCouiClickSelectMenu.setPopupState(show);
        }
    }

    public void setMenuShowEnabled(boolean enabled) {
        mHelperEnabled = enabled;
        if (mCouiClickSelectMenu != null) {
            mCouiClickSelectMenu.setHelperEnabled(enabled);
        }
    }

    @Override
    public void setOnPreciseClickListener(PreciseClickHelper.OnPreciseClickListener listener) {
        mListener = listener;
    }

    public void setPopInputMethod(int mode) {
        mPopInputMethod = mode;
        if (mCouiClickSelectMenu != null) {
            mCouiClickSelectMenu.getPopup().setInputMethodMode(mPopInputMethod);
        }
    }

    public void setPopupList(ArrayList<PopupListItem> items) {
        mPopupListItems.clear();
        mPopupListItems.addAll(items);
    }

    public void setReusePopup(boolean reuse) {
        mReuseMenuWhenOffsetChanged = reuse;
        if (mCouiClickSelectMenu != null) {
            mCouiClickSelectMenu.getPopup().reuseMenuWhenOffsetChanged(reuse);
        }
    }

    @Override
    public void setSummary(CharSequence summary) {
        super.setSummary(summary);
        if (summary == null && mSummary != null) {
            mSummary = null;
        } else if (summary != null && !summary.equals(mSummary)) {
            mSummary = summary.toString();
        }
    }

    public void setUseBackgroundBlur(boolean useBackgroundBlur) {
        mUseBackgroundBlur = useBackgroundBlur;
        if (mCouiClickSelectMenu != null) {
            mCouiClickSelectMenu.getPopup().setUseBackgroundBlur(mUseBackgroundBlur, mBlurMinAnimLevel);
        }
    }

    public void setValue(String value) {
        if (!TextUtils.equals(mValue, value) || !mValueSet) {
            mValue = value;
            mValueSet = true;
            if (mPopupListItems.size() > 0 && !TextUtils.isEmpty(value)) {
                for (PopupListItem item : mPopupListItems) {
                    String title = item.getTitle();
                    CharSequence expected = mEntries != null ? mEntries[findIndexOfValue(value)] : value;
                    item.setChecked(TextUtils.equals(title, expected));
                }
            }
            persistString(value);
            notifyChanged();
        }
    }

    public void setValueIndex(int index) {
        if (mEntryValues != null) {
            setValue(mEntryValues[index].toString());
        }
    }

    public static class SavedState extends Preference.BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            @Override public SavedState createFromParcel(Parcel in) { return new SavedState(in); }
            @Override public SavedState[] newArray(int size) { return new SavedState[size]; }
        };
        String mValue;

        public SavedState(Parcel source) {
            super(source);
            mValue = source.readString();
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(mValue);
        }
    }
}
