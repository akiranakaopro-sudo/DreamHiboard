package com.coui.appcompat.searchview;


import com.coui.appcompat.R;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.coui.appcompat.animation.COUIEaseInterpolator;
import com.coui.appcompat.contextutil.COUIContextUtil;
import com.google.android.material.internal.TextWatcherAdapter;
import java.util.ArrayList;
import java.util.List;


public class COUIHintAnimationLayout extends FrameLayout {
    private static final int DURATION_TIME = 600;
    private static final int INTERVAL_TIME = 3000;
    private static final int IN_DELAY_TIME = 150;
    private static final String TAG = "COUIHintAnimationLayout";
    boolean mAnimationIsWorking;
    private AnimatorSet mAnimatorSetIn;
    private AnimatorSet mAnimatorSetOut;
    private COUIHintAnimationChangeListener mCOUIHintAnimationChangeListener;
    private Runnable mChangeHintRunnable;
    private int mCouiSearchBarAnimationTranslateExtra;
    private int mCurrentHintIndex;
    private TextView mCurrentHintTextView;
    private int mCurrentPlayTime;
    private ObjectAnimator mFadeIn;
    private ObjectAnimator mFadeOut;
    private List<String> mHintStrings;
    private TextView mHintTextViewFirst;
    private TextView mHintTextViewThen;
    private ObjectAnimator mMoveIn;
    private ObjectAnimator mMoveOut;
    boolean mNeedRePlay;
    private boolean mNeedStopAnimation;
    private int mRepeatCount;
    private EditText mSearchEditText;
    private String mTempQueryHint;
    private static final TimeInterpolator TRANSLATEINTERPOLATOR = androidx.core.view.animation.PathInterpolatorCompat.create(0.3f, 0.0f, 0.2f, 1.0f);
    private static final COUIEaseInterpolator EASEINTERPOLATOR = new COUIEaseInterpolator();

    public interface COUIHintAnimationChangeListener {
        void hintAnimationChange(int hintIndex, String hint, TextView textView);
    }

    public COUIHintAnimationLayout(Context context) {
        this(context, null);
    }

    private boolean animationIsRunning() {
        AnimatorSet animatorSet;
        AnimatorSet animatorSetIn = this.mAnimatorSetIn;
        return (animatorSetIn != null && animatorSetIn.isRunning()) || ((animatorSet = this.mAnimatorSetOut) != null && animatorSet.isRunning());
    }


