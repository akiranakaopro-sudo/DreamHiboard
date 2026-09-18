package com.coui.appcompat.preference;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.view.ViewCompat;
import androidx.preference.ListPreferenceDialogFragmentCompat;

import com.coui.appcompat.R;
import com.coui.appcompat.cardlist.COUICardListHelper;
import com.coui.appcompat.darkmode.COUIDarkModeUtil;
import com.coui.appcompat.toolbar.COUIToolbar;
import com.coui.appcompat.version.COUIVersionUtil;
import com.google.android.material.appbar.AppBarLayout;

public class COUIActivityDialogFragment extends ListPreferenceDialogFragmentCompat {
    private static final int SYSTEM_UI_FLAG_FULLSCREEN = 1024;
    private static final int SYSTEM_UI_FLAG_LIGHT_STATUS_BAR = 8192;
    private static final int SYSTEM_UI_FLAG_LAYOUT_STABLE = 256;

    private int mClickedDialogEntryIndex;
    private AppCompatDialog mDialog;

    public static class CheckedItemAdapter extends ArrayAdapter<CharSequence> {
        public CheckedItemAdapter(Context context, int resource, int textViewResourceId, CharSequence[] objects) {
            super(context, resource, textViewResourceId, objects);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }

    private COUIActivityDialogPreference getListPreference() {
        return (COUIActivityDialogPreference) getPreference();
    }

    public static int getStatusBarHeight(Context context) {
        int identifier = context.getApplicationContext().getResources()
                .getIdentifier("status_bar_height", "dimen", "android");
        if (identifier > 0) {
            try {
                return context.getApplicationContext().getResources().getDimensionPixelSize(identifier);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    private View getStatusBarView(Context context) {
        int statusBarHeight = getStatusBarHeight(context);
        ImageView imageView = new ImageView(context);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                statusBarHeight
        ));
        return imageView;
    }

    public static COUIActivityDialogFragment newInstance(String key) {
        COUIActivityDialogFragment fragment = new COUIActivityDialogFragment();
        Bundle args = new Bundle(1);
        args.putString(ARG_KEY, key);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AppCompatDialog dialog = new AppCompatDialog(requireActivity(), R.style.Theme_COUI_ActivityDialog) {
            @Override
            public boolean onMenuItemSelected(int featureId, MenuItem item) {
                if (item.getItemId() != android.R.id.home) {
                    return super.onMenuItemSelected(featureId, item);
                }
                dismiss();
                return true;
            }
        };
        mDialog = dialog;
        if (dialog.getWindow() != null) {
            Window window = dialog.getWindow();
            View decorView = window.getDecorView();
            decorView.setSystemUiVisibility(SYSTEM_UI_FLAG_FULLSCREEN);
            window.setStatusBarColor(0);
            int systemUiVisibility = decorView.getSystemUiVisibility();
            int osVersionCode = COUIVersionUtil.getOSVersionCode();
            boolean whiteStatus = getResources().getBoolean(R.bool.list_status_white_enabled);
            if (osVersionCode >= COUIVersionUtil.COUI_3_0 || osVersionCode == COUIVersionUtil.UNKNOWN) {
                window.addFlags(WindowManagerFlags.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                decorView.setSystemUiVisibility(COUIDarkModeUtil.isNightMode(dialog.getContext())
                        ? systemUiVisibility & ~SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                        : !whiteStatus
                        ? systemUiVisibility | SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                        : systemUiVisibility | SYSTEM_UI_FLAG_LAYOUT_STABLE);
            }
        }

        View contentView = LayoutInflater.from(getActivity())
                .inflate(R.layout.coui_preference_listview, null);
        COUIToolbar toolbar = contentView.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(AppCompatResources.getDrawable(toolbar.getContext(), R.drawable.coui_back_arrow));
        toolbar.setNavigationOnClickListener(view -> mDialog.dismiss());

        final AppBarLayout appBarLayout = contentView.findViewById(R.id.abl);
        final ListView listView = contentView.findViewById(R.id.coui_preference_listview);
        View divider = contentView.findViewById(R.id.divider_line);
        if (getResources().getBoolean(R.bool.is_dialog_preference_immersive)) {
            divider.setVisibility(View.GONE);
        }
        ViewCompat.setNestedScrollingEnabled(listView, true);
        View statusBarView = getStatusBarView(appBarLayout.getContext());
        appBarLayout.addView(statusBarView, 0, statusBarView.getLayoutParams());
        appBarLayout.post(() -> {
            if (isAdded()) {
                int measuredHeight = appBarLayout.getMeasuredHeight()
                        + getResources().getDimensionPixelSize(R.dimen.support_preference_listview_margin_top);
                View header = new View(appBarLayout.getContext());
                header.setVisibility(View.INVISIBLE);
                header.setLayoutParams(new AbsListView.LayoutParams(
                        AbsListView.LayoutParams.MATCH_PARENT,
                        measuredHeight
                ));
                listView.addHeaderView(header);
            }
        });

        if (getListPreference() != null) {
            mClickedDialogEntryIndex = getListPreference().findIndexOfValue(getListPreference().getValue());
            toolbar.setTitle(getListPreference().getDialogTitle());
            listView.setAdapter((ListAdapter) new CheckedItemAdapter(
                    getActivity(),
                    R.layout.coui_preference_listview_item,
                    R.id.checkedtextview,
                    getListPreference().getEntries()
            ) {
                @Override
                public View getView(final int position, View convertView, ViewGroup parent) {
                    View itemView = super.getView(position, convertView, parent);
                    if (position == mClickedDialogEntryIndex) {
                        listView.setItemChecked(listView.getHeaderViewsCount() + position, true);
                    }
                    View itemDivider = itemView.findViewById(R.id.item_divider);
                    int count = getCount();
                    if (itemDivider != null) {
                        itemDivider.setVisibility(count == 1 || position == count - 1
                                ? View.GONE
                                : View.VISIBLE);
                    }
                    itemView.setOnClickListener(view -> {
                        mClickedDialogEntryIndex = position;
                        onClick(null, Dialog.BUTTON_POSITIVE);
                        dialog.dismiss();
                    });
                    COUICardListHelper.setItemCardBackground(
                            itemView,
                            COUICardListHelper.getPositionInGroup(getListPreference().getEntries().length, position)
                    );
                    return itemView;
                }
            });
        }
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        dialog.setContentView(contentView);
        return dialog;
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        COUIActivityDialogPreference preference = getListPreference();
        if (!positiveResult || mClickedDialogEntryIndex < 0) {
            return;
        }
        String value = getListPreference().getEntryValues()[mClickedDialogEntryIndex].toString();
        if (preference.callChangeListener(value)) {
            preference.setValue(value);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getPreference() == null) {
            dismiss();
        }
    }

    private static final class WindowManagerFlags {
        private static final int FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS = 0x80000000;
    }
}
