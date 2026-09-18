package com.coui.appcompat.card;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;

import androidx.preference.PreferenceViewHolder;
import androidx.viewpager2.widget.ViewPager2;

import com.coui.appcompat.R;
import com.coui.appcompat.darkmode.COUIDarkModeUtil;
import com.coui.appcompat.indicator.COUIPageIndicator;
import com.coui.appcompat.preference.COUIPreference;

import java.util.List;

public final class COUICardInstructionPreference extends COUIPreference {
    public static final Companion Companion = new Companion();
    public static final int CARD_INSTRUCTION_TYPE_DESCRIPTION = 1;
    public static final int CARD_INSTRUCTION_TYPE_SELECTOR = 2;

    private int cardType = CARD_INSTRUCTION_TYPE_DESCRIPTION;
    private int lastPagerItem;
    private BaseCardInstructionAdapter<?> pageAdapter;

    public interface OnItemSelectedListener {
        void onItemSelected(int position);
    }

    public COUICardInstructionPreference(Context context) {
        this(context, null);
    }

    public COUICardInstructionPreference(Context context, AttributeSet attrs) {
        this(context, attrs, androidx.preference.R.attr.preferenceStyle);
    }

    public COUICardInstructionPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public COUICardInstructionPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setLayoutResource(R.layout.coui_component_card_instruction_preference);
        TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.COUICardInstructionPreference, defStyleAttr, defStyleRes);
        int type = a.getInteger(
                R.styleable.COUICardInstructionPreference_instructionCardType,
                CARD_INSTRUCTION_TYPE_DESCRIPTION);
        a.recycle();
        setCardType(type);
    }

    private BaseCardInstructionAdapter<? extends BaseCardInstructionAdapter.BaseHolder> getPageAdapter(int cardType) {
        if (cardType == CARD_INSTRUCTION_TYPE_SELECTOR) {
            return new CardInstructionSelectorAdapter();
        }
        return new CardInstructionDescriptionAdapter();
    }

    private void setPagerCallback(ViewPager2 viewPager, COUIPageIndicator pageIndicator) {
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                pageIndicator.onPageScrollStateChanged(state);
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                pageIndicator.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                pageIndicator.onPageSelected(position);
                lastPagerItem = position;
            }
        });
    }

    public int getCardType() {
        return cardType;
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        COUIDarkModeUtil.setForceDarkAllow(holder.itemView, false);
        ViewPager2 viewPager = (ViewPager2) holder.findViewById(R.id.pager);
        COUIPageIndicator pageIndicator = (COUIPageIndicator) holder.findViewById(R.id.indicator);
        pageIndicator.setVisibility(pageAdapter.getItemCount() > 1 ? View.VISIBLE : View.GONE);
        if (pageAdapter.getItemCount() > 0) {
            viewPager.setAdapter(pageAdapter);
            viewPager.setCurrentItem(lastPagerItem);
            viewPager.setOffscreenPageLimit(pageAdapter.getItemCount());
            pageIndicator.setDotsCount(pageAdapter.getItemCount());
            setPagerCallback(viewPager, pageIndicator);
        }
    }

    public void setCardType(int cardType) {
        this.cardType = cardType;
        pageAdapter = getPageAdapter(cardType);
        notifyChanged();
    }

    public void setDisplayInfos(List<? extends BaseDisplayInfo> displayInfos) {
        if (displayInfos == null) {
            throw new NullPointerException("displayInfos");
        }
        pageAdapter.updateDisplayInfos(displayInfos);
        notifyChanged();
    }

    public void setOnItemSelected(OnItemSelectedListener listener) {
        if (listener == null) {
            throw new NullPointerException("onItemSelectedListener");
        }
        if (pageAdapter instanceof CardInstructionSelectorAdapter) {
            ((CardInstructionSelectorAdapter) pageAdapter).setOnSelectedCardChangedListener(listener);
        }
        notifyChanged();
    }

    public void setSelectedIndex(int selectedIndex) {
        if (pageAdapter instanceof CardInstructionSelectorAdapter) {
            ((CardInstructionSelectorAdapter) pageAdapter).setSelectedIndex(selectedIndex);
        }
        notifyChanged();
    }

    public static final class Companion {
        private Companion() {
        }
    }
}
