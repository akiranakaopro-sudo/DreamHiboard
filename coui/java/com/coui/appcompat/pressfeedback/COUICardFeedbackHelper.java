package com.coui.appcompat.pressfeedback;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;

import com.coui.appcompat.animation.COUIMoveEaseInterpolator;

public class COUICardFeedbackHelper {
    private static final float CARD_DRAG_BASE_RATIO = 0.5f;
    private static final float CARD_DRAG_DISTANCE_DEFAULT = 70.0f;
    private static final float CARD_DRAG_TANGENT_MIN = 1.0f;
    private static final long CARD_MOVE_BACK_ANIM_DURATION = 300L;
    private static final long CARD_PRESS_ANIM_DURATION = 200L;
    private static final long CARD_RELEASE_ANIM_DURATION = 340L;
    private static final float CARD_SCALE_MAX = 1.0f;
    private static final float CARD_SCALE_PRESS_DEFAULT = 0.98f;
    public static final int DRAG_DIRECTION_ALL = 15;
    public static final int DRAG_DIRECTION_DOWN = 8;
    public static final int DRAG_DIRECTION_HORIZONTAL = 3;
    public static final int DRAG_DIRECTION_LEFT = 1;
    public static final int DRAG_DIRECTION_NONE = 0;
    public static final int DRAG_DIRECTION_RIGHT = 2;
    public static final int DRAG_DIRECTION_UP = 4;
    public static final int DRAG_DIRECTION_VERTICAL = 12;
    private static final float UNSET = Float.MAX_VALUE;
    private static final Interpolator CARD_PRESS_ANIM_INTERPOLATOR =
            new PathInterpolator(0.4f, 0.0f, 0.2f, 1.0f);
    private static final Interpolator CARD_RELEASE_ANIM_INTERPOLATOR =
            new PathInterpolator(0.0f, 0.0f, 0.2f, 1.0f);

    private Runnable mAfterDragAction;
    private Runnable mAfterUpdateAction;
    private Runnable mBeforeDragAction;
    private Runnable mBeforeUpdateAction;
    private int mCurDragDirection = DRAG_DIRECTION_ALL;
    private float mCurTranslateX;
    private float mCurTranslateY;
    private float mDownX;
    private float mDownY;
    private int mDragDirection = DRAG_DIRECTION_NONE;
    private float mDragMaxHorizontal;
    private float mDragMaxVertical;
    private float mDragRatio = CARD_DRAG_BASE_RATIO;
    private float mDragTangent = CARD_DRAG_TANGENT_MIN;
    private boolean mEnableMoveBack = true;
    private boolean mEnableScale = true;
    private boolean mEnableSloping = true;
    private boolean mHasDrag;
    private float mLastScaleX = UNSET;
    private float mLastScaleY = UNSET;
    private float mLastX;
    private float mLastY;
    private float mMinScale = CARD_SCALE_PRESS_DEFAULT;
    private final ValueAnimator mMoveBackAnimator;
    private float mMoveX;
    private float mMoveY;
    private final ValueAnimator mPressAnimator;
    private final View mProxyView;
    private final ValueAnimator mReleaseAnimator;
    private float mStartTranslateX;
    private float mStartTranslateY;

    public COUICardFeedbackHelper(View view) {
        mPressAnimator = ValueAnimator.ofFloat(0.0f, 1.0f).setDuration(CARD_PRESS_ANIM_DURATION);
        mReleaseAnimator = ValueAnimator.ofFloat(0.0f, 1.0f).setDuration(CARD_RELEASE_ANIM_DURATION);
        mMoveBackAnimator = ValueAnimator.ofFloat(0.0f, 1.0f).setDuration(CARD_MOVE_BACK_ANIM_DURATION);
        mProxyView = view;
        mPressAnimator.setInterpolator(CARD_PRESS_ANIM_INTERPOLATOR);
        mReleaseAnimator.setInterpolator(CARD_RELEASE_ANIM_INTERPOLATOR);
        mMoveBackAnimator.setInterpolator(new COUIMoveEaseInterpolator());
        initPressAnim();
        initReleaseAnim();
        initMoveBackAnim();
        resetTouchParams();
        float defaultDragDistance = dp2px(view.getContext(), CARD_DRAG_DISTANCE_DEFAULT) * 1.0f;
        mDragMaxVertical = defaultDragDistance;
        mDragMaxHorizontal = defaultDragDistance;
    }

