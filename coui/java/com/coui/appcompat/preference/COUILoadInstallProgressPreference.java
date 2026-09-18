package com.coui.appcompat.preference;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;

import androidx.preference.PreferenceViewHolder;

import com.coui.appcompat.R;
import com.coui.appcompat.contextutil.COUIContextUtil;
import com.coui.appcompat.progressbar.COUIInstallLoadProgress;
import com.coui.appcompat.progressbar.COUILoadProgress;
import com.coui.appcompat.statelistutil.COUIStateListUtil;
import com.coui.appcompat.textutil.COUIChangeTextUtil;

public class COUILoadInstallProgressPreference extends COUIPreference {
    private final int mDisabledColor;
    private ColorStateList mInstallBackgroundColorStateList;
    private int mInstallProgressTextColor;
    private COUILoadProgress.OnStateChangeListener mListener;
    private COUIInstallLoadProgress mLoadProgress;
    private int mMax;
    private OnStateChangeListener mOnStateChangeListener;
    private int mProgress;
    private ColorStateList mProgressBackgroundColorStateList;
    private CharSequence mProgressText;
    private ColorStateList mProgressTextColorStateList;
    private int mProgressTextSize;
    private int mState;

    public interface OnStateChangeListener {
        void onStateChanged(COUILoadProgress loadProgress, int state);
    }

    public COUILoadInstallProgressPreference(Context context) {
        this(context, null);
    }

    public COUILoadInstallProgressPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.couiLoadInstallProgressPreferenceStyle);
    }

    public COUILoadInstallProgressPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Preference_COUI_COUILoadInstallProgressPreference);
    }

    public COUILoadInstallProgressPreference(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mListener = new COUILoadProgress.OnStateChangeListener() {
            @Override
            public void onStateChanged(COUILoadProgress loadProgress, int state) {
                if (mOnStateChangeListener != null) {
                    mState = state;
                    mOnStateChangeListener.onStateChanged(loadProgress, state);
                }
            }
        };
        mProgressText = "";
        int disabledColor = COUIContextUtil.getColor(getContext(), R.color.coui_color_disabled_neutral);
        mDisabledColor = disabledColor;
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.COUILoadInstallProgressPreference, defStyleAttr, defStyleRes);
        mProgressText = a.getText(R.styleable.COUILoadInstallProgressPreference_progressText);
        mProgressTextSize = a.getDimensionPixelSize(
                R.styleable.COUILoadInstallProgressPreference_progressTextSize,
                context.getResources().getDimensionPixelSize(
                        R.dimen.coui_install_download_progress_textsize));
        mProgressTextSize = (int) COUIChangeTextUtil.getSuitableFontSize(mProgressTextSize,
                context.getResources().getConfiguration().fontScale, 2);
        int progressTextColor = a.getColor(
                R.styleable.COUILoadInstallProgressPreference_progressTextColor, 0);
        int backgroundColor = a.getColor(
                R.styleable.COUILoadInstallProgressPreference_backgroundColor, 0);
        int installBackgroundColor = a.getColor(
                R.styleable.COUILoadInstallProgressPreference_installBackgroundColor, 0);
        mInstallProgressTextColor = a.getColor(
                R.styleable.COUILoadInstallProgressPreference_installProgressTextColor, 0);
        a.recycle();
        if (progressTextColor != 0) {
            mProgressTextColorStateList = COUIStateListUtil.createColorStateList(
                    progressTextColor, disabledColor);
        }
        if (backgroundColor != 0) {
            mProgressBackgroundColorStateList = COUIStateListUtil.createColorStateList(
                    backgroundColor, disabledColor);
        }
        if (installBackgroundColor != 0) {
            mInstallBackgroundColorStateList = COUIStateListUtil.createColorStateList(
                    installBackgroundColor, disabledColor);
        }
    }

    private int getInstallProgressTextColor() {
        return mInstallProgressTextColor;
    }

    public COUIInstallLoadProgress getLoadProgressView() {
        return mLoadProgress;
    }

    public int getMax() {
        if (mLoadProgress != null) {
            return mLoadProgress.getMax();
        }
        return 0;
    }

    public int getProgress() {
        if (mLoadProgress != null) {
            return mLoadProgress.getProgress();
        }
        return 0;
    }

    public CharSequence getProgressText() {
        return mProgressText;
    }

    public int getProgressTextSize() {
        return mProgressTextSize;
    }

    public int getState() {
        if (mLoadProgress != null) {
            return mLoadProgress.getState();
        }
        return 0;
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        mLoadProgress = (COUIInstallLoadProgress) holder.findViewById(R.id.coui_load_progress);
        if (mLoadProgress != null) {
            mLoadProgress.setText(getProgressText().toString());
            mLoadProgress.setDefaultTextSize(getProgressTextSize());
            if (mProgressTextColorStateList != null) {
                mLoadProgress.setBtnTextColorStateList(mProgressTextColorStateList);
            }
            if (mProgressBackgroundColorStateList != null) {
                mLoadProgress.setThemeSecondaryColorStateList(mProgressBackgroundColorStateList);
            }
            if (mInstallBackgroundColorStateList != null) {
                mLoadProgress.setThemeColorStateList(mInstallBackgroundColorStateList);
            }
            if (getInstallProgressTextColor() != 0) {
                mLoadProgress.setBtnTextColorBySurpassProgress(getInstallProgressTextColor());
            }
            if (mMax != 0) {
                mLoadProgress.setMax(mMax);
            }
            mLoadProgress.setProgress(mProgress);
            mLoadProgress.setState(mState);
            mLoadProgress.setOnStateChangeListener(mListener);
        }
    }

    public void setBackgroundColor(int color) {
        mProgressBackgroundColorStateList = COUIStateListUtil.createColorStateList(color, mDisabledColor);
        notifyChanged();
    }

    public void setInstallBackgroundColor(int color) {
        mInstallBackgroundColorStateList = COUIStateListUtil.createColorStateList(color, mDisabledColor);
        notifyChanged();
    }

    public void setInstallProgressTextColor(int color) {
        if (mInstallProgressTextColor != color) {
            mInstallProgressTextColor = color;
            notifyChanged();
        }
    }

    public void setMax(int max) {
        if (mMax != max) {
            mMax = max;
            notifyChanged();
        }
    }

    public void setOnStateChangeListener(OnStateChangeListener listener) {
        mOnStateChangeListener = listener;
    }

    public void setProgress(int progress) {
        mProgress = progress;
        if (mLoadProgress != null) {
            mLoadProgress.setProgress(progress);
        }
    }

    public void setProgressText(CharSequence text) {
        if (!TextUtils.equals(text, mProgressText)) {
            mProgressText = text;
            if (mLoadProgress != null) {
                mLoadProgress.setText(text.toString());
            }
        }
    }

    public void setProgressTextColor(int color) {
        mProgressTextColorStateList = COUIStateListUtil.createColorStateList(color, mDisabledColor);
        notifyChanged();
    }

    public void setProgressTextSize(int textSize) {
        if (mProgressTextSize != textSize) {
            mProgressTextSize = textSize;
            notifyChanged();
        }
    }

    public void setState(int state) {
        mState = state;
        if (mLoadProgress != null) {
            mLoadProgress.setState(state);
        }
    }
}
