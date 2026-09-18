package com.coui.appcompat.preference;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.preference.PreferenceViewHolder;

import com.airbnb.lottie.LottieAnimationView;
import com.coui.appcompat.R;
import com.coui.appcompat.textview.COUITextView;

public class COUIStatusSwitchingPreference extends COUIPreference {
    private static final int STATUS_TYPE_ANIM = 1;
    private static final int STATUS_TYPE_DEFAULT = 0;
    private static final int STATUS_TYPE_IMAGE = 3;
    private static final int STATUS_TYPE_TEXT = 2;

    // Leapy modified 2026-07-26: Use Lottie for the decoded OPPO status
    // animation state instead of the placeholder Effective view.
    private LottieAnimationView mAnimView;
    private int mAnimViewHeight;
    private int mAnimViewWidth;
    private int mCurrentType;
    private Drawable mImage;
    private ImageView mImageView;
    private int mRawResId;
    private CharSequence mText;
    private COUITextView mTextView;

    public COUIStatusSwitchingPreference(Context context) {
        this(context, null);
    }

    public COUIStatusSwitchingPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.couiStatusSwitchingPreferenceStyle);
    }

    public COUIStatusSwitchingPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, defStyleAttr);
    }

    public COUIStatusSwitchingPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
        mCurrentType = STATUS_TYPE_DEFAULT;
        mAnimViewWidth = ViewGroup.LayoutParams.WRAP_CONTENT;
        mAnimViewHeight = ViewGroup.LayoutParams.WRAP_CONTENT;
    }

    private void executePlayAnim() {
        LottieAnimationView animView = mAnimView;
        if (animView != null) {
            animView.setVisibility(View.VISIBLE);
            mTextView.setVisibility(View.GONE);
            mImageView.setVisibility(View.GONE);
            setAnimViewSize();
            playAnimView();
        }
    }

    private void executeShowImage() {
        if (mImageView == null || mImage == null) {
            return;
        }
        stopAnim();
        mAnimView.setVisibility(View.GONE);
        mTextView.setVisibility(View.GONE);
        mImageView.setVisibility(View.VISIBLE);
        mImageView.setImageDrawable(mImage);
    }

    private void executeShowText() {
        if (mTextView != null) {
            stopAnim();
            mAnimView.setVisibility(View.GONE);
            mTextView.setVisibility(View.VISIBLE);
            mImageView.setVisibility(View.GONE);
            mTextView.setText(mText);
        }
    }

    private void playAnimView() {
        stopAnim();
        mAnimView.setAnimation(mRawResId);
        mAnimView.loop(true);
        mAnimView.playAnimation();
    }

    private void setAnimViewSize() {
        ViewGroup.LayoutParams layoutParams = mAnimView.getLayoutParams();
        layoutParams.width = mAnimViewWidth;
        layoutParams.height = mAnimViewHeight;
        mAnimView.setLayoutParams(layoutParams);
    }

    private void stopAnim() {
        LottieAnimationView animView = mAnimView;
        if (animView == null || !animView.isAnimating()) {
            return;
        }
        mAnimView.cancelAnimation();
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        mAnimView = (LottieAnimationView) holder.findViewById(R.id.coui_anim);
        mTextView = (COUITextView) holder.findViewById(R.id.coui_text);
        mImageView = (ImageView) holder.findViewById(R.id.coui_image);
        int type = mCurrentType;
        if (type == STATUS_TYPE_ANIM) {
            executePlayAnim();
        } else if (type == STATUS_TYPE_TEXT) {
            executeShowText();
        } else if (type == STATUS_TYPE_IMAGE) {
            executeShowImage();
        }
    }

    public void showAnim(int rawResId) {
        mCurrentType = STATUS_TYPE_ANIM;
        mRawResId = rawResId;
        notifyChanged();
    }

    public void showAnim(int rawResId, int width, int height) {
        mCurrentType = STATUS_TYPE_ANIM;
        mRawResId = rawResId;
        mAnimViewWidth = width;
        mAnimViewHeight = height;
        notifyChanged();
    }

    public void showIcon(Drawable drawable) {
        if (drawable != mImage) {
            mCurrentType = STATUS_TYPE_IMAGE;
            mImage = drawable;
            notifyChanged();
        }
    }

    public void showText(CharSequence text) {
        if (TextUtils.equals(text, mText)) {
            return;
        }
        mCurrentType = STATUS_TYPE_TEXT;
        mText = text;
        notifyChanged();
    }
}
