package com.coui.appcompat.animation;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import com.coui.appcompat.accessibilityutil.COUIAccessibilityUtil;
import com.coui.appcompat.animation.dynamicanimation.COUIDynamicAnimation;
import com.coui.appcompat.animation.dynamicanimation.COUISpringAnimation;
import com.coui.appcompat.animation.dynamicanimation.COUISpringForce;
import java.util.ArrayList;
import java.util.Iterator;

public class COUISpringDragHelper {
    private static final int ANIM_X_RUNNING = 1;
    private static final int ANIM_Y_RUNNING = 2;
    private static final int ATTACH_FLAG_UNSET = 0;
    private static final int ATTACH_FLAG_X = 1;
    private static final int ATTACH_FLAG_XY = 3;
    private static final int ATTACH_FLAG_Y = 2;
    public static final int CHANGING = 1;
    private static final float DEFAULT_BOUNCE_ATTACH = 0.2f;
    private static final float DEFAULT_BOUNCE_DRAG = 0.15f;
    private static final float DEFAULT_CURVE_RATIO = 0.55f;
    private static final float DEFAULT_MAX_SIZE = 800.0f;
    private static final float DEFAULT_MIN_SIZE = 100.0f;
    private static final float DEFAULT_OVER_DISTANCE = 20.0f;
    private static final float DEFAULT_RESPONSE_ATTACH = 0.4f;
    private static final float DEFAULT_RESPONSE_DRAG = 0.15f;
    private static final int DRAGGING = 4;
    public static final int IDLE = 0;
    private static final int MAX_VELOCITY = 12000;
    private static final int MILLISECOND_VELOCITY_UNIT = 1000;
    private static final float MINCHANGE = 0.01f;
    private static final int MOVE_DISTANCE_MIN = 1;
    public static final String TAG = "COUISpringDragHelper";
    private static final int TRANSFORM_DISTANCE = 8;
    private static final int TRANSFORM_VELOCITY = 2000;
    private static final float UNSET = Float.MIN_VALUE;
    private final COUISpringAnimation mAnimX;
    private final COUISpringAnimation mAnimY;
    private float mBounceAttach;
    private float mBounceDrag;
    private float mCurX;
    private float mCurY;
    private float mCurveRatio;
    private final Vec2 mCustomMax;
    private final Vec2 mCustomMin;
    private boolean mDebug;
    private boolean mDeltaSatisfy;
    private float mDeltaX;
    private float mDeltaY;
    private float mDownX;
    private float mDownY;
    private final androidx.dynamicanimation.animation.FloatValueHolder mFloatValueHolderX;
    private final androidx.dynamicanimation.animation.FloatValueHolder mFloatValueHolderY;
    private float mMaxOverDistanceX;
    private float mMaxOverDistanceY;
    private Vec2 mMaxPosition;
    private Vec2 mMinPosition;
    private float mResponseAttach;
    private float mResponseDrag;
    private final SpringChangeObserver mSpringChangeObserver;
    private final COUISpringForce mSpringX;
    private final COUISpringForce mSpringY;
    private ArrayList<Vec2> mStableList;
    private float mStartX;
    private float mStartY;
    private int mStatus;
    private float mTransformDistance;
    private float mTransformVelocity;
    private VelocityTracker mVelocityTracker;

    public interface SpringChangeObserver {
        void onSizeChange(float x, float y);

        void onStateChange(int state);
    }

    public static class Vec2 {
        float mX;
        float mY;

        public Vec2(Vec2 vector) {
            this.mX = vector.getX();
            this.mY = vector.getY();
        }

        public float getX() {
            return this.mX;
        }

        public float getY() {
            return this.mY;
        }

        public void set(float x, float y) {
            this.mX = x;
            this.mY = y;
        }

        public void setX(float x) {
            this.mX = x;
        }

        public void setY(float y) {
            this.mY = y;
        }

        public String toString() {
            return "[" + this.mX + COUIAccessibilityUtil.PAUSE_STRING + this.mY + "]";
        }

        public void set(Vec2 vector) {
            this.mX = vector.getX();
            this.mY = vector.getY();
        }

