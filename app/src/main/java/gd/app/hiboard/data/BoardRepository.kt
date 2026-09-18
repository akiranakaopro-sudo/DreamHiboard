package gd.app.hiboard.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import gd.app.hiboard.catalog.DefaultCatalog
import gd.app.hiboard.model.BoardSnapshot
import gd.app.hiboard.model.CardArea
import gd.app.hiboard.model.CardInstance
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.boardStore by preferencesDataStore(name = "hiboard_board")

class BoardRepository(context: Context) {
    private val dataStore = context.applicationContext.boardStore

    val snapshot: Flow<BoardSnapshot> = dataStore.data.map { prefs ->
        val subscribedRaw = prefs[KEY_SUBSCRIBED]
        val recommendedRaw = prefs[KEY_RECOMMENDED]
        val subscribedIds = subscribedRaw?.split(',')?.filter { it.isNotBlank() }
            ?: DefaultCatalog.entries.filter { it.defaultSubscribed }.map { it.id }
        val recommendedIds = recommendedRaw?.split(',')?.filter { it.isNotBlank() }
            ?: DefaultCatalog.entries.filter { !it.defaultSubscribed }.map { it.id }
        BoardSnapshot(
            subscribed = instantiate(subscribedIds, CardArea.Subscribe),
            recommended = instantiate(recommendedIds, CardArea.Recommend),
        )
    }

    suspend fun subscribe(catalogId: String) {
        dataStore.edit { prefs ->
            val current = prefs[KEY_SUBSCRIBED].toIdList().ifEmpty {
                DefaultCatalog.entries.filter { it.defaultSubscribed }.map { it.id }
            }
            if (catalogId !in current) {
                prefs[KEY_SUBSCRIBED] = (current + catalogId).joinToString(",")
            }
            val recommended = prefs[KEY_RECOMMENDED].toIdList().ifEmpty {
                DefaultCatalog.entries.filter { !it.defaultSubscribed }.map { it.id }
            }
            prefs[KEY_RECOMMENDED] = recommended.filterNot { it == catalogId }.joinToString(",")
        }
    }

    suspend fun unsubscribe(catalogId: String) {
        dataStore.edit { prefs ->
            val current = prefs[KEY_SUBSCRIBED].toIdList().ifEmpty {
                DefaultCatalog.entries.filter { it.defaultSubscribed }.map { it.id }
            }
            prefs[KEY_SUBSCRIBED] = current.filterNot { it == catalogId }.joinToString(",")
            val recommended = prefs[KEY_RECOMMENDED].toIdList().ifEmpty {
                DefaultCatalog.entries.filter { !it.defaultSubscribed }.map { it.id }
            }
            if (catalogId !in recommended && DefaultCatalog.byId(catalogId) != null) {
                prefs[KEY_RECOMMENDED] = (recommended + catalogId).joinToString(",")
            }
        }
    }

    suspend fun reorder(area: CardArea, catalogIds: List<String>) {
        dataStore.edit { prefs ->
            val key = if (area == CardArea.Subscribe) KEY_SUBSCRIBED else KEY_RECOMMENDED
            val defaults = if (area == CardArea.Subscribe) {
                DefaultCatalog.entries.filter { it.defaultSubscribed }.map { it.id }
            } else {
                DefaultCatalog.entries.filter { !it.defaultSubscribed }.map { it.id }
            }
            val current = prefs[key].toIdList().ifEmpty { defaults }
            val incoming = catalogIds.filter { it in current.toSet() }
            val rest = current.filter { it !in incoming.toSet() }
            prefs[key] = (incoming + rest).joinToString(",")
        }
    }

    suspend fun move(catalogId: String, from: CardArea, to: CardArea) {
        if (from == to) return
        if (to == CardArea.Subscribe) subscribe(catalogId) else unsubscribe(catalogId)
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
                order = index,
            )
        }
    }

    private fun String?.toIdList(): List<String> =
        this?.split(',')?.map { it.trim() }?.filter { it.isNotBlank() }.orEmpty()

    private companion object {
        val KEY_SUBSCRIBED = stringPreferencesKey("subscribed")
        val KEY_RECOMMENDED = stringPreferencesKey("recommended")
    }
}
