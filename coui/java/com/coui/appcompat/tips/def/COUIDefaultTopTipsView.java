package com.coui.appcompat.tips.def;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.view.ViewCompat;

import com.coui.appcompat.R;
import com.coui.appcompat.textviewcompatutil.COUITextViewCompatUtil;
import com.coui.appcompat.tips.COUIMarqueeTextView;

public class COUIDefaultTopTipsView extends ConstraintLayout implements IDefaultTopTips {
    public static final int ICON_ID = 0;
    public static final int TITLE_ID = 1;
    public static final int IGNORE_ID = 2;
    public static final int ACTION_ID = 3;
    public static final int CLOSE_ID = 4;
    public static final int TEXT_BTN_TYPE = 0;
    public static final int IMAGE_BTN_TYPE = 1;
    private static final int TEXT_BTN_ACTION_WRAP = 1;
    private static final int TEXT_BTN_IGNORE_WRAP = 2;

    private final ConstraintSet end = new ConstraintSet();
    private TextView mAction;
    private ImageView mClose;
    private View.OnClickListener mCloseBtnClickListener;
    private int mContentLines = -1;
    private TextView mIgnore;
    private ImageView mImage;
    private boolean mIsChangeText = true;
    private int mMultiTitleTopMargin;
    private View.OnClickListener mNegativeClickListener;
    private OnLinesChangedListener mOnLinesChangedListener;
    private View.OnClickListener mPositiveClickListener;
    private int mTextBtnRuleFlag;
    private COUIMarqueeTextView mTitle;
    private int mType = TEXT_BTN_TYPE;

    public COUIDefaultTopTipsView(Context context) {
        this(context, null);
    }

