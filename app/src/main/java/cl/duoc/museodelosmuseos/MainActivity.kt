package cl.duoc.museodelosmuseos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import cl.duoc.museodelosmuseos.ui.screens.CotizadorScreen
import cl.duoc.museodelosmuseos.ui.screens.EscanerScreen
import cl.duoc.museodelosmuseos.ui.screens.MapaScreen
import cl.duoc.museodelosmuseos.ui.theme.AppMuseoDeLosMuseosTheme






class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            AppMuseoDeLosMuseosTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    EscanerScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AppMuseoDeLosMuseosTheme {
        Greeting("Android")
    }
}