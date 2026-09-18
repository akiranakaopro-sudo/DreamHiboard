package com.coui.appcompat.tablayout;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import androidx.core.view.ViewCompat;
import com.coui.appcompat.animation.COUIEaseInterpolator;
import com.coui.appcompat.contextutil.COUIContextUtil;
import com.coui.appcompat.hapticfeedback.COUIHapticFeedbackConstants;
import com.coui.appcompat.scrollview.COUIHorizontalScrollView;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import androidx.core.util.Pools.Pool;
import androidx.core.util.Pools.SynchronizedPool;
import androidx.viewpager.widget.ViewPager;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import com.coui.appcompat.R;


public class COUITabLayout extends COUIHorizontalScrollView {
    private static final int ANIMATION_DURATION = 300;
    protected static final int DEFAULT_GAP_TEXT_ICON = 8;
    private static final int DEFAULT_HEIGHT = 48;
    private static final int DEFAULT_HEIGHT_WITH_TEXT_ICON = 72;
    private static final float DEFAULT_MAXIMUM_WIDTH_RATIO = 0.7f;
    public static final int DEFAULT_MIN_INDICATOR = 32;
    private static final int FIFTY = 50;
    public static final int GRAVITY_CENTER = 1;
    public static final int GRAVITY_FILL = 0;
    private static final int HUNDRED_FIFTY = 150;
    public static final int INVALID_WIDTH = -1;
    private static final String MEDIUM_FONT = "sans-serif-medium";
    public static final int MODE_FIXED = 1;
    public static final int MODE_SCROLLABLE = 0;
    public static final int MOTION_NON_ADJACENT_OFFSET = 24;
    public static final float ONE = 1.0f;
    private static final float POINT_FIVE = 0.5f;
    public static final int PRESS_RIPPLE_CORNER_RADIUS = 8;
    private static final String REGULAR_FONT = "sans-serif";
    public static final int SCROLL_STATE_DRAGGING = 1;
    public static final int SCROLL_STATE_IDLE = 0;
    public static final int SCROLL_STATE_SETTLING = 2;
    private static final Pool TAB_POOL = new SynchronizedPool(16);
    private static final int THREE_HUNDRED = 300;
    public static final float ZERO = 0.0f;
    private AdapterChangeListener mAdapterChangeListener;
    private int mBottomDividerColor;
    private boolean mBottomDividerEnabled;
    private int mButtonMarginEnd;
    private ArrayList<PrivateButton> mButtons;
    private OnTabSelectedListener mCurrentVpSelectedListener;
    private float mDefaultIndicatorRatio;
    private float mDefaultTabTextSize;
    protected int mDotHorizontalOffset;
    protected int mDotVerticalOffsetFromNumberRed;
    protected int mDotVerticalOffsetFromOnlyRed;
    protected boolean mEnableVibrator;
    private ArgbEvaluator mEvaluator;
    private int mIndicatorPadding;
    private boolean mIsUpdateindicatorposition;
    private float mLastOffset;
    private int mLongTextViewHeight;
    private int mMode;
    protected boolean mNeedAdjust;
    protected int mNormalTextColor;
    protected Typeface mNormalTypeface;
    private int mOriginalRequestedTabMaxWidth;
    private int mOriginalRequestedTabMinWidth;
    private TabLayoutOnPageChangeListener mPageChangeListener;
    private androidx.viewpager.widget.PagerAdapter mPagerAdapter;
    private DataSetObserver mPagerAdapterObserver;
    protected int mRequestedTabMaxWidth;
    private int mRequestedTabMinWidth;
    private int mResizeHeight;
    private ValueAnimator mScrollAnimator;
    private int mSelectedIndicatorColor;
    private int mSelectedIndicatorDisableColor;
    private OnTabSelectedListener mSelectedListener;
    private final ArrayList<OnTabSelectedListener> mSelectedListeners;
    private int mSelectedPosition;
    protected COUITab mSelectedTab;
    protected int mSelectedTextColor;
    protected Typeface mSelectedTypeface;
    private boolean mSetupViewPagerImplicitly;
    private int mStyle;
    protected boolean mTabAlreadyMeasure;
    protected final int mTabBackgroundResId;

    @Deprecated
    private int mTabGravity;
    private int mTabMinDivider;
    private int mTabMinMargin;
    protected int mTabPaddingBottom;
    protected int mTabPaddingEnd;
    protected int mTabPaddingStart;
    protected int mTabPaddingTop;
    protected final COUISlidingTabStrip mTabStrip;
    private int mTabTextAppearance;
    protected ColorStateList mTabTextColors;
    private int mTabTextDisabledColor;
    private float mTabTextSize;
    private Typeface mTabTextTypeFace;
    private final Pool mTabViewPool;
    private final ArrayList<COUITab> mTabs;
    private int mTextColorBlue;
    private int mTextColorGreen;
    private int mTextColorRed;
    private ViewPager mViewPager;


