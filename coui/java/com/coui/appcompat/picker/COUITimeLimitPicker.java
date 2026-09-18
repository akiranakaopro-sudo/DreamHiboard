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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.coui.appcompat.R;
import com.coui.appcompat.darkmode.COUIDarkModeUtil;

import java.util.Calendar;
import java.util.Locale;

public class COUITimeLimitPicker extends FrameLayout {
    private static final int MAX_MINUTE = 59;
    private static final int LONGPRESS_UPDATE_INTERVAL = 100;
    private static final String PAUSE_STRING = ", ";
    private static final OnTimeChangedListener NO_OP_CHANGE_LISTENER = (view, hour, minute) -> {
    };

    private final Button amPmButton;
    private final String[] amPmStrings;
    private final COUINumberPicker amPmSpinner;
    private final COUINumberPicker hourSpinner;
    private final COUINumberPicker minuteSpinner;
    private Context context;
    private Locale currentLocale;
    private Calendar tempCalendar;
    private TextView hourText;
    private TextView minuteText;
    private boolean is24HourView;
    private boolean isAm;
    private boolean isEnabled = true;
    private int maxWidth;
    private int backgroundLeft;
    private int backgroundRadius;
    private int backgroundDividerHeight;
    private int leftPickerPosition = -1;
    private int rightPickerPosition = -1;
    private LinearLayout pickerLayout;
    private OnTimeChangedListener onTimeChangedListener = NO_OP_CHANGE_LISTENER;

    public interface OnTimeChangedListener {
        void onTimeChanged(COUITimeLimitPicker view, int hourOfDay, int minute);
    }

    public COUITimeLimitPicker(Context context) {
        this(context, null);
    }

