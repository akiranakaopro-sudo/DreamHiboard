package com.coui.appcompat.card;

import java.util.List;

public final class ImageDisplayInfo extends BaseDisplayInfo {
    private final Integer[] imageResources;

    public ImageDisplayInfo(Integer[] imageResources, CharSequence title, CharSequence summary) {
        super(title, summary);
        if (imageResources == null) {
            throw new NullPointerException("imageResources");
        }
        this.imageResources = imageResources;
    }

    public ImageDisplayInfo(Integer[] imageResources, String[] choices) {
        this(imageResources, "", "");
        setChoices(choices);
    }

    public ImageDisplayInfo(Integer[] imageResources, List<String> animTitles) {
        this(imageResources, "", "");
        if (animTitles == null) {
            throw new NullPointerException("animTitles");
        }
        getAnimTitles().clear();
        getAnimTitles().addAll(animTitles);
    }

    public final Integer[] getImageResources() {
        return imageResources;
    }
}
