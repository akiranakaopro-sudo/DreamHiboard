package com.oplus.physicsengine.engine;

import android.graphics.RectF;

public final class AttachmentBehavior extends ConstraintBehavior {
    public int mAttachmentType;
    public float mBottomSideChangedThreshold;
    public int mBoundsSide;
    public final float mChangeSideVelocityLimit = 1000.0f;
    public int mCurrentSide;
    public float mCustomLinearDamping;
    public float mLeftSideChangedThreshold;
    public float mOriginLinearDamping;
    public float mRightSideChangedThreshold;
    public float mTopSideChangedThreshold;

    public AttachmentBehavior(RectF rectF) {
        super(rectF, 0);
        mAttachmentType = 0;
        mBoundsSide = 15;
        mCurrentSide = 0;
        mLeftSideChangedThreshold = 0.5f;
        mRightSideChangedThreshold = 0.5f;
        mTopSideChangedThreshold = 0.5f;
        mBottomSideChangedThreshold = 0.5f;
    }

    public void attachCloseSideWithConstraint() {
        convertToCloseSide();
        calculateConstraintPosition();
    }

    public void calculateConstraintPosition() {
        super.calculateConstraintPosition();
        switch (mCurrentSide) {
            case 1:
                mConstraintPointX = mConstraintRect.left;
                break;
            case 2:
                mConstraintPointX = mConstraintRect.right;
                break;
            case 4:
                mConstraintPointY = mConstraintRect.top;
                break;
            case 8:
                mConstraintPointY = mConstraintRect.bottom;
                break;
            default:
                break;
        }
    }

    public void convertToBottom() {
        mCurrentSide = 8;
    }

    public void convertToCloseSide() {
        if (mPropertyBody == null) {
            mCurrentSide = 1;
            return;
        }
        float x = mPropertyBody.mWorldCenter.mX;
        float y = mPropertyBody.mWorldCenter.mY;
        float left = Math.abs(x - mConstraintRect.left);
        float right = Math.abs(x - mConstraintRect.right);
        float top = Math.abs(y - mConstraintRect.top);
        float bottom = Math.abs(y - mConstraintRect.bottom);
        float min = Math.min(Math.min(left, right), Math.min(top, bottom));
        if (min == left) {
            convertToLeft();
        } else if (min == right) {
            convertToRight();
        } else if (min == top) {
            convertToTop();
        } else {
            convertToBottom();
        }
    }

    public void convertToLeft() {
        mCurrentSide = 1;
    }

    public void convertToRight() {
        mCurrentSide = 2;
    }

    public void convertToTop() {
        mCurrentSide = 4;
    }

    public int getCurrentBodySide() {
        return mCurrentSide;
    }

    public int getType() {
        return 3;
    }

    public void handlePositionChanging() {
        super.handlePositionChanging();
        if (mAttachmentType == 0) {
            convertToCloseSide();
        }
    }

    public void setAttachmentType(int attachmentType) {
        mAttachmentType = attachmentType;
    }

    public void setBottomSideChangedThreshold(float threshold) {
        mBottomSideChangedThreshold = threshold;
    }

    public void setBoundsSide(int boundsSide) {
        mBoundsSide = boundsSide;
    }

    public void setLeftSideChangedThreshold(float threshold) {
        mLeftSideChangedThreshold = threshold;
    }

    public AttachmentBehavior setLinearDamping(float linearDamping) {
        mCustomLinearDamping = linearDamping;
        return this;
    }

    public void setRightSideChangedThreshold(float threshold) {
        mRightSideChangedThreshold = threshold;
    }

    public void setTopSideChangedThreshold(float threshold) {
        mTopSideChangedThreshold = threshold;
    }

    public void start() {
        if (mCustomLinearDamping != 0.0f && mPropertyBody != null) {
            mOriginLinearDamping = mPropertyBody.mLinearDamping;
            mPropertyBody.setLinearDamping(mCustomLinearDamping);
        }
        startBehavior();
    }

    public boolean stopBehavior() {
        if (mOriginLinearDamping != 0.0f && mPropertyBody != null) {
            mPropertyBody.setLinearDamping(mOriginLinearDamping);
        }
        return super.stopBehavior();
    }
}
