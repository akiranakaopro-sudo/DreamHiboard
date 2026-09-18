package com.coui.appcompat.preference;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.DialogPreference;
import androidx.preference.EditTextPreferenceDialogFragmentCompat;

import com.coui.appcompat.dialog.COUIAlertDialogBuilder;
import com.coui.appcompat.edittext.COUIEditText;
import com.coui.appcompat.uiutil.AnimLevel;
import com.coui.appcompat.uiutil.UIUtil;

public class COUIEditTextPreferenceDialogFragment extends EditTextPreferenceDialogFragmentCompat {
    private static final String SAVE_STATE_BLUR_BACKGROUND = "ListPreferenceDialogFragment.SAVE_STATE_BLUR_BACKGROUND";
    private static final String SAVE_STATE_TEXT = "EditTextPreferenceDialogFragment.text";
    private static final String TAG = "COUIEditTextPreferenceDialogFragment";
    private boolean mBlurBackground = false;
    private AnimLevel mBlurMinAnimLevel = UIUtil.ANIM_LEVEL_SUPPORT_BLUR_MIN;
    private COUIEditText mEditText;

    private COUIAlertDialogBuilder initCOUIAlertDialogBuilder() {
        return new COUIAlertDialogBuilder(requireContext(), com.coui.appcompat.R.style.COUIAlertDialog_BottomAssignment)
                .setTitle(getPreference().getDialogTitle())
                .setMessage(getPreference().getDialogMessage())
                .setPositiveButton(getPreference().getPositiveButtonText(), this)
                .setNegativeButton(getPreference().getNegativeButtonText(), this);
    }

    public static COUIEditTextPreferenceDialogFragment newInstance(String key) {
        COUIEditTextPreferenceDialogFragment fragment = new COUIEditTextPreferenceDialogFragment();
        Bundle args = new Bundle(1);
        args.putString(ARG_KEY, key);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        FragmentActivity activity = getActivity();
        COUIAlertDialogBuilder builder = initCOUIAlertDialogBuilder();
        View contentView = onCreateDialogView(activity);
        if (contentView == null) {
            Log.d(TAG, "COUIEditTextPreferenceDialogFragment  contentView == null ");
            return builder.create();
        }

        mEditText = contentView.findViewById(android.R.id.edit);
        onBindDialogView(contentView);
        builder.setView(contentView);
        if (getPreference() != null) {
            onBindDialogView(contentView);
        }

        onPrepareDialogBuilder(builder);
        COUIEditTextPreference couiPreference = getCOUIEditTextPreference();
        if (couiPreference != null) {
            mBlurBackground = couiPreference.isBlurBackground();
            mBlurMinAnimLevel = couiPreference.getBlurMinAnimLevel();
        }
        AlertDialog dialog = builder.setBlurBackgroundDrawable(mBlurBackground, mBlurMinAnimLevel).create();
        boolean supportEmptyInput = couiPreference != null && couiPreference.isSupportEmptyInput();
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                Button positive = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                if (positive == null || supportEmptyInput) {
                    return;
                }
                positive.setEnabled(!TextUtils.isEmpty(editable));
            }
        });
        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mEditText != null) {
            mEditText.setFocusable(true);
            mEditText.requestFocus();
            Dialog dialog = getDialog();
            if (dialog != null && dialog.getWindow() != null) {
                dialog.getWindow().setSoftInputMode(5);
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mEditText != null) {
            outState.putCharSequence(SAVE_STATE_TEXT, mEditText.getText());
            outState.putBoolean(SAVE_STATE_BLUR_BACKGROUND, mBlurBackground);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getPreference() == null) {
            dismiss();
        }
    }

    private COUIEditTextPreference getCOUIEditTextPreference() {
        DialogPreference preference = getPreference();
        if (preference instanceof COUIEditTextPreference) {
            return (COUIEditTextPreference) preference;
        }
        return null;
    }
}
