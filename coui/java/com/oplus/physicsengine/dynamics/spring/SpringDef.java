package com.oplus.physicsengine.dynamics.spring;

import com.oplus.physicsengine.common.Vector;
import com.oplus.physicsengine.dynamics.Body;

public final class SpringDef {
    public Body bodyA;
    public Body bodyB;
    public float dampingRatio;
    public float frequencyHz;
    public float maxForce;
    public final Vector target = new Vector();

    public SpringDef() {
        target.mX = 0.0f;
        target.mY = 0.0f;
        frequencyHz = 6.0f;
        dampingRatio = 0.8f;
        maxForce = Float.MAX_VALUE;
    }

    public String toString() {
        return "SpringDef{target=" + target + ", frequencyHz=" + frequencyHz
                + ", dampingRatio=" + dampingRatio + "}@" + hashCode();
    }
}
