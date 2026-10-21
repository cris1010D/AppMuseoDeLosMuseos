package cl.duoc.museodelosmuseos.repository

import android.content.Context
import cl.duoc.museodelosmuseos.repository.local.AppDatabase
import cl.duoc.museodelosmuseos.repository.local.SessionManager

// Único lugar donde se decide de dónde salen los datos
object RepositoryProvider {

    // Cambiar a false cuando el backend Spring Boot esté funcionando

    fun localRepository(context: Context): LocalRepository =
        LocalRepository(
            AppDatabase.getInstance(context),
            SessionManager.getInstance(context)
        )

    fun productoRepository(context: Context): ProductoRepository = FakeProductoRepository()
}

