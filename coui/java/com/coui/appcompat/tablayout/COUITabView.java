package com.coui.appcompat.tablayout;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.widget.TooltipCompat;
import androidx.core.view.ViewCompat;
import androidx.viewpager.widget.ViewPager;
import com.coui.appcompat.accessibilityutil.COUIAccessibilityUtil;
import com.coui.appcompat.darkmode.COUIDarkModeUtil;
import com.coui.appcompat.hapticfeedback.COUIHapticFeedbackConstants;
import com.coui.appcompat.reddot.COUIHintRedDot;
import com.coui.appcompat.reddot.COUIHintRedDotMemento;
import com.coui.appcompat.state.COUIMaskEffectDrawable;
import com.coui.appcompat.state.COUIStateEffectDrawable;
import com.coui.appcompat.textutil.COUIChangeTextUtil;
import androidx.core.content.res.ResourcesCompat;
import com.coui.appcompat.R;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import com.coui.appcompat.R;


public class COUITabView extends LinearLayout {
    private COUITabLayout mCOUITabLayout;
    private ImageView mCustomIconView;
    private TextView mCustomTextView;
    protected View mCustomView;
    private int mDefaultMaxLines;
    private COUIHintRedDot mHintRedDot;
    private ImageView mIconView;
    private COUIMaskEffectDrawable mMaskEffectDrawable;
    private int mNoticeWithNumberDescriptionId;
    private boolean mSelectedByClick;
    private COUIStateEffectDrawable mStateEffectBackground;
    private COUITab mTab;
    private final RectF mTabRect;
    private TextView mTextFrame;

    public COUITabView(Context context, COUITabLayout cOUITabLayout) {
        super(context);
        this.mTabRect = new RectF();
        this.mDefaultMaxLines = 1;
        this.mCOUITabLayout = cOUITabLayout;
        if (cOUITabLayout.mTabBackgroundResId != 0) {
            ViewCompat.setBackground(this, ResourcesCompat.getDrawable(context.getResources(), this.mCOUITabLayout.mTabBackgroundResId, getContext().getTheme()));
        }
        ViewCompat.setPaddingRelative(this, this.mCOUITabLayout.getTabPaddingStart(), this.mCOUITabLayout.getTabPaddingTop(), this.mCOUITabLayout.getTabPaddingEnd(), this.mCOUITabLayout.getTabPaddingBottom());
        setGravity(17);
        setOrientation(0);
        setClickable(true);
        configStateEffectBackground();
        this.mNoticeWithNumberDescriptionId = R.plurals.red_dot_with_number_description;
    }

    private float approximateLineWidth(Layout layout, int index, float value) {
        return layout.getLineWidth(index) * (value / layout.getPaint().getTextSize());
    }

    private void configStateEffectBackground() {
        setDefaultFocusHighlightEnabled(false);
        COUIMaskEffectDrawable cOUIMaskEffectDrawable = new COUIMaskEffectDrawable(getContext(), 1);
        this.mMaskEffectDrawable = cOUIMaskEffectDrawable;
        cOUIMaskEffectDrawable.setMaskRect(this.mTabRect, dpToPx(8), dpToPx(8));
        this.mMaskEffectDrawable.enableSelectedState(false);
        this.mMaskEffectDrawable.setMinProgressForTouchEnterAnimation(0.0f);
        Drawable[] drawableArr = new Drawable[2];
        drawableArr[0] = getBackground() == null ? new ColorDrawable(0) : getBackground();
        drawableArr[1] = this.mMaskEffectDrawable;
        COUIStateEffectDrawable cOUIStateEffectDrawable = new COUIStateEffectDrawable(drawableArr);
        this.mStateEffectBackground = cOUIStateEffectDrawable;
        super.setBackground(cOUIStateEffectDrawable);
        COUIDarkModeUtil.setForceDarkAllow(this, false);
    }

    public void lambda$updateTextAndIcon$0() {
        COUITabLayout cOUITabLayout = this.mCOUITabLayout;
        cOUITabLayout.mTabAlreadyMeasure = false;
        cOUITabLayout.mTabStrip.requestLayout();
    }

    private static Integer stringToInteger(String str) {
        if (str != null && !str.trim().isEmpty()) {
            try {
                return Integer.valueOf(str.trim());
            } catch (Exception unused) {
            }
        }
        return null;
    }

