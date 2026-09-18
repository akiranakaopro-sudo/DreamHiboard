package com.coui.appcompat.poplist;

import com.coui.appcompat.R;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.ViewCompat;
import com.coui.appcompat.list.IListSelectedItem;
import com.coui.appcompat.log.COUILog;
import com.coui.appcompat.poplist.COUIPopupMenuRootView;
import com.coui.appcompat.state.COUIStateEffectDrawable;
import com.coui.appcompat.state.DrawableStateProxy;
import com.coui.appcompat.uiutil.AnimLevel;
import com.coui.appcompat.uiutil.UIUtil;
import com.coui.component.responsiveui.unit.Dp;
import com.coui.component.responsiveui.window.WindowHeightSizeClass;
import com.coui.component.responsiveui.window.WindowSizeClass;
import com.coui.component.responsiveui.window.WindowWidthSizeClass;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;


public class COUIPopupListWindow extends COUIPopupWindow {
    private static final boolean COUI_DEBUG;
    private static final int EXIT_DURATION = 350;
    public static final int GROUP_MIN_ITEMS = 3;
    private static final String TAG = "COUIPopupListWindow";
    private View mAnchorView;
    private COUIPopupMenuRootView mContentView;
    private Context mContext;
    private int mCustomMenuMaxWidth;
    private int mCustomMenuWidth;
    private boolean mDismissWhenLayoutChange;
    private boolean mDismissWhenWindowSizeChange;
    private boolean mDismissWithWindowAnimation;
    private int mGlobalOffsetX;
    private int mGlobalOffsetY;
    private boolean mIfNeedResetTouchableToTrue;
    private boolean mIsAdaptiveFontSize;
    private boolean mIsDismissing;
    private boolean mIsFixedFontSize;
    private int mLastClickedMainMenuItemPosition;
    private ListView mListViewUsedToMeasure;
    private PopupMenuLocateHelper mLocateHelper;
    private ListView mMainListView;
    private DefaultAdapter mMainMenuAdapter;
    private int mMainMenuHeight;
    private final AdapterView.OnItemClickListener mMainMenuItemClickListener;
    private List<PopupListItem> mMainMenuItemList;
    private int mMainMenuWidth;
    private RoundFrameLayout mMainMenuWrapper;
    private final View.OnLayoutChangeListener mMenuDismissWhenRootChange;
    private boolean mNeedOffsetWhenSetWindowType;
    private PopupWindow.OnDismissListener mOnDismissListener;
    private AdapterView.OnItemClickListener mOnMainMenuItemClickListener;
    private AdapterView.OnItemClickListener mOnSubMenuItemClickListener;
    private boolean mReuseMenuWhenOffsetChanged;
    private View mRootView;
    private int mShowOffsetX;
    private int mShowOffsetY;
    private ListView mSubListView;
    private DefaultAdapter mSubMenuAdapter;
    private View mSubMenuAnchorView;
    private int mSubMenuHeight;
    private final AdapterView.OnItemClickListener mSubMenuItemClickListener;
    private int mSubMenuWidth;
    private RoundFrameLayout mSubMenuWrapper;
    private final Runnable mWindowAnimationDismissRunnable;

