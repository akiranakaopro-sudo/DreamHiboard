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
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.CollapsibleActionView;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.Interpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.core.view.ViewCompat;
import com.coui.appcompat.accessibilityutil.COUIAccessibilityUtil;
import com.coui.appcompat.animation.COUIMoveEaseInterpolator;
import com.coui.appcompat.contextutil.COUIContextUtil;
import com.coui.appcompat.darkmode.COUIDarkModeUtil;
import com.coui.appcompat.grid.COUIResponsiveUtils;
import com.coui.appcompat.log.COUILog;
import com.coui.appcompat.rippleutil.COUIRippleDrawableUtil;
import com.coui.appcompat.searchview.ImeInsetsAnimationCallback;
import com.coui.appcompat.state.COUIMaskEffectDrawable;
import com.coui.appcompat.state.COUIMaskRippleDrawable;
import com.coui.appcompat.state.COUIStateEffectDrawable;
import com.coui.appcompat.state.COUIStrokeDrawable;
import com.coui.appcompat.textutil.COUIChangeTextUtil;
import com.coui.appcompat.textviewcompatutil.COUITextViewCompatUtil;
import com.coui.appcompat.toolbar.COUIToolbar;
import com.coui.appcompat.uiutil.UIUtil;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


public class COUISearchBar extends ViewGroup implements CollapsibleActionView, ImeInsetsAnimationCallback.OnImeAnimationListener {
    private static final float CLAMP_ANIMATION_PERCENT = 0.3f;
    private static final long DEFAULT_BUTTON_ALPHA_CHANGE_DURATION = 350;
    private static final long DEFAULT_FADE_DURATION = 150;
    private static final int DEFAULT_MAX_LINES = 1;
    private static final long DEFAULT_SEARCH_VIEW_BUTTON_ALPHA_FADE_IN_DURATION = 400;
    private static final long DEFAULT_SEARCH_VIEW_BUTTON_ALPHA_FADE_OUT_DURATION = 200;
    private static final long DEFAULT_SEARCH_VIEW_BUTTON_ANIMATION_START_DELAY = 50;
    private static final long DEFAULT_SEARCH_VIEW_BUTTON_OFFSET_DURATION = 400;
    private static final long DEFAULT_SEARCH_VIEW_OFFSET_CHANGE_DURATION = 450;
    private static final long DEFAULT_SEARCH_VIEW_SCALE_CHANGE_DURATION = 450;
    private static final float FLOAT_FOUR = 4.0f;
    private static final float FLOAT_POINT_SEVEN_FIVE = 0.75f;
    private static final float FLOAT_TWO = 2.0f;
    private static final long NON_INSTANT_SEARCH_BUTTON_ALPHA_CHANGE_DURATION = 100;
    private static final int RESPONSIVE_WIDTH_TYPE_COMPAT = 0;
    private static final int RESPONSIVE_WIDTH_TYPE_EXPANDED = 2;
    private static final int RESPONSIVE_WIDTH_TYPE_MEDIUM = 1;
    public static final int STATE_EDIT = 1;
    public static final int STATE_NORMAL = 0;
    private static final String TAG = "COUISearchBar";
    public static final int TYPE_INSTANT_SEARCH = 0;
    public static final int TYPE_NON_INSTANT_SEARCH = 1;
    private static final int WAY_AT_BEHIND = 1;
    private static final int WAY_AT_FRONT = 2;
    private static final int WAY_NONE = 0;
    private int mAddToToolbarWay;
    private AnimatorHelper mAnimatorHelper;
    private AttributeSet mAttrs;
    private final Rect mBackgroundRect;
    private float mBackgroundScaleFraction;
    private int mButtonOffsetAnimationDistance;
    private COUIHintAnimationLayout mCOUIHintAnimationLayout;
    private int mClearTextDrawableResourceId;
    private int mCollapsedMinHeight;
    private float mCollapsingHeightPercent;
    private int mDividerHeight;
    private final Rect mDividerRect;
    private int mDividerWidth;
    private final COUISearchBarDrawingProxyDrawable mDrawingProxyDrawable;
    private View mDrawingProxyView;
    private int mEditFrameEndGap;
    private int mExtraHorizontalBackground;
    private int[] mFunctionButtonEndGap;
    private int mFunctionButtonMaxWidth;
    private int mFunctionButtonStartGap;
    private TextView mFunctionalButton;
    private ValueAnimator mFunctionalButtonAlphaEnterAnimator;
    private ValueAnimator mFunctionalButtonAlphaExitAnimator;
    private ValueAnimator mFunctionalButtonOffsetEnterAnimator;
    private ValueAnimator mFunctionalButtonOffsetExitAnimator;
    private int mGravityInToolBar;
    private boolean mHasAddedToToolbar;
    private boolean mHideFunctionalButton;
    private int mHorizontalDividerColor;
    private int mIconMaxHeight;
    private int mIconMaxWidth;
    private int mInitSearchBarHeight;
    private int mInnerIconSize;
    private ImageView mInnerPrimaryButton;
    private ImageView mInnerSecondaryButton;
    private boolean mInputMethodAnimationEnabled;
    private boolean mIsAtLeastR;
    private COUIMaskEffectDrawable mMaskDrawable;
    private MenuItem mMenuItem;
    private int[] mNavigationButtonStartGap;
    private int mNavigationButtonWidth;
    private ImageView mNavigationView;
    private int mNonInstantSearchInnerGap;
    private int mNormalBackgroundColor;
    private int mNormalBackgroundHeight;
    private OnAnimationListener mOnAnimationListener;
    private OnSearchBarBackgroundBoundsChangedListener mOnSearchBarBackgroundBoundsChangedListener;
    private List<OnStateChangeListener> mOnStateChangeListeners;
    private ValueAnimator mOuterButtonAlphaEnterAnimator;
    private ValueAnimator mOuterButtonAlphaExitAnimator;
    private int[] mOuterButtonEndGap;
    private int mOuterButtonGap;
    private ValueAnimator mOuterButtonOffsetEnterAnimator;
    private ValueAnimator mOuterButtonOffsetExitAnimator;
    private int mOuterButtonWidth;
    private ImageView mOuterPrimaryButton;
    private ImageView mOuterSecondaryButton;
    private boolean mPressed;
    private int mPressedBackgroundColor;
    private ImageView mQuickDeleteButton;
    private int[] mResponsiveHorizontalPadding;
    private int mResponsiveWidthSize;
    private String mSearchBarContentDesc;
    private String mSearchBarHintTitleDesc;
    private EditText mSearchEditText;
    private int mSearchIconStartGap;
    private ImageView mSearchIconView;
    private ValueAnimator mSearchViewOffsetEnterAnimator;
    private ValueAnimator mSearchViewOffsetExitAnimator;
    private ValueAnimator mSearchViewScaleEnterAnimator;
    private ValueAnimator mSearchViewScaleExitAnimator;
    private AnimatorSet mSearchViewSmoothEnterAnimatorSet;
    private AnimatorSet mSearchViewSmoothExitAnimatorSet;
    private int mSearchViewType;
    private int mShowImeAnimDuration;
    private Interpolator mShowImeInterpolator;
    private float mShrinkFraction;
    private volatile AtomicInteger mState;
    private COUIStateEffectDrawable mStateEffectBackground;
    private COUIStrokeDrawable mStrokeDrawable;
    private int mStyle;
    private COUIToolbar mToolBar;
    private boolean mToolBarAnimationRunning;
    private boolean mUseResponsivePadding;
    private static final Interpolator DEFAULT_SEARCH_VIEW_OFFSET_CHANGE_INTERPOLATOR = new COUIMoveEaseInterpolator();
    private static final Interpolator DEFAULT_SEARCH_VIEW_SCALE_CHANGE_INTERPOLATOR = new COUIMoveEaseInterpolator();
    private static final Interpolator DEFAULT_BUTTON_ALPHA_CHANGE_INTERPOLATOR = new COUIMoveEaseInterpolator();
    private static final ArgbEvaluator DEFAULT_EVALUATOR = new ArgbEvaluator();
    private static final String[] TYPE_NAME = {"TYPE_INSTANT_SEARCH", "TYPE_NON_INSTANT_SEARCH"};

    public class AnimatorHelper {
        private int mTopOffset = 0;
        private int mTopMargin = 0;
        private int mStartY = 0;
        private int mExtraY = 0;
        private volatile AtomicBoolean mAnimatingAtomic = new AtomicBoolean(false);

        public AnimatorHelper() {
            initAnimator();
        }


        public void endAnimateToEditState() {
            this.mTopOffset = 0;
            if (COUISearchBar.this.mSearchViewType == TYPE_INSTANT_SEARCH) {
                int index = this.mStartY - this.mExtraY;
                if (((ViewGroup.MarginLayoutParams) COUISearchBar.this.getLayoutParams()) != null) {
                    ((ViewGroup.MarginLayoutParams) COUISearchBar.this.getLayoutParams()).topMargin = this.mTopMargin - index;
                }
                COUISearchBar.this.mShrinkFraction = 1.0f;
                if (COUISearchBar.this.functionalButtonShouldShow()) {
                    COUISearchBar.this.mFunctionalButton.setAlpha(1.0f);
                }
                COUISearchBar.this.setOuterButtonVisibility(8);
            } else if (COUISearchBar.this.mSearchViewType == 1) {
                COUISearchBar.this.mDrawingProxyDrawable.setDividerAlpha(1.0f);
                if (COUISearchBar.this.functionalButtonShouldShow()) {
                    COUISearchBar.this.mFunctionalButton.setAlpha(1.0f);
                }
            }
            COUISearchBar.this.requestLayout();
        }


