package com.coui.appcompat.scrollbar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Interpolator;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.StateListDrawable;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;

import androidx.core.view.ViewCompat;

import com.coui.appcompat.R;
import com.coui.appcompat.contextutil.COUIContextUtil;
import com.coui.appcompat.darkmode.COUIDarkModeUtil;

import java.util.Locale;

public class COUIScrollBar {
    public static final long SCROLLER_FADE_TIMEOUT = 2000;
    private static final int[] DRAWABLE_STATE_PRESSED = {android.R.attr.state_pressed};
    private static final int[] DRAWABLE_STATE_DEFAULT = new int[0];

    private OnCOUIScrollListener mCOUIScrollListener;
    private final COUIScrollable mCOUIScrollable;
    private final float mDensity;
    private float mDownY;
    private boolean mIfShowWhenStateChange = true;
    private boolean mIsDragging;
    private boolean mIsRTL;
    private final ScrollabilityCache mScrollCache;
    private Drawable mThumbDrawable;
    private boolean mThumbDynamicHeight;
    private int mThumbMinHeight;
    private final Rect mThumbRect;
    private View mView;

    public static class Builder {
        private final COUIScrollable couiScrollable;
        private int height;
        private boolean isDynamicHeight = true;
        public int scrollbar_drawable_default_inset;
        public int scrollbar_drawable_pressed_inset;
        public int scroller_margin_bottom;
        public int scroller_margin_top;
        public int scroller_min_height;
        public int scroller_width;
        private Drawable thumbDrawable;
        private int thumbNormalColor;
        private int thumbPressedColor;
        private int width;

        public Builder(COUIScrollable cOUIScrollable) {
            couiScrollable = cOUIScrollable;
            Context context = cOUIScrollable.getCOUIScrollableView().getContext();
            width = context.getResources().getDimensionPixelSize(R.dimen.coui_scrollbar_wight);
            height = context.getResources().getDimensionPixelSize(R.dimen.coui_scrollbar_min_height);
            scroller_margin_top = context.getResources().getDimensionPixelSize(R.dimen.coui_scrollbar_margin_top);
            scroller_margin_bottom = context.getResources().getDimensionPixelSize(R.dimen.coui_scrollbar_margin_bottom);
            scrollbar_drawable_default_inset = context.getResources().getDimensionPixelSize(R.dimen.coui_scrollbar_drawable_default_inset);
            scrollbar_drawable_pressed_inset = context.getResources().getDimensionPixelSize(R.dimen.coui_scrollbar_drawable_pressed_inset);
            thumbNormalColor = COUIContextUtil.getColor(context, R.color.coui_scrollbar_color);
            thumbPressedColor = COUIContextUtil.getColor(context, R.color.coui_scrollbar_color);
        }

        private Drawable makeDefaultThumbDrawable() {
            StateListDrawable stateListDrawable = new StateListDrawable();
            GradientDrawable pressedDrawable = new GradientDrawable();
            pressedDrawable.setColor(thumbPressedColor);
            float radius = width / 2.0f;
            pressedDrawable.setCornerRadius(radius);
            int pressedInset = scrollbar_drawable_pressed_inset;
            stateListDrawable.addState(DRAWABLE_STATE_PRESSED,
                    new InsetDrawable(pressedDrawable, pressedInset, scroller_margin_top,
                            pressedInset, scroller_margin_bottom));

            GradientDrawable normalDrawable = new GradientDrawable();
            normalDrawable.setColor(thumbNormalColor);
            normalDrawable.setCornerRadius(radius);
            int defaultInset = scrollbar_drawable_default_inset;
            stateListDrawable.addState(DRAWABLE_STATE_DEFAULT,
                    new InsetDrawable(normalDrawable, defaultInset, scroller_margin_top,
                            defaultInset, scroller_margin_bottom));
            return stateListDrawable;
        }

        public COUIScrollBar build() {
            if (thumbDrawable == null) {
                thumbDrawable = makeDefaultThumbDrawable();
            }
            return new COUIScrollBar(couiScrollable, width, height, thumbDrawable, isDynamicHeight);
        }

        public Builder dynamicHeight(boolean dynamicHeight) {
            isDynamicHeight = dynamicHeight;
            return this;
        }

        public Builder height(int height) {
            this.height = height;
            return this;
        }

        public Builder marginBottom(int marginBottom) {
            scroller_margin_bottom = marginBottom;
            return this;
        }

        public Builder marginTop(int marginTop) {
            scroller_margin_top = marginTop;
            return this;
        }

