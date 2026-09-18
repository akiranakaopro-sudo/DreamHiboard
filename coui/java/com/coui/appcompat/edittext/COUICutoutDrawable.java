package com.coui.appcompat.edittext;

import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextDirectionHeuristic;
import android.text.TextDirectionHeuristics;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.EditText;

import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;

import com.coui.appcompat.textutil.COUIChangeTextUtil;

import java.util.ArrayList;
import java.util.Locale;

public class COUICutoutDrawable extends GradientDrawable {
    private final RectF mCutoutBounds = new RectF();
    private final Paint mCutoutPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int mSavedLayer;

    public COUICutoutDrawable() {
        mCutoutPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mCutoutPaint.setColor(Color.WHITE);
        mCutoutPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
    }

    @Override
    public void draw(Canvas canvas) {
        preDraw(canvas);
        super.draw(canvas);
        canvas.drawRect(mCutoutBounds, mCutoutPaint);
        postDraw(canvas);
    }

    public RectF getCutout() {
        return mCutoutBounds;
    }

    public boolean hasCutout() {
        return !mCutoutBounds.isEmpty();
    }

    public void removeCutout() {
        setCutout(0f, 0f, 0f, 0f);
    }

    public void setCutout(RectF bounds) {
        setCutout(bounds.left, bounds.top, bounds.right, bounds.bottom);
    }

    public void setCutout(float left, float top, float right, float bottom) {
        if (left == mCutoutBounds.left && top == mCutoutBounds.top
                && right == mCutoutBounds.right && bottom == mCutoutBounds.bottom) {
            return;
        }
        mCutoutBounds.set(left, top, right, bottom);
        invalidateSelf();
    }

    private void preDraw(Canvas canvas) {
        Drawable.Callback callback = getCallback();
        if (callback instanceof View) {
            ((View) callback).setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            mSavedLayer = canvas.saveLayer(0f, 0f, canvas.getWidth(), canvas.getHeight(), null);
        }
    }

    private void postDraw(Canvas canvas) {
        if (!(getCallback() instanceof View)) {
            canvas.restoreToCount(mSavedLayer);
        }
    }

    public static final class COUICollapseTextHelper {
        public static final int DEFAULT_HINT_LINES = 1;
        public static final int MAX_HINT_LINES = 3;
        private static final float POINT_001 = 0.001f;
        private static final float SCALE_MY = 1.3f;

        private final View mView;
        private final TextPaint mTextPaint;
        private final TextPaint mTmpPaint;
        private final Rect mCollapsedBounds = new Rect();
        private final Rect mExpandedBounds = new Rect();
        private final RectF mCurrentBounds = new RectF();
        private CharSequence mText;
        private CharSequence mTextToDraw;
        private final ArrayList<CharSequence> mTextToDrawList = new ArrayList<>();
        private ColorStateList mCollapsedTextColor;
        private ColorStateList mExpandedTextColor;
        private int[] mState;
        private int mCollapsedTextGravity = Gravity.CENTER_VERTICAL;
        private int mExpandedTextGravity = Gravity.CENTER_VERTICAL;
        private float mExpandedTextSize = 30f;
        private float mCollapsedTextSize = 30f;
        private float mCurrentTextSize;
        private float mCollapsedDrawX;
        private float mCollapsedDrawY;
        private float mExpandedDrawX;
        private float mExpandedDrawY;
        private float mCurrentDrawX;
        private float mCurrentDrawY;
        private float mExpandedFraction;
        private float mScale = 1f;
        private float mHintPaddingStart;
        private int mHintLines = DEFAULT_HINT_LINES;
        private boolean mBoundsChanged;
        private boolean mDrawTitle;
        private boolean mIsRtl;
        private Interpolator mPositionInterpolator;
        private Interpolator mTextSizeInterpolator;

        public COUICollapseTextHelper(View view) {
            mView = view;
            mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
            mTmpPaint = new TextPaint(mTextPaint);
        }

