package cl.duoc.museodelosmuseos

import cl.duoc.museodelosmuseos.viewmodel.GeoUtils
import org.junit.Assert.assertEquals
import org.junit.Test

class GeoUtilsTest {

    @Test
    fun mismoPunto_distanciaCero() {
        val d = GeoUtils.distanciaKm(-33.4489, -70.6693, -33.4489, -70.6693)
        assertEquals(0.0, d, 0.001)
    }

    @Test
    fun santiagoBuenosAires_aproximadamente1140km() {
        val d = GeoUtils.distanciaKm(-33.4489, -70.6693, -34.6037, -58.3816)
        assertEquals(1140.0, d, 30.0)
    }

    @Test
    fun santiagoMadrid_aproximadamente10700km() {
        val d = GeoUtils.distanciaKm(-33.4489, -70.6693, 40.4168, -3.7038)
        assertEquals(10700.0, d, 100.0)
    }
}

