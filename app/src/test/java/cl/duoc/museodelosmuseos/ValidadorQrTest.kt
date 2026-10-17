package cl.duoc.museodelosmuseos

import cl.duoc.museodelosmuseos.model.Producto
import cl.duoc.museodelosmuseos.viewmodel.TipoQr
import cl.duoc.museodelosmuseos.viewmodel.ValidadorQr
import org.junit.Assert.assertEquals
import org.junit.Test

class ValidadorQrTest {

    private val catalogo = mapOf(
        "L001" to Producto("L001", "Arte precolombino", "Libro", 35.0, 12)
    )

    @Test
    fun idDelCatalogo_esProducto() {
        val r = ValidadorQr.clasificar("L001", catalogo)
        assertEquals(TipoQr.PRODUCTO, r.tipo)
        assertEquals("Arte precolombino", r.descripcion)
    }

    @Test
    fun minusculasYEspacios_seNormalizan() {
        assertEquals(TipoQr.PRODUCTO, ValidadorQr.clasificar("  l001 ", catalogo).tipo)
    }

    @Test
    fun formatoPub_esPublicacion() {
        assertEquals(TipoQr.PUBLICACION, ValidadorQr.clasificar("PUB-001", catalogo).tipo)
    }

    @Test
    fun textoCualquiera_esDesconocido() {
        assertEquals(TipoQr.DESCONOCIDO, ValidadorQr.clasificar("https://example.com", catalogo).tipo)
    }
}