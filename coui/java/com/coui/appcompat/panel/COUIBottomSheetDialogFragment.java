package com.coui.appcompat.panel;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.coui.appcompat.log.COUILog;
import com.coui.appcompat.panel.COUIBottomSheetDialog;
import com.coui.appcompat.panel.COUIPanelFragment;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;


public class COUIBottomSheetDialogFragment extends BottomSheetDialogFragment {
    private static final float PHYSICS_UNSET = Float.MIN_VALUE;
    private static final String SAVE_CAN_PULL_UP_KEY = "SAVE_CAN_PULL_UP_KEY_DRAGGABLE_KEY";
    private static final String SAVE_DRAGGABLE_KEY = "SAVE_DRAGGABLE_KEY";
    private static final String SAVE_FIRST_SHOW_COLLAPSED_KEY = "SAVE_FIRST_SHOW_COLLAPSED_KEY";
    private static final String SAVE_FRAME_RATE_KEY = "SAVE_FRAME_RATE_KEY";
    private static final String SAVE_HAS_SET_PEEK_HEIGHT_KEY = "SAVE_HAS_SET_PEEK_HEIGHT_KEY";
    private static final String SAVE_HAS_SET_SKIP_COLLAPSED_KEY = "SAVE_HAS_SET_SKIP_COLLAPSED_KEY";
    private static final String SAVE_IS_HANDLE_PANEL_KEY = "SAVE_IS_HANDLE_PANEL_KEY";
    private static final String SAVE_IS_IN_TINY_SCREEN_PANEL_KEY = "SAVE_IS_IN_TINY_SCREEN_PANEL_KEY";
    private static final String SAVE_PANEL_EDGE_TO_EDGE = "SAVE_PANEL_EDGE_TO_EDGE";
    private static final String SAVE_PANEL_HEIGHT_KEY = "SAVE_PANEL_HEIGHT_KEY";
    private static final String SAVE_PANEL_WIDTH_KEY = "SAVE_PANEL_WIDTH_KEY";
    private static final String SAVE_PEEK_HEIGHT_KEY = "SAVE_PEEK_HEIGHT_KEY";
    private static final String SAVE_REGISTER_CONFIGURATION_KEY = "SAVE_REGISTER_CONFIGURATION_KEY";
    private static final String SAVE_SHOW_IN_MAX_HEIGHT_KEY = "SAVE_SHOW_IN_MAX_HEIGHT_KEY";
    private static final String SAVE_SKIP_COLLAPSED_KEY = "SAVE_SKIP_COLLAPSED_KEY";
    private static final String SAVE_SUPPORT_EXIT_BLOCKING_ANIMATION = "SAVE_SUPPORT_EXIT_BLOCKING_ANIMATION";
    private static final String SAVE_USE_NORMAL_SMOOTH_CORNER = "SAVE_USE_NORMAL_SMOOTH_CORNER";
    private static final String TAG = "COUIBottomSheetDialogFragment";
    private static final int UNSET_WIDTH = -1;

    @Deprecated
    private long mAlphaAnimDuration;
    private View mAnchorView;
    private BottomSheetBehavior<FrameLayout> mBehavior;
    private COUIBottomSheetDialog mBottomSheetDialog;
    private COUIBottomSheetDialog.BottomSheetDialogAnimatorListener mBottomSheetDialogAnimatorListener;
    private boolean mCanPullUp;
    private boolean mCouiPanelEdgeToEdgeEnable;
    private COUIPanelFragment mCurrentPanelFragment;
    private int mCurrentPanelHeight;
    private View mDialogFragmentView;
    private boolean mDisableSubExpand;

    @Deprecated
    private int mFinalNavColorAfterDismiss;
    private boolean mFirstShowCollapsed;
    private boolean mFrameRate;
    private boolean mGlobalDrag;
    private boolean mHasSetPeekHeight;
    private boolean mHasSetSkipCollapsed;
    private int mHideDragViewHeight;
    private InputMethodManager mInputMethodManager;
    private boolean mIsDraggable;

    @Deprecated
    private boolean mIsExecuteNavColorAnimAfterDismiss;
    private boolean mIsFullScreenInTinyScreen;
    private boolean mIsGestureNavigation;
    private boolean mIsHandlePanel;
    private boolean mIsInTinyScreen;
    private boolean mIsSavedInstanceState;
    private boolean mIsShowInMaxHeight;
    private OnDismissListener mOnDismissListener;
    private View mOutSideView;
    private ViewGroup mPanelContainer;
    private int mPanelHeight;
    private int mPanelWidth;
    private int mPeekHeight;
    private float mPhysicsDampingRatio;
    private float mPhysicsFrequency;
    private int mPreferWidth;
    private boolean mRegisterConfigurationChangeCallBack;
    private boolean mShouldRegisterWindowInsetsListener;
    private boolean mSkipCollapsed;
    private boolean mSupportExitBlockingAnimation;
    private boolean mUseNormalSmoothCorner;

