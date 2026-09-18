package com.coui.appcompat.lockview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.customview.widget.ExploreByTouchHelper;
import com.coui.appcompat.animation.COUIEaseInterpolator;
import com.coui.appcompat.animation.COUIInEaseInterpolator;
import com.coui.appcompat.contextutil.COUIContextUtil;
import com.coui.appcompat.darkmode.COUIDarkModeUtil;
import com.coui.appcompat.vibrateutil.VibrateUtils;
import com.coui.appcompat.R;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class COUILockPatternView extends View {
    public static final long ALPHA_DELAY = 166;
    public static final long ALPHA_DURATION = 167;
    public static final long ALPHA_OFFSET = 16;
    private static final int ASPECT_LOCK_HEIGHT = 2;
    private static final int ASPECT_LOCK_WIDTH = 1;
    private static final int ASPECT_SQUARE = 0;
    public static final boolean DEBUG_A11Y = false;
    private static final float DRAG_THRESHHOLD = 0.0f;
    private static final int FEEDBACK_MIN_SIZE = 1;
    private static final float MAX_ALPHA = 255.0f;
    private static final int MILLIS_PER_CIRCLE_ANIMATING = 700;
    private static final boolean PROFILE_DRAWING = false;
    private static final String TAG = "COUILockPatternView";
    public static final long TRANSLATE_Y_DURATION = 500;
    public static final long TRANSLATE_Y_OFFSET = 16;
    public static final int VIRTUAL_BASE_VIEW_ID = 1;
    private AccessibilityManager mAccessibilityManagerService;
    private Interpolator mAlphaInterpolator;
    private long mAnimatingPeriodStart;
    private final CellState[][] mCellStates;
    private Context mContext;
    private final Path mCurrentPath;
    private int mDefaultHeight;
    private int mDefaultWidth;
    private boolean mDrawingProfilingStarted;
    private boolean mEnableHapticFeedback;
    private int mErrorColor;
    private PatternExploreByTouchHelper mExploreByTouchHelper;
    private final Interpolator mFastOutSlowInInterpolator;
    private float mHitFactor;
    private float mInProgressX;
    private float mInProgressY;
    private boolean mInStealthMode;
    private Drawable mInnerDrawable;
    private boolean mInputEnabled;
    private final Rect mInvalidate;
    private boolean mIsLinearMotorVersion;
    private boolean mIsSetPassword;
    private int mMaxTranslateY;
    private OnPatternListener mOnPatternListener;
    private float mOuterCircleMaxAlpha;
    private Drawable mOuterDrawable;
    private final Paint mPaint;
    private float mPathAlpha;
    private final Paint mPathPaint;
    private final int mPathWidth;
    private final ArrayList<Cell> mPattern;
    private DisplayMode mPatternDisplayMode;
    private final boolean[][] mPatternDrawLookup;
    private boolean mPatternInProgress;
    private int mRegularColor;
    private float mSquareHeight;
    private float mSquareWidth;
    private int mStyle;
    private int mSuccessColor;
    private final Rect mTmpInvalidateRect;
    private Interpolator mTranslateYInterpolator;
    private AnimatorListenerAdapter mWongAnimatorListener;
    private ValueAnimator mWrongAnimator;

    public static final class Cell {
        private static final Cell[][] sCells = createCells();
        private final int column;
        private final int row;

        private Cell(int row, int column) {
            checkRange(row, column);
            this.row = row;
            this.column = column;
        }

        private static void checkRange(int row, int column) {
            if (row < 0 || row > 2) {
                throw new IllegalArgumentException("row must be in range 0-2");
            }
            if (column < 0 || column > 2) {
                throw new IllegalArgumentException("column must be in range 0-2");
            }
        }

        private static Cell[][] createCells() {
            Cell[][] cellArr = (Cell[][]) Array.newInstance((Class<?>) Cell.class, 3, 3);
            for (int row = 0; row < 3; row++) {
                for (int column = 0; column < 3; column++) {
                    cellArr[row][column] = new Cell(row, column);
                }
            }
            return cellArr;
        }

        public static Cell of(int row, int column) {
            checkRange(row, column);
            return sCells[row][column];
        }

        public int getColumn() {
            return this.column;
        }

        public int getRow() {
            return this.row;
        }

        public String toString() {
            return "(row=" + this.row + ",clmn=" + this.column + ")";
        }
    }

    public static class CellState {
        float alpha;
        OnCellDrawListener cellDrawListener;
        int col;
        float innerCircleAlpha;
        float innerCircleScale;
        public ValueAnimator lineAnimator;
        public float lineEndX = Float.MIN_VALUE;
        public float lineEndY = Float.MIN_VALUE;
        boolean needDrawCircle;
        float outerCircleAlpha;
        float outerCircleScale;
        float radius;
        int row;
        float translationX;
        float translationY;

        public void setCellDrawListener(OnCellDrawListener onCellDrawListener) {
            this.cellDrawListener = onCellDrawListener;
        }

        public void setCellNumberAlpha(float alpha) {
            this.alpha = alpha;
            this.cellDrawListener.drawCell();
        }

        public void setCellNumberTranslateX(int translationX) {
            this.translationX = translationX;
            this.cellDrawListener.drawCell();
        }

        public void setCellNumberTranslateY(int translationY) {
            this.translationY = translationY;
            this.cellDrawListener.drawCell();
        }
    }

    public enum DisplayMode {
        Correct,
        Animate,
        Wrong,
        FingerprintMatch,
        FingerprintNoMatch
    }

    public interface OnCellDrawListener {
        void drawCell();
    }

    public interface OnPatternListener {
        void onPatternCellAdded(List<Cell> list);

        void onPatternCleared();

        void onPatternDetected(List<Cell> list);

        void onPatternStart();
    }

    public final class PatternExploreByTouchHelper extends ExploreByTouchHelper {
        private final SparseArray<VirtualViewContainer> mItems;
        private Rect mTempRect;

        public class VirtualViewContainer {
            CharSequence description;

            public VirtualViewContainer(CharSequence charSequence) {
                this.description = charSequence;
            }
        }

        public PatternExploreByTouchHelper(View view) {
            super(view);
            this.mTempRect = new Rect();
            this.mItems = new SparseArray<>();
            for (int virtualViewId = 1; virtualViewId < 10; virtualViewId++) {
                this.mItems.put(virtualViewId, new VirtualViewContainer(getTextForVirtualView(virtualViewId)));
            }
        }

        private Rect getBoundsForVirtualView(int virtualViewId) {
            int cellIndex = virtualViewId - 1;
            Rect rect = this.mTempRect;
            int row = cellIndex / 3;
            float centerXForColumn = COUILockPatternView.this.getCenterXForColumn(cellIndex % 3);
            float centerYForRow = COUILockPatternView.this.getCenterYForRow(row);
            float halfHeight = COUILockPatternView.this.mSquareHeight * COUILockPatternView.this.mHitFactor * 0.5f;
            float halfWidth = COUILockPatternView.this.mSquareWidth * COUILockPatternView.this.mHitFactor * 0.5f;
            rect.left = (int) (centerXForColumn - halfWidth);
            rect.right = (int) (centerXForColumn + halfWidth);
            rect.top = (int) (centerYForRow - halfHeight);
            rect.bottom = (int) (centerYForRow + halfHeight);
            return rect;
        }

        private CharSequence getTextForVirtualView(int virtualViewId) {
            return COUILockPatternView.this.getResources().getString(R.string.lockscreen_access_pattern_cell_added_verbose, String.valueOf(virtualViewId));
        }

        private int getVirtualViewIdForHit(float x, float y) {
            int columnHit;
            int rowHit = COUILockPatternView.this.getRowHit(y);
            if (rowHit < 0 || (columnHit = COUILockPatternView.this.getColumnHit(x)) < 0) {
                return Integer.MIN_VALUE;
            }
            boolean partOfPattern = COUILockPatternView.this.mPatternDrawLookup[rowHit][columnHit];
            int virtualViewId = (rowHit * 3) + columnHit + 1;
            if (partOfPattern) {
                return virtualViewId;
            }
            return Integer.MIN_VALUE;
        }

        private boolean isClickable(int virtualViewId) {
            if (virtualViewId == Integer.MIN_VALUE || virtualViewId == Integer.MAX_VALUE) {
                return false;
            }
            int cellIndex = virtualViewId - 1;
            return !COUILockPatternView.this.mPatternDrawLookup[cellIndex / 3][cellIndex % 3];
        }

        @Override
        public int getVirtualViewAt(float x, float y) {
            return getVirtualViewIdForHit(x, y);
        }

        @Override
        public void getVisibleVirtualViews(List<Integer> list) {
            if (COUILockPatternView.this.mPatternInProgress) {
                for (int virtualViewId = 1; virtualViewId < 10; virtualViewId++) {
                    list.add(Integer.valueOf(virtualViewId));
                }
            }
        }

        public boolean onItemClicked(int virtualViewId) {
            invalidateVirtualView(virtualViewId);
            sendEventForVirtualView(virtualViewId, 1);
            return true;
        }

        @Override
        public boolean onPerformActionForVirtualView(int virtualViewId, int action, Bundle bundle) {
            if (action != 16) {
                return false;
            }
            return onItemClicked(virtualViewId);
        }

        @Override
        public void onPopulateAccessibilityEvent(View view, AccessibilityEvent accessibilityEvent) {
            super.onPopulateAccessibilityEvent(view, accessibilityEvent);
            if (COUILockPatternView.this.mPatternInProgress) {
                return;
            }
            accessibilityEvent.setContentDescription(COUILockPatternView.this.getContext().getText(R.string.lockscreen_access_pattern_area));
        }

        @Override
        public void onPopulateEventForVirtualView(int virtualViewId, AccessibilityEvent accessibilityEvent) {
            VirtualViewContainer virtualViewContainer = this.mItems.get(virtualViewId);
            if (virtualViewContainer != null) {
                accessibilityEvent.getText().add(virtualViewContainer.description);
            }
        }

        @Override
        public void onPopulateNodeForVirtualView(int virtualViewId, @NonNull AccessibilityNodeInfoCompat accessibilityNodeInfoCompat) {
            accessibilityNodeInfoCompat.setText(getTextForVirtualView(virtualViewId));
            accessibilityNodeInfoCompat.setContentDescription(getTextForVirtualView(virtualViewId));
            if (COUILockPatternView.this.mPatternInProgress) {
                accessibilityNodeInfoCompat.setFocusable(true);
                if (isClickable(virtualViewId)) {
                    accessibilityNodeInfoCompat.addAction(AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_CLICK);
                    accessibilityNodeInfoCompat.setClickable(isClickable(virtualViewId));
                }
            }
            accessibilityNodeInfoCompat.setBoundsInParent(getBoundsForVirtualView(virtualViewId));
        }
    }

    public COUILockPatternView(Context context) {
        this(context, null);
    }

    private void addCellToPattern(Cell cell) {
        this.mPatternDrawLookup[cell.getRow()][cell.getColumn()] = true;
        this.mPattern.add(cell);
        if (!this.mInStealthMode) {
            startCellActivatedAnimation(cell);
        }
        notifyCellAdded();
    }

    private float calculateLastSegmentAlpha(float x, float y, float lastX, float lastY) {
        float dx = x - lastX;
        float dy = y - lastY;
        return Math.min(1.0f, Math.max(0.0f, ((((float) Math.sqrt((dx * dx) + (dy * dy))) / this.mSquareWidth) - 0.3f) * 4.0f));
    }

    private void cancelLineAnimations() {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                CellState cellState = this.mCellStates[row][column];
                ValueAnimator valueAnimator = cellState.lineAnimator;
                if (valueAnimator != null) {
                    valueAnimator.cancel();
                    cellState.lineEndX = Float.MIN_VALUE;
                    cellState.lineEndY = Float.MIN_VALUE;
                }
            }
        }
    }

    private Cell checkForNewHit(float x, float y) {
        int columnHit;
        int rowHit = getRowHit(y);
        if (rowHit >= 0 && (columnHit = getColumnHit(x)) >= 0 && !this.mPatternDrawLookup[rowHit][columnHit]) {
            return Cell.of(rowHit, columnHit);
        }
        return null;
    }

    private void clearPatternDrawLookup() {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                this.mPatternDrawLookup[row][column] = false;
            }
        }
    }

    private Cell detectAndAddHit(float x, float y) {
        Cell cellCheckForNewHit = checkForNewHit(x, y);
        Cell cellOf = null;
        if (cellCheckForNewHit == null) {
            return null;
        }
        ArrayList<Cell> arrayList = this.mPattern;
        if (!arrayList.isEmpty()) {
            Cell cell = arrayList.get(arrayList.size() - 1);
            int rowDelta = cellCheckForNewHit.row - cell.row;
            int columnDelta = cellCheckForNewHit.column - cell.column;
            int fillRow = cell.row;
            int fillColumn = cell.column;
            if (Math.abs(rowDelta) == 2 && Math.abs(columnDelta) != 1) {
                fillRow = cell.row + (rowDelta > 0 ? 1 : -1);
            }
            if (Math.abs(columnDelta) == 2 && Math.abs(rowDelta) != 1) {
                fillColumn = cell.column + (columnDelta <= 0 ? -1 : 1);
            }
            cellOf = Cell.of(fillRow, fillColumn);
        }
        if (cellOf != null && !this.mPatternDrawLookup[cellOf.row][cellOf.column]) {
            addCellToPattern(cellOf);
        }
        addCellToPattern(cellCheckForNewHit);
        if (this.mEnableHapticFeedback) {
            performHitFeedback();
        }
        return cellCheckForNewHit;
    }

    private void drawCircle(Canvas canvas, float centerX, float centerY, float radius, boolean partOfPattern, float alpha) {
        this.mPaint.setColor(this.mRegularColor);
        this.mPaint.setAlpha((int) (alpha * MAX_ALPHA));
        canvas.drawCircle(centerX, centerY, radius, this.mPaint);
    }

    private void drawCircleDrawable(Canvas canvas, float centerX, float centerY, float innerScale, float innerAlpha, float outerScale, float outerAlpha) {
        canvas.save();
        int intrinsicWidth = this.mInnerDrawable.getIntrinsicWidth();
        float halfInner = intrinsicWidth / 2;
        int left = (int) (centerX - halfInner);
        int top = (int) (centerY - halfInner);
        canvas.scale(innerScale, innerScale, centerX, centerY);
        this.mInnerDrawable.setTint(getCurrentColor(true));
        this.mInnerDrawable.setBounds(left, top, left + intrinsicWidth, intrinsicWidth + top);
        this.mInnerDrawable.setAlpha((int) (innerAlpha * MAX_ALPHA));
        this.mInnerDrawable.draw(canvas);
        canvas.restore();
        canvas.save();
        int intrinsicWidth2 = this.mOuterDrawable.getIntrinsicWidth();
        float halfOuter = intrinsicWidth2 / 2;
        int outerLeft = (int) (centerX - halfOuter);
        int outerTop = (int) (centerY - halfOuter);
        canvas.scale(outerScale, outerScale, centerX, centerY);
        this.mOuterDrawable.setTint(getCurrentColor(true));
        this.mOuterDrawable.setBounds(outerLeft, outerTop, outerLeft + intrinsicWidth2, intrinsicWidth2 + outerTop);
        this.mOuterDrawable.setAlpha((int) (outerAlpha * MAX_ALPHA));
        this.mOuterDrawable.draw(canvas);
        canvas.restore();
    }

    public float getCenterXForColumn(int column) {
        float paddingLeft = getPaddingLeft();
        float squareWidth = this.mSquareWidth;
        return paddingLeft + (column * squareWidth) + (squareWidth / 2.0f);
    }

    public float getCenterYForRow(int row) {
        float paddingTop = getPaddingTop();
        float squareHeight = this.mSquareHeight;
        return paddingTop + (row * squareHeight) + (squareHeight / 2.0f);
    }

    public int getColumnHit(float x) {
        float squareWidth = this.mSquareWidth;
        float hitSize = this.mHitFactor * squareWidth;
        float paddingLeft = getPaddingLeft() + ((squareWidth - hitSize) / 2.0f);
        for (int column = 0; column < 3; column++) {
            float left = (column * squareWidth) + paddingLeft;
            if (x >= left && x <= left + hitSize) {
                return column;
            }
        }
        return -1;
    }

    private int getCurrentColor(boolean partOfPattern) {
        DisplayMode displayMode = this.mPatternDisplayMode;
        if (displayMode == DisplayMode.Wrong || displayMode == DisplayMode.FingerprintNoMatch) {
            return this.mErrorColor;
        }
        if (displayMode == DisplayMode.Correct || displayMode == DisplayMode.Animate || displayMode == DisplayMode.FingerprintMatch) {
            return this.mSuccessColor;
        }
        if (!partOfPattern || this.mInStealthMode || this.mPatternInProgress) {
            return this.mRegularColor;
        }
        throw new IllegalStateException("unknown display mode " + this.mPatternDisplayMode);
    }

    public int getRowHit(float y) {
        float squareHeight = this.mSquareHeight;
        float hitSize = this.mHitFactor * squareHeight;
        float paddingTop = getPaddingTop() + ((squareHeight - hitSize) / 2.0f);
        for (int row = 0; row < 3; row++) {
            float top = (row * squareHeight) + paddingTop;
            if (y >= top && y <= top + hitSize) {
                return row;
            }
        }
        return -1;
    }

    private void handleActionDown(MotionEvent motionEvent) {
        this.mPathAlpha = 1.0f;
        resetPattern();
        float x = motionEvent.getX();
        float y = motionEvent.getY();
        Cell cellDetectAndAddHit = detectAndAddHit(x, y);
        if (cellDetectAndAddHit != null) {
            setPatternInProgress(true);
            this.mPatternDisplayMode = DisplayMode.Correct;
            notifyPatternStarted();
        } else if (this.mPatternInProgress) {
            setPatternInProgress(false);
            notifyPatternCleared();
        }
        if (cellDetectAndAddHit != null) {
            float centerXForColumn = getCenterXForColumn(cellDetectAndAddHit.column);
            float centerYForRow = getCenterYForRow(cellDetectAndAddHit.row);
            float halfWidth = this.mSquareWidth / 2.0f;
            float halfHeight = this.mSquareHeight / 2.0f;
            invalidate((int) (centerXForColumn - halfWidth), (int) (centerYForRow - halfHeight), (int) (centerXForColumn + halfWidth), (int) (centerYForRow + halfHeight));
        }
        this.mInProgressX = x;
        this.mInProgressY = y;
    }

    private void handleActionMove(MotionEvent motionEvent) {
        float pathWidth = this.mPathWidth;
        int historySize = motionEvent.getHistorySize();
        this.mTmpInvalidateRect.setEmpty();
        int index = 0;
        boolean partOfPattern = false;
        while (index < historySize + 1) {
            float historicalX = index < historySize ? motionEvent.getHistoricalX(index) : motionEvent.getX();
            float historicalY = index < historySize ? motionEvent.getHistoricalY(index) : motionEvent.getY();
            Cell cellDetectAndAddHit = detectAndAddHit(historicalX, historicalY);
            int size = this.mPattern.size();
            if (cellDetectAndAddHit != null && size == 1) {
                setPatternInProgress(true);
                notifyPatternStarted();
            }
            float absDx = Math.abs(historicalX - this.mInProgressX);
            float absDy = Math.abs(historicalY - this.mInProgressY);
            if (absDx > 0.0f || absDy > 0.0f) {
                partOfPattern = true;
            }
            if (this.mPatternInProgress && size > 0) {
                Cell cell = this.mPattern.get(size - 1);
                float centerXForColumn = getCenterXForColumn(cell.column);
                float centerYForRow = getCenterYForRow(cell.row);
                float left = Math.min(centerXForColumn, historicalX) - pathWidth;
                float right = Math.max(centerXForColumn, historicalX) + pathWidth;
                float top = Math.min(centerYForRow, historicalY) - pathWidth;
                float bottom = Math.max(centerYForRow, historicalY) + pathWidth;
                if (cellDetectAndAddHit != null) {
                    float halfWidth = this.mSquareWidth * 0.5f;
                    float halfHeight = this.mSquareHeight * 0.5f;
                    float hitCenterX = getCenterXForColumn(cellDetectAndAddHit.column);
                    float hitCenterY = getCenterYForRow(cellDetectAndAddHit.row);
                    left = Math.min(hitCenterX - halfWidth, left);
                    right = Math.max(hitCenterX + halfWidth, right);
                    top = Math.min(hitCenterY - halfHeight, top);
                    bottom = Math.max(hitCenterY + halfHeight, bottom);
                }
                this.mTmpInvalidateRect.union(Math.round(left), Math.round(top), Math.round(right), Math.round(bottom));
            }
            index++;
        }
        this.mInProgressX = motionEvent.getX();
        this.mInProgressY = motionEvent.getY();
        if (partOfPattern) {
            this.mInvalidate.union(this.mTmpInvalidateRect);
            invalidate(this.mInvalidate);
            this.mInvalidate.set(this.mTmpInvalidateRect);
        }
    }

    private void handleActionUp() {
        if (this.mPattern.isEmpty()) {
            return;
        }
        setPatternInProgress(false);
        cancelLineAnimations();
        notifyPatternDetected();
        invalidate();
    }

    private void initCellAnim(CellState cellState, List<Animator> list, int animIndex) {
        cellState.setCellNumberAlpha(0.0f);
        cellState.setCellNumberTranslateY(this.mMaxTranslateY);
        ObjectAnimator objectAnimatorOfFloat = ObjectAnimator.ofFloat(cellState, "cellNumberAlpha", 0.0f,
                Color.alpha(this.mRegularColor) / MAX_ALPHA);
        long alphaDelay = ALPHA_DELAY + (((long) animIndex) * ALPHA_OFFSET);
        long translateDelay = ((long) animIndex) * TRANSLATE_Y_OFFSET;
        objectAnimatorOfFloat.setStartDelay(alphaDelay);
        objectAnimatorOfFloat.setDuration(ALPHA_DURATION);
        objectAnimatorOfFloat.setInterpolator(this.mAlphaInterpolator);
        list.add(objectAnimatorOfFloat);
        ObjectAnimator objectAnimatorOfInt = ObjectAnimator.ofInt(cellState, "cellNumberTranslateY", this.mMaxTranslateY, 0);
        objectAnimatorOfInt.setStartDelay(translateDelay);
        objectAnimatorOfInt.setDuration(TRANSLATE_Y_DURATION);
        objectAnimatorOfInt.setInterpolator(this.mTranslateYInterpolator);
        list.add(objectAnimatorOfInt);
    }

    private void notifyCellAdded() {
        OnPatternListener onPatternListener = this.mOnPatternListener;
        if (onPatternListener != null) {
            onPatternListener.onPatternCellAdded(this.mPattern);
        }
        this.mExploreByTouchHelper.invalidateRoot();
    }

    private void notifyPatternCleared() {
        sendAccessEvent(R.string.lockscreen_access_pattern_cleared);
        OnPatternListener onPatternListener = this.mOnPatternListener;
        if (onPatternListener != null) {
            onPatternListener.onPatternCleared();
        }
    }

    private void notifyPatternDetected() {
        sendAccessEvent(R.string.lockscreen_access_pattern_detected);
        OnPatternListener onPatternListener = this.mOnPatternListener;
        if (onPatternListener != null) {
            onPatternListener.onPatternDetected(this.mPattern);
        }
    }

    private void notifyPatternStarted() {
        sendAccessEvent(R.string.lockscreen_access_pattern_start);
        OnPatternListener onPatternListener = this.mOnPatternListener;
        if (onPatternListener != null) {
            onPatternListener.onPatternStart();
        }
    }

    private void performHitFeedback() {
        if (this.mIsLinearMotorVersion) {
            performHapticFeedback(302);
        } else {
            performHapticFeedback(1);
        }
    }

    private void performWrongModeFeedback() {
        if (this.mEnableHapticFeedback) {
            if (this.mIsLinearMotorVersion) {
                performHapticFeedback(304, 3);
            } else {
                performHapticFeedback(300, 3);
            }
        }
    }

    public void resetPattern() {
        this.mPattern.clear();
        clearPatternDrawLookup();
        this.mPatternDisplayMode = DisplayMode.Correct;
        invalidate();
    }

    private int resolveMeasured(int measureSpec, int desired) {
        int size = View.MeasureSpec.getSize(measureSpec);
        int mode = View.MeasureSpec.getMode(measureSpec);
        return mode != Integer.MIN_VALUE ? mode != 0 ? size : desired : Math.max(size, desired);
    }

    private void sendAccessEvent(int resId) {
        announceForAccessibility(this.mContext.getString(resId));
    }

    private void setPatternInProgress(boolean inProgress) {
        this.mPatternInProgress = inProgress;
        this.mExploreByTouchHelper.invalidateRoot();
    }

    private void startCellActivatedAnimation(Cell cell) {
        CellState cellState = this.mCellStates[cell.row][cell.column];
        startOuterAnimation(cellState);
        startInnerAnimation(cellState);
        startLineEndAnimation(cellState, this.mInProgressX, this.mInProgressY, getCenterXForColumn(cell.column), getCenterYForRow(cell.row));
    }

    private void startFingerprintNoMatchAnimator() {
        ValueAnimator valueAnimatorOfPropertyValuesHolder = ValueAnimator.ofPropertyValuesHolder(PropertyValuesHolder.ofKeyframe("pathAlpha", Keyframe.ofFloat(0.0f, 1.0f), Keyframe.ofFloat(0.2f, 0.35f), Keyframe.ofFloat(0.4f, 1.0f), Keyframe.ofFloat(0.6f, 0.15f), Keyframe.ofFloat(0.8f, 0.5f), Keyframe.ofFloat(1.0f, 0.0f)));
        valueAnimatorOfPropertyValuesHolder.setDuration(1000L);
        valueAnimatorOfPropertyValuesHolder.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                for (int row = 0; row < 3; row++) {
                    for (int column = 0; column < 3; column++) {
                        CellState cellState = COUILockPatternView.this.mCellStates[row][column];
                        float fFloatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                        cellState.innerCircleAlpha = fFloatValue;
                        cellState.needDrawCircle = fFloatValue <= 0.1f;
                    }
                }
                COUILockPatternView.this.invalidate();
            }
        });
        valueAnimatorOfPropertyValuesHolder.start();
    }

    private void startInnerAnimation(final CellState cellState) {
        ValueAnimator valueAnimatorOfFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        valueAnimatorOfFloat.setInterpolator(new COUIEaseInterpolator());
        valueAnimatorOfFloat.setDuration(230L);
        valueAnimatorOfFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                cellState.innerCircleAlpha = ((Float) valueAnimator.getAnimatedValue()).floatValue();
            }
        });
        valueAnimatorOfFloat.start();
    }

    private void startLineEndAnimation(final CellState cellState, final float startX, final float startY, final float endX, final float endY) {
        ValueAnimator valueAnimatorOfFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        valueAnimatorOfFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float fFloatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                CellState state = cellState;
                float remaining = 1.0f - fFloatValue;
                state.lineEndX = (startX * remaining) + (endX * fFloatValue);
                state.lineEndY = (remaining * startY) + (fFloatValue * endY);
                COUILockPatternView.this.invalidate();
            }
        });
        valueAnimatorOfFloat.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animator) {
                cellState.lineAnimator = null;
            }
        });
        valueAnimatorOfFloat.setInterpolator(this.mFastOutSlowInInterpolator);
        valueAnimatorOfFloat.setDuration(100L);
        valueAnimatorOfFloat.start();
        cellState.lineAnimator = valueAnimatorOfFloat;
    }

    private void startOuterAnimation(final CellState cellState) {
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(460L);
        animatorSet.setInterpolator(new COUIInEaseInterpolator());
        ValueAnimator valueAnimatorOfFloat = ValueAnimator.ofFloat(1.0f, 7.0f);
        valueAnimatorOfFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                cellState.outerCircleScale = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                COUILockPatternView.this.invalidate();
            }
        });
        ValueAnimator valueAnimatorOfPropertyValuesHolder = ValueAnimator.ofPropertyValuesHolder(PropertyValuesHolder.ofKeyframe("alpha", Keyframe.ofFloat(0.0f, 0.0f), Keyframe.ofFloat(0.5f, this.mOuterCircleMaxAlpha), Keyframe.ofFloat(1.0f, 0.0f)));
        valueAnimatorOfPropertyValuesHolder.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                cellState.outerCircleAlpha = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                COUILockPatternView.this.invalidate();
            }
        });
        animatorSet.play(valueAnimatorOfFloat).with(valueAnimatorOfPropertyValuesHolder);
        animatorSet.start();
    }

    private void startWrongAnimator() {
        ValueAnimator valueAnimatorOfPropertyValuesHolder = ValueAnimator.ofPropertyValuesHolder(PropertyValuesHolder.ofKeyframe("pathAlpha", Keyframe.ofFloat(0.0f, 1.0f), Keyframe.ofFloat(0.2f, 0.35f), Keyframe.ofFloat(0.4f, 1.0f), Keyframe.ofFloat(0.6f, 0.15f), Keyframe.ofFloat(0.8f, 0.5f), Keyframe.ofFloat(1.0f, 0.0f)));
        this.mWrongAnimator = valueAnimatorOfPropertyValuesHolder;
        valueAnimatorOfPropertyValuesHolder.setDuration(1000L);
        this.mWrongAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                COUILockPatternView.this.mPathAlpha = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                for (Cell cell : COUILockPatternView.this.mPattern) {
                    CellState cellState = COUILockPatternView.this.mCellStates[cell.row][cell.column];
                    cellState.innerCircleAlpha = COUILockPatternView.this.mPathAlpha;
                    cellState.needDrawCircle = COUILockPatternView.this.mPathAlpha <= 0.1f;
                }
                COUILockPatternView.this.invalidate();
            }
        });
        this.mWrongAnimator.start();
    }

    @Deprecated
    public void clearPattern(boolean partOfPattern) {
    }

    public void disableInput() {
        this.mInputEnabled = false;
    }

    @Override
    public boolean dispatchHoverEvent(MotionEvent motionEvent) {
        return this.mExploreByTouchHelper.dispatchHoverEvent(motionEvent) | super.dispatchHoverEvent(motionEvent);
    }

    public void enableInput() {
        this.mInputEnabled = true;
    }

    public CellState[][] getCellStates() {
        return this.mCellStates;
    }

    public AnimatorSet getEnterAnim() {
        AnimatorSet animatorSet = new AnimatorSet();
        ArrayList arrayList = new ArrayList();
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                initCellAnim(this.mCellStates[row][column], arrayList, (row * 3) + column);
            }
        }
        animatorSet.playTogether(arrayList);
        return animatorSet;
    }

    @Deprecated
    public Animator getFailAnimator() {
        return ValueAnimator.ofFloat(0.0f, 1.0f);
    }

    @Deprecated
    public Animator getSuccessAnimator() {
        return ValueAnimator.ofInt((int) MAX_ALPHA, 0);
    }

    public boolean isInStealthMode() {
        return this.mInStealthMode;
    }

    public boolean isSetLockPassword() {
        return this.mIsSetPassword;
    }

    public boolean isTactileFeedbackEnabled() {
        return this.mEnableHapticFeedback;
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ValueAnimator valueAnimator = this.mWrongAnimator;
        if (valueAnimator != null) {
            valueAnimator.removeAllUpdateListeners();
            this.mWrongAnimator.removeAllListeners();
            this.mWrongAnimator = null;
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        ArrayList<Cell> arrayList = this.mPattern;
        int size = arrayList.size();
        boolean[][] zArr = this.mPatternDrawLookup;
        if (this.mPatternDisplayMode == DisplayMode.Animate) {
            int elapsed = (int) (SystemClock.elapsedRealtime() - this.mAnimatingPeriodStart) % ((size + 1) * MILLIS_PER_CIRCLE_ANIMATING);
            int numCircles = elapsed / MILLIS_PER_CIRCLE_ANIMATING;
            clearPatternDrawLookup();
            for (int index = 0; index < numCircles; index++) {
                Cell cell = arrayList.get(index);
                zArr[cell.getRow()][cell.getColumn()] = true;
            }
            if (numCircles > 0 && numCircles < size) {
                float x = (elapsed % MILLIS_PER_CIRCLE_ANIMATING) / (float) MILLIS_PER_CIRCLE_ANIMATING;
                Cell cell = arrayList.get(numCircles - 1);
                float centerXForColumn = getCenterXForColumn(cell.column);
                float centerYForRow = getCenterYForRow(cell.row);
                Cell nextCell = arrayList.get(numCircles);
                float y = (getCenterXForColumn(nextCell.column) - centerXForColumn) * x;
                float centerX = x * (getCenterYForRow(nextCell.row) - centerYForRow);
                this.mInProgressX = centerXForColumn + y;
                this.mInProgressY = centerYForRow + centerX;
            }
            invalidate();
        }
        Path path = this.mCurrentPath;
        path.rewind();
        if (!this.mInStealthMode) {
            this.mPathPaint.setColor(getCurrentColor(true));
            this.mPathPaint.setAlpha((int) (this.mPathAlpha * MAX_ALPHA));
            float hitCenterX = 0.0f;
            float hitCenterY = 0.0f;
            boolean partOfPattern = false;
            for (int index = 0; index < size; index++) {
                Cell cell = arrayList.get(index);
                if (!zArr[cell.row][cell.column]) {
                    break;
                }
                hitCenterX = getCenterXForColumn(cell.column);
                hitCenterY = getCenterYForRow(cell.row);
                if (index == 0) {
                    path.rewind();
                    path.moveTo(hitCenterX, hitCenterY);
                } else {
                    CellState cellState = this.mCellStates[cell.row][cell.column];
                    float x = cellState.lineEndX;
                    float y = cellState.lineEndY;
                    if (x != Float.MIN_VALUE && y != Float.MIN_VALUE) {
                        path.lineTo(x, y);
                    } else {
                        path.lineTo(hitCenterX, hitCenterY);
                    }
                }
                partOfPattern = true;
            }
            if ((this.mPatternInProgress || this.mPatternDisplayMode == DisplayMode.Animate) && partOfPattern) {
                path.moveTo(hitCenterX, hitCenterY);
                path.lineTo(this.mInProgressX, this.mInProgressY);
            }
            canvas.drawPath(path, this.mPathPaint);
        }
        for (int row = 0; row < 3; row++) {
            float hitCenterY = getCenterYForRow(row);
            for (int column = 0; column < 3; column++) {
                CellState cellState = this.mCellStates[row][column];
                float hitCenterX = getCenterXForColumn(column);
                float translationY = cellState.translationY;
                float translationX = cellState.translationX;
                boolean partOfPattern = zArr[row][column];
                if (partOfPattern || this.mPatternDisplayMode == DisplayMode.FingerprintNoMatch) {
                    drawCircleDrawable(canvas, ((int) hitCenterX) + translationX, ((int) hitCenterY) + translationY, cellState.innerCircleScale, cellState.innerCircleAlpha, cellState.outerCircleScale, cellState.outerCircleAlpha);
                }
                if (cellState.needDrawCircle) {
                    drawCircle(canvas, ((int) hitCenterX) + translationX, ((int) hitCenterY) + translationY, cellState.radius, partOfPattern, cellState.alpha);
                }
            }
        }
    }

    @Override
    public boolean onHoverEvent(MotionEvent motionEvent) {
        if (this.mAccessibilityManagerService.isTouchExplorationEnabled()) {
            int action = motionEvent.getAction();
            if (action == 7) {
                motionEvent.setAction(2);
            } else if (action == 9) {
                motionEvent.setAction(0);
            } else if (action == 10) {
                motionEvent.setAction(1);
            }
            onTouchEvent(motionEvent);
            motionEvent.setAction(action);
        }
        return super.onHoverEvent(motionEvent);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int mode = View.MeasureSpec.getMode(widthMeasureSpec);
        int size = View.MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = View.MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = View.MeasureSpec.getSize(heightMeasureSpec);
        if (mode == Integer.MIN_VALUE) {
            size = this.mDefaultWidth;
        }
        if (heightMode == Integer.MIN_VALUE) {
            heightSize = this.mDefaultHeight;
        }
        setMeasuredDimension(size, heightSize);
    }

    @Override
    public void onRestoreInstanceState(Parcelable parcelable) {
        SavedState savedState = (SavedState) parcelable;
        super.onRestoreInstanceState(savedState.getSuperState());
        setPattern(DisplayMode.Correct, COUILockPatternUtils.stringToPattern(savedState.getSerializedPattern()));
        this.mPatternDisplayMode = DisplayMode.values()[savedState.getDisplayMode()];
        this.mInputEnabled = savedState.isInputEnabled();
        this.mInStealthMode = savedState.isInStealthMode();
        this.mEnableHapticFeedback = savedState.isTactileFeedbackEnabled();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        return new SavedState(super.onSaveInstanceState(), COUILockPatternUtils.patternToString(this.mPattern), this.mPatternDisplayMode.ordinal(), this.mInputEnabled, this.mInStealthMode, this.mEnableHapticFeedback);
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        this.mSquareWidth = ((w - getPaddingLeft()) - getPaddingRight()) / 3.0f;
        this.mSquareHeight = ((h - getPaddingTop()) - getPaddingBottom()) / 3.0f;
        this.mExploreByTouchHelper.invalidateRoot();
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (!this.mInputEnabled || !isEnabled()) {
            return false;
        }
        int action = motionEvent.getAction();
        if (action == 0) {
            ValueAnimator valueAnimator = this.mWrongAnimator;
            if (valueAnimator != null && valueAnimator.isRunning()) {
                this.mWrongAnimator.end();
            }
            handleActionDown(motionEvent);
            return true;
        }
        if (action == 1) {
            handleActionUp();
            return true;
        }
        if (action == 2) {
            handleActionMove(motionEvent);
            return true;
        }
        if (action != 3) {
            return false;
        }
        if (this.mPatternInProgress) {
            setPatternInProgress(false);
            resetPattern();
            notifyPatternCleared();
        }
        return true;
    }

    public void refresh() {
        String resourceTypeName = getResources().getResourceTypeName(this.mStyle);
        TypedArray typedArrayObtainStyledAttributes = null;
        if ("attr".equals(resourceTypeName)) {
            typedArrayObtainStyledAttributes = this.mContext.obtainStyledAttributes(null, R.styleable.COUILockPatternView, this.mStyle, 0);
        } else if ("style".equals(resourceTypeName)) {
            typedArrayObtainStyledAttributes = this.mContext.obtainStyledAttributes(null, R.styleable.COUILockPatternView, 0, this.mStyle);
        }
        if (typedArrayObtainStyledAttributes != null) {
            this.mRegularColor = typedArrayObtainStyledAttributes.getColor(R.styleable.COUILockPatternView_couiRegularColor, 0);
            this.mErrorColor = typedArrayObtainStyledAttributes.getColor(R.styleable.COUILockPatternView_couiErrorColor, 0);
            this.mSuccessColor = typedArrayObtainStyledAttributes.getColor(R.styleable.COUILockPatternView_couiSuccessColor, 0);
            this.mPathPaint.setColor(typedArrayObtainStyledAttributes.getColor(R.styleable.COUILockPatternView_couiPathColor, this.mRegularColor));
            this.mOuterCircleMaxAlpha = typedArrayObtainStyledAttributes.getFloat(R.styleable.COUILockPatternView_couiOuterCircleMaxAlpha, 0.0f);
            typedArrayObtainStyledAttributes.recycle();
        }
    }

    public void setDisplayMode(DisplayMode displayMode) {
        setDisplayMode(displayMode, true);
    }

    public void setErrorColor(int color) {
        this.mErrorColor = color;
    }

    public void setInStealthMode(boolean inStealthMode) {
        this.mInStealthMode = inStealthMode;
    }

    public void setLockPassword(boolean isSetPassword) {
        this.mIsSetPassword = isSetPassword;
    }

    public void setOnPatternListener(OnPatternListener onPatternListener) {
        this.mOnPatternListener = onPatternListener;
    }

    public void setOuterCircleMaxAlpha(int alpha) {
        this.mOuterCircleMaxAlpha = alpha;
    }

    public void setPathColor(int color) {
        this.mPathPaint.setColor(color);
    }

    public void setPattern(DisplayMode displayMode, List<Cell> list) {
        this.mPattern.clear();
        this.mPattern.addAll(list);
        clearPatternDrawLookup();
        for (Cell cell : list) {
            this.mPatternDrawLookup[cell.getRow()][cell.getColumn()] = true;
        }
        setDisplayMode(displayMode);
    }

    public void setRegularColor(int color) {
        this.mRegularColor = color;
    }

    public void setSuccessColor(int color) {
        this.mSuccessColor = color;
    }

    @Deprecated
    public void setSuccessFinger() {
    }

    public void setTactileFeedbackEnabled(boolean enabled) {
        this.mEnableHapticFeedback = enabled;
    }

    public static class SavedState extends View.BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel parcel) {
                return new SavedState(parcel);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        private final int mDisplayMode;
        private final boolean mInStealthMode;
        private final boolean mInputEnabled;
        private final String mSerializedPattern;
        private final boolean mTactileFeedbackEnabled;

        public int getDisplayMode() {
            return this.mDisplayMode;
        }

        public String getSerializedPattern() {
            return this.mSerializedPattern;
        }

        public boolean isInStealthMode() {
            return this.mInStealthMode;
        }

        public boolean isInputEnabled() {
            return this.mInputEnabled;
        }

        public boolean isTactileFeedbackEnabled() {
            return this.mTactileFeedbackEnabled;
        }

        @Override
        public void writeToParcel(Parcel parcel, int row) {
            super.writeToParcel(parcel, row);
            parcel.writeString(this.mSerializedPattern);
            parcel.writeInt(this.mDisplayMode);
            parcel.writeValue(Boolean.valueOf(this.mInputEnabled));
            parcel.writeValue(Boolean.valueOf(this.mInStealthMode));
            parcel.writeValue(Boolean.valueOf(this.mTactileFeedbackEnabled));
        }

        private SavedState(Parcelable parcelable, String str, int row, boolean inputEnabled, boolean inStealthMode, boolean partOfPattern) {
            super(parcelable);
            this.mSerializedPattern = str;
            this.mDisplayMode = row;
            this.mInputEnabled = inputEnabled;
            this.mInStealthMode = inStealthMode;
            this.mTactileFeedbackEnabled = partOfPattern;
        }

        private SavedState(Parcel parcel) {
            super(parcel);
            this.mSerializedPattern = parcel.readString();
            this.mDisplayMode = parcel.readInt();
            this.mInputEnabled = ((Boolean) parcel.readValue(null)).booleanValue();
            this.mInStealthMode = ((Boolean) parcel.readValue(null)).booleanValue();
            this.mTactileFeedbackEnabled = ((Boolean) parcel.readValue(null)).booleanValue();
        }
    }

    public COUILockPatternView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mPathAlpha = 1.0f;
        this.mDrawingProfilingStarted = false;
        Paint paint = new Paint();
        this.mPaint = paint;
        Paint paint2 = new Paint();
        this.mPathPaint = paint2;
        this.mPattern = new ArrayList<>(9);
        this.mPatternDrawLookup = (boolean[][]) Array.newInstance((Class<?>) Boolean.TYPE, 3, 3);
        this.mInProgressX = -1.0f;
        this.mInProgressY = -1.0f;
        this.mPatternDisplayMode = DisplayMode.Correct;
        this.mInputEnabled = true;
        this.mInStealthMode = false;
        this.mEnableHapticFeedback = true;
        this.mPatternInProgress = false;
        this.mHitFactor = 0.6f;
        this.mCurrentPath = new Path();
        this.mInvalidate = new Rect();
        this.mTmpInvalidateRect = new Rect();
        this.mIsSetPassword = false;
        this.mAlphaInterpolator = new COUIEaseInterpolator();
        this.mTranslateYInterpolator = new COUIInEaseInterpolator();
        this.mWongAnimatorListener = new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animator) {
                COUILockPatternView.this.resetPattern();
                if (COUILockPatternView.this.mWrongAnimator != null) {
                    COUILockPatternView.this.mWrongAnimator.removeAllListeners();
                }
            }
        };
        if (attributeSet == null || attributeSet.getStyleAttribute() == 0) {
            this.mStyle = R.attr.couiLockPatternViewStyle;
        } else {
            this.mStyle = attributeSet.getStyleAttribute();
        }
        this.mContext = context;
        COUIDarkModeUtil.setForceDarkAllow(this, false);
        this.mContext = context;
        TypedArray typedArrayObtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.COUILockPatternView, R.attr.couiLockPatternViewStyle, COUIContextUtil.isCOUIDarkTheme(context) ? R.style.Widget_COUI_COUILockPatternView_Dark : R.style.Widget_COUI_COUILockPatternView);
        setClickable(true);
        paint2.setAntiAlias(true);
        paint2.setDither(true);
        this.mRegularColor = typedArrayObtainStyledAttributes.getColor(R.styleable.COUILockPatternView_couiRegularColor, 0);
        this.mErrorColor = typedArrayObtainStyledAttributes.getColor(R.styleable.COUILockPatternView_couiErrorColor, 0);
        this.mSuccessColor = typedArrayObtainStyledAttributes.getColor(R.styleable.COUILockPatternView_couiSuccessColor, 0);
        paint2.setColor(typedArrayObtainStyledAttributes.getColor(R.styleable.COUILockPatternView_couiPathColor, this.mRegularColor));
        paint2.setStyle(Paint.Style.STROKE);
        paint2.setStrokeJoin(Paint.Join.ROUND);
        paint2.setStrokeCap(Paint.Cap.ROUND);
        int dimensionPixelSize = getResources().getDimensionPixelSize(R.dimen.lock_pattern_dot_line_width);
        this.mPathWidth = dimensionPixelSize;
        paint2.setStrokeWidth(dimensionPixelSize);
        int dimensionPixelSize2 = getResources().getDimensionPixelSize(R.dimen.lock_pattern_dot_size);
        paint.setAntiAlias(true);
        paint.setDither(true);
        this.mMaxTranslateY = getResources().getDimensionPixelSize(R.dimen.color_lock_pattern_view_max_translate_y);
        this.mCellStates = (CellState[][]) Array.newInstance((Class<?>) CellState.class, 3, 3);
        for (int index = 0; index < 3; index++) {
            for (int column = 0; column < 3; column++) {
                this.mCellStates[index][column] = new CellState();
                CellState cellState = this.mCellStates[index][column];
                cellState.radius = dimensionPixelSize2 / 2;
                cellState.row = index;
                cellState.col = column;
                cellState.alpha = Color.alpha(this.mRegularColor) / MAX_ALPHA;
                CellState state = this.mCellStates[index][column];
                state.innerCircleAlpha = 0.0f;
                state.innerCircleScale = 1.0f;
                state.outerCircleAlpha = 0.0f;
                state.outerCircleScale = 1.0f;
                state.needDrawCircle = true;
                state.setCellDrawListener(new OnCellDrawListener() {
                    @Override
                    public void drawCell() {
                        COUILockPatternView.this.invalidate();
                    }
                });
            }
        }
        this.mInnerDrawable = getResources().getDrawable(R.drawable.coui_lock_pattern_inner_circle);
        this.mOuterDrawable = getResources().getDrawable(R.drawable.coui_lock_pattern_outer_circle);
        this.mDefaultWidth = getResources().getDimensionPixelSize(R.dimen.coui_lock_pattern_view_width);
        this.mDefaultHeight = getResources().getDimensionPixelSize(R.dimen.coui_lock_pattern_view_height);
        this.mOuterCircleMaxAlpha = typedArrayObtainStyledAttributes.getFloat(R.styleable.COUILockPatternView_couiOuterCircleMaxAlpha, 0.0f);
        this.mFastOutSlowInInterpolator = AnimationUtils.loadInterpolator(context, 17563661);
        PatternExploreByTouchHelper patternExploreByTouchHelper = new PatternExploreByTouchHelper(this);
        this.mExploreByTouchHelper = patternExploreByTouchHelper;
        ViewCompat.setAccessibilityDelegate(this, patternExploreByTouchHelper);
        this.mAccessibilityManagerService = (AccessibilityManager) this.mContext.getSystemService(Context.ACCESSIBILITY_SERVICE);
        typedArrayObtainStyledAttributes.recycle();
        this.mIsLinearMotorVersion = VibrateUtils.isLinearMotorVersion(context);
    }

    public void clearPattern() {
        ValueAnimator valueAnimator = this.mWrongAnimator;
        if (valueAnimator == null || !valueAnimator.isRunning()) {
            resetPattern();
        } else {
            this.mWrongAnimator.addListener(this.mWongAnimatorListener);
        }
    }

    public void setDisplayMode(DisplayMode displayMode, boolean partOfPattern) {
        this.mPatternDisplayMode = displayMode;
        if (displayMode == DisplayMode.Animate) {
            if (this.mPattern.size() == 0) {
                throw new IllegalStateException("you must have a pattern to animate if you want to set the display mode to animate");
            }
            this.mAnimatingPeriodStart = SystemClock.elapsedRealtime();
            Cell cell = this.mPattern.get(0);
            this.mInProgressX = getCenterXForColumn(cell.getColumn());
            this.mInProgressY = getCenterYForRow(cell.getRow());
            clearPatternDrawLookup();
        }
        if (displayMode == DisplayMode.Wrong) {
            if (partOfPattern && this.mPattern.size() > 1) {
                performWrongModeFeedback();
            }
            startWrongAnimator();
        }
        if (displayMode == DisplayMode.FingerprintNoMatch) {
            startFingerprintNoMatchAnimator();
        }
        invalidate();
    }
}
