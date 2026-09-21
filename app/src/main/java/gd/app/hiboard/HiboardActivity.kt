package gd.app.hiboard

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import gd.app.hiboard.host.HostEvent
import gd.app.hiboard.ui.HiboardView
import gd.app.hiboard.ui.HiboardViewModel
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch

class HiboardActivity : AppCompatActivity() {
    private val viewModel: HiboardViewModel by viewModels { HiboardViewModel.factory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = HiboardView(this)
        view.bind(viewModel, this)
        setContentView(view)
        applyDeeplink(intent)
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (viewModel.state.value.showStore) {
                        viewModel.closeStore()
                    } else {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                        isEnabled = true
                    }
                }
            },
        )
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.onHostEvent(HostEvent.Enter)
                try {
                    awaitCancellation()
                } finally {
                    viewModel.onHostEvent(HostEvent.Exit)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        applyDeeplink(intent)
    }

    override fun onPause() {
        viewModel.onHostEvent(HostEvent.Pause)
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        viewModel.onHostEvent(HostEvent.Resume)
    }

    override fun onDestroy() {
        viewModel.onHostEvent(HostEvent.Destroy)
        super.onDestroy()
    }

    private fun applyDeeplink(intent: Intent?) {
        val data = intent?.data ?: return
        val card = data.getQueryParameter("card") ?: data.getQueryParameter("id")
        val host = data.host.orEmpty()
        viewModel.applyDeeplink(
            cardId = card ?: host.takeIf { it in setOf("advice", "weather", "notes", "infoflow", "recent", "flashlight", "storage", "recorder") },
            edit = host == "edit" || data.getBooleanQueryParameter("edit", false),
            store = host == "store" || host == "subscribe",
        )
    }
}