    public interface OnDismissListener {
        void onDismiss();
    }

    public COUIBottomSheetDialogFragment() {
        this.mAlphaAnimDuration = 100L;
        this.mIsSavedInstanceState = false;
        this.mPeekHeight = 0;
        this.mSkipCollapsed = true;
        this.mFirstShowCollapsed = false;
        this.mIsDraggable = true;
        this.mCanPullUp = true;
        this.mIsShowInMaxHeight = false;
        this.mRegisterConfigurationChangeCallBack = true;
        this.mPhysicsFrequency = PHYSICS_UNSET;
        this.mPhysicsDampingRatio = PHYSICS_UNSET;
        this.mAnchorView = null;
        this.mBottomSheetDialogAnimatorListener = null;
        this.mDisableSubExpand = false;
        this.mShouldRegisterWindowInsetsListener = true;
        this.mPreferWidth = -1;
        this.mHasSetPeekHeight = false;
        this.mHasSetSkipCollapsed = false;
        this.mIsHandlePanel = false;
        this.mHideDragViewHeight = 0;
        this.mFrameRate = true;
    }

    private int getFragmentHeight(Fragment fragment) {
        if (fragment == null || fragment.getView() == null) {
            return 0;
        }
        return fragment.getView().getHeight();
    }

    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = this.mInputMethodManager;
        if (inputMethodManager == null || !inputMethodManager.isActive()) {
            return;
        }
        this.mInputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void initBottomSheetDialogSize() {
        int panelWidth = this.mPanelWidth;
        if (panelWidth != 0) {
            this.mBottomSheetDialog.setWidth(panelWidth);
        }
        int panelHeight = this.mPanelHeight;
        if (panelHeight != 0) {
            this.mBottomSheetDialog.setHeight(panelHeight);
            setCurrentPanelHeight(this.mPanelHeight);
        }
    }

