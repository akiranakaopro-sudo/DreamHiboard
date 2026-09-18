package com.coui.appcompat.poplist;

import android.annotation.SuppressLint;
import android.view.View;

public class COUIViewCompat {
    static final ViewCompatImpl IMPL = new JbMr1ViewCompatImpl();

    public interface ViewCompatImpl {
        int getRawLayoutDirection(View view);
        int getTextAlignment(View view);
        boolean isVisibleToUser(View view);
        void setTextAlignment(View view, int textAlignment);
    }

    public static class BaseViewCompatImpl implements ViewCompatImpl {
        @Override
        @SuppressLint("NewApi")
        public int getRawLayoutDirection(View view) {
            return view.getLayoutDirection();
        }

        @Override
        public int getTextAlignment(View view) {
            return 0;
        }

        @Override
        public boolean isVisibleToUser(View view) {
            return true;
        }

        @Override
        public void setTextAlignment(View view, int textAlignment) {
        }
    }

    public static class JBViewCompatImpl extends BaseViewCompatImpl {
        @Override
        @SuppressLint("NewApi")
        public int getTextAlignment(View view) {
            return view.getTextAlignment();
        }

        @Override
        @SuppressLint("NewApi")
        public void setTextAlignment(View view, int textAlignment) {
            view.setTextAlignment(textAlignment);
        }
    }

    public static class JbMr1ViewCompatImpl extends JBViewCompatImpl {
        @Override
        public int getRawLayoutDirection(View view) {
            return 2;
        }
    }

    public static int getRawLayoutDirection(View view) {
        return IMPL.getRawLayoutDirection(view);
    }

    public static int getTextAlignment(View view) {
        return IMPL.getTextAlignment(view);
    }

    public static boolean isVisibleToUser(View view) {
        return IMPL.isVisibleToUser(view);
    }

    public static void setTextAlignment(View view, int textAlignment) {
        IMPL.setTextAlignment(view, textAlignment);
    }
}
