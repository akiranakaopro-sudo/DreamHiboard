package com.coui.appcompat.chip;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.widget.TextView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.coui.appcompat.R;
import com.coui.appcompat.state.COUIMaskEffectDrawable;

/**
 * COUI pill-shaped chip.
 *
 * <p>The attributes and state model follow the decoded OPPO COUI chip module. The rendering is
 * hosted directly in this unified library instead of depending on the APK's split support R
 * packages.</p>
 */
public class COUIChip extends AppCompatCheckBox implements COUICheckable<COUIChip> {
    private static final int[] CHECKED_STATE = {android.R.attr.state_checked};

    private COUICheckable.OnCheckedChangeListener<COUIChip> mInternalListener;
    private OnClickListener mCloseIconClickListener;
    private Drawable mChipIcon;
    private Drawable mCloseIcon;
    private boolean mChipIconVisible;
    private boolean mCloseIconVisible;
    private boolean mCheckable;
    private boolean mBroadcasting;
    private boolean mApplyIconTint;
    private float mCornerRadius;
    private int mChipStartPadding;
    private int mChipEndPadding;
    private int mChipIconStartPadding;
    private int mChipIconEndPadding;
    private int mChipIconSize;
    private int mCloseIconStartPadding;
    private int mCloseIconEndPadding;
    private int mCloseIconSize;
    private int mCloseIconTouchBoundsSize;
    private int mCheckedBackgroundColor;
    private int mUncheckedBackgroundColor;
    private int mCheckedDisabledBackgroundColor;
    private int mUncheckedDisabledBackgroundColor;
    private int mCheckedTextColor;
    private int mUncheckedTextColor;
    private int mCheckedDisabledTextColor;
    private int mUncheckedDisabledTextColor;
    private int mCheckedIconTint;
    private int mUncheckedIconTint;
    private int mCheckedDisabledIconTint;
    private int mUncheckedDisabledIconTint;
    private final COUIMaskEffectDrawable mMaskEffectDrawable;
    private final COUIChipDrawable mChipDrawable;

    public COUIChip(@NonNull Context context) {
        this(context, null);
    }

