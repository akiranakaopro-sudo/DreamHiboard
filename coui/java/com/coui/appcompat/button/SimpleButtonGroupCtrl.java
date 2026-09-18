package com.coui.appcompat.button;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import com.coui.appcompat.button.COUIButtonLayout;
import com.coui.appcompat.R;
import com.coui.appcompat.button.listener.OnSizeChangeListener;
import com.coui.appcompat.button.listener.OnTextChangeListener;
import com.coui.appcompat.grid.COUIResponsiveUtils;
import com.coui.appcompat.log.COUILog;
import com.coui.appcompat.state.IViewStateController;
import com.coui.appcompat.state.Processor;
import com.coui.appcompat.state.SizeProcessor;
import com.coui.appcompat.textutil.COUIChangeTextUtil;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


public class SimpleButtonGroupCtrl implements IViewStateController, OnTextChangeListener, OnSizeChangeListener, View.OnLayoutChangeListener {
    public static final int MAX_SIZE = 2;
    public static final int MULTI_LINE = 2;
    public static final int SINGLE_LINE = 1;
    private static final String TAG = "SimpleButtonGroupCtrl";
    private COUIButton mCustomButton;
    private SingleButtonWrap mCustomButtonWarp;
    private LinearLayout mCustomLayout;
    private COUIButton mTextChangeBtn;
    private List<SingleButtonWrap> mSingleButtonWrapList = new LinkedList();
    private int mLineCountIndex = -1;
    private int mCurLineCount = SINGLE_LINE;
    private int mType = 1;
    private boolean mParentListenerRegistered = false;
    private int mCachedHorizontalButtonMaxWidth = 0;

    private void adjustButtonWidths() {
        Iterator<SingleButtonWrap> it = this.mSingleButtonWrapList.iterator();
        while (it.hasNext()) {
            View processView = it.next().getProcessView();
            if (processView != null) {
                ViewGroup.LayoutParams layoutParams = processView.getLayoutParams();
                if (layoutParams instanceof LinearLayout.LayoutParams) {
                    LinearLayout.LayoutParams layoutParams2 = (LinearLayout.LayoutParams) layoutParams;
                    if (layoutParams2.width != -1) {
                        layoutParams2.width = -1;
                        layoutParams2.weight = 0.0f;
                        processView.setLayoutParams(layoutParams2);
                    }
                }
            }
        }
    }

    private void ensureParentListenerRegistered(final COUIButtonLayout buttonLayout) {
        if (this.mParentListenerRegistered) {
            return;
        }
        this.mParentListenerRegistered = true;
        buttonLayout.setOnButtonLayoutVisibilityChangedListener(new COUIButtonLayout.OnButtonLayoutVisibilityChangedListener() {
            @Override
            public final void onButtonLayoutVisibilityChanged(int visibility) {
                SimpleButtonGroupCtrl.this.lambda$ensureParentListenerRegistered$0(buttonLayout, visibility);
            }
        });
    }

    private int getButtonLineCount(COUIButton button) {
        Context context = button.getContext();
        if (TextUtils.isEmpty(button.getText())) {
            return 1;
        }
        if (!(button.getParent() instanceof COUIButtonLayout)) {
            return button.getLineCount();
        }
        if (button.isDescType()) {
            return 1;
        }
        COUIButtonLayout buttonLayout = (COUIButtonLayout) button.getParent();
        int measureMaxWidth = button.getMeasureMaxWidth();
        float measuredTextWidth = button.getPaint().measureText(button.getText().toString());
        if (!COUIResponsiveUtils.isSmallScreen(context, context.getResources().getDisplayMetrics().widthPixels)) {
            measureMaxWidth = buttonLayout.getOrientation() == LinearLayout.HORIZONTAL ? context.getResources().getDimensionPixelSize(R.dimen.coui_medium_btn_width) : context.getResources().getDimensionPixelSize(R.dimen.coui_larger_btn_width);
        } else if (buttonLayout.getOrientation() != LinearLayout.HORIZONTAL || measureMaxWidth <= 0) {
            measureMaxWidth = this.mCachedHorizontalButtonMaxWidth;
            if (measureMaxWidth <= 0) {
                measureMaxWidth = context.getResources().getDimensionPixelSize(R.dimen.coui_medium_btn_width);
            }
        } else {
            this.mCachedHorizontalButtonMaxWidth = measureMaxWidth;
        }
        return (measuredTextWidth <= ((float) ((measureMaxWidth - button.getPaddingStart()) - button.getPaddingEnd())) || measureMaxWidth == 0) ? 1 : 2;
    }

