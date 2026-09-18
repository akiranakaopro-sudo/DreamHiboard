package com.coui.appcompat.poplist;

import com.coui.appcompat.R;

import android.animation.Animator;
import android.util.Log;
import android.view.View;
import androidx.dynamicanimation.animation.FloatPropertyCompat;
import com.coui.appcompat.animation.dynamicanimation.COUIDynamicAnimation;
import com.coui.appcompat.animation.dynamicanimation.COUISpringAnimation;
import com.coui.appcompat.animation.dynamicanimation.COUISpringForce;
import com.coui.appcompat.poplist.BasePopupMenuAnimationController;
import com.coui.appcompat.uiutil.UIUtil;


class DefaultScreenAnimationController extends BasePopupMenuAnimationController {
    private static final float DEFAULT_ALPHA = 1.0f;
    private static final float DEFAULT_SCALE = 1.0f;
    private static final float MIN_ALPHA = 0.1f;
    private static final String TAG = "PopupMenuAnimCtrl-D";
    private COUISpringAnimation mMainMenuAlphaAnimation;
    private Animator mMainMenuAlphaRenderAnimator;
    private COUISpringAnimation mMainMenuScaleAnimation;
    private Animator mMainMenuScaleRenderAnimator;
    private COUISpringAnimation mSubMenuAnimation;
    private static final FloatPropertyCompat<DefaultScreenAnimationController> SUB_MENU_TRANSITION = new FloatPropertyCompat<DefaultScreenAnimationController>("subMenuTransition") {
        @Override
        public float getValue(DefaultScreenAnimationController defaultScreenAnimationController) {
            return defaultScreenAnimationController.getSubMenuTransitionProgress();
        }

        @Override
        public void setValue(DefaultScreenAnimationController defaultScreenAnimationController, float value) {
            defaultScreenAnimationController.setSubMenuTransitionProgress(value);
        }
    };
    private static final FloatPropertyCompat<DefaultScreenAnimationController> MAIN_MENU_SCALE_TRANSITION = new FloatPropertyCompat<DefaultScreenAnimationController>("mainMenuScaleTransition") {
        @Override
        public float getValue(DefaultScreenAnimationController defaultScreenAnimationController) {
            return defaultScreenAnimationController.getMainMenuScaleTransitionProgress();
        }

        @Override
        public void setValue(DefaultScreenAnimationController defaultScreenAnimationController, float value) {
            defaultScreenAnimationController.setMainMenuScaleTransitionProgress(value);
        }
    };
    private static final FloatPropertyCompat<DefaultScreenAnimationController> MAIN_MENU_ALPHA_TRANSITION = new FloatPropertyCompat<DefaultScreenAnimationController>("mainMenuAlphaTransition") {
        @Override
        public float getValue(DefaultScreenAnimationController defaultScreenAnimationController) {
            return defaultScreenAnimationController.getMainMenuAlphaTransitionProgress();
        }

        @Override
        public void setValue(DefaultScreenAnimationController defaultScreenAnimationController, float value) {
            defaultScreenAnimationController.setMainMenuAlphaTransitionProgress(value);
        }
    };
    private final COUIDynamicAnimation.OnAnimationEndListener mSubMenuAnimationEndListener = new COUIDynamicAnimation.OnAnimationEndListener() {
        @Override
        public final void onAnimationEnd(COUIDynamicAnimation cOUIDynamicAnimation, boolean flag, float value, float value_2) {
            DefaultScreenAnimationController.this.dispatchSubMenuAnimationEnd(cOUIDynamicAnimation, flag, value, value_2);
        }
    };
    private float mMainMenuScaleTransitionProgress = 0.0f;
    private float mMainMenuAlphaTransitionProgress = 0.0f;
    private float mSubMenuTransitionProgress = 0.0f;
    private float mRootViewAlpha = DEFAULT_ALPHA;

    private void ensureMainMenuEnterAnimator() {
        boolean flag = (this.mMainMenuScaleAnimation == null || this.mMainMenuAlphaAnimation == null) ? false : true;
        if (shouldReuseMainMenuAnimations(flag)) {
            return;
        }
        if (flag) {
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


    public void dispatchSubMenuAnimationEnd(COUIDynamicAnimation cOUIDynamicAnimation, boolean flag, float value, float value_2) {
        if (flag) {
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


    public void setMainMenuAlphaTransitionProgress(float mainMenuAlphaTransitionProgress) {
        this.mMainMenuAlphaTransitionProgress = mainMenuAlphaTransitionProgress;
        float value = mainMenuAlphaTransitionProgress / 10000.0f;
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
                    DefaultScreenAnimationController.this.ensureMainMenuRootVisibleFromAlpha();
                }
            });
        }
        float convertedFraction = UIUtil.getConvertedFraction(0.01f, 1.0f, value);
        this.mRootViewAlpha = convertedFraction;
        executor.setAlpha(this.mRootView, convertedFraction);
        View view_2 = this.mMainMenuRoot;
        if ((view_2 instanceof RoundFrameLayout) && ((RoundFrameLayout) view_2).getUseBackgroundBlur()) {
            this.mMainMenuRoot.setAlpha(this.mRootViewAlpha);
        }
        View view_3 = this.mSubMenuRoot;
        if ((view_3 instanceof RoundFrameLayout) && ((RoundFrameLayout) view_3).getUseBackgroundBlur()) {
            this.mSubMenuRoot.setAlpha(this.mRootViewAlpha);
        }
    }


