package com.coui.appcompat.edittext;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.coui.appcompat.R;

public class COUICardSingleInputView extends COUIInputView {
    public COUICardSingleInputView(Context context) {
        super(context);
    }

    public COUICardSingleInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public COUICardSingleInputView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public int getEdittextPaddingBottom() {
        return !TextUtils.isEmpty(mTitle)
                ? getResources().getDimensionPixelSize(
                R.dimen.coui_input_preference_single_title_padding_bottom)
                : (int) getResources().getDimension(
                R.dimen.coui_input_edit_text_no_title_padding_bottom);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.coui_single_input_card_view;
    }

    @Override
    public int getTitlePaddingTop() {
        return !TextUtils.isEmpty(mTitle)
                ? getResources().getDimensionPixelSize(
                R.dimen.coui_input_preference_single_title_padding_top)
                : getResources().getDimensionPixelSize(
                R.dimen.coui_input_preference_title_padding_top);
    }

    @Override
    public COUIEditText instanceCOUIEditText(Context context, AttributeSet attrs) {
        context.getTheme().applyStyle(R.style.COUICardSingleInputViewStyle, true);
        COUIEditText editText = new COUIEditText(context, attrs,
                R.attr.couiCardSingleInputEditTextStyle);
        editText.setShowDeleteIcon(false);
        editText.setVerticalScrollBarEnabled(false);
        return editText;
    }

    @Override
    public boolean isIsCardSingleInput() {
        return true;
    }
}
