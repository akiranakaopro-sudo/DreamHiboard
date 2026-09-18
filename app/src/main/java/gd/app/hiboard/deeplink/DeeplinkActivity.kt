package gd.app.hiboard.deeplink

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import gd.app.hiboard.HiboardActivity

/**
 * ColorOS uses `assistantscreen://`. We accept that scheme and forward into Hiboard.
 */
class DeeplinkActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val incoming = intent?.data
        val forward = Intent(this, HiboardActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            data = incoming?.buildUpon()?.scheme("hiboard")?.build() ?: incoming
        }
        startActivity(forward)
        finish()
    }
}
