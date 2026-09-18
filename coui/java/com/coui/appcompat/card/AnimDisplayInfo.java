package com.coui.appcompat.card;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class AnimDisplayInfo extends BaseDisplayInfo {
    private final List<String> animAssets;
    private final List<Integer> animResources;

    public AnimDisplayInfo() {
        this("", "");
    }

    public AnimDisplayInfo(CharSequence title, CharSequence summary) {
        super(title, summary);
        animResources = new ArrayList<>();
        animAssets = new ArrayList<>();
    }

    public AnimDisplayInfo(Integer[] animResources, String[] choices, int selectedIndex) {
        this("", "");
        addAll(this.animResources, animResources);
        setChoices(choices);
        setSelectedIndex(selectedIndex);
    }

    public AnimDisplayInfo(Integer[] animResources, CharSequence title, CharSequence summary) {
        this(title, summary);
        addAll(this.animResources, animResources);
    }

    public AnimDisplayInfo(Integer[] animResources, String[] animTitles) {
        this("", "");
        addAll(this.animResources, animResources);
        addAll(getAnimTitles(), animTitles);
    }

    public AnimDisplayInfo(String[] animAssets, String[] animTitles) {
        this("", "");
        addAll(this.animAssets, animAssets);
        addAll(getAnimTitles(), animTitles);
    }

    public AnimDisplayInfo(String[] animAssets, CharSequence title, CharSequence summary) {
        this(title, summary);
        addAll(this.animAssets, animAssets);
    }

    public AnimDisplayInfo(String[] animAssets, String[] choices, int selectedIndex) {
        this("", "");
        addAll(this.animAssets, animAssets);
        setChoices(choices);
        setSelectedIndex(selectedIndex);
    }

    public final List<String> getAnimAssets() {
        return animAssets;
    }

    public final List<Integer> getAnimResources() {
        return animResources;
    }

    private static <T> void addAll(List<T> out, T[] values) {
        if (values == null) {
            throw new NullPointerException("values");
        }
        out.clear();
        Collections.addAll(out, values);
    }
}