        public void draw(Canvas canvas) {
            int save = canvas.save();
            float lineSpacingExtra = 0.0f;
            if (mTextToDraw == null || !mDrawTitle) {
                canvas.drawText(" ", 0f, 0f, mTextPaint);
                canvas.restoreToCount(save);
                return;
            }
            float x = mCurrentDrawX;
            float y = mCurrentDrawY;
            if (mScale != 1f) {
                canvas.scale(mScale, mScale, x, y);
            }
            float lineSpacingMultiplier = 1.0f;
            if (mView instanceof EditText) {
                EditText editText = (EditText) mView;
                lineSpacingExtra = editText.getLineSpacingExtra();
                lineSpacingMultiplier = editText.getLineSpacingMultiplier();
            }
            CharSequence drawText = mHintLines > 1 ? mText : mTextToDraw;
            StaticLayout layout = StaticLayout.Builder.obtain(drawText, 0, drawText.length(),
                            mTextPaint, (int) mCurrentBounds.width())
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setIncludePad(true)
                    .setEllipsize(TextUtils.TruncateAt.END)
                    .setTextDirection(getTextDirectionHeuristic())
                    .setMaxLines(mHintLines)
                    .setLineSpacing(lineSpacingExtra, lineSpacingMultiplier)
                    .build();
            if (layout != null) {
                canvas.save();
                canvas.translate(mIsRtl ? mCurrentBounds.left - mHintPaddingStart
                                : mCurrentBounds.left + mHintPaddingStart,
                        y - layout.getLineBaseline(0));
                layout.draw(canvas);
                canvas.restore();
            }
            canvas.restoreToCount(save);
        }

        public float calculateCollapsedTextWidth() {
            if (mText == null) {
                return 0f;
            }
            mTmpPaint.setTextSize(mCollapsedTextSize);
            return mTmpPaint.measureText(mText, 0, mText.length());
        }

        public void getCollapsedTextActualBounds(RectF out) {
            boolean rtl = calculateIsRtl(mText);
            float width = calculateCollapsedTextWidth();
            float left = rtl ? mCollapsedBounds.right - width : mCollapsedBounds.left;
            out.left = left;
            out.top = mCollapsedBounds.top;
            out.right = rtl ? mCollapsedBounds.right : left + width;
            out.bottom = mCollapsedBounds.top + getCollapsedTextHeight();
        }

        public Rect getCollapsedBounds() {
            return mCollapsedBounds;
        }

        public Rect getExpandedBounds() {
            return mExpandedBounds;
        }

        public ColorStateList getCollapsedTextColor() {
            return mCollapsedTextColor;
        }

        public int getCollapsedTextGravity() {
            return mCollapsedTextGravity;
        }

        public float getCollapsedTextHeight() {
            mTmpPaint.setTextSize(mCollapsedTextSize);
            return Locale.getDefault().getLanguage().equals("my")
                    ? -mTmpPaint.ascent() * SCALE_MY : -mTmpPaint.ascent();
        }

        public float getCollapsedTextSize() {
            return mCollapsedTextSize;
        }

        public int getCurrentCollapsedTextColor() {
            if (mCollapsedTextColor == null) {
                return Color.TRANSPARENT;
            }
            return mState != null ? mCollapsedTextColor.getColorForState(mState, 0)
                    : mCollapsedTextColor.getDefaultColor();
        }

        public float getExpandedFraction() {
            return mExpandedFraction;
        }

        public ColorStateList getExpandedTextColor() {
            return mExpandedTextColor;
        }

        public int getCurrentExpandedTextColor() {
            if (mExpandedTextColor == null) {
                return Color.TRANSPARENT;
            }
            return mState != null ? mExpandedTextColor.getColorForState(mState, 0)
                    : mExpandedTextColor.getDefaultColor();
        }

        public int getExpandedTextGravity() {
            return mExpandedTextGravity;
        }

        private TextDirectionHeuristic getTextDirectionHeuristic() {
            return isRtlMode() ? TextDirectionHeuristics.RTL : TextDirectionHeuristics.LTR;
        }

        public float getExpandedTextSize() {
            return mExpandedTextSize;
        }

        public float getExpansionFraction() {
            return mExpandedFraction;
        }

        public float getHintHeight() {
            mTmpPaint.setTextSize(mCollapsedTextSize);
            float height = mTmpPaint.descent() - mTmpPaint.ascent();
            return Locale.getDefault().getLanguage().equals("my") ? height * SCALE_MY : height;
        }

