package cl.duoc.museodelosmuseos.model

data class MuseoVirtual(
    val id: String,
    val nombre: String,
    val pais: String,
    val latitud: Double,
    val longitud: Double,
    val descripcion: String,
    val urlVirtual: String,
    val esFavorito: Boolean = false
)

data class Producto(
    val id: String,
    val titulo: String,
    val categoria: String, // "Libro", "Merch", "Curso"
    val precioUsd: Double,
    val stock: Int
)