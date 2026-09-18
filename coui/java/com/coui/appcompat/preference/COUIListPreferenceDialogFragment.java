package com.coui.appcompat.preference;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Point;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

import androidx.annotation.NonNull;
import androidx.preference.ListPreferenceDialogFragmentCompat;

import com.coui.appcompat.R;
import com.coui.appcompat.dialog.COUIAlertDialogBuilder;
import com.coui.appcompat.dialog.adapter.ChoiceListAdapter;
import com.coui.appcompat.log.COUILog;
import com.coui.appcompat.uiutil.AnimLevel;
import com.coui.appcompat.uiutil.UIUtil;

public class COUIListPreferenceDialogFragment extends ListPreferenceDialogFragmentCompat {
    private static final String SAVE_STATE_BLUR_ANIM_LAVEL = "ListPreferenceDialogFragment.SAVE_STATE_BLUR_ANIM_LAVEL";
    private static final String SAVE_STATE_BLUR_BACKGROUND = "ListPreferenceDialogFragment.SAVE_STATE_BLUR_BACKGROUND";
    private static final String SAVE_STATE_ENTRIES = "ListPreferenceDialogFragment.entries";
    private static final String SAVE_STATE_ENTRY_VALUES = "ListPreferenceDialogFragment.entryValues";
    private static final String SAVE_STATE_FOLLOWHAND = "ListPreferenceDialogFragment.SAVE_STATE_FOLLOWHAND";
    private static final String SAVE_STATE_INDEX = "COUIListPreferenceDialogFragment.index";
    private static final String SAVE_STATE_MESSAGE = "COUIListPreferenceDialogFragment.message";
    private static final String SAVE_STATE_SUMMARYS = "COUListPreferenceDialogFragment.summarys";
    private static final String SAVE_STATE_TITLE = "COUIListPreferenceDialogFragment.title";
    private static final String TAG = "COUIListDialogFragment";

    private boolean mBlurBackground = false;
    private AnimLevel mBlurMinAnimLevel = UIUtil.ANIM_LEVEL_SUPPORT_BLUR_MIN;
    private COUIAlertDialogBuilder mBuilder;
    private int mClickedDialogEntryIndex = -1;
    private CharSequence mDialogMessage;
    private CharSequence mDialogTitle;
    private CharSequence[] mEntries;
    private CharSequence[] mEntryValues;
    private boolean mIfFollowHand = true;
    private COUIListPreference mPreference;
    private CharSequence[] mSummaries;

    public static COUIListPreferenceDialogFragment newInstance(String key) {
        COUIListPreferenceDialogFragment fragment = new COUIListPreferenceDialogFragment();
        Bundle args = new Bundle(1);
        args.putString(ARG_KEY, key);
        fragment.setArguments(args);
        return fragment;
    }

