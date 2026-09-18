package com.coui.appcompat.chip;

import android.widget.Checkable;

import androidx.annotation.IdRes;
import androidx.annotation.Nullable;

/**
 * Checkable contract used by {@link COUIChipGroup}.
 *
 * <p>Ported from the COUI chip module exposed by the decoded Settings sources.</p>
 */
public interface COUICheckable<T extends COUICheckable<T>> extends Checkable {
    interface OnCheckedChangeListener<C> {
        void onCheckedChanged(C checkable, boolean checked);
    }

    @IdRes
    int getId();

    void setInternalOnCheckedChangeListener(
            @Nullable OnCheckedChangeListener<T> listener);
}
