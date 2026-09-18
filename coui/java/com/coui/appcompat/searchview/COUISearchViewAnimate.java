package com.coui.appcompat.searchview;


import com.coui.appcompat.R;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.animation.Interpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.ViewCompat;
import com.coui.appcompat.animation.COUIEaseInterpolator;
import com.coui.appcompat.animation.COUIMoveEaseInterpolator;
import com.coui.appcompat.contextutil.COUIContextUtil;
import com.coui.appcompat.darkmode.COUIDarkModeUtil;
import com.coui.appcompat.roundRect.COUIShapePath;
import com.coui.appcompat.searchview.ImeInsetsAnimationCallback;
import com.coui.appcompat.textutil.COUIChangeTextUtil;
import com.coui.appcompat.textviewcompatutil.COUITextViewCompatUtil;
import com.coui.appcompat.toolbar.COUIToolbar;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


public class COUISearchViewAnimate extends LinearLayout implements android.view.CollapsibleActionView, ImeInsetsAnimationCallback.OnImeAnimationListener {
    private static final float CLAMP_ANIMATION_PERCENT = 0.3f;
    private static final boolean DEBUG = true;
    private static final long DEFAULT_BUTTON_ALPHA_CHANGE_DURATION = 350;
    private static final long DEFAULT_BUTTON_MOVE_DURATION = 450;
    private static final long DEFAULT_FADE_DURATION = 150;
    private static final long DEFAULT_PRESS_FEEDBACK_DURATION = 150;
    private static final long DEFAULT_SEARCH_VIEW_OFFSET_CHANGE_DURATION = 450;
    private static final long DEFAULT_SEARCH_VIEW_SCALE_CHANGE_DURATION = 450;
    private static final float FLOAT_POINT_FIVE = 0.5f;
    private static final float FLOAT_TWO = 2.0f;
    private static final long NON_INSTANT_SEARCH_BUTTON_ALPHA_CHANGE_DURATION = 100;
    public static final int STATE_EDIT = 1;
    public static final int STATE_NORMAL = 0;
    private static final String TAG = "COUISearchViewAnimate";
    public static final int TYPE_INSTANT_SEARCH = 0;
    public static final int TYPE_NON_INSTANT_SEARCH = 1;
    private static final int WAY_AT_BEHIND = 1;
    private static final int WAY_AT_FRONT = 2;
    private static final int WAY_NONE = 0;
    private int mAddToToolbarWay;
    private volatile AnimatorHelper mAnimatorHelper;
    private OnStateChangeListener mAtToolbarFrontStateChangeListener;
    private int mBackgroundEndGap;
    private int mBackgroundStartGap;
    private ValueAnimator mButtonAlphaEnterAnimator;
    private ValueAnimator mButtonAlphaExitAnimator;
    private ImageView mButtonDivider;
    private final RectF mButtonHitRect;
    private final int[] mButtonLocation;
    private int mCancelButtonLargeStartMargin;
    private int mCancelButtonSmallStartMargin;
    private ImageView mCloseBtn;
    private int mCollapsedMinHeight;
    private float mCollapsingHeightPercent;
    private Context mContext;
    private int mCurrentBackgroundColor;
    private boolean mDivideBackground;
    private final ArgbEvaluator mEvaluator;
    private int mExtraY;
    private SearchFunctionalButton mFunctionalButton;
    private int mGravity;
    private int mGravityInToolBar;
    private boolean mHasAddedToToolbar;
    private int mHorizontalDividerColor;
    private int mInitSearchViewAnimateHeight;
    private int mInitSearchViewWrapperHeight;
    private boolean mInputMethodAnimationEnabled;
    private boolean mIsAtLeastR;
    private final int[] mLocation;
    private ImageView mMainIcon;
    private Rect mMainIconRect;
    private MenuItem mMenuItem;
    private boolean mNeedUpdateNormalRectPath;
    private boolean mNeedUpdatePressFeedbackRectPath;
    private int mNormalBackgroundColor;
    private final Path mNormalBackgroundPath;
    private RectF mNormalBackgroundRect;
    private final Paint mNormalPaint;
    private OnAnimationListener mOnAnimationListener;
    private List<OnCancelButtonClickListener> mOnCancelButtonClickListeners;
    private OnDispatchKeyEventPreImeListener mOnDispatchKeyEventPreImeListener;
    private List<OnStateChangeListener> mOnStateChangeListeners;
    private ValueAnimator mPressFeedbackAnimator;
    private final Path mPressFeedbackBackgroundPath;
    private RectF mPressFeedbackBackgroundRect;
    private final Paint mPressFeedbackPaint;
    private boolean mPressed;
    private int mPressedBackgroundColor;
    private Rect mRect;
    private ImageView mSearchIcon;
    private boolean mSearchIconCanAnimate;
    private COUISearchView mSearchView;
    private ValueAnimator mSearchViewOffsetEnterAnimator;
    private ValueAnimator mSearchViewOffsetExitAnimator;
    private ValueAnimator mSearchViewScaleEnterAnimator;
    private ValueAnimator mSearchViewScaleExitAnimator;
    private int mSearchViewShrinkWidth;
    private AnimatorSet mSearchViewSmoothEnterAnimatorSet;
    private AnimatorSet mSearchViewSmoothExitAnimatorSet;
    private int mSearchViewType;
    private ConstraintLayout mSearchViewWrapper;
    private boolean mShouldClearFocus;
    private int mShowImeAnimDuration;
    private Interpolator mShowImeInterpolator;
    private int mStartY;
    public AtomicInteger mState;
    private int mStyle;
    private ImageView mSubIcon;
    private Rect mSubIconRect;
    private COUIToolbar mToolBar;
    private boolean mToolBarAnimationRunning;
    private int mTopOffset;
    private final Rect mWrapperBounds;
    private static final String[] TYPE_NAME = {"TYPE_INSTANT_SEARCH", "TYPE_NON_INSTANT_SEARCH"};
    private static final Interpolator DEFAULT_SEARCH_VIEW_OFFSET_CHANGE_INTERPOLATOR = new COUIMoveEaseInterpolator();
    private static final Interpolator DEFAULT_SEARCH_VIEW_SCALE_CHANGE_INTERPOLATOR = new COUIMoveEaseInterpolator();
    private static final Interpolator DEFAULT_BUTTON_ALPHA_CHANGE_INTERPOLATOR = new COUIMoveEaseInterpolator();
    private static final Interpolator DEFAULT_PRESS_FEEDBACK_INTERPOLATOR = new COUIEaseInterpolator();
    private static final ArgbEvaluator DEFAULT_PRESS_FEEDBACK_EVALUATOR = new ArgbEvaluator();

    @Retention(RetentionPolicy.SOURCE)
    public @interface AddToolBarWay {
    }

    public class AnimatorHelper {
        private int mIconTranslation;
        private volatile AtomicBoolean mAnimatingAtomic = new AtomicBoolean(false);
        private Runnable mToEditStartRunnable = new Runnable() {
            @Override
            public void run() {
                if (COUISearchViewAnimate.this.mShouldClearFocus) {
                    COUISearchViewAnimate.this.setSearchAutoCompleteUnFocus();
                    COUISearchViewAnimate.this.openSoftInput(true);
                }
                COUISearchViewAnimate.this.mShouldClearFocus = true;
                if (COUISearchViewAnimate.this.mOnAnimationListener != null) {
                    COUISearchViewAnimate.this.mOnAnimationListener.onAnimationStart(1);
                }
                COUISearchViewAnimate.this.notifyOnStateChange(STATE_NORMAL, STATE_EDIT);
            }
        };
        private Runnable mToEditEndRunnable = new Runnable() {
            @Override
            public void run() {
                COUISearchViewAnimate.this.setSearchAutoCompleteFocus();
                AnimatorHelper.this.mAnimatingAtomic.set(false);
                if (COUISearchViewAnimate.this.mOnAnimationListener != null) {
                    COUISearchViewAnimate.this.mOnAnimationListener.onAnimationEnd(1);
                }
            }
        };
        private Runnable mToNormalStartRunnable = new Runnable() {
            @Override
            public void run() {
                COUISearchViewAnimate.this.setSearchAutoCompleteFocus();
                COUISearchViewAnimate.this.openSoftInput(false);
                if (COUISearchViewAnimate.this.mOnAnimationListener != null) {
                    COUISearchViewAnimate.this.mOnAnimationListener.onAnimationStart(0);
                }
                COUISearchViewAnimate.this.notifyOnStateChange(STATE_EDIT, STATE_NORMAL);
            }
        };
        private Runnable mToNormalEndRunnable = new Runnable() {
            @Override
            public void run() {
                COUISearchViewAnimate.this.setSearchAutoCompleteUnFocus();
                AnimatorHelper.this.mAnimatingAtomic.set(false);
                COUISearchViewAnimate.this.mSearchView.setQuery("", false);
                if (COUISearchViewAnimate.this.mOnAnimationListener != null) {
                    COUISearchViewAnimate.this.mOnAnimationListener.onAnimationEnd(0);
                }
            }
        };

        public AnimatorHelper() {
        }