    private void updateTextAndIcon(TextView textView, ImageView imageView) {
        COUITab cOUITab = this.mTab;
        Drawable icon = cOUITab != null ? cOUITab.getIcon() : null;
        COUITab cOUITab2 = this.mTab;
        CharSequence text = cOUITab2 != null ? cOUITab2.getText() : null;
        COUITab cOUITab3 = this.mTab;
        CharSequence contentDescription = cOUITab3 != null ? cOUITab3.getContentDescription() : null;
        int iDpToPx = 0;
        if (imageView != null) {
            if (icon != null) {
                imageView.setImageDrawable(icon);
                imageView.setVisibility(0);
                setVisibility(0);
            } else {
                imageView.setVisibility(8);
                imageView.setImageDrawable(null);
            }
            imageView.setContentDescription(contentDescription);
        }
        boolean isEmpty_2 = !TextUtils.isEmpty(text);
        if (textView != null) {
            if (isEmpty_2) {
                CharSequence text2 = textView.getText();
                textView.setText(text);
                textView.setVisibility(0);
                COUITabLayout cOUITabLayout = this.mCOUITabLayout;
                if (cOUITabLayout.mTabAlreadyMeasure) {
                    COUISlidingTabStrip cOUISlidingTabStrip = cOUITabLayout.mTabStrip;
                    if (cOUISlidingTabStrip != null) {
                        cOUITabLayout.mTabAlreadyMeasure = false;
                        cOUISlidingTabStrip.requestLayout();
                    }
                } else if (!text.equals(text2)) {
                    this.mCOUITabLayout.mTabStrip.post(new Runnable() {
                        @Override
                        public final void run() {
                            COUITabView.this.lambda$updateTextAndIcon$0();
                        }
                    });
                }
                textView.setMaxLines(this.mDefaultMaxLines);
                setVisibility(0);
            } else {
                textView.setVisibility(8);
                textView.setText((CharSequence) null);
            }
            textView.setContentDescription(contentDescription);
        }
        if (imageView != null) {
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) imageView.getLayoutParams();
            if (isEmpty_2 && imageView.getVisibility() == 0) {
                iDpToPx = dpToPx(8);
            }
            if (iDpToPx != marginLayoutParams.bottomMargin) {
                marginLayoutParams.bottomMargin = iDpToPx;
                imageView.requestLayout();
            }
        }
        TooltipCompat.setTooltipText(this, isEmpty_2 ? null : contentDescription);
    }

    public int dpToPx(int index) {
        return Math.round(getResources().getDisplayMetrics().density * index);
    }

    public COUIHintRedDot getHintRedDot() {
        return this.mHintRedDot;
    }

    public boolean getSelectedByClick() {
        return this.mSelectedByClick;
    }

    public COUITab getTab() {
        return this.mTab;
    }

    public TextView getTextView() {
        return this.mTextFrame;
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        super.onInitializeAccessibilityEvent(accessibilityEvent);
        accessibilityEvent.setClassName(ViewPager.class.getName());
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
        TextView textView;
        super.onInitializeAccessibilityNodeInfo(accessibilityNodeInfo);
        accessibilityNodeInfo.setClassName(ViewPager.class.getName());
        AccessibilityNodeInfoCompat hVarL0 = AccessibilityNodeInfoCompat.wrap(accessibilityNodeInfo);
        hVarL0.setCollectionItemInfo(AccessibilityNodeInfoCompat.CollectionItemInfoCompat.obtain(0, 1, this.mTab.getPosition(), 1, false, isSelected()));
        if (isSelected()) {
            hVarL0.setSelected(false);
            hVarL0.removeAction(AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_CLICK);
        }
        CharSequence contentDescription = this.mTab.getContentDescription();
        if (this.mCustomView == null && ((contentDescription == null || contentDescription == "") && (textView = this.mTextFrame) != null)) {
            contentDescription = textView.getText();
        }
        if (this.mHintRedDot.getPointMode() == 1 || this.mHintRedDot.getPointMode() == 4) {
            contentDescription = ((Object) contentDescription) + COUIAccessibilityUtil.PAUSE_STRING + getResources().getString(R.string.red_dot_description);
        }
        if (this.mHintRedDot.getPointMode() == 2 || this.mHintRedDot.getPointMode() == 5) {
            String quantityString = COUIAccessibilityUtil.PAUSE_STRING + getResources().getString(R.string.red_dot_description);
            Integer numStringToInteger = stringToInteger(this.mHintRedDot.getPointText());
            if (numStringToInteger != null) {
                quantityString = getResources().getQuantityString(this.mNoticeWithNumberDescriptionId, numStringToInteger.intValue(), numStringToInteger);
            }
            contentDescription = ((Object) contentDescription) + COUIAccessibilityUtil.PAUSE_STRING + quantityString;
        }
        accessibilityNodeInfo.setContentDescription(contentDescription);
        hVarL0.setRoleDescription(getResources().getString(R.string.item_view_role_description));
        if (isSelected()) {
            return;
        }
        accessibilityNodeInfo.setStateDescription(getContext().getResources().getString(R.string.coui_accessibility_unselected));
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.mTabRect.set(0.0f, 0.0f, w, h);
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (isEnabled()) {
            if (motionEvent.getAction() == 0) {
                COUITabLayout cOUITabLayout = this.mCOUITabLayout;
                if (cOUITabLayout.mEnableVibrator) {
                    COUITab cOUITab = cOUITabLayout.mSelectedTab;
                    if (cOUITab != null && cOUITab.mView != this) {
                        performHapticFeedback(COUIHapticFeedbackConstants.GRANULAR_SHORT_VIBRATE);
                    }
                    this.mStateEffectBackground.setTouched(true);
                }
            }
            if (motionEvent.getAction() == 1 || motionEvent.getAction() == 3) {
                this.mStateEffectBackground.setTouched(false);
            }
        }
        return super.onTouchEvent(motionEvent);
    }

    @Override
    public boolean performClick() {
        boolean zPerformClick = super.performClick();
        if (this.mTab == null) {
            return zPerformClick;
        }
        if (!zPerformClick) {
            playSoundEffect(0);
        }
        this.mCOUITabLayout.mNeedAdjust = false;
        this.mSelectedByClick = true;
        this.mTab.select();
        this.mSelectedByClick = false;
        return true;
    }

    public void refresh() {
        COUIStateEffectDrawable cOUIStateEffectDrawable = this.mStateEffectBackground;
        if (cOUIStateEffectDrawable != null) {
            cOUIStateEffectDrawable.refresh(getContext());
        }
    }

    public void reset() {
        setTab(null);
        setSelected(false);
    }

    @Override
    public void setBackground(Drawable drawable) {
        COUIStateEffectDrawable cOUIStateEffectDrawable = this.mStateEffectBackground;
        if (cOUIStateEffectDrawable == null) {
            super.setBackground(drawable);
        } else if (drawable == null) {
            cOUIStateEffectDrawable.setViewBackground(new ColorDrawable(0));
        } else {
            cOUIStateEffectDrawable.setViewBackground(drawable);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        TextView textView = this.mTextFrame;
        if (textView != null) {
            textView.setEnabled(enabled);
        }
        ImageView imageView = this.mIconView;
        if (imageView != null) {
            imageView.setEnabled(enabled);
        }
        View view = this.mCustomView;
        if (view != null) {
            view.setEnabled(enabled);
        }
    }

    @Override
    public void setSelected(boolean selected) {
        TextView textView;
        boolean flag = isSelected() != selected;
        super.setSelected(selected);
        if (flag && (textView = this.mTextFrame) != null) {
            if (selected) {
                textView.setTypeface(this.mCOUITabLayout.mSelectedTypeface);
            } else {
                textView.setTypeface(this.mCOUITabLayout.mNormalTypeface);
            }
        }
        TextView textView2 = this.mTextFrame;
        if (textView2 != null) {
            COUIDarkModeUtil.setForceDarkAllow(textView2, !selected);
        }
        TextView textView3 = this.mTextFrame;
        if (textView3 != null) {
            textView3.setSelected(selected);
        }
        ImageView imageView = this.mIconView;
        if (imageView != null) {
            imageView.setSelected(selected);
        }
        View view = this.mCustomView;
        if (view != null) {
            view.setSelected(selected);
        }
    }

    public void setTab(COUITab cOUITab) {
        if (cOUITab != this.mTab) {
            this.mTab = cOUITab;
            update();
        }
    }

    public final void update() {
        COUITab cOUITab = this.mTab;
        COUIHintRedDotMemento cOUIHintRedDotMementoSaveMemento = null;
        View customView = cOUITab != null ? cOUITab.getCustomView() : null;
        boolean flag = false;
        if (customView != null) {
            ViewParent parent = customView.getParent();
            if (parent != this) {
                if (parent != null) {
                    ((ViewGroup) parent).removeView(customView);
                }
                addView(customView, 0, new ViewGroup.LayoutParams(-2, -2));
            }
            this.mCustomView = customView;
            TextView textView = this.mTextFrame;
            if (textView != null) {
                textView.setVisibility(8);
            }
            ImageView imageView = this.mIconView;
            if (imageView != null) {
                imageView.setVisibility(8);
                this.mIconView.setImageDrawable(null);
            }
            TextView textView2 = (TextView) customView.findViewById(android.R.id.text1);
            this.mCustomTextView = textView2;
            if (textView2 != null) {
                this.mDefaultMaxLines = textView2.getMaxLines();
            }
            this.mCustomIconView = (ImageView) customView.findViewById(android.R.id.icon);
        } else {
            View view = this.mCustomView;
            if (view != null) {
                removeView(view);
                this.mCustomView = null;
            }
            this.mCustomTextView = null;
            this.mCustomIconView = null;
        }
        if (this.mCustomView == null) {
            if (this.mIconView == null) {
                ImageView imageView2 = (ImageView) LayoutInflater.from(getContext()).inflate(R.layout.coui_tab_layout_icon, (ViewGroup) this, false);
                addView(imageView2, 0);
                this.mIconView = imageView2;
            }
            if (this.mTextFrame == null) {
                TextView textView3 = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.coui_tab_layout_text, (ViewGroup) this, false);
                this.mTextFrame = textView3;
                addView(textView3);
                TextView textView4 = this.mTextFrame;
                COUITabLayout cOUITabLayout = this.mCOUITabLayout;
                ViewCompat.setPaddingRelative(textView4, cOUITabLayout.mTabPaddingStart, cOUITabLayout.mTabPaddingTop, cOUITabLayout.mTabPaddingEnd, cOUITabLayout.mTabPaddingBottom);
                this.mDefaultMaxLines = this.mTextFrame.getMaxLines();
                COUIChangeTextUtil.adaptBoldAndMediumFont(this.mTextFrame, cOUITab != null && cOUITab.isSelected());
            }
            COUIHintRedDot cOUIHintRedDot = this.mHintRedDot;
            if (cOUIHintRedDot != null) {
                cOUIHintRedDotMementoSaveMemento = cOUIHintRedDot.saveMemento();
                removeView(this.mHintRedDot);
            }
            this.mHintRedDot = new COUIHintRedDot(getContext());
            this.mHintRedDot.setLayoutParams(new LinearLayout.LayoutParams(-2, -2));
            addView(this.mHintRedDot);
            if (cOUIHintRedDotMementoSaveMemento != null) {
                cOUIHintRedDotMementoSaveMemento.applyTo(this.mHintRedDot);
            }
            this.mTextFrame.setTextSize(0, this.mCOUITabLayout.getTabTextSize());
            if (cOUITab == null || !cOUITab.isSelected()) {
                this.mTextFrame.setTypeface(this.mCOUITabLayout.mNormalTypeface);
            } else {
                this.mTextFrame.setTypeface(this.mCOUITabLayout.mSelectedTypeface);
            }
            this.mTextFrame.setIncludeFontPadding(false);
            ColorStateList colorStateList = this.mCOUITabLayout.mTabTextColors;
            if (colorStateList != null) {
                this.mTextFrame.setTextColor(colorStateList);
            }
            updateTextAndIcon(this.mTextFrame, this.mIconView);
        } else {
            if (this.mTextFrame == null) {
                this.mTextFrame = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.coui_tab_layout_text, (ViewGroup) this, false);
            }
            TextView textView5 = this.mCustomTextView;
            if (textView5 != null || this.mCustomIconView != null) {
                updateTextAndIcon(textView5, this.mCustomIconView);
            }
        }
        if (cOUITab != null && cOUITab.isSelected()) {
            flag = true;
        }
        setSelected(flag);
    }
}







