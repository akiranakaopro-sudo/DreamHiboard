package com.coui.appcompat.tablayout;

import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.PathInterpolator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.coui.appcompat.animation.COUIMoveEaseInterpolator;
import com.coui.appcompat.tablayout.COUITabLayout;
import com.coui.appcompat.viewpager.COUIViewPager2;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;


public final class COUITabLayoutMediator {
    private static final int ALL_CUSTOM_TAB_VIEW = 2;
    private static final int INVALID_VALUE = -1;
    private static final int NO_CUSTOM_TAB_VIEW = 0;
    private static final int PART_CUSTOM_TAB_VIEW = 1;
    private static final String SELECT_TAB_NAME = "TabLayout.selectTab(TabLayout.Tab, boolean)";
    private static final String SET_SCROLL_POSITION_NAME = "TabLayout.setScrollPosition(int, float, boolean, boolean)";
    private static Method sSelectTab;
    private static Method sSetScrollPosition;
    private RecyclerView.Adapter mAdapter;
    private boolean mAttached;
    private final boolean mAutoRefresh;
    private int mCustomTabViewType;
    private int mLayoutResAll;
    private SparseIntArray mLayoutResIdMap;
    private final OnConfigureTabCallback mOnConfigureTabCallback;
    private TabLayoutOnPageChangeCallback mOnPageChangeCallback;
    private COUITabLayout.OnTabSelectedListener mOnTabSelectedListener;
    private RecyclerView.AdapterDataObserver mPagerAdapterObserver;
    private Map<Integer, String> mTabContentDescMap;
    private final COUITabLayout mTabLayout;
    private final COUIViewPager2 mViewPager;

    public interface OnConfigureTabCallback {
        void onConfigureTab(COUITab cOUITab, int index);
    }

    public class PagerAdapterObserver extends RecyclerView.AdapterDataObserver {
        public PagerAdapterObserver() {
        }

        @Override
        public void onChanged() {
            COUITabLayoutMediator.this.populateTabsFromPagerAdapter();
        }

        @Override
        public void onItemRangeChanged(int index, int index_2) {
            COUITabLayoutMediator.this.populateTabsFromPagerAdapter();
        }

        @Override
        public void onItemRangeInserted(int index, int index_2) {
            COUITabLayoutMediator.this.populateTabsFromPagerAdapter();
        }

        @Override
        public void onItemRangeMoved(int index, int index_2, int index_3) {
            COUITabLayoutMediator.this.populateTabsFromPagerAdapter();
        }

        @Override
        public void onItemRangeRemoved(int index, int index_2) {
            COUITabLayoutMediator.this.populateTabsFromPagerAdapter();
        }

        @Override
        public void onItemRangeChanged(int index, int index_2, Object obj) {
            COUITabLayoutMediator.this.populateTabsFromPagerAdapter();
        }
    }

    public static class TabLayoutOnPageChangeCallback extends ViewPager2.OnPageChangeCallback {
        private int mPreviousScrollState;
        private int mScrollState;
        private final WeakReference<COUITabLayout> mTabLayoutRef;
        private final WeakReference<COUIViewPager2> mViewPager2Ref;

        public TabLayoutOnPageChangeCallback(COUITabLayout cOUITabLayout, COUIViewPager2 cOUIViewPager2) {
            this.mTabLayoutRef = new WeakReference<>(cOUITabLayout);
            this.mViewPager2Ref = new WeakReference<>(cOUIViewPager2);
            reset();
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            this.mPreviousScrollState = this.mScrollState;
            this.mScrollState = state;
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            COUIViewPager2 cOUIViewPager2 = this.mViewPager2Ref.get();
            COUITabLayout cOUITabLayout = this.mTabLayoutRef.get();
            if (cOUITabLayout == null || cOUIViewPager2 == null || cOUIViewPager2.isFakeDragging()) {
                return;
            }
            int index = this.mScrollState;
            boolean flag = true;
            boolean flag_2 = index != 2 || this.mPreviousScrollState == 1;
            if (index == 2 && this.mPreviousScrollState == 0) {
                flag = false;
            }
            if (index == 0 && this.mPreviousScrollState == 0 && positionOffset != 0.0f) {
                ((RecyclerView) cOUIViewPager2.getChildAt(0)).scrollBy(positionOffsetPixels, 0);
                cOUITabLayout.selectTab(cOUITabLayout.getTabAt(position));
            } else {
                COUITabLayoutMediator.setScrollPosition(cOUITabLayout, position, positionOffset, flag_2, flag);
            }
            if (positionOffset != 0.0f || position == cOUITabLayout.getSelectedTabPosition()) {
                return;
            }
            cOUITabLayout.selectTab(cOUITabLayout.getTabAt(position));
        }

