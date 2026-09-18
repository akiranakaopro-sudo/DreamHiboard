package com.coui.appcompat.poplist;

import com.coui.appcompat.R;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import androidx.dynamicanimation.animation.FloatPropertyCompat;
import android.view.ViewGroup;
import android.widget.ListView;
import com.coui.appcompat.animation.dynamicanimation.COUIDynamicAnimation;
import com.coui.appcompat.animation.dynamicanimation.COUISpringAnimation;
import com.coui.appcompat.animation.dynamicanimation.COUISpringForce;
import com.coui.appcompat.poplist.BasePopupMenuAnimationController;
import com.coui.appcompat.uiutil.UIUtil;


class SmallScreenAnimationController extends BasePopupMenuAnimationController {
    private static final float DEFAULT_MAIN_MENU_ALPHA_PERCENT = 0.3f;
    private static final float DEFAULT_TRANSLATE = 0.0f;
    private static final String TAG = "PopupMenuAnimCtrl-S";
    private COUISpringAnimation mMainMenuAlphaAnimation;
    private float mMainMenuAlphaPercent;
    private Animator mMainMenuAlphaRenderAnimator;
    private COUISpringAnimation mMainMenuScaleAnimation;
    private float mMainMenuScalePercentX;
    private float mMainMenuScalePercentY;
    private Animator mMainMenuScaleRenderAnimator;
    private final int mMenuInnerPaddingVertical;
    private final int mMinGapBetweenMainAndSubMenu;
    private COUISpringAnimation mSubMenuAnimation;
    private int mSubMenuClippedBottomEnd;
    private int mSubMenuClippedBottomStart;
    private int mSubMenuClippedTopEnd;
    private int mSubMenuClippedTopStart;
    private int mSubMenuTranslateEnd;
    private int mSubMenuTranslateStart;
    private static final FloatPropertyCompat<SmallScreenAnimationController> SUB_MENU_TRANSITION = new FloatPropertyCompat<SmallScreenAnimationController>("subMenuTransition") {
        @Override
        public float getValue(SmallScreenAnimationController smallScreenAnimationController) {
            return smallScreenAnimationController.getSubMenuTransitionProgress();
        }

        @Override
        public void setValue(SmallScreenAnimationController smallScreenAnimationController, float value) {
            smallScreenAnimationController.setSubMenuTransitionProgress(value);
        }
    };
    private static final FloatPropertyCompat<SmallScreenAnimationController> MAIN_MENU_SCALE_TRANSITION = new FloatPropertyCompat<SmallScreenAnimationController>("mainMenuTScaletransition") {
        @Override
        public float getValue(SmallScreenAnimationController smallScreenAnimationController) {
            return smallScreenAnimationController.getMainMenuScaleTransitionProgress();
        }

        @Override
        public void setValue(SmallScreenAnimationController smallScreenAnimationController, float value) {
            smallScreenAnimationController.setMainMenuScaleTransitionProgress(value);
        }
    };
    private static final FloatPropertyCompat<SmallScreenAnimationController> MAIN_MENU_ALPHA_TRANSITION = new FloatPropertyCompat<SmallScreenAnimationController>("mainMenuAlphaTransition") {
        @Override
        public float getValue(SmallScreenAnimationController smallScreenAnimationController) {
            return smallScreenAnimationController.getMainMenuAlphaTransitionProgress();
        }

        @Override
        public void setValue(SmallScreenAnimationController smallScreenAnimationController, float value) {
            smallScreenAnimationController.setMainMenuAlphaTransitionProgress(value);
        }
    };
    private final COUIDynamicAnimation.OnAnimationEndListener mSubMenuAnimationEndListener = new COUIDynamicAnimation.OnAnimationEndListener() {
        @Override
        public final void onAnimationEnd(COUIDynamicAnimation cOUIDynamicAnimation, boolean canceled, float value, float velocity) {
            SmallScreenAnimationController.this.onSubMenuSpringAnimationEnd(cOUIDynamicAnimation, canceled, value, velocity);
        }
    };
    private float mMainMenuScaleTransitionProgress = 0.0f;
    private float mMainMenuAlphaTransitionProgress = 0.0f;
    private float mSubMenuTransitionProgress = 0.0f;
    private float mRootViewAlpha = 1.0f;

