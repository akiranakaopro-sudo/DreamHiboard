package com.coui.appcompat.progressbar;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import com.airbnb.lottie.LottieAnimationView;
import com.coui.appcompat.R;
import com.coui.appcompat.darkmode.COUIDarkModeUtil;

public class COUILottieLoadingView extends FrameLayout {
    // Leapy modified 2026-07-26: Back the COUI compatibility wrapper with
    // upstream Lottie so it renders the decoded OPPO JSON instead of a fake arc.
    private final LottieAnimationView loadingView;
    private String animationName;

    public COUILottieLoadingView(Context context) {
        this(context, null);
    }

    public COUILottieLoadingView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.couiLottieLoadingViewStyle);
    }

    public COUILottieLoadingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        COUIDarkModeUtil.setForceDarkAllow(this, false);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.COUILottieLoadingView,
                defStyleAttr, 0);
        int width = a.getDimensionPixelSize(
                R.styleable.COUILottieLoadingView_couiLottieLoadingViewWidth,
                getResources().getDimensionPixelOffset(R.dimen.coui_lottie_loading_view_large_width));
        int height = a.getDimensionPixelSize(
                R.styleable.COUILottieLoadingView_couiLottieLoadingViewHeight,
                getResources().getDimensionPixelOffset(R.dimen.coui_lottie_loading_view_large_height));
        animationName = a.getString(R.styleable.COUILottieLoadingView_couiLottieLoadingJsonName);
        if (animationName == null) {
            animationName = getResources().getString(R.string.coui_lottie_loading_large_json);
        }
        a.recycle();
        loadingView = new LottieAnimationView(context);
        loadingView.setRepeatCount(ValueAnimator.INFINITE);
        if (!TextUtils.isEmpty(animationName)) {
            loadingView.setAnimation(animationName);
        }
        // Leapy added 2026-07-26: Preserve OPPO's repeat-boundary visibility
        // guard so an off-screen infinite animation does not keep rendering.
        loadingView.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                if (getVisibility() != VISIBLE
                        || loadingView.getVisibility() != VISIBLE
                        || getWindowVisibility() != VISIBLE) {
                    pauseAnimation();
                }
            }
        });
        LayoutParams lp = new LayoutParams(width, height);
        lp.gravity = android.view.Gravity.CENTER;
        addView(loadingView, lp);
    }

    public void pauseAnimation() {
        if (loadingView.isAnimating()) {
            loadingView.pauseAnimation();
        }
    }

    private void resumeAnimation() {
        if (!loadingView.isAnimating() && getVisibility() == VISIBLE
                && getWindowVisibility() == VISIBLE) {
            loadingView.resumeAnimation();
        }
    }

    public LottieAnimationView getLoadingView() {
        return loadingView;
    }

    public String getAnimationName() {
        return animationName;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        resumeAnimation();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        pauseAnimation();
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (getVisibility() == VISIBLE) {
            resumeAnimation();
        } else {
            pauseAnimation();
        }
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == VISIBLE) {
            resumeAnimation();
        } else {
            pauseAnimation();
        }
    }

    public void cancelAnimation() {
        loadingView.cancelAnimation();
    }

    public void playAnimation() {
        loadingView.playAnimation();
    }

    public void setAnimation(String animationName) {
        this.animationName = animationName;
        if (!TextUtils.isEmpty(animationName)) {
            loadingView.setAnimation(animationName);
        }
    }
}
