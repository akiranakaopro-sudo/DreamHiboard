package com.coui.appcompat.tablayout;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;


public abstract class COUIFragmentStatePagerAdapter extends FragmentStatePagerAdapter {
    public COUIFragmentStatePagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    public int getPageIcon(int index) {
        return 0;
    }
}






