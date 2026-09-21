package gd.app.hiboard.engine

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import java.io.File

/**
 * Sound Recorder cannot File.delete() a MediaStore row this app inserted.
 * It calls here so we remove our own recording.
 */
class RecorderCleanupProvider : ContentProvider() {

    override fun onCreate(): Boolean = true

    override fun call(method: String, arg: String?, extras: Bundle?): Bundle? {
        if (method != METHOD_DELETE) return null
        val path = extras?.getString(EXTRA_PATH).orEmpty()
        if (path.isBlank()) return null
        val ctx = context ?: return null
        val aliases = listOf(
            path,
            path.replace("/storage/emulated/0", "/sdcard"),
            path.replace("/sdcard", "/storage/emulated/0"),
        ).distinct()
        for (alias in aliases) {
            File(alias).delete()
            deleteMediaRows(ctx.contentResolver, alias)
        }
        val gone = aliases.none { File(it).exists() }
        return Bundle().apply { putBoolean(EXTRA_OK, gone) }
    }

    private fun deleteMediaRows(resolver: android.content.ContentResolver, path: String) {
        val where = "${MediaStore.MediaColumns.DATA}=?"
        val args = arrayOf(path)
        val uris = listOf(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            MediaStore.Files.getContentUri("external"),
        )
        for (uri in uris) {
            try {
                resolver.delete(uri, where, args)
            } catch (_: Exception) {
            }
        }
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor? = null

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int = 0

    companion object {
        const val AUTHORITY = "gd.app.hiboard.recordings"
        const val METHOD_DELETE = "delete"
        const val EXTRA_PATH = "path"
        const val EXTRA_OK = "ok"
    }
}
