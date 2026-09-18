package com.coui.appcompat.panel;

import android.R;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Rect;
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
import android.view.animation.PathInterpolator;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.core.view.accessibility.AccessibilityViewCommand;
import androidx.customview.view.AbsSavedState;
import androidx.dynamicanimation.animation.FloatValueHolder;
import com.coui.appcompat.animation.COUIOutEaseInterpolator;
import com.coui.appcompat.animation.dynamicanimation.COUIDynamicAnimation;
import com.coui.appcompat.animation.dynamicanimation.COUISpringAnimation;
import com.coui.appcompat.animation.dynamicanimation.COUISpringForce;
import com.coui.appcompat.log.COUILog;
import com.coui.appcompat.panel.COUIViewDragHelper;
import com.oplus.physicsengine.engine.AnimationListener;
import com.oplus.physicsengine.engine.AnimationUpdateListener;
import com.oplus.physicsengine.engine.BaseBehavior;
import com.oplus.physicsengine.engine.DragBehavior;
import com.oplus.physicsengine.engine.PhysicalAnimator;
import com.coui.appcompat.uiutil.UIUtil;
import com.coui.appcompat.version.COUIVersionUtil;
import com.coui.appcompat.view.MaterialResource;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.oplus.flexiblewindow.FlexibleWindowManager;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class COUIBottomSheetBehavior<V extends View> extends BottomSheetBehavior<V> implements AnimationListener, AnimationUpdateListener {
    private static final int BOTTOM_DISPLAY_VIEW_HEIGHT_LONG_TO_SHORT = 1;
    private static final int BOTTOM_DISPLAY_VIEW_HEIGHT_SHORT_TO_LONG = 2;
    private static final int CENTER_DISPLAY_VIEW_HEIGHT_LONG_TO_SHORT = 3;
    private static final int CENTER_DISPLAY_VIEW_HEIGHT_SHORT_TO_LONG = 4;
    private static final int CORNER_ANIMATION_DURATION = 500;
    private static boolean DEBUG = false;
    private static final int DEFAULT_DISPLAY = 0;
    private static final float DEFAULT_PHYSICS_DAMPING_RATIO = 0.6f;
    private static final float DEFAULT_PHYSICS_FREQUENCY = 16.0f;
    private static final float DEFAULT_TRANSLATE_HIDING_ANIMATOR_DURATION = 333.0f;
    private static final int DEF_STYLE_RES;
    private static final int DRAG_TO_HIDDEN_SPEED_THRESHOLD = 5000;
    private static final float HIDE_FRICTION = 0.1f;
    private static final float HIDE_THRESHOLD = 0.5f;
    public static final int PEEK_HEIGHT_AUTO = -1;
    private static final float PHYSICS_UNSET = Float.MIN_VALUE;
    private static final String PROPERTY_OFFSET_TOP_AND_BOTTOM = "offsetTopAndBottom";
    private static final int PULL_UP_DY_THRESHOLD = -100;
    private static final float PULL_UP_FRICTION = 0.5f;
    private static final int PULL_UP_SPEED_THRESHOLD = 10000;
    public static final int SAVE_ALL = -1;
    public static final int SAVE_FIT_TO_CONTENTS = 2;
    public static final int SAVE_HIDEABLE = 4;
    public static final int SAVE_NONE = 0;
    public static final int SAVE_PEEK_HEIGHT = 1;
    public static final int SAVE_SKIP_COLLAPSED = 8;
    private static final int SDK_SUB_VERSION_FOR_FLEXIBLE = 12;
    private static final int SDK_VERSION_FOR_FLEXIBLE = 34;
    private static final float SETTLE_ANIM_SPRING_BOUNCE = 0.0f;
    private static final float SETTLE_ANIM_SPRING_RESPONSE = 0.4f;
    private static final int SHAKE_HAND_MOVING_BASE_DOWN_VELOCITY = 100;
    private static final int SHAKE_HAND_MOVING_BASE_UP_VELOCITY = -100;
    private static final int SHAKE_HAND_MOVING_DIRECTION_DEFAULT = 0;
    private static final int SHAKE_HAND_MOVING_DIRECTION_DOWN = 2;
    private static final int SHAKE_HAND_MOVING_DIRECTION_UP = 1;
    private static final float SHAKE_HAND_MOVING_FACTOR = 0.4f;
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
    private float alphaRadio;
    private final ArrayList<COUIBottomSheetCallback> callbacks;
    int collapsedOffset;
    private final COUIViewDragHelper.Callback dragCallback;
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
    private Rect mBarRect;
    COUIPanelDragListener mCOUIPanelDragListener;
    private boolean mCanHideKeyboard;
    private Context mContext;
    private int mCurTop;
    // Leapy removed 2026-07-24: BEGIN remove the non-OPPO height-animation token state.
    // Leapy end 2026-07-24: decoded OPPO behavior uses mStartHeightChangeAnim directly.
    private int mDialogMaxHeight;
    private DragBehavior mDragBehavior;
    private View mDragChild;
    private float mDragCurrentValue;
    private float mDragDampingRatio;
    private float mDragFrequency;
    private com.oplus.physicsengine.engine.FloatValueHolder mDragValueHolder;
    private boolean mGlobalDrag;
    boolean mHalfExpandOffsetUseParentRootViewHeight;
    private boolean mIsHandlePanel;
    private boolean mIsIgnoreExpandedOffsetChange;
    private boolean mIsInTinyScreen;
    private boolean mIsNestedScrollingCheckEnabled;
    private int mLastMeasureHeight;
    private int mLastOffsetInFling;
    private int mLastOrientation;
    private boolean mLayoutAtMaxHeight;
    private int mLayoutBottom;
    private Rect mLayoutRect;
    private OnNestedScrollingChild mOnNestedScrollingChild;
    private COUISpringAnimation mPanelHeightChangeAnim;
    private OnPanelHeightChangeAnimListener mPanelHeightChangeAnimListener;
    private COUISpringForce mPanelHeightSpringForce;
    private int mPanelPaddingBottom;
    private Rect mParentRect;
    private PhysicalAnimator mPhysicalAnimator;
    private boolean mPhysicsEnable;
    int mPressDownState;
    private COUIPanelPullUpListener mPullUpListener;
    private PullUpToDismissPanelListener mPullUpToDismissPanelListener;
    private int mSettleTargetState;
    private int mShakeHandMovingDirection;
    private boolean mStartHeightChangeAnim;
    private int mStartTopValue;
    // Leapy removed 2026-07-24: BEGIN remove the synthetic target-state field absent from decoded OPPO COUI.
    // Leapy end 2026-07-24: mSettleTargetState is the reference implementation's only settling target.
    private int mViewHeightType;
    private int mWantTop;
    private float mYVelocity;
    private MaterialShapeDrawable materialShapeDrawable;
    private float maximumVelocity;
    private boolean nestedScrolled;
    WeakReference<View> nestedScrollingChildRef;
    int parentHeight;
    int parentMarginTop;
    int parentRootViewHeight;
    int parentWidth;
    private int peekHeight;
    private boolean peekHeightAuto;
    private int peekHeightMin;
    private int saveFlags;
    private ShapeAppearanceModel shapeAppearanceModelDefault;
    private boolean shapeThemingEnabled;
    private boolean skipCollapsed;
    int state;
    boolean touchingScrollingChild;
    private boolean updateImportantForAccessibilityOnSiblings;
    private VelocityTracker velocityTracker;
    COUIViewDragHelper viewDragHelper;
    WeakReference<V> viewRef;

    public static abstract class COUIBottomSheetCallback {
        public abstract void onSlide(View view, float slideOffset);

        public abstract void onStateChanged(View view, int newState);
    }

    public interface OnNestedScrollingChild {
        View getNestedScrollingChild();
    }

    public interface OnPanelHeightChangeAnimListener {
        default void onAnimationEnd(COUIDynamicAnimation cOUIDynamicAnimation, boolean canceled, float value) {
        }

        default void onAnimationStart() {
        }

        default void onAnimationUpdate(COUIDynamicAnimation cOUIDynamicAnimation, float value, float velocity) {
        }
    }

    public interface PullUpToDismissPanelListener {
        void onPullUpDismiss();
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

        public SavedState(Parcelable parcelable, COUIBottomSheetBehavior<?> cOUIBottomSheetBehavior) {
            super(parcelable);
            this.state = cOUIBottomSheetBehavior.state;
            this.peekHeight = ((COUIBottomSheetBehavior) cOUIBottomSheetBehavior).peekHeight;
            this.fitToContents = ((COUIBottomSheetBehavior) cOUIBottomSheetBehavior).fitToContents;
            this.hideable = cOUIBottomSheetBehavior.hideable;
            this.skipCollapsed = ((COUIBottomSheetBehavior) cOUIBottomSheetBehavior).skipCollapsed;
        }

        @Deprecated
        public SavedState(Parcelable parcelable, int state) {
            super(parcelable);
            this.state = state;
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface State {
    }

    static {
        DEBUG = COUILog.LOG_DEBUG || COUILog.isLoggable(TAG, 3);
        DEF_STYLE_RES = com.google.android.material.R.style.Widget_Design_BottomSheet_Modal;
    }

    public COUIBottomSheetBehavior(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        int peekHeightValue;
        this.saveFlags = SAVE_NONE;
        this.fitToContents = true;
        this.updateImportantForAccessibilityOnSiblings = false;
        this.halfExpandedRatio = 0.5f;
        this.elevation = -1.0f;
        this.draggable = true;
        this.state = STATE_COLLAPSED;
        this.mPressDownState = STATE_COLLAPSED;
        this.mYVelocity = 0.0f;
        this.mLayoutAtMaxHeight = true;
        this.mLastOrientation = -1;
        this.mShakeHandMovingDirection = SHAKE_HAND_MOVING_DIRECTION_DEFAULT;
        this.mViewHeightType = 0;
        this.mParentRect = new Rect();
        this.mLayoutRect = new Rect();
        this.mHalfExpandOffsetUseParentRootViewHeight = true;
        this.callbacks = new ArrayList<>();
        this.mLastOffsetInFling = 0;
        this.alphaRadio = 0.0f;
        this.mDragFrequency = DEFAULT_PHYSICS_FREQUENCY;
        this.mDragDampingRatio = DEFAULT_PHYSICS_DAMPING_RATIO;
        this.mPhysicsEnable = false;
        this.mDragChild = null;
        this.mIsInTinyScreen = false;
        this.mIsHandlePanel = false;
        this.mBarRect = new Rect();
        this.mGlobalDrag = true;
        this.mIsNestedScrollingCheckEnabled = false;
        // Leapy removed 2026-07-24: BEGIN remove initialization of non-OPPO height-animation tokens.
        // Leapy end 2026-07-24: no token state exists in the decoded OPPO implementation.
        this.dragCallback = new COUIViewDragHelper.Callback() {
            private boolean releasedLow(View view) {
                int top = view.getTop();
                COUIBottomSheetBehavior cOUIBottomSheetBehavior = COUIBottomSheetBehavior.this;
                return top > (cOUIBottomSheetBehavior.parentHeight + cOUIBottomSheetBehavior.getExpandedOffset()) / 2;
            }

            @Override
            public int clampViewPositionHorizontal(View view, int left, int dx) {
                return view.getLeft();
            }

            @Override
            public int clampViewPositionVertical(View view, int top, int dy) {
                if (COUIBottomSheetBehavior.this.mPullUpListener != null) {
                    COUIBottomSheetBehavior.this.mPullUpListener.onDraggingPanel();
                }
                COUIBottomSheetBehavior cOUIBottomSheetBehavior = COUIBottomSheetBehavior.this;
                int pullOffset = 0;
                if (cOUIBottomSheetBehavior.state == STATE_DRAGGING) {
                    if (cOUIBottomSheetBehavior.isPanelHeightChangeAnimRunning()) {
                        COUIBottomSheetBehavior.this.mPanelHeightChangeAnim.cancel();
                    }
                    if (view.getTop() <= COUIBottomSheetBehavior.this.getExpandedOffset()) {
                        if (COUIBottomSheetBehavior.this.mPhysicsEnable && COUIBottomSheetBehavior.this.mDragBehavior.isDragging()) {
                            COUIBottomSheetBehavior.this.mDragBehavior.endDrag(0.0f);
                            COUIBottomSheetBehavior.this.mDragChild = null;
                        }
                        if (COUIBottomSheetBehavior.this.mPullUpListener != null && COUIBottomSheetBehavior.this.getExpandedOffset() > 0) {
                            COUIBottomSheetBehavior.this.mIsIgnoreExpandedOffsetChange = true;
                            if (dy < 0) {
                                dy = Math.max(dy, (view.getMeasuredHeight() - COUIBottomSheetBehavior.this.mPanelPaddingBottom) - COUIBottomSheetBehavior.this.mDialogMaxHeight);
                            }
                            if (dy != 0) {
                                pullOffset = COUIBottomSheetBehavior.this.mPullUpListener.onDragging(dy, COUIBottomSheetBehavior.this.getExpandedOffset());
                            }
                        }
                    } else {
                        int currentTop = view.getTop();
                        if (COUIBottomSheetBehavior.this.mPhysicsEnable) {
                            COUIBottomSheetBehavior.this.dragToNewTop(view, currentTop + dy);
                        } else if (COUIBottomSheetBehavior.this.getYVelocity() > 10000.0f) {
                            top = ((int) ((dy * 0.5f) + 0.5f)) + currentTop;
                        }
                    }
                }
                COUIBottomSheetBehavior.this.calculatePanelOutsideAlpha(view);
                int expandedOffset = COUIBottomSheetBehavior.this.getExpandedOffset() - pullOffset;
                COUIBottomSheetBehavior cOUIBottomSheetBehavior2 = COUIBottomSheetBehavior.this;
                return androidx.core.math.MathUtils.clamp(top, expandedOffset, cOUIBottomSheetBehavior2.hideable ? cOUIBottomSheetBehavior2.parentHeight : cOUIBottomSheetBehavior2.collapsedOffset);
            }

            @Override
            public int getViewVerticalDragRange(View view) {
                COUIBottomSheetBehavior cOUIBottomSheetBehavior = COUIBottomSheetBehavior.this;
                return cOUIBottomSheetBehavior.hideable ? cOUIBottomSheetBehavior.parentHeight : cOUIBottomSheetBehavior.collapsedOffset;
            }

            @Override
            public void onViewDragStateChanged(int state) {
                if (state == 1 && COUIBottomSheetBehavior.this.draggable) {
                    COUIBottomSheetBehavior.this.setStateInternal(STATE_DRAGGING);
                }
            }

            @Override
            public void onViewPositionChanged(View view, int left, int top, int dx, int dy) {
                COUIBottomSheetBehavior.this.dispatchOnSlide(top);
            }

            @Override
            public void onViewReleased(View view, float xvel, float yvel) {
                int top;
                if (COUIBottomSheetBehavior.this.mPhysicsEnable && COUIBottomSheetBehavior.this.mDragBehavior.isDragging()) {
                    COUIBottomSheetBehavior.this.mDragBehavior.endDrag(0.0f);
                    COUIBottomSheetBehavior.this.mDragChild = null;
                }
                boolean settled = false;
                COUIBottomSheetBehavior.this.mIsIgnoreExpandedOffsetChange = false;
                if (COUIBottomSheetBehavior.this.mPullUpListener != null) {
                    COUIBottomSheetBehavior.this.mPullUpListener.onReleasedDrag();
                    float ratio = view instanceof COUIPanelPercentFrameLayout ? ((COUIPanelPercentFrameLayout) view).getRatio() : 1.0f;
                    COUIBottomSheetBehavior cOUIBottomSheetBehavior = COUIBottomSheetBehavior.this;
                    if (((int) (((cOUIBottomSheetBehavior.parentHeight - cOUIBottomSheetBehavior.getMarginBottom(view)) / ratio) - ((view.getHeight() - COUIBottomSheetBehavior.this.mPanelPaddingBottom) / ratio))) <= COUIBottomSheetBehavior.this.getExpandedOffset() && view.getTop() < COUIBottomSheetBehavior.this.getExpandedOffset()) {
                        COUIBottomSheetBehavior.this.mPullUpListener.onReleased(COUIBottomSheetBehavior.this.getExpandedOffset());
                        return;
                    }
                }
                int targetState = 6;
                if (yvel < 0.0f) {
                    if (COUIBottomSheetBehavior.this.fitToContents) {
                        top = COUIBottomSheetBehavior.this.fitToContentsOffset;
                    } else {
                        int currentTop = view.getTop();
                        COUIBottomSheetBehavior cOUIBottomSheetBehavior2 = COUIBottomSheetBehavior.this;
                        int halfExpanded = cOUIBottomSheetBehavior2.halfExpandedOffset;
                        if (currentTop > halfExpanded) {
                            top = halfExpanded;
                        } else {
                            top = cOUIBottomSheetBehavior2.expandedOffset;
                        }
                    }
                    targetState = 3;
                } else {
                    COUIBottomSheetBehavior cOUIBottomSheetBehavior3 = COUIBottomSheetBehavior.this;
                    if (cOUIBottomSheetBehavior3.hideable && cOUIBottomSheetBehavior3.shouldHide(view, yvel)) {
                        COUIPanelDragListener cOUIPanelDragListener = COUIBottomSheetBehavior.this.mCOUIPanelDragListener;
                        if (cOUIPanelDragListener != null && cOUIPanelDragListener.onDragWhileEditing()) {
                            COUIBottomSheetBehavior cOUIBottomSheetBehavior4 = COUIBottomSheetBehavior.this;
                            int fitOffset = cOUIBottomSheetBehavior4.fitToContentsOffset;
                            cOUIBottomSheetBehavior4.mCanHideKeyboard = false;
                            top = fitOffset;
                            targetState = 3;
                        } else if ((Math.abs(xvel) < Math.abs(yvel) && yvel > 500.0f) || releasedLow(view)) {
                            COUIBottomSheetBehavior cOUIBottomSheetBehavior5 = COUIBottomSheetBehavior.this;
                            int parentHeight = cOUIBottomSheetBehavior5.parentRootViewHeight;
                            cOUIBottomSheetBehavior5.mCanHideKeyboard = true;
                            top = parentHeight;
                            targetState = 5;
                        } else if (COUIBottomSheetBehavior.this.fitToContents) {
                            top = COUIBottomSheetBehavior.this.fitToContentsOffset;
                            targetState = 3;
                        } else if (Math.abs(view.getTop() - COUIBottomSheetBehavior.this.expandedOffset) < Math.abs(view.getTop() - COUIBottomSheetBehavior.this.halfExpandedOffset)) {
                            top = COUIBottomSheetBehavior.this.expandedOffset;
                            targetState = 3;
                        } else {
                            top = COUIBottomSheetBehavior.this.halfExpandedOffset;
                            targetState = 3;
                        }
                        // Leapy modified 2026-07-30: BEGIN preserve the decoded
                        // OPPO release branch's hidden state.
                        //
                        // JADX placed a shared "state = expanded" assignment
                        // after this whole branch. That assignment also ran
                        // after the fast/low downward release selected the
                        // off-screen top, producing an off-screen panel whose
                        // logical target was still STATE_EXPANDED. Keep state 3
                        // only on the non-dismiss fallbacks; the dismiss path
                        // must retain STATE_HIDDEN so the dialog callback runs.
                        // Leapy end 2026-07-30: restore the original branch
                        // semantics instead of the decompiler's merged write.
                    } else if (yvel == 0.0f || Math.abs(xvel) > Math.abs(yvel)) {
                        int top2 = view.getTop();
                        if (!COUIBottomSheetBehavior.this.fitToContents) {
                            COUIBottomSheetBehavior cOUIBottomSheetBehavior6 = COUIBottomSheetBehavior.this;
                            int halfExpanded = cOUIBottomSheetBehavior6.halfExpandedOffset;
                            if (top2 < halfExpanded) {
                                if (top2 < Math.abs(top2 - cOUIBottomSheetBehavior6.collapsedOffset)) {
                                    top = COUIBottomSheetBehavior.this.expandedOffset;
                                    targetState = 3;
                                } else {
                                    top = COUIBottomSheetBehavior.this.halfExpandedOffset;
                                }
                            } else if (Math.abs(top2 - halfExpanded) < Math.abs(top2 - COUIBottomSheetBehavior.this.collapsedOffset)) {
                                top = COUIBottomSheetBehavior.this.halfExpandedOffset;
                            } else {
                                top = COUIBottomSheetBehavior.this.collapsedOffset;
                                targetState = 4;
                            }
                        } else if (Math.abs(top2 - COUIBottomSheetBehavior.this.fitToContentsOffset) < Math.abs(top2 - COUIBottomSheetBehavior.this.collapsedOffset)) {
                            top = COUIBottomSheetBehavior.this.fitToContentsOffset;
                            targetState = 3;
                        } else {
                            top = COUIBottomSheetBehavior.this.collapsedOffset;
                            targetState = 4;
                        }
                    } else {
                        if (COUIBottomSheetBehavior.this.fitToContents) {
                            COUIBottomSheetBehavior cOUIBottomSheetBehavior7 = COUIBottomSheetBehavior.this;
                            COUIPanelDragListener cOUIPanelDragListener2 = cOUIBottomSheetBehavior7.mCOUIPanelDragListener;
                            if (cOUIPanelDragListener2 == null) {
                                top = cOUIBottomSheetBehavior7.collapsedOffset;
                                targetState = 4;
                            } else if (cOUIPanelDragListener2.onDragWhileEditing()) {
                                top = COUIBottomSheetBehavior.this.fitToContentsOffset;
                                targetState = 3;
                            } else {
                                top = COUIBottomSheetBehavior.this.parentRootViewHeight;
                                targetState = 5;
                            }
                        } else {
                            int top3 = view.getTop();
                            COUIBottomSheetBehavior cOUIBottomSheetBehavior8 = COUIBottomSheetBehavior.this;
                            int halfExpanded = cOUIBottomSheetBehavior8.halfExpandedOffset;
                            if (top3 > halfExpanded && top3 < cOUIBottomSheetBehavior8.collapsedOffset) {
                                settled = true;
                            }
                            if (cOUIBottomSheetBehavior8.mPressDownState == STATE_HALF_EXPANDED && settled) {
                                top = cOUIBottomSheetBehavior8.collapsedOffset;
                                targetState = 4;
                            } else if (Math.abs(top3 - halfExpanded) < Math.abs(top3 - COUIBottomSheetBehavior.this.collapsedOffset)) {
                                top = COUIBottomSheetBehavior.this.halfExpandedOffset;
                                targetState = 6;
                            } else {
                                top = COUIBottomSheetBehavior.this.collapsedOffset;
                                targetState = 4;
                            }
                        }
                        // Leapy modified 2026-07-30: BEGIN restore the exact
                        // state branches from Settings.apk smali.
                        //
                        // COUIBottomSheetBehavior$7 keeps STATE_HIDDEN for a
                        // downward release when the panel listener permits
                        // dismissal, and keeps STATE_HALF_EXPANDED when that is
                        // the selected anchor. JADX incorrectly merged both
                        // paths into a final STATE_COLLAPSED assignment, which
                        // changed the spring target during slower releases and
                        // made the dismissal visibly snap instead of following
                        // OPPO's continuous velocity-driven curve.
                        // Leapy end 2026-07-30: do not merge distinct release
                        // states after their target coordinates are selected.
                    }
                }
                COUIBottomSheetBehavior.this.startSettlingAnimation(view, targetState, top, true);
            }

            @Override
            public boolean tryCaptureView(View view, int pointerId) {
                COUIBottomSheetBehavior cOUIBottomSheetBehavior = COUIBottomSheetBehavior.this;
                int targetState = cOUIBottomSheetBehavior.state;
                if (targetState == 1 || cOUIBottomSheetBehavior.touchingScrollingChild) {
                    return false;
                }
                if (targetState == 3 && cOUIBottomSheetBehavior.activePointerId == pointerId) {
                    WeakReference<View> weakReference = cOUIBottomSheetBehavior.nestedScrollingChildRef;
                    View view2 = weakReference != null ? weakReference.get() : null;
                    if (view2 != null && view2.canScrollVertically(-1)) {
                        return false;
                    }
                }
                WeakReference<V> weakReference2 = COUIBottomSheetBehavior.this.viewRef;
                return weakReference2 != null && weakReference2.get() == view;
            }
        };
        this.mContext = context;
        TypedArray typedArrayObtainStyledAttributes = context.obtainStyledAttributes(attributeSet, com.google.android.material.R.styleable.BottomSheetBehavior_Layout);
        this.shapeThemingEnabled = typedArrayObtainStyledAttributes.hasValue(com.google.android.material.R.styleable.BottomSheetBehavior_Layout_shapeAppearance);
        int top = com.google.android.material.R.styleable.BottomSheetBehavior_Layout_backgroundTint;
        boolean zHasValue = typedArrayObtainStyledAttributes.hasValue(top);
        if (zHasValue) {
            createMaterialShapeDrawable(context, attributeSet, zHasValue, MaterialResource.getColorStateList(context, typedArrayObtainStyledAttributes, top));
        } else {
            createMaterialShapeDrawable(context, attributeSet, zHasValue);
        }
        createShapeValueAnimator();
        this.elevation = typedArrayObtainStyledAttributes.getDimension(com.google.android.material.R.styleable.BottomSheetBehavior_Layout_android_elevation, -1.0f);
        int targetState = com.google.android.material.R.styleable.BottomSheetBehavior_Layout_behavior_peekHeight;
        TypedValue typedValuePeekValue = typedArrayObtainStyledAttributes.peekValue(targetState);
        if (typedValuePeekValue == null || (peekHeightValue = typedValuePeekValue.data) != -1) {
            setPanelPeekHeight(typedArrayObtainStyledAttributes.getDimensionPixelSize(targetState, -1));
        } else {
            setPanelPeekHeight(peekHeightValue);
        }
        setHideable(typedArrayObtainStyledAttributes.getBoolean(com.google.android.material.R.styleable.BottomSheetBehavior_Layout_behavior_hideable, false));
        setGestureInsetBottomIgnored(typedArrayObtainStyledAttributes.getBoolean(com.google.android.material.R.styleable.BottomSheetBehavior_Layout_gestureInsetBottomIgnored, false));
        setFitToContents(typedArrayObtainStyledAttributes.getBoolean(com.google.android.material.R.styleable.BottomSheetBehavior_Layout_behavior_fitToContents, true));
        setPanelSkipCollapsed(typedArrayObtainStyledAttributes.getBoolean(com.google.android.material.R.styleable.BottomSheetBehavior_Layout_behavior_skipCollapsed, false));
        setDraggable(typedArrayObtainStyledAttributes.getBoolean(com.google.android.material.R.styleable.BottomSheetBehavior_Layout_behavior_draggable, true));
        setSaveFlags(typedArrayObtainStyledAttributes.getInt(com.google.android.material.R.styleable.BottomSheetBehavior_Layout_behavior_saveFlags, -1));
        setHalfExpandedRatio(typedArrayObtainStyledAttributes.getFloat(com.google.android.material.R.styleable.BottomSheetBehavior_Layout_behavior_halfExpandedRatio, 0.5f));
        int halfExpanded = com.google.android.material.R.styleable.BottomSheetBehavior_Layout_behavior_expandedOffset;
        TypedValue typedValuePeekValue2 = typedArrayObtainStyledAttributes.peekValue(halfExpanded);
        if (typedValuePeekValue2 == null || typedValuePeekValue2.type != 16) {
            setExpandedOffset(typedArrayObtainStyledAttributes.getDimensionPixelOffset(halfExpanded, 0));
        } else {
            setExpandedOffset(typedValuePeekValue2.data);
        }
        typedArrayObtainStyledAttributes.recycle();
        this.maximumVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();
        this.mCanHideKeyboard = false;
    }

    private void addAccessibilityActionForState(V child, AccessibilityNodeInfoCompat.AccessibilityActionCompat action, final int state) {
        ViewCompat.replaceAccessibilityAction(child, action, null, new AccessibilityViewCommand() {
            @Override
            public boolean perform(View view, AccessibilityViewCommand.CommandArguments arguments) {
                COUIBottomSheetBehavior.this.setPanelState(state);
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
        if (DEBUG) {
            Log.d(TAG, "calculateHalfExpandedOffset: halfExpandedRatio=" + this.halfExpandedRatio + " halfExpandedOffset=" + this.halfExpandedOffset);
        }
        if (this.mHalfExpandOffsetUseParentRootViewHeight && this.mIsHandlePanel && this.halfExpandedRatio == 0.5f) {
            this.halfExpandedOffset = (this.parentRootViewHeight / 2) - this.parentMarginTop;
            if (DEBUG) {
                Log.d(TAG, "calculateHalfExpandedOffset: modified halfExpandedOffset=" + this.halfExpandedOffset);
            }
        }
        if (this.mIsHandlePanel) {
            this.halfExpandedOffset = Math.max(this.halfExpandedOffset, getExpandedOffset());
        }
    }


    public void calculatePanelOutsideAlpha(View view) {
        float top = 1.0f - ((view.getTop() - getExpandedOffset()) / this.parentHeight);
        this.alphaRadio = top;
        COUIPanelPullUpListener cOUIPanelPullUpListener = this.mPullUpListener;
        if (cOUIPanelPullUpListener != null) {
            cOUIPanelPullUpListener.onOffsetChanged(top);
        }
    }

    private int calculatePeekHeight() {
        return this.peekHeightAuto ? Math.max(this.peekHeightMin, this.parentHeight - ((this.parentWidth * 9) / 16)) : this.peekHeight;
    }

    private void checkOrientationChange() {
        int orientation = this.mContext.getResources().getConfiguration().orientation;
        int top = this.mLastOrientation;
        if (top != -1 && top != orientation && this.mStartHeightChangeAnim) {
            this.mStartHeightChangeAnim = false;
        }
        this.mLastOrientation = orientation;
    }

    private void createMaterialShapeDrawable(Context context, AttributeSet attributeSet, boolean withBackground) {
        createMaterialShapeDrawable(context, attributeSet, withBackground, null);
    }

    private void createPanelHeightChangeAnim() {
        FloatValueHolder dVar = new FloatValueHolder(0.0f);
        COUISpringForce cOUISpringForce = new COUISpringForce();
        this.mPanelHeightSpringForce = cOUISpringForce;
        cOUISpringForce.setBounce(SETTLE_ANIM_SPRING_BOUNCE);
        COUISpringAnimation spring = new COUISpringAnimation(dVar).setSpring(this.mPanelHeightSpringForce);
        this.mPanelHeightChangeAnim = spring;
        spring.addUpdateListener(new COUIDynamicAnimation.OnAnimationUpdateListener() {
            @Override
            public void onAnimationUpdate(COUIDynamicAnimation cOUIDynamicAnimation, float value, float velocity) {
                if (COUIBottomSheetBehavior.this.mViewHeightType == 0) {
                    COUIBottomSheetBehavior.this.panelHeightVerticalMoving(value);
                } else {
                    COUIBottomSheetBehavior.this.panelHeightAdaptive(cOUIDynamicAnimation, value, velocity);
                }
            }
        });
        this.mPanelHeightChangeAnim.addEndListener(new COUIDynamicAnimation.OnAnimationEndListener() {
            @Override
            public void onAnimationEnd(COUIDynamicAnimation cOUIDynamicAnimation, boolean canceled, float value, float velocity) {
                if (COUIBottomSheetBehavior.this.mViewHeightType == 0) {
                    COUIBottomSheetBehavior cOUIBottomSheetBehavior = COUIBottomSheetBehavior.this;
                    cOUIBottomSheetBehavior.setStateInternal(cOUIBottomSheetBehavior.mSettleTargetState);
                    COUIBottomSheetBehavior.this.viewDragHelper.setDragState(0);
                    return;
                }
                if (COUIBottomSheetBehavior.this.mPanelHeightChangeAnimListener != null) {
                    COUIBottomSheetBehavior.this.mPanelHeightChangeAnimListener.onAnimationEnd(cOUIDynamicAnimation, canceled, velocity);
                }
                // Leapy modified 2026-07-24: BEGIN restore decoded OPPO adaptive-height animation cleanup.
                COUIBottomSheetBehavior.this.mViewHeightType = 0;
                COUIBottomSheetBehavior.this.mStartHeightChangeAnim = false;
                // Leapy end 2026-07-24: always clear height-change mode after its spring finishes.
                WeakReference<V> weakReference = COUIBottomSheetBehavior.this.viewRef;
                if (weakReference == null || weakReference.get() == null) {
                    return;
                }
                COUIBottomSheetBehavior.this.setOutlineBottomOffset(0);
                COUIBottomSheetBehavior.this.viewRef.get().requestLayout();
            }
        });
    }

    private void createShapeValueAnimator() {
        ValueAnimator valueAnimatorOfFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        this.interpolatorAnimator = valueAnimatorOfFloat;
        valueAnimatorOfFloat.setDuration(CORNER_ANIMATION_DURATION);
        this.interpolatorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float fFloatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                if (COUIBottomSheetBehavior.this.materialShapeDrawable != null) {
                    COUIBottomSheetBehavior.this.materialShapeDrawable.setInterpolation(fFloatValue);
                }
            }
        });
    }


    public void dragToNewTop(View view, float newTop) {
        if (this.mDragBehavior.isDragging()) {
            this.mDragBehavior.dragTo(newTop);
            return;
        }
        this.mDragChild = view;
        float top = view.getTop();
        this.mDragValueHolder.setValue(top);
        this.mDragBehavior.beginDrag(top, top);
        this.mDragCurrentValue = top;
    }

    public static <V extends View> COUIBottomSheetBehavior<V> from(V view) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (!(layoutParams instanceof CoordinatorLayout.LayoutParams)) {
            throw new IllegalArgumentException("The view is not a child of CoordinatorLayout");
        }
        CoordinatorLayout.Behavior cVarF = ((CoordinatorLayout.LayoutParams) layoutParams).getBehavior();
        if (cVarF instanceof COUIBottomSheetBehavior) {
            return (COUIBottomSheetBehavior) cVarF;
        }
        throw new IllegalArgumentException("The view is not associated with COUIBottomSheetBehavior");
    }

    private Rect getLayoutRect(CoordinatorLayout coordinatorLayout, View view, int layoutDirection) {
        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) view.getLayoutParams();
        this.mParentRect.set(coordinatorLayout.getPaddingLeft() + ((ViewGroup.MarginLayoutParams) lp).leftMargin, coordinatorLayout.getPaddingTop() + ((ViewGroup.MarginLayoutParams) lp).topMargin, (coordinatorLayout.getWidth() - coordinatorLayout.getPaddingRight()) - ((ViewGroup.MarginLayoutParams) lp).rightMargin, (coordinatorLayout.getHeight() - coordinatorLayout.getPaddingBottom()) - ((ViewGroup.MarginLayoutParams) lp).bottomMargin);
        WindowInsetsCompat lastWindowInsets = coordinatorLayout.getLastWindowInsets();
        if (lastWindowInsets != null && ViewCompat.getFitsSystemWindows(coordinatorLayout) && !ViewCompat.getFitsSystemWindows(view)) {
            Insets systemBarInsets = lastWindowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            this.mParentRect.left += systemBarInsets.left;
            this.mParentRect.top += systemBarInsets.top;
            this.mParentRect.right -= systemBarInsets.right;
            this.mParentRect.bottom -= systemBarInsets.bottom;
        }
        GravityCompat.apply(resolveGravity(lp.gravity), view.getMeasuredWidth(), view.getMeasuredHeight(), this.mParentRect, this.mLayoutRect, layoutDirection);
        return this.mLayoutRect;
    }


    public int getMarginBottom(View view) {
        if (view != null) {
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
                return ((ViewGroup.MarginLayoutParams) layoutParams).bottomMargin;
            }
        }
        return 0;
    }

    private int getTargetTopForState(int state) {
        if (state == 3) {
            return getExpandedOffset();
        }
        if (state == 4) {
            return this.collapsedOffset;
        }
        if (state == 5) {
            return this.parentRootViewHeight;
        }
        if (state != 6) {
            return -1;
        }
        return this.halfExpandedOffset;
    }

    private View getVisiblePanelContentLayout(ViewGroup viewGroup) {
        if (viewGroup == null) {
            return null;
        }
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View childAt = viewGroup.getChildAt(i);
            if ((childAt instanceof COUIPanelContentLayout) && childAt.getVisibility() == 0) {
                return childAt;
            }
        }
        return null;
    }

    private int getWantTop(V child, int state) {
        float ratio;
        boolean hasAnchor;
        if (child instanceof COUIPanelPercentFrameLayout) {
            COUIPanelPercentFrameLayout cOUIPanelPercentFrameLayout = (COUIPanelPercentFrameLayout) child;
            ratio = cOUIPanelPercentFrameLayout.getRatio();
            hasAnchor = cOUIPanelPercentFrameLayout.getHasAnchor();
        } else {
            ratio = 1.0f;
            hasAnchor = false;
        }
        if (!this.mIsIgnoreExpandedOffsetChange) {
            int marginBottom = getMarginBottom(child);
            if (hasAnchor) {
                this.fitToContentsOffset = 0;
            } else {
                this.fitToContentsOffset = (int) Math.max(0.0f, ((this.parentHeight - marginBottom) / ratio) - ((state - this.mPanelPaddingBottom) / ratio));
            }
            if (this.mIsHandlePanel) {
                this.expandedOffset = this.fitToContentsOffset;
            }
        }
        return getExpandedOffset();
    }


    public float getYVelocity() {
        VelocityTracker velocityTracker = this.velocityTracker;
        if (velocityTracker == null) {
            return 0.0f;
        }
        velocityTracker.computeCurrentVelocity(1000, this.maximumVelocity);
        return this.velocityTracker.getYVelocity(this.activePointerId);
    }

    private boolean ifInTopOfMultiWindowMode() {
        Activity activityContextToActivity = UIUtil.contextToActivity(this.mContext);
        if (activityContextToActivity == null) {
            return false;
        }
        View decorView = activityContextToActivity.getWindow().getDecorView();
        int[] iArr = new int[2];
        decorView.getLocationInWindow(iArr);
        int top = iArr[1];
        decorView.getLocationOnScreen(iArr);
        return activityContextToActivity.isInMultiWindowMode() && iArr[1] == top;
    }

    private boolean isClickedOnBar(View view, int x, int y) {
        View viewFindViewById;
        if (!(view instanceof COUIPanelPercentFrameLayout) || (viewFindViewById = view.findViewById(com.coui.appcompat.R.id.panel_drag_bar)) == null) {
            return false;
        }
        viewFindViewById.getHitRect(this.mBarRect);
        return this.mBarRect.contains(x, y);
    }

    private boolean isImeVisible(View view) {
        try {
            WindowInsetsCompat rootWindowInsets = ViewCompat.getRootWindowInsets(view);
            if (rootWindowInsets != null) {
                return rootWindowInsets.isVisible(WindowInsetsCompat.Type.ime());
            }
            return false;
        } catch (Exception e2) {
            Log.e(TAG, "isImeVisible exception: " + e2.getMessage());
            return false;
        }
    }

    private boolean isInFreeFormModeWindowMode() {
        Activity activityContextToActivity = UIUtil.contextToActivity(this.mContext);
        return activityContextToActivity != null && COUIVersionUtil.checkOPlusViewSubSDK(SDK_VERSION_FOR_FLEXIBLE, SDK_SUB_VERSION_FOR_FLEXIBLE) && FlexibleWindowManager.getInstance().getFlexibleWindowState(activityContextToActivity) == 1;
    }

    private boolean isPanelCenterDisplay() {
        return (COUIPanelMultiWindowUtils.isSmallScreen(this.mContext, null) || this.mIsHandlePanel) ? false : true;
    }


    public void panelHeightAdaptive(COUIDynamicAnimation cOUIDynamicAnimation, float value, float velocity) {
        WeakReference<V> weakReference = this.viewRef;
        if (weakReference == null || weakReference.get() == null) {
            return;
        }
        int distance = Math.abs(this.mCurTop - this.mWantTop);
        float fAbs = Math.abs((value - this.mCurTop) / (distance == 0 ? 1 : distance));
        OnPanelHeightChangeAnimListener onPanelHeightChangeAnimListener = this.mPanelHeightChangeAnimListener;
        if (onPanelHeightChangeAnimListener != null) {
            onPanelHeightChangeAnimListener.onAnimationUpdate(cOUIDynamicAnimation, fAbs, velocity);
        }
        int animatedTop = (int) value;
        int dy = animatedTop - this.viewRef.get().getTop();
        if (dy != 0) {
            ViewCompat.offsetTopAndBottom(this.viewRef.get(), dy);
            if (isPanelCenterDisplay()) {
                int viewHeightType = this.mViewHeightType;
                if (viewHeightType == 3) {
                    setOutlineBottomOffset(Math.abs(getExpandedOffset() - animatedTop) * (-2));
                    this.viewRef.get().invalidateOutline();
                } else if (viewHeightType == 4) {
                    setOutlineBottomOffset(Math.abs(animatedTop - this.mWantTop) * (-2));
                    this.viewRef.get().invalidateOutline();
                }
            }
        }
    }


    public void panelHeightVerticalMoving(float animatedTop) {
        COUIViewDragHelper dragHelper = this.viewDragHelper;
        if (dragHelper == null || dragHelper.getCapturedView() == null) {
            return;
        }
        int targetTop = (int) animatedTop;
        if (this.mSettleTargetState == STATE_HALF_EXPANDED) {
            if (this.mShakeHandMovingDirection == SHAKE_HAND_MOVING_DIRECTION_UP
                    && targetTop < this.halfExpandedOffset
                    && this.mStartTopValue > this.halfExpandedOffset) {
                targetTop = (int) (this.halfExpandedOffset - ((this.halfExpandedOffset - targetTop) * SHAKE_HAND_MOVING_FACTOR));
            } else if (this.mShakeHandMovingDirection == SHAKE_HAND_MOVING_DIRECTION_DOWN
                    && targetTop > this.halfExpandedOffset
                    && this.mStartTopValue < this.halfExpandedOffset) {
                targetTop = (int) (this.halfExpandedOffset + ((targetTop - this.halfExpandedOffset) * SHAKE_HAND_MOVING_FACTOR));
            }
        } else if (this.mSettleTargetState == STATE_COLLAPSED) {
            if (this.mShakeHandMovingDirection == SHAKE_HAND_MOVING_DIRECTION_DOWN
                    && targetTop > this.collapsedOffset
                    && this.mStartTopValue < this.collapsedOffset) {
                targetTop = (int) (this.collapsedOffset + ((targetTop - this.collapsedOffset) * SHAKE_HAND_MOVING_FACTOR));
            }
        } else if (this.mSettleTargetState == STATE_EXPANDED
                && this.mShakeHandMovingDirection == SHAKE_HAND_MOVING_DIRECTION_UP
                && targetTop < getExpandedOffset()
                && this.mStartTopValue > getExpandedOffset()) {
            int expandedOffset = getExpandedOffset();
            targetTop = (int) (expandedOffset - ((expandedOffset - targetTop) * SHAKE_HAND_MOVING_FACTOR));
        }
        View capturedView = this.viewDragHelper.getCapturedView();
        calculatePanelOutsideAlpha(capturedView);
        int dy = targetTop - capturedView.getTop();
        if (dy != 0) {
            ViewCompat.offsetTopAndBottom(capturedView, dy);
            this.dragCallback.onViewPositionChanged(this.viewDragHelper.getCapturedView(), 0, targetTop, 0, dy);
        }
    }

    private void reset() {
        this.activePointerId = -1;
        VelocityTracker velocityTracker = this.velocityTracker;
        if (velocityTracker != null) {
            velocityTracker.recycle();
            this.velocityTracker = null;
        }
    }

    private int resolveGravity(int gravity) {
        if ((gravity & 7) == 0) {
            gravity |= 8388611;
        }
        return (gravity & 112) == 0 ? gravity | 48 : gravity;
    }

    private void restoreOptionalState(SavedState savedState) {
        int flags = this.saveFlags;
        if (flags == 0) {
            return;
        }
        if (flags == -1 || (flags & 1) == 1) {
            this.peekHeight = savedState.peekHeight;
        }
        if (flags == -1 || (flags & 2) == 2) {
            this.fitToContents = savedState.fitToContents;
        }
        if (flags == -1 || (flags & 4) == 4) {
            this.hideable = savedState.hideable;
        }
        if (flags == -1 || (flags & 8) == 8) {
            this.skipCollapsed = savedState.skipCollapsed;
        }
    }

    private void setFragmentPanelViewBottom(View view) {
        View viewFindViewById = view.findViewById(com.coui.appcompat.R.id.bottom_sheet_dialog);
        if (viewFindViewById == null) {
            return;
        }
        viewFindViewById.setBottom(view.getHeight() - view.getPaddingBottom());
        View viewFindViewById2 = viewFindViewById.findViewById(com.coui.appcompat.R.id.first_panel_container);
        if (viewFindViewById2 == null || !(viewFindViewById2 instanceof ViewGroup)) {
            return;
        }
        viewFindViewById2.setBottom(viewFindViewById.getHeight());
        View visiblePanelContentLayout = getVisiblePanelContentLayout((ViewGroup) viewFindViewById2);
        if (visiblePanelContentLayout != null) {
            visiblePanelContentLayout.setBottom(viewFindViewById2.getHeight());
        }
    }

    private void setNormalPanelViewBottom(View view, View view2) {
        view2.setBottom(view.getHeight() - view.getPaddingBottom());
    }


    public void setOutlineBottomOffset(int offset) {
        WeakReference<V> weakReference = this.viewRef;
        if (weakReference == null || !(weakReference.get() instanceof COUIPanelPercentFrameLayout)) {
            return;
        }
        ((COUIPanelPercentFrameLayout) this.viewRef.get()).setOutlineBottomOffset(offset);
    }

    private void setShakeHandMovingDirection(float dy) {
        if (dy > 100.0f) {
            this.mShakeHandMovingDirection = SHAKE_HAND_MOVING_DIRECTION_DOWN;
        } else if (dy < -100.0f) {
            this.mShakeHandMovingDirection = SHAKE_HAND_MOVING_DIRECTION_UP;
        } else {
            this.mShakeHandMovingDirection = SHAKE_HAND_MOVING_DIRECTION_DEFAULT;
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
                    COUIBottomSheetBehavior.this.settleToState(child, state);
                }
            });
        } else {
            settleToState(child, state);
        }
    }

    private void startHeightChangeAnimation(int from, int to) {
        OnPanelHeightChangeAnimListener onPanelHeightChangeAnimListener = this.mPanelHeightChangeAnimListener;
        if (onPanelHeightChangeAnimListener != null) {
            onPanelHeightChangeAnimListener.onAnimationStart();
        }
        // Leapy removed 2026-07-24: BEGIN remove non-OPPO animation-token capture.
        // Leapy end 2026-07-24: the decoded implementation starts the spring without token gating.
        this.mPanelHeightChangeAnim.setStartValue(from);
        this.mPanelHeightChangeAnim.animateToFinalPosition(to);
    }

    private void startPanelTranslateAnimation(final View view, int from, int to, float duration, PathInterpolator pathInterpolator) {
        ValueAnimator valueAnimatorOfFloat = ValueAnimator.ofFloat(from, to);
        valueAnimatorOfFloat.setDuration((long) duration);
        valueAnimatorOfFloat.setInterpolator(pathInterpolator);
        valueAnimatorOfFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int iFloatValue = (int) ((Float) valueAnimator.getAnimatedValue()).floatValue();
                view.offsetTopAndBottom(iFloatValue - COUIBottomSheetBehavior.this.mLastOffsetInFling);
                COUIBottomSheetBehavior.this.dispatchOnSlide(view.getTop());
                COUIBottomSheetBehavior.this.mLastOffsetInFling = iFloatValue;
                if (COUIBottomSheetBehavior.this.mPullUpListener != null) {
                    COUIBottomSheetBehavior.this.calculatePanelOutsideAlpha(view);
                }
            }
        });
        valueAnimatorOfFloat.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animator) {
                super.onAnimationEnd(animator);
                COUIBottomSheetBehavior.this.setStateInternal(STATE_HIDDEN);
            }
        });
        this.mLastOffsetInFling = view.getTop();
        view.offsetTopAndBottom(view.getTop());
        valueAnimatorOfFloat.start();
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
        if (this.hideable && this.state != STATE_HIDDEN) {
            addAccessibilityActionForState(child, AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_DISMISS, STATE_HIDDEN);
        }
        int currentState = this.state;
        if (currentState == 3) {
            addAccessibilityActionForState(child, AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_COLLAPSE, this.fitToContents ? STATE_COLLAPSED : STATE_HALF_EXPANDED);
            return;
        }
        if (currentState == 4) {
            addAccessibilityActionForState(child, AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_EXPAND, this.fitToContents ? STATE_EXPANDED : STATE_HALF_EXPANDED);
        } else {
            if (currentState != 6) {
                return;
            }
            addAccessibilityActionForState(child, AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_COLLAPSE, STATE_COLLAPSED);
            addAccessibilityActionForState(child, AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_EXPAND, STATE_EXPANDED);
        }
    }

    private void updateDrawableForTargetState(int state) {
        ValueAnimator valueAnimator;
        if (state == 2) {
            return;
        }
        boolean settled = state == 3;
        if (this.isShapeExpanded != settled) {
            this.isShapeExpanded = settled;
            if (this.materialShapeDrawable == null || (valueAnimator = this.interpolatorAnimator) == null) {
                return;
            }
            if (valueAnimator.isRunning()) {
                this.interpolatorAnimator.reverse();
                return;
            }
            float xvel = settled ? 0.0f : 1.0f;
            this.interpolatorAnimator.setFloatValues(1.0f - xvel, xvel);
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
            for (int i = 0; i < childCount; i++) {
                View childAt = coordinatorLayout.getChildAt(i);
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

    public void addBottomSheetCallback(COUIBottomSheetCallback cOUIBottomSheetCallback) {
        if (this.callbacks.contains(cOUIBottomSheetCallback)) {
            return;
        }
        this.callbacks.add(cOUIBottomSheetCallback);
    }

    public void applyPhysics(float frequency, float dampingRatio) {
        if (frequency == Float.MIN_VALUE || dampingRatio == Float.MIN_VALUE) {
            this.mPhysicsEnable = false;
            return;
        }
        this.mPhysicsEnable = true;
        this.mDragFrequency = frequency;
        this.mDragDampingRatio = dampingRatio;
        this.mPhysicalAnimator = PhysicalAnimator.create(this.mContext);
        this.mDragValueHolder = new com.oplus.physicsengine.engine.FloatValueHolder(0.0f);
        DragBehavior dragBehavior = new DragBehavior().withProperty(this.mDragValueHolder);
        dragBehavior.setSpringProperty(this.mDragFrequency, this.mDragDampingRatio);
        this.mDragBehavior = dragBehavior;
        this.mPhysicalAnimator.addBehavior(dragBehavior);
        this.mPhysicalAnimator.addAnimationListener(this.mDragBehavior, this);
        this.mPhysicalAnimator.addAnimationUpdateListener(this.mDragBehavior, this);
    }

    public void disableShapeAnimations() {
        this.interpolatorAnimator = null;
    }

    public void dispatchOnSlide(int top) {
        float slideOffsetNumerator;
        float slideOffsetDenominator;
        V child = this.viewRef.get();
        if (child == null || this.callbacks.isEmpty()) {
            return;
        }
        int collapsedOffset = this.collapsedOffset;
        if (top > collapsedOffset || collapsedOffset == getExpandedOffset()) {
            slideOffsetNumerator = collapsedOffset - top;
            slideOffsetDenominator = this.parentHeight - collapsedOffset;
        } else {
            slideOffsetNumerator = collapsedOffset - top;
            slideOffsetDenominator = collapsedOffset - getExpandedOffset();
        }
        float slideOffset = slideOffsetNumerator / slideOffsetDenominator;
        for (int i = 0; i < this.callbacks.size(); i++) {
            this.callbacks.get(i).onSlide(child, slideOffset);
        }
    }

    public View findScrollingChild(View view) {
        // Leapy modified 2026-07-24: BEGIN match decoded OPPO nested-scrolling child detection so the panel root remains draggable.
        if (ViewCompat.isNestedScrollingEnabled(view) && view.getVisibility() == 0) {
            return view;
        }
        // Leapy end 2026-07-24: only nested-scrolling descendants may block whole-panel drag capture.
        if (!(view instanceof ViewGroup) || view.getVisibility() != 0) {
            return null;
        }
        ViewGroup viewGroup = (ViewGroup) view;
        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View viewFindScrollingChild = findScrollingChild(viewGroup.getChildAt(i));
            if (viewFindScrollingChild != null) {
                return viewFindScrollingChild;
            }
        }
        return null;
    }

    public void forceSetPanelState(int state) {
        WeakReference<V> weakReference;
        int targetTopForState;
        if (isPanelHeightChangeAnimRunning() && this.mPanelHeightChangeAnim != null && (targetTopForState = getTargetTopForState(state)) != -1) {
            this.mSettleTargetState = state;
            this.mPanelHeightChangeAnim.animateToFinalPosition(targetTopForState);
            // Leapy removed 2026-07-24: BEGIN remove non-reference duplicate target-state assignment.
            // Leapy end 2026-07-24: the running spring is governed by mSettleTargetState.
            updateDrawableForTargetState(state);
            return;
        }
        if (this.state == STATE_SETTLING && (weakReference = this.viewRef) != null && weakReference.get() != null) {
            int top = this.viewRef.get().getTop();
            if (top <= getExpandedOffset()) {
                this.state = STATE_EXPANDED;
            } else if (top >= this.collapsedOffset) {
                this.state = STATE_COLLAPSED;
            } else if (Math.abs(top - this.halfExpandedOffset) < Math.abs(top - getExpandedOffset())) {
                this.state = 6;
            } else {
                this.state = STATE_EXPANDED;
            }
        }
        setPanelState(state);
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

    public int getTargetState() {
        return (this.state == STATE_SETTLING || isPanelHeightChangeAnimRunning()) ? this.mSettleTargetState : this.state;
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

    public boolean isPanelHeightChangeAnimRunning() {
        COUISpringAnimation cOUISpringAnimation = this.mPanelHeightChangeAnim;
        if (cOUISpringAnimation != null) {
            return cOUISpringAnimation.isRunning();
        }
        return false;
    }

    public void onAnimationCancel(BaseBehavior behavior) {
    }

    @Override
    public void onAnimationEnd(BaseBehavior behavior) {
    }

    @Override
    public void onAnimationStart(BaseBehavior behavior) {
    }

    @Override
    public void onAnimationUpdate(BaseBehavior behavior) {
        if (behavior.getAnimatedValue() != null) {
            this.mDragCurrentValue = ((Float) behavior.getAnimatedValue()).floatValue();
        }
        if (this.mDragChild != null) {
            ViewCompat.offsetTopAndBottom(this.mDragChild, -((int) (this.mDragChild.getTop() - this.mDragCurrentValue)));
            dispatchOnSlide(this.mDragChild.getTop());
        }
    }

    @Override
    public void onAttachedToLayoutParams(CoordinatorLayout.LayoutParams params) {
        super.onAttachedToLayoutParams(params);
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
        COUIViewDragHelper cOUIViewDragHelper;
        if (!child.isShown() || !this.draggable) {
            this.ignoreEvents = true;
            return false;
        }
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == 0) {
            COUISpringAnimation cOUISpringAnimation = this.mPanelHeightChangeAnim;
            this.mPressDownState = (cOUISpringAnimation == null || !cOUISpringAnimation.isRunning()) ? getState() : this.mSettleTargetState;
            reset();
        }
        if (this.velocityTracker == null) {
            this.velocityTracker = VelocityTracker.obtain();
        }
        this.velocityTracker.addMovement(motionEvent);
        if (actionMasked == 0) {
            this.initialX = (int) motionEvent.getX();
            int y = (int) motionEvent.getY();
            this.initialY = y;
            if (!this.mGlobalDrag && !isClickedOnBar(child, this.initialX, y)) {
                this.ignoreEvents = true;
                return false;
            }
            this.ignoreEvents = false;
            if (this.state != STATE_SETTLING) {
                OnNestedScrollingChild onNestedScrollingChild = this.mOnNestedScrollingChild;
                if (onNestedScrollingChild != null && (this.nestedScrollingChildRef == null || onNestedScrollingChild.getNestedScrollingChild() != this.nestedScrollingChildRef.get())) {
                    this.nestedScrollingChildRef = new WeakReference<>(this.mOnNestedScrollingChild.getNestedScrollingChild());
                }
                WeakReference<View> weakReference = this.nestedScrollingChildRef;
                View view = weakReference != null ? weakReference.get() : null;
                if (view != null && coordinatorLayout.isPointInChildBounds(view, this.initialX, this.initialY)) {
                    this.activePointerId = motionEvent.getPointerId(UIUtil.getAdjustmentPointerIndex(motionEvent, motionEvent.getActionIndex()));
                    this.touchingScrollingChild = true;
                }
            }
            this.ignoreEvents = this.activePointerId == -1 && !coordinatorLayout.isPointInChildBounds(child, this.initialX, this.initialY);
        } else if (actionMasked == 1) {
            COUIPanelPullUpListener cOUIPanelPullUpListener = this.mPullUpListener;
            if (cOUIPanelPullUpListener != null) {
                cOUIPanelPullUpListener.onCancel();
            }
        } else if (actionMasked == 3) {
            this.touchingScrollingChild = false;
            this.activePointerId = -1;
            if (this.ignoreEvents) {
                this.ignoreEvents = false;
                return false;
            }
        }
        if (!this.ignoreEvents && (cOUIViewDragHelper = this.viewDragHelper) != null && cOUIViewDragHelper.shouldInterceptTouchEvent(motionEvent)) {
            return true;
        }
        WeakReference<View> weakReference2 = this.nestedScrollingChildRef;
        View view2 = weakReference2 != null ? weakReference2.get() : null;
        return view2 != null ? (actionMasked != 2 || this.ignoreEvents || this.state == 1 || coordinatorLayout.isPointInChildBounds(view2, this.initialX, this.initialY) || this.viewDragHelper == null || Math.abs(((float) this.initialY) - motionEvent.getY()) <= ((float) this.viewDragHelper.getTouchSlop())) ? false : true : (actionMasked != 2 || this.ignoreEvents || this.state == 1 || this.viewDragHelper == null || Math.abs(((float) this.initialY) - motionEvent.getY()) <= ((float) this.viewDragHelper.getTouchSlop())) ? false : true;
    }

    @Override
    public boolean onLayoutChild(CoordinatorLayout coordinatorLayout, V child, int layoutDirection) {
        boolean hasAnchor;
        MaterialShapeDrawable gVar;
        // Leapy removed 2026-07-24: BEGIN remove the non-OPPO early return during STATE_HIDDEN settling.
        // Leapy end 2026-07-24: decoded COUI always completes the normal layout path while a hide spring runs.
        if (ViewCompat.getFitsSystemWindows(coordinatorLayout) && !ViewCompat.getFitsSystemWindows(child)) {
            child.setFitsSystemWindows(true);
        }
        float ratio = 1.0f;
        if (this.viewRef == null) {
            this.peekHeightMin = coordinatorLayout.getResources().getDimensionPixelSize(com.google.android.material.R.dimen.design_bottom_sheet_peek_height_min);
            this.mDialogMaxHeight = this.mContext.getResources().getDimensionPixelOffset(com.coui.appcompat.R.dimen.coui_panel_max_height);
            setSystemGestureInsets(coordinatorLayout);
            this.viewRef = new WeakReference<>(child);
            if (this.shapeThemingEnabled && (gVar = this.materialShapeDrawable) != null) {
                ViewCompat.setBackground(child, gVar);
            }
            MaterialShapeDrawable gVar2 = this.materialShapeDrawable;
            if (gVar2 != null) {
                float fT = this.elevation;
                if (fT == -1.0f) {
                    fT = (int) ViewCompat.getElevation(child);
                }
                gVar2.setElevation(fT);
                boolean settled = this.state == 3;
                this.isShapeExpanded = settled;
                this.materialShapeDrawable.setInterpolation(settled ? 0.0f : 1.0f);
            }
            updateAccessibilityActions();
            if (ViewCompat.getImportantForAccessibility(child) == 0) {
                ViewCompat.setImportantForAccessibility(child, 1);
            }
        }
        if (this.viewDragHelper == null) {
            this.viewDragHelper = COUIViewDragHelper.create(coordinatorLayout, this.dragCallback);
        }
        int savedTop = child.getTop();
        int viewHeightType = this.mViewHeightType;
        if (viewHeightType == 1 || viewHeightType == 3) {
            getLayoutRect(coordinatorLayout, child, layoutDirection);
            Rect rect = this.mLayoutRect;
            child.layout(rect.left, rect.top, rect.right, this.mLayoutBottom);
            View viewFindViewById = child.findViewById(com.coui.appcompat.R.id.coui_panel_content_layout);
            if (viewFindViewById != null) {
                if (viewFindViewById.getParent() == child) {
                    setNormalPanelViewBottom(child, viewFindViewById);
                } else {
                    setFragmentPanelViewBottom(child);
                }
            }
        } else {
            coordinatorLayout.onLayoutChild(child, layoutDirection);
        }
        this.parentWidth = coordinatorLayout.getWidth();
        this.parentHeight = coordinatorLayout.getHeight();
        if (ifInTopOfMultiWindowMode() && isImeVisible(child)) {
            this.parentRootViewHeight = Math.max(this.parentRootViewHeight, UIUtil.getScreenHeightMetrics(this.mContext));
        } else {
            this.parentRootViewHeight = coordinatorLayout.getRootView().getHeight();
        }
        this.parentMarginTop = COUIViewMarginUtil.getMargin(coordinatorLayout, 1);
        if (DEBUG) {
            Log.d(TAG, "onLayoutChild: parentHeight=" + this.parentHeight + " parentRootViewHeight=" + this.parentRootViewHeight + " marginTop=" + this.parentMarginTop);
        }
        if (child instanceof COUIPanelPercentFrameLayout) {
            COUIPanelPercentFrameLayout cOUIPanelPercentFrameLayout = (COUIPanelPercentFrameLayout) child;
            ratio = cOUIPanelPercentFrameLayout.getRatio();
            hasAnchor = cOUIPanelPercentFrameLayout.getHasAnchor();
        } else {
            hasAnchor = false;
        }
        if (!this.mIsIgnoreExpandedOffsetChange) {
            int marginBottom = getMarginBottom(child);
            if (hasAnchor) {
                this.fitToContentsOffset = 0;
            } else {
                this.fitToContentsOffset = (int) Math.max(0.0f, ((this.parentHeight - marginBottom) / ratio) - ((child.getHeight() - this.mPanelPaddingBottom) / ratio));
            }
            if (this.mIsHandlePanel) {
                this.expandedOffset = this.fitToContentsOffset;
            }
        }
        if (DEBUG) {
            Log.d(TAG, "updateFollowHandPanelLocation fitToContentsOffset:" + this.fitToContentsOffset + " expandOffset=" + this.expandedOffset + " mIsHandlePanel=" + this.mIsHandlePanel);
        }
        this.mIsIgnoreExpandedOffsetChange = false;
        calculateHalfExpandedOffset();
        calculateCollapsedOffset();
        int targetState = this.state;
        if (targetState == 3) {
            int halfExpanded = this.mViewHeightType;
            if (halfExpanded == 1) {
                ViewCompat.offsetTopAndBottom(child, isPanelHeightChangeAnimRunning() ? this.mCurTop : getExpandedOffset());
            } else if (halfExpanded == 2) {
                ViewCompat.offsetTopAndBottom(child, this.mCurTop);
            } else if (halfExpanded == 3) {
                ViewCompat.offsetTopAndBottom(child, isPanelHeightChangeAnimRunning() ? this.mCurTop : getExpandedOffset());
                if (isPanelHeightChangeAnimRunning()) {
                    setOutlineBottomOffset(Math.abs(getExpandedOffset() - this.mCurTop) * (-2));
                    child.invalidateOutline();
                }
            } else if (halfExpanded != 4) {
                ViewCompat.offsetTopAndBottom(child, getExpandedOffset());
            } else {
                ViewCompat.offsetTopAndBottom(child, this.mCurTop);
                setOutlineBottomOffset(Math.abs(this.mWantTop - this.mCurTop) * (-2));
                child.invalidateOutline();
            }
        } else if (targetState == 6) {
            ViewCompat.offsetTopAndBottom(child, this.halfExpandedOffset);
        } else if (this.hideable && targetState == 5) {
            ViewCompat.offsetTopAndBottom(child, this.parentHeight);
        } else if (targetState == 4) {
            ViewCompat.offsetTopAndBottom(child, this.collapsedOffset);
        } else if (targetState == 1 || targetState == 2) {
            ViewCompat.offsetTopAndBottom(child, savedTop - child.getTop());
        }
        if (DEBUG) {
            Log.e(TAG, "behavior parentHeight: " + this.parentHeight + " marginBottom: " + getMarginBottom(child) + "\n mDesignBottomSheetFrameLayout.getRatio()" + ratio + " fitToContentsOffset: " + this.fitToContentsOffset + " H: " + child.getMeasuredHeight() + "\n Y: " + child.getY() + " getExpandedOffset" + getExpandedOffset());
        }
        this.nestedScrollingChildRef = new WeakReference<>(findScrollingChild(child));
        return true;
    }

    @Override
    public boolean onMeasureChild(CoordinatorLayout coordinatorLayout, V child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
        boolean zOnMeasureChild = super.onMeasureChild(coordinatorLayout, child, parentWidthMeasureSpec, widthUsed, View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(parentHeightMeasureSpec) + child.getPaddingBottom(), View.MeasureSpec.getMode(parentHeightMeasureSpec)), heightUsed);
        int measuredHeight = child.getMeasuredHeight();
        // Leapy modified 2026-07-30: BEGIN keep the decoded OPPO height animation state
        // separate from the drag-to-hidden settling state.
        if (!this.mStartHeightChangeAnim || getState() != STATE_EXPANDED
                || this.mLayoutAtMaxHeight || measuredHeight == this.mLastMeasureHeight) {
            this.mViewHeightType = 0;
            this.mStartHeightChangeAnim = false;
        } else {
            if (this.mPanelHeightChangeAnim == null) {
                createPanelHeightChangeAnim();
            }
            this.mPanelHeightSpringForce.setResponse(SETTLE_ANIM_SPRING_RESPONSE);
            setOutlineBottomOffset(0);
            this.mCurTop = child.getTop();
            this.mWantTop = getWantTop(child, measuredHeight);
            int fitOffset = this.mLastMeasureHeight;
            if (measuredHeight < fitOffset) {
                this.mLayoutBottom = fitOffset;
                if (isPanelCenterDisplay()) {
                    this.mViewHeightType = 3;
                } else {
                    this.mViewHeightType = 1;
                }
                this.mPanelHeightChangeAnim.setStartValue(this.mCurTop);
                this.mPanelHeightChangeAnim.animateToFinalPosition(this.mWantTop);
            } else if (measuredHeight > fitOffset) {
                if (isPanelCenterDisplay()) {
                    this.mViewHeightType = 4;
                } else {
                    this.mViewHeightType = 2;
                }
                this.mPanelHeightChangeAnim.setStartValue(this.mCurTop);
                this.mPanelHeightChangeAnim.animateToFinalPosition(this.mWantTop);
            }
        }
        // Leapy end 2026-07-30: STATE_HIDDEN is committed by the normal settling
        // spring and observed by COUIBottomSheetDialogFragment.
        this.mLastMeasureHeight = measuredHeight;
        return zOnMeasureChild;
    }

    @Override
    public boolean onNestedPreFling(CoordinatorLayout coordinatorLayout, V child, View view, float velocityX, float velocityY) {
        WeakReference<View> weakReference;
        this.mYVelocity = -velocityY;
        if (this.mIsNestedScrollingCheckEnabled || (weakReference = this.nestedScrollingChildRef) == null || view != weakReference.get()) {
            return false;
        }
        return this.state != 3 || super.onNestedPreFling(coordinatorLayout, child, view, velocityX, velocityY);
    }

    @Override
    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, V child, View view, int dx, int dy, int[] iArr, int type) {
        if (type == 1 || this.mIsNestedScrollingCheckEnabled) {
            return;
        }
        WeakReference<View> weakReference = this.nestedScrollingChildRef;
        if (view != (weakReference != null ? weakReference.get() : null)) {
            return;
        }
        int top = child.getTop();
        int halfExpanded = top - dy;
        if (dy > 0) {
            if (halfExpanded < getExpandedOffset()) {
                iArr[1] = top - getExpandedOffset();
                calculatePanelOutsideAlpha(child);
                if (this.mPhysicsEnable) {
                    dragToNewTop(child, getExpandedOffset());
                } else {
                    ViewCompat.offsetTopAndBottom(child, -iArr[1]);
                }
                setStateInternal(3);
            } else {
                if (!this.draggable) {
                    return;
                }
                calculatePanelOutsideAlpha(child);
                iArr[1] = dy;
                if (this.mPhysicsEnable) {
                    dragToNewTop(child, halfExpanded);
                } else {
                    ViewCompat.offsetTopAndBottom(child, -dy);
                }
                setStateInternal(STATE_DRAGGING);
            }
        } else if (dy < 0 && !view.canScrollVertically(-1)) {
            if (halfExpanded > this.collapsedOffset && !this.hideable) {
                calculatePanelOutsideAlpha(child);
                int fitOffset = this.collapsedOffset;
                int parentHeight = top - fitOffset;
                iArr[1] = parentHeight;
                if (this.mPhysicsEnable) {
                    dragToNewTop(child, fitOffset);
                } else {
                    ViewCompat.offsetTopAndBottom(child, -parentHeight);
                }
                setStateInternal(4);
            } else {
                if (!this.draggable) {
                    return;
                }
                iArr[1] = dy;
                if (dy < -100) {
                    dy = (int) (dy * 0.5f);
                }
                calculatePanelOutsideAlpha(child);
                if (this.mPhysicsEnable) {
                    dragToNewTop(child, halfExpanded);
                } else {
                    ViewCompat.offsetTopAndBottom(child, -dy);
                }
                setStateInternal(STATE_DRAGGING);
            }
        }
        if (!this.mPhysicsEnable) {
            dispatchOnSlide(child.getTop());
        }
        this.lastNestedScrollDy = dy;
        this.nestedScrolled = true;
    }

    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, V child, View view, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type, int[] iArr) {
    }

    @Override
    public void onRestoreInstanceState(CoordinatorLayout coordinatorLayout, V child, Parcelable parcelable) {
        SavedState savedState = (SavedState) parcelable;
        super.onRestoreInstanceState(coordinatorLayout, child, savedState.getSuperState());
        restoreOptionalState(savedState);
        int saved = savedState.state;
        if (saved == 1 || saved == 2) {
            this.state = STATE_COLLAPSED;
        } else {
            this.state = saved;
        }
    }

    @Override
    public Parcelable onSaveInstanceState(CoordinatorLayout coordinatorLayout, V child) {
        return new SavedState(super.onSaveInstanceState(coordinatorLayout, child), (COUIBottomSheetBehavior<?>) this);
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, V child, View view, View view2, int axes, int type) {
        this.lastNestedScrollDy = 0;
        this.nestedScrolled = false;
        return (axes & 2) != 0;
    }

    @Override
    public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, V child, View view, int type) {
        int top;
        if (this.mPhysicsEnable && this.mDragBehavior.isDragging()) {
            this.mDragBehavior.endDrag(0.0f);
            this.mDragChild = null;
        }
        int targetState = 3;
        if (child.getTop() == getExpandedOffset()) {
            setStateInternal(3);
            return;
        }
        WeakReference<View> weakReference = this.nestedScrollingChildRef;
        if (weakReference != null && view == weakReference.get() && this.nestedScrolled) {
            if (this.lastNestedScrollDy <= 0) {
                if (this.hideable && shouldHide(child, getYVelocity())) {
                    COUIPanelDragListener cOUIPanelDragListener = this.mCOUIPanelDragListener;
                    if (cOUIPanelDragListener == null || !cOUIPanelDragListener.onDragWhileEditing()) {
                        top = this.parentRootViewHeight;
                        this.mCanHideKeyboard = true;
                        targetState = 5;
                    } else {
                        top = this.fitToContentsOffset;
                        this.mCanHideKeyboard = false;
                    }
                } else if (this.lastNestedScrollDy == 0) {
                    int currentTop = child.getTop();
                    if (!this.fitToContents) {
                        int halfExpanded = this.halfExpandedOffset;
                        if (currentTop < halfExpanded) {
                            if (currentTop < Math.abs(currentTop - this.collapsedOffset)) {
                                top = this.expandedOffset;
                            } else {
                                top = this.halfExpandedOffset;
                            }
                        } else if (Math.abs(currentTop - halfExpanded) < Math.abs(currentTop - this.collapsedOffset)) {
                            top = this.halfExpandedOffset;
                        } else {
                            top = this.collapsedOffset;
                            targetState = 4;
                        }
                        targetState = 6;
                    } else if (Math.abs(currentTop - this.fitToContentsOffset) < Math.abs(currentTop - this.collapsedOffset)) {
                        top = this.fitToContentsOffset;
                    } else {
                        top = this.collapsedOffset;
                        targetState = 4;
                    }
                } else {
                    if (this.fitToContents) {
                        COUIPanelDragListener cOUIPanelDragListener2 = this.mCOUIPanelDragListener;
                        if (cOUIPanelDragListener2 == null) {
                            top = this.collapsedOffset;
                        } else if (cOUIPanelDragListener2.onDragWhileEditing()) {
                            top = this.fitToContentsOffset;
                        } else {
                            top = this.parentRootViewHeight;
                            targetState = 5;
                        }
                    } else {
                        int top2 = child.getTop();
                        int fitOffset = this.halfExpandedOffset;
                        boolean settled = top2 > fitOffset && top2 < this.collapsedOffset;
                        if (!(this.mPressDownState == 6 && settled) && Math.abs(top2 - fitOffset) < Math.abs(top2 - this.collapsedOffset)) {
                            top = this.halfExpandedOffset;
                            targetState = 6;
                        } else {
                            top = this.collapsedOffset;
                        }
                    }
                    targetState = 4;
                }
            } else if (this.fitToContents) {
                top = this.fitToContentsOffset;
            } else {
                int top3 = child.getTop();
                int parentHeight = this.halfExpandedOffset;
                if (top3 > parentHeight) {
                    targetState = 6;
                    top = parentHeight;
                } else {
                    top = this.expandedOffset;
                }
            }
            startSettlingAnimation(child, targetState, top, false);
            this.nestedScrolled = false;
        }
    }

    @Override
    public boolean onTouchEvent(CoordinatorLayout coordinatorLayout, V child, MotionEvent motionEvent) {
        if (!child.isShown()) {
            return false;
        }
        int actionMasked = motionEvent.getActionMasked();
        if (this.state == 1 && actionMasked == 0) {
            return true;
        }
        COUIViewDragHelper cOUIViewDragHelper = this.viewDragHelper;
        if (cOUIViewDragHelper != null) {
            try {
                cOUIViewDragHelper.processTouchEvent(motionEvent);
            } catch (Exception e2) {
                e2.printStackTrace();
                return true;
            }
        }
        if (actionMasked == 0) {
            reset();
        }
        if (this.velocityTracker == null) {
            this.velocityTracker = VelocityTracker.obtain();
        }
        this.velocityTracker.addMovement(motionEvent);
        this.mYVelocity = getYVelocity();
        if (actionMasked == 2 && !this.ignoreEvents && this.viewDragHelper != null && Math.abs(this.initialY - motionEvent.getY()) > this.viewDragHelper.getTouchSlop()) {
            this.viewDragHelper.captureChildView(child, motionEvent.getPointerId(UIUtil.getAdjustmentPointerIndex(motionEvent, motionEvent.getActionIndex())));
        }
        return !this.ignoreEvents;
    }

    public void removeBottomSheetCallback(COUIBottomSheetCallback cOUIBottomSheetCallback) {
        this.callbacks.remove(cOUIBottomSheetCallback);
    }

    @Deprecated
    public void setBottomSheetCallback(COUIBottomSheetCallback cOUIBottomSheetCallback) {
        if (DEBUG) {
            Log.w(TAG, "BottomSheetBehavior now supports multiple callbacks. `setBottomSheetCallback()` removes all existing callbacks, including ones set internally by library authors, which may result in unintended behavior. This may change in the future. Please use `addBottomSheetCallback()` and `removeBottomSheetCallback()` instead to set your own callbacks.");
        }
        this.callbacks.clear();
        if (cOUIBottomSheetCallback != null) {
            this.callbacks.add(cOUIBottomSheetCallback);
        }
    }

    public void setCanHideKeyboard(boolean settled) {
        this.mCanHideKeyboard = settled;
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
        setStateInternal((this.fitToContents && this.state == 6) ? 3 : this.state);
        updateAccessibilityActions();
    }

    @Override
    public void setGestureInsetBottomIgnored(boolean gestureInsetBottomIgnored) {
        this.gestureInsetBottomIgnored = gestureInsetBottomIgnored;
    }

    public void setGlobalDrag(boolean settled) {
        this.mGlobalDrag = settled;
    }

    public void setHalfExpandOffsetUseParentRootViewHeight(boolean settled) {
        this.mHalfExpandOffsetUseParentRootViewHeight = settled;
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

    public void setHeightChangeAnim(boolean settled) {
        WeakReference<V> weakReference;
        // Leapy modified 2026-07-24: BEGIN match decoded OPPO height-change state handling.
        this.mStartHeightChangeAnim = settled;
        // Leapy end 2026-07-24: no synthetic generation token is used by the reference implementation.
        if (settled && isPanelCenterDisplay() && (weakReference = this.viewRef) != null && (weakReference.get() instanceof COUIPanelPercentFrameLayout)) {
            ((COUIPanelPercentFrameLayout) this.viewRef.get()).prepareForOutlineProvider();
        }
    }

    @Override
    @SuppressLint({"WrongConstant"})
    public void setHideable(boolean hideable) {
        if (this.hideable != hideable) {
            this.hideable = hideable;
            if (!hideable && this.state == STATE_HIDDEN) {
                setPanelState(4);
            }
            updateAccessibilityActions();
        }
    }

    public void setIsHandlePanel(boolean settled) {
        this.mIsHandlePanel = settled;
    }

    public void setIsInTinyScreen(boolean settled) {
        this.mIsInTinyScreen = settled;
    }

    public void setIsNestedScrollingCheckEnabled(boolean settled) {
        this.mIsNestedScrollingCheckEnabled = settled;
    }

    public void setLayoutAtMaxHeight(boolean settled) {
        this.mLayoutAtMaxHeight = settled;
    }

    public void setOnNestedScrollingChild(OnNestedScrollingChild onNestedScrollingChild) {
        this.mOnNestedScrollingChild = onNestedScrollingChild;
    }

    public void setOnPanelHeightChangeAnimListener(OnPanelHeightChangeAnimListener onPanelHeightChangeAnimListener) {
        this.mPanelHeightChangeAnimListener = onPanelHeightChangeAnimListener;
    }

    public void setPanelDragListener(COUIPanelDragListener cOUIPanelDragListener) {
        this.mCOUIPanelDragListener = cOUIPanelDragListener;
    }

    public void setPanelPaddingBottom(int paddingBottom) {
        this.mPanelPaddingBottom = paddingBottom;
    }

    public void setPanelPeekHeight(int peekHeight) {
        setPanelPeekHeight(peekHeight, false);
    }

    public void setPanelSkipCollapsed(boolean settled) {
        this.skipCollapsed = settled;
    }

    public void setPanelState(int state) {
        if (state == this.state) {
            return;
        }
        if (this.viewRef != null) {
            settleToStatePendingLayout(state);
            return;
        }
        if (state == 4 || state == 3 || state == 6 || (this.hideable && state == 5)) {
            this.state = state;
        }
    }

    public void setPullUpListener(COUIPanelPullUpListener cOUIPanelPullUpListener) {
        this.mPullUpListener = cOUIPanelPullUpListener;
    }

    public void setPullUpToDismissPanelListener(PullUpToDismissPanelListener pullUpToDismissPanelListener) {
        this.mPullUpToDismissPanelListener = pullUpToDismissPanelListener;
    }

    @Override
    public void setSaveFlags(int flags) {
        this.saveFlags = flags;
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
        if (state == 3) {
            updateImportantForAccessibility(true);
        } else if (state == 6 || state == 5 || state == 4) {
            updateImportantForAccessibility(false);
        }
        updateDrawableForTargetState(state);
        for (int top = 0; top < this.callbacks.size(); top++) {
            this.callbacks.get(top).onStateChanged(child, state);
        }
        updateAccessibilityActions();
    }

    public void setUpdateImportantForAccessibilityOnSiblings(boolean update) {
        this.updateImportantForAccessibilityOnSiblings = update;
    }

    public void settleToState(View view, int state) {
        int expandedOffset;
        int top;
        if (state == 4) {
            expandedOffset = this.collapsedOffset;
        } else if (state == 6) {
            expandedOffset = this.halfExpandedOffset;
            if (this.fitToContents && expandedOffset <= (top = this.fitToContentsOffset)) {
                state = 3;
                expandedOffset = top;
            }
        } else if (state == 3) {
            expandedOffset = getExpandedOffset();
        } else {
            if (!this.hideable || state != 5) {
                throw new IllegalArgumentException("Illegal state argument: " + state);
            }
            expandedOffset = this.parentRootViewHeight;
        }
        startSettlingAnimation(view, state, expandedOffset, false);
    }

    public boolean shouldHide(View view, float yvel) {
        if (this.skipCollapsed) {
            return true;
        }
        if (view.getTop() < this.collapsedOffset) {
            return false;
        }
        return Math.abs((((float) view.getTop()) + (yvel * HIDE_FRICTION)) - ((float) this.collapsedOffset)) / ((float) calculatePeekHeight()) > HIDE_THRESHOLD;
    }

    public void startSettleRunnable(View view, int state, int top) {
        if (this.mPanelHeightChangeAnim == null) {
            createPanelHeightChangeAnim();
        }
        if (state == 5) {
            float remainingDistance = this.parentHeight - view.getTop();
            float parentHeight = this.parentHeight;
            float responseFactor = 0.0f;
            if (remainingDistance > 0.0f && parentHeight != 0.0f) {
                responseFactor = remainingDistance / parentHeight;
            }
            this.mPanelHeightSpringForce.setResponse(0.18f + (0.19f * responseFactor));
        } else {
            this.mPanelHeightSpringForce.setResponse(SETTLE_ANIM_SPRING_RESPONSE);
        }
        if (this.mPanelHeightChangeAnim.isRunning()) {
            // Leapy modified 2026-07-30: BEGIN match decoded OPPO running-spring behavior.
            //
            // The shared spring's end listener reads mSettleTargetState. While it
            // is already running, OPPO only replaces that logical target and lets
            // the existing animation complete; it does not restart the spring.
            this.mSettleTargetState = state;
            // Leapy end 2026-07-30: preserve decoded OPPO spring ownership.
            return;
        }
        this.mSettleTargetState = state;
        COUIViewDragHelper cOUIViewDragHelper = this.viewDragHelper;
        if (cOUIViewDragHelper == null || cOUIViewDragHelper.getCapturedView() == null || this.viewDragHelper.getViewDragState() != 2) {
            setStateInternal(this.mSettleTargetState);
            return;
        }
        setShakeHandMovingDirection(this.mYVelocity);
        int top2 = this.viewDragHelper.getCapturedView().getTop();
        this.mStartTopValue = top2;
        this.mPanelHeightChangeAnim.setStartValue(top2);
        this.mPanelHeightChangeAnim.setStartVelocity(this.mYVelocity);
        this.mPanelHeightChangeAnim.animateToFinalPosition(top);
    }

    public void startSettlingAnimation(View view, int state, int top, boolean settleImmediately) {
        if ((settleImmediately && getState() == 1) ? this.viewDragHelper.settleCapturedViewAt(view.getLeft(), top) : this.viewDragHelper.smoothSlideViewTo(view, view.getLeft(), top)) {
            setStateInternal(2);
            updateDrawableForTargetState(state);
            getYVelocity();
            if (!this.mIsInTinyScreen) {
                if (state == 5 && isImeVisible(view) && isInFreeFormModeWindowMode()) {
                    top += UIUtil.getScreenHeightMetrics(this.mContext);
                }
                startSettleRunnable(view, state, top);
            } else if (state == 5) {
                startPanelTranslateAnimation(view, 0, this.mContext.getResources().getDimensionPixelOffset(com.coui.appcompat.R.dimen.coui_panel_max_height_tiny_screen), DEFAULT_TRANSLATE_HIDING_ANIMATOR_DURATION, new COUIOutEaseInterpolator());
            } else {
                startSettleRunnable(view, state, top);
            }
            // Leapy removed 2026-07-24: BEGIN remove non-reference duplicate hide-target bookkeeping.
            // Leapy end 2026-07-24: STATE_HIDDEN completion is dispatched from mSettleTargetState.
        } else {
            setStateInternal(state);
        }
        PullUpToDismissPanelListener pullUpToDismissPanelListener = this.mPullUpToDismissPanelListener;
        if (pullUpToDismissPanelListener == null || state != 5) {
            return;
        }
        pullUpToDismissPanelListener.onPullUpDismiss();
    }

    public void stopSettlingAnimationIfRunning() {
        COUISpringAnimation cOUISpringAnimation = this.mPanelHeightChangeAnim;
        if (cOUISpringAnimation == null || !cOUISpringAnimation.isRunning()) {
            return;
        }
        this.mPanelHeightChangeAnim.cancel();
    }

    private void createMaterialShapeDrawable(Context context, AttributeSet attributeSet, boolean withBackground, ColorStateList colorStateList) {
        if (this.shapeThemingEnabled) {
            this.shapeAppearanceModelDefault = ShapeAppearanceModel.builder(context, attributeSet, com.google.android.material.R.attr.bottomSheetStyle, DEF_STYLE_RES).build();
            MaterialShapeDrawable gVar = new MaterialShapeDrawable(this.shapeAppearanceModelDefault);
            this.materialShapeDrawable = gVar;
            gVar.initializeElevationOverlay(context);
            if (withBackground && colorStateList != null) {
                this.materialShapeDrawable.setFillColor(colorStateList);
                return;
            }
            TypedValue typedValue = new TypedValue();
            context.getTheme().resolveAttribute(R.attr.colorBackground, typedValue, true);
            this.materialShapeDrawable.setTint(typedValue.data);
        }
    }

    private void setPanelPeekHeight(int peekHeight, boolean animate) {
        V child;
        if (peekHeight == -1) {
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
            if (this.state != 4 || (child = this.viewRef.get()) == null) {
                return;
            }
            if (animate) {
                settleToStatePendingLayout(this.state);
            } else {
                child.requestLayout();
            }
        }
    }
}