        public void endAnimateToNormalState() {
            this.mTopOffset = 0;
            if (COUISearchBar.this.mSearchViewType == TYPE_INSTANT_SEARCH) {
                if (((ViewGroup.MarginLayoutParams) COUISearchBar.this.getLayoutParams()) != null) {
                    ((ViewGroup.MarginLayoutParams) COUISearchBar.this.getLayoutParams()).topMargin = this.mTopMargin;
                }
                COUISearchBar.this.mShrinkFraction = 0.0f;
            } else if (COUISearchBar.this.mSearchViewType == 1) {
                COUISearchBar.this.mDrawingProxyDrawable.setDividerAlpha(0.0f);
            }
            if (COUISearchBar.this.functionalButtonShouldShow()) {
                COUISearchBar.this.mFunctionalButton.setAlpha(0.0f);
                COUISearchBar.this.mFunctionalButton.setVisibility(8);
            }
            COUISearchBar.this.requestLayout();
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
            COUISearchBar.this.mFunctionalButtonAlphaEnterAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
            COUISearchBar.this.mFunctionalButtonAlphaEnterAnimator.setDuration(COUISearchBar.this.mSearchViewType == TYPE_INSTANT_SEARCH ? COUISearchBar.DEFAULT_BUTTON_ALPHA_CHANGE_DURATION : COUISearchBar.this.getOuterButtonCount() == 1 ? COUISearchBar.DEFAULT_SEARCH_VIEW_BUTTON_ALPHA_FADE_IN_DURATION : NON_INSTANT_SEARCH_BUTTON_ALPHA_CHANGE_DURATION);
            COUISearchBar.this.mFunctionalButtonAlphaEnterAnimator.setInterpolator(COUISearchBar.DEFAULT_BUTTON_ALPHA_CHANGE_INTERPOLATOR);
            COUISearchBar.this.mFunctionalButtonAlphaEnterAnimator.setStartDelay(COUISearchBar.this.mSearchViewType != TYPE_INSTANT_SEARCH ? COUISearchBar.this.getOuterButtonCount() == 1 ? COUISearchBar.DEFAULT_SEARCH_VIEW_BUTTON_ANIMATION_START_DELAY : 0L : 100L);
            COUISearchBar.this.mFunctionalButtonAlphaEnterAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    AnimatorHelper.this.lambda$initButtonEnterAnimator$2(valueAnimator);
                }
            });
            COUISearchBar.this.mFunctionalButtonOffsetEnterAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
            COUISearchBar.this.mFunctionalButtonOffsetEnterAnimator.setDuration(COUISearchBar.DEFAULT_SEARCH_VIEW_BUTTON_OFFSET_DURATION);
            COUISearchBar.this.mFunctionalButtonOffsetEnterAnimator.setInterpolator(COUISearchBar.DEFAULT_SEARCH_VIEW_OFFSET_CHANGE_INTERPOLATOR);
            COUISearchBar.this.mFunctionalButtonOffsetEnterAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    AnimatorHelper.this.lambda$initButtonEnterAnimator$3(valueAnimator);
                }
            });
            COUISearchBar.this.mOuterButtonAlphaEnterAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
            COUISearchBar.this.mOuterButtonAlphaEnterAnimator.setDuration(COUISearchBar.DEFAULT_SEARCH_VIEW_BUTTON_ALPHA_FADE_OUT_DURATION);
            COUISearchBar.this.mOuterButtonAlphaEnterAnimator.setInterpolator(COUISearchBar.DEFAULT_BUTTON_ALPHA_CHANGE_INTERPOLATOR);
            COUISearchBar.this.mOuterButtonAlphaEnterAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    AnimatorHelper.this.lambda$initButtonEnterAnimator$4(valueAnimator);
                }
            });
            COUISearchBar.this.mOuterButtonOffsetEnterAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
            COUISearchBar.this.mOuterButtonOffsetEnterAnimator.setDuration(COUISearchBar.DEFAULT_SEARCH_VIEW_BUTTON_OFFSET_DURATION);
            COUISearchBar.this.mOuterButtonOffsetEnterAnimator.setInterpolator(COUISearchBar.DEFAULT_SEARCH_VIEW_OFFSET_CHANGE_INTERPOLATOR);
            COUISearchBar.this.mOuterButtonOffsetEnterAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    AnimatorHelper.this.lambda$initButtonEnterAnimator$5(valueAnimator);
                }
            });
        }

        private void initButtonExitAnimator() {
            COUISearchBar.this.mFunctionalButtonAlphaExitAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
            COUISearchBar.this.mFunctionalButtonAlphaExitAnimator.setDuration(COUISearchBar.this.mSearchViewType == TYPE_INSTANT_SEARCH ? COUISearchBar.DEFAULT_BUTTON_ALPHA_CHANGE_DURATION : COUISearchBar.this.getOuterButtonCount() == 1 ? COUISearchBar.DEFAULT_SEARCH_VIEW_BUTTON_ALPHA_FADE_OUT_DURATION : NON_INSTANT_SEARCH_BUTTON_ALPHA_CHANGE_DURATION);
            COUISearchBar.this.mFunctionalButtonAlphaExitAnimator.setInterpolator(COUISearchBar.DEFAULT_BUTTON_ALPHA_CHANGE_INTERPOLATOR);
            COUISearchBar.this.mFunctionalButtonAlphaExitAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    AnimatorHelper.this.lambda$initButtonExitAnimator$8(valueAnimator);
                }
            });
            COUISearchBar.this.mFunctionalButtonOffsetExitAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
            COUISearchBar.this.mFunctionalButtonOffsetExitAnimator.setDuration(COUISearchBar.DEFAULT_SEARCH_VIEW_BUTTON_OFFSET_DURATION);
            COUISearchBar.this.mFunctionalButtonOffsetExitAnimator.setInterpolator(COUISearchBar.DEFAULT_SEARCH_VIEW_OFFSET_CHANGE_INTERPOLATOR);
            COUISearchBar.this.mFunctionalButtonOffsetExitAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    AnimatorHelper.this.lambda$initButtonExitAnimator$9(valueAnimator);
                }
            });
            COUISearchBar.this.mOuterButtonAlphaExitAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
            COUISearchBar.this.mOuterButtonAlphaExitAnimator.setDuration(COUISearchBar.DEFAULT_SEARCH_VIEW_BUTTON_ALPHA_FADE_IN_DURATION);
            COUISearchBar.this.mOuterButtonAlphaExitAnimator.setStartDelay(COUISearchBar.DEFAULT_SEARCH_VIEW_BUTTON_ANIMATION_START_DELAY);
            COUISearchBar.this.mOuterButtonAlphaExitAnimator.setInterpolator(COUISearchBar.DEFAULT_BUTTON_ALPHA_CHANGE_INTERPOLATOR);
            COUISearchBar.this.mOuterButtonAlphaExitAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    AnimatorHelper.this.lambda$initButtonExitAnimator$10(valueAnimator);
                }
            });
            COUISearchBar.this.mOuterButtonOffsetExitAnimator = ValueAnimator.ofFloat(1.0f, 0.0f);
            COUISearchBar.this.mOuterButtonOffsetExitAnimator.setDuration(COUISearchBar.DEFAULT_SEARCH_VIEW_BUTTON_OFFSET_DURATION);
            COUISearchBar.this.mOuterButtonOffsetExitAnimator.setStartDelay(COUISearchBar.DEFAULT_SEARCH_VIEW_BUTTON_ANIMATION_START_DELAY);
            COUISearchBar.this.mOuterButtonOffsetExitAnimator.setInterpolator(COUISearchBar.DEFAULT_SEARCH_VIEW_OFFSET_CHANGE_INTERPOLATOR);
            COUISearchBar.this.mOuterButtonOffsetExitAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    AnimatorHelper.this.lambda$initButtonExitAnimator$11(valueAnimator);
                }
            });
        }

        private void initSearchViewEnterAnimator() {
            COUISearchBar.this.mSearchViewOffsetEnterAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
            COUISearchBar.this.mSearchViewOffsetEnterAnimator.setDuration(COUISearchBar.DEFAULT_SEARCH_VIEW_OFFSET_CHANGE_DURATION);
            COUISearchBar.this.mSearchViewOffsetEnterAnimator.setInterpolator(COUISearchBar.DEFAULT_SEARCH_VIEW_OFFSET_CHANGE_INTERPOLATOR);
            COUISearchBar.this.mSearchViewOffsetEnterAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    AnimatorHelper.this.lambda$initSearchViewEnterAnimator$0(valueAnimator);
                }
            });
            COUISearchBar.this.mSearchViewScaleEnterAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
            COUISearchBar.this.mSearchViewScaleEnterAnimator.setDuration(COUISearchBar.DEFAULT_SEARCH_VIEW_SCALE_CHANGE_DURATION);
            COUISearchBar.this.mSearchViewScaleEnterAnimator.setInterpolator(COUISearchBar.DEFAULT_SEARCH_VIEW_SCALE_CHANGE_INTERPOLATOR);
            COUISearchBar.this.mSearchViewScaleEnterAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    AnimatorHelper.this.lambda$initSearchViewEnterAnimator$1(valueAnimator);
                }
            });
        }

        private void initSearchViewExitAnimator() {
            COUISearchBar.this.mSearchViewOffsetExitAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
            COUISearchBar.this.mSearchViewOffsetExitAnimator.setDuration(COUISearchBar.DEFAULT_SEARCH_VIEW_OFFSET_CHANGE_DURATION);
            COUISearchBar.this.mSearchViewOffsetExitAnimator.setInterpolator(COUISearchBar.DEFAULT_SEARCH_VIEW_OFFSET_CHANGE_INTERPOLATOR);
            COUISearchBar.this.mSearchViewOffsetExitAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    AnimatorHelper.this.lambda$initSearchViewExitAnimator$6(valueAnimator);
                }
            });
            COUISearchBar.this.mSearchViewScaleExitAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
            COUISearchBar.this.mSearchViewScaleExitAnimator.setDuration(COUISearchBar.DEFAULT_SEARCH_VIEW_SCALE_CHANGE_DURATION);
            COUISearchBar.this.mSearchViewScaleExitAnimator.setInterpolator(COUISearchBar.DEFAULT_SEARCH_VIEW_OFFSET_CHANGE_INTERPOLATOR);
            COUISearchBar.this.mSearchViewScaleExitAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    AnimatorHelper.this.lambda$initSearchViewExitAnimator$7(valueAnimator);
                }
            });
        }

        private void initSmoothEnterAnimatorSet() {
            COUISearchBar.this.mSearchViewSmoothEnterAnimatorSet = new AnimatorSet();
            COUISearchBar.this.mSearchViewSmoothEnterAnimatorSet.addListener(new DefaultAnimatorListener() {
                @Override
                public void onAnimationEnd(Animator animator) {
                    AnimatorHelper.this.endAnimateToEditState();
                    AnimatorHelper.this.mAnimatingAtomic.set(false);
                    if (COUISearchBar.this.mOnAnimationListener != null) {
                        COUISearchBar.this.mOnAnimationListener.onAnimationEnd(1);
                    }
                }

                @Override
                public void onAnimationStart(Animator animator) {
                    if (COUISearchBar.this.mOnAnimationListener != null) {
                        COUISearchBar.this.mOnAnimationListener.onAnimationStart(1);
                    }
                }
            });
            COUISearchBar.this.mSearchViewSmoothEnterAnimatorSet.playTogether(COUISearchBar.this.mSearchViewOffsetEnterAnimator, COUISearchBar.this.mSearchViewScaleEnterAnimator, COUISearchBar.this.mFunctionalButtonAlphaEnterAnimator, COUISearchBar.this.mFunctionalButtonOffsetEnterAnimator, COUISearchBar.this.mOuterButtonAlphaEnterAnimator, COUISearchBar.this.mOuterButtonOffsetEnterAnimator);
        }

        private void initSmoothExitAnimatorSet() {
            COUISearchBar.this.mSearchViewSmoothExitAnimatorSet = new AnimatorSet();
            COUISearchBar.this.mSearchViewSmoothExitAnimatorSet.addListener(new DefaultAnimatorListener() {
                @Override
                public void onAnimationEnd(Animator animator) {
                    AnimatorHelper.this.endAnimateToNormalState();
                    AnimatorHelper.this.mAnimatingAtomic.set(false);
                    if (COUISearchBar.this.mOnAnimationListener != null) {
                        COUISearchBar.this.mOnAnimationListener.onAnimationEnd(0);
                    }
                }

                @Override
                public void onAnimationStart(Animator animator) {
                    if (COUISearchBar.this.mOnAnimationListener != null) {
                        COUISearchBar.this.mOnAnimationListener.onAnimationStart(0);
                    }
                    if (COUISearchBar.this.mSearchViewType == 0 && COUISearchBar.this.getOuterButtonCount() == 1 && COUISearchBar.this.functionalButtonShouldShow()) {
                        COUISearchBar.this.mFunctionalButton.jumpDrawablesToCurrentState();
                    }
                }
            });
            COUISearchBar.this.mSearchViewSmoothExitAnimatorSet.playTogether(COUISearchBar.this.mSearchViewOffsetExitAnimator, COUISearchBar.this.mSearchViewScaleExitAnimator, COUISearchBar.this.mFunctionalButtonAlphaExitAnimator, COUISearchBar.this.mFunctionalButtonOffsetExitAnimator, COUISearchBar.this.mOuterButtonAlphaExitAnimator, COUISearchBar.this.mOuterButtonOffsetExitAnimator);
        }


        public void lambda$initButtonEnterAnimator$2(ValueAnimator valueAnimator) {
            float fFloatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
            if (COUISearchBar.this.mSearchViewType == TYPE_INSTANT_SEARCH) {
                if (COUISearchBar.this.functionalButtonShouldShow()) {
                    COUISearchBar.this.mFunctionalButton.setAlpha(fFloatValue);
                }
            } else if (COUISearchBar.this.mSearchViewType == 1) {
                COUISearchBar.this.mDrawingProxyDrawable.setDividerAlpha(fFloatValue);
                if (COUISearchBar.this.functionalButtonShouldShow()) {
                    COUISearchBar.this.mFunctionalButton.setAlpha(fFloatValue);
                }
            }
        }


        public void lambda$initButtonEnterAnimator$3(ValueAnimator valueAnimator) {
            if (COUISearchBar.this.mSearchViewType == TYPE_INSTANT_SEARCH) {
                float fFloatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                if (COUISearchBar.this.functionalButtonShouldShow()) {
                    COUISearchBar.this.mFunctionalButton.setTranslationX((1.0f - fFloatValue) * COUISearchBar.this.mButtonOffsetAnimationDistance);
                }
            }
        }


        public void lambda$initButtonEnterAnimator$4(ValueAnimator valueAnimator) {
            if (COUISearchBar.this.mSearchViewType == TYPE_INSTANT_SEARCH) {
                float fFloatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                if (COUISearchBar.this.mOuterPrimaryButton != null) {
                    COUISearchBar.this.mOuterPrimaryButton.setAlpha(1.0f - fFloatValue);
                }
                if (COUISearchBar.this.mOuterSecondaryButton != null) {
                    COUISearchBar.this.mOuterSecondaryButton.setAlpha(1.0f - fFloatValue);
                }
            }
        }


        public void lambda$initButtonEnterAnimator$5(ValueAnimator valueAnimator) {
            if (COUISearchBar.this.mSearchViewType == TYPE_INSTANT_SEARCH) {
                float fFloatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                if (COUISearchBar.this.mOuterPrimaryButton != null) {
                    COUISearchBar.this.mOuterPrimaryButton.setTranslationX((-fFloatValue) * COUISearchBar.this.mButtonOffsetAnimationDistance);
                }
                if (COUISearchBar.this.mOuterSecondaryButton != null) {
                    COUISearchBar.this.mOuterSecondaryButton.setTranslationX((-fFloatValue) * COUISearchBar.this.mButtonOffsetAnimationDistance);
                }
            }
        }


        public void lambda$initButtonExitAnimator$10(ValueAnimator valueAnimator) {
            if (COUISearchBar.this.mSearchViewType == TYPE_INSTANT_SEARCH) {
                float fFloatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                if (COUISearchBar.this.mOuterPrimaryButton != null) {
                    COUISearchBar.this.mOuterPrimaryButton.setAlpha(fFloatValue);
                }
                if (COUISearchBar.this.mOuterSecondaryButton != null) {
                    COUISearchBar.this.mOuterSecondaryButton.setAlpha(fFloatValue);
                }
            }
        }


        public void lambda$initButtonExitAnimator$11(ValueAnimator valueAnimator) {
            if (COUISearchBar.this.mSearchViewType == TYPE_INSTANT_SEARCH) {
                float fFloatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                if (COUISearchBar.this.mOuterPrimaryButton != null) {
                    COUISearchBar.this.mOuterPrimaryButton.setTranslationX((-fFloatValue) * COUISearchBar.this.mButtonOffsetAnimationDistance);
                }
                if (COUISearchBar.this.mOuterSecondaryButton != null) {
                    COUISearchBar.this.mOuterSecondaryButton.setTranslationX((-fFloatValue) * COUISearchBar.this.mButtonOffsetAnimationDistance);
                }
            }
        }


        public void lambda$initButtonExitAnimator$8(ValueAnimator valueAnimator) {
            float fFloatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
            if (COUISearchBar.this.mSearchViewType == TYPE_INSTANT_SEARCH) {
                if (COUISearchBar.this.functionalButtonShouldShow()) {
                    COUISearchBar.this.mFunctionalButton.setAlpha(1.0f - fFloatValue);
                }
            } else if (COUISearchBar.this.mSearchViewType == 1) {
                float fraction = 1.0f - fFloatValue;
                COUISearchBar.this.mDrawingProxyDrawable.setDividerAlpha(fraction);
                if (COUISearchBar.this.functionalButtonShouldShow()) {
                    COUISearchBar.this.mFunctionalButton.setAlpha(fraction);
                }
            }
        }


        public void lambda$initButtonExitAnimator$9(ValueAnimator valueAnimator) {
            if (COUISearchBar.this.mSearchViewType == 0 && COUISearchBar.this.getOuterButtonCount() == 1) {
                float fFloatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                if (COUISearchBar.this.functionalButtonShouldShow()) {
                    COUISearchBar.this.mFunctionalButton.setTranslationX(fFloatValue * COUISearchBar.this.mButtonOffsetAnimationDistance);
                }
            }
        }


        public void lambda$initSearchViewEnterAnimator$0(ValueAnimator valueAnimator) {
            float fFloatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
            if (COUISearchBar.this.mSearchViewType == TYPE_INSTANT_SEARCH) {
                int topOffset = (int) (fFloatValue * (this.mStartY - this.mExtraY));
                ((ViewGroup.MarginLayoutParams) COUISearchBar.this.getLayoutParams()).topMargin -= topOffset - this.mTopOffset;
                this.mTopOffset = topOffset;
                COUISearchBar.this.requestLayout();
            }
        }


        public void lambda$initSearchViewEnterAnimator$1(ValueAnimator valueAnimator) {
            float fFloatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
            if (COUISearchBar.this.mSearchViewType == TYPE_INSTANT_SEARCH) {
                COUISearchBar.this.mShrinkFraction = fFloatValue;
            }
        }


        public void lambda$initSearchViewExitAnimator$6(ValueAnimator valueAnimator) {
            float fFloatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
            if (COUISearchBar.this.mSearchViewType == TYPE_INSTANT_SEARCH) {
                int topOffset = (int) (fFloatValue * (this.mStartY - this.mExtraY));
                ((ViewGroup.MarginLayoutParams) COUISearchBar.this.getLayoutParams()).topMargin += topOffset - this.mTopOffset;
                this.mTopOffset = topOffset;
                COUISearchBar.this.requestLayout();
            }
        }


        public void lambda$initSearchViewExitAnimator$7(ValueAnimator valueAnimator) {
            float fFloatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
            if (COUISearchBar.this.mSearchViewType == TYPE_INSTANT_SEARCH) {
                COUISearchBar.this.mShrinkFraction = 1.0f - fFloatValue;
            }
        }


        public void startAnimateToEditState() {
            this.mTopOffset = 0;
            if (((ViewGroup.MarginLayoutParams) COUISearchBar.this.getLayoutParams()) != null) {
                this.mTopMargin = ((ViewGroup.MarginLayoutParams) COUISearchBar.this.getLayoutParams()).topMargin;
            }
            this.mStartY = COUISearchBar.this.getTop();
            if (COUISearchBar.this.functionalButtonShouldShow()) {
                COUISearchBar.this.mFunctionalButton.setVisibility(0);
            }
            if ((!COUISearchBar.this.mIsAtLeastR || COUISearchBar.this.mShowImeAnimDuration == 0) && COUISearchBar.this.mInputMethodAnimationEnabled) {
                COUISearchBar.this.openSoftInput(true);
            }
            COUISearchBar.this.mState.set(STATE_EDIT);
            COUISearchBar.this.notifyOnStateChange(STATE_NORMAL, STATE_EDIT);
        }

        private void startAnimateToNormalState() {
            this.mTopOffset = 0;
            COUISearchBar.this.mSearchEditText.setText((CharSequence) null);
            if (COUISearchBar.this.mInputMethodAnimationEnabled) {
                COUISearchBar.this.openSoftInput(false);
            }
            if (COUISearchBar.this.mSearchViewType == TYPE_INSTANT_SEARCH) {
                COUISearchBar.this.setOuterButtonVisibility(0);
            }
            COUISearchBar.this.mState.set(STATE_NORMAL);
            COUISearchBar.this.notifyOnStateChange(STATE_EDIT, STATE_NORMAL);
        }

        public void runStateChangeAnimation(int targetState) {
            if (COUISearchBar.this.mState.get() == targetState) {
                COUILog.d(COUISearchBar.TAG, "runStateChangeAnimation: same state , return. targetState = " + targetState);
                return;
            }
            if (targetState == 1) {
                startToEditAnimator();
            } else if (targetState == 0) {
                startToNormalAnimator();
            }
        }

        public void runStateChangeImmediately(int targetState) {
            if (this.mAnimatingAtomic.get()) {
                COUILog.w(COUISearchBar.TAG, "animating");
                return;
            }
            if (targetState == 1) {
                startAnimateToEditState();
                endAnimateToEditState();
            } else if (targetState == 0) {
                startAnimateToNormalState();
                endAnimateToNormalState();
            }
        }

        public void startToEditAnimator() {
            if (COUISearchBar.this.mState.get() == 0 && this.mAnimatingAtomic.compareAndSet(false, true)) {
                startAnimateToEditState();
                COUISearchBar.this.mSearchViewSmoothEnterAnimatorSet.start();
            }
        }

        public void startToNormalAnimator() {
            if (COUISearchBar.this.mState.get() == 1 && this.mAnimatingAtomic.compareAndSet(false, true)) {
                startAnimateToNormalState();
                COUISearchBar.this.mSearchViewSmoothExitAnimatorSet.start();
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

    public static class DefaultAnimatorListener implements Animator.AnimatorListener {
        private DefaultAnimatorListener() {
        }

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
        }
    }

    public interface OnAnimationListener {
        void onAnimationEnd(int state);

        void onAnimationStart(int state);

        @Deprecated
        void onUpdate(int state, ValueAnimator valueAnimator);
    }

    public interface OnSearchBarBackgroundBoundsChangedListener {
        void onBackgroundBoundsChanged(int left, int top, int right, int bottom);
    }

    public interface OnStateChangeListener {
        void onStateChange(int fromState, int toState);
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface SearchViewState {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface SearchViewType {
    }

    public COUISearchBar(Context context) {
        this(context, null);
    }

    private int calculateRelativeTop(int index, int count, int value) {
        return index + ((count - value) / 2);
    }

    private float clampMarginValue(float fraction) {
        return Math.max(0.0f, Math.min(1.0f, fraction / CLAMP_ANIMATION_PERCENT));
    }

    private float clampSearchViewHeight(float fraction) {
        return (fraction / 0.7f) - 0.42857146f;
    }

    private void configImageViewDrawable(ImageView imageView, Drawable drawable, int index) {
        if (imageView != null) {
            imageView.setImageDrawable(drawable);
            imageView.setClickable(true);
            if (drawable != null) {
                int intrinsicWidth = (index - drawable.getIntrinsicWidth()) / 2;
                imageView.setPadding(intrinsicWidth, 0, intrinsicWidth, 0);
            }
        }
    }

    private void configResponsive() {
        if (COUIResponsiveUtils.isSmallScreen(getContext(), getMeasuredWidth())) {
            this.mResponsiveWidthSize = RESPONSIVE_WIDTH_TYPE_COMPAT;
        } else if (COUIResponsiveUtils.isMediumScreen(getContext(), getMeasuredWidth(), UIUtil.getScreenHeightMetrics(getContext()))) {
            this.mResponsiveWidthSize = RESPONSIVE_WIDTH_TYPE_MEDIUM;
        } else if (COUIResponsiveUtils.isLargeScreen(getContext(), getMeasuredWidth(), UIUtil.getScreenHeightMetrics(getContext()))) {
            this.mResponsiveWidthSize = RESPONSIVE_WIDTH_TYPE_EXPANDED;
        }
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        int intrinsicWidth = drawable.getIntrinsicWidth();
        int intrinsicHeight = drawable.getIntrinsicHeight();
        Bitmap bitmapCreateBitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, drawable.getOpacity() != -1 ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmapCreateBitmap);
        drawable.setBounds(0, 0, intrinsicWidth, intrinsicHeight);
        drawable.draw(canvas);
        return bitmapCreateBitmap;
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

    private ImageView ensureImageView(Drawable drawable, boolean enabled, boolean flag, int index) {
        if (drawable == null) {
            return null;
        }
        ImageView imageView = new ImageView(getContext());
        if (!flag) {
            imageView.setClickable(false);
            imageView.setFocusable(false);
        }
        if (enabled && flag) {
            COUIRippleDrawableUtil.setIconPressRippleDrawable(imageView, index);
        }
        addView(imageView);
        return imageView;
    }

    private void ensureNavigationView() {
        if (this.mNavigationView == null) {
            ImageView imageView = new ImageView(getContext());
            this.mNavigationView = imageView;
            COUIRippleDrawableUtil.setIconPressRippleDrawable(imageView, COUIMaskRippleDrawable.getMaskRippleRadiusByType(getContext(), 0));
            int dimensionPixelOffset = getContext().getResources().getDimensionPixelOffset(R.dimen.coui_action_bar_navigation_padding_start_material);
            this.mNavigationView.setPadding(dimensionPixelOffset, 0, dimensionPixelOffset, 0);
            addView(this.mNavigationView);
        }
    }


    public void ensureQuickDeleteButton() {
        Drawable drawableF;
        if (this.mQuickDeleteButton == null && (drawableF = androidx.core.content.res.ResourcesCompat.getDrawable(getResources(), this.mClearTextDrawableResourceId, getContext().getTheme())) != null) {
            ImageView imageViewEnsureImageView = ensureImageView(drawableF, true, true, this.mInnerIconSize / 2);
            this.mQuickDeleteButton = imageViewEnsureImageView;
            imageViewEnsureImageView.setContentDescription(getResources().getString(R.string.coui_search_clear_button_description));
            configImageViewDrawable(this.mQuickDeleteButton, drawableF, this.mInnerIconSize);
            showButton(this.mQuickDeleteButton, false);
            this.mQuickDeleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public final void onClick(View view) {
                    COUISearchBar.this.lambda$ensureQuickDeleteButton$1(view);
                }
            });
        }
    }


    public boolean functionalButtonShouldShow() {
        return !this.mHideFunctionalButton;
    }

    private AnimatorHelper getAnimatorHelper() {
        if (this.mAnimatorHelper == null) {
            this.mAnimatorHelper = new AnimatorHelper();
        }
        return this.mAnimatorHelper;
    }

    private int getInternalPaddingEnd() {
        return this.mUseResponsivePadding ? this.mResponsiveHorizontalPadding[this.mResponsiveWidthSize] : getPaddingEnd();
    }

    private int getInternalPaddingStart() {
        return this.mUseResponsivePadding ? this.mResponsiveHorizontalPadding[this.mResponsiveWidthSize] : getPaddingStart();
    }


    public int getOuterButtonCount() {
        return (shouldLayoutOut(this.mOuterPrimaryButton) ? 1 : 0) + (shouldLayoutOut(this.mOuterSecondaryButton) ? 1 : 0);
    }

    private View getSearchEditOrAnimationLayout() {
        COUIHintAnimationLayout cOUIHintAnimationLayout = this.mCOUIHintAnimationLayout;
        return cOUIHintAnimationLayout != null ? cOUIHintAnimationLayout : this.mSearchEditText;
    }

    private void init(Context context, AttributeSet attributeSet, int style, int index) {
        this.mIsAtLeastR = true;
        initDrawingProxyView();
        initEdittext();
        loadHideFunctionButtonAttr(context, attributeSet, style, index);
        initFunctionButton();
        this.mAttrs = attributeSet;
        if (attributeSet != null) {
            this.mStyle = attributeSet.getStyleAttribute();
        }
        if (this.mStyle == 0) {
            this.mStyle = style;
        }
        setWillNotDraw(false);
        setClipChildren(false);
        setClipToPadding(false);
        this.mCollapsedMinHeight = context.getResources().getDimensionPixelSize(R.dimen.coui_search_view_collapsed_min_height);
        this.mHorizontalDividerColor = context.getResources().getColor(R.color.coui_color_divider);
        this.mInitSearchBarHeight = context.getResources().getDimensionPixelOffset(R.dimen.coui_search_bar_height);
        this.mEditFrameEndGap = context.getResources().getDimensionPixelOffset(R.dimen.coui_search_bar_edittext_end_gap);
        this.mNonInstantSearchInnerGap = context.getResources().getDimensionPixelOffset(R.dimen.coui_search_bar_non_instant_search_inner_gap);
        this.mNormalBackgroundHeight = context.getResources().getDimensionPixelOffset(R.dimen.coui_search_bar_normal_background_height);
        this.mInnerIconSize = context.getResources().getDimensionPixelOffset(R.dimen.coui_search_bar_inner_search_icon_size);
        this.mSearchIconStartGap = context.getResources().getDimensionPixelOffset(R.dimen.coui_search_bar_inner_search_icon_start_gap);
        this.mDividerWidth = context.getResources().getDimensionPixelOffset(R.dimen.coui_search_bar_functional_button_divider_width);
        this.mDividerHeight = context.getResources().getDimensionPixelOffset(R.dimen.coui_search_bar_functional_button_divider_height);
        this.mOuterButtonWidth = context.getResources().getDimensionPixelOffset(R.dimen.coui_search_bar_outer_button_width);
        this.mNavigationButtonWidth = context.getResources().getDimensionPixelOffset(R.dimen.coui_search_bar_navigation_button_width);
        this.mOuterButtonGap = context.getResources().getDimensionPixelOffset(R.dimen.coui_search_bar_outer_button_gap_between_background);
        this.mFunctionButtonMaxWidth = context.getResources().getDimensionPixelOffset(R.dimen.coui_search_bar_functional_button_max_width);
        this.mFunctionButtonStartGap = context.getResources().getDimensionPixelOffset(R.dimen.coui_search_bar_functional_button_start_gap);
        this.mButtonOffsetAnimationDistance = context.getResources().getDimensionPixelOffset(R.dimen.coui_search_bar_functional_button_offset_distance);
        Resources resources = context.getResources();
        int value = R.dimen.coui_search_view_icon_size;
        this.mIconMaxWidth = resources.getDimensionPixelSize(value);
        this.mIconMaxHeight = context.getResources().getDimensionPixelSize(value);
        this.mFunctionButtonEndGap = new int[]{context.getResources().getDimensionPixelOffset(R.dimen.coui_search_bar_functional_button_end_gap_compat), context.getResources().getDimensionPixelOffset(R.dimen.coui_search_bar_functional_button_end_gap_medium), context.getResources().getDimensionPixelOffset(R.dimen.coui_search_bar_functional_button_end_gap_expanded)};
        this.mNavigationButtonStartGap = new int[]{context.getResources().getDimensionPixelOffset(R.dimen.coui_search_bar_navigation_button_start_gap_compat), context.getResources().getDimensionPixelOffset(R.dimen.coui_search_bar_navigation_button_start_gap_medium), context.getResources().getDimensionPixelOffset(R.dimen.coui_search_bar_navigation_button_start_gap_expanded)};
        this.mOuterButtonEndGap = new int[]{context.getResources().getDimensionPixelOffset(R.dimen.coui_search_bar_outer_button_end_gap_compat), context.getResources().getDimensionPixelOffset(R.dimen.coui_search_bar_outer_button_end_gap_medium), context.getResources().getDimensionPixelOffset(R.dimen.coui_search_bar_outer_button_end_gap_expanded)};
        this.mResponsiveHorizontalPadding = new int[]{context.getResources().getDimensionPixelOffset(R.dimen.coui_search_bar_responsive_horizontal_padding_compat), context.getResources().getDimensionPixelOffset(R.dimen.coui_search_bar_responsive_horizontal_padding_medium), context.getResources().getDimensionPixelOffset(R.dimen.coui_search_bar_responsive_horizontal_padding_expanded)};
        loadAttr(context, attributeSet, style, index);
        this.mNormalBackgroundColor = androidx.core.content.res.ResourcesCompat.getColor(getContext().getResources(), R.color.coui_search_view_selector_color_normal, getContext().getTheme());
        this.mPressedBackgroundColor = androidx.core.content.res.ResourcesCompat.getColor(getContext().getResources(), R.color.coui_search_view_selector_color_pressed, getContext().getTheme());
        this.mDrawingProxyDrawable.setButtonDividerColor(COUIContextUtil.getAttrColor(getContext(), R.attr.couiColorDivider));
        this.mDrawingProxyDrawable.setCurrentBackgroundColor(this.mNormalBackgroundColor);
        this.mSearchBarContentDesc = getResources().getString(R.string.coui_search_edit_box_description);
        this.mSearchBarHintTitleDesc = getResources().getString(R.string.support_abc_searchview_description_search);
    }

    private void initDrawingProxyView() {
        View view = new View(getContext());
        this.mDrawingProxyView = view;
        COUIDarkModeUtil.setForceDarkAllow(view, false);
        COUIMaskEffectDrawable cOUIMaskEffectDrawable = new COUIMaskEffectDrawable(getContext(), 0);
        this.mMaskDrawable = cOUIMaskEffectDrawable;
        cOUIMaskEffectDrawable.setMaskPath(this.mDrawingProxyDrawable.getNormalBackgroundPath());
        COUIStrokeDrawable cOUIStrokeDrawable = new COUIStrokeDrawable(getContext());
        this.mStrokeDrawable = cOUIStrokeDrawable;
        cOUIStrokeDrawable.setStrokePath(this.mDrawingProxyDrawable.getNormalBackgroundPath());
        COUIStateEffectDrawable cOUIStateEffectDrawable = new COUIStateEffectDrawable(new Drawable[]{this.mDrawingProxyDrawable, this.mMaskDrawable, this.mStrokeDrawable});
        this.mStateEffectBackground = cOUIStateEffectDrawable;
        this.mDrawingProxyView.setBackground(cOUIStateEffectDrawable);
        addView(this.mDrawingProxyView, new ViewGroup.LayoutParams(-1, -1));
        setDefaultFocusHighlightEnabled(false);
        setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public final void onFocusChange(View otherView, boolean enabled) {
                COUISearchBar.this.lambda$initDrawingProxyView$0(otherView, enabled);
            }
        });
    }

    private void initEdittext() {
        EditText editText = new EditText(new ContextThemeWrapper(getContext(), R.style.Widget_COUI_EditText_SearchViewStyle), null);
        this.mSearchEditText = editText;
        editText.setVerticalScrollBarEnabled(false);
        this.mSearchEditText.setMaxLines(DEFAULT_MAX_LINES);
        this.mSearchEditText.setInputType(1);
        this.mSearchEditText.setEllipsize(TextUtils.TruncateAt.END);
        this.mSearchEditText.setImeOptions(3);
        this.mSearchEditText.setId(R.id.search_src_text);
        this.mSearchEditText.setImportantForAccessibility(2);
        this.mSearchEditText.setImportantForAutofill(2);
        this.mSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                if (TextUtils.isEmpty(editable)) {
                    COUISearchBar cOUISearchBar = COUISearchBar.this;
                    cOUISearchBar.showButton(cOUISearchBar.mInnerPrimaryButton, true);
                    COUISearchBar cOUISearchBar2 = COUISearchBar.this;
                    cOUISearchBar2.showButton(cOUISearchBar2.mInnerSecondaryButton, true);
                    COUISearchBar cOUISearchBar3 = COUISearchBar.this;
                    cOUISearchBar3.showButton(cOUISearchBar3.mQuickDeleteButton, false);
                    return;
                }
                COUISearchBar cOUISearchBar4 = COUISearchBar.this;
                cOUISearchBar4.showButton(cOUISearchBar4.mInnerPrimaryButton, false);
                COUISearchBar cOUISearchBar5 = COUISearchBar.this;
                cOUISearchBar5.showButton(cOUISearchBar5.mInnerSecondaryButton, false);
                COUISearchBar cOUISearchBar6 = COUISearchBar.this;
                cOUISearchBar6.showButton(cOUISearchBar6.mQuickDeleteButton, true);
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int index, int count, int value) {
                COUISearchBar.this.ensureQuickDeleteButton();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int index, int count, int value) {
            }
        });
        addView(this.mSearchEditText);
    }

    private void initFunctionButton() {
        if (this.mHideFunctionalButton) {
            COUILog.i(TAG, "mHideFunctionalButton is true");
            return;
        }
        TextView textView = new TextView(getContext());
        this.mFunctionalButton = textView;
        textView.setMaxLines(DEFAULT_MAX_LINES);
        this.mFunctionalButton.setEllipsize(TextUtils.TruncateAt.END);
        this.mFunctionalButton.setTextAppearance(getContext(), R.style.couiTextAppearanceButton);
        this.mFunctionalButton.setText(R.string.coui_search_view_cancel);
        this.mFunctionalButton.setTextColor(androidx.core.content.res.ResourcesCompat.getColor(getResources(), R.color.coui_searchview_cancel_button_color, getContext().getTheme()));
        this.mFunctionalButton.setClickable(true);
        this.mFunctionalButton.setFocusable(true);
        this.mFunctionalButton.setAlpha(0.0f);
        this.mFunctionalButton.setVisibility(8);
        this.mFunctionalButton.setTextSize(0, COUIChangeTextUtil.getSuitableFontSize(this.mFunctionalButton.getTextSize(), getContext().getResources().getConfiguration().fontScale, 2));
        COUITextViewCompatUtil.setPressRippleDrawable(this.mFunctionalButton);
        addView(this.mFunctionalButton);
    }

    private boolean isInMultiWindowMode() {
        if (getContext() instanceof Activity) {
            return ((Activity) getContext()).isInMultiWindowMode();
        }
        return false;
    }

    private boolean isInsideBackground(float fraction, float ratio) {
        return this.mBackgroundRect.contains((int) fraction, (int) ratio);
    }

    private boolean isInsideFunctionButton(float fraction, float ratio) {
        return isInsideView(this.mFunctionalButton, fraction, ratio);
    }

    private boolean isInsideInnerButton(float fraction, float ratio) {
        return isInsideView(this.mInnerPrimaryButton, fraction, ratio) || isInsideView(this.mInnerSecondaryButton, fraction, ratio) || isInsideView(this.mQuickDeleteButton, fraction, ratio);
    }

    private boolean isInsideOuterButton(float fraction, float ratio) {
        return isInsideView(this.mOuterPrimaryButton, fraction, ratio) || isInsideView(this.mOuterSecondaryButton, fraction, ratio) || isInsideView(this.mNavigationView, fraction, ratio);
    }

    private boolean isInsideView(View view, float fraction, float ratio) {
        return view != null && view.getVisibility() != 8 && fraction >= ((float) view.getLeft()) && fraction <= ((float) view.getRight()) && ratio >= ((float) view.getTop()) && ratio <= ((float) view.getBottom());
    }

    private boolean isRtl() {
        return ViewCompat.getLayoutDirection(this) == 1;
    }


    public void lambda$ensureQuickDeleteButton$1(View view) {
        EditText editText = this.mSearchEditText;
        if (editText != null) {
            editText.setText((CharSequence) null);
        }
    }


    public void lambda$hideInToolBar$3(ValueAnimator valueAnimator) {
        setToolBarAlpha(((Float) valueAnimator.getAnimatedValue()).floatValue());
    }


    public void lambda$initDrawingProxyView$0(View view, boolean enabled) {
        this.mStateEffectBackground.setFocused(enabled);
    }


    public void lambda$setImeInsetsAnimationCallback$4(ImeInsetsAnimationCallback imeInsetsAnimationCallback) {
        ViewCompat.setWindowInsetsAnimationCallback(this.mSearchEditText.getRootView(), imeInsetsAnimationCallback);
    }


    public void lambda$showInToolBar$2(ValueAnimator valueAnimator) {
        setToolBarAlpha(((Float) valueAnimator.getAnimatedValue()).floatValue());
    }

    private void layoutBackground() {
        int right;
        int iWidth;
        View view = this.mDrawingProxyView;
        view.layout(0, 0, view.getMeasuredWidth(), this.mDrawingProxyView.getMeasuredHeight());
        int iCalculateRelativeTop = calculateRelativeTop(0, getMeasuredHeight(), this.mBackgroundRect.height());
        int iHeight = this.mBackgroundRect.height() + iCalculateRelativeTop;
        if (isRtl()) {
            iWidth = shouldLayoutOut(this.mNavigationView) ? this.mNavigationView.getLeft() : getMeasuredWidth() - (getInternalPaddingStart() - this.mExtraHorizontalBackground);
            right = iWidth - this.mBackgroundRect.width();
        } else {
            right = shouldLayoutOut(this.mNavigationView) ? this.mNavigationView.getRight() : getInternalPaddingStart() - this.mExtraHorizontalBackground;
            iWidth = this.mBackgroundRect.width() + right;
        }
        OnSearchBarBackgroundBoundsChangedListener onSearchBarBackgroundBoundsChangedListener = this.mOnSearchBarBackgroundBoundsChangedListener;
        if (onSearchBarBackgroundBoundsChangedListener != null) {
            Rect rect = this.mBackgroundRect;
            if (right != rect.left && iCalculateRelativeTop != rect.top && iWidth != rect.right && iHeight != rect.bottom) {
                onSearchBarBackgroundBoundsChangedListener.onBackgroundBoundsChanged(right, iCalculateRelativeTop, iWidth, iHeight);
            }
        }
        this.mBackgroundRect.set(right, iCalculateRelativeTop, iWidth, iHeight);
        updateBackgroundPath();
    }

    private void layoutBackgroundArea() {
        layoutBackground();
        layoutSearchIcon();
        layoutEditFrame();
        int iLayoutInnerButton = layoutInnerButton();
        if (this.mSearchViewType == 1) {
            layoutFunctionalButton(layoutDivider(iLayoutInnerButton));
        }
    }

    private void layoutBackgroundLeft() {
        if (this.mSearchViewType == 1) {
            layoutNavigationButton();
        }
    }

    private void layoutBackgroundRight() {
        int index = this.mSearchViewType;
        if (index != 0) {
            if (index == 1) {
                layoutOuterButton();
            }
        } else {
            layoutFunctionalButton(isRtl() ? this.mBackgroundRect.left - this.mFunctionButtonStartGap : this.mBackgroundRect.right + this.mFunctionButtonStartGap);
            if (getOuterButtonCount() == 1) {
                layoutOuterButton();
            }
        }
    }

    private int layoutDivider(int index) {
        int iWidth;
        int count;
        Rect rect = this.mBackgroundRect;
        int iCalculateRelativeTop = calculateRelativeTop(rect.top, rect.height(), this.mDividerRect.height());
        int iHeight = this.mDividerRect.height() + iCalculateRelativeTop;
        if (isRtl()) {
            int iWidth2 = index - this.mDividerRect.width();
            count = index - this.mNonInstantSearchInnerGap;
            iWidth = index;
            index = iWidth2;
        } else {
            iWidth = this.mDividerRect.width() + index;
            count = this.mNonInstantSearchInnerGap + index;
        }
        int iWidth3 = count + this.mDividerRect.width();
        this.mDividerRect.set(index, iCalculateRelativeTop, iWidth, iHeight);
        this.mDrawingProxyDrawable.setDividerRect(this.mDividerRect);
        return iWidth3;
    }

    private void layoutEditFrame() {
        Rect rect = this.mBackgroundRect;
        int iCalculateRelativeTop = calculateRelativeTop(rect.top, rect.height(), getSearchEditOrAnimationLayout().getMeasuredHeight());
        if (isRtl()) {
            int left = shouldLayoutOut(this.mSearchIconView) ? this.mSearchIconView.getLeft() : this.mBackgroundRect.right;
            getSearchEditOrAnimationLayout().layout(left - getSearchEditOrAnimationLayout().getMeasuredWidth(), iCalculateRelativeTop, left, getSearchEditOrAnimationLayout().getMeasuredHeight() + iCalculateRelativeTop);
        } else {
            int right = shouldLayoutOut(this.mSearchIconView) ? this.mSearchIconView.getRight() : this.mBackgroundRect.left;
            getSearchEditOrAnimationLayout().layout(right, iCalculateRelativeTop, getSearchEditOrAnimationLayout().getMeasuredWidth() + right, getSearchEditOrAnimationLayout().getMeasuredHeight() + iCalculateRelativeTop);
        }
    }

    private void layoutFunctionalButton(int index) {
        if (shouldLayoutOut(this.mFunctionalButton)) {
            Rect rect = this.mBackgroundRect;
            int iCalculateRelativeTop = calculateRelativeTop(rect.top, rect.height(), this.mFunctionalButton.getMeasuredHeight());
            if (isRtl()) {
                TextView textView = this.mFunctionalButton;
                textView.layout(index - textView.getMeasuredWidth(), iCalculateRelativeTop, index, this.mFunctionalButton.getMeasuredHeight() + iCalculateRelativeTop);
            } else {
                TextView textView2 = this.mFunctionalButton;
                textView2.layout(index, iCalculateRelativeTop, textView2.getMeasuredWidth() + index, this.mFunctionalButton.getMeasuredHeight() + iCalculateRelativeTop);
            }
        }
    }

    private int layoutInnerButton() {
        if (isRtl()) {
            int left = getSearchEditOrAnimationLayout().getLeft() - this.mEditFrameEndGap;
            if (shouldLayoutOut(this.mQuickDeleteButton)) {
                Rect rect = this.mBackgroundRect;
                int iCalculateRelativeTop = calculateRelativeTop(rect.top, rect.height(), this.mQuickDeleteButton.getMeasuredHeight());
                ImageView imageView = this.mQuickDeleteButton;
                imageView.layout(left - imageView.getMeasuredWidth(), iCalculateRelativeTop, left, this.mQuickDeleteButton.getMeasuredHeight() + iCalculateRelativeTop);
                left -= this.mQuickDeleteButton.getMeasuredWidth();
            }
            if (shouldLayoutOut(this.mInnerPrimaryButton)) {
                Rect rect2 = this.mBackgroundRect;
                int iCalculateRelativeTop2 = calculateRelativeTop(rect2.top, rect2.height(), this.mInnerPrimaryButton.getMeasuredHeight());
                ImageView imageView2 = this.mInnerPrimaryButton;
                imageView2.layout(left - imageView2.getMeasuredWidth(), iCalculateRelativeTop2, left, this.mInnerPrimaryButton.getMeasuredHeight() + iCalculateRelativeTop2);
                left -= this.mInnerPrimaryButton.getMeasuredWidth();
            }
            if (shouldLayoutOut(this.mInnerSecondaryButton)) {
                Rect rect3 = this.mBackgroundRect;
                int iCalculateRelativeTop3 = calculateRelativeTop(rect3.top, rect3.height(), this.mInnerSecondaryButton.getMeasuredHeight());
                ImageView imageView3 = this.mInnerSecondaryButton;
                imageView3.layout(left - imageView3.getMeasuredWidth(), iCalculateRelativeTop3, left, this.mInnerSecondaryButton.getMeasuredHeight() + iCalculateRelativeTop3);
                left -= this.mInnerSecondaryButton.getMeasuredWidth();
            }
            return left != getSearchEditOrAnimationLayout().getLeft() ? left - this.mNonInstantSearchInnerGap : left;
        }
        int right = getSearchEditOrAnimationLayout().getRight() + this.mEditFrameEndGap;
        if (shouldLayoutOut(this.mQuickDeleteButton)) {
            Rect rect4 = this.mBackgroundRect;
            int iCalculateRelativeTop4 = calculateRelativeTop(rect4.top, rect4.height(), this.mQuickDeleteButton.getMeasuredHeight());
            ImageView imageView4 = this.mQuickDeleteButton;
            imageView4.layout(right, iCalculateRelativeTop4, imageView4.getMeasuredWidth() + right, this.mQuickDeleteButton.getMeasuredHeight() + iCalculateRelativeTop4);
            right += this.mQuickDeleteButton.getMeasuredWidth();
        }
        if (shouldLayoutOut(this.mInnerPrimaryButton)) {
            Rect rect5 = this.mBackgroundRect;
            int iCalculateRelativeTop5 = calculateRelativeTop(rect5.top, rect5.height(), this.mInnerPrimaryButton.getMeasuredHeight());
            ImageView imageView5 = this.mInnerPrimaryButton;
            imageView5.layout(right, iCalculateRelativeTop5, imageView5.getMeasuredWidth() + right, this.mInnerPrimaryButton.getMeasuredHeight() + iCalculateRelativeTop5);
            right += this.mInnerPrimaryButton.getMeasuredWidth();
        }
        if (shouldLayoutOut(this.mInnerSecondaryButton)) {
            Rect rect6 = this.mBackgroundRect;
            int iCalculateRelativeTop6 = calculateRelativeTop(rect6.top, rect6.height(), this.mInnerSecondaryButton.getMeasuredHeight());
            ImageView imageView6 = this.mInnerSecondaryButton;
            imageView6.layout(right, iCalculateRelativeTop6, imageView6.getMeasuredWidth() + right, this.mInnerSecondaryButton.getMeasuredHeight() + iCalculateRelativeTop6);
            right += this.mInnerSecondaryButton.getMeasuredWidth();
        }
        return right != getSearchEditOrAnimationLayout().getRight() ? right + this.mNonInstantSearchInnerGap : right;
    }

    private void layoutNavigationButton() {
        if (shouldLayoutOut(this.mNavigationView)) {
            int iCalculateRelativeTop = calculateRelativeTop(0, getMeasuredHeight(), this.mNavigationView.getMeasuredHeight());
            if (isRtl()) {
                int measuredWidth = getMeasuredWidth() - this.mNavigationButtonStartGap[this.mResponsiveWidthSize];
                ImageView imageView = this.mNavigationView;
                imageView.layout(measuredWidth - imageView.getMeasuredWidth(), iCalculateRelativeTop, measuredWidth, this.mNavigationView.getMeasuredHeight() + iCalculateRelativeTop);
            } else {
                int index = this.mNavigationButtonStartGap[this.mResponsiveWidthSize];
                ImageView imageView2 = this.mNavigationView;
                imageView2.layout(index, iCalculateRelativeTop, imageView2.getMeasuredWidth() + index, this.mNavigationView.getMeasuredHeight() + iCalculateRelativeTop);
            }
        }
    }

    private void layoutOuterButton() {
        if (isRtl()) {
            int measuredWidth = this.mBackgroundRect.left - this.mOuterButtonGap;
            if (shouldLayoutOut(this.mOuterPrimaryButton)) {
                int iCalculateRelativeTop = calculateRelativeTop(0, getMeasuredHeight(), this.mOuterPrimaryButton.getMeasuredHeight());
                ImageView imageView = this.mOuterPrimaryButton;
                imageView.layout(measuredWidth - imageView.getMeasuredWidth(), iCalculateRelativeTop, measuredWidth, this.mOuterPrimaryButton.getMeasuredHeight() + iCalculateRelativeTop);
                measuredWidth -= this.mOuterPrimaryButton.getMeasuredWidth();
            }
            if (shouldLayoutOut(this.mOuterSecondaryButton)) {
                int iCalculateRelativeTop2 = calculateRelativeTop(0, getMeasuredHeight(), this.mOuterSecondaryButton.getMeasuredHeight());
                ImageView imageView2 = this.mOuterSecondaryButton;
                imageView2.layout(measuredWidth - imageView2.getMeasuredWidth(), iCalculateRelativeTop2, measuredWidth, this.mOuterSecondaryButton.getMeasuredHeight() + iCalculateRelativeTop2);
                return;
            }
            return;
        }
        int measuredWidth2 = this.mBackgroundRect.right + this.mOuterButtonGap;
        if (shouldLayoutOut(this.mOuterPrimaryButton)) {
            int iCalculateRelativeTop3 = calculateRelativeTop(0, getMeasuredHeight(), this.mOuterPrimaryButton.getMeasuredHeight());
            ImageView imageView3 = this.mOuterPrimaryButton;
            imageView3.layout(measuredWidth2, iCalculateRelativeTop3, imageView3.getMeasuredWidth() + measuredWidth2, this.mOuterPrimaryButton.getMeasuredHeight() + iCalculateRelativeTop3);
            measuredWidth2 += this.mOuterPrimaryButton.getMeasuredWidth();
        }
        if (shouldLayoutOut(this.mOuterSecondaryButton)) {
            int iCalculateRelativeTop4 = calculateRelativeTop(0, getMeasuredHeight(), this.mOuterSecondaryButton.getMeasuredHeight());
            ImageView imageView4 = this.mOuterSecondaryButton;
            imageView4.layout(measuredWidth2, iCalculateRelativeTop4, imageView4.getMeasuredWidth() + measuredWidth2, this.mOuterSecondaryButton.getMeasuredHeight() + iCalculateRelativeTop4);
        }
    }

    private void layoutSearchIcon() {
        if (shouldLayoutOut(this.mSearchIconView)) {
            Rect rect = this.mBackgroundRect;
            int iCalculateRelativeTop = calculateRelativeTop(rect.top, rect.height(), this.mSearchIconView.getMeasuredHeight());
            if (isRtl()) {
                int index = this.mBackgroundRect.right - this.mSearchIconStartGap;
                ImageView imageView = this.mSearchIconView;
                imageView.layout(index - imageView.getMeasuredWidth(), iCalculateRelativeTop, index, this.mSearchIconView.getMeasuredHeight() + iCalculateRelativeTop);
            } else {
                int count = this.mBackgroundRect.left + this.mSearchIconStartGap;
                ImageView imageView2 = this.mSearchIconView;
                imageView2.layout(count, iCalculateRelativeTop, imageView2.getMeasuredWidth() + count, this.mSearchIconView.getMeasuredHeight() + iCalculateRelativeTop);
            }
        }
    }

    private void loadAttr(Context context, AttributeSet attributeSet, int index, int count) {
        TypedArray typedArrayObtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.COUISearchBar, index, count);
        int delta = R.styleable.COUISearchBar_inputTextSize;
        if (typedArrayObtainStyledAttributes.hasValue(delta)) {
            int dimensionPixelSize = typedArrayObtainStyledAttributes.getDimensionPixelSize(delta, 0);
            this.mSearchEditText.setTextSize(0, dimensionPixelSize);
            COUIHintAnimationLayout cOUIHintAnimationLayout = this.mCOUIHintAnimationLayout;
            if (cOUIHintAnimationLayout != null) {
                cOUIHintAnimationLayout.setTextSize(dimensionPixelSize);
            }
        }
        int start = R.styleable.COUISearchBar_inputTextColor;
        if (typedArrayObtainStyledAttributes.hasValue(start)) {
            this.mSearchEditText.setTextColor(typedArrayObtainStyledAttributes.getColorStateList(start));
        }
        int end = R.styleable.COUISearchBar_normalHintColor;
        if (typedArrayObtainStyledAttributes.hasValue(end)) {
            this.mSearchEditText.setHintTextColor(typedArrayObtainStyledAttributes.getColorStateList(end));
        }
        int min = R.styleable.COUISearchBar_functionalButtonText;
        if (typedArrayObtainStyledAttributes.hasValue(min) && functionalButtonShouldShow()) {
            this.mFunctionalButton.setText(typedArrayObtainStyledAttributes.getString(min));
        }
        int max = R.styleable.COUISearchBar_functionalButtonTextColor;
        if (typedArrayObtainStyledAttributes.hasValue(max) && functionalButtonShouldShow()) {
            this.mFunctionalButton.setTextColor(typedArrayObtainStyledAttributes.getColorStateList(max));
        }
        int size = R.styleable.COUISearchBar_couiSearchIcon;
        Drawable drawable = typedArrayObtainStyledAttributes.hasValue(size) ? typedArrayObtainStyledAttributes.getDrawable(size) : androidx.core.content.res.ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.coui_search_view_icon, getContext().getTheme());
        int intrinsicWidth = drawable.getIntrinsicWidth();
        int pos = this.mIconMaxWidth;
        if (intrinsicWidth > pos) {
            drawable = zoomDrawable(drawable, (int) (pos * getResources().getDisplayMetrics().density), (int) (this.mIconMaxHeight * getResources().getDisplayMetrics().density));
        }
        if (this.mSearchIconView == null) {
            ImageView imageViewEnsureImageView = ensureImageView(drawable, false, false, 0);
            this.mSearchIconView = imageViewEnsureImageView;
            imageViewEnsureImageView.setId(R.id.animated_search_icon);
            this.mSearchIconView.setImportantForAccessibility(2);
        }
        configImageViewDrawable(this.mSearchIconView, drawable, this.mInnerIconSize);
        int width = R.styleable.COUISearchBar_searchHint;
        if (typedArrayObtainStyledAttributes.hasValue(width)) {
            this.mSearchEditText.setHint(typedArrayObtainStyledAttributes.getString(width));
        }
        this.mClearTextDrawableResourceId = typedArrayObtainStyledAttributes.getResourceId(R.styleable.COUISearchBar_couiSearchClearSelector, R.drawable.ic_edit_text_delete_search_view);
        typedArrayObtainStyledAttributes.recycle();
    }

    private void loadHideFunctionButtonAttr(Context context, AttributeSet attributeSet, int index, int count) {
        TypedArray typedArrayObtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.COUISearchBar, index, count);
        this.mHideFunctionalButton = typedArrayObtainStyledAttributes.getBoolean(R.styleable.COUISearchBar_couiHideFunctionalButton, false);
        typedArrayObtainStyledAttributes.recycle();
    }

    private int measureBackground(int index) {
        measureFunctionalButton();
        measureView(this.mDrawingProxyView, View.MeasureSpec.makeMeasureSpec(getMeasuredWidth(), 1073741824), View.MeasureSpec.makeMeasureSpec(getMeasuredHeight(), 1073741824));
        int count = this.mSearchViewType;
        if (count == 0) {
            int collapsedWidth = (getMeasuredWidth() - getInternalPaddingStart()) - (!functionalButtonShouldShow() ? getInternalPaddingEnd() : (this.mFunctionalButton.getMeasuredWidth() + this.mFunctionButtonStartGap) + this.mFunctionButtonEndGap[this.mResponsiveWidthSize]);
            int measuredWidth = (int) (collapsedWidth + ((index - collapsedWidth) * (1.0f - this.mShrinkFraction)));
            measureRect(this.mBackgroundRect, (this.mExtraHorizontalBackground * 2) + measuredWidth, (int) Float.max(this.mCollapsedMinHeight, this.mNormalBackgroundHeight * this.mBackgroundScaleFraction));
            return measuredWidth;
        }
        if (count != 1) {
            return index;
        }
        measureRect(this.mBackgroundRect, index, (int) Float.max(this.mCollapsedMinHeight, this.mNormalBackgroundHeight * this.mBackgroundScaleFraction));
        return index;
    }

    private int measureDivider(int index) {
        if (this.mSearchViewType != 1) {
            return index;
        }
        measureRect(this.mDividerRect, this.mDividerWidth, this.mDividerHeight);
        return (index - this.mNonInstantSearchInnerGap) - this.mDividerWidth;
    }

    private void measureEditFrame(int index) {
        measureView(getSearchEditOrAnimationLayout(), View.MeasureSpec.makeMeasureSpec(index, 1073741824), View.MeasureSpec.makeMeasureSpec(this.mNormalBackgroundHeight, Integer.MIN_VALUE));
    }

    private void measureFunctionalButton() {
        if (shouldLayoutOut(this.mFunctionalButton)) {
            measureView(this.mFunctionalButton, View.MeasureSpec.makeMeasureSpec(this.mFunctionButtonMaxWidth, Integer.MIN_VALUE), View.MeasureSpec.makeMeasureSpec(getMeasuredHeight(), Integer.MIN_VALUE));
        }
    }

    private int measureInnerButton(int index) {
        int iMeasureView = shouldLayoutOut(this.mInnerPrimaryButton) ? index - measureView(this.mInnerPrimaryButton, View.MeasureSpec.makeMeasureSpec(this.mInnerIconSize, 1073741824), View.MeasureSpec.makeMeasureSpec(this.mInnerIconSize, 1073741824)) : index;
        if (shouldLayoutOut(this.mInnerSecondaryButton)) {
            iMeasureView -= measureView(this.mInnerSecondaryButton, View.MeasureSpec.makeMeasureSpec(this.mInnerIconSize, 1073741824), View.MeasureSpec.makeMeasureSpec(this.mInnerIconSize, 1073741824));
        }
        if (shouldLayoutOut(this.mQuickDeleteButton)) {
            iMeasureView -= measureView(this.mQuickDeleteButton, View.MeasureSpec.makeMeasureSpec(this.mInnerIconSize, 1073741824), View.MeasureSpec.makeMeasureSpec(this.mInnerIconSize, 1073741824));
        }
        return iMeasureView != index ? iMeasureView - this.mNonInstantSearchInnerGap : iMeasureView;
    }

    private void measureInsideBackground(int index) {
        int iMeasureSearchIcon = measureSearchIcon(index);
        int value = this.mSearchViewType;
        if (value == 0) {
            iMeasureSearchIcon = measureInnerButton(iMeasureSearchIcon);
        } else if (value == 1) {
            if (shouldLayoutOut(this.mFunctionalButton)) {
                iMeasureSearchIcon = measureDivider(iMeasureSearchIcon - (this.mFunctionalButton.getMeasuredWidth() + this.mNonInstantSearchInnerGap));
            }
            iMeasureSearchIcon = measureInnerButton(iMeasureSearchIcon);
        }
        measureEditFrame(iMeasureSearchIcon - this.mEditFrameEndGap);
    }

    private int measureNavigationButton(int index) {
        if (!shouldLayoutOut(this.mNavigationView) || this.mSearchViewType != 1) {
            return index;
        }
        int iMeasureView = index - measureView(this.mNavigationView, View.MeasureSpec.makeMeasureSpec(this.mNavigationButtonWidth, 1073741824), View.MeasureSpec.makeMeasureSpec(getMeasuredHeight(), 1073741824));
        return (iMeasureView == index || !this.mUseResponsivePadding) ? iMeasureView : (iMeasureView + getInternalPaddingStart()) - this.mNavigationButtonStartGap[this.mResponsiveWidthSize];
    }

    private int measureOuterButton(int index) {
        if (this.mSearchViewType != 1 && getOuterButtonCount() != 1) {
            return index;
        }
        int iMeasureView = shouldLayoutOut(this.mOuterPrimaryButton) ? index - measureView(this.mOuterPrimaryButton, View.MeasureSpec.makeMeasureSpec(this.mOuterButtonWidth, 1073741824), View.MeasureSpec.makeMeasureSpec(getMeasuredHeight(), 1073741824)) : index;
        if (shouldLayoutOut(this.mOuterSecondaryButton)) {
            iMeasureView -= measureView(this.mOuterSecondaryButton, View.MeasureSpec.makeMeasureSpec(this.mOuterButtonWidth, 1073741824), View.MeasureSpec.makeMeasureSpec(getMeasuredHeight(), 1073741824));
        }
        if (iMeasureView == index) {
            return iMeasureView;
        }
        if (this.mUseResponsivePadding) {
            iMeasureView = (iMeasureView + getInternalPaddingEnd()) - this.mOuterButtonEndGap[this.mResponsiveWidthSize];
        }
        return iMeasureView - this.mOuterButtonGap;
    }

    private int measureOutsideBackground() {
        return measureOuterButton(measureNavigationButton((getMeasuredWidth() - getInternalPaddingStart()) - getInternalPaddingEnd()));
    }

    private void measureRect(Rect rect, int index, int count) {
        if (rect != null) {
            rect.set(0, 0, index, count);
        }
    }

    private int measureSearchIcon(int index) {
        if (!shouldLayoutOut(this.mSearchIconView)) {
            return index;
        }
        int iMeasureView = index - measureView(this.mSearchIconView, View.MeasureSpec.makeMeasureSpec(this.mInnerIconSize, 1073741824), View.MeasureSpec.makeMeasureSpec(this.mInnerIconSize, 1073741824));
        return iMeasureView != index ? iMeasureView - this.mSearchIconStartGap : iMeasureView;
    }

    private int measureView(View view, int index, int count) {
        view.measure(index, count);
        return view.getMeasuredWidth();
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

    private void removeLast() {
        int childCount = this.mToolBar.getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (getClass().isInstance(this.mToolBar.getChildAt(i))) {
                this.mToolBar.removeViewAt(i);
                return;
            }
        }
    }

    private void setMenuItem(MenuItem menuItem) {
        this.mMenuItem = menuItem;
        if (menuItem == null || menuItem.getActionView() != this) {
            return;
        }
        this.mMenuItem.setActionView((View) null);
    }


    public void setOuterButtonVisibility(int index) {
        if (index == 0 || index == 4 || index == 8) {
            ImageView imageView = this.mOuterPrimaryButton;
            if (imageView != null) {
                imageView.setVisibility(index);
            }
            ImageView imageView2 = this.mOuterSecondaryButton;
            if (imageView2 != null) {
                imageView2.setVisibility(index);
            }
        }
    }

    private void setToolBarAlpha(float fraction) {
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

    private boolean shouldLayoutOut(View view) {
        if (view == null) {
            return false;
        }
        return view instanceof ImageView ? (((ImageView) view).getDrawable() == null || view.getVisibility() == 8) ? false : true : view.getVisibility() != 8;
    }


    public void showButton(View view, boolean enabled) {
        if (view != null) {
            view.setVisibility(enabled ? 0 : 8);
        }
    }

    private void showSoftInput() {
        CustomWindowInsetsAnimationControlListener customWindowInsetsAnimationControlListener = new CustomWindowInsetsAnimationControlListener(true, this.mShowImeAnimDuration, this.mShowImeInterpolator);
        if (this.mSearchEditText.getWindowInsetsController() != null) {
            this.mSearchEditText.getWindowInsetsController().controlWindowInsetsAnimation(WindowInsets.Type.ime(), this.mShowImeAnimDuration, this.mShowImeInterpolator, null, customWindowInsetsAnimationControlListener);
        }
    }

    private void updateAccessibilityImportance(int index) {
        if (index == 1) {
            this.mSearchEditText.setImportantForAccessibility(1);
        } else if (index == 0) {
            this.mSearchEditText.setImportantForAccessibility(2);
        }
    }

    private void updateBackgroundPath() {
        this.mDrawingProxyDrawable.setBackgroundRect(this.mBackgroundRect);
    }

    private Drawable zoomDrawable(Drawable drawable, int index, int count) {
        return new BitmapDrawable(Bitmap.createScaledBitmap(drawableToBitmap(drawable), index, count, true));
    }

    public void addOnStateChangeListener(OnStateChangeListener onStateChangeListener) {
        if (this.mOnStateChangeListeners == null) {
            this.mOnStateChangeListeners = new ArrayList();
        }
        this.mOnStateChangeListeners.add(onStateChangeListener);
    }

    public void changeState(int index, boolean enabled) {
        if (enabled) {
            changeStateWithAnimation(index);
        } else {
            changeStateImmediately(index);
        }
    }

    public void changeStateImmediately(int index) {
        if (this.mState.get() == index) {
            COUILog.d(TAG, "changeStateImmediately: same state , return. targetState = " + index);
            return;
        }
        updateAccessibilityImportance(index);
        if (this.mState.get() == 1) {
            getAnimatorHelper().runStateChangeImmediately(0);
        } else if (this.mState.get() == 0) {
            getAnimatorHelper().runStateChangeImmediately(1);
        }
    }

    public void changeStateWithAnimation(int index) {
        if (this.mState.get() == index) {
            COUILog.d(TAG, "changeStateWithAnimation: same state , return. targetState = " + index);
            return;
        }
        updateAccessibilityImportance(index);
        if (this.mState.get() == 1) {
            getAnimatorHelper().runStateChangeAnimation(0);
        } else if (this.mState.get() == 0) {
            getAnimatorHelper().runStateChangeAnimation(1);
        }
    }

    public void controlImeShowAnim(int showImeAnimDuration, Interpolator interpolator) {
        this.mShowImeAnimDuration = showImeAnimDuration;
        this.mShowImeInterpolator = interpolator;
    }

    @Override
    public void dispatchDraw(Canvas canvas) {
        float fraction = this.mCollapsingHeightPercent;
        if (fraction >= 1.0f || fraction < FLOAT_POINT_SEVEN_FIVE) {
            super.dispatchDraw(canvas);
            return;
        }
        canvas.save();
        canvas.clipRect(0, this.mBackgroundRect.top, getWidth(), this.mBackgroundRect.bottom);
        super.dispatchDraw(canvas);
        canvas.restore();
    }

    @Override
    public boolean dispatchHoverEvent(MotionEvent motionEvent) {
        if (isEnabled() && (motionEvent.getActionMasked() == 9 || motionEvent.getActionMasked() == 7)) {
            this.mStateEffectBackground.setHovered(isInsideBackground(motionEvent.getX(), motionEvent.getY()));
        }
        if (motionEvent.getActionMasked() == 10) {
            this.mStateEffectBackground.setHovered(false);
        }
        return super.dispatchHoverEvent(motionEvent);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        if (!isEnabled()) {
            return super.dispatchTouchEvent(motionEvent);
        }
        int action = motionEvent.getAction();
        if (action != 0) {
            if (action == 1 || action == 3) {
                if (isInsideBackground(motionEvent.getX(), motionEvent.getY()) || this.mPressed) {
                    this.mPressed = false;
                    this.mStateEffectBackground.setTouched(false);
                }
            } else if (!isInsideBackground(motionEvent.getX(), motionEvent.getY()) && this.mPressed) {
                this.mPressed = false;
                this.mStateEffectBackground.setTouched(false);
            }
        } else {
            if (motionEvent.getY() < this.mBackgroundRect.top || motionEvent.getY() > this.mBackgroundRect.bottom) {
                return false;
            }
            if (isInsideBackground(motionEvent.getX(), motionEvent.getY()) && !isInsideInnerButton(motionEvent.getX(), motionEvent.getY()) && !isInsideFunctionButton(motionEvent.getX(), motionEvent.getY())) {
                this.mPressed = true;
                this.mStateEffectBackground.setTouched(true);
            }
        }
        return super.dispatchTouchEvent(motionEvent);
    }

    public TextView getFunctionalButton() {
        return this.mFunctionalButton;
    }

    public COUIHintAnimationLayout getHintAnimationLayout() {
        if (this.mCOUIHintAnimationLayout == null) {
            this.mCOUIHintAnimationLayout = new COUIHintAnimationLayout(getContext());
            removeView(this.mSearchEditText);
            this.mCOUIHintAnimationLayout.setSearchEditText(this.mSearchEditText);
            addView(this.mCOUIHintAnimationLayout);
        }
        return this.mCOUIHintAnimationLayout;
    }

    public View getInnerPrimaryButton() {
        return this.mInnerPrimaryButton;
    }

    public View getInnerSecondaryButton() {
        return this.mInnerSecondaryButton;
    }

    public boolean getInputMethodAnimationEnabled() {
        return this.mInputMethodAnimationEnabled;
    }

    public View getNavigationView() {
        return this.mNavigationView;
    }

    public View getOuterPrimaryButton() {
        return this.mOuterPrimaryButton;
    }

    public View getOuterSecondaryButton() {
        return this.mOuterSecondaryButton;
    }

    public View getQuickDeleteButton() {
        return this.mQuickDeleteButton;
    }

    public EditText getSearchEditText() {
        return this.mSearchEditText;
    }

    public int getSearchState() {
        return this.mState.get();
    }

    public float getSearchViewAnimateHeightPercent() {
        return this.mCollapsingHeightPercent;
    }

    public void hideInToolBar() {
        if (this.mToolBar == null || this.mToolBarAnimationRunning) {
            return;
        }
        this.mToolBarAnimationRunning = true;
        ensureAddedToToolBar();
        if (this.mAddToToolbarWay == WAY_AT_BEHIND) {
            animate().alpha(0.0f).setDuration(DEFAULT_FADE_DURATION).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animator) {
                    super.onAnimationEnd(animator);
                    COUISearchBar.this.setVisibility(8);
                }
            }).start();
        }
        ValueAnimator valueAnimatorOfFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        valueAnimatorOfFloat.setDuration(DEFAULT_FADE_DURATION);
        valueAnimatorOfFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                COUISearchBar.this.lambda$hideInToolBar$3(valueAnimator);
            }
        });
        valueAnimatorOfFloat.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animator) {
                super.onAnimationEnd(animator);
                COUISearchBar.this.mToolBarAnimationRunning = false;
                COUISearchBar.this.setToolBarChildVisibility(0);
            }

            @Override
            public void onAnimationStart(Animator animator) {
                super.onAnimationStart(animator);
                if (COUISearchBar.this.mInputMethodAnimationEnabled) {
                    COUISearchBar.this.openSoftInput(false);
                }
            }
        });
        valueAnimatorOfFloat.start();
    }

    @Override
    public void onActionViewCollapsed() {
    }

    @Override
    public void onActionViewExpanded() {
    }

    @Override
    public void onImeAnimStart() {
        if (this.mSearchEditText.getRootWindowInsets() == null || !this.mSearchEditText.getRootWindowInsets().isVisible(WindowInsets.Type.ime())) {
            return;
        }
        getAnimatorHelper().startAnimateToEditState();
        this.mSearchViewSmoothEnterAnimatorSet.start();
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(accessibilityNodeInfo);
        if (this.mSearchEditText.getImportantForAccessibility() == 1) {
            return;
        }
        String str = "";
        if (!TextUtils.isEmpty(this.mSearchBarContentDesc)) {
            str = "" + this.mSearchBarContentDesc;
        }
        COUIHintAnimationLayout cOUIHintAnimationLayout = this.mCOUIHintAnimationLayout;
        if (cOUIHintAnimationLayout != null && cOUIHintAnimationLayout.getCurrentHintTextView() != null) {
            str = str + COUIAccessibilityUtil.PAUSE_STRING + this.mSearchBarHintTitleDesc + ((Object) this.mCOUIHintAnimationLayout.getCurrentHintTextView().getText());
        }
        accessibilityNodeInfo.setContentDescription(str);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        if (isInsideInnerButton(motionEvent.getX(), motionEvent.getY()) || isInsideOuterButton(motionEvent.getX(), motionEvent.getY())) {
            return false;
        }
        if (this.mState.get() == 0) {
            return true;
        }
        return super.onInterceptTouchEvent(motionEvent);
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        layoutBackgroundLeft();
        layoutBackgroundArea();
        layoutBackgroundRight();
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(View.MeasureSpec.getSize(widthMeasureSpec), View.MeasureSpec.getSize(heightMeasureSpec));
        configResponsive();
        measureInsideBackground(measureBackground(measureOutsideBackground()));
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
        if (motionEvent.getAction() == 1 && !isInsideBackground(motionEvent.getX(), motionEvent.getY())) {
            MotionEvent motionEventObtain = MotionEvent.obtain(motionEvent);
            motionEventObtain.setAction(3);
            super.onTouchEvent(motionEventObtain);
            motionEventObtain.recycle();
        }
        super.onTouchEvent(motionEvent);
        return true;
    }

    public void openSoftInput(boolean enabled) {
        if (this.mSearchEditText != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService("input_method");
            COUILog.d(TAG, "openSoftInput: " + enabled);
            if (!enabled) {
                this.mSearchEditText.clearFocus();
                if (inputMethodManager == null || !inputMethodManager.isActive()) {
                    return;
                }
                inputMethodManager.hideSoftInputFromWindow(this.mSearchEditText.getWindowToken(), 0);
                return;
            }
            this.mSearchEditText.requestFocus();
            if (inputMethodManager != null) {
                if (this.mIsAtLeastR && this.mShowImeAnimDuration != 0 && !isInMultiWindowMode()) {
                    showSoftInput();
                    return;
                }
                WindowInsetsController windowInsetsController = this.mSearchEditText.getWindowInsetsController();
                if (windowInsetsController != null) {
                    windowInsetsController.show(WindowInsets.Type.ime());
                }
            }
        }
    }

    public void refresh() {
        loadAttr(getContext(), this.mAttrs, this.mStyle, COUIContextUtil.isCOUIDarkTheme(getContext()) ? R.style.Widget_COUI_COUISearchBar_Dark : R.style.Widget_COUI_COUISearchBar);
        ImageView imageView = this.mQuickDeleteButton;
        if (imageView != null) {
            imageView.setImageDrawable(androidx.core.content.res.ResourcesCompat.getDrawable(getContext().getResources(), this.mClearTextDrawableResourceId, getContext().getTheme()));
        }
        COUIStateEffectDrawable cOUIStateEffectDrawable = this.mStateEffectBackground;
        if (cOUIStateEffectDrawable != null) {
            cOUIStateEffectDrawable.refresh(getContext());
        }
    }

    public void removeOnStateChangeListener(OnStateChangeListener onStateChangeListener) {
        List<OnStateChangeListener> list = this.mOnStateChangeListeners;
        if (list == null) {
            return;
        }
        list.remove(onStateChangeListener);
    }

    public void setAtBehindToolBar(COUIToolbar cOUIToolbar, int gravityInToolBar, MenuItem menuItem) {
        this.mToolBar = cOUIToolbar;
        this.mGravityInToolBar = gravityInToolBar;
        this.mAddToToolbarWay = WAY_AT_BEHIND;
        setMenuItem(menuItem);
        setVisibility(8);
    }

    public void setAtFrontToolBar(COUIToolbar cOUIToolbar, int gravityInToolBar, MenuItem menuItem) {
        this.mToolBar = cOUIToolbar;
        this.mGravityInToolBar = gravityInToolBar;
        this.mAddToToolbarWay = WAY_AT_FRONT;
        setMenuItem(menuItem);
        ensureAddedToToolBar();
        menuItem.setVisible(false);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (functionalButtonShouldShow()) {
            this.mFunctionalButton.setEnabled(enabled);
        }
        this.mDrawingProxyView.setEnabled(enabled);
        ImageView imageView = this.mSearchIconView;
        if (imageView != null) {
            imageView.setEnabled(enabled);
        }
        ImageView imageView2 = this.mNavigationView;
        if (imageView2 != null) {
            imageView2.setEnabled(enabled);
        }
        ImageView imageView3 = this.mQuickDeleteButton;
        if (imageView3 != null) {
            imageView3.setEnabled(enabled);
        }
        ImageView imageView4 = this.mInnerPrimaryButton;
        if (imageView4 != null) {
            imageView4.setEnabled(enabled);
        }
        ImageView imageView5 = this.mInnerSecondaryButton;
        if (imageView5 != null) {
            imageView5.setEnabled(enabled);
        }
        ImageView imageView6 = this.mOuterPrimaryButton;
        if (imageView6 != null) {
            imageView6.setEnabled(enabled);
        }
        ImageView imageView7 = this.mOuterSecondaryButton;
        if (imageView7 != null) {
            imageView7.setEnabled(enabled);
        }
    }

    public void setExtraActivateMarginTop(int extraY) {
        getAnimatorHelper().mExtraY = extraY;
    }

    public void setFunctionalButtonText(String str) {
        if (functionalButtonShouldShow()) {
            this.mFunctionalButton.setText(str);
        }
    }

    public void setHideFunctionalButton(boolean hideFunctionalButton) {
        if (this.mHideFunctionalButton != hideFunctionalButton) {
            this.mHideFunctionalButton = hideFunctionalButton;
            if (hideFunctionalButton) {
                this.mFunctionalButton.setVisibility(8);
            } else {
                if (this.mFunctionalButton == null) {
                    initFunctionButton();
                }
                this.mFunctionalButton.setVisibility(0);
            }
            requestLayout();
        }
    }

    public void setImeInsetsAnimationCallback() {
        if (this.mIsAtLeastR) {
            this.mShowImeAnimDuration = 450;
            this.mShowImeInterpolator = DEFAULT_SEARCH_VIEW_OFFSET_CHANGE_INTERPOLATOR;
            final ImeInsetsAnimationCallback imeInsetsAnimationCallback = new ImeInsetsAnimationCallback();
            imeInsetsAnimationCallback.setImeAnimationListener(this);
            this.mSearchEditText.post(new Runnable() {
                @Override
                public final void run() {
                    COUISearchBar.this.lambda$setImeInsetsAnimationCallback$4(imeInsetsAnimationCallback);
                }
            });
        }
    }

    public void setInnerPrimaryButton(Drawable drawable) {
        int intrinsicWidth = drawable.getIntrinsicWidth();
        int index = this.mIconMaxWidth;
        if (intrinsicWidth > index) {
            drawable = zoomDrawable(drawable, (int) (index * getResources().getDisplayMetrics().density), (int) (this.mIconMaxHeight * getResources().getDisplayMetrics().density));
        }
        if (this.mInnerPrimaryButton == null) {
            this.mInnerPrimaryButton = ensureImageView(drawable, true, true, this.mInnerIconSize / 2);
        }
        ImageView imageView = this.mInnerPrimaryButton;
        if (imageView != null) {
            configImageViewDrawable(imageView, drawable, this.mInnerIconSize);
        }
    }

    public void setInnerSecondaryButton(Drawable drawable) {
        int intrinsicWidth = drawable.getIntrinsicWidth();
        int index = this.mIconMaxWidth;
        if (intrinsicWidth > index) {
            drawable = zoomDrawable(drawable, (int) (index * getResources().getDisplayMetrics().density), (int) (this.mIconMaxHeight * getResources().getDisplayMetrics().density));
        }
        if (this.mInnerSecondaryButton == null) {
            this.mInnerSecondaryButton = ensureImageView(drawable, true, true, this.mInnerIconSize / 2);
        }
        ImageView imageView = this.mInnerSecondaryButton;
        if (imageView != null) {
            configImageViewDrawable(imageView, drawable, this.mInnerIconSize);
        }
    }

    public void setInputMethodAnimationEnabled(boolean inputMethodAnimationEnabled) {
        this.mInputMethodAnimationEnabled = inputMethodAnimationEnabled;
    }

    public void setNavigationViewDrawable(Drawable drawable) {
        ensureNavigationView();
        this.mNavigationView.setImageDrawable(drawable);
        this.mNavigationView.setClickable(true);
    }

    public void setOnAnimationListener(OnAnimationListener onAnimationListener) {
        this.mOnAnimationListener = onAnimationListener;
    }

    public void setOnSearchBarBackgroundBoundsChangedListener(OnSearchBarBackgroundBoundsChangedListener onSearchBarBackgroundBoundsChangedListener) {
        this.mOnSearchBarBackgroundBoundsChangedListener = onSearchBarBackgroundBoundsChangedListener;
    }

    public void setOuterPrimaryButton(Drawable drawable) {
        if (drawable == null) {
            ImageView imageView = this.mOuterPrimaryButton;
            if (imageView != null) {
                removeView(imageView);
                this.mOuterPrimaryButton = null;
                return;
            }
            return;
        }
        if (this.mOuterPrimaryButton == null) {
            this.mOuterPrimaryButton = ensureImageView(drawable, true, true, this.mOuterButtonWidth / 2);
        }
        ImageView imageView2 = this.mOuterPrimaryButton;
        if (imageView2 != null) {
            configImageViewDrawable(imageView2, drawable, this.mOuterButtonWidth);
        }
    }

    public void setOuterSecondaryButton(Drawable drawable) {
        if (drawable == null) {
            ImageView imageView = this.mOuterSecondaryButton;
            if (imageView != null) {
                removeView(imageView);
                this.mOuterSecondaryButton = null;
                return;
            }
            return;
        }
        if (this.mOuterSecondaryButton == null) {
            this.mOuterSecondaryButton = ensureImageView(drawable, true, true, this.mOuterButtonWidth / 2);
        }
        ImageView imageView2 = this.mOuterSecondaryButton;
        if (imageView2 != null) {
            configImageViewDrawable(imageView2, drawable, this.mOuterButtonWidth);
        }
    }

    public void setSearchAnimateType(int searchViewType) {
        if (this.mState.get() != 1) {
            this.mSearchViewType = searchViewType;
            requestLayout();
            return;
        }
        COUILog.d(TAG, "setSearchAnimateType to " + TYPE_NAME[searchViewType] + " is not allowed in STATE_EDIT");
    }

    public void setSearchBackgroundColor(ColorStateList colorStateList) {
        if (colorStateList != null) {
            int index = this.mNormalBackgroundColor;
            int defaultColor = colorStateList.getDefaultColor();
            this.mNormalBackgroundColor = defaultColor;
            this.mPressedBackgroundColor = colorStateList.getColorForState(new int[]{16842919}, defaultColor);
            if (this.mDrawingProxyDrawable.getCurrentBackgroundColor() == index) {
                this.mDrawingProxyDrawable.setCurrentBackgroundColor(this.mNormalBackgroundColor);
            }
            invalidate();
        }
    }

    public void setSearchViewAnimateHeightPercent(float collapsingHeightPercent) {
        this.mCollapsingHeightPercent = collapsingHeightPercent;
        this.mBackgroundScaleFraction = clampSearchViewHeight(collapsingHeightPercent);
        this.mExtraHorizontalBackground = (int) (getInternalPaddingEnd() * (1.0f - clampMarginValue(collapsingHeightPercent)));
        setTranslationY(Math.max(0.0f, ((this.mInitSearchBarHeight / 2.0f) * (1.0f - collapsingHeightPercent)) - 1.0f));
        float fraction = (collapsingHeightPercent - FLOAT_POINT_SEVEN_FIVE) * FLOAT_FOUR;
        ImageView imageView = this.mSearchIconView;
        if (imageView != null) {
            imageView.setAlpha(fraction);
        }
        ImageView imageView2 = this.mInnerPrimaryButton;
        if (imageView2 != null) {
            imageView2.setAlpha(fraction);
        }
        ImageView imageView3 = this.mInnerSecondaryButton;
        if (imageView3 != null) {
            imageView3.setAlpha(fraction);
        }
        ImageView imageView4 = this.mOuterPrimaryButton;
        if (imageView4 != null) {
            imageView4.setAlpha(fraction);
        }
        ImageView imageView5 = this.mOuterSecondaryButton;
        if (imageView5 != null) {
            imageView5.setAlpha(fraction);
        }
        this.mDrawingProxyDrawable.setCurrentBackgroundColor(((Integer) DEFAULT_EVALUATOR.evaluate(clampMarginValue(collapsingHeightPercent), Integer.valueOf(this.mHorizontalDividerColor), Integer.valueOf(this.mNormalBackgroundColor))).intValue());
        COUIHintAnimationLayout cOUIHintAnimationLayout = this.mCOUIHintAnimationLayout;
        if (cOUIHintAnimationLayout != null) {
            cOUIHintAnimationLayout.setAlpha(fraction);
        } else {
            this.mSearchEditText.setAlpha(fraction);
        }
        COUIHintAnimationLayout cOUIHintAnimationLayout2 = this.mCOUIHintAnimationLayout;
        if (cOUIHintAnimationLayout2 != null) {
            if (collapsingHeightPercent < 1.0f) {
                cOUIHintAnimationLayout2.pauseHintsAnimation();
            } else {
                cOUIHintAnimationLayout2.resumeHintsAnimation();
            }
        }
        if (!isInLayout()) {
            requestLayout();
            return;
        }
        configResponsive();
        measureInsideBackground(measureBackground(measureOutsideBackground()));
        layoutBackgroundLeft();
        layoutBackgroundArea();
        layoutBackgroundRight();
    }

    public void setSearchViewIcon(Drawable drawable) {
        configImageViewDrawable(this.mSearchIconView, drawable, this.mInnerIconSize);
    }

    public void setUseResponsivePadding(boolean useResponsivePadding) {
        this.mUseResponsivePadding = useResponsivePadding;
        requestLayout();
    }

    public void showInToolBar() {
        if (this.mToolBar == null || this.mToolBarAnimationRunning) {
            return;
        }
        this.mToolBarAnimationRunning = true;
        ensureAddedToToolBar();
        if (this.mAddToToolbarWay == WAY_AT_BEHIND) {
            setVisibility(0);
            setAlpha(0.0f);
            changeStateImmediately(STATE_EDIT);
            animate().alpha(1.0f).setDuration(DEFAULT_FADE_DURATION).setListener(null).start();
        }
        setToolBarChildVisibility(8);
        ValueAnimator valueAnimatorOfFloat = ValueAnimator.ofFloat(1.0f, 0.0f);
        valueAnimatorOfFloat.setDuration(DEFAULT_FADE_DURATION);
        valueAnimatorOfFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                COUISearchBar.this.lambda$showInToolBar$2(valueAnimator);
            }
        });
        valueAnimatorOfFloat.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animator) {
                super.onAnimationEnd(animator);
                COUISearchBar.this.mToolBarAnimationRunning = false;
            }

            @Override
            public void onAnimationStart(Animator animator) {
                super.onAnimationStart(animator);
                if (COUISearchBar.this.mInputMethodAnimationEnabled) {
                    COUISearchBar.this.openSoftInput(true);
                }
            }
        });
        valueAnimatorOfFloat.start();
    }

    public COUISearchBar(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, R.attr.couiSearchBarStyle);
    }

    public COUISearchBar(Context context, AttributeSet attributeSet, int index) {
        this(context, attributeSet, index, COUIContextUtil.isCOUIDarkTheme(context) ? R.style.Widget_COUI_COUISearchBar_Dark : R.style.Widget_COUI_COUISearchBar);
    }

    public COUISearchBar(Context context, AttributeSet attributeSet, int index, int count) {
        super(context, attributeSet, index, count);
        this.mDrawingProxyDrawable = new COUISearchBarDrawingProxyDrawable();
        this.mBackgroundRect = new Rect();
        this.mDividerRect = new Rect();
        this.mBackgroundScaleFraction = 1.0f;
        this.mResponsiveWidthSize = RESPONSIVE_WIDTH_TYPE_COMPAT;
        this.mAddToToolbarWay = WAY_NONE;
        this.mGravityInToolBar = 48;
        this.mPressed = false;
        this.mUseResponsivePadding = true;
        this.mButtonOffsetAnimationDistance = 0;
        this.mStyle = 0;
        this.mClearTextDrawableResourceId = -1;
        this.mCollapsingHeightPercent = 1.0f;
        this.mShrinkFraction = 0.0f;
        this.mAttrs = null;
        this.mInputMethodAnimationEnabled = true;
        this.mShowImeAnimDuration = 0;
        this.mShowImeInterpolator = null;
        this.mState = new AtomicInteger(0);
        this.mSearchViewType = 0;
        this.mHideFunctionalButton = false;
        init(context, attributeSet, index, count);
    }
}
