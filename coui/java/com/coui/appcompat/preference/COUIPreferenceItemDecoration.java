package com.coui.appcompat.preference;

import android.content.Context;
import android.view.View;

import androidx.preference.Preference;
import androidx.preference.PreferenceGroupAdapter;
import androidx.preference.PreferenceScreen;
import androidx.recyclerview.widget.COUIRecyclerView;
import androidx.recyclerview.widget.RecyclerView;

// Leapy modified 2026-07-22: Restore OPPO's COUIDividerItemDecoration API.
public class COUIPreferenceItemDecoration extends COUIRecyclerView.COUIDividerItemDecoration {
    private final int[] mChildLocation;
    private final int[] mItemLocation;
    private PreferenceScreen mPreferenceScreen;

    public COUIPreferenceItemDecoration(Context context, PreferenceScreen preferenceScreen) {
        super(context);
        mItemLocation = new int[2];
        mChildLocation = new int[2];
        mPreferenceScreen = preferenceScreen;
    }

    @Override
    public int getDividerInsetEnd(RecyclerView recyclerView, int position) {
        int left;
        int right;
        if (mPreferenceScreen == null) {
            return super.getDividerInsetEnd(recyclerView, position);
        }
        RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
        if (adapter instanceof PreferenceGroupAdapter) {
            View child = recyclerView.getChildAt(position);
            Preference preference = ((PreferenceGroupAdapter) adapter).getItem(
                    recyclerView.getChildAdapterPosition(child)
            );
            if (preference instanceof COUIRecyclerView.ICOUIDividerDecorationInterface) {
                boolean rtl = child.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
                COUIRecyclerView.ICOUIDividerDecorationInterface dividerPreference = (COUIRecyclerView.ICOUIDividerDecorationInterface) preference;
                View alignView = dividerPreference.getDividerEndAlignView();
                if (alignView == null) {
                    return dividerPreference.getDividerEndInset();
                }
                child.getLocationInWindow(mItemLocation);
                alignView.getLocationInWindow(mChildLocation);
                if (rtl) {
                    left = mChildLocation[0] + alignView.getPaddingEnd();
                    right = mItemLocation[0];
                } else {
                    left = mItemLocation[0] + child.getWidth();
                    right = (mChildLocation[0] + alignView.getWidth()) - alignView.getPaddingEnd();
                }
                return left - right;
            }
        }
        return super.getDividerInsetStart(recyclerView, position);
    }

    @Override
    public int getDividerInsetStart(RecyclerView recyclerView, int position) {
        int left;
        int right;
        if (mPreferenceScreen == null) {
            return super.getDividerInsetStart(recyclerView, position);
        }
        RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
        if (adapter instanceof PreferenceGroupAdapter) {
            View child = recyclerView.getChildAt(position);
            Preference preference = ((PreferenceGroupAdapter) adapter).getItem(
                    recyclerView.getChildAdapterPosition(child)
            );
            if (preference instanceof COUIRecyclerView.ICOUIDividerDecorationInterface) {
                boolean rtl = child.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
                COUIRecyclerView.ICOUIDividerDecorationInterface dividerPreference = (COUIRecyclerView.ICOUIDividerDecorationInterface) preference;
                View alignView = dividerPreference.getDividerStartAlignView();
                if (alignView == null) {
                    return dividerPreference.getDividerStartInset();
                }
                child.getLocationInWindow(mItemLocation);
                alignView.getLocationInWindow(mChildLocation);
                if (rtl) {
                    left = mItemLocation[0] + child.getWidth();
                    right = (mChildLocation[0] + alignView.getWidth()) - alignView.getPaddingStart();
                } else {
                    left = mChildLocation[0] + alignView.getPaddingStart();
                    right = mItemLocation[0];
                }
                return left - right;
            }
        }
        return super.getDividerInsetStart(recyclerView, position);
    }

    public PreferenceScreen getPreferenceScreen() {
        return mPreferenceScreen;
    }

    public void onDestroy() {
        mPreferenceScreen = null;
    }

    @Override
    public boolean shouldDrawDivider(RecyclerView recyclerView, int position) {
        if (mPreferenceScreen == null) {
            return false;
        }
        RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
        if (adapter instanceof PreferenceGroupAdapter) {
            View child = recyclerView.getChildAt(position);
            Preference preference = ((PreferenceGroupAdapter) adapter).getItem(
                    recyclerView.getChildAdapterPosition(child)
            );
            if (preference instanceof COUIRecyclerView.ICOUIDividerDecorationInterface) {
                return ((COUIRecyclerView.ICOUIDividerDecorationInterface) preference).drawDivider();
            }
        }
        return false;
    }
}
