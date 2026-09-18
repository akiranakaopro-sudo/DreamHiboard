package com.coui.appcompat.scroll;

import android.content.Context;
import android.graphics.PointF;
import android.view.View;
import android.view.animation.Interpolator;
import androidx.recyclerview.widget.COUIRecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.OrientationHelper;


public class COUIFlingLocateHelper {
    private static final int CENTER_ALIGN = 2;
    private static final int INVALID_ALIGN = 0;
    private static final int INVALID_POSITION = -1;
    private static final float ONE = 1.0f;
    private static final int START_ALIGN = 1;
    private static final String TAG = "COUIFlingLocateHelper";
    private Context mContext;
    private OrientationHelper mHorizontalHelper;
    private RecyclerView.LayoutManager mLayoutManager;
    private OnCalculatePreChildDistanceListener mOnCalculatePreChildDistanceListener;
    private COUIRecyclerView mRecyclerView;
    private int mHorizontalItemAlign = INVALID_ALIGN;
    private boolean mEnableSnapToCenter = true;
    private Interpolator mCustomInterpolator = null;
    private int mCustomDuration = 0;
    private RecyclerView.OnScrollListener mAlignScrollListener = new RecyclerView.OnScrollListener() {
        boolean mScrolled = false;

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (newState == 0 && this.mScrolled) {
                this.mScrolled = false;
                COUIFlingLocateHelper.this.snapToTargetExistingView();
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            if (dx == 0 && dy == 0) {
                return;
            }
            this.mScrolled = true;
        }
    };

    public interface OnCalculatePreChildDistanceListener {
        int onCalculatePreChildDistance();

        int onCalculateTargetPosition(int position, int delta);
    }

