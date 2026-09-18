package com.coui.appcompat.edittext;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Editable;
import android.text.Selection;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.Gravity;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.customview.widget.ExploreByTouchHelper;

import java.util.List;
import java.util.Locale;

import com.coui.appcompat.R;
import com.coui.appcompat.animation.COUIInEaseInterpolator;
import com.coui.appcompat.animation.COUILinearInterpolator;
import com.coui.appcompat.animation.COUIMoveEaseInterpolator;
import com.coui.appcompat.textutil.COUIChangeTextUtil;

public class COUIEditText extends AppCompatEditText {
    public static final int MODE_BACKGROUND_NONE = 0;
    public static final int MODE_BACKGROUND_LINE = 1;
    public static final int MODE_BACKGROUND_RECT = 2;
    public static final int MODE_BACKGROUND_NO_LINE = 3;

    private static final long BACKGROUND_ANIMATION_DURATION = 250L;

    private final Paint mNormalPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mFocusedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mDisabledPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mHintPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mBoxPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mEmptyTextPaint = new Paint();
    private final TextPaint mTextPaint = new TextPaint();
    private final Rect mDeleteBounds = new Rect();
    private final RectF mBoxRect = new RectF();
    private final RectF mCutoutRect = new RectF();
    private final COUICutoutDrawable.COUICollapseTextHelper mCollapseTextHelper =
            new COUICutoutDrawable.COUICollapseTextHelper(this);

    private ValueAnimator mFocusAnimator;
    private ValueAnimator mHideFocusAnimator;
    private ValueAnimator mHintAnimator;
    private View.OnFocusChangeListener mEditFocusChangeListener;
    private View.OnTouchListener mCustomEditTextTouchListener;
    private InputConnectionListener mInputConnectionListener;
    private OnTextDeletedListener mTextDeleteListener;
    private OnPasswordDeletedListener mPasswordDeleteListener;
    private COUIErrorEditTextHelper mErrorStateHelper;
    private AccessibilityTouchHelper mTouchHelper;
    private Runnable mCancelDeleteIcon;
    private Runnable mSetDeleteIcon;

    private Drawable mDeleteNormal;
    private Drawable mDeletePressed;
    private Drawable mCurrentDeleteDrawable;
    private GradientDrawable mBoxBackground;
    private ColorStateList mOriginalTextColors;
    private ColorStateList mDefaultHintTextColor;
    private ColorStateList mFocusedHintTextColor;
    private int mOriginalHighlightColor;
    private int mBackgroundMode;
    private int mStrokeWidth;
    private int mFocusedStrokeWidth;
    private int mDefaultStrokeColor;
    private int mFocusedStrokeColor;
    private int mDisabledStrokeColor;
    private int mErrorColor;
    private int mBoxStrokeColor;
    private int mDeleteIconWidth;
    private int mDeleteIconHeight;
    private int mDrawableSizeRight;
    private int mCollapsedTextColor;
    private int mCollapsedTextSize;
    private float mBoxCornerRadiusBottomEnd;
    private float mBoxCornerRadiusBottomStart;
    private float mBoxCornerRadiusTopEnd;
    private float mBoxCornerRadiusTopStart;
    private int mRectModePaddingTop;
    private int mLabelCutoutPadding;
    private int mLineModePaddingMiddle;
    private int mLineModePaddingTop;
    private int mLinePadding;
    private int mRefreshStyle;
    private boolean mQuickDelete;
    private boolean mShowDeleteIcon = true;
    private boolean mDeletable;
    private boolean mShouldHandleDelete;
    private boolean mErrorState;
    private boolean mHintEnabled;
    private boolean mHintAnimationEnabled = true;
    private boolean mHintExpanded = true;
    private boolean mIsEllipsis;
    private boolean mIsEllipsisEnabled = true;
    private boolean mIsProvidingHint = true;
    private boolean mJustShowFocusLine;
    private boolean mInDrawableStateChanged;
    private boolean mForceFinishDetach;
    private boolean mLineExpanded;
    private int mFocusedAlpha;
    private CharSequence mTopHint;
    private CharSequence mOriginalHint;
    private String mDeleteButton;
    private String mInputText = "";
    private int mClickSelectionPosition;
    private float mFocusProgress;
    private float mHintExpansion;
    private Interpolator mPathInterpolator1;
    private Interpolator mPathInterpolator2;

    private COUITextWatcher mDeleteWatcher;

    public interface InputConnectionListener {
        void onCreateInputConnection();
    }

    public interface OnErrorStateChangedListener {
        void onErrorStateChangeAnimationEnd(boolean error);

        void onErrorStateChanged(boolean error);
    }

    public interface OnPasswordDeletedListener {
        boolean onPasswordDeleted();
    }

    public interface OnTextDeletedListener {
        boolean onTextDeleted();
    }

    public class AccessibilityTouchHelper extends ExploreByTouchHelper implements View.OnClickListener {
        private Rect mDeleteRect;
        private Rect mViewRect;

        public AccessibilityTouchHelper(View host) {
            super(host);
        }

        private Rect getItemBounds(int virtualViewId) {
            if (virtualViewId != 0) {
                return new Rect();
            }
            if (mDeleteRect == null) {
                initDeleteRect();
            }
            return mDeleteRect;
        }

        private void initDeleteRect() {
            Rect rect = new Rect();
            mDeleteRect = rect;
            rect.left = getDeleteButtonLeft();
            rect.right = getWidth();
            rect.top = 0;
            rect.bottom = getHeight();
        }

        private void initViewRect() {
            Rect rect = new Rect();
            mViewRect = rect;
            rect.left = 0;
            rect.right = getWidth();
            rect.top = 0;
            rect.bottom = getHeight();
        }

        @Override
        protected int getVirtualViewAt(float x, float y) {
            if (mDeleteRect == null) {
                initDeleteRect();
            }
            Rect rect = mDeleteRect;
            return x < rect.left || x > rect.right || y < rect.top || y > rect.bottom
                    || !isDeleteButtonExist() ? ExploreByTouchHelper.INVALID_ID : 0;
        }

        @Override
        protected void getVisibleVirtualViews(List<Integer> virtualViewIds) {
            if (isDeleteButtonExist()) {
                virtualViewIds.add(0);
            }
        }

        @Override
        public void onClick(View v) {
        }

