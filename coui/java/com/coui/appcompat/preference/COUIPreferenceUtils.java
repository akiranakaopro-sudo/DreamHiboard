package com.coui.appcompat.preference;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.preference.PreferenceViewHolder;

import com.coui.appcompat.R;
import com.coui.appcompat.imageview.COUIRoundImageView;

public class COUIPreferenceUtils {
    static final int DEFALUT_RADIUS = 14;
    static final int DELAY_TIME = 70;
    public static final int ICON_SIZE_DP_LARGE = 50;
    public static final int ICON_SIZE_DP_MEDIUM = 32;
    public static final int ICON_SIZE_DP_MEDIUM_LARGE = 36;
    public static final int ICON_SIZE_DP_SMALL = 24;

    public static void bindAssignmentView(PreferenceViewHolder holder, CharSequence assignment, int color) {
        TextView assignmentView = (TextView) holder.findViewById(R.id.assignment);
        if (assignmentView != null) {
            if (TextUtils.isEmpty(assignment)) {
                assignmentView.setVisibility(View.GONE);
                return;
            }
            assignmentView.setText(assignment);
            assignmentView.setVisibility(View.VISIBLE);
            if (color != 0) {
                assignmentView.setTextColor(color);
            }
        }
    }

    public static void bindView(PreferenceViewHolder holder, Drawable jump, CharSequence statusText, CharSequence assignment) {
        bindView(holder, jump, statusText, assignment, 0);
    }

    public static void bindView(PreferenceViewHolder holder, Drawable jump, CharSequence statusText, CharSequence assignment, int color) {
        ImageView jumpView = (ImageView) holder.findViewById(R.id.coui_preference_widget_jump);
        if (jumpView != null) {
            if (jump != null) {
                jumpView.setImageDrawable(jump);
                jumpView.setVisibility(View.VISIBLE);
            } else {
                jumpView.setVisibility(View.GONE);
            }
        }
        View icon = holder.findViewById(android.R.id.icon);
        View iconLayout = holder.findViewById(R.id.img_layout);
        if (iconLayout != null) {
            iconLayout.setVisibility(icon != null ? icon.getVisibility() : View.GONE);
        }
        TextView status = (TextView) holder.findViewById(R.id.coui_statusText1);
        if (status != null) {
            if (TextUtils.isEmpty(statusText)) {
                status.setVisibility(View.GONE);
            } else {
                status.setText(statusText);
                status.setVisibility(View.VISIBLE);
            }
        }
        bindAssignmentView(holder, assignment, color);
    }

    public static void setIconStyle(PreferenceViewHolder holder, Context context, int radius, boolean hasBorder, int iconStyle, boolean custom) {
        View icon = holder.findViewById(android.R.id.icon);
        if (!(icon instanceof COUIRoundImageView)) {
            return;
        }
        COUIRoundImageView roundImageView = (COUIRoundImageView) icon;
        if (custom) {
            roundImageView.setHasBorder(hasBorder);
            roundImageView.setBorderRectRadius(0);
            roundImageView.setType(iconStyle);
            return;
        }
        Drawable drawable = roundImageView.getDrawable();
        if (drawable != null && radius == DEFALUT_RADIUS) {
            radius = drawable.getIntrinsicHeight() / 6;
            Resources resources = context.getResources();
            int minRadius = resources.getDimensionPixelOffset(R.dimen.coui_preference_icon_min_radius);
            int maxRadius = resources.getDimensionPixelOffset(R.dimen.coui_preference_icon_max_radius);
            if (radius < minRadius) {
                radius = minRadius;
            } else if (radius > maxRadius) {
                radius = maxRadius;
            }
        }
        roundImageView.setHasBorder(hasBorder);
        roundImageView.setBorderRectRadius(radius);
        roundImageView.setType(iconStyle);
    }

    public static void setSummaryView(Context context, PreferenceViewHolder holder) {
        final TextView summary = (TextView) holder.findViewById(android.R.id.summary);
        if (summary != null) {
            summary.setHighlightColor(context.getResources().getColor(android.R.color.transparent, context.getTheme()));
            summary.setMovementMethod(LinkMovementMethod.getInstance());
            summary.setOnTouchListener((view, event) -> {
                int actionMasked = event.getActionMasked();
                int selectionStart = summary.getSelectionStart();
                int selectionEnd = summary.getSelectionEnd();
                int offset = summary.getOffsetForPosition(event.getX(), event.getY());
                boolean outsideSelection = selectionStart == selectionEnd || offset <= selectionStart || offset >= selectionEnd;
                if (actionMasked == MotionEvent.ACTION_DOWN) {
                    if (outsideSelection) {
                        return false;
                    }
                    summary.setPressed(true);
                    summary.invalidate();
                } else if (actionMasked == MotionEvent.ACTION_UP || actionMasked == MotionEvent.ACTION_CANCEL) {
                    summary.setPressed(false);
                    summary.postInvalidateDelayed(DELAY_TIME);
                }
                return false;
            });
        }
    }

    public static void setSummaryViewColor(PreferenceViewHolder holder, ColorStateList color) {
        TextView summary = (TextView) holder.findViewById(android.R.id.summary);
        if (summary != null && color != null) {
            summary.setTextColor(color);
        }
    }

    public static void setTitleViewColor(Context context, PreferenceViewHolder holder, ColorStateList color) {
        View title = holder.findViewById(android.R.id.title);
        if (title != null && color != null) {
            ((TextView) title).setTextColor(color);
        }
    }
}
