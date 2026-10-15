package cl.duoc.museodelosmuseos.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Tabla de usuarios: cada persona que usa la app en el mismo dispositivo
@Entity(
    tableName = "usuarios",
    indices = [Index(value = ["email"], unique = true)]
)
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val email: String,
    val fechaRegistro: Long = System.currentTimeMillis()
)

// Museos favoritos, separados por usuario
@Entity(
    tableName = "favoritos",
    primaryKeys = ["usuarioId", "museoId"],
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class FavoritoEntity(
    val usuarioId: Long,
    val museoId: String,
    val fechaGuardado: Long = System.currentTimeMillis()
)

// Historial de lecturas QR. El índice único (usuario + contenido)
// impide que un mismo usuario guarde dos veces el mismo QR.
@Entity(
    tableName = "lecturas_qr",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["usuarioId", "contenido"], unique = true)]
)
data class LecturaQrEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val usuarioId: Long,
    val contenido: String,
    val tipo: String, // "PRODUCTO", "PUBLICACION" o "DESCONOCIDO"
    val fechaLectura: Long = System.currentTimeMillis()
)

// Caché offline del catálogo (se llena con lo que traiga Retrofit)
@Entity(tableName = "productos_cache")
data class ProductoCacheEntity(
    @PrimaryKey val id: String,
    val titulo: String,
    val categoria: String,
    val precioUsd: Double,
    val stock: Int,
    val actualizadoEn: Long = System.currentTimeMillis()
)

// Conversión entre el modelo de la app y la caché
fun ProductoCacheEntity.toProducto() = Producto(id, titulo, categoria, precioUsd, stock)
fun Producto.toCacheEntity() = ProductoCacheEntity(id, titulo, categoria, precioUsd, stock)