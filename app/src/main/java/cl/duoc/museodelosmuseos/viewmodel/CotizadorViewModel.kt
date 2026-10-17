package cl.duoc.museodelosmuseos.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.duoc.museodelosmuseos.model.Cotizacion
import cl.duoc.museodelosmuseos.model.ItemCarrito
import cl.duoc.museodelosmuseos.model.Producto
import cl.duoc.museodelosmuseos.model.ZonaEnvio
import cl.duoc.museodelosmuseos.repository.ProductoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

//Todo lo que la pantalla necesita mostrar, en un solo objeto
data class CotizadorUiState(
    val cargando: Boolean = false,
    val error: String? = null,
    val catalogo: List<Producto> = emptyList(),
    val carrito: List<ItemCarrito> = emptyList(),
    val zona: ZonaEnvio = ZonaEnvio.NACIONAL,
    val cotizacion: Cotizacion = CotizadorEnvios.cotizar(emptyList(), ZonaEnvio.NACIONAL)
)

class CotizadorViewModel(
    private val repository: ProductoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CotizadorUiState())
    val uiState: StateFlow<CotizadorUiState> = _uiState.asStateFlow()

    init {
        cargarCatalogo()
    }

    fun cargarCatalogo() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            repository.obtenerProductos()
                .onSuccess { productos ->
                    _uiState.update { it.copy(cargando = false, catalogo = productos) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(cargando = false, error = "No se pudo cargar el catálogo: ${e.message}")
                    }
                }
        }
    }

    fun agregarAlCarrito(producto: Producto) {
        _uiState.update { estado ->
            val item = estado.carrito.find { it.producto.id == producto.id }
            val cantidadActual = item?.cantidad ?: 0

            if (cantidadActual >= producto.stock) {
                return@update estado.copy(error = "No hay más stock de ${producto.titulo}")
            }

            val nuevoCarrito = if (item == null) {
                estado.carrito + ItemCarrito(producto, 1)
            } else {
                estado.carrito.map {
                    if (it.producto.id == producto.id) it.copy(cantidad = it.cantidad + 1) else it
                }
            }
            recalcular(estado.copy(carrito = nuevoCarrito, error = null))
        }
    }

    fun quitarDelCarrito(producto: Producto) {
        _uiState.update { estado ->
            val nuevoCarrito = estado.carrito.mapNotNull {
                when {
                    it.producto.id != producto.id -> it
                    it.cantidad > 1 -> it.copy(cantidad = it.cantidad - 1)
                    else -> null // si llega a 0, se elimina del carrito
                }
            }
            recalcular(estado.copy(carrito = nuevoCarrito))
        }
    }

    fun seleccionarZona(zona: ZonaEnvio) {
        _uiState.update { recalcular(it.copy(zona = zona)) }
    }

    fun vaciarCarrito() {
        _uiState.update { recalcular(it.copy(carrito = emptyList())) }
    }

    // Aplica la regla de negocio cada vez que cambia el carrito o la zona
    private fun recalcular(estado: CotizadorUiState): CotizadorUiState =
        estado.copy(cotizacion = CotizadorEnvios.cotizar(estado.carrito, estado.zona))

    companion object {
        // Permite crear el ViewModel pasándole el repositorio
        fun factory(repository: ProductoRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                CotizadorViewModel(repository) as T
        }
    }
}