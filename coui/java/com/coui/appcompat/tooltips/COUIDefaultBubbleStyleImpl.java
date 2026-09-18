package com.coui.appcompat.tooltips;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.coui.appcompat.R;
import com.coui.appcompat.contextutil.COUIContextUtil;
import com.coui.appcompat.textutil.COUIChangeTextUtil;

public class COUIDefaultBubbleStyleImpl implements COUIIBubbleStyle {
    private CharSequence mContentText;
    private int mContentTextResId;
    private TextView mContentTv;
    private Context mContext;
    private final int[] mDefStyleParams = new int[2];
    private ImageView mDismissIv;
    private int mMode;
    private ScrollView mScrollView;
    private IToolTipsAction mToolTipsAction;

    public static class Builder {
        private CharSequence mContentText;
        private int mContentTextResId;

        public COUIDefaultBubbleStyleImpl build() {
            return new COUIDefaultBubbleStyleImpl(this);
        }

        public Builder setContentText(CharSequence contentText) {
            mContentText = contentText;
            mContentTextResId = 0;
            return this;
        }

        public Builder setContentTextRes(int resId) {
            mContentTextResId = resId;
            mContentText = null;
            return this;
        }
    }

    private COUIDefaultBubbleStyleImpl(Builder builder) {
        mContentText = builder.mContentText;
        mContentTextResId = builder.mContentTextResId;
    }

    @Override
    public void dismissWindow() {
    }

    @Override
    public TextView getContentView() {
        return mContentTv;
    }

    @Override
    public int[] getDefStyleParams() {
        if (mMode == COUIToolTips.MODE_INFO) {
            mDefStyleParams[0] = R.attr.couiToolTipsDetailFloatingStyle;
            mDefStyleParams[1] = COUIContextUtil.isCOUIDarkTheme(mContext)
                    ? R.style.COUIToolTips_DetailFloating_Dark
                    : R.style.COUIToolTips_DetailFloating;
        } else {
            mDefStyleParams[0] = R.attr.couiToolTipsStyle;
            mDefStyleParams[1] = COUIContextUtil.isCOUIDarkTheme(mContext)
                    ? R.style.COUIToolTips_Dark
                    : R.style.COUIToolTips;
        }
        return mDefStyleParams;
    }

    @Override
    public ImageView getDismissIv() {
        return mDismissIv;
    }

    @Override
    public int getLayoutId() {
        return R.layout.coui_tool_tips_layout;
    }

    @Override
    public int getMaxWidth() {
        return mContext.getResources().getDimensionPixelSize(R.dimen.tool_tips_max_width);
    }

    @Override
    public int getRealWidth(int width, ViewGroup viewGroup) {
        return Math.min(viewGroup.getMeasuredWidth(), width);
    }

    @Override
    public void hideDismissView() {
        if (mDismissIv != null) {
            mDismissIv.setVisibility(View.GONE);
        }
    }

