package com.coui.appcompat.tips;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.hardware.display.DisplayManager;
import android.text.StaticLayout;
import android.util.AttributeSet;
import android.view.Display;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatTextView;

import com.coui.appcompat.R;
import com.coui.appcompat.animation.COUILinearInterpolator;
import com.coui.appcompat.log.COUILog;

import java.util.List;

public final class COUIMarqueeTextView extends AppCompatTextView {
    private static final long DEFAULT_SCROLL_DELAY_DURATION = 1000L;
    private static final String TAG = "MarqueeView";

    private float fadingEdgeStrength;
    private boolean isActualMarqueeByMeasured;
    private boolean isAllCharactersLtR = true;
    private boolean isMarqueeEnable;
    private boolean mContinueScrollingEnable;
    private float mCurrentScrollLocation;
    private String mFinalDrawText = "";
    private String mIndividuallyAssembledText = "";
    private int mIndividuallyAssembledTextWidth;
    private int mInitStringWidth;
    private String mOriginText = "";
    private int mScrollRepeatCount;
    private ValueAnimator mScroller;
    private float mScrollerSpeed;
    private StartScrollRunnable mStartScrollRunnable;
    private boolean mSuppressAccessibilityEvents;
    private final int mTextViewScrollDistance;

    public final class StartScrollRunnable implements Runnable {
        @Override
        public void run() {
            continueRoll();
        }
    }

    public COUIMarqueeTextView(Context context) {
        this(context, null);
    }

