package com.coui.appcompat.panel;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.ComponentCallbacks;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowInsets;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.window.OnBackInvokedCallback;
import android.window.OnBackInvokedDispatcher;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.FloatValueHolder;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;
import com.coui.appcompat.R;
import com.coui.appcompat.animation.COUIEaseInterpolator;
import com.coui.appcompat.animation.COUIInEaseInterpolator;
import com.coui.appcompat.animation.COUIOutEaseInterpolator;
import com.coui.appcompat.animation.dynamicanimation.COUIDynamicAnimation;
import com.coui.appcompat.animation.dynamicanimation.COUISpringAnimation;
import com.coui.appcompat.animation.dynamicanimation.COUISpringForce;
import com.coui.appcompat.contextutil.COUIContextUtil;
import com.coui.appcompat.darkmode.COUIDarkModeUtil;
import com.coui.appcompat.edittext.COUIInputView;
import com.coui.appcompat.grid.COUIResponsiveUtils;
import com.coui.appcompat.log.COUILog;
import com.coui.appcompat.panel.COUIBottomSheetBehavior;
import com.coui.appcompat.roundcorner.RoundCornerUtil;
import com.coui.appcompat.theme.COUIThemeOverlay;
import com.coui.appcompat.uiutil.ShadowUtils;
import com.coui.appcompat.uiutil.UIUtil;
import com.coui.appcompat.version.COUIVersionUtil;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringListener;
import com.facebook.rebound.SpringSystem;
import com.oplus.dynamicframerate.AnimationVelocityCalculator;
import com.oplus.dynamicframerate.DynamicFrameRateManager;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import okio.Segment;


public class COUIBottomSheetDialog extends BottomSheetDialog {
    private static final float ALPHA_OPAQUE = 1.0f;
    private static final float ALPHA_TRANSPARENT = 0.0f;
    private static final int ANIMATION_TYPE_DIALOG_ALPHA = 8;
    public static final int ANIMATION_TYPE_ID = 10101;
    private static final int ANIMATION_TYPE_OUTSIDE_ALPHA = 2;
    private static final int ANIMATION_TYPE_SCALE = 4;
    private static final int ANIMATION_TYPE_TRANSLATION = 1;
    private static final boolean DEBUG;
    private static final float DEFAULT_ALPHA_HIDE_SPRING_RESPONSE = 0.25f;
    private static final float DEFAULT_ALPHA_HIDING_ANIMATOR_DURATION = 183.0f;
    private static final float DEFAULT_ALPHA_SHOW_SPRING_RESPONSE = 0.25f;
    private static final float DEFAULT_CENTER_HIDE_SPRING_RESPONSE = 0.25f;
    private static final float DEFAULT_CENTER_SHOW_SPRING_RESPONSE = 0.25f;
    private static final float DEFAULT_MINIMUM_VISIBLE_CHANGE_DISMISS = 10.0f;
    private static final float DEFAULT_MINIMUM_VISIBLE_CHANGE_SHOW = 1.0f;
    private static final int DEFAULT_MOVE_PX = 1;
    private static final float DEFAULT_SPRING_DAMPING_RATIO = 0.7f;
    private static final int DEFAULT_SPRING_FACTOR = 10000;
    private static final float DEFAULT_SPRING_STIFFNESS = 200.0f;
    private static final float DEFAULT_TRANSLATE_HIDING_ANIMATOR_DURATION = 333.0f;
    static final float DEFAULT_TRANSLATION_HIDE_SPRING_RESPONSE_LARGE = 0.37f;
    static final float DEFAULT_TRANSLATION_HIDE_SPRING_RESPONSE_SMALL = 0.18f;
    private static final float DEFAULT_TRANSLATION_SHOW_SPRING_RESPONSE_LARGE = 0.45f;
    private static final float DEFAULT_TRANSLATION_SHOW_SPRING_RESPONSE_SMALL = 0.25f;
    private static final float DEFAULT_TRANSLATION_SPRING_BOUNCE = 0.0f;
    private static final float DIALOG_SHOW_SCALE_DELTA = 0.2f;
    private static final float DIALOG_SHOW_SCALE_START = 0.8f;
    private static final Interpolator DISMISS_ALPHA_ANIM_INTERPOLATOR;
    private static final float ELEVATION_VALUE = 24.0f;
    private static final int FINAL_POSITION = 100;
    private static final float FIRST_TIER_ALPHA = 0.75f;
    private static final float FLOAT_ONE = 1.0f;
    private static final float FLOAT_POINT_FIVE = 0.5f;
    private static final int HUNDRED = 100;
    private static final int INT_TWO = 2;
    private static final float MAX_ALPHA = 255.0f;
    private static final long NAV_COLOR_ANIM_DURATION = 200;
    private static final float NO_ELEVATION_VALUE = 0.0f;
    private static final Interpolator OUTSIDE_ALPHA_ANIM_INTERPOLATOR;
    private static final float PHYSICS_UNSET = Float.MIN_VALUE;
    private static final float PULL_UP_FRICTION = 0.8f;
    private static final int PULL_UP_REBOUND_BOUNCINESS = 6;
    private static final int PULL_UP_REBOUND_SPEED = 42;
    private static final int SDK_SUB_VERSION_FOR_COMPUTE = 10;
    private static final int SDK_SUB_VERSION_FOR_FRAME_RATE = 10;
    private static final int SDK_VERSION_FOR_COMPUTE = 34;
    private static final float SECOND_TIER_ALPHA = 0.5f;
    private static final float SHOW_HEIGHT_ANIM_DURATION_IN_TINY_SCREEN = 167.0f;
    private static final Interpolator SHOW_HEIGHT_ANIM_INTERPOLATOR;
    private static final float SPRING_ANIM_CONTENT_CHANGE_BOUNCE = 0.0f;
    private static final float SPRING_ANIM_CONTENT_CHANGE_MINIMUM_VISIBLE_CHANGE = 95.0f;
    private static final float SPRING_ANIM_CONTENT_CHANGE_RESPONSE = 0.4f;
    private static final String STATE_FOCUS_CHANGES = "state_focus_changes";
    private static final String STATE_LAST_STATIC_CHANGES = "last_static_state";
    private static final String TAG = "COUIBottomSheetDialog";
    private static final float THIRD_TIER_ALPHA = 0.25f;
    private static final double THREE_POINT_EIGHT = 3.8d;
    private static final double TWENTY = 20.0d;
    private static final int UNSET_SIZE = -1;
    private static final double ZERO = 0.0d;
    protected boolean isLargeScreenLimitMaxSize;
    private int mADFRFeatureType;
    private WeakReference<Activity> mActivityWeakReference;
    private ViewGroup mAdjustLayout;
    private boolean mAdjustResizeEnable;
    private COUIPanelAdjustResizeHelper mAdjustResizeHelper;
    private COUISpringAnimation mAlphaSpringAnimation;
    private COUIDynamicAnimation.OnAnimationEndListener mAlphaSpringEndListener;
    private COUIDynamicAnimation.OnAnimationUpdateListener mAlphaSpringUpdateListener;
    private View mAnchorView;
    private int mAnimationFlag;
    private OnAnimationListener mAnimationListener;
    private float mAppearDampingRatio;
    private SpringAnimation mAppearSpringAnim;
    private SpringForce mAppearSpringForce;
    private float mAppearStiffness;
    private WindowInsets mApplyWindowInsets;
    private BottomSheetDialogAnimatorListener mBottomSheetDialogAnimatorListener;
    private boolean mCanPerformHapticFeedback;
    private boolean mCanPullUp;
    private boolean mCancelable;
    private boolean mCanceledOnTouchOutside;
    private int mColorMask;
    private ComponentCallbacks mComponentCallbacks;
    private Configuration mConfiguration;
    private IgnoreWindowInsetsFrameLayout mContainerFrameLayout;
    private View mContentView;
    private View mCoordinatorLayout;
    protected int mCoordinatorLayoutMinInsetsTop;
    private int mCoordinatorLayoutPaddingExtra;
    private boolean mCouiPanelEdgeToEdgeEnable;
    private float mCurrentOutSideAlphaStateHidden;
    private float mCurrentOutSideAlphaStateShow;
    private float mCurrentOutsideAlpha;
    private float mCurrentParentViewTranslationY;
    private int mCurrentSpringTotalOffset;
    private int mDefaultPaddingBottom;
    private COUIPanelPercentFrameLayout mDesignBottomSheetFrameLayout;
    private DialogOffsetListener mDialogOffsetListener;
    private Spring mDisableFastCloseFeedbackSpring;
    private boolean mDisableSubExpand;
    protected COUIPanelContentLayout mDraggableConstraintLayout;
    private float mEndValueOfTranslateAnimation;
    private View mFeedBackView;

    @Deprecated
    private int mFinalNavColorAfterDismiss;
    private boolean mFirstShowCollapsed;
    private Boolean mFocusChange;
    private boolean mFrameRate;
    private boolean mGlobalDrag;
    private GradientDrawable mGradientDrawable;
    private boolean mHandleViewHasPressAnim;
    private int mHideDragViewHeight;
    private InputMethodManager mInputMethodManager;
    private boolean mIsAnimationInFirst;
    private boolean mIsAppearSpringAnimStared;
    private boolean mIsDraggable;
    private boolean mIsEntering;

    @Deprecated
    private boolean mIsExecuteNavColorAnimAfterDismiss;
    private boolean mIsExecutingDismissAnim;
    private boolean mIsFullScreenInTinyScreen;
    private boolean mIsGestureNavigation;
    private boolean mIsHandlePanel;
    private boolean mIsInTinyScreen;
    private boolean mIsInWindowFloatingMode;
    private boolean mIsInterruptingAnim;
    private boolean mIsNeedOutsideViewAnim;
    private boolean mIsNeedShowKeyboard;
    private boolean mIsRevertAnimationFromSettlingAnimation;
    private boolean mIsShowInDialogFragment;
    private boolean mIsShowInMaxHeight;
    private boolean mIsVSdk;
    private int mLastStaticState;
    private int mNavColor;
    private View mNavigationCustomView;
    private com.oplus.wrapper.view.ViewTreeObserver.OnComputeInternalInsetsListener mOSDKComputeListener;
    private com.oplus.wrapper.view.ViewTreeObserver mOSDKViewTreeObserver;
    private View.OnAttachStateChangeListener mOnAttatchStateChangeListener;
    private OnBackInvokedCallback mOnBackInvokedCallback;
    private OnBackInvokedLocalListener mOnBackInvokedLocalListener;
    private ViewTreeObserver.OnPreDrawListener mOnPreDrawListener;
    private int mOriginWidth;
    private View.OnTouchListener mOutSideViewTouchListener;
    private View mOutsideView;
    private float mOutsideViewBackgroundAlpha;
    private Drawable mPanelBackground;
    private int mPanelBackgroundTintColor;
    private COUIPanelBarView mPanelBarView;
    private Drawable mPanelDragViewDrawable;
    private int mPanelDragViewDrawableTintColor;
    private int mPanelHeight;
    private int mPanelPaddingBottom;
    private COUIPanelPullUpListener mPanelPullUpListener;
    private float mPanelRatio;
    private Spring mPanelSpringBackAnim;
    private AnimatorSet mPanelViewTranslationAnimationSet;
    private int mPanelWidth;
    private int mParentViewPaddingBottom;
    private int mPeekHeight;
    private float mPhysicsDampingRatio;
    private float mPhysicsFrequency;
    private int mPreferWidth;
    private WindowInsets mProgressWindowInsets;
    private int mPullUpMaxOffset;
    private COUIBottomSheetBehavior.PullUpToDismissPanelListener mPullUpToDismissPanelListener;
    private View mPulledUpView;
    private boolean mRegisterConfigurationChangeCallBack;
    private boolean mShouldRegisterWindowInsetsListener;
    private boolean mSkipCollapsed;
    private int mSnapStartBottom;
    private COUISpringForce mSpringForceAlpha;
    private COUISpringForce mSpringForceTranslationAndScale;
    private float mStartValueOfTranslateAnimation;
    private int mStatusBarHeight;
    private boolean mSupportExitBlockingAnimation;
    private final Rect mTemtRect;
    private float mTranslateHidingDuration;
    private COUIDynamicAnimation.OnAnimationEndListener mTranslationAndScaleEndListener;
    private COUISpringAnimation mTranslationAndScaleSpringAnimation;
    private COUIDynamicAnimation.OnAnimationUpdateListener mTranslationAndScaleUpdateListener;
    private boolean mUseNormalSmoothCorner;
    private boolean mWindowInsetsAnimEnable;
    private int mWindowInsetsLeft;
    private View.OnApplyWindowInsetsListener mWindowInsetsListener;
    private int mWindowInsetsTop;

    @Retention(RetentionPolicy.SOURCE)
    public @interface AnimationType {
    }

    public interface BottomSheetDialogAnimatorListener {
        void onBottomSheetDialogCollapsed();

        void onBottomSheetDialogExpanded();
    }

    public interface DialogOffsetListener {
        void onDialogOffsetChanged(float offset);
    }

    public interface OnAnimationListener {
        default void onDismissAnimationEnd() {
        }

        default void onDismissAnimationStart() {
        }

        default void onShowAnimationEnd() {
        }

        default void onShowAnimationStart() {
        }
    }

    public interface OnBackInvokedLocalListener {
        void onBackInvokedLocal();
    }

    static {
        COUIInEaseInterpolator cOUIInEaseInterpolator = new COUIInEaseInterpolator();
        SHOW_HEIGHT_ANIM_INTERPOLATOR = cOUIInEaseInterpolator;
        OUTSIDE_ALPHA_ANIM_INTERPOLATOR = new COUIEaseInterpolator();
        DISMISS_ALPHA_ANIM_INTERPOLATOR = cOUIInEaseInterpolator;
        DEBUG = Log.isLoggable(TAG, 3);
    }

    public COUIBottomSheetDialog(Context context) {
        this(context, 0);
    }

    private void addAnimationFlag(int flag) {
        this.mAnimationFlag = flag | this.mAnimationFlag;
    }


    public void addOSDKViewTreeObserver() {
        if (this.mOSDKViewTreeObserver == null) {
            com.oplus.wrapper.view.ViewTreeObserver viewTreeObserver = new com.oplus.wrapper.view.ViewTreeObserver(this.mOutsideView.getViewTreeObserver());
            this.mOSDKViewTreeObserver = viewTreeObserver;
            viewTreeObserver.addOnComputeInternalInsetsListener(this.mOSDKComputeListener);
        }
    }


    public void adjustResize(WindowInsets windowInsets, boolean enable) {
        if (enable) {
            boolean useConstraintLayout = getContext().getResources().getBoolean(com.coui.appcompat.R.bool.is_coui_bottom_sheet_ime_adjust_in_constraint_layout);
            ViewGroup viewGroup = (ViewGroup) findViewById(com.coui.appcompat.R.id.design_bottom_sheet);
            ViewGroup viewGroup2 = (ViewGroup) findViewById(com.coui.appcompat.R.id.coui_panel_content_layout);
            if (useConstraintLayout) {
                viewGroup = viewGroup2;
            }
            ViewGroup viewGroup3 = this.mAdjustLayout;
            if (viewGroup3 != (useConstraintLayout ? this.mDraggableConstraintLayout : this.mDesignBottomSheetFrameLayout)) {
                COUIViewMarginUtil.setMargin(viewGroup3, 3, 0);
            }
            ViewGroup viewGroup4 = useConstraintLayout ? this.mDraggableConstraintLayout : this.mDesignBottomSheetFrameLayout;
            this.mAdjustLayout = viewGroup4;
            if (viewGroup4 != null) {
                viewGroup = viewGroup4;
            }
            adjustResizeInternal(windowInsets, viewGroup);
        }
    }


    public void adjustResizeInternal(final WindowInsets windowInsets, final ViewGroup viewGroup) {
        if (viewGroup == null || !viewGroup.isLayoutRequested()) {
            getAdjustResizeHelper().adjustResize(getContext(), viewGroup, windowInsets, this.mCoordinatorLayout, getFocusChange());
        } else {
            viewGroup.post(new Runnable() {
                @Override
                public void run() {
                    COUIBottomSheetDialog.this.adjustResizeInternal(windowInsets, viewGroup);
                }
            });
        }
    }


    public void animationEnd() {
        if (this.mDesignBottomSheetFrameLayout != null) {
            if (!isFollowHand() && !isFadeInCenter()) {
                this.mDesignBottomSheetFrameLayout.setTranslationY(this.mCurrentParentViewTranslationY);
            }
            if (getBehavior() != null && getBehavior().getState() == 3 && this.mCanPerformHapticFeedback) {
                this.mDesignBottomSheetFrameLayout.performHapticFeedback(14);
            }
        }
        OnAnimationListener onAnimationListener = this.mAnimationListener;
        if (onAnimationListener != null) {
            onAnimationListener.onShowAnimationEnd();
        }
        if (isFollowHand()) {
            haveEnoughSpace();
        }
    }


    public void animationStart() {
        if (getBehavior() != null && getBehavior().getState() == 5) {
            ((COUIBottomSheetBehavior) getBehavior()).setPanelState(this.mLastStaticState);
        }
        OnAnimationListener onAnimationListener = this.mAnimationListener;
        if (onAnimationListener != null) {
            onAnimationListener.onShowAnimationStart();
        }
    }

    private int[] calculateFinalLocationOnScreen(View anchorView) {
        ViewGroup rootView = (ViewGroup) anchorView.getRootView();
        View rootContentView = rootView.getChildAt(0);
        Rect anchorRect = getLocationRectInScreen(anchorView);
        Rect displayRect = new Rect(
                0,
                0,
                getContext().getResources().getDisplayMetrics().widthPixels,
                getContext().getResources().getDisplayMetrics().heightPixels
        );
        Rect dialogRect = getLocationRectInScreen(this.mDesignBottomSheetFrameLayout);
        WindowInsetsCompat rootInsets = ViewCompat.getRootWindowInsets(rootContentView);
        if (rootInsets != null) {
            Insets systemBarInsets = rootInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            this.mWindowInsetsTop = systemBarInsets.top;
            this.mWindowInsetsLeft = systemBarInsets.left;
        }

        int dialogWidth = this.mDesignBottomSheetFrameLayout.getMeasuredWidth();
        int dialogHeight = this.mDesignBottomSheetFrameLayout.getMeasuredHeight();
        int bottomMargin = getContext().getResources().getDimensionPixelOffset(com.coui.appcompat.R.dimen.coui_bottom_sheet_dialog_follow_hand_margin_bottom);
        int sideMargin = getContext().getResources().getDimensionPixelOffset(com.coui.appcompat.R.dimen.coui_bottom_sheet_dialog_follow_hand_margin_left);
        int maxX = displayRect.right - dialogWidth;
        int finalX = normalizePoints((((anchorRect.left + anchorRect.right) / 2) - (dialogWidth / 2)) - this.mWindowInsetsLeft, maxX);
        if (finalX <= sideMargin) {
            finalX = sideMargin;
        } else if (finalX + dialogWidth + sideMargin >= displayRect.right) {
            finalX = (displayRect.right - sideMargin) - dialogWidth;
        }

        int maxY = displayRect.bottom - dialogHeight;
        int rightSpace = displayRect.right - anchorRect.right;
        int leftSpace = anchorRect.left - displayRect.left;
        int topSpace = ((anchorRect.top - displayRect.top) - this.mCoordinatorLayoutMinInsetsTop) - bottomMargin;
        int bottomSpace = displayRect.bottom - anchorRect.bottom;
        int finalY;
        if (dialogHeight < topSpace) {
            finalY = normalizePoints(((((anchorRect.top - dialogHeight) - this.mCoordinatorLayoutMinInsetsTop) + this.mStatusBarHeight) - bottomMargin) - this.mWindowInsetsTop, maxY);
        } else if (dialogHeight < bottomSpace) {
            finalY = normalizePoints((anchorRect.bottom - this.mCoordinatorLayoutMinInsetsTop) + bottomMargin, maxY);
        } else {
            finalY = normalizePoints((((anchorRect.bottom + anchorRect.top) / 2) - (dialogHeight / 2)) - this.mWindowInsetsTop, maxY);
            if (dialogWidth < leftSpace) {
                finalX = (anchorRect.left - dialogWidth) - sideMargin;
            } else if (dialogWidth < rightSpace) {
                finalX = anchorRect.right + sideMargin;
            }
        }
        if (DEBUG) {
            Log.d(TAG, "calculateFinalLocationInScreen: \n anchorViewLocationRect = " + anchorRect + ", \n anchorContentViewLocationRect = " + displayRect + ", \n dialogViewLocalRect = " + dialogRect + "\n -> final : x = " + finalX + ", y = " + finalY + "\n -> insetTop: " + this.mWindowInsetsTop + " maxY: " + maxY);
        }
        return new int[]{finalX, finalY};
    }

