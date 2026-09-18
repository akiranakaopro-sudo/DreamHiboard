package com.coui.appcompat.chip;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Selection coordinator matching the decoded COUI chip module contract. */
final class COUICheckableGroup<T extends COUICheckable<T>> {
    interface OnCheckedStateChangeListener {
        void onCheckedStateChanged(@NonNull Set<Integer> checkedIds);
    }

    private final Map<Integer, T> mCheckables = new HashMap<>();
    private final Set<Integer> mCheckedIds = new HashSet<>();
    private OnCheckedStateChangeListener mListener;
    private boolean mSelectionRequired;
    private boolean mSingleSelection;

    void addCheckable(@NonNull T checkable) {
        mCheckables.put(checkable.getId(), checkable);
        if (checkable.isChecked()) {
            checkInternal(checkable);
        }
        checkable.setInternalOnCheckedChangeListener((item, checked) -> {
            boolean changed = checked
                    ? checkInternal(item)
                    : uncheckInternal(item, mSelectionRequired);
            if (changed) {
                notifyChanged();
            }
        });
    }

    void removeCheckable(@NonNull T checkable) {
        checkable.setInternalOnCheckedChangeListener(null);
        mCheckables.remove(checkable.getId());
        mCheckedIds.remove(checkable.getId());
    }

    void check(@IdRes int id) {
        T checkable = mCheckables.get(id);
        if (checkable != null && checkInternal(checkable)) {
            notifyChanged();
        }
    }

    void uncheck(@IdRes int id) {
        T checkable = mCheckables.get(id);
        if (checkable != null && uncheckInternal(checkable, mSelectionRequired)) {
            notifyChanged();
        }
    }

    void clearCheck() {
        boolean changed = !mCheckedIds.isEmpty();
        for (T checkable : mCheckables.values()) {
            uncheckInternal(checkable, false);
        }
        if (changed) {
            notifyChanged();
        }
    }

    @NonNull
    Set<Integer> getCheckedIds() {
        return new HashSet<>(mCheckedIds);
    }

    @NonNull
    List<Integer> getCheckedIdsSortedByChildOrder(@NonNull ViewGroup parent) {
        Set<Integer> checkedIds = getCheckedIds();
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (child instanceof COUICheckable && checkedIds.contains(child.getId())) {
                result.add(child.getId());
            }
        }
        return result;
    }

    @IdRes
    int getSingleCheckedId() {
        if (!mSingleSelection || mCheckedIds.isEmpty()) {
            return View.NO_ID;
        }
        return mCheckedIds.iterator().next();
    }

    boolean isSelectionRequired() {
        return mSelectionRequired;
    }

    boolean isSingleSelection() {
        return mSingleSelection;
    }

    void setSelectionRequired(boolean required) {
        mSelectionRequired = required;
    }

    void setSingleSelection(boolean singleSelection) {
        if (mSingleSelection != singleSelection) {
            mSingleSelection = singleSelection;
            clearCheck();
        }
    }

    void setOnCheckedStateChangeListener(@Nullable OnCheckedStateChangeListener listener) {
        mListener = listener;
    }

    private boolean checkInternal(@NonNull T checkable) {
        int id = checkable.getId();
        if (mCheckedIds.contains(id)) {
            return false;
        }
        if (mSingleSelection) {
            T previous = mCheckables.get(getSingleCheckedId());
            if (previous != null) {
                uncheckInternal(previous, false);
            }
        }
        boolean changed = mCheckedIds.add(id);
        if (!checkable.isChecked()) {
            checkable.setChecked(true);
        }
        return changed;
    }

    private boolean uncheckInternal(@NonNull T checkable, boolean enforceSelection) {
        int id = checkable.getId();
        if (!mCheckedIds.contains(id)) {
            return false;
        }
        if (enforceSelection && mCheckedIds.size() == 1) {
            checkable.setChecked(true);
            return false;
        }
        boolean changed = mCheckedIds.remove(id);
        if (checkable.isChecked()) {
            checkable.setChecked(false);
        }
        return changed;
    }

    private void notifyChanged() {
        if (mListener != null) {
            mListener.onCheckedStateChanged(getCheckedIds());
        }
    }
}