        public Vec2(float x, float y) {
            this.mX = x;
            this.mY = y;
        }
    }

    public COUISpringDragHelper(SpringChangeObserver springChangeObserver, ArrayList<Vec2> stableList) {
        this(springChangeObserver, stableList, DEFAULT_CURVE_RATIO, DEFAULT_OVER_DISTANCE, DEFAULT_OVER_DISTANCE, 2000.0f, 8.0f);
    }

    private void beginDrag(float x, float y) {
        if (this.mStartX == Float.MIN_VALUE || this.mStartY == Float.MIN_VALUE) {
            this.mStartX = 0.0f;
            this.mStartY = 0.0f;
            Log.d(TAG, "beginDrag : startValue is Unset");
        }
        this.mDownX = x;
        this.mDownY = y;
        int status = this.mStatus;
        if ((status & DRAGGING) == 0) {
            onStateChange(status | DRAGGING);
        }
        this.mDeltaSatisfy = false;
        float deltaX = this.mStartX - x;
        this.mDeltaX = deltaX;
        this.mDeltaY = this.mStartY - y;
        this.mAnimX.setStartValue(deltaX + x);
        this.mAnimY.setStartValue(this.mDeltaY + y);
        this.mSpringX.setResponse(this.mResponseDrag).setBounce(this.mBounceDrag);
        this.mSpringY.setResponse(this.mResponseDrag).setBounce(this.mBounceDrag);
        Log.d(TAG, "beginDrag : startSize:" + this.mStartX + COUIAccessibilityUtil.PAUSE_STRING + this.mStartY + ",startValue:" + (x + this.mDeltaX) + COUIAccessibilityUtil.PAUSE_STRING + (y + this.mDeltaY));
    }

    private void dragTo(float x, float y) {
        dragTo(x, y, false);
    }

    @SuppressLint({"NewApi"})
    private Vec2 findNeighborWithX(float x, float y, final float velocityX, ArrayList<Vec2> positions) {
        ArrayList sortedPositions = new ArrayList(positions);
        sortedPositions.sort((obj, object) -> lambda$findNeighborWithX$4(velocityX, (Vec2) obj, (Vec2) object));
        Iterator iterator = sortedPositions.iterator();
        Vec2 sameRowCandidate = null;
        Vec2 fallbackCandidate = null;
        while (true) {
            if (!iterator.hasNext()) {
                break;
            }
            Vec2 position = (Vec2) iterator.next();
            boolean movingTowardX = (velocityX < 0.0f && position.mX < x)
                    || (velocityX > 0.0f && position.mX > x);
            boolean sameY = position.mY == y;
            if (movingTowardX && sameY) {
                if (sameRowCandidate == null) {
                    sameRowCandidate = position;
                }
            } else if (movingTowardX && fallbackCandidate == null) {
                fallbackCandidate = position;
            }
        }
        if (sameRowCandidate == null) {
            sameRowCandidate = fallbackCandidate != null
                    ? fallbackCandidate
                    : (Vec2) sortedPositions.get(sortedPositions.size() - 1);
        }
        Log.d(TAG, "getAttachPosition : attachFlag-1: xOrderPosition:" + sortedPositions + ",result:" + sameRowCandidate);
        return sameRowCandidate;
    }

