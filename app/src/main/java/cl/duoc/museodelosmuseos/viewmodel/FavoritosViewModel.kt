package cl.duoc.museodelosmuseos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.duoc.museodelosmuseos.model.MuseoVirtual
import cl.duoc.museodelosmuseos.repository.LocalRepository
import cl.duoc.museodelosmuseos.repository.ProductoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FavoritosUiState(
    val cargando: Boolean = true,
    val error: String? = null,
    val haySesion: Boolean = false,
    val museos: List<MuseoVirtual> = emptyList() // solo los favoritos del usuario activo
)

class FavoritosViewModel(
    private val productoRepository: ProductoRepository,
    private val localRepository: LocalRepository
) : ViewModel() {

    private val _museos = MutableStateFlow<List<MuseoVirtual>>(emptyList())
    private val _carga = MutableStateFlow<Pair<Boolean, String?>>(true to null)

    val uiState: StateFlow<FavoritosUiState> = combine(
        _museos, _carga, localRepository.observarFavoritos(), localRepository.usuarioActivoId
    ) { museos, carga, favoritos, usuarioId ->
        val ids = favoritos.map { it.museoId }.toSet()
        FavoritosUiState(
            cargando = carga.first,
            error = carga.second,
            haySesion = usuarioId != null,
            museos = museos.filter { it.id in ids }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FavoritosUiState())

    init {
        cargar()
    }

    fun cargar() {
        viewModelScope.launch {
            _carga.value = true to null
            productoRepository.obtenerMuseos()
                .onSuccess {
                    _museos.value = it
                    _carga.value = false to null
                }
                .onFailure { _carga.value = false to "No se pudieron cargar los museos: ${it.message}" }
        }
    }

    fun quitarFavorito(museoId: String) {
        viewModelScope.launch { localRepository.alternarFavorito(museoId) }
    }

    companion object {
        fun factory(
            productoRepository: ProductoRepository,
            localRepository: LocalRepository
        ) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                FavoritosViewModel(productoRepository, localRepository) as T
        }
    }
}
