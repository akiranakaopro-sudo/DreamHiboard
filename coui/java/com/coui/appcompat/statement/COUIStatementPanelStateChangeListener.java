package com.coui.appcompat.statement;

import android.content.res.Configuration;
import com.coui.appcompat.uiutil.UIUtil;
import com.coui.component.responsiveui.unit.Dp;
import kotlin.jvm.internal.SourceDebugExtension;


public interface COUIStatementPanelStateChangeListener {
    public static final Companion Companion = new Companion();

    @SourceDebugExtension({"SMAP\nCOUIStatementPanelStateChangeListener.kt\nKotlin\n*S Kotlin\n*F\n+ 1 COUIStatementPanelStateChangeListener.kt\ncom/coui/appcompat/statement/COUIStatementPanelStateChangeListener$Companion\n+ 2 Dp.kt\ncom/coui/component/responsiveui/unit/DpKt\n*L\n1#1,99:1\n57#2:100\n57#2:101\n57#2:102\n57#2:103\n*S KotlinDebug\n*F\n+ 1 COUIStatementPanelStateChangeListener.kt\ncom/coui/appcompat/statement/COUIStatementPanelStateChangeListener$Companion\n*L\n31#1:100\n34#1:101\n36#1:102\n39#1:103\n*E\n"})
    public static final class Companion {
        private static final Dp SCREN_DP_MINI_WIDTH = new Dp(207);
        private static final Dp SCREN_DP_SPLIT_HEIGHT = new Dp(UIUtil.MEDIUM_WIDTH_DP);
        private static final Dp SCREN_DP_DEFAULT_HEIGHT = new Dp(670);
        private static final Dp SCREN_DP_SMALL_LAND_SINGLE_LINE_HEIGHT = new Dp(300);

        public Companion() {
        }

        public final Dp getSCREN_DP_DEFAULT_HEIGHT() {
            return SCREN_DP_DEFAULT_HEIGHT;
        }

        public final Dp getSCREN_DP_MINI_WIDTH() {
            return SCREN_DP_MINI_WIDTH;
        }

        public final Dp getSCREN_DP_SMALL_LAND_SINGLE_LINE_HEIGHT() {
            return SCREN_DP_SMALL_LAND_SINGLE_LINE_HEIGHT;
        }

        public final Dp getSCREN_DP_SPLIT_HEIGHT() {
            return SCREN_DP_SPLIT_HEIGHT;
        }
    }

    public enum PanelStatusTypeEnum {
        INIT,
        NORMAL,
        SMALL_LAND,
        SPLIT_SCREEN,
        MINI,
        TINY
    }

    void initMINIContentView(Configuration configuration, PanelStatusTypeEnum panelStatusTypeEnum);

    void initNormalContentView(Configuration configuration, PanelStatusTypeEnum panelStatusTypeEnum);

    void initSmallLandContentView(Configuration configuration, PanelStatusTypeEnum panelStatusTypeEnum);

    void initSplitScreenContentView(Configuration configuration, PanelStatusTypeEnum panelStatusTypeEnum);

    void initTinyContentView(Configuration configuration, PanelStatusTypeEnum panelStatusTypeEnum);

    void updateMINIContentView(Configuration configuration);

    void updateNormalContentView(Configuration configuration);

    void updateSmallLandContentView(Configuration configuration);

    void updateSplitScreenContentView(Configuration configuration);

    void updateTinyContentView(Configuration configuration);
}
