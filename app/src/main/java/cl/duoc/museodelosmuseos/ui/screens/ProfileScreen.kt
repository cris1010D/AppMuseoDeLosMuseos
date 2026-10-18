package cl.duoc.museodelosmuseos.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
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
import cl.duoc.museodelosmuseos.repository.local.PerfilPrefs
import cl.duoc.museodelosmuseos.viewmodel.ProfileViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val formatoFecha = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

@Composable
fun ProfileScreen(onCerrarSesion: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val factory = remember {
        ProfileViewModel.factory(
            RepositoryProvider.localRepository(context),
            PerfilPrefs.getInstance(context)
        )
    }
    val vm: ProfileViewModel = viewModel(factory = factory)
    val estado by vm.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Datos del usuario activo
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(estado.avatar, style = MaterialTheme.typography.displayLarge)
            Text(estado.usuario?.nombre ?: "Sin sesión", style = MaterialTheme.typography.headlineSmall)
            estado.usuario?.let {
                Text(it.email, style = MaterialTheme.typography.bodyMedium)
                Text(
                    "Miembro desde ${formatoFecha.format(Date(it.fechaRegistro))}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        HorizontalDivider()

        // Ajustes
        Text("Ajustes", style = MaterialTheme.typography.titleMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Recibir notificaciones", Modifier.weight(1f))
            Switch(checked = estado.notificaciones, onCheckedChange = vm::alternarNotificaciones)
        }

        HorizontalDivider()

        // Centro de notificaciones
        Text("Notificaciones", style = MaterialTheme.typography.titleMedium)
        if (estado.notificaciones) {
            listOf(
                "¡Bienvenido al Museo de los Museos!",
                "Tienes ${estado.totalFavoritos} museos favoritos guardados.",
                "Has registrado ${estado.totalLecturasQr} códigos QR."
            ).forEach { texto ->
                Card(Modifier.fillMaxWidth()) { Text(texto, Modifier.padding(12.dp)) }
            }
        } else {
            Text("Las notificaciones están desactivadas.", style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(Modifier.height(8.dp))

        OutlinedButton(
            onClick = {
                vm.cerrarSesion()
                onCerrarSesion()
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Cerrar sesión") }
    }
}