    public SmallScreenAnimationController(Context context) {
        this.mMinGapBetweenMainAndSubMenu = context.getResources().getDimensionPixelOffset(R.dimen.coui_popup_list_window_min_gap_to_top);
        this.mMenuInnerPaddingVertical = context.getResources().getDimensionPixelOffset(R.dimen.coui_popup_list_padding_vertical);
    }

    private void configMainMenuAnimationPropertiesForSubMenuEnter() {
        this.mMainMenuAlphaPercent = DEFAULT_MAIN_MENU_ALPHA_PERCENT;
        float fWidth = this.mDomain.mMainMenuRelocated.width() / this.mDomain.mMainMenu.width();
        this.mMainMenuScalePercentX = fWidth;
        this.mMainMenuScalePercentY = fWidth;
        PopupMenuDomain popupMenuDomain = this.mDomain;
        Rect rect = popupMenuDomain.mMainMenu;
        int mainLeft = rect.left;
        Rect rect2 = popupMenuDomain.mMainMenuRelocated;
        if (mainLeft == rect2.left) {
            this.mMainMenuRoot.setPivotX(0.0f);
        } else if (rect.right == rect2.right) {
            this.mMainMenuRoot.setPivotX(this.mMainMenuRoot.getWidth());
        } else {
            this.mMainMenuRoot.setPivotX(this.mMainMenuRoot.getWidth() / 2.0f);
        }
        this.mMainMenuRoot.setPivotY(0.0f);
    }

    private void configSubMenuAnimationPropertiesForSubMenuEnter() {
        BasePopupMenuAnimationController.OnMenuStateChangedListener onMenuStateChangedListener = this.mMenuStateChangedListener;
        if (onMenuStateChangedListener != null) {
            onMenuStateChangedListener.onSubMenuStartToEnter();
        }
        this.mSubMenuClippedTopStart = this.mMenuInnerPaddingVertical * 2;
        this.mSubMenuClippedTopEnd = 0;
        this.mSubMenuClippedBottomStart = this.mDomain.mSubMenuAnchor.height() - this.mSubMenuClippedTopStart;
        this.mSubMenuClippedBottomEnd = this.mDomain.mSubMenu.height();
        View view = this.mSubMenuRoot;
        if (view instanceof RoundFrameLayout) {
            ((RoundFrameLayout) view).setOverrideOutline(0, this.mSubMenuClippedTopStart, this.mDomain.mSubMenu.width(), this.mSubMenuClippedBottomStart, 1.0f);
        }
    }

    private void ensureMainMenuEnterAnimator() {
        boolean animationsExist = (this.mMainMenuScaleAnimation == null || this.mMainMenuAlphaAnimation == null) ? false : true;
        if (shouldReuseMainMenuAnimations(animationsExist)) {
            return;
        }
        if (animationsExist) {
            this.mMainMenuScaleAnimation.cancel();
            this.mMainMenuScaleAnimation = null;
            this.mMainMenuAlphaAnimation.cancel();
            this.mMainMenuAlphaAnimation = null;
        }
        COUISpringForce cOUISpringForce = new COUISpringForce();
        cOUISpringForce.setBounce(0.2f);
        cOUISpringForce.setResponse(0.35f);
        COUISpringAnimation cOUISpringAnimation = new COUISpringAnimation(this, MAIN_MENU_SCALE_TRANSITION);
        this.mMainMenuScaleAnimation = cOUISpringAnimation;
        cOUISpringAnimation.setSpring(cOUISpringForce);
        COUISpringForce cOUISpringForce2 = new COUISpringForce();
        cOUISpringForce2.setBounce(0.2f);
        cOUISpringForce2.setResponse(0.35f);
        COUISpringAnimation cOUISpringAnimation2 = new COUISpringAnimation(this, MAIN_MENU_ALPHA_TRANSITION);
        this.mMainMenuAlphaAnimation = cOUISpringAnimation2;
        cOUISpringAnimation2.setSpring(cOUISpringForce2);
        this.mMainMenuAlphaAnimation.addEndListener(this.mMainMenuAnimationEndListener);
    }

