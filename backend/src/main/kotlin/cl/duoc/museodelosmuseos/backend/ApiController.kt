package cl.duoc.museodelosmuseos.backend

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

// Endpoints que consume Retrofit en la app (ver ApiService en RemoteApi.kt)
@RestController
@RequestMapping("/api")
class ApiController {

    // Catálogo completo: libros, merchandising y cursos
    @GetMapping("/productos")
    fun productos(): List<Producto> = Datos.productos

    @GetMapping("/productos/{id}")
    fun producto(@PathVariable id: String): Producto =
        Datos.productos.find { it.id.equals(id, ignoreCase = true) }
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado")

    // Solo las capacitaciones
    @GetMapping("/cursos")
    fun cursos(): List<Producto> = Datos.productos.filter { it.categoria == "Curso" }

    @GetMapping("/museos")
    fun museos(): List<MuseoVirtual> = Datos.museos
}
