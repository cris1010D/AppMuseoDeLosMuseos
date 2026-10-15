package cl.duoc.museodelosmuseos.repository

import android.content.Context
import cl.duoc.museodelosmuseos.repository.local.AppDatabase
import cl.duoc.museodelosmuseos.repository.local.SessionManager
import cl.duoc.museodelosmuseos.repository.remote.RetrofitClient

// Único lugar donde se decide de dónde salen los datos
object RepositoryProvider {

    // Cambiar a false cuando el backend Spring Boot esté funcionando
    const val USAR_DATOS_SIMULADOS = true

    fun localRepository(context: Context): LocalRepository =
        LocalRepository(
            AppDatabase.getInstance(context),
            SessionManager.getInstance(context)
        )

    fun productoRepository(context: Context): ProductoRepository =
        if (USAR_DATOS_SIMULADOS) FakeProductoRepository()
        else RemoteProductoRepository(RetrofitClient.api, localRepository(context))
}