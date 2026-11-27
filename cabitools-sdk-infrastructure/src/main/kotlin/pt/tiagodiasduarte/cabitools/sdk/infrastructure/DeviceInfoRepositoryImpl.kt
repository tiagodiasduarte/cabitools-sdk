package pt.tiagodiasduarte.cabitools.sdk.infrastructure

import pt.tiagodiasduarte.cabitools.sdk.domain.model.DeviceInfo
import pt.tiagodiasduarte.cabitools.sdk.domain.repository.DeviceInfoRepository

public class DeviceInfoRepositoryImpl internal constructor(
    private val source: DeviceBuildSource,
) : DeviceInfoRepository {

    /** Reads the running device through `android.os.Build`. */
    public constructor() : this(AndroidDeviceBuildSource)

    override fun getDeviceInfo(): DeviceInfo = DeviceInfo(
        manufacturer = source.manufacturer,
        model = source.model,
        androidRelease = source.release,
        apiLevel = source.sdkInt,
    )
}
