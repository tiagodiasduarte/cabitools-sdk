package pt.tiagodiasduarte.cabitools.sdk.infrastructure

import android.os.Build

/** Indirection over [android.os.Build]'s static fields so the repository can be unit tested. */
internal interface DeviceBuildSource {
    val manufacturer: String
    val model: String
    val release: String
    val sdkInt: Int
}

internal object AndroidDeviceBuildSource : DeviceBuildSource {
    override val manufacturer: String get() = Build.MANUFACTURER
    override val model: String get() = Build.MODEL
    override val release: String get() = Build.VERSION.RELEASE
    override val sdkInt: Int get() = Build.VERSION.SDK_INT
}