        private void startCancelButtonEnterValueAnimator() {
            COUISearchViewAnimate.this.mFunctionalButton.setAlpha(0.0f);
            COUISearchViewAnimate.this.mFunctionalButton.setVisibility(0);
            ValueAnimator valueAnimatorOfFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
            valueAnimatorOfFloat.setDuration(DEFAULT_BUTTON_MOVE_DURATION);
            valueAnimatorOfFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float fFloatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                    if (COUISearchViewAnimate.this.mSearchViewType == TYPE_INSTANT_SEARCH) {
                        COUISearchViewAnimate.this.mFunctionalButton.setAlpha(fFloatValue);
                        COUISearchViewAnimate.this.mSearchViewShrinkWidth = (int) (fFloatValue * (COUISearchViewAnimate.this.getOriginWidth() - COUISearchViewAnimate.this.getShrinkWidth()));
                        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) COUISearchViewAnimate.this.mSearchView.getLayoutParams();
                        marginLayoutParams.setMarginEnd(COUISearchViewAnimate.this.mSearchViewShrinkWidth);
                        COUISearchViewAnimate.this.mSearchView.setLayoutParams(marginLayoutParams);
                    } else if (COUISearchViewAnimate.this.mSearchViewType == 1) {
                        COUISearchViewAnimate.this.mButtonDivider.setAlpha(fFloatValue);
                        COUISearchViewAnimate.this.mFunctionalButton.setAlpha(fFloatValue);
                    }
                    if (COUISearchViewAnimate.this.mOnAnimationListener != null) {
                        COUISearchViewAnimate.this.mOnAnimationListener.onUpdate(1, valueAnimator);
                    }
                }
            });
            valueAnimatorOfFloat.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animator) {
                    super.onAnimationEnd(animator);
                    AnimatorHelper.this.mToEditEndRunnable.run();
                }

                @Override
                public void onAnimationStart(Animator animator) {
                    super.onAnimationStart(animator);
                    AnimatorHelper.this.mToEditStartRunnable.run();
                }
            });
            valueAnimatorOfFloat.start();
        }

        private void startCancelButtonExitValueAnimator() {
            ValueAnimator valueAnimatorOfFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
            valueAnimatorOfFloat.setDuration(DEFAULT_BUTTON_MOVE_DURATION);
            valueAnimatorOfFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float fFloatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                    if (COUISearchViewAnimate.this.mSearchViewType == TYPE_INSTANT_SEARCH) {
                        float fraction = 1.0f - fFloatValue;
                        COUISearchViewAnimate.this.mFunctionalButton.setAlpha(fraction);
                        COUISearchViewAnimate.this.mSearchViewShrinkWidth = (int) (fraction * (COUISearchViewAnimate.this.getOriginWidth() - COUISearchViewAnimate.this.getShrinkWidth()));
                        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) COUISearchViewAnimate.this.mSearchView.getLayoutParams();
                        marginLayoutParams.setMarginEnd(COUISearchViewAnimate.this.mSearchViewShrinkWidth);
                        COUISearchViewAnimate.this.mSearchView.setLayoutParams(marginLayoutParams);
                    } else if (COUISearchViewAnimate.this.mSearchViewType == 1) {
                        float ratio = 1.0f - fFloatValue;
                        COUISearchViewAnimate.this.mButtonDivider.setAlpha(ratio);
                        COUISearchViewAnimate.this.mFunctionalButton.setAlpha(ratio);
                    }
                    if (COUISearchViewAnimate.this.mOnAnimationListener != null) {
                        COUISearchViewAnimate.this.mOnAnimationListener.onUpdate(0, valueAnimator);
                    }
                }
            });
            valueAnimatorOfFloat.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animator) {
                    super.onAnimationEnd(animator);
                    if (COUISearchViewAnimate.this.mSearchViewType == 1) {
                        COUISearchViewAnimate.this.mButtonDivider.setVisibility(8);
                        COUISearchViewAnimate.this.mFunctionalButton.setVisibility(8);
                    } else if (COUISearchViewAnimate.this.mSearchViewType == TYPE_INSTANT_SEARCH) {
                        COUISearchViewAnimate.this.mFunctionalButton.setVisibility(4);
                    }
                    AnimatorHelper.this.mToNormalEndRunnable.run();
                }

                @Override
                public void onAnimationStart(Animator animator) {
                    super.onAnimationStart(animator);
                    AnimatorHelper.this.mToNormalStartRunnable.run();
                }
            });
            valueAnimatorOfFloat.start();
        }

        private void startSearchIconFadeInAnimator() {
            if (COUISearchViewAnimate.this.mSearchIcon != null) {
                COUISearchViewAnimate.this.mSearchIcon.setPivotX(0.0f);
                COUISearchViewAnimate.this.mSearchIcon.setRotationY(0.0f);
                COUISearchViewAnimate.this.mSearchIcon.setVisibility(0);
                COUISearchViewAnimate.this.mSearchIcon.animate().setDuration(DEFAULT_FADE_DURATION).alpha(1.0f).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animator) {
                        super.onAnimationEnd(animator);
                        COUISearchViewAnimate.this.mSearchIcon.setVisibility(0);
                    }
                }).start();
            }
        }

        private void startSearchIconFadeOutAnimator() {
            if (COUISearchViewAnimate.this.mSearchIcon != null) {
                COUISearchViewAnimate.this.mSearchIcon.setPivotX(0.0f);
                COUISearchViewAnimate.this.mSearchIcon.setRotationY(0.0f);
                COUISearchViewAnimate.this.mSearchIcon.animate().setDuration(DEFAULT_FADE_DURATION).alpha(0.0f).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animator) {
                        super.onAnimationEnd(animator);
                        COUISearchViewAnimate.this.mSearchIcon.setVisibility(4);
                    }
                }).start();
            }
        }

        public void runStateChangeAnimation(int targetState) {
            if (COUISearchViewAnimate.this.mState.get() == targetState) {
                Log.d(COUISearchViewAnimate.TAG, "runStateChangeAnimation: same state , return. targetState = " + targetState);
                return;
            }
            if (targetState == STATE_EDIT) {
                startToEditAnimator();
            } else if (targetState == STATE_NORMAL) {
                startToNormalAnimator();
            }
        }

        public void startCancelButtonEnterAnimator() {
            if (COUISearchViewAnimate.this.mFunctionalButton != null) {
                COUISearchViewAnimate.this.mFunctionalButton.setAlpha(0.0f);
                if (COUISearchViewAnimate.this.mSearchViewType == 1) {
                    COUISearchViewAnimate.this.mButtonDivider.setAlpha(0.0f);
                    COUISearchViewAnimate.this.mButtonDivider.setVisibility(0);
                }
                COUISearchViewAnimate.this.mFunctionalButton.setVisibility(0);
                startCancelButtonEnterValueAnimator();
            }
        }

        public void startCancelButtonExitAnimator() {
            if (COUISearchViewAnimate.this.mFunctionalButton != null) {
                COUISearchViewAnimate.this.mFunctionalButton.setAlpha(1.0f);
                if (COUISearchViewAnimate.this.mSearchViewType == 1) {
                    COUISearchViewAnimate.this.mButtonDivider.setAlpha(1.0f);
                }
                if (COUISearchViewAnimate.this.mFunctionalButton.isPerformClicked()) {
                    COUISearchViewAnimate.this.mFunctionalButton.setPerformClicked(false);
                } else {
                    COUISearchViewAnimate.this.mFunctionalButton.setVisibility(0);
                    if (COUISearchViewAnimate.this.mSearchViewType == 1) {
                        COUISearchViewAnimate.this.mButtonDivider.setVisibility(0);
                    }
                }
                startCancelButtonExitValueAnimator();
            }
        }

        public void startSearchIconEnterAnimator() {
            if (COUISearchViewAnimate.this.mSearchIcon != null) {
                if (this.mIconTranslation == 0) {
                    if (COUISearchViewAnimate.this.isRtl()) {
                        this.mIconTranslation = (COUISearchViewAnimate.this.getWidth() - COUISearchViewAnimate.this.mSearchIcon.getRight()) + COUISearchViewAnimate.this.mSearchIcon.getWidth();
                    } else {
                        this.mIconTranslation = -COUISearchViewAnimate.this.mSearchIcon.getLeft();
                    }
                }
                COUISearchViewAnimate.this.mSearchIcon.setVisibility(0);
                COUISearchViewAnimate.this.mSearchIcon.setPivotX(this.mIconTranslation);
                COUISearchViewAnimate.this.mSearchIcon.setRotationY(80.0f);
                COUISearchViewAnimate.this.mSearchIcon.animate().setDuration(DEFAULT_FADE_DURATION).rotationY(0.0f).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animator) {
                        super.onAnimationEnd(animator);
                        COUISearchViewAnimate.this.mSearchIcon.setRotationY(0.0f);
                    }
                }).start();
            }
        }

        public void startSearchIconExitAnimator() {
            if (COUISearchViewAnimate.this.mSearchIcon != null) {
                if (this.mIconTranslation == 0) {
                    if (COUISearchViewAnimate.this.isRtl()) {
                        this.mIconTranslation = (COUISearchViewAnimate.this.getWidth() - COUISearchViewAnimate.this.mSearchIcon.getRight()) + COUISearchViewAnimate.this.mSearchIcon.getWidth();
                    } else {
                        this.mIconTranslation = -COUISearchViewAnimate.this.mSearchIcon.getLeft();
                    }
                }
                COUISearchViewAnimate.this.mSearchIcon.setPivotX(this.mIconTranslation);
                COUISearchViewAnimate.this.mSearchIcon.animate().setDuration(DEFAULT_FADE_DURATION).rotationY(80.0f).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animator) {
                        super.onAnimationEnd(animator);
                        COUISearchViewAnimate.this.mSearchIcon.setVisibility(8);
                    }
                }).start();
            }
        }

        public void startToEditAnimator() {
            synchronized (this) {
                try {
                    if (this.mAnimatingAtomic.compareAndSet(false, true)) {
                        if (!COUISearchViewAnimate.this.mIsAtLeastR || COUISearchViewAnimate.this.mShowImeAnimDuration == 0 || COUISearchViewAnimate.this.isInMultiWindowMode()) {
                            COUISearchViewAnimate.this.mState.set(STATE_EDIT);
                            COUISearchViewAnimate.this.mSearchViewSmoothEnterAnimatorSet.start();
                        } else {
                            COUISearchViewAnimate.this.setSearchAutoCompleteUnFocus();
                            COUISearchViewAnimate.this.openSoftInput(true);
                        }
                    }
                } catch (Throwable th) {
                    throw th;
                }
            }
        }

        public void startToNormalAnimator() {
            synchronized (this) {
                try {
                    if (this.mAnimatingAtomic.compareAndSet(false, true)) {
                        COUISearchViewAnimate.this.mSearchViewSmoothExitAnimatorSet.start();
                    }
                } catch (Throwable th) {
                    throw th;
                }
            }
        }
    }

    public static class COUISavedState extends View.BaseSavedState {
        public static final Parcelable.Creator<COUISavedState> CREATOR = new Parcelable.Creator<COUISavedState>() {

            @Override
            public COUISavedState createFromParcel(Parcel parcel) {
                return new COUISavedState(parcel);
            }


            @Override
            public COUISavedState[] newArray(int size) {
                return new COUISavedState[size];
            }
        };
        float mCollapsingHeightPercent;

        public COUISavedState(Parcelable parcelable) {
            super(parcelable);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public void readFromParcel(Parcel parcel) {
            this.mCollapsingHeightPercent = parcel.readFloat();
        }

        @Override
        public void writeToParcel(Parcel parcel, int flags) {
            super.writeToParcel(parcel, flags);
            parcel.writeFloat(this.mCollapsingHeightPercent);
        }

        public COUISavedState(Parcel parcel) {
            super(parcel);
            this.mCollapsingHeightPercent = parcel.readFloat();
        }
    }

    public interface OnAnimationListener {
        void onAnimationEnd(int state);

        void onAnimationStart(int state);

        void onUpdate(int state, ValueAnimator valueAnimator);
    }

    public interface OnCancelButtonClickListener {
        boolean onClickCancel();
    }

    public interface OnDispatchKeyEventPreImeListener {
        void onDispatchKeyEventPreIme(KeyEvent keyEvent);
    }

    public interface OnStateChangeListener {
        void onStateChange(int fromState, int toState);
    }

    public static class SearchFunctionalButton extends AppCompatButton {
        private static final int DEFAULT_MAX_LINES = 1;
        volatile boolean mIsPerformClicked;
        PerformClickCallback mPerformClickCallback;

        public interface PerformClickCallback {
            void performClick();
        }

        public SearchFunctionalButton(Context context) {
            this(context, null);
        }

        public boolean isPerformClicked() {
            return this.mIsPerformClicked;
        }

        @Override
        public boolean performClick() {
            if (this.mPerformClickCallback != null) {
                this.mIsPerformClicked = true;
                this.mPerformClickCallback.performClick();
            }
            return super.performClick();
        }

        public void setPerformClickCallback(PerformClickCallback performClickCallback) {
            this.mPerformClickCallback = performClickCallback;
        }

        public void setPerformClicked(boolean isPerformClicked) {
            this.mIsPerformClicked = isPerformClicked;
        }

        public SearchFunctionalButton(Context context, AttributeSet attributeSet) {
            this(context, attributeSet, 0);
        }

        public SearchFunctionalButton(Context context, AttributeSet attributeSet, int index) {
            super(context, attributeSet, index);
            this.mPerformClickCallback = null;
            this.mIsPerformClicked = false;
            setMaxLines(DEFAULT_MAX_LINES);
            setMaxWidth(context.getResources().getDimensionPixelOffset(R.dimen.coui_search_function_button_max_width));
            setEllipsize(TextUtils.TruncateAt.END);
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface SearchViewState {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface SearchViewType {
    }

    public COUISearchViewAnimate(Context context) {
        this(context, null);
    }


    public void changeStateWithOutAnimation(int targetState) {
        if (this.mState.get() == targetState) {
            Log.d(TAG, "changeStateWithOutAnimation: same state , return. targetState = " + targetState);
            return;
        }
        this.mState.set(targetState);
        Log.d(TAG, "changeStateWithOutAnimation: " + targetState);
        if (targetState == STATE_EDIT) {
            this.mSearchView.setAlpha(1.0f);
            this.mFunctionalButton.setAlpha(1.0f);
            this.mSearchView.setVisibility(0);
            this.mFunctionalButton.setVisibility(0);
            this.mSearchIcon.setVisibility(0);
            int count = this.mSearchViewType;
            if (count == 1) {
                this.mButtonDivider.setAlpha(1.0f);
            } else if (count == 0) {
                int originWidth = getOriginWidth() - getShrinkWidth();
                ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) this.mSearchView.getLayoutParams();
                marginLayoutParams.setMarginEnd(originWidth);
                this.mSearchView.setLayoutParams(marginLayoutParams);
            }
            getAnimatorHelper().mToEditStartRunnable.run();
            getAnimatorHelper().mToEditEndRunnable.run();
        } else {
            this.mSearchIcon.setAlpha(1.0f);
            this.mSearchIcon.setRotationY(0.0f);
            this.mSearchView.setQuery("", false);
            int value = this.mSearchViewType;
            if (value == 1) {
                this.mButtonDivider.setAlpha(0.0f);
                this.mFunctionalButton.setVisibility(8);
            } else if (value == 0) {
                ViewGroup.MarginLayoutParams marginLayoutParams2 = (ViewGroup.MarginLayoutParams) this.mSearchView.getLayoutParams();
                marginLayoutParams2.setMarginEnd(0);
                this.mSearchView.setLayoutParams(marginLayoutParams2);
                this.mFunctionalButton.setVisibility(4);
            }
            this.mSearchIcon.setVisibility(0);
            getAnimatorHelper().mToNormalStartRunnable.run();
            getAnimatorHelper().mToNormalEndRunnable.run();
        }
        requestLayout();
    }

    private float clampMarginValue(float fraction) {
        return Math.max(0.0f, Math.min(1.0f, fraction / CLAMP_ANIMATION_PERCENT));
    }

    private float clampProgress(float fraction, float ratio, float scale) {
        return Float.max(0.0f, Float.min((scale / (ratio - fraction)) + (fraction / (fraction - ratio)), 1.0f));
    }

    private float clampSearchViewHeight(float fraction) {
        return (fraction / 0.7f) - 0.42857146f;
    }

    private void ensureAddedToToolBar() {
        if (this.mHasAddedToToolbar) {
            return;
        }
        this.mHasAddedToToolbar = true;
        if (this.mToolBar != null) {
            removeLast();
            COUIToolbar.LayoutParams layoutParams = new COUIToolbar.LayoutParams(-1, this.mToolBar.getHeight() - this.mToolBar.getPaddingTop());
            layoutParams.gravity = this.mGravityInToolBar;
            this.mToolBar.setSearchView(this, layoutParams);
        }
    }

    private List ensureList(List list) {
        return list == null ? new ArrayList() : list;
    }


    public AnimatorHelper getAnimatorHelper() {
        if (this.mAnimatorHelper == null) {
            synchronized (this) {
                try {
                    if (this.mAnimatorHelper == null) {
                        this.mAnimatorHelper = new AnimatorHelper();
                    }
                } finally {
                }
            }
        }
        return this.mAnimatorHelper;
    }


    public int getOriginWidth() {
        return (((getMeasuredWidth() - getPaddingStart()) - getPaddingEnd()) - this.mBackgroundStartGap) - this.mBackgroundEndGap;
    }


    public int getShrinkWidth() {
        int index = this.mSearchViewType;
        return index == 0 ? ((getOriginWidth() - this.mCancelButtonLargeStartMargin) - this.mFunctionalButton.getMeasuredWidth()) + this.mFunctionalButton.getPaddingEnd() : index == 1 ? (((getOriginWidth() - this.mCancelButtonSmallStartMargin) - this.mBackgroundEndGap) - this.mFunctionalButton.getMeasuredWidth()) - this.mButtonDivider.getMeasuredWidth() : getOriginWidth();
    }

    private void inflateView(Context context, AttributeSet attributeSet) {
        View.inflate(context, R.layout.coui_search_view_animate_layout, this);
        this.mSearchIcon = (ImageView) findViewById(R.id.animated_search_icon);
        this.mSearchView = (COUISearchView) findViewById(R.id.animated_search_view);
        this.mFunctionalButton = (SearchFunctionalButton) findViewById(R.id.animated_cancel_button);
        this.mButtonDivider = (ImageView) findViewById(R.id.button_divider);
        this.mSearchViewWrapper = (ConstraintLayout) findViewById(R.id.coui_search_view_wrapper);
    }

    private void initAnimator() {
        initSearchViewEnterAnimator();
        initSearchViewExitAnimator();
        initButtonEnterAnimator();
        initButtonExitAnimator();
        initSmoothEnterAnimatorSet();
        initSmoothExitAnimatorSet();
    }

    private void initButtonEnterAnimator() {
        ValueAnimator valueAnimatorOfFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        this.mButtonAlphaEnterAnimator = valueAnimatorOfFloat;
        valueAnimatorOfFloat.setDuration(this.mSearchViewType == TYPE_INSTANT_SEARCH ? DEFAULT_BUTTON_ALPHA_CHANGE_DURATION : NON_INSTANT_SEARCH_BUTTON_ALPHA_CHANGE_DURATION);
        this.mButtonAlphaEnterAnimator.setInterpolator(DEFAULT_BUTTON_ALPHA_CHANGE_INTERPOLATOR);
        this.mButtonAlphaEnterAnimator.setStartDelay(this.mSearchViewType != 0 ? 0L : 100L);
        this.mButtonAlphaEnterAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float fFloatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                if (COUISearchViewAnimate.this.mSearchViewType == TYPE_INSTANT_SEARCH) {
                    COUISearchViewAnimate.this.mFunctionalButton.setAlpha(fFloatValue);
                } else if (COUISearchViewAnimate.this.mSearchViewType == 1) {
                    COUISearchViewAnimate.this.mButtonDivider.setAlpha(fFloatValue);
                    COUISearchViewAnimate.this.mFunctionalButton.setAlpha(fFloatValue);
                }
            }
        });
        this.mButtonAlphaEnterAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (COUISearchViewAnimate.this.mSearchViewType == 1) {
                    COUISearchViewAnimate.this.mDivideBackground = true;
                }
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }

            @Override
            public void onAnimationStart(Animator animator) {
                if (COUISearchViewAnimate.this.mSearchViewType == 1) {
                    COUISearchViewAnimate.this.mButtonDivider.setVisibility(0);
                }
                COUISearchViewAnimate.this.mFunctionalButton.setVisibility(0);
            }
        });
    }

    private void initButtonExitAnimator() {
        ValueAnimator valueAnimatorOfFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        this.mButtonAlphaExitAnimator = valueAnimatorOfFloat;
        valueAnimatorOfFloat.setDuration(this.mSearchViewType == TYPE_INSTANT_SEARCH ? DEFAULT_BUTTON_ALPHA_CHANGE_DURATION : NON_INSTANT_SEARCH_BUTTON_ALPHA_CHANGE_DURATION);
        this.mButtonAlphaExitAnimator.setInterpolator(DEFAULT_BUTTON_ALPHA_CHANGE_INTERPOLATOR);
        this.mButtonAlphaExitAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float fFloatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                if (COUISearchViewAnimate.this.mSearchViewType == TYPE_INSTANT_SEARCH) {
                    COUISearchViewAnimate.this.mFunctionalButton.setAlpha(1.0f - fFloatValue);
                } else if (COUISearchViewAnimate.this.mSearchViewType == 1) {
                    float fraction = 1.0f - fFloatValue;
                    COUISearchViewAnimate.this.mButtonDivider.setAlpha(fraction);
                    COUISearchViewAnimate.this.mFunctionalButton.setAlpha(fraction);
                }
            }
        });
    }

    private void initSearchViewEnterAnimator() {
        ValueAnimator valueAnimatorOfFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        this.mSearchViewOffsetEnterAnimator = valueAnimatorOfFloat;
        valueAnimatorOfFloat.setDuration(DEFAULT_SEARCH_VIEW_OFFSET_CHANGE_DURATION);
        this.mSearchViewOffsetEnterAnimator.setInterpolator(DEFAULT_SEARCH_VIEW_OFFSET_CHANGE_INTERPOLATOR);
        this.mSearchViewOffsetEnterAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                COUISearchViewAnimate.this.lambda$initSearchViewEnterAnimator$0(valueAnimator);
            }
        });
        this.mSearchViewOffsetEnterAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }

            @Override
            public void onAnimationStart(Animator animator) {
                COUISearchViewAnimate.this.mTopOffset = 0;
                COUISearchViewAnimate cOUISearchViewAnimate = COUISearchViewAnimate.this;
                cOUISearchViewAnimate.mStartY = cOUISearchViewAnimate.getTop();
            }
        });
        ValueAnimator valueAnimatorOfFloat2 = ValueAnimator.ofFloat(0.0f, 1.0f);
        this.mSearchViewScaleEnterAnimator = valueAnimatorOfFloat2;
        valueAnimatorOfFloat2.setDuration(DEFAULT_SEARCH_VIEW_SCALE_CHANGE_DURATION);
        this.mSearchViewScaleEnterAnimator.setInterpolator(DEFAULT_SEARCH_VIEW_SCALE_CHANGE_INTERPOLATOR);
        this.mSearchViewScaleEnterAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                COUISearchViewAnimate.this.lambda$initSearchViewEnterAnimator$1(valueAnimator);
            }
        });
        this.mSearchViewScaleEnterAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationCancel(Animator animator) {
                COUISearchViewAnimate.this.updateBackgroundRect();
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                COUISearchViewAnimate.this.updateBackgroundRect();
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }

            @Override
            public void onAnimationStart(Animator animator) {
            }
        });
    }

    private void initSearchViewExitAnimator() {
        ValueAnimator valueAnimatorOfFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        this.mSearchViewOffsetExitAnimator = valueAnimatorOfFloat;
        valueAnimatorOfFloat.setDuration(DEFAULT_SEARCH_VIEW_OFFSET_CHANGE_DURATION);
        ValueAnimator valueAnimator = this.mSearchViewOffsetExitAnimator;
        Interpolator interpolator = DEFAULT_SEARCH_VIEW_OFFSET_CHANGE_INTERPOLATOR;
        valueAnimator.setInterpolator(interpolator);
        this.mSearchViewOffsetExitAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator2) {
                float fFloatValue = ((Float) valueAnimator2.getAnimatedValue()).floatValue();
                if (COUISearchViewAnimate.this.mSearchViewType == TYPE_INSTANT_SEARCH) {
                    int topOffset = (int) (fFloatValue * (COUISearchViewAnimate.this.mStartY - COUISearchViewAnimate.this.mExtraY));
                    ((ViewGroup.MarginLayoutParams) COUISearchViewAnimate.this.getLayoutParams()).topMargin += topOffset - COUISearchViewAnimate.this.mTopOffset;
                    COUISearchViewAnimate.this.requestLayout();
                    COUISearchViewAnimate.this.mTopOffset = topOffset;
                }
            }
        });
        ValueAnimator valueAnimatorOfFloat2 = ValueAnimator.ofFloat(0.0f, 1.0f);
        this.mSearchViewScaleExitAnimator = valueAnimatorOfFloat2;
        valueAnimatorOfFloat2.setDuration(DEFAULT_SEARCH_VIEW_SCALE_CHANGE_DURATION);
        this.mSearchViewScaleExitAnimator.setInterpolator(interpolator);
        this.mSearchViewScaleExitAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator2) {
                float fFloatValue = ((Float) valueAnimator2.getAnimatedValue()).floatValue();
                if (COUISearchViewAnimate.this.mSearchViewType == TYPE_INSTANT_SEARCH) {
                    COUISearchViewAnimate.this.mSearchViewShrinkWidth = (int) ((1.0f - fFloatValue) * (COUISearchViewAnimate.this.getOriginWidth() - COUISearchViewAnimate.this.getShrinkWidth()));
                    ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) COUISearchViewAnimate.this.mSearchView.getLayoutParams();
                    marginLayoutParams.setMarginEnd(COUISearchViewAnimate.this.mSearchViewShrinkWidth);
                    COUISearchViewAnimate.this.mSearchView.setLayoutParams(marginLayoutParams);
                }
            }
        });
    }

    private void initSmoothEnterAnimatorSet() {
        AnimatorSet animatorSet = new AnimatorSet();
        this.mSearchViewSmoothEnterAnimatorSet = animatorSet;
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationCancel(Animator animator) {
                COUISearchViewAnimate.this.updateBackgroundRect();
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                COUISearchViewAnimate.this.mTopOffset = 0;
                if (COUISearchViewAnimate.this.mSearchViewType == 1) {
                    COUISearchViewAnimate.this.mDivideBackground = true;
                }
                COUISearchViewAnimate.this.updateBackgroundRect();
                COUISearchViewAnimate.this.setSearchAutoCompleteFocus();
                COUISearchViewAnimate.this.getAnimatorHelper().mAnimatingAtomic.set(false);
                if (COUISearchViewAnimate.this.mOnAnimationListener != null) {
                    COUISearchViewAnimate.this.mOnAnimationListener.onAnimationEnd(1);
                }
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }

            @Override
            public void onAnimationStart(Animator animator) {
                COUISearchViewAnimate.this.mTopOffset = 0;
                if (COUISearchViewAnimate.this.mSearchViewType == 1) {
                    COUISearchViewAnimate.this.mButtonDivider.setVisibility(0);
                }
                COUISearchViewAnimate.this.mFunctionalButton.setVisibility(0);
                COUISearchViewAnimate.this.mState.set(STATE_EDIT);
                if (!COUISearchViewAnimate.this.mIsAtLeastR || COUISearchViewAnimate.this.mShowImeAnimDuration == 0 || COUISearchViewAnimate.this.isInMultiWindowMode()) {
                    COUISearchViewAnimate.this.setSearchAutoCompleteUnFocus();
                    COUISearchViewAnimate.this.openSoftInput(true);
                }
                COUISearchViewAnimate.this.notifyOnStateChange(STATE_NORMAL, STATE_EDIT);
                COUISearchViewAnimate cOUISearchViewAnimate = COUISearchViewAnimate.this;
                cOUISearchViewAnimate.mStartY = cOUISearchViewAnimate.getTop();
                if (COUISearchViewAnimate.this.mOnAnimationListener != null) {
                    COUISearchViewAnimate.this.mOnAnimationListener.onAnimationStart(1);
                }
            }
        });
        this.mSearchViewSmoothEnterAnimatorSet.playTogether(this.mSearchViewOffsetEnterAnimator, this.mSearchViewScaleEnterAnimator, this.mButtonAlphaEnterAnimator);
    }

    private void initSmoothExitAnimatorSet() {
        AnimatorSet animatorSet = new AnimatorSet();
        this.mSearchViewSmoothExitAnimatorSet = animatorSet;
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationCancel(Animator animator) {
                COUISearchViewAnimate.this.updateBackgroundRect();
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                COUISearchViewAnimate.this.mTopOffset = 0;
                if (COUISearchViewAnimate.this.mSearchViewType == 1) {
                    COUISearchViewAnimate.this.mDivideBackground = false;
                    COUISearchViewAnimate.this.mButtonDivider.setVisibility(8);
                    COUISearchViewAnimate.this.mFunctionalButton.setVisibility(8);
                } else if (COUISearchViewAnimate.this.mSearchViewType == TYPE_INSTANT_SEARCH) {
                    COUISearchViewAnimate.this.mFunctionalButton.setVisibility(4);
                }
                COUISearchViewAnimate.this.updateBackgroundRect();
                COUISearchViewAnimate.this.setSearchAutoCompleteUnFocus();
                COUISearchViewAnimate.this.getAnimatorHelper().mAnimatingAtomic.set(false);
                COUISearchViewAnimate.this.mSearchView.setQuery("", false);
                if (COUISearchViewAnimate.this.mOnAnimationListener != null) {
                    COUISearchViewAnimate.this.mOnAnimationListener.onAnimationEnd(0);
                }
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }

            @Override
            public void onAnimationStart(Animator animator) {
                COUISearchViewAnimate.this.mTopOffset = 0;
                COUISearchViewAnimate.this.mSearchView.getSearchAutoComplete().setText((CharSequence) null);
                COUISearchViewAnimate.this.mSearchView.getSearchAutoComplete().clearFocus();
                COUISearchViewAnimate.this.openSoftInput(false);
                COUISearchViewAnimate.this.mState.set(STATE_NORMAL);
                COUISearchViewAnimate.this.notifyOnStateChange(STATE_EDIT, STATE_NORMAL);
                if (COUISearchViewAnimate.this.mOnAnimationListener != null) {
                    COUISearchViewAnimate.this.mOnAnimationListener.onAnimationStart(0);
                }
            }
        });
        this.mSearchViewSmoothExitAnimatorSet.playTogether(this.mSearchViewOffsetExitAnimator, this.mSearchViewScaleExitAnimator, this.mButtonAlphaExitAnimator);
    }

    private boolean isInButton(float fraction, float ratio) {
        return this.mButtonHitRect.contains(fraction, ratio);
    }

    private boolean isInIcon(float fraction, float ratio) {
        getGlobalVisibleRect(this.mRect);
        this.mMainIcon.getGlobalVisibleRect(this.mMainIconRect);
        this.mSubIcon.getGlobalVisibleRect(this.mSubIconRect);
        this.mMainIconRect.offset(0, -this.mRect.top);
        this.mSubIconRect.offset(0, -this.mRect.top);
        int index = (int) fraction;
        int count = (int) ratio;
        return this.mMainIconRect.contains(index, count) || this.mSubIconRect.contains(index, count);
    }


    public boolean isInMultiWindowMode() {
        Context context = this.mContext;
        if (context instanceof Activity) {
            return ((Activity) context).isInMultiWindowMode();
        }
        return false;
    }

    private boolean isInView(float fraction, float ratio) {
        float scale = (int) fraction;
        float y = (int) ratio;
        return this.mPressFeedbackBackgroundRect.contains(scale, y) || this.mNormalBackgroundRect.contains(scale, y);
    }


    public boolean isRtl() {
        return getLayoutDirection() == 1;
    }


    public void lambda$initSearchViewEnterAnimator$0(ValueAnimator valueAnimator) {
        float fFloatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        if (this.mSearchViewType == TYPE_INSTANT_SEARCH) {
            int topOffset = (int) (fFloatValue * (this.mStartY - this.mExtraY));
            ((ViewGroup.MarginLayoutParams) getLayoutParams()).topMargin -= topOffset - this.mTopOffset;
            requestLayout();
            this.mTopOffset = topOffset;
        }
    }


    public void lambda$initSearchViewEnterAnimator$1(ValueAnimator valueAnimator) {
        float fFloatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        if (this.mSearchViewType == TYPE_INSTANT_SEARCH) {
            this.mSearchViewShrinkWidth = (int) (fFloatValue * (getOriginWidth() - getShrinkWidth()));
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) this.mSearchView.getLayoutParams();
            marginLayoutParams.setMarginEnd(this.mSearchViewShrinkWidth);
            this.mSearchView.setLayoutParams(marginLayoutParams);
        }
    }


    public void lambda$setImeInsetsAnimationCallback$2(ImeInsetsAnimationCallback imeInsetsAnimationCallback) {
        ViewCompat.setWindowInsetsAnimationCallback(this.mSearchView.getRootView(), imeInsetsAnimationCallback);
    }

    private void loadAttr(Context context, AttributeSet attributeSet, int style, int index) {
        Drawable drawable;
        if (attributeSet != null) {
            int styleAttribute = attributeSet.getStyleAttribute();
            this.mStyle = styleAttribute;
            if (styleAttribute == 0) {
                this.mStyle = style;
            }
        } else {
            this.mStyle = style;
        }
        this.mBackgroundEndGap = context.getResources().getDimensionPixelOffset(R.dimen.coui_search_view_background_end_gap);
        this.mBackgroundStartGap = context.getResources().getDimensionPixelOffset(R.dimen.coui_search_view_background_start_gap);
        this.mCancelButtonSmallStartMargin = context.getResources().getDimensionPixelOffset(R.dimen.coui_search_view_cancel_margin_small);
        this.mCancelButtonLargeStartMargin = context.getResources().getDimensionPixelOffset(R.dimen.coui_search_view_cancel_margin_large);
        this.mCollapsedMinHeight = context.getResources().getDimensionPixelSize(R.dimen.coui_search_view_collapsed_min_height);
        this.mInitSearchViewWrapperHeight = context.getResources().getDimensionPixelOffset(R.dimen.coui_search_view_wrapper_height);
        this.mInitSearchViewAnimateHeight = context.getResources().getDimensionPixelOffset(R.dimen.coui_search_view_animate_height);
        if (this.mPressFeedbackBackgroundRect == null) {
            this.mPressFeedbackBackgroundRect = new RectF();
        }
        if (this.mNormalBackgroundRect == null) {
            this.mNormalBackgroundRect = new RectF();
        }
        if (this.mRect == null) {
            this.mRect = new Rect();
        }
        if (this.mMainIconRect == null) {
            this.mMainIconRect = new Rect();
        }
        if (this.mSubIconRect == null) {
            this.mSubIconRect = new Rect();
        }
        this.mNormalBackgroundColor = context.getResources().getColor(R.color.coui_search_view_selector_color_normal);
        this.mPressedBackgroundColor = context.getResources().getColor(R.color.coui_search_view_selector_color_pressed);
        this.mCurrentBackgroundColor = this.mNormalBackgroundColor;
        this.mHorizontalDividerColor = context.getResources().getColor(R.color.coui_color_divider);
        TypedArray typedArrayObtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.COUISearchViewAnimate, style, index);
        float fraction = context.getResources().getConfiguration().fontScale;
        this.mSearchView.getSearchAutoComplete().setTextSize(0, typedArrayObtainStyledAttributes.getDimensionPixelSize(R.styleable.COUISearchViewAnimate_inputTextSize, getResources().getDimensionPixelSize(R.dimen.coui_search_view_input_text_size)));
        SearchView.SearchAutoComplete searchAutoComplete = this.mSearchView.getSearchAutoComplete();
        searchAutoComplete.setPaddingRelative(0, 0, getResources().getDimensionPixelSize(R.dimen.coui_search_view_auto_complete_padding_end), 0);
        searchAutoComplete.setTextColor(typedArrayObtainStyledAttributes.getColor(R.styleable.COUISearchViewAnimate_inputTextColor, 0));
        this.mSearchIcon.setImageDrawable(typedArrayObtainStyledAttributes.getDrawable(R.styleable.COUISearchViewAnimate_couiSearchIcon));
        this.mSearchView.getSearchAutoComplete().setHintTextColor(typedArrayObtainStyledAttributes.getColorStateList(R.styleable.COUISearchViewAnimate_normalHintColor));
        this.mSearchViewType = typedArrayObtainStyledAttributes.getInt(R.styleable.COUISearchViewAnimate_couiSearchViewAnimateType, 0);
        int value = R.styleable.COUISearchViewAnimate_searchHint;
        if (typedArrayObtainStyledAttributes.hasValue(value)) {
            setQueryHint(typedArrayObtainStyledAttributes.getString(value));
        }
        int offset = R.styleable.COUISearchViewAnimate_functionalButtonTextColor;
        if (typedArrayObtainStyledAttributes.hasValue(offset)) {
            this.mFunctionalButton.setTextColor(typedArrayObtainStyledAttributes.getColor(offset, 0));
        }
        int delta = R.styleable.COUISearchViewAnimate_functionalButtonText;
        if (typedArrayObtainStyledAttributes.hasValue(delta)) {
            this.mFunctionalButton.setText(typedArrayObtainStyledAttributes.getString(delta));
        } else {
            this.mFunctionalButton.setText(R.string.coui_search_view_cancel);
        }
        this.mFunctionalButton.setTextSize(0, COUIChangeTextUtil.getSuitableFontSize(this.mFunctionalButton.getTextSize(), fraction, 2));
        COUITextViewCompatUtil.setPressRippleDrawable(this.mFunctionalButton);
        int start = R.styleable.COUISearchViewAnimate_buttonDivider;
        if (typedArrayObtainStyledAttributes.hasValue(start) && (drawable = typedArrayObtainStyledAttributes.getDrawable(start)) != null) {
            this.mButtonDivider.setImageDrawable(drawable);
        }
        this.mSearchView.setBackgroundColor(typedArrayObtainStyledAttributes.getColor(R.styleable.COUISearchViewAnimate_searchBackground, 0));
        this.mMainIcon = (ImageView) this.mSearchView.findViewById(R.id.search_main_icon_btn);
        this.mSubIcon = (ImageView) this.mSearchView.findViewById(R.id.search_sub_icon_btn);
        this.mMainIcon.setImageDrawable(typedArrayObtainStyledAttributes.getDrawable(R.styleable.COUISearchViewAnimate_couiSearchViewMainIcon));
        this.mSubIcon.setImageDrawable(typedArrayObtainStyledAttributes.getDrawable(R.styleable.COUISearchViewAnimate_couiSearchViewSubIcon));
        this.mCloseBtn = (ImageView) this.mSearchView.findViewById(R.id.search_close_btn);
        int resourceId = typedArrayObtainStyledAttributes.getResourceId(R.styleable.COUISearchViewAnimate_couiSearchClearSelector, 0);
        ImageView imageView = this.mCloseBtn;
        if (imageView != null) {
            imageView.setImageResource(resourceId);
        }
        int end = typedArrayObtainStyledAttributes.getInt(R.styleable.COUISearchViewAnimate_android_gravity, 16);
        Log.i(TAG, "gravity " + end);
        setGravity(end);
        typedArrayObtainStyledAttributes.recycle();
    }

    private boolean notifyCancelButton() {
        List<OnCancelButtonClickListener> list = this.mOnCancelButtonClickListeners;
        boolean zOnClickCancel = false;
        if (list != null) {
            for (OnCancelButtonClickListener onCancelButtonClickListener : list) {
                if (onCancelButtonClickListener != null) {
                    zOnClickCancel |= onCancelButtonClickListener.onClickCancel();
                }
            }
        }
        return zOnClickCancel;
    }


    public void notifyOnStateChange(int index, int count) {
        List<OnStateChangeListener> list = this.mOnStateChangeListeners;
        if (list != null) {
            for (OnStateChangeListener onStateChangeListener : list) {
                if (onStateChangeListener != null) {
                    onStateChangeListener.onStateChange(index, count);
                }
            }
        }
    }

    private void onClickStateEdit() {
        getAnimatorHelper().runStateChangeAnimation(0);
    }

    private void onClickStateNormal() {
        getAnimatorHelper().runStateChangeAnimation(1);
    }

    private void playPressAnimation() {
        ValueAnimator animator = this.mPressFeedbackAnimator;
        if (animator != null && animator.isRunning()) {
            this.mPressFeedbackAnimator.cancel();
        }
        animator = createBackgroundColorAnimator(this.mCurrentBackgroundColor, this.mPressedBackgroundColor);
        this.mPressFeedbackAnimator = animator;
        animator.setDuration(DEFAULT_PRESS_FEEDBACK_DURATION);
        this.mPressFeedbackAnimator.setInterpolator(DEFAULT_PRESS_FEEDBACK_INTERPOLATOR);
        this.mPressFeedbackAnimator.setEvaluator(DEFAULT_PRESS_FEEDBACK_EVALUATOR);
        this.mPressFeedbackAnimator.start();
    }

    private void playReleaseAnimation() {
        ValueAnimator animator = this.mPressFeedbackAnimator;
        if (animator != null && animator.isRunning()) {
            this.mPressFeedbackAnimator.cancel();
        }
        animator = createBackgroundColorAnimator(this.mCurrentBackgroundColor, this.mNormalBackgroundColor);
        this.mPressFeedbackAnimator = animator;
        animator.setDuration(DEFAULT_PRESS_FEEDBACK_DURATION);
        this.mPressFeedbackAnimator.setInterpolator(DEFAULT_PRESS_FEEDBACK_INTERPOLATOR);
        this.mPressFeedbackAnimator.setEvaluator(DEFAULT_PRESS_FEEDBACK_EVALUATOR);
        this.mPressFeedbackAnimator.start();
    }

    private ValueAnimator createBackgroundColorAnimator(int startColor, int endColor) {
        ValueAnimator animator = ValueAnimator.ofInt(startColor, endColor);
        animator.addUpdateListener(animation -> setCurrentBackgroundColor((Integer) animation.getAnimatedValue()));
        return animator;
    }

    private void removeLast() {
        int childCount = this.mToolBar.getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (getClass().isInstance(this.mToolBar.getChildAt(i))) {
                this.mToolBar.removeViewAt(i);
                return;
            }
        }
    }

    private void setCurrentBackgroundColor(int color) {
        this.mCurrentBackgroundColor = color;
        invalidate();
    }

    private void setMenuItem(MenuItem menuItem) {
        this.mMenuItem = menuItem;
        if (menuItem == null || menuItem.getActionView() != this) {
            return;
        }
        this.mMenuItem.setActionView((View) null);
    }

    private void setRelativeVerticalGravity(View view, int index) {
        ViewGroup.LayoutParams layoutParams;
        if (view == null || (layoutParams = view.getLayoutParams()) == null || !(layoutParams instanceof RelativeLayout.LayoutParams)) {
            return;
        }
        RelativeLayout.LayoutParams layoutParams2 = (RelativeLayout.LayoutParams) layoutParams;
        Arrays.fill(layoutParams2.getRules(), 0);
        layoutParams2.alignWithParent = true;
        int count = index & 112;
        int offset = 15;
        if (count != 16) {
            if (count == 48) {
                offset = 10;
            } else if (count == 80) {
                offset = 12;
            }
        }
        layoutParams2.addRule(offset);
        view.requestLayout();
    }


    public void setSearchAutoCompleteFocus() {
        SearchView.SearchAutoComplete searchAutoComplete;
        COUISearchView cOUISearchView = this.mSearchView;
        if (cOUISearchView == null || (searchAutoComplete = cOUISearchView.getSearchAutoComplete()) == null) {
            return;
        }
        searchAutoComplete.setFocusable(true);
        searchAutoComplete.setFocusableInTouchMode(true);
        searchAutoComplete.requestFocus();
    }


    public void setSearchAutoCompleteUnFocus() {
        COUISearchView cOUISearchView = this.mSearchView;
        if (cOUISearchView != null) {
            cOUISearchView.clearFocus();
            this.mSearchView.setFocusable(false);
            this.mSearchView.onWindowFocusChanged(false);
            SearchView.SearchAutoComplete searchAutoComplete = this.mSearchView.getSearchAutoComplete();
            if (searchAutoComplete != null) {
                searchAutoComplete.setFocusable(false);
            }
        }
    }


    public void setToolBarAlpha(float fraction) {
        COUIToolbar cOUIToolbar = this.mToolBar;
        if (cOUIToolbar != null) {
            int childCount = cOUIToolbar.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View childAt = this.mToolBar.getChildAt(i);
                if (childAt != this) {
                    childAt.setAlpha(fraction);
                }
            }
        }
    }


    public void setToolBarChildVisibility(int index) {
        COUIToolbar cOUIToolbar = this.mToolBar;
        if (cOUIToolbar != null) {
            int childCount = cOUIToolbar.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View childAt = this.mToolBar.getChildAt(i);
                if (childAt != this) {
                    childAt.setVisibility(index);
                }
            }
        }
    }

    private void showSoftInput() {
        CustomWindowInsetsAnimationControlListener customWindowInsetsAnimationControlListener = new CustomWindowInsetsAnimationControlListener(true, this.mShowImeAnimDuration, this.mShowImeInterpolator);
        COUISearchView cOUISearchView = this.mSearchView;
        if (cOUISearchView == null || cOUISearchView.getWindowInsetsController() == null) {
            return;
        }
        this.mSearchView.getWindowInsetsController().controlWindowInsetsAnimation(WindowInsets.Type.ime(), this.mShowImeAnimDuration, this.mShowImeInterpolator, null, customWindowInsetsAnimationControlListener);
    }

    private static String state2String(int index) {
        return index != 0 ? index != 1 ? String.valueOf(index) : "state edit" : "state normal";
    }


    public void updateBackgroundRect() {
        if (!this.mDivideBackground) {
            if (isRtl()) {
                this.mPressFeedbackBackgroundRect.right = this.mSearchViewWrapper.getRight();
                int index = this.mSearchViewType;
                if (index == 0) {
                    this.mPressFeedbackBackgroundRect.left = this.mSearchView.getLeft() + getPaddingEnd();
                } else if (index == 1) {
                    this.mPressFeedbackBackgroundRect.left = this.mSearchViewWrapper.getLeft();
                }
            } else {
                this.mPressFeedbackBackgroundRect.left = this.mSearchViewWrapper.getLeft();
                int count = this.mSearchViewType;
                if (count == 0) {
                    this.mPressFeedbackBackgroundRect.right = this.mSearchView.getRight() + getPaddingStart();
                } else if (count == 1) {
                    this.mPressFeedbackBackgroundRect.right = this.mSearchViewWrapper.getRight();
                }
            }
            this.mPressFeedbackBackgroundRect.top = this.mSearchViewWrapper.getTop();
            this.mPressFeedbackBackgroundRect.bottom = this.mSearchViewWrapper.getBottom();
            this.mNeedUpdatePressFeedbackRectPath = true;
            return;
        }
        if (isRtl()) {
            this.mPressFeedbackBackgroundRect.right = this.mSearchViewWrapper.getRight();
            this.mPressFeedbackBackgroundRect.left = this.mButtonDivider.getRight() + this.mSearchViewWrapper.getLeft();
        } else {
            this.mPressFeedbackBackgroundRect.left = this.mSearchViewWrapper.getLeft();
            this.mPressFeedbackBackgroundRect.right = this.mButtonDivider.getLeft() + this.mPressFeedbackBackgroundRect.left;
        }
        this.mPressFeedbackBackgroundRect.top = this.mSearchViewWrapper.getTop();
        this.mPressFeedbackBackgroundRect.bottom = this.mSearchViewWrapper.getBottom();
        this.mNeedUpdatePressFeedbackRectPath = true;
        if (isRtl()) {
            RectF rectF = this.mNormalBackgroundRect;
            rectF.right = this.mPressFeedbackBackgroundRect.left;
            rectF.left = this.mSearchViewWrapper.getLeft();
        } else {
            RectF rectF2 = this.mNormalBackgroundRect;
            rectF2.left = this.mPressFeedbackBackgroundRect.right;
            rectF2.right = this.mSearchViewWrapper.getRight();
        }
        RectF rectF3 = this.mNormalBackgroundRect;
        RectF rectF4 = this.mPressFeedbackBackgroundRect;
        rectF3.top = rectF4.top;
        rectF3.bottom = rectF4.bottom;
        this.mNeedUpdateNormalRectPath = true;
    }

    private void updateBounds() {
        this.mFunctionalButton.getLocationOnScreen(this.mButtonLocation);
        getLocationOnScreen(this.mLocation);
        int left = this.mButtonLocation[0] - this.mLocation[0];
        int top = this.mButtonLocation[1] - this.mLocation[1];
        this.mButtonHitRect.set(left, top, left + this.mFunctionalButton.getWidth(), top + this.mFunctionalButton.getHeight());
        this.mFunctionalButton.post(new Runnable() {
            @Override
            public void run() {
                COUISearchViewAnimate.this.mFunctionalButton.getHitRect(COUISearchViewAnimate.this.mWrapperBounds);
                COUISearchViewAnimate.this.mWrapperBounds.right += COUISearchViewAnimate.this.getPaddingStart();
                COUISearchViewAnimate.this.mWrapperBounds.left += COUISearchViewAnimate.this.getPaddingStart();
                COUISearchViewAnimate.this.mWrapperBounds.top += COUISearchViewAnimate.this.mSearchViewWrapper.getTop();
                COUISearchViewAnimate.this.mWrapperBounds.bottom += COUISearchViewAnimate.this.mSearchViewWrapper.getTop();
                float fMax = Float.max(0.0f, COUISearchViewAnimate.this.mSearchViewWrapper.getMeasuredHeight() - COUISearchViewAnimate.this.mWrapperBounds.height());
                float fraction = fMax / 2.0f;
                COUISearchViewAnimate.this.mWrapperBounds.top = (int) (COUISearchViewAnimate.this.mWrapperBounds.top - fraction);
                COUISearchViewAnimate.this.mWrapperBounds.bottom = (int) (COUISearchViewAnimate.this.mWrapperBounds.bottom + fraction);
            }
        });
    }

    private void updatePath() {
        RectF rectF = this.mPressFeedbackBackgroundRect;
        float fraction = (rectF.bottom - rectF.top) / 2.0f;
        boolean zIsRtl = isRtl();
        if (this.mNeedUpdateNormalRectPath) {
            COUIShapePath.getRoundRectPath(this.mNormalBackgroundPath, this.mNormalBackgroundRect, fraction, zIsRtl, !zIsRtl, zIsRtl, !zIsRtl);
            this.mNeedUpdateNormalRectPath = false;
        }
        if (this.mNeedUpdatePressFeedbackRectPath) {
            if (this.mDivideBackground) {
                COUIShapePath.getRoundRectPath(this.mPressFeedbackBackgroundPath, this.mPressFeedbackBackgroundRect, fraction, !zIsRtl, zIsRtl, !zIsRtl, zIsRtl);
            } else {
                COUIShapePath.getRoundRectPath(this.mPressFeedbackBackgroundPath, this.mPressFeedbackBackgroundRect, fraction, true, true, true, true);
            }
            this.mNeedUpdatePressFeedbackRectPath = false;
        }
    }

    @Deprecated
    public void addOnCancelButtonClickListener(OnCancelButtonClickListener onCancelButtonClickListener) {
        List<OnCancelButtonClickListener> listEnsureList = ensureList(this.mOnCancelButtonClickListeners);
        this.mOnCancelButtonClickListeners = listEnsureList;
        listEnsureList.add(onCancelButtonClickListener);
    }

    public void addOnStateChangeListener(OnStateChangeListener onStateChangeListener) {
        List<OnStateChangeListener> listEnsureList = ensureList(this.mOnStateChangeListeners);
        this.mOnStateChangeListeners = listEnsureList;
        listEnsureList.add(onStateChangeListener);
    }

    @Override
    public void addView(View view) {
        super.addView(view);
    }

    public void changeStateImmediately(final int index) {
        Log.d(TAG, "changeStateImmediately: " + state2String(index));
        post(new Runnable() {
            @Override
            public void run() {
                COUISearchViewAnimate.this.changeStateWithOutAnimation(index);
            }
        });
    }

    public void changeStateWithAnimation(int index) {
        if (this.mState.get() == index) {
            Log.d(TAG, "changeStateWithAnimation: same state , return. targetState = " + index);
            return;
        }
        if (this.mState.get() == 1) {
            onClickStateEdit();
        } else if (this.mState.get() == 0) {
            onClickStateNormal();
        }
    }

    public void controlImeShowAnim(int showImeAnimDuration, Interpolator interpolator) {
        this.mShowImeAnimDuration = showImeAnimDuration;
        this.mShowImeInterpolator = interpolator;
    }

    @Override
    public boolean dispatchKeyEventPreIme(KeyEvent keyEvent) {
        OnDispatchKeyEventPreImeListener onDispatchKeyEventPreImeListener = this.mOnDispatchKeyEventPreImeListener;
        if (onDispatchKeyEventPreImeListener != null) {
            onDispatchKeyEventPreImeListener.onDispatchKeyEventPreIme(keyEvent);
        }
        return super.dispatchKeyEventPreIme(keyEvent);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        if (!isEnabled()) {
            return super.dispatchTouchEvent(motionEvent);
        }
        int action = motionEvent.getAction();
        if (action != 0) {
            if (action == 1 || action == 3) {
                if (isInView(motionEvent.getX(), motionEvent.getY()) || this.mPressed) {
                    this.mPressed = false;
                    playReleaseAnimation();
                }
            } else if (!isInView(motionEvent.getX(), motionEvent.getY()) && this.mPressed) {
                this.mPressed = false;
                playReleaseAnimation();
            }
        } else {
            if (motionEvent.getY() < this.mSearchViewWrapper.getTop() || motionEvent.getY() > this.mSearchViewWrapper.getBottom()) {
                return false;
            }
            if (isInView(motionEvent.getX(), motionEvent.getY()) && !isInButton(motionEvent.getX(), motionEvent.getY())) {
                this.mPressed = true;
                playPressAnimation();
            }
        }
        return super.dispatchTouchEvent(motionEvent);
    }

    public long getAnimatorDuration() {
        return 150L;
    }

    public boolean getCancelIconAnimating() {
        return this.mToolBarAnimationRunning;
    }

    public SearchFunctionalButton getFunctionalButton() {
        return this.mFunctionalButton;
    }

    @Override
    public int getGravity() {
        return this.mGravity;
    }

    public boolean getInputMethodAnimationEnabled() {
        return this.mInputMethodAnimationEnabled;
    }

    public ImageView getMainIconView() {
        return this.mMainIcon;
    }

    public int getSearchState() {
        return this.mState.get();
    }

    public COUISearchView getSearchView() {
        return this.mSearchView;
    }

    public float getSearchViewAnimateHeightPercent() {
        return this.mCollapsingHeightPercent;
    }

    public ImageView getSubIconView() {
        return this.mSubIcon;
    }

    public void hideInToolBar() {
        if (this.mToolBarAnimationRunning) {
            return;
        }
        this.mToolBarAnimationRunning = true;
        ensureAddedToToolBar();
        if (this.mAddToToolbarWay == WAY_AT_BEHIND) {
            animate().alpha(0.0f).setDuration(DEFAULT_FADE_DURATION).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animator) {
                    super.onAnimationEnd(animator);
                    COUISearchViewAnimate.this.setVisibility(8);
                }
            }).start();
        }
        ValueAnimator valueAnimatorOfFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        valueAnimatorOfFloat.setDuration(DEFAULT_FADE_DURATION);
        valueAnimatorOfFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                COUISearchViewAnimate.this.setToolBarAlpha(((Float) valueAnimator.getAnimatedValue()).floatValue());
            }
        });
        valueAnimatorOfFloat.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animator) {
                super.onAnimationEnd(animator);
                COUISearchViewAnimate.this.mToolBarAnimationRunning = false;
                COUISearchViewAnimate.this.setToolBarChildVisibility(0);
                COUISearchViewAnimate.this.setSearchAutoCompleteUnFocus();
                COUISearchViewAnimate.this.mFunctionalButton.setVisibility(4);
                COUISearchViewAnimate.this.mButtonDivider.setVisibility(4);
            }

            @Override
            public void onAnimationStart(Animator animator) {
                super.onAnimationStart(animator);
            }
        });
        valueAnimatorOfFloat.start();
        if (this.mInputMethodAnimationEnabled) {
            openSoftInput(false);
        }
    }

    @Deprecated
    public boolean isIconCanAnimate() {
        return this.mSearchIconCanAnimate;
    }

    @Override
    public void onActionViewCollapsed() {
    }

    @Override
    public void onActionViewExpanded() {
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (this.mNeedUpdateNormalRectPath || this.mNeedUpdatePressFeedbackRectPath) {
            updatePath();
        }
        Paint paint = this.mPressFeedbackPaint;
        Paint.Style style = Paint.Style.FILL;
        paint.setStyle(style);
        this.mNormalPaint.setStyle(style);
        int iSave = canvas.save();
        if (this.mDivideBackground) {
            this.mNormalPaint.setColor(this.mCurrentBackgroundColor);
            canvas.drawPath(this.mNormalBackgroundPath, this.mNormalPaint);
        }
        this.mPressFeedbackPaint.setColor(this.mCurrentBackgroundColor);
        canvas.drawPath(this.mPressFeedbackBackgroundPath, this.mPressFeedbackPaint);
        canvas.restoreToCount(iSave);
        super.onDraw(canvas);
    }

    @Override
    public void onImeAnimStart() {
        if (this.mSearchView.getRootWindowInsets() != null && this.mSearchView.getRootWindowInsets().isVisible(WindowInsets.Type.ime()) && this.mState.get() == 0) {
            this.mState.set(STATE_EDIT);
            this.mSearchViewSmoothEnterAnimatorSet.start();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        if (isInIcon(motionEvent.getX(), motionEvent.getY())) {
            return false;
        }
        if (this.mState.get() != 0 || isInButton(motionEvent.getX(), motionEvent.getY())) {
            return super.onInterceptTouchEvent(motionEvent);
        }
        return true;
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        updateBackgroundRect();
        updateBounds();
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (this.mSearchViewType == 1) {
            int measuredWidth = (this.mBackgroundEndGap * 2) + this.mFunctionalButton.getMeasuredWidth() + this.mButtonDivider.getMeasuredWidth();
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) this.mSearchView.getLayoutParams();
            if (marginLayoutParams.getMarginEnd() != measuredWidth) {
                marginLayoutParams.setMarginEnd(measuredWidth);
                this.mSearchView.setLayoutParams(marginLayoutParams);
            }
        }
    }

    @Override
    public void onRestoreInstanceState(Parcelable parcelable) {
        if (parcelable instanceof COUISavedState) {
            setSearchViewAnimateHeightPercent(((COUISavedState) parcelable).mCollapsingHeightPercent);
        }
        super.onRestoreInstanceState(parcelable);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        COUISavedState cOUISavedState = new COUISavedState(super.onSaveInstanceState());
        cOUISavedState.mCollapsingHeightPercent = this.mCollapsingHeightPercent;
        return cOUISavedState;
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        boolean zOnTouchEvent = super.onTouchEvent(motionEvent);
        if (this.mPressFeedbackBackgroundRect.contains(motionEvent.getX(), motionEvent.getY())) {
            return true;
        }
        return zOnTouchEvent;
    }

    public void openSoftInput(boolean enabled) {
        COUISearchView cOUISearchView = this.mSearchView;
        if (cOUISearchView == null || cOUISearchView.getSearchAutoComplete() == null) {
            return;
        }
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService("input_method");
        Log.d(TAG, "openSoftInput: " + enabled);
        if (!enabled) {
            if (inputMethodManager == null || !inputMethodManager.isActive()) {
                return;
            }
            inputMethodManager.hideSoftInputFromWindow(this.mSearchView.getSearchAutoComplete().getWindowToken(), 0);
            return;
        }
        setSearchAutoCompleteFocus();
        if (inputMethodManager != null) {
            if (!this.mIsAtLeastR || this.mShowImeAnimDuration == 0) {
                inputMethodManager.showSoftInput(this.mSearchView.getSearchAutoComplete(), 0);
            } else {
                showSoftInput();
            }
        }
    }

    public void refresh() {
        TypedArray typedArrayObtainStyledAttributes = null;
        String resourceTypeName = this.mStyle != 0 ? getResources().getResourceTypeName(this.mStyle) : null;
        if ("attr".equals(resourceTypeName)) {
            typedArrayObtainStyledAttributes = getContext().getTheme().obtainStyledAttributes(null, R.styleable.COUISearchViewAnimate, this.mStyle, 0);
        } else if ("style".equals(resourceTypeName)) {
            typedArrayObtainStyledAttributes = getContext().getTheme().obtainStyledAttributes(null, R.styleable.COUISearchViewAnimate, 0, this.mStyle);
        }
        if (typedArrayObtainStyledAttributes != null) {
            this.mNormalBackgroundColor = getContext().getResources().getColor(R.color.coui_search_view_selector_color_normal);
            this.mPressedBackgroundColor = getContext().getResources().getColor(R.color.coui_search_view_selector_color_pressed);
            this.mCurrentBackgroundColor = this.mNormalBackgroundColor;
            this.mSearchView.getSearchAutoComplete().setTextColor(typedArrayObtainStyledAttributes.getColor(R.styleable.COUISearchViewAnimate_inputTextColor, 0));
            int index = R.styleable.COUISearchViewAnimate_couiSearchIcon;
            Drawable drawable = typedArrayObtainStyledAttributes.getDrawable(index);
            if (typedArrayObtainStyledAttributes.hasValue(index)) {
                this.mSearchIcon.setImageDrawable(drawable);
            } else {
                this.mSearchIcon.setImageDrawable(drawable);
            }
            this.mSearchView.getSearchAutoComplete().setHintTextColor(typedArrayObtainStyledAttributes.getColorStateList(R.styleable.COUISearchViewAnimate_normalHintColor));
            this.mSearchView.setBackgroundColor(typedArrayObtainStyledAttributes.getColor(R.styleable.COUISearchViewAnimate_searchBackground, 0));
            this.mCloseBtn = (ImageView) this.mSearchView.findViewById(R.id.search_close_btn);
            Drawable drawable2 = typedArrayObtainStyledAttributes.getDrawable(R.styleable.COUISearchViewAnimate_couiSearchClearSelector);
            ImageView imageView = this.mCloseBtn;
            if (imageView != null) {
                imageView.setImageDrawable(drawable2);
            }
            Drawable drawable3 = typedArrayObtainStyledAttributes.getDrawable(R.styleable.COUISearchViewAnimate_buttonDivider);
            if (drawable3 != null) {
                this.mButtonDivider.setImageDrawable(drawable3);
            }
            COUITextViewCompatUtil.setPressRippleDrawable(this.mFunctionalButton);
            typedArrayObtainStyledAttributes.recycle();
        }
        if (this.mSearchViewShrinkWidth != 0) {
            this.mSearchViewShrinkWidth = getOriginWidth() - getShrinkWidth();
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) this.mSearchView.getLayoutParams();
            marginLayoutParams.setMarginEnd(this.mSearchViewShrinkWidth);
            this.mSearchView.setLayoutParams(marginLayoutParams);
        }
    }

    @Deprecated
    public void removeHintViewLayoutOnClickListener() {
    }

    public void setAtBehindToolBar(COUIToolbar cOUIToolbar, int gravityInToolBar, MenuItem menuItem) {
        this.mToolBar = cOUIToolbar;
        this.mGravityInToolBar = gravityInToolBar;
        this.mAddToToolbarWay = WAY_AT_BEHIND;
        setMenuItem(menuItem);
        this.mShouldClearFocus = false;
        changeStateImmediately(STATE_EDIT);
        setVisibility(8);
    }

    public void setAtFrontToolBar(COUIToolbar cOUIToolbar, int gravityInToolBar, MenuItem menuItem) {
        this.mToolBar = cOUIToolbar;
        this.mGravityInToolBar = gravityInToolBar;
        this.mAddToToolbarWay = WAY_AT_FRONT;
        setMenuItem(menuItem);
        ensureAddedToToolBar();
        menuItem.setVisible(false);
        addOnStateChangeListener(this.mAtToolbarFrontStateChangeListener);
    }

    public void setCancelButtonBackground(Drawable drawable) {
        this.mFunctionalButton.setBackground(drawable);
    }

    public void setCancelDividerImageDrawable(Drawable drawable) {
        this.mButtonDivider.setImageDrawable(drawable);
    }

    public void setCloseBtnBackground(Drawable drawable) {
        this.mCloseBtn.setBackground(drawable);
    }

    public void setCloseBtnImageDrawable(Drawable drawable) {
        this.mCloseBtn.setImageDrawable(drawable);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        ImageView imageView = this.mSearchIcon;
        if (imageView != null) {
            imageView.setEnabled(enabled);
        }
        COUISearchView cOUISearchView = this.mSearchView;
        if (cOUISearchView != null) {
            cOUISearchView.setEnabled(enabled);
        }
        SearchFunctionalButton searchFunctionalButton = this.mFunctionalButton;
        if (searchFunctionalButton != null) {
            searchFunctionalButton.setEnabled(enabled);
        }
    }

    public void setExtraActivateMarginTop(int extraY) {
        this.mExtraY = extraY;
    }

    public void setFunctionalButtonText(String str) {
        this.mFunctionalButton.setText(str);
    }

    @Override
    public void setGravity(int gravity) {
        if (this.mGravity != gravity) {
            if ((8388615 & gravity) == 0) {
                gravity |= 8388611;
            }
            if ((gravity & 112) == 0) {
                gravity |= 16;
            }
            this.mGravity = gravity;
        }
    }

    @Deprecated
    public void setHintTextViewHintTextColor(int index) {
    }

    @Deprecated
    public void setHintTextViewTextColor(int index) {
    }

    @Deprecated
    public void setHintViewBackground(Drawable drawable) {
    }

    @Deprecated
    public void setHintViewLayoutOnClickListener(View.OnClickListener onClickListener) {
    }

    @Deprecated
    public void setIconCanAnimate(boolean searchIconCanAnimate) {
        this.mSearchIconCanAnimate = searchIconCanAnimate;
    }

    public void setImeInsetsAnimationCallback() {
        if (this.mIsAtLeastR) {
            this.mShowImeAnimDuration = 450;
            this.mShowImeInterpolator = DEFAULT_SEARCH_VIEW_OFFSET_CHANGE_INTERPOLATOR;
            final ImeInsetsAnimationCallback imeInsetsAnimationCallback = new ImeInsetsAnimationCallback();
            imeInsetsAnimationCallback.setImeAnimationListener(this);
            this.mSearchView.post(new Runnable() {
                @Override
                public final void run() {
                    COUISearchViewAnimate.this.lambda$setImeInsetsAnimationCallback$2(imeInsetsAnimationCallback);
                }
            });
        }
    }

    public void setInputHintTextColor(int index) {
        this.mSearchView.getSearchAutoComplete().setHintTextColor(index);
    }

    public void setInputMethodAnimationEnabled(boolean inputMethodAnimationEnabled) {
        this.mInputMethodAnimationEnabled = inputMethodAnimationEnabled;
    }

    public void setInputTextColor(int index) {
        this.mSearchView.getSearchAutoComplete().setTextColor(index);
    }

    public void setMainIconDrawable(Drawable drawable) {
        this.mMainIcon.setImageDrawable(drawable);
    }

    public void setOnAnimationListener(OnAnimationListener onAnimationListener) {
        this.mOnAnimationListener = onAnimationListener;
    }

    public void setOnDispatchKeyEventPreImeListener(OnDispatchKeyEventPreImeListener onDispatchKeyEventPreImeListener) {
        this.mOnDispatchKeyEventPreImeListener = onDispatchKeyEventPreImeListener;
    }

    public void setQueryHint(CharSequence charSequence) {
        COUISearchView cOUISearchView = this.mSearchView;
        if (cOUISearchView != null) {
            cOUISearchView.setQueryHint(charSequence);
        }
    }

    public void setSearchAnimateType(int searchViewType) {
        if (this.mState.get() == 1) {
            Log.d(TAG, "setSearchAnimateType to " + TYPE_NAME[searchViewType] + " is not allowed in STATE_EDIT");
            return;
        }
        this.mSearchViewType = searchViewType;
        if (searchViewType == 0) {
            this.mButtonDivider.setVisibility(8);
            this.mFunctionalButton.setVisibility(4);
            this.mFunctionalButton.setAlpha(0.0f);
            ((ViewGroup.MarginLayoutParams) this.mFunctionalButton.getLayoutParams()).setMarginStart(this.mCancelButtonLargeStartMargin);
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) this.mSearchView.getLayoutParams();
            marginLayoutParams.setMarginEnd(0);
            this.mSearchView.setLayoutParams(marginLayoutParams);
            return;
        }
        if (searchViewType == 1) {
            this.mButtonDivider.setVisibility(8);
            this.mFunctionalButton.setVisibility(8);
            ((ViewGroup.MarginLayoutParams) this.mFunctionalButton.getLayoutParams()).setMarginStart(this.mCancelButtonSmallStartMargin);
            ViewGroup.MarginLayoutParams marginLayoutParams2 = (ViewGroup.MarginLayoutParams) this.mSearchView.getLayoutParams();
            marginLayoutParams2.setMarginEnd(getOriginWidth() - getShrinkWidth());
            this.mSearchView.setLayoutParams(marginLayoutParams2);
        }
    }

    public void setSearchBackgroundColor(ColorStateList colorStateList) {
        if (colorStateList != null) {
            int index = this.mNormalBackgroundColor;
            int defaultColor = colorStateList.getDefaultColor();
            this.mNormalBackgroundColor = defaultColor;
            this.mPressedBackgroundColor = colorStateList.getColorForState(new int[]{16842919}, defaultColor);
            if (this.mCurrentBackgroundColor == index) {
                this.mCurrentBackgroundColor = this.mNormalBackgroundColor;
            }
            invalidate();
        }
    }

    public void setSearchViewAnimateHeightPercent(float collapsingHeightPercent) {
        this.mCollapsingHeightPercent = collapsingHeightPercent;
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) getChildAt(0).getLayoutParams();
        marginLayoutParams.height = (int) Float.max(this.mCollapsedMinHeight, this.mInitSearchViewWrapperHeight * clampSearchViewHeight(collapsingHeightPercent));
        marginLayoutParams.setMarginStart((int) (getPaddingStart() * (1.0f - clampMarginValue(collapsingHeightPercent)) * (-1.0f)));
        marginLayoutParams.setMarginEnd((int) (getPaddingEnd() * (1.0f - clampMarginValue(collapsingHeightPercent)) * (-1.0f)));
        getChildAt(0).setLayoutParams(marginLayoutParams);
        setTranslationY((this.mInitSearchViewAnimateHeight / 2.0f) * (1.0f - collapsingHeightPercent));
        float fraction = (collapsingHeightPercent - 0.5f) * 2.0f;
        this.mSearchView.setAlpha(fraction);
        this.mSearchIcon.setAlpha(fraction);
        this.mCurrentBackgroundColor = ((Integer) this.mEvaluator.evaluate(clampMarginValue(collapsingHeightPercent), Integer.valueOf(this.mHorizontalDividerColor), Integer.valueOf(this.mNormalBackgroundColor))).intValue();
    }

    public void setSearchViewBackgroundColor(int index) {
        this.mSearchView.setBackgroundColor(index);
    }

    public void setSearchViewIcon(Drawable drawable) {
        this.mSearchIcon.setImageDrawable(drawable);
    }

    public void setSubIconDrawable(Drawable drawable) {
        this.mSubIcon.setImageDrawable(drawable);
    }

    public void showInToolBar() {
        if (this.mToolBarAnimationRunning) {
            return;
        }
        this.mToolBarAnimationRunning = true;
        ensureAddedToToolBar();
        if (this.mAddToToolbarWay == WAY_AT_BEHIND) {
            setVisibility(0);
            setAlpha(0.0f);
            int index = this.mSearchViewType;
            if (index == 0) {
                this.mFunctionalButton.setVisibility(0);
                this.mButtonDivider.setVisibility(8);
            } else if (index == 1) {
                this.mFunctionalButton.setVisibility(0);
                this.mButtonDivider.setVisibility(0);
            }
            post(new Runnable() {
                @Override
                public void run() {
                    ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) COUISearchViewAnimate.this.mSearchView.getLayoutParams();
                    marginLayoutParams.setMarginEnd(COUISearchViewAnimate.this.getOriginWidth() - COUISearchViewAnimate.this.getShrinkWidth());
                    COUISearchViewAnimate.this.mSearchView.setLayoutParams(marginLayoutParams);
                }
            });
            animate().alpha(1.0f).setDuration(DEFAULT_FADE_DURATION).setListener(null).start();
        }
        setToolBarChildVisibility(8);
        ValueAnimator valueAnimatorOfFloat = ValueAnimator.ofFloat(1.0f, 0.0f);
        valueAnimatorOfFloat.setDuration(DEFAULT_FADE_DURATION);
        valueAnimatorOfFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                COUISearchViewAnimate.this.setToolBarAlpha(((Float) valueAnimator.getAnimatedValue()).floatValue());
            }
        });
        valueAnimatorOfFloat.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animator) {
                super.onAnimationEnd(animator);
                COUISearchViewAnimate.this.mToolBarAnimationRunning = false;
                COUISearchViewAnimate.this.setSearchAutoCompleteFocus();
            }
        });
        valueAnimatorOfFloat.start();
        setSearchAutoCompleteUnFocus();
        if (this.mInputMethodAnimationEnabled) {
            openSoftInput(true);
        }
    }

    public COUISearchViewAnimate(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, R.attr.couiSearchViewAnimateStyle);
    }

    public COUISearchViewAnimate(Context context, AttributeSet attributeSet, int index) {
        this(context, attributeSet, index, COUIContextUtil.isCOUIDarkTheme(context) ? R.style.Widget_COUI_COUISearchViewAnimate_Dark : R.style.Widget_COUI_COUISearchViewAnimate);
    }

    public COUISearchViewAnimate(Context context, AttributeSet attributeSet, int index, int count) {
        super(context, attributeSet, index, count);
        this.mPressFeedbackBackgroundPath = new Path();
        this.mNormalBackgroundPath = new Path();
        this.mPressFeedbackPaint = new Paint(1);
        this.mNormalPaint = new Paint(1);
        this.mButtonLocation = new int[2];
        this.mLocation = new int[2];
        this.mEvaluator = new ArgbEvaluator();
        this.mButtonHitRect = new RectF();
        Rect rect = new Rect();
        this.mWrapperBounds = rect;
        this.mState = new AtomicInteger(0);
        this.mGravityInToolBar = 48;
        this.mAddToToolbarWay = WAY_NONE;
        this.mSearchIconCanAnimate = true;
        this.mShouldClearFocus = true;
        this.mInputMethodAnimationEnabled = true;
        this.mTopOffset = 0;
        this.mExtraY = 0;
        this.mSearchViewType = 1;
        this.mCollapsingHeightPercent = 1.0f;
        this.mDivideBackground = false;
        this.mNeedUpdatePressFeedbackRectPath = true;
        this.mNeedUpdateNormalRectPath = true;
        this.mShowImeAnimDuration = 0;
        this.mShowImeInterpolator = null;
        this.mPressed = false;
        this.mAtToolbarFrontStateChangeListener = new OnStateChangeListener() {
            @Override
            public void onStateChange(int delta, int start) {
                if (start == 1) {
                    COUISearchViewAnimate.this.showInToolBar();
                } else if (start == 0) {
                    COUISearchViewAnimate.this.hideInToolBar();
                }
            }
        };
        this.mGravity = 16;
        this.mContext = context;
        COUIDarkModeUtil.setForceDarkAllow(this, false);
        this.mIsAtLeastR = true;
        inflateView(context, attributeSet);
        loadAttr(context, attributeSet, index, count);
        setClipToPadding(false);
        setClipChildren(false);
        setWillNotDraw(false);
        initAnimator();
        setLayoutDirection(3);
        setSearchAnimateType(this.mSearchViewType);
        setTouchDelegate(new TouchDelegate(rect, this.mFunctionalButton));
        this.mSearchView.getSearchAutoComplete().addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                if ("".contentEquals(editable)) {
                    COUISearchViewAnimate.this.mMainIcon.setVisibility(0);
                    COUISearchViewAnimate.this.mSubIcon.setVisibility(0);
                } else {
                    COUISearchViewAnimate.this.mMainIcon.setVisibility(8);
                    COUISearchViewAnimate.this.mSubIcon.setVisibility(8);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int delta, int start, int end) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int delta, int start, int end) {
            }
        });
    }
}
