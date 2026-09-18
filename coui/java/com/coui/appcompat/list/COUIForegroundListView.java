package com.coui.appcompat.list;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HeaderViewListAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

import androidx.appcompat.view.menu.MenuAdapter;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.appcompat.widget.MenuItemHoverListener;

import com.coui.appcompat.poplist.DefaultAdapter;
import com.coui.appcompat.poplist.PopupListItem;

public class COUIForegroundListView extends ListView {
    private int mAdvanceKey;
    private MenuItemHoverListener mHoverListener;
    private MenuItem mHoveredMenuItem;
    private boolean mListSelectionHidden;
    private final Paint mPaint = new Paint();
    private Path mPath;
    private float mRadius;
    private RectF mRectF;
    private int mRetreatKey;

    public COUIForegroundListView(Context context) {
        super(context);
        initKeyValue(context);
    }

    public COUIForegroundListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initKeyValue(context);
    }

    public COUIForegroundListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initKeyValue(context);
    }

    public COUIForegroundListView(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initKeyValue(context);
    }

    private Path genPath() {
        float radius = mRadius;
        mPath.addRoundRect(new RectF(0.0f, 0.0f, getWidth(), getHeight()),
                new float[]{radius, radius, radius, radius, radius, radius, radius, radius},
                Path.Direction.CW);
        return mPath;
    }

    private void initKeyValue(Context context) {
        if (context.getResources().getConfiguration().getLayoutDirection() == LAYOUT_DIRECTION_RTL) {
            mAdvanceKey = KeyEvent.KEYCODE_DPAD_LEFT;
            mRetreatKey = KeyEvent.KEYCODE_DPAD_RIGHT;
        } else {
            mAdvanceKey = KeyEvent.KEYCODE_DPAD_RIGHT;
            mRetreatKey = KeyEvent.KEYCODE_DPAD_LEFT;
        }
    }

    public void clearSelection() {
        setSelection(INVALID_POSITION);
    }

    @Override
    public boolean isInTouchMode() {
        return mListSelectionHidden || super.isInTouchMode();
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

    @Override
    public void onDraw(Canvas canvas) {
        if (mRadius > 0.0f) {
            canvas.clipPath(mPath);
        }
        super.onDraw(canvas);
    }

    @Override
    public boolean onHoverEvent(MotionEvent ev) {
        if (mHoverListener != null) {
            ListAdapter adapter = getAdapter();
            int headersCount;
            MenuAdapter menuAdapter;
            if (adapter instanceof HeaderViewListAdapter) {
                HeaderViewListAdapter headerAdapter = (HeaderViewListAdapter) adapter;
                headersCount = headerAdapter.getHeadersCount();
                menuAdapter = (MenuAdapter) headerAdapter.getWrappedAdapter();
            } else {
                headersCount = 0;
                menuAdapter = (MenuAdapter) adapter;
            }
            MenuItemImpl menuItem = null;
            if (ev.getAction() != MotionEvent.ACTION_HOVER_EXIT) {
                int position = pointToPosition((int) ev.getX(), (int) ev.getY());
                if (position != INVALID_POSITION) {
                    int itemPosition = position - headersCount;
                    if (itemPosition >= 0 && itemPosition < menuAdapter.getCount()) {
                        menuItem = menuAdapter.getItem(itemPosition);
                    }
                }
            }
            MenuItem oldMenuItem = mHoveredMenuItem;
            if (oldMenuItem != menuItem) {
                MenuBuilder menu = menuAdapter.getAdapterMenu();
                if (oldMenuItem != null) {
                    mHoverListener.onItemHoverExit(menu, oldMenuItem);
                }
                mHoveredMenuItem = menuItem;
                if (menuItem != null) {
                    mHoverListener.onItemHoverEnter(menu, menuItem);
                }
            }
        }
        return super.onHoverEvent(ev);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        View selectedView = getSelectedView();
        if (!(selectedView instanceof LinearLayout)) {
            return super.onKeyDown(keyCode, event);
        }
        LinearLayout selectedLayout = (LinearLayout) selectedView;
        ListAdapter adapter = getAdapter();
        if (keyCode == mAdvanceKey && adapter instanceof DefaultAdapter) {
            if (selectedLayout.isEnabled()) {
                PopupListItem item = (PopupListItem) ((DefaultAdapter) adapter)
                        .getItem(getSelectedItemPosition());
                if (item.hasSubArray()) {
                    performItemClick(selectedLayout, getSelectedItemPosition(), getSelectedItemId());
                }
            }
            return true;
        }
        if (keyCode == mRetreatKey) {
            setSelection(INVALID_POSITION);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mPath == null) {
            mPath = new Path();
        } else {
            mPath.reset();
        }
        mRectF = new RectF(0.0f, 0.0f, getWidth(), getHeight());
        genPath();
    }

    public void setHoverListener(MenuItemHoverListener hoverListener) {
        mHoverListener = hoverListener;
    }

    public void setListSelectionHidden(boolean listSelectionHidden) {
        mListSelectionHidden = listSelectionHidden;
    }

    public void setRadius(float radius) {
        mRadius = radius;
    }
}
