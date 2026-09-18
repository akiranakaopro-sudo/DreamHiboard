package com.coui.appcompat.statement;

import com.coui.appcompat.R;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.coui.appcompat.button.COUIButton;
import com.coui.appcompat.button.COUIButtonLayout;
import com.coui.appcompat.button.SimpleButtonGroupCtrl;
import com.coui.appcompat.button.SingleButtonWrap;
import com.coui.appcompat.contextutil.COUIContextUtil;
import com.coui.appcompat.darkmode.COUIDarkModeUtil;
import com.coui.appcompat.dialog.widget.COUIMaxHeightNestedScrollView;
import com.coui.appcompat.grid.COUIResponsiveUtils;
import com.coui.appcompat.panel.COUIBottomSheetDialog;
import com.coui.appcompat.panel.COUIPanelBarView;
import com.coui.appcompat.panel.COUIPanelMultiWindowUtils;
import com.coui.appcompat.statement.COUIStatementPanelStateChangeListener;
import com.coui.appcompat.statement.COUIUserStatementDialog;
import com.coui.appcompat.textutil.COUIChangeTextUtil;
import com.coui.appcompat.textviewcompatutil.COUITextViewCompatUtil;
import java.util.ArrayList;
import java.util.List;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;


public class COUIUserStatementDialog extends COUIBottomSheetDialog {
    public static final float BUTTON_EXTSIZE = 16.0f;
    public static final Companion Companion = new Companion(null);
    public static final float SCROLL_TEXTSIZE = 14.0f;
    public static final float TITLE_TEXTSIZE = 18.0f;
    private CharSequence appMessage;
    private CharSequence appName;
    private CharSequence bottomButtonText;
    private COUIStatementPanelStateChangeListener changeEnumUIListener;
    private final int contentPaddingEnd;
    private final int customLayoutMinHeight;
    private final int customPaddingTop;
    private View customView;
    private View customViewTiny;
    private CharSequence exitButtonText;
    private final int expandPanelMarginTop;
    private final int expandScrollPadding;
    private boolean isFullPage;
    private OnItemClickListener itemClickListener;
    private View.OnLayoutChangeListener layoutChangeListenerFromNormal;
    private View.OnLayoutChangeListener layoutChangeListenerFromSmallLand;
    private List<COUIUserStatementListItem> listItems;
    private List<ListItemViewHolder> listViewHolderArray;
    private Drawable logoDrawable;
    private COUIStatementPanelStateChangeListener.PanelStatusTypeEnum mEnumPanelStatusType;
    private final int messagePaddingTop;
    private View miniContentView;
    private MINIContentViewHolder miniContentViewHolder;
    private View normalContentView;
    private NormalContentViewHolder normalContentViewHolder;
    private int oldScreenHeightDp;
    private int oldScreenWidthDp;
    private OnButtonClickListener onButtonClickListener;
    private final int panelEndPadding;
    private final int panelPaddingTopMin;
    private final int panelStartPadding;
    private CharSequence protocolText;
    private final int scrollTextMaxHeight;
    private final int scrollTextMaxHeightNormal;
    private View smallLandContentView;
    private SmallLandContentViewHolder smallLandContentViewHolder;
    private CharSequence statement;
    private final int subTitlePaddingTop;
    private View tinyContentView;
    private TinyContentViewHolder tinyContentViewHolder;
    private CharSequence titleText;

    public static final class COUIUserStatementListItem {
        private Drawable icon;
        private String message;
        private String title;

        public COUIUserStatementListItem(Drawable drawable, String str, String str2) {
            this.icon = drawable;
            this.title = str;
            this.message = str2;
        }

        public static COUIUserStatementListItem copy$default(COUIUserStatementListItem cOUIUserStatementListItem, Drawable drawable, String str, String str2, int i2, Object obj) {
            if ((i2 & 1) != 0) {
                drawable = cOUIUserStatementListItem.icon;
            }
            if ((i2 & 2) != 0) {
                str = cOUIUserStatementListItem.title;
            }
            if ((i2 & 4) != 0) {
                str2 = cOUIUserStatementListItem.message;
            }
            return cOUIUserStatementListItem.copy(drawable, str, str2);
        }

        public final Drawable component1() {
            return this.icon;
        }

        public final String component2() {
            return this.title;
        }

        public final String component3() {
            return this.message;
        }

