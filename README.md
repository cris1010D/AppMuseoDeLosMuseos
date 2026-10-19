# Museo de los Museos

Aplicación móvil Android desarrollada en **Kotlin** con **Jetpack Compose** y un **backend en Spring Boot**. Permite explorar un catálogo de libros, merchandising y cursos, ubicar museos virtuales en un mapa, escanear códigos QR, cotizar envíos y guardar museos favoritos.

Proyecto evaluativo realizado en pareja.

## Integrantes

| Integrante | Responsabilidad principal |
|---|---|
| **Cristobal Pizarro** | Base de la app: datos, base de datos local, red, mapa, escáner QR y cotizador |
| **Anderson Ovando** | Backend Spring Boot, navegación, menú principal y pantallas de registro, catálogo, detalle, favoritos y perfil |

---

## Funcionalidades

### Parte de Cristobal Pizarro

- **Mapa de museos**: muestra los museos virtuales sobre OpenStreetMap (osmdroid), con la distancia desde Santiago y la opción de marcarlos como favoritos.
- **Escáner QR**: lee códigos con la cámara (CameraX + ML Kit) y los clasifica como producto, publicación (`PUB-###`) o código desconocido. Detecta lecturas duplicadas, avisa si no hay sesión iniciada, guarda el historial por usuario y muestra el resultado con una tarjeta animada.
- **Cotizador de envíos**: carrito con cantidades y cálculo del despacho por zona (Chile, América, Resto del mundo) según el peso. Envío gratis en Chile desde USD 50 y cursos digitales sin costo de despacho.
- **Base de datos local (Room)**: tablas `usuarios`, `favoritos`, `lecturas_qr` y `productos_cache`, con sus DAOs y la sesión del usuario activo (`SessionManager`).
- **Capa de datos**: repositorio local, repositorio con datos simulados y repositorio remoto con Retrofit. `RepositoryProvider` decide cuál usar.
- **Modelos y rutas**: modelos base, `Screen.kt` con las rutas, `MainViewModel` y eventos de navegación.
- **Reglas de negocio y pruebas unitarias**: `CotizadorEnvios`, `GeoUtils` (distancia con la fórmula de Haversine) y `ValidadorQr`, cada una con su test.

### Parte de Anderson Ovando

- **Backend Spring Boot** (carpeta `backend/`): API REST con el catálogo de productos, los cursos y la lista de museos, que la app consume con Retrofit.
- **Registro / bienvenida** (`RegisterScreen`): formulario con nombre, correo, contraseña y selección de avatar, con mensajes de error de validación bajo cada campo.
- **Menú principal** (`HomeScreen`): acceso a todas las secciones de la app.
- **Catálogo** (`CatalogoScreen`): cuadrícula de productos y cursos con filtro por categoría y precios.
- **Detalle de producto o curso** (`DetalleProductoScreen`): descripción, precio, stock y botones para agregar al carrito o solicitar información.
- **Favoritos** (`FavoritosScreen`): lista de museos guardados, con acceso al recorrido virtual.
- **Perfil y ajustes** (`ProfileScreen`): datos del usuario, activación de notificaciones, centro de notificaciones y cierre de sesión.
- **Navegación** (`AppNavHost`) y componente reutilizable `PantallaConBarra` (barra superior con botón para volver).
- **ViewModels** de cada pantalla anterior, `CampoFormulario` y `PerfilPrefs` (avatar y notificaciones).
- **Cambios en archivos de Cristobal**: en `MainActivity.kt` se reemplazó el contenido temporal por `AppNavHost()`, y en `Screen.kt` se agregaron las rutas `Escaner`, `Cotizador`, `Favoritos` y `Perfil`.

---

## Tecnologías

- Kotlin, Gradle (Kotlin DSL), Jetpack Compose y Material 3
- Navigation Compose
- MVVM con `ViewModel` y `StateFlow`
- Room (con KSP)
- Retrofit, Gson y OkHttp
- osmdroid (mapa)
- CameraX y ML Kit Barcode Scanning
- Backend: Spring Boot 3 con Kotlin
- Android: minSdk 26, targetSdk 37

## Estructura del proyecto

```
AppMuseoDeLosMuseos/
├── app/src/main/java/cl/duoc/museodelosmuseos/
│   ├── MainActivity.kt          # Punto de entrada (Anderson, modificado sobre la base de Cristobal)
│   ├── model/                   # Modelos y entidades de Room (Cristobal) + CampoFormulario (Anderson)
│   ├── repository/              # Local, simulado y remoto (Cristobal) + PerfilPrefs (Anderson)
│   ├── navigation/              # Screen, MainViewModel (Cristobal) + AppNavHost (Anderson)
│   ├── viewmodel/               # Mapa, Escáner y Cotizador (Cristobal); resto (Anderson)
│   └── ui/
│       ├── components/          # PantallaConBarra (Anderson)
│       ├── screens/             # Mapa, Escáner y Cotizador (Cristobal); resto (Anderson)
│       └── theme/
└── backend/                     # API Spring Boot (Anderson)
```

La carpeta `java` es la que usa Android Studio por defecto para el código fuente; el código del proyecto está escrito en Kotlin.

## Cómo ejecutar

### 1. Backend (opcional: la app puede usar datos simulados)

Requiere JDK 17 o superior.

```bash
cd backend
./gradlew bootRun        # en Windows: gradlew.bat bootRun
```


| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/productos` | Catálogo completo (libros, merchandising y cursos) |
| GET | `/api/productos/{id}` | Un producto por su id |
| GET | `/api/cursos` | Solo los cursos |
| GET | `/api/museos` | Lista de museos virtuales |

### 2. Aplicación

1. Abrir la carpeta del proyecto en **Android Studio** y esperar a que Gradle sincronice.
2. Ejecutar en un emulador o en un celular con Android 8.0 (API 26) o superior.
3. Por defecto la app usa datos simulados. Para usar el backend, en `RepositoryProvider.kt` cambiar `USAR_DATOS_SIMULADOS` a `false`.
4. En `RemoteApi.kt` revisar `BASE_URL`:
   - Emulador: `http://10.0.2.2:8080/`
   - Celular físico: `http://IP_DE_TU_PC:8080/` (en la misma red Wi-Fi)

El escáner necesita permiso de cámara y el mapa necesita conexión a internet para descargar los mosaicos.

## Pruebas

Las pruebas unitarias están en `app/src/test/` y cubren el cotizador de envíos, el cálculo de distancias y el validador de QR.
