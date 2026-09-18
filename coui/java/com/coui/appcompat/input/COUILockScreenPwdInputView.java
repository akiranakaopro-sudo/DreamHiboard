package com.coui.appcompat.input;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.CheckBox;

import com.coui.appcompat.R;
import com.coui.appcompat.edittext.COUIEditText;
import com.coui.appcompat.edittext.COUIInputView;
import com.coui.appcompat.roundRect.COUIShapePath;
import com.coui.appcompat.roundcorner.RoundCornerUtil;
import com.coui.appcompat.textutil.COUIChangeTextUtil;
import com.coui.appcompat.uiutil.UIUtil;
import com.oplus.graphics.OplusOutlineAdapter;
import com.oplus.graphics.OplusPathAdapter;

public class COUILockScreenPwdInputView extends COUIInputView {
    public static final int DEFAULT_MAX_INPUT_COUNT = 16;
    public static final int DEFAULT_MIN_INPUT_COUNT = 6;

    private static final float HALF_HEIGHT = 2.0f;
    private static final float INNER_SHADOW_DX = 0.0f;
    private static final float INNER_SHADOW_DY_1 = -8.0f;
    private static final float INNER_SHADOW_DY_2 = 2.0f;
    private static final float INNER_SHADOW_RADIUS_1 = 32.0f;
    private static final float INNER_SHADOW_RADIUS_2 = 8.0f;
    private static final float INNER_SHADOW_STROKE_WIDTH_1 = 20.0f;
    private static final float INNER_SHADOW_STROKE_WIDTH_2 = 12.0f;

    private final int DEFAULT_SCREEN_WIDTH_DP;
    private final int INPUT_LOCK_SCREEN_PWD_EDIT_MARGIN;
    private final Path mBackgroundPath;
    private int mBorderLineColor;
    private Paint mBorderPaint;
    private float mButtonBorderWidth;
    private int mDefaultInputLockScreenPwdWidth;
    private Bitmap mInnerShadowBitmap;
    private InnerShadowHelper mInnerShadowHelper;
    private final RectF mInputViewRect;
    private View mLockScreenPwdCard;
    private int mLowerInnerShadowColor;
    private int mMinInputCount;
    private final Rect mOplusOutLineRect;
    private OplusOutlineAdapter mOplusOutline;
    private int mScenesMode;
    private SmoothRoundCornerHelper mSmoothRoundCornerHelper;
    private TextWatcher mTextWatcher;
    private int mUpperInnerShadowColor;

    public final class SmoothRoundCornerHelper {
        private OplusPathAdapter mPathAdapter;
        private final int mSmoothType;

        public SmoothRoundCornerHelper() {
            mPathAdapter = null;
            int smoothStyleType = RoundCornerUtil.getSmoothStyleType();
            mSmoothType = smoothStyleType;
            if (smoothStyleType == 1) {
                mPathAdapter = new OplusPathAdapter(mBackgroundPath, smoothStyleType);
            }
        }

        public OplusPathAdapter getPathAdapter() {
            return mPathAdapter;
        }

        public int getSmoothType() {
            return mSmoothType;
        }

        public void updatePath(RectF rectF, float radius) {
            COUIShapePath.getRoundRectPath(mBackgroundPath, rectF, radius);
        }
    }

    public COUILockScreenPwdInputView(Context context) {
        this(context, null);
    }

    public COUILockScreenPwdInputView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public COUILockScreenPwdInputView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        DEFAULT_SCREEN_WIDTH_DP = 360;
        INPUT_LOCK_SCREEN_PWD_EDIT_MARGIN = getResources()
                .getDimensionPixelOffset(R.dimen.coui_input_lock_screen_pwd_edit_margin);
        mOplusOutLineRect = new Rect();
        mBackgroundPath = new Path();
        mInputViewRect = new RectF();
        mMinInputCount = DEFAULT_MIN_INPUT_COUNT;
        mScenesMode = 0;
        mSmoothRoundCornerHelper = null;
        mBorderLineColor = 0;
    }

    private void addSmoothRoundRect(RectF rectF) {
        float radius = rectF.height() / HALF_HEIGHT;
        if (mSmoothRoundCornerHelper == null) {
            mSmoothRoundCornerHelper = new SmoothRoundCornerHelper();
        }
        int smoothType = mSmoothRoundCornerHelper.getSmoothType();
        if (smoothType == 0) {
            mBackgroundPath.reset();
            mSmoothRoundCornerHelper.updatePath(rectF, radius);
        } else if (smoothType != 1) {
            mBackgroundPath.reset();
            mBackgroundPath.addRoundRect(rectF, rectF.height() / HALF_HEIGHT, rectF.height() / HALF_HEIGHT,
                    Path.Direction.CCW);
        } else {
            OplusPathAdapter pathAdapter = mSmoothRoundCornerHelper.getPathAdapter();
            mBackgroundPath.reset();
            pathAdapter.addSmoothRoundRect(rectF.left, rectF.top, rectF.right, rectF.bottom, radius, radius,
                    Path.Direction.CCW);
        }
    }

    private void clipInputView() {
        setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                if (RoundCornerUtil.getSmoothStyleType() != 1) {
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), view.getHeight() / HALF_HEIGHT);
                    return;
                }
                if (mOplusOutline == null) {
                    mOplusOutline = new OplusOutlineAdapter(outline, 1);
                }
                mOplusOutLineRect.set(0, 0, view.getWidth(), view.getHeight());
                mOplusOutline.setSmoothRoundRect(mOplusOutLineRect, (view.getHeight() * view.getScaleY()) / HALF_HEIGHT);
            }
        });
        setClipToOutline(true);
    }

    private void drawInnerBorder(Canvas canvas) {
        int save = canvas.save();
        if (mBorderPaint == null) {
            mBorderPaint = new Paint();
            mBorderPaint.setColor(mBorderLineColor);
            mBorderPaint.setStyle(Paint.Style.STROKE);
            mBorderPaint.setStrokeWidth(mButtonBorderWidth * HALF_HEIGHT);
        }
        canvas.drawPath(mBackgroundPath, mBorderPaint);
        canvas.restoreToCount(save);
    }

    private void drawInnerShadow(Canvas canvas) {
        int save = canvas.save();
        canvas.drawBitmap(mInnerShadowBitmap, 0.0f, 0.0f, null);
        canvas.restoreToCount(save);
    }

    private void initAttr() {
        mDefaultInputLockScreenPwdWidth = getResources()
                .getDimensionPixelOffset(R.dimen.coui_input_lock_screen_pwd_setting_width);
        mLockScreenPwdCard = findViewById(R.id.lock_screen_pwd_card);
        getEditText().setVerticalScrollBarEnabled(false);
        COUIChangeTextUtil.adaptFontSize(getEditText(), 3);
        Resources resources = getContext().getResources();
        mBorderLineColor = resources.getColor(R.color.coui_input_lock_screen_border_color);
        mButtonBorderWidth = resources.getDimension(R.dimen.coui_input_lock_screen_border_width);
        mUpperInnerShadowColor = resources.getColor(R.color.coui_input_lock_screen_upper_inner_shadow_color);
        mLowerInnerShadowColor = resources.getColor(R.color.coui_input_lock_screen_lower_inner_shadow_color);
    }

    private void initInnerShadowBitmap() {
        if (mInnerShadowHelper == null) {
            mInnerShadowHelper = new InnerShadowHelper(getWidth(), getHeight());
        } else {
            mInnerShadowHelper.reset();
        }
        mInnerShadowHelper.addInnerShadowLayer(INNER_SHADOW_RADIUS_1, INNER_SHADOW_DX, INNER_SHADOW_DY_1,
                mUpperInnerShadowColor, 0, INNER_SHADOW_STROKE_WIDTH_1, mBackgroundPath);
        mInnerShadowHelper.addInnerShadowLayer(INNER_SHADOW_RADIUS_2, INNER_SHADOW_DX, INNER_SHADOW_DY_2,
                mLowerInnerShadowColor, 0, INNER_SHADOW_STROKE_WIDTH_2, mBackgroundPath);
        mInnerShadowBitmap = mInnerShadowHelper.createInnerShadowBitmap();
    }

    public void append(String value) {
        mEditText.append(value);
    }

    public void cropBeyondFont() {
        String text = mEditText.getCouiEditTexttNoEllipsisText();
        if (mMaxCount <= 0 || mEditText.getText() == null) {
            return;
        }
        if (text.length() > mMaxCount) {
            mEditText.setText(text.subSequence(0, mMaxCount));
        }
    }

    @Override
    public int getEdittextPaddingBottom() {
        return getResources().getDimensionPixelSize(R.dimen.coui_input_lock_screen_pwd_title_padding_bottom);
    }

    @Override
    public int getEdittextPaddingEnd() {
        return mButtonLayout.getWidth();
    }

    @Override
    public int getEdittextPaddingTop() {
        return getResources().getDimensionPixelSize(R.dimen.coui_input_lock_screen_pwd_title_padding_top);
    }

    public int getInputCount() {
        String text = mEditText.getCouiEditTexttNoEllipsisText();
        if (mEditText.getText() == null || text.length() <= 0) {
            return 0;
        }
        return text.length();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.coui_input_lock_screen_pwd_view;
    }

    public int getMinInputCount() {
        return mMinInputCount;
    }

    @Deprecated
    public View getmLockScreenPwdCard() {
        return mLockScreenPwdCard;
    }

    @Override
    public void handleWithCountTextView() {
    }

    @Override
    public COUIEditText instanceCOUIEditText(Context context, AttributeSet attrs) {
        return mScenesMode == 1
                ? new COUIEditText(context, attrs, R.attr.COUICardLockScreenPwdInputStyleEditDesktop)
                : new COUIEditText(context, attrs, R.attr.COUICardLockScreenPwdInputStyleEdit);
    }

    public void lazyInitInputView(AttributeSet attrs, int scenesMode) {
        mScenesMode = scenesMode;
        lazyInit(getContext(), attrs);
        initAttr();
        clipInputView();
    }

    @Override
    public void nowInit(Context context, AttributeSet attrs) {
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mScenesMode == 1 && UIUtil.confirmLevelAnim(UIUtil.ANIM_LEVEL_SUPPORT_BLUR_MIN)) {
            drawInnerBorder(canvas);
            drawInnerShadow(canvas);
        }
    }

    @Override
    public void onSizeChanged(int width, int height, int oldw, int oldh) {
        super.onSizeChanged(width, height, oldw, oldh);
        mInputViewRect.right = width;
        mInputViewRect.bottom = height;
        addSmoothRoundRect(mInputViewRect);
        if (width > 0 && height > 0 && mScenesMode == 1
                && UIUtil.confirmLevelAnim(UIUtil.ANIM_LEVEL_SUPPORT_BLUR_MIN)) {
            initInnerShadowBitmap();
        }
    }

    public void removeLastInputText() {
        String text = mEditText.getCouiEditTexttNoEllipsisText();
        if (mEditText.getText() == null || text.length() <= 0) {
            return;
        }
        mEditText.setText(text.subSequence(0, text.length() - 1));
    }

    public void setCheckBoxImageResourceDesktop() {
        ((CheckBox) findViewById(R.id.checkbox_password)).setButtonDrawable(R.drawable.coui_edittext_password_icon_desktop);
    }

    @Deprecated
    public void setDefaultInputLockScreenPwdWidth(int width) {
        mDefaultInputLockScreenPwdWidth = width;
    }

    @Override
    public void setEnableInputCount(boolean enabled) {
        mEnableInputCount = enabled;
        cropBeyondFont();
        handleWithCount();
    }

    public void setInputType(int inputType) {
        if (mInputType == inputType) {
            return;
        }
        mInputType = inputType;
        handleWithPassword();
    }

    @Override
    public void setMaxCount(int maxCount) {
        mMaxCount = maxCount;
        cropBeyondFont();
        handleWithCount();
    }

    public void setMinInputCount(int minInputCount) {
        mMinInputCount = minInputCount;
    }

    @Deprecated
    public void updateCardWidth() {
    }
}