    @Override
    public void initBubbleStyle(final ViewGroup viewGroup) {
        int[] defStyle = getDefStyleParams();
        TypedArray a = mContext.obtainStyledAttributes(null, R.styleable.COUIToolTips, defStyle[0], defStyle[1]);
        int gravity = a.getInt(R.styleable.COUIToolTips_couiToolTipsContainerLayoutGravity, 0);
        int marginStart = a.getDimensionPixelSize(R.styleable.COUIToolTips_couiToolTipsContainerLayoutMarginStart, 0);
        int marginTop = a.getDimensionPixelSize(R.styleable.COUIToolTips_couiToolTipsContainerLayoutMarginTop, 0);
        int marginEnd = a.getDimensionPixelSize(R.styleable.COUIToolTips_couiToolTipsContainerLayoutMarginEnd, 0);
        int marginBottom = a.getDimensionPixelSize(R.styleable.COUIToolTips_couiToolTipsContainerLayoutMarginBottom, 0);
        ColorStateList textColor = a.getColorStateList(R.styleable.COUIToolTips_couiToolTipsContentTextColor);
        a.recycle();

        mContentTv = viewGroup.findViewById(R.id.contentTv);
        mContentTv.setMovementMethod(LinkMovementMethod.getInstance());
        mScrollView = viewGroup.findViewById(R.id.scrollView);
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mScrollView.getLayoutParams();
        lp.gravity = gravity;
        lp.setMargins(marginStart, marginTop, marginEnd, marginBottom);
        lp.setMarginStart(marginStart);
        lp.setMarginEnd(marginEnd);
        mScrollView.setLayoutParams(lp);
        int textSize = mContext.getResources().getDimensionPixelSize(
                mMode == COUIToolTips.MODE_TOOLTIPS
                        ? R.dimen.tool_tips_content_text_size
                        : R.dimen.detail_floating_content_text_size);
        mContentTv.setTextSize(0, COUIChangeTextUtil.getSuitableFontSize(
                textSize, mContext.getResources().getConfiguration().fontScale, 4));
        if (textColor != null) {
            mContentTv.setTextColor(textColor);
        }
        if (mContentTextResId != 0) {
            mContentTv.setText(mContext.getString(mContentTextResId));
        } else if (!TextUtils.isEmpty(mContentText)) {
            mContentTv.setText(mContentText);
        }
        mDismissIv = viewGroup.findViewById(R.id.dismissIv);
        if (mMode == COUIToolTips.MODE_TOOLTIPS) {
            mDismissIv.setVisibility(View.VISIBLE);
            mDismissIv.setOnClickListener(v -> {
                if (mToolTipsAction != null) {
                    mToolTipsAction.onCloseClick();
                }
            });
        } else {
            mDismissIv.setVisibility(View.GONE);
        }
        int inset = mContext.getResources().getDimensionPixelOffset(R.dimen.couiToolTipsCancelButtonInsects);
        mDismissIv.post(() -> {
            Rect rect = new Rect();
            mDismissIv.getHitRect(rect);
            rect.inset(-inset, -inset);
            viewGroup.setTouchDelegate(new TouchDelegate(rect, mDismissIv));
        });
    }

    @Override
    public void refreshBubbleStyle(ColorStateList colorStateList) {
        if (mContentTv != null && colorStateList != null) {
            mContentTv.setTextColor(colorStateList);
        }
    }

    @Override
    public void refreshTextResources() {
        if (mContext != null && mContentTextResId != 0 && mContentTv != null) {
            mContentTv.setText(mContext.getString(mContentTextResId));
        }
    }

    @Override
    public void setContentText(CharSequence charSequence) {
        mContentTextResId = 0;
        if (mContentTv != null) {
            mContentTv.setText(charSequence);
        } else {
            mContentText = charSequence;
        }
    }

    public void setContentTextColor(int color) {
        setContentTextColor(ColorStateList.valueOf(color));
    }

    @Override
    public void setContentTextColor(ColorStateList colorStateList) {
        if (mContentTv != null) {
            mContentTv.setTextColor(colorStateList);
        }
    }

    @Override
    public void setContentTextRes(int resId) {
        mContentTextResId = resId;
        if (mContentTv != null && mContext != null && resId != 0) {
            mContentTv.setText(mContext.getString(resId));
        }
    }

    @Override
    public void setContentView(View view) {
        if (mScrollView != null) {
            mScrollView.removeAllViews();
            mScrollView.addView(view);
        }
    }

    @Override
    public void setToolTipsAction(IToolTipsAction action, Context context, int mode) {
        mToolTipsAction = action;
        mContext = context;
        mMode = mode;
    }

    @Override
    public void sizeBubbleStyle(ViewGroup viewGroup, int width) {
        if (mScrollView == null || mContentTv == null) {
            return;
        }
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mScrollView.getLayoutParams();
        mContentTv.setMaxWidth(width - viewGroup.getPaddingLeft() - viewGroup.getPaddingRight()
                - lp.leftMargin - lp.rightMargin);
    }
}
