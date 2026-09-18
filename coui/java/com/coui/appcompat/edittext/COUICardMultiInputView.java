package com.coui.appcompat.edittext;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.coui.appcompat.R;
import com.coui.appcompat.contextutil.COUIContextUtil;

public class COUICardMultiInputView extends ConstraintLayout implements View.OnLayoutChangeListener {
    private static final int ERROR_COUNT_COLOR_ANIMATOR_TIME = 250;
    private static final int ERROR_COUNT_COLOR_DELAY_TIME = 1000;
    private static final int MAX_HINT_LINE = 3;
    private static final int MAX_LINE = 5;

    private boolean isErrorColor;
    private int mCountTextColor;
    private ValueAnimator mCountTextColorAnimator;
    private TextView mCountTextView;
    private COUIEditText mEditText;
    private int mEditTextMaxHeight;
    private LinearLayout mEdittextContainer;
    private final Rect mEdittextContainerRect = new Rect();
    private boolean mEnableInputCount;
    private CharSequence mHint;
    private TextWatcher mHintTextWatcher;
    private InputMethodManager mInputMethodManager;
    private int mMaxCount;
    private final Runnable setCountColorRunnable = new Runnable() {
        @Override
        public void run() {
            if (isErrorColor) {
                executeColorAnimator(mCountTextColor,
                        COUIContextUtil.getAttrColor(getContext(), R.attr.couiColorHintNeutral));
                isErrorColor = false;
            }
        }
    };

    public COUICardMultiInputView(Context context) {
        this(context, null);
    }

