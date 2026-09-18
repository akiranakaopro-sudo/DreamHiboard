package com.coui.appcompat.statement;

import com.coui.appcompat.R;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import com.coui.appcompat.button.COUIButton;
import com.coui.appcompat.textutil.COUIChangeTextUtil;


public class COUIFullPageStatement extends LinearLayout {
    private static final int CONFIRM_BUTTON_MAX_LINES = 2;
    private static final int MEDIUM_LARGE_SCREEN_SW_THRESHOLD = 480;
    private boolean isInSmallLand;
    private TextView mAppStatement;
    private COUIButton mBottomButtonLand;
    private COUIButton mBottomButtonNormal;
    private LinearLayout mButtonLayoutLand;
    private LinearLayout mButtonLayoutNormal;
    private Context mContext;
    private LinearLayoutCompat mCustomView;
    private COUIButton mExitButtonLand;
    private TextView mExitTextView;
    private LayoutInflater mLayoutInflater;
    private int mLayoutResourceId;
    private OnButtonClickListener mOnButtonClickListener;
    private ScrollView mScrollButton;
    private COUIMaxHeightScrollView mScrollText;
    private int mStyle;
    private TextView mTitle;
    private COUIMaxHeightScrollView mTitleScrollView;

    public interface OnButtonClickListener {
        void onBottomButtonClick();

        void onExitButtonClick();
    }

    public COUIFullPageStatement(Context context) {
        this(context, (AttributeSet) null);
    }