    private void ensureSubMenuAnimator() {
        if (this.mSubMenuAnimation != null) {
            return;
        }
        COUISpringForce cOUISpringForce = new COUISpringForce();
        cOUISpringForce.setBounce(0.0f);
        cOUISpringForce.setResponse(0.35f);
        COUISpringAnimation cOUISpringAnimation = new COUISpringAnimation(this, SUB_MENU_TRANSITION);
        this.mSubMenuAnimation = cOUISpringAnimation;
        cOUISpringAnimation.setSpring(cOUISpringForce);
        this.mSubMenuAnimation.addEndListener(this.mSubMenuAnimationEndListener);
    }


    public float getMainMenuAlphaTransitionProgress() {
        return this.mMainMenuAlphaTransitionProgress;
    }


    public float getMainMenuScaleTransitionProgress() {
        return this.mMainMenuScaleTransitionProgress;
    }


    public float getSubMenuTransitionProgress() {
        return this.mSubMenuTransitionProgress;
    }


    public void onSubMenuSpringAnimationEnd(COUIDynamicAnimation cOUIDynamicAnimation, final boolean canceled, final float value, float velocity) {
        View view;
        AnimationExecutor executor = getExecutor();
        if (!executor.isAsynchronous() || (view = this.mRootView) == null) {
            dispatchSubMenuAnimationEnd(canceled, value);
        } else {
            executor.runOnMainThread(view, new Runnable() {
                @Override
                public final void run() {
                    SmallScreenAnimationController.this.dispatchSubMenuAnimationEnd(canceled, value);
                }
            });
        }
    }


    public void ensureMainMenuRootVisibleFromAlpha() {
        View view = this.mMainMenuRoot;
        if (view == null || view.getVisibility() == 0) {
            return;
        }
        this.mMainMenuRoot.setVisibility(0);
    }


    public void ensureMainMenuRootVisibleFromScale() {
        View view = this.mMainMenuRoot;
        if (view == null || view.getVisibility() == 0) {
            return;
        }
        this.mMainMenuRoot.setVisibility(0);
    }



    public void dispatchSubMenuAnimationEnd(boolean canceled, float value) {
        if (canceled) {
            BasePopupMenuAnimationController.OnMenuStateChangedListener onMenuStateChangedListener = this.mMenuStateChangedListener;
            if (onMenuStateChangedListener != null) {
                onMenuStateChangedListener.onSubMenuAnimationCanceled();
                return;
            }
            return;
        }
        if (value == 0.0f) {
            BasePopupMenuAnimationController.OnMenuStateChangedListener onMenuStateChangedListener2 = this.mMenuStateChangedListener;
            if (onMenuStateChangedListener2 != null) {
                onMenuStateChangedListener2.onSubMenuExited();
                return;
            }
            return;
        }
        BasePopupMenuAnimationController.OnMenuStateChangedListener onMenuStateChangedListener3 = this.mMenuStateChangedListener;
        if (onMenuStateChangedListener3 != null) {
            onMenuStateChangedListener3.onSubMenuEntered();
        }
    }