    @SuppressLint({"NewApi"})
    private Vec2 findNeighborWithXY(float x, float y, final float velocityX, final float velocityY,
            ArrayList<Vec2> positions) {
        ArrayList sortedPositions = new ArrayList(positions);
        sortedPositions.sort((obj, object) -> lambda$findNeighborWithXY$6(velocityY, velocityX, (Vec2) obj, (Vec2) object));
        Iterator iterator = sortedPositions.iterator();
        Vec2 bothChangedCandidate = null;
        Vec2 yChangedSameXCandidate = null;
        Vec2 xChangedSameYCandidate = null;
        Vec2 yChangedOnlyCandidate = null;
        Vec2 xChangedOnlyCandidate = null;
        while (true) {
            if (!iterator.hasNext()) {
                break;
            }
            Vec2 position = (Vec2) iterator.next();
            boolean movingTowardY = (velocityY < 0.0f && position.mY < y)
                    || (velocityY > 0.0f && position.mY > y);
            boolean movingTowardX = (velocityX < 0.0f && position.mX < x)
                    || (velocityX > 0.0f && position.mX > x);
            boolean sameY = position.mY == y;
            boolean sameX = position.mX == x;
            if (movingTowardX && movingTowardY) {
                if (bothChangedCandidate == null) {
                    bothChangedCandidate = position;
                }
            } else if (movingTowardY && sameX) {
                if (yChangedSameXCandidate == null) {
                    yChangedSameXCandidate = position;
                }
            } else if (movingTowardX && sameY) {
                if (xChangedSameYCandidate == null) {
                    xChangedSameYCandidate = position;
                }
            } else if (movingTowardY) {
                if (yChangedOnlyCandidate == null) {
                    yChangedOnlyCandidate = position;
                }
            } else if (movingTowardX && xChangedOnlyCandidate == null) {
                xChangedOnlyCandidate = position;
            }
        }
        Vec2 result = bothChangedCandidate != null ? bothChangedCandidate
                : yChangedSameXCandidate != null ? yChangedSameXCandidate
                : xChangedSameYCandidate != null ? xChangedSameYCandidate
                : yChangedOnlyCandidate != null ? yChangedOnlyCandidate
                : xChangedOnlyCandidate != null ? xChangedOnlyCandidate
                : (Vec2) sortedPositions.get(sortedPositions.size() - 1);
        Log.d(TAG, "getAttachPosition : attachFlag-3: orderPosition:" + sortedPositions
                + ",yChangedXEqual:" + yChangedSameXCandidate
                + ",xChangedYEqual:" + xChangedSameYCandidate
                + ",allChanged:" + bothChangedCandidate
                + ",yChangedOnly:" + yChangedOnlyCandidate
                + ",xChangedOnly:" + xChangedOnlyCandidate
                + ",result:" + result);
        return result;
    }

    @SuppressLint({"NewApi"})
    private Vec2 findNeighborWithY(float x, float y, final float velocityY, ArrayList<Vec2> positions) {
        ArrayList sortedPositions = new ArrayList(positions);
        sortedPositions.sort((obj, object) -> lambda$findNeighborWithY$5(velocityY, (Vec2) obj, (Vec2) object));
        Iterator iterator = sortedPositions.iterator();
        Vec2 sameColumnCandidate = null;
        Vec2 fallbackCandidate = null;
        while (true) {
            if (!iterator.hasNext()) {
                break;
            }
            Vec2 position = (Vec2) iterator.next();
            boolean movingTowardY = (velocityY < 0.0f && position.mY < y)
                    || (velocityY > 0.0f && position.mY > y);
            boolean sameX = position.mX == x;
            if (movingTowardY && sameX) {
                if (sameColumnCandidate == null) {
                    sameColumnCandidate = position;
                }
            } else if (movingTowardY && fallbackCandidate == null) {
                fallbackCandidate = position;
            }
        }
        if (sameColumnCandidate == null) {
            sameColumnCandidate = fallbackCandidate != null
                    ? fallbackCandidate
                    : (Vec2) sortedPositions.get(sortedPositions.size() - 1);
        }
        Log.d(TAG, "getAttachPosition : attachFlag-2: yOrderPosition:" + sortedPositions + ",result:" + sameColumnCandidate);
        return sameColumnCandidate;
    }

    private float getSpringApproximateOffset(float delta, float maxDistance) {
        if (delta == 0.0f || maxDistance == 0.0f) {
            return 0.0f;
        }
        float denominator = ((this.mCurveRatio * delta) / maxDistance) + 1.0f;
        if (denominator == 0.0f) {
            return 0.0f;
        }
        return (1.0f - (1.0f / denominator)) * maxDistance;
    }

    private void initOrResetVelocityTracker() {
        VelocityTracker velocityTracker = this.mVelocityTracker;
        if (velocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        } else {
            velocityTracker.clear();
        }
    }

    private void initVelocityTrackerIfNotExists() {
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
    }

