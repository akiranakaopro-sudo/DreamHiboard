package com.coui.appcompat.chip;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.IdRes;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.dynamicanimation.animation.FloatPropertyCompat;

import com.coui.appcompat.R;
import com.coui.appcompat.animation.dynamicanimation.COUIDynamicAnimation;
import com.coui.appcompat.animation.dynamicanimation.COUISpringAnimation;
import com.coui.appcompat.animation.dynamicanimation.COUISpringForce;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Wrapping COUI chip container with selection and two-row collapse support.
 *
 * <p>Its XML attributes, public selection API, spacing, RTL layout and collapse affordance match
 * the decoded COUI implementation used by OPPO Settings.</p>
 */
public class COUIChipGroup extends ViewGroup {
    private static final float HEIGHT_CHANGE_ANIMATION_RESPONSE = 0.35f;
    private static final float HEIGHT_CHANGE_ANIMATION_BOUNCE = 0.0f;
    public interface ChipGroupLayoutAnimationCallback {
        default void onAnimatingHeightUpdate(
                int animatingHeight, int finalHeight, int suggestedTranslationY) {
        }

        default void onAnimatingWidthUpdate(
                int animatingWidth, int finalWidth, int suggestedTranslationX) {
        }
    }

    /**
     * Animation contract shared by chips and the editable trailing child in the decoded COUI
     * implementation.
     */
    public interface IChipGroupAnimator {
        float DEFAULT_CHIP_ALPHA_SPRING_ANIMATION_RESPONSE = 0.2f;
        float DEFAULT_CHIP_SCALE_START = 0.8f;
        float DEFAULT_CHIP_SPRING_ANIMATION_BOUNCE = 0.0f;
        float DEFAULT_CHIP_SPRING_ANIMATION_RANGE = 10000.0f;
        float DEFAULT_CHIP_SPRING_ANIMATION_RESPONSE = 0.3f;

        void bindController(COUIChipGroup controller);
        void forceFinishAllAnimation();
        void getDrawingBounds(RectF outBounds);
        boolean isChipAnimationRunning();
        void resetChipGroupAnimations();
        void unbindController();
        void updateAttachState(boolean attached, boolean animate);
        void updateChipRealBounds(
                int left, int top, int right, int bottom, boolean animate);
    }

    public interface OnCheckedStateChangeListener {
        void onCheckedChanged(@NonNull COUIChipGroup group, @NonNull List<Integer> checkedIds);
    }

    public interface OnChipGroupCollapsableButtonClickListener {
        void onExpandButtonClicked();
        void onCollapseButtonClicked();
    }

    public static class LayoutParams extends MarginLayoutParams {
        int row = -1;

        public LayoutParams(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }
    }

    private final COUICheckableGroup<COUIChip> mCheckableGroup = new COUICheckableGroup<>();
    private final List<View> mVisibleChildren = new ArrayList<>();
    private final Set<View> mHiddenChildren = new HashSet<>();
    private int mChipSpacingHorizontal;
    private int mChipSpacingVertical;
    private int mCollapsedMaxRows = 3;
    private int mDefaultCheckedId = View.NO_ID;
    private int mRowCount;
    private boolean mSingleLine;
    private boolean mIsCollapsable;
    private boolean mIsCollapsed;
    private boolean mAddingInternalView;
    private OnCheckedStateChangeListener mCheckedStateListener;
    private OnChipGroupCollapsableButtonClickListener mCollapseListener;
    private ChipGroupLayoutAnimationCallback mLayoutAnimationCallback;
    private OnHierarchyChangeListener mExternalHierarchyListener;
    private final COUIChip mExpandChip;
    private final COUIChip mCollapseChip;
    private final COUISpringAnimation mGroupHeightChangeAnimation;
    private int mAnimatingHeight = -1;
    private int mFinalHeight = -1;
    private int mAnimationStartHeight = -1;
    private boolean mHeightAnimationPending;
    private boolean mEnableChipsAnimations = true;

    public COUIChipGroup(Context context) {
        this(context, null);
    }

