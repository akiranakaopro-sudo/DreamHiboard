package com.coui.appcompat.lockview;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BlendMode;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.customview.widget.ExploreByTouchHelper;
import androidx.dynamicanimation.animation.FloatValueHolder;
import com.coui.appcompat.accessibilityutil.COUIAccessibilityUtil;
import com.coui.appcompat.animation.COUIEaseInterpolator;
import com.coui.appcompat.animation.COUIInEaseInterpolator;
import com.coui.appcompat.animation.COUIOutEaseInterpolator;
import com.coui.appcompat.animation.dynamicanimation.COUIDynamicAnimation;
import com.coui.appcompat.animation.dynamicanimation.COUISpringAnimation;
import com.coui.appcompat.animation.dynamicanimation.COUISpringForce;
import com.coui.appcompat.darkmode.COUIDarkModeUtil;
import com.coui.appcompat.lockview.LightEffectHelper;
import com.coui.appcompat.log.COUILog;
import com.coui.appcompat.pressfeedback.COUIPressFeedbackHelper;
import com.coui.appcompat.uiutil.UIUtil;
import com.coui.appcompat.vibrateutil.VibrateUtils;
import com.coui.appcompat.R;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class COUINumericKeyboard extends View {
    public static final long ALPHA_DELAY = 166;
    public static final long ALPHA_DURATION = 167;
    public static final long ALPHA_OFFSET = 16;
    private static final float BLUR_END_SCALE = 2.0f;
    private static final float BLUR_START_SCALE = 1.0f;
    public static final int CELL_COLUMN_COUNT = 3;
    public static final int CELL_ROW_COUNT = 4;
    public static final float DEFAULT_ALPHA_VALUE = 255.0f;
    private static final int ELEVEN = 11;

    @Deprecated
    public static final int EMPTY_NINE_AND_ELEVEN = 1;
    private static final int FADE_ANIMATOR_TIME = 160;
    private static final int FADE_BLUR_ANIMATOR_TIME = 400;
    private static final float FADE_END_SCALE = 2.5f;
    private static final float FADE_START_SCALE = 2.15f;
    public static final int FONT_VARIATION_DEFAULT = 550;
    public static final int FONT_VARIATION_DEFAULT_PLUS = 200;
    public static final String FONT_VARIATION_SETTINGS = "font_variation_settings";
    private static final float GRADIENT_COLOR_STOP_END = 1.0f;
    private static final float GRADIENT_COLOR_STOP_START = 0.0f;
    private static final float GRADIENT_INNER_STOP_1 = 0.7f;
    private static final float GRADIENT_OUTER_STOP_1 = 0.3f;
    private static final float GRADIENT_OUTER_STOP_2 = 0.6f;
    private static final float GRADIENT_OUTER_STOP_3 = 0.8f;
    private static final float INNER_SHADOW_DX = 0.0f;
    private static final float INNER_SHADOW_DY_1 = -8.0f;
    private static final float INNER_SHADOW_DY_2 = 2.0f;
    private static final float INNER_SHADOW_RADIUS_1 = 32.0f;
    private static final float INNER_SHADOW_RADIUS_2 = 8.0f;
    private static final float INNER_SHADOW_STROKE_WIDTH_1 = 20.0f;
    private static final float INNER_SHADOW_STROKE_WIDTH_2 = 12.0f;
    private static final int KEYCODE_0_COLUMN = 1;
    private static final int KEYCODE_0_ROW = 3;
    private static final int LIGHT_EFFECT = 1;
    private static final int NINE = 9;

    @Deprecated
    public static final int RETAIN_ELEVEN = 3;

    @Deprecated
    public static final int RETAIN_NINE = 2;
    private static final int RIPPLE_EFFECT = 0;
    private static final int SHOW_ANIMATOR_TIME = 100;
    private static final float SHOW_END_SCALE = 2.15f;
    private static final float SHOW_START_SCALE = 1.0f;
    private static final float SIDE_STYLE_SPRING_RESPONSE = 0.2f;
    public static final int SIDE_TYPE_DELETE = 1;
    public static final int SIDE_TYPE_FINISH = 2;
    public static final int SIDE_TYPE_NONE = 0;
    private static final String TAG = "COUINumericKeyboard";
    private static final int TEN = 10;
    public static final long TRANSLATE_Y_DURATION = 500;
    public static final long TRANSLATE_Y_OFFSET = 16;

    @Deprecated
    public int NUMERIC;

    @Deprecated
    public int WORD;
    private final AccessibilityManager mAccessibilityManagerService;
    private int mAdditionalPressableArea;
    private Interpolator mAlphaInterpolator;
    private int mBorderLineAlpha;
    private int mBorderLineColor;
    private int mBorderLineHighLightAlpha;
    private int mBorderLineHighLightColor;
    private final Paint mBorderLinePaint;
    private final float mButtonBorderWidth;
    private final Path mButtonPath;
    private int mCellHeight;
    private int mCellWidth;
    private float mCircleMaxAlpha;
    private int mCircleRadius;
    private final Paint mClipPaint;
    private Context mContext;
    private Typeface mCustomTypeface;
    private int mDefaultHeight;
    private int mDefaultWidth;
    public SideStyle mDeleteStyle;
    private boolean mDownState;
    private KeyboardDrawDelegate mDrawDelegate;
    private float mDrawableAlpha;
    private int mDrawableTranslateX;
    private int mDrawableTranslateY;
    private boolean mEnableHapticFeedback;
    private PatternExploreByTouchHelper mExploreByTouchHelper;
    public final SideStyle mFinishStyle;
    public int mFontVariationDefaultPlus;
    private RadialGradient mGradient;
    private RadialGradient mGradient2;
    private boolean mHasCustomTypeface;
    private int mHorizontalSpacing;
    private final int mInnerGradientColor1;
    private final int mInnerGradientColor2;
    private Bitmap mInnerShadowBitmap;
    private Paint mInnerShadowBitmapPaint;
    private InnerShadowHelper mInnerShadowHelper;
    private Matrix mInnerShadowMatrix;
    private boolean mIsLinearMotorVersion;
    private Drawable mKeyboardDelete;
    private int mKeyboardLineColor;
    private int mKeyboardNumberTextAlpha;
    private int mKeyboardNumberTextColor;
    private float mKeyboardNumberTextSize;
    private int[] mKeyboardNumbers;
    private SideStyle mLeftStyle;
    private int mLightShaderRadius;
    private Paint mLinePaint;
    private final int mLowerInnerShadowColor;
    private int mMaxTranslateY;
    private float mNormalAlpha;
    private GradientDrawable mNumberBackground;
    private int mNumberBackgroundAlpha;
    private int mNumberBackgroundColor;
    private int mNumberBackgroundRadius;
    private final RectF mNumberBounds;
    private float mNumberOffsetY;
    private Paint.FontMetrics mNumberTextFontMetrics;
    private TextPaint mNumberTextPaint;
    private OnClickItemListener mOnClickItemListener;
    private final int mOuterGradientColor1;
    private final int mOuterGradientColor2;
    private final int mOuterGradientColor3;
    private Paint mPaint;
    private int mPreVariation;
    private int mPressEffectStyle;
    private int mPressedColor;
    private SideStyle mRightStyle;
    private Path mShadowLayerPath;
    private int mSideBackgroundColor;
    private int mStyle;
    private float mTextAlpha;
    private int mTextTranslateX;
    private int mTextTranslateY;
    private Cell mTouchCell;
    private final RectF mTranslateBounds;
    private Interpolator mTranslateYInterpolator;
    private String mTtfPath;
    private final int mUpperInnerShadowColor;
    private int mVerticalSpacing;
    private int mViewSize;
    private Paint.FontMetricsInt mWordTextFontMetrics;
    private TextPaint mWordTextPaint;
    private Cell[][] sCells;
    private static final Interpolator DEFAULT_OUT_EASE_INTERPOLATOR = new COUIOutEaseInterpolator();
    private static final Interpolator PATH_INTERPOLATOR = new PathInterpolator(0.0f, 0.0f, 0.6f, 1.0f);

    public class Cell {
        float blurAlpha;
        Drawable blurCircle;
        ValueAnimator blurFadeAnimator;
        float blurScale;
        String cellLettersStr;
        float cellNumberAlpha;
        String cellNumberStr;
        int cellNumberTranslateX;
        int cellNumberTranslateY;
        int column;
        ValueAnimator fadeAnimator;
        float mButtonScale;
        COUIPressFeedbackHelper mButtonScaleHelper;
        float mInnerLightAlpha;
        boolean mInvalidatePaths;
        LightEffectHelper mLightEffectHelper;
        Path mLightEffectPath;
        Path mNumberPaths;
        float normalAlpha;
        Drawable normalCircle;
        float normalScale;
        int pointerId;
        int pressedColor;
        int row;
        ValueAnimator showAnimator;

        public void refreshLightEffectDrawPath() {
            if (this.mInvalidatePaths) {
                refreshNumberPaths();
                this.mLightEffectPath.reset();
                this.mLightEffectPath.addOval(-COUINumericKeyboard.this.mLightShaderRadius, -COUINumericKeyboard.this.mLightShaderRadius, COUINumericKeyboard.this.mLightShaderRadius, COUINumericKeyboard.this.mLightShaderRadius, Path.Direction.CCW);
                this.mLightEffectPath.op(this.mNumberPaths, Path.Op.INTERSECT);
                this.mInvalidatePaths = false;
            }
        }

        private void refreshNumberPaths() {
            this.mNumberPaths.reset();
            float centerXForColumn = COUINumericKeyboard.this.getCenterXForColumn(this.column);
            float centerYForRow = COUINumericKeyboard.this.getCenterYForRow(this.row);
            for (int row = 0; row < 4; row++) {
                for (int column = 0; column < 3; column++) {
                    if ((row != 3 || column != 0) && ((row != 3 || column != 2) && (row != this.row || column != this.column))) {
                        this.mNumberPaths.addOval((COUINumericKeyboard.this.getCenterXForColumn(column) - (COUINumericKeyboard.this.mNumberBackgroundRadius * COUINumericKeyboard.this.sCells[row][column].mButtonScale)) - centerXForColumn, (COUINumericKeyboard.this.getCenterYForRow(row) - (COUINumericKeyboard.this.mNumberBackgroundRadius * COUINumericKeyboard.this.sCells[row][column].mButtonScale)) - centerYForRow, (COUINumericKeyboard.this.getCenterXForColumn(column) + (COUINumericKeyboard.this.mNumberBackgroundRadius * COUINumericKeyboard.this.sCells[row][column].mButtonScale)) - centerXForColumn, (COUINumericKeyboard.this.getCenterYForRow(row) + (COUINumericKeyboard.this.mNumberBackgroundRadius * COUINumericKeyboard.this.sCells[row][column].mButtonScale)) - centerYForRow, Path.Direction.CCW);
                    }
                }
            }
        }

        public boolean equals(Cell cell) {
            if (cell == null) {
                return false;
            }
            if (this == cell) {
                return true;
            }
            return this.row == cell.row && this.column == cell.column;
        }

        public int getColumn() {
            return this.column;
        }

        public int getRow() {
            return this.row;
        }

        public int hashCode() {
            return (this.row * 31) + this.column;
        }

        public void setCellNumberAlpha(float alpha) {
            this.cellNumberAlpha = alpha;
            COUINumericKeyboard.this.invalidate();
        }

        public void setCellNumberTranslateX(int translationX) {
            this.cellNumberTranslateX = translationX;
            COUINumericKeyboard.this.invalidate();
        }

        public void setCellNumberTranslateY(int translationY) {
            this.cellNumberTranslateY = translationY;
            COUINumericKeyboard.this.invalidate();
        }

        public void setCircleColor(int color) {
            if (color != 0) {
                this.pressedColor = color;
                Drawable drawable = this.normalCircle;
                if (drawable != null) {
                    drawable.mutate().setTint(color);
                }
                Drawable tintDrawable = this.blurCircle;
                if (tintDrawable != null) {
                    tintDrawable.mutate().setTint(color);
                }
            }
        }

        public String toString() {
            return "row " + this.row + "column " + this.column;
        }

        private Cell(int row, int column) {
            this.cellNumberStr = "";
            this.cellLettersStr = "";
            this.cellNumberAlpha = 1.0f;
            this.mButtonScale = 1.0f;
            this.normalAlpha = -1.0f;
            this.blurAlpha = -1.0f;
            this.mInnerLightAlpha = 0.0f;
            this.pointerId = -1;
            this.mLightEffectPath = new Path();
            this.mNumberPaths = new Path();
            this.mInvalidatePaths = true;
            COUINumericKeyboard.this.checkRange(row, column);
            this.row = row;
            this.column = column;
            this.normalCircle = COUINumericKeyboard.this.getContext().getDrawable(R.drawable.coui_number_keyboard_normal_circle);
            this.blurCircle = COUINumericKeyboard.this.getContext().getDrawable(R.drawable.coui_number_keyboard_blur_circle);
            this.normalCircle.setTint(COUINumericKeyboard.this.mPressedColor);
            this.blurCircle.setTint(COUINumericKeyboard.this.mPressedColor);
            this.pressedColor = COUINumericKeyboard.this.mPressedColor;
        }

        public boolean equals(Object obj) {
            try {
                return equals((Cell) obj);
            } catch (ClassCastException unused) {
                Log.e(COUINumericKeyboard.TAG, "ClassCastException when equals");
                return false;
            }
        }
    }

    public interface KeyboardDrawDelegate {
        Paint[] getCustomKeyboardPaint(int row, int column, RectF rectF);
    }

    public interface OnClickItemListener {
        void onClickLeft();

        void onClickNumber(int number);

        void onClickRight();
    }

    @Deprecated
    public interface OnItemTouchListener {
        void OnItemTouch();
    }

    @Deprecated
    public interface OnTouchTextListener {
        void onTouchText(int index);
    }

    @Deprecated
    public interface OnTouchUpListener {
        void OnTouchUp();
    }

    public final class PatternExploreByTouchHelper extends ExploreByTouchHelper {
        private Rect mTempRect;

        public PatternExploreByTouchHelper(View view) {
            super(view);
            this.mTempRect = new Rect();
        }

        private Rect getBoundsForVirtualView(int virtualViewId) {
            int centerXForColumn;
            int centerYForRow;
            Rect rect = this.mTempRect;
            if (virtualViewId != -1) {
                Cell cellOf = COUINumericKeyboard.this.of(virtualViewId / 3, virtualViewId % 3);
                centerXForColumn = (int) COUINumericKeyboard.this.getCenterXForColumn(cellOf.column);
                centerYForRow = (int) COUINumericKeyboard.this.getCenterYForRow(cellOf.row);
            } else {
                centerXForColumn = 0;
                centerYForRow = 0;
            }
            rect.left = centerXForColumn - COUINumericKeyboard.this.mCircleRadius;
            rect.right = centerXForColumn + COUINumericKeyboard.this.mCircleRadius;
            rect.top = centerYForRow - COUINumericKeyboard.this.mCircleRadius;
            rect.bottom = centerYForRow + COUINumericKeyboard.this.mCircleRadius;
            return rect;
        }

        private int getVirtualViewIdForHit(float x, float y) {
            Cell cellCheckForNewHit = COUINumericKeyboard.this.checkForNewHit(x, y);
            if (cellCheckForNewHit == null) {
                return -1;
            }
            int virtualViewId = (cellCheckForNewHit.getRow() * 3) + cellCheckForNewHit.getColumn();
            if (virtualViewId == 9) {
                COUINumericKeyboard cOUINumericKeyboard = COUINumericKeyboard.this;
                if (cOUINumericKeyboard.isEmptyStyle(cOUINumericKeyboard.mLeftStyle)) {
                    virtualViewId = -1;
                }
            }
            if (virtualViewId == 11) {
                COUINumericKeyboard cOUINumericKeyboard2 = COUINumericKeyboard.this;
                if (cOUINumericKeyboard2.isEmptyStyle(cOUINumericKeyboard2.mRightStyle)) {
                    return -1;
                }
            }
            return virtualViewId;
        }

        public int getItemCounts() {
            return 12;
        }

        public CharSequence getItemDescription(int virtualViewId) {
            if (virtualViewId == 9) {
                COUINumericKeyboard cOUINumericKeyboard = COUINumericKeyboard.this;
                if (!cOUINumericKeyboard.isEmptyStyle(cOUINumericKeyboard.mLeftStyle)) {
                    return COUINumericKeyboard.this.mLeftStyle.mDescription;
                }
            }
            if (virtualViewId == 11) {
                COUINumericKeyboard cOUINumericKeyboard2 = COUINumericKeyboard.this;
                if (!cOUINumericKeyboard2.isEmptyStyle(cOUINumericKeyboard2.mRightStyle)) {
                    return COUINumericKeyboard.this.mRightStyle.mDescription;
                }
            }
            if (virtualViewId == -1) {
                return PatternExploreByTouchHelper.class.getSimpleName();
            }
            return COUINumericKeyboard.this.mKeyboardNumbers[virtualViewId] + "";
        }

        @Override
        public int getVirtualViewAt(float x, float y) {
            return getVirtualViewIdForHit(x, y);
        }

        @Override
        public void getVisibleVirtualViews(List<Integer> virtualViewIds) {
            for (int i = 0; i < getItemCounts(); i++) {
                if (i == 9 && COUINumericKeyboard.this.isEmptyStyle(COUINumericKeyboard.this.mLeftStyle)) {
                    virtualViewIds.add(-1);
                } else if (i == 11 && COUINumericKeyboard.this.isEmptyStyle(COUINumericKeyboard.this.mRightStyle)) {
                    virtualViewIds.add(-1);
                } else {
                    virtualViewIds.add(i);
                }
            }
        }

        public boolean onItemClicked(int virtualViewId) {
            invalidateVirtualView(virtualViewId);
            if (COUINumericKeyboard.this.isEnabled()) {
                COUINumericKeyboard.this.callback(virtualViewId);
                COUINumericKeyboard.this.announceForAccessibility(getItemDescription(virtualViewId));
            }
            sendEventForVirtualView(virtualViewId, 1);
            return true;
        }

        @Override
        public boolean onPerformActionForVirtualView(int virtualViewId, int action, Bundle bundle) {
            if (action != 16) {
                return false;
            }
            return onItemClicked(virtualViewId);
        }

        @Override
        public void onPopulateAccessibilityEvent(View view, AccessibilityEvent accessibilityEvent) {
            super.onPopulateAccessibilityEvent(view, accessibilityEvent);
        }

        @Override
        public void onPopulateEventForVirtualView(int virtualViewId, AccessibilityEvent accessibilityEvent) {
            accessibilityEvent.getText().add(getItemDescription(virtualViewId));
        }

        @Override
        public void onPopulateNodeForVirtualView(int virtualViewId, @NonNull AccessibilityNodeInfoCompat accessibilityNodeInfoCompat) {
            accessibilityNodeInfoCompat.setContentDescription(getItemDescription(virtualViewId));
            accessibilityNodeInfoCompat.setClassName(COUIAccessibilityUtil.BUTTON_CLASS_NAME);
            accessibilityNodeInfoCompat.addAction(AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_CLICK);
            accessibilityNodeInfoCompat.setClickable(true);
            accessibilityNodeInfoCompat.setBoundsInParent(getBoundsForVirtualView(virtualViewId));
        }
    }

    public static class SideStyle {
        private float mAlpha;
        private String mDescription;
        private Drawable mDrawable;
        private boolean mIsDisappearing;
        private COUISpringAnimation mSideStyleAlphaAnimator;
        private String mText;
        private int mTextColor;
        private float mTextSize;
        private int mType;

        public static class Builder {
            private String mDescription;
            private Drawable mDrawable;
            private String mText;
            private int mTextColor;
            private float mTextSize;
            private int mType = 0;

            public SideStyle build() {
                return new SideStyle(this);
            }

            public Builder description(String str) {
                this.mDescription = str;
                return this;
            }

            public Builder drawable(Drawable drawable) {
                this.mDrawable = drawable;
                return this;
            }

            public Builder text(String str) {
                this.mText = str;
                return this;
            }

            public Builder textColor(int row) {
                this.mTextColor = row;
                return this;
            }

            public Builder textSize(float x) {
                this.mTextSize = x;
                return this;
            }

            public Builder type(int row) {
                this.mType = row;
                return this;
            }
        }

        private SideStyle(Builder builder) {
            this.mAlpha = 0.0f;
            this.mSideStyleAlphaAnimator = null;
            this.mIsDisappearing = false;
            this.mDrawable = builder.mDrawable;
            this.mText = builder.mText;
            this.mTextColor = builder.mTextColor;
            this.mTextSize = builder.mTextSize;
            this.mDescription = builder.mDescription;
            this.mType = builder.mType;
        }
    }

    public COUINumericKeyboard(Context context) {
        this(context, null);
    }

    public void callback(int row) {
        OnClickItemListener onClickItemListener = this.mOnClickItemListener;
        if (onClickItemListener != null) {
            if (row >= 0 && row <= 8) {
                onClickItemListener.onClickNumber(row + 1);
            }
            if (row == 10) {
                this.mOnClickItemListener.onClickNumber(0);
            }
            if (row == 9) {
                this.mOnClickItemListener.onClickLeft();
            }
            if (row == 11) {
                this.mOnClickItemListener.onClickRight();
            }
        }
    }

    public Cell checkForNewHit(float x, float y) {
        int columnHit;
        int rowHit = getRowHit(y);
        if (rowHit >= 0 && (columnHit = getColumnHit(x)) >= 0) {
            return of(rowHit, columnHit);
        }
        return null;
    }

    public void checkRange(int row, int column) {
        if (row < 0 || row > 3) {
            throw new IllegalArgumentException("row must be in range 0-3");
        }
        if (column < 0 || column > 2) {
            throw new IllegalArgumentException("column must be in range 0-2");
        }
    }

    private void drawBackground(Canvas canvas, float x, float y, int alpha, int translateX, int translateY, float scale) {
        int radius = this.mNumberBackgroundRadius;
        this.mNumberBackground.setBounds(((int) (x - (radius * scale))) + translateX, ((int) (y - (radius * scale))) + translateY, ((int) (x + (radius * scale))) + translateX, ((int) (y + (radius * scale))) + translateY);
        this.mNumberBackground.setAlpha(alpha);
        this.mNumberBackground.draw(canvas);
    }

    private void drawCell(Canvas canvas, int column, int row) {
        Paint[] paintArr;
        Cell cell = this.sCells[row][column];
        float centerXForColumn = getCenterXForColumn(column);
        float centerYForRow = getCenterYForRow(row);
        int padding = (row * 3) + column;
        if (padding == 9) {
            drawSide(this.mLeftStyle, canvas, centerXForColumn, centerYForRow, cell);
            return;
        }
        if (padding == 11) {
            drawSide(this.mRightStyle, canvas, centerXForColumn, centerYForRow, cell);
            return;
        }
        if (padding != -1) {
            this.mNumberTextPaint.setTextSize(this.mKeyboardNumberTextSize * cell.mButtonScale);
            float fMeasureText = this.mNumberTextPaint.measureText(cell.cellNumberStr);
            Paint.FontMetrics fontMetrics = this.mNumberTextFontMetrics;
            float centerY = (centerYForRow - ((fontMetrics.descent + fontMetrics.ascent) / 2.0f)) - this.mNumberOffsetY;
            this.mNumberTextPaint.setAlpha((int) (cell.cellNumberAlpha * this.mKeyboardNumberTextAlpha));
            int pressEffectStyle = this.mPressEffectStyle;
            if (pressEffectStyle == 0) {
                this.mNumberBackground.setColor(this.mNumberBackgroundColor);
                drawBackground(canvas, centerXForColumn, centerYForRow, (int) (cell.cellNumberAlpha * this.mNumberBackgroundAlpha), cell.cellNumberTranslateX, cell.cellNumberTranslateY, 1.0f);
            } else if (pressEffectStyle == 1) {
                KeyboardDrawDelegate keyboardDrawDelegate = this.mDrawDelegate;
                if (keyboardDrawDelegate != null) {
                    RectF rectF = this.mNumberBounds;
                    int backgroundRadius = this.mNumberBackgroundRadius;
                    float buttonScale = cell.mButtonScale;
                    rectF.left = (int) (centerXForColumn - (backgroundRadius * buttonScale));
                    rectF.top = (int) (centerYForRow - (backgroundRadius * buttonScale));
                    rectF.right = (int) ((backgroundRadius * buttonScale) + centerXForColumn);
                    rectF.bottom = (int) ((backgroundRadius * buttonScale) + centerYForRow);
                    Paint[] customKeyboardPaint = keyboardDrawDelegate.getCustomKeyboardPaint(row, column, rectF);
                    if (customKeyboardPaint != null && customKeyboardPaint.length > 0) {
                        canvas.save();
                        int length = customKeyboardPaint.length;
                        int paintIndex = 0;
                        while (paintIndex < length) {
                            Paint paint = customKeyboardPaint[paintIndex];
                            if (paint == null) {
                                paintArr = customKeyboardPaint;
                            } else {
                                paint.setAlpha((int) (paint.getAlpha() * cell.cellNumberAlpha));
                                RectF rectF2 = this.mTranslateBounds;
                                RectF rectF3 = this.mNumberBounds;
                                float left = rectF3.left;
                                int translateX = cell.cellNumberTranslateX;
                                rectF2.left = left + translateX;
                                float top = rectF3.top;
                                int translateY = cell.cellNumberTranslateY;
                                paintArr = customKeyboardPaint;
                                rectF2.top = top + translateY;
                                rectF2.right = rectF3.right + translateX;
                                rectF2.bottom = rectF3.bottom + translateY;
                                canvas.drawOval(rectF2, paint);
                            }
                            paintIndex++;
                            customKeyboardPaint = paintArr;
                        }
                        canvas.restore();
                    }
                }
                drawInnerShadowLayer(canvas, centerXForColumn, centerYForRow, cell, cell.cellNumberTranslateX, cell.cellNumberTranslateY, cell.cellNumberAlpha);
                drawInnerBorder(canvas, centerXForColumn, centerYForRow, cell, cell.cellNumberTranslateX, cell.cellNumberTranslateY, cell.cellNumberAlpha);
            }
            this.mNumberTextPaint.setAlpha((int) (cell.cellNumberAlpha * this.mKeyboardNumberTextAlpha));
            canvas.drawText(cell.cellNumberStr, (centerXForColumn - (fMeasureText / 2.0f)) + cell.cellNumberTranslateX, centerY + cell.cellNumberTranslateY, this.mNumberTextPaint);
        }
    }

    private void drawInnerBorder(Canvas canvas, float x, float y, Cell cell, int translateX, int translateY, float alpha) {
        int iSave = canvas.save();
        this.mButtonPath.reset();
        this.mButtonPath.addCircle(x + translateX, y + translateY, this.mNumberBackgroundRadius * cell.mButtonScale, Path.Direction.CW);
        canvas.clipPath(this.mButtonPath);
        this.mBorderLinePaint.setStyle(Paint.Style.STROKE);
        this.mBorderLinePaint.setStrokeWidth(this.mButtonBorderWidth * 2.0f);
        if (cell.mInnerLightAlpha > 0.0f) {
            this.mBorderLinePaint.setColor(this.mBorderLineHighLightAlpha);
            this.mBorderLinePaint.setAlpha((int) cell.mInnerLightAlpha);
            this.mBorderLinePaint.setBlendMode(BlendMode.LUMINOSITY);
            canvas.drawPath(this.mButtonPath, this.mBorderLinePaint);
        }
        this.mBorderLinePaint.setColor(this.mBorderLineColor);
        this.mBorderLinePaint.setAlpha((int) (this.mBorderLineAlpha * alpha));
        canvas.drawPath(this.mButtonPath, this.mBorderLinePaint);
        canvas.restoreToCount(iSave);
    }

    private void drawInnerShadowLayer(Canvas canvas, float x, float y, Cell cell, int translateX, int translateY, float alpha) {
        int iSave = canvas.save();
        if (this.mInnerShadowBitmapPaint == null) {
            this.mInnerShadowBitmapPaint = new Paint();
        }
        this.mInnerShadowBitmapPaint.setAlpha((int) (alpha * DEFAULT_ALPHA_VALUE));
        Matrix matrix = this.mInnerShadowMatrix;
        if (matrix == null) {
            this.mInnerShadowMatrix = new Matrix();
        } else {
            matrix.reset();
        }
        Matrix matrix2 = this.mInnerShadowMatrix;
        float buttonScale = cell.mButtonScale;
        matrix2.postScale(buttonScale, buttonScale);
        Matrix matrix3 = this.mInnerShadowMatrix;
        float translatedX = x + translateX;
        int backgroundRadius = this.mNumberBackgroundRadius;
        float scale = cell.mButtonScale;
        matrix3.postTranslate(translatedX - (backgroundRadius * scale), (y + translateY) - (backgroundRadius * scale));
        canvas.drawBitmap(this.mInnerShadowBitmap, this.mInnerShadowMatrix, this.mInnerShadowBitmapPaint);
        canvas.clipPath(this.mShadowLayerPath);
        canvas.restoreToCount(iSave);
    }

    private void drawLightEffect(Canvas canvas, int column, int row) {
        Cell cell = this.sCells[row][column];
        if (cell == null || getTouchIndex(cell) == -1 || cell.mInnerLightAlpha <= 0.0f) {
            return;
        }
        cell.refreshLightEffectDrawPath();
        float centerXForColumn = getCenterXForColumn(cell.column);
        float centerYForRow = getCenterYForRow(cell.row);
        canvas.save();
        canvas.translate(centerXForColumn, centerYForRow);
        cell.mLightEffectHelper.drawLightEffect(canvas, cell.mButtonScale, cell.mLightEffectPath, this.mClipPaint, 0.0f, 0.0f);
        canvas.restore();
    }

    private void drawPressCircle(Canvas canvas, int column, int row) {
        Cell cell = this.sCells[row][column];
        if (cell != null) {
            float centerXForColumn = getCenterXForColumn(cell.column);
            float centerYForRow = getCenterYForRow(cell.row);
            if (getTouchIndex(cell) != -1) {
                if (cell.normalAlpha >= 0.0f || cell.blurAlpha >= 0.0f) {
                    int radius = this.mCircleRadius;
                    int left = (int) (centerXForColumn - radius);
                    int top = (int) (centerYForRow - radius);
                    int right = (int) (radius + centerXForColumn);
                    int bottom = (int) (radius + centerYForRow);
                    canvas.save();
                    int pressedColor = this.mPressedColor;
                    if (pressedColor != cell.pressedColor) {
                        cell.setCircleColor(pressedColor);
                    }
                    float normalScale = cell.normalScale;
                    canvas.scale(normalScale, normalScale, centerXForColumn, centerYForRow);
                    cell.normalCircle.setAlpha((int) Math.max(0.0f, cell.normalAlpha * DEFAULT_ALPHA_VALUE));
                    cell.normalCircle.setBounds(left, top, right, bottom);
                    cell.normalCircle.draw(canvas);
                    canvas.restore();
                    canvas.save();
                    float blurScale = cell.blurScale;
                    canvas.scale(blurScale, blurScale, centerXForColumn, centerYForRow);
                    cell.blurCircle.setBounds(left, top, right, bottom);
                    cell.blurCircle.setAlpha((int) Math.max(0.0f, cell.blurAlpha * DEFAULT_ALPHA_VALUE));
                    cell.blurCircle.draw(canvas);
                    canvas.restore();
                    if (cell.normalAlpha == 0.0f) {
                        cell.normalAlpha = -1.0f;
                    }
                    if (cell.blurAlpha == 0.0f) {
                        cell.blurAlpha = -1.0f;
                    }
                }
            }
        }
    }

    private void drawSide(SideStyle sideStyle, Canvas canvas, float x, float y, Cell cell) {
        if (isEmptyStyle(sideStyle)) {
            return;
        }
        this.mNumberBackground.setColor(this.mSideBackgroundColor);
        if (sideStyle.mDrawable != null) {
            int intrinsicWidth = (int) (x - ((sideStyle.mDrawable.getIntrinsicWidth() * cell.mButtonScale) / 2.0f));
            int intrinsicWidth2 = (int) (intrinsicWidth + (sideStyle.mDrawable.getIntrinsicWidth() * cell.mButtonScale));
            int intrinsicHeight = (int) (y - ((sideStyle.mDrawable.getIntrinsicHeight() * cell.mButtonScale) / 2.0f));
            int intrinsicHeight2 = (int) (intrinsicHeight + (sideStyle.mDrawable.getIntrinsicHeight() * cell.mButtonScale));
            drawBackground(canvas, x, y, (int) (this.mDrawableAlpha * sideStyle.mAlpha), this.mDrawableTranslateX, this.mDrawableTranslateY, cell.mButtonScale);
            Drawable drawable = sideStyle.mDrawable;
            int translateX = this.mDrawableTranslateX;
            int translateY = this.mDrawableTranslateY;
            drawable.setBounds(intrinsicWidth + translateX, intrinsicHeight + translateY, intrinsicWidth2 + translateX, intrinsicHeight2 + translateY);
            sideStyle.mDrawable.setAlpha((int) (this.mDrawableAlpha * sideStyle.mAlpha));
            sideStyle.mDrawable.draw(canvas);
        } else if (!TextUtils.isEmpty(sideStyle.mText)) {
            this.mWordTextPaint.setTextSize(sideStyle.mTextSize * cell.mButtonScale);
            this.mWordTextPaint.setColor(sideStyle.mTextColor);
            this.mWordTextPaint.setAlpha((int) (this.mTextAlpha * sideStyle.mAlpha));
            float fMeasureText = this.mWordTextPaint.measureText(sideStyle.mText);
            this.mWordTextFontMetrics = this.mWordTextPaint.getFontMetricsInt();
            drawBackground(canvas, x, y, (int) (this.mTextAlpha * sideStyle.mAlpha), this.mTextTranslateX, this.mTextTranslateY, cell.mButtonScale);
            canvas.drawText(sideStyle.mText, (x - (fMeasureText / 2.0f)) + this.mTextTranslateX, (y - ((this.mWordTextFontMetrics.descent + this.mWordTextFontMetrics.ascent) / 2.0f)) + this.mTextTranslateY, this.mWordTextPaint);
        }
        if (this.mPressEffectStyle == 1) {
            drawInnerBorder(canvas, x, y, cell, 0, 0, 0.0f);
        }
    }

    private void ensureButtonScaleAnimator(final Cell cell) {
        if (cell != null && cell.mButtonScaleHelper == null) {
            COUIPressFeedbackHelper cOUIPressFeedbackHelper = new COUIPressFeedbackHelper(getContext());
            cell.mButtonScaleHelper = cOUIPressFeedbackHelper;
            cOUIPressFeedbackHelper.setCallback(new COUIPressFeedbackHelper.COUIPressFeedbackHelperCallback() {
                @Override
                public int getTargetHeight() {
                    return COUINumericKeyboard.this.mNumberBackgroundRadius * 2;
                }

                @Override
                public int getTargetWidth() {
                    return COUINumericKeyboard.this.mNumberBackgroundRadius * 2;
                }

                @Override
                public void onScaleUpdate(float scale) {
                    Cell nextCell = cell;
                    nextCell.mButtonScale = scale;
                    COUINumericKeyboard.this.invalidatePaths(nextCell);
                    COUINumericKeyboard.this.invalidate();
                }
            });
        }
    }

    private void ensureLightEffectAnimator(final Cell cell) {
        if (cell != null && cell.mLightEffectHelper == null) {
            LightEffectHelper lightEffectHelper = new LightEffectHelper(this, this.mNumberBackgroundRadius, this.mLightShaderRadius, this.mGradient2, this.mGradient);
            cell.mLightEffectHelper = lightEffectHelper;
            lightEffectHelper.setCallback(new LightEffectHelper.LightEffectHelperCallback() {
                @Override
                public void onInnerLightUpdate(float alpha) {
                    cell.mInnerLightAlpha = alpha;
                }
            });
        }
    }

    private void ensureRightStyleAnimator(final SideStyle sideStyle) {
        if (sideStyle.mSideStyleAlphaAnimator == null) {
            COUISpringForce cOUISpringForce = new COUISpringForce();
            cOUISpringForce.setBounce(0.0f);
            cOUISpringForce.setResponse(SIDE_STYLE_SPRING_RESPONSE);
            sideStyle.mSideStyleAlphaAnimator = new COUISpringAnimation(new FloatValueHolder(sideStyle.mAlpha));
            sideStyle.mSideStyleAlphaAnimator.setSpring(cOUISpringForce);
            sideStyle.mSideStyleAlphaAnimator.addUpdateListener(new COUIDynamicAnimation.OnAnimationUpdateListener() {
                @Override
                public void onAnimationUpdate(COUIDynamicAnimation cOUIDynamicAnimation, float x, float y) {
                    COUINumericKeyboard.this.onRightStyleAlphaUpdate(sideStyle, cOUIDynamicAnimation, x, y);
                }
            });
        }
    }

    private void executeLightEffectAnimator(Cell cell, boolean partOfPattern) {
        if (cell != null) {
            ensureLightEffectAnimator(cell);
            ensureButtonScaleAnimator(cell);
            cell.mLightEffectHelper.executeLightEffectAnimator(partOfPattern);
            cell.mButtonScaleHelper.executeFeedbackAnimator(partOfPattern);
            if (partOfPattern) {
                return;
            }
            cell.pointerId = -1;
        }
    }

    private Cell findCellByPointerId(int index) {
        for (int row = 0; row < 4; row++) {
            for (int column = 0; column < 3; column++) {
                Cell cell = this.sCells[row][column];
                if (cell.pointerId == index) {
                    return cell;
                }
            }
        }
        return null;
    }

    public float getCenterXForColumn(int column) {
        return getPaddingLeft() + (this.mCellWidth / 2.0f) + (this.mCellWidth * column) + (this.mHorizontalSpacing * column);
    }

    public float getCenterYForRow(int row) {
        return getPaddingTop() + (this.mCellHeight / 2.0f) + (this.mCellHeight * row) + (this.mVerticalSpacing * row);
    }

    private int getColumnHit(float x) {
        for (int column = 0; column < 3; column++) {
            int centerXForColumn = (int) getCenterXForColumn(column);
            int halfSpacing = Math.max(0, Math.min(this.mHorizontalSpacing / 2, this.mAdditionalPressableArea));
            int left = Math.max((centerXForColumn - (this.mCellWidth / 2)) - halfSpacing, 0);
            int right = centerXForColumn + (this.mCellWidth / 2) + halfSpacing;
            if (left <= x && x <= right) {
                return column;
            }
        }
        return -1;
    }

    private int[] getDeleteCellIndex() {
        SideStyle sideStyle = this.mLeftStyle;
        if (sideStyle != null && sideStyle.mType == 1) {
            return new int[]{0, 3};
        }
        SideStyle sideStyle2 = this.mRightStyle;
        if (sideStyle2 == null || sideStyle2.mType != 1) {
            return null;
        }
        return new int[]{2, 3};
    }

    private int[] getFinishCellIndex() {
        SideStyle sideStyle = this.mLeftStyle;
        if (sideStyle != null && sideStyle.mType == 2) {
            return new int[]{0, 3};
        }
        SideStyle sideStyle2 = this.mRightStyle;
        if (sideStyle2 == null || sideStyle2.mType != 2) {
            return null;
        }
        return new int[]{2, 3};
    }

    private float[] getKeyboardNumberPosition(int keyCode) {
        int column;
        int row = 3;
        if (keyCode >= 8 && keyCode <= 16) {
            int mapped = keyCode - 8;
            column = mapped % 3;
            row = mapped / 3;
        } else if (keyCode >= 145 && keyCode <= 153) {
            int cellIndex = keyCode - 145;
            column = cellIndex % 3;
            row = cellIndex / 3;
        } else if (keyCode == 67) {
            int[] deleteCellIndex = getDeleteCellIndex();
            if (deleteCellIndex == null || deleteCellIndex.length != 2) {
                return new float[]{-1.0f, -1.0f};
            }
            column = deleteCellIndex[0];
            row = deleteCellIndex[1];
        } else if (keyCode == 7 || keyCode == 144) {
            column = 1;
        } else {
            if (keyCode != 66 && keyCode != FADE_ANIMATOR_TIME) {
                return new float[]{-1.0f, -1.0f};
            }
            int[] finishCellIndex = getFinishCellIndex();
            if (finishCellIndex == null || finishCellIndex.length != 2) {
                return new float[]{-1.0f, -1.0f};
            }
            column = finishCellIndex[0];
            row = finishCellIndex[1];
        }
        Cell cell = this.sCells[row][column];
        float centerXForColumn = getCenterXForColumn(column);
        float centerYForRow = getCenterYForRow(row);
        Paint.FontMetrics fontMetrics = this.mNumberTextFontMetrics;
        return new float[]{centerXForColumn + cell.cellNumberTranslateX, (centerYForRow - ((fontMetrics.descent + fontMetrics.ascent) / 2.0f)) + cell.cellNumberTranslateY};
    }

    private int getRowHit(float y) {
        for (int row = 0; row < 4; row++) {
            int centerYForRow = (int) getCenterYForRow(row);
            int halfSpacing = Math.max(0, Math.min(this.mVerticalSpacing / 2, this.mAdditionalPressableArea));
            int top = Math.max((centerYForRow - (this.mCellHeight / 2)) - halfSpacing, 0);
            int bottom = centerYForRow + (this.mCellHeight / 2) + halfSpacing;
            if (top <= y && y <= bottom) {
                return row;
            }
        }
        return -1;
    }

    private Typeface getTypeface(int[] iArr) {
        try {
            final java.io.File fontFile = new java.io.File(this.mTtfPath);
            if (!fontFile.isFile()) {
                return Typeface.create("sans-serif", Typeface.NORMAL);
            }
            if (iArr[0] == 0) {
                return new Typeface.Builder(this.mTtfPath).build();
            }
            return new Typeface.Builder(this.mTtfPath)
                    .setFontVariationSettings("'wght' " + (iArr[1] + this.mFontVariationDefaultPlus))
                    .build();
        } catch (RuntimeException e) {
            return Typeface.create("sans-serif", Typeface.NORMAL);
        }
    }

    private synchronized void handleActionCancel(int row) {
        try {
            Cell cellFindCellByPointerId = findCellByPointerId(row);
            int column = this.mPressEffectStyle;
            if (column == 0) {
                initFadeAnimator(cellFindCellByPointerId);
            } else if (column == 1) {
                executeLightEffectAnimator(cellFindCellByPointerId, false);
            }
            int touchIndex = getTouchIndex(cellFindCellByPointerId);
            if (this.mAccessibilityManagerService.isTouchExplorationEnabled() && cellFindCellByPointerId != null) {
                this.mExploreByTouchHelper.invalidateRoot();
                if (this.mEnableHapticFeedback && touchIndex != -1) {
                    setTouchFeedback();
                }
            }
            invalidate();
        } catch (Throwable th) {
            throw th;
        }
    }

    private void handleActionDown(MotionEvent motionEvent, int row) {
        handleActionDown(motionEvent.getX(row), motionEvent.getY(row), motionEvent.getPointerId(row));
    }

    private void handleActionMove(MotionEvent motionEvent, int row) {
        int iFindPointerIndex = motionEvent.findPointerIndex(row);
        if (iFindPointerIndex >= 0) {
            handleActionMove(motionEvent.getX(iFindPointerIndex), motionEvent.getY(iFindPointerIndex), row);
        }
    }

    private void handleActionUp(MotionEvent motionEvent, int row) {
        handleActionUp(motionEvent.getX(row), motionEvent.getY(row), motionEvent.getPointerId(row));
    }

    private void handleKeyEvent(int row, boolean partOfPattern) {
        if (isValidKeyCode(row)) {
            float[] keyboardNumberPosition = getKeyboardNumberPosition(row);
            if (partOfPattern) {
                handleActionDown(keyboardNumberPosition[0], keyboardNumberPosition[1], -1);
            } else {
                handleActionUp(keyboardNumberPosition[0], keyboardNumberPosition[1], -1);
            }
        }
    }

    private void initCellAnim(Cell cell, List<Animator> list, int animIndex) {
        cell.setCellNumberAlpha(0.0f);
        cell.setCellNumberTranslateY(this.mMaxTranslateY);
        ObjectAnimator objectAnimatorOfFloat = ObjectAnimator.ofFloat(cell, "cellNumberAlpha", 0.0f, 1.0f);
        int alphaIndex = (animIndex == 10 && isEmptyStyle(this.mLeftStyle)) ? animIndex - 1 : animIndex;
        objectAnimatorOfFloat.setStartDelay(ALPHA_DELAY + (((long) alphaIndex) * ALPHA_OFFSET));
        objectAnimatorOfFloat.setDuration(ALPHA_DURATION);
        objectAnimatorOfFloat.setInterpolator(this.mAlphaInterpolator);
        list.add(objectAnimatorOfFloat);
        ObjectAnimator objectAnimatorOfInt = ObjectAnimator.ofInt(cell, "cellNumberTranslateY", this.mMaxTranslateY, 0);
        if (animIndex == 10 && isEmptyStyle(this.mLeftStyle)) {
            animIndex--;
        }
        objectAnimatorOfInt.setStartDelay(TRANSLATE_Y_OFFSET * ((long) animIndex));
        objectAnimatorOfInt.setDuration(TRANSLATE_Y_DURATION);
        objectAnimatorOfInt.setInterpolator(this.mTranslateYInterpolator);
        list.add(objectAnimatorOfInt);
    }

    private void initFadeAnimator(final Cell cell) {
        if (cell == null) {
            return;
        }
        cell.pointerId = -1;
        if (cell.fadeAnimator == null) {
            ValueAnimator valueAnimatorOfPropertyValuesHolder = ValueAnimator.ofPropertyValuesHolder(PropertyValuesHolder.ofFloat("scaleHolder", FADE_START_SCALE, FADE_END_SCALE), PropertyValuesHolder.ofFloat("alphaHolder", this.mCircleMaxAlpha, 0.0f));
            valueAnimatorOfPropertyValuesHolder.setDuration(FADE_ANIMATOR_TIME);
            valueAnimatorOfPropertyValuesHolder.setInterpolator(PATH_INTERPOLATOR);
            valueAnimatorOfPropertyValuesHolder.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    Cell nextCell = cell;
                    if (nextCell == null) {
                        return;
                    }
                    nextCell.normalAlpha = ((Float) valueAnimator.getAnimatedValue("alphaHolder")).floatValue();
                    cell.normalScale = ((Float) valueAnimator.getAnimatedValue("scaleHolder")).floatValue();
                    COUINumericKeyboard.this.invalidate();
                }
            });
            cell.fadeAnimator = valueAnimatorOfPropertyValuesHolder;
        }
        if (cell.blurFadeAnimator == null) {
            ValueAnimator animation = ValueAnimator.ofPropertyValuesHolder(PropertyValuesHolder.ofKeyframe("blurAlpha", Keyframe.ofFloat(0.0f, 0.0f), Keyframe.ofFloat(0.5f, this.mCircleMaxAlpha), Keyframe.ofFloat(1.0f, 0.0f)), PropertyValuesHolder.ofFloat("blurScale", BLUR_START_SCALE, BLUR_END_SCALE));
            animation.setDuration(FADE_BLUR_ANIMATOR_TIME);
            animation.setInterpolator(PATH_INTERPOLATOR);
            animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    Cell nextCell = cell;
                    if (nextCell == null) {
                        return;
                    }
                    nextCell.blurAlpha = ((Float) valueAnimator.getAnimatedValue("blurAlpha")).floatValue();
                    cell.blurScale = ((Float) valueAnimator.getAnimatedValue("blurScale")).floatValue();
                    COUINumericKeyboard.this.invalidate();
                }
            });
            cell.blurFadeAnimator = animation;
        }
        ValueAnimator valueAnimator = cell.showAnimator;
        if (valueAnimator != null && valueAnimator.isRunning()) {
            cell.showAnimator.end();
        }
        cell.fadeAnimator.start();
        cell.blurFadeAnimator.start();
    }

    private void initInnerShadowBitmap() {
        Path path = this.mShadowLayerPath;
        if (path == null) {
            this.mShadowLayerPath = new Path();
        } else {
            path.reset();
        }
        Path path2 = this.mShadowLayerPath;
        int row = this.mNumberBackgroundRadius;
        path2.addCircle(row, row, row, Path.Direction.CCW);
        InnerShadowHelper innerShadowHelper = this.mInnerShadowHelper;
        if (innerShadowHelper == null) {
            this.mInnerShadowHelper = new InnerShadowHelper(this.mCellWidth, this.mCellHeight);
        } else {
            innerShadowHelper.reset();
        }
        this.mInnerShadowHelper.addInnerShadowLayer(INNER_SHADOW_RADIUS_1, INNER_SHADOW_DX, INNER_SHADOW_DY_1, this.mUpperInnerShadowColor, 0, INNER_SHADOW_STROKE_WIDTH_1, this.mShadowLayerPath);
        this.mInnerShadowHelper.addInnerShadowLayer(INNER_SHADOW_RADIUS_2, INNER_SHADOW_DX, INNER_SHADOW_DY_2, this.mLowerInnerShadowColor, 0, INNER_SHADOW_STROKE_WIDTH_2, this.mShadowLayerPath);
        this.mInnerShadowBitmap = this.mInnerShadowHelper.createInnerShadowBitmap();
    }

    private void initPaint() {
        Paint paint = new Paint(5);
        this.mPaint = paint;
        paint.setColor(this.mPressedColor);
        this.mPaint.setMaskFilter(new BlurMaskFilter(20.0f, BlurMaskFilter.Blur.NORMAL));
        this.mPaint.setAlpha(0);
        this.mNumberTextPaint.setTextSize(this.mKeyboardNumberTextSize);
        this.mNumberTextPaint.setColor(this.mKeyboardNumberTextColor);
        this.mNumberTextPaint.setAntiAlias(true);
        this.mKeyboardNumberTextAlpha = this.mNumberTextPaint.getAlpha();
        if (this.mHasCustomTypeface) {
            Typeface typeface = this.mCustomTypeface;
            if (typeface != null) {
                this.mNumberTextPaint.setTypeface(typeface);
                invalidate();
            }
        } else {
            updateNumberTextTypeface();
        }
        this.mNumberTextFontMetrics = this.mNumberTextPaint.getFontMetrics();
        this.mLinePaint.setColor(this.mKeyboardLineColor);
        this.mLinePaint.setAntiAlias(true);
        this.mLinePaint.setStyle(Paint.Style.STROKE);
        this.mWordTextPaint.setFakeBoldText(true);
        this.mWordTextPaint.setAntiAlias(true);
    }

    private void initRadialGradient() {
        float x = this.mLightShaderRadius;
        Shader.TileMode tileMode = Shader.TileMode.CLAMP;
        this.mGradient = new RadialGradient(0.0f, 0.0f, x, new int[]{0, this.mOuterGradientColor1, this.mOuterGradientColor2, this.mOuterGradientColor3, 0}, new float[]{GRADIENT_COLOR_STOP_START, GRADIENT_OUTER_STOP_1, GRADIENT_OUTER_STOP_2, GRADIENT_OUTER_STOP_3, GRADIENT_COLOR_STOP_END}, tileMode);
        this.mGradient2 = new RadialGradient(0.0f, 0.0f, this.mNumberBackgroundRadius, new int[]{0, this.mInnerGradientColor1, this.mInnerGradientColor2}, new float[]{GRADIENT_COLOR_STOP_START, GRADIENT_INNER_STOP_1, GRADIENT_COLOR_STOP_END}, tileMode);
    }

    private void initShowAnimator(final Cell cell) {
        if (cell == null) {
            return;
        }
        if (cell.showAnimator == null) {
            ValueAnimator valueAnimatorOfPropertyValuesHolder = ValueAnimator.ofPropertyValuesHolder(PropertyValuesHolder.ofFloat("scaleHolder", SHOW_START_SCALE, SHOW_END_SCALE), PropertyValuesHolder.ofFloat("alphaHolder", 0.0f, this.mCircleMaxAlpha));
            valueAnimatorOfPropertyValuesHolder.setDuration(SHOW_ANIMATOR_TIME);
            valueAnimatorOfPropertyValuesHolder.setInterpolator(DEFAULT_OUT_EASE_INTERPOLATOR);
            valueAnimatorOfPropertyValuesHolder.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    Cell nextCell = cell;
                    if (nextCell == null) {
                        return;
                    }
                    nextCell.normalAlpha = ((Float) valueAnimator.getAnimatedValue("alphaHolder")).floatValue();
                    cell.normalScale = ((Float) valueAnimator.getAnimatedValue("scaleHolder")).floatValue();
                    COUINumericKeyboard.this.invalidate();
                }
            });
            cell.showAnimator = valueAnimatorOfPropertyValuesHolder;
        }
        cell.showAnimator.removeAllListeners();
        if (cell.showAnimator.isRunning()) {
            cell.showAnimator.end();
        }
        ValueAnimator valueAnimator = cell.fadeAnimator;
        if (valueAnimator != null && valueAnimator.isRunning()) {
            cell.fadeAnimator.end();
        }
        cell.showAnimator.start();
    }

    private void initSideAnim(SideStyle sideStyle, List<Animator> list, int row) {
        if (isEmptyStyle(sideStyle)) {
            return;
        }
        if (sideStyle.mDrawable != null) {
            setDrawableAlpha(0.0f);
            setDrawableTranslateY(this.mMaxTranslateY);
            ObjectAnimator objectAnimatorOfFloat = ObjectAnimator.ofFloat(this, "drawableAlpha", 0.0f, 1.0f);
            long alphaDelay = ALPHA_DELAY + (((long) row) * ALPHA_OFFSET);
            long translateDelay = ((long) row) * TRANSLATE_Y_OFFSET;
            objectAnimatorOfFloat.setStartDelay(alphaDelay);
            objectAnimatorOfFloat.setDuration(ALPHA_DURATION);
            objectAnimatorOfFloat.setInterpolator(this.mAlphaInterpolator);
            list.add(objectAnimatorOfFloat);
            ObjectAnimator objectAnimatorOfInt = ObjectAnimator.ofInt(this, "drawableTranslateY", this.mMaxTranslateY, 0);
            objectAnimatorOfInt.setStartDelay(translateDelay);
            objectAnimatorOfInt.setDuration(TRANSLATE_Y_DURATION);
            objectAnimatorOfInt.setInterpolator(this.mTranslateYInterpolator);
            list.add(objectAnimatorOfInt);
            return;
        }
        if (TextUtils.isEmpty(sideStyle.mText)) {
            return;
        }
        setTextAlpha(0.0f);
        setTextTranslateY(this.mMaxTranslateY);
        ObjectAnimator objectAnimatorOfFloat2 = ObjectAnimator.ofFloat(this, "textAlpha", 0.0f, 1.0f);
        long alphaDelayMs = ALPHA_DELAY + (((long) row) * ALPHA_OFFSET);
        long translateDelayMs = ((long) row) * TRANSLATE_Y_OFFSET;
        objectAnimatorOfFloat2.setStartDelay(alphaDelayMs);
        objectAnimatorOfFloat2.setDuration(ALPHA_DURATION);
        objectAnimatorOfFloat2.setInterpolator(this.mAlphaInterpolator);
        list.add(objectAnimatorOfFloat2);
        ObjectAnimator objectAnimatorOfInt2 = ObjectAnimator.ofInt(this, "textTranslateY", this.mMaxTranslateY, 0);
        objectAnimatorOfInt2.setStartDelay(translateDelayMs);
        objectAnimatorOfInt2.setDuration(TRANSLATE_Y_DURATION);
        objectAnimatorOfInt2.setInterpolator(this.mTranslateYInterpolator);
        list.add(objectAnimatorOfInt2);
    }

    public void invalidatePaths(Cell cell) {
        Cell nextCell;
        int top;
        if (cell == null) {
            return;
        }
        for (int row = 0; row < 4; row++) {
            for (int column = 0; column < 3; column++) {
                int cellRow = cell.row;
                if ((row != cellRow || column != cell.column) && (top = (nextCell = this.sCells[row][column]).row) >= cellRow - 1) {
                    int nextColumn = nextCell.column;
                    int cellColumn = cell.column;
                    if (nextColumn >= cellColumn - 1 && top <= cellRow + 1 && nextColumn <= cellColumn + 1) {
                        nextCell.mInvalidatePaths = true;
                    }
                }
            }
        }
    }

    public boolean isEmptyStyle(SideStyle sideStyle) {
        return sideStyle == null || (sideStyle.mDrawable == null && TextUtils.isEmpty(sideStyle.mText)) || sideStyle.mAlpha == 0.0f;
    }

    private boolean isMultiPointerEvent(MotionEvent motionEvent) {
        return motionEvent.getPointerId(motionEvent.getActionIndex()) > 0;
    }

    private boolean isValidKeyCode(int keyCode) {
        return (keyCode >= 7 && keyCode <= 16) || (keyCode >= 144 && keyCode <= 153) || keyCode == 67 || keyCode == 66 || keyCode == FADE_ANIMATOR_TIME;
    }

    public /* synthetic */ void onRightStyleAlphaUpdate(SideStyle sideStyle, COUIDynamicAnimation cOUIDynamicAnimation, float x, float y) {
        sideStyle.mAlpha = x;
        invalidate();
    }

    private boolean needFadeWhenDisabled(int row) {
        return this.mNormalAlpha > 0.0f && (1 == row || 3 == row || row == 0);
    }

    private void setTouchFeedback() {
        if (this.mIsLinearMotorVersion) {
            performHapticFeedback(302);
        } else {
            performHapticFeedback(301);
        }
    }

    private void setTouchSoundFeedBack() {
        playSoundEffect(0);
    }

    private void showSideStyle(SideStyle sideStyle, boolean partOfPattern) {
        if (sideStyle == null) {
            return;
        }
        ensureRightStyleAnimator(sideStyle);
        sideStyle.mSideStyleAlphaAnimator.animateToFinalPosition(partOfPattern ? DEFAULT_ALPHA_VALUE : 0.0f);
        sideStyle.mIsDisappearing = !partOfPattern;
    }

    private void updateNumberTextTypeface() {
        int[] statusAndVariation = getStatusAndVariation();
        if (statusAndVariation == null) {
            return;
        }
        // Prefer SysSans when present (OPPO). Without it, wght on Roboto looks too bold.
        final java.io.File fontFile = new java.io.File(this.mTtfPath);
        if (fontFile.isFile()) {
            final Typeface typeface = getTypeface(statusAndVariation);
            if (typeface != null) {
                this.mNumberTextPaint.setTypeface(typeface);
            }
            this.mNumberTextPaint.setFontVariationSettings(
                    "'wght' " + (statusAndVariation[1] + this.mFontVariationDefaultPlus));
        } else {
            this.mNumberTextPaint.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
        }
        this.mNumberTextFontMetrics = this.mNumberTextPaint.getFontMetrics();
        invalidate();
    }

    @Override
    public boolean dispatchHoverEvent(MotionEvent motionEvent) {
        return this.mExploreByTouchHelper.dispatchHoverEvent(motionEvent) | super.dispatchHoverEvent(motionEvent);
    }

    public AnimatorSet getEnterAnim() {
        AnimatorSet animatorSet = new AnimatorSet();
        ArrayList arrayList = new ArrayList();
        for (int row = 0; row < 4; row++) {
            for (int column = 0; column < 3; column++) {
                Cell cellOf = of(row, column);
                int animIndex = (row * 3) + column;
                if (animIndex == 9) {
                    initSideAnim(this.mLeftStyle, arrayList, animIndex);
                } else if (animIndex == 11) {
                    SideStyle sideStyle = this.mRightStyle;
                    if (isEmptyStyle(this.mLeftStyle)) {
                        animIndex--;
                    }
                    initSideAnim(sideStyle, arrayList, animIndex);
                } else {
                    initCellAnim(cellOf, arrayList, animIndex);
                }
            }
        }
        animatorSet.playTogether(arrayList);
        return animatorSet;
    }

    public int[] getStatusAndVariation() {
        int variationSettings = Settings.System.getInt(this.mContext.getContentResolver(), FONT_VARIATION_SETTINGS, FONT_VARIATION_DEFAULT);
        int[] statusAndVariation = {(61440 & variationSettings) >> 12, variationSettings & 4095};
        int preVariation = this.mPreVariation;
        int variation = statusAndVariation[1];
        if (preVariation == variation) {
            return null;
        }
        this.mPreVariation = variation;
        return statusAndVariation;
    }

    @Deprecated
    public int getTouchIndex() {
        return 0;
    }

    public boolean isTactileFeedbackEnabled() {
        return this.mEnableHapticFeedback;
    }

    public synchronized Cell of(int row, int column) {
        checkRange(row, column);
        return this.sCells[row][column];
    }

    @Override
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        if (this.mHasCustomTypeface) {
            return;
        }
        updateNumberTextTypeface();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mPaint != null) {
            this.mPaint = null;
        }
        if (this.mTouchCell != null) {
            this.mTouchCell = null;
        }
        this.mDownState = false;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int index = this.mPressEffectStyle;
        if (index == 0) {
            for (int row = 0; row < 4; row++) {
                for (int column = 0; column < 3; column++) {
                    drawPressCircle(canvas, column, row);
                    drawCell(canvas, column, row);
                }
            }
            return;
        }
        if (index != 1) {
            return;
        }
        for (int row = 0; row < 4; row++) {
            for (int column = 0; column < 3; column++) {
                drawCell(canvas, column, row);
            }
        }
        for (int row = 0; row < 4; row++) {
            for (int column = 0; column < 3; column++) {
                drawLightEffect(canvas, column, row);
            }
        }
    }

    @Override
    public boolean onHoverEvent(MotionEvent motionEvent) {
        if (this.mAccessibilityManagerService.isTouchExplorationEnabled()) {
            int action = motionEvent.getAction();
            if (action == 7) {
                motionEvent.setAction(2);
            } else if (action == 9) {
                motionEvent.setAction(0);
            } else if (action == 10) {
                motionEvent.setAction(1);
            }
            onTouchEvent(motionEvent);
            motionEvent.setAction(action);
        }
        return super.onHoverEvent(motionEvent);
    }

    @Override
    public boolean onKeyDown(int row, KeyEvent keyEvent) {
        if (keyEvent.getRepeatCount() == 0) {
            handleKeyEvent(row, true);
        }
        return super.onKeyDown(row, keyEvent);
    }

    @Override
    public boolean onKeyUp(int row, KeyEvent keyEvent) {
        if (keyEvent.getScanCode() != 0) {
            handleKeyEvent(row, false);
        }
        return super.onKeyUp(row, keyEvent);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int mode = View.MeasureSpec.getMode(widthMeasureSpec);
        int size = View.MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = View.MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = View.MeasureSpec.getSize(heightMeasureSpec);
        if (mode == View.MeasureSpec.AT_MOST || mode == View.MeasureSpec.UNSPECIFIED) {
            size = this.mDefaultWidth;
        }
        if (heightMode == View.MeasureSpec.AT_MOST || heightMode == View.MeasureSpec.UNSPECIFIED) {
            heightSize = this.mDefaultHeight;
        }
        setMeasuredDimension(size, heightSize);
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        int action = this.mViewSize;
        this.mCellWidth = action;
        this.mCellHeight = action;
        this.mNumberBackgroundRadius = action / 2;
        this.mHorizontalSpacing = (((getWidth() - getPaddingLeft()) - getPaddingRight()) - (this.mCellWidth * 3)) / 2;
        int height = (getHeight() - getPaddingTop()) - getPaddingBottom();
        int left = this.mCellHeight;
        this.mVerticalSpacing = (height - (left * 4)) / 3;
        this.mCircleRadius = left / 2;
        if (this.mPressEffectStyle == 1) {
            initRadialGradient();
            initInnerShadowBitmap();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int actionIndex = event.getActionIndex();
        final int actionMasked = event.getActionMasked();
        if (!isEnabled()) {
            if (needFadeWhenDisabled(actionMasked)) {
                final int pointerCount = event.getPointerCount();
                for (int i = 0; i < pointerCount; i++) {
                    handleActionCancel(event.getPointerId(i));
                }
            }
            return false;
        }
        final Cell hit = checkForNewHit(event.getX(actionIndex), event.getY(actionIndex));
        final int touchIndex = getTouchIndex(hit);
        if (touchIndex == 9
                && this.mLeftStyle != null
                && this.mLeftStyle.mSideStyleAlphaAnimator != null
                && this.mLeftStyle.mSideStyleAlphaAnimator.isRunning()
                && this.mLeftStyle.mIsDisappearing) {
            return false;
        }
        if (touchIndex == 11
                && this.mRightStyle != null
                && this.mRightStyle.mSideStyleAlphaAnimator != null
                && this.mRightStyle.mSideStyleAlphaAnimator.isRunning()
                && this.mRightStyle.mIsDisappearing) {
            return false;
        }
        if (touchIndex != -1 && hit != null && hit.cellNumberAlpha < 1.0f) {
            return false;
        }
        switch (actionMasked) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                this.mDownState = true;
                handleActionDown(event, actionIndex);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                this.mDownState = false;
                handleActionUp(event, actionIndex);
                break;
            case MotionEvent.ACTION_MOVE:
                final int moveCount = event.getPointerCount();
                for (int i = 0; i < moveCount; i++) {
                    handleActionMove(event, event.getPointerId(i));
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                final int cancelCount = event.getPointerCount();
                for (int i = 0; i < cancelCount; i++) {
                    handleActionCancel(event.getPointerId(i));
                }
                this.mDownState = false;
                break;
            default:
                break;
        }
        return true;
    }

    public void refresh() {
        String resourceTypeName = getResources().getResourceTypeName(this.mStyle);
        TypedArray typedArrayObtainStyledAttributes = null;
        if ("attr".equals(resourceTypeName)) {
            typedArrayObtainStyledAttributes = this.mContext.obtainStyledAttributes(null, R.styleable.COUINumericKeyboard, this.mStyle, 0);
        } else if ("style".equals(resourceTypeName)) {
            typedArrayObtainStyledAttributes = this.mContext.obtainStyledAttributes(null, R.styleable.COUINumericKeyboard, 0, this.mStyle);
        }
        if (typedArrayObtainStyledAttributes != null) {
            this.mPressedColor = typedArrayObtainStyledAttributes.getColor(R.styleable.COUINumericKeyboard_couiNumPressColor, 0);
            this.mKeyboardNumberTextColor = typedArrayObtainStyledAttributes.getColor(R.styleable.COUINumericKeyboard_couiNumberColor, 0);
            this.mKeyboardLineColor = typedArrayObtainStyledAttributes.getColor(R.styleable.COUINumericKeyboard_couiLineColor, 0);
            this.mCircleMaxAlpha = typedArrayObtainStyledAttributes.getFloat(R.styleable.COUINumericKeyboard_couiCircleMaxAlpha, 0.0f);
            this.mNumberBackgroundColor = typedArrayObtainStyledAttributes.getColor(R.styleable.COUINumericKeyboard_couiNumberBackgroundColor, 0);
            this.mSideBackgroundColor = typedArrayObtainStyledAttributes.getColor(R.styleable.COUINumericKeyboard_couiSideBackgroundColor, 0);
            typedArrayObtainStyledAttributes.recycle();
        }
        this.mKeyboardDelete.setTint(this.mKeyboardNumberTextColor);
        initPaint();
    }

    public void setCellViewSize(int row) {
        this.mViewSize = row;
    }

    public void setCircleMaxAlpha(int row) {
        setCircleMaxAlpha(row / DEFAULT_ALPHA_VALUE);
    }

    public void setCustomTypeFace(Typeface typeface) {
        if (this.mHasCustomTypeface) {
            this.mCustomTypeface = typeface;
            this.mNumberTextPaint.setTypeface(typeface);
            invalidate();
        }
    }

    public void setDeleteStyle(Drawable drawable) {
        this.mDeleteStyle = new SideStyle.Builder().drawable(drawable).description(getResources().getString(R.string.coui_number_keyboard_delete)).type(1).build();
    }

    public void setDrawableAlpha(float x) {
        this.mDrawableAlpha = x;
        invalidate();
    }

    public void setDrawableTranslateX(int row) {
        this.mDrawableTranslateX = row;
        invalidate();
    }

    public void setDrawableTranslateY(int row) {
        this.mDrawableTranslateY = row;
        invalidate();
    }

    @Override
    public void setEnabled(boolean partOfPattern) {
        Paint paint;
        if (!partOfPattern && this.mDownState && (paint = this.mPaint) != null) {
            paint.setAlpha(0);
            this.mDownState = false;
            invalidate();
        }
        super.setEnabled(partOfPattern);
    }

    @Deprecated
    public void setHasFinishButton(boolean partOfPattern) {
    }

    @Deprecated
    public void setItemTouchListener(OnItemTouchListener onItemTouchListener) {
    }

    public void setKeyboardDrawDelegate(KeyboardDrawDelegate keyboardDrawDelegate) {
        this.mDrawDelegate = keyboardDrawDelegate;
    }

    public void setKeyboardLineColor(int row) {
        this.mKeyboardLineColor = row;
        initPaint();
    }

    public void setKeyboardNumberTextColor(int row) {
        this.mKeyboardNumberTextColor = row;
        this.mKeyboardDelete.setTint(row);
    }

    public void setLeftStyle(SideStyle sideStyle) {
        this.mLeftStyle = sideStyle;
        this.mExploreByTouchHelper.invalidateVirtualView(9);
        if (sideStyle != null) {
            sideStyle.mAlpha = DEFAULT_ALPHA_VALUE;
        }
        invalidate();
    }

    public void setNumberBackgroundColor(int row) {
        this.mNumberBackgroundColor = row;
    }

    public void setNumberOffsetY(float x) {
        if (this.mNumberOffsetY != x) {
            this.mNumberOffsetY = x;
            invalidate();
        }
    }

    public void setOnClickItemListener(OnClickItemListener onClickItemListener) {
        this.mOnClickItemListener = onClickItemListener;
    }

    public void setPressedColor(int row) {
        this.mPressedColor = row;
        initPaint();
    }

    public void setRightStyle(SideStyle sideStyle) {
        this.mRightStyle = sideStyle;
        this.mExploreByTouchHelper.invalidateVirtualView(11);
        if (sideStyle != null) {
            sideStyle.mAlpha = DEFAULT_ALPHA_VALUE;
        }
        invalidate();
    }

    public void setSideBackgroundColor(int row) {
        this.mSideBackgroundColor = row;
    }

    public void setTactileFeedbackEnabled(boolean enabled) {
        this.mEnableHapticFeedback = enabled;
    }

    public void setTextAlpha(float x) {
        this.mTextAlpha = x;
        invalidate();
    }

    public void setTextTranslateX(int row) {
        this.mTextTranslateX = row;
        invalidate();
    }

    public void setTextTranslateY(int row) {
        this.mTextTranslateY = row;
        invalidate();
    }

    @Deprecated
    public void setTouchTextListener(OnTouchTextListener onTouchTextListener) {
    }

    @Deprecated
    public void setTouchUpListener(OnTouchUpListener onTouchUpListener) {
    }

    @Deprecated
    public void setType(int row) {
    }

    public void setWordTextNormalColor(int row) {
        this.mFinishStyle.mTextColor = row;
    }

    public COUINumericKeyboard(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, R.attr.couiNumericKeyboardStyle);
    }

    private int getTouchIndex(Cell cell) {
        if (cell == null) {
            return -1;
        }
        int row = (cell.getRow() * 3) + cell.getColumn();
        if (row == 9 && isEmptyStyle(this.mLeftStyle)) {
            row = -1;
        }
        if (row == 11 && isEmptyStyle(this.mRightStyle)) {
            return -1;
        }
        return row;
    }

    private void handleActionDown(float x, float y, int column) {
        if (this.mAccessibilityManagerService.isTouchExplorationEnabled()) {
            return;
        }
        Cell cellCheckForNewHit = checkForNewHit(x, y);
        if (cellCheckForNewHit != null) {
            int touchIndex = getTouchIndex(cellCheckForNewHit);
            this.mExploreByTouchHelper.invalidateRoot();
            if (this.mEnableHapticFeedback && touchIndex != -1) {
                setTouchFeedback();
            }
            if (column != -1) {
                cellCheckForNewHit.pointerId = column;
            }
            int row = this.mPressEffectStyle;
            if (row == 0) {
                initShowAnimator(cellCheckForNewHit);
            } else if (row == 1) {
                executeLightEffectAnimator(cellCheckForNewHit, true);
            }
        }
        invalidate();
    }

    private void handleActionUp(float x, float y, int row) {
        int column;
        Cell cellCheckForNewHit = checkForNewHit(x, y);
        int touchIndex = getTouchIndex(cellCheckForNewHit);
        if (this.mAccessibilityManagerService.isTouchExplorationEnabled()) {
            if (cellCheckForNewHit == null || (column = cellCheckForNewHit.pointerId) == -1 || column != row) {
                return;
            }
            this.mExploreByTouchHelper.invalidateRoot();
            if (!this.mEnableHapticFeedback || touchIndex == -1) {
                return;
            }
            setTouchFeedback();
            return;
        }
        if (cellCheckForNewHit != null && cellCheckForNewHit.pointerId == row) {
            callback(touchIndex);
        }
        if (row != -1 && (cellCheckForNewHit == null || cellCheckForNewHit.pointerId != row)) {
            cellCheckForNewHit = findCellByPointerId(row);
        }
        int index = this.mPressEffectStyle;
        if (index == 0) {
            initFadeAnimator(cellCheckForNewHit);
        } else if (index == 1) {
            executeLightEffectAnimator(cellCheckForNewHit, false);
        }
        if (touchIndex != -1 && isEnabled() && !hasOnClickListeners()) {
            setTouchSoundFeedBack();
        }
        invalidate();
    }

    public void setCircleMaxAlpha(float x) {
        if (x < 0.0f || x > 1.0f) {
            COUILog.e(TAG, "The alpha value must be greater than or equal to 0 and less than or equal to 1");
            return;
        }
        this.mCircleMaxAlpha = x;
        for (int row = 0; row < 4; row++) {
            for (int column = 0; column < 3; column++) {
                Cell cell = this.sCells[row][column];
                if (cell != null) {
                    ValueAnimator valueAnimator = cell.showAnimator;
                    if (valueAnimator != null && !valueAnimator.isRunning()) {
                        this.sCells[row][column].showAnimator = null;
                    }
                    ValueAnimator animation = this.sCells[row][column].fadeAnimator;
                    if (animation != null && !animation.isRunning()) {
                        this.sCells[row][column].fadeAnimator = null;
                    }
                    ValueAnimator animator = this.sCells[row][column].blurFadeAnimator;
                    if (animator != null && !animator.isRunning()) {
                        this.sCells[row][column].blurFadeAnimator = null;
                    }
                }
            }
        }
    }

    public COUINumericKeyboard(Context context, AttributeSet attributeSet, int row) {
        this(context, attributeSet, row, R.style.Widget_COUI_COUINumericKeyboard);
    }

    private void handleActionMove(float x, float y, int row) {
        Cell cellCheckForNewHit = checkForNewHit(x, y);
        if (row != -1) {
            if (cellCheckForNewHit == null || cellCheckForNewHit.pointerId != row) {
                handleActionCancel(row);
            }
        }
    }

    public COUINumericKeyboard(Context context, AttributeSet attributeSet, int defStyleAttr, int defStyleRes) {
        super(context, attributeSet, defStyleAttr, defStyleRes);
        this.mClipPaint = new Paint(1);
        this.mNumberBounds = new RectF();
        this.mTranslateBounds = new RectF();
        this.mButtonPath = new Path();
        this.NUMERIC = 1;
        this.WORD = 2;
        this.mPaint = null;
        this.mTouchCell = null;
        this.mDrawDelegate = null;
        this.mEnableHapticFeedback = true;
        int left = 0;
        this.mDownState = false;
        this.sCells = (Cell[][]) Array.newInstance((Class<?>) Cell.class, 4, 3);
        this.mKeyboardDelete = null;
        this.mNumberBackgroundAlpha = 255;
        this.mKeyboardNumbers = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, -1, 0, -1};
        this.mNumberTextPaint = new TextPaint();
        this.mNumberTextFontMetrics = null;
        this.mWordTextFontMetrics = null;
        this.mLinePaint = new Paint();
        this.mKeyboardNumberTextSize = -1.0f;
        this.mKeyboardNumberTextColor = -1;
        this.mKeyboardNumberTextAlpha = 255;
        this.mKeyboardLineColor = -1;
        this.mBorderLineColor = 0;
        this.mBorderLineAlpha = 0;
        this.mBorderLineHighLightAlpha = 0;
        this.mBorderLineHighLightColor = 0;
        this.mWordTextPaint = new TextPaint();
        this.mNormalAlpha = 0.12f;
        this.mPreVariation = -1;
        this.mDrawableAlpha = 1.0f;
        this.mTextAlpha = 1.0f;
        this.mAlphaInterpolator = new COUIEaseInterpolator();
        this.mTranslateYInterpolator = new COUIInEaseInterpolator();
        this.mLightShaderRadius = 0;
        this.mPressEffectStyle = 0;
        if (attributeSet != null && attributeSet.getStyleAttribute() != 0) {
            this.mStyle = attributeSet.getStyleAttribute();
        } else {
            this.mStyle = defStyleAttr;
        }
        COUIDarkModeUtil.setForceDarkAllow(this, false);
        this.mContext = context;
        TypedArray typedArrayObtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.COUINumericKeyboard, defStyleAttr, defStyleRes);
        this.mPressedColor = typedArrayObtainStyledAttributes.getColor(R.styleable.COUINumericKeyboard_couiNumPressColor, 0);
        Resources resources = context.getResources();
        this.mLightShaderRadius = resources.getDimensionPixelOffset(R.dimen.coui_numeric_light_shader_radius);
        this.mDefaultWidth = resources.getDimensionPixelSize(R.dimen.coui_numeric_keyboard_view_width);
        this.mDefaultHeight = resources.getDimensionPixelSize(R.dimen.coui_numeric_keyboard_view_height);
        this.mViewSize = resources.getDimensionPixelSize(R.dimen.coui_numeric_keyboard_view_size);
        this.mAdditionalPressableArea = resources.getDimensionPixelOffset(R.dimen.coui_additional_pressable_area);
        this.mKeyboardNumberTextSize = typedArrayObtainStyledAttributes.getDimensionPixelSize(R.styleable.COUINumericKeyboard_couiNumberTextSize, resources.getDimensionPixelSize(R.dimen.number_keyboard_number_size));
        this.mMaxTranslateY = resources.getDimensionPixelSize(R.dimen.coui_numeric_keyboard_max_translate_y);
        this.mFontVariationDefaultPlus = resources.getInteger(R.integer.font_variation_default_plus);
        this.mNumberOffsetY = typedArrayObtainStyledAttributes.getDimension(R.styleable.COUINumericKeyboard_couiNumberOffsetY, resources.getDimensionPixelOffset(R.dimen.coui_numeric_keyboard_number_offset_y));
        this.mKeyboardNumberTextColor = typedArrayObtainStyledAttributes.getColor(R.styleable.COUINumericKeyboard_couiNumberColor, 0);
        this.mKeyboardLineColor = typedArrayObtainStyledAttributes.getColor(R.styleable.COUINumericKeyboard_couiLineColor, 0);
        int color = typedArrayObtainStyledAttributes.getColor(R.styleable.COUINumericKeyboard_couiWordTextNormalColor, 0);
        this.mCircleMaxAlpha = typedArrayObtainStyledAttributes.getFloat(R.styleable.COUINumericKeyboard_couiCircleMaxAlpha, 0.0f);
        this.mNumberBackgroundColor = typedArrayObtainStyledAttributes.getColor(R.styleable.COUINumericKeyboard_couiNumberBackgroundColor, 0);
        this.mSideBackgroundColor = typedArrayObtainStyledAttributes.getColor(R.styleable.COUINumericKeyboard_couiSideBackgroundColor, 0);
        this.mKeyboardDelete = typedArrayObtainStyledAttributes.getDrawable(R.styleable.COUINumericKeyboard_couiKeyboardDelete);
        this.mHasCustomTypeface = typedArrayObtainStyledAttributes.getBoolean(R.styleable.COUINumericKeyboard_couiSetCustomTypeface, false);
        this.mPressEffectStyle = typedArrayObtainStyledAttributes.getInt(R.styleable.COUINumericKeyboard_couiPressEffect, 0);
        if (!UIUtil.confirmLevelAnim(UIUtil.ANIM_LEVEL_SUPPORT_BLUR_MIN)) {
            this.mPressEffectStyle = 0;
        }
        typedArrayObtainStyledAttributes.recycle();
        this.mButtonBorderWidth = resources.getDimension(R.dimen.coui_keyboard_button_border_width);
        int color2 = resources.getColor(R.color.coui_numeric_keyboard_border_color);
        this.mBorderLineColor = color2;
        this.mBorderLineAlpha = Color.alpha(color2);
        int color3 = resources.getColor(R.color.coui_numeric_keyboard_border_highlight_color);
        this.mBorderLineHighLightColor = color3;
        this.mBorderLineHighLightAlpha = Color.alpha(color3);
        this.mUpperInnerShadowColor = resources.getColor(R.color.coui_numeric_keyboard_upper_inner_shadow_color);
        this.mLowerInnerShadowColor = resources.getColor(R.color.coui_numeric_keyboard_lower_inner_shadow_color);
        this.mOuterGradientColor1 = resources.getColor(R.color.coui_numeric_keyboard_outer_gradient_color_1);
        this.mOuterGradientColor2 = resources.getColor(R.color.coui_numeric_keyboard_outer_gradient_color_2);
        this.mOuterGradientColor3 = resources.getColor(R.color.coui_numeric_keyboard_outer_gradient_color_3);
        this.mInnerGradientColor1 = resources.getColor(R.color.coui_numeric_keyboard_inner_gradient_color_1);
        this.mInnerGradientColor2 = resources.getColor(R.color.coui_numeric_keyboard_inner_gradient_color_2);
        if (this.mKeyboardDelete == null) {
            this.mKeyboardDelete = context.getDrawable(R.drawable.ic_coui_number_keyboard_launhcer_delete);
        }
        PatternExploreByTouchHelper patternExploreByTouchHelper = new PatternExploreByTouchHelper(this);
        this.mExploreByTouchHelper = patternExploreByTouchHelper;
        ViewCompat.setAccessibilityDelegate(this, patternExploreByTouchHelper);
        setImportantForAccessibility(1);
        this.mExploreByTouchHelper.invalidateRoot();
        String[] stringArray = context.getResources().getStringArray(R.array.coui_number_keyboard_letters);
        this.mIsLinearMotorVersion = VibrateUtils.isLinearMotorVersion(context);
        GradientDrawable gradientDrawable = new GradientDrawable();
        this.mNumberBackground = gradientDrawable;
        gradientDrawable.setShape(1);
        this.mNumberBackground.setCornerRadius(this.mNumberBackgroundRadius);
        int top = 0;
        while (top < 4) {
            int index = left;
            while (index < 3) {
                this.sCells[top][index] = new Cell(top, index);
                Cell cell = this.sCells[top][index];
                int row = (top * 3) + index;
                cell.cellLettersStr = stringArray[row];
                int column = this.mKeyboardNumbers[row];
                if (column > -1) {
                    cell.cellNumberStr = String.format(Locale.getDefault(), "%d", Integer.valueOf(column));
                }
                index++;
            }
            top++;
            left = 0;
        }
        this.mTtfPath = getResources().getString(R.string.ttf_path);
        String string = getResources().getString(R.string.coui_numeric_keyboard_sure);
        this.mFinishStyle = new SideStyle.Builder().text(string).textColor(color).textSize(resources.getDimensionPixelSize(R.dimen.coui_number_keyboard_finish_text_size)).description(string).type(2).build();
        this.mKeyboardDelete.setTint(this.mKeyboardNumberTextColor);
        this.mDeleteStyle = new SideStyle.Builder().drawable(this.mKeyboardDelete).description(getResources().getString(R.string.coui_number_keyboard_delete)).type(1).build();
        this.mAccessibilityManagerService = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        initPaint();
        this.mInnerShadowMatrix = new Matrix();
        this.mBorderLinePaint = new Paint();
    }

    public void setRightStyle(SideStyle sideStyle, boolean partOfPattern) {
        if (!partOfPattern) {
            setRightStyle(sideStyle);
            return;
        }
        if (sideStyle != null) {
            this.mRightStyle = sideStyle;
            showSideStyle(sideStyle, true);
        } else {
            showSideStyle(this.mRightStyle, false);
        }
        this.mExploreByTouchHelper.invalidateVirtualView(11);
        invalidate();
    }
}
