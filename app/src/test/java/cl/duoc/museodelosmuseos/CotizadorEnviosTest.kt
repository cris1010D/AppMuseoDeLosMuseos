package cl.duoc.museodelosmuseos

import cl.duoc.museodelosmuseos.model.ItemCarrito
import cl.duoc.museodelosmuseos.model.Producto
import cl.duoc.museodelosmuseos.model.ZonaEnvio
import cl.duoc.museodelosmuseos.viewmodel.CotizadorEnvios
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CotizadorEnviosTest {

    private val libro = Producto("L1", "Libro de arte", "Libro", 20.0, 10)
    private val polera = Producto("M1", "Polera del museo", "Merch", 15.0, 10)
    private val curso = Producto("C1", "Curso de curaduría", "Curso", 30.0, 99)

    @Test
    fun carritoVacio_noCobraNada() {
        val c = CotizadorEnvios.cotizar(emptyList(), ZonaEnvio.NACIONAL)
        assertEquals(0.0, c.totalUsd, 0.001)
    }

    @Test
    fun envioNacional_cobraBaseMasPeso() {
        // 1 libro = 0,8 kg -> se cobra 1,0 kg -> 4 + 1,5 = 5,5
        val c = CotizadorEnvios.cotizar(listOf(ItemCarrito(libro, 1)), ZonaEnvio.NACIONAL)
        assertEquals(5.5, c.costoEnvioUsd, 0.001)
        assertEquals(25.5, c.totalUsd, 0.001)
    }

    @Test
    fun envioInternacional_esMasCaroQueNacional() {
        // 2 poleras = 0,8 kg -> se cobra 1,0 kg -> América: 12 + 5 = 17
        val items = listOf(ItemCarrito(polera, 2))
        val nacional = CotizadorEnvios.cotizar(items, ZonaEnvio.NACIONAL)
        val america = CotizadorEnvios.cotizar(items, ZonaEnvio.AMERICA)
        assertEquals(17.0, america.costoEnvioUsd, 0.001)
        assertTrue(america.costoEnvioUsd > nacional.costoEnvioUsd)
    }

    @Test
    fun nacionalSobreUmbral_tieneEnvioGratis() {
        // 3 libros = USD 60 -> supera los USD 50
        val c = CotizadorEnvios.cotizar(listOf(ItemCarrito(libro, 3)), ZonaEnvio.NACIONAL)
        assertTrue(c.envioGratis)
        assertEquals(0.0, c.costoEnvioUsd, 0.001)
    }

    @Test
    fun soloCursos_noPaganEnvio() {
        val c = CotizadorEnvios.cotizar(listOf(ItemCarrito(curso, 1)), ZonaEnvio.RESTO_MUNDO)
        assertTrue(c.soloDigital)
        assertEquals(0.0, c.costoEnvioUsd, 0.001)
    }
}