    private float computeDistancePerChild(RecyclerView.LayoutManager layoutManager, OrientationHelper orientationHelper) {
        int childCount = layoutManager.getChildCount();
        if (childCount == 0) {
            return 1.0f;
        }
        View minPosView = null;
        int maxPosition = Integer.MIN_VALUE;
        int minPosition = Integer.MAX_VALUE;
        View maxPosView = null;
        for (int i = 0; i < childCount; i++) {
            View childAt = layoutManager.getChildAt(i);
            int position = layoutManager.getPosition(childAt);
            if (position != -1 && position != layoutManager.getItemCount() - 1 && position != 0) {
                if (position < minPosition) {
                    minPosView = childAt;
                    minPosition = position;
                }
                if (position > maxPosition) {
                    maxPosView = childAt;
                    maxPosition = position;
                }
            }
        }
        if (minPosView == null || maxPosView == null) {
            return 1.0f;
        }
        int span = Math.max(orientationHelper.getDecoratedEnd(minPosView), orientationHelper.getDecoratedEnd(maxPosView)) - Math.min(orientationHelper.getDecoratedStart(minPosView), orientationHelper.getDecoratedStart(maxPosView));
        if (span == 0) {
            return 1.0f;
        }
        return (span * 1.0f) / ((maxPosition - minPosition) + 1);
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

    private View findStartView(RecyclerView.LayoutManager layoutManager, OrientationHelper orientationHelper) {
        int childCount = layoutManager.getChildCount();
        View closestChild = null;
        if (childCount == 0) {
            return null;
        }
        if (layoutManager instanceof LinearLayoutManager) {
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
            boolean firstIsLast = linearLayoutManager.findFirstCompletelyVisibleItemPosition() == layoutManager.getItemCount() - 1;
            boolean lastIsLast = linearLayoutManager.findLastCompletelyVisibleItemPosition() == layoutManager.getItemCount() - 1;
            if (firstIsLast || lastIsLast) {
                return null;
            }
        }
        int edge = isRtlMode(this.mContext) ? orientationHelper.getEndAfterPadding() : orientationHelper.getStartAfterPadding();
        int closestAbs = Integer.MAX_VALUE;
        for (int i = 0; i < childCount; i++) {
            View childAt = layoutManager.getChildAt(i);
            int abs = Math.abs((isRtlMode(this.mContext) ? orientationHelper.getDecoratedEnd(childAt) : orientationHelper.getDecoratedStart(childAt)) - edge);
            if (abs < closestAbs) {
                closestChild = childAt;
                closestAbs = abs;
            }
        }
        return closestChild;
    }

    private OrientationHelper getHorizontalHelper(RecyclerView.LayoutManager layoutManager) {
        OrientationHelper helper = this.mHorizontalHelper;
        if (helper == null || helper.getLayoutManager() != layoutManager) {
            this.mHorizontalHelper = OrientationHelper.createHorizontalHelper(layoutManager);
        }
        return this.mHorizontalHelper;
    }

    private RecyclerView.LayoutManager getLayoutManager() {
        RecyclerView.LayoutManager layoutManager = this.mLayoutManager;
        if (layoutManager == null || layoutManager != this.mRecyclerView.getLayoutManager()) {
            this.mLayoutManager = this.mRecyclerView.getLayoutManager();
        }
        return this.mLayoutManager;
    }

    private boolean isRtlMode(Context context) {
        return context != null && context.getResources().getConfiguration().getLayoutDirection() == 1;
    }

    private void smoothScrollBy(int dx, int dy) {
        int duration;
        Interpolator interpolator = this.mCustomInterpolator;
        if (interpolator == null || (duration = this.mCustomDuration) == 0) {
            this.mRecyclerView.smoothScrollBy(dx, dy);
        } else {
            this.mRecyclerView.smoothScrollBy(dx, dy, interpolator, duration);
        }
    }


    public void snapToTargetExistingView() {
        RecyclerView.LayoutManager layoutManager;
        View snapView;
        int decoratedEdge;
        int paddingEdge;
        if ((!this.mEnableSnapToCenter && this.mHorizontalItemAlign == CENTER_ALIGN) || (layoutManager = getLayoutManager()) == null || (snapView = findSnapView(layoutManager)) == null) {
            return;
        }
        int align = this.mHorizontalItemAlign;
        if (align == 2) {
            int targetCenter = getHorizontalHelper(layoutManager).getStartAfterPadding() + (getHorizontalHelper(layoutManager).getTotalSpace() / 2);
            int lastItem = layoutManager.getItemCount() - 1;
            if (layoutManager.getPosition(snapView) == 0) {
                targetCenter = isRtlMode(this.mContext) ? getHorizontalHelper(layoutManager).getEndAfterPadding() - (getHorizontalHelper(layoutManager).getDecoratedMeasurement(snapView) / 2) : getHorizontalHelper(layoutManager).getStartAfterPadding() + (getHorizontalHelper(layoutManager).getDecoratedMeasurement(snapView) / 2);
            }
            if (layoutManager.getPosition(snapView) == lastItem) {
                targetCenter = isRtlMode(this.mContext) ? getHorizontalHelper(layoutManager).getStartAfterPadding() + (getHorizontalHelper(layoutManager).getDecoratedMeasurement(snapView) / 2) : getHorizontalHelper(layoutManager).getEndAfterPadding() - (getHorizontalHelper(layoutManager).getDecoratedMeasurement(snapView) / 2);
            }
            int delta = (getHorizontalHelper(layoutManager).getDecoratedStart(snapView) + (getHorizontalHelper(layoutManager).getDecoratedMeasurement(snapView) / 2)) - targetCenter;
            if (Math.abs(delta) > 1.0f) {
                smoothScrollBy(delta, 0);
                return;
            }
            return;
        }
        if (align == START_ALIGN) {
            if (isRtlMode(this.mContext)) {
                decoratedEdge = getHorizontalHelper(layoutManager).getDecoratedEnd(snapView);
                paddingEdge = getHorizontalHelper(layoutManager).getEndAfterPadding();
            } else {
                decoratedEdge = getHorizontalHelper(layoutManager).getDecoratedStart(snapView);
                paddingEdge = getHorizontalHelper(layoutManager).getStartAfterPadding();
            }
            int delta = decoratedEdge - paddingEdge;
            if (Math.abs(delta) > 1.0f) {
                smoothScrollBy(delta, 0);
            }
        }
    }

    public void attachToRecyclerView(COUIRecyclerView recyclerView) {
        this.mRecyclerView = recyclerView;
        this.mContext = recyclerView.getContext();
    }

    public void cancelHorizontalItemAlign() {
        this.mHorizontalItemAlign = INVALID_ALIGN;
        this.mRecyclerView.removeOnScrollListener(this.mAlignScrollListener);
    }

    public View findSnapView(RecyclerView.LayoutManager layoutManager) {
        if (layoutManager.canScrollHorizontally()) {
            int align = this.mHorizontalItemAlign;
            if (align == 2) {
                return findCenterView(layoutManager, getHorizontalHelper(layoutManager));
            }
            if (align == START_ALIGN) {
                return findStartView(layoutManager, getHorizontalHelper(layoutManager));
            }
        }
        return null;
    }

    public int getHorizontalItemAlign() {
        return this.mHorizontalItemAlign;
    }


    public int getTargetViewDistance(int velocity) {
        View snapView;
        float distancePerChild;
        int deltaItems;
        int targetCoord;
        RecyclerView.LayoutManager layoutManager = getLayoutManager();
        int itemCount = layoutManager.getItemCount();
        if (itemCount == 0 || (snapView = findSnapView(layoutManager)) == null) {
            return -1;
        }
        int position = layoutManager.getPosition(snapView);
        int lastItem = itemCount - 1;
        PointF scrollVector = ((RecyclerView.SmoothScroller.ScrollVectorProvider) layoutManager).computeScrollVectorForPosition(lastItem);
        if (scrollVector == null) {
            return -1;
        }
        if (layoutManager.canScrollHorizontally()) {
            distancePerChild = this.mOnCalculatePreChildDistanceListener != null ? this.mOnCalculatePreChildDistanceListener.onCalculatePreChildDistance() : computeDistancePerChild(layoutManager, getHorizontalHelper(layoutManager));
            deltaItems = Math.round(velocity / distancePerChild);
            if (scrollVector.x < 0.0f) {
                deltaItems = -deltaItems;
            }
        } else {
            distancePerChild = 1.0f;
            deltaItems = 0;
        }
        OnCalculatePreChildDistanceListener listener = this.mOnCalculatePreChildDistanceListener;
        int targetPosition = listener != null ? listener.onCalculateTargetPosition(position, deltaItems) : deltaItems + position;
        if (targetPosition != position && targetPosition >= 0 && targetPosition < itemCount) {
            int align = this.mHorizontalItemAlign;
            if (align == 2) {
                View edgeChild = (layoutManager.getPosition(snapView) != 0 || layoutManager.getChildCount() == 0) ? null : layoutManager.getChildAt(layoutManager.getChildCount() - 1);
                if (layoutManager.getPosition(snapView) == lastItem && layoutManager.getChildCount() != 0) {
                    edgeChild = layoutManager.getChildAt(0);
                }
                int center = getHorizontalHelper(layoutManager).getStartAfterPadding() + (getHorizontalHelper(layoutManager).getTotalSpace() / 2);
                if (edgeChild != null) {
                    targetCoord = getHorizontalHelper(layoutManager).getDecoratedStart(edgeChild) + (getHorizontalHelper(layoutManager).getDecoratedMeasurement(edgeChild) / 2) + (isRtlMode(this.mContext) ? -((int) ((targetPosition - layoutManager.getPosition(edgeChild)) * distancePerChild)) : (int) ((targetPosition - layoutManager.getPosition(edgeChild)) * distancePerChild));
                } else {
                    targetCoord = getHorizontalHelper(layoutManager).getDecoratedStart(snapView) + (getHorizontalHelper(layoutManager).getDecoratedMeasurement(snapView) / 2) + (isRtlMode(this.mContext) ? -((int) ((targetPosition - layoutManager.getPosition(snapView)) * distancePerChild)) : (int) ((targetPosition - layoutManager.getPosition(snapView)) * distancePerChild));
                }
                return targetCoord - center;
            }
            if (align == START_ALIGN) {
                int positionDelta = targetPosition - position;
                return ((isRtlMode(this.mContext) ? getHorizontalHelper(layoutManager).getDecoratedEnd(snapView) : getHorizontalHelper(layoutManager).getDecoratedStart(snapView)) + (isRtlMode(this.mContext) ? -((int) (positionDelta * distancePerChild)) : (int) (positionDelta * distancePerChild))) - (isRtlMode(this.mContext) ? getHorizontalHelper(layoutManager).getEndAfterPadding() : getHorizontalHelper(layoutManager).getStartAfterPadding());
            }
        }
        return -1;
    }

    public void setCustomSmooth(Interpolator interpolator, int duration) {
        this.mCustomInterpolator = interpolator;
        this.mCustomDuration = duration;
    }

    public void setEnableSnapToCenter(boolean enable) {
        this.mEnableSnapToCenter = enable;
    }

    public void setHorizontalItemAlign(int align) {
        this.mHorizontalItemAlign = align;
        this.mRecyclerView.addOnScrollListener(this.mAlignScrollListener);
    }

    public void setOnCalculatePreChildDistanceListener(OnCalculatePreChildDistanceListener listener) {
        this.mOnCalculatePreChildDistanceListener = listener;
    }

    public void trySnapToTargetExistingView() {
        if (this.mHorizontalItemAlign != INVALID_ALIGN) {
            snapToTargetExistingView();
        }
    }
}
