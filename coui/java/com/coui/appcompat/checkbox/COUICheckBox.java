package com.coui.appcompat.checkbox;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewDebug;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Checkable;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.ViewCompat;

import com.coui.appcompat.R;
import com.coui.appcompat.accessibilityutil.COUIAccessibilityUtil;
import com.coui.appcompat.darkmode.COUIDarkModeUtil;
import com.coui.appcompat.state.COUIMaskRippleDrawable;
import com.coui.appcompat.state.COUIStateEffectDrawable;
import com.coui.appcompat.uiutil.COUIWorkHandler;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;

public class COUICheckBox extends AppCompatButton implements Checkable {
    public static final int SELECT_UNSPECIFIC = -1;
    public static final int SELECT_NONE = 0;
    public static final int SELECT_PART = 1;
    public static final int SELECT_ALL = 2;

    private static final long DEFAULT_LOAD_DRAWABLE_DELAY = 100;

    private static final int[] ALLSELECT_SET = {R.attr.coui_state_allSelect};
    private static final int[] PARTSELECT_SET = {R.attr.coui_state_partSelect};
    private static final Rect BUTTON_DRAWABLE_BOUNDS = new Rect();

    private AccessibilityManager mAccessibilityManager;
    private final AtomicBoolean mAsyncLock;
    private boolean mBroadcasting;
    private Drawable mButtonDrawable;
    private int mButtonResource;
    private int mDrawableTextMargin;
    private COUIMaskRippleDrawable mMaskRippleDrawable;
    private OnStateChangeListener mOnStateChangeListener;
    private int mPendingState;
    private int mState;
    private COUIStateEffectDrawable mStateEffectBackground;
    private int mStyle;

    public interface OnStateChangeListener {
        void onStateChanged(COUICheckBox checkBox, int state);
    }

    public static class LoadDrawableRunnable implements Runnable {
        private final AttributeSet mAttrs;
        private final int mDefStyle;
        private final WeakReference<COUICheckBox> mWeakCheckbox;

        public LoadDrawableRunnable(COUICheckBox checkBox, AttributeSet attrs, int defStyle) {
            mWeakCheckbox = new WeakReference<>(checkBox);
            mAttrs = attrs;
            mDefStyle = defStyle;
        }

        private void configAnimatedVectorDrawableAndSetState(final COUICheckBox checkBox,
                final Drawable drawable) {
            if (Looper.getMainLooper() == Looper.myLooper()) {
                checkBox.configAnimatedVectorDrawableAndSetState(drawable);
            } else {
                checkBox.postOnAnimation(() ->
                        checkBox.configAnimatedVectorDrawableAndSetState(drawable));
            }
        }

        @Override
        public void run() {
            COUICheckBox checkBox = mWeakCheckbox.get();
            if (checkBox != null && checkBox.requestLock()) {
                TypedArray a = checkBox.getContext().obtainStyledAttributes(
                        mAttrs, R.styleable.COUICheckBox, mDefStyle, 0);
                Drawable drawable = a.getDrawable(R.styleable.COUICheckBox_couiButton);
                if (drawable != null) {
                    configAnimatedVectorDrawableAndSetState(checkBox, drawable);
                }
                a.recycle();
            }
        }
    }