    public void setMainMenuAlphaTransitionProgress(float progress) {
        this.mMainMenuAlphaTransitionProgress = progress;
        float fraction = progress / 10000.0f;
        AnimationExecutor executor = getExecutor();
        View view = this.mMainMenuRoot;
        if (view == null || this.mRootView == null) {
            Log.w(TAG, "No main menu root view! Skip animation update");
            return;
        }
        if (view.getVisibility() != 0) {
            executor.runOnMainThread(this.mRootView, new Runnable() {
                @Override
                public final void run() {
                    SmallScreenAnimationController.this.ensureMainMenuRootVisibleFromAlpha();
                }
            });
        }
        float convertedFraction = UIUtil.getConvertedFraction(0.01f, 1.0f, fraction);
        this.mRootViewAlpha = convertedFraction;
        executor.setAlpha(this.mRootView, convertedFraction);
        View view2 = this.mMainMenuRoot;
        if ((view2 instanceof RoundFrameLayout) && ((RoundFrameLayout) view2).getUseBackgroundBlur()) {
            this.mMainMenuRoot.setAlpha(this.mRootViewAlpha);
        }
        View view3 = this.mSubMenuRoot;
        if ((view3 instanceof RoundFrameLayout) && ((RoundFrameLayout) view3).getUseBackgroundBlur()) {
            this.mSubMenuRoot.setAlpha(this.mRootViewAlpha);
        }
    }


    public void setMainMenuScaleTransitionProgress(float progress) {
        COUISpringAnimation cOUISpringAnimation;
        if (this.mMainMenuScaleTransitionProgress == 0.0f || this.mMainMenuAlphaTransitionProgress != 0.0f || (cOUISpringAnimation = this.mMainMenuScaleAnimation) == null || !cOUISpringAnimation.canSkipToEnd()) {
            this.mMainMenuScaleTransitionProgress = progress;
        } else {
            this.mMainMenuScaleTransitionProgress = 0.0f;
        }
        float fraction = this.mMainMenuScaleTransitionProgress / 10000.0f;
        AnimationExecutor executor = getExecutor();
        View view = this.mMainMenuRoot;
        if (view == null || this.mRootView == null) {
            Log.w(TAG, "No main menu root view! Skip animation update");
            return;
        }
        if (view.getVisibility() != 0) {
            executor.runOnMainThread(this.mRootView, new Runnable() {
                @Override
                public final void run() {
                    SmallScreenAnimationController.this.ensureMainMenuRootVisibleFromScale();
                }
            });
        }
        float convertedFraction = UIUtil.getConvertedFraction(0.0f, 1.0f, fraction);
        executor.setScaleX(this.mRootView, convertedFraction);
        executor.setScaleY(this.mRootView, convertedFraction);
    }


    public void setSubMenuTransitionProgress(float progress) {
        this.mSubMenuTransitionProgress = progress;
        float fraction = progress / 10000.0f;
        int iRound = Math.round(UIUtil.getConvertedFraction(this.mSubMenuTranslateStart, this.mSubMenuTranslateEnd, fraction));
        View view = this.mSubMenuRoot;
        if (view instanceof RoundFrameLayout) {
            if (view.getVisibility() != 0) {
                this.mSubMenuRoot.setVisibility(0);
            }
            this.mSubMenuRoot.setTranslationY(iRound);
            int convertedFraction = (int) UIUtil.getConvertedFraction(this.mSubMenuClippedTopStart, this.mSubMenuClippedTopEnd, fraction);
            ((RoundFrameLayout) this.mSubMenuRoot).setOverrideOutline(0, convertedFraction, this.mDomain.mSubMenu.width(), convertedFraction + ((int) UIUtil.getConvertedFraction(this.mSubMenuClippedBottomStart, this.mSubMenuClippedBottomEnd, fraction)), fraction);
            View childAt = ((RoundFrameLayout) this.mSubMenuRoot).getChildAt(0);
            if (childAt instanceof ListView) {
                int childIndex = 1;
                while (true) {
                    ListView listView = (ListView) childAt;
                    if (childIndex > listView.getChildCount()) {
                        break;
                    }
                    View childAt2 = listView.getChildAt(childIndex);
                    if (childAt2 != null) {
                        childAt2.setAlpha(fraction);
                    }
                    childIndex++;
                }
            }
        }
        View view2 = this.mMainMenuRoot;
        if (view2 != null) {
            if (view2 instanceof ViewGroup) {
                ((ViewGroup) view2).getChildAt(0).setAlpha(UIUtil.getConvertedFraction(1.0f, this.mMainMenuAlphaPercent, fraction));
            }
            this.mMainMenuRoot.setScaleX(UIUtil.getConvertedFraction(1.0f, this.mMainMenuScalePercentX, fraction));
            this.mMainMenuRoot.setScaleY(UIUtil.getConvertedFraction(1.0f, this.mMainMenuScalePercentY, fraction));
            translateMainMenu(fraction, iRound);
        }
    }

