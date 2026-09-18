package com.coui.appcompat.dialog;

import android.content.Context;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.coui.appcompat.R;

import java.util.List;

public class COUIGuideDialogBuilder {
    private AlertDialog mAlertDialog;
    private final COUIGuideLayoutContentView mContentView;
    private final Context mContext;
    private final COUIAlertDialogBuilder mCouiAlertDialogBuilder;

    public COUIGuideDialogBuilder(Context context) {
        this(context, R.style.COUIAlertDialog_Guide);
    }

    private COUIGuideDialogBuilder(Context context, int style) {
        mContext = context;
        mCouiAlertDialogBuilder = new COUIAlertDialogBuilder(context, style);
        mContentView = new COUIGuideLayoutContentView(context);
        mCouiAlertDialogBuilder.setView(mContentView);
    }

    public COUIGuideDialogBuilder addButtonClickListener(
            COUIGuideLayoutContentView.OnButtonClickListener listener) {
        mContentView.addButtonClickListener(listener);
        return this;
    }

    public COUIGuideDialogBuilder setButtonText(CharSequence skip, CharSequence next,
            CharSequence start) {
        mContentView.setButtonText(skip, next, start);
        return this;
    }

    public COUIGuideDialogBuilder setButtonText(CharSequence text) {
        mContentView.setButtonText(text);
        return this;
    }

    public COUIGuideDialogBuilder setGuidePages(List<COUIGuidePageItem> list) {
        mContentView.setGuidePages(list);
        return this;
    }

    public COUIGuideDialogBuilder setImageHeightStyle(
            COUIGuideLayoutContentView.ImageHeightStyle imageHeightStyle) {
        mContentView.setImageHeightStyle(imageHeightStyle);
        return this;
    }

    public COUIGuideDialogBuilder setImagePadding(int left, int top, int right, int bottom) {
        mContentView.setImagePadding(left, top, right, bottom);
        return this;
    }

    public AlertDialog show() {
        mAlertDialog = mCouiAlertDialogBuilder.create();
        mAlertDialog.setCanceledOnTouchOutside(false);
        mAlertDialog.show();
        mCouiAlertDialogBuilder.updateViewAfterShown();
        View custom = mAlertDialog.findViewById(android.R.id.custom);
        if (custom == null) {
            custom = mAlertDialog.findViewById(R.id.custom);
        }
        if (custom != null) {
            custom.setPadding(custom.getPaddingLeft(), 0, custom.getPaddingRight(),
                    custom.getPaddingBottom());
        }
        return mAlertDialog;
    }
}
