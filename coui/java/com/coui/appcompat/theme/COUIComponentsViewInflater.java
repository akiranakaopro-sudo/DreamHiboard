package com.coui.appcompat.theme;

import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.app.AppCompatViewInflater;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;

import com.coui.appcompat.button.COUIButton;
import com.coui.appcompat.textview.COUITextView;

public class COUIComponentsViewInflater extends AppCompatViewInflater {
    @Override
    public AppCompatButton createButton(Context context, AttributeSet attrs) {
        return new COUIButton(context, attrs);
    }

    @Override
    public AppCompatTextView createTextView(Context context, AttributeSet attrs) {
        return new COUITextView(context, attrs);
    }
}