    public COUICardMultiInputView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public COUICardMultiInputView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.COUIInputView,
                defStyleAttr, 0);
        mHint = a.getText(R.styleable.COUIInputView_couiHint);
        mMaxCount = a.getInt(R.styleable.COUIInputView_couiInputMaxCount, 0);
        mEnableInputCount = a.getBoolean(R.styleable.COUIInputView_couiEnableInputCount, false);
        a.recycle();
        LayoutInflater.from(getContext()).inflate(getLayoutResId(), (ViewGroup) this, true);
        mEdittextContainer = findViewById(R.id.edittext_container);
        mEditText = instanceCOUIEditText(context, attrs);
        mEditText.setMaxLines(MAX_HINT_LINE);
        mEditText.setEllipsize(TextUtils.TruncateAt.END);
        mEditText.setGravity(8388659);
        mEdittextContainer.addView(mEditText, -1, -1);
        mEdittextContainer.addOnLayoutChangeListener(this);
        mCountTextView = findViewById(R.id.input_count);
        int dimensionPixelSize = getResources().getDimensionPixelSize(
                R.dimen.support_preference_category_layout_title_margin_start);
        mCountTextColor = COUIContextUtil.getAttrColor(getContext(), R.attr.couiColorHintNeutral);
        findViewById(R.id.single_card).setOnTouchListener((view, event) -> {
            int action = event.getAction();
            if ((action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL)
                    && event.getX() > dimensionPixelSize
                    && event.getX() < getWidth() - dimensionPixelSize) {
                if (mInputMethodManager == null) {
                    mInputMethodManager = (InputMethodManager) getContext()
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                }
                mEditText.setFocusable(true);
                mEditText.requestFocus();
                mInputMethodManager.showSoftInput(mEditText, 0);
            }
            return true;
        });
        init();
    }

    private void cancelColorAnimator() {
        if (mCountTextColorAnimator != null && mCountTextColorAnimator.isRunning()) {
            mCountTextColorAnimator.cancel();
        }
    }

    private void executeColorAnimator(int startColor, int endColor) {
        cancelColorAnimator();
        mCountTextColorAnimator = ValueAnimator.ofInt(startColor, endColor);
        mCountTextColorAnimator.addUpdateListener(animation -> {
            mCountTextColor = (Integer) animation.getAnimatedValue();
            mCountTextView.setTextColor(mCountTextColor);
        });
        mCountTextColorAnimator.setDuration(ERROR_COUNT_COLOR_ANIMATOR_TIME);
        mCountTextColorAnimator.setEvaluator(new ArgbEvaluator());
        mCountTextColorAnimator.start();
    }

    private void handleWithCount() {
        if (!mEnableInputCount || mMaxCount <= 0) {
            mCountTextView.setVisibility(GONE);
            mEditText.setPadding(mEditText.getPaddingLeft(), mEditText.getPaddingTop(),
                    mEditText.getPaddingRight(), mEditText.getPaddingTop());
            return;
        }
        mCountTextView.setVisibility(VISIBLE);
        mCountTextView.setText(mEditText.getText().length() + "/" + mMaxCount);
        mEditText.post(() -> mEditText.setPadding(mEditText.getPaddingLeft(),
                mEditText.getPaddingTop(), mEditText.getPaddingRight(),
                mCountTextView.getMeasuredHeight()));
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                int length = editable.length();
                if (length < mMaxCount) {
                    mCountTextView.setText(length + "/" + mMaxCount);
                    if (isErrorColor) {
                        executeColorAnimator(mCountTextColor,
                                COUIContextUtil.getAttrColor(getContext(),
                                        R.attr.couiColorHintNeutral));
                        mEditText.removeCallbacks(setCountColorRunnable);
                    }
                    isErrorColor = false;
                    return;
                }
                mCountTextView.setText(mMaxCount + "/" + mMaxCount);
                if (length > mMaxCount) {
                    mEditText.setText(editable.subSequence(0, mMaxCount));
                    mEditText.setSelection(mEditText.getText().length());
                }
                executeColorAnimator(mCountTextColor,
                        COUIContextUtil.getAttrColor(getContext(), R.attr.couiColorError));
                mEditText.removeCallbacks(setCountColorRunnable);
                mEditText.postDelayed(setCountColorRunnable, ERROR_COUNT_COLOR_DELAY_TIME);
                isErrorColor = true;
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
    }

    private void init() {
        if (mHintTextWatcher == null) {
            mHintTextWatcher = new TextWatcher() {
                @Override
                public void afterTextChanged(Editable editable) {
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (TextUtils.isEmpty(s)) {
                        mEditText.setMaxLines(MAX_HINT_LINE);
                        mEditText.setEllipsize(TextUtils.TruncateAt.END);
                    } else {
                        mEditText.setMaxLines(MAX_LINE);
                        mEditText.setEllipsize(null);
                    }
                }
            };
        }
        mEditText.addTextChangedListener(mHintTextWatcher);
        mEditText.setTopHint(mHint);
        handleWithCount();
    }

    private void releaseCountColorRunnable() {
        if (mEditText != null) {
            mEditText.removeCallbacks(setCountColorRunnable);
        }
    }

    public COUIEditText getEditText() {
        return mEditText;
    }

    public CharSequence getHint() {
        return mHint;
    }

    public int getLayoutResId() {
        return R.layout.coui_multi_input_card_view;
    }

    public COUIEditText instanceCOUIEditText(Context context, AttributeSet attrs) {
        context.getTheme().applyStyle(R.style.COUIMultiInputViewStyle, true);
        return new COUIEditText(context, attrs, R.attr.couiCardMultiInputEditTextStyle);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        releaseCountColorRunnable();
        cancelColorAnimator();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mEditTextMaxHeight = (((mEdittextContainer.getMeasuredHeight()
                    - mEdittextContainer.getPaddingTop()) - mEdittextContainer.getPaddingBottom())
                    - mEditText.getPaddingTop()) - mEditText.getPaddingBottom();
            boolean canScrollText = mEditText.getLineCount() * mEditText.getLineHeight()
                    > mEditTextMaxHeight;
            if (mEdittextContainerRect.contains((int) event.getX(), (int) event.getY())
                    && canScrollText && mEditText.getLineCount() >= 1) {
                mEdittextContainer.requestDisallowInterceptTouchEvent(true);
            }
        }
        return false;
    }

    @Override
    public void onLayoutChange(View view, int left, int top, int right, int bottom, int oldLeft,
            int oldTop, int oldRight, int oldBottom) {
        mEdittextContainerRect.left = 0;
        mEdittextContainerRect.top = 0;
        mEdittextContainerRect.right = getMeasuredWidth();
        mEdittextContainerRect.bottom = getMeasuredHeight() - mEdittextContainer.getPaddingBottom();
    }

    public void setHint(CharSequence hint) {
        mHint = hint;
        mEditText.setTopHint(hint);
    }

    public void setMaxCount(int maxCount) {
        mMaxCount = maxCount;
        handleWithCount();
    }
}
