package com.coui.appcompat.card;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BaseDisplayInfo {
    private int animHeight;
    private final List<String> animTitles;
    private int animWidth;
    private final List<String> choices;
    private int selectedIndex;
    private CharSequence summary;
    private CharSequence title;

    public BaseDisplayInfo(CharSequence title, CharSequence summary) {
        if (title == null) {
            throw new NullPointerException("title");
        }
        if (summary == null) {
            throw new NullPointerException("summary");
        }
        this.title = title;
        this.summary = summary;
        this.choices = new ArrayList<>();
        this.animTitles = new ArrayList<>();
    }

    public final int getAnimHeight() {
        return animHeight;
    }

    public final List<String> getAnimTitles() {
        return animTitles;
    }

    public final int getAnimWidth() {
        return animWidth;
    }

    public final List<String> getChoices() {
        return choices;
    }

    public final int getSelectedIndex() {
        return selectedIndex;
    }

    public final CharSequence getSummary() {
        return summary;
    }

    public final CharSequence getTitle() {
        return title;
    }

    public final void setAnimHeight(int animHeight) {
        this.animHeight = animHeight;
    }

    public final void setAnimWidth(int animWidth) {
        this.animWidth = animWidth;
    }

    public final void setChoices(String[] choices) {
        if (choices == null) {
            throw new NullPointerException("choices");
        }
        this.choices.clear();
        Collections.addAll(this.choices, choices);
    }

    public final void setSelectedChoice(int selectedIndex) {
        this.selectedIndex = selectedIndex;
    }

    public final void setSelectedIndex(int selectedIndex) {
        this.selectedIndex = selectedIndex;
    }

    public final void setSummary(CharSequence summary) {
        if (summary == null) {
            throw new NullPointerException("<set-?>");
        }
        this.summary = summary;
    }

    public final void setTitle(CharSequence title) {
        if (title == null) {
            throw new NullPointerException("<set-?>");
        }
        this.title = title;
    }
}
