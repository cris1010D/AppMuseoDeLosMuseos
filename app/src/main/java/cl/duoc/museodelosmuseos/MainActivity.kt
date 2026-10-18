package cl.duoc.museodelosmuseos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cl.duoc.museodelosmuseos.navigation.AppNavHost
import cl.duoc.museodelosmuseos.ui.theme.AppMuseoDeLosMuseosTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            AppMuseoDeLosMuseosTheme {
                AppNavHost()
            }
        }
    }
}