    public class AnonymousClass1 implements ValueAnimator.AnimatorUpdateListener {
        public AnonymousClass1() {
        }

        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            COUITabLayout.this.scrollTo(((Integer) valueAnimator.getAnimatedValue()).intValue(), 0);
        }
    }

    public class AdapterChangeListener implements ViewPager.OnAdapterChangeListener {
        private boolean mAutoRefresh;

        public AdapterChangeListener() {
        }

        @Override
        public void onAdapterChanged(ViewPager bVar, androidx.viewpager.widget.PagerAdapter aVar, androidx.viewpager.widget.PagerAdapter aVar2) {
            if (COUITabLayout.this.mViewPager == bVar) {
                COUITabLayout.this.setPagerAdapter(aVar2, this.mAutoRefresh);
            }
        }

        public void setAutoRefresh(boolean autoRefresh) {
            this.mAutoRefresh = autoRefresh;
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface Mode {
    }

    public interface OnTabSelectedListener {
        void onTabReselected(COUITab cOUITab);

        void onTabSelected(COUITab cOUITab);

        void onTabUnselected(COUITab cOUITab);
    }

    public class PagerAdapterObserver extends DataSetObserver {
        public PagerAdapterObserver() {
        }

        @Override
        public void onChanged() {
            COUITabLayout.this.populateFromPagerAdapter();
        }

        @Override
        public void onInvalidated() {
            COUITabLayout.this.populateFromPagerAdapter();
        }
    }

    public class PrivateButton {
        View.OnClickListener mButtonClicklistener;
        Drawable mButtonDrawable;

        public PrivateButton(Drawable drawable, View.OnClickListener onClickListener) {
            this.mButtonDrawable = drawable;
            this.mButtonClicklistener = onClickListener;
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface TabGravity {
    }

    public static class TabLayoutOnPageChangeListener implements ViewPager.OnPageChangeListener {
        private int mPreviousScrollState;
        private int mScrollState;
        private final WeakReference<COUITabLayout> mTabLayoutRef;

        public TabLayoutOnPageChangeListener(COUITabLayout cOUITabLayout) {
            this.mTabLayoutRef = new WeakReference<>(cOUITabLayout);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            this.mPreviousScrollState = this.mScrollState;
            this.mScrollState = state;
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            COUITabLayout cOUITabLayout = this.mTabLayoutRef.get();
            if (cOUITabLayout != null) {
                int index = this.mScrollState;
                cOUITabLayout.setScrollPosition(position, positionOffset, index != SCROLL_STATE_SETTLING || this.mPreviousScrollState == SCROLL_STATE_DRAGGING, (index == SCROLL_STATE_SETTLING && this.mPreviousScrollState == SCROLL_STATE_IDLE) ? false : true);
            }
        }

        @Override
        public void onPageSelected(int position) {
            COUITabLayout cOUITabLayout = this.mTabLayoutRef.get();
            if (cOUITabLayout == null || cOUITabLayout.getSelectedTabPosition() == position || position >= cOUITabLayout.getTabCount()) {
                return;
            }
            int index = this.mScrollState;
            cOUITabLayout.selectTab(cOUITabLayout.getTabAt(position), index == SCROLL_STATE_IDLE || (index == SCROLL_STATE_SETTLING && this.mPreviousScrollState == SCROLL_STATE_IDLE));
        }

        public void reset() {
            this.mPreviousScrollState = SCROLL_STATE_IDLE;
            this.mScrollState = SCROLL_STATE_IDLE;
        }
    }

    public static class ViewPagerOnTabSelectedListener implements OnTabSelectedListener {
        private final ViewPager mViewPager;

        public ViewPagerOnTabSelectedListener(ViewPager bVar) {
            this.mViewPager = bVar;
        }

        @Override
        public void onTabReselected(COUITab cOUITab) {
        }

        @Override
        public void onTabSelected(COUITab cOUITab) {
            this.mViewPager.setCurrentItem(cOUITab.getPosition());
        }

        @Override
        public void onTabUnselected(COUITab cOUITab) {
        }
    }

    public COUITabLayout(Context context) {
        this(context, null);
    }

    private void addTabFromItemView(COUITabItem cOUITabItem) {
        COUITab cOUITabNewTab = newTab();
        CharSequence charSequence = cOUITabItem.mText;
        if (charSequence != null) {
            cOUITabNewTab.setText(charSequence);
        }
        Drawable drawable = cOUITabItem.mIcon;
        if (drawable != null) {
            cOUITabNewTab.setIcon(drawable);
        }
        int index = cOUITabItem.mCustomLayout;
        if (index != 0) {
            cOUITabNewTab.setCustomView(index);
        }
        if (!TextUtils.isEmpty(cOUITabItem.getContentDescription())) {
            cOUITabNewTab.setContentDescription(cOUITabItem.getContentDescription());
        }
        addTab(cOUITabNewTab);
    }

    private void addTabView(COUITab cOUITab) {
        this.mTabStrip.addView(cOUITab.mView, cOUITab.getPosition(), createLayoutParamsForTabs());
    }

    private void addViewInternal(View view) {
        if (!(view instanceof COUITabItem)) {
            throw new IllegalArgumentException("Only TabItem instances can be added to TabLayout");
        }
        addTabFromItemView((COUITabItem) view);
    }

    private void animateToTab(int index) {
        if (index == -1) {
            return;
        }
        if (getWindowToken() == null || !ViewCompat.isLaidOut(this) || this.mTabStrip.childrenNeedLayout()) {
            setScrollPosition(index, 0.0f, true);
            return;
        }
        int scrollX = getScrollX();
        int iCalculateScrollXForTab = calculateScrollXForTab(index, 0.0f);
        if (scrollX != iCalculateScrollXForTab) {
            ensureScrollAnimator();
            this.mScrollAnimator.setIntValues(scrollX, iCalculateScrollXForTab);
            this.mScrollAnimator.start();
        }
        this.mTabStrip.animateIndicatorToPosition(index, 300);
    }

    private void applyModeAndGravity() {
        updateTabViews(true);
    }

    private int calculateScrollXForTab(int index, float value) {
        int width;
        int width2 = 0;
        if (getWidth() == 0) {
            return 0;
        }
        View childAt = this.mTabStrip.getChildAt(index);
        int index_2 = index + 1;
        View childAt2 = index_2 < this.mTabStrip.getChildCount() ? this.mTabStrip.getChildAt(index_2) : null;
        if (childAt != null) {
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) childAt.getLayoutParams();
            width = childAt.getWidth() + layoutParams.leftMargin + layoutParams.rightMargin;
        } else {
            width = 0;
        }
        if (childAt2 != null) {
            LinearLayout.LayoutParams layoutParams_2 = (LinearLayout.LayoutParams) childAt2.getLayoutParams();
            width2 = layoutParams_2.rightMargin + childAt2.getWidth() + layoutParams_2.leftMargin;
        }
        int width3 = (width / 2) - (getWidth() / 2);
        if (childAt != null) {
            LinearLayout.LayoutParams layoutParams_3 = (LinearLayout.LayoutParams) childAt.getLayoutParams();
            width3 += ViewCompat.getLayoutDirection(this) == 0 ? (childAt.getLeft() - layoutParams_3.leftMargin) + (getPaddingLeft() / 2) + (getPaddingRight() / 2) : ((childAt.getRight() + layoutParams_3.rightMargin) - (getPaddingLeft() / 2)) - (getPaddingRight() / 2);
        }
        int index_3 = (int) ((width + width2) * 0.5f * value);
        return ViewCompat.getLayoutDirection(this) == 0 ? width3 + index_3 : width3 - index_3;
    }

    private void configureTab(COUITab cOUITab, int index) {
        cOUITab.setPosition(index);
        this.mTabs.add(index, cOUITab);
        int size = this.mTabs.size();
        while (true) {
            index++;
            if (index >= size) {
                return;
            } else {
                this.mTabs.get(index).setPosition(index);
            }
        }
    }

    private static ColorStateList createColorStateList(int index, int index_2, int index_3) {
        return new ColorStateList(new int[][]{new int[]{16842913, 16842910}, new int[]{-16842913, -16842910}, HorizontalScrollView.EMPTY_STATE_SET}, new int[]{index_3, index_2, index});
    }

    private LinearLayout.LayoutParams createLayoutParamsForTabs() {
        return new LinearLayout.LayoutParams(1, -1);
    }

    private COUITabView createTabView(COUITab cOUITab) {
        Pool eVar = this.mTabViewPool;
        COUITabView cOUITabView = eVar != null ? (COUITabView) eVar.acquire() : null;
        if (cOUITabView == null) {
            cOUITabView = new COUITabView(getContext(), this);
        }
        cOUITabView.setTab(cOUITab);
        cOUITabView.setFocusable(true);
        cOUITabView.setMinimumWidth(getTabMinWidth());
        cOUITabView.setEnabled(isEnabled());
        return cOUITabView;
    }

    private void dispatchTabReselected(COUITab cOUITab) {
        for (int size = this.mSelectedListeners.size() - 1; size >= 0; size--) {
            this.mSelectedListeners.get(size).onTabReselected(cOUITab);
        }
    }

    private void dispatchTabSelected(COUITab cOUITab) {
        for (int size = this.mSelectedListeners.size() - 1; size >= 0; size--) {
            this.mSelectedListeners.get(size).onTabSelected(cOUITab);
        }
    }

    private void dispatchTabUnselected(COUITab cOUITab) {
        for (int size = this.mSelectedListeners.size() - 1; size >= 0; size--) {
            this.mSelectedListeners.get(size).onTabUnselected(cOUITab);
        }
    }

    private void drawButton(Canvas canvas) {
        int width;
        int scrollX;
        int width2;
        int width3;
        int scrollX2;
        int dimensionPixelSize = getResources().getDimensionPixelSize(R.dimen.coui_tab_layout_button_width);
        if (this.mButtons.size() == 1) {
            Drawable drawable = this.mButtons.get(0).mButtonDrawable;
            int dimensionPixelSize2 = this.mButtonMarginEnd;
            if (dimensionPixelSize2 == -1) {
                dimensionPixelSize2 = getResources().getDimensionPixelSize(R.dimen.coui_tab_layout_button_default_horizontal_margin);
            }
            if (ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL) {
                width2 = getScrollX() + dimensionPixelSize2;
                width3 = dimensionPixelSize + dimensionPixelSize2;
                scrollX2 = getScrollX();
            } else {
                width2 = (getWidth() - (dimensionPixelSize + dimensionPixelSize2)) + getScrollX();
                width3 = getWidth() - dimensionPixelSize2;
                scrollX2 = getScrollX();
            }
            int index_2 = width3 + scrollX2;
            int height = getHeight() / 2;
            Resources resources = getResources();
            int index_3 = R.dimen.coui_tab_layout_button_default_vertical_margin;
            drawable.setBounds(width2, height - resources.getDimensionPixelSize(index_3), index_2, (getHeight() / 2) + getResources().getDimensionPixelSize(index_3));
            drawable.draw(canvas);
            return;
        }
        if (this.mButtons.size() >= 2) {
            for (int index = 0; index < this.mButtons.size(); index++) {
                int dimensionPixelSize3 = this.mButtonMarginEnd;
                if (dimensionPixelSize3 == -1) {
                    dimensionPixelSize3 = getResources().getDimensionPixelSize(R.dimen.coui_tab_layout_multi_button_default_horizontal_margin);
                }
                if (ViewCompat.getLayoutDirection(this) == 1) {
                    scrollX = dimensionPixelSize3 + (getResources().getDimensionPixelSize(R.dimen.coui_tab_layout_multi_button_default_padding) * index);
                    width = getScrollX();
                } else {
                    width = getWidth() - ((dimensionPixelSize3 + dimensionPixelSize) + (getResources().getDimensionPixelSize(R.dimen.coui_tab_layout_multi_button_default_padding) * index));
                    scrollX = getScrollX();
                }
                int index_4 = scrollX + width;
                Drawable doubleValue = this.mButtons.get(index).mButtonDrawable;
                int height2 = getHeight() / 2;
                Resources resources2 = getResources();
                int index_5 = R.dimen.coui_tab_layout_button_default_vertical_margin;
                doubleValue.setBounds(index_4, height2 - resources2.getDimensionPixelSize(index_5), index_4 + dimensionPixelSize, (getHeight() / 2) + getResources().getDimensionPixelSize(index_5));
                doubleValue.draw(canvas);
            }
        }
    }

    private void ensureScrollAnimator() {
        if (this.mScrollAnimator == null) {
            ValueAnimator valueAnimator = new ValueAnimator();
            this.mScrollAnimator = valueAnimator;
            valueAnimator.setInterpolator(new COUIEaseInterpolator());
            this.mScrollAnimator.setDuration(ANIMATION_DURATION);
            this.mScrollAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator_2) {
                    COUITabLayout.this.scrollTo(((Integer) valueAnimator_2.getAnimatedValue()).intValue(), 0);
                }
            });
        }
    }

    private int getDefaultHeight() {
        int size = this.mTabs.size();
        for (int index = 0; index < size; index++) {
            COUITab cOUITab = this.mTabs.get(index);
            if (cOUITab != null && cOUITab.getIcon() != null && !TextUtils.isEmpty(cOUITab.getText())) {
                return DEFAULT_HEIGHT_WITH_TEXT_ICON;
            }
        }
        return DEFAULT_HEIGHT;
    }

    private float getScrollPosition() {
        return this.mTabStrip.getIndicatorPosition();
    }

    private int getTabMinWidth() {
        return 0;
    }

    private int getTabScrollRange() {
        return Math.max(0, ((this.mTabStrip.getWidth() - getWidth()) - getPaddingLeft()) - getPaddingRight());
    }

    private void removeTabViewAt(int index) {
        COUITabView cOUITabView = (COUITabView) this.mTabStrip.getChildAt(index);
        this.mTabStrip.removeViewAt(index);
        if (cOUITabView != null) {
            cOUITabView.reset();
            this.mTabViewPool.release(cOUITabView);
        }
        requestLayout();
    }

    private void setSelectedTabView(int selectedTabView) {
        int childCount = this.mTabStrip.getChildCount();
        if (selectedTabView < childCount) {
            int index = 0;
            while (index < childCount) {
                this.mTabStrip.getChildAt(index).setSelected(index == selectedTabView);
                index++;
            }
        }
    }

    private void updateAllTabs() {
        int size = this.mTabs.size();
        for (int index = 0; index < size; index++) {
            this.mTabs.get(index).updateView();
        }
    }

    private void updateTextColor() {
        this.mNormalTextColor = this.mTabTextColors.getDefaultColor();
        int colorForState = this.mTabTextColors.getColorForState(new int[]{16842910, 16842913}, COUIContextUtil.getAttrColor(getContext(), R.attr.couiColorPrimaryText, 0));
        this.mSelectedTextColor = colorForState;
        this.mTextColorRed = Math.abs(Color.red(colorForState) - Color.red(this.mNormalTextColor));
        this.mTextColorGreen = Math.abs(Color.green(this.mSelectedTextColor) - Color.green(this.mNormalTextColor));
        this.mTextColorBlue = Math.abs(Color.blue(this.mSelectedTextColor) - Color.blue(this.mNormalTextColor));
    }

    public void addButton(int index, View.OnClickListener onClickListener) {
        addButton(getContext().getDrawable(index), onClickListener);
    }

    public void addOnTabSelectedListener(OnTabSelectedListener onTabSelectedListener) {
        if (this.mSelectedListeners.contains(onTabSelectedListener)) {
            return;
        }
        this.mSelectedListeners.add(onTabSelectedListener);
    }

    public void addTab(COUITab cOUITab) {
        addTab(cOUITab, this.mTabs.isEmpty());
    }

    @Override
    public void addView(View view) {
        addViewInternal(view);
    }

    @Deprecated
    public void changeTabTextFont(COUITabView cOUITabView, boolean flag) {
    }

    public void clearOnTabSelectedListeners() {
        this.mSelectedListeners.clear();
    }

    @Override
    public void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        COUISlidingTabStrip cOUISlidingTabStrip = this.mTabStrip;
        if (cOUISlidingTabStrip != null) {
            if (cOUISlidingTabStrip.getIndicatorBackgroundPaint() != null) {
                canvas.drawRect(this.mTabStrip.getIndicatorBackgroundPaddingLeft() + getScrollX(), getHeight() - this.mTabStrip.getIndicatorBackgroundHeight(), (getWidth() + getScrollX()) - this.mTabStrip.getIndicatorBackgroundPaddingRight(), getHeight(), this.mTabStrip.getIndicatorBackgroundPaint());
            }
            if (this.mTabStrip.getSelectedIndicatorPaint() != null) {
                canvas.drawText(" ", 0.0f, 0.0f, this.mTabStrip.getSelectedIndicatorPaint());
                if (this.mTabStrip.getIndicatorRight() > this.mTabStrip.getIndicatorLeft()) {
                    int paddingLeft = getPaddingLeft() + this.mTabStrip.getIndicatorLeft();
                    int paddingLeft2 = getPaddingLeft() + this.mTabStrip.getIndicatorRight();
                    int scrollX = (getScrollX() + getPaddingLeft()) - this.mIndicatorPadding;
                    int scrollX2 = ((getScrollX() + getWidth()) - getPaddingRight()) + this.mIndicatorPadding;
                    if (paddingLeft2 > scrollX && paddingLeft < scrollX2) {
                        if (paddingLeft < scrollX) {
                            paddingLeft = scrollX;
                        }
                        if (paddingLeft2 > scrollX2) {
                            paddingLeft2 = scrollX2;
                        }
                        canvas.drawRect(paddingLeft, getHeight() - this.mTabStrip.mSelectedIndicatorHeight, paddingLeft2, getHeight(), this.mTabStrip.getSelectedIndicatorPaint());
                    }
                }
                if (this.mBottomDividerEnabled) {
                    canvas.drawRect(getLeft(), getHeight() - 1, getScrollX() + getWidth() + this.mIndicatorPadding, getHeight(), this.mTabStrip.getBottomDividerPaint());
                }
            }
        }
        drawButton(canvas);
    }

    public int dpToPx(int index) {
        return Math.round(getResources().getDisplayMetrics().density * index);
    }

    public boolean enableTab(int index, boolean flag) {
        COUITabView cOUITabView;
        COUITab tabAt = getTabAt(index);
        if (tabAt == null || (cOUITabView = tabAt.mView) == null) {
            return false;
        }
        cOUITabView.setEnabled(flag);
        return true;
    }

    public float getDefaultIndicatoRatio() {
        return this.mDefaultIndicatorRatio;
    }

    public int getIndicatorAnimTime(int index, int index_2) {
        return Math.min(THREE_HUNDRED, (Math.abs(index - index_2) * FIFTY) + HUNDRED_FIFTY);
    }

    public int getIndicatorBackgroundHeight() {
        COUISlidingTabStrip cOUISlidingTabStrip = this.mTabStrip;
        if (cOUISlidingTabStrip == null) {
            return -1;
        }
        return cOUISlidingTabStrip.getIndicatorBackgroundHeight();
    }

    public int getIndicatorBackgroundPaddingLeft() {
        COUISlidingTabStrip cOUISlidingTabStrip = this.mTabStrip;
        if (cOUISlidingTabStrip == null) {
            return -1;
        }
        return cOUISlidingTabStrip.getIndicatorBackgroundPaddingLeft();
    }

    public int getIndicatorBackgroundPaddingRight() {
        COUISlidingTabStrip cOUISlidingTabStrip = this.mTabStrip;
        if (cOUISlidingTabStrip == null) {
            return -1;
        }
        return cOUISlidingTabStrip.getIndicatorBackgroundPaddingRight();
    }

    public int getIndicatorBackgroundPaintColor() {
        COUISlidingTabStrip cOUISlidingTabStrip = this.mTabStrip;
        if (cOUISlidingTabStrip == null) {
            return -1;
        }
        return cOUISlidingTabStrip.getIndicatorBackgroundPaint().getColor();
    }

    public int getIndicatorPadding() {
        return this.mIndicatorPadding;
    }

    public float getIndicatorWidthRatio() {
        COUISlidingTabStrip cOUISlidingTabStrip = this.mTabStrip;
        if (cOUISlidingTabStrip == null) {
            return -1.0f;
        }
        return cOUISlidingTabStrip.getIndicatorWidthRatio();
    }

    public int getRequestedTabMaxWidth() {
        return this.mRequestedTabMaxWidth;
    }

    public int getRequestedTabMinWidth() {
        return this.mRequestedTabMinWidth;
    }

    public int getSelectedIndicatorColor() {
        return this.mSelectedIndicatorColor;
    }

    public int getSelectedTabPosition() {
        COUITab cOUITab = this.mSelectedTab;
        if (cOUITab != null) {
            return cOUITab.getPosition();
        }
        return -1;
    }

    public COUITab getTabAt(int index) {
        if (index < 0 || index >= getTabCount()) {
            return null;
        }
        return this.mTabs.get(index);
    }

    public int getTabCount() {
        return this.mTabs.size();
    }

    public int getTabGravity() {
        return this.mTabGravity;
    }

    public int getTabMinDivider() {
        return this.mTabMinDivider;
    }

    public int getTabMinMargin() {
        return this.mTabMinMargin;
    }

    public int getTabMode() {
        return this.mMode;
    }

    public int getTabPaddingBottom() {
        return this.mTabPaddingBottom;
    }

    public int getTabPaddingEnd() {
        return this.mTabPaddingEnd;
    }

    public int getTabPaddingStart() {
        return this.mTabPaddingStart;
    }

    public int getTabPaddingTop() {
        return this.mTabPaddingTop;
    }

    public COUISlidingTabStrip getTabStrip() {
        return this.mTabStrip;
    }

    public ColorStateList getTabTextColors() {
        return this.mTabTextColors;
    }

    public float getTabTextSize() {
        return this.mTabTextSize;
    }

    @Deprecated
    public boolean isResizeText() {
        return false;
    }

    public boolean isUpdateindicatorposition() {
        return this.mIsUpdateindicatorposition;
    }

    public COUITab newTab() {
        COUITab cOUITab = (COUITab) TAB_POOL.acquire();
        if (cOUITab == null) {
            cOUITab = new COUITab();
        }
        cOUITab.mParent = this;
        cOUITab.mView = createTabView(cOUITab);
        return cOUITab;
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.mViewPager == null) {
            ViewParent parent = getParent();
            if (parent instanceof ViewPager) {
                setupWithViewPager((ViewPager) parent, true, true);
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        this.mTabAlreadyMeasure = false;
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mSetupViewPagerImplicitly) {
            setupWithViewPager(null);
            this.mSetupViewPagerImplicitly = false;
        }
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(accessibilityNodeInfo);
        AccessibilityNodeInfoCompat.wrap(accessibilityNodeInfo).setCollectionInfo(AccessibilityNodeInfoCompat.CollectionInfoCompat.obtain(1, getTabCount(), false, 0));
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getAction() == 0) {
            for (int index = 0; index < this.mButtons.size(); index++) {
                if (this.mButtons.get(index).mButtonClicklistener != null && this.mButtons.get(index).mButtonDrawable.getBounds().contains(((int) motionEvent.getX()) + getScrollX(), (int) motionEvent.getY())) {
                    return true;
                }
            }
        }
        return super.onInterceptTouchEvent(motionEvent);
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int index;
        super.onLayout(changed, left, top, right, bottom);
        if (!this.mNeedAdjust || (index = this.mSelectedPosition) < 0 || index >= this.mTabStrip.getChildCount()) {
            return;
        }
        this.mNeedAdjust = false;
        scrollTo(calculateScrollXForTab(this.mSelectedPosition, 0.0f), 0);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int iDpToPx = dpToPx(getDefaultHeight()) + getPaddingTop() + getPaddingBottom();
        int mode = View.MeasureSpec.getMode(heightMeasureSpec);
        if (mode == Integer.MIN_VALUE) {
            heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(Math.min(iDpToPx, View.MeasureSpec.getSize(heightMeasureSpec)), MeasureSpec.EXACTLY);
        } else if (mode == 0) {
            heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(iDpToPx, MeasureSpec.EXACTLY);
        }
        int size = View.MeasureSpec.getSize(widthMeasureSpec);
        if (this.mOriginalRequestedTabMaxWidth == INVALID_WIDTH) {
            this.mRequestedTabMaxWidth = (int) (size * DEFAULT_MAXIMUM_WIDTH_RATIO);
        }
        if (View.MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY) {
            setMeasuredDimension(0, 0);
            return;
        }
        int index = this.mMode;
        if (index == 0) {
            getChildAt(0).measure(View.MeasureSpec.makeMeasureSpec(536870911, MeasureSpec.AT_MOST), heightMeasureSpec);
        } else if (index == 1) {
            getChildAt(0).measure(View.MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY), heightMeasureSpec);
        }
        setMeasuredDimension(size, getChildAt(0).getMeasuredHeight());
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getAction() == 1) {
            for (int index = 0; index < this.mButtons.size(); index++) {
                if (this.mButtons.get(index).mButtonClicklistener != null && this.mButtons.get(index).mButtonDrawable.getBounds().contains(((int) motionEvent.getX()) + getScrollX(), (int) motionEvent.getY())) {
                    this.mButtons.get(index).mButtonClicklistener.onClick(this);
                    return true;
                }
            }
        }
        return super.onTouchEvent(motionEvent);
    }

    public void populateFromPagerAdapter() {
        int currentItem;
        removeAllTabs();
        androidx.viewpager.widget.PagerAdapter aVar = this.mPagerAdapter;
        if (aVar != null) {
            int count = aVar.getCount();
            androidx.viewpager.widget.PagerAdapter aVar2 = this.mPagerAdapter;
            if (aVar2 instanceof COUIFragmentStatePagerAdapter) {
                COUIFragmentStatePagerAdapter cOUIFragmentStatePagerAdapter = (COUIFragmentStatePagerAdapter) aVar2;
                for (int index = 0; index < count; index++) {
                    if (cOUIFragmentStatePagerAdapter.getPageIcon(index) > 0) {
                        addTab(newTab().setIcon(cOUIFragmentStatePagerAdapter.getPageIcon(index)), false);
                    } else {
                        addTab(newTab().setText(cOUIFragmentStatePagerAdapter.getPageTitle(index)), false);
                    }
                }
            } else {
                for (int index_2 = 0; index_2 < count; index_2++) {
                    addTab(newTab().setText(this.mPagerAdapter.getPageTitle(index_2)), false);
                }
            }
            ViewPager bVar = this.mViewPager;
            if (bVar == null || count <= 0 || (currentItem = bVar.getCurrentItem()) == getSelectedTabPosition() || currentItem >= getTabCount()) {
                return;
            }
            selectTab(getTabAt(currentItem));
        }
    }

    public void refresh() {
        String resourceTypeName = getResources().getResourceTypeName(this.mStyle);
        TypedArray typedArrayObtainStyledAttributes = null;
        if ("attr".equals(resourceTypeName)) {
            typedArrayObtainStyledAttributes = getContext().getTheme().obtainStyledAttributes(null, R.styleable.COUITabLayout, this.mStyle, 0);
        } else if ("style".equals(resourceTypeName)) {
            typedArrayObtainStyledAttributes = getContext().getTheme().obtainStyledAttributes(null, R.styleable.COUITabLayout, 0, this.mStyle);
        }
        if (typedArrayObtainStyledAttributes != null) {
            int index = R.styleable.COUITabLayout_couiTabTextColor;
            if (typedArrayObtainStyledAttributes.hasValue(index)) {
                this.mTabTextColors = typedArrayObtainStyledAttributes.getColorStateList(index);
            }
            int index_2 = R.styleable.COUITabLayout_couiTabIndicatorColor;
            if (typedArrayObtainStyledAttributes.hasValue(index_2)) {
                setSelectedTabIndicatorColor(typedArrayObtainStyledAttributes.getColor(index_2, 0));
            }
            updateTextColor();
            typedArrayObtainStyledAttributes.recycle();
        }
        for (COUITab cOUITab : this.mTabs) {
            if (cOUITab != null && cOUITab.getView() != null) {
                cOUITab.getView().refresh();
            }
        }
    }

    public void removeAllButtons(int index) {
        this.mButtons.clear();
        setTabMode(index);
        invalidate();
    }

    public void removeAllTabs() {
        for (int childCount = this.mTabStrip.getChildCount() - 1; childCount >= 0; childCount--) {
            removeTabViewAt(childCount);
        }
        Iterator<COUITab> it = this.mTabs.iterator();
        while (it.hasNext()) {
            COUITab next = it.next();
            it.remove();
            next.reset();
            TAB_POOL.release(next);
        }
        this.mSelectedTab = null;
        this.mTabAlreadyMeasure = false;
    }

    public void removeOnTabSelectedListener(OnTabSelectedListener onTabSelectedListener) {
        this.mSelectedListeners.remove(onTabSelectedListener);
    }

    public void removeTab(COUITab cOUITab) {
        if (cOUITab.mParent != this) {
            throw new IllegalArgumentException("COUITab does not belong to this TabLayout.");
        }
        removeTabAt(cOUITab.getPosition());
    }

    public void removeTabAt(int index) {
        COUITab cOUITab = this.mSelectedTab;
        int position = cOUITab != null ? cOUITab.getPosition() : 0;
        removeTabViewAt(index);
        COUITab cOUITabRemove = this.mTabs.remove(index);
        if (cOUITabRemove != null) {
            cOUITabRemove.reset();
            TAB_POOL.release(cOUITabRemove);
        }
        int size = this.mTabs.size();
        for (int index_2 = index; index_2 < size; index_2++) {
            this.mTabs.get(index_2).setPosition(index_2);
        }
        if (position == index) {
            selectTab(this.mTabs.isEmpty() ? null : this.mTabs.get(Math.max(0, index - 1)));
        }
    }

    public void resetTextColorAfterAnim() {
        int childCount = this.mTabStrip.getChildCount();
        for (int index = 0; index < childCount; index++) {
            View childAt = this.mTabStrip.getChildAt(index);
            if (childAt instanceof COUITabView) {
                ((COUITabView) childAt).getTextView().setTextColor(this.mTabTextColors);
            }
        }
    }

    public void selectTab(COUITab cOUITab) {
        selectTab(cOUITab, true);
    }

    @Override
    public void setEnableVibrator(boolean enableVibrator) {
        this.mEnableVibrator = enableVibrator;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.mTabStrip.setSelectedIndicatorColor(enabled ? this.mSelectedIndicatorColor : this.mSelectedIndicatorDisableColor);
        for (int index = 0; index < getTabCount(); index++) {
            enableTab(index, enabled);
        }
    }

    public void setIndicatorAnimTime(int indicatorAnimTime) {
        COUISlidingTabStrip cOUISlidingTabStrip = this.mTabStrip;
        if (cOUISlidingTabStrip != null) {
            cOUISlidingTabStrip.setIndicatorAnimTime(indicatorAnimTime);
        }
    }

    public void setIndicatorBackgroundColor(int indicatorBackgroundColor) {
        COUISlidingTabStrip cOUISlidingTabStrip = this.mTabStrip;
        if (cOUISlidingTabStrip == null) {
            return;
        }
        cOUISlidingTabStrip.getIndicatorBackgroundPaint().setColor(indicatorBackgroundColor);
    }

    public void setIndicatorBackgroundHeight(int indicatorBackgroundHeight) {
        COUISlidingTabStrip cOUISlidingTabStrip = this.mTabStrip;
        if (cOUISlidingTabStrip == null) {
            return;
        }
        cOUISlidingTabStrip.setIndicatorBackgroundHeight(indicatorBackgroundHeight);
    }

    public void setIndicatorBackgroundPaddingLeft(int indicatorBackgroundPaddingLeft) {
        COUISlidingTabStrip cOUISlidingTabStrip = this.mTabStrip;
        if (cOUISlidingTabStrip == null) {
            return;
        }
        cOUISlidingTabStrip.setIndicatorBackgroundPaddingLeft(indicatorBackgroundPaddingLeft);
    }

    public void setIndicatorBackgroundPaddingRight(int indicatorBackgroundPaddingRight) {
        COUISlidingTabStrip cOUISlidingTabStrip = this.mTabStrip;
        if (cOUISlidingTabStrip == null) {
            return;
        }
        cOUISlidingTabStrip.setIndicatorBackgroundPaddingRight(indicatorBackgroundPaddingRight);
    }

    public void setIndicatorPadding(int indicatorPadding) {
        this.mIndicatorPadding = indicatorPadding;
        requestLayout();
    }

    public void setIndicatorWidthRatio(float indicatorWidthRatio) {
        COUISlidingTabStrip cOUISlidingTabStrip = this.mTabStrip;
        if (cOUISlidingTabStrip == null) {
            return;
        }
        this.mDefaultIndicatorRatio = indicatorWidthRatio;
        cOUISlidingTabStrip.setIndicatorWidthRatio(indicatorWidthRatio);
    }

    @Deprecated
    public void setOnTabSelectedListener(OnTabSelectedListener onTabSelectedListener) {
        OnTabSelectedListener onTabSelectedListener2 = this.mSelectedListener;
        if (onTabSelectedListener2 != null) {
            removeOnTabSelectedListener(onTabSelectedListener2);
        }
        this.mSelectedListener = onTabSelectedListener;
        if (onTabSelectedListener != null) {
            addOnTabSelectedListener(onTabSelectedListener);
        }
    }

    public void setPaddingLeftAndRight(int index, int index_2) {
        ViewCompat.setPaddingRelative(this, index, 0, index_2, 0);
    }

    public void setPagerAdapter(androidx.viewpager.widget.PagerAdapter aVar, boolean pagerAdapter) {
        DataSetObserver dataSetObserver;
        androidx.viewpager.widget.PagerAdapter aVar2 = this.mPagerAdapter;
        if (aVar2 != null && (dataSetObserver = this.mPagerAdapterObserver) != null) {
            aVar2.unregisterDataSetObserver(dataSetObserver);
        }
        this.mPagerAdapter = aVar;
        if (pagerAdapter && aVar != null) {
            if (this.mPagerAdapterObserver == null) {
                this.mPagerAdapterObserver = new PagerAdapterObserver();
            }
            aVar.registerDataSetObserver(this.mPagerAdapterObserver);
        }
        populateFromPagerAdapter();
    }

    public void setRequestedTabMaxWidth(int requestedTabMaxWidth) {
        this.mRequestedTabMaxWidth = requestedTabMaxWidth;
        this.mOriginalRequestedTabMaxWidth = requestedTabMaxWidth;
    }

    public void setRequestedTabMinWidth(int requestedTabMinWidth) {
        this.mRequestedTabMinWidth = requestedTabMinWidth;
        this.mOriginalRequestedTabMinWidth = requestedTabMinWidth;
    }

    public void setScrollAnimatorListener(Animator.AnimatorListener animatorListener) {
        ensureScrollAnimator();
        this.mScrollAnimator.addListener(animatorListener);
    }

    public void setScrollPosition(int index, float value, boolean flag) {
        setScrollPosition(index, value, flag, true);
    }

    public void setSelectedTabIndicatorColor(int selectedTabIndicatorColor) {
        this.mTabStrip.setSelectedIndicatorColor(selectedTabIndicatorColor);
        this.mSelectedIndicatorColor = selectedTabIndicatorColor;
    }

    public void setSelectedTabIndicatorHeight(int selectedTabIndicatorHeight) {
        this.mTabStrip.setSelectedIndicatorHeight(selectedTabIndicatorHeight);
    }

    public void setTabGravity(int tabGravity) {
    }

    public void setTabMinDivider(int tabMinDivider) {
        this.mTabMinDivider = tabMinDivider;
        requestLayout();
    }

    public void setTabMinMargin(int tabMinMargin) {
        this.mTabMinMargin = tabMinMargin;
        ViewCompat.setPaddingRelative(this, tabMinMargin, 0, tabMinMargin, 0);
        requestLayout();
    }

    public void setTabMode(int tabMode) {
        if (tabMode != this.mMode) {
            this.mMode = tabMode;
            applyModeAndGravity();
        }
    }

    public void setTabPaddingBottom(int tabPaddingBottom) {
        this.mTabPaddingBottom = tabPaddingBottom;
        requestLayout();
    }

    public void setTabPaddingEnd(int tabPaddingEnd) {
        this.mTabPaddingEnd = tabPaddingEnd;
        requestLayout();
    }

    public void setTabPaddingStart(int tabPaddingStart) {
        this.mTabPaddingStart = tabPaddingStart;
        requestLayout();
    }

    public void setTabPaddingTop(int tabPaddingTop) {
        this.mTabPaddingTop = tabPaddingTop;
        requestLayout();
    }

    public void setTabTextColors(ColorStateList colorStateList) {
        if (this.mTabTextColors != colorStateList) {
            this.mTabTextColors = colorStateList;
            updateTextColor();
            updateAllTabs();
        }
    }

    public void setTabTextSize(float tabTextSize) {
        if (this.mTabStrip != null) {
            this.mDefaultTabTextSize = tabTextSize;
            this.mTabTextSize = tabTextSize;
        }
    }

    @Deprecated
    public void setTabsFromPagerAdapter(androidx.viewpager.widget.PagerAdapter aVar) {
        setPagerAdapter(aVar, false);
    }

    public void setUpdateindicatorposition(boolean updateindicatorposition) {
        this.mIsUpdateindicatorposition = updateindicatorposition;
    }

    public void setupWithViewPager(ViewPager bVar) {
        setupWithViewPager(bVar, true);
    }

    @Override
    public boolean shouldDelayChildPressedState() {
        return getTabScrollRange() > 0;
    }

    public void updateTabViews(boolean flag) {
        for (int index = 0; index < this.mTabStrip.getChildCount(); index++) {
            COUITabView cOUITabView = (COUITabView) this.mTabStrip.getChildAt(index);
            cOUITabView.setMinimumWidth(getTabMinWidth());
            if (cOUITabView.getTextView() != null) {
                ViewCompat.setPaddingRelative(cOUITabView.getTextView(), this.mTabPaddingStart, this.mTabPaddingTop, this.mTabPaddingEnd, this.mTabPaddingBottom);
            }
            if (flag) {
                cOUITabView.requestLayout();
            }
        }
    }

    public COUITabLayout(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, R.attr.couiTabLayoutStyle);
    }

    public void addButton(Drawable drawable, View.OnClickListener onClickListener) {
        addButton(drawable, onClickListener, (Drawable) null, (View.OnClickListener) null);
    }

    public void addTab(COUITab cOUITab, int index) {
        addTab(cOUITab, index, this.mTabs.isEmpty());
    }

    @Override
    public void addView(View view, int index) {
        addViewInternal(view);
    }

    @Override
    public FrameLayout.LayoutParams generateLayoutParams(AttributeSet attributeSet) {
        return generateDefaultLayoutParams();
    }

    public void selectTab(COUITab cOUITab, boolean flag) {
        COUITab cOUITab2 = this.mSelectedTab;
        if (cOUITab2 == cOUITab) {
            if (cOUITab2 != null) {
                dispatchTabReselected(cOUITab);
                return;
            }
            return;
        }
        int position = cOUITab != null ? cOUITab.getPosition() : -1;
        if (flag) {
            if ((cOUITab2 == null || cOUITab2.getPosition() == -1) && position != -1) {
                setScrollPosition(position, 0.0f, true);
            } else {
                animateToTab(position);
            }
            if (position != -1) {
                setSelectedTabView(position);
            }
            this.mSelectedPosition = position;
        } else if (isEnabled() && this.mEnableVibrator) {
            performHapticFeedback(COUIHapticFeedbackConstants.GRANULAR_SHORT_VIBRATE);
        }
        if (cOUITab2 != null) {
            dispatchTabUnselected(cOUITab2);
        }
        this.mSelectedTab = cOUITab;
        if (cOUITab != null) {
            dispatchTabSelected(cOUITab);
        }
    }

    public void setScrollPosition(int index, float value, boolean flag, boolean flag_2) {
        int iRound = Math.round(index + value);
        if (iRound < 0 || iRound >= this.mTabStrip.getChildCount()) {
            return;
        }
        if (flag_2) {
            this.mTabStrip.setIndicatorPositionFromTabPosition(index, value);
        } else if (this.mTabStrip.mSelectedPosition != getSelectedTabPosition()) {
            this.mTabStrip.mSelectedPosition = getSelectedTabPosition();
            this.mTabStrip.updateIndicatorPosition();
        }
        ValueAnimator valueAnimator = this.mScrollAnimator;
        if (valueAnimator != null && valueAnimator.isRunning()) {
            this.mScrollAnimator.cancel();
        }
        scrollTo(calculateScrollXForTab(index, value), 0);
        if (flag) {
            setSelectedTabView(iRound, value);
        }
    }

    @Deprecated
    public void setTabTextSize(float width, boolean height) {
        setTabTextSize(width);
    }

    public void setupWithViewPager(ViewPager bVar, boolean upWithViewPager) {
        setupWithViewPager(bVar, upWithViewPager, false);
    }

    public COUITabLayout(Context context, AttributeSet attributeSet, int index) {
        this(context, attributeSet, index, R.style.COUITabLayoutBaseStyle);
    }

    private void setupWithViewPager(ViewPager bVar, boolean flag, boolean flag_2) {
        ViewPager bVar2 = this.mViewPager;
        if (bVar2 != null) {
            TabLayoutOnPageChangeListener tabLayoutOnPageChangeListener = this.mPageChangeListener;
            if (tabLayoutOnPageChangeListener != null) {
                bVar2.removeOnPageChangeListener(tabLayoutOnPageChangeListener);
            }
            AdapterChangeListener adapterChangeListener = this.mAdapterChangeListener;
            if (adapterChangeListener != null) {
                this.mViewPager.removeOnAdapterChangeListener(adapterChangeListener);
            }
        }
        OnTabSelectedListener onTabSelectedListener = this.mCurrentVpSelectedListener;
        if (onTabSelectedListener != null) {
            removeOnTabSelectedListener(onTabSelectedListener);
            this.mCurrentVpSelectedListener = null;
        }
        if (bVar != null) {
            this.mViewPager = bVar;
            if (this.mPageChangeListener == null) {
                this.mPageChangeListener = new TabLayoutOnPageChangeListener(this);
            }
            this.mPageChangeListener.reset();
            bVar.addOnPageChangeListener(this.mPageChangeListener);
            ViewPagerOnTabSelectedListener viewPagerOnTabSelectedListener = new ViewPagerOnTabSelectedListener(bVar);
            this.mCurrentVpSelectedListener = viewPagerOnTabSelectedListener;
            addOnTabSelectedListener(viewPagerOnTabSelectedListener);
            if (bVar.getAdapter() != null) {
                setPagerAdapter(bVar.getAdapter(), flag);
            }
            if (this.mAdapterChangeListener == null) {
                this.mAdapterChangeListener = new AdapterChangeListener();
            }
            this.mAdapterChangeListener.setAutoRefresh(flag);
            bVar.addOnAdapterChangeListener(this.mAdapterChangeListener);
            setScrollPosition(bVar.getCurrentItem(), 0.0f, true);
        } else {
            this.mViewPager = null;
            setPagerAdapter(null, false);
        }
        this.mSetupViewPagerImplicitly = flag_2;
    }

    public void addButton(int index, View.OnClickListener onClickListener, int index_2, View.OnClickListener onClickListener2) {
        addButton(getContext().getDrawable(index), onClickListener, getContext().getDrawable(index_2), onClickListener2);
    }

    public void addTab(COUITab cOUITab, boolean flag) {
        addTab(cOUITab, this.mTabs.size(), flag);
    }

    @Override
    public void addView(View view, ViewGroup.LayoutParams layoutParams) {
        addViewInternal(view);
    }

    public void setTabTextColors(int index, int index_2) {
        setTabTextColors(createColorStateList(index, this.mTabTextDisabledColor, index_2));
    }

    public COUITabLayout(Context context, AttributeSet attributeSet, int index, int index_2) {
        super(context, attributeSet, index, index_2);
        this.mTabs = new ArrayList<>();
        this.mSelectedListeners = new ArrayList<>();
        this.mTabViewPool = new androidx.core.util.Pools.SimplePool(12);
        this.mRequestedTabMaxWidth = INVALID_WIDTH;
        this.mSelectedPosition = 0;
        this.mLastOffset = 0.0f;
        this.mEvaluator = new ArgbEvaluator();
        this.mIsUpdateindicatorposition = false;
        this.mButtons = new ArrayList<>();
        if (attributeSet != null) {
            int styleAttribute = attributeSet.getStyleAttribute();
            this.mStyle = styleAttribute;
            if (styleAttribute == 0) {
                this.mStyle = index;
            }
        } else {
            this.mStyle = index;
        }
        this.mSelectedTypeface = Typeface.create(MEDIUM_FONT, Typeface.NORMAL);
        this.mNormalTypeface = Typeface.create(REGULAR_FONT, Typeface.NORMAL);
        setHorizontalScrollBarEnabled(false);
        COUISlidingTabStrip cOUISlidingTabStrip = new COUISlidingTabStrip(context, this);
        this.mTabStrip = cOUISlidingTabStrip;
        super.addView(cOUISlidingTabStrip, 0, new FrameLayout.LayoutParams(-2, -1));
        TypedArray typedArrayObtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.COUITabLayout, index, index_2);
        cOUISlidingTabStrip.setSelectedIndicatorHeight(typedArrayObtainStyledAttributes.getDimensionPixelSize(R.styleable.COUITabLayout_couiTabIndicatorHeight, 0));
        int color = typedArrayObtainStyledAttributes.getColor(R.styleable.COUITabLayout_couiTabIndicatorColor, 0);
        this.mSelectedIndicatorColor = color;
        cOUISlidingTabStrip.setSelectedIndicatorColor(color);
        this.mBottomDividerColor = typedArrayObtainStyledAttributes.getColor(R.styleable.COUITabLayout_couiTabBottomDividerColor, 0);
        this.mBottomDividerEnabled = typedArrayObtainStyledAttributes.getBoolean(R.styleable.COUITabLayout_couiTabBottomDividerEnabled, false);
        cOUISlidingTabStrip.setBottomDividerColor(this.mBottomDividerColor);
        setIndicatorBackgroundHeight(typedArrayObtainStyledAttributes.getDimensionPixelSize(R.styleable.COUITabLayout_couiTabIndicatorBackgroundHeight, 0));
        setIndicatorBackgroundColor(typedArrayObtainStyledAttributes.getColor(R.styleable.COUITabLayout_couiTabIndicatorBackgroundColor, 0));
        setIndicatorBackgroundPaddingLeft(typedArrayObtainStyledAttributes.getDimensionPixelSize(R.styleable.COUITabLayout_couiTabIndicatorBackgroundPaddingLeft, 0));
        setIndicatorBackgroundPaddingRight(typedArrayObtainStyledAttributes.getDimensionPixelSize(R.styleable.COUITabLayout_couiTabIndicatorBackgroundPaddingRight, 0));
        setIndicatorWidthRatio(typedArrayObtainStyledAttributes.getFloat(R.styleable.COUITabLayout_couiTabIndicatorWidthRatio, 0.0f));
        this.mResizeHeight = getResources().getDimensionPixelOffset(R.dimen.coui_tablayout_default_resize_height);
        this.mLongTextViewHeight = getResources().getDimensionPixelOffset(R.dimen.tablayout_long_text_view_height);
        this.mTabMinDivider = typedArrayObtainStyledAttributes.getDimensionPixelOffset(R.styleable.COUITabLayout_couiTabMinDivider, -1);
        this.mTabMinMargin = typedArrayObtainStyledAttributes.getDimensionPixelOffset(R.styleable.COUITabLayout_couiTabMinMargin, -1);
        this.mIndicatorPadding = getResources().getDimensionPixelOffset(R.dimen.coui_tablayout_indicator_padding);
        int dimensionPixelSize = typedArrayObtainStyledAttributes.getDimensionPixelSize(R.styleable.COUITabLayout_couiTabPadding, -1);
        this.mTabPaddingStart = dimensionPixelSize;
        this.mTabPaddingTop = dimensionPixelSize;
        this.mTabPaddingEnd = dimensionPixelSize;
        this.mTabPaddingBottom = dimensionPixelSize;
        this.mTabPaddingStart = typedArrayObtainStyledAttributes.getDimensionPixelSize(R.styleable.COUITabLayout_couiTabPaddingStart, dimensionPixelSize);
        this.mTabPaddingTop = typedArrayObtainStyledAttributes.getDimensionPixelSize(R.styleable.COUITabLayout_couiTabPaddingTop, this.mTabPaddingTop);
        this.mTabPaddingEnd = typedArrayObtainStyledAttributes.getDimensionPixelSize(R.styleable.COUITabLayout_couiTabPaddingEnd, this.mTabPaddingEnd);
        this.mTabPaddingBottom = typedArrayObtainStyledAttributes.getDimensionPixelSize(R.styleable.COUITabLayout_couiTabPaddingBottom, this.mTabPaddingBottom);
        this.mTabPaddingStart = Math.max(0, this.mTabPaddingStart);
        this.mTabPaddingTop = Math.max(0, this.mTabPaddingTop);
        this.mTabPaddingEnd = Math.max(0, this.mTabPaddingEnd);
        this.mTabPaddingBottom = Math.max(0, this.mTabPaddingBottom);
        int resourceId = typedArrayObtainStyledAttributes.getResourceId(R.styleable.COUITabLayout_couiTabTextAppearance, R.style.TextAppearance_Design_COUITab);
        this.mTabTextAppearance = resourceId;
        TypedArray typedArrayObtainStyledAttributes2 = context.obtainStyledAttributes(resourceId, androidx.appcompat.R.styleable.TextAppearance);
        try {
            float dimensionPixelSize2 = typedArrayObtainStyledAttributes2.getDimensionPixelSize(androidx.appcompat.R.styleable.TextAppearance_android_textSize, 0);
            this.mTabTextSize = dimensionPixelSize2;
            this.mDefaultTabTextSize = dimensionPixelSize2;
            this.mTabTextColors = typedArrayObtainStyledAttributes2.getColorStateList(androidx.appcompat.R.styleable.TextAppearance_android_textColor);
            typedArrayObtainStyledAttributes2.recycle();
            int index_3 = R.styleable.COUITabLayout_couiTabTextColor;
            if (typedArrayObtainStyledAttributes.hasValue(index_3)) {
                this.mTabTextColors = typedArrayObtainStyledAttributes.getColorStateList(index_3);
            }
            this.mTabTextDisabledColor = COUIContextUtil.getAttrColor(getContext(), R.attr.couiColorDisabledNeutral, 0);
            int index_4 = R.styleable.COUITabLayout_couiTabSelectedTextColor;
            if (typedArrayObtainStyledAttributes.hasValue(index_4)) {
                this.mTabTextColors = createColorStateList(this.mTabTextColors.getDefaultColor(), this.mTabTextDisabledColor, typedArrayObtainStyledAttributes.getColor(index_4, 0));
            }
            this.mRequestedTabMinWidth = typedArrayObtainStyledAttributes.getDimensionPixelSize(R.styleable.COUITabLayout_couiTabMinWidth, INVALID_WIDTH);
            this.mTabBackgroundResId = typedArrayObtainStyledAttributes.getResourceId(R.styleable.COUITabLayout_couiTabBackground, 0);
            this.mMode = typedArrayObtainStyledAttributes.getInt(R.styleable.COUITabLayout_couiTabMode, 1);
            this.mTabGravity = typedArrayObtainStyledAttributes.getInt(R.styleable.COUITabLayout_couiTabGravity, GRAVITY_FILL);
            this.mEnableVibrator = typedArrayObtainStyledAttributes.getBoolean(R.styleable.COUITabLayout_couiTabEnableVibrator, true);
            this.mSelectedIndicatorDisableColor = typedArrayObtainStyledAttributes.getColor(R.styleable.COUITabLayout_couiTabIndicatorDisableColor, getResources().getColor(R.color.couiTabIndicatorDisableColor));
            int index_5 = R.styleable.COUITabLayout_couiTabTextSize;
            if (typedArrayObtainStyledAttributes.hasValue(index_5)) {
                float dimension = typedArrayObtainStyledAttributes.getDimension(index_5, 0.0f);
                this.mTabTextSize = dimension;
                this.mDefaultTabTextSize = dimension;
            }
            this.mOriginalRequestedTabMinWidth = this.mRequestedTabMinWidth;
            this.mOriginalRequestedTabMaxWidth = this.mRequestedTabMaxWidth;
            this.mButtonMarginEnd = typedArrayObtainStyledAttributes.getDimensionPixelOffset(R.styleable.COUITabLayout_couiTabButtonMarginEnd, -1);
            typedArrayObtainStyledAttributes.recycle();
            this.mDotHorizontalOffset = context.getResources().getDimensionPixelSize(R.dimen.coui_dot_horizontal_offset);
            this.mDotVerticalOffsetFromOnlyRed = context.getResources().getDimensionPixelSize(R.dimen.coui_dot_vertical_offset_only_red);
            this.mDotVerticalOffsetFromNumberRed = context.getResources().getDimensionPixelSize(R.dimen.coui_dot_vertical_offset_number_red);
            applyModeAndGravity();
            updateTextColor();
            setOverScrollMode(1);
        } catch (Throwable th) {
            typedArrayObtainStyledAttributes2.recycle();
            throw th;
        }
    }

    private void setSelectedTabView(int index, float value) {
        COUITabView cOUITabView;
        float value_3;
        if (Math.abs(value - this.mLastOffset) > 0.5f || value == 0.0f) {
            this.mSelectedPosition = index;
        }
        this.mLastOffset = value;
        if (index != this.mSelectedPosition && isEnabled()) {
            COUITabView cOUITabView2 = (COUITabView) this.mTabStrip.getChildAt(index);
            if (value >= 0.5f) {
                cOUITabView = (COUITabView) this.mTabStrip.getChildAt(index - 1);
                value_3 = value - 0.5f;
            } else {
                cOUITabView = (COUITabView) this.mTabStrip.getChildAt(index + 1);
                value_3 = 0.5f - value;
            }
            float value_2 = value_3 / 0.5f;
            if (cOUITabView.getTextView() != null) {
                cOUITabView.getTextView().setTextColor(((Integer) this.mEvaluator.evaluate(value_2, Integer.valueOf(this.mSelectedTextColor), Integer.valueOf(this.mNormalTextColor))).intValue());
            }
            if (cOUITabView2.getTextView() != null) {
                cOUITabView2.getTextView().setTextColor(((Integer) this.mEvaluator.evaluate(value_2, Integer.valueOf(this.mNormalTextColor), Integer.valueOf(this.mSelectedTextColor))).intValue());
            }
        }
        if (value != 0.0f || index >= getTabCount()) {
            return;
        }
        int index_2 = 0;
        while (true) {
            boolean flag = true;
            if (index_2 >= getTabCount()) {
                this.mNeedAdjust = true;
                return;
            }
            View childAt = this.mTabStrip.getChildAt(index_2);
            COUITabView cOUITabView3 = (COUITabView) childAt;
            if (cOUITabView3.getTextView() != null) {
                cOUITabView3.getTextView().setTextColor(this.mTabTextColors);
            }
            if (index_2 != index) {
                flag = false;
            }
            childAt.setSelected(flag);
            index_2++;
        }
    }

    public void addTab(COUITab cOUITab, int index, boolean flag) {
        if (cOUITab.mParent == this) {
            configureTab(cOUITab, index);
            addTabView(cOUITab);
            if (flag) {
                cOUITab.select();
                return;
            }
            return;
        }
        throw new IllegalArgumentException("COUITab belongs to a different TabLayout.");
    }

    @Override
    public void addView(View view, int index, ViewGroup.LayoutParams layoutParams) {
        addViewInternal(view);
    }

    public void addButton(Drawable drawable, View.OnClickListener onClickListener, Drawable doubleValue, View.OnClickListener onClickListener2) {
        this.mButtons.clear();
        this.mButtons.add(new PrivateButton(drawable, onClickListener));
        if (doubleValue != null) {
            this.mButtons.add(new PrivateButton(doubleValue, onClickListener2));
        }
        setTabMode(0);
        invalidate();
    }
}







