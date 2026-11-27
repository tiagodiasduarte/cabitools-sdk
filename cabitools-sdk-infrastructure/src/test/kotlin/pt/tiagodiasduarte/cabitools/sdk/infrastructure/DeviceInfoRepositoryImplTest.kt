package pt.tiagodiasduarte.cabitools.sdk.infrastructure

import org.junit.Assert.assertEquals
import org.junit.Test
import pt.tiagodiasduarte.cabitools.sdk.domain.model.DeviceInfo

class DeviceInfoRepositoryImplTest {

    @Test
    fun `given build source values when device info is read then it maps them into device info`() {
        // Given
        val source = object : DeviceBuildSource {
            override val manufacturer = "samsung"
            override val model = "SM-S918B"
            override val release = "14"
            override val sdkInt = 34
        }

        // When
        val info = DeviceInfoRepositoryImpl(source).getDeviceInfo()

        // Then
        assertEquals(DeviceInfo("samsung", "SM-S918B", "14", 34), info)
    }
}
