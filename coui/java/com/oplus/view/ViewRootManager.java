package com.oplus.view;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;

import com.oplus.graphics.OplusBlurParam;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ViewRootManager {
    private static final String TAG = "ViewRootManager";
    private Drawable mBackgroundBlurDrawable;

    public ViewRootManager(View view) {
        Object viewRootImpl = call(view, "getViewRootImpl");
        Object drawable = call(viewRootImpl, "createBackgroundBlurDrawable");
        if (drawable instanceof Drawable) {
            mBackgroundBlurDrawable = (Drawable) drawable;
        } else {
            Log.d(TAG, "viewRootImpl is null return null");
        }
    }

    public Drawable getBackgroundBlurDrawable() {
        return mBackgroundBlurDrawable;
    }

    public void setBlurRadius(int blurRadius) {
        if (mBackgroundBlurDrawable == null) {
            Log.d(TAG, "BackgroundBlurDrawable is null return null");
            return;
        }
        call(mBackgroundBlurDrawable, "setBlurRadius", new Class[]{int.class}, blurRadius);
    }

    public void setCornerRadius(float cornerRadius) {
        if (mBackgroundBlurDrawable == null) {
            Log.d(TAG, "BackgroundBlurDrawable is null return null");
            return;
        }
        call(mBackgroundBlurDrawable, "setCornerRadius", new Class[]{float.class}, cornerRadius);
    }

    public void setCornerRadius(float topLeft, float topRight, float bottomLeft, float bottomRight) {
        if (mBackgroundBlurDrawable == null) {
            Log.d(TAG, "BackgroundBlurDrawable is null return null by four");
            return;
        }
        call(mBackgroundBlurDrawable, "setCornerRadius",
                new Class[]{float.class, float.class, float.class, float.class},
                topLeft, topRight, bottomLeft, bottomRight);
    }

    public void setColor(int color) {
        if (mBackgroundBlurDrawable == null) {
            Log.d(TAG, "BackgroundBlurDrawable is null return null");
            return;
        }
        call(mBackgroundBlurDrawable, "setColor", new Class[]{int.class}, color);
    }

    public void setBlurParams(OplusBlurParam params) {
        if (mBackgroundBlurDrawable == null) {
            return;
        }
        Object wrapper = call(mBackgroundBlurDrawable, "getWrapper");
        Object extImpl = call(wrapper, "getExtImpl");
        call(extImpl, "setBlurParams", new Class[]{OplusBlurParam.class}, params);
    }

    private static Object call(Object target, String methodName) {
        if (target == null) {
            return null;
        }
        try {
            Method method = target.getClass().getMethod(methodName);
            return method.invoke(target);
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            return null;
        }
    }

    private static Object call(Object target, String methodName, Class<?>[] parameterTypes,
            Object... args) {
        if (target == null) {
            return null;
        }
        try {
            Method method = target.getClass().getMethod(methodName, parameterTypes);
            return method.invoke(target, args);
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            return null;
        }
    }
}
