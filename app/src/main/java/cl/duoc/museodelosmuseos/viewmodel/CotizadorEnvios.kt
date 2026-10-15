package cl.duoc.museodelosmuseos.viewmodel

import cl.duoc.museodelosmuseos.model.Cotizacion
import cl.duoc.museodelosmuseos.model.ItemCarrito
import cl.duoc.museodelosmuseos.model.Producto
import cl.duoc.museodelosmuseos.model.ZonaEnvio
import kotlin.math.ceil
import kotlin.math.round

// Regla de negocio del cálculo de despacho (capa ViewModel).
// Los valores son de ejemplo: ajustarlos a la tabla del caso si existe.
object CotizadorEnvios {

    private val PESO_POR_CATEGORIA_KG = mapOf(
        "Libro" to 0.8,
        "Merch" to 0.4,
        "Curso" to 0.0 // digital, no se despacha
    )
    private const val PESO_POR_DEFECTO_KG = 0.5

    private data class Tarifa(val baseUsd: Double, val porKgUsd: Double)

    private val TARIFAS = mapOf(
        ZonaEnvio.NACIONAL to Tarifa(baseUsd = 4.0, porKgUsd = 1.5),
        ZonaEnvio.AMERICA to Tarifa(baseUsd = 12.0, porKgUsd = 5.0),
        ZonaEnvio.RESTO_MUNDO to Tarifa(baseUsd = 20.0, porKgUsd = 8.0)
    )

    const val UMBRAL_ENVIO_GRATIS_NACIONAL_USD = 50.0

    fun pesoUnitario(producto: Producto): Double =
        PESO_POR_CATEGORIA_KG[producto.categoria] ?: PESO_POR_DEFECTO_KG

    fun cotizar(items: List<ItemCarrito>, zona: ZonaEnvio): Cotizacion {
        val subtotal = items.sumOf { it.producto.precioUsd * it.cantidad }
        val peso = redondear(items.sumOf { pesoUnitario(it.producto) * it.cantidad })

        val soloDigital = items.isNotEmpty() && peso == 0.0
        val envioGratis = zona == ZonaEnvio.NACIONAL &&
                subtotal >= UMBRAL_ENVIO_GRATIS_NACIONAL_USD

        val costoEnvio = when {
            items.isEmpty() || soloDigital || envioGratis -> 0.0
            else -> {
                val tarifa = TARIFAS.getValue(zona)
                val pesoFacturable = ceil(peso * 2) / 2 // redondea hacia arriba a 0,5 kg
                tarifa.baseUsd + tarifa.porKgUsd * pesoFacturable
            }
        }

        return Cotizacion(
            subtotalUsd = redondear(subtotal),
            pesoTotalKg = peso,
            costoEnvioUsd = redondear(costoEnvio),
            totalUsd = redondear(subtotal + costoEnvio),
            envioGratis = envioGratis,
            soloDigital = soloDigital
        )
    }

    private fun redondear(valor: Double): Double = round(valor * 100) / 100
}