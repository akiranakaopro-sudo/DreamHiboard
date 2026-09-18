package com.coui.appcompat.springchain;


public interface ICOUIGridSpringChainViewGroup {

    public static final class DefaultImpls {
        public static float getLastTranslationY(ICOUIGridSpringChainViewGroup iCOUIGridSpringChainViewGroup) {
            COUIGridSpringChain springChain = iCOUIGridSpringChainViewGroup.getSpringChain();
            if (springChain != null) {
                return springChain.getLastTranslationY();
            }
            return 0.0f;
        }

        public static COUIGridSpringChain getSpringChain(ICOUIGridSpringChainViewGroup iCOUIGridSpringChainViewGroup) {
            return null;
        }

        public static int isSpringSystemRunning(ICOUIGridSpringChainViewGroup iCOUIGridSpringChainViewGroup) {
            COUIGridSpringChain springChain = iCOUIGridSpringChainViewGroup.getSpringChain();
            if (springChain != null) {
                return springChain.isSpringSystemRunning();
            }
            return 0;
        }

        public static void releaseSpring(ICOUIGridSpringChainViewGroup iCOUIGridSpringChainViewGroup) {
            COUIGridSpringChain springChain = iCOUIGridSpringChainViewGroup.getSpringChain();
            if (springChain != null) {
                springChain.releaseSpring();
            }
        }

        public static void startRebound(ICOUIGridSpringChainViewGroup iCOUIGridSpringChainViewGroup, int index) {
            COUIGridSpringChain springChain = iCOUIGridSpringChainViewGroup.getSpringChain();
            if (springChain != null) {
                springChain.startRebound(index);
            }
        }

        public static void updateMoveTranslation(ICOUIGridSpringChainViewGroup iCOUIGridSpringChainViewGroup, float value, int index) {
            COUIGridSpringChain springChain = iCOUIGridSpringChainViewGroup.getSpringChain();
            if (springChain != null) {
                springChain.updateMoveTranslation(value, index);
            }
        }
    }

    float getLastTranslationY();

    COUIGridSpringChain getSpringChain();

    int isSpringSystemRunning();

    void releaseSpring();

    void startRebound(int index);

    void updateMoveTranslation(float value, int index);
}
