package gd.app.hiboard.engine

import gd.app.hiboard.model.RecorderUiState
import org.junit.Assert.assertEquals
import org.junit.Test

class RecorderTest {
    @Test
    fun formatsMinutesAndSeconds() {
        assertEquals("00:00", formatRecorderTime(0L))
        assertEquals("00:07", formatRecorderTime(7_000L))
        assertEquals("01:05", formatRecorderTime(65_000L))
        assertEquals("1:00:00", formatRecorderTime(3_600_000L))
    }

    @Test
    fun mapsPrimaryButtonToDreamRecorderActions() {
        assertEquals(RecorderCommand.Start, recorderPrimaryCommand(RecorderUiState.Idle))
        assertEquals(RecorderCommand.Pause, recorderPrimaryCommand(RecorderUiState.Recording))
        assertEquals(RecorderCommand.Resume, recorderPrimaryCommand(RecorderUiState.Paused))
    }

    @Test
    fun parsesServiceStateNames() {
        assertEquals(RecorderUiState.Recording, recorderUiStateFrom("RECORDING"))
        assertEquals(RecorderUiState.Paused, recorderUiStateFrom("PAUSED"))
        assertEquals(RecorderUiState.Idle, recorderUiStateFrom("IDLE"))
        assertEquals(RecorderUiState.Idle, recorderUiStateFrom(null))
    }

    @Test
    fun markAndSaveResultsCarryOppoPayload() {
        val marked = RecorderSendResult.Marked("Mark 1", 7_000L)
        assertEquals("Mark 1", marked.text)
        assertEquals(7_000L, marked.timeMs)
        assertEquals(RecorderSendResult.Saved, RecorderSendResult.Saved)
    }
}
