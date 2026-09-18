package com.coui.appcompat.panel;

//noinspection SuspiciousImport
import android.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatImageView;
import com.coui.appcompat.buttonBar.COUIButtonBarLayout;
import com.coui.appcompat.poplist.PopupMenuConfigRule;

public class COUIPanelContentLayout extends LinearLayout implements PopupMenuConfigRule {
    private static final Rect PANEL_OUTSETS = new Rect();
    private COUIPanelPressHelper mCOUIPanelPressHelper;
    private Rect mDisplayFrame;
    private boolean mIsLayoutAtMaxHeight;
    public boolean mIsTurnOnAnim;
    private int mPaddingBottomTemp;
    private final int mPanelHorizontalPadding;
    private boolean mPopupRuleEnable;

    public COUIPanelContentLayout(Context context) {
        this(context, null);
    }

    private int getNavigationBarHeight(WindowInsets windowInsets, Configuration configuration) {
        if (windowInsets != null) {
            return windowInsets.getInsets(WindowInsets.Type.navigationBars()).bottom;
        }
        int identifier = getContext().getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        return configuration != null ? getContext().createConfigurationContext(configuration).getResources().getDimensionPixelSize(identifier) : getContext().getResources().getDimensionPixelSize(identifier);
    }

    private void initButton(Button button, String text, View.OnClickListener onClickListener) {
        if (button != null) {
            if (TextUtils.isEmpty(text)) {
                button.setVisibility(View.GONE);
                return;
            }
            button.setVisibility(View.VISIBLE);
            button.setText(text);
            button.setOnClickListener(onClickListener);
        }
    }


    public boolean onDragViewPressTouch(View dragPressBgView, boolean enablePressAnim, View touchedView, MotionEvent motionEvent) {
        if (motionEvent.getAction() == 0) {
            if (dragPressBgView != null) {
                dragPressBgView.setVisibility(View.VISIBLE);
            }
            if (enablePressAnim) {
                this.mIsTurnOnAnim = true;
                this.mCOUIPanelPressHelper.startAnim(dragPressBgView);
            }
        }
        return true;
    }

    private void setSpecifyViewPaddingButton(boolean shouldSetPaddingButton, int bottomPadding) {
        View paddingTarget = getRootView().findViewById(com.coui.appcompat.R.id.coui_need_set_paddingbottom_id);
        if (paddingTarget != null) {
            if (!shouldSetPaddingButton) {
                if (this.mPaddingBottomTemp != -1) {
                    paddingTarget.setPadding(paddingTarget.getPaddingStart(), paddingTarget.getPaddingTop(), paddingTarget.getPaddingEnd(), this.mPaddingBottomTemp);
                    this.mPaddingBottomTemp = -1;
                    return;
                }
                return;
            }
            if (bottomPadding > 0) {
                if (this.mPaddingBottomTemp == -1) {
                    this.mPaddingBottomTemp = paddingTarget.getPaddingBottom();
                    paddingTarget.setPadding(paddingTarget.getPaddingStart(), paddingTarget.getPaddingTop(), paddingTarget.getPaddingEnd(), bottomPadding + this.mPaddingBottomTemp);
                    return;
                }
                return;
            }
            if (this.mPaddingBottomTemp != -1) {
                paddingTarget.setPadding(paddingTarget.getPaddingStart(), paddingTarget.getPaddingTop(), paddingTarget.getPaddingEnd(), this.mPaddingBottomTemp);
                this.mPaddingBottomTemp = -1;
            }
        }
    }

    public void addContentView(View contentView) {
        LinearLayout panelContent;
        if (contentView == null || (panelContent = (LinearLayout) findViewById(com.coui.appcompat.R.id.panel_content)) == null) {
            return;
        }
        panelContent.setClipChildren(false);
        panelContent.addView(contentView, new LinearLayout.LayoutParams(-1, -1));
    }

    public void dragBgEndAnim() {
        this.mCOUIPanelPressHelper.endAnim(findViewById(com.coui.appcompat.R.id.tv_drag_press_bg));
    }

    @Override
    public int getBarrierDirection() {
        return 4;
    }

    public COUIButtonBarLayout getBtnBarLayout() {
        return (COUIButtonBarLayout) findViewById(com.coui.appcompat.R.id.bottom_bar);
    }