    public COUIChip(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.couiChipStyle);
    }

    public COUIChip(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Widget_COUI_Chip);
    }

    public COUIChip(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr);
        mMaskEffectDrawable = new COUIMaskEffectDrawable(
                context, COUIMaskEffectDrawable.MASK_EFFECT_TYPE_WIDGET_WITH_BACKGROUND);
        mChipDrawable = COUIChipDrawable.createFromAttributes(
                context, attrs, defStyleAttr, defStyleRes);
        // COUIChip's TextView owns text layout; the shared drawable owns the chip surface and
        // icons, exactly as in the decoded implementation.
        mChipDrawable.setShouldDrawText(false);
        mChipDrawable.setCallback(this);
        mChipDrawable.setDelegate(new COUIChipDrawable.COUIChipDrawableDelegate() {
            @Override
            public void onChipDrawableSizeChange() {
                requestLayout();
                invalidate();
            }

            @Override
            public void onChipTextColorChange(int color) {
                setTextColor(color);
            }

            @Override
            public void onChipTextFontChanged(android.graphics.Typeface typeface) {
                setTypeface(typeface);
            }

            @Override
            public void onChipTextOffsetChanged(float x, float y) {
                invalidate();
            }
        });
        super.setBackground(mChipDrawable);
        setForeground(null);
        setButtonDrawable((Drawable) null);
        setGravity(Gravity.CENTER);
        setEllipsize(TextUtils.TruncateAt.END);
        setSingleLine(true);
        setIncludeFontPadding(false);

        TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.COUIChip, defStyleAttr, defStyleRes);
        mCheckable = a.getBoolean(R.styleable.COUIChip_android_checkable, false);
        mCornerRadius = a.getDimension(R.styleable.COUIChip_chipCornerRadius, -1f);
        mChipStartPadding = a.getDimensionPixelSize(R.styleable.COUIChip_chipStartPadding, 0);
        mChipEndPadding = a.getDimensionPixelSize(R.styleable.COUIChip_chipEndPadding, 0);
        mChipIconStartPadding =
                a.getDimensionPixelSize(R.styleable.COUIChip_chipIconStartPadding, 0);
        mChipIconEndPadding =
                a.getDimensionPixelSize(R.styleable.COUIChip_chipIconEndPadding, 0);
        mChipIconSize = a.getDimensionPixelSize(R.styleable.COUIChip_chipIconSize, 0);
        mCloseIconStartPadding =
                a.getDimensionPixelSize(R.styleable.COUIChip_closeIconStartPadding, 0);
        mCloseIconEndPadding =
                a.getDimensionPixelSize(R.styleable.COUIChip_closeIconEndPadding, 0);
        mCloseIconSize = a.getDimensionPixelSize(R.styleable.COUIChip_closeIconSize, 0);
        mCloseIconTouchBoundsSize =
                a.getDimensionPixelSize(R.styleable.COUIChip_closeIconTouchBoundsSize, 0);
        mChipIcon = a.getDrawable(R.styleable.COUIChip_chipIcon);
        mCloseIcon = a.getDrawable(R.styleable.COUIChip_closeIcon);
        mChipIconVisible = a.getBoolean(R.styleable.COUIChip_chipIconVisible, mChipIcon != null);
        // The decoded Suggestion style supplies a close icon resource but keeps it hidden until
        // a caller explicitly enables it. Search-history chips therefore remain text-only.
        mCloseIconVisible = a.getBoolean(R.styleable.COUIChip_closeIconVisible, false);
        mApplyIconTint = a.getBoolean(R.styleable.COUIChip_chipIconApplyTint, true);
        mCheckedBackgroundColor = a.getColor(
                R.styleable.COUIChip_checkedBackgroundColor, 0xff1a73e8);
        mUncheckedBackgroundColor = a.getColor(
                R.styleable.COUIChip_uncheckedBackgroundColor, 0xffe6e6e6);
        mCheckedDisabledBackgroundColor = a.getColor(
                R.styleable.COUIChip_checkedDisabledBackgroundColor, mCheckedBackgroundColor);
        mUncheckedDisabledBackgroundColor = a.getColor(
                R.styleable.COUIChip_uncheckedDisabledBackgroundColor, mUncheckedBackgroundColor);
        mCheckedTextColor = a.getColor(
                R.styleable.COUIChip_checkedTextColor, 0xffffffff);
        mUncheckedTextColor = a.getColor(
                R.styleable.COUIChip_uncheckedTextColor, 0xff000000);
        mCheckedDisabledTextColor = a.getColor(
                R.styleable.COUIChip_checkedDisabledTextColor, 0x8affffff);
        mUncheckedDisabledTextColor = a.getColor(
                R.styleable.COUIChip_uncheckedDisabledTextColor, 0x42000000);
        mCheckedIconTint = a.getColor(
                R.styleable.COUIChip_checkedChipIconTint, mCheckedTextColor);
        mUncheckedIconTint = a.getColor(
                R.styleable.COUIChip_uncheckedChipIconTint, mUncheckedTextColor);
        mCheckedDisabledIconTint = a.getColor(
                R.styleable.COUIChip_checkedDisabledChipIconTint, mCheckedDisabledTextColor);
        mUncheckedDisabledIconTint = a.getColor(
                R.styleable.COUIChip_uncheckedDisabledChipIconTint, mUncheckedDisabledTextColor);
        int maxTextWidth = a.getDimensionPixelSize(
                R.styleable.COUIChip_chipTextMaxWidth, Integer.MAX_VALUE);
        boolean initiallyChecked = a.getBoolean(R.styleable.COUIChip_android_checked, false);
        if (maxTextWidth != Integer.MAX_VALUE) {
            setMaxWidth(Math.min(getMaxWidth(), maxTextWidth
                    + mChipStartPadding + mChipEndPadding
                    + mChipIconStartPadding + mChipIconSize + mChipIconEndPadding
                    + mCloseIconStartPadding + mCloseIconSize + mCloseIconEndPadding));
        }
        a.recycle();

        if (mCheckable) {
            super.setChecked(initiallyChecked);
        }
        super.setOnCheckedChangeListener((button, checked) -> {
            updateVisualState();
            if (!mBroadcasting && mInternalListener != null) {
                mBroadcasting = true;
                mInternalListener.onCheckedChanged(this, checked);
                mBroadcasting = false;
            }
        });
        updateVisualState();
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        updateVisualState();
    }

    @Override
    public void toggle() {
        if (mCheckable) {
            super.toggle();
        }
    }

    @Override
    public void setChecked(boolean checked) {
        if (mCheckable || !checked) {
            super.setChecked(checked);
            if (mChipDrawable != null) {
                mChipDrawable.setState(getDrawableState());
            }
        }
    }

    public boolean isCheckable() {
        return mCheckable;
    }

    public void setCheckable(boolean checkable) {
        mCheckable = checkable;
        if (mChipDrawable != null) {
            mChipDrawable.setCheckable(checkable);
        }
        setClickable(isClickable() || checkable);
        refreshDrawableState();
    }

    @Override
    public void setInternalOnCheckedChangeListener(
            @Nullable COUICheckable.OnCheckedChangeListener<COUIChip> listener) {
        mInternalListener = listener;
    }

    public void setChipIcon(@Nullable Drawable icon) {
        mChipIcon = icon;
        mChipDrawable.setChipIcon(icon);
        updateVisualState();
    }

    public void setChipIconResource(@DrawableRes int resourceId) {
        setChipIcon(resourceId == 0 ? null : ContextCompat.getDrawable(getContext(), resourceId));
    }

    @Nullable
    public Drawable getChipIcon() {
        return mChipIcon;
    }

    public void setChipIconVisible(boolean visible) {
        mChipIconVisible = visible;
        mChipDrawable.setChipIconVisible(visible);
        updateVisualState();
    }

    public boolean isChipIconVisible() {
        return mChipIconVisible;
    }

    public void setCloseIcon(@Nullable Drawable icon) {
        mCloseIcon = icon;
        mChipDrawable.setCloseIcon(icon);
        updateVisualState();
    }

    public void setCloseIconResource(@DrawableRes int resourceId) {
        setCloseIcon(resourceId == 0 ? null : ContextCompat.getDrawable(getContext(), resourceId));
    }

    @Nullable
    public Drawable getCloseIcon() {
        return mCloseIcon;
    }

    public void setCloseIconVisible(boolean visible) {
        mCloseIconVisible = visible;
        mChipDrawable.setCloseIconVisible(visible);
        updateVisualState();
    }

    public boolean isCloseIconVisible() {
        return mCloseIconVisible && mCloseIcon != null;
    }

    public void setOnCloseIconClickListener(@Nullable OnClickListener listener) {
        mCloseIconClickListener = listener;
    }

    public boolean performCloseIconClick() {
        if (!isCloseIconVisible() || mCloseIconClickListener == null) {
            return false;
        }
        playSoundEffect(android.view.SoundEffectConstants.CLICK);
        mCloseIconClickListener.onClick(this);
        sendAccessibilityEvent(android.view.accessibility.AccessibilityEvent.TYPE_VIEW_CLICKED);
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean inCloseIcon = isCloseIconVisible()
                && isInCloseIconTouchBounds(event.getX(), event.getY());
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mChipDrawable.setTouched(true);
                mChipDrawable.setCloseIconTouched(inCloseIcon);
                break;
            case MotionEvent.ACTION_MOVE:
                mChipDrawable.setCloseIconTouched(inCloseIcon);
                break;
            case MotionEvent.ACTION_UP:
                mChipDrawable.setTouched(false);
                mChipDrawable.setCloseIconTouched(false);
                if (inCloseIcon) {
                    return performCloseIconClick() || super.onTouchEvent(event);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                mChipDrawable.setTouched(false);
                mChipDrawable.setCloseIconTouched(false);
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    public void setCheckedBackgroundColor(@ColorInt int color) {
        mCheckedBackgroundColor = color;
        mChipDrawable.setCheckedBackgroundColor(color);
        updateVisualState();
    }

    public void setUncheckedBackgroundColor(@ColorInt int color) {
        mUncheckedBackgroundColor = color;
        mChipDrawable.setUncheckedBackgroundColor(color);
        updateVisualState();
    }

    public void setCheckedChipIconTint(@ColorInt int color) {
        mCheckedIconTint = color;
        mChipDrawable.setCheckedChipIconTint(color);
        updateVisualState();
    }

    public void setUncheckedChipIconTint(@ColorInt int color) {
        mUncheckedIconTint = color;
        mChipDrawable.setUncheckedChipIconTint(color);
        updateVisualState();
    }

    public void setCheckedDisabledChipIconTint(@ColorInt int color) {
        mCheckedDisabledIconTint = color;
        mChipDrawable.setCheckedDisabledChipIconTint(color);
        updateVisualState();
    }

    public void setUncheckedDisabledChipIconTint(@ColorInt int color) {
        mUncheckedDisabledIconTint = color;
        mChipDrawable.setUncheckedDisabledChipIconTint(color);
        updateVisualState();
    }

    public void setChipCornerRadius(float radius) {
        mCornerRadius = radius;
        mChipDrawable.setChipCornerRadius(radius);
        updateVisualState();
    }

    public float getChipCornerRadius() {
        return mCornerRadius;
    }

    public void setChipIconSize(float size) {
        mChipIconSize = Math.round(size);
        mChipDrawable.setChipIconSize(size);
        updateVisualState();
    }

    public float getChipIconSize() {
        return mChipIconSize;
    }

    public float getChipStartPadding() {
        return mChipStartPadding;
    }

    public void setChipStartPadding(float padding) {
        mChipStartPadding = Math.round(padding);
        mChipDrawable.setChipStartPadding(padding);
        updateVisualState();
    }

    public float getChipEndPadding() {
        return mChipEndPadding;
    }

    public void setChipEndPadding(float padding) {
        mChipEndPadding = Math.round(padding);
        mChipDrawable.setChipEndPadding(padding);
        updateVisualState();
    }

    public float getChipIconStartPadding() {
        return mChipIconStartPadding;
    }

    public void setChipIconStartPadding(float padding) {
        mChipIconStartPadding = Math.round(padding);
        mChipDrawable.setChipIconStartPadding(padding);
        updateVisualState();
    }

    public float getChipIconEndPadding() {
        return mChipIconEndPadding;
    }

    public void setChipIconEndPadding(float padding) {
        mChipIconEndPadding = Math.round(padding);
        mChipDrawable.setChipIconEndPadding(padding);
        updateVisualState();
    }

    public float getCloseIconStartPadding() {
        return mCloseIconStartPadding;
    }

    public void setCloseIconStartPadding(float padding) {
        mCloseIconStartPadding = Math.round(padding);
        mChipDrawable.setCloseIconStartPadding(padding);
        updateVisualState();
    }

    public float getCloseIconEndPadding() {
        return mCloseIconEndPadding;
    }

    public void setCloseIconEndPadding(float padding) {
        mCloseIconEndPadding = Math.round(padding);
        mChipDrawable.setCloseIconEndPadding(padding);
        updateVisualState();
    }

    public float getCloseIconSize() {
        return mCloseIconSize;
    }

    public void setCloseIconSize(float size) {
        mCloseIconSize = Math.round(size);
        mChipDrawable.setCloseIconSize(size);
        updateVisualState();
    }

    public void setCloseIconContentDescription(@Nullable CharSequence description) {
        mChipDrawable.setCloseIconContentDescription(description);
    }

    @Nullable
    public CharSequence getCloseIconContentDescription() {
        return mChipDrawable.getCloseIconContentDescription();
    }

    public void setCheckedDisabledBackgroundColor(@ColorInt int color) {
        mCheckedDisabledBackgroundColor = color;
        mChipDrawable.setCheckedDisabledBackgroundColor(color);
        updateVisualState();
    }

    public void setUncheckedDisabledBackgroundColor(@ColorInt int color) {
        mUncheckedDisabledBackgroundColor = color;
        mChipDrawable.setUncheckedDisabledBackgroundColor(color);
        updateVisualState();
    }

    public void setCheckedTextColor(@ColorInt int color) {
        mCheckedTextColor = color;
        mChipDrawable.setCheckedTextColor(color);
        updateVisualState();
    }

    public void setUncheckedTextColor(@ColorInt int color) {
        mUncheckedTextColor = color;
        mChipDrawable.setUncheckedTextColor(color);
        updateVisualState();
    }

    public void setCheckedDisabledTextColor(@ColorInt int color) {
        mCheckedDisabledTextColor = color;
        mChipDrawable.setCheckedDisabledTextColor(color);
        updateVisualState();
    }

    public void setUncheckedDisabledTextColor(@ColorInt int color) {
        mUncheckedDisabledTextColor = color;
        mChipDrawable.setUncheckedDisabledTextColor(color);
        updateVisualState();
    }

    public int getCheckedBackgroundColor() {
        return mCheckedBackgroundColor;
    }

    public int getUncheckedBackgroundColor() {
        return mUncheckedBackgroundColor;
    }

    public int getCheckedDisabledBackgroundColor() {
        return mCheckedDisabledBackgroundColor;
    }

    public int getUncheckedDisabledBackgroundColor() {
        return mUncheckedDisabledBackgroundColor;
    }

    public int getCheckedTextColor() {
        return mCheckedTextColor;
    }

    public int getUncheckedTextColor() {
        return mUncheckedTextColor;
    }

    public int getCheckedDisabledTextColor() {
        return mCheckedDisabledTextColor;
    }

    public int getUncheckedDisabledTextColor() {
        return mUncheckedDisabledTextColor;
    }

    public void setShowRedDot(boolean show) {
        mChipDrawable.setShowRedDot(show);
    }

    public boolean isShowRedDot() {
        return mChipDrawable.isShowRedDot();
    }

    @Override
    public void setText(CharSequence text, TextView.BufferType type) {
        super.setText(text, type);
        if (mChipDrawable != null) {
            mChipDrawable.setText(text);
        }
    }

    @Override
    public int[] onCreateDrawableState(int extraSpace) {
        int[] state = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            mergeDrawableStates(state, CHECKED_STATE);
        }
        return state;
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (mChipDrawable != null) {
            mChipDrawable.setState(getDrawableState());
        }
        updateVisualState();
    }

    @NonNull
    public COUIChipDrawable getChipDrawable() {
        return mChipDrawable;
    }

    private boolean isInCloseIconTouchBounds(float x, float y) {
        int touchWidth = Math.max(mCloseIconTouchBoundsSize,
                mCloseIconSize + mCloseIconStartPadding + mCloseIconEndPadding);
        boolean rtl = getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
        return y >= 0 && y <= getHeight()
                && (rtl ? x <= touchWidth : x >= getWidth() - touchWidth);
    }

    private void updateVisualState() {
        if (mChipDrawable == null
                || (mCheckedTextColor == 0 && mUncheckedTextColor == 0)) {
            return;
        }
        boolean checked = isChecked();
        boolean enabled = isEnabled();
        int backgroundColor = enabled
                ? (checked ? mCheckedBackgroundColor : mUncheckedBackgroundColor)
                : (checked ? mCheckedDisabledBackgroundColor : mUncheckedDisabledBackgroundColor);
        int textColor = enabled
                ? (checked ? mCheckedTextColor : mUncheckedTextColor)
                : (checked ? mCheckedDisabledTextColor : mUncheckedDisabledTextColor);
        int iconTint = enabled
                ? (checked ? mCheckedIconTint : mUncheckedIconTint)
                : (checked ? mCheckedDisabledIconTint : mUncheckedDisabledIconTint);

        mChipDrawable.setCheckedBackgroundColor(mCheckedBackgroundColor);
        mChipDrawable.setUncheckedBackgroundColor(mUncheckedBackgroundColor);
        mChipDrawable.setCheckedDisabledBackgroundColor(mCheckedDisabledBackgroundColor);
        mChipDrawable.setUncheckedDisabledBackgroundColor(mUncheckedDisabledBackgroundColor);
        mChipDrawable.setCheckedTextColor(mCheckedTextColor);
        mChipDrawable.setUncheckedTextColor(mUncheckedTextColor);
        mChipDrawable.setCheckedDisabledTextColor(mCheckedDisabledTextColor);
        mChipDrawable.setUncheckedDisabledTextColor(mUncheckedDisabledTextColor);
        mChipDrawable.setCheckedChipIconTint(mCheckedIconTint);
        mChipDrawable.setUncheckedChipIconTint(mUncheckedIconTint);
        mChipDrawable.setCheckedDisabledChipIconTint(mCheckedDisabledIconTint);
        mChipDrawable.setUncheckedDisabledChipIconTint(mUncheckedDisabledIconTint);
        mChipDrawable.setChipCornerRadius(mCornerRadius);
        mChipDrawable.setChipIcon(mChipIcon);
        mChipDrawable.setChipIconVisible(mChipIconVisible);
        mChipDrawable.setChipIconSize(mChipIconSize);
        mChipDrawable.setCloseIcon(mCloseIcon);
        mChipDrawable.setCloseIconVisible(mCloseIconVisible, false);
        mChipDrawable.setState(getDrawableState());
        if (getBackground() != mChipDrawable) {
            super.setBackground(mChipDrawable);
        }
        setTextColor(textColor);

        setCompoundDrawablesRelative(null, null, null, null);
        setCompoundDrawablePadding(0);
        setPaddingRelative(
                mChipIconVisible && mChipIcon != null
                        ? mChipIconStartPadding + mChipIconSize + mChipIconEndPadding
                        : mChipStartPadding,
                getPaddingTop(),
                isCloseIconVisible()
                        ? mCloseIconStartPadding + mCloseIconSize + mCloseIconEndPadding
                        : mChipEndPadding,
                getPaddingBottom());
    }

    @Nullable
    private Drawable prepareDrawable(@Nullable Drawable source, int size, @ColorInt int tint) {
        if (source == null) {
            return null;
        }
        Drawable drawable = DrawableCompat.wrap(source.mutate());
        if (mApplyIconTint) {
            DrawableCompat.setTint(drawable, tint);
        }
        int width = size > 0 ? size : Math.max(0, drawable.getIntrinsicWidth());
        int height = size > 0 ? size : Math.max(0, drawable.getIntrinsicHeight());
        drawable.setBounds(new Rect(0, 0, width, height));
        return drawable;
    }
}
