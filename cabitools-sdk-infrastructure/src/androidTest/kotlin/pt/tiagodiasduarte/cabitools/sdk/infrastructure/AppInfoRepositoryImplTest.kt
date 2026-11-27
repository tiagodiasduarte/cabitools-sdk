package pt.tiagodiasduarte.cabitools.sdk.infrastructure

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppInfoRepositoryImplTest {

    @Test
    fun givenTheRunningPackage_whenAppInfoIsRead_thenItReturnsItsIdAndVersionCode() {
        // Given
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        // When
        val info = AppInfoRepositoryImpl(context).getAppInfo()

        // Then
        assertEquals(context.packageName, info.applicationId)
        assertTrue("version code should be non-negative", info.versionCode >= 0)
    }
}