    public static class SavedState extends View.BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };

        int mState;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            Object value = in.readValue(Integer.class.getClassLoader());
            mState = value instanceof Integer ? (Integer) value : SELECT_NONE;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeValue(mState);
        }

        @Override
        public String toString() {
            return "CompoundButton.SavedState{"
                    + Integer.toHexString(System.identityHashCode(this))
                    + " state=" + mState + "}";
        }
    }

    public COUICheckBox(Context context) {
        this(context, null);
    }

    public COUICheckBox(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.couiCheckBoxStyle);
    }

    public COUICheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mAsyncLock = new AtomicBoolean(false);
        mStyle = attrs == null || attrs.getStyleAttribute() == 0
                ? defStyleAttr
                : attrs.getStyleAttribute();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.COUICheckBox, defStyleAttr, 0);
        boolean asyncLoad = a.getBoolean(R.styleable.COUICheckBox_couiAsyncLoad, false);
        int buttonAttr = R.styleable.COUICheckBox_couiButton;
        int resourceId = a.getResourceId(buttonAttr, -1);
        int state = a.getInteger(R.styleable.COUICheckBox_couiCheckBoxState, SELECT_NONE);
        mPendingState = state;
        if (asyncLoad
                && resourceId == R.drawable.coui_checkbox_state
                && Looper.getMainLooper() == Looper.myLooper()) {
            asyncLoadAnimatedVectorDrawable(new LoadDrawableRunnable(this, attrs, defStyleAttr));
        } else {
            Drawable drawable = a.getDrawable(buttonAttr);
            if (drawable != null) {
                setButtonDrawable(drawable);
                mPendingState = SELECT_UNSPECIFIC;
                setState(state);
            }
        }
        a.recycle();
        configStateEffectBackground();
        mDrawableTextMargin = getResources().getDimensionPixelSize(
                R.dimen.coui_checkbox_margin_between_text_drawable);
    }

    private void asyncLoadAnimatedVectorDrawable(Runnable runnable) {
        COUIWorkHandler.getInstance().start(runnable);
        postDelayed(runnable, DEFAULT_LOAD_DRAWABLE_DELAY);
        configDefaultDrawable(mPendingState);
    }

    private void checkDrawableEnableState() {
        if (mMaskRippleDrawable != null) {
            mMaskRippleDrawable.setDrawableEnabled(isFocusable() || isClickable());
        }
    }

    private void configStateDrawable() {
        if (mStateEffectBackground != null) {
            mStateEffectBackground.setBounds(BUTTON_DRAWABLE_BOUNDS);
        }
    }

    private void configAnimatedVectorDrawableAndSetState(Drawable drawable) {
        setButtonDrawable(drawable);
        jumpToCurrentState(drawable, mPendingState);
        int pendingState = mPendingState;
        mPendingState = SELECT_UNSPECIFIC;
        setState(pendingState);
    }

    private void configDefaultDrawable(int state) {
        int drawableRes;
        if (state == SELECT_NONE) {
            drawableRes = isEnabled()
                    ? R.drawable.coui_btn_check_off_normal
                    : R.drawable.coui_btn_check_off_disabled;
        } else if (state == SELECT_PART) {
            drawableRes = isEnabled()
                    ? R.drawable.coui_btn_part_check_on_normal
                    : R.drawable.coui_btn_part_check_on_disabled;
        } else if (state == SELECT_ALL) {
            drawableRes = isEnabled()
                    ? R.drawable.coui_btn_check_on_normal
                    : R.drawable.coui_btn_check_on_disabled;
        } else {
            drawableRes = 0;
        }
        if (drawableRes != 0) {
            setButtonDrawable(drawableRes);
        }
    }

    private void configStateEffectBackground() {
        mMaskRippleDrawable = new COUIMaskRippleDrawable(getContext());
        mMaskRippleDrawable.setCircleRippleMask(
                COUIMaskRippleDrawable.getMaskRippleRadiusByType(getContext(), 1));
        Drawable viewBackground = getBackground() == null ? new ColorDrawable(0) : getBackground();
        mStateEffectBackground = new COUIStateEffectDrawable(new Drawable[]{viewBackground, mMaskRippleDrawable});
        super.setBackground(mStateEffectBackground);
        COUIDarkModeUtil.setForceDarkAllow(this, false);
    }

    private CharSequence getButtonStateDescription() {
        if (mState == SELECT_ALL) {
            return getResources().getString(R.string.coui_accessibility_checked);
        }
        if (mState == SELECT_NONE) {
            return getResources().getString(R.string.coui_accessibility_unchecked);
        }
        return getResources().getString(R.string.coui_accessibility_partchecked);
    }

    private boolean isLayoutRtl() {
        return ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL;
    }

    private void jumpToCurrentState(Drawable drawable, int state) {
        if (state == SELECT_PART) {
            drawable.setState(PARTSELECT_SET);
        } else if (state == SELECT_ALL) {
            drawable.setState(ALLSELECT_SET);
        }
        drawable.jumpToCurrentState();
    }

    private boolean requestLock() {
        return mAsyncLock.compareAndSet(false, true);
    }

    private void updateButtonDrawableBounds() {
        if (mButtonDrawable == null) {
            return;
        }
        int gravity = getGravity() & 112;
        int drawableHeight = mButtonDrawable.getIntrinsicHeight();
        int drawableWidth = mButtonDrawable.getIntrinsicWidth();
        int top = gravity == 16 ? (getHeight() - drawableHeight) / 2
                : gravity == 80 ? getHeight() - drawableHeight : 0;
        if (isLayoutRtl()) {
            BUTTON_DRAWABLE_BOUNDS.set(getWidth() - drawableWidth - getPaddingRight(), top,
                    getWidth() - getPaddingRight(), top + drawableHeight);
        } else {
            BUTTON_DRAWABLE_BOUNDS.set(getPaddingLeft(), top,
                    getPaddingLeft() + drawableWidth, top + drawableHeight);
        }
    }

    private void updateStateDescription() {
        if (getImportantForAccessibility() == IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
            setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
        }
        if (mAccessibilityManager == null) {
            mAccessibilityManager = (AccessibilityManager) getContext()
                    .getSystemService(Context.ACCESSIBILITY_SERVICE);
        }
        if (mAccessibilityManager != null && mAccessibilityManager.isEnabled()) {
            AccessibilityEvent event = AccessibilityEvent.obtain();
            event.setEventType(AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED);
            event.setContentChangeTypes(AccessibilityEvent.CONTENT_CHANGE_TYPE_STATE_DESCRIPTION);
            sendAccessibilityEventUnchecked(event);
        }
    }

    @Override
    public boolean dispatchHoverEvent(MotionEvent event) {
        if (isEnabled() && event.getActionMasked() == MotionEvent.ACTION_HOVER_ENTER) {
            setHovered(true);
        }
        if (event.getActionMasked() == MotionEvent.ACTION_HOVER_EXIT && isHovered()) {
            setHovered(false);
        }
        return super.dispatchHoverEvent(event);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        updateButtonDrawableBounds();
        checkDrawableEnableState();
        configStateDrawable();
        super.draw(canvas);
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (mButtonDrawable != null) {
            mButtonDrawable.setState(getDrawableState());
            invalidate();
        }
    }

    @Override
    public CharSequence getAccessibilityClassName() {
        return COUIAccessibilityUtil.COMPOUND_BUTTON_CLASS_NAME;
    }

    @Override
    public int getCompoundPaddingLeft() {
        int padding = super.getCompoundPaddingLeft();
        if (isLayoutRtl() || mButtonDrawable == null) {
            return padding;
        }
        padding += mButtonDrawable.getIntrinsicWidth();
        return TextUtils.isEmpty(getText()) ? padding : padding + mDrawableTextMargin;
    }

    @Override
    public int getCompoundPaddingRight() {
        int padding = super.getCompoundPaddingRight();
        if (!isLayoutRtl() || mButtonDrawable == null) {
            return padding;
        }
        padding += mButtonDrawable.getIntrinsicWidth();
        return TextUtils.isEmpty(getText()) ? padding : padding + mDrawableTextMargin;
    }

    @ViewDebug.ExportedProperty
    public int getState() {
        return mState;
    }

    @Override
    public boolean isChecked() {
        return getState() == SELECT_ALL;
    }

    @Override
    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        if (mButtonDrawable != null) {
            mButtonDrawable.jumpToCurrentState();
        }
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        int[] state = super.onCreateDrawableState(extraSpace + 1);
        if (getState() == SELECT_PART) {
            mergeDrawableStates(state, PARTSELECT_SET);
        }
        if (getState() == SELECT_ALL) {
            mergeDrawableStates(state, ALLSELECT_SET);
        }
        return state;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mButtonDrawable != null) {
            mButtonDrawable.setBounds(BUTTON_DRAWABLE_BOUNDS);
            mButtonDrawable.draw(canvas);
            Drawable background = getBackground();
            if (background != null) {
                background.setHotspotBounds(BUTTON_DRAWABLE_BOUNDS.left, BUTTON_DRAWABLE_BOUNDS.top,
                        BUTTON_DRAWABLE_BOUNDS.right, BUTTON_DRAWABLE_BOUNDS.bottom);
            }
        }
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setChecked(mState == SELECT_ALL);
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setCheckable(true);
        info.setChecked(mState == SELECT_ALL);
        info.setClassName(COUIAccessibilityUtil.COMPOUND_BUTTON_CLASS_NAME);
        info.setStateDescription(getButtonStateDescription());
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        setState(savedState.mState);
        requestLayout();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        SavedState savedState = new SavedState(super.onSaveInstanceState());
        savedState.mState = getState();
        return savedState;
    }

    @Override
    public boolean performClick() {
        toggle();
        return super.performClick();
    }

    public void refresh() {
        String resourceTypeName = getResources().getResourceTypeName(mStyle);
        TypedArray a = null;
        if ("attr".equals(resourceTypeName)) {
            a = getContext().obtainStyledAttributes(null, R.styleable.COUICheckBox, mStyle, 0);
        } else if ("style".equals(resourceTypeName)) {
            a = getContext().obtainStyledAttributes(null, R.styleable.COUICheckBox, 0, mStyle);
        }
        if (a != null) {
            Drawable drawable = a.getDrawable(R.styleable.COUICheckBox_couiButton);
            if (drawable != null) {
                setButtonDrawable(drawable);
            }
            a.recycle();
        }
        if (mStateEffectBackground != null) {
            mStateEffectBackground.refresh(getContext());
        }
    }

    @Override
    public void setBackground(Drawable background) {
        if (mStateEffectBackground == null) {
            super.setBackground(background);
        } else {
            mStateEffectBackground.setViewBackground(
                    background == null ? new ColorDrawable(0) : background);
        }
    }

    public void setButtonDrawable(@DrawableRes int resId) {
        if (resId == 0 || resId != mButtonResource) {
            mButtonResource = resId;
            setButtonDrawable(resId == 0 ? null
                    : ResourcesCompat.getDrawable(getResources(), mButtonResource, getContext().getTheme()));
        }
    }

    public void setButtonDrawable(Drawable drawable) {
        if (drawable != null) {
            if (mButtonDrawable != null) {
                mButtonDrawable.setCallback(null);
                unscheduleDrawable(mButtonDrawable);
            }
            drawable.setCallback(this);
            drawable.setState(getDrawableState());
            drawable.setVisible(getVisibility() == VISIBLE, false);
            mButtonDrawable = drawable;
            drawable.setState(null);
            setMinHeight(mButtonDrawable.getIntrinsicHeight());
        }
        refreshDrawableState();
    }

    @Override
    public void setChecked(boolean checked) {
        setState(checked ? SELECT_ALL : SELECT_NONE);
    }

    public void setOnStateChangeListener(OnStateChangeListener listener) {
        mOnStateChangeListener = listener;
    }

    public void setState(int state) {
        if (mPendingState != SELECT_UNSPECIFIC) {
            mPendingState = state;
            configDefaultDrawable(state);
            return;
        }
        if (state < SELECT_NONE || state > SELECT_ALL) {
            state = SELECT_NONE;
        }
        if (mState == state) {
            return;
        }
        mState = state;
        refreshDrawableState();
        if (mBroadcasting) {
            return;
        }
        mBroadcasting = true;
        if (mOnStateChangeListener != null) {
            mOnStateChangeListener.onStateChanged(this, mState);
        }
        mBroadcasting = false;
        updateStateDescription();
    }

    @Override
    public void toggle() {
        setState(mState >= SELECT_ALL ? SELECT_NONE : SELECT_ALL);
    }

    @Override
    protected boolean verifyDrawable(@NonNull Drawable who) {
        return super.verifyDrawable(who) || who == mButtonDrawable;
    }
}
