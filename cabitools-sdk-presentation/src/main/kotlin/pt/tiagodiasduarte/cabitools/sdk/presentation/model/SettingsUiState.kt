package pt.tiagodiasduarte.cabitools.sdk.presentation.model

import androidx.annotation.StringRes

internal data class SettingsUiState(
    val sections: List<SectionUi> = emptyList(),
)

/** A titled block on the settings screen. Future tools add sections here. */
internal data class SectionUi(
    @StringRes val titleRes: Int,
    val rows: List<RowUi>,
)

/** One row in a section. */
internal sealed interface RowUi {
    @get:StringRes
    val labelRes: Int
}

/** A label with its value. */
internal data class InfoRowUi(
    @StringRes override val labelRes: Int,
    val value: UiText,
) : RowUi

/** A label that opens another SDK screen when selected. */
internal data class NavigationRowUi(
    @StringRes override val labelRes: Int,
    val destination: CabitoolsRoute,
) : RowUi

/** Text that is either a literal value or a string resource resolved at render time. */
internal sealed interface UiText {
    data class Plain(val value: String) : UiText
    data class Resource(@StringRes val resId: Int) : UiText
}