    private void initFragment() {
        if (this.mCurrentPanelFragment != null) {
            if (!this.mIsSavedInstanceState) {
                getChildFragmentManager().beginTransaction().replace(com.coui.appcompat.R.id.first_panel_container, this.mCurrentPanelFragment).commit();
            }
            COUIPanelFragment firstPanelFragment = this.mCurrentPanelFragment;
            Boolean showOnFirstPanel = Boolean.TRUE;
            firstPanelFragment.setShowOnFirstPanel(showOnFirstPanel);
            this.mCurrentPanelFragment.onAdd(showOnFirstPanel);
            setUpViewHeight(this.mPanelContainer, this.mIsShowInMaxHeight);
        }
        this.mPanelContainer.post(new Runnable() {
            @Override
            public void run() {
                if (COUIBottomSheetDialogFragment.this.mCurrentPanelFragment == null) {
                    return;
                }
                COUIBottomSheetDialogFragment fragment = COUIBottomSheetDialogFragment.this;
                fragment.mOutSideView = fragment.mBottomSheetDialog.findViewById(com.coui.appcompat.R.id.touch_outside);
                if (COUIBottomSheetDialogFragment.this.mOutSideView != null) {
                    COUIBottomSheetDialogFragment.this.mOutSideView.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent) {
                            if (motionEvent != null && motionEvent.getAction() == 1) {
                                COUIBottomSheetDialogFragment.this.mBottomSheetDialog.dismiss();
                            }
                            return true;
                        }
                    });
                }
                COUIBottomSheetDialogFragment.this.mIsSavedInstanceState = false;
                COUIBottomSheetDialogFragment cOUIBottomSheetDialogFragment2 = COUIBottomSheetDialogFragment.this;
                cOUIBottomSheetDialogFragment2.setPanelListener(cOUIBottomSheetDialogFragment2.mCurrentPanelFragment);
                COUIBottomSheetDialogFragment.this.mBottomSheetDialog.setDragableLinearLayout(COUIBottomSheetDialogFragment.this.mCurrentPanelFragment.getDraggableLinearLayout(), false);
                COUIBottomSheetDialogFragment.this.mCurrentPanelFragment.onShow(Boolean.TRUE);
            }
        });
    }

    private void preShowPanel(COUIPanelFragment panelFragment) {
        if (getChildFragmentManager().getFragments().contains(panelFragment) || panelFragment.isAdded()) {
            getChildFragmentManager().beginTransaction().setCustomAnimations(com.coui.appcompat.R.anim.coui_open_panel_fragment_enter, COUIPanelFragment.CUSTOM_ANIMATION_EXIT, com.coui.appcompat.R.anim.coui_close_panel_fragment_enter, com.coui.appcompat.R.anim.coui_close_panel_fragment_exit).hide(this.mCurrentPanelFragment).show(panelFragment).addToBackStack(null).commitAllowingStateLoss();
        } else {
            getChildFragmentManager().beginTransaction().setCustomAnimations(com.coui.appcompat.R.anim.coui_open_panel_fragment_enter, COUIPanelFragment.CUSTOM_ANIMATION_EXIT, com.coui.appcompat.R.anim.coui_close_panel_fragment_enter, com.coui.appcompat.R.anim.coui_close_panel_fragment_exit).hide(this.mCurrentPanelFragment).add(com.coui.appcompat.R.id.first_panel_container, panelFragment).commitAllowingStateLoss();
            panelFragment.onAdd(Boolean.FALSE);
        }
        getChildFragmentManager().executePendingTransactions();
        if (!panelFragment.isAdded() || panelFragment.getView() == null) {
            COUILog.e(TAG, "isAdded:" + panelFragment.isAdded() + ",view:" + panelFragment.getView());
            return;
        }
        if (this.mBottomSheetDialog.getAdjustResizeHelper() != null) {
            this.mBottomSheetDialog.getAdjustResizeHelper().recoveryScrollingParentViewPaddingBottom(this.mCurrentPanelFragment.getDraggableLinearLayout());
        }
        COUIPanelFragment previousPanelFragment = this.mCurrentPanelFragment;
        previousPanelFragment.onHide(previousPanelFragment.getShowOnFirstPanel());
        this.mCurrentPanelFragment = panelFragment;
        this.mBottomSheetDialog.setDragableLinearLayout(panelFragment.getDraggableLinearLayout(), true);
        this.mCurrentPanelFragment.onShow(Boolean.FALSE);
        setPanelListener(this.mCurrentPanelFragment);
    }

    private void setDialogOnKeyListener(DialogInterface.OnKeyListener onKeyListener) {
        COUIBottomSheetDialog bottomSheetDialog = this.mBottomSheetDialog;
        if (bottomSheetDialog != null) {
            bottomSheetDialog.setOnKeyListener(onKeyListener);
        }
    }

    private void setOnBackInvokedLocalListener(COUIBottomSheetDialog.OnBackInvokedLocalListener onBackInvokedLocalListener) {
        COUIBottomSheetDialog bottomSheetDialog = this.mBottomSheetDialog;
        if (bottomSheetDialog != null) {
            bottomSheetDialog.setOnBackInvokedLocalListener(onBackInvokedLocalListener);
        }
    }

    private void setOnTouchOutSideViewListener(View.OnTouchListener onTouchListener) {
        COUIBottomSheetDialog bottomSheetDialog = this.mBottomSheetDialog;
        if (bottomSheetDialog != null) {
            bottomSheetDialog.setOutSideViewTouchListener(onTouchListener);
        }
    }

    private void setPanelDragListener(COUIPanelDragListener panelDragListener) {
        COUIBottomSheetDialog bottomSheetDialog = this.mBottomSheetDialog;
        if (bottomSheetDialog == null || !(bottomSheetDialog.getBehavior() instanceof COUIBottomSheetBehavior)) {
            return;
        }
        ((COUIBottomSheetBehavior) this.mBottomSheetDialog.getBehavior()).setPanelDragListener(panelDragListener);
    }

    private void setPanelListener(COUIPanelFragment panelFragment) {
        if (panelFragment != null) {
            setPanelDragListener(panelFragment.getDragPanelListener());
            setOnTouchOutSideViewListener(panelFragment.getOutSideViewOnTouchListener());
            setDialogOnKeyListener(panelFragment.getDialogOnKeyListener());
            setOnBackInvokedLocalListener(panelFragment.getOnBackInvokedLocalListener());
        }
    }

    private void setUpViewHeight(View view, boolean showInMaxHeight) {
        if (view != null) {
            int viewHeight = (showInMaxHeight || this.mPanelHeight != 0) ? -1 : -2;
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.height = viewHeight;
            view.setLayoutParams(layoutParams);
        }
    }

    public void backToFirstPanel() {
        if (this.mCurrentPanelFragment != null) {
            setCancelable(false);
            hideKeyboard(this.mDialogFragmentView);
            int currentPanelIndex = getChildFragmentManager().getFragments().indexOf(this.mCurrentPanelFragment);
            final COUIPanelFragment abandonedPanelFragment = this.mCurrentPanelFragment;
            if (currentPanelIndex > 0) {
                COUIPanelFragment previousPanelFragment = (COUIPanelFragment) getChildFragmentManager().getFragments().get(currentPanelIndex - 1);
                getChildFragmentManager().beginTransaction().setCustomAnimations(COUIPanelFragment.CUSTOM_ANIMATION_ENTER, com.coui.appcompat.R.anim.coui_close_panel_fragment_exit).hide(this.mCurrentPanelFragment).show(previousPanelFragment).commit();
                COUIPanelContentLayout draggableLinearLayout = this.mCurrentPanelFragment.getDraggableLinearLayout();
                if (this.mBottomSheetDialog.getAdjustResizeHelper() != null) {
                    this.mBottomSheetDialog.getAdjustResizeHelper().recoveryScrollingParentViewPaddingBottom(draggableLinearLayout);
                }
                this.mCurrentPanelFragment.onHide(Boolean.FALSE);
                this.mCurrentPanelFragment = previousPanelFragment;
                this.mBottomSheetDialog.setDragableLinearLayout(draggableLinearLayout, true);
                COUIPanelFragment currentPanelFragment = this.mCurrentPanelFragment;
                currentPanelFragment.onShow(currentPanelFragment.getShowOnFirstPanel());
                setPanelListener(this.mCurrentPanelFragment);
                this.mCurrentPanelFragment.setPanelFragmentAnimationListener(new COUIPanelFragment.PanelFragmentAnimationListener() {
                    @Override
                    public void onAnimationEnd() {
                        if (abandonedPanelFragment.isAdded()) {
                            abandonedPanelFragment.onAbandon(Boolean.FALSE);
                            COUIBottomSheetDialogFragment.this.getChildFragmentManager().beginTransaction().remove(abandonedPanelFragment).commitNowAllowingStateLoss();
                        }
                    }
                });
            }
            this.mPanelContainer.post(new Runnable() {
                @Override
                public void run() {
                    COUIBottomSheetDialogFragment.this.setCancelable(true);
                }
            });
        }
    }

    public void delPreferWidth() {
        this.mPreferWidth = -1;
        COUIBottomSheetDialog bottomSheetDialog = this.mBottomSheetDialog;
        if (bottomSheetDialog != null) {
            bottomSheetDialog.delPreferWidth();
        }
        Log.d(TAG, "delPreferWidth");
    }

    @Override
    public void dismiss() {
        COUIBottomSheetDialog bottomSheetDialog = this.mBottomSheetDialog;
        if (bottomSheetDialog != null) {
            bottomSheetDialog.dismiss();
            if (this.mPreferWidth != -1) {
                this.mBottomSheetDialog.delPreferWidth();
                return;
            }
            return;
        }
        try {
            super.dismiss();
        } catch (Exception e2) {
            Log.e(TAG, e2.getMessage(), e2);
        }
    }

    public void doFeedbackAnimation() {
        COUIBottomSheetDialog bottomSheetDialog = this.mBottomSheetDialog;
        if (bottomSheetDialog != null) {
            bottomSheetDialog.doFeedbackAnimation();
        }
    }

    @Deprecated
    public long getAlphaAnimDuration() {
        return this.mAlphaAnimDuration;
    }

    public COUIBottomSheetDialog getBottomSheetDialog() {
        return this.mBottomSheetDialog;
    }

    public int getCurrentPanelHeight() {
        return this.mCurrentPanelHeight;
    }

    public boolean getIsHandlePanel() {
        return this.mIsHandlePanel;
    }

    public int getPeekHeight() {
        return this.mPeekHeight;
    }

    public boolean isFirstShowCollapsed() {
        return this.mFirstShowCollapsed;
    }

    public boolean isSkipCollapsed() {
        return this.mSkipCollapsed;
    }

    @Override
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        if (this.mBottomSheetDialog == null || this.mCurrentPanelHeight == 0 || getContext() == null) {
            return;
        }
        this.mBottomSheetDialog.setHeight(Math.min(this.mCurrentPanelHeight, COUIPanelMultiWindowUtils.getPanelMaxHeight(getContext(), configuration)));
        this.mBottomSheetDialog.updateLayoutWhileConfigChange(configuration);
    }

    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        if (bundle != null) {
            this.mIsSavedInstanceState = true;
            this.mIsInTinyScreen = bundle.getBoolean(SAVE_IS_IN_TINY_SCREEN_PANEL_KEY, false);
            this.mIsDraggable = bundle.getBoolean(SAVE_DRAGGABLE_KEY, true);
            this.mPeekHeight = bundle.getInt(SAVE_PEEK_HEIGHT_KEY, 0);
            this.mSkipCollapsed = bundle.getBoolean(SAVE_SKIP_COLLAPSED_KEY, true);
            this.mFirstShowCollapsed = bundle.getBoolean(SAVE_FIRST_SHOW_COLLAPSED_KEY, false);
            this.mCanPullUp = bundle.getBoolean(SAVE_CAN_PULL_UP_KEY, true);
            this.mIsShowInMaxHeight = bundle.getBoolean(SAVE_SHOW_IN_MAX_HEIGHT_KEY, false);
            this.mRegisterConfigurationChangeCallBack = bundle.getBoolean(SAVE_REGISTER_CONFIGURATION_KEY, true);
            this.mIsHandlePanel = bundle.getBoolean(SAVE_IS_HANDLE_PANEL_KEY, false);
            this.mHasSetPeekHeight = bundle.getBoolean(SAVE_HAS_SET_PEEK_HEIGHT_KEY, false);
            this.mHasSetSkipCollapsed = bundle.getBoolean(SAVE_HAS_SET_SKIP_COLLAPSED_KEY, false);
            this.mFrameRate = bundle.getBoolean(SAVE_FRAME_RATE_KEY, true);
            this.mCouiPanelEdgeToEdgeEnable = bundle.getBoolean(SAVE_PANEL_EDGE_TO_EDGE, false);
            this.mSupportExitBlockingAnimation = bundle.getBoolean(SAVE_SUPPORT_EXIT_BLOCKING_ANIMATION, false);
            this.mUseNormalSmoothCorner = bundle.getBoolean(SAVE_USE_NORMAL_SMOOTH_CORNER, false);
        }
        boolean zIsGestureNavigation = COUINavigationBarUtil.isGestureNavigation(requireContext());
        this.mIsGestureNavigation = zIsGestureNavigation;
        if (this.mIsHandlePanel) {
            if (!this.mHasSetPeekHeight) {
                if (zIsGestureNavigation) {
                    this.mPeekHeight = getContext().getResources().getDimensionPixelOffset(com.coui.appcompat.R.dimen.coui_panel_default_peek_height_in_gesture);
                } else {
                    this.mPeekHeight = getContext().getResources().getDimensionPixelOffset(com.coui.appcompat.R.dimen.coui_panel_default_peek_height);
                }
            }
            if (!this.mHasSetSkipCollapsed) {
                this.mSkipCollapsed = false;
            }
        }
        if (getActivity() != null) {
            COUIBottomSheetDialog bottomSheetDialog = new COUIBottomSheetDialog(getActivity(), com.coui.appcompat.R.style.DefaultBottomSheetDialog, this.mPhysicsFrequency, this.mPhysicsDampingRatio);
            this.mBottomSheetDialog = bottomSheetDialog;
            View view = this.mAnchorView;
            if (view != null) {
                bottomSheetDialog.setAnchorView(view);
            }
            this.mBottomSheetDialog.setIsInTinyScreen(this.mIsInTinyScreen, this.mIsFullScreenInTinyScreen);
            this.mBottomSheetDialog.setDisableSubExpand(this.mDisableSubExpand);
            this.mBottomSheetDialog.setBottomSheetDialogAnimatorListener(this.mBottomSheetDialogAnimatorListener);
        }
        this.mBottomSheetDialog.setShouldRegisterWindowInsetsListener(this.mShouldRegisterWindowInsetsListener);
        this.mBottomSheetDialog.setShowInDialogFragment(true);
        this.mBottomSheetDialog.setPeekHeight(this.mPeekHeight);
        this.mBottomSheetDialog.setIsHandlePanel(this.mIsHandlePanel);
        this.mBottomSheetDialog.setSkipCollapsed(this.mSkipCollapsed);
        this.mBottomSheetDialog.setFirstShowCollapsed(this.mFirstShowCollapsed);
        this.mBottomSheetDialog.setCanPullUp(this.mCanPullUp);
        this.mBottomSheetDialog.setIsShowInMaxHeight(this.mIsShowInMaxHeight);
        this.mBottomSheetDialog.setFrameRate(this.mFrameRate);
        this.mBottomSheetDialog.setCouiPanelEdgeToEdgeEnable(this.mCouiPanelEdgeToEdgeEnable);
        this.mBottomSheetDialog.setSupportExitBlockingAnimation(this.mSupportExitBlockingAnimation);
        this.mBottomSheetDialog.setUseNormalSmoothCorner(this.mUseNormalSmoothCorner);
        this.mBottomSheetDialog.setRegisterConfigurationChangeCallBack(this.mRegisterConfigurationChangeCallBack);
        this.mBottomSheetDialog.setHideDragViewHeight(this.mHideDragViewHeight);
        int preferWidth = this.mPreferWidth;
        if (preferWidth != -1) {
            this.mBottomSheetDialog.setPreferWidth(preferWidth);
        }
        initBottomSheetDialogSize();
        BottomSheetBehavior<FrameLayout> behavior = this.mBottomSheetDialog.getBehavior();
        this.mBehavior = behavior;
        behavior.setDraggable(this.mIsDraggable);
        BottomSheetBehavior<FrameLayout> bottomSheetBehavior = this.mBehavior;
        if (bottomSheetBehavior instanceof COUIBottomSheetBehavior) {
            ((COUIBottomSheetBehavior) bottomSheetBehavior).setGlobalDrag(this.mGlobalDrag);
        }
        return this.mBottomSheetDialog;
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        if (this.mIsShowInMaxHeight) {
            this.mDialogFragmentView = View.inflate(getActivity(), com.coui.appcompat.R.layout.coui_bottom_sheet_dialog_max_height, null);
        } else {
            this.mDialogFragmentView = View.inflate(getActivity(), com.coui.appcompat.R.layout.coui_bottom_sheet_dialog, null);
        }
        return this.mDialogFragmentView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        COUIPanelFragment currentPanelFragment = this.mCurrentPanelFragment;
        if (currentPanelFragment != null) {
            currentPanelFragment.onAbandon(currentPanelFragment.getShowOnFirstPanel());
        }
        COUIBottomSheetDialog bottomSheetDialog = this.mBottomSheetDialog;
        if (bottomSheetDialog != null) {
            bottomSheetDialog.setOnKeyListener(null);
            this.mBottomSheetDialog.setOnBackInvokedLocalListener(null);
            this.mBottomSheetDialog.setOutSideViewTouchListener(null);
            OnDismissListener onDismissListener = this.mOnDismissListener;
            if (onDismissListener != null) {
                onDismissListener.onDismiss();
            }
        }
        BottomSheetBehavior<FrameLayout> bottomSheetBehavior = this.mBehavior;
        if (bottomSheetBehavior instanceof COUIBottomSheetBehavior) {
            ((COUIBottomSheetBehavior) bottomSheetBehavior).setPanelDragListener(null);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putInt(SAVE_PANEL_HEIGHT_KEY, this.mPanelHeight);
        bundle.putInt(SAVE_PANEL_WIDTH_KEY, this.mPanelWidth);
        bundle.putBoolean(SAVE_DRAGGABLE_KEY, this.mIsDraggable);
        bundle.putInt(SAVE_PEEK_HEIGHT_KEY, this.mPeekHeight);
        bundle.putBoolean(SAVE_SKIP_COLLAPSED_KEY, this.mSkipCollapsed);
        bundle.putBoolean(SAVE_FIRST_SHOW_COLLAPSED_KEY, this.mFirstShowCollapsed);
        bundle.putBoolean(SAVE_CAN_PULL_UP_KEY, this.mCanPullUp);
        bundle.putBoolean(SAVE_SHOW_IN_MAX_HEIGHT_KEY, this.mIsShowInMaxHeight);
        bundle.putBoolean(SAVE_IS_IN_TINY_SCREEN_PANEL_KEY, this.mIsInTinyScreen);
        bundle.putBoolean(SAVE_REGISTER_CONFIGURATION_KEY, this.mRegisterConfigurationChangeCallBack);
        bundle.putBoolean(SAVE_IS_HANDLE_PANEL_KEY, this.mIsHandlePanel);
        bundle.putBoolean(SAVE_HAS_SET_PEEK_HEIGHT_KEY, this.mHasSetPeekHeight);
        bundle.putBoolean(SAVE_HAS_SET_SKIP_COLLAPSED_KEY, this.mHasSetSkipCollapsed);
        bundle.putBoolean(SAVE_FRAME_RATE_KEY, this.mFrameRate);
        bundle.putBoolean(SAVE_PANEL_EDGE_TO_EDGE, this.mCouiPanelEdgeToEdgeEnable);
        bundle.putBoolean(SAVE_SUPPORT_EXIT_BLOCKING_ANIMATION, this.mSupportExitBlockingAnimation);
        bundle.putBoolean(SAVE_USE_NORMAL_SMOOTH_CORNER, this.mUseNormalSmoothCorner);
    }

    @Override
    public void onStart() {
        super.onStart();
        BottomSheetBehavior<FrameLayout> bottomSheetBehavior = this.mBehavior;
        if (bottomSheetBehavior instanceof COUIBottomSheetBehavior) {
            bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onSlide(View view, float slideOffset) {
                }

                @Override
                public void onStateChanged(View view, int state) {
                    if (state == 5) {
                        COUIBottomSheetDialogFragment.this.dismissAllowingStateLoss();
                    }
                    if (state == 2 && ((COUIBottomSheetBehavior) COUIBottomSheetDialogFragment.this.mBehavior).isCanHideKeyboard()) {
                        COUIBottomSheetDialogFragment fragment = COUIBottomSheetDialogFragment.this;
                        fragment.hideKeyboard(fragment.mDialogFragmentView);
                    }
                }
            });
        }
    }

    @Override
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        if (getActivity() != null) {
            this.mInputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        }
        ViewGroup viewGroup = (ViewGroup) this.mDialogFragmentView.findViewById(com.coui.appcompat.R.id.first_panel_container);
        this.mPanelContainer = viewGroup;
        if (viewGroup == null) {
            return;
        }
        if (bundle != null) {
            this.mIsSavedInstanceState = true;
            this.mPanelHeight = bundle.getInt(SAVE_PANEL_HEIGHT_KEY, 0);
            this.mPanelWidth = bundle.getInt(SAVE_PANEL_WIDTH_KEY, 0);
            initBottomSheetDialogSize();
        }
        initFragment();
    }

    public void replacePanelFragment(COUIPanelFragment panelFragment) {
        if (panelFragment == null || this.mPanelContainer == null) {
            return;
        }
        if (this.mBottomSheetDialog.getAdjustResizeHelper() != null) {
            this.mBottomSheetDialog.getAdjustResizeHelper().setIgnoreHideKeyboardAnim(true);
        }
        hideKeyboard(this.mDialogFragmentView);
        preShowPanel(panelFragment);
    }

    @Deprecated
    public void setAlphaAnimDuration(long alphaAnimDuration) {
        this.mAlphaAnimDuration = alphaAnimDuration;
    }

    public void setAnchorView(View view) {
        this.mAnchorView = view;
        COUIBottomSheetDialog bottomSheetDialog = this.mBottomSheetDialog;
        if (bottomSheetDialog == null || view == null) {
            return;
        }
        bottomSheetDialog.setAnchorView(view);
    }

    public void setBottomSheetDialogAnimatorListener(COUIBottomSheetDialog.BottomSheetDialogAnimatorListener bottomSheetDialogAnimatorListener) {
        this.mBottomSheetDialogAnimatorListener = bottomSheetDialogAnimatorListener;
    }

    public void setCanPullUp(boolean canPullUp) {
        this.mCanPullUp = canPullUp;
        COUIBottomSheetDialog bottomSheetDialog = this.mBottomSheetDialog;
        if (bottomSheetDialog != null) {
            bottomSheetDialog.setCanPullUp(canPullUp);
        }
    }

    public void setCouiPanelEdgeToEdgeEnable(boolean couiPanelEdgeToEdgeEnable) {
        this.mCouiPanelEdgeToEdgeEnable = couiPanelEdgeToEdgeEnable;
    }

    public void setCurrentPanelHeight(int currentPanelHeight) {
        this.mCurrentPanelHeight = currentPanelHeight;
    }

    public void setDisableSubExpand(boolean disableSubExpand) {
        this.mDisableSubExpand = disableSubExpand;
    }

    public void setDraggable(boolean draggable) {
        if (this.mIsDraggable != draggable) {
            this.mIsDraggable = draggable;
            BottomSheetBehavior<FrameLayout> bottomSheetBehavior = this.mBehavior;
            if (bottomSheetBehavior != null) {
                bottomSheetBehavior.setDraggable(draggable);
            }
        }
    }

    @Deprecated
    public void setExecuteNavColorAnimAfterDismiss(boolean executeNavColorAnimAfterDismiss) {
        this.mIsExecuteNavColorAnimAfterDismiss = executeNavColorAnimAfterDismiss;
        if (getDialog() instanceof COUIBottomSheetDialog) {
            ((COUIBottomSheetDialog) getDialog()).setExecuteNavColorAnimAfterDismiss(executeNavColorAnimAfterDismiss);
        }
    }

    @Deprecated
    public void setFinalNavColorAfterDismiss(int finalNavColorAfterDismiss) {
        this.mFinalNavColorAfterDismiss = finalNavColorAfterDismiss;
        if (getDialog() instanceof COUIBottomSheetDialog) {
            ((COUIBottomSheetDialog) getDialog()).setFinalNavColorAfterDismiss(finalNavColorAfterDismiss);
        }
    }

    public void setFirstShowCollapsed(boolean firstShowCollapsed) {
        this.mFirstShowCollapsed = firstShowCollapsed;
    }

    public void setFrameRate(boolean frameRate) {
        this.mFrameRate = frameRate;
    }

    public void setGlobalDrag(boolean globalDrag) {
        this.mGlobalDrag = globalDrag;
    }

    public void setHeight(int panelHeight) {
        this.mPanelHeight = panelHeight;
        if (this.mBottomSheetDialog != null) {
            initBottomSheetDialogSize();
        }
        if (this.mCurrentPanelFragment != null) {
            setUpViewHeight(this.mPanelContainer, this.mIsShowInMaxHeight);
        }
    }

    public void setHideDragViewHeight(int hideDragViewHeight) {
        this.mHideDragViewHeight = hideDragViewHeight;
        COUIBottomSheetDialog bottomSheetDialog = this.mBottomSheetDialog;
        if (bottomSheetDialog != null) {
            bottomSheetDialog.setHideDragViewHeight(hideDragViewHeight);
        }
    }

    public void setIsHandlePanel(boolean handlePanel) {
        this.mIsHandlePanel = handlePanel;
    }

    public void setIsInTinyScreen(boolean inTinyScreen, boolean fullScreenInTinyScreen) {
        this.mIsInTinyScreen = inTinyScreen;
        this.mIsFullScreenInTinyScreen = fullScreenInTinyScreen;
        if (inTinyScreen) {
            setIsShowInMaxHeight(true);
        }
    }

    public void setIsShowInMaxHeight(boolean showInMaxHeight) {
        this.mIsShowInMaxHeight = showInMaxHeight;
    }

    public void setMainPanelFragment(COUIPanelFragment panelFragment) {
        this.mCurrentPanelFragment = panelFragment;
    }

    public void setOnDismissListener(OnDismissListener onDismissListener) {
        this.mOnDismissListener = onDismissListener;
    }

    public void setPanelFragment(final COUIPanelFragment panelFragment, Boolean showOnFirstPanel) {
        this.mCurrentPanelFragment = panelFragment;
        this.mBottomSheetDialog.setDragableLinearLayout(panelFragment.getDraggableLinearLayout(), true);
        this.mPanelContainer.post(new Runnable() {
            @Override
            public void run() {
                COUIBottomSheetDialogFragment fragment = COUIBottomSheetDialogFragment.this;
                fragment.mCurrentPanelHeight = fragment.getFragmentHeight(panelFragment);
            }
        });
        setUpViewHeight(this.mPanelContainer, this.mIsShowInMaxHeight);
    }

    public void setPeekHeight(int peekHeight) {
        this.mPeekHeight = peekHeight;
        this.mHasSetPeekHeight = true;
    }

    public void setPreferWidth(int preferWidth) {
        this.mPreferWidth = preferWidth;
        Log.d(TAG, "setPreferWidth =：" + this.mPreferWidth);
    }

    public void setShouldRegisterWindowInsetsListener(boolean shouldRegisterWindowInsetsListener) {
        this.mShouldRegisterWindowInsetsListener = shouldRegisterWindowInsetsListener;
    }

    public void setSkipCollapsed(boolean skipCollapsed) {
        this.mSkipCollapsed = skipCollapsed;
        this.mHasSetSkipCollapsed = true;
    }

    public void setSupportExitBlockingAnimation(boolean supportExitBlockingAnimation) {
        if (this.mSupportExitBlockingAnimation != supportExitBlockingAnimation) {
            this.mSupportExitBlockingAnimation = supportExitBlockingAnimation;
            COUIBottomSheetDialog bottomSheetDialog = this.mBottomSheetDialog;
            if (bottomSheetDialog != null) {
                bottomSheetDialog.setSupportExitBlockingAnimation(supportExitBlockingAnimation);
            }
        }
    }

    public void setUseNormalSmoothCorner(boolean useNormalSmoothCorner) {
        this.mUseNormalSmoothCorner = useNormalSmoothCorner;
    }

    public void setWidth(int panelWidth) {
        this.mPanelWidth = panelWidth;
        if (this.mBottomSheetDialog != null) {
            initBottomSheetDialogSize();
        }
    }

    @Override
    public void show(FragmentManager fragmentManager, String tag) {
        show(fragmentManager, tag, null);
    }

    public boolean updateFollowHandPanelLocation() {
        COUIBottomSheetDialog bottomSheetDialog = this.mBottomSheetDialog;
        if (bottomSheetDialog == null) {
            return false;
        }
        return bottomSheetDialog.updateFollowHandPanelLocation();
    }

    public COUIBottomSheetDialogFragment(float physicsFrequency, float physicsDampingRatio) {
        this.mAlphaAnimDuration = 100L;
        this.mIsSavedInstanceState = false;
        this.mPeekHeight = 0;
        this.mSkipCollapsed = true;
        this.mFirstShowCollapsed = false;
        this.mIsDraggable = true;
        this.mCanPullUp = true;
        this.mIsShowInMaxHeight = false;
        this.mRegisterConfigurationChangeCallBack = true;
        this.mAnchorView = null;
        this.mBottomSheetDialogAnimatorListener = null;
        this.mDisableSubExpand = false;
        this.mShouldRegisterWindowInsetsListener = true;
        this.mPreferWidth = -1;
        this.mHasSetPeekHeight = false;
        this.mHasSetSkipCollapsed = false;
        this.mIsHandlePanel = false;
        this.mHideDragViewHeight = 0;
        this.mFrameRate = true;
        this.mPhysicsFrequency = physicsFrequency;
        this.mPhysicsDampingRatio = physicsDampingRatio;
    }

    public void show(FragmentManager fragmentManager, String tag, View anchorView) {
        COUIBottomSheetDialog bottomSheetDialog;
        if (isAdded()) {
            return;
        }
        int preferWidth = this.mPreferWidth;
        if (preferWidth != -1 && (bottomSheetDialog = this.mBottomSheetDialog) != null) {
            bottomSheetDialog.setPreferWidth(preferWidth);
        }
        if (this.mCurrentPanelFragment == null) {
            this.mCurrentPanelFragment = new COUIPanelFragment();
        }
        this.mCurrentPanelFragment.setIsInTinyScreen(this.mIsInTinyScreen);
        this.mAnchorView = anchorView;
        super.show(fragmentManager, tag);
    }

    public void dismiss(boolean animate) {
        COUIBottomSheetDialog bottomSheetDialog = this.mBottomSheetDialog;
        if (bottomSheetDialog != null) {
            bottomSheetDialog.dismiss(animate);
            if (this.mPreferWidth != -1) {
                this.mBottomSheetDialog.delPreferWidth();
                return;
            }
            return;
        }
        try {
            super.dismiss();
        } catch (Exception e2) {
            Log.e(TAG, e2.getMessage(), e2);
        }
    }
}
