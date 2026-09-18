package com.coui.appcompat.preference;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import androidx.core.view.ViewCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.COUIRecyclerView;
import androidx.recyclerview.widget.RecyclerView;

import com.coui.appcompat.R;
import com.coui.appcompat.darkmode.COUIDarkModeUtil;
import com.coui.appcompat.toolbar.COUIToolbar;
import com.google.android.material.appbar.AppBarLayout;

public abstract class COUIPreferenceWithAppbarFragment extends COUIPreferenceFragment {
    private int mDividerHeight;
    private RecyclerView mRecyclerView = null;
    private COUIToolbar mToolbar = null;
    private boolean mContainerFitsSystemWindows = false;

    private int getStatusBarHeight(Context context) {
        int identifier = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (identifier > 0) {
            return context.getResources().getDimensionPixelSize(identifier);
        }
        return 0;
    }

    private View getStatusBarView() {
        ImageView imageView = new ImageView(getActivity());
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                getStatusBarHeight(imageView.getContext())
        ));
        return imageView;
    }

    public abstract String getTitle();

    public COUIToolbar getToolbar() {
        return mToolbar;
    }

    public boolean isCustomWindowBackground() {
        return false;
    }

    public boolean needStatusBarViewHolder() {
        return !mContainerFitsSystemWindows;
    }

    @Override
    public RecyclerView onCreateRecyclerView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        COUIRecyclerView recyclerView = (COUIRecyclerView) inflater.inflate(
                R.layout.coui_preference_percent_recyclerview,
                parent,
                false
        );
        recyclerView.setEnablePointerDownAction(false);
        recyclerView.setLayoutManager(onCreateLayoutManager());
        COUIDarkModeUtil.setForceDarkAllow(recyclerView, false);
        return recyclerView;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container != null) {
            mContainerFitsSystemWindows = container.getFitsSystemWindows();
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        COUIToolbar toolbar = view.findViewById(R.id.toolbar);
        mToolbar = toolbar;
        if (toolbar == null) {
            return;
        }
        toolbar.setNavigationIcon(R.drawable.coui_back_arrow);
        mToolbar.setNavigationOnClickListener(v -> {
            FragmentActivity activity = getActivity();
            if (activity != null) {
                activity.finish();
            }
        });
        mToolbar.setTitle(getTitle());
        mDividerHeight = getResources().getDimensionPixelSize(R.dimen.toolbar_divider_height);
        final AppBarLayout appBarLayout = view.findViewById(R.id.appbar_layout);
        if (appBarLayout != null) {
            if (needStatusBarViewHolder()) {
                View statusBarView = getStatusBarView();
                appBarLayout.addView(statusBarView, 0, statusBarView.getLayoutParams());
            }
            appBarLayout.setTouchscreenBlocksFocus(false);
        }
        RecyclerView listView = getListView();
        mRecyclerView = listView;
        if (listView != null) {
            ViewCompat.setNestedScrollingEnabled(listView, true);
            mRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    RecyclerView.LayoutManager layoutManager = mRecyclerView.getLayoutManager();
                    View firstItem = layoutManager == null ? null : layoutManager.findViewByPosition(0);
                    if (firstItem != null) {
                        int measuredHeight = appBarLayout.getMeasuredHeight() - mDividerHeight;
                        if (measuredHeight > 0) {
                            RecyclerView.LayoutParams layoutParams =
                                    (RecyclerView.LayoutParams) firstItem.getLayoutParams();
                            ((ViewGroup.MarginLayoutParams) layoutParams).height = measuredHeight;
                            firstItem.setLayoutParams(layoutParams);
                        }
                        mRecyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
            });
        }
        if (getActivity() != null && !isCustomWindowBackground()) {
            getActivity().getWindow().setBackgroundDrawableResource(
                    R.drawable.coui_window_background_with_card_selector
            );
        }
    }
}
