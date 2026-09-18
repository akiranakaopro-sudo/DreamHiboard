package com.coui.appcompat.panel;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.COUIPanelPreferenceLinearLayoutManager;
import androidx.recyclerview.widget.COUIRecyclerView;
import com.coui.appcompat.R;
import com.coui.appcompat.panel.COUIBottomSheetChoiceListAdapter;
import com.coui.appcompat.toolbar.COUIToolbar;


public class COUIListBottomSheetDialog {
    private COUIBottomSheetDialog mBottomSheetDialog;

    public static class Builder extends AlertDialog.Builder {
        private View.OnClickListener mCenterButtonClickListener;
        private String mCenterButtonText;
        public int mCheckedItem;
        public boolean[] mCheckedItems;
        private Context mContext;

        @Deprecated
        private int mFinalNavColorAfterDismiss;

        @Deprecated
        private boolean mIsExecuteNavColorAnimAfterDismiss;
        private boolean mIsMultiChoice;
        private CharSequence[] mItems;
        private View mLayout;
        private View.OnClickListener mLeftButtonClickListener;
        private String mLeftButtonText;
        private COUIListBottomSheetDialog mListBottomSheetDialog;
        public DialogInterface.OnMultiChoiceClickListener mOnCheckboxClickListener;
        public OnMenuItemClickListener mOnMenuItemClick;
        public DialogInterface.OnClickListener mOnRadioButtonClickListener;
        private View.OnClickListener mRightButtonClickListener;
        private String mRightButtonText;
        private CharSequence[] mSummaries;
        private CharSequence mTitle;

        public Builder(Context context) {
            super(context);
            this.mCheckedItem = -1;
            this.mListBottomSheetDialog = new COUIListBottomSheetDialog();
            this.mIsMultiChoice = false;
            init(context);
        }

        private void init(Context context) {
            this.mContext = context;
            this.mLayout = LayoutInflater.from(context).inflate(com.coui.appcompat.R.layout.coui_list_bottom_sheet_dialog_layout, (ViewGroup) null);
        }

