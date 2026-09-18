package com.coui.appcompat.card;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.preference.PreferenceViewHolder;

import com.coui.appcompat.R;
import com.coui.appcompat.preference.COUIJumpPreference;
import com.coui.appcompat.pressfeedback.COUIPressFeedbackHelper;

public class COUIPressFeedbackJumpPreference extends COUIJumpPreference {
    private View rootView;

    public COUIPressFeedbackJumpPreference(Context context) {
        this(context, null);
    }

    public COUIPressFeedbackJumpPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.couiJumpPreferenceStyle);
    }

    public COUIPressFeedbackJumpPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public COUIPressFeedbackJumpPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public final View getRootView() {
        return rootView;
    }

    public final void setRootView(View rootView) {
        this.rootView = rootView;
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        rootView = holder.itemView;
        COUIPressFeedbackHelper pressFeedbackHelper = new COUIPressFeedbackHelper(rootView, 0);
        if (rootView == null || !rootView.isEnabled()) {
            if (rootView != null) {
                rootView.setOnTouchListener(null);
            }
            return;
        }
        rootView.setOnTouchListener((view, event) ->
                onBindViewHolderTouch(this, pressFeedbackHelper, view, event));
    }

    private static boolean onBindViewHolderTouch(COUIPressFeedbackJumpPreference preference,
            COUIPressFeedbackHelper pressFeedbackHelper, View view, MotionEvent event) {
        View root = preference.rootView;
        if (root != null && root.isEnabled()) {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_DOWN) {
                pressFeedbackHelper.executeFeedbackAnimator(true);
            } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                pressFeedbackHelper.executeFeedbackAnimator(false);
            }
        }
        return false;
    }
}
