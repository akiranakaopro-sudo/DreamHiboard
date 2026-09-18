package com.coui.appcompat.chip;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Xml;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.annotation.StyleRes;
import androidx.annotation.XmlRes;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.text.BidiFormatter;
import androidx.dynamicanimation.animation.FloatPropertyCompat;

import com.coui.appcompat.R;
import com.coui.appcompat.animation.dynamicanimation.COUISpringAnimation;
import com.coui.appcompat.animation.dynamicanimation.COUISpringForce;
import com.coui.appcompat.state.COUIMaskEffectDrawable;
import com.coui.appcompat.state.COUIStrokeDrawable;
import com.coui.appcompat.state.StatefulDrawableListener;
import com.google.android.material.resources.MaterialResources;
import com.google.android.material.resources.TextAppearance;
import com.oplus.graphics.OplusPathAdapter;

import java.lang.ref.WeakReference;
import java.util.Arrays;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * COUI chip renderer.
 *
 * <p>This is the unified-resource port of OPPO's decoded {@code COUIChipDrawable}. It preserves
 * the original state palette, determinate sizing, close-icon bounds, RTL geometry, mask/stroke
 * effects and spring transitions while replacing the APK's split {@code com.support.*.R}
 * references with this library's generated {@link R}.</p>
 */
