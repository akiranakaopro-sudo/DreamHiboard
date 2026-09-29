package gd.app.hiboard.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StorageTest {
    @Test
    fun usedIsTotalMinusFreeRam() {
        val status = storageStatusFrom(totalBytes = 8_000_000_000L, freeBytes = 3_200_000_000L)
        assertEquals(4_800_000_000L, status.usedBytes)
        assertEquals(8_000_000_000L, status.totalBytes)
        assertEquals(0.6f, status.ratio, 0.01f)
    }

    @Test
    fun clampsFreeAboveTotal() {
        val status = storageStatusFrom(totalBytes = 100L, freeBytes = 400L)
        assertEquals(0L, status.usedBytes)
        assertEquals(100L, status.totalBytes)
        assertEquals(0f, status.ratio, 0f)
    }

    @Test
    fun formatsWholeAndTenthsGigabytes() {
        assertEquals("8GB", formatStorageGb(8_000_000_000L))
        assertEquals("4.8GB", formatStorageGb(4_800_000_000L))
        assertEquals("5.1GB", formatStorageGb(5_100_000_000L))
        assertEquals("4.8GB / 8GB", formatStorageUsage(4_800_000_000L, 8_000_000_000L))
        assertEquals("—", formatStorageUsage(0L, 0L))
        assertEquals("4.4GB | 7.6GB", formatStoragePair(4_800_000_000L, 8_000_000_000L))
        assertEquals("58%", formatStoragePercent(4_800_000_000L, 8_000_000_000L))
        assertEquals("21%", formatStoragePercent(2_900_000_000L, 12_400_000_000L))
        assertEquals("2.5GB | 12GB", formatStoragePair(2_900_000_000L, 12_400_000_000L))
    }

    @Test
    fun picksFirstResolvableCandidate() {
        val picked = pickFirstResolvable(listOf("first", "second")) { it == "second" }
        assertEquals("second", picked)
        assertNull(pickFirstResolvable(listOf("first")) { false })
    }
}
