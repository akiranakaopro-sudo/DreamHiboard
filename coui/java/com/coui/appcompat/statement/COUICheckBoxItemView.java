package com.coui.appcompat.statement;

import com.coui.appcompat.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.coui.appcompat.checkbox.COUICheckBox;
import kotlin.jvm.internal.Intrinsics;


public final class COUICheckBoxItemView extends LinearLayout {
    private COUICheckBox checkBox;
    private final PrivacyItem privacyItem;


    public COUICheckBoxItemView(Context context, PrivacyItem privacyItem) {
        super(context);
        Intrinsics.checkNotNullParameter(context, "context");
        Intrinsics.checkNotNullParameter(privacyItem, "privacyItem");
        this.privacyItem = privacyItem;
        View viewInflate = LayoutInflater.from(context).inflate(R.layout.coui_component_layout_privacy_checkbox, this);
        ((TextView) viewInflate.findViewById(R.id.checkbox_title)).setText(privacyItem.getTitleText());
        ((TextView) viewInflate.findViewById(R.id.checkbox_summary)).setText(privacyItem.getSummaryText());
        View viewFindViewById = viewInflate.findViewById(R.id.checkbox);
        Intrinsics.checkNotNullExpressionValue(viewFindViewById, "findViewById(R.id.checkbox)");
        this.checkBox = (COUICheckBox) viewFindViewById;
        ((ConstraintLayout) viewInflate.findViewById(R.id.checkbox_layout)).setOnClickListener(new View.OnClickListener() {
            @Override
            public final void onClick(View view) {
                COUICheckBoxItemView.lambda$1$lambda$0(COUICheckBoxItemView.this, view);
            }
        });
    }


    public static final void lambda$1$lambda$0(COUICheckBoxItemView this$0, View view) {
        Intrinsics.checkNotNullParameter(this$0, "this$0");
        int state = this$0.checkBox.getState();
        if (state == 0) {
            this$0.checkBox.setState(2);
        } else {
            if (state != 2) {
                return;
            }
            this$0.checkBox.setState(0);
        }
    }

    public final PrivacyItem getPrivacyItem() {
        return this.privacyItem;
    }

    public final boolean isChecked() {
        return this.checkBox.getState() == 2;
    }

    public final void setOnStateChangeListener(COUICheckBox.OnStateChangeListener listener) {
        Intrinsics.checkNotNullParameter(listener, "listener");
        this.checkBox.setOnStateChangeListener(listener);
    }
}