        public Builder thumbDrawable(Drawable drawable) {
            thumbDrawable = drawable;
            return this;
        }

        public Builder width(int width) {
            this.width = width;
            return this;
        }
    }

    public interface COUIScrollable {
        COUIScrollBar getCOUIScrollDelegate();
        View getCOUIScrollableView();
        void setNewCOUIScrollDelegate(COUIScrollBar cOUIScrollBar);
        int superComputeVerticalScrollExtent();
        int superComputeVerticalScrollOffset();
        int superComputeVerticalScrollRange();
        void superOnTouchEvent(MotionEvent motionEvent);
    }

    public interface OnCOUIScrollListener {
        void onCOUIScrollEnd(View view, COUIScrollBar cOUIScrollBar);
        void onCOUIScrollStart(View view, COUIScrollBar cOUIScrollBar);
        void onCOUIScrolled(View view, COUIScrollBar cOUIScrollBar, int index, int index_2, float value);
    }

    public static class ScrollabilityCache implements Runnable {
        public static final int FADING = 2;
        public static final int OFF = 0;
        public static final int ON = 1;
        private static final float[] OPAQUE = {255.0f};
        private static final float[] TRANSPARENT = {0.0f};
        public long fadeStartTime;
        public View host;
        public float[] interpolatorValues;
        public final int RUNNABLE_RETRY_MIN_TIME = 50;
        public final Interpolator scrollBarInterpolator = new Interpolator(1, 2);
        public int state = OFF;
        public final int scrollBarDefaultDelayBeforeFade = ViewConfiguration.getScrollDefaultDelay();
        public final int scrollBarFadeDuration = ViewConfiguration.getScrollBarFadeDuration();

        public ScrollabilityCache(ViewConfiguration viewConfiguration, View view) {
            host = view;
        }

        @Override
        public void run() {
            View view;
            long currentTime = AnimationUtils.currentAnimationTimeMillis();
            if (currentTime < fadeStartTime) {
                if (Math.abs(currentTime - fadeStartTime) < RUNNABLE_RETRY_MIN_TIME
                        && (view = host) != null) {
                    view.post(this);
                }
                return;
            }
            int keyTime = (int) currentTime;
            scrollBarInterpolator.setKeyFrame(0, keyTime, OPAQUE);
            scrollBarInterpolator.setKeyFrame(1, keyTime + scrollBarFadeDuration, TRANSPARENT);
            state = FADING;
            host.invalidate();
        }
    }

    private COUIScrollBar(COUIScrollable cOUIScrollable, int width, int height,
            Drawable drawable, boolean dynamicHeight) {
        View scrollableView = cOUIScrollable.getCOUIScrollableView();
        mView = scrollableView;
        scrollableView.setVerticalScrollBarEnabled(false);
        COUIDarkModeUtil.setForceDarkAllow(mView, false);
        Context context = mView.getContext();
        mIsRTL = TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == View.LAYOUT_DIRECTION_RTL;
        mDensity = context.getResources().getDisplayMetrics().density;
        mThumbMinHeight = context.getResources().getDimensionPixelSize(R.dimen.coui_scrollbar_min_height);
        mThumbRect = new Rect(0, 0, width, height);
        mThumbDrawable = drawable;
        mCOUIScrollable = cOUIScrollable;
        mScrollCache = new ScrollabilityCache(ViewConfiguration.get(context), mView);
        mThumbDynamicHeight = dynamicHeight;
    }

    private int dp2px(float value) {
        return (int) ((mDensity * value) + 0.5f);
    }

    private void findAndUploadDrawableColor(StateListDrawable drawable, int index, int color) {
        Drawable stateDrawable = drawable.getStateDrawable(index);
        if (stateDrawable instanceof InsetDrawable) {
            Drawable child = ((InsetDrawable) stateDrawable).getDrawable();
            if (child instanceof GradientDrawable) {
                ((GradientDrawable) child).setColor(color);
            }
        }
    }

    private boolean initialAwakenScrollBars() {
        return awakenScrollBars(((long) mScrollCache.scrollBarDefaultDelayBeforeFade) * 4);
    }