    public static int lambda$findNeighborWithX$4(float velocityX, Vec2 first, Vec2 second) {
        int xCompare = (int) (first.mX - second.mX);
        if (xCompare != 0) {
            return velocityX > 0.0f ? xCompare : -xCompare;
        }
        int yCompare = (int) (first.mY - second.mY);
        return velocityX > 0.0f ? yCompare : -yCompare;
    }

    public static int lambda$findNeighborWithXY$6(float velocityY, float velocityX, Vec2 first, Vec2 second) {
        int yCompare = (int) (first.mY - second.mY);
        if (yCompare != 0) {
            return velocityY > 0.0f ? yCompare : -yCompare;
        }
        int xCompare = (int) (first.mX - second.mX);
        return velocityX > 0.0f ? xCompare : -xCompare;
    }

    public static int lambda$findNeighborWithY$5(float velocityY, Vec2 first, Vec2 second) {
        int yCompare = (int) (first.mY - second.mY);
        if (yCompare != 0) {
            return velocityY > 0.0f ? yCompare : -yCompare;
        }
        int xCompare = (int) (first.mX - second.mX);
        return velocityY > 0.0f ? xCompare : -xCompare;
    }

    public void lambda$new$0(COUIDynamicAnimation animation, float value, float velocity) {
        if (value != this.mCurX) {
            this.mCurX = value;
            this.mSpringChangeObserver.onSizeChange(value, this.mCurY);
        }
        int status = this.mStatus;
        if ((status & ANIM_X_RUNNING) == 0) {
            onStateChange(status | ANIM_X_RUNNING);
        }
    }

    public void lambda$new$1(COUIDynamicAnimation animation, boolean canceled, float value, float velocity) {
        int status = this.mStatus;
        if ((status & ANIM_X_RUNNING) != 0) {
            onStateChange(status & ~ANIM_X_RUNNING);
        }
    }

    public void lambda$new$2(COUIDynamicAnimation animation, float value, float velocity) {
        if (value != this.mCurY) {
            this.mCurY = value;
            this.mSpringChangeObserver.onSizeChange(this.mCurX, value);
        }
        int status = this.mStatus;
        if ((status & ANIM_Y_RUNNING) == 0) {
            onStateChange(status | ANIM_Y_RUNNING);
        }
    }

    public void lambda$new$3(COUIDynamicAnimation animation, boolean canceled, float value, float velocity) {
        int status = this.mStatus;
        if ((status & ANIM_Y_RUNNING) != 0) {
            onStateChange(status & ~ANIM_Y_RUNNING);
        }
    }

    private void loadStableList(ArrayList<Vec2> stableList) {
        this.mStableList = stableList;
        if (stableList == null || stableList.size() < 1) {
            this.mMinPosition.set(this.mCustomMin);
            this.mMaxPosition.set(this.mCustomMax);
            Log.d(TAG, "COUISpringDragHelper : stableList is empty");
            return;
        }
        float maxX = Float.MIN_VALUE;
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxY = Float.MIN_VALUE;
        for (Vec2 position : stableList) {
            minX = Math.min(minX, position.mX);
            minY = Math.min(minY, position.mY);
            maxX = Math.max(maxX, position.mX);
            maxY = Math.max(maxY, position.mY);
        }
        this.mMinPosition.set(minX, minY);
        this.mMaxPosition.set(maxX, maxY);
        Log.d(TAG, "COUISpringDragHelper : stableList:" + stableList);
    }

    private void onStateChange(int newStatus) {
        int oldStatus = this.mStatus;
        if (newStatus == oldStatus) {
            return;
        }
        int newIdle = 0;
        int oldIdle = ((oldStatus & ANIM_X_RUNNING) == 0
                && (oldStatus & ANIM_Y_RUNNING) == 0
                && (oldStatus & DRAGGING) == 0) ? 1 : 0;
        if ((newStatus & ANIM_X_RUNNING) == 0
                && (newStatus & ANIM_Y_RUNNING) == 0
                && (newStatus & DRAGGING) == 0) {
            newIdle = 1;
        }
        if (oldIdle != newIdle) {
            this.mSpringChangeObserver.onStateChange(newIdle ^ 1);
        }
        this.mStatus = newStatus;
    }

