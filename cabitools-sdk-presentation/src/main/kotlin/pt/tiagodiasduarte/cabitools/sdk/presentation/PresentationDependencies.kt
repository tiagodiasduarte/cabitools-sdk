package pt.tiagodiasduarte.cabitools.sdk.presentation

import pt.tiagodiasduarte.cabitools.sdk.domain.repository.HttpLogsRepository
import pt.tiagodiasduarte.cabitools.sdk.domain.usecase.GetAppInfoUseCase
import pt.tiagodiasduarte.cabitools.sdk.domain.usecase.GetDeviceInfoUseCase

/** What the Cabitools screens need. Provided by the module that assembles the SDK. */
public interface PresentationDependencies {
    public val getAppInfo: GetAppInfoUseCase
    public val getDeviceInfo: GetDeviceInfoUseCase
    public val httpLogsRepository: HttpLogsRepository
}
