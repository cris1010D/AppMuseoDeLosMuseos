package cl.duoc.museodelosmuseos.repository.remote

import cl.duoc.museodelosmuseos.model.MuseoVirtual
import cl.duoc.museodelosmuseos.model.Producto
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.util.concurrent.TimeUnit

// Endpoints que debe exponer el backend Spring Boot
interface ApiService {
    @GET("api/productos")
    suspend fun obtenerProductos(): List<Producto>

    @GET("api/museos")
    suspend fun obtenerMuseos(): List<MuseoVirtual>
}

object RetrofitClient {
    // Emulador:        http://10.0.2.2:8080/
    // Celular físico:  http://IP_DE_TU_PC:8080/  (mismo Wi-Fi)
    // AWS EC2:         http://IP_PUBLICA:8080/
    private const val BASE_URL = "http://10.0.2.2:8080/"

    // Muestra en Logcat cada petición y respuesta (útil para depurar)
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}