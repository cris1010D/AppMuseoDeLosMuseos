package cl.duoc.museodelosmuseos.repository

import cl.duoc.museodelosmuseos.model.MuseoVirtual
import cl.duoc.museodelosmuseos.model.Producto
import cl.duoc.museodelosmuseos.repository.remote.ApiService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first

// Contrato: la app solo conoce esta interfaz, no sabe si los datos
// vienen del mock o del backend real.
interface ProductoRepository {
    suspend fun obtenerProductos(): Result<List<Producto>>
    suspend fun obtenerMuseos(): Result<List<MuseoVirtual>>
}

// Implementación real: consulta el backend y guarda una copia en Room.
// Si no hay conexión, entrega lo último que quedó en caché.
class RemoteProductoRepository(
    private val api: ApiService,
    private val local: LocalRepository
) : ProductoRepository {

    override suspend fun obtenerProductos(): Result<List<Producto>> =
        try {
            val productos = api.obtenerProductos()
            local.guardarCatalogoCache(productos)
            Result.success(productos)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            val cache = local.observarCatalogoCache().first()
            if (cache.isNotEmpty()) Result.success(cache) else Result.failure(e)
        }

    override suspend fun obtenerMuseos(): Result<List<MuseoVirtual>> =
        try {
            Result.success(api.obtenerMuseos())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
}