        @Override
        public void onPageSelected(int position) {
            COUITabLayout cOUITabLayout = this.mTabLayoutRef.get();
            if (cOUITabLayout == null || cOUITabLayout.getSelectedTabPosition() == position || position >= cOUITabLayout.getTabCount()) {
                return;
            }
            int index = this.mScrollState;
            COUITabLayoutMediator.selectTab(cOUITabLayout, cOUITabLayout.getTabAt(position), index == 0 || (index == 2 && this.mPreviousScrollState == 0));
        }

        public void reset() {
            this.mScrollState = 0;
            this.mPreviousScrollState = 0;
        }
    }

    public static class ViewPagerOnTabSelectedListener implements COUITabLayout.OnTabSelectedListener {
        private int[] mScrollDistanceAndDuration = new int[2];
        private PathInterpolator mScrollPathInterpolator = new COUIMoveEaseInterpolator();
        private final COUIViewPager2 mViewPager;

        public ViewPagerOnTabSelectedListener(COUIViewPager2 cOUIViewPager2) {
            this.mViewPager = cOUIViewPager2;
        }

        private void getScrollDistanceAndDuration(LinearLayoutManager linearLayoutManager, RecyclerView recyclerView, int index) {
            View viewFindViewByPosition;
            int[] iArr = this.mScrollDistanceAndDuration;
            iArr[0] = 0;
            iArr[1] = 0;
            int iFindFirstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition();
            if (iFindFirstVisibleItemPosition == -1 || (viewFindViewByPosition = linearLayoutManager.findViewByPosition(iFindFirstVisibleItemPosition)) == null) {
                return;
            }
            int leftDecorationWidth = linearLayoutManager.getLeftDecorationWidth(viewFindViewByPosition);
            int rightDecorationWidth = linearLayoutManager.getRightDecorationWidth(viewFindViewByPosition);
            ViewGroup.LayoutParams layoutParams = viewFindViewByPosition.getLayoutParams();
            if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) layoutParams;
                leftDecorationWidth += marginLayoutParams.leftMargin;
                rightDecorationWidth += marginLayoutParams.rightMargin;
            }
            int width = viewFindViewByPosition.getWidth() + leftDecorationWidth + rightDecorationWidth;
            int left = (viewFindViewByPosition.getLeft() - leftDecorationWidth) - recyclerView.getPaddingLeft();
            if (linearLayoutManager.getLayoutDirection() == 1) {
                width = -width;
            }
            int index_2 = ((index - iFindFirstVisibleItemPosition) * width) + left;
            int[] iArr2 = this.mScrollDistanceAndDuration;
            iArr2[0] = index_2;
            iArr2[1] = getScrollDuration(Math.abs(index_2), Math.abs(width));
        }

        private int getScrollDuration(int index, int index_2) {
            float value = index_2 * 3;
            if (index <= index_2) {
                return 350;
            }
            float value_2 = index;
            if (value_2 > value) {
                return 650;
            }
            return (int) (((value_2 / value) * 300.0f) + 350.0f);
        }

        @Override
        public void onTabReselected(COUITab cOUITab) {
        }

        @Override
        public void onTabSelected(COUITab cOUITab) {
            RecyclerView.Adapter adapter;
            if (cOUITab.mView.getSelectedByClick() && (adapter = this.mViewPager.getAdapter()) != null && adapter.getItemCount() > 0) {
                int iMin = Math.min(Math.max(cOUITab.getPosition(), 0), adapter.getItemCount() - 1);
                if (this.mViewPager.getChildAt(0) instanceof RecyclerView) {
                    this.mViewPager.setCurrentItemWithoutAnimation(iMin);
                    RecyclerView recyclerView = (RecyclerView) this.mViewPager.getChildAt(0);
                    LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if (linearLayoutManager == null) {
                        return;
                    }
                    getScrollDistanceAndDuration(linearLayoutManager, recyclerView, iMin);
                    this.mViewPager.beginFakeDrag();
                    int[] iArr = this.mScrollDistanceAndDuration;
                    recyclerView.smoothScrollBy(iArr[0], 0, this.mScrollPathInterpolator, iArr[1]);
                }
            }
        }

        @Override
        public void onTabUnselected(COUITab cOUITab) {
        }
    }

    static {
        try {
            Class cls = Boolean.TYPE;
            Method declaredMethod = COUITabLayout.class.getDeclaredMethod("setScrollPosition", Integer.TYPE, Float.TYPE, cls, cls);
            sSetScrollPosition = declaredMethod;
            declaredMethod.setAccessible(true);
            Method declaredMethod2 = COUITabLayout.class.getDeclaredMethod("selectTab", COUITab.class, cls);
            sSelectTab = declaredMethod2;
            declaredMethod2.setAccessible(true);
        } catch (NoSuchMethodException unused) {
            throw new IllegalStateException("Can't reflect into method TabLayout.setScrollPosition(int, float, boolean, boolean)");
        }
    }

    public COUITabLayoutMediator(COUITabLayout cOUITabLayout, COUIViewPager2 cOUIViewPager2, OnConfigureTabCallback onConfigureTabCallback) {
        this(cOUITabLayout, cOUIViewPager2, true, onConfigureTabCallback);
    }

    public static void selectTab(COUITabLayout cOUITabLayout, COUITab cOUITab, boolean flag) {
        try {
            Method method = sSelectTab;
            if (method != null) {
                method.invoke(cOUITabLayout, cOUITab, Boolean.valueOf(flag));
            } else {
                throwMethodNotFound(SELECT_TAB_NAME);
            }
        } catch (Exception unused) {
            throwInvokeFailed(SELECT_TAB_NAME);
        }
    }

    public static void setScrollPosition(COUITabLayout cOUITabLayout, int index, float value, boolean flag, boolean flag_2) {
        try {
            Method method = sSetScrollPosition;
            if (method != null) {
                method.invoke(cOUITabLayout, Integer.valueOf(index), Float.valueOf(value), Boolean.valueOf(flag), Boolean.valueOf(flag_2));
            } else {
                throwMethodNotFound(SET_SCROLL_POSITION_NAME);
            }
        } catch (Exception unused) {
            throwInvokeFailed(SET_SCROLL_POSITION_NAME);
        }
    }

    private static void throwInvokeFailed(String str) {
        throw new IllegalStateException("Couldn't invoke method " + str);
    }

    private static void throwMethodNotFound(String str) {
        throw new IllegalStateException("Method " + str + " not found");
    }

    public void attach() {
        if (this.mAttached) {
            throw new IllegalStateException("TabLayoutMediator is already attached");
        }
        RecyclerView.Adapter adapter = this.mViewPager.getAdapter();
        this.mAdapter = adapter;
        if (adapter == null) {
            throw new IllegalStateException("TabLayoutMediator attached before ViewPager2 has an adapter");
        }
        this.mAttached = true;
        TabLayoutOnPageChangeCallback tabLayoutOnPageChangeCallback = new TabLayoutOnPageChangeCallback(this.mTabLayout, this.mViewPager);
        this.mOnPageChangeCallback = tabLayoutOnPageChangeCallback;
        this.mViewPager.registerOnPageChangeCallback(tabLayoutOnPageChangeCallback);
        ViewPagerOnTabSelectedListener viewPagerOnTabSelectedListener = new ViewPagerOnTabSelectedListener(this.mViewPager);
        this.mOnTabSelectedListener = viewPagerOnTabSelectedListener;
        this.mTabLayout.addOnTabSelectedListener(viewPagerOnTabSelectedListener);
        if (this.mAutoRefresh) {
            PagerAdapterObserver pagerAdapterObserver = new PagerAdapterObserver();
            this.mPagerAdapterObserver = pagerAdapterObserver;
            this.mAdapter.registerAdapterDataObserver(pagerAdapterObserver);
        }
        populateTabsFromPagerAdapter();
        this.mTabLayout.setScrollPosition(this.mViewPager.getCurrentItem(), 0.0f, true);
    }

    public void detach() {
        if (this.mAutoRefresh) {
            this.mAdapter.unregisterAdapterDataObserver(this.mPagerAdapterObserver);
        }
        this.mTabLayout.removeOnTabSelectedListener(this.mOnTabSelectedListener);
        this.mViewPager.unregisterOnPageChangeCallback(this.mOnPageChangeCallback);
        this.mPagerAdapterObserver = null;
        this.mOnTabSelectedListener = null;
        this.mOnPageChangeCallback = null;
        this.mAttached = false;
    }

    public void populateTabsFromPagerAdapter() {
        this.mTabLayout.removeAllTabs();
        RecyclerView.Adapter hVar = this.mAdapter;
        if (hVar != null) {
            int itemCount = hVar.getItemCount();
            for (int index = 0; index < itemCount; index++) {
                COUITab cOUITabNewTab = this.mTabLayout.newTab();
                int index_2 = this.mCustomTabViewType;
                if (index_2 != 1) {
                    if (index_2 == 2) {
                        cOUITabNewTab.setCustomView(this.mLayoutResAll);
                    }
                } else if (this.mLayoutResIdMap.get(index, -1) != -1) {
                    cOUITabNewTab.setCustomView(this.mLayoutResIdMap.get(index));
                }
                String str = this.mTabContentDescMap.get(Integer.valueOf(index));
                if (str != null) {
                    cOUITabNewTab.setContentDescription(str);
                }
                this.mOnConfigureTabCallback.onConfigureTab(cOUITabNewTab, index);
                this.mTabLayout.addTab(cOUITabNewTab, false);
            }
            if (itemCount > 0) {
                int currentItem = this.mViewPager.getCurrentItem();
                COUITab tabAt = this.mTabLayout.getTabAt(currentItem);
                if (currentItem == this.mTabLayout.getSelectedTabPosition() || tabAt == null) {
                    return;
                }
                tabAt.select();
            }
        }
    }

    public void setTabCustomView(int tabCustomView) {
        this.mCustomTabViewType = ALL_CUSTOM_TAB_VIEW;
        this.mLayoutResAll = tabCustomView;
        if (this.mAttached) {
            populateTabsFromPagerAdapter();
            this.mTabLayout.setScrollPosition(this.mViewPager.getCurrentItem(), 0.0f, true);
        }
    }

    public COUITabLayoutMediator(COUITabLayout cOUITabLayout, COUIViewPager2 cOUIViewPager2, boolean flag, OnConfigureTabCallback onConfigureTabCallback) {
        this.mTabLayout = cOUITabLayout;
        cOUITabLayout.setUpdateindicatorposition(true);
        this.mViewPager = cOUIViewPager2;
        this.mAutoRefresh = flag;
        this.mOnConfigureTabCallback = onConfigureTabCallback;
        this.mCustomTabViewType = NO_CUSTOM_TAB_VIEW;
        this.mLayoutResIdMap = new SparseIntArray();
        this.mTabContentDescMap = new HashMap();
    }

    public void setTabCustomView(int index, int index_2) {
        setTabCustomView(index, index_2, null);
    }

    public void setTabCustomView(int index, int index_2, String str) {
        this.mCustomTabViewType = PART_CUSTOM_TAB_VIEW;
        this.mLayoutResIdMap.put(index_2, index);
        this.mTabContentDescMap.put(Integer.valueOf(index_2), str);
        if (this.mAttached) {
            populateTabsFromPagerAdapter();
            this.mTabLayout.setScrollPosition(this.mViewPager.getCurrentItem(), 0.0f, true);
        }
    }
}







