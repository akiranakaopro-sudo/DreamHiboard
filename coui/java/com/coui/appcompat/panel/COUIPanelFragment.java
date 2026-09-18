package com.coui.appcompat.panel;

import android.R;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import com.coui.appcompat.contextutil.COUIContextUtil;
import com.coui.appcompat.toolbar.COUIToolbar;
import java.io.Serializable;


public class COUIPanelFragment extends Fragment implements Serializable {
    private static final long ENTER_MASK_ALPHA_ANIM_DURATION = 350;
    private static final long EXIT_MASK_ALPHA_ANIM_DURATION = 300;
    private static final long MASK_ALPHA = 255;
    private static final String SAVE_IS_IN_TINY_SCREEN_PANEL_KEY = "SAVE_IS_IN_TINY_SCREEN_PANEL_KEY";
    private static final String SAVE_IS_SHOW_IN_FIRST_PANEL_KEY = "SAVE_IS_SHOW_IN_FIRST_PANEL_KEY";
    private View mContentView;
    private DialogInterface.OnKeyListener mDialogOnKeyListener;
    private COUIPanelDragListener mDragPanelListener;
    private View mDragView;
    protected boolean mIsInTinyScreen;
    private Boolean mIsShowOnFirstPanel = Boolean.FALSE;
    private COUIBottomSheetDialog.OnBackInvokedLocalListener mOnBackInvokedLocalListener;
    private View.OnTouchListener mOutSideViewOnTouchListener;
    private COUIPanelBarView mPanelBarView;
    private PanelFragmentAnimationListener mPanelFragmentAnimationListener;
    private COUIPanelContentLayout mPanelView;
    private View mTitleView;
    private FrameLayout mTitleViewLayout;
    private COUIToolbar mToolbar;
    protected static final int CUSTOM_ANIMATION_ENTER = com.coui.appcompat.R.anim.coui_close_panel_fragment_enter;
    protected static final int CUSTOM_ANIMATION_EXIT = com.coui.appcompat.R.anim.coui_open_panel_fragment_exit;
    private static final Interpolator ENTER_MASK_ALPHA_ANIM_INTERPOLATOR = new PathInterpolator(0.3f, 0.26f, 0.4f, 1.0f);
    private static final Interpolator EXIT_MASK_ALPHA_ANIM_INTERPOLATOR = new PathInterpolator(0.3f, 0.15f, 0.3f, 1.0f);

    public interface PanelFragmentAnimationListener {
        void onAnimationEnd();
    }

    private void ensurePanelView() {
        if (this.mPanelView == null) {
            this.mPanelView = (COUIPanelContentLayout) getLayoutInflater().inflate(this.mIsInTinyScreen ? com.coui.appcompat.R.layout.coui_panel_view_layout_tiny : com.coui.appcompat.R.layout.coui_panel_view_layout, null);
        }
    }

    private void setBottomButtonBar(String leftText, View.OnClickListener leftClickListener, String centerText, View.OnClickListener centerClickListener, String rightText, View.OnClickListener rightClickListener) {
        ensurePanelView();
        this.mPanelView.setUpBottomBar(true, leftText, leftClickListener, centerText, centerClickListener, rightText, rightClickListener);
    }

    public Button getCenterButton() {
        COUIPanelContentLayout panelView = this.mPanelView;
        if (panelView != null) {
            return panelView.findViewById(R.id.button3);
        }
        return null;
    }

    public int getContentResId() {
        return com.coui.appcompat.R.id.panel_container;
    }

    public View getContentView() {
        return this.mContentView;
    }

    public DialogInterface.OnKeyListener getDialogOnKeyListener() {
        return this.mDialogOnKeyListener;
    }

    public COUIPanelDragListener getDragPanelListener() {
        return this.mDragPanelListener;
    }

    public View getDragView() {
        return this.mDragView;
    }

    public int getDragViewHeight() {
        View view = this.mDragView;
        if (view != null) {
            return view.getHeight();
        }
        return 0;
    }

    public COUIPanelContentLayout getDraggableLinearLayout() {
        return this.mPanelView;
    }

    public Button getLeftButton() {
        COUIPanelContentLayout panelView = this.mPanelView;
        if (panelView != null) {
            return panelView.findViewById(R.id.button2);
        }
        return null;
    }

    public COUIBottomSheetDialog.OnBackInvokedLocalListener getOnBackInvokedLocalListener() {
        return this.mOnBackInvokedLocalListener;
    }

    public View.OnTouchListener getOutSideViewOnTouchListener() {
        return this.mOutSideViewOnTouchListener;
    }

