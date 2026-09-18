package com.coui.appcompat.list;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListView;

import com.coui.appcompat.R;
import com.coui.appcompat.scrollbar.COUIScrollBar;

public class COUIListView extends ListView implements COUIScrollBar.COUIScrollable {
    private static final float DEFAULT_INTERACTING_NESTED_SCROLL_ANGLE = 20.0f;
    private static final double DEGREE_TO_ARC_CONSTANT = 0.017453292519943295d;
    private static final int INVALID_SCROLL_CHOICE_POSITION = -2;
    private static final int SCROLLBARS_NONE = 0;
    private static final int SCROLLBARS_VERTICAL = 512;
    private static final long SCROLL_CHOICE_SCROLL_DELAY = 50L;
    private static final String TAG = "COUIListView";

    private COUIScrollBar mCOUIScrollBar;
    private int mCheckItemId;
    private final Runnable mDelayedScroll;
    private boolean mEnableDispatchEventWhileScrolling;
    private float mEventFilterAngle;
    private boolean mFlag;
    private int mInitialTouchX;
    private int mInitialTouchY;
    private int mLastPosition;
    private int mLastSite;
    private int mLasterPosition;
    private int mLeftOffset;
    private boolean mMultiChoice;
    private int mRightOffset;
    private ScrollMultiChoiceListener mScrollMultiChoiceListener;
    private Drawable mScrollbarThumbVertical;
    private int mScrollbars;
    private int mScrollbarsSize;
    private int mStyle;
    private boolean mUpScroll;

    public interface ScrollMultiChoiceListener {
        void onItemTouch(int position, View view);
    }

    public COUIListView(Context context) {
        this(context, null);
    }

