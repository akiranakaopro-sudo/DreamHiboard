package com.coui.appcompat.animation.blendanimation;

import android.view.View;
import androidx.dynamicanimation.animation.FloatPropertyCompat;

public abstract class COUIProperty<T> extends FloatPropertyCompat<T> {
    public static final float ALPHA_MIN_VISIBLE_CHANGE = 0.00390625f;
    public static final float BLUR_MIN_VISIBLE_CHANGE = 1.0f;
    public static final float COLOR_MIN_VISIBLE_CHANGE = 1.0f;
    public static final float DEFAULT_MIN_VISIBLE_CHANGE = 0.001f;
    public static final float HEIGHT_MIN_VISIBLE_CHANGE = 0.5f;
    public static final float POSITION_MIN_VISIBLE_CHANGE = 0.5f;
    public static final float RADIUS_MIN_VISIBLE_CHANGE = 0.001f;
    public static final float ROTATION_MIN_VISIBLE_CHANGE = 3.2552084E-4f;
    public static final float SCALE_MIN_VISIBLE_CHANGE = 3.2552084E-4f;
    public static final float SCROLL_MIN_VISIBLE_CHANGE = 1.0f;
    public static final float TRANSLATION_MIN_VISIBLE_CHANGE = 0.5f;
    public static final float WIDTH_MIN_VISIBLE_CHANGE = 0.5f;
    private final float mMinVisibleChange;
    public static final COUIViewProperty TRANSLATION_X = new COUIViewProperty("translationX") {
        @Override
        public float getValue(View view) {
            return view.getTranslationX();
        }

        @Override
        public void setValue(View view, float value) {
            view.setTranslationX(value);
        }
    };
    public static final COUIViewProperty TRANSLATION_Y = new COUIViewProperty("translationY") {
        @Override
        public float getValue(View view) {
            return view.getTranslationY();
        }

        @Override
        public void setValue(View view, float value) {
            view.setTranslationY(value);
        }
    };
    public static final COUIViewProperty TRANSLATION_Z = new COUIViewProperty("translationZ") {
        @Override
        public float getValue(View view) {
            return view.getTranslationZ();
        }

        @Override
        public void setValue(View view, float value) {
            view.setTranslationZ(value);
        }
    };
    public static final COUIViewProperty SCALE_X = new COUIViewProperty("scaleX") {
        @Override
        public float getValue(View view) {
            return view.getScaleX();
        }

        @Override
        public void setValue(View view, float value) {
            view.setScaleX(value);
        }
    };
    public static final COUIViewProperty SCALE = new COUIViewProperty("scale") {
        @Override
        public float getValue(View view) {
            return view.getScaleX();
        }

        @Override
        public void setValue(View view, float value) {
            view.setScaleX(value);
            view.setScaleY(value);
        }
    };
    public static final COUIViewProperty SCALE_Y = new COUIViewProperty("scaleY") {
        @Override
        public float getValue(View view) {
            return view.getScaleY();
        }

        @Override
        public void setValue(View view, float value) {
            view.setScaleY(value);
        }
    };
    public static final COUIViewProperty ROTATION = new COUIViewProperty("rotation") {
        @Override
        public float getValue(View view) {
            return view.getRotation();
        }

        @Override
        public void setValue(View view, float value) {
            view.setRotation(value);
        }
    };
    public static final COUIViewProperty ROTATION_X = new COUIViewProperty("rotationX") {
        @Override
        public float getValue(View view) {
            return view.getRotationX();
        }

        @Override
        public void setValue(View view, float value) {
            view.setRotationX(value);
        }
    };
    public static final COUIViewProperty ROTATION_Y = new COUIViewProperty("rotationY") {
        @Override
        public float getValue(View view) {
            return view.getRotationY();
        }

        @Override
        public void setValue(View view, float value) {
            view.setRotationY(value);
        }
    };
    public static final COUIViewProperty X = new COUIViewProperty("x") {
        @Override
        public float getValue(View view) {
            return view.getX();
        }

        @Override
        public void setValue(View view, float value) {
            view.setX(value);
        }
    };
    public static final COUIViewProperty Y = new COUIViewProperty("y") {
        @Override
        public float getValue(View view) {
            return view.getY();
        }

        @Override
        public void setValue(View view, float value) {
            view.setY(value);
        }
    };
    public static final COUIViewProperty Z = new COUIViewProperty("z") {
        @Override
        public float getValue(View view) {
            return view.getZ();
        }

        @Override
        public void setValue(View view, float value) {
            view.setZ(value);
        }
    };
    public static final COUIViewProperty ALPHA = new COUIViewProperty("alpha") {
        @Override
        public float getValue(View view) {
            return view.getAlpha();
        }

        @Override
        public void setValue(View view, float value) {
            view.setAlpha(value);
        }
    };
    public static final COUIViewProperty SCROLL_X = new COUIViewProperty("scrollX") {
        @Override
        public float getValue(View view) {
            return view.getScrollX();
        }

        @Override
        public void setValue(View view, float value) {
            view.setScrollX((int) value);
        }
    };
    public static final COUIViewProperty SCROLL_Y = new COUIViewProperty("scrollY") {
        @Override
        public float getValue(View view) {
            return view.getScrollY();
        }

        @Override
        public void setValue(View view, float value) {
            view.setScrollY((int) value);
        }
    };

