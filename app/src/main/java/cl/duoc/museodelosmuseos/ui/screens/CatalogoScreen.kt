package cl.duoc.museodelosmuseos.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.museodelosmuseos.repository.RepositoryProvider
import cl.duoc.museodelosmuseos.viewmodel.CatalogoViewModel

internal fun iconoCategoria(categoria: String) = when (categoria) {
    "Libro" -> "📚"
    "Merch" -> "🛍️"
    "Curso" -> "🎓"
    else -> "📦"
}

internal fun precioUsd(valor: Double) = "USD %.2f".format(valor)

// Catálogo de libros, merchandising y cursos en cuadrícula
@Composable
fun CatalogoScreen(onProductoClick: (String) -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val repository = remember { RepositoryProvider.productoRepository(context) }
    val vm: CatalogoViewModel = viewModel(factory = CatalogoViewModel.factory(repository))
    val estado by vm.uiState.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize().padding(horizontal = 16.dp)) {

        // Filtros por categoría
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = estado.categoriaSeleccionada == null,
                onClick = { vm.filtrarPorCategoria(null) },
                label = { Text("Todos") }
            )
            estado.categorias.forEach { categoria ->
                FilterChip(
                    selected = estado.categoriaSeleccionada == categoria,
                    onClick = { vm.filtrarPorCategoria(categoria) },
                    label = { Text(categoria) }
                )
            }
        }

        if (estado.cargando) {
            CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally).padding(16.dp))
        }

        estado.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            Button(onClick = vm::cargar) { Text("Reintentar") }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(estado.visibles, key = { it.id }) { producto ->
                Card(modifier = Modifier.fillMaxWidth().clickable { onProductoClick(producto.id) }) {
                    Column(Modifier.padding(12.dp)) {
                        Text(iconoCategoria(producto.categoria), style = MaterialTheme.typography.headlineMedium)
                        Spacer(Modifier.height(4.dp))
                        Text(producto.titulo, style = MaterialTheme.typography.titleSmall, minLines = 2, maxLines = 2)
                        Text(producto.categoria, style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(4.dp))
                        Text(precioUsd(producto.precioUsd), style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}
