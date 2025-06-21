package ar.unq.ttip.neohub.dto

data class RegisterRequest (
    val username: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val action: String, // "CREATE" o "JOIN"
    val homeName: String? = null, // Requerido para ambas acciones
    val accessKey: String? = null // Requerido para ambas acciones
)