    public COUIPanelBarView getPanelBarView() {
        return this.mPanelBarView;
    }

    public Button getRightButton() {
        COUIPanelContentLayout panelView = this.mPanelView;
        if (panelView != null) {
            return panelView.findViewById(R.id.button1);
        }
        return null;
    }

    public Boolean getShowOnFirstPanel() {
        return this.mIsShowOnFirstPanel;
    }

    public View getTitleView() {
        return this.mTitleView;
    }

    public COUIToolbar getToolbar() {
        return this.mToolbar;
    }

    public int getToolbarHeight() {
        COUIToolbar toolbar = this.mToolbar;
        if (toolbar != null) {
            return toolbar.getHeight();
        }
        return 0;
    }

    public void hideDragView() {
        ViewGroup.LayoutParams dragViewLayoutParams = getDragView().getLayoutParams();
        dragViewLayoutParams.height = getDragView().getContext().getResources().getDimensionPixelSize(com.coui.appcompat.R.dimen.coui_panel_drag_view_hide_height);
        getDragView().setVisibility(View.INVISIBLE);
        getDragView().setLayoutParams(dragViewLayoutParams);
    }

    public void initView(View view) {
    }

    public void onAbandon(Boolean bool) {
        setPanelDragListener(null);
        setDialogOnKeyListener(null);
        setOnBackInvokedLocalListener(null);
        setOutSideViewOnTouchListener(null);
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        ensurePanelView();
        if (bundle != null) {
            this.mIsShowOnFirstPanel = bundle.getBoolean(SAVE_IS_SHOW_IN_FIRST_PANEL_KEY, false);
            if (getParentFragment() instanceof COUIBottomSheetDialogFragment) {
                ((COUIBottomSheetDialogFragment) getParentFragment()).setPanelFragment(this, this.mIsShowOnFirstPanel);
            }
        }
        initView(this.mPanelView);
    }

