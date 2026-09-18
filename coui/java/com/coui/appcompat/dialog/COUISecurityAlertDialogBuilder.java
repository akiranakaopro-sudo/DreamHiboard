package com.coui.appcompat.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.coui.appcompat.R;
import com.coui.appcompat.checkbox.COUICheckBox;
import com.coui.appcompat.clickablespan.COUIClickableSpan;
import com.coui.appcompat.textutil.COUIChangeTextUtil;

public class COUISecurityAlertDialogBuilder extends COUIAlertDialogBuilder {
    private static final int DELAY = 70;

    private String mCheckBoxString;
    private final Context mContext;
    private AlertDialog mDialog;
    private boolean mHasCheckBox = true;
    private boolean mIsChecked;
    private boolean mIsShowStatementText;
    private int mLinkId;
    private DialogInterface.OnKeyListener mOnKeyListener;
    private OnLinkTextClickListener mOnLinkTextClickListener;
    private OnSelectedListener mOnSelectedListener;
    private int mStatementId;

    public interface OnLinkTextClickListener {
        void onLinkTextClick();
    }

    public interface OnSelectedListener {
        void onSelected(int which, boolean checked);
    }

    public COUISecurityAlertDialogBuilder(Context context) {
        super(context, R.style.COUIAlertDialog_Security_Bottom);
        mContext = context;
        init();
    }

    public COUISecurityAlertDialogBuilder(Context context, int themeResId) {
        super(context, themeResId, R.style.COUIAlertDialog_Security_Bottom);
        mContext = context;
        init();
    }

    public COUISecurityAlertDialogBuilder(Context context, int themeResId, int defStyleRes) {
        super(context, themeResId, defStyleRes);
        mContext = context;
        init();
    }