        public COUIListBottomSheetDialog createDialog() {
            COUIBottomSheetChoiceListAdapter choiceListAdapter;
            this.mListBottomSheetDialog.mBottomSheetDialog = new COUIBottomSheetDialog(this.mContext, com.coui.appcompat.R.style.DefaultBottomSheetDialog);
            this.mListBottomSheetDialog.mBottomSheetDialog.setContentView(this.mLayout);
            COUIRecyclerView recyclerView = (COUIRecyclerView) this.mListBottomSheetDialog.mBottomSheetDialog.findViewById(com.coui.appcompat.R.id.select_dialog_listview);
            COUIPanelPreferenceLinearLayoutManager layoutManager = new COUIPanelPreferenceLinearLayoutManager(this.mContext);
            layoutManager.setOrientation(1);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setItemAnimator(null);
            COUIToolbar toolbar = (COUIToolbar) this.mListBottomSheetDialog.mBottomSheetDialog.findViewById(com.coui.appcompat.R.id.toolbar);
            toolbar.setTitle(this.mTitle);
            toolbar.setIsTitleCenterStyle(true);
            if (this.mIsMultiChoice) {
                ((LinearLayout.LayoutParams) recyclerView.getLayoutParams()).bottomMargin = 0;
                choiceListAdapter = new COUIBottomSheetChoiceListAdapter(this.mContext, R.layout.coui_select_dialog_multichoice, this.mItems, this.mSummaries, -1, this.mCheckedItems, true);
            } else {
                choiceListAdapter = new COUIBottomSheetChoiceListAdapter(this.mContext, R.layout.coui_select_dialog_singlechoice, this.mItems, this.mSummaries, this.mCheckedItem);
            }
            this.mListBottomSheetDialog.mBottomSheetDialog.getDragableLinearLayout().getDragView().setVisibility(4);
            recyclerView.setAdapter(choiceListAdapter);
            choiceListAdapter.setOnItemClickListener(new COUIBottomSheetChoiceListAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position, int state) {
                    if (Builder.this.mIsMultiChoice) {
                        Builder builder = Builder.this;
                        DialogInterface.OnMultiChoiceClickListener onMultiChoiceClickListener = builder.mOnCheckboxClickListener;
                        if (onMultiChoiceClickListener != null) {
                            onMultiChoiceClickListener.onClick(builder.mListBottomSheetDialog.mBottomSheetDialog, position, state == 2);
                            return;
                        }
                        return;
                    }
                    Builder builder = Builder.this;
                    DialogInterface.OnClickListener onClickListener = builder.mOnRadioButtonClickListener;
                    if (onClickListener != null) {
                        onClickListener.onClick(builder.mListBottomSheetDialog.mBottomSheetDialog, position);
                    }
                }
            });
            return this.mListBottomSheetDialog;
        }

        public Dialog getBottomSheetDialog() {
            return this.mListBottomSheetDialog.mBottomSheetDialog;
        }

        public Builder setCenterButton(String str, View.OnClickListener onClickListener) {
            this.mCenterButtonText = str;
            this.mCenterButtonClickListener = onClickListener;
            return this;
        }

        @Deprecated
        public Builder setExecuteNavColorAnimAfterDismiss(boolean executeNavColorAnimAfterDismiss) {
            this.mIsExecuteNavColorAnimAfterDismiss = executeNavColorAnimAfterDismiss;
            return this;
        }

        @Deprecated
        public Builder setFinalNavColorAfterDismiss(int finalNavColorAfterDismiss) {
            this.mFinalNavColorAfterDismiss = finalNavColorAfterDismiss;
            return this;
        }

        public Builder setLeftButton(String str, View.OnClickListener onClickListener) {
            this.mLeftButtonText = str;
            this.mLeftButtonClickListener = onClickListener;
            return this;
        }

        @Deprecated
        public Builder setMenuItemClickListener(OnMenuItemClickListener onMenuItemClickListener) {
            this.mOnMenuItemClick = onMenuItemClickListener;
            return this;
        }

        @Override
        public Builder setMultiChoiceItems(CharSequence[] items, boolean[] checkedItems, DialogInterface.OnMultiChoiceClickListener onMultiChoiceClickListener) {
            this.mItems = items;
            this.mCheckedItems = checkedItems;
            this.mIsMultiChoice = true;
            this.mOnCheckboxClickListener = onMultiChoiceClickListener;
            return this;
        }

        public Builder setRightButton(String str, View.OnClickListener onClickListener) {
            this.mRightButtonText = str;
            this.mRightButtonClickListener = onClickListener;
            return this;
        }

        @Override
        public Builder setSingleChoiceItems(CharSequence[] items, int checkedItem, DialogInterface.OnClickListener onClickListener) {
            this.mItems = items;
            this.mOnRadioButtonClickListener = onClickListener;
            this.mCheckedItem = checkedItem;
            this.mIsMultiChoice = false;
            return this;
        }

        public Builder setSummaries(CharSequence[] summaries) {
            this.mSummaries = summaries;
            return this;
        }

        @Override
        public Builder setTitle(CharSequence title) {
            this.mTitle = title;
            return this;
        }

        public Builder setMultiChoiceItems(CharSequence[] items, boolean[] checkedItems, CharSequence[] summaries, DialogInterface.OnMultiChoiceClickListener onMultiChoiceClickListener) {
            this.mItems = items;
            this.mCheckedItems = checkedItems;
            this.mIsMultiChoice = true;
            this.mSummaries = summaries;
            this.mOnCheckboxClickListener = onMultiChoiceClickListener;
            return this;
        }

        public Builder setSingleChoiceItems(CharSequence[] items, int checkedItem, CharSequence[] summaries, DialogInterface.OnClickListener onClickListener) {
            this.mItems = items;
            this.mOnRadioButtonClickListener = onClickListener;
            this.mCheckedItem = checkedItem;
            this.mIsMultiChoice = false;
            this.mSummaries = summaries;
            return this;
        }

        public Builder setSummaries(int summariesResId) {
            this.mSummaries = this.mContext.getResources().getTextArray(summariesResId);
            return this;
        }

        public Builder(Context context, int themeResId) {
            super(context, themeResId);
            this.mCheckedItem = -1;
            this.mListBottomSheetDialog = new COUIListBottomSheetDialog();
            this.mIsMultiChoice = false;
            init(new ContextThemeWrapper(context, themeResId));
        }

        @Override
        public Builder setTitle(int titleResId) {
            this.mTitle = this.mContext.getString(titleResId);
            return this;
        }

        @Override
        public Builder setMultiChoiceItems(int itemsResId, boolean[] checkedItems, DialogInterface.OnMultiChoiceClickListener onMultiChoiceClickListener) {
            this.mItems = this.mContext.getResources().getTextArray(itemsResId);
            this.mCheckedItems = checkedItems;
            this.mIsMultiChoice = true;
            this.mOnCheckboxClickListener = onMultiChoiceClickListener;
            return this;
        }

        @Override
        public Builder setSingleChoiceItems(int itemsResId, int checkedItem, DialogInterface.OnClickListener onClickListener) {
            this.mItems = this.mContext.getResources().getTextArray(itemsResId);
            this.mOnRadioButtonClickListener = onClickListener;
            this.mCheckedItem = checkedItem;
            this.mIsMultiChoice = false;
            return this;
        }

        public Builder setMultiChoiceItems(int itemsResId, boolean[] checkedItems, int summariesResId, DialogInterface.OnMultiChoiceClickListener onMultiChoiceClickListener) {
            this.mItems = this.mContext.getResources().getTextArray(itemsResId);
            this.mCheckedItems = checkedItems;
            this.mIsMultiChoice = true;
            this.mSummaries = this.mContext.getResources().getTextArray(summariesResId);
            this.mOnCheckboxClickListener = onMultiChoiceClickListener;
            return this;
        }

        public Builder setSingleChoiceItems(int itemsResId, int checkedItem, int summariesResId, DialogInterface.OnClickListener onClickListener) {
            this.mItems = this.mContext.getResources().getTextArray(itemsResId);
            this.mOnRadioButtonClickListener = onClickListener;
            this.mCheckedItem = checkedItem;
            this.mIsMultiChoice = false;
            this.mSummaries = this.mContext.getResources().getTextArray(summariesResId);
            return this;
        }
    }

    public interface OnMenuItemClickListener {
        void onCancelItemClick();

        void onSaveItemClick();
    }

    public void dismiss() {
        COUIBottomSheetDialog bottomSheetDialog = this.mBottomSheetDialog;
        if (bottomSheetDialog != null) {
            bottomSheetDialog.dismiss();
        }
    }

    public boolean isShowing() {
        COUIBottomSheetDialog bottomSheetDialog = this.mBottomSheetDialog;
        if (bottomSheetDialog != null) {
            return bottomSheetDialog.isShowing();
        }
        return false;
    }

    public void refresh() {
        COUIBottomSheetDialog bottomSheetDialog = this.mBottomSheetDialog;
        if (bottomSheetDialog != null) {
            bottomSheetDialog.refresh();
        }
    }

    public void show() {
        COUIBottomSheetDialog bottomSheetDialog = this.mBottomSheetDialog;
        if (bottomSheetDialog != null) {
            bottomSheetDialog.show();
        }
    }
}