    public static abstract class COUIBlurProperty<T> extends COUIProperty<T> {
        public COUIBlurProperty() {
            super("blur");
        }

        public abstract int getBlur(T target);

        @Override
        public float getValue(T target) {
            return getBlur(target);
        }

        public abstract void setBlur(T target, int value);

        @Override
        public void setValue(T target, float value) {
            setBlur(target, (int) value);
        }
    }

    public static abstract class COUIColorProperty<T> extends COUIProperty<T> {
        public COUIColorProperty() {
            super("color");
        }

        public abstract int getColor(T target);

        @Override
        public float getValue(T target) {
            return getColor(target);
        }

        public abstract void setColor(T target, int value);

        @Override
        public void setValue(T target, float value) {
            setColor(target, (int) value);
        }
    }

    public static abstract class COUIDefaultProperty<T> extends COUIProperty<T> {
        public COUIDefaultProperty() {
            super("default");
        }
    }

    public static abstract class COUIHeightProperty<T> extends COUIProperty<T> {
        public COUIHeightProperty() {
            super("height");
        }

        public abstract float getHeight(T target);

        @Override
        public float getValue(T target) {
            return getHeight(target);
        }

        public abstract void setHeight(T target, float value);

        @Override
        public void setValue(T target, float value) {
            setHeight(target, value);
        }
    }

    public static abstract class COUIRadiusProperty<T> extends COUIProperty<T> {
        public COUIRadiusProperty() {
            super("radius");
        }

        public abstract float getRadius(T target);

        @Override
        public float getValue(T target) {
            return getRadius(target);
        }

        public abstract void setRadius(T target, float value);

        @Override
        public void setValue(T target, float value) {
            setRadius(target, value);
        }
    }

    public static abstract class COUIViewProperty extends COUIProperty<View> {
        private COUIViewProperty(String propertyName) {
            super(propertyName);
        }
    }

    public static abstract class COUIWidthProperty<T> extends COUIProperty<T> {
        public COUIWidthProperty() {
            super("width");
        }

        @Override
        public float getValue(T target) {
            return getWidth(target);
        }

        public abstract float getWidth(T target);

        @Override
        public void setValue(T target, float value) {
            setWidth(target, value);
        }

        public abstract void setWidth(T target, float value);
    }

    public COUIProperty(String propertyName) {
        super(propertyName);
        this.mMinVisibleChange = getMinVisibleChangeByName(propertyName);
    }

    private static float getMinVisibleChangeByName(String propertyName) {
        propertyName.hashCode();
        switch (propertyName) {
            case "rotationX":
            case "rotationY":
            case "rotation":
                return ROTATION_MIN_VISIBLE_CHANGE;
            case "translationX":
            case "translationY":
            case "translationZ":
            case "height":
                return TRANSLATION_MIN_VISIBLE_CHANGE;
            case "scaleX":
            case "scaleY":
            case "scale":
                return SCALE_MIN_VISIBLE_CHANGE;
            case "x":
            case "y":
            case "z":
                return POSITION_MIN_VISIBLE_CHANGE;
            case "blur":
                return BLUR_MIN_VISIBLE_CHANGE;
            case "alpha":
                return ALPHA_MIN_VISIBLE_CHANGE;
            case "color":
                return COLOR_MIN_VISIBLE_CHANGE;
            case "width":
                return WIDTH_MIN_VISIBLE_CHANGE;
            case "scrollX":
            case "scrollY":
                return SCROLL_MIN_VISIBLE_CHANGE;
            default:
                return DEFAULT_MIN_VISIBLE_CHANGE;
        }
    }

    public float getMinimumVisibleChange() {
        return this.mMinVisibleChange;
    }
}

