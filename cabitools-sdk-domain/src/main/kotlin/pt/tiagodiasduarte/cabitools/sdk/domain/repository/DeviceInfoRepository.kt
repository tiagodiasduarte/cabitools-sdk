package pt.tiagodiasduarte.cabitools.sdk.domain.repository

import pt.tiagodiasduarte.cabitools.sdk.domain.model.DeviceInfo

public interface DeviceInfoRepository {
    public fun getDeviceInfo(): DeviceInfo
}
