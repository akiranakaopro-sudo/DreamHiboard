package com.coui.appcompat.panel;

import android.R;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowInsets;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.core.view.accessibility.AccessibilityViewCommand;
import androidx.customview.view.AbsSavedState;
import androidx.customview.widget.ViewDragHelper;
import com.coui.appcompat.view.MaterialResource;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class COUIGuideBehavior<V extends View> extends BottomSheetBehavior<V> {
    private static final int CORNER_ANIMATION_DURATION = 500;
    private static final int DEF_STYLE_RES = com.google.android.material.R.style.Widget_Design_BottomSheet_Modal;
    private static final float HIDE_FRICTION = 0.1f;
    private static final float HIDE_THRESHOLD = 0.5f;
    public static final int PEEK_HEIGHT_AUTO = -1;
    public static final int SAVE_ALL = -1;
    public static final int SAVE_FIT_TO_CONTENTS = 2;
    public static final int SAVE_HIDEABLE = 4;
    public static final int SAVE_NONE = 0;
    public static final int SAVE_PEEK_HEIGHT = 1;
    public static final int SAVE_SKIP_COLLAPSED = 8;
    private static final int SIGNIFICANT_VEL_THRESHOLD = 500;
    public static final int STATE_COLLAPSED = 4;
    public static final int STATE_DRAGGING = 1;
    public static final int STATE_EXPANDED = 3;
    public static final int STATE_HALF_EXPANDED = 6;
    public static final int STATE_HIDDEN = 5;
    public static final int STATE_SETTLING = 2;
    private static final String TAG = "BottomSheetBehavior";
    private static final int VERTICAL_SLIDING_PARAMETER_THRESHOLD = 2;
    int activePointerId;
    private final ArrayList<COUIBottomSheetCallback> callbacks;
    int collapsedOffset;
    private final ViewDragHelper.Callback dragCallback;
    private boolean draggable;
    float elevation;
    int expandedOffset;
    private boolean fitToContents;
    int fitToContentsOffset;
    private boolean gestureInsetBottomIgnored;
    int halfExpandedOffset;
    float halfExpandedRatio;
    boolean hideable;
    private boolean ignoreEvents;
    private Map<View, Integer> importantForAccessibilityMap;
    private int initialX;
    private int initialY;
    private ValueAnimator interpolatorAnimator;
    private boolean isShapeExpanded;
    private int lastNestedScrollDy;
    COUIPanelDragListener mCOUIPanelDragListener;
    private boolean mCanHideKeyboard;
    private boolean mIsIgnoreExpandedOffsetChange;
    private COUIPanelPullUpListener mPullUpListener;
    private MaterialShapeDrawable materialShapeDrawable;
    private float maximumVelocity;
    private boolean nestedScrolled;
    WeakReference<View> nestedScrollingChildRef;
    int parentHeight;
    int parentWidth;
    private int peekHeight;
    private boolean peekHeightAuto;
    private int peekHeightMin;
    private int saveFlags;
    private COUIGuideBehavior<V>.SettleRunnable settleRunnable;
    private ShapeAppearanceModel shapeAppearanceModelDefault;
    private boolean shapeThemingEnabled;
    private boolean skipCollapsed;
    int state;
    boolean touchingScrollingChild;
    private boolean updateImportantForAccessibilityOnSiblings;
    private VelocityTracker velocityTracker;
    ViewDragHelper viewDragHelper;
    WeakReference<V> viewRef;

    public static abstract class COUIBottomSheetCallback {
        public abstract void onSlide(View view, float slideOffset);

        public abstract void onStateChanged(View view, int newState);
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface SaveFlags {
    }

    public static class SavedState extends AbsSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.ClassLoaderCreator<SavedState>() {
            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }


            @Override
            public SavedState createFromParcel(Parcel parcel, ClassLoader classLoader) {
                return new SavedState(parcel, classLoader);
            }

            @Override
            public SavedState createFromParcel(Parcel parcel) {
                return new SavedState(parcel, (ClassLoader) null);
            }
        };
        boolean fitToContents;
        boolean hideable;
        int peekHeight;
        boolean skipCollapsed;
        final int state;

        public SavedState(Parcel parcel) {
            this(parcel, (ClassLoader) null);
        }

        @Override
        public void writeToParcel(Parcel parcel, int flags) {
            super.writeToParcel(parcel, flags);
            parcel.writeInt(this.state);
            parcel.writeInt(this.peekHeight);
            parcel.writeInt(this.fitToContents ? 1 : 0);
            parcel.writeInt(this.hideable ? 1 : 0);
            parcel.writeInt(this.skipCollapsed ? 1 : 0);
        }

        public SavedState(Parcel parcel, ClassLoader classLoader) {
            super(parcel, classLoader);
            this.state = parcel.readInt();
            this.peekHeight = parcel.readInt();
            this.fitToContents = parcel.readInt() == 1;
            this.hideable = parcel.readInt() == 1;
            this.skipCollapsed = parcel.readInt() == 1;
        }

        public SavedState(Parcelable parcelable, COUIGuideBehavior<?> behavior) {
            super(parcelable);
            this.state = behavior.state;
            this.peekHeight = ((COUIGuideBehavior) behavior).peekHeight;
            this.fitToContents = ((COUIGuideBehavior) behavior).fitToContents;
            this.hideable = behavior.hideable;
            this.skipCollapsed = ((COUIGuideBehavior) behavior).skipCollapsed;
        }

        @Deprecated
        public SavedState(Parcelable parcelable, int state) {
            super(parcelable);
            this.state = state;
        }
    }

    public class SettleRunnable implements Runnable {
        private boolean isPosted;
        int targetState;
        private final View view;

        public SettleRunnable(View view, int targetState) {
            this.view = view;
            this.targetState = targetState;
        }

        @Override
        public void run() {
            ViewDragHelper dragHelper = COUIGuideBehavior.this.viewDragHelper;
            if (dragHelper == null || !dragHelper.continueSettling(true)) {
                COUIGuideBehavior.this.setStateInternal(this.targetState);
            } else {
                ViewCompat.postOnAnimation(this.view, this);
            }
            this.isPosted = false;
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface State {
    }

    public COUIGuideBehavior() {
        this.saveFlags = 0;
        this.fitToContents = true;
        this.updateImportantForAccessibilityOnSiblings = false;
        this.settleRunnable = null;
        this.halfExpandedRatio = 0.5f;
        this.elevation = -1.0f;
        this.draggable = true;
        this.state = STATE_COLLAPSED;
        this.callbacks = new ArrayList<>();
        this.dragCallback = new ViewDragHelper.Callback() {
            private boolean releasedLow(View view) {
                int top = view.getTop();
                COUIGuideBehavior behavior = COUIGuideBehavior.this;
                return top > (behavior.parentHeight + behavior.getExpandedOffset()) / 2;
            }

            @Override
            public int clampViewPositionHorizontal(View view, int left, int dx) {
                return view.getLeft();
            }

            @Override
            public int clampViewPositionVertical(View view, int top, int dy) {
                int pullUpOffset;
                int currentState;
                if (COUIGuideBehavior.this.mPullUpListener == null || ((currentState = COUIGuideBehavior.this.state) != STATE_EXPANDED && (currentState != STATE_DRAGGING || view.getTop() > COUIGuideBehavior.this.getExpandedOffset()))) {
                    pullUpOffset = 0;
                } else {
                    COUIGuideBehavior.this.mIsIgnoreExpandedOffsetChange = true;
                    pullUpOffset = COUIGuideBehavior.this.mPullUpListener.onDragging(dy, COUIGuideBehavior.this.getExpandedOffset());
                }
                int expandedOffset = COUIGuideBehavior.this.getExpandedOffset() - pullUpOffset;
                COUIGuideBehavior behavior = COUIGuideBehavior.this;
                return androidx.core.math.MathUtils.clamp(top, expandedOffset, behavior.hideable ? behavior.parentHeight : behavior.collapsedOffset);
            }

            @Override
            public int getViewVerticalDragRange(View view) {
                COUIGuideBehavior behavior = COUIGuideBehavior.this;
                return behavior.hideable ? behavior.parentHeight : behavior.collapsedOffset;
            }

            @Override
            public void onViewDragStateChanged(int state) {
                if (state == STATE_DRAGGING && COUIGuideBehavior.this.draggable) {
                    COUIGuideBehavior.this.setStateInternal(STATE_DRAGGING);
                }
            }

            @Override
            public void onViewPositionChanged(View view, int left, int top, int dx, int dy) {
                COUIGuideBehavior.this.dispatchOnSlide(top);
            }

            @Override
            public void onViewReleased(View view, float xVelocity, float yVelocity) {
                int targetTop;
                if (COUIGuideBehavior.this.mPullUpListener != null && COUIGuideBehavior.this.parentHeight - view.getHeight() < COUIGuideBehavior.this.getExpandedOffset() && view.getTop() < COUIGuideBehavior.this.getExpandedOffset()) {
                    COUIGuideBehavior.this.mPullUpListener.onReleased(COUIGuideBehavior.this.getExpandedOffset());
                    return;
                }
                int targetState = 6;
                if (yVelocity < 0.0f) {
                    if (COUIGuideBehavior.this.fitToContents) {
                        targetTop = COUIGuideBehavior.this.fitToContentsOffset;
                    } else {
                        int top = view.getTop();
                        COUIGuideBehavior behavior = COUIGuideBehavior.this;
                        int halfExpandedOffset = behavior.halfExpandedOffset;
                        if (top > halfExpandedOffset) {
                            targetTop = halfExpandedOffset;
                        } else {
                            targetTop = behavior.expandedOffset;
                        }
                    }
                    targetState = 3;
                } else {
                    COUIGuideBehavior behavior = COUIGuideBehavior.this;
                    if (behavior.hideable && behavior.shouldHide(view, yVelocity)) {
                        COUIPanelDragListener dragListener = COUIGuideBehavior.this.mCOUIPanelDragListener;
                        if (dragListener != null && dragListener.onDragWhileEditing()) {
                            COUIGuideBehavior guideBehavior = COUIGuideBehavior.this;
                            int fitToContentsOffset = guideBehavior.fitToContentsOffset;
                            guideBehavior.mCanHideKeyboard = false;
                            targetTop = fitToContentsOffset;
                        } else if ((Math.abs(xVelocity) < Math.abs(yVelocity) && yVelocity > 500.0f) || releasedLow(view)) {
                            COUIGuideBehavior guideBehavior = COUIGuideBehavior.this;
                            int parentHeight = guideBehavior.parentHeight;
                            guideBehavior.mCanHideKeyboard = true;
                            targetState = 5;
                            targetTop = parentHeight;
                        } else if (COUIGuideBehavior.this.fitToContents) {
                            targetTop = COUIGuideBehavior.this.fitToContentsOffset;
                        } else if (Math.abs(view.getTop() - COUIGuideBehavior.this.expandedOffset) < Math.abs(view.getTop() - COUIGuideBehavior.this.halfExpandedOffset)) {
                            targetTop = COUIGuideBehavior.this.expandedOffset;
                        } else {
                            targetTop = COUIGuideBehavior.this.halfExpandedOffset;
                        }
                        targetState = 3;
                    } else if (yVelocity == 0.0f || Math.abs(xVelocity) > Math.abs(yVelocity)) {
                        int top2 = view.getTop();
                        if (!COUIGuideBehavior.this.fitToContents) {
                            COUIGuideBehavior guideBehavior = COUIGuideBehavior.this;
                            int halfExpandedOffset = guideBehavior.halfExpandedOffset;
                            if (top2 < halfExpandedOffset) {
                                if (top2 < Math.abs(top2 - guideBehavior.collapsedOffset)) {
                                    targetTop = COUIGuideBehavior.this.expandedOffset;
                                    targetState = 3;
                                } else {
                                    targetTop = COUIGuideBehavior.this.halfExpandedOffset;
                                }
                            } else if (Math.abs(top2 - halfExpandedOffset) < Math.abs(top2 - COUIGuideBehavior.this.collapsedOffset)) {
                                targetTop = COUIGuideBehavior.this.halfExpandedOffset;
                            } else {
                                targetTop = COUIGuideBehavior.this.collapsedOffset;
                                targetState = 4;
                            }
                        } else if (Math.abs(top2 - COUIGuideBehavior.this.fitToContentsOffset) < Math.abs(top2 - COUIGuideBehavior.this.collapsedOffset)) {
                            targetTop = COUIGuideBehavior.this.fitToContentsOffset;
                            targetState = 3;
                        } else {
                            targetTop = COUIGuideBehavior.this.collapsedOffset;
                            targetState = 4;
                        }
                    } else {
                        if (COUIGuideBehavior.this.fitToContents) {
                            targetTop = COUIGuideBehavior.this.collapsedOffset;
                        } else {
                            int top3 = view.getTop();
                            if (Math.abs(top3 - COUIGuideBehavior.this.halfExpandedOffset) < Math.abs(top3 - COUIGuideBehavior.this.collapsedOffset)) {
                                targetTop = COUIGuideBehavior.this.halfExpandedOffset;
                            } else {
                                targetTop = COUIGuideBehavior.this.collapsedOffset;
                            }
                        }
                        targetState = 4;
                    }
                }
                COUIGuideBehavior.this.startSettlingAnimation(view, targetState, targetTop, true);
            }

            @Override
            public boolean tryCaptureView(View view, int pointerId) {
                COUIGuideBehavior behavior = COUIGuideBehavior.this;
                int currentState = behavior.state;
                if (currentState == 1 || behavior.touchingScrollingChild) {
                    return false;
                }
                if (currentState == 3 && behavior.activePointerId == pointerId) {
                    WeakReference<View> weakReference = behavior.nestedScrollingChildRef;
                    View nestedChild = weakReference != null ? weakReference.get() : null;
                    if (nestedChild != null && nestedChild.canScrollVertically(-1)) {
                        return false;
                    }
                }
                WeakReference<V> sheetRef = COUIGuideBehavior.this.viewRef;
                return sheetRef != null && sheetRef.get() == view;
            }
        };
    }

    private void addAccessibilityActionForState(V child, AccessibilityNodeInfoCompat.AccessibilityActionCompat action, final int state) {
        ViewCompat.replaceAccessibilityAction(child, action, null, new AccessibilityViewCommand() {
            @Override
            public boolean perform(View view, AccessibilityViewCommand.CommandArguments commandArguments) {
                COUIGuideBehavior.this.setState(state);
                return true;
            }
        });
    }

    private void calculateCollapsedOffset() {
        int iCalculatePeekHeight = calculatePeekHeight();
        if (this.fitToContents) {
            this.collapsedOffset = Math.max(this.parentHeight - iCalculatePeekHeight, this.fitToContentsOffset);
        } else {
            this.collapsedOffset = this.parentHeight - iCalculatePeekHeight;
        }
    }

    private void calculateHalfExpandedOffset() {
        this.halfExpandedOffset = (int) (this.parentHeight * (1.0f - this.halfExpandedRatio));
    }

    private int calculatePeekHeight() {
        return this.peekHeightAuto ? Math.max(this.peekHeightMin, this.parentHeight - ((this.parentWidth * 9) / 16)) : this.peekHeight;
    }

    private void createMaterialShapeDrawable(Context context, AttributeSet attributeSet, boolean hasBackgroundTint) {
        createMaterialShapeDrawable(context, attributeSet, hasBackgroundTint, null);
    }

    private void createShapeValueAnimator() {
        ValueAnimator valueAnimatorOfFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        this.interpolatorAnimator = valueAnimatorOfFloat;
        valueAnimatorOfFloat.setDuration(CORNER_ANIMATION_DURATION);
        this.interpolatorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float interpolation = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                if (COUIGuideBehavior.this.materialShapeDrawable != null) {
                    COUIGuideBehavior.this.materialShapeDrawable.setInterpolation(interpolation);
                }
            }
        });
    }

    public static <V extends View> COUIGuideBehavior<V> from(V view) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (!(layoutParams instanceof CoordinatorLayout.LayoutParams)) {
            throw new IllegalArgumentException("The view is not a child of CoordinatorLayout");
        }
        CoordinatorLayout.Behavior cVarF = ((CoordinatorLayout.LayoutParams) layoutParams).getBehavior();
        if (cVarF instanceof BottomSheetBehavior) {
            return (COUIGuideBehavior) cVarF;
        }
        throw new IllegalArgumentException("The view is not associated with BottomSheetBehavior");
    }

    private float getYVelocity() {
        VelocityTracker velocityTracker = this.velocityTracker;
        if (velocityTracker == null) {
            return 0.0f;
        }
        velocityTracker.computeCurrentVelocity(1000, this.maximumVelocity);
        return this.velocityTracker.getYVelocity(this.activePointerId);
    }

    private void reset() {
        this.activePointerId = -1;
        VelocityTracker velocityTracker = this.velocityTracker;
        if (velocityTracker != null) {
            velocityTracker.recycle();
            this.velocityTracker = null;
        }
    }

    private void restoreOptionalState(SavedState savedState) {
        int saveFlags = this.saveFlags;
        if (saveFlags == SAVE_NONE) {
            return;
        }
        if (saveFlags == SAVE_ALL || (saveFlags & SAVE_PEEK_HEIGHT) == SAVE_PEEK_HEIGHT) {
            this.peekHeight = savedState.peekHeight;
        }
        if (saveFlags == SAVE_ALL || (saveFlags & SAVE_FIT_TO_CONTENTS) == SAVE_FIT_TO_CONTENTS) {
            this.fitToContents = savedState.fitToContents;
        }
        if (saveFlags == SAVE_ALL || (saveFlags & SAVE_HIDEABLE) == SAVE_HIDEABLE) {
            this.hideable = savedState.hideable;
        }
        if (saveFlags == SAVE_ALL || (saveFlags & SAVE_SKIP_COLLAPSED) == SAVE_SKIP_COLLAPSED) {
            this.skipCollapsed = savedState.skipCollapsed;
        }
    }

    private void setPanelPeekHeight(int peekHeight, boolean animate) {
        V child;
        if (peekHeight == PEEK_HEIGHT_AUTO) {
            if (this.peekHeightAuto) {
                return;
            } else {
                this.peekHeightAuto = true;
            }
        } else {
            if (!this.peekHeightAuto && this.peekHeight == peekHeight) {
                return;
            }
            this.peekHeightAuto = false;
            this.peekHeight = Math.max(0, peekHeight);
        }
        if (this.viewRef != null) {
            calculateCollapsedOffset();
            if (this.state != STATE_COLLAPSED || (child = this.viewRef.get()) == null) {
                return;
            }
            if (animate) {
                settleToStatePendingLayout(this.state);
            } else {
                child.requestLayout();
            }
        }
    }

    private void setSystemGestureInsets(CoordinatorLayout coordinatorLayout) {
        WindowInsets rootWindowInsets;
        if (isGestureInsetBottomIgnored() || (rootWindowInsets = coordinatorLayout.getRootWindowInsets()) == null) {
            return;
        }
        this.peekHeight += rootWindowInsets.getSystemGestureInsets().bottom;
    }

    private void settleToStatePendingLayout(final int state) {
        final V child = this.viewRef.get();
        if (child == null) {
            return;
        }
        ViewParent parent = child.getParent();
        if (parent != null && parent.isLayoutRequested() && ViewCompat.isLaidOut(child)) {
            child.post(new Runnable() {
                @Override
                public void run() {
                    COUIGuideBehavior.this.settleToState(child, state);
                }
            });
        } else {
            settleToState(child, state);
        }
    }

    private void updateAccessibilityActions() {
        V child;
        WeakReference<V> weakReference = this.viewRef;
        if (weakReference == null || (child = weakReference.get()) == null) {
            return;
        }
        ViewCompat.removeAccessibilityAction(child, 524288);
        ViewCompat.removeAccessibilityAction(child, 262144);
        ViewCompat.removeAccessibilityAction(child, 1048576);
        if (this.hideable && this.state != 5) {
            addAccessibilityActionForState(child, AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_DISMISS, STATE_HIDDEN);
        }
        int currentState = this.state;
        if (currentState == STATE_EXPANDED) {
            addAccessibilityActionForState(child, AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_COLLAPSE, this.fitToContents ? STATE_COLLAPSED : STATE_HALF_EXPANDED);
            return;
        }
        if (currentState == STATE_COLLAPSED) {
            addAccessibilityActionForState(child, AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_EXPAND, this.fitToContents ? STATE_EXPANDED : STATE_HALF_EXPANDED);
        } else {
            if (currentState != STATE_HALF_EXPANDED) {
                return;
            }
            addAccessibilityActionForState(child, AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_COLLAPSE, STATE_COLLAPSED);
            addAccessibilityActionForState(child, AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_EXPAND, STATE_EXPANDED);
        }
    }

    private void updateDrawableForTargetState(int targetState) {
        ValueAnimator valueAnimator;
        if (targetState == STATE_SETTLING) {
            return;
        }
        boolean expanded = targetState == STATE_EXPANDED;
        if (this.isShapeExpanded != expanded) {
            this.isShapeExpanded = expanded;
            if (this.materialShapeDrawable == null || (valueAnimator = this.interpolatorAnimator) == null) {
                return;
            }
            if (valueAnimator.isRunning()) {
                this.interpolatorAnimator.reverse();
                return;
            }
            float targetInterpolation = expanded ? 0.0f : 1.0f;
            this.interpolatorAnimator.setFloatValues(1.0f - targetInterpolation, targetInterpolation);
            this.interpolatorAnimator.start();
        }
    }

    private void updateImportantForAccessibility(boolean expanded) {
        Map<View, Integer> map;
        WeakReference<V> weakReference = this.viewRef;
        if (weakReference == null) {
            return;
        }
        ViewParent parent = weakReference.get().getParent();
        if (parent instanceof CoordinatorLayout) {
            CoordinatorLayout coordinatorLayout = (CoordinatorLayout) parent;
            int childCount = coordinatorLayout.getChildCount();
            if (expanded) {
                if (this.importantForAccessibilityMap != null) {
                    return;
                } else {
                    this.importantForAccessibilityMap = new HashMap(childCount);
                }
            }
            for (int childIndex = 0; childIndex < childCount; childIndex++) {
                View childAt = coordinatorLayout.getChildAt(childIndex);
                if (childAt != this.viewRef.get()) {
                    if (expanded) {
                        this.importantForAccessibilityMap.put(childAt, Integer.valueOf(childAt.getImportantForAccessibility()));
                        if (this.updateImportantForAccessibilityOnSiblings) {
                            ViewCompat.setImportantForAccessibility(childAt, 4);
                        }
                    } else if (this.updateImportantForAccessibilityOnSiblings && (map = this.importantForAccessibilityMap) != null && map.containsKey(childAt)) {
                        ViewCompat.setImportantForAccessibility(childAt, this.importantForAccessibilityMap.get(childAt).intValue());
                    }
                }
            }
            if (expanded) {
                return;
            }
            this.importantForAccessibilityMap = null;
        }
    }

    public void addBottomSheetCallback(COUIBottomSheetCallback callback) {
        if (this.callbacks.contains(callback)) {
            return;
        }
        this.callbacks.add(callback);
    }

    public void disableShapeAnimations() {
        this.interpolatorAnimator = null;
    }

    public void dispatchOnSlide(int top) {
        float slideDistance;
        float slideRange;
        V child = this.viewRef.get();
        if (child == null || this.callbacks.isEmpty()) {
            return;
        }
        int collapsedOffset = this.collapsedOffset;
        if (top > collapsedOffset || collapsedOffset == getExpandedOffset()) {
            int collapsedTop = this.collapsedOffset;
            slideDistance = collapsedTop - top;
            slideRange = this.parentHeight - collapsedTop;
        } else {
            int collapsedTop = this.collapsedOffset;
            slideDistance = collapsedTop - top;
            slideRange = collapsedTop - getExpandedOffset();
        }
        float slideOffset = slideDistance / slideRange;
        for (int callbackIndex = 0; callbackIndex < this.callbacks.size(); callbackIndex++) {
            this.callbacks.get(callbackIndex).onSlide(child, slideOffset);
        }
    }

    public View findScrollingChild(View view) {
        if (ViewCompat.isAttachedToWindow(view) && view.getVisibility() == 0) {
            return view;
        }
        if (!(view instanceof ViewGroup) || view.getVisibility() != 0) {
            return null;
        }
        ViewGroup viewGroup = (ViewGroup) view;
        int childCount = viewGroup.getChildCount();
        for (int childIndex = 0; childIndex < childCount; childIndex++) {
            View scrollingChild = findScrollingChild(viewGroup.getChildAt(childIndex));
            if (scrollingChild != null) {
                return scrollingChild;
            }
        }
        return null;
    }

    public COUIPanelDragListener getCOUIPanelDragListener() {
        return this.mCOUIPanelDragListener;
    }

    @Override
    public int getExpandedOffset() {
        return this.fitToContents ? this.fitToContentsOffset : this.expandedOffset;
    }

    @Override
    public float getHalfExpandedRatio() {
        return this.halfExpandedRatio;
    }

    public int getPeekHeight() {
        if (this.peekHeightAuto) {
            return -1;
        }
        return this.peekHeight;
    }

    public int getPeekHeightMin() {
        return this.peekHeightMin;
    }

    public int getSaveFlags() {
        return this.saveFlags;
    }

    public boolean getSkipCollapsed() {
        return this.skipCollapsed;
    }

    @Override
    @SuppressLint({"WrongConstant"})
    public int getState() {
        return this.state;
    }

    public boolean isCanHideKeyboard() {
        return this.mCanHideKeyboard;
    }

    public boolean isDraggable() {
        return this.draggable;
    }

    public boolean isFitToContents() {
        return this.fitToContents;
    }

    @Override
    public boolean isGestureInsetBottomIgnored() {
        return this.gestureInsetBottomIgnored;
    }

    @Override
    public boolean isHideable() {
        return this.hideable;
    }

    @Override
    public void onAttachedToLayoutParams(CoordinatorLayout.LayoutParams fVar) {
        super.onAttachedToLayoutParams(fVar);
        this.viewRef = null;
        this.viewDragHelper = null;
    }

    @Override
    public void onDetachedFromLayoutParams() {
        super.onDetachedFromLayoutParams();
        this.viewRef = null;
        this.viewDragHelper = null;
    }

    @Override
    public boolean onInterceptTouchEvent(CoordinatorLayout coordinatorLayout, V child, MotionEvent motionEvent) {
        ViewDragHelper dragHelper;
        if (!child.isShown() || !this.draggable) {
            this.ignoreEvents = true;
            return false;
        }
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == 0) {
            reset();
        }
        if (this.velocityTracker == null) {
            this.velocityTracker = VelocityTracker.obtain();
        }
        this.velocityTracker.addMovement(motionEvent);
        if (actionMasked == 0) {
            this.initialX = (int) motionEvent.getX();
            this.initialY = (int) motionEvent.getY();
            if (this.state != 2) {
                WeakReference<View> weakReference = this.nestedScrollingChildRef;
                View view = weakReference != null ? weakReference.get() : null;
                if (view != null && coordinatorLayout.isPointInChildBounds(view, this.initialX, this.initialY)) {
                    this.activePointerId = motionEvent.getPointerId(motionEvent.getActionIndex());
                    this.touchingScrollingChild = true;
                }
            }
            this.ignoreEvents = this.activePointerId == -1 && !coordinatorLayout.isPointInChildBounds(child, this.initialX, this.initialY);
        } else if (actionMasked == 1) {
            COUIPanelPullUpListener pullUpListener = this.mPullUpListener;
            if (pullUpListener != null) {
                pullUpListener.onCancel();
            }
        } else if (actionMasked == 3) {
            this.touchingScrollingChild = false;
            this.activePointerId = -1;
            if (this.ignoreEvents) {
                this.ignoreEvents = false;
                return false;
            }
        }
        if (!this.ignoreEvents && (dragHelper = this.viewDragHelper) != null && dragHelper.shouldInterceptTouchEvent(motionEvent)) {
            return true;
        }
        if (Math.abs(this.initialY - motionEvent.getY()) > Math.abs(this.initialX - motionEvent.getX()) * 2.0f && this.viewDragHelper != null && Math.abs(this.initialY - motionEvent.getY()) > this.viewDragHelper.getTouchSlop()) {
            return true;
        }
        WeakReference<View> nestedChildRef = this.nestedScrollingChildRef;
        View nestedChild = nestedChildRef != null ? nestedChildRef.get() : null;
        return (actionMasked != 2 || nestedChild == null || this.ignoreEvents || this.state == STATE_DRAGGING || coordinatorLayout.isPointInChildBounds(nestedChild, (int) motionEvent.getX(), (int) motionEvent.getY()) || this.viewDragHelper == null || Math.abs(((float) this.initialY) - motionEvent.getY()) <= ((float) this.viewDragHelper.getTouchSlop())) ? false : true;
    }

    @Override
    public boolean onLayoutChild(CoordinatorLayout coordinatorLayout, V child, int layoutDirection) {
        MaterialShapeDrawable shapeDrawable;
        if (ViewCompat.getFitsSystemWindows(coordinatorLayout) && !ViewCompat.getFitsSystemWindows(child)) {
            child.setFitsSystemWindows(true);
        }
        if (this.viewRef == null) {
            this.peekHeightMin = coordinatorLayout.getResources().getDimensionPixelSize(com.google.android.material.R.dimen.design_bottom_sheet_peek_height_min);
            setSystemGestureInsets(coordinatorLayout);
            this.viewRef = new WeakReference<>(child);
            if (this.shapeThemingEnabled && (shapeDrawable = this.materialShapeDrawable) != null) {
                ViewCompat.setBackground(child, shapeDrawable);
            }
            MaterialShapeDrawable materialShapeDrawable = this.materialShapeDrawable;
            if (materialShapeDrawable != null) {
                float fT = this.elevation;
                if (fT == -1.0f) {
                    fT = (int) ViewCompat.getElevation(child);
                }
                materialShapeDrawable.setElevation(fT);
                boolean expanded = this.state == STATE_EXPANDED;
                this.isShapeExpanded = expanded;
                this.materialShapeDrawable.setInterpolation(expanded ? 0.0f : 1.0f);
            }
            updateAccessibilityActions();
            if (ViewCompat.getImportantForAccessibility(child) == 0) {
                ViewCompat.setImportantForAccessibility(child, 1);
            }
        }
        if (this.viewDragHelper == null) {
            this.viewDragHelper = ViewDragHelper.create(coordinatorLayout, this.dragCallback);
        }
        int top = child.getTop();
        coordinatorLayout.onLayoutChild(child, layoutDirection);
        this.parentWidth = coordinatorLayout.getWidth();
        int height = coordinatorLayout.getHeight();
        this.parentHeight = height;
        if (!this.mIsIgnoreExpandedOffsetChange) {
            this.fitToContentsOffset = Math.max(0, height - child.getHeight());
        }
        this.mIsIgnoreExpandedOffsetChange = false;
        calculateHalfExpandedOffset();
        calculateCollapsedOffset();
        int currentState = this.state;
        if (currentState == STATE_EXPANDED) {
            ViewCompat.offsetTopAndBottom(child, getExpandedOffset());
        } else if (currentState == STATE_HALF_EXPANDED) {
            ViewCompat.offsetTopAndBottom(child, this.halfExpandedOffset);
        } else if (this.hideable && currentState == STATE_HIDDEN) {
            ViewCompat.offsetTopAndBottom(child, this.parentHeight);
        } else if (currentState == STATE_COLLAPSED) {
            ViewCompat.offsetTopAndBottom(child, this.collapsedOffset);
        } else if (currentState == STATE_DRAGGING || currentState == STATE_SETTLING) {
            ViewCompat.offsetTopAndBottom(child, top - child.getTop());
        }
        this.nestedScrollingChildRef = new WeakReference<>(findScrollingChild(child));
        return true;
    }

    @Override
    public boolean onNestedPreFling(CoordinatorLayout coordinatorLayout, V child, View target, float velocityX, float velocityY) {
        WeakReference<View> weakReference = this.nestedScrollingChildRef;
        if (weakReference == null || target != weakReference.get()) {
            return false;
        }
        return this.state != STATE_EXPANDED || super.onNestedPreFling(coordinatorLayout, child, target, velocityX, velocityY);
    }

    @Override
    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, V child, View target, int dx, int dy, int[] consumed, int type) {
        if (type == 1) {
            return;
        }
        WeakReference<View> weakReference = this.nestedScrollingChildRef;
        if (target != (weakReference != null ? weakReference.get() : null)) {
            return;
        }
        int top = child.getTop();
        int newTop = top - dy;
        if (dy > 0) {
            if (newTop < getExpandedOffset()) {
                int expandedOffset = top - getExpandedOffset();
                consumed[1] = expandedOffset;
                ViewCompat.offsetTopAndBottom(child, -expandedOffset);
                setStateInternal(STATE_EXPANDED);
            } else {
                if (!this.draggable) {
                    return;
                }
                consumed[1] = dy;
                ViewCompat.offsetTopAndBottom(child, -dy);
                setStateInternal(STATE_DRAGGING);
            }
        } else if (dy < 0 && !target.canScrollVertically(-1)) {
            int collapsedOffset = this.collapsedOffset;
            if (newTop > collapsedOffset && !this.hideable) {
                int collapsedDelta = top - collapsedOffset;
                consumed[1] = collapsedDelta;
                ViewCompat.offsetTopAndBottom(child, -collapsedDelta);
                setStateInternal(STATE_COLLAPSED);
            } else {
                if (!this.draggable) {
                    return;
                }
                consumed[1] = dy;
                ViewCompat.offsetTopAndBottom(child, -dy);
                setStateInternal(STATE_DRAGGING);
            }
        }
        dispatchOnSlide(child.getTop());
        this.lastNestedScrollDy = dy;
        this.nestedScrolled = true;
    }

    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, V child, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type, int[] consumed) {
    }

    @Override
    public void onRestoreInstanceState(CoordinatorLayout coordinatorLayout, V child, Parcelable parcelable) {
        SavedState savedState = (SavedState) parcelable;
        super.onRestoreInstanceState(coordinatorLayout, child, savedState.getSuperState());
        restoreOptionalState(savedState);
        int savedStateValue = savedState.state;
        if (savedStateValue == STATE_DRAGGING || savedStateValue == STATE_SETTLING) {
            this.state = STATE_COLLAPSED;
        } else {
            this.state = savedStateValue;
        }
    }

    @Override
    public Parcelable onSaveInstanceState(CoordinatorLayout coordinatorLayout, V child) {
        return new SavedState(super.onSaveInstanceState(coordinatorLayout, child), (COUIGuideBehavior<?>) this);
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, V child, View directTargetChild, View target, int axes, int type) {
        this.lastNestedScrollDy = 0;
        this.nestedScrolled = false;
        return (axes & 2) != 0;
    }

    @Override
    public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, V child, View target, int type) {
        int targetTop;
        int targetState = STATE_EXPANDED;
        if (child.getTop() == getExpandedOffset()) {
            setStateInternal(STATE_EXPANDED);
            return;
        }
        WeakReference<View> weakReference = this.nestedScrollingChildRef;
        if (weakReference != null && target == weakReference.get() && this.nestedScrolled) {
            if (this.lastNestedScrollDy > 0) {
                if (this.fitToContents) {
                    targetTop = this.fitToContentsOffset;
                } else {
                    int top = child.getTop();
                    int halfExpandedOffset = this.halfExpandedOffset;
                    if (top > halfExpandedOffset) {
                        targetState = STATE_HALF_EXPANDED;
                        targetTop = halfExpandedOffset;
                    } else {
                        targetTop = this.expandedOffset;
                    }
                }
            } else if (this.hideable && shouldHide(child, getYVelocity())) {
                COUIPanelDragListener panelDragListener = this.mCOUIPanelDragListener;
                if (panelDragListener == null || !panelDragListener.onDragWhileEditing()) {
                    targetTop = this.parentHeight;
                    this.mCanHideKeyboard = true;
                    targetState = STATE_HIDDEN;
                } else {
                    targetTop = this.fitToContentsOffset;
                    this.mCanHideKeyboard = false;
                }
            } else if (this.lastNestedScrollDy == 0) {
                int top = child.getTop();
                if (!this.fitToContents) {
                    int halfExpandedOffset = this.halfExpandedOffset;
                    if (top < halfExpandedOffset) {
                        if (top < Math.abs(top - this.collapsedOffset)) {
                            targetTop = this.expandedOffset;
                        } else {
                            targetTop = this.halfExpandedOffset;
                        }
                    } else if (Math.abs(top - halfExpandedOffset) < Math.abs(top - this.collapsedOffset)) {
                        targetTop = this.halfExpandedOffset;
                    } else {
                        targetTop = this.collapsedOffset;
                        targetState = STATE_COLLAPSED;
                    }
                    targetState = STATE_HALF_EXPANDED;
                } else if (Math.abs(top - this.fitToContentsOffset) < Math.abs(top - this.collapsedOffset)) {
                    targetTop = this.fitToContentsOffset;
                } else {
                    targetTop = this.collapsedOffset;
                    targetState = STATE_COLLAPSED;
                }
            } else {
                if (this.fitToContents) {
                    targetTop = this.collapsedOffset;
                } else {
                    int top = child.getTop();
                    if (Math.abs(top - this.halfExpandedOffset) < Math.abs(top - this.collapsedOffset)) {
                        targetTop = this.halfExpandedOffset;
                        targetState = STATE_HALF_EXPANDED;
                    } else {
                        targetTop = this.collapsedOffset;
                    }
                }
                targetState = STATE_COLLAPSED;
            }
            startSettlingAnimation(child, targetState, targetTop, false);
            this.nestedScrolled = false;
        }
    }

    @Override
    public boolean onTouchEvent(CoordinatorLayout coordinatorLayout, V child, MotionEvent motionEvent) {
        if (!child.isShown()) {
            return false;
        }
        int actionMasked = motionEvent.getActionMasked();
        if (this.state == STATE_DRAGGING && actionMasked == 0) {
            return true;
        }
        ViewDragHelper dragHelper = this.viewDragHelper;
        if (dragHelper != null) {
            dragHelper.processTouchEvent(motionEvent);
        }
        if (actionMasked == 0) {
            reset();
        }
        if (this.velocityTracker == null) {
            this.velocityTracker = VelocityTracker.obtain();
        }
        this.velocityTracker.addMovement(motionEvent);
        if (actionMasked == 2 && !this.ignoreEvents && this.viewDragHelper != null && Math.abs(this.initialY - motionEvent.getY()) > this.viewDragHelper.getTouchSlop()) {
            this.viewDragHelper.captureChildView(child, motionEvent.getPointerId(motionEvent.getActionIndex()));
        }
        return !this.ignoreEvents;
    }

    public void removeBottomSheetCallback(COUIBottomSheetCallback cOUIBottomSheetCallback) {
        this.callbacks.remove(cOUIBottomSheetCallback);
    }

    @Deprecated
    public void setBottomSheetCallback(COUIBottomSheetCallback cOUIBottomSheetCallback) {
        Log.w(TAG, "BottomSheetBehavior now supports multiple callbacks. `setBottomSheetCallback()` removes all existing callbacks, including ones set internally by library authors, which may result in unintended behavior. This may change in the future. Please use `addBottomSheetCallback()` and `removeBottomSheetCallback()` instead to set your own callbacks.");
        this.callbacks.clear();
        if (cOUIBottomSheetCallback != null) {
            this.callbacks.add(cOUIBottomSheetCallback);
        }
    }

    public void setCanHideKeyboard(boolean canHideKeyboard) {
        this.mCanHideKeyboard = canHideKeyboard;
    }

    @Override
    public void setDraggable(boolean draggable) {
        this.draggable = draggable;
    }

    @Override
    public void setExpandedOffset(int offset) {
        if (offset < 0) {
            throw new IllegalArgumentException("offset must be greater than or equal to 0");
        }
        this.expandedOffset = offset;
    }

    @Override
    public void setFitToContents(boolean fitToContents) {
        if (this.fitToContents == fitToContents) {
            return;
        }
        this.fitToContents = fitToContents;
        if (this.viewRef != null) {
            calculateCollapsedOffset();
        }
        setStateInternal((this.fitToContents && this.state == STATE_HALF_EXPANDED) ? STATE_EXPANDED : this.state);
        updateAccessibilityActions();
    }

    @Override
    public void setGestureInsetBottomIgnored(boolean gestureInsetBottomIgnored) {
        this.gestureInsetBottomIgnored = gestureInsetBottomIgnored;
    }

    @Override
    public void setHalfExpandedRatio(float ratio) {
        if (ratio <= 0.0f || ratio >= 1.0f) {
            throw new IllegalArgumentException("ratio must be a float value between 0 and 1");
        }
        this.halfExpandedRatio = ratio;
        if (this.viewRef != null) {
            calculateHalfExpandedOffset();
        }
    }

    @Override
    @SuppressLint({"WrongConstant"})
    public void setHideable(boolean hideable) {
        if (this.hideable != hideable) {
            this.hideable = hideable;
            if (!hideable && this.state == STATE_HIDDEN) {
                setState(STATE_COLLAPSED);
            }
            updateAccessibilityActions();
        }
    }

    public void setPanelDragListener(COUIPanelDragListener panelDragListener) {
        this.mCOUIPanelDragListener = panelDragListener;
    }

    @Override
    public void setPeekHeight(int peekHeight) {
        setPanelPeekHeight(peekHeight, false);
    }

    public void setPullUpListener(COUIPanelPullUpListener pullUpListener) {
        this.mPullUpListener = pullUpListener;
    }

    @Override
    public void setSaveFlags(int saveFlags) {
        this.saveFlags = saveFlags;
    }

    @Override
    public void setSkipCollapsed(boolean skipCollapsed) {
        this.skipCollapsed = skipCollapsed;
    }

    @Override
    public void setState(int state) {
        if (state == this.state) {
            return;
        }
        if (this.viewRef != null) {
            settleToStatePendingLayout(state);
            return;
        }
        if (state == STATE_COLLAPSED || state == STATE_EXPANDED || state == STATE_HALF_EXPANDED || (this.hideable && state == STATE_HIDDEN)) {
            this.state = state;
        }
    }

    public void setStateInternal(int state) {
        V child;
        if (this.state == state) {
            return;
        }
        this.state = state;
        WeakReference<V> weakReference = this.viewRef;
        if (weakReference == null || (child = weakReference.get()) == null) {
            return;
        }
        if (state == STATE_EXPANDED) {
            updateImportantForAccessibility(true);
        } else if (state == STATE_HALF_EXPANDED || state == STATE_HIDDEN || state == STATE_COLLAPSED) {
            updateImportantForAccessibility(false);
        }
        updateDrawableForTargetState(state);
        for (int callbackIndex = 0; callbackIndex < this.callbacks.size(); callbackIndex++) {
            this.callbacks.get(callbackIndex).onStateChanged(child, state);
        }
        updateAccessibilityActions();
    }

    public void setStateWithoutAnim(int state, boolean notifyCallbacks) {
        V child;
        if (this.state == state) {
            return;
        }
        this.state = state;
        WeakReference<V> weakReference = this.viewRef;
        if (weakReference == null || !notifyCallbacks || (child = weakReference.get()) == null) {
            return;
        }
        for (int callbackIndex = 0; callbackIndex < this.callbacks.size(); callbackIndex++) {
            this.callbacks.get(callbackIndex).onStateChanged(child, state);
        }
    }

    public void setUpdateImportantForAccessibilityOnSiblings(boolean updateImportantForAccessibilityOnSiblings) {
        this.updateImportantForAccessibilityOnSiblings = updateImportantForAccessibilityOnSiblings;
    }

    public void settleToState(View view, int state) {
        int expandedOffset;
        int fitToContentsOffset;
        if (state == STATE_COLLAPSED) {
            expandedOffset = this.collapsedOffset;
        } else if (state == STATE_HALF_EXPANDED) {
            expandedOffset = this.halfExpandedOffset;
            if (this.fitToContents && expandedOffset <= (fitToContentsOffset = this.fitToContentsOffset)) {
                state = STATE_EXPANDED;
                expandedOffset = fitToContentsOffset;
            }
        } else if (state == STATE_EXPANDED) {
            expandedOffset = getExpandedOffset();
        } else {
            if (!this.hideable || state != STATE_HIDDEN) {
                throw new IllegalArgumentException("Illegal state argument: " + state);
            }
            expandedOffset = this.parentHeight;
        }
        startSettlingAnimation(view, state, expandedOffset, false);
    }

    public boolean shouldHide(View view, float yVelocity) {
        if (this.skipCollapsed) {
            return true;
        }
        if (view.getTop() < this.collapsedOffset) {
            return false;
        }
        return Math.abs((((float) view.getTop()) + (yVelocity * HIDE_FRICTION)) - ((float) this.collapsedOffset)) / ((float) calculatePeekHeight()) > HIDE_THRESHOLD;
    }

    public void startSettlingAnimation(View view, int state, int top, boolean settleFromViewDragHelper) {
        if (!(settleFromViewDragHelper ? this.viewDragHelper.settleCapturedViewAt(view.getLeft(), top) : this.viewDragHelper.smoothSlideViewTo(view, view.getLeft(), top))) {
            setStateInternal(state);
            return;
        }
        setStateInternal(STATE_SETTLING);
        updateDrawableForTargetState(state);
        if (this.settleRunnable == null) {
            this.settleRunnable = new SettleRunnable(view, state);
        }
        if (((SettleRunnable) this.settleRunnable).isPosted) {
            this.settleRunnable.targetState = state;
            return;
        }
        COUIGuideBehavior<V>.SettleRunnable settleRunnable = this.settleRunnable;
        settleRunnable.targetState = state;
        ViewCompat.postOnAnimation(view, settleRunnable);
        ((SettleRunnable) this.settleRunnable).isPosted = true;
    }

    private void createMaterialShapeDrawable(Context context, AttributeSet attributeSet, boolean hasBackgroundTint, ColorStateList colorStateList) {
        if (this.shapeThemingEnabled) {
            this.shapeAppearanceModelDefault = ShapeAppearanceModel.builder(context, attributeSet, com.google.android.material.R.attr.bottomSheetStyle, DEF_STYLE_RES).build();
            MaterialShapeDrawable shapeDrawable = new MaterialShapeDrawable(this.shapeAppearanceModelDefault);
            this.materialShapeDrawable = shapeDrawable;
            shapeDrawable.initializeElevationOverlay(context);
            if (hasBackgroundTint && colorStateList != null) {
                this.materialShapeDrawable.setFillColor(colorStateList);
                return;
            }
            TypedValue typedValue = new TypedValue();
            context.getTheme().resolveAttribute(R.attr.colorBackground, typedValue, true);
            this.materialShapeDrawable.setTint(typedValue.data);
        }
    }

    public COUIGuideBehavior(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        int peekHeightValue;
        this.saveFlags = 0;
        this.fitToContents = true;
        this.updateImportantForAccessibilityOnSiblings = false;
        this.settleRunnable = null;
        this.halfExpandedRatio = 0.5f;
        this.elevation = -1.0f;
        this.draggable = true;
        this.state = STATE_COLLAPSED;
        this.callbacks = new ArrayList<>();
        this.dragCallback = new ViewDragHelper.Callback() {
            private boolean releasedLow(View view) {
                int top = view.getTop();
                COUIGuideBehavior behavior = COUIGuideBehavior.this;
                return top > (behavior.parentHeight + behavior.getExpandedOffset()) / 2;
            }

            @Override
            public int clampViewPositionHorizontal(View view, int left, int dx) {
                return view.getLeft();
            }

            @Override
            public int clampViewPositionVertical(View view, int top, int dy) {
                int pullUpOffset;
                int currentState;
                if (COUIGuideBehavior.this.mPullUpListener == null || ((currentState = COUIGuideBehavior.this.state) != STATE_EXPANDED && (currentState != STATE_DRAGGING || view.getTop() > COUIGuideBehavior.this.getExpandedOffset()))) {
                    pullUpOffset = 0;
                } else {
                    COUIGuideBehavior.this.mIsIgnoreExpandedOffsetChange = true;
                    pullUpOffset = COUIGuideBehavior.this.mPullUpListener.onDragging(dy, COUIGuideBehavior.this.getExpandedOffset());
                }
                int expandedOffset = COUIGuideBehavior.this.getExpandedOffset() - pullUpOffset;
                COUIGuideBehavior behavior = COUIGuideBehavior.this;
                return androidx.core.math.MathUtils.clamp(top, expandedOffset, behavior.hideable ? behavior.parentHeight : behavior.collapsedOffset);
            }

            @Override
            public int getViewVerticalDragRange(View view) {
                COUIGuideBehavior behavior = COUIGuideBehavior.this;
                return behavior.hideable ? behavior.parentHeight : behavior.collapsedOffset;
            }

            @Override
            public void onViewDragStateChanged(int state) {
                if (state == STATE_DRAGGING && COUIGuideBehavior.this.draggable) {
                    COUIGuideBehavior.this.setStateInternal(STATE_DRAGGING);
                }
            }

            @Override
            public void onViewPositionChanged(View view, int left, int top, int dx, int dy) {
                COUIGuideBehavior.this.dispatchOnSlide(top);
            }

            @Override
            public void onViewReleased(View view, float xVelocity, float yVelocity) {
                int targetTop;
                if (COUIGuideBehavior.this.mPullUpListener != null && COUIGuideBehavior.this.parentHeight - view.getHeight() < COUIGuideBehavior.this.getExpandedOffset() && view.getTop() < COUIGuideBehavior.this.getExpandedOffset()) {
                    COUIGuideBehavior.this.mPullUpListener.onReleased(COUIGuideBehavior.this.getExpandedOffset());
                    return;
                }
                int targetState = 6;
                if (yVelocity < 0.0f) {
                    if (COUIGuideBehavior.this.fitToContents) {
                        targetTop = COUIGuideBehavior.this.fitToContentsOffset;
                    } else {
                        int top = view.getTop();
                        COUIGuideBehavior behavior = COUIGuideBehavior.this;
                        int halfExpandedOffset = behavior.halfExpandedOffset;
                        if (top > halfExpandedOffset) {
                            targetTop = halfExpandedOffset;
                        } else {
                            targetTop = behavior.expandedOffset;
                        }
                    }
                    targetState = 3;
                } else {
                    COUIGuideBehavior behavior = COUIGuideBehavior.this;
                    if (behavior.hideable && behavior.shouldHide(view, yVelocity)) {
                        COUIPanelDragListener dragListener = COUIGuideBehavior.this.mCOUIPanelDragListener;
                        if (dragListener != null && dragListener.onDragWhileEditing()) {
                            COUIGuideBehavior guideBehavior = COUIGuideBehavior.this;
                            int fitToContentsOffset = guideBehavior.fitToContentsOffset;
                            guideBehavior.mCanHideKeyboard = false;
                            targetTop = fitToContentsOffset;
                        } else if ((Math.abs(xVelocity) < Math.abs(yVelocity) && yVelocity > 500.0f) || releasedLow(view)) {
                            COUIGuideBehavior guideBehavior = COUIGuideBehavior.this;
                            int parentHeight = guideBehavior.parentHeight;
                            guideBehavior.mCanHideKeyboard = true;
                            targetState = 5;
                            targetTop = parentHeight;
                        } else if (COUIGuideBehavior.this.fitToContents) {
                            targetTop = COUIGuideBehavior.this.fitToContentsOffset;
                        } else if (Math.abs(view.getTop() - COUIGuideBehavior.this.expandedOffset) < Math.abs(view.getTop() - COUIGuideBehavior.this.halfExpandedOffset)) {
                            targetTop = COUIGuideBehavior.this.expandedOffset;
                        } else {
                            targetTop = COUIGuideBehavior.this.halfExpandedOffset;
                        }
                        targetState = 3;
                    } else if (yVelocity == 0.0f || Math.abs(xVelocity) > Math.abs(yVelocity)) {
                        int top2 = view.getTop();
                        if (!COUIGuideBehavior.this.fitToContents) {
                            COUIGuideBehavior guideBehavior = COUIGuideBehavior.this;
                            int halfExpandedOffset = guideBehavior.halfExpandedOffset;
                            if (top2 < halfExpandedOffset) {
                                if (top2 < Math.abs(top2 - guideBehavior.collapsedOffset)) {
                                    targetTop = COUIGuideBehavior.this.expandedOffset;
                                    targetState = 3;
                                } else {
                                    targetTop = COUIGuideBehavior.this.halfExpandedOffset;
                                }
                            } else if (Math.abs(top2 - halfExpandedOffset) < Math.abs(top2 - COUIGuideBehavior.this.collapsedOffset)) {
                                targetTop = COUIGuideBehavior.this.halfExpandedOffset;
                            } else {
                                targetTop = COUIGuideBehavior.this.collapsedOffset;
                                targetState = 4;
                            }
                        } else if (Math.abs(top2 - COUIGuideBehavior.this.fitToContentsOffset) < Math.abs(top2 - COUIGuideBehavior.this.collapsedOffset)) {
                            targetTop = COUIGuideBehavior.this.fitToContentsOffset;
                            targetState = 3;
                        } else {
                            targetTop = COUIGuideBehavior.this.collapsedOffset;
                            targetState = 4;
                        }
                    } else {
                        if (COUIGuideBehavior.this.fitToContents) {
                            targetTop = COUIGuideBehavior.this.collapsedOffset;
                        } else {
                            int top3 = view.getTop();
                            if (Math.abs(top3 - COUIGuideBehavior.this.halfExpandedOffset) < Math.abs(top3 - COUIGuideBehavior.this.collapsedOffset)) {
                                targetTop = COUIGuideBehavior.this.halfExpandedOffset;
                            } else {
                                targetTop = COUIGuideBehavior.this.collapsedOffset;
                            }
                        }
                        targetState = 4;
                    }
                }
                COUIGuideBehavior.this.startSettlingAnimation(view, targetState, targetTop, true);
            }

            @Override
            public boolean tryCaptureView(View view, int pointerId) {
                COUIGuideBehavior behavior = COUIGuideBehavior.this;
                int currentState = behavior.state;
                if (currentState == 1 || behavior.touchingScrollingChild) {
                    return false;
                }
                if (currentState == 3 && behavior.activePointerId == pointerId) {
                    WeakReference<View> weakReference = behavior.nestedScrollingChildRef;
                    View nestedChild = weakReference != null ? weakReference.get() : null;
                    if (nestedChild != null && nestedChild.canScrollVertically(-1)) {
                        return false;
                    }
                }
                WeakReference<V> sheetRef = COUIGuideBehavior.this.viewRef;
                return sheetRef != null && sheetRef.get() == view;
            }
        };
        TypedArray attributes = context.obtainStyledAttributes(attributeSet, com.google.android.material.R.styleable.BottomSheetBehavior_Layout);
        this.shapeThemingEnabled = attributes.hasValue(com.google.android.material.R.styleable.BottomSheetBehavior_Layout_shapeAppearance);
        int backgroundTint = com.google.android.material.R.styleable.BottomSheetBehavior_Layout_backgroundTint;
        boolean hasBackgroundTint = attributes.hasValue(backgroundTint);
        if (hasBackgroundTint) {
            createMaterialShapeDrawable(context, attributeSet, hasBackgroundTint, MaterialResource.getColorStateList(context, attributes, backgroundTint));
        } else {
            createMaterialShapeDrawable(context, attributeSet, hasBackgroundTint);
        }
        createShapeValueAnimator();
        this.elevation = attributes.getDimension(com.google.android.material.R.styleable.BottomSheetBehavior_Layout_android_elevation, -1.0f);
        int peekHeight = com.google.android.material.R.styleable.BottomSheetBehavior_Layout_behavior_peekHeight;
        TypedValue peekHeightTypedValue = attributes.peekValue(peekHeight);
        if (peekHeightTypedValue != null && (peekHeightValue = peekHeightTypedValue.data) == PEEK_HEIGHT_AUTO) {
            setPeekHeight(peekHeightValue);
        } else {
            setPeekHeight(attributes.getDimensionPixelSize(peekHeight, PEEK_HEIGHT_AUTO));
        }
        setHideable(attributes.getBoolean(com.google.android.material.R.styleable.BottomSheetBehavior_Layout_behavior_hideable, false));
        setGestureInsetBottomIgnored(attributes.getBoolean(com.google.android.material.R.styleable.BottomSheetBehavior_Layout_gestureInsetBottomIgnored, false));
        setFitToContents(attributes.getBoolean(com.google.android.material.R.styleable.BottomSheetBehavior_Layout_behavior_fitToContents, true));
        setSkipCollapsed(attributes.getBoolean(com.google.android.material.R.styleable.BottomSheetBehavior_Layout_behavior_skipCollapsed, false));
        setDraggable(attributes.getBoolean(com.google.android.material.R.styleable.BottomSheetBehavior_Layout_behavior_draggable, true));
        setSaveFlags(attributes.getInt(com.google.android.material.R.styleable.BottomSheetBehavior_Layout_behavior_saveFlags, SAVE_NONE));
        setHalfExpandedRatio(attributes.getFloat(com.google.android.material.R.styleable.BottomSheetBehavior_Layout_behavior_halfExpandedRatio, 0.5f));
        int expandedOffset = com.google.android.material.R.styleable.BottomSheetBehavior_Layout_behavior_expandedOffset;
        TypedValue expandedOffsetValue = attributes.peekValue(expandedOffset);
        if (expandedOffsetValue != null && expandedOffsetValue.type == TypedValue.TYPE_INT_DEC) {
            setExpandedOffset(expandedOffsetValue.data);
        } else {
            setExpandedOffset(attributes.getDimensionPixelOffset(expandedOffset, 0));
        }
        attributes.recycle();
        this.maximumVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();
        this.mCanHideKeyboard = false;
    }
}