    public COUIMarqueeTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public COUIMarqueeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScrollerSpeed = getResources().getDimensionPixelOffset(R.dimen.coui_top_tips_scroll_speed);
        mCurrentScrollLocation = getResources().getDimensionPixelOffset(
                R.dimen.coui_top_tips_scroll_text_start_location);
        mTextViewScrollDistance = getResources().getDimensionPixelOffset(
                R.dimen.coui_top_tips_scroll_text_interval);
        initSpeed();
        initTextViewAttributes();
        if (isMarqueeEnable) {
            postDelayed(mStartScrollRunnable, DEFAULT_SCROLL_DELAY_DURATION);
        }
    }

    private void checkAllCharactersDirections() {
        StaticLayout layout = StaticLayout.Builder.obtain(
                mOriginText, 0, mOriginText.length(), getPaint(), getWidth()).build();
        isAllCharactersLtR = true;
        for (int i = 0; i < mOriginText.length(); i++) {
            if (layout.isRtlCharAt(i)) {
                isAllCharactersLtR = false;
                return;
            }
        }
    }

    private String generateTextDistance() {
        int count = (int) Math.ceil(mTextViewScrollDistance / getPaint().measureText(" "));
        String result = mTextViewScrollDistance != 0 ? "" : " ";
        for (int i = 0; i <= count; i++) {
            result += ' ';
        }
        return result;
    }

    private void initSpeed() {
        Display display = ((DisplayManager) getContext().getSystemService(DisplayManager.class)).getDisplay(0);
        mScrollerSpeed = getResources().getDimensionPixelOffset(R.dimen.coui_top_tips_scroll_speed)
                / display.getRefreshRate();
        mStartScrollRunnable = new StartScrollRunnable();
    }

    private void initTextViewAttributes() {
        setHorizontalFadingEdgeEnabled(true);
        setFadingEdgeLength(getResources().getDimensionPixelSize(R.dimen.coui_top_tips_fading_edge_size));
        mCurrentScrollLocation = getResources().getDimensionPixelOffset(
                R.dimen.coui_top_tips_scroll_text_start_location);
        getPaint().setColor(getCurrentTextColor());
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
    }

    private void setActualMarqueeByMeasured(boolean actual) {
        setFadingEdgeStrength((actual && isMarqueeEnable) ? 1.0f : 0.0f);
        isActualMarqueeByMeasured = actual;
    }

    private void setContent() {
        mIndividuallyAssembledText = mOriginText + generateTextDistance();
        mScrollRepeatCount = 0;
        mIndividuallyAssembledTextWidth = (int) getPaint().measureText(mIndividuallyAssembledText);
        int repeat = (int) Math.ceil((getMeasuredWidth() / (double) mIndividuallyAssembledTextWidth) + 1.0d);
        mFinalDrawText = mIndividuallyAssembledText;
        for (int i = 0; i <= repeat; i++) {
            mFinalDrawText += mIndividuallyAssembledText;
        }
        mInitStringWidth = (int) getPaint().measureText(mFinalDrawText);
        checkAllCharactersDirections();
        super.setText(mFinalDrawText, TextView.BufferType.NORMAL);
    }

    private void setFadingEdgeStrength(float value) {
        fadingEdgeStrength = Math.signum(value);
    }

    public void continueRoll() {
        setMarqueeEnable(true);
        if (getPaint().measureText(getText().toString()) <= getMeasuredWidth() || mContinueScrollingEnable) {
            return;
        }
        if (mScroller != null) {
            mScroller.cancel();
            mScroller = null;
        }
        mContinueScrollingEnable = true;
        mScroller = ValueAnimator.ofInt(Integer.MAX_VALUE);
        mScroller.setDuration(Integer.MAX_VALUE);
        mScroller.setInterpolator(new COUILinearInterpolator());
        mScroller.setRepeatCount(ValueAnimator.INFINITE);
        mScroller.setRepeatMode(ValueAnimator.RESTART);
        mScroller.addUpdateListener(animation -> {
            mCurrentScrollLocation -= mScrollerSpeed;
            invalidate();
        });
        mScroller.start();
    }

    @Override public float getLeftFadingEdgeStrength() { return fadingEdgeStrength; }
    @Override public float getRightFadingEdgeStrength() { return fadingEdgeStrength; }
    public boolean isMarqueeEnable() { return isMarqueeEnable; }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (isMarqueeEnable) {
            stopRoll();
            removeCallbacks(mStartScrollRunnable);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!isMarqueeEnable || !isActualMarqueeByMeasured) {
            COUILog.d(TAG, "onDraw: isMarqueeEnable=" + isMarqueeEnable
                    + ", isActualMarqueeByMeasured=" + isActualMarqueeByMeasured);
            super.onDraw(canvas);
            return;
        }
        if (mCurrentScrollLocation < 0.0f) {
            int repeat = (int) Math.abs(mCurrentScrollLocation / mIndividuallyAssembledTextWidth);
            if (repeat >= mScrollRepeatCount) {
                mScrollRepeatCount++;
                if (mCurrentScrollLocation <= -mInitStringWidth) {
                    mFinalDrawText = mFinalDrawText.substring(mIndividuallyAssembledText.length());
                    mCurrentScrollLocation += mIndividuallyAssembledTextWidth;
                    mScrollRepeatCount--;
                }
                mFinalDrawText += mIndividuallyAssembledText;
                mSuppressAccessibilityEvents = true;
                super.setText(mFinalDrawText, TextView.BufferType.NORMAL);
            }
        }
        if (getLayout() == null) {
            super.onDraw(canvas);
            return;
        }
        canvas.save();
        float scroll = isAllCharactersLtR ? mCurrentScrollLocation : -mCurrentScrollLocation;
        canvas.translate(scroll, 0.0f);
        getLayout().draw(canvas);
        canvas.restore();
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        if (!mSuppressAccessibilityEvents) {
            super.onInitializeAccessibilityEvent(event);
            return;
        }
        if (event != null) {
            List<CharSequence> text = event.getText();
            if (text != null) {
                text.clear();
            }
            event.setContentDescription(null);
        }
        mSuppressAccessibilityEvents = false;
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setText(mOriginText);
        info.setContentDescription(mOriginText);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (getPaint().measureText(getText().toString()) <= getMeasuredWidth()) {
            setActualMarqueeByMeasured(false);
            return;
        }
        setActualMarqueeByMeasured(true);
        if (isMarqueeEnable) {
            setContent();
        }
    }

    public void setMarqueeEnable(boolean enabled) {
        if (enabled) {
            setSingleLine(true);
            setMaxLines(1);
            setFadingEdgeStrength(1.0f);
        } else {
            setSingleLine(false);
            setMaxLines(Integer.MAX_VALUE);
            setFadingEdgeStrength(0.0f);
        }
        isMarqueeEnable = enabled;
    }

    @Override
    public void setText(CharSequence text, TextView.BufferType type) {
        mOriginText = String.valueOf(text);
        super.setText(text, type);
    }

    @Override
    public void setTextColor(int color) {
        super.setTextColor(color);
        getPaint().setColor(getCurrentTextColor());
    }

    public void stopRoll() {
        mContinueScrollingEnable = false;
        mCurrentScrollLocation = getResources().getDimensionPixelOffset(
                R.dimen.coui_top_tips_scroll_text_start_location);
        if (mScroller != null) {
            mScroller.cancel();
        }
        mScroller = null;
    }
}
