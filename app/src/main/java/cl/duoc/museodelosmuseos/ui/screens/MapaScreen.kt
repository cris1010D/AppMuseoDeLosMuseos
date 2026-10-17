package cl.duoc.museodelosmuseos.ui.screens

import android.content.Context
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.museodelosmuseos.repository.RepositoryProvider
import cl.duoc.museodelosmuseos.viewmodel.GeoUtils
import cl.duoc.museodelosmuseos.viewmodel.MapaViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapaScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    val factory = remember {
        MapaViewModel.factory(
            RepositoryProvider.productoRepository(context),
            RepositoryProvider.localRepository(context)
        )
    }
    val vm: MapaViewModel = viewModel(factory = factory)
    val estado by vm.uiState.collectAsStateWithLifecycle()

    // osmdroid exige identificar la app (política de uso de OpenStreetMap)
    val mapView = remember {
        Configuration.getInstance().load(
            context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = context.packageName

        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(3.0)
            controller.setCenter(GeoPoint(-10.0, -55.0)) // centrado en Sudamérica
        }
    }

    // Libera el mapa al salir de la pantalla
    DisposableEffect(Unit) {
        onDispose { mapView.onDetach() }
    }

    // Al seleccionar un museo, el mapa se mueve hacia él
    LaunchedEffect(estado.museoSeleccionado?.museo?.id) {
        estado.museoSeleccionado?.let {
            mapView.controller.animateTo(GeoPoint(it.museo.latitud, it.museo.longitud), 6.0, 800L)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {

        // El mapa (vista clásica de Android dentro de Compose)
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize(),
            update = { mv ->
                mv.overlays.removeAll { it is Marker }
                estado.museos.forEach { item ->
                    val marcador = Marker(mv).apply {
                        position = GeoPoint(item.museo.latitud, item.museo.longitud)
                        title = item.museo.nombre
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        setOnMarkerClickListener { _, _ ->
                            vm.seleccionarMuseo(item.museo.id)
                            true
                        }
                    }
                    mv.overlays.add(marcador)
                }
                mv.invalidate()
            }
        )

        // Filtros por país
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val colores = FilterChipDefaults.filterChipColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
            FilterChip(
                selected = estado.paisSeleccionado == null,
                onClick = { vm.filtrarPorPais(null) },
                label = { Text("Todos") },
                colors = colores
            )
            estado.paises.forEach { pais ->
                FilterChip(
                    selected = estado.paisSeleccionado == pais,
                    onClick = { vm.filtrarPorPais(pais) },
                    label = { Text(pais) },
                    colors = colores
                )
            }
        }

        if (estado.cargando) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        }

        estado.error?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.align(Alignment.Center).padding(24.dp)
            )
        }

        // Tarjeta flotante del museo seleccionado
        estado.museoSeleccionado?.let { item ->
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(item.museo.nombre, style = MaterialTheme.typography.titleMedium)
                    Text(item.museo.pais, style = MaterialTheme.typography.bodySmall)
                    Text(
                        "A %.0f km de Santiago".format(item.distanciaKm),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(item.museo.descripcion, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(onClick = { uriHandler.openUri(item.museo.urlVirtual) }) {
                            Text("Ver recorrido virtual")
                        }
                        TextButton(
                            onClick = { vm.alternarFavorito(item.museo.id) },
                            enabled = estado.haySesion
                        ) {
                            Text(if (item.esFavorito) "★ Favorito" else "☆ Favorito")
                        }
                        TextButton(onClick = { vm.seleccionarMuseo(null) }) {
                            Text("Cerrar")
                        }
                    }

                    if (!estado.haySesion) {
                        Text(
                            "Inicia sesión para guardar favoritos",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}