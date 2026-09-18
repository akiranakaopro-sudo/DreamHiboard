package com.coui.appcompat.springchain;

import android.util.Log;
import com.coui.appcompat.springchain.api.IChainItem;
import com.coui.appcompat.springchain.api.ISpringUpdateListener;
import com.facebook.rebound.OrigamiValueConverter;
import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringSystem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


public final class COUIGridSpringChain {
    public static final int ATTACHMENT_FRICTION = 10;
    public static final int ATTACHMENT_TENSION = 150;
    public static final int BACK_TO_BOTTOM = 2;
    public static final int BACK_TO_LEFT = 3;
    public static final int BACK_TO_RIGHT = 4;
    public static final int BACK_TO_TOP = 1;
    public static final boolean DEBUG = true;
    public static final int MAIN_FRICTION = 10;
    public static final int MAIN_TENSION = 150;
    public static final int MAX_X = 50;
    public static final int MAX_Y = 50;
    public static final String TAG = "COUIGridSpringChain";
    private List<List<IChainItem>> allItems;
    private TransCalculator backToBottomTC;
    private TransCalculator backToLeftTC;
    private TransCalculator backToRightTC;
    private TransCalculator backToTopTC;
    private float[] curSpringXArray;
    private float[] curSpringYArray;
    private int currentMaxX;
    private int currentMaxY;
    private boolean enableAutoAcc;
    private int lastMoveDirection;
    private int lastReboundDirection;
    private float[] lastSpringXArray;
    private float[] lastSpringYArray;
    private int maxXSize;
    private int maxYSize;
    private COUISpringChain springChainX;
    private COUISpringChain springChainY;
    private SpringSystem springSystem;
    private ISpringUpdateListener springUpdateListener;
    private int xDirection;
    private int yDirection;

    public static final class DefaultTransCalculator implements TransCalculator {
        @Override
        public float getTrans(int index, float distance, int direction) {
            return distance * 0.1f;
        }
    }

    public final class GridSpringListener extends SimpleSpringListener {
        private int direction;
        private int index;

        public GridSpringListener(int index, int direction) {
            this.index = index;
            this.direction = direction;
        }

        public final int getDirection() {
            return this.direction;
        }

        public final int getIndex() {
            return this.index;
        }

        @Override
        public void onSpringUpdate(Spring spring) {
            Objects.requireNonNull(spring, "spring");
            if (COUIGridSpringChain.this.yDirection != -1 && COUIGridSpringChain.this.isPortrait(this.direction)) {
                COUIGridSpringChain.this.springUpdateTranslation(this.index, (float) spring.getCurrentValue(), COUIGridSpringChain.this.yDirection);
            } else {
                if (COUIGridSpringChain.this.xDirection == -1 || COUIGridSpringChain.this.isPortrait(this.direction)) {
                    return;
                }
                COUIGridSpringChain.this.springUpdateTranslation(this.index, (float) spring.getCurrentValue(), COUIGridSpringChain.this.xDirection);
            }
        }

        public final void setDirection(int direction) {
            this.direction = direction;
        }

        public final void setIndex(int index) {
            this.index = index;
        }
    }

    public interface TransCalculator {
        float getTrans(int index, float distance, int direction);
    }

    public COUIGridSpringChain() {
        this(MAIN_TENSION, MAIN_FRICTION, ATTACHMENT_TENSION, ATTACHMENT_FRICTION,
                MAIN_TENSION, MAIN_FRICTION, ATTACHMENT_TENSION, ATTACHMENT_FRICTION,
                MAX_X, MAX_Y);
    }

    private final float calculateTranslation(int position, float velocity, int direction) {
        int index = direction == 4 ? this.currentMaxX - position : position;
        if (direction == 2) {
            index = this.currentMaxY - position;
        }
        if (!this.enableAutoAcc) {
            return getTrans(index, velocity, direction);
        }
        float trans = 0.0f;
        if (index < 0) {
            return 0.0f;
        }
        int index_2 = 0;
        while (true) {
            if (!skipCumulativeCalculate(index_2, index, direction)) {
                trans += getTrans(index_2, velocity, direction);
            }
            if (index_2 == index) {
                return trans;
            }
            index_2++;
        }
    }

