package com.coui.appcompat.edittext;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.text.Editable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.coui.appcompat.R;
import com.coui.appcompat.contextutil.COUIContextUtil;
import com.coui.appcompat.darkmode.COUIDarkModeUtil;
import com.coui.appcompat.roundRect.COUIShapePath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class COUICodeInputView extends RelativeLayout {
    private static final int CELL_COUNT = 6;
    private static final int DEFAULT_SCREEN_WIDTH_DP = 360;

    private final List<CodeItemView> codeViews = new ArrayList<>();
    private List<String> codes = new ArrayList<>();
    private int mCellCount;
    private int mCellHeight;
    private int mCellMarginHorizontal;
    private int mCellWidth;
    private LinearLayout mCodeViewsContainer;
    private int mCodeViewsContainerMarginHorizontal;
    private EditText mEditText;
    private boolean mIsEnableSecurity;
    private int mMaxCellMarginHorizontal;
    private int mMinCellMarginHorizontal;
    private final UpdateItemWidthRunnable mUpdateItemWidthRunnable = new UpdateItemWidthRunnable();
    private OnInputListener onInputListener;

    public static class CodeItemView extends View {
        private final COUICodeInputHelper codeInputHelper;
        private final int mCircleRadius;
        private boolean mIsEnableSecurity;
        private boolean mIsSelected;
        private String mNumber = "";
        private final TextPaint mNumberTextPaint = new TextPaint();
        private final Paint mPaint = new Paint();
        private Path mPath = new Path();
        private final int mRadius;
        private final Paint mStrokePaint = new Paint();
        private final Paint mCirclePaint = new Paint();
        private final int strokeWidth;

        public CodeItemView(Context context) {
            super(context);
            int textSize = getResources().getDimensionPixelSize(
                    R.dimen.coui_code_input_cell_text_size);
            mRadius = COUIContextUtil.getAttrDimens(getContext(), R.attr.couiRoundCornerS);
            strokeWidth = getResources().getDimensionPixelSize(
                    R.dimen.coui_code_input_cell_stroke_width);
            mCircleRadius = getResources().getDimensionPixelSize(
                    R.dimen.coui_code_input_cell_security_circle_radius);
            int circleColor = COUIContextUtil.getColor(getContext(),
                    R.color.coui_code_input_security_circle_color);
            mNumberTextPaint.setTextSize(textSize);
            mNumberTextPaint.setAntiAlias(true);
            mNumberTextPaint.setColor(COUIContextUtil.getAttrColor(getContext(),
                    R.attr.couiColorPrimaryNeutral));
            mPaint.setColor(COUIContextUtil.getAttrColor(getContext(),
                    R.attr.couiColorCardBackground));
            mStrokePaint.setColor(COUIContextUtil.getAttrColor(getContext(),
                    R.attr.couiColorPrimary));
            mStrokePaint.setStyle(Paint.Style.STROKE);
            mStrokePaint.setStrokeWidth(strokeWidth);
            mCirclePaint.setColor(circleColor);
            mCirclePaint.setAntiAlias(true);
            codeInputHelper = new COUICodeInputHelper(this);
        }

        private float getDrawTextStartX(int width, String text) {
            return (width / 2.0f) - (mNumberTextPaint.measureText(text) / 2.0f);
        }

        private float getDrawTextStartY(int height) {
            Paint.FontMetricsInt metrics = mNumberTextPaint.getFontMetricsInt();
            return (height / 2.0f) - ((metrics.descent + metrics.ascent) / 2.0f);
        }

        @Override
        public void onDraw(Canvas canvas) {
            int width = getWidth();
            int height = getHeight();
            mPath = COUIShapePath.getRoundRectPath(mPath,
                    new RectF(0.0f, 0.0f, width, height), mRadius);
            canvas.drawPath(mPath, mPaint);
            if (mIsSelected || codeInputHelper.isInboxAnimatorRuning()) {
                float inset = strokeWidth / 2.0f;
                RectF rect = new RectF(inset, inset, width - inset, height - inset);
                mStrokePaint.setAlpha((int) (codeInputHelper.getCurrentInboxAlpha() * 255.0f));
                mPath = COUIShapePath.getRoundRectPath(mPath, rect, mRadius);
                canvas.drawPath(mPath, mStrokePaint);
            }
            if (!TextUtils.isEmpty(mNumber) || codeInputHelper.isNumberAnimatorRuning()) {
                if (mIsEnableSecurity) {
                    canvas.drawCircle(width / 2.0f, height / 2.0f, mCircleRadius, mCirclePaint);
                    return;
                }
                if (!codeInputHelper.isNumberAnimatorRuning()) {
                    float x = getDrawTextStartX(width, mNumber);
                    float y = getDrawTextStartY(height);
                    mNumberTextPaint.setAlpha(255);
                    canvas.drawText(mNumber, x, y, mNumberTextPaint);
                    return;
                }
                float alpha = codeInputHelper.getCurrentNumberAlpha();
                String animatorNumber = mNumber;
                mNumberTextPaint.setAlpha((int) (alpha * 255.0f));
                float x;
                float y;
                if (codeInputHelper.isCurrentNumberAppear()) {
                    x = getDrawTextStartX(width, animatorNumber);
                    y = getDrawTextStartY(height);
                    float scale = codeInputHelper.getCurrentNumberScale();
                    canvas.scale(scale, scale, x, y);
                } else {
                    animatorNumber = codeInputHelper.getAnimatorNumber();
                    x = getDrawTextStartX(width, animatorNumber);
                    y = getDrawTextStartY(height);
                }
                canvas.drawText(animatorNumber, x, y, mNumberTextPaint);
            }
        }

        public void setEnableSecurity(boolean enableSecurity) {
            mIsEnableSecurity = enableSecurity;
        }

        public void setIsSelected(boolean selected) {
            if (selected != mIsSelected) {
                codeInputHelper.startInboxAnimator(selected);
            }
            mIsSelected = selected;
        }

        public void setNumber(String number) {
            if (!mIsEnableSecurity) {
                if (!TextUtils.isEmpty(mNumber) && TextUtils.isEmpty(number)) {
                    codeInputHelper.startNumberAnimator(false, mNumber);
                } else if (TextUtils.isEmpty(mNumber) && !TextUtils.isEmpty(number)) {
                    codeInputHelper.startNumberAnimator(true, number);
                }
            }
            mNumber = number;
        }
    }

    public interface OnInputListener {
        void onInput();

        void onSuccess(String code);
    }

    public static class UpdateItemWidthRunnable implements Runnable {
        private View codeViewsContainer;

        @Override
        public void run() {
            if (codeViewsContainer != null) {
                codeViewsContainer.requestLayout();
                codeViewsContainer = null;
            }
        }

        public void setCodeViewsContainer(View view) {
            codeViewsContainer = view;
        }
    }

    public COUICodeInputView(Context context) {
        this(context, null);
    }

    public COUICodeInputView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public COUICodeInputView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        COUIDarkModeUtil.setForceDarkAllow(this, false);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.COUICodeInputView,
                defStyleAttr, 0);
        mCellCount = a.getInteger(R.styleable.COUICodeInputView_couiCodeInputCount, CELL_COUNT);
        mIsEnableSecurity = a.getBoolean(
                R.styleable.COUICodeInputView_couiEnableSecurityInput, false);
        a.recycle();
        View view = LayoutInflater.from(context).inflate(R.layout.coui_phone_code_layout, this);
        initResource();
        initView(view);
    }

    private void callBack() {
        if (onInputListener == null) {
            return;
        }
        if (codes.size() == mCellCount) {
            onInputListener.onSuccess(getPhoneCode());
        } else {
            onInputListener.onInput();
        }
    }

    private int getCellMarginHorizontal(int width, int cellWidth) {
        int margin = Math.min(Math.max(Math.round(((width - (cellWidth * codeViews.size()))
                - (mCodeViewsContainerMarginHorizontal * 2.0f))
                / ((codeViews.size() * 2.0f) - 2.0f)), mMinCellMarginHorizontal),
                mMaxCellMarginHorizontal);
        mCellMarginHorizontal = margin;
        return margin;
    }

    private void initResource() {
        mCellWidth = getResources().getDimensionPixelSize(R.dimen.coui_code_input_cell_width);
        mCellMarginHorizontal = getResources().getDimensionPixelSize(
                R.dimen.coui_code_input_cell_margin_horizontal);
        mCellHeight = getResources().getDimensionPixelSize(R.dimen.coui_code_input_cell_height);
        mMaxCellMarginHorizontal = getResources().getDimensionPixelSize(
                R.dimen.coui_code_input_cell_max_margin_horizontal);
        mMinCellMarginHorizontal = getResources().getDimensionPixelSize(
                R.dimen.coui_code_input_cell_min_margin_horizontal);
        mCodeViewsContainerMarginHorizontal = getResources().getDimensionPixelSize(
                R.dimen.coui_code_input_layout_margin_start);
    }

    private void initView(View view) {
        mCodeViewsContainer = view.findViewById(R.id.code_container_layout);
        for (int i = 0; i < mCellCount; i++) {
            CodeItemView codeItemView = new CodeItemView(getContext());
            codeItemView.setEnableSecurity(mIsEnableSecurity);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mCellWidth, -1);
            params.setMarginStart(mCellMarginHorizontal);
            params.setMarginEnd(mCellMarginHorizontal);
            mCodeViewsContainer.addView(codeItemView, params);
            codeViews.add(codeItemView);
        }
        codeViews.get(0).setIsSelected(true);
        mEditText = view.findViewById(R.id.code_container_edittext);
        mEditText.requestFocus();
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                if (editable == null || editable.length() <= 0) {
                    return;
                }
                mEditText.setText("");
                if (codes.size() < mCellCount) {
                    String input = editable.toString().trim();
                    if (input.length() > 1) {
                        if (input.length() > mCellCount) {
                            input = input.substring(0, mCellCount);
                        }
                        codes = new ArrayList<>(Arrays.asList(input.split("")));
                    } else {
                        codes.add(input);
                    }
                }
                updateViewsByCodesChange();
                callBack();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
        mEditText.setOnKeyListener((view_2, keyCode, event) -> {
            if (!isNotEmpty(codes) || keyCode != KeyEvent.KEYCODE_DEL
                    || event.getAction() != KeyEvent.ACTION_DOWN || codes.size() <= 0) {
                return false;
            }
            codes.remove(codes.size() - 1);
            updateViewsByCodesChange();
            callBack();
            return true;
        });
        mEditText.setOnFocusChangeListener((view_3, hasFocus) -> {
            CodeItemView itemView = codeViews.get(Math.min(codes.size(), mCellCount - 1));
            itemView.setIsSelected(hasFocus);
            itemView.invalidate();
        });
    }

    private boolean isNotEmpty(List<String> list) {
        return !list.isEmpty();
    }

    private void setCodeItemWidth(int width) {
        double scale = Math.min(getResources().getConfiguration().screenWidthDp,
                DEFAULT_SCREEN_WIDTH_DP) / (double) DEFAULT_SCREEN_WIDTH_DP;
        int itemWidth = (int) (mCellWidth * scale);
        int itemHeight = (int) (mCellHeight * scale);
        mCellMarginHorizontal = getCellMarginHorizontal(width, itemWidth);
        for (int i = 0; i < mCodeViewsContainer.getChildCount(); i++) {
            View child = mCodeViewsContainer.getChildAt(i);
            if (child instanceof CodeItemView) {
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) child.getLayoutParams();
                params.width = itemWidth;
                params.height = itemHeight;
                params.setMarginStart(i == 0 ? 0 : mCellMarginHorizontal);
                params.setMarginEnd(i == mCellCount - 1 ? 0 : mCellMarginHorizontal);
                child.setLayoutParams(params);
            }
        }
        mUpdateItemWidthRunnable.setCodeViewsContainer(mCodeViewsContainer);
        post(mUpdateItemWidthRunnable);
    }

    private void updateViewsByCodesChange() {
        int size = codes.size();
        for (int i = 0; i < mCellCount; i++) {
            String number = size > i ? codes.get(i) : "";
            CodeItemView itemView = codeViews.get(i);
            itemView.setNumber(number);
            if (size == mCellCount && i == mCellCount - 1) {
                itemView.setIsSelected(true);
            } else {
                itemView.setIsSelected(size == i);
            }
            itemView.invalidate();
        }
    }

    public void clearAll() {
        mEditText.setText("");
        codes.clear();
        updateViewsByCodesChange();
    }

    public String getPhoneCode() {
        StringBuilder builder = new StringBuilder();
        Iterator<String> iterator = codes.iterator();
        while (iterator.hasNext()) {
            builder.append(iterator.next());
        }
        return builder.toString();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(mUpdateItemWidthRunnable);
    }

    @Override
    public void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        if (width != oldWidth) {
            setCodeItemWidth(width);
        }
    }

    public void setOnInputListener(OnInputListener listener) {
        onInputListener = listener;
    }
}
