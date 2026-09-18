package com.coui.appcompat.dialog.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Outline;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;

import androidx.appcompat.widget.LinearLayoutCompat;

import com.coui.appcompat.R;
import com.coui.appcompat.contextutil.COUIContextUtil;
import com.coui.appcompat.log.COUILog;
import com.coui.appcompat.roundcorner.RoundCornerUtil;
import com.oplus.graphics.OplusOutline;

public class COUIAlertDialogClipCornerLinearLayout extends LinearLayoutCompat {
    private static final String TAG = "COUIAlertDialogClipCorner";

    private boolean mBlurBackgroundWindow;
    private boolean mIsSupportRoundCornerWhenBlur;
    private boolean mIsSupportSmoothRoundCorner;
    private int mRadius;

    public COUIAlertDialogClipCornerLinearLayout(Context context) {
        super(context);
        initDefaults();
    }

    public COUIAlertDialogClipCornerLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initDefaults();
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.COUIAlertDialogClipCornerLinearLayout);
        boolean supportSmooth = RoundCornerUtil.isVersionSupport();
        mIsSupportSmoothRoundCorner = supportSmooth;
        mRadius = a.getDimensionPixelSize(
                R.styleable.COUIAlertDialogClipCornerLinearLayout_clip_radius,
                COUIContextUtil.getAttrDimens(getContext(),
                        supportSmooth ? R.attr.couiRoundCornerXLRadius : R.attr.couiRoundCornerXL));
        a.recycle();
    }

    public COUIAlertDialogClipCornerLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initDefaults();
    }

    private void initDefaults() {
        mBlurBackgroundWindow = false;
        mIsSupportRoundCornerWhenBlur = false;
        mIsSupportSmoothRoundCorner = false;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mRadius > 0) {
            setClipToOutline(true);
            setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    boolean notUseRoundCornerWhenBlur =
                            mBlurBackgroundWindow && !mIsSupportRoundCornerWhenBlur;
                    if (!mIsSupportSmoothRoundCorner || notUseRoundCornerWhenBlur) {
                        outline.setRoundRect(0, 0, getMeasuredWidth(), getMeasuredHeight(), mRadius);
                    } else {
                        new OplusOutline(outline).setSmoothRoundRect(0, 0, getMeasuredWidth(),
                                getMeasuredHeight(), mRadius,
                                COUIContextUtil.getFloat(getContext(), R.attr.couiRoundCornerXLWeight));
                    }
                    COUILog.i(TAG, "getOutline: notUseRoundCornerWhenBlur"
                            + notUseRoundCornerWhenBlur + " mBlurBackgroundWindow=" + mBlurBackgroundWindow
                            + " mIsSupportRoundCornerWhenBlur=" + mIsSupportRoundCornerWhenBlur
                            + " mIsSupportSmoothRoundCorner=" + mIsSupportSmoothRoundCorner
                            + " mRadius=" + mRadius);
                }
            });
        }
    }

    public void setBlurBackgroundWindow(boolean blurBackgroundWindow) {
        mBlurBackgroundWindow = blurBackgroundWindow;
    }

    public void setIsSupportRoundCornerWhenBlur(boolean supportRoundCornerWhenBlur) {
        mIsSupportRoundCornerWhenBlur = supportRoundCornerWhenBlur;
    }
}
