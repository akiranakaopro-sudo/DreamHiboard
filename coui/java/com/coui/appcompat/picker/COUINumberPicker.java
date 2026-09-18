package com.coui.appcompat.picker;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeProvider;
import android.view.animation.PathInterpolator;
import android.widget.LinearLayout;
import android.widget.Scroller;

import androidx.annotation.Nullable;

import com.coui.appcompat.R;
import com.coui.appcompat.contextutil.COUIContextUtil;
import com.coui.appcompat.darkmode.COUIDarkModeUtil;
import com.coui.appcompat.hapticfeedback.COUIHapticFeedbackConstants;
import com.coui.appcompat.math.COUIMathUtils;
import com.coui.appcompat.soundloadutil.COUISoundLoadUtil;
import com.coui.appcompat.tooltips.COUIToolTips;
import com.coui.appcompat.vibrateutil.VibrateUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class COUINumberPicker extends LinearLayout {
    public static final int ALIGN_MIDDLE = 0;
    public static final int ALIGN_LEFT = 1;
    public static final int ALIGN_RIGHT = 2;
    public static final int SELECTOR_INDEX_IGNORE = Integer.MIN_VALUE;
    public static final int VIBRATE_LEVEL_CRISP = 0;
    public static final int VIBRATE_LEVEL_SOFT = 1;

    private static final int DEFAULT_ROW_COUNT = 5;
    private static final float DECELERATION_RATE = (float) (Math.log(0.78d) / Math.log(0.9d));
    private static final float INFLEXION = 0.35f;
    private static final int MAX_VELOCITY = 5000;
    private static final int MINIMUM_FLING_VELOCITY = 750;
    private static final int SNAP_SCROLL_DURATION = 300;
    // Leapy added 2026-07-24: BEGIN decoded OPPO picker fling velocity multiplier.
    private static final float VELOCITY_SPEED_UP_RATIO = 1.8f;
    // Leapy end 2026-07-24: decoded OPPO picker fling velocity multiplier.
    private static final String TAG = "COUINumberPicker";
    private static final int MSG_PLAY_SOUND = 0;
    private static final PathInterpolator FLING_INTERPOLATOR = new PathInterpolator(0.0f, 0.0f, 0.4f, 1.0f);
    private static final PathInterpolator SLOW_FLING_INTERPOLATOR = new PathInterpolator(0.0f, 0.23f, 0.1f, 1.0f);

    private final Paint selectorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dividerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Scroller flingScroller;
    private final Scroller adjustScroller;
    private final SparseArray<String> selectorCache = new SparseArray<>();
    private final int touchSlop;
    private final int minimumFlingVelocity;
    private final int maximumFlingVelocity;
    private final float flingFriction;
    private final float physicalCoeff;
    private final AccessibilityManager accessibilityManager;
    private AccessibilityNodeProviderImpl accessibilityNodeProvider;
    private final COUISoundLoadUtil soundUtil;
    private final int clickSoundId;
    private Handler handler;
    private NumberPickerHandlerThread touchEffectThread;

    private int minValue;
    private int maxValue;
    private int value;
    private int selectorItemCount = DEFAULT_ROW_COUNT;
    private int selectorMiddleItemIndex = DEFAULT_ROW_COUNT / 2;
    private int[] selectorIndices = new int[DEFAULT_ROW_COUNT];
    private int selectorTextGapHeight;
    private int selectorElementHeight;
    private int currentScrollOffset;
    private int topSelectionDividerTop;
    private int bottomSelectionDividerBottom;
    private int previousScrollerY;
    private float lastDownY;
    private float lastMoveY;
    // Leapy added 2026-07-24: BEGIN preserve decoded OPPO drag distance in the fling alignment calculation.
    private int deltaMoveY;
    // Leapy end 2026-07-24: decoded OPPO picker fling phase.
    private int previousTime;
    private int scrollerVelocity;
    private int lastHandledDownDpadKeyCode = -1;
    private int lastHoveredChildVirtualViewId = Integer.MIN_VALUE;
    private VelocityTracker velocityTracker;
    private boolean dragging;
    private boolean performClickOnTap;
    private long lastDownEventTime;
    private long longPressUpdateInterval = 300L;
    private final PressedStateHelper pressedStateHelper = new PressedStateHelper();
    private ChangeCurrentByOneFromLongPressCommand changeCurrentByOneFromLongPressCommand;
    private boolean wrapSelectorWheel = true;
    private boolean ignorable;
    private boolean hasBackground;
    private boolean enableAdaptiveVibrator = true;
    private boolean verticalFadingEdgeEnabled;
    private int normalTextColor;
    private int focusTextColor;
    private int alphaStart;
    private int alphaEnd;
    private int redStart;
    private int redEnd;
    private int greenStart;
    private int greenEnd;
    private int blueStart;
    private int blueEnd;
    private int backgroundColor;
    private int normalTextSize;
    private int focusTextSize;
    private int visualWidth;
    private int minHeight;
    private int maxHeight;
    private int minWidth;
    private int maxWidth;
    private int maxViewWidth;
    private int numberPickerPaddingLeft;
    private int numberPickerPaddingRight;
    private int selectedValueWidth;
    private int unitMargin;
    private int unitTextSize;
    private int unitMinWidth;
    private int unitMarginBottom;
    private int backgroundDividerHeight;
    private int backgroundRadius;
    private float diffusion;
    private float normalTextTop;
    private float normalTextBottom;
    private int alignPosition;
    private int pickerOffset;
    private int drawItemOffsetY;
    private int initTextMargin;
    private int textMargin;
    private int gradientPositionTop;
    private int gradientPositionBottom;
    private int touchEffectInterval;
    private int vibrateLevel;
    private float vibrateIntensity = 1.0f;
    private String[] displayedValues;
    private String unitText = "";
    private String talkbackSuffix = "";
    private Formatter formatter;
    private TwoDigitFormatter twoDigitFormatter;
    private OnValueChangeListener onValueChangeListener;
    private OnScrollListener onScrollListener;
    private OnScrollingStopListener onScrollingStopListener;
    private int scrollState = OnScrollListener.SCROLL_STATE_IDLE;

    public interface Formatter {
        String format(int value);
    }

    public interface OnValueChangeListener {
        void onValueChange(COUINumberPicker picker, int oldVal, int newVal);
    }

    public interface OnScrollListener {
        int SCROLL_STATE_IDLE = 0;
        int SCROLL_STATE_TOUCH_SCROLL = 1;
        int SCROLL_STATE_FLING = 2;

        void onScrollStateChange(COUINumberPicker view, int scrollState);
    }

    public interface OnScrollingStopListener {
        void onScrollingStop();
    }

    public static class TwoDigitFormatter implements Formatter {
        @Override
        public String format(int value) {
            return String.format(Locale.getDefault(), "%02d", value);
        }
    }

    private final class AccessibilityNodeProviderImpl extends AccessibilityNodeProvider {
        private static final int VIRTUAL_VIEW_ID_NUMBER_PICKER = -1;
        private final Rect tempRect = new Rect();
        private final int[] tempArray = new int[2];
        private int accessibilityFocusedView = Integer.MIN_VALUE;

        private AccessibilityNodeInfo createAccessibilityNodeInfoForNumberPicker(String text,
                                                                                 int left,
                                                                                 int top,
                                                                                 int right,
                                                                                 int bottom) {
            AccessibilityNodeInfo info = AccessibilityNodeInfo.obtain();
            info.setClassName(COUINumberPicker.class.getName());
            info.setPackageName(getContext().getPackageName());
            info.setSource(COUINumberPicker.this);
            String nodeText = appendTalkbackSuffix(text);
            info.setText(nodeText);
            info.setStateDescription(nodeText);
            info.setParent((View) getParentForAccessibility());
            info.setEnabled(isEnabled());
            info.setScrollable(true);
            info.setFocusable(true);
            info.setAccessibilityFocused(accessibilityFocusedView == VIRTUAL_VIEW_ID_NUMBER_PICKER);
            tempRect.set(left, top, right, bottom);
            info.setBoundsInParent(tempRect);
            info.setVisibleToUser(isShown());
            getLocationOnScreen(tempArray);
            tempRect.offset(tempArray[0], tempArray[1]);
            info.setBoundsInScreen(tempRect);
            if (accessibilityFocusedView != VIRTUAL_VIEW_ID_NUMBER_PICKER) {
                info.addAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
            }
            if (accessibilityFocusedView == VIRTUAL_VIEW_ID_NUMBER_PICKER) {
                info.addAction(AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS);
            }
            if (isEnabled()) {
                info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SET_PROGRESS);
                info.setRangeInfo(AccessibilityNodeInfo.RangeInfo.obtain(
                        AccessibilityNodeInfo.RangeInfo.RANGE_TYPE_INT,
                        getMinValue() - 1,
                        getMaxValue() + 1,
                        getValue()));
                if (getWrapSelectorWheel() || getValue() < getMaxValue()) {
                    info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD);
                    info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_DOWN);
                }
                if (getWrapSelectorWheel() || getValue() > getMinValue()) {
                    info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_BACKWARD);
                    info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_UP);
                }
            }
            return info;
        }

        private void findAccessibilityNodeInfosByTextInChild(String searched, int virtualViewId, List<AccessibilityNodeInfo> outInfos) {
            String virtualText;
            if (virtualViewId == 1) {
                virtualText = getVirtualText(value + 1);
            } else if (virtualViewId == 3) {
                virtualText = getVirtualText(value - 1);
            } else {
                return;
            }
            if (!TextUtils.isEmpty(virtualText) && virtualText.toLowerCase(Locale.getDefault()).contains(searched)) {
                outInfos.add(createAccessibilityNodeInfo(VIRTUAL_VIEW_ID_NUMBER_PICKER));
            }
        }

        private String getVirtualText(int virtualValue) {
            int resolved = virtualValue;
            if (wrapSelectorWheel) {
                resolved = getWrappedSelectorIndex(virtualValue, 0);
            }
            if (resolved > maxValue || resolved < minValue || resolved == SELECTOR_INDEX_IGNORE) {
                return null;
            }
            return displayedValues == null ? formatValue(resolved) : displayedValues[resolved - minValue];
        }

        private void sendAccessibilityEventForVirtualText(int eventType, String text) {
            if (accessibilityManager == null || !accessibilityManager.isEnabled()) {
                return;
            }
            AccessibilityEvent event = AccessibilityEvent.obtain(eventType);
            event.setPackageName(getContext().getPackageName());
            event.getText().add(appendTalkbackSuffix(text));
            event.setEnabled(isEnabled());
            event.setSource(COUINumberPicker.this, VIRTUAL_VIEW_ID_NUMBER_PICKER);
            requestSendAccessibilityEvent(COUINumberPicker.this, event);
        }

        @Override
        public AccessibilityNodeInfo createAccessibilityNodeInfo(int virtualViewId) {
            if (virtualViewId != VIRTUAL_VIEW_ID_NUMBER_PICKER) {
                return super.createAccessibilityNodeInfo(virtualViewId);
            }
            return createAccessibilityNodeInfoForNumberPicker(
                    getVirtualText(value),
                    getScrollX(),
                    getScrollY(),
                    getScrollX() + (getRight() - getLeft()),
                    getScrollY() + (getBottom() - getTop()));
        }

        @Override
        public List<AccessibilityNodeInfo> findAccessibilityNodeInfosByText(String text, int virtualViewId) {
            if (TextUtils.isEmpty(text)) {
                return Collections.emptyList();
            }
            String searched = text.toLowerCase(Locale.getDefault());
            ArrayList<AccessibilityNodeInfo> result = new ArrayList<>();
            if (virtualViewId == VIRTUAL_VIEW_ID_NUMBER_PICKER) {
                findAccessibilityNodeInfosByTextInChild(searched, 3, result);
                findAccessibilityNodeInfosByTextInChild(searched, 2, result);
                findAccessibilityNodeInfosByTextInChild(searched, 1, result);
                return result;
            }
            if (virtualViewId != 1 && virtualViewId != 2 && virtualViewId != 3) {
                return super.findAccessibilityNodeInfosByText(text, virtualViewId);
            }
            findAccessibilityNodeInfosByTextInChild(searched, virtualViewId, result);
            return result;
        }

        @Override
        public boolean performAction(int virtualViewId, int action, Bundle arguments) {
            if (virtualViewId == VIRTUAL_VIEW_ID_NUMBER_PICKER) {
                if (action == AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS) {
                    if (accessibilityFocusedView == virtualViewId) {
                        return false;
                    }
                    accessibilityFocusedView = virtualViewId;
                    sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED);
                    return true;
                }
                if (action == AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS) {
                    if (accessibilityFocusedView != virtualViewId) {
                        return false;
                    }
                    accessibilityFocusedView = Integer.MIN_VALUE;
                    sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED);
                    return true;
                }
                if (action == AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
                        || action == AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_DOWN.getId()) {
                    if (!isEnabled()) {
                        return false;
                    }
                    notifyScrollState(OnScrollListener.SCROLL_STATE_FLING);
                    changeValueByOne(true);
                    return true;
                }
                if (action == AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
                        || action == AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_UP.getId()) {
                    if (!isEnabled()) {
                        return false;
                    }
                    notifyScrollState(OnScrollListener.SCROLL_STATE_FLING);
                    changeValueByOne(false);
                    return true;
                }
            }
            return super.performAction(virtualViewId, action, arguments);
        }

        void sendAccessibilityEventForVirtualView(int virtualViewId, int eventType) {
            if (virtualViewId == VIRTUAL_VIEW_ID_NUMBER_PICKER) {
                sendAccessibilityEventForVirtualText(eventType, getVirtualText(value));
            }
        }
    }

    private final class ChangeCurrentByOneFromLongPressCommand implements Runnable {
        private boolean increment;

        void setStep(boolean increment) {
            this.increment = increment;
        }

        @Override
        public void run() {
            changeValueByOne(increment);
            postDelayed(this, longPressUpdateInterval);
        }
    }

    private final class PressedStateHelper implements Runnable {
        static final int BUTTON_INCREMENT = 1;
        static final int BUTTON_DECREMENT = 2;
        private static final int MODE_PRESS = 1;
        private static final int MODE_TAPPED = 2;

        private int managedButton;
        private int mode;

        void buttonPressDelayed(int button) {
            cancel();
            mode = MODE_PRESS;
            managedButton = button;
            postDelayed(this, ViewConfiguration.getTapTimeout());
        }

        void buttonTapped(int button) {
            cancel();
            mode = MODE_TAPPED;
            managedButton = button;
            post(this);
        }

        void cancel() {
            mode = 0;
            managedButton = 0;
            removeCallbacks(this);
        }

        @Override
        public void run() {
            if (mode == MODE_PRESS) {
                invalidate();
                return;
            }
            if (mode != MODE_TAPPED) {
                return;
            }
            postDelayed(this, ViewConfiguration.getPressedStateDuration());
            invalidate();
        }
    }

    private final class TouchEffectHandler extends Handler {
        TouchEffectHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message message) {
            if (message.what == MSG_PLAY_SOUND) {
                playSoundEffect();
            }
            super.handleMessage(message);
        }
    }

    public COUINumberPicker(Context context) {
        this(context, null);
    }

    public COUINumberPicker(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.couiNumberPickerStyle);
    }

    public COUINumberPicker(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr,
                COUIContextUtil.isCOUIDarkTheme(context)
                        ? R.style.COUINumberPicker_Dark
                        : R.style.COUINumberPicker);
    }

    public COUINumberPicker(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        COUIDarkModeUtil.setForceDarkAllow(this, false);
        setWillNotDraw(false);
        setFocusable(true);
        setFocusableInTouchMode(true);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.COUINumberPicker, defStyleAttr, defStyleRes);
        setPickerRowNumber(a.getInteger(R.styleable.COUINumberPicker_couiPickerRowNumber, 3));
        normalTextColor = a.getColor(R.styleable.COUINumberPicker_couiNormalTextColor, 0x66000000);
        focusTextColor = a.getColor(R.styleable.COUINumberPicker_couiFocusTextColor, 0xde000000);
        setGradientColor(normalTextColor, focusTextColor);
        backgroundColor = a.getColor(R.styleable.COUINumberPicker_couiPickerBackgroundColor, 0x1f000000);
        normalTextSize = a.getDimensionPixelSize(R.styleable.COUINumberPicker_startTextSize,
                getResources().getDimensionPixelSize(R.dimen.coui_numberpicker_textSize_small));
        focusTextSize = a.getDimensionPixelSize(R.styleable.COUINumberPicker_focusTextSize,
                getResources().getDimensionPixelSize(R.dimen.coui_numberpicker_textSize_big));
        visualWidth = a.getDimensionPixelSize(R.styleable.COUINumberPicker_couiPickerVisualWidth, -1);
        minHeight = a.getDimensionPixelSize(R.styleable.COUINumberPicker_internalMinHeight, -1);
        maxHeight = a.getDimensionPixelSize(R.styleable.COUINumberPicker_internalMaxHeight, -1);
        if (minHeight != -1 && maxHeight != -1 && minHeight > maxHeight) {
            throw new IllegalArgumentException("minHeight > maxHeight");
        }
        minWidth = a.getDimensionPixelSize(R.styleable.COUINumberPicker_internalMinWidth, -1);
        maxWidth = a.getDimensionPixelSize(R.styleable.COUINumberPicker_internalMaxWidth, -1);
        if (minWidth != -1 && maxWidth != -1 && minWidth > maxWidth) {
            throw new IllegalArgumentException("minWidth > maxWidth");
        }
        numberPickerPaddingLeft = a.getDimensionPixelSize(R.styleable.COUINumberPicker_couiNOPickerPaddingLeft, 0);
        numberPickerPaddingRight = a.getDimensionPixelSize(R.styleable.COUINumberPicker_couiNOPickerPaddingRight, 0);
        hasBackground = a.getBoolean(R.styleable.COUINumberPicker_couiIsDrawBackground, false);
        alignPosition = a.getInteger(R.styleable.COUINumberPicker_couiPickerAlignPosition, -1);
        touchEffectInterval = a.getInteger(R.styleable.COUINumberPicker_couiPickerTouchEffectInterval, 50);
        enableAdaptiveVibrator = a.getBoolean(R.styleable.COUINumberPicker_couiPickerAdaptiveVibrator, true);
        verticalFadingEdgeEnabled = a.getBoolean(R.styleable.COUINumberPicker_couiPickerVerticalFading, false);
        vibrateLevel = a.getInteger(R.styleable.COUINumberPicker_couiVibrateLevel, VIBRATE_LEVEL_CRISP);
        diffusion = a.getDimensionPixelOffset(R.styleable.COUINumberPicker_couiPickerDiffusion, 0);
        a.recycle();

        TypedArray commonAttrs = context.obtainStyledAttributes(attrs, R.styleable.COUIPickersCommonAttrs, defStyleAttr, 0);
        maxViewWidth = commonAttrs.getDimensionPixelSize(R.styleable.COUIPickersCommonAttrs_couiPickersMaxWidth, 0);
        commonAttrs.recycle();

        selectedValueWidth = getResources().getDimensionPixelOffset(R.dimen.coui_number_picker_text_width);
        unitMargin = getResources().getDimensionPixelOffset(R.dimen.coui_number_picker_text_margin_start);
        unitMinWidth = getResources().getDimensionPixelOffset(R.dimen.coui_number_picker_unit_min_width);
        unitTextSize = getResources().getDimensionPixelSize(R.dimen.coui_numberpicker_unit_textSize);
        unitMarginBottom = getResources().getDimensionPixelSize(R.dimen.coui_numberpicker_unit_margin_bottom);
        backgroundDividerHeight = Math.max(getResources().getDimensionPixelSize(R.dimen.coui_number_picker_background_divider_height), 1);
        backgroundRadius = getResources().getDimensionPixelOffset(R.dimen.coui_selected_background_radius);
        initTextMargin = Math.max(0, ((minWidth - selectedValueWidth) - unitMinWidth) - (unitMargin * 2));
        textMargin = initTextMargin;

        selectorPaint.setTextAlign(Paint.Align.CENTER);
        selectorPaint.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        selectorPaint.setTextSize(normalTextSize);
        Paint.FontMetrics fontMetrics = selectorPaint.getFontMetrics();
        normalTextTop = fontMetrics.top;
        normalTextBottom = fontMetrics.bottom;
        dividerPaint.setColor(backgroundColor);
        flingScroller = new Scroller(context, SLOW_FLING_INTERPOLATOR);
        adjustScroller = new Scroller(context, FLING_INTERPOLATOR);
        ViewConfiguration configuration = ViewConfiguration.get(context);
        touchSlop = configuration.getScaledTouchSlop();
        minimumFlingVelocity = Math.max(configuration.getScaledMinimumFlingVelocity(), MINIMUM_FLING_VELOCITY);
        maximumFlingVelocity = MAX_VELOCITY;
        flingFriction = ViewConfiguration.getScrollFriction();
        float ppi = getResources().getDisplayMetrics().density * 160f;
        physicalCoeff = SensorManager.GRAVITY_EARTH * 39.37f * ppi * 0.84f;
        accessibilityManager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        soundUtil = COUISoundLoadUtil.getInstance();
        clickSoundId = soundUtil.loadSoundFile(context, R.raw.coui_numberpicker_click);
        updateContentDescription();
        initializeSelectorWheelIndices();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpec = makeMeasureSpec(widthMeasureSpec, maxWidth);
        super.onMeasure(widthSpec, makeMeasureSpec(heightMeasureSpec, maxHeight));
        if (MeasureSpec.getMode(widthSpec) != MeasureSpec.AT_MOST) {
            textMargin = (getMeasuredWidth() - selectedValueWidth) / 2;
        }
        int width = resolveSizeAndStateRespectingMinSize(minWidth, getMeasuredWidth(), widthMeasureSpec)
                + numberPickerPaddingRight + numberPickerPaddingLeft;
        if (maxViewWidth > 0 && width > maxViewWidth) {
            width = maxViewWidth;
        }
        setMeasuredDimension(width, resolveSizeAndStateRespectingMinSize(minHeight, getMeasuredHeight(), heightMeasureSpec));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initializeSelectorWheel();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed) {
            initializeSelectorWheel();
            initializeFadingEdges();
        }
        initColorGradientRes();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (hasBackground) {
            canvas.drawRect(numberPickerPaddingLeft,
                    (getHeight() / 2f) - backgroundRadius - diffusion,
                    getWidth() - numberPickerPaddingRight,
                    (getHeight() / 2f) - backgroundRadius - diffusion + backgroundDividerHeight,
                    dividerPaint);
            canvas.drawRect(numberPickerPaddingLeft,
                    (getHeight() / 2f) + backgroundRadius + diffusion,
                    getWidth() - numberPickerPaddingRight,
                    (getHeight() / 2f) + backgroundRadius + diffusion + backgroundDividerHeight,
                    dividerPaint);
        }

        float textX = resolveTextCenterX();
        if (!TextUtils.isEmpty(unitText)) {
            textX = unitTextAnchor();
        }
        int y = currentScrollOffset - selectorElementHeight;
        float lastTextX = textX;
        float unitBaseline = 0f;
        for (int i = 0; i < selectorIndices.length; i++) {
            int selectorValue = selectorIndices[i];
            if (selectorValue == SELECTOR_INDEX_IGNORE) {
                drawIgnoreBars(canvas, textX, y);
                y += selectorElementHeight;
                continue;
            }
            String text = selectorCache.get(selectorValue);
            if (text == null) {
                text = formatValue(selectorValue);
            }
            // Leapy fixed 2026-08-27: Decoded OPPO smali computes gradient coeffs
            // inside the band but always paints the non-focus pass from the start
            // hint-neutral tokens. Interpolating normalColor made the row below
            // selection use focus black (coeff=0 -> focus end color).
            int normalColor = Color.argb(alphaStart, redStart, greenStart, blueStart);
            int focusColor = Color.argb(alphaEnd, redEnd, greenEnd, blueEnd);
            float blendedTextSize = gradualChangeTextSize(normalTextSize, focusTextSize, normalTextSize, normalTextSize, y);
            // Leapy modified 2026-07-24: BEGIN match decoded OPPO clipping-based size transition while a number leaves the focus band.
            selectorPaint.setTextSize(normalTextSize);
            if (selectorPaint.measureText(text) >= getMeasuredWidth()) {
                selectorPaint.setTextAlign(Paint.Align.LEFT);
                lastTextX = 0f;
            } else {
                selectorPaint.setTextAlign(Paint.Align.CENTER);
                lastTextX = textX;
            }
            float baseline = (int) (((((y + y) + selectorElementHeight) - normalTextTop) - normalTextBottom) / 2f)
                    + (pickerOffset / 2f)
                    + drawItemOffsetY;
            float selectedTop = (getHeight() / 2f) - backgroundRadius - diffusion;
            float selectedBottom = (getHeight() / 2f) + backgroundRadius + diffusion;
            int save = canvas.save();
            canvas.clipOutRect(0f, selectedTop, getWidth(), selectedBottom);
            selectorPaint.setColor(normalColor);
            selectorPaint.setTextSize(normalTextSize);
            canvas.drawText(text, lastTextX, baseline, selectorPaint);
            canvas.restoreToCount(save);
            // Leapy end 2026-07-24: the focus clip progressively reveals the normal-size glyph during dragging.

            int saveFocus = canvas.save();
            canvas.clipRect(0f, selectedTop, getWidth(), selectedBottom);
            selectorPaint.setColor(focusColor);
            selectorPaint.setTextSize(focusTextSize);
            canvas.drawText(text, lastTextX, baseline, selectorPaint);
            canvas.restoreToCount(saveFocus);
            Paint.FontMetrics unitMetrics = selectorPaint.getFontMetrics();
            unitBaseline = (int) ((((selectorElementHeight - unitMetrics.top) - unitMetrics.bottom) / 2f)
                    + (pickerOffset / 2f)
                    + selectorElementHeight);
            y += selectorElementHeight;
        }
        if (!TextUtils.isEmpty(unitText)) {
            selectorPaint.setTextSize(unitTextSize);
            selectorPaint.setColor(focusTextColor);
            selectorPaint.setTextAlign(Paint.Align.LEFT);
            float unitX = lastTextX + (selectedValueWidth / 2f) + unitMargin;
            if (isLayoutRtl()) {
                unitX = (getMeasuredWidth() - unitX) - selectorPaint.measureText(unitText);
            }
            canvas.drawText(unitText, unitX, unitBaseline - unitMarginBottom, selectorPaint);
            selectorPaint.setTextAlign(Paint.Align.CENTER);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (!isEnabled() || event.getActionMasked() != MotionEvent.ACTION_DOWN) {
            return false;
        }
        removeAllCallbacks();
        float y = event.getY();
        lastDownY = y;
        lastMoveY = y;
        lastDownEventTime = event.getEventTime();
        performClickOnTap = false;
        dragging = false;
        if (y < topSelectionDividerTop) {
            if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
                pressedStateHelper.buttonPressDelayed(PressedStateHelper.BUTTON_DECREMENT);
            }
        } else if (y > bottomSelectionDividerBottom && scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
            pressedStateHelper.buttonPressDelayed(PressedStateHelper.BUTTON_INCREMENT);
        }
        requestParentDisallowInterceptTouchEvent(true);
        if (!flingScroller.isFinished()) {
            flingScroller.abortAnimation();
            adjustScroller.forceFinished(true);
            notifyScrollState(OnScrollListener.SCROLL_STATE_IDLE);
        } else if (adjustScroller.isFinished()) {
            if (y < topSelectionDividerTop) {
                postChangeCurrentByOneFromLongPress(false, ViewConfiguration.getLongPressTimeout());
            } else if (y > bottomSelectionDividerBottom) {
                postChangeCurrentByOneFromLongPress(true, ViewConfiguration.getLongPressTimeout());
            } else {
                performClickOnTap = true;
            }
        } else {
            flingScroller.abortAnimation();
            adjustScroller.forceFinished(true);
        }
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                initOrResetVelocityTracker();
                velocityTracker.addMovement(event);
                return true;
            case MotionEvent.ACTION_MOVE:
                initVelocityTrackerIfNotExists();
                velocityTracker.addMovement(event);
                float y = event.getY();
                if (scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    int delta = (int) (y - lastMoveY);
                    // Leapy modified 2026-07-24: BEGIN retain the decoded OPPO drag delta for fling settling.
                    deltaMoveY = delta;
                    // Leapy end 2026-07-24: decoded OPPO drag-to-fling transition.
                    scrollBy(0, delta);
                    invalidate();
                } else if ((int) Math.abs(y - lastDownY) > touchSlop) {
                    removeAllCallbacks();
                    notifyScrollState(OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
                }
                lastMoveY = y;
                return true;
            case MotionEvent.ACTION_UP:
                removeChangeCurrentByOneFromLongPress();
                pressedStateHelper.cancel();
                initVelocityTrackerIfNotExists();
                velocityTracker.addMovement(event);
                int yUp = (int) event.getY();
                int deltaY = (int) Math.abs(yUp - lastDownY);
                velocityTracker.computeCurrentVelocity(1000, maximumFlingVelocity);
                int initialVelocity = (int) velocityTracker.getYVelocity();
                if (Math.abs(initialVelocity) > minimumFlingVelocity) {
                    // Leapy modified 2026-07-24: BEGIN use the decoded OPPO drag-to-fling velocity scaling.
                    fling((int) (initialVelocity * getDampRatio()));
                    // Leapy end 2026-07-24: decoded OPPO picker settling velocity.
                    notifyScrollState(OnScrollListener.SCROLL_STATE_FLING);
                } else {
                    long eventTime = event.getEventTime() - lastDownEventTime;
                    if (deltaY > touchSlop || eventTime >= ViewConfiguration.getLongPressTimeout()) {
                        ensureScrollWheelAdjusted();
                    } else if (performClickOnTap) {
                        performClickOnTap = false;
                        performClick();
                    } else {
                        int rowOffset = ((yUp / Math.max(1, selectorElementHeight)) - selectorMiddleItemIndex) + 1;
                        if (rowOffset > 0) {
                            changeValueByOne(true);
                            pressedStateHelper.buttonTapped(PressedStateHelper.BUTTON_INCREMENT);
                        } else if (rowOffset < 0) {
                            changeValueByOne(false);
                            pressedStateHelper.buttonTapped(PressedStateHelper.BUTTON_DECREMENT);
                        }
                        ensureScrollWheelAdjusted();
                    }
                }
                requestParentDisallowInterceptTouchEvent(false);
                recycleVelocityTracker();
                return true;
            case MotionEvent.ACTION_CANCEL:
                removeChangeCurrentByOneFromLongPress();
                pressedStateHelper.cancel();
                ensureScrollWheelAdjusted();
                requestParentDisallowInterceptTouchEvent(false);
                recycleVelocityTracker();
                return true;
            default:
                return true;
        }
    }

    @Override
    public void computeScroll() {
        if (!flingScroller.isFinished()) {
            flingScroller.computeScrollOffset();
            int y = flingScroller.getCurrY();
            if (previousScrollerY == 0) {
                previousScrollerY = flingScroller.getStartY();
            }
            int now = (int) android.os.SystemClock.uptimeMillis();
            int elapsed = now - previousTime;
            int distance = Math.abs(y - previousScrollerY);
            if (elapsed > 0) {
                scrollerVelocity = Math.min(maximumFlingVelocity, (int) ((distance * 1000f) / elapsed));
            }
            scrollBy(0, y - previousScrollerY);
            previousScrollerY = y;
            previousTime = now;
            if (flingScroller.isFinished()) {
                onScrollerFinished(flingScroller);
            } else {
                postInvalidateOnAnimation();
            }
            return;
        }
        if (!adjustScroller.isFinished()) {
            adjustScroller.computeScrollOffset();
            int y = adjustScroller.getCurrY();
            if (previousScrollerY == 0) {
                previousScrollerY = adjustScroller.getStartY();
            }
            scrollBy(0, y - previousScrollerY);
            previousScrollerY = y;
            if (adjustScroller.isFinished()) {
                // Leapy modified 2026-07-24: BEGIN match decoded OPPO completion without snapping the visual offset.
                notifyScrollState(OnScrollListener.SCROLL_STATE_IDLE);
                // Leapy end 2026-07-24: preserve the final continuously animated picker frame.
            } else {
                postInvalidateOnAnimation();
            }
        }
    }

    @Override
    public int computeVerticalScrollExtent() {
        return getHeight();
    }

    @Override
    public int computeVerticalScrollOffset() {
        return currentScrollOffset;
    }

    @Override
    public int computeVerticalScrollRange() {
        return ((maxValue - minValue) + 1) * selectorElementHeight;
    }

    @Override
    public boolean dispatchHoverEvent(MotionEvent event) {
        if (accessibilityManager == null || !accessibilityManager.isEnabled()) {
            return false;
        }
        int action = event.getActionMasked();
        AccessibilityNodeProviderImpl provider = (AccessibilityNodeProviderImpl) getAccessibilityNodeProvider();
        if (action == MotionEvent.ACTION_HOVER_ENTER) {
            sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_HOVER_ENTER);
            lastHoveredChildVirtualViewId = -1;
            return false;
        }
        if (action == MotionEvent.ACTION_HOVER_EXIT) {
            provider.sendAccessibilityEventForVirtualView(-1, COUIToolTips.ALIGN_TOP);
            lastHoveredChildVirtualViewId = -1;
            return false;
        }
        if (action != MotionEvent.ACTION_HOVER_MOVE) {
            return false;
        }
        int lastHovered = lastHoveredChildVirtualViewId;
        if (lastHovered == -1 || lastHovered == -1) {
            return false;
        }
        provider.sendAccessibilityEventForVirtualView(lastHovered, COUIToolTips.ALIGN_TOP);
        provider.sendAccessibilityEventForVirtualView(-1, AccessibilityEvent.TYPE_VIEW_HOVER_ENTER);
        lastHoveredChildVirtualViewId = -1;
        provider.performAction(-1, AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, null);
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            int action = event.getAction();
            if (action == KeyEvent.ACTION_DOWN) {
                requestFocus();
                lastHandledDownDpadKeyCode = keyCode;
                removeAllCallbacks();
                if (flingScroller.isFinished()) {
                    changeValueByOne(keyCode == KeyEvent.KEYCODE_DPAD_DOWN);
                }
                return true;
            }
            if (action == KeyEvent.ACTION_UP && lastHandledDownDpadKeyCode == keyCode) {
                lastHandledDownDpadKeyCode = -1;
                return true;
            }
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
            removeAllCallbacks();
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            removeAllCallbacks();
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean dispatchTrackballEvent(MotionEvent event) {
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            removeAllCallbacks();
        }
        return super.dispatchTrackballEvent(event);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeAllCallbacks();
        if (!flingScroller.isFinished()) {
            flingScroller.forceFinished(true);
        }
        if (!adjustScroller.isFinished()) {
            adjustScroller.forceFinished(true);
        }
        recycleVelocityTracker();
        if (touchEffectThread != null) {
            touchEffectThread.quit();
            touchEffectThread = null;
        }
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        VibrateUtils.unRegisterHapticObserver();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        touchEffectThread = new NumberPickerHandlerThread("touchEffect", -16);
        touchEffectThread.start();
        if (touchEffectThread.getLooper() != null) {
            handler = new TouchEffectHandler(touchEffectThread.getLooper());
        }
        VibrateUtils.registerHapticObserver(getContext());
        initOrResetVelocityTracker();
    }

    @Override
    protected float getTopFadingEdgeStrength() {
        return verticalFadingEdgeEnabled ? 0.9f : super.getTopFadingEdgeStrength();
    }

    @Override
    protected float getBottomFadingEdgeStrength() {
        return verticalFadingEdgeEnabled ? 0.9f : super.getBottomFadingEdgeStrength();
    }

    public void scrollBy(int x, int y) {
        scrollByRows(y);
    }

    public void scrollForceFinished() {
        if (!flingScroller.isFinished()) {
            moveToFinalScrollerPosition(flingScroller);
        }
        if (!adjustScroller.isFinished()) {
            moveToFinalScrollerPosition(adjustScroller);
        }
        // Leapy modified 2026-07-24: BEGIN decoded OPPO forced completion retains the offset produced by scrollBy().
        notifyScrollState(OnScrollListener.SCROLL_STATE_IDLE);
        invalidate();
        // Leapy end 2026-07-24: do not hard-reset picker typography after forced completion.
    }

    public int getValue() {
        return value;
    }

    public int getMinValue() {
        return minValue;
    }

    public int getMaxValue() {
        return maxValue;
    }

    public String[] getDisplayedValues() {
        return displayedValues;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public String getCurrentText() {
        return formatValue(value);
    }

    @Override
    public AccessibilityNodeProvider getAccessibilityNodeProvider() {
        if (accessibilityNodeProvider == null) {
            accessibilityNodeProvider = new AccessibilityNodeProviderImpl();
        }
        return accessibilityNodeProvider;
    }

    public boolean isAccessibilityEnable() {
        return accessibilityManager != null && accessibilityManager.isEnabled();
    }

    public boolean isIgnorable() {
        return ignorable;
    }

    public boolean getWrapSelectorWheel() {
        return wrapSelectorWheel;
    }

    public int getNumberPickerPaddingLeft() {
        return numberPickerPaddingLeft;
    }

    public int getNumberPickerPaddingRight() {
        return numberPickerPaddingRight;
    }

    public Paint getSelectorTextPaint() {
        return selectorPaint;
    }

    public float getTextSize() {
        return selectorPaint.getTextSize();
    }

    public void addTalkbackSuffix(String suffix) {
        talkbackSuffix = suffix == null ? "" : suffix;
        updateContentDescription();
    }

    public void clearNumberPickerPadding() {
        numberPickerPaddingLeft = 0;
        numberPickerPaddingRight = 0;
        requestLayout();
    }

    public void refresh() {
        invalidate();
    }

    public void setAlignPosition(int alignPosition) {
        this.alignPosition = alignPosition;
        invalidate();
    }

    public void setBackgroundRadius(int radius) {
        backgroundRadius = radius;
        invalidate();
    }

    public void setDiffusion(int diffusion) {
        this.diffusion = diffusion;
        invalidate();
    }

    public void setDisplayedValues(String[] values) {
        displayedValues = values;
        selectorCache.clear();
        initializeSelectorWheelIndices();
        invalidate();
        updateContentDescription();
    }

    public void setDrawItemVerticalOffset(int offset) {
        drawItemOffsetY = offset;
        invalidate();
    }

    public void setEnableAdaptiveVibrator(boolean enable) {
        enableAdaptiveVibrator = enable;
    }

    public void setFocusTextSize(int size) {
        focusTextSize = size;
        invalidate();
    }

    public void setFormatter(Formatter formatter) {
        this.formatter = formatter;
        selectorCache.clear();
        initializeSelectorWheelIndices();
        invalidate();
        updateContentDescription();
    }

    public void setGradientColor(int normalColor, int focusColor) {
        normalTextColor = normalColor;
        focusTextColor = focusColor;
        alphaStart = Color.alpha(normalColor);
        alphaEnd = Color.alpha(focusColor);
        redStart = Color.red(normalColor);
        redEnd = Color.red(focusColor);
        greenStart = Color.green(normalColor);
        greenEnd = Color.green(focusColor);
        blueStart = Color.blue(normalColor);
        blueEnd = Color.blue(focusColor);
        invalidate();
    }

    public void setHasBackground(boolean hasBackground) {
        this.hasBackground = hasBackground;
        invalidate();
    }

    public void setIgnorable(boolean ignorable) {
        this.ignorable = ignorable;
        invalidate();
    }

    public void setMaxValue(int maxValue) {
        if (maxValue < 0) {
            throw new IllegalArgumentException("maxValue must be >= 0");
        }
        this.maxValue = maxValue;
        if (value > maxValue) {
            setValueInternal(maxValue, false);
        }
        updateWrapSelectorWheel();
        selectorCache.clear();
        initializeSelectorWheelIndices();
        invalidate();
    }

    public void setMinValue(int minValue) {
        if (minValue < 0) {
            throw new IllegalArgumentException("minValue must be >= 0");
        }
        this.minValue = minValue;
        if (value < minValue) {
            setValueInternal(minValue, false);
        }
        updateWrapSelectorWheel();
        selectorCache.clear();
        initializeSelectorWheelIndices();
        invalidate();
    }

    public void setNormalTextColor(int color) {
        normalTextColor = color;
        invalidate();
    }

    public void setNormalTextSize(int size) {
        normalTextSize = size;
        invalidate();
    }

    public void setNumberPickerPaddingLeft(int padding) {
        numberPickerPaddingLeft = padding;
        invalidate();
    }

    public void setNumberPickerPaddingRight(int padding) {
        numberPickerPaddingRight = padding;
        invalidate();
    }

    public void setOnLongPressUpdateInterval(long interval) {
        longPressUpdateInterval = interval;
    }

    public void setOnScrollListener(OnScrollListener listener) {
        onScrollListener = listener;
    }

    public void setOnScrollingStopListener(OnScrollingStopListener listener) {
        onScrollingStopListener = listener;
    }

    public void setOnValueChangedListener(OnValueChangeListener listener) {
        onValueChangeListener = listener;
    }

    public void setPickerFocusColor(int color) {
        focusTextColor = color;
        alphaEnd = Color.alpha(color);
        redEnd = Color.red(color);
        greenEnd = Color.green(color);
        blueEnd = Color.blue(color);
        invalidate();
    }

    public void setPickerNormalColor(int color) {
        normalTextColor = color;
        alphaStart = Color.alpha(color);
        redStart = Color.red(color);
        greenStart = Color.green(color);
        blueStart = Color.blue(color);
        invalidate();
    }

    public void setPickerOffset(int offset) {
        pickerOffset = offset;
        invalidate();
    }

    public void setPickerRowNumber(int rowNumber) {
        int totalRows = Math.max(3, rowNumber + 2);
        if (totalRows % 2 == 0) {
            totalRows++;
        }
        selectorItemCount = totalRows;
        selectorMiddleItemIndex = totalRows / 2;
        selectorIndices = new int[totalRows];
        initializeSelectorWheel();
        requestLayout();
    }

    public void setSelectedValueWidth(int width) {
        selectedValueWidth = width;
        invalidate();
    }

    public void setSelectorTextColor(int normalColor, int focusColor) {
        setGradientColor(normalColor, focusColor);
    }

    public void setTouchEffectInterval(int interval) {
        touchEffectInterval = interval;
    }

    public void setTwoDigitFormatter() {
        if (twoDigitFormatter == null) {
            twoDigitFormatter = new TwoDigitFormatter();
        }
        setFormatter(twoDigitFormatter);
    }

    public void setUnitText(String unitText) {
        this.unitText = unitText == null ? "" : unitText;
        invalidate();
    }

    public void setValue(int value) {
        setValueInternal(value, false);
    }

    public void setVerticalFadingEdgeEnable(boolean enable) {
        verticalFadingEdgeEnabled = enable;
        setVerticalFadingEdgeEnabled(enable);
        setFadingEdgeLength(Math.max(0, (getHeight() - normalTextSize) / 2));
        invalidate();
    }

    public void setVibrateIntensity(float intensity) {
        vibrateIntensity = intensity;
    }

    public void setVibrateLevel(int level) {
        vibrateLevel = level;
    }

    public void setWrapSelectorWheel(boolean wrap) {
        wrapSelectorWheel = wrap;
        initializeSelectorWheelIndices();
        invalidate();
    }

    private void scrollByRows(int deltaY) {
        int oldScrollOffset = currentScrollOffset;
        currentScrollOffset += deltaY;
        int rowHeight = Math.max(1, selectorElementHeight);
        while (currentScrollOffset > (rowHeight * 0.95f) + (pickerOffset / 2f)) {
            currentScrollOffset -= rowHeight;
            decrementSelectorIndices();
            setValueInternal(selectorIndices[selectorMiddleItemIndex], true);
            if (!wrapSelectorWheel && selectorIndices[selectorMiddleItemIndex] < minValue) {
                currentScrollOffset = 0;
            }
        }
        while (currentScrollOffset < ((-rowHeight) * 0.95f) - (pickerOffset / 2f)) {
            currentScrollOffset += rowHeight;
            incrementSelectorIndices();
            setValueInternal(selectorIndices[selectorMiddleItemIndex], true);
            if (!wrapSelectorWheel && selectorIndices[selectorMiddleItemIndex] > maxValue) {
                currentScrollOffset = 0;
            }
        }
        if (oldScrollOffset != currentScrollOffset) {
            onScrollChanged(0, currentScrollOffset, 0, oldScrollOffset);
        }
        invalidate();
    }

    private void requestParentDisallowInterceptTouchEvent(boolean disallowIntercept) {
        ViewParent parent = getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(disallowIntercept);
        }
    }

    private void adjustToNearestRow() {
        ensureScrollWheelAdjusted();
    }

    private boolean ensureScrollWheelAdjusted() {
        int delta = -currentScrollOffset;
        if (delta == 0) {
            // Leapy modified 2026-07-24: BEGIN decoded OPPO zero-offset completion changes state only.
            notifyScrollState(OnScrollListener.SCROLL_STATE_IDLE);
            // Leapy end 2026-07-24: avoid a final-frame text-size reset.
            return false;
        }
        int rowHeight = Math.max(1, selectorElementHeight);
        float elementHeight = rowHeight + Math.max(0f, diffusion);
        if (Math.abs(delta) > elementHeight / 2f) {
            delta = (int) (delta + (delta > 0 ? -elementHeight : elementHeight));
        }
        previousScrollerY = 0;
        adjustScroller.startScroll(0, 0, 0, delta, SNAP_SCROLL_DURATION);
        notifyScrollState(OnScrollListener.SCROLL_STATE_FLING);
        postInvalidateOnAnimation();
        return true;
    }

    private void fling(int velocityY) {
        previousScrollerY = 0;
        previousTime = (int) android.os.SystemClock.uptimeMillis();
        scrollerVelocity = Math.abs(velocityY);
        int rowHeight = Math.max(1, selectorElementHeight);
        float elementHeight = rowHeight + Math.max(0f, diffusion);
        double splineDistance = getSplineFlingDistance(velocityY);
        double roundedDistance = splineDistance > elementHeight
                ? splineDistance - (splineDistance % elementHeight)
                : splineDistance % elementHeight;
        // Leapy modified 2026-07-24: BEGIN include decoded OPPO drag delta and selector phase in fling distance.
        double distanceWithDrag = roundedDistance + deltaMoveY;
        int distance = (int) (velocityY < 0
                ? -(distanceWithDrag + ((currentScrollOffset - deltaMoveY) % elementHeight))
                : distanceWithDrag - ((currentScrollOffset + deltaMoveY) % elementHeight));
        // Leapy end 2026-07-24: decoded OPPO fling lands continuously on the selector row.
        int duration = Math.max(300, (int) (getSplineFlingDuration(velocityY) * 1.5f));
        flingScroller.startScroll(0, 0, 0, distance, duration);
        postInvalidateOnAnimation();
    }

    private double getSplineDeceleration(float velocity) {
        return Math.log((Math.abs(velocity) * INFLEXION) / (flingFriction * physicalCoeff));
    }

    // Leapy added 2026-07-24: BEGIN decoded OPPO COUINumberPicker velocity damping.
    private float getDampRatio() {
        return Math.min(VELOCITY_SPEED_UP_RATIO, 1.6f);
    }
    // Leapy end 2026-07-24: decoded OPPO COUINumberPicker velocity damping.

    private double getSplineFlingDistance(float velocity) {
        double deceleration = getSplineDeceleration(velocity);
        return flingFriction * physicalCoeff
                * Math.exp((DECELERATION_RATE / (DECELERATION_RATE - 1.0)) * deceleration);
    }

    private int getSplineFlingDuration(float velocity) {
        return (int) (1000.0 * Math.exp(getSplineDeceleration(velocity) / (DECELERATION_RATE - 1.0)));
    }

    private boolean moveToFinalScrollerPosition(Scroller scroller) {
        scroller.forceFinished(true);
        int finalY = scroller.getFinalY() - scroller.getCurrY();
        int rowHeight = Math.max(1, selectorElementHeight);
        int delta = -((currentScrollOffset + finalY) % rowHeight);
        if (delta != 0 && Math.abs(delta) > rowHeight / 2) {
            delta += delta > 0 ? -rowHeight : rowHeight;
        }
        if (finalY == 0 && delta == 0) {
            return false;
        }
        scrollBy(0, finalY + delta);
        return true;
    }

    private void onScrollerFinished(Scroller scroller) {
        if (scroller == flingScroller) {
            // Leapy modified 2026-07-24: BEGIN decoded OPPO starts final alignment and reports idle without resetting offset.
            ensureScrollWheelAdjusted();
            notifyScrollState(OnScrollListener.SCROLL_STATE_IDLE);
            // Leapy end 2026-07-24: continuous picker settle completion.
        }
    }

    private void initVelocityTrackerIfNotExists() {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
    }

    private void recycleVelocityTracker() {
        if (velocityTracker != null) {
            velocityTracker.recycle();
            velocityTracker = null;
        }
    }

    private void initOrResetVelocityTracker() {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        } else {
            velocityTracker.clear();
        }
    }

    private void postChangeCurrentByOneFromLongPress(boolean increment, long delayMillis) {
        if (changeCurrentByOneFromLongPressCommand == null) {
            changeCurrentByOneFromLongPressCommand = new ChangeCurrentByOneFromLongPressCommand();
        } else {
            removeCallbacks(changeCurrentByOneFromLongPressCommand);
        }
        changeCurrentByOneFromLongPressCommand.setStep(increment);
        postDelayed(changeCurrentByOneFromLongPressCommand, delayMillis);
    }

    private void removeChangeCurrentByOneFromLongPress() {
        if (changeCurrentByOneFromLongPressCommand != null) {
            removeCallbacks(changeCurrentByOneFromLongPressCommand);
        }
    }

    private void removeAllCallbacks() {
        if (changeCurrentByOneFromLongPressCommand != null) {
            removeCallbacks(changeCurrentByOneFromLongPressCommand);
        }
        pressedStateHelper.cancel();
    }

    private void finishScroll() {
        // Leapy modified 2026-07-24: BEGIN keep the scroller-produced offset exactly as decoded OPPO does.
        notifyScrollState(OnScrollListener.SCROLL_STATE_IDLE);
        invalidate();
        // Leapy end 2026-07-24: never snap picker typography by hard-resetting the final offset.
    }

    private void changeValueByOne(boolean increment) {
        int next = increment ? value + 1 : value - 1;
        if (next > maxValue) {
            next = wrapSelectorWheel ? minValue : maxValue;
        } else if (next < minValue) {
            next = wrapSelectorWheel ? maxValue : minValue;
        }
        setValueInternal(next, true);
    }

    private void setValueInternal(int newValue, boolean notify) {
        int old = value;
        int clamped = wrapValue(newValue);
        value = clamped;
        if (notify && old != clamped && onValueChangeListener != null) {
            onValueChangeListener.onValueChange(this, old, clamped);
        }
        if (notify && old != clamped) {
            playFeedback();
        }
        initializeSelectorWheelIndices();
        updateContentDescription();
        invalidate();
    }

    private void playFeedback() {
        performFeedback();
        if (handler != null) {
            handler.removeMessages(MSG_PLAY_SOUND);
            handler.sendEmptyMessageDelayed(MSG_PLAY_SOUND, touchEffectInterval);
        } else {
            playSoundEffect();
        }
    }

    private void performFeedback() {
        if (performHapticFeedback(COUIHapticFeedbackConstants.GRANULAR_SHORT_VIBRATE_SYNC)) {
            return;
        }
        performHapticFeedback(COUIHapticFeedbackConstants.GRANULAR_SHORT_VIBRATE);
    }

    public void playSoundEffect() {
        soundUtil.play(getContext(), clickSoundId, 1f, 1f, 1, 0, 1f);
    }

    private int wrapValue(int requested) {
        if (maxValue < minValue) {
            return requested;
        }
        if (requested > maxValue) {
            return wrapSelectorWheel ? minValue : maxValue;
        }
        if (requested < minValue) {
            return wrapSelectorWheel ? maxValue : minValue;
        }
        return requested;
    }

    private int getWrappedSelectorIndex(int base, int offset) {
        int next = base + offset;
        if (maxValue < minValue) {
            return next;
        }
        int count = maxValue - minValue + 1 + (ignorable ? 1 : 0);
        if (count <= 0) {
            return next;
        }
        if (wrapSelectorWheel) {
            int wrapped = COUIMathUtils.floorMod(next - minValue, count);
            if (wrapped > maxValue - minValue) {
                return SELECTOR_INDEX_IGNORE;
            }
            return minValue + wrapped;
        }
        if (next < minValue || next > maxValue) {
            return SELECTOR_INDEX_IGNORE;
        }
        return next;
    }

    private void initializeSelectorWheel() {
        initializeSelectorWheelIndices();
        int count = Math.max(1, selectorIndices.length - 2);
        selectorTextGapHeight = (int) (Math.max(0, (getHeight() - (count * normalTextSize)) - pickerOffset) / (float) count + 0.5f);
        selectorElementHeight = Math.max(1, normalTextSize + selectorTextGapHeight);
        currentScrollOffset = 0;
        topSelectionDividerTop = (getHeight() / 2) - (selectorElementHeight / 2);
        bottomSelectionDividerBottom = (getHeight() / 2) + (selectorElementHeight / 2);
    }

    private void initializeFadingEdges() {
        setVerticalFadingEdgeEnabled(verticalFadingEdgeEnabled);
        setFadingEdgeLength(((getBottom() - getTop()) - normalTextSize) / 2);
    }

    private void initColorGradientRes() {
        gradientPositionTop = (int) (selectorElementHeight * (selectorMiddleItemIndex - 0.5d));
        gradientPositionBottom = (int) (selectorElementHeight * (selectorMiddleItemIndex + 0.5d));
    }

    private void initializeSelectorWheelIndices() {
        if (selectorIndices == null) {
            return;
        }
        selectorCache.clear();
        for (int i = 0; i < selectorIndices.length; i++) {
            int offset = i - selectorMiddleItemIndex;
            int selectorIndex = ignorable ? getWrappedSelectorIndex(value, offset) : value + offset;
            if (wrapSelectorWheel) {
                selectorIndex = getWrappedSelectorIndex(selectorIndex, 0);
            }
            selectorIndices[i] = selectorIndex;
            ensureCachedScrollSelectorValue(selectorIndex);
        }
    }

    private void incrementSelectorIndices() {
        for (int i = 0; i < selectorIndices.length; i++) {
            selectorIndices[i] = getWrappedSelectorIndex(selectorIndices[i], 1);
        }
        ensureCachedScrollSelectorValue(selectorIndices[selectorIndices.length - 1]);
    }

    private void decrementSelectorIndices() {
        for (int i = 0; i < selectorIndices.length; i++) {
            selectorIndices[i] = getWrappedSelectorIndex(selectorIndices[i], -1);
        }
        ensureCachedScrollSelectorValue(selectorIndices[0]);
    }

    private void ensureCachedScrollSelectorValue(int selectorIndex) {
        if (selectorCache.get(selectorIndex) != null) {
            return;
        }
        if (selectorIndex < minValue || selectorIndex > maxValue || selectorIndex == SELECTOR_INDEX_IGNORE) {
            selectorCache.put(selectorIndex, "");
        } else {
            selectorCache.put(selectorIndex, formatValue(selectorIndex));
        }
    }

    private void updateWrapSelectorWheel() {
        wrapSelectorWheel = (maxValue - minValue) >= selectorIndices.length - 2 && wrapSelectorWheel;
    }

    private String formatValue(int selectorValue) {
        String cached = selectorCache.get(selectorValue);
        if (cached != null) {
            return cached;
        }
        String valueText;
        if (displayedValues != null && selectorValue >= minValue && selectorValue - minValue < displayedValues.length) {
            valueText = displayedValues[selectorValue - minValue];
        } else if (formatter != null) {
            valueText = formatter.format(selectorValue);
        } else {
            valueText = String.valueOf(selectorValue);
        }
        selectorCache.put(selectorValue, valueText);
        return valueText;
    }

    private int resolveTextCenterX() {
        int left = numberPickerPaddingLeft;
        int right = getWidth() - numberPickerPaddingRight;
        if (visualWidth > 0 && visualWidth < right - left) {
            if (alignPosition == ALIGN_LEFT) {
                right = left + visualWidth;
            } else if (alignPosition == ALIGN_RIGHT) {
                left = right - visualWidth;
            }
        }
        return (left + right) / 2;
    }

    private boolean isLayoutRtl() {
        return getLayoutDirection() == LAYOUT_DIRECTION_RTL;
    }

    private float unitTextAnchor() {
        float right = textMargin() + (selectedValueWidth / 2f);
        if (isLayoutRtl()) {
            right = ((getMeasuredWidth() - right) - numberPickerPaddingRight) - numberPickerPaddingLeft;
        }
        return right;
    }

    private int textMargin() {
        return Math.max(0, textMargin);
    }

    private void drawIgnoreBars(Canvas canvas, float centerX, int y) {
        selectorPaint.setColor(normalTextColor);
        for (float pos = -0.5f; pos < 1.0f; pos += 1.0f) {
            float width = getResources().getDimension(R.dimen.coui_numberpicker_ignore_bar_width);
            float spacing = getResources().getDimension(R.dimen.coui_numberpicker_ignore_bar_spacing);
            float height = getResources().getDimension(R.dimen.coui_numberpicker_ignore_bar_height);
            float x = centerX + ((spacing + width) * pos);
            float halfWidth = width / 2f;
            float cy = y + (selectorElementHeight / 2f);
            canvas.drawRect(x - halfWidth, cy - (height / 2f), x + halfWidth, cy + (height / 2f), selectorPaint);
        }
    }

    private int makeMeasureSpec(int measureSpec, int maxSize) {
        if (maxSize == -1) {
            return measureSpec;
        }
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        if (mode == MeasureSpec.UNSPECIFIED) {
            return MeasureSpec.makeMeasureSpec(maxSize, MeasureSpec.EXACTLY);
        }
        if (mode == MeasureSpec.AT_MOST) {
            // Leapy modified 2026-07-24: BEGIN match decoded OPPO measurement when the hidden unit label is an empty string.
            if (unitText != null) {
                float unitWidth = selectorPaint.measureText(unitText);
                int measuredUnitWidth = unitMinWidth;
                if (unitWidth > measuredUnitWidth) {
                    measuredUnitWidth = (int) unitWidth;
                }
                size = measuredUnitWidth + (initTextMargin - unitMinWidth) + initTextMargin + selectedValueWidth;
            }
            // Leapy end 2026-07-24: decoded OPPO compact number-picker width measurement.
            return MeasureSpec.makeMeasureSpec(Math.min(size, maxSize), MeasureSpec.EXACTLY);
        }
        if (mode == MeasureSpec.EXACTLY) {
            return measureSpec;
        }
        throw new IllegalArgumentException("Unknown measure mode: " + mode);
    }

    private int resolveSizeAndStateRespectingMinSize(int minSize, int measuredSize, int measureSpec) {
        return minSize != -1 ? View.resolveSizeAndState(Math.max(minSize, measuredSize), measureSpec, 0) : measuredSize;
    }

    private void notifyScrollState(int state) {
        if (scrollState == state) {
            return;
        }
        scrollState = state;
        if (onScrollListener != null) {
            onScrollListener.onScrollStateChange(this, state);
        }
        if (state == OnScrollListener.SCROLL_STATE_IDLE) {
            String text = selectorCache.get(getValue());
            if (TextUtils.isEmpty(text)) {
                text = formatValue(getValue());
            }
            if (accessibilityManager != null && accessibilityManager.isEnabled()) {
                ((AccessibilityNodeProviderImpl) getAccessibilityNodeProvider())
                        .sendAccessibilityEventForVirtualView(-1, COUIToolTips.ALIGN_TOP);
            } else {
                announceForAccessibility(appendTalkbackSuffix(text));
            }
            if (onScrollingStopListener != null) {
                onScrollingStopListener.onScrollingStop();
            }
        }
    }

    private void updateContentDescription() {
        String text = formatValue(value);
        setContentDescription(appendTalkbackSuffix(text));
    }

    private String appendTalkbackSuffix(String text) {
        String valueText = text == null ? "" : text;
        return TextUtils.isEmpty(talkbackSuffix) ? valueText : valueText + talkbackSuffix;
    }

    private float gradualChangeTextSize(int normalStart, int focus, int normalBefore, int normalAfter, int y) {
        int middleTop = (selectorMiddleItemIndex - 1) * selectorElementHeight;
        double dy = y;
        double top = middleTop;
        if (dy > top - (selectorElementHeight * 0.5d) && dy < top + (selectorElementHeight * 0.5d)) {
            return focus - ((((focus - normalStart) * 2.0f) * Math.abs(y - middleTop)) / Math.max(1, selectorElementHeight));
        }
        if (y <= middleTop - selectorElementHeight) {
            return normalBefore + (((((normalAfter - normalBefore) * 1.0f) * y) / Math.max(1, selectorElementHeight)) / 2.0f);
        }
        if (y >= middleTop + selectorElementHeight) {
            return normalBefore + (((((normalAfter - normalBefore) * 1.0f)
                    * (((selectorIndices.length - 3) * selectorElementHeight) - y)) / Math.max(1, selectorElementHeight)) / 2.0f);
        }
        return focus;
    }
}