    private void recycleVelocityTracker() {
        VelocityTracker velocityTracker = this.mVelocityTracker;
        if (velocityTracker != null) {
            velocityTracker.recycle();
            this.mVelocityTracker = null;
        }
    }

    private void resetDrag() {
        int status = this.mStatus;
        if ((status & DRAGGING) != 0) {
            onStateChange(status & ~DRAGGING);
        }
        this.mDeltaSatisfy = false;
        this.mDownX = Float.MIN_VALUE;
        this.mDownY = Float.MIN_VALUE;
        this.mStartX = Float.MIN_VALUE;
        this.mStartY = Float.MIN_VALUE;
        this.mDeltaX = Float.MIN_VALUE;
        this.mDeltaY = Float.MIN_VALUE;
    }

    public void cancelDrag() {
        this.mAnimX.cancelComplete();
        this.mAnimY.cancelComplete();
    }

    public Vec2 getAttachPosition() {
        float velocityX = 0.0f;
        float velocityY = 0.0f;
        if (mVelocityTracker != null) {
            mVelocityTracker.computeCurrentVelocity(MILLISECOND_VELOCITY_UNIT, MAX_VELOCITY);
            velocityX = (int) mVelocityTracker.getXVelocity();
            velocityY = (int) mVelocityTracker.getYVelocity();
        }
        float curX = mFloatValueHolderX.getValue();
        float curY = mFloatValueHolderY.getValue();
        boolean outOfBounds = curX < mMinPosition.mX || curX > mMaxPosition.mX
                || curY < mMinPosition.mY || curY > mMaxPosition.mY;
        ArrayList<Vec2> attachList;
        float attachVelocityX;
        float attachVelocityY;
        if (outOfBounds && (mStableList == null || mStableList.size() < 1)) {
            Vec2 clampedPosition = new Vec2(curX, curY);
            if (curX < mMinPosition.mX) {
                clampedPosition.setX(mMinPosition.mX);
            } else if (curX > mMaxPosition.mX) {
                clampedPosition.setX(mMaxPosition.mX);
            }
            if (curY < mMinPosition.mY) {
                clampedPosition.setY(mMinPosition.mY);
            } else if (curY > mMaxPosition.mY) {
                clampedPosition.setY(mMaxPosition.mY);
            }
            attachList = new ArrayList<>();
            attachList.add(clampedPosition);
            attachVelocityX = 0.0f;
            attachVelocityY = 0.0f;
        } else {
            attachList = mStableList;
            attachVelocityX = velocityX;
            attachVelocityY = velocityY;
        }
        Log.d(TAG, "getAttachPosition : stableList:" + attachList + " ,velocity:" + attachVelocityX
                + COUIAccessibilityUtil.PAUSE_STRING + attachVelocityY + " ,curX:" + curX + ",curY:" + curY);
        if (attachList == null || attachList.size() < 1) {
            return null;
        }
        if (attachList.size() == 1) {
            return attachList.get(0);
        }
        int attachFlag = ATTACH_FLAG_UNSET;
        float absVelocityX = Math.abs(attachVelocityX);
        float absVelocityY = Math.abs(attachVelocityY);
        if (absVelocityX >= mTransformVelocity && absVelocityY >= mTransformVelocity && mDeltaSatisfy) {
            attachFlag = ATTACH_FLAG_XY;
        } else if (absVelocityX >= mTransformVelocity && mDeltaSatisfy) {
            attachFlag = ATTACH_FLAG_X;
        } else if (absVelocityY >= mTransformVelocity && mDeltaSatisfy) {
            attachFlag = ATTACH_FLAG_Y;
        }
        double minDistance = Double.MAX_VALUE;
        Vec2 nearest = null;
        for (Vec2 position : attachList) {
            double distance = Math.pow(position.mX - curX, 2.0d) + Math.pow(position.mY - curY, 2.0d);
            if (distance < minDistance) {
                minDistance = distance;
                nearest = position;
            }
        }
        if (nearest == null) {
            return null;
        }
        if (attachFlag == ATTACH_FLAG_X) {
            return findNeighborWithX(nearest.mX, nearest.mY, attachVelocityX, attachList);
        }
        if (attachFlag == ATTACH_FLAG_Y) {
            return findNeighborWithY(nearest.mX, nearest.mY, attachVelocityY, attachList);
        }
        if (attachFlag == ATTACH_FLAG_XY) {
            return findNeighborWithXY(nearest.mX, nearest.mY, attachVelocityX, attachVelocityY, attachList);
        }
        return nearest;
    }

