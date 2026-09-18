package com.coui.appcompat.springchain;

import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringConfigRegistry;
import com.facebook.rebound.SpringListener;
import com.facebook.rebound.SpringSystem;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class COUISpringChain implements SpringListener {
    private static final int DEFAULT_ATTACHMENT_FRICTION = 10;
    private static final int DEFAULT_ATTACHMENT_TENSION = 70;
    private static final int DEFAULT_MAIN_FRICTION = 6;
    private static final int DEFAULT_MAIN_TENSION = 40;
    private final SpringConfig mAttachmentSpringConfig;
    private int mControlSpringIndex;
    private final CopyOnWriteArrayList<SpringListener> mListeners;
    private final SpringConfig mMainSpringConfig;
    private final SpringSystem mSpringSystem;
    private final CopyOnWriteArrayList<Spring> mSprings;
    private static final SpringConfigRegistry registry = SpringConfigRegistry.getInstance();
    private static int id = 0;

    private COUISpringChain(SpringSystem springSystem) {
        this(springSystem, DEFAULT_MAIN_TENSION, DEFAULT_MAIN_FRICTION,
                DEFAULT_ATTACHMENT_TENSION, DEFAULT_ATTACHMENT_FRICTION);
    }

    private COUISpringChain(SpringSystem springSystem, int mainTension, int mainFriction,
            int attachmentTension, int attachmentFriction) {
        this.mListeners = new CopyOnWriteArrayList<>();
        this.mSprings = new CopyOnWriteArrayList<>();
        this.mControlSpringIndex = -1;
        this.mSpringSystem = springSystem;
        this.mMainSpringConfig = SpringConfig.fromOrigamiTensionAndFriction(mainTension, mainFriction);
        this.mAttachmentSpringConfig =
                SpringConfig.fromOrigamiTensionAndFriction(attachmentTension, attachmentFriction);
        registry.addSpringConfig(this.mMainSpringConfig, "main spring " + id++);
        registry.addSpringConfig(this.mAttachmentSpringConfig, "attachment spring " + id++);
    }

    public static COUISpringChain create(SpringSystem springSystem) {
        return new COUISpringChain(springSystem);
    }

    public static COUISpringChain create(SpringSystem springSystem, int mainTension, int mainFriction,
            int attachmentTension, int attachmentFriction) {
        return new COUISpringChain(springSystem, mainTension, mainFriction,
                attachmentTension, attachmentFriction);
    }

    public COUISpringChain addSpring(SpringListener listener) {
        Spring spring = this.mSpringSystem.createSpring();
        spring.addListener(this)
                .setSpringConfig(this.mAttachmentSpringConfig)
                .setRestSpeedThreshold(0.1d)
                .setRestDisplacementThreshold(0.1d);
        this.mSprings.add(spring);
        this.mListeners.add(listener);
        return this;
    }

    public List<SpringListener> getAllListeners() {
        return this.mListeners;
    }

    public List<Spring> getAllSprings() {
        return this.mSprings;
    }

    public SpringConfig getAttachmentSpringConfig() {
        return this.mAttachmentSpringConfig;
    }

    public Spring getControlSpring() {
        return this.mSprings.get(this.mControlSpringIndex);
    }

    public SpringConfig getMainSpringConfig() {
        return this.mMainSpringConfig;
    }

    @Override
    public void onSpringActivate(Spring spring) {
        CopyOnWriteArrayList<SpringListener> listeners;
        int indexOf;
        CopyOnWriteArrayList<Spring> springs = this.mSprings;
        if (springs == null || springs.isEmpty() || (listeners = this.mListeners) == null
                || listeners.isEmpty() || (indexOf = this.mSprings.indexOf(spring)) < 0
                || indexOf >= this.mListeners.size()) {
            return;
        }
        this.mListeners.get(indexOf).onSpringActivate(spring);
    }

    @Override
    public void onSpringAtRest(Spring spring) {
        CopyOnWriteArrayList<SpringListener> listeners;
        int indexOf;
        CopyOnWriteArrayList<Spring> springs = this.mSprings;
        if (springs == null || springs.isEmpty() || (listeners = this.mListeners) == null
                || listeners.isEmpty() || (indexOf = this.mSprings.indexOf(spring)) < 0
                || indexOf >= this.mListeners.size()) {
            return;
        }
        this.mListeners.get(indexOf).onSpringAtRest(spring);
    }

    @Override
    public void onSpringEndStateChange(Spring spring) {
        CopyOnWriteArrayList<SpringListener> listeners;
        int indexOf;
        CopyOnWriteArrayList<Spring> springs = this.mSprings;
        if (springs == null || springs.isEmpty() || (listeners = this.mListeners) == null
                || listeners.isEmpty() || (indexOf = this.mSprings.indexOf(spring)) < 0
                || indexOf >= this.mListeners.size()) {
            return;
        }
        this.mListeners.get(indexOf).onSpringEndStateChange(spring);
    }

    @Override
    public void onSpringUpdate(Spring spring) {
        CopyOnWriteArrayList<SpringListener> listeners;
        int indexOf;
        int nextIndex;
        int previousIndex;
        CopyOnWriteArrayList<Spring> springs = this.mSprings;
        if (springs == null || springs.isEmpty() || (listeners = this.mListeners) == null
                || listeners.isEmpty() || (indexOf = this.mSprings.indexOf(spring)) < 0
                || indexOf >= this.mListeners.size()) {
            return;
        }
        SpringListener listener = this.mListeners.get(indexOf);
        int controlSpringIndex = this.mControlSpringIndex;
        if (indexOf == controlSpringIndex) {
            previousIndex = indexOf - 1;
            nextIndex = indexOf + 1;
        } else if (indexOf < controlSpringIndex) {
            previousIndex = indexOf - 1;
            nextIndex = -1;
        } else {
            nextIndex = indexOf + 1;
            previousIndex = -1;
        }
        if (nextIndex > -1 && nextIndex < this.mSprings.size()) {
            this.mSprings.get(nextIndex).setEndValue(spring.getCurrentValue());
        }
        if (previousIndex > -1 && previousIndex < this.mSprings.size()) {
            this.mSprings.get(previousIndex).setEndValue(spring.getCurrentValue());
        }
        listener.onSpringUpdate(spring);
    }

    public COUISpringChain setControlSpringIndex(int controlSpringIndex) {
        this.mControlSpringIndex = controlSpringIndex;
        if (this.mSprings.get(controlSpringIndex) == null) {
            return null;
        }
        Iterator<Spring> iterator = this.mSpringSystem.getAllSprings().iterator();
        while (iterator.hasNext()) {
            iterator.next().setSpringConfig(this.mAttachmentSpringConfig);
        }
        getControlSpring().setSpringConfig(this.mMainSpringConfig);
        return this;
    }
}
