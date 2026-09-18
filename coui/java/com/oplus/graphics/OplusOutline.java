package com.oplus.graphics;

import android.graphics.Outline;
import android.graphics.Rect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class OplusOutline implements IOplusOutline {
    private final Outline mOutline;
    private final OplusOutlineReflector mReflector;

    public OplusOutline(Outline outline) {
        mOutline = outline;
        mReflector = new OplusOutlineReflector(outline);
    }

    @Override
    public void setSmoothRoundRect(int left, int top, int right, int bottom, float radius, float weight) {
        if (!mReflector.callSmoothRoundRect("setSmoothRoundRect", left, top, right, bottom, radius, weight)) {
            mOutline.setRoundRect(left, top, right, bottom, reverseNoWeightRadius(radius, weight));
        }
    }

    @Override
    public void setSmoothRoundRect(int left, int top, int right, int bottom, float radius) {
        if (!mReflector.callSmoothRoundRect("setSmoothRoundRect", left, top, right, bottom, radius)) {
            mOutline.setRoundRect(left, top, right, bottom, radius);
        }
    }

    @Override
    public void setSmoothRoundRect(Rect rect, float radius, float weight) {
        setSmoothRoundRect(rect.left, rect.top, rect.right, rect.bottom, radius, weight);
    }

    @Override
    public void setSmoothRoundRect(Rect rect, float radius) {
        setSmoothRoundRect(rect.left, rect.top, rect.right, rect.bottom, radius);
    }

    private float reverseNoWeightRadius(float radius, float weight) {
        return weight > 0f ? radius / weight : radius;
    }

    static class OplusOutlineReflector {
        private final Object mExtImpl;

        OplusOutlineReflector(Outline outline) {
            mExtImpl = getExtImpl(outline);
        }

        boolean callSmoothRoundRect(String methodName, int left, int top, int right, int bottom,
                float radius) {
            if (mExtImpl == null) {
                return false;
            }
            try {
                Method method = mExtImpl.getClass().getMethod(methodName, int.class, int.class,
                        int.class, int.class, float.class);
                method.invoke(mExtImpl, left, top, right, bottom, radius);
                return true;
            } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                return false;
            }
        }

        boolean callSmoothRoundRect(String methodName, int left, int top, int right, int bottom,
                float radius, float weight) {
            if (mExtImpl == null) {
                return false;
            }
            try {
                Method method = mExtImpl.getClass().getMethod(methodName, int.class, int.class,
                        int.class, int.class, float.class, float.class);
                method.invoke(mExtImpl, left, top, right, bottom, radius, weight);
                return true;
            } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                return false;
            }
        }

        private Object getExtImpl(Outline outline) {
            try {
                Method getWrapper = Outline.class.getMethod("getWrapper");
                Object wrapper = getWrapper.invoke(outline);
                if (wrapper == null) {
                    return null;
                }
                Method getExtImpl = wrapper.getClass().getMethod("getExtImpl");
                return getExtImpl.invoke(wrapper);
            } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                return null;
            }
        }
    }
}
