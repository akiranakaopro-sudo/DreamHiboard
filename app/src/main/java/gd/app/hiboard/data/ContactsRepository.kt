package gd.app.hiboard.data

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import gd.app.hiboard.engine.CONTACT_LIMIT
import gd.app.hiboard.engine.RankedContact
import gd.app.hiboard.engine.contactGivenName
import gd.app.hiboard.engine.rankContacts
import gd.app.hiboard.model.BoardContact

data class ContactsLoad(
    val permitted: Boolean,
    val people: List<BoardContact>,
)

class ContactsRepository(context: Context) {
    private val appContext = context.applicationContext

    fun load(): ContactsLoad {
        if (ContextCompat.checkSelfPermission(appContext, Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return ContactsLoad(permitted = false, people = emptyList())
        }
        return try {
            ContactsLoad(permitted = true, people = query())
        } catch (_: SecurityException) {
            ContactsLoad(permitted = false, people = emptyList())
        }
    }

    fun open(lookupUri: String): Intent? {
        if (lookupUri.isBlank()) return null
        return Intent(Intent.ACTION_VIEW, Uri.parse(lookupUri))
    }

    fun openApp(): Intent {
        val contacts = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_CONTACTS)
        if (appContext.packageManager.resolveActivity(contacts, PackageManager.MATCH_DEFAULT_ONLY) != null) {
            return contacts
        }
        return Intent(Intent.ACTION_VIEW, ContactsContract.Contacts.CONTENT_URI)
    }

    private fun query(): List<BoardContact> {
        val rows = try {
            readRows(includeTimes = true)
        } catch (_: IllegalArgumentException) {
            readRows(includeTimes = false)
        }
        return rankContacts(rows, CONTACT_LIMIT).map { person ->
            BoardContact(person.name, person.lookupUri, person.photoUri)
        }
    }

    private fun readRows(includeTimes: Boolean): List<RankedContact> {
        val projection = buildList {
            add(ContactsContract.Contacts._ID)
            add(ContactsContract.Contacts.LOOKUP_KEY)
            add(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
            add(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI)
            add(ContactsContract.Contacts.STARRED)
            if (includeTimes) add(ContactsContract.Contacts.TIMES_CONTACTED)
        }.toTypedArray()
        val sort = if (includeTimes) {
            "${ContactsContract.Contacts.STARRED} DESC, " +
                "${ContactsContract.Contacts.TIMES_CONTACTED} DESC, " +
                "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} COLLATE LOCALIZED ASC"
        } else {
            "${ContactsContract.Contacts.STARRED} DESC, " +
                "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} COLLATE LOCALIZED ASC"
        }
        val rows = ArrayList<RankedContact>()
        appContext.contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            projection,
            "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} IS NOT NULL",
            null,
            sort,
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID)
            val keyCol = cursor.getColumnIndexOrThrow(ContactsContract.Contacts.LOOKUP_KEY)
            val nameCol = cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
            val photoCol = cursor.getColumnIndexOrThrow(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI)
            val starredCol = cursor.getColumnIndexOrThrow(ContactsContract.Contacts.STARRED)
            val timesCol = if (includeTimes) {
                cursor.getColumnIndexOrThrow(ContactsContract.Contacts.TIMES_CONTACTED)
            } else {
                -1
            }
            while (cursor.moveToNext()) {
                val name = contactGivenName(cursor.getString(nameCol).orEmpty())
                val key = cursor.getString(keyCol) ?: continue
                if (name.isBlank()) continue
                val lookup = ContactsContract.Contacts.getLookupUri(cursor.getLong(idCol), key)
                    ?.toString()
                    .orEmpty()
                rows += RankedContact(
                    name = name,
                    starred = cursor.getInt(starredCol) == 1,
                    timesContacted = if (timesCol >= 0) cursor.getInt(timesCol) else 0,
                    lookupUri = lookup,
                    photoUri = cursor.getString(photoCol),
                )
            }
        }
        return rows
    }
}
