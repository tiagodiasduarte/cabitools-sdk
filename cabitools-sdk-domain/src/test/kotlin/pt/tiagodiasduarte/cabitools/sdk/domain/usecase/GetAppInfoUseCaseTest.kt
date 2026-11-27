package pt.tiagodiasduarte.cabitools.sdk.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import pt.tiagodiasduarte.cabitools.sdk.domain.model.AppInfo
import pt.tiagodiasduarte.cabitools.sdk.domain.repository.AppInfoRepository

class GetAppInfoUseCaseTest {

    private fun useCase(info: AppInfo) = GetAppInfoUseCase(
        object : AppInfoRepository {
            override fun getAppInfo() = info
        },
    )

    @Test
    fun `given host app info in the repository when the use case runs then it returns that info`() {
        // Given
        val info = AppInfo("com.example.host", "2.3.0", 230L)

        // When
        val result = useCase(info)()

        // Then
        assertEquals(info, result)
    }

    @Test
    fun `given a missing version name when the use case runs then the version name stays null`() {
        // Given
        val info = AppInfo("com.example.host", null, 1L)

        // When
        val result = useCase(info)()

        // Then
        assertNull(result.versionName)
    }

    @Test
    fun `given a version code larger than 32 bits when the use case runs then it keeps the full value`() {
        // Given
        val info = AppInfo("com.example.host.debug", "1.0", 3_000_000_000L)

        // When
        val result = useCase(info)()

        // Then
        assertEquals(3_000_000_000L, result.versionCode)
    }
}
