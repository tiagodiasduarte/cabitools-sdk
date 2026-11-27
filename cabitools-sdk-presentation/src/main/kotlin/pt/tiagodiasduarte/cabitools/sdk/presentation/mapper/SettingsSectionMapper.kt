package pt.tiagodiasduarte.cabitools.sdk.presentation.mapper

import pt.tiagodiasduarte.cabitools.sdk.domain.model.AppInfo
import pt.tiagodiasduarte.cabitools.sdk.domain.model.DeviceDetails
import pt.tiagodiasduarte.cabitools.sdk.presentation.R
import pt.tiagodiasduarte.cabitools.sdk.presentation.model.CabitoolsRoute
import pt.tiagodiasduarte.cabitools.sdk.presentation.model.InfoRowUi
import pt.tiagodiasduarte.cabitools.sdk.presentation.model.NavigationRowUi
import pt.tiagodiasduarte.cabitools.sdk.presentation.model.SectionUi
import pt.tiagodiasduarte.cabitools.sdk.presentation.model.UiText

/** Maps the app and device info to the settings screen's sections. */
internal fun AppInfo.toSection() = SectionUi(
    titleRes = R.string.cabitools_section_application,
    rows = listOf(
        InfoRowUi(
            R.string.cabitools_label_version_name,
            versionName?.let { UiText.Plain(it) } ?: UiText.Resource(R.string.cabitools_value_unknown),
        ),
        InfoRowUi(R.string.cabitools_label_version_code, UiText.Plain(versionCode.toString())),
        InfoRowUi(R.string.cabitools_label_app_id, UiText.Plain(applicationId)),
    ),
)

internal fun DeviceDetails.toSection() = SectionUi(
    titleRes = R.string.cabitools_section_device,
    rows = listOf(
        InfoRowUi(R.string.cabitools_label_model, UiText.Plain(model)),
        InfoRowUi(R.string.cabitools_label_android_version, UiText.Plain(androidVersion)),
    ),
)

internal fun logsSection() = SectionUi(
    titleRes = R.string.cabitools_section_logs,
    rows = listOf(NavigationRowUi(R.string.cabitools_label_open_logs, CabitoolsRoute.Logs)),
)
