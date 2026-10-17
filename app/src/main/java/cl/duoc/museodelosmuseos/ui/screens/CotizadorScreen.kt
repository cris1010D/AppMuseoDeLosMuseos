package cl.duoc.museodelosmuseos.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.museodelosmuseos.model.ZonaEnvio
import cl.duoc.museodelosmuseos.repository.RepositoryProvider
import cl.duoc.museodelosmuseos.viewmodel.CotizadorViewModel

private fun usd(valor: Double) = "USD %.2f".format(valor)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CotizadorScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val repository = remember { RepositoryProvider.productoRepository(context) }
    val vm: CotizadorViewModel = viewModel(factory = CotizadorViewModel.factory(repository))
    val estado by vm.uiState.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {

        Text("Cotizador de envíos", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))

        // Selector de zona de despacho
        Row {
            ZonaEnvio.entries.forEach { zona ->
                FilterChip(
                    selected = estado.zona == zona,
                    onClick = { vm.seleccionarZona(zona) },
                    label = { Text(zona.etiqueta) },
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        }

        if (estado.cargando) {
            CircularProgressIndicator(Modifier.padding(16.dp))
        }

        estado.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        // Catálogo con botones + y −
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(estado.catalogo, key = { it.id }) { producto ->
                val cantidad = estado.carrito.find { it.producto.id == producto.id }?.cantidad ?: 0
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(producto.titulo, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "${producto.categoria} · ${usd(producto.precioUsd)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    TextButton(
                        onClick = { vm.quitarDelCarrito(producto) },
                        enabled = cantidad > 0
                    ) { Text("−") }
                    Text("$cantidad")
                    TextButton(onClick = { vm.agregarAlCarrito(producto) }) { Text("+") }
                }
            }
        }

        HorizontalDivider(Modifier.padding(vertical = 8.dp))

        // Resumen de la cotización
        val c = estado.cotizacion
        Text("Subtotal: ${usd(c.subtotalUsd)}")
        Text("Peso: ${c.pesoTotalKg} kg")
        Text(
            when {
                c.soloDigital -> "Envío: gratis (productos digitales)"
                c.envioGratis -> "Envío: gratis (compra nacional sobre USD 50)"
                else -> "Envío: ${usd(c.costoEnvioUsd)}"
            }
        )
        Text("Total: ${usd(c.totalUsd)}", style = MaterialTheme.typography.titleMedium)
    }
}