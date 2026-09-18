package com.coui.appcompat.tips;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.coui.appcompat.R;
import com.coui.appcompat.cardview.COUICardView;
import com.coui.appcompat.contextutil.COUIContextUtil;
import com.coui.appcompat.darkmode.COUIDarkModeUtil;
import com.coui.appcompat.roundcorner.RoundCornerUtil;

public abstract class COUICustomTopTips extends COUICardView {
    private Animator.AnimatorListener mAnimatorDismissListener;
    private AnimatorSet mAnimatorSetDismiss;
    private AnimatorSet mAnimatorSetShow;
    private Animator.AnimatorListener mAnimatorShowListener;
    private View mView;

    public COUICustomTopTips(Context context) {
        this(context, null);
    }

    public COUICustomTopTips(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public COUICustomTopTips(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void dismissWithAnim() {
        if (mAnimatorSetDismiss == null) {
            ObjectAnimator scale = ObjectAnimator.ofFloat(this, "scaleY", 1.0f, 0.0f);
            scale.setDuration(250L);
            mAnimatorSetDismiss = new AnimatorSet();
            mAnimatorSetDismiss.play(scale);
        }
        mAnimatorSetDismiss.addListener(new ForwardingAnimatorListener(false));
        mAnimatorSetDismiss.start();
    }

    public AnimatorSet getAnimatorSetDismiss() {
        return mAnimatorSetDismiss;
    }

    public AnimatorSet getAnimatorSetShow() {
        return mAnimatorSetShow;
    }

    public View getContentView() {
        return mView;
    }

    public abstract int getContentViewId();

    public void init() {
        COUIDarkModeUtil.setForceDarkAllow(this, false);
        setContentView(getContentViewId());
        if (RoundCornerUtil.isVersionSupport()) {
            setRadius(COUIContextUtil.getAttrDimens(getContext(), R.attr.couiRoundCornerMRadius));
            setWeight(COUIContextUtil.getAttrFloat(getContext(), R.attr.couiRoundCornerMWeight));
        } else {
            setRadius(COUIContextUtil.getAttrDimens(getContext(), R.attr.couiRoundCornerM));
        }
        setCardBackgroundColor(ColorStateList.valueOf(
                COUIContextUtil.getAttrColor(getContext(), R.attr.couiColorFillThin)));
    }

    public void setAnimatorDismissListener(Animator.AnimatorListener listener) {
        mAnimatorDismissListener = listener;
    }

    public void setAnimatorSetDismiss(AnimatorSet animatorSet) {
        mAnimatorSetDismiss = animatorSet;
    }

    public void setAnimatorSetShow(AnimatorSet animatorSet) {
        mAnimatorSetShow = animatorSet;
    }

    public void setAnimatorShowListener(Animator.AnimatorListener listener) {
        mAnimatorShowListener = listener;
    }

    public void setContentView(View view) {
        if (mView != null) {
            throw new RuntimeException("Repeat calls are not allowed!!");
        }
        mView = view;
        addView(view);
    }

    public void setContentView(int layoutId) {
        if (layoutId != 0) {
            setContentView(LayoutInflater.from(getContext()).inflate(layoutId, (ViewGroup) this, false));
        }
    }

    public void showWithAnim() {
        if (mAnimatorSetShow == null) {
            ObjectAnimator scale = ObjectAnimator.ofFloat(this, "scaleY", 0.0f, 1.0f);
            scale.setDuration(250L);
            mAnimatorSetShow = new AnimatorSet();
            mAnimatorSetShow.play(scale);
        }
        mAnimatorSetShow.addListener(new ForwardingAnimatorListener(true));
        mAnimatorSetShow.start();
    }

    private final class ForwardingAnimatorListener implements Animator.AnimatorListener {
        private final boolean mShow;

        ForwardingAnimatorListener(boolean show) {
            mShow = show;
        }

        private Animator.AnimatorListener listener() {
            return mShow ? mAnimatorShowListener : mAnimatorDismissListener;
        }

        @Override public void onAnimationStart(Animator animation) {
            if (listener() != null) listener().onAnimationStart(animation);
        }

        @Override public void onAnimationEnd(Animator animation) {
            if (listener() != null) listener().onAnimationEnd(animation);
        }

        @Override public void onAnimationCancel(Animator animation) {
            if (listener() != null) listener().onAnimationCancel(animation);
        }

        @Override public void onAnimationRepeat(Animator animation) {
            if (listener() != null) listener().onAnimationRepeat(animation);
        }
    }
}
