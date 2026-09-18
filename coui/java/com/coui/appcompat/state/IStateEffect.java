package com.coui.appcompat.state;

import android.content.Context;

public interface IStateEffect {
    void refresh(Context context);

    void reset();

    void setAnimateEnabled(boolean enabled);
}