    private final class MainMenuItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (!DefaultAdapter.isDataIndex(position)) {
                return;
            }
            int dataIndex = DefaultAdapter.realPositionToDataIndex(position);
            if (mOnMainMenuItemClickListener != null) {
                mOnMainMenuItemClickListener.onItemClick(parent, view, dataIndex, id);
            }
            if (mSubMenuWrapper.getParent() == null
                    || mLastClickedMainMenuItemPosition == dataIndex) {
                triggerShowSubMenu(view, dataIndex);
            } else {
                mContentView.hideSubMenu(false);
                mContentView.postSkipExitAnimationAndShowSubMenu(
                        () -> triggerShowSubMenu(view, dataIndex));
            }
        }
    }

    public final class WindowAnimationDismissRunnable implements Runnable {
        private WindowAnimationDismissRunnable() {
        }

        @Override
        public void run() {
            COUIPopupListWindow.this.forceDismiss();
        }
    }

    static {
        COUI_DEBUG = COUILog.LOG_DEBUG || COUILog.isLoggable(TAG, 3);
    }

    public COUIPopupListWindow(Context context) {
        super(context);
        this.mMenuDismissWhenRootChange = new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int left, int top, int right, int bottom,
                    int oldLeft, int oldTop, int oldRight, int oldBottom) {
                boolean layoutChanged = left != oldLeft || top != oldTop
                        || right != oldRight || bottom != oldBottom;
                COUILog.d(COUIPopupListWindow.TAG,
                        "PopupWindow anchor layout changed! left:" + left + ",top:" + top
                                + ",right:" + right + ",bottom:" + bottom + ",oldLeft:"
                                + oldLeft + ",oldTop:" + oldTop + ",oldRight:" + oldRight
                                + ",oldBottom:" + oldBottom + ",layoutChange:" + layoutChanged);
                if (layoutChanged) {
                    if (COUIPopupListWindow.this.mDismissWhenLayoutChange || (COUIPopupListWindow.this.mDismissWhenWindowSizeChange && COUIPopupListWindow.this.mLocateHelper.checkIfLimitedWindowOrAnchorResized(COUIPopupListWindow.this.mAnchorView, COUIPopupListWindow.this.mShowOffsetX, COUIPopupListWindow.this.mShowOffsetY, COUIPopupListWindow.this.mRootView))) {
                        COUIPopupListWindow.this.dismiss();
                    }
                }
            }
        };
        this.mMainMenuItemClickListener = new MainMenuItemClickListener();
        this.mSubMenuItemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int dataIndex = DefaultAdapter.realPositionToDataIndex(position);
                if (COUIPopupListWindow.this.mLocateHelper.isCurrentContainerSmallScreen()) {
                    dataIndex--;
                }
                if (dataIndex < 0) {
                    COUIPopupListWindow.this.mContentView.performSubMenuHeader(view);
                } else if (COUIPopupListWindow.this.mOnSubMenuItemClickListener != null) {
                    COUIPopupListWindow.this.mOnSubMenuItemClickListener.onItemClick(
                            parent, view, dataIndex, id);
                }
            }
        };
        this.mWindowAnimationDismissRunnable = new WindowAnimationDismissRunnable();
        this.mRootView = null;
        this.mSubMenuAnchorView = null;
        this.mGlobalOffsetX = 0;
        this.mGlobalOffsetY = 0;
        this.mCustomMenuWidth = -1;
        this.mCustomMenuMaxWidth = -1;
        this.mShowOffsetX = Integer.MIN_VALUE;
        this.mShowOffsetY = Integer.MIN_VALUE;
        this.mLastClickedMainMenuItemPosition = -1;
        this.mDismissWhenLayoutChange = false;
        this.mDismissWhenWindowSizeChange = true;
        this.mIsAdaptiveFontSize = false;
        this.mIsFixedFontSize = false;
        this.mNeedOffsetWhenSetWindowType = false;
        this.mIsDismissing = false;
        this.mReuseMenuWhenOffsetChanged = true;
        this.mIfNeedResetTouchableToTrue = false;
        this.mDismissWithWindowAnimation = false;
        this.mContext = context;
        setClippingEnabled(false);
        setTouchModal(false);
        setFocusable(true);
        setOutsideTouchable(true);
        setElevationInPopupwindow(true);
        setExitTransition(null);
        setEnterTransition(null);
        setAnimationStyle(R.style.Animation_COUI_PopupListWindow);
        ListView listView = new ListView(context);
        this.mListViewUsedToMeasure = listView;
        listView.setDivider(null);
        this.mListViewUsedToMeasure.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
        this.mMainMenuItemList = new ArrayList();
        COUIPopupMenuRootView contentView = createContentView();
        this.mContentView = contentView;
        setContentView(contentView);
        this.mLocateHelper = new PopupMenuLocateHelper(this.mContext);
    }

    private void cancelDelayedDismiss() {
        COUIPopupMenuRootView contentView = this.mContentView;
        if (contentView != null) {
            contentView.removeDelayedDismiss();
        }
    }

    private boolean checkListElementsNotNull(List<?> list) {
        Iterator<?> it = list.iterator();
        while (it.hasNext()) {
            if (it.next() == null) {
                return false;
            }
        }
        return true;
    }

    private boolean checkListNotNull(List<?> list) {
        return (list == null || list.isEmpty()) ? false : true;
    }

    private void configMainListView() {
        this.mLastClickedMainMenuItemPosition = -1;
        this.mMainListView.setAdapter((ListAdapter) this.mMainMenuAdapter);
        if (this.mOnMainMenuItemClickListener != null) {
            this.mMainListView.setOnItemClickListener(this.mMainMenuItemClickListener);
        }
    }

    private void configSubListView() {
        this.mSubListView.setAdapter((ListAdapter) this.mSubMenuAdapter);
        this.mSubListView.setOnItemClickListener(this.mSubMenuItemClickListener);
    }

    private COUIPopupMenuRootView createContentView() {
        COUIPopupMenuRootView cOUIPopupMenuRootView = new COUIPopupMenuRootView(this.mContext);
        cOUIPopupMenuRootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public final void onClick(View view) {
                COUIPopupListWindow.this.onOutsideContentClicked(view);
            }
        });
        LayoutInflater layoutInflaterFrom = LayoutInflater.from(this.mContext);
        int index_2 = R.layout.coui_popup_list_window_layout;
        this.mMainMenuWrapper = (RoundFrameLayout) layoutInflaterFrom.inflate(index_2, (ViewGroup) cOUIPopupMenuRootView, false);
        this.mSubMenuWrapper = (RoundFrameLayout) LayoutInflater.from(this.mContext).inflate(index_2, (ViewGroup) cOUIPopupMenuRootView, false);
        RoundFrameLayout roundFrameLayout = this.mMainMenuWrapper;
        int index_3 = R.id.coui_popup_list_view;
        this.mMainListView = (ListView) roundFrameLayout.findViewById(index_3);
        this.mSubListView = (ListView) this.mSubMenuWrapper.findViewById(index_3);
        TypedArray typedArrayObtainStyledAttributes = this.mContext.getTheme().obtainStyledAttributes(new int[]{R.attr.couiPopupWindowBackground});
        Drawable drawable = typedArrayObtainStyledAttributes.getDrawable(0);
        if (drawable == null) {
            drawable = ResourcesCompat.getDrawable(this.mContext.getResources(), R.drawable.coui_popup_window_background, this.mContext.getTheme());
        }
        if (drawable != null) {
            this.mMainMenuWrapper.setBackground(drawable.getConstantState().newDrawable());
            this.mSubMenuWrapper.setBackground(drawable.getConstantState().newDrawable());
        }
        typedArrayObtainStyledAttributes.recycle();
        cOUIPopupMenuRootView.setOnSubMenuStateChangedListener(new COUIPopupMenuRootView.OnMenuStateChangedListener() {
            private void requestAccessibilityFocusForListHeader(ViewGroup viewGroup) {
                View childAt = viewGroup.getChildAt(0);
                if (childAt != null) {
                    childAt.performAccessibilityAction(64, null);
                }
            }

            private void setListViewFocusable(ListView listView, boolean isMainMenu) {
                if (listView != null) {
                    listView.setFocusable(false);
                    for (int index = 0; index < listView.getChildCount(); index++) {
                        listView.getChildAt(index).setFocusable(isMainMenu);
                    }
                }
            }

            @Override
            public void onMainMenuAnimationCanceled() {
                COUIPopupListWindow.this.mIsDismissing = false;
                COUIPopupListWindow.this.mSubMenuAnchorView = null;
                try {
                    COUIPopupListWindow.super.dismiss();
                } catch (IllegalArgumentException e) {
                    COUILog.w(COUIPopupListWindow.TAG, "Failed to dismiss popup window, view may be detached: " + e.getMessage());
                }
            }

            @Override
            public void onMainMenuEntered() {
                requestAccessibilityFocusForListHeader(COUIPopupListWindow.this.mMainListView);
            }

            @Override
            public void onMainMenuExited() {
                COUIPopupListWindow.this.mIsDismissing = false;
                COUIPopupListWindow.this.mSubMenuAnchorView = null;
                COUIPopupListWindow cOUIPopupListWindow = COUIPopupListWindow.this;
                cOUIPopupListWindow.setAnchorHoveredState(false, cOUIPopupListWindow.mAnchorView);
                COUIPopupListWindow.this.mMainMenuWidth = 0;
                COUIPopupListWindow.this.mMainMenuHeight = 0;
                try {
                    COUIPopupListWindow.super.dismiss();
                } catch (IllegalArgumentException e) {
                    COUILog.w(COUIPopupListWindow.TAG, "Failed to dismiss popup window, view may be detached: " + e.getMessage());
                }
            }

            @Override
            public void onMainMenuStartToEnter() {
                COUIPopupListWindow.this.mIsDismissing = false;
                // Leapy modified 2026-07-25: Exact decoded OPPO/smali reset.
                // Touchable and focusable are restored as one popup state.
                if (COUIPopupListWindow.this.mIfNeedResetTouchableToTrue) {
                    COUIPopupListWindow.this.setTouchable(true);
                    COUIPopupListWindow.this.setFocusable(true);
                    COUIPopupListWindow.this.update();
                    COUIPopupListWindow.this.mIfNeedResetTouchableToTrue = false;
                }
                // Leapy end
            }

            @Override
            public void onMainMenuStartToExit() {
                COUIPopupListWindow.this.mIsDismissing = true;
            }

            @Override
            public void onSubMenuAnimationCanceled() {
                COUIPopupListWindow.this.setSubMenuGroupItemActivation(false);
            }

            @Override
            public void onSubMenuEntered() {
                requestAccessibilityFocusForListHeader(COUIPopupListWindow.this.mSubListView);
            }

            @Override
            public void onSubMenuExited() {
                if (COUIPopupListWindow.this.mSubMenuAnchorView != null) {
                    if (COUIPopupListWindow.this.mSubListView != null && COUIPopupListWindow.this.mSubListView.getChildAt(0) != null) {
                        COUIPopupListWindow.this.mSubListView.getChildAt(0).setBackground(null);
                    }
                    COUIPopupListWindow.this.mSubMenuAnchorView = null;
                }
            }

            @Override
            public void onSubMenuStartToEnter() {
                COUIPopupListWindow.this.setSubMenuGroupItemActivation(true);
                setListViewFocusable(COUIPopupListWindow.this.mMainListView, false);
            }

            @Override
            public void onSubMenuStartToExit() {
                COUIPopupListWindow.this.setSubMenuGroupItemActivation(false);
                setListViewFocusable(COUIPopupListWindow.this.mMainListView, true);
            }
        });
        return cOUIPopupMenuRootView;
    }

    private int getMainMenuMaxWidth() {
        if (this.mCustomMenuWidth >= 0) {
            if (COUI_DEBUG) {
                Log.i(TAG, "Use custom menu width = " + this.mCustomMenuWidth);
            }
            return this.mCustomMenuWidth;
        }
        if (this.mCustomMenuMaxWidth >= getMainMenuMinWidth()) {
            return this.mCustomMenuMaxWidth;
        }
        Log.w(TAG, "Illegal max width! Custom menu max width smaller than min width!");
        DefaultAdapter defaultAdapter = this.mMainMenuAdapter;
        if (defaultAdapter == null) {
            Log.w(TAG, "Get main menu max width fail! Adapter is NULL!");
            return 0;
        }
        if (defaultAdapter.hasSubMenu() && !this.mMainMenuAdapter.hasIcon()) {
            return this.mContext.getResources().getDimensionPixelOffset(R.dimen.coui_popup_list_window_width_with_icon);
        }
        return this.mContext.getResources().getDimensionPixelOffset(R.dimen.coui_popup_list_window_max_width);
    }

    private int getMainMenuMinWidth() {
        int index = this.mCustomMenuWidth;
        if (index >= 0) {
            return index;
        }
        DefaultAdapter defaultAdapter = this.mMainMenuAdapter;
        if (defaultAdapter != null) {
            return defaultAdapter.hasSubMenu() ? this.mMainMenuAdapter.hasIcon() ? this.mContext.getResources().getDimensionPixelOffset(R.dimen.coui_popup_list_window_max_width) : this.mContext.getResources().getDimensionPixelOffset(R.dimen.coui_popup_list_window_width_with_icon) : this.mContext.getResources().getDimensionPixelOffset(R.dimen.coui_popup_list_window_min_width);
        }
        Log.w(TAG, "Get main menu min width fail! Adapter is NULL!");
        return 0;
    }

    private boolean isRtl(View view) {
        return ViewCompat.getLayoutDirection(view) == 1;
    }

    @Deprecated
    public static boolean isSmallScreen(Context context, int width, int height) {
        WindowSizeClass.Companion companion = WindowSizeClass.Companion;
        Dp.Companion companion2 = Dp.Companion;
        WindowSizeClass windowSizeClassCalculateFromSize = companion.calculateFromSize(companion2.pixel2Dp(context, Math.abs(width)), companion2.pixel2Dp(context, Math.abs(height)));
        return windowSizeClassCalculateFromSize.getWindowWidthSizeClass() == WindowWidthSizeClass.Compact || windowSizeClassCalculateFromSize.getWindowHeightSizeClass() == WindowHeightSizeClass.Compact;
    }

    public void onOutsideContentClicked(View view) {
        forceDismiss();
    }

    public static int comparePopupItemGroupId(PopupListItem popupListItem, PopupListItem popupListItem2) {
        return popupListItem.getGroupId() - popupListItem2.getGroupId();
    }

    private void refreshAdapter(List<PopupListItem> list, DefaultAdapter defaultAdapter) {
        defaultAdapter.setAdapterFontSize(this.mIsAdaptiveFontSize);
        defaultAdapter.setIsFixedFontSize(this.mIsFixedFontSize);
        defaultAdapter.setItemList(list);
    }

    private void reuseMainMenu(boolean animate) {
        this.mContentView.setMainMenuSize(this.mMainMenuWidth, this.mMainMenuHeight);
        this.mLocateHelper.prepareShowMainMenu(this.mMainMenuWidth, this.mMainMenuHeight, animate, this.mGlobalOffsetX, this.mGlobalOffsetY);
        syncMainMenuSize();
    }

    public void setAnchorHoveredState(boolean hovered, View view) {
        if (view != null && (view instanceof IListSelectedItem)) {
            if (view.getBackground() instanceof DrawableStateProxy) {
                ((DrawableStateProxy) view.getBackground()).setStateLocked(16843623, hovered, hovered, true);
            }
            if (view.getBackground() instanceof COUIStateEffectDrawable) {
                ((COUIStateEffectDrawable) view.getBackground()).setStateLocked(16843623, hovered, hovered, true);
            }
        }
    }

    private void setItemListInternal(List<PopupListItem> list, DefaultAdapter defaultAdapter, boolean isMainMenu) {
        HashSet hashSet;
        if (list.size() >= 3) {
            if (isMainMenu) {
                Collections.sort(list, new Comparator() {
                    @Override
                    public final int compare(Object obj, Object object) {
                        return COUIPopupListWindow.comparePopupItemGroupId((PopupListItem) obj, (PopupListItem) object);
                    }
                });
            }
            hashSet = new HashSet();
            int groupId = list.get(0).getGroupId();
            for (int index = 1; index < list.size(); index++) {
                int groupId2 = list.get(index).getGroupId();
                if (groupId2 != groupId) {
                    hashSet.add(Integer.valueOf(index));
                    groupId = groupId2;
                }
            }
        } else {
            hashSet = null;
        }
        if (hashSet != null) {
            defaultAdapter.setGroupSets(hashSet);
        }
        refreshAdapter(list, defaultAdapter);
    }

    public void setSubMenuGroupItemActivation(boolean activated) {
        if (this.mSubMenuAdapter == null) {
            return;
        }
        if (this.mLocateHelper.isCurrentContainerSmallScreen()) {
            int index = activated ? 2 : 0;
            Object item = this.mSubMenuAdapter.getItem(0);
            if (item instanceof PopupListItem) {
                ((PopupListItem) item).setGroupState(index);
                this.mSubMenuAdapter.notifyDataSetChanged();
                return;
            }
            return;
        }
        int index_2 = this.mLastClickedMainMenuItemPosition;
        if (index_2 != -1) {
            Object item2 = this.mMainMenuAdapter.getItem(DefaultAdapter.dataIndexToRealPosition(index_2));
            if (item2 instanceof PopupListItem) {
                ((PopupListItem) item2).setGroupState(activated ? 1 : 0);
                this.mMainMenuAdapter.notifyDataSetChanged();
            }
        }
        View view = this.mSubMenuAnchorView;
        if (view == null || !(view.getBackground() instanceof ListItemMaskEffectDrawable)) {
            return;
        }
        ((ListItemMaskEffectDrawable) this.mSubMenuAnchorView.getBackground()).setHoverStateLocked(activated, activated, true);
    }

    private void showSub(View view, int position) {
        if (this.mSubMenuWrapper.getParent() != null && position == this.mLastClickedMainMenuItemPosition) {
            this.mContentView.showSubMenu();
            return;
        }
        configSubListView();
        measurePopupWindow(this.mSubMenuAdapter);
        this.mContentView.setSubMenuSize(this.mSubMenuWidth, this.mSubMenuHeight);
        this.mLocateHelper.prepareShowSubMenu(view, this.mSubMenuWidth, this.mSubMenuHeight, isRtl(view));
        syncSubMenuSize();
        this.mContentView.addSubMenuView(this.mSubMenuWrapper);
    }

    private void syncMainMenuSize() {
        int mainMenuWidth = this.mLocateHelper.getMainMenuWidth();
        int mainMenuHeight = this.mLocateHelper.getMainMenuHeight();
        if (mainMenuWidth == this.mMainMenuWidth && mainMenuHeight == this.mMainMenuHeight) {
            return;
        }
        this.mMainMenuWidth = mainMenuWidth;
        this.mMainMenuHeight = mainMenuHeight;
        this.mContentView.setMainMenuSize(mainMenuWidth, mainMenuHeight);
    }

    private void syncSubMenuSize() {
        int subMenuWidth = this.mLocateHelper.getSubMenuWidth();
        int subMenuHeight = this.mLocateHelper.getSubMenuHeight();
        if (subMenuWidth == this.mSubMenuWidth && subMenuHeight == this.mSubMenuHeight) {
            return;
        }
        this.mSubMenuWidth = subMenuWidth;
        this.mSubMenuHeight = subMenuHeight;
        this.mContentView.setSubMenuSize(subMenuWidth, subMenuHeight);
    }

    public void triggerShowSubMenu(View view, int position) {
        PopupListItem popupListItem;
        this.mLastClickedMainMenuItemPosition = position;
        if (this.mMainMenuItemList.isEmpty() || this.mMainMenuItemList.size() <= position || (popupListItem = this.mMainMenuItemList.get(position)) == null || !popupListItem.isEnable() || !checkListNotNull(popupListItem.getSubMenuItemList()) || !checkListElementsNotNull(popupListItem.getSubMenuItemList())) {
            return;
        }
        ArrayList arrayList = new ArrayList();
        if (this.mLocateHelper.isCurrentContainerSmallScreen()) {
            arrayList.add(popupListItem);
        }
        this.mLocateHelper.setSubMenuAnchorIsFirstItem(position == 0);
        arrayList.addAll(popupListItem.getSubMenuItemList());
        if (this.mSubMenuAdapter == null) {
            this.mSubMenuAdapter = new DefaultAdapter(this.mContext, null);
        }
        setItemListInternal(arrayList, this.mSubMenuAdapter, false);
        if (view.getBackground() instanceof ListItemMaskEffectDrawable) {
            this.mSubMenuAdapter.setSharedBackground((ListItemMaskEffectDrawable) view.getBackground());
        }
        this.mSubMenuAnchorView = view;
        showSub(view, position);
    }

    @Deprecated
    public boolean configPopupValue(View view, boolean enabled) {
        return false;
    }

    @Override
    public void dismiss() {
        // Leapy modified 2026-07-25: Restore the exact decoded OPPO/smali
        // dismissal control flow. The decompiled local body had inverted and
        // empty focusability branches, leaving a focusable popup on screen.
        if (isTouchable()) {
            setTouchable(false);
            setFocusable(false);
            update();
            this.mIfNeedResetTouchableToTrue = true;
        }
        if (!isShowing() || this.mIsDismissing) {
            return;
        }
        cancelDelayedDismiss();
        if (this.mDismissWithWindowAnimation) {
            this.mIsDismissing = true;
            COUIPopupMenuRootView contentView = this.mContentView;
            if (contentView != null) {
                contentView.postDelayedDismiss(this.mWindowAnimationDismissRunnable, EXIT_DURATION);
            }
        } else {
            View view = this.mAnchorView;
            if (view != null && view.getRootView() != null) {
                this.mAnchorView.getRootView().removeOnLayoutChangeListener(
                        this.mMenuDismissWhenRootChange);
            }
            if (this.mLastClickedMainMenuItemPosition != -1
                    && this.mMainMenuAdapter != null) {
                COUILog.d(TAG, "LastClickedMainMenuItemPosition = "
                        + this.mLastClickedMainMenuItemPosition);
                Object item = this.mMainMenuAdapter.getItem(
                        DefaultAdapter.dataIndexToRealPosition(
                                this.mLastClickedMainMenuItemPosition));
                if (item instanceof PopupListItem) {
                    ((PopupListItem) item).setGroupState(0);
                }
            }
            this.mContentView.hideMainMenu(true);
            setAnchorHoveredState(false, this.mAnchorView);
        }
        PopupWindow.OnDismissListener onDismissListener = this.mOnDismissListener;
        if (onDismissListener != null) {
            onDismissListener.onDismiss();
        }
        // Leapy end
    }

    public void dismissSubMenu() {
        COUIPopupMenuRootView cOUIPopupMenuRootView = this.mContentView;
        if (cOUIPopupMenuRootView != null) {
            cOUIPopupMenuRootView.hideSubMenu(true);
        }
    }

    public void forceDismiss() {
        if (super.isShowing()) {
            cancelDelayedDismiss();
            View view = this.mAnchorView;
            if (view != null && view.getRootView() != null) {
                this.mAnchorView.getRootView().removeOnLayoutChangeListener(this.mMenuDismissWhenRootChange);
            }
            if (this.mLastClickedMainMenuItemPosition != -1 && this.mMainMenuAdapter != null) {
                COUILog.d(TAG, "LastClickedMainMenuItemPosition = " + this.mLastClickedMainMenuItemPosition);
                Object item = this.mMainMenuAdapter.getItem(DefaultAdapter.dataIndexToRealPosition(this.mLastClickedMainMenuItemPosition));
                if (item instanceof PopupListItem) {
                    ((PopupListItem) item).setGroupState(0);
                }
            }
            this.mSubMenuAnchorView = null;
            setAnchorHoveredState(false, this.mAnchorView);
            this.mIsDismissing = false;
            this.mMainMenuWidth = 0;
            this.mMainMenuHeight = 0;
            super.dismiss();
            PopupWindow.OnDismissListener onDismissListener = this.mOnDismissListener;
            if (onDismissListener != null && !this.mDismissWithWindowAnimation) {
                onDismissListener.onDismiss();
            }
            this.mDismissWithWindowAnimation = false;
        }
    }

    public ListAdapter getAdapter() {
        ListView listView = this.mMainListView;
        if (listView != null) {
            return listView.getAdapter();
        }
        return null;
    }

    public View getAnchorView() {
        return this.mAnchorView;
    }

    public List<PopupListItem> getItemList() {
        return this.mMainMenuItemList;
    }

    @Deprecated
    public ListView getListView() {
        return this.mMainListView;
    }

    public PopupMenuLocateHelper getLocateHelper() {
        return this.mLocateHelper;
    }

    public ListView getMainMenuListView() {
        return this.mMainListView;
    }

    public ListView getSubMenuListView() {
        return this.mSubListView;
    }

    @Override
    public void initElevationInPopupwindow() {
        setBackgroundDrawable(null);
    }

    @Override
    public void initOutlineRoundRectBackground() {
    }

    @Override
    public boolean isShowing() {
        return super.isShowing() && !this.mIsDismissing;
    }

    public void measurePopupWindow() {
        measurePopupWindow(this.mMainMenuAdapter);
    }

    public void prepareShowMainMenu(View view, int offsetX, int offsetY, boolean showAboveAnchor) {
        configMainListView();
        this.mLocateHelper.prepareWindowAndAnchor(view, offsetX, offsetY, this.mRootView);
        this.mContentView.setDomain(this.mLocateHelper.getDomain());
        this.mContentView.addMainMenuView(this.mMainMenuWrapper);
        if (this.mMainMenuWidth == 0 || this.mMainMenuHeight == 0) {
            measurePopupWindow();
        }
        this.mContentView.setMainMenuSize(this.mMainMenuWidth, this.mMainMenuHeight);
        this.mLocateHelper.prepareShowMainMenu(this.mMainMenuWidth, this.mMainMenuHeight, showAboveAnchor, this.mGlobalOffsetX, this.mGlobalOffsetY);
        syncMainMenuSize();
    }

    public void refresh() {
        TypedArray typedArrayObtainStyledAttributes = this.mContext.getTheme().obtainStyledAttributes(new int[]{R.attr.couiPopupWindowBackground});
        Drawable drawable = typedArrayObtainStyledAttributes.getDrawable(0);
        typedArrayObtainStyledAttributes.recycle();
        if (drawable == null) {
            drawable = ResourcesCompat.getDrawable(this.mContext.getResources(), R.drawable.coui_popup_window_background, this.mContext.getTheme());
        }
        if (drawable != null) {
            this.mMainMenuWrapper.setBackground(drawable.getConstantState().newDrawable());
            this.mSubMenuWrapper.setBackground(drawable.getConstantState().newDrawable());
        }
    }

    public void resetOffset() {
        setGlobalOffset(0, 0);
    }

    public void reuseMenuWhenOffsetChanged(boolean reuse) {
        this.mReuseMenuWhenOffsetChanged = reuse;
    }

    @Deprecated
    public void setAdapter(BaseAdapter baseAdapter, boolean isMainMenu) {
    }

    @Deprecated
    public void setAdapterFontSize(boolean fixedFontSize) {
        setIsAdaptiveFontSize(fixedFontSize);
    }

    @Deprecated
    public void setAlwaysBelowAnchor(boolean alwaysBelowAnchor) {
    }

    public void setAnchorView(View view) {
        this.mAnchorView = view;
    }

    @Deprecated
    public void setContentHeight(int height) {
    }

    @Deprecated
    public void setContentWidth(int width) {
    }

    public void setDismissWhenLayoutChange(boolean dismissWhenLayoutChanges) {
        this.mDismissWhenLayoutChange = dismissWhenLayoutChanges;
    }

    public void setDismissWhenWindowSizeChange(boolean dismissWhenWindowSizeChanges) {
        this.mDismissWhenWindowSizeChange = dismissWhenWindowSizeChanges;
    }

    public void setDismissWithWindowAnimation(boolean dismissWithAnimation) {
        this.mDismissWithWindowAnimation = dismissWithAnimation;
    }

    @Deprecated
    public void setEnableAddExtraWidth(boolean enabled) {
    }

    public void setEnableRenderThreadAnimation(boolean enabled) {
        COUIPopupMenuRootView cOUIPopupMenuRootView = this.mContentView;
        if (cOUIPopupMenuRootView != null) {
            cOUIPopupMenuRootView.setEnableRenderThreadAnimation(enabled);
        }
    }

    public void setGlobalOffset(int offsetX, int offsetY) {
        this.mGlobalOffsetX = offsetX;
        this.mGlobalOffsetY = offsetY;
    }

    public void setIsAdaptiveFontSize(boolean adaptive) {
        this.mIsAdaptiveFontSize = adaptive;
        DefaultAdapter defaultAdapter = this.mMainMenuAdapter;
        if (defaultAdapter != null) {
            defaultAdapter.setAdapterFontSize(this.mIsAdaptiveFontSize);
        }
        DefaultAdapter defaultAdapter2 = this.mSubMenuAdapter;
        if (defaultAdapter2 != null) {
            defaultAdapter2.setAdapterFontSize(this.mIsAdaptiveFontSize);
        }
    }

    public void setIsFixedFontSize(boolean fixed) {
        this.mIsFixedFontSize = fixed;
        DefaultAdapter defaultAdapter = this.mMainMenuAdapter;
        if (defaultAdapter != null) {
            defaultAdapter.setIsFixedFontSize(fixed);
        }
        DefaultAdapter defaultAdapter2 = this.mSubMenuAdapter;
        if (defaultAdapter2 != null) {
            defaultAdapter2.setIsFixedFontSize(this.mIsFixedFontSize);
        }
    }

    public void setItemList(List<PopupListItem> list) {
        setItemList(list, false);
    }

    @Deprecated
    public void setItemTextColor(ColorStateList colorStateList) {
    }

    @Deprecated
    public void setMaxShowItemCount(int maxItemCount) {
    }

    @Deprecated
    public void setMaxShowItemCountSubWindow(int maxItemCount) {
    }

    public void setMenuMaxWidth(int maxWidth) {
        this.mCustomMenuMaxWidth = maxWidth;
    }

    public void setMenuWidth(int width) {
        this.mCustomMenuWidth = width;
    }

    public void setNeedOffsetWhenSetWindowType(boolean needsOffset) {
        this.mNeedOffsetWhenSetWindowType = needsOffset;
    }

    public void setNonApplicationType(boolean nonApplicationType, boolean enableOffset) {
        this.mLocateHelper.useWindowBarrier(nonApplicationType);
        this.mLocateHelper.setCenterAlign(enableOffset);
    }

    @Deprecated
    public void setOffset(int horizontalOffset, int verticalOffset, int globalOffsetX, int globalOffsetY) {
        setGlobalOffset(-horizontalOffset, -verticalOffset);
    }

    @Override
    public void setOnDismissListener(PopupWindow.OnDismissListener onDismissListener) {
        this.mOnDismissListener = onDismissListener;
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        if (onItemClickListener == null) {
            COUILog.w(TAG, "set main menu item click listener = null. caller = " + Log.getStackTraceString(new Throwable()));
        }
        this.mOnMainMenuItemClickListener = onItemClickListener;
    }

    public void setPopupWindowLimitedRootView(View view) {
        this.mRootView = view;
    }

    @Deprecated
    public void setSelectItemColor(int color) {
    }

    @Deprecated
    public void setShowInBottomSheetDialog(boolean showInBottomSheet) {
    }

    @Deprecated
    public void setSubMenuClickListener(COUISubMenuClickListener cOUISubMenuClickListener) {
        this.mOnSubMenuItemClickListener = cOUISubMenuClickListener;
    }

    @Deprecated
    public void setSubMenuOffset(int offsetX, int offsetY) {
    }

    public void setUseBackgroundBlur(boolean useBackgroundBlur) {
        setUseBackgroundBlur(useBackgroundBlur, UIUtil.ANIM_LEVEL_SUPPORT_BLUR_MIN);
        if (useBackgroundBlur) {
            return;
        }
        refresh();
    }

    public void show() {
        View view = this.mAnchorView;
        if (view != null) {
            show(view);
        }
    }

    @Override
    public void showAsDropDown(View view, int xOffset, int yOffset, int gravity) {
    }

    @Deprecated
    public void showAtAbove(View view) {
        show(view, true);
    }

    @Deprecated
    public void showAtAboveOrBelow(View view) {
        show(view, true);
    }

    @Deprecated
    public void showEndOfAnchorViewStart(View view) {
    }

    @Deprecated
    public void superDismiss() {
        super.dismiss();
    }

    public void measurePopupWindow(DefaultAdapter defaultAdapter) {
        View view;
        int index_5;
        boolean isMainMenu = defaultAdapter == this.mMainMenuAdapter;
        PopupMenuLocateHelper popupMenuLocateHelper = this.mLocateHelper;
        int maxMainMenuHeight = isMainMenu ? popupMenuLocateHelper.getMaxMainMenuHeight() : popupMenuLocateHelper.getMaxSubMenuHeight();
        ArrayList arrayList = new ArrayList();
        int iMakeMeasureSpec = View.MeasureSpec.makeMeasureSpec(getMainMenuMaxWidth(), Integer.MIN_VALUE);
        int iMakeMeasureSpec2 = View.MeasureSpec.makeMeasureSpec(0, 0);
        int count = defaultAdapter.getCount();
        View view_2 = null;
        int index = 0;
        int index_2 = 0;
        int index_3 = 0;
        int index_4 = 0;
        int dividerHeight = 0;
        View view_3 = null;
        boolean flag = true;
        while (index < count) {
            if (DefaultAdapter.isDataIndex(index)) {
                if (defaultAdapter.getItemViewType(index) == 3) {
                    view = defaultAdapter.getView(index, view_2, this.mListViewUsedToMeasure);
                } else {
                    view_3 = defaultAdapter.getView(index, view_3, this.mListViewUsedToMeasure);
                    view = view_3;
                }
                if (view != null) {
                    if ((view.getLayoutParams() instanceof AbsListView.LayoutParams) && (index_5 = ((AbsListView.LayoutParams) view.getLayoutParams()).height) != -2) {
                        iMakeMeasureSpec2 = View.MeasureSpec.makeMeasureSpec(index_5, View.MeasureSpec.EXACTLY);
                    }
                    view.measure(iMakeMeasureSpec, iMakeMeasureSpec2);
                    int measuredWidth = view.getMeasuredWidth();
                    int measuredHeight = view.getMeasuredHeight();
                    if (measuredWidth > index_3) {
                        index_3 = measuredWidth;
                    }
                    if (flag && index_2 + measuredHeight > maxMainMenuHeight) {
                        index_2 -= dividerHeight;
                        flag = false;
                    }
                    if (flag) {
                        index_2 += measuredHeight;
                    }
                    index_4 += measuredHeight;
                    if (index == 0 || arrayList.isEmpty()) {
                        arrayList.add(Integer.valueOf(measuredHeight));
                    } else {
                        arrayList.add(Integer.valueOf(measuredHeight + ((Integer) arrayList.get(index - 1)).intValue()));
                    }
                }
            } else {
                dividerHeight = defaultAdapter.isGroupIndex(index) ? defaultAdapter.getDividerHeight(2) : defaultAdapter.getDividerHeight(1);
                if (flag) {
                    index_2 += dividerHeight;
                }
                index_4 += dividerHeight;
                if (index == 0 || arrayList.isEmpty()) {
                    arrayList.add(Integer.valueOf(dividerHeight));
                } else {
                    arrayList.add(Integer.valueOf(dividerHeight + ((Integer) arrayList.get(index - 1)).intValue()));
                }
            }
            index++;
            view_2 = null;
        }
        if (index_2 != 0) {
            maxMainMenuHeight = index_2;
        }
        if (isMainMenu) {
            this.mMainMenuWidth = Math.max(index_3, getMainMenuMinWidth());
            this.mMainMenuHeight = maxMainMenuHeight;
            ListView listView = this.mMainListView;
            if (listView instanceof COUITouchListView) {
                ((COUITouchListView) listView).setItemHeightMap(arrayList, index_4);
                return;
            }
            return;
        }
        this.mSubMenuWidth = this.mMainMenuWidth;
        this.mSubMenuHeight = maxMainMenuHeight;
        ListView listView2 = this.mSubListView;
        if (listView2 instanceof COUITouchListView) {
            ((COUITouchListView) listView2).setItemHeightMap(arrayList, index_4);
        }
    }

    @Deprecated
    public void setAdapter(BaseAdapter baseAdapter) {
        setAdapter(baseAdapter, true);
    }

    public void setItemList(List<PopupListItem> list, boolean isMainMenu) {
        if (!checkListNotNull(list) || !checkListElementsNotNull(list)) {
            Log.e(TAG, "Error! Item list must not be empty or null!");
            return;
        }
        this.mMainMenuItemList = list;
        if (this.mMainMenuAdapter == null) {
            this.mMainMenuAdapter = new DefaultAdapter(this.mContext, null);
        }
        setItemListInternal(this.mMainMenuItemList, this.mMainMenuAdapter, isMainMenu);
    }

    public void setSubMenuClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        if (onItemClickListener == null) {
            COUILog.w(TAG, "set sub menu item click listener = null. caller = " + Log.getStackTraceString(new Throwable()));
        }
        this.mOnSubMenuItemClickListener = onItemClickListener;
    }

    public void show(View view) {
        show(view, false);
    }

    public void setUseBackgroundBlur(boolean useBackgroundBlur, AnimLevel animLevel) {
        this.mSubMenuWrapper.initUseBackgroundBlur(useBackgroundBlur, animLevel);
        this.mMainMenuWrapper.initUseBackgroundBlur(useBackgroundBlur, animLevel);
    }

    public void show(View view, boolean showAboveAnchor) {
        show(view, showAboveAnchor, Integer.MIN_VALUE, Integer.MIN_VALUE);
    }

    public void show(View view, int offsetX, int offsetY) {
        show(view, false, offsetX, offsetY);
    }

    public void show(View view, boolean showAboveAnchor, int offsetX, int offsetY) {
        int index_2;
        WindowInsets rootWindowInsets;
        Context context = this.mContext;
        if (context == null) {
            Log.e(TAG, " The context of COUIPopupListWindow is null ");
            return;
        }
        if ((context instanceof Activity) && ((Activity) context).isFinishing()) {
            Log.e(TAG, " The context of COUIPopupListWindow is Finish ");
            return;
        }
        if (view == null || view.getContext() == null || view.getWindowToken() == null) {
            Log.e(TAG, " COUIPopupListWindow's anchor state is wrong ");
            return;
        }
        if (this.mMainMenuAdapter == null) {
            Log.e(TAG, "The MainMenuAdapter is null");
            return;
        }
        boolean flag = this.mIsDismissing && this.mAnchorView == view;
        this.mAnchorView = view;
        if (this.mNeedOffsetWhenSetWindowType && (rootWindowInsets = view.getRootWindowInsets()) != null) {
            getContentView().setTranslationX(-rootWindowInsets.getSystemWindowInsetLeft());
            COUILog.w(TAG, "mNeedOffsetWhenSetWindowType is true , offset the root view.");
        }
        int index = this.mMainMenuWidth;
        if (index != 0 && (index_2 = this.mMainMenuHeight) != 0) {
            measurePopupWindow();
            flag &= index == this.mMainMenuWidth && index_2 == this.mMainMenuHeight;
        }
        if (flag & (this.mReuseMenuWhenOffsetChanged || (this.mShowOffsetX == offsetX && this.mShowOffsetY == offsetY)) & (!this.mDismissWithWindowAnimation)) {
            DefaultAdapter defaultAdapter = this.mMainMenuAdapter;
            if (defaultAdapter != null) {
                defaultAdapter.notifyDataSetChanged();
            }
            DefaultAdapter defaultAdapter2 = this.mSubMenuAdapter;
            if (defaultAdapter2 != null) {
                defaultAdapter2.notifyDataSetChanged();
            }
            reuseMainMenu(showAboveAnchor);
            setWidth(this.mLocateHelper.mApplicationWindow.width());
            setHeight(this.mLocateHelper.mApplicationWindow.height());
            this.mContentView.showMainMenu();
        } else {
            if (super.isShowing()) {
                forceDismiss();
            }
            this.mShowOffsetX = offsetX;
            this.mShowOffsetY = offsetY;
            prepareShowMainMenu(view, offsetX, offsetY, showAboveAnchor);
            setWidth(this.mLocateHelper.mApplicationWindow.width());
            setHeight(this.mLocateHelper.mApplicationWindow.height());
            super.showAtLocation(view.getRootView(), 0, 0, 0);
        }
        view.getRootView().addOnLayoutChangeListener(this.mMenuDismissWhenRootChange);
        setAnchorHoveredState(true, view);
    }

    @Deprecated
    public void measurePopupWindow(boolean hovered) {
        measurePopupWindow();
    }
}
