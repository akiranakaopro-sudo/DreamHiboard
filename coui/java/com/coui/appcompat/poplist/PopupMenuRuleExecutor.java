package com.coui.appcompat.poplist;

import android.graphics.Rect;
import android.util.Log;
import android.view.View;

import com.coui.appcompat.log.COUILog;

final class PopupMenuRuleExecutor {
    private static final String TAG = "PopupMenuRuleExecutor";
    private StringBuilder mConfigRulesRecord;

    private void ensureBarrierLegal(Rect rect) {
        if (rect.left < 0) {
            Log.e(TAG, "barrier left < 0 !!");
            rect.left = 0;
        }
        if (rect.top < 0) {
            Log.e(TAG, "barrier top < 0 !!");
            rect.top = 0;
        }
        if (rect.right < 0) {
            Log.e(TAG, "barrier right < 0 !!");
            rect.right = 0;
        }
        if (rect.bottom < 0) {
            Log.e(TAG, "barrier bottom < 0 !!");
            rect.bottom = 0;
        }
    }

    private Rect getOutsetsRect(Rect rect, Rect outsets) {
        return new Rect(rect.left - outsets.left,
                rect.top - outsets.top,
                rect.right + outsets.right,
                rect.bottom + outsets.bottom);
    }

    private void recordBarrierRules(PopupMenuConfigRule rule, PopupMenuDomain domain) {
        Rect barrier = getOutsetsRect(rule.getDisplayFrame(), rule.getOutsets());
        switch (rule.getBarrierDirection()) {
            case PopupMenuConfigRule.BARRIER_GONE:
                mConfigRulesRecord.append("#BARRIER_GONE:");
                break;
            case PopupMenuConfigRule.BARRIER_FROM_LEFT:
                mConfigRulesRecord.append("#BARRIER_FROM_LEFT:");
                break;
            case PopupMenuConfigRule.BARRIER_FROM_TOP:
                mConfigRulesRecord.append("#BARRIER_FROM_TOP:");
                break;
            case PopupMenuConfigRule.BARRIER_FROM_RIGHT:
                mConfigRulesRecord.append("#BARRIER_FROM_RIGHT:");
                break;
            case PopupMenuConfigRule.BARRIER_FROM_BOTTOM:
                mConfigRulesRecord.append("#BARRIER_FROM_BOTTOM:");
                break;
            case PopupMenuConfigRule.BARRIER_WINDOW:
                mConfigRulesRecord.append("#BARRIER_WINDOW:");
                break;
            default:
                break;
        }
        mConfigRulesRecord.append("old domain window barrier:");
        mConfigRulesRecord.append(domain.mWindowBarriers);
        mConfigRulesRecord.append(" barrier:");
        mConfigRulesRecord.append(barrier);
        mConfigRulesRecord.append(" domain window:");
        mConfigRulesRecord.append(domain.mWindow);
        mConfigRulesRecord.append(" rule: ");
        mConfigRulesRecord.append(rule);
        if (rule instanceof View) {
            mConfigRulesRecord.append(" parent: ");
            mConfigRulesRecord.append(((View) rule).getParent());
        }
        mConfigRulesRecord.append("\n");
    }

    private void recordConfigRules(PopupMenuConfigRule rule, PopupMenuDomain domain) {
        if (mConfigRulesRecord == null) {
            mConfigRulesRecord = new StringBuilder();
        }
        int type = rule.getType();
        if (type == PopupMenuConfigRule.TYPE_WINDOW) {
            mConfigRulesRecord.append("#TYPE_WINDOW: display frame: ");
            mConfigRulesRecord.append(rule.getDisplayFrame());
        } else if (type == PopupMenuConfigRule.TYPE_ANCHOR) {
            mConfigRulesRecord.append("#TYPE_ANCHOR: display frame: ");
            mConfigRulesRecord.append(rule.getDisplayFrame());
            mConfigRulesRecord.append(" outsets: ");
            mConfigRulesRecord.append(rule.getOutsets());
        } else if (type == PopupMenuConfigRule.TYPE_BARRIER) {
            recordBarrierRules(rule, domain);
            return;
        } else if (type == PopupMenuConfigRule.TYPE_SUBMENU_ANCHOR) {
            mConfigRulesRecord.append("#TYPE_SUBMENU_ANCHOR: display frame: ");
            mConfigRulesRecord.append(rule.getDisplayFrame());
        } else {
            return;
        }
        mConfigRulesRecord.append(" rule: ");
        mConfigRulesRecord.append(rule);
        if (rule instanceof View) {
            mConfigRulesRecord.append(" parent: ");
            mConfigRulesRecord.append(((View) rule).getParent());
        }
        mConfigRulesRecord.append("\n");
    }

