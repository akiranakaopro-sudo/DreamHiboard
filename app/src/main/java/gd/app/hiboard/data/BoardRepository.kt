package gd.app.hiboard.data

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import gd.app.hiboard.catalog.DefaultCatalog
import gd.app.hiboard.model.BoardSnapshot
import gd.app.hiboard.model.CardArea
import gd.app.hiboard.model.CardInstance
import gd.app.hiboard.ui.grid.closeHalfRowGaps
import gd.app.hiboard.ui.grid.insertFillingEmptyTwoByTwo
import gd.app.hiboard.ui.grid.pinLockedCards
import gd.app.hiboard.ui.grid.unionCards
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.boardStore by preferencesDataStore(name = "hiboard_board")

class BoardRepository(context: Context) {
    private val dataStore = context.applicationContext.boardStore

    val snapshot: Flow<BoardSnapshot> = dataStore.data.map { prefs ->
        val subscribedIds = DefaultCatalog.pinLocked(prefs.subscribedIds())
        val recommendedIds = prefs.recommendedIds()
        BoardSnapshot(
            subscribed = instantiate(subscribedIds, CardArea.Subscribe),
            recommended = instantiate(recommendedIds.filter { it !in DefaultCatalog.lockedIds() }, CardArea.Recommend),
        )
    }

    /**
     * [keepIds] are the widgets currently on screen. They are merged with the
     * saved list so an add cannot replace the board with a stale save and drop
     * a widget that only one of the two lists still has.
     */
    suspend fun subscribe(catalogId: String, keepIds: List<String> = emptyList()) {
        if (DefaultCatalog.byId(catalogId)?.locked == true) return
        dataStore.edit { prefs ->
            val saved = instantiate(prefs.subscribedIds(), CardArea.Subscribe)
            val shown = instantiate(keepIds, CardArea.Subscribe)
            val merged = unionCards(shown, saved)
            val incoming = instantiate(listOf(catalogId), CardArea.Subscribe).singleOrNull()
            val next = if (incoming != null && merged.none { it.catalogId == catalogId }) {
                pinLockedCards(insertFillingEmptyTwoByTwo(merged, incoming)).map { it.catalogId }
            } else {
                pinLockedCards(merged).map { it.catalogId }
            }
            prefs[KEY_SUBSCRIBED] = next.joinToString(",")
            val onBoard = next.toSet()
            prefs[KEY_RECOMMENDED] = prefs.recommendedIds().filterNot { it in onBoard }.joinToString(",")
            prefs.markBoardStored()
        }
    }

    suspend fun unsubscribe(catalogId: String, keepIds: List<String> = emptyList()) {
        if (DefaultCatalog.byId(catalogId)?.locked == true) return
        dataStore.edit { prefs ->
            val saved = instantiate(prefs.subscribedIds(), CardArea.Subscribe)
            val shown = instantiate(keepIds, CardArea.Subscribe)
            val current = unionCards(shown, saved)
                .filterNot { it.catalogId == catalogId }
                .map { it.catalogId }
            prefs[KEY_SUBSCRIBED] = packedSubscribedIds(current).joinToString(",")
            val recommended = prefs.recommendedIds()
            if (catalogId !in recommended && DefaultCatalog.byId(catalogId) != null) {
                prefs[KEY_RECOMMENDED] = (recommended + catalogId).joinToString(",")
            } else {
                prefs[KEY_RECOMMENDED] = recommended.joinToString(",")
            }
            prefs.markBoardStored()
        }
    }

    suspend fun reorder(area: CardArea, catalogIds: List<String>, keepIds: List<String> = emptyList()) {
        dataStore.edit { prefs ->
            val current = if (area == CardArea.Subscribe) prefs.subscribedIds() else prefs.recommendedIds()
            val known = if (area == CardArea.Subscribe) (current + keepIds).distinct() else current
            val incoming = catalogIds.filter { it in known.toSet() }
            val rest = known.filter { it !in incoming.toSet() }
            val next = incoming + rest
            if (area == CardArea.Subscribe) {
                prefs[KEY_SUBSCRIBED] = DefaultCatalog.pinLocked(next).joinToString(",")
            } else {
                prefs[KEY_RECOMMENDED] = next.filter { it !in DefaultCatalog.lockedIds() }.joinToString(",")
            }
            prefs.markBoardStored()
        }
    }

    suspend fun move(catalogId: String, from: CardArea, to: CardArea) {
        if (from == to) return
        if (to == CardArea.Subscribe) subscribe(catalogId) else unsubscribe(catalogId)
    }

    /**
     * Boards saved before removal reflow still have a hole beside every
     * leftover 2x2. Close those once, then leave a later drag order alone.
     */
    suspend fun closeStoredHalfRowGaps() {
        dataStore.edit { prefs ->
            if (prefs[KEY_GAPS_CLOSED] == true) return@edit
            prefs[KEY_SUBSCRIBED] = packedSubscribedIds(prefs.subscribedIds()).joinToString(",")
            prefs[KEY_GAPS_CLOSED] = true
            prefs.markBoardStored()
        }
    }

    private fun packedSubscribedIds(ids: List<String>): List<String> {
        val cards = instantiate(ids, CardArea.Subscribe)
        return pinLockedCards(closeHalfRowGaps(cards)).map { it.catalogId }
    }

    private fun instantiate(ids: List<String>, area: CardArea): List<CardInstance> {
        return ids.mapIndexedNotNull { index, catalogId ->
            val entry = DefaultCatalog.byId(catalogId) ?: return@mapIndexedNotNull null
            CardInstance(
                instanceId = UUID.nameUUIDFromBytes("$area:$catalogId".toByteArray()).toString(),
                catalogId = entry.id,
                displayName = entry.name,
                size = entry.size,
                area = area,
                engine = entry.engine,
                canDrag = !entry.locked,
                canEdit = !entry.locked,
                order = index,
            )
        }
    }

    private fun Preferences.storedBoard(): Boolean =
        (this[KEY_LAYOUT_VERSION] ?: 0) >= BOARD_LAYOUT_VERSION

    private fun Preferences.subscribedIds(): List<String> {
        val stored = this[KEY_SUBSCRIBED].toIdList()
        return if (storedBoard() && stored.isNotEmpty()) stored else DefaultCatalog.defaultBoardIds()
    }

    private fun Preferences.recommendedIds(): List<String> {
        return if (storedBoard()) this[KEY_RECOMMENDED].toIdList() else DefaultCatalog.defaultRecommendedIds()
    }

    private fun MutablePreferences.markBoardStored() {
        this[KEY_LAYOUT_VERSION] = BOARD_LAYOUT_VERSION
    }

    private fun String?.toIdList(): List<String> =
        this?.split(',')?.map { it.trim() }?.filter { it.isNotBlank() && DefaultCatalog.byId(it) != null }.orEmpty()

    private companion object {
        const val BOARD_LAYOUT_VERSION = 2
        val KEY_SUBSCRIBED = stringPreferencesKey("subscribed")
        val KEY_RECOMMENDED = stringPreferencesKey("recommended")
        val KEY_LAYOUT_VERSION = intPreferencesKey("board_layout_version")
        val KEY_GAPS_CLOSED = booleanPreferencesKey("half_row_gaps_closed")
    }
}
