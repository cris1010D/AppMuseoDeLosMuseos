package cl.duoc.museodelosmuseos.repository

import cl.duoc.museodelosmuseos.model.MuseoVirtual
import cl.duoc.museodelosmuseos.model.Producto


// Contrato: la app solo conoce esta interfaz, no sabe si los datos
// vienen del mock o del backend real.
interface ProductoRepository {
    suspend fun obtenerProductos(): Result<List<Producto>>
    suspend fun obtenerMuseos(): Result<List<MuseoVirtual>>
}
