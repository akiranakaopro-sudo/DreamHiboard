package com.coui.appcompat.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.preference.PreferenceViewHolder;

import com.coui.appcompat.R;
import com.coui.appcompat.cardlist.COUICardListHelper;
import com.coui.appcompat.cardlist.COUICardListSelectedItemLayout;
import com.coui.appcompat.edittext.COUIEditText;
import com.coui.appcompat.edittext.COUIInputView;
import com.coui.appcompat.edittext.COUIScrolledEditText;

public class COUIInputPreference extends COUIPreference {
    private CharSequence mContent;
    private COUIEditText mEditText;
    private COUICardListItemInputView mInputView;
    private View mPreferenceView;
    private CharSequence mTitle;

    public static class COUICardListItemInputView extends COUIInputView {
        boolean mJustShowFocusLine;

        public COUICardListItemInputView(Context context) {
            this(context, null);
        }

        public COUICardListItemInputView(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public COUICardListItemInputView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            mJustShowFocusLine = false;
        }

        @Override
        public int getEdittextPaddingBottom() {
            return !TextUtils.isEmpty(mTitle)
                    ? getResources().getDimensionPixelSize(R.dimen.coui_input_edit_error_text_has_title_padding_bottom)
                    : (int) getResources().getDimension(R.dimen.coui_input_edit_text_no_title_padding_bottom_inPreference);
        }

        @Override
        public int getEdittextPaddingTop() {
            return !TextUtils.isEmpty(mTitle)
                    ? getResources().getDimensionPixelSize(R.dimen.coui_input_edit_text_has_title_padding_top)
                    : (int) getResources().getDimension(R.dimen.coui_input_edit_text_no_title_padding_top_inPreference);
        }

        @Override
        public COUIEditText instanceCOUIEditText(Context context, AttributeSet attrs) {
            context.getTheme().applyStyle(R.style.COUIInputPreferenceTheme, true);
            COUIScrolledEditText editText = new COUIScrolledEditText(context, attrs, R.attr.couiInputPreferenceEditTextStyle);
            editText.setTextDirection(TEXT_DIRECTION_LOCALE);
            editText.setShowDeleteIcon(false);
            editText.setVerticalScrollBarEnabled(false);
            return editText;
        }

        @Override
        public boolean isIsCardSingleInput() {
            return true;
        }

        public void setJustShowFocusLine(boolean justShowFocusLine) {
            if (mJustShowFocusLine != justShowFocusLine) {
                mJustShowFocusLine = justShowFocusLine;
                COUIEditText editText = mEditText;
                if (editText != null) {
                    editText.setJustShowFocusLine(justShowFocusLine);
                }
            }
        }
    }

    public COUIInputPreference(Context context) {
        this(context, null);
    }

    public COUIInputPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.couiInputPreferenceStyle);
    }

    public COUIInputPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Preference_COUI_COUIInputPreference);
    }

    public COUIInputPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.COUIInputPreference, defStyleAttr, defStyleRes);
        mContent = a.getText(R.styleable.COUIInputPreference_couiContent);
        boolean justShowFocusLine = a.getBoolean(R.styleable.COUIInputPreference_couiJustShowFocusLine, true);
        a.recycle();
        TypedArray preferenceAttrs = context.obtainStyledAttributes(attrs, androidx.preference.R.styleable.Preference, defStyleAttr, defStyleRes);
        mTitle = preferenceAttrs.getText(androidx.preference.R.styleable.Preference_android_title);
        preferenceAttrs.recycle();
        mInputView = new COUICardListItemInputView(context, attrs);
        mInputView.setId(android.R.id.input);
        mInputView.setTitle(mTitle);
        mEditText = mInputView.getEditText();
        mInputView.setJustShowFocusLine(justShowFocusLine);
    }

    @Override
    public boolean drawDivider() {
        if (mEditText.isErrorState()) {
            return false;
        }
        return super.drawDivider();
    }

    public CharSequence getContent() {
        return mEditText != null ? mEditText.getCouiEditTexttNoEllipsisText() : mContent;
    }

    public COUIEditText getEditText() {
        return mEditText;
    }

    public CharSequence getHint() {
        return mInputView.getHint();
    }

    public COUIInputView getInputView() {
        return mInputView;
    }

    public View getPreferenceView() {
        return mPreferenceView;
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        mPreferenceView = holder.itemView;
        if (mPreferenceView instanceof COUICardListSelectedItemLayout) {
            ((COUICardListSelectedItemLayout) mPreferenceView).consumeDispatchingEventForState(true);
        }
        ViewGroup container = mPreferenceView.findViewById(R.id.edittext_container);
        if (container != null) {
            COUICardListItemInputView currentInputView =
                    (COUICardListItemInputView) container.findViewById(android.R.id.input);
            if (!mInputView.equals(currentInputView)) {
                mInputView.getEditText().refresh();
                ViewParent parent = mInputView.getParent();
                if (parent != null) {
                    ((ViewGroup) parent).removeView(mInputView);
                }
                container.removeAllViews();
                container.addView(mInputView, -1, -2);
                int positionInGroup = COUICardListHelper.getPositionInGroup(this);
                if (positionInGroup == COUICardListHelper.TAIL || positionInGroup == COUICardListHelper.FULL) {
                    mInputView.getEditText().setBoxBackgroundMode(3);
                }
            }
        }
        mInputView.setEnabled(isEnabled());
    }

    @Override
    public Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        setContent(savedState.mText);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            return superState;
        }
        SavedState savedState = new SavedState(superState);
        if (mContent != null) {
            savedState.mText = mContent.toString();
        }
        return savedState;
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        if (TextUtils.isEmpty(mContent)) {
            return;
        }
        setContent(getPersistedString(defaultValue == null ? mContent.toString() : (String) defaultValue));
    }

    public void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        if (TextUtils.isEmpty(mContent)) {
            return;
        }
        String value = restoreValue ? getPersistedString(mContent.toString()) : (String) defaultValue;
        setContent(value);
    }

    public void setContent(CharSequence content) {
        if (mEditText != null) {
            mEditText.setCouiEditTexttNoEllipsisText((String) content);
            mContent = content;
            return;
        }
        if (!TextUtils.equals(mContent, content)) {
            notifyChanged();
        }
        boolean wasBlocking = shouldDisableDependents();
        mContent = content;
        if (content != null) {
            persistString(content.toString());
        }
        boolean isBlocking = shouldDisableDependents();
        if (isBlocking != wasBlocking) {
            notifyDependencyChange(isBlocking);
        }
    }

    public void setHint(CharSequence hint) {
        CharSequence oldHint = getHint();
        if ((hint != null || oldHint == null) && (hint == null || hint.equals(oldHint))) {
            return;
        }
        mInputView.setHint(hint);
        notifyChanged();
    }

    @Override
    public boolean shouldDisableDependents() {
        return TextUtils.isEmpty(mContent) || super.shouldDisableDependents();
    }

    public static class SavedState extends android.preference.Preference.BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel source) {
                return new SavedState(source);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        String mText;

        public SavedState(Parcel source) {
            super(source);
            mText = source.readString();
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(mText);
        }
    }
}
