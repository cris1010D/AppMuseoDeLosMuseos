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

// Un museo listo para mostrar en el mapa, con su distancia y si es favorito
data class MuseoEnMapa(
    val museo: MuseoVirtual,
    val distanciaKm: Double,
    val esFavorito: Boolean
)

data class MapaUiState(
    val cargando: Boolean = true,
    val error: String? = null,
    val paises: List<String> = emptyList(),
    val paisSeleccionado: String? = null, // null = todos los países
    val museos: List<MuseoEnMapa> = emptyList(),
    val museoSeleccionado: MuseoEnMapa? = null,
    val haySesion: Boolean = false
)

class MapaViewModel(
    private val productoRepository: ProductoRepository,
    private val localRepository: LocalRepository
) : ViewModel() {

    private data class Carga(val cargando: Boolean = true, val error: String? = null)

    private val _museos = MutableStateFlow<List<MuseoVirtual>>(emptyList())
    private val _carga = MutableStateFlow(Carga())
    private val _pais = MutableStateFlow<String?>(null)
    private val _seleccionadoId = MutableStateFlow<String?>(null)

    // IDs de museos favoritos del usuario activo + si hay alguien con sesión iniciada
    private val _favoritos = combine(
        localRepository.observarFavoritos(),
        localRepository.usuarioActivoId
    ) { favoritos, usuarioId ->
        favoritos.map { it.museoId }.toSet() to (usuarioId != null)
    }

    // Combina todas las fuentes en un único estado para la pantalla
    val uiState: StateFlow<MapaUiState> = combine(
        _museos, _carga, _pais, _seleccionadoId, _favoritos
    ) { museos, carga, pais, seleccionadoId, (idsFavoritos, haySesion) ->

        val visibles = museos
            .filter { pais == null || it.pais == pais }
            .map { museo ->
                MuseoEnMapa(
                    museo = museo,
                    distanciaKm = GeoUtils.distanciaKm(
                        GeoUtils.ORIGEN_LAT, GeoUtils.ORIGEN_LON,
                        museo.latitud, museo.longitud
                    ),
                    esFavorito = museo.id in idsFavoritos
                )
            }
            .sortedBy { it.distanciaKm }

        MapaUiState(
            cargando = carga.cargando,
            error = carga.error,
            paises = museos.map { it.pais }.distinct().sorted(),
            paisSeleccionado = pais,
            museos = visibles,
            museoSeleccionado = visibles.find { it.museo.id == seleccionadoId },
            haySesion = haySesion
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MapaUiState())

    init {
        cargarMuseos()
    }

    fun cargarMuseos() {
        viewModelScope.launch {
            _carga.value = Carga(cargando = true)
            productoRepository.obtenerMuseos()
                .onSuccess {
                    _museos.value = it
                    _carga.value = Carga(cargando = false)
                }
                .onFailure {
                    _carga.value = Carga(cargando = false, error = "No se pudieron cargar los museos: ${it.message}")
                }
        }
    }

    fun filtrarPorPais(pais: String?) {
        _pais.value = pais
        _seleccionadoId.value = null // cierra la tarjeta al cambiar el filtro
    }

    fun seleccionarMuseo(museoId: String?) {
        _seleccionadoId.value = museoId
    }

    fun alternarFavorito(museoId: String) {
        viewModelScope.launch { localRepository.alternarFavorito(museoId) }
    }

    companion object {
        fun factory(
            productoRepository: ProductoRepository,
            localRepository: LocalRepository
        ) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                MapaViewModel(productoRepository, localRepository) as T
        }
    }
}