    public void setAttachBounce(float bounce) {
        this.mBounceAttach = bounce;
    }

    public void setAttachProp(float response, float bounce) {
        this.mResponseAttach = response;
        this.mBounceAttach = bounce;
    }

    public void setAttachResponse(float response) {
        this.mResponseAttach = response;
    }

    public void setCurveRatio(float curveRatio) {
        this.mCurveRatio = curveRatio;
    }

    public void setDebug() {
        this.mDebug = true;
    }

    public void setDragBounce(float bounce) {
        this.mBounceDrag = bounce;
    }

    public void setDragProp(float response, float bounce) {
        this.mResponseDrag = response;
        this.mBounceDrag = bounce;
    }

    public void setDragResponse(float response) {
        this.mResponseDrag = response;
    }

    public void setMax(Vec2 max) {
        this.mCustomMax.set(max);
        ArrayList<Vec2> stableList = this.mStableList;
        if (stableList == null || stableList.size() < 1) {
            this.mMinPosition.set(this.mCustomMin);
            this.mMaxPosition.set(this.mCustomMax);
        }
    }

    public void setMin(Vec2 min) {
        this.mCustomMin.set(min);
        ArrayList<Vec2> stableList = this.mStableList;
        if (stableList == null || stableList.size() < 1) {
            this.mMinPosition.set(this.mCustomMin);
            this.mMaxPosition.set(this.mCustomMax);
        }
    }

    public void setOverDistance(float maxOverDistanceX, float maxOverDistanceY) {
        this.mMaxOverDistanceX = maxOverDistanceX;
        this.mMaxOverDistanceY = maxOverDistanceY;
    }

    public void setOverProp(float curveRatio, float maxOverDistanceX, float maxOverDistanceY) {
        this.mCurveRatio = curveRatio;
        this.mMaxOverDistanceX = maxOverDistanceX;
        this.mMaxOverDistanceY = maxOverDistanceY;
    }

    public void setStableList(ArrayList<Vec2> stableList) {
        loadStableList(stableList);
    }

    public void setStartValue(float x, float y) {
        this.mStartX = x;
        this.mStartY = y;
        this.mCurX = x;
        this.mCurY = y;
    }

    public void setTransformDistance(float transformDistance) {
        this.mTransformDistance = transformDistance;
    }

    public void setTransformProp(float transformVelocity, float transformDistance) {
        this.mTransformVelocity = transformVelocity;
        this.mTransformDistance = transformDistance;
    }

    public void setTransformVelocity(float transformVelocity) {
        this.mTransformVelocity = transformVelocity;
    }

    public void springDrag(MotionEvent motionEvent) {
        int actionMasked = motionEvent.getActionMasked();
        float rawX = motionEvent.getRawX();
        float rawY = motionEvent.getRawY();
        if (actionMasked == 0) {
            initOrResetVelocityTracker();
            this.mVelocityTracker.addMovement(motionEvent);
            beginDrag(rawX, rawY);
            return;
        }
        if (actionMasked != 1) {
            if (actionMasked == 2) {
                initVelocityTrackerIfNotExists();
                this.mVelocityTracker.addMovement(motionEvent);
                if ((this.mStatus & 4) == 0) {
                    beginDrag(rawX, rawY);
                }
                dragTo(rawX, rawY);
                return;
            }
            if (actionMasked != 3) {
                return;
            }
        }
        Vec2 attachPosition = getAttachPosition();
        if (attachPosition != null) {
            cancelDrag();
            dragTo(attachPosition.mX - this.mDeltaX, attachPosition.mY - this.mDeltaY, true);
        } else {
            dragTo(rawX, rawY);
        }
        recycleVelocityTracker();
        resetDrag();
    }

