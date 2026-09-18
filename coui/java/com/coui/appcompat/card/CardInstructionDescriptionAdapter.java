package com.coui.appcompat.card;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.coui.appcompat.R;
import com.airbnb.lottie.LottieAnimationView;

import java.util.ArrayList;
import java.util.List;

public final class CardInstructionDescriptionAdapter
        extends BaseCardInstructionAdapter<CardInstructionDescriptionAdapter.DescriptionHolder> {
    public static final Companion Companion = new Companion();

    public CardInstructionDescriptionAdapter() {
        this(new ArrayList<>());
    }

    public CardInstructionDescriptionAdapter(List<BaseDisplayInfo> displayInfos) {
        super(displayInfos);
    }

    @Override
    public DescriptionHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.coui_component_card_instruction_description_page, parent, false);
        return new DescriptionHolder(view, this);
    }

    public final class DescriptionHolder extends BaseCardInstructionAdapter.BaseHolder {
        private final LinearLayout animContainer;
        private final COUIMutableSizeScrollView scrollViewContainer;
        private final TextView summary;
        private final LinearLayout summaryContainer;
        private final TextView title;

        public DescriptionHolder(View itemView, BaseCardInstructionAdapter<?> adapter) {
            super(itemView, adapter);
            animContainer = itemView.findViewById(R.id.anim_container);
            title = itemView.findViewById(R.id.title);
            summary = itemView.findViewById(R.id.summary);
            summaryContainer = itemView.findViewById(R.id.summary_container);
            scrollViewContainer = itemView.findViewById(R.id.content_container);
        }

        @Override
        public void bind(BaseDisplayInfo displayInfo) {
            animContainer.removeAllViews();
            BaseCardInstructionAdapter.Companion.updateContentAndVisibility(title, displayInfo.getTitle());
            BaseCardInstructionAdapter.Companion.updateContentAndVisibility(
                    summary, displayInfo.getSummary(), summaryContainer);

            int summaryMarginRes;
            if (title.getVisibility() == View.VISIBLE) {
                scrollViewContainer.setMaxHeight(scrollViewContainer.getResources().getDimensionPixelSize(
                        R.dimen.coui_component_card_instruction_content_height_complete));
                summaryMarginRes = R.dimen.coui_component_card_instruction_summary_margin_top_small;
            } else {
                scrollViewContainer.setMaxHeight(scrollViewContainer.getResources().getDimensionPixelSize(
                        R.dimen.coui_component_card_instruction_content_height_part));
                summaryMarginRes = R.dimen.coui_component_card_instruction_summary_margin_top_large;
            }
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) summaryContainer.getLayoutParams();
            params.topMargin = CardInstructionDescriptionAdapterKt.getDimenPx(
                    summaryContainer, summaryMarginRes);
            summaryContainer.setLayoutParams(params);

            if (displayInfo instanceof AnimDisplayInfo) {
                bindAnimDisplayInfo((AnimDisplayInfo) displayInfo);
            } else if (displayInfo instanceof ImageDisplayInfo) {
                bindImageDisplayInfo((ImageDisplayInfo) displayInfo);
            }
        }

        private void bindAnimDisplayInfo(AnimDisplayInfo displayInfo) {
            int dividerWidth = animContainer.getContext().getResources().getDimensionPixelSize(
                    R.dimen.coui_component_card_instruction_divider_width);
            if (!displayInfo.getAnimAssets().isEmpty() && !displayInfo.getAnimResources().isEmpty()) {
                throw new IllegalArgumentException(
                        "imageAssets and imageResources cannot be used at the same time. Please use only one at once.");
            }
            if (!displayInfo.getAnimTitles().isEmpty()
                    && displayInfo.getAnimAssets().size() + displayInfo.getAnimResources().size()
                    != displayInfo.getAnimTitles().size()) {
                throw new IllegalArgumentException("the image count must equals to the animTitle count");
            }
            for (int i = 0; i < displayInfo.getAnimResources().size(); i++) {
                addAnimViewGroup(displayInfo, i, dividerWidth);
            }
            for (int i = 0; i < displayInfo.getAnimAssets().size(); i++) {
                addAnimViewGroup(displayInfo, i, dividerWidth);
            }
        }

        private void bindImageDisplayInfo(ImageDisplayInfo displayInfo) {
            if (!displayInfo.getAnimTitles().isEmpty()
                    && displayInfo.getImageResources().length != displayInfo.getAnimTitles().size()) {
                throw new IllegalArgumentException("the anim count must equals to the animTitle count");
            }
            int dividerWidth = animContainer.getContext().getResources().getDimensionPixelSize(
                    R.dimen.coui_component_card_instruction_divider_width);
            for (int i = 0; i < displayInfo.getImageResources().length; i++) {
                Context context = itemView.getContext();
                AnimViewGroup animViewGroup = new AnimViewGroup(context);
                animViewGroup.bind(displayInfo, i);
                addAnimRoot(animViewGroup, dividerWidth);
            }
        }

        private void addAnimViewGroup(AnimDisplayInfo displayInfo, int index, int dividerWidth) {
            Context context = itemView.getContext();
            AnimViewGroup animViewGroup = new AnimViewGroup(context);
            animViewGroup.bind(displayInfo, index);
            addAnimRoot(animViewGroup, dividerWidth);
        }

        private void addAnimRoot(AnimViewGroup animViewGroup, int dividerWidth) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.weight = 1.0f;
            if (animContainer.getChildCount() != 0) {
                params.setMarginStart(dividerWidth);
            }
            animViewGroup.getRootView().setLayoutParams(params);
            animContainer.addView(animViewGroup.getRootView());
        }
    }

    public static final class AnimViewGroup {
        private final TextView animTitle;
        // Leapy modified 2026-07-26: Render OPPO's JSON assets through the
        // upstream Lottie view instead of the removed Effective placeholder.
        private final LottieAnimationView animView;
        private final LinearLayout rootView;

        public AnimViewGroup(Context context) {
            View view = View.inflate(context, R.layout.coui_component_card_instruction_anim, null);
            rootView = (LinearLayout) view;
            animView = rootView.findViewById(R.id.anim_view);
            animTitle = rootView.findViewById(R.id.anim_title);
        }

        public void bind(ImageDisplayInfo displayInfo, int index) {
            if (!displayInfo.getAnimTitles().isEmpty()) {
                animTitle.setVisibility(View.VISIBLE);
                animTitle.setText(displayInfo.getAnimTitles().get(index));
            }
            animView.setImageResource(displayInfo.getImageResources()[index]);
            animView.setLayoutParams(Companion.getAnimViewLayoutParam(
                    animView.getLayoutParams(), displayInfo.getAnimWidth(), displayInfo.getAnimHeight()));
        }

        public void bind(AnimDisplayInfo displayInfo, int index) {
            if (!displayInfo.getAnimTitles().isEmpty()) {
                animTitle.setText(displayInfo.getAnimTitles().get(index));
            } else {
                animTitle.setVisibility(View.GONE);
            }
            if (!displayInfo.getAnimResources().isEmpty()) {
                animView.setAnimation(displayInfo.getAnimResources().get(index));
            } else {
                animView.setAnimation(displayInfo.getAnimAssets().get(index));
            }
            animView.setLayoutParams(Companion.getAnimViewLayoutParam(
                    animView.getLayoutParams(), displayInfo.getAnimWidth(), displayInfo.getAnimHeight()));
        }

        public TextView getAnimTitle() {
            return animTitle;
        }

        public LottieAnimationView getAnimView() {
            return animView;
        }

        public LinearLayout getRootView() {
            return rootView;
        }
    }

    public static final class Companion {
        private Companion() {
        }

        LinearLayout.LayoutParams getAnimViewLayoutParam(ViewGroup.LayoutParams layoutParams, int width, int height) {
            LinearLayout.LayoutParams params = layoutParams instanceof LinearLayout.LayoutParams
                    ? (LinearLayout.LayoutParams) layoutParams
                    : new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
            params.gravity = android.view.Gravity.CENTER_HORIZONTAL;
            if (width > 0 && height > 0) {
                params.width = width;
                params.height = height;
            }
            return params;
        }
    }
}