    private final boolean checkItemsCount(int index, int index_2) {
        return this.allItems.size() > index && this.allItems.get(index).size() > index_2;
    }

    private final int getCurrentMaxSize(int index) {
        return isPortrait(index) ? this.currentMaxY : this.currentMaxX;
    }

    private final float getTrans(int position, float velocity, int direction_2) {
        if (direction_2 == 1) {
            TransCalculator transCalculator = this.backToTopTC;
            if (transCalculator != null) {
                return transCalculator.getTrans(position, velocity, direction_2);
            }
            return 0.0f;
        }
        if (direction_2 == 2) {
            TransCalculator transCalculator2 = this.backToBottomTC;
            if (transCalculator2 != null) {
                return transCalculator2.getTrans(position, velocity, direction_2);
            }
            return 0.0f;
        }
        if (direction_2 == 3) {
            TransCalculator transCalculator3 = this.backToLeftTC;
            if (transCalculator3 != null) {
                return transCalculator3.getTrans(position, velocity, direction_2);
            }
            return 0.0f;
        }
        if (direction_2 == 4) {
            TransCalculator transCalculator4 = this.backToRightTC;
            if (transCalculator4 != null) {
                return transCalculator4.getTrans(position, velocity, direction_2);
            }
            return 0.0f;
        }
        Log.e(TAG, "getDelta: error direction=" + direction_2);
        return 0.0f;
    }

    private final float getTranslation(float value, IChainItem iChainItem, int index) {
        int itemIndex;
        if (index == BACK_TO_TOP) {
            itemIndex = iChainItem.getItemY();
        } else if (index == BACK_TO_BOTTOM) {
            itemIndex = (iChainItem.getItemY() + iChainItem.getItemHeight()) - 1;
        } else if (index == BACK_TO_LEFT) {
            itemIndex = iChainItem.getItemX();
        } else if (index == BACK_TO_RIGHT) {
            itemIndex = (iChainItem.getItemX() + iChainItem.getItemWidth()) - 1;
        } else {
            itemIndex = 0;
        }
        return calculateTranslation(itemIndex, value, index);
    }


    public final boolean isPortrait(int index) {
        if (index == 1 || index == 2) {
            return true;
        }
        if (index == 3 || index == 4) {
            return false;
        }
        throw new IllegalArgumentException("isPortrait: wrong dir=" + index);
    }

    private final void removeItem(IChainItem iChainItem) {
        int itemX = iChainItem.getItemX();
        int itemY = iChainItem.getItemY();
        int itemWidth = iChainItem.getItemWidth() + itemX;
        int itemHeight = iChainItem.getItemHeight() + itemY;
        if (itemWidth > this.currentMaxX || itemHeight > this.currentMaxY) {
            Log.e(TAG, "can not remove a item that over gridSpringChain size");
            return;
        }
        while (itemY < itemHeight) {
            for (int index = itemX; index < itemWidth; index++) {
                if (checkItemsCount(itemY, index)) {
                    this.allItems.get(itemY).set(index, null);
                }
            }
            itemY++;
        }
        boolean flag = itemWidth == this.currentMaxX;
        boolean flag_2 = itemHeight == this.currentMaxY;
        if (flag) {
            while (flag && this.currentMaxX > 0) {
                int index_2 = this.currentMaxY;
                int index_3 = 0;
                while (true) {
                    if (index_3 >= index_2) {
                        break;
                    }
                    if (checkItemsCount(index_3, this.currentMaxX - 1) && this.allItems.get(index_3).get(this.currentMaxX - 1) != null) {
                        flag = false;
                        break;
                    }
                    index_3++;
                }
                if (flag) {
                    this.currentMaxX--;
                }
            }
        }
        if (flag_2) {
            while (flag_2 && this.currentMaxY > 0) {
                int index_4 = this.currentMaxX;
                int index_5 = 0;
                while (true) {
                    if (index_5 >= index_4) {
                        break;
                    }
                    if (checkItemsCount(this.currentMaxY - 1, index_5) && this.allItems.get(this.currentMaxY - 1).get(index_5) != null) {
                        flag_2 = false;
                        break;
                    }
                    index_5++;
                }
                if (flag_2) {
                    this.currentMaxY--;
                }
            }
        }
    }

