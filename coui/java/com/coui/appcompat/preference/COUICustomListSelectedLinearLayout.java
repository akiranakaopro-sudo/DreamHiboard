package com.coui.appcompat.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;

import com.coui.appcompat.R;
import com.coui.appcompat.cardlist.COUICardListSelectedItemLayout;
import com.coui.appcompat.uiutil.UIUtil;

public class COUICustomListSelectedLinearLayout extends COUICardListSelectedItemLayout {
    public static final int REMAINING_TOTAL_LINE = 2;
    private boolean mIconMarginDependOnImageView;
    private int mMarginBetweenLine;
    private int mTextContentPaddingTop;
    private boolean mWithDividerItem;

    public COUICustomListSelectedLinearLayout(Context context) {
        this(context, null);
    }

    public COUICustomListSelectedLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mWithDividerItem = false;
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setOrientation(HORIZONTAL);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.COUICustomListSelectedLinearLayout);
        mWithDividerItem = a.getBoolean(R.styleable.COUICustomListSelectedLinearLayout_couiPreferenceWithDividerItem, false);
        a.recycle();
        mTextContentPaddingTop = getContext().getResources().getDimensionPixelSize(R.dimen.support_preference_text_content_padding_top);
        mMarginBetweenLine = getContext().getResources().getDimensionPixelSize(R.dimen.support_preference_margin_between_line);
    }

    private int checkFirstTwoLineCenter(TextView textView, int top, int remainingLines, float[] centers) {
        if (remainingLines == 0 || textView == null || textView.getVisibility() != VISIBLE || TextUtils.isEmpty(textView.getText())) {
            return remainingLines;
        }
        if (remainingLines == 1) {
            centers[1] = top + textView.getLayout().getLineBottom(0);
            return 0;
        }
        centers[0] = top + textView.getLayout().getLineTop(0);
        int remaining = remainingLines - 1;
        if (textView.getLayout().getLineCount() < 2) {
            return remaining;
        }
        centers[1] = top + textView.getLayout().getLineBottom(1);
        return remaining - 1;
    }

    private boolean operateMultilineIconPosition() {
        View imgLayout = findViewById(R.id.img_layout);
        AppCompatImageView iconView = findViewById(android.R.id.icon);
        if (imgLayout == null || imgLayout.getVisibility() != VISIBLE || iconView == null) {
            return false;
        }
        boolean hasMessageLayout = findViewById(R.id.messageLayout) != null;
        TextView title = findViewById(android.R.id.title);
        TextView summary = findViewById(android.R.id.summary);
        int titleHeight;
        int titleLineCount;
        if (title == null || title.getVisibility() != VISIBLE) {
            titleHeight = 0;
            titleLineCount = 0;
        } else {
            titleLineCount = title.getLineCount();
            titleHeight = title.getMeasuredHeight() + mMarginBetweenLine;
        }
        int summaryHeight;
        int summaryLineCount;
        if (summary == null || summary.getVisibility() != VISIBLE) {
            summaryHeight = 0;
            summaryLineCount = 0;
        } else {
            summaryLineCount = summary.getLineCount();
            summaryHeight = summary.getMeasuredHeight() + mMarginBetweenLine;
        }
        TextView assignment = findViewById(R.id.assignment);
        int assignmentLineCount = (hasMessageLayout || assignment == null || assignment.getVisibility() != VISIBLE)
                ? 0 : assignment.getLineCount();
        int iconSizeDp = UIUtil.px2dip(getContext(), mIconMarginDependOnImageView
                ? iconView.getMeasuredHeight()
                : iconView.getDrawable() != null ? iconView.getDrawable().getIntrinsicHeight() : 0);
        int totalLineCount = titleLineCount + summaryLineCount + assignmentLineCount;
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) imgLayout.getLayoutParams();
        if (totalLineCount > REMAINING_TOTAL_LINE) {
            setIconTopInParent(lp, titleHeight, summaryHeight, imgLayout);
        } else {
            setIconCenterInParent(lp, iconSizeDp, totalLineCount);
        }
        imgLayout.setLayoutParams(lp);
        return true;
    }

    private void setIconCenterInParent(LinearLayout.LayoutParams lp, int iconSizeDp, int lineCount) {
        lp.gravity = android.view.Gravity.CENTER_VERTICAL;
        int margin = getContext().getResources().getDimensionPixelSize(R.dimen.coui_preference_icon_margin_top);
        if (iconSizeDp == 24) {
            margin = lineCount <= 1
                    ? getContext().getResources().getDimensionPixelSize(R.dimen.coui_preference_24icon_margin_vertical_default)
                    : getContext().getResources().getDimensionPixelSize(R.dimen.coui_preference_24icon_margin_top_multiline);
        } else if (iconSizeDp == 32) {
            margin = lineCount <= 1
                    ? getContext().getResources().getDimensionPixelSize(R.dimen.coui_preference_32icon_margin_vertical_default)
                    : getContext().getResources().getDimensionPixelSize(R.dimen.coui_preference_32icon_margin_top_multiline);
        } else if (iconSizeDp == 36) {
            margin = lineCount <= 1
                    ? getContext().getResources().getDimensionPixelSize(R.dimen.coui_preference_36icon_margin_vertical_default)
                    : getContext().getResources().getDimensionPixelSize(R.dimen.coui_preference_36icon_margin_top_multiline);
        } else if (iconSizeDp == 50) {
            margin = getContext().getResources().getDimensionPixelSize(R.dimen.coui_preference_50icon_margin_vertical_default);
        }
        if (lp.topMargin != margin || lp.bottomMargin != margin) {
            lp.topMargin = margin;
            lp.bottomMargin = margin;
        }
    }

    private void setIconTopInParent(LinearLayout.LayoutParams lp, int titleHeight, int summaryHeight, View iconLayout) {
        lp.gravity = android.view.Gravity.TOP;
        float[] centers = new float[2];
        int firstTop = mTextContentPaddingTop;
        int summaryTop = firstTop + titleHeight;
        int remaining = checkFirstTwoLineCenter((TextView) findViewById(android.R.id.title), firstTop, REMAINING_TOTAL_LINE, centers);
        remaining = checkFirstTwoLineCenter((TextView) findViewById(android.R.id.summary), summaryTop, remaining, centers);
        checkFirstTwoLineCenter((TextView) findViewById(R.id.assignment), firstTop + titleHeight + summaryHeight, remaining, centers);
        int margin = Math.max((int) (((centers[0] + centers[1]) / 2.0f) - (iconLayout.getMeasuredHeight() / 2.0f)),
                getContext().getResources().getDimensionPixelSize(R.dimen.coui_preference_50icon_margin_vertical_default));
        if (lp.topMargin != margin) {
            lp.topMargin = margin;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (operateMultilineIconPosition()) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mWithDividerItem) {
            return true;
        }
        return super.onTouchEvent(event);
    }

    public void setIconMarginDependOnImageView(boolean dependOnImageView) {
        mIconMarginDependOnImageView = dependOnImageView;
    }
}
