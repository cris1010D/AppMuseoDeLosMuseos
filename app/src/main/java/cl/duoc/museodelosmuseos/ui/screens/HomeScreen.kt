package cl.duoc.museodelosmuseos.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duoc.museodelosmuseos.navigation.Screen

private data class OpcionMenu(val icono: String, val titulo: String, val destino: Screen)

private val OPCIONES = listOf(
    OpcionMenu("🛍️", "Catálogo", Screen.Catalogo),
    OpcionMenu("🗺️", "Mapa de museos", Screen.Mapa),
    OpcionMenu("📷", "Escáner QR", Screen.Escaner),
    OpcionMenu("📦", "Cotizador de envíos", Screen.Cotizador),
    OpcionMenu("★", "Favoritos", Screen.Favoritos),
    OpcionMenu("👤", "Perfil y ajustes", Screen.Perfil)
)

// Menú principal de la app
@Composable
fun HomeScreen(onNavigate: (Screen) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text("Bienvenido al Museo de los Museos", style = MaterialTheme.typography.headlineSmall)
        Text("¿Qué quieres hacer hoy?", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(OPCIONES, key = { it.titulo }) { opcion ->
                Card(modifier = Modifier.fillMaxWidth().clickable { onNavigate(opcion.destino) }) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp, horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(opcion.icono, style = MaterialTheme.typography.displaySmall)
                        Spacer(Modifier.height(8.dp))
                        Text(opcion.titulo, style = MaterialTheme.typography.titleSmall)
                    }
                }
            }
        }
    }
}
