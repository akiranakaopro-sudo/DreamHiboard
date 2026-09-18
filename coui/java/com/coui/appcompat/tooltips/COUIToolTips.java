package com.coui.appcompat.tooltips;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import com.coui.appcompat.R;

public class COUIToolTips extends PopupWindow {
    public static final int ALIGN_RIGHT = 8;
    public static final int ALIGN_LEFT = 16;
    public static final int ALIGN_START = 32;
    public static final int ALIGN_END = 64;
    public static final int ALIGN_BOTTOM = 128;
    public static final int ALIGN_TOP = 256;
    public static final int ANIMATION_DURATION = 300;
    public static final int ANIMATION_EXIT_DURATION = 20;
    public static final int DEFAULT_ALIGN_DIRECTION = 4;
    public static final int MODE_TOOLTIPS = 0;
    public static final int MODE_INFO = 1;

    private static final int DEFAULT_DIRECTION = DEFAULT_ALIGN_DIRECTION;

    private final Context mContext;
    private final Rect mAnchorRect = new Rect();
    private COUIIBubbleStyle mBubbleStyle;
    private ViewGroup mContentContainer;
    private ViewGroup mMainPanel;
    private ImageView mArrowView;
    private View mAnchor;
    private int mMode;
    private int mShowDirection = DEFAULT_DIRECTION;
    private int mOffsetX;
    private int mOffsetY;
    private int mArrowOverflow;
    private ColorStateList mBackgroundColor;
    private OnCloseIconClickListener mOnCloseIconClickListener;
    private OnDismissListener mUserDismissListener;
    private final IToolTipsAction mAction = () -> {
        if (mOnCloseIconClickListener != null) {
            mOnCloseIconClickListener.onCloseIconClick();
        }
        dismiss();
    };

    public interface OnCloseIconClickListener {
        void onCloseIconClick();
    }

    @Deprecated
    public COUIToolTips(Window window) {
        this(window, MODE_TOOLTIPS);
    }

    @Deprecated
    public COUIToolTips(Window window, int mode) {
        this(window.getContext(), mode);
    }

    public COUIToolTips(Context context) {
        this(context, MODE_TOOLTIPS);
    }

    public COUIToolTips(Context context, int mode) {
        this(context, mode, new COUIDefaultBubbleStyleImpl.Builder().build());
    }

    public COUIToolTips(Context context, COUIIBubbleStyle bubbleStyle) {
        this(context, MODE_TOOLTIPS, bubbleStyle);
    }

    public COUIToolTips(Context context, int mode, COUIIBubbleStyle bubbleStyle) {
        super(context);
        mContext = context;
        mMode = mode;
        mBubbleStyle = bubbleStyle == null ? new COUIDefaultBubbleStyleImpl.Builder().build() : bubbleStyle;
        init(mode);
    }