    public void setMainMenuScaleTransitionProgress(float mainMenuScaleTransitionProgress) {
        COUISpringAnimation cOUISpringAnimation;
        if (this.mMainMenuScaleTransitionProgress == 0.0f || this.mMainMenuAlphaTransitionProgress != 0.0f || (cOUISpringAnimation = this.mMainMenuScaleAnimation) == null || !cOUISpringAnimation.canSkipToEnd()) {
            this.mMainMenuScaleTransitionProgress = mainMenuScaleTransitionProgress;
        } else {
            this.mMainMenuScaleTransitionProgress = 0.0f;
        }
        float value = this.mMainMenuScaleTransitionProgress / 10000.0f;
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
                    DefaultScreenAnimationController.this.ensureMainMenuRootVisibleFromScale();
                }
            });
        }
        float convertedFraction = UIUtil.getConvertedFraction(0.0f, 1.0f, value);
        executor.setScaleX(this.mRootView, convertedFraction);
        executor.setScaleY(this.mRootView, convertedFraction);
    }


    public void setSubMenuTransitionProgress(float subMenuTransitionProgress) {
        this.mSubMenuTransitionProgress = subMenuTransitionProgress;
        float value = subMenuTransitionProgress / 10000.0f;
        View view = this.mSubMenuRoot;
        if (view != null) {
            if (view.getVisibility() != 0) {
                this.mSubMenuRoot.setVisibility(0);
            }
            float convertedFraction = UIUtil.getConvertedFraction(0.01f, 1.0f, value);
            View view_2 = this.mSubMenuRoot;
            if ((view_2 instanceof RoundFrameLayout) && ((RoundFrameLayout) view_2).getUseBackgroundBlur()) {
                this.mSubMenuRoot.setVisibility(convertedFraction <= MIN_ALPHA ? 8 : 0);
            }
            this.mSubMenuRoot.setAlpha(convertedFraction * this.mRootViewAlpha);
            this.mSubMenuRoot.setScaleX(UIUtil.getConvertedFraction(0.0f, 1.0f, value));
            this.mSubMenuRoot.setScaleY(UIUtil.getConvertedFraction(0.0f, 1.0f, value));
        }
    }

    @Override
    public void detach() {
        View view = this.mSubMenuRoot;
        if (view instanceof RoundFrameLayout) {
            view.setAlpha(1.0f);
            this.mSubMenuRoot.setScaleX(DEFAULT_SCALE);
            this.mSubMenuRoot.setScaleY(DEFAULT_SCALE);
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
            Animator animator_2 = this.mMainMenuAlphaRenderAnimator;
            if (animator_2 != null) {
                animator_2.cancel();
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
            this.mSubMenuAnimation.cancel();
        }
        super.setSubMenuView(view);
    }

    @Override
    public void startMainMenuEnter(boolean flag) {
        // Leapy modified: restore OPPO's decoded direct spring-animation flow.
        if (this.mMainMenuRoot == null) {
            Log.w(TAG, "No main menu root view! Set a main menu view before starting animation!");
            return;
        }
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
        if (!flag && this.mMainMenuScaleAnimation.canSkipToEnd()) {
            this.mMainMenuScaleAnimation.skipToEnd();
        }
        this.mMainMenuAlphaAnimation.getSpring().setResponse(0.35f);
        this.mMainMenuAlphaAnimation.getSpring().setBounce(0.2f);
        this.mMainMenuAlphaAnimation.setStartValue(this.mMainMenuAlphaTransitionProgress);
        this.mMainMenuAlphaAnimation.animateToFinalPosition(10000.0f);
        if (flag || !this.mMainMenuAlphaAnimation.canSkipToEnd()) {
            return;
        }
        this.mMainMenuAlphaAnimation.skipToEnd();
        // Leapy end
    }

    @Override
    public void startMainMenuExit(boolean flag) {
        // Leapy modified: restore OPPO's decoded direct spring-animation flow.
        if (this.mMainMenuRoot == null) {
            Log.w(TAG, "No main menu root view! Set a main menu view before starting animation!");
            return;
        }
        this.mRootView.setPivotX(this.mDomain.getMainMenuEnterPivotX());
        this.mRootView.setPivotY(this.mDomain.getMainMenuEnterPivotY());
        BasePopupMenuAnimationController.OnMenuStateChangedListener onMenuStateChangedListener = this.mMenuStateChangedListener;
        if (onMenuStateChangedListener != null) {
            onMenuStateChangedListener.onMainMenuStartToExit();
        }
        this.mMainMenuScaleAnimation.getSpring().setResponse(0.3f);
        this.mMainMenuScaleAnimation.getSpring().setBounce(0.0f);
        this.mMainMenuScaleAnimation.setStartValue(this.mMainMenuScaleTransitionProgress);
        this.mMainMenuScaleAnimation.animateToFinalPosition(0.0f);
        if (!flag && this.mMainMenuScaleAnimation.canSkipToEnd()) {
            this.mMainMenuScaleAnimation.skipToEnd();
        }
        this.mMainMenuAlphaAnimation.getSpring().setResponse(0.25f);
        this.mMainMenuAlphaAnimation.getSpring().setBounce(0.0f);
        this.mMainMenuAlphaAnimation.setStartValue(this.mMainMenuAlphaTransitionProgress);
        this.mMainMenuAlphaAnimation.animateToFinalPosition(0.0f);
        if (flag || !this.mMainMenuAlphaAnimation.canSkipToEnd()) {
            return;
        }
        this.mMainMenuAlphaAnimation.skipToEnd();
        // Leapy end
    }

    @Override
    public void startSubMenuEnter(boolean flag) {
        if (this.mMainMenuRoot == null) {
            Log.e(TAG, "No main menu view! Add a main menu view before showing sub menu!");
            return;
        }
        if (this.mSubMenuRoot == null) {
            Log.w(TAG, "No sub menu root view! Set a sub menu view before starting animation!");
            return;
        }
        ensureSubMenuAnimator();
        BasePopupMenuAnimationController.OnMenuStateChangedListener onMenuStateChangedListener = this.mMenuStateChangedListener;
        if (onMenuStateChangedListener != null) {
            onMenuStateChangedListener.onSubMenuStartToEnter();
        }
        this.mSubMenuRoot.setPivotX(this.mDomain.getSubMenuEnterPivotX());
        this.mSubMenuRoot.setPivotY(this.mDomain.getSubMenuEnterPivotY());
        this.mSubMenuAnimation.setStartValue(this.mSubMenuTransitionProgress);
        this.mSubMenuAnimation.animateToFinalPosition(10000.0f);
        if (flag || !this.mSubMenuAnimation.canSkipToEnd()) {
            return;
        }
        this.mSubMenuAnimation.skipToEnd();
    }

    @Override
    public void startSubMenuExit(boolean flag) {
        if (this.mMainMenuRoot == null) {
            Log.e(TAG, "No main menu view! Add a main menu view before showing sub menu!");
            return;
        }
        if (this.mSubMenuRoot == null) {
            Log.w(TAG, "No sub menu root view! Set a sub menu view before starting animation!");
            return;
        }
        BasePopupMenuAnimationController.OnMenuStateChangedListener onMenuStateChangedListener = this.mMenuStateChangedListener;
        if (onMenuStateChangedListener != null) {
            onMenuStateChangedListener.onSubMenuStartToExit();
        }
        ensureSubMenuAnimator();
        this.mSubMenuAnimation.setStartValue(this.mSubMenuTransitionProgress);
        this.mSubMenuAnimation.animateToFinalPosition(0.0f);
        if (flag || !this.mSubMenuAnimation.canSkipToEnd()) {
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
                this.mMainMenuRoot.setAlpha(DEFAULT_ALPHA);
            }
            View view_2 = this.mSubMenuRoot;
            if (view_2 != null && (view_2 instanceof RoundFrameLayout) && ((RoundFrameLayout) view_2).getUseBackgroundBlur()) {
                this.mSubMenuRoot.setAlpha(DEFAULT_ALPHA);
            }
        }
        Animator animator = this.mMainMenuScaleRenderAnimator;
        if (animator != null) {
            animator.cancel();
        }
        Animator animator_2 = this.mMainMenuAlphaRenderAnimator;
        if (animator_2 != null) {
            animator_2.cancel();
        }
        COUISpringAnimation cOUISpringAnimation3 = this.mSubMenuAnimation;
        if (cOUISpringAnimation3 != null) {
            cOUISpringAnimation3.cancel();
            setSubMenuTransitionProgress(0.0f);
        }
    }
}
