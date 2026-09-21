package gd.app.hiboard.overlay

/**
 * Shared WindowServer contract constants (launcher ↔ Quick Glance).
 */
object OverlayContract {
    const val PACKAGE = "gd.app.hiboard"
    const val ACTION_WINDOW_SERVER = "gd.app.hiboard.intent.action.WINDOW_OVERLAY"
    const val META_API_VERSION = "gd.app.hiboard.overlay.api_version"
    const val API_VERSION = 3

    const val EXTRA_LAYOUT_PARAMS = "layout_params"
    const val EXTRA_CONFIGURATION = "configuration"
    const val EXTRA_CLIENT_OPTIONS = "client_options"

    /** Bit 0 set = overlay service connected / attached. */
    const val STATUS_CONNECTED = 1

    const val FLAG_ANIMATE = 1
}
