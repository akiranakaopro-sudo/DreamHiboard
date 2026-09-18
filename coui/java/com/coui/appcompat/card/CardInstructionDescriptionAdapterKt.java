package com.coui.appcompat.card;

import android.view.View;

final class CardInstructionDescriptionAdapterKt {
    private CardInstructionDescriptionAdapterKt() {
    }

    static int getDimenPx(View view, int dimenRes) {
        return view.getContext().getResources().getDimensionPixelSize(dimenRes);
    }
}
