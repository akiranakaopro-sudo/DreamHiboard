package gd.app.hiboard.host

import android.content.Context
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import gd.app.hiboard.ui.HiboardView
import gd.app.hiboard.ui.HiboardViewModel

/**
 * Launcher-injectable host. ColorOS uses `AssistantViewCtrl.create(Context)`.
 * A launcher (or test) constructs this, calls [create], then [enter]/[exit].
 */
class HiboardViewController(
    private val context: Context,
) : ViewModelStoreOwner, LifecycleOwner {

    private val store = ViewModelStore()
    private val lifecycleRegistry = LifecycleRegistry(this)
    private var hostView: HiboardView? = null
    private var viewModel: HiboardViewModel? = null

    override val viewModelStore: ViewModelStore = store
    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    init {
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
    }

    fun create(onNavigateHome: () -> Unit = {}): View {
        val vm = ViewModelProvider(this, HiboardViewModel.factory())[HiboardViewModel::class.java]
        viewModel = vm
        vm.onHostEvent(HostEvent.Create)
        val view = HiboardView(context)
        view.onNavigateHome = onNavigateHome
        view.bind(vm, this)
        hostView = view
        return view
    }

    fun enter() {
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
        viewModel?.onHostEvent(HostEvent.Enter)
        // Visibility is owned by HiboardOverlayBinder.applyProgress (park / slide).
    }

    fun resume() {
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
        viewModel?.onHostEvent(HostEvent.Resume)
    }

    fun pause() {
        viewModel?.onHostEvent(HostEvent.Pause)
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
    }

    fun exit() {
        viewModel?.onHostEvent(HostEvent.Exit)
        hostView?.visibility = View.INVISIBLE
    }

    fun destroy() {
        viewModel?.onHostEvent(HostEvent.Destroy)
        hostView = null
        store.clear()
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
    }

    fun onBackPressed(): Boolean = hostView?.onBackPressed() == true
}
