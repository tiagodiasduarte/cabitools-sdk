package pt.tiagodiasduarte.cabitools.sdk.infrastructure

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowBuild

@RunWith(AndroidJUnit4::class)
class AndroidDeviceBuildSourceTest {

    @Test
    @Config(sdk = [34])
    fun `given Build values on API 34 when the source is read then it returns those values`() {
        // Given
        ShadowBuild.setManufacturer("Google")
        ShadowBuild.setModel("Pixel 8")
        ShadowBuild.setVersionRelease("14")

        // When
        val source = AndroidDeviceBuildSource

        // Then
        assertEquals("Google", source.manufacturer)
        assertEquals("Pixel 8", source.model)
        assertEquals("14", source.release)
        assertEquals(34, source.sdkInt)
    }
}
