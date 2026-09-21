package gd.app.hiboard.overlay;

import android.os.Bundle;
import gd.app.hiboard.overlay.ILauncherOverlayCallback;

/**
 * Remote overlay contract matching OPPO/Heytap ILauncherOverlay (core methods).
 * Launcher binds HiboardOverlayService and drives scroll / lifecycle over Binder.
 * Uses windowAttached2 (API ≥ 3) with LayoutParams inside the Bundle.
 *
 * Scroll methods are oneway so the launcher UI thread is not blocked per finger frame.
 */
interface ILauncherOverlay {
    oneway void startScroll();
    oneway void onScroll(float progress);
    oneway void endScroll();
    void windowDetached(boolean isChangingConfigurations);
    void closeOverlay(int flags);
    void onStart();
    void onPause();
    void onResume();
    void onStop();
    void onDestroy();
    void openOverlay(int flags);
    void windowAttached2(in Bundle bundle, ILauncherOverlayCallback callback);
    oneway void endScrollWithVelocity(float velocity);
    boolean hasOverlayContent();
}
