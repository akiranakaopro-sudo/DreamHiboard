package com.coui.appcompat.toolbar;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.TooltipCompat;
import androidx.core.view.ViewCompat;
import com.coui.appcompat.R;
import com.coui.appcompat.accessibilityutil.COUIAccessibilityUtil;
import com.coui.appcompat.contextutil.COUIContextUtil;
import com.coui.appcompat.darkmode.COUIDarkModeUtil;
import com.coui.appcompat.log.COUILog;
import com.coui.appcompat.poplist.COUIPopupListWindow;
import com.coui.appcompat.poplist.COUISubMenuClickListener;
import com.coui.appcompat.poplist.PopupListItem;
import com.coui.appcompat.reddot.COUIHintRedDotHelper;
import com.coui.appcompat.state.COUIMaskRippleDrawable;
import com.coui.appcompat.textutil.COUIChangeTextUtil;
import com.coui.appcompat.uiutil.AnimLevel;
import com.coui.appcompat.uiutil.UIUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class COUIActionMenuView extends ActionMenuView {
    private static final String EMPTY_TITLE = "";
    private static final int HUNDRED = 100;
    private static final int MAX_TEXT_MENU_ITEM_LINE = 2;
    private static final String OVER_FLOW_MENU_CLASS = "androidx.appcompat.widget.ActionMenuPresenter$OverflowMenuButton";
    private static final String TAG = "COUIActionMenuView";
    private static final int TEN = 10;
    private AnimLevel mBlurMinAnimLevel;
    private int mEdgeIconItemMargin;
    private int mEdgeTextItemMargin;
    private boolean mEnableAddExtraWidth;
    private COUIHintRedDotHelper mHintRedDotHelper;
    private int mIconItemHorOffset;
    private boolean mIsFixTitleFontSize;
    private boolean mIsSameSide;
    private int mItemSpacing;
    private HashMap<Integer, Integer> mItemSpecialColorMap;
    private int mItemVerOffset;
    private COUIMaskRippleDrawable mMaskRippleDrawable;
    private MenuBuilder mMenu;
    private int mMenuIconBgRadius;
    private int mMenuIconTopPadding;
    private MenuItemImpl mMenuItem;
    private int mMenuViewPadding;
    private int mNonActionRedDotCount;
    private int mNonActionRedDotSum;
    private PopupWindow.OnDismissListener mOnDismissListener;
    private AdapterView.OnItemClickListener mOnSubMenuItemClickListener;
    private final OpenOverflowRunnable mOpenOverflowRunnable;
    private String mOverFlowButtonDescription;
    private int mOverFlowHorPadding;
    private View mOverFlowMenuButton;
    private int mOverFlowMinWidth;
    private ArrayList<PopupListItem> mOverflowItems;
    private OverflowMenuListener mOverflowMenuListener;
    public COUIPopupListWindow mOverflowPopup;
    private List<Class<?>> mPresenterClasses;
    private String mRedDotDescription;
    private int mRedDotHorizontalOffset;
    private HashMap<Integer, Integer> mRedDotMap;
    private int mRedDotVerticalOffset;
    private int mRedDotWithBigNumberHorizontalOffset;
    private int mRedDotWithNumberDescriptionId;
    private int mRedDotWithNumberHorizontalOffset;
    private int mRedDotWithNumberVerticalOffset;
    private int mRedDotWithSmallNumberHorizontalOffset;
    private HashMap<Integer, Integer> mSubItemSpecialColorMap;
    private ArrayList<PopupListItem> mSubMenuList;
    private int mSubPosition;
    private int mTextExtarPadding;
    private int mTextMenuItemHorizontalPadding;
    private int mTextMenuItemMaxWidth;
    private int mToolbarTitleMinWidth;
    private boolean mUseBackgroundBlur;

    public class OpenOverflowRunnable implements Runnable {
        private OpenOverflowRunnable() {
        }

        @Override
        public void run() {
            COUIPopupListWindow cOUIPopupListWindow;
            if (COUIActionMenuView.this.mMenu != null) {
                COUIActionMenuView.this.mMenu.changeMenuMode();
            }
            if (COUIActionMenuView.this.getWindowToken() == null || (cOUIPopupListWindow = COUIActionMenuView.this.mOverflowPopup) == null || cOUIPopupListWindow.isShowing()) {
                return;
            }
            COUIActionMenuView cOUIActionMenuView = COUIActionMenuView.this;
            cOUIActionMenuView.mOverflowPopup.show(cOUIActionMenuView.mOverFlowMenuButton, COUIActionMenuView.this.mOverFlowMenuButton.getWidth() / 2, COUIActionMenuView.this.mOverFlowMenuButton.getHeight());
        }
    }

    public interface OverflowMenuListener {
        void onOverflowMenuPreShow(COUIPopupListWindow cOUIPopupListWindow);
    }

    public COUIActionMenuView(Context context) {
        this(context, null);
    }

    private void configOverflowIconBackground() {
        COUIMaskRippleDrawable cOUIMaskRippleDrawable = new COUIMaskRippleDrawable(getContext());
        this.mMaskRippleDrawable = cOUIMaskRippleDrawable;
        cOUIMaskRippleDrawable.setCircleRippleMask(COUIMaskRippleDrawable.getMaskRippleRadiusByType(getContext(), 0));
        this.mOverFlowMenuButton.setBackground(this.mMaskRippleDrawable);
        COUIDarkModeUtil.setForceDarkAllow(this.mOverFlowMenuButton, false);
    }

    private void drawRedDot(View view, int amount, Canvas canvas) {
        int index_2;
        int index_3;
        float iconLeft;
        float value;
        float top;
        float value_2;
        float right;
        float left;
        int index = amount != -1 ? amount != 0 ? 2 : 1 : 0;
        if (view != null) {
            int viewWidth = this.mHintRedDotHelper.getViewWidth(index, amount);
            int viewHeight = this.mHintRedDotHelper.getViewHeight(index);
            if (index == 1) {
                index_2 = this.mRedDotHorizontalOffset;
                index_3 = this.mRedDotVerticalOffset;
            } else if (amount < 10) {
                index_2 = this.mRedDotWithSmallNumberHorizontalOffset;
                index_3 = this.mRedDotWithNumberVerticalOffset;
            } else if (amount < 100) {
                index_2 = this.mRedDotWithNumberHorizontalOffset;
                index_3 = this.mRedDotWithNumberVerticalOffset;
            } else {
                index_2 = this.mRedDotWithBigNumberHorizontalOffset;
                index_3 = this.mRedDotWithNumberVerticalOffset;
            }
            RectF rectF = new RectF();
            if ((view instanceof ActionMenuItemView) && ((ActionMenuItemView) view).getItemData().getIcon() == null) {
                if (isLayoutRTL()) {
                    right = (view.getX() + index_2) - this.mMenuViewPadding;
                    left = right - viewWidth;
                } else {
                    left = ((view.getX() + view.getWidth()) - index_2) + this.mMenuViewPadding;
                    right = viewWidth + left;
                }
                top = (this.mMenuIconTopPadding - index_3) + this.mItemVerOffset;
                value_2 = viewHeight + top;
            } else {
                if (isLayoutRTL()) {
                    iconLeft = (view.getX() + ((view.getWidth() - this.mMenuIconBgRadius) / 2)) - index_2;
                    value = viewWidth + iconLeft;
                } else {
                    float iconRight = ((view.getX() + view.getWidth()) - ((view.getWidth() - this.mMenuIconBgRadius) / 2)) + index_2;
                    iconLeft = iconRight - viewWidth;
                    value = iconRight;
                }
                top = (view.getY() + ((view.getHeight() - this.mMenuIconBgRadius) / 2)) - index_3;
                value_2 = top + viewHeight;
                right = value;
                left = iconLeft;
            }
            rectF.left = left;
            rectF.top = top;
            rectF.right = right;
            rectF.bottom = value_2;
            this.mHintRedDotHelper.drawRedPoint(canvas, index, Integer.valueOf(amount), rectF);
        }
    }

    private void ensureOverflowMenu() {
        if (this.mOverflowPopup == null) {
            Context context = getContext();
            if (!COUIContextUtil.isCOUITheme(context)) {
                Configuration configuration = getContext().getResources().getConfiguration();
                configuration.densityDpi = getContext().getResources().getDisplayMetrics().densityDpi;
                context = new ContextThemeWrapper(getContext().createConfigurationContext(configuration), R.style.Theme_COUI);
            }
            COUIPopupListWindow cOUIPopupListWindow = new COUIPopupListWindow(context);
            this.mOverflowPopup = cOUIPopupListWindow;
            cOUIPopupListWindow.setUseBackgroundBlur(this.mUseBackgroundBlur, this.mBlurMinAnimLevel);
            this.mOverflowPopup.setInputMethodMode(2);
            this.mOverflowPopup.setOnDismissListener(this.mOnDismissListener);
            this.mOverflowItems = new ArrayList<>();
        }
    }

    private boolean isLayoutRTL() {
        return ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL;
    }

    private int measureChild(int widthMeasureSpec, int heightMeasureSpec) {
        int iMeasureChildCollapseMargins;
        int index = 0;
        if (!shouldUseStrictTextMeasure()) {
            int iMeasureChildCollapseMargins2 = 0;
            while (index < getChildCount()) {
                iMeasureChildCollapseMargins2 += measureChildCollapseMargins(getChildAt(index), widthMeasureSpec, iMeasureChildCollapseMargins2, heightMeasureSpec, 0);
                index++;
            }
            return iMeasureChildCollapseMargins2;
        }
        int size = View.MeasureSpec.getSize(widthMeasureSpec);
        int index_2 = this.mToolbarTitleMinWidth;
        while (index < getChildCount()) {
            View childAt = getChildAt(index);
            if (childAt instanceof COUIActionMenuItemView) {
                COUIActionMenuItemView cOUIActionMenuItemView = (COUIActionMenuItemView) childAt;
                if (cOUIActionMenuItemView.isTextMenuItem()) {
                    TextView textView = (TextView) childAt;
                    int iMeasureTextLineCount = COUIChangeTextUtil.measureTextLineCount(textView, this.mTextMenuItemMaxWidth, this.mTextMenuItemHorizontalPadding * 2);
                    if (index == 0) {
                        if (iMeasureTextLineCount <= MAX_TEXT_MENU_ITEM_LINE) {
                            cOUIActionMenuItemView.setMaxWidth(this.mTextMenuItemMaxWidth);
                        } else {
                            cOUIActionMenuItemView.setMaxWidth((size - index_2) / 2);
                        }
                        iMeasureChildCollapseMargins = measureChildCollapseMargins(childAt, widthMeasureSpec, ((size - index_2) / 2) + index_2, heightMeasureSpec, 0);
                    } else {
                        if (iMeasureTextLineCount <= MAX_TEXT_MENU_ITEM_LINE) {
                            cOUIActionMenuItemView.setMaxWidth(this.mTextMenuItemMaxWidth);
                        } else {
                            cOUIActionMenuItemView.setMaxWidth(COUIChangeTextUtil.binarySearchForOptimalTextViewWidth(textView, 2, this.mTextMenuItemMaxWidth, size - index_2, this.mTextMenuItemHorizontalPadding * 2));
                        }
                        iMeasureChildCollapseMargins = measureChildCollapseMargins(childAt, widthMeasureSpec, index_2, heightMeasureSpec, 0);
                    }
                    index_2 += iMeasureChildCollapseMargins;
                }
            }
            index++;
        }
        return index_2;
    }

    private int measureChildCollapseMargins(View view, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        int index = marginLayoutParams.leftMargin + marginLayoutParams.rightMargin;
        view.measure(ViewGroup.getChildMeasureSpec(parentWidthMeasureSpec, getPaddingLeft() + getPaddingRight() + index + widthUsed, marginLayoutParams.width), ViewGroup.getChildMeasureSpec(parentHeightMeasureSpec, getPaddingTop() + getPaddingBottom() + marginLayoutParams.topMargin + marginLayoutParams.bottomMargin + heightUsed, marginLayoutParams.height));
        return view.getMeasuredWidth() + index;
    }

    private void resetItemMargin() {
        int index_2 = -1;
        int index_3 = -1;
        int index_4 = 0;
        for (int index = 0; index < getChildCount(); index++) {
            if (getChildAt(index).getVisibility() != 8) {
                index_4++;
                if (index_4 == 1) {
                    index_2 = index;
                    index_3 = index_2;
                } else {
                    index_3 = index;
                }
            }
        }
        if (index_2 != -1 && !this.mIsSameSide && index_4 > 1) {
            View childAt = getChildAt(index_2);
            if (childAt instanceof ActionMenuItemView) {
                ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) childAt.getLayoutParams();
                if (((ActionMenuItemView) childAt).getItemData().getIcon() == null) {
                    if (isLayoutRTL()) {
                        marginLayoutParams.rightMargin = this.mEdgeTextItemMargin;
                    } else {
                        marginLayoutParams.leftMargin = this.mEdgeTextItemMargin;
                    }
                } else if (isLayoutRTL()) {
                    marginLayoutParams.rightMargin = this.mEdgeIconItemMargin;
                } else {
                    marginLayoutParams.leftMargin = this.mEdgeIconItemMargin;
                }
            }
        }
        if (index_3 != -1) {
            View childAt2 = getChildAt(index_3);
            if (childAt2 instanceof ActionMenuItemView) {
                ViewGroup.MarginLayoutParams marginLayoutParams2 = (ViewGroup.MarginLayoutParams) childAt2.getLayoutParams();
                if (((ActionMenuItemView) childAt2).getItemData().getIcon() == null) {
                    if (isLayoutRTL()) {
                        marginLayoutParams2.leftMargin = this.mEdgeTextItemMargin;
                        return;
                    } else {
                        marginLayoutParams2.rightMargin = this.mEdgeTextItemMargin;
                        return;
                    }
                }
                if (isLayoutRTL()) {
                    marginLayoutParams2.leftMargin = this.mEdgeIconItemMargin;
                } else {
                    marginLayoutParams2.rightMargin = this.mEdgeIconItemMargin;
                }
            }
        }
    }

    private String setRedDotDescription(int amount) {
        return amount != -1
                ? amount != 0
                        ? getResources().getQuantityString(
                                this.mRedDotWithNumberDescriptionId, amount, amount)
                        : this.mRedDotDescription
                : EMPTY_TITLE;
    }

    private boolean shouldUseStrictTextMeasure() {
        if (getChildCount() != 2 || this.mIsSameSide) {
            return false;
        }
        for (int index = 0; index < getChildCount(); index++) {
            View child = getChildAt(index);
            if (child instanceof COUIActionMenuItemView
                    && !((COUIActionMenuItemView) child).isTextMenuItem()) {
                return false;
            }
        }
        return true;
    }

    public void tryBuildOverflowMenu() {
        ensureOverflowMenu();
        this.mOverflowItems.clear();
        if (this.mMenu != null) {
            PopupListItem.Builder builder = new PopupListItem.Builder();
            for (int index = 0; index < this.mMenu.getNonActionItems().size(); index++) {
                MenuItemImpl menuItem = this.mMenu.getNonActionItems().get(index);
                this.mMenuItem = menuItem;
                ArrayList<PopupListItem> subMenuItems = null;
                if (menuItem.hasSubMenu()) {
                    subMenuItems = new ArrayList<>();
                    SubMenu subMenu = this.mMenuItem.getSubMenu();
                    for (int subIndex = 0; subIndex < subMenu.size(); subIndex++) {
                        MenuItem item = subMenu.getItem(subIndex);
                        builder.reset().setId(item.getItemId()).setIcon(item.getIcon()).setTitle(item.getTitle() != null ? item.getTitle().toString() : "").setGroupId(item.getGroupId()).setIsEnable(item.isEnabled());
                        subMenuItems.add(builder.build());
                    }
                }
                Integer mappedAmount = this.mRedDotMap.get(this.mMenuItem.getItemId());
                int redDotAmount = mappedAmount != null ? mappedAmount : -1;
                int hintType = redDotAmount != -1 ? 0 : -1;
                builder.reset().setId(this.mMenuItem.getItemId()).setIcon(this.mMenuItem.getIcon()).setTitle(this.mMenuItem.getTitle() != null ? this.mMenuItem.getTitle().toString() : "").setIsChecked(this.mMenuItem.isChecked()).setGroupId(this.mMenuItem.getGroupId()).setRedDotAmount(redDotAmount).setHintType(hintType).setSubMenuItemList(subMenuItems).setIsEnable(this.mMenuItem.isEnabled());
                this.mOverflowItems.add(builder.build());
            }
            this.mOverflowPopup.setItemList(this.mOverflowItems);
            this.mOverflowPopup.setIsFixedFontSize(this.mIsFixTitleFontSize);
            this.mOverflowPopup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (position < COUIActionMenuView.this.mMenu.getNonActionItems().size()) {
                        if (COUIActionMenuView.this.mOverflowItems.get(position).getSubMenuItemList() != null) {
                            COUIActionMenuView.this.mMenu.performItemAction(COUIActionMenuView.this.mMenu.getNonActionItems().get(position), 4);
                            return;
                        }
                        if (COUIActionMenuView.this.mMenu.getNonActionItems().get(position).getIntent() != null) {
                            COUIActionMenuView.this.mOverflowPopup.setDismissWithWindowAnimation(true);
                        }
                        COUIActionMenuView.this.mMenu.performItemAction(
                                COUIActionMenuView.this.mMenu.getNonActionItems().get(position), 0);
                        COUIActionMenuView.this.mOverflowPopup.forceDismiss();
                        return;
                    }
                    COUILog.e(COUIActionMenuView.TAG, "IndexOutOfBoundsException! position = " + position + " non action items size = " + COUIActionMenuView.this.mMenu.getNonActionItems().size() + "popup menu size = " + COUIActionMenuView.this.mOverflowPopup.getItemList().size());
                }
            });
            this.mOverflowPopup.setSubMenuClickListener(this.mOnSubMenuItemClickListener);
            OverflowMenuListener overflowMenuListener = this.mOverflowMenuListener;
            if (overflowMenuListener != null) {
                overflowMenuListener.onOverflowMenuPreShow(this.mOverflowPopup);
            }
        }
    }

    @Override
    public void addView(View view, int index, ViewGroup.LayoutParams layoutParams) {
        view.setHapticFeedbackEnabled(false);
        TooltipCompat.setTooltipText(view, "");
        if (((ActionMenuView.LayoutParams) layoutParams).isOverflowButton) {
            this.mOverFlowMenuButton = view;
            configOverflowIconBackground();
            layoutParams.height = -1;
            this.mOverFlowMenuButton.setMinimumWidth(this.mOverFlowMinWidth);
            View view_2 = this.mOverFlowMenuButton;
            view_2.setPadding(this.mOverFlowHorPadding, view_2.getPaddingTop(), this.mOverFlowHorPadding, this.mOverFlowMenuButton.getPaddingBottom());
            this.mOverFlowMenuButton.setOnTouchListener(null);
            this.mOverFlowMenuButton.setLongClickable(false);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view_3) {
                    if (COUIActionMenuView.this.mOverflowPopup != null
                            && COUIActionMenuView.this.mOverflowPopup.isShowing()) {
                        COUIActionMenuView.this.mOverflowPopup.forceDismiss();
                        return;
                    }
                    COUIActionMenuView.this.tryBuildOverflowMenu();
                    COUIActionMenuView cOUIActionMenuView = COUIActionMenuView.this;
                    cOUIActionMenuView.post(cOUIActionMenuView.mOpenOverflowRunnable);
                }
            });
        }
        super.addView(view, index, layoutParams);
        configMenuItemViewAlignment();
    }

    public void clearRedDotInfo() {
        this.mNonActionRedDotSum = 0;
        this.mNonActionRedDotCount = 0;
        this.mRedDotMap.clear();
        postInvalidate();
    }

    public void configMenuItemViewAlignment() {
        if (getParent() instanceof COUIToolbar) {
            this.mIsSameSide = !((COUIToolbar) getParent()).getIsTitleCenterStyle();
        } else {
            this.mIsSameSide = true;
        }
        if (!this.mIsSameSide) {
            View view = null;
            for (int childCount = getChildCount() - 1; childCount >= 0; childCount--) {
                View childAt = getChildAt(childCount);
                if (childAt instanceof ActionMenuItemView) {
                    if (view != null) {
                        childAt.setTextAlignment(5);
                        view.setTextAlignment(6);
                    } else {
                        childAt.setTextAlignment(6);
                    }
                    view = childAt;
                }
            }
            return;
        }
        int index = 0;
        for (int childCount2 = getChildCount() - 1; childCount2 >= 0; childCount2--) {
            View childAt2 = getChildAt(childCount2);
            if (childAt2 instanceof ActionMenuItemView) {
                index++;
                childAt2.setTextAlignment(4);
            }
        }
        if (index == 1 && (getChildAt(0) instanceof COUIActionMenuItemView)) {
            COUIActionMenuItemView cOUIActionMenuItemView = (COUIActionMenuItemView) getChildAt(0);
            if (cOUIActionMenuItemView.isTextMenuItem()) {
                cOUIActionMenuItemView.setTextAlignment(6);
            }
        }
    }

    @Override
    public void dismissPopupMenus() {
        COUIPopupListWindow cOUIPopupListWindow = this.mOverflowPopup;
        if (cOUIPopupListWindow != null) {
            cOUIPopupListWindow.dismiss();
        }
        super.dismissPopupMenus();
    }

    @Override
    public void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        for (int index = 0; index < getChildCount(); index++) {
            View childAt = getChildAt(index);
            if (this.mRedDotMap.containsKey(Integer.valueOf(childAt.getId()))) {
                Integer num = this.mRedDotMap.get(Integer.valueOf(childAt.getId()));
                drawRedDot(childAt, num == null ? 0 : num.intValue(), canvas);
            }
            if (((ActionMenuView.LayoutParams) childAt.getLayoutParams()).isOverflowButton && this.mRedDotMap.size() > 0) {
                int index_2 = this.mNonActionRedDotCount == 0 ? -1 : this.mNonActionRedDotSum;
                drawRedDot(childAt, index_2, canvas);
                childAt.setContentDescription(TextUtils.isEmpty(setRedDotDescription(index_2)) ? this.mOverFlowButtonDescription : this.mOverFlowButtonDescription + COUIAccessibilityUtil.PAUSE_STRING + setRedDotDescription(index_2));
            }
        }
    }

    @Override
    public Menu getMenu() {
        MenuBuilder menuBuilder = (MenuBuilder) super.getMenu();
        this.mMenu = menuBuilder;
        return menuBuilder;
    }

    public View getOverFlowMenuButton() {
        return this.mOverFlowMenuButton;
    }

    public COUIPopupListWindow getOverflowPopupWindow() {
        return this.mOverflowPopup;
    }

    @Override
    public void initialize(MenuBuilder menuBuilder) {
        this.mMenu = menuBuilder;
        super.initialize(menuBuilder);
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int childCount = getChildCount();
        int index_4 = 0;
        int index_5 = 0;
        for (int index = 0; index < childCount; index++) {
            if (getChildAt(index).getVisibility() != 8) {
                index_5++;
            }
        }
        boolean zB = ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL;
        int index_6 = (bottom - top) / 2;
        if (this.mIsSameSide) {
            if (zB) {
                int width = getWidth() - getPaddingRight();
                while (index_4 < childCount) {
                    View childAt = getChildAt(index_4);
                    ActionMenuView.LayoutParams cVar = (ActionMenuView.LayoutParams) childAt.getLayoutParams();
                    if (childAt.getVisibility() != 8) {
                        int index_7 = width - ((LinearLayout.LayoutParams) cVar).rightMargin;
                        int measuredWidth = childAt.getMeasuredWidth();
                        int measuredHeight = childAt.getMeasuredHeight();
                        int index_8 = index_6 - (measuredHeight / 2);
                        childAt.layout(index_7 - measuredWidth, index_8, index_7, measuredHeight + index_8);
                        width = index_7 - ((measuredWidth + ((LinearLayout.LayoutParams) cVar).leftMargin) + this.mItemSpacing);
                    }
                    index_4++;
                }
                return;
            }
            int paddingLeft = getPaddingLeft();
            while (index_4 < childCount) {
                View childAt2 = getChildAt(index_4);
                ActionMenuView.LayoutParams cVar2 = (ActionMenuView.LayoutParams) childAt2.getLayoutParams();
                if (childAt2.getVisibility() != 8) {
                    int index_9 = paddingLeft + ((LinearLayout.LayoutParams) cVar2).leftMargin;
                    int measuredWidth2 = childAt2.getMeasuredWidth();
                    int measuredHeight2 = childAt2.getMeasuredHeight();
                    int index_10 = index_6 - (measuredHeight2 / 2);
                    childAt2.layout(index_9, index_10, index_9 + measuredWidth2, measuredHeight2 + index_10);
                    paddingLeft = index_9 + measuredWidth2 + ((LinearLayout.LayoutParams) cVar2).rightMargin + this.mItemSpacing;
                }
                index_4++;
            }
            return;
        }
        if (zB) {
            int paddingLeft2 = getPaddingLeft();
            boolean flag = true;
            for (int index_2 = childCount - 1; index_2 >= 0; index_2--) {
                View childAt3 = getChildAt(index_2);
                ActionMenuView.LayoutParams cVar3 = (ActionMenuView.LayoutParams) childAt3.getLayoutParams();
                if (childAt3.getVisibility() != 8) {
                    paddingLeft2 += ((LinearLayout.LayoutParams) cVar3).leftMargin;
                    if (flag) {
                        if ((childAt3 instanceof TextView) && !TextUtils.isEmpty(((TextView) childAt3).getText())) {
                            paddingLeft2 += this.mTextExtarPadding;
                        }
                        flag = false;
                    }
                    int measuredWidth3 = childAt3.getMeasuredWidth();
                    int measuredHeight3 = childAt3.getMeasuredHeight();
                    int index_11 = index_6 - (measuredHeight3 / 2);
                    if (index_2 != 0 || index_5 <= 1) {
                        childAt3.layout(paddingLeft2, index_11, paddingLeft2 + measuredWidth3, measuredHeight3 + index_11);
                        paddingLeft2 += measuredWidth3 + ((LinearLayout.LayoutParams) cVar3).rightMargin + this.mItemSpacing;
                    } else {
                        int width2 = ((getWidth() - getPaddingRight()) - ((LinearLayout.LayoutParams) cVar3).rightMargin) - measuredWidth3;
                        if ((childAt3 instanceof TextView) && !TextUtils.isEmpty(((TextView) childAt3).getText())) {
                            width2 -= this.mMenuViewPadding;
                        }
                        childAt3.layout(width2, index_11, measuredWidth3 + width2, measuredHeight3 + index_11);
                    }
                }
            }
            return;
        }
        int width3 = getWidth() - getPaddingRight();
        boolean flag_2 = true;
        for (int index_3 = childCount - 1; index_3 >= 0; index_3--) {
            View childAt4 = getChildAt(index_3);
            ActionMenuView.LayoutParams cVar4 = (ActionMenuView.LayoutParams) childAt4.getLayoutParams();
            if (childAt4.getVisibility() != 8) {
                width3 -= ((LinearLayout.LayoutParams) cVar4).rightMargin;
                if (flag_2) {
                    if ((childAt4 instanceof TextView) && !TextUtils.isEmpty(((TextView) childAt4).getText())) {
                        width3 -= this.mTextExtarPadding;
                    }
                    flag_2 = false;
                }
                int measuredWidth4 = childAt4.getMeasuredWidth();
                int measuredHeight4 = childAt4.getMeasuredHeight();
                int index_12 = index_6 - (measuredHeight4 / 2);
                if (index_3 != 0 || index_5 <= 1) {
                    childAt4.layout(width3 - measuredWidth4, index_12, width3, measuredHeight4 + index_12);
                    width3 -= (measuredWidth4 + ((LinearLayout.LayoutParams) cVar4).leftMargin) + this.mItemSpacing;
                } else {
                    int paddingLeft3 = getPaddingLeft() + ((LinearLayout.LayoutParams) cVar4).leftMargin;
                    if ((childAt4 instanceof TextView) && !TextUtils.isEmpty(((TextView) childAt4).getText())) {
                        paddingLeft3 += this.mMenuViewPadding;
                    }
                    childAt4.layout(paddingLeft3, index_12, measuredWidth4 + paddingLeft3, measuredHeight4 + index_12);
                }
            }
        }
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (this.mMenu == null) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        this.mIsSameSide = true;
        if ((getParent() instanceof COUIToolbar) && ((COUIToolbar) getParent()).getIsTitleCenterStyle()) {
            this.mIsSameSide = false;
        }
        setPadding(0, getPaddingTop(), 0, getPaddingBottom());
        boolean layoutDirection = ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL;
        int size = View.MeasureSpec.getSize(widthMeasureSpec);
        View.MeasureSpec.getSize(heightMeasureSpec);
        resetItemMargin();
        int iMeasureChild = measureChild(widthMeasureSpec, heightMeasureSpec);
        int measuredHeight = 0;
        for (int index = 0; index < getChildCount(); index++) {
            View childAt = getChildAt(index);
            if (childAt.getMeasuredHeight() > measuredHeight) {
                measuredHeight = childAt.getMeasuredHeight();
            }
        }
        if (this.mIsSameSide) {
            int childCount = getChildCount();
            if (childCount > 0) {
                int index_3 = 0;
                int index_4 = -1;
                for (int index_2 = 0; index_2 < childCount; index_2++) {
                    if (getChildAt(index_2).getVisibility() != 8) {
                        index_3++;
                        index_4 = index_2;
                    }
                }
                int index_5 = iMeasureChild + ((index_3 - 1) * this.mItemSpacing);
                if (index_4 != -1) {
                    View childAt2 = getChildAt(index_4);
                    if ((childAt2 instanceof TextView) && !TextUtils.isEmpty(((TextView) childAt2).getText())) {
                        index_5 += this.mTextExtarPadding;
                    }
                }
                size = index_5;
            } else {
                size = 0;
            }
            if (layoutDirection) {
                setPadding(getPaddingLeft(), getPaddingTop(), 0, getPaddingBottom());
            }
        }
        setMeasuredDimension(size, measuredHeight);
    }

    public void refresh() {
        COUIPopupListWindow cOUIPopupListWindow = this.mOverflowPopup;
        if (cOUIPopupListWindow != null) {
            cOUIPopupListWindow.refresh();
        }
        COUIMaskRippleDrawable cOUIMaskRippleDrawable = this.mMaskRippleDrawable;
        if (cOUIMaskRippleDrawable != null) {
            cOUIMaskRippleDrawable.refresh(getContext());
        }
        for (int index = 0; index < getChildCount(); index++) {
            View child = getChildAt(index);
            if (child instanceof COUIActionMenuItemView) {
                ((COUIActionMenuItemView) child).refresh();
            }
        }
    }

    public void setBlurMinAnimLevel(AnimLevel animLevel) {
        this.mBlurMinAnimLevel = animLevel;
    }

    public void setDismissMenuWithWindowAnimation(boolean dismissWithAnimation) {
        ensureOverflowMenu();
        this.mOverflowPopup.setDismissWithWindowAnimation(dismissWithAnimation);
    }

    @Deprecated
    public void setEnableAddExtraWidth(boolean enabled) {
    }

    @Deprecated
    public void setIsFixTitleFontSize(boolean fixed) {
        this.mIsFixTitleFontSize = fixed;
    }

    @Deprecated
    public void setItemSpecialColor(int itemId, int color) {
    }

    public void setMenuItemGap(boolean hasGap) {
        int childCount = getChildCount();
        for (int index = 0; index < childCount; index++) {
            View child = getChildAt(index);
            if (child instanceof COUIActionMenuItemView) {
                ((COUIActionMenuItemView) child).setItemWithGap(hasGap);
            }
        }
    }

    public void setOnSubMenuItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        this.mOnSubMenuItemClickListener = onItemClickListener;
    }

    public void setOverflowMenuListener(OverflowMenuListener overflowMenuListener) {
        this.mOverflowMenuListener = overflowMenuListener;
    }

    @Override
    public void setOverflowReserved(boolean reserved) {
        super.setOverflowReserved(reserved);
        COUIPopupListWindow overflowPopup = this.mOverflowPopup;
        if (overflowPopup == null || !overflowPopup.isShowing()) {
            return;
        }
        if (this.mMenu.getNonActionItems().isEmpty()) {
            this.mOverflowPopup.dismiss();
        } else {
            tryBuildOverflowMenu();
        }
    }

    public void setPopupWindowOnDismissListener(PopupWindow.OnDismissListener onDismissListener) {
        this.mOnDismissListener = onDismissListener;
    }

    public void setRedDot(int itemId, int amount) {
        MenuBuilder menuBuilder = this.mMenu;
        if (menuBuilder == null) {
            Log.e(TAG, "The MenuBuilder is null");
            return;
        }
        menuBuilder.flagActionItems();
        MenuItemImpl menuItemImpl = (MenuItemImpl) this.mMenu.findItem(itemId);
        if (menuItemImpl == null) {
            return;
        }
        if (amount != -1) {
            if (!menuItemImpl.isActionButton()) {
                if (this.mRedDotMap.containsKey(itemId)) {
                    Integer previousAmount = this.mRedDotMap.get(itemId);
                    this.mNonActionRedDotSum = (this.mNonActionRedDotSum + amount)
                            - (previousAmount != null ? previousAmount : 0);
                } else {
                    this.mNonActionRedDotCount++;
                    this.mNonActionRedDotSum += amount;
                }
            }
            this.mRedDotMap.put(itemId, amount);
        } else if (this.mRedDotMap.containsKey(itemId)) {
            if (!menuItemImpl.isActionButton()) {
                int currentCount = this.mNonActionRedDotCount;
                this.mNonActionRedDotCount = currentCount - (currentCount == 0 ? 0 : 1);
                Integer previousAmount = this.mRedDotMap.get(itemId);
                this.mNonActionRedDotSum -= previousAmount != null ? previousAmount : 0;
            }
            this.mRedDotMap.remove(itemId);
        }
        CharSequence title = menuItemImpl.getTitle();
        if (amount != -1) {
            title = ((Object) title) + COUIAccessibilityUtil.PAUSE_STRING
                    + setRedDotDescription(amount);
        }
        menuItemImpl.setContentDescription(title);
        postInvalidate();
    }

    @Deprecated
    public void setSubItemSpecialColor(int itemId, int color) {
    }

    @Deprecated
    public void setSubMenuClickListener(COUISubMenuClickListener cOUISubMenuClickListener) {
        this.mOnSubMenuItemClickListener = cOUISubMenuClickListener;
    }

    @Deprecated
    public void setSubMenuList(ArrayList<PopupListItem> items, int position) {
    }

    public void setUseBackgroundBlur(boolean useBackgroundBlur) {
        this.mUseBackgroundBlur = useBackgroundBlur;
    }

    @Override
    public boolean showOverflowMenu() {
        View view;
        Activity activityContextToActivity = UIUtil.contextToActivity(getContext());
        if ((activityContextToActivity != null && (activityContextToActivity.isFinishing() || activityContextToActivity.isDestroyed())) || this.mOverflowPopup == null || (view = this.mOverFlowMenuButton) == null || view.getParent() == null) {
            return false;
        }
        tryBuildOverflowMenu();
        post(this.mOpenOverflowRunnable);
        return true;
    }

    public COUIActionMenuView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mOpenOverflowRunnable = new OpenOverflowRunnable();
        this.mMenu = null;
        this.mPresenterClasses = new ArrayList();
        this.mIsSameSide = true;
        this.mMenuViewPadding = 0;
        this.mSubMenuList = null;
        this.mSubPosition = -1;
        this.mEnableAddExtraWidth = true;
        this.mOverflowMenuListener = null;
        this.mUseBackgroundBlur = false;
        this.mBlurMinAnimLevel = UIUtil.ANIM_LEVEL_SUPPORT_BLUR_MIN;
        this.mOverFlowMinWidth = getResources().getDimensionPixelSize(R.dimen.coui_action_menu_item_min_width);
        this.mOverFlowHorPadding = getResources().getDimensionPixelSize(R.dimen.overflow_button_padding_horizontal);
        this.mEdgeIconItemMargin = getResources().getDimensionPixelSize(R.dimen.toolbar_edge_icon_menu_item_margin);
        this.mIconItemHorOffset = getResources().getDimensionPixelSize(R.dimen.toolbar_icon_item_horizontal_offset);
        this.mItemVerOffset = getResources().getDimensionPixelSize(R.dimen.toolbar_item_vertical_offset);
        this.mItemSpacing = getResources().getDimensionPixelSize(R.dimen.coui_actionbar_menuitemview_item_spacing);
        this.mRedDotMap = new HashMap<>();
        this.mRedDotHorizontalOffset = getResources().getDimensionPixelSize(R.dimen.coui_toolbar_menu_red_dot_horizontal_offset);
        this.mRedDotVerticalOffset = getResources().getDimensionPixelSize(R.dimen.coui_toolbar_menu_red_dot_vertical_offset);
        this.mRedDotWithNumberVerticalOffset = getResources().getDimensionPixelSize(R.dimen.coui_toolbar_menu_red_dot_with_number_vertical_offset);
        this.mRedDotWithNumberHorizontalOffset = getResources().getDimensionPixelSize(R.dimen.coui_toolbar_menu_red_dot_with_number_horizontal_offset);
        this.mRedDotWithSmallNumberHorizontalOffset = getResources().getDimensionPixelSize(R.dimen.coui_toolbar_menu_red_dot_with_small_number_horizontal_offset);
        this.mRedDotWithBigNumberHorizontalOffset = getResources().getDimensionPixelSize(R.dimen.coui_toolbar_menu_red_dot_with_big_number_horizontal_offset);
        this.mMenuIconTopPadding = getResources().getDimensionPixelSize(R.dimen.coui_toolbar_menu_icon_top_padding);
        this.mHintRedDotHelper = new COUIHintRedDotHelper(getContext(), null, R.styleable.COUIHintRedDot, 0, R.style.Widget_COUI_COUIHintRedDot_Small);
        this.mOverFlowButtonDescription = getResources().getString(R.string.abc_action_menu_overflow_description);
        this.mRedDotDescription = getResources().getString(R.string.red_dot_description);
        this.mRedDotWithNumberDescriptionId = R.plurals.red_dot_with_number_description;
        this.mMenuIconBgRadius = getResources().getDimensionPixelSize(R.dimen.coui_toolbar_menu_bg_radius);
        this.mTextMenuItemMaxWidth = context.getResources().getDimensionPixelSize(R.dimen.coui_action_bar_text_menu_item_max_width);
        this.mToolbarTitleMinWidth = context.getResources().getDimensionPixelSize(R.dimen.coui_toolbar_title_min_width);
        this.mTextMenuItemHorizontalPadding = context.getResources().getDimensionPixelSize(R.dimen.coui_toolbar_text_menu_bg_padding_horizontal);
    }

    public void dismissPopupMenus(boolean animate) {
        COUIPopupListWindow overflowPopup = this.mOverflowPopup;
        if (overflowPopup != null) {
            if (animate) {
                overflowPopup.dismiss();
            } else {
                overflowPopup.superDismiss();
            }
        }
    }
}
