package com.coui.appcompat.poplist;

import android.content.Context;
import android.graphics.Rect;
import android.util.Log;
import android.view.DisplayCutout;
import android.view.View;
import android.view.ViewGroup;
import com.coui.appcompat.log.COUILog;
import com.coui.appcompat.R;
import com.coui.component.responsiveui.ResponsiveUIModel;
import com.coui.component.responsiveui.layoutgrid.MarginType;
import com.coui.component.responsiveui.window.WindowTotalSizeClass;
import java.util.Arrays;


final class PopupMenuLocateHelper {
    private static final boolean COUI_DEBUG;
    private static final Rect DEFAULT_ANCHOR_OUTSETS;
    private static final Rect EMPTY_OUTSETS;
    private static final String TAG = "PopupMenuLocateHelper";
    private PopupMenuConfigRule mContextAnchorConfigRule;
    private PopupMenuConfigRule mDefaultAnchorConfigRule;
    private DisplayCutout mDisplayCutout;
    private int mHorizontalOverlapBetweenMainAndSubMenu;
    private boolean mIgnoreBarrier;
    private PopupMenuControlRule mMainMenuCenterLocateXRule;
    private PopupMenuControlRule mMainMenuCenterLocateYRule;
    private PopupMenuControlRule mMainMenuGlobalOffsetRule;
    private int mMainMenuHeight;
    private PopupMenuControlRule mMainMenuLocateXRule;
    private PopupMenuControlRule mMainMenuLocateYRule;
    private PopupMenuControlRule mMainMenuRelocateRule;
    private int mMainMenuShrinkWidth;
    private int mMainMenuWidth;
    private int mMinGapBetweenMainAndSubMenu;
    private int mNavigationBarMargin;
    private ResponsiveUIModel mResponsiveUIModel;
    private int mStatusBarMargin;
    private int mSubMenuHeight;
    private PopupMenuControlRule mSubMenuLocateRule;
    private int mSubMenuWidth;
    private PopupMenuConfigRule mSubmenuAnchorConfigRule;
    private int mVerticalOverlapBetweenMainAndSubMenu;
    private PopupMenuConfigRule mWindowBottomBarrierRule;
    private PopupMenuConfigRule mWindowConfigRule;
    private PopupMenuConfigRule mWindowCutoutBarrierRule;
    private PopupMenuConfigRule mWindowLeftBarrierRule;
    private PopupMenuConfigRule mWindowRightBarrierRule;
    private PopupMenuConfigRule mWindowTopBarrierRule;
    final Rect mApplicationWindow = new Rect();
    private final Rect mContentVisibleBounds = new Rect();
    private final Rect mAnchorBounds = new Rect();
    private final Rect mAvailableBounds = new Rect();
    private final Rect mSubmenuAnchorBounds = new Rect();
    private final Rect mTempContentVisibleBounds = new Rect();
    private final int[] mOffset = new int[2];
    private final int[] mAnchorOffset = new int[2];
    private final int[] mAnchorLocationInWindow = new int[2];
    private int mGlobalOffsetX = 0;
    private int mGlobalOffsetY = 0;
    private boolean mLocateFromAboveAnchorToBelow = false;
    private boolean mIsRtl = false;
    private boolean mUseWindowBarrier = true;
    private boolean mCenterAlign = false;
    private final PopupMenuDomain mDomain = new PopupMenuDomain();
    private final PopupMenuRuleExecutor mExecutor = new PopupMenuRuleExecutor();

    public static class DefaultPopupMenuConfigRule implements PopupMenuConfigRule {
        private boolean mEnabled;

        private DefaultPopupMenuConfigRule() {
            this.mEnabled = true;
        }

        @Override
        public int getBarrierDirection() {
            return -1;
        }

        @Override
        public Rect getDisplayFrame() {
            return PopupMenuLocateHelper.EMPTY_OUTSETS;
        }

        @Override
        public Rect getOutsets() {
            return PopupMenuLocateHelper.EMPTY_OUTSETS;
        }

        @Override
        public boolean getPopupMenuRuleEnabled() {
            return this.mEnabled;
        }

        @Override
        public int getType() {
            return 2;
        }

        @Override
        public void setPopupMenuRuleEnabled(boolean popupMenuRuleEnabled) {
            this.mEnabled = popupMenuRuleEnabled;
        }
    }

    static {
        COUI_DEBUG = COUILog.LOG_DEBUG || COUILog.isLoggable(TAG, 3);
        EMPTY_OUTSETS = new Rect();
        DEFAULT_ANCHOR_OUTSETS = new Rect();
    }