    private void translateMainMenu(float fraction, int subMenuTranslationY) {
        if (this.mMainMenuRoot == null) {
            return;
        }
        if (this.mDomain.mSubMenu.isEmpty()) {
            this.mMainMenuRoot.setTranslationY(0.0f);
            return;
        }
        PopupMenuDomain popupMenuDomain = this.mDomain;
        int mainTop = popupMenuDomain.mMainMenu.top;
        int minGap = this.mMinGapBetweenMainAndSubMenu;
        int mainTopWithGap = mainTop + minGap;
        int subTop = popupMenuDomain.mSubMenu.top;
        if (mainTopWithGap > subTop) {
            this.mMainMenuRoot.setTranslationY((int) UIUtil.getConvertedFraction(0.0f, (subTop - minGap) - mainTop, fraction));
        } else if (mainTop + minGap > subTop + subMenuTranslationY) {
            this.mMainMenuRoot.setTranslationY((subTop + subMenuTranslationY) - (mainTop + minGap));
        } else {
            this.mMainMenuRoot.setTranslationY(0.0f);
        }
    }

    @Override
    public void detach() {
        View view = this.mSubMenuRoot;
        if (view instanceof RoundFrameLayout) {
            view.setTranslationY(0.0f);
            ((RoundFrameLayout) this.mSubMenuRoot).clearOverrideOutline();
        }
    }

    @Override
    public void setMainMenuView(View view) {
        if (this.mMainMenuRoot != view) {
            COUISpringAnimation cOUISpringAnimation = this.mMainMenuScaleAnimation;
            if (cOUISpringAnimation != null) {
                cOUISpringAnimation.cancel();
                this.mMainMenuScaleAnimation = null;
            }
            COUISpringAnimation cOUISpringAnimation2 = this.mMainMenuAlphaAnimation;
            if (cOUISpringAnimation2 != null) {
                cOUISpringAnimation2.cancel();
                this.mMainMenuAlphaAnimation = null;
            }
            Animator animator = this.mMainMenuScaleRenderAnimator;
            if (animator != null) {
                animator.cancel();
                this.mMainMenuScaleRenderAnimator = null;
            }
            Animator animator2 = this.mMainMenuAlphaRenderAnimator;
            if (animator2 != null) {
                animator2.cancel();
                this.mMainMenuAlphaRenderAnimator = null;
            }
        }
        this.mMainMenuScaleTransitionProgress = 0.0f;
        this.mMainMenuAlphaTransitionProgress = 0.0f;
        super.setMainMenuView(view);
        ensureMainMenuEnterAnimator();
    }

    @Override
    public void setMenuRootView(View view) {
        super.setMenuRootView(view);
        ensureMainMenuEnterAnimator();
    }