    private void cancelAnim(Animator animator) {
        if (animator == null || !animator.isRunning()) {
            return;
        }
        animator.end();
    }

    private void checkInitState() {
        if (this.mContainerFrameLayout == null) {
            throw new IllegalArgumentException("container can not be null");
        }
        if (this.mCoordinatorLayout == null) {
            throw new IllegalArgumentException("coordinator can not be null");
        }
        if (this.mOutsideView == null) {
            throw new IllegalArgumentException("panel_outside can not be null");
        }
        if (this.mDesignBottomSheetFrameLayout == null) {
            throw new IllegalArgumentException("design_bottom_sheet can not be null");
        }
    }

    public static COUISpringAnimation createContentChangeSpringAnimation() {
        FloatValueHolder dVar = new FloatValueHolder(0.0f);
        COUISpringForce cOUISpringForce = new COUISpringForce();
        cOUISpringForce.setBounce(DEFAULT_TRANSLATION_SPRING_BOUNCE);
        cOUISpringForce.setResponse(SPRING_ANIM_CONTENT_CHANGE_RESPONSE);
        COUISpringAnimation spring = new COUISpringAnimation(dVar).setSpring(cOUISpringForce);
        spring.setMinimumVisibleChange(SPRING_ANIM_CONTENT_CHANGE_MINIMUM_VISIBLE_CHANGE);
        return spring;
    }

