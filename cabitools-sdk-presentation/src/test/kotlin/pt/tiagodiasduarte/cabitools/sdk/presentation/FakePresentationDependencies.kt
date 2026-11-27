package pt.tiagodiasduarte.cabitools.sdk.presentation

import pt.tiagodiasduarte.cabitools.sdk.domain.model.AppInfo
import pt.tiagodiasduarte.cabitools.sdk.domain.model.DeviceInfo
import pt.tiagodiasduarte.cabitools.sdk.domain.repository.AppInfoRepository
import pt.tiagodiasduarte.cabitools.sdk.domain.repository.DeviceInfoRepository
import pt.tiagodiasduarte.cabitools.sdk.domain.repository.HttpLogsRepository
import pt.tiagodiasduarte.cabitools.sdk.domain.usecase.GetAppInfoUseCase
import pt.tiagodiasduarte.cabitools.sdk.domain.usecase.GetDeviceInfoUseCase
import pt.tiagodiasduarte.cabitools.sdk.presentation.logs.FakeHttpLogsRepository

/** Screens' dependencies backed by fixed values, for tests that render the whole navigation. */
internal class FakePresentationDependencies(
    override val httpLogsRepository: HttpLogsRepository = FakeHttpLogsRepository(),
) : PresentationDependencies {

    override val getAppInfo = GetAppInfoUseCase(
        object : AppInfoRepository {
            override fun getAppInfo() = AppInfo("com.example.host", "2.3.0", 230L)
        },
    )

    override val getDeviceInfo = GetDeviceInfoUseCase(
        object : DeviceInfoRepository {
            override fun getDeviceInfo() = DeviceInfo("samsung", "SM-S918B", "15", 35)
        },
    )
}