    private void init() {
        mCheckBoxString = mContext.getString(R.string.coui_security_alertdialog_checkbox_msg);
        mOnKeyListener = (dialog, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN
                    && mDialog != null && mDialog.isShowing() && mOnSelectedListener != null) {
                mOnSelectedListener.onSelected(DialogInterface.BUTTON_NEGATIVE, mIsChecked);
            }
            return false;
        };
    }

    private DialogInterface.OnClickListener getDefaultButtonClickListener() {
        return (dialog, which) -> {
            if (mOnSelectedListener != null) {
                mOnSelectedListener.onSelected(which, mIsChecked);
            }
        };
    }

    private SpannableStringBuilder getStatementStringBuilder(String text, int start, int length) {
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        COUIClickableSpan span = new COUIClickableSpan(mContext);
        span.setStatusBarClickListener(() -> {
            if (mOnLinkTextClickListener != null) {
                mOnLinkTextClickListener.onLinkTextClick();
            }
        });
        builder.setSpan(span, start, start + length, SpannableStringBuilder.SPAN_INCLUSIVE_EXCLUSIVE);
        return builder;
    }

    private View.OnTouchListener getStatementTextTouchListener(final int start, final int length) {
        return (view, event) -> {
            if (!(view instanceof TextView)) {
                return false;
            }
            int actionMasked = event.getActionMasked();
            int offset = ((TextView) view).getOffsetForPosition(event.getX(), event.getY());
            boolean outside = offset <= start || offset >= start + length;
            if (actionMasked == MotionEvent.ACTION_DOWN) {
                if (outside) {
                    return true;
                }
                view.setPressed(true);
                view.invalidate();
            } else if (actionMasked == MotionEvent.ACTION_UP
                    || actionMasked == MotionEvent.ACTION_CANCEL) {
                view.setPressed(false);
                view.postInvalidateDelayed(DELAY);
            }
            return false;
        };
    }

    private void initMessageText() {
        if (mDialog == null) {
            return;
        }
        View view = mDialog.findViewById(android.R.id.message);
        if (view instanceof TextView) {
            ((TextView) view).setTextSize(0, (int) COUIChangeTextUtil.getSuitableFontSize(
                    mContext.getResources().getDimensionPixelSize(
                            R.dimen.coui_alert_dialog_builder_message_text_size),
                    mContext.getResources().getConfiguration().fontScale, 5));
        }
    }

    private void initStatementText() {
        if (mDialog == null) {
            return;
        }
        TextView textView = mDialog.findViewById(R.id.coui_security_alertdialog_statement);
        if (textView == null) {
            return;
        }
        if (!mIsShowStatementText) {
            textView.setVisibility(View.GONE);
            return;
        }
        String link = mLinkId <= 0 ? mContext.getString(R.string.coui_security_alertdailog_privacy)
                : mContext.getString(mLinkId);
        String statement = mStatementId <= 0
                ? mContext.getString(R.string.coui_security_alertdailog_statement, link)
                : mContext.getString(mStatementId, link);
        int start = statement.indexOf(link);
        int length = link.length();
        textView.setVisibility(View.VISIBLE);
        textView.setHighlightColor(0);
        textView.setText(getStatementStringBuilder(statement, start, length));
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setOnTouchListener(getStatementTextTouchListener(start, length));
    }

    private void initCheckBox() {
        if (mDialog == null) {
            return;
        }
        View view = mDialog.findViewById(R.id.coui_security_alert_dialog_checkbox);
        if (view instanceof COUICheckBox) {
            if (!mHasCheckBox) {
                view.setVisibility(View.GONE);
                return;
            }
            view.setVisibility(View.VISIBLE);
            COUICheckBox checkBox = (COUICheckBox) view;
            checkBox.setChecked(mIsChecked);
            checkBox.setText(mCheckBoxString);
            checkBox.setTextSize(0, COUIChangeTextUtil.getSuitableFontSize(
                    mContext.getResources().getDimensionPixelSize(
                            R.dimen.coui_security_alert_dialog_checkbox_text_size),
                    mContext.getResources().getConfiguration().fontScale, 5));
            checkBox.setOnStateChangeListener((box, state) -> {
                mIsChecked = state == COUICheckBox.SELECT_ALL;
                if (mOnSelectedListener != null) {
                    mOnSelectedListener.onSelected(0, mIsChecked);
                }
            });
        }
    }

    private void initView() {
        initMessageText();
        initStatementText();
        initCheckBox();
    }

    @Override
    public AlertDialog create() {
        super.setOnKeyListener(mOnKeyListener);
        mDialog = super.create();
        return mDialog;
    }

    @Override
    public AlertDialog show() {
        mDialog = super.show();
        initView();
        return mDialog;
    }

    @Override
    public void updateViewAfterShown() {
        super.updateViewAfterShown();
        initView();
    }

    public COUISecurityAlertDialogBuilder setCheckBoxString(String text) {
        mCheckBoxString = text;
        return this;
    }

    public COUISecurityAlertDialogBuilder setCheckBoxString(int textId) {
        mCheckBoxString = mContext.getString(textId);
        return this;
    }

    public COUISecurityAlertDialogBuilder setChecked(boolean checked) {
        mIsChecked = checked;
        return this;
    }

    public COUISecurityAlertDialogBuilder setHasCheckBox(boolean hasCheckBox) {
        mHasCheckBox = hasCheckBox;
        return this;
    }

    public COUISecurityAlertDialogBuilder setNegativeString(String text) {
        super.setNegativeButton(text, getDefaultButtonClickListener());
        return this;
    }

    public COUISecurityAlertDialogBuilder setNegativeString(int textId) {
        super.setNegativeButton(textId, getDefaultButtonClickListener());
        return this;
    }

    public COUISecurityAlertDialogBuilder setPositiveString(String text) {
        super.setPositiveButton(text, getDefaultButtonClickListener());
        return this;
    }

    public COUISecurityAlertDialogBuilder setPositiveString(int textId) {
        super.setPositiveButton(textId, getDefaultButtonClickListener());
        return this;
    }

    public COUISecurityAlertDialogBuilder setOnLinkTextClickListener(
            OnLinkTextClickListener listener) {
        mOnLinkTextClickListener = listener;
        return this;
    }

    public COUISecurityAlertDialogBuilder setOnSelectedListener(OnSelectedListener listener) {
        mOnSelectedListener = listener;
        return this;
    }

    public COUISecurityAlertDialogBuilder setShowStatementText(boolean showStatementText) {
        mIsShowStatementText = showStatementText;
        return this;
    }

    public COUISecurityAlertDialogBuilder setStatementLinkString(int statementId, int linkId) {
        if (statementId <= 0) {
            mStatementId = -1;
        } else {
            String statement = mContext.getString(statementId);
            if (TextUtils.isEmpty(statement)
                    || !(statement.contains("%1$s") || statement.contains("%s"))) {
                mStatementId = -1;
            } else {
                mStatementId = statementId;
            }
        }
        mLinkId = linkId;
        return this;
    }

    @Override
    public COUISecurityAlertDialogBuilder setOnKeyListener(DialogInterface.OnKeyListener listener) {
        mOnKeyListener = listener;
        super.setOnKeyListener(listener);
        return this;
    }
}