    public PopupMenuLocateHelper(Context context) {
        this.mStatusBarMargin = 0;
        this.mNavigationBarMargin = 0;
        this.mMinGapBetweenMainAndSubMenu = 0;
        this.mHorizontalOverlapBetweenMainAndSubMenu = 0;
        this.mVerticalOverlapBetweenMainAndSubMenu = 0;
        this.mMainMenuShrinkWidth = 0;
        this.mStatusBarMargin = context.getResources().getDimensionPixelOffset(R.dimen.coui_popup_list_window_top_status_bar_margin);
        this.mNavigationBarMargin = context.getResources().getDimensionPixelOffset(R.dimen.coui_popup_list_window_bottom_navigation_bar_margin);
        this.mMinGapBetweenMainAndSubMenu = context.getResources().getDimensionPixelOffset(R.dimen.coui_popup_list_window_min_gap_to_top);
        this.mMainMenuShrinkWidth = context.getResources().getDimensionPixelOffset(R.dimen.coui_popup_list_window_main_menu_shrink_width);
        this.mHorizontalOverlapBetweenMainAndSubMenu = context.getResources().getDimensionPixelOffset(R.dimen.coui_popup_list_window_horizontal_overlap_between_main_and_sub_menu);
        this.mVerticalOverlapBetweenMainAndSubMenu = context.getResources().getDimensionPixelOffset(R.dimen.coui_popup_list_window_vertical_overlap_between_main_and_sub_menu);
        int dimensionPixelOffset = context.getResources().getDimensionPixelOffset(R.dimen.coui_popup_list_window_default_vertical_gap_to_anchor);
        DEFAULT_ANCHOR_OUTSETS.set(0, dimensionPixelOffset, 0, dimensionPixelOffset);
        setupRules();
    }


    private void executeConfigRules(View view, int index, int index_2) {
        this.mDomain.reset();
        this.mExecutor.execute((PopupMenuRule) this.mWindowConfigRule, this.mDomain);
        if (!this.mIgnoreBarrier && this.mUseWindowBarrier) {
            this.mExecutor.execute((PopupMenuRule) this.mWindowLeftBarrierRule, this.mDomain).execute(this.mWindowTopBarrierRule, this.mDomain).execute(this.mWindowRightBarrierRule, this.mDomain).execute(this.mWindowBottomBarrierRule, this.mDomain).execute(this.mWindowCutoutBarrierRule, this.mDomain);
        }
        if (view instanceof PopupMenuConfigRule) {
            PopupMenuConfigRule popupMenuConfigRule = (PopupMenuConfigRule) view;
            if (popupMenuConfigRule.getType() == 1) {
                this.mExecutor.execute((PopupMenuRule) popupMenuConfigRule, this.mDomain);
                return;
            }
        }
        if (index == Integer.MIN_VALUE || index_2 == Integer.MIN_VALUE) {
            this.mExecutor.execute((PopupMenuRule) this.mDefaultAnchorConfigRule, this.mDomain);
        } else {
            this.mExecutor.execute((PopupMenuRule) this.mContextAnchorConfigRule, this.mDomain);
        }
    }

    private void executeShowMainMenu() {
        if (this.mCenterAlign) {
            this.mExecutor.execute((PopupMenuRule) this.mMainMenuCenterLocateXRule, this.mDomain).execute(this.mMainMenuCenterLocateYRule, this.mDomain);
        } else {
            this.mExecutor.execute((PopupMenuRule) this.mMainMenuLocateXRule, this.mDomain).execute(this.mMainMenuLocateYRule, this.mDomain);
        }
        this.mExecutor.execute((PopupMenuRule) this.mMainMenuGlobalOffsetRule, this.mDomain);
    }

    private void executeShowSubMenu() {
        this.mExecutor.execute((PopupMenuRule) this.mSubmenuAnchorConfigRule, this.mDomain).execute(this.mMainMenuRelocateRule, this.mDomain).execute(this.mSubMenuLocateRule, this.mDomain);
    }


