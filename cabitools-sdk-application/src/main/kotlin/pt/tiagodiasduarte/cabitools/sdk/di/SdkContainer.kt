package pt.tiagodiasduarte.cabitools.sdk.di

import android.content.Context
import pt.tiagodiasduarte.cabitools.sdk.infrastructure.AppInfoRepositoryImpl
import pt.tiagodiasduarte.cabitools.sdk.infrastructure.DeviceInfoRepositoryImpl
import pt.tiagodiasduarte.cabitools.sdk.infrastructure.HttpLogs
import pt.tiagodiasduarte.cabitools.sdk.presentation.PresentationDependencies
import pt.tiagodiasduarte.cabitools.sdk.domain.repository.AppInfoRepository
import pt.tiagodiasduarte.cabitools.sdk.domain.repository.DeviceInfoRepository
import pt.tiagodiasduarte.cabitools.sdk.domain.repository.HttpLogsRepository
import pt.tiagodiasduarte.cabitools.sdk.domain.usecase.GetAppInfoUseCase
import pt.tiagodiasduarte.cabitools.sdk.domain.usecase.GetDeviceInfoUseCase

/** Manual dependency wiring; no DI framework is imposed on host apps. */
internal class SdkContainer(context: Context) : PresentationDependencies {

    private val appContext: Context = context.applicationContext

    private val appInfoRepository: AppInfoRepository by lazy { AppInfoRepositoryImpl(appContext) }
    private val deviceInfoRepository: DeviceInfoRepository by lazy { DeviceInfoRepositoryImpl() }

    override val getAppInfo: GetAppInfoUseCase by lazy { GetAppInfoUseCase(appInfoRepository) }
    override val getDeviceInfo: GetDeviceInfoUseCase by lazy { GetDeviceInfoUseCase(deviceInfoRepository) }

    override val httpLogsRepository: HttpLogsRepository by lazy { HttpLogs.repository(appContext) }
}
