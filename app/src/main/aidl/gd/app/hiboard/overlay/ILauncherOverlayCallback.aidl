package gd.app.hiboard.overlay;

/**
 * Callback from Quick Glance WindowServer to the launcher (OPPO/Heytap parity).
 */
interface ILauncherOverlayCallback {
    void overlayScrollChanged(float progress);
    void overlayStatusChanged(int status);
}
