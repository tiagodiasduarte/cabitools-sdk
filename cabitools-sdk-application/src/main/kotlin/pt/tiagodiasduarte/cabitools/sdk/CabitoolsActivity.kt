package pt.tiagodiasduarte.cabitools.sdk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import pt.tiagodiasduarte.cabitools.sdk.di.SdkContainer
import pt.tiagodiasduarte.cabitools.sdk.presentation.CabitoolsNavHost
import pt.tiagodiasduarte.cabitools.sdk.presentation.theme.CabitoolsTheme

/** Hosts the Cabitools screens. Not exported: only reachable through `Cabitools.open`. */
internal class CabitoolsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val container = SdkContainer(applicationContext)
        setContent {
            CabitoolsTheme {
                CabitoolsNavHost(container)
            }
        }
    }
}
