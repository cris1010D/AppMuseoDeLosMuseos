package cl.duoc.museodelosmuseos.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.museodelosmuseos.repository.RepositoryProvider
import cl.duoc.museodelosmuseos.viewmodel.DetalleProductoViewModel

private fun descripcionDe(categoria: String) = when (categoria) {
    "Libro" -> "Publicación impresa de nuestra colección. Ideal para profundizar en el arte y la historia de los museos de Iberoamérica."
    "Merch" -> "Artículo oficial del Museo de los Museos. Cada compra ayuda a financiar el trabajo de conservación."
    "Curso" -> "Capacitación online dictada por especialistas. Incluye material de apoyo y certificado de participación."
    else -> "Producto del Museo de los Museos."
}

@Composable
fun DetalleProductoScreen(productoId: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val repository = remember { RepositoryProvider.productoRepository(context) }
    val vm: DetalleProductoViewModel = viewModel(
        factory = DetalleProductoViewModel.factory(repository, productoId)
    )
    val estado by vm.uiState.collectAsStateWithLifecycle()
    var mensaje by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)
    ) {
        if (estado.cargando) CircularProgressIndicator()

        estado.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        estado.producto?.let { producto ->
            Text(iconoCategoria(producto.categoria), style = MaterialTheme.typography.displayLarge)
            Spacer(Modifier.height(8.dp))
            Text(producto.titulo, style = MaterialTheme.typography.headlineSmall)
            Text(producto.categoria, style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(12.dp))
            Text(precioUsd(producto.precioUsd), style = MaterialTheme.typography.headlineMedium)
            Text(
                if (producto.categoria == "Curso") "Cupos disponibles" else "Stock: ${producto.stock} unidades",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(16.dp))
            Text(descripcionDe(producto.categoria), style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { mensaje = "✓ \"${producto.titulo}\" agregado al carrito" },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Agregar al carrito") }

            Spacer(Modifier.height(8.dp))

            OutlinedButton(
                onClick = { mensaje = "✓ Solicitud enviada. Te contactaremos con más información." },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Solicitar información") }

            mensaje?.let {
                Spacer(Modifier.height(16.dp))
                Text(it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