    public COUIChipGroup(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.couiChipGroupStyle);
    }

    public COUIChipGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Widget_COUI_ChipGroup);
    }

    public COUIChipGroup(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.COUIChipGroup, defStyleAttr, defStyleRes);
        int spacing = a.getDimensionPixelSize(R.styleable.COUIChipGroup_chipSpacing, 0);
        mChipSpacingHorizontal = a.getDimensionPixelSize(
                R.styleable.COUIChipGroup_chipSpacingHorizontal, spacing);
        mChipSpacingVertical = a.getDimensionPixelSize(
                R.styleable.COUIChipGroup_chipSpacingVertical, spacing);
        mSingleLine = a.getBoolean(R.styleable.COUIChipGroup_singleLine, false);
        mCheckableGroup.setSingleSelection(
                a.getBoolean(R.styleable.COUIChipGroup_singleSelection, false));
        mCheckableGroup.setSelectionRequired(
                a.getBoolean(R.styleable.COUIChipGroup_selectionRequired, false));
        mDefaultCheckedId =
                a.getResourceId(R.styleable.COUIChipGroup_checkedChip, View.NO_ID);
        mIsCollapsable = a.getBoolean(R.styleable.COUIChipGroup_isCollapsable, false);
        mIsCollapsed = a.getBoolean(R.styleable.COUIChipGroup_isCollapsed, false);
        mCollapsedMaxRows = Math.max(1,
                a.getInteger(R.styleable.COUIChipGroup_collapsedMaxRows, 3));
        a.recycle();

        mCheckableGroup.setOnCheckedStateChangeListener(ids -> {
            if (mCheckedStateListener != null) {
                mCheckedStateListener.onCheckedChanged(
                        this, mCheckableGroup.getCheckedIdsSortedByChildOrder(this));
            }
        });
        mExpandChip = createCollapsableChip(false);
        mCollapseChip = createCollapsableChip(true);
        final FloatPropertyCompat<COUIChipGroup> heightProperty =
                new FloatPropertyCompat<COUIChipGroup>("ChipGroupHeight") {
                    @Override
                    public float getValue(COUIChipGroup group) {
                        return group.mAnimatingHeight;
                    }

                    @Override
                    public void setValue(COUIChipGroup group, float value) {
                        group.setAnimatingHeight(value);
                    }
                };
        final COUISpringForce heightSpring = new COUISpringForce();
        heightSpring.setResponse(HEIGHT_CHANGE_ANIMATION_RESPONSE);
        heightSpring.setBounce(HEIGHT_CHANGE_ANIMATION_BOUNCE);
        mGroupHeightChangeAnimation = new COUISpringAnimation(this, heightProperty);
        mGroupHeightChangeAnimation.setSpring(heightSpring);
        mGroupHeightChangeAnimation.addEndListener(
                new COUIDynamicAnimation.OnAnimationEndListener() {
                    @Override
                    public void onAnimationEnd(
                            COUIDynamicAnimation animation, boolean canceled,
                            float value, float velocity) {
                        mAnimatingHeight = -1;
                        if (mLayoutAnimationCallback != null) {
                            mLayoutAnimationCallback.onAnimatingHeightUpdate(
                                    mFinalHeight, mFinalHeight, 0);
                        }
                        requestLayout();
                    }
                });
        mAddingInternalView = true;
        super.addView(mExpandChip, generateCollapsableChipLayoutParams());
        super.addView(mCollapseChip, generateCollapsableChipLayoutParams());
        mAddingInternalView = false;
        ViewCompat.setImportantForAccessibility(this, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);
        setClipChildren(false);
        setClipToPadding(false);
    }

    private COUIChip createCollapsableChip(boolean collapse) {
        COUIChip chip = new COUIChip(getContext(), null, R.attr.couiChipStyle,
                R.style.Widget_COUI_Chip_Suggestion_Collapsable);
        chip.setId(collapse
                ? R.id.coui_chip_group_collapse_button
                : R.id.coui_chip_group_expand_button);
        chip.setChipIconResource(collapse
                ? R.drawable.ic_chip_collapse
                : R.drawable.ic_chip_expand);
        chip.setChipIconVisible(true);
        chip.setOnClickListener(view -> {
            beginHeightAnimation();
            mIsCollapsed = !collapse;
            if (mCollapseListener != null) {
                if (collapse) {
                    mCollapseListener.onCollapseButtonClicked();
                } else {
                    mCollapseListener.onExpandButtonClicked();
                }
            }
            requestLayout();
        });
        chip.setVisibility(GONE);
        return chip;
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (!(child instanceof COUIChip) && !mAddingInternalView) {
            throw new IllegalArgumentException("COUIChipGroup children must be COUIChip views");
        }
        if (child instanceof COUIChip && child.getId() == View.NO_ID) {
            child.setId(ViewCompat.generateViewId());
        }
        super.addView(child, index, params);
    }

    @Override
    public void onViewAdded(View child) {
        super.onViewAdded(child);
        if (child instanceof COUIChip && child != mExpandChip && child != mCollapseChip) {
            mCheckableGroup.addCheckable((COUIChip) child);
        }
        if (mExternalHierarchyListener != null) {
            mExternalHierarchyListener.onChildViewAdded(this, child);
        }
    }

    @Override
    public void onViewRemoved(View child) {
        super.onViewRemoved(child);
        if (child instanceof COUIChip && child != mExpandChip && child != mCollapseChip) {
            mCheckableGroup.removeCheckable((COUIChip) child);
        }
        if (mExternalHierarchyListener != null) {
            mExternalHierarchyListener.onChildViewRemoved(this, child);
        }
    }

    @Override
    public void setOnHierarchyChangeListener(OnHierarchyChangeListener listener) {
        mExternalHierarchyListener = listener;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (mDefaultCheckedId != View.NO_ID) {
            check(mDefaultCheckedId);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int contentWidth = widthMode == MeasureSpec.UNSPECIFIED
                ? Integer.MAX_VALUE
                : Math.max(0, widthSize - getPaddingLeft() - getPaddingRight());
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child != mExpandChip && child != mCollapseChip && child.getVisibility() != GONE) {
                measureChip(child, widthMeasureSpec, heightMeasureSpec);
            }
        }
        // Force measurement of the normally-GONE internal buttons. The decoded group reserves
        // the real 28dp affordance width while deciding which chip ends row two.
        mExpandChip.forceLayout();
        mCollapseChip.forceLayout();
        measureChip(mExpandChip, widthMeasureSpec, heightMeasureSpec);
        measureChip(mCollapseChip, widthMeasureSpec, heightMeasureSpec);

        buildVisibleChildren(contentWidth);

        int lineWidth = 0;
        int lineHeight = 0;
        int maxLineWidth = 0;
        int totalHeight = 0;
        int row = 0;
        for (View child : mVisibleChildren) {
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            int childWidth = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            int childHeight = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
            boolean newLine = !mSingleLine && lineWidth > 0
                    && lineWidth + mChipSpacingHorizontal + childWidth > contentWidth;
            if (newLine) {
                maxLineWidth = Math.max(maxLineWidth, lineWidth);
                totalHeight += lineHeight + mChipSpacingVertical;
                lineWidth = 0;
                lineHeight = 0;
                row++;
            }
            if (lineWidth > 0) {
                lineWidth += mChipSpacingHorizontal;
            }
            lineWidth += childWidth;
            lineHeight = Math.max(lineHeight, childHeight);
            lp.row = row;
        }
        if (!mVisibleChildren.isEmpty()) {
            totalHeight += lineHeight;
            maxLineWidth = Math.max(maxLineWidth, lineWidth);
            mRowCount = row + 1;
        } else {
            mRowCount = 0;
        }
        int desiredWidth = maxLineWidth + getPaddingLeft() + getPaddingRight();
        int desiredHeight = totalHeight + getPaddingTop() + getPaddingBottom();
        final int resolvedWidth = resolveSize(desiredWidth, widthMeasureSpec);
        final int resolvedHeight = resolveSize(desiredHeight, heightMeasureSpec);
        mFinalHeight = resolvedHeight;
        if (mHeightAnimationPending) {
            final int startHeight = mAnimationStartHeight;
            mHeightAnimationPending = false;
            mAnimationStartHeight = -1;
            if (startHeight > 0 && startHeight != resolvedHeight) {
                mAnimatingHeight = startHeight;
                post(() -> {
                    mGroupHeightChangeAnimation.cancel();
                    mGroupHeightChangeAnimation.setStartValue(startHeight);
                    mGroupHeightChangeAnimation.animateToFinalPosition(mFinalHeight);
                });
            }
        }
        final int measuredHeight = mAnimatingHeight >= 0 ? mAnimatingHeight : resolvedHeight;
        setMeasuredDimension(resolvedWidth, measuredHeight);
    }

    private void measureChip(View child, int widthMeasureSpec, int heightMeasureSpec) {
        LayoutParams lp = (LayoutParams) child.getLayoutParams();
        int horizontalPadding = getPaddingLeft() + getPaddingRight()
                + lp.leftMargin + lp.rightMargin;
        int verticalPadding = getPaddingTop() + getPaddingBottom()
                + lp.topMargin + lp.bottomMargin;
        child.measure(getChildMeasureSpec(widthMeasureSpec, horizontalPadding, lp.width),
                getChildMeasureSpec(heightMeasureSpec, verticalPadding, lp.height));
    }

    private void buildVisibleChildren(int contentWidth) {
        mVisibleChildren.clear();
        mHiddenChildren.clear();
        mExpandChip.setVisibility(GONE);
        mCollapseChip.setVisibility(GONE);

        List<View> regular = new ArrayList<>();
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child != mExpandChip && child != mCollapseChip && child.getVisibility() != GONE) {
                regular.add(child);
            }
        }
        if (!mIsCollapsable || mSingleLine) {
            mVisibleChildren.addAll(regular);
            return;
        }

        int fullRows = countRows(regular, contentWidth);
        if (fullRows <= mCollapsedMaxRows) {
            mVisibleChildren.addAll(regular);
            return;
        }
        if (!mIsCollapsed) {
            mVisibleChildren.addAll(regular);
            mCollapseChip.setVisibility(VISIBLE);
            mVisibleChildren.add(mCollapseChip);
            return;
        }

        mExpandChip.setVisibility(VISIBLE);
        int row = 1;
        int lineWidth = 0;
        int expandWidth = measuredWidthWithMargins(mExpandChip);
        for (View child : regular) {
            int childWidth = measuredWidthWithMargins(child);
            int needed = lineWidth == 0 ? childWidth : lineWidth + mChipSpacingHorizontal + childWidth;
            int rowLimit = row == mCollapsedMaxRows
                    ? Math.max(0, contentWidth - expandWidth - mChipSpacingHorizontal)
                    : contentWidth;
            if (lineWidth > 0 && needed > rowLimit) {
                row++;
                lineWidth = 0;
                rowLimit = row == mCollapsedMaxRows
                        ? Math.max(0, contentWidth - expandWidth - mChipSpacingHorizontal)
                        : contentWidth;
            }
            if (row > mCollapsedMaxRows || childWidth > rowLimit && row == mCollapsedMaxRows) {
                mHiddenChildren.add(child);
                continue;
            }
            mVisibleChildren.add(child);
            lineWidth = lineWidth == 0
                    ? childWidth
                    : lineWidth + mChipSpacingHorizontal + childWidth;
        }
        mVisibleChildren.add(mExpandChip);
    }

    private int countRows(List<View> children, int contentWidth) {
        int rows = children.isEmpty() ? 0 : 1;
        int lineWidth = 0;
        for (View child : children) {
            int childWidth = measuredWidthWithMargins(child);
            if (lineWidth > 0
                    && lineWidth + mChipSpacingHorizontal + childWidth > contentWidth) {
                rows++;
                lineWidth = childWidth;
            } else {
                lineWidth = lineWidth == 0
                        ? childWidth : lineWidth + mChipSpacingHorizontal + childWidth;
            }
        }
        return rows;
    }

    private int measuredWidthWithMargins(View child) {
        LayoutParams lp = (LayoutParams) child.getLayoutParams();
        return child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        boolean rtl = getLayoutDirection() == LAYOUT_DIRECTION_RTL;
        int contentWidth = right - left - getPaddingLeft() - getPaddingRight();
        int x = 0;
        int y = getPaddingTop();
        int lineHeight = 0;
        for (View child : mVisibleChildren) {
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            int outerWidth = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            int outerHeight = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
            if (!mSingleLine && x > 0
                    && x + mChipSpacingHorizontal + outerWidth > contentWidth) {
                x = 0;
                y += lineHeight + mChipSpacingVertical;
                lineHeight = 0;
            }
            if (x > 0) {
                x += mChipSpacingHorizontal;
            }
            int logicalLeft = x + lp.leftMargin;
            int childLeft = rtl
                    ? right - left - getPaddingRight() - logicalLeft - child.getMeasuredWidth()
                    : getPaddingLeft() + logicalLeft;
            int childTop = y + lp.topMargin;
            child.layout(childLeft, childTop,
                    childLeft + child.getMeasuredWidth(), childTop + child.getMeasuredHeight());
            x += outerWidth;
            lineHeight = Math.max(lineHeight, outerHeight);
        }
        for (View child : mHiddenChildren) {
            child.layout(0, 0, 0, 0);
        }
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        super.draw(canvas);
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setCollectionInfo(AccessibilityNodeInfo.CollectionInfo.obtain(
                mRowCount, mSingleLine ? getChipCount() : -1, false,
                getCheckedChipIds().isEmpty()
                        ? AccessibilityNodeInfo.CollectionInfo.SELECTION_MODE_NONE
                        : (isSingleSelection()
                                ? AccessibilityNodeInfo.CollectionInfo.SELECTION_MODE_SINGLE
                                : AccessibilityNodeInfo.CollectionInfo.SELECTION_MODE_MULTIPLE)));
    }

    public void addChip(COUIChip chip) {
        addChip(chip, true);
    }

    public void addChip(COUIChip chip, boolean animate) {
        if (animate) beginHeightAnimation();
        addView(chip);
    }

    public void addChip(COUIChip chip, int index, boolean animate) {
        if (animate) beginHeightAnimation();
        addView(chip, index);
    }

    public void removeChip(COUIChip chip) {
        removeChip(chip, true);
    }

    public void removeChip(COUIChip chip, boolean animate) {
        if (animate) beginHeightAnimation();
        removeView(chip);
    }

    public void removeChip(int index) {
        removeChip(index, true);
    }

    public void removeChip(int index, boolean animate) {
        COUIChip chip = getChipAt(index);
        if (chip != null) {
            removeChip(chip, animate);
        }
    }

    public void removeAllChips() {
        removeAllChips(true);
    }

    public void removeAllChips(boolean animate) {
        if (animate && getChipCount() > 0) {
            beginHeightAnimation();
        }
        for (int i = getChildCount() - 1; i >= 0; i--) {
            View child = getChildAt(i);
            if (child != mExpandChip && child != mCollapseChip) {
                removeViewAt(i);
            }
        }
    }

    public int getChipCount() {
        int count = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child instanceof COUIChip && child != mExpandChip && child != mCollapseChip) {
                count++;
            }
        }
        return count;
    }

    @Nullable
    public COUIChip getChipAt(int index) {
        int chipIndex = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child instanceof COUIChip && child != mExpandChip && child != mCollapseChip) {
                if (chipIndex == index) {
                    return (COUIChip) child;
                }
                chipIndex++;
            }
        }
        return null;
    }

    public int indexOfChips(COUIChip chip) {
        for (int i = 0; i < getChipCount(); i++) {
            if (getChipAt(i) == chip) {
                return i;
            }
        }
        return -1;
    }

    public void check(@IdRes int id) {
        mCheckableGroup.check(id);
    }

    public void clearCheck() {
        mCheckableGroup.clearCheck();
    }

    @IdRes
    public int getCheckedChipId() {
        return mCheckableGroup.getSingleCheckedId();
    }

    @NonNull
    public List<Integer> getCheckedChipIds() {
        return mCheckableGroup.getCheckedIdsSortedByChildOrder(this);
    }

    public boolean isSelectionRequired() {
        return mCheckableGroup.isSelectionRequired();
    }

    public boolean isSingleSelection() {
        return mCheckableGroup.isSingleSelection();
    }

    public void setSelectionRequired(boolean required) {
        mCheckableGroup.setSelectionRequired(required);
    }

    public void setSingleSelection(boolean singleSelection) {
        mCheckableGroup.setSingleSelection(singleSelection);
    }

    public void setOnCheckedStateChangeListener(
            @Nullable OnCheckedStateChangeListener listener) {
        mCheckedStateListener = listener;
    }

    public int getChipSpacingHorizontal() {
        return mChipSpacingHorizontal;
    }

    public int getChipSpacingVertical() {
        return mChipSpacingVertical;
    }

    public void setChipSpacing(@Dimension int spacing) {
        setChipSpacingHorizontal(spacing);
        setChipSpacingVertical(spacing);
    }

    public void setChipSpacingHorizontal(@Dimension int spacing) {
        if (mChipSpacingHorizontal != spacing) {
            mChipSpacingHorizontal = spacing;
            requestLayout();
        }
    }

    public void setChipSpacingVertical(@Dimension int spacing) {
        if (mChipSpacingVertical != spacing) {
            mChipSpacingVertical = spacing;
            requestLayout();
        }
    }

    public void setChipSpacingResource(int resourceId) {
        setChipSpacing(getResources().getDimensionPixelOffset(resourceId));
    }

    public void setChipSpacingHorizontalResource(int resourceId) {
        setChipSpacingHorizontal(getResources().getDimensionPixelOffset(resourceId));
    }

    public void setChipSpacingVerticalResource(int resourceId) {
        setChipSpacingVertical(getResources().getDimensionPixelOffset(resourceId));
    }

    public boolean isSingleLine() {
        return mSingleLine;
    }

    public void setSingleLine(boolean singleLine) {
        if (mSingleLine != singleLine) {
            mSingleLine = singleLine;
            requestLayout();
        }
    }

    public boolean isCollapsable() {
        return mIsCollapsable;
    }

    public void setIsCollapsable(boolean collapsable) {
        if (mIsCollapsable != collapsable) {
            mIsCollapsable = collapsable;
            requestLayout();
        }
    }

    public boolean isCollapsed() {
        return mIsCollapsed;
    }

    public void setIsCollapsed(boolean collapsed) {
        if (mIsCollapsed != collapsed) {
            beginHeightAnimation();
            mIsCollapsed = collapsed;
            requestLayout();
        }
    }

    public int getCollapsedMaxRows() {
        return mCollapsedMaxRows;
    }

    public void setCollapsedMaxRows(@IntRange(from = 1) int rows) {
        if (rows < 1) {
            throw new IllegalArgumentException("collapsedMaxRows must be at least 1");
        }
        if (mCollapsedMaxRows != rows) {
            mCollapsedMaxRows = rows;
            requestLayout();
        }
    }

    public int getRowCount() {
        return mRowCount;
    }

    public int getRowIndex(@NonNull View child) {
        ViewGroup.LayoutParams params = child.getLayoutParams();
        return params instanceof LayoutParams ? ((LayoutParams) params).row : -1;
    }

    public boolean isChipHidden(COUIChip chip) {
        return mHiddenChildren.contains(chip);
    }

    public void setOnChipGroupCollapsableButtonClickListener(
            @Nullable OnChipGroupCollapsableButtonClickListener listener) {
        mCollapseListener = listener;
    }

    public void setChipGroupLayoutAnimationCallback(
            @Nullable ChipGroupLayoutAnimationCallback callback) {
        mLayoutAnimationCallback = callback;
    }

    public void setCollapsableButtonBackgroundColor(@ColorInt int color) {
        mExpandChip.setUncheckedBackgroundColor(color);
        mCollapseChip.setUncheckedBackgroundColor(color);
    }

    public void setCollapsableButtonIconTint(@ColorInt int color) {
        mExpandChip.setUncheckedChipIconTint(color);
        mCollapseChip.setUncheckedChipIconTint(color);
    }

    public void setChipGroupAnimationsEnabled(boolean enabled) {
        mEnableChipsAnimations = enabled;
        if (!enabled && mGroupHeightChangeAnimation.isRunning()) {
            mGroupHeightChangeAnimation.cancel();
            mAnimatingHeight = -1;
            requestLayout();
        }
    }

    private void beginHeightAnimation() {
        if (!mEnableChipsAnimations || !isLaidOut() || getHeight() <= 0) return;
        mAnimationStartHeight = getHeight();
        mHeightAnimationPending = true;
    }

    private void setAnimatingHeight(float height) {
        mAnimatingHeight = Math.round(height);
        if (mLayoutAnimationCallback != null) {
            mLayoutAnimationCallback.onAnimatingHeightUpdate(
                    mAnimatingHeight, mFinalHeight,
                    mAnimatingHeight - Math.max(mAnimatingHeight, getHeight()));
        }
        requestLayout();
    }

    public boolean isChipGroupAnimationsEnabled() {
        return mEnableChipsAnimations;
    }

    public ViewGroup.LayoutParams generateCollapsableChipLayoutParams() {
        int size = getResources().getDimensionPixelSize(
                R.dimen.coui_chip_group_collapsable_button_size);
        return new LayoutParams(size, size);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams params) {
        return params instanceof MarginLayoutParams
                ? new LayoutParams((MarginLayoutParams) params)
                : new LayoutParams(params);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams params) {
        return params instanceof LayoutParams;
    }
}
