package nl.koenhabets.yahtzeescore

import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*

import org.junit.Before
import org.junit.Test

class AppUpdatesTest {
    lateinit var appUpdates: AppUpdates

    @Before
    fun setUp() {
        appUpdates = AppUpdates(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun getInfo() {
        assertNotNull(appUpdates.getInfo())
    }
}