        @Override
        protected boolean onPerformActionForVirtualView(int virtualViewId, int action, Bundle arguments) {
            if (action != AccessibilityNodeInfoCompat.ACTION_CLICK) {
                return false;
            }
            if (virtualViewId != 0 || !isDeleteButtonExist()) {
                return true;
            }
            onFastDelete();
            return true;
        }

        @Override
        protected void onPopulateEventForVirtualView(int virtualViewId, AccessibilityEvent event) {
            event.setContentDescription(mDeleteButton);
        }

        @Override
        protected void onPopulateNodeForVirtualView(int virtualViewId, AccessibilityNodeInfoCompat node) {
            if (mViewRect == null) {
                initViewRect();
            }
            if (virtualViewId == 0) {
                node.setContentDescription(mDeleteButton);
                node.setClassName(Button.class.getName());
                node.addAction(AccessibilityNodeInfoCompat.ACTION_CLICK);
            }
            node.setBoundsInParent(getItemBounds(virtualViewId));
        }
    }

    public static class COUISavedState extends BaseSavedState {
        public static final Parcelable.Creator<COUISavedState> CREATOR =
                new Parcelable.Creator<COUISavedState>() {
                    @Override
                    public COUISavedState createFromParcel(Parcel in) {
                        return new COUISavedState(in);
                    }

                    @Override
                    public COUISavedState[] newArray(int size) {
                        return new COUISavedState[size];
                    }
                };

        String text;

        COUISavedState(Parcelable superState) {
            super(superState);
        }

        COUISavedState(Parcel in) {
            super(in);
            text = in.readString();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public void readFromParcel(Parcel in) {
            text = in.readString();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeString(text);
        }
    }

    public COUIEditText(Context context) {
        this(context, null);
    }

    public COUIEditText(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.editTextStyle);
    }

    public COUIEditText(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        mOriginalTextColors = getTextColors();
        mOriginalHighlightColor = getHighlightColor();
        mStrokeWidth = 1;
        mFocusedStrokeWidth = 3;
        if (attrs != null) {
            mRefreshStyle = attrs.getStyleAttribute();
        }
        if (mRefreshStyle == 0) {
            mRefreshStyle = defStyleAttr;
        }

        int fallbackError = ContextCompat.getColor(context, R.color.coui_color_error_text_bg);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.COUIEditText, defStyleAttr, 0);
        boolean quickDelete = a.getBoolean(R.styleable.COUIEditText_quickDelete, false);
        mErrorColor = a.getColor(R.styleable.COUIEditText_couiEditTextErrorColor, fallbackError);
        mDeleteNormal = a.getDrawable(R.styleable.COUIEditText_couiEditTextDeleteIconNormal);
        mDeletePressed = a.getDrawable(R.styleable.COUIEditText_couiEditTextDeleteIconPressed);
        mIsEllipsisEnabled = a.getBoolean(R.styleable.COUIEditText_couiEditTextIsEllipsis, true);
        int hintLines = a.getInt(R.styleable.COUIEditText_couiEditTextHintLines,
                COUICutoutDrawable.COUICollapseTextHelper.DEFAULT_HINT_LINES);
        a.recycle();
        setFastDeletable(quickDelete);

        configureDeleteDrawable(mDeleteNormal);
        configureDeleteDrawable(mDeletePressed);
        mCancelDeleteIcon = () -> setCompoundDrawables(null, null, null, null);
        mSetDeleteIcon = () -> setCompoundDrawables(null, null, mDeleteNormal, null);

