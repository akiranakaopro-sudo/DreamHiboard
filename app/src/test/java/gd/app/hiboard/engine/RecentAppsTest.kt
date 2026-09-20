package gd.app.hiboard.engine

import org.junit.Assert.assertEquals
import org.junit.Test

class RecentAppsTest {
    @Test
    fun lastOpenedAppMovesToTheFrontAndKeepsFiveSlots() {
        val defaults = listOf("dialer", "contacts", "messages", "camera", "settings")
        assertEquals(defaults, recentPackageOrder(defaults, emptyList()))
        assertEquals(
            listOf("chrome", "dialer", "contacts", "messages", "camera"),
            recentPackageOrder(defaults, emptyList(), lastOpened = "chrome"),
        )
        assertEquals(
            listOf("camera", "dialer", "contacts", "messages", "settings"),
            recentPackageOrder(defaults, listOf("camera"), lastOpened = "camera"),
        )
        assertEquals(
            listOf("chrome", "camera", "dialer", "contacts", "messages"),
            recentPackageOrder(defaults, listOf("camera", "chrome"), lastOpened = "chrome"),
        )
        assertEquals(
            listOf("messages", "phone", "camera", "settings", "browser"),
            recentPackageOrder(
                defaults,
                listOf("phone", "messages", "camera", "settings", "browser"),
                lastOpened = "messages",
            ),
        )
    }
}
