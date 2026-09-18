package com.coui.appcompat.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import com.airbnb.lottie.LottieAnimationView;

import com.coui.appcompat.R;
import com.coui.appcompat.textutil.COUIChangeTextUtil;

public class COUIRotatingDialogBuilder {
    private AlertDialog mAlertDialog;
    private int mAnimationViewMaxSize;
    private boolean mBlurBackgroundWindow;
    private String mCancelButton;
    private DialogInterface.OnClickListener mCancelClickListener;
    private final Context mContext;
    private String mDialogTitle;
    private DialogInterface.OnDismissListener mDismissListener;
    private String mFileName;
    private final String mMessageText;
    private TextView mMessageView;
    private int mRawResource = -1;
    private int mRepeatCount = -1;
    // Leapy modified 2026-07-26: Match OPPO's JSON spinner output with the
    // real Lottie renderer and remove the synthetic Effective spinner.
    private LottieAnimationView mRotatingAnimationView;
    private COUIAlertDialogBuilder mRotatingDialogBuilder;
    private DialogInterface.OnShowListener mShowListener;
    private int mStyle;

    public COUIRotatingDialogBuilder(Context context) {
        this(context, null);
    }

    public COUIRotatingDialogBuilder(Context context, String messageText) {
        mContext = context;
        mMessageText = messageText;
        mStyle = 0;
        mBlurBackgroundWindow = false;
        mAnimationViewMaxSize = context.getResources()
                .getDimensionPixelSize(R.dimen.coui_spinner_loading_height);
    }

    private void addOnWindowAttachListener(AlertDialog dialog) {
        View decorView = dialog.getWindow().getDecorView();
        mRotatingAnimationView = decorView.findViewById(R.id.progress);
        mMessageView = decorView.findViewById(R.id.progress_tips);
        if (mMessageView != null && mMessageText != null) {
            mMessageView.setText(mMessageText);
            COUIChangeTextUtil.adaptFontSize(mMessageView, 4);
        }
        if (mRotatingAnimationView != null) {
            if (mRawResource != -1 && mFileName != null) {
                throw new IllegalArgumentException(
                        "mRawResource and mFileName cannot be used at the same time. Please use only one at once.");
            }
            if (mRawResource != -1) {
                mRotatingAnimationView.setAnimation(mRawResource);
                if (!hasButton()) {
                    setLayoutParamsMaxSize(mRotatingAnimationView);
                }
            } else if (mFileName != null) {
                mRotatingAnimationView.setAnimation(mFileName);
                if (!hasButton()) {
                    setLayoutParamsMaxSize(mRotatingAnimationView);
                }
            }
        }
    }

    private boolean hasButton() {
        return mCancelButton != null;
    }

    public AlertDialog create() {
        if (mRotatingDialogBuilder == null) {
            if (mStyle == 0) {
                mStyle = mCancelButton == null
                        ? R.style.COUIAlertDialog_Rotating
                        : R.style.COUIAlertDialog_Rotating_Cancelable;
            }
            mRotatingDialogBuilder = new COUIAlertDialogBuilder(mContext, mStyle);
            if (mCancelButton != null) {
                mRotatingDialogBuilder.setNegativeButton(mCancelButton, mCancelClickListener);
            }
            if (mDialogTitle != null) {
                mRotatingDialogBuilder.setTitle(mDialogTitle);
            }
            mRotatingDialogBuilder.setBlurBackgroundDrawable(mBlurBackgroundWindow);
            mAlertDialog = mRotatingDialogBuilder.create();
            mAlertDialog.setOnShowListener(dialog -> {
                if (mRotatingAnimationView != null) {
                    mRotatingAnimationView.setRepeatCount(mRepeatCount);
                    mRotatingAnimationView.playAnimation();
                }
                if (mShowListener != null) {
                    mShowListener.onShow(dialog);
                }
            });
            mAlertDialog.setOnDismissListener(dialog -> {
                if (mRotatingAnimationView != null) {
                    mRotatingAnimationView.pauseAnimation();
                }
                if (mDismissListener != null) {
                    mDismissListener.onDismiss(dialog);
                }
            });
        }
        return mAlertDialog;
    }

    public AlertDialog show() {
        AlertDialog dialog = create();
        dialog.show();
        mRotatingDialogBuilder.updateViewAfterShown();
        addOnWindowAttachListener(dialog);
        return dialog;
    }

    public LottieAnimationView getAnimationView() {
        return mRotatingAnimationView;
    }

    public TextView getMessageView() {
        return mMessageView;
    }

    public COUIRotatingDialogBuilder setBlurBackgroundWindow(boolean blurBackgroundWindow) {
        mBlurBackgroundWindow = blurBackgroundWindow;
        return this;
    }

    public COUIRotatingDialogBuilder setCancelButton(String text,
            DialogInterface.OnClickListener listener) {
        mCancelButton = text;
        mCancelClickListener = listener;
        return this;
    }

    public COUIRotatingDialogBuilder setCancelButton(int textId,
            DialogInterface.OnClickListener listener) {
        return setCancelButton(mContext.getString(textId), listener);
    }

    public COUIRotatingDialogBuilder setDialogTitle(String title) {
        mDialogTitle = title;
        return this;
    }

    public COUIRotatingDialogBuilder setDialogTitle(int titleId) {
        return setDialogTitle(mContext.getString(titleId));
    }

    public COUIRotatingDialogBuilder setDismissListener(
            DialogInterface.OnDismissListener listener) {
        mDismissListener = listener;
        return this;
    }

    public COUIRotatingDialogBuilder setFileName(String fileName) {
        mFileName = fileName;
        return this;
    }

    public COUIRotatingDialogBuilder setLayoutParamsMaxSize(View view) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.width = mAnimationViewMaxSize;
        layoutParams.height = mAnimationViewMaxSize;
        view.setLayoutParams(layoutParams);
        return this;
    }

    @Deprecated
    public COUIRotatingDialogBuilder setLayoutParamsWrapContent(View view) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        view.setLayoutParams(layoutParams);
        return this;
    }

    public COUIRotatingDialogBuilder setRawResource(int rawResource) {
        mRawResource = rawResource;
        return this;
    }

    public COUIRotatingDialogBuilder setRepeatCount(int repeatCount) {
        mRepeatCount = repeatCount;
        return this;
    }

    public COUIRotatingDialogBuilder setShowListener(DialogInterface.OnShowListener listener) {
        mShowListener = listener;
        return this;
    }

    public void setStyle(int style) {
        mStyle = style;
    }
}
