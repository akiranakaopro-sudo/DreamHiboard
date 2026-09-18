package com.oplus.view;

import android.view.View;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class OplusView {
    private final View mView;

    public OplusView(View view) {
        this.mView = view;
    }

    public void setOverrideLightSourceGeometry(float lightX, float lightY, float lightZ,
            float lightRadius, float blurRadius) {
        Object viewWrapper = call(this.mView, "getViewWrapper");
        Object viewExt = call(viewWrapper, "getViewExt");
        call(viewExt, "setOverrideLightSourceGeometry",
                new Class[]{float.class, float.class, float.class, float.class, float.class},
                lightX, lightY, lightZ, lightRadius, blurRadius);
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
            method.invoke(target, args);
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException ignored) {
        }
        return null;
    }
}
