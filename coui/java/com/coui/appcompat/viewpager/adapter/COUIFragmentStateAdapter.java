package com.coui.appcompat.viewpager.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public abstract class COUIFragmentStateAdapter extends FragmentStateAdapter {
    public COUIFragmentStateAdapter(FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    public COUIFragmentStateAdapter(Fragment fragment) {
        super(fragment);
    }

    public COUIFragmentStateAdapter(FragmentManager fragmentManager, Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    public static abstract class DataSetChangeObserver extends RecyclerView.AdapterDataObserver {
        @Override
        public abstract void onChanged();

        @Override public final void onItemRangeChanged(int positionStart, int itemCount) { onChanged(); }
        @Override public final void onItemRangeChanged(int positionStart, int itemCount, Object payload) { onChanged(); }
        @Override public final void onItemRangeInserted(int positionStart, int itemCount) { onChanged(); }
        @Override public final void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) { onChanged(); }
        @Override public final void onItemRangeRemoved(int positionStart, int itemCount) { onChanged(); }
    }

}