    private final boolean skipCumulativeCalculate(int index, int position, int direction) {
        if (index != position) {
            if (isPortrait(direction)) {
                if (direction == 2) {
                    index = this.currentMaxY - index;
                }
                int index_4 = this.currentMaxX;
                for (int index_2 = 0; index_2 < index_4; index_2++) {
                    if (checkItemsCount(index, index_2) && this.allItems.get(index).get(index_2) != null) {
                        IChainItem iChainItem = this.allItems.get(index).get(index_2);
                        if (iChainItem.getSkipSpringChainCalc()) {
                            return true;
                        }
                    }
                }
            } else {
                if (direction == 4) {
                    index = this.currentMaxX - index;
                }
                int index_5 = this.currentMaxY;
                for (int index_3 = 0; index_3 < index_5; index_3++) {
                    if (checkItemsCount(index_3, index) && this.allItems.get(index_3).get(index) != null) {
                        IChainItem iChainItem2 = this.allItems.get(index_3).get(index);
                        if (iChainItem2.getSkipSpringChainCalc()) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private final void updateSpring(float value, IChainItem iChainItem, int index) {
        float translation = getTranslation(value, iChainItem, index);
        if (isPortrait(index)) {
            int itemY = iChainItem.getItemY();
            float[] fArr = this.curSpringYArray;
            float value_2 = translation + this.lastSpringYArray[itemY];
            fArr[itemY] = value_2;
            iChainItem.updateSpringY(value_2);
            return;
        }
        int itemX = iChainItem.getItemX();
        float[] fArr2 = this.curSpringXArray;
        float value_3 = translation + this.lastSpringXArray[itemX];
        fArr2[itemX] = value_3;
        iChainItem.updateSpringX(value_3);
    }

    private final void updateSpringChain(int index) {
        COUISpringChain cOUISpringChain = isPortrait(index) ? this.springChainY : this.springChainX;
        int currentMaxSize = getCurrentMaxSize(index);
        for (int size = cOUISpringChain.getAllSprings().size(); size < currentMaxSize; size++) {
            cOUISpringChain.addSpring(new GridSpringListener(size, index));
        }
    }

    public final void addItem(IChainItem gridSpringItem) {
        IChainItem iChainItem;
        Objects.requireNonNull(gridSpringItem, "gridSpringItem");
        int itemX = gridSpringItem.getItemX();
        int itemY = gridSpringItem.getItemY();
        int itemWidth = gridSpringItem.getItemWidth() + itemX;
        int itemHeight = gridSpringItem.getItemHeight() + itemY;
        if (itemHeight > this.maxYSize || itemWidth > this.maxXSize) {
            Log.d(TAG, "can not addItem for the gridSpringChain is full");
            return;
        }
        for (int index = itemY; index < itemHeight; index++) {
            for (int index_2 = itemX; index_2 < itemWidth; index_2++) {
                if (checkItemsCount(index, index_2) && (iChainItem = this.allItems.get(index).get(index_2)) != null) {
                    removeItem(iChainItem);
                }
            }
        }
        while (itemY < itemHeight) {
            for (int index_3 = itemX; index_3 < itemWidth; index_3++) {
                while (this.allItems.size() <= itemY) {
                    this.allItems.add(new ArrayList());
                }
                while (this.allItems.get(itemY).size() <= index_3) {
                    this.allItems.get(itemY).add(null);
                }
                this.allItems.get(itemY).set(index_3, gridSpringItem);
            }
            itemY++;
        }
        if (itemWidth > this.currentMaxX) {
            this.currentMaxX = itemWidth;
        }
        if (itemHeight > this.currentMaxY) {
            this.currentMaxY = itemHeight;
        }
    }

    public final void clearAllItems() {
        IChainItem iChainItem;
        int index_3 = this.currentMaxY;
        for (int index = 0; index < index_3; index++) {
            int index_4 = this.currentMaxX;
            for (int index_2 = 0; index_2 < index_4; index_2++) {
                if (checkItemsCount(index, index_2) && (iChainItem = this.allItems.get(index).get(index_2)) != null) {
                    removeItem(iChainItem);
                }
            }
        }
        this.currentMaxX = 0;
        this.currentMaxY = 0;
    }

    public final float getCurrentSpringX(int index) {
        if (index <= -1) {
            return -1.0f;
        }
        float[] fArr = this.curSpringXArray;
        if (index < fArr.length) {
            return fArr[index];
        }
        return -1.0f;
    }

    public final float getCurrentSpringY(int index) {
        if (index <= -1) {
            return -1.0f;
        }
        float[] fArr = this.curSpringYArray;
        if (index < fArr.length) {
            return fArr[index];
        }
        return -1.0f;
    }

    public final float getLastTranslationX() {
        return this.lastReboundDirection == 4 ? this.lastSpringXArray[this.currentMaxX - 1] : this.lastSpringXArray[0];
    }

    public final float getLastTranslationY() {
        return this.lastReboundDirection == 2 ? this.lastSpringYArray[this.currentMaxY - 1] : this.lastSpringYArray[0];
    }

    public final COUISpringChain getSpringChainX() {
        return this.springChainX;
    }

    public final COUISpringChain getSpringChainY() {
        return this.springChainY;
    }

    public final int isSpringSystemRunning() {
        if (isSpringSystemIdle()) {
            return 0;
        }
        return this.lastReboundDirection;
    }

    private boolean isSpringSystemIdle() {
        if (this.springSystem == null) {
            return true;
        }
        for (Spring spring : this.springSystem.getAllSprings()) {
            if (!spring.isAtRest()) {
                return false;
            }
        }
        return true;
    }

    public final void releaseSpring() {
        for (Spring spring : this.springChainY.getAllSprings()) {
            spring.setEndValue(spring.getCurrentValue());
            spring.setAtRest();
        }
        this.yDirection = -1;
        for (Spring spring : this.springChainX.getAllSprings()) {
            spring.setEndValue(spring.getCurrentValue());
            spring.setAtRest();
        }
        this.xDirection = -1;
    }

    public final void releaseSpringAndLoc() {
        releaseSpringComplete();
        resetTranslation();
    }

    public final void releaseSpringComplete() {
        for (Spring spring : this.springChainY.getAllSprings()) {
            spring.setEndValue(0.0d);
            spring.setAtRest();
        }
        this.yDirection = -1;
        for (Spring spring : this.springChainX.getAllSprings()) {
            spring.setEndValue(0.0d);
            spring.setAtRest();
        }
        this.xDirection = -1;
    }

    public final void resetTranslation() {
        Arrays.fill(this.lastSpringXArray, 0.0f);
        Arrays.fill(this.lastSpringYArray, 0.0f);
        updateMoveTranslation(0.0f, 1);
        updateMoveTranslation(0.0f, 3);
    }

    public final void setCurrentSpringX(int index, float value) {
        if (index > -1) {
            float[] fArr = this.curSpringXArray;
            if (index < fArr.length) {
                fArr[index] = value;
            }
        }
    }

    public final void setCurrentSpringY(int index, float value) {
        if (index > -1) {
            float[] fArr = this.curSpringYArray;
            if (index < fArr.length) {
                fArr[index] = value;
            }
        }
    }

    public final void setEnableAutoAcc(boolean enableAutoAcc_2) {
        this.enableAutoAcc = enableAutoAcc_2;
    }

    public final void setSpringUpdateListener(ISpringUpdateListener springUpdateListener) {
        this.springUpdateListener = Objects.requireNonNull(springUpdateListener,
                "springUpdateListener");
    }

    public final void setTranCalculator(int tranCalculator) {
        if (tranCalculator == 1) {
            this.backToTopTC = new DefaultTransCalculator();
            return;
        }
        if (tranCalculator == 2) {
            this.backToBottomTC = new DefaultTransCalculator();
            return;
        }
        if (tranCalculator == 3) {
            this.backToLeftTC = new DefaultTransCalculator();
        } else if (tranCalculator != 4) {
            Log.e(TAG, "the direction is not illegal!");
        } else {
            this.backToRightTC = new DefaultTransCalculator();
        }
    }

    public final void springUpdateTranslation(int index, float value, int index_2) {
        IChainItem iChainItem;
        IChainItem iChainItem2;
        IChainItem iChainItem3;
        IChainItem iChainItem4;
        int index_3 = 0;
        if (!isPortrait(index_2)) {
            if (index_2 == 4) {
                while (index_3 < this.currentMaxY) {
                    if (checkItemsCount(index_3, index) && (iChainItem2 = this.allItems.get(index_3).get(index)) != null && iChainItem2.getItemY() == index_3 && (iChainItem2.getItemX() + iChainItem2.getItemWidth()) - 1 == index) {
                        iChainItem2.updateSpringX(value);
                    }
                    index_3++;
                }
            } else {
                while (index_3 < this.currentMaxY) {
                    if (checkItemsCount(index_3, index) && (iChainItem = this.allItems.get(index_3).get(index)) != null && iChainItem.getItemY() == index_3 && iChainItem.getItemX() == index) {
                        iChainItem.updateSpringX(value);
                    }
                    index_3++;
                }
            }
            this.curSpringXArray[index] = value;
            this.lastSpringXArray[index] = value;
            return;
        }
        if (index_2 == 2) {
            while (index_3 < this.currentMaxX) {
                if (checkItemsCount(index, index_3) && (iChainItem4 = this.allItems.get(index).get(index_3)) != null && (iChainItem4.getItemY() + iChainItem4.getItemHeight()) - 1 == index && iChainItem4.getItemX() == index_3) {
                    iChainItem4.updateSpringY(value);
                    ISpringUpdateListener iSpringUpdateListener = this.springUpdateListener;
                    if (iSpringUpdateListener != null) {
                        iSpringUpdateListener.onUpdate(index, value, index_2, iChainItem4);
                    }
                }
                index_3++;
            }
        } else {
            while (index_3 < this.currentMaxX) {
                if (checkItemsCount(index, index_3) && (iChainItem3 = this.allItems.get(index).get(index_3)) != null && iChainItem3.getItemY() == index && iChainItem3.getItemX() == index_3) {
                    iChainItem3.updateSpringY(value);
                    ISpringUpdateListener iSpringUpdateListener2 = this.springUpdateListener;
                    if (iSpringUpdateListener2 != null) {
                        iSpringUpdateListener2.onUpdate(index, value, index_2, iChainItem3);
                    }
                }
                index_3++;
            }
        }
        this.curSpringYArray[index] = value;
        this.lastSpringYArray[index] = value;
    }

    public final void startRebound(int index) {
        if (index != 0) {
            this.lastReboundDirection = index;
        } else {
            index = this.lastReboundDirection;
            if (index == 0) {
                return;
            }
        }
        boolean zIsPortrait = isPortrait(index);
        updateSpringChain(4);
        updateSpringChain(2);
        COUISpringChain cOUISpringChain = zIsPortrait ? this.springChainY : this.springChainX;
        int index_3 = zIsPortrait ? this.currentMaxY : this.currentMaxX;
        float[] fArr = zIsPortrait ? this.curSpringYArray : this.curSpringXArray;
        if (zIsPortrait) {
            this.yDirection = index;
        } else {
            this.xDirection = index;
        }
        List<Spring> allSprings = cOUISpringChain.getAllSprings();
        int index_4 = -1;
        for (int index_2 = 0; index_2 < index_3; index_2++) {
            Spring spring = allSprings.get(index_2);
            double doubleValue = fArr[index_2];
            if ((doubleValue >= 0.0d || index_2 == index_3 - 1) && index_4 == -1) {
                index_4 = index_2;
            }
            spring.setCurrentValue(doubleValue, false);
            spring.setVelocity(0.0d);
        }
        if (index_4 == -1) {
            Log.d(TAG, "startRebound failed : chain is empty");
            return;
        }
        Log.d(TAG, "startRebound : ctrIndex=:" + index_4 + " ,endValue=:0.0");
        cOUISpringChain.setControlSpringIndex(index_4).getControlSpring().setEndValue(0.0d);
    }

    public final void updateMoveTranslation(float value, int index) {
        IChainItem iChainItem;
        if (index != 0) {
            this.lastMoveDirection = index;
        } else {
            index = this.lastMoveDirection;
        }
        updateSpringChain(4);
        updateSpringChain(2);
        if (isPortrait(index)) {
            this.yDirection = index;
        } else {
            this.xDirection = index;
        }
        int index_4 = this.currentMaxY;
        for (int index_2 = 0; index_2 < index_4; index_2++) {
            int index_5 = this.currentMaxX;
            for (int index_3 = 0; index_3 < index_5; index_3++) {
                if (checkItemsCount(index_2, index_3) && (iChainItem = this.allItems.get(index_2).get(index_3)) != null && iChainItem.getItemY() == index_2 && iChainItem.getItemX() == index_3) {
                    updateSpring(value, iChainItem, index);
                }
            }
        }
    }

    public final void updateSpringChainConfig(double doubleValue, double doubleValue_2, boolean flag) {
        COUISpringChain cOUISpringChain = flag ? this.springChainX : this.springChainY;
        cOUISpringChain.getMainSpringConfig().friction = OrigamiValueConverter.frictionFromOrigamiValue(doubleValue);
        cOUISpringChain.getMainSpringConfig().tension = OrigamiValueConverter.tensionFromOrigamiValue(doubleValue_2);
        cOUISpringChain.getAttachmentSpringConfig().friction = OrigamiValueConverter.frictionFromOrigamiValue(doubleValue);
        cOUISpringChain.getAttachmentSpringConfig().friction = OrigamiValueConverter.frictionFromOrigamiValue(doubleValue_2);
    }

    public COUIGridSpringChain(int index, int index_2, int index_3, int index_4, int index_5, int index_6, int index_7, int index_8, int index_9, int index_10) {
        this.xDirection = -1;
        this.yDirection = -1;
        this.curSpringYArray = new float[index_10];
        this.curSpringXArray = new float[index_9];
        this.lastSpringYArray = new float[index_10];
        this.lastSpringXArray = new float[index_9];
        this.allItems = new ArrayList();
        this.springSystem = SpringSystem.create();
        this.enableAutoAcc = true;
        COUISpringChain cOUISpringChainCreate = COUISpringChain.create(this.springSystem, index_5, index_6, index_7, index_8);
        this.springChainX = cOUISpringChainCreate;
        COUISpringChain cOUISpringChainCreate2 = COUISpringChain.create(this.springSystem, index, index_2, index_3, index_4);
        this.springChainY = cOUISpringChainCreate2;
        this.maxXSize = index_9;
        this.maxYSize = index_10;
        this.lastMoveDirection = 2;
    }

    public final void setTranCalculator(TransCalculator transCalculator, int tranCalculator) {
        Objects.requireNonNull(transCalculator, "transCalculator");
        if (tranCalculator == 1) {
            this.backToTopTC = transCalculator;
            return;
        }
        if (tranCalculator == 2) {
            this.backToBottomTC = transCalculator;
            return;
        }
        if (tranCalculator == 3) {
            this.backToLeftTC = transCalculator;
        } else if (tranCalculator != 4) {
            Log.e(TAG, "the direction is not illegal!");
        } else {
            this.backToRightTC = transCalculator;
        }
    }

    public final void releaseSpring(int index) {
        if (isPortrait(index)) {
            for (Spring spring : this.springChainY.getAllSprings()) {
                spring.setEndValue(spring.getCurrentValue());
                spring.setAtRest();
            }
            this.yDirection = -1;
            return;
        }
        for (Spring spring : this.springChainX.getAllSprings()) {
            spring.setEndValue(spring.getCurrentValue());
            spring.setAtRest();
        }
        this.xDirection = -1;
    }
}
