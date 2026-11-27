package pt.tiagodiasduarte.cabitools.sdk.domain.usecase

import pt.tiagodiasduarte.cabitools.sdk.domain.model.AppInfo
import pt.tiagodiasduarte.cabitools.sdk.domain.repository.AppInfoRepository

public class GetAppInfoUseCase(
    private val repository: AppInfoRepository,
) {
    public operator fun invoke(): AppInfo = repository.getAppInfo()
}
