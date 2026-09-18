package com.coui.appcompat.uiutil;

import android.content.Context;
import android.database.ContentObserver;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import com.coui.appcompat.R;
import com.coui.appcompat.contextutil.COUIContextUtil;
import com.coui.appcompat.darkmode.COUIDarkModeUtil;
import com.coui.appcompat.log.COUILog;
import com.coui.appcompat.roundcorner.RoundCornerUtil;
import com.oplus.graphics.OplusBlurParam;
import com.oplus.view.ViewRootManager;
import java.util.function.Consumer;

@Deprecated
public class COUIBackgroundBlurBuilder {
    private static final int MAX_ALPHA = 255;
    private static final String SYSTEM_MATERIAL_BLUR_ENABLE = "system_material_blur_enable";
    private static final String TAG = "BackgroundBlurBuilder";
    private int mBlurRadius;
    private Context mContext;
    private float mCornerRadiusBottomLeft;
    private float mCornerRadiusBottomRight;
    private float mCornerRadiusTopLeft;
    private float mCornerRadiusTopRight;
    private Consumer<Boolean> mCrossWindowBlurEnabledListener;
    private boolean mIsDarkMode;
    private View mRootView;
    private float mSmoothWeight;
    private View mTargetView;
    private boolean mUseBackgroundBlur;
    private ViewRootManager mViewRootManager;
    private WindowManager mWindowManager;
    private final ContentObserver mBlurSettingObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
        @Override
        public void onChange(boolean flag) {
            boolean zIsMaterialBlurEnabled = COUIBackgroundBlurBuilder.this.isMaterialBlurEnabled();
            if (COUIBackgroundBlurBuilder.this.mSettingBlurState != zIsMaterialBlurEnabled) {
                COUIBackgroundBlurBuilder.this.mSettingBlurState = zIsMaterialBlurEnabled;
                COUIBackgroundBlurBuilder.this.setBlurEnable("BlurSettingObserver");
            }
        }
    };
    private float[] mBlendColorLight = null;
    private float[] mBlendColorDark = null;
    private float[] mMixColorLight = null;
    private float[] mMixColorDark = null;
    private boolean mSettingBlurState = false;
    private boolean mWindowBlurEnable = false;
    private boolean mIsInitialized = false;

    public COUIBackgroundBlurBuilder(Context context) {
        this.mContext = context;
        init();
    }

    private OplusBlurParam createBlurParams() {
        float[] fArr;
        float[] fArr2;
        OplusBlurParam oplusBlurParam = new OplusBlurParam();
        oplusBlurParam.setBlurType(2);
        boolean flag = this.mIsDarkMode;
        int index = flag ? 2 : 3;
        if (flag) {
            fArr = this.mBlendColorDark;
            fArr2 = this.mMixColorDark;
        } else {
            fArr = this.mBlendColorLight;
            fArr2 = this.mMixColorLight;
        }
        oplusBlurParam.setMaterialParams(index, fArr, fArr2);
        if (RoundCornerUtil.isSupportRoundCornerWhenBlur()) {
            oplusBlurParam.setSmoothCornerWeight(this.mSmoothWeight);
            COUILog.i(TAG, "Current version supports roundCorner when using blur");
        }
        return oplusBlurParam;
    }

    private void init() {
        this.mIsDarkMode = COUIContextUtil.isCOUIDarkTheme(this.mContext) || COUIDarkModeUtil.isNightMode(this.mContext);
        this.mBlurRadius = this.mContext.getResources().getDimensionPixelSize(R.dimen.coui_list_dialog_background_blur_radius);
    }

    public void lambda$applyBlurBackground$0(Boolean bool) {
        if (this.mWindowBlurEnable != bool.booleanValue()) {
            this.mWindowBlurEnable = bool.booleanValue();
            setBlurEnable("CrossWindowBlurEnabledListener");
        }
    }

    public void setBlurEnable(String str) {
        if (this.mViewRootManager == null || this.mTargetView == null) {
            COUILog.w(TAG, "setBlurEnable skipped: mViewRootManager or mTargetView is null, tag:" + str);
            return;
        }
        int color = COUIContextUtil.getColor(this.mContext, R.color.coui_list_dialog_background_color_above_blur);
        int color2 = this.mIsDarkMode ? COUIContextUtil.getColor(this.mContext, R.color.coui_list_dialog_background_color_no_blur_night) : COUIContextUtil.getColor(this.mContext, R.color.coui_list_dialog_background_color_no_blur_light);
        ViewRootManager viewRootManager = this.mViewRootManager;
        if (!this.mSettingBlurState || !this.mWindowBlurEnable) {
            color = color2;
        }
        viewRootManager.setColor(color);
        this.mTargetView.invalidate();
        COUILog.i(TAG, "setBlurEnable mLastBlurState = " + this.mSettingBlurState + ",mWindowBlurEnable = " + this.mWindowBlurEnable + ",tag:" + str);
    }

    private void updateBlurState() {
        if (this.mViewRootManager == null) {
            return;
        }
        this.mViewRootManager.setBlurParams(createBlurParams());
        this.mViewRootManager.setBlurRadius(this.mBlurRadius);
        refreshCornerRadius();
    }

    public void applyBlurBackground() {
        if (useBackgroundBlur()) {
            if (this.mIsInitialized) {
                updateBlurState();
                setBlurEnable("updateBlurState");
                return;
            }
            COUILog.i(TAG, "applyBlurBackground");
            this.mSettingBlurState = isMaterialBlurEnabled();
            this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor(SYSTEM_MATERIAL_BLUR_ENABLE), false, this.mBlurSettingObserver);
            this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
            if (this.mTargetView == null) {
                throw new IllegalStateException("Must setTargetView before applyBlurBackground");
            }
            if (this.mViewRootManager == null) {
                if (this.mRootView != null) {
                    this.mViewRootManager = new ViewRootManager(this.mRootView);
                } else {
                    this.mViewRootManager = new ViewRootManager(this.mTargetView);
                }
            }
            Drawable backgroundBlurDrawable = this.mViewRootManager.getBackgroundBlurDrawable();
            if (this.mCrossWindowBlurEnabledListener == null) {
                this.mCrossWindowBlurEnabledListener = new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean bool) {
                        COUIBackgroundBlurBuilder.this.lambda$applyBlurBackground$0(bool);
                    }
                };
            }
            if (Build.VERSION.SDK_INT >= 31) {
                this.mWindowManager.addCrossWindowBlurEnabledListener(this.mCrossWindowBlurEnabledListener);
                this.mWindowBlurEnable = this.mWindowManager.isCrossWindowBlurEnabled();
            }
            updateBlurState();
            if (backgroundBlurDrawable != null) {
                backgroundBlurDrawable.setAlpha((int) (this.mTargetView.getAlpha() * 255.0f));
                this.mTargetView.setBackground(backgroundBlurDrawable);
            }
            setBlurEnable("ApplyBlurBackground");
            this.mIsInitialized = true;
        }
    }

    public boolean isMaterialBlurEnabled() {
        return Settings.System.getInt(this.mContext.getContentResolver(), SYSTEM_MATERIAL_BLUR_ENABLE, 0) == 1;
    }

    public void refreshCornerRadius() {
        this.mViewRootManager.setCornerRadius(this.mCornerRadiusTopLeft, this.mCornerRadiusTopRight, this.mCornerRadiusBottomLeft, this.mCornerRadiusBottomRight);
    }

    public void release() {
        Consumer<Boolean> consumer;
        WindowManager windowManager;
        if (Build.VERSION.SDK_INT >= 31 && (consumer = this.mCrossWindowBlurEnabledListener) != null && (windowManager = this.mWindowManager) != null) {
            windowManager.removeCrossWindowBlurEnabledListener(consumer);
        }
        this.mContext.getContentResolver().unregisterContentObserver(this.mBlurSettingObserver);
        View view = this.mTargetView;
        if (view != null && this.mViewRootManager != null && view.getBackground() == this.mViewRootManager.getBackgroundBlurDrawable()) {
            this.mTargetView.setBackground(null);
        }
        this.mViewRootManager = null;
        this.mIsInitialized = false;
        COUILog.i(TAG, "release");
    }

    public void setBlendColorDark(float[] fArr) {
        this.mBlendColorDark = fArr;
    }

    public void setBlendColorLight(float[] fArr) {
        this.mBlendColorLight = fArr;
    }

    public COUIBackgroundBlurBuilder setBlurRadius(int blurRadius) {
        this.mBlurRadius = blurRadius;
        return this;
    }

    public COUIBackgroundBlurBuilder setCornerRadius(float cornerRadius) {
        this.mCornerRadiusTopLeft = cornerRadius;
        this.mCornerRadiusTopRight = cornerRadius;
        this.mCornerRadiusBottomLeft = cornerRadius;
        this.mCornerRadiusBottomRight = cornerRadius;
        return this;
    }

    public void setMixColorDark(float[] fArr) {
        this.mMixColorDark = fArr;
    }

    public void setMixColorLight(float[] fArr) {
        this.mMixColorLight = fArr;
    }

    public COUIBackgroundBlurBuilder setRootView(View view) {
        this.mRootView = view;
        return this;
    }

    public COUIBackgroundBlurBuilder setSmoothWeight(float smoothWeight) {
        this.mSmoothWeight = smoothWeight;
        return this;
    }

    public COUIBackgroundBlurBuilder setTargetView(View view) {
        this.mTargetView = view;
        return this;
    }

    public COUIBackgroundBlurBuilder setUseBackgroundBlur(boolean useBackgroundBlur, AnimLevel animLevel) {
        return setUseBackgroundBlur(useBackgroundBlur, animLevel, this.mContext.getResources().getBoolean(R.bool.coui_blur_enable));
    }

    public boolean useBackgroundBlur() {
        return this.mUseBackgroundBlur;
    }

    public COUIBackgroundBlurBuilder setCornerRadius(float value, float value_2, float value_3, float value_4) {
        this.mCornerRadiusTopLeft = value;
        this.mCornerRadiusTopRight = value_2;
        this.mCornerRadiusBottomLeft = value_3;
        this.mCornerRadiusBottomRight = value_4;
        return this;
    }

    public COUIBackgroundBlurBuilder setUseBackgroundBlur(boolean flag, AnimLevel animLevel, boolean flag_2) {
        if (ShadowUtils.checkOPlusViewElevationSDK() && UIUtil.confirmLevelAnim(animLevel) && flag_2) {
            this.mUseBackgroundBlur = flag;
        } else {
            Log.e(TAG, "Machines below V do not support setting blurred backgrounds or current animLevel is too low or is in third party theme");
            this.mUseBackgroundBlur = false;
        }
        return this;
    }
}
