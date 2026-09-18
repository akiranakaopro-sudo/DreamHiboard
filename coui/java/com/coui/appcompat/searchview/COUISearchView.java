package com.coui.appcompat.searchview;


import com.coui.appcompat.R;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.WindowInsets;
import androidx.appcompat.widget.SearchView;
import java.lang.reflect.Field;


public class COUISearchView extends SearchView {
    private COUIHintAnimationLayout mCOUIHintAnimationLayout;
    private boolean mIsHintTextSize;
    private SearchView.SearchAutoComplete mSearchSrcTextView;

    public static class COUISearchAutoComplete extends SearchView.SearchAutoComplete {
        private static final int VERSION_CODE_U = 34;
        private boolean mEnableNativeKeyPreIme;

        public COUISearchAutoComplete(Context context) {
            super(context);
            this.mEnableNativeKeyPreIme = false;
        }

        @Override
        public boolean onKeyPreIme(int keyCode, KeyEvent keyEvent) {
            WindowInsets rootWindowInsets;
            boolean handled = super.onKeyPreIme(keyCode, keyEvent);
            if (Build.VERSION.SDK_INT < VERSION_CODE_U || this.mEnableNativeKeyPreIme || (rootWindowInsets = getRootView().getRootWindowInsets()) == null || rootWindowInsets.isVisible(WindowInsets.Type.ime()) || keyCode != 4) {
                return handled;
            }
            return false;
        }

        public void setEnableNativeKeyPreIme(boolean enable) {
            this.mEnableNativeKeyPreIme = enable;
        }

        public COUISearchAutoComplete(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
            this.mEnableNativeKeyPreIme = false;
        }

        public COUISearchAutoComplete(Context context, AttributeSet attributeSet, int defStyleAttr) {
            super(context, attributeSet, defStyleAttr);
            this.mEnableNativeKeyPreIme = false;
        }
    }

    public COUISearchView(Context context) {
        super(context);
        this.mIsHintTextSize = true;
    }

    private void changeTextSize(String text) {
        if (this.mSearchSrcTextView == null) {
            this.mSearchSrcTextView = getSearchAutoComplete();
        }
        if (text.isEmpty()) {
            this.mSearchSrcTextView.setTextSize(0, getContext().getResources().getDimensionPixelSize(R.dimen.coui_searchview_text_hint_size));
            this.mIsHintTextSize = true;
        } else if (this.mIsHintTextSize) {
            this.mSearchSrcTextView.setTextSize(0, getContext().getResources().getDimensionPixelSize(R.dimen.coui_searchview_text_size));
            this.mIsHintTextSize = false;
        }
    }

    public COUIHintAnimationLayout getHintAnimationLayout() {
        return this.mCOUIHintAnimationLayout;
    }

    public SearchView.SearchAutoComplete getSearchAutoComplete() {
        SearchView.SearchAutoComplete searchAutoComplete = this.mSearchSrcTextView;
        if (searchAutoComplete != null) {
            return searchAutoComplete;
        }
        try {
            Field declaredField = SearchView.class.getDeclaredField("mSearchSrcTextView");
            declaredField.setAccessible(true);
            SearchView.SearchAutoComplete reflected = (SearchView.SearchAutoComplete) declaredField.get(this);
            this.mSearchSrcTextView = reflected;
            return reflected;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public COUISearchView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mIsHintTextSize = true;
    }

    public COUISearchView(Context context, AttributeSet attributeSet, int defStyleAttr) {
        super(context, attributeSet, defStyleAttr);
        this.mIsHintTextSize = true;
        this.mCOUIHintAnimationLayout = (COUIHintAnimationLayout) findViewById(R.id.search_animation_layout);
    }
}