    public COUISpringDragHelper(SpringChangeObserver springChangeObserver, ArrayList<Vec2> stableList,
            float curveRatio, float maxOverDistanceX, float maxOverDistanceY,
            float transformVelocity, float transformDistance) {
        this(springChangeObserver, stableList, DEFAULT_RESPONSE_DRAG, DEFAULT_BOUNCE_DRAG, DEFAULT_RESPONSE_ATTACH, DEFAULT_BOUNCE_ATTACH,
                curveRatio, maxOverDistanceX, maxOverDistanceY, transformVelocity, transformDistance);
    }

    private void dragTo(float x, float y, boolean attach) {
        VelocityTracker velocityTracker;
        if (attach) {
            this.mSpringX.setResponse(this.mResponseAttach).setBounce(this.mBounceAttach);
            this.mSpringY.setResponse(this.mResponseAttach).setBounce(this.mBounceAttach);
        }
        if (!this.mDeltaSatisfy) {
            this.mDeltaSatisfy = Math.abs(x - this.mDownX) >= this.mTransformDistance
                    || Math.abs(y - this.mDownY) >= this.mTransformDistance;
        }
        float targetX = this.mDeltaX + x;
        float targetY = this.mDeltaY + y;
        float minX = this.mMinPosition.mX;
        if (targetX < minX) {
            targetX = this.mMinPosition.mX - getSpringApproximateOffset(minX - targetX, this.mMaxOverDistanceX);
        } else {
            float maxX = this.mMaxPosition.mX;
            if (targetX > maxX) {
                targetX = getSpringApproximateOffset(targetX - maxX, this.mMaxOverDistanceX) + this.mMaxPosition.mX;
            }
        }
        float minY = this.mMinPosition.mY;
        if (targetY < minY) {
            targetY = this.mMinPosition.mY - getSpringApproximateOffset(minY - targetY, this.mMaxOverDistanceY);
        } else {
            float maxY = this.mMaxPosition.mY;
            if (targetY > maxY) {
                targetY = getSpringApproximateOffset(targetY - maxY, this.mMaxOverDistanceY) + this.mMaxPosition.mY;
            }
        }
        if (this.mDebug) {
            Log.d(TAG, "dragTo : isAttach:" + attach + " ,down:" + this.mDownX + COUIAccessibilityUtil.PAUSE_STRING + this.mDownY + " ,x:" + x + ",y:" + y + " ,mTransformDistance:" + this.mTransformDistance + " ,mDeltaSatisfy:" + this.mDeltaSatisfy + " ,target:" + (x + this.mDeltaX) + COUIAccessibilityUtil.PAUSE_STRING + (y + this.mDeltaY) + " ,minPosition:" + this.mMinPosition.mX + COUIAccessibilityUtil.PAUSE_STRING + this.mMinPosition.mY + " ,maxPosition:" + this.mMaxPosition.mX + COUIAccessibilityUtil.PAUSE_STRING + this.mMaxPosition.mY + " ,limitTarget:" + targetX + COUIAccessibilityUtil.PAUSE_STRING + targetY + " ,curValue:" + this.mFloatValueHolderX.getValue() + COUIAccessibilityUtil.PAUSE_STRING + this.mFloatValueHolderY.getValue());
        }
        if (Math.abs(targetX - this.mFloatValueHolderX.getValue()) >= MINCHANGE
                || Math.abs(targetY - this.mFloatValueHolderY.getValue()) >= MINCHANGE) {
            if (this.mAnimX.isRunning() || this.mAnimY.isRunning()
                    || Math.abs(targetX - this.mFloatValueHolderX.getValue()) >= MOVE_DISTANCE_MIN
                    || Math.abs(targetY - this.mFloatValueHolderY.getValue()) >= MOVE_DISTANCE_MIN) {
                if (attach && (velocityTracker = this.mVelocityTracker) != null) {
                    velocityTracker.computeCurrentVelocity(MILLISECOND_VELOCITY_UNIT, MAX_VELOCITY);
                    this.mAnimX.setStartVelocity(Math.min(this.mVelocityTracker.getXVelocity(), this.mTransformVelocity));
                    this.mAnimY.setStartVelocity(Math.min(this.mVelocityTracker.getYVelocity(), this.mTransformVelocity));
                }
                this.mAnimX.animateToFinalPosition(targetX);
                this.mAnimY.animateToFinalPosition(targetY);
                int status = this.mStatus;
                if ((status & ANIM_X_RUNNING) == 0) {
                    onStateChange(status | ANIM_X_RUNNING);
                }
                int updatedStatus = this.mStatus;
                if ((updatedStatus & ANIM_Y_RUNNING) == 0) {
                    onStateChange(updatedStatus | ANIM_Y_RUNNING);
                }
            }
        }
    }

