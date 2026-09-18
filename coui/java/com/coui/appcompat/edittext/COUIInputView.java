package com.coui.appcompat.edittext;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.PathInterpolator;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.coui.appcompat.R;
import com.coui.appcompat.animation.COUIEaseInterpolator;
import com.coui.appcompat.contextutil.COUIContextUtil;
import com.coui.appcompat.uiutil.UIUtil;

import java.util.Locale;

public class COUIInputView extends ConstraintLayout {
    private static final int APPEAR_DURATION = 217;
    private static final int COUNT_VIEW_PADDING = 8;
    private static final int DISAPPEAR_DURATION = 283;
    public static final int INPUT_TYPE_TEXT = 0;
    public static final int INPUT_TYPE_NUMBER = 1;
    public static final int INPUT_TYPE_NUMBER_PASSWORD = 2;
    private static final int MAX_BUTTON_ICON_COUNT = 2;
    private static final int MAX_LINE = 5;
    private static final int PASSWORD_STATUES_TYPE_OPEN = 0;
    private static final int PASSWORD_STATUES_TYPE_CLOSE = 1;

    protected View mButtonLayout;
    private ErrorStateChangeCallback mCallback;
    private Paint mCountPaint;
    protected TextView mCountTextView;
    private boolean mCustomFormat;
    private ImageButton mDeleteButton;
    private int mDeleteIconMarginEndWithPsd;
    private boolean mEditLineColor;
    protected COUIEditText mEditText;
    private LinearLayout mEdittextContainer;
    private boolean mEnableError;
    protected boolean mEnableInputCount;
    private boolean mEnablePassword;
    private TextView mErrorText;
    private ValueAnimator mHideErrorTextAnimator;
    private CharSequence mHint;
    protected int mInputType;
    protected int mMaxCount;
    protected OnEditTextChangeListener mOnEditTextChangeListener;
    private View.OnFocusChangeListener mOnFocusChangeListener;
    CheckBox mPasswordButton;
    private int mPasswordType;
    private PathInterpolator mPathInterpolator;
    private ValueAnimator mShowErrorTextAnimator;
    private String mSpaceString;
    private int mTextMinHeightInInputView;
    private TextWatcher mTextWatcher;
    protected CharSequence mTitle;
    protected TextView mTitleTextView;
    private Runnable mUpdateRunnable;
    private String replaceString;

    public interface ErrorStateChangeCallback {
        void callback(boolean error);
    }

    public interface OnEditTextChangeListener {
        void afterTextChange(Editable editable);
    }

    public COUIInputView(Context context) {
        this(context, null);
    }

