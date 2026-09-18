package androidx.recyclerview.widget;

import android.graphics.Color;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import androidx.dynamicanimation.animation.FloatPropertyCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroupAdapter;

import com.coui.appcompat.R;
import com.coui.appcompat.animation.dynamicanimation.COUISpringAnimation;
import com.coui.appcompat.animation.dynamicanimation.COUISpringForce;
import com.coui.appcompat.contextutil.COUIContextUtil;

// Leapy added 2026-07-22: Exact Java reconstruction of OPPO Settings APK's
// androidx.recyclerview.widget.COUIRecyclerDividerManager smali.
class COUIRecyclerDividerManager {
    public static float SPRING_BOUNCE = 0.0f;
    public static float SPRING_RESPONSE = 0.15f;

    private FloatPropertyCompat<COUIRecyclerDividerManager> mAlphaTransition;
    private View mChildViewWhenTouchDown;
    private int mDividerAlpha;
    private boolean mEnablePressHideDivider;
    private int mInitialTouchY;
    private boolean mIsPress;
    private int mMaxAlpha;
    private final RecyclerView mRecyclerView;
    private COUISpringAnimation mSpringAnimation;
    private final int mTouchSlop;

    COUIRecyclerDividerManager(RecyclerView recyclerView, int touchSlop) {
        mRecyclerView = recyclerView;
        mTouchSlop = touchSlop;
        init();
    }

    private void ensureSpringAnimation() {
        if (mSpringAnimation != null) {
            return;
        }
        mSpringAnimation = new COUISpringAnimation(this, mAlphaTransition);
        COUISpringForce springForce = new COUISpringForce();
        springForce.setBounce(SPRING_BOUNCE);
        springForce.setResponse(SPRING_RESPONSE);
        mSpringAnimation.setSpring(springForce);
    }

    private void executeDividerHideAnim(MotionEvent event) {
        if (isExecuteAnim(event.getX(), event.getY())) {
            ensureSpringAnimation();
            mSpringAnimation.setStartValue(mDividerAlpha);
            mSpringAnimation.animateToFinalPosition(0.0f);
        }
    }

    private void executeDividerShowAnim(MotionEvent event) {
        mIsPress = false;
        if (mChildViewWhenTouchDown != null) {
            ensureSpringAnimation();
            mSpringAnimation.setStartValue(mDividerAlpha);
            mSpringAnimation.animateToFinalPosition(mMaxAlpha);
        }
    }

    private int getDividerAlpha() {
        return mDividerAlpha;
    }

    private void init() {
        setEnablePressHideDivider(true);
    }

    private boolean isExecuteAnim(float x, float y) {
        mChildViewWhenTouchDown = mRecyclerView.findChildViewUnder(x, y);
        if (mChildViewWhenTouchDown == null
                || !(mRecyclerView.getAdapter() instanceof PreferenceGroupAdapter)) {
            KeyEvent.Callback callback = mChildViewWhenTouchDown;
            if (callback instanceof ICOUIBaseListItemView) {
                return ((ICOUIBaseListItemView) callback).getItemEnabled();
            }
            return true;
        }
        Preference item = ((PreferenceGroupAdapter) mRecyclerView.getAdapter()).getItem(
                mRecyclerView.getChildAdapterPosition(mChildViewWhenTouchDown));
        return item != null && item.isEnabled();
    }

    private void setDividerAlpha(float alpha) {
        mDividerAlpha = (int) alpha;
        setPressDividerAlpha();
    }

    private void setPressDividerAlpha() {
        if (mChildViewWhenTouchDown == null) {
            return;
        }
        for (int i = 0; i < mRecyclerView.getItemDecorationCount(); i++) {
            RecyclerView.ItemDecoration decoration = mRecyclerView.getItemDecorationAt(i);
            if (decoration instanceof COUIRecyclerView.COUIDividerItemDecoration) {
                COUIRecyclerView.COUIDividerItemDecoration divider =
                        (COUIRecyclerView.COUIDividerItemDecoration) decoration;
                divider.setPressDividerPos(mRecyclerView.indexOfChild(mChildViewWhenTouchDown));
                divider.setPressDividerAlpha(mDividerAlpha);
                mRecyclerView.invalidate();
            }
        }
    }

    public void dispatchTouchEvent(MotionEvent event) {
        if (!mEnablePressHideDivider) {
            return;
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mIsPress = true;
            mInitialTouchY = (int) (event.getY() + 0.5f);
            executeDividerHideAnim(event);
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            executeDividerShowAnim(event);
        } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
            executeDividerShowAnim(event);
        } else if (event.getAction() == MotionEvent.ACTION_MOVE
                && Math.abs(((int) (event.getY() + 0.5f)) - mInitialTouchY) > mTouchSlop
                && mIsPress) {
            executeDividerShowAnim(event);
        }
    }

    public boolean isDrawDivider(View view, int position) {
        if (mEnablePressHideDivider) {
            View next = mRecyclerView.getChildAt(position + 1);
            if (next instanceof ICOUIDividerDecoration
                    && !((ICOUIDividerDecoration) next).drawDivider()) {
                return false;
            }
            if (view instanceof ICOUIDividerDecoration
                    && !((ICOUIDividerDecoration) view).drawDivider()) {
                return false;
            }
        }
        return true;
    }

    public void onInterceptTouchEvent(MotionEvent event) {
        if (mIsPress) {
            executeDividerShowAnim(event);
        }
    }

    public void setEnablePressHideDivider(boolean enabled) {
        mEnablePressHideDivider = enabled;
        if (enabled) {
            int alpha = Color.alpha(COUIContextUtil.getAttrColor(
                    mRecyclerView.getContext(), R.attr.couiColorDivider));
            mMaxAlpha = alpha;
            mDividerAlpha = alpha;
            if (mAlphaTransition == null) {
                mAlphaTransition = new FloatPropertyCompat<COUIRecyclerDividerManager>(
                        "dividerAlpha") {
                    @Override
                    public float getValue(COUIRecyclerDividerManager manager) {
                        return manager.getDividerAlpha();
                    }

                    @Override
                    public void setValue(COUIRecyclerDividerManager manager, float value) {
                        manager.setDividerAlpha(value);
                    }
                };
            }
            ensureSpringAnimation();
        }
    }
}
