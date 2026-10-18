# Backend Spring Boot (Museo de los Museos)

Requiere JDK 17 o superior.

    cd backend
    ./gradlew bootRun        (Windows: gradlew.bat bootRun)

Prueba en el navegador: http://localhost:8080/api/productos y http://localhost:8080/api/museos

Endpoints: GET /api/productos, /api/productos/{id}, /api/cursos, /api/museos

En la app: RepositoryProvider.USAR_DATOS_SIMULADOS = false
- Emulador: BASE_URL = http://10.0.2.2:8080/ (ya viene así)
- Celular: BASE_URL = http://IP_DE_TU_PC:8080/ (mismo Wi-Fi)
