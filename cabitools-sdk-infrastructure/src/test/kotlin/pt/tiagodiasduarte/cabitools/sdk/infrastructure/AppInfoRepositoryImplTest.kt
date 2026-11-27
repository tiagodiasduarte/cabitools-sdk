package pt.tiagodiasduarte.cabitools.sdk.infrastructure

import android.content.Context
import android.content.pm.PackageInfo
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import pt.tiagodiasduarte.cabitools.sdk.domain.model.AppInfo

@RunWith(AndroidJUnit4::class)
class AppInfoRepositoryImplTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Before
    fun installHostPackage() {
        val packageInfo = PackageInfo().apply {
            packageName = context.packageName
            versionName = "2.3.0"
            longVersionCode = 230L
        }
        shadowOf(context.packageManager).installPackage(packageInfo)
    }

    @Test
    @Config(sdk = [33])
    fun `given an installed host package on API 33 when app info is read then it returns the package info`() {
        // Given
        val repository = AppInfoRepositoryImpl(context)

        // When
        val info = repository.getAppInfo()

        // Then
        assertEquals(expected(), info)
    }

    @Test
    @Config(sdk = [32])
    fun `given an installed host package below API 33 when app info is read then it returns the package info`() {
        // Given
        val repository = AppInfoRepositoryImpl(context)

        // When
        val info = repository.getAppInfo()

        // Then
        assertEquals(expected(), info)
    }

    private fun expected() = AppInfo(applicationId = context.packageName, versionName = "2.3.0", versionCode = 230L)
}
