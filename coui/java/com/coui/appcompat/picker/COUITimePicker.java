package com.coui.appcompat.picker;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.coui.appcompat.R;
import com.coui.appcompat.darkmode.COUIDarkModeUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class COUITimePicker extends FrameLayout {
    private static final long MILLISECOND_A_DAY = 86400000L;
    private static final int START_YEAR = 1900;

    private Context context;
    private Calendar calendar;
    private Calendar defaultCalendar;
    private Calendar todayCalendar;
    private Date endDate;
    private long startTime;
    private SimpleDateFormat outFormatter;
    private String[] textAMPM;
    private String[] textDates;
    private String[] dateNamesTemp;
    private String textToday;
    private String textDay;
    private int todayIndex = -1;
    private int amPm = -1;
    private int todayYear;
    private int todayMonth;
    private int todayDate;
    private int maxWidth;
    private int backgroundLeft;
    private int backgroundRadius;
    private int backgroundDividerHeight;
    private int leftPickerPosition = -1;
    private int rightPickerPosition = -1;
    private boolean minuteFiveStep;
    private COUINumberPicker pickerDate;
    private COUINumberPicker pickerHour;
    private COUINumberPicker pickerMinute;
    private COUINumberPicker pickerAmPm;
    private LinearLayout pickerLayout;
    private OnTimeChangeListener onTimeChangeListener;

    public interface OnTimeChangeListener {
        void onTimeChange(View view, Calendar calendar);
    }

    public COUITimePicker(Context context) {
        this(context, null);
    }

    public COUITimePicker(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.couiTimePickerStyle);
    }

    public COUITimePicker(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.COUINumberPicker);
    }

    public COUITimePicker(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        COUIDarkModeUtil.setForceDarkAllow(this, false);
        this.context = context;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.COUIPickersCommonAttrs, defStyleAttr, defStyleRes);
        maxWidth = a.getDimensionPixelSize(R.styleable.COUIPickersCommonAttrs_couiPickersMaxWidth, 0);
        a.recycle();
        textAMPM = context.getResources().getStringArray(R.array.coui_time_picker_ampm);
        textToday = context.getString(R.string.coui_time_picker_today);
        textDay = context.getString(R.string.coui_time_picker_day);
        calendar = Calendar.getInstance();
        todayCalendar = Calendar.getInstance();
        todayYear = todayCalendar.get(Calendar.YEAR);
        todayMonth = todayCalendar.get(Calendar.MONTH);
        todayDate = todayCalendar.get(Calendar.DAY_OF_MONTH);
        outFormatter = new SimpleDateFormat("yyyy MMM dd" + textDay + " E", Locale.getDefault());
        ViewGroup root = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.coui_time_picker, this, true);
        pickerDate = root.findViewById(R.id.coui_time_picker_date);
        pickerHour = root.findViewById(R.id.coui_time_picker_hour);
        pickerMinute = root.findViewById(R.id.coui_time_picker_minute);
        pickerAmPm = root.findViewById(R.id.coui_time_picker_ampm);
        pickerLayout = root.findViewById(R.id.pickers);
        backgroundRadius = getResources().getDimensionPixelOffset(R.dimen.coui_selected_background_radius);
        backgroundLeft = getResources().getDimensionPixelOffset(R.dimen.coui_selected_background_horizontal_padding);
        backgroundDividerHeight = Math.max(getResources().getDimensionPixelOffset(R.dimen.coui_number_picker_background_divider_height), 1);
        if (!Locale.getDefault().getLanguage().equals("zh") && !Locale.getDefault().getLanguage().equals("en")) {
            pickerDate.getLayoutParams().width = getResources().getDimensionPixelOffset(R.dimen.coui_number_picker_width_biggest);
        }
        reorderSpinners();
        if (pickerHour.isAccessibilityEnable()) {
            pickerHour.addTalkbackSuffix(context.getString(R.string.coui_hour_abbreviation));
            pickerMinute.addTalkbackSuffix(context.getString(R.string.coui_minute_abbreviation));
            pickerAmPm.addTalkbackSuffix(context.getString(R.string.coui_minute_abbreviation));
        }
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
        getTimePicker();
    }

    @Override
    public void dispatchDraw(Canvas canvas) {
        int left = is24Hours() ? backgroundLeft : 0;
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(pickerDate.getBackgroundColor());
        canvas.drawRect(left, (getHeight() / 2f) - backgroundRadius,
                getWidth() - left, (getHeight() / 2f) - backgroundRadius + backgroundDividerHeight, paint);
        canvas.drawRect(left, (getHeight() / 2f) + backgroundRadius,
                getWidth() - left, (getHeight() / 2f) + backgroundRadius + backgroundDividerHeight, paint);
        super.dispatchDraw(canvas);
    }

    public COUINumberPicker getPickerAmPm() {
        return pickerAmPm;
    }

    public COUINumberPicker getPickerDate() {
        return pickerDate;
    }

    public COUINumberPicker getPickerHour() {
        return pickerHour;
    }

    public COUINumberPicker getPickerMinute() {
        return pickerMinute;
    }

    public View getTimePicker() {
        Calendar source = defaultCalendar != null ? defaultCalendar : todayCalendar;
        calendar.setTimeZone(source.getTimeZone());
        outFormatter.setTimeZone(source.getTimeZone());
        calendar.setTimeInMillis(source.getTimeInMillis());
        int year = source.get(Calendar.YEAR);
        int month = source.get(Calendar.MONTH);
        int day = source.get(Calendar.DAY_OF_MONTH);
        int hour = source.get(Calendar.HOUR_OF_DAY);
        int minute = source.get(Calendar.MINUTE);
        int monthOneBased = month + 1;

        int dayCount = 36500;
        for (int i = 0; i < 100; i++) {
            dayCount += getDaysAmountOfYear((year - 50) + i);
        }
        int currentIndex = 0;
        for (int i = 0; i < 50; i++) {
            currentIndex += getDaysAmountOfYear((year - 50) + i);
        }
        if (monthOneBased > 2 && !isLeapYear(year - 50) && isLeapYear(year)) {
            currentIndex++;
        }
        if (monthOneBased > 2 && isLeapYear(year - 50)) {
            currentIndex--;
        }

        textDates = new String[dayCount];
        dateNamesTemp = textDates.clone();
        Calendar startCalendar = Calendar.getInstance(source.getTimeZone());
        startCalendar.set(year, month, day, hour, minute);
        if (isLeapYear(year) && monthOneBased == 2 && day == 29) {
            startCalendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        startCalendar.add(Calendar.YEAR, -50);
        startTime = startCalendar.getTimeInMillis();
        endDate = new Date();

        if (is24Hours()) {
            pickerHour.setMinValue(0);
            pickerHour.setMaxValue(23);
            pickerHour.setTwoDigitFormatter();
            pickerHour.setValue(hour);
            pickerAmPm.setVisibility(GONE);
        } else {
            pickerHour.setMinValue(1);
            pickerHour.setMaxValue(12);
            pickerHour.setTwoDigitFormatter();
            int hour12 = source.get(Calendar.HOUR);
            pickerHour.setValue(hour12 == 0 ? 12 : hour12);
            pickerAmPm.setMinValue(0);
            pickerAmPm.setMaxValue(textAMPM.length - 1);
            pickerAmPm.setDisplayedValues(textAMPM);
            pickerAmPm.setWrapSelectorWheel(false);
            pickerAmPm.setValue(source.get(Calendar.AM_PM));
            pickerAmPm.setVisibility(VISIBLE);
            amPm = source.get(Calendar.AM_PM);
        }
        pickerHour.setWrapSelectorWheel(true);

        pickerMinute.setMinValue(0);
        if (minuteFiveStep) {
            pickerMinute.setMaxValue(11);
            String[] minutes = new String[12];
            for (int i = 0; i < minutes.length; i++) {
                minutes[i] = String.format(Locale.getDefault(), "%02d", i * 5);
            }
            pickerMinute.setDisplayedValues(minutes);
            pickerMinute.setValue(minute / 5);
            calendar.set(Calendar.MINUTE, (minute / 5) * 5);
        } else {
            pickerMinute.setMaxValue(59);
            pickerMinute.setValue(minute);
        }
        pickerMinute.setTwoDigitFormatter();
        pickerMinute.setWrapSelectorWheel(true);

        pickerDate.setMinValue(1);
        pickerDate.setMaxValue(dayCount);
        pickerDate.setWrapSelectorWheel(false);
        pickerDate.setValue(currentIndex);
        pickerDate.setFormatter(new DateFormatter());

        bindListeners();
        return this;
    }

    public boolean isLayoutRtl() {
        return TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == LAYOUT_DIRECTION_RTL;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int mode = MeasureSpec.getMode(widthMeasureSpec);
        int size = MeasureSpec.getSize(widthMeasureSpec);
        if (maxWidth > 0 && size > maxWidth) {
            size = maxWidth;
        }
        int constrainedWidth = MeasureSpec.makeMeasureSpec(size, mode);
        pickerMinute.clearNumberPickerPadding();
        pickerHour.clearNumberPickerPadding();
        pickerDate.clearNumberPickerPadding();
        pickerAmPm.clearNumberPickerPadding();
        float scale = size / (float) (pickerMinute.getLayoutParams().width
                + pickerHour.getLayoutParams().width
                + pickerDate.getLayoutParams().width
                + (is24Hours() ? 0 : pickerAmPm.getLayoutParams().width));
        measureChildConstrained(pickerMinute, widthMeasureSpec, heightMeasureSpec, scale);
        measureChildConstrained(pickerHour, widthMeasureSpec, heightMeasureSpec, scale);
        measureChildConstrained(pickerDate, widthMeasureSpec, heightMeasureSpec, scale);
        measureChildConstrained(pickerAmPm, widthMeasureSpec, heightMeasureSpec, scale);
        int sidePadding = (size - pickerMinute.getMeasuredWidth() - pickerHour.getMeasuredWidth()
                - pickerDate.getMeasuredWidth() - (is24Hours() ? 0 : pickerAmPm.getMeasuredWidth())) / 2;
        if (leftPickerPosition >= 0 && pickerLayout.getChildAt(leftPickerPosition) instanceof COUINumberPicker) {
            ((COUINumberPicker) pickerLayout.getChildAt(leftPickerPosition)).setNumberPickerPaddingLeft(sidePadding);
        }
        if (rightPickerPosition >= 0 && pickerLayout.getChildAt(rightPickerPosition) instanceof COUINumberPicker) {
            ((COUINumberPicker) pickerLayout.getChildAt(rightPickerPosition)).setNumberPickerPaddingRight(sidePadding);
        }
        super.onMeasure(constrainedWidth, heightMeasureSpec);
    }

    public void refresh() {
        pickerDate.refresh();
        pickerHour.refresh();
        pickerMinute.refresh();
        pickerAmPm.refresh();
    }

    public void scrollForceFinished() {
        pickerDate.scrollForceFinished();
        pickerHour.scrollForceFinished();
        pickerMinute.scrollForceFinished();
        if (!is24Hours()) {
            pickerAmPm.scrollForceFinished();
        }
    }

    public void setMinuteStepToFive() {
        minuteFiveStep = true;
        getTimePicker();
    }

    public void setNormalTextColor(int color) {
        pickerDate.setNormalTextColor(color);
        pickerHour.setNormalTextColor(color);
        pickerMinute.setNormalTextColor(color);
        pickerAmPm.setNormalTextColor(color);
    }

    public void setOnTimeChangeListener(OnTimeChangeListener listener) {
        onTimeChangeListener = listener;
    }

    @Deprecated
    public void setTimePicker(int ignored, Calendar calendar) {
        defaultCalendar = calendar;
        getTimePicker();
    }

    public void setTimePicker(Calendar calendar) {
        defaultCalendar = calendar;
        getTimePicker();
    }

    public void setUnitVisible(boolean visible) {
        pickerHour.setUnitText(visible ? getContext().getString(R.string.coui_hour_abbreviation) : "");
        pickerMinute.setUnitText(visible ? getContext().getString(R.string.coui_minute_abbreviation) : "");
    }

    public void setVibrateIntensity(float intensity) {
        pickerDate.setVibrateIntensity(intensity);
        pickerHour.setVibrateIntensity(intensity);
        pickerMinute.setVibrateIntensity(intensity);
        pickerAmPm.setVibrateIntensity(intensity);
    }

    public void setVibrateLevel(int level) {
        pickerDate.setVibrateLevel(level);
        pickerHour.setVibrateLevel(level);
        pickerMinute.setVibrateLevel(level);
        pickerAmPm.setVibrateLevel(level);
    }

    private void bindListeners() {
        pickerAmPm.setOnValueChangedListener((picker, oldVal, newVal) -> {
            amPm = picker.getValue();
            calendar.set(Calendar.AM_PM, amPm);
            notifyTimeChange();
        });
        pickerHour.setOnValueChangedListener((picker, oldVal, newVal) -> {
            if (is24Hours()) {
                calendar.set(Calendar.HOUR_OF_DAY, picker.getValue());
            } else {
                int hour = picker.getValue();
                calendar.set(Calendar.HOUR, hour == 12 ? 0 : hour);
                calendar.set(Calendar.AM_PM, amPm < 0 ? Calendar.AM : amPm);
            }
            notifyTimeChange();
        });
        pickerMinute.setOnValueChangedListener((picker, oldVal, newVal) -> {
            calendar.set(Calendar.MINUTE, minuteFiveStep ? picker.getValue() * 5 : picker.getValue());
            notifyTimeChange();
        });
        pickerDate.setOnValueChangedListener((picker, oldVal, newVal) -> {
            Date date = getDateFromValue(picker.getValue());
            if (date != null) {
                Calendar dateCalendar = Calendar.getInstance(calendar.getTimeZone());
                dateCalendar.setTime(date);
                calendar.set(Calendar.YEAR, dateCalendar.get(Calendar.YEAR));
                calendar.set(Calendar.MONTH, dateCalendar.get(Calendar.MONTH));
                calendar.set(Calendar.DAY_OF_MONTH, dateCalendar.get(Calendar.DAY_OF_MONTH));
                notifyTimeChange();
            }
        });
        COUINumberPicker.OnScrollingStopListener stopListener = () -> announceForAccessibility(reorderUtterance());
        pickerAmPm.setOnScrollingStopListener(stopListener);
        pickerHour.setOnScrollingStopListener(stopListener);
        pickerMinute.setOnScrollingStopListener(stopListener);
        pickerDate.setOnScrollingStopListener(stopListener);
    }

    private void notifyTimeChange() {
        if (onTimeChangeListener != null) {
            onTimeChangeListener.onTimeChange(this, calendar);
        }
    }

    private Date getDateFromValue(int value) {
        try {
            return outFormatter.parse(dateNamesTemp[value - 1]);
        } catch (ParseException | RuntimeException e) {
            return null;
        }
    }

    private String getDateYMDW(int value) {
        endDate.setTime(startTime + ((long) value * MILLISECOND_A_DAY));
        Calendar temp = Calendar.getInstance(calendar.getTimeZone());
        temp.setTime(endDate);
        todayIndex = isToday(temp.get(Calendar.YEAR), temp.get(Calendar.MONTH), temp.get(Calendar.DAY_OF_MONTH)) ? value : -1;
        return outFormatter.format(endDate);
    }

    private boolean is24Hours() {
        return DateFormat.is24HourFormat(context);
    }

    private boolean isToday(int year, int month, int date) {
        return year == todayYear && month == todayMonth && date == todayDate;
    }

    private int getDaysAmountOfYear(int year) {
        return isLeapYear(year) ? 366 : 365;
    }

    private boolean isLeapYear(int year) {
        return (year % 4 == 0 && year % 100 != 0) || year % 400 == 0;
    }

    private void measureChildConstrained(View view, int parentWidthSpec, int parentHeightSpec, float scale) {
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        if (scale < 1.0f) {
            lp.width = (int) (lp.width * scale);
        }
        view.measure(ViewGroup.getChildMeasureSpec(parentWidthSpec,
                        getPaddingLeft() + getPaddingRight() + lp.leftMargin + lp.rightMargin, lp.width),
                ViewGroup.getChildMeasureSpec(parentHeightSpec,
                        getPaddingTop() + getPaddingBottom() + lp.topMargin + lp.bottomMargin, lp.height));
    }

    private void reorderSpinners() {
        String pattern = deduplicate(DateFormat.getBestDateTimePattern(Locale.getDefault(), "yyyyMMMddhm"));
        ViewGroup parent = (ViewGroup) pickerDate.getParent();
        parent.removeAllViews();
        leftPickerPosition = -1;
        rightPickerPosition = -1;
        boolean inQuote = false;
        boolean addedDate = false;
        for (int i = 0; i < pattern.length(); i++) {
            char field = pattern.charAt(i);
            if (field == '\'') {
                int next = i + 1;
                if (next < pattern.length() && pattern.charAt(next) == '\'') {
                    i = next;
                } else {
                    inQuote = !inQuote;
                }
                continue;
            }
            if (!inQuote) {
                if (field == 'm') {
                    parent.addView(pickerMinute);
                } else if (field == 'a') {
                    parent.addView(pickerAmPm);
                } else if (field == 'd' || field == 'M' || field == 'y') {
                    if (!addedDate) {
                        parent.addView(pickerDate);
                        addedDate = true;
                    }
                } else if (field == 'h' || field == 'K') {
                    parent.addView(pickerHour);
                }
            }
            updatePickerEdgePositions(parent);
        }
        if (isLayoutRtl()) {
            int left = leftPickerPosition;
            leftPickerPosition = rightPickerPosition;
            rightPickerPosition = left;
        }
    }

    private String reorderUtterance() {
        String pattern = deduplicate(DateFormat.getBestDateTimePattern(Locale.getDefault(), "yyyyMMMddhm"));
        String utterance = "";
        boolean addedDate = false;
        for (int i = 0; i < pattern.length(); i++) {
            char field = pattern.charAt(i);
            if (field == 'm') {
                utterance = utterance + pickerMinute.getCurrentText()
                        + context.getString(R.string.coui_minute_abbreviation);
            } else if (field == 'a') {
                if (!is24Hours()) {
                    utterance = utterance + pickerAmPm.getCurrentText();
                }
            } else if (field == 'd' || field == 'M' || field == 'y') {
                if (!addedDate) {
                    utterance = utterance + new DateFormatter().format(pickerDate.getValue()) + ",";
                    addedDate = true;
                }
            } else if (field == 'h' || field == 'K') {
                utterance = utterance + pickerHour.getCurrentText()
                        + context.getString(R.string.coui_hour_abbreviation);
            }
        }
        return utterance;
    }

    private void updatePickerEdgePositions(ViewGroup parent) {
        if (!is24Hours()) {
            if (leftPickerPosition == -1) {
                leftPickerPosition = parent.getChildCount() - 1;
            }
            rightPickerPosition = parent.getChildCount() - 1;
            return;
        }
        if (parent.getChildAt(parent.getChildCount() - 1) != pickerAmPm) {
            if (leftPickerPosition == -1) {
                leftPickerPosition = parent.getChildCount() - 1;
            }
            rightPickerPosition = parent.getChildCount() - 1;
        }
    }

    private String deduplicate(String pattern) {
        if (TextUtils.isEmpty(pattern)) {
            return "";
        }
        String result = String.valueOf(pattern.charAt(0));
        for (int i = 1; i < pattern.length(); i++) {
            char current = pattern.charAt(i);
            if (current != pattern.charAt(i - 1)) {
                result += current;
            }
        }
        return result;
    }

    private class DateFormatter implements COUINumberPicker.Formatter {
        @Override
        public String format(int value) {
            int index = value - 1;
            dateNamesTemp[index] = getDateYMDW(value);
            if (value == todayIndex) {
                textDates[index] = textToday;
                return textToday;
            }
            if (!Locale.getDefault().getLanguage().equals("zh")) {
                return DateUtils.formatDateTime(getContext(), endDate.getTime(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY);
            }
            return new SimpleDateFormat("MMMdd" + textDay + " E", Locale.getDefault()).format(endDate);
        }
    }
}
