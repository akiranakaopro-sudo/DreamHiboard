package com.coui.appcompat.tooltips;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.coui.appcompat.R;
import com.coui.appcompat.contextutil.COUIContextUtil;
import com.coui.appcompat.textviewcompatutil.COUITextViewCompatUtil;

public class COUIIconBubbleStyleImpl implements COUIIBubbleStyle {
    private CharSequence mContentText;
    private int mContentTextResId;
    private TextView mContentTv;
    private Context mContext;
    private final int[] mDefStyleParams = new int[2];
    private CharSequence mDismissText;
    private int mDismissTextResId;
    private Bitmap mIconBitmap;
    private Drawable mIconDrawable;
    private int mIconResId;
    private ImageView mIvIcon;
    private ScrollView mScrollView;
    private CharSequence mTitle;
    private int mTitleResId;
    private IToolTipsAction mToolTipsAction;
    private TextView mTvDismiss;
    private TextView mTvTitle;

    public static class Builder {
        private CharSequence mContentText;
        private int mContentTextResId;
        private CharSequence mDismissText;
        private int mDismissTextResId;
        private Bitmap mIconBitmap;
        private Drawable mIconDrawable;
        private int mIconResId;
        private CharSequence mTitle;
        private int mTitleResId;

        public COUIIconBubbleStyleImpl build() {
            return new COUIIconBubbleStyleImpl(this);
        }

        public Builder loadIcon(int resId) {
            mIconResId = resId;
            return this;
        }

        public Builder loadIcon(Bitmap bitmap) {
            mIconBitmap = bitmap;
            return this;
        }

        public Builder loadIcon(Drawable drawable) {
            mIconDrawable = drawable;
            return this;
        }

        public Builder setTitle(CharSequence title) {
            mTitle = title;
            mTitleResId = 0;
            return this;
        }

        public Builder setTitleRes(int resId) {
            mTitleResId = resId;
            mTitle = null;
            return this;
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

        public Builder setDismissText(CharSequence dismissText) {
            mDismissText = dismissText;
            mDismissTextResId = 0;
            return this;
        }

        public Builder setDismissTextRes(int resId) {
            mDismissTextResId = resId;
            mDismissText = null;
            return this;
        }
    }

    private COUIIconBubbleStyleImpl(Builder builder) {
        mIconResId = builder.mIconResId;
        mIconDrawable = builder.mIconDrawable;
        mIconBitmap = builder.mIconBitmap;
        mTitle = builder.mTitle;
        mContentText = builder.mContentText;
        mDismissText = builder.mDismissText;
        mTitleResId = builder.mTitleResId;
        mContentTextResId = builder.mContentTextResId;
        mDismissTextResId = builder.mDismissTextResId;
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
        mDefStyleParams[0] = R.attr.couiToolTipsIconStyle;
        mDefStyleParams[1] = R.style.COUIToolTips_Icon;
        return mDefStyleParams;
    }