    public void onAdd(Boolean bool) {
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        ValueAnimator maskAlphaAnimator;
        Animation animationLoadAnimation;
        final Drawable drawable = AppCompatResources.getDrawable(requireContext(), com.coui.appcompat.R.drawable.coui_default_panel_bg_without_shadow);
        drawable.setTint(COUIContextUtil.getColor(requireContext(), com.coui.appcompat.R.color.coui_color_mask));
        drawable.setAlpha(0);
        if (nextAnim == CUSTOM_ANIMATION_EXIT) {
            maskAlphaAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
            maskAlphaAnimator.setDuration(EXIT_MASK_ALPHA_ANIM_DURATION);
            maskAlphaAnimator.setInterpolator(EXIT_MASK_ALPHA_ANIM_INTERPOLATOR);

// android.animation.ValueAnimator.AnimatorUpdateListener
            maskAlphaAnimator.addUpdateListener(valueAnimator -> drawable.setAlpha((int) ((Float) valueAnimator.getAnimatedValue() * MASK_ALPHA)));
            animationLoadAnimation = AnimationUtils.loadAnimation(getContext(), nextAnim);
        } else {
            maskAlphaAnimator = null;
            animationLoadAnimation = null;
        }
        if (nextAnim == CUSTOM_ANIMATION_ENTER) {
            maskAlphaAnimator = ValueAnimator.ofFloat(1.0f, 0.0f);
            maskAlphaAnimator.setDuration(ENTER_MASK_ALPHA_ANIM_DURATION);
            maskAlphaAnimator.setInterpolator(ENTER_MASK_ALPHA_ANIM_INTERPOLATOR);

// android.animation.ValueAnimator.AnimatorUpdateListener
            maskAlphaAnimator.addUpdateListener(valueAnimator -> drawable.setAlpha((int) (((Float) valueAnimator.getAnimatedValue()).floatValue() * MASK_ALPHA)));
            maskAlphaAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animator) {
                    super.onAnimationEnd(animator);
                    if (COUIPanelFragment.this.mPanelFragmentAnimationListener != null) {
                        COUIPanelFragment.this.mPanelFragmentAnimationListener.onAnimationEnd();
                    }
                }
            });
            animationLoadAnimation = AnimationUtils.loadAnimation(getContext(), nextAnim);
        }
        if (maskAlphaAnimator == null || animationLoadAnimation == null) {
            return super.onCreateAnimation(transit, enter, nextAnim);
        }
        final ValueAnimator finalMaskAlphaAnimator = maskAlphaAnimator;
        animationLoadAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                if (COUIPanelFragment.this.mPanelView != null) {
                    COUIPanelFragment.this.mPanelView.setForeground(null);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationStart(Animation animation) {
                if (COUIPanelFragment.this.mPanelView != null) {
                    COUIPanelFragment.this.mPanelView.setForeground(drawable);
                }
                finalMaskAlphaAnimator.start();
            }
        });
        return animationLoadAnimation;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        if (bundle != null) {
            this.mIsInTinyScreen = bundle.getBoolean(SAVE_IS_IN_TINY_SCREEN_PANEL_KEY, false);
        }
        COUIPanelContentLayout panelView = (COUIPanelContentLayout) getLayoutInflater().inflate(this.mIsInTinyScreen ? com.coui.appcompat.R.layout.coui_panel_view_layout_tiny : com.coui.appcompat.R.layout.coui_panel_view_layout, null);
        this.mPanelView = panelView;
        panelView.setLayoutParams(new ViewGroup.LayoutParams(-1, -2));
        this.mDragView = this.mPanelView.getDragView();
        this.mPanelBarView = this.mPanelView.getPanelBarView();
        View contentRoot = layoutInflater.inflate(com.coui.appcompat.R.layout.coui_panel_layout, viewGroup, false);
        this.mToolbar = contentRoot.findViewById(com.coui.appcompat.R.id.bottom_sheet_toolbar);
        this.mTitleViewLayout = contentRoot.findViewById(com.coui.appcompat.R.id.title_view_container);
        this.mContentView = contentRoot.findViewById(getContentResId());
        this.mPanelView.addContentView(contentRoot);
        return this.mPanelView;
    }

    public void onHide(Boolean bool) {
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putBoolean(SAVE_IS_SHOW_IN_FIRST_PANEL_KEY, this.mIsShowOnFirstPanel);
        bundle.putBoolean(SAVE_IS_IN_TINY_SCREEN_PANEL_KEY, this.mIsInTinyScreen);
    }

    public void onShow(Boolean bool) {
    }

    public void setContentView(View view) {
        this.mContentView = view;
    }

    public void setDialogOnKeyListener(DialogInterface.OnKeyListener onKeyListener) {
        this.mDialogOnKeyListener = onKeyListener;
    }

    public void setIsInTinyScreen(boolean isInTinyScreen) {
        this.mIsInTinyScreen = isInTinyScreen;
    }

    public void setOnBackInvokedLocalListener(COUIBottomSheetDialog.OnBackInvokedLocalListener onBackInvokedLocalListener) {
        this.mOnBackInvokedLocalListener = onBackInvokedLocalListener;
    }

    public void setOutSideViewOnTouchListener(View.OnTouchListener onTouchListener) {
        this.mOutSideViewOnTouchListener = onTouchListener;
    }

    public void setPanelDragListener(COUIPanelDragListener cOUIPanelDragListener) {
        this.mDragPanelListener = cOUIPanelDragListener;
    }

    public void setPanelFragmentAnimationListener(PanelFragmentAnimationListener panelFragmentAnimationListener) {
        this.mPanelFragmentAnimationListener = panelFragmentAnimationListener;
    }

    public void setShowOnFirstPanel(Boolean bool) {
        this.mIsShowOnFirstPanel = bool;
    }

    public void setTitleView(int layoutResId) {
        if (layoutResId > 0) {
            setTitleView(LayoutInflater.from(getContext()).inflate(layoutResId, this.mTitleViewLayout, false));
        }
    }

    public void setToolbar(COUIToolbar toolbar) {
        if (toolbar == null || this.mToolbar == null) {
            return;
        }
        this.mTitleViewLayout.setVisibility(View.GONE);
        this.mToolbar.setVisibility(View.VISIBLE);
        this.mToolbar = toolbar;
    }

    public void showDragView() {
        ViewGroup.LayoutParams dragViewLayoutParams = getDragView().getLayoutParams();
        dragViewLayoutParams.height = getDragView().getContext().getResources().getDimensionPixelSize(com.coui.appcompat.R.dimen.coui_panel_drag_view_height);
        getDragView().setVisibility(View.VISIBLE);
        getDragView().setLayoutParams(dragViewLayoutParams);
    }

    public void setTitleView(View view) {
        this.mTitleView = view;
        if (this.mTitleViewLayout == null || view == null || view.getVisibility() == View.GONE) {
            return;
        }
        this.mToolbar.setVisibility(View.GONE);
        this.mTitleViewLayout.setVisibility(View.VISIBLE);
        this.mTitleView = view;
        this.mTitleViewLayout.addView(view);
    }
}
