package com.coui.appcompat.preference;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.res.TypedArrayUtils;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceViewHolder;

import com.coui.appcompat.R;
import com.coui.appcompat.contextutil.COUIContextUtil;
import com.coui.appcompat.reddot.COUIHintRedDot;
import com.coui.appcompat.rippleutil.COUIRippleDrawableUtil;
import com.coui.appcompat.state.COUIMaskRippleDrawable;
import com.coui.appcompat.textviewcompatutil.COUITextViewCompatUtil;

public class COUIPreferenceCategory extends PreferenceCategory {
    public static final int MARGIN_TYPE_LARGE = 0;
    public static final int MARGIN_TYPE_SMALL = 1;
    public static final int MARGIN_TYPE_ZERO = 2;
    public static final int TITLE_MARGIN_START_TYPE_LARGE = 1;
    public static final int TITLE_MARGIN_START_TYPE_SMALL = 0;
    public static final int TITLE_TYPE_LARGE = 2;
    public static final int TITLE_TYPE_MEDIUM = 1;
    public static final int TITLE_TYPE_SMALL = 0;

    private static final String TAG = "COUIPreferenceCategory";
    private static final float TITLE_LARGE_TEXTSIZE = 16.0f;
    private static final float TITLE_MEDIUM_TEXTSIZE = 14.0f;
    private static final float TITLE_SMALL_TEXTSIZE = 12.0f;

    private final Context mContext;
    private IGetWidgetLayoutListener mGetWidgetLayoutListener;
    private int mHorizontalMarginType = TITLE_MARGIN_START_TYPE_LARGE;
    private int mIconInRight;
    private int mIconWithTitle;
    private View.OnClickListener mItemviewClickListener;
    private final int mLayoutTitleMarginEndLarge;
    private final int mLayoutTitleMarginStartLarge;
    private final int mLayoutTitleMarginStartSmall;
    private final ArrayMap<Integer, Integer> mMarginEndMap;
    private int mMarginTopType = MARGIN_TYPE_LARGE;
    private COUIMaskRippleDrawable mMaskRippleDrawable;
    private String mRightIconContentDescription;
    private String mTextInReddot;
    private String mTextInRight;
    private View.OnClickListener mTitleIconClickListener;
    private String mTitleIconContentDescription;
    private boolean mTitleOnly;
    private TextView mTitleTextView;
    private int mTitleType = TITLE_TYPE_SMALL;
    private View mWidgetLayout;
    private View.OnClickListener mWidgetLayoutClickListener;
    private int mWidgetLayoutRes;

    public interface IGetWidgetLayoutListener {
        void widgetInflated(View view);
    }

    public COUIPreferenceCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        TypedArray category = context.obtainStyledAttributes(attrs, R.styleable.COUIPreferenceCategory, 0, 0);
        mTextInRight = category.getString(R.styleable.COUIPreferenceCategory_text_in_right);
        mIconInRight = category.getResourceId(R.styleable.COUIPreferenceCategory_icon_in_right, 0);
        mIconWithTitle = category.getResourceId(R.styleable.COUIPreferenceCategory_icon_with_title, 0);
        mTextInReddot = category.getString(R.styleable.COUIPreferenceCategory_text_in_reddot);
        mHorizontalMarginType = category.getInteger(
                R.styleable.COUIPreferenceCategory_title_margin_start_type,
                mHorizontalMarginType
        );
        mTitleType = category.getInteger(R.styleable.COUIPreferenceCategory_title_type, mTitleType);
        mMarginTopType = category.getInteger(R.styleable.COUIPreferenceCategory_top_margin_type, mMarginTopType);
        mTitleIconContentDescription = category.getString(
                R.styleable.COUIPreferenceCategory_icon_with_title_content_description
        );
        mRightIconContentDescription = category.getString(
                R.styleable.COUIPreferenceCategory_icon_in_right_content_description
        );
        category.recycle();

        TypedArray preference = context.obtainStyledAttributes(attrs, androidx.preference.R.styleable.Preference, 0, 0);
        mWidgetLayoutRes = TypedArrayUtils.getResourceId(
                preference,
                androidx.preference.R.styleable.Preference_widgetLayout,
                androidx.preference.R.styleable.Preference_android_widgetLayout,
                0
        );
        preference.recycle();