        mCollapseTextHelper.setHintPaddingStart(
                context.getResources().getDimensionPixelSize(R.dimen.coui_edit_text_hint_start_padding));
        mTouchHelper = new AccessibilityTouchHelper(this);
        ViewCompat.setAccessibilityDelegate(this, mTouchHelper);
        ViewCompat.setImportantForAccessibility(this, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);
        mDeleteButton = context.getString(R.string.coui_slide_delete);
        mTouchHelper.invalidateRoot();
        mErrorStateHelper = new COUIErrorEditTextHelper(this, hintLines);
        initHintMode(context, attrs, defStyleAttr);
        mErrorStateHelper.init(mErrorColor, mFocusedStrokeWidth, mBackgroundMode,
                getCornerRadiiAsArray(), mCollapseTextHelper);

    }

    public class COUITextWatcher implements TextWatcher {
        private COUITextWatcher() {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            updateDeletableStatus(hasFocus());
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    }

    private void initHintMode(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        int fallbackPrimary = resolveColor(R.attr.couiColorPrimary, 0);
        int fallbackCollapsed = resolveColor(R.attr.couiColorSecondNeutral, 0x8a000000);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.COUIEditText, defStyleAttr,
                R.style.Widget_COUI_EditText_HintAnim_Line);
        mCollapseTextHelper.setTextSizeInterpolator(new COUILinearInterpolator());
        mCollapseTextHelper.setPositionInterpolator(new COUILinearInterpolator());
        mCollapseTextHelper.setCollapsedTextGravity(Gravity.TOP | GravityCompat.START);
        mPathInterpolator1 = new COUIMoveEaseInterpolator();
        mPathInterpolator2 = new COUIInEaseInterpolator();
        mHintEnabled = a.getBoolean(R.styleable.COUIEditText_couiHintEnabled, false);
        setTopHint(a.getText(R.styleable.COUIEditText_android_hint));
        if (mHintEnabled) {
            mHintAnimationEnabled = a.getBoolean(R.styleable.COUIEditText_couiHintAnimationEnabled, true);
        }
        mRectModePaddingTop = a.getDimensionPixelOffset(R.styleable.COUIEditText_rectModePaddingTop, 0);
        float cornerRadius = a.getDimension(R.styleable.COUIEditText_cornerRadius, 0.0f);
        mBoxCornerRadiusTopStart = cornerRadius;
        mBoxCornerRadiusTopEnd = cornerRadius;
        mBoxCornerRadiusBottomEnd = cornerRadius;
        mBoxCornerRadiusBottomStart = cornerRadius;
        mFocusedStrokeColor = a.getColor(R.styleable.COUIEditText_couiStrokeColor, fallbackPrimary);
        mStrokeWidth = a.getDimensionPixelSize(R.styleable.COUIEditText_couiStrokeWidth, 0);
        mFocusedStrokeWidth = a.getDimensionPixelSize(R.styleable.COUIEditText_couiFocusStrokeWidth,
                mFocusedStrokeWidth);
        mLinePadding = context.getResources().getDimensionPixelOffset(R.dimen.coui_textinput_line_padding);
        if (mHintEnabled) {
            mLabelCutoutPadding = context.getResources().getDimensionPixelOffset(
                    R.dimen.coui_textinput_label_cutout_padding);
            mLineModePaddingTop = context.getResources().getDimensionPixelOffset(
                    R.dimen.coui_textinput_line_padding_top);
            mLineModePaddingMiddle = context.getResources().getDimensionPixelOffset(
                    R.dimen.coui_textinput_line_padding_middle);
        }
        int backgroundMode = a.getInt(R.styleable.COUIEditText_couiBackgroundMode, MODE_BACKGROUND_NONE);
        setBoxBackgroundMode(backgroundMode);
        if (mBackgroundMode != MODE_BACKGROUND_NONE) {
            setBackground(null);
        }
        int hintColorIndex = R.styleable.COUIEditText_android_textColorHint;
        if (a.hasValue(hintColorIndex)) {
            ColorStateList colorStateList = a.getColorStateList(hintColorIndex);
            mDefaultHintTextColor = colorStateList;
            mFocusedHintTextColor = colorStateList;
        }
        mDefaultStrokeColor = a.getColor(R.styleable.COUIEditText_couiDefaultStrokeColor, 0);
        mDisabledStrokeColor = a.getColor(R.styleable.COUIEditText_couiDisabledStrokeColor, 0);
        String inputText = a.getString(R.styleable.COUIEditText_couiEditTextNoEllipsisText);
        mInputText = inputText;
        setText(inputText);
        setCollapsedTextAppearance(a.getDimensionPixelSize(R.styleable.COUIEditText_collapsedTextSize, 0),
                a.getColorStateList(R.styleable.COUIEditText_collapsedTextColor));
        if (backgroundMode == MODE_BACKGROUND_RECT) {
            mCollapseTextHelper.setTypefaces(Typeface.create(COUIChangeTextUtil.MEDIUM_FONT, 0));
        }
        a.recycle();

        mNormalPaint.setStyle(Paint.Style.FILL);
        mFocusedPaint.setStyle(Paint.Style.FILL);
        mDisabledPaint.setStyle(Paint.Style.FILL);
        mNormalPaint.setColor(mDefaultStrokeColor);
        mFocusedPaint.setColor(mFocusedStrokeColor);
        mDisabledPaint.setColor(mDisabledStrokeColor);
        mHintPaint.setColor(mCollapsedTextColor != 0 ? mCollapsedTextColor : fallbackCollapsed);
        mHintPaint.setTextSize(mCollapsedTextSize);
        mHintPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        mTextPaint.setTextSize(getTextSize());
        mBoxPaint.setStyle(Paint.Style.STROKE);
        setEditText();
    }

    private void configureDeleteDrawable(Drawable drawable) {
        if (drawable == null) {
            return;
        }
        mDeleteIconWidth = Math.max(mDeleteIconWidth, drawable.getIntrinsicWidth());
        mDeleteIconHeight = Math.max(mDeleteIconHeight, drawable.getIntrinsicHeight());
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
    }

    private int resolveColor(int attr, int fallback) {
        TypedValue value = new TypedValue();
        if (getContext().getTheme().resolveAttribute(attr, value, true)) {
            if (value.resourceId != 0) {
                return ContextCompat.getColor(getContext(), value.resourceId);
            }
            return value.data;
        }
        return fallback;
    }

    private void animateFocusLine(boolean focused) {
        if (mBackgroundMode != MODE_BACKGROUND_LINE) {
            return;
        }
        if (focused) {
            animateToShowBackground();
        } else {
            animateToHideBackground();
        }
    }

    private void animateToShowBackground() {
        if (mFocusAnimator == null) {
            mFocusAnimator = new ValueAnimator();
            mFocusAnimator.setInterpolator(mPathInterpolator2);
            mFocusAnimator.setDuration(BACKGROUND_ANIMATION_DURATION);
            mFocusAnimator.addUpdateListener(animation -> {
                mFocusProgress = (float) animation.getAnimatedValue();
                invalidate();
            });
        }
        mFocusedAlpha = 255;
        mFocusAnimator.setFloatValues(0.0f, 1.0f);
        if (mHideFocusAnimator != null && mHideFocusAnimator.isRunning()) {
            mHideFocusAnimator.cancel();
        }
        mFocusAnimator.start();
        mLineExpanded = true;
    }

    private void animateToHideBackground() {
        if (mHideFocusAnimator == null) {
            mHideFocusAnimator = new ValueAnimator();
            mHideFocusAnimator.setInterpolator(mPathInterpolator2);
            mHideFocusAnimator.setDuration(BACKGROUND_ANIMATION_DURATION);
            mHideFocusAnimator.addUpdateListener(animation -> {
                mFocusedAlpha = (Integer) animation.getAnimatedValue();
                invalidate();
            });
        }
        mHideFocusAnimator.setIntValues(255, 0);
        mHideFocusAnimator.start();
        mLineExpanded = false;
    }

    private void applyBoxAttributes() {
        if (mBoxBackground == null) {
            return;
        }
        setBoxAttributes();
        if (mStrokeWidth > -1 && mBoxStrokeColor != 0) {
            mBoxBackground.setStroke(mStrokeWidth, mBoxStrokeColor);
        }
        mBoxBackground.setCornerRadii(getCornerRadiiAsArray());
        invalidate();
    }

    private void applyCutoutPadding(RectF rectF) {
        rectF.left -= mLabelCutoutPadding;
        rectF.top -= mLabelCutoutPadding;
        rectF.right += mLabelCutoutPadding;
        rectF.bottom += mLabelCutoutPadding;
    }

    private void assignBoxBackgroundByMode() {
        if (mBackgroundMode == MODE_BACKGROUND_NONE) {
            mBoxBackground = null;
            return;
        }
        if (mBackgroundMode == MODE_BACKGROUND_RECT && mHintEnabled
                && !(mBoxBackground instanceof COUICutoutDrawable)) {
            mBoxBackground = new COUICutoutDrawable();
        } else if (mBoxBackground == null) {
            mBoxBackground = new GradientDrawable();
        }
    }

    private boolean cutoutEnabled() {
        return mHintEnabled && !TextUtils.isEmpty(mTopHint)
                && (mBoxBackground instanceof COUICutoutDrawable);
    }

    public boolean cutoutIsOpen() {
        return cutoutEnabled() && ((COUICutoutDrawable) mBoxBackground).hasCutout();
    }

    public void destroyAnimators() {
        if (mHintAnimator != null) {
            mHintAnimator.cancel();
            mHintAnimator.removeAllListeners();
            mHintAnimator.removeAllUpdateListeners();
            mHintAnimator = null;
        }
        if (mFocusAnimator != null) {
            mFocusAnimator.cancel();
            mFocusAnimator.removeAllListeners();
            mFocusAnimator.removeAllUpdateListeners();
            mFocusAnimator = null;
        }
        if (mHideFocusAnimator != null) {
            mHideFocusAnimator.cancel();
            mHideFocusAnimator.removeAllListeners();
            mHideFocusAnimator.removeAllUpdateListeners();
            mHideFocusAnimator = null;
        }
    }

    private void openCutout() {
        if (cutoutEnabled()) {
            RectF rectF = mCutoutRect;
            mCollapseTextHelper.getCollapsedTextActualBounds(rectF);
            applyCutoutPadding(rectF);
            ((COUICutoutDrawable) mBoxBackground).setCutout(rectF);
        }
    }

    private void closeCutout() {
        if (cutoutEnabled()) {
            ((COUICutoutDrawable) mBoxBackground).removeCutout();
        }
    }

    private int getBoundsTop() {
        if (mBackgroundMode == MODE_BACKGROUND_LINE) {
            return mLineModePaddingTop;
        }
        if (mBackgroundMode == MODE_BACKGROUND_RECT || mBackgroundMode == MODE_BACKGROUND_NO_LINE) {
            return (int) (mCollapseTextHelper.getCollapsedTextHeight() / 2.0f);
        }
        return 0;
    }

    private Drawable getBoxBackground() {
        if (mBackgroundMode == MODE_BACKGROUND_LINE || mBackgroundMode == MODE_BACKGROUND_RECT) {
            return mBoxBackground;
        }
        return null;
    }

    private int calculateCollapsedTextTopBounds() {
        if (mBackgroundMode == MODE_BACKGROUND_LINE) {
            if (getBoxBackground() != null) {
                return getBoxBackground().getBounds().top;
            }
            return 0;
        }
        if (mBackgroundMode != MODE_BACKGROUND_RECT && mBackgroundMode != MODE_BACKGROUND_NO_LINE) {
            return getPaddingTop();
        }
        if (getBoxBackground() != null) {
            return getBoxBackground().getBounds().top - getLabelMarginTop();
        }
        return 0;
    }

    private int getModePaddingTop() {
        int hintHeight;
        int collapsedTextHeight;
        if (mBackgroundMode == MODE_BACKGROUND_LINE) {
            hintHeight = mLineModePaddingTop + ((int) mCollapseTextHelper.getHintHeight());
            collapsedTextHeight = mLineModePaddingMiddle;
        } else {
            if (mBackgroundMode != MODE_BACKGROUND_RECT && mBackgroundMode != MODE_BACKGROUND_NO_LINE) {
                return 0;
            }
            hintHeight = mRectModePaddingTop;
            collapsedTextHeight = (int) (mCollapseTextHelper.getCollapsedTextHeight() / 2.0f);
        }
        return hintHeight + collapsedTextHeight;
    }

    private float[] getCornerRadiiAsArray() {
        float topEnd = mBoxCornerRadiusTopEnd;
        float topStart = mBoxCornerRadiusTopStart;
        float bottomStart = mBoxCornerRadiusBottomStart;
        float bottomEnd = mBoxCornerRadiusBottomEnd;
        return new float[]{
                topEnd, topEnd,
                topStart, topStart,
                bottomStart, bottomStart,
                bottomEnd, bottomEnd
        };
    }

    private void onApplyBoxBackgroundMode() {
        assignBoxBackgroundByMode();
        updateTextInputBoxBounds();
    }

    private void setBoxAttributes() {
        if (mBackgroundMode == MODE_BACKGROUND_RECT && mFocusedStrokeColor == 0
                && mFocusedHintTextColor != null) {
            mFocusedStrokeColor = mFocusedHintTextColor.getColorForState(getDrawableState(),
                    mFocusedHintTextColor.getDefaultColor());
        }
    }

    private void setEditText() {
        onApplyBoxBackgroundMode();
        mCollapseTextHelper.setExpandedTextSize(getTextSize());
        int gravity = getGravity();
        mCollapseTextHelper.setCollapsedTextGravity((gravity & ~Gravity.VERTICAL_GRAVITY_MASK) | Gravity.TOP);
        mCollapseTextHelper.setExpandedTextGravity(gravity);
        if (mDefaultHintTextColor == null) {
            mDefaultHintTextColor = getHintTextColors();
        }
        boolean myanmar = Locale.getDefault().getLanguage().equals("my");
        if (!myanmar) {
            setHint(mHintEnabled ? null : "");
        }
        if (TextUtils.isEmpty(mTopHint) && !myanmar) {
            CharSequence hint = getHint();
            mOriginalHint = hint;
            setTopHint(hint);
            setHint(mHintEnabled ? null : "");
        }
        mIsProvidingHint = !myanmar;
        updateLabelState(false, true);
        if (mHintEnabled) {
            updateModePadding();
        }
    }

    private void setHintInternal(CharSequence hint) {
        if (TextUtils.equals(hint, mTopHint)) {
            return;
        }
        if (Locale.getDefault().getLanguage().equals("my")) {
            mTopHint = hint;
            super.setHint(hint);
            mCollapseTextHelper.setText(null);
            return;
        }
        mTopHint = hint;
        mOriginalHint = hint;
        mCollapseTextHelper.setText(hint);
        if (!mHintExpanded) {
            openCutout();
        }
        if (mErrorStateHelper != null) {
            mErrorStateHelper.setHintInternal(mCollapseTextHelper);
        }
        setContentDescription(hint);
    }

    private void updateTextInputBoxBounds() {
        if (mBackgroundMode == MODE_BACKGROUND_NONE || mBoxBackground == null || getRight() == 0) {
            return;
        }
        mBoxBackground.setBounds(0, getBoundsTop(), getWidth(), getHeight());
        applyBoxAttributes();
    }

    private void updateTextInputBoxState() {
        if (mBoxBackground == null || mBackgroundMode == MODE_BACKGROUND_NONE
                || mBackgroundMode != MODE_BACKGROUND_RECT) {
            return;
        }
        if (!isEnabled()) {
            mBoxStrokeColor = mDisabledStrokeColor;
        } else if (hasFocus()) {
            mBoxStrokeColor = mFocusedStrokeColor;
        } else {
            mBoxStrokeColor = mDefaultStrokeColor;
        }
        applyBoxAttributes();
    }

    private void updateLineModeBackground() {
        if (mBackgroundMode != MODE_BACKGROUND_LINE) {
            return;
        }
        if (!isEnabled()) {
            mFocusProgress = 0.0f;
            return;
        }
        if (hasFocus()) {
            if (!mLineExpanded) {
                animateFocusLine(true);
            }
        } else if (mLineExpanded) {
            animateFocusLine(false);
        }
    }

    private void animateHint(boolean collapsed) {
        if (!mHintEnabled) {
            return;
        }
        if (mHintAnimator != null) {
            mHintAnimator.cancel();
        }
        float target = collapsed ? 1f : 0f;
        if (!mHintAnimationEnabled) {
            mHintExpansion = target;
            mCollapseTextHelper.setExpansionFraction(mHintExpansion);
            invalidate();
            return;
        }
        mHintAnimator = ValueAnimator.ofFloat(mHintExpansion, target);
        mHintAnimator.setDuration(200L);
        mHintAnimator.setInterpolator(mPathInterpolator1);
        mHintAnimator.addUpdateListener(animation -> {
            mHintExpansion = (float) animation.getAnimatedValue();
            mCollapseTextHelper.setExpansionFraction(mHintExpansion);
            invalidate();
        });
        mHintAnimator.start();
    }

    private void collapseHint(boolean animate) {
        if (mHintAnimator != null && mHintAnimator.isRunning()) {
            mHintAnimator.cancel();
        }
        if (animate && mHintAnimationEnabled) {
            animateHint(true);
        } else {
            mHintExpansion = 1.0f;
            mCollapseTextHelper.setExpansionFraction(1.0f);
        }
        mHintExpanded = false;
        if (cutoutEnabled()) {
            openCutout();
        }
    }

    private void expandHint(boolean animate) {
        if (mHintAnimator != null && mHintAnimator.isRunning()) {
            mHintAnimator.cancel();
        }
        if (animate && mHintAnimationEnabled) {
            animateHint(false);
        } else {
            mHintExpansion = 0.0f;
            mCollapseTextHelper.setExpansionFraction(0.0f);
        }
        if (cutoutEnabled() && ((COUICutoutDrawable) mBoxBackground).hasCutout()) {
            closeCutout();
        }
        mHintExpanded = true;
    }

    private boolean isRtlMode() {
        return getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
    }

    private boolean isEmpty(String text) {
        if (text == null) {
            return false;
        }
        return TextUtils.isEmpty(text);
    }

    private boolean isGravityCenterHorizontal() {
        return (getGravity() & Gravity.HORIZONTAL_GRAVITY_MASK) == Gravity.CENTER_HORIZONTAL;
    }

    public boolean isDeleteButtonExist() {
        return mQuickDelete && !isEmpty(getText().toString()) && hasFocus();
    }

    private void updateDeletableStatus(boolean hasFocus) {
        if (TextUtils.isEmpty(getText().toString())) {
            if (isGravityCenterHorizontal()) {
                setPaddingRelative(0, getPaddingTop(), getPaddingEnd(), getPaddingBottom());
            }
            if (mDeletable) {
                setCompoundDrawables(null, null, null, null);
            } else if (mCancelDeleteIcon != null) {
                post(mCancelDeleteIcon);
            }
            mCurrentDeleteDrawable = null;
            mDeletable = false;
            return;
        }
        if (!hasFocus) {
            if (mDeletable) {
                if (isGravityCenterHorizontal()) {
                    setPaddingRelative(0, getPaddingTop(), getPaddingEnd(), getPaddingBottom());
                }
                if (mCancelDeleteIcon != null) {
                    post(mCancelDeleteIcon);
                }
                mCurrentDeleteDrawable = null;
                mDeletable = false;
                return;
            }
            return;
        }
        if (mDeleteNormal == null || mDeletable) {
            return;
        }
        if (isGravityCenterHorizontal()) {
            setPaddingRelative(mDeleteIconWidth + getCompoundDrawablePadding(), getPaddingTop(),
                    getPaddingEnd(), getPaddingBottom());
        }
        if (isFastDeletable() && mShowDeleteIcon && mSetDeleteIcon != null) {
            post(mSetDeleteIcon);
            mCurrentDeleteDrawable = mDeleteNormal;
        }
        mDeletable = true;
        updateLabelState(true);
    }

    private boolean getDeleteRect(Rect outRect) {
        int left = isRtlMode()
                ? (getCompoundPaddingLeft() - mDeleteIconWidth) - getCompoundDrawablePadding()
                : (getWidth() - getCompoundPaddingRight()) + getCompoundDrawablePadding();
        int right = mDeleteIconWidth + left;
        int top = ((((getHeight() - getCompoundPaddingTop()) - getCompoundPaddingBottom())
                - mDeleteIconWidth) / 2) + getCompoundPaddingTop();
        outRect.set(left, top, right, mDeleteIconWidth + top);
        return true;
    }

    public void onFastDelete() {
        Editable text = getText();
        text.delete(0, text.length());
    }

    @Override
    public void draw(Canvas canvas) {
        if (getMaxLines() < 2 && mIsEllipsisEnabled) {
            setEllipsize();
        }
        if (getHintTextColors() != mDefaultHintTextColor) {
            updateLabelState(false);
        }
        int save = canvas.save();
        canvas.translate(getScrollX(), getScrollY());
        if (mHintEnabled || getText().length() == 0) {
            mCollapseTextHelper.draw(canvas);
        } else {
            canvas.drawText(" ", 0.0f, 0.0f, mEmptyTextPaint);
        }
        if (mBoxBackground != null && mBackgroundMode == MODE_BACKGROUND_RECT) {
            if (getScrollX() != 0) {
                updateTextInputBoxBounds();
            }
            if (mErrorStateHelper.isErrorState()) {
                mErrorStateHelper.drawModeBackgroundRect(canvas, mBoxBackground, mBoxStrokeColor);
            } else {
                mBoxBackground.draw(canvas);
            }
        }
        if (mBackgroundMode == MODE_BACKGROUND_LINE) {
            int height = getHeight();
            mFocusedPaint.setAlpha(mFocusedAlpha);
            if (isEnabled()) {
                if (mErrorStateHelper.isErrorState()) {
                    mErrorStateHelper.drawModeBackgroundLine(canvas, height, getWidth(),
                            (int) (mFocusProgress * getWidth()), mNormalPaint, mFocusedPaint);
                } else {
                    if (!mJustShowFocusLine) {
                        canvas.drawRect(0.0f, height - mStrokeWidth, getWidth(), height, mNormalPaint);
                    }
                    if (hasFocus()) {
                        canvas.drawRect(0.0f, height - mFocusedStrokeWidth,
                                mFocusProgress * getWidth(), height, mFocusedPaint);
                    }
                }
            } else if (!mJustShowFocusLine) {
                canvas.drawRect(0.0f, height - mStrokeWidth, getWidth(), height, mDisabledPaint);
            }
        }
        canvas.restoreToCount(save);
        super.draw(canvas);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mErrorStateHelper.onDraw(canvas);
    }

    @Override
    public boolean dispatchHoverEvent(MotionEvent event) {
        if (isDeleteButtonExist() && mTouchHelper != null && mTouchHelper.dispatchHoverEvent(event)) {
            return true;
        }
        return super.dispatchHoverEvent(event);
    }

    @Override
    public void dispatchStartTemporaryDetach() {
        super.dispatchStartTemporaryDetach();
        if (mForceFinishDetach) {
            onStartTemporaryDetach();
        }
    }

    public void forceFinishDetach() {
        mForceFinishDetach = true;
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mInputConnectionListener != null) {
            mInputConnectionListener = null;
        }
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mBoxBackground != null) {
            updateTextInputBoxBounds();
        }
        if (mHintEnabled) {
            updateModePadding();
        }
        int compoundPaddingLeft = getCompoundPaddingLeft();
        int width = getWidth() - getCompoundPaddingRight();
        int collapsedTextTopBounds = calculateCollapsedTextTopBounds();
        mCollapseTextHelper.setExpandedBounds(compoundPaddingLeft, getCompoundPaddingTop(), width,
                getHeight() - getCompoundPaddingBottom());
        mCollapseTextHelper.setCollapsedBounds(compoundPaddingLeft, collapsedTextTopBounds, width,
                getHeight() - getCompoundPaddingBottom());
        mCollapseTextHelper.recalculate();
        if (cutoutEnabled() && !mHintExpanded) {
            openCutout();
        }
        mErrorStateHelper.onLayout(mCollapseTextHelper);
    }

    @Override
    public void drawableStateChanged() {
        if (mInDrawableStateChanged) {
            return;
        }
        mInDrawableStateChanged = true;
        super.drawableStateChanged();
        int[] drawableState = getDrawableState();
        if (mHintEnabled) {
            updateLabelState(ViewCompat.isLaidOut(this) && isEnabled());
        } else {
            updateLabelState(false);
        }
        updateLineModeBackground();
        if (mHintEnabled) {
            updateTextInputBoxBounds();
            updateTextInputBoxState();
            if (mCollapseTextHelper != null) {
                boolean stateChanged = mCollapseTextHelper.setState(drawableState);
                mErrorStateHelper.drawableStateChanged(drawableState);
                if (stateChanged) {
                    invalidate();
                }
            }
        }
        mInDrawableStateChanged = false;
    }

    private void updateModePadding() {
        setPaddingRelative(isRtlMode() ? getPaddingRight() : getPaddingLeft(), getModePaddingTop(),
                isRtlMode() ? getPaddingLeft() : getPaddingRight(), getPaddingBottom());
    }

    private void setEllipsize() {
        if (isFocused()) {
            if (mIsEllipsis) {
                setText(mInputText);
                setSelection(mClickSelectionPosition >= getSelectionEnd()
                        ? getSelectionEnd() : mClickSelectionPosition);
            }
            mIsEllipsis = false;
            return;
        }
        if (mTextPaint.measureText(String.valueOf(getText())) <= getWidth() || mIsEllipsis) {
            return;
        }
        mInputText = String.valueOf(getText());
        mIsEllipsis = true;
        setText(TextUtils.ellipsize(getText(), mTextPaint, getWidth(), TextUtils.TruncateAt.END));
        if (mErrorState) {
            setErrorState(true);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mShowDeleteIcon && mQuickDelete && !TextUtils.isEmpty(getText()) && hasFocus()) {
            Rect rect = new Rect();
            boolean inDeleteRect = getDeleteRect(rect)
                    && rect.contains((int) event.getX(), (int) event.getY());
            if (mDeletable && inDeleteRect) {
                int action = event.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    mShouldHandleDelete = true;
                    return true;
                }
                if (action != MotionEvent.ACTION_UP) {
                    if (action == MotionEvent.ACTION_MOVE && mShouldHandleDelete) {
                        return true;
                    }
                } else if (mShouldHandleDelete) {
                    OnTextDeletedListener listener = mTextDeleteListener;
                    if (listener != null && listener.onTextDeleted()) {
                        return true;
                    }
                    onFastDelete();
                    mShouldHandleDelete = false;
                    return true;
                }
            }
        }
        if (mCustomEditTextTouchListener != null) {
            mCustomEditTextTouchListener.onTouch(this, event);
        }
        boolean handled = super.onTouchEvent(event);
        mClickSelectionPosition = getSelectionEnd();
        return handled;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mQuickDelete && keyCode == KeyEvent.KEYCODE_DEL) {
            super.onKeyDown(keyCode, event);
            if (mPasswordDeleteListener != null) {
                mPasswordDeleteListener.onPasswordDeleted();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (mQuickDelete) {
            updateDeletableStatus(focused);
        }
        if (mEditFocusChangeListener != null) {
            mEditFocusChangeListener.onFocusChange(this, focused);
        }
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        if (mInputConnectionListener != null) {
            mInputConnectionListener.onCreateInputConnection();
        }
        return super.onCreateInputConnection(outAttrs);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        if (getMaxLines() >= 2 || !mIsEllipsisEnabled || isFocused()) {
            return superState;
        }
        COUISavedState state = new COUISavedState(superState);
        state.text = getCouiEditTexttNoEllipsisText();
        return state;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (getMaxLines() < 2 && mIsEllipsisEnabled && state instanceof COUISavedState) {
            COUISavedState savedState = (COUISavedState) state;
            if (savedState.text != null) {
                setText(savedState.text);
            }
        }
        super.onRestoreInstanceState(state);
    }

    @Override
    public void setOnFocusChangeListener(OnFocusChangeListener listener) {
        mEditFocusChangeListener = listener;
    }

    @Override
    public void setText(CharSequence text, TextView.BufferType type) {
        super.setText(text, type);
        Selection.setSelection(getText(), length());
    }

    public void addOnErrorStateChangedListener(OnErrorStateChangedListener listener) {
        mErrorStateHelper.addOnErrorStateChangedListener(listener);
    }

    public void removeOnErrorStateChangedListener(OnErrorStateChangedListener listener) {
        mErrorStateHelper.removeOnErrorStateChangedListener(listener);
    }

    public String getCouiEditTexttNoEllipsisText() {
        return mIsEllipsis ? mInputText : String.valueOf(getText());
    }

    public Rect getBackgroundRect() {
        if ((mBackgroundMode == MODE_BACKGROUND_LINE || mBackgroundMode == MODE_BACKGROUND_RECT
                || mBackgroundMode == MODE_BACKGROUND_NO_LINE) && getBoxBackground() != null) {
            getBoxBackground().getBounds();
        }
        return null;
    }

    public int getBoxStrokeColor() {
        return mFocusedStrokeColor;
    }

    public int getDeleteButtonLeft() {
        Drawable drawable = mDeleteNormal;
        return ((getRight() - getLeft()) - getPaddingRight())
                - (drawable != null ? drawable.getIntrinsicWidth() : 0);
    }

    public int getDeleteIconWidth() {
        return mDeleteIconWidth;
    }

    @Override
    public CharSequence getHint() {
        if (mHintEnabled) {
            return mTopHint;
        }
        return null;
    }

    public int getLabelMarginTop() {
        if (mHintEnabled) {
            return (int) (mCollapseTextHelper.getCollapsedTextHeight() / 2.0f);
        }
        return 0;
    }

    public boolean isEllipsisEnabled() {
        return mIsEllipsisEnabled;
    }

    public boolean isHintEnabled() {
        return mHintEnabled;
    }

    public boolean isProvidingHint() {
        return mIsProvidingHint;
    }

    public boolean ismHintAnimationEnabled() {
        return mHintAnimationEnabled;
    }

    public void refresh() {
        TypedArray a;
        Drawable drawable;
        String resourceTypeName = getResources().getResourceTypeName(mRefreshStyle);
        if ("attr".equals(resourceTypeName)) {
            a = getContext().getTheme().obtainStyledAttributes(null, R.styleable.COUIEditText,
                    mRefreshStyle, 0);
        } else if (!"style".equals(resourceTypeName)) {
            return;
        } else {
            a = getContext().getTheme().obtainStyledAttributes(null, R.styleable.COUIEditText,
                    0, mRefreshStyle);
        }
        int hintColorIndex = R.styleable.COUIEditText_android_textColorHint;
        if (a.hasValue(hintColorIndex)) {
            ColorStateList colorStateList = a.getColorStateList(hintColorIndex);
            mDefaultHintTextColor = colorStateList;
            mFocusedHintTextColor = colorStateList;
            if (colorStateList == null) {
                mDefaultHintTextColor = getHintTextColors();
            }
        }
        mErrorColor = a.getColor(R.styleable.COUIEditText_couiEditTextErrorColor,
                resolveColor(R.attr.couiColorErrorTextBg, ContextCompat.getColor(getContext(),
                        R.color.coui_color_error_text_bg)));
        mFocusedStrokeColor = a.getColor(R.styleable.COUIEditText_couiStrokeColor,
                resolveColor(R.attr.couiColorPrimary, 0));
        mDefaultStrokeColor = a.getColor(R.styleable.COUIEditText_couiDefaultStrokeColor, 0);
        mDisabledStrokeColor = a.getColor(R.styleable.COUIEditText_couiDisabledStrokeColor, 0);
        mErrorStateHelper.setErrorColor(mErrorColor);
        mNormalPaint.setColor(mDefaultStrokeColor);
        mDisabledPaint.setColor(mDisabledStrokeColor);
        mFocusedPaint.setColor(mFocusedStrokeColor);
        mDeleteNormal = a.getDrawable(R.styleable.COUIEditText_couiEditTextDeleteIconNormal);
        mDeletePressed = a.getDrawable(R.styleable.COUIEditText_couiEditTextDeleteIconPressed);
        Drawable normal = mDeleteNormal;
        if (normal != null) {
            mDeleteIconWidth = normal.getIntrinsicWidth();
            mDeleteIconHeight = mDeleteNormal.getIntrinsicHeight();
            mDeleteNormal.setBounds(0, 0, mDeleteIconWidth, mDeleteIconHeight);
        }
        Drawable pressed = mDeletePressed;
        if (pressed != null) {
            pressed.setBounds(0, 0, mDeleteIconWidth, mDeleteIconHeight);
        }
        if (mQuickDelete && mShowDeleteIcon && !TextUtils.isEmpty(getText()) && hasFocus()
                && mDeletable && (drawable = mDeleteNormal) != null) {
            setCompoundDrawables(null, null, drawable, null);
        }
        updateTextInputBoxState();
        a.recycle();
        invalidate();
    }

    public void setBoxBackgroundMode(int mode) {
        if (mBackgroundMode == mode) {
            return;
        }
        mBackgroundMode = mode;
        onApplyBoxBackgroundMode();
    }

    public void setBoxStrokeColor(int color) {
        if (mFocusedStrokeColor != color) {
            mFocusedStrokeColor = color;
            mFocusedPaint.setColor(color);
            updateTextInputBoxState();
        }
    }

    public void setCollapsedTextAppearance(int textSize, ColorStateList colorStateList) {
        mCollapseTextHelper.setCollapsedTextAppearance(textSize, colorStateList);
        mFocusedHintTextColor = mCollapseTextHelper.getCollapsedTextColor();
        updateLabelState(false);
        if (mErrorStateHelper != null) {
            mErrorStateHelper.setCollapsedTextAppearance(textSize, colorStateList);
        }
    }

    @Override
    public void setCompoundDrawables(Drawable left, Drawable top, Drawable right, Drawable bottom) {
        setCompoundDrawablesRelative(left, top, right, bottom);
        if (right != null) {
            mDrawableSizeRight = right.getBounds().width();
            mCurrentDeleteDrawable = right;
        } else {
            mDrawableSizeRight = 0;
            mCurrentDeleteDrawable = null;
        }
    }

    public void setCouiEditTexttNoEllipsisText(String text) {
        mInputText = text;
        setText(text);
    }

    public void setCustomEditTextOnTouchListener(View.OnTouchListener listener) {
        mCustomEditTextTouchListener = listener;
    }

    public void setDefaultStrokeColor(int color) {
        if (mDefaultStrokeColor != color) {
            mDefaultStrokeColor = color;
            mNormalPaint.setColor(color);
            updateTextInputBoxState();
        }
    }

    public void setDisabledStrokeColor(int color) {
        if (mDisabledStrokeColor != color) {
            mDisabledStrokeColor = color;
            mDisabledPaint.setColor(color);
            updateTextInputBoxState();
        }
    }

    public void setEditFocusChangeListener(View.OnFocusChangeListener listener) {
        mEditFocusChangeListener = listener;
    }

    public void setEditTextColor(int color) {
        setTextColor(color);
        mErrorStateHelper.setOriginalTextColors(getTextColors());
    }

    public void setEditTextDeleteIconNormal(Drawable drawable) {
        if (drawable != null) {
            mDeleteNormal = drawable;
            mDeleteIconWidth = drawable.getIntrinsicWidth();
            mDeleteIconHeight = mDeleteNormal.getIntrinsicHeight();
            mDeleteNormal.setBounds(0, 0, mDeleteIconWidth, mDeleteIconHeight);
            invalidate();
        }
    }

    public void setEditTextDeleteIconPressed(Drawable drawable) {
        if (drawable != null) {
            mDeletePressed = drawable;
            drawable.setBounds(0, 0, mDeleteIconWidth, mDeleteIconHeight);
            invalidate();
        }
    }

    public void setEditTextErrorColor(int color) {
        if (color != mErrorColor) {
            mErrorColor = color;
            mErrorStateHelper.setErrorColor(color);
            invalidate();
        }
    }

    public void setErrorState(boolean error) {
        mErrorState = error;
        mErrorStateHelper.setErrorState(error);
    }

    public boolean isErrorState() {
        return mErrorState;
    }

    public OnTextDeletedListener getTextDeleteListener() {
        return mTextDeleteListener;
    }

    public boolean isFastDeletable() {
        return mQuickDelete;
    }

    public boolean isShowDeleteIcon() {
        return mShowDeleteIcon;
    }

    public void setFastDeletable(boolean deletable) {
        if (mQuickDelete != deletable) {
            mQuickDelete = deletable;
            if (deletable && mDeleteWatcher == null) {
                mDeleteWatcher = new COUITextWatcher();
                addTextChangedListener(mDeleteWatcher);
            }
        }
    }

    public void setHintEnabled(boolean enabled) {
        if (enabled != mHintEnabled) {
            mHintEnabled = enabled;
            if (!enabled) {
                mIsProvidingHint = false;
                if (!TextUtils.isEmpty(mTopHint) && TextUtils.isEmpty(getHint())) {
                    setHint(mTopHint);
                }
                setHintInternal(null);
                return;
            }
            CharSequence hint = getHint();
            if (!TextUtils.isEmpty(hint)) {
                if (TextUtils.isEmpty(mTopHint)) {
                    setTopHint(hint);
                }
                setHint((CharSequence) null);
            }
            mIsProvidingHint = true;
        }
    }

    public void setInputConnectionListener(InputConnectionListener listener) {
        mInputConnectionListener = listener;
    }

    public void setIsEllipsisEnabled(boolean enabled) {
        mIsEllipsisEnabled = enabled;
    }

    public void setJustShowFocusLine(boolean justShowFocusLine) {
        mJustShowFocusLine = justShowFocusLine;
    }

    public void setOnTextDeletedListener(OnTextDeletedListener listener) {
        mTextDeleteListener = listener;
    }

    public void setShowDeleteIcon(boolean show) {
        mShowDeleteIcon = show;
    }

    public void setTextDeletedListener(OnPasswordDeletedListener listener) {
        mPasswordDeleteListener = listener;
    }

    public void setTopHint(CharSequence hint) {
        setHintInternal(hint);
    }

    public void setmHintAnimationEnabled(boolean enabled) {
        mHintAnimationEnabled = enabled;
    }

    public void updateLabelState(boolean animate) {
        updateLabelState(animate, false);
    }

    private void updateLabelState(boolean animate, boolean force) {
        boolean enabled = isEnabled();
        boolean hasText = !TextUtils.isEmpty(getText());
        if (mDefaultHintTextColor != null) {
            mDefaultHintTextColor = getHintTextColors();
            mCollapseTextHelper.setCollapsedTextColor(mFocusedHintTextColor);
            mCollapseTextHelper.setExpandedTextColor(mDefaultHintTextColor);
        }
        if (!enabled) {
            mCollapseTextHelper.setCollapsedTextColor(ColorStateList.valueOf(mDisabledStrokeColor));
            mCollapseTextHelper.setExpandedTextColor(ColorStateList.valueOf(mDisabledStrokeColor));
        } else if (hasFocus() && mFocusedHintTextColor != null) {
            mCollapseTextHelper.setCollapsedTextColor(mFocusedHintTextColor);
        }
        if (hasText || (isEnabled() && hasFocus())) {
            if (force || mHintExpanded) {
                collapseHint(animate);
            }
        } else if ((force || !mHintExpanded) && isHintEnabled()) {
            expandHint(animate);
        }
        if (mErrorStateHelper != null) {
            mErrorStateHelper.updateLabelState(mCollapseTextHelper);
        }
    }
}
