package gd.app.hiboard.engine

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class FlashlightToggle {
    Changed,
    Unavailable,
    NeedsCamera,
}

data class TorchCamera(
    val id: String,
    val hasFlash: Boolean,
    val facing: Int,
)

fun pickTorchCameraId(cameras: List<TorchCamera>): String? {
    return cameras.firstOrNull { it.hasFlash && it.facing == CameraCharacteristics.LENS_FACING_BACK }?.id
        ?: cameras.firstOrNull { it.hasFlash }?.id
}

class FlashlightController(context: Context) {
    private val appContext = context.applicationContext
    private val camera = appContext.getSystemService(CameraManager::class.java)
    private val cameraId: String? = camera?.let { pickTorchCameraId(discoverCameras(it)) }
    private val _on = MutableStateFlow(false)
    val on: StateFlow<Boolean> = _on
    val available: Boolean
        get() = camera != null && cameraId != null

    private val callback = object : CameraManager.TorchCallback() {
        override fun onTorchModeChanged(id: String, enabled: Boolean) {
            if (id == cameraId) _on.value = enabled
        }

        override fun onTorchModeUnavailable(id: String) {
            if (id == cameraId) _on.value = false
        }
    }

    init {
        camera?.registerTorchCallback(callback, Handler(Looper.getMainLooper()))
    }

    fun toggle(): FlashlightToggle {
        val manager = camera ?: return FlashlightToggle.Unavailable
        val id = cameraId ?: return FlashlightToggle.Unavailable
        return try {
            val next = !_on.value
            manager.setTorchMode(id, next)
            _on.value = next
            FlashlightToggle.Changed
        } catch (_: SecurityException) {
            FlashlightToggle.NeedsCamera
        } catch (_: Exception) {
            FlashlightToggle.Unavailable
        }
    }

    private fun discoverCameras(manager: CameraManager): List<TorchCamera> {
        return try {
            manager.cameraIdList.map { id ->
                val chars = manager.getCameraCharacteristics(id)
                TorchCamera(
                    id = id,
                    hasFlash = chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true,
                    facing = chars.get(CameraCharacteristics.LENS_FACING)
                        ?: CameraCharacteristics.LENS_FACING_BACK,
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}