    private void findAllBarrierRulesAndExecute(View view) {
        if (view.getVisibility() != 0) {
            return;
        }
        if (view instanceof PopupMenuConfigRule) {
            PopupMenuConfigRule popupMenuConfigRule = (PopupMenuConfigRule) view;
            if (popupMenuConfigRule.getType() == 2) {
                this.mExecutor.execute((PopupMenuRule) popupMenuConfigRule, this.mDomain);
            }
        }
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            int childCount = viewGroup.getChildCount();
            for (int index = 0; index < childCount; index++) {
                findAllBarrierRulesAndExecute(viewGroup.getChildAt(index));
            }
        }
    }

    private void getGlobalVisibleRectWithoutTransformation(View view, Rect rect) {
        view.getGlobalVisibleRect(rect);
        view.getLocationInWindow(this.mAnchorLocationInWindow);
        int[] iArr = this.mAnchorLocationInWindow;
        rect.offset(iArr[0] - rect.left, iArr[1] - rect.top);
        int iWidth = rect.left;
        int iHeight = rect.top;
        if (view.getWidth() != 0 && view.getScaleX() != 0.0f) {
            float pivotX = view.getPivotX() / view.getWidth();
            iWidth = (int) ((rect.left + (rect.width() * pivotX)) - ((rect.width() * pivotX) / view.getScaleX()));
        }
        if (view.getHeight() != 0 && view.getScaleY() != 0.0f) {
            float pivotY = view.getPivotY() / view.getHeight();
            iHeight = (int) ((rect.top + (rect.height() * pivotY)) - ((rect.height() * pivotY) / view.getScaleY()));
        }
        rect.set(iWidth, iHeight, view.getWidth() + iWidth, view.getHeight() + iHeight);
        if (COUI_DEBUG) {
            Log.d(TAG, "bounds with scale transform = " + rect + ",mAnchorLocationInWindow:" + Arrays.toString(this.mAnchorLocationInWindow) + " origin width = " + view.getWidth() + " origin height = " + view.getHeight() + " offset x = " + iWidth + " offset y = " + iHeight + " bounds = " + rect);
        }
    }


    public void applyMainMenuGlobalOffset(PopupMenuDomain popupMenuDomain) {
        int index = this.mGlobalOffsetX;
        popupMenuDomain.mGlobalOffsetX = index;
        popupMenuDomain.mGlobalOffsetY = this.mGlobalOffsetY;
        int iMin = Math.min(Math.max(this.mAvailableBounds.left, popupMenuDomain.mMainMenu.left + index), this.mAvailableBounds.right - popupMenuDomain.mMainMenu.width());
        int iMin2 = Math.min(Math.max(this.mAvailableBounds.top, popupMenuDomain.mMainMenu.top + this.mGlobalOffsetY), this.mAvailableBounds.bottom - popupMenuDomain.mMainMenu.height());
        Rect rect = popupMenuDomain.mMainMenu;
        rect.set(iMin, iMin2, rect.width() + iMin, popupMenuDomain.mMainMenu.height() + iMin2);
    }

    private void setAnchor(View view, int index, int index_2, View view_2) {
        getGlobalVisibleRectWithoutTransformation(view, this.mAnchorBounds);
        if (index != Integer.MIN_VALUE && index_2 != Integer.MIN_VALUE) {
            Rect rect = this.mAnchorBounds;
            int index_3 = rect.left;
            int index_4 = rect.top;
            rect.set(index_3 + index, index_4 + index_2, index_3 + index, index_4 + index_2);
        }
        Rect rect_2 = this.mContentVisibleBounds;
        int[] iArr = this.mOffset;
        rect_2.offset(-iArr[0], -iArr[1]);
        Rect rect_3 = this.mContentVisibleBounds;
        rect_3.bottom = Math.min(rect_3.bottom, this.mApplicationWindow.bottom);
        ResponsiveUIModel responsiveUIModel = this.mResponsiveUIModel;
        if (responsiveUIModel == null) {
            ResponsiveUIModel responsiveUIModel2 = new ResponsiveUIModel(view.getContext(), Math.abs(this.mApplicationWindow.width()), Math.abs(this.mApplicationWindow.height()));
            this.mResponsiveUIModel = responsiveUIModel2;
            responsiveUIModel2.chooseMargin(MarginType.MARGIN_SMALL);
        } else {
            responsiveUIModel.rebuild(Math.abs(this.mApplicationWindow.width()), Math.abs(this.mApplicationWindow.height()));
        }
        if (view.getRootView().isAttachedToWindow()) {
            return;
        }
        Log.d(TAG, "Detected an unattached anchor, could be a dummy anchor");
        this.mIgnoreBarrier = true;
    }

    private void setSubmenuAnchor(View view) {
        view.getGlobalVisibleRect(this.mSubmenuAnchorBounds);
    }

    private void setupContextAnchorConfigRule() {
        this.mContextAnchorConfigRule = new DefaultPopupMenuConfigRule() {
            @Override
            public int getBarrierDirection() {
                return -1;
            }

            @Override
            public Rect getDisplayFrame() {
                return PopupMenuLocateHelper.this.mAnchorBounds;
            }

            @Override
            public Rect getOutsets() {
                return PopupMenuLocateHelper.EMPTY_OUTSETS;
            }

            @Override
            public int getType() {
                return 1;
            }
        };
    }

    private void setupDefaultAnchorConfigRule() {
        this.mDefaultAnchorConfigRule = new DefaultPopupMenuConfigRule() {
            @Override
            public int getBarrierDirection() {
                return -1;
            }

            @Override
            public Rect getDisplayFrame() {
                return PopupMenuLocateHelper.this.mAnchorBounds;
            }

            @Override
            public Rect getOutsets() {
                return PopupMenuLocateHelper.DEFAULT_ANCHOR_OUTSETS;
            }

            @Override
            public int getType() {
                return 1;
            }
        };
    }

    private void setupMainMenuCenterLocateXRule() {
        this.mMainMenuCenterLocateXRule = new PopupMenuControlRule() {
            @Override
            public void operation(PopupMenuDomain popupMenuDomain) {
                int iCenterX = popupMenuDomain.mAnchor.centerX() - (PopupMenuLocateHelper.this.mMainMenuWidth / 2);
                if (PopupMenuLocateHelper.this.mAvailableBounds.right - PopupMenuLocateHelper.this.mAvailableBounds.left >= PopupMenuLocateHelper.this.mMainMenuWidth) {
                    iCenterX = Math.min(Math.max(iCenterX, PopupMenuLocateHelper.this.mAvailableBounds.left), PopupMenuLocateHelper.this.mAvailableBounds.right - PopupMenuLocateHelper.this.mMainMenuWidth);
                }
                Rect rect = popupMenuDomain.mMainMenu;
                rect.set(iCenterX, rect.top, PopupMenuLocateHelper.this.mMainMenuWidth + iCenterX, popupMenuDomain.mMainMenu.bottom);
            }
        };
    }

    private void setupMainMenuCenterLocateYRule() {
        this.mMainMenuCenterLocateYRule = new PopupMenuControlRule() {
            @Override
            public void operation(PopupMenuDomain popupMenuDomain) {
                int iCenterY = popupMenuDomain.mAnchor.centerY() - (PopupMenuLocateHelper.this.mMainMenuHeight / 2);
                if (PopupMenuLocateHelper.this.mAvailableBounds.bottom - PopupMenuLocateHelper.this.mAvailableBounds.top >= PopupMenuLocateHelper.this.mMainMenuHeight) {
                    iCenterY = Math.min(Math.max(iCenterY, PopupMenuLocateHelper.this.mAvailableBounds.top), PopupMenuLocateHelper.this.mAvailableBounds.bottom - PopupMenuLocateHelper.this.mMainMenuHeight);
                }
                Rect rect = popupMenuDomain.mMainMenu;
                rect.set(rect.left, iCenterY, rect.right, PopupMenuLocateHelper.this.mMainMenuHeight + iCenterY);
            }
        };
    }

    private void setupMainMenuGlobalOffsetRule() {
        this.mMainMenuGlobalOffsetRule = new PopupMenuControlRule() {
            @Override
            public final void operation(PopupMenuDomain popupMenuDomain) {
                PopupMenuLocateHelper.this.applyMainMenuGlobalOffset(popupMenuDomain);
            }
        };
    }

    private void setupMainMenuLocateXRule() {
        this.mMainMenuLocateXRule = new PopupMenuControlRule() {
            private int locateX() {
                int iCenterX = PopupMenuLocateHelper.this.mDomain.mAnchor.centerX() - (PopupMenuLocateHelper.this.mMainMenuWidth / 2);
                if (iCenterX < PopupMenuLocateHelper.this.mAvailableBounds.left) {
                    iCenterX = PopupMenuLocateHelper.this.mAvailableBounds.left;
                }
                if (PopupMenuLocateHelper.this.mMainMenuWidth + iCenterX > PopupMenuLocateHelper.this.mAvailableBounds.right) {
                    iCenterX = PopupMenuLocateHelper.this.mAvailableBounds.right - PopupMenuLocateHelper.this.mMainMenuWidth;
                }
                if (iCenterX < PopupMenuLocateHelper.this.mAvailableBounds.left) {
                    iCenterX = PopupMenuLocateHelper.this.mAvailableBounds.centerX() - (PopupMenuLocateHelper.this.mMainMenuWidth / 2);
                }
                if (PopupMenuLocateHelper.COUI_DEBUG) {
                    Log.d(PopupMenuLocateHelper.TAG, "mMainMenuLocateXRule mAnchor [left " + PopupMenuLocateHelper.this.mDomain.mAnchor.left + " top " + PopupMenuLocateHelper.this.mDomain.mAnchor.top + " right " + PopupMenuLocateHelper.this.mDomain.mAnchor.right + " bottom " + PopupMenuLocateHelper.this.mDomain.mAnchor.bottom + "] mMainMenuWidth " + PopupMenuLocateHelper.this.mMainMenuWidth + " mAvailableBounds [left " + PopupMenuLocateHelper.this.mAvailableBounds.left + " top " + PopupMenuLocateHelper.this.mAvailableBounds.top + " right " + PopupMenuLocateHelper.this.mAvailableBounds.right + " bottom " + PopupMenuLocateHelper.this.mAvailableBounds.bottom + "] result x = " + iCenterX);
                }
                return iCenterX;
            }

            @Override
            public void operation(PopupMenuDomain popupMenuDomain) {
                int iLocateX = locateX();
                Rect rect = popupMenuDomain.mMainMenu;
                rect.set(iLocateX, rect.top, PopupMenuLocateHelper.this.mMainMenuWidth + iLocateX, popupMenuDomain.mMainMenu.bottom);
            }
        };
    }

    private void setupMainMenuLocateYRule() {
        this.mMainMenuLocateYRule = new PopupMenuControlRule() {


            int mY = 0;

            private void locateY(Rect rect) {
                int iMax = Math.max(rect.bottom, PopupMenuLocateHelper.this.mAvailableBounds.top);
                int iMin = Math.min(rect.top, PopupMenuLocateHelper.this.mAvailableBounds.bottom);
                if (PopupMenuLocateHelper.this.mLocateFromAboveAnchorToBelow) {
                    if (!tryLocateAboveAnchor(iMin)) {
                        tryLocateBelowAnchor(iMax);
                    }
                } else if (!tryLocateBelowAnchor(iMax)) {
                    tryLocateAboveAnchor(iMin);
                }
                if (PopupMenuLocateHelper.COUI_DEBUG) {
                    Log.d(PopupMenuLocateHelper.TAG, "mMainMenuLocateYRule anchorBounds [left " + rect.left + " top " + rect.top + " right " + rect.right + " bottom " + rect.bottom + "] mMainMenuHeight " + PopupMenuLocateHelper.this.mMainMenuHeight + " mAvailableBounds [left " + PopupMenuLocateHelper.this.mAvailableBounds.left + " top " + PopupMenuLocateHelper.this.mAvailableBounds.top + " right " + PopupMenuLocateHelper.this.mAvailableBounds.right + " bottom " + PopupMenuLocateHelper.this.mAvailableBounds.bottom + "] result y = " + this.mY);
                }
            }

            private boolean tryLocateAboveAnchor(int y_2) {
                if (y_2 - PopupMenuLocateHelper.this.mAvailableBounds.top < PopupMenuLocateHelper.this.mMainMenuHeight) {
                    return false;
                }
                this.mY = y_2 - PopupMenuLocateHelper.this.mMainMenuHeight;
                return true;
            }

            private boolean tryLocateBelowAnchor(int y_2) {
                if (PopupMenuLocateHelper.this.mAvailableBounds.bottom - y_2 < PopupMenuLocateHelper.this.mMainMenuHeight) {
                    return false;
                }
                this.mY = y_2;
                return true;
            }

            @Override
            public void operation(PopupMenuDomain popupMenuDomain) {
                Rect rect = new Rect();
                popupMenuDomain.getAnchorRealRect(rect);
                this.mY = PopupMenuLocateHelper.this.mAvailableBounds.top;
                locateY(rect);
                Rect rect_2 = popupMenuDomain.mMainMenu;
                int y_2 = rect_2.left;
                int index = this.mY;
                rect_2.set(y_2, index, rect_2.right, PopupMenuLocateHelper.this.mMainMenuHeight + index);
            }
        };
    }

    private void setupMainMenuRelocateRule() {
        this.mMainMenuRelocateRule = new PopupMenuControlRule() {
            private int getOffsetX(PopupMenuDomain popupMenuDomain) {
                int iCenterX = popupMenuDomain.mAnchor.centerX();
                int iCenterX2 = popupMenuDomain.mMainMenu.centerX();
                if (iCenterX < iCenterX2 - 1) {
                    return 0;
                }
                return iCenterX > iCenterX2 + 1 ? PopupMenuLocateHelper.this.mMainMenuShrinkWidth : PopupMenuLocateHelper.this.mMainMenuShrinkWidth / 2;
            }

            private int getOffsetY(PopupMenuDomain popupMenuDomain) {
                if (popupMenuDomain.mMainMenu.top + PopupMenuLocateHelper.this.mMinGapBetweenMainAndSubMenu + PopupMenuLocateHelper.this.mSubMenuHeight < PopupMenuLocateHelper.this.mAvailableBounds.bottom) {
                    return 0;
                }
                return ((PopupMenuLocateHelper.this.mAvailableBounds.bottom - PopupMenuLocateHelper.this.mSubMenuHeight) - PopupMenuLocateHelper.this.mMinGapBetweenMainAndSubMenu) - popupMenuDomain.mMainMenu.top;
            }

            @Override
            public void operation(PopupMenuDomain popupMenuDomain) {
                if (!PopupMenuLocateHelper.this.isCurrentContainerSmallScreen()) {
                    popupMenuDomain.mMainMenuRelocated.set(popupMenuDomain.mMainMenu);
                    return;
                }
                Rect rect = popupMenuDomain.mMainMenuRelocated;
                Rect rect_2 = popupMenuDomain.mMainMenu;
                rect.set(rect_2.left, rect_2.top, rect_2.right - PopupMenuLocateHelper.this.mMainMenuShrinkWidth, popupMenuDomain.mMainMenu.bottom - ((int) ((PopupMenuLocateHelper.this.mMainMenuShrinkWidth / popupMenuDomain.mMainMenu.width()) * popupMenuDomain.mMainMenu.height())));
                popupMenuDomain.mMainMenuRelocated.offset(getOffsetX(popupMenuDomain), getOffsetY(popupMenuDomain));
            }
        };
    }

    private void setupRules() {
        setupWindowConfigRule();
        setupWindowLeftBarrierRule();
        setupWindowRightBarrierRule();
        setupWindowTopBarrierRule();
        setupWindowBottomBarrierRule();
        setupWindowCutoutBarrierRule();
        setupContextAnchorConfigRule();
        setupDefaultAnchorConfigRule();
        setupSubmenuAnchorConfigRule();
        setupMainMenuLocateXRule();
        setupMainMenuLocateYRule();
        setupMainMenuGlobalOffsetRule();
        setupMainMenuRelocateRule();
        setupSubMenuLocateRule();
        setupMainMenuCenterLocateXRule();
        setupMainMenuCenterLocateYRule();
    }

    private void setupSubMenuLocateRule() {
        this.mSubMenuLocateRule = new PopupMenuControlRule() {
            private int getOffsetX(PopupMenuDomain popupMenuDomain) {
                int index;
                int index_2;
                if (PopupMenuLocateHelper.this.isCurrentContainerSmallScreen()) {
                    return popupMenuDomain.mMainMenu.left;
                }
                if (PopupMenuLocateHelper.this.mIsRtl) {
                    if ((popupMenuDomain.mMainMenuRelocated.right - PopupMenuLocateHelper.this.mHorizontalOverlapBetweenMainAndSubMenu) + PopupMenuLocateHelper.this.mSubMenuWidth < PopupMenuLocateHelper.this.mAvailableBounds.right) {
                        index = popupMenuDomain.mMainMenuRelocated.right;
                        index_2 = PopupMenuLocateHelper.this.mHorizontalOverlapBetweenMainAndSubMenu;
                    } else {
                        index = popupMenuDomain.mMainMenuRelocated.left + PopupMenuLocateHelper.this.mHorizontalOverlapBetweenMainAndSubMenu;
                        index_2 = PopupMenuLocateHelper.this.mSubMenuWidth;
                    }
                } else if ((popupMenuDomain.mMainMenuRelocated.left + PopupMenuLocateHelper.this.mHorizontalOverlapBetweenMainAndSubMenu) - PopupMenuLocateHelper.this.mSubMenuWidth > PopupMenuLocateHelper.this.mAvailableBounds.left) {
                    index = popupMenuDomain.mMainMenuRelocated.left + PopupMenuLocateHelper.this.mHorizontalOverlapBetweenMainAndSubMenu;
                    index_2 = PopupMenuLocateHelper.this.mSubMenuWidth;
                } else {
                    index = popupMenuDomain.mMainMenuRelocated.right;
                    index_2 = PopupMenuLocateHelper.this.mHorizontalOverlapBetweenMainAndSubMenu;
                }
                return index - index_2;
            }

            private int getOffsetY(PopupMenuDomain popupMenuDomain) {
                int subMenuAnchorTopAfterMainMenuRelocated;
                int index;
                if (PopupMenuLocateHelper.this.isCurrentContainerSmallScreen()) {
                    subMenuAnchorTopAfterMainMenuRelocated = getSubMenuAnchorTopAfterMainMenuRelocated(popupMenuDomain);
                    if ((subMenuAnchorTopAfterMainMenuRelocated - PopupMenuLocateHelper.this.mVerticalOverlapBetweenMainAndSubMenu) + PopupMenuLocateHelper.this.mSubMenuHeight < PopupMenuLocateHelper.this.mAvailableBounds.bottom) {
                        index = PopupMenuLocateHelper.this.mVerticalOverlapBetweenMainAndSubMenu;
                    } else {
                        subMenuAnchorTopAfterMainMenuRelocated = PopupMenuLocateHelper.this.mAvailableBounds.bottom;
                        index = PopupMenuLocateHelper.this.mSubMenuHeight;
                    }
                } else {
                    if (PopupMenuLocateHelper.this.mSubmenuAnchorBounds.top + PopupMenuLocateHelper.this.mSubMenuHeight < PopupMenuLocateHelper.this.mAvailableBounds.bottom) {
                        return PopupMenuLocateHelper.this.mSubmenuAnchorBounds.top;
                    }
                    subMenuAnchorTopAfterMainMenuRelocated = PopupMenuLocateHelper.this.mAvailableBounds.bottom;
                    index = PopupMenuLocateHelper.this.mSubMenuHeight;
                }
                return subMenuAnchorTopAfterMainMenuRelocated - index;
            }

            private int getSubMenuAnchorTopAfterMainMenuRelocated(PopupMenuDomain popupMenuDomain) {
                int index = PopupMenuLocateHelper.this.mSubmenuAnchorBounds.top;
                return (int) (popupMenuDomain.mMainMenuRelocated.top + ((popupMenuDomain.mMainMenu.height() > 0 ? popupMenuDomain.mMainMenuRelocated.height() / popupMenuDomain.mMainMenu.height() : 1.0f) * (index - popupMenuDomain.mMainMenu.top)));
            }

            @Override
            public void operation(PopupMenuDomain popupMenuDomain) {
                popupMenuDomain.mSubMenu.set(0, 0, PopupMenuLocateHelper.this.mSubMenuWidth, PopupMenuLocateHelper.this.mSubMenuHeight);
                popupMenuDomain.mSubMenu.offset(getOffsetX(popupMenuDomain), getOffsetY(popupMenuDomain));
            }
        };
    }

    private void setupSubmenuAnchorConfigRule() {
        this.mSubmenuAnchorConfigRule = new DefaultPopupMenuConfigRule() {
            @Override
            public int getBarrierDirection() {
                return -1;
            }

            @Override
            public Rect getDisplayFrame() {
                return PopupMenuLocateHelper.this.mSubmenuAnchorBounds;
            }

            @Override
            public Rect getOutsets() {
                return PopupMenuLocateHelper.EMPTY_OUTSETS;
            }

            @Override
            public int getType() {
                return 3;
            }
        };
    }

    private void setupWindowBottomBarrierRule() {
        this.mWindowBottomBarrierRule = new DefaultPopupMenuConfigRule() {
            private final Rect mDisplayFrame = new Rect();
            private final Rect mDisplayFrameOutsets;

            {
                this.mDisplayFrameOutsets = new Rect(0, PopupMenuLocateHelper.this.mNavigationBarMargin, 0, 0);
            }

            @Override
            public int getBarrierDirection() {
                return 3;
            }

            @Override
            public Rect getDisplayFrame() {
                PopupMenuLocateHelper popupMenuLocateHelper = PopupMenuLocateHelper.this;
                int index = popupMenuLocateHelper.mApplicationWindow.bottom - popupMenuLocateHelper.mContentVisibleBounds.bottom;
                Rect rect = this.mDisplayFrame;
                Rect rect_2 = PopupMenuLocateHelper.this.mApplicationWindow;
                rect.set(0, rect_2.bottom - index, Math.abs(rect_2.width()), PopupMenuLocateHelper.this.mApplicationWindow.bottom);
                return this.mDisplayFrame;
            }

            @Override
            public Rect getOutsets() {
                return this.mDisplayFrameOutsets;
            }

            @Override
            public int getType() {
                return 2;
            }
        };
    }

    private void setupWindowConfigRule() {
        this.mWindowConfigRule = new DefaultPopupMenuConfigRule() {
            @Override
            public int getBarrierDirection() {
                return -1;
            }

            @Override
            public Rect getDisplayFrame() {
                return PopupMenuLocateHelper.this.mApplicationWindow;
            }

            @Override
            public Rect getOutsets() {
                return PopupMenuLocateHelper.EMPTY_OUTSETS;
            }

            @Override
            public int getType() {
                return 0;
            }
        };
    }

    private void setupWindowCutoutBarrierRule() {
        this.mWindowCutoutBarrierRule = new DefaultPopupMenuConfigRule() {
            private final Rect mDisplayFrame = new Rect();

            @Override
            public int getBarrierDirection() {
                if (PopupMenuLocateHelper.this.mDisplayCutout == null) {
                    return -1;
                }
                if (!PopupMenuLocateHelper.this.mDisplayCutout.getBoundingRectTop().isEmpty()) {
                    return 1;
                }
                if (!PopupMenuLocateHelper.this.mDisplayCutout.getBoundingRectBottom().isEmpty()) {
                    return 3;
                }
                if (PopupMenuLocateHelper.this.mDisplayCutout.getBoundingRectLeft().isEmpty()) {
                    return !PopupMenuLocateHelper.this.mDisplayCutout.getBoundingRectRight().isEmpty() ? 2 : -1;
                }
                return 0;
            }

            @Override
            public Rect getDisplayFrame() {
                if (PopupMenuLocateHelper.this.mDisplayCutout == null) {
                    return this.mDisplayFrame;
                }
                if (!PopupMenuLocateHelper.this.mDisplayCutout.getBoundingRectTop().isEmpty()) {
                    this.mDisplayFrame.set(0, 0, PopupMenuLocateHelper.this.mApplicationWindow.width(), Math.max(PopupMenuLocateHelper.this.mContentVisibleBounds.top, PopupMenuLocateHelper.this.mDisplayCutout.getBoundingRectTop().bottom));
                } else if (!PopupMenuLocateHelper.this.mDisplayCutout.getBoundingRectBottom().isEmpty()) {
                    this.mDisplayFrame.set(0, PopupMenuLocateHelper.this.mDisplayCutout.getBoundingRectBottom().top, Math.abs(PopupMenuLocateHelper.this.mApplicationWindow.width()), PopupMenuLocateHelper.this.mApplicationWindow.bottom);
                } else if (!PopupMenuLocateHelper.this.mDisplayCutout.getBoundingRectLeft().isEmpty()) {
                    this.mDisplayFrame.set(0, 0, PopupMenuLocateHelper.this.mDisplayCutout.getBoundingRectLeft().right, Math.abs(PopupMenuLocateHelper.this.mApplicationWindow.height()));
                } else if (!PopupMenuLocateHelper.this.mDisplayCutout.getBoundingRectRight().isEmpty()) {
                    Rect rect = this.mDisplayFrame;
                    int boundingRectRight = PopupMenuLocateHelper.this.mDisplayCutout.getBoundingRectRight().left;
                    Rect rect_2 = PopupMenuLocateHelper.this.mApplicationWindow;
                    rect.set(boundingRectRight, 0, rect_2.right, Math.abs(rect_2.height()));
                }
                return this.mDisplayFrame;
            }

            @Override
            public Rect getOutsets() {
                return PopupMenuLocateHelper.EMPTY_OUTSETS;
            }

            @Override
            public int getType() {
                return 2;
            }
        };
    }

    private void setupWindowLeftBarrierRule() {
        this.mWindowLeftBarrierRule = new DefaultPopupMenuConfigRule() {
            private final Rect mDisplayFrame = new Rect();

            @Override
            public int getBarrierDirection() {
                return 0;
            }

            @Override
            public Rect getDisplayFrame() {
                this.mDisplayFrame.set(0, 0, Math.max(PopupMenuLocateHelper.this.mResponsiveUIModel.margin(), PopupMenuLocateHelper.this.mContentVisibleBounds.left), Math.abs(PopupMenuLocateHelper.this.mApplicationWindow.height()));
                return this.mDisplayFrame;
            }

            @Override
            public Rect getOutsets() {
                return PopupMenuLocateHelper.EMPTY_OUTSETS;
            }

            @Override
            public int getType() {
                return 2;
            }
        };
    }

    private void setupWindowRightBarrierRule() {
        this.mWindowRightBarrierRule = new DefaultPopupMenuConfigRule() {
            private final Rect mDisplayFrame = new Rect();

            @Override
            public int getBarrierDirection() {
                return 2;
            }

            @Override
            public Rect getDisplayFrame() {
                int iMargin = PopupMenuLocateHelper.this.mResponsiveUIModel.margin();
                PopupMenuLocateHelper popupMenuLocateHelper = PopupMenuLocateHelper.this;
                int iMax = Math.max(iMargin, popupMenuLocateHelper.mApplicationWindow.right - popupMenuLocateHelper.mContentVisibleBounds.right);
                Rect rect = this.mDisplayFrame;
                Rect rect_2 = PopupMenuLocateHelper.this.mApplicationWindow;
                int index = rect_2.right;
                rect.set(index - iMax, 0, index, Math.abs(rect_2.height()));
                return this.mDisplayFrame;
            }

            @Override
            public Rect getOutsets() {
                return PopupMenuLocateHelper.EMPTY_OUTSETS;
            }

            @Override
            public int getType() {
                return 2;
            }
        };
    }

    private void setupWindowTopBarrierRule() {
        this.mWindowTopBarrierRule = new DefaultPopupMenuConfigRule() {
            private final Rect mDisplayFrame = new Rect();

            @Override
            public int getBarrierDirection() {
                return 1;
            }

            @Override
            public Rect getDisplayFrame() {
                this.mDisplayFrame.set(0, 0, Math.abs(PopupMenuLocateHelper.this.mApplicationWindow.width()), PopupMenuLocateHelper.this.mContentVisibleBounds.top + PopupMenuLocateHelper.this.mStatusBarMargin);
                return this.mDisplayFrame;
            }

            @Override
            public Rect getOutsets() {
                return PopupMenuLocateHelper.EMPTY_OUTSETS;
            }

            @Override
            public int getType() {
                return 2;
            }
        };
    }

    public boolean checkIfLimitedWindowOrAnchorResized(View view, int index, int index_2, View view_2) {
        boolean flag = true;
        if (view == null) {
            COUILog.e(TAG, "Anchor is null!");
            return true;
        }
        if (view_2 == null) {
            view_2 = view.getRootView();
        }
        view_2.getWindowVisibleDisplayFrame(this.mTempContentVisibleBounds);
        if (this.mTempContentVisibleBounds.width() == this.mContentVisibleBounds.width() && this.mTempContentVisibleBounds.height() == this.mContentVisibleBounds.height()) {
            flag = false;
        } else {
            COUILog.w(TAG, "Visible bounds changed!");
        }
        COUILog.d(TAG, " old content visible bounds = " + this.mContentVisibleBounds + " new content visible bounds = " + this.mTempContentVisibleBounds);
        this.mContentVisibleBounds.set(this.mTempContentVisibleBounds);
        return flag;
    }

    public PopupMenuDomain getDomain() {
        return this.mDomain;
    }

    public int getMainMenuHeight() {
        return this.mMainMenuHeight;
    }

    public int getMainMenuWidth() {
        return this.mMainMenuWidth;
    }

    public int getMaxMainMenuHeight() {
        return this.mDomain.getAvailableRectHeight();
    }

    public int getMaxSubMenuHeight() {
        return isCurrentContainerSmallScreen() ? this.mDomain.getAvailableRectHeight() : this.mDomain.getAvailableRectHeight() - this.mMinGapBetweenMainAndSubMenu;
    }

    public int getSubMenuHeight() {
        return this.mSubMenuHeight;
    }

    public int getSubMenuWidth() {
        return this.mSubMenuWidth;
    }

    public boolean isCurrentContainerSmallScreen() {
        ResponsiveUIModel responsiveUIModel = this.mResponsiveUIModel;
        return responsiveUIModel != null && responsiveUIModel.windowSizeClass().getWindowTotalSizeClass() == WindowTotalSizeClass.Compact;
    }

    public void prepareShowMainMenu(int index, int index_2, boolean flag, int index_3, int index_4) {
        this.mLocateFromAboveAnchorToBelow = flag;
        this.mGlobalOffsetX = index_3;
        this.mGlobalOffsetY = index_4;
        this.mDomain.getAvailableRect(this.mAvailableBounds);
        this.mMainMenuWidth = Math.min(index, Math.abs(this.mAvailableBounds.width()));
        this.mMainMenuHeight = Math.min(index_2, Math.abs(this.mAvailableBounds.height()));
        executeShowMainMenu();
        this.mDomain.dump();
        this.mExecutor.endConfigRulesRecord();
    }

    public void prepareShowSubMenu(View view, int index, int index_2, boolean flag) {
        this.mIsRtl = flag;
        boolean zIsCurrentContainerSmallScreen = isCurrentContainerSmallScreen();
        setSubmenuAnchor(view);
        this.mSubMenuWidth = Math.min(index, Math.abs(this.mAvailableBounds.width()));
        this.mSubMenuHeight = Math.min(index_2, Math.abs(this.mAvailableBounds.height()) - (zIsCurrentContainerSmallScreen ? this.mMinGapBetweenMainAndSubMenu : 0));
        executeShowSubMenu();
        this.mDomain.dump();
    }

    public void prepareWindowAndAnchor(View view, int index, int index_2, View view_2) {
        View rootView = view_2 != null ? view_2 : view.getRootView();
        rootView.getLocationOnScreen(this.mOffset);
        rootView.getGlobalVisibleRect(this.mApplicationWindow);
        rootView.getWindowVisibleDisplayFrame(this.mContentVisibleBounds);
        if (COUI_DEBUG) {
            Log.d(TAG, "limited window = " + rootView + " anchor = " + view + " window location = (" + this.mOffset[0] + ", " + this.mOffset[1] + ") anchor location = (" + this.mAnchorOffset[0] + ", " + this.mAnchorOffset[1] + ") final offset = (" + index + ", " + index_2 + ") use window barrier = " + this.mUseWindowBarrier + " center align = " + this.mCenterAlign + " mApplicationWindow [left " + this.mApplicationWindow.left + " top " + this.mApplicationWindow.top + " right " + this.mApplicationWindow.right + " bottom " + this.mApplicationWindow.bottom + "]");
        }
        setAnchor(view, index, index_2, view_2);
        if (view.getRootWindowInsets() != null) {
            this.mDisplayCutout = view.getRootWindowInsets().getDisplayCutout();
        }
        this.mExecutor.beginConfigRulesRecord();
        executeConfigRules(view, index, index_2);
        findAllBarrierRulesAndExecute(view.getRootView());
    }

    public void setCenterAlign(boolean centerAlign) {
        this.mCenterAlign = centerAlign;
        this.mDomain.mMainMenuCenterAlign = centerAlign;
    }

    public void setSubMenuAnchorIsFirstItem(boolean subMenuAnchorIsFirstItem) {
        this.mDomain.mSubMenuAnchorIsFirstItem = subMenuAnchorIsFirstItem;
    }

    public void useWindowBarrier(boolean flag) {
        this.mUseWindowBarrier = flag;
    }
}
