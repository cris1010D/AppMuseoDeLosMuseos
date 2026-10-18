package cl.duoc.museodelosmuseos.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.duoc.museodelosmuseos.repository.RepositoryProvider
import cl.duoc.museodelosmuseos.ui.screens.CatalogoScreen
import cl.duoc.museodelosmuseos.ui.screens.CotizadorScreen
import cl.duoc.museodelosmuseos.ui.screens.DetalleProductoScreen
import cl.duoc.museodelosmuseos.ui.screens.EscanerScreen
import cl.duoc.museodelosmuseos.ui.screens.FavoritosScreen
import cl.duoc.museodelosmuseos.ui.screens.HomeScreen
import cl.duoc.museodelosmuseos.ui.screens.MapaScreen
import cl.duoc.museodelosmuseos.ui.screens.ProfileScreen
import cl.duoc.museodelosmuseos.ui.screens.RegisterScreen

private val TITULOS = mapOf(
    Screen.Home.route to "Museo de los Museos",
    Screen.Catalogo.route to "Catálogo",
    Screen.Detalle.ROUTE to "Detalle",
    Screen.Mapa.route to "Mapa de museos",
    Screen.Escaner.route to "Escáner QR",
    Screen.Cotizador.route to "Cotizador",
    Screen.Favoritos.route to "Favoritos",
    Screen.Perfil.route to "Perfil y ajustes"
)

// Va a una pantalla limpiando toda la pila (al entrar o cerrar sesión)
private fun NavHostController.irLimpiando(destino: Screen) {
    navigate(destino.route) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost() {
    val context = LocalContext.current
    val navController = rememberNavController()

    // Si ya hay un usuario con sesión, se salta el registro
    val inicio = remember {
        if (RepositoryProvider.localRepository(context).usuarioActivoId.value != null)
            Screen.Home.route else Screen.Registro.route
    }

    val entrada by navController.currentBackStackEntryAsState()
    val ruta = entrada?.destination?.route
    val mostrarBarra = ruta != null && ruta != Screen.Registro.route
    val mostrarAtras = mostrarBarra && ruta != Screen.Home.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (mostrarBarra) {
                TopAppBar(
                    title = { Text(TITULOS[ruta] ?: "") },
                    navigationIcon = {
                        if (mostrarAtras) {
                            TextButton(onClick = { navController.popBackStack() }) { Text("← Volver") }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = inicio,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Registro.route) {
                RegisterScreen(onIngresoExitoso = { navController.irLimpiando(Screen.Home) })
            }
            composable(Screen.Home.route) {
                HomeScreen(onNavigate = { navController.navigate(it.route) { launchSingleTop = true } })
            }
            composable(Screen.Catalogo.route) {
                CatalogoScreen(onProductoClick = { id -> navController.navigate(Screen.Detalle(id).route) })
            }
            composable(
                route = Screen.Detalle.ROUTE,
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { backStackEntry ->
                DetalleProductoScreen(productoId = backStackEntry.arguments?.getString("id").orEmpty())
            }
            composable(Screen.Mapa.route) { MapaScreen() }
            composable(Screen.Escaner.route) { EscanerScreen() }
            composable(Screen.Cotizador.route) { CotizadorScreen() }
            composable(Screen.Favoritos.route) { FavoritosScreen() }
            composable(Screen.Perfil.route) {
                ProfileScreen(onCerrarSesion = { navController.irLimpiando(Screen.Registro) })
            }
        }
    }
}
