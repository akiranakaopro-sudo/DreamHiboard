package com.coui.appcompat.buttonBar;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.core.content.ContextCompat;

import com.coui.appcompat.R;
import com.coui.appcompat.button.COUIButton;
import com.coui.appcompat.contextutil.COUIContextUtil;

public class COUIButtonBarLayout extends LinearLayout {
    private static final int BUTTON_FADE_IN_DURATION = 100;
    private static final int BUTTON_FADE_OUT_DURATION = 360;
    public static final int NO_RECOMMEND_ID = -1;
    private static final int ONE = 1;
    private static final String TAG = "COUIButtonBarLayout";
    private static final int THREE = 3;
    private static final int TWO = 2;
    private static final int ZERO = 0;

    private View mButDivider1;
    private View mButDivider2;
    private int mButDividerSizeHorizontalButton;
    private int mButDividerSizeVerticalButton;
    private View mContentPanel;
    private Context mContext;
    private int mCouiBottomAlertDialogButtonbarMargintop;
    private View mCustomPanel;
    private int mDialogMaxWidth;
    private int mDividerMarginHorizontalDefault;
    private boolean mDynamicLayout;
    private int mHorButDividerVerMarginBottom;
    private int mHorButDividerVerMarginTop;
    private int mHorButHorPadding;
    private int mHorButHorPaddingWithRecommend;
    private int mHorButPaddingBottom;
    private int mHorButPaddingTop;
    private int mHorButPanelMinHeight;
    private int mHorButVerPaddingWithRecommend;
    private int mHorizontalButtonPaddingBottom;
    private int mHorizontalButtonPaddingTop;
    private boolean mIsVerticalButton;
    private COUIButton mNegButton;
    private COUIButton mNeuButton;
    private int mNonRecommendButtonMarginVertical;
    private View mParentView;
    private COUIButton mPosButton;
    private int mRecomentButtonPaddingVertical;
    private int mRecomentButtonPaddingVerticalMultiline;
    private int mRecommendButtonHeight;
    private int mRecommendButtonId;
    private int mRecommendButtonMarginHorizontal;
    private boolean mSetTopMarginFlag;
    private boolean mShowDivider;
    private boolean mShowDividerWhenHasItems;
    private View mTopPanel;
    private int mVerButDividerHorMargin;
    private int mVerButMinHeightBottom;
    private int mVerButMinHeightNormal;
    private int mVerButtonVecPaddingNew;
    private int mVerCenterButVerPaddingBottomExtra;
    private int mVerPaddingBottom;
    private int mVerPaddingBottomExtraNew;
    private int mVerPaddingTopExtraNew;

    public COUIButtonBarLayout(Context context) {
        super(context, null);
        this.mShowDivider = true;
        this.mDynamicLayout = true;
        this.mRecommendButtonId = NO_RECOMMEND_ID;
        this.mSetTopMarginFlag = true;
        this.mShowDividerWhenHasItems = false;
    }