        public CharSequence getText() {
            return mText;
        }

        public boolean isStateful() {
            return (mCollapsedTextColor != null && mCollapsedTextColor.isStateful())
                    || (mExpandedTextColor != null && mExpandedTextColor.isStateful());
        }

        public void recalculate() {
            if (mView.getHeight() <= 0 || mView.getWidth() <= 0) {
                return;
            }
            calculateBaseOffsets();
            calculateCurrentOffsets();
        }

        public void setCollapsedBounds(int left, int top, int right, int bottom) {
            if (rectEquals(mCollapsedBounds, left, top, right, bottom)) {
                return;
            }
            mCollapsedBounds.set(left, top, right, bottom);
            mBoundsChanged = true;
            onBoundsChanged();
        }

        public void setCollapsedTextAppearance(int textSize, ColorStateList color) {
            mCollapsedTextColor = color;
            mCollapsedTextSize = textSize;
            recalculate();
        }

        public void setCollapsedTextColor(ColorStateList color) {
            if (mCollapsedTextColor != color) {
                mCollapsedTextColor = color;
                recalculate();
            }
        }

        public void setCollapsedTextGravity(int gravity) {
            if (mCollapsedTextGravity != gravity) {
                mCollapsedTextGravity = gravity;
                recalculate();
            }
        }

        public void setCollapsedTextSize(float size) {
            if (mCollapsedTextSize != size) {
                mCollapsedTextSize = size;
                recalculate();
            }
        }

        public void setExpandedBounds(int left, int top, int right, int bottom) {
            if (rectEquals(mExpandedBounds, left, top, right, bottom)) {
                return;
            }
            mExpandedBounds.set(left, top, right, bottom);
            mBoundsChanged = true;
            onBoundsChanged();
        }

        public void setExpandedTextColor(ColorStateList color) {
            if (mExpandedTextColor != color) {
                mExpandedTextColor = color;
                recalculate();
            }
        }

        public void setExpandedTextGravity(int gravity) {
            if (mExpandedTextGravity != gravity) {
                mExpandedTextGravity = gravity;
                recalculate();
            }
        }

        public void setExpandedTextSize(float size) {
            if (mExpandedTextSize != size) {
                mExpandedTextSize = size;
                recalculate();
            }
        }

        public void setExpansionFraction(float fraction) {
            float constrained = constrain(fraction, 0f, 1f);
            if (constrained != mExpandedFraction) {
                mExpandedFraction = constrained;
                calculateCurrentOffsets();
            }
        }

        public void setHintLines(int lines) {
            mHintLines = Math.min(MAX_HINT_LINES, Math.max(DEFAULT_HINT_LINES, lines));
        }

        public void setHintPaddingStart(float paddingStart) {
            if (paddingStart > 0f) {
                mHintPaddingStart = paddingStart;
            }
        }

        public void setPositionInterpolator(Interpolator interpolator) {
            mPositionInterpolator = interpolator;
            recalculate();
        }

        public boolean setState(int[] state) {
            mState = state;
            if (!isStateful()) {
                return false;
            }
            recalculate();
            return true;
        }

        public void setText(CharSequence text) {
            if (text == null || !text.equals(mText)) {
                mText = text;
                mTextToDraw = null;
                mTextToDrawList.clear();
                recalculate();
            }
        }

        public void setTextSizeInterpolator(Interpolator interpolator) {
            mTextSizeInterpolator = interpolator;
            recalculate();
        }

        public void setTypefaces(Typeface typeface) {
            COUIChangeTextUtil.adaptBoldAndMediumFont(mTextPaint, true);
            COUIChangeTextUtil.adaptBoldAndMediumFont(mTmpPaint, true);
            recalculate();
        }

