package cl.duoc.museodelosmuseos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cl.duoc.museodelosmuseos.model.Producto
import cl.duoc.museodelosmuseos.repository.ProductoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CatalogoUiState(
    val cargando: Boolean = true,
    val error: String? = null,
    val productos: List<Producto> = emptyList(),
    val categoriaSeleccionada: String? = null // null = todas
) {
    val categorias: List<String> get() = productos.map { it.categoria }.distinct()
    val visibles: List<Producto>
        get() = productos.filter { categoriaSeleccionada == null || it.categoria == categoriaSeleccionada }
}

class CatalogoViewModel(private val repository: ProductoRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(CatalogoUiState())
    val uiState: StateFlow<CatalogoUiState> = _uiState.asStateFlow()

    init {
        cargar()
    }

    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            repository.obtenerProductos()
                .onSuccess { lista -> _uiState.update { it.copy(cargando = false, productos = lista) } }
                .onFailure { e ->
                    _uiState.update { it.copy(cargando = false, error = "No se pudo cargar el catálogo: ${e.message}") }
                }
        }
    }

    fun filtrarPorCategoria(categoria: String?) {
        _uiState.update { it.copy(categoriaSeleccionada = categoria) }
    }

    companion object {
        fun factory(repository: ProductoRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                CatalogoViewModel(repository) as T
        }
    }
}