    private int getButtonMaxHeight(COUIButton button, int lineCount) {
        if (button == null) {
            return 0;
        }
        if (button.isDescType()) {
            return (int) COUIChangeTextUtil.getDpG2Size(button.getContext().getResources().getDimensionPixelSize(R.dimen.coui_btn_desc_height_min), button.getContext().getResources().getConfiguration().fontScale);
        }
        return (((button.getLineHeight() * lineCount) + button.getPaddingTop()) + button.getPaddingBottom()) - ((int) (button.getLineHeight() - (button.getLineHeight() / button.getLineSpacingMultiplier())));
    }

    private View getFirstButton() {
        return this.mSingleButtonWrapList.get(0).getProcessView();
    }

    private void handleExtraLongLogic(int buttonIndex, List<SingleButtonWrap> buttonWrapList) {
        int buttonMaxHeight;
        int buttonWidth;
        SingleButtonWrap singleButtonWrap = buttonWrapList.get(buttonIndex);
        if (singleButtonWrap == null) {
            throw new IllegalArgumentException("ButtonGroupStateController: buttonWrap == null");
        }
        COUIButton button = (COUIButton) singleButtonWrap.getProcessView();
        if (button == null) {
            throw new IllegalArgumentException("ButtonGroupStateController: button == null");
        }
        if (button.getParent() instanceof COUIButtonLayout) {
            Context context = button.getContext();
            COUIButtonLayout buttonLayout = (COUIButtonLayout) button.getParent();
            int mediumButtonWidth = context.getResources().getDimensionPixelSize(R.dimen.coui_medium_btn_width);
            int verticalButtonMarginTop = context.getResources().getDimensionPixelSize(R.dimen.coui_vertical_btn_margin_top);
            int height = button.getHeight();
            boolean zIsSmallScreen = COUIResponsiveUtils.isSmallScreen(context, context.getResources().getDisplayMetrics().widthPixels);
            context.getResources().getDimensionPixelSize(R.dimen.coui_medium_btn_height);
            boolean canFitVerticalButtons = (height * 2) + verticalButtonMarginTop <= buttonLayout.getMaxHeight() && buttonLayout.getMaxHeight() != 0;
            if (this.mCurLineCount == MULTI_LINE && canFitVerticalButtons && !buttonLayout.isLimitHeight()) {
                buttonLayout.setOrientation(LinearLayout.VERTICAL);
                buttonWidth = zIsSmallScreen ? -1 : context.getResources().getDimensionPixelSize(R.dimen.coui_larger_btn_width);
                buttonMaxHeight = -2;
            } else {
                buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
                if (zIsSmallScreen || buttonLayout.isForceSmallScreenWidth()) {
                    mediumButtonWidth = -1;
                }
                buttonMaxHeight = getButtonMaxHeight(button, this.mCurLineCount);
                buttonWidth = mediumButtonWidth;
            }
            SizeProcessor sizeProcessorCreate = new SizeProcessor.Builder(this.mCurLineCount).setHeight(buttonMaxHeight).setWidth(buttonWidth).create();
            int processorLineCount = buttonLayout.getOrientation() != LinearLayout.VERTICAL ? this.mCurLineCount : 1;
            for (int index = 0; index < buttonWrapList.size(); index++) {
                List<Processor> processors = buttonWrapList.get(index).getProcessorMap().get(processorLineCount);
                if (processors != null) {
                    for (Processor processor : processors) {
                        if (processor instanceof SizeProcessor) {
                            sizeProcessorCreate.process(buttonWrapList.get(index).getProcessView());
                        } else {
                            processor.process(buttonWrapList.get(index).getProcessView());
                        }
                    }
                }
            }
            setHorizontalButtonMargin(buttonWrapList);
        }
    }

    private void handleVisibilityChange(COUIButtonLayout buttonLayout) {
        View firstButton = getFirstButton();
        if (firstButton instanceof COUIButton) {
            if (((COUIButtonLayout) firstButton.getParent()).getOrientation() == LinearLayout.VERTICAL) {
                adjustButtonWidths();
            }
            this.mTextChangeBtn = (COUIButton) firstButton;
            firstButton.requestLayout();
        }
    }

    public void lambda$ensureParentListenerRegistered$0(COUIButtonLayout buttonLayout, int visibility) {
        if (visibility != View.VISIBLE || this.mSingleButtonWrapList.isEmpty()) {
            return;
        }
        handleVisibilityChange(buttonLayout);
    }

