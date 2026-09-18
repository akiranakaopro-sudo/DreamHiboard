package com.coui.appcompat.input;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.text.Editable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.coui.appcompat.R;
import com.coui.appcompat.contextutil.COUIContextUtil;
import com.coui.appcompat.darkmode.COUIDarkModeUtil;
import com.coui.appcompat.edittext.COUIEditText;
import com.coui.appcompat.edittext.COUIInputView;
import com.coui.appcompat.pressfeedback.COUIPressFeedbackHelper;
import com.coui.appcompat.roundcorner.RoundCornerUtil;
import com.coui.appcompat.uiutil.UIUtil;
import com.oplus.graphics.OplusOutlineAdapter;

public class COUILockScreenPwdInputLayout extends ConstraintLayout {
    public static final int DESKTOP = 1;
    public static final int INPUT_VIEW_TRANSPARENT_BG_COLOR =
            R.color.coui_input_lock_screen_pwd_view_bg_color_desktop;
    public static final int SETTING = 0;
    public static final int SETTING1 = 2;

    private static final float GRADIENT_COLOR_STOP_END = 1.0f;
    private static final float GRADIENT_COLOR_STOP_START = 0.0f;
    private static final float GRADIENT_INNER_STOP_1 = 0.7f;
    private static final float GRADIENT_OUTER_STOP_1 = 0.3f;
    private static final float GRADIENT_OUTER_STOP_2 = 0.6f;
    private static final float GRADIENT_OUTER_STOP_3 = 0.8f;
    private static final float HALF_HEIGHT = 2.0f;
    private static final float INNER_SHADOW_DX = 0.0f;
    private static final float INNER_SHADOW_DY_1 = -8.0f;
    private static final float INNER_SHADOW_DY_2 = 2.0f;
    private static final float INNER_SHADOW_RADIUS_1 = 32.0f;
    private static final float INNER_SHADOW_RADIUS_2 = 8.0f;
    private static final float INNER_SHADOW_STROKE_WIDTH_1 = 20.0f;
    private static final float INNER_SHADOW_STROKE_WIDTH_2 = 12.0f;

    private boolean mAllowNext;
    private int mBorderLineColor;
    private Paint mBorderPaint;
    private float mButtonBorderWidth;
    private COUIPressFeedbackHelper mButtonScaleHelper;
    private final Paint mClipPaint;
    private RadialGradient mGradient;
    private RadialGradient mGradient2;
    private int mInnerGradientColor1;
    private int mInnerGradientColor2;
    private Bitmap mInnerShadowBitmap;
    private InnerShadowHelper mInnerShadowHelper;
    private Matrix mInnerShadowMatrix;
    private COUILockScreenPwdInputView mInputView;
    private final Path mInputViewPath;
    private float mLightEffectAlpha;
    private LightEffectHelper mLightEffectHelper;
    private final Path mLightEffectPath;
    private float mLightShaderRadius;
    private int mLowerInnerShadowColor;
    private ImageView mNextIcon;
    private NextIconCheckListener mNextIconCheckListener;
    private final Path mNextIconPath;
    private float mNextIconRadius;
    private COUIInputView.OnEditTextChangeListener mOnEditTextChangeListener;
    private final Rect mOplusOutLineRect;
    private OplusOutlineAdapter mOplusOutline;
    private int mOuterGradientColor1;
    private int mOuterGradientColor2;
    private int mOuterGradientColor3;
    private boolean mRefreshLightEffectPath;
    private int mScenesMode;
    private int mUpperInnerShadowColor;

    public interface NextIconCheckListener {
        void checkedPwd(String password);
    }

    public COUILockScreenPwdInputLayout(Context context) {
        this(context, null);
    }

    public COUILockScreenPwdInputLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public COUILockScreenPwdInputLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mLightEffectPath = new Path();
        mInputViewPath = new Path();
        mNextIconPath = new Path();
        mOplusOutLineRect = new Rect();
        mClipPaint = new Paint(1);
        mScenesMode = 0;
        mAllowNext = false;
        mRefreshLightEffectPath = true;
        mBorderLineColor = 0;
        mLightEffectAlpha = 0.0f;
        LayoutInflater.from(getContext()).inflate(getLayoutResId(), (ViewGroup) this, true);
        initAttr(context, attrs, defStyleAttr);
    }

    private void changeNextTransparentImageResource(boolean allowNext) {
        if (mAllowNext == allowNext) {
            return;
        }
        mAllowNext = allowNext;
        if (allowNext && mScenesMode == DESKTOP) {
            setAllowNextDesktopBackground();
            return;
        }
        if (!allowNext && mScenesMode == DESKTOP) {
            setNormalNextDesktopBackground();
        } else if (allowNext) {
            setAllowNextSettingBackground();
        } else {
            setNormalNextSettingBackground();
        }
    }

    private void clipNextIconView() {
        if (mNextIcon == null) {
            return;
        }
        mNextIcon.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                if (RoundCornerUtil.getSmoothStyleType() != 1) {
                    outline.setRoundRect(0, 0, (int) (view.getWidth() * view.getScaleX()),
                            (int) (view.getHeight() * view.getScaleY()),
                            (view.getHeight() * view.getScaleY()) / HALF_HEIGHT);
                } else {
                    if (mOplusOutline == null) {
                        mOplusOutline = new OplusOutlineAdapter(outline, 1);
                    }
                    mOplusOutLineRect.set(0, 0, (int) (view.getWidth() * view.getScaleX()),
                            (int) (view.getHeight() * view.getScaleY()));
                    mOplusOutline.setSmoothRoundRect(mOplusOutLineRect,
                            (view.getHeight() * view.getScaleY()) / HALF_HEIGHT);
                }
                outline.setRoundRect(0, 0, (int) (view.getWidth() * view.getScaleX()),
                        (int) (view.getHeight() * view.getScaleY()),
                        (view.getHeight() * view.getScaleY()) / HALF_HEIGHT);
            }
        });
        mNextIcon.setClipToOutline(true);
    }

    private void drawInnerBorder(Canvas canvas, Path path) {
        int save = canvas.save();
        canvas.clipPath(path);
        if (mBorderPaint == null) {
            mBorderPaint = new Paint(1);
            mBorderPaint.setColor(mBorderLineColor);
            mBorderPaint.setStyle(Paint.Style.STROKE);
            mBorderPaint.setStrokeWidth(mButtonBorderWidth * HALF_HEIGHT);
        }
        canvas.drawPath(path, mBorderPaint);
        canvas.restoreToCount(save);
    }

    private void drawInnerShadow(Canvas canvas) {
        float left = (mNextIcon.getLeft() + mNextIcon.getRight()) / HALF_HEIGHT;
        float top = (mNextIcon.getTop() + mNextIcon.getBottom()) / HALF_HEIGHT;
        float measuredHeight = (mNextIcon.getMeasuredHeight() * mNextIcon.getScaleY()) / HALF_HEIGHT;
        int save = canvas.save();
        if (mInnerShadowMatrix == null) {
            mInnerShadowMatrix = new Matrix();
        } else {
            mInnerShadowMatrix.reset();
        }
        mInnerShadowMatrix.postScale(mNextIcon.getScaleY(), mNextIcon.getScaleY());
        mInnerShadowMatrix.postTranslate(left - measuredHeight, top - measuredHeight);
        canvas.drawBitmap(mInnerShadowBitmap, mInnerShadowMatrix, null);
        canvas.clipPath(mNextIconPath);
        canvas.restoreToCount(save);
    }

    private void drawLightEffect(Canvas canvas) {
        if (mLightEffectAlpha > 0.0f) {
            refreshLightEffectPaths();
            canvas.save();
            mLightEffectHelper.drawLightEffect(canvas, mNextIcon.getScaleX(), mLightEffectPath, mClipPaint,
                    (mNextIcon.getLeft() + mNextIcon.getRight()) / HALF_HEIGHT,
                    (mNextIcon.getTop() + mNextIcon.getBottom()) / HALF_HEIGHT);
            canvas.restore();
        }
    }

    private void ensureButtonScaleAnimator() {
        if (mButtonScaleHelper == null) {
            mButtonScaleHelper = new COUIPressFeedbackHelper(mNextIcon);
            mButtonScaleHelper.setCallback(new COUIPressFeedbackHelper.COUIPressFeedbackHelperCallback() {
                @Override
                public void onScaleUpdate(float scale) {
                    if (mScenesMode == DESKTOP && mNextIcon.getVisibility() == VISIBLE
                            && UIUtil.confirmLevelAnim(UIUtil.ANIM_LEVEL_SUPPORT_BLUR_MIN)) {
                        clipNextIconView();
                    }
                    invalidate();
                }
            });
        }
    }

    private void ensureLightEffectAnimator() {
        if (mLightEffectHelper == null) {
            mLightEffectHelper = new LightEffectHelper(this);
            mLightEffectHelper.setCallback(new LightEffectHelper.LightEffectHelperCallback() {
                @Override
                public void onInnerLightUpdate(float alpha) {
                    mLightEffectAlpha = alpha;
                }
            });
        }
    }

    private void executeLightEffectAnimator(boolean pressed) {
        ensureLightEffectAnimator();
        ensureButtonScaleAnimator();
        mLightEffectHelper.executeLightEffectAnimator(pressed);
        mButtonScaleHelper.executeFeedbackAnimator(pressed);
    }

    private void initAttr(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.COUILockScreenPwdInputLayout,
                defStyleAttr, 0);
        boolean enableInputCount = a.getBoolean(
                R.styleable.COUILockScreenPwdInputLayout_couiEnableInputCount, false);
        int maxCount = a.getInt(R.styleable.COUILockScreenPwdInputLayout_couiInputMaxCount, 0);
        int inputType = a.getInt(R.styleable.COUILockScreenPwdInputLayout_couiInputType, 2);
        mScenesMode = a.getInt(R.styleable.COUILockScreenPwdInputLayout_couiIsScenesMode, 0);
        int minCount = a.getInt(R.styleable.COUILockScreenPwdInputLayout_couiInputMinCount, 6);
        a.recycle();

        mInputView = (COUILockScreenPwdInputView) findViewById(R.id.coui_lock_screen_pwd_input_view);
        mInputView.lazyInitInputView(attrs, mScenesMode);
        mInputView.setInputType(inputType);
        mInputView.setEnableInputCount(enableInputCount);
        mNextIcon = (ImageView) findViewById(R.id.iv_intput_next);

        Resources resources = context.getResources();
        mBorderLineColor = resources.getColor(R.color.coui_input_lock_screen_border_color);
        mButtonBorderWidth = resources.getDimension(R.dimen.coui_input_lock_screen_border_width);
        mLightShaderRadius = resources.getDimensionPixelOffset(R.dimen.coui_input_lock_screen_light_shader_radius);
        mUpperInnerShadowColor = resources.getColor(R.color.coui_input_lock_screen_upper_inner_shadow_color);
        mLowerInnerShadowColor = resources.getColor(R.color.coui_input_lock_screen_lower_inner_shadow_color);
        mOuterGradientColor1 = resources.getColor(R.color.coui_input_lock_screen_outer_gradient_color_1);
        mOuterGradientColor2 = resources.getColor(R.color.coui_input_lock_screen_outer_gradient_color_2);
        mOuterGradientColor3 = resources.getColor(R.color.coui_input_lock_screen_outer_gradient_color_3);
        mInnerGradientColor1 = resources.getColor(R.color.coui_input_lock_screen_inner_gradient_color_1);
        mInnerGradientColor2 = resources.getColor(R.color.coui_input_lock_screen_inner_gradient_color_2);

        initInputCountSetting(maxCount, minCount);
        initListener();
        initScenesModeUI(context);
        COUIDarkModeUtil.setForceDarkAllow(this, false);
    }

    private void initInnerShadowBitmap() {
        mNextIconPath.reset();
        float measuredHeight = mNextIcon.getMeasuredHeight() / HALF_HEIGHT;
        mNextIconPath.addCircle(measuredHeight, measuredHeight, measuredHeight, Path.Direction.CCW);
        if (mInnerShadowHelper == null) {
            mInnerShadowHelper = new InnerShadowHelper(mNextIcon.getMeasuredWidth(), mNextIcon.getMeasuredHeight());
        } else {
            mInnerShadowHelper.reset();
        }
        mInnerShadowHelper.addInnerShadowLayer(INNER_SHADOW_RADIUS_1, INNER_SHADOW_DX, INNER_SHADOW_DY_1,
                mUpperInnerShadowColor, 0, INNER_SHADOW_STROKE_WIDTH_1, mNextIconPath);
        mInnerShadowHelper.addInnerShadowLayer(INNER_SHADOW_RADIUS_2, INNER_SHADOW_DX, INNER_SHADOW_DY_2,
                mLowerInnerShadowColor, 0, INNER_SHADOW_STROKE_WIDTH_2, mNextIconPath);
        mInnerShadowBitmap = mInnerShadowHelper.createInnerShadowBitmap();
    }

    private void initInputCountSetting(int maxCount, int minCount) {
        if (maxCount <= 0 || minCount <= 0 || maxCount <= minCount) {
            mInputView.setMaxCount(COUILockScreenPwdInputView.DEFAULT_MAX_INPUT_COUNT);
            mInputView.setMinInputCount(COUILockScreenPwdInputView.DEFAULT_MIN_INPUT_COUNT);
        } else {
            mInputView.setMaxCount(maxCount);
            mInputView.setMinInputCount(minCount);
        }
    }

    private void initListener() {
        mNextIcon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if ((!mInputView.isEnableInputCount() || mInputView.getMinInputCount() <= mInputView.getInputCount())
                        && mNextIconCheckListener != null) {
                    mNextIconCheckListener.checkedPwd(mInputView.getEditText().getCouiEditTexttNoEllipsisText());
                }
            }
        });
        if (mScenesMode == DESKTOP && UIUtil.confirmLevelAnim(UIUtil.ANIM_LEVEL_SUPPORT_BLUR_MIN)) {
            mNextIcon.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    if (mInputView.isEnableInputCount() && mInputView.getMinInputCount() > mInputView.getInputCount()) {
                        return false;
                    }
                    int action = event.getAction();
                    if (action == MotionEvent.ACTION_DOWN) {
                        executeLightEffectAnimator(true);
                    } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                        executeLightEffectAnimator(false);
                    }
                    return false;
                }
            });
        }
        mInputView.setOnEditTextChangeListener(new COUIInputView.OnEditTextChangeListener() {
            @Override
            public void afterTextChange(Editable editable) {
                if (mInputView.isEnableInputCount()) {
                    if (mInputView.getMinInputCount() <= editable.length()) {
                        changeNextTransparentImageResource(true);
                    } else {
                        changeNextTransparentImageResource(false);
                    }
                    if (editable.length() > mInputView.getMaxCount()) {
                        mInputView.getEditText().setText(editable.subSequence(0, mInputView.getMaxCount()));
                    }
                }
                if (mOnEditTextChangeListener != null) {
                    mOnEditTextChangeListener.afterTextChange(editable);
                }
            }
        });
    }

    private void initNextIconPath() {
        mNextIconPath.reset();
        mNextIconPath.addCircle((mNextIcon.getLeft() + mNextIcon.getRight()) / HALF_HEIGHT,
                (mNextIcon.getTop() + mNextIcon.getBottom()) / HALF_HEIGHT,
                (mNextIcon.getHeight() * mNextIcon.getScaleY()) / HALF_HEIGHT, Path.Direction.CCW);
    }

    private void initRadialGradient(float centerX, float centerY) {
        int[] outerColors = {0, mOuterGradientColor1, mOuterGradientColor2, mOuterGradientColor3, 0};
        float[] outerStops = {GRADIENT_COLOR_STOP_START, GRADIENT_OUTER_STOP_1, GRADIENT_OUTER_STOP_2,
                GRADIENT_OUTER_STOP_3, GRADIENT_COLOR_STOP_END};
        Shader.TileMode tileMode = Shader.TileMode.CLAMP;
        mGradient = new RadialGradient(centerX, centerY, mLightShaderRadius, outerColors, outerStops, tileMode);
        mGradient2 = new RadialGradient(centerX, centerY, mNextIconRadius,
                new int[]{0, mInnerGradientColor1, mInnerGradientColor2},
                new float[]{GRADIENT_COLOR_STOP_START, GRADIENT_INNER_STOP_1, GRADIENT_COLOR_STOP_END}, tileMode);
    }

    private void initScenesModeUI(Context context) {
        if (mScenesMode == DESKTOP) {
            clipNextIconView();
            if (UIUtil.confirmLevelAnim(UIUtil.ANIM_LEVEL_SUPPORT_BLUR_MIN)) {
                mNextIcon.setBackgroundColor(0);
                mInputView.setBackgroundColor(0);
            } else {
                mNextIcon.setBackgroundColor(COUIContextUtil.getColor(context,
                        R.color.coui_input_lock_screen_pwd_view_bg_color_desktop));
                mInputView.setBackgroundColor(COUIContextUtil.getColor(context,
                        R.color.coui_input_lock_screen_pwd_view_bg_color_desktop));
            }
            mInputView.setCheckBoxImageResourceDesktop();
            COUIEditText editText = mInputView.getEditText();
            int textColor = R.color.coui_input_lock_screen_pwd_view_edittext_text_color_desktop;
            editText.setTextColor(getResources().getColor(textColor, context.getTheme()));
            mInputView.getEditText().setEditTextColor(getResources().getColor(textColor, context.getTheme()));
            setNormalNextDesktopBackground();
        } else {
            mInputView.setBackgroundColor(COUIContextUtil.getAttrColor(context, R.attr.couiColorCard));
        }
        if (!mInputView.isEnableInputCount() || mInputView.getInputCount() >= mInputView.getMinInputCount()) {
            changeNextTransparentImageResource(true);
        } else {
            changeNextTransparentImageResource(false);
        }
    }

    private void refreshLightEffectPaths() {
        if (mRefreshLightEffectPath) {
            float left = (mNextIcon.getLeft() + mNextIcon.getRight()) / HALF_HEIGHT;
            float top = (mNextIcon.getTop() + mNextIcon.getBottom()) / HALF_HEIGHT;
            initRadialGradient(left, top);
            mLightEffectHelper.updateLightShaderConfig(mNextIconRadius, mLightShaderRadius, mGradient2, mGradient);
            mInputViewPath.reset();
            Path.Direction direction = Path.Direction.CCW;
            mInputViewPath.addRoundRect(mInputView.getLeft(), mInputView.getTop(), mInputView.getRight(),
                    mInputView.getBottom(), mInputView.getHeight() / HALF_HEIGHT,
                    mInputView.getHeight() / HALF_HEIGHT, direction);
            mLightEffectPath.reset();
            mLightEffectPath.addCircle(left, top, mLightShaderRadius, direction);
            mLightEffectPath.op(mInputViewPath, Path.Op.INTERSECT);
            mRefreshLightEffectPath = false;
        }
    }

    private void setAllowNextDesktopBackground() {
        mNextIcon.setImageResource(R.drawable.coui_input_lock_screen_pwd_next_desktop_src_allow);
    }

    private void setAllowNextSettingBackground() {
        mNextIcon.setImageResource(R.drawable.coui_input_lock_screen_pwd_next_src_allow);
        mNextIcon.setBackgroundResource(R.drawable.coui_input_lock_screen_pwd_next_bg);
    }

    private void setNormalNextDesktopBackground() {
        mNextIcon.setImageResource(R.drawable.coui_input_lock_screen_pwd_next_desktop_src);
    }

    private void setNormalNextSettingBackground() {
        mNextIcon.setBackgroundResource(R.drawable.coui_input_lock_screen_pwd_next_bg);
        mNextIcon.setImageResource(R.drawable.coui_input_lock_screen_pwd_next_src);
    }

    public void append(String value) {
        if (!TextUtils.isEmpty(value)) {
            mInputView.append(value);
        }
    }

    public void clearInputText() {
        mInputView.getEditText().setCouiEditTexttNoEllipsisText("");
    }

    public void closeError() {
        if (mInputView.getEditText().isErrorState()) {
            mInputView.showError("");
        }
    }

    @Override
    public void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mScenesMode == DESKTOP && mNextIcon.getVisibility() == VISIBLE
                && UIUtil.confirmLevelAnim(UIUtil.ANIM_LEVEL_SUPPORT_BLUR_MIN)) {
            initNextIconPath();
            drawInnerShadow(canvas);
            drawInnerBorder(canvas, mNextIconPath);
            drawLightEffect(canvas);
        }
    }

    public COUILockScreenPwdInputView getInputView() {
        return mInputView;
    }

    public int getLayoutResId() {
        return R.layout.coui_input_lock_screen_pwd_layout;
    }

    public ImageView getNextIconView() {
        return mNextIcon;
    }

    public boolean isErrorState() {
        return mInputView.getEditText().isErrorState();
    }

    @Override
    public void onSizeChanged(int width, int height, int oldw, int oldh) {
        super.onSizeChanged(width, height, oldw, oldh);
        if (width > 0 && height > 0 && mScenesMode == DESKTOP && mNextIcon.getVisibility() == VISIBLE
                && UIUtil.confirmLevelAnim(UIUtil.ANIM_LEVEL_SUPPORT_BLUR_MIN)) {
            mLightShaderRadius = (width - (mInputView.getMeasuredWidth() / HALF_HEIGHT))
                    - (mNextIcon.getMeasuredWidth() / HALF_HEIGHT);
            mNextIconRadius = mNextIcon.getMeasuredWidth() / HALF_HEIGHT;
            mRefreshLightEffectPath = true;
            initInnerShadowBitmap();
        }
    }

    public void popupKeyboard() {
        mInputView.getEditText().setFocusable(true);
        mInputView.getEditText().setFocusableInTouchMode(true);
        mInputView.getEditText().requestFocus();
    }

    public void removeLastInputText() {
        mInputView.removeLastInputText();
    }

    public void setCOUIEditTextChangeListener(COUIInputView.OnEditTextChangeListener listener) {
        mOnEditTextChangeListener = listener;
    }

    public boolean setCOUIInputMaxCount(int maxCount) {
        if (maxCount <= 0 || maxCount < mInputView.getMinInputCount()) {
            return false;
        }
        mInputView.setMaxCount(maxCount);
        return true;
    }

    public boolean setCOUIInputMinCount(int minCount) {
        if (minCount <= 0 || minCount > mInputView.getMaxCount()) {
            return false;
        }
        mInputView.setMinInputCount(minCount);
        if (!mInputView.isEnableInputCount() || mInputView.getMinInputCount() <= mInputView.getInputCount()) {
            changeNextTransparentImageResource(true);
        } else {
            changeNextTransparentImageResource(false);
        }
        return true;
    }

    public void setCOUIInputType(int inputType) {
        mInputView.setInputType(inputType);
    }

    public void setNextIcOnClickListener(NextIconCheckListener listener) {
        mNextIconCheckListener = listener;
    }

    public void showError() {
        mInputView.showError("error");
    }

    public void withCOUILengthLimit(boolean enabled) {
        mInputView.setEnableInputCount(enabled);
        if (!mInputView.isEnableInputCount() || mInputView.getInputCount() >= mInputView.getMinInputCount()) {
            changeNextTransparentImageResource(true);
        } else {
            changeNextTransparentImageResource(false);
        }
    }
}