        private void calculateBaseOffsets() {
            float currentTextSize = mCurrentTextSize;
            calculateUsingTextSize(mCollapsedTextSize);
            float collapsedWidth = calculateTextWidth();
            int collapsedGravity = GravityCompat.getAbsoluteGravity(mCollapsedTextGravity,
                    isRtlMode() ? ViewCompat.LAYOUT_DIRECTION_RTL : ViewCompat.LAYOUT_DIRECTION_LTR);
            int collapsedVertical = collapsedGravity & Gravity.VERTICAL_GRAVITY_MASK;
            if (mHintLines <= 1 && collapsedVertical == Gravity.CENTER_VERTICAL) {
                mCollapsedDrawY = mCollapsedBounds.centerY()
                        + (((mTextPaint.descent() - mTextPaint.ascent()) / 2f) - mTextPaint.descent());
            } else if (collapsedVertical == Gravity.BOTTOM) {
                mCollapsedDrawY = mCollapsedBounds.bottom;
            } else {
                mCollapsedDrawY = Locale.getDefault().getLanguage().equals("my")
                        ? mCollapsedBounds.top - (mTextPaint.ascent() * SCALE_MY)
                        : mCollapsedBounds.top - mTextPaint.ascent();
            }
            int collapsedHorizontal = collapsedGravity & Gravity.HORIZONTAL_GRAVITY_MASK;
            if (collapsedHorizontal == Gravity.CENTER_HORIZONTAL) {
                mCollapsedDrawX = mCollapsedBounds.centerX() - (collapsedWidth / 2f);
            } else if (collapsedHorizontal == Gravity.RIGHT) {
                mCollapsedDrawX = mCollapsedBounds.right - collapsedWidth;
            } else {
                mCollapsedDrawX = mCollapsedBounds.left;
            }

            calculateUsingTextSize(mExpandedTextSize);
            float expandedWidth = calculateTextWidth();
            int expandedGravity = GravityCompat.getAbsoluteGravity(mExpandedTextGravity,
                    isRtlMode() ? ViewCompat.LAYOUT_DIRECTION_RTL : ViewCompat.LAYOUT_DIRECTION_LTR);
            int expandedVertical = expandedGravity & Gravity.VERTICAL_GRAVITY_MASK;
            if (mHintLines > 1 || expandedVertical == Gravity.TOP) {
                mExpandedDrawY = mExpandedBounds.top - mTextPaint.ascent();
            } else if (expandedVertical == Gravity.BOTTOM) {
                mExpandedDrawY = mExpandedBounds.bottom;
            } else {
                Paint.FontMetrics fm = mTextPaint.getFontMetrics();
                mExpandedDrawY = mExpandedBounds.centerY() + (((fm.bottom - fm.top) / 2f) - fm.bottom);
            }
            int expandedHorizontal = expandedGravity & Gravity.HORIZONTAL_GRAVITY_MASK;
            if (expandedHorizontal == Gravity.CENTER_HORIZONTAL) {
                mExpandedDrawX = mExpandedBounds.centerX() - (expandedWidth / 2f);
            } else if (expandedHorizontal == Gravity.RIGHT) {
                mExpandedDrawX = mExpandedBounds.right - expandedWidth;
            } else {
                mExpandedDrawX = mExpandedBounds.left;
            }
            setInterpolatedTextSize(currentTextSize);
        }

        private void calculateCurrentOffsets() {
            calculateOffsets(mExpandedFraction);
        }

        private boolean calculateIsRtl(CharSequence text) {
            return isRtlMode();
        }

        private void calculateOffsets(float fraction) {
            interpolateBounds(fraction);
            mCurrentDrawX = lerp(mExpandedDrawX, mCollapsedDrawX, fraction, mPositionInterpolator);
            mCurrentDrawY = lerp(mExpandedDrawY, mCollapsedDrawY, fraction, mPositionInterpolator);
            setInterpolatedTextSize(lerp(mExpandedTextSize, mCollapsedTextSize, fraction,
                    mTextSizeInterpolator));
            if (mCollapsedTextColor != mExpandedTextColor && mCollapsedTextColor != null
                    && mExpandedTextColor != null) {
                mTextPaint.setColor(blendColors(getCurrentExpandedTextColor(),
                        getCurrentCollapsedTextColor(), fraction));
            } else if (mCollapsedTextColor != null) {
                mTextPaint.setColor(getCurrentCollapsedTextColor());
            }
            mView.postInvalidate();
        }