    public void init(int mode) {
        mMode = mode;
        mBubbleStyle.setToolTipsAction(mAction, mContext, mMode);
        mContentContainer = new FrameLayout(mContext);
        mContentContainer.setClipChildren(false);
        mContentContainer.setClipToPadding(false);
        mMainPanel = (ViewGroup) LayoutInflater.from(mContext).inflate(mBubbleStyle.getLayoutId(), mContentContainer, false);
        initPopupWindowBackground(mContext, null);
        mBubbleStyle.initBubbleStyle(mMainPanel);
        mContentContainer.addView(mMainPanel, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setContentView(mContentContainer);
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setBackgroundDrawable(new ColorDrawable(0));
        setOutsideTouchable(true);
        setFocusable(false);
        setClippingEnabled(false);
        super.setOnDismissListener(() -> {
            mBubbleStyle.dismissWindow();
            if (mUserDismissListener != null) {
                mUserDismissListener.onDismiss();
            }
        });
    }

    public void initPopupWindowBackground(Context context, TypedArray typedArray) {
        int[] defStyle = mBubbleStyle.getDefStyleParams();
        TypedArray a = typedArray != null
                ? typedArray
                : context.obtainStyledAttributes(null, R.styleable.COUIToolTips, defStyle[0], defStyle[1]);
        Drawable background = a.getDrawable(R.styleable.COUIToolTips_couiToolTipsBackground);
        ColorStateList backgroundColor = a.getColorStateList(R.styleable.COUIToolTips_couiToolTipsBackgroundColor);
        mArrowOverflow = a.getDimensionPixelOffset(R.styleable.COUIToolTips_couiToolTipsArrowOverflowOffset, 0);
        if (typedArray == null) {
            a.recycle();
        }
        if (background != null) {
            mMainPanel.setBackground(background);
        }
        if (backgroundColor != null) {
            mBackgroundColor = backgroundColor;
            mMainPanel.setBackgroundTintList(backgroundColor);
        }
    }

    public TextView getContentTv() {
        return mBubbleStyle.getContentView();
    }

    public ImageView getDismissIv() {
        return mBubbleStyle.getDismissIv();
    }

    public void hideDismissButton() {
        mBubbleStyle.hideDismissView();
    }

    public void setContent(CharSequence content) {
        mBubbleStyle.setContentText(content);
    }

    public void setContentRes(int resId) {
        mBubbleStyle.setContentTextRes(resId);
    }

    public void setContent(View view) {
        mBubbleStyle.setContentView(view);
    }

    @Deprecated
    public void setContentTextColor(@ColorInt int color) {
        setContentTextColor(ColorStateList.valueOf(color));
    }

    @Deprecated
    public void setContentTextColor(ColorStateList colorStateList) {
        mBubbleStyle.setContentTextColor(colorStateList);
    }

    public void setTitleRes(int resId) {
        mBubbleStyle.setTitleRes(resId);
    }

    public void setDismissTextRes(int resId) {
        mBubbleStyle.setDismissTextRes(resId);
    }

    public void setBackgroundColor(@ColorInt int color) {
        setBackgroundColor(ColorStateList.valueOf(color));
    }

    public void setBackgroundColor(ColorStateList colorStateList) {
        mBackgroundColor = colorStateList;
        if (mMainPanel != null) {
            mMainPanel.setBackgroundTintList(colorStateList);
        }
        if (mArrowView != null) {
            mArrowView.setImageTintList(colorStateList);
        }
        mBubbleStyle.refreshBubbleStyle(colorStateList);
    }

    @Deprecated
    public void setDelay(int delay) {
    }

    public void setDismissOnTouchOutside(boolean dismissOnTouchOutside) {
        setOutsideTouchable(dismissOnTouchOutside);
    }

    public void setArrowOverflow(int arrowOverflow) {
        mArrowOverflow = arrowOverflow;
    }

    public void setOnCloseIconClickListener(OnCloseIconClickListener listener) {
        mOnCloseIconClickListener = listener;
    }

    @Override
    public void setOnDismissListener(OnDismissListener onDismissListener) {
        mUserDismissListener = onDismissListener;
    }

    public boolean isLayoutRtl(View view) {
        return ViewCompat.getLayoutDirection(view) == ViewCompat.LAYOUT_DIRECTION_RTL;
    }

    public void show(View anchor) {
        show(anchor, true);
    }

    public void show(View anchor, boolean hasIndicator) {
        showWithDirection(anchor, DEFAULT_DIRECTION, hasIndicator);
    }

    public void showWithDirection(View anchor, int direction) {
        showWithDirection(anchor, direction, true);
    }

    public void showWithDirection(View anchor, int direction, boolean hasIndicator) {
        showWithDirection(anchor, direction, hasIndicator, 0, 0);
    }

    public void showWithDirection(View anchor, int direction, boolean hasIndicator, int offsetX, int offsetY) {
        showWithDirection(anchor, direction, hasIndicator, offsetX, offsetY, true);
    }

    public void showWithDirection(View anchor, int direction, boolean hasIndicator, int offsetX, int offsetY, boolean dismissWhenTouchOutside) {
        if (anchor == null) {
            return;
        }
        if (isShowing()) {
            dismissImmediately();
        }
        mAnchor = anchor;
        mShowDirection = resolveDirection(anchor, direction);
        mOffsetX = offsetX;
        mOffsetY = offsetY;
        setOutsideTouchable(dismissWhenTouchOutside);
        prepareContent(hasIndicator);
        mContentContainer.measure(
                View.MeasureSpec.makeMeasureSpec(getScreenWidth(anchor), View.MeasureSpec.AT_MOST),
                View.MeasureSpec.makeMeasureSpec(getScreenHeight(anchor), View.MeasureSpec.AT_MOST));
        int width = mBubbleStyle.getRealWidth(
                Math.min(mBubbleStyle.getMaxWidth(), mContentContainer.getMeasuredWidth()),
                mMainPanel);
        if (width > 0) {
            mBubbleStyle.sizeBubbleStyle(mMainPanel, width);
        }
        int[] location = new int[2];
        anchor.getLocationOnScreen(location);
        mAnchorRect.set(location[0], location[1], location[0] + anchor.getWidth(), location[1] + anchor.getHeight());
        int popupWidth = Math.max(mContentContainer.getMeasuredWidth(), width);
        int popupHeight = mContentContainer.getMeasuredHeight();
        int x = calculateX(anchor, popupWidth);
        int y = calculateY(anchor, popupHeight);
        View parent = getRootView(anchor);
        showAtLocation(parent, Gravity.NO_GRAVITY, x, y);
        mContentContainer.setAlpha(0f);
        mContentContainer.setScaleX(0.92f);
        mContentContainer.setScaleY(0.92f);
        mContentContainer.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(ANIMATION_DURATION).start();
    }

    public void refresh() {
        mBubbleStyle.refreshTextResources();
        if (mBackgroundColor != null) {
            setBackgroundColor(mBackgroundColor);
        }
        if (isShowing() && mAnchor != null) {
            showWithDirection(mAnchor, mShowDirection, mArrowView != null, mOffsetX, mOffsetY, isOutsideTouchable());
        }
    }

    public void refreshWhileLayoutChange() {
        refresh();
    }

    @Override
    public void dismiss() {
        if (!isShowing()) {
            return;
        }
        mContentContainer.animate()
                .alpha(0f)
                .scaleX(0.92f)
                .scaleY(0.92f)
                .setDuration(ANIMATION_EXIT_DURATION)
                .withEndAction(this::dismissImmediately)
                .start();
    }

    public void dismissImmediately() {
        if (mContentContainer != null) {
            mContentContainer.animate().cancel();
        }
        super.dismiss();
    }

    private void prepareContent(boolean hasIndicator) {
        if (mArrowView != null) {
            mContentContainer.removeView(mArrowView);
            mArrowView = null;
        }
        FrameLayout.LayoutParams panelLp = (FrameLayout.LayoutParams) mMainPanel.getLayoutParams();
        panelLp.gravity = Gravity.CENTER;
        mMainPanel.setLayoutParams(panelLp);
        if (hasIndicator) {
            mArrowView = new ImageView(mContext);
            mArrowView.setImageDrawable(getArrowDrawable(mShowDirection));
            if (mBackgroundColor != null) {
                mArrowView.setImageTintList(mBackgroundColor);
            }
            mContentContainer.addView(mArrowView, createArrowLayoutParams(mShowDirection));
        }
    }

    private FrameLayout.LayoutParams createArrowLayoutParams(int direction) {
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (direction == ALIGN_TOP || direction == DEFAULT_DIRECTION) {
            lp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
            lp.bottomMargin = mArrowOverflow;
            mArrowView.setRotation(180f);
        } else if (direction == ALIGN_BOTTOM) {
            lp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            lp.topMargin = mArrowOverflow;
        } else if (direction == ALIGN_LEFT) {
            lp.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
            lp.rightMargin = mArrowOverflow;
            mArrowView.setRotation(90f);
        } else {
            lp.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
            lp.leftMargin = mArrowOverflow;
            mArrowView.setRotation(270f);
        }
        return lp;
    }

    private Drawable getArrowDrawable(int direction) {
        int resId;
        if (direction == ALIGN_TOP || direction == DEFAULT_DIRECTION) {
            resId = R.drawable.coui_tool_tips_arrow_down;
        } else if (direction == ALIGN_BOTTOM) {
            resId = R.drawable.coui_tool_tips_arrow_up;
        } else if (direction == ALIGN_LEFT) {
            resId = R.drawable.coui_tool_tips_arrow_right;
        } else {
            resId = R.drawable.coui_tool_tips_arrow_left;
        }
        return ContextCompat.getDrawable(mContext, resId);
    }

    private int resolveDirection(View anchor, int direction) {
        if (direction == ALIGN_START) {
            return isLayoutRtl(anchor) ? ALIGN_RIGHT : ALIGN_LEFT;
        }
        if (direction == ALIGN_END) {
            return isLayoutRtl(anchor) ? ALIGN_LEFT : ALIGN_RIGHT;
        }
        return direction;
    }

    private int calculateX(View anchor, int popupWidth) {
        int screenWidth = getScreenWidth(anchor);
        int x;
        if (mShowDirection == ALIGN_LEFT) {
            x = mAnchorRect.left - popupWidth;
        } else if (mShowDirection == ALIGN_RIGHT) {
            x = mAnchorRect.right;
        } else {
            x = mAnchorRect.centerX() - popupWidth / 2;
        }
        x += mOffsetX;
        return Math.max(0, Math.min(x, Math.max(0, screenWidth - popupWidth)));
    }

    private int calculateY(View anchor, int popupHeight) {
        int screenHeight = getScreenHeight(anchor);
        int y;
        if (mShowDirection == ALIGN_BOTTOM) {
            y = mAnchorRect.bottom;
        } else if (mShowDirection == ALIGN_LEFT || mShowDirection == ALIGN_RIGHT) {
            y = mAnchorRect.centerY() - popupHeight / 2;
        } else {
            y = mAnchorRect.top - popupHeight;
        }
        y += mOffsetY;
        return Math.max(0, Math.min(y, Math.max(0, screenHeight - popupHeight)));
    }

    private int getScreenWidth(View view) {
        return view.getResources().getDisplayMetrics().widthPixels;
    }

    private int getScreenHeight(View view) {
        return view.getResources().getDisplayMetrics().heightPixels;
    }

    private View getRootView(@NonNull View anchor) {
        Context context = anchor.getContext();
        if (context instanceof Activity) {
            View decor = ((Activity) context).getWindow().getDecorView();
            if (decor != null) {
                return decor;
            }
        }
        return anchor.getRootView();
    }
}