public class COUIChipDrawable extends Drawable implements Drawable.Callback,
        StatefulDrawableListener {
    private static final int[] DEFAULT_STATE = {android.R.attr.state_enabled};
    private static final float ANIMATION_RANGE = 10000.0f;
    private static final ArgbEvaluator ARGB_EVALUATOR = new ArgbEvaluator();

    public interface COUIChipDrawableDelegate {
        void onChipDrawableSizeChange();
        void onChipTextColorChange(int color);
        void onChipTextFontChanged(Typeface typeface);
        void onChipTextOffsetChanged(float x, float y);
    }

    public abstract static class ChipAnimation {
        final FloatPropertyCompat<ChipAnimation> mTransition;
        final COUISpringAnimation mAnimation;
        float mCurrentProgress;

        ChipAnimation(String name) {
            mTransition = new FloatPropertyCompat<ChipAnimation>(name) {
                @Override
                public float getValue(ChipAnimation animation) {
                    return animation.mCurrentProgress;
                }

                @Override
                public void setValue(ChipAnimation animation, float value) {
                    animation.mCurrentProgress = value;
                    animation.onProgressChanged(value / ANIMATION_RANGE);
                }
            };
            COUISpringForce force = new COUISpringForce();
            force.setBounce(0.0f);
            force.setResponse(0.3f);
            mAnimation = new COUISpringAnimation(this, mTransition);
            mAnimation.setSpring(force);
        }

        abstract void onProgressChanged(float fraction);
    }

    private final Context mContext;
    private final Paint mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final TextPaint mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final RectF mTmpBounds = new RectF();
    private final RectF mCloseIconTouchRect = new RectF();
    private final Path mBackgroundPath = new Path();
    private final COUIMaskEffectDrawable mMaskEffectDrawable;
    private final COUIMaskEffectDrawable mCloseIconEffectDrawable;
    private final COUIStrokeDrawable mStrokeDrawable;
    private WeakReference<COUIChipDrawableDelegate> mDelegate = new WeakReference<>(null);

    private CharSequence mText = "";
    private TextAppearance mDefaultTextAppearance;
    private TextAppearance mCheckedTextAppearance;
    private TextUtils.TruncateAt mTruncateAt = TextUtils.TruncateAt.END;
    private Drawable mChipBackground;
    private Drawable mChipIcon;
    private Drawable mCloseIcon;
    private CharSequence mCloseIconContentDescription;
    private int[] mCloseIconStateSet = DEFAULT_STATE;

    private boolean mShouldDrawText = true;
    private boolean mCheckable;
    private boolean mChecked;
    private boolean mChipIconVisible;
    private boolean mCloseIconVisible;
    private boolean mChipIconApplyTint = true;
    private boolean mShowRedDot;
    private boolean mCloseIconTouched;

    private int mCheckedBackgroundColor;
    private int mUncheckedBackgroundColor;
    private int mCheckedDisabledBackgroundColor;
    private int mUncheckedDisabledBackgroundColor;
    private int mCheckedTextColor;
    private int mUncheckedTextColor;
    private int mCheckedDisabledTextColor;
    private int mUncheckedDisabledTextColor;
    private int mCheckedChipIconTint;
    private int mUncheckedChipIconTint;
    private int mCheckedDisabledChipIconTint;
    private int mUncheckedDisabledChipIconTint;
    private int mCurrentBackgroundColor;
    private int mCurrentTextColor;
    private int mCurrentIconTint;
    private int mAlpha = 255;

    private float mChipMinHeight;
    private float mChipCornerRadius;
    private float mChipStartPadding;
    private float mChipEndPadding;
    private float mChipIconStartPadding;
    private float mChipIconEndPadding;
    private float mChipIconSize;
    private float mTextStartPadding;
    private float mTextEndPadding;
    private float mCloseIconStartPadding;
    private float mCloseIconEndPadding;
    private float mCloseIconSize;
    private float mCloseIconTouchBoundsSize;
    private float mCloseIconScale = 1.0f;
    private float mTextTranslationX;
    private float mTextTranslationY;
    private int mTextMaxWidth = Integer.MAX_VALUE;
    private int mMinWidth;
    private int mMaxWidth = Integer.MAX_VALUE;

    private final class TintAnimation extends ChipAnimation {
        private int mStartBackground;
        private int mEndBackground;
        private int mStartText;
        private int mEndText;
        private int mStartIcon;
        private int mEndIcon;

        TintAnimation() {
            super("TintAnimation");
        }

        void start(int background, int text, int icon) {
            mStartBackground = mCurrentBackgroundColor;
            mEndBackground = background;
            mStartText = mCurrentTextColor;
            mEndText = text;
            mStartIcon = mCurrentIconTint;
            mEndIcon = icon;
            mCurrentProgress = 0;
            mAnimation.animateToFinalPosition(ANIMATION_RANGE);
        }

        @Override
        void onProgressChanged(float fraction) {
            mCurrentBackgroundColor =
                    (Integer) ARGB_EVALUATOR.evaluate(fraction, mStartBackground, mEndBackground);
            mCurrentTextColor =
                    (Integer) ARGB_EVALUATOR.evaluate(fraction, mStartText, mEndText);
            mCurrentIconTint =
                    (Integer) ARGB_EVALUATOR.evaluate(fraction, mStartIcon, mEndIcon);
            onChipTextColorChange(mCurrentTextColor);
            invalidateSelf();
        }
    }

    private final TintAnimation mTintAnimation = new TintAnimation();

    private final ChipAnimation mCloseIconAnimation = new ChipAnimation("CloseIconAnimation") {
        @Override
        void onProgressChanged(float fraction) {
            mCloseIconScale = 0.3f + (0.7f * fraction);
            invalidateSelf();
        }
    };

    private COUIChipDrawable(
            @NonNull Context context, @Nullable AttributeSet attrs,
            @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        mContext = context;
        mMaskEffectDrawable = new COUIMaskEffectDrawable(
                context, COUIMaskEffectDrawable.MASK_EFFECT_TYPE_WIDGET_WITH_BACKGROUND);
        mCloseIconEffectDrawable = new COUIMaskEffectDrawable(
                context, COUIMaskEffectDrawable.MASK_EFFECT_TYPE_WIDGET_WITH_BACKGROUND);
        mStrokeDrawable = new COUIStrokeDrawable(context);
        mMaskEffectDrawable.setStatefulDrawableListener(this);
        mCloseIconEffectDrawable.setStatefulDrawableListener(this);
        mStrokeDrawable.setStatefulDrawableListener(this);
        loadFromAttributes(attrs, defStyleAttr, defStyleRes);
        setState(DEFAULT_STATE);
    }

    public static COUIChipDrawable createFromAttributes(
            @NonNull Context context, @Nullable AttributeSet attrs,
            @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        return new COUIChipDrawable(context, attrs, defStyleAttr, defStyleRes);
    }

    public static COUIChipDrawable createFromResource(
            @NonNull Context context, @XmlRes int resourceId) {
        try (XmlResourceParser parser = context.getResources().getXml(resourceId)) {
            int type;
            do {
                type = parser.next();
            } while (type != XmlPullParser.START_TAG && type != XmlPullParser.END_DOCUMENT);
            if (type != XmlPullParser.START_TAG || !"chip".equals(parser.getName())) {
                throw new XmlPullParserException("Must have a <chip> start tag");
            }
            AttributeSet attrs = Xml.asAttributeSet(parser);
            int style = attrs.getStyleAttribute();
            if (style == 0) {
                style = R.style.Widget_COUI_Chip_Suggestion;
            }
            return createFromAttributes(context, attrs, R.attr.couiChipStyle, style);
        } catch (Exception exception) {
            Resources.NotFoundException notFound = new Resources.NotFoundException(
                    "Can't load chip resource ID #0x" + Integer.toHexString(resourceId));
            notFound.initCause(exception);
            throw notFound;
        }
    }

    private void loadFromAttributes(
            @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        TypedArray a = mContext.obtainStyledAttributes(
                attrs, R.styleable.COUIChip, defStyleAttr, defStyleRes);
        mCheckable = a.getBoolean(R.styleable.COUIChip_android_checkable, false);
        mChecked = a.getBoolean(R.styleable.COUIChip_android_checked, false);
        mUncheckedBackgroundColor = a.getColor(
                R.styleable.COUIChip_uncheckedBackgroundColor, 0);
        mCheckedBackgroundColor = a.getColor(
                R.styleable.COUIChip_checkedBackgroundColor, 0);
        mUncheckedDisabledBackgroundColor = a.getColor(
                R.styleable.COUIChip_uncheckedDisabledBackgroundColor, 0);
        mCheckedDisabledBackgroundColor = a.getColor(
                R.styleable.COUIChip_checkedDisabledBackgroundColor, 0);
        mChipIconApplyTint = a.getBoolean(
                R.styleable.COUIChip_chipIconApplyTint, true);
        mUncheckedChipIconTint = a.getColor(
                R.styleable.COUIChip_uncheckedChipIconTint, 0);
        mCheckedChipIconTint = a.getColor(
                R.styleable.COUIChip_checkedChipIconTint, 0);
        mUncheckedDisabledChipIconTint = a.getColor(
                R.styleable.COUIChip_uncheckedDisabledChipIconTint, 0);
        mCheckedDisabledChipIconTint = a.getColor(
                R.styleable.COUIChip_checkedDisabledChipIconTint, 0);
        mUncheckedTextColor = a.getColor(
                R.styleable.COUIChip_uncheckedTextColor, 0);
        mCheckedTextColor = a.getColor(
                R.styleable.COUIChip_checkedTextColor, 0);
        mUncheckedDisabledTextColor = a.getColor(
                R.styleable.COUIChip_uncheckedDisabledTextColor, 0);
        mCheckedDisabledTextColor = a.getColor(
                R.styleable.COUIChip_checkedDisabledTextColor, 0);
        mTextMaxWidth = a.getDimensionPixelSize(
                R.styleable.COUIChip_chipTextMaxWidth, Integer.MAX_VALUE);
        mChipMinHeight = a.getDimension(R.styleable.COUIChip_android_minHeight, 0);
        mMinWidth = a.getDimensionPixelSize(R.styleable.COUIChip_android_minWidth, 0);
        mMaxWidth = a.getDimensionPixelSize(
                R.styleable.COUIChip_android_maxWidth, Integer.MAX_VALUE);
        mChipStartPadding = a.getDimension(R.styleable.COUIChip_chipStartPadding, 0);
        mChipEndPadding = a.getDimension(R.styleable.COUIChip_chipEndPadding, 0);
        mChipCornerRadius = a.getDimension(R.styleable.COUIChip_chipCornerRadius, -1);
        mChipIcon = a.getDrawable(R.styleable.COUIChip_chipIcon);
        mChipIconVisible = a.getBoolean(R.styleable.COUIChip_chipIconVisible, false);
        mChipIconSize = a.getDimension(R.styleable.COUIChip_chipIconSize, -1);
        mChipIconStartPadding = a.getDimension(
                R.styleable.COUIChip_chipIconStartPadding, 0);
        mChipIconEndPadding = a.getDimension(
                R.styleable.COUIChip_chipIconEndPadding, 0);
        CharSequence text = a.getText(R.styleable.COUIChip_android_text);
        mText = text == null ? "" : text;
        mTextStartPadding = a.getDimension(R.styleable.COUIChip_textStartPadding, 0);
        mTextEndPadding = a.getDimension(R.styleable.COUIChip_textEndPadding, 0);
        mDefaultTextAppearance = MaterialResources.getTextAppearance(
                mContext, a, R.styleable.COUIChip_android_textAppearance);
        mCheckedTextAppearance = MaterialResources.getTextAppearance(
                mContext, a, R.styleable.COUIChip_checkedTextAppearance);
        if (a.hasValue(R.styleable.COUIChip_android_textSize)) {
            mTextPaint.setTextSize(a.getDimension(
                    R.styleable.COUIChip_android_textSize, mTextPaint.getTextSize()));
        } else if (mDefaultTextAppearance != null) {
            mTextPaint.setTextSize(mDefaultTextAppearance.getTextSize());
        }
        int ellipsize = a.getInt(R.styleable.COUIChip_android_ellipsize, 0);
        if (ellipsize == 1) {
            mTruncateAt = TextUtils.TruncateAt.START;
        } else if (ellipsize == 2) {
            mTruncateAt = TextUtils.TruncateAt.MIDDLE;
        } else if (ellipsize == 3) {
            mTruncateAt = TextUtils.TruncateAt.END;
        }
        mCloseIcon = a.getDrawable(R.styleable.COUIChip_closeIcon);
        mCloseIconVisible = a.getBoolean(R.styleable.COUIChip_closeIconVisible, false);
        mCloseIconSize = a.getDimension(R.styleable.COUIChip_closeIconSize, 0);
        mCloseIconStartPadding = a.getDimension(
                R.styleable.COUIChip_closeIconStartPadding, 0);
        mCloseIconEndPadding = a.getDimension(
                R.styleable.COUIChip_closeIconEndPadding, 0);
        mCloseIconTouchBoundsSize = a.getDimensionPixelSize(
                R.styleable.COUIChip_closeIconTouchBoundsSize, 0);
        a.recycle();
        applyChildDrawable(mChipIcon);
        applyChildDrawable(mCloseIcon);
        updateStateColors(false);
    }

    private void applyChildDrawable(@Nullable Drawable drawable) {
        if (drawable == null) {
            return;
        }
        drawable.setCallback(this);
        DrawableCompat.setLayoutDirection(drawable, getLayoutDirection());
        drawable.setLevel(getLevel());
        drawable.setVisible(isVisible(), false);
    }

    private void unapplyChildDrawable(@Nullable Drawable drawable) {
        if (drawable != null) {
            drawable.setCallback(null);
        }
    }

    private int resolveBackgroundColor(boolean enabled, boolean checked) {
        if (enabled) {
            return checked ? mCheckedBackgroundColor : mUncheckedBackgroundColor;
        }
        return checked ? mCheckedDisabledBackgroundColor : mUncheckedDisabledBackgroundColor;
    }

    private int resolveTextColor(boolean enabled, boolean checked) {
        if (enabled) {
            return checked ? mCheckedTextColor : mUncheckedTextColor;
        }
        return checked ? mCheckedDisabledTextColor : mUncheckedDisabledTextColor;
    }

    private int resolveIconTint(boolean enabled, boolean checked) {
        if (enabled) {
            return checked ? mCheckedChipIconTint : mUncheckedChipIconTint;
        }
        return checked ? mCheckedDisabledChipIconTint : mUncheckedDisabledChipIconTint;
    }

    private void updateStateColors(boolean animate) {
        boolean enabled = containsState(getState(), android.R.attr.state_enabled);
        boolean checked = mCheckable && containsState(getState(), android.R.attr.state_checked);
        int background = resolveBackgroundColor(enabled, checked);
        int text = resolveTextColor(enabled, checked);
        int icon = resolveIconTint(enabled, checked);
        mChecked = checked;
        if (animate && mCurrentBackgroundColor != 0) {
            mTintAnimation.mAnimation.cancel();
            mTintAnimation.start(background, text, icon);
        } else {
            mCurrentBackgroundColor = background;
            mCurrentTextColor = text;
            mCurrentIconTint = icon;
            onChipTextColorChange(text);
            invalidateSelf();
        }
    }

    private static boolean containsState(int[] states, int state) {
        for (int value : states) {
            if (value == state) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Rect bounds = getBounds();
        if (bounds.isEmpty() || mAlpha == 0) {
            return;
        }
        int save = canvas.saveLayerAlpha(
                bounds.left, bounds.top, bounds.right, bounds.bottom, mAlpha);
        mTmpBounds.set(bounds);
        float radius = mChipCornerRadius < 0 ? bounds.height() / 2.0f : mChipCornerRadius;

        if (mChipBackground != null) {
            mChipBackground.setBounds(bounds);
            mChipBackground.draw(canvas);
        } else {
            mBackgroundPaint.setColor(mCurrentBackgroundColor);
            mBackgroundPath.reset();
            new OplusPathAdapter(mBackgroundPath, OplusPathAdapter.NEW_PATH_SMOOTH)
                    .addSmoothRoundRect(mTmpBounds, radius, radius, Path.Direction.CW);
            canvas.drawPath(mBackgroundPath, mBackgroundPaint);
        }
        // OPPO draws the container state layer above the surface but below all chip content.
        mMaskEffectDrawable.setBounds(bounds);
        mMaskEffectDrawable.setMaskRect(mTmpBounds, radius, radius);
        mMaskEffectDrawable.draw(canvas);
        mStrokeDrawable.setBounds(bounds);
        mStrokeDrawable.setStrokeRect(mTmpBounds, radius, radius);
        mStrokeDrawable.draw(canvas);
        drawChipIcon(canvas, bounds);
        if (mShouldDrawText) {
            drawText(canvas, bounds);
        }
        drawCloseIcon(canvas, bounds);
        if (mShowRedDot) {
            mBackgroundPaint.setColor(0xffff3b30);
            float dotRadius = Math.max(2, bounds.height() / 12.0f);
            float x = isRTL() ? bounds.left : bounds.right;
            canvas.drawCircle(x, bounds.top, dotRadius, mBackgroundPaint);
        }
        canvas.restoreToCount(save);
    }

    private void drawChipIcon(Canvas canvas, Rect bounds) {
        if (!showsChipIcon()) {
            return;
        }
        float width = getDrawableWidth(mChipIcon, mChipIconSize);
        float height = getDrawableHeight(mChipIcon, mChipIconSize);
        float left = isRTL()
                ? bounds.right - mChipIconStartPadding - width
                : bounds.left + mChipIconStartPadding;
        float top = bounds.exactCenterY() - (height / 2);
        mChipIcon.setBounds(
                Math.round(left), Math.round(top), Math.round(left + width), Math.round(top + height));
        if (mChipIconApplyTint) {
            DrawableCompat.setTint(mChipIcon, mCurrentIconTint);
        }
        mChipIcon.draw(canvas);
    }

    private void drawCloseIcon(Canvas canvas, Rect bounds) {
        if (!showsCloseIcon()) {
            return;
        }
        float width = getDrawableWidth(mCloseIcon, mCloseIconSize);
        float height = getDrawableHeight(mCloseIcon, mCloseIconSize);
        float left = isRTL()
                ? bounds.left + mCloseIconEndPadding
                : bounds.right - mCloseIconEndPadding - width;
        float top = bounds.exactCenterY() - (height / 2);
        canvas.save();
        canvas.scale(mCloseIconScale, mCloseIconScale,
                left + width / 2, top + height / 2);
        mCloseIcon.setBounds(
                Math.round(left), Math.round(top), Math.round(left + width), Math.round(top + height));
        DrawableCompat.setTint(mCloseIcon, mCurrentIconTint);
        float touchSize = mCloseIconTouchBoundsSize > 0
                ? mCloseIconTouchBoundsSize : calculateCloseIconWidth();
        float centerX = left + (width / 2.0f);
        float centerY = top + (height / 2.0f);
        mCloseIconTouchRect.set(
                centerX - (touchSize / 2.0f), centerY - (touchSize / 2.0f),
                centerX + (touchSize / 2.0f), centerY + (touchSize / 2.0f));
        float effectRadius = Math.min(
                mCloseIconTouchRect.width(), mCloseIconTouchRect.height()) / 2.0f;
        mCloseIconEffectDrawable.setBounds(
                Math.round(mCloseIconTouchRect.left), Math.round(mCloseIconTouchRect.top),
                Math.round(mCloseIconTouchRect.right), Math.round(mCloseIconTouchRect.bottom));
        mCloseIconEffectDrawable.setMaskRect(
                mCloseIconTouchRect, effectRadius, effectRadius);
        mCloseIconEffectDrawable.draw(canvas);
        mCloseIcon.draw(canvas);
        canvas.restore();
    }

    private void drawText(Canvas canvas, Rect bounds) {
        float start = (showsChipIcon() ? calculateChipIconWidth() : mChipStartPadding)
                + mTextStartPadding;
        float end = (showsCloseIcon() ? calculateCloseIconWidth() : mChipEndPadding)
                + mTextEndPadding;
        float available = Math.max(0, bounds.width() - start - end);
        CharSequence display = TextUtils.ellipsize(
                mText, mTextPaint, Math.min(available, mTextMaxWidth), mTruncateAt);
        mTextPaint.setColor(mCurrentTextColor);
        mTextPaint.setAlpha(mAlpha);
        Paint.FontMetrics metrics = mTextPaint.getFontMetrics();
        float baseline = bounds.exactCenterY() - ((metrics.ascent + metrics.descent) / 2);
        float x = isRTL() ? bounds.right - start : bounds.left + start;
        mTextPaint.setTextAlign(isRTL() ? Paint.Align.RIGHT : Paint.Align.LEFT);
        canvas.drawText(display, 0, display.length(),
                x + mTextTranslationX, baseline + mTextTranslationY, mTextPaint);
    }

    private static float getDrawableWidth(Drawable drawable, float requestedSize) {
        if (requestedSize >= 0) {
            return requestedSize;
        }
        return Math.max(0, drawable.getIntrinsicWidth());
    }

    private static float getDrawableHeight(Drawable drawable, float requestedSize) {
        if (requestedSize >= 0) {
            return requestedSize;
        }
        return Math.max(0, drawable.getIntrinsicHeight());
    }

    public float calculateChipIconWidth() {
        if (!showsChipIcon()) {
            return 0;
        }
        return mChipIconStartPadding + getDrawableWidth(mChipIcon, mChipIconSize)
                + mChipIconEndPadding;
    }

    public float calculateCloseIconWidth() {
        if (!showsCloseIcon()) {
            return 0;
        }
        return mCloseIconStartPadding + getDrawableWidth(mCloseIcon, mCloseIconSize)
                + mCloseIconEndPadding;
    }

    public boolean isRTL() {
        return DrawableCompat.getLayoutDirection(this) == android.view.View.LAYOUT_DIRECTION_RTL;
    }

    public void getChipTouchBounds(@NonNull RectF outBounds) {
        outBounds.set(getBounds());
        if (showsCloseIcon()) {
            float closeWidth = calculateCloseIconWidth();
            if (isRTL()) {
                outBounds.left += closeWidth;
            } else {
                outBounds.right -= closeWidth;
            }
        }
    }

    public void getCloseIconTouchBounds(@NonNull RectF outBounds) {
        outBounds.setEmpty();
        if (!showsCloseIcon()) {
            return;
        }
        Rect bounds = getBounds();
        float touchWidth = calculateCloseIconWidth();
        if (isRTL()) {
            outBounds.set(bounds.left, bounds.top, bounds.left + touchWidth, bounds.bottom);
        } else {
            outBounds.set(bounds.right - touchWidth, bounds.top, bounds.right, bounds.bottom);
        }
    }

    public void getDeterminateChipSize(int[] outSize) {
        if (outSize == null || outSize.length < 2) {
            return;
        }
        outSize[0] = getIntrinsicWidth();
        outSize[1] = getIntrinsicHeight();
    }

    @Override
    public int getIntrinsicHeight() {
        return Math.round(mChipMinHeight);
    }

    @Override
    public int getIntrinsicWidth() {
        float textWidth = Math.min(mTextPaint.measureText(mText, 0, mText.length()), mTextMaxWidth);
        int width = Math.round((showsChipIcon() ? calculateChipIconWidth() : mChipStartPadding)
                + mTextStartPadding + textWidth + mTextEndPadding
                + (showsCloseIcon() ? calculateCloseIconWidth() : mChipEndPadding));
        return Math.min(mMaxWidth, Math.max(mMinWidth, width));
    }

    @Override
    public boolean isStateful() {
        return true;
    }

    @Override
    protected boolean onStateChange(int[] state) {
        boolean oldChecked = mChecked;
        updateStateColors(true);
        if (mChipIcon != null && mChipIcon.isStateful()) {
            mChipIcon.setState(state);
        }
        if (mCloseIcon != null && mCloseIcon.isStateful()) {
            mCloseIcon.setState(mCloseIconStateSet);
        }
        if (oldChecked != mChecked) {
            onSizeChange();
        }
        return true;
    }

    @Override
    public boolean onLayoutDirectionChanged(int layoutDirection) {
        boolean changed = false;
        if (mChipIcon != null) {
            changed |= DrawableCompat.setLayoutDirection(mChipIcon, layoutDirection);
        }
        if (mCloseIcon != null) {
            changed |= DrawableCompat.setLayoutDirection(mCloseIcon, layoutDirection);
        }
        invalidateSelf();
        return changed;
    }

    @Override
    protected boolean onLevelChange(int level) {
        boolean changed = false;
        if (mChipIcon != null) {
            changed |= mChipIcon.setLevel(level);
        }
        if (mCloseIcon != null) {
            changed |= mCloseIcon.setLevel(level);
        }
        return changed;
    }

    @Override
    public void invalidateDrawable(@NonNull Drawable who) {
        invalidateSelf();
    }

    @Override
    public void scheduleDrawable(@NonNull Drawable who, @NonNull Runnable what, long when) {
        scheduleSelf(what, when);
    }

    @Override
    public void unscheduleDrawable(@NonNull Drawable who, @NonNull Runnable what) {
        unscheduleSelf(what);
    }

    @Override
    public void onDrawableUpdate() {
        invalidateSelf();
    }

    @Override
    public void setAlpha(int alpha) {
        if (mAlpha != alpha) {
            mAlpha = alpha;
            invalidateSelf();
        }
    }

    @Override
    public int getAlpha() {
        return mAlpha;
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        mBackgroundPaint.setColorFilter(colorFilter);
        invalidateSelf();
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    public void setDelegate(@Nullable COUIChipDrawableDelegate delegate) {
        mDelegate = new WeakReference<>(delegate);
    }

    private void onSizeChange() {
        COUIChipDrawableDelegate delegate = mDelegate.get();
        if (delegate != null) {
            delegate.onChipDrawableSizeChange();
        }
        invalidateSelf();
    }

    public void onChipTextColorChange(int color) {
        COUIChipDrawableDelegate delegate = mDelegate.get();
        if (delegate != null) {
            delegate.onChipTextColorChange(color);
        }
    }

    public void onChipTextOffsetChanged(float x, float y) {
        mTextTranslationX = x;
        mTextTranslationY = y;
        COUIChipDrawableDelegate delegate = mDelegate.get();
        if (delegate != null) {
            delegate.onChipTextOffsetChanged(x, y);
        }
        invalidateSelf();
    }

    public boolean isCheckable() { return mCheckable; }
    public void setCheckable(boolean value) { mCheckable = value; invalidateSelf(); }
    public boolean isChipIconVisible() { return mChipIconVisible; }
    public boolean isCloseIconVisible() { return mCloseIconVisible; }
    public boolean isShowRedDot() { return mShowRedDot; }
    public boolean showsChipIcon() { return mChipIconVisible && mChipIcon != null; }
    public boolean showsCloseIcon() { return mCloseIconVisible && mCloseIcon != null; }
    public Drawable getChipBackground() { return mChipBackground; }
    public Drawable getChipIcon() { return mChipIcon; }
    public Drawable getCloseIcon() { return mCloseIcon; }
    public CharSequence getCloseIconContentDescription() { return mCloseIconContentDescription; }
    public float getChipCornerRadius() { return mChipCornerRadius; }
    public float getChipEndPadding() { return mChipEndPadding; }
    public float getChipIconEndPadding() { return mChipIconEndPadding; }
    public float getChipIconSize() { return mChipIconSize; }
    public float getChipIconStartPadding() { return mChipIconStartPadding; }
    public float getChipMinHeight() { return mChipMinHeight; }
    public float getChipStartPadding() { return mChipStartPadding; }
    public float getCloseIconEndPadding() { return mCloseIconEndPadding; }
    public float getCloseIconSize() { return mCloseIconSize; }
    public float getCloseIconStartPadding() { return mCloseIconStartPadding; }
    public int[] getCloseIconState() { return mCloseIconStateSet; }
    public TextUtils.TruncateAt getEllipsize() { return mTruncateAt; }
    public CharSequence getText() { return mText; }
    public TextAppearance getTextAppearance() { return mDefaultTextAppearance; }
    public TextAppearance getCheckedTextAppearance() { return mCheckedTextAppearance; }
    public float getTextEndPadding() { return mTextEndPadding; }
    public int getTextMaxWidth() { return mTextMaxWidth; }
    public float getTextSize() { return mTextPaint.getTextSize(); }
    public float getTextStartPadding() { return mTextStartPadding; }
    public int getCheckedBackgroundColor() { return mCheckedBackgroundColor; }
    public int getUncheckedBackgroundColor() { return mUncheckedBackgroundColor; }
    public int getCheckedDisabledBackgroundColor() { return mCheckedDisabledBackgroundColor; }
    public int getUncheckedDisabledBackgroundColor() {
        return mUncheckedDisabledBackgroundColor;
    }
    public int getCheckedTextColor() { return mCheckedTextColor; }
    public int getUncheckedTextColor() { return mUncheckedTextColor; }
    public int getCheckedDisabledTextColor() { return mCheckedDisabledTextColor; }
    public int getUncheckedDisabledTextColor() { return mUncheckedDisabledTextColor; }
    public int getCheckedChipIconTint() { return mCheckedChipIconTint; }
    public int getUncheckedChipIconTint() { return mUncheckedChipIconTint; }
    public int getCheckedDisabledChipIconTint() { return mCheckedDisabledChipIconTint; }
    public int getUncheckedDisabledChipIconTint() {
        return mUncheckedDisabledChipIconTint;
    }
    public int getMaxWidth() { return mMaxWidth; }
    public boolean shouldDrawText() { return mShouldDrawText; }

    public void setChipBackground(@Nullable Drawable drawable) {
        mChipBackground = drawable;
        invalidateSelf();
    }

    public void setChipIcon(@Nullable Drawable drawable) {
        if (mChipIcon == drawable) return;
        unapplyChildDrawable(mChipIcon);
        mChipIcon = drawable;
        applyChildDrawable(drawable);
        onSizeChange();
    }

    public void setCloseIcon(@Nullable Drawable drawable) {
        if (mCloseIcon == drawable) return;
        unapplyChildDrawable(mCloseIcon);
        mCloseIcon = drawable;
        applyChildDrawable(drawable);
        onSizeChange();
    }

    public void setChipIconVisible(boolean value) {
        if (mChipIconVisible != value) {
            mChipIconVisible = value;
            onSizeChange();
        }
    }

    public void setCloseIconVisible(boolean value) {
        setCloseIconVisible(value, true);
    }

    public void setCloseIconVisible(boolean value, boolean animate) {
        if (mCloseIconVisible == value) return;
        mCloseIconVisible = value;
        if (animate) {
            mCloseIconAnimation.mAnimation.cancel();
            mCloseIconAnimation.mCurrentProgress = value ? 0 : ANIMATION_RANGE;
            mCloseIconAnimation.mAnimation.animateToFinalPosition(
                    value ? ANIMATION_RANGE : 0);
        } else {
            mCloseIconScale = value ? 1.0f : 0.3f;
        }
        onSizeChange();
    }

    public final boolean setCloseIconState(@NonNull int[] state) {
        if (Arrays.equals(mCloseIconStateSet, state)) return false;
        mCloseIconStateSet = state;
        return mCloseIcon != null && mCloseIcon.isStateful() && mCloseIcon.setState(state);
    }

    public void setCloseIconTouched(boolean touched) {
        mCloseIconTouched = touched;
        if (touched) {
            mCloseIconEffectDrawable.setTouchEntered();
        } else {
            mCloseIconEffectDrawable.setTouchExited();
        }
    }

    public void setTouched(boolean touched) {
        if (touched) {
            mMaskEffectDrawable.setTouchEntered();
        } else {
            mMaskEffectDrawable.setTouchExited();
        }
    }

    public void setShowRedDot(boolean value) { mShowRedDot = value; invalidateSelf(); }
    public void setShouldDrawText(boolean value) { mShouldDrawText = value; invalidateSelf(); }
    public void setEllipsize(@Nullable TextUtils.TruncateAt value) { mTruncateAt = value; }
    public void setMaxWidth(@Px int value) { mMaxWidth = value; onSizeChange(); }
    public void setTextMaxWidth(@Px int value) { mTextMaxWidth = value; onSizeChange(); }
    public void setChipCornerRadius(float value) { mChipCornerRadius = value; invalidateSelf(); }
    public void setChipEndPadding(float value) { mChipEndPadding = value; onSizeChange(); }
    public void setChipIconEndPadding(float value) { mChipIconEndPadding = value; onSizeChange(); }
    public void setChipIconSize(float value) { mChipIconSize = value; onSizeChange(); }
    public void setChipIconStartPadding(float value) { mChipIconStartPadding = value; onSizeChange(); }
    public void setChipMinHeight(float value) { mChipMinHeight = value; onSizeChange(); }
    public void setChipStartPadding(float value) { mChipStartPadding = value; onSizeChange(); }
    public void setCloseIconEndPadding(float value) { mCloseIconEndPadding = value; onSizeChange(); }
    public void setCloseIconSize(float value) { mCloseIconSize = value; onSizeChange(); }
    public void setCloseIconStartPadding(float value) { mCloseIconStartPadding = value; onSizeChange(); }
    public void setTextEndPadding(float value) { mTextEndPadding = value; onSizeChange(); }
    public void setTextStartPadding(float value) { mTextStartPadding = value; onSizeChange(); }

    public void setText(@Nullable CharSequence value) {
        CharSequence text = value == null ? "" : value;
        if (!TextUtils.equals(mText, text)) {
            mText = text;
            onSizeChange();
        }
    }

    public void setTextSize(@Dimension float value) {
        if (mTextPaint.getTextSize() != value) {
            mTextPaint.setTextSize(value);
            onSizeChange();
        }
    }

    public void setTextAppearance(@Nullable TextAppearance value) {
        mDefaultTextAppearance = value;
        invalidateSelf();
    }

    public void setCheckedTextAppearance(@Nullable TextAppearance value) {
        mCheckedTextAppearance = value;
        invalidateSelf();
    }

    public void setCloseIconContentDescription(@Nullable CharSequence value) {
        mCloseIconContentDescription =
                value == null ? null : BidiFormatter.getInstance().unicodeWrap(value);
    }

    public void setCheckedBackgroundColor(@ColorInt int value) {
        mCheckedBackgroundColor = value; updateStateColors(false);
    }
    public void setUncheckedBackgroundColor(@ColorInt int value) {
        mUncheckedBackgroundColor = value; updateStateColors(false);
    }
    public void setCheckedDisabledBackgroundColor(@ColorInt int value) {
        mCheckedDisabledBackgroundColor = value; updateStateColors(false);
    }
    public void setUncheckedDisabledBackgroundColor(@ColorInt int value) {
        mUncheckedDisabledBackgroundColor = value; updateStateColors(false);
    }
    public void setCheckedTextColor(@ColorInt int value) {
        mCheckedTextColor = value; updateStateColors(false);
    }
    public void setUncheckedTextColor(@ColorInt int value) {
        mUncheckedTextColor = value; updateStateColors(false);
    }
    public void setCheckedDisabledTextColor(@ColorInt int value) {
        mCheckedDisabledTextColor = value; updateStateColors(false);
    }
    public void setUncheckedDisabledTextColor(@ColorInt int value) {
        mUncheckedDisabledTextColor = value; updateStateColors(false);
    }
    public void setCheckedChipIconTint(@ColorInt int value) {
        mCheckedChipIconTint = value; updateStateColors(false);
    }
    public void setUncheckedChipIconTint(@ColorInt int value) {
        mUncheckedChipIconTint = value; updateStateColors(false);
    }
    public void setCheckedDisabledChipIconTint(@ColorInt int value) {
        mCheckedDisabledChipIconTint = value; updateStateColors(false);
    }
    public void setUncheckedDisabledChipIconTint(@ColorInt int value) {
        mUncheckedDisabledChipIconTint = value; updateStateColors(false);
    }

    public void refresh(Context context) {
        mMaskEffectDrawable.refresh(context);
        mCloseIconEffectDrawable.refresh(context);
        mStrokeDrawable.refresh(context);
        updateStateColors(false);
    }
}
