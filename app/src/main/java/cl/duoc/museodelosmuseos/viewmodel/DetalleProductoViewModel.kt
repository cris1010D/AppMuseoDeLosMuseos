package cl.duoc.museodelosmuseos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.duoc.museodelosmuseos.model.Producto
import cl.duoc.museodelosmuseos.repository.ProductoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DetalleUiState(
    val cargando: Boolean = true,
    val error: String? = null,
    val producto: Producto? = null
)

class DetalleProductoViewModel(
    private val repository: ProductoRepository,
    private val productoId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetalleUiState())
    val uiState: StateFlow<DetalleUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.obtenerProductos()
                .onSuccess { lista ->
                    val encontrado = lista.find { it.id == productoId }
                    _uiState.value = DetalleUiState(
                        cargando = false,
                        producto = encontrado,
                        error = if (encontrado == null) "Producto no encontrado" else null
                    )
                }
                .onFailure {
                    _uiState.value = DetalleUiState(cargando = false, error = "No se pudo cargar el producto: ${it.message}")
                }
        }
    }

    companion object {
        fun factory(repository: ProductoRepository, productoId: String) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                DetalleProductoViewModel(repository, productoId) as T
        }
    }
}
