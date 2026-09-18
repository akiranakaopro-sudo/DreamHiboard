package com.coui.appcompat.button.listener;

import android.view.View;

public interface OnTextChangeListener {
    void onTextChanged(View view, CharSequence text, int start, int before, int count);
}