        public final COUIUserStatementListItem copy(Drawable drawable, String str, String str2) {
            return new COUIUserStatementListItem(drawable, str, str2);
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof COUIUserStatementListItem)) {
                return false;
            }
            COUIUserStatementListItem cOUIUserStatementListItem = (COUIUserStatementListItem) obj;
            return Intrinsics.areEqual(this.icon, cOUIUserStatementListItem.icon) && Intrinsics.areEqual(this.title, cOUIUserStatementListItem.title) && Intrinsics.areEqual(this.message, cOUIUserStatementListItem.message);
        }

        public final Drawable getIcon() {
            return this.icon;
        }

        public final String getMessage() {
            return this.message;
        }

        public final String getTitle() {
            return this.title;
        }

        public int hashCode() {
            Drawable drawable = this.icon;
            int iHashCode = (drawable == null ? 0 : drawable.hashCode()) * 31;
            String str = this.title;
            int iHashCode2 = (iHashCode + (str == null ? 0 : str.hashCode())) * 31;
            String str2 = this.message;
            return iHashCode2 + (str2 != null ? str2.hashCode() : 0);
        }

        public final void setIcon(Drawable drawable) {
            this.icon = drawable;
        }

        public final void setMessage(String str) {
            this.message = str;
        }

        public final void setTitle(String str) {
            this.title = str;
        }

        public String toString() {
            return "COUIUserStatementListItem(icon=" + this.icon + ", title=" + this.title + ", message=" + this.message + ')';
        }
    }

    public static final class Companion {
        private Companion() {
        }

        public Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }
    }

    public static class ContentViewHolder {
        private final int BTN_MAX_LINE;
        private SingleButtonWrap bottomButtonWrap;
        private COUIButton btnConfirm;
        private TextView exitButton;
        private ImageView ivLogo;
        private View llContentStatementContentChild;
        private LinearLayout llListLayout;
        private LinearLayout llStatementContentLayout;
        private RelativeLayout rlCustomLayout;
        private RelativeLayout rlCustomParentLayout;
        private COUIComponentMaxHeightScrollView scrollCustomLayout;
        private COUIComponentMaxHeightScrollView scrollText;
        private COUIComponentMaxHeightScrollView scrollTextStatementProtocol;
        private SimpleButtonGroupCtrl simpleButtonGroupCtrl;
        private COUIMaxHeightNestedScrollView slStatementContentLayout;
        private COUIButtonLayout smallLandButtonLayout;
        private COUIButton smallLandConfirmButton;
        private COUIButton smallLandExitButton;
        private TextView tvLogoMessage;
        private TextView tvLogoName;
        private TextView tvLogoSubTitle;
        private TextView tvStatementProtocol;
        private TextView txtStatement;

        public ContentViewHolder(View view, Context context) {
            Intrinsics.checkNotNullParameter(view, "view");
            Intrinsics.checkNotNullParameter(context, "context");
            this.BTN_MAX_LINE = 2;
            this.llStatementContentLayout = (LinearLayout) view.findViewById(R.id.ll_statement_content_layout);
            this.slStatementContentLayout = (COUIMaxHeightNestedScrollView) view.findViewById(R.id.sl_statement_content_layout);
            View viewInflate = LayoutInflater.from(context).inflate(R.layout.coui_component_statement_content_item, (ViewGroup) null);
            Intrinsics.checkNotNullExpressionValue(viewInflate, "from(context)\n          …ement_content_item, null)");
            this.llContentStatementContentChild = viewInflate;
            LinearLayout linearLayout = this.llStatementContentLayout;
            if (linearLayout != null) {
                linearLayout.addView(viewInflate);
            }
            if (viewInflate.getLayoutParams() != null) {
                ViewGroup.LayoutParams layoutParams = viewInflate.getLayoutParams();
                Intrinsics.checkNotNullExpressionValue(layoutParams, "layoutParams");
                layoutParams.width = -1;
                layoutParams.height = -1;
                viewInflate.setLayoutParams(layoutParams);
            }
            this.btnConfirm = (COUIButton) view.findViewById(R.id.btn_confirm);
            TextView textView = (TextView) view.findViewById(R.id.txt_exit);
            this.exitButton = textView;
            if (textView != null) {
                textView.setTextSize(2, BUTTON_EXTSIZE);
                COUIChangeTextUtil.adaptFontSize(textView, 4);
            }
            View viewFindViewById = view.findViewById(R.id.small_land_button_layout);
            Intrinsics.checkNotNullExpressionValue(viewFindViewById, "findViewById(R.id.small_land_button_layout)");
            COUIButtonLayout cOUIButtonLayout = (COUIButtonLayout) viewFindViewById;
            this.smallLandButtonLayout = cOUIButtonLayout;
            cOUIButtonLayout.setLimitHeight(true);
            this.smallLandButtonLayout.setForceSmallScreenWidth(true);
            View viewFindViewById2 = view.findViewById(R.id.small_land_btn_confirm);
            Intrinsics.checkNotNullExpressionValue(viewFindViewById2, "findViewById(R.id.small_land_btn_confirm)");
            this.smallLandConfirmButton = (COUIButton) viewFindViewById2;
            View viewFindViewById3 = view.findViewById(R.id.small_land_btn_exit);
            Intrinsics.checkNotNullExpressionValue(viewFindViewById3, "findViewById(R.id.small_land_btn_exit)");
            this.smallLandExitButton = (COUIButton) viewFindViewById3;
            View viewFindViewById4 = view.findViewById(R.id.iv_logo);
            Intrinsics.checkNotNullExpressionValue(viewFindViewById4, "findViewById(R.id.iv_logo)");
            this.ivLogo = (ImageView) viewFindViewById4;
            View viewFindViewById5 = view.findViewById(R.id.tv_logo_sub_title);
            Intrinsics.checkNotNullExpressionValue(viewFindViewById5, "findViewById(R.id.tv_logo_sub_title)");
            this.tvLogoSubTitle = (TextView) viewFindViewById5;
            View viewFindViewById6 = view.findViewById(R.id.tv_logo_name);
            Intrinsics.checkNotNullExpressionValue(viewFindViewById6, "findViewById(R.id.tv_logo_name)");
            this.tvLogoName = (TextView) viewFindViewById6;
            this.scrollCustomLayout = (COUIComponentMaxHeightScrollView) view.findViewById(R.id.scroll_custom_layout);
            View viewFindViewById7 = view.findViewById(R.id.rl_custom_layout);
            Intrinsics.checkNotNullExpressionValue(viewFindViewById7, "findViewById(R.id.rl_custom_layout)");
            this.rlCustomLayout = (RelativeLayout) viewFindViewById7;
            View viewFindViewById8 = view.findViewById(R.id.tv_logo_message);
            Intrinsics.checkNotNullExpressionValue(viewFindViewById8, "findViewById(R.id.tv_logo_message)");
            this.tvLogoMessage = (TextView) viewFindViewById8;
            View viewFindViewById9 = view.findViewById(R.id.ll_list_layout);
            Intrinsics.checkNotNullExpressionValue(viewFindViewById9, "findViewById(R.id.ll_list_layout)");
            this.llListLayout = (LinearLayout) viewFindViewById9;
            View viewFindViewById10 = view.findViewById(R.id.scroll_text);
            Intrinsics.checkNotNullExpressionValue(viewFindViewById10, "findViewById(R.id.scroll_text)");
            this.scrollText = (COUIComponentMaxHeightScrollView) viewFindViewById10;
            View viewFindViewById11 = view.findViewById(R.id.txt_statement);
            Intrinsics.checkNotNullExpressionValue(viewFindViewById11, "findViewById(R.id.txt_statement)");
            this.txtStatement = (TextView) viewFindViewById11;
            View viewFindViewById12 = view.findViewById(R.id.scroll_text_statement_protocol);
            Intrinsics.checkNotNullExpressionValue(viewFindViewById12, "findViewById(R.id.scroll_text_statement_protocol)");
            this.scrollTextStatementProtocol = (COUIComponentMaxHeightScrollView) viewFindViewById12;
            View viewFindViewById13 = view.findViewById(R.id.statement_protocol);
            Intrinsics.checkNotNullExpressionValue(viewFindViewById13, "findViewById(R.id.statement_protocol)");
            this.tvStatementProtocol = (TextView) viewFindViewById13;
            View viewFindViewById14 = view.findViewById(R.id.rl_custom_parent_layout);
            Intrinsics.checkNotNullExpressionValue(viewFindViewById14, "findViewById(R.id.rl_custom_parent_layout)");
            this.rlCustomParentLayout = (RelativeLayout) viewFindViewById14;
        }

        public final void bindBottomButtonWarp() {
            COUIButton cOUIButton = this.btnConfirm;
            this.bottomButtonWrap = cOUIButton != null ? new SingleButtonWrap(cOUIButton, 0) : null;
        }

        public final void bindCustomView(View view) {
            if (this.rlCustomLayout.getChildCount() != 0) {
                this.rlCustomLayout.removeAllViews();
            }
            if (view != null) {
                if (view.getParent() != null) {
                    ViewParent parent = view.getParent();
                    Intrinsics.checkNotNull(parent, "null cannot be cast to non-null type android.view.ViewGroup");
                    ((ViewGroup) parent).removeAllViews();
                }
                this.rlCustomLayout.addView(view);
            }
        }

        public final void bindList(List<ListItemViewHolder> list) {
            LinearLayout linearLayout = this.llListLayout;
            if (linearLayout.getChildCount() != 0) {
                linearLayout.removeAllViews();
            }
            if (list != null) {
                int size = list.size();
                for (int i2 = 0; i2 < size; i2++) {
                    ListItemViewHolder listItemViewHolder = list.get(i2);
                    if (listItemViewHolder.getMItemView().getParent() != null) {
                        ViewParent parent = listItemViewHolder.getMItemView().getParent();
                        Intrinsics.checkNotNull(parent, "null cannot be cast to non-null type android.view.ViewGroup");
                        ((ViewGroup) parent).removeAllViews();
                    }
                    this.llListLayout.addView(listItemViewHolder.getMItemView());
                }
            }
        }

        public void bindSimpleButtonGroupCtrl() {
            SimpleButtonGroupCtrl simpleButtonGroupCtrl = new SimpleButtonGroupCtrl();
            simpleButtonGroupCtrl.registerButton(this.smallLandExitButton, 3);
            simpleButtonGroupCtrl.registerButton(this.smallLandConfirmButton, 3);
            this.simpleButtonGroupCtrl = simpleButtonGroupCtrl;
        }

        public final int getBTN_MAX_LINE() {
            return this.BTN_MAX_LINE;
        }

        public final SingleButtonWrap getBottomButtonWrap() {
            return this.bottomButtonWrap;
        }

        public final COUIButton getBtnConfirm() {
            return this.btnConfirm;
        }

        public final TextView getExitButton() {
            return this.exitButton;
        }

        public final ImageView getIvLogo() {
            return this.ivLogo;
        }

        public final View getLlContentStatementContentChild() {
            return this.llContentStatementContentChild;
        }

        public final LinearLayout getLlListLayout() {
            return this.llListLayout;
        }

        public final LinearLayout getLlStatementContentLayout() {
            return this.llStatementContentLayout;
        }

        public final RelativeLayout getRlCustomLayout() {
            return this.rlCustomLayout;
        }

        public final RelativeLayout getRlCustomParentLayout() {
            return this.rlCustomParentLayout;
        }

        public final COUIComponentMaxHeightScrollView getScrollCustomLayout() {
            return this.scrollCustomLayout;
        }

        public final COUIComponentMaxHeightScrollView getScrollText() {
            return this.scrollText;
        }

        public final COUIComponentMaxHeightScrollView getScrollTextStatementProtocol() {
            return this.scrollTextStatementProtocol;
        }

        public final SimpleButtonGroupCtrl getSimpleButtonGroupCtrl() {
            return this.simpleButtonGroupCtrl;
        }

        public final COUIMaxHeightNestedScrollView getSlStatementContentLayout() {
            return this.slStatementContentLayout;
        }

        public final COUIButtonLayout getSmallLandButtonLayout() {
            return this.smallLandButtonLayout;
        }

        public final COUIButton getSmallLandConfirmButton() {
            return this.smallLandConfirmButton;
        }

        public final COUIButton getSmallLandExitButton() {
            return this.smallLandExitButton;
        }

        public final TextView getTvLogoMessage() {
            return this.tvLogoMessage;
        }

        public final TextView getTvLogoName() {
            return this.tvLogoName;
        }

        public final TextView getTvLogoSubTitle() {
            return this.tvLogoSubTitle;
        }

        public final TextView getTvStatementProtocol() {
            return this.tvStatementProtocol;
        }

        public final TextView getTxtStatement() {
            return this.txtStatement;
        }

        public void refreshBottomButtonWarp(Configuration configuration, View view) {
            Intrinsics.checkNotNullParameter(configuration, "configuration");
            Intrinsics.checkNotNullParameter(view, "view");
            COUIButton cOUIButton = this.btnConfirm;
            if (cOUIButton != null) {
                cOUIButton.setMaxLines(this.BTN_MAX_LINE);
            }
            TextView textView = this.exitButton;
            if (textView != null) {
                textView.setVisibility(!COUIResponsiveUtils.isSmallScreenDp(configuration.screenWidthDp) ? 8 : 0);
            }
            COUIButton cOUIButton2 = this.btnConfirm;
            if (cOUIButton2 != null) {
                cOUIButton2.setVisibility(COUIResponsiveUtils.isSmallScreenDp(configuration.screenWidthDp) ? 0 : 8);
            }
            SingleButtonWrap singleButtonWrap = this.bottomButtonWrap;
            if (singleButtonWrap != null) {
                singleButtonWrap.onConfigurationChanged(configuration);
            }
            TextView textView2 = this.exitButton;
            if (textView2 == null) {
                return;
            }
            textView2.setMaxWidth(view.getContext().getResources().getDimensionPixelSize(R.dimen.coui_full_page_statement_button_width));
        }

        public void refreshSimpleButtonGroupCtrl(Configuration configuration, View view) {
            Intrinsics.checkNotNullParameter(configuration, "configuration");
            Intrinsics.checkNotNullParameter(view, "view");
            COUIButton cOUIButton = this.smallLandConfirmButton;
            if (cOUIButton != null) {
                cOUIButton.setMaxLines(this.BTN_MAX_LINE);
            }
            COUIButton cOUIButton2 = this.smallLandExitButton;
            if (cOUIButton2 != null) {
                cOUIButton2.setMaxLines(this.BTN_MAX_LINE);
            }
            SimpleButtonGroupCtrl simpleButtonGroupCtrl = this.simpleButtonGroupCtrl;
            if (simpleButtonGroupCtrl != null) {
                simpleButtonGroupCtrl.onConfigurationChanged(configuration);
            }
            this.smallLandButtonLayout.setVisibility(COUIResponsiveUtils.isSmallScreenDp(configuration.screenWidthDp) ? 8 : 0);
        }

        public final void setBottomButtonWrap(SingleButtonWrap singleButtonWrap) {
            this.bottomButtonWrap = singleButtonWrap;
        }

        public final void setBtnConfirm(COUIButton cOUIButton) {
            this.btnConfirm = cOUIButton;
        }

        public final void setExitButton(TextView textView) {
            this.exitButton = textView;
        }

        public final void setIvLogo(ImageView imageView) {
            Intrinsics.checkNotNullParameter(imageView, "<set-?>");
            this.ivLogo = imageView;
        }

        public final void setLlContentStatementContentChild(View view) {
            Intrinsics.checkNotNullParameter(view, "<set-?>");
            this.llContentStatementContentChild = view;
        }

        public final void setLlListLayout(LinearLayout linearLayout) {
            Intrinsics.checkNotNullParameter(linearLayout, "<set-?>");
            this.llListLayout = linearLayout;
        }

        public final void setLlStatementContentLayout(LinearLayout linearLayout) {
            this.llStatementContentLayout = linearLayout;
        }

        public final void setRlCustomLayout(RelativeLayout relativeLayout) {
            Intrinsics.checkNotNullParameter(relativeLayout, "<set-?>");
            this.rlCustomLayout = relativeLayout;
        }

        public final void setRlCustomParentLayout(RelativeLayout relativeLayout) {
            Intrinsics.checkNotNullParameter(relativeLayout, "<set-?>");
            this.rlCustomParentLayout = relativeLayout;
        }

        public final void setScrollCustomLayout(COUIComponentMaxHeightScrollView cOUIComponentMaxHeightScrollView) {
            this.scrollCustomLayout = cOUIComponentMaxHeightScrollView;
        }

        public final void setScrollText(COUIComponentMaxHeightScrollView cOUIComponentMaxHeightScrollView) {
            Intrinsics.checkNotNullParameter(cOUIComponentMaxHeightScrollView, "<set-?>");
            this.scrollText = cOUIComponentMaxHeightScrollView;
        }

        public final void setScrollTextStatementProtocol(COUIComponentMaxHeightScrollView cOUIComponentMaxHeightScrollView) {
            Intrinsics.checkNotNullParameter(cOUIComponentMaxHeightScrollView, "<set-?>");
            this.scrollTextStatementProtocol = cOUIComponentMaxHeightScrollView;
        }

        public final void setSimpleButtonGroupCtrl(SimpleButtonGroupCtrl simpleButtonGroupCtrl) {
            this.simpleButtonGroupCtrl = simpleButtonGroupCtrl;
        }

        public final void setSlStatementContentLayout(COUIMaxHeightNestedScrollView cOUIMaxHeightNestedScrollView) {
            this.slStatementContentLayout = cOUIMaxHeightNestedScrollView;
        }

        public final void setSmallLandButtonLayout(COUIButtonLayout cOUIButtonLayout) {
            Intrinsics.checkNotNullParameter(cOUIButtonLayout, "<set-?>");
            this.smallLandButtonLayout = cOUIButtonLayout;
        }

        public final void setSmallLandConfirmButton(COUIButton cOUIButton) {
            Intrinsics.checkNotNullParameter(cOUIButton, "<set-?>");
            this.smallLandConfirmButton = cOUIButton;
        }

        public final void setSmallLandExitButton(COUIButton cOUIButton) {
            Intrinsics.checkNotNullParameter(cOUIButton, "<set-?>");
            this.smallLandExitButton = cOUIButton;
        }

        public final void setTvLogoMessage(TextView textView) {
            Intrinsics.checkNotNullParameter(textView, "<set-?>");
            this.tvLogoMessage = textView;
        }

        public final void setTvLogoName(TextView textView) {
            Intrinsics.checkNotNullParameter(textView, "<set-?>");
            this.tvLogoName = textView;
        }

        public final void setTvLogoSubTitle(TextView textView) {
            Intrinsics.checkNotNullParameter(textView, "<set-?>");
            this.tvLogoSubTitle = textView;
        }

        public final void setTvStatementProtocol(TextView textView) {
            Intrinsics.checkNotNullParameter(textView, "<set-?>");
            this.tvStatementProtocol = textView;
        }

        public final void setTxtStatement(TextView textView) {
            Intrinsics.checkNotNullParameter(textView, "<set-?>");
            this.txtStatement = textView;
        }

        public void updateLayoutUiVisibleState(List<COUIUserStatementListItem> list, View view, int i2, int i6, int i10) {
            if (view != null) {
                this.tvLogoMessage.setVisibility(8);
                this.ivLogo.setVisibility(8);
                this.rlCustomLayout.setVisibility(0);
                this.llListLayout.setVisibility(8);
                this.rlCustomParentLayout.setPadding(0, i6, 0, 0);
                this.tvLogoSubTitle.setPadding(0, 0, 0, 0);
                return;
            }
            if (list == null || list.isEmpty()) {
                this.tvLogoMessage.setVisibility(0);
                this.ivLogo.setVisibility(0);
                this.rlCustomLayout.setVisibility(8);
                this.llListLayout.setVisibility(8);
                this.rlCustomParentLayout.setPadding(0, i2, 0, 0);
                this.tvLogoSubTitle.setPadding(0, i10, 0, 0);
                return;
            }
            this.tvLogoMessage.setVisibility(8);
            this.ivLogo.setVisibility(0);
            this.rlCustomLayout.setVisibility(8);
            this.llListLayout.setVisibility(0);
            this.rlCustomParentLayout.setPadding(0, i6, 0, 0);
            this.tvLogoSubTitle.setPadding(0, i10, 0, 0);
        }
    }

    public final class ListItemViewHolder {
        private ImageView icon;
        private View mItemView;
        private int mPosition;
        private TextView message;
        final COUIUserStatementDialog this$0;
        private TextView title;

        public ListItemViewHolder(COUIUserStatementDialog cOUIUserStatementDialog, View itemView) {
            Intrinsics.checkNotNullParameter(itemView, "itemView");
            this.this$0 = cOUIUserStatementDialog;
            this.mItemView = itemView;
            View viewFindViewById = itemView.findViewById(R.id.iv_statement_list_icon);
            Intrinsics.checkNotNullExpressionValue(viewFindViewById, "itemView.findViewById(R.id.iv_statement_list_icon)");
            this.icon = (ImageView) viewFindViewById;
            View viewFindViewById2 = itemView.findViewById(R.id.tv_statement_list_title);
            Intrinsics.checkNotNullExpressionValue(viewFindViewById2, "itemView.findViewById(R.….tv_statement_list_title)");
            this.title = (TextView) viewFindViewById2;
            View viewFindViewById3 = itemView.findViewById(R.id.tv_statement_list_message);
            Intrinsics.checkNotNullExpressionValue(viewFindViewById3, "itemView.findViewById(R.…v_statement_list_message)");
            this.message = (TextView) viewFindViewById3;
        }


        public final void onItemViewClicked(OnItemClickListener onItemClickListener, View it) {
            Intrinsics.checkNotNullParameter(this, "this$0");
            if (onItemClickListener != null) {
                Intrinsics.checkNotNullExpressionValue(it, "it");
                onItemClickListener.onItemClick(it, this.mPosition);
            }
        }

        public final void bindItemData(COUIUserStatementListItem item, int i2) {
            String message;
            Intrinsics.checkNotNullParameter(item, "item");
            this.mPosition = i2;
            this.title.setText(item.getTitle());
            this.icon.setImageDrawable(item.getIcon());
            if (item.getMessage() == null || ((message = item.getMessage()) != null && message.length() == 0)) {
                this.message.setVisibility(8);
            } else {
                this.message.setText(item.getMessage());
                this.message.setVisibility(0);
            }
        }

        public final void bindListener(final OnItemClickListener onItemClickListener) {
            View view = this.mItemView;
            if (view != null) {
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public final void onClick(View view2) {
                        ListItemViewHolder.this.onItemViewClicked(onItemClickListener, view2);
                    }
                });
            }
        }

        public final ImageView getIcon() {
            return this.icon;
        }

        public final View getMItemView() {
            return this.mItemView;
        }

        public final int getMPosition() {
            return this.mPosition;
        }

        public final TextView getMessage() {
            return this.message;
        }

        public final TextView getTitle() {
            return this.title;
        }

        public final void setIcon(ImageView imageView) {
            Intrinsics.checkNotNullParameter(imageView, "<set-?>");
            this.icon = imageView;
        }

        public final void setMItemView(View view) {
            Intrinsics.checkNotNullParameter(view, "<set-?>");
            this.mItemView = view;
        }

        public final void setMPosition(int i2) {
            this.mPosition = i2;
        }

        public final void setMessage(TextView textView) {
            Intrinsics.checkNotNullParameter(textView, "<set-?>");
            this.message = textView;
        }

        public final void setTitle(TextView textView) {
            Intrinsics.checkNotNullParameter(textView, "<set-?>");
            this.title = textView;
        }
    }

    public static final class MINIContentViewHolder {
        private TextView appStatement;
        private COUIButton bottomButton;
        private SingleButtonWrap bottomButtonWrap;
        private TextView exitButton;
        private COUIComponentMaxHeightScrollView mScrollViewComponent;
        private TextView protocolStatement;
        private SimpleButtonGroupCtrl simpleButtonGroupCtrl;
        private COUIButtonLayout smallLandButtonLayout;
        private COUIButton smallLandConfirmButton;
        private COUIButton smallLandexitButton;
        private TextView titleView;

        public MINIContentViewHolder(View view) {
            Intrinsics.checkNotNullParameter(view, "view");
            View viewFindViewById = view.findViewById(R.id.txt_statement);
            Intrinsics.checkNotNullExpressionValue(viewFindViewById, "findViewById(R.id.txt_statement)");
            this.appStatement = (TextView) viewFindViewById;
            View viewFindViewById2 = view.findViewById(R.id.btn_confirm);
            Intrinsics.checkNotNullExpressionValue(viewFindViewById2, "findViewById(R.id.btn_confirm)");
            this.bottomButton = (COUIButton) viewFindViewById2;
            View viewFindViewById3 = view.findViewById(R.id.scroll_text);
            Intrinsics.checkNotNullExpressionValue(viewFindViewById3, "findViewById(R.id.scroll_text)");
            this.mScrollViewComponent = (COUIComponentMaxHeightScrollView) viewFindViewById3;
            View viewFindViewById4 = view.findViewById(R.id.txt_exit);
            Intrinsics.checkNotNullExpressionValue(viewFindViewById4, "findViewById(R.id.txt_exit)");
            this.exitButton = (TextView) viewFindViewById4;
            View viewFindViewById5 = view.findViewById(R.id.txt_title);
            Intrinsics.checkNotNullExpressionValue(viewFindViewById5, "findViewById(R.id.txt_title)");
            this.titleView = (TextView) viewFindViewById5;
            View viewFindViewById6 = view.findViewById(R.id.statement_protocol);
            Intrinsics.checkNotNullExpressionValue(viewFindViewById6, "findViewById(R.id.statement_protocol)");
            this.protocolStatement = (TextView) viewFindViewById6;
            View viewFindViewById7 = view.findViewById(R.id.small_land_button_layout);
            Intrinsics.checkNotNullExpressionValue(viewFindViewById7, "findViewById(R.id.small_land_button_layout)");
            COUIButtonLayout cOUIButtonLayout = (COUIButtonLayout) viewFindViewById7;
            this.smallLandButtonLayout = cOUIButtonLayout;
            cOUIButtonLayout.setLimitHeight(true);
            this.smallLandButtonLayout.setForceSmallScreenWidth(true);
            View viewFindViewById8 = view.findViewById(R.id.small_land_btn_confirm);
            Intrinsics.checkNotNullExpressionValue(viewFindViewById8, "findViewById(R.id.small_land_btn_confirm)");
            this.smallLandConfirmButton = (COUIButton) viewFindViewById8;
            View viewFindViewById9 = view.findViewById(R.id.small_land_btn_exit);
            Intrinsics.checkNotNullExpressionValue(viewFindViewById9, "findViewById(R.id.small_land_btn_exit)");
            this.smallLandexitButton = (COUIButton) viewFindViewById9;
        }

        private final boolean isSmallScreen(Configuration configuration) {
            return configuration.smallestScreenWidthDp < 480;
        }

        public final TextView getAppStatement() {
            return this.appStatement;
        }

        public final COUIButton getBottomButton() {
            return this.bottomButton;
        }

        public final SingleButtonWrap getBottomButtonWrap() {
            return this.bottomButtonWrap;
        }

        public final TextView getExitButton() {
            return this.exitButton;
        }

        public final COUIComponentMaxHeightScrollView getMScrollViewComponent() {
            return this.mScrollViewComponent;
        }

        public final TextView getProtocolStatement() {
            return this.protocolStatement;
        }

        public final SimpleButtonGroupCtrl getSimpleButtonGroupCtrl() {
            return this.simpleButtonGroupCtrl;
        }

        public final COUIButtonLayout getSmallLandButtonLayout() {
            return this.smallLandButtonLayout;
        }

        public final COUIButton getSmallLandConfirmButton() {
            return this.smallLandConfirmButton;
        }

        public final COUIButton getSmallLandexitButton() {
            return this.smallLandexitButton;
        }

        public final TextView getTitleView() {
            return this.titleView;
        }

        public final void refreshMINITextSize(Configuration configuration, Context context) {
            int i2;
            COUIButton cOUIButton;
            Intrinsics.checkNotNullParameter(configuration, "configuration");
            Intrinsics.checkNotNullParameter(context, "context");
            if (configuration.screenWidthDp < COUIStatementPanelStateChangeListener.Companion.getSCREN_DP_MINI_WIDTH().getValue()) {
                SimpleButtonGroupCtrl simpleButtonGroupCtrl = this.simpleButtonGroupCtrl;
                if (simpleButtonGroupCtrl != null) {
                    simpleButtonGroupCtrl.release();
                }
                SingleButtonWrap singleButtonWrap = this.bottomButtonWrap;
                if (singleButtonWrap != null) {
                    singleButtonWrap.release();
                }
                COUIButton cOUIButton2 = this.bottomButton;
                i2 = 1;
                if (cOUIButton2 != null) {
                    cOUIButton2.setTextSize(1, BUTTON_EXTSIZE);
                    COUIChangeTextUtil.adaptFontSize(cOUIButton2, 4);
                }
                COUIButton cOUIButton3 = this.smallLandexitButton;
                cOUIButton3.setTextSize(1, BUTTON_EXTSIZE);
                COUIChangeTextUtil.adaptFontSize(cOUIButton3, 4);
                COUIButton cOUIButton4 = this.smallLandConfirmButton;
                cOUIButton4.setTextSize(1, BUTTON_EXTSIZE);
                COUIChangeTextUtil.adaptFontSize(cOUIButton4, 4);
            } else {
                SimpleButtonGroupCtrl simpleButtonGroupCtrl2 = this.simpleButtonGroupCtrl;
                if (simpleButtonGroupCtrl2 != null && simpleButtonGroupCtrl2.getSingleButtonWrapListSize() == 0) {
                    SimpleButtonGroupCtrl simpleButtonGroupCtrl3 = this.simpleButtonGroupCtrl;
                    if (simpleButtonGroupCtrl3 != null) {
                        simpleButtonGroupCtrl3.registerButton(this.smallLandexitButton, 3);
                    }
                    SimpleButtonGroupCtrl simpleButtonGroupCtrl4 = this.simpleButtonGroupCtrl;
                    if (simpleButtonGroupCtrl4 != null) {
                        simpleButtonGroupCtrl4.registerButton(this.smallLandConfirmButton, 3);
                    }
                }
                SingleButtonWrap singleButtonWrap2 = this.bottomButtonWrap;
                if ((singleButtonWrap2 != null ? singleButtonWrap2.getProcessView() : null) == null && (cOUIButton = this.bottomButton) != null) {
                    this.bottomButtonWrap = new SingleButtonWrap(cOUIButton, 0);
                }
                i2 = 2;
            }
            TextView textView = this.exitButton;
            if (textView != null) {
                textView.setTextSize(i2, BUTTON_EXTSIZE);
                COUIChangeTextUtil.adaptFontSize(textView, 4);
            }
            TextView textView2 = this.titleView;
            if (textView2 != null) {
                textView2.setTextSize(i2, TITLE_TEXTSIZE);
            }
            TextView textView3 = this.appStatement;
            if (textView3 != null) {
                textView3.setTextSize(i2, SCROLL_TEXTSIZE);
            }
            TextView textView4 = this.protocolStatement;
            if (textView4 != null) {
                textView4.setTextSize(i2, SCROLL_TEXTSIZE);
            }
            TextView textView5 = this.appStatement;
            if (textView5 != null) {
                COUIChangeTextUtil.adaptFontSize(textView5, 2);
            }
            TextView textView6 = this.protocolStatement;
            if (textView6 != null) {
                COUIChangeTextUtil.adaptFontSize(textView6, 2);
            }
        }

        public final void refreshSimpleButtonGroupCtrl(Configuration configuration, Context context) {
            SimpleButtonGroupCtrl simpleButtonGroupCtrl;
            SingleButtonWrap singleButtonWrap;
            Intrinsics.checkNotNullParameter(configuration, "configuration");
            Intrinsics.checkNotNullParameter(context, "context");
            SimpleButtonGroupCtrl simpleButtonGroupCtrl2 = this.simpleButtonGroupCtrl;
            if ((simpleButtonGroupCtrl2 == null || simpleButtonGroupCtrl2.getSingleButtonWrapListSize() != 0) && (simpleButtonGroupCtrl = this.simpleButtonGroupCtrl) != null) {
                simpleButtonGroupCtrl.onConfigurationChanged(configuration);
            }
            SingleButtonWrap singleButtonWrap2 = this.bottomButtonWrap;
            if ((singleButtonWrap2 != null ? singleButtonWrap2.getProcessView() : null) != null && (singleButtonWrap = this.bottomButtonWrap) != null) {
                singleButtonWrap.onConfigurationChanged(configuration);
            }
            Configuration configuration2 = context.getResources().getConfiguration();
            Intrinsics.checkNotNullExpressionValue(configuration2, "context.resources.configuration");
            boolean z6 = isSmallScreen(configuration2) && !COUIPanelMultiWindowUtils.isPortrait(context);
            this.exitButton.setVisibility(z6 ? 8 : 0);
            this.bottomButton.setVisibility(z6 ? 8 : 0);
            this.smallLandButtonLayout.setVisibility(z6 ? 0 : 8);
        }

        public final void setAppStatement(TextView textView) {
            Intrinsics.checkNotNullParameter(textView, "<set-?>");
            this.appStatement = textView;
        }

        public final void setBottomButton(COUIButton cOUIButton) {
            Intrinsics.checkNotNullParameter(cOUIButton, "<set-?>");
            this.bottomButton = cOUIButton;
        }

        public final void setBottomButtonWrap(SingleButtonWrap singleButtonWrap) {
            this.bottomButtonWrap = singleButtonWrap;
        }

        public final void setExitButton(TextView textView) {
            Intrinsics.checkNotNullParameter(textView, "<set-?>");
            this.exitButton = textView;
        }

        public final void setMScrollViewComponent(COUIComponentMaxHeightScrollView cOUIComponentMaxHeightScrollView) {
            Intrinsics.checkNotNullParameter(cOUIComponentMaxHeightScrollView, "<set-?>");
            this.mScrollViewComponent = cOUIComponentMaxHeightScrollView;
        }

        public final void setProtocolStatement(TextView textView) {
            Intrinsics.checkNotNullParameter(textView, "<set-?>");
            this.protocolStatement = textView;
        }

        public final void setSimpleButtonGroupCtrl(SimpleButtonGroupCtrl simpleButtonGroupCtrl) {
            this.simpleButtonGroupCtrl = simpleButtonGroupCtrl;
        }

        public final void setSmallLandButtonLayout(COUIButtonLayout cOUIButtonLayout) {
            Intrinsics.checkNotNullParameter(cOUIButtonLayout, "<set-?>");
            this.smallLandButtonLayout = cOUIButtonLayout;
        }

        public final void setSmallLandConfirmButton(COUIButton cOUIButton) {
            Intrinsics.checkNotNullParameter(cOUIButton, "<set-?>");
            this.smallLandConfirmButton = cOUIButton;
        }

        public final void setSmallLandexitButton(COUIButton cOUIButton) {
            Intrinsics.checkNotNullParameter(cOUIButton, "<set-?>");
            this.smallLandexitButton = cOUIButton;
        }

        public final void setTitleView(TextView textView) {
            Intrinsics.checkNotNullParameter(textView, "<set-?>");
            this.titleView = textView;
        }
    }

    public static final class NormalContentViewHolder extends ContentViewHolder {

        public NormalContentViewHolder(View view, Context context) {
            super(view, context);
            Intrinsics.checkNotNullParameter(view, "view");
            Intrinsics.checkNotNullParameter(context, "context");
        }

        private final boolean isLargeScreenLayoutSize(Context context) {
            return COUIResponsiveUtils.isLargeScreenDp(context.getResources().getConfiguration().screenHeightDp) && COUIResponsiveUtils.isMediumScreenDp(context.getResources().getConfiguration().screenWidthDp);
        }

        public final void updateExpandScrollUI(int i2) {
            int layoutDirection = getLlContentStatementContentChild().getContext().getResources().getConfiguration().getLayoutDirection();
            LinearLayout llStatementContentLayout = getLlStatementContentLayout();
            if (llStatementContentLayout != null) {
                llStatementContentLayout.removeAllViews();
            }
            COUIMaxHeightNestedScrollView slStatementContentLayout = getSlStatementContentLayout();
            if (slStatementContentLayout != null && slStatementContentLayout.getChildCount() == 0) {
                View llContentStatementContentChild = getLlContentStatementContentChild();
                COUIMaxHeightNestedScrollView slStatementContentLayout2 = getSlStatementContentLayout();
                if (slStatementContentLayout2 != null) {
                    slStatementContentLayout2.addView(llContentStatementContentChild);
                }
                if (llContentStatementContentChild.getLayoutParams() != null) {
                    ViewGroup.LayoutParams layoutParams = llContentStatementContentChild.getLayoutParams();
                    Intrinsics.checkNotNullExpressionValue(layoutParams, "layoutParams");
                    layoutParams.width = -1;
                    layoutParams.height = -2;
                    llContentStatementContentChild.setLayoutParams(layoutParams);
                }
            }
            getLlContentStatementContentChild().setLayoutDirection(layoutDirection);
            COUIComponentMaxHeightScrollView scrollCustomLayout = getScrollCustomLayout();
            if (scrollCustomLayout != null) {
                scrollCustomLayout.setMinHeight(i2);
            }
            COUIComponentMaxHeightScrollView scrollCustomLayout2 = getScrollCustomLayout();
            if (scrollCustomLayout2 != null) {
                scrollCustomLayout2.setMaxHeight(-1);
            }
            COUIMaxHeightNestedScrollView slStatementContentLayout3 = getSlStatementContentLayout();
            if (slStatementContentLayout3 != null) {
                slStatementContentLayout3.setVisibility(0);
            }
            LinearLayout llStatementContentLayout2 = getLlStatementContentLayout();
            if (llStatementContentLayout2 == null) {
                return;
            }
            llStatementContentLayout2.setVisibility(8);
        }

        public final void updateLogoPaddingTop(List<COUIUserStatementListItem> list, View view, int i2, int i6, int i10, Context context, int i11, int i12, int i13, int i14, boolean z6, boolean z10) {
            Intrinsics.checkNotNullParameter(context, "context");
            Integer numValueOf = null;
            if (COUIResponsiveUtils.isSmallScreenDp(context.getResources().getConfiguration().screenWidthDp)) {
                Resources resources = context.getResources();
                if (resources != null) {
                    numValueOf = Integer.valueOf(resources.getDimensionPixelSize(R.dimen.coui_component_statement_margin_top_small_screen_max));
                }
            } else {
                Resources resources2 = context.getResources();
                if (resources2 != null) {
                    numValueOf = Integer.valueOf(resources2.getDimensionPixelSize(R.dimen.coui_component_statement_margin_top_big_screen_max));
                }
            }
            if (view == null && ((list == null || list.isEmpty()) && !z10 && !z6 && numValueOf != null)) {
                i2 = numValueOf.intValue();
            }
            getLlContentStatementContentChild().setPaddingRelative(0, i2, i11, 0);
            COUIMaxHeightNestedScrollView slStatementContentLayout = getSlStatementContentLayout();
            if (slStatementContentLayout != null) {
                slStatementContentLayout.setPaddingRelative(i6, 0, i10, 0);
            }
        }

        public final void updateNormalFoldingScrollUI() {
            COUIMaxHeightNestedScrollView slStatementContentLayout = getSlStatementContentLayout();
            if (slStatementContentLayout != null) {
                slStatementContentLayout.removeAllViews();
            }
            LinearLayout llStatementContentLayout = getLlStatementContentLayout();
            if (llStatementContentLayout != null && llStatementContentLayout.getChildCount() == 0) {
                View llContentStatementContentChild = getLlContentStatementContentChild();
                LinearLayout llStatementContentLayout2 = getLlStatementContentLayout();
                if (llStatementContentLayout2 != null) {
                    llStatementContentLayout2.addView(llContentStatementContentChild);
                }
                if (llContentStatementContentChild.getLayoutParams() != null) {
                    ViewGroup.LayoutParams layoutParams = llContentStatementContentChild.getLayoutParams();
                    Intrinsics.checkNotNullExpressionValue(layoutParams, "layoutParams");
                    layoutParams.width = -1;
                    layoutParams.height = -1;
                    llContentStatementContentChild.setLayoutParams(layoutParams);
                }
            }
            COUIComponentMaxHeightScrollView scrollCustomLayout = getScrollCustomLayout();
            if (scrollCustomLayout != null) {
                scrollCustomLayout.setMinHeight(0);
            }
            COUIComponentMaxHeightScrollView scrollCustomLayout2 = getScrollCustomLayout();
            if (scrollCustomLayout2 != null) {
                scrollCustomLayout2.setMaxHeight(-1);
            }
            COUIMaxHeightNestedScrollView slStatementContentLayout2 = getSlStatementContentLayout();
            if (slStatementContentLayout2 != null) {
                slStatementContentLayout2.setVisibility(8);
            }
            LinearLayout llStatementContentLayout3 = getLlStatementContentLayout();
            if (llStatementContentLayout3 == null) {
                return;
            }
            llStatementContentLayout3.setVisibility(0);
        }

        public final void updateScrollTextMaxHeight(List<COUIUserStatementListItem> list, View view, Configuration configuration, int i2, int i6, Context context) {
            Intrinsics.checkNotNullParameter(configuration, "configuration");
            Intrinsics.checkNotNullParameter(context, "context");
            if (view == null && ((list == null || list.isEmpty()) && COUIResponsiveUtils.isSmallScreenDp(configuration.screenWidthDp))) {
                getScrollText().setMaxHeight(i6);
            } else {
                getScrollText().setMaxHeight(i2);
            }
            getScrollTextStatementProtocol().setMaxHeight(context.getResources().getDimensionPixelSize(R.dimen.coui_component_statement_scroll_text_statement_protocol_max_height));
        }

        public final void updateSplitScreenLogoPaddingTop(int i2, int i6, int i10, int i11, int i12, int i13) {
            getLlContentStatementContentChild().setPaddingRelative(0, i2, i13, 0);
            COUIMaxHeightNestedScrollView slStatementContentLayout = getSlStatementContentLayout();
            if (slStatementContentLayout != null) {
                slStatementContentLayout.setPaddingRelative(i11, i6, i12, i10);
            }
        }

        public final void updateSplitScreenScrollTextMaxHeight() {
            getScrollText().setMaxHeight(-1);
            COUIComponentMaxHeightScrollView scrollTextStatementProtocol = getScrollTextStatementProtocol();
            if (scrollTextStatementProtocol != null) {
                scrollTextStatementProtocol.setMaxHeight(-1);
            }
        }
    }

    public interface OnButtonClickListener {
        void onBottomButtonClick();

        void onExitButtonClick();
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int i2);
    }

    public static final class SmallLandContentViewHolder extends ContentViewHolder {

        public SmallLandContentViewHolder(View view, Context context) {
            super(view, context);
            Intrinsics.checkNotNullParameter(view, "view");
            Intrinsics.checkNotNullParameter(context, "context");
        }

        @Override
        public void bindSimpleButtonGroupCtrl() {
            COUIButton smallLandConfirmButton = getSmallLandConfirmButton();
            smallLandConfirmButton.setTextSize(1, 16.0f);
            COUIChangeTextUtil.adaptFontSize(smallLandConfirmButton, 4);
            COUIButton smallLandExitButton = getSmallLandExitButton();
            smallLandExitButton.setTextSize(1, 16.0f);
            COUIChangeTextUtil.adaptFontSize(smallLandExitButton, 4);
        }

        @Override
        public void refreshSimpleButtonGroupCtrl(Configuration configuration, View view) {
            Intrinsics.checkNotNullParameter(configuration, "configuration");
            Intrinsics.checkNotNullParameter(view, "view");
        }
    }

    public static final class TinyContentViewHolder {
        private TextView appStatementTiny;
        private COUIButton btnConfirmTiny;
        private COUIButton btnExitTiny;
        private LinearLayoutCompat customFunctionalAreaTiny;
        private LinearLayoutCompat customFunctionalAreaWrapperTiny;
        private RelativeLayout rlTextTiny;
        private ScrollView scrollButtonTiny;
        private TextView titleTiny;

        public TinyContentViewHolder(View view) {
            Intrinsics.checkNotNullParameter(view, "view");
            View viewFindViewById = view.findViewById(R.id.rl_text_tiny);
            Intrinsics.checkNotNullExpressionValue(viewFindViewById, "findViewById(R.id.rl_text_tiny)");
            this.rlTextTiny = (RelativeLayout) viewFindViewById;
            View viewFindViewById2 = view.findViewById(R.id.txt_title_tiny);
            Intrinsics.checkNotNullExpressionValue(viewFindViewById2, "findViewById(R.id.txt_title_tiny)");
            this.titleTiny = (TextView) viewFindViewById2;
            View viewFindViewById3 = view.findViewById(R.id.txt_statement_tiny);
            Intrinsics.checkNotNullExpressionValue(viewFindViewById3, "findViewById(R.id.txt_statement_tiny)");
            this.appStatementTiny = (TextView) viewFindViewById3;
            View viewFindViewById4 = view.findViewById(R.id.scroll_button_tiny);
            Intrinsics.checkNotNullExpressionValue(viewFindViewById4, "findViewById(R.id.scroll_button_tiny)");
            this.scrollButtonTiny = (ScrollView) viewFindViewById4;
            View viewFindViewById5 = view.findViewById(R.id.btn_confirm_tiny);
            Intrinsics.checkNotNullExpressionValue(viewFindViewById5, "findViewById(R.id.btn_confirm_tiny)");
            this.btnConfirmTiny = (COUIButton) viewFindViewById5;
            View viewFindViewById6 = view.findViewById(R.id.txt_exit_tiny);
            Intrinsics.checkNotNullExpressionValue(viewFindViewById6, "findViewById(R.id.txt_exit_tiny)");
            this.btnExitTiny = (COUIButton) viewFindViewById6;
            View viewFindViewById7 = view.findViewById(R.id.custom_functional_area_wrapper);
            Intrinsics.checkNotNullExpressionValue(viewFindViewById7, "findViewById(R.id.custom_functional_area_wrapper)");
            this.customFunctionalAreaWrapperTiny = (LinearLayoutCompat) viewFindViewById7;
            View viewFindViewById8 = view.findViewById(R.id.custom_functional_area);
            Intrinsics.checkNotNullExpressionValue(viewFindViewById8, "findViewById(R.id.custom_functional_area)");
            this.customFunctionalAreaTiny = (LinearLayoutCompat) viewFindViewById8;
        }

        public final TextView getAppStatementTiny() {
            return this.appStatementTiny;
        }

        public final COUIButton getBtnConfirmTiny() {
            return this.btnConfirmTiny;
        }

        public final COUIButton getBtnExitTiny() {
            return this.btnExitTiny;
        }

        public final LinearLayoutCompat getCustomFunctionalAreaTiny() {
            return this.customFunctionalAreaTiny;
        }

        public final LinearLayoutCompat getCustomFunctionalAreaWrapperTiny() {
            return this.customFunctionalAreaWrapperTiny;
        }

        public final RelativeLayout getRlTextTiny() {
            return this.rlTextTiny;
        }

        public final ScrollView getScrollButtonTiny() {
            return this.scrollButtonTiny;
        }

        public final TextView getTitleTiny() {
            return this.titleTiny;
        }

        public final void refreshTinyContentViewHolder(View view) {
            if (view != null) {
                this.customFunctionalAreaWrapperTiny.setVisibility(0);
            } else {
                this.customFunctionalAreaWrapperTiny.setVisibility(8);
            }
        }

        public final void setAppStatementTiny(TextView textView) {
            Intrinsics.checkNotNullParameter(textView, "<set-?>");
            this.appStatementTiny = textView;
        }

        public final void setBtnConfirmTiny(COUIButton cOUIButton) {
            Intrinsics.checkNotNullParameter(cOUIButton, "<set-?>");
            this.btnConfirmTiny = cOUIButton;
        }

        public final void setBtnExitTiny(COUIButton cOUIButton) {
            Intrinsics.checkNotNullParameter(cOUIButton, "<set-?>");
            this.btnExitTiny = cOUIButton;
        }

        public final void setCustomFunctionalAreaTiny(LinearLayoutCompat linearLayoutCompat) {
            Intrinsics.checkNotNullParameter(linearLayoutCompat, "<set-?>");
            this.customFunctionalAreaTiny = linearLayoutCompat;
        }

        public final void setCustomFunctionalAreaWrapperTiny(LinearLayoutCompat linearLayoutCompat) {
            Intrinsics.checkNotNullParameter(linearLayoutCompat, "<set-?>");
            this.customFunctionalAreaWrapperTiny = linearLayoutCompat;
        }

        public final void setRlTextTiny(RelativeLayout relativeLayout) {
            Intrinsics.checkNotNullParameter(relativeLayout, "<set-?>");
            this.rlTextTiny = relativeLayout;
        }

        public final void setScrollButtonTiny(ScrollView scrollView) {
            Intrinsics.checkNotNullParameter(scrollView, "<set-?>");
            this.scrollButtonTiny = scrollView;
        }

        public final void setTitleTiny(TextView textView) {
            Intrinsics.checkNotNullParameter(textView, "<set-?>");
            this.titleTiny = textView;
        }
    }

    public static class WhenMappings {
        public static final int[] $EnumSwitchMapping$0;

        static {
            int[] iArr = new int[COUIStatementPanelStateChangeListener.PanelStatusTypeEnum.values().length];
            try {
                iArr[COUIStatementPanelStateChangeListener.PanelStatusTypeEnum.NORMAL.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                iArr[COUIStatementPanelStateChangeListener.PanelStatusTypeEnum.SMALL_LAND.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                iArr[COUIStatementPanelStateChangeListener.PanelStatusTypeEnum.SPLIT_SCREEN.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                iArr[COUIStatementPanelStateChangeListener.PanelStatusTypeEnum.TINY.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            $EnumSwitchMapping$0 = iArr;
        }
    }


    public COUIUserStatementDialog(Context context) {
        this(context, 0, 0.0f, 0.0f, 14, null);
        Intrinsics.checkNotNullParameter(context, "context");
    }


    public final void addOnLayoutChangeListenerToScrollText(CharSequence charSequence, CharSequence charSequence2, ContentViewHolder contentViewHolder) {
        contentViewHolder.getTxtStatement().setText(charSequence);
        contentViewHolder.getTvStatementProtocol().setText("");
        if (!TextUtils.isEmpty(charSequence2)) {
            contentViewHolder.getTxtStatement().append(charSequence2);
        }
        if ((contentViewHolder instanceof NormalContentViewHolder) && this.layoutChangeListenerFromNormal == null) {
            this.layoutChangeListenerFromNormal = initLayoutChangeListener(charSequence, charSequence2, contentViewHolder);
            contentViewHolder.getScrollText().addOnLayoutChangeListener(this.layoutChangeListenerFromNormal);
        }
        if ((contentViewHolder instanceof SmallLandContentViewHolder) && this.layoutChangeListenerFromSmallLand == null) {
            this.layoutChangeListenerFromSmallLand = initLayoutChangeListener(charSequence, charSequence2, contentViewHolder);
            contentViewHolder.getScrollText().addOnLayoutChangeListener(this.layoutChangeListenerFromSmallLand);
        }
    }

    private final void changePanelState(COUIStatementPanelStateChangeListener.PanelStatusTypeEnum panelStatusTypeEnum, Configuration configuration) {
        int i2 = WhenMappings.$EnumSwitchMapping$0[panelStatusTypeEnum.ordinal()];
        if (i2 == 1) {
            this.changeEnumUIListener.initNormalContentView(configuration, this.mEnumPanelStatusType);
            this.changeEnumUIListener.updateNormalContentView(configuration);
        } else if (i2 == 2) {
            this.changeEnumUIListener.initSmallLandContentView(configuration, this.mEnumPanelStatusType);
            this.changeEnumUIListener.updateSmallLandContentView(configuration);
        } else if (i2 == 3) {
            this.changeEnumUIListener.initSplitScreenContentView(configuration, this.mEnumPanelStatusType);
            this.changeEnumUIListener.updateSplitScreenContentView(configuration);
        } else if (i2 != 4) {
            this.changeEnumUIListener.initMINIContentView(configuration, this.mEnumPanelStatusType);
            this.changeEnumUIListener.updateMINIContentView(configuration);
        } else {
            this.changeEnumUIListener.initTinyContentView(configuration, this.mEnumPanelStatusType);
            this.changeEnumUIListener.updateTinyContentView(configuration);
        }
        this.mEnumPanelStatusType = panelStatusTypeEnum;
    }

    private final void checkPanelStatus(Configuration configuration) {
        if (COUIResponsiveUtils.isSmallScreenDp(configuration.screenWidthDp) && configuration.screenWidthDp < COUIStatementPanelStateChangeListener.Companion.getSCREN_DP_MINI_WIDTH().getValue()) {
            changePanelState(COUIStatementPanelStateChangeListener.PanelStatusTypeEnum.TINY, configuration);
            return;
        }
        if (!this.isFullPage) {
            changePanelState(COUIStatementPanelStateChangeListener.PanelStatusTypeEnum.MINI, configuration);
            return;
        }
        if (!COUIPanelMultiWindowUtils.isInMultiWindowMode(COUIPanelMultiWindowUtils.contextToActivity(getContext())) && configuration.orientation == 2) {
            int i2 = configuration.screenLayout;
            if ((i2 & 15) == 2 && (i2 & 48) == 32) {
                super.setIsShowInMaxHeight(this.isFullPage);
                setIsInTinyScreen(false, false);
                changePanelState(COUIStatementPanelStateChangeListener.PanelStatusTypeEnum.SMALL_LAND, configuration);
                return;
            }
        }
        if (configuration.screenHeightDp <= COUIStatementPanelStateChangeListener.Companion.getSCREN_DP_SPLIT_HEIGHT().getValue()) {
            super.setIsShowInMaxHeight(this.isFullPage);
            setIsInTinyScreen(false, false);
            changePanelState(COUIStatementPanelStateChangeListener.PanelStatusTypeEnum.SPLIT_SCREEN, configuration);
        } else {
            super.setIsShowInMaxHeight(this.isFullPage);
            setIsInTinyScreen(false, false);
            changePanelState(COUIStatementPanelStateChangeListener.PanelStatusTypeEnum.NORMAL, configuration);
        }
    }

    private final int getPanelHeight() {
        View viewFindViewById;
        Window window = getWindow();
        if (window == null || (viewFindViewById = window.findViewById(com.google.android.material.R.id.design_bottom_sheet)) == null) {
            return 0;
        }
        return viewFindViewById.getHeight();
    }

    private final int getPanelWidth() {
        View viewFindViewById;
        Window window = getWindow();
        if (window == null || (viewFindViewById = window.findViewById(com.google.android.material.R.id.design_bottom_sheet)) == null) {
            return 0;
        }
        return viewFindViewById.getWidth();
    }

    private final View.OnLayoutChangeListener initLayoutChangeListener(final CharSequence charSequence, final CharSequence charSequence2, final ContentViewHolder contentViewHolder) {
        return new View.OnLayoutChangeListener() {
            @Override
            public final void onLayoutChange(View view, int i2, int i6, int i10, int i11, int i12, int i13, int i14, int i15) {
                COUIUserStatementDialog.onStatementLayoutChanged(contentViewHolder, COUIUserStatementDialog.this, charSequence, charSequence2, view, i2, i6, i10, i11, i12, i13, i14, i15);
            }
        };
    }


    public static final void onStatementLayoutChanged(final ContentViewHolder viewHolder, final COUIUserStatementDialog this$0, final CharSequence charSequence, final CharSequence charSequence2, View view, int i2, int i6, int i10, int i11, int i12, int i13, int i14, int i15) {
        Intrinsics.checkNotNullParameter(viewHolder, "$viewHolder");
        Intrinsics.checkNotNullParameter(this$0, "this$0");
        view.post(new Runnable() {
            @Override
            public final void run() {
                COUIUserStatementDialog.applyStatementLayoutAfterChange(viewHolder, this$0, charSequence, charSequence2);
            }
        });
    }


    public static final void applyStatementLayoutAfterChange(ContentViewHolder viewHolder, COUIUserStatementDialog this$0, CharSequence charSequence, CharSequence charSequence2) {
        NormalContentViewHolder normalContentViewHolder;
        Intrinsics.checkNotNullParameter(viewHolder, "$viewHolder");
        Intrinsics.checkNotNullParameter(this$0, "this$0");
        if (viewHolder instanceof SmallLandContentViewHolder) {
            viewHolder.getScrollText().setVisibility(TextUtils.isEmpty(viewHolder.getTxtStatement().getText()) ? 8 : 0);
            return;
        }
        int measuredHeight = TextUtils.isEmpty(viewHolder.getTvStatementProtocol().getText()) ? 0 : viewHolder.getTvStatementProtocol().getMeasuredHeight();
        int panelWidth = ((this$0.getPanelWidth() - this$0.panelStartPadding) - this$0.panelEndPadding) - this$0.contentPaddingEnd;
        CharSequence text = viewHolder.getTvLogoName().getText();
        if (text == null) {
            text = "";
        }
        float fMeasureText = viewHolder.getTvLogoName().getPaint().measureText(text.toString());
        float value = (COUIResponsiveUtils.isSmallScreenDp(this$0.getContext().getResources().getConfiguration().screenWidthDp) ? COUIStatementPanelStateChangeListener.Companion.getSCREN_DP_DEFAULT_HEIGHT() : COUIStatementPanelStateChangeListener.Companion.getSCREN_DP_SPLIT_HEIGHT()).getValue();
        boolean z6 = ((float) panelWidth) < fMeasureText;
        boolean z10 = ((float) this$0.getPanelHeight()) / this$0.getContext().getResources().getDisplayMetrics().density < value;
        if (viewHolder.getTxtStatement().getMeasuredHeight() + measuredHeight < viewHolder.getScrollText().getMaxHeight() || viewHolder.getScrollText().getMaxHeight() <= 0 || (COUIResponsiveUtils.isSmallScreenDp(this$0.getContext().getResources().getConfiguration().screenWidthDp) && z6 && z10)) {
            viewHolder.getScrollText().setVisibility(TextUtils.isEmpty(viewHolder.getTxtStatement().getText()) ? 8 : 0);
            viewHolder.getScrollTextStatementProtocol().setVisibility(TextUtils.isEmpty(viewHolder.getTvStatementProtocol().getText()) ? 8 : 0);
        } else {
            viewHolder.getTxtStatement().setText(charSequence);
            viewHolder.getTvStatementProtocol().setText(charSequence2);
            viewHolder.getScrollText().setVisibility(TextUtils.isEmpty(charSequence) ? 8 : 0);
            viewHolder.getScrollTextStatementProtocol().setVisibility(TextUtils.isEmpty(charSequence2) ? 8 : 0);
        }
        if (!(viewHolder instanceof NormalContentViewHolder) || this$0.mEnumPanelStatusType == COUIStatementPanelStateChangeListener.PanelStatusTypeEnum.SPLIT_SCREEN || (normalContentViewHolder = this$0.normalContentViewHolder) == null) {
            return;
        }
        List<COUIUserStatementListItem> list = this$0.listItems;
        View view = this$0.customView;
        int i2 = this$0.panelPaddingTopMin;
        int i6 = this$0.panelStartPadding;
        int i10 = this$0.panelEndPadding;
        Context context = this$0.getContext();
        Intrinsics.checkNotNullExpressionValue(context, "context");
        normalContentViewHolder.updateLogoPaddingTop(list, view, i2, i6, i10, context, this$0.contentPaddingEnd, this$0.mCoordinatorLayoutMinInsetsTop, this$0.getPanelHeight(), this$0.getPanelWidth(), z6, z10);
    }


    public final void initMINIView(MINIContentViewHolder mINIContentViewHolder) {
        TextView appStatement = mINIContentViewHolder.getAppStatement();
        COUIDarkModeUtil.setForceDarkAllow(appStatement, false);
        Context context = appStatement.getContext();
        int i2 = R.attr.couiColorSecondNeutral;
        appStatement.setTextColor(COUIContextUtil.getAttrColor(context, i2));
        COUIChangeTextUtil.adaptFontSize(appStatement, 2);
        COUILinkMovementMethod cOUILinkMovementMethod = COUILinkMovementMethod.INSTANCE;
        appStatement.setMovementMethod(cOUILinkMovementMethod);
        TextView protocolStatement = mINIContentViewHolder.getProtocolStatement();
        if (protocolStatement != null) {
            COUIDarkModeUtil.setForceDarkAllow(protocolStatement, false);
            protocolStatement.setVisibility(0);
            protocolStatement.setTextColor(COUIContextUtil.getAttrColor(protocolStatement.getContext(), i2));
            COUIChangeTextUtil.adaptFontSize(protocolStatement, 2);
            protocolStatement.setMovementMethod(cOUILinkMovementMethod);
        }
        COUIComponentMaxHeightScrollView mScrollViewComponent = mINIContentViewHolder.getMScrollViewComponent();
        if (mScrollViewComponent != null) {
            TextView protocolStatement2 = mINIContentViewHolder.getProtocolStatement();
            if (protocolStatement2 != null) {
                protocolStatement2.measure(View.MeasureSpec.makeMeasureSpec(0, 0), View.MeasureSpec.makeMeasureSpec(0, 0));
            }
            mScrollViewComponent.setMaxHeight((mScrollViewComponent.getContext().getResources().getDimensionPixelOffset(R.dimen.coui_component_statement_max_height) - mScrollViewComponent.getMeasuredHeight()) - mScrollViewComponent.getPaddingTop());
            COUIComponentMaxHeightScrollView mScrollViewComponent2 = mINIContentViewHolder.getMScrollViewComponent();
            if (mScrollViewComponent2 != null) {
                mScrollViewComponent2.setProtocolFixed(true);
            }
        }
        TextView exitButton = mINIContentViewHolder.getExitButton();
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public final void onClick(View view) {
                COUIUserStatementDialog.onMiniExitButtonClicked(COUIUserStatementDialog.this, view);
            }
        });
        COUITextViewCompatUtil.setPressRippleDrawable(exitButton);
        COUIButton bottomButton = mINIContentViewHolder.getBottomButton();
        bottomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public final void onClick(View view) {
                COUIUserStatementDialog.onMiniConfirmButtonClicked(COUIUserStatementDialog.this, view);
            }
        });
        mINIContentViewHolder.setBottomButtonWrap(new SingleButtonWrap(bottomButton, 0));
        mINIContentViewHolder.getSmallLandexitButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public final void onClick(View view) {
                COUIUserStatementDialog.onMiniAltExitButtonClicked(COUIUserStatementDialog.this, view);
            }
        });
        mINIContentViewHolder.getSmallLandConfirmButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public final void onClick(View view) {
                COUIUserStatementDialog.onMiniAltConfirmButtonClicked(COUIUserStatementDialog.this, view);
            }
        });
        mINIContentViewHolder.getTitleView().setText(this.titleText);
        mINIContentViewHolder.getBottomButton().setText(this.bottomButtonText);
        mINIContentViewHolder.getExitButton().setText(this.exitButtonText);
        mINIContentViewHolder.getAppStatement().setText(this.statement);
        mINIContentViewHolder.getProtocolStatement().setText(this.protocolText);
        mINIContentViewHolder.getSmallLandConfirmButton().setText(this.bottomButtonText);
        mINIContentViewHolder.getSmallLandexitButton().setText(this.exitButtonText);
        SimpleButtonGroupCtrl simpleButtonGroupCtrl = new SimpleButtonGroupCtrl();
        simpleButtonGroupCtrl.registerButton(mINIContentViewHolder.getSmallLandexitButton(), 3);
        simpleButtonGroupCtrl.registerButton(mINIContentViewHolder.getSmallLandConfirmButton(), 3);
        mINIContentViewHolder.setSimpleButtonGroupCtrl(simpleButtonGroupCtrl);
    }


    public static final void onMiniExitButtonClicked(COUIUserStatementDialog this$0, View view) {
        Intrinsics.checkNotNullParameter(this$0, "this$0");
        OnButtonClickListener onButtonClickListener = this$0.onButtonClickListener;
        if (onButtonClickListener != null) {
            onButtonClickListener.onExitButtonClick();
        }
    }


    public static final void onMiniConfirmButtonClicked(COUIUserStatementDialog this$0, View view) {
        Intrinsics.checkNotNullParameter(this$0, "this$0");
        OnButtonClickListener onButtonClickListener = this$0.onButtonClickListener;
        if (onButtonClickListener != null) {
            onButtonClickListener.onBottomButtonClick();
        }
    }


    public static final void onMiniAltExitButtonClicked(COUIUserStatementDialog this$0, View view) {
        Intrinsics.checkNotNullParameter(this$0, "this$0");
        OnButtonClickListener onButtonClickListener = this$0.onButtonClickListener;
        if (onButtonClickListener != null) {
            onButtonClickListener.onExitButtonClick();
        }
    }


    public static final void onMiniAltConfirmButtonClicked(COUIUserStatementDialog this$0, View view) {
        Intrinsics.checkNotNullParameter(this$0, "this$0");
        OnButtonClickListener onButtonClickListener = this$0.onButtonClickListener;
        if (onButtonClickListener != null) {
            onButtonClickListener.onBottomButtonClick();
        }
    }


    public final void initNormalView() {
        NormalContentViewHolder normalContentViewHolder = this.normalContentViewHolder;
        if (normalContentViewHolder != null) {
            initViewHolderBind(normalContentViewHolder);
        }
    }


    public final void initSmallLandView() {
        SmallLandContentViewHolder smallLandContentViewHolder = this.smallLandContentViewHolder;
        if (smallLandContentViewHolder != null) {
            initViewHolderBind(smallLandContentViewHolder);
        }
    }


    public final void initTinyView(TinyContentViewHolder tinyContentViewHolder) {
        TextView appStatementTiny = tinyContentViewHolder.getAppStatementTiny();
        COUIDarkModeUtil.setForceDarkAllow(appStatementTiny, false);
        appStatementTiny.setTextColor(COUIContextUtil.getAttrColor(appStatementTiny.getContext(), R.attr.couiColorSecondNeutral));
        COUIChangeTextUtil.adaptFontSize(appStatementTiny, 2);
        appStatementTiny.setMovementMethod(COUILinkMovementMethod.INSTANCE);
        tinyContentViewHolder.getBtnConfirmTiny().setOnClickListener(new View.OnClickListener() {
            @Override
            public final void onClick(View view) {
                COUIUserStatementDialog.onTinyExitButtonClicked(COUIUserStatementDialog.this, view);
            }
        });
        tinyContentViewHolder.getBtnExitTiny().setOnClickListener(new View.OnClickListener() {
            @Override
            public final void onClick(View view) {
                COUIUserStatementDialog.onTinyConfirmButtonClicked(COUIUserStatementDialog.this, view);
            }
        });
        tinyContentViewHolder.getTitleTiny().setText(this.titleText);
        tinyContentViewHolder.getBtnConfirmTiny().setText(this.bottomButtonText);
        tinyContentViewHolder.getBtnExitTiny().setText(this.exitButtonText);
        tinyContentViewHolder.getAppStatementTiny().setText(this.statement);
        if (!TextUtils.isEmpty(this.protocolText)) {
            tinyContentViewHolder.getAppStatementTiny().append(this.protocolText);
        }
        View view = this.customViewTiny;
        if (view != null) {
            tinyContentViewHolder.getCustomFunctionalAreaTiny().addView(view);
        }
    }


    public static final void onTinyExitButtonClicked(COUIUserStatementDialog this$0, View view) {
        Intrinsics.checkNotNullParameter(this$0, "this$0");
        OnButtonClickListener onButtonClickListener = this$0.onButtonClickListener;
        if (onButtonClickListener != null) {
            onButtonClickListener.onBottomButtonClick();
        }
    }


    public static final void onTinyConfirmButtonClicked(COUIUserStatementDialog this$0, View view) {
        Intrinsics.checkNotNullParameter(this$0, "this$0");
        OnButtonClickListener onButtonClickListener = this$0.onButtonClickListener;
        if (onButtonClickListener != null) {
            onButtonClickListener.onExitButtonClick();
        }
    }

    private final void initViewHolderBind(ContentViewHolder contentViewHolder) {
        contentViewHolder.getIvLogo().setImageDrawable(this.logoDrawable);
        contentViewHolder.getTvLogoSubTitle().setText(this.titleText);
        contentViewHolder.getTvLogoName().setText(this.appName);
        contentViewHolder.getTvLogoMessage().setText(this.appMessage);
        contentViewHolder.getTxtStatement().setText(this.statement);
        if (!TextUtils.isEmpty(this.protocolText)) {
            contentViewHolder.getTxtStatement().append(this.protocolText);
        }
        TextView exitButton = contentViewHolder.getExitButton();
        if (exitButton != null) {
            exitButton.setText(this.exitButtonText);
        }
        contentViewHolder.getSmallLandExitButton().setText(this.exitButtonText);
        contentViewHolder.getSmallLandConfirmButton().setText(this.bottomButtonText);
        COUIButton btnConfirm = contentViewHolder.getBtnConfirm();
        if (btnConfirm != null) {
            btnConfirm.setText(this.bottomButtonText);
        }
        contentViewHolder.bindList(this.listViewHolderArray);
        COUIChangeTextUtil.adaptFontSize(contentViewHolder.getTvLogoMessage(), 2);
        COUIChangeTextUtil.adaptFontSize(contentViewHolder.getTvLogoSubTitle(), 2);
        COUIChangeTextUtil.adaptFontSize(contentViewHolder.getTvLogoName(), 4);
        TextView txtStatement = contentViewHolder.getTxtStatement();
        COUIChangeTextUtil.adaptFontSize(txtStatement, 2);
        COUILinkMovementMethod cOUILinkMovementMethod = COUILinkMovementMethod.INSTANCE;
        txtStatement.setMovementMethod(cOUILinkMovementMethod);
        TextView tvStatementProtocol = contentViewHolder.getTvStatementProtocol();
        COUIChangeTextUtil.adaptFontSize(tvStatementProtocol, 2);
        tvStatementProtocol.setMovementMethod(cOUILinkMovementMethod);
        TextView exitButton2 = contentViewHolder.getExitButton();
        if (exitButton2 != null) {
            exitButton2.setOnClickListener(new View.OnClickListener() {
                @Override
                public final void onClick(View view) {
                    COUIUserStatementDialog.onNormalExitButtonClicked(COUIUserStatementDialog.this, view);
                }
            });
            COUITextViewCompatUtil.setPressRippleDrawable(exitButton2);
        }
        COUIButton btnConfirm2 = contentViewHolder.getBtnConfirm();
        if (btnConfirm2 != null) {
            btnConfirm2.setOnClickListener(new View.OnClickListener() {
                @Override
                public final void onClick(View view) {
                    COUIUserStatementDialog.onNormalConfirmButtonClicked(COUIUserStatementDialog.this, view);
                }
            });
        }
        contentViewHolder.getSmallLandExitButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public final void onClick(View view) {
                COUIUserStatementDialog.onNormalAltExitButtonClicked(COUIUserStatementDialog.this, view);
            }
        });
        contentViewHolder.getSmallLandConfirmButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public final void onClick(View view) {
                COUIUserStatementDialog.onNormalAltConfirmButtonClicked(COUIUserStatementDialog.this, view);
            }
        });
        if (TextUtils.isEmpty(this.protocolText)) {
            contentViewHolder.getScrollTextStatementProtocol().setVisibility(8);
        }
        contentViewHolder.bindBottomButtonWarp();
        contentViewHolder.bindSimpleButtonGroupCtrl();
    }


    public static final void onNormalExitButtonClicked(COUIUserStatementDialog this$0, View view) {
        Intrinsics.checkNotNullParameter(this$0, "this$0");
        OnButtonClickListener onButtonClickListener = this$0.onButtonClickListener;
        if (onButtonClickListener != null) {
            onButtonClickListener.onExitButtonClick();
        }
    }


    public static final void onNormalConfirmButtonClicked(COUIUserStatementDialog this$0, View view) {
        Intrinsics.checkNotNullParameter(this$0, "this$0");
        OnButtonClickListener onButtonClickListener = this$0.onButtonClickListener;
        if (onButtonClickListener != null) {
            onButtonClickListener.onBottomButtonClick();
        }
    }


    public static final void onNormalAltExitButtonClicked(COUIUserStatementDialog this$0, View view) {
        Intrinsics.checkNotNullParameter(this$0, "this$0");
        OnButtonClickListener onButtonClickListener = this$0.onButtonClickListener;
        if (onButtonClickListener != null) {
            onButtonClickListener.onExitButtonClick();
        }
    }


    public static final void onNormalAltConfirmButtonClicked(COUIUserStatementDialog this$0, View view) {
        Intrinsics.checkNotNullParameter(this$0, "this$0");
        OnButtonClickListener onButtonClickListener = this$0.onButtonClickListener;
        if (onButtonClickListener != null) {
            onButtonClickListener.onBottomButtonClick();
        }
    }

    public final CharSequence getAppMessage() {
        return this.appMessage;
    }

    public final CharSequence getAppName() {
        return this.appName;
    }

    public final CharSequence getBottomButtonText() {
        return this.bottomButtonText;
    }

    public final View getCustomView() {
        return this.customView;
    }

    public final View getCustomViewTiny() {
        return this.customViewTiny;
    }

    public final CharSequence getExitButtonText() {
        return this.exitButtonText;
    }

    public final OnItemClickListener getItemClickListener() {
        return this.itemClickListener;
    }

    public final List<COUIUserStatementListItem> getListItems() {
        return this.listItems;
    }

    public final Drawable getLogoDrawable() {
        return this.logoDrawable;
    }

    public final OnButtonClickListener getOnButtonClickListener() {
        return this.onButtonClickListener;
    }

    public final CharSequence getProtocolText() {
        return this.protocolText;
    }

    public final CharSequence getStatement() {
        return this.statement;
    }

    public final CharSequence getTitleText() {
        return this.titleText;
    }

    public final void setAppMessage(CharSequence charSequence) {
        this.appMessage = charSequence;
    }

    public final void setAppName(CharSequence charSequence) {
        this.appName = charSequence;
    }

    public final void setBottomButtonText(CharSequence charSequence) {
        this.bottomButtonText = charSequence;
    }

    public final void setCustomView(View view) {
        this.customView = view;
    }

    public final void setCustomViewTiny(View view) {
        this.customViewTiny = view;
    }

    public final void setExitButtonText(CharSequence charSequence) {
        this.exitButtonText = charSequence;
    }

    @Override
    @Deprecated
    public void setIsShowInMaxHeight(boolean z6) {
        super.setIsShowInMaxHeight(z6);
        this.isFullPage = z6;
    }

    public final void setItemClickListener(OnItemClickListener onItemClickListener) {
        this.itemClickListener = onItemClickListener;
    }

    public final void setListItems(List<COUIUserStatementListItem> list) {
        this.listItems = list;
        this.listViewHolderArray.clear();
        List<COUIUserStatementListItem> list2 = this.listItems;
        if (list2 != null) {
            Intrinsics.checkNotNull(list2);
            int size = list2.size();
            for (int i2 = 0; i2 < size; i2++) {
                List<COUIUserStatementListItem> list3 = this.listItems;
                Intrinsics.checkNotNull(list3);
                COUIUserStatementListItem cOUIUserStatementListItem = list3.get(i2);
                ConstraintLayout constraintLayout = (ConstraintLayout) LayoutInflater.from(getContext()).inflate(R.layout.coui_component_statement_list_item, (ViewGroup) null);
                if (constraintLayout != null) {
                    ListItemViewHolder listItemViewHolder = new ListItemViewHolder(this, constraintLayout);
                    TextView title = listItemViewHolder.getTitle();
                    if (title != null) {
                        COUIChangeTextUtil.adaptFontSize(title, 2);
                    }
                    TextView message = listItemViewHolder.getMessage();
                    if (message != null) {
                        COUIChangeTextUtil.adaptFontSize(message, 2);
                    }
                    listItemViewHolder.bindListener(this.itemClickListener);
                    listItemViewHolder.bindItemData(cOUIUserStatementListItem, i2);
                    this.listViewHolderArray.add(listItemViewHolder);
                }
            }
        }
    }

    public final void setLogoDrawable(Drawable drawable) {
        this.logoDrawable = drawable;
    }

    public final void setOnButtonClickListener(OnButtonClickListener onButtonClickListener) {
        this.onButtonClickListener = onButtonClickListener;
    }

    public final void setProtocolText(CharSequence charSequence) {
        this.protocolText = charSequence;
    }

    public final void setStatement(CharSequence charSequence) {
        this.statement = charSequence;
    }

    public final void setTitleText(CharSequence charSequence) {
        this.titleText = charSequence;
    }

    @Override
    public void show() {
        if (COUIResponsiveUtils.isSmallScreenDp(getContext().getResources().getConfiguration().screenWidthDp) && getContext().getResources().getConfiguration().screenWidthDp < COUIStatementPanelStateChangeListener.Companion.getSCREN_DP_MINI_WIDTH().getValue()) {
            setIsInTinyScreen(true, false);
            super.setIsShowInMaxHeight(true);
        }
        super.show();
        Configuration configuration = getContext().getResources().getConfiguration();
        Intrinsics.checkNotNullExpressionValue(configuration, "context.resources.configuration");
        checkPanelStatus(configuration);
    }

    @Override
    public void updateLayoutWhileConfigChange(Configuration configuration) {
        Intrinsics.checkNotNullParameter(configuration, "configuration");
        super.updateLayoutWhileConfigChange(configuration);
        if (getContext().getResources().getConfiguration().screenWidthDp == configuration.screenWidthDp) {
            int i2 = getContext().getResources().getConfiguration().screenHeightDp;
            int i6 = configuration.screenHeightDp;
            if (i2 == i6) {
                int i10 = configuration.screenWidthDp;
                if (i10 == this.oldScreenWidthDp && i6 == this.oldScreenHeightDp) {
                    return;
                }
                this.oldScreenWidthDp = i10;
                this.oldScreenHeightDp = i6;
                checkPanelStatus(configuration);
            }
        }
    }


    public COUIUserStatementDialog(Context context, int i2) {
        this(context, i2, 0.0f, 0.0f, 12, null);
        Intrinsics.checkNotNullParameter(context, "context");
    }

    public final void setBottomButtonText(int i2) {
        this.bottomButtonText = getContext().getString(i2);
    }

    public final void setExitButtonText(int i2) {
        this.exitButtonText = getContext().getString(i2);
    }

    public final void setProtocolText(int i2) {
        this.protocolText = getContext().getString(i2);
    }

    public final void setStatement(int i2) {
        this.statement = getContext().getString(i2);
    }

    public final void setTitleText(int i2) {
        this.titleText = getContext().getString(i2);
    }


    public COUIUserStatementDialog(Context context, int i2, float f2) {
        this(context, i2, f2, 0.0f, 8, null);
        Intrinsics.checkNotNullParameter(context, "context");
    }

    public COUIUserStatementDialog(Context context, int i2, float f2, float f10, int i6, DefaultConstructorMarker defaultConstructorMarker) {
        this(context, (i6 & 2) != 0 ? R.style.DefaultBottomSheetDialog : i2, (i6 & 4) != 0 ? Float.MIN_VALUE : f2, (i6 & 8) != 0 ? Float.MIN_VALUE : f10);
    }


    public COUIUserStatementDialog(final Context context, int i2, float f2, float f10) {
        super(context, i2, f2, f10);
        Intrinsics.checkNotNullParameter(context, "context");
        int dimensionPixelSize = context.getResources().getDimensionPixelSize(R.dimen.coui_component_statement_margin_top_min);
        this.panelPaddingTopMin = dimensionPixelSize;
        int dimensionPixelSize2 = context.getResources().getDimensionPixelSize(R.dimen.coui_component_statement_expand_scroll_padding);
        this.expandScrollPadding = dimensionPixelSize2;
        this.expandPanelMarginTop = dimensionPixelSize - dimensionPixelSize2;
        this.scrollTextMaxHeightNormal = context.getResources().getDimensionPixelSize(R.dimen.coui_component_statement_scroll_text_height_normal);
        this.scrollTextMaxHeight = context.getResources().getDimensionPixelSize(R.dimen.coui_component_statement_scroll_text_height_max);
        this.panelStartPadding = context.getResources().getDimensionPixelSize(R.dimen.coui_component_statement_panel_start_padding);
        this.panelEndPadding = context.getResources().getDimensionPixelSize(R.dimen.coui_component_statement_panel_end_padding);
        this.customPaddingTop = context.getResources().getDimensionPixelSize(R.dimen.coui_component_statement_custom_padding_top);
        this.messagePaddingTop = context.getResources().getDimensionPixelSize(R.dimen.coui_component_statement_logo_message_margin_top);
        this.subTitlePaddingTop = context.getResources().getDimensionPixelSize(R.dimen.coui_component_statement_logo_subtitle_margin);
        this.contentPaddingEnd = context.getResources().getDimensionPixelOffset(R.dimen.coui_component_statement_panel_content_item_end_padding);
        this.customLayoutMinHeight = context.getResources().getDimensionPixelOffset(R.dimen.coui_component_statement_custom_layout_min_height);
        this.isFullPage = true;
        this.listViewHolderArray = new ArrayList();
        this.mEnumPanelStatusType = COUIStatementPanelStateChangeListener.PanelStatusTypeEnum.INIT;
        this.changeEnumUIListener = new COUIStatementPanelStateChangeListener() {
            @Override
            public void initMINIContentView(Configuration configuration, COUIStatementPanelStateChangeListener.PanelStatusTypeEnum oldPanelStatusTypeEnum) {
                Intrinsics.checkNotNullParameter(configuration, "configuration");
                Intrinsics.checkNotNullParameter(oldPanelStatusTypeEnum, "oldPanelStatusTypeEnum");
                if (oldPanelStatusTypeEnum == COUIStatementPanelStateChangeListener.PanelStatusTypeEnum.MINI) {
                    return;
                }
                if (COUIUserStatementDialog.this.miniContentViewHolder == null) {
                    COUIUserStatementDialog.this.miniContentView = LayoutInflater.from(context).inflate(R.layout.coui_component_statement_with_protocol_fixed, (ViewGroup) null);
                    View view = COUIUserStatementDialog.this.miniContentView;
                    if (view != null) {
                        COUIUserStatementDialog.this.miniContentViewHolder = new COUIUserStatementDialog.MINIContentViewHolder(view);
                    }
                    COUIUserStatementDialog.MINIContentViewHolder mINIContentViewHolder = COUIUserStatementDialog.this.miniContentViewHolder;
                    if (mINIContentViewHolder != null) {
                        COUIUserStatementDialog.this.initMINIView(mINIContentViewHolder);
                    }
                }
                COUIUserStatementDialog cOUIUserStatementDialog = COUIUserStatementDialog.this;
                cOUIUserStatementDialog.setContentView(cOUIUserStatementDialog.miniContentView);
                COUIUserStatementDialog.this.getBehavior().setDraggable(false);
                Object parent = COUIUserStatementDialog.this.getDragableLinearLayout().getDragView().getParent();
                Intrinsics.checkNotNull(parent, "null cannot be cast to non-null type android.view.View");
                ((View) parent).setVisibility(8);
            }

            @Override
            public void initNormalContentView(Configuration configuration, COUIStatementPanelStateChangeListener.PanelStatusTypeEnum oldPanelStatusTypeEnum) {
                Intrinsics.checkNotNullParameter(configuration, "configuration");
                Intrinsics.checkNotNullParameter(oldPanelStatusTypeEnum, "oldPanelStatusTypeEnum");
                if (oldPanelStatusTypeEnum == COUIStatementPanelStateChangeListener.PanelStatusTypeEnum.NORMAL) {
                    return;
                }
                if (COUIUserStatementDialog.this.normalContentViewHolder == null) {
                    COUIUserStatementDialog.this.normalContentView = LayoutInflater.from(context).inflate(R.layout.coui_component_full_page_statement_with_protocol, (ViewGroup) null);
                    View view = COUIUserStatementDialog.this.normalContentView;
                    if (view != null) {
                        COUIUserStatementDialog cOUIUserStatementDialog = COUIUserStatementDialog.this;
                        cOUIUserStatementDialog.normalContentViewHolder = new COUIUserStatementDialog.NormalContentViewHolder(view, context);
                        cOUIUserStatementDialog.initNormalView();
                    }
                }
                COUIUserStatementDialog.NormalContentViewHolder normalContentViewHolder = COUIUserStatementDialog.this.normalContentViewHolder;
                if (normalContentViewHolder != null) {
                    normalContentViewHolder.bindList(COUIUserStatementDialog.this.listViewHolderArray);
                }
                COUIUserStatementDialog.NormalContentViewHolder normalContentViewHolder2 = COUIUserStatementDialog.this.normalContentViewHolder;
                if (normalContentViewHolder2 != null) {
                    normalContentViewHolder2.bindCustomView(COUIUserStatementDialog.this.getCustomView());
                }
                COUIUserStatementDialog cOUIUserStatementDialog2 = COUIUserStatementDialog.this;
                cOUIUserStatementDialog2.setContentView(cOUIUserStatementDialog2.normalContentView);
                COUIUserStatementDialog.this.getBehavior().setDraggable(false);
                Object parent = COUIUserStatementDialog.this.getDragableLinearLayout().getDragView().getParent();
                Intrinsics.checkNotNull(parent, "null cannot be cast to non-null type android.view.View");
                ((View) parent).setVisibility(8);
            }

            @Override
            public void initSmallLandContentView(Configuration configuration, COUIStatementPanelStateChangeListener.PanelStatusTypeEnum oldPanelStatusTypeEnum) {
                Intrinsics.checkNotNullParameter(configuration, "configuration");
                Intrinsics.checkNotNullParameter(oldPanelStatusTypeEnum, "oldPanelStatusTypeEnum");
                if (oldPanelStatusTypeEnum == COUIStatementPanelStateChangeListener.PanelStatusTypeEnum.SMALL_LAND) {
                    return;
                }
                if (COUIUserStatementDialog.this.smallLandContentViewHolder == null) {
                    COUIUserStatementDialog.this.smallLandContentView = LayoutInflater.from(context).inflate(R.layout.coui_component_full_page_statement_with_protocol_small_land, (ViewGroup) null);
                    View view = COUIUserStatementDialog.this.smallLandContentView;
                    if (view != null) {
                        COUIUserStatementDialog cOUIUserStatementDialog = COUIUserStatementDialog.this;
                        cOUIUserStatementDialog.smallLandContentViewHolder = new COUIUserStatementDialog.SmallLandContentViewHolder(view, context);
                        cOUIUserStatementDialog.initSmallLandView();
                    }
                }
                COUIUserStatementDialog.SmallLandContentViewHolder smallLandContentViewHolder = COUIUserStatementDialog.this.smallLandContentViewHolder;
                if (smallLandContentViewHolder != null) {
                    smallLandContentViewHolder.bindList(COUIUserStatementDialog.this.listViewHolderArray);
                }
                COUIUserStatementDialog.SmallLandContentViewHolder smallLandContentViewHolder2 = COUIUserStatementDialog.this.smallLandContentViewHolder;
                if (smallLandContentViewHolder2 != null) {
                    smallLandContentViewHolder2.bindCustomView(COUIUserStatementDialog.this.getCustomView());
                }
                COUIUserStatementDialog cOUIUserStatementDialog2 = COUIUserStatementDialog.this;
                cOUIUserStatementDialog2.setContentView(cOUIUserStatementDialog2.smallLandContentView);
                COUIUserStatementDialog.this.getBehavior().setDraggable(false);
                Object parent = COUIUserStatementDialog.this.getDragableLinearLayout().getDragView().getParent();
                Intrinsics.checkNotNull(parent, "null cannot be cast to non-null type android.view.View");
                ((View) parent).setVisibility(8);
            }

            @Override
            public void initSplitScreenContentView(Configuration configuration, COUIStatementPanelStateChangeListener.PanelStatusTypeEnum oldPanelStatusTypeEnum) {
                Intrinsics.checkNotNullParameter(configuration, "configuration");
                Intrinsics.checkNotNullParameter(oldPanelStatusTypeEnum, "oldPanelStatusTypeEnum");
                initNormalContentView(configuration, oldPanelStatusTypeEnum);
            }

            @Override
            public void initTinyContentView(Configuration configuration, COUIStatementPanelStateChangeListener.PanelStatusTypeEnum oldPanelStatusTypeEnum) {
                Intrinsics.checkNotNullParameter(configuration, "configuration");
                Intrinsics.checkNotNullParameter(oldPanelStatusTypeEnum, "oldPanelStatusTypeEnum");
                if (oldPanelStatusTypeEnum == COUIStatementPanelStateChangeListener.PanelStatusTypeEnum.TINY) {
                    return;
                }
                if (COUIUserStatementDialog.this.tinyContentViewHolder == null) {
                    COUIUserStatementDialog.this.tinyContentView = LayoutInflater.from(context).inflate(R.layout.coui_component_statement_with_protocol_fixed_tiny, (ViewGroup) null);
                    View view = COUIUserStatementDialog.this.tinyContentView;
                    if (view != null) {
                        COUIUserStatementDialog.this.tinyContentViewHolder = new COUIUserStatementDialog.TinyContentViewHolder(view);
                    }
                    COUIUserStatementDialog.TinyContentViewHolder tinyContentViewHolder = COUIUserStatementDialog.this.tinyContentViewHolder;
                    if (tinyContentViewHolder != null) {
                        COUIUserStatementDialog.this.initTinyView(tinyContentViewHolder);
                    }
                }
                COUIUserStatementDialog cOUIUserStatementDialog = COUIUserStatementDialog.this;
                cOUIUserStatementDialog.setContentView(cOUIUserStatementDialog.tinyContentView);
                COUIUserStatementDialog.this.getBehavior().setDraggable(false);
                COUIPanelBarView panelBarView = COUIUserStatementDialog.this.getDragableLinearLayout().getPanelBarView();
                if (panelBarView != null) {
                    panelBarView.setVisibility(8);
                }
                COUIUserStatementDialog.this.getDragableLinearLayout().getDragView().setVisibility(8);
            }

            @Override
            public void updateMINIContentView(Configuration configuration) {
                Intrinsics.checkNotNullParameter(configuration, "configuration");
                COUIUserStatementDialog.MINIContentViewHolder mINIContentViewHolder = COUIUserStatementDialog.this.miniContentViewHolder;
                if (mINIContentViewHolder != null) {
                    Context context2 = COUIUserStatementDialog.this.getContext();
                    Intrinsics.checkNotNullExpressionValue(context2, "getContext()");
                    mINIContentViewHolder.refreshMINITextSize(configuration, context2);
                }
                COUIUserStatementDialog.MINIContentViewHolder mINIContentViewHolder2 = COUIUserStatementDialog.this.miniContentViewHolder;
                if (mINIContentViewHolder2 != null) {
                    Context context3 = COUIUserStatementDialog.this.getContext();
                    Intrinsics.checkNotNullExpressionValue(context3, "getContext()");
                    mINIContentViewHolder2.refreshSimpleButtonGroupCtrl(configuration, context3);
                }
            }

            @Override
            public void updateNormalContentView(Configuration configuration) {
                Intrinsics.checkNotNullParameter(configuration, "configuration");
                COUIUserStatementDialog.NormalContentViewHolder normalContentViewHolder = COUIUserStatementDialog.this.normalContentViewHolder;
                if (normalContentViewHolder != null) {
                    COUIUserStatementDialog cOUIUserStatementDialog = COUIUserStatementDialog.this;
                    cOUIUserStatementDialog.addOnLayoutChangeListenerToScrollText(cOUIUserStatementDialog.getStatement(), cOUIUserStatementDialog.getProtocolText(), normalContentViewHolder);
                }
                COUIUserStatementDialog.NormalContentViewHolder normalContentViewHolder2 = COUIUserStatementDialog.this.normalContentViewHolder;
                if (normalContentViewHolder2 != null) {
                    normalContentViewHolder2.updateNormalFoldingScrollUI();
                }
                COUIUserStatementDialog.NormalContentViewHolder normalContentViewHolder3 = COUIUserStatementDialog.this.normalContentViewHolder;
                if (normalContentViewHolder3 != null) {
                    normalContentViewHolder3.updateScrollTextMaxHeight(COUIUserStatementDialog.this.getListItems(), COUIUserStatementDialog.this.getCustomView(), configuration, COUIUserStatementDialog.this.scrollTextMaxHeightNormal, COUIUserStatementDialog.this.scrollTextMaxHeight, context);
                }
                COUIUserStatementDialog.NormalContentViewHolder normalContentViewHolder4 = COUIUserStatementDialog.this.normalContentViewHolder;
                if (normalContentViewHolder4 != null) {
                    normalContentViewHolder4.updateLayoutUiVisibleState(COUIUserStatementDialog.this.getListItems(), COUIUserStatementDialog.this.getCustomView(), COUIUserStatementDialog.this.messagePaddingTop, COUIUserStatementDialog.this.customPaddingTop, COUIUserStatementDialog.this.subTitlePaddingTop);
                }
                COUIUserStatementDialog.NormalContentViewHolder normalContentViewHolder5 = COUIUserStatementDialog.this.normalContentViewHolder;
                if (normalContentViewHolder5 != null) {
                    View contentView = COUIUserStatementDialog.this.getContentView();
                    Intrinsics.checkNotNullExpressionValue(contentView, "contentView");
                    normalContentViewHolder5.refreshBottomButtonWarp(configuration, contentView);
                }
                COUIUserStatementDialog.NormalContentViewHolder normalContentViewHolder6 = COUIUserStatementDialog.this.normalContentViewHolder;
                if (normalContentViewHolder6 != null) {
                    View contentView2 = COUIUserStatementDialog.this.getContentView();
                    Intrinsics.checkNotNullExpressionValue(contentView2, "contentView");
                    normalContentViewHolder6.refreshSimpleButtonGroupCtrl(configuration, contentView2);
                }
            }

            @Override
            public void updateSmallLandContentView(Configuration configuration) {
                Intrinsics.checkNotNullParameter(configuration, "configuration");
                COUIUserStatementDialog.SmallLandContentViewHolder smallLandContentViewHolder = COUIUserStatementDialog.this.smallLandContentViewHolder;
                if (smallLandContentViewHolder != null) {
                    COUIUserStatementDialog cOUIUserStatementDialog = COUIUserStatementDialog.this;
                    cOUIUserStatementDialog.addOnLayoutChangeListenerToScrollText(cOUIUserStatementDialog.getStatement(), cOUIUserStatementDialog.getProtocolText(), smallLandContentViewHolder);
                }
                COUIUserStatementDialog.SmallLandContentViewHolder smallLandContentViewHolder2 = COUIUserStatementDialog.this.smallLandContentViewHolder;
                if (smallLandContentViewHolder2 != null) {
                    smallLandContentViewHolder2.updateLayoutUiVisibleState(COUIUserStatementDialog.this.getListItems(), COUIUserStatementDialog.this.getCustomView(), COUIUserStatementDialog.this.messagePaddingTop, COUIUserStatementDialog.this.customPaddingTop, COUIUserStatementDialog.this.subTitlePaddingTop);
                }
            }

            @Override
            public void updateSplitScreenContentView(Configuration configuration) {
                Intrinsics.checkNotNullParameter(configuration, "configuration");
                COUIUserStatementDialog.NormalContentViewHolder normalContentViewHolder = COUIUserStatementDialog.this.normalContentViewHolder;
                if (normalContentViewHolder != null) {
                    COUIUserStatementDialog cOUIUserStatementDialog = COUIUserStatementDialog.this;
                    cOUIUserStatementDialog.addOnLayoutChangeListenerToScrollText(cOUIUserStatementDialog.getStatement(), cOUIUserStatementDialog.getProtocolText(), normalContentViewHolder);
                }
                COUIUserStatementDialog.NormalContentViewHolder normalContentViewHolder2 = COUIUserStatementDialog.this.normalContentViewHolder;
                if (normalContentViewHolder2 != null) {
                    normalContentViewHolder2.updateExpandScrollUI(COUIUserStatementDialog.this.customLayoutMinHeight);
                }
                COUIUserStatementDialog.NormalContentViewHolder normalContentViewHolder3 = COUIUserStatementDialog.this.normalContentViewHolder;
                if (normalContentViewHolder3 != null) {
                    normalContentViewHolder3.updateSplitScreenScrollTextMaxHeight();
                }
                COUIUserStatementDialog.NormalContentViewHolder normalContentViewHolder4 = COUIUserStatementDialog.this.normalContentViewHolder;
                if (normalContentViewHolder4 != null) {
                    normalContentViewHolder4.updateLayoutUiVisibleState(COUIUserStatementDialog.this.getListItems(), COUIUserStatementDialog.this.getCustomView(), COUIUserStatementDialog.this.messagePaddingTop, COUIUserStatementDialog.this.customPaddingTop, COUIUserStatementDialog.this.subTitlePaddingTop);
                }
                COUIUserStatementDialog.NormalContentViewHolder normalContentViewHolder5 = COUIUserStatementDialog.this.normalContentViewHolder;
                if (normalContentViewHolder5 != null) {
                    View contentView = COUIUserStatementDialog.this.getContentView();
                    Intrinsics.checkNotNullExpressionValue(contentView, "contentView");
                    normalContentViewHolder5.refreshBottomButtonWarp(configuration, contentView);
                }
                COUIUserStatementDialog.NormalContentViewHolder normalContentViewHolder6 = COUIUserStatementDialog.this.normalContentViewHolder;
                if (normalContentViewHolder6 != null) {
                    View contentView2 = COUIUserStatementDialog.this.getContentView();
                    Intrinsics.checkNotNullExpressionValue(contentView2, "contentView");
                    normalContentViewHolder6.refreshSimpleButtonGroupCtrl(configuration, contentView2);
                }
                COUIUserStatementDialog.NormalContentViewHolder normalContentViewHolder7 = COUIUserStatementDialog.this.normalContentViewHolder;
                if (normalContentViewHolder7 != null) {
                    normalContentViewHolder7.updateSplitScreenLogoPaddingTop(COUIUserStatementDialog.this.expandPanelMarginTop, COUIUserStatementDialog.this.expandScrollPadding, COUIUserStatementDialog.this.expandScrollPadding, COUIUserStatementDialog.this.panelStartPadding, COUIUserStatementDialog.this.panelEndPadding, COUIUserStatementDialog.this.contentPaddingEnd);
                }
            }

            @Override
            public void updateTinyContentView(Configuration configuration) {
                Intrinsics.checkNotNullParameter(configuration, "configuration");
                COUIUserStatementDialog.TinyContentViewHolder tinyContentViewHolder = COUIUserStatementDialog.this.tinyContentViewHolder;
                if (tinyContentViewHolder != null) {
                    tinyContentViewHolder.refreshTinyContentViewHolder(COUIUserStatementDialog.this.getCustomViewTiny());
                }
            }
        };
        setIsShowInMaxHeight(true);
        setCanceledOnTouchOutside(false);
        this.isLargeScreenLimitMaxSize = true;
    }
}
