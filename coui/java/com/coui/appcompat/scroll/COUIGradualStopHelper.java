package com.coui.appcompat.scroll;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.COUIRecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.OrientationHelper;
import com.coui.appcompat.log.COUILog;


public class COUIGradualStopHelper {
    private static final boolean DEBUG;
    private static final int INVALID_POSITION = -1;
    private static final int MAX_ITERATE = 1000;
    private static final float ONE = 1.0f;
    private static final String TAG = "COUIGradualStopHelper";
    private Context mContext;
    private RecyclerView.LayoutManager mLayoutManager;
    private COUIRecyclerView mRecyclerView;
    private OrientationHelper mOrientationHelper = null;
    private int mLastOrientation = -1;

    static {
        DEBUG = COUILog.LOG_DEBUG || COUILog.isLoggable(TAG, 3);
    }

    private View findCenterView(RecyclerView.LayoutManager layoutManager, OrientationHelper orientationHelper) {
        int childCount = layoutManager.getChildCount();
        View closestChild = null;
        if (childCount == 0) {
            return null;
        }
        int center = orientationHelper.getStartAfterPadding() + (orientationHelper.getTotalSpace() / 2);
        int closestAbs = Integer.MAX_VALUE;
        for (int i = 0; i < childCount; i++) {
            View childAt = layoutManager.getChildAt(i);
            int abs = Math.abs((layoutManager.getDecoratedLeft(childAt) + (layoutManager.getDecoratedMeasuredWidth(childAt) / 2)) - center);
            if (abs < closestAbs) {
                closestChild = childAt;
                closestAbs = abs;
            }
        }
        return closestChild;
    }

