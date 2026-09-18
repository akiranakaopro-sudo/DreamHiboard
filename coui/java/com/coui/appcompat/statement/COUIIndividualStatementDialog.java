package com.coui.appcompat.statement;

import com.coui.appcompat.R;

import android.content.Context;
import android.content.res.Configuration;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.coui.appcompat.button.COUIButton;
import com.coui.appcompat.checkbox.COUICheckBox;
import com.coui.appcompat.contextutil.COUIContextUtil;
import com.coui.appcompat.darkmode.COUIDarkModeUtil;
import com.coui.appcompat.panel.COUIBottomSheetDialog;
import com.coui.appcompat.panel.COUIPanelMultiWindowUtils;
import com.coui.appcompat.textutil.COUIChangeTextUtil;
import com.coui.appcompat.textviewcompatutil.COUITextViewCompatUtil;
import java.util.ArrayList;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.SourceDebugExtension;


@SourceDebugExtension({"SMAP\nCOUIIndividualStatementDialog.kt\nKotlin\n*S Kotlin\n*F\n+ 1 COUIIndividualStatementDialog.kt\ncom/coui/appcompat/statement/COUIIndividualStatementDialog\n+ 2 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n*L\n1#1,384:1\n1864#2,3:385\n*S KotlinDebug\n*F\n+ 1 COUIIndividualStatementDialog.kt\ncom/coui/appcompat/statement/COUIIndividualStatementDialog\n*L\n298#1:385,3\n*E\n"})
public class COUIIndividualStatementDialog extends COUIBottomSheetDialog {
    public static final Companion Companion = new Companion(null);
    public static final int MEDIUM_LARGE_SCREEN_SW_THRESHOLD = 480;
    public static final float ORIGIN_STATEMENT_TEXT_SIZE = 14.0f;
    public static final float STATEMENT_TEXT_SIZE_WITH_CHECKBOX = 12.0f;
    private TextView appStatement;
    private COUIButton bottomButton;
    private CharSequence bottomButtonText;
    private TextView exitButton;
    private CharSequence exitButtonText;
    private boolean isInSmallLand;
    private boolean isInSmallPortrait;
    private COUIComponentMaxHeightScrollView mScrollViewComponent;
    private OnButtonClickListener onButtonClickListener;
    private LinearLayout scrollViewLayout;
    private LinearLayout smallLandButtonLayout;
    private COUIButton smallLandConfirmButton;
    private COUIButton smallLandExitButton;
    private CharSequence statement;
    private CharSequence titleText;
    private TextView titleView;

    public static final class Companion {
        private Companion() {
        }

