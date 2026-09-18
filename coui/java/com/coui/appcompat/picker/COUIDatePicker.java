package com.coui.appcompat.picker;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.text.format.DateUtils;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;

public class COUIDatePicker extends FrameLayout {
    public static final int IGNORED_YEAR = Integer.MIN_VALUE;
    private static final String DATE_FORMAT = "MM/dd/yyyy";
    private static final int DEFAULT_START_YEAR = 1900;
    private static final int DEFAULT_END_YEAR = 2100;
    private static final int LONGPRESS_UPDATE_INTERVAL = 100;
    private static final int MONTH_LONGPRESS_UPDATE_INTERVAL = 200;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.US);
    private Context context;
    private Locale currentLocale;
    private Calendar minDate;
    private Calendar maxDate;
    private Calendar currentDate;
    private Calendar tempDate;
    private COUINumberPicker daySpinner;
    private COUINumberPicker monthSpinner;
    private COUINumberPicker yearSpinner;
    private LinearLayout spinners;
    private String[] shortMonths;
    private boolean isEnabled = true;
    private boolean yearIgnorable;
    private int maxWidth;
    private int backgroundLeft;
    private int backgroundRadius;
    private int backgroundDividerHeight;
    private int leftPickerPosition = -1;
    private int rightPickerPosition = -1;
    private OnDateChangedListener onDateChangedListener;

    public interface OnDateChangedListener {
        void onDateChanged(COUIDatePicker view, int year, int monthOfYear, int dayOfMonth);
    }

    public COUIDatePicker(Context context) {
        this(context, null);
    }

    public COUIDatePicker(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.couiDatePickerStyle);
    }

    public COUIDatePicker(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.DatePickerStyle);
    }

    public COUIDatePicker(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
        COUIDarkModeUtil.setForceDarkAllow(this, false);
        setCurrentLocale(Locale.getDefault());
        TypedArray pickerAttrs = context.obtainStyledAttributes(attrs, R.styleable.COUIDatePicker, defStyleAttr, defStyleRes);
        int startYear = pickerAttrs.getInt(R.styleable.COUIDatePicker_startYear, DEFAULT_START_YEAR);
        int endYear = pickerAttrs.getInt(R.styleable.COUIDatePicker_endYear, DEFAULT_END_YEAR);
        String minDateText = pickerAttrs.getString(R.styleable.COUIDatePicker_minDate);
        String maxDateText = pickerAttrs.getString(R.styleable.COUIDatePicker_maxDate);
        boolean spinnersShown = pickerAttrs.getBoolean(R.styleable.COUIDatePicker_spinnersShown, true);
        yearIgnorable = pickerAttrs.getBoolean(R.styleable.COUIDatePicker_couiYearIgnorable, false);
        pickerAttrs.recycle();

        TypedArray commonAttrs = context.obtainStyledAttributes(attrs, R.styleable.COUIPickersCommonAttrs, defStyleAttr, defStyleRes);
        maxWidth = commonAttrs.getDimensionPixelSize(R.styleable.COUIPickersCommonAttrs_couiPickersMaxWidth, 0);
        commonAttrs.recycle();

        backgroundDividerHeight = Math.max(getResources().getDimensionPixelOffset(R.dimen.coui_number_picker_background_divider_height), 1);
        LayoutInflater.from(context).inflate(R.layout.coui_date_picker, this, true);
        spinners = findViewById(R.id.pickers);
        daySpinner = findViewById(R.id.day);
        monthSpinner = findViewById(R.id.month);
        yearSpinner = findViewById(R.id.year);
        shortMonths = getResources().getStringArray(R.array.coui_solor_mounth);

        COUINumberPicker.OnValueChangeListener listener = (picker, oldVal, newVal) -> {
            tempDate.setTimeInMillis(currentDate.getTimeInMillis());
            if (picker == daySpinner) {
                tempDate.set(Calendar.DAY_OF_MONTH, newVal);
            } else if (picker == monthSpinner) {
                tempDate.set(Calendar.MONTH, newVal);
            } else if (picker == yearSpinner) {
                tempDate.set(Calendar.YEAR, newVal);
            }
            setDate(tempDate.get(Calendar.YEAR), tempDate.get(Calendar.MONTH), tempDate.get(Calendar.DAY_OF_MONTH));
            updateSpinners();
            updateCalendarView();
            notifyDateChanged();
        };
        COUINumberPicker.OnScrollingStopListener stopListener = () -> announceForAccessibility(formatDate());
        daySpinner.setOnLongPressUpdateInterval(LONGPRESS_UPDATE_INTERVAL);
        daySpinner.setOnValueChangedListener(listener);
        daySpinner.setOnScrollingStopListener(stopListener);
        monthSpinner.setOnLongPressUpdateInterval(MONTH_LONGPRESS_UPDATE_INTERVAL);
        monthSpinner.setOnValueChangedListener(listener);
        monthSpinner.setOnScrollingStopListener(stopListener);
        yearSpinner.setOnLongPressUpdateInterval(LONGPRESS_UPDATE_INTERVAL);
        yearSpinner.setOnValueChangedListener(listener);
        yearSpinner.setOnScrollingStopListener(stopListener);
        yearSpinner.setIgnorable(yearIgnorable);

        if (TextUtils.isEmpty(minDateText) || !parseDate(minDateText, minDate)) {
            minDate.set(startYear, Calendar.JANUARY, 1);
        }
        if (TextUtils.isEmpty(maxDateText) || !parseDate(maxDateText, maxDate)) {
            maxDate.set(endYear, Calendar.DECEMBER, 31);
        }
        currentDate.setTimeInMillis(System.currentTimeMillis());
        init(currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DAY_OF_MONTH), null);
        setSpinnersShown(spinnersShown);
        reorderSpinners();
        backgroundRadius = getResources().getDimensionPixelOffset(R.dimen.coui_selected_background_radius);
        backgroundLeft = getResources().getDimensionPixelOffset(R.dimen.coui_selected_background_horizontal_padding);
        if (getImportantForAccessibility() == IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
            setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
        }
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

    public String formatDate() {
        return DateUtils.formatDateTime(context, currentDate.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE);
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

    public long getMaxDate() {
        return maxDate.getTimeInMillis();
    }

    public long getMinDate() {
        return minDate.getTimeInMillis();
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
        int remaining = size;
        for (int i = 0; i < spinners.getChildCount(); i++) {
            View child = spinners.getChildAt(i);
            if (child instanceof COUINumberPicker) {
                ((COUINumberPicker) child).clearNumberPickerPadding();
                measureChildConstrained(child, widthMeasureSpec, heightMeasureSpec);
                remaining -= child.getMeasuredWidth();
            }
        }
        int sidePadding = Math.max(0, remaining / 2);
        if (spinners.getChildAt(0) instanceof COUINumberPicker) {
            ((COUINumberPicker) spinners.getChildAt(0)).setNumberPickerPaddingLeft(sidePadding);
        }
        int last = spinners.getChildCount() - 1;
        if (last >= 0 && spinners.getChildAt(last) instanceof COUINumberPicker) {
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

    @Override
    public void setBackground(Drawable background) {
        super.setBackground(background);
    }

    public void setBackground(int color) {
        setBackgroundColor(color);
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

    public void setFocusColor(int color) {
        daySpinner.setPickerFocusColor(color);
        monthSpinner.setPickerFocusColor(color);
        yearSpinner.setPickerFocusColor(color);
    }

    public void setMaxDate(long maxDateMillis) {
        tempDate.setTimeInMillis(maxDateMillis);
        if (tempDate.get(Calendar.YEAR) == maxDate.get(Calendar.YEAR)
                && tempDate.get(Calendar.DAY_OF_YEAR) == maxDate.get(Calendar.DAY_OF_YEAR)) {
            return;
        }
        maxDate.setTimeInMillis(maxDateMillis);
        if (currentDate.after(maxDate)) {
            currentDate.setTimeInMillis(maxDate.getTimeInMillis());
            updateCalendarView();
        }
        updateSpinners();
    }

    public void setMinDate(long minDateMillis) {
        tempDate.setTimeInMillis(minDateMillis);
        if (tempDate.get(Calendar.YEAR) == minDate.get(Calendar.YEAR)
                && tempDate.get(Calendar.DAY_OF_YEAR) == minDate.get(Calendar.DAY_OF_YEAR)) {
            return;
        }
        minDate.setTimeInMillis(minDateMillis);
        if (currentDate.before(minDate)) {
            currentDate.setTimeInMillis(minDate.getTimeInMillis());
            updateCalendarView();
        }
        updateSpinners();
    }

    public void setNormalColor(int color) {
        daySpinner.setPickerNormalColor(color);
        monthSpinner.setPickerNormalColor(color);
        yearSpinner.setPickerNormalColor(color);
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

    public void updateCalendarView() {
    }

    public void updateDate(int year, int month, int dayOfMonth) {
        if (isNewDate(year, month, dayOfMonth)) {
            setDate(year, month, dayOfMonth);
            updateSpinners();
            updateCalendarView();
            notifyDateChanged();
        }
    }

    public void updateSpinners() {
        monthSpinner.setFormatter(new DateFormatter(R.string.coui_month, "MONTH"));
        if (currentDate.get(Calendar.YEAR) == minDate.get(Calendar.YEAR)
                && currentDate.get(Calendar.YEAR) != maxDate.get(Calendar.YEAR)) {
            monthSpinner.setMinValue(minDate.get(Calendar.MONTH));
            monthSpinner.setMaxValue(minDate.getActualMaximum(Calendar.MONTH));
            monthSpinner.setWrapSelectorWheel(minDate.get(Calendar.MONTH) == Calendar.JANUARY);
        } else if (currentDate.get(Calendar.YEAR) != minDate.get(Calendar.YEAR)
                && currentDate.get(Calendar.YEAR) == maxDate.get(Calendar.YEAR)) {
            monthSpinner.setMinValue(0);
            monthSpinner.setMaxValue(maxDate.get(Calendar.MONTH));
            monthSpinner.setWrapSelectorWheel(maxDate.get(Calendar.MONTH) == maxDate.getActualMaximum(Calendar.MONTH));
        } else if (currentDate.get(Calendar.YEAR) == minDate.get(Calendar.YEAR)
                && currentDate.get(Calendar.YEAR) == maxDate.get(Calendar.YEAR)) {
            monthSpinner.setMinValue(minDate.get(Calendar.MONTH));
            monthSpinner.setMaxValue(maxDate.get(Calendar.MONTH));
            monthSpinner.setWrapSelectorWheel(maxDate.get(Calendar.MONTH) == maxDate.getActualMaximum(Calendar.MONTH)
                    && minDate.get(Calendar.MONTH) == Calendar.JANUARY);
        } else {
            monthSpinner.setMinValue(currentDate.getActualMinimum(Calendar.MONTH));
            monthSpinner.setMaxValue(currentDate.getActualMaximum(Calendar.MONTH));
            monthSpinner.setWrapSelectorWheel(true);
        }

        if (currentDate.get(Calendar.YEAR) == minDate.get(Calendar.YEAR)
                && currentDate.get(Calendar.MONTH) == minDate.get(Calendar.MONTH)
                && (currentDate.get(Calendar.YEAR) != maxDate.get(Calendar.YEAR)
                || currentDate.get(Calendar.MONTH) != maxDate.get(Calendar.MONTH))) {
            daySpinner.setMinValue(minDate.get(Calendar.DAY_OF_MONTH));
            daySpinner.setMaxValue(minDate.getActualMaximum(Calendar.DAY_OF_MONTH));
            daySpinner.setWrapSelectorWheel(minDate.get(Calendar.DAY_OF_MONTH) == 1);
        } else if (!(currentDate.get(Calendar.YEAR) == minDate.get(Calendar.YEAR)
                && currentDate.get(Calendar.MONTH) == minDate.get(Calendar.MONTH))
                && currentDate.get(Calendar.YEAR) == maxDate.get(Calendar.YEAR)
                && currentDate.get(Calendar.MONTH) == maxDate.get(Calendar.MONTH)) {
            daySpinner.setMinValue(1);
            daySpinner.setMaxValue(maxDate.get(Calendar.DAY_OF_MONTH));
            daySpinner.setWrapSelectorWheel(maxDate.get(Calendar.DAY_OF_MONTH)
                    == maxDate.getActualMaximum(Calendar.DAY_OF_MONTH));
        } else if (currentDate.get(Calendar.YEAR) == minDate.get(Calendar.YEAR)
                && currentDate.get(Calendar.MONTH) == minDate.get(Calendar.MONTH)
                && currentDate.get(Calendar.YEAR) == maxDate.get(Calendar.YEAR)
                && currentDate.get(Calendar.MONTH) == maxDate.get(Calendar.MONTH)) {
            daySpinner.setMinValue(minDate.get(Calendar.DAY_OF_MONTH));
            daySpinner.setMaxValue(maxDate.get(Calendar.DAY_OF_MONTH));
            daySpinner.setWrapSelectorWheel(maxDate.get(Calendar.DAY_OF_MONTH)
                    == maxDate.getActualMaximum(Calendar.DAY_OF_MONTH)
                    && minDate.get(Calendar.DAY_OF_MONTH) == 1);
        } else {
            daySpinner.setMinValue(currentDate.getActualMinimum(Calendar.DAY_OF_MONTH));
            daySpinner.setMaxValue(currentDate.getActualMaximum(Calendar.DAY_OF_MONTH));
            daySpinner.setWrapSelectorWheel(true);
        }

        yearSpinner.setMinValue(minDate.get(Calendar.YEAR));
        yearSpinner.setMaxValue(maxDate.get(Calendar.YEAR));
        yearSpinner.setWrapSelectorWheel(true);
        yearSpinner.setFormatter(new DateFormatter(R.string.coui_year, "YEAR"));
        yearSpinner.setValue(currentDate.get(Calendar.YEAR));
        monthSpinner.setValue(currentDate.get(Calendar.MONTH));
        daySpinner.setValue(currentDate.get(Calendar.DAY_OF_MONTH));
        daySpinner.setFormatter(new DateFormatter(R.string.coui_day, "DAY"));
        if (daySpinner.getValue() > 27) {
            daySpinner.invalidate();
        }
    }

    private void clampDate() {
        if (currentDate.before(minDate)) {
            currentDate.setTimeInMillis(minDate.getTimeInMillis());
        } else if (currentDate.after(maxDate)) {
            currentDate.setTimeInMillis(maxDate.getTimeInMillis());
        }
    }

    private boolean isNewDate(int year, int month, int dayOfMonth) {
        return currentDate.get(Calendar.YEAR) != year
                || currentDate.get(Calendar.MONTH) != month
                || currentDate.get(Calendar.DAY_OF_MONTH) != dayOfMonth;
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
            onDateChangedListener.onDateChanged(this, getYear(), getMonth(), getDayOfMonth());
        }
    }

    private boolean parseDate(String date, Calendar outDate) {
        try {
            outDate.setTime(dateFormat.parse(date));
            return true;
        } catch (ParseException | RuntimeException e) {
            return false;
        }
    }

    private void reorderSpinners() {
        String pattern = android.text.format.DateFormat.getBestDateTimePattern(Locale.getDefault(), "yyyyMMMMdd");
        if (isLayoutRtl()) {
            pattern = TextUtils.getReverse(pattern, 0, pattern.length()).toString();
        }
        spinners.removeAllViews();
        leftPickerPosition = -1;
        for (int i = 0; i < pattern.length(); i++) {
            char c = pattern.charAt(i);
            if (c == 'y' && yearSpinner.getParent() == null) {
                spinners.addView(yearSpinner);
            } else if (c == 'M' && monthSpinner.getParent() == null) {
                spinners.addView(monthSpinner);
            } else if (c == 'd' && daySpinner.getParent() == null) {
                spinners.addView(daySpinner);
            }
            if (spinners.getChildCount() > 0) {
                if (leftPickerPosition == -1) {
                    leftPickerPosition = spinners.getChildCount() - 1;
                }
                rightPickerPosition = spinners.getChildCount() - 1;
            }
        }
    }

    private void setCurrentLocale(Locale locale) {
        if (locale.equals(currentLocale)) {
            return;
        }
        currentLocale = locale;
        tempDate = getCalendarForLocale(tempDate, locale);
        minDate = getCalendarForLocale(minDate, locale);
        maxDate = getCalendarForLocale(maxDate, locale);
        currentDate = getCalendarForLocale(currentDate, locale);
    }

    private Calendar getCalendarForLocale(Calendar calendar, Locale locale) {
        if (calendar == null) {
            return Calendar.getInstance(locale);
        }
        long time = calendar.getTimeInMillis();
        Calendar result = Calendar.getInstance(locale);
        result.setTimeInMillis(time);
        return result;
    }

    private void setDate(int year, int month, int dayOfMonth) {
        currentDate.set(year, month, dayOfMonth);
        clampDate();
    }

    public void setDate(IncompleteDate incompleteDate) {
        currentDate.setTimeInMillis(incompleteDate.getTimeInMillis());
        clampDate();
    }

    private class DateFormatter implements COUINumberPicker.Formatter {
        private final int suffixId;
        private final String tag;

        DateFormatter(int suffixId, String tag) {
            this.suffixId = suffixId;
            this.tag = tag;
        }

        @Override
        public String format(int value) {
            if ("MONTH".equals(tag) && shortMonths != null && value >= 0 && value < shortMonths.length) {
                return shortMonths[value];
            }
            if (!Locale.getDefault().getLanguage().equals("zh")) {
                Formatter formatter = new Formatter(new StringBuilder(), currentLocale);
                if ("YEAR".equals(tag)) {
                    formatter.format("%d", value);
                } else {
                    formatter.format("%02d", value);
                }
                return formatter.toString();
            }
            return value + getResources().getString(suffixId);
        }
    }

    public static class IncompleteDate {
        private Calendar date;
        private boolean incomplete;

        public IncompleteDate(Locale locale) {
            date = Calendar.getInstance(locale);
        }

        public boolean after(Calendar calendar) {
            return !incomplete && date.after(calendar);
        }

        public boolean before(Calendar calendar) {
            return !incomplete && date.before(calendar);
        }

        public void clampDate(Calendar min, Calendar max) {
            if (incomplete) {
                return;
            }
            if (date.before(min)) {
                date.setTimeInMillis(min.getTimeInMillis());
            } else if (date.after(max)) {
                date.setTimeInMillis(max.getTimeInMillis());
            }
        }

        public int clampDay(int day) {
            return Math.min(day, date.getActualMaximum(Calendar.DAY_OF_MONTH));
        }

        public void clear() {
            date.clear();
            incomplete = false;
        }

        public int get(int field) {
            if (incomplete && field == Calendar.YEAR) {
                return IGNORED_YEAR;
            }
            return date.get(field);
        }

        public int getActualMaximum(int field) {
            return date.getActualMaximum(field);
        }

        public int getActualMinimum(int field) {
            return date.getActualMinimum(field);
        }

        public Date getTime() {
            return date.getTime();
        }

        public long getTimeInMillis() {
            return date.getTimeInMillis();
        }

        public void set(int field, int value) {
            if (field == Calendar.YEAR && value == IGNORED_YEAR) {
                incomplete = true;
                return;
            }
            if (field == Calendar.DAY_OF_MONTH) {
                date.set(field, clampDay(value));
            } else {
                date.set(field, value);
                if (field == Calendar.YEAR) {
                    incomplete = false;
                }
            }
        }

        public void set(int year, int month, int day) {
            if (year == IGNORED_YEAR) {
                incomplete = true;
                date.set(Calendar.MONTH, month);
                date.set(Calendar.DAY_OF_MONTH, day);
            } else {
                incomplete = false;
                date.set(year, month, day);
            }
        }

        public void setTimeInMillis(long millis) {
            incomplete = false;
            date.setTimeInMillis(millis);
        }

        public void setWith(IncompleteDate other) {
            incomplete = other.incomplete;
            date.setTimeInMillis(other.date.getTimeInMillis());
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