        Resources resources = context.getResources();
        mLayoutTitleMarginStartLarge = resources.getDimensionPixelSize(
                R.dimen.support_preference_category_layout_title_margin_start_large
        );
        mLayoutTitleMarginStartSmall = resources.getDimensionPixelSize(
                R.dimen.support_preference_category_layout_title_margin_start_small
        );
        mLayoutTitleMarginEndLarge = resources.getDimensionPixelSize(
                R.dimen.support_preference_category_layout_title_margin_end_large
        );
        mMarginEndMap = new ArrayMap<>();
        mMarginEndMap.put(R.layout.coui_preference_category_widget_layout_checkbox, 0);
        mMarginEndMap.put(R.layout.coui_preference_category_widget_layout_loading, 0);
        mMarginEndMap.put(
                R.layout.coui_preference_category_widget_layout_singleicon,
                resources.getDimensionPixelSize(R.dimen.coui_preference_category_Loading_marginend)
        );
        mMarginEndMap.put(
                R.layout.coui_preference_category_widget_layout_textbutton,
                resources.getDimensionPixelSize(R.dimen.coui_preference_category_textbutton_marginend)
        );
        mMarginEndMap.put(R.layout.coui_preference_category_widget_layout_textwithicon, 0);
        ensureMaskRippleDrawable();
    }

    private void ensureMaskRippleDrawable() {
        if (mMaskRippleDrawable == null) {
            mMaskRippleDrawable = new COUIMaskRippleDrawable(getContext());
            mMaskRippleDrawable.setCircleRippleMask(
                    getContext().getResources().getDimensionPixelSize(
                            R.dimen.coui_preference_widget_layout_single_icon_radius
                    )
            );
        }
    }

    private void initEndRect(PreferenceViewHolder holder) {
        TextView endTextView;
        TextView titleTextView;
        View widgetFrame = holder.findViewById(android.R.id.widget_frame);
        if (mWidgetLayoutRes != 0) {
            if (!(widgetFrame instanceof LinearLayout)) {
                return;
            }
            LinearLayout linearLayout = (LinearLayout) widgetFrame;
            if (linearLayout.getChildCount() > 0) {
                linearLayout.removeAllViews();
            }
            mWidgetLayout = LayoutInflater.from(mContext).inflate(mWidgetLayoutRes, linearLayout, false);
            if (mWidgetLayout == null) {
                Log.e(TAG, "inflate mWidgetLayoutRes failed");
                return;
            }
            mTitleOnly = false;
            if (mWidgetLayout.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams marginParams =
                        (ViewGroup.MarginLayoutParams) mWidgetLayout.getLayoutParams();
                int baseMargin = mMarginEndMap.get(mWidgetLayoutRes);
                int targetMargin = mHorizontalMarginType == TITLE_MARGIN_START_TYPE_SMALL
                        ? baseMargin
                        : baseMargin + mLayoutTitleMarginEndLarge;
                if (marginParams.getMarginEnd() != targetMargin) {
                    marginParams.setMarginEnd(targetMargin);
                    mWidgetLayout.setLayoutParams(marginParams);
                }
            }
            if (mGetWidgetLayoutListener != null) {
                mWidgetLayout.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                    @Override
                    public void onViewAttachedToWindow(View view) {
                        COUIPreferenceCategory.this.mWidgetLayout.post(() ->
                                COUIPreferenceCategory.this.mGetWidgetLayoutListener.widgetInflated(
                                        COUIPreferenceCategory.this.mWidgetLayout
                                )
                        );
                    }

                    @Override
                    public void onViewDetachedFromWindow(View view) {
                        view.removeOnAttachStateChangeListener(this);
                    }
                });
            }
            if (!(mWidgetLayout.getLayoutParams() instanceof ViewGroup.MarginLayoutParams)) {
                return;
            }
            ViewGroup.MarginLayoutParams marginParams =
                    (ViewGroup.MarginLayoutParams) mWidgetLayout.getLayoutParams();
            LinearLayout.LayoutParams layoutParams =
                    new LinearLayout.LayoutParams(marginParams.width, marginParams.height);
            layoutParams.gravity = 16;
            layoutParams.setMarginStart(marginParams.getMarginStart());
            layoutParams.topMargin = marginParams.topMargin;
            layoutParams.setMarginEnd(marginParams.getMarginEnd());
            layoutParams.bottomMargin = marginParams.bottomMargin;
            linearLayout.addView(mWidgetLayout, layoutParams);
            linearLayout.setVisibility(View.VISIBLE);

            if (mWidgetLayoutClickListener != null) {
                if (mWidgetLayoutRes == R.layout.coui_preference_category_widget_layout_singleicon) {
                    ensureMaskRippleDrawable();
                    linearLayout.getChildAt(0).setBackground(mMaskRippleDrawable);
                } else {
                    COUITextViewCompatUtil.setPressRippleDrawable(mWidgetLayout, false);
                }
                mWidgetLayout.setOnClickListener(mWidgetLayoutClickListener);
            } else if (mItemviewClickListener != null) {
                holder.itemView.setOnClickListener(mItemviewClickListener);
                COUIRippleDrawableUtil.setPressRippleDrawable(holder.itemView, 0, false);
            }

            int widgetLayout = mWidgetLayoutRes;
            if (widgetLayout == R.layout.coui_preference_category_widget_layout_textwithicon) {
                endTextView = linearLayout.findViewById(R.id.text_in_composition);
                if (endTextView != null && !TextUtils.isEmpty(mTextInRight)) {
                    endTextView.setText(mTextInRight);
                    endTextView.setVisibility(View.VISIBLE);
                }
                if (mWidgetLayoutClickListener == null || rightTextfixSecondaryColor()) {
                    endTextView.setTextColor(COUIContextUtil.getAttrColor(
                            getContext(), R.attr.couiColorSecondNeutral, 0
                    ));
                } else {
                    endTextView.setTextColor(COUIContextUtil.getAttrColor(
                            getContext(), R.attr.couiColorPrimaryNeutral, 0
                    ));
                }
                ImageView imageView = linearLayout.findViewById(R.id.icon_in_composition);
                if (imageView != null && mIconInRight != 0) {
                    imageView.setImageResource(mIconInRight);
                    imageView.setVisibility(View.VISIBLE);
                }
            } else if (widgetLayout == R.layout.coui_preference_category_widget_layout_textbutton) {
                endTextView = linearLayout.findViewById(R.id.text_button);
                if (endTextView != null && !TextUtils.isEmpty(mTextInRight)) {
                    endTextView.setText(mTextInRight);
                    endTextView.setVisibility(View.VISIBLE);
                    COUITextViewCompatUtil.setPressRippleDrawable(endTextView);
                }
            } else {
                if (widgetLayout == R.layout.coui_preference_category_widget_layout_singleicon) {
                    ImageView imageView = linearLayout.findViewById(R.id.singleIcon);
                    if (imageView != null && mIconInRight != 0) {
                        imageView.setImageResource(mIconInRight);
                        imageView.setVisibility(View.VISIBLE);
                        if (!TextUtils.isEmpty(mRightIconContentDescription)) {
                            imageView.setContentDescription(mRightIconContentDescription);
                        }
                    }
                } else if (widgetLayout == R.layout.coui_preference_category_widget_layout_loading) {
                    endTextView = mWidgetLayout.findViewById(R.id.text_in_loading);
                }
                endTextView = null;
            }
            if (endTextView != null) {
                applyEndTextSize(linearLayout, endTextView);
            }
        } else if (widgetFrame != null) {
            widgetFrame.setVisibility(View.GONE);
        }

        if (mTitleOnly
                && (titleTextView = mTitleTextView) != null
                && titleTextView.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams marginParams =
                    (ViewGroup.MarginLayoutParams) mTitleTextView.getLayoutParams();
            marginParams.setMarginEnd(marginParams.getMarginStart());
            mTitleTextView.setLayoutParams(marginParams);
        }
    }

    private void applyEndTextSize(LinearLayout linearLayout, TextView textView) {
        if (mTitleType == TITLE_TYPE_SMALL) {
            textView.setTextSize(2, TITLE_SMALL_TEXTSIZE);
        } else {
            textView.setTextSize(2, TITLE_MEDIUM_TEXTSIZE);
            linearLayout.getChildAt(0).setMinimumHeight(
                    getContext().getResources().getDimensionPixelSize(
                            R.dimen.coui_preference_widget_layout_min_height_when_title_isnot_small
                    )
            );
        }
    }

    private void initStartRect(PreferenceViewHolder holder) {
        ImageView imageView;
        boolean hasTitleIcon;
        mTitleOnly = true;
        View titleView = holder.findViewById(android.R.id.title);
        if (titleView instanceof TextView) {
            mTitleTextView = (TextView) titleView;
        }
        View titleIcon = holder.findViewById(R.id.icon_with_title);
        if (titleIcon instanceof ImageView) {
            imageView = (ImageView) titleIcon;
            if (mTitleIconClickListener != null) {
                imageView.setOnClickListener(mTitleIconClickListener);
                ensureMaskRippleDrawable();
                imageView.setBackground(mMaskRippleDrawable);
            }
            if (!TextUtils.isEmpty(mTitleIconContentDescription)) {
                imageView.setContentDescription(mTitleIconContentDescription);
            }
        } else {
            imageView = null;
        }
        View redDotView = holder.findViewById(R.id.reddot_with_title);
        COUIHintRedDot redDot = redDotView instanceof COUIHintRedDot ? (COUIHintRedDot) redDotView : null;
        if (imageView == null) {
            hasTitleIcon = false;
        } else if (mIconWithTitle != 0) {
            imageView.setImageResource(mIconWithTitle);
            imageView.setVisibility(View.VISIBLE);
            mTitleOnly = false;
            if (titleView.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams marginParams =
                        (ViewGroup.MarginLayoutParams) titleView.getLayoutParams();
                marginParams.setMarginEnd(getContext().getResources().getDimensionPixelSize(
                        mTitleType == TITLE_TYPE_LARGE
                                ? R.dimen.coui_category_title_margin_end_with_icon_large
                                : R.dimen.coui_category_title_margin_end_with_icon_small
                ));
                titleView.setLayoutParams(marginParams);
            }
            hasTitleIcon = true;
        } else {
            imageView.setVisibility(View.GONE);
            hasTitleIcon = false;
        }
        if (redDot != null) {
            if (TextUtils.isEmpty(mTextInReddot) || hasTitleIcon) {
                redDot.setVisibility(View.GONE);
            } else {
                redDot.setPointMode(COUIHintRedDot.POINT_WITH_NUM_MODE);
                redDot.setPointText(mTextInReddot);
                redDot.setVisibility(View.VISIBLE);
                mTitleOnly = false;
                if (titleView.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                    ViewGroup.MarginLayoutParams marginParams =
                            (ViewGroup.MarginLayoutParams) titleView.getLayoutParams();
                    marginParams.setMarginEnd(getContext().getResources().getDimensionPixelSize(
                            R.dimen.coui_category_title_pading_end_with_reddot_default
                    ));
                    titleView.setLayoutParams(marginParams);
                }
            }
        }
        if (mTitleTextView != null && mTitleTextView.getVisibility() == View.VISIBLE) {
            int titleType = mTitleType;
            if (titleType == TITLE_TYPE_SMALL) {
                mTitleTextView.setTextSize(2, TITLE_SMALL_TEXTSIZE);
                mTitleTextView.setMinHeight(getContext().getResources().getDimensionPixelSize(
                        R.dimen.coui_preference_category_text_height
                ));
                mTitleTextView.setTextColor(COUIContextUtil.getAttrColor(
                        getContext(),
                        hasTitleIcon ? R.attr.couiColorPrimaryNeutral : R.attr.couiColorSecondNeutral
                ));
            } else if (titleType == TITLE_TYPE_LARGE) {
                mTitleTextView.setTextSize(2, TITLE_LARGE_TEXTSIZE);
                mTitleTextView.setMinHeight(getContext().getResources().getDimensionPixelSize(
                        R.dimen.coui_preference_category_text_height_large
                ));
                mTitleTextView.setTextColor(COUIContextUtil.getAttrColor(getContext(), R.attr.couiColorPrimaryNeutral));
            } else {
                mTitleTextView.setTextSize(2, TITLE_MEDIUM_TEXTSIZE);
                mTitleTextView.setMinHeight(getContext().getResources().getDimensionPixelSize(
                        R.dimen.coui_preference_category_text_height_medium
                ));
                mTitleTextView.setTextColor(COUIContextUtil.getAttrColor(getContext(), R.attr.couiColorPrimaryNeutral));
            }
            if (mTitleTextView.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams marginParams =
                        (ViewGroup.MarginLayoutParams) mTitleTextView.getLayoutParams();
                if (mHorizontalMarginType == TITLE_MARGIN_START_TYPE_SMALL) {
                    if (marginParams.getMarginStart() != mLayoutTitleMarginStartSmall) {
                        marginParams.setMarginStart(mLayoutTitleMarginStartSmall);
                    }
                } else {
                    mTitleTextView.setLayoutParams(marginParams);
                    if (marginParams.getMarginStart() != mLayoutTitleMarginStartLarge) {
                        marginParams.setMarginStart(mLayoutTitleMarginStartLarge);
                    }
                }
                mTitleTextView.setLayoutParams(marginParams);
                View itemView = holder.itemView;
                itemView.setPadding(itemView.getPaddingStart(), 0, itemView.getPaddingEnd(), 0);
                if (mTitleType == TITLE_TYPE_SMALL) {
                    int margin = getContext().getResources().getDimensionPixelSize(
                            R.dimen.support_preference_category_layout_title_margin_end_new
                    );
                    marginParams.topMargin = margin;
                    marginParams.bottomMargin = margin;
                } else if (mTitleType == TITLE_TYPE_LARGE) {
                    marginParams.topMargin = getContext().getResources().getDimensionPixelSize(
                            R.dimen.coui_common_category_text_padding_top_Large_style
                    );
                    marginParams.bottomMargin = getContext().getResources().getDimensionPixelSize(
                            R.dimen.coui_common_category_text_padding_bottom_large_style
                    );
                } else {
                    marginParams.topMargin = getContext().getResources().getDimensionPixelSize(
                            R.dimen.coui_common_category_text_padding_top_medium_style
                    );
                    marginParams.bottomMargin = getContext().getResources().getDimensionPixelSize(
                            R.dimen.coui_common_category_text_padding_bottom_medium_style
                    );
                }
                mTitleTextView.setLayoutParams(marginParams);
            }
        }
        if (holder.itemView.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams marginParams =
                    (ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams();
            if (mMarginTopType == MARGIN_TYPE_LARGE) {
                marginParams.topMargin = getContext().getResources().getDimensionPixelSize(
                        R.dimen.coui_preference_category_margintop_large
                );
            } else if (mMarginTopType == MARGIN_TYPE_SMALL) {
                marginParams.topMargin = getContext().getResources().getDimensionPixelSize(
                        R.dimen.coui_preference_category_margintop_small
                );
            } else if (mMarginTopType == MARGIN_TYPE_ZERO) {
                marginParams.topMargin = getContext().getResources().getDimensionPixelSize(
                        R.dimen.coui_preference_category_margintop_zero
                );
            }
        }
    }

    public View getWidgetLayout() {
        return mWidgetLayout;
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        initStartRect(holder);
        initEndRect(holder);
    }

    public boolean rightTextfixSecondaryColor() {
        return false;
    }

    public void setHorizontalMarginType(int type) {
        if (mHorizontalMarginType != type) {
            mHorizontalMarginType = type;
            notifyChanged();
        }
    }

    public void setIconInRight(int resId) {
        if (mIconInRight != resId) {
            mIconInRight = resId;
            notifyChanged();
        }
    }

    public void setIconWithTitle(int resId) {
        mIconWithTitle = resId;
    }

    public void setItemViewLayoutClickListener(View.OnClickListener listener) {
        mItemviewClickListener = listener;
    }

    public void setMarginTopType(int type) {
        if (mMarginTopType != type) {
            mMarginTopType = type;
            notifyChanged();
        }
    }

    public void setRightIconContentDescription(String description) {
        mRightIconContentDescription = description;
        notifyChanged();
    }

    public void setTextInReddot(String text) {
        mTextInReddot = text;
    }

    public void setTextInRight(String text) {
        if (!TextUtils.equals(text, mTextInRight)) {
            mTextInRight = text;
            notifyChanged();
        }
    }

    public void setTitleIconClickListener(View.OnClickListener listener) {
        mTitleIconClickListener = listener;
    }

    public void setTitleIconContentDescription(String description) {
        mTitleIconContentDescription = description;
        notifyChanged();
    }

    public void setTitleType(int type) {
        if (mTitleType != type) {
            mTitleType = type;
            notifyChanged();
        }
    }

    public void setWidgetLayoutClickListener(View.OnClickListener listener) {
        mWidgetLayoutClickListener = listener;
    }

    public void setWidgetLayoutRes(int resId) {
        mWidgetLayoutRes = resId;
    }

    public void getWidgetLayout(IGetWidgetLayoutListener listener) {
        if (mWidgetLayout != null) {
            listener.widgetInflated(mWidgetLayout);
        } else {
            mGetWidgetLayoutListener = listener;
        }
    }
}
