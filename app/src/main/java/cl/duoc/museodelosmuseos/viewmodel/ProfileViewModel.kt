package cl.duoc.museodelosmuseos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.duoc.museodelosmuseos.model.UsuarioEntity
import cl.duoc.museodelosmuseos.repository.LocalRepository
import cl.duoc.museodelosmuseos.repository.local.AVATARES
import cl.duoc.museodelosmuseos.repository.local.PerfilPrefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class ProfileUiState(
    val usuario: UsuarioEntity? = null,
    val avatar: String = AVATARES.first(),
    val notificaciones: Boolean = true,
    val totalFavoritos: Int = 0,
    val totalLecturasQr: Int = 0
)

class ProfileViewModel(
    private val local: LocalRepository,
    private val perfil: PerfilPrefs
) : ViewModel() {

    private val _notificaciones = MutableStateFlow(
        perfil.notificacionesActivas(local.usuarioActivoId.value ?: -1L)
    )

    val uiState: StateFlow<ProfileUiState> = combine(
        local.observarUsuarios(),
        local.usuarioActivoId,
        local.observarFavoritos(),
        local.observarHistorialQr(),
        _notificaciones
    ) { usuarios, usuarioId, favoritos, lecturas, notificaciones ->
        ProfileUiState(
            usuario = usuarios.find { it.id == usuarioId },
            avatar = usuarioId?.let { perfil.avatarDe(it) } ?: AVATARES.first(),
            notificaciones = notificaciones,
            totalFavoritos = favoritos.size,
            totalLecturasQr = lecturas.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())

    fun alternarNotificaciones(activas: Boolean) {
        local.usuarioActivoId.value?.let { perfil.guardarNotificaciones(it, activas) }
        _notificaciones.value = activas
    }

    fun cerrarSesion() = local.cerrarSesion()

    companion object {
        fun factory(local: LocalRepository, perfil: PerfilPrefs) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ProfileViewModel(local, perfil) as T
        }
    }
}