    @Override
    public int getLayoutId() {
        return R.layout.coui_tool_tips_icon_style_layout;
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
    public void initBubbleStyle(ViewGroup viewGroup) {
        int[] defStyle = getDefStyleParams();
        TypedArray a = mContext.obtainStyledAttributes(null, R.styleable.COUIToolTips, defStyle[0], defStyle[1]);
        ColorStateList contentTextColor = a.getColorStateList(R.styleable.COUIToolTips_couiToolTipsContentTextColor);
        a.recycle();
        mIvIcon = viewGroup.findViewById(R.id.iv_icon);
        mTvTitle = viewGroup.findViewById(R.id.tv_title);
        mScrollView = viewGroup.findViewById(R.id.scrollView);
        mContentTv = viewGroup.findViewById(R.id.contentTv);
        mTvDismiss = viewGroup.findViewById(R.id.tv_dismiss);
        refreshIconResource();
        bindText(mTvTitle, mTitleResId, mTitle);
        mContentTv.setMovementMethod(LinkMovementMethod.getInstance());
        if (contentTextColor != null) {
            mContentTv.setTextColor(contentTextColor);
        }
        bindText(mContentTv, mContentTextResId, mContentText);
        bindText(mTvDismiss, mDismissTextResId, mDismissText);
        mTvDismiss.setOnClickListener(v -> {
            if (mToolTipsAction != null) {
                mToolTipsAction.onCloseClick();
            }
        });
        COUITextViewCompatUtil.setPressRippleDrawable(mTvDismiss);
    }

    private void bindText(TextView textView, int resId, CharSequence text) {
        if (resId != 0) {
            textView.setText(mContext.getString(resId));
        } else if (!TextUtils.isEmpty(text)) {
            textView.setText(text);
        }
    }

    private void refreshIconResource() {
        if (mIvIcon == null) {
            return;
        }
        if (mIconResId != 0) {
            mIvIcon.setImageResource(mIconResId);
        } else if (mIconDrawable != null) {
            mIvIcon.setImageDrawable(mIconDrawable);
        } else if (mIconBitmap != null) {
            mIvIcon.setImageBitmap(mIconBitmap);
        }
    }

    public void loadIcon(int resId) {
        mIconResId = resId;
        mIconDrawable = null;
        mIconBitmap = null;
        refreshIconResource();
    }

    public void loadIcon(Drawable drawable) {
        mIconResId = 0;
        mIconDrawable = drawable;
        mIconBitmap = null;
        refreshIconResource();
    }

    public void loadIcon(Bitmap bitmap) {
        mIconResId = 0;
        mIconDrawable = null;
        mIconBitmap = bitmap;
        refreshIconResource();
    }

    @Override
    public void refreshBubbleStyle(ColorStateList colorStateList) {
        if (mContentTv != null && colorStateList != null) {
            mContentTv.setTextColor(colorStateList);
        }
        if (mTvTitle != null && mContext != null) {
            mTvTitle.setTextColor(COUIContextUtil.getAttrColor(mContext, R.attr.couiColorLabelPrimary));
        }
        if (mTvDismiss != null && mContext != null) {
            mTvDismiss.setTextColor(COUIContextUtil.getAttrColor(mContext, R.attr.couiColorLabelTheme));
        }
    }

    @Override
    public void refreshTextResources() {
        if (mContext == null) {
            return;
        }
        if (mTitleResId != 0 && mTvTitle != null) {
            mTvTitle.setText(mContext.getString(mTitleResId));
        }
        if (mContentTextResId != 0 && mContentTv != null) {
            mContentTv.setText(mContext.getString(mContentTextResId));
        }
        if (mDismissTextResId != 0 && mTvDismiss != null) {
            mTvDismiss.setText(mContext.getString(mDismissTextResId));
        }
        refreshIconResource();
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

    public void setDismissButtonTextRes(int resId) {
        setDismissTextRes(resId);
    }

    public void setDismissText(CharSequence charSequence) {
        mDismissTextResId = 0;
        if (mTvDismiss != null) {
            mTvDismiss.setText(charSequence);
        } else {
            mDismissText = charSequence;
        }
    }

    @Override
    public void setDismissTextRes(int resId) {
        mDismissTextResId = resId;
        if (mTvDismiss != null && mContext != null && resId != 0) {
            mTvDismiss.setText(mContext.getString(resId));
        }
    }

    public void setTitle(CharSequence title) {
        mTitleResId = 0;
        if (mTvTitle != null) {
            mTvTitle.setText(title);
        } else {
            mTitle = title;
        }
    }

    @Override
    public void setTitleRes(int resId) {
        mTitleResId = resId;
        if (mTvTitle != null && mContext != null && resId != 0) {
            mTvTitle.setText(mContext.getString(resId));
        }
    }

    public void setTitleTextRes(int resId) {
        setTitleRes(resId);
    }

    @Override
    public void setToolTipsAction(IToolTipsAction action, Context context, int mode) {
        mToolTipsAction = action;
        mContext = context;
    }

    @Override
    public void sizeBubbleStyle(ViewGroup viewGroup, int width) {
        if (mContentTv == null || mTvTitle == null || mTvDismiss == null) {
            return;
        }
        RelativeLayout.LayoutParams contentLp = (RelativeLayout.LayoutParams) mContentTv.getLayoutParams();
        mContentTv.setMaxWidth(width - viewGroup.getPaddingLeft() - viewGroup.getPaddingRight()
                - contentLp.getMarginStart() - contentLp.getMarginEnd());
        RelativeLayout.LayoutParams titleLp = (RelativeLayout.LayoutParams) mTvTitle.getLayoutParams();
        mTvTitle.setMaxWidth(width - viewGroup.getPaddingLeft() - viewGroup.getPaddingRight()
                - titleLp.getMarginStart() - titleLp.getMarginEnd());
        LinearLayout.LayoutParams dismissLp = (LinearLayout.LayoutParams) mTvDismiss.getLayoutParams();
        mTvDismiss.setMaxWidth(width - viewGroup.getPaddingLeft() - viewGroup.getPaddingRight()
                - dismissLp.getMarginStart() - dismissLp.getMarginEnd());
    }
}