    public COUIInputView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public COUIInputView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mOnEditTextChangeListener = null;
        mPathInterpolator = new COUIEaseInterpolator();
        mCountPaint = null;
        mEditLineColor = false;
        mCustomFormat = true;
        mUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                mEditText.setPaddingRelative(0, getEdittextPaddingTop(), getEdittextPaddingEnd(),
                        getEdittextPaddingBottom());
                mTitleTextView.setPaddingRelative(mTitleTextView.getPaddingStart(), getTitlePaddingTop(),
                        mTitleTextView.getPaddingEnd(), mTitleTextView.getPaddingBottom());
                UIUtil.setMargin(mButtonLayout, 1,
                        (getEdittextPaddingTop() - getEdittextPaddingBottom()) / 2);
            }
        };

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.COUIInputView, defStyleAttr, 0);
        mTitle = a.getText(R.styleable.COUIInputView_couiTitle);
        mHint = a.getText(R.styleable.COUIInputView_couiHint);
        mEnablePassword = a.getBoolean(R.styleable.COUIInputView_couiEnablePassword, false);
        mPasswordType = a.getInt(R.styleable.COUIInputView_couiPasswordType, PASSWORD_STATUES_TYPE_OPEN);
        mEnableError = a.getBoolean(R.styleable.COUIInputView_couiEnableError, false);
        mMaxCount = a.getInt(R.styleable.COUIInputView_couiInputMaxCount, 0);
        mEnableInputCount = a.getBoolean(R.styleable.COUIInputView_couiEnableInputCount, false);
        mInputType = a.getInt(R.styleable.COUIInputView_couiInputType, -1);
        mCustomFormat = a.getBoolean(R.styleable.COUIInputView_couiInputCustomFormat, true);
        mEditLineColor = a.getBoolean(R.styleable.COUIInputView_couiEditLineColor, false);
        a.recycle();

        LayoutInflater.from(getContext()).inflate(getLayoutResId(), this, true);
        mTitleTextView = findViewById(R.id.title);
        mCountTextView = findViewById(R.id.input_count);
        mErrorText = findViewById(R.id.text_input_error);
        mButtonLayout = findViewById(R.id.button_layout);
        mEdittextContainer = findViewById(R.id.edittext_container);
        mDeleteButton = findViewById(R.id.delete_button);
        mPasswordButton = findViewById(R.id.checkbox_password);
        mDeleteIconMarginEndWithPsd = getResources().getDimensionPixelSize(
                R.dimen.coui_inputview_delete_button_margin_end_with_passwordicon);
        mTextMinHeightInInputView = getResources().getDimensionPixelOffset(
                R.dimen.coui_inputView_edittext_content_minheight);
        nowInit(context, attrs);
        initListener();
    }

    private int getCountTextWidth() {
        if (!mEnableInputCount) {
            return 0;
        }
        if (mCountPaint == null) {
            mCountPaint = new Paint();
            mCountPaint.setTextSize(mCountTextView.getTextSize());
        }
        return ((int) mCountPaint.measureText(String.valueOf(mCountTextView.getText()))) + COUNT_VIEW_PADDING;
    }

    private int getCustomButtonShowNum() {
        if (!(mButtonLayout instanceof ViewGroup)) {
            return 0;
        }
        ViewGroup viewGroup = (ViewGroup) mButtonLayout;
        int count = 0;
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            if (child.getVisibility() == VISIBLE && mCountTextView != null
                    && mCountTextView.getId() != child.getId()) {
                count++;
            }
        }
        return count;
    }

    private void handleCustomStyleText(CharSequence text) {
        if (!Locale.getDefault().getLanguage().equals("zh")) {
            return;
        }
        if (mSpaceString != null && mSpaceString.equals(text.toString())) {
            return;
        }
        mSpaceString = text.toString();
        boolean mobile = RegexUtils.isCnMobileExact(text);
        boolean bankCard = RegexUtils.isCnBankCardId(text);
        if (!mobile && !bankCard) {
            resetCustomStyleText(text);
            return;
        }
        replaceString = text.toString();
        SpannableString spannableString = new SpannableString(text);
        int length = spannableString.length() / 4;
        for (int i = 0; i < length; i++) {
            int position = (i + 1) * 4;
            if (mobile) {
                spannableString.setSpan(new CustomEditTextSpan(), position - 2, position - 1, 17);
            } else {
                spannableString.setSpan(new CustomEditTextSpan(), position - 1, position, 17);
            }
        }
        int selectionStart = mEditText.getSelectionStart();
        mEditText.setText(spannableString);
        mEditText.setSelection(Math.min(selectionStart, mEditText.getText().length()));
    }

    private void handleWithError() {
        if (!mEnableError) {
            mErrorText.setVisibility(GONE);
            return;
        }
        if (!TextUtils.isEmpty(mErrorText.getText())) {
            mErrorText.setVisibility(VISIBLE);
        }
        mEditText.addOnErrorStateChangedListener(new COUIEditText.OnErrorStateChangedListener() {
            @Override
            public void onErrorStateChanged(boolean error) {
                mEditText.setSelectAllOnFocus(error);
                if (error) {
                    showErrorMsgAnim();
                } else {
                    hideErrorMsgAnim();
                }
                if (mCallback != null) {
                    mCallback.callback(error);
                }
            }

            @Override
            public void onErrorStateChangeAnimationEnd(boolean error) {
            }
        });
    }

    private void handleWithTitle() {
        if (TextUtils.isEmpty(mTitle)) {
            return;
        }
        mTitleTextView.setText(mTitle);
        mTitleTextView.setVisibility(VISIBLE);
    }

    private void hideErrorMsgAnim() {
        if (mShowErrorTextAnimator != null && mShowErrorTextAnimator.isRunning()) {
            mShowErrorTextAnimator.cancel();
        }
        if (mHideErrorTextAnimator == null) {
            mHideErrorTextAnimator = ValueAnimator.ofFloat(1.0f, 0.0f);
            mHideErrorTextAnimator.setDuration(DISAPPEAR_DURATION).setInterpolator(mPathInterpolator);
            mHideErrorTextAnimator.addUpdateListener(animation ->
                    mErrorText.setAlpha((Float) animation.getAnimatedValue()));
            mHideErrorTextAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationCancel(Animator animation) {
                    mErrorText.setVisibility(GONE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mErrorText.setVisibility(GONE);
                }
            });
        }
        if (mHideErrorTextAnimator.isStarted()) {
            mHideErrorTextAnimator.cancel();
        }
        mHideErrorTextAnimator.start();
    }

    private void init() {
        handleWithTitle();
        mEditText.setTopHint(mHint);
        if (mEditLineColor) {
            mEditText.setDefaultStrokeColor(COUIContextUtil.getAttrColor(getContext(), R.attr.couiColorPrimary));
        }
        handleWithCount();
        handleWithPassword();
        handleWithError();
        initDeleteButton();
        updatePadding(false);
    }

    private void initDeleteButton() {
        if (mDeleteButton == null || mEditText.isShowDeleteIcon()) {
            return;
        }
        mDeleteButton.setOnClickListener(view -> {
            COUIEditText.OnTextDeletedListener listener = mEditText.getTextDeleteListener();
            if (listener == null || !listener.onTextDeleted()) {
                mEditText.onFastDelete();
            }
        });
    }

    private void initListener() {
        if (mButtonLayout != null) {
            mButtonLayout.addOnLayoutChangeListener((view, left, top, right, bottom, oldLeft, oldTop,
                    oldRight, oldBottom) -> {
                if (oldRight - oldLeft != right - left) {
                    updatePadding(true);
                }
            });
        }
    }

    private boolean isEnablePassword() {
        return mPasswordButton.getVisibility() == VISIBLE
                ? mEnablePassword
                : mEnablePassword && getCustomButtonShowNum() < MAX_BUTTON_ICON_COUNT;
    }

    private boolean isShowDeleteButton() {
        return mDeleteButton.getVisibility() == VISIBLE
                ? mEditText.isFastDeletable()
                : mEditText.isFastDeletable() && getCustomButtonShowNum() < MAX_BUTTON_ICON_COUNT;
    }

    private void resetCustomStyleText(CharSequence text) {
        if (replaceString != null) {
            String value = String.valueOf(text);
            int selectionStart = mEditText.getSelectionStart();
            mEditText.setText(value);
            mEditText.setSelection(Math.min(selectionStart, mEditText.getText().length()));
            replaceString = null;
        }
    }

    private void setInputType() {
        if (mInputType == -1) {
            return;
        }
        if (mInputType == INPUT_TYPE_TEXT) {
            mEditText.setInputType(1);
        } else if (mInputType == INPUT_TYPE_NUMBER) {
            mEditText.setInputType(2);
        } else if (mInputType == INPUT_TYPE_NUMBER_PASSWORD) {
            mEditText.setInputType(18);
        } else {
            mEditText.setInputType(0);
        }
    }

    private void showErrorMsgAnim() {
        if (mHideErrorTextAnimator != null && mHideErrorTextAnimator.isRunning()) {
            mHideErrorTextAnimator.cancel();
        }
        mErrorText.setVisibility(VISIBLE);
        if (mShowErrorTextAnimator == null) {
            mShowErrorTextAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
            mShowErrorTextAnimator.setDuration(APPEAR_DURATION).setInterpolator(mPathInterpolator);
            mShowErrorTextAnimator.addUpdateListener(animation ->
                    mErrorText.setAlpha((Float) animation.getAnimatedValue()));
        }
        if (mShowErrorTextAnimator.isStarted()) {
            mShowErrorTextAnimator.cancel();
        }
        mShowErrorTextAnimator.start();
    }

    private void updateDeleteButton(boolean focused) {
        if (mDeleteButton == null) {
            return;
        }
        if (!isShowDeleteButton() || !focused || TextUtils.isEmpty(mEditText.getText().toString())) {
            mDeleteButton.setVisibility(GONE);
        } else {
            if (UIUtil.isInVisibleRect(mDeleteButton)) {
                return;
            }
            mDeleteButton.setVisibility(INVISIBLE);
            post(() -> mDeleteButton.setVisibility(VISIBLE));
        }
    }

    private void updatePadding(boolean post) {
        if (!post) {
            mUpdateRunnable.run();
        } else {
            mEditText.removeCallbacks(mUpdateRunnable);
            mEditText.post(mUpdateRunnable);
        }
    }

    public void addCustomButton(View view) {
        if (!(mButtonLayout instanceof ViewGroup) || view == null) {
            return;
        }
        ViewGroup viewGroup = (ViewGroup) mButtonLayout;
        if (getCustomButtonShowNum() < MAX_BUTTON_ICON_COUNT) {
            int size = getResources().getDimensionPixelSize(R.dimen.coui_inputview_custom_button_size);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMarginStart(mDeleteIconMarginEndWithPsd);
            params.setMarginEnd(0);
            viewGroup.addView(view, params);
            updatePadding(true);
        }
    }

    public TextView getCountTextView() {
        return mCountTextView;
    }

    public COUIEditText getEditText() {
        return mEditText;
    }

    public int getEdittextPaddingBottom() {
        return !TextUtils.isEmpty(mTitle)
                ? getResources().getDimensionPixelSize(R.dimen.coui_input_edit_error_text_has_title_padding_bottom)
                : (int) getResources().getDimension(R.dimen.coui_input_edit_text_no_title_padding_bottom);
    }

    public int getEdittextPaddingEnd() {
        return mButtonLayout.getWidth();
    }

    public int getEdittextPaddingTop() {
        return !TextUtils.isEmpty(mTitle)
                ? getResources().getDimensionPixelSize(R.dimen.coui_input_edit_text_has_title_padding_top)
                : (int) getResources().getDimension(R.dimen.coui_input_edit_text_no_title_padding_top);
    }

    public CharSequence getHint() {
        return mHint;
    }

    public int getLayoutResId() {
        return R.layout.coui_input_view;
    }

    public int getMaxCount() {
        return mMaxCount;
    }

    public CharSequence getTitle() {
        return mTitle;
    }

    public int getTitlePaddingTop() {
        return getResources().getDimensionPixelSize(R.dimen.coui_input_preference_title_padding_top);
    }

    public void handleWithCount() {
        handleWithCountTextView();
        if (mTextWatcher == null) {
            mTextWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (isIsCardSingleInput() && mCustomFormat) {
                        handleCustomStyleText(s);
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    if (mEnableInputCount && mMaxCount > 0) {
                        if (mOnEditTextChangeListener != null) {
                            mOnEditTextChangeListener.afterTextChange(editable);
                        } else {
                            int length = editable.length();
                            if (length < mMaxCount) {
                                mCountTextView.setText(length + "/" + mMaxCount);
                                mCountTextView.setTextColor(COUIContextUtil.getAttrColor(getContext(),
                                        R.attr.couiColorHintNeutral));
                            } else {
                                mCountTextView.setText(mMaxCount + "/" + mMaxCount);
                                mCountTextView.setTextColor(COUIContextUtil.getAttrColor(getContext(),
                                        R.attr.couiColorError));
                                if (length > mMaxCount) {
                                    mEditText.setText(editable.subSequence(0, mMaxCount));
                                }
                            }
                        }
                    }
                    updateDeleteButton(hasFocus());
                    updatePadding(true);
                }
            };
            mEditText.addTextChangedListener(mTextWatcher);
        }
        if (mOnFocusChangeListener == null) {
            mOnFocusChangeListener = (view, focused) -> {
                updateDeleteButton(focused);
                updatePadding(true);
            };
            mEditText.setOnFocusChangeListener(mOnFocusChangeListener);
        }
    }

    public void handleWithCountTextView() {
        if (!mEnableInputCount || mMaxCount <= 0) {
            mCountTextView.setVisibility(GONE);
            return;
        }
        mCountTextView.setVisibility(VISIBLE);
        mCountTextView.setText(mEditText.getText().length() + "/" + mMaxCount);
    }

    public void handleWithPassword() {
        if (!isEnablePassword()) {
            mPasswordButton.setVisibility(GONE);
            setInputType();
            return;
        }
        mPasswordButton.setVisibility(VISIBLE);
        if (mPasswordType == PASSWORD_STATUES_TYPE_CLOSE) {
            mPasswordButton.setChecked(false);
            if (mInputType == INPUT_TYPE_NUMBER || mInputType == INPUT_TYPE_NUMBER_PASSWORD) {
                mEditText.setInputType(18);
            } else {
                mEditText.setInputType(129);
            }
        } else {
            mPasswordButton.setChecked(true);
            if (mInputType == INPUT_TYPE_NUMBER || mInputType == INPUT_TYPE_NUMBER_PASSWORD) {
                mEditText.setInputType(2);
            } else {
                mEditText.setInputType(145);
            }
        }
        mPasswordButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean checked) {
                if (checked) {
                    if (mInputType == INPUT_TYPE_NUMBER || mInputType == INPUT_TYPE_NUMBER_PASSWORD) {
                        mEditText.setInputType(2);
                    } else {
                        mEditText.setInputType(145);
                    }
                } else {
                    if (mInputType == INPUT_TYPE_NUMBER || mInputType == INPUT_TYPE_NUMBER_PASSWORD) {
                        mEditText.setInputType(18);
                    } else {
                        mEditText.setInputType(129);
                    }
                }
            }
        });
    }

    public COUIEditText instanceCOUIEditText(Context context, AttributeSet attrs) {
        context.getTheme().applyStyle(R.style.COUIInputViewStyle, true);
        COUIEditText editText = new COUIEditText(context, attrs, R.attr.couiInputPreferenceEditTextStyle);
        editText.setShowDeleteIcon(false);
        editText.setVerticalScrollBarEnabled(false);
        editText.setMinHeight(mTextMinHeightInInputView);
        return editText;
    }

    public boolean isEnableInputCount() {
        return mEnableInputCount;
    }

    public boolean isIsCardSingleInput() {
        return false;
    }

    public void lazyInit(Context context, AttributeSet attrs) {
        mEditText = instanceCOUIEditText(context, attrs);
        mEditText.setMaxLines(MAX_LINE);
        mEdittextContainer.addView(mEditText, -1, -2);
        init();
    }

    public void nowInit(Context context, AttributeSet attrs) {
        lazyInit(context, attrs);
    }

    public void removeCustomButton(View view) {
        if (!(mButtonLayout instanceof ViewGroup) || view == null) {
            return;
        }
        ((ViewGroup) mButtonLayout).removeView(view);
        updatePadding(true);
    }

    public void setCustomFormat(Boolean customFormat) {
        mCustomFormat = customFormat;
        if (mEditText.getText() == null) {
            return;
        }
        if (isIsCardSingleInput() && mCustomFormat) {
            handleCustomStyleText(mEditText.getText());
        } else {
            resetCustomStyleText(mEditText.getText());
        }
    }

    public void setEnableError(boolean enableError) {
        if (mEnableError != enableError) {
            mEnableError = enableError;
            handleWithError();
            updatePadding(false);
        }
    }

    public void setEnableInputCount(boolean enableInputCount) {
        mEnableInputCount = enableInputCount;
        handleWithCount();
        updatePadding(true);
    }

    public void setEnablePassword(boolean enablePassword) {
        if (mEnablePassword != enablePassword) {
            mEnablePassword = enablePassword;
            handleWithPassword();
            updatePadding(true);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        mEditText.setEnabled(enabled);
        mTitleTextView.setEnabled(enabled);
        mButtonLayout.setEnabled(enabled);
        mPasswordButton.setEnabled(enabled);
        mCountTextView.setEnabled(enabled);
    }

    public void setErrorStateChangeCallBack(ErrorStateChangeCallback callback) {
        mCallback = callback;
    }

    public void setHint(CharSequence hint) {
        mHint = hint;
        mEditText.setTopHint(hint);
    }

    public void setMaxCount(int maxCount) {
        mMaxCount = maxCount;
        handleWithCount();
    }

    public void setOnEditTextChangeListener(OnEditTextChangeListener listener) {
        mOnEditTextChangeListener = listener;
    }

    public void setPasswordType(int passwordType) {
        if (mPasswordType != passwordType) {
            mPasswordType = passwordType;
            handleWithPassword();
            updatePadding(true);
        }
    }

    public void setTitle(CharSequence title) {
        if (title == null || title.equals(mTitle)) {
            return;
        }
        mTitle = title;
        handleWithTitle();
        updatePadding(false);
    }

    public void showError(CharSequence error) {
        if (TextUtils.isEmpty(error)) {
            mEditText.setErrorState(false);
        } else {
            mEditText.setErrorState(true);
            if (mEnableError) {
                mErrorText.setVisibility(VISIBLE);
            }
        }
        mErrorText.setText(error);
    }
}
