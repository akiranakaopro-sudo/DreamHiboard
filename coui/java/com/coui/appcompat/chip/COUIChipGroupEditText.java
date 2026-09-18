package com.coui.appcompat.chip;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.TextView;

import androidx.dynamicanimation.animation.FloatPropertyCompat;

import com.coui.appcompat.R;
import com.coui.appcompat.animation.dynamicanimation.COUISpringAnimation;
import com.coui.appcompat.animation.dynamicanimation.COUISpringForce;
import com.coui.appcompat.edittext.COUIEditText;

/**
 * The editable trailing child used by {@link COUIChipGroup}.
 *
 * <p>Ported from the decoded COUI implementation. The only method which was damaged in the
 * decompiled Java, {@code updateRealBounds}, is reconstructed instruction-for-instruction from
 * {@code COUIChipGroupEditText$ChipGroupAnimatorImpl.smali}.</p>
 */
public class COUIChipGroupEditText extends COUIEditText
        implements COUIChipGroup.IChipGroupAnimator {
    private static final int MIN_WIDTH = 10;

    private final COUIChipGroup.IChipGroupAnimator mChipGroupAnimator;
    private float mTextLength;

    public static class ChipGroupAnimatorImpl implements COUIChipGroup.IChipGroupAnimator {
        private COUIChipGroup mController;
        private COUISpringAnimation mHorizontalAnimation;
        private final COUIChipGroupEditText mHost;
        private final RectF mDrawingBounds = new RectF();
        private final RectF mRealBounds = new RectF();

        public ChipGroupAnimatorImpl(COUIChipGroupEditText host) {
            mHost = host;
            initAnimation();
        }

        private void initAnimation() {
            FloatPropertyCompat<ChipGroupAnimatorImpl> property =
                    new FloatPropertyCompat<ChipGroupAnimatorImpl>("ChipGroupAnimatorImpl") {
                        @Override
                        public float getValue(ChipGroupAnimatorImpl animator) {
                            return animator.getX();
                        }

                        @Override
                        public void setValue(ChipGroupAnimatorImpl animator, float value) {
                            animator.setX(value);
                        }
                    };
            COUISpringForce spring = new COUISpringForce();
            spring.setResponse(0.3f);
            spring.setBounce(0.0f);
            mHorizontalAnimation = new COUISpringAnimation(this, property);
            mHorizontalAnimation.setSpring(spring);
        }

        /**
         * Exact control flow reconstructed from the OPPO smali.
         */
        private void updateRealBounds(
                int left, int top, int right, int bottom, boolean animate) {
            if (mRealBounds.left == left
                    && mRealBounds.top == top
                    && mRealBounds.right == right
                    && mRealBounds.bottom == bottom) {
                return;
            }

            Editable editable = mHost.getEditableText();
            if (editable != null) {
                // The original bytecode intentionally measures here so mTextLength reflects the
                // most recent text before deciding whether a horizontal move should animate.
                mHost.getPaint().measureText(editable.toString());
            }

            if (animate) {
                if (!mRealBounds.isEmpty()
                        && mHost.mTextLength == 0.0f
                        && mRealBounds.top == top
                        && mRealBounds.left != left
                        && mDrawingBounds.left != left) {
                    mHorizontalAnimation.animateToFinalPosition(left);
                    mHost.setX(mDrawingBounds.left);
                } else {
                    mDrawingBounds.set(left, top, right, bottom);
                    mHost.setX(mDrawingBounds.left);
                }
            } else {
                mDrawingBounds.set(left, top, right, bottom);
            }
            mRealBounds.set(left, top, right, bottom);
        }

        @Override
        public void bindController(COUIChipGroup controller) {
            if (mController == null) {
                mController = controller;
            }
        }

        @Override
        public void forceFinishAllAnimation() {
            if (mHorizontalAnimation.canSkipToEnd()) {
                mHorizontalAnimation.skipToEnd();
            }
        }

        @Override
        public void getDrawingBounds(RectF outBounds) {
            outBounds.set(mDrawingBounds);
        }

        public float getX() {
            return mDrawingBounds.left;
        }

        @Override
        public boolean isChipAnimationRunning() {
            return mHorizontalAnimation.isRunning();
        }

        @Override
        public void resetChipGroupAnimations() {
            mHorizontalAnimation.cancel();
            mHost.setTranslationX(0.0f);
        }

        public void setX(float x) {
            mDrawingBounds.offsetTo(x, mDrawingBounds.top);
            mHost.setX(mDrawingBounds.left);
        }

        @Override
        public void unbindController() {
            mHorizontalAnimation.cancel();
            mHost.setTranslationX(0.0f);
            mRealBounds.setEmpty();
            mDrawingBounds.setEmpty();
            mController = null;
        }

        @Override
        public void updateAttachState(boolean attached, boolean animate) {
        }

        @Override
        public void updateChipRealBounds(
                int left, int top, int right, int bottom, boolean animate) {
            updateRealBounds(left, top, right, bottom, animate);
        }
    }

    public COUIChipGroupEditText(Context context) {
        super(context);
        mChipGroupAnimator = new ChipGroupAnimatorImpl(this);
        init();
    }

    public COUIChipGroupEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        mChipGroupAnimator = new ChipGroupAnimatorImpl(this);
        init();
    }

    public COUIChipGroupEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mChipGroupAnimator = new ChipGroupAnimatorImpl(this);
        init();
    }

    private void init() {
        setPadding(0, 0, 0, 0);
        setTextAppearance(getContext(), R.style.couiTextAppearanceBodyL);
        setMinimumHeight(0);
        setImeOptions(6);
        setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                if (actionId != 6 && actionId != 0) {
                    return false;
                }
                requestFocus();
                return true;
            }
        });
        setBackground(null);
    }

    @Override
    public void bindController(COUIChipGroup controller) {
        mChipGroupAnimator.bindController(controller);
    }

    public void ensureMinWidth() {
        setMinWidth(MIN_WIDTH);
    }

    @Override
    public void forceFinishAllAnimation() {
        mChipGroupAnimator.forceFinishAllAnimation();
    }

    @Override
    public void getDrawingBounds(RectF outBounds) {
        mChipGroupAnimator.getDrawingBounds(outBounds);
    }

    public int getEdittextMinWidth() {
        return MIN_WIDTH;
    }

    @Override
    public boolean isChipAnimationRunning() {
        return mChipGroupAnimator.isChipAnimationRunning();
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Editable editable = getEditableText();
        if (editable != null) {
            mTextLength = getPaint().measureText(editable.toString());
        }
    }

    @Override
    public void resetChipGroupAnimations() {
        mChipGroupAnimator.resetChipGroupAnimations();
    }

    @Override
    public void unbindController() {
        mChipGroupAnimator.unbindController();
    }

    @Override
    public void updateAttachState(boolean attached, boolean animate) {
        mChipGroupAnimator.updateAttachState(attached, animate);
    }

    @Override
    public void updateChipRealBounds(
            int left, int top, int right, int bottom, boolean animate) {
        mChipGroupAnimator.updateChipRealBounds(left, top, right, bottom, animate);
    }
}
