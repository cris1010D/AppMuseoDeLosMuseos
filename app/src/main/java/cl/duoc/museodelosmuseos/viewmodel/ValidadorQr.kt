package cl.duoc.museodelosmuseos.viewmodel

import cl.duoc.museodelosmuseos.model.Producto

enum class TipoQr { PRODUCTO, PUBLICACION, DESCONOCIDO }

data class QrClasificado(
    val contenido: String,
    val tipo: TipoQr,
    val descripcion: String
)

// Regla de validación: decide qué representa el texto leído en el QR
object ValidadorQr {

    private val PATRON_PUBLICACION = Regex("^PUB-\\d{3,}$")

    fun clasificar(contenidoLeido: String, catalogo: Map<String, Producto>): QrClasificado {
        val limpio = contenidoLeido.trim().uppercase()

        catalogo[limpio]?.let {
            return QrClasificado(limpio, TipoQr.PRODUCTO, it.titulo)
        }
        if (PATRON_PUBLICACION.matches(limpio)) {
            return QrClasificado(limpio, TipoQr.PUBLICACION, "Publicación $limpio")
        }
        return QrClasificado(limpio, TipoQr.DESCONOCIDO, "Código no reconocido")
    }
}