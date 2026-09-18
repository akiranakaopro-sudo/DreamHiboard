package com.coui.appcompat.picker;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.coui.appcompat.R;
import com.coui.appcompat.darkmode.COUIDarkModeUtil;
import com.coui.appcompat.lunarutil.COUILunarUtil;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class COUILunarDatePicker extends FrameLayout {
    public static final int IGNORED_YEAR = Integer.MIN_VALUE;

    private static final int DEFAULT_START_YEAR = 1910;
    private static final int DEFAULT_END_YEAR = 2099;
    private static final int LONGPRESS_UPDATE_INTERVAL = 100;
    private static final int MONTH_LONGPRESS_UPDATE_INTERVAL = 200;
    private static final int NORMAL_MONTH_COUNT = 12;
    private static final int LEAPYEAR_MONTH_COUNT = 13;
    private static final int IGNORED_YEAR_MONTH_COUNT = 24;

    private static final String[] CHINESE_NUMBER = {
            "一", "二", "三", "四", "五", "六", "七", "八", "九", "十", "十一", "十二"
    };
    private static String sLeapString;
    private static Calendar sMinDate = Calendar.getInstance();
    private static Calendar sMaxDate = Calendar.getInstance();

    static {
        sMinDate.set(DEFAULT_START_YEAR, Calendar.MARCH, 10, 0, 0);
        sMaxDate.set(DEFAULT_END_YEAR, Calendar.DECEMBER, 31, 23, 59);
    }

    private Locale currentLocale;
    private IncompleteDate currentDate;
    private IncompleteDate tempDate;
    private COUINumberPicker daySpinner;
    private COUINumberPicker monthSpinner;
    private COUINumberPicker yearSpinner;
    private LinearLayout spinners;
    private String[] shortMonths;
    private boolean isEnabled = true;
    private boolean yearIgnorable;
    private int numberOfMonths = NORMAL_MONTH_COUNT;
    private int maxWidth;
    private int backgroundLeft;
    private int backgroundRadius;
    private int backgroundDividerHeight;
    private int dayMinValue;
    private int dayMaxValue;
    private int monthMinValue;
    private int monthMaxValue;
    private OnDateChangedListener onDateChangedListener;

    public interface OnDateChangedListener {
        void onLunarDateChanged(COUILunarDatePicker view, int year, int monthOfYear, int dayOfMonth);
    }

    public COUILunarDatePicker(Context context) {
        this(context, null);
    }

    public COUILunarDatePicker(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.couiDatePickerStyle);
    }

    public COUILunarDatePicker(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.DatePickerStyle);
    }

    public COUILunarDatePicker(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        COUIDarkModeUtil.setForceDarkAllow(this, false);
        setCurrentLocale(Locale.getDefault());

        TypedArray pickerAttrs = context.obtainStyledAttributes(attrs, R.styleable.COUILunarDatePicker, defStyleAttr, defStyleRes);
        yearIgnorable = pickerAttrs.getBoolean(R.styleable.COUILunarDatePicker_couiYearIgnorable, false);
        pickerAttrs.recycle();

        TypedArray commonAttrs = context.obtainStyledAttributes(attrs, R.styleable.COUIPickersCommonAttrs, defStyleAttr, defStyleRes);
        maxWidth = commonAttrs.getDimensionPixelSize(R.styleable.COUIPickersCommonAttrs_couiPickersMaxWidth, 0);
        commonAttrs.recycle();

        backgroundDividerHeight = Math.max(getResources().getDimensionPixelOffset(R.dimen.coui_number_picker_background_divider_height), 1);
        shortMonths = getResources().getStringArray(R.array.coui_lunar_month);
        sLeapString = getResources().getString(R.string.coui_lunar_leap_string);

        LayoutInflater.from(context).inflate(R.layout.coui_lunar_date_picker, this, true);
        spinners = findViewById(R.id.pickers);
        daySpinner = findViewById(R.id.day);
        monthSpinner = findViewById(R.id.month);
        yearSpinner = findViewById(R.id.year);

        COUINumberPicker.OnValueChangeListener listener = (picker, oldVal, newVal) -> {
            tempDate.setWith(currentDate);
            if (picker == daySpinner) {
                tempDate.change(Calendar.DAY_OF_MONTH, oldVal, newVal);
            } else if (picker == monthSpinner) {
                tempDate.change(Calendar.MONTH, oldVal, newVal);
            } else if (picker == yearSpinner) {
                tempDate.change(Calendar.YEAR, oldVal, newVal);
            } else {
                throw new IllegalArgumentException();
            }
            setDate(tempDate);
            updateSpinners();
            updateCalendarView();
            notifyDateChanged();
        };
        COUINumberPicker.OnScrollingStopListener stopListener =
                () -> announceForAccessibility(getLunarDateString2(currentDate));

        daySpinner.setOnLongPressUpdateInterval(LONGPRESS_UPDATE_INTERVAL);
        daySpinner.setOnValueChangedListener(listener);
        daySpinner.setOnScrollingStopListener(stopListener);
        monthSpinner.setMinValue(0);
        monthSpinner.setMaxValue(numberOfMonths - 1);
        monthSpinner.setDisplayedValues(shortMonths);
        monthSpinner.setOnLongPressUpdateInterval(MONTH_LONGPRESS_UPDATE_INTERVAL);
        monthSpinner.setOnValueChangedListener(listener);
        monthSpinner.setOnScrollingStopListener(stopListener);
        yearSpinner.setOnLongPressUpdateInterval(LONGPRESS_UPDATE_INTERVAL);
        yearSpinner.setOnValueChangedListener(listener);
        yearSpinner.setOnScrollingStopListener(stopListener);
        yearSpinner.setIgnorable(yearIgnorable);

        setSpinnersShown(true);
        setCalendarViewShown(true);
        tempDate.clear();
        tempDate.set(DEFAULT_START_YEAR, Calendar.JANUARY, 1);
        setMinDate(tempDate.getTimeInMillis());
        tempDate.clear();
        tempDate.set(DEFAULT_END_YEAR, Calendar.DECEMBER, 31, 23, 59);
        setMaxDate(tempDate.getTimeInMillis());
        currentDate.setTimeInMillis(System.currentTimeMillis());
        init(currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DAY_OF_MONTH), null);
        if (yearSpinner.isAccessibilityEnable()) {
            yearSpinner.addTalkbackSuffix("年");
        }
        backgroundRadius = context.getResources().getDimensionPixelOffset(R.dimen.coui_selected_background_radius);
        backgroundLeft = context.getResources().getDimensionPixelOffset(R.dimen.coui_selected_background_horizontal_padding);
        if (getImportantForAccessibility() == IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
            setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
        }
    }

    @Deprecated
    public static String getLunarDateString(Calendar calendar) {
        int[] lunar = COUILunarUtil.calculateLunarByGregorian(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
        return getLunarDateString(lunar[0], lunar[1], lunar[2], lunar[3]);
    }

    public static String getLunarDateString2(IncompleteDate incompleteDate) {
        int[] lunar = COUILunarUtil.calculateLunarByGregorian(incompleteDate.get(Calendar.YEAR),
                incompleteDate.get(Calendar.MONTH) + 1, incompleteDate.get(Calendar.DAY_OF_MONTH));
        return getLunarDateString(lunar[0], lunar[1], lunar[2], lunar[3]);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(daySpinner.getBackgroundColor());
        canvas.drawRect(backgroundLeft, (getHeight() / 2f) - backgroundRadius,
                getWidth() - backgroundLeft, (getHeight() / 2f) - backgroundRadius + backgroundDividerHeight, paint);
        canvas.drawRect(backgroundLeft, (getHeight() / 2f) + backgroundRadius,
                getWidth() - backgroundLeft, (getHeight() / 2f) + backgroundRadius + backgroundDividerHeight, paint);
        super.dispatchDraw(canvas);
    }

    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        dispatchThawSelfOnly(container);
    }

    public CalendarView getCalendarView() {
        return null;
    }

    public boolean getCalendarViewShown() {
        return false;
    }

    public int getDayOfMonth() {
        return currentDate.get(Calendar.DAY_OF_MONTH);
    }

    public COUINumberPicker getDaySpinner() {
        return daySpinner;
    }

    public int getLeapMonth() {
        return COUILunarUtil.leapMonth(currentDate.get(Calendar.YEAR));
    }

    public int[] getLunarDate() {
        return COUILunarUtil.calculateLunarByGregorian(currentDate.get(Calendar.YEAR),
                currentDate.get(Calendar.MONTH) + 1, currentDate.get(Calendar.DAY_OF_MONTH));
    }

    public long getMaxDate() {
        return sMaxDate.getTimeInMillis();
    }

    public long getMinDate() {
        return sMinDate.getTimeInMillis();
    }

    public int getMonth() {
        return currentDate.get(Calendar.MONTH);
    }

    public COUINumberPicker getMonthSpinner() {
        return monthSpinner;
    }

    public OnDateChangedListener getOnDateChangedListener() {
        return onDateChangedListener;
    }

    public boolean getSpinnersShown() {
        return spinners.isShown();
    }

    public int getYear() {
        return currentDate.get(Calendar.YEAR);
    }

    public COUINumberPicker getYearSpinner() {
        return yearSpinner;
    }

    public void init(int year, int monthOfYear, int dayOfMonth, OnDateChangedListener listener) {
        setDate(year, monthOfYear, dayOfMonth);
        updateSpinners();
        updateCalendarView();
        onDateChangedListener = listener;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    public boolean isLayoutRtl() {
        return TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == LAYOUT_DIRECTION_RTL;
    }

    public boolean isLeapMonth(int month) {
        return month == COUILunarUtil.leapMonth(currentDate.get(Calendar.YEAR));
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setCurrentLocale(newConfig.locale);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int mode = MeasureSpec.getMode(widthMeasureSpec);
        int size = MeasureSpec.getSize(widthMeasureSpec);
        if (maxWidth > 0 && size > maxWidth) {
            size = maxWidth;
        }
        int constrainedWidth = MeasureSpec.makeMeasureSpec(size, mode);
        daySpinner.clearNumberPickerPadding();
        monthSpinner.clearNumberPickerPadding();
        yearSpinner.clearNumberPickerPadding();
        measureChildConstrained(daySpinner, widthMeasureSpec, heightMeasureSpec);
        measureChildConstrained(monthSpinner, widthMeasureSpec, heightMeasureSpec);
        measureChildConstrained(yearSpinner, widthMeasureSpec, heightMeasureSpec);
        int sidePadding = (size - daySpinner.getMeasuredWidth() - monthSpinner.getMeasuredWidth()
                - yearSpinner.getMeasuredWidth()) / 2;
        if (spinners.getChildAt(0) instanceof COUINumberPicker) {
            ((COUINumberPicker) spinners.getChildAt(0)).setNumberPickerPaddingLeft(sidePadding);
        }
        int last = spinners.getChildCount() - 1;
        if (spinners.getChildAt(last) instanceof COUINumberPicker) {
            ((COUINumberPicker) spinners.getChildAt(last)).setNumberPickerPaddingRight(sidePadding);
        }
        super.onMeasure(constrainedWidth, heightMeasureSpec);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        setDate(savedState.year, savedState.month, savedState.day);
        updateSpinners();
        updateCalendarView();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        return new SavedState(super.onSaveInstanceState(), getYear(), getMonth(), getDayOfMonth());
    }

    public void refresh() {
        daySpinner.refresh();
        monthSpinner.refresh();
        yearSpinner.refresh();
    }

    public void scrollForceFinished() {
        daySpinner.scrollForceFinished();
        monthSpinner.scrollForceFinished();
        yearSpinner.scrollForceFinished();
    }

    public void setCalendarViewShown(boolean shown) {
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (isEnabled == enabled) {
            return;
        }
        super.setEnabled(enabled);
        daySpinner.setEnabled(enabled);
        monthSpinner.setEnabled(enabled);
        yearSpinner.setEnabled(enabled);
        isEnabled = enabled;
    }

    public void setMaxDate(long maxDateMillis) {
        tempDate.setTimeInMillis(maxDateMillis);
        if (tempDate.get(Calendar.YEAR) != sMaxDate.get(Calendar.YEAR)
                || tempDate.get(Calendar.DAY_OF_YEAR) == sMaxDate.get(Calendar.DAY_OF_YEAR)) {
            sMaxDate.setTimeInMillis(maxDateMillis);
            if (currentDate.after(sMaxDate)) {
                currentDate.setTimeInMillis(sMaxDate.getTimeInMillis());
                updateCalendarView();
            }
            updateSpinners();
        }
    }

    public void setMinDate(long minDateMillis) {
        tempDate.setTimeInMillis(minDateMillis);
        if (tempDate.get(Calendar.YEAR) != sMinDate.get(Calendar.YEAR)
                || tempDate.get(Calendar.DAY_OF_YEAR) == sMinDate.get(Calendar.DAY_OF_YEAR)) {
            sMinDate.setTimeInMillis(minDateMillis);
            if (currentDate.before(sMinDate)) {
                currentDate.setTimeInMillis(sMinDate.getTimeInMillis());
                updateCalendarView();
            }
            updateSpinners();
        }
    }

    public void setNormalTextColor(int color) {
        daySpinner.setNormalTextColor(color);
        monthSpinner.setNormalTextColor(color);
        yearSpinner.setNormalTextColor(color);
    }

    public void setOnDateChangedListener(OnDateChangedListener listener) {
        onDateChangedListener = listener;
    }

    public void setSpinnersShown(boolean shown) {
        spinners.setVisibility(shown ? VISIBLE : GONE);
    }

    public void setVibrateIntensity(float intensity) {
        daySpinner.setVibrateIntensity(intensity);
        monthSpinner.setVibrateIntensity(intensity);
        yearSpinner.setVibrateIntensity(intensity);
    }

    public void setVibrateLevel(int level) {
        daySpinner.setVibrateLevel(level);
        monthSpinner.setVibrateLevel(level);
        yearSpinner.setVibrateLevel(level);
    }

    public void updateDate(int year, int month, int dayOfMonth) {
        if (isNewDate(year, month, dayOfMonth)) {
            setDate(year, month, dayOfMonth);
            updateSpinners();
            updateCalendarView();
            notifyDateChanged();
        }
    }

    public void updateCalendarView() {
    }

    private void clampDate() {
        currentDate.clampDate(sMinDate, sMaxDate);
    }

    private IncompleteDate getCalendarForLocale(IncompleteDate date, Locale locale) {
        if (date == null) {
            return new IncompleteDate(locale);
        }
        IncompleteDate result = new IncompleteDate(locale);
        if (date.mIsIncomplete) {
            result.setWith(date);
        } else {
            result.setTimeInMillis(date.getTimeInMillis());
        }
        return result;
    }

    private Calendar getCalendarForLocale(Calendar calendar, Locale locale) {
        if (calendar == null) {
            return Calendar.getInstance(locale);
        }
        long millis = calendar.getTimeInMillis();
        Calendar result = Calendar.getInstance(locale);
        result.setTimeInMillis(millis);
        return result;
    }

    private boolean isNewDate(int year, int month, int day) {
        return currentDate.get(Calendar.YEAR) != year
                || currentDate.get(Calendar.MONTH) != month
                || currentDate.get(Calendar.DAY_OF_MONTH) != day;
    }

    private void measureChildConstrained(View child, int parentWidthSpec, int parentHeightSpec) {
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) child.getLayoutParams();
        child.measure(ViewGroup.getChildMeasureSpec(parentWidthSpec,
                        getPaddingLeft() + getPaddingRight() + lp.leftMargin + lp.rightMargin, lp.width),
                ViewGroup.getChildMeasureSpec(parentHeightSpec,
                        getPaddingTop() + getPaddingBottom() + lp.topMargin + lp.bottomMargin, lp.height));
    }

    private void notifyDateChanged() {
        if (onDateChangedListener != null) {
            onDateChangedListener.onLunarDateChanged(this, getYear(), getMonth(), getDayOfMonth());
        }
    }

    private void reorderSpinners() {
        spinners.removeAllViews();
        char[] order = DateFormat.getDateFormatOrder(getContext());
        for (char field : order) {
            if (field == 'M') {
                spinners.addView(monthSpinner);
            } else if (field == 'd') {
                spinners.addView(daySpinner);
            } else if (field == 'y') {
                spinners.addView(yearSpinner);
            } else {
                throw new IllegalArgumentException();
            }
        }
    }

    private void setCurrentLocale(Locale locale) {
        if (locale.equals(currentLocale)) {
            return;
        }
        currentLocale = locale;
        tempDate = getCalendarForLocale(tempDate, locale);
        sMinDate = getCalendarForLocale(sMinDate, locale);
        sMaxDate = getCalendarForLocale(sMaxDate, locale);
        currentDate = getCalendarForLocale(currentDate, locale);
    }

    private void setDate(int year, int month, int day) {
        currentDate.set(year, month, day);
        clampDate();
    }

    private void setDate(IncompleteDate date) {
        currentDate.setWith(date);
        clampDate();
    }

    private void updateSpinners() {
        int year = currentDate.get(Calendar.YEAR);
        int[] lunar = COUILunarUtil.calculateLunarByGregorian(year,
                currentDate.get(Calendar.MONTH) + 1, currentDate.get(Calendar.DAY_OF_MONTH));
        int leapMonth = COUILunarUtil.leapMonth(lunar[0]);
        int monthIndex = lunar[1];
        String dateText = getLunarDateString2(currentDate);

        if (leapMonth == 0 || (monthIndex < leapMonth && leapMonth != 0)
                || (monthIndex == leapMonth && !dateText.contains(sLeapString))) {
            monthIndex--;
        }
        if (year == IGNORED_YEAR && lunar[3] == COUILunarUtil.LEAP_MONTH) {
            monthIndex += NORMAL_MONTH_COUNT;
        }

        boolean hasLeapMonth;
        if (year == IGNORED_YEAR) {
            numberOfMonths = IGNORED_YEAR_MONTH_COUNT;
            hasLeapMonth = false;
        } else if (leapMonth != 0) {
            numberOfMonths = LEAPYEAR_MONTH_COUNT;
            hasLeapMonth = true;
        } else {
            numberOfMonths = NORMAL_MONTH_COUNT;
            hasLeapMonth = false;
        }

        int maxDay = COUILunarUtil.daysOfALunarMonth(lunar[0], lunar[1]);
        if (leapMonth != 0 && monthIndex == leapMonth && dateText.contains(sLeapString)) {
            maxDay = COUILunarUtil.daysOfLeapMonthInLunarYear(lunar[0]);
        }

        if (currentDate.beforeOrEqual(sMinDate)) {
            daySpinner.setDisplayedValues(null);
            setDayRange(lunar[2], maxDay);
            daySpinner.setWrapSelectorWheel(false);
            monthSpinner.setDisplayedValues(null);
            setMonthRange(monthIndex, numberOfMonths - 1);
            monthSpinner.setWrapSelectorWheel(false);
        } else if (currentDate.afterOrEqual(sMaxDate)) {
            daySpinner.setDisplayedValues(null);
            setDayRange(1, lunar[2]);
            daySpinner.setWrapSelectorWheel(false);
            monthSpinner.setDisplayedValues(null);
            setMonthRange(0, monthIndex);
            monthSpinner.setWrapSelectorWheel(false);
        } else {
            daySpinner.setDisplayedValues(null);
            setDayRange(1, maxDay);
            daySpinner.setWrapSelectorWheel(true);
            monthSpinner.setDisplayedValues(null);
            setMonthRange(0, numberOfMonths - 1);
            monthSpinner.setWrapSelectorWheel(true);
        }

        monthSpinner.setDisplayedValues(buildMonthDisplayedValues(year, hasLeapMonth, leapMonth));
        daySpinner.setDisplayedValues(buildDayDisplayedValues());

        int[] minLunar = COUILunarUtil.calculateLunarByGregorian(sMinDate.get(Calendar.YEAR),
                sMinDate.get(Calendar.MONTH) + 1, sMinDate.get(Calendar.DAY_OF_MONTH));
        int[] maxLunar = COUILunarUtil.calculateLunarByGregorian(sMaxDate.get(Calendar.YEAR),
                sMaxDate.get(Calendar.MONTH) + 1, sMaxDate.get(Calendar.DAY_OF_MONTH));
        yearSpinner.setMinValue(minLunar[0]);
        yearSpinner.setMaxValue(maxLunar[0]);
        yearSpinner.setWrapSelectorWheel(true);
        yearSpinner.setValue(lunar[0]);
        monthSpinner.setValue(monthIndex);
        daySpinner.setValue(lunar[2]);
    }

    private String[] buildMonthDisplayedValues(int year, boolean hasLeapMonth, int leapMonth) {
        String[] values;
        if (year == IGNORED_YEAR) {
            values = new String[IGNORED_YEAR_MONTH_COUNT];
            for (int i = 0; i < IGNORED_YEAR_MONTH_COUNT; i++) {
                if (i < NORMAL_MONTH_COUNT) {
                    values[i] = shortMonths[i];
                } else {
                    values[i] = sLeapString + shortMonths[i - NORMAL_MONTH_COUNT];
                }
            }
            return values;
        }
        if (hasLeapMonth) {
            values = new String[LEAPYEAR_MONTH_COUNT];
            for (int i = 0; i < leapMonth; i++) {
                values[i] = shortMonths[i];
            }
            values[leapMonth] = sLeapString + shortMonths[leapMonth - 1];
            for (int i = leapMonth + 1; i < LEAPYEAR_MONTH_COUNT; i++) {
                values[i] = shortMonths[i - 1];
            }
        } else {
            values = shortMonths;
        }
        return Arrays.copyOfRange(values, monthMinValue, monthMaxValue + 1);
    }

    private String[] buildDayDisplayedValues() {
        int min = dayMinValue;
        int max = dayMaxValue;
        String[] values = new String[(max - min) + 1];
        for (int day = min; day <= max; day++) {
            values[day - min] = COUILunarUtil.chineseStringOfALunarDay(day);
        }
        return values;
    }

    private void setDayRange(int min, int max) {
        dayMinValue = min;
        dayMaxValue = max;
        daySpinner.setMinValue(min);
        daySpinner.setMaxValue(max);
    }

    private void setMonthRange(int min, int max) {
        monthMinValue = min;
        monthMaxValue = max;
        monthSpinner.setMinValue(min);
        monthSpinner.setMaxValue(max);
    }

    private static String getLunarDateString(int year, int month, int day, int leapState) {
        if (month <= 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        if (year != IGNORED_YEAR) {
            builder.append(year).append("年");
        }
        builder.append(leapState == COUILunarUtil.LEAP_MONTH ? sLeapString : "");
        builder.append(CHINESE_NUMBER[month - 1]).append("月");
        builder.append(COUILunarUtil.chineseStringOfALunarDay(day));
        return builder.toString();
    }

    public static class IncompleteDate {
        public static final int LEAP_MONTH_ADDED_VALUE = 12;

        private Calendar mDate;
        private int mYear;
        private int mMonth;
        private int mDay;
        private int mHour;
        private int mMinute;
        private boolean mIsIncomplete;

        public IncompleteDate() {
            init(Calendar.getInstance());
        }

        public IncompleteDate(Locale locale) {
            init(Calendar.getInstance(locale));
        }

        public void add(int field, int amount) {
            if (!mIsIncomplete) {
                mDate.add(field, amount);
            } else if (field == Calendar.DAY_OF_MONTH) {
                mDay += amount;
            } else if (field == Calendar.MONTH) {
                mMonth += amount;
            }
        }

        public boolean after(Calendar calendar) {
            return !mIsIncomplete && mDate.after(calendar);
        }

        public boolean afterOrEqual(Calendar calendar) {
            return !mIsIncomplete && (mDate.after(calendar) || mDate.equals(calendar));
        }

        public boolean before(Calendar calendar) {
            return !mIsIncomplete && mDate.before(calendar);
        }

        public boolean beforeOrEqual(Calendar calendar) {
            return !mIsIncomplete && (mDate.before(calendar) || mDate.equals(calendar));
        }

        public void change(int field, int oldValue, int newValue) {
            int[] lunar = COUILunarUtil.calculateLunarByGregorian(get(Calendar.YEAR),
                    get(Calendar.MONTH) + 1, get(Calendar.DAY_OF_MONTH));
            if (field == Calendar.DAY_OF_MONTH) {
                if (!mIsIncomplete) {
                    if (oldValue > 27 && newValue == 1) {
                        mDate.add(Calendar.DAY_OF_MONTH, 1 - oldValue);
                    } else if (oldValue == 1 && newValue > 27) {
                        mDate.add(Calendar.DAY_OF_MONTH, newValue - 1);
                    } else {
                        mDate.add(Calendar.DAY_OF_MONTH, newValue - oldValue);
                    }
                } else {
                    mDay = newValue;
                }
                return;
            }
            if (field == Calendar.MONTH) {
                if (!mIsIncomplete) {
                    int lunarMonth = newValue + 1;
                    boolean leap = false;
                    int leapMonth = COUILunarUtil.leapMonth(lunar[0]);
                    if (leapMonth != 0) {
                        if (lunarMonth > leapMonth) {
                            if (lunarMonth == leapMonth + 1) {
                                lunarMonth = leapMonth;
                                leap = true;
                            } else {
                                lunarMonth--;
                            }
                        }
                    }
                    int day = COUILunarUtil.clampDay(lunar[0], lunarMonth, lunar[2], leap);
                    Date date = COUILunarUtil.lunarToSolar(lunar[0], lunarMonth, day, leap);
                    if (date != null) {
                        setTimeInMillis(date.getTime());
                    }
                } else {
                    mMonth = newValue;
                }
                return;
            }
            if (field == Calendar.YEAR) {
                if (!mIsIncomplete && newValue != IGNORED_YEAR) {
                    setWith(COUILunarUtil.changeALunarYear(newValue, lunar[1], lunar[2], lunar[3]));
                } else if (!mIsIncomplete) {
                    mIsIncomplete = true;
                    mYear = newValue;
                    mMonth = (lunar[1] - 1) + (lunar[3] == COUILunarUtil.NORMAL_MONTH ? 0 : LEAP_MONTH_ADDED_VALUE);
                    mDay = lunar[2];
                    mHour = mDate.get(Calendar.HOUR_OF_DAY);
                    mMinute = mDate.get(Calendar.MINUTE);
                } else if (newValue != IGNORED_YEAR) {
                    mIsIncomplete = false;
                    mYear = newValue;
                    int lunarMonth = (mMonth % LEAP_MONTH_ADDED_VALUE) + 1;
                    boolean leap = mMonth / LEAP_MONTH_ADDED_VALUE > 0
                            && COUILunarUtil.leapMonth(mYear) == lunarMonth;
                    mDay = COUILunarUtil.clampDay(mYear, lunarMonth, mDay, leap);
                    Date date = COUILunarUtil.lunarToSolar(mYear, lunarMonth, mDay, leap);
                    if (date != null) {
                        setTimeInMillis(date.getTime());
                    }
                } else {
                    mYear = newValue;
                }
            }
        }

        public void clampDate(Calendar min, Calendar max) {
            if (mIsIncomplete) {
                return;
            }
            if (mDate.before(min)) {
                setTimeInMillis(min.getTimeInMillis());
            } else if (mDate.after(max)) {
                setTimeInMillis(max.getTimeInMillis());
            }
        }

        public void clear() {
            mDate.clear();
            mYear = 0;
            mMonth = 0;
            mDay = 0;
            mHour = 0;
            mMinute = 0;
            mIsIncomplete = false;
        }

        public int get(int field) {
            if (!mIsIncomplete) {
                return mDate.get(field);
            }
            if (field == Calendar.DAY_OF_MONTH) {
                return mDay;
            }
            if (field == Calendar.MONTH) {
                return mMonth;
            }
            if (field == Calendar.YEAR) {
                return mYear;
            }
            return mDate.get(field);
        }

        public int getActualMaximum(int field) {
            return mDate.getActualMaximum(field);
        }

        public int getActualMinimum(int field) {
            return mDate.getActualMinimum(field);
        }

        public Date getTime() {
            return mDate.getTime();
        }

        public long getTimeInMillis() {
            return mDate.getTimeInMillis();
        }

        public void init(Calendar calendar) {
            mDate = calendar;
            mIsIncomplete = false;
        }

        public void set(int year, int month, int day) {
            if (year != IGNORED_YEAR) {
                mDate.set(Calendar.YEAR, year);
                mDate.set(Calendar.MONTH, month);
                mDate.set(Calendar.DAY_OF_MONTH, day);
                mIsIncomplete = false;
            } else {
                mYear = IGNORED_YEAR;
                mMonth = month;
                mDay = day;
                mIsIncomplete = true;
            }
        }

        public void set(int year, int month, int day, int hour, int minute) {
            if (year != IGNORED_YEAR) {
                mDate.set(Calendar.YEAR, year);
                mDate.set(Calendar.MONTH, month);
                mDate.set(Calendar.DAY_OF_MONTH, day);
                mDate.set(Calendar.HOUR_OF_DAY, hour);
                mDate.set(Calendar.MINUTE, minute);
                mIsIncomplete = false;
            } else {
                mYear = IGNORED_YEAR;
                mMonth = month;
                mDay = day;
                mHour = hour;
                mMinute = minute;
                mIsIncomplete = true;
            }
        }

        public void setTimeInMillis(long millis) {
            mDate.setTimeInMillis(millis);
            mIsIncomplete = false;
        }

        public void setWith(IncompleteDate date) {
            mDate.setTimeInMillis(date.mDate.getTimeInMillis());
            mYear = date.mYear;
            mMonth = date.mMonth;
            mDay = date.mDay;
            mHour = date.mHour;
            mMinute = date.mMinute;
            mIsIncomplete = date.mIsIncomplete;
        }
    }

    public static class SavedState extends BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel source) {
                return new SavedState(source);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };

        private final int year;
        private final int month;
        private final int day;

        private SavedState(Parcelable superState, int year, int month, int day) {
            super(superState);
            this.year = year;
            this.month = month;
            this.day = day;
        }

        private SavedState(Parcel source) {
            super(source);
            year = source.readInt();
            month = source.readInt();
            day = source.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(year);
            out.writeInt(month);
            out.writeInt(day);
        }
    }
}
