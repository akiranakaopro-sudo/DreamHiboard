package com.coui.appcompat.button;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.View;
import com.coui.appcompat.R;
import com.coui.appcompat.button.listener.OnSizeChangeListener;
import com.coui.appcompat.button.listener.OnTextChangeListener;
import com.coui.appcompat.grid.COUIResponsiveUtils;
import com.coui.appcompat.state.COUIViewStateController;
import com.coui.appcompat.state.PaddingProcessor;
import com.coui.appcompat.state.Processor;
import com.coui.appcompat.state.SizeProcessor;
import com.coui.appcompat.state.TextSizeProcessor;
import com.coui.appcompat.textutil.COUIChangeTextUtil;
import com.coui.appcompat.uiutil.UIUtil;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;


public class SingleButtonWrap extends COUIViewStateController implements OnTextChangeListener, OnSizeChangeListener {
    public static final int BUTTON_MAX_LINE = 2;
    public static final int MULTI_LINE = 2;
    public static final int SINGLE_LINE = 1;
    private COUIButton mCOUIButton;
    private int mSmallPaddingStart;
    private int mType;
    private int mLargeButtonCurrentLines = 0;
    private Runnable setCOUIButtonRequestLayoutRunnable = new Runnable() {
        @Override
        public void run() {
            if (SingleButtonWrap.this.mCOUIButton != null) {
                SingleButtonWrap.this.mCOUIButton.requestLayout();
            }
        }
    };

    @Target({ElementType.TYPE, ElementType.PARAMETER, ElementType.FIELD})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {
        public static final int DescLarge = 7;
        public static final int DescMedium = 4;
        public static final int Large = 0;
        public static final int Medium = 1;
        public static final int PanelMedium = 3;
        public static final int SingleCentralLarge = 6;
        public static final int Small = 2;
        public static final int TextLarge = 5;
    }

    public SingleButtonWrap(COUIButton button, int type) {
        this.mType = 0;
        this.mSmallPaddingStart = 0;
        if (button == null) {
            throw new IllegalArgumentException(getClass().getSimpleName() + ": parameter is null!");
        }
        this.mCOUIButton = button;
        button.setDrawableRadius(-1);
        this.mCOUIButton.setIncludeFontPadding(false);
        this.mType = type;
        this.mCOUIButton.setOnSizeChangeListener(this);
        this.mCOUIButton.setOnTextChangeListener(this);
        this.mCOUIButton.setSingleLine(false);
        this.mCOUIButton.setMaxLines(BUTTON_MAX_LINE);
        this.mSmallPaddingStart = this.mCOUIButton.getContext().getResources().getDimensionPixelSize(R.dimen.coui_small_single_btn_padding_horizontal);
        initProcessor();
        setSmallButtonStateChange();
        LimitLargeButtonMaxWidth();
    }

    private void LimitLargeButtonMaxWidth() {
        if (needLimitLargeButtonMaxWidth()) {
            this.mCOUIButton.setNeedLimitMaxWidth(true);
        } else {
            this.mCOUIButton.setNeedLimitMaxWidth(false);
        }
    }

