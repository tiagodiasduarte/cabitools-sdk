package pt.tiagodiasduarte.cabitools.sdk.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test
import pt.tiagodiasduarte.cabitools.sdk.domain.model.DeviceInfo
import pt.tiagodiasduarte.cabitools.sdk.domain.repository.DeviceInfoRepository

class GetDeviceInfoUseCaseTest {

    private fun useCase(info: DeviceInfo) = GetDeviceInfoUseCase(
        object : DeviceInfoRepository {
            override fun getDeviceInfo() = info
        },
    )

    private fun device(manufacturer: String, model: String, release: String = "15", api: Int = 35) =
        DeviceInfo(manufacturer, model, release, api)

    @Test
    fun `given a model without the manufacturer when the use case runs then it prefixes the capitalized maker`() {
        // Given
        val useCase = useCase(device("samsung", "SM-S918B"))

        // When
        val details = useCase()

        // Then
        assertEquals("Samsung SM-S918B", details.model)
    }

    @Test
    fun `given a model that starts with the manufacturer when the use case runs then it does not repeat it`() {
        // Given
        val useCase = useCase(device("Google", "Google Pixel 8"))

        // When
        val details = useCase()

        // Then
        assertEquals("Google Pixel 8", details.model)
    }

    @Test
    fun `given a manufacturer in a different case when the use case runs then the prefix match ignores case`() {
        // Given
        val useCase = useCase(device("google", "Google Pixel 8"))

        // When
        val details = useCase()

        // Then
        assertEquals("Google Pixel 8", details.model)
    }

    @Test
    fun `given a blank manufacturer when the use case runs then the model is used on its own`() {
        // Given
        val useCase = useCase(device("", "Pixel 8"))

        // When
        val details = useCase()

        // Then
        assertEquals("Pixel 8", details.model)
    }

    @Test
    fun `given a release and API level when the use case runs then the android version shows both`() {
        // Given
        val useCase = useCase(device("Google", "Pixel 8", release = "15", api = 35))

        // When
        val details = useCase()

        // Then
        assertEquals("15 (API 35)", details.androidVersion)
    }
}
