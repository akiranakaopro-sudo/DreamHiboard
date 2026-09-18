package com.coui.appcompat.textviewcompatutil;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;

import com.coui.appcompat.R;
import com.coui.appcompat.accessibilityutil.COUIAccessibilityUtil;
import com.coui.appcompat.darkmode.COUIDarkModeUtil;
import com.coui.appcompat.state.COUIMaskRippleDrawable;
import com.coui.appcompat.textutil.COUIChangeTextUtil;
import com.coui.appcompat.version.COUICompatUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class COUITextViewCompatUtil {
    private static final String TAG = "COUITextViewCompatUtil";
    private static final String TEXT_VIEW_WRAPPER = "android.view.OplusBaseView";
    private static String mTextViewCompatName;

    private static boolean canReachFrameworkWrapper() {
        try {
            Class.forName(TEXT_VIEW_WRAPPER);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    public static float getParaSpacing(TextView textView) {
        mTextViewCompatName = canReachFrameworkWrapper()
                ? TEXT_VIEW_WRAPPER
                : COUICompatUtil.getInstance().getBaseViewName();
        try {
            return reflectGetParaSpacing(textView);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            return 0.0f;
        }
    }

    private static float reflectGetParaSpacing(TextView textView) throws ReflectiveOperationException {
        Method method = Class.forName(mTextViewCompatName).getDeclaredMethod("getParaSpacing");
        method.setAccessible(true);
        return (Float) method.invoke(textView);
    }

    private static void reflectSetParaSpacing(TextView textView, float spacing)
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = Class.forName(mTextViewCompatName).getDeclaredMethod("setParaSpacing", Float.TYPE);
        method.setAccessible(true);
        method.invoke(textView, spacing);
    }

    public static void setButtonTextView(TextView textView) {
        setPressRippleDrawable(textView);
        COUIChangeTextUtil.adaptFontSize(textView, 4);
    }

    public static boolean setParaSpacing(TextView textView, float spacing) {
        mTextViewCompatName = canReachFrameworkWrapper()
                ? TEXT_VIEW_WRAPPER
                : COUICompatUtil.getInstance().getBaseViewName();
        try {
            reflectSetParaSpacing(textView, spacing);
            return true;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            return false;
        }
    }

    public static void setPressRippleDrawable(TextView textView) {
        setPressRippleDrawable(textView, false);
    }

    public static void setPressRippleDrawable(View view, boolean keepPadding) {
        if (view == null) {
            return;
        }
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        } else {
            layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        }
        view.setLayoutParams(layoutParams);
        if (!keepPadding) {
            int vertical = view.getContext().getResources().getDimensionPixelOffset(R.dimen.text_ripple_bg_padding_vertical);
            int horizontal = view.getContext().getResources().getDimensionPixelOffset(R.dimen.text_ripple_bg_padding_horizontal);
            view.setPadding(horizontal, vertical, horizontal, vertical);
        }
        COUIMaskRippleDrawable mask = new COUIMaskRippleDrawable(view.getContext());
        mask.setCustomRippleMask();
        view.setBackground(mask);
        COUIDarkModeUtil.setForceDarkAllow(view, false);
        if (ViewCompat.getAccessibilityDelegate(view) == null) {
            ViewCompat.setAccessibilityDelegate(view, new AccessibilityDelegateCompat() {
                @Override
                public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfoCompat info) {
                    super.onInitializeAccessibilityNodeInfo(host, info);
                    info.setClassName(COUIAccessibilityUtil.BUTTON_CLASS_NAME);
                }
            });
        }
    }
}
