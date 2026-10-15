package cl.duoc.museodelosmuseos.navigation

// Sealed class para definir rutas seguras sin errores de tipeo
sealed class Screen(val route: String) {
    data object Home : Screen("home_screen")
    data object Mapa : Screen("mapa_screen")
    data object Catalogo : Screen("catalogo_screen")
    data object Registro : Screen("registro_screen")

    // Ruta con parámetro para ver el detalle de un museo o producto
    data class Detalle(val id: String) : Screen("detalle_screen/$id") {
        companion object {
            const val ROUTE = "detalle_screen/{id}"
        }
    }
}