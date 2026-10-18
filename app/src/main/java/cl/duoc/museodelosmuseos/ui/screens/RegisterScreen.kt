package cl.duoc.museodelosmuseos.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.museodelosmuseos.repository.RepositoryProvider
import cl.duoc.museodelosmuseos.repository.local.AVATARES
import cl.duoc.museodelosmuseos.repository.local.PerfilPrefs
import cl.duoc.museodelosmuseos.viewmodel.AuthViewModel

// Pantalla de bienvenida: sirve para registrarse y para iniciar sesión
@Composable
fun RegisterScreen(onIngresoExitoso: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val factory = remember {
        AuthViewModel.factory(
            RepositoryProvider.localRepository(context),
            PerfilPrefs.getInstance(context)
        )
    }
    val vm: AuthViewModel = viewModel(factory = factory)
    val estado by vm.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Museo de los Museos", style = MaterialTheme.typography.headlineMedium)
        Text(
            if (estado.modoRegistro) "Crea tu cuenta" else "Inicia sesión",
            style = MaterialTheme.typography.titleMedium
        )

        if (estado.modoRegistro) {
            OutlinedTextField(
                value = estado.nombre,
                onValueChange = vm::onNombre,
                label = { Text("Nombre") },
                isError = estado.errorNombre != null,
                supportingText = { estado.errorNombre?.let { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        OutlinedTextField(
            value = estado.email,
            onValueChange = vm::onEmail,
            label = { Text("Correo") },
            isError = estado.errorEmail != null,
            supportingText = { estado.errorEmail?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = estado.password,
            onValueChange = vm::onPassword,
            label = { Text("Contraseña") },
            isError = estado.errorPassword != null,
            supportingText = { estado.errorPassword?.let { Text(it) } },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        if (estado.modoRegistro) {
            Text("Elige tu avatar", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AVATARES.forEach { avatar ->
                    FilterChip(
                        selected = estado.avatar == avatar,
                        onClick = { vm.onAvatar(avatar) },
                        label = { Text(avatar, style = MaterialTheme.typography.titleLarge) }
                    )
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        Button(
            onClick = { vm.enviar(onIngresoExitoso) },
            enabled = !estado.enviando,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (estado.enviando) {
                CircularProgressIndicator(Modifier.height(18.dp), strokeWidth = 2.dp)
            } else {
                Text(if (estado.modoRegistro) "Registrarme" else "Entrar")
            }
        }

        TextButton(
            onClick = vm::cambiarModo,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(if (estado.modoRegistro) "¿Ya tienes cuenta? Inicia sesión" else "¿No tienes cuenta? Regístrate")
        }
    }
}
