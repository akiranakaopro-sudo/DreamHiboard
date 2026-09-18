package com.coui.appcompat.toolbar;

import com.coui.appcompat.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.appcompat.view.menu.MenuPresenter;
import androidx.appcompat.view.menu.MenuView;
import androidx.appcompat.view.menu.SubMenuBuilder;
import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.widget.TintTypedArray;
import androidx.appcompat.view.CollapsibleActionView;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import com.coui.appcompat.darkmode.COUIDarkModeUtil;
import com.coui.appcompat.grid.COUIResponsiveUtils;
import com.coui.appcompat.poplist.COUIPopupListWindow;
import com.coui.appcompat.poplist.COUISubMenuClickListener;
import com.coui.appcompat.poplist.PopupListItem;
import com.coui.appcompat.poplist.PopupMenuConfigRule;
import com.coui.appcompat.state.COUIMaskRippleDrawable;
import com.coui.appcompat.textutil.COUIChangeTextUtil;
import com.coui.appcompat.uiutil.UIUtil;
import java.util.ArrayList;
import java.util.List;
import androidx.core.view.MarginLayoutParamsCompat;

public class COUIToolbar extends Toolbar implements PopupMenuConfigRule {
    private static final int DEFAULT_TEXT_MAX = 24;
    private static final int DEFAULT_TEXT_MIN = 16;
    private static final int DEFAULT_GRAVITY = Gravity.START | Gravity.CENTER_VERTICAL;
    private static final String TAG = "Toolbar";
    public static final int TITLE_TYPE_HEAD = 0;
    public static final int TITLE_TYPE_SECONDARY = 1;
    private static final Rect TOOLBAR_OUTSETS = new Rect();
    private MenuPresenter.Callback mActionMenuPresenterCallback;
    private int mButtonGravity;
    private ImageButton mCollapseButtonView;
    private CharSequence mCollapseDescription;
    private Drawable mCollapseIcon;
    private boolean mCollapsible;
    private final COUIRtlSpacingHelper mContentInsets;
    private Rect mDisplayFrame;
    private View mDummyView;
    private boolean mEatingHover;
    private boolean mEatingTouch;
    private View mExpandedActionView;
    private ExpandedActionViewMenuPresenter mExpandedMenuPresenter;
    private final int mGapBeforeMenuView;
    private int mGapBetweenNavigationAndTitle;
    private int mGapBetweenSearchViewAndMenu;
    private int mGravity;
    private boolean mHasCustomViewBeforeTitle;
    private boolean mHasSearchViewFlag;
    boolean mIsInsidePanel;
    private boolean mIsInsideSideNavigationBar;
    private boolean mIsTiny;
    private boolean mIsTitleCenterStyle;
    private ImageView mLogoView;
    private COUIMaskRippleDrawable mMaskRippleDrawable;
    private int mMaxButtonHeight;
    private MenuBuilder.Callback mMenuBuilderCallback;
    private COUIActionMenuView mMenuView;
    private final ActionMenuView.OnMenuItemClickListener mMenuViewItemClickListener;
    private int mMinHeight;
    private ImageButton mNavButtonView;
    private Toolbar.OnMenuItemClickListener mOnMenuItemClickListener;
    private Context mPopupContext;
    private boolean mPopupRuleEnable;
    private int mPopupTheme;
    private int mResId;
    private final int[] mSearchCollapsingMargins;
    private int mSectionGap;
    private int mSectionGapMediumLarge;
    private final int mSectionGapSmall;
    private View mSegmentButton;
    private final int mSegmentButtonHeight;
    private final int mSegmentButtonMaxWidth;
    private final int mSegmentButtonMinWidth;
    private final Runnable mShowOverflowMenuRunnable;
    private int mStyle;
    private CharSequence mSubtitleText;
    private int mSubtitleTextAppearance;
    private int mSubtitleTextColor;
    private TextView mSubtitleTextView;
    private final int[] mTempMargins;
    private final ArrayList<View> mTempViews;
    private View mTextButton;
    private float mTextMaxSize;
    private float mTextMinSize;
    private int mTitleMarginBottom;
    private int mTitleMarginEnd;
    private int mTitleMarginStart;
    private int mTitleMarginTop;
    private int mTitleMinWidth;
    private int mTitlePaddingBottom;
    private int mTitlePaddingTop;
    private int[] mTitlePosition;
    private CharSequence mTitleText;
    private int mTitleTextAppearance;
    private int mTitleTextColor;
    private final int mTitleTextMinWidth;
    private float mTitleTextSize;
    private TextView mTitleTextView;
    private int mTitleType;
    private int mToolbarCenterTitlePaddingLeft;
    private int mToolbarCenterTitlePaddingRight;
    private int mToolbarHeight;
    private int mToolbarNormalPaddingLeft;
    private int mToolbarNormalPaddingRight;
    private int mToolbarOverFlowPadding;
    private boolean mUseResponsivePadding;
    private Rect mWindowFrame;


    public class ExpandedActionViewMenuPresenter implements MenuPresenter {
        MenuItemImpl mCurrentExpandedItem;
        MenuBuilder mMenu;

        private ExpandedActionViewMenuPresenter() {
        }

        @Override
        public boolean collapseItemActionView(MenuBuilder menuBuilder, MenuItemImpl menuItemImpl) {
            if (COUIToolbar.this.mExpandedActionView instanceof CollapsibleActionView) {
                ((CollapsibleActionView) COUIToolbar.this.mExpandedActionView).onActionViewCollapsed();
            }
            COUIToolbar toolbar = COUIToolbar.this;
            toolbar.removeView(toolbar.mExpandedActionView);
            toolbar.removeView(toolbar.mCollapseButtonView);
            COUIToolbar.this.mExpandedActionView = null;
            COUIToolbar.this.setChildVisibilityForExpandedActionView(false);
            this.mCurrentExpandedItem = null;
            COUIToolbar.this.requestLayout();
            menuItemImpl.setActionViewExpanded(false);
            return true;
        }

        @Override
        public boolean expandItemActionView(MenuBuilder menuBuilder, MenuItemImpl menuItemImpl) {
            COUIToolbar.this.ensureCollapseButtonView();
            ViewParent parent = COUIToolbar.this.mCollapseButtonView.getParent();
            COUIToolbar toolbar = COUIToolbar.this;
            if (parent != toolbar) {
                toolbar.addView(toolbar.mCollapseButtonView);
            }
            COUIToolbar.this.mExpandedActionView = menuItemImpl.getActionView();
            this.mCurrentExpandedItem = menuItemImpl;
            ViewParent expandedViewParent = COUIToolbar.this.mExpandedActionView.getParent();
            if (expandedViewParent != toolbar) {
                LayoutParams toolbarLayoutParams = toolbar.generateDefaultLayoutParams();
                toolbarLayoutParams.gravity = (COUIToolbar.this.mButtonGravity & Gravity.VERTICAL_GRAVITY_MASK) | Gravity.START;
                toolbarLayoutParams.mViewType = 2;
                COUIToolbar.this.mExpandedActionView.setLayoutParams(toolbarLayoutParams);
                toolbar.addView(toolbar.mExpandedActionView);
            }
            COUIToolbar.this.setChildVisibilityForExpandedActionView(true);
            COUIToolbar.this.requestLayout();
            menuItemImpl.setActionViewExpanded(true);
            if (COUIToolbar.this.mExpandedActionView instanceof CollapsibleActionView) {
                ((CollapsibleActionView) COUIToolbar.this.mExpandedActionView).onActionViewExpanded();
            }
            return true;
        }

        @Override
        public boolean flagActionItems() {
            return false;
        }

        @Override
        public int getId() {
            return 0;
        }

        @Override
        public MenuView getMenuView(ViewGroup viewGroup) {
            return null;
        }

        @Override
        public void initForMenu(Context context, MenuBuilder menuBuilder) {
            MenuItemImpl menuItemImpl;
            MenuBuilder currentMenu = this.mMenu;
            if (currentMenu != null && (menuItemImpl = this.mCurrentExpandedItem) != null) {
                currentMenu.collapseItemActionView(menuItemImpl);
            }
            this.mMenu = menuBuilder;
        }

        @Override
        public void onCloseMenu(MenuBuilder menuBuilder, boolean allMenusAreClosing) {
        }

        @Override
        public void onRestoreInstanceState(Parcelable parcelable) {
        }

        @Override
        public Parcelable onSaveInstanceState() {
            return null;
        }

        @Override
        public boolean onSubMenuSelected(SubMenuBuilder subMenuBuilder) {
            return subMenuBuilder != null && subMenuBuilder.size() > 0;
        }

        @Override
        public void setCallback(MenuPresenter.Callback callback) {
        }

        @Override
        public void updateMenuView(boolean cleared) {
            if (this.mCurrentExpandedItem != null) {
                MenuBuilder menuBuilder = this.mMenu;
                if (menuBuilder != null) {
                    int size = menuBuilder.size();
                    for (int index = 0; index < size; index++) {
                        if (this.mMenu.getItem(index) == this.mCurrentExpandedItem) {
                            return;
                        }
                    }
                }
                collapseItemActionView(this.mMenu, this.mCurrentExpandedItem);
            }
        }
    }

    public static class LayoutParams extends Toolbar.LayoutParams {
        static final int CUSTOM = 0;
        static final int EXPANDED = 2;
        static final int SYSTEM = 1;
        boolean mTypeSearch;
        boolean mTypeSegmentButton;
        boolean mTypeTextButton;
        boolean mTypeTitle;
        int mViewType;

        public LayoutParams(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
            this.mViewType = 0;
            this.mTypeSearch = false;
            this.mTypeTitle = false;
            this.mTypeSegmentButton = false;
            this.mTypeTextButton = false;
        }

        public void copyMarginsFromCompat(ViewGroup.MarginLayoutParams marginLayoutParams) {
            ((ViewGroup.MarginLayoutParams) this).leftMargin = marginLayoutParams.leftMargin;
            ((ViewGroup.MarginLayoutParams) this).topMargin = marginLayoutParams.topMargin;
            ((ViewGroup.MarginLayoutParams) this).rightMargin = marginLayoutParams.rightMargin;
            ((ViewGroup.MarginLayoutParams) this).bottomMargin = marginLayoutParams.bottomMargin;
        }

        public LayoutParams(int width, int height) {
            super(width, height);
            this.mViewType = 0;
            this.mTypeSearch = false;
            this.mTypeTitle = false;
            this.mTypeSegmentButton = false;
            this.mTypeTextButton = false;
            this.gravity = DEFAULT_GRAVITY;
        }

        public LayoutParams(int width, int height, int gravity) {
            super(width, height);
            this.mViewType = 0;
            this.mTypeSearch = false;
            this.mTypeTitle = false;
            this.mTypeSegmentButton = false;
            this.mTypeTextButton = false;
            this.gravity = gravity;
        }

        public LayoutParams(int gravity) {
            this(-2, -1, gravity);
        }

        public LayoutParams(LayoutParams layoutParams) {
            super((Toolbar.LayoutParams) layoutParams);
            this.mViewType = 0;
            this.mTypeSearch = false;
            this.mTypeTitle = false;
            this.mTypeSegmentButton = false;
            this.mTypeTextButton = false;
            this.mViewType = layoutParams.mViewType;
        }

        public LayoutParams(ViewGroup.MarginLayoutParams marginLayoutParams) {
            super(marginLayoutParams);
            this.mViewType = 0;
            this.mTypeSearch = false;
            this.mTypeTitle = false;
            this.mTypeSegmentButton = false;
            this.mTypeTextButton = false;
            copyMarginsFromCompat(marginLayoutParams);
        }

        public LayoutParams(ViewGroup.LayoutParams layoutParams) {
            super(layoutParams);
            this.mViewType = 0;
            this.mTypeSearch = false;
            this.mTypeTitle = false;
            this.mTypeSegmentButton = false;
            this.mTypeTextButton = false;
        }
    }

    public COUIToolbar(Context context) {
        this(context, null);
    }

    private void addCustomCenterViews(List<View> list) {
        int childCount = getChildCount();
        int centerGravity = GravityCompat.getAbsoluteGravity(
                Gravity.CENTER_HORIZONTAL, ViewCompat.getLayoutDirection(this));
        list.clear();
        for (int index = 0; index < childCount; index++) {
            View child = getChildAt(index);
            LayoutParams layoutParams = (LayoutParams) child.getLayoutParams();
            if (layoutParams.mViewType == 0 && shouldLayout(child)
                    && getChildHorizontalGravity(layoutParams.gravity) == centerGravity) {
                list.add(child);
            }
        }
    }

    private void addCustomViewsWithGravity(List<View> list, int gravity) {
        boolean isRtl = ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL;
        int childCount = getChildCount();
        int absoluteGravity = GravityCompat.getAbsoluteGravity(
                gravity, ViewCompat.getLayoutDirection(this));
        list.clear();
        if (!isRtl) {
            for (int index = 0; index < childCount; index++) {
                View child = getChildAt(index);
                LayoutParams layoutParams = (LayoutParams) child.getLayoutParams();
                if (layoutParams.mViewType == 0 && shouldLayout(child)
                        && getChildHorizontalGravity(layoutParams.gravity) == absoluteGravity) {
                    list.add(child);
                }
            }
            return;
        }
        for (int index = childCount - 1; index >= 0; index--) {
            View child = getChildAt(index);
            LayoutParams layoutParams = (LayoutParams) child.getLayoutParams();
            if (layoutParams.mViewType == 0 && shouldLayout(child)
                    && getChildHorizontalGravity(layoutParams.gravity) == absoluteGravity) {
                list.add(child);
            }
        }
    }

