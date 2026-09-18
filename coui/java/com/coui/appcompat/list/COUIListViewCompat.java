package com.coui.appcompat.list;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListAdapter;
import android.widget.ListView;

import androidx.core.graphics.drawable.DrawableCompat;

import java.lang.reflect.Field;

public class COUIListViewCompat extends ListView {
    public static final int INVALID_POSITION = -1;
    public static final int NO_POSITION = -1;
    private static final int[] STATE_SET_NOTHING = {0};

    private Field mIsChildViewEnabled;
    int mSelectionBottomPadding;
    int mSelectionLeftPadding;
    int mSelectionRightPadding;
    int mSelectionTopPadding;
    private GateKeeperDrawable mSelector;
    final Rect mSelectorRect = new Rect();

    public COUIListViewCompat(Context context) {
        this(context, null);
    }

    public COUIListViewCompat(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public COUIListViewCompat(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        try {
            mIsChildViewEnabled = AbsListView.class.getDeclaredField("mIsChildViewEnabled");
            mIsChildViewEnabled.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void dispatchDraw(Canvas canvas) {
        drawSelectorCompat(canvas);
        super.dispatchDraw(canvas);
    }

    public void drawSelectorCompat(Canvas canvas) {
        if (!mSelectorRect.isEmpty()) {
            Drawable selector = getSelector();
            if (selector != null) {
                selector.setBounds(mSelectorRect);
                selector.draw(canvas);
            }
        }
    }

    @Override
    public void drawableStateChanged() {
        super.drawableStateChanged();
        setSelectorEnabled(true);
        updateSelectorStateCompat();
    }

    public int lookForSelectablePosition(int position, boolean lookDown) {
        ListAdapter adapter = getAdapter();
        if (adapter == null || isInTouchMode()) {
            return INVALID_POSITION;
        }
        int count = adapter.getCount();
        if (!adapter.areAllItemsEnabled()) {
            int selectablePosition;
            if (lookDown) {
                selectablePosition = Math.max(0, position);
                while (selectablePosition < count && !adapter.isEnabled(selectablePosition)) {
                    selectablePosition++;
                }
            } else {
                selectablePosition = Math.min(position, count - 1);
                while (selectablePosition >= 0 && !adapter.isEnabled(selectablePosition)) {
                    selectablePosition--;
                }
            }
            if (selectablePosition < 0 || selectablePosition >= count) {
                return INVALID_POSITION;
            }
            return selectablePosition;
        }
        if (position < 0 || position >= count) {
            return INVALID_POSITION;
        }
        return position;
    }

    public int measureHeightOfChildrenCompat(int widthMeasureSpec, int startPosition,
            int endPosition, int maxHeight, int disallowPartialChildPosition) {
        int returnedHeight = getListPaddingTop() + getListPaddingBottom();
        int dividerHeight = getDividerHeight();
        Drawable divider = getDivider();
        ListAdapter adapter = getAdapter();
        if (adapter == null) {
            return returnedHeight;
        }
        if (dividerHeight <= 0 || divider == null) {
            dividerHeight = 0;
        }
        int count = adapter.getCount();
        View child = null;
        int viewType = 0;
        int prevHeightWithoutPartialChild = 0;
        for (int i = 0; i < count; i++) {
            int newType = adapter.getItemViewType(i);
            if (newType != viewType) {
                child = null;
                viewType = newType;
            }
            child = adapter.getView(i, child, this);
            ViewGroup.LayoutParams lp = child.getLayoutParams();
            int childHeightSpec = lp != null && lp.height > 0
                    ? MeasureSpec.makeMeasureSpec(lp.height, MeasureSpec.EXACTLY)
                    : MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
            child.measure(widthMeasureSpec, childHeightSpec);
            if (i > 0) {
                returnedHeight += dividerHeight;
            }
            returnedHeight += child.getMeasuredHeight();
            if (returnedHeight >= maxHeight) {
                return disallowPartialChildPosition >= 0
                        && i > disallowPartialChildPosition
                        && prevHeightWithoutPartialChild > 0
                        && returnedHeight != maxHeight
                        ? prevHeightWithoutPartialChild
                        : maxHeight;
            }
            if (disallowPartialChildPosition >= 0 && i >= disallowPartialChildPosition) {
                prevHeightWithoutPartialChild = returnedHeight;
            }
        }
        return returnedHeight;
    }

    public void positionSelectorCompat(int position, View sel) {
        Rect selectorRect = mSelectorRect;
        selectorRect.set(sel.getLeft(), sel.getTop(), sel.getRight(), sel.getBottom());
        selectorRect.left -= mSelectionLeftPadding;
        selectorRect.top -= mSelectionTopPadding;
        selectorRect.right += mSelectionRightPadding;
        selectorRect.bottom += mSelectionBottomPadding;
        try {
            boolean isChildViewEnabled = mIsChildViewEnabled.getBoolean(this);
            if (sel.isEnabled() != isChildViewEnabled) {
                mIsChildViewEnabled.set(this, Boolean.valueOf(!isChildViewEnabled));
                if (position != INVALID_POSITION) {
                    refreshDrawableState();
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void positionSelectorLikeFocusCompat(int position, View sel) {
        Drawable selector = getSelector();
        boolean manageState = selector != null && position != INVALID_POSITION;
        if (manageState) {
            selector.setVisible(false, false);
        }
        positionSelectorCompat(position, sel);
        if (manageState) {
            Rect rect = mSelectorRect;
            float x = rect.exactCenterX();
            float y = rect.exactCenterY();
            selector.setVisible(getVisibility() == VISIBLE, false);
            DrawableCompat.setHotspot(selector, x, y);
        }
    }

    public void positionSelectorLikeTouchCompat(int position, View sel, float x, float y) {
        positionSelectorLikeFocusCompat(position, sel);
        Drawable selector = getSelector();
        if (selector != null && position != INVALID_POSITION) {
            DrawableCompat.setHotspot(selector, x, y);
        }
    }

    @Override
    public void setSelector(Drawable sel) {
        mSelector = sel != null ? new GateKeeperDrawable(sel) : null;
        super.setSelector(mSelector);
        Rect padding = new Rect();
        if (sel != null) {
            sel.getPadding(padding);
        }
        mSelectionLeftPadding = padding.left;
        mSelectionTopPadding = padding.top;
        mSelectionRightPadding = padding.right;
        mSelectionBottomPadding = padding.bottom;
    }

    public void setSelectorEnabled(boolean enabled) {
        if (mSelector != null) {
            mSelector.setEnabled(enabled);
        }
    }

    public boolean shouldShowSelectorCompat() {
        return touchModeDrawsInPressedStateCompat() && isPressed();
    }

    public boolean touchModeDrawsInPressedStateCompat() {
        return false;
    }

    public void updateSelectorStateCompat() {
        Drawable selector = getSelector();
        if (selector != null && shouldShowSelectorCompat()) {
            selector.setState(getDrawableState());
        }
    }

    public static class GateKeeperDrawable extends Drawable {
        private final Drawable mDrawable;
        private boolean mEnabled = true;

        public GateKeeperDrawable(Drawable drawable) {
            mDrawable = drawable;
        }

        @Override
        public void draw(Canvas canvas) {
            if (mEnabled) {
                mDrawable.draw(canvas);
            }
        }

        public void setEnabled(boolean enabled) {
            mEnabled = enabled;
        }

        @Override
        public int getOpacity() {
            return mDrawable.getOpacity();
        }

        @Override
        public void setAlpha(int alpha) {
            mDrawable.setAlpha(alpha);
        }

        @Override
        public void setBounds(Rect bounds) {
            super.setBounds(bounds);
            mDrawable.setBounds(bounds);
        }

        @Override
        protected void onBoundsChange(Rect bounds) {
            mDrawable.setBounds(bounds);
        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {
            mDrawable.setColorFilter(colorFilter);
        }

        @Override
        public boolean setState(int[] stateSet) {
            return mEnabled && mDrawable.setState(stateSet);
        }

        @Override
        public boolean setVisible(boolean visible, boolean restart) {
            return mEnabled && mDrawable.setVisible(visible, restart);
        }
    }
}