    public void beginConfigRulesRecord() {
        mConfigRulesRecord = new StringBuilder();
    }

    public void endConfigRulesRecord() {
        if (mConfigRulesRecord != null) {
            COUILog.i(TAG, mConfigRulesRecord.toString());
        } else {
            COUILog.e(TAG, "No config rules record! Not initialized!");
        }
    }

    public PopupMenuRuleExecutor execute(PopupMenuRule rule, PopupMenuDomain domain) {
        if (rule instanceof PopupMenuControlRule) {
            ((PopupMenuControlRule) rule).operation(domain);
        } else if (rule instanceof PopupMenuConfigRule) {
            PopupMenuConfigRule configRule = (PopupMenuConfigRule) rule;
            if (!configRule.getPopupMenuRuleEnabled()) {
                COUILog.i(TAG, "Skip disabled rule " + configRule);
                return this;
            }
            recordConfigRules(configRule, domain);
            int type = configRule.getType();
            if (type == PopupMenuConfigRule.TYPE_WINDOW) {
                domain.mWindow.set(configRule.getDisplayFrame());
            } else if (type == PopupMenuConfigRule.TYPE_ANCHOR) {
                domain.mAnchor.set(configRule.getDisplayFrame());
                domain.mAnchorOutsets.set(configRule.getOutsets());
            } else if (type == PopupMenuConfigRule.TYPE_BARRIER) {
                Rect barrier = getOutsetsRect(configRule.getDisplayFrame(), configRule.getOutsets());
                ensureBarrierLegal(barrier);
                int direction = configRule.getBarrierDirection();
                if (direction == PopupMenuConfigRule.BARRIER_FROM_LEFT) {
                    domain.mWindowBarriers.left = Math.max(domain.mWindowBarriers.left, barrier.right - domain.mWindow.left);
                } else if (direction == PopupMenuConfigRule.BARRIER_FROM_TOP) {
                    domain.mWindowBarriers.top = Math.max(domain.mWindowBarriers.top, barrier.bottom - domain.mWindow.top);
                } else if (direction == PopupMenuConfigRule.BARRIER_FROM_RIGHT) {
                    domain.mWindowBarriers.right = Math.max(domain.mWindowBarriers.right, domain.mWindow.right - barrier.left);
                } else if (direction == PopupMenuConfigRule.BARRIER_FROM_BOTTOM) {
                    domain.mWindowBarriers.bottom = Math.max(domain.mWindowBarriers.bottom, domain.mWindow.bottom - barrier.top);
                } else if (direction == PopupMenuConfigRule.BARRIER_WINDOW) {
                    domain.mWindowBarriers.left = Math.max(domain.mWindowBarriers.left, barrier.left - domain.mWindow.left);
                    domain.mWindowBarriers.top = Math.max(domain.mWindowBarriers.top, barrier.top - domain.mWindow.top);
                    domain.mWindowBarriers.right = Math.max(domain.mWindowBarriers.right, domain.mWindow.right - barrier.right);
                    domain.mWindowBarriers.bottom = Math.max(domain.mWindowBarriers.bottom, domain.mWindow.bottom - barrier.bottom);
                }
            } else if (type == PopupMenuConfigRule.TYPE_SUBMENU_ANCHOR) {
                domain.mSubMenuAnchor.set(configRule.getDisplayFrame());
            }
        }
        return this;
    }
}