    public void lambda$onLayoutChange$1(int lineCount, COUIButton button, int buttonIndex) {
        List<SingleButtonWrap> buttonWrapList = this.mSingleButtonWrapList;
        if (buttonWrapList == null || buttonWrapList.isEmpty()) {
            return;
        }
        onViewStateChanged(lineCount > 1 ? MULTI_LINE : SINGLE_LINE);
        postProcessExtraLongLogic(button, buttonIndex, this.mSingleButtonWrapList);
    }

    public void postProcessExtraLongLogic(COUIButton button, int buttonIndex, List<SingleButtonWrap> buttonWrapList) {
        if (buttonWrapList == null || buttonWrapList.size() <= 0) {
            return;
        }
        handleExtraLongLogic(buttonIndex, buttonWrapList);
    }

    private void processSameSize() {
        COUIButton customButton = this.mCustomButton;
        if (customButton == null || this.mCustomLayout == null) {
            return;
        }
        int buttonWidth = customButton.getContext().getResources().getDimensionPixelSize(R.dimen.coui_medium_btn_width);
        int lineCount = getButtonLineCount(this.mCustomButton) != MULTI_LINE ? SINGLE_LINE : MULTI_LINE;
        int buttonMaxHeight = getButtonMaxHeight(this.mCustomButton, lineCount);
        SingleButtonWrap singleButtonWrap = this.mCustomButtonWarp;
        if (singleButtonWrap != null) {
            singleButtonWrap.onViewStateChanged(lineCount);
        }
        if (this.mCustomButton.getLayoutParams() instanceof LinearLayout.LayoutParams) {
            boolean zIsSmallScreen = COUIResponsiveUtils.isSmallScreen(this.mCustomButton.getContext(), this.mCustomButton.getContext().getResources().getDisplayMetrics().widthPixels);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) this.mCustomButton.getLayoutParams();
            LinearLayout.LayoutParams customLayoutParams = (LinearLayout.LayoutParams) this.mCustomLayout.getLayoutParams();
            if (zIsSmallScreen) {
                layoutParams.weight = 1.0f;
                customLayoutParams.weight = 1.0f;
                buttonWidth = -1;
            } else {
                layoutParams.weight = 0.0f;
                customLayoutParams.weight = 0.0f;
            }
            SizeProcessor sizeProcessorCreate = new SizeProcessor.Builder(lineCount).setHeight(buttonMaxHeight).setWidth(buttonWidth).create();
            sizeProcessorCreate.process(this.mCustomLayout);
            sizeProcessorCreate.process(this.mCustomButton);
            this.mCustomButton.setLayoutParams(layoutParams);
            this.mCustomLayout.setLayoutParams(customLayoutParams);
        }
    }

    private void resetLayoutToHorizontal(Configuration configuration) {
        View processView;
        if (this.mSingleButtonWrapList.isEmpty() || (processView = this.mSingleButtonWrapList.get(0).getProcessView()) == null || !(processView.getParent() instanceof COUIButtonLayout)) {
            return;
        }
        COUIButtonLayout buttonLayout = (COUIButtonLayout) processView.getParent();
        Context context = processView.getContext();
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        this.mCachedHorizontalButtonMaxWidth = 0;
        boolean zIsSmallScreen = COUIResponsiveUtils.isSmallScreen(context, context.getResources().getDisplayMetrics().widthPixels);
        Iterator<SingleButtonWrap> it = this.mSingleButtonWrapList.iterator();
        while (it.hasNext()) {
            View processView2 = it.next().getProcessView();
            if (processView2 != null) {
                ViewGroup.LayoutParams layoutParams = processView2.getLayoutParams();
                if (layoutParams instanceof LinearLayout.LayoutParams) {
                    LinearLayout.LayoutParams layoutParams2 = (LinearLayout.LayoutParams) layoutParams;
                    if (zIsSmallScreen) {
                        layoutParams2.width = -1;
                        layoutParams2.weight = 1.0f;
                    } else {
                        layoutParams2.width = context.getResources().getDimensionPixelSize(R.dimen.coui_medium_btn_width);
                        layoutParams2.weight = 0.0f;
                    }
                    processView2.setLayoutParams(layoutParams2);
                }
            }
        }
    }

    private void setHorizontalButtonMargin(List<SingleButtonWrap> list) {
        if (list.size() != 2) {
            return;
        }
        SingleButtonWrap firstButtonWrap = list.get(0);
        int horizontalWeight = 1;
        SingleButtonWrap secondButtonWrap = list.get(1);
        COUIButton firstButton = (COUIButton) firstButtonWrap.getProcessView();
        COUIButton secondButton = (COUIButton) secondButtonWrap.getProcessView();
        Context context = firstButton.getContext();
        if (firstButton.getParent() instanceof COUIButtonLayout) {
            boolean zIsSmallScreen = COUIResponsiveUtils.isSmallScreen(context, context.getResources().getDisplayMetrics().widthPixels);
            COUIButtonLayout buttonLayout = (COUIButtonLayout) firstButton.getParent();
            LinearLayout.LayoutParams firstLayoutParams = (LinearLayout.LayoutParams) firstButton.getLayoutParams();
            LinearLayout.LayoutParams secondLayoutParams = (LinearLayout.LayoutParams) secondButton.getLayoutParams();
            buttonLayout.removeAllViews();
            if (buttonLayout.getOrientation() != LinearLayout.HORIZONTAL) {
                firstLayoutParams.setMarginEnd(0);
                firstLayoutParams.setMarginStart(0);
                firstLayoutParams.topMargin = context.getResources().getDimensionPixelSize(R.dimen.coui_vertical_btn_margin_top);
                float verticalWeight = 0;
                firstLayoutParams.weight = verticalWeight;
                firstButton.setLayoutParams(firstLayoutParams);
                secondLayoutParams.setMarginEnd(0);
                secondLayoutParams.setMarginStart(0);
                secondLayoutParams.topMargin = 0;
                secondLayoutParams.weight = verticalWeight;
                secondButton.setLayoutParams(secondLayoutParams);
                buttonLayout.addView(secondButton);
                buttonLayout.addView(firstButton);
                return;
            }
            if (!zIsSmallScreen && !buttonLayout.isForceSmallScreenWidth()) {
                horizontalWeight = 0;
            }
            Resources resources = context.getResources();
            int horizontalButtonPadding = R.dimen.coui_horizontal_btn_padding;
            firstLayoutParams.setMarginEnd(resources.getDimensionPixelSize(horizontalButtonPadding));
            firstLayoutParams.setMarginStart(0);
            firstLayoutParams.topMargin = 0;
            float horizontalLayoutWeight = horizontalWeight;
            firstLayoutParams.weight = horizontalLayoutWeight;
            firstButton.setLayoutParams(firstLayoutParams);
            secondLayoutParams.setMarginEnd(0);
            secondLayoutParams.setMarginStart(context.getResources().getDimensionPixelSize(horizontalButtonPadding));
            secondLayoutParams.topMargin = 0;
            secondLayoutParams.weight = horizontalLayoutWeight;
            secondButton.setLayoutParams(secondLayoutParams);
            buttonLayout.addView(firstButton);
            buttonLayout.addView(secondButton);
        }
    }

    public int getSingleButtonWrapListSize() {
        return this.mSingleButtonWrapList.size();
    }

    public int getType() {
        return this.mType;
    }

    public void onConfigurationChanged(Configuration configuration) {
        resetLayoutToHorizontal(configuration);
        Iterator<SingleButtonWrap> it = this.mSingleButtonWrapList.iterator();
        while (it.hasNext()) {
            it.next().onConfigurationChanged(configuration);
        }
        SingleButtonWrap singleButtonWrap = this.mCustomButtonWarp;
        if (singleButtonWrap != null) {
            singleButtonWrap.onConfigurationChanged(configuration);
            processSameSize();
        }
    }

    @Override
    public void onLayoutChange(final View view, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        if ((view instanceof COUIButton) && view == this.mTextChangeBtn) {
            this.mTextChangeBtn = null;
            if (!this.mParentListenerRegistered && (view.getParent() instanceof COUIButtonLayout)) {
                ensureParentListenerRegistered((COUIButtonLayout) view.getParent());
            }
            final COUIButton button = (COUIButton) view;
            int buttonLineCount = getButtonLineCount(button);
            int maxOtherLineCount = -1;
            int maxOtherLineCountIndex = -1;
            for (int index = 0; index < this.mSingleButtonWrapList.size(); index++) {
                COUIButton currentButton = (COUIButton) this.mSingleButtonWrapList.get(index).getProcessView();
                int currentButtonLineCount = getButtonLineCount(currentButton);
                if (currentButton == view) {
                    this.mLineCountIndex = index;
                } else if (currentButtonLineCount > maxOtherLineCount) {
                    maxOtherLineCountIndex = index;
                    maxOtherLineCount = currentButtonLineCount;
                }
            }
            if (buttonLineCount > maxOtherLineCount) {
                this.mCurLineCount = buttonLineCount;
            } else {
                this.mCurLineCount = maxOtherLineCount;
                this.mLineCountIndex = maxOtherLineCountIndex;
            }
            final int lineCount = this.mCurLineCount;
            final int lineCountIndex = this.mLineCountIndex;
            ViewTreeObserver viewTreeObserver = view.getViewTreeObserver();
            if (viewTreeObserver.isAlive()) {
                viewTreeObserver.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        ViewTreeObserver observer = view.getViewTreeObserver();
                        if (observer.isAlive()) {
                            observer.removeOnPreDrawListener(this);
                        }
                        if (SimpleButtonGroupCtrl.this.mSingleButtonWrapList == null || SimpleButtonGroupCtrl.this.mSingleButtonWrapList.isEmpty()) {
                            return true;
                        }
                        SimpleButtonGroupCtrl.this.mCurLineCount = lineCount;
                        SimpleButtonGroupCtrl.this.mLineCountIndex = lineCountIndex;
                        SimpleButtonGroupCtrl simpleButtonGroupCtrl = SimpleButtonGroupCtrl.this;
                        simpleButtonGroupCtrl.onViewStateChanged(simpleButtonGroupCtrl.mCurLineCount > 1 ? MULTI_LINE : SINGLE_LINE);
                        SimpleButtonGroupCtrl simpleButtonGroupCtrl2 = SimpleButtonGroupCtrl.this;
                        simpleButtonGroupCtrl2.postProcessExtraLongLogic(button, lineCountIndex, simpleButtonGroupCtrl2.mSingleButtonWrapList);
                        return false;
                    }
                });
            } else {
                button.post(new Runnable() {
                    @Override
                    public final void run() {
                        SimpleButtonGroupCtrl.this.lambda$onLayoutChange$1(lineCount, button, lineCountIndex);
                    }
                });
            }
        }
    }

    @Override
    public void onSizeChanged(View view, int width, int height, int oldWidth, int oldHeight) {
        processSameSize();
    }

    @Override
    public void onTextChanged(View view, CharSequence text, int start, int before, int count) {
        this.mTextChangeBtn = (COUIButton) view;
        view.requestLayout();
    }

    @Override
    public void onViewStateChanged(int state) {
        int lineCountIndex = this.mLineCountIndex;
        if (lineCountIndex < 0) {
            COUILog.e(TAG, "The mLineCountIndex cannot be less than zero");
            return;
        }
        SingleButtonWrap singleButtonWrap = this.mSingleButtonWrapList.get(lineCountIndex);
        if (singleButtonWrap != null) {
            singleButtonWrap.onViewStateChanged(state);
        }
        SingleButtonWrap singleButtonWrap2 = this.mCustomButtonWarp;
        if (singleButtonWrap2 != null) {
            singleButtonWrap2.onViewStateChanged(state);
        }
    }

    public void registerButton(COUIButton button) {
        registerButton(button, 1);
    }

    public void registerButtonAndCustomView(COUIButton button, LinearLayout customLayout, int type) {
        this.mCustomButtonWarp = new SingleButtonWrap(button, type);
        this.mCustomButton = button;
        this.mCustomLayout = customLayout;
        button.setOnSizeChangeListener(this);
        button.setText(button.getText());
        processSameSize();
    }

    @Override
    public void release() {
        View processView;
        if (this.mParentListenerRegistered && !this.mSingleButtonWrapList.isEmpty() && (processView = this.mSingleButtonWrapList.get(0).getProcessView()) != null && (processView.getParent() instanceof COUIButtonLayout)) {
            ((COUIButtonLayout) processView.getParent()).setOnButtonLayoutVisibilityChangedListener(null);
        }
        this.mParentListenerRegistered = false;
        for (SingleButtonWrap singleButtonWrap : this.mSingleButtonWrapList) {
            singleButtonWrap.getProcessView().removeOnLayoutChangeListener(this);
            singleButtonWrap.release();
        }
        this.mSingleButtonWrapList.clear();
        this.mTextChangeBtn = null;
        SingleButtonWrap singleButtonWrap2 = this.mCustomButtonWarp;
        if (singleButtonWrap2 != null) {
            singleButtonWrap2.release();
        }
    }

    public void setType(int type) {
        this.mType = type;
    }

    public void unregisterButton(COUIButton button) {
        if (button == null || this.mSingleButtonWrapList.size() == 0) {
            return;
        }
        Iterator<SingleButtonWrap> it = this.mSingleButtonWrapList.iterator();
        while (it.hasNext()) {
            SingleButtonWrap next = it.next();
            if (next.getProcessView() == button) {
                next.release();
                it.remove();
            }
        }
    }

    public void registerButton(COUIButton button, int type) {
        if (this.mSingleButtonWrapList.size() >= 2) {
            return;
        }
        setType(type);
        this.mSingleButtonWrapList.add(new SingleButtonWrap(button, type));
        button.setOnTextChangeListener(this);
        button.addOnLayoutChangeListener(this);
        button.setText(button.getText());
    }
}