    private void saveSelectedIndex() {
        int index = mClickedDialogEntryIndex;
        if (index >= 0 && index < mEntryValues.length) {
            String value = mEntryValues[index].toString();
            if (getPreference() != null) {
                COUIListPreference preference = (COUIListPreference) getPreference();
                if (preference.callChangeListener(value)) {
                    preference.setValue(value);
                }
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null && getPreference() == null) {
            mClickedDialogEntryIndex = savedInstanceState.getInt(SAVE_STATE_INDEX, -1);
            mDialogTitle = savedInstanceState.getString(SAVE_STATE_TITLE);
            mDialogMessage = savedInstanceState.getString(SAVE_STATE_MESSAGE);
            mEntries = savedInstanceState.getCharSequenceArray(SAVE_STATE_ENTRIES);
            mEntryValues = savedInstanceState.getCharSequenceArray(SAVE_STATE_ENTRY_VALUES);
            mSummaries = savedInstanceState.getCharSequenceArray(SAVE_STATE_SUMMARYS);
            mIfFollowHand = savedInstanceState.getBoolean(SAVE_STATE_FOLLOWHAND);
            mBlurBackground = savedInstanceState.getBoolean(SAVE_STATE_BLUR_BACKGROUND);
            mBlurMinAnimLevel = AnimLevel.valueOf(savedInstanceState.getInt(SAVE_STATE_BLUR_ANIM_LAVEL,
                    UIUtil.ANIM_LEVEL_SUPPORT_BLUR_MIN.getIntValue()));
            COUILog.e(TAG, "OnCreate, mPreference == null");
            return;
        }
        mPreference = (COUIListPreference) getPreference();
        if (mPreference.getEntries() == null || mPreference.getEntryValues() == null) {
            throw new IllegalStateException("ListPreference requires an entries array and an entryValues array.");
        }
        mDialogTitle = mPreference.getDialogTitle();
        mDialogMessage = mPreference.getDialogMessage();
        mSummaries = mPreference.getSummaries();
        mClickedDialogEntryIndex = mPreference.findIndexOfValue(mPreference.getValue());
        mEntries = mPreference.getEntries();
        mEntryValues = mPreference.getEntryValues();
        mIfFollowHand = mPreference.isIfFollowHand();
        mBlurBackground = mPreference.isBlurBackground();
        mBlurMinAnimLevel = mPreference.getBlurMinAnimLevel();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        boolean[] checkedItems = null;
        if (mEntries != null && mClickedDialogEntryIndex >= 0 && mClickedDialogEntryIndex < mEntries.length) {
            checkedItems = new boolean[mEntries.length];
            checkedItems[mClickedDialogEntryIndex] = true;
        }
        ChoiceListAdapter adapter = new ChoiceListAdapter(getContext(), R.layout.coui_select_dialog_singlechoice,
                mEntries, mSummaries, checkedItems, false) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                View divider = view.findViewById(R.id.item_divider);
                int count = getCount();
                if (divider != null) {
                    divider.setVisibility(count == 1 || position == count - 1 ? View.GONE : View.VISIBLE);
                }
                return view;
            }
        };
        mBuilder = new COUIAlertDialogBuilder(requireContext(), R.style.COUIAlertDialog_BottomAssignment)
                .setTitle(mDialogTitle)
                .setMessage(mDialogMessage)
                .setNegativeButton(R.string.dialog_cancel, null)
                .setBlurBackgroundDrawable(mBlurBackground, mBlurMinAnimLevel)
                .setAdapter((ListAdapter) adapter, (dialog, which) -> {
                    mClickedDialogEntryIndex = which;
                    onClick(dialog, DialogInterface.BUTTON_POSITIVE);
                    dialog.dismiss();
                });

        if (!mIfFollowHand) {
            return mBuilder.create();
        }
        Point point = new Point();
        View preferenceView = null;
        if (mPreference != null) {
            preferenceView = mPreference.getPreferenceView();
            point = mPreference.getLastTouchPoint();
        }
        return preferenceView == null ? mBuilder.create() : mBuilder.create(preferenceView, point);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult && mEntries != null) {
            saveSelectedIndex();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SAVE_STATE_INDEX, mClickedDialogEntryIndex);
        if (mDialogTitle != null) {
            outState.putString(SAVE_STATE_TITLE, String.valueOf(mDialogTitle));
        }
        if (mDialogMessage != null) {
            outState.putString(SAVE_STATE_MESSAGE, String.valueOf(mDialogMessage));
        }
        outState.putCharSequenceArray(SAVE_STATE_SUMMARYS, mSummaries);
        outState.putBoolean(SAVE_STATE_FOLLOWHAND, mIfFollowHand);
        outState.putBoolean(SAVE_STATE_BLUR_BACKGROUND, mBlurBackground);
        outState.putInt(SAVE_STATE_BLUR_ANIM_LAVEL, mBlurMinAnimLevel.getIntValue());
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getPreference() == null) {
            dismiss();
            return;
        }
        if (mBuilder != null) {
            mBuilder.updateViewAfterShown();
        }
    }
}