    private void onDrawScrollBars(Canvas canvas) {
        boolean invalidate = false;
        if (mIsDragging) {
            mThumbDrawable.setAlpha(255);
        } else {
            ScrollabilityCache cache = mScrollCache;
            int state = cache.state;
            if (state == ScrollabilityCache.OFF) {
                return;
            }
            if (state == ScrollabilityCache.FADING) {
                if (cache.interpolatorValues == null) {
                    cache.interpolatorValues = new float[1];
                }
                float[] values = cache.interpolatorValues;
                Interpolator.Result result = cache.scrollBarInterpolator.timeToValues(values);
                if (result == Interpolator.Result.FREEZE_END) {
                    cache.state = ScrollabilityCache.OFF;
                } else {
                    mThumbDrawable.setAlpha(Math.round(values[0]));
                    invalidate = true;
                }
            } else {
                mThumbDrawable.setAlpha(255);
            }
        }
        if (updateThumbRect(0)) {
            int scrollY = mView.getScrollY();
            int scrollX = mView.getScrollX();
            Rect rect = mThumbRect;
            mThumbDrawable.setBounds(rect.left + scrollX, rect.top + scrollY,
                    rect.right + scrollX, rect.bottom + scrollY);
            mThumbDrawable.draw(canvas);
        }
        if (invalidate) {
            mView.invalidate();
        }
    }

    private boolean onInterceptTouchEventInternal(MotionEvent motionEvent) {
        if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {
            return onTouchEventInternal(motionEvent);
        }
        return false;
    }

    private boolean onTouchEventInternal(MotionEvent motionEvent) {
        int action = motionEvent.getActionMasked();
        float y = motionEvent.getY();
        if (action == MotionEvent.ACTION_DOWN) {
            if (mScrollCache.state == ScrollabilityCache.OFF) {
                mIsDragging = false;
                return false;
            }
            if (!mIsDragging) {
                updateThumbRect(0);
                float x = motionEvent.getX();
                Rect rect = mThumbRect;
                if (y >= rect.top && y <= rect.bottom && x >= rect.left && x <= rect.right) {
                    mIsDragging = true;
                    mDownY = y;
                    mCOUIScrollable.superOnTouchEvent(motionEvent);
                    MotionEvent cancel = MotionEvent.obtain(motionEvent);
                    cancel.setAction(MotionEvent.ACTION_CANCEL);
                    mCOUIScrollable.superOnTouchEvent(cancel);
                    cancel.recycle();
                    setPressedThumb(true);
                    updateThumbRect(0, true);
                    mView.removeCallbacks(mScrollCache);
                }
            }
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            if (mIsDragging) {
                setPressedThumb(false);
                mIsDragging = false;
                awakenScrollBars();
            }
        } else if (action == MotionEvent.ACTION_MOVE && mIsDragging) {
            int dy = Math.round(y - mDownY);
            if (dy != 0) {
                updateThumbRect(dy);
                mDownY = y;
            }
        }
        if (mIsDragging) {
            mView.invalidate();
            mView.getParent().requestDisallowInterceptTouchEvent(true);
            return true;
        }
        return false;
    }

    private void setPressedThumb(boolean pressed) {
        mThumbDrawable.setState(pressed ? DRAWABLE_STATE_PRESSED : DRAWABLE_STATE_DEFAULT);
        mView.invalidate();
        OnCOUIScrollListener listener = mCOUIScrollListener;
        if (listener != null) {
            if (pressed) {
                listener.onCOUIScrollStart(mView, this);
            } else {
                listener.onCOUIScrollEnd(mView, this);
            }
        }
    }

    private boolean updateThumbRect(int dy) {
        return updateThumbRect(dy, false);
    }