    public COUIListView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.listViewStyle);
    }

    public COUIListView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Widget_COUI_ListView);
    }

    public COUIListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mMultiChoice = true;
        mLastPosition = INVALID_SCROLL_CHOICE_POSITION;
        mLasterPosition = INVALID_SCROLL_CHOICE_POSITION;
        mFlag = false;
        mUpScroll = true;
        mLastSite = -1;
        mCheckItemId = -1;
        mScrollbars = SCROLLBARS_NONE;
        mEventFilterAngle = DEFAULT_INTERACTING_NESTED_SCROLL_ANGLE;
        mEnableDispatchEventWhileScrolling = false;
        mDelayedScroll = new Runnable() {
            @Override
            public void run() {
                if (mUpScroll) {
                    setSelectionFromTop(getFirstVisiblePosition() - 1, -getPaddingTop());
                } else {
                    alignBottomChild(getLastVisiblePosition() + 1, getPaddingBottom());
                }
            }
        };
        initAttr(context, attrs, defStyleAttr, defStyleRes);
        if (mScrollbars == SCROLLBARS_VERTICAL) {
            createCOUIScrollDelegate(context);
            if (mScrollbarsSize != 0) {
                mCOUIScrollBar.setThumbSize(mScrollbarsSize);
            }
            if (mScrollbarThumbVertical != null) {
                mCOUIScrollBar.setThumbDrawable(mScrollbarThumbVertical);
            }
        }
        mLeftOffset = getResources().getDimensionPixelOffset(R.dimen.coui_listview_scrollchoice_left_offset);
        mRightOffset = getResources().getDimensionPixelOffset(R.dimen.coui_listview_scrollchoice_right_offset);
    }

    private void alignBottomChild(int position, int offset) {
        setSelectionFromTop(position, (((getHeight() - getPaddingTop()) - getPaddingBottom())
                - getChildAt(getChildCount() - 1).getHeight()) + offset);
    }

    private void createCOUIScrollDelegate(Context context) {
        mCOUIScrollBar = new COUIScrollBar.Builder(this).build();
    }

    private void initAttr(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        if (attrs == null || attrs.getStyleAttribute() == 0) {
            mStyle = defStyleAttr;
        } else {
            mStyle = attrs.getStyleAttribute();
        }
        if (context != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.COUIListView,
                    defStyleAttr, defStyleRes);
            mScrollbars = a.getInteger(R.styleable.COUIListView_couiScrollbars, SCROLLBARS_NONE);
            mScrollbarsSize = a.getDimensionPixelSize(R.styleable.COUIListView_couiScrollbarSize, 0);
            mScrollbarThumbVertical = a.getDrawable(R.styleable.COUIListView_couiScrollbarThumbVertical);
            a.recycle();
        }
    }

    private boolean isInScrollRange(MotionEvent event) {
        int position = pointToPosition((int) event.getX(), (int) event.getY());
        int rawX = (int) event.getRawX();
        int[] location = new int[2];
        try {
            if (mCheckItemId <= 0) {
                mMultiChoice = false;
                return false;
            }
            CheckBox checkBox = (CheckBox) getChildAt(position - getFirstVisiblePosition())
                    .findViewById(mCheckItemId);
            checkBox.getLocationOnScreen(location);
            int left = location[0] - mLeftOffset;
            int right = location[0] + mRightOffset;
            if (checkBox.getVisibility() == VISIBLE && rawX > left && rawX < right
                    && position > getHeaderViewsCount() - 1
                    && position < getCount() - getFooterViewsCount()) {
                mMultiChoice = true;
                return true;
            }
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                mMultiChoice = false;
            }
            return false;
        } catch (Exception e) {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                mMultiChoice = false;
            }
            return false;
        }
    }

    @Override
    public boolean awakenScrollBars() {
        return mCOUIScrollBar != null ? mCOUIScrollBar.awakenScrollBars() : super.awakenScrollBars();
    }

    @Override
    public void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mCOUIScrollBar != null) {
            mCOUIScrollBar.dispatchDrawOver(canvas);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (mEnableDispatchEventWhileScrolling
                && (event.getAction() & 255) == MotionEvent.ACTION_DOWN) {
            super.onTouchEvent(event);
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public COUIScrollBar getCOUIScrollDelegate() {
        return mCOUIScrollBar;
    }

    @Override
    public View getCOUIScrollableView() {
        return this;
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mCOUIScrollBar != null) {
            mCOUIScrollBar.onAttachedToWindow();
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mCOUIScrollBar != null) {
            mCOUIScrollBar.release();
            mCOUIScrollBar = null;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (mCOUIScrollBar != null && mCOUIScrollBar.onInterceptTouchEvent(event)) {
            return true;
        }
        int action = event.getAction() & 255;
        if (action == MotionEvent.ACTION_DOWN) {
            mInitialTouchX = (int) event.getX();
            mInitialTouchY = (int) event.getY();
            if (isInScrollRange(event)) {
                return true;
            }
        }
        if (action == MotionEvent.ACTION_MOVE) {
            float deltaX = Math.abs(event.getX() - mInitialTouchX);
            float deltaY = Math.abs(event.getY() - mInitialTouchY);
            if (deltaX != 0.0f && mEnableDispatchEventWhileScrolling
                    && Math.abs(deltaY / deltaX)
                    < Math.tan(mEventFilterAngle * DEGREE_TO_ARC_CONSTANT)) {
                return false;
            }
        }
        return super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mCOUIScrollBar != null && mCOUIScrollBar.onTouchEvent(event)) {
            return true;
        }
        if (mMultiChoice && isInScrollRange(event)) {
            int position = pointToPosition((int) event.getX(), (int) event.getY());
            int actionMasked = event.getActionMasked();
            if (actionMasked == MotionEvent.ACTION_DOWN) {
                mFlag = true;
            } else if (actionMasked == MotionEvent.ACTION_UP) {
                mLastPosition = INVALID_SCROLL_CHOICE_POSITION;
                mLasterPosition = INVALID_SCROLL_CHOICE_POSITION;
            }
            if (position == getCount() - 1) {
                alignBottomChild(position, 0);
            }
            if (mFlag && mLastPosition != position && position != INVALID_POSITION
                    && mScrollMultiChoiceListener != null) {
                removeCallbacks(mDelayedScroll);
                mScrollMultiChoiceListener.onItemTouch(position,
                        getChildAt(position - getFirstVisiblePosition()));
                if (mLastPosition != INVALID_SCROLL_CHOICE_POSITION) {
                    if (position == getFirstVisiblePosition() && position > 0) {
                        mUpScroll = true;
                        postDelayed(mDelayedScroll, SCROLL_CHOICE_SCROLL_DELAY);
                    } else if (position == getLastVisiblePosition() && position < getCount()) {
                        mUpScroll = false;
                        postDelayed(mDelayedScroll, SCROLL_CHOICE_SCROLL_DELAY);
                    }
                }
                if (mLasterPosition == position) {
                    mScrollMultiChoiceListener.onItemTouch(mLastPosition,
                            getChildAt(mLastPosition - getFirstVisiblePosition()));
                }
                mLasterPosition = mLastPosition;
                mLastPosition = position;
            }
            return true;
        }
        int action = event.getAction() & 255;
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            mUpScroll = true;
            mLastPosition = INVALID_SCROLL_CHOICE_POSITION;
            mLasterPosition = INVALID_SCROLL_CHOICE_POSITION;
            mFlag = false;
            mMultiChoice = true;
            mLastSite = -1;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (mCOUIScrollBar != null) {
            mCOUIScrollBar.onVisibilityChanged(changedView, visibility);
        }
    }

    @Override
    public void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (mCOUIScrollBar != null) {
            mCOUIScrollBar.onWindowVisibilityChanged(visibility);
        }
    }

    public void refresh() {
        String resourceTypeName = getResources().getResourceTypeName(mStyle);
        TypedArray a = null;
        if ("attr".equals(resourceTypeName)) {
            a = getContext().obtainStyledAttributes(null, R.styleable.COUIListView, mStyle, 0);
        } else if ("style".equals(resourceTypeName)) {
            a = getContext().obtainStyledAttributes(null, R.styleable.COUIListView, 0, mStyle);
        }
        if (a != null) {
            mScrollbarThumbVertical = a.getDrawable(R.styleable.COUIListView_couiScrollbarThumbVertical);
            a.recycle();
        }
        if (mScrollbars == SCROLLBARS_VERTICAL) {
            if (mScrollbarThumbVertical != null) {
                mCOUIScrollBar.setThumbDrawable(mScrollbarThumbVertical);
            } else {
                mCOUIScrollBar.refreshScrollBarColor();
            }
        }
        invalidate();
    }

    public void setCheckItemId(int checkItemId) {
        mCheckItemId = checkItemId;
    }

    public void setDispatchEventWhileScrolling(boolean enabled) {
        mEnableDispatchEventWhileScrolling = enabled;
    }

    public void setEventFilterTangent(float angle) {
        mEventFilterAngle = angle;
    }

    @Override
    public void setNewCOUIScrollDelegate(COUIScrollBar scrollBar) {
        if (scrollBar == null) {
            throw new IllegalArgumentException("setNewFastScrollDelegate must NOT be NULL.");
        }
        mCOUIScrollBar = scrollBar;
        scrollBar.onAttachedToWindow();
    }

    public void setScrollMultiChoiceListener(ScrollMultiChoiceListener listener) {
        mScrollMultiChoiceListener = listener;
    }

    @Override
    public int superComputeVerticalScrollExtent() {
        return super.computeVerticalScrollExtent();
    }

    @Override
    public int superComputeVerticalScrollOffset() {
        return super.computeVerticalScrollOffset();
    }

    @Override
    public int superComputeVerticalScrollRange() {
        return super.computeVerticalScrollRange();
    }

    @Override
    public void superOnTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
    }
}