    public void applyHintChangeAnimation(String hint) {
        if (this.mCurrentHintTextView == null) {
            return;
        }
        int playTime = this.mCurrentPlayTime + 1;
        this.mCurrentPlayTime = playTime;
        int repeatCount = this.mRepeatCount;
        if (repeatCount != -1 && playTime > repeatCount) {
            pauseHintsAnimation();
            return;
        }
        this.mTempQueryHint = hint;
        int measuredHeight = ((getMeasuredHeight() - this.mCurrentHintTextView.getLineHeight()) / 2) + this.mCouiSearchBarAnimationTranslateExtra;
        if (this.mAnimatorSetOut == null || this.mAnimatorSetIn == null) {
            ObjectAnimator moveOut = ObjectAnimator.ofFloat(this.mCurrentHintTextView, "translationY", 0.0f, -measuredHeight);
            this.mMoveOut = moveOut;
            TimeInterpolator translateInterpolator = TRANSLATEINTERPOLATOR;
            moveOut.setInterpolator(translateInterpolator);
            ObjectAnimator fadeOut = ObjectAnimator.ofFloat(this.mCurrentHintTextView, "alpha", 1.0f, 0.0f);
            this.mFadeOut = fadeOut;
            COUIEaseInterpolator easeInterpolator = EASEINTERPOLATOR;
            fadeOut.setInterpolator(easeInterpolator);
            AnimatorSet animatorSetOut = new AnimatorSet();
            this.mAnimatorSetOut = animatorSetOut;
            animatorSetOut.playTogether(this.mMoveOut, this.mFadeOut);
            this.mAnimatorSetOut.setDuration(DURATION_TIME);
            ObjectAnimator moveIn = ObjectAnimator.ofFloat(getNextHintTextView(), "translationY", measuredHeight, 0.0f);
            this.mMoveIn = moveIn;
            moveIn.setInterpolator(translateInterpolator);
            ObjectAnimator fadeIn = ObjectAnimator.ofFloat(getNextHintTextView(), "alpha", 0.0f, 1.0f);
            this.mFadeIn = fadeIn;
            fadeIn.setInterpolator(easeInterpolator);
            AnimatorSet animatorSetIn = new AnimatorSet();
            this.mAnimatorSetIn = animatorSetIn;
            animatorSetIn.playTogether(this.mMoveIn, this.mFadeIn);
            this.mAnimatorSetIn.setDuration(DURATION_TIME);
            this.mAnimatorSetIn.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animator) {
                    super.onAnimationEnd(animator);
                    COUIHintAnimationLayout layout = COUIHintAnimationLayout.this;
                    layout.mCurrentHintTextView = layout.getNextHintTextView();
                    if (COUIHintAnimationLayout.this.mNeedStopAnimation) {
                        COUIHintAnimationLayout.this.pauseHintsAnimation();
                        COUIHintAnimationLayout.this.mNeedStopAnimation = false;
                    }
                }

                @Override
                public void onAnimationStart(Animator animator) {
                    super.onAnimationStart(animator);
                    COUIHintAnimationLayout.this.getNextHintTextView().setText(COUIHintAnimationLayout.this.mTempQueryHint);
                    COUIHintAnimationLayout.this.getNextHintTextView().setVisibility(0);
                }
            });
        } else {
            this.mMoveOut.setTarget(this.mCurrentHintTextView);
            this.mFadeOut.setTarget(this.mCurrentHintTextView);
            this.mMoveIn.setTarget(getNextHintTextView());
            this.mFadeIn.setTarget(getNextHintTextView());
        }
        postDelayed(new Runnable() {
            @Override
            public void run() {
                COUIHintAnimationLayout.this.mAnimatorSetIn.start();
            }
        }, IN_DELAY_TIME);
        this.mAnimatorSetOut.start();
        COUIHintAnimationChangeListener listener = this.mCOUIHintAnimationChangeListener;
        if (listener != null) {
            listener.hintAnimationChange(this.mCurrentHintIndex, hint, this.mCurrentHintTextView);
        }
    }


    public void cancelAnimation() {
        AnimatorSet animatorSetIn = this.mAnimatorSetIn;
        if (animatorSetIn != null && animatorSetIn.isRunning()) {
            this.mAnimatorSetIn.cancel();
        }
        AnimatorSet animatorSetOut = this.mAnimatorSetOut;
        if (animatorSetOut == null || !animatorSetOut.isRunning()) {
            return;
        }
        this.mAnimatorSetOut.cancel();
    }


    public TextView getNextHintTextView() {
        TextView current = this.mCurrentHintTextView;
        TextView first = this.mHintTextViewFirst;
        return current == first ? this.mHintTextViewThen : first;
    }

    private void initHintAnimationTextView() {
        if (this.mHintTextViewFirst == null && this.mHintTextViewThen == null) {
            Context context = getContext();
            int hintStyle = R.style.Widget_COUI_EditText_SearchViewStyle_HintText;
            this.mHintTextViewFirst = new TextView(new ContextThemeWrapper(context, hintStyle), null);
            this.mHintTextViewThen = new TextView(new ContextThemeWrapper(getContext(), hintStyle), null);
            this.mHintTextViewFirst.setImportantForAccessibility(2);
            this.mHintTextViewThen.setImportantForAccessibility(2);
            TextView first = this.mHintTextViewFirst;
            int textAppearance = R.style.couiTextAppearanceBodyL;
            first.setTextAppearance(textAppearance);
            this.mHintTextViewThen.setTextAppearance(textAppearance);
            TextView firstAgain = this.mHintTextViewFirst;
            Context colorContext = getContext();
            int labelColorAttr = R.attr.couiColorLabelSecondary;
            firstAgain.setTextColor(COUIContextUtil.getAttrColor(colorContext, labelColorAttr));
            this.mHintTextViewThen.setTextColor(COUIContextUtil.getAttrColor(getContext(), labelColorAttr));
            this.mHintTextViewFirst.setId(R.id.coui_hint_text_view_first);
            this.mHintTextViewThen.setId(R.id.coui_hint_text_view_then);
            addView(this.mHintTextViewFirst);
            addView(this.mHintTextViewThen);
        }
    }

    private void initTextHintAnimation() {
        if (this.mChangeHintRunnable == null) {
            this.mHintStrings = new ArrayList();
            this.mChangeHintRunnable = new Runnable() {
                @Override
                public void run() {
                    if (COUIHintAnimationLayout.this.mHintStrings.isEmpty()) {
                        return;
                    }
                    COUIHintAnimationLayout layout = COUIHintAnimationLayout.this;
                    layout.mCurrentHintIndex = (layout.mCurrentHintIndex + 1) % COUIHintAnimationLayout.this.mHintStrings.size();
                    COUIHintAnimationLayout layout2 = COUIHintAnimationLayout.this;
                    if (layout2.mAnimationIsWorking) {
                        layout2.applyHintChangeAnimation((String) layout2.mHintStrings.get(COUIHintAnimationLayout.this.mCurrentHintIndex));
                    }
                    COUIHintAnimationLayout.this.postDelayed(this, INTERVAL_TIME);
                }
            };
            this.mSearchEditText.addTextChangedListener(new TextWatcherAdapter() {
                @Override
                public void afterTextChanged(Editable editable) {
                    if (TextUtils.isEmpty(COUIHintAnimationLayout.this.mSearchEditText.getText().toString())) {
                        if (TextUtils.isEmpty(COUIHintAnimationLayout.this.mTempQueryHint)) {
                            return;
                        }
                        COUIHintAnimationLayout.this.mCurrentHintTextView.setText(COUIHintAnimationLayout.this.mTempQueryHint);
                        COUIHintAnimationLayout.this.mCurrentHintTextView.setVisibility(0);
                        COUIHintAnimationLayout.this.getNextHintTextView().setVisibility(8);
                        return;
                    }
                    COUIHintAnimationLayout layout = COUIHintAnimationLayout.this;
                    layout.removeCallbacks(layout.mChangeHintRunnable);
                    COUIHintAnimationLayout.this.mHintTextViewFirst.setVisibility(8);
                    COUIHintAnimationLayout.this.mHintTextViewThen.setVisibility(8);
                    COUIHintAnimationLayout.this.cancelAnimation();
                    COUIHintAnimationLayout.this.mAnimationIsWorking = false;
                }
            });
        }
    }

    private void resetHintTextView() {
        this.mHintTextViewFirst.setTranslationY(0.0f);
        this.mHintTextViewFirst.setAlpha(1.0f);
        this.mHintTextViewThen.setTranslationY(0.0f);
        this.mHintTextViewThen.setAlpha(1.0f);
    }

    public TextView getCurrentHintTextView() {
        return this.mCurrentHintTextView;
    }

    public List<String> getHintStrings() {
        return this.mHintStrings;
    }

    @Override
    public void onAttachedToWindow() {
        resumeHintsAnimation();
        super.onAttachedToWindow();
    }

    @Override
    public void onDetachedFromWindow() {
        pauseHintsAnimation();
        super.onDetachedFromWindow();
    }

    @Override
    public void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == 0) {
            if (this.mNeedRePlay) {
                resumeHintsAnimation();
                this.mNeedRePlay = false;
                return;
            }
            return;
        }
        if (this.mAnimationIsWorking) {
            pauseHintsAnimation();
            this.mNeedRePlay = true;
        }
    }

    public void pauseHintsAnimation() {
        List<String> list;
        removeCallbacks(this.mChangeHintRunnable);
        if (!this.mAnimationIsWorking || (list = this.mHintStrings) == null || list.size() == 0) {
            Log.e(TAG, "pauseHintsAnimation return");
            return;
        }
        if (animationIsRunning()) {
            this.mNeedStopAnimation = true;
            return;
        }
        resetHintTextView();
        this.mAnimationIsWorking = false;
        if (TextUtils.isEmpty(this.mTempQueryHint)) {
            this.mHintTextViewFirst.setVisibility(8);
            this.mHintTextViewThen.setVisibility(8);
        } else {
            this.mCurrentHintTextView.setText(this.mTempQueryHint);
            this.mCurrentHintTextView.setVisibility(0);
            getNextHintTextView().setVisibility(8);
        }
    }

    public void resumeHintsAnimation() {
        setHintsAnimation(this.mHintStrings);
    }

    public void setCOUIHintAnimationChangeListener(COUIHintAnimationChangeListener listener) {
        this.mCOUIHintAnimationChangeListener = listener;
    }

    public void setHintsAnimation(List<String> hints) {
        if (hints == null || hints.size() == 0) {
            return;
        }
        if (this.mSearchEditText == null) {
            if (!(getChildAt(0) instanceof EditText)) {
                Log.e(TAG, "Before calling this method, you must ensure that there is an edittext object in the container:1, you can call setSearchEditText or add an edittext yourself, refer to COUISearchBar2, you can put an edittext object in xml ( Refer to coui_search_view_animated_support_layout)to use the related functions of this animation container");
                return;
            }
            this.mSearchEditText = (EditText) getChildAt(0);
        }
        if (!TextUtils.isEmpty(this.mSearchEditText.getText().toString())) {
            Log.e(TAG, "Setting hints animation content is invalid when the searchEdittext has a value");
            return;
        }
        initHintAnimationTextView();
        initTextHintAnimation();
        if (!this.mHintStrings.equals(hints)) {
            this.mHintStrings.clear();
            this.mHintStrings.addAll(hints);
        }
        if (this.mCurrentHintTextView == null) {
            this.mCurrentHintTextView = this.mHintTextViewFirst;
        }
        if (TextUtils.isEmpty(this.mTempQueryHint)) {
            this.mTempQueryHint = this.mHintStrings.get(this.mCurrentHintIndex);
        }
        this.mCurrentHintTextView.setText(this.mTempQueryHint);
        this.mCurrentHintTextView.setVisibility(0);
        removeCallbacks(this.mChangeHintRunnable);
        this.mSearchEditText.setHint("");
        postDelayed(this.mChangeHintRunnable, INTERVAL_TIME);
        this.mAnimationIsWorking = true;
    }

    public void setRepeatCount(int repeatCount) {
        if (repeatCount <= 0) {
            Log.e(TAG, "RepeatCount must be greater than zero");
        } else {
            this.mRepeatCount = repeatCount;
        }
    }

    public void setSearchEditText(EditText editText) {
        this.mSearchEditText = editText;
        if (getChildCount() == 0) {
            addView(this.mSearchEditText, new FrameLayout.LayoutParams(-1, -1));
        } else {
            Log.e(TAG, "setSearchEditText() can only be executed once");
        }
    }

    public void setTextSize(int sizePx) {
        TextView first = this.mHintTextViewFirst;
        if (first == null || this.mHintTextViewThen == null) {
            return;
        }
        float size = sizePx;
        first.setTextSize(0, size);
        this.mHintTextViewThen.setTextSize(0, size);
    }

    public COUIHintAnimationLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mCurrentHintIndex = 0;
        this.mAnimationIsWorking = false;
        this.mNeedRePlay = false;
        this.mNeedStopAnimation = false;
        this.mRepeatCount = -1;
        this.mCurrentPlayTime = 0;
        this.mCouiSearchBarAnimationTranslateExtra = context.getResources().getDimensionPixelSize(R.dimen.coui_search_bar_animation_translate_extra);
    }
}