        public Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }
    }

    public interface OnButtonClickListener {
        void onBottomButtonClick(ArrayList<PrivacyItem> arrayList);

        void onExitButtonClick();
    }


    public COUIIndividualStatementDialog(Context context) {
        this(context, 0, 0.0f, 0.0f, 14, null);
        Intrinsics.checkNotNullParameter(context, "context");
    }


    public static final void addPrivacyList$lambda$16$lambda$15$lambda$14(COUIIndividualStatementDialog this$0, COUICheckBox cOUICheckBox, int index) {
        Intrinsics.checkNotNullParameter(this$0, "this$0");
        this$0.resetBottomButton();
    }

    private final ArrayList<PrivacyItem> getCheckedFunctionList() {
        ArrayList<PrivacyItem> arrayList = new ArrayList<>();
        LinearLayout linearLayout = this.scrollViewLayout;
        if (linearLayout.getChildCount() > 1) {
            int childCount = linearLayout.getChildCount();
            for (int index = 1; index < childCount; index++) {
                View childAt = linearLayout.getChildAt(index);
                Intrinsics.checkNotNull(childAt, "null cannot be cast to non-null type com.coui.appcompat.statement.COUICheckBoxItemView");
                COUICheckBoxItemView cOUICheckBoxItemView = (COUICheckBoxItemView) childAt;
                if (cOUICheckBoxItemView.isChecked()) {
                    arrayList.add(cOUICheckBoxItemView.getPrivacyItem());
                }
            }
        }
        return arrayList;
    }

    private final void initView() {
        TextView textView = this.appStatement;
        COUIDarkModeUtil.setForceDarkAllow(textView, false);
        textView.setTextColor(COUIContextUtil.getAttrColor(textView.getContext(), R.attr.couiColorSecondNeutral));
        COUIChangeTextUtil.adaptFontSize(textView, 2);
        textView.setMovementMethod(COUILinkMovementMethod.INSTANCE);
        TextView textView2 = this.exitButton;
        COUIChangeTextUtil.adaptFontSize(textView2, 4);
        textView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public final void onClick(View view) {
                COUIIndividualStatementDialog.initView$lambda$3$lambda$2(COUIIndividualStatementDialog.this, view);
            }
        });
        COUITextViewCompatUtil.setPressRippleDrawable(textView2);
        COUIButton cOUIButton = this.bottomButton;
        ViewGroup.LayoutParams layoutParams = cOUIButton.getLayoutParams();
        layoutParams.width = this.isInSmallPortrait ? cOUIButton.getContext().getResources().getDimensionPixelOffset(R.dimen.coui_component_statement_button_width) : cOUIButton.getContext().getResources().getDimensionPixelOffset(R.dimen.coui_component_statement_large_button_width);
        cOUIButton.setLayoutParams(layoutParams);
        cOUIButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public final void onClick(View view) {
                COUIIndividualStatementDialog.initView$lambda$6$lambda$5(COUIIndividualStatementDialog.this, view);
            }
        });
        this.smallLandConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public final void onClick(View view) {
                COUIIndividualStatementDialog.initView$lambda$7(COUIIndividualStatementDialog.this, view);
            }
        });
        this.smallLandExitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public final void onClick(View view) {
                COUIIndividualStatementDialog.initView$lambda$8(COUIIndividualStatementDialog.this, view);
            }
        });
        updateBottomButton(this.isInSmallLand);
    }


    public static final void initView$lambda$3$lambda$2(COUIIndividualStatementDialog this$0, View view) {
        Intrinsics.checkNotNullParameter(this$0, "this$0");
        OnButtonClickListener onButtonClickListener = this$0.onButtonClickListener;
        if (onButtonClickListener != null) {
            onButtonClickListener.onExitButtonClick();
        }
    }


    public static final void initView$lambda$6$lambda$5(COUIIndividualStatementDialog this$0, View view) {
        Intrinsics.checkNotNullParameter(this$0, "this$0");
        OnButtonClickListener onButtonClickListener = this$0.onButtonClickListener;
        if (onButtonClickListener != null) {
            onButtonClickListener.onBottomButtonClick(this$0.getCheckedFunctionList());
        }
    }


    public static final void initView$lambda$7(COUIIndividualStatementDialog this$0, View view) {
        Intrinsics.checkNotNullParameter(this$0, "this$0");
        OnButtonClickListener onButtonClickListener = this$0.onButtonClickListener;
        if (onButtonClickListener != null) {
            onButtonClickListener.onBottomButtonClick(this$0.getCheckedFunctionList());
        }
    }


    public static final void initView$lambda$8(COUIIndividualStatementDialog this$0, View view) {
        Intrinsics.checkNotNullParameter(this$0, "this$0");
        OnButtonClickListener onButtonClickListener = this$0.onButtonClickListener;
        if (onButtonClickListener != null) {
            onButtonClickListener.onExitButtonClick();
        }
    }

    private final boolean isSmallScreen(Configuration configuration) {
        return configuration.smallestScreenWidthDp < MEDIUM_LARGE_SCREEN_SW_THRESHOLD;
    }

    private final void resetBottomButton() {
        LinearLayout linearLayout = this.scrollViewLayout;
        boolean flag = false;
        if (linearLayout.getChildCount() > 1) {
            int childCount = linearLayout.getChildCount();
            for (int index = 1; index < childCount; index++) {
                View childAt = linearLayout.getChildAt(index);
                Intrinsics.checkNotNull(childAt, "null cannot be cast to non-null type com.coui.appcompat.statement.COUICheckBoxItemView");
                if (((COUICheckBoxItemView) childAt).isChecked()) {
                    flag = true;
                }
            }
        }
        this.bottomButton.setEnabled(flag);
        this.smallLandConfirmButton.setEnabled(flag);
    }

    private final void updateBottomButton(boolean flag) {
        this.exitButton.setVisibility(flag ? 8 : 0);
        this.bottomButton.setVisibility(flag ? 8 : 0);
        this.smallLandButtonLayout.setVisibility(flag ? 0 : 8);
    }

    private final void updateUI(Configuration configuration) {
        boolean isInSmallPortrait_2 = false;
        boolean isInSmallLand_2 = isSmallScreen(configuration) && !COUIPanelMultiWindowUtils.isPortrait(configuration);
        if (this.isInSmallLand != isInSmallLand_2) {
            this.isInSmallLand = isInSmallLand_2;
            updateBottomButton(isInSmallLand_2);
        }
        if (isSmallScreen(configuration) && COUIPanelMultiWindowUtils.isPortrait(configuration)) {
            isInSmallPortrait_2 = true;
        }
        if (this.isInSmallPortrait != isInSmallPortrait_2) {
            this.isInSmallPortrait = isInSmallPortrait_2;
            COUIButton cOUIButton = this.bottomButton;
            ViewGroup.LayoutParams layoutParams = cOUIButton.getLayoutParams();
            layoutParams.width = this.isInSmallPortrait ? cOUIButton.getContext().getResources().getDimensionPixelOffset(R.dimen.coui_component_statement_button_width) : cOUIButton.getContext().getResources().getDimensionPixelOffset(R.dimen.coui_component_statement_large_button_width);
            cOUIButton.setLayoutParams(layoutParams);
        }
    }

    public final void addPrivacyList(ArrayList<PrivacyItem> arrayList) {
        if (arrayList != null) {
            int index = 0;
            for (Object obj : arrayList) {
                int index_2 = index + 1;
                Context context = getContext();
                Intrinsics.checkNotNullExpressionValue(context, "context");
                COUICheckBoxItemView cOUICheckBoxItemView = new COUICheckBoxItemView(context, (PrivacyItem) obj);
                cOUICheckBoxItemView.setOnStateChangeListener(new COUICheckBox.OnStateChangeListener() {
                    @Override
                    public final void onStateChanged(COUICheckBox cOUICheckBox, int index_3) {
                        COUIIndividualStatementDialog.addPrivacyList$lambda$16$lambda$15$lambda$14(COUIIndividualStatementDialog.this, cOUICheckBox, index_3);
                    }
                });
                this.scrollViewLayout.addView(cOUICheckBoxItemView, -1, -2);
                this.bottomButton.setEnabled(false);
                this.smallLandConfirmButton.setEnabled(false);
                if (index == arrayList.size() - 1) {
                    cOUICheckBoxItemView.findViewById(R.id.checkbox_line).setVisibility(8);
                }
                index = index_2;
            }
        }
        COUIComponentMaxHeightScrollView cOUIComponentMaxHeightScrollView = this.mScrollViewComponent;
        cOUIComponentMaxHeightScrollView.setPadding(cOUIComponentMaxHeightScrollView.getPaddingLeft(), (arrayList == null || arrayList.isEmpty()) ? cOUIComponentMaxHeightScrollView.getResources().getDimensionPixelOffset(R.dimen.coui_component_individual_padding_top) : cOUIComponentMaxHeightScrollView.getResources().getDimensionPixelOffset(R.dimen.coui_component_individual_padding_top_with_checkbox), cOUIComponentMaxHeightScrollView.getPaddingRight(), cOUIComponentMaxHeightScrollView.getPaddingBottom());
        TextView textView = this.appStatement;
        textView.setTextSize((arrayList == null || arrayList.isEmpty()) ? ORIGIN_STATEMENT_TEXT_SIZE : STATEMENT_TEXT_SIZE_WITH_CHECKBOX);
        textView.setPadding(textView.getPaddingLeft(), textView.getPaddingTop(), textView.getPaddingRight(), (arrayList == null || arrayList.isEmpty()) ? textView.getResources().getDimensionPixelOffset(R.dimen.coui_component_individual_padding_bottom) : textView.getResources().getDimensionPixelOffset(R.dimen.coui_component_individual_padding_bottom_with_checkbox));
    }

    public final CharSequence getBottomButtonText() {
        return this.bottomButtonText;
    }

    public final CharSequence getExitButtonText() {
        return this.exitButtonText;
    }

    public final OnButtonClickListener getOnButtonClickListener() {
        return this.onButtonClickListener;
    }

    public final CharSequence getStatement() {
        return this.statement;
    }

    public final CharSequence getTitleText() {
        return this.titleText;
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Configuration configuration = getContext().getResources().getConfiguration();
        Intrinsics.checkNotNullExpressionValue(configuration, "context.resources.configuration");
        updateUI(configuration);
    }

    public final void setBottomButtonText(CharSequence charSequence) {
        this.bottomButtonText = charSequence;
        this.bottomButton.setText(charSequence);
        this.smallLandConfirmButton.setText(charSequence);
    }

    public final void setExitButtonText(CharSequence charSequence) {
        this.exitButtonText = charSequence;
        this.exitButton.setText(charSequence);
        this.smallLandExitButton.setText(charSequence);
    }

    public final void setOnButtonClickListener(OnButtonClickListener onButtonClickListener) {
        this.onButtonClickListener = onButtonClickListener;
    }

    public final void setStatement(CharSequence charSequence) {
        this.statement = charSequence;
        this.appStatement.setText(charSequence);
    }

    public final void setTitleText(CharSequence charSequence) {
        this.titleText = charSequence;
        this.titleView.setText(charSequence);
    }

    @Override
    public void updateLayoutWhileConfigChange(Configuration configuration) {
        Intrinsics.checkNotNullParameter(configuration, "configuration");
        super.updateLayoutWhileConfigChange(configuration);
        updateUI(configuration);
    }


    public COUIIndividualStatementDialog(Context context, int index) {
        this(context, index, 0.0f, 0.0f, 12, null);
        Intrinsics.checkNotNullParameter(context, "context");
    }

    public final void setStatement(int statement) {
        setStatement(getContext().getString(statement));
    }

    public final void setTitleText(int titleText) {
        setTitleText(getContext().getString(titleText));
    }


    public COUIIndividualStatementDialog(Context context, int index, float value) {
        this(context, index, value, 0.0f, 8, null);
        Intrinsics.checkNotNullParameter(context, "context");
    }

    public final void setBottomButtonText(int bottomButtonText) {
        setBottomButtonText(getContext().getString(bottomButtonText));
    }

    public final void setExitButtonText(int exitButtonText) {
        setExitButtonText(getContext().getString(exitButtonText));
    }

    public COUIIndividualStatementDialog(Context context, int index, float value, float value_2, int index_2, DefaultConstructorMarker defaultConstructorMarker) {
        this(context, (index_2 & 2) != 0 ? R.style.DefaultBottomSheetDialog : index, (index_2 & 4) != 0 ? Float.MIN_VALUE : value, (index_2 & 8) != 0 ? Float.MIN_VALUE : value_2);
    }


    public COUIIndividualStatementDialog(Context context, int index, float value, float value_2) {
        super(context, index, value, value_2);
        Intrinsics.checkNotNullParameter(context, "context");
        View viewInflate = LayoutInflater.from(context).inflate(R.layout.coui_component_full_page_function_privacy, (ViewGroup) null);
        View viewFindViewById = viewInflate.findViewById(R.id.txt_statement);
        Intrinsics.checkNotNullExpressionValue(viewFindViewById, "findViewById(R.id.txt_statement)");
        this.appStatement = (TextView) viewFindViewById;
        View viewFindViewById2 = viewInflate.findViewById(R.id.btn_confirm);
        Intrinsics.checkNotNullExpressionValue(viewFindViewById2, "findViewById(R.id.btn_confirm)");
        this.bottomButton = (COUIButton) viewFindViewById2;
        View viewFindViewById3 = viewInflate.findViewById(R.id.scroll_text);
        Intrinsics.checkNotNullExpressionValue(viewFindViewById3, "findViewById(R.id.scroll_text)");
        this.mScrollViewComponent = (COUIComponentMaxHeightScrollView) viewFindViewById3;
        View viewFindViewById4 = viewInflate.findViewById(R.id.layout_scroll_text);
        Intrinsics.checkNotNullExpressionValue(viewFindViewById4, "findViewById(R.id.layout_scroll_text)");
        this.scrollViewLayout = (LinearLayout) viewFindViewById4;
        View viewFindViewById5 = viewInflate.findViewById(R.id.txt_exit);
        Intrinsics.checkNotNullExpressionValue(viewFindViewById5, "findViewById(R.id.txt_exit)");
        this.exitButton = (TextView) viewFindViewById5;
        View viewFindViewById6 = viewInflate.findViewById(R.id.txt_title);
        Intrinsics.checkNotNullExpressionValue(viewFindViewById6, "findViewById(R.id.txt_title)");
        this.titleView = (TextView) viewFindViewById6;
        View viewFindViewById7 = viewInflate.findViewById(R.id.small_land_button_layout);
        Intrinsics.checkNotNullExpressionValue(viewFindViewById7, "findViewById(R.id.small_land_button_layout)");
        this.smallLandButtonLayout = (LinearLayout) viewFindViewById7;
        View viewFindViewById8 = viewInflate.findViewById(R.id.small_land_btn_confirm);
        Intrinsics.checkNotNullExpressionValue(viewFindViewById8, "findViewById(R.id.small_land_btn_confirm)");
        this.smallLandConfirmButton = (COUIButton) viewFindViewById8;
        View viewFindViewById9 = viewInflate.findViewById(R.id.small_land_btn_exit);
        Intrinsics.checkNotNullExpressionValue(viewFindViewById9, "findViewById(R.id.small_land_btn_exit)");
        this.smallLandExitButton = (COUIButton) viewFindViewById9;
        setContentView(viewInflate);
        super.setCanceledOnTouchOutside(false);
        Configuration configuration = context.getResources().getConfiguration();
        Intrinsics.checkNotNullExpressionValue(configuration, "context.resources.configuration");
        this.isInSmallLand = isSmallScreen(configuration) && !COUIPanelMultiWindowUtils.isPortrait(context);
        Configuration configuration2 = context.getResources().getConfiguration();
        Intrinsics.checkNotNullExpressionValue(configuration2, "context.resources.configuration");
        this.isInSmallPortrait = isSmallScreen(configuration2) && COUIPanelMultiWindowUtils.isPortrait(context);
        getBehavior().setDraggable(false);
        Object parent = getDragableLinearLayout().getDragView().getParent();
        Intrinsics.checkNotNull(parent, "null cannot be cast to non-null type android.view.View");
        ((View) parent).setVisibility(8);
        initView();
    }
}