    private ValueAnimator createNavigationColorAnimation(int color) {
        if (COUINavigationBarUtil.isNavigationBarShow(getContext()) && getWindow() != null) {
            final Window window = getWindow();
            int navigationBarColor = window.getNavigationBarColor();
            if (Color.alpha(color) == 0) {
                color = Color.argb(1, Color.red(color), Color.green(color), Color.blue(color));
            }
            if (navigationBarColor != color) {
                ValueAnimator valueAnimatorOfObject = ValueAnimator.ofObject(new ArgbEvaluator(), Integer.valueOf(navigationBarColor), Integer.valueOf(color));
                valueAnimatorOfObject.setDuration(NAV_COLOR_ANIM_DURATION);
                valueAnimatorOfObject.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        window.setNavigationBarColor(((Integer) valueAnimator.getAnimatedValue()).intValue());
                    }
                });
                return valueAnimatorOfObject;
            }
        }
        return null;
    }

    private ValueAnimator createOutsideAlphaAnimation(final boolean showing, float duration, PathInterpolator pathInterpolator) {
        final float startAlpha = this.mCurrentOutsideAlpha;
        final float endAlpha = showing ? 1.0f : 0.0f;
        if (startAlpha == endAlpha) {
            COUILog.w(TAG, "StartAlphaValue == endAlphaValue, No need to perform transparency animation anymore");
            return null;
        }
        ValueAnimator valueAnimatorOfFloat = ValueAnimator.ofFloat(startAlpha, endAlpha);
        valueAnimatorOfFloat.setDuration((long) duration);
        valueAnimatorOfFloat.setInterpolator(pathInterpolator);
        valueAnimatorOfFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float fFloatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                float fromAlpha = startAlpha;
                float toAlpha = endAlpha;
                COUIBottomSheetDialog.this.outsideAlphaChange(fromAlpha != toAlpha ? (fFloatValue - fromAlpha) / (toAlpha - fromAlpha) : 0.0f, showing);
            }
        });
        valueAnimatorOfFloat.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animator) {
                super.onAnimationEnd(animator);
                if (COUIBottomSheetDialog.this.mDesignBottomSheetFrameLayout != null && COUIBottomSheetDialog.this.mDesignBottomSheetFrameLayout.getAlpha() == 0.0f) {
                    COUIBottomSheetDialog.this.mDesignBottomSheetFrameLayout.setAlpha(ALPHA_OPAQUE);
                }
                COUIBottomSheetDialog.this.mIsNeedShowKeyboard = false;
            }
        });
        return valueAnimatorOfFloat;
    }

    private void createPanelConstraintLayout() {
        COUIPanelContentLayout cOUIPanelContentLayout = (COUIPanelContentLayout) getLayoutInflater().inflate(this.mIsInTinyScreen ? com.coui.appcompat.R.layout.coui_panel_view_layout_tiny : com.coui.appcompat.R.layout.coui_panel_view_layout, (ViewGroup) null);
        Drawable drawable = this.mPanelDragViewDrawable;
        if (drawable != null) {
            drawable.setTint(this.mPanelDragViewDrawableTintColor);
            cOUIPanelContentLayout.setDragViewDrawable(this.mPanelDragViewDrawable);
        }
        if (this.mHandleViewHasPressAnim) {
            cOUIPanelContentLayout.setDragViewPressAnim(true);
        }
        WindowInsets windowInsets = this.mApplyWindowInsets;
        COUIPanelPercentFrameLayout cOUIPanelPercentFrameLayout = this.mDesignBottomSheetFrameLayout;
        cOUIPanelContentLayout.setNavigationMargin(null, windowInsets, cOUIPanelPercentFrameLayout != null && cOUIPanelPercentFrameLayout.getRatio() == 1.0f, this.mCouiPanelEdgeToEdgeEnable);
        this.mDraggableConstraintLayout = cOUIPanelContentLayout;
        if (this.mIsHandlePanel) {
            return;
        }
        hideDragView();
    }

    private ValueAnimator createPanelTranslateAnimation(float startY, float endY, float duration, PathInterpolator pathInterpolator) {
        ValueAnimator valueAnimatorOfFloat = ValueAnimator.ofFloat(startY, endY);
        valueAnimatorOfFloat.setDuration((long) duration);
        valueAnimatorOfFloat.setInterpolator(pathInterpolator);
        valueAnimatorOfFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                COUIBottomSheetDialog.this.translateUpdate(((Float) valueAnimator.getAnimatedValue()).floatValue());
            }
        });
        setFrameRate(valueAnimatorOfFloat);
        return valueAnimatorOfFloat;
    }


    public void dismissWithAlphaAnim() {
        this.mIsEntering = false;
        this.mIsRevertAnimationFromSettlingAnimation = false;
        AnimatorListenerAdapter animatorListenerAdapter = new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animator) {
                COUIBottomSheetDialog.this.mCurrentOutSideAlphaStateHidden = 0.0f;
                super.onAnimationCancel(animator);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (COUIBottomSheetDialog.this.mBottomSheetDialogAnimatorListener != null) {
                    COUIBottomSheetDialog.this.mBottomSheetDialogAnimatorListener.onBottomSheetDialogCollapsed();
                }
                COUIBottomSheetDialog.this.mCurrentOutSideAlphaStateHidden = 0.0f;
            }

            @Override
            public void onAnimationStart(Animator animator) {
                COUIBottomSheetDialog.this.mIsExecutingDismissAnim = true;
                if (COUIBottomSheetDialog.this.mAnimationListener != null) {
                    COUIBottomSheetDialog.this.mAnimationListener.onDismissAnimationStart();
                }
                super.onAnimationStart(animator);
            }
        };
        stopCurrentRunningViewTranslationAnim();
        this.mCurrentOutSideAlphaStateHidden = this.mCurrentOutsideAlpha;
        resetAnimationFlag();
        addAnimationFlag(2);
        doAlphaSpringAnimaion(animatorListenerAdapter);
    }

    private void dismissWithInterruptibleAnim() {
        doParentViewTranslationHidingAnim(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animator) {
                super.onAnimationCancel(animator);
                COUIBottomSheetDialog.this.mIsExecutingDismissAnim = false;
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                super.onAnimationEnd(animator);
                if (COUIBottomSheetDialog.this.mBottomSheetDialogAnimatorListener != null) {
                    COUIBottomSheetDialog.this.mBottomSheetDialogAnimatorListener.onBottomSheetDialogCollapsed();
                }
                COUIBottomSheetDialog.this.mIsExecutingDismissAnim = false;
                COUIBottomSheetDialog.this.superDismiss();
            }

            @Override
            public void onAnimationStart(Animator animator) {
                super.onAnimationStart(animator);
                COUIBottomSheetDialog.this.mIsExecutingDismissAnim = true;
                if (COUIBottomSheetDialog.this.mAnimationListener != null) {
                    COUIBottomSheetDialog.this.mAnimationListener.onDismissAnimationStart();
                }
            }
        });
    }

    private void doAlphaSpringAnimaion(final Animator.AnimatorListener animatorListener) {
        if (this.mAlphaSpringAnimation == null) {
            this.mAlphaSpringAnimation = new COUISpringAnimation(new FloatValueHolder());
            COUISpringForce cOUISpringForce = new COUISpringForce();
            this.mSpringForceAlpha = cOUISpringForce;
            cOUISpringForce.setBounce(DEFAULT_TRANSLATION_SPRING_BOUNCE);
            this.mAlphaSpringAnimation.setSpring(this.mSpringForceAlpha);
        }
        if (hasAnimationFlag(ANIMATION_TYPE_OUTSIDE_ALPHA)) {
            if (!isFadeInCenter()) {
                this.mSpringForceAlpha.setResponse(getTranslationResponse());
            } else if (this.mIsEntering) {
                this.mSpringForceAlpha.setResponse(DEFAULT_ALPHA_SHOW_SPRING_RESPONSE);
            } else {
                this.mSpringForceAlpha.setResponse(DEFAULT_ALPHA_HIDE_SPRING_RESPONSE);
            }
        }
        if (animatorListener != null) {
            // Leapy modified 2026-07-24: BEGIN match OPPO's single alpha-spring completion listener ownership.
            this.mAlphaSpringAnimation.removeEndListener(this.mAlphaSpringEndListener);
            COUIDynamicAnimation.OnAnimationEndListener onAnimationEndListener = new COUIDynamicAnimation.OnAnimationEndListener() {
                @Override
                public void onAnimationEnd(COUIDynamicAnimation cOUIDynamicAnimation, boolean canceled, float value, float velocity) {
                    if (canceled) {
                        animatorListener.onAnimationCancel(null);
                    } else {
                        animatorListener.onAnimationEnd(null);
                    }
                    COUIBottomSheetDialog.this.mAlphaSpringAnimation.removeEndListener(COUIBottomSheetDialog.this.mAlphaSpringEndListener);
                    COUIBottomSheetDialog.this.mAlphaSpringAnimation.removeUpdateListener(COUIBottomSheetDialog.this.mAlphaSpringUpdateListener);
                }
            };
            this.mAlphaSpringEndListener = onAnimationEndListener;
            this.mAlphaSpringAnimation.addEndListener(onAnimationEndListener);
            animatorListener.onAnimationStart(null);
            // Leapy end 2026-07-24: preserve the decoded callback and release it only after physical spring completion.
        }
        this.mAlphaSpringAnimation.removeUpdateListener(this.mAlphaSpringUpdateListener);
        this.mAlphaSpringUpdateListener = new COUIDynamicAnimation.OnAnimationUpdateListener() {
            @Override
            public void onAnimationUpdate(COUIDynamicAnimation cOUIDynamicAnimation, float value, float velocity) {
                float fMax = COUIBottomSheetDialog.this.mEndValueOfTranslateAnimation != COUIBottomSheetDialog.this.mStartValueOfTranslateAnimation ? (value - COUIBottomSheetDialog.this.mStartValueOfTranslateAnimation) / (COUIBottomSheetDialog.this.mEndValueOfTranslateAnimation - COUIBottomSheetDialog.this.mStartValueOfTranslateAnimation) : 0.0f;
                if (COUIBottomSheetDialog.this.hasAnimationFlag(ANIMATION_TYPE_OUTSIDE_ALPHA)) {
                    COUIBottomSheetDialog cOUIBottomSheetDialog = COUIBottomSheetDialog.this;
                    cOUIBottomSheetDialog.outsideAlphaChange(fMax, cOUIBottomSheetDialog.mIsEntering);
                }
                if (!COUIBottomSheetDialog.this.hasAnimationFlag(ANIMATION_TYPE_DIALOG_ALPHA) || COUIBottomSheetDialog.this.mDesignBottomSheetFrameLayout == null) {
                    return;
                }
                COUIPanelPercentFrameLayout cOUIPanelPercentFrameLayout = COUIBottomSheetDialog.this.mDesignBottomSheetFrameLayout;
                if (!COUIBottomSheetDialog.this.mIsEntering) {
                    fMax = Math.max(0.0f, 1.0f - fMax);
                }
                cOUIPanelPercentFrameLayout.setAlpha(fMax);
            }
        };
        // Leapy removed 2026-07-24: BEGIN remove the migrated cleanup listener that replaced OPPO's lifecycle listener.
        // The decoded implementation proceeds directly to the outside-alpha state setup here.
        // Leapy end 2026-07-24: keep the caller completion listener registered until the spring ends.
        if (this.mIsRevertAnimationFromSettlingAnimation) {
            this.mCurrentOutSideAlphaStateShow = this.mOutsideView.getAlpha();
        } else {
            this.mCurrentOutSideAlphaStateShow = 0.0f;
        }
        this.mAlphaSpringAnimation.addUpdateListener(this.mAlphaSpringUpdateListener);
        this.mAlphaSpringAnimation.setStartValue(this.mStartValueOfTranslateAnimation);
        this.mAlphaSpringAnimation.animateToFinalPosition(this.mEndValueOfTranslateAnimation);
    }

    private void doFeedbackAnimation(View view) {
        if (view == null) {
            return;
        }
        if (this.mDisableFastCloseFeedbackSpring == null || this.mFeedBackView != view) {
            this.mFeedBackView = view;
            Spring spring = SpringSystem.create().createSpring();
            this.mDisableFastCloseFeedbackSpring = spring;
            spring.setSpringConfig(SpringConfig.fromBouncinessAndSpeed(THREE_POINT_EIGHT, TWENTY));
            this.mDisableFastCloseFeedbackSpring.addListener(new SpringListener() {
                @Override
                public void onSpringActivate(Spring spring) {
                }

                @Override
                public void onSpringAtRest(Spring spring) {
                }

                @Override
                public void onSpringEndStateChange(Spring spring) {
                }

                @Override
                public void onSpringUpdate(Spring spring) {
                    if (COUIBottomSheetDialog.this.mDisableFastCloseFeedbackSpring == null || COUIBottomSheetDialog.this.mFeedBackView == null) {
                        return;
                    }
                    int currentValue = (int) spring.getCurrentValue();
                    if (currentValue >= 100) {
                        COUIBottomSheetDialog.this.mDisableFastCloseFeedbackSpring.setEndValue(0.0d);
                    }
                    COUIBottomSheetDialog.this.mFeedBackView.setTranslationY(currentValue);
                }
            });
        }
        this.mDisableFastCloseFeedbackSpring.setEndValue(100.0d);
    }

    private void doParentViewTranslationHidingAnim(Animator.AnimatorListener animatorListener) {
        if (reversalAnimation(animatorListener, false)) {
            this.mIsExecutingDismissAnim = true;
            return;
        }
        this.mIsEntering = false;
        stopCurrentRunningViewTranslationAnim();
        resetAnimationFlag();
        if (getDialogMaxHeight() == 0) {
            Log.d(TAG, "doParentViewTranslationHidingAnim return directly for dialogMaxHeight is 0, but call superDismiss");
            superDismiss();
            return;
        }
        this.mPanelViewTranslationAnimationSet = new AnimatorSet();
        if (this.mIsInTinyScreen) {
            startReleaseAnimInTinyScreen(this.mStartValueOfTranslateAnimation, this.mEndValueOfTranslateAnimation, this.mTranslateHidingDuration, animatorListener);
            return;
        }
        if (isFollowHand()) {
            setDefaultSpringStartEndValue();
            if (this.mDesignBottomSheetFrameLayout.getAlpha() != ALPHA_OPAQUE) {
                this.mDesignBottomSheetFrameLayout.setAlpha(ALPHA_OPAQUE);
            }
            if (haveEnoughSpace()) {
                addAnimationFlag(8);
            } else {
                addAnimationFlag(8);
                addAnimationFlag(2);
            }
        } else if (isFadeInCenter()) {
            addAnimationFlag(ANIMATION_TYPE_SCALE);
            addAnimationFlag(2);
            addAnimationFlag(8);
            setDefaultSpringStartEndValue();
        } else {
            addAnimationFlag(ANIMATION_TYPE_TRANSLATION);
            addAnimationFlag(2);
            this.mStartValueOfTranslateAnimation = (int) this.mCurrentParentViewTranslationY;
            this.mEndValueOfTranslateAnimation = getTranslationDistance();
        }
        this.mIsAnimationInFirst = false;
        doTranslationAndScaleSpringAnimaion(animatorListener);
        doAlphaSpringAnimaion(null);
    }


    public void doParentViewTranslationShowingAnim(int extraOffset, Animator.AnimatorListener animatorListener) {
        this.mCurrentOutSideAlphaStateShow = 0.0f;
        if (reversalAnimation(animatorListener, true)) {
            this.mIsExecutingDismissAnim = false;
            return;
        }
        if (((COUIBottomSheetBehavior) getBehavior()).isPanelHeightChangeAnimRunning()) {
            this.mIsRevertAnimationFromSettlingAnimation = true;
            ((COUIBottomSheetBehavior) getBehavior()).stopSettlingAnimationIfRunning();
            this.mIsExecutingDismissAnim = false;
        }
        stopCurrentRunningViewTranslationAnim();
        resetAnimationFlag();
        if (getDialogMaxHeight() == 0) {
            Log.d(TAG, "doParentViewTranslationShowingAnim return directly for dialogMaxHeight is 0");
            return;
        }
        this.mIsEntering = true;
        getContentViewHeightWithMargins();
        this.mPanelViewTranslationAnimationSet = new AnimatorSet();
        if (this.mIsInTinyScreen) {
            startShowingAnimInTinyScreen(extraOffset, animatorListener);
            return;
        }
        if (isFollowHand()) {
            COUIPanelPercentFrameLayout cOUIPanelPercentFrameLayout = this.mDesignBottomSheetFrameLayout;
            if (cOUIPanelPercentFrameLayout != null && cOUIPanelPercentFrameLayout.getAlpha() != 0.0f) {
                this.mDesignBottomSheetFrameLayout.setAlpha(ALPHA_TRANSPARENT);
                this.mDesignBottomSheetFrameLayout.setScaleX(DIALOG_SHOW_SCALE_START);
                this.mDesignBottomSheetFrameLayout.setScaleY(DIALOG_SHOW_SCALE_START);
            }
            setDefaultSpringStartEndValue();
            if (haveEnoughSpace()) {
                offsetViewTo();
                addAnimationFlag(8);
                addAnimationFlag(ANIMATION_TYPE_SCALE);
            } else {
                updateBottomSheetCenterVertical();
                addAnimationFlag(8);
                addAnimationFlag(ANIMATION_TYPE_SCALE);
                addAnimationFlag(2);
            }
        } else if (isFadeInCenter()) {
            COUIPanelPercentFrameLayout cOUIPanelPercentFrameLayout2 = this.mDesignBottomSheetFrameLayout;
            if (cOUIPanelPercentFrameLayout2 != null) {
                cOUIPanelPercentFrameLayout2.setAlpha(ALPHA_TRANSPARENT);
                this.mDesignBottomSheetFrameLayout.setScaleX(DIALOG_SHOW_SCALE_START);
                this.mDesignBottomSheetFrameLayout.setScaleY(DIALOG_SHOW_SCALE_START);
            }
            addAnimationFlag(ANIMATION_TYPE_SCALE);
            addAnimationFlag(2);
            addAnimationFlag(8);
            setDefaultSpringStartEndValue();
        } else {
            COUIPanelPercentFrameLayout cOUIPanelPercentFrameLayout3 = this.mDesignBottomSheetFrameLayout;
            if (cOUIPanelPercentFrameLayout3 != null) {
                cOUIPanelPercentFrameLayout3.setAlpha(1.0f);
                this.mDesignBottomSheetFrameLayout.setScaleX(1.0f);
                this.mDesignBottomSheetFrameLayout.setScaleY(1.0f);
            }
            addAnimationFlag(ANIMATION_TYPE_TRANSLATION);
            addAnimationFlag(2);
            this.mStartValueOfTranslateAnimation = getTranslationDistance();
            this.mEndValueOfTranslateAnimation = 0.0f;
            if (this.mIsRevertAnimationFromSettlingAnimation) {
                this.mStartValueOfTranslateAnimation = this.mDesignBottomSheetFrameLayout.getTop();
            }
        }
        this.mIsAnimationInFirst = true;
        doTranslationAndScaleSpringAnimaion(animatorListener);
        doAlphaSpringAnimaion(null);
    }


    public void doSpringBackReboundAnim(final int endPadding) {
        Spring spring = SpringSystem.create().createSpring();
        this.mPanelSpringBackAnim = spring;
        spring.setSpringConfig(SpringConfig.fromBouncinessAndSpeed(PULL_UP_REBOUND_BOUNCINESS, PULL_UP_REBOUND_SPEED));
        this.mCurrentSpringTotalOffset = 0;
        this.mPanelSpringBackAnim.addListener(new SpringListener() {
            @Override
            public void onSpringActivate(Spring spring) {
            }

            @Override
            public void onSpringAtRest(Spring spring) {
                if ((COUIBottomSheetDialog.this.getBehavior() instanceof COUIBottomSheetBehavior) && COUIBottomSheetDialog.this.mPulledUpView != null) {
                    COUIBottomSheetDialog.this.mParentViewPaddingBottom = 0;
                    COUIBottomSheetDialog.this.setPulledUpViewPaddingBottom(0);
                    ((COUIBottomSheetBehavior) COUIBottomSheetDialog.this.getBehavior()).setStateInternal(3);
                }
                COUIBottomSheetDialog.this.setCanPullUp(true);
            }

            @Override
            public void onSpringEndStateChange(Spring spring) {
            }

            @Override
            public void onSpringUpdate(Spring spring) {
                if (COUIBottomSheetDialog.this.mPanelSpringBackAnim == null || COUIBottomSheetDialog.this.mDesignBottomSheetFrameLayout == null) {
                    return;
                }
                if (spring.wasAtRest() && spring.getVelocity() == 0.0d) {
                    COUIBottomSheetDialog.this.mPanelSpringBackAnim.setAtRest();
                    return;
                }
                int currentValue = (int) spring.getCurrentValue();
                COUIBottomSheetDialog.this.mDesignBottomSheetFrameLayout.offsetTopAndBottom(currentValue - COUIBottomSheetDialog.this.mCurrentSpringTotalOffset);
                COUIBottomSheetDialog.this.mCurrentSpringTotalOffset = currentValue;
                COUIBottomSheetDialog.this.setPulledUpViewPaddingBottom(endPadding - currentValue);
            }
        });
        this.mPanelSpringBackAnim.setEndValue(endPadding);
    }

    private void doTranslationAndScaleSpringAnimaion(final Animator.AnimatorListener animatorListener) {
        initTranslationAndScaleSpringAnimation();
        if (hasAnimationFlag(ANIMATION_TYPE_TRANSLATION)) {
            this.mSpringForceTranslationAndScale.setResponse(getTranslationResponse());
        } else if (hasAnimationFlag(ANIMATION_TYPE_SCALE)) {
            if (this.mIsEntering) {
                this.mSpringForceTranslationAndScale.setResponse(DEFAULT_CENTER_SHOW_SPRING_RESPONSE);
            } else {
                this.mSpringForceTranslationAndScale.setResponse(DEFAULT_CENTER_HIDE_SPRING_RESPONSE);
            }
        }
        this.mTranslationAndScaleSpringAnimation.removeEndListener(this.mTranslationAndScaleEndListener);
        COUIDynamicAnimation.OnAnimationEndListener onAnimationEndListener = new COUIDynamicAnimation.OnAnimationEndListener() {
            @Override
            public void onAnimationEnd(COUIDynamicAnimation cOUIDynamicAnimation, boolean canceled, float value, float velocity) {
                if (canceled) {
                    animatorListener.onAnimationCancel(null);
                } else {
                    animatorListener.onAnimationEnd(null);
                }
                COUIBottomSheetDialog.this.mTranslationAndScaleSpringAnimation.removeEndListener(COUIBottomSheetDialog.this.mTranslationAndScaleEndListener);
                COUIBottomSheetDialog.this.mTranslationAndScaleSpringAnimation.removeUpdateListener(COUIBottomSheetDialog.this.mTranslationAndScaleUpdateListener);
                if (COUIBottomSheetDialog.this.mAlphaSpringAnimation == null || !COUIBottomSheetDialog.this.mAlphaSpringAnimation.isRunning()) {
                    return;
                }
                COUIBottomSheetDialog.this.mAlphaSpringAnimation.cancelComplete();
            }
        };
        this.mTranslationAndScaleEndListener = onAnimationEndListener;
        this.mTranslationAndScaleSpringAnimation.addEndListener(onAnimationEndListener);
        animatorListener.onAnimationStart(null);
        this.mTranslationAndScaleSpringAnimation.removeUpdateListener(this.mTranslationAndScaleUpdateListener);
        this.mTranslationAndScaleUpdateListener = new COUIDynamicAnimation.OnAnimationUpdateListener() {
            @Override
            public void onAnimationUpdate(COUIDynamicAnimation cOUIDynamicAnimation, float value, float velocity) {
                float progress = COUIBottomSheetDialog.this.mEndValueOfTranslateAnimation != COUIBottomSheetDialog.this.mStartValueOfTranslateAnimation ? (value - COUIBottomSheetDialog.this.mStartValueOfTranslateAnimation) / (COUIBottomSheetDialog.this.mEndValueOfTranslateAnimation - COUIBottomSheetDialog.this.mStartValueOfTranslateAnimation) : 0.0f;
                if (COUIBottomSheetDialog.this.hasAnimationFlag(ANIMATION_TYPE_TRANSLATION)) {
                    COUIBottomSheetDialog.this.translateUpdate(value);
                }
                if (!COUIBottomSheetDialog.this.hasAnimationFlag(ANIMATION_TYPE_SCALE) || COUIBottomSheetDialog.this.mDesignBottomSheetFrameLayout == null) {
                    return;
                }
                float scale = COUIBottomSheetDialog.this.mIsEntering ? (progress * DIALOG_SHOW_SCALE_DELTA) + DIALOG_SHOW_SCALE_START : ((1.0f - progress) * DIALOG_SHOW_SCALE_DELTA) + DIALOG_SHOW_SCALE_START;
                COUIBottomSheetDialog.this.mDesignBottomSheetFrameLayout.setScaleX(scale);
                COUIBottomSheetDialog.this.mDesignBottomSheetFrameLayout.setScaleY(scale);
            }
        };
        if (!this.mSupportExitBlockingAnimation) {
            if (this.mIsEntering) {
                this.mTranslationAndScaleSpringAnimation.setMinimumVisibleChange(DEFAULT_MINIMUM_VISIBLE_CHANGE_SHOW);
            } else {
                this.mTranslationAndScaleSpringAnimation.setMinimumVisibleChange(DEFAULT_MINIMUM_VISIBLE_CHANGE_DISMISS);
            }
        }
        this.mTranslationAndScaleSpringAnimation.addUpdateListener(this.mTranslationAndScaleUpdateListener);
        setFrameRate(this.mTranslationAndScaleSpringAnimation);
        this.mTranslationAndScaleSpringAnimation.setStartValue(this.mStartValueOfTranslateAnimation);
        this.mTranslationAndScaleSpringAnimation.animateToFinalPosition(this.mIsEntering ? this.mEndValueOfTranslateAnimation : this.mEndValueOfTranslateAnimation + 1.0f);
    }

    private void enforceChangeScreenWidth() {
        if (this.mPreferWidth == -1) {
            return;
        }
        try {
            Resources resources = getContext().getResources();
            Configuration configuration = resources.getConfiguration();
            this.mOriginWidth = configuration.screenWidthDp;
            configuration.screenWidthDp = this.mPreferWidth;
            resources.updateConfiguration(configuration, resources.getDisplayMetrics());
            Log.d(TAG, "enforceChangeScreenWidth : OriginWidth=" + this.mOriginWidth + " ,PreferWidth:" + this.mPreferWidth);
            COUIPanelPercentFrameLayout cOUIPanelPercentFrameLayout = this.mDesignBottomSheetFrameLayout;
            if (cOUIPanelPercentFrameLayout != null) {
                cOUIPanelPercentFrameLayout.setPreferWidth(this.mPreferWidth);
            }
        } catch (Exception unused) {
            Log.d(TAG, "enforceChangeScreenWidth : failed to updateConfiguration");
        }
    }

    private void ensureDraggableContentLayout() {
        if (this.mDraggableConstraintLayout == null) {
            createPanelConstraintLayout();
        }
    }


    public int getContentViewHeightWithMargins() {
        COUIPanelPercentFrameLayout cOUIPanelPercentFrameLayout = this.mDesignBottomSheetFrameLayout;
        if (cOUIPanelPercentFrameLayout != null) {
            return cOUIPanelPercentFrameLayout.getMeasuredHeight() + COUIViewMarginUtil.getMargin(this.mDesignBottomSheetFrameLayout, 3);
        }
        return 0;
    }

    private boolean getFocusChange() {
        Boolean bool = this.mFocusChange;
        if (bool == null) {
            return false;
        }
        return bool.booleanValue();
    }

    private Rect getLocationRectInScreen(View view) {
        int[] iArr = new int[2];
        view.getLocationOnScreen(iArr);
        int left = iArr[0];
        return new Rect(left, iArr[1], view.getMeasuredWidth() + left, iArr[1] + view.getMeasuredHeight());
    }

    private int getNavColor() {
        COUIPanelPercentFrameLayout cOUIPanelPercentFrameLayout;
        int navColor = this.mNavColor;
        return navColor != Integer.MAX_VALUE ? navColor : (this.mIsHandlePanel || ((cOUIPanelPercentFrameLayout = this.mDesignBottomSheetFrameLayout) != null && cOUIPanelPercentFrameLayout.getRatio() == 1.0f)) ? this.mPanelBackgroundTintColor : this.mColorMask;
    }

    private Drawable getNavigationDrawable(int color) {
        if (this.mGradientDrawable == null) {
            GradientDrawable gradientDrawable = new GradientDrawable();
            this.mGradientDrawable = gradientDrawable;
            gradientDrawable.setShape(0);
            this.mGradientDrawable.setOrientation(GradientDrawable.Orientation.BOTTOM_TOP);
        }
        this.mGradientDrawable.setColors(new int[]{color, color, getSpecifiedTransparencyColor(color, FIRST_TIER_ALPHA), getSpecifiedTransparencyColor(color, SECOND_TIER_ALPHA), getSpecifiedTransparencyColor(color, 0.25f), 0});
        return this.mGradientDrawable;
    }

    private COUIPanelPullUpListener getPanelPullUpListener() {
        return new COUIPanelPullUpListener() {
            private int mLastPosition = -1;

            @Override
            public void onCancel() {
                COUIBottomSheetDialog.this.setPulledUpViewPaddingBottom(0);
            }

            @Override
            public int onDragging(int dy, int expandedOffset) {
                if (COUIBottomSheetDialog.this.mPanelSpringBackAnim != null && COUIBottomSheetDialog.this.mPanelSpringBackAnim.getVelocity() != 0.0d) {
                    COUIBottomSheetDialog.this.mPanelSpringBackAnim.setAtRest();
                    return COUIBottomSheetDialog.this.mParentViewPaddingBottom;
                }
                int iB = androidx.core.math.MathUtils.clamp((int) ((COUIBottomSheetDialog.this.mPulledUpView.getPaddingBottom() - COUIBottomSheetDialog.this.mPanelPaddingBottom) - (dy * 0.19999999f)), 0, Math.min(COUIBottomSheetDialog.this.mPullUpMaxOffset, COUIBottomSheetDialog.this.mDesignBottomSheetFrameLayout.getTop()));
                if (COUIBottomSheetDialog.this.mParentViewPaddingBottom != iB) {
                    COUIBottomSheetDialog.this.mParentViewPaddingBottom = iB;
                    COUIBottomSheetDialog cOUIBottomSheetDialog = COUIBottomSheetDialog.this;
                    cOUIBottomSheetDialog.setPulledUpViewPaddingBottom(cOUIBottomSheetDialog.mParentViewPaddingBottom);
                }
                return COUIBottomSheetDialog.this.mParentViewPaddingBottom;
            }

            @Override
            public void onDraggingPanel() {
                boolean unused = COUIBottomSheetDialog.this.mIsInTinyScreen;
            }

            @Override
            public void onOffsetChanged(float slideOffset) {
                if (this.mLastPosition == -1) {
                    this.mLastPosition = COUIBottomSheetDialog.this.mDesignBottomSheetFrameLayout.getHeight();
                }
                if (COUIBottomSheetDialog.this.mDialogOffsetListener != null) {
                    COUIBottomSheetDialog.this.mDialogOffsetListener.onDialogOffsetChanged(COUIBottomSheetDialog.this.mDesignBottomSheetFrameLayout.getTop());
                }
                if (COUIBottomSheetDialog.this.mIsNeedOutsideViewAnim && !COUIBottomSheetDialog.this.mIsExecutingDismissAnim) {
                    float fMax = Math.max(0.0f, COUIBottomSheetDialog.this.getOutsideViewAlpha(slideOffset));
                    COUIBottomSheetDialog.this.mOutsideView.setAlpha(fMax);
                    COUIBottomSheetDialog.this.mCurrentOutsideAlpha = fMax;
                    if ((!COUIPanelMultiWindowUtils.isSmallScreen(COUIBottomSheetDialog.this.getContext(), null)) && COUINavigationBarUtil.isNavigationBarShow(COUIBottomSheetDialog.this.getContext()) && ((!COUIBottomSheetDialog.this.mIsHandlePanel || COUIBottomSheetDialog.this.shouldHandlePanelUpdateNavBarColor()) && COUIBottomSheetDialog.this.getWindow() != null && ((int) (COUIBottomSheetDialog.this.mOutsideViewBackgroundAlpha * slideOffset)) != 0 && !COUINavigationBarUtil.isGestureNavigation(COUIBottomSheetDialog.this.getContext()))) {
                        COUIBottomSheetDialog.this.setNavigationBarColorAlpha(fMax);
                    }
                }
                if (COUIBottomSheetDialog.this.mPanelBarView == null || slideOffset == 1.0f || !COUIBottomSheetDialog.this.mIsInTinyScreen) {
                    return;
                }
                COUIBottomSheetDialog.this.mPanelBarView.setPanelOffset(this.mLastPosition - ((int) (COUIBottomSheetDialog.this.mDesignBottomSheetFrameLayout.getHeight() * slideOffset)));
                this.mLastPosition = (int) (COUIBottomSheetDialog.this.mDesignBottomSheetFrameLayout.getHeight() * slideOffset);
            }

            @Override
            public void onReleased(int paddingBottom) {
                COUIBottomSheetDialog.this.setCanPullUp(false);
                int top = COUIBottomSheetDialog.this.mDesignBottomSheetFrameLayout.getTop() - (paddingBottom - COUIBottomSheetDialog.this.mParentViewPaddingBottom);
                COUIBottomSheetDialog cOUIBottomSheetDialog = COUIBottomSheetDialog.this;
                cOUIBottomSheetDialog.doSpringBackReboundAnim(cOUIBottomSheetDialog.mParentViewPaddingBottom - top);
            }

            @Override
            public void onReleasedDrag() {
                boolean unused = COUIBottomSheetDialog.this.mIsInTinyScreen;
            }
        };
    }


    public Animator.AnimatorListener getPanelShowAnimListener() {
        return new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animator) {
                COUIBottomSheetDialog.this.animationEnd();
            }

            @Override
            public void onAnimationStart(Animator animator) {
                COUIBottomSheetDialog.this.animationStart();
            }
        };
    }

    private float getRevertAnimationFinalPositionToHide() {
        return this.mIsAnimationInFirst ? this.mStartValueOfTranslateAnimation : this.mEndValueOfTranslateAnimation;
    }

    private float getRevertAnimationFinalPositionToShow() {
        return this.mIsAnimationInFirst ? this.mEndValueOfTranslateAnimation : this.mStartValueOfTranslateAnimation;
    }

    private int getSpecifiedTransparencyColor(int color, float alphaFactor) {
        return Color.argb((int) ((Color.alpha(color) / MAX_ALPHA) * alphaFactor * MAX_ALPHA), Color.red(color), Color.green(color), Color.blue(color));
    }

    private int getTranslationDistance() {
        int height = this.mDesignBottomSheetFrameLayout.getHeight() - this.mPanelPaddingBottom;
        InputMethodManager inputMethodManager = this.mInputMethodManager;
        boolean imeAccepting = inputMethodManager != null && inputMethodManager.isAcceptingText();
        if (!this.mIsEntering && isInMultiWindowMode() && imeAccepting) {
            this.mDesignBottomSheetFrameLayout.getGlobalVisibleRect(this.mTemtRect);
            height = Math.max(height, UIUtil.getScreenHeightMetrics(getContext()) - this.mTemtRect.top);
        }
        COUIBottomSheetBehavior cOUIBottomSheetBehavior = (COUIBottomSheetBehavior) getBehavior();
        return cOUIBottomSheetBehavior.getState() == 3 ? height : cOUIBottomSheetBehavior.getState() == 6 ? this.mCoordinatorLayout.getMeasuredHeight() - cOUIBottomSheetBehavior.halfExpandedOffset : cOUIBottomSheetBehavior.getState() == 4 ? this.mPeekHeight : height;
    }

    private float getTranslationResponse() {
        float minResponse;
        float maxResponse;
        if (this.mIsEntering) {
            minResponse = DEFAULT_TRANSLATION_SHOW_SPRING_RESPONSE_SMALL;
            maxResponse = DEFAULT_TRANSLATION_SHOW_SPRING_RESPONSE_LARGE;
        } else {
            minResponse = DEFAULT_TRANSLATION_HIDE_SPRING_RESPONSE_SMALL;
            maxResponse = DEFAULT_TRANSLATION_HIDE_SPRING_RESPONSE_LARGE;
        }
        float currentVisibleHeight = this.mDesignBottomSheetFrameLayout.getHeight() - this.mPanelPaddingBottom;
        int state = getBehavior().getState();
        if (state == 4) {
            currentVisibleHeight = this.mPeekHeight;
        } else if (state == 6) {
            currentVisibleHeight = this.mCoordinatorLayout.getHeight() * getBehavior().getHalfExpandedRatio();
        } else if (state != 3) {
            currentVisibleHeight = UIUtil.getScreenHeightMetrics(getContext()) - this.mDesignBottomSheetFrameLayout.getTop();
        }
        float maxVisibleRange = Math.max(Float.valueOf(this.mCoordinatorLayout.getHeight() - this.mPeekHeight).floatValue(), 0.0f);
        float currentRange = Math.max(Float.valueOf(currentVisibleHeight - this.mPeekHeight).floatValue(), 0.0f);
        float rangeFraction = maxVisibleRange != 0.0f ? currentRange / maxVisibleRange : 1.0f;
        float clampedFraction = Math.max(0.0f, rangeFraction);
        return minResponse + ((maxResponse - minResponse) * clampedFraction);
    }

    private Drawable getTypedArrayDrawable(TypedArray typedArray, int index, int fallbackResId) {
        Drawable drawable = typedArray != null ? typedArray.getDrawable(index) : null;
        return drawable == null ? getContext().getResources().getDrawable(fallbackResId, getContext().getTheme()) : drawable;
    }


    public void handleBehaviorStateChange(View view, int state) {
        if (state == 3 || state == 6 || state == 4) {
            this.mLastStaticState = state;
        }
        if (state == 2) {
            if (needHideKeyboardWhenSettling()) {
                hideKeyboard();
            }
        } else if (state == 3) {
            this.mAdjustResizeEnable = true;
            this.mWindowInsetsAnimEnable = false;
        } else if (state == 5 && !this.mIsRevertAnimationFromSettlingAnimation) {
            dismiss();
        }
    }


    public boolean hasAnimationFlag(int flag) {
        return (this.mAnimationFlag & flag) > 0;
    }

    private boolean hasEditText(ViewGroup viewGroup) {
        for (int index = 0; index < viewGroup.getChildCount(); index++) {
            View childAt = viewGroup.getChildAt(index);
            if ((childAt instanceof EditText) || (childAt instanceof COUIInputView)) {
                return true;
            }
            if ((childAt instanceof ViewGroup) && hasEditText((ViewGroup) childAt)) {
                return true;
            }
        }
        return false;
    }


    public boolean haveEnoughSpace() {
        View view;
        if (this.mDesignBottomSheetFrameLayout == null || (view = this.mAnchorView) == null) {
            return false;
        }
        Rect locationRectInScreen = getLocationRectInScreen(view);
        int measuredWidth = this.mDesignBottomSheetFrameLayout.getMeasuredWidth();
        int measuredHeight = this.mDesignBottomSheetFrameLayout.getMeasuredHeight();
        Rect locationRectInScreen2 = getLocationRectInScreen(((ViewGroup) this.mAnchorView.getRootView()).getChildAt(0));
        int navigationBarHeight = COUINavigationBarUtil.getNavigationBarHeight(getContext());
        int dimensionPixelOffset = getContext().getResources().getDimensionPixelOffset(R.dimen.coui_bottom_sheet_dialog_follow_hand_margin_bottom);
        int dimensionPixelOffset2 = getContext().getResources().getDimensionPixelOffset(R.dimen.coui_bottom_sheet_dialog_follow_hand_margin_right);
        if ((locationRectInScreen.left - measuredWidth) - dimensionPixelOffset2 <= locationRectInScreen2.left && locationRectInScreen.right + measuredWidth + dimensionPixelOffset2 >= locationRectInScreen2.right && ((locationRectInScreen.top - measuredHeight) - this.mCoordinatorLayoutMinInsetsTop) - dimensionPixelOffset <= locationRectInScreen2.top && locationRectInScreen.bottom + measuredHeight + navigationBarHeight + dimensionPixelOffset >= locationRectInScreen2.bottom) {
            Log.d(TAG, "anchor view have no enoughSpace anchorContentViewLocationRect: " + locationRectInScreen2);
            this.mDesignBottomSheetFrameLayout.setHasAnchor(false);
            this.mDesignBottomSheetFrameLayout.setElevation(0.0f);
            this.mOutsideView.setAlpha(1.0f);
            return false;
        }
        Log.d(TAG, "anchor view haveEnoughSpace");
        this.mDesignBottomSheetFrameLayout.setHasAnchor(true);
        this.mDesignBottomSheetFrameLayout.setTop(0);
        this.mDesignBottomSheetFrameLayout.setBottom(measuredHeight);
        ShadowUtils.setElevationToView(this.mDesignBottomSheetFrameLayout, 3, getContext().getResources().getDimensionPixelOffset(com.coui.appcompat.R.dimen.coui_bottom_sheet_dialog_elevation), androidx.core.content.ContextCompat.getColor(getContext(), com.coui.appcompat.R.color.coui_panel_follow_hand_spot_shadow_color));
        this.mOutsideView.setAlpha(ALPHA_TRANSPARENT);
        setCanPullUp(false);
        getBehavior().setDraggable(false);
        return true;
    }

    private void hideKeyboard() {
        InputMethodManager inputMethodManager = this.mInputMethodManager;
        if (inputMethodManager == null || !inputMethodManager.isActive()) {
            return;
        }
        if (getWindow() != null) {
            this.mAdjustResizeEnable = false;
        }
        COUIPanelPercentFrameLayout cOUIPanelPercentFrameLayout = this.mDesignBottomSheetFrameLayout;
        if (cOUIPanelPercentFrameLayout != null) {
            this.mInputMethodManager.hideSoftInputFromWindow(cOUIPanelPercentFrameLayout.getWindowToken(), 0);
        }
    }

    private void initBehavior() {
        int state;
        boolean fitToContents;
        if (!(getBehavior() instanceof COUIBottomSheetBehavior)) {
            throw new IllegalArgumentException("Must use COUIBottomSheetBehavior, check value of bottom_sheet_behavior in strings.xml");
        }
        COUIBottomSheetBehavior cOUIBottomSheetBehavior = (COUIBottomSheetBehavior) getBehavior();
        cOUIBottomSheetBehavior.applyPhysics(this.mPhysicsFrequency, this.mPhysicsDampingRatio);
        cOUIBottomSheetBehavior.setGlobalDrag(this.mGlobalDrag);
        cOUIBottomSheetBehavior.setIsInTinyScreen(this.mIsInTinyScreen);
        cOUIBottomSheetBehavior.setPanelPeekHeight(this.mPeekHeight);
        cOUIBottomSheetBehavior.setPanelSkipCollapsed(this.mSkipCollapsed);
        cOUIBottomSheetBehavior.setIsHandlePanel(this.mIsHandlePanel);
        cOUIBottomSheetBehavior.setLayoutAtMaxHeight(this.mIsShowInMaxHeight);
        cOUIBottomSheetBehavior.setPanelPaddingBottom(this.mPanelPaddingBottom);
        if (this.mIsHandlePanel) {
            if (COUIPanelMultiWindowUtils.isNormalLandScreen(getContext(), this.mConfiguration)) {
                state = 4;
                fitToContents = true;
            } else {
                state = 6;
                fitToContents = false;
            }
            cOUIBottomSheetBehavior.setFitToContents(fitToContents);
            cOUIBottomSheetBehavior.setGestureInsetBottomIgnored(true);
            setIsNeedOutsideViewAnim(false);
        } else {
            state = 3;
        }
        int panelState = this.mFirstShowCollapsed ? 4 : state;
        cOUIBottomSheetBehavior.setPanelState(panelState);
        this.mLastStaticState = panelState;
        cOUIBottomSheetBehavior.addBottomSheetCallback(new COUIBottomSheetBehavior.COUIBottomSheetCallback() {
            @Override
            public void onSlide(View view, float slideOffset) {
            }

            @Override
            public void onStateChanged(View view, int newState) {
                if (COUIBottomSheetDialog.DEBUG) {
                    Log.d(COUIBottomSheetDialog.TAG, "onStateChanged: newState=" + newState);
                }
                COUIBottomSheetDialog.this.handleBehaviorStateChange(view, newState);
            }
        });
        if (DEBUG) {
            Log.d(TAG, "initBehavior: peekHeight=" + this.mPeekHeight + " mSkipCollapsed=" + this.mSkipCollapsed + " mIsHandlePanel=" + this.mIsHandlePanel + " mFirstShowCollapsed=" + this.mFirstShowCollapsed + " state=" + panelState);
        }
    }


    public void initCoordinateInsets(WindowInsets windowInsets) {
        View view = this.mCoordinatorLayout;
        if (view == null || windowInsets == null) {
            return;
        }
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
        boolean isPhone = getContext().getResources().getConfiguration().smallestScreenWidthDp < 600;
        this.mCoordinatorLayoutMinInsetsTop = (int) getContext().getResources().getDimension(com.coui.appcompat.R.dimen.coui_bottom_sheet_margin_top_default);
        if (isPhone && windowInsets.getSystemWindowInsetTop() > 0) {
            this.mCoordinatorLayoutMinInsetsTop = windowInsets.getSystemWindowInsetTop();
        }
        if (this.mIsInTinyScreen) {
            if (this.mIsFullScreenInTinyScreen) {
                this.mCoordinatorLayoutMinInsetsTop = (int) getContext().getResources().getDimension(com.coui.appcompat.R.dimen.coui_panel_min_padding_top_tiny_screen);
            } else {
                this.mCoordinatorLayoutMinInsetsTop = (int) getContext().getResources().getDimension(com.coui.appcompat.R.dimen.coui_panel_normal_padding_top_tiny_screen);
            }
        }
        int topMargin = layoutParams.topMargin;
        int minInsetsTop = this.mCoordinatorLayoutMinInsetsTop;
        if (topMargin != minInsetsTop) {
            layoutParams.topMargin = minInsetsTop;
            this.mCoordinatorLayout.setLayoutParams(layoutParams);
        }
        COUIPanelContentLayout cOUIPanelContentLayout = this.mDraggableConstraintLayout;
        if (cOUIPanelContentLayout != null) {
            Configuration configuration = this.mConfiguration;
            COUIPanelPercentFrameLayout cOUIPanelPercentFrameLayout = this.mDesignBottomSheetFrameLayout;
            cOUIPanelContentLayout.setNavigationMargin(configuration, windowInsets, cOUIPanelPercentFrameLayout != null && cOUIPanelPercentFrameLayout.getRatio() == 1.0f, this.mCouiPanelEdgeToEdgeEnable);
        }
    }

    private void initDraggableConstraintLayoutSize() {
        setPanelWidth();
        setPanelHeight();
    }


    public void initMaxHeight(WindowInsets windowInsets) {
        boolean atMaxHeight = this.mPanelHeight >= COUIPanelMultiWindowUtils.getPanelMaxHeight(getContext(), null, windowInsets, this.mIsHandlePanel, this.mCouiPanelEdgeToEdgeEnable);
        COUIPanelPercentFrameLayout cOUIPanelPercentFrameLayout = this.mDesignBottomSheetFrameLayout;
        if (cOUIPanelPercentFrameLayout != null) {
            cOUIPanelPercentFrameLayout.getLayoutParams().height = (this.mIsShowInMaxHeight || atMaxHeight) ? -1 : -2;
        }
        COUIPanelContentLayout cOUIPanelContentLayout = this.mDraggableConstraintLayout;
        if (cOUIPanelContentLayout != null) {
            if (this.mIsShowInMaxHeight || atMaxHeight) {
                cOUIPanelContentLayout.getLayoutParams().height = -1;
            }
        }
    }


    public void initOrRefreshNavigationView() {
        if (unNeedNavigationCustomView()) {
            if (this.mNavigationCustomView == null || !(this.mContainerFrameLayout.getParent() instanceof FrameLayout)) {
                return;
            }
            FrameLayout frameLayout = (FrameLayout) this.mContainerFrameLayout.getParent();
            if (frameLayout.indexOfChild(this.mNavigationCustomView) != -1) {
                frameLayout.removeView(this.mNavigationCustomView);
            }
            this.mNavigationCustomView = null;
            return;
        }
        if (this.mNavigationCustomView == null) {
            this.mNavigationCustomView = new View(getContext());
        }
        setNavigationBarColor(getNavColor());
        if (this.mContainerFrameLayout.getParent() instanceof FrameLayout) {
            FrameLayout frameLayout2 = (FrameLayout) this.mContainerFrameLayout.getParent();
            if (frameLayout2.indexOfChild(this.mNavigationCustomView) != -1) {
                setNavigationCustomViewHeight(this.mApplyWindowInsets);
            } else {
                frameLayout2.addView(this.mNavigationCustomView, new FrameLayout.LayoutParams(-1, this.mApplyWindowInsets.getInsets(WindowInsets.Type.navigationBars()).bottom, 80));
            }
        }
    }

    private void initPeekHeight() {
        if (this.mIsGestureNavigation) {
            this.mPeekHeight = getContext().getResources().getDimensionPixelOffset(com.coui.appcompat.R.dimen.coui_panel_default_peek_height_in_gesture);
        } else if (this.mCouiPanelEdgeToEdgeEnable) {
            this.mPeekHeight = getContext().getResources().getDimensionPixelOffset(com.coui.appcompat.R.dimen.coui_panel_default_peek_height_panel_extend_to_navi);
        } else {
            this.mPeekHeight = getContext().getResources().getDimensionPixelOffset(com.coui.appcompat.R.dimen.coui_panel_default_peek_height);
        }
    }

    private void initPhysics() {
        if (this.mAppearStiffness == Float.MIN_VALUE) {
            this.mAppearStiffness = DEFAULT_SPRING_STIFFNESS;
        }
        if (this.mAppearDampingRatio == Float.MIN_VALUE) {
            this.mAppearDampingRatio = DEFAULT_SPRING_DAMPING_RATIO;
        }
        this.mAppearSpringForce = new SpringForce(0.0f).setStiffness(this.mAppearStiffness).setDampingRatio(this.mAppearDampingRatio);
        SpringAnimation eVarS = new SpringAnimation(new FloatValueHolder()).setSpring(this.mAppearSpringForce);
        this.mAppearSpringAnim = eVarS;
        eVarS.addEndListener(new DynamicAnimation.OnAnimationEndListener() {
            @Override
            public void onAnimationEnd(DynamicAnimation animation, boolean canceled, float value, float velocity) {
                COUIBottomSheetDialog.this.onAppearAnimationEnd(canceled, value, velocity);
            }
        });
        this.mAppearSpringAnim.addUpdateListener(new DynamicAnimation.OnAnimationUpdateListener() {
            @Override
            public void onAnimationUpdate(DynamicAnimation animation, float value, float velocity) {
                COUIBottomSheetDialog.this.onAppearAnimationUpdate(value, velocity);
            }
        });
    }

    private void initThemeResources(int defStyleRes) {
        TypedArray typedArrayObtainStyledAttributes = getContext().obtainStyledAttributes(null, com.coui.appcompat.R.styleable.COUIBottomSheetDialog, com.coui.appcompat.R.attr.couiBottomSheetDialogStyle, defStyleRes);
        this.mPanelDragViewDrawable = getTypedArrayDrawable(typedArrayObtainStyledAttributes, com.coui.appcompat.R.styleable.COUIBottomSheetDialog_panelDragViewIcon, com.coui.appcompat.R.drawable.coui_panel_drag_view);
        int color = typedArrayObtainStyledAttributes.getColor(com.coui.appcompat.R.styleable.COUIBottomSheetDialog_panelDragViewTintColor, COUIContextUtil.getAttrColor(getContext(), com.coui.appcompat.R.attr.couiColorControls));
        this.mPanelDragViewDrawableTintColor = color;
        this.mPanelDragViewDrawable.setTint(color);
        this.mPanelBackground = getTypedArrayDrawable(typedArrayObtainStyledAttributes, com.coui.appcompat.R.styleable.COUIBottomSheetDialog_panelBackground, com.coui.appcompat.R.drawable.coui_default_panel_bg_without_shadow);
        this.mPanelBackgroundTintColor = typedArrayObtainStyledAttributes.getColor(com.coui.appcompat.R.styleable.COUIBottomSheetDialog_panelBackgroundTintColor, COUIContextUtil.getAttrColor(getContext(), com.coui.appcompat.R.attr.couiColorSurface));
        this.mHandleViewHasPressAnim = typedArrayObtainStyledAttributes.getBoolean(com.coui.appcompat.R.styleable.COUIBottomSheetDialog_couiHandleViewHasPressAnim, true);
        this.mIsShowInMaxHeight = typedArrayObtainStyledAttributes.getBoolean(com.coui.appcompat.R.styleable.COUIBottomSheetDialog_couiShowMaxHeight, true);
        this.mIsHandlePanel = typedArrayObtainStyledAttributes.getBoolean(com.coui.appcompat.R.styleable.COUIBottomSheetDialog_couiIsHandlePanel, false);
        this.mDefaultPaddingBottom = typedArrayObtainStyledAttributes.getDimensionPixelSize(com.coui.appcompat.R.styleable.COUIBottomSheetDialog_couiPanelPaddingBottom, getContext().getResources().getDimensionPixelSize(com.coui.appcompat.R.dimen.coui_panel_padding_bottom));
        this.mCouiPanelEdgeToEdgeEnable = typedArrayObtainStyledAttributes.getBoolean(com.coui.appcompat.R.styleable.COUIBottomSheetDialog_couiPanelEdgeToEdge, false);
        this.mSupportExitBlockingAnimation = typedArrayObtainStyledAttributes.getBoolean(com.coui.appcompat.R.styleable.COUIBottomSheetDialog_couiPanelSupportExitBlockingAnimation, false);
        getAdjustResizeHelper().setCouiPanelEdgeToEdgeEnable(this.mCouiPanelEdgeToEdgeEnable);
        if (this.mIsHandlePanel && this.mSkipCollapsed) {
            this.mSkipCollapsed = false;
        }
        typedArrayObtainStyledAttributes.recycle();
        Drawable drawable = this.mPanelBackground;
        if (drawable != null) {
            drawable.setTint(this.mPanelBackgroundTintColor);
        }
    }

    private void initTranslationAndScaleSpringAnimation() {
        if (this.mTranslationAndScaleSpringAnimation == null) {
            this.mTranslationAndScaleSpringAnimation = new COUISpringAnimation(new FloatValueHolder());
            COUISpringForce cOUISpringForce = new COUISpringForce();
            this.mSpringForceTranslationAndScale = cOUISpringForce;
            cOUISpringForce.setBounce(DEFAULT_TRANSLATION_SPRING_BOUNCE);
            this.mTranslationAndScaleSpringAnimation.setSpring(this.mSpringForceTranslationAndScale);
        }
    }

    private void initValueResources() {
        this.mPullUpMaxOffset = (int) getContext().getResources().getDimension(com.coui.appcompat.R.dimen.coui_panel_pull_up_max_offset);
        this.mCoordinatorLayoutMinInsetsTop = (int) getContext().getResources().getDimension(com.coui.appcompat.R.dimen.coui_panel_min_padding_top);
        this.mCoordinatorLayoutPaddingExtra = getContext().getResources().getDimensionPixelOffset(com.coui.appcompat.R.dimen.coui_panel_normal_padding_top);
        refreshColorMask();
        this.mIsGestureNavigation = COUINavigationBarUtil.isGestureNavigation(getContext());
        initPeekHeight();
    }

    private void initView() {
        IgnoreWindowInsetsFrameLayout ignoreWindowInsetsFrameLayout = (IgnoreWindowInsetsFrameLayout) findViewById(com.coui.appcompat.R.id.container);
        this.mContainerFrameLayout = ignoreWindowInsetsFrameLayout;
        if (ignoreWindowInsetsFrameLayout != null) {
            ignoreWindowInsetsFrameLayout.setCouiPanelEdgeToEdgeEnable(this.mCouiPanelEdgeToEdgeEnable);
        }
        this.mOutsideView = findViewById(com.coui.appcompat.R.id.panel_outside);
        operateBlockingAnimation();
        this.mCoordinatorLayout = findViewById(com.coui.appcompat.R.id.coordinator);
        COUIPanelPercentFrameLayout cOUIPanelPercentFrameLayout = (COUIPanelPercentFrameLayout) findViewById(com.coui.appcompat.R.id.design_bottom_sheet);
        this.mDesignBottomSheetFrameLayout = cOUIPanelPercentFrameLayout;
        cOUIPanelPercentFrameLayout.setUseNormalSmoothCorner(this.mUseNormalSmoothCorner);
        this.mDesignBottomSheetFrameLayout.setIsHandlePanel(this.mIsHandlePanel);
        this.mPanelBarView = (COUIPanelBarView) findViewById(com.coui.appcompat.R.id.panel_drag_bar);
        this.mDesignBottomSheetFrameLayout.getLayoutParams().height = this.mIsShowInMaxHeight ? -1 : -2;
        if (isFollowHand()) {
            this.mDesignBottomSheetFrameLayout.post(new Runnable() {
                @Override
                public void run() {
                    if (COUIBottomSheetDialog.this.haveEnoughSpace()) {
                        ShadowUtils.setElevationToView(COUIBottomSheetDialog.this.mDesignBottomSheetFrameLayout, 3, COUIBottomSheetDialog.this.getContext().getResources().getDimensionPixelOffset(com.coui.appcompat.R.dimen.coui_bottom_sheet_dialog_elevation), androidx.core.content.ContextCompat.getColor(COUIBottomSheetDialog.this.getContext(), com.coui.appcompat.R.color.coui_panel_follow_hand_spot_shadow_color));
                        COUIBottomSheetDialog.this.setCanPullUp(false);
                        COUIBottomSheetDialog.this.getBehavior().setDraggable(false);
                    }
                }
            });
        }
        COUIPanelContentLayout cOUIPanelContentLayout = this.mDraggableConstraintLayout;
        if (cOUIPanelContentLayout != null) {
            cOUIPanelContentLayout.setLayoutAtMaxHeight(this.mIsShowInMaxHeight);
        }
        this.mPulledUpView = this.mDesignBottomSheetFrameLayout;
        checkInitState();
        this.mOutsideView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (COUIBottomSheetDialog.this.mCancelable && COUIBottomSheetDialog.this.isShowing() && COUIBottomSheetDialog.this.mCanceledOnTouchOutside) {
                    COUIBottomSheetDialog.this.cancel();
                }
            }
        });
        this.mDesignBottomSheetFrameLayout.setBackground(this.mPanelBackground);
        updatePaddingBottom();
    }

    private void initWindow() {
        Window window = getWindow();
        if (window != null) {
            window.setDimAmount(0.0f);
            window.setLayout(-1, -1);
            window.setGravity(80);
        }
    }

    private void initWindowInsetsListener() {
        if (this.mShouldRegisterWindowInsetsListener && getWindow() != null && this.mWindowInsetsListener == null) {
            View decorView = getWindow().getDecorView();
            View.OnApplyWindowInsetsListener onApplyWindowInsetsListener = new View.OnApplyWindowInsetsListener() {
                @Override
                public WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                    if (view == null || view.getLayoutParams() == null) {
                        return windowInsets;
                    }
                    if (windowInsets.equals(COUIBottomSheetDialog.this.mApplyWindowInsets)) {
                        COUILog.d(COUIBottomSheetDialog.TAG, "Window inset is not change, return!");
                        return windowInsets;
                    }
                    COUIBottomSheetDialog.this.initCoordinateInsets(windowInsets);
                    COUIBottomSheetDialog.this.initMaxHeight(windowInsets);
                    if (COUIBottomSheetDialog.this.mInputMethodManager == null) {
                        COUIBottomSheetDialog cOUIBottomSheetDialog = COUIBottomSheetDialog.this;
                        cOUIBottomSheetDialog.mInputMethodManager = (InputMethodManager) cOUIBottomSheetDialog.getContext().getSystemService("input_method");
                    }
                    COUIBottomSheetDialog cOUIBottomSheetDialog2 = COUIBottomSheetDialog.this;
                    cOUIBottomSheetDialog2.adjustResize(windowInsets, cOUIBottomSheetDialog2.mAdjustResizeEnable);
                    COUIBottomSheetDialog.this.largeScreenLimitMaxSize();
                    if (!COUIBottomSheetDialog.this.mIsExecutingDismissAnim && COUIBottomSheetDialog.this.shouldUpdatePanelMarginBottom(windowInsets)) {
                        COUIBottomSheetDialog cOUIBottomSheetDialog3 = COUIBottomSheetDialog.this;
                        cOUIBottomSheetDialog3.updatePanelMarginBottom(cOUIBottomSheetDialog3.mConfiguration, windowInsets);
                    }
                    COUIBottomSheetDialog.this.mApplyWindowInsets = windowInsets;
                    view.onApplyWindowInsets(COUIBottomSheetDialog.this.mApplyWindowInsets);
                    COUIBottomSheetDialog.this.initOrRefreshNavigationView();
                    return COUIBottomSheetDialog.this.mApplyWindowInsets;
                }
            };
            this.mWindowInsetsListener = onApplyWindowInsetsListener;
            decorView.setOnApplyWindowInsetsListener(onApplyWindowInsetsListener);
        }
    }


    public boolean isFadeInCenter() {
        COUIPanelPercentFrameLayout cOUIPanelPercentFrameLayout = this.mDesignBottomSheetFrameLayout;
        return cOUIPanelPercentFrameLayout != null && cOUIPanelPercentFrameLayout.getRatio() == 2.0f && (getBehavior() == null || !(getBehavior() == null || getBehavior().getState() == 4));
    }

    private boolean isFadeInCenterAllState() {
        return this.mDesignBottomSheetFrameLayout.getRatio() == 2.0f;
    }


    public boolean isFollowHand() {
        COUIPanelPercentFrameLayout cOUIPanelPercentFrameLayout;
        return this.mAnchorView != null && (cOUIPanelPercentFrameLayout = this.mDesignBottomSheetFrameLayout) != null && cOUIPanelPercentFrameLayout.getRatio() == 2.0f && this.mAnchorView.isAttachedToWindow();
    }

    private boolean isInMultiWindowMode() {
        WeakReference<Activity> weakReference = this.mActivityWeakReference;
        return (weakReference == null || weakReference.get() == null || !COUIPanelMultiWindowUtils.isInMultiWindowMode(this.mActivityWeakReference.get())) ? false : true;
    }


    public void onAnimatorFrameRateUpdate(AnimationVelocityCalculator animationVelocityCalculator, ValueAnimator valueAnimator, ValueAnimator valueAnimator2) {
        float fCalculator = animationVelocityCalculator.calculator(this.mDesignBottomSheetFrameLayout.getHeight(), valueAnimator);
        COUILog.d(TAG, "DynamicFrameRateManager.getSuggestFrameRate: v " + fCalculator + " frame " + DynamicFrameRateManager.getSuggestFrameRate(fCalculator, 2));
        DynamicFrameRateManager.setFrameRate(this.mDesignBottomSheetFrameLayout, ANIMATION_TYPE_ID, (int) fCalculator, (Bundle) null);
    }


    public void onSpringFrameRateUpdate(COUIDynamicAnimation cOUIDynamicAnimation, float value, float velocity) {
        COUILog.d(TAG, "COUISpringAnimation DynamicFrameRateManager.getSuggestFrameRate: v " + Math.abs(velocity) + " frame " + DynamicFrameRateManager.getSuggestFrameRate(velocity, 2));
        DynamicFrameRateManager.setFrameRate(this.mDesignBottomSheetFrameLayout, ANIMATION_TYPE_ID, Math.abs((int) velocity), (Bundle) null);
    }


    public void largeScreenLimitMaxSize() {
        if (this.mDesignBottomSheetFrameLayout == null) {
            return;
        }
        int widthDp = getContext().getResources().getConfiguration().screenWidthDp;
        int heightDp = getContext().getResources().getConfiguration().screenHeightDp;
        if (!this.isLargeScreenLimitMaxSize || !COUIResponsiveUtils.isLargePadWindow(getContext(), widthDp, heightDp) || COUIPanelMultiWindowUtils.isInMultiWindowMode(COUIPanelMultiWindowUtils.contextToActivity(getContext()))) {
            this.mDesignBottomSheetFrameLayout.restoreDefaultMaxSize();
            this.mDesignBottomSheetFrameLayout.setMaxHeight(getContext().getResources().getDimensionPixelOffset(com.coui.appcompat.R.dimen.coui_panel_max_height));
            return;
        }
        int iMin = Math.min(UIUtil.getScreenHeightRealSize(getContext()), UIUtil.getScreenWidthRealSize(getContext()));
        int iMax = Math.max(UIUtil.getScreenHeightRealSize(getContext()), UIUtil.getScreenWidthRealSize(getContext()));
        this.mDesignBottomSheetFrameLayout.setMaxSize((int) COUIResponsiveUtils.calculateWidth(iMax, iMin, this.mDesignBottomSheetFrameLayout.getGridNumber(), this.mDesignBottomSheetFrameLayout.getPaddingType(), this.mDesignBottomSheetFrameLayout.getPaddingSize(), getContext()), iMin - (this.mCoordinatorLayoutMinInsetsTop * 2));
    }

    private boolean needHideKeyboardWhenSettling() {
        return ((COUIBottomSheetBehavior) getBehavior()).isCanHideKeyboard();
    }

    private int normalizePoints(int value, int max) {
        return Math.max(0, Math.min(value, max));
    }

    private void offsetViewTo() {
        int[] iArrCalculateFinalLocationOnScreen = calculateFinalLocationOnScreen(this.mAnchorView);
        this.mDesignBottomSheetFrameLayout.setX(iArrCalculateFinalLocationOnScreen[0]);
        this.mDesignBottomSheetFrameLayout.setY(iArrCalculateFinalLocationOnScreen[1]);
        this.mCurrentParentViewTranslationY = this.mDesignBottomSheetFrameLayout.getY();
    }

    private void operateBlockingAnimation() {
        View view = this.mOutsideView;
        if (view == null) {
            return;
        }
        if (!this.mSupportExitBlockingAnimation) {
            view.removeOnAttachStateChangeListener(this.mOnAttatchStateChangeListener);
            return;
        }
        if (this.mOSDKComputeListener == null) {
            this.mOSDKComputeListener = new com.oplus.wrapper.view.ViewTreeObserver.OnComputeInternalInsetsListener() {
                public void onComputeInternalInsets(com.oplus.wrapper.view.ViewTreeObserver.InternalInsetsInfo internalInsetsInfo) {
                    internalInsetsInfo.setTouchableInsets(com.oplus.wrapper.view.ViewTreeObserver.InternalInsetsInfo.TOUCHABLE_INSETS_REGION);
                    if (!COUIBottomSheetDialog.this.mIsExecutingDismissAnim) {
                        internalInsetsInfo.getTouchableRegion().set(0, 0, COUIBottomSheetDialog.this.mDesignBottomSheetFrameLayout.getRootView().getWidth(), COUIBottomSheetDialog.this.mDesignBottomSheetFrameLayout.getRootView().getHeight());
                    } else {
                        COUIBottomSheetDialog.this.mDesignBottomSheetFrameLayout.getGlobalVisibleRect(COUIBottomSheetDialog.this.mTemtRect);
                        internalInsetsInfo.getTouchableRegion().set(COUIBottomSheetDialog.this.mTemtRect);
                    }
                }
            };
        }
        if (this.mOnAttatchStateChangeListener == null) {
            this.mOnAttatchStateChangeListener = new View.OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View view2) {
                    COUIBottomSheetDialog.this.addOSDKViewTreeObserver();
                }

                @Override
                public void onViewDetachedFromWindow(View view2) {
                    COUIBottomSheetDialog.this.removeOSDKViewTreeObserver();
                }
            };
        }
        this.mOutsideView.addOnAttachStateChangeListener(this.mOnAttatchStateChangeListener);
    }


    public void outsideAlphaChange(float progress, boolean showing) {
        View view;
        View viewFindFocus;
        InputMethodManager inputMethodManager;
        float hiddenAlpha = this.mCurrentOutSideAlphaStateHidden;
        if (hiddenAlpha <= 0.0f) {
            hiddenAlpha = 1.0f;
        }
        float shownAlpha = this.mCurrentOutSideAlphaStateShow;
        if (shownAlpha <= 0.0f) {
            shownAlpha = 0.0f;
        }
        float outsideViewAlpha = getOutsideViewAlpha(progress);
        this.mCurrentOutsideAlpha = outsideViewAlpha;
        float fMax = showing ? shownAlpha + (outsideViewAlpha * (1.0f - shownAlpha)) : Math.max(0.0f, 1.0f - outsideViewAlpha) * hiddenAlpha;
        View view2 = this.mOutsideView;
        if (view2 != null) {
            view2.setAlpha(fMax);
        }
        boolean updateNavBar = isFollowHand() || isFadeInCenterAllState() || shouldHandlePanelUpdateNavBarColor();
        if (this.mOutsideView != null && COUIPanelMultiWindowUtils.isVirtualNavigation(getContext()) && updateNavBar && !this.mIsInTinyScreen) {
            setNavigationBarColorAlpha(fMax);
        } else if (this.mCouiPanelEdgeToEdgeEnable && (view = this.mNavigationCustomView) != null) {
            if (!this.mIsEntering) {
                progress = Math.max(0.0f, 1.0f - progress);
            }
            view.setAlpha(progress);
        }
        COUIPanelContentLayout cOUIPanelContentLayout = this.mDraggableConstraintLayout;
        if (cOUIPanelContentLayout == null || !this.mIsNeedShowKeyboard || (viewFindFocus = cOUIPanelContentLayout.findFocus()) == null || !showing || (inputMethodManager = this.mInputMethodManager) == null) {
            return;
        }
        inputMethodManager.showSoftInput(viewFindFocus, 0);
    }

    private void refreshColorMask() {
        this.mColorMask = getContext().getResources().getColor(com.coui.appcompat.R.color.coui_color_mask);
        this.mOutsideViewBackgroundAlpha = Color.alpha(this.mColorMask);
    }

    private void refreshParams() {
        if (COUIPanelMultiWindowUtils.isVirtualNavigation(getContext())) {
            return;
        }
        resetParentViewStyle(getContext().getResources().getConfiguration());
        resetNavigationBarColor();
    }

    private void registerApplicationConfigChangeListener() {
        getContext().registerComponentCallbacks(this.mComponentCallbacks);
    }


    public void registerBackCallback(View view) {
        OnBackInvokedDispatcher onBackInvokedDispatcherFindOnBackInvokedDispatcher = view.findOnBackInvokedDispatcher();
        if (onBackInvokedDispatcherFindOnBackInvokedDispatcher == null) {
            COUILog.e(TAG, "OnBackInvokedDispatcher is null！");
            return;
        }
        OnBackInvokedCallback onBackInvokedCallback = this.mOnBackInvokedCallback;
        if (onBackInvokedCallback != null) {
            try {
                onBackInvokedDispatcherFindOnBackInvokedDispatcher.unregisterOnBackInvokedCallback(onBackInvokedCallback);
            } catch (Exception e2) {
                COUILog.e(TAG, "unregisterOnBackInvokedCallback fail: " + e2.getMessage());
            }
            this.mOnBackInvokedCallback = null;
        }
        OnBackInvokedCallback onBackInvokedCallback2 = new OnBackInvokedCallback() {
            public void onBackInvoked() {
                if (COUIBottomSheetDialog.this.mOnBackInvokedLocalListener != null) {
                    COUIBottomSheetDialog.this.mOnBackInvokedLocalListener.onBackInvokedLocal();
                } else {
                    COUIBottomSheetDialog.this.onBackPressed();
                }
            }
        };
        this.mOnBackInvokedCallback = onBackInvokedCallback2;
        try {
            onBackInvokedDispatcherFindOnBackInvokedDispatcher.registerOnBackInvokedCallback(0, onBackInvokedCallback2);
        } catch (Exception e10) {
            COUILog.e(TAG, "registerOnBackInvokedCallback fail: " + e10.getMessage());
        }
    }

    private void registerBehaviorPullUpListener() {
        if (getBehavior() instanceof COUIBottomSheetBehavior) {
            this.mPanelPullUpListener = this.mCanPullUp ? getPanelPullUpListener() : null;
            ((COUIBottomSheetBehavior) getBehavior()).setPullUpListener(this.mPanelPullUpListener);
        }
    }

    private void registerPreDrawListener() {
        View view = this.mOutsideView;
        if (view != null) {
            view.getViewTreeObserver().addOnPreDrawListener(this.mOnPreDrawListener);
        }
    }

    private void registerPullUpToDismissPanelListener() {
        if (getBehavior() instanceof COUIBottomSheetBehavior) {
            ((COUIBottomSheetBehavior) getBehavior()).setPullUpToDismissPanelListener(this.mPullUpToDismissPanelListener);
        }
    }

    private void releaseApplicationConfigChangeListener() {
        if (this.mComponentCallbacks != null) {
            getContext().unregisterComponentCallbacks(this.mComponentCallbacks);
        }
    }

    private void releaseApplyWindowInsetsListener() {
        Window window = getWindow();
        if (window != null) {
            window.getDecorView().setOnApplyWindowInsetsListener(null);
            this.mWindowInsetsListener = null;
        }
    }

    private void releaseBehaviorPullUpListener() {
        if (getBehavior() instanceof COUIBottomSheetBehavior) {
            ((COUIBottomSheetBehavior) getBehavior()).setPullUpListener(null);
            this.mPanelPullUpListener = null;
        }
    }

    private void releasePullUpToDismissPanelListener() {
        if (getBehavior() instanceof COUIBottomSheetBehavior) {
            ((COUIBottomSheetBehavior) getBehavior()).setPullUpToDismissPanelListener(null);
        }
    }

    private void releaseResizeHelper() {
        COUIPanelAdjustResizeHelper cOUIPanelAdjustResizeHelper = this.mAdjustResizeHelper;
        if (cOUIPanelAdjustResizeHelper != null) {
            cOUIPanelAdjustResizeHelper.releaseData();
            this.mAdjustResizeHelper = null;
        }
    }


    public void removeOSDKViewTreeObserver() {
        com.oplus.wrapper.view.ViewTreeObserver viewTreeObserver = this.mOSDKViewTreeObserver;
        if (viewTreeObserver != null) {
            viewTreeObserver.removeOnComputeInternalInsetsListener(this.mOSDKComputeListener);
            this.mOSDKViewTreeObserver = null;
        }
    }


    public void removeOnPreDrawListener() {
        View view = this.mOutsideView;
        if (view != null) {
            view.getViewTreeObserver().removeOnPreDrawListener(this.mOnPreDrawListener);
        }
    }

    private void resetAnimationFlag() {
        this.mAnimationFlag = 0;
    }

    private void resetNavigationBarColor() {
        setNavigationBarColor(getNavColor());
    }

    private void resetParentViewStyle(Configuration configuration) {
        COUIPanelPercentFrameLayout cOUIPanelPercentFrameLayout = this.mDesignBottomSheetFrameLayout;
        if (cOUIPanelPercentFrameLayout == null) {
            return;
        }
        COUIViewMarginUtil.setMargin(cOUIPanelPercentFrameLayout, 3, 0);
    }

    private void resetWindowImeAnimFlags() {
        this.mAdjustResizeEnable = true;
        int softInputMode = 0;
        this.mIsNeedShowKeyboard = false;
        Window window = getWindow();
        getAdjustResizeHelper().setWindowType(window.getAttributes().type);
        int stateMask = window.getAttributes().softInputMode & 15;
        if (stateMask != 5 || isInMultiWindowMode() || this.mIsInWindowFloatingMode) {
            softInputMode = stateMask;
        } else {
            this.mIsNeedShowKeyboard = true;
        }
        window.setSoftInputMode(softInputMode | 48);
    }

    public static int resolveDialogTheme(Context context, int themeResId) {
        if (((themeResId >>> 24) & 255) >= 1) {
            return themeResId;
        }
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(com.coui.appcompat.R.attr.couiBottomSheetDialogStyle, typedValue, true);
        return typedValue.resourceId;
    }

    private void restoreScreenWidth() {
        if (this.mOriginWidth == -1) {
            return;
        }
        try {
            Resources resources = getContext().getResources();
            Configuration configuration = resources.getConfiguration();
            configuration.screenWidthDp = this.mOriginWidth;
            resources.updateConfiguration(configuration, resources.getDisplayMetrics());
            Log.d(TAG, "restoreScreenWidth : PreferWidth=" + this.mPreferWidth + " ,OriginWidth=" + this.mOriginWidth);
            COUIPanelPercentFrameLayout cOUIPanelPercentFrameLayout = this.mDesignBottomSheetFrameLayout;
            if (cOUIPanelPercentFrameLayout != null) {
                cOUIPanelPercentFrameLayout.delPreferWidth();
            }
        } catch (Exception unused) {
            Log.d(TAG, "restoreScreenWidth : failed to updateConfiguration");
        }
    }

    private boolean reversalAnimation(final Animator.AnimatorListener animatorListener, boolean toShow) {
        COUISpringAnimation cOUISpringAnimation;
        COUISpringAnimation cOUISpringAnimation2 = this.mTranslationAndScaleSpringAnimation;
        if (cOUISpringAnimation2 == null || !cOUISpringAnimation2.isRunning() || (cOUISpringAnimation = this.mAlphaSpringAnimation) == null || !cOUISpringAnimation.isRunning()) {
            return false;
        }
        if (toShow) {
            this.mTranslationAndScaleSpringAnimation.animateToFinalPosition(getRevertAnimationFinalPositionToShow());
            this.mAlphaSpringAnimation.animateToFinalPosition(getRevertAnimationFinalPositionToShow());
        } else {
            float revertAnimationFinalPositionToHide = getRevertAnimationFinalPositionToHide();
            if (this.mDesignBottomSheetFrameLayout.getRatio() == 1.0f) {
                revertAnimationFinalPositionToHide = Math.max(getRevertAnimationFinalPositionToHide(), getTranslationDistance());
            }
            this.mTranslationAndScaleSpringAnimation.animateToFinalPosition(revertAnimationFinalPositionToHide);
            this.mAlphaSpringAnimation.animateToFinalPosition(revertAnimationFinalPositionToHide);
            OnAnimationListener onAnimationListener = this.mAnimationListener;
            if (onAnimationListener != null) {
                onAnimationListener.onDismissAnimationStart();
            }
        }
        this.mTranslationAndScaleSpringAnimation.removeEndListener(this.mTranslationAndScaleEndListener);
        COUIDynamicAnimation.OnAnimationEndListener onAnimationEndListener = new COUIDynamicAnimation.OnAnimationEndListener() {
            @Override
            public void onAnimationEnd(COUIDynamicAnimation cOUIDynamicAnimation, boolean canceled, float value, float velocity) {
                if (canceled) {
                    animatorListener.onAnimationCancel(null);
                } else {
                    animatorListener.onAnimationEnd(null);
                }
            }
        };
        this.mTranslationAndScaleEndListener = onAnimationEndListener;
        this.mTranslationAndScaleSpringAnimation.addEndListener(onAnimationEndListener);
        return true;
    }

    private void saveActivityContextToGetMultiWindowInfo(Context context) {
        Activity activityContextToActivity = UIUtil.contextToActivity(context);
        if (activityContextToActivity != null) {
            this.mActivityWeakReference = new WeakReference<>(activityContextToActivity);
        }
    }

    private void setContentViewLocal(View view) {
        if (this.mIsShowInDialogFragment) {
            super.setContentView(view);
        } else {
            ensureDraggableContentLayout();
            this.mDraggableConstraintLayout.removeContentView();
            this.mDraggableConstraintLayout.addContentView(view);
            super.setContentView(this.mDraggableConstraintLayout);
        }
        this.mContentView = view;
    }

    private void setDefaultSpringStartEndValue() {
        this.mStartValueOfTranslateAnimation = 0.0f;
        this.mEndValueOfTranslateAnimation = 100.0f;
    }

    private void setFocusChangeFalseIfHasnotEdittext() {
        if (this.mFocusChange == null && hasEditText((ViewGroup) getWindow().getDecorView().getRootView())) {
            this.mFocusChange = Boolean.TRUE;
        }
    }

    private void setNavigation() {
        if (!this.mIsGestureNavigation) {
            getWindow().setDecorFitsSystemWindows(false);
            getWindow().setNavigationBarContrastEnforced(false);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(getWindow().getDecorView().getSystemUiVisibility() | 512);
            getWindow().setNavigationBarContrastEnforced(false);
            setNavigationBarColor(0);
        }
    }

    private void setNavigationBarColor(int color) {
        if (unNeedNavigationCustomView()) {
            getWindow().setNavigationBarColor(color);
        } else {
            getWindow().setNavigationBarColor(0);
        }
        setNavigationCustomViewColor(color);
        COUILog.d(TAG, "setNavigationBarColor color: " + Integer.toHexString(color));
    }


    public void setNavigationBarColorAlpha(float alpha) {
        int alphaInt = (int) (alpha * this.mOutsideViewBackgroundAlpha);
        if (alphaInt > 0) {
            setNavigationBarColor(Color.argb(alphaInt, 0, 0, 0));
        } else {
            setNavigationBarColor(0);
            getWindow().setNavigationBarContrastEnforced(false);
        }
    }

    private void setNavigationCustomViewColor(int color) {
        View view;
        if (unNeedNavigationCustomView() || (view = this.mNavigationCustomView) == null) {
            return;
        }
        if (this.mCouiPanelEdgeToEdgeEnable) {
            view.setBackground(getNavigationDrawable(color));
        } else {
            view.setBackgroundColor(color);
        }
    }

    private void setNavigationCustomViewHeight(WindowInsets windowInsets) {
        if (unNeedNavigationCustomView() || windowInsets == null || this.mNavigationCustomView == null) {
            return;
        }
        int navBarHeight = windowInsets.getInsets(WindowInsets.Type.navigationBars()).bottom;
        this.mNavigationCustomView.getLayoutParams().height = Math.max(0, navBarHeight);
    }

    private void setPanelHeight() {
        COUIPanelContentLayout cOUIPanelContentLayout = this.mDraggableConstraintLayout;
        if (cOUIPanelContentLayout != null) {
            ViewGroup.LayoutParams layoutParams = cOUIPanelContentLayout.getLayoutParams();
            int height = this.mPanelHeight;
            if (height != 0) {
                layoutParams.height = height;
            }
            this.mDraggableConstraintLayout.setLayoutParams(layoutParams);
        }
        WindowInsets windowInsets = this.mApplyWindowInsets;
        if (windowInsets != null) {
            initMaxHeight(windowInsets);
        }
    }

    private void setPanelWidth() {
        COUIPanelPercentFrameLayout cOUIPanelPercentFrameLayout = this.mDesignBottomSheetFrameLayout;
        if (cOUIPanelPercentFrameLayout != null) {
            ViewGroup.LayoutParams layoutParams = cOUIPanelPercentFrameLayout.getLayoutParams();
            int width = this.mPanelWidth;
            if (width != 0) {
                layoutParams.width = width;
            }
            this.mDesignBottomSheetFrameLayout.setLayoutParams(layoutParams);
        }
    }


    public void setPulledUpViewPaddingBottom(int paddingBottom) {
        COUIPanelPercentFrameLayout cOUIPanelPercentFrameLayout;
        if (this.mPulledUpView == null || (cOUIPanelPercentFrameLayout = this.mDesignBottomSheetFrameLayout) == null) {
            return;
        }
        if (TextUtils.equals(cOUIPanelPercentFrameLayout.getClass().getSimpleName(), this.mPulledUpView.getClass().getSimpleName())) {
            paddingBottom += this.mPanelPaddingBottom;
        }
        View view = this.mPulledUpView;
        view.setPadding(view.getPaddingLeft(), this.mPulledUpView.getPaddingTop(), this.mPulledUpView.getPaddingRight(), paddingBottom);
    }

    private void setSpringStartPosition(float startValue) {
        this.mAppearSpringAnim.setStartValue(startValue);
    }

    private void setStatusBarTransparentAndFont(Window window) {
        if (window == null) {
            return;
        }
        View decorView = window.getDecorView();
        int systemUiVisibility = decorView.getSystemUiVisibility();
        int flags = systemUiVisibility | Segment.SHARE_MINIMUM;
        window.setStatusBarColor(0);
        window.addFlags(Integer.MIN_VALUE);
        decorView.setSystemUiVisibility(COUIDarkModeUtil.isNightMode(getContext()) ? flags & (-8209) : systemUiVisibility | 1280);
    }


    public boolean shouldHandlePanelUpdateNavBarColor() {
        if (this.mIsHandlePanel) {
            return COUIPanelMultiWindowUtils.isNormalLandScreen(getContext(), this.mConfiguration);
        }
        return false;
    }


    public boolean shouldUpdatePanelMarginBottom(WindowInsets windowInsets) {
        return windowInsets == null || this.mApplyWindowInsets == null || this.mDesignBottomSheetFrameLayout == null || windowInsets.getInsets(WindowInsets.Type.navigationBars()).bottom != this.mApplyWindowInsets.getInsets(WindowInsets.Type.navigationBars()).bottom || this.mPanelRatio != this.mDesignBottomSheetFrameLayout.getRatio();
    }

    private void snapToTop() {
        COUIPanelPercentFrameLayout cOUIPanelPercentFrameLayout = this.mDesignBottomSheetFrameLayout;
        if (cOUIPanelPercentFrameLayout != null) {
            this.mSnapStartBottom = cOUIPanelPercentFrameLayout.getBottom();
        }
        this.mIsAppearSpringAnimStared = true;
        this.mAppearSpringAnim.start();
    }

    private void startListeningForBackCallbacks(View view) {
        if (Build.VERSION.SDK_INT >= 33) {
            if (view.isAttachedToWindow()) {
                registerBackCallback(view);
            } else {
                view.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                    @Override
                    public void onViewAttachedToWindow(View view2) {
                        COUIBottomSheetDialog.this.registerBackCallback(view2);
                    }

                    @Override
                    public void onViewDetachedFromWindow(View view2) {
                        view2.removeOnAttachStateChangeListener(this);
                        if (COUIBottomSheetDialog.this.mDesignBottomSheetFrameLayout != null) {
                            COUIBottomSheetDialog cOUIBottomSheetDialog = COUIBottomSheetDialog.this;
                            cOUIBottomSheetDialog.stopListeningForBackCallbacks(cOUIBottomSheetDialog.mDesignBottomSheetFrameLayout);
                        }
                    }
                });
            }
        }
    }

    private void startReleaseAnim(Animator.AnimatorListener animatorListener) {
        if (animatorListener != null) {
            this.mPanelViewTranslationAnimationSet.addListener(animatorListener);
        }
        this.mPanelViewTranslationAnimationSet.start();
    }

    private void startReleaseAnimInTinyScreen(float startY, float endY, float durationMs, Animator.AnimatorListener animatorListener) {
        this.mPanelViewTranslationAnimationSet.playTogether(createPanelTranslateAnimation(startY, endY, this.mTranslateHidingDuration, new COUIOutEaseInterpolator()), createOutsideAlphaAnimation(false, DEFAULT_ALPHA_HIDING_ANIMATOR_DURATION, new COUIEaseInterpolator()));
        startReleaseAnim(animatorListener);
    }

    private void startShowingAnim(Animator.AnimatorListener animatorListener) {
        if (animatorListener != null) {
            this.mPanelViewTranslationAnimationSet.addListener(animatorListener);
        }
        this.mPanelViewTranslationAnimationSet.start();
    }

    private void startShowingAnimInTinyScreen(int extraOffset, Animator.AnimatorListener animatorListener) {
        this.mPanelViewTranslationAnimationSet.playTogether(createOutsideAlphaAnimation(true, SHOW_HEIGHT_ANIM_DURATION_IN_TINY_SCREEN, (PathInterpolator) OUTSIDE_ALPHA_ANIM_INTERPOLATOR));
        setSpringStartPosition(this.mFirstShowCollapsed ? this.mPeekHeight : getContentViewHeightWithMargins() + extraOffset);
        snapToTop();
        startShowingAnim(animatorListener);
    }

    private void stopCurrentRunningViewTranslationAnim() {
        AnimatorSet animatorSet = this.mPanelViewTranslationAnimationSet;
        if (animatorSet != null && animatorSet.isRunning()) {
            this.mIsInterruptingAnim = true;
            this.mPanelViewTranslationAnimationSet.end();
        }
        COUISpringAnimation cOUISpringAnimation = this.mTranslationAndScaleSpringAnimation;
        if (cOUISpringAnimation != null && cOUISpringAnimation.isRunning()) {
            this.mTranslationAndScaleSpringAnimation.cancel();
        }
        COUISpringAnimation cOUISpringAnimation2 = this.mAlphaSpringAnimation;
        if (cOUISpringAnimation2 != null && cOUISpringAnimation2.isRunning()) {
            this.mAlphaSpringAnimation.cancel();
        }
        if (this.mIsInTinyScreen && this.mIsAppearSpringAnimStared) {
            this.mAppearSpringAnim.cancel();
        }
    }

    private void stopFeedbackAnimation() {
        Spring spring = this.mDisableFastCloseFeedbackSpring;
        if (spring == null || spring.getVelocity() == 0.0d) {
            return;
        }
        this.mDisableFastCloseFeedbackSpring.setAtRest();
        this.mDisableFastCloseFeedbackSpring = null;
    }


    public void stopListeningForBackCallbacks(View view) {
        OnBackInvokedDispatcher onBackInvokedDispatcherFindOnBackInvokedDispatcher;
        OnBackInvokedCallback onBackInvokedCallback;
        if (Build.VERSION.SDK_INT < 33 || (onBackInvokedDispatcherFindOnBackInvokedDispatcher = view.findOnBackInvokedDispatcher()) == null || (onBackInvokedCallback = this.mOnBackInvokedCallback) == null) {
            return;
        }
        onBackInvokedDispatcherFindOnBackInvokedDispatcher.unregisterOnBackInvokedCallback(onBackInvokedCallback);
        this.mOnBackInvokedCallback = null;
    }


    public void superDismiss() {
        if (DEBUG) {
            Log.d(TAG, "superDismiss");
        }
        try {
            this.mIsRevertAnimationFromSettlingAnimation = false;
            // Leapy added 2026-07-30: restore decoded OPPO window-inset cleanup
            // before removing a panel dismissed by a downward swipe.
            adjustResize(this.mApplyWindowInsets, true);
            super.dismiss();
            OnAnimationListener onAnimationListener = this.mAnimationListener;
            if (onAnimationListener != null) {
                onAnimationListener.onDismissAnimationEnd();
            }
            this.mIsExecutingDismissAnim = false;
        } catch (Exception e2) {
            Log.e(TAG, e2.getMessage(), e2);
        }
    }


    public void translateUpdate(float translationY) {
        COUIPanelPercentFrameLayout cOUIPanelPercentFrameLayout = this.mDesignBottomSheetFrameLayout;
        if (cOUIPanelPercentFrameLayout != null) {
            cOUIPanelPercentFrameLayout.setTranslationY(translationY);
            if (!this.mIsInterruptingAnim) {
                this.mCurrentParentViewTranslationY = translationY;
            }
            this.mIsInterruptingAnim = false;
        }
    }

    private boolean unNeedNavigationCustomView() {
        return this.mIsGestureNavigation || this.mDesignBottomSheetFrameLayout == null;
    }

    private void updateBottomSheetCenterVertical() {
        View view = this.mCoordinatorLayout;
        if (view == null) {
            Log.w(TAG, "updateBottomSheetCenterVertical: directly return for mCoordinatorLayout is null");
            return;
        }
        if (this.mDesignBottomSheetFrameLayout == null) {
            Log.i(TAG, "updateBottomSheetCenterVertical: directly return for mDesignBottomSheetFrameLayout is null");
            return;
        }
        int measuredHeight = view.getMeasuredHeight();
        ViewGroup.LayoutParams layoutParams = this.mDesignBottomSheetFrameLayout.getLayoutParams();
        int bottomMargin = layoutParams instanceof ViewGroup.MarginLayoutParams ? ((ViewGroup.MarginLayoutParams) layoutParams).bottomMargin : 0;
        int iMax = (int) Math.max(0.0f, ((measuredHeight - bottomMargin) / this.mDesignBottomSheetFrameLayout.getRatio()) - (this.mDesignBottomSheetFrameLayout.getHeight() / this.mDesignBottomSheetFrameLayout.getRatio()));
        if (this.mDesignBottomSheetFrameLayout.getBottom() + iMax <= measuredHeight) {
            this.mDesignBottomSheetFrameLayout.setY(iMax);
        }
    }

    private void updateFitToContents() {
        if (this.mIsHandlePanel) {
            COUIPanelPercentFrameLayout cOUIPanelPercentFrameLayout = this.mDesignBottomSheetFrameLayout;
            if (cOUIPanelPercentFrameLayout == null) {
                Log.e(TAG, "updateFitToContents: mDesignBottomSheetFrameLayout is null");
            } else {
                COUIBottomSheetBehavior.from(cOUIPanelPercentFrameLayout).setFitToContents(COUIPanelMultiWindowUtils.isNormalLandScreen(getContext(), this.mConfiguration));
            }
        }
    }

    private void updateListeningForBackCallbacks() {
        if (this.mCancelable) {
            startListeningForBackCallbacks(this.mDesignBottomSheetFrameLayout);
        } else {
            stopListeningForBackCallbacks(this.mDesignBottomSheetFrameLayout);
        }
    }

    private void updatePaddingBottom() {
        COUIPanelPercentFrameLayout cOUIPanelPercentFrameLayout = this.mDesignBottomSheetFrameLayout;
        if (cOUIPanelPercentFrameLayout != null) {
            this.mPanelPaddingBottom = cOUIPanelPercentFrameLayout.getRatio() == 2.0f ? 0 : this.mDefaultPaddingBottom;
            COUIPanelPercentFrameLayout cOUIPanelPercentFrameLayout2 = this.mDesignBottomSheetFrameLayout;
            cOUIPanelPercentFrameLayout2.setPaddingRelative(cOUIPanelPercentFrameLayout2.getPaddingStart(), this.mDesignBottomSheetFrameLayout.getPaddingTop(), this.mDesignBottomSheetFrameLayout.getPaddingEnd(), this.mPanelPaddingBottom);
            ((COUIBottomSheetBehavior) getBehavior()).setPanelPaddingBottom(this.mPanelPaddingBottom);
        }
    }


    public void updatePanelMarginBottom(Configuration configuration, WindowInsets windowInsets) {
        if (windowInsets == null || configuration == null || this.mDesignBottomSheetFrameLayout == null) {
            return;
        }
        int panelMarginBottom = COUIPanelMultiWindowUtils.getPanelMarginBottom(getContext(), configuration, windowInsets, this.mIsHandlePanel, this.mCouiPanelEdgeToEdgeEnable);
        CoordinatorLayout.LayoutParams fVar = (CoordinatorLayout.LayoutParams) this.mDesignBottomSheetFrameLayout.getLayoutParams();
        if (((ViewGroup.MarginLayoutParams) fVar).bottomMargin != panelMarginBottom) {
            ((ViewGroup.MarginLayoutParams) fVar).bottomMargin = panelMarginBottom;
        }
    }

    public boolean canPullUp() {
        return this.mCanPullUp;
    }

    public void delPreferWidth() {
        restoreScreenWidth();
        this.mPreferWidth = -1;
        this.mOriginWidth = -1;
        Log.d(TAG, "delPreferWidth");
    }

    @Override
    public void dismiss() {
        stopFeedbackAnimation();
        dismiss(true);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        COUIPanelContentLayout cOUIPanelContentLayout;
        int action = motionEvent.getAction();
        if ((action == 1 || action == 3) && (cOUIPanelContentLayout = this.mDraggableConstraintLayout) != null && cOUIPanelContentLayout.mIsTurnOnAnim) {
            cOUIPanelContentLayout.mIsTurnOnAnim = false;
            cOUIPanelContentLayout.dragBgEndAnim();
        }
        return super.dispatchTouchEvent(motionEvent);
    }

    public COUIPanelAdjustResizeHelper getAdjustResizeHelper() {
        if (this.mAdjustResizeHelper == null) {
            this.mAdjustResizeHelper = new COUIPanelAdjustResizeHelper();
        }
        return this.mAdjustResizeHelper;
    }

    public boolean getCanPerformHapticFeedback() {
        return this.mCanPerformHapticFeedback;
    }

    public Button getCenterButton() {
        if (getWindow() != null) {
            return (Button) getWindow().findViewById(android.R.id.button3);
        }
        return null;
    }

    public View getContentView() {
        return this.mContentView;
    }

    public int getDialogHeight() {
        COUIPanelPercentFrameLayout cOUIPanelPercentFrameLayout = this.mDesignBottomSheetFrameLayout;
        if (cOUIPanelPercentFrameLayout != null) {
            return cOUIPanelPercentFrameLayout.getHeight();
        }
        return 0;
    }

    public int getDialogMaxHeight() {
        View view = this.mCoordinatorLayout;
        if (view != null) {
            return view.getMeasuredHeight();
        }
        return 0;
    }

    public COUIPanelContentLayout getDragableLinearLayout() {
        return this.mDraggableConstraintLayout;
    }

    public boolean getIsHandlePanel() {
        return this.mIsHandlePanel;
    }

    public boolean getIsInWindowFloatingMode() {
        return this.mIsInWindowFloatingMode;
    }

    public Button getLeftButton() {
        if (getWindow() != null) {
            return (Button) getWindow().findViewById(android.R.id.button2);
        }
        return null;
    }

    public float getOutsideViewAlpha(float slideOffset) {
        return !this.mIsInTinyScreen ? slideOffset : Math.max(0.0f, slideOffset - 0.5f) * 2.0f;
    }

    public int getPeekHeight() {
        return this.mPeekHeight;
    }

    public Button getRightButton() {
        if (getWindow() != null) {
            return (Button) getWindow().findViewById(android.R.id.button1);
        }
        return null;
    }

    @Override
    public void hide() {
        COUIPanelContentLayout cOUIPanelContentLayout;
        if (!this.mIsShowInDialogFragment || (cOUIPanelContentLayout = this.mDraggableConstraintLayout) == null || cOUIPanelContentLayout.findFocus() == null) {
            super.hide();
        }
    }

    public void hideDragView() {
        COUIPanelBarView cOUIPanelBarView = this.mPanelBarView;
        if (cOUIPanelBarView != null) {
            cOUIPanelBarView.setVisibility(4);
        }
        COUIPanelContentLayout cOUIPanelContentLayout = this.mDraggableConstraintLayout;
        if (cOUIPanelContentLayout == null || cOUIPanelContentLayout.getDrawLayout() == null) {
            return;
        }
        setHideDragViewHeight();
        this.mDraggableConstraintLayout.getDrawLayout().setVisibility(4);
        if (this.mDraggableConstraintLayout.getDragBgView() != null) {
            this.mDraggableConstraintLayout.getDragBgView().setVisibility(8);
        }
    }

    public boolean isFirstShowCollapsed() {
        return this.mFirstShowCollapsed;
    }

    public boolean isPanelHeightChangeAnimRunning() {
        return ((COUIBottomSheetBehavior) getBehavior()).isPanelHeightChangeAnimRunning();
    }

    public boolean isSkipCollapsed() {
        return this.mSkipCollapsed;
    }

    private void onAppearAnimationEnd(boolean canceled, float value, float velocity) {
        this.mIsAppearSpringAnimStared = false;
        COUIPanelPercentFrameLayout cOUIPanelPercentFrameLayout = this.mDesignBottomSheetFrameLayout;
        if (cOUIPanelPercentFrameLayout != null && this.mSnapStartBottom != -1) {
            cOUIPanelPercentFrameLayout.layout(cOUIPanelPercentFrameLayout.getLeft(), this.mDesignBottomSheetFrameLayout.getTop(), this.mDesignBottomSheetFrameLayout.getRight(), this.mSnapStartBottom);
        }
        this.mSnapStartBottom = -1;
        BottomSheetDialogAnimatorListener bottomSheetDialogAnimatorListener = this.mBottomSheetDialogAnimatorListener;
        if (bottomSheetDialogAnimatorListener != null) {
            bottomSheetDialogAnimatorListener.onBottomSheetDialogExpanded();
        }
    }

    private void onAppearAnimationUpdate(float value, float velocity) {
        COUIPanelPercentFrameLayout cOUIPanelPercentFrameLayout = this.mDesignBottomSheetFrameLayout;
        if (cOUIPanelPercentFrameLayout == null || this.mSnapStartBottom == -1) {
            return;
        }
        if (value < 0.0f) {
            cOUIPanelPercentFrameLayout.layout(cOUIPanelPercentFrameLayout.getLeft(), this.mDesignBottomSheetFrameLayout.getTop(), this.mDesignBottomSheetFrameLayout.getRight(), (int) (this.mSnapStartBottom - value));
        }
        this.mDesignBottomSheetFrameLayout.setTranslationY(value);
        if (!this.mIsInterruptingAnim) {
            this.mCurrentParentViewTranslationY = this.mDesignBottomSheetFrameLayout.getTranslationY();
        }
        this.mIsInterruptingAnim = false;
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        enforceChangeScreenWidth();
        refreshParams();
        resetWindowImeAnimFlags();
        setStatusBarTransparentAndFont(getWindow());
        registerPreDrawListener();
        registerApplicationConfigChangeListener();
        registerBehaviorPullUpListener();
        registerPullUpToDismissPanelListener();
        initWindowInsetsListener();
        setNavigation();
        if (this.mDesignBottomSheetFrameLayout != null) {
            updateListeningForBackCallbacks();
        }
        if (this.mIsExecutingDismissAnim) {
            return;
        }
        updatePanelMarginBottom(this.mConfiguration, this.mApplyWindowInsets);
    }

    @Override
    public void onBackPressed() {
        WeakReference<Activity> weakReference = this.mActivityWeakReference;
        if (weakReference != null && weakReference.get() != null && this.mIsExecutingDismissAnim) {
            this.mActivityWeakReference.get().onBackPressed();
        }
        super.onBackPressed();
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.mConfiguration = getContext().getResources().getConfiguration();
        int identifier = getContext().getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (identifier > 0) {
            this.mStatusBarHeight = getContext().getResources().getDimensionPixelSize(identifier);
        }
        if (this.mIsInTinyScreen) {
            initPhysics();
        }
        initBehavior();
        initWindow();
        initDraggableConstraintLayoutSize();
        if (this.mFrameRate && COUIVersionUtil.checkOPlusViewSubSDK(SDK_VERSION_FOR_COMPUTE, SDK_SUB_VERSION_FOR_COMPUTE)) {
            this.mADFRFeatureType = DynamicFrameRateManager.getDynamicFrameRateType();
            this.mIsVSdk = true;
        }
    }

    @Override
    public void onDetachedFromWindow() {
        releaseResizeHelper();
        releaseApplyWindowInsetsListener();
        cancelAnim(this.mPanelViewTranslationAnimationSet);
        cancelAnim(this.mTranslationAndScaleSpringAnimation);
        cancelAnim(this.mAlphaSpringAnimation);
        releaseApplicationConfigChangeListener();
        releaseBehaviorPullUpListener();
        releasePullUpToDismissPanelListener();
        restoreScreenWidth();
        super.onDetachedFromWindow();
    }

    @Override
    public void onRestoreInstanceState(Bundle bundle) {
        this.mFocusChange = Boolean.valueOf(bundle.getBoolean(STATE_FOCUS_CHANGES, getFocusChange()));
        this.mLastStaticState = bundle.getInt(STATE_LAST_STATIC_CHANGES, 3);
        super.onRestoreInstanceState(bundle);
    }

    @Override
    public Bundle onSaveInstanceState() {
        Bundle bundleOnSaveInstanceState = super.onSaveInstanceState();
        bundleOnSaveInstanceState.putBoolean(STATE_FOCUS_CHANGES, getFocusChange());
        bundleOnSaveInstanceState.putInt(STATE_LAST_STATIC_CHANGES, this.mLastStaticState);
        return bundleOnSaveInstanceState;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            setFocusChangeFalseIfHasnotEdittext();
        }
        super.onWindowFocusChanged(hasFocus);
    }

    public void refresh() {
        if (this.mDraggableConstraintLayout == null) {
            return;
        }
        TypedArray typedArrayObtainStyledAttributes = getContext().getTheme().obtainStyledAttributes(null, com.coui.appcompat.R.styleable.COUIBottomSheetDialog, 0, com.coui.appcompat.R.style.DefaultBottomSheetDialog);
        this.mPanelDragViewDrawable = getTypedArrayDrawable(typedArrayObtainStyledAttributes, com.coui.appcompat.R.styleable.COUIBottomSheetDialog_panelDragViewIcon, com.coui.appcompat.R.drawable.coui_panel_drag_view);
        this.mPanelDragViewDrawableTintColor = typedArrayObtainStyledAttributes.getColor(com.coui.appcompat.R.styleable.COUIBottomSheetDialog_panelDragViewTintColor, COUIContextUtil.getAttrColor(getContext(), com.coui.appcompat.R.attr.couiColorControls));
        this.mPanelBackground = getTypedArrayDrawable(typedArrayObtainStyledAttributes, com.coui.appcompat.R.styleable.COUIBottomSheetDialog_panelBackground, com.coui.appcompat.R.drawable.coui_default_panel_bg_without_shadow);
        this.mPanelBackgroundTintColor = typedArrayObtainStyledAttributes.getColor(com.coui.appcompat.R.styleable.COUIBottomSheetDialog_panelBackgroundTintColor, COUIContextUtil.getAttrColor(getContext(), com.coui.appcompat.R.attr.couiColorSurface));
        typedArrayObtainStyledAttributes.recycle();
        Drawable drawable = this.mPanelDragViewDrawable;
        if (drawable != null && this.mDraggableConstraintLayout != null) {
            drawable.setTint(this.mPanelDragViewDrawableTintColor);
            this.mDraggableConstraintLayout.setDragViewDrawable(this.mPanelDragViewDrawable);
            this.mDraggableConstraintLayout.refresh();
        }
        if (this.mPanelBackground == null || this.mDraggableConstraintLayout == null) {
            return;
        }
        if (getWindow() != null && !COUINavigationBarUtil.isGestureNavigation(getContext())) {
            setNavigationBarColor(getNavColor());
        }
        this.mPanelBackground.setTint(this.mPanelBackgroundTintColor);
        this.mDraggableConstraintLayout.setBackground(this.mIsShowInDialogFragment ? this.mPanelBackground : null);
        COUIPanelPercentFrameLayout cOUIPanelPercentFrameLayout = this.mDesignBottomSheetFrameLayout;
        if (cOUIPanelPercentFrameLayout != null) {
            cOUIPanelPercentFrameLayout.setBackground(this.mPanelBackground);
        }
    }

    public void setAnchorView(View view) {
        if (view != null) {
            Log.e(TAG, "setAnchorView: ---------");
            this.mAnchorView = view;
            getBehavior().setDraggable(false);
        }
    }

    public void setAnimationListener(OnAnimationListener onAnimationListener) {
        this.mAnimationListener = onAnimationListener;
    }

    public void setBottomButtonBar(boolean visible, String str, View.OnClickListener onClickListener, String str2, View.OnClickListener onClickListener2, String str3, View.OnClickListener onClickListener3) {
        ensureDraggableContentLayout();
        this.mDraggableConstraintLayout.setUpBottomBar(visible, str, onClickListener, str2, onClickListener2, str3, onClickListener3);
    }

    public void setBottomSheetDialogAnimatorListener(BottomSheetDialogAnimatorListener bottomSheetDialogAnimatorListener) {
        this.mBottomSheetDialogAnimatorListener = bottomSheetDialogAnimatorListener;
    }

    public void setCanPerformHapticFeedback(boolean enable) {
        this.mCanPerformHapticFeedback = enable;
    }

    public void setCanPullUp(boolean canPullUp) {
        if (this.mCanPullUp != canPullUp) {
            this.mCanPullUp = canPullUp;
            if (getBehavior() instanceof COUIBottomSheetBehavior) {
                this.mPanelPullUpListener = this.mCanPullUp ? getPanelPullUpListener() : null;
                ((COUIBottomSheetBehavior) getBehavior()).setPullUpListener(this.mPanelPullUpListener);
            }
        }
    }

    @Override
    public void setCancelable(boolean cancelable) {
        super.setCancelable(cancelable);
        if (this.mCancelable != cancelable) {
            this.mCancelable = cancelable;
            if (this.mDesignBottomSheetFrameLayout == null || getWindow() == null) {
                return;
            }
            updateListeningForBackCallbacks();
        }
    }

    @Override
    public void setCanceledOnTouchOutside(boolean cancel) {
        super.setCanceledOnTouchOutside(cancel);
        if (cancel && !this.mCancelable) {
            this.mCancelable = true;
            if (this.mDesignBottomSheetFrameLayout != null && getWindow() != null) {
                startListeningForBackCallbacks(this.mDesignBottomSheetFrameLayout);
            }
        }
        this.mCanceledOnTouchOutside = cancel;
    }

    public void setCenterButton(String str, View.OnClickListener onClickListener) {
        ensureDraggableContentLayout();
        this.mDraggableConstraintLayout.setCenterButton(str, onClickListener);
    }

    @Override
    public void setContentView(int layoutResId) {
        setContentView(getLayoutInflater().inflate(layoutResId, (ViewGroup) null));
    }

    public void setCouiPanelEdgeToEdgeEnable(boolean enable) {
        if (this.mCouiPanelEdgeToEdgeEnable != enable) {
            this.mCouiPanelEdgeToEdgeEnable = enable;
            initPeekHeight();
            getAdjustResizeHelper().setCouiPanelEdgeToEdgeEnable(this.mCouiPanelEdgeToEdgeEnable);
        }
    }

    public void setDialogOffsetListener(DialogOffsetListener dialogOffsetListener) {
        this.mDialogOffsetListener = dialogOffsetListener;
    }

    public void setDisableSubExpand(boolean disable) {
        this.mDisableSubExpand = disable;
    }

    public void setDragableLinearLayout(COUIPanelContentLayout cOUIPanelContentLayout) {
        setDragableLinearLayout(cOUIPanelContentLayout, false);
    }

    public void setDraggable(boolean draggable) {
        if (this.mIsDraggable != draggable) {
            this.mIsDraggable = draggable;
            getBehavior().setDraggable(this.mIsDraggable);
        }
    }

    @Deprecated
    public void setExecuteNavColorAnimAfterDismiss(boolean enable) {
        this.mIsExecuteNavColorAnimAfterDismiss = enable;
    }

    @Deprecated
    public void setFinalNavColorAfterDismiss(int color) {
        this.mFinalNavColorAfterDismiss = color;
    }

    public void setFirstShowCollapsed(boolean collapsed) {
        this.mFirstShowCollapsed = collapsed;
    }

    public void setFollowWindowChange(boolean follow) {
        this.mFocusChange = Boolean.valueOf(follow);
    }

    public void setFrameRate(boolean enable) {
        this.mFrameRate = enable;
    }

    public void setGlobalDrag(boolean enable) {
        this.mGlobalDrag = enable;
    }

    public void setHandleViewHasPressAnim(boolean enable) {
        if (this.mHandleViewHasPressAnim != enable) {
            this.mHandleViewHasPressAnim = enable;
            COUIPanelContentLayout cOUIPanelContentLayout = this.mDraggableConstraintLayout;
            if (cOUIPanelContentLayout == null) {
                return;
            }
            if (enable) {
                cOUIPanelContentLayout.setDragViewPressAnim(true);
            } else {
                cOUIPanelContentLayout.removeDragViewPressAnim();
            }
        }
    }

    public void setHeight(int height) {
        this.mPanelHeight = height;
        setPanelHeight();
    }

    public void setHeightChangeAnim(boolean enable) {
        ((COUIBottomSheetBehavior) getBehavior()).setHeightChangeAnim(enable);
    }

    public void setHideDragViewHeight(int height) {
        COUIPanelContentLayout cOUIPanelContentLayout;
        this.mHideDragViewHeight = height;
        if (this.mIsHandlePanel || (cOUIPanelContentLayout = this.mDraggableConstraintLayout) == null || cOUIPanelContentLayout.getDrawLayout() == null) {
            return;
        }
        setHideDragViewHeight();
    }

    public void setIsHandlePanel(boolean handlePanel) {
        if (this.mIsHandlePanel != handlePanel) {
            this.mIsHandlePanel = handlePanel;
            if (this.mDraggableConstraintLayout == null) {
                return;
            }
            if (handlePanel) {
                showDragView();
            } else {
                hideDragView();
            }
        }
    }

    public void setIsInTinyScreen(boolean tinyScreen, boolean fullScreen) {
        this.mIsInTinyScreen = tinyScreen;
        this.mIsFullScreenInTinyScreen = fullScreen;
    }

    public void setIsInWindowFloatingMode(boolean floating) {
        this.mIsInWindowFloatingMode = floating;
    }

    public void setIsNeedOutsideViewAnim(boolean needAnim) {
        this.mIsNeedOutsideViewAnim = needAnim;
    }

    public void setIsShowInMaxHeight(boolean maxHeight) {
        this.mIsShowInMaxHeight = maxHeight;
        int layoutHeight = maxHeight ? -1 : -2;
        COUIPanelContentLayout cOUIPanelContentLayout = this.mDraggableConstraintLayout;
        if (cOUIPanelContentLayout != null) {
            cOUIPanelContentLayout.setLayoutAtMaxHeight(maxHeight);
        }
        COUIPanelPercentFrameLayout cOUIPanelPercentFrameLayout = this.mDesignBottomSheetFrameLayout;
        if (cOUIPanelPercentFrameLayout != null) {
            ViewGroup.LayoutParams layoutParams = cOUIPanelPercentFrameLayout.getLayoutParams();
            layoutParams.height = layoutHeight;
            this.mDesignBottomSheetFrameLayout.setLayoutParams(layoutParams);
            ((COUIBottomSheetBehavior) getBehavior()).setLayoutAtMaxHeight(this.mIsShowInMaxHeight);
        }
    }

    public void setLeftButton(String str, View.OnClickListener onClickListener) {
        ensureDraggableContentLayout();
        this.mDraggableConstraintLayout.setLeftButton(str, onClickListener);
    }

    public void setNavColor(int color) {
        this.mNavColor = color;
        if (getWindow() != null) {
            setNavigationBarColor(getNavColor());
        }
    }

    public void setOnBackInvokedLocalListener(OnBackInvokedLocalListener onBackInvokedLocalListener) {
        this.mOnBackInvokedLocalListener = onBackInvokedLocalListener;
    }

    public void setOnPanelHeightChangeAnimListener(COUIBottomSheetBehavior.OnPanelHeightChangeAnimListener onPanelHeightChangeAnimListener) {
        ((COUIBottomSheetBehavior) getBehavior()).setOnPanelHeightChangeAnimListener(onPanelHeightChangeAnimListener);
    }

    public void setOutSideViewTouchListener(View.OnTouchListener onTouchListener) {
        if (this.mOutsideView == null) {
            this.mOutsideView = findViewById(com.coui.appcompat.R.id.panel_outside);
        }
        this.mOutSideViewTouchListener = onTouchListener;
        View view = this.mOutsideView;
        if (view != null) {
            view.setOnTouchListener(onTouchListener);
        }
    }

    public void setOutsideMaskColor(int color) {
        View view = this.mOutsideView;
        if (view != null) {
            view.setBackgroundColor(color);
        }
    }

    public void setPanelBackground(Drawable drawable) {
        if (this.mDesignBottomSheetFrameLayout == null || drawable == null || this.mPanelBackground == drawable) {
            return;
        }
        this.mPanelBackground = drawable;
        COUIPanelContentLayout cOUIPanelContentLayout = this.mDraggableConstraintLayout;
        if (cOUIPanelContentLayout != null) {
            if (!this.mIsShowInDialogFragment) {
                drawable = null;
            }
            cOUIPanelContentLayout.setBackground(drawable);
        }
        this.mDesignBottomSheetFrameLayout.setBackground(this.mPanelBackground);
    }

    public void setPanelBackgroundTintColor(int color) {
        if (this.mDesignBottomSheetFrameLayout == null || this.mPanelBackground == null || this.mPanelBackgroundTintColor == color) {
            return;
        }
        this.mPanelBackgroundTintColor = color;
        if (getWindow() != null && !COUINavigationBarUtil.isGestureNavigation(getContext())) {
            setNavigationBarColor(getNavColor());
        }
        this.mPanelBackground.setTint(this.mPanelBackgroundTintColor);
        COUIPanelContentLayout cOUIPanelContentLayout = this.mDraggableConstraintLayout;
        if (cOUIPanelContentLayout != null) {
            cOUIPanelContentLayout.setBackground(this.mIsShowInDialogFragment ? this.mPanelBackground : null);
        }
        this.mDesignBottomSheetFrameLayout.setBackground(this.mPanelBackground);
    }

    public void setPanelBarViewColor(int color) {
        COUIPanelBarView cOUIPanelBarView = this.mPanelBarView;
        if (cOUIPanelBarView != null) {
            cOUIPanelBarView.setBarColor(color);
        }
    }

    public void setPanelDismissTranslateDuration(float duration) {
        this.mTranslateHidingDuration = duration;
    }

    public void setPanelDragViewDrawable(Drawable drawable) {
        COUIPanelContentLayout cOUIPanelContentLayout = this.mDraggableConstraintLayout;
        if (cOUIPanelContentLayout == null || drawable == null || this.mPanelDragViewDrawable == drawable) {
            return;
        }
        this.mPanelDragViewDrawable = drawable;
        cOUIPanelContentLayout.setDragViewDrawable(drawable);
    }

    public void setPanelDragViewDrawableTintColor(int color) {
        Drawable drawable;
        if (this.mDraggableConstraintLayout == null || (drawable = this.mPanelDragViewDrawable) == null || this.mPanelDragViewDrawableTintColor == color) {
            return;
        }
        this.mPanelDragViewDrawableTintColor = color;
        drawable.setTint(color);
        this.mDraggableConstraintLayout.setDragViewDrawable(this.mPanelDragViewDrawable);
    }

    public void setPeekHeight(int peekHeight) {
        this.mPeekHeight = peekHeight;
    }

    public void setPhysicsParams(float frequency, float dampingRatio) {
        this.mAppearStiffness = frequency;
        this.mAppearDampingRatio = dampingRatio;
    }

    public void setPreferWidth(int width) {
        this.mPreferWidth = width;
        Log.d(TAG, "setPreferWidth =：" + this.mPreferWidth);
    }

    public void setRegisterConfigurationChangeCallBack(boolean enable) {
        this.mRegisterConfigurationChangeCallBack = enable;
    }

    public void setRightButton(String str, View.OnClickListener onClickListener) {
        ensureDraggableContentLayout();
        this.mDraggableConstraintLayout.setRightButton(str, onClickListener);
    }

    public void setShouldRegisterWindowInsetsListener(boolean enable) {
        this.mShouldRegisterWindowInsetsListener = enable;
    }

    public void setShowInDialogFragment(boolean show) {
        this.mIsShowInDialogFragment = show;
    }

    public void setSkipCollapsed(boolean skip) {
        this.mSkipCollapsed = skip;
    }

    public void setSupportExitBlockingAnimation(boolean enable) {
        if (!COUIVersionUtil.checkOPlusViewSubSDK(SDK_VERSION_FOR_COMPUTE, SDK_SUB_VERSION_FOR_COMPUTE) || this.mSupportExitBlockingAnimation == enable) {
            return;
        }
        this.mSupportExitBlockingAnimation = enable;
        operateBlockingAnimation();
    }

    public void setUseNormalSmoothCorner(boolean enable) {
        if (RoundCornerUtil.isPathSupportSingleCorner() && RoundCornerUtil.isSmoothRoundRectOn() && COUIVersionUtil.getOSVersionCode() > 37) {
            this.mUseNormalSmoothCorner = enable;
        }
    }

    public void setWidth(int width) {
        this.mPanelWidth = width;
        setPanelWidth();
    }

    @Override
    public void show() {
        if (isShowing() && this.mIsExecutingDismissAnim && this.mSupportExitBlockingAnimation) {
            doParentViewTranslationShowingAnim(0, getPanelShowAnimListener());
        } else {
            super.show();
        }
    }

    public void showDragView() {
        COUIPanelBarView cOUIPanelBarView = this.mPanelBarView;
        if (cOUIPanelBarView != null) {
            cOUIPanelBarView.setVisibility(0);
        }
        COUIPanelContentLayout cOUIPanelContentLayout = this.mDraggableConstraintLayout;
        if (cOUIPanelContentLayout == null || cOUIPanelContentLayout.getDrawLayout() == null) {
            return;
        }
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) this.mDraggableConstraintLayout.getDrawLayout().getLayoutParams();
        marginLayoutParams.height = getContext().getResources().getDimensionPixelSize(com.coui.appcompat.R.dimen.coui_panel_drag_view_height);
        marginLayoutParams.topMargin = getContext().getResources().getDimensionPixelSize(com.coui.appcompat.R.dimen.coui_panel_drag_view_shadow_margin_top);
        this.mDraggableConstraintLayout.getDrawLayout().setLayoutParams(marginLayoutParams);
        this.mDraggableConstraintLayout.getDrawLayout().setVisibility(0);
    }


    public <T> T typeCasting(Class<T> cls, Object obj) {
        if (obj == null || !cls.isInstance(obj)) {
            return null;
        }
        return cls.cast(obj);
    }

    public boolean updateFollowHandPanelLocation() {
        if (this.mDesignBottomSheetFrameLayout == null) {
            Log.e(TAG, "update follow hand panel while config change error.");
            return false;
        }
        boolean zIsFollowHand = isFollowHand();
        this.mDesignBottomSheetFrameLayout.setHasAnchor(zIsFollowHand);
        boolean zHaveEnoughSpace = haveEnoughSpace();
        if (zIsFollowHand && zHaveEnoughSpace) {
            this.mOutsideView.setAlpha(ALPHA_TRANSPARENT);
            this.mCurrentOutsideAlpha = 0.0f;
            offsetViewTo();
            return true;
        }
        updateBottomSheetCenterVertical();
        this.mDesignBottomSheetFrameLayout.setElevation(0.0f);
        this.mOutsideView.setAlpha(1.0f);
        this.mCurrentOutsideAlpha = 1.0f;
        this.mDesignBottomSheetFrameLayout.setTranslationY(0.0f);
        this.mDesignBottomSheetFrameLayout.setTranslationX(0.0f);
        return true;
    }

    public void updateLayoutWhileConfigChange(Configuration configuration) {
        refreshColorMask();
        enforceChangeScreenWidth(configuration);
        this.mConfiguration = configuration;
        this.mIsGestureNavigation = COUINavigationBarUtil.isGestureNavigation(getContext());
        getAdjustResizeHelper().resetInnerStatus();
        if (this.mDesignBottomSheetFrameLayout != null) {
            largeScreenLimitMaxSize();
            this.mDesignBottomSheetFrameLayout.updateLayoutWhileConfigChange(configuration);
            if (!this.mIsHandlePanel || COUIPanelMultiWindowUtils.isNormalScreen(getContext(), this.mConfiguration)) {
                resetNavigationBarColor();
            }
            setNavigation();
            this.mPanelRatio = this.mDesignBottomSheetFrameLayout.getRatio();
        }
        updateFitToContents();
        updatePaddingBottom();
        initCoordinateInsets(this.mApplyWindowInsets);
        if (this.mIsExecutingDismissAnim) {
            return;
        }
        updatePanelMarginBottom(this.mConfiguration, this.mApplyWindowInsets);
    }

    public COUIBottomSheetDialog(Context context, boolean cancelable, DialogInterface.OnCancelListener onCancelListener) {
        this(context, com.coui.appcompat.R.style.DefaultBottomSheetDialog);
        setCancelable(cancelable);
        setOnCancelListener(onCancelListener);
    }

    private void setFrameRate(final ValueAnimator valueAnimator) {
        if (!this.mIsVSdk || this.mDesignBottomSheetFrameLayout == null) {
            return;
        }
        int featureType = this.mADFRFeatureType;
        if (featureType == 2) {
            final AnimationVelocityCalculator animationVelocityCalculator = new AnimationVelocityCalculator(valueAnimator);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public final void onAnimationUpdate(ValueAnimator valueAnimator2) {
                    COUIBottomSheetDialog.this.onAnimatorFrameRateUpdate(animationVelocityCalculator, valueAnimator, valueAnimator2);
                }
            });
            valueAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animator) {
                    super.onAnimationEnd(animator);
                    COUILog.d(COUIBottomSheetDialog.TAG, "LEVEL_HIGH_PRECISION onAnimatorEnd: DynamicFrameRateManager.FRAME_RATE_END");
                    DynamicFrameRateManager.setFrameRate(COUIBottomSheetDialog.this.mDesignBottomSheetFrameLayout, COUIBottomSheetDialog.ANIMATION_TYPE_ID, -2, (Bundle) null);
                }
            });
        } else if (featureType == 1) {
            valueAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animator) {
                    super.onAnimationEnd(animator);
                    COUILog.d(COUIBottomSheetDialog.TAG, "LEVEL_LOW_PRECISION onAnimatorEnd: DynamicFrameRateManager.FRAME_RATE_END");
                    DynamicFrameRateManager.setFrameRate(COUIBottomSheetDialog.this.mDesignBottomSheetFrameLayout, COUIBottomSheetDialog.ANIMATION_TYPE_ID, -2, (Bundle) null);
                }

                @Override
                public void onAnimationStart(Animator animator) {
                    super.onAnimationStart(animator);
                    COUILog.d(COUIBottomSheetDialog.TAG, "LEVEL_LOW_PRECISION onAnimatorStart: DynamicFrameRateManager.LOW_PRECISION_FRAME_RATE");
                    DynamicFrameRateManager.setFrameRate(COUIBottomSheetDialog.this.mDesignBottomSheetFrameLayout, COUIBottomSheetDialog.ANIMATION_TYPE_ID, -1, (Bundle) null);
                }
            });
        } else if (featureType == 0) {
            COUILog.d(TAG, "LEVEL_DEFAULT do nothing");
        }
    }

    @Override
    public void setContentView(View view) {
        if (view == null) {
            throw new IllegalArgumentException("ContentView can't be null");
        }
        COUIThemeOverlay.getInstance().applyThemeOverlays(getContext());
        setContentViewLocal(view);
        initView();
    }

    public void setDragableLinearLayout(COUIPanelContentLayout cOUIPanelContentLayout, boolean needRefresh) {
        this.mDraggableConstraintLayout = cOUIPanelContentLayout;
        if (!this.mIsHandlePanel) {
            hideDragView();
        }
        if (cOUIPanelContentLayout != null) {
            this.mPulledUpView = (ViewGroup) this.mDraggableConstraintLayout.getParent();
            cOUIPanelContentLayout.setLayoutAtMaxHeight(this.mIsShowInMaxHeight);
            if (this.mHandleViewHasPressAnim) {
                cOUIPanelContentLayout.setDragViewPressAnim(true);
            }
            cOUIPanelContentLayout.setDragViewDrawable(this.mPanelDragViewDrawable);
        }
        if (needRefresh) {
            refresh();
        } else if (cOUIPanelContentLayout != null) {
            WindowInsets windowInsets = this.mApplyWindowInsets;
            COUIPanelPercentFrameLayout cOUIPanelPercentFrameLayout = this.mDesignBottomSheetFrameLayout;
            cOUIPanelContentLayout.setNavigationMargin(null, windowInsets, cOUIPanelPercentFrameLayout != null && cOUIPanelPercentFrameLayout.getRatio() == 1.0f, this.mCouiPanelEdgeToEdgeEnable);
        }
        initDraggableConstraintLayoutSize();
    }

    private void cancelAnim(COUISpringAnimation cOUISpringAnimation) {
        if (cOUISpringAnimation == null || !cOUISpringAnimation.isRunning()) {
            return;
        }
        cOUISpringAnimation.cancel();
    }

    private void setHideDragViewHeight() {
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) this.mDraggableConstraintLayout.getDrawLayout().getLayoutParams();
        int height = this.mHideDragViewHeight;
        if (height > 0) {
            marginLayoutParams.height = height;
        } else {
            marginLayoutParams.height = getContext().getResources().getDimensionPixelSize(com.coui.appcompat.R.dimen.coui_panel_drag_view_hide_height);
        }
        marginLayoutParams.topMargin = 0;
        this.mDraggableConstraintLayout.getDrawLayout().setLayoutParams(marginLayoutParams);
    }

    public void dismiss(boolean animated) {
        if (isShowing() && animated && !this.mIsExecutingDismissAnim) {
            hideKeyboard();
            if (getBehavior().getState() != 5) {
                dismissWithInterruptibleAnim();
                return;
            }
            return;
        }
        superDismiss();
    }

    public COUIBottomSheetDialog(Context context, int themeResId, float frequency, float dampingRatio) {
        this(context, themeResId);
        this.mPhysicsFrequency = frequency;
        this.mPhysicsDampingRatio = dampingRatio;
    }

    public COUIBottomSheetDialog(Context context, int themeResId) {
        super(context, resolveDialogTheme(context, themeResId));
        this.mTemtRect = new Rect();
        this.mHandleViewHasPressAnim = true;
        this.mIsShowInDialogFragment = false;
        this.mCancelable = true;
        this.mCanceledOnTouchOutside = true;
        this.mCanPullUp = true;
        this.mCurrentSpringTotalOffset = 0;
        this.mCoordinatorLayoutMinInsetsTop = 0;
        this.mCoordinatorLayoutPaddingExtra = 0;
        this.mPeekHeight = 0;
        this.mSkipCollapsed = true;
        this.mFirstShowCollapsed = false;
        this.mCurrentParentViewTranslationY = 0.0f;
        this.mCurrentOutsideAlpha = 0.0f;
        this.mIsInterruptingAnim = false;
        this.mWindowInsetsListener = null;
        this.mPanelPullUpListener = null;
        this.mNavColor = Integer.MAX_VALUE;
        this.mWindowInsetsAnimEnable = false;
        this.mIsInWindowFloatingMode = false;
        this.mCanPerformHapticFeedback = false;
        this.mRegisterConfigurationChangeCallBack = true;
        this.mIsNeedShowKeyboard = false;
        this.mIsNeedOutsideViewAnim = true;
        this.mFocusChange = null;
        this.mIsDraggable = true;
        this.mTranslateHidingDuration = DEFAULT_TRANSLATE_HIDING_ANIMATOR_DURATION;
        this.mPanelBarView = null;
        this.mBottomSheetDialogAnimatorListener = null;
        this.mDisableSubExpand = false;
        this.mGlobalDrag = true;
        this.mPhysicsFrequency = Float.MIN_VALUE;
        this.mPhysicsDampingRatio = Float.MIN_VALUE;
        this.mAnchorView = null;
        this.mStatusBarHeight = 0;
        this.mSnapStartBottom = -1;
        this.mAppearStiffness = Float.MIN_VALUE;
        this.mAppearDampingRatio = Float.MIN_VALUE;
        this.mIsAppearSpringAnimStared = false;
        this.mShouldRegisterWindowInsetsListener = true;
        this.mPreferWidth = -1;
        this.mOriginWidth = -1;
        this.isLargeScreenLimitMaxSize = false;
        this.mIsHandlePanel = false;
        this.mIsGestureNavigation = true;
        this.mHideDragViewHeight = 0;
        this.mFrameRate = true;
        this.mAnimationFlag = 0;
        this.mCurrentOutSideAlphaStateHidden = 0.0f;
        this.mCurrentOutSideAlphaStateShow = 0.0f;
        this.mPullUpToDismissPanelListener = new COUIBottomSheetBehavior.PullUpToDismissPanelListener() {
            @Override
            public void onPullUpDismiss() {
                COUIBottomSheetDialog.this.dismissWithAlphaAnim();
            }
        };
        this.mIsAnimationInFirst = false;
        this.mOnAttatchStateChangeListener = null;
        this.mLastStaticState = 3;
        this.mPanelRatio = 1.0f;
        this.mComponentCallbacks = new ComponentCallbacks() {
            @Override
            public void onConfigurationChanged(Configuration configuration) {
                if (COUIBottomSheetDialog.this.mRegisterConfigurationChangeCallBack) {
                    COUIBottomSheetDialog.this.updateLayoutWhileConfigChange(configuration);
                }
            }

            @Override
            public void onLowMemory() {
            }
        };
        this.mOnPreDrawListener = new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                COUIBottomSheetDialog.this.removeOnPreDrawListener();
                if (COUIBottomSheetDialog.this.mDesignBottomSheetFrameLayout == null) {
                    COUIBottomSheetDialog cOUIBottomSheetDialog = COUIBottomSheetDialog.this;
                    cOUIBottomSheetDialog.doParentViewTranslationShowingAnim(0, cOUIBottomSheetDialog.getPanelShowAnimListener());
                    return true;
                }
                int contentViewHeightWithMargins = COUIBottomSheetDialog.this.getContentViewHeightWithMargins();
                if (COUIBottomSheetDialog.this.mFirstShowCollapsed) {
                    contentViewHeightWithMargins = COUIBottomSheetDialog.this.mPeekHeight;
                }
                COUIPanelContentLayout cOUIPanelContentLayout = COUIBottomSheetDialog.this.mDraggableConstraintLayout;
                if ((cOUIPanelContentLayout == null || cOUIPanelContentLayout.findFocus() == null) && !COUIBottomSheetDialog.this.isFollowHand() && !COUIBottomSheetDialog.this.isFadeInCenter()) {
                    COUIBottomSheetDialog.this.mDesignBottomSheetFrameLayout.setTranslationY(contentViewHeightWithMargins);
                }
                COUIBottomSheetDialog.this.mOutsideView.setAlpha(ALPHA_TRANSPARENT);
                if (COUIBottomSheetDialog.this.mDesignBottomSheetFrameLayout.getRatio() == 2.0f) {
                    COUIBottomSheetDialog cOUIBottomSheetDialog2 = COUIBottomSheetDialog.this;
                    cOUIBottomSheetDialog2.doParentViewTranslationShowingAnim(cOUIBottomSheetDialog2.mCoordinatorLayout.getHeight() / 2, COUIBottomSheetDialog.this.getPanelShowAnimListener());
                } else {
                    COUIBottomSheetDialog cOUIBottomSheetDialog3 = COUIBottomSheetDialog.this;
                    cOUIBottomSheetDialog3.doParentViewTranslationShowingAnim(0, cOUIBottomSheetDialog3.getPanelShowAnimListener());
                }
                COUIBottomSheetDialog cOUIBottomSheetDialog4 = COUIBottomSheetDialog.this;
                cOUIBottomSheetDialog4.mPanelRatio = cOUIBottomSheetDialog4.mDesignBottomSheetFrameLayout.getRatio();
                return true;
            }
        };
        initThemeResources(themeResId);
        initValueResources();
        saveActivityContextToGetMultiWindowInfo(context);
    }

    private void setFrameRate(COUISpringAnimation cOUISpringAnimation) {
        if (!this.mIsVSdk || this.mDesignBottomSheetFrameLayout == null) {
            return;
        }
        int featureType = this.mADFRFeatureType;
        if (featureType == 2) {
            cOUISpringAnimation.addUpdateListener(new COUIDynamicAnimation.OnAnimationUpdateListener() {
                @Override
                public final void onAnimationUpdate(COUIDynamicAnimation cOUIDynamicAnimation, float value, float velocity) {
                    COUIBottomSheetDialog.this.onSpringFrameRateUpdate(cOUIDynamicAnimation, value, velocity);
                }
            });
            cOUISpringAnimation.addEndListener(new COUIDynamicAnimation.OnAnimationEndListener() {
                @Override
                public void onAnimationEnd(COUIDynamicAnimation cOUIDynamicAnimation, boolean canceled, float value, float velocity) {
                    COUILog.d(COUIBottomSheetDialog.TAG, "COUISpringAnimation LEVEL_HIGH_PRECISION onAnimatorEnd: DynamicFrameRateManager.FRAME_RATE_END");
                    DynamicFrameRateManager.setFrameRate(COUIBottomSheetDialog.this.mDesignBottomSheetFrameLayout, COUIBottomSheetDialog.ANIMATION_TYPE_ID, -2, (Bundle) null);
                }
            });
        } else if (featureType == 1) {
            COUILog.d(TAG, "COUISpringAnimation LEVEL_LOW_PRECISION onAnimatorStart: DynamicFrameRateManager.LOW_PRECISION_FRAME_RATE");
            DynamicFrameRateManager.setFrameRate(this.mDesignBottomSheetFrameLayout, ANIMATION_TYPE_ID, -1, (Bundle) null);
            cOUISpringAnimation.addEndListener(new COUIDynamicAnimation.OnAnimationEndListener() {
                @Override
                public void onAnimationEnd(COUIDynamicAnimation cOUIDynamicAnimation, boolean canceled, float value, float velocity) {
                    COUILog.d(COUIBottomSheetDialog.TAG, "COUISpringAnimation LEVEL_LOW_PRECISION onAnimatorEnd: DynamicFrameRateManager.FRAME_RATE_END");
                    DynamicFrameRateManager.setFrameRate(COUIBottomSheetDialog.this.mDesignBottomSheetFrameLayout, COUIBottomSheetDialog.ANIMATION_TYPE_ID, -2, (Bundle) null);
                }
            });
        } else if (featureType == 0) {
            COUILog.d(TAG, "COUISpringAnimation LEVEL_DEFAULT do nothing");
        }
    }

    public void doFeedbackAnimation() {
        if (this.mDesignBottomSheetFrameLayout != null) {
            AnimatorSet animatorSet = this.mPanelViewTranslationAnimationSet;
            if (animatorSet == null || !animatorSet.isRunning()) {
                COUISpringAnimation cOUISpringAnimation = this.mTranslationAndScaleSpringAnimation;
                if (cOUISpringAnimation == null || !cOUISpringAnimation.isRunning()) {
                    doFeedbackAnimation(this.mDesignBottomSheetFrameLayout);
                }
            }
        }
    }

    private void enforceChangeScreenWidth(Configuration configuration) {
        if (this.mPreferWidth == -1) {
            return;
        }
        try {
            Resources resources = getContext().getResources();
            this.mOriginWidth = configuration.screenWidthDp;
            configuration.screenWidthDp = this.mPreferWidth;
            resources.updateConfiguration(configuration, resources.getDisplayMetrics());
            Log.d(TAG, "enforceChangeScreenWidth : OriginWidth=" + this.mOriginWidth + " ,PreferWidth:" + this.mPreferWidth);
            COUIPanelPercentFrameLayout cOUIPanelPercentFrameLayout = this.mDesignBottomSheetFrameLayout;
            if (cOUIPanelPercentFrameLayout != null) {
                cOUIPanelPercentFrameLayout.setPreferWidth(this.mPreferWidth);
            }
        } catch (Exception unused) {
            Log.d(TAG, "enforceChangeScreenWidth : failed to updateConfiguration");
        }
    }
}