    public COUIButtonBarLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public COUIButtonBarLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mShowDivider = true;
        this.mDynamicLayout = true;
        this.mRecommendButtonId = NO_RECOMMEND_ID;
        this.mSetTopMarginFlag = true;
        this.mShowDividerWhenHasItems = false;
        init(context, attrs);
    }

    private void applyRecommendLayout(COUIButton button) {
        ViewGroup.LayoutParams layoutParams = button.getLayoutParams();
        layoutParams.height = -1;
        button.setMaxLines(2);
        button.setEllipsize(TextUtils.TruncateAt.END);
        String text = button.getText().toString();
        int measuredWidth = (button.getMeasuredWidth() - button.getPaddingLeft()) - button.getPaddingRight();
        float textWidth = button.getPaint().measureText(text);
        int verticalPadding = this.mRecomentButtonPaddingVertical;
        if (textWidth > measuredWidth) {
            verticalPadding = this.mRecomentButtonPaddingVerticalMultiline;
        }
        int horizontalPadding = this.mHorButHorPaddingWithRecommend;
        button.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);
        if ((layoutParams instanceof ViewGroup.MarginLayoutParams) && this.mIsVerticalButton) {
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) layoutParams;
            int topMargin = this.mNonRecommendButtonMarginVertical;
            COUIButton negButton = this.mNegButton;
            int bottomMargin = (button == negButton
                    || (button == this.mPosButton && !hasContent(negButton))
                    || (button == this.mNeuButton && !hasContent(this.mPosButton) && !hasContent(this.mNegButton)))
                    ? this.mVerPaddingBottomExtraNew + topMargin
                    : topMargin;
            button.setMinimumHeight(this.mRecommendButtonHeight);
            int horizontalMargin = this.mRecommendButtonMarginHorizontal;
            marginLayoutParams.setMargins(horizontalMargin, topMargin, horizontalMargin, bottomMargin);
        }
        button.setLayoutParams(layoutParams);
    }

    private void applyRecommendStyle(COUIButton button) {
        if (!hasContent(button)) {
            return;
        }
        if (button.getId() == this.mRecommendButtonId) {
            if (button.getDrawableColor() == getResources().getColor(R.color.coui_transparence)) {
                button.setDrawableColor(COUIContextUtil.getAttrColor(getContext(), R.attr.couiColorContainerTheme, 0));
            }
            button.setTextColor(ContextCompat.getColorStateList(this.mContext, R.color.coui_btn_default_text_color));
            button.setAnimType(1);
            button.setScaleEnable(true);
            button.setAnimEnable(true);
            button.setDisabledColor(COUIContextUtil.getAttrColor(getContext(), R.attr.couiColorDisable));
        } else {
            button.setAnimType(0);
        }
        button.setDrawableRadius(-1);
    }

    private int getBtnTextMeasureLength(Button button) {
        if (button == null || button.getVisibility() != VISIBLE) {
            return 0;
        }
        String text = button.getText().toString();
        return (int) (button.isAllCaps()
                ? button.getPaint().measureText(text.toUpperCase())
                : button.getPaint().measureText(text));
    }

    private boolean hasContent(View view) {
        return view != null && view.getVisibility() == VISIBLE;
    }

    private void hideAllDivider() {
        this.mButDivider1.setVisibility(GONE);
        this.mButDivider2.setVisibility(GONE);
    }

    private void init(Context context, AttributeSet attrs) {
        this.mContext = context;
        Resources resources = context.getResources();
        this.mHorButHorPadding = resources.getDimensionPixelSize(R.dimen.coui_alert_dialog_button_horizontal_padding);
        this.mHorButHorPaddingWithRecommend = resources.getDimensionPixelSize(R.dimen.coui_alert_dialog_button_horizontal_padding_with_recommend);
        this.mHorButVerPaddingWithRecommend = resources.getDimensionPixelSize(R.dimen.coui_alert_dialog_button_vertical_padding_with_recommend);
        this.mHorButPaddingTop = resources.getDimensionPixelSize(R.dimen.coui_alert_dialog_button_padding_top);
        this.mHorButPaddingBottom = resources.getDimensionPixelSize(R.dimen.coui_alert_dialog_button_padding_bottom);
        this.mVerButMinHeightNormal = resources.getDimensionPixelSize(R.dimen.coui_alert_dialog_vertical_button_min_height);
        int verticalExtra = resources.getDimensionPixelSize(R.dimen.coui_center_alert_dialog_vertical_button_paddingbottom_vertical_extra);
        this.mVerCenterButVerPaddingBottomExtra = verticalExtra;
        this.mVerButMinHeightBottom = this.mVerButMinHeightNormal + verticalExtra;
        int recommendMargin = R.dimen.coui_bottom_alert_dialog_horizontal_button_margin_recommend;
        this.mVerButDividerHorMargin = resources.getDimensionPixelSize(recommendMargin);
        this.mHorButDividerVerMarginTop = resources.getDimensionPixelSize(R.dimen.coui_bottom_alert_dialog_horizontal_button_padding_top_extra_divider_new);
        this.mHorButDividerVerMarginBottom = resources.getDimensionPixelSize(R.dimen.coui_bottom_alert_dialog_horizontal_button_padding_bottom_extra_divider_new);
        this.mHorButPanelMinHeight = resources.getDimensionPixelSize(R.dimen.coui_alert_dialog_button_height);
        this.mDialogMaxWidth = resources.getDimensionPixelSize(R.dimen.coui_dialog_max_width);
        this.mButDividerSizeHorizontalButton = resources.getDimensionPixelSize(R.dimen.coui_delete_alert_dialog_divider_height_horizontalbutton);
        TypedArray typedArray = this.mContext.obtainStyledAttributes(attrs, R.styleable.COUIButtonBarLayout);
        this.mShowDivider = typedArray.getBoolean(R.styleable.COUIButtonBarLayout_buttonBarShowDivider, true);
        this.mButDividerSizeVerticalButton = typedArray.getDimensionPixelOffset(
                R.styleable.COUIButtonBarLayout_buttonBarDividerSize,
                resources.getDimensionPixelSize(R.dimen.coui_delete_alert_dialog_divider_height_verticalbutton));
        this.mVerPaddingTopExtraNew = resources.getDimensionPixelSize(R.dimen.coui_bottom_alert_dialog_vertical_button_padding_top_extra_new);
        this.mVerPaddingBottomExtraNew = resources.getDimensionPixelSize(R.dimen.coui_bottom_alert_dialog_vertical_button_padding_bottom_extra_new);
        this.mVerButtonVecPaddingNew = resources.getDimensionPixelSize(R.dimen.coui_bottom_alert_dialog_vertical_button_padding_vertical_new);
        this.mHorizontalButtonPaddingTop = resources.getDimensionPixelSize(R.dimen.coui_bottom_alert_dialog_horizontal_button_padding_top_extra_new);
        this.mHorizontalButtonPaddingBottom = resources.getDimensionPixelSize(R.dimen.coui_bottom_alert_dialog_horizontal_button_padding_bottom_extra_new);
        this.mDividerMarginHorizontalDefault = resources.getDimensionPixelSize(R.dimen.coui_bottom_alert_dialog_horizontal_button_margin_default);
        this.mRecommendButtonMarginHorizontal = resources.getDimensionPixelSize(recommendMargin);
        this.mRecomentButtonPaddingVertical = resources.getDimensionPixelSize(R.dimen.coui_bottom_alert_dialog_recommend_button_padding_vertical);
        this.mRecomentButtonPaddingVerticalMultiline = resources.getDimensionPixelSize(R.dimen.coui_bottom_alert_dialog_recommend_button_padding_vertical_multi_line);
        this.mNonRecommendButtonMarginVertical = resources.getDimensionPixelSize(R.dimen.coui_bottom_alert_dialog_vertical_button_margin_nonrecommend);
        this.mCouiBottomAlertDialogButtonbarMargintop = resources.getDimensionPixelSize(R.dimen.coui_bottom_alert_dialog_buttonbar_margintop);
        this.mRecommendButtonHeight = resources.getDimensionPixelSize(R.dimen.coui_bottom_alert_dialog_button_recommend_height);
        typedArray.recycle();
    }

    private void initChildView() {
        if (this.mPosButton == null || this.mNegButton == null || this.mNeuButton == null || this.mButDivider1 == null || this.mButDivider2 == null) {
            this.mPosButton = findViewById(android.R.id.button1);
            this.mNegButton = findViewById(android.R.id.button2);
            this.mNeuButton = findViewById(android.R.id.button3);
            this.mButDivider1 = findViewById(R.id.coui_dialog_button_divider_1);
            this.mButDivider2 = findViewById(R.id.coui_dialog_button_divider_2);
        }
    }

    private void initParentView() {
        if (this.mParentView == null || this.mTopPanel == null || this.mContentPanel == null || this.mCustomPanel == null) {
            View parentView = (View) ((View) getParent()).getParent();
            this.mParentView = parentView;
            this.mTopPanel = parentView.findViewById(R.id.topPanel);
            this.mContentPanel = this.mParentView.findViewById(R.id.contentPanel);
            this.mCustomPanel = this.mParentView.findViewById(R.id.customPanel);
        }
    }

    private boolean isRecommendButton(Button button) {
        return button.getId() == this.mRecommendButtonId;
    }

    private boolean isVertical() {
        return getOrientation() == VERTICAL;
    }

    private boolean needSetButVertical(int width) {
        int buttonCount = getButtonCount();
        if (buttonCount == 0) {
            return false;
        }
        int availableButtonWidth = ((width - ((buttonCount - 1) * this.mButDividerSizeVerticalButton)) / buttonCount) - (this.mHorButHorPadding * 2);
        return getBtnTextMeasureLength(this.mPosButton) > availableButtonWidth
                || getBtnTextMeasureLength(this.mNegButton) > availableButtonWidth
                || getBtnTextMeasureLength(this.mNeuButton) > availableButtonWidth;
    }

    private void resetHorButsPadding() {
        setPaddingTop(this.mNegButton, this.mHorizontalButtonPaddingTop);
        setPaddingBottom(this.mNegButton, this.mHorizontalButtonPaddingBottom);
        setPaddingTop(this.mPosButton, this.mHorizontalButtonPaddingTop);
        setPaddingBottom(this.mPosButton, this.mHorizontalButtonPaddingBottom);
        setPaddingTop(this.mNeuButton, this.mHorizontalButtonPaddingTop);
        setPaddingBottom(this.mNeuButton, this.mHorizontalButtonPaddingBottom);
    }

    private void resetHorDividerVisibility() {
        if (getButtonCount() != TWO) {
            if (getButtonCount() == THREE) {
                showDivider(this.mButDivider1, this.mButDivider2);
            } else {
                hideAllDivider();
            }
            return;
        }
        if (!hasContent(this.mNegButton)) {
            showDivider(this.mButDivider2);
        } else if (hasContent(this.mNeuButton) || hasContent(this.mPosButton)) {
            showDivider(this.mButDivider1);
        } else {
            hideAllDivider();
        }
    }

    private void resetVerButsPadding() {
        if (hasContent(this.mNegButton)) {
            int top;
            int bottom;
            if (getButtonCount() > ONE) {
                top = this.mVerButtonVecPaddingNew;
                if (!hasContent(this.mPosButton) && !hasContent(this.mNeuButton) && !hasContent(this.mTopPanel) && !hasContent(this.mContentPanel) && !hasContent(this.mCustomPanel)) {
                    top += this.mVerPaddingTopExtraNew;
                }
                bottom = this.mVerButtonVecPaddingNew + this.mVerPaddingBottomExtraNew;
            } else {
                top = this.mHorizontalButtonPaddingTop;
                bottom = this.mHorizontalButtonPaddingBottom;
                this.mNegButton.setMinimumHeight(this.mHorButPanelMinHeight);
            }
            this.mNegButton.setPaddingRelative(this.mNegButton.getPaddingStart(), top, this.mNegButton.getPaddingEnd(), bottom);
        }
        if (hasContent(this.mPosButton)) {
            int bottom = this.mVerButtonVecPaddingNew;
            int top = (hasContent(this.mNeuButton) || hasContent(this.mTopPanel) || hasContent(this.mContentPanel) || hasContent(this.mCustomPanel))
                    ? bottom
                    : this.mVerPaddingTopExtraNew + bottom;
            if (!hasContent(this.mNegButton)) {
                bottom += this.mVerPaddingBottomExtraNew;
            }
            this.mPosButton.setPaddingRelative(this.mPosButton.getPaddingStart(), top, this.mPosButton.getPaddingEnd(), bottom);
        }
        if (hasContent(this.mNeuButton)) {
            int bottom = this.mVerButtonVecPaddingNew;
            int top = (hasContent(this.mTopPanel) || hasContent(this.mContentPanel) || hasContent(this.mCustomPanel))
                    ? bottom
                    : this.mVerPaddingTopExtraNew + bottom;
            if (!hasContent(this.mNegButton) && !hasContent(this.mPosButton)) {
                bottom += this.mVerPaddingBottomExtraNew;
            }
            this.mNeuButton.setPaddingRelative(this.mNeuButton.getPaddingStart(), top, this.mNeuButton.getPaddingEnd(), bottom);
        }
    }

    private void resetVerDividerVisibility() {
        if (this.mRecommendButtonId != NO_RECOMMEND_ID || getButtonCount() == ZERO) {
            hideAllDivider();
            return;
        }
        if (!hasContent(this.mNegButton)) {
            if (hasContent(this.mNeuButton) && hasContent(this.mPosButton)) {
                showDivider(this.mButDivider1);
            } else {
                hideAllDivider();
            }
            return;
        }
        if (hasContent(this.mNeuButton) && hasContent(this.mPosButton)) {
            showDivider(this.mButDivider1, this.mButDivider2);
        } else if (hasContent(this.mNeuButton)) {
            showDivider(this.mButDivider1);
        } else if (hasContent(this.mPosButton)) {
            showDivider(this.mButDivider2);
        } else if (this.mShowDividerWhenHasItems) {
            showDivider(this.mButDivider2);
        } else {
            hideAllDivider();
        }
    }

    private void resetVerPaddingBottom() {
        setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), this.mVerPaddingBottom);
    }

    private void resortButton() {
        if (!hasContent(this.mPosButton) && !hasContent(this.mNegButton) && !hasContent(this.mNeuButton)) {
            return;
        }
        if (getOrientation() == VERTICAL) {
            bringChildToFront((View) this.mNeuButton.getParent());
            bringChildToFront(this.mButDivider1);
            bringChildToFront((View) this.mPosButton.getParent());
            bringChildToFront(this.mButDivider2);
            bringChildToFront((View) this.mNegButton.getParent());
            return;
        }
        bringChildToFront((View) this.mNegButton.getParent());
        bringChildToFront(this.mButDivider1);
        bringChildToFront((View) this.mNeuButton.getParent());
        bringChildToFront(this.mButDivider2);
        bringChildToFront((View) this.mPosButton.getParent());
    }

    private void setButHorizontal(COUIButton button) {
        LinearLayout parent = (LinearLayout) button.getParent();
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) parent.getLayoutParams();
        layoutParams.weight = 1.0f;
        layoutParams.width = 0;
        layoutParams.height = this.mRecommendButtonId != NO_RECOMMEND_ID ? -2 : -1;
        layoutParams.gravity = 16;
        parent.setLayoutParams(layoutParams);
        int horizontal = this.mHorButHorPadding;
        int top = this.mHorButPaddingTop;
        int bottom = this.mHorButPaddingBottom;
        if (this.mRecommendButtonId != NO_RECOMMEND_ID) {
            horizontal = this.mHorButHorPaddingWithRecommend;
            top = this.mHorButVerPaddingWithRecommend;
            bottom = top;
        }
        button.setMinimumHeight(this.mHorButPanelMinHeight);
        button.setPaddingRelative(horizontal, top, horizontal, bottom);
    }

    private void setButtonsHorizontal() {
        setOrientation(HORIZONTAL);
        setGravity(16);
        setHorButDivider1();
        setButHorizontal(this.mNeuButton);
        setHorButDivider2();
        setButHorizontal(this.mPosButton);
        setButHorizontal(this.mNegButton);
    }

    private void setButtonsVertical() {
        setOrientation(VERTICAL);
        setMinimumHeight(0);
        setNeuButVertical();
        setVerButDivider1();
        setPosButVertical();
        setVerButDivider2();
        setNegButVertical();
    }

    private void setHorButDivider1() {
        setHorizontalDivider(this.mButDivider1);
    }

    private void setHorButDivider2() {
        setHorizontalDivider(this.mButDivider2);
    }

    private void setHorizontalDivider(View divider) {
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) divider.getLayoutParams();
        layoutParams.width = this.mButDividerSizeHorizontalButton;
        layoutParams.height = -1;
        layoutParams.setMarginStart(0);
        layoutParams.setMarginEnd(0);
        layoutParams.topMargin = this.mHorButDividerVerMarginTop;
        layoutParams.bottomMargin = this.mHorButDividerVerMarginBottom;
        divider.setLayoutParams(layoutParams);
    }

    private void setNegButVertical() {
        setButtonVertical(this.mNegButton, this.mVerButMinHeightBottom);
    }

    private void setNeuButVertical() {
        setButtonVertical(this.mNeuButton, hasContent(this.mNegButton) || hasContent(this.mPosButton) ? this.mVerButMinHeightNormal : this.mVerButMinHeightBottom);
    }

    private void setPaddingBottom(View view, int paddingBottom) {
        view.setPaddingRelative(view.getPaddingStart(), view.getPaddingTop(), view.getPaddingEnd(), paddingBottom);
    }

    private void setPaddingTop(View view, int paddingTop) {
        view.setPaddingRelative(view.getPaddingStart(), paddingTop, view.getPaddingEnd(), view.getPaddingBottom());
    }

    private void setPosButVertical() {
        setButtonVertical(this.mPosButton, hasContent(this.mNegButton) ? this.mVerButMinHeightNormal : this.mVerButMinHeightBottom);
    }

    private void setButtonVertical(COUIButton button, int minHeight) {
        View parent = (View) button.getParent();
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) parent.getLayoutParams();
        layoutParams.weight = 1.0f;
        layoutParams.width = -1;
        layoutParams.height = -2;
        button.setMinimumHeight(minHeight);
        parent.setLayoutParams(layoutParams);
    }

    private void setVerButDivider1() {
        setVerticalDivider(this.mButDivider1);
    }

    private void setVerButDivider2() {
        setVerticalDivider(this.mButDivider2);
    }

    private void setVerticalDivider(View divider) {
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) divider.getLayoutParams();
        layoutParams.width = -1;
        layoutParams.height = this.mButDividerSizeVerticalButton;
        if (this.mRecommendButtonId != NO_RECOMMEND_ID) {
            layoutParams.setMarginStart(this.mVerButDividerHorMargin);
            layoutParams.setMarginEnd(this.mVerButDividerHorMargin);
        } else {
            layoutParams.setMarginStart(this.mDividerMarginHorizontalDefault);
            layoutParams.setMarginEnd(this.mDividerMarginHorizontalDefault);
        }
        layoutParams.topMargin = 0;
        layoutParams.bottomMargin = 0;
        divider.setLayoutParams(layoutParams);
    }

    private void showButton() {
        showButtonParent(this.mPosButton);
        showButtonParent(this.mNeuButton);
        showButtonParent(this.mNegButton);
        if (this.mRecommendButtonId != NO_RECOMMEND_ID) {
            applyRecommendStyle(this.mPosButton);
            applyRecommendStyle(this.mNegButton);
            applyRecommendStyle(this.mNeuButton);
        }
    }

    private void showButtonParent(COUIButton button) {
        if (hasContent(button)) {
            ((ViewGroup) button.getParent()).setVisibility(VISIBLE);
        }
    }

    private void showDivider(View... dividers) {
        hideAllDivider();
        if (!this.mShowDivider || dividers == null) {
            return;
        }
        for (View divider : dividers) {
            divider.setVisibility(VISIBLE);
        }
    }

    public int getButtonCount() {
        int count = hasContent(this.mPosButton) ? 1 : 0;
        if (hasContent(this.mNegButton)) {
            count++;
        }
        return hasContent(this.mNeuButton) ? count + 1 : count;
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        initParentView();
        showButton();
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        initChildView();
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        boolean isVertical = this.mDynamicLayout && !(!needSetButVertical(Math.min(this.mDialogMaxWidth, getMeasuredWidth())) && getButtonCount() == TWO && this.mRecommendButtonId == NO_RECOMMEND_ID);
        this.mIsVerticalButton = isVertical;
        if (!isVertical) {
            setButtonsHorizontal();
            resetHorButsPadding();
            resetHorDividerVisibility();
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        setButtonsVertical();
        resetVerButsPadding();
        resetVerDividerVisibility();
        resetVerPaddingBottom();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (this.mSetTopMarginFlag && (getButtonCount() > ONE || (getButtonCount() == ONE && this.mRecommendButtonId != NO_RECOMMEND_ID))) {
            ((ViewGroup.MarginLayoutParams) getLayoutParams()).topMargin = this.mCouiBottomAlertDialogButtonbarMargintop;
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
        if (this.mRecommendButtonId != NO_RECOMMEND_ID) {
            applyRecommendLayout(this.mPosButton);
            applyRecommendLayout(this.mNegButton);
            applyRecommendLayout(this.mNeuButton);
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    public void setDynamicLayout(boolean dynamicLayout) {
        this.mDynamicLayout = dynamicLayout;
    }

    @Override
    public void setOrientation(int orientation) {
        if (getOrientation() != orientation) {
            super.setOrientation(orientation);
            resortButton();
        }
    }

    public void setRecommendButtonId(int recommendButtonId) {
        this.mRecommendButtonId = recommendButtonId;
    }

    public void setShowDividerWhenHasItems(boolean showDividerWhenHasItems) {
        this.mShowDividerWhenHasItems = showDividerWhenHasItems;
    }

    public void setTopMarginFlag(boolean setTopMarginFlag) {
        this.mSetTopMarginFlag = setTopMarginFlag;
    }

    @Deprecated
    public void setVerButDividerVerMargin(int margin) {
    }

    @Deprecated
    public void setVerButPaddingOffset(int offset) {
    }

    @Deprecated
    public void setVerButVerPadding(int padding) {
    }

    @Deprecated
    public void setVerNegButVerPaddingOffset(int offset) {
    }

    public void setVerPaddingBottom(int paddingBottom) {
        this.mVerPaddingBottom = paddingBottom;
    }
}