    public COUISpringDragHelper(SpringChangeObserver springChangeObserver, ArrayList<Vec2> stableList,
            float responseDrag, float bounceDrag, float responseAttach, float bounceAttach,
            float curveRatio, float maxOverDistanceX, float maxOverDistanceY,
            float transformVelocity, float transformDistance) {
        this.mCustomMin = new Vec2(DEFAULT_MIN_SIZE, DEFAULT_MIN_SIZE);
        this.mCustomMax = new Vec2(DEFAULT_MAX_SIZE, DEFAULT_MAX_SIZE);
        this.mMinPosition = new Vec2(DEFAULT_MIN_SIZE, DEFAULT_MIN_SIZE);
        this.mMaxPosition = new Vec2(DEFAULT_MAX_SIZE, DEFAULT_MAX_SIZE);
        androidx.dynamicanimation.animation.FloatValueHolder valueHolderX = new androidx.dynamicanimation.animation.FloatValueHolder();
        this.mFloatValueHolderX = valueHolderX;
        androidx.dynamicanimation.animation.FloatValueHolder valueHolderY = new androidx.dynamicanimation.animation.FloatValueHolder();
        this.mFloatValueHolderY = valueHolderY;
        this.mDownX = Float.MIN_VALUE;
        this.mDownY = Float.MIN_VALUE;
        this.mStartX = Float.MIN_VALUE;
        this.mStartY = Float.MIN_VALUE;
        this.mDeltaX = Float.MIN_VALUE;
        this.mDeltaY = Float.MIN_VALUE;
        this.mDeltaSatisfy = false;
        this.mStatus = 0;
        this.mDebug = false;
        this.mResponseDrag = responseDrag;
        this.mBounceDrag = bounceDrag;
        this.mResponseAttach = responseAttach;
        this.mBounceAttach = bounceAttach;
        this.mCurveRatio = curveRatio;
        this.mMaxOverDistanceX = maxOverDistanceX;
        this.mMaxOverDistanceY = maxOverDistanceY;
        this.mTransformVelocity = transformVelocity;
        this.mTransformDistance = transformDistance;
        this.mSpringChangeObserver = springChangeObserver;
        loadStableList(stableList);
        COUISpringForce bounce = new COUISpringForce(0.0f).setResponse(this.mResponseDrag).setBounce(this.mBounceDrag);
        this.mSpringX = bounce;
        COUISpringForce bounce2 = new COUISpringForce(0.0f).setResponse(this.mResponseDrag).setBounce(this.mBounceDrag);
        this.mSpringY = bounce2;
        COUISpringAnimation spring = new COUISpringAnimation(valueHolderX).setSpring(bounce);
        this.mAnimX = spring;
        COUISpringAnimation spring2 = new COUISpringAnimation(valueHolderY).setSpring(bounce2);
        this.mAnimY = spring2;
        spring.addUpdateListener((animation, value, velocity) ->
                lambda$new$0(animation, value, velocity));
        spring.addEndListener((animation, canceled, value, velocity) ->
                lambda$new$1(animation, canceled, value, velocity));
        spring2.addUpdateListener((animation, value, velocity) ->
                lambda$new$2(animation, value, velocity));
        spring2.addEndListener((animation, canceled, value, velocity) ->
                lambda$new$3(animation, canceled, value, velocity));
    }
}

