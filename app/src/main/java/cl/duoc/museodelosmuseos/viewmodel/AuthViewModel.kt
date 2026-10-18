package cl.duoc.museodelosmuseos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.duoc.museodelosmuseos.repository.LocalRepository
import cl.duoc.museodelosmuseos.repository.local.AVATARES
import cl.duoc.museodelosmuseos.repository.local.PerfilPrefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val modoRegistro: Boolean = true, // false = iniciar sesión
    val nombre: String = "",
    val email: String = "",
    val password: String = "",
    val avatar: String = AVATARES.first(),
    val errorNombre: String? = null,
    val errorEmail: String? = null,
    val errorPassword: String? = null,
    val enviando: Boolean = false
)

class AuthViewModel(
    private val local: LocalRepository,
    private val perfil: PerfilPrefs
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun cambiarModo() = _uiState.update {
        it.copy(
            modoRegistro = !it.modoRegistro,
            errorNombre = null, errorEmail = null, errorPassword = null
        )
    }

    fun onNombre(valor: String) = _uiState.update { it.copy(nombre = valor, errorNombre = null) }
    fun onEmail(valor: String) = _uiState.update { it.copy(email = valor, errorEmail = null) }
    fun onPassword(valor: String) = _uiState.update { it.copy(password = valor, errorPassword = null) }
    fun onAvatar(valor: String) = _uiState.update { it.copy(avatar = valor) }

    fun enviar(onExito: () -> Unit) {
        val estado = _uiState.value
        if (estado.enviando) return

        val email = estado.email.trim().lowercase()
        val errorNombre = if (estado.modoRegistro && estado.nombre.isBlank()) "Ingresa tu nombre" else null
        val errorEmail = if (!EMAIL.matches(email)) "Ingresa un correo válido" else null
        val errorPassword = if (estado.password.length < 6) "Mínimo 6 caracteres" else null

        if (errorNombre != null || errorEmail != null || errorPassword != null) {
            _uiState.update {
                it.copy(errorNombre = errorNombre, errorEmail = errorEmail, errorPassword = errorPassword)
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(enviando = true) }
            val existe = local.observarUsuarios().first().any { it.email == email }

            when {
                estado.modoRegistro && existe ->
                    _uiState.update { it.copy(enviando = false, errorEmail = "Este correo ya está registrado") }

                !estado.modoRegistro && !existe ->
                    _uiState.update { it.copy(enviando = false, errorEmail = "No existe una cuenta con este correo") }

                else -> {
                    val id = local.ingresar(estado.nombre.trim(), email)
                    if (estado.modoRegistro) perfil.guardarAvatar(id, estado.avatar)
                    _uiState.update { it.copy(enviando = false) }
                    onExito()
                }
            }
        }
    }

    companion object {
        private val EMAIL = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

        fun factory(local: LocalRepository, perfil: PerfilPrefs) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                AuthViewModel(local, perfil) as T
        }
    }
}