    private static int dp2px(Context context, float dp) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics()));
    }

    private float getRealDragDistance(float delta, float start, float current, float maxDistance) {
        return delta * mDragRatio * (1.0f - (Math.abs(current - start) / maxDistance));
    }

    private void initMoveBackAnim() {
        mMoveBackAnimator.addUpdateListener(animator -> {
            float fraction = (Float) animator.getAnimatedValue();
            if (mBeforeUpdateAction != null) {
                mBeforeUpdateAction.run();
            }
            mProxyView.setTranslationX(
                    mCurTranslateX - ((mCurTranslateX - mStartTranslateX) * fraction));
            mProxyView.setTranslationY(
                    mCurTranslateY - ((mCurTranslateY - mStartTranslateY) * fraction));
            if (mAfterUpdateAction != null) {
                mAfterUpdateAction.run();
            }
        });
        mMoveBackAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                mStartTranslateX = UNSET;
                mStartTranslateY = UNSET;
                mCurDragDirection = DRAG_DIRECTION_ALL;
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });
    }

    private void initPressAnim() {
        mPressAnimator.addUpdateListener(animator -> {
            float fraction = (Float) animator.getAnimatedValue();
            if (mBeforeUpdateAction != null) {
                mBeforeUpdateAction.run();
            }
            mProxyView.setScaleX(mLastScaleX - ((mLastScaleY - mMinScale) * fraction));
            mProxyView.setScaleY(mLastScaleY - ((mLastScaleY - mMinScale) * fraction));
            if (mAfterUpdateAction != null) {
                mAfterUpdateAction.run();
            }
        });
    }

    private void initReleaseAnim() {
        mReleaseAnimator.addUpdateListener(animator -> {
            float fraction = (Float) animator.getAnimatedValue();
            if (mBeforeUpdateAction != null) {
                mBeforeUpdateAction.run();
            }
            mProxyView.setScaleX(mLastScaleX + ((CARD_SCALE_MAX - mLastScaleX) * fraction));
            mProxyView.setScaleY(mLastScaleY + ((CARD_SCALE_MAX - mLastScaleY) * fraction));
            if (mAfterUpdateAction != null) {
                mAfterUpdateAction.run();
            }
        });
    }

    private void onDrag(float x, float y) {
        if (!mHasDrag) {
            mHasDrag = true;
            if (mBeforeDragAction != null) {
                mBeforeDragAction.run();
            }
        }
        if (mStartTranslateX == UNSET || mStartTranslateY == UNSET) {
            mStartTranslateX = mProxyView.getTranslationX();
            mStartTranslateY = mProxyView.getTranslationY();
        }
        int dragDirection = mDragDirection;
        float minX = (dragDirection & DRAG_DIRECTION_LEFT) != 0
                ? mStartTranslateX - mDragMaxHorizontal : mStartTranslateX;
        float maxX = (dragDirection & DRAG_DIRECTION_RIGHT) != 0
                ? mStartTranslateX + mDragMaxHorizontal : mStartTranslateX;
        float minY = (dragDirection & DRAG_DIRECTION_UP) != 0
                ? mStartTranslateY - mDragMaxVertical : mStartTranslateY;
        float maxY = (dragDirection & DRAG_DIRECTION_DOWN) != 0
                ? mStartTranslateY + mDragMaxVertical : mStartTranslateY;
        float targetX = (dragDirection & DRAG_DIRECTION_HORIZONTAL) != 0
                ? Math.max(minX, Math.min(maxX,
                        mProxyView.getTranslationX() + getRealDragDistance(
                                x - mLastX, mStartTranslateX, mProxyView.getTranslationX(),
                                mDragMaxHorizontal)))
                : UNSET;
        float targetY = (dragDirection & DRAG_DIRECTION_VERTICAL) != 0
                ? Math.max(minY, Math.min(maxY,
                        mProxyView.getTranslationY() + getRealDragDistance(
                                y - mLastY, mStartTranslateY, mProxyView.getTranslationY(),
                                mDragMaxVertical)))
                : UNSET;
        if (!mEnableSloping && mCurDragDirection == DRAG_DIRECTION_ALL) {
            if (targetX != UNSET && targetY != UNSET) {
                mCurDragDirection = Math.abs(mStartTranslateX - targetX)
                        > Math.abs(mStartTranslateY - targetY)
                        ? DRAG_DIRECTION_HORIZONTAL : DRAG_DIRECTION_VERTICAL;
            }
            if (targetX != UNSET && targetY == UNSET) {
                mCurDragDirection = DRAG_DIRECTION_HORIZONTAL;
            }
            if (targetX == UNSET && targetY != UNSET) {
                mCurDragDirection = DRAG_DIRECTION_VERTICAL;
            }
        }
        if (mBeforeUpdateAction != null) {
            mBeforeUpdateAction.run();
        }
        if (targetX != UNSET && (mCurDragDirection & DRAG_DIRECTION_HORIZONTAL) != 0) {
            mProxyView.setTranslationX(targetX);
        }
        if (targetY != UNSET && (mCurDragDirection & DRAG_DIRECTION_VERTICAL) != 0) {
            mProxyView.setTranslationY(targetY);
        }
        if (mAfterUpdateAction != null) {
            mAfterUpdateAction.run();
        }
    }

    private void resetTouchParams() {
        mDownX = UNSET;
        mDownY = UNSET;
        mLastX = UNSET;
        mLastY = UNSET;
        mMoveX = UNSET;
        mMoveY = UNSET;
    }

    private void startMoveBack() {
        if (mDownX == UNSET || mDownY == UNSET || !mHasDrag) {
            return;
        }
        mHasDrag = false;
        if (mAfterDragAction != null) {
            mAfterDragAction.run();
        }
        if (!mEnableMoveBack) {
            return;
        }
        mCurTranslateX = mProxyView.getTranslationX();
        mCurTranslateY = mProxyView.getTranslationY();
        if ((mCurTranslateX == mStartTranslateX || mStartTranslateX == UNSET)
                && (mCurTranslateY == mStartTranslateY || mStartTranslateY == UNSET)) {
            return;
        }
        mMoveBackAnimator.start();
    }

    private void startPressAnim() {
        if (!mEnableScale) {
            return;
        }
        if (mReleaseAnimator.isRunning()) {
            mReleaseAnimator.pause();
        }
        mLastScaleX = mProxyView.getScaleX();
        mLastScaleY = mProxyView.getScaleY();
        if (mLastScaleX == mMinScale && mLastScaleY == mMinScale) {
            return;
        }
        mPressAnimator.start();
    }

    private void startReleaseAnim() {
        if (!mEnableScale) {
            return;
        }
        if (mPressAnimator.isRunning()) {
            mPressAnimator.pause();
        }
        mLastScaleX = mProxyView.getScaleX();
        mLastScaleY = mProxyView.getScaleY();
        if (mLastScaleX == CARD_SCALE_MAX && mLastScaleY == CARD_SCALE_MAX) {
            return;
        }
        mReleaseAnimator.start();
    }

    public void addMoveBackAnimatorListener(Animator.AnimatorListener listener) {
        mMoveBackAnimator.addListener(listener);
    }

    public void addMoveBackAnimatorUpdateListener(ValueAnimator.AnimatorUpdateListener listener) {
        mMoveBackAnimator.addUpdateListener(listener);
    }

    public void addPressAnimatorListener(Animator.AnimatorListener listener) {
        mPressAnimator.addListener(listener);
    }

    public void addPressAnimatorUpdateListener(ValueAnimator.AnimatorUpdateListener listener) {
        mPressAnimator.addUpdateListener(listener);
    }

    public void addReleaseAnimatorListener(Animator.AnimatorListener listener) {
        mReleaseAnimator.addListener(listener);
    }

    public void addReleaseAnimatorUpdateListener(ValueAnimator.AnimatorUpdateListener listener) {
        mReleaseAnimator.addUpdateListener(listener);
    }

    public void doAfterDrag(Runnable runnable) {
        mAfterDragAction = runnable;
    }

    public void doAfterUpdate(Runnable runnable) {
        mAfterUpdateAction = runnable;
    }

    public void doBeforeDrag(Runnable runnable) {
        mBeforeDragAction = runnable;
    }

    public void doBeforeUpdate(Runnable runnable) {
        mBeforeUpdateAction = runnable;
    }

    public int getDragDirection() {
        return mDragDirection;
    }

    public void handleTouchDown(MotionEvent event) {
        if (mMoveBackAnimator.isRunning()) {
            mMoveBackAnimator.pause();
        }
        resetTouchParams();
        mDownX = event.getRawX();
        mDownY = event.getRawY();
        mLastX = mDownX;
        mLastY = mDownY;
        startPressAnim();
    }

    public boolean handleTouchMove(MotionEvent event) {
        if (mMoveBackAnimator.isRunning()) {
            mMoveBackAnimator.pause();
        }
        if (mDownX == UNSET || mDownY == UNSET) {
            return false;
        }
        if (mMoveX == UNSET && mMoveY == UNSET) {
            float distanceX = Math.abs(event.getRawX() - mDownX);
            float distanceY = Math.abs(event.getRawY() - mDownY);
            if (distanceX == 0.0f && distanceY == 0.0f) {
                return false;
            }
            int dragDirection = mDragDirection;
            if ((dragDirection & DRAG_DIRECTION_ALL) == 0) {
                return false;
            }
            if ((dragDirection & DRAG_DIRECTION_HORIZONTAL) != 0
                    && (dragDirection & DRAG_DIRECTION_VERTICAL) == 0
                    && distanceX < mDragTangent * distanceY) {
                resetTouchParams();
                return false;
            }
            if ((dragDirection & DRAG_DIRECTION_HORIZONTAL) == 0
                    && (dragDirection & DRAG_DIRECTION_VERTICAL) != 0
                    && distanceY < distanceX * mDragTangent) {
                resetTouchParams();
                return false;
            }
        }
        mMoveX = event.getRawX();
        mMoveY = event.getRawY();
        onDrag(mMoveX, mMoveY);
        mLastX = mMoveX;
        mLastY = mMoveY;
        return true;
    }

    public void handleTouchUpOrCancel() {
        startReleaseAnim();
        startMoveBack();
        resetTouchParams();
    }

    public void pauseAnimation() {
        if (mPressAnimator.isRunning()) {
            mPressAnimator.pause();
        }
        if (mReleaseAnimator.isRunning()) {
            mReleaseAnimator.pause();
        }
        if (mMoveBackAnimator.isRunning()) {
            mMoveBackAnimator.pause();
        }
    }

    public void setDragDirection(int dragDirection) {
        mDragDirection = dragDirection;
    }

    public void setDragMaxDistance(float horizontalDp, float verticalDp) {
        mDragMaxHorizontal = dp2px(mProxyView.getContext(), horizontalDp) * 1.0f;
        mDragMaxVertical = dp2px(mProxyView.getContext(), verticalDp) * 1.0f;
    }

    public void setDragRatio(float dragRatio) {
        mDragRatio = dragRatio;
    }

    public void setDragTangent(float dragTangent) {
        mDragTangent = dragTangent;
    }

    public void setEnableDragOnSloping(boolean enableSloping) {
        mEnableSloping = enableSloping;
    }

    public void setEnableMoveBack(boolean enableMoveBack) {
        mEnableMoveBack = enableMoveBack;
    }

    public void setEnableScale(boolean enableScale) {
        mEnableScale = enableScale;
    }

    public void setMinScale(float minScale) {
        mMinScale = minScale;
    }

    public void setMoveBackDuration(long duration) {
        mMoveBackAnimator.setDuration(duration);
    }

    public void setMoveBackInterpolator(Interpolator interpolator) {
        mMoveBackAnimator.setInterpolator(interpolator);
    }

    public void setPressDuration(long duration) {
        mPressAnimator.setDuration(duration);
    }

    public void setPressInterpolator(Interpolator interpolator) {
        mPressAnimator.setInterpolator(interpolator);
    }

    public void setReleaseDuration(long duration) {
        mReleaseAnimator.setDuration(duration);
    }

    public void setReleaseInterpolator(Interpolator interpolator) {
        mReleaseAnimator.setInterpolator(interpolator);
    }
}