    private void addSystemView(View view) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        LayoutParams toolbarLayoutParams = layoutParams == null ? generateDefaultLayoutParams() : !checkLayoutParams(layoutParams) ? generateLayoutParams(layoutParams) : (LayoutParams) layoutParams;
        toolbarLayoutParams.mViewType = 1;
        addView(view, toolbarLayoutParams);
    }

    private void calculateTitlePosition(int[] titlePosition) {
        int startMenuWidth;
        int endMenuWidth;
        boolean isRtl = ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL;
        int itemSpacing = getResources().getDimensionPixelSize(R.dimen.coui_actionbar_menuitemview_item_spacing);
        titlePosition[0] = Math.max(this.mContentInsets.getLeft(), getPaddingLeft());
        titlePosition[1] = getMeasuredWidth() - Math.max(this.mContentInsets.getRight(), getPaddingRight());
        if (!shouldLayout(this.mMenuView) || this.mMenuView.getChildCount() == 0) {
            return;
        }
        if (this.mMenuView.getChildCount() == 1) {
            endMenuWidth = this.mMenuView.getChildAt(0).getMeasuredWidth() + itemSpacing;
            startMenuWidth = 0;
        } else {
            startMenuWidth = this.mMenuView.getChildAt(0).getMeasuredWidth() + itemSpacing;
            endMenuWidth = 0;
            for (int index = 1; index < this.mMenuView.getChildCount(); index++) {
                endMenuWidth += this.mMenuView.getChildAt(index).getMeasuredWidth() + itemSpacing;
            }
        }
        if (isRtl) {
            titlePosition[0] += endMenuWidth;
            titlePosition[1] -= startMenuWidth;
        } else {
            titlePosition[0] += startMenuWidth;
            titlePosition[1] -= endMenuWidth;
        }
        int symmetricInset = Math.max(titlePosition[0], getMeasuredWidth() - titlePosition[1])
                + getResources().getDimensionPixelSize(R.dimen.coui_toolbar_action_menu_inner_padding);
        if (this.mIsInsidePanel || COUIChangeTextUtil.measureTextLineCount(this.mTitleTextView, getMeasuredWidth(), symmetricInset * 2) > 1) {
            return;
        }
        titlePosition[0] = symmetricInset;
        titlePosition[1] = getMeasuredWidth() - symmetricInset;
    }

    private void calculateToolbarPadding(MenuBuilder menuBuilder, int widthMeasureSpec, boolean hasVisibleMenu) {
        boolean hasEndContent = true;
        boolean hasStartContent = this.mHasCustomViewBeforeTitle || shouldLayout(this.mNavButtonView);
        if ((menuBuilder == null || (menuBuilder.getNonActionItems().isEmpty() && menuBuilder.getActionItems().isEmpty())) && !hasVisibleMenu) {
            hasEndContent = false;
        }
        if (COUIResponsiveUtils.isSmallScreen(getContext(), View.MeasureSpec.getSize(widthMeasureSpec))) {
            this.mToolbarNormalPaddingLeft = getContext().getResources().getDimensionPixelOffset(hasStartContent ? R.dimen.toolbar_normal_menu_padding_left_compat : R.dimen.toolbar_normal_padding_left_compat);
            this.mToolbarNormalPaddingRight = hasEndContent ? getContext().getResources().getDimensionPixelOffset(R.dimen.toolbar_normal_menu_padding_right_compat) : getContext().getResources().getDimensionPixelOffset(R.dimen.toolbar_normal_padding_right_compat);
            this.mToolbarCenterTitlePaddingLeft = getContext().getResources().getDimensionPixelOffset(R.dimen.toolbar_center_menu_padding_horizontal_compat);
        } else if (COUIResponsiveUtils.isMediumScreen(getContext(), View.MeasureSpec.getSize(widthMeasureSpec), UIUtil.getScreenHeightMetrics(getContext()))) {
            this.mToolbarNormalPaddingLeft = getContext().getResources().getDimensionPixelOffset(hasStartContent ? R.dimen.toolbar_normal_menu_padding_left_medium : R.dimen.toolbar_normal_padding_left_medium);
            this.mToolbarNormalPaddingRight = hasEndContent ? getContext().getResources().getDimensionPixelOffset(R.dimen.toolbar_normal_menu_padding_right_medium) : getContext().getResources().getDimensionPixelOffset(R.dimen.toolbar_normal_padding_right_medium);
            this.mToolbarCenterTitlePaddingLeft = getContext().getResources().getDimensionPixelOffset(R.dimen.toolbar_center_menu_padding_horizontal_medium);
        } else if (COUIResponsiveUtils.isLargeScreen(getContext(), View.MeasureSpec.getSize(widthMeasureSpec), UIUtil.getScreenHeightMetrics(getContext()))) {
            this.mToolbarNormalPaddingLeft = getContext().getResources().getDimensionPixelOffset(hasStartContent ? R.dimen.toolbar_normal_menu_padding_left_expanded : R.dimen.toolbar_normal_padding_left_expanded);
            this.mToolbarNormalPaddingRight = hasEndContent ? getContext().getResources().getDimensionPixelOffset(R.dimen.toolbar_normal_menu_padding_right_expanded) : getContext().getResources().getDimensionPixelOffset(R.dimen.toolbar_normal_padding_right_expanded);
            this.mToolbarCenterTitlePaddingLeft = getContext().getResources().getDimensionPixelOffset(R.dimen.toolbar_center_menu_padding_horizontal_expanded);
        }
        if (this.mIsInsideSideNavigationBar) {
            this.mToolbarCenterTitlePaddingLeft = getContext().getResources().getDimensionPixelOffset(R.dimen.toolbar_center_menu_padding_horizontal_medium);
        }
        this.mToolbarCenterTitlePaddingRight = this.mToolbarCenterTitlePaddingLeft;
        if (this.mIsTiny) {
            this.mToolbarNormalPaddingLeft = hasStartContent ? 0 : getContext().getResources().getDimensionPixelOffset(R.dimen.toolbar_normal_menu_padding_tiny_left);
            this.mToolbarNormalPaddingRight = getContext().getResources().getDimensionPixelOffset(R.dimen.toolbar_normal_menu_padding_tiny_right);
        }
    }

    private void changeBackViewParams() {
        ImageButton imageButton = this.mNavButtonView;
        if (imageButton == null || !this.mIsTiny) {
            return;
        }
        LayoutParams layoutParams = (LayoutParams) imageButton.getLayoutParams();
        ((ViewGroup.MarginLayoutParams) layoutParams).width = getContext().getResources().getDimensionPixelOffset(R.dimen.coui_toolbar_back_view_tiny_width);
        this.mNavButtonView.setLayoutParams(layoutParams);
        this.mNavButtonView.setPadding(0, 0, 0, 0);
    }

    private void changeToolbarPadding(MenuBuilder menuBuilder, ImageButton imageButton, boolean isRtl, int widthMeasureSpec, boolean hasCustomMenu) {
        if (menuBuilder == null && imageButton == null && !hasCustomMenu) {
            return;
        }
        calculateToolbarPadding(menuBuilder, widthMeasureSpec, hasCustomMenu);
        if ((menuBuilder == null || (menuBuilder.getNonActionItems().isEmpty() && menuBuilder.getActionItems().isEmpty())) && !hasCustomMenu) {
            if (this.mUseResponsivePadding) {
                int startPadding = this.mIsTitleCenterStyle ? this.mToolbarCenterTitlePaddingLeft : this.mToolbarNormalPaddingLeft;
                int endPadding = useTextMenuItemPaddingEnd() ? this.mToolbarCenterTitlePaddingRight : this.mToolbarNormalPaddingRight;
                if (isRtl) {
                    setPadding(endPadding, getPaddingTop(), startPadding, getPaddingBottom());
                    return;
                } else {
                    setPadding(startPadding, getPaddingTop(), endPadding, getPaddingBottom());
                    return;
                }
            }
            return;
        }
        if (this.mUseResponsivePadding) {
            int startPadding = this.mIsTitleCenterStyle ? this.mToolbarCenterTitlePaddingLeft : this.mToolbarNormalPaddingLeft;
            int endPadding = this.mIsTitleCenterStyle ? this.mToolbarCenterTitlePaddingRight : this.mToolbarNormalPaddingRight;
            if (isRtl) {
                setPadding(endPadding, getPaddingTop(), startPadding, getPaddingBottom());
            } else {
                setPadding(startPadding, getPaddingTop(), endPadding, getPaddingBottom());
            }
        }
    }

    private void configNavigationButtonBackground() {
        COUIMaskRippleDrawable maskRippleDrawable = new COUIMaskRippleDrawable(getContext());
        this.mMaskRippleDrawable = maskRippleDrawable;
        maskRippleDrawable.setCircleRippleMask(COUIMaskRippleDrawable.getMaskRippleRadiusByType(getContext(), 0));
        this.mNavButtonView.setBackground(this.mMaskRippleDrawable);
        COUIDarkModeUtil.setForceDarkAllow(this.mNavButtonView, false);
    }

    public void ensureCollapseButtonView() {
        if (this.mCollapseButtonView == null) {
            ImageButton imageButton = new ImageButton(getContext(), null, R.attr.couiToolbarNavigationButtonStyle, R.style.Widget_COUI_Toolbar_Button_Navigation);
            this.mCollapseButtonView = imageButton;
            imageButton.setImageDrawable(this.mCollapseIcon);
            this.mCollapseButtonView.setContentDescription(this.mCollapseDescription);
            LayoutParams toolbarLayoutParams = generateDefaultLayoutParams();
            toolbarLayoutParams.gravity = (this.mButtonGravity & Gravity.VERTICAL_GRAVITY_MASK) | Gravity.START;
            toolbarLayoutParams.mViewType = 2;
            this.mCollapseButtonView.setLayoutParams(toolbarLayoutParams);
            this.mCollapseButtonView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    COUIToolbar.this.collapseActionView();
                }
            });
        }
    }

    private void ensureLogoView() {
        if (this.mLogoView == null) {
            this.mLogoView = new ImageView(getContext());
        }
    }

    private void ensureMenu() {
        ensureMenuView();
        if (this.mMenuView.peekMenu() == null) {
            MenuBuilder menuBuilder = (MenuBuilder) this.mMenuView.getMenu();
            if (this.mExpandedMenuPresenter == null) {
                this.mExpandedMenuPresenter = new ExpandedActionViewMenuPresenter();
            }
            this.mMenuView.setExpandedActionViewsExclusive(true);
            menuBuilder.addMenuPresenter(this.mExpandedMenuPresenter, this.mPopupContext);
        }
    }

    private void ensureMenuView() {
        if (this.mMenuView == null) {
            COUIActionMenuView actionMenuView = new COUIActionMenuView(getContext());
            this.mMenuView = actionMenuView;
            actionMenuView.setId(R.id.coui_toolbar_more_view);
            this.mMenuView.setPopupTheme(this.mPopupTheme);
            this.mMenuView.setOnMenuItemClickListener(this.mMenuViewItemClickListener);
            this.mMenuView.setMenuCallbacks(this.mActionMenuPresenterCallback, this.mMenuBuilderCallback);
            LayoutParams toolbarLayoutParams = generateDefaultLayoutParams();
            if (this.mIsTitleCenterStyle) {
                ((ViewGroup.MarginLayoutParams) toolbarLayoutParams).width = -1;
            } else {
                ((ViewGroup.MarginLayoutParams) toolbarLayoutParams).width = -2;
            }
            toolbarLayoutParams.gravity = (this.mButtonGravity & Gravity.VERTICAL_GRAVITY_MASK) | Gravity.END;
            this.mMenuView.setLayoutParams(toolbarLayoutParams);
            addSystemView(this.mMenuView);
        }
    }

    private void ensureNavButtonView() {
        if (this.mNavButtonView == null) {
            ImageButton imageButton = new ImageButton(getContext(), null, R.attr.couiToolbarNavigationButtonStyle, R.style.Widget_COUI_Toolbar_Button_Navigation);
            this.mNavButtonView = imageButton;
            imageButton.setId(R.id.coui_toolbar_back_view);
            LayoutParams toolbarLayoutParams = generateDefaultLayoutParams();
            toolbarLayoutParams.gravity = (this.mButtonGravity & Gravity.VERTICAL_GRAVITY_MASK) | Gravity.START;
            this.mNavButtonView.setLayoutParams(toolbarLayoutParams);
            configNavigationButtonBackground();
            changeBackViewParams();
        }
    }

    private void ensureTitleTextView() {
        if (this.mTitleTextView == null) {
            Context context = getContext();
            TextView textView = new TextView(context);
            this.mTitleTextView = textView;
            textView.setPaddingRelative(0, this.mTitlePaddingTop, 0, this.mTitlePaddingBottom);
            LayoutParams toolbarLayoutParams = generateDefaultLayoutParams();
            toolbarLayoutParams.mTypeTitle = true;
            ((ViewGroup.MarginLayoutParams) toolbarLayoutParams).bottomMargin = this.mIsTiny ? 0 : this.mTitleMarginBottom;
            toolbarLayoutParams.gravity = (this.mButtonGravity & Gravity.VERTICAL_GRAVITY_MASK) | Gravity.END;
            this.mTitleTextView.setLayoutParams(toolbarLayoutParams);
            this.mTitleTextView.setSingleLine();
            this.mTitleTextView.setEllipsize(TextUtils.TruncateAt.END);
            int titleTextAppearance = this.mTitleTextAppearance;
            if (titleTextAppearance != 0) {
                setTitleTextAppearance(context, titleTextAppearance);
            }
            int titleTextColor = this.mTitleTextColor;
            if (titleTextColor != 0) {
                this.mTitleTextView.setTextColor(titleTextColor);
            }
            this.mTitleTextView.setTextAlignment(this.mIsTitleCenterStyle ? 4 : 5);
            if (this.mTitleType == TITLE_TYPE_SECONDARY) {
                this.mTitleTextView.setTextSize(0, COUIChangeTextUtil.getSuitableFontSize(this.mTitleTextView.getTextSize(), getContext().getResources().getConfiguration().fontScale, 2));
            }
        }
    }

    private int getChildHorizontalGravity(int gravity) {
        int layoutDirection = ViewCompat.getLayoutDirection(this);
        int horizontalGravity = GravityCompat.getAbsoluteGravity(gravity, layoutDirection)
                & Gravity.HORIZONTAL_GRAVITY_MASK;
        return (horizontalGravity == Gravity.CENTER_HORIZONTAL
                || horizontalGravity == Gravity.LEFT
                || horizontalGravity == Gravity.RIGHT)
                ? horizontalGravity
                : layoutDirection == ViewCompat.LAYOUT_DIRECTION_RTL
                        ? Gravity.RIGHT : Gravity.LEFT;
    }

    private int getChildTop(View view, int alignmentHeight) {
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        int measuredHeight = view.getMeasuredHeight();
        int alignmentOffset = alignmentHeight > 0 ? (measuredHeight - alignmentHeight) / 2 : 0;
        int childVerticalGravity = getChildVerticalGravity(layoutParams.gravity);
        if (childVerticalGravity == Gravity.TOP) {
            return getPaddingTop() - alignmentOffset;
        }
        if (childVerticalGravity == Gravity.BOTTOM) {
            return (((getHeight() - getPaddingBottom()) - measuredHeight) - ((ViewGroup.MarginLayoutParams) layoutParams).bottomMargin) - alignmentOffset;
        }
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        int height = getHeight();
        int childTop = (((height - paddingTop) - paddingBottom) - measuredHeight) / 2;
        int topMargin = ((ViewGroup.MarginLayoutParams) layoutParams).topMargin;
        if (childTop < topMargin) {
            childTop = topMargin;
        } else {
            int remainingBottomSpace = (((height - paddingBottom) - measuredHeight) - childTop) - paddingTop;
            int bottomMargin = ((ViewGroup.MarginLayoutParams) layoutParams).bottomMargin;
            if (remainingBottomSpace < bottomMargin) {
                childTop = Math.max(0, childTop - (bottomMargin - remainingBottomSpace));
            }
        }
        return paddingTop + childTop;
    }

    private int getChildVerticalGravity(int gravity) {
        int verticalGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;
        return (verticalGravity == Gravity.CENTER_VERTICAL || verticalGravity == Gravity.TOP || verticalGravity == Gravity.BOTTOM)
                ? verticalGravity : this.mGravity & Gravity.VERTICAL_GRAVITY_MASK;
    }

    private int getHorizontalMargins(View view) {
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        return MarginLayoutParamsCompat.getMarginStart(marginLayoutParams) + MarginLayoutParamsCompat.getMarginEnd(marginLayoutParams);
    }

    private int getMinimumHeightCompat() {
        return getMinimumHeight();
    }

    private int getVerticalMargins(View view) {
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        return marginLayoutParams.topMargin + marginLayoutParams.bottomMargin;
    }

    private int getViewListMeasuredWidth(List<View> list, int[] collapsingMargins) {
        int leftMarginCarry = collapsingMargins[0];
        int rightMarginCarry = collapsingMargins[1];
        int measuredWidth = 0;
        for (int index = 0; index < list.size(); index++) {
            View view = list.get(index);
            LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
            int leftMargin = ((ViewGroup.MarginLayoutParams) layoutParams).leftMargin - leftMarginCarry;
            int rightMargin = ((ViewGroup.MarginLayoutParams) layoutParams).rightMargin - rightMarginCarry;
            measuredWidth += Math.max(0, leftMargin) + view.getMeasuredWidth() + Math.max(0, rightMargin);
            leftMarginCarry = Math.max(0, -leftMargin);
            rightMarginCarry = Math.max(0, -rightMargin);
        }
        return measuredWidth;
    }

    private boolean isDummyView(View view, LayoutParams layoutParams) {
        if (view == null || view.getClass() != View.class) {
            return false;
        }
        this.mDummyView = view;
        return true;
    }

    private int layoutChildLeft(View view, int left, int alignmentHeight, int[] collapsingMargins, int verticalOffset) {
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        int leftMargin = ((ViewGroup.MarginLayoutParams) layoutParams).leftMargin - collapsingMargins[0];
        int childLeft = left + Math.max(0, leftMargin);
        collapsingMargins[0] = Math.max(0, -leftMargin);
        int childTop = getChildTop(view, verticalOffset);
        int measuredWidth = view.getMeasuredWidth();
        view.layout(childLeft, childTop, Math.min(alignmentHeight, childLeft + measuredWidth), view.getMeasuredHeight() + childTop);
        return childLeft + measuredWidth + ((ViewGroup.MarginLayoutParams) layoutParams).rightMargin;
    }

    private int layoutChildRight(View view, int right, int alignmentHeight, int[] collapsingMargins, int verticalOffset) {
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        int rightMargin = ((ViewGroup.MarginLayoutParams) layoutParams).rightMargin - collapsingMargins[1];
        int childRight = alignmentHeight - Math.max(0, rightMargin);
        collapsingMargins[1] = Math.max(0, -rightMargin);
        int childTop = getChildTop(view, verticalOffset);
        int measuredWidth = view.getMeasuredWidth();
        view.layout(Math.max(right, childRight - measuredWidth), childTop, childRight, view.getMeasuredHeight() + childTop);
        return childRight - (measuredWidth + ((ViewGroup.MarginLayoutParams) layoutParams).leftMargin);
    }

    private int measureChildCollapseMargins(View view, int parentWidthSpec, int widthUsed, int parentHeightSpec, int heightUsed, int[] collapsingMargins) {
        return measureChildCollapseMargins(view, parentWidthSpec, widthUsed, Integer.MAX_VALUE, parentHeightSpec, heightUsed, collapsingMargins);
    }

    private void measureChildConstrained(View view, int parentWidthSpec, int widthUsed, int parentHeightSpec, int heightUsed, int heightConstraint) {
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        int childMeasureSpec = ViewGroup.getChildMeasureSpec(parentWidthSpec, getPaddingLeft() + getPaddingRight() + marginLayoutParams.leftMargin + marginLayoutParams.rightMargin + widthUsed, marginLayoutParams.width);
        int childHeightSpec = ViewGroup.getChildMeasureSpec(parentHeightSpec, getPaddingTop() + getPaddingBottom() + marginLayoutParams.topMargin + marginLayoutParams.bottomMargin + heightUsed, marginLayoutParams.height);
        int mode = View.MeasureSpec.getMode(childHeightSpec);
        if (mode != View.MeasureSpec.EXACTLY && heightConstraint >= 0) {
            if (mode != 0) {
                heightConstraint = Math.min(View.MeasureSpec.getSize(childHeightSpec), heightConstraint);
            }
            childHeightSpec = View.MeasureSpec.makeMeasureSpec(heightConstraint, View.MeasureSpec.EXACTLY);
        }
        view.measure(childMeasureSpec, childHeightSpec);
    }

    private void measureChildMaxWidthConstrained(View view, int parentWidthSpec, int widthUsed, int parentHeightSpec, int heightUsed, int maxWidth) {
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        int childMeasureSpec = ViewGroup.getChildMeasureSpec(parentWidthSpec, getPaddingLeft() + getPaddingRight() + marginLayoutParams.leftMargin + marginLayoutParams.rightMargin + widthUsed, marginLayoutParams.width);
        int childHeightSpec = ViewGroup.getChildMeasureSpec(heightUsed, getPaddingTop() + getPaddingBottom() + marginLayoutParams.topMargin + marginLayoutParams.bottomMargin, marginLayoutParams.height);
        int mode = View.MeasureSpec.getMode(childHeightSpec);
        if (mode != View.MeasureSpec.EXACTLY && maxWidth >= 0) {
            if (mode != 0) {
                maxWidth = Math.min(View.MeasureSpec.getSize(childHeightSpec), maxWidth);
            }
            childHeightSpec = View.MeasureSpec.makeMeasureSpec(maxWidth, View.MeasureSpec.EXACTLY);
        }
        view.measure(childMeasureSpec, childHeightSpec);
        if (parentHeightSpec <= 0 || view.getMeasuredWidth() <= parentHeightSpec) {
            return;
        }
        view.measure(View.MeasureSpec.makeMeasureSpec(parentHeightSpec, View.MeasureSpec.EXACTLY), childHeightSpec);
    }

    private void refreshWidthLimits(int availableWidth) {
        if (COUIResponsiveUtils.isSmallScreen(getContext(), availableWidth)) {
            this.mSectionGap = this.mSectionGapSmall;
        } else {
            this.mSectionGap = this.mSectionGapMediumLarge;
        }
    }

    public void setChildVisibilityForExpandedActionView(boolean expanded) {
        int childCount = getChildCount();
        for (int index = 0; index < childCount; index++) {
            View childAt = getChildAt(index);
            if (((LayoutParams) childAt.getLayoutParams()).mViewType != 2 && childAt != this.mMenuView) {
                childAt.setVisibility(expanded ? 8 : 0);
            }
        }
    }

    private boolean shouldCollapse() {
        if (!this.mCollapsible) {
            return false;
        }
        int childCount = getChildCount();
        for (int index = 0; index < childCount; index++) {
            View childAt = getChildAt(index);
            if (shouldLayout(childAt) && childAt.getMeasuredWidth() > 0 && childAt.getMeasuredHeight() > 0) {
                return false;
            }
        }
        return true;
    }

    private boolean shouldLayout(View view) {
        return (view == null || view.getParent() != this || view.getVisibility() == 8) ? false : true;
    }

    private void updateChildVisibilityForExpandedActionView(View view) {
        if (((LayoutParams) view.getLayoutParams()).mViewType == 2 || view == this.mMenuView) {
            return;
        }
        view.setVisibility(this.mExpandedActionView != null ? 8 : 0);
    }

    private boolean useTextMenuItemPaddingEnd() {
        COUIActionMenuView actionMenuView = this.mMenuView;
        return this.mIsTitleCenterStyle || ((actionMenuView == null || actionMenuView.getChildCount() != 1 || !(this.mMenuView.getChildAt(0) instanceof COUIActionMenuItemView)) ? false : ((COUIActionMenuItemView) this.mMenuView.getChildAt(0)).isTextMenuItem());
    }

    @Override
    public boolean checkLayoutParams(ViewGroup.LayoutParams layoutParams) {
        return super.checkLayoutParams(layoutParams) && (layoutParams instanceof LayoutParams);
    }

    public void clearMenu() {
        this.mResId = 0;
        getMenu().clear();
    }

    @Override
    public void collapseActionView() {
        ExpandedActionViewMenuPresenter expandedActionViewMenuPresenter = this.mExpandedMenuPresenter;
        MenuItemImpl menuItemImpl = expandedActionViewMenuPresenter == null ? null : expandedActionViewMenuPresenter.mCurrentExpandedItem;
        if (menuItemImpl != null) {
            menuItemImpl.collapseActionView();
        }
    }

    @Override
    public void dismissPopupMenus() {
        COUIActionMenuView actionMenuView = this.mMenuView;
        if (actionMenuView != null) {
            actionMenuView.dismissPopupMenus();
        }
    }

    @Override
    public int getBarrierDirection() {
        if (this.mWindowFrame == null) {
            this.mWindowFrame = new Rect();
        }
        getRootView().getGlobalVisibleRect(this.mWindowFrame);
        return this.mWindowFrame.height() <= getContext().getResources().getDimensionPixelSize(R.dimen.coui_popup_list_window_min_window_height_to_apply_vertical_barrier) ? -1 : 1;
    }

    public TextView getCOUITitleTextView() {
        ensureTitleTextView();
        return this.mTitleTextView;
    }

    @Override
    public int getContentInsetEnd() {
        return this.mContentInsets.getEnd();
    }

    @Override
    public int getContentInsetLeft() {
        return this.mContentInsets.getLeft();
    }

    @Override
    public int getContentInsetRight() {
        return this.mContentInsets.getRight();
    }

    @Override
    public int getContentInsetStart() {
        return this.mContentInsets.getStart();
    }

    @Override
    public Rect getDisplayFrame() {
        if (this.mDisplayFrame == null) {
            this.mDisplayFrame = new Rect();
        }
        getGlobalVisibleRect(this.mDisplayFrame);
        return this.mDisplayFrame;
    }

    public boolean getIsTitleCenterStyle() {
        return this.mIsTitleCenterStyle;
    }

    @Override
    public Drawable getLogo() {
        ImageView imageView = this.mLogoView;
        if (imageView != null) {
            return imageView.getDrawable();
        }
        return null;
    }

    @Override
    public CharSequence getLogoDescription() {
        ImageView imageView = this.mLogoView;
        if (imageView != null) {
            return imageView.getContentDescription();
        }
        return null;
    }

    @Override
    public Menu getMenu() {
        ensureMenu();
        return this.mMenuView.getMenu();
    }

    public COUIActionMenuView getMenuView() {
        ensureMenuView();
        return this.mMenuView;
    }

    @Override
    public CharSequence getNavigationContentDescription() {
        ImageButton imageButton = this.mNavButtonView;
        if (imageButton != null) {
            return imageButton.getContentDescription();
        }
        return null;
    }

    @Override
    public Drawable getNavigationIcon() {
        ImageButton imageButton = this.mNavButtonView;
        if (imageButton != null) {
            return imageButton.getDrawable();
        }
        return null;
    }

    @Override
    public Rect getOutsets() {
        return TOOLBAR_OUTSETS;
    }

    public View getOverFlowMenuButton() {
        COUIActionMenuView actionMenuView = this.mMenuView;
        if (actionMenuView != null) {
            return actionMenuView.getOverFlowMenuButton();
        }
        return null;
    }

    @Override
    public Drawable getOverflowIcon() {
        ensureMenu();
        return this.mMenuView.getOverflowIcon();
    }

    @Override
    public boolean getPopupMenuRuleEnabled() {
        return this.mPopupRuleEnable;
    }

    @Override
    public int getPopupTheme() {
        return this.mPopupTheme;
    }

    public int getSectionGapMediumLarge() {
        return this.mSectionGapMediumLarge;
    }

    @Override
    public CharSequence getSubtitle() {
        return this.mSubtitleText;
    }

    @Override
    public CharSequence getTitle() {
        return this.mTitleText;
    }

    public View getTitleView() {
        return this.mTitleTextView;
    }

    @Override
    public int getType() {
        return 2;
    }

    @Override
    public void inflateMenu(int menuResId) {
        super.inflateMenu(menuResId);
        this.mResId = menuResId;
        COUIActionMenuView actionMenuView = this.mMenuView;
        if (actionMenuView instanceof COUIActionMenuView) {
            actionMenuView.clearRedDotInfo();
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(this.mShowOverflowMenuRunnable);
    }

    @Override
    public boolean onHoverEvent(MotionEvent motionEvent) {
        int action = motionEvent.getActionMasked();
        if (action == MotionEvent.ACTION_HOVER_ENTER) {
            this.mEatingHover = false;
        }
        if (!this.mEatingHover) {
            boolean handled = super.onHoverEvent(motionEvent);
            if (action == MotionEvent.ACTION_HOVER_ENTER && !handled) {
                this.mEatingHover = true;
            }
        }
        if (action == MotionEvent.ACTION_HOVER_EXIT || action == MotionEvent.ACTION_CANCEL) {
            this.mEatingHover = false;
        }
        return true;
    }

    @Override
    public void onLayout(boolean changed, int layoutLeft, int layoutTop, int layoutRight, int layoutBottom) {
        boolean isRtl = ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL;
        int width = getWidth();
        int height = getHeight();
        int left = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int top = getPaddingTop();
        int bottom = getPaddingBottom();
        int right = width - paddingRight;
        int[] collapsingMargins = this.mTempMargins;
        collapsingMargins[0] = 0;
        collapsingMargins[1] = 0;
        int alignmentHeight = getMinimumHeightCompat();

        if (shouldLayout(this.mNavButtonView)) {
            if (isRtl) {
                right = layoutChildRight(this.mNavButtonView, left, right, collapsingMargins, alignmentHeight);
            } else {
                left = layoutChildLeft(this.mNavButtonView, left, right, collapsingMargins, alignmentHeight);
            }
        }
        if (shouldLayout(this.mCollapseButtonView)) {
            if (isRtl) {
                right = layoutChildRight(this.mCollapseButtonView, left, right, collapsingMargins, alignmentHeight);
            } else {
                left = layoutChildLeft(this.mCollapseButtonView, left, right, collapsingMargins, alignmentHeight);
            }
        }
        if (shouldLayout(this.mTextButton)) {
            if (isRtl) {
                left = layoutChildLeft(this.mTextButton, left, right, collapsingMargins, alignmentHeight);
            } else {
                right = layoutChildRight(this.mTextButton, left, right, collapsingMargins, alignmentHeight);
            }
        }

        boolean isSmallScreen = COUIResponsiveUtils.isSmallScreen(getContext(), getMeasuredWidth());
        if (shouldLayout(this.mMenuView)) {
            if (isRtl) {
                left = layoutChildLeft(this.mMenuView, left, right, collapsingMargins, alignmentHeight);
                if (!isSmallScreen) {
                    left += this.mGapBeforeMenuView;
                }
            } else {
                right = layoutChildRight(this.mMenuView, left, right, collapsingMargins, alignmentHeight);
                if (!isSmallScreen) {
                    right -= this.mGapBeforeMenuView;
                }
            }
        }

        if (shouldLayout(this.mSegmentButton) && !this.mIsTitleCenterStyle) {
            int segmentWidth = this.mSegmentButton.getMeasuredWidth();
            int segmentLeft = (width / 2) - (segmentWidth / 2);
            int segmentRight = segmentLeft + segmentWidth;
            if (segmentLeft < left) {
                segmentLeft = left;
            } else if (segmentRight > right) {
                segmentLeft -= segmentRight - right;
            }
            LayoutParams segmentLp = (LayoutParams) this.mSegmentButton.getLayoutParams();
            if (isRtl) {
                int newLeft = layoutChildLeft(this.mSegmentButton, segmentLeft, right, collapsingMargins, alignmentHeight);
                left = newLeft;
            } else {
                int nextLeft = layoutChildLeft(this.mSegmentButton, segmentLeft, right, collapsingMargins, alignmentHeight);
                right = nextLeft - (this.mSegmentButton.getMeasuredWidth() + segmentLp.rightMargin);
            }
        }

        if (shouldLayout(this.mTitleTextView)) {
            addCustomViewsWithGravity(this.mTempViews, 1);
            int centerViewsWidth = getViewListMeasuredWidth(this.mTempViews, collapsingMargins);
            int centeredLeft = (width / 2) - (centerViewsWidth / 2);
            int centeredRight = centeredLeft + centerViewsWidth;
            if (centeredLeft < left) {
                centeredLeft = left;
            } else if (centeredRight > right) {
                centeredLeft -= centeredRight - right;
            }
            int centerCount = this.mTempViews.size();
            int centerLeft = centeredLeft;
            for (int index = 0; index < centerCount; index++) {
                View child = this.mTempViews.get(index);
                ViewGroup.LayoutParams childLp = child.getLayoutParams();
                if (childLp instanceof LayoutParams && ((LayoutParams) childLp).mTypeSegmentButton) {
                    continue;
                }
                centerLeft = layoutChildLeft(child, centerLeft, right, collapsingMargins, alignmentHeight);
            }
        }

        collapsingMargins[0] = Math.max(0, getContentInsetLeft() - left);
        collapsingMargins[1] = Math.max(0, getContentInsetRight() - ((width - paddingRight) - right));
        left = Math.max(left, getContentInsetLeft());
        right = Math.min(right, width - getContentInsetRight());

        if (shouldLayout(this.mExpandedActionView)) {
            if (isRtl) {
                right = layoutChildRight(this.mExpandedActionView, left, right, collapsingMargins, alignmentHeight);
            } else {
                left = layoutChildLeft(this.mExpandedActionView, left, right, collapsingMargins, alignmentHeight);
            }
        }
        if (shouldLayout(this.mLogoView)) {
            if (isRtl) {
                right = layoutChildRight(this.mLogoView, left, right, collapsingMargins, alignmentHeight);
            } else {
                left = layoutChildLeft(this.mLogoView, left, right, collapsingMargins, alignmentHeight);
            }
        }

        addCustomViewsWithGravity(this.mTempViews, 3);
        int leftCount = this.mTempViews.size();
        if (this.mHasSearchViewFlag) {
            for (int index = 0; index < leftCount; index++) {
                View child = this.mTempViews.get(index);
                LayoutParams childLp = (LayoutParams) child.getLayoutParams();
                if (isDummyView(child, childLp)) {
                    continue;
                }
                if (childLp.mTypeSearch) {
                    left = layoutChildLeft(child, 0, width, this.mSearchCollapsingMargins, 0);
                } else {
                    left = layoutChildLeft(child, left, right, collapsingMargins, alignmentHeight);
                }
            }
        } else {
            for (int index = 0; index < leftCount; index++) {
                View child = this.mTempViews.get(index);
                LayoutParams childLp = (LayoutParams) child.getLayoutParams();
                if (childLp.mTypeTextButton || isDummyView(child, childLp)) {
                    continue;
                }
                left = layoutChildLeft(child, left, right, collapsingMargins, alignmentHeight);
            }
        }

        addCustomViewsWithGravity(this.mTempViews, 5);
        int rightCount = this.mTempViews.size();
        if (this.mHasSearchViewFlag) {
            for (int index = 0; index < rightCount; index++) {
                View child = this.mTempViews.get(index);
                LayoutParams childLp = (LayoutParams) child.getLayoutParams();
                if (isDummyView(child, childLp)) {
                    continue;
                }
                if (childLp.mTypeSearch) {
                    right = layoutChildRight(child, 0, width, this.mSearchCollapsingMargins, 0);
                } else {
                    right = layoutChildRight(child, left, right, collapsingMargins, alignmentHeight);
                }
            }
        } else if (isRtl) {
            for (int index = rightCount - 1; index >= 0; index--) {
                View child = this.mTempViews.get(index);
                LayoutParams childLp = (LayoutParams) child.getLayoutParams();
                if (childLp.mTypeTextButton || isDummyView(child, childLp)) {
                    continue;
                }
                right = layoutChildRight(child, left, right, collapsingMargins, alignmentHeight);
            }
        } else {
            for (int index = 0; index < rightCount; index++) {
                View child = this.mTempViews.get(index);
                LayoutParams childLp = (LayoutParams) child.getLayoutParams();
                if (childLp.mTypeTextButton || isDummyView(child, childLp)) {
                    continue;
                }
                right = layoutChildRight(child, left, right, collapsingMargins, alignmentHeight);
            }
        }

        if (!shouldLayout(this.mTitleTextView)) {
            addCustomViewsWithGravity(this.mTempViews, 1);
            int centerViewsWidth = getViewListMeasuredWidth(this.mTempViews, collapsingMargins);
            int centeredLeft = (width / 2) - (centerViewsWidth / 2);
            int centeredRight = centeredLeft + centerViewsWidth;
            int minCenterLeft = left + this.mSectionGap;
            if (centeredLeft < minCenterLeft) {
                centeredLeft = minCenterLeft;
            } else {
                int maxCenterRight = right - this.mSectionGap;
                if (centeredRight > maxCenterRight) {
                    centeredLeft -= centeredRight - maxCenterRight;
                }
            }
            int centerCount = this.mTempViews.size();
            int centerLeft = centeredLeft;
            for (int index = 0; index < centerCount; index++) {
                View child = this.mTempViews.get(index);
                ViewGroup.LayoutParams childLp = child.getLayoutParams();
                if (childLp instanceof LayoutParams && ((LayoutParams) childLp).mTypeSegmentButton) {
                    continue;
                }
                centerLeft = layoutChildLeft(child, centerLeft, right, collapsingMargins, alignmentHeight);
            }
        }
        this.mTempViews.clear();

        boolean hasTitle = shouldLayout(this.mTitleTextView);
        boolean hasSubtitle = shouldLayout(this.mSubtitleTextView);
        int titleHeight = 0;
        if (hasTitle) {
            LayoutParams lp = (LayoutParams) this.mTitleTextView.getLayoutParams();
            titleHeight = lp.topMargin + this.mTitleTextView.getMeasuredHeight() + lp.bottomMargin;
        }
        if (hasSubtitle) {
            LayoutParams lp = (LayoutParams) this.mSubtitleTextView.getLayoutParams();
            titleHeight += lp.topMargin + this.mSubtitleTextView.getMeasuredHeight() + lp.bottomMargin;
        }

        int titleLeftBoundary = left;
        int titleRightBoundary = right;
        if (hasTitle || hasSubtitle) {
            View firstTitleView = hasTitle ? this.mTitleTextView : this.mSubtitleTextView;
            View lastTitleView = hasSubtitle ? this.mSubtitleTextView : this.mTitleTextView;
            LayoutParams firstLp = (LayoutParams) firstTitleView.getLayoutParams();
            LayoutParams lastLp = (LayoutParams) lastTitleView.getLayoutParams();
            boolean hasTitleWidth = (hasTitle && this.mTitleTextView.getMeasuredWidth() > 0) || (hasSubtitle && this.mSubtitleTextView.getMeasuredWidth() > 0);

            int titleTop;
            int verticalGravity = this.mGravity & Gravity.VERTICAL_GRAVITY_MASK;
            if (verticalGravity == Gravity.TOP) {
                titleTop = getPaddingTop() + firstLp.topMargin + this.mTitleMarginTop;
            } else if (verticalGravity == Gravity.BOTTOM) {
                titleTop = ((height - bottom) - lastLp.bottomMargin) - this.mTitleMarginBottom - titleHeight;
            } else {
                int titleSpace = (((height - top) - bottom) - titleHeight) / 2;
                int topMargin = firstLp.topMargin + this.mTitleMarginTop;
                if (titleSpace < topMargin) {
                    titleSpace = topMargin;
                } else {
                    int bottomSpace = (((height - bottom) - titleHeight) - titleSpace) - top;
                    int bottomMargin = firstLp.bottomMargin + this.mTitleMarginBottom;
                    if (bottomSpace < bottomMargin) {
                        titleSpace = Math.max(0, titleSpace - ((lastLp.bottomMargin + this.mTitleMarginBottom) - bottomSpace));
                    }
                }
                titleTop = top + titleSpace;
            }

            if (this.mIsTitleCenterStyle) {
                int titleWidth = hasTitle ? this.mTitleTextView.getMeasuredWidth() : 0;
                int subtitleWidth = hasSubtitle ? this.mSubtitleTextView.getMeasuredWidth() : 0;
                int maxTitleWidth = Math.max(titleWidth, subtitleWidth);
                int availableCenterWidth = getWidth() - (Math.max(this.mTitlePosition[0], getWidth() - this.mTitlePosition[1]) * 2);
                int titlePositionWidth = this.mTitlePosition[1] - this.mTitlePosition[0];

                if (hasTitle) {
                    LayoutParams lp = (LayoutParams) this.mTitleTextView.getLayoutParams();
                    int measuredWidth = this.mTitleTextView.getMeasuredWidth();
                    int titleLeft = (getWidth() - measuredWidth) / 2;
                    int titleRight = titleLeft + measuredWidth;
                    int titleBottom = titleTop + this.mTitleTextView.getMeasuredHeight();
                    if (availableCenterWidth < maxTitleWidth) {
                        if (measuredWidth >= titlePositionWidth) {
                            titleLeft = this.mTitlePosition[0];
                            titleRight = this.mTitlePosition[1];
                        } else {
                            titleLeft = this.mTitlePosition[0] + ((titlePositionWidth - measuredWidth) / 2);
                            titleRight = titleLeft + measuredWidth;
                        }
                    }
                    this.mTitleTextView.layout(titleLeft, titleTop, titleRight, titleBottom);
                    titleTop = titleBottom + lp.bottomMargin;
                }
                if (hasSubtitle) {
                    LayoutParams lp = (LayoutParams) this.mSubtitleTextView.getLayoutParams();
                    titleTop += lp.topMargin;
                    int measuredWidth = this.mSubtitleTextView.getMeasuredWidth();
                    int subtitleLeft = (getWidth() - measuredWidth) / 2;
                    int subtitleRight = subtitleLeft + measuredWidth;
                    int subtitleBottom = titleTop + this.mSubtitleTextView.getMeasuredHeight();
                    if (availableCenterWidth < maxTitleWidth) {
                        if (measuredWidth >= titlePositionWidth) {
                            subtitleLeft = this.mTitlePosition[0];
                            subtitleRight = this.mTitlePosition[1];
                        } else {
                            subtitleLeft = this.mTitlePosition[0] + ((titlePositionWidth - measuredWidth) / 2);
                            subtitleRight = subtitleLeft + measuredWidth;
                        }
                    }
                    this.mSubtitleTextView.layout(subtitleLeft, titleTop, subtitleRight, subtitleBottom);
                }
            } else if (isRtl) {
                int titleStart = hasTitleWidth ? this.mTitleMarginStart : 0;
                titleStart -= collapsingMargins[1];
                titleStart += (!this.mHasCustomViewBeforeTitle && !shouldLayout(this.mNavButtonView)) ? 0 : this.mGapBetweenNavigationAndTitle;
                int titleRight = this.mIsTiny ? right : right - Math.max(0, titleStart);
                collapsingMargins[1] = Math.max(0, -titleStart);

                int titleLeft = titleRight;
                int subtitleLeft = titleRight;
                if (hasTitle) {
                    LayoutParams lp = (LayoutParams) this.mTitleTextView.getLayoutParams();
                    titleLeft = Math.max(titleLeftBoundary, titleRight - this.mTitleTextView.getMeasuredWidth());
                    int titleBottom = titleTop + this.mTitleTextView.getMeasuredHeight();
                    this.mTitleTextView.layout(titleLeft, titleTop, titleRight, titleBottom);
                    titleLeft -= this.mTitleMarginEnd;
                    titleTop = titleBottom + lp.bottomMargin;
                }
                if (hasSubtitle) {
                    LayoutParams lp = (LayoutParams) this.mSubtitleTextView.getLayoutParams();
                    titleTop += lp.topMargin;
                    subtitleLeft = titleRight - this.mSubtitleTextView.getMeasuredWidth();
                    int subtitleBottom = titleTop + this.mSubtitleTextView.getMeasuredHeight();
                    this.mSubtitleTextView.layout(subtitleLeft, titleTop, titleRight, subtitleBottom);
                    subtitleLeft = titleRight - this.mTitleMarginEnd;
                }
                if (hasTitleWidth) {
                    titleRight = Math.min(titleLeft, subtitleLeft);
                }
                titleRightBoundary = titleRight;
            } else {
                int titleStart = hasTitleWidth ? this.mTitleMarginStart : 0;
                titleStart -= collapsingMargins[0];
                titleStart += (!this.mHasCustomViewBeforeTitle && !shouldLayout(this.mNavButtonView)) ? 0 : this.mGapBetweenNavigationAndTitle;
                int titleLeft = this.mIsTiny ? left : left + Math.max(0, titleStart);
                collapsingMargins[0] = Math.max(0, -titleStart);

                int titleRight = titleLeft;
                int subtitleRight = titleLeft;
                if (hasTitle) {
                    LayoutParams lp = (LayoutParams) this.mTitleTextView.getLayoutParams();
                    titleRight = Math.min(titleLeft + this.mTitleTextView.getMeasuredWidth(), titleRightBoundary);
                    int titleBottom = titleTop + this.mTitleTextView.getMeasuredHeight();
                    this.mTitleTextView.layout(titleLeft, titleTop, titleRight, titleBottom);
                    titleRight += this.mTitleMarginEnd;
                    titleTop = titleBottom + lp.bottomMargin;
                }
                if (hasSubtitle) {
                    LayoutParams lp = (LayoutParams) this.mSubtitleTextView.getLayoutParams();
                    titleTop += lp.topMargin;
                    subtitleRight = titleLeft + this.mSubtitleTextView.getMeasuredWidth();
                    int subtitleBottom = titleTop + this.mSubtitleTextView.getMeasuredHeight();
                    this.mSubtitleTextView.layout(titleLeft, titleTop, subtitleRight, subtitleBottom);
                    subtitleRight += this.mTitleMarginEnd;
                }
                titleLeftBoundary = hasTitleWidth ? Math.max(titleRight, subtitleRight) : titleLeft;
            }
        }

        if (shouldLayout(this.mDummyView)) {
            if (isRtl) {
                layoutChildRight(this.mDummyView, titleLeftBoundary, titleRightBoundary, collapsingMargins, alignmentHeight);
            } else {
                layoutChildLeft(this.mDummyView, titleLeftBoundary, titleRightBoundary, collapsingMargins, alignmentHeight);
            }
        }
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int availableWidth = View.MeasureSpec.getSize(widthMeasureSpec);
        refreshWidthLimits(availableWidth);
        boolean isRtl = ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL;
        if (this.mIsTitleCenterStyle) {
            int[] collapsingMargins = this.mTempMargins;
            int endMarginIndex = !isRtl ? 1 : 0;
            int contentInsetStart = getContentInsetStart();
            int startInsetWidth = Math.max(contentInsetStart, 0);
            collapsingMargins[isRtl ? 1 : 0] = Math.max(0, contentInsetStart);
            int menuWidth;
            int maxChildHeight;
            int measuredState;
            if (shouldLayout(this.mMenuView)) {
                changeToolbarPadding((MenuBuilder) this.mMenuView.getMenu(), null, isRtl, widthMeasureSpec, false);
                measureChildConstrained(this.mMenuView, widthMeasureSpec, 0, heightMeasureSpec, 0, this.mMaxButtonHeight);
                menuWidth = this.mMenuView.getMeasuredWidth() + getHorizontalMargins(this.mMenuView);
                maxChildHeight = Math.max(0, this.mMenuView.getMeasuredHeight() + getVerticalMargins(this.mMenuView));
                measuredState = View.combineMeasuredStates(0, ViewCompat.getMeasuredState(this.mMenuView));
            } else {
                menuWidth = 0;
                measuredState = 0;
                maxChildHeight = 0;
            }
            int contentInsetEnd = getContentInsetEnd();
            int widthUsed = startInsetWidth + Math.max(contentInsetEnd, menuWidth);
            collapsingMargins[endMarginIndex] = Math.max(0, contentInsetEnd - menuWidth);
            if (shouldLayout(this.mExpandedActionView)) {
                widthUsed += measureChildCollapseMargins(this.mExpandedActionView, widthMeasureSpec, widthUsed, heightMeasureSpec, 0, collapsingMargins);
                maxChildHeight = Math.max(maxChildHeight, this.mExpandedActionView.getMeasuredHeight() + getVerticalMargins(this.mExpandedActionView));
                measuredState = View.combineMeasuredStates(measuredState, ViewCompat.getMeasuredState(this.mExpandedActionView));
            }
            int childCount = getChildCount();
            for (int childIndex = 0; childIndex < childCount; childIndex++) {
                View childAt = getChildAt(childIndex);
                LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
                if (layoutParams.mViewType == 0 && shouldLayout(childAt) && !layoutParams.mTypeSegmentButton) {
                    widthUsed += measureChildCollapseMargins(childAt, widthMeasureSpec, widthUsed, heightMeasureSpec, 0, collapsingMargins);
                    maxChildHeight = Math.max(maxChildHeight, childAt.getMeasuredHeight() + getVerticalMargins(childAt));
                    measuredState = View.combineMeasuredStates(measuredState, ViewCompat.getMeasuredState(childAt));
                }
            }
            int customViewsHeight = maxChildHeight;
            int titleVerticalMargins = this.mTitleMarginTop + this.mTitleMarginBottom;
            int titleBlockHeight;
            int titleBlockWidth;
            if (shouldLayout(this.mTitleTextView)) {
                this.mTitleTextView.getLayoutParams().width = -2;
                this.mTitleTextView.setTextSize(0, this.mTitleTextSize);
                measureChildCollapseMargins(this.mTitleTextView, widthMeasureSpec, 0, heightMeasureSpec, titleVerticalMargins, collapsingMargins);
                int titleMeasuredWidth = this.mTitleTextView.getMeasuredWidth() + getHorizontalMargins(this.mTitleTextView);
                titleBlockHeight = this.mTitleTextView.getMeasuredHeight() + getVerticalMargins(this.mTitleTextView);
                measuredState = View.combineMeasuredStates(measuredState, ViewCompat.getMeasuredState(this.mTitleTextView));
                titleBlockWidth = titleMeasuredWidth;
            } else {
                titleBlockHeight = 0;
                titleBlockWidth = 0;
            }
            int titleHeightBeforeSubtitle = titleBlockHeight;
            if (shouldLayout(this.mSubtitleTextView)) {
                this.mSubtitleTextView.getLayoutParams().width = -2;
                titleBlockWidth = Math.max(titleBlockWidth, measureChildCollapseMargins(this.mSubtitleTextView, widthMeasureSpec, 0, heightMeasureSpec, titleBlockHeight + titleVerticalMargins, collapsingMargins));
                measuredState = View.combineMeasuredStates(measuredState, ViewCompat.getMeasuredState(this.mSubtitleTextView));
            }
            int contentHeight = Math.max(customViewsHeight, titleHeightBeforeSubtitle);
            int paddingLeft = widthUsed + titleBlockWidth + getPaddingLeft() + getPaddingRight();
            int paddingTop = contentHeight + getPaddingTop() + getPaddingBottom();
            int resolvedWidth = ViewCompat.resolveSizeAndState(Math.max(paddingLeft, getSuggestedMinimumWidth()), widthMeasureSpec, (View.MEASURED_STATE_MASK) & measuredState);
            int resolvedHeight = ViewCompat.resolveSizeAndState(Math.max(paddingTop, getSuggestedMinimumHeight()), heightMeasureSpec, measuredState << 16);
            if (shouldCollapse()) {
                resolvedHeight = 0;
            }
            setMeasuredDimension(resolvedWidth, resolvedHeight);
            calculateTitlePosition(this.mTitlePosition);
            int[] titlePosition = this.mTitlePosition;
            int availableTitleWidth = titlePosition[1] - titlePosition[0];
            if (shouldLayout(this.mTitleTextView)) {
                this.mTitleTextView.setMaxWidth(availableTitleWidth);
                measureChildCollapseMargins(this.mTitleTextView, View.MeasureSpec.makeMeasureSpec(availableTitleWidth, Integer.MIN_VALUE), 0, heightMeasureSpec, titleVerticalMargins, collapsingMargins);
            }
            if (shouldLayout(this.mSubtitleTextView)) {
                this.mSubtitleTextView.setMaxWidth(availableTitleWidth);
                measureChildCollapseMargins(this.mSubtitleTextView, View.MeasureSpec.makeMeasureSpec(availableTitleWidth, Integer.MIN_VALUE), 0, heightMeasureSpec, titleHeightBeforeSubtitle + titleVerticalMargins, collapsingMargins);
                return;
            }
            return;
        }
        int[] collapsingMargins = this.mTempMargins;
        int endMarginIndex = !isRtl ? 1 : 0;
        int startButtonWidth;
        int maxChildHeight;
        int measuredState;
        if (shouldLayout(this.mNavButtonView)) {
            changeToolbarPadding(null, this.mNavButtonView, isRtl, widthMeasureSpec, false);
            measureChildConstrained(this.mNavButtonView, widthMeasureSpec, 0, heightMeasureSpec, 0, this.mMaxButtonHeight);
            startButtonWidth = this.mNavButtonView.getMeasuredWidth() + getHorizontalMargins(this.mNavButtonView);
            maxChildHeight = Math.max(0, this.mNavButtonView.getMeasuredHeight() + getVerticalMargins(this.mNavButtonView));
            measuredState = View.combineMeasuredStates(0, ViewCompat.getMeasuredState(this.mNavButtonView));
        } else {
            startButtonWidth = 0;
            measuredState = 0;
            maxChildHeight = 0;
        }
        if (shouldLayout(this.mCollapseButtonView)) {
            measureChildConstrained(this.mCollapseButtonView, widthMeasureSpec, 0, heightMeasureSpec, 0, this.mMaxButtonHeight);
            startButtonWidth = this.mCollapseButtonView.getMeasuredWidth() + getHorizontalMargins(this.mCollapseButtonView);
            maxChildHeight = Math.max(maxChildHeight, this.mCollapseButtonView.getMeasuredHeight() + getVerticalMargins(this.mCollapseButtonView));
            measuredState = View.combineMeasuredStates(measuredState, ViewCompat.getMeasuredState(this.mCollapseButtonView));
        }
        int contentInsetStart = getContentInsetStart();
        int startWidthUsed = Math.max(contentInsetStart, startButtonWidth);
        collapsingMargins[isRtl ? 1 : 0] = Math.max(0, contentInsetStart - startButtonWidth);
        int endActionsWidth;
        if (shouldLayout(this.mTextButton)) {
            measureChildCollapseMargins(this.mTextButton, widthMeasureSpec, startWidthUsed, heightMeasureSpec, 0, collapsingMargins);
            int textButtonWidth = this.mTextButton.getMeasuredWidth() + getHorizontalMargins(this.mTextButton);
            maxChildHeight = Math.max(maxChildHeight, this.mTextButton.getMeasuredHeight() + getVerticalMargins(this.mTextButton));
            measuredState = View.combineMeasuredStates(measuredState, ViewCompat.getMeasuredState(this.mTextButton));
            endActionsWidth = textButtonWidth;
        } else {
            endActionsWidth = 0;
        }
        boolean isSmallScreen = COUIResponsiveUtils.isSmallScreen(getContext(), View.MeasureSpec.getSize(widthMeasureSpec));
        MenuBuilder menuBuilder;
        if (shouldLayout(this.mMenuView)) {
            menuBuilder = (MenuBuilder) this.mMenuView.getMenu();
            changeToolbarPadding(menuBuilder, this.mNavButtonView, isRtl, widthMeasureSpec, false);
            measureChildConstrained(this.mMenuView, widthMeasureSpec, startWidthUsed, heightMeasureSpec, 0, this.mMaxButtonHeight);
            endActionsWidth += this.mMenuView.getMeasuredWidth() + getHorizontalMargins(this.mMenuView);
            maxChildHeight = Math.max(maxChildHeight, this.mMenuView.getMeasuredHeight() + getVerticalMargins(this.mMenuView));
            measuredState = View.combineMeasuredStates(measuredState, ViewCompat.getMeasuredState(this.mMenuView));
        } else {
            menuBuilder = null;
        }
        if (endActionsWidth > 0) {
            changeToolbarPadding(null, this.mNavButtonView, isRtl, widthMeasureSpec, true);
            if (!isSmallScreen) {
                endActionsWidth += this.mGapBeforeMenuView;
            }
        }
        addCustomViewsWithGravity(this.mTempViews, Gravity.START);
        this.mHasCustomViewBeforeTitle = (this.mTempViews.isEmpty() || isSmallScreen) ? false : true;
        int startViewCount = this.mTempViews.size();
        for (int startViewIndex = 0; startViewIndex < startViewCount; startViewIndex++) {
            View view = this.mTempViews.get(startViewIndex);
            LayoutParams childLayoutParams = (LayoutParams) view.getLayoutParams();
            if (childLayoutParams.mViewType != 0 || childLayoutParams.mTypeTextButton || childLayoutParams.mTypeSegmentButton || isDummyView(view, childLayoutParams) || !shouldLayout(view)) {
                if (isDummyView(view, childLayoutParams) && startViewCount == 1) {
                    this.mHasCustomViewBeforeTitle = false;
                    break;
                }
            } else {
                startWidthUsed += measureChildCollapseMargins(view, widthMeasureSpec, startWidthUsed + endActionsWidth, heightMeasureSpec, 0, collapsingMargins);
                maxChildHeight = Math.max(maxChildHeight, view.getMeasuredHeight() + getVerticalMargins(view));
                measuredState = View.combineMeasuredStates(measuredState, ViewCompat.getMeasuredState(view));
            }
        }
        changeToolbarPadding(menuBuilder, this.mNavButtonView, isRtl, widthMeasureSpec,
                this.mHasCustomViewBeforeTitle || endActionsWidth > 0);
        int titleVerticalMargins = this.mTitleMarginTop + this.mTitleMarginBottom;
        int titleHorizontalMargins = this.mTitleMarginStart + this.mTitleMarginEnd;
        int segmentWidth;
        int titleTextWidth;
        if (!shouldLayout(this.mSegmentButton) || this.mIsTitleCenterStyle) {
            segmentWidth = 0;
        } else {
            if (shouldLayout(this.mTitleTextView)) {
                this.mTitleTextView.getLayoutParams().width = -2;
                this.mTitleTextView.setTextSize(0, this.mTitleTextSize);
                TextPaint paint = this.mTitleTextView.getPaint();
                CharSequence titleText = this.mTitleText;
                titleTextWidth = (int) paint.measureText(titleText, 0, titleText.length());
            } else {
                titleTextWidth = 0;
            }
            int availableStartHalf = (availableWidth / 2) - startWidthUsed;
            if (titleTextWidth <= ((availableStartHalf - (this.mSegmentButtonMinWidth / 2)) - this.mSectionGap) - getPaddingStart()) {
                int paddingStart = (((availableStartHalf - titleTextWidth) - this.mSectionGap) - getPaddingStart()) * 2;
                measureChildMaxWidthConstrained(this.mSegmentButton, widthMeasureSpec, startWidthUsed, isSmallScreen ? Math.min(paddingStart, this.mSegmentButtonMaxWidth) : paddingStart, heightMeasureSpec, this.mSegmentButtonHeight);
            } else {
                measureChildMaxWidthConstrained(this.mSegmentButton, widthMeasureSpec, startWidthUsed, (((availableStartHalf - getPaddingStart()) - this.mSectionGap) / 2) * 2, heightMeasureSpec, this.mSegmentButtonHeight);
            }
            segmentWidth = this.mSegmentButton.getMeasuredWidth() + getHorizontalMargins(this.mSegmentButton);
            maxChildHeight = Math.max(maxChildHeight, this.mSegmentButton.getMeasuredHeight() + getVerticalMargins(this.mSegmentButton));
            measuredState = View.combineMeasuredStates(measuredState, ViewCompat.getMeasuredState(this.mSegmentButton));
        }
        int minimumContentWidth = startWidthUsed == 0 ? this.mSectionGap + endActionsWidth : startWidthUsed + endActionsWidth + (this.mSectionGap * 2);
        int minimumTitleWidth;
        if (shouldLayout(this.mTitleTextView)) {
            this.mTitleTextView.getLayoutParams().width = -2;
            this.mTitleTextView.setTextSize(0, this.mTitleTextSize);
            TextPaint titlePaint = this.mTitleTextView.getPaint();
            CharSequence titleText = this.mTitleText;
            int measuredMinimumTitleWidth = Math.min((int) titlePaint.measureText(titleText, 0, titleText.length()), this.mTitleTextMinWidth) + ((this.mHasCustomViewBeforeTitle || shouldLayout(this.mNavButtonView)) ? this.mGapBetweenNavigationAndTitle : 0);
            int sectionGap = this.mSectionGap;
            minimumTitleWidth = measuredMinimumTitleWidth;
            minimumContentWidth = Math.max((startWidthUsed + measuredMinimumTitleWidth + sectionGap) * 2, (sectionGap + endActionsWidth) * 2);
        } else {
            minimumTitleWidth = 0;
        }
        addCustomCenterViews(this.mTempViews);
        int centerViewCount = this.mTempViews.size();
        int centerViewsWidth = segmentWidth;
        int centerWidthUsed = minimumContentWidth;
        for (int centerViewIndex = 0; centerViewIndex < centerViewCount; centerViewIndex++) {
            View centerView = this.mTempViews.get(centerViewIndex);
            LayoutParams centerLayoutParams = (LayoutParams) centerView.getLayoutParams();
            if (centerLayoutParams.mViewType == 0 && !centerLayoutParams.mTypeSegmentButton
                    && shouldLayout(centerView) && !isDummyView(centerView, centerLayoutParams)) {
                centerViewsWidth += measureChildCollapseMargins(centerView, widthMeasureSpec, centerWidthUsed, heightMeasureSpec, 0, collapsingMargins);
                centerWidthUsed += centerViewsWidth;
                maxChildHeight = Math.max(maxChildHeight, centerView.getMeasuredHeight() + getVerticalMargins(centerView));
                measuredState = View.combineMeasuredStates(measuredState, ViewCompat.getMeasuredState(centerView));
            }
        }
        int halfWidth = availableWidth / 2;
        int centerContentRight = (centerViewsWidth / 2) + halfWidth;
        int centeredStartWidth = (this.mSectionGap + centerContentRight) - getPaddingStart();
        int paddingEnd = (centerContentRight + this.mSectionGap) - getPaddingEnd();
        int contentInsetEnd = getContentInsetEnd();
        int widthUsed;
        int endActionsWidthForInsets;
        if (centerViewsWidth > 0) {
            if (!isRtl) {
                centeredStartWidth = paddingEnd;
            }
            widthUsed = startWidthUsed + centeredStartWidth;
            endActionsWidthForInsets = endActionsWidth;
        } else {
            endActionsWidthForInsets = endActionsWidth;
            widthUsed = startWidthUsed + Math.max(contentInsetEnd, endActionsWidthForInsets) + this.mSectionGap;
        }
        collapsingMargins[endMarginIndex] = Math.max(0, contentInsetEnd - endActionsWidthForInsets);
        if (shouldLayout(this.mExpandedActionView)) {
            widthUsed += measureChildCollapseMargins(this.mExpandedActionView, widthMeasureSpec, widthUsed, heightMeasureSpec, 0, collapsingMargins);
            int expandedActionHeight = Math.max(maxChildHeight, this.mExpandedActionView.getMeasuredHeight() + getVerticalMargins(this.mExpandedActionView));
            measuredState = View.combineMeasuredStates(measuredState, ViewCompat.getMeasuredState(this.mExpandedActionView));
            maxChildHeight = expandedActionHeight;
        }
        if (shouldLayout(this.mLogoView)) {
            widthUsed += measureChildCollapseMargins(this.mLogoView, widthMeasureSpec, widthUsed, heightMeasureSpec, 0, collapsingMargins);
            maxChildHeight = Math.max(maxChildHeight, this.mLogoView.getMeasuredHeight() + getVerticalMargins(this.mLogoView));
            measuredState = View.combineMeasuredStates(measuredState, ViewCompat.getMeasuredState(this.mLogoView));
        }
        addCustomViewsWithGravity(this.mTempViews, Gravity.END);
        int endViewCount = this.mTempViews.size();
        int endViewsWidthLimit = centerViewsWidth > 0 ? ((halfWidth - (((this.mSectionGap * 2) + centerViewsWidth) / 2)) - getPaddingEnd()) - endActionsWidthForInsets : (((availableWidth - widthUsed) - minimumTitleWidth) - getPaddingEnd()) - getPaddingStart();
        for (int endViewIndex = 0; endViewIndex < endViewCount; endViewIndex++) {
            View endView = this.mTempViews.get(endViewIndex);
            LayoutParams endLayoutParams = (LayoutParams) endView.getLayoutParams();
            if (endLayoutParams.mViewType == 0 && !endLayoutParams.mTypeSegmentButton
                    && !endLayoutParams.mTypeTextButton && shouldLayout(endView)
                    && !isDummyView(endView, endLayoutParams)) {
                int childWidth = measureChildCollapseMargins(endView, widthMeasureSpec, widthUsed + minimumTitleWidth, endViewsWidthLimit, heightMeasureSpec, 0, collapsingMargins);
                if (centerViewsWidth == 0) {
                    widthUsed += childWidth;
                }
                maxChildHeight = Math.max(maxChildHeight, endView.getMeasuredHeight() + getVerticalMargins(endView));
                measuredState = View.combineMeasuredStates(measuredState, ViewCompat.getMeasuredState(endView));
            }
        }
        if (shouldLayout(this.mDummyView)) {
            measureChildCollapseMargins(this.mDummyView, widthMeasureSpec, widthUsed, heightMeasureSpec, titleVerticalMargins, collapsingMargins);
            widthUsed += this.mDummyView.getMeasuredWidth() + getHorizontalMargins(this.mDummyView);
        }
        int titleBlockWidth;
        int titleBlockHeight;
        if (shouldLayout(this.mTitleTextView)) {
            this.mTitleTextView.getLayoutParams().width = -1;
            this.mTitleTextView.setTextSize(0, this.mTitleTextSize);
            measureChildCollapseMargins(this.mTitleTextView, widthMeasureSpec, widthUsed + titleHorizontalMargins + ((this.mHasCustomViewBeforeTitle || shouldLayout(this.mNavButtonView)) ? this.mGapBetweenNavigationAndTitle : 0), heightMeasureSpec, titleVerticalMargins, collapsingMargins);
            int titleMeasuredWidth = this.mTitleTextView.getMeasuredWidth() + getHorizontalMargins(this.mTitleTextView);
            int titleMeasuredHeight = this.mTitleTextView.getMeasuredHeight() + getVerticalMargins(this.mTitleTextView);
            titleBlockWidth = titleMeasuredWidth;
            measuredState = View.combineMeasuredStates(measuredState, ViewCompat.getMeasuredState(this.mTitleTextView));
            titleBlockHeight = titleMeasuredHeight;
        } else {
            titleBlockWidth = 0;
            titleBlockHeight = 0;
        }
        if (shouldLayout(this.mSubtitleTextView)) {
            this.mSubtitleTextView.getLayoutParams().width = -1;
            titleBlockWidth = Math.max(titleBlockWidth, measureChildCollapseMargins(this.mSubtitleTextView, widthMeasureSpec, widthUsed + titleHorizontalMargins + ((this.mHasCustomViewBeforeTitle || shouldLayout(this.mNavButtonView)) ? this.mGapBetweenNavigationAndTitle : 0), heightMeasureSpec, titleBlockHeight + titleVerticalMargins, collapsingMargins));
            titleBlockHeight += this.mSubtitleTextView.getMeasuredHeight() + getVerticalMargins(this.mSubtitleTextView);
            measuredState = View.combineMeasuredStates(measuredState, ViewCompat.getMeasuredState(this.mSubtitleTextView));
        }
        setMeasuredDimension(ViewCompat.resolveSizeAndState(Math.max(widthUsed + titleBlockWidth + getPaddingLeft() + getPaddingRight(), getSuggestedMinimumWidth()), widthMeasureSpec, (View.MEASURED_STATE_MASK) & measuredState), shouldCollapse() ? 0 : ViewCompat.resolveSizeAndState(Math.max(Math.max(maxChildHeight, titleBlockHeight) + getPaddingTop() + getPaddingBottom(), getSuggestedMinimumHeight()), heightMeasureSpec, measuredState << 16));
    }

    @Override
    public void onRtlPropertiesChanged(int layoutDirection) {
        super.onRtlPropertiesChanged(layoutDirection);
        COUIRtlSpacingHelper contentInsets = this.mContentInsets;
        if (contentInsets != null) {
            contentInsets.setDirection(layoutDirection == 1);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        int action = motionEvent.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            this.mEatingTouch = false;
        }
        if (!this.mEatingTouch) {
            boolean handled = super.onTouchEvent(motionEvent);
            if (action == MotionEvent.ACTION_DOWN && !handled) {
                this.mEatingTouch = true;
            }
        }
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            this.mEatingTouch = false;
        }
        return true;
    }

    public void refresh() {
        TypedArray actionBarAttributes = getContext().obtainStyledAttributes(null, androidx.appcompat.R.styleable.ActionBar, android.R.attr.actionBarStyle, 0);
        setOverflowIcon(getResources().getDrawable(R.drawable.coui_toolbar_menu_icon_more, getContext().getTheme()));
        Drawable drawable = actionBarAttributes.getDrawable(androidx.appcompat.R.styleable.ActionBar_homeAsUpIndicator);
        if (drawable != null) {
            setNavigationIcon(drawable);
        }
        COUIMaskRippleDrawable maskRippleDrawable = this.mMaskRippleDrawable;
        if (maskRippleDrawable != null) {
            maskRippleDrawable.refresh(getContext());
        }
        COUIActionMenuView actionMenuView = this.mMenuView;
        if (actionMenuView instanceof COUIActionMenuView) {
            COUIPopupListWindow overflowPopup = actionMenuView.mOverflowPopup;
            if (overflowPopup != null && overflowPopup.isShowing()) {
                actionMenuView.mOverflowPopup.dismiss();
            }
            actionMenuView.mOverflowPopup = null;
        }
        if (this.mTitleTextView != null && this.mTitleTextAppearance != 0) {
            setTitleTextAppearance(getContext(), this.mTitleTextAppearance);
        }
        TextView textView = this.mTitleTextView;
        int titleTextColor = this.mTitleTextColor;
        if (textView != null && titleTextColor != 0) {
            textView.setTextColor(titleTextColor);
        }
        TextView subtitleView = this.mSubtitleTextView;
        if (subtitleView != null && this.mSubtitleTextAppearance != 0) {
            subtitleView.setTextAppearance(getContext(), this.mSubtitleTextAppearance);
        }
        TextView coloredSubtitleView = this.mSubtitleTextView;
        int subtitleTextColor = this.mSubtitleTextColor;
        if (coloredSubtitleView != null && subtitleTextColor != 0) {
            coloredSubtitleView.setTextColor(subtitleTextColor);
        }
        if (this.mResId != 0) {
            getMenu().clear();
            inflateMenu(this.mResId);
        }
        actionBarAttributes.recycle();
    }

    @Override
    public void setCollapsible(boolean collapsible) {
        this.mCollapsible = collapsible;
        requestLayout();
    }

    @Override
    public void setContentInsetsAbsolute(int left, int right) {
        this.mContentInsets.setAbsolute(left, right);
    }

    @Override
    public void setContentInsetsRelative(int start, int end) {
        this.mContentInsets.setRelative(start, end);
    }

    @Deprecated
    public void setEnableAddExtraWidth(boolean enabled) {
    }

    @Deprecated
    public void setIsFixTitleFontSize(boolean fixed) {
        COUIActionMenuView actionMenuView = this.mMenuView;
        if (actionMenuView != null) {
            actionMenuView.setIsFixTitleFontSize(fixed);
        } else {
            Log.e(TAG, "setIsFixTitleFontSize when mMenuView is null");
        }
    }

    public void setIsInsideSideNavigationBar(boolean insideSideNavigationBar) {
        if (this.mIsInsideSideNavigationBar != insideSideNavigationBar) {
            this.mIsInsideSideNavigationBar = insideSideNavigationBar;
            requestLayout();
        }
    }

    public void setIsTitleCenterStyle(boolean centered) {
        ensureMenuView();
        this.mIsTitleCenterStyle = centered;
        LayoutParams layoutParams = (LayoutParams) this.mMenuView.getLayoutParams();
        if (this.mIsTitleCenterStyle) {
            ((ViewGroup.MarginLayoutParams) layoutParams).width = -1;
        } else {
            ((ViewGroup.MarginLayoutParams) layoutParams).width = -2;
        }
        TextView textView = this.mTitleTextView;
        if (textView != null) {
            textView.setTextAlignment(this.mIsTitleCenterStyle ? View.TEXT_ALIGNMENT_CENTER : View.TEXT_ALIGNMENT_VIEW_END);
        }
        this.mMenuView.setLayoutParams(layoutParams);
        requestLayout();
    }

    @Override
    public void setLogo(int drawableResId) {
        setLogo(androidx.appcompat.content.res.AppCompatResources.getDrawable(getContext(), drawableResId));
    }

    @Override
    public void setLogoDescription(int descriptionResId) {
        setLogoDescription(getContext().getText(descriptionResId));
    }

    @Override
    public void setMenuCallbacks(MenuPresenter.Callback presenterCallback, MenuBuilder.Callback menuCallback) {
        this.mActionMenuPresenterCallback = presenterCallback;
        this.mMenuBuilderCallback = menuCallback;
        if (this.mMenuView != null) {
            this.mMenuView.setMenuCallbacks(presenterCallback, menuCallback);
        }
    }

    public void setMenuViewColor(int color) {
        Drawable overflowIcon;
        COUIActionMenuView actionMenuView = this.mMenuView;
        if (actionMenuView == null || (overflowIcon = actionMenuView.getOverflowIcon()) == null) {
            return;
        }
        DrawableCompat.setTint(overflowIcon, color);
        this.mMenuView.setOverflowIcon(overflowIcon);
    }

    public void setMinTitleTextSize(float minTextSize) {
        float maxTextSize = this.mTextMaxSize;
        if (minTextSize > maxTextSize) {
            minTextSize = maxTextSize;
        }
        this.mTextMinSize = minTextSize;
    }

    @Override
    public void setMinimumHeight(int minHeight) {
        this.mMinHeight = minHeight;
        super.setMinimumHeight(minHeight);
    }

    @Override
    public void setNavigationContentDescription(int descriptionResId) {
        setNavigationContentDescription(descriptionResId != 0 ? getContext().getText(descriptionResId) : null);
    }

    @Override
    public void setNavigationIcon(int drawableResId) {
        setNavigationIcon(androidx.appcompat.content.res.AppCompatResources.getDrawable(getContext(), drawableResId));
    }

    @Override
    public void setNavigationOnClickListener(View.OnClickListener onClickListener) {
        ensureNavButtonView();
        this.mNavButtonView.setOnClickListener(onClickListener);
    }

    @Override
    public void setOnMenuItemClickListener(Toolbar.OnMenuItemClickListener listener) {
        this.mOnMenuItemClickListener = listener;
    }

    @Override
    public void setOverflowIcon(Drawable drawable) {
        ensureMenu();
        this.mMenuView.setOverflowIcon(drawable);
    }

    @Override
    public void setPopupMenuRuleEnabled(boolean enabled) {
        this.mPopupRuleEnable = enabled;
    }

    @Override
    public void setPopupTheme(int themeResId) {
        if (this.mPopupTheme != themeResId) {
            this.mPopupTheme = themeResId;
            if (themeResId == 0) {
                this.mPopupContext = getContext();
            } else {
                this.mPopupContext = new ContextThemeWrapper(getContext(), themeResId);
            }
        }
    }

    public void setPopupWindowOnDismissListener(PopupWindow.OnDismissListener onDismissListener) {
        COUIActionMenuView actionMenuView = this.mMenuView;
        if (actionMenuView instanceof COUIActionMenuView) {
            actionMenuView.setPopupWindowOnDismissListener(onDismissListener);
        }
    }

    public void setRedDot(int itemId, int amount) {
        COUIActionMenuView actionMenuView = this.mMenuView;
        if (actionMenuView == null) {
            Log.e(TAG, "The COUIActionMenuView is null");
        } else {
            actionMenuView.setRedDot(itemId, amount);
        }
    }

    public void setSearchView(View view) {
        setSearchView(view, view != null ? view.getLayoutParams() == null ? new LayoutParams(new LayoutParams(-1, this.mToolbarHeight)) : new LayoutParams(view.getLayoutParams()) : null);
    }

    public void setSectionGapMediumLarge(int gap) {
        if (this.mSectionGapMediumLarge != gap) {
            this.mSectionGapMediumLarge = gap;
            requestLayout();
        }
    }

    public void setSegmentButtons(View view) {
        View currentSegmentButton = this.mSegmentButton;
        if (currentSegmentButton != null) {
            removeView(currentSegmentButton);
        }
        if (view != null) {
            setSegmentButtons(view, view.getLayoutParams() == null ? new LayoutParams(new LayoutParams(-2, this.mSegmentButtonHeight)) : new LayoutParams(view.getLayoutParams()));
        } else {
            this.mSegmentButton = null;
        }
    }

    @Deprecated
    public void setSubMenuList(ArrayList<PopupListItem> items, int position, COUISubMenuClickListener listener) {
    }

    @Override
    public void setSubtitle(int subtitleResId) {
        setSubtitle(getContext().getText(subtitleResId));
    }

    @Override
    public void setSubtitleTextAppearance(Context context, int textAppearanceResId) {
        this.mSubtitleTextAppearance = textAppearanceResId;
        TextView textView = this.mSubtitleTextView;
        if (textView != null) {
            textView.setTextAppearance(context, textAppearanceResId);
        }
    }

    @Override
    public void setSubtitleTextColor(int color) {
        this.mSubtitleTextColor = color;
        TextView textView = this.mSubtitleTextView;
        if (textView != null) {
            textView.setTextColor(color);
        }
    }

    public void setTextButton(View view) {
        View currentTextButton = this.mTextButton;
        if (currentTextButton != null) {
            removeView(currentTextButton);
        }
        if (view != null) {
            setTextButton(view, view.getLayoutParams() == null ? new LayoutParams(new LayoutParams(-2, -2)) : new LayoutParams(view.getLayoutParams()));
        } else {
            this.mTextButton = null;
        }
    }

    @Override
    public void setTitle(int titleResId) {
        setTitle(getContext().getText(titleResId));
    }

    @Override
    public void setTitleMarginStart(int marginStart) {
        this.mTitleMarginStart = marginStart;
        requestLayout();
    }

    @Override
    public void setTitleTextAppearance(Context context, int textAppearanceResId) {
        this.mTitleTextAppearance = textAppearanceResId;
        TextView textView = this.mTitleTextView;
        if (textView != null) {
            textView.setTextAppearance(context, textAppearanceResId);
            if (this.mTitleType == TITLE_TYPE_SECONDARY) {
                this.mTitleTextView.setTextSize(0, COUIChangeTextUtil.getSuitableFontSize(this.mTitleTextView.getTextSize(), getContext().getResources().getConfiguration().fontScale, 2));
            }
            TypedArray minHeightAttributes = context.obtainStyledAttributes(this.mTitleTextAppearance, new int[]{android.R.attr.minHeight});
            if (minHeightAttributes != null) {
                this.mTitleTextView.setMinHeight(minHeightAttributes.getDimensionPixelSize(0, 0));
                minHeightAttributes.recycle();
            }
            TypedArray lineSpacingAttributes = context.obtainStyledAttributes(this.mTitleTextAppearance, new int[]{R.attr.lineSpacingMultiplier});
            if (lineSpacingAttributes != null) {
                float lineSpacingMultiplier = lineSpacingAttributes.getFloat(0, 1.4f);
                TextView titleView = this.mTitleTextView;
                titleView.setLineSpacing(titleView.getLineSpacingExtra(), lineSpacingMultiplier);
                lineSpacingAttributes.recycle();
            }
            TypedArray textAlignmentAttributes = context.obtainStyledAttributes(this.mTitleTextAppearance, new int[]{android.R.attr.textAlignment});
            if (textAlignmentAttributes != null) {
                int textAlignment = textAlignmentAttributes.getInteger(0, View.TEXT_ALIGNMENT_VIEW_END);
                if (textAlignment >= 0) {
                    this.mTitleTextView.setTextAlignment(textAlignment);
                }
                textAlignmentAttributes.recycle();
            }
            TypedArray maxLinesAttributes = context.obtainStyledAttributes(this.mTitleTextAppearance, new int[]{android.R.attr.maxLines});
            if (maxLinesAttributes != null) {
                int maxLines = maxLinesAttributes.getInteger(0, 1);
                if (maxLines >= 1) {
                    this.mTitleTextView.setSingleLine(false);
                    this.mTitleTextView.setMaxLines(maxLines);
                }
                maxLinesAttributes.recycle();
            }
            this.mTextMaxSize = this.mTitleTextView.getTextSize();
            this.mTitleTextSize = this.mTitleTextView.getTextSize();
        }
    }

    @Override
    public void setTitleTextColor(int color) {
        this.mTitleTextColor = color;
        TextView textView = this.mTitleTextView;
        if (textView != null) {
            textView.setTextColor(color);
        }
    }

    public void setTitleTextSize(float textSize) {
        TextView textView = this.mTitleTextView;
        if (textView != null) {
            textView.setTextSize(textSize);
            this.mTitleTextSize = TypedValue.applyDimension(1, textSize, getResources().getDisplayMetrics());
        }
    }

    public void setTitleTextViewTypeface(Typeface typeface) {
        ensureTitleTextView();
        this.mTitleTextView.setTypeface(typeface);
    }

    public void setUseResponsivePadding(boolean enabled) {
        this.mUseResponsivePadding = enabled;
        requestLayout();
    }

    @Override
    public boolean showOverflowMenu() {
        COUIActionMenuView actionMenuView = this.mMenuView;
        return (!(actionMenuView instanceof COUIActionMenuView) || actionMenuView.getWindowToken() == null) ? super.showOverflowMenu() : this.mMenuView.showOverflowMenu();
    }

    public void tintNavigationIconDrawable(int color) {
        Drawable drawable;
        ImageButton imageButton = this.mNavButtonView;
        if (imageButton == null || (drawable = imageButton.getDrawable()) == null) {
            return;
        }
        DrawableCompat.setTint(drawable, color);
    }

    public COUIToolbar(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, android.R.attr.toolbarStyle);
    }

    private int measureChildCollapseMargins(View view, int parentWidthSpec, int widthUsed, int parentHeightSpec, int heightUsed, int verticalMargins, int[] collapsingMargins) {
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        int leftMargin = marginLayoutParams.leftMargin - collapsingMargins[0];
        int rightMargin = marginLayoutParams.rightMargin - collapsingMargins[1];
        int horizontalMargins = Math.max(0, leftMargin) + Math.max(0, rightMargin);
        collapsingMargins[0] = Math.max(0, -leftMargin);
        collapsingMargins[1] = Math.max(0, -rightMargin);
        boolean isSearchView = false;
        boolean isCenteredTitle = false;
        if (marginLayoutParams instanceof LayoutParams) {
            LayoutParams layoutParams = (LayoutParams) marginLayoutParams;
            isSearchView = layoutParams.mTypeSearch && this.mHasSearchViewFlag;
            isCenteredTitle = layoutParams.mTypeTitle && this.mIsTitleCenterStyle;
        }
        int childWidthSpec = (isSearchView || isCenteredTitle)
                ? ViewGroup.getChildMeasureSpec(parentWidthSpec, horizontalMargins, marginLayoutParams.width)
                : ViewGroup.getChildMeasureSpec(parentWidthSpec, getPaddingLeft() + getPaddingRight() + horizontalMargins + widthUsed, marginLayoutParams.width);
        int childHeightSpec = ViewGroup.getChildMeasureSpec(heightUsed, getPaddingTop() + getPaddingBottom() + marginLayoutParams.topMargin + marginLayoutParams.bottomMargin + verticalMargins, marginLayoutParams.height);
        view.measure(childWidthSpec, childHeightSpec);
        if (!isSearchView) {
            if (view.getMeasuredWidth() > parentHeightSpec) {
                view.measure(View.MeasureSpec.makeMeasureSpec(Math.max(parentHeightSpec, 0), View.MeasureSpec.EXACTLY), childHeightSpec);
            }
            return view.getMeasuredWidth() + horizontalMargins;
        }
        COUIActionMenuView actionMenuView = this.mMenuView;
        if (actionMenuView != null && actionMenuView.getVisibility() != 8) {
            view.measure(ViewGroup.getChildMeasureSpec(parentWidthSpec, horizontalMargins, ((view.getMeasuredWidth() - this.mMenuView.getMeasuredWidth()) - (this.mMenuView.getMeasuredWidth() != 0 ? getPaddingEnd() : 0)) - this.mGapBetweenSearchViewAndMenu), childHeightSpec);
        }
        return horizontalMargins;
    }

    @Override
    public void setLogo(Drawable drawable) {
        if (drawable != null) {
            ensureLogoView();
            if (this.mLogoView.getParent() == null) {
                addSystemView(this.mLogoView);
                updateChildVisibilityForExpandedActionView(this.mLogoView);
            }
        } else {
            ImageView imageView = this.mLogoView;
            if (imageView != null && imageView.getParent() != null) {
                removeView(this.mLogoView);
            }
        }
        ImageView logoView = this.mLogoView;
        if (logoView != null) {
            logoView.setImageDrawable(drawable);
        }
    }

    @Override
    public void setLogoDescription(CharSequence description) {
        if (!TextUtils.isEmpty(description)) {
            ensureLogoView();
        }
        ImageView imageView = this.mLogoView;
        if (imageView != null) {
            imageView.setContentDescription(description);
        }
    }

    @Override
    public void setNavigationContentDescription(CharSequence description) {
        if (!TextUtils.isEmpty(description)) {
            ensureNavButtonView();
        }
        ImageButton imageButton = this.mNavButtonView;
        if (imageButton != null) {
            imageButton.setContentDescription(description);
        }
    }

    @Override
    public void setNavigationIcon(Drawable drawable) {
        if (drawable != null) {
            ensureNavButtonView();
            if (this.mNavButtonView.getParent() == null) {
                addSystemView(this.mNavButtonView);
                updateChildVisibilityForExpandedActionView(this.mNavButtonView);
            }
        } else {
            ImageButton imageButton = this.mNavButtonView;
            if (imageButton != null && imageButton.getParent() != null) {
                removeView(this.mNavButtonView);
            }
        }
        ImageButton navigationButton = this.mNavButtonView;
        if (navigationButton != null) {
            navigationButton.setImageDrawable(drawable);
        }
    }

    @Override
    public void setSubtitle(CharSequence subtitle) {
        if (TextUtils.isEmpty(subtitle)) {
            TextView textView = this.mSubtitleTextView;
            if (textView != null && textView.getParent() != null) {
                removeView(this.mSubtitleTextView);
            }
        } else {
            if (this.mSubtitleTextView == null) {
                Context context = getContext();
                this.mSubtitleTextView = new TextView(context);
                LayoutParams toolbarLayoutParams = generateDefaultLayoutParams();
                toolbarLayoutParams.mTypeTitle = true;
                this.mSubtitleTextView.setLayoutParams(toolbarLayoutParams);
                this.mSubtitleTextView.setSingleLine();
                this.mSubtitleTextView.setEllipsize(TextUtils.TruncateAt.END);
                int subtitleResId = this.mSubtitleTextAppearance;
                if (subtitleResId != 0) {
                    this.mSubtitleTextView.setTextAppearance(context, subtitleResId);
                }
                int subtitleTextColor = this.mSubtitleTextColor;
                if (subtitleTextColor != 0) {
                    this.mSubtitleTextView.setTextColor(subtitleTextColor);
                }
            }
            if (this.mSubtitleTextView.getParent() == null) {
                addSystemView(this.mSubtitleTextView);
                updateChildVisibilityForExpandedActionView(this.mSubtitleTextView);
            }
        }
        TextView subtitleView = this.mSubtitleTextView;
        if (subtitleView != null) {
            subtitleView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
            this.mSubtitleTextView.setText(subtitle);
        }
        this.mSubtitleText = subtitle;
    }

    @Override
    public void setTitle(CharSequence title) {
        if (TextUtils.isEmpty(title)) {
            TextView textView = this.mTitleTextView;
            if (textView != null && textView.getParent() != null) {
                removeView(this.mTitleTextView);
            }
        } else {
            ensureTitleTextView();
            if (this.mTitleTextView.getParent() == null) {
                addSystemView(this.mTitleTextView);
                updateChildVisibilityForExpandedActionView(this.mTitleTextView);
            }
        }
        TextView titleView = this.mTitleTextView;
        if (titleView != null) {
            titleView.setText(title);
            this.mTitleTextSize = this.mTitleTextView.getTextSize();
        }
        this.mTitleText = title;
    }

    public COUIToolbar(Context context, AttributeSet attributeSet, int defStyleAttr) {
        this(context, attributeSet, defStyleAttr, R.style.Widget_COUI_Toolbar);
    }

    @Override
    public LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(-2, -2);
    }

    public COUIToolbar(Context context, AttributeSet attributeSet, int defStyleAttr,
            int defStyleRes) {
        super(context, attributeSet, defStyleAttr);
        COUIRtlSpacingHelper contentInsets = new COUIRtlSpacingHelper();
        this.mContentInsets = contentInsets;
        this.mTempViews = new ArrayList<>();
        this.mTempMargins = new int[2];
        this.mMenuViewItemClickListener = new ActionMenuView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (COUIToolbar.this.mOnMenuItemClickListener != null) {
                    return COUIToolbar.this.mOnMenuItemClickListener.onMenuItemClick(menuItem);
                }
                return false;
            }
        };
        this.mSearchCollapsingMargins = new int[2];
        this.mShowOverflowMenuRunnable = new Runnable() {
            @Override
            public void run() {
                COUIToolbar.this.showOverflowMenu();
            }
        };
        this.mDisplayFrame = null;
        this.mWindowFrame = null;
        this.mHasCustomViewBeforeTitle = false;
        this.mGravity = DEFAULT_GRAVITY;
        this.mIsTitleCenterStyle = false;
        this.mTitlePosition = new int[2];
        this.mTitleTextSize = 0.0f;
        this.mHasSearchViewFlag = false;
        this.mIsInsideSideNavigationBar = false;
        this.mPopupRuleEnable = true;
        this.mUseResponsivePadding = true;
        this.mDummyView = null;
        setClipToPadding(false);
        setClipChildren(false);
        if (attributeSet != null) {
            int styleAttribute = attributeSet.getStyleAttribute();
            this.mStyle = styleAttribute;
            if (styleAttribute == 0) {
                this.mStyle = defStyleAttr;
            }
        } else {
            this.mStyle = 0;
        }
        TintTypedArray attributes = TintTypedArray.obtainStyledAttributes(getContext(), attributeSet, R.styleable.COUIToolbar, R.attr.couiToolbarStyle, defStyleRes);
        int titleTypeIndex = R.styleable.COUIToolbar_titleType;
        if (attributes.hasValue(titleTypeIndex)) {
            this.mTitleType = attributes.getInt(titleTypeIndex, TITLE_TYPE_HEAD);
        }
        this.mTitleTextAppearance = attributes.getResourceId(R.styleable.COUIToolbar_supportTitleTextAppearance, 0);
        this.mSubtitleTextAppearance = attributes.getResourceId(R.styleable.COUIToolbar_supportSubtitleTextAppearance, 0);
        this.mGravity = attributes.getInteger(R.styleable.COUIToolbar_android_gravity, this.mGravity);
        this.mButtonGravity = attributes.getInteger(R.styleable.COUIToolbar_supportButtonGravity, Gravity.TOP);
        this.mTitleMarginStart = attributes.getDimensionPixelOffset(R.styleable.COUIToolbar_supportTitleMargins, 0);
        this.mIsTiny = attributes.getBoolean(R.styleable.COUIToolbar_supportIsTiny, false);
        this.mIsInsidePanel = attributes.getBoolean(R.styleable.COUIToolbar_supportPanelStyle, false);
        int defaultTitleMargin = this.mTitleMarginStart;
        this.mTitleMarginEnd = defaultTitleMargin;
        this.mTitleMarginTop = defaultTitleMargin;
        this.mTitleMarginBottom = defaultTitleMargin;
        int titleMarginStart = attributes.getDimensionPixelOffset(R.styleable.COUIToolbar_supportTitleMarginStart, getContext().getResources().getDimensionPixelSize(R.dimen.coui_toolbar_support_margin_start));
        if (titleMarginStart >= 0) {
            this.mTitleMarginStart = titleMarginStart;
        }
        int titleMarginEnd = attributes.getDimensionPixelOffset(R.styleable.COUIToolbar_supportTitleMarginEnd, -1);
        if (titleMarginEnd >= 0) {
            this.mTitleMarginEnd = titleMarginEnd;
        }
        int titleMarginTop = attributes.getDimensionPixelOffset(R.styleable.COUIToolbar_supportTitleMarginTop, -1);
        if (titleMarginTop >= 0) {
            this.mTitleMarginTop = titleMarginTop;
        }
        int titleMarginBottom = attributes.getDimensionPixelOffset(R.styleable.COUIToolbar_supportTitleMarginBottom, -1);
        if (titleMarginBottom >= 0) {
            this.mTitleMarginBottom = titleMarginBottom;
        }
        this.mTitlePaddingTop = attributes.getDimensionPixelSize(R.styleable.COUIToolbar_supportTitlePaddingTop, 0);
        this.mTitlePaddingBottom = attributes.getDimensionPixelSize(R.styleable.COUIToolbar_supportTitlePaddingBottom, 0);
        this.mMaxButtonHeight = attributes.getDimensionPixelSize(R.styleable.COUIToolbar_supportMaxButtonHeight, -1);
        int contentInsetStart = attributes.getDimensionPixelOffset(R.styleable.COUIToolbar_supportContentInsetStart, Integer.MIN_VALUE);
        int contentInsetEnd = attributes.getDimensionPixelOffset(R.styleable.COUIToolbar_supportContentInsetEnd, Integer.MIN_VALUE);
        contentInsets.setAbsolute(attributes.getDimensionPixelSize(R.styleable.COUIToolbar_supportContentInsetLeft, 0), attributes.getDimensionPixelSize(R.styleable.COUIToolbar_supportContentInsetRight, 0));
        if (contentInsetStart != Integer.MIN_VALUE || contentInsetEnd != Integer.MIN_VALUE) {
            contentInsets.setRelative(contentInsetStart, contentInsetEnd);
        }
        this.mCollapseIcon = attributes.getDrawable(R.styleable.COUIToolbar_supportCollapseIcon);
        this.mCollapseDescription = attributes.getText(R.styleable.COUIToolbar_supportCollapseContentDescription);
        CharSequence title = attributes.getText(R.styleable.COUIToolbar_supportTitle);
        if (!TextUtils.isEmpty(title)) {
            setTitle(title);
        }
        CharSequence subtitle = attributes.getText(R.styleable.COUIToolbar_supportSubtitle);
        if (!TextUtils.isEmpty(subtitle)) {
            setSubtitle(subtitle);
        }
        this.mPopupContext = getContext();
        setPopupTheme(attributes.getResourceId(R.styleable.COUIToolbar_supportPopupTheme, 0));
        Drawable navigationIcon = attributes.getDrawable(R.styleable.COUIToolbar_supportNavigationIcon);
        if (navigationIcon != null) {
            setNavigationIcon(navigationIcon);
        }
        CharSequence navigationDescription = attributes.getText(R.styleable.COUIToolbar_supportNavigationContentDescription);
        if (!TextUtils.isEmpty(navigationDescription)) {
            setNavigationContentDescription(navigationDescription);
        }
        this.mMinHeight = attributes.getDimensionPixelSize(androidx.appcompat.R.styleable.Toolbar_android_minHeight, 0);
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        if (attributes.hasValue(R.styleable.COUIToolbar_minTitleTextSize)) {
            this.mTextMinSize = attributes.getDimensionPixelSize(R.styleable.COUIToolbar_minTitleTextSize, (int) (displayMetrics.scaledDensity * 16.0f));
        } else {
            this.mTextMinSize = displayMetrics.scaledDensity * 16.0f;
        }
        TypedArray titleTextSizeAttributes = context.obtainStyledAttributes(this.mTitleTextAppearance, new int[]{android.R.attr.textSize});
        if (titleTextSizeAttributes != null) {
            this.mTextMaxSize = titleTextSizeAttributes.getDimensionPixelSize(0, (int) (displayMetrics.scaledDensity * 24.0f));
            titleTextSizeAttributes.recycle();
        }
        if (this.mTitleType == TITLE_TYPE_SECONDARY) {
            this.mTextMaxSize = COUIChangeTextUtil.getSuitableFontSize(this.mTextMaxSize, getResources().getConfiguration().fontScale, 2);
            this.mTextMinSize = COUIChangeTextUtil.getSuitableFontSize(this.mTextMinSize, getResources().getConfiguration().fontScale, 2);
        }
        this.mSegmentButtonMaxWidth = getContext().getResources().getDimensionPixelSize(R.dimen.coui_toolbar_segment_button_max_width);
        this.mSegmentButtonMinWidth = getContext().getResources().getDimensionPixelSize(R.dimen.coui_toolbar_segment_button_min_width);
        this.mSectionGapMediumLarge = getContext().getResources().getDimensionPixelSize(R.dimen.coui_toolbar_section_gap);
        this.mSectionGapSmall = getContext().getResources().getDimensionPixelSize(R.dimen.coui_toolbar_section_gap_small);
        this.mTitleTextMinWidth = getContext().getResources().getDimensionPixelSize(R.dimen.coui_toolbar_title_text_min_width);
        this.mToolbarHeight = getContext().getResources().getDimensionPixelSize(R.dimen.toolbar_min_height);
        this.mSegmentButtonHeight = getContext().getResources().getDimensionPixelSize(R.dimen.segment_button_height);
        this.mToolbarNormalPaddingLeft = getContext().getResources().getDimensionPixelOffset(R.dimen.toolbar_normal_menu_padding_left);
        this.mGapBeforeMenuView = getContext().getResources().getDimensionPixelSize(R.dimen.coui_toolbar_gap_before_menu);
        if (this.mIsTiny) {
            this.mToolbarNormalPaddingRight = getContext().getResources().getDimensionPixelOffset(R.dimen.toolbar_normal_menu_padding_tiny_right);
            changeBackViewParams();
        } else {
            this.mToolbarNormalPaddingRight = getContext().getResources().getDimensionPixelOffset(R.dimen.toolbar_normal_menu_padding_right);
        }
        this.mToolbarCenterTitlePaddingLeft = getContext().getResources().getDimensionPixelOffset(R.dimen.toolbar_center_title_padding_left);
        this.mToolbarCenterTitlePaddingRight = getContext().getResources().getDimensionPixelOffset(R.dimen.toolbar_center_title_padding_right);
        this.mToolbarOverFlowPadding = getContext().getResources().getDimensionPixelOffset(R.dimen.toolbar_overflow_menu_padding);
        this.mTitleMinWidth = getContext().getResources().getDimensionPixelOffset(R.dimen.coui_toolbar_title_min_width);
        this.mGapBetweenSearchViewAndMenu = getContext().getResources().getDimensionPixelOffset(R.dimen.coui_toolbar_gap_between_search_and_menu);
        this.mGapBetweenNavigationAndTitle = getContext().getResources().getDimensionPixelOffset(R.dimen.coui_toolbar_gap_between_navigation_and_title);
        int titleCenterIndex = R.styleable.COUIToolbar_titleCenter;
        if (attributes.hasValue(titleCenterIndex)) {
            this.mIsTitleCenterStyle = attributes.getBoolean(titleCenterIndex, false);
        }
        TextView textView = this.mSubtitleTextView;
        int subtitleTextAppearance = this.mSubtitleTextAppearance;
        if (textView != null && subtitleTextAppearance != 0) {
            textView.setTextAppearance(context, subtitleTextAppearance);
        }
        setWillNotDraw(false);
        attributes.recycle();
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attributeSet) {
        return new LayoutParams(getContext(), attributeSet);
    }

    @Override
    public LayoutParams generateLayoutParams(ViewGroup.LayoutParams layoutParams) {
        if (layoutParams instanceof LayoutParams) {
            return new LayoutParams((LayoutParams) layoutParams);
        }
        if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
            return new LayoutParams((ViewGroup.MarginLayoutParams) layoutParams);
        }
        if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
            return new LayoutParams((ViewGroup.MarginLayoutParams) layoutParams);
        }
        return new LayoutParams(layoutParams);
    }

    public void setSearchView(View view, LayoutParams layoutParams) {
        if (view == null) {
            this.mHasSearchViewFlag = false;
            return;
        }
        this.mHasSearchViewFlag = true;
        LayoutParams searchLayoutParams = new LayoutParams(layoutParams);
        searchLayoutParams.mTypeSearch = true;
        searchLayoutParams.mViewType = LayoutParams.CUSTOM;
        addView(view, 0, searchLayoutParams);
    }

    private void setSegmentButtons(View view, LayoutParams layoutParams) {
        this.mSegmentButton = view;
        LayoutParams segmentLayoutParams = new LayoutParams(layoutParams);
        segmentLayoutParams.mViewType = LayoutParams.CUSTOM;
        segmentLayoutParams.mTypeSegmentButton = true;
        segmentLayoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        addView(view, 0, segmentLayoutParams);
    }

    private void setTextButton(View view, LayoutParams layoutParams) {
        this.mTextButton = view;
        LayoutParams textButtonLayoutParams = new LayoutParams(layoutParams);
        textButtonLayoutParams.mViewType = LayoutParams.CUSTOM;
        textButtonLayoutParams.mTypeTextButton = true;
        textButtonLayoutParams.gravity = Gravity.END;
        addView(view, textButtonLayoutParams);
    }
}
