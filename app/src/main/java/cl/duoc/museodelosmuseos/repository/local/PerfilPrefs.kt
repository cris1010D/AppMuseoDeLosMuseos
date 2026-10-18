package cl.duoc.museodelosmuseos.repository.local

import android.content.Context
import java.security.MessageDigest

// Avatares disponibles al registrarse
val AVATARES = listOf("🏛️", "🎨", "🗿", "📚", "🏺")

// Guarda avatar, notificaciones y contraseña (cifrada) de cada usuario.
// Va aparte de Room para no modificar las tablas ya creadas.
class PerfilPrefs private constructor(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("perfil_museo", Context.MODE_PRIVATE)

    fun avatarDe(usuarioId: Long): String =
        prefs.getString("avatar_$usuarioId", null) ?: AVATARES.first()

    fun guardarAvatar(usuarioId: Long, avatar: String) {
        prefs.edit().putString("avatar_$usuarioId", avatar).apply()
    }

    fun notificacionesActivas(usuarioId: Long): Boolean =
        prefs.getBoolean("notif_$usuarioId", true)

    fun guardarNotificaciones(usuarioId: Long, activas: Boolean) {
        prefs.edit().putBoolean("notif_$usuarioId", activas).apply()
    }

    // Guarda la contraseña cifrada con SHA-256, nunca en texto plano
    fun guardarClave(usuarioId: Long, clave: String) {
        prefs.edit().putString("clave_$usuarioId", cifrar(clave)).apply()
    }

    // Cuentas creadas antes de este cambio no tienen clave guardada: se aceptan
    fun claveCorrecta(usuarioId: Long, clave: String): Boolean {
        val guardada = prefs.getString("clave_$usuarioId", null) ?: return true
        return guardada == cifrar(clave)
    }

    private fun cifrar(texto: String): String =
        MessageDigest.getInstance("SHA-256").digest(texto.toByteArray())
            .joinToString("") { "%02x".format(it) }

    companion object {
        @Volatile
        private var INSTANCE: PerfilPrefs? = null

        fun getInstance(context: Context): PerfilPrefs =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: PerfilPrefs(context).also { INSTANCE = it }
            }
    }
}