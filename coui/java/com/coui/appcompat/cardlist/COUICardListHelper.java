package com.coui.appcompat.cardlist;

import android.view.View;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;

import com.coui.appcompat.list.ConfigurationChangedListener;
import com.coui.appcompat.preference.COUICardSupportInterface;
import com.coui.appcompat.preference.ListSelectedItemLayout;

import java.util.ArrayList;

public class COUICardListHelper {
    public static final int FULL = 4;
    public static final int HEAD = 1;
    public static final int MIDDLE = 2;
    public static final int NONE = 0;
    public static final int TAIL = 3;

    public static int getPositionInGroup(int count, int position) {
        if (count == 1) {
            return FULL;
        }
        if (position == 0) {
            return HEAD;
        }
        return position == count - 1 ? TAIL : MIDDLE;
    }

    private static boolean isSupportCard(PreferenceGroup group, Preference preference) {
        return group instanceof PreferenceScreen
                ? preference instanceof COUICardSupportInterface
                && ((COUICardSupportInterface) preference).isSupportCardUse()
                : !(preference instanceof PreferenceCategory);
    }

    public static void refreshCardBg(View view, int color) {
        if (view instanceof ListSelectedItemLayout) {
            ((ListSelectedItemLayout) view).refreshCardBg(color);
        }
    }

    public static void setConfigurationChangeListener(View view, ConfigurationChangedListener listener) {
        if (view instanceof ListSelectedItemLayout) {
            ((ListSelectedItemLayout) view).setConfigurationChangeListener(listener);
        }
    }

    public static void setItemCardBackground(View view, int positionInGroup) {
        if (view instanceof ListSelectedItemLayout) {
            ((ListSelectedItemLayout) view).setPositionInGroup(positionInGroup);
        }
    }

    public static int getPositionInGroup(Preference preference) {
        PreferenceGroup parent = preference.getParent();
        int index = 0;
        if (parent == null) {
            return NONE;
        }
        ArrayList<Preference> visiblePreferences = new ArrayList<>();
        for (int i = 0; i < parent.getPreferenceCount(); i++) {
            Preference item = parent.getPreference(i);
            if (item.isVisible()) {
                visiblePreferences.add(item);
            }
        }
        int size = visiblePreferences.size();
        for (int i = 0; i < size; i++) {
            if (preference == visiblePreferences.get(i)) {
                index = i;
                break;
            }
        }
        Preference previous = index > 0 ? visiblePreferences.get(index - 1) : null;
        Preference next = index < size - 1 ? visiblePreferences.get(index + 1) : null;
        int start = previous == null || !isSupportCard(parent, previous) ? HEAD : MIDDLE;
        return next == null || !isSupportCard(parent, next) ? start == HEAD ? FULL : TAIL : start;
    }
}
