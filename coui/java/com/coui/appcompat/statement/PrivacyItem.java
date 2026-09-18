package com.coui.appcompat.statement;

import android.content.Context;
import kotlin.jvm.internal.Intrinsics;


public final class PrivacyItem {
    private final String summaryText;
    private final String titleText;

    public PrivacyItem(String titleText, String summaryText) {
        Intrinsics.checkNotNullParameter(titleText, "titleText");
        Intrinsics.checkNotNullParameter(summaryText, "summaryText");
        this.titleText = titleText;
        this.summaryText = summaryText;
    }

    public static PrivacyItem copy$default(PrivacyItem privacyItem, String str, String text, int index, Object obj) {
        if ((index & 1) != 0) {
            str = privacyItem.titleText;
        }
        if ((index & 2) != 0) {
            text = privacyItem.summaryText;
        }
        return privacyItem.copy(str, text);
    }

    public final String component1() {
        return this.titleText;
    }

    public final String component2() {
        return this.summaryText;
    }

    public final PrivacyItem copy(String titleText, String summaryText) {
        Intrinsics.checkNotNullParameter(titleText, "titleText");
        Intrinsics.checkNotNullParameter(summaryText, "summaryText");
        return new PrivacyItem(titleText, summaryText);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PrivacyItem)) {
            return false;
        }
        PrivacyItem privacyItem = (PrivacyItem) obj;
        return Intrinsics.areEqual(this.titleText, privacyItem.titleText) && Intrinsics.areEqual(this.summaryText, privacyItem.summaryText);
    }

    public final String getSummaryText() {
        return this.summaryText;
    }

    public final String getTitleText() {
        return this.titleText;
    }

    public int hashCode() {
        return (this.titleText.hashCode() * 31) + this.summaryText.hashCode();
    }

    public String toString() {
        return "PrivacyItem(titleText=" + this.titleText + ", summaryText=" + this.summaryText + ')';
    }

    public PrivacyItem(Context context, int index, int index_2) {
        this(context.getString(index), context.getString(index_2));
        Intrinsics.checkNotNullParameter(context, "context");
    }
}
