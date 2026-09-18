package com.coui.appcompat.preference;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Point;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

import androidx.annotation.NonNull;
import androidx.preference.MultiSelectListPreferenceDialogFragmentCompat;

import com.coui.appcompat.R;
import com.coui.appcompat.dialog.COUIAlertDialogBuilder;
import com.coui.appcompat.dialog.adapter.ChoiceListAdapter;
import com.coui.appcompat.log.COUILog;
import com.coui.appcompat.uiutil.AnimLevel;
import com.coui.appcompat.uiutil.UIUtil;

import java.util.HashSet;
import java.util.Set;

public class COUIMultiSelectListPreferenceDialogFragment
        extends MultiSelectListPreferenceDialogFragmentCompat {
    private static final String SAVE_STATE_BLUR_ANIM_LAVEL = "ListPreferenceDialogFragment.SAVE_STATE_BLUR_ANIM_LAVEL";
    private static final String SAVE_STATE_BLUR_BACKGROUND = "ListPreferenceDialogFragment.SAVE_STATE_BLUR_BACKGROUND";
    private static final String SAVE_STATE_ENTRIES = "MultiSelectListPreferenceDialogFragmentCompat.entries";
    private static final String SAVE_STATE_ENTRY_VALUES = "MultiSelectListPreferenceDialogFragmentCompat.entryValues";
    private static final String SAVE_STATE_FOLLOWHAND = "ListPreferenceDialogFragment.SAVE_STATE_FOLLOWHAND";
    private static final String SAVE_STATE_MESSAGE = "COUIMultiSelectListPreferenceDialogFragment.message";
    private static final String SAVE_STATE_NEGATIVE_BUTTON_TEXT = "COUIMultiSelectListPreferenceDialogFragment.negativeButtonTextitle";
    private static final String SAVE_STATE_POSITIVE_BUTTON_TEXT = "COUIMultiSelectListPreferenceDialogFragment.positiveButtonText";
    private static final String SAVE_STATE_SUMMARYS = "COUIMultiSelectListPreferenceDialogFragment.summarys";
    private static final String SAVE_STATE_TITLE = "COUIMultiSelectListPreferenceDialogFragment.title";
    private static final String SAVE_STATE_VALUES = "COUIMultiSelectListPreferenceDialogFragment.values";
    private static final String TAG = "COUIMultiDialogFragment";

    private ChoiceListAdapter mAdapter;
    private boolean mBlurBackground = false;
    private AnimLevel mBlurMinAnimLevel = UIUtil.ANIM_LEVEL_SUPPORT_BLUR_MIN;
    private COUIAlertDialogBuilder mBuilder;
    private boolean[] mCheckboxStates;
    private CharSequence mDialogMessage;
    private CharSequence mDialogTitle;
    private CharSequence[] mEntries;
    private CharSequence[] mEntryValues;
    private boolean mIfFollowHand = true;
    private CharSequence mNegativeButtonText;
    private CharSequence mPositiveButtonText;
    private COUIMultiSelectListPreference mPreference;
    private CharSequence[] mSummaries;

    public static COUIMultiSelectListPreferenceDialogFragment newInstance(String key) {
        COUIMultiSelectListPreferenceDialogFragment fragment =
                new COUIMultiSelectListPreferenceDialogFragment();
        Bundle args = new Bundle(1);
        args.putString(ARG_KEY, key);
        fragment.setArguments(args);
        return fragment;
    }

    private boolean[] getCheckboxStatesFromValues(Set<String> values) {
        boolean[] states = new boolean[mEntries.length];
        for (int i = 0; i < mEntries.length; i++) {
            states[i] = values.contains(mEntries[i].toString());
        }
        return states;
    }

    private Set<String> getSelectedValues() {
        HashSet<String> selected = new HashSet<>();
        boolean[] states = mAdapter.getCheckBoxStates();
        for (int i = 0; i < states.length; i++) {
            if (i >= mEntryValues.length) {
                break;
            }
            if (states[i]) {
                selected.add(mEntryValues[i].toString());
            }
        }
        return selected;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null && getPreference() == null) {
            mDialogTitle = savedInstanceState.getString(SAVE_STATE_TITLE);
            mDialogMessage = savedInstanceState.getString(SAVE_STATE_MESSAGE);
            mEntries = savedInstanceState.getCharSequenceArray(SAVE_STATE_ENTRIES);
            mEntryValues = savedInstanceState.getCharSequenceArray(SAVE_STATE_ENTRY_VALUES);
            mSummaries = savedInstanceState.getCharSequenceArray(SAVE_STATE_SUMMARYS);
            mPositiveButtonText = savedInstanceState.getString(SAVE_STATE_POSITIVE_BUTTON_TEXT);
            mNegativeButtonText = savedInstanceState.getString(SAVE_STATE_NEGATIVE_BUTTON_TEXT);
            mCheckboxStates = savedInstanceState.getBooleanArray(SAVE_STATE_VALUES);
            mIfFollowHand = savedInstanceState.getBoolean(SAVE_STATE_FOLLOWHAND);
            mBlurBackground = savedInstanceState.getBoolean(SAVE_STATE_BLUR_BACKGROUND);
            mBlurMinAnimLevel = AnimLevel.valueOf(savedInstanceState.getInt(SAVE_STATE_BLUR_ANIM_LAVEL,
                    UIUtil.ANIM_LEVEL_SUPPORT_BLUR_MIN.getIntValue()));
            COUILog.e(TAG, "OnCreate, mPreference == null");
            return;
        }
        mPreference = (COUIMultiSelectListPreference) getPreference();
        if (mPreference.getEntries() == null || mPreference.getEntryValues() == null) {
            throw new IllegalStateException("MultiSelectListPreference requires an entries array and an entryValues array.");
        }
        mDialogTitle = mPreference.getDialogTitle();
        mDialogMessage = mPreference.getDialogMessage();
        mEntries = mPreference.getEntries();
        mEntryValues = mPreference.getEntryValues();
        mSummaries = mPreference.getSummaries();
        mPositiveButtonText = mPreference.getPositiveButtonText();
        mNegativeButtonText = mPreference.getNegativeButtonText();
        mCheckboxStates = getCheckboxStatesFromValues(mPreference.getValues());
        mIfFollowHand = mPreference.isIfFollowHand();
        mBlurBackground = mPreference.isBlurBackground();
        mBlurMinAnimLevel = mPreference.getBlurMinAnimLevel();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mAdapter = new ChoiceListAdapter(getContext(), R.layout.coui_select_dialog_multichoice,
                mEntries, mSummaries, mCheckboxStates, true) {
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
                .setAdapter((ListAdapter) mAdapter, (DialogInterface.OnClickListener) this)
                .setPositiveButton(mPositiveButtonText, (DialogInterface.OnClickListener) this)
                .setNegativeButton(mNegativeButtonText, (DialogInterface.OnClickListener) this)
                .setBlurBackgroundDrawable(mBlurBackground, mBlurMinAnimLevel);
        if (!mIfFollowHand) {
            return mBuilder.create();
        }
        Point lastTouchPoint = new Point();
        View preferenceView = null;
        if (mPreference != null) {
            preferenceView = mPreference.getPreferenceView();
            lastTouchPoint = mPreference.getLastTouchPoint();
        }
        return preferenceView == null ? mBuilder.create() : mBuilder.create(preferenceView, lastTouchPoint);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
            Set<String> selectedValues = getSelectedValues();
            if (getPreference() != null) {
                COUIMultiSelectListPreference preference =
                        (COUIMultiSelectListPreference) getPreference();
                if (preference.callChangeListener(selectedValues)) {
                    preference.setValues(selectedValues);
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBooleanArray(SAVE_STATE_VALUES, mAdapter.getCheckBoxStates());
        if (mDialogTitle != null) {
            outState.putString(SAVE_STATE_TITLE, String.valueOf(mDialogTitle));
        }
        if (mDialogMessage != null) {
            outState.putString(SAVE_STATE_MESSAGE, String.valueOf(mDialogMessage));
        }
        outState.putString(SAVE_STATE_POSITIVE_BUTTON_TEXT, String.valueOf(mPositiveButtonText));
        outState.putString(SAVE_STATE_NEGATIVE_BUTTON_TEXT, String.valueOf(mNegativeButtonText));
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