    private boolean updateThumbRect(int dy, boolean notify) {
        OnCOUIScrollListener listener;
        int width = mThumbRect.width();
        mThumbRect.right = mIsRTL ? width : mView.getWidth();
        Rect rect = mThumbRect;
        rect.left = mIsRTL ? 0 : rect.right - width;
        int range = mCOUIScrollable.superComputeVerticalScrollRange();
        if (range <= 0) {
            return false;
        }
        int offset = mCOUIScrollable.superComputeVerticalScrollOffset();
        int extent = mCOUIScrollable.superComputeVerticalScrollExtent();
        int scrollableRange = range - extent;
        if (scrollableRange <= 0) {
            return false;
        }
        float scrollableRangeFloat = scrollableRange;
        float offsetFraction = (offset * 1.0f) / scrollableRangeFloat;
        float extentFraction = (extent * 1.0f) / range;
        int height = mView.getHeight();
        int thumbHeight = mThumbDynamicHeight
                ? Math.max(mThumbMinHeight, Math.round(extentFraction * height))
                : mThumbMinHeight;
        Rect thumbRect = mThumbRect;
        thumbRect.bottom = thumbRect.top + thumbHeight;
        int maxTop = height - thumbHeight;
        float maxTopFloat = maxTop;
        int currentTop = Math.round(maxTopFloat * offsetFraction);
        Rect thumbRect2 = mThumbRect;
        thumbRect2.offsetTo(thumbRect2.left, currentTop);
        if (dy == 0) {
            if (notify && (listener = mCOUIScrollListener) != null) {
                listener.onCOUIScrolled(mView, this, 0, 0, offsetFraction);
            }
            return true;
        }
        int nextTop = currentTop + dy;
        if (nextTop <= maxTop) {
            maxTop = nextTop < 0 ? 0 : nextTop;
        }
        float fraction = (maxTop * 1.0f) / maxTopFloat;
        int scrollBy = Math.round(scrollableRangeFloat * fraction) - offset;
        View view = mView;
        if (view instanceof AbsListView) {
            ((AbsListView) view).smoothScrollBy(scrollBy, 0);
        } else {
            view.scrollBy(0, scrollBy);
        }
        OnCOUIScrollListener list = mCOUIScrollListener;
        if (list != null) {
            list.onCOUIScrolled(mView, this, dy, scrollBy, fraction);
        }
        return true;
    }

    public boolean awakenScrollBars() {
        return awakenScrollBars(SCROLLER_FADE_TIMEOUT);
    }

    public boolean awakenScrollBars(long delay) {
        ViewCompat.postInvalidateOnAnimation(mView);
        if (mIsDragging) {
            return false;
        }
        if (mScrollCache.state == ScrollabilityCache.OFF) {
            delay = Math.max(750L, delay);
        }
        long fadeStart = AnimationUtils.currentAnimationTimeMillis() + delay;
        mScrollCache.fadeStartTime = fadeStart;
        mScrollCache.state = ScrollabilityCache.ON;
        mView.removeCallbacks(mScrollCache);
        mView.postDelayed(mScrollCache, fadeStart - AnimationUtils.currentAnimationTimeMillis());
        return false;
    }

    public void dispatchDrawOver(Canvas canvas) {
        onDrawScrollBars(canvas);
    }

    public View getView() {
        return mView;
    }

    public void onAttachedToWindow() {
        if (mIfShowWhenStateChange) {
            initialAwakenScrollBars();
        }
    }

    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        return onInterceptTouchEventInternal(motionEvent);
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        return onTouchEventInternal(motionEvent);
    }

    public void onVisibilityChanged(View view, int visibility) {
        if (mIfShowWhenStateChange && visibility == View.VISIBLE && ViewCompat.isAttachedToWindow(mView)) {
            initialAwakenScrollBars();
        }
    }

    public void onWindowVisibilityChanged(int visibility) {
        if (mIfShowWhenStateChange && visibility == View.VISIBLE) {
            initialAwakenScrollBars();
        }
    }

    public void refreshScrollBarColor() {
        Drawable drawable = mThumbDrawable;
        if (drawable instanceof StateListDrawable) {
            StateListDrawable stateListDrawable = (StateListDrawable) drawable;
            if (stateListDrawable.getStateCount() < 1) {
                return;
            }
            Context context = mView.getContext();
            findAndUploadDrawableColor(stateListDrawable, 0,
                    COUIContextUtil.getColor(context, R.color.coui_scrollbar_color));
            findAndUploadDrawableColor(stateListDrawable, 1,
                    COUIContextUtil.getColor(context, R.color.coui_scrollbar_color));
        }
    }

    public void release() {
        mView = null;
    }

    public void setIfShowWhenStateChange(boolean ifShowWhenStateChange) {
        mIfShowWhenStateChange = ifShowWhenStateChange;
    }

    public void setOnCOUIScrollListener(OnCOUIScrollListener listener) {
        mCOUIScrollListener = listener;
    }

    public void setThumbDrawable(Drawable drawable) {
        if (drawable == null) {
            throw new IllegalArgumentException("setThumbDrawable must NOT be NULL");
        }
        mThumbDrawable = drawable;
        updateThumbRect(0);
    }

    public void setThumbDynamicHeight(boolean dynamicHeight) {
        if (mThumbDynamicHeight != dynamicHeight) {
            mThumbDynamicHeight = dynamicHeight;
            updateThumbRect(0);
        }
    }

    public void setThumbSize(int size) {
        Rect rect = mThumbRect;
        rect.left = rect.right - size;
        updateThumbRect(0);
    }
}