    private int getDefaultItemCenterOffset(int position) {
        View viewByPosition;
        int startMargin;
        int endMargin;
        RecyclerView.LayoutManager layoutManager = getLayoutManager();
        if (layoutManager == null || (viewByPosition = layoutManager.findViewByPosition(position)) == null || !(viewByPosition.getLayoutParams() instanceof RecyclerView.LayoutParams)) {
            return 0;
        }
        RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) viewByPosition.getLayoutParams();
        if ((layoutManager instanceof LinearLayoutManager) && ((LinearLayoutManager) layoutManager).getOrientation() == 1) {
            startMargin = ((ViewGroup.MarginLayoutParams) layoutParams).topMargin;
            endMargin = ((ViewGroup.MarginLayoutParams) layoutParams).bottomMargin;
        } else {
            startMargin = ((ViewGroup.MarginLayoutParams) layoutParams).leftMargin;
            endMargin = ((ViewGroup.MarginLayoutParams) layoutParams).rightMargin;
        }
        return startMargin - endMargin;
    }

    private int getDefautDecoratedMeasurement(int position) {
        int itemCount;
        View childAt;
        RecyclerView.LayoutManager layoutManager = getLayoutManager();
        if (layoutManager == null || (itemCount = layoutManager.getItemCount()) <= 0 || position < 0 || position >= itemCount) {
            return 0;
        }
        View viewByPosition = layoutManager.findViewByPosition(position);
        OrientationHelper orientationHelper = getOrientationHelper(layoutManager);
        if (viewByPosition != null) {
            return orientationHelper.getDecoratedMeasurement(viewByPosition);
        }
        if (layoutManager.getChildCount() <= 0 || (childAt = layoutManager.getChildAt(0)) == null) {
            return 0;
        }
        return orientationHelper.getDecoratedMeasurement(childAt);
    }

    private RecyclerView.LayoutManager getLayoutManager() {
        RecyclerView.LayoutManager layoutManager = this.mLayoutManager;
        if (layoutManager == null || layoutManager != this.mRecyclerView.getLayoutManager()) {
            this.mLayoutManager = this.mRecyclerView.getLayoutManager();
        }
        return this.mLayoutManager;
    }

    private OrientationHelper getOrientationHelper(RecyclerView.LayoutManager layoutManager) {
        int orientation = layoutManager instanceof LinearLayoutManager ? ((LinearLayoutManager) layoutManager).getOrientation() : -1;
        OrientationHelper helper = this.mOrientationHelper;
        if (helper == null || this.mLastOrientation != orientation || helper.getLayoutManager() != layoutManager) {
            this.mLastOrientation = orientation;
            this.mOrientationHelper = orientation == 1 ? OrientationHelper.createVerticalHelper(layoutManager) : OrientationHelper.createHorizontalHelper(layoutManager);
        }
        return this.mOrientationHelper;
    }

    private boolean isRtlMode(Context context) {
        COUIRecyclerView recyclerView = this.mRecyclerView;
        return recyclerView != null ? ViewCompat.getLayoutDirection(recyclerView) == 1 : context != null && context.getResources().getConfiguration().getLayoutDirection() == 1;
    }

    private float iterateDisplacement(int startPos, boolean forward, float initialDisplacement, int distance, int itemCount) {
        int absDistance = Math.abs(distance);
        int step = 1;
        int direction = forward ? 1 : -1;
        if (isRtlMode(this.mContext)) {
            direction = forward ? -1 : 1;
        }
        float displacement = initialDisplacement;
        int iterations = 0;
        int nextPos = startPos;
        while (displacement < absDistance && nextPos >= 0 && nextPos < itemCount) {
            int itemCenterOffset = getItemCenterOffset(nextPos);
            float midDisplacement = displacement + ((getDecoratedMeasurement(nextPos) - (direction * itemCenterOffset)) / 2.0f);
            boolean debug = DEBUG;
            if (debug) {
                Log.d(TAG, "displacement:" + midDisplacement + " nextPos:" + nextPos + " offset:" + itemCenterOffset);
            }
            nextPos = forward ? nextPos + 1 : nextPos - 1;
            iterations += step;
            if (nextPos < 0 || nextPos >= itemCount || iterations > MAX_ITERATE) {
                return distance;
            }
            int nextItemWidth = getDecoratedMeasurement(nextPos);
            if (nextItemWidth == -1 || nextItemWidth < 0) {
                return distance;
            }
            int nextItemCenterOffset = getItemCenterOffset(nextPos);
            displacement = midDisplacement + ((getDecoratedMeasurement(nextPos) + (direction * nextItemCenterOffset)) / 2.0f);
            if (debug) {
                Log.d(TAG, "displacement:" + displacement + " nextPos:" + nextPos + " offset:" + nextItemCenterOffset + " nextItemWidth:" + nextItemWidth);
            }
            step = 1;
        }
        return direction * displacement;
    }

    public void attachToRecyclerView(COUIRecyclerView recyclerView) {
        this.mRecyclerView = recyclerView;
        this.mContext = recyclerView.getContext();
    }

    public View getCenterItemView() {
        RecyclerView.LayoutManager layoutManager = getLayoutManager();
        int childCount = layoutManager.getChildCount();
        OrientationHelper orientationHelper = getOrientationHelper(layoutManager);
        float center = orientationHelper.getStartAfterPadding() + (orientationHelper.getTotalSpace() / 2.0f);
        for (int i = 0; i < childCount; i++) {
            View childAt = layoutManager.getChildAt(i);
            if (childAt != null) {
                int start = orientationHelper.getDecoratedStart(childAt);
                int end = orientationHelper.getDecoratedEnd(childAt);
                if (center >= start && center <= end) {
                    return childAt;
                }
            }
        }
        return null;
    }

    public float getCenterToEdgeOffsetInVelocityDirection(int velocity) {
        int childCount;
        RecyclerView.LayoutManager layoutManager = getLayoutManager();
        if (layoutManager == null || (childCount = layoutManager.getChildCount()) == 0) {
            return 0.0f;
        }
        OrientationHelper orientationHelper = getOrientationHelper(layoutManager);
        float center = orientationHelper.getStartAfterPadding() + (orientationHelper.getTotalSpace() / 2.0f);
        for (int i = 0; i < childCount; i++) {
            View childAt = layoutManager.getChildAt(i);
            if (childAt != null) {
                int start = orientationHelper.getDecoratedStart(childAt);
                int end = orientationHelper.getDecoratedEnd(childAt);
                float startF = start;
                if (center >= startF) {
                    float endF = end;
                    if (center <= endF) {
                        if (velocity > 0) {
                            return endF - center;
                        }
                        if (velocity < 0) {
                            return startF - center;
                        }
                        float toStart = startF - center;
                        float toEnd = endF - center;
                        return Math.abs(toStart) <= Math.abs(toEnd) ? toStart : toEnd;
                    }
                } else {
                    continue;
                }
            }
        }
        if (DEBUG) {
            Log.d(TAG, "getCenterToEdgeOffsetInVelocityDirection has no center item");
        }
        return 0.0f;
    }

    public float getCenterToItemCenterOffsetUnderCenter() {
        int childCount;
        RecyclerView.LayoutManager layoutManager = getLayoutManager();
        float closestOffset = 0.0f;
        if (layoutManager == null || (childCount = layoutManager.getChildCount()) == 0) {
            return 0.0f;
        }
        OrientationHelper orientationHelper = getOrientationHelper(layoutManager);
        float center = orientationHelper.getStartAfterPadding() + (orientationHelper.getTotalSpace() / 2.0f);
        for (int i = 0; i < childCount; i++) {
            View childAt = layoutManager.getChildAt(i);
            if (childAt != null) {
                int start = orientationHelper.getDecoratedStart(childAt);
                int end = orientationHelper.getDecoratedEnd(childAt);
                if (center >= start && center <= end) {
                    return getItemViewCenter(childAt) - center;
                }
            }
        }
        float closestAbs = Float.MAX_VALUE;
        for (int i = 0; i < childCount; i++) {
            View childAt = layoutManager.getChildAt(i);
            if (childAt != null) {
                float offset = getItemViewCenter(childAt) - center;
                float abs = Math.abs(offset);
                if (abs < closestAbs) {
                    closestOffset = offset;
                    closestAbs = abs;
                }
            }
        }
        return closestOffset;
    }

    public float getCenterViewNextPositionCenter(View view, int position, boolean forward) {
        RecyclerView.LayoutManager layoutManager = getLayoutManager();
        if (layoutManager == null) {
            return 0.0f;
        }
        OrientationHelper orientationHelper = getOrientationHelper(layoutManager);
        int end = orientationHelper.getDecoratedEnd(view);
        int start = orientationHelper.getDecoratedStart(view);
        int itemCenterOffset = getItemCenterOffset(position);
        int decoratedMeasurement = getDecoratedMeasurement(position);
        int direction = forward ? 1 : -1;
        if (isRtlMode(this.mContext)) {
            direction = forward ? -1 : 1;
        }
        if (direction != 1) {
            end = start;
        }
        return end + ((direction * (decoratedMeasurement + (itemCenterOffset * direction))) / 2.0f);
    }

    public int getDecoratedMeasurement(int position) {
        if (getLayoutManager() == null) {
            return 0;
        }
        this.mRecyclerView.getAdapter();
        return getDefautDecoratedMeasurement(position);
    }

    public float getDisplacementToAlignCenter(int initialVelocity, int distance) {
        float displacement;
        int startPos;
        COUIGradualStopHelper helper = this;
        if (!validation()) {
            return 0.0f;
        }
        RecyclerView.LayoutManager layoutManager = getLayoutManager();
        OrientationHelper orientationHelper = helper.getOrientationHelper(layoutManager);
        float center = orientationHelper.getStartAfterPadding() + (orientationHelper.getTotalSpace() / 2.0f);
        int childIndex = 0;
        boolean forward = true;
        if (!helper.isRtlMode(helper.mContext) ? initialVelocity <= 0 : initialVelocity >= 0) {
            forward = false;
        }
        int childCount = layoutManager.getChildCount();
        int itemCount = layoutManager.getItemCount();
        View centerItemView = getCenterItemView();
        int centerAdapterPosition = centerItemView != null ? layoutManager.getPosition(centerItemView) : -1;
        if (centerItemView == null || centerAdapterPosition == -1) {
            displacement = 0.0f;
            startPos = 0;
        } else {
            startPos = forward ? centerAdapterPosition + 1 : centerAdapterPosition - 1;
            if (startPos < 0 || startPos >= itemCount) {
                float itemViewCenter = helper.getItemViewCenter(centerItemView) - center;
                return (((float) initialVelocity) * itemViewCenter <= 0.0f || Math.abs(itemViewCenter) <= ((float) Math.abs(distance))) ? distance : itemViewCenter;
            }
            displacement = Math.abs(helper.getCenterViewNextPositionCenter(centerItemView, startPos, forward) - center);
        }
        if (displacement == 0.0f) {
            float closestOffset = Float.MAX_VALUE;
            while (childIndex < childCount) {
                View childAt = layoutManager.getChildAt(childIndex);
                if (childAt != null) {
                    float itemViewCenter = helper.getItemViewCenter(childAt) - center;
                    if (initialVelocity * itemViewCenter > 0.0f && Math.abs(itemViewCenter) < Math.abs(closestOffset)) {
                        float abs = Math.abs(itemViewCenter);
                        startPos = layoutManager.getPosition(childAt);
                        closestOffset = itemViewCenter;
                        displacement = abs;
                    }
                }
                childIndex++;
                helper = this;
            }
        }
        float finalDisplacement = displacement;
        if (DEBUG) {
            Log.d(TAG, "initialVelocity:" + initialVelocity + " distance:" + distance + " centerAdapterPosition:" + centerAdapterPosition + " startPos:" + startPos + " displacement:" + finalDisplacement + " itemCount:" + itemCount);
        }
        return iterateDisplacement(startPos, forward, finalDisplacement, distance, itemCount);
    }

    public int getItemCenterOffset(int position) {
        if (getLayoutManager() == null) {
            return 0;
        }
        this.mRecyclerView.getAdapter();
        return getDefaultItemCenterOffset(position);
    }

    public float getItemViewCenter(View view) {
        RecyclerView.LayoutManager layoutManager = getLayoutManager();
        if (layoutManager == null || view == null) {
            return 0.0f;
        }
        OrientationHelper orientationHelper = getOrientationHelper(layoutManager);
        return orientationHelper.getDecoratedStart(view) + ((orientationHelper.getDecoratedMeasurement(view) + getItemCenterOffset(layoutManager.getPosition(view))) / 2.0f);
    }

    public void trySnapToTargetExistingView() {
        OrientationHelper orientationHelper;
        View centerView;
        RecyclerView.LayoutManager layoutManager = getLayoutManager();
        if (layoutManager == null || (centerView = findCenterView(layoutManager, (orientationHelper = getOrientationHelper(layoutManager)))) == null) {
            return;
        }
        int targetCenter = orientationHelper.getStartAfterPadding() + (orientationHelper.getTotalSpace() / 2);
        int lastItem = layoutManager.getItemCount() - 1;
        if (layoutManager.getPosition(centerView) == 0) {
            targetCenter = isRtlMode(this.mContext) ? orientationHelper.getEndAfterPadding() - (orientationHelper.getDecoratedMeasurement(centerView) / 2) : orientationHelper.getStartAfterPadding() + (orientationHelper.getDecoratedMeasurement(centerView) / 2);
        }
        if (layoutManager.getPosition(centerView) == lastItem) {
            targetCenter = isRtlMode(this.mContext) ? orientationHelper.getStartAfterPadding() + (orientationHelper.getDecoratedMeasurement(centerView) / 2) : orientationHelper.getEndAfterPadding() - (orientationHelper.getDecoratedMeasurement(centerView) / 2);
        }
        int delta = (orientationHelper.getDecoratedStart(centerView) + (orientationHelper.getDecoratedMeasurement(centerView) / 2)) - targetCenter;
        if (Math.abs(delta) > 1.0f) {
            if ((layoutManager instanceof LinearLayoutManager) && ((LinearLayoutManager) layoutManager).getOrientation() == 1) {
                this.mRecyclerView.smoothScrollBy(0, delta);
            } else {
                this.mRecyclerView.smoothScrollBy(delta, 0);
            }
        }
    }

    public boolean validation() {
        RecyclerView.LayoutManager layoutManager = getLayoutManager();
        if (layoutManager == null) {
            return false;
        }
        return (layoutManager.getChildCount() == 0 || layoutManager.getItemCount() == 0) ? false : true;
    }
}
