package com.coui.appcompat.list;

public interface ICardListSelectedItem {
    boolean isCardType();

    void refreshCardBg(int color);

    void setConfigurationChangeListener(ConfigurationChangedListener listener);

    void setPositionInGroup(int position);
}
