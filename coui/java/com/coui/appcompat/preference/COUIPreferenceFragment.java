package com.coui.appcompat.preference;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import androidx.recyclerview.widget.COUIRecyclerView;
import androidx.recyclerview.widget.RecyclerView;

import com.coui.appcompat.R;
import com.coui.appcompat.darkmode.COUIDarkModeUtil;

public class COUIPreferenceFragment extends PreferenceFragmentCompat {
    private static final String DIALOG_FRAGMENT_TAG = "androidx.preference.PreferenceFragment.DIALOG";

    private boolean mEnableInternalDivider = true;
    private COUIPreferenceItemDecoration mPreferenceItemDecoration;

    public COUIPreferenceItemDecoration getItemDecoration() {
        return mPreferenceItemDecoration;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    }

    @Override
    public RecyclerView onCreateRecyclerView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        COUIRecyclerView recyclerView = (COUIRecyclerView) inflater.inflate(
                R.layout.coui_preference_recyclerview,
                parent,
                false
        );
        recyclerView.setEnablePointerDownAction(false);
        recyclerView.setLayoutManager(onCreateLayoutManager());
        COUIDarkModeUtil.setForceDarkAllow(recyclerView, false);
        return recyclerView;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        setDivider(null);
        setDividerHeight(0);
        return view;
    }

    @Override
    public void onDestroyView() {
        if (mPreferenceItemDecoration != null) {
            mPreferenceItemDecoration.onDestroy();
        }
        super.onDestroyView();
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager != null
                && fragmentManager.findFragmentByTag(DIALOG_FRAGMENT_TAG) != null) {
            return;
        }
        DialogFragment fragment;
        if (preference instanceof COUIActivityDialogPreference) {
            fragment = COUIActivityDialogFragment.newInstance(preference.getKey());
        } else if (preference instanceof COUIEditTextPreference) {
            fragment = COUIEditTextPreferenceDialogFragment.newInstance(preference.getKey());
        } else if (preference instanceof COUIMultiSelectListPreference) {
            fragment = COUIMultiSelectListPreferenceDialogFragment.newInstance(preference.getKey());
        } else if (preference instanceof ListPreference) {
            fragment = COUIListPreferenceDialogFragment.newInstance(preference.getKey());
        } else {
            super.onDisplayPreferenceDialog(preference);
            return;
        }
        fragment.setTargetFragment(this, 0);
        fragment.show(getFragmentManager(), DIALOG_FRAGMENT_TAG);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getListView() == null || mPreferenceItemDecoration == null || !mEnableInternalDivider) {
            return;
        }
        getListView().removeItemDecoration(mPreferenceItemDecoration);
        if (mPreferenceItemDecoration.getPreferenceScreen() == null) {
            mPreferenceItemDecoration = new COUIPreferenceItemDecoration(getContext(), getPreferenceScreen());
        }
        getListView().addItemDecoration(mPreferenceItemDecoration);
    }

    public void setEnableCOUIPreferenceDivider(boolean enable) {
        mEnableInternalDivider = enable;
        if (!enable) {
            if (getListView() != null) {
                getListView().removeItemDecoration(mPreferenceItemDecoration);
            }
        } else if (getListView() != null && mPreferenceItemDecoration != null) {
            getListView().removeItemDecoration(mPreferenceItemDecoration);
            if (mPreferenceItemDecoration.getPreferenceScreen() == null) {
                mPreferenceItemDecoration = new COUIPreferenceItemDecoration(getContext(), getPreferenceScreen());
            }
            getListView().addItemDecoration(mPreferenceItemDecoration);
        }
    }

    @Override
    public void setPreferenceScreen(PreferenceScreen preferenceScreen) {
        if (preferenceScreen == getPreferenceScreen()) {
            return;
        }
        super.setPreferenceScreen(preferenceScreen);
        if (mPreferenceItemDecoration != null && getListView() != null) {
            getListView().removeItemDecoration(mPreferenceItemDecoration);
        }
        mPreferenceItemDecoration = new COUIPreferenceItemDecoration(getContext(), preferenceScreen);
        if (getListView() != null && mEnableInternalDivider) {
            getListView().addItemDecoration(mPreferenceItemDecoration);
        }
    }
}
