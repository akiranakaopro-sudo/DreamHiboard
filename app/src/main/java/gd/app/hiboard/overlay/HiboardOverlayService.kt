package gd.app.hiboard.overlay

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

/**
 * OPPO-style WindowServer for Quick Glance.
 * Launcher binds with [OverlayContract.ACTION_WINDOW_SERVER].
 */
class HiboardOverlayService : Service() {

    private var binder: HiboardOverlayBinder? = null

    override fun onCreate() {
        super.onCreate()
        binder = HiboardOverlayBinder(applicationContext)
        Log.i(TAG, "onCreate")
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.i(TAG, "onBind action=${intent?.action}")
        return binder?.asBinderPublic()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.i(TAG, "onUnbind")
        return true
    }

    override fun onDestroy() {
        binder?.onDestroy()
        binder = null
        super.onDestroy()
        Log.i(TAG, "onDestroy")
    }

    companion object {
        private const val TAG = "HiboardOverlayService"
    }
}
