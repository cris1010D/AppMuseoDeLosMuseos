package cl.duoc.museodelosmuseos.repository.local

import android.content.Context

// Avatares disponibles al registrarse
val AVATARES = listOf("🏛️", "🎨", "🗿", "📚", "🏺")

// Guarda el avatar y la preferencia de notificaciones de cada usuario.
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

    companion object {
        @Volatile
        private var INSTANCE: PerfilPrefs? = null

        fun getInstance(context: Context): PerfilPrefs =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: PerfilPrefs(context).also { INSTANCE = it }
            }
    }
}
