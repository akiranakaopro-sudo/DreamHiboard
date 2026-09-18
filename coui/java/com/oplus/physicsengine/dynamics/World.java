package com.oplus.physicsengine.dynamics;

import com.oplus.physicsengine.common.Vector;
import com.oplus.physicsengine.dynamics.spring.Edge;
import com.oplus.physicsengine.dynamics.spring.Spring;

public final class World {
    public int mBodyCount;
    public Body mBodyList;
    public int mSpringCount;
    public Spring mSpringList;
    public final Vector mVectorTemp = new Vector();

    public Body createBody(Vector position, int type, int property, float width, float height, String tag) {
        Body body = new Body(position, type, property, width, height);
        body.setTag(tag);
        body.mPrev = null;
        body.mNext = mBodyList;
        if (mBodyList != null) {
            mBodyList.mPrev = body;
        }
        mBodyList = body;
        mBodyCount++;
        return body;
    }

    public Spring createSpring(com.oplus.physicsengine.dynamics.spring.SpringDef springDef) {
        Spring spring = new Spring(mVectorTemp, springDef);
        spring.mPrev = null;
        spring.mNext = mSpringList;
        if (mSpringList != null) {
            mSpringList.mPrev = spring;
        }
        mSpringList = spring;
        mSpringCount++;
        linkEdge(spring.getBodyA(), spring.mEdgeA);
        linkEdge(spring.getBodyB(), spring.mEdgeB);
        return spring;
    }

    public void destroyBody(Body body) {
        if (body == null || mBodyCount <= 0) {
            return;
        }
        Edge edge = body.mEdgeList;
        while (edge != null) {
            Edge next = edge.next;
            if (edge.spring != null) {
                destroySpring(edge.spring);
            }
            edge = next;
        }
        if (body.mPrev != null) {
            body.mPrev.mNext = body.mNext;
        }
        if (body.mNext != null) {
            body.mNext.mPrev = body.mPrev;
        }
        if (body == mBodyList) {
            mBodyList = body.mNext;
        }
        body.mPrev = null;
        body.mNext = null;
        mBodyCount--;
    }

    public void destroySpring(Spring spring) {
        if (mSpringCount <= 0 || spring == null) {
            return;
        }
        Spring prev = spring.mPrev;
        Spring next = spring.mNext;
        if (prev != null) {
            prev.mNext = next;
        }
        if (next != null) {
            next.mPrev = prev;
        }
        if (spring == mSpringList) {
            mSpringList = next;
        }
        unlinkEdge(spring.getBodyA(), spring.mEdgeA);
        unlinkEdge(spring.getBodyB(), spring.mEdgeB);
        spring.mPrev = null;
        spring.mNext = null;
        mSpringCount--;
    }

    public Vector getVectorTemp() {
        return mVectorTemp;
    }

    public void step(float timeStep) {
        for (Body body = mBodyList; body != null; body = body.mNext) {
            body.mIsSolved = false;
        }
        for (Spring spring = mSpringList; spring != null; spring = spring.mNext) {
            spring.mIsSolved = false;
        }
        for (Body body = mBodyList; body != null; body = body.mNext) {
            if (!body.mIsSolved && body.mIsAwake && body.getType() != 0) {
                solveBody(body, timeStep);
                body.mIsSolved = true;
                body.mForce.setZero();
            }
        }
    }

    private static void unlinkEdge(Body body, Edge edge) {
        if (body == null || edge == null) {
            return;
        }
        Edge prev = edge.prev;
        Edge next = edge.next;
        if (prev != null) {
            prev.next = next;
        }
        if (next != null) {
            next.prev = prev;
        }
        if (edge == body.mEdgeList) {
            body.mEdgeList = next;
        }
        edge.prev = null;
        edge.next = null;
    }

    private void solveBody(Body body, float timeStep) {
        body.applyActiveRectForce();
        mVectorTemp.set(body.mForce);
        mVectorTemp.mulLocal(body.mInvMass);
        mVectorTemp.mulLocal(timeStep);
        body.mLinearVelocity.addLocal(mVectorTemp);
        body.mLinearVelocity.mulLocal(1.0f / ((body.mLinearDamping * timeStep) + 1.0f));
        for (Edge edge = body.mEdgeList; edge != null; edge = edge.next) {
            Spring spring = edge.spring;
            if (spring != null && !spring.mIsSolved) {
                spring.mIsSolved = true;
                Body other = edge.other;
                if (other != null && !other.mIsSolved && other.mIsAwake) {
                    spring.prepare(body, timeStep);
                    for (int i = 0; i < 4; i++) {
                        spring.solve(body);
                    }
                }
            }
        }
        body.mWorldCenter.mX += body.mLinearVelocity.mX * timeStep;
        body.mWorldCenter.mY += body.mLinearVelocity.mY * timeStep;
        body.syncOriginFromWorld();
    }

    private static void linkEdge(Body body, Edge edge) {
        if (body == null || edge == null) {
            return;
        }
        edge.prev = null;
        edge.next = body.mEdgeList;
        if (body.mEdgeList != null) {
            body.mEdgeList.prev = edge;
        }
        body.mEdgeList = edge;
    }
}