    @Override
    public Rect getDisplayFrame() {
        if (this.mDisplayFrame == null) {
            this.mDisplayFrame = new Rect();
        }
        getGlobalVisibleRect(this.mDisplayFrame);
        Rect displayFrame = this.mDisplayFrame;
        int left = displayFrame.left;
        int horizontalPadding = this.mPanelHorizontalPadding;
        displayFrame.left = left + horizontalPadding;
        displayFrame.right -= horizontalPadding;
        return displayFrame;
    }

    public View getDivider() {
        return findViewById(com.coui.appcompat.R.id.divider_line);
    }

    public View getDragBgView() {
        return findViewById(com.coui.appcompat.R.id.tv_drag_press_bg);
    }

    public ImageView getDragView() {
        return (ImageView) findViewById(com.coui.appcompat.R.id.drag_img);
    }

    public FrameLayout getDrawLayout() {
        return (FrameLayout) findViewById(com.coui.appcompat.R.id.drag_layout);
    }

    public boolean getLayoutAtMaxHeight() {
        return this.mIsLayoutAtMaxHeight;
    }

    public int getMaxHeight() {
        return COUIPanelMultiWindowUtils.getPanelMaxHeight(getContext(), null);
    }

    @Override
    public Rect getOutsets() {
        return PANEL_OUTSETS;
    }

    public COUIPanelBarView getPanelBarView() {
        return (COUIPanelBarView) findViewById(com.coui.appcompat.R.id.panel_drag_bar);
    }

    @Override
    public boolean getPopupMenuRuleEnabled() {
        return this.mPopupRuleEnable;
    }

    @Override
    public int getType() {
        return 2;
    }

    public void refresh() {
        findViewById(com.coui.appcompat.R.id.tv_drag_press_bg).setBackground(AppCompatResources.getDrawable(getContext(), com.coui.appcompat.R.drawable.coui_pannel_press_shadow_bg));
    }

    public void removeContentView() {
        ((LinearLayout) findViewById(com.coui.appcompat.R.id.panel_content)).removeAllViews();
    }

    @SuppressLint({"ClickableViewAccessibility"})
    public void removeDragViewPressAnim() {
        View dragPressBgView = findViewById(com.coui.appcompat.R.id.tv_drag_press_bg);
        if (dragPressBgView != null) {
            dragPressBgView.setOnTouchListener(null);
        }
    }

    public void setCenterButton(String text, View.OnClickListener onClickListener) {
        initButton((Button) findViewById(R.id.button3), text, onClickListener);
    }

    public void setDividerVisibility(boolean visible) {
        View divider = findViewById(com.coui.appcompat.R.id.divider_line);
        if (visible) {
            divider.setVisibility(View.VISIBLE);
        } else {
            divider.setVisibility(View.GONE);
        }
    }

    public void setDragViewDrawable(Drawable drawable) {
        if (drawable != null) {
            ((ImageView) findViewById(com.coui.appcompat.R.id.drag_img)).setImageDrawable(drawable);
        }
    }

    public void setDragViewDrawableTintColor(int tintColor) {
        ((AppCompatImageView) findViewById(com.coui.appcompat.R.id.drag_img)).getDrawable().setTint(tintColor);
    }

