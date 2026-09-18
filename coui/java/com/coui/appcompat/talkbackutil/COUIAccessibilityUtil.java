package com.coui.appcompat.talkbackutil;

import android.content.ComponentName;
import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityManager;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Talkback / touch-exploration helpers.
 * Replaces the former {@code AccessibilityUtils} / touchsearchview duplicates.
 */
public final class COUIAccessibilityUtil {
    public static final char ENABLED_ACCESSIBILITY_SERVICES_SEPARATOR = ':';
    private static final String TALKBACK_PACKAGE = "com.google.android.marvin.talkback";
    private static final TextUtils.SimpleStringSplitter sStringColonSplitter =
            new TextUtils.SimpleStringSplitter(ENABLED_ACCESSIBILITY_SERVICES_SEPARATOR);

    private COUIAccessibilityUtil() {
    }

    public static Set<ComponentName> getEnabledServicesFromSettings(Context context) {
        String enabledServices = Settings.Secure.getString(context.getContentResolver(),
                "enabled_accessibility_services");
        if (enabledServices == null) {
            return Collections.emptySet();
        }
        HashSet<ComponentName> enabledServiceSet = new HashSet<>();
        TextUtils.SimpleStringSplitter splitter = sStringColonSplitter;
        splitter.setString(enabledServices);
        while (splitter.hasNext()) {
            ComponentName enabledService = ComponentName.unflattenFromString(splitter.next());
            if (enabledService != null) {
                enabledServiceSet.add(enabledService);
            }
        }
        return enabledServiceSet;
    }

    private static boolean isAccessibilityEnabled(Context context) {
        AccessibilityManager accessibilityManager =
                (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        return accessibilityManager != null
                && accessibilityManager.isEnabled()
                && accessibilityManager.isTouchExplorationEnabled();
    }

    public static boolean isTalkbackEnabled(Context context) {
        return isTalkbackServiceRunning(context) && isAccessibilityEnabled(context);
    }

    private static boolean isTalkbackServiceRunning(Context context) {
        Set<ComponentName> enabledServices = getEnabledServicesFromSettings(context);
        if (enabledServices == null || enabledServices.isEmpty()) {
            return false;
        }
        for (ComponentName enabledService : enabledServices) {
            if (TextUtils.equals(enabledService.getPackageName(), TALKBACK_PACKAGE)) {
                return true;
            }
        }
        return false;
    }
}
