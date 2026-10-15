package cl.duoc.museodelosmuseos.repository

import cl.duoc.museodelosmuseos.model.FavoritoEntity
import cl.duoc.museodelosmuseos.model.LecturaQrEntity
import cl.duoc.museodelosmuseos.model.Producto
import cl.duoc.museodelosmuseos.model.UsuarioEntity
import cl.duoc.museodelosmuseos.model.toCacheEntity
import cl.duoc.museodelosmuseos.model.toProducto
import cl.duoc.museodelosmuseos.repository.local.AppDatabase
import cl.duoc.museodelosmuseos.repository.local.SessionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

// Punto único de acceso a los datos locales.
// Los ViewModels usan esta clase y nunca tocan Room directamente.
@OptIn(ExperimentalCoroutinesApi::class)
class LocalRepository(
    private val db: AppDatabase,
    private val session: SessionManager
) {
    val usuarioActivoId: StateFlow<Long?> = session.usuarioActivoId

    // ---------- Usuarios ----------
    fun observarUsuarios(): Flow<List<UsuarioEntity>> = db.usuarioDao().observarTodos()

    // Si el email ya existe, entra con ese usuario; si no, lo crea.
    suspend fun ingresar(nombre: String, email: String): Long {
        val dao = db.usuarioDao()
        val id = dao.buscarPorEmail(email)?.id
            ?: dao.insertar(UsuarioEntity(nombre = nombre, email = email))
        session.iniciarSesion(id)
        return id
    }

    fun cerrarSesion() = session.cerrarSesion()

    // ---------- Favoritos (solo del usuario activo) ----------
    fun observarFavoritos(): Flow<List<FavoritoEntity>> =
        usuarioActivoId.flatMapLatest { id ->
            if (id == null) flowOf(emptyList())
            else db.favoritoDao().observarPorUsuario(id)
        }

    suspend fun alternarFavorito(museoId: String) {
        val id = usuarioActivoId.value ?: return
        val dao = db.favoritoDao()
        if (dao.esFavorito(id, museoId).first()) dao.quitar(id, museoId)
        else dao.agregar(FavoritoEntity(usuarioId = id, museoId = museoId))
    }

    // ---------- Historial QR (solo del usuario activo) ----------
    fun observarHistorialQr(): Flow<List<LecturaQrEntity>> =
        usuarioActivoId.flatMapLatest { id ->
            if (id == null) flowOf(emptyList())
            else db.lecturaQrDao().observarHistorial(id)
        }

    // Devuelve true si se guardó, false si era duplicado o no hay sesión
    suspend fun registrarLecturaQr(contenido: String, tipo: String): Boolean {
        val id = usuarioActivoId.value ?: return false
        val resultado = db.lecturaQrDao().registrar(
            LecturaQrEntity(usuarioId = id, contenido = contenido, tipo = tipo)
        )
        return resultado != -1L
    }

    // ---------- Caché del catálogo (offline) ----------
    fun observarCatalogoCache(): Flow<List<Producto>> =
        db.productoCacheDao().observarTodos().map { lista -> lista.map { it.toProducto() } }

    suspend fun guardarCatalogoCache(productos: List<Producto>) =
        db.productoCacheDao().guardarTodos(productos.map { it.toCacheEntity() })
}