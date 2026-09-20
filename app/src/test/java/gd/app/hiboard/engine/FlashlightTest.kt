package gd.app.hiboard.engine

import android.hardware.camera2.CameraCharacteristics
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FlashlightTest {
    @Test
    fun prefersBackCameraWithFlash() {
        val cameras = listOf(
            TorchCamera("front", hasFlash = true, facing = CameraCharacteristics.LENS_FACING_FRONT),
            TorchCamera("back", hasFlash = true, facing = CameraCharacteristics.LENS_FACING_BACK),
        )
        assertEquals("back", pickTorchCameraId(cameras))
    }

    @Test
    fun fallsBackToAnyFlashCamera() {
        val cameras = listOf(
            TorchCamera("front", hasFlash = true, facing = CameraCharacteristics.LENS_FACING_FRONT),
            TorchCamera("wide", hasFlash = false, facing = CameraCharacteristics.LENS_FACING_BACK),
        )
        assertEquals("front", pickTorchCameraId(cameras))
    }

    @Test
    fun missingFlashReturnsNull() {
        val cameras = listOf(
            TorchCamera("back", hasFlash = false, facing = CameraCharacteristics.LENS_FACING_BACK),
        )
        assertNull(pickTorchCameraId(cameras))
        assertNull(pickTorchCameraId(emptyList()))
    }
}
