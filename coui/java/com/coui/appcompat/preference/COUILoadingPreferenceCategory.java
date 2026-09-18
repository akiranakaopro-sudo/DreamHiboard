package com.coui.appcompat.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.preference.PreferenceViewHolder;

import com.coui.appcompat.R;
import com.coui.appcompat.progressbar.COUICompProgressIndicator;

public class COUILoadingPreferenceCategory extends COUIPreferenceCategory {
    private static final String TAG = "LoadingCategory";

    private String mLoadingText;
    private TextView mLoadingTextView;
    private LoadingType mLoadingType = LoadingType.LOADING;
    private COUICompProgressIndicator mLoadingView;
    private int mWidgetLayoutAfterLoading;
    private int mWidgetLayoutBeforeLoading;

    public enum LoadingType {
        LOADING,
        PAUSE,
        INVISIBLE,
        AFTER_LOADING,
        BEFORE_LOADING
    }

    public COUILoadingPreferenceCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.COUILoadingPreferenceCategory, 0, 0);
        mWidgetLayoutAfterLoading = a.getResourceId(
                R.styleable.COUILoadingPreferenceCategory_coui_loading_after_layout, 0);
        mWidgetLayoutBeforeLoading = a.getResourceId(
                R.styleable.COUILoadingPreferenceCategory_coui_loading_before_layout, 0);
        mLoadingText = a.getString(R.styleable.COUILoadingPreferenceCategory_text_in_loading);
        a.recycle();
        if (mWidgetLayoutBeforeLoading != 0) {
            mLoadingType = LoadingType.BEFORE_LOADING;
        }
    }

    public LoadingType getLoadingType() {
        return mLoadingType;
    }

    public void hideLoading() {
        LoadingType loadingType = mLoadingType;
        if (loadingType == LoadingType.AFTER_LOADING) {
            Log.e(TAG, "It is no longer loading state");
            return;
        }
        LoadingType invisible = LoadingType.INVISIBLE;
        if (loadingType != invisible) {
            mLoadingType = invisible;
            notifyChanged();
        }
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        switch (mLoadingType) {
            case BEFORE_LOADING:
                setWidgetLayoutRes(mWidgetLayoutBeforeLoading);
                super.onBindViewHolder(holder);
                return;
            case LOADING:
                setWidgetLayoutRes(R.layout.coui_preference_category_widget_layout_loading);
                super.onBindViewHolder(holder);
                mLoadingView = (COUICompProgressIndicator) getWidgetLayout().findViewById(R.id.catagory_loading);
                mLoadingTextView = (TextView) getWidgetLayout().findViewById(R.id.text_in_loading);
                mLoadingView.setVisibility(View.VISIBLE);
                if (mLoadingView.getAnimationView() != null) {
                    mLoadingView.getAnimationView().playAnimation();
                }
                if (TextUtils.isEmpty(mLoadingText)) {
                    mLoadingTextView.setVisibility(View.GONE);
                } else {
                    mLoadingTextView.setText(mLoadingText);
                    mLoadingTextView.setVisibility(View.VISIBLE);
                }
                if (TextUtils.isEmpty(mLoadingText)) {
                    getWidgetLayout().setBackground(null);
                }
                return;
            case PAUSE:
                if (mLoadingView != null) {
                    mLoadingView.setVisibility(View.VISIBLE);
                    mLoadingView.getAnimationView().pauseAnimation();
                }
                return;
            case INVISIBLE:
                if (mLoadingView != null) {
                    mLoadingView.setVisibility(View.GONE);
                }
                return;
            case AFTER_LOADING:
                setWidgetLayoutRes(mWidgetLayoutAfterLoading);
                super.onBindViewHolder(holder);
                return;
            default:
        }
    }

    public void pauseLoading() {
        LoadingType loadingType = mLoadingType;
        if (loadingType == LoadingType.AFTER_LOADING) {
            Log.e(TAG, "It is no longer loading state");
            return;
        }
        LoadingType pause = LoadingType.PAUSE;
        if (loadingType != pause) {
            mLoadingType = pause;
            notifyChanged();
        }
    }

    @Override
    public boolean rightTextfixSecondaryColor() {
        return true;
    }

    public void setLoadingText(String loadingText) {
        if (!TextUtils.equals(mLoadingText, loadingText)) {
            mLoadingText = loadingText;
            notifyChanged();
        }
    }

    public void setShowAfterView() {
        setShowAfterView(mWidgetLayoutAfterLoading);
    }

    public void setShowAfterView(int layoutResId) {
        mWidgetLayoutAfterLoading = layoutResId;
        mLoadingType = LoadingType.AFTER_LOADING;
        notifyChanged();
    }

    public void startLoading() {
        LoadingType loading = LoadingType.LOADING;
        if (mLoadingType != loading) {
            mLoadingType = loading;
            notifyChanged();
        }
    }
}
