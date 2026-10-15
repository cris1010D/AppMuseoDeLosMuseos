package cl.duoc.museodelosmuseos.repository.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import cl.duoc.museodelosmuseos.model.FavoritoEntity
import cl.duoc.museodelosmuseos.model.LecturaQrEntity
import cl.duoc.museodelosmuseos.model.ProductoCacheEntity
import cl.duoc.museodelosmuseos.model.UsuarioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(usuario: UsuarioEntity): Long

    @Query("SELECT * FROM usuarios WHERE email = :email LIMIT 1")
    suspend fun buscarPorEmail(email: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios ORDER BY nombre")
    fun observarTodos(): Flow<List<UsuarioEntity>>
}

@Dao
interface FavoritoDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun agregar(favorito: FavoritoEntity)

    @Query("DELETE FROM favoritos WHERE usuarioId = :usuarioId AND museoId = :museoId")
    suspend fun quitar(usuarioId: Long, museoId: String)

    @Query("SELECT * FROM favoritos WHERE usuarioId = :usuarioId ORDER BY fechaGuardado DESC")
    fun observarPorUsuario(usuarioId: Long): Flow<List<FavoritoEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favoritos WHERE usuarioId = :usuarioId AND museoId = :museoId)")
    fun esFavorito(usuarioId: Long, museoId: String): Flow<Boolean>
}

@Dao
interface LecturaQrDao {
    // Devuelve -1 si el QR ya estaba registrado para ese usuario (duplicado)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun registrar(lectura: LecturaQrEntity): Long

    @Query("SELECT * FROM lecturas_qr WHERE usuarioId = :usuarioId ORDER BY fechaLectura DESC")
    fun observarHistorial(usuarioId: Long): Flow<List<LecturaQrEntity>>
}

@Dao
interface ProductoCacheDao {
    @Upsert
    suspend fun guardarTodos(productos: List<ProductoCacheEntity>)

    @Query("SELECT * FROM productos_cache ORDER BY titulo")
    fun observarTodos(): Flow<List<ProductoCacheEntity>>

    @Query("DELETE FROM productos_cache")
    suspend fun limpiar()
}