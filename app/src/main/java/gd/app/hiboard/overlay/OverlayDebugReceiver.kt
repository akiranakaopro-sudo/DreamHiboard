package gd.app.hiboard.overlay

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Debug helper: adb shell am broadcast -a gd.app.hiboard.action.DEBUG_OPEN_OVERLAY -p gd.app.hiboard
 */
class OverlayDebugReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        Log.i(TAG, "onReceive $action")
        when (action) {
            ACTION_OPEN -> HiboardOverlayBinder.active?.openOverlay(0)
            ACTION_CLOSE -> HiboardOverlayBinder.active?.closeOverlay(0)
        }
    }

    companion object {
        private const val TAG = "OverlayDebugReceiver"
        const val ACTION_OPEN = "gd.app.hiboard.action.DEBUG_OPEN_OVERLAY"
        const val ACTION_CLOSE = "gd.app.hiboard.action.DEBUG_CLOSE_OVERLAY"
    }
}
