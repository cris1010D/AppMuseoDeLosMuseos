package cl.duoc.museodelosmuseos.viewmodel

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

// Cálculo de distancia entre dos coordenadas (fórmula de Haversine)
object GeoUtils {

    // Punto de origen para medir distancias: Santiago de Chile
    const val ORIGEN_LAT = -33.4489
    const val ORIGEN_LON = -70.6693
    private const val RADIO_TIERRA_KM = 6371.0

    fun distanciaKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return RADIO_TIERRA_KM * c
    }
}