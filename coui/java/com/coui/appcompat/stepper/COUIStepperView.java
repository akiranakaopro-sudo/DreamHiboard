package com.coui.appcompat.stepper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.coui.appcompat.R;
import com.coui.appcompat.contextutil.COUIContextUtil;
import com.coui.appcompat.hapticfeedback.COUIHapticFeedbackConstants;
import com.coui.appcompat.state.COUIMaskEffectDrawable;
import com.coui.appcompat.state.COUIStateEffectDrawable;
import com.coui.appcompat.state.COUIStrokeDrawable;

import java.util.Observable;
import java.util.Observer;

public class COUIStepperView extends ConstraintLayout implements IStepper, Observer {
    private final String TAG;
    private Context mContext;
    private OnStepChangeListener mListener;
    private ImageView mMinusImage;
    private LongPressProxy mMinusLongPressProxy;
    private final Runnable mMinusRunnable;
    private ImageView mPlusImage;
    private LongPressProxy mPlusLongPressProxy;
    private final Runnable mPlusRunnable;
    private ObservableStep mStep;
    private int mStyle;
    private int mUnit;
    private TextView mValueText;

    public COUIStepperView(Context context) {
        this(context, null);
    }

    public COUIStepperView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.couiStepperViewStyle);
    }

    public COUIStepperView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TAG = "COUIStepperView";
        mPlusRunnable = () -> {
            performHapticFeedback(COUIHapticFeedbackConstants.GRANULAR_SHORT_VIBRATE_SYNC, 0);
            plus();
        };
        mMinusRunnable = () -> {
            performHapticFeedback(COUIHapticFeedbackConstants.GRANULAR_SHORT_VIBRATE_SYNC, 0);
            minus();
        };
        mContext = context;
        init(attrs, defStyleAttr);
    }

    private void configEffectDrawableForButton() {
        configEffectDrawableForButton(mMinusImage, mMinusLongPressProxy);
        configEffectDrawableForButton(mPlusImage, mPlusLongPressProxy);
    }

    @SuppressLint({"ClickableViewAccessibility"})
    private void configEffectDrawableForButton(ImageView imageView, LongPressProxy longPressProxy) {
        float dimension = getContext().getResources().getDimension(R.dimen.stepper_button_size);
        ShapeDrawable shapeDrawable = new ShapeDrawable(new OvalShape());
        RectF rect = new RectF(0.0f, 0.0f, dimension, dimension);
        shapeDrawable.getPaint().setColor(COUIContextUtil.getAttrColor(getContext(), R.attr.couiColorPressBackground));
        int size = (int) dimension;
        shapeDrawable.setBounds(0, 0, size, size);
        COUIMaskEffectDrawable maskDrawable = new COUIMaskEffectDrawable(getContext(), 0);
        float radius = dimension / 2.0f;
        maskDrawable.setMaskRect(rect, radius, radius);
        COUIStrokeDrawable strokeDrawable = new COUIStrokeDrawable(getContext());
        strokeDrawable.setStrokeRect(rect, radius, radius);
        COUIStateEffectDrawable stateEffectDrawable = new COUIStateEffectDrawable(
                new Drawable[]{shapeDrawable, maskDrawable, strokeDrawable});
        stateEffectDrawable.enableScaleEffect(imageView, 2);
        imageView.setBackground(stateEffectDrawable);
        longPressProxy.setOnTouchListener((view, event) -> {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                stateEffectDrawable.setTouched(true);
            }
            if (event.getActionMasked() == MotionEvent.ACTION_UP
                    || event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
                stateEffectDrawable.setTouched(false);
            }
            return false;
        });
    }

    private int getNumForMaxWidth() {
        int result = 1;
        float maxWidth = 0.0f;
        for (int i = 0; i < 10; i++) {
            float width = mValueText.getPaint().measureText(String.valueOf(i));
            if (width > maxWidth) {
                result = i;
                maxWidth = width;
            }
        }
        return result;
    }

    private void initAttr(TypedArray typedArray) {
        try {
            int textStyle = typedArray.getResourceId(R.styleable.COUIStepperView_couiStepperTextStyle, 0);
            int plusImage = typedArray.getResourceId(R.styleable.COUIStepperView_couiStepperPlusImage, 0);
            int minusImage = typedArray.getResourceId(R.styleable.COUIStepperView_couiStepperMinusImage, 0);
            if (textStyle != 0) {
                mValueText.setTextAppearance(textStyle);
            }
            if (plusImage != 0) {
                mPlusImage.setImageDrawable(ContextCompat.getDrawable(getContext(), plusImage));
            }
            if (minusImage != 0) {
                mMinusImage.setImageDrawable(ContextCompat.getDrawable(getContext(), minusImage));
            }
            configEffectDrawableForButton();
        } catch (Resources.NotFoundException e) {
            Log.e("COUIStepperView", e.getMessage());
        }
    }

    public void init(AttributeSet attrs, int defStyleAttr) {
        int style = R.style.COUIStepperViewDefStyle;
        mStyle = style;
        LayoutInflater.from(getContext()).inflate(R.layout.coui_stepper_view, this);
        mPlusImage = findViewById(R.id.plus);
        mMinusImage = findViewById(R.id.minus);
        mValueText = findViewById(R.id.indicator);
        mPlusLongPressProxy = new LongPressProxy(mPlusImage, mPlusRunnable);
        mMinusLongPressProxy = new LongPressProxy(mMinusImage, mMinusRunnable);
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.COUIStepperView, defStyleAttr, style);
        int maximum = typedArray.getInt(R.styleable.COUIStepperView_couiMaximum, ObservableStep.MAX_VALUE);
        int minimum = typedArray.getInt(R.styleable.COUIStepperView_couiMinimum, ObservableStep.MIN_VALUE);
        int defStep = typedArray.getInt(R.styleable.COUIStepperView_couiDefStep, 0);
        mUnit = typedArray.getInt(R.styleable.COUIStepperView_couiUnit, 1);
        initAttr(typedArray);
        typedArray.recycle();
        ObservableStep observableStep = new ObservableStep();
        mStep = observableStep;
        observableStep.addObserver(this);
        setMaximum(maximum);
        setMinimum(minimum);
        setCurStep(defStep);
    }

    @Override
    public int getCurStep() {
        return mStep.getStep();
    }

    @Override
    public int getMaximum() {
        return mStep.getMaximum();
    }

    @Override
    public int getMinimum() {
        return mStep.getMinimum();
    }

    @Override
    public int getUnit() {
        return mUnit;
    }

    @Override
    public void minus() {
        ObservableStep observableStep = mStep;
        observableStep.setStep(observableStep.getStep() - getUnit());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int numForMaxWidth = getNumForMaxWidth();
        String[] split = String.valueOf(getMaximum()).split("");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < split.length; i++) {
            builder.append(numForMaxWidth);
        }
        mValueText.setWidth(Math.round(mValueText.getPaint().measureText(builder.toString())));
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void plus() {
        ObservableStep observableStep = mStep;
        observableStep.setStep(observableStep.getStep() + getUnit());
    }

    public void refresh() {
        TypedArray typedArray = mContext.obtainStyledAttributes(null, R.styleable.COUIStepperView, 0, mStyle);
        if (typedArray != null) {
            initAttr(typedArray);
            typedArray.recycle();
        }
    }

    public void release() {
        mPlusLongPressProxy.release();
        mMinusLongPressProxy.release();
        mStep.deleteObservers();
        mListener = null;
    }

    @Override
    public void setCurStep(int step) {
        mStep.setStep(step);
    }

    @Override
    public void setMaximum(int maximum) {
        mStep.setMaximum(maximum);
    }

    @Override
    public void setMinimum(int minimum) {
        mStep.setMinimum(minimum);
    }

    @Override
    public void setOnStepChangeListener(OnStepChangeListener listener) {
        mListener = listener;
    }

    @Override
    public void setUnit(int unit) {
        mUnit = unit;
    }

    @Override
    public void update(Observable observable, Object data) {
        int step = ((ObservableStep) observable).getStep();
        int oldStep = ((Integer) data).intValue();
        boolean minusEnabled = false;
        mPlusImage.setEnabled(step < getMaximum() && isEnabled());
        if (step > getMinimum() && isEnabled()) {
            minusEnabled = true;
        }
        mMinusImage.setEnabled(minusEnabled);
        mValueText.setText(String.valueOf(step));
        OnStepChangeListener listener = mListener;
        if (listener != null) {
            listener.onStepChanged(step, oldStep);
        }
    }
}