    @SuppressLint({"ClickableViewAccessibility"})
    public void setDragViewPressAnim(final boolean enablePressAnim) {
        final View dragPressBgView = findViewById(com.coui.appcompat.R.id.tv_drag_press_bg);
        if (dragPressBgView != null) {
            dragPressBgView.setOnTouchListener(null);
            dragPressBgView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public final boolean onTouch(View view, MotionEvent motionEvent) {
                    return COUIPanelContentLayout.this.onDragViewPressTouch(dragPressBgView, enablePressAnim, view, motionEvent);
                }
            });
        }
    }

    public void setLayoutAtMaxHeight(boolean layoutAtMaxHeight) {
        this.mIsLayoutAtMaxHeight = layoutAtMaxHeight;
        ViewGroup.LayoutParams params = getLayoutParams();
        if (params != null) {
            if (layoutAtMaxHeight) {
                params.height = -1;
            } else {
                params.height = -2;
            }
            requestLayout();
        }
    }

    public void setLeftButton(String text, View.OnClickListener onClickListener) {
        initButton((Button) findViewById(R.id.button2), text, onClickListener);
    }

    @Deprecated
    public void setNavigationMargin(Configuration configuration, int navigationBarHeight, WindowInsets windowInsets) {
        setNavigationMargin(configuration, windowInsets, true, false);
    }

    @Override
    public void setPopupMenuRuleEnabled(boolean enabled) {
        this.mPopupRuleEnable = enabled;
    }

    public void setRightButton(String text, View.OnClickListener onClickListener) {
        initButton((Button) findViewById(R.id.button1), text, onClickListener);
    }

    public void setUpBottomBar(boolean showDivider, String leftText, View.OnClickListener leftClickListener, String centerText, View.OnClickListener centerClickListener, String rightText, View.OnClickListener rightClickListener) {
        setDividerVisibility(showDivider);
        COUIButtonBarLayout buttonBarLayout = (COUIButtonBarLayout) findViewById(com.coui.appcompat.R.id.bottom_bar);
        if (TextUtils.isEmpty(leftText) && TextUtils.isEmpty(centerText) && TextUtils.isEmpty(rightText)) {
            buttonBarLayout.setVisibility(View.GONE);
            return;
        }
        buttonBarLayout.setVisibility(View.VISIBLE);
        buttonBarLayout.setVerButDividerVerMargin(getContext().getResources().getDimensionPixelOffset(com.coui.appcompat.R.dimen.coui_panel_bottom_bar_padding_top));
        buttonBarLayout.setVerButVerPadding(getContext().getResources().getDimensionPixelOffset(com.coui.appcompat.R.dimen.coui_panel_bottom_button_vertical_padding));
        buttonBarLayout.setVerPaddingBottom(getContext().getResources().getDimensionPixelOffset(com.coui.appcompat.R.dimen.coui_panel_bottom_bar_padding_bottom));
        buttonBarLayout.setVerButPaddingOffset(0);
        Button button = (Button) findViewById(R.id.button2);
        Button button2 = (Button) findViewById(R.id.button3);
        Button button3 = (Button) findViewById(R.id.button1);
        initButton(button, leftText, leftClickListener);
        initButton(button2, centerText, centerClickListener);
        initButton(button3, rightText, rightClickListener);
    }

    public COUIPanelContentLayout(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public void setNavigationMargin(Configuration configuration, WindowInsets windowInsets, boolean shouldSetPaddingButton, boolean applyPaddingWhenNotGestureNavigation) {
        if (Build.VERSION.SDK_INT <= 30) {
            return;
        }
        boolean isDisplayInUpperWindow = COUIPanelMultiWindowUtils.isDisplayInUpperWindow(COUIPanelMultiWindowUtils.contextToActivity(getContext()));
        boolean isInMultiWindowMode = COUIPanelMultiWindowUtils.isInMultiWindowMode(COUIPanelMultiWindowUtils.contextToActivity(getContext()));
        boolean isNotSmallScreen = !COUIPanelMultiWindowUtils.isSmallScreen(getContext(), null);
        int navigationBarHeight = getNavigationBarHeight(windowInsets, configuration);
        View coordinator = getRootView().findViewById(com.coui.appcompat.R.id.coordinator);
        boolean isHandlePanel = false;
        if (coordinator != null) {
            View designBottomSheet = coordinator.findViewById(com.coui.appcompat.R.id.design_bottom_sheet);
            if (designBottomSheet instanceof COUIPanelPercentFrameLayout) {
                isHandlePanel = ((COUIPanelPercentFrameLayout) designBottomSheet).isIsHandlePanel();
            }
        }
        int coordinatorBottomMargin = navigationBarHeight;
        int paddingButtonBottom = 0;
        if ((isDisplayInUpperWindow && isInMultiWindowMode) || !isNotSmallScreen || isHandlePanel) {
            coordinatorBottomMargin = 0;
            paddingButtonBottom = navigationBarHeight;
        }
        if (COUINavigationBarUtil.isGestureNavigation(getContext())) {
            COUIViewMarginUtil.setMargin(coordinator, COUIViewMarginUtil.DIRECTION_BOTTOM, coordinatorBottomMargin);
            setSpecifyViewPaddingButton(shouldSetPaddingButton, paddingButtonBottom);
        } else if (applyPaddingWhenNotGestureNavigation) {
            setSpecifyViewPaddingButton(shouldSetPaddingButton, paddingButtonBottom);
        }
    }

    public COUIPanelContentLayout(Context context, AttributeSet attributeSet, int defStyleAttr) {
        super(context, attributeSet, defStyleAttr);
        this.mPopupRuleEnable = true;
        this.mCOUIPanelPressHelper = new COUIPanelPressHelper();
        this.mPanelHorizontalPadding = context.getResources().getDimensionPixelOffset(com.coui.appcompat.R.dimen.coui_bottom_sheet_content_horizontal_padding_with_card);
        this.mPaddingBottomTemp = -1;
    }
}