    @Override
    public void setSubMenuView(View view) {
        ensureSubMenuAnimator();
        if (this.mSubMenuAnimation.isRunning() && this.mSubMenuAnimation.canSkipToEnd()) {
            if (view == this.mSubMenuRoot) {
                this.mSubMenuAnimation.cancel();
            } else {
                this.mSubMenuAnimation.skipToEnd();
            }
        }
        PopupMenuDomain popupMenuDomain = this.mDomain;
        int translateStart = popupMenuDomain.mSubMenuAnchor.top - popupMenuDomain.mSubMenu.top;
        this.mSubMenuTranslateStart = translateStart;
        if (!popupMenuDomain.mSubMenuAnchorIsFirstItem) {
            this.mSubMenuTranslateStart = translateStart - this.mMenuInnerPaddingVertical;
        }
        this.mSubMenuTranslateEnd = 0;
        super.setSubMenuView(view);
    }

    @Override
    public void startMainMenuEnter(boolean skipToEnd) {
        // Leapy modified: restore OPPO's decoded direct spring-animation flow.
        if (this.mMainMenuRoot == null) {
            Log.w(TAG, "No main menu root view! Set a main menu view before starting animation!");
            return;
        }
        this.mRootView.setTranslationY(0.0f);
        this.mRootView.setPivotX(this.mDomain.getMainMenuEnterPivotX());
        this.mRootView.setPivotY(this.mDomain.getMainMenuEnterPivotY());
        BasePopupMenuAnimationController.OnMenuStateChangedListener onMenuStateChangedListener = this.mMenuStateChangedListener;
        if (onMenuStateChangedListener != null) {
            onMenuStateChangedListener.onMainMenuStartToEnter();
        }
        this.mMainMenuScaleAnimation.getSpring().setResponse(0.35f);
        this.mMainMenuScaleAnimation.getSpring().setBounce(0.2f);
        this.mMainMenuScaleAnimation.setStartValue(this.mMainMenuScaleTransitionProgress);
        this.mMainMenuScaleAnimation.animateToFinalPosition(10000.0f);
        if (!skipToEnd && this.mMainMenuScaleAnimation.canSkipToEnd()) {
            this.mMainMenuScaleAnimation.skipToEnd();
        }
        this.mMainMenuAlphaAnimation.getSpring().setResponse(0.35f);
        this.mMainMenuAlphaAnimation.getSpring().setBounce(0.2f);
        this.mMainMenuAlphaAnimation.setStartValue(this.mMainMenuAlphaTransitionProgress);
        this.mMainMenuAlphaAnimation.animateToFinalPosition(10000.0f);
        if (skipToEnd || !this.mMainMenuAlphaAnimation.canSkipToEnd()) {
            return;
        }
        this.mMainMenuAlphaAnimation.skipToEnd();
        // Leapy end
    }

    @Override
    public void startMainMenuExit(boolean skipToEnd) {
        // Leapy modified: restore OPPO's decoded direct spring-animation flow.
        if (this.mMainMenuRoot == null) {
            Log.w(TAG, "No main menu root view! Set a main menu view before starting animation!");
            return;
        }
        BasePopupMenuAnimationController.OnMenuStateChangedListener onMenuStateChangedListener = this.mMenuStateChangedListener;
        if (onMenuStateChangedListener != null) {
            onMenuStateChangedListener.onMainMenuStartToExit();
        }
        this.mMainMenuScaleAnimation.getSpring().setResponse(0.3f);
        this.mMainMenuScaleAnimation.getSpring().setBounce(0.0f);
        this.mMainMenuScaleAnimation.setStartValue(this.mMainMenuScaleTransitionProgress);
        this.mMainMenuScaleAnimation.animateToFinalPosition(0.0f);
        if (!skipToEnd && this.mMainMenuScaleAnimation.canSkipToEnd()) {
            this.mMainMenuScaleAnimation.skipToEnd();
        }
        this.mMainMenuAlphaAnimation.getSpring().setResponse(0.25f);
        this.mMainMenuAlphaAnimation.getSpring().setBounce(0.0f);
        this.mMainMenuAlphaAnimation.setStartValue(this.mMainMenuAlphaTransitionProgress);
        this.mMainMenuAlphaAnimation.animateToFinalPosition(0.0f);
        if (skipToEnd || !this.mMainMenuAlphaAnimation.canSkipToEnd()) {
            return;
        }
        this.mMainMenuAlphaAnimation.skipToEnd();
        // Leapy end
    }

