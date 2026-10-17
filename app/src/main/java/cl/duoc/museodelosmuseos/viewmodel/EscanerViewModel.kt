package cl.duoc.museodelosmuseos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.duoc.museodelosmuseos.model.LecturaQrEntity
import cl.duoc.museodelosmuseos.model.Producto
import cl.duoc.museodelosmuseos.repository.LocalRepository
import cl.duoc.museodelosmuseos.repository.ProductoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Posibles resultados de una lectura
sealed class ResultadoEscaneo {
    data class Exito(val qr: QrClasificado) : ResultadoEscaneo()
    data class Duplicado(val qr: QrClasificado) : ResultadoEscaneo()
    data class Invalido(val contenido: String) : ResultadoEscaneo()
    data object SinSesion : ResultadoEscaneo()
}

data class EscanerUiState(
    val resultado: ResultadoEscaneo? = null,
    val historial: List<LecturaQrEntity> = emptyList()
)

class EscanerViewModel(
    private val productoRepository: ProductoRepository,
    private val localRepository: LocalRepository
) : ViewModel() {

    private var catalogo: Map<String, Producto> = emptyMap()
    private val _resultado = MutableStateFlow<ResultadoEscaneo?>(null)

    // La cámara entrega muchos cuadros por segundo: esto evita procesar
    // el mismo código una y otra vez mientras sigue frente al lente
    private var procesando = false
    private var ultimoContenido: String? = null
    private var ultimaLecturaMs = 0L

    val uiState: StateFlow<EscanerUiState> = combine(
        _resultado,
        localRepository.observarHistorialQr()
    ) { resultado, historial ->
        EscanerUiState(resultado, historial)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EscanerUiState())

    init {
        viewModelScope.launch {
            productoRepository.obtenerProductos().onSuccess { lista ->
                catalogo = lista.associateBy { it.id.uppercase() }
            }
        }
    }

    fun procesarLectura(contenido: String) {
        val ahora = System.currentTimeMillis()
        if (procesando) return
        if (contenido == ultimoContenido && ahora - ultimaLecturaMs < 3_000) return

        procesando = true
        ultimoContenido = contenido
        ultimaLecturaMs = ahora

        viewModelScope.launch {
            val qr = ValidadorQr.clasificar(contenido, catalogo)

            _resultado.value = when {
                qr.tipo == TipoQr.DESCONOCIDO -> ResultadoEscaneo.Invalido(qr.contenido)
                localRepository.usuarioActivoId.value == null -> ResultadoEscaneo.SinSesion
                localRepository.registrarLecturaQr(qr.contenido, qr.tipo.name) -> ResultadoEscaneo.Exito(qr)
                else -> ResultadoEscaneo.Duplicado(qr)
            }
            procesando = false
        }
    }

    fun limpiarResultado() {
        _resultado.value = null
    }

    companion object {
        fun factory(
            productoRepository: ProductoRepository,
            localRepository: LocalRepository
        ) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                EscanerViewModel(productoRepository, localRepository) as T
        }
    }
}