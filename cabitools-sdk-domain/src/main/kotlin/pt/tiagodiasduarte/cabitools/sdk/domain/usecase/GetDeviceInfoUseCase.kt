package pt.tiagodiasduarte.cabitools.sdk.domain.usecase

import pt.tiagodiasduarte.cabitools.sdk.domain.model.DeviceDetails
import pt.tiagodiasduarte.cabitools.sdk.domain.mapper.toDetails
import pt.tiagodiasduarte.cabitools.sdk.domain.repository.DeviceInfoRepository

public class GetDeviceInfoUseCase(
    private val repository: DeviceInfoRepository,
) {
    public operator fun invoke(): DeviceDetails = repository.getDeviceInfo().toDetails()
}
