package cl.duoc.museodelosmuseos.model

// Un producto dentro del carrito, con su cantidad
data class ItemCarrito(
    val producto: Producto,
    val cantidad: Int
)

// Zonas de despacho disponibles
enum class ZonaEnvio(val etiqueta: String) {
    NACIONAL("Chile"),
    AMERICA("América"),
    RESTO_MUNDO("Resto del mundo")
}

// Resultado del cálculo que se muestra al usuario
data class Cotizacion(
    val subtotalUsd: Double,
    val pesoTotalKg: Double,
    val costoEnvioUsd: Double,
    val totalUsd: Double,
    val envioGratis: Boolean,
    val soloDigital: Boolean
)