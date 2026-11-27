package pt.tiagodiasduarte.cabitools.sdk.domain.repository

import pt.tiagodiasduarte.cabitools.sdk.domain.model.AppInfo

public interface AppInfoRepository {
    public fun getAppInfo(): AppInfo
}
