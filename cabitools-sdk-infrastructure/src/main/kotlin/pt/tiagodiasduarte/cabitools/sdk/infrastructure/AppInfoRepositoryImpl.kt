package pt.tiagodiasduarte.cabitools.sdk.infrastructure

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.pm.PackageInfoCompat
import pt.tiagodiasduarte.cabitools.sdk.domain.model.AppInfo
import pt.tiagodiasduarte.cabitools.sdk.domain.repository.AppInfoRepository

/**
 * Reads the **host** app's package info. `context.packageName` is the host's application id
 * (including any variant suffix), not the SDK's namespace.
 */
public class AppInfoRepositoryImpl(
    private val context: Context,
) : AppInfoRepository {

    override fun getAppInfo(): AppInfo {
        val packageName = context.packageName
        val packageInfo = context.packageManager.packageInfoOf(packageName)
        return AppInfo(
            applicationId = packageName,
            versionName = packageInfo.versionName,
            versionCode = PackageInfoCompat.getLongVersionCode(packageInfo),
        )
    }

    private fun PackageManager.packageInfoOf(packageName: String): PackageInfo =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            getPackageInfo(packageName, 0)
        }
}