    private void disableTitleScrollBeforeMaxHeight() {
        if (this.mTitleScrollView == null) {
            return;
        }
        post(new Runnable() {
            @Override
            public void run() {
                if (COUIFullPageStatement.this.mTitleScrollView.getHeight() < COUIFullPageStatement.this.mTitleScrollView.getMaxHeight()) {
                    COUIFullPageStatement.this.mTitleScrollView.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent) {
                            return true;
                        }
                    });
                }
            }
        });
    }

    private void initView() {
        LayoutInflater layoutInflater = (LayoutInflater) this.mContext.getSystemService("layout_inflater");
        this.mLayoutInflater = layoutInflater;
        View viewInflate = layoutInflater.inflate(this.mLayoutResourceId, this);
        this.mAppStatement = (TextView) viewInflate.findViewById(R.id.txt_statement);
        this.mScrollText = (COUIMaxHeightScrollView) viewInflate.findViewById(R.id.scroll_text);
        this.isInSmallLand = isSmallScreen(this.mContext.getResources().getConfiguration()) && !isPortrait(this.mContext.getResources().getConfiguration());
        this.mExitTextView = (TextView) viewInflate.findViewById(R.id.txt_exit);
        this.mBottomButtonNormal = (COUIButton) viewInflate.findViewById(R.id.btn_confirm);
        if (isNotTinyStyle()) {
            this.mExitButtonLand = (COUIButton) viewInflate.findViewById(R.id.btn_exit_land);
            this.mBottomButtonLand = (COUIButton) viewInflate.findViewById(R.id.btn_confirm_land);
            this.mButtonLayoutLand = (LinearLayout) viewInflate.findViewById(R.id.button_layout_land);
            this.mButtonLayoutNormal = (LinearLayout) viewInflate.findViewById(R.id.button_layout_normal);
            this.mBottomButtonLand.setSingleLine(false);
            this.mBottomButtonLand.setMaxLines(CONFIRM_BUTTON_MAX_LINES);
            this.mBottomButtonLand.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (COUIFullPageStatement.this.mOnButtonClickListener != null) {
                        COUIFullPageStatement.this.mOnButtonClickListener.onBottomButtonClick();
                    }
                }
            });
            this.mExitButtonLand.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (COUIFullPageStatement.this.mOnButtonClickListener != null) {
                        COUIFullPageStatement.this.mOnButtonClickListener.onExitButtonClick();
                    }
                }
            });
        }
        this.mTitle = (TextView) viewInflate.findViewById(R.id.txt_title);
        this.mTitleScrollView = (COUIMaxHeightScrollView) viewInflate.findViewById(R.id.title_scroll_view);
        this.mScrollButton = (ScrollView) viewInflate.findViewById(R.id.scroll_button);
        this.mCustomView = (LinearLayoutCompat) viewInflate.findViewById(R.id.custom_functional_area);
        refreshParams();
        disableTitleScrollBeforeMaxHeight();
        COUIChangeTextUtil.adaptFontSize(this.mAppStatement, 2);
        this.mBottomButtonNormal.setSingleLine(false);
        this.mBottomButtonNormal.setMaxLines(CONFIRM_BUTTON_MAX_LINES);
        this.mBottomButtonNormal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (COUIFullPageStatement.this.mOnButtonClickListener != null) {
                    COUIFullPageStatement.this.mOnButtonClickListener.onBottomButtonClick();
                }
            }
        });
        this.mExitTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (COUIFullPageStatement.this.mOnButtonClickListener != null) {
                    COUIFullPageStatement.this.mOnButtonClickListener.onExitButtonClick();
                }
            }
        });
        COUIChangeTextUtil.adaptFontSize(this.mExitTextView, 4);
    }

    private boolean isNotTinyStyle() {
        return this.mLayoutResourceId == R.layout.coui_full_page_statement;
    }

    private boolean isPortrait(Configuration configuration) {
        return configuration.orientation == 1;
    }

    private boolean isSmallScreen(Configuration configuration) {
        return configuration.smallestScreenWidthDp < MEDIUM_LARGE_SCREEN_SW_THRESHOLD;
    }

    public TextView getAppStatement() {
        return this.mAppStatement;
    }

    public COUIButton getConfirmButton() {
        return (this.isInSmallLand && isNotTinyStyle()) ? this.mBottomButtonLand : this.mBottomButtonNormal;
    }

    public TextView getExitButton() {
        return (this.isInSmallLand && isNotTinyStyle()) ? this.mExitButtonLand : this.mExitTextView;
    }

    public COUIMaxHeightScrollView getScrollTextView() {
        return this.mScrollText;
    }

    @Override
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        if (this.mLayoutResourceId == R.layout.coui_full_page_statement_tiny) {
            return;
        }
        boolean isInSmallLand_2 = isSmallScreen(configuration) && !isPortrait(configuration);
        if (!isInSmallLand_2) {
            this.mBottomButtonNormal.getLayoutParams().width = getContext().createConfigurationContext(configuration).getResources().getDimensionPixelOffset(R.dimen.coui_full_page_statement_button_width);
        }
        if (this.isInSmallLand != isInSmallLand_2) {
            this.isInSmallLand = isInSmallLand_2;
            refreshParams();
        }
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom_2) {
        super.onLayout(changed, left, top, right, bottom_2);
        if (this.mLayoutResourceId == R.layout.coui_full_page_statement_tiny) {
            ViewParent parent = this.mScrollButton.getParent();
            if (parent instanceof LinearLayout) {
                LinearLayout linearLayout = (LinearLayout) parent;
                int bottom = ((linearLayout.getBottom() - linearLayout.getTop()) - this.mScrollButton.getTop()) - this.mScrollButton.getMeasuredHeight();
                ScrollView scrollView = this.mScrollButton;
                scrollView.layout(scrollView.getLeft(), this.mScrollButton.getTop() + bottom, this.mScrollButton.getRight(), this.mScrollButton.getBottom() + bottom);
            }
        }
    }

    public void refresh() {
        String resourceTypeName = getResources().getResourceTypeName(this.mStyle);
        TypedArray typedArrayObtainStyledAttributes = null;
        if ("attr".equals(resourceTypeName)) {
            typedArrayObtainStyledAttributes = this.mContext.obtainStyledAttributes(null, R.styleable.COUIFullPageStatement, this.mStyle, 0);
        } else if ("style".equals(resourceTypeName)) {
            typedArrayObtainStyledAttributes = this.mContext.obtainStyledAttributes(null, R.styleable.COUIFullPageStatement, 0, this.mStyle);
        }
        if (typedArrayObtainStyledAttributes != null) {
            this.mExitTextView.setTextColor(typedArrayObtainStyledAttributes.getColor(R.styleable.COUIFullPageStatement_couiFullPageStatementTextButtonColor, 0));
            this.mAppStatement.setTextColor(typedArrayObtainStyledAttributes.getColor(R.styleable.COUIFullPageStatement_couiFullPageStatementTextColor, 0));
            typedArrayObtainStyledAttributes.recycle();
        }
    }

    public void refreshParams() {
        if (isNotTinyStyle()) {
            if (this.isInSmallLand) {
                this.mButtonLayoutNormal.setVisibility(8);
                this.mButtonLayoutLand.setVisibility(0);
            } else {
                this.mButtonLayoutNormal.setVisibility(0);
                this.mButtonLayoutLand.setVisibility(8);
            }
        }
    }

    public void setAppStatement(CharSequence charSequence) {
        this.mAppStatement.setText(charSequence);
    }

    public void setAppStatementTextColor(int appStatementTextColor) {
        this.mAppStatement.setTextColor(appStatementTextColor);
    }

    public void setButtonDisableColor(int buttonDisableColor) {
        this.mBottomButtonNormal.setDisabledColor(buttonDisableColor);
        if (isNotTinyStyle()) {
            this.mBottomButtonLand.setDisabledColor(buttonDisableColor);
        }
    }

    public void setButtonDrawableColor(int buttonDrawableColor) {
        this.mBottomButtonNormal.setDrawableColor(buttonDrawableColor);
        if (isNotTinyStyle()) {
            this.mBottomButtonLand.setDrawableColor(buttonDrawableColor);
        }
    }

    public void setButtonListener(OnButtonClickListener onButtonClickListener) {
        this.mOnButtonClickListener = onButtonClickListener;
    }

    public void setButtonText(CharSequence charSequence) {
        this.mBottomButtonNormal.setText(charSequence);
        if (isNotTinyStyle()) {
            this.mBottomButtonLand.setText(charSequence);
        }
    }

    public void setCustomView(View view) {
        LinearLayoutCompat linearLayoutCompat = this.mCustomView;
        if (linearLayoutCompat != null) {
            if (view == null) {
                linearLayoutCompat.removeAllViews();
                this.mCustomView.setVisibility(8);
            } else {
                linearLayoutCompat.setVisibility(0);
                this.mCustomView.removeAllViews();
                this.mCustomView.addView(view);
            }
        }
    }

    public void setExitButtonText(CharSequence charSequence) {
        if (isNotTinyStyle()) {
            this.mExitButtonLand.setText(charSequence);
        }
        this.mExitTextView.setText(charSequence);
    }

    public void setExitTextColor(int exitTextColor) {
        this.mExitTextView.setTextColor(exitTextColor);
    }

    public void setStatementMaxHeight(int statementMaxHeight) {
        this.mScrollText.setMaxHeight(statementMaxHeight);
    }

    public void setTitleText(CharSequence charSequence) {
        this.mTitle.setText(charSequence);
    }

    public COUIFullPageStatement(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, R.attr.couiFullPageStatementStyle);
    }

    public COUIFullPageStatement(Context context, AttributeSet attributeSet, int index) {
        this(context, attributeSet, index, R.style.Widget_COUI_COUIFullPageStatement);
    }

    public COUIFullPageStatement(Context context, int index) {
        this(context, null, 0, index);
    }

    public COUIFullPageStatement(Context context, AttributeSet attributeSet, int index, int index_2) {
        super(context, attributeSet, index, index_2);
        this.mContext = context;
        if (attributeSet == null || attributeSet.getStyleAttribute() == 0) {
            this.mStyle = index;
        } else {
            this.mStyle = attributeSet.getStyleAttribute();
        }
        TypedArray typedArrayObtainStyledAttributes = this.mContext.obtainStyledAttributes(attributeSet, R.styleable.COUIFullPageStatement, index, index_2);
        String string = typedArrayObtainStyledAttributes.getString(R.styleable.COUIFullPageStatement_exitButtonText);
        String string2 = typedArrayObtainStyledAttributes.getString(R.styleable.COUIFullPageStatement_bottomButtonText);
        String string3 = typedArrayObtainStyledAttributes.getString(R.styleable.COUIFullPageStatement_couiFullPageStatementTitleText);
        this.mLayoutResourceId = typedArrayObtainStyledAttributes.getResourceId(R.styleable.COUIFullPageStatement_android_layout, R.layout.coui_full_page_statement);
        initView();
        this.mAppStatement.setText(typedArrayObtainStyledAttributes.getString(R.styleable.COUIFullPageStatement_appStatement));
        int color = typedArrayObtainStyledAttributes.getColor(R.styleable.COUIFullPageStatement_couiFullPageStatementTextButtonColor, 0);
        if (color != 0) {
            this.mExitTextView.setTextColor(color);
        }
        this.mAppStatement.setTextColor(typedArrayObtainStyledAttributes.getColor(R.styleable.COUIFullPageStatement_couiFullPageStatementTextColor, 0));
        if (string2 != null) {
            this.mBottomButtonNormal.setText(string2);
            if (isNotTinyStyle()) {
                this.mBottomButtonLand.setText(string2);
            }
        }
        if (string != null) {
            if (isNotTinyStyle()) {
                this.mExitButtonLand.setText(string);
            }
            this.mExitTextView.setText(string);
        }
        if (string3 != null) {
            this.mTitle.setText(string3);
        }
        typedArrayObtainStyledAttributes.recycle();
    }
}