    @Override
    public void startSubMenuEnter(boolean skipToEnd) {
        if (this.mMainMenuRoot == null) {
            Log.e(TAG, "No main menu view! Add a main menu view before showing sub menu!");
            return;
        }
        if (this.mSubMenuRoot == null) {
            Log.w(TAG, "No sub menu root view! Set a sub menu view before starting animation!");
            return;
        }
        ensureSubMenuAnimator();
        configMainMenuAnimationPropertiesForSubMenuEnter();
        configSubMenuAnimationPropertiesForSubMenuEnter();
        this.mSubMenuRoot.setAlpha(this.mRootViewAlpha);
        this.mSubMenuAnimation.setStartValue(this.mSubMenuTransitionProgress);
        this.mSubMenuAnimation.animateToFinalPosition(10000.0f);
        if (skipToEnd || !this.mSubMenuAnimation.canSkipToEnd()) {
            return;
        }
        this.mSubMenuAnimation.skipToEnd();
    }

    @Override
    public void startSubMenuExit(boolean skipToEnd) {
        ensureSubMenuAnimator();
        if (this.mSubMenuAnimation.isRunning()) {
            Log.w(TAG, "Sub menu is exiting!");
        }
        if (this.mSubMenuRoot == null) {
            Log.w(TAG, "No sub menu root view! Set a sub menu view before starting animation!");
            return;
        }
        BasePopupMenuAnimationController.OnMenuStateChangedListener onMenuStateChangedListener = this.mMenuStateChangedListener;
        if (onMenuStateChangedListener != null) {
            onMenuStateChangedListener.onSubMenuStartToExit();
        }
        this.mSubMenuAnimation.setStartValue(this.mSubMenuTransitionProgress);
        this.mSubMenuAnimation.animateToFinalPosition(0.0f);
        if (skipToEnd || !this.mSubMenuAnimation.canSkipToEnd()) {
            return;
        }
        this.mSubMenuAnimation.skipToEnd();
    }

    @Override
    public void stopAllAnimation() {
        cleanupPendingMainMenuCallbacks();
        COUISpringAnimation cOUISpringAnimation = this.mMainMenuScaleAnimation;
        if (cOUISpringAnimation != null) {
            cOUISpringAnimation.cancel();
        }
        COUISpringAnimation cOUISpringAnimation2 = this.mMainMenuAlphaAnimation;
        if (cOUISpringAnimation2 != null) {
            cOUISpringAnimation2.cancel();
        }
        if (this.mMainMenuScaleAnimation != null || this.mMainMenuAlphaAnimation != null) {
            setMainMenuScaleTransitionProgress(0.0f);
            setMainMenuAlphaTransitionProgress(0.0f);
            View view = this.mMainMenuRoot;
            if (view != null && (view instanceof RoundFrameLayout) && ((RoundFrameLayout) view).getUseBackgroundBlur()) {
                this.mMainMenuRoot.setAlpha(1.0f);
            }
            View view2 = this.mSubMenuRoot;
            if (view2 != null && (view2 instanceof RoundFrameLayout) && ((RoundFrameLayout) view2).getUseBackgroundBlur()) {
                this.mSubMenuRoot.setAlpha(1.0f);
            }
        }
        Animator animator = this.mMainMenuScaleRenderAnimator;
        if (animator != null) {
            animator.cancel();
        }
        Animator animator2 = this.mMainMenuAlphaRenderAnimator;
        if (animator2 != null) {
            animator2.cancel();
        }
        COUISpringAnimation cOUISpringAnimation3 = this.mSubMenuAnimation;
        if (cOUISpringAnimation3 != null) {
            cOUISpringAnimation3.cancel();
            setSubMenuTransitionProgress(0.0f);
        }
    }
}
