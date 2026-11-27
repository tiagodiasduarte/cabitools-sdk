package pt.tiagodiasduarte.cabitools.sdk.di

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith
import pt.tiagodiasduarte.cabitools.sdk.infrastructure.HttpLogs

@RunWith(AndroidJUnit4::class)
class SdkContainerTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun `given the container when the app info use case runs then it reads the running package`() {
        // Given
        val container = SdkContainer(context)

        // When
        val info = container.getAppInfo()

        // Then
        assertEquals(context.packageName, info.applicationId)
    }

    @Test
    fun `given the container when the device info use case runs then it reads the device`() {
        // Given
        val container = SdkContainer(context)

        // When
        val details = container.getDeviceInfo()

        // Then
        assertEquals(true, details.androidVersion.contains("API"))
    }

    @Test
    fun `given the container when the logs repository is read then it is the shared store`() = runTest {
        // Given
        val container = SdkContainer(context)

        // When
        val logs = container.httpLogsRepository.observeLogs().first()

        // Then
        assertEquals(HttpLogs.repository(context).observeLogs().first(), logs)
        assertSame(container.httpLogsRepository, container.httpLogsRepository)
    }
}
