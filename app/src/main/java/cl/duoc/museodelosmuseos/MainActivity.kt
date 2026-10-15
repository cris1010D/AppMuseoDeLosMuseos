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
import cl.duoc.museodelosmuseos.ui.theme.AppMuseoDeLosMuseosTheme



import android.util.Log
import androidx.lifecycle.lifecycleScope
import cl.duoc.museodelosmuseos.repository.LocalRepository
import cl.duoc.museodelosmuseos.repository.local.AppDatabase
import cl.duoc.museodelosmuseos.repository.local.SessionManager
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ---- PRUEBA TEMPORAL DE ROOM (borrar después) ----
        val repo = LocalRepository(
            AppDatabase.getInstance(this),
            SessionManager.getInstance(this)
        )
        lifecycleScope.launch {
            val id = repo.ingresar("Cristóbal", "cristobal@duocuc.cl")
            repo.alternarFavorito("museo_prado")
            val primera = repo.registrarLecturaQr("PROD-001", "PRODUCTO")
            val segunda = repo.registrarLecturaQr("PROD-001", "PRODUCTO")
            Log.d("PruebaRoom", "Usuario=$id | 1ra lectura=$primera | 2da lectura=$segunda")
        }
        // ---- FIN PRUEBA ----
        enableEdgeToEdge()
        setContent {
            AppMuseoDeLosMuseosTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
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