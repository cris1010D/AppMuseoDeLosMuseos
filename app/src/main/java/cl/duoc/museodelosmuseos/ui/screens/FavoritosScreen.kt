package cl.duoc.museodelosmuseos.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.museodelosmuseos.repository.RepositoryProvider
import cl.duoc.museodelosmuseos.viewmodel.FavoritosViewModel

// Museos que el usuario guardó como favoritos (se marcan desde el Mapa)
@Composable
fun FavoritosScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val factory = remember {
        FavoritosViewModel.factory(
            RepositoryProvider.productoRepository(context),
            RepositoryProvider.localRepository(context)
        )
    }
    val vm: FavoritosViewModel = viewModel(factory = factory)
    val estado by vm.uiState.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        if (estado.cargando) CircularProgressIndicator()

        estado.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            Button(onClick = vm::cargar) { Text("Reintentar") }
        }

        when {
            !estado.haySesion && !estado.cargando ->
                Text("Inicia sesión para ver tus museos favoritos.")

            estado.museos.isEmpty() && !estado.cargando && estado.error == null ->
                Text("Aún no tienes favoritos. Márcalos desde el mapa con ☆ Favorito.")
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(estado.museos, key = { it.id }) { museo ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(museo.nombre, style = MaterialTheme.typography.titleMedium)
                        Text(museo.pais, style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(8.dp))
                        Text(museo.descripcion, style = MaterialTheme.typography.bodyMedium)
                        Row {
                            TextButton(onClick = { uriHandler.openUri(museo.urlVirtual) }) {
                                Text("Ver recorrido virtual")
                            }
                            TextButton(onClick = { vm.quitarFavorito(museo.id) }) {
                                Text("★ Quitar")
                            }
                        }
                    }
                }
            }
        }
    }
}
