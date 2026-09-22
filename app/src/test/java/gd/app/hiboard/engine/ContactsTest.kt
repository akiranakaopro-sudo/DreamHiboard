package gd.app.hiboard.engine

import org.junit.Assert.assertEquals
import org.junit.Test

class ContactsTest {
    @Test
    fun starredContactsComeFirstThenFrequentThenName() {
        val ranked = rankContacts(
            listOf(
                RankedContact("Zoe", starred = false, timesContacted = 9),
                RankedContact("Layla", starred = true, timesContacted = 1),
                RankedContact("Jim", starred = true, timesContacted = 4),
                RankedContact("Carlos", starred = false, timesContacted = 3),
                RankedContact("Sophia", starred = true, timesContacted = 2),
                RankedContact("Amy", starred = false, timesContacted = 8),
            ),
        )
        assertEquals(listOf("Jim", "Sophia", "Layla", "Zoe"), ranked.map { it.name })
    }

    @Test
    fun givenNameDropsTheFamilyName() {
        assertEquals("Sophia", contactGivenName("Sophia Nguyen"))
        assertEquals("Layla", contactGivenName("  Layla  "))
        assertEquals("王伟", contactGivenName("王伟"))
    }

    @Test
    fun displayPhotoBeatsAnEmptyThumbnail() {
        assertEquals(
            "content://com.android.contacts/display_photo/1",
            contactPhotoUri("content://com.android.contacts/display_photo/1", "content://thumb"),
        )
        assertEquals("content://thumb", contactPhotoUri(null, "content://thumb"))
        assertEquals("content://thumb", contactPhotoUri("  ", "content://thumb"))
        assertEquals(null, contactPhotoUri(null, null))
    }
}