    public COUITimeLimitPicker(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.couiTimePickerStyle);
    }

    public COUITimeLimitPicker(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.TimePickerStyle);
    }

    public COUITimeLimitPicker(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
        COUIDarkModeUtil.setForceDarkAllow(this, false);
        setCurrentLocale(Locale.getDefault());
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.COUIPickersCommonAttrs, defStyleAttr, defStyleRes);
        maxWidth = a.getDimensionPixelSize(R.styleable.COUIPickersCommonAttrs_couiPickersMaxWidth, 0);
        a.recycle();
        backgroundDividerHeight = Math.max(getResources().getDimensionPixelOffset(R.dimen.coui_number_picker_background_divider_height), 1);
        LayoutInflater.from(context).inflate(R.layout.coui_time_limit_picker, this, true);
        minuteText = findViewById(R.id.coui_timepicker_minute_text);
        hourText = findViewById(R.id.coui_timepicker_hour_text);
        pickerLayout = findViewById(R.id.time_pickers);
        hourSpinner = findViewById(R.id.hour);
        minuteSpinner = findViewById(R.id.minute);
        View amPmView = findViewById(R.id.amPm);
        amPmStrings = getResources().getStringArray(R.array.coui_time_picker_ampm);
        if (amPmView instanceof Button) {
            amPmButton = (Button) amPmView;
            amPmSpinner = null;
            amPmButton.setOnClickListener(v -> {
                v.requestFocus();
                isAm = !isAm;
                updateAmPmControl();
                onTimeChanged();
            });
        } else {
            amPmButton = null;
            amPmSpinner = (COUINumberPicker) amPmView;
            amPmSpinner.setMinValue(0);
            amPmSpinner.setMaxValue(1);
            amPmSpinner.setDisplayedValues(amPmStrings);
            amPmSpinner.setOnValueChangedListener((picker, oldVal, newVal) -> {
                picker.requestFocus();
                isAm = picker.getValue() == 0;
                updateAmPmControl();
                onTimeChanged();
            });
            amPmSpinner.setOnScrollingStopListener(this::announceForAccessibility);
        }

        hourSpinner.setOnValueChangedListener((picker, oldVal, newVal) -> onTimeChanged());
        hourSpinner.setOnScrollingStopListener(this::announceForAccessibility);
        hourSpinner.setUnitText("");
        minuteSpinner.setTwoDigitFormatter();
        minuteSpinner.setMinValue(0);
        minuteSpinner.setMaxValue(MAX_MINUTE);
        minuteSpinner.setUnitText("");
        minuteSpinner.setOnLongPressUpdateInterval(LONGPRESS_UPDATE_INTERVAL);
        minuteSpinner.setOnValueChangedListener((picker, oldVal, newVal) -> onTimeChanged());
        minuteSpinner.setOnScrollingStopListener(this::announceForAccessibility);
        minuteText.setTextAlignment(TEXT_ALIGNMENT_TEXT_END);
        hourText.setTextAlignment(TEXT_ALIGNMENT_TEXT_END);
        updateHourControl();
        updateAmPmControl();
        setCurrentHour(tempCalendar.get(Calendar.HOUR_OF_DAY));
        setCurrentMinute(tempCalendar.get(Calendar.MINUTE));
        reorderSpinners();
        if (hourSpinner.isAccessibilityEnable()) {
            hourSpinner.addTalkbackSuffix(context.getString(R.string.coui_hour_abbreviation));
            minuteSpinner.addTalkbackSuffix(context.getString(R.string.coui_minute_abbreviation));
        }
        backgroundRadius = getResources().getDimensionPixelOffset(R.dimen.coui_selected_background_radius);
        backgroundLeft = getResources().getDimensionPixelOffset(R.dimen.coui_selected_background_horizontal_padding);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(hourSpinner.getBackgroundColor());
        canvas.drawRect(backgroundLeft, (getHeight() / 2f) - backgroundRadius,
                getWidth() - backgroundLeft, (getHeight() / 2f) - backgroundRadius + backgroundDividerHeight, paint);
        canvas.drawRect(backgroundLeft, (getHeight() / 2f) + backgroundRadius,
                getWidth() - backgroundLeft, (getHeight() / 2f) + backgroundRadius + backgroundDividerHeight, paint);
        super.dispatchDraw(canvas);
    }

    @Override
    public int getBaseline() {
        return hourSpinner.getBaseline();
    }

    public COUINumberPicker getAmPmSpinner() {
        return amPmSpinner;
    }

    public Integer getCurrentHour() {
        int value = hourSpinner.getValue();
        if (is24HourView()) {
            return value;
        }
        return isAm ? value % 12 : (value % 12) + 12;
    }

    public Integer getCurrentMinute() {
        return minuteSpinner.getValue();
    }

    public COUINumberPicker getHourSpinner() {
        return hourSpinner;
    }

    public COUINumberPicker getMinuteSpinner() {
        return minuteSpinner;
    }

    public boolean is24HourView() {
        return is24HourView;
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
        leftPickerPosition = -1;
        rightPickerPosition = -1;
        int remaining = size;
        for (int i = 0; i < pickerLayout.getChildCount(); i++) {
            View child = pickerLayout.getChildAt(i);
            if (child instanceof COUINumberPicker && child.getVisibility() == VISIBLE) {
                if (leftPickerPosition == -1) {
                    leftPickerPosition = i;
                }
                rightPickerPosition = i;
                ((COUINumberPicker) child).clearNumberPickerPadding();
                measureChildConstrained(child, widthMeasureSpec, heightMeasureSpec);
                remaining -= child.getMeasuredWidth();
            }
        }
        int sidePadding = Math.max(0, remaining / 2);
        if (isLayoutRtl()) {
            int oldLeft = leftPickerPosition;
            leftPickerPosition = rightPickerPosition;
            rightPickerPosition = oldLeft;
        }
        if (leftPickerPosition >= 0 && pickerLayout.getChildAt(leftPickerPosition) instanceof COUINumberPicker) {
            ((COUINumberPicker) pickerLayout.getChildAt(leftPickerPosition)).setNumberPickerPaddingLeft(sidePadding);
        }
        if (rightPickerPosition >= 0 && pickerLayout.getChildAt(rightPickerPosition) instanceof COUINumberPicker) {
            ((COUINumberPicker) pickerLayout.getChildAt(rightPickerPosition)).setNumberPickerPaddingRight(sidePadding);
        }
        super.onMeasure(constrainedWidth, heightMeasureSpec);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        setCurrentHour(savedState.getHour());
        setCurrentMinute(savedState.getMinute());
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        return new SavedState(super.onSaveInstanceState(), getCurrentHour(), getCurrentMinute());
    }

    public void refresh() {
        hourSpinner.refresh();
        minuteSpinner.refresh();
        if (amPmSpinner != null) {
            amPmSpinner.refresh();
        }
    }

    public void scrollForceFinished() {
        hourSpinner.scrollForceFinished();
        minuteSpinner.scrollForceFinished();
        if (amPmSpinner != null) {
            amPmSpinner.scrollForceFinished();
        }
    }

    public void setCurrentData(Integer hour, Integer minute) {
        if (minute.equals(getCurrentMinute()) && hour.equals(getCurrentHour())) {
            return;
        }
        setCurrentHour(hour);
        setCurrentMinute(minute);
        onTimeChanged();
    }

    public void setCurrentHour(Integer hour) {
        if (hour == null || hour.equals(getCurrentHour())) {
            return;
        }
        int spinnerHour = hour;
        if (!is24HourView()) {
            if (hour >= 12) {
                isAm = false;
                if (hour > 12) {
                    spinnerHour = hour - 12;
                }
            } else {
                isAm = true;
                if (hour == 0) {
                    spinnerHour = 12;
                }
            }
            updateAmPmControl();
        }
        hourSpinner.setValue(spinnerHour);
        onTimeChanged();
    }

    public void setCurrentMinute(Integer minute) {
        if (minute == null || minute.equals(getCurrentMinute())) {
            return;
        }
        minuteSpinner.setValue(minute);
        onTimeChanged();
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (isEnabled == enabled) {
            return;
        }
        super.setEnabled(enabled);
        minuteSpinner.setEnabled(enabled);
        hourSpinner.setEnabled(enabled);
        if (amPmSpinner != null) {
            amPmSpinner.setEnabled(enabled);
        } else if (amPmButton != null) {
            amPmButton.setEnabled(enabled);
        }
        isEnabled = enabled;
    }

    public void setIs24HourView(Boolean is24HourView) {
        if (this.is24HourView == is24HourView) {
            return;
        }
        int currentHour = getCurrentHour();
        this.is24HourView = is24HourView;
        updateHourControl();
        setCurrentHour(currentHour);
        updateAmPmControl();
        hourSpinner.requestLayout();
    }

    public void setNormalTextColor(int color) {
        hourSpinner.setNormalTextColor(color);
        minuteSpinner.setNormalTextColor(color);
        if (amPmSpinner != null) {
            amPmSpinner.setNormalTextColor(color);
        }
    }

    public void setOnTimeChangedListener(OnTimeChangedListener listener) {
        onTimeChangedListener = listener == null ? NO_OP_CHANGE_LISTENER : listener;
    }

    public void setRowNumber(int rowNumber) {
        if (rowNumber <= 0) {
            return;
        }
        hourSpinner.setPickerRowNumber(rowNumber);
        minuteSpinner.setPickerRowNumber(rowNumber);
        if (amPmSpinner != null) {
            amPmSpinner.setPickerRowNumber(rowNumber);
        }
    }

    public void setTextVisibility(boolean visible) {
        minuteText.setVisibility(visible ? VISIBLE : GONE);
        hourText.setVisibility(visible ? VISIBLE : GONE);
    }

    public void setUnitVisible(boolean visible) {
        hourSpinner.setUnitText(visible ? getContext().getString(R.string.coui_hour_abbreviation) : "");
        minuteSpinner.setUnitText(visible ? getContext().getString(R.string.coui_minute_abbreviation) : "");
    }

    public void setVibrateIntensity(float intensity) {
        hourSpinner.setVibrateIntensity(intensity);
        minuteSpinner.setVibrateIntensity(intensity);
        if (amPmSpinner != null) {
            amPmSpinner.setVibrateIntensity(intensity);
        }
    }

    public void setVibrateLevel(int level) {
        hourSpinner.setVibrateLevel(level);
        minuteSpinner.setVibrateLevel(level);
        if (amPmSpinner != null) {
            amPmSpinner.setVibrateLevel(level);
        }
    }

    private void announceForAccessibility() {
        String amPm = isAm ? amPmStrings[0] : amPmStrings[1];
        String text = is24HourView()
                ? hourSpinner.getCurrentText() + getContext().getString(R.string.coui_hour_abbreviation)
                + PAUSE_STRING + minuteSpinner.getCurrentText() + getContext().getString(R.string.coui_minute_abbreviation)
                : amPm + PAUSE_STRING + hourSpinner.getCurrentText() + getContext().getString(R.string.coui_hour_abbreviation)
                + PAUSE_STRING + minuteSpinner.getCurrentText() + getContext().getString(R.string.coui_minute_abbreviation);
        announceForAccessibility(text);
    }

    private void measureChildConstrained(View child, int parentWidthSpec, int parentHeightSpec) {
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) child.getLayoutParams();
        child.measure(ViewGroup.getChildMeasureSpec(parentWidthSpec,
                        getPaddingLeft() + getPaddingRight() + lp.leftMargin + lp.rightMargin, lp.width),
                ViewGroup.getChildMeasureSpec(parentHeightSpec,
                        getPaddingTop() + getPaddingBottom() + lp.topMargin + lp.bottomMargin, lp.height));
    }

    private void onTimeChanged() {
        onTimeChangedListener.onTimeChanged(this, getCurrentHour(), getCurrentMinute());
    }

    private void reorderSpinners() {
        if (DateFormat.getBestDateTimePattern(Locale.getDefault(), "hm").startsWith("a") || amPmSpinner == null) {
            return;
        }
        ViewGroup parent = (ViewGroup) amPmSpinner.getParent();
        parent.removeView(amPmSpinner);
        parent.addView(amPmSpinner);
    }

    private void setCurrentLocale(Locale locale) {
        if (locale.equals(currentLocale)) {
            return;
        }
        currentLocale = locale;
        tempCalendar = Calendar.getInstance(locale);
    }

    private void updateAmPmControl() {
        if (is24HourView()) {
            if (amPmSpinner != null) {
                amPmSpinner.setVisibility(GONE);
            } else if (amPmButton != null) {
                amPmButton.setVisibility(GONE);
            }
            return;
        }
        int index = isAm ? 0 : 1;
        if (amPmSpinner != null) {
            amPmSpinner.setValue(index);
            amPmSpinner.setVisibility(VISIBLE);
        } else if (amPmButton != null) {
            amPmButton.setText(amPmStrings[index]);
            amPmButton.setVisibility(VISIBLE);
        }
    }

    private void updateHourControl() {
        if (is24HourView()) {
            hourSpinner.setMinValue(0);
            hourSpinner.setMaxValue(23);
            hourSpinner.setTwoDigitFormatter();
        } else {
            hourSpinner.setMinValue(1);
            hourSpinner.setMaxValue(12);
        }
        hourSpinner.setWrapSelectorWheel(true);
        minuteSpinner.setWrapSelectorWheel(true);
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

        private final int hour;
        private final int minute;

        private SavedState(Parcelable superState, int hour, int minute) {
            super(superState);
            this.hour = hour;
            this.minute = minute;
        }

        private SavedState(Parcel source) {
            super(source);
            hour = source.readInt();
            minute = source.readInt();
        }

        public int getHour() {
            return hour;
        }

        public int getMinute() {
            return minute;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(hour);
            out.writeInt(minute);
        }
    }
}