    private static int dp2px(Context context, float dp) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics()));
    }

    private static float getAdapterSize(Context context, int textSize, int scaleLevel) {
        return COUIChangeTextUtil.getSuitableFontSize(textSize, context.getResources().getConfiguration().fontScale, scaleLevel);
    }

    private int getButtonMaxHeight(COUIButton button) {
        if (button == null) {
            return 0;
        }
        return (((button.getLineHeight() * BUTTON_MAX_LINE) + button.getPaddingTop()) + button.getPaddingBottom()) - ((int) (button.getLineHeight() - (button.getLineHeight() / button.getLineSpacingMultiplier())));
    }

    private List<Processor> getCentralLargeProcessor(Context context) {
        ArrayList arrayList = new ArrayList();
        int largeButtonWidth = getLargeButtonWidth(context);
        arrayList.add(new SizeProcessor.Builder(1).setHeight(-2).setWidth(largeButtonWidth).create());
        arrayList.add(new SizeProcessor.Builder(2).setHeight(-2).setWidth(largeButtonWidth).create());
        return arrayList;
    }

    private List<Processor> getDescMediumProcessor(Context context) {
        ArrayList arrayList = new ArrayList();
        int dimensionPixelSize = this.mType == 4 ? context.getResources().getDimensionPixelSize(R.dimen.coui_medium_btn_width) : context.getResources().getDimensionPixelSize(R.dimen.coui_larger_btn_width);
        if (COUIResponsiveUtils.isSmallScreen(context, context.getResources().getDisplayMetrics().widthPixels)) {
            dimensionPixelSize = this.mType == Type.DescLarge ? getLargeButtonWidth(context) : -1;
        }
        arrayList.add(new SizeProcessor.Builder(1).setHeight(-2).setWidth(dimensionPixelSize).create());
        PaddingProcessor.Builder builder = new PaddingProcessor.Builder(1);
        Resources resources = context.getResources();
        int descPaddingVertical = R.dimen.coui_btn_desc_padding_vertical;
        PaddingProcessor.Builder paddingTop = builder.setPaddingBottom(resources.getDimensionPixelSize(descPaddingVertical)).setPaddingTop(context.getResources().getDimensionPixelSize(descPaddingVertical));
        Resources resources2 = context.getResources();
        int buttonPaddingHorizontal = R.dimen.coui_btn_padding_horizontal;
        arrayList.add(paddingTop.setPaddingStart(resources2.getDimensionPixelSize(buttonPaddingHorizontal)).setPaddingEnd(context.getResources().getDimensionPixelSize(buttonPaddingHorizontal)).create());
        TextSizeProcessor.Builder builder2 = new TextSizeProcessor.Builder(1);
        Resources resources3 = context.getResources();
        int groupTextSize = R.dimen.coui_btn_group_text_size;
        arrayList.add(builder2.setTextSize(getAdapterSize(context, resources3.getDimensionPixelSize(groupTextSize), 4)).setSizeType(2.0f).create());
        arrayList.add(new SizeProcessor.Builder(2).setHeight(-2).setWidth(dimensionPixelSize).create());
        arrayList.add(new PaddingProcessor.Builder(2).setPaddingBottom(context.getResources().getDimensionPixelSize(descPaddingVertical)).setPaddingTop(context.getResources().getDimensionPixelSize(descPaddingVertical)).setPaddingStart(context.getResources().getDimensionPixelSize(buttonPaddingHorizontal)).setPaddingEnd(context.getResources().getDimensionPixelSize(buttonPaddingHorizontal)).create());
        arrayList.add(new TextSizeProcessor.Builder(2).setTextSize(getAdapterSize(context, context.getResources().getDimensionPixelSize(groupTextSize), 4)).setSizeType(2.0f).create());
        return arrayList;
    }

    private int getLargeButtonWidth(Context context) {
        int dimensionPixelSize = context.getResources().getDimensionPixelSize(R.dimen.coui_larger_btn_width);
        if (!COUIResponsiveUtils.isSmallScreen(context, context.getResources().getDisplayMetrics().widthPixels) || this.mCOUIButton == null) {
            return dimensionPixelSize;
        }
        int dimensionPixelSize2 = context.getResources().getDimensionPixelSize(R.dimen.coui_single_larger_btn_width);
        if (UIUtil.getScreenWidthMetrics(context) >= context.getResources().getDimensionPixelSize(R.dimen.coui_single_larger_window_screen)) {
            return dimensionPixelSize2;
        }
        return -1;
    }

    private List<Processor> getLargeProcessor(Context context) {
        ArrayList arrayList = new ArrayList();
        int largeButtonWidth = getLargeButtonWidth(context);
        arrayList.add(new SizeProcessor.Builder(1).setHeight(-2).setWidth(largeButtonWidth).create());
        arrayList.add(new SizeProcessor.Builder(2).setHeight(-2).setWidth(largeButtonWidth).create());
        PaddingProcessor.Builder paddingTop = new PaddingProcessor.Builder(1).setPaddingBottom(dp2px(context, 12.0f)).setPaddingTop(dp2px(context, 12.0f));
        Resources resources = context.getResources();
        int buttonPaddingHorizontal = R.dimen.coui_btn_padding_horizontal;
        arrayList.add(paddingTop.setPaddingStart(resources.getDimensionPixelSize(buttonPaddingHorizontal)).setPaddingEnd(context.getResources().getDimensionPixelSize(buttonPaddingHorizontal)).create());
        arrayList.add(new PaddingProcessor.Builder(2).setPaddingBottom(dp2px(context, 6.0f)).setPaddingTop(dp2px(context, 6.0f)).setPaddingStart(context.getResources().getDimensionPixelSize(buttonPaddingHorizontal)).setPaddingEnd(context.getResources().getDimensionPixelSize(buttonPaddingHorizontal)).create());
        TextSizeProcessor.Builder builder = new TextSizeProcessor.Builder(1);
        Resources resources2 = context.getResources();
        int groupTextSize = R.dimen.coui_btn_group_text_size;
        arrayList.add(builder.setTextSize(getAdapterSize(context, resources2.getDimensionPixelSize(groupTextSize), 4)).setSizeType(2.0f).create());
        arrayList.add(new TextSizeProcessor.Builder(2).setTextSize(getAdapterSize(context, context.getResources().getDimensionPixelSize(groupTextSize), 4)).setSizeType(2.0f).create());
        return arrayList;
    }

    private List<Processor> getMediumProcessor(Context context) {
        ArrayList arrayList = new ArrayList();
        SizeProcessor.Builder height = new SizeProcessor.Builder(1).setHeight(-2);
        Resources resources = context.getResources();
        int mediumButtonWidth = R.dimen.coui_medium_btn_width;
        arrayList.add(height.setWidth(resources.getDimensionPixelSize(mediumButtonWidth)).create());
        PaddingProcessor.Builder paddingTop = new PaddingProcessor.Builder(1).setPaddingBottom(dp2px(context, 12.0f)).setPaddingTop(dp2px(context, 12.0f));
        Resources resources2 = context.getResources();
        int buttonPaddingHorizontal = R.dimen.coui_btn_padding_horizontal;
        arrayList.add(paddingTop.setPaddingStart(resources2.getDimensionPixelSize(buttonPaddingHorizontal)).setPaddingEnd(context.getResources().getDimensionPixelSize(buttonPaddingHorizontal)).create());
        TextSizeProcessor.Builder builder = new TextSizeProcessor.Builder(1);
        Resources resources3 = context.getResources();
        int groupTextSize = R.dimen.coui_btn_group_text_size;
        arrayList.add(builder.setTextSize(getAdapterSize(context, resources3.getDimensionPixelSize(groupTextSize), 4)).setSizeType(2.0f).create());
        arrayList.add(new SizeProcessor.Builder(2).setHeight(-2).setWidth(context.getResources().getDimensionPixelSize(mediumButtonWidth)).create());
        arrayList.add(new PaddingProcessor.Builder(2).setPaddingBottom(dp2px(context, 6.0f)).setPaddingTop(dp2px(context, 6.0f)).setPaddingStart(context.getResources().getDimensionPixelSize(buttonPaddingHorizontal)).setPaddingEnd(context.getResources().getDimensionPixelSize(buttonPaddingHorizontal)).create());
        arrayList.add(new TextSizeProcessor.Builder(2).setTextSize(getAdapterSize(context, context.getResources().getDimensionPixelSize(groupTextSize), 4)).setSizeType(2.0f).create());
        return arrayList;
    }

    private List<Processor> getPanelMediumProcessor(Context context) {
        ArrayList arrayList = new ArrayList();
        arrayList.add(new SizeProcessor.Builder(1).setHeight(context.getResources().getDimensionPixelSize(R.dimen.coui_medium_btn_height)).setWidth(0).setWeight(1.0f).create());
        PaddingProcessor.Builder paddingTop = new PaddingProcessor.Builder(1).setPaddingBottom(dp2px(context, 11.0f)).setPaddingTop(dp2px(context, 11.0f));
        Resources resources = context.getResources();
        int buttonPaddingHorizontal = R.dimen.coui_btn_padding_horizontal;
        arrayList.add(paddingTop.setPaddingStart(resources.getDimensionPixelSize(buttonPaddingHorizontal)).setPaddingEnd(context.getResources().getDimensionPixelSize(buttonPaddingHorizontal)).create());
        arrayList.add(new TextSizeProcessor.Builder(1).setTextSize(16.0f).setSizeType(1.0f).create());
        arrayList.add(new SizeProcessor.Builder(2).setHeight(-2).setWidth(0).setWeight(1.0f).create());
        arrayList.add(new PaddingProcessor.Builder(2).setPaddingBottom(dp2px(context, 6.0f)).setPaddingTop(dp2px(context, 6.0f)).setPaddingStart(context.getResources().getDimensionPixelSize(buttonPaddingHorizontal)).setPaddingEnd(context.getResources().getDimensionPixelSize(buttonPaddingHorizontal)).create());
        arrayList.add(new TextSizeProcessor.Builder(2).setTextSize(16.0f).setSizeType(1.0f).create());
        return arrayList;
    }

    private List<Processor> getSmallProcessor(Context context) {
        ArrayList arrayList = new ArrayList();
        arrayList.add(new SizeProcessor.Builder(1).setHeight(-2).setWidth(-2).create());
        PaddingProcessor.Builder builder = new PaddingProcessor.Builder(1);
        Resources resources = context.getResources();
        int smallButtonPaddingVertical = R.dimen.coui_small_btn_padding_victical;
        PaddingProcessor.Builder paddingTop = builder.setPaddingBottom(resources.getDimensionPixelSize(smallButtonPaddingVertical)).setPaddingTop(context.getResources().getDimensionPixelSize(smallButtonPaddingVertical));
        Resources resources2 = context.getResources();
        int smallSingleButtonPaddingHorizontal = R.dimen.coui_small_single_btn_padding_horizontal;
        arrayList.add(paddingTop.setPaddingStart(resources2.getDimensionPixelSize(smallSingleButtonPaddingHorizontal)).setPaddingEnd(context.getResources().getDimensionPixelSize(smallSingleButtonPaddingHorizontal)).create());
        TextSizeProcessor.Builder builder2 = new TextSizeProcessor.Builder(1);
        Resources resources3 = context.getResources();
        int smallSingleTextSize = R.dimen.coui_btn_group_small_single_text_size;
        arrayList.add(builder2.setTextSize(getAdapterSize(context, resources3.getDimensionPixelSize(smallSingleTextSize), 2)).setSizeType(2.0f).create());
        arrayList.add(new SizeProcessor.Builder(2).setHeight(-2).setWidth(-2).create());
        PaddingProcessor.Builder paddingTop2 = new PaddingProcessor.Builder(2).setPaddingBottom(context.getResources().getDimensionPixelSize(smallButtonPaddingVertical)).setPaddingTop(context.getResources().getDimensionPixelSize(smallButtonPaddingVertical));
        Resources resources4 = context.getResources();
        int smallButtonPaddingHorizontal = R.dimen.coui_small_btn_padding_horizontal;
        arrayList.add(paddingTop2.setPaddingStart(resources4.getDimensionPixelSize(smallButtonPaddingHorizontal)).setPaddingEnd(context.getResources().getDimensionPixelSize(smallButtonPaddingHorizontal)).create());
        arrayList.add(new TextSizeProcessor.Builder(2).setTextSize(getAdapterSize(context, context.getResources().getDimensionPixelSize(smallSingleTextSize), 2)).setSizeType(2.0f).create());
        return arrayList;
    }

    private void initProcessor() {
        COUIButton button = this.mCOUIButton;
        if (button == null) {
            return;
        }
        int type = this.mType;
        if (type == Type.Large || type == Type.TextLarge) {
            addViewStateProcessor(getLargeProcessor(button.getContext()));
        } else if (type == Type.SingleCentralLarge) {
            addViewStateProcessor(getCentralLargeProcessor(button.getContext()));
        } else if (type == Type.Medium) {
            addViewStateProcessor(getMediumProcessor(button.getContext()));
        } else if (type == Type.Small) {
            addViewStateProcessor(getSmallProcessor(button.getContext()));
        } else if (type == Type.DescMedium || type == Type.DescLarge) {
            addViewStateProcessor(getDescMediumProcessor(button.getContext()));
            onViewStateChanged(1);
        } else {
            addViewStateProcessor(getPanelMediumProcessor(button.getContext()));
        }
        COUIButton currentButton = this.mCOUIButton;
        currentButton.setText(currentButton.getText());
    }

    public void lambda$onConfigurationChanged$0() {
        super.release();
        initProcessor();
    }

    public void lambda$setSmallButtonLines$1() {
        COUIButton button = this.mCOUIButton;
        if (button == null) {
            return;
        }
        if (getButtonMaxHeight(button) <= this.mCOUIButton.getMeasureMaxHeight() || this.mCOUIButton.getMeasureMaxHeight() == 0 || !this.mCOUIButton.isLimitHeight()) {
            this.mCOUIButton.setSingleLine(false);
            this.mCOUIButton.setMaxLines(MULTI_LINE);
        } else {
            this.mCOUIButton.setSingleLine(false);
            this.mCOUIButton.setMaxLines(SINGLE_LINE);
        }
        this.mCOUIButton.requestLayout();
    }

    private boolean needLimitLargeButtonMaxWidth() {
        int type;
        COUIButton button = this.mCOUIButton;
        return button != null
                && (type = this.mType) != Type.Small
                && type != Type.DescMedium
                && COUIResponsiveUtils.isSmallScreen(button.getContext(), this.mCOUIButton.getContext().getResources().getDisplayMetrics().widthPixels);
    }

    private void reInProcessor() {
        if (needLimitLargeButtonMaxWidth()) {
            if (this.mCOUIButton.getMeasuredWidth() >= this.mCOUIButton.getContext().getResources().getDimensionPixelSize(R.dimen.coui_single_larger_btn_width)) {
                super.release();
                initProcessor();
                this.mCOUIButton.removeCallbacks(this.setCOUIButtonRequestLayoutRunnable);
                this.mCOUIButton.post(this.setCOUIButtonRequestLayoutRunnable);
                return;
            }
            if (this.mCOUIButton.getLineCount() != this.mLargeButtonCurrentLines) {
                onViewStateChanged(this.mCOUIButton.getLineCount());
                this.mLargeButtonCurrentLines = this.mCOUIButton.getLineCount();
            }
        }
    }

    private void setLargeButtonLines(COUIButton button, float textWidth) {
        int dimensionPixelSize = button.getContext().getResources().getDimensionPixelSize(R.dimen.coui_larger_btn_width);
        if (COUIResponsiveUtils.isSmallScreen(button.getContext(), button.getContext().getResources().getDisplayMetrics().widthPixels)) {
            dimensionPixelSize = Math.min(Math.max(dimensionPixelSize, button.getMeasuredWidth()), button.getContext().getResources().getDimensionPixelSize(R.dimen.coui_single_larger_btn_width));
        }
        if (textWidth > dimensionPixelSize - (button.getContext().getResources().getDimensionPixelSize(R.dimen.coui_btn_padding_horizontal) * 2)) {
            onViewStateChanged(MULTI_LINE);
            this.mLargeButtonCurrentLines = MULTI_LINE;
        } else {
            onViewStateChanged(SINGLE_LINE);
            this.mLargeButtonCurrentLines = SINGLE_LINE;
        }
    }

    private void setMediumButtonLines(COUIButton button, float textWidth) {
        if (textWidth > button.getContext().getResources().getDimensionPixelSize(R.dimen.coui_medium_btn_width) - (button.getContext().getResources().getDimensionPixelSize(R.dimen.coui_btn_padding_horizontal) * 2)) {
            onViewStateChanged(MULTI_LINE);
        } else {
            onViewStateChanged(SINGLE_LINE);
        }
    }

    private void setSmallButtonLines() {
        if (this.mType == 2) {
            this.mCOUIButton.post(new Runnable() {
                @Override
                public final void run() {
                    SingleButtonWrap.this.lambda$setSmallButtonLines$1();
                }
            });
        }
    }

    private void setSmallButtonStateChange() {
        if (this.mType == 2) {
            if (this.mCOUIButton.getPaint().measureText(this.mCOUIButton.getText() == null ? "" : this.mCOUIButton.getText().toString()) > this.mCOUIButton.getMeasuredWidth() - (this.mSmallPaddingStart * 2)) {
                onViewStateChanged(MULTI_LINE);
                COUIButton button = this.mCOUIButton;
                button.setDrawableRadius(dp2px(button.getContext(), 14.0f));
            } else {
                onViewStateChanged(SINGLE_LINE);
                this.mCOUIButton.setDrawableRadius(-1);
            }
            this.mCOUIButton.requestLayout();
        }
    }

    @Override
    public View getProcessView() {
        return this.mCOUIButton;
    }

    @Override
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        this.mCOUIButton.post(new Runnable() {
            @Override
            public final void run() {
                SingleButtonWrap.this.lambda$onConfigurationChanged$0();
            }
        });
        LimitLargeButtonMaxWidth();
    }

    @Override
    public void onSizeChanged(View view, int width, int height, int oldWidth, int oldHeight) {
        if (view != null) {
            if (width == oldWidth && height == oldHeight) {
                return;
            }
            setSmallButtonStateChange();
            setTextButtonPressBackGround(this.mCOUIButton);
            reInProcessor();
        }
    }

    @Override
    public void onTextChanged(View view, CharSequence text, int start, int before, int count) {
        if (view == null || !(view instanceof COUIButton)) {
            return;
        }
        COUIButton button = (COUIButton) view;
        float textWidth = button.getPaint().measureText(button.getText().toString());
        int type = this.mType;
        if (type == Type.Large || type == Type.TextLarge || type == Type.SingleCentralLarge) {
            setLargeButtonLines(button, textWidth);
        } else if (type == Type.Medium) {
            setMediumButtonLines(button, textWidth);
        } else if (type == Type.Small) {
            setSmallButtonLines();
        }
    }

    @Override
    public void release() {
        super.release();
        COUIButton button = this.mCOUIButton;
        if (button != null) {
            button.setOnTextChangeListener(null);
            this.mCOUIButton.setOnSizeChangeListener(null);
            this.mCOUIButton.removeCallbacks(this.setCOUIButtonRequestLayoutRunnable);
            this.mCOUIButton = null;
        }
    }

    public void setTextButtonPressBackGround(COUIButton button) {
        if (this.mType == Type.TextLarge) {
            button.setAnimType(0);
        }
    }
}