    public COUIDefaultTopTipsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public COUIDefaultTopTipsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void changeBtnTypeImpl() {
        end.clone(this);
        int title = R.id.title;
        int close = R.id.close;
        end.connect(title, ConstraintSet.END, close, ConstraintSet.START);
        end.setMargin(title, ConstraintSet.END, getResources().getDimensionPixelSize(R.dimen.coui_toptips_view_btn_margin_right));
        end.connect(title, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
        end.connect(R.id.ignore, ConstraintSet.TOP, title, ConstraintSet.TOP);
        end.setMargin(R.id.ignore, ConstraintSet.TOP, 0);
        end.connect(R.id.action, ConstraintSet.TOP, title, ConstraintSet.TOP);
        end.setMargin(R.id.action, ConstraintSet.TOP, 0);
        end.setVisibility(close, VISIBLE);
        end.setVisibility(R.id.ignore, TextUtils.isEmpty(mIgnore.getText()) ? GONE : INVISIBLE);
        end.setVisibility(R.id.action, TextUtils.isEmpty(mAction.getText()) ? GONE : INVISIBLE);
        end.applyTo(this);
    }

    private void changeTextTypeImpl() {
        end.clone(this);
        int title = R.id.title;
        if (isNeedMultiText()) {
            end.connect(title, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
            end.connect(title, ConstraintSet.BOTTOM, TextUtils.isEmpty(mAction.getText()) && TextUtils.isEmpty(mIgnore.getText())
                    ? ConstraintSet.PARENT_ID : ConstraintSet.UNSET, ConstraintSet.BOTTOM);
            end.setMargin(title, ConstraintSet.END, getResources().getDimensionPixelSize(R.dimen.coui_toptips_view_title_end_margin));
            connectMultiButton(R.id.ignore);
            connectMultiButton(R.id.action);
            end.connect(R.id.image, ConstraintSet.BOTTOM, ConstraintSet.UNSET, ConstraintSet.BOTTOM);
            end.connect(R.id.image, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
            end.setMargin(R.id.image, ConstraintSet.TOP, getImageMarginTopMultiText());
        } else {
            end.connect(title, ConstraintSet.END, R.id.ignore, ConstraintSet.START);
            end.connect(title, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
            end.setMargin(title, ConstraintSet.END, getResources().getDimensionPixelSize(R.dimen.coui_toptips_view_btn_margin_right));
            connectSingleButton(R.id.ignore, title);
            connectSingleButton(R.id.action, title);
            end.connect(R.id.image, ConstraintSet.TOP, title, ConstraintSet.TOP);
            end.connect(R.id.image, ConstraintSet.BOTTOM, title, ConstraintSet.BOTTOM);
            end.setMargin(R.id.image, ConstraintSet.TOP, 0);
        }
        if (mOnLinesChangedListener != null && mContentLines != mTitle.getLineCount()) {
            mContentLines = mTitle.getLineCount();
            mOnLinesChangedListener.onLinesChanged(mContentLines);
        }
        end.setVisibility(R.id.close, INVISIBLE);
        end.setVisibility(R.id.ignore, TextUtils.isEmpty(mIgnore.getText()) ? GONE : VISIBLE);
        end.setVisibility(R.id.action, TextUtils.isEmpty(mAction.getText()) ? GONE : VISIBLE);
        end.applyTo(this);
    }

    private void connectMultiButton(int id) {
        end.connect(id, ConstraintSet.TOP, R.id.title, ConstraintSet.BOTTOM);
        end.connect(id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
        end.setMargin(id, ConstraintSet.TOP, getResources().getDimensionPixelSize(R.dimen.coui_toptips_view_btn_top_margin));
        end.setMargin(id, ConstraintSet.BOTTOM, getResources().getDimensionPixelSize(R.dimen.coui_toptips_view_multi_btn_text_bottom_margin));
    }

    private void connectSingleButton(int id, int title) {
        end.connect(id, ConstraintSet.TOP, title, ConstraintSet.TOP);
        end.connect(id, ConstraintSet.BOTTOM, title, ConstraintSet.BOTTOM);
        end.setMargin(id, ConstraintSet.TOP, 0);
        end.setMargin(id, ConstraintSet.BOTTOM, 0);
    }

    private int getImageMarginTopMultiText() {
        float center = mTitle.getLayout() != null
                ? ((mTitle.getLayout().getLineTop(0) + mTitle.getLayout().getLineBottom(0)) / 2.0f) + mTitle.getY()
                : 0.0f;
        return Math.max((int) (center - (mImage.getMeasuredHeight() / 2.0f)), mMultiTitleTopMargin);
    }

    private boolean isNeedMultiText() {
        if (mTitle.getMaxLines() == 1 || mTitle.getLayout() == null || TextUtils.isEmpty(mTitle.getText())) {
            return false;
        }
        float titleWidth = mTitle.getPaint().measureText(mTitle.getText().toString());
        if (titleWidth >= (mTitle.getWidth() - mTitle.getPaddingStart()) - mTitle.getPaddingEnd()) {
            return true;
        }
        if (TextUtils.isEmpty(mAction.getText()) && TextUtils.isEmpty(mIgnore.getText())) {
            return false;
        }
        int width = (getWidth() - (((LayoutParams) mTitle.getLayoutParams()).getMarginStart() + mImage.getWidth()))
                - getResources().getDimensionPixelSize(R.dimen.coui_toptips_view_btn_margin_right);
        TextView button = !TextUtils.isEmpty(mIgnore.getText()) ? mIgnore : mAction;
        float buttonWidth = button.getMeasuredWidth();
        if (buttonWidth <= 0.0f && !TextUtils.isEmpty(button.getText())) {
            buttonWidth = button.getPaint().measureText(button.getText().toString())
                    + button.getPaddingStart() + button.getPaddingEnd();
        }
        return titleWidth + buttonWidth > width;
    }

    private void remeasureTextBtnWidth(TextView textView, int width) {
        textView.measure(ViewGroup.getChildMeasureSpec(
                        MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST), 0, LayoutParams.WRAP_CONTENT),
                ViewGroup.getChildMeasureSpec(
                        MeasureSpec.makeMeasureSpec(textView.getMeasuredHeight(), MeasureSpec.AT_MOST), 0, LayoutParams.WRAP_CONTENT));
    }

    private void setBtnColorImpl(int which, int color) {
        if (which == IGNORE_ID) {
            mIgnore.setTextColor(color);
        } else if (which == ACTION_ID) {
            mAction.setTextColor(color);
        } else {
            throw new IllegalArgumentException("setBtnColorImpl parameter 'which' is wrong");
        }
    }

    private void setBtnDrawableImpl(int which, Drawable drawable) {
        if (which != CLOSE_ID) {
            throw new IllegalArgumentException("setBtnDrawableImpl parameter 'which' is wrong");
        }
        mClose.setImageDrawable(drawable);
        changeType(IMAGE_BTN_TYPE);
    }

    private void setBtnTextImpl(int which, CharSequence text) {
        if (which == IGNORE_ID) {
            mIgnore.setText(text);
            changeType(TEXT_BTN_TYPE);
        } else if (which == ACTION_ID) {
            mAction.setText(text);
            changeType(TEXT_BTN_TYPE);
        } else {
            throw new IllegalArgumentException("setBtnTextImpl parameter 'which' is wrong");
        }
    }

    public final void changeType(int type) {
        if (type == TEXT_BTN_TYPE) {
            changeTextTypeImpl();
        } else {
            changeBtnTypeImpl();
        }
        mType = type;
    }

    public TextView getAction() { return mAction; }
    public TextView getIgnore() { return mIgnore; }
    public TextView getTitle() { return mTitle; }

    public void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.coui_default_toptips, this);
        mImage = findViewById(R.id.image);
        mTitle = findViewById(R.id.title);
        mIgnore = findViewById(R.id.ignore);
        COUITextViewCompatUtil.setPressRippleDrawable(mIgnore);
        mIgnore.setOnClickListener(view -> {
            if (mNegativeClickListener != null) mNegativeClickListener.onClick(view);
        });
        mAction = findViewById(R.id.action);
        COUITextViewCompatUtil.setPressRippleDrawable(mAction);
        mAction.setOnClickListener(view -> {
            if (mPositiveClickListener != null) mPositiveClickListener.onClick(view);
        });
        mClose = findViewById(R.id.close);
        mClose.setOnClickListener(view -> {
            if (mCloseBtnClickListener != null) mCloseBtnClickListener.onClick(view);
        });
        mMultiTitleTopMargin = getResources().getDimensionPixelSize(R.dimen.coui_toptips_view_multi_title_top_margin);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL) {
            mAction.layout(mAction.getLeft(), mAction.getTop(), mAction.getLeft() + mAction.getMeasuredWidth(), mAction.getBottom());
            mIgnore.layout(mAction.getRight(), mIgnore.getTop(), mAction.getRight() + mIgnore.getMeasuredWidth(), mIgnore.getBottom());
        } else {
            mAction.layout(mAction.getRight() - mAction.getMeasuredWidth(), mAction.getTop(), mAction.getRight(), mAction.getBottom());
            mIgnore.layout(mAction.getLeft() - mIgnore.getMeasuredWidth(), mIgnore.getTop(), mAction.getLeft(), mIgnore.getBottom());
        }
        if (mType == TEXT_BTN_TYPE && mIsChangeText) {
            mIsChangeText = false;
            changeTextTypeImpl();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measuredWidth = getMeasuredWidth()
                - ((((LayoutParams) mTitle.getLayoutParams()).getMarginStart() + mImage.getMeasuredWidth())
                + ((LayoutParams) mImage.getLayoutParams()).getMarginStart());
        int half = measuredWidth >> 1;
        if (mAction.getMeasuredWidth() <= half) mTextBtnRuleFlag++;
        if (mIgnore.getMeasuredWidth() <= half) mTextBtnRuleFlag += 2;
        if (mTextBtnRuleFlag == 0) {
            remeasureTextBtnWidth(mAction, half);
            remeasureTextBtnWidth(mIgnore, half);
        } else if (mTextBtnRuleFlag == TEXT_BTN_ACTION_WRAP) {
            remeasureTextBtnWidth(mIgnore, measuredWidth - mAction.getMeasuredWidth());
        } else if (mTextBtnRuleFlag == TEXT_BTN_IGNORE_WRAP) {
            remeasureTextBtnWidth(mAction, measuredWidth - mIgnore.getMeasuredWidth());
        }
        mTextBtnRuleFlag = 0;
    }

    @Override public void setCloseBtnListener(View.OnClickListener listener) { mCloseBtnClickListener = listener; }
    @Override public void setCloseDrawable(Drawable drawable) { setBtnDrawableImpl(CLOSE_ID, drawable); }
    @Override public void setNegativeButton(CharSequence text) { setBtnTextImpl(IGNORE_ID, text); }
    @Override public void setNegativeButtonColor(int color) { setBtnColorImpl(IGNORE_ID, color); }
    @Override public void setNegativeButtonListener(View.OnClickListener listener) { mNegativeClickListener = listener; }
    public void setOnLinesChangedListener(OnLinesChangedListener listener) { mOnLinesChangedListener = listener; }
    @Override public void setPositiveButton(CharSequence text) { setBtnTextImpl(ACTION_ID, text); }
    @Override public void setPositiveButtonColor(int color) { setBtnColorImpl(ACTION_ID, color); }
    @Override public void setPositiveButtonListener(View.OnClickListener listener) { mPositiveClickListener = listener; }
    @Override public void setStartIcon(Drawable drawable) { mImage.setImageDrawable(drawable); }
    @Override public void setTipsText(CharSequence text) { mIsChangeText = true; mTitle.setText(text); }
    @Override public void setTipsTextColor(int color) { mTitle.setTextColor(color); }
    public void startRoll() { mTitle.continueRoll(); }
    public void stopRoll() { mTitle.stopRoll(); mTitle.setMarqueeEnable(false); }
}
