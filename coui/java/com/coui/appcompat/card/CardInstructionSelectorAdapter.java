package com.coui.appcompat.card;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import com.airbnb.lottie.LottieAnimationView;

import com.coui.appcompat.R;
import com.coui.appcompat.contextutil.COUIContextUtil;

import java.util.ArrayList;
import java.util.List;

public final class CardInstructionSelectorAdapter
        extends BaseCardInstructionAdapter<CardInstructionSelectorAdapter.SelectorHolder> {
    private int lastSelectedIndex = -1;
    private COUICardInstructionPreference.OnItemSelectedListener onSelectedCardChangedListener;

    public CardInstructionSelectorAdapter() {
        this(new ArrayList<>());
    }

    public CardInstructionSelectorAdapter(List<BaseDisplayInfo> displayInfos) {
        super(displayInfos);
    }

    public COUICardInstructionPreference.OnItemSelectedListener getOnSelectedCardChangedListener() {
        return onSelectedCardChangedListener;
    }

    public void setOnSelectedCardChangedListener(
            COUICardInstructionPreference.OnItemSelectedListener listener) {
        onSelectedCardChangedListener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setSelectedIndex(int selectedIndex) {
        lastSelectedIndex = selectedIndex;
        notifyDataSetChanged();
    }

    @Override
    public SelectorHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.coui_component_card_instruction_selector_page, parent, false);
        return new SelectorHolder(view, this);
    }

    @Override
    public void onBindViewHolder(SelectorHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        holder.setSelectedChoiceIndex(lastSelectedIndex);
    }

    public static final class Selector {
        // Leapy modified 2026-07-26: Keep decoded OPPO card animations while
        // using the upstream Lottie renderer directly.
        private final LottieAnimationView animView;
        private final RadioButton radio;
        private final View rootView;
        private final TextView title;

        public Selector(Context context) {
            rootView = View.inflate(context, R.layout.coui_component_card_instruction_selector, null);
            animView = rootView.findViewById(R.id.anim_view);
            title = rootView.findViewById(R.id.title);
            radio = rootView.findViewById(R.id.radio);
        }

        public LottieAnimationView getAnimView() {
            return animView;
        }

        public RadioButton getRadio() {
            return radio;
        }

        public View getRootView() {
            return rootView;
        }

        public TextView getTitle() {
            return title;
        }

        public void setAnimViewSize(int width, int height) {
            if (width <= 0 || height <= 0) {
                return;
            }
            ConstraintLayout.LayoutParams params =
                    (ConstraintLayout.LayoutParams) animView.getLayoutParams();
            params.width = width;
            params.height = height;
            animView.setLayoutParams(params);
        }
    }

    public final class SelectorHolder extends BaseCardInstructionAdapter.BaseHolder {
        private final LinearLayout selectorContainer;
        private final List<Selector> selectorGroup = new ArrayList<>();

        public SelectorHolder(View itemView, BaseCardInstructionAdapter<?> adapter) {
            super(itemView, adapter);
            selectorContainer = itemView.findViewById(R.id.container);
        }

        @Override
        public void bind(BaseDisplayInfo displayInfo) {
            selectorGroup.clear();
            selectorContainer.removeAllViews();
            if (displayInfo instanceof AnimDisplayInfo) {
                bindAnimDisplayInfo((AnimDisplayInfo) displayInfo);
            } else if (displayInfo instanceof ImageDisplayInfo) {
                bindImageDisplayInfo((ImageDisplayInfo) displayInfo);
            }
            dealRadioGroupClickEvents();
            setSelectedChoiceIndex(displayInfo.getSelectedIndex());
        }

        private void bindAnimDisplayInfo(AnimDisplayInfo displayInfo) {
            if (!displayInfo.getAnimAssets().isEmpty() && !displayInfo.getAnimResources().isEmpty()) {
                throw new IllegalArgumentException(
                        "imageAssets and imageResources cannot be used at the same time. Please use only one at once.");
            }
            if (displayInfo.getAnimAssets().size() + displayInfo.getAnimResources().size()
                    != displayInfo.getChoices().size()) {
                throw new IllegalArgumentException("the anim count must equal to the choice count");
            }
            int index = 0;
            for (Integer animRes : displayInfo.getAnimResources()) {
                Selector selector = createSelector(displayInfo, index);
                selector.getAnimView().setAnimation(animRes);
                selector.setAnimViewSize(displayInfo.getAnimWidth(), displayInfo.getAnimHeight());
                addSelector(selector);
                index++;
            }
            index = 0;
            for (String animAsset : displayInfo.getAnimAssets()) {
                Selector selector = createSelector(displayInfo, index);
                selector.getAnimView().setAnimation(animAsset);
                selector.setAnimViewSize(displayInfo.getAnimWidth(), displayInfo.getAnimHeight());
                addSelector(selector);
                index++;
            }
        }

        private void bindImageDisplayInfo(ImageDisplayInfo displayInfo) {
            if (displayInfo.getImageResources().length != displayInfo.getChoices().size()) {
                throw new IllegalArgumentException("the image count must equal to the choice count");
            }
            for (int i = 0; i < displayInfo.getImageResources().length; i++) {
                Selector selector = createSelector(displayInfo, i);
                selector.getAnimView().setImageResource(displayInfo.getImageResources()[i]);
                selector.setAnimViewSize(displayInfo.getAnimWidth(), displayInfo.getAnimHeight());
                addSelector(selector);
            }
        }

        private Selector createSelector(BaseDisplayInfo displayInfo, int index) {
            Selector selector = new Selector(itemView.getContext());
            BaseCardInstructionAdapter.Companion.updateContentAndVisibility(
                    selector.getTitle(), displayInfo.getChoices().get(index));
            return selector;
        }

        private void addSelector(Selector selector) {
            selectorContainer.addView(selector.getRootView());
            selectorGroup.add(selector);
        }

        private void dealRadioGroupClickEvents() {
            for (Selector selector : selectorGroup) {
                selector.getRootView().setOnClickListener(view -> {
                    int index = selectorGroup.indexOf(selector);
                    if (index != lastSelectedIndex) {
                        lastSelectedIndex = index;
                        if (onSelectedCardChangedListener != null) {
                            onSelectedCardChangedListener.onItemSelected(index);
                        }
                    }
                    setSelectedChoiceIndex(index);
                });
            }
        }

        @SuppressLint("PrivateResource")
        public void setSelectedChoiceIndex(int selectedIndex) {
            if (selectedIndex < 0 || selectedIndex >= selectorGroup.size()) {
                return;
            }
            Selector selected = selectorGroup.get(selectedIndex);
            selected.getRadio().setChecked(true);
            selected.getTitle().setTextAppearance(R.style.couiTextAppearanceButton);
            selected.getTitle().setTextColor(COUIContextUtil.getAttrColor(
                    itemView.getContext(), R.attr.couiColorPrimary));

            for (Selector selector : selectorGroup) {
                if (selector == selected) {
                    continue;
                }
                selector.getRadio().setChecked(false);
                selector.getTitle().setTextAppearance(R.style.couiTextAppearanceBody);
                selector.getTitle().setTextColor(COUIContextUtil.getAttrColor(
                        itemView.getContext(), R.attr.couiColorSecondNeutral));
            }
        }
    }
}