        private float calculateTextWidth() {
            CharSequence text = mTextToDraw;
            float measured = text != null ? mTextPaint.measureText(text, 0, text.length()) : 0f;
            return (mHintLines <= 1 || mTextToDraw == null || mTextToDrawList.isEmpty())
                    ? measured
                    : Math.max(mTextPaint.measureText(mTextToDrawList.get(0).toString()), measured);
        }

        private void calculateUsingTextSize(float textSize) {
            if (mText == null) {
                return;
            }
            float collapsedWidth = mCollapsedBounds.width();
            float expandedWidth = mExpandedBounds.width();
            float targetTextSize;
            if (isClose(textSize, mCollapsedTextSize)) {
                targetTextSize = mCollapsedTextSize;
                mScale = 1f;
            } else {
                targetTextSize = mExpandedTextSize;
                mScale = isClose(textSize, mExpandedTextSize) ? 1f : textSize / mExpandedTextSize;
                float textSizeRatio = mCollapsedTextSize / mExpandedTextSize;
                collapsedWidth = collapsedWidth < expandedWidth * textSizeRatio
                        ? Math.min(collapsedWidth / textSizeRatio, expandedWidth) : expandedWidth;
            }
            boolean changed = collapsedWidth > 0f
                    && (mCurrentTextSize != targetTextSize || mBoundsChanged);
            mCurrentTextSize = targetTextSize;
            mBoundsChanged = false;
            if (mTextToDraw == null || changed) {
                mTextPaint.setTextSize(mCurrentTextSize);
                mTextPaint.setLinearText(mScale != 1f);
                CharSequence ellipsized = TextUtils.ellipsize(mText, mTextPaint,
                        collapsedWidth - mHintPaddingStart, TextUtils.TruncateAt.END);
                if (!TextUtils.equals(ellipsized, mTextToDraw)) {
                    mTextToDraw = ellipsized;
                }
            }
            mIsRtl = isRtlMode();
        }

        private void interpolateBounds(float fraction) {
            mCurrentBounds.left = lerp(mExpandedBounds.left, mCollapsedBounds.left, fraction,
                    mPositionInterpolator);
            mCurrentBounds.top = lerp(mExpandedDrawY, mCollapsedDrawY, fraction,
                    mPositionInterpolator);
            mCurrentBounds.right = lerp(mExpandedBounds.right, mCollapsedBounds.right, fraction,
                    mPositionInterpolator);
            mCurrentBounds.bottom = lerp(mExpandedBounds.bottom, mCollapsedBounds.bottom, fraction,
                    mPositionInterpolator);
        }

        private void setInterpolatedTextSize(float textSize) {
            calculateUsingTextSize(textSize);
            mView.postInvalidate();
        }

        private boolean isRtlMode() {
            return ViewCompat.getLayoutDirection(mView) == ViewCompat.LAYOUT_DIRECTION_RTL;
        }

        private void onBoundsChanged() {
            mDrawTitle = mCollapsedBounds.width() > 0 && mCollapsedBounds.height() > 0
                    && mExpandedBounds.width() > 0 && mExpandedBounds.height() > 0;
        }

        private static int blendColors(int from, int to, float fraction) {
            float inverse = 1f - fraction;
            return Color.argb(
                    (int) ((Color.alpha(from) * inverse) + (Color.alpha(to) * fraction)),
                    (int) ((Color.red(from) * inverse) + (Color.red(to) * fraction)),
                    (int) ((Color.green(from) * inverse) + (Color.green(to) * fraction)),
                    (int) ((Color.blue(from) * inverse) + (Color.blue(to) * fraction)));
        }

        private static float constrain(float value, float min, float max) {
            return value < min ? min : Math.min(value, max);
        }

        private static boolean isClose(float left, float right) {
            return Math.abs(left - right) < POINT_001;
        }

        private static float lerp(float start, float end, float fraction) {
            return start + (fraction * (end - start));
        }

        private static float lerp(float start, float end, float fraction, Interpolator interpolator) {
            return lerp(start, end, interpolator == null ? fraction
                    : interpolator.getInterpolation(fraction));
        }

        private static boolean rectEquals(Rect rect, int left, int top, int right, int bottom) {
            return rect.left == left && rect.top == top && rect.right == right && rect.bottom == bottom;
        }
    }
}
