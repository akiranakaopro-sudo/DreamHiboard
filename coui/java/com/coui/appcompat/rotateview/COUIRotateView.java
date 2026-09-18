package com.coui.appcompat.rotateview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.animation.Interpolator;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.view.animation.PathInterpolatorCompat;

import com.coui.appcompat.R;
import com.coui.appcompat.log.COUILog;

public class COUIRotateView extends AppCompatImageView {
    private static final int ROTATE_ALONG_X = 0;
    private static final int ROTATE_ALONG_Z = 1;
    private static final int ROTATION_ANGLE = 180;
    private static final String TAG = "COUIRotateView";
    private static final int[] STATE_CONTENT_EXPANDED = {R.attr.supportExpanded};
    private static final int[] STATE_CONTENT_COLLAPSED = {R.attr.supportCollapsed};
    private static final int[] STATE_CONTENT_EXPANDED_ANIM = {R.attr.supportExpandedAnimate};
    private static final int[] STATE_CONTENT_COLLAPSED_ANIM = {R.attr.supportCollapsedAnimate};

    private String mCloseContentDescription;
    private long mDuration;
    private String mExpandContentDescription;
    private Interpolator mInterpolator;
    private boolean mIsExpanded;
    private boolean mIsRotating;
    private OnRotateStateChangeListener mOnRotateStateChangeListener;
    private int mRotateType;

    public interface OnRotateStateChangeListener {
        void onRotateStateChange(boolean expanded);
    }

    public COUIRotateView(Context context) {
        this(context, null);
    }

    public COUIRotateView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public COUIRotateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, 0);
        mInterpolator = PathInterpolatorCompat.create(0.133f, 0.0f, 0.3f, 1.0f);
        mDuration = 400L;
        mIsExpanded = false;
        mIsRotating = false;
        mOnRotateStateChangeListener = null;
        if (getContext() != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.COUIRotateView);
            mRotateType = a.getInteger(R.styleable.COUIRotateView_supportRotateType, ROTATE_ALONG_X);
            mIsExpanded = a.getBoolean(R.styleable.COUIRotateView_supportExpanded, false);
            a.recycle();
        }
        if (mRotateType == ROTATE_ALONG_Z) {
            animate().setDuration(mDuration).setInterpolator(mInterpolator)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationCancel(Animator animation) {
                            mIsRotating = false;
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mIsRotating = false;
                        }

                        @Override
                        public void onAnimationStart(Animator animation) {
                            mIsRotating = true;
                        }
                    });
        } else if (mRotateType == ROTATE_ALONG_X) {
            setState(true);
        }
        mExpandContentDescription = getContext().getString(R.string.coui_toolar_expand_button_description);
        mCloseContentDescription = getContext().getString(R.string.coui_toolar_close_button_description);
        updateContentDescription();
    }

    private void setState(boolean animation) {
        if (mIsExpanded) {
            if (animation) {
                setImageState(STATE_CONTENT_EXPANDED_ANIM, true);
            } else {
                setImageState(STATE_CONTENT_EXPANDED, true);
            }
        } else if (animation) {
            setImageState(STATE_CONTENT_COLLAPSED_ANIM, true);
        } else {
            setImageState(STATE_CONTENT_COLLAPSED, true);
        }
    }

    private void updateContentDescription() {
        CharSequence contentDescription = super.getContentDescription();
        if (TextUtils.isEmpty(contentDescription)
                || TextUtils.equals(contentDescription, mExpandContentDescription)
                || TextUtils.equals(contentDescription, mCloseContentDescription)) {
            setContentDescription(mIsExpanded ? mExpandContentDescription : mCloseContentDescription);
        } else {
            COUILog.e(TAG, "The user has set the content description, so the default description does not take effect.");
        }
    }

    public boolean isExpanded() {
        return mIsExpanded;
    }

    public void setExpanded(boolean expanded) {
        setExpanded(expanded, true);
    }

    public void setExpanded(boolean expanded, boolean animation) {
        if (mIsExpanded == expanded) {
            return;
        }
        if (mRotateType == ROTATE_ALONG_Z) {
            if (mIsRotating) {
                return;
            }
            mIsExpanded = expanded;
            if (animation) {
                animate().rotation(expanded ? ROTATION_ANGLE : 0.0f);
            } else {
                setRotation(expanded ? ROTATION_ANGLE : 0.0f);
            }
        } else if (mRotateType == ROTATE_ALONG_X) {
            mIsExpanded = expanded;
            setState(animation);
        }
        OnRotateStateChangeListener listener = mOnRotateStateChangeListener;
        if (listener != null) {
            listener.onRotateStateChange(mIsExpanded);
        }
        updateContentDescription();
    }

    public void setOnRotateStateChangeListener(OnRotateStateChangeListener listener) {
        mOnRotateStateChangeListener = listener;
    }

    @Deprecated
    public void startCollapseAnimation() {
        if (mRotateType == ROTATE_ALONG_Z) {
            animate().rotation(0.0f);
            mIsExpanded = false;
            updateContentDescription();
        } else if (mRotateType == ROTATE_ALONG_X) {
            setExpanded(false);
        }
    }

    @Deprecated
    public void startExpandAnimation() {
        if (mRotateType == ROTATE_ALONG_Z) {
            animate().rotation(ROTATION_ANGLE);
            mIsExpanded = true;
            updateContentDescription();
        } else if (mRotateType == ROTATE_ALONG_X) {
            setExpanded(true);
        }
    }

    @Deprecated
    public void startRotateAnimation() {
        if (mRotateType == ROTATE_ALONG_Z) {
            if (mIsExpanded) {
                startCollapseAnimation();
            } else {
                startExpandAnimation();
            }
        } else if (mRotateType == ROTATE_ALONG_X) {
            setExpanded(!mIsExpanded);
        }
